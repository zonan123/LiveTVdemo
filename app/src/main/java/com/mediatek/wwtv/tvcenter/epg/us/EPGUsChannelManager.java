
package com.mediatek.wwtv.tvcenter.epg.us;

import android.content.Context;
import android.util.Log;

import com.mediatek.twoworlds.tv.MtkTvEventATSC;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfo;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;

import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;

import java.util.ArrayList;
import java.util.List;

/***
 * get channel Info
 * change channel
 *
 */
public class EPGUsChannelManager {
  private static String TAG = "EPGUsChannelManager";
  private static EPGUsChannelManager epgUsChannelManager;

  private int mChannelNum;
  private String mChannelName;
  private String mChannnelDay;
  private String mChannelTime;// from..to..
  private String mChannelprogram;
  private boolean blocked;

  // private MtkTvChannelList mtkChannelManager;
  private final CommonIntegration integration;
  private final TIFChannelManager mTIFChannelManager;
  private List<MtkTvChannelInfoBase> mChannelList;
  private MtkTvChannelInfoBase curChannelInfo;
  private MtkTvChannelInfoBase preChannelInfo;
  private MtkTvChannelInfoBase nextChannel;
  private TIFChannelInfo mCurrentChannelTifInfo;
  private TIFChannelInfo mPreChannelTifInfo;
  private TIFChannelInfo mNextChannelTifInfo;

  public synchronized static EPGUsChannelManager getInstance(Context context) {
    if (epgUsChannelManager == null) {
      epgUsChannelManager = new EPGUsChannelManager(context);
    }
    return epgUsChannelManager;
  }

  public EPGUsChannelManager(Context context) {
    integration = CommonIntegration.getInstanceWithContext(context.getApplicationContext());
    mTIFChannelManager = TIFChannelManager.getInstance(context);
  }

  public boolean preChannel() {
    if (CommonIntegration.supportTIFFunction()) {
      Log.d(TAG, "preChannel()...");
      if (mCurrentChannelTifInfo != null && mPreChannelTifInfo != null
          && mCurrentChannelTifInfo.mId != mPreChannelTifInfo.mId) {
        Log.d(TAG, "preChannel()go...");
        mTIFChannelManager.selectChannelByTIFInfo(mPreChannelTifInfo);
        // integration.selectChannelByInfo(mPreChannelTifInfo.mMtkTvChannelInfo);
      } else {
        return false;
      }
    } else {
      integration.selectChannelByInfo(preChannelInfo);
      initChannelData();
    }
    // initChannelData();
    return true;
  }

  public boolean nextChannel() {
    if (CommonIntegration.supportTIFFunction()) {
      Log.d(TAG, "nextChannel()...");
      if (mCurrentChannelTifInfo != null && mNextChannelTifInfo != null
          && mCurrentChannelTifInfo.mId != mNextChannelTifInfo.mId) {
        Log.d(TAG, "nextChannel()go...");
        mTIFChannelManager.selectChannelByTIFInfo(mNextChannelTifInfo);
        // integration.selectChannelByInfo(mNextChannelTifInfo.mMtkTvChannelInfo);
      } else {
        return false;
      }
    } else {
      integration.selectChannelByInfo(nextChannel);
      initChannelData();
    }
    // initChannelData();
    return true;
  }

  public boolean isNextChannelDig() {
      if (CommonIntegration.supportTIFFunction() && mNextChannelTifInfo !=null && mNextChannelTifInfo.mMtkTvChannelInfo !=null) {
          return (mNextChannelTifInfo.mMtkTvChannelInfo.getBrdcstType() != MtkTvChCommonBase.BRDCST_TYPE_ANALOG);
      }
      return false;
    }

  public boolean isPreChannelDig() {
      if (CommonIntegration.supportTIFFunction() && mPreChannelTifInfo !=null && mPreChannelTifInfo.mMtkTvChannelInfo !=null) {
        return (mPreChannelTifInfo.mMtkTvChannelInfo.getBrdcstType() != MtkTvChCommonBase.BRDCST_TYPE_ANALOG);
      }
      return false;
   }

  public boolean isCurrentChannelDig() {
      if (CommonIntegration.supportTIFFunction() && mCurrentChannelTifInfo !=null && mCurrentChannelTifInfo.mMtkTvChannelInfo !=null) {
          return (mCurrentChannelTifInfo.mMtkTvChannelInfo.getBrdcstType() != MtkTvChCommonBase.BRDCST_TYPE_ANALOG);
      }
      return false;
   }

