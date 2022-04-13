
package com.mediatek.wwtv.tvcenter.util.tif;

import com.mediatek.wwtv.setting.scan.EditChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.CopyOnWriteArraySet;

import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView.BlockChecker;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.input.AtvInput;
import com.mediatek.wwtv.tvcenter.nav.input.DtvInput;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.WeakHandler;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo.CustomerComparator;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.MtkTvChannelListBase;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvMultiView;
import com.android.tv.util.AsyncDbTask;
import com.android.tv.util.Utils;

import android.app.ActivityManager;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager.TvInputCallback;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.MutableInt;
import android.util.Log;
import android.database.ContentObserver;
import android.database.Cursor;

/**
 * TIF channel get and process
 *
 * @author sin_xinsheng
 */
public class TIFChannelManager {
  private static final String TAG = "TIFChannelManager";
  private static final String SELECTION = "";// TvContract.Channels.COLUMN_INPUT_ID + " = ?"
  // private static final String INPUT_DTV_ID = "com.mediatek.tvinput/.dtv.DTVInputService/HW0";
  // private static final String INPUT_ATV_ID = "com.mediatek.tvinput/.atv.ATVInputService/HW1";
  private static final String ORDERBY = "substr(cast(" +
      TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA + " as varchar),19,10)";// DTV00607832
  private static final String SELECTION_WITH_SVLID = SELECTION + "substr(cast(" +
      TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA + " as varchar),7,5) = ?";
  private static final String SELECTION_WITH_SVLID_CHANNELID = SELECTION_WITH_SVLID +
      " and substr(cast(" + TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA +
      " as varchar),19,10) = ?";
  private static final String SELECTION_WITH_SVLID_INPUTID = SELECTION_WITH_SVLID
          + " and substr(" + TvContract.Channels.COLUMN_INPUT_ID
          + " ,length(" + TvContract.Channels.COLUMN_INPUT_ID + ")-2,3) = ?";
  private static TIFChannelManager mTIFChannelManagerInstance;
  private final Context mContext;
  private final ContentResolver mContentResolver;
  private static final boolean mGetApiChannelFromSvlRecd = true;
  CommonIntegration mCI;
  private int CURRENT_CHANNEL_SORT = 0;
  // private TIFChannelInfo mPreTIFChannelInfo, mPreCatcheTIFChannelInfo;

  private static final String SPNAME = "CHMODE";
  Map<Integer,MtkTvChannelInfoBase> maps =new HashMap<>();
  private BlockChecker mChecker;
  private  boolean getAllChannelsrunning=false;
  private  boolean upDateAllChannelsrunning=false;
  private  boolean afterGetAllChanelsGoonsort=false;
  private  boolean updateChannelIntercept = false;
  private TIFChannelManager(Context context) {
    mContext = context;
    mContentResolver = mContext.getContentResolver();
    mCI = TvSingletons.getSingletons().getCommonIntegration();

    init();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIFChannelManager() mChannels.size() = " + getChannelCount());
  }

  public synchronized static TIFChannelManager getInstance(Context context) {
    if (mTIFChannelManagerInstance == null ) {
      mTIFChannelManagerInstance = new TIFChannelManager(context.getApplicationContext());
    }

    return mTIFChannelManagerInstance;
  }

  public ContentResolver getContentResolver() {
    if (mContentResolver == null) {
      return mContext.getContentResolver();
    }
    return mContentResolver;
  }

  public TIFChannelInfo getPreChannelInfo() {
    return getTIFChannelInfoById(mCI.getLastChannelId());
  }

  /**
   * get current TIF channelinfo
   *
   * @return
   */
  public TIFChannelInfo getCurrChannelInfo() {
        return getTIFChannelInfoById(mCI.getCurrentChannelId());
    }

    /**
     * get current TIF channelinfo by channelUri
     *
     * @return
     */
    public TIFChannelInfo getChannelInfoByUri() {
        if(TurnkeyUiMainActivity.getInstance() !=null && TurnkeyUiMainActivity.getInstance().getTvView() != null){
            long channelId =TurnkeyUiMainActivity.getInstance().getTvView().getCurrentChannelId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelInfoByUri channelId = " + channelId);
            return getChannel(channelId);
        }else{
            return null;
        }

    }

    /**
     * get all channels with mask
     *
     * @param attentionMask
     * @param expectValue
     * @return
     */
    public List<TIFChannelInfo> queryChanelListAllForEPG(int attentionMask, int expectValue) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryChanelListAll~");
      List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
//      Map<Integer,MtkTvChannelInfoBase> maps = getAllChannels();
      List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListAll   mChannelList.size() " + mChannelList.size());
      int curId = mCI.getCurrentChannelId();
      for( TIFChannelInfo temTIFChannel : mChannelList){
        // only US EPG use hidden channel, other region not be used
        if ((!CommonIntegration.isUSRegion() && !temTIFChannel.mIsBrowsable) || temTIFChannel.isAnalogService() 
                || (temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != curId ) ) {
          continue;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryChanelListAll~ temTIFChannel : "+temTIFChannel);
        temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
        if (TIFFunctionUtil.checkChMaskformDataValue(temTIFChannel, attentionMask, expectValue)) {
            if (TIFFunctionUtil.checkChCategoryMask(temTIFChannel.mMtkTvChannelInfo, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                tIFChannelInfoList.add(temTIFChannel);
            }
        }else if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && TIFFunctionUtil.is3rdTVSource(temTIFChannel)){
            tIFChannelInfoList.add(temTIFChannel);
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end queryChanelListAll  tIFChannelInfoList>>>" + tIFChannelInfoList.size());
      return tIFChannelInfoList;
    }
    
  /**
   * get all channels with mask
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public List<TIFChannelInfo> queryChanelListAll(int attentionMask, int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryChanelListAll~");
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
//    Map<Integer,MtkTvChannelInfoBase> maps = getAllChannels();
    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListAll   mChannelList.size() " + (mChannelList==null?mChannelList:mChannelList.size()));
    for( TIFChannelInfo temTIFChannel : mChannelList){
      // only US EPG use hidden channel, other region not be used
      if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
        continue;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryChanelListAll~ temTIFChannel : "+temTIFChannel);
      temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
      if (TIFFunctionUtil.checkChMaskformDataValue(temTIFChannel, attentionMask, expectValue)) {
          if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
              temTIFChannel.mMtkTvChannelInfo.setChannelNumber(CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(temTIFChannel.mMtkTvChannelInfo.getChannelNumber()));
           }
      //  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
        tIFChannelInfoList.add(temTIFChannel);
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end queryChanelListAll  tIFChannelInfoList>>>" + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  /**
   * get all channels with mask
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public List<TIFChannelInfo> queryRegionChanelListAll(int attentionMask, int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryChanelListAll~");
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, selection,
        selectionargs, ORDERBY);
    if (c == null) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListAll   c>>>" + c.getCount() + "   ORDERBY>>" + ORDERBY);
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
      // only US EPG use hidden channel, other region not be used
      if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
        continue;
      }
   //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
          temTIFChannel.mDataValue);
      if (tempApiChannel != null && TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
        if(tempApiChannel instanceof MtkTvDvbChannelInfo){
            MtkTvDvbChannelInfo channelInfo = (MtkTvDvbChannelInfo)tempApiChannel;
            String svcProName = channelInfo.getSvcProName();
            if(svcProName.startsWith("ORF [Region")){
                 tIFChannelInfoList.add(temTIFChannel);
            }
        }

      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end queryChanelListAll  tIFChannelInfoList>>>" + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }
  /**
   * get all channels with mask
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public TIFChannelInfo getFirstChannelForScan() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getFirstChannelForScan~");
    String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }
    int tryCount = 0;
    while(tryCount <= 2){
        Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, selection,
                selectionargs, ORDERBY);
        if (c == null) {
            return null;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFirstChannelForScan   c>>>" + c.getCount() + "   ORDERBY>>" + ORDERBY);
        TIFChannelInfo temTIFChannel = null;
        while (c.moveToNext()) {
            temTIFChannel = TIFChannelInfo.parse(c);
            // only US EPG use hidden channel, other region not be used
            if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
                continue;
            }
        //    parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                    temTIFChannel.mDataValue);
            if (TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL)) {
                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getFirstChannelForScan  temTIFChannel>>>" + temTIFChannel);
                c.close();
                return temTIFChannel;
            }
        }
        c.close();
        tryCount ++;
        try {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Thread sleep beacuse not get channel");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getFirstChannelForScan null>>> tryCount="+tryCount);
    }
    return null;
  }
  public List<TIFChannelInfo> queryRegionChannelForHDAustria() {
      List<TIFChannelInfo> result = new ArrayList<TIFChannelInfo>();
      int length = CommonIntegration.getInstance().getChannelNumByAPIForBanner();
      List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration.getInstance()
              .getChannelListByMaskFilter(0, MtkTvChannelListBase.CHLST_ITERATE_DIR_FROM_FIRST,
                        length,
                        CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
      List<TIFChannelInfo> tifChannelInfos = TIFFunctionUtil.getTIFChannelList(channelBaseList);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "size:"+(channelBaseList==null?channelBaseList:channelBaseList.size()));
      for (TIFChannelInfo tifChannelInfo : tifChannelInfos) {
        if(tifChannelInfo.mMtkTvChannelInfo  instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo channelInfo = (MtkTvDvbChannelInfo)tifChannelInfo.mMtkTvChannelInfo;
            String svcProName = channelInfo.getSvcProName();
            Log.d("===", "svcProName="+svcProName);
             if(svcProName.startsWith("ORF [Region")) {
                result.add(tifChannelInfo);
            }
        }
      }
      return result;
  }

  public Map<String, List<TIFChannelInfo>> queryRegionChannelForDiveo() {
      Map<String, List<TIFChannelInfo>> result = new LinkedHashMap<String, List<TIFChannelInfo>>();
      result.put("BR Fernsehen", new ArrayList<TIFChannelInfo>());
      result.put("MDR", new ArrayList<TIFChannelInfo>());
      result.put("NDR", new ArrayList<TIFChannelInfo>());
      result.put("RBB", new ArrayList<TIFChannelInfo>());
      result.put("SWR", new ArrayList<TIFChannelInfo>());
      result.put("WDR", new ArrayList<TIFChannelInfo>());
      int length = CommonIntegration.getInstance().getChannelNumByAPIForBanner();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("queryRegionChannelForDiveo","length="+length);
      List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration.getInstance()
              .getChannelListByMaskFilter(0, MtkTvChannelListBase.CHLST_ITERATE_DIR_FROM_FIRST,
                        length,
                        CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
      List<TIFChannelInfo> tifChannelInfos = TIFFunctionUtil.getTIFChannelList(channelBaseList);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryRegionChannelForDiveo size:"+(channelBaseList==null?channelBaseList:channelBaseList.size()));
      for (TIFChannelInfo tifChannelInfo : tifChannelInfos) {
        if(tifChannelInfo.mMtkTvChannelInfo  instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo channelInfo = (MtkTvDvbChannelInfo)tifChannelInfo.mMtkTvChannelInfo;
            String svcProName = channelInfo.getSvcProName();
            Log.d(TAG, "queryRegionChannelForDiveo svcProName="+svcProName);
            if(svcProName.startsWith("BR [Region")) {
                result.get("BR Fernsehen").add(tifChannelInfo);
            } else if(svcProName.startsWith("MDR [Region")) {
                result.get("MDR").add(tifChannelInfo);
            } else if(svcProName.startsWith("NDR [Region")) {
                result.get("NDR").add(tifChannelInfo);
            } else if(svcProName.startsWith("RBB [Region")) {
                result.get("RBB").add(tifChannelInfo);
            } else if(svcProName.startsWith("SWR [Region")) {
                result.get("SWR").add(tifChannelInfo);
            } else if(svcProName.startsWith("WDR [Region")) {
                result.get("WDR").add(tifChannelInfo);
            }
        }
      }

      Iterator<Entry<String, List<TIFChannelInfo>>> iterator = result.entrySet().iterator();
      while (iterator.hasNext()) {
          if(iterator.next().getValue().size() == 0) {
              iterator.remove();
          }
      }
      return result;
  }
  
  public Map<String, List<TIFChannelInfo>> queryRegionChannelForFastScan() {
      Map<String, List<TIFChannelInfo>> result = new LinkedHashMap<String, List<TIFChannelInfo>>();
      int length = CommonIntegration.getInstance().getChannelNumByAPIForBanner();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("queryRegionChannelForFastScan","length="+length);
      List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration.getInstance()
              .getChannelListByMaskFilter(0, MtkTvChannelListBase.CHLST_ITERATE_DIR_FROM_FIRST,
                      length,
                      CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
      List<TIFChannelInfo> tifChannelInfos = TIFFunctionUtil.getTIFChannelList(channelBaseList);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryRegionChannelForFastScan size:"+channelBaseList.size());
      for (TIFChannelInfo tifChannelInfo : tifChannelInfos) {
          if(tifChannelInfo.mMtkTvChannelInfo  instanceof MtkTvDvbChannelInfo) {
              MtkTvDvbChannelInfo channelInfo = (MtkTvDvbChannelInfo)tifChannelInfo.mMtkTvChannelInfo;
              String svcProName = channelInfo.getSvcProName();
              Log.d(TAG, "queryRegionChannelForFastScan svcProName="+svcProName);
              if(svcProName.contains("[Region")) {
                  String regionName = svcProName.substring(0, svcProName.indexOf("[Region")).trim();
                  if(!result.containsKey(regionName)){
                      result.put(regionName, new ArrayList<TIFChannelInfo>());
                  }
                  result.get(regionName).add(tifChannelInfo);
              }
          }
      }
      return result;
  }
  /**
    * get current programs id
    *
    * @return
    */
   public TIFChannelInfo queryChannelById(int id) {

       TIFChannelInfo tifChannelInfo = null;
       String sELECTION = TvContract.Channels._ID + "="
               + id +" or "+TvContract.Channels.COLUMN_INTERNAL_PROVIDER_FLAG3 + "=" + id;
       Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, sELECTION, null,
               null);
       if (c == null) {
           return null;
       }
       int svlid = mCI.getSvl();
       while (c.moveToNext()) {
           tifChannelInfo = TIFChannelInfo.parse(c);
           if(svlid == tifChannelInfo.mInternalProviderFlag1){
               tifChannelInfo.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(tifChannelInfo.mDataValue);
               c.close();
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChannelById:" + tifChannelInfo);
               return tifChannelInfo;
           }
          
       }
       c.close();
       return null;
   }

   /**
    * get current hidden id
    *
    * @return hide channel ,select chanel by number
    */
   public TIFChannelInfo getHideChannelById(int channelId) {

       TIFChannelInfo tifChannelInfo = null;
       Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID_CHANNELID, getSvlIdAndChannelIdSelectionArgs(channelId), ORDERBY);
       if (c == null) {
           return tifChannelInfo;
       }
       while (c.moveToNext()) {
           tifChannelInfo = TIFChannelInfo.parse(c);
    //       parserTIFChannelData(tifChannelInfo, tifChannelInfo.mData);
           tifChannelInfo.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(
                   tifChannelInfo.mDataValue);
       }
       c.close();
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getHideChannelById:" + tifChannelInfo);
       return tifChannelInfo;
   }

   public List<TIFChannelInfo> queryChanelListWithMaskCount(int count, int attentionMask,
              int expectValue) {
       return queryChanelListWithMaskCount(count,attentionMask,expectValue,false);
   }


   public Map<Integer,MtkTvChannelInfoBase> getAllChannels() {
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannels:" );
       getAllChannelsrunning=true;
       upDateAllChannelsrunning=false;
       List<MtkTvChannelInfoBase> mtkInfoList = mCI.getChannelListForMap();
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannels mtkInfoList.size: "+ (mtkInfoList==null?mtkInfoList:mtkInfoList.size()));
       Map<Integer,MtkTvChannelInfoBase> mapstemp =new HashMap<>();
       if(mtkInfoList != null && !mtkInfoList.isEmpty()){
          for(MtkTvChannelInfoBase info:mtkInfoList){
       //       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannels info " +info.getServiceName()+" info.getChannelId() "+info.getChannelId());
              mapstemp.put(info.getChannelId(),info);

          }
       }
     //  maps.clear();
       maps.putAll(mapstemp);
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannels maps.size: "+ maps.size());
       getAllChannelsrunning=false;
       if(afterGetAllChanelsGoonsort){
           afterGetAllChanelsGoonsort();
       }
       if(upDateAllChannelsrunning){
           getAllChannels();
       }
      return maps;
   }

  /**
   * get channels with mask and count
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */

  public List<TIFChannelInfo> queryChanelListWithMaskCount(int count, int attentionMask,
      int expectValue,boolean showSkip) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryChanelListWithMaskCount~" + count+" attentionMask: "+attentionMask+" expectValue: " +expectValue);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    if (count <= 0) {
      return tIFChannelInfoList;
    }
