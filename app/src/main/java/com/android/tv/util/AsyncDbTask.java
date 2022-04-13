/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
//import android.support.annotation.MainThread;
//import android.support.annotation.Nullable;
//import android.support.annotation.WorkerThread;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * {@link AsyncTask} that defaults to executing on its own single threaded Executor Service.
 *
 * @param <P> the type of the parameters sent to the task upon execution.
 * @param <S> the type of the progress units published during the background computation.
 * @param <R> the type of the result of the background computation.
 */
public abstract class AsyncDbTask<P, S, R>
        extends AsyncTask<P, S, R> {
    private static final String TAG = "AsyncDbTask";
    private static final boolean DEBUG = false;

    private final Executor mExecutor;
    boolean mCalledExecuteOnDbThread;

    protected AsyncDbTask(Executor mExecutor) {
        this.mExecutor = mExecutor;
    }

    /**
     * Returns the result of a {@link ContentResolver#query(Uri, String[], String, String[],
     * String)}.
     *
     * <p>{@link #doInBackground(Void...)} executes the query on call {@link #onQuery(Cursor)} which
     * is implemented by subclasses.
     *
     * @param <R> the type of result returned by {@link #onQuery(Cursor)}
     */
    public abstract static class AsyncQueryTask<R> extends AsyncDbTask<Void, Void, R> {
        private final WeakReference<Context> mContextReference;
        private final Uri mUri;
        private final String mSelection;
        private final String[] mSelectionArgs;
        private final String mOrderBy;
        private String[] mProjection;

        public AsyncQueryTask(
                Executor executor,
                Context context,
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String orderBy) {
            super(executor);
            mContextReference = new WeakReference<>(context);
            mUri = uri;
            mProjection = projection;
            mSelection = selection;
            mSelectionArgs = selectionArgs;
            mOrderBy = orderBy;
        }

        @Override
        protected final R doInBackground(Void... params) {
            if (!mCalledExecuteOnDbThread) {
                IllegalStateException e =
                        new IllegalStateException(
                                this
                                        + " should only be executed using executeOnDbThread, "
                                        + "but it was called on thread "
                                        + Thread.currentThread());
                Log.w(TAG, e);
            }

            if (isCancelled()) {
                // This is guaranteed to never call onPostExecute because the task is canceled.
                return null;
            }
            Context context = mContextReference.get();
            if (context == null) {
                return null;
            }
            if (DEBUG) {
                Log.v(TAG, "Starting query for " + this);
            }
            try (Cursor c =
                    context.getContentResolver().query(
                            mUri, mProjection, mSelection, mSelectionArgs, mOrderBy)) {
                if (c != null && !isCancelled()) {
                    R result = onQuery(c);
                    if (DEBUG) {
                        Log.v(TAG, "Finished query for " + this);
                    }
                    return result;
                } else {
                    if (c == null) {
                        Log.e(TAG, "Unknown query error for " + this);
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "Canceled query for " + this);
                        }
                    }
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Return the result from the cursor.
         *
         * <p><b>Note</b> This is executed on the DB thread by {@link #doInBackground(Void...)}
         */
        @WorkerThread
        protected abstract R onQuery(Cursor c);

        @Override
        public String toString() {
            return this.getClass().getName() + "(" + mUri + ")";
        }
    }

    /**
     * Returns the result of a query as an {@link List} of {@code T}.
     *
     * <p>Subclasses must implement {@link #fromCursor(Cursor)}.
     *
     * @param <T> the type of result returned in a list by {@link #onQuery(Cursor)}
     */
    public abstract static class AsyncQueryListTask<T> extends AsyncQueryTask<List<T>> {
        private final CursorFilter mFilter;

        public AsyncQueryListTask(
                Executor executor,
                Context context,
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String orderBy) {
            this(
                    executor,
                    context,
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    orderBy,
                    null);
        }

        public AsyncQueryListTask(
                Executor executor,
                Context context,
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String orderBy,
                CursorFilter filter) {
            super(executor, context, uri, projection, selection, selectionArgs, orderBy);
            mFilter = filter;
        }

        @Override
        protected final List<T> onQuery(Cursor c) {
            List<T> result = new ArrayList<>();
            while (c.moveToNext()) {
                if (isCancelled()) {
                    // This is guaranteed to never call onPostExecute because the task is canceled.
                    return null;
                }
                if (mFilter != null && !mFilter.filter(c)) {
                    continue;
                }
                T t = fromCursor(c);
                result.add(t);
            }
            if (DEBUG) {
                Log.v(TAG, "Found " + result.size() + " for  " + this);
            }
            return result;
        }

        /**
         * Return a single instance of {@code T} from the cursor.
         *
         * <p><b>NOTE</b> Do not move the cursor or close it, that is handled by {@link
         * #onQuery(Cursor)}.
         *
         * <p><b>Note</b> This is executed on the DB thread by {@link #onQuery(Cursor)}
         *
         * @param c The cursor with the values to create T from.
         */
        @WorkerThread
        protected abstract T fromCursor(Cursor c);
    }

    /**
     * Returns the result of a query as a single instance of {@code T}.
     *
     * <p>Subclasses must implement {@link #fromCursor(Cursor)}.
     */
    public abstract static class AsyncQueryItemTask<T> extends AsyncQueryTask<T> {

        public AsyncQueryItemTask(
                Executor executor,
                Context context,
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String orderBy) {
            super(executor, context, uri, projection, selection, selectionArgs, orderBy);
        }

        @Override
        protected final T onQuery(Cursor c) {
            if (c.moveToNext()) {
                if (isCancelled()) {
                    // This is guaranteed to never call onPostExecute because the task is canceled.
                    return null;
                }
                T result = fromCursor(c);
                if (c.moveToNext()) {
                    Log.w(TAG, "More than one result for found for  " + this);
                }
                return result;
            } else {
                if (DEBUG) {
                    Log.v(TAG, "No result for found  for  " + this);
                }
                return null;
            }
        }

        /**
         * Return a single instance of {@code T} from the cursor.
         *
         * <p><b>NOTE</b> Do not move the cursor or close it, that is handled by {@link
         * #onQuery(Cursor)}.
         *
         * <p><b>Note</b> This is executed on the DB thread by {@link #onQuery(Cursor)}
         *
         * @param c The cursor with the values to create T from.
         */
        @WorkerThread
        protected abstract T fromCursor(Cursor c);
    }

    /** Execute the task on {@link TvSingletons#getDbExecutor()}. */
    @MainThread
    public final void executeOnDbThread(P... params) {
        mCalledExecuteOnDbThread = true;
        executeOnExecutor(mExecutor, params);
    }

    /** An interface which filters the row. */
    public interface CursorFilter extends Filter<Cursor> {}
}
