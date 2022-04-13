package com.mediatek.wwtv.tvcenter.dvr.db;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
//import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;



import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper[dvr]";

    /* schedule list */
    public static final String  SCHEDULE_DATABASE_NAME      = "alarm.db";
    public static final int     SCHEDULE_DATABASE_VERSION   = 1;
    public static final String  SCHEDULE_CONTACTS_TABLE     = "scheduledvr";
    public static final String  SCHEDULE_ALARM_ACTION       = "com.mediatek.wwtv.tvcenter.schedule.dvr";

    private static final String SCHEDULE_DATABASE_CREATE =
                    "CREATE TABLE " + SCHEDULE_CONTACTS_TABLE +" ("
                    + AlarmColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + AlarmColumn.TASKID + " TEXT,"
                    + AlarmColumn.INPUTSOURCE + " TEXT NOT NULL,"
                    + AlarmColumn.CHANNELNUM + " TEXT NOT NULL,"
                    + AlarmColumn.CHANNELNAME + " TEXT,"
                    + AlarmColumn.STARTTIME + " INTEGER,"
                    + AlarmColumn.ENDTIME + " INTEGER,"
                    + AlarmColumn.SCHEDULETYPE + " INTEGER,"    //0,record; 1,reminder.
                    + AlarmColumn.REPEATTYPE + " INTEGER,"  //0,once; 1,daily; 2,week;
                    + AlarmColumn.DAYOFWEEK + " INTEGER,"
                    + AlarmColumn.ISENABLE + " INTEGER,"
                    + AlarmColumn.CHANNELID + " TEXT NOT NULL);";

    /* record list */
    private static final Uri CONTENT_URI =
        TvContract.RecordedPrograms.CONTENT_URI;

    private static final String ORDERBY =