/*    String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }*/
   // Map<Integer,MtkTvChannelInfoBase> maps = getAllChannels();
    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    if (mChannelList == null || mChannelList.isEmpty()) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListWithMask  mChannelList.size "+mChannelList.size());
     for( TIFChannelInfo temTIFChannel : mChannelList){
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListWithMask ==== temTIFChannel: "+temTIFChannel);
      // only US EPG use hidden channel, other region not be used
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListWithMask ==== isUSRegion: "+CommonIntegration.isUSRegion()+"  temTIFChannel.mIsBrowsable:"+temTIFChannel.mIsBrowsable);
      if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
        continue;
      }
      if((attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL)){ // deal 3rd channels
          if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListWithMask  3rd channel temTIFChannel: "+temTIFChannel);
              tIFChannelInfoList.add(temTIFChannel);
              if (tIFChannelInfoList.size() == count) {
                break;
              }
          }
      }else{
          if(mCI.isDisableColorKey()){
              if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListWithMask isDisableColorKey 3rd channel temTIFChannel: "+temTIFChannel);
                  tIFChannelInfoList.add(temTIFChannel);
                  if (tIFChannelInfoList.size() == count) {
                    break;
                  }
                  continue;
              }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryChanelListWithMask  not 3rd channel temTIFChannel: "+temTIFChannel);
          temTIFChannel.mMtkTvChannelInfo =getAPIChannelInfoByChannelId( temTIFChannel.mInternalProviderFlag3);
          if(temTIFChannel.mMtkTvChannelInfo == null || (!showSkip && temTIFChannel.mMtkTvChannelInfo.isSkip() && temTIFChannel.mMtkTvChannelInfo.getChannelId() != mCI.getCurrentChannelId())){
              continue;
          }
          if (TIFFunctionUtil.checkChMask(temTIFChannel.mMtkTvChannelInfo, attentionMask, expectValue)) {  //
              //  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                tIFChannelInfoList.add(temTIFChannel);
                if (tIFChannelInfoList.size() == count) {
                  break;
                }
           }/*else if(TIFFunctionUtil.checkChMask(temTIFChannel.mMtkTvChannelInfo, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
               tIFChannelInfoList.add(temTIFChannel);
               if (tIFChannelInfoList.size() == count) {
                 break;
               }
           }*/
      }

    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end queryChanelListWithMaskCount  tIFChannelInfoList>>>"
        + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }


  /**
   * get channels with mask and count
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */

  public List<TIFChannelInfo> queryDukChanelListWithMaskCount(int count, int attentionMask,
      int expectValue,int sdtServiceType,boolean showSkip) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start queryDukChanelListWithMaskCount~" + count+" attentionMask: "+attentionMask+" expectValue: " +expectValue +" sdtServiceType:" +sdtServiceType);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    if (count <= 0) {
      return tIFChannelInfoList;
    }

    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    if (mChannelList == null || mChannelList.isEmpty()) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryDukChanelListWithMaskCount  mChannelList.size "+mChannelList.size());
     for( TIFChannelInfo temTIFChannel : mChannelList){

          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "queryDukChanelListWithMaskCount  not 3rd channel temTIFChannel: "+temTIFChannel);
          temTIFChannel.mMtkTvChannelInfo =getAPIChannelInfoByChannelId( temTIFChannel.mInternalProviderFlag3);
          if(temTIFChannel.mMtkTvChannelInfo != null && (!showSkip && temTIFChannel.mMtkTvChannelInfo.isSkip() && temTIFChannel.mMtkTvChannelInfo.getChannelId() != mCI.getCurrentChannelId())){
              continue;
          }
          if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                tIFChannelInfoList.add(temTIFChannel);
                if (tIFChannelInfoList.size() == count) {
                  break;
                }
           }

    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end queryDukChanelListWithMaskCount  tIFChannelInfoList>>>"
        + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  /**
   * get all channes with mask and where condition
   *
   * @param projection
   * @param selection
   * @param selectionArgs
   * @param order
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public List<TIFChannelInfo> getTIFChannelListByWhereCondition(String[] projection,
      String selection, String[] selectionArgs, String order
      , int attentionMask, int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFChannelListByWhereCondition~" + selection);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    int svlId = mCI.getSvl();
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, projection,
        selection, selectionArgs, order);
    if (c == null) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelListByWhereCondition c>>>>" + c.getCount());
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
      // only US EPG use hidden channel, other region not be used
      if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
        continue;
      }
   //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      if (svlId != temTIFChannel.mDataValue[0]) {
        continue;
      }
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
          temTIFChannel.mDataValue);
      if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
        tIFChannelInfoList.add(temTIFChannel);
      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelListByWhereCondition  tIFChannelInfoList>>>"
        + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  public List<TIFChannelInfo> getAllDTVTIFChannels() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getAllDTVTIFChannels~");
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
  //  Map<Integer,MtkTvChannelInfoBase> maps = getAllChannels();
    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllDTVTIFChannels mChannelList.size()>>>> " + (mChannelList==null?mChannelList:mChannelList.size()));
   for(TIFChannelInfo temTIFChannel:mChannelList) {
      // only US EPG use hidden channel, other region not be used
      if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
        continue;
      }

      MtkTvChannelInfoBase temvpApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
      if (temvpApiChannel != null
          && temvpApiChannel.getBrdcstType() != MtkTvChCommonBase.BRDCST_TYPE_ANALOG
          && TIFFunctionUtil.checkChMask(temvpApiChannel, TIFFunctionUtil.CH_LIST_MASK,TIFFunctionUtil.CH_LIST_VAL)) {
        temTIFChannel.mMtkTvChannelInfo = temvpApiChannel;
        tIFChannelInfoList.add(temTIFChannel);
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getAllDTVTIFChannels  tIFChannelInfoList>>>"
        + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  public List<TIFChannelInfo> getAllATVTIFChannels() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getAllATVTIFChannels~");
      List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
   //   Map<Integer,MtkTvChannelInfoBase> maps = getAllChannels();
      List<TIFChannelInfo> mChannelList = getCurrentSVLChannelListBase();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllATVTIFChannels mChannelList.size()>>>> " + (mChannelList==null?mChannelList:mChannelList.size()));
     if (mChannelList != null) {
         for (TIFChannelInfo temTIFChannel : mChannelList) {
             // only US EPG use hidden channel, other region not be used
             if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
                 continue;
             }

             MtkTvChannelInfoBase temvpApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
             if (temvpApiChannel != null
                     && temvpApiChannel.getBrdcstType() == MtkTvChCommonBase.BRDCST_TYPE_ANALOG
                     && TIFFunctionUtil.checkChMask(temvpApiChannel, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL)) {
                 temTIFChannel.mMtkTvChannelInfo = temvpApiChannel;
                 tIFChannelInfoList.add(temTIFChannel);
             }
         }
     }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getAllATVTIFChannels  tIFChannelInfoList>>>"
          + tIFChannelInfoList.size());
      return tIFChannelInfoList;
    }
  /**
   * get previous or next n channels with mask and start channel id
   *
   * @param startChannelId
   * @param isPrePage
   * @param count
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public List<TIFChannelInfo> getTIFPreOrNextChannelList(int startChannelId, boolean isPrePage,
      boolean containsStartChId, int count, int attentionMask, int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFPreOrNextChannelList>" + startChannelId + ">>"
        + isPrePage + ">>" + containsStartChId);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    if (count <= 0) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFPreOrNextChannelList  attentionMask: "+attentionMask+" expectValue: " +expectValue);
    long newId =(int) (startChannelId & 0xffffffffL);
    int beforeStartIdCount = 0;
    int afterStartIdCount = 0;
    boolean canAddData = false;
    if (startChannelId == -1) {
      canAddData = true;
    }
    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isPrePage>>>" + isPrePage
        + "  mChannelList.size() = " + (mChannelList==null?mChannelList:mChannelList.size()));
    int currentChId= mCI.getCurrentChannelId();
    if (isPrePage) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList temTIFChannel isPrePage");
        TIFChannelInfo temTIFChannel = null;
        for(int i=mChannelList.size()-1 ; i>=0;i-- ){
          temTIFChannel = mChannelList.get(i);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1 getTIFPreOrNextChannelList temTIFChannel = "+temTIFChannel);
        //  parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
          if (canAddData) {
              if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextChannelList> 3rd");
                  if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable){
                      tIFChannelInfoList.add(0, temTIFChannel);
                  }
                  if (tIFChannelInfoList.size() == count) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextChannelList>");
                      break;
                  }
              }else{
                    // only US EPG use hidden channel, other region not be used
                    if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
                      continue;
                    }
                    if(mCI.isDisableColorKey()){
                        if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isDisableColorKey 3rd channel temTIFChannel: "+temTIFChannel);
                            tIFChannelInfoList.add(0, temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                              break;
                            }
                            continue;
                        }
                    }
                    MtkTvChannelInfoBase tempApiChannel =  getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                    if (tempApiChannel == null || ( tempApiChannel.isSkip() && tempApiChannel.getChannelId() != currentChId)) {
                      continue;
                    } else {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChannel>>>" + tempApiChannel.getChannelId()
                          + "   >" + tempApiChannel.getChannelNumber()
                          + "  >" + tempApiChannel.getServiceName());
                    }
                    if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask,
                        expectValue)) {
                        if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                            temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                            if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                            }
                            tIFChannelInfoList.add(0, temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextChannelList>");
                                break;
                            }

                          }
                    }else if(TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextChannelList> add current fake channel");
                        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                        tIFChannelInfoList.add(0, temTIFChannel);
                        if (tIFChannelInfoList.size() == count) {
                          break;
                        }
                    }
              }
          } else {

            if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
              canAddData = true;
                if (containsStartChId) {
                  if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFPreOrNextChannelList> 3rd");
                      if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable){
                          tIFChannelInfoList.add(0, temTIFChannel);
                      }
                      if (tIFChannelInfoList.size() == count) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFPreOrNextChannelList>");
                          break;
                      }
                  }else{
                      if(mCI.isDisableColorKey()){
                          if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isDisableColorKey 3rd channel temTIFChannel: "+temTIFChannel);
                              tIFChannelInfoList.add(0, temTIFChannel);
                              if (tIFChannelInfoList.size() == count) {
                                break;
                              }
                              continue;
                          }
                      }
                      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                      if (tempApiChannel == null || ( tempApiChannel.isSkip() && tempApiChannel.getChannelId() != currentChId)) {
                            continue;
                          } else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChannel>>>" +tempApiChannel);
                          }
                          if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                              if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                                  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                  if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                      temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                                  }
                                  tIFChannelInfoList.add(0, temTIFChannel);
                                  if (tIFChannelInfoList.size() == count) {
                                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFPreOrNextChannelList>");
                                      break;
                                  }

                                }
                          }else if(TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFPreOrNextChannelList> add current fake channel");
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                tIFChannelInfoList.add(0, temTIFChannel);
                                if (tIFChannelInfoList.size() == count) {
                                  break;
                                }
                            }

                  }

              } else {
                // if (TIFFunctionUtil.checkChMask(tempApiChannel,
                // attentionMask, expectValue)) {
                afterStartIdCount++;
                // }
              }
            } else {
              // if (TIFFunctionUtil.checkChMask(tempApiChannel,
              // attentionMask, expectValue)) {
              afterStartIdCount++;
              // }
            }
          }
        }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pretIFChannelInfoList>>>" + tIFChannelInfoList.size()
          + "   afterStartIdCount>>" + afterStartIdCount);
      if (tIFChannelInfoList.size() < count && afterStartIdCount > 0) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList first not get page channel, this is second");
          for(int i=mChannelList.size()-1 ; i>=0;i-- ){
              temTIFChannel = mChannelList.get(i);
              if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelList>3rd 3");
                  if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable){
                      tIFChannelInfoList.add(0, temTIFChannel);
                  }
                  if (tIFChannelInfoList.size() == count) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelList>3rd");
                    break;
                  }
              }else{
                  if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
                      // only US EPG use
                      // hidden channel,
                      // other region not
                    continue;
                  }
                  if(mCI.isDisableColorKey()){
                      if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isDisableColorKey 3rd channel temTIFChannel: "+temTIFChannel);
                          tIFChannelInfoList.add(0, temTIFChannel);
                          if (tIFChannelInfoList.size() == count) {
                            break;
                          }
                          continue;
                      }
                  }
                 MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                 if (tempApiChannel == null || ( tempApiChannel.isSkip() && tempApiChannel.getChannelId() != currentChId)) {
                    continue;
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChannel>>>" + tempApiChannel.getChannelId() + "   >"
                        + tempApiChannel.getChannelNumber()
                        + "  >" + tempApiChannel.getServiceName());
                  }
                  if (tempApiChannel.getChannelId() != startChannelId) {
                    if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                        if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                            temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                            if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                            }
                            tIFChannelInfoList.add(0, temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelList>");
                              break;
                            }

                          }

                    }else if(TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelList> add current fake channel");
                        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                        tIFChannelInfoList.add(0, temTIFChannel);
                        if (tIFChannelInfoList.size() == count) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelList>");
                          break;
                        }
                    }
                  } else {
                    if (!containsStartChId) {
                      if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                          if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                              temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                              if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                  temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                              }
                              tIFChannelInfoList.add(0, temTIFChannel);
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFPreOrNextChannelList>");
                            }

                        }

                    }
                    break;
                  }
              }
          }
      }
    } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel nextpage  canAddData:"+ canAddData);
      for(TIFChannelInfo temTIFChannel : mChannelList){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList  nextpage temTIFChannel is " + temTIFChannel);
        if (canAddData) {
            if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelList>3rd 5");
                if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                    tIFChannelInfoList.add(temTIFChannel);
                }
                if (tIFChannelInfoList.size() == count) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelList>3rd");
                  break;
                }
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran getTIFPreOrNextChannelList =================");
                  if(temTIFChannel == null){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel is null!");
                      continue;
                  }
                  if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {// only US EPG use
                                                                                     // hidden channel,
                                                                                     // other region not be
                                                                                     // used
                    continue;
                  }
                  if(mCI.isDisableColorKey()){
                      if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isDisableColorKey nextpage 3rd channel temTIFChannel: "+temTIFChannel);
                          tIFChannelInfoList.add(temTIFChannel);
                          if (tIFChannelInfoList.size() == count) {
                            break;
                          }
                          continue;
                      }
                  }
                  MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                  if (tempApiChannel == null || ( tempApiChannel.isSkip() && tempApiChannel.getChannelId() != currentChId)) {
                    continue;
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChannel>>>" + tempApiChannel.getChannelId() + "   >"
                        + tempApiChannel.getChannelNumber()
                        + "  >" + tempApiChannel.getServiceName());
                  }
                  if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                      temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                      if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                          if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                              temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                          }
                          tIFChannelInfoList.add(temTIFChannel);
                          if (tIFChannelInfoList.size() == count) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelList>");
                            break;
                          }
                        }

                  }else if(TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelList> add current fake channel");
                      temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                      tIFChannelInfoList.add(temTIFChannel);
                      if (tIFChannelInfoList.size() == count) {
                        break;
                      }
                  }
            }
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran getTIFPreOrNextChannelList  newId:"+newId);
          if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
            canAddData = true;
            if (containsStartChId) {
                if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelList>3rd 6");
                    if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                        tIFChannelInfoList.add(temTIFChannel);
                    }
                    if (tIFChannelInfoList.size() == count) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelList>3rd");
                      break;
                    }
                }else {
                    if(mCI.isDisableColorKey()){
                        if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isDisableColorKey nextpage 3rd channel temTIFChannel: "+temTIFChannel);
                            tIFChannelInfoList.add(temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                              break;
                            }
                            continue;
                        }
                    }
                    MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                    if (tempApiChannel == null || ( tempApiChannel.isSkip() && tempApiChannel.getChannelId() != currentChId)) {
                      continue;
                    } else {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChannel>>>" + tempApiChannel.getChannelId() + "   >"
                          + tempApiChannel.getChannelNumber()
                          + "  >" + tempApiChannel.getServiceName());
                    }


                    if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {           
                        if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                            temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                            if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                            }
                            tIFChannelInfoList.add(temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelList>");
                                break;
                            }
                          }
                        
                    }else if(TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelList> add current fake channel");
                        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                        tIFChannelInfoList.add(temTIFChannel);
                        if (tIFChannelInfoList.size() == count) {
                          break;
                        }
                    }
                }

            } else {
              // if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              beforeStartIdCount++;
              // }
            }
          } else {
            // if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
            beforeStartIdCount++;
            // }
          }
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nexttIFChannelInfoList>>>" + tIFChannelInfoList.size()
          + "   beforeStartIdCount>>" + beforeStartIdCount);
      if (tIFChannelInfoList.size() < count && beforeStartIdCount > 0) {

     //   if (c.moveToFirst()) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList 2first not get page channel, this is second");
         for(TIFChannelInfo temTIFChannel:mChannelList){
          //  temTIFChannel = TIFChannelInfo.parse(c);
             if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelList>3rd 6");
                 if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                     tIFChannelInfoList.add(temTIFChannel);
                 }
                 if (tIFChannelInfoList.size() == count) {
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelList>3rd");
                   break;
                 }
             }else{
                    if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {// only US EPG use
                                                                                       // hidden channel,
                                                                                       // other region not
                                                                                       // be used
                      continue;
                    }
                    if(mCI.isDisableColorKey()){
                        if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextChannelList isDisableColorKey nextpage 3rd channel temTIFChannel: "+temTIFChannel);
                            tIFChannelInfoList.add(temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                              break;
                            }
                            continue;
                        }
                    }
               //     parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
                    MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                    if (tempApiChannel == null || ( tempApiChannel.isSkip() && tempApiChannel.getChannelId() != currentChId)) {
                      continue;
                    } else {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChannel>>>" + tempApiChannel.getChannelId() + "   >"
                          + tempApiChannel.getChannelNumber()
                          + "  >" + tempApiChannel.getServiceName());
                    }
                    if (tempApiChannel.getChannelId() != startChannelId) {
                      if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                          if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                              if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                  temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                              }
                              tIFChannelInfoList.add(temTIFChannel);
                              if (tIFChannelInfoList.size() == count) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 7 getTIFPreOrNextChannelList>");
                                break;
                              }
                          }

                      }else if(TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 7 getTIFPreOrNextChannelList> add current fake channel");
                          temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                          tIFChannelInfoList.add(temTIFChannel);
                          if (tIFChannelInfoList.size() == count) {
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 7 getTIFPreOrNextChannelList>");
                            break;
                          }
                      }
                    } else {
                      if (!containsStartChId) {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                            if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                                    temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(tempApiChannel.getChannelNumber());
                                }
                                tIFChannelInfoList.add(temTIFChannel);
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 8 getTIFPreOrNextChannelList>");
                            }

                        }
                      }
                      break;
                    }
                  }
             }
       }
    }
   // c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFPreOrNextChannelList  tIFChannelInfoList>>>" + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }
  /**
   * get previous or next  channels with mask and start channel id
   *
   * @param startChannelId
   * @param isPrePage
   * @param count
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public List<TIFChannelInfo> getTIFPreOrNextDukChannelList(int startChannelId, boolean isPrePage,
      boolean containsStartChId, int count, int attentionMask, int expectValue,int sdtServiceType) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFPreOrNextDukChannelList>" + startChannelId + ">>"
        + isPrePage + ">>" + containsStartChId);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    if (count <= 0) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFPreOrNextDukChannelList  attentionMask: "+attentionMask+" expectValue: " +expectValue);
    long newId =(int) (startChannelId & 0xffffffffL);
    int beforeStartIdCount = 0;
    int afterStartIdCount = 0;
    boolean canAddData = false;
    if (startChannelId == -1) {
      canAddData = true;
    }
    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList isPrePage>>>" + isPrePage
        + "  mChannelList.size() = " + (mChannelList==null?mChannelList:mChannelList.size()));
    int currentChId= mCI.getCurrentChannelId();
    if (isPrePage) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList temTIFChannel isPrePage");
        TIFChannelInfo temTIFChannel = null;
        for(int i=mChannelList.size()-1 ; i>=0;i-- ){
          temTIFChannel = mChannelList.get(i);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1 getTIFPreOrNextDukChannelList temTIFChannel = "+temTIFChannel);
          if (canAddData) {

                    // only US EPG use hidden channel, other region not be used
                    if (!temTIFChannel.mIsBrowsable) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1 getTIFPreOrNextDukChannelList temTIFChannel.mIsBrowsable = false");
                        continue;
                    }

                    temTIFChannel.mMtkTvChannelInfo =  getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                    if (temTIFChannel.mMtkTvChannelInfo != null && ( temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != currentChId)) {
                      continue;
                    } else {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList temTIFChannel>>>" + temTIFChannel);
                    }
                    if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                            tIFChannelInfoList.add(0, temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextDukChannelList>");
                                break;
                            }
                    }
          } else {

            if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
              canAddData = true;
                if (containsStartChId) {
                      if (!temTIFChannel.mIsBrowsable) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "2 getTIFPreOrNextDukChannelList temTIFChannel.mIsBrowsable = false");
                          continue;
                      }
                      temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                      if (temTIFChannel.mMtkTvChannelInfo != null && ( temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != currentChId)) {
                            continue;
                          } else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel>>>" +temTIFChannel);
                          }
                          if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                                  tIFChannelInfoList.add(0, temTIFChannel);
                                  if (tIFChannelInfoList.size() == count) {
                                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFPreOrNextDukChannelList>");
                                      break;
                                  }

                          }

                  }else {
                      afterStartIdCount++;
                    }
              } else {
                afterStartIdCount++;
              }
            }
          }

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "preduktIFChannelInfoList>>>" + tIFChannelInfoList.size()
          + "   afterStartIdCount>>" + afterStartIdCount);
      if (tIFChannelInfoList.size() < count && afterStartIdCount > 0) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList first not get page channel, this is second");
          for(int i=mChannelList.size()-1 ; i>=0;i-- ){
              temTIFChannel = mChannelList.get(i);
                  if (!temTIFChannel.mIsBrowsable) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "3 getTIFPreOrNextDukChannelList temTIFChannel.mIsBrowsable = false");
                      continue;
                  }
                  temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                 if (temTIFChannel.mMtkTvChannelInfo != null && ( temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != currentChId)) {
                    continue;
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel >>>" + temTIFChannel);
                  }
                  if (temTIFChannel.mInternalProviderFlag3 != startChannelId) {
                    if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                            tIFChannelInfoList.add(0, temTIFChannel);
                            if (tIFChannelInfoList.size() == count) {
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextDukChannelList>");
                              break;
                            }

                    }
                  } else {
                    if (!containsStartChId) {
                      if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                              tIFChannelInfoList.add(0, temTIFChannel);
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFPreOrNextDukChannelList>");
                            }
                    }
                    break;
                  }
          }
      }
    } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList temTIFChannel nextpage");
      for(TIFChannelInfo temTIFChannel : mChannelList){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList  nextpage temTIFChannel is " + temTIFChannel);
        if (canAddData) {

                  if (!temTIFChannel.mIsBrowsable) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1 getTIFPreOrNextDukChannelList temTIFChannel.mIsBrowsable = false");
                      continue;
                  }

                  temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                  if (temTIFChannel.mMtkTvChannelInfo != null && ( temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != currentChId)) {
                    continue;
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel>>>" + temTIFChannel);
                  }
                  if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                          tIFChannelInfoList.add(temTIFChannel);
                          if (tIFChannelInfoList.size() == count) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextDukChannelList>");
                            break;
                          }
                  }
        } else {
          if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
            canAddData = true;
            if (containsStartChId) {

                   temTIFChannel.mMtkTvChannelInfo= getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                   if (temTIFChannel.mMtkTvChannelInfo != null && ( temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != currentChId)) {
                      continue;
                    } else {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel>>>" + temTIFChannel);
                    }
                    if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {

                        tIFChannelInfoList.add(temTIFChannel);
                        if (tIFChannelInfoList.size() == count) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextDukChannelList>");
                          break;
                        }

                    }
            } else {

              beforeStartIdCount++;
           }
          } else {

            beforeStartIdCount++;

          }
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList>>>" + tIFChannelInfoList.size()
          + "   beforeStartIdCount>>" + beforeStartIdCount);
      if (tIFChannelInfoList.size() < count && beforeStartIdCount > 0) {

         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextDukChannelList 2first not get page channel, this is second");
         for(TIFChannelInfo temTIFChannel:mChannelList){

                 if (!temTIFChannel.mIsBrowsable) {
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "7 getTIFPreOrNextDukChannelList temTIFChannel.mIsBrowsable = false");
                     continue;
                 }

                    temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                    if (temTIFChannel.mMtkTvChannelInfo != null && ( temTIFChannel.isSkip() && temTIFChannel.mInternalProviderFlag3 != currentChId)) {
                      continue;
                    } else {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temTIFChannel>>>" + temTIFChannel);
                    }
                    if (temTIFChannel.mInternalProviderFlag3 != startChannelId) {
                      if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                              tIFChannelInfoList.add(temTIFChannel);
                              if (tIFChannelInfoList.size() == count) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 7 getTIFPreOrNextDukChannelList>");
                                break;
                              }
                      }
                    } else {
                      if (!containsStartChId) {
                        if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                                tIFChannelInfoList.add(temTIFChannel);
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 8 getTIFPreOrNextDukChannelList>");
                        }
                      }
                      break;
                    }
             }
       }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFPreOrNextChannelList  tIFChannelInfoList>>>" + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  /**
   * get previous or next n satellite channels with mask and start channel id
   *
   * @param startChannelId
   * @param isPrePage
   * @param count
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public List<TIFChannelInfo> getTIFPreOrNextChannelListBySateRecId(int startChannelId,
      boolean isPrePage, boolean containsStartChId, int sateRecId, int count, int attentionMask,
      int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFPreOrNextChannelListBySateRecId>" + startChannelId + ">>"
        + isPrePage + ">>" + containsStartChId + ">>" + sateRecId);
    if (count <= 0) {
      return null;
    }
    long newId = (startChannelId & 0xffffffffL);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    TIFChannelInfo temTIFChannel = null;
    int beforeStartIdCount = 0;
    int afterStartIdCount = 0;
    boolean canAddData = false;
    if (startChannelId == -1) {
      canAddData = true;
    }
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID,
        getSvlIdSelectionArgs(), ORDERBY);
    if (c == null) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
        "getTIFPreOrNextChannelListBySateRecId isPrePage>>>" + isPrePage + "   c>>" + c.getCount());
    if (isPrePage) {
      if (c.moveToLast()) {
        do {
          temTIFChannel = TIFChannelInfo.parse(c);
          if(temTIFChannel ==null ){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextChannelListBySateRecId> temTIFChannel is null");
              continue;
          }
    //      parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
          if (canAddData) {
            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                temTIFChannel.mDataValue);
            if (tempApiChannel == null) {
              continue;
            }
            if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                if (tmpdvb.getSatRecId() == sateRecId) {
                  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                  tIFChannelInfoList.add(0, temTIFChannel);
                  if (tIFChannelInfoList.size() == count) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFPreOrNextChannelListBySateRecId>");
                    break;
                  }
                }
              }
            }
          } else {
            if (temTIFChannel.mDataValue != null && temTIFChannel.mDataValue[2] == newId) {
              canAddData = true;
              if (containsStartChId) {
                MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                    temTIFChannel.mDataValue);
                if (tempApiChannel == null) {
                  continue;
                }
                if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                  MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                  if (tmpdvb.getSatRecId() == sateRecId) {
                    temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                    tIFChannelInfoList.add(0, temTIFChannel);
                    if (tIFChannelInfoList.size() == count) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFPreOrNextChannelListBySateRecId>");
                      break;
                    }
                  }
                }
              } else {
                // if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                afterStartIdCount++;
                // }
              }
            } else {
              // if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              afterStartIdCount++;
              // }
            }
          }
        } while (c.moveToPrevious());
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pretIFChannelInfoList>>>" + tIFChannelInfoList.size()
          + "   afterStartIdCount>>" + afterStartIdCount
          + "    c.moveToLast()>>" + c.moveToLast());
      if (tIFChannelInfoList.size() < count && afterStartIdCount > 0) {
        if (c.moveToLast()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
              "getTIFPreOrNextChannelListBySateRecId first not get page channel, this is second");
          do {
            temTIFChannel = TIFChannelInfo.parse(c);
            if(temTIFChannel ==null ){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelListBySateRecId> temTIFChannel is null");
                continue;
            }
   //         parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                temTIFChannel.mDataValue);
            if (tempApiChannel == null) {
              continue;
            }
            if (tempApiChannel.getChannelId() != startChannelId) {
              if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                  MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                  if (tmpdvb.getSatRecId() == sateRecId) {
                    temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                    tIFChannelInfoList.add(0, temTIFChannel);
                    if (tIFChannelInfoList.size() == count) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFPreOrNextChannelListBySateRecId>");
                      break;
                    }
                  }
                }
              }
            } else {
              if (!containsStartChId) {
                if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                  MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                  if (tmpdvb.getSatRecId() == sateRecId) {
                    temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                    tIFChannelInfoList.add(0, temTIFChannel);
                  }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFPreOrNextChannelListBySateRecId>");
              }
              break;
            }
          } while (c.moveToPrevious());
        }
      }
    } else {
      while (c.moveToNext()) {
        temTIFChannel = TIFChannelInfo.parse(c);
        if(temTIFChannel ==null ){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelListBySateRecId> temTIFChannel is null");
            continue;
        }
    //    parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelListBySateRecId temTIFChannel" + temTIFChannel);
        if (canAddData) {
          MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
              temTIFChannel.mDataValue);
          if (tempApiChannel == null) {
            continue;
          }
          if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
            if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
              MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
              if (tmpdvb.getSatRecId() == sateRecId) {
                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                tIFChannelInfoList.add(temTIFChannel);
                if (tIFChannelInfoList.size() == count) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 5 getTIFPreOrNextChannelListBySateRecId>");
                  break;
                }
              }
            }
          }
        } else {
          if ((temTIFChannel.mDataValue != null && temTIFChannel.mDataValue.length > 2 && temTIFChannel.mDataValue[2] == newId)) {
            canAddData = true;
            if (containsStartChId) {
              MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                  temTIFChannel.mDataValue);
              if (tempApiChannel == null) {
                continue;
              }
              if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                if (tmpdvb.getSatRecId() == sateRecId) {
                  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                  tIFChannelInfoList.add(temTIFChannel);
                  if (tIFChannelInfoList.size() == count) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 6 getTIFPreOrNextChannelListBySateRecId>");
                    break;
                  }
                }
              }
            } else {
              // if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              beforeStartIdCount++;
              // }
            }
          } else {
            // if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
            beforeStartIdCount++;
            // }
          }
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nexttIFChannelInfoList>>>" + tIFChannelInfoList.size()
          + "   beforeStartIdCount>>" + beforeStartIdCount
          + "  c.moveToFirst()>>>" + c.moveToFirst());
      if (tIFChannelInfoList.size() < count && beforeStartIdCount > 0) {
        if (c.moveToFirst()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "2first not get page channel, this is second");
          do {
            temTIFChannel = TIFChannelInfo.parse(c);
            if(temTIFChannel ==null ){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 7 getTIFPreOrNextChannelListBySateRecId> temTIFChannel is null");
                continue;
            }
      //      parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                temTIFChannel.mDataValue);
            if (tempApiChannel == null) {
              continue;
            }
            if (tempApiChannel.getChannelId() != startChannelId) {
              if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                  MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                  if (tmpdvb.getSatRecId() == sateRecId) {
                    temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                    tIFChannelInfoList.add(temTIFChannel);
                    if (tIFChannelInfoList.size() == count) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 7 getTIFPreOrNextChannelListBySateRecId>");
                      break;
                    }
                  }
                }
              }
            } else {
              if (!containsStartChId) {
                if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                  MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                  if (tmpdvb.getSatRecId() == sateRecId) {
                    temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                    tIFChannelInfoList.add(temTIFChannel);
                    if (tIFChannelInfoList.size() == count) {
                      break;
                    }
                  }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 8 getTIFPreOrNextChannelListBySateRecId>");
              }
              break;
            }
          } while (c.moveToNext());
        }
      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFPreOrNextChannelListBySateRecId  tIFChannelInfoList>>>"
        + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  /**
   * get previous or next n satellite channel with mask and current channel id
   *
   * @param isPrePage
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public TIFChannelInfo getTIFUpOrDownChannelBySateRecId(boolean isPrePage, int sateRecId,
      int attentionMask, int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFUpOrDownChannelBySateRecId>>" + isPrePage);
    TIFChannelInfo temTIFChannel = null;
    int currentChannelId = TIFFunctionUtil.getCurrentChannelId();
    long newId = (currentChannelId & 0xffffffffL);
    boolean canAddData = false;
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID,
        getSvlIdSelectionArgs(), ORDERBY);
    if (c == null) {
      return temTIFChannel;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
        "getTIFUpOrDownChannelBySateRecId isPrePage>>>" + isPrePage + "   c>>" + c.getCount());
    if (isPrePage) {
      if (c.moveToLast()) {
        do {
          temTIFChannel = TIFChannelInfo.parse(c);
          if(temTIFChannel ==null ){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannelBySateRecId> temTIFChannel is null");
              continue;
          }
       //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
          if (canAddData) {
            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                temTIFChannel.mDataValue);
            if (tempApiChannel == null) {
              continue;
            }
            if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                if (tmpdvb.getSatRecId() == sateRecId) {
                  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                  c.close();
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannelBySateRecId>>");
                  return temTIFChannel;
                }
              }
            }
          } else {
            if ((temTIFChannel.mDataValue != null && temTIFChannel.mDataValue[2] == newId)) {
              canAddData = true;
            }
          }
        } while (c.moveToPrevious());
      }
      if (c.moveToLast()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog
            .d(TAG, "getTIFUpOrDownChannelBySateRecId first not get page channel, this is second");
        do {
          temTIFChannel = TIFChannelInfo.parse(c);
          if(temTIFChannel ==null ){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannelBySateRecId> temTIFChannel is null");
              continue;
          }
     //     parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
          MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
              temTIFChannel.mDataValue);
          if (tempApiChannel == null) {
            continue;
          }
          if (tempApiChannel.getChannelId() != currentChannelId) {
            if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                if (tmpdvb.getSatRecId() == sateRecId) {
                  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                  c.close();
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannelBySateRecId>>");
                  return temTIFChannel;
                }
              }
            }
          } else {
            break;
          }
        } while (c.moveToPrevious());
      }
    } else {
      while (c.moveToNext()) {
        temTIFChannel = TIFChannelInfo.parse(c);
        if(temTIFChannel ==null ){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelBySateRecId> temTIFChannel is null");
            continue;
        }
    //    parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelBySateRecId> temTIFChannel>>" + temTIFChannel);
        if (canAddData) {
          MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
              temTIFChannel.mDataValue);
          if (tempApiChannel == null) {
            continue;
          }
          if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
            if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
              MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
              if (tmpdvb.getSatRecId() == sateRecId) {
                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                c.close();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelBySateRecId>>");
                return temTIFChannel;
              }
            }
          }
        } else {
          if (temTIFChannel.mDataValue != null && temTIFChannel.mDataValue.length > 2 && temTIFChannel.mDataValue[2] == newId) {
            canAddData = true;
          }
        }
      }
      if (c.moveToFirst()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
            "getTIFUpOrDownChannelBySateRecId 2first not get page channel, this is second");
        do {
          temTIFChannel = TIFChannelInfo.parse(c);
          if(temTIFChannel ==null ){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannelBySateRecId> temTIFChannel is null");
              continue;
          }
  //        parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
          MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
              temTIFChannel.mDataValue);
          if (tempApiChannel == null) {
            continue;
          }
          if (tempApiChannel.getChannelId() != currentChannelId) {
            if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
                MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
                if (tmpdvb.getSatRecId() == sateRecId) {
                  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                  c.close();
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannelBySateRecId>>");
                  return temTIFChannel;
                }
              }
            }
          } else {
            break;
          }
        } while (c.moveToNext());
      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFUpOrDownChannelBySateRecId  >>>null");
    return null;
  }

  /**
   * get previous or next channel for 3rdsource
   *
   * @param isPreChannel
   * @return
   */

  public TIFChannelInfo  getTIFUpOrDownChannelfor3rdsource(boolean isup){
      TIFChannelInfo selchannel = getTIFUpOrDownChannel(isup, CommonIntegration.CH_LIST_3RDCAHNNEL_MASK , CommonIntegration.CH_LIST_3RDCAHNNEL_VAL);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannelfor3rdsource step 0 .isup ="+isup + ",selchannel: "+selchannel);
      return selchannel;
  }

  public TIFChannelInfo getTIFUpOrDownChannel(boolean isPreChannel, int attentionMask,
          int expectValue) {
      return getTIFUpOrDownChannel(isPreChannel,attentionMask,expectValue,getChannelListForFindOrNomal());
  }

  public TIFChannelInfo getTIFUpOrDownChannelForUSEPG(boolean isPreChannel, int attentionMask,
          int expectValue) {
      return getTIFUpOrDownChannelForUSEPG(isPreChannel,attentionMask,expectValue,
          CommonIntegration.getInstance().getChannelListForEPG());//getChannelListForFindOrNomal()
  }

  private TIFChannelInfo getTIFUpOrDownChannel(boolean isPreChannel, int attentionMask,
          int expectValue, List<TIFChannelInfo> mChannelList) {
      return getTIFUpOrDownChannel(isPreChannel,attentionMask,expectValue,mChannelList,false);
  }

  /**
   * get previous or next channel
   *
   * @param isPreChannel
   * @param attentionMask
   * @param expectValue
   * @param showSkip :can show skip channel
   * @return
   */
  private TIFChannelInfo getTIFUpOrDownChannel(boolean isPreChannel, int attentionMask,
      int expectValue, List<TIFChannelInfo> mChannelList,boolean showSkip) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFUpOrDownChannel>>" + isPreChannel);
   /* String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }*/
   // Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, selection,
   //     selectionargs, ORDERBY);
  //  if (c == null) {
  //    return null;
 //   }
    int currentChannelId = TIFFunctionUtil.getCurrentChannelId();
    long newId = (int)(currentChannelId & 0xffffffffL);
    TIFChannelInfo currentChannelInfo = getTIFChannelInfoById(currentChannelId);
    TIFChannelInfo current3rdChannelInfo= getChannelInfoByUri();
    if(current3rdChannelInfo != null && current3rdChannelInfo.mMtkTvChannelInfo ==null){
        currentChannelId = (int)current3rdChannelInfo.mId;
        newId = current3rdChannelInfo.mId;
    }
    boolean canGetData = false;
    // this option for skip SA minorNo channel.
    final boolean isSAsKIPOption = false;
    //if (CommonIntegration.isSARegion()
    //    && mCI.getProperty(CommonIntegration.WW_SKIP_MINOR) == 1) {
    //  isSAsKIPOption = true;
    //}
