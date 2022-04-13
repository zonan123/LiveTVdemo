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

package com.mediatek.wwtv.tvcenter.search;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import android.media.tv.TvContract.Programs;
import android.media.tv.TvContract.WatchedPrograms;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import androidx.annotation.WorkerThread;

import com.mediatek.wwtv.tvcenter.search.LocalSearchProvider.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link SearchInterface} to search query from TvProvider directly.
 */
public class TvProviderSearch implements SearchInterface {
    private static final boolean DEBUG = true;
    private static final String TAG = "TvProviderSearch";

    private static final int NO_LIMIT = 0;

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final TvInputManager mTvInputManager;

    TvProviderSearch(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);

        com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager.enableAllChannels(context);
    }

    /**
     * Search channels, inputs, or programs from TvProvider.
     * This assumes that parental control settings will not be change while searching.
     *
     * @param action One of {@link #ACTION_TYPE_SWITCH_CHANNEL}, {@link #ACTION_TYPE_SWITCH_INPUT},
     *               or {@link #ACTION_TYPE_AMBIGUOUS},
     */
    @Override
    @WorkerThread
    public List<SearchResult> search(String query, int limit, int action) {
        List<SearchResult> results = new ArrayList<>();
        Set<Long> channelsFound = new HashSet<>();
        if (action == ACTION_TYPE_SWITCH_CHANNEL) {
            results.addAll(searchChannels(query, channelsFound, limit));
        } else if (action == ACTION_TYPE_SWITCH_INPUT) {
            results.addAll(searchInputs(query, limit));
        } else {
            // Search channels first.
            results.addAll(searchChannels(query, channelsFound, limit));
            if (results.size() >= limit) {
                return results;
            }

            // In case the user wanted to perform the action "switch to XXX", which is indicated by
            // setting the limit to 1, search inputs.
            //if (limit == 1) {
                results.addAll(searchInputs(query, limit));
                if (!results.isEmpty()) {
                    return results;
                }
            //}

            // Lastly, search programs.
            limit -= results.size();
            results.addAll(searchPrograms(query, null, new String[] {
                    Programs.COLUMN_TITLE, Programs.COLUMN_SHORT_DESCRIPTION },
                    channelsFound, limit));
        }
        return results;
    }

    private void appendSelectionString(StringBuilder sb, String[] columnForExactMatching,
            String[] columnForPartialMatching) {
        boolean firstColumn = true;
        if (columnForExactMatching != null) {
            for (String column : columnForExactMatching) {
                if (!firstColumn) {
                    sb.append(" OR ");
                } else {
                    firstColumn = false;
                }
                sb.append(column).append("=?");
            }
        }
        if (columnForPartialMatching != null) {
            for (String column : columnForPartialMatching) {
                if (!firstColumn) {
                    sb.append(" OR ");
                } else {
                    firstColumn = false;
                }
                sb.append(column).append(" LIKE ?");
            }
        }
    }

    private void insertSelectionArgumentStrings(String[] selectionArgs, int pos,
            String query, String[] columnForExactMatching, String[] columnForPartialMatching) {
        if (columnForExactMatching != null) {
            int until = pos + columnForExactMatching.length;
            for (; pos < until; ++pos) {
                selectionArgs[pos] = query;
            }
        }
        String selectionArg = "%" + query + "%";
        if (columnForPartialMatching != null) {
            int until = pos + columnForPartialMatching.length;
            for (; pos < until; ++pos) {
                selectionArgs[pos] = selectionArg;
            }
        }
    }

    @WorkerThread
    private List<SearchResult> searchChannels(String query, Set<Long> channels, int limit) {
        List<SearchResult> results = new ArrayList<>();
        if (TextUtils.isDigitsOnly(query)) {
            results.addAll(searchChannels(query, new String[] { Channels.COLUMN_DISPLAY_NUMBER },
                    null, channels, NO_LIMIT));
            if (results.size() > 1) {
                Collections.sort(results, new ChannelComparatorWithSameDisplayNumber());
            }
        }
        if (results.size() < limit) {
            results.addAll(searchChannels(query, null,
                    new String[] { Channels.COLUMN_DISPLAY_NAME, Channels.COLUMN_DESCRIPTION },
                    channels, limit - results.size()));
        }
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }
        for (SearchResult result : results) {
            fillProgramInfo(result);
        }
        return results;
    }

    @WorkerThread
    private List<SearchResult> searchChannels(String query, String[] columnForExactMatching,
            String[] columnForPartialMatching, Set<Long> channelsFound, int limit) {
        //Assert.assertTrue(
        //   (columnForExactMatching != null && columnForExactMatching.length > 0) ||
        //   (columnForPartialMatching != null && columnForPartialMatching.length > 0));

        String[] projection = {
                Channels._ID,
                Channels.COLUMN_DISPLAY_NUMBER,
                Channels.COLUMN_DISPLAY_NAME,
                Channels.COLUMN_DESCRIPTION
        };

        StringBuilder sb = new StringBuilder();
        sb.append(Channels.COLUMN_BROWSABLE).append("=1 AND ")
                .append(Channels.COLUMN_SEARCHABLE).append("=1 AND ");
        sb.append(Channels.COLUMN_TYPE).append("!='TYPE_PREVIEW' AND ")
                .append(Channels.COLUMN_INPUT_ID).append("!=''");
        if (mTvInputManager.isParentalControlsEnabled()) {
            sb.append(" AND ").append(Channels.COLUMN_LOCKED).append("=0");
        }
        sb.append(" AND (");
        appendSelectionString(sb, columnForExactMatching, columnForPartialMatching);
        sb.append(")");
        String selection = sb.toString();

        int len = (columnForExactMatching == null ? 0 : columnForExactMatching.length) +
                (columnForPartialMatching == null ? 0 : columnForPartialMatching.length);
        String[] selectionArgs = new String[len];
        insertSelectionArgumentStrings(selectionArgs, 0, query, columnForExactMatching,
                columnForPartialMatching);

        List<SearchResult> searchResults = new ArrayList<>();

        try (Cursor c = mContentResolver.query(Channels.CONTENT_URI, projection, selection,
                selectionArgs, null)) {
            if (c != null) {
                int count = 0;
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    // Filter out the channel which has been already searched.
                    if (channelsFound.contains(id)) {
                        continue;
                    }
                    channelsFound.add(id);

                    SearchResult result = new SearchResult();
                    result.channelId = id;
                    result.channelNumber = c.getString(1);
                    result.title = c.getString(2);
                    result.description = c.getString(3);
                    result.imageUri = Uri.parse("android.resource://com.mediatek.wwtv.tvcenter/drawable/icon").toString();
                    result.intentAction = Intent.ACTION_VIEW;
                    result.intentData = buildIntentData(result.channelId);
                    result.contentType = Programs.CONTENT_ITEM_TYPE;
                    result.isLive = true;
                    result.progressPercentage = LocalSearchProvider.PROGRESS_PERCENTAGE_HIDE;

                    searchResults.add(result);
                    ++count;
                    if (limit != NO_LIMIT && count >= limit) {
                        break;
                    }
                }
            }
        }
        return searchResults;
    }

    /**
     * Replaces the channel information - title, description, channel logo - with the current
     * program information of the channel if the current program information exists and it is not
     * blocked.
     */
    @WorkerThread
    private void fillProgramInfo(SearchResult result) {
        long now = System.currentTimeMillis();
        Uri uri = TvContract.buildProgramsUriForChannel(result.channelId, now, now);
        String[] projection = new String[] {
                Programs.COLUMN_TITLE,
                Programs.COLUMN_POSTER_ART_URI,
                Programs.COLUMN_CONTENT_RATING,
                Programs.COLUMN_VIDEO_WIDTH,
                Programs.COLUMN_VIDEO_HEIGHT,
                Programs.COLUMN_START_TIME_UTC_MILLIS,
                Programs.COLUMN_END_TIME_UTC_MILLIS
        };

        try (Cursor c = mContentResolver.query(uri, projection, null, null, null)) {
            if (c != null && c.moveToNext() && !isRatingBlocked(c.getString(2))) {
                String channelName = result.title;
                long startUtcMillis = c.getLong(5);
                long endUtcMillis = c.getLong(6);
                result.title = c.getString(0);
                result.description = buildProgramDescription(result.channelNumber, channelName,
                        startUtcMillis, endUtcMillis);
                String imageUri = c.getString(1);
                if (imageUri != null) {
                    result.imageUri = imageUri;
                }
                result.videoWidth = c.getInt(3);
                result.videoHeight = c.getInt(4);
                result.duration = endUtcMillis - startUtcMillis;
                result.progressPercentage = getProgressPercentage(startUtcMillis, endUtcMillis);
            }
        }
    }

    private String buildProgramDescription(String channelNumber, String channelName,
            long programStartUtcMillis, long programEndUtcMillis) {
        return getDurationString(mContext, programStartUtcMillis, programEndUtcMillis, false)
                + System.lineSeparator() + channelNumber + " " + channelName;
    }

    private int getProgressPercentage(long startUtcMillis, long endUtcMillis) {
        long current = System.currentTimeMillis();
        if (startUtcMillis > current || endUtcMillis <= current) {
            return LocalSearchProvider.PROGRESS_PERCENTAGE_HIDE;
        }
        if(endUtcMillis - startUtcMillis == 0){
            return 0;
        }
        return (int)(100 * (current - startUtcMillis) / (endUtcMillis - startUtcMillis));
    }

    @WorkerThread
    private List<SearchResult> searchPrograms(String query, String[] columnForExactMatching,
            String[] columnForPartialMatching, Set<Long> channelsFound, int limit) {
        //Assert.assertTrue(
        //        (columnForExactMatching != null && columnForExactMatching.length > 0) ||
        //        (columnForPartialMatching != null && columnForPartialMatching.length > 0));

        String[] projection = {
                Programs.COLUMN_CHANNEL_ID,
                Programs.COLUMN_TITLE,
                Programs.COLUMN_POSTER_ART_URI,
                Programs.COLUMN_CONTENT_RATING,
                Programs.COLUMN_VIDEO_WIDTH,
                Programs.COLUMN_VIDEO_HEIGHT,
                Programs.COLUMN_START_TIME_UTC_MILLIS,
                Programs.COLUMN_END_TIME_UTC_MILLIS
        };

        StringBuilder sb = new StringBuilder();
        // Search among the programs which are now being on the air.
        sb.append(Programs.COLUMN_START_TIME_UTC_MILLIS).append("<=? AND ");
        sb.append(Programs.COLUMN_END_TIME_UTC_MILLIS).append(">=? AND (");
        appendSelectionString(sb, columnForExactMatching, columnForPartialMatching);
        sb.append(")");
        String selection = sb.toString();

        int len = (columnForExactMatching == null ? 0 : columnForExactMatching.length) +
                (columnForPartialMatching == null ? 0 : columnForPartialMatching.length);
        String[] selectionArgs = new String[len + 2];
        selectionArgs[0] = selectionArgs[1] = String.valueOf(System.currentTimeMillis());
        insertSelectionArgumentStrings(selectionArgs, 2, query, columnForExactMatching,
                columnForPartialMatching);

        List<SearchResult> searchResults = new ArrayList<>();

        if (DEBUG) {
            Log.d(TAG, "searchPrograms(" + projection + ", selection=" + selection + selectionArgs + ")");
        }

        try (Cursor c = mContentResolver.query(Programs.CONTENT_URI, projection, selection,
                selectionArgs, null)) {
            if (c != null) {
                int count = 0;
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    // Filter out the program whose channel is already searched.
                    if (channelsFound.contains(id)) {
                        continue;
                    }
                    channelsFound.add(id);

                    // Don't know whether the channel is searchable or not.
                    String[] channelProjection = {
                            Channels._ID,
                            Channels.COLUMN_DISPLAY_NUMBER,
                            Channels.COLUMN_DISPLAY_NAME
                    };
                    sb = new StringBuilder();
                    sb.append(Channels._ID).append("=? AND ")
                            .append(Channels.COLUMN_BROWSABLE).append("=1 AND ")
                            .append(Channels.COLUMN_SEARCHABLE).append("=1");
                    if (mTvInputManager.isParentalControlsEnabled()) {
                        sb.append(" AND ").append(Channels.COLUMN_LOCKED).append("=0");
                    }
                    String selectionChannel = sb.toString();
                    try (Cursor cChannel = mContentResolver.query(Channels.CONTENT_URI,
                            channelProjection, selectionChannel,
                            new String[] { String.valueOf(id) }, null)) {
                        if (cChannel != null && cChannel.moveToNext()
                                && !isRatingBlocked(c.getString(3))) {
                            long startUtcMillis = c.getLong(6);
                            long endUtcMillis = c.getLong(7);
                            SearchResult result = new SearchResult();
                            result.channelId = c.getLong(0);
                            result.title = c.getString(1);
                            result.description = buildProgramDescription(cChannel.getString(1),
                                    cChannel.getString(2), startUtcMillis, endUtcMillis);
                            result.imageUri = c.getString(2);
                            result.intentAction = Intent.ACTION_VIEW;
                            result.intentData = buildIntentData(id);
                            result.contentType = Programs.CONTENT_ITEM_TYPE;
                            result.isLive = true;
                            result.videoWidth = c.getInt(4);
                            result.videoHeight = c.getInt(5);
                            result.duration = endUtcMillis - startUtcMillis;
                            result.progressPercentage = getProgressPercentage(startUtcMillis,
                                    endUtcMillis);
                            searchResults.add(result);
                            ++count;
                            if (limit != NO_LIMIT && count >= limit) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return searchResults;
    }

    private String buildIntentData(long channelId) {
        return TvContract.buildChannelUri(channelId).buildUpon()
                .appendQueryParameter("source", SOURCE_TV_SEARCH)
                .build().toString();
    }

    private boolean isRatingBlocked(String ratings) {
        if (ratings == null) {
            return false;
        }
        for (String rating : ratings.split("\\s*,\\s*")) {
            try {
                if (mTvInputManager.isParentalControlsEnabled() && mTvInputManager.isRatingBlocked(
                        TvContentRating.unflattenFromString(rating))) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private List<SearchResult> searchInputs(String query, int limit) {
        if (DEBUG) {
            Log.d(TAG, "searchInputs(" + query + ", limit=" + limit + ")");
        }

        query = canonicalizeLabel(query);
        List<TvInputInfo> inputList = mTvInputManager.getTvInputList();
        List<SearchResult> results = new ArrayList<>();

        // Find exact matches first.
        for (TvInputInfo input : inputList) {
            if(input.getType() == TvInputInfo.TYPE_TUNER && !input.getId().startsWith("com.mediatek.tvinput")) {
                continue;
            }
            String label = canonicalizeLabel(input.loadLabel(mContext));
            String customLabel = canonicalizeLabel(input.loadCustomLabel(mContext));
            if (TextUtils.equals(query, label) || TextUtils.equals(query, customLabel)) {
                results.add(buildSearchResultForInput(input));
                if (!results.isEmpty()) {
                    return results;
                }
            }
        }

        // Then look for partial matches.
        for (TvInputInfo input : inputList) {
            if(input.getType() == TvInputInfo.TYPE_TUNER && !input.getId().startsWith("com.mediatek.tvinput")) {
                continue;
            }
            String label = canonicalizeLabel(input.loadLabel(mContext));
            String customLabel = canonicalizeLabel(input.loadCustomLabel(mContext));
            if ((label != null && label.contains(query)) ||
                    (customLabel != null && customLabel.contains(query))) {
                results.add(buildSearchResultForInput(input));
                if (results.size() >= limit) {
                    return results;
                }
            }
        }
        return results;
    }

    private String canonicalizeLabel(CharSequence cs) {
        Locale locale = mContext.getResources().getConfiguration().locale;
        return cs != null ? cs.toString().replaceAll("[ -]", "").toLowerCase(locale) : null;
    }

    private SearchResult buildSearchResultForInput(TvInputInfo input) {
        SearchResult result = new SearchResult();

        result.title = input.loadLabel(mContext).toString();
        result.imageUri = Uri.parse("android.resource://com.mediatek.wwtv.tvcenter/drawable/icon").toString();
        result.intentAction = Intent.ACTION_VIEW;
        result.intentData = TvContract.buildChannelUriForPassthroughInput(input.getId()).toString();
        return result;
    }

    @WorkerThread
    private class ChannelComparatorWithSameDisplayNumber implements Comparator<SearchResult> {
        private final Map<Long, Long> mMaxWatchStartTimeMap = new HashMap<>();

        @Override
        public int compare(SearchResult lhs, SearchResult rhs) {
            // Show recently watched channel first
            Long lhsMaxWatchStartTime = mMaxWatchStartTimeMap.get(lhs.channelId);
            if (lhsMaxWatchStartTime == null) {
                lhsMaxWatchStartTime = getMaxWatchStartTime(lhs.channelId);
                mMaxWatchStartTimeMap.put(lhs.channelId, lhsMaxWatchStartTime);
            }
            Long rhsMaxWatchStartTime = mMaxWatchStartTimeMap.get(rhs.channelId);
            if (rhsMaxWatchStartTime == null) {
                rhsMaxWatchStartTime = getMaxWatchStartTime(rhs.channelId);
                mMaxWatchStartTimeMap.put(rhs.channelId, rhsMaxWatchStartTime);
            }
            if (!Objects.equals(lhsMaxWatchStartTime, rhsMaxWatchStartTime)) {
                return Long.compare(rhsMaxWatchStartTime, lhsMaxWatchStartTime);
            }
            // Show recently added channel first if there's no watch history.
            return Long.compare(rhs.channelId, lhs.channelId);
        }

        private long getMaxWatchStartTime(long channelId) {
            Uri uri = WatchedPrograms.CONTENT_URI;
            String[] projections = new String[] {
                    "MAX(" + WatchedPrograms.COLUMN_START_TIME_UTC_MILLIS
                    + ") AS max_watch_start_time"
            };
            String selection = WatchedPrograms.COLUMN_CHANNEL_ID + "=?";
            String[] selectionArgs = new String[] { Long.toString(channelId) };
            try (Cursor c = mContentResolver.query(uri, projections, selection, selectionArgs,
                    null)) {
                if (c != null && c.moveToNext()) {
                    return c.getLong(0);
                }
            }
            return -1;
        }
    }

    /**
     * Returns duration string according to the date & time format.
     * If {@code startUtcMillis} and {@code endUtcMills} are equal,
     * formatted time will be returned instead.
     *
     * @param startUtcMillis start of duration in millis. Should be less than {code endUtcMillis}.
     * @param endUtcMillis end of duration in millis. Should be larger than {@code startUtcMillis}.
     * @param useShortFormat {@code true} if abbreviation is needed to save space.
     *                       In that case, date will be omitted if duration starts from today
     *                       and is less than a day. If it's necessary,
     *                       {@link DateUtils#FORMAT_NUMERIC_DATE} is used otherwise.
     */
    public static String getDurationString(
            Context context, long startUtcMillis, long endUtcMillis, boolean useShortFormat) {
        return getDurationString(context, System.currentTimeMillis(), startUtcMillis, endUtcMillis,
                useShortFormat, 0);
    }

    static String getDurationString(Context context, long baseMillis,
            long startUtcMillis, long endUtcMillis, boolean useShortFormat, int flag) {
        flag |= DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME
                | ((useShortFormat) ? DateUtils.FORMAT_NUMERIC_DATE : 0);
        if (!isInGivenDay(baseMillis, startUtcMillis)) {
            flag |= DateUtils.FORMAT_SHOW_DATE;
        }
        if (startUtcMillis != endUtcMillis && useShortFormat) {
            // Do special handling for 12:00 AM when checking if it's in the given day.
            // If it's start, it's considered as beginning of the day. (e.g. 12:00 AM - 12:30 AM)
            // If it's end, it's considered as end of the day (e.g. 11:00 PM - 12:00 AM)
            if (!isInGivenDay(startUtcMillis, endUtcMillis - 1)
                    && endUtcMillis - startUtcMillis < TimeUnit.HOURS.toMillis(11)) {
                // Do not show date for short format.
                // Extracting a day is needed because {@link DateUtils@formatDateRange}
                // adds date if the duration covers multiple days.
                return DateUtils.formatDateRange(context,
                        startUtcMillis, endUtcMillis - TimeUnit.DAYS.toMillis(1), flag);
            }
        }
        return DateUtils.formatDateRange(context, startUtcMillis, endUtcMillis, flag);
    }

    public static boolean isInGivenDay(long dayToMatchInMillis, long subjectTimeInMillis) {
        final long dayInMS = TimeUnit.DAYS.toMillis(1);
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        long offset = timeZone.getRawOffset();
        if (timeZone.inDaylightTime(new Date(dayToMatchInMillis))) {
            offset += timeZone.getDSTSavings();
        }
        return (dayToMatchInMillis + offset - ((dayToMatchInMillis + offset) % dayInMS))
                == (subjectTimeInMillis + offset - ((subjectTimeInMillis + offset) % dayInMS));
    }
}