//        TvContract.RecordedPrograms.COLUMN_CHANNEL_ID  + "," +
        TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS;

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final AlarmManager mAlarm;
    private PendingIntent mPendingIntent = null;

    private static DBHelper mDBHelper = null;

    public DBHelper(Context context) {
        super(context, SCHEDULE_DATABASE_NAME, null, SCHEDULE_DATABASE_VERSION);

        mContext = context;
        mContentResolver = context.getContentResolver();
        mAlarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public synchronized static DBHelper getInstance(Context context) {
        if(mDBHelper==null){
            mDBHelper = new DBHelper(context);
        }

      return mDBHelper;
    }

    /**
     * onCreate
     *
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCHEDULE_DATABASE_CREATE);
    }

    /**
     * onUpgrade
     *
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_CONTACTS_TABLE);
        onCreate(db);
    }

    /**
     * updateAlarm
     *
     */
    public void updateAlarm() {
        new Thread(mUpdateAlarm).start();
    }

    private final Runnable mUpdateAlarm = new Runnable() {
        @Override
        public void run() {
            synchronized (DBHelper.class) {
                if(null != mPendingIntent) {
                    mAlarm.cancel(mPendingIntent);
                }

                Cursor c = mContentResolver.query(
                    ScheduleDVRProvider.CONTENT_URI,
                    null,
                    null,
                    null,
                    AlarmColumn.STARTTIME + " ASC");

                if(null == c) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateAlarm, c null");
                    return ;
                }

                if(c.moveToNext()) {
                    String channelId =
                        c.getString(c.getColumnIndex(AlarmColumn.CHANNELID));
                    long startTime =
                        c.getLong(c.getColumnIndex(AlarmColumn.STARTTIME));
                    long endTime =
                        c.getLong(c.getColumnIndex(AlarmColumn.ENDTIME));

                    Intent intent = new Intent(SCHEDULE_ALARM_ACTION);
                    intent.putExtra(AlarmColumn.STARTTIME, startTime);
                    intent.putExtra(AlarmColumn.ENDTIME, endTime);
                    intent.putExtra(AlarmColumn.CHANNELID, channelId);

                    mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

                    mAlarm.set(AlarmManager.RTC, startTime, mPendingIntent);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateAlarm, mAlarm.set, " + channelId + "," +
                        startTime + "," + endTime);
                }

                c.close();
            }// synchronized
        }
    };

    /**
     * getRecordedList
     *
     */
    public static List<RecordedProgramInfo> getRecordedList(Context content) {
        List<RecordedProgramInfo> programList = new ArrayList<RecordedProgramInfo>();
        ContentResolver mContentResolver = content.getContentResolver();
        Cursor c = mContentResolver.query(CONTENT_URI, null, null, null, ORDERBY);
        if (c == null) {
            return programList;
        }

        while (c.moveToNext()) {
            RecordedProgramInfo info = new RecordedProgramInfo();

            parserRecordedProgramInfo(info, c);
            programList.add(info);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRecordedList, " + info);
        }
        c.close();

        com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "getRecordedList, " + programList.size());
        return programList;
    }

    public static int deleteRecordById(Context content, long id) {
        String selection = TvContract.RecordedPrograms._ID + " = " + id;
        return content.getContentResolver().delete(CONTENT_URI, selection, null);
    }

    /**
     * getRecorded
     *
     */
    public static RecordedProgramInfo getRecordedInfoById(Context content, long id) {
        RecordedProgramInfo program = new RecordedProgramInfo();
        ContentResolver mContentResolver = content.getContentResolver();
        String selection = TvContract.RecordedPrograms._ID + "=" + id;
        Cursor c = mContentResolver.query(CONTENT_URI, null, selection, null, null);

        if (c == null) {
            return null;
        }

        if (c.moveToNext()) {
            parserRecordedProgramInfo(program, c);
        }

        c.close();

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRecordedInfoById, " + program);
        return program;
    }

    /**
     * parserRecordedProgramInfo
     *
     */
    private static void parserRecordedProgramInfo(RecordedProgramInfo info, Cursor c) {
        info.mId = c.getLong(
                c.getColumnIndex(TvContract.RecordedPrograms._ID));

        info.mInputId = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INPUT_ID));

        info.mChannelId = c.getLong(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_CHANNEL_ID));

        info.mTitle = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_TITLE));

        info.mSeasonDisplayNumber = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_SEASON_DISPLAY_NUMBER));

        info.mSeasonTitle = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_SEASON_TITLE));

        info.mEpisodeDisplayNumber = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_EPISODE_DISPLAY_NUMBER));

        info.mEpisodeTitle = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_EPISODE_TITLE));

        info.mStartTimeUtcMills = c.getLong(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS));

        info.mEndTimeUtcMills = c.getLong(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS));

        info.mBroadcastGenre = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_BROADCAST_GENRE));

        info.mCanonicalGenre = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_CANONICAL_GENRE));

        info.mShortDescription = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_SHORT_DESCRIPTION));

        info.mLongDescription = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_LONG_DESCRIPTION));

        info.mVideoWidth = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_VIDEO_WIDTH));

        info.mVideoHeight = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_VIDEO_HEIGHT));

        info.mAudioLanguage = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_AUDIO_LANGUAGE));

        info.mContentRating = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_CONTENT_RATING));

        info.mPosterArtUri = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_POSTER_ART_URI));

        info.mThumbnallUri = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_THUMBNAIL_URI));

        info.mSearchable = c.getInt(c.getColumnIndex(
            TvContract.RecordedPrograms.COLUMN_SEARCHABLE)) == 1 ? true : false;

        info.mRecordingDataUri = c.getString(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_RECORDING_DATA_URI));
     
        info.mRecordingDataBytes = c.getLong(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_RECORDING_DATA_BYTES));

        info.mRecordingDurationMills = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS));

        info.mRecordingExpireTimeUtcMills = c.getLong(c.getColumnIndex(
            TvContract.RecordedPrograms.COLUMN_RECORDING_EXPIRE_TIME_UTC_MILLIS));

        info.mInternalData = c.getBlob(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA));

        info.mInternalFlag1 = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1));

        info.mInternalFlag2 = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG2));

        info.mInternalFlag3 = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG3));

        info.mInternalFlag4 = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG4));

        info.mVersionNumber = c.getInt(
            c.getColumnIndex(TvContract.RecordedPrograms.COLUMN_VERSION_NUMBER));
    }
}