//    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannel isPrePage>>>" + isPreChannel  + "  mChannelList.size() = " + (mChannelList==null?mChannelList:mChannelList.size()));
    if (isPreChannel) {
        TIFChannelInfo temTIFChannel = null;
        if (mChannelList != null) {
            for (int i = mChannelList.size() - 1; i >= 0; i--) {
                temTIFChannel = mChannelList.get(i);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannel temTIFChannel>>> " + temTIFChannel);
                //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
                if (canGetData) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannel isPreChannel canGetData>>> " + canGetData);
                    if (attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannel> 3rd");
                        if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable) {
                            return temTIFChannel;
                        }
                    }
                    if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {// only US EPG use
                        // hidden channel,
                        // other region not
                        // be used
                        continue;
                    }
                    if (mCI.isDisableColorKey()) {
                        if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannel isDisableColorKey isPreChannel 3rd channel temTIFChannel: " + temTIFChannel);
                            return temTIFChannel;
                        }
                    }
                    MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                    if (tempApiChannel == null || (!showSkip && tempApiChannel.isSkip())) {
                        continue;
                    }
                    if (CommonIntegration.isSARegion() && isSAsKIPOption) {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)
                                && tempApiChannel instanceof MtkTvISDBChannelInfo
                                && currentChannelInfo.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo) {
                            if (((MtkTvISDBChannelInfo) tempApiChannel).getMajorNum()
                                    != ((MtkTvISDBChannelInfo) currentChannelInfo.mMtkTvChannelInfo)
                                    .getMajorNum()) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end sa 1 getTIFUpOrDownChannel>>");
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                return temTIFChannel;
                            }
                        }
                    } else {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                            if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    }
                } else {
                    if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
                        canGetData = true;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannel> PreChannel canGetData = true");
                    }
                }
            }
            for (int i = mChannelList.size() - 1; i >= 0; i--) {

                temTIFChannel = mChannelList.get(i);
                if (attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannel> 3rd");
                    if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mId != newId && temTIFChannel.mIsBrowsable) {
                        return temTIFChannel;
                    } else {
                        continue;
                    }
                }
                if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {// only US EPG use
                    // hidden channel,
                    // other region not be
                    // used
                    continue;
                }
                if (mCI.isDisableColorKey()) {
                    if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannel isDisableColorKey isPreChannel 3rd channel temTIFChannel: " + temTIFChannel);
                        return temTIFChannel;
                    }
                }
                //     parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
                MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                if (tempApiChannel == null || (!showSkip && tempApiChannel.isSkip())) {
                    continue;
                }
                if (tempApiChannel.getChannelId() != currentChannelId) {
                    if (CommonIntegration.isSARegion() && isSAsKIPOption) {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)
                                && tempApiChannel instanceof MtkTvISDBChannelInfo
                                && currentChannelInfo.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo) {
                            if (((MtkTvISDBChannelInfo) tempApiChannel).getMajorNum()
                                    != ((MtkTvISDBChannelInfo) currentChannelInfo.mMtkTvChannelInfo)
                                    .getMajorNum()) {
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end sa 2 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    } else {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                            if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())) {
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }
    } else {

        if (mChannelList != null) {
            for (TIFChannelInfo temTIFChannel : mChannelList) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannel> nextChannel temTIFChannel = " + temTIFChannel);
                if (canGetData) {
                    if (attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannel> 3rd");
                        if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable) {
                            return temTIFChannel;
                        }
                    }
                    if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {// only US EPG use
                        // hidden channel,
                        // other region not be
                        // used
                        continue;
                    }
                    if (mCI.isDisableColorKey()) {
                        if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannel isDisableColorKey nextChannel 3rd channel temTIFChannel: " + temTIFChannel);
                            return temTIFChannel;
                        }
                    }
                    MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                    if (tempApiChannel == null || (!showSkip && tempApiChannel.isSkip())) {
                        continue;
                    }
                    if (CommonIntegration.isSARegion() && isSAsKIPOption) {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)
                                && tempApiChannel instanceof MtkTvISDBChannelInfo
                                && currentChannelInfo.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo) {
                            if (((MtkTvISDBChannelInfo) tempApiChannel).getMajorNum()
                                    != ((MtkTvISDBChannelInfo) currentChannelInfo.mMtkTvChannelInfo)
                                    .getMajorNum()) {
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end sa 3 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    } else {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {

                            if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())) {
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    }
                } else {
                    if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
                        canGetData = true;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannel> nextChannel canGetData = true");
                    }
                }
            }

            for (TIFChannelInfo temTIFChannel : mChannelList) {
                //   temTIFChannel = TIFChannelInfo.parse(c);
                if (attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannel> 3rd");
                    if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mId != newId && temTIFChannel.mIsBrowsable) {
                        return temTIFChannel;
                    }
                }
                if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {// only US EPG use
                    // hidden channel,
                    // other region not be
                    // used
                    continue;
                }
                if (mCI.isDisableColorKey()) {
                    if ((temTIFChannel.mDataValue == null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannel isDisableColorKey nextChannel 3rd channel temTIFChannel: " + temTIFChannel);
                        return temTIFChannel;
                    }
                }
                //  parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
                MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                if (tempApiChannel == null || (!showSkip && tempApiChannel.isSkip())) {
                    continue;
                }
                if (tempApiChannel.getChannelId() != currentChannelId) {
                    if (CommonIntegration.isSARegion() && isSAsKIPOption) {
                        if (tempApiChannel != null
                                && TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)
                                && tempApiChannel instanceof MtkTvISDBChannelInfo
                                && currentChannelInfo.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo) {
                            if (((MtkTvISDBChannelInfo) tempApiChannel).getMajorNum()
                                    != ((MtkTvISDBChannelInfo) currentChannelInfo.mMtkTvChannelInfo)
                                    .getMajorNum()) {
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end sa 4 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    } else {
                        if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                            if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())) {
                                temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannel>>");
                                return temTIFChannel;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }
  //  c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFUpOrDownChannel>> null");
    return null;
  }

  /**
   * For US, get previous or next one DTV channel(Skip ATV)
   *
   * @param isPreChannel
   * @param attentionMask
   * @param expectValue
   * @param showSkip :can show skip channel
   * @return
   */
  private TIFChannelInfo getTIFUpOrDownChannelForUSEPG(boolean isPreChannel, int attentionMask,
      int expectValue, List<TIFChannelInfo> mChannelList) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFUpOrDownChannelForUSEPG>>" + isPreChannel);

    int currentChannelId = TIFFunctionUtil.getCurrentChannelId();
    long newId = (int)(currentChannelId & 0xffffffffL);
    //TIFChannelInfo currentChannelInfo = getTIFChannelInfoById(currentChannelId);
    TIFChannelInfo current3rdChannelInfo= getChannelInfoByUri();
    if(current3rdChannelInfo != null && current3rdChannelInfo.mMtkTvChannelInfo ==null){
        currentChannelId = (int)current3rdChannelInfo.mId;
        newId = current3rdChannelInfo.mId;
    }
    boolean canGetData = false;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannel isPrePage>>>" + isPreChannel  + "  mChannelList.size() = " + (mChannelList==null?mChannelList:mChannelList.size()));
    if(mChannelList == null){
        return null;
    }
    if (isPreChannel) {
        TIFChannelInfo temTIFChannel = null;
        for(int i=mChannelList.size()-1 ; i>=0;i-- ){
          temTIFChannel = mChannelList.get(i);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannelForUSEPG temTIFChannel>>> "+ temTIFChannel);
          if (canGetData) {
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownChannelForUSEPG isPreChannel canGetData>>> "+ canGetData);
               if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannelForUSEPG > 3rd");
                   if((temTIFChannel.mDataValue ==null || TIFFunctionUtil.is3rdTVSource(temTIFChannel)) && temTIFChannel.mIsBrowsable){
                      return  temTIFChannel;
                   }
               }
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "00preChannel getTIFUpOrDownChannelForUSEPG  temTIFChannel:"+temTIFChannel);
                MtkTvChannelInfoBase tempApiChannel =  getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                if (tempApiChannel != null
                        && TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                  if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownChannelForUSEPG>>");
                    return temTIFChannel;
                  }
                }
          } else {
            if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId== newId )) {
              canGetData = true;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannelForUSEPG> PreChannel canGetData = true");
            }
          }
        }

        for(int i=mChannelList.size()-1 ; i>=0;i-- ){
          temTIFChannel = mChannelList.get(i);
          if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannelForUSEPG> 3rd");
              if((temTIFChannel.mDataValue ==null ||
                  TIFFunctionUtil.is3rdTVSource(temTIFChannel))
                  && temTIFChannel.mId != newId  && temTIFChannel.mIsBrowsable){
                  return  temTIFChannel;
              }else{
                  continue;
              }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "preChannel getTIFUpOrDownChannelForUSEPG  temTIFChannel:"+temTIFChannel);
          MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
          if (tempApiChannel != null && tempApiChannel.getChannelId() != currentChannelId) {
            if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
              if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                temTIFChannel.mMtkTvChannelInfo =tempApiChannel;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownChannelForUSEPG>>");
                return temTIFChannel;
              }
            }
          } else {
            break;
          }
        }
    } else {
        if (mChannelList != null) {
            for (TIFChannelInfo temTIFChannel : mChannelList) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelForUSEPG> nextChannel temTIFChannel = " + temTIFChannel);
                if (canGetData) {
                    if (attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelForUSEPG> 3rd");
                        if ((temTIFChannel.mDataValue == null ||
                                TIFFunctionUtil.is3rdTVSource(temTIFChannel))
                                && temTIFChannel.mIsBrowsable) {
                            return temTIFChannel;
                        }
                    }
                    MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                    if (tempApiChannel != null
                            && TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {

                        if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())) {
                            temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelForUSEPG>>");
                            return temTIFChannel;
                        }
                    }
                } else {
                    if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId == newId)) {
                        canGetData = true;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownChannelForUSEPG> nextChannel canGetData = true");
                    }
                }
            }

            for (TIFChannelInfo temTIFChannel : mChannelList) {
                if (attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannelForUSEPG> 3rd");
                    if ((temTIFChannel.mDataValue == null
                            || TIFFunctionUtil.is3rdTVSource(temTIFChannel))
                            && temTIFChannel.mId != newId && temTIFChannel.mIsBrowsable) {
                        return temTIFChannel;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "11getTIFUpOrDownChannelForUSEPG  temTIFChannel:" + temTIFChannel);
                MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                if (tempApiChannel != null && tempApiChannel.getChannelId() != currentChannelId) {
                    if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
                        if (TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())) {
                            temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownChannelForUSEPG>>done");
                            return temTIFChannel;
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFUpOrDownChannelForUSEPG for us epg>> null");
    return null;
  }

  // /**
  // * return by list
  // * @return
  // */
  // public List<TIFChannelInfo> getTIFUpOrDownChannelByList(boolean isPre) {
  // int currentChannelId = TIFFunctionUtil.getCurrentChannelId();
  // return getTIFPreOrNextChannelList(currentChannelId, isPre, false, 1,
  // TIFFunctionUtil.CH_UP_DOWN_MASK, TIFFunctionUtil.CH_UP_DOWN_VAL);
  // }

  /**
   * get TIF channel by channel id
   *
   * @param channelId
   * @return
   */
  public TIFChannelInfo getTIFChannelInfoById(int channelId) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFChannelInfoById>>" + channelId);

      List<TIFChannelInfo> mChannelList =new ArrayList<TIFChannelInfo>();
      mChannelList.addAll(getChannelListForFindOrNomal());
//      mChannelList.addAll(getFakeChannelList()); // add fake channel by channelid
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfoById mChannelList.size>>" + mChannelList.size());
     for (TIFChannelInfo temTIFChannel : mChannelList) {
            if (temTIFChannel != null &&(temTIFChannel.mInternalProviderFlag3 == channelId|| temTIFChannel.mId == channelId)) {
                MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
              temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoById>>" + temTIFChannel.mDisplayName);
              return temTIFChannel;
            }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoById>>null");
      return null;

}


    public TIFChannelInfo getChannelInfoByChannelIdAndSvlId(long channelid,long svlid){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getChannelInfoByChannelIdAndSvlId>>" + channelid+",svlid="+svlid);
        List<TIFChannelInfo> list = getChannelList();
        for(TIFChannelInfo item : list){
            if (item != null && item.mDataValue != null && item.mDataValue[2] == channelid && item.mDataValue[0] == svlid )
            {
                return item;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"end null");
        return null;
    }


  /**
   * select channel for tune mode
   *
   * @param
   * @return
   */
  public void selectTIFChannelInfoByChannelIdForTuneMode() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start selectTIFChannelInfoByChannelIdForTuneMode>>");
      //CommonIntegration.getInstance().getCurChInfo();
      List<TIFChannelInfo> mChannelList =new ArrayList<TIFChannelInfo>();
      mChannelList.addAll(getChannelListForFindOrNomal());
      int channelId= CommonIntegration.getInstance().getChannelIdByConfigId();

//      mChannelList.addAll(getFakeChannelList()); // add fake channel by channelid
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectTIFChannelInfoByChannelIdForTuneMode mChannelList.size>>" + mChannelList.size());
      TIFChannelInfo tiFChannel=null;
     for (TIFChannelInfo temTIFChannel : mChannelList) {
            if (temTIFChannel != null &&(temTIFChannel.mInternalProviderFlag3 == channelId)) {
                tiFChannel= temTIFChannel;
              break;
            }
      }
     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end selectTIFChannelInfoByChannelIdForTuneMode>>" + tiFChannel);
     selectChannelByTIFInfo(tiFChannel);
}

  /**
   * get TIF channel by channel id
   *
   * @param channelId
   * @return
   */
  public TIFChannelInfo getTIFChannelInfoForSourceById(int channelId) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFChannelInfoById>>" + channelId);

      List<TIFChannelInfo> mChannelList =new ArrayList<TIFChannelInfo>();
      mChannelList.addAll(getChannelList());
      int svlId= CommonIntegration.getInstance().getSvl();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfoById mChannelList.size>>" + mChannelList.size());
     for (TIFChannelInfo temTIFChannel : mChannelList) {
//            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
         if(mCI.isCNRegion() || mCI.isTVSourceSeparation()){
             if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == channelId || temTIFChannel.mId == channelId)) {
           //      parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
                 temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
                 if(temTIFChannel != null){
                     if(mCI.isCurrentSourceATV()){
                         if(!TIFFunctionUtil.checkChMask(temTIFChannel, TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_DIGITAL_VAL)
                                 && temTIFChannel.mInternalProviderFlag1 == svlId){
                             return temTIFChannel;
                         }
                     }else if(mCI.isCurrentSourceDTV()){
                         if(TIFFunctionUtil.checkChMask(temTIFChannel, TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_DIGITAL_VAL)){
                             return temTIFChannel;
                         }
                     }else {
                         if(mCI.getBrdcstType() == mCI.BRDCST_TYPE_DTV){
                             if(!TIFFunctionUtil.checkChMask(temTIFChannel, TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_DIGITAL_VAL)
                                     && temTIFChannel.mInternalProviderFlag1 == svlId){
                                 return temTIFChannel;
                             }
                         }else if(mCI.getBrdcstType() == mCI.BRDCST_TYPE_ATV){
                             if(TIFFunctionUtil.checkChMask(temTIFChannel, TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_DIGITAL_VAL)){
                                 return temTIFChannel;
                             }
                         }
                     }
                 }
                }
         }else {
                 if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == channelId || temTIFChannel.mId == channelId) &&
                         temTIFChannel.mInternalProviderFlag1 == svlId) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoById>>" + temTIFChannel.mDisplayName);
                      return temTIFChannel;
                    }

         }

      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoById>>null");
      return null;

}

    /**
     * judge if the current channel is CI virtual channel
     */
    public boolean isCiVirtualChannel() {
        TIFChannelInfo tifInfo =
            getTIFChannelInfoById(mCI.getCurrentChannelId());

        if (tifInfo != null) {
            long[] datas = tifInfo.mDataValue;
            if (datas != null && datas.length > 5) {
                return (datas[5] == MtkTvChCommonBase.SVL_SERVICE_TYPE_CI14_VIRTUAL_CH);
            }
        }
        return false;
    }

    /**
     * get TIF channel by provider channel id
     *
     * @param channelId
     * @return
     */
  public TIFChannelInfo getTIFChannelInfoByProviderId(long providerChannelId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFChannelInfoByProviderId>>" + providerChannelId);
    Cursor c = mContentResolver.query(buildChannelUri(providerChannelId), null,
                null, null, ORDERBY);
    if (c == null) {
      return null;
    }
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
   //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (tempApiChannel != null) {
        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
        c.close();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoByProviderId>>" + temTIFChannel.mDisplayName);
        return temTIFChannel;
      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoByProviderId>>null");
    return null;
  }