  public void initChannelData() {
    if (CommonIntegration.supportTIFFunction()) {
      if(integration.is3rdTVSource() && !integration.isDisableColorKey()) {
        long currntChId = TurnkeyUiMainActivity.getInstance().getTvView().getCurrentChannelId();
        mCurrentChannelTifInfo = mTIFChannelManager.getTIFChannelInfoPLusByProviderId(
            (currntChId == -1) ? 0 : currntChId);
    	mPreChannelTifInfo = mTIFChannelManager.getTIFUpOrDownChannelfor3rdsource(true);
        mNextChannelTifInfo = mTIFChannelManager.getTIFUpOrDownChannelfor3rdsource(false);
      } else {
        int currntChId = integration.getCurrentChannelId();
        mCurrentChannelTifInfo = mTIFChannelManager.getTIFChannelInfoById(currntChId);
        if(mCurrentChannelTifInfo!=null&&integration.isFakerChannel(mCurrentChannelTifInfo.mMtkTvChannelInfo)){
        	mPreChannelTifInfo = getTifChannelPrevious();
        	preChannel();
        	mCurrentChannelTifInfo=null;
        	mPreChannelTifInfo=null;
        	mNextChannelTifInfo=null;
        }
        else{
        	mPreChannelTifInfo = getTifChannelPrevious();
            mNextChannelTifInfo = getTifChannelNext();
        }
      }
      // if (mCurrentChannelTifInfo != null) {
      // if (mPreChannelTifInfo == null) {
      // mPreChannelTifInfo = mCurrentChannelTifInfo;
      // }
      // if (mNextChannelTifInfo == null) {
      // mNextChannelTifInfo = mCurrentChannelTifInfo;
      // }
      // }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentChannelTifInfo>>>" + mCurrentChannelTifInfo + " >>"
          + mPreChannelTifInfo + " >>" + mNextChannelTifInfo);
    } else {
      curChannelInfo = (MtkTvChannelInfoBase) integration.getCurChInfo();// (MtkTvChannelInfo)MtkTvChannelList.getCurrentChannel();
      preChannelInfo = getChannelPrevious();
      nextChannel = getChannelNext();
      if (preChannelInfo != null && nextChannel != null && curChannelInfo != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "curChannelInfo>>>" + preChannelInfo.getChannelNumber() + "  "
            + preChannelInfo.getChannelId() + "  " + preChannelInfo.getServiceName()
            + "   " + curChannelInfo.getChannelNumber() + "  " + curChannelInfo.getChannelId()
            + "  " + curChannelInfo.getServiceName()
            + "   " + nextChannel.getChannelNumber() + "  " + nextChannel.getChannelId() + "  "
            + nextChannel.getServiceName());
      }
      // if (curChannelInfo != null) {
      // if (preChannelInfo == null) {
      // preChannelInfo = curChannelInfo;
      // }
      // if (nextChannel == null) {
      // nextChannel = curChannelInfo;
      // }
      // mChannelList = integration.getEPGChList(curChannelInfo.getChannelId(), 1, 2);
      // if (mChannelList != null) {
      // com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "initChannelData:"+mChannelList.size());
      // for (int i = 0; i < mChannelList.size(); i++) {
      // com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "initChannelData:"+mChannelList.get(i).getChannelNumber() + "  " +
      // mChannelList.get(i).getServiceName());
      // }
      // }
      // }
    }
  }

  public int getCurrentChId() {
    return integration.getCurrentChannelId();
  }

  /**
   * return the channel info which is playing
   * @return
   */
  public MtkTvChannelInfoBase getChannelCurrent() {
    curChannelInfo = (MtkTvChannelInfoBase) integration.getCurChInfo();
    return curChannelInfo;
  }

  public String getCurrentChannelNum() {
    String number = "";
    if (mCurrentChannelTifInfo != null) {
      number = mCurrentChannelTifInfo.mDisplayNumber;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "number>>"+number);
      if(integration.getFavTypeState()){
        int favIndex = integration.getFavStateIndex();
        switch (favIndex){
          case CommonIntegration.FAVOURITE_1:
            number = String.valueOf(mCurrentChannelTifInfo.mMtkTvChannelInfo.getFavorites1Index());
            break;
          case CommonIntegration.FAVOURITE_2:
            number = String.valueOf(mCurrentChannelTifInfo.mMtkTvChannelInfo.getFavorites2Index());
              break;
          case CommonIntegration.FAVOURITE_3:
            number = String.valueOf(mCurrentChannelTifInfo.mMtkTvChannelInfo.getFavorites3Index());
            break;
          case CommonIntegration.FAVOURITE_4:
            number = String.valueOf(mCurrentChannelTifInfo.mMtkTvChannelInfo.getFavorites4Index());
            break;
          default:
            break;
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "2number>>"+number);
      // if (mCurrentChannelTifInfo.mMtkTvChannelInfo instanceof MtkTvATSCChannelInfo) {
      // MtkTvATSCChannelInfo tmpAtsc =
      // (MtkTvATSCChannelInfo)mCurrentChannelTifInfo.mMtkTvChannelInfo;
      // number = tmpAtsc.getMajorNum()+"-" +tmpAtsc.getMinorNum();
      // } else {
      // number = String.valueOf(mCurrentChannelTifInfo.mMtkTvChannelInfo.getChannelNumber());
      // }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelNum--->number="+number);
    return number;
  }

  public String getCurrentChannelName() {
    String name = "";
    if (mCurrentChannelTifInfo != null) {
      name = mCurrentChannelTifInfo.mDisplayName;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelName--->name="+name);
    return name;
  }

  public String getPreChannelNum() {
    String number = "";
    if (mPreChannelTifInfo != null) {
      number = mPreChannelTifInfo.mDisplayNumber;
      // if (mPreChannelTifInfo.mMtkTvChannelInfo instanceof MtkTvATSCChannelInfo) {
      // MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo)mPreChannelTifInfo.mMtkTvChannelInfo;
      // number = tmpAtsc.getMajorNum()+"-" +tmpAtsc.getMinorNum();
      // } else {
      // number = String.valueOf(mPreChannelTifInfo.mMtkTvChannelInfo.getChannelNumber());
      // }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getPreChannelNum:"+number);
    return number;
  }

  public String getNextChannelNum() {
    String number = "";
    if (mNextChannelTifInfo != null) {
      number = mNextChannelTifInfo.mDisplayNumber;
      // if (mNextChannelTifInfo.mMtkTvChannelInfo instanceof MtkTvATSCChannelInfo) {
      // MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo)mNextChannelTifInfo.mMtkTvChannelInfo;
      // number = tmpAtsc.getMajorNum()+"-" +tmpAtsc.getMinorNum();
      // } else {
      // number = String.valueOf(mNextChannelTifInfo.mMtkTvChannelInfo.getChannelNumber());
      // }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getNextChannelNum:"+number);
    return number;
  }

  /**
   * return the previous tif channel info
   */
  public TIFChannelInfo getTifChannelPrevious() {
//    if (preChannel == null && mCurrentChannelTifInfo != null) {
//      // preChannel = mCurrentChannelTifInfo;
//    }
    return mTIFChannelManager.getTIFUpOrDownChannelForUSEPG(true,
        CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
  }

  /**
   * return the previous channel info
   */
  public MtkTvChannelInfoBase getChannelPrevious() {
    return integration.getUpDownChInfoByFilter(true,
        integration.getChUpDownFilterEPG());
  }

  /**
   * return the previous tif channel info
   */
  public TIFChannelInfo getTifChannelNext() {
    return mTIFChannelManager.getTIFUpOrDownChannelForUSEPG(false,
        CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
  }

  /**
   * return the next channel info
   */
  public MtkTvChannelInfoBase getChannelNext() {
    return integration.getUpDownChInfoByFilter(false,
        integration.getChUpDownFilterEPG());
  }

  /**
   * return the channelList of all channel info
   * @return
   */
  public List<MtkTvChannelInfoBase> getChannelList() {
    return mChannelList;
  }

  /**
   *  get the location of the channel in the collection
   * @param channelInfo
   * @return
   */
//  private int getChannelPosition(MtkTvChannelInfoBase channelInfo) {
//    int position = 0;
//    com.mediatek.wwtv.tvcenter.util.MtkLog.e("mChannelList", "mChannelList:p:" + position);
//    if (null != mChannelList) {
//      position = mChannelList.indexOf(channelInfo);
//    }
//    return position;
//  }

  /****
   * Event
   */
  public void loadProgramEventDefault() {
    MtkTvEventATSC.getInstance().loadEvents(getChannelCurrent().getChannelId(), 0, 10);
    MtkTvEventInfo curEventInfo = (MtkTvEventInfo) MtkTvEventATSC.getInstance().getEvent(0);
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "curEventInfo:" + curEventInfo);
  }

  public int[] loadProgramEvent(int channelId, long startTime, int count) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("ChannelManager", "--- set time : [" + startTime + "," + count + "]");
    int[] value = MtkTvEventATSC.getInstance().loadEvents(channelId, startTime, count);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("ChannelManager", "returnvalue:" + value);
    return value;
  }

  /**
   * return all the program list of a channel
   * @param requests
   * @return
   */
  public List<MtkTvEventInfoBase> getProgramList(int value) {
    List<MtkTvEventInfoBase> mTVProgramInfoList = new ArrayList<MtkTvEventInfoBase>();
    mTVProgramInfoList.add(getProgramInfo(value));
    return mTVProgramInfoList;
  }

  /**
   * return one program info of the channel by requestId
   * @param requestId
   * @return
   */
  public MtkTvEventInfoBase getProgramInfo(int requestId) {
    return (MtkTvEventInfoBase) MtkTvEventATSC.getInstance().getEvent(requestId);
  }

  public int getmChannelNum() {
    return mChannelNum;
  }

  public String getmChannelName() {
    return mChannelName;
  }

  public String getmChannnelDay() {
    return mChannnelDay;
  }

  public String getmChannelTime() {
    return mChannelTime;
  }

  public String getmChannelprogram() {
    return mChannelprogram;
  }

  public boolean isBlocked() {
    return blocked;
  }

}
