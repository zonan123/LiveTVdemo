package com.mediatek.wwtv.tvcenter.util.tif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.twoworlds.tv.MtkTvEvent;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import android.content.ContentResolver;
import android.content.Context;
import android.media.tv.TvContract;
import android.database.Cursor;

/**
 *
 * @author sin_xinsheng
 *
 */
public final class TIFProgramManager {
    private static final String TAG = "TIFProgramManager";
    private static TIFProgramManager mTIFProgramManagerInstance;
    private Context mContext;
    private ContentResolver mContentResolver;
    private MtkTvEvent mMtkTvEvent;

    private TIFProgramManager(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mMtkTvEvent = MtkTvEvent.getInstance();
    }

    public static synchronized TIFProgramManager getInstance(Context context) {
        if (mTIFProgramManagerInstance == null) {
            mTIFProgramManagerInstance = new TIFProgramManager(context);
        }
        return mTIFProgramManagerInstance;
    }

    /**
     * get program list with group
     *
     * @return
     */
    public Map<Long, List<TIFProgramInfo>> queryProgramListWithGroup() {
        Map<Long, List<TIFProgramInfo>> channelProgramMap = new HashMap<Long, List<TIFProgramInfo>>();
        List<TIFProgramInfo> tempProgramList = null;
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                null, null, null, TvContract.Programs.COLUMN_CHANNEL_ID + ","
                        + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
        if (c == null) {
            return channelProgramMap;
        }
        TIFProgramInfo tempProgramInfo = null;
        while (c.moveToNext()) {
            tempProgramInfo = new TIFProgramInfo();
            parserTIFPrograminfo(tempProgramInfo, c, false);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramInfo>>" + tempProgramInfo.mChannelId
                    + "  " + tempProgramInfo.mTitle + "  "
                    + tempProgramInfo.mStartTimeUtcSec + "  "
                    + tempProgramInfo.mEndTimeUtcSec + "   "
                    + tempProgramInfo.mEventId);
            if (channelProgramMap.containsKey(tempProgramInfo.mChannelId)) {
                channelProgramMap.get(tempProgramInfo.mChannelId).add(
                        tempProgramInfo);
            } else {
                tempProgramList = new ArrayList<TIFProgramInfo>();
                tempProgramList.add(tempProgramInfo);
                channelProgramMap.put(tempProgramInfo.mChannelId,
                        tempProgramList);
            }
        }
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramListWithGroup channelProgramMap:"
                + channelProgramMap.size());
        return channelProgramMap;
    }

    /**
     * get program list with mId For 3rd app
     *
     * @return
     */
    public List<TIFProgramInfo> queryProgramListWithGroupFor3rd(int channelId) {
        List<TIFProgramInfo> tempProgramList = new ArrayList<TIFProgramInfo>();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramListWithGroupFor3rd channelId == "
                + channelId);
        TIFChannelInfo tIFChannelInfo = TIFChannelManager.getInstance(mContext)
                .getTIFChannelInfoById(channelId);
        if (tIFChannelInfo == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "tIFChannelInfo==null");
            return tempProgramList;
        }
        EPGChannelInfo ePGChannelInfo = new EPGChannelInfo(tIFChannelInfo);
        long startTime = EPGUtil.getCurrentTime();
        startTime = startTime * 1000;
        long endTime = startTime + 2 * 60 * 60 * 1000;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime>> " + startTime + " endTime > " + endTime);
        String cSELECTION = TvContract.Programs.COLUMN_CHANNEL_ID + " = ";
        String tempStr = "" + ePGChannelInfo.mId;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ePGChannelInfo>>" + ePGChannelInfo.mId);
        /*
         * for (EPGChannelInfo tempInfo : normalChannels) { if
         * (!tempStr.equals("(")) { tempStr += ","; } tempStr +=
         * String.valueOf(tempInfo.mId); com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramInfo>>" +
         * tempInfo.mId); } tempStr += ")";
         */
        cSELECTION += tempStr + " and "
                + TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS + " > ? and "
                + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS + " < ?";
        String[] cSELECTIONARGS = { String.valueOf(startTime),
                String.valueOf(endTime) };
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                null, cSELECTION, cSELECTIONARGS,
                TvContract.Programs.COLUMN_CHANNEL_ID + ","
                        + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
        if (c == null) {
            return tempProgramList;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ePGChannelInfo Cursor c.size   " + c.getCount());
        TIFProgramInfo tempProgramInfo = null;
        while (c.moveToNext()) {
            tempProgramInfo = new TIFProgramInfo();
            parserTIFPrograminfo(tempProgramInfo, c, false);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramListWithGroupFor3rd tempProgramInfo>>"
                    + tempProgramInfo.mChannelId + "  "
                    + tempProgramInfo.mTitle + "  "
                    + tempProgramInfo.mStartTimeUtcSec + "  "
                    + tempProgramInfo.mEndTimeUtcSec + "   "
                    + tempProgramInfo.mEventId);
            tempProgramList.add(tempProgramInfo);

        }
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " queryProgramListWithGroupFor3rd tempProgramList:"
                + tempProgramList.size());
        return tempProgramList;
    }

    public List<EPGProgramInfo> queryProgramByChannelId(int mtkChId,long chId,
            long startTime, long endTime) {
        List<EPGProgramInfo> programList = new ArrayList<EPGProgramInfo>();
        String cSELECTION = TvContract.Programs.COLUMN_CHANNEL_ID + "=" + chId
                + " and " + TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS
                + ">= ? and "
                + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ?";
        String[] cSELECTIONARGS = { String.valueOf(startTime),
                String.valueOf(endTime) };
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                null, cSELECTION, cSELECTIONARGS,
                TvContract.Programs.COLUMN_CHANNEL_ID + ","
                        + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
        if (c == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, " queryProgramByChannelId c == null!");
            return programList;
        }
        TIFProgramInfo tempProgramInfo = null;
        while (c.moveToNext()) {
            tempProgramInfo = new TIFProgramInfo();
            parserTIFPrograminfo(tempProgramInfo, c, false);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " queryProgramByChannelId tempProgramInfo ="
                    + tempProgramInfo.toString());
            if (TIFFunctionUtil.containsEvent(programList, tempProgramInfo)) {
                continue;
            }
            EPGProgramInfo tempEPGProgramInfo = new EPGProgramInfo((int) chId,
                    tempProgramInfo.mEventId, tempProgramInfo.mStartTimeUtcSec,
                    tempProgramInfo.mEndTimeUtcSec, tempProgramInfo.mTitle,
                    tempProgramInfo.mRating);
            tempEPGProgramInfo.setDescribe(tempProgramInfo.getmDescription());
            tempEPGProgramInfo
                    .setLongDescription(tempProgramInfo.mLongDescription);
            tempEPGProgramInfo.setmStartTimeStr(EPGTimeConvert.converTimeByLong2Str(tempEPGProgramInfo.getmStartTime()));
            tempEPGProgramInfo.setmEndTimeStr(EPGTimeConvert.converTimeByLong2Str(tempEPGProgramInfo.getmEndTime()));
            regetEPGProgramInfo(tempEPGProgramInfo,mtkChId);
            programList.add(tempEPGProgramInfo);
        }
        c.close();
    if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EPG_1D_SUPPORT)
        && MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
        && programList.isEmpty()){
      EPGProgramInfo tempEPGProgramInfo = new EPGProgramInfo((int) chId, -1, startTime/1000,
          endTime/1000, mContext.getResources().getString(R.string.nav_epg_no_program_title), "");
      tempEPGProgramInfo.setAppendDescription(mContext.getResources().getString(R.string.nav_epg_no_program_data));
      tempEPGProgramInfo.setmStartTimeStr(EPGTimeConvert.converTimeByLong2Str(startTime/1000));
      tempEPGProgramInfo.setmEndTimeStr(EPGTimeConvert.converTimeByLong2Str(endTime/1000));
      programList.add(tempEPGProgramInfo);
    }
        return programList;
    }


    private void regetEPGProgramInfo(EPGProgramInfo tempEPGProgramInfo,int mtkChId){
        MtkTvEventInfoBase apiEPGInfo = MtkTvEvent.getInstance().getEventInfoByEventId(tempEPGProgramInfo.getProgramId(), mtkChId);
        if (apiEPGInfo != null &&apiEPGInfo.getStartTime()>0) {
            DataReader.getInstanceWithoutContext().setMStype(tempEPGProgramInfo, apiEPGInfo.getEventCategory());
            tempEPGProgramInfo.setCategoryType(apiEPGInfo.getEventCategory());
            tempEPGProgramInfo.setHasSubTitle(apiEPGInfo.isCaption());
            tempEPGProgramInfo.setRatingType(apiEPGInfo.getEventRating());
            tempEPGProgramInfo.setRatingValue(apiEPGInfo.getEventRatingType());
            String resultDetail = DataReader.getInstanceWithoutContext().getResultDetail(
                    apiEPGInfo.getGuidanceText(), tempEPGProgramInfo.getDescribe(),
                    tempEPGProgramInfo.getLongDescription());
            resultDetail = CommonIntegration.getInstance().getAvailableStringForProg(
                    resultDetail);
            tempEPGProgramInfo.setAppendDescription(resultDetail);
        }
    }

    /**
     * get program list with group
     *
     * @return
     */
    public Map<Long, List<TIFProgramInfo>> queryProgramListWithGroupByChannelId(
            List<EPGChannelInfo> channels, long startTime) {
        List<EPGChannelInfo> ciChannels = new ArrayList<EPGChannelInfo>();
        List<EPGChannelInfo> normalChannels = new ArrayList<EPGChannelInfo>();
        for (EPGChannelInfo tempItem : channels) {
            if (tempItem.isCiVirturalCh()) {
                ciChannels.add(tempItem);
            } else {
                normalChannels.add(tempItem);
            }
        }
        // channels.removeAll(ciChannels); for fix CR:DTV00829714

        startTime = startTime * 1000;
        long endTime = startTime + 2 * 60 * 60 * 1000;
        Map<Long, List<TIFProgramInfo>> channelProgramMap = new HashMap<Long, List<TIFProgramInfo>>();
        if (normalChannels != null) {
            String cSELECTION = TvContract.Programs.COLUMN_CHANNEL_ID + " in ";
            String tempStr = "(";
            for (EPGChannelInfo tempInfo : normalChannels) {
                if (!"(".equals(tempStr)) {
                    tempStr += ",";
                }
                tempStr += String.valueOf(tempInfo.mId);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramInfo>>" + tempInfo.mId);
            }
            tempStr += ")";
            cSELECTION += tempStr + " and "
                    + TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS
                    + " > ? and "
                    + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS + " < ?";
            String[] cSELECTIONARGS = { String.valueOf(startTime),
                    String.valueOf(endTime) };
            List<TIFProgramInfo> tempProgramList = null;
            Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                    null, cSELECTION, cSELECTIONARGS,
                    TvContract.Programs.COLUMN_CHANNEL_ID + ","
                            + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
            if (c == null) {
                return channelProgramMap;
            }
            TIFProgramInfo tempProgramInfo = null;
            while (c.moveToNext()) {
                tempProgramInfo = new TIFProgramInfo();
                parserTIFPrograminfo(tempProgramInfo, c, false);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramInfo normalChannels>>"
                        + tempProgramInfo.toString());
                if (channelProgramMap.containsKey(tempProgramInfo.mChannelId)) {
                    channelProgramMap.get(tempProgramInfo.mChannelId).add(
                            tempProgramInfo);
                } else {
                    tempProgramList = new ArrayList<TIFProgramInfo>();
                    tempProgramList.add(tempProgramInfo);
                    channelProgramMap.put(tempProgramInfo.mChannelId,
                            tempProgramList);
                }
            }
            c.close();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramListWithGroup channelProgramMap:"
                + channelProgramMap.size());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "queryProgramListWithGroup ciChannels:" + ciChannels.size());
        if (ciChannels != null && (!ciChannels.isEmpty())) {
            String ciSELECTION = TvContract.Programs.COLUMN_CHANNEL_ID + " in ";
            String ciTmpStr = "(";
            for (EPGChannelInfo tempInfo : ciChannels) {
                if (!"(".equals(ciTmpStr)) {
                    ciTmpStr += ",";
                }
                ciTmpStr += String.valueOf(tempInfo.mId);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramInfo>>" + tempInfo.mId);
            }
            ciTmpStr += ")";
            List<TIFProgramInfo> tempProgramList = null;
            Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                    null, ciSELECTION + ciTmpStr, null,
                    TvContract.Programs.COLUMN_CHANNEL_ID);
            if (c == null) {
                return channelProgramMap;
            }
            TIFProgramInfo tempProgramInfo = null;
            while (c.moveToNext()) {
                tempProgramInfo = new TIFProgramInfo();
                parserTIFPrograminfo(tempProgramInfo, c, true);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "tempProgramInfo ciChannels>>"
                                + tempProgramInfo.toString());
                if (channelProgramMap.containsKey(tempProgramInfo.mChannelId)) {
                    channelProgramMap.get(tempProgramInfo.mChannelId).add(
                            tempProgramInfo);
                } else {
                    tempProgramList = new ArrayList<TIFProgramInfo>();
                    tempProgramList.add(tempProgramInfo);
                    channelProgramMap.put(tempProgramInfo.mChannelId,
                            tempProgramList);
                }
            }
            c.close();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramListWithGroup channelProgramMap:"
                + channelProgramMap.size());

        return channelProgramMap;
    }

    /**
     * get program list with group and condition
     *
     * @return
     */
    public Map<Long, List<TIFProgramInfo>> queryProgramListWithGroupCondition(
            String[] projection, String selection, String[] selectionArgs,
            String order) {
        Map<Long, List<TIFProgramInfo>> channelProgramMap = new HashMap<Long, List<TIFProgramInfo>>();
        List<TIFProgramInfo> tempProgramList = null;
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                projection, selection, selectionArgs, order);
        if (c == null) {
            return channelProgramMap;
        }
        TIFProgramInfo tempProgramInfo = null;
        while (c.moveToNext()) {
            tempProgramInfo = new TIFProgramInfo();
            parserTIFPrograminfo(tempProgramInfo, c, false);
            if (channelProgramMap.containsKey(tempProgramInfo.mChannelId)) {
                channelProgramMap.get(tempProgramInfo.mChannelId).add(
                        tempProgramInfo);
            } else {
                tempProgramList = new ArrayList<TIFProgramInfo>();
                tempProgramList.add(tempProgramInfo);
                channelProgramMap.put(tempProgramInfo.mChannelId,
                        tempProgramList);
            }
        }
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramListWithGroupCondition channelProgramMap:"
                + channelProgramMap.size());
        return channelProgramMap;
    }

    /**
     * get all programs
     *
     * @return
     */
    public List<TIFProgramInfo> queryProgramListAll() {
        List<TIFProgramInfo> tempProgramList = new ArrayList<TIFProgramInfo>();
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                null, null, null, TvContract.Programs.COLUMN_CHANNEL_ID + ","
                        + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
        if (c == null) {
            return tempProgramList;
        }
        TIFProgramInfo tempProgramInfo = null;
        while (c.moveToNext()) {
            tempProgramInfo = new TIFProgramInfo();
            parserTIFPrograminfo(tempProgramInfo, c, false);
            tempProgramList.add(tempProgramInfo);
        }
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "queryProgramListAll tempProgramList:" + tempProgramList.size());
        return tempProgramList;
    }

    /**
     * get current programs id
     *
     * @return
     */
    public long queryCurrentProgram() {

        long programsId = -1;
        TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                .getTIFChannelInfoById(
                        CommonIntegration.getInstance().getCurrentChannelId());
        if (tifChannelInfo == null) {
            return programsId;
        }
        long current = System.currentTimeMillis();
        String sSELECTION = TvContract.Programs.COLUMN_CHANNEL_ID + "="
                + tifChannelInfo.mId + " and " + current + " >= "
                + TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS + " and "
                + current + " < "
                + TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "current = " + current);
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                null, sSELECTION, null, null);
        if (c == null) {
            return programsId;
        }
        while (c.moveToNext()) {
            programsId = parserTIFProgramId(c);
        }
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryProgramId programsId:" + programsId);
        return programsId;
    }

    /**
     * get all programs with condition
     *
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param order
     * @return
     */
    public List<TIFProgramInfo> getTIFProgramListByWhereCondition(
            String[] projection, String selection, String[] selectionArgs,
            String order) {
        List<TIFProgramInfo> tempProgramList = new ArrayList<TIFProgramInfo>();
        Cursor c = mContentResolver.query(TvContract.Programs.CONTENT_URI,
                projection, selection, selectionArgs, order);
        if (c == null) {
            return tempProgramList;
        }
        TIFProgramInfo tempProgramInfo = null;
        while (c.moveToNext()) {
            tempProgramInfo = new TIFProgramInfo();
            parserTIFPrograminfo(tempProgramInfo, c, false);
            tempProgramList.add(tempProgramInfo);
        }
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFProgramListByWhereCondition tempProgramList:"
                + tempProgramList.size());
        return tempProgramList;
    }

    /**
     * parser public program info
     *
     * @param tempProgramInfo
     * @param c
     */
    private void parserTIFPrograminfo(TIFProgramInfo tempProgramInfo, Cursor c,
            boolean isVirCh) {
        tempProgramInfo.mChannelId = c.getLong(c
                .getColumnIndex(TvContract.Programs.COLUMN_CHANNEL_ID));
        tempProgramInfo.mTitle = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_TITLE));
        tempProgramInfo.mSeasonNumber = c.getInt(c
                .getColumnIndex(TvContract.Programs.COLUMN_SEASON_NUMBER));
        tempProgramInfo.mEpisodeNumber = c.getInt(c
                .getColumnIndex(TvContract.Programs.COLUMN_EPISODE_NUMBER));
        tempProgramInfo.mEpisodeTitle = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_EPISODE_TITLE));
        tempProgramInfo.mStartTimeUtcSec = c
                .getLong(c
                        .getColumnIndex(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS));
        tempProgramInfo.mStartTimeUtcSec = tempProgramInfo.mStartTimeUtcSec / 1000;// mills
                                                                                    // to
                                                                                    // seconds
        tempProgramInfo.mEndTimeUtcSec = c
                .getLong(c
                        .getColumnIndex(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS));
        tempProgramInfo.mEndTimeUtcSec = tempProgramInfo.mEndTimeUtcSec / 1000;// mills
                                                                                // to
                                                                                // seconds
        if (isVirCh) {
            tempProgramInfo.mStartTimeUtcSec = EPGUtil.getCurrentTime() / 1000;
            tempProgramInfo.mEndTimeUtcSec = (EPGUtil.getCurrentTime() / 1000)
                    + 8 * 24 * 60 * 60;
        }
        tempProgramInfo.mBroadcastGenre = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_BROADCAST_GENRE));
        tempProgramInfo.mCanonicalGenre = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_CANONICAL_GENRE));
        tempProgramInfo.mDescription = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_SHORT_DESCRIPTION));
        tempProgramInfo.mLongDescription = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_LONG_DESCRIPTION));
        tempProgramInfo.mVideoWidth = c.getInt(c
                .getColumnIndex(TvContract.Programs.COLUMN_VIDEO_WIDTH));
        tempProgramInfo.mVideoHeight = c.getInt(c
                .getColumnIndex(TvContract.Programs.COLUMN_VIDEO_HEIGHT));
        tempProgramInfo.mAudioLanguage = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_AUDIO_LANGUAGE));
        tempProgramInfo.mRating = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_CONTENT_RATING));
        tempProgramInfo.mPosterArtUri = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_POSTER_ART_URI));
        tempProgramInfo.mThumbnailUri = c.getString(c
                .getColumnIndex(TvContract.Programs.COLUMN_THUMBNAIL_URI));
        try {
            tempProgramInfo.mData = c
                    .getString(c
                            .getColumnIndex(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA));
            parserTIFProgramData(tempProgramInfo, tempProgramInfo.mData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * parser public program id
     *
     * @param c
     */
    private long parserTIFProgramId(Cursor c) {
        return c.getLong(c.getColumnIndex(TvContract.Programs._ID));
    }

    /**
     * parser event id
     *
     * @param tempProgramInfo
     * @param data
     * @return
     */
    private int[] parserTIFProgramData(TIFProgramInfo tempProgramInfo,
            String data) {
        int v[] = new int[3];
        if (data == null) {
            return v;
        }
        String value[] = data.split(",");
        if (value.length < 3) {
            return v;
        }
        tempProgramInfo.mEventId = Integer.parseInt(value[1]);
        return v;
    }

    /**
     *
     * @param channelId
     *            TV API channel id
     * @param eventId
     * @return
     */
    public MtkTvEventInfoBase getApiEventInfoById(int channelId, int eventId) {
        return mMtkTvEvent.getEventInfoByEventId(eventId, channelId);
    }

}