// test data
  public List<TIFChannelInfo> getTIFChannelInfofro3rd() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfofro3rd:");
      Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null,
          null, null, null);
      if (c == null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfoBySource, c");
        return null;
      }

      List<TIFChannelInfo> list = new ArrayList<TIFChannelInfo>();
      TIFChannelInfo temTIFChannel = null;

      while (c.moveToNext()) {
        temTIFChannel = TIFChannelInfo.parse(c);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfofro3rd, temTIFChannel s" + temTIFChannel);
       // parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
       /* MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
        if (tempApiChannel != null) {
          temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
        }*/
        list.add(temTIFChannel);
      }
      c.close();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfofro3rd, " + list.size());
      return list;
    }


  /**
   * get TIF channel by provider channel id
   *
   * @param channelId
   * @return
   */
  public List<TIFChannelInfo> getTIFChannelInfoBySource(String sourceId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfoBySource:" + sourceId);

    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    List<TIFChannelInfo> list = new ArrayList<TIFChannelInfo>();

    for(TIFChannelInfo temTIFChannel:mChannelList){
        if(temTIFChannel.mInputServiceName.equals(sourceId)){
       //     parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
            MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
            if (tempApiChannel != null && TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL)) {
              temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
            }
            list.add(temTIFChannel);
        }

    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFChannelInfoBySource, " + list.size());
    return list;
  }

  /**
   * get TIF channel by provider channel id
   *
   * @param channelId
   * @return
   */
  public TIFChannelInfo getTIFChannelInfoPLusByProviderId(long providerChannelId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFChannelInfoByProviderId>>" + providerChannelId);
    Cursor c = mContentResolver.query(buildChannelUri(providerChannelId), null,
        null, null, ORDERBY);
    if (c == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoByProviderId>>" + c);
        return null;

    }
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
    //  parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
    }

    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFChannelInfoByProviderId>>temTIFChannel :"+temTIFChannel);
    return temTIFChannel;
  }

  /**
   * get API channel by channel id
   *
   * @param channelId
   * @return
   */
  public MtkTvChannelInfoBase getAPIChannelInfoById(int channelId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getAPIChannelInfoById>>" + channelId);
   /* Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null,
        SELECTION_WITH_SVLID_CHANNELID, getSvlIdAndChannelIdSelectionArgs(channelId), ORDERBY);
    if (c == null) {
      return null;
    }*/
    List<TIFChannelInfo> mChannelList =new ArrayList<TIFChannelInfo>();
    mChannelList.addAll(getChannelListForFindOrNomal());
//    mChannelList.addAll(getFakeChannelList()); // add fake channel by channelid
    for (TIFChannelInfo temTIFChannel:mChannelList) {
     // temTIFChannel = TIFChannelInfo.parse(c);
    //  parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      if (temTIFChannel != null && temTIFChannel.mInternalProviderFlag3 == channelId) {
          MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
          temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getAPIChannelInfoById>>" + temTIFChannel.mDisplayName);
          return tempApiChannel;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getAPIChannelInfoById>>null");
    return null;
  }

  public int getCurrentSvlChannelListLength(int attentionMask, int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getCurrentSvlChannelListLength  >>>");
    int count = 0;
    String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, selection,
        selectionargs, ORDERBY);
    if (c == null) {
      return count;
    }
    TIFChannelInfo temTIFChannel = null;
    MtkTvChannelInfoBase tempApiChannel = null;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSvlChannelListLength c.moveToNext()>>> c  " + c.getCount());
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
      if (!temTIFChannel.mIsBrowsable) {// hidden channel filter
        continue;
      }
    //  parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
        count++;
      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getCurrentSvlChannelListLength  >>>" + count);
    return count;
  }

  /**
   * get attention mask channel list size
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public int getChannelListConfirmLength(int count, int attentionMask, int expectValue) {
    int length = queryChanelListWithMaskCount(count, attentionMask, expectValue).size();
    // add for check current Channel if Hidden channel, channellistDialog need show Hidden channel
    // if it's current play channel.
    // for US
    if (CommonIntegration.isUSRegion()) {
      if (TIFFunctionUtil.checkChMask(getAPIChannelInfoByChannelId(TIFFunctionUtil.getCurrentChannelId()),
          TIFFunctionUtil.CH_LIST_MASK, 0)) {
        length++;
      }
    }
    return length;
  }

  /**
   * get attention mask channel list size
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */
  public int getDukChannelListConfirmLength(int count, int attentionMask, int expectValue,int sdtServiceType) {
    int length = queryDukChanelListWithMaskCount(count, attentionMask, expectValue,sdtServiceType,false).size();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getDukChannelListConfirmLength ~ length" + length);
    return length;
  }
  /**
   * get channels with mask and count for rus or turnkey
   *
   * @param attentionMask
   * @param expectValue
   * @return
   */

  public int getChannelListConfirmLengthForSecondType(int count, int attentionMask,
      int expectValue) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getChannelListConfirmLengthForSecondType~" + count+" attentionMask: "+attentionMask+" expectValue: " +expectValue);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    if (count <= 0) {
      return 0;
    }

    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    if (mChannelList == null || mChannelList.isEmpty()) {
      return 0;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListConfirmLengthForSecondType  mChannelList.size "+mChannelList.size());
     for( TIFChannelInfo temTIFChannel : mChannelList){
      // only US EPG use hidden channel, other region not be used
      if (!(CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) && !temTIFChannel.mIsBrowsable) {
        continue;
      }
      if((attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && expectValue == CommonIntegration.CH_LIST_3RDCAHNNEL_VAL)){ // deal 3rd channels
          if((temTIFChannel.mDataValue ==null ||  (TIFFunctionUtil.is3rdTVSource(temTIFChannel) && temTIFChannel.mDataValue.length != TIFFunctionUtil.channelDataValuelength))  && temTIFChannel.mIsBrowsable){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListConfirmLengthForSecondType  3rd channel temTIFChannel: "+temTIFChannel);
              tIFChannelInfoList.add(temTIFChannel);
              if (tIFChannelInfoList.size() == count) {
                break;
              }
          }
      }else{
          if(mCI.isDisableColorKey()){
              if((temTIFChannel.mDataValue ==null ||  TIFFunctionUtil.is3rdTVSource(temTIFChannel))  && temTIFChannel.mIsBrowsable){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListConfirmLengthForSecondType isDisableColorKey 3rd channel temTIFChannel: "+temTIFChannel);
                  tIFChannelInfoList.add(temTIFChannel);
                  if (tIFChannelInfoList.size() == count) {
                    break;
                  }
                  continue;
              }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListConfirmLengthForSecondType  not 3rd channel temTIFChannel: "+temTIFChannel);
          temTIFChannel.mMtkTvChannelInfo =getAPIChannelInfoByChannelId( temTIFChannel.mInternalProviderFlag3);
          if(temTIFChannel.mMtkTvChannelInfo == null || ( temTIFChannel.mMtkTvChannelInfo.isSkip() && temTIFChannel.mMtkTvChannelInfo.getChannelId() != mCI.getCurrentChannelId())){
              continue;
          }
          if (TIFFunctionUtil.checkChMask(temTIFChannel.mMtkTvChannelInfo, attentionMask, expectValue)) {  //
              if (TIFFunctionUtil.checkChCategoryMask(temTIFChannel.mMtkTvChannelInfo, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
              //  temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
                  tIFChannelInfoList.add(temTIFChannel);
                  if (tIFChannelInfoList.size() == count) {
                    break;
                  }
                }

           }else if(TIFFunctionUtil.checkChMask(temTIFChannel.mMtkTvChannelInfo, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
               tIFChannelInfoList.add(temTIFChannel);
               if (tIFChannelInfoList.size() == count) {
                 break;
               }
           }
      }

    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getChannelListConfirmLengthForSecondType  tIFChannelInfoList>>>"
        + tIFChannelInfoList.size());
    return tIFChannelInfoList.size();
  }


  public long[] parserTIFChannelData(TIFChannelInfo tIFChannelInfo, String data) {
    long v[] = new long[6];
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "data:" + data);
    if (data == null) {
      return v;
    }
    String value[] = data.split(",");
    if (value.length < 5) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parserTIFChannelData data.length <6" );
      return v;
    }
    long mSvlId = Long.parseLong(value[1]);
    long mSvlRecId = Long.parseLong(value[2]);
    long channelId = Long.parseLong(value[3]);
    v[0] = mSvlId;
    v[1] = mSvlRecId;
    v[2] = channelId;
    // v[3] = mHashcode;
    v[4] = (mSvlId << 16) + mSvlRecId;
    if (value.length == 6) {
      long mServiceType = Long.parseLong(value[5]);
      v[5] = mServiceType;
    }
    tIFChannelInfo.mDataValue = v;
    return v;
  }

  private synchronized MtkTvChannelInfoBase getAPIChannelInfoByBlobData(long mDataValue[]) {
      if(mDataValue != null && mDataValue.length >3){
          if (mGetApiChannelFromSvlRecd) {
              try {
                return MtkTvChannelList.getInstance().getChannelInfoBySvlRecId((int) mDataValue[0],
                    (int) mDataValue[1]);
              } catch (Exception e) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelById " +e.getMessage() );
                return null;
              }
            } else {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelById chID = " + mDataValue[2]);
              MtkTvChannelInfoBase chInfo = null;
              chInfo = mCI.getChannelById((int) mDataValue[2]);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelById chInfo = " + chInfo);
              return chInfo;
            }
      }
      return null;
  }

  public  MtkTvChannelInfoBase getAPIChannelInfoByChannelId(int channelId) {
      if(maps !=null && !maps.isEmpty()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAPIChannelInfoByChannelI maps.size()"+maps.size());
         return  maps.get(channelId);
      }else if(!getAllChannelsrunning){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAPIChannelInfoByChannelI maps.size() null");

          TVAsyncExecutor.getInstance().execute(new Runnable() {
              @Override
                public void run() {
                   maps =new HashMap<>();
                   getAllChannels();
                }
            });
      }else{
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllchannels is running,go on updata");
          upDateAllChannelsrunning=true;
      }
      return null;
  }
  public void updateMapsChannelInfoByList(List<MtkTvChannelInfoBase> list) {
      if(maps !=null){
          for(MtkTvChannelInfoBase channelInfo:list){
              maps.put(channelInfo.getChannelId() ,channelInfo);
          }
      }

  }
  private String[] getSvlIdSelectionArgs() {
    int svlId = mCI.getSvl();
    String path = mCI.getCurrentFocus();
    String inputId = InputSourceManager.getInstance().getTvInputId(path);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSvlIdSelectionArgs>>>" + inputId + ">>"
        + InputSourceManager.getInstance().isCurrentTvSource(path));
    return new String[] {
        String.format(Locale.UK,"%05d", svlId)
    };
  }

  private String[] getSvlIdAndInputIdSelectionArgs() {
    int svlId = mCI.getSvl();

    String[] selectionArgs = {
        String.format(Locale.UK,"%05d", svlId)
    };
    if (mCI.isCNRegion()  || mCI.isTVSourceSeparation()) {
      // atv source
      if (mCI.isCurrentSourceATV()) {
        selectionArgs = new String[] {
            String.format(Locale.UK,"%05d", svlId), "HW1"
        };
      } else if (mCI.isCurrentSourceDTV()) {
        selectionArgs = new String[] {
            String.format(Locale.UK,"%05d", svlId), "HW0"
        };
      }
    }
    return selectionArgs;
  }

  private String[] getSvlIdAndChannelIdSelectionArgs(int channelId) {
    long newId = (channelId & 0xffffffffL);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelId>>>" + channelId + ">>" + newId);
    int svlId = mCI.getSvl();
    String path = mCI.getCurrentFocus();
    String inputId = InputSourceManager.getInstance().getTvInputId(path);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSvlIdAndChannelIdSelectionArgs>>>" + inputId + ">>"
        + InputSourceManager.getInstance().isCurrentTvSource(path));
    return new String[] {
        String.format(Locale.UK,"%05d", svlId), String.format(Locale.UK,"%010d", newId)
    };
   
  }

  public boolean channelPre() {
    // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre mPreTIFChannelInfo =" + mPreTIFChannelInfo);
    if (mCI.isCurrentSourceTv()
        && mCI.isMenuInputTvBlock()) {
      return false;
    }
    // PIP/POP
    if (mCI.isPipOrPopState()) {
      // current input is conflict with TV source, pre_channel fail
      List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
      String sourceName = InputSourceManager.getInstance()
          .getCurrentInputSourceName(mCI.getCurrentFocus());
      if (!mCI.isCurrentSourceTv()
          || (sourceList != null && sourceList.contains(sourceName))) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict");
        return false;
      }
    }
    if (mCI.isCurrentSourceTv()) {
      TIFChannelInfo tIFChannelInfo = getTIFChannelInfoById(mCI
          .getLastChannelId());
      int lastId = mCI.getLastChannelId();
      int currentId = mCI.getCurrentChannelId();
      if (tIFChannelInfo == null || lastId == currentId) {
        return false;
      }
      return selectChannelByTIFInfo(tIFChannelInfo);
    } else {// non TV source, just jump to TV source, not change channel
      mCI.iSetSourcetoTv();
    }
    return false;
  }

  public TIFChannelInfo getTifChannelInfoByUri(Uri channelUri) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTifChannelInfoByUri  channelUri>>>" + channelUri);
    if (channelUri == null) {
        return null;
      }
        Cursor c = mContentResolver.query(channelUri, null, null,
                null, ORDERBY);
    if (c == null) {
      return null;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTifChannelInfoByUri c.moveToNext()>>> c  " + c.getCount());
    TIFChannelInfo temTIFChannel=null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " getTifChannelInfoByUri temTIFChannel >>>" + temTIFChannel);
      temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTifChannelInfoByUri  >>>" + temTIFChannel);
    return temTIFChannel;
  }


  public boolean selectChannelByTIFInfo(TIFChannelInfo tifChannel) {
      return doSelectChannelByTIFInfo(tifChannel);
  }

  /**
   * select channel with TIF tune
   *
   * @param num
   * @return true (OK) false (Error)
   */
  private boolean doSelectChannelByTIFInfo(TIFChannelInfo tifChannel) {
    Log.d(TAG, "selectChannelByTIFInfo chInfo = " + tifChannel);
    if (tifChannel == null) {
        ScanViewActivity.isSelectedChannel = true;
      Log.d(TAG, Log.getStackTraceString(new Throwable()));
      return false;
    }

    if(tifChannel.mMtkTvChannelInfo != null){
     if (!CommonIntegration.isCNRegion() && CommonIntegration.isTVSourceSeparation() && tifChannel.mMtkTvChannelInfo != null) {
      TIFChannelInfo currentInfo = getTIFChannelInfoById(TIFFunctionUtil.getCurrentChannelId());
      if (tifChannel.mMtkTvChannelInfo.getBrdcstType() == MtkTvChCommonBase.BRDCST_TYPE_ANALOG) {
        if (currentInfo != null
            && currentInfo.mMtkTvChannelInfo != null
            && currentInfo.mMtkTvChannelInfo.getBrdcstType()
              != MtkTvChCommonBase.BRDCST_TYPE_ANALOG) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "D-A selectChannelByTIFInfo setChgSource = true");
//          MtkTvMultiView.getInstance().setChgSource(true);
        }
      } else {
        if (currentInfo != null
            && currentInfo.mMtkTvChannelInfo != null
            && currentInfo.mMtkTvChannelInfo.getBrdcstType()
              == MtkTvChCommonBase.BRDCST_TYPE_ANALOG) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "A-D selectChannelByTIFInfo setChgSource = true");
//          MtkTvMultiView.getInstance().setChgSource(true);
        }
      }
     }
    }
    return selectChannelByTIFId(tifChannel.mId, tifChannel.mInputServiceName);
  }

  /**
   * in according to tif channel ID to select channel
   *
   * @param channelID
   */
  public boolean selectChannelByTIFId(final long tifChannelId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByTIFId channelId =" + tifChannelId);
    // mPreCatcheTIFChannelInfo = getTIFChannelInfoById(TIFFunctionUtil.getCurrentChannelId());
    final String path = mCI.getCurrentFocus();
    String channelUri = buildChannelUri(tifChannelId).toString();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "path>>>" + path + "  uri:" + channelUri);
    // because LiveTVSetting is separated whit LiveTV so only use broadcaster to send data
    Intent intent = new Intent("com.mediatek.tv.selectchannel");
    intent.putExtra("path", path);
    intent.putExtra("channelUriStr", channelUri);
    mContext.sendBroadcast(intent);
    return true;
  }

  /**
   * in according to tif channel ID to select channel
   *
   * @param channelID
   */
  private boolean selectChannelByTIFId(final long tifChannelId, String inputId) {
      Log.d(TAG, "selectChannelByTIFId channelId =" + tifChannelId);
      Bundle params = new Bundle();
      // mPreCatcheTIFChannelInfo = getTIFChannelInfoById(TIFFunctionUtil.getCurrentChannelId());
      final String path = mCI.getCurrentFocus();
      // String inputId = InputSourceManager.getInstance().getTvInputId(path);
      // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "path>>>" + path + "    inputId:" + inputId + "  uri:" + tifChannelId);
      // if (TextUtils.isEmpty(inputId)) {
      // if (CommonIntegration.isCNRegion()) {
      // if (mCI.isCurrentSourceATV()) {
      // inputId = INPUT_ATV_ID;
      // } else {
      // inputId = INPUT_DTV_ID;
      // }
      // } else {
      // inputId = INPUT_DTV_ID;
      // }
      // }
      if (TextUtils.isEmpty(inputId)) {
          TIFChannelInfo tifChannelInfo = getTIFChannelInfoPLusByProviderId(tifChannelId);
          if (tifChannelInfo == null){
              return false;
          }
          String inputServiceName = tifChannelInfo.mInputServiceName;
          if (TextUtils.isEmpty(inputServiceName)) {
              Log.i(TAG, "inputId again null");
              return false;
          } else {
              inputId = inputServiceName;
          }
      }
      final String inputIdtemp = inputId;
      if (path != null && TurnkeyUiMainActivity.getInstance() != null
              && path.equalsIgnoreCase(InputSourceManager.MAIN)
              && TurnkeyUiMainActivity.getInstance().getTvView() != null) {
          TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  if (TurnkeyUiMainActivity.getInstance().getTvView() != null) {
                      params.putInt("Path_Logo", 0);
                      TurnkeyUiMainActivity.getInstance().getTvView()
                              .tune(inputIdtemp, buildChannelUri(tifChannelId), params);
                      ScanViewActivity.isSelectedChannel = true;
                  }
              }
          });
      } else if (path != null && TurnkeyUiMainActivity.getInstance() != null
              && path.equalsIgnoreCase(InputSourceManager.SUB)
              && TurnkeyUiMainActivity.getInstance().getPipView() != null) {
          TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {

              @Override
              public void run() {
                  if (TurnkeyUiMainActivity.getInstance().getPipView() != null) {
                      Log.d(TAG, "setNewTvMode:"
                              + mCI.getCurrentTVState());
                      MtkTvMultiView.getInstance().setNewTvMode(
                              mCI.getCurrentTVState());
                      params.putInt("Path_Logo", 1);
                      TurnkeyUiMainActivity.getInstance().getPipView()
                              .tune(inputIdtemp, buildChannelUri(tifChannelId), params);
                      ScanViewActivity.isSelectedChannel = true;
                  }
              }
          });
      } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByTIFId return false");
          ScanViewActivity.isSelectedChannel = true;
          return false;
      }
      return true;
  }

  public Uri buildChannelUri(long channelId) {
    return ContentUris.withAppendedId(TvContract.Channels.CONTENT_URI, channelId);
  }

  /**
   * selected channel up true(OK),false(error)
   */
  public boolean channelUpDownByMask(boolean isUp, int mask, int val) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMask");
    // PIP/POP
    if (mCI.isPipOrPopState()) {
      // current input is conflict with TV source, pre_channel fail
      List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
      String sourceName = InputSourceManager.getInstance()
          .getCurrentInputSourceName(mCI.getCurrentFocus());
      if (!mCI.isCurrentSourceTv()
          || (sourceList != null && sourceList.contains(sourceName))) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict:" + sourceName);
        return false;
      }
    }
    if (mCI.isCurrentSourceTv()) {
      if (mCI.isMenuInputTvBlock()) {
        return false;
      }
      TIFChannelInfo tifChannel = getTIFUpOrDownChannel(!isUp, mask, val);
      if (tifChannel == null) {
        return false;
      }
    //  MtkTvChannelInfoBase chInfo = tifChannel.mMtkTvChannelInfo;
   //   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMask chInfo = " + chInfo);
   //   if (chInfo != null) {
        // return mCI.selectChannelByInfo(chInfo);
        return selectChannelByTIFInfo(tifChannel);
   //   }
    } else if(mCI.is3rdTVSource()){
         TIFChannelInfo tifChannel = getTIFUpOrDownChannelfor3rdsource(!isUp);
         if(tifChannel !=null){
             return selectChannelByTIFInfo(tifChannel);
         }
    }else{
         mCI.iSetSourcetoTv();// non TV source, just jump to TV source, not change channel
    }
    return false;
  }
  /**
   * selected channel up true(OK),false(error)
   */
  public boolean dukChannelUpDownByMask(boolean isUp, int mask, int val,int sdtServiceType) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dukChannelUpDownByMask");
    // PIP/POP
    if (mCI.isPipOrPopState()) {
      // current input is conflict with TV source, pre_channel fail
      List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
      String sourceName = InputSourceManager.getInstance()
          .getCurrentInputSourceName(mCI.getCurrentFocus());
      if (!mCI.isCurrentSourceTv()
          || (sourceList != null && sourceList.contains(sourceName))) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict:" + sourceName);
        return false;
      }
    }
    if (mCI.isCurrentSourceTv()) {
      if (mCI.isMenuInputTvBlock()) {
        return false;
      }
      TIFChannelInfo tifChannel = getTIFUpOrDownDukChannel(!isUp, mask, val,sdtServiceType,false);
      if (tifChannel == null) {
        return false;
      }
        return selectChannelByTIFInfo(tifChannel);
    }else{
         mCI.iSetSourcetoTv();// non TV source, just jump to TV source, not change channel
    }
    return false;
  }
  public TIFChannelInfo getUpAndDownChannel(boolean isUp) {
    return getTIFUpOrDownChannel(isUp, TIFFunctionUtil.CH_LIST_MASK,
        TIFFunctionUtil.CH_LIST_VAL);
  }

  /**
   * get previous or next channel
   *
   * @param isPreChannel
   * @param attentionMask
   * @param expectValue
   * @param showSkip :can show skip channel
   * @param sdtServiceType for duk
   * @return
   */
  private TIFChannelInfo getTIFUpOrDownDukChannel(boolean isPreChannel, int attentionMask,
      int expectValue,int sdtServiceType ,boolean showSkip) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getTIFUpOrDownDukChannel>>" + isPreChannel);

    int currentChannelId = TIFFunctionUtil.getCurrentChannelId();
    long newId = (int)(currentChannelId & 0xffffffffL);
    //TIFChannelInfo currentChannelInfo = getTIFChannelInfoById(currentChannelId);
    TIFChannelInfo current3rdChannelInfo= getChannelInfoByUri();
    if(current3rdChannelInfo != null && current3rdChannelInfo.mMtkTvChannelInfo ==null){
        currentChannelId = (int)current3rdChannelInfo.mId;
        newId = current3rdChannelInfo.mId;
    }
    boolean canGetData = false;
    // this option for skip SA minorNo channel.
    //boolean isSAsKIPOption = false;
    List<TIFChannelInfo> mChannelList = getChannelListForFindOrNomal();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownDukChannel isPrePage>>>" + isPreChannel  + "  mChannelList.size() = " + (mChannelList==null?mChannelList:mChannelList.size()));
    if (isPreChannel) {
        TIFChannelInfo temTIFChannel = null;
        for(int i=mChannelList.size()-1 ; i>=0;i-- ){
          temTIFChannel = mChannelList.get(i);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownDukChannel temTIFChannel>>> "+ temTIFChannel);
       //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
          if (canGetData) {
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFUpOrDownDukChannel isPreChannel canGetData>>> "+ canGetData);

               if (!temTIFChannel.mIsBrowsable) {
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1 getTIFUpOrDownDukChannel temTIFChannel.mIsBrowsable = false");
                   continue;
               }

               temTIFChannel.mMtkTvChannelInfo =  getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
                if ( temTIFChannel.mMtkTvChannelInfo != null && (!showSkip && temTIFChannel.isSkip())) {
                  continue;
                }
                if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 1 getTIFUpOrDownDukChannel>>");
                     return temTIFChannel;
               }

          } else {
            if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId== newId )) {
              canGetData = true;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownDukChannel> PreChannel canGetData = true");
            }
          }
        }
        for(int i=mChannelList.size()-1 ; i>=0;i-- ){

          temTIFChannel = mChannelList.get(i);

          if (!temTIFChannel.mIsBrowsable) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "2 getTIFUpOrDownDukChannel temTIFChannel.mIsBrowsable = false");
              continue;
          }

          temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
          if ( temTIFChannel.mMtkTvChannelInfo != null && (!showSkip && temTIFChannel.isSkip())) {
            continue;
          }
          if (temTIFChannel.mInternalProviderFlag3 != currentChannelId) {
                if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 2 getTIFUpOrDownDukChannel>>");
                      return temTIFChannel;
               }
          } else {
               break;
          }
        }
    } else {


     for(TIFChannelInfo temTIFChannel:mChannelList){
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownDukChannel> nextChannel temTIFChannel = "+temTIFChannel);
        if (canGetData) {
            if (!temTIFChannel.mIsBrowsable) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "3 getTIFUpOrDownDukChannel temTIFChannel.mIsBrowsable = false");
                continue;
            }
            temTIFChannel.mMtkTvChannelInfo =  getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
          if (temTIFChannel.mMtkTvChannelInfo != null && (!showSkip && temTIFChannel.isSkip())) {
            continue;
          }
          if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownDukChannel>>");
                return temTIFChannel;
            }

        } else {
          if (temTIFChannel != null && (temTIFChannel.mInternalProviderFlag3 == newId || temTIFChannel.mId== newId )) {
            canGetData = true;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 3 getTIFUpOrDownDukChannel> nextChannel canGetData = true");
          }
        }
      }

     for(TIFChannelInfo temTIFChannel:mChannelList){

         if (!temTIFChannel.mIsBrowsable) {
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownDukChannel temTIFChannel.mIsBrowsable = false");
             continue;
         }
         temTIFChannel.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(temTIFChannel.mInternalProviderFlag3);
          if (temTIFChannel.mMtkTvChannelInfo != null && (!showSkip && temTIFChannel.isSkip())) {
            continue;
          }
          if (temTIFChannel.mInternalProviderFlag3 != currentChannelId) {
              if (TIFFunctionUtil.checkChMaskForDuk(temTIFChannel, attentionMask, expectValue,sdtServiceType)) {
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end 4 getTIFUpOrDownDukChannel>>");
                   return temTIFChannel;
              }
          } else {
            break;
          }
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getTIFUpOrDownDukChannel>> null");
    return null;
  }

  /**
   * selected channel up or down by satellite true(OK),false(error)
   */
  public boolean channelUpDownByMaskAndSat(int mask, int val, int satRecId, boolean isUp) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMaskAndSat");
    // PIP/POP
    if (mCI.isPipOrPopState()) {
      // current input is conflict with TV source, pre_channel fail
      List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
      String sourceName = InputSourceManager.getInstance()
          .getCurrentInputSourceName(mCI.getCurrentFocus());
      if (!mCI.isCurrentSourceTv()
          || (sourceList != null && sourceList.contains(sourceName))) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict:" + sourceName);
        return false;
      }
    }
    if (mCI.isCurrentSourceTv()) {
      if (mCI.isMenuInputTvBlock()) {
        return false;
      }
      // int curChId = TIFFunctionUtil.getCurrentChannelId();
      // MtkTvChannelInfoBase chInfo = null;
      // List<TIFChannelInfo> chInfoList = getTIFPreOrNextChannelListBySateRecId(curChId, !isUp,
      // false, satRecId, 1, mask, val);
      // if (chInfoList != null && chInfoList.size() > 0) {
      // chInfo = chInfoList.get(0).mMtkTvChannelInfo;
      // }
      // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMaskAndSat chInfo = " + chInfo);
      // if (chInfo != null) {
      // return mCI.selectChannelByInfo(chInfo);
      // }
      TIFChannelInfo tifChannelInfo = getTIFUpOrDownChannelBySateRecId(!isUp, satRecId, mask, val);
      if (tifChannelInfo != null) {
        return selectChannelByTIFInfo(tifChannelInfo);
      } else {
        return false;
      }
    } else {// non TV source, just jump to TV source, not change channel
      mCI.iSetSourcetoTv();
    }
    return false;
  }

  public boolean hasDTVChannels() {
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID,
        getSvlIdSelectionArgs(), ORDERBY);
    if (c == null) {
      return false;
    }
    TIFChannelInfo temTIFChannel = null;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasDTVChannelsWithSvlid c.moveToNext()>>> c  " + c.getCount());
    boolean hasDTVChannel = false;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
   //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_LIST_MASK,
          TIFFunctionUtil.CH_LIST_VAL)) {
        if (!(tempApiChannel instanceof MtkTvAnalogChannelInfo)) {
          c.close();
          hasDTVChannel = true;
          return hasDTVChannel;
        }
      }
    }
    c.close();
    return hasDTVChannel;
  }

  public boolean hasATVChannels() {
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID,
        getSvlIdSelectionArgs(), ORDERBY);
    if (c == null) {
      return false;
    }
    TIFChannelInfo temTIFChannel = null;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasDTVChannelsWithSvlid c.moveToNext()>>> c  " + c.getCount());
    boolean hasATVChannel = false;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
    //  parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (TIFFunctionUtil.checkChMask(tempApiChannel, TIFFunctionUtil.CH_LIST_MASK,
          TIFFunctionUtil.CH_LIST_VAL)) {
        if (tempApiChannel instanceof MtkTvAnalogChannelInfo) {
          c.close();
          hasATVChannel = true;
          return hasATVChannel;
        }
      }
    }
    c.close();
    return hasATVChannel;
  }

  public boolean hasActiveChannel() {
      return hasActiveChannel(false);
  }

  public boolean hasActiveChannel(boolean showSkip) {
    List<TIFChannelInfo> chList = queryChanelListWithMaskCount(1, TIFFunctionUtil.CH_LIST_MASK,
        TIFFunctionUtil.CH_LIST_VAL,showSkip);
    if (chList != null && !chList.isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannel true~ ");
      return true;
    }
    // add for check current Channel if Hidden channel, channellistDialog need show Hidden channel
    // if it's current play channel.
    // for US
    if (CommonIntegration.isUSRegion()) {
      if (TIFFunctionUtil.checkChMask(getAPIChannelInfoById(TIFFunctionUtil.getCurrentChannelId()),
          TIFFunctionUtil.CH_LIST_MASK, 0)) {
        return true;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannel false ");
    return false;
  }

  public boolean hasActiveChannelForUS(boolean showSkip) {
        List<TIFChannelInfo> chList = queryChanelListWithMaskCount(2, TIFFunctionUtil.CH_LIST_MASK,
            TIFFunctionUtil.CH_LIST_VAL,showSkip);
        if (chList != null && !chList.isEmpty()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannelUS true~ ");
          return true;
        }
        // add for check current Channel if Hidden channel, channellistDialog need show Hidden channel
        // if it's current play channel.
        // for US
//      if (CommonIntegration.isUSRegion()) {
//        if (TIFFunctionUtil.checkChMask(getAPIChannelInfoById(TIFFunctionUtil.getCurrentChannelId()),
//            TIFFunctionUtil.CH_LIST_MASK, 0)) {
//          return true;
//        }
//      }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannelUS false ");
        return false;
      }

  public boolean hasOneChannel() {
    List<TIFChannelInfo> chList = queryChanelListWithMaskCount(2, TIFFunctionUtil.CH_LIST_MASK,
        TIFFunctionUtil.CH_LIST_VAL);
    if (chList != null && chList.size() <= 1) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "has only one channel true~ ");
      return true;
    } else {
      return false;
    }
  }

  /**
   * get channel count by satellite record Id
   *
   * @param sateRecordId
   * @return
   */
  public int getSatelliteChannelCount(int sateRecordId, int mask, int val) {
    int count = 0;
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID,
        getSvlIdSelectionArgs(), ORDERBY);
    if (c == null) {
      return count;
    }
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
   //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (tempApiChannel != null && TIFFunctionUtil.checkChMask(tempApiChannel, mask, val)) {
        if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
          MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
          if (tmpdvb.getSatRecId() == sateRecordId) {
            count++;
          }
        }
      }
    }
    c.close();
    return count;
  }

  /**
   * get channel count by satellite record Id
   *
   * @param sateRecordId
   * @param attentionCount
   * @return
   */
  public int getSatelliteChannelConfirmCount(int sateRecordId, int attentionCount, int mask,
      int val) {
    int count = 0;
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, SELECTION_WITH_SVLID,
        getSvlIdSelectionArgs(), ORDERBY);
    if (c == null) {
      return count;
    }
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
  //    parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (tempApiChannel != null && TIFFunctionUtil.checkChMask(tempApiChannel, mask, val)) {
        if (tempApiChannel instanceof MtkTvDvbChannelInfo) {
          MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo) tempApiChannel;
          if (tmpdvb.getSatRecId() == sateRecordId) {
            count++;
            if (count > attentionCount) {
              break;
            }
          }
        }
      }
    }
    c.close();
    return count;
  }

  public boolean isChannelsExist() {
    boolean isExist = false;
    String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, selection,
        selectionargs, ORDERBY);

    if (c == null) {
      return false;
    }

    while (c.moveToNext()) {
//      temTIFChannel = TIFChannelInfo.parse(c);
 //     parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      isExist = true;
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isChannelsExist:" + isExist);
    return isExist;
  }

  /**
   * check has inactive channel or not
   *
   * @param attentionMask
   * @param expectValue
   * @param count value:-1, num all; value>0, num count
   * @return
   */
  public List<TIFChannelInfo> getAttentionMaskChannels(int attentionMask, int expectValue,
      int count) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getAttentionMaskChannels~" + count);
    List<TIFChannelInfo> tIFChannelInfoList = new ArrayList<TIFChannelInfo>();
    String selection = SELECTION_WITH_SVLID;
    String[] selectionargs = getSvlIdSelectionArgs();
    if (mCI.isCNRegion() || mCI.isTVSourceSeparation()) {
      selection = SELECTION_WITH_SVLID_INPUTID;
      selectionargs = getSvlIdAndInputIdSelectionArgs();
    }
    Cursor c = mContentResolver.query(TvContract.Channels.CONTENT_URI, null, selection,
        selectionargs, ORDERBY);

    if (c == null) {
      return tIFChannelInfoList;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAttentionMaskChannels    c>>>" + c.getCount() + "   ORDERBY>>" + ORDERBY);
    TIFChannelInfo temTIFChannel = null;
    while (c.moveToNext()) {
      temTIFChannel = TIFChannelInfo.parse(c);
   //   parserTIFChannelData(temTIFChannel, temTIFChannel.mData);
      MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(temTIFChannel.mDataValue);
      if (tempApiChannel != null && TIFFunctionUtil.checkChMask(tempApiChannel, attentionMask, expectValue)) {
        temTIFChannel.mMtkTvChannelInfo = tempApiChannel;
        tIFChannelInfoList.add(temTIFChannel);
        if (count != -1 && tIFChannelInfoList.size() >= count) {
          break;
        }
      }
    }
    c.close();
    com.mediatek.wwtv.tvcenter.util.MtkLog
        .d(TAG, "end getAttentionMaskChannels  tIFChannelInfoList>>>" + tIFChannelInfoList.size());
    return tIFChannelInfoList;
  }

  public void selectCurrentChannelWithTIF() {
    SharedPreferences mSharedPreferences = mContext.getSharedPreferences(SPNAME,Context.MODE_PRIVATE);
    int channel3rdMId= mSharedPreferences.getInt(TIFFunctionUtil.current3rdMId, -1);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF, channel3rdMId :"+channel3rdMId);
    if(channel3rdMId > 0){
        TIFChannelInfo mTIFChannelInfo = getTifChannelInfoByUri(buildChannelUri(channel3rdMId));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF, mTIFChannelInfo : "+mTIFChannelInfo);
        if (null != mTIFChannelInfo) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF, selectChannelByTIFInfo 3rd");
            selectChannelByTIFInfo(mTIFChannelInfo);
          } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF,currentChannelInfo 3rd is null");
          }

    }else{
        int currentChannelId = TIFFunctionUtil.getCurrentChannelId();
        long newId = (currentChannelId & 0xffffffffL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF, newId =" + newId);
        TIFChannelInfo currentChannelInfo = getTIFChannelInfoById(currentChannelId);
        if (null != currentChannelInfo) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF, selectChannelByTIFInfo");
          selectChannelByTIFInfo(currentChannelInfo);
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurrentChannelWithTIF,currentChannelInfo is null");
        }
    }

  }

  public static void enableAllChannels(Context context) {
      ContentValues values = new ContentValues();
      values.put(TvContract.Channels.COLUMN_BROWSABLE, 1);
      context.getContentResolver().update(TvContract.Channels.CONTENT_URI, values, null, null);
  }


  /**
     * API channel to TIF channel
     * @param tifChannelList
     * @return
     * @author jg_xiaominli
     */
    public List<TIFChannelInfo> getTIFChannelList(List<MtkTvChannelInfoBase> apiChannelList) {
        List<TIFChannelInfo> chlist = new ArrayList<TIFChannelInfo>();
        if (apiChannelList == null || apiChannelList.isEmpty()) {
            return chlist;
        }
        TIFChannelInfo tempTIFChInfo = null;
        for (MtkTvChannelInfoBase tempApiChannel:apiChannelList) {
            tempTIFChInfo=getTIFChannelInfoById(tempApiChannel.getChannelId());
            if (tempTIFChInfo != null){
                tempTIFChInfo.mMtkTvChannelInfo=tempApiChannel;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelList tempTIFChInfo " +tempTIFChInfo);
                chlist.add(tempTIFChInfo);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelList tempTIFChInfo is null");
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelList chlist.size " +chlist.size());
        return chlist;
    }
    /**
     * API channel to TIF channel
     * @param MtkTvChannelInfoBase
     * @return List<TIFChannelInfo>
     * @author jg_xiaominli
     */
    public List<TIFChannelInfo> getTIFChannelListForHomeChannels(List<MtkTvChannelInfoBase> apiChannelList) {
        List<TIFChannelInfo> chlist = new ArrayList<TIFChannelInfo>();
        if (apiChannelList == null || apiChannelList.isEmpty()) {
            return chlist;
        }
        List<TIFChannelInfo> lists= queryRegionChanelListAll(TIFFunctionUtil.CH_LIST_MASK,TIFFunctionUtil.CH_LIST_VAL);
        Map<Integer,TIFChannelInfo> maps=new HashMap();
        for(TIFChannelInfo mtif:lists){
            maps.put(mtif.mInternalProviderFlag3,mtif) ;
        }
        TIFChannelInfo tempTIFChInfo = null;
        for (MtkTvChannelInfoBase tempApiChannel:apiChannelList) {
            tempTIFChInfo=maps.get(tempApiChannel.getChannelId());
            if (tempTIFChInfo != null){
                tempTIFChInfo.mMtkTvChannelInfo=tempApiChannel;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelList tempTIFChInfo " +tempTIFChInfo);
                chlist.add(tempTIFChInfo);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelList tempTIFChInfo is null");
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelList chlist.size " +chlist.size());
        return chlist;
    }
    /**
     * API channel to TIF channel
     * @param apiChannelList
     * @returnList<TIFChannelInfo>
     * @author jg_xiaominli
     */
    public List<TIFChannelInfo> getTIFChannelListForFavIndex(List<MtkTvChannelInfoBase> apiChannelList,int cURRENTFAVOURITETYPE) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex");
        List<TIFChannelInfo> chlist = new ArrayList<TIFChannelInfo>();
        if (apiChannelList == null || apiChannelList.isEmpty()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex apiChannelList is null");
            return chlist;
        }
        TIFChannelInfo tempTIFChInfo = null;
        for (MtkTvChannelInfoBase tempApiChannel:apiChannelList) {
            tempTIFChInfo = new TIFChannelInfo();
            tempTIFChInfo.copyFrom(getTIFChannelInfoById(tempApiChannel.getChannelId()));
            tempTIFChInfo.mMtkTvChannelInfo=tempApiChannel;
            switch (cURRENTFAVOURITETYPE) {
            case CommonIntegration.FAVOURITE_1:
                tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites1Index();
            //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempApiChannel.getFavorites1Index() " +tempApiChannel.getFavorites1Index());
                break;
            case CommonIntegration.FAVOURITE_2:
                tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites2Index();
            //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempApiChannel.getFavorites2Index() " +tempApiChannel.getFavorites2Index());
                break;
            case CommonIntegration.FAVOURITE_3:
                tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites3Index();
            //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex getFavorites3Index " +tempApiChannel.getFavorites3Index());
                break;
            case CommonIntegration.FAVOURITE_4:
                tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites4Index();
            //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex getFavorites4Index " +tempApiChannel.getFavorites4Index());
                break;
            default:
                tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites1Index();
            //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempApiChannel.getFavorites1Index() " +tempApiChannel.getFavorites1Index());
                break;
            }
          //  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempTIFChInfo " +tempTIFChInfo);
            chlist.add(tempTIFChInfo);
        }
        return chlist;
    }

    public List<TIFChannelInfo> getTIFChannelListForFavIndexForEPG(List<MtkTvChannelInfoBase> apiChannelList,int cURRENTFAVOURITETYPE) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex");
        List<TIFChannelInfo> chlist = new ArrayList<TIFChannelInfo>();
        if (apiChannelList == null || apiChannelList.isEmpty()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex apiChannelList is null");
            return chlist;
        }
        TIFChannelInfo tempTIFChInfo = null;
        for (MtkTvChannelInfoBase tempApiChannel:apiChannelList) {
            tempTIFChInfo = new TIFChannelInfo();
            tempTIFChInfo.copyFrom(getTIFChannelInfoById(tempApiChannel.getChannelId()));
            if (tempTIFChInfo.isAnalogService()){
                continue;
            }
            tempTIFChInfo.mMtkTvChannelInfo=tempApiChannel;
            switch (cURRENTFAVOURITETYPE) {
                case CommonIntegration.FAVOURITE_1:
                    tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites1Index();
                    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempApiChannel.getFavorites1Index() " +tempApiChannel.getFavorites1Index());
                    break;
                case CommonIntegration.FAVOURITE_2:
                    tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites2Index();
                    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempApiChannel.getFavorites2Index() " +tempApiChannel.getFavorites2Index());
                    break;
                case CommonIntegration.FAVOURITE_3:
                    tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites3Index();
                    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex getFavorites3Index " +tempApiChannel.getFavorites3Index());
                    break;
                case CommonIntegration.FAVOURITE_4:
                    tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites4Index();
                    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex getFavorites4Index " +tempApiChannel.getFavorites4Index());
                    break;
                default:
                    tempTIFChInfo.mDisplayNumber=""+tempApiChannel.getFavorites1Index();
                    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempApiChannel.getFavorites1Index() " +tempApiChannel.getFavorites1Index());
                    break;
            }
            //  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTIFChannelListForFavIndex tempTIFChInfo " +tempTIFChInfo);
            chlist.add(tempTIFChInfo);
        }
        return chlist;
    }
    private   List<TIFChannelInfo> mChannelsForCurrentSVLBase=new CopyOnWriteArrayList<>();
    private   List<TIFChannelInfo> mChannelsForCurrentSVL=new CopyOnWriteArrayList<>();
    private   List<TIFChannelInfo> mFindResultForChannels=new ArrayList<>();
    private   List<TIFChannelInfo> mChannelsFor3RDSource=new CopyOnWriteArrayList<>();
  //  private  volatile List<TIFChannelInfo> mChannelsForFakeList=new CopyOnWriteArrayList<>();
    CustomerComparator customerComparator;

    /////////////////////////////////////
    // start
    /////////////////////////////////////
    private static final int MSG_UPDATE_CHANNELS = 1000;

    private boolean mStarted;
    private boolean mDbLoadFinished;
    private volatile QueryAllChannelsTask mChannelsUpdateTask;

    private Handler mHandler;
    private final Set<Long> mBrowsableUpdateChannelIds = new HashSet<>();
    private final Set<Long> mLockedUpdateChannelIds = new HashSet<>();

    private ContentObserver mChannelObserver;

    private final Set<Listener> mListeners = new CopyOnWriteArraySet<>();
    private final Executor mDbExecutor =
        TvSingletons.getSingletons().getDbExecutor();
    private final com.android.tv.util.TvInputManagerHelper mInputManager =
        TvSingletons.getSingletons().getTvInputManagerHelper();

    private final List<Runnable> mPostRunnablesAfterChannelUpdate = new ArrayList<>();

    private final Map<Long, ChannelWrapper> mChannelWrapperMap = new HashMap<>();
    private final Map<String, MutableInt> mChannelCountMap = new HashMap<>();
    private final List<TIFChannelInfo> mChannels = new CopyOnWriteArrayList<>();
    private int queryDataBaseTimes =0;

    private final TvInputCallback mTvInputCallback =
            new TvInputCallback() {
                @Override
                public void onInputAdded(String inputId) {
                    Log.d(TAG, "onInputAdded()" );
                    if (DtvInput.DEFAULT_ID.equals(inputId) || AtvInput.DEFAULT_ID.equals(inputId)){
                        handleUpdateChannels();
                    }
                }

                @Override
                public void onInputRemoved(String inputId) {
                    Log.d(TAG, "onInputRemoved()" );
                }
            };

    private void init() {
        Log.d(TAG, "init()" );
        mHandler = new ChannelDataManagerHandler(this);


        mChannelObserver =
                new ContentObserver(mHandler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        if (!mHandler.hasMessages(MSG_UPDATE_CHANNELS)) {
                             Message msg=Message.obtain();
                             msg.what = MSG_UPDATE_CHANNELS;
                             msg.arg1 = 0;
                             mHandler.sendMessage(msg);
                        }
                    }
                };
        customerComparator=new CustomerComparator(mContext, CURRENT_CHANNEL_SORT);
        
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                getAllChannels();
            }
        });
    }

    ContentObserver getContentObserver() {
        return mChannelObserver;
    }

    /**
     * Starts the manager. If data is ready, {@link Listener#onLoadFinished()} will be called.
     */
    public void start() {

        Log.d(TAG, "start() mStarted =" +mStarted);
     //   com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTraceEx();
        if (mStarted) {
            return;
        }
        mStarted = true;
        // Should be called directly instead of posting MSG_UPDATE_CHANNELS message to the handler.
        // If not, other DB tasks can be executed before channel loading.
        handleUpdateChannels();
        mContentResolver.registerContentObserver(
                TvContract.Channels.CONTENT_URI, true, mChannelObserver);
        mInputManager.addCallback(mTvInputCallback);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start() mChannels.size() = " + getChannelCount());
    }

