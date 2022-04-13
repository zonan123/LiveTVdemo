package com.mediatek.wwtv.tvcenter.dvr.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;



public class ScheduleDVRProvider extends ContentProvider {
    private static final String TAG = "ScheduleDVRProvider";

    private DBHelper mDbHelper;
    private SQLiteDatabase mDvrDB;

    public static final String AUTHORITY = "dvr.alarm";
    public static final String CONTACTS_TABLE = DBHelper.SCHEDULE_CONTACTS_TABLE;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + CONTACTS_TABLE);

    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CONTACTS_TABLE, ITEM);
        uriMatcher.addURI(AUTHORITY, CONTACTS_TABLE + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = DBHelper.getInstance(getContext());
        mDvrDB = mDbHelper.getWritableDatabase();

        mDbHelper.updateAlarm();
        return (mDvrDB == null) ? false : true;
    }

    @Override
    public int delete(Uri uri, String where, String[] selectionArgs) {
        // TODO Auto-generated method stub
        int count;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "delete. " + uriMatcher.match(uri));
        switch (uriMatcher.match(uri)) {
        case ITEM:
            count = mDvrDB.delete(CONTACTS_TABLE, where, selectionArgs);
            break;
        case ITEM_ID:
            long contactID = ContentUris.parseId(uri);//uri.getPathSegments().get(1);
            count = mDvrDB.delete(
                    CONTACTS_TABLE,
                    AlarmColumn.TASKID+ "="+ contactID+ (!TextUtils.isEmpty(where) ? " and (" + where
                                    + ")" : ""), selectionArgs);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "delete. " + ContentUris.parseId(uri) +
                "___id:" + uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        mDbHelper.updateAlarm();

        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        switch (uriMatcher.match(uri)) {
        case ITEM:
            return "";
        case ITEM_ID:
            return "";
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        if ((uriMatcher.match(uri) != ITEM)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        switch (uriMatcher.match(uri)) {
        case ITEM:
            if (initialValues != null) {
                values = new ContentValues(initialValues);
            } else {
                values = new ContentValues();
            }
            Long now1 = Long.valueOf(System.currentTimeMillis());

            if (!values.containsKey(AlarmColumn.INPUTSOURCE)) {
                values.put(AlarmColumn.INPUTSOURCE, "DTV");
            }
            if (!values.containsKey(AlarmColumn.CHANNELNUM)) {
                values.put(AlarmColumn.CHANNELNUM, now1);
            }
            if (!values.containsKey(AlarmColumn.STARTTIME)) {
                values.put(AlarmColumn.STARTTIME, "");
            }
            if (!values.containsKey(AlarmColumn.ENDTIME)) {
                values.put(AlarmColumn.ENDTIME, "");
            }
            if (!values.containsKey(AlarmColumn.SCHEDULETYPE)) {
                values.put(AlarmColumn.SCHEDULETYPE, -1);
            }
            if (!values.containsKey(AlarmColumn.REPEATTYPE)) {
                values.put(AlarmColumn.REPEATTYPE, "");
            }
            if (!values.containsKey(AlarmColumn.DAYOFWEEK)) {
                values.put(AlarmColumn.DAYOFWEEK, 0);
            }
            if (!values.containsKey(AlarmColumn.ISENABLE)) {
                values.put(AlarmColumn.ISENABLE, 1);
            }
            if (!values.containsKey(AlarmColumn.CHANNELID)) {
                values.put(AlarmColumn.CHANNELID, -1);
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, values.toString());
            long rowId1 = mDvrDB.insert(CONTACTS_TABLE, null, values);
            if (rowId1 > 0) {
                Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId1);
                getContext().getContentResolver().notifyChange(noteUri, null);

                mDbHelper.updateAlarm();

                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "insert, Success.row: " + noteUri.toString());
                return noteUri;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "insert, fail");
            break;
        case ITEM_ID:
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "case CONTACT_ID");
            return null;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);

        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {

        case ITEM:
            qb.setTables(CONTACTS_TABLE);
            break;

        case ITEM_ID:
            qb.setTables(CONTACTS_TABLE);
            qb.appendWhere(AlarmColumn.TASKID + "=" + uri.getPathSegments().get(1));
            break;
        default:
            break;
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = AlarmColumn.TASKID;
        } else {
            orderBy = sortOrder;
        }

        try {
            Cursor c = qb.query(mDvrDB, projection, selection, selectionArgs,
                    null, null, orderBy);

            c.setNotificationUri(getContext().getContentResolver(), uri);

            return c;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] selectionArgs) {
        int count;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "update, " + values.toString() +
            "," + uri.toString() + "," + uriMatcher.match(uri));

        switch (uriMatcher.match(uri)) {
        case ITEM:
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "update, " +ITEM);

            count = mDvrDB.update(
                    CONTACTS_TABLE,
                    values,
                    AlarmColumn.TASKID
                            + "="
                            + values.getAsString(AlarmColumn.TASKID)
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ")" : ""), selectionArgs);

            break;
        case ITEM_ID:
            String contactID1 = uri.getPathSegments().get(1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG + "update", contactID1 + "");
            count = mDvrDB.update(
                    CONTACTS_TABLE,
                    values,
                    AlarmColumn.TASKID
                            + "="
                            + contactID1
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ")" : ""), selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        mDbHelper.updateAlarm();

        return count;
    }
}
