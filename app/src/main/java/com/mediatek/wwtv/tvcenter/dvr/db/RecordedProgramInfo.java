package com.mediatek.wwtv.tvcenter.dvr.db;

public class RecordedProgramInfo {
    // _ID
    public long mId;

    //COLUMN_INPUT_ID
    public String   mInputId;

    //COLUMN_CHANNEL_ID
    public long     mChannelId;

    //COLUMN_TITLE
    public String   mTitle;

    //COLUMN_SEASON_DISPLAY_NUMBER
    public String   mSeasonDisplayNumber;

    //COLUMN_SEASON_TITLE
    public String   mSeasonTitle;

    //COLUMN_EPISODE_DISPLAY_NUMBER
    public String   mEpisodeDisplayNumber;

    //COLUMN_EPISODE_TITLE
    public String   mEpisodeTitle;

    //COLUMN_START_TIME_UTC_MILLIS
    public long     mStartTimeUtcMills;

    //COLUMN_END_TIME_UTC_MILLIS
    public long     mEndTimeUtcMills;

    //COLUMN_BROADCAST_GENRE
    public String   mBroadcastGenre;

    //COLUMN_CANONICAL_GENRE
    public String   mCanonicalGenre;

    //COLUMN_SHORT_DESCRIPTION
    public String   mShortDescription;

    //COLUMN_LONG_DESCRIPTION
    public String   mLongDescription;

    //COLUMN_VIDEO_WIDTH
    public int      mVideoWidth;

    //COLUMN_VIDEO_HEIGHT
    public int      mVideoHeight;

    //COLUMN_AUDIO_LANGUAGE
    public String   mAudioLanguage;

    //COLUMN_CONTENT_RATING
    public String   mContentRating;

    //COLUMN_POSTER_ART_URI
    public String   mPosterArtUri;

    //COLUMN_THUMBNAIL_URI
    public String   mThumbnallUri;

    //COLUMN_SEARCHABLE
    public boolean  mSearchable;

    //COLUMN_RECORDING_DATA_URI
    public String   mRecordingDataUri;

    //COLUMN_RECORDING_DATA_BYTES
    public long     mRecordingDataBytes;

    //COLUMN_RECORDING_DURATION_MILLIS
    public int      mRecordingDurationMills;

    //COLUMN_RECORDING_EXPIRE_TIME_UTC_MILLIS
    public long     mRecordingExpireTimeUtcMills;

    //COLUMN_INTERNAL_PROVIDER_DATA
    public byte[]   mInternalData;

    //COLUMN_INTERNAL_PROVIDER_FLAG1
    public int      mInternalFlag1;

    //COLUMN_INTERNAL_PROVIDER_FLAG2
    public int      mInternalFlag2;

    //COLUMN_INTERNAL_PROVIDER_FLAG3
    public int      mInternalFlag3;

    //COLUMN_INTERNAL_PROVIDER_FLAG4
    public int      mInternalFlag4;

    //COLUMN_VERSION_NUMBER
    public int      mVersionNumber;
    
    public String   mDetailInfo;

    @Override
    public String toString() {
        return "mInputId:" + mInputId + "," +
               "mChannelId:" + mChannelId + "," +
               "mTitle:" + mTitle + "," +
               "mSeasonDisplayNumber:" + mSeasonDisplayNumber + "," +
               "mSeasonTitle:" + mSeasonTitle + "," +
               "mEpisodeDisplayNumber:" + mEpisodeDisplayNumber + "," +
               "mEpisodeTitle:" + mEpisodeTitle + "," +
               "mStartTimeUtcMills:" + mStartTimeUtcMills + "," +
               "mEndTimeUtcMills:" + mEndTimeUtcMills + "," +
               "mRecordingDurationMills:" + mRecordingDurationMills + "," +
               "mRecordingExpireTimeUtcMills:" + mRecordingExpireTimeUtcMills + "," +
               "mVersionNumber:" + mVersionNumber +
               "mDetailInfo" + mDetailInfo; 
    }
}