/*    public void setMStarted(boolean mStarted){
        this.mStarted = mStarted;
        Log.d(TAG, "setMStarted() mStarted =" +mStarted);
    }*/
    public void stop() {
        Log.d(TAG, "stop()" );
        if (!mStarted) {
            return;
        }
        mStarted = false;
        mDbLoadFinished = false;

        mInputManager.removeCallback(mTvInputCallback);
        mContentResolver.unregisterContentObserver(mChannelObserver);
        mHandler.removeCallbacksAndMessages(null);

        mChannelWrapperMap.clear();
        clearChannels();
        mPostRunnablesAfterChannelUpdate.clear();
        if (mChannelsUpdateTask != null) {
            mChannelsUpdateTask.cancel(true);
            mChannelsUpdateTask = null;
        }
        applyUpdatedValuesToDb();
    }

    /**
     * Adds a {@link Listener}.
     */
    public void addListener(Listener listener) {
        Log.d(TAG, "addListener " + listener);
        //SoftPreconditions.checkNotNull(listener);
        if (listener != null) {
            mListeners.add(listener);
        }
    }

    /**
     * Removes a {@link Listener}.
     */
    public void removeListener(Listener listener) {
        Log.d(TAG, "removeListener " + listener);
        //SoftPreconditions.checkNotNull(listener);
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    /**
     * Adds a {@link ChannelListener} for a specific channel with the channel ID {@code channelId}.
     */
    public void addChannelListener(Long channelId, ChannelListener listener) {
        ChannelWrapper channelWrapper = mChannelWrapperMap.get(channelId);
        if (channelWrapper == null) {
            return;
        }
        channelWrapper.addListener(listener);
    }

    /**
     * Removes a {@link ChannelListener} for a specific channel with the channel ID
     * {@code channelId}.
     */
    public void removeChannelListener(Long channelId, ChannelListener listener) {
        ChannelWrapper channelWrapper = mChannelWrapperMap.get(channelId);
        if (channelWrapper == null) {
            return;
        }
        channelWrapper.removeListener(listener);
    }

    /**
     * Checks whether data is ready.
     */
    public boolean isDbLoadFinished() {
        return mDbLoadFinished;
    }

    /**
     * Returns the number of channels.
     */
    public int getChannelCount() {
        return mChannels.size();
    }

    /**
     * Returns a list of channels.
     */
    public List<TIFChannelInfo> getChannelList() {
        return Collections.unmodifiableList(mChannels);
    }

    /**
     * Returns a list of browsable channels.
     */
    public List<TIFChannelInfo> getBrowsableChannelList() {
        List<TIFChannelInfo> channels = new ArrayList<>();
        for (TIFChannelInfo channel : mChannels) {
            if (channel.mIsBrowsable) {
                channels.add(channel);
            }
        }
        return channels;
    }

    /**
     * Returns the total channel count for a given input.
     *
     * @param inputId The ID of the input.
     */
    public int getChannelCountForInput(String inputId) {
        MutableInt count = mChannelCountMap.get(inputId);
        return count == null ? 0 : count.value;
    }

    /**
     * Checks if the channel exists in DB.
     *
     * <p>Note that the channels of the removed inputs can not be obtained from {@link #getChannel}.
     * In that case this method is used to check if the channel exists in the DB.
     */
    public boolean doesChannelExistInDb(long channelId) {
        return mChannelWrapperMap.get(channelId) != null;
    }

    /**
     * Returns true if and only if there exists at least one channel and all channels are hidden.
     */
    public boolean areAllChannelsHidden() {
        if (mChannels ==null || mChannels.isEmpty()) {
            return false;
        }
        for (TIFChannelInfo channel : mChannels) {
            if (channel.mIsBrowsable) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the channel with the channel ID {@code channelId}.
     */
    public TIFChannelInfo getChannel(Long channelId) {
        ChannelWrapper channelWrapper = mChannelWrapperMap.get(channelId);
        if (channelWrapper == null || channelWrapper.mInputRemoved) {
            return null;
        }
        return channelWrapper.mChannel;
    }

    /** The value change will be applied to DB when applyPendingDbOperation is called. */
    public void updateBrowsable(Long channelId, boolean browsable) {
        updateBrowsable(channelId, browsable, false);
    }

    /**
     * The value change will be applied to DB when applyPendingDbOperation is called.
     *
     * @param skipNotifyChannelBrowsableChanged If it's true, {@link Listener
     *     #onChannelBrowsableChanged()} is not called, when this method is called. {@link
     *     #notifyChannelBrowsableChanged} should be directly called, once browsable update is
     *     completed.
     */
    public void updateBrowsable(
            Long channelId, boolean browsable, boolean skipNotifyChannelBrowsableChanged) {/*
        ChannelWrapper channelWrapper = mData.channelWrapperMap.get(channelId);
        if (channelWrapper == null) {
            return;
        }
        if (channelWrapper.mChannel.isBrowsable() != browsable) {
            channelWrapper.mChannel.setBrowsable(browsable);
            if (browsable == channelWrapper.mBrowsableInDb) {
                mBrowsableUpdateChannelIds.remove(channelWrapper.mChannel.getId());
            } else {
                mBrowsableUpdateChannelIds.add(channelWrapper.mChannel.getId());
            }
            channelWrapper.notifyChannelUpdated();
            // When updateBrowsable is called multiple times in a method, we don't need to
            // notify Listener.onChannelBrowsableChanged multiple times but only once. So
            // we send a message instead of directly calling onChannelBrowsableChanged.
            if (!skipNotifyChannelBrowsableChanged) {
                notifyChannelBrowsableChanged();
            }
        }*/
    }

    public void notifyChannelBrowsableChanged() {
        for (Listener l : mListeners) {
            l.onChannelBrowsableChanged();
        }
    }

    private void notifyChannelListUpdated() {
        for (Listener l : mListeners) {
            l.onChannelListUpdated();
        }
    }

    private void notifyLoadFinished() {
        for (Listener l : mListeners) {
            l.onLoadFinished();
        }
    }

    /**
     * Updates channels from DB. Once the update is done, {@code postRunnable} will
     * be called.
     */
    public void updateChannels(Runnable postRunnable) {

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateChannels() DestroyApp.isCurTaskTKUI() :"+DestroyApp.isCurTaskTKUI());
        if(!DestroyApp.isCurTaskTKUI()){
            return ;
        }
        if (mChannelsUpdateTask != null) {
            mChannelsUpdateTask.cancel(true);
            mChannelsUpdateTask = null;
        }
        mPostRunnablesAfterChannelUpdate.add(postRunnable);
        if (!mHandler.hasMessages(MSG_UPDATE_CHANNELS)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateChannels() MSG_UPDATE_CHANNELS");
             Message msg=Message.obtain();
             msg.what = MSG_UPDATE_CHANNELS;
             msg.arg1 = 0;
             mHandler.sendMessage(msg);
        }
    }

    public void updateChannelLockByChannelInfo(TIFChannelInfo info, boolean locked) {
        if (info != null && info.mMtkTvChannelInfo != null) {
            int channelId = info.mMtkTvChannelInfo.getChannelId();
            updateLocked((long) channelId,locked);
            EditChannel.getInstance(mContext).blockChannel(info.mMtkTvChannelInfo, locked);
            for (TIFChannelInfo channel : getChannelList()) {
                if(channel.mId==info.mId){
                    channel.mLocked=locked;
                    break;
                }
            }
        }
    }

    /**
     * The value change will be applied to DB when applyPendingDbOperation is called.
     */
    public void updateLocked(Long channelId, boolean locked) {
        ChannelWrapper channelWrapper = mChannelWrapperMap.get(channelId);
        if (channelWrapper == null) {
            return;
        }
        if (channelWrapper.mChannel.mLocked != locked) {
            channelWrapper.mChannel.mLocked = locked;
            if (locked == channelWrapper.mLockedInDb) {
                mLockedUpdateChannelIds.remove(channelWrapper.mChannel.mId);
            } else {
                mLockedUpdateChannelIds.add(channelWrapper.mChannel.mId);
            }
            channelWrapper.notifyChannelUpdated();
        }
    }

    /**
     * Applies the changed values by {@link #updateBrowsable} and {@link #updateLocked}
     * to DB.
     */
    public void applyUpdatedValuesToDb() {
        ArrayList<Long> browsableIds = new ArrayList<>();
        ArrayList<Long> unbrowsableIds = new ArrayList<>();
        for (Long id : mBrowsableUpdateChannelIds) {
            ChannelWrapper channelWrapper = mChannelWrapperMap.get(id);
            if (channelWrapper == null) {
                continue;
            }
            if (channelWrapper.mChannel.mIsBrowsable) {
                browsableIds.add(id);
            } else {
                unbrowsableIds.add(id);
            }
            channelWrapper.mBrowsableInDb = channelWrapper.mChannel.mIsBrowsable;
        }
        String column = TvContract.Channels.COLUMN_BROWSABLE;
        /*if (mStoreBrowsableInSharedPreferences) {
            Editor editor = mBrowsableSharedPreferences.edit();
            for (Long id : browsableIds) {
                editor.putBoolean(getBrowsableKey(getChannel(id)), true);
            }
            for (Long id : unbrowsableIds) {
                editor.putBoolean(getBrowsableKey(getChannel(id)), false);
            }
            editor.apply();
        } else */{
            if (browsableIds !=null && !browsableIds.isEmpty()) {
                updateOneColumnValue(column, 1, browsableIds);
            }
            if (unbrowsableIds !=null && !unbrowsableIds.isEmpty()) {
                updateOneColumnValue(column, 0, unbrowsableIds);
            }
        }
        mBrowsableUpdateChannelIds.clear();

        ArrayList<Long> lockedIds = new ArrayList<>();
        ArrayList<Long> unlockedIds = new ArrayList<>();
        for (Long id : mLockedUpdateChannelIds) {
            ChannelWrapper channelWrapper = mChannelWrapperMap.get(id);
            if (channelWrapper == null) {
                continue;
            }
            if (channelWrapper.mChannel.mLocked) {
                lockedIds.add(id);
            } else {
                unlockedIds.add(id);
            }
            channelWrapper.mLockedInDb = channelWrapper.mChannel.mLocked;
        }
        column = TvContract.Channels.COLUMN_LOCKED;
        if (lockedIds != null && !lockedIds.isEmpty()) {
            updateOneColumnValue(column, 1, lockedIds);
        }
        if (unlockedIds != null && !unlockedIds.isEmpty()) {
            updateOneColumnValue(column, 0, unlockedIds);
        }
        mLockedUpdateChannelIds.clear();
        Log.d(TAG, "applyUpdatedValuesToDb"
                + "\n browsableIds size:" + browsableIds.size()
                + "\n unbrowsableIds size:" + unbrowsableIds.size()
                + "\n lockedIds size:" + lockedIds.size()
                + "\n unlockedIds size:" + unlockedIds.size());
    }

    private void addChannel(TIFChannelInfo channel) {
        mChannels.add(channel);
 //       Log.d(TAG, "addChannel() TIFChannelInfo : "+channel);
        String inputId = channel.mInputServiceName;
        MutableInt count = mChannelCountMap.get(inputId);
        if (count == null) {
            mChannelCountMap.put(inputId, new MutableInt(1));
        } else {
            count.value++;
        }
    }

    private void clearChannels() {
        mChannels.clear();
        mChannelCountMap.clear();
    }

    public void handleUpdateChannels() {
        if(queryDataBaseTimes < 50){
            if (mChannelsUpdateTask != null){
                updateChannelIntercept = true;
                return;
            }
            updateChannelIntercept = false;
            mChannelsUpdateTask = new QueryAllChannelsTask();
            TVAsyncExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    mChannelsUpdateTask.executeOnDbThread();
                }
            });
            //com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUpdateChannels() mChannels.size() = " + getChannelCount());
        }else{
            queryDataBaseTimes=0;
        }
    }

    /**
     * Reloads channel data.
     */
    public void reload() {
        Log.d(TAG, "reload() " );
        if (mDbLoadFinished && !mHandler.hasMessages(MSG_UPDATE_CHANNELS)) {
            Log.d(TAG, "reload() MSG_UPDATE_CHANNELS" );
             Message msg=Message.obtain();
             msg.what = MSG_UPDATE_CHANNELS;
             msg.arg1 = 0;
             mHandler.sendMessage(msg);
        }
    }

    public interface Listener {
        /**
         * Called when data load is finished.
         */
        void onLoadFinished();

        /**
         * Called when channels are added, deleted, or updated. But, when browsable is changed,
         * it won't be called. Instead, {@link #onChannelBrowsableChanged} will be called.
         */
        void onChannelListUpdated();

        /**
         * Called when browsable of channels are changed.
         */
        void onChannelBrowsableChanged();
    }

    public interface ChannelListener {
        /**
         * Called when the channel has been removed in DB.
         */
        void onChannelRemoved(TIFChannelInfo channel);

        /**
         * Called when values of the channel has been changed.
         */
        void onChannelUpdated(TIFChannelInfo channel);
    }

    private class ChannelWrapper {
        final Set<ChannelListener> mChannelListeners = new ArraySet<>();
        final TIFChannelInfo mChannel;
        boolean mBrowsableInDb;
        boolean mLockedInDb;
        boolean mInputRemoved;

        ChannelWrapper(TIFChannelInfo channel) {
            mChannel = channel;
            mBrowsableInDb = channel.mIsBrowsable;
            mLockedInDb = channel.mLocked;
            mInputRemoved = !mInputManager.hasTvInputInfo(channel.mInputServiceName);
        }

        void addListener(ChannelListener listener) {
            mChannelListeners.add(listener);
        }

        void removeListener(ChannelListener listener) {
            mChannelListeners.remove(listener);
        }

        void notifyChannelUpdated() {
            for (ChannelListener l : mChannelListeners) {
                l.onChannelUpdated(mChannel);
            }
        }

        void notifyChannelRemoved() {
            for (ChannelListener l : mChannelListeners) {
                l.onChannelRemoved(mChannel);
            }
        }
    }

    private final class QueryAllChannelsTask extends AsyncDbTask.AsyncQueryListTask<TIFChannelInfo> {

        QueryAllChannelsTask() {
            super(
                    mDbExecutor,
                    mContext,
                    TvContract.Channels.CONTENT_URI,
                    null,//PROJECTION,
                    null,
                    null,
                    null);
        }

        @Override
        protected TIFChannelInfo fromCursor(Cursor c) {
            return TIFChannelInfo.parse(c);
        }

        @Override
        protected void onPostExecute(List<TIFChannelInfo> channels) {
            Log.d(TAG, "onPostExecute");
            if (channels == null) {
                queryDataBaseTimes++;
                 Message msg=Message.obtain();
                 msg.what = MSG_UPDATE_CHANNELS;
                 msg.arg1 = 1;
                 mHandler.sendMessageDelayed(msg, 500);
                mChannelsUpdateTask = null;
                return ;
            }
            Set<Long> removedChannelIds = new HashSet<>(mChannelWrapperMap.keySet());
            List<ChannelWrapper> removedChannelWrappers = new ArrayList<>();
            List<ChannelWrapper> updatedChannelWrappers = new ArrayList<>();

            boolean channelAdded = false;
            boolean channelUpdated = false;
            boolean channelRemoved = false;
            //Map<String, ?> deletedBrowsableMap = null;
            //if (mStoreBrowsableInSharedPreferences) {
            //    deletedBrowsableMap = new HashMap<>(mBrowsableSharedPreferences.getAll());
            //}
            Log.d(TAG, "onPostExecute channels.size = "+(channels==null?channels:channels.size()));
            for (TIFChannelInfo channel : channels) {
                /*
                if (mStoreBrowsableInSharedPreferences) {
                    String browsableKey = getBrowsableKey(channel);
                    channel.setBrowsable(
                            mBrowsableSharedPreferences.getBoolean(browsableKey, false));
                    deletedBrowsableMap.remove(browsableKey);
                }*/

                long channelId = channel.mId;
                boolean newlyAdded = !removedChannelIds.remove(channelId);
                if(channel.mType.equalsIgnoreCase(TIFFunctionUtil.channelType)){ //remove type =preview
                    if (!newlyAdded) {
                        mChannelWrapperMap.remove(channel.mId);
                    }
                    continue;
                }
                ChannelWrapper channelWrapper;
                if (newlyAdded) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute newlyAdded = "+newlyAdded);
                    //new CheckChannelLogoExistTask(channel)
                    //        .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    channelWrapper = new ChannelWrapper(channel);
                    mChannelWrapperMap.put(channel.mId, channelWrapper);
                    if (!channelWrapper.mInputRemoved) {
                        channelAdded = true;
                    }
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute newlyAdded = "+newlyAdded);
                    channelWrapper = mChannelWrapperMap.get(channelId);
                    channelWrapper.mInputRemoved = !mInputManager.hasTvInputInfo(channel.mInputServiceName);
        //            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute channelWrapper.mChannel.hasSameReadOnlyInfo(channel) = "+channelWrapper.mChannel.hasSameReadOnlyInfo(channel));
        //            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute  channel.mDisplayNumber= "+channel.mDisplayNumber+" channel.mIsBrowsable is "+channel.mIsBrowsable);
                    if (!channelWrapper.mChannel.hasSameReadOnlyInfo(channel)) {
                        // Channel data updated
                        TIFChannelInfo oldChannel = channelWrapper.mChannel;
                        // We assume that mBrowsable and mLocked are controlled by only TV app.
                        // The values for mBrowsable and mLocked are updated when
                        // {@link #applyUpdatedValuesToDb} is called. Therefore, the value
                        // between DB and ChannelDataManager could be different for a while.
                        // Therefore, we'll keep the values in ChannelDataManager.
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute oldChannel.mIsBrowsable = "+oldChannel.mIsBrowsable);
                    //    channel.mIsBrowsable = oldChannel.mIsBrowsable;
                        channel.mLocked = oldChannel.mLocked;
                        channelWrapper.mChannel.copyFrom(channel);
                        if (!channelWrapper.mInputRemoved) {
                            channelUpdated = true;
                            updatedChannelWrappers.add(channelWrapper);
                        }
                    }else{
                        channelWrapper.mChannel.copyFrom(channel);
                        if (!channelWrapper.mInputRemoved) {
                            channelUpdated = true;
                            updatedChannelWrappers.add(channelWrapper);
                        }
                    }
                }
            }
            /*
            if (mStoreBrowsableInSharedPreferences
                    && !deletedBrowsableMap.isEmpty()
                    && PermissionUtils.hasReadTvListings(mContext)) {
                // If hasReadTvListings(mContext) is false, the given channel list would
                // empty. In this case, we skip the browsable data clean up process.
                Editor editor = mBrowsableSharedPreferences.edit();
                for (String key : deletedBrowsableMap.keySet()) {
                    editor.remove(key);
                }
                editor.apply();
            }*/
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute removedChannelIds.size() = "+removedChannelIds.size());
            for (long id : removedChannelIds) {
                ChannelWrapper channelWrapper = mChannelWrapperMap.remove(id);
                if (!channelWrapper.mInputRemoved) {
                    channelRemoved = true;
                    removedChannelWrappers.add(channelWrapper);
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran vvvvvv mChannels.size = " + mChannels.size());
            clearChannels();
            Log.d(TAG, "onPostExecute mChannelWrapperMap.size() "+mChannelWrapperMap.size());
            for (ChannelWrapper channelWrapper : mChannelWrapperMap.values()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPostExecute !channelWrapper "+!channelWrapper.mInputRemoved);
                if (!channelWrapper.mInputRemoved) {
                    addChannel(channelWrapper.mChannel);
                }
            }
            if(mSdxChannelListener != null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onChannelLoadFinished "+channels.size());
                mSdxChannelListener.onChannelLoadFinished(channels.size());
            }
            
            if (!mDbLoadFinished) {
                mDbLoadFinished = true;
                notifyLoadFinished();
            } else if (channelAdded || channelUpdated || channelRemoved) {
                notifyChannelListUpdated();
            }
            for (ChannelWrapper channelWrapper : removedChannelWrappers) {
                channelWrapper.notifyChannelRemoved();
            }
            for (ChannelWrapper channelWrapper : updatedChannelWrappers) {
                channelWrapper.notifyChannelUpdated();
            }
            for (Runnable r : mPostRunnablesAfterChannelUpdate) {
                r.run();
            }
            mPostRunnablesAfterChannelUpdate.clear();
            TVAsyncExecutor.getInstance().execute(new Runnable() {
              @Override
                public void run() {
                 getCurrentSVLChannellist();
                //add BEGIN by jg_jianwang for DTV01570224
                 if(mChecker != null){
                     mChecker.check();
                 }
                 //add END by jg_jianwang for DTV01570224
                }
            });
            CommonIntegration.getInstance().getChannelAllandActionNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran vvvvvvsss mChannels.size = " + mChannels.size());
            mChannelsUpdateTask = null;
            queryDataBaseTimes=0;
            if (updateChannelIntercept){
                updateChannelIntercept = false;
                Message msg=Message.obtain();
                msg.what = MSG_UPDATE_CHANNELS;
                msg.arg1 = 0;
                mHandler.sendMessage(msg);
            }
        }
    }

    /**
     * Updates a column {@code columnName} of DB table {@code uri} with the value
     * {@code columnValue}. The selective rows in the ID list {@code ids} will be updated.
     * The DB operations will run on {@link AsyncDbTask#getExecutor()}.
     */
    private void updateOneColumnValue(
            final String columnName, final int columnValue, final List<Long> ids) {
        //if (!PermissionUtils.hasAccessAllEpg(mContext)) {
            // TODO: support this feature for non-system LC app. b/23939816
        //    return;
        //}
        AsyncDbTask.execute(new Runnable() {
            @Override
            public void run() {
                String selection = Utils.buildSelectionForIds(TvContract.Channels._ID, ids);
                ContentValues values = new ContentValues();
                values.put(columnName, columnValue);
                mContentResolver.update(TvContract.Channels.CONTENT_URI, values, selection, null);
            }
        });
    }
/*
    private String getBrowsableKey(TIFChannelInfo channel) {
        return channel.mInputServiceName + "|" + channel.mId;
    }
*/
    private static class ChannelDataManagerHandler extends WeakHandler<TIFChannelManager> {
        public ChannelDataManagerHandler(TIFChannelManager channelDataManager) {
            super(Looper.getMainLooper(), channelDataManager);
        }

        @Override
        public void handleMessage(Message msg, TIFChannelManager channelDataManager) {
            if (msg.what == MSG_UPDATE_CHANNELS) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage() DestroyApp.isCurTaskTKUI() :"+DestroyApp.isCurTaskTKUI()+" running name:"+channelDataManager.getRunningActivityName());
                if(!DestroyApp.isCurTaskTKUI() && msg.arg1 != 1 && !"com.mediatek.wwtv.setupwizard.TvWizardActivity".equals(channelDataManager.getRunningActivityName())){
                    return ;
                }
                Log.d(TAG, "ChannelDataManagerHandler handleMessage MSG_UPDATE_CHANNELS ");
                channelDataManager.handleUpdateChannels();
            }
        }
    }

    public String getRunningActivityName(){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = "";
        if (activityManager.getRunningTasks(1) != null && activityManager.getRunningTasks(1).size() > 0){
            //add by xun
            if(activityManager.getRunningTasks(1).isEmpty()){
                return runningActivity;
            }
            //end xun
            runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        }
        return runningActivity;
    }

/*
    private List<TIFChannelInfo> getCurrentSvlChannellistNotCheckMask() {
        List<TIFChannelInfo> result = new ArrayList<TIFChannelInfo>();
        int svlId= CommonIntegration.getInstance().getSvl();
        for(TIFChannelInfo chinfo:mChannels) {
        //    parserTIFChannelData(chinfo, chinfo.mData);
            if(chinfo.mDataValue != null && (chinfo.mDataValue.length == 6 || chinfo.mDataValue.length == TIFFunctionUtil.channelDataValuelength) && chinfo.mInternalProviderFlag1 == svlId){
                MtkTvChannelInfoBase tempApiChannel = getAPIChannelInfoByBlobData(
                        chinfo.mDataValue);
                chinfo.mMtkTvChannelInfo=tempApiChannel;
                result.add(chinfo);
            }
        }
        return result;
    }
*/
    private void getCurrentSVLChannellist() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist start" );
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist getChannelCount() is "+getChannelCount());
       int svlId= CommonIntegration.getInstance().getSvl();
       List<TIFChannelInfo> mChannelsForCurrentSVLBaseTemp=new ArrayList<>();
       List<TIFChannelInfo> mChannelsFor3RDSourceTemp=new ArrayList<>();
       for(TIFChannelInfo chinfo:mChannels) {
       //    parserTIFChannelData(chinfo, chinfo.mData);
      //   Log.d(TAG, "getCurrentSVLChannellist chinfo.mDataValue length is "+chinfo.mDataValue.length);
           if(chinfo.mDataValue != null && (chinfo.mDataValue.length == 6 || chinfo.mDataValue.length == TIFFunctionUtil.channelDataValuelength) ){
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist is livetv channel");
                if(chinfo.mInternalProviderFlag1 == svlId){
                         mChannelsForCurrentSVLBaseTemp.add(chinfo);
                 }

           }else if(chinfo.mDataValue == null || TIFFunctionUtil.is3rdTVSource(chinfo)){
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist is 3rd channel chinfo.mIsBrowsable"+chinfo.mIsBrowsable);
               if(chinfo.mIsBrowsable){
                   mChannelsFor3RDSourceTemp.add(chinfo);
               }
           }
       }
       mChannelsForCurrentSVLBase.clear();
       mChannelsForCurrentSVLBase.addAll(mChannelsForCurrentSVLBaseTemp);
      //deal base data
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist end mChannelsForCurrentSVLBase.size() "+mChannelsForCurrentSVLBase.size());
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist end mChannelsFor3RDSource.size() "+mChannelsFor3RDSource.size());
       // deal 3rd channels
        if (MarketRegionInfo.F_3RD_INPUTS_SUPPORT){
            mChannelsFor3RDSource.clear();
            mChannelsFor3RDSource.addAll(mChannelsFor3RDSourceTemp);
        }
       // sort channel

      /* if(CURRENT_CHANNEL_SORT == 3 || CURRENT_CHANNEL_SORT == 4){//3 nav_select_sort_encrypted, 4DDRA
            for(TIFChannelInfo tempinfo:mChannelsForCurrentSVLBase){
                 tempinfo.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(tempinfo.mInternalProviderFlag3);
             }
       }*/
       mChannelsForCurrentSVL.clear();
       mChannelsForCurrentSVL.addAll(mChannelsForCurrentSVLBase);
       mChannelsForCurrentSVL.addAll(mChannelsFor3RDSource);
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist end mChannelsForCurrentSVL.size() "+mChannelsForCurrentSVL.size());

       if(CURRENT_CHANNEL_SORT == 3 || CURRENT_CHANNEL_SORT == 4 || CURRENT_CHANNEL_SORT == 5){//3 nav_select_sort_encrypted, 4DDRA
           TVAsyncExecutor.getInstance().execute(new Runnable() {
               @Override
                 public void run() {
                   afterGetAllChanelsGoonsort=true;
                   if(!getAllChannelsrunning){
                       getAllChannels();
                   }else{
                       upDateAllChannelsrunning=true;
                   }
                 }
             });

      }else{
          updataChannelListSort();
          TVAsyncExecutor.getInstance().execute(new Runnable() {
              @Override
                public void run() {
                  if(!getAllChannelsrunning){
                      getAllChannels();
                  }else{
                      upDateAllChannelsrunning=true;
                  }
                }
            });
      }
       if(mCI.isCurrentSourceATV() || mCI.isCurrentSourceDTV() || mCI.isCurrentSourceTv()){
           if(getChannelInfoByUri() ==null 
                   && (SaveValue.getInstance(mContext).readValue(CommonIntegration.channelListfortypeMask,0) == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK)){
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentSVLChannellist end current chanenl is network,and this chanenl removed from the tv.db");
               if(!mChannelsFor3RDSource.isEmpty()){
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectchannel  netowrk for first channel ");
                   selectChannelByTIFInfo(mChannelsFor3RDSource.get(0));
               }else if(!mChannelsForCurrentSVLBase.isEmpty()){
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectchannel  broadcast for first channel ");
                   if (DvrManager.getInstance() != null && DvrManager.getInstance().pvrIsRecording()){
                       selectChannelByTIFInfo(getTIFChannelInfoById((int)SaveValue.getInstance(mContext).readLongValue(DvrManager.DVR_URI_ID)));
                   }else{
                       selectChannelByTIFInfo(mChannelsForCurrentSVLBase.get(0));
                   }
               }
           }
       }
    }
    /**
     * Returns a list of channels for current svl
     */
    public List<TIFChannelInfo> getCurrentSVLChannelList() {
        return Collections.unmodifiableList(mChannelsForCurrentSVL);
    }

    /**
     * Returns a list of channels for current svl
     */
    public List<TIFChannelInfo> get3RDChannelList() {
        Collections.sort(mChannelsFor3RDSource, new CustomerComparator(mContext, 0));
        return Collections.unmodifiableList(mChannelsFor3RDSource);
    }
    /**
     * Returns a list of channels for current svl
     */
    public List<TIFChannelInfo> getCurrentSVLChannelListBase() {
        Collections.sort(mChannelsForCurrentSVLBase, new CustomerComparator(mContext, 0));
        return mChannelsForCurrentSVLBase;
    }

    public void setCurrentChannelSort(int sort){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCurrentChannelSort " + sort);
        CURRENT_CHANNEL_SORT =sort;
        customerComparator.setSortType(sort);
        updataChannelListSort();
    }

    public void updataChannelListSort(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updataChannelListSort ");
        Collections.sort(mChannelsForCurrentSVL, customerComparator);
        Collections.sort(mChannelsForCurrentSVLBase, new CustomerComparator(mContext, 0));
    }

    public void afterGetAllChanelsGoonsort(){
        afterGetAllChanelsGoonsort=false;
        for(TIFChannelInfo tempinfo:mChannelsForCurrentSVL){
            tempinfo.mMtkTvChannelInfo = getAPIChannelInfoByChannelId(tempinfo.mInternalProviderFlag3);
        }
        updataChannelListSort();
    }
    public void mChannelListSort(List<TIFChannelInfo> mChlist){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChannelListSort");
        Collections.sort(mChlist, customerComparator);
    }
    public List<TIFChannelInfo> findChanelsForlist(String findStr,int mCurMask,int mCurVal){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findChanelsForlist findStr = "+findStr);
        if("".equals(findStr)){
            mFindResultForChannels.clear();
            return null;
        }
        mFindResultForChannels.clear();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findChanelsForlist mChannelsForCurrentSVLBase.size() "+mChannelsForCurrentSVLBase.size());
        mFindResultForChannels.addAll(findChanelsForlist(queryChanelListWithMaskCount(10000,mCurMask,mCurVal,false),findStr));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findChanelsForlist mFindResultForChannels.size() "+mFindResultForChannels.size());
        return mFindResultForChannels;
    }
    public List<TIFChannelInfo> getChannelListForFindOrNomal(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForFindOrNomal ");
        if(mChannels == null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForFindOrNomal mChannels =null");
            handleUpdateChannels();
        }
        if(mFindResultForChannels == null || mFindResultForChannels.isEmpty()){
            int svlId= mCI.getInstance().getSvl();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForFindOrNomal svlId = "+svlId);
            List<TIFChannelInfo> tempChannels=new ArrayList<>();
            tempChannels.addAll(mChannelsForCurrentSVLBase);
            if(tempChannels != null && !tempChannels.isEmpty() ){
               if(tempChannels.get(0).mInternalProviderFlag1 !=svlId){
                    getCurrentSVLChannellist();
                }
            }else{
                mChannelsForCurrentSVLBase=new CopyOnWriteArrayList<>();
                getCurrentSVLChannellist();
            }
            Log.d(TAG, "getChannelListForFindOrNomal mChannelsForCurrentSVL.size() "+mChannelsForCurrentSVL.size());
            List<TIFChannelInfo> templist = new ArrayList();
            templist.addAll(mChannelsForCurrentSVL);
            return templist;
        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForFindOrNomal mFindResultForChannels.size() "+mFindResultForChannels.size());
            return mFindResultForChannels;
        }

    }

    private List<TIFChannelInfo> findChanelsForlist(List<TIFChannelInfo> findList,String findStr){
        List<TIFChannelInfo> tempResultForChannels=new ArrayList<>();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findChanelsForlist findStr is "+findStr);
        for(TIFChannelInfo chinfo:findList) {
          //  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findChanelsForlist chinfo is "+chinfo);
            if((chinfo.mDisplayNumber!=null && chinfo.mDisplayNumber.contains(findStr)) || (chinfo.mDisplayName!=null&&chinfo.mDisplayName.toLowerCase(Locale.UK).contains(findStr.toLowerCase(Locale.UK)))){
                tempResultForChannels.add(chinfo);
                continue;
            }
        }
        Collections.sort(tempResultForChannels, new CustomerComparator(mContext, 0));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findChanelsForlist tempResultForChannels.size "+tempResultForChannels.size());
        return tempResultForChannels;
    }


    public TIFChannelInfo getChannelByNumOrName(String chNumOrName,boolean isName){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChanelByNumOrName() ");
        for(TIFChannelInfo chinfo:mChannelsForCurrentSVLBase) {
         //   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChanelByNumOrName chinfo is "+chinfo);

            if(isName){
               if(chinfo.mDisplayName.equalsIgnoreCase(chNumOrName)){
                    chinfo.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(chinfo.mDataValue);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChanelByNumOrName() isName chinfo is "+chinfo);
                    return chinfo;
                }
            }else{
               if(chinfo.mDisplayNumber.equals(chNumOrName)) {
                   chinfo.mMtkTvChannelInfo = getAPIChannelInfoByBlobData(chinfo.mDataValue);
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChanelByNumOrName() not isName chinfo is "+chinfo);
                   return chinfo;
               }
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChanelByNumOrName() not isName chinfo is null");
        return null;
    }

    public int getATVTIFChannelsForSourceSetup() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getATVTIFChannelsForSourceSetup~");
       int count= MtkTvChannelList.getInstance().getChannelCountByMask(mCI.getSvl(), MtkTvChCommonBase.SB_VNET_ACTIVE
               | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE | MtkTvChCommonBase.SB_VNET_FAKE, MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getATVTIFChannelsForSourceSetup  count>>>"+count);
        return count;
      }

    public int getDTVTIFChannelsForSourceSetup() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start getDTVTIFChannelsForSourceSetup~");
       int count= MtkTvChannelList.getInstance().getChannelCountByMask(mCI.getSvl(), MtkTvChCommonBase.SB_VNET_ACTIVE
               | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE, MtkTvChCommonBase.SB_VNET_ACTIVE);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end getDTVTIFChannelsForSourceSetup  count>>>"+count);
        return count;
      }

    //add BEGIN by jg_jianwang for DTV01570224
    public void setBlockCheck(BlockChecker checker) {
        mChecker = checker;
    }
    //add END by jg_jianwang for DTV01570224
    
    /**
     * Add sdx listener for dvbs sdx scan.
     */
    public void setSdxChannelListener(SdxChannelListener listener) {
        mSdxChannelListener = listener;
    }
    
    public interface SdxChannelListener {
        /**
         * Called when sdx data load is finished.
         */
        void onChannelLoadFinished(int channelNum);
    }
    
    public SdxChannelListener mSdxChannelListener;

    public void changeTypeToBroadCast(){
        Log.d(TAG, "changeTypeToBroadCast start ---");
        long midLong = SaveValue.getInstance(mContext).readLongValue(DvrManager.DVR_URI_ID,-1);
        Log.d(TAG, "changeTypeToBroadCast ooo midLong:"+midLong);
        int mid = (int)midLong;
        TIFChannelInfo minfo = queryChannelById(mid);
        int mask = SaveValue.getInstance(mContext).readValue(CommonIntegration.channelListfortypeMask,0);
        int val = SaveValue.getInstance(mContext).readValue(CommonIntegration.channelListfortypeMaskvalue,0);
        if (minfo != null && minfo.mMtkTvChannelInfo != null && !TIFFunctionUtil.checkChMask(minfo.mMtkTvChannelInfo,mask,val)){
            NavBasicDialog dialog = (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (dialog instanceof ChannelListDialog){
                ChannelListDialog channelListDialog =  (ChannelListDialog)dialog;
                channelListDialog.chooseChannelTypeByMask(0,mask,val);
            }
        }
    }

    public void channelListRearrangeFav(){
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelListRearrangeFav is run");
                for (int i = 0;i<4;i++){
                    MtkTvChannelList.getInstance().channellistRearrangeFav(CommonIntegration.getInstance().getSvl(),i);
                }
            }
        });
    }
}
