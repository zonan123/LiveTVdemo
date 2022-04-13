
package com.mediatek.wwtv.setting.scan;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvTimeFormat;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.MtkTvChannelListBase;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvFreqChgCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvFreqChgParamBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.base.scan.model.DVBTCNScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBTScanner;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.tvcenter.TvSingletons;

/**
 * Methods about channel edit
 *
 * @author MTK40405
 */
public final class EditChannel {
  /* the position of data Item about channel edit */
  public static final int FREQUENCY = 2;
  public static final int COLOR = 3;
  public static final int SOUND = 4;
  public static final int AFT = 5;
  public static final int SKIP = 7;
  private static final String TAG = "EditChannel";
  private final CommonIntegration mCommonInter;
  private final Context mContext;
  private final TVContent mTVContent;
  boolean isBlockForTest;
  private int powerOnChannelNum = 0;
  private static EditChannel mEditChannel = null;
  private static final float mFineTuneStep = 0.065f;
  private static final float mFineTuneMax = 1.5f;
  private final static int FINECOUNT = 200;
  final int mFineCount = FINECOUNT;
  private float mRestoreHz = 0;
  private boolean isFineTuneDone = true;
  private final MenuConfigManager mcf;
  private static int SELECTCHANNEL = 999;
  /* store original information about color system and sound system */
  int mOriginalColorSystem;
  int mOriginalSoundSystem;

  /* store original information about frequency */
  private float mOriginalFrequency;
  private float mCenterFrequency = -1f;
  /* the flag to judge whether channel data is stored */
  private boolean isStored = true;

  private final SaveValue sv;
  private final MtkTvAppTVBase appTV;

  Handler mHandler;

  /**
   * Construct method
   *
   * @param context
   */
  private EditChannel(Context context) {
    mContext = context;
    mTVContent = TVContent.getInstance(mContext);
    mCommonInter = CommonIntegration.getInstance();
    appTV = new MtkTvAppTVBase();
    sv = SaveValue.getInstance(context);
    mcf = MenuConfigManager.getInstance(mContext);
    mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == SELECTCHANNEL){
                MtkTvChannelInfoBase selChannel = (MtkTvChannelInfoBase)msg.obj;
                mCommonInter.selectChannelByInfo(selChannel);
            }
            super.handleMessage(msg);
        }
    };
  }

  /**
   * get an instance of EditChannel
   *
   * @param context
   * @return
   */
  public static synchronized EditChannel getInstance(Context context) {
    if (mEditChannel == null) {
      mEditChannel = new EditChannel(context.getApplicationContext());
    }
    return mEditChannel;
  }

  /**
   * get restore Frequency
   *
   * @return
   */
  public float getRestoreHZ() {
    return mRestoreHz;
  }

  /**
   * set restore Frequency
   *
   * @param restoreHZ
   */
  public void setRestoreHZ(float restoreHZ) {
    this.mRestoreHz = restoreHZ;
  }

  /**
   * get stored flag
   *
   * @return
   */
  public boolean getStoredFlag() {
    return isStored;
  }

  /**
   * set colorSystem and soundSystem
   *
   * @param colorSystem
   * @param soundSystem
   */
  public void setOriginalTVSystem(int colorSystem, int soundSystem) {
    this.mOriginalColorSystem = colorSystem;
    this.mOriginalSoundSystem = soundSystem;
  }

  /**
   * set original frequency
   *
   * @param frequency
   */
  public void setOriginalFrequency(float frequency) {
    this.mOriginalFrequency = frequency;
  }

  /**
   * set stored flag
   *
   * @param isStored
   */
  public void setStoredFlag(boolean isStored) {
    this.isStored = isStored;
  }

  /**
   * Swap two channels
   *
   * @param from
   * @param to
   */
/*  public void swapChannel(int from, int to) {
    List<MtkTvChannelInfo> list = null;// mTVManager.getChannels();
    if (null != list) {
      // mTVManager.swapChannel(list.get(to), list.get(from));
      // mChannelSelector.select(list.get(to));
    }
  }*/

  /**
   * Insert channel
   *
   * @param from
   * @param to
   */
/*  public void insertChannel(int from, int to) {
    List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(mContext).getCh_list();
    if (null != list) {
      if (from < to) {
        try {
          // ITVCommon mItvCommon =
          // TVCommonNative.getDefault(mContext);
          // if (null != mItvCommon) {
          // mItvCommon.insertChannel(list.get(from), list.get(to));
          // }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (from > to) {
        // mTVManager.insertChannel(list.get(from), list.get(to));
      }
      // mChannelSelector.select(list.get(to));
      // mTVManager.flush();
    }
  }*/

  /**
   * Delete a channel
   *
   * @param deleteId the position of channel located in channel list
   * @return the channel list after finish deleting
   */
  public List<MtkTvChannelInfoBase> deleteChannel(int deleteId) {
    List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(mContext).getChList();
    if (list != null && deleteId < list.size()) {
    	List<Integer> listID = new ArrayList<Integer>();
    	listID.add(list.get(deleteId).getChannelId());
      deleteInactiveChannel(listID);
      list.remove(deleteId);
    }
    return list;
  }

  /**
   * for user to delete inactive channel(logo)
   * @param channelId
   */
  public boolean deleteInactiveChannel(List<Integer> channelIdList) {
    boolean deleteSucess = false;
    boolean isDeleteCurrentChannel = false;
    int channelId = 0;
    int operator=MtkTvChannelList.CHLST_OPERATOR_MOD;
    List<TIFChannelInfo> tifChannelInfoList = null;
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    for(Integer in:channelIdList){
    	channelId = (int) in;
    	  MtkTvChannelInfoBase selChannel = TIFChannelManager.getInstance(mContext)
    		        .getAPIChannelInfoById(in);
    		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteInactiveChannel selChannel>>>" + selChannel);
    		    if (null != selChannel) {

    		      selChannel.setChannelDeleted(true);

    		      if(selChannel.isAnalogService()){
    		          operator =MtkTvChannelList.CHLST_OPERATOR_DEL;
    		      }
    		      int nwMask = selChannel.getNwMask();
    		      selChannel.setNwMask(nwMask &= ~240); //240 fav1+fav2+fav3+fav4,cancel fav channel
    		      list.add(selChannel);

    		      if (channelId == mCommonInter.getCurrentChannelId()) {
    		    	  isDeleteCurrentChannel= true;
    		      }

    		    }
    }
    mCommonInter.setChannelList(operator, list);
   if(isDeleteCurrentChannel){
	   tifChannelInfoList = TIFChannelManager.getInstance(mContext).getTIFPreOrNextChannelList(
             channelId, false, false, 1, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
	   if (tifChannelInfoList != null && !tifChannelInfoList.isEmpty()) {
		   TIFChannelManager.getInstance(mContext).selectChannelByTIFId(tifChannelInfoList.get(0).mId);
	   }
   }

   deleteSucess = true;
   return deleteSucess;
   /* MtkTvChannelInfoBase selChannel = TIFChannelManager.getInstance(mContext)
        .getAPIChannelInfoById(channelId);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteInactiveChannel selChannel>>>" + selChannel);
    if (null != selChannel) {
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      selChannel.setChannelDeleted(true);
      int operator=MtkTvChannelList.CHLST_OPERATOR_MOD;
      if(selChannel.isAnalogService()){
          operator =MtkTvChannelList.CHLST_OPERATOR_DEL;
      }
      int nwMask = selChannel.getNwMask();
      selChannel.setNwMask(nwMask &= ~240); //240 fav1+fav2+fav3+fav4,cancel fav channel
      list.add(selChannel);
      mCommonInter.setChannelList(operator, list);
      List<TIFChannelInfo> tifChannelInfoList = null;
    //  TIFChannelManager.getInstance(mContext).updateMapsChannelInfoByChannel(selChannel);
      if (channelId == mCommonInter.getCurrentChannelId()) {
          tifChannelInfoList = TIFChannelManager.getInstance(mContext).getTIFPreOrNextChannelList(
                  channelId, false, false, 1, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
      }
      if (tifChannelInfoList != null && tifChannelInfoList.size() > 0) {
        TIFChannelManager.getInstance(mContext).selectChannelByTIFId(tifChannelInfoList.get(0).mId);
      }
      deleteSucess = true;
    }
    return deleteSucess;*/
  }

  /**
   * for user to delete all inactive channels(logo)
   * @param channelId
   */
  public boolean deleteAllInactiveChannels(List<TIFChannelInfo> inactiveChannelList) {
    boolean deleteSucess = false;
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    for (TIFChannelInfo tempInfo : inactiveChannelList) {
      list.add(TIFChannelManager.getInstance(mContext).getAPIChannelInfoById(
          tempInfo.mMtkTvChannelInfo.getChannelId()));
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteAllInactiveChannels list.size()>>>" + list.size());
    // List<TIFChannelInfo> tifChannelInfoList =
    // TIFChannelManager.getInstance(mContext)
    //.getAttentionMaskChannels(TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK,
    // TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL, -1);
    if (!list.isEmpty()) {
      // list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_DEL, list);
      deleteSucess = true;
    }
    return deleteSucess;
  }

  /**
   * restore Fine tune
   */
  public void restoreFineTune() {
    MtkTvChannelInfoBase channel = mCommonInter.getCurChInfoByTIF();
    if (channel != null) {
      channel.setFrequency((int) (mRestoreHz * 1000000));
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(channel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
      exitFinetune(mRestoreHz);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "restoreFineTune : channel == null");
    }
  }

  public void saveFineTune() {
    MtkTvChannelInfoBase channel = mCommonInter.getCurChInfoByTIF();
    if (channel != null) {
      channel.setFrequency((int) (mOriginalFrequency * 1000000));
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(channel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
      //   TIFChannelManager.getInstance(mContext).updateMapsChannelInfoByChannel(channel);
      exitFinetune(mOriginalFrequency);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveFineTune : channel == null");
    }
  }

  /**
   * get the current channel number
   *
   * @return
   */
  public int getCurrentChannelNumber() {
    int channelNum = 0;
    MtkTvChannelInfoBase currentChannel = mCommonInter.getCurChInfoByTIF();
    if (null != currentChannel) {
      channelNum = currentChannel.getChannelNumber();
    }
    return channelNum;
  }

  public int getCurrentChannelId() {
    return mCommonInter.getCurrentChannelId();
  }

  public void selectChannel(int lastChannelId) { // scan complete channel change, with TIF
    if (isCurrentSourceTv() &&  mCommonInter.getChannelActiveNumByAPIForScan() > 0) {
      if (mCommonInter.selectChannelById(lastChannelId)) {
        // do nothing
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "selectChannel lastChannelId:" + lastChannelId);
      } else {
        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
        List<TIFChannelInfo> tempList = TIFChannelManager.getInstance(mContext)
            .getTIFPreOrNextChannelList(-1, false, false, 2, TIFFunctionUtil.CH_LIST_MASK,
                TIFFunctionUtil.CH_LIST_VAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "selectChannel first channel:" + (tempList == null ? null : tempList.size()));
        if (tempList != null && !tempList.isEmpty()) {
          TIFChannelManager.getInstance(mContext).selectChannelByTIFId(tempList.get(0).mId);
        }
      }
    }
  }

  public MtkTvChannelInfoBase getSelectChannel(int lastChannelId) {
    if (mCommonInter.getChannelAllNumByAPI() > 0) {
      MtkTvChannelInfoBase channel = mCommonInter.getChannelById(lastChannelId);
      if (channel == null) {
        // do nothing
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "selectChannel first channel:");
        List<MtkTvChannelInfoBase> chList = mCommonInter.getChList(0, 0, 3);
        if (chList == null) {
          return null;
        }
        int index = 0;
        for (int i = 0; i < chList.size(); i++) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "selectChannel first channel:" + chList.get(i).getNwMask()
              + "SB_VNET_FAKE:" + MtkTvChCommonBase.SB_VNET_FAKE);
          if ((chList.get(i).getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
              != MtkTvChCommonBase.SB_VNET_FAKE) {
            index = i;
            break;
          }

        }
        if (index < chList.size()) {
          channel = chList.get(index);
        }

      }
      return channel;
    }
    return null;
  }

  /**
  * store channel data
  *
  * @param channelNumber
  *            channel number
  * @param channelName
  *            channel name
  * @param channelFrequency
  *            channel frequency
  * @param colorSystem
  *            color system
  * @param soundSystem
  *            sound system
  * @param aft
  *            auto fine tune
  * @param skip
  *            skip flag
  */
  public void storeChannel(int channelNumber, String channelName,
      String channelFrequency, int colorSystem, int soundSystem,
      int autoFineTune, int skip) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelNumber>>" + channelNumber + ">>" + channelName + ">>" + channelFrequency
        + ">>>" + colorSystem
        + ">>>" + soundSystem + ">>>" + autoFineTune + ">>>" + skip);
    MtkTvChannelInfoBase currentChannel = mCommonInter.getCurChInfoByTIF();
    if (null != currentChannel) {
      currentChannel.setChannelNumber(channelNumber);
      currentChannel.setServiceName(channelName);
      int pwdShow = MtkTvPWDDialog.getInstance().PWDShow();
      if (currentChannel instanceof MtkTvAnalogChannelInfo) {
        currentChannel.setFrequency((int) (Float.parseFloat(channelFrequency) * 1000000));
        if (pwdShow != 1) {
          ((MtkTvAnalogChannelInfo) currentChannel).setNoAutoFineTune(true);
        } else {
          ((MtkTvAnalogChannelInfo) currentChannel).setNoAutoFineTune(autoFineTune == 0 ? true
              : false);
        }
      }
      currentChannel.setSkip(skip != 0 ? true : false);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(currentChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
      isStored = true;
      mOriginalColorSystem = colorSystem;
      mOriginalSoundSystem = soundSystem;
    }
  }

  public boolean isFineTuneDone() {
    return isFineTuneDone;
  }

  /**
   * for channel edit store channel and current channel freq modify
   */
  public void exitFinetune() {
    MtkTvChannelInfoBase channel = mCommonInter.getCurChInfoByTIF();
    if (channel != null) {
      int frequency = channel.getFrequency();
      mCenterFrequency = -1f;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "++++++++ Call exitFinetune ++++++++");
      if (CommonIntegration.getInstance().getCurrentFocus().equalsIgnoreCase("sub")) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " SUB TV");
        appTV.setFinetuneFreq("sub", frequency, true);
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " MAIN TV");
        appTV.setFinetuneFreq("main", frequency, true);
      }
      isFineTuneDone = true;
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "exitFinetune : channel == null");
    }
  }

  /**
   * exit fine tune mode when fine tune is completed
   */
  public void exitFinetune(float frequency) {
    mCenterFrequency = -1f;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "++++++++ Call exit fine tune ++++++++");
    if (CommonIntegration.getInstance().getCurrentFocus()
        .equalsIgnoreCase("sub")) {
      appTV.setFinetuneFreq("sub", (int) (frequency * 1000000), true);
    } else {
      appTV.setFinetuneFreq("main", (int) (frequency * 1000000), true);
    }
    isFineTuneDone = true;

  }

  /**
   * fine tune
   *
   * @param originalMHZ original million HZ
   * @param isUp judge the action of user
   * @return million HZ after fine tune
   */
  public float fineTune(float originalMHZ, int keyCode) {
    //float original = 0;
    if (mCenterFrequency == -1f) {
      MtkTvChannelInfoBase channel = mCommonInter.getCurChInfoByTIF();
      if (channel != null) {
        mCenterFrequency = ((float) ((MtkTvAnalogChannelInfo) channel).getCentralFreq()) / 1000000;
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel == null");
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyCode:" + keyCode + "originalMHZ:" + originalMHZ);
    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
      originalMHZ = originalMHZ + mFineTuneStep;
      if (originalMHZ >= mCenterFrequency + mFineTuneMax) {
        originalMHZ = mCenterFrequency + mFineTuneMax;
      }
    } else {
      originalMHZ = originalMHZ - mFineTuneStep;
      if (originalMHZ < mCenterFrequency - mFineTuneMax) {
        originalMHZ = mCenterFrequency - mFineTuneMax;
      }
    }
    // add chunyan interface
    mOriginalFrequency = originalMHZ;
    if (CommonIntegration.getInstance().getCurrentFocus().equalsIgnoreCase("sub")) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " SUB TV");
      appTV.setFinetuneFreq("sub", (int) (originalMHZ * 1000000), false);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " MAIN TV");
      // the last param false mean exist fine tune page or not
      appTV.setFinetuneFreq("main", (int) (originalMHZ * 1000000), false);
    }
    // just tuneing so set isFineTuneDone =false
    isFineTuneDone = false;
    return originalMHZ;
  }

  public void saveFineTuneInfo() {
    sv.saveStrValue("FineTune_RestoreHz", "" + mRestoreHz);
    sv.saveStrValue("FineTune_IsFineTuneDone", "" + isFineTuneDone);
  }

  public void cleanChannelListForAdviseScan(){
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cleanChannelListForAdviseScan");
    MtkTvChannelListBase.cleanChannelList(1, false);
    MtkTvConfig.getInstance().setConfigValue(
            MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
  }
  /**
   * clean channel list
   */
  public void cleanChannelList() {
    int i = mCommonInter.getSvl();
    if (CommonIntegration.isCNRegion()) {
      int brdType = 7;
      if (i == 1) {
        brdType = 7;// DTMB
      } else {
        if (InputSourceManager.getInstance().isCurrentATvSource(
            mCommonInter.getCurrentFocus())) {
          brdType = 1;// ATV
        } else if (InputSourceManager.getInstance().isCurrentDTvSource(
            mCommonInter.getCurrentFocus())) {
          brdType = 2;// DVB
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "brdType:" + brdType);
      MtkTvChannelListBase mtkchLst = new MtkTvChannelListBase();
      //!!!!!!!!NOTE::below code is needed,but need add the api(unblockSvc) first!!!!!!!!!!!!!
      mtkchLst.deleteChannelByBrdcstType(i, brdType);
    } else if(TvSingletons.getSingletons().getCommonIntegration().isEUPARegion()){
    	if (i == 1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Svl == 1,Dvbt cleanChannelList");
            MtkTvChannelListBase.cleanChannelList(i);
        }else {
    	   int brdType = 1;
    	   String currFocus= mCommonInter.getCurrentFocus();
    	    if (InputSourceManager.getInstance().isCurrentATvSource(currFocus)) {
              brdType = 1;// ATV
    	    } else if (InputSourceManager.getInstance().isCurrentDTvSource(currFocus)) {
              brdType = 2;// DVB
    	      }
    	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSvl:" +i+"isEUPARegion brdType"+ brdType);
    	    MtkTvChannelListBase mtkchLst = new MtkTvChannelListBase();
    	    mtkchLst.deleteChannelByBrdcstType(i, brdType, true);
        }
    } else {
      if (i == 3 || i == 4 || i == 7) {
        MtkTvChannelListBase.cleanChannelList(i, false);
      } else {
        MtkTvChannelListBase.cleanChannelList(i);
      }
    }
    mCommonInter.clearTypeLastChannel();
    resetChannelListDialog();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetChannelListDialog finish");
    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
  }

  public void resetChannelListDialog(){
    NavBasicDialog dialog = (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
    if (dialog instanceof ChannelListDialog){
      ChannelListDialog channelListDialog =  (ChannelListDialog)dialog;
      channelListDialog.resetVariable();
    }else{
      SaveValue.getInstance(mContext).saveValue(CommonIntegration.CH_TYPE_BASE+CommonIntegration.getInstance().getSvl(),0);
      SaveValue.getInstance(mContext).saveValue(CommonIntegration.CH_TYPE_SECOND,-1);
    }
  }
  // /**
  // * Process KEYCODE_DPAD_DOWN/KEYCODE_DPAD_UP when focus is on channel list
  // *
  // * @param keyCode
  // */
  // public void channelUpAndDown(int keyCode) {
  // // ITVCommon tv = TVCommonNative.getDefault(mContext);
  // // if(null != tv){
  // // if (keyCode == KeyMap.KEYCODE_DPAD_DOWN) {
  // // try {
  // // tv.selectUp();
  // // } catch (RemoteException e) {
  // // e.printStackTrace();
  // // }
  // // // mChannelSelector.channelUp();
  // // } else {
  // // try {
  // // tv.selectDown();
  // // } catch (RemoteException e) {
  // // e.printStackTrace();
  // // }
  // // // mChannelSelector.channelDown();
  // // }
  // // }
  // // mChannelSelector.select(nextChannel);
  // }
  //
  // public void selectChannel(short index) {
  // // mChannelSelector.select(index);
  // }
  //
  // public void selectChannel(MtkTvChannelInfo ch) {
  // // mChannelSelector.select(ch);
  // }

  public boolean setPowerOnChannel(int channelid) {
    String cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
    if (mTVContent.getCurrentTunerMode() == 0) {
      cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
    } else {
      cfgId = MenuConfigManager.POWER_ON_CH_CABLE_MODE;
    }
    if (mTVContent.getConfigValue(cfgId) <= 0) {
      powerOnChannelNum = channelid;
      mTVContent.setConfigValue(cfgId, channelid);
      return true;
    } else {
      if (powerOnChannelNum == channelid) {
        BigInteger bigInteger = new BigInteger("ffffffff", 16);
        mTVContent.setConfigValue(cfgId, bigInteger.intValue());
        return false;
      } else {
        powerOnChannelNum = channelid;
        mTVContent.setConfigValue(cfgId, channelid);
        return true;
      }
    }
  }

  public void disablePowerOnChannel(){
      String cfgId;
      if (mTVContent.getCurrentTunerMode() == 0) {
          cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
        } else {
          cfgId = MenuConfigManager.POWER_ON_CH_CABLE_MODE;
        }
      BigInteger bigInteger = new BigInteger("ffffffff", 16);
      mTVContent.setConfigValue(cfgId, bigInteger.intValue());
  }

  public void blockChannel(MtkTvChannelInfoBase selChannel, boolean blocked) {
	  if(selChannel==null){
		  com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "selChannel can not equals null!");
		  return;
	  }
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " channel block previous state selChannel != null: " + blocked);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      blockChannel(list,blocked);
    if (blocked && ScanContent.isDVBSForTivusatOp()) {
      MtkTvChannelList.getInstance().channellistResetTmpUnlock(mCommonInter.getSvl(), selChannel.getSvlRecId(), false, false);
    }

  }

  public void blockAllChannels(List<MtkTvChannelInfoBase> list, boolean blocked){
      if(list==null ||list.isEmpty()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "list is null or empty!");
          return;
      }
      MtkTvChannelInfoBase selChannel=null;
      MtkTvChannelInfoBase currentChannel = mCommonInter.getCurChInfo();
      for(MtkTvChannelInfoBase channel:list){
          channel.setBlock(blocked);
          if(currentChannel != null && currentChannel.equals(channel)){
              selChannel=channel;
          }
      }
      mCommonInter.setBlockAll(list, blocked);
      if (selChannel==null || mCommonInter.is3rdTVSource()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "selChannel==null!");
          return;
      }
      if (StateDvrPlayback.getInstance() != null
              && StateDvrPlayback.getInstance().isRunning()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Current PVR playing.don't need to re-tune channel");
      }else{
          mCommonInter.selectChannelByInfo(selChannel);
          appTV.unblockSvc(mCommonInter.getCurrentFocus(), false);
      }
    if (blocked && ScanContent.isDVBSForTivusatOp()) {
      MtkTvChannelList.getInstance().channellistResetTmpUnlock(mCommonInter.getSvl(), selChannel.getSvlRecId(), true, false);
    }
  }

  public void blockChannel(List<MtkTvChannelInfoBase> list,boolean blocked) {
	  if(list==null ||list.isEmpty()){
		  com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "list is null or empty!");
		  return;
	  }
	  MtkTvChannelInfoBase selChannel=null;
	  MtkTvChannelInfoBase currentChannel = mCommonInter.getCurChInfo();
	  for(MtkTvChannelInfoBase channel:list){
		  channel.setBlock(blocked);
		  if(currentChannel != null && currentChannel.equals(channel)){
			  selChannel=channel;
		  }
	  }
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
      if (selChannel==null || mCommonInter.is3rdTVSource()) {
    	  com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "selChannel==null!");
		  return;
	  }
      if (StateDvrPlayback.getInstance() != null
              && StateDvrPlayback.getInstance().isRunning()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Current PVR playing.don't need to re-tune channel");
      }else{
        if (LiveTvSetting.isBootFromLiveTV()) {
          mCommonInter.selectChannelByInfo(selChannel);
          if (TurnkeyUiMainActivity.getInstance() != null && TurnkeyUiMainActivity.getInstance().getTvView() != null) {
            TurnkeyUiMainActivity.getInstance().getTvView().post(() -> {
              com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "tune current channel, no notify of service changed, reset flag.");
              CommonIntegration.getInstance().setCHChanging(false);
            });
          }
          appTV.unblockSvc(mCommonInter.getCurrentFocus(), false);
        }
    }
    if (blocked && ScanContent.isDVBSForTivusatOp()) {
      MtkTvChannelList.getInstance().channellistResetTmpUnlock(mCommonInter.getSvl(), selChannel.getSvlRecId(), true, false);
    }

  }





  /**
   * block selected channel
   *
   * @param chId the selected channel num
   * @param blocked true-block selected channel false-unblock
   */
    public void blockChannel(int chId, boolean blocked) {
        MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
        if (null != selChannel) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " channel block previous state selChannel != null: " + blocked);
            selChannel.setBlock(blocked);
            List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
            list.add(selChannel);
            mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
            //EURegion need call unblockSvc,but USRegion no need.
//            if(CommonIntegration.isEURegion()){
	            MtkTvChannelInfoBase currentChannel2 = mCommonInter.getCurChInfo();
	            if (currentChannel2 != null && currentChannel2.equals(selChannel)) {
	               mCommonInter.selectChannelByInfo(selChannel);
	              appTV.unblockSvc(mCommonInter.getCurrentFocus(), false);
	            }
//            }
//            MtkTvChannelInfoBase currentChannel = mCommonInter.getCurChInfoByTIF();
//            if (currentChannel != null && currentChannel.equals(selChannel)) {
//                Message msg = mHandler.obtainMessage();
//                msg.what = SELECTCHANNEL;
//                msg.obj = selChannel;
//                mHandler.sendMessageDelayed(msg, 500);
//            }
      }
    }

  @SuppressWarnings("unused")
  public boolean isChannelBlock(int chId) {
    boolean channelBlock = false;
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      channelBlock = selChannel.isBlock();// selChannel.isPhysicalBlocked();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelBlock:" + channelBlock);
    }
    return channelBlock;
  }

  public boolean isChannelBlockForRecord(int svlId, int svlRecId) {
    boolean channelBlock = false;
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelByRecId(svlId, svlRecId);
    if (null != selChannel) {
      channelBlock = selChannel.isBlock();// selChannel.isPhysicalBlocked();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelBlock:" + channelBlock);
    }
    return channelBlock;
  }

  public boolean isChannelSkip(int chId) {
    boolean channelSkip = false;
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      channelSkip = selChannel.isSkip();// selChannel.isPhysicalBlocked();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSkip:" + channelSkip);
    return channelSkip;
  }

  public boolean isChannelDecode(int chId) {
    boolean channelDecode = false;
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      if ((selChannel.getNwMask() & MtkTvChCommonBase.SB_VNET_USE_DECODER)
          == MtkTvChCommonBase.SB_VNET_USE_DECODER) {
        channelDecode = true;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, channelDecode + "isChannelDecode");
    }
    return channelDecode;
  }

  public void channelSort(int chNumSrc, int chNumDst) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort:src:" + chNumSrc + ",dst:" + chNumDst);
    MtkTvChannelInfoBase selChannelSrc = mCommonInter.getChannelById(chNumSrc);
    MtkTvChannelInfoBase selChannelDst = mCommonInter.getChannelById(chNumDst);
    if (null != selChannelSrc && null != selChannelDst) {
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      selChannelSrc.setOptionMask(0x400 | selChannelSrc.getOptionMask());
      selChannelDst.setOptionMask(0x400 | selChannelDst.getOptionMask());
      int firstchannelid = 0;
      int secondchannelid = 0;
      int currentId = mCommonInter.getCurrentChannelId();
      if (selChannelDst instanceof MtkTvAnalogChannelInfo) {
        firstchannelid = getNewChannelIdAna(chNumSrc, chNumDst);
      } else {
        firstchannelid = getNewChannelId(chNumSrc, chNumDst);
      }
      if (selChannelSrc instanceof MtkTvAnalogChannelInfo) {
        secondchannelid = getNewChannelIdAna(chNumDst, chNumSrc);
      } else {
        secondchannelid = getNewChannelId(chNumDst, chNumSrc);
      }
      selChannelSrc.setChannelId(firstchannelid);
      selChannelDst.setChannelId(secondchannelid);
      list.add(selChannelSrc);
      list.add(selChannelDst);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort:curr:" + currentId + ",fst:" + firstchannelid + ",sec:"
          + secondchannelid);
//      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
      mCommonInter.setChannelSwap(list, firstchannelid, secondchannelid);
      if (chNumSrc == currentId) {
        mCommonInter.setCurrentChannelId(firstchannelid);
      } else if (chNumDst == currentId) {
        mCommonInter.setCurrentChannelId(secondchannelid);
      }
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort channel == null:");
    }
  }

  public void channelMoveForfusion(int chNumSrc, int chNumDst) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort:src:" + chNumSrc + ",dst:" + chNumDst);
      MtkTvChannelInfoBase selChannelSrc = mCommonInter.getChannelById(chNumSrc);
      MtkTvChannelInfoBase selChannelDst = mCommonInter.getChannelById(chNumDst);



      if (null != selChannelSrc && null != selChannelDst) {

        mCommonInter.setChannelListForfusion(chNumSrc, chNumDst);
        /*List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
        selChannelSrc.setOptionMask(0x400 | selChannelSrc.getOptionMask());
        selChannelDst.setOptionMask(0x400 | selChannelDst.getOptionMask());
        int firstchannelid = 0;
        int secondchannelid = 0;
        int currentId = mCommonInter.getCurrentChannelId();
        if (selChannelDst instanceof MtkTvAnalogChannelInfo) {
          firstchannelid = getNewChannelIdAna(chNumSrc, chNumDst);
        } else {
          firstchannelid = getNewChannelId(chNumSrc, chNumDst);
        }
        if (selChannelSrc instanceof MtkTvAnalogChannelInfo) {
          secondchannelid = getNewChannelIdAna(chNumDst, chNumSrc);
        } else {
          secondchannelid = getNewChannelId(chNumDst, chNumSrc);
        }
        selChannelSrc.setChannelId(firstchannelid);
        selChannelDst.setChannelId(secondchannelid);
        list.add(selChannelSrc);
        list.add(selChannelDst);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort:curr:" + currentId + ",fst:" + firstchannelid + ",sec:"
            + secondchannelid);
        if (chNumSrc == currentId) {
          mCommonInter.setCurrentChannelId(firstchannelid);
        } else if (chNumDst == currentId) {
          mCommonInter.setCurrentChannelId(secondchannelid);
        }
        mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);*/

      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort channel == null:");
      }
    }

  public int getNewChannelId(int firstsrc, int secDes) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelSort:getNewId firstsrc:" + firstsrc + ",secDes=" + secDes);
    int newId = 0;
    int index = firstsrc & 0xf;
    int major = (secDes >> 18) & 0x3fff;
    // com.mediatek.wwtv.tvcenter.util.MtkLog.i("wangjinben", "index:"+index+",major="+major);
//    int x = (major & 0x3fff) << 18;
    newId = ((major & 0x3fff) << 18) | (index & 0xf) | 0x80;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelSort:getNewId:" + newId);
    return newId;
  }

  public int getNewChannelIdAna(int firstsrc, int secDes) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelSort:getNewId firstsrc:" + firstsrc + ",secDes=" + secDes);
    int newId = 0;
    // int index = firstsrc & 0xf;
    int index = 0;
    int major = (secDes >> 18) & 0x3fff;
    // com.mediatek.wwtv.tvcenter.util.MtkLog.i("wangjinben", "index:"+index+",major="+major);
//    int x = (major & 0x3fff) << 18;
    newId = ((major & 0x3fff) << 18) | (index & 0x3f) | 0x80;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelSort:getNewIdAna:" + newId);
    return newId;
  }
  public void setChannelSkip(int chId, boolean skip) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSkip:" + skip);
      selChannel.setSkip(skip);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelSkip selChannel is null");
    }
  }

  public void setChannelDecode(int chId, boolean decode) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setChannelDecode:" + decode);
      int nwMask = selChannel.getNwMask();
      if (decode) {
        nwMask |= MtkTvChCommonBase.SB_VNET_USE_DECODER;
      } else {
        nwMask &= ~(MtkTvChCommonBase.SB_VNET_USE_DECODER);
      }
      selChannel.setNwMask(nwMask);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelDecode selChannel is null");
    }
  }

  public int getSchBlockType(int chId) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    return getSchBlockType(selChannel);
  }

  public int getSchBlockType(MtkTvChannelInfoBase selChannel) {
    if (selChannel instanceof MtkTvAnalogChannelInfo) {
      int type = ((MtkTvAnalogChannelInfo) selChannel).getSchBlkType();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSchBlkType MtkTvAnalogChannelInfo:" + type);
      return type;
    } else if (selChannel instanceof MtkTvISDBChannelInfo) {
      int type = ((MtkTvISDBChannelInfo) selChannel).getSchBlkType();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSchBlkType MtkTvISDBChannelInfo:" + type);
      return type;
    }
    return 0;
  }

  private int getSchBlockFromUTCTime(MtkTvChannelInfoBase selChannel) {
    int time = 0;
    if (selChannel instanceof MtkTvAnalogChannelInfo) {
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkEnable(1);
      time = ((MtkTvAnalogChannelInfo) selChannel).getSchBlkFromTime();
    } else if (selChannel instanceof MtkTvISDBChannelInfo) {
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkEnable(1);
      time = ((MtkTvISDBChannelInfo) selChannel).getSchBlkFromTime();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSchBlockFromUTCTime selChannel id:" + selChannel.getChannelId() + "time:"
        + time);
    return time;
  }

  public String getFromDate(MtkTvChannelInfoBase selChannel) {
    int mill = getSchBlockFromUTCTime(selChannel);
    MtkTvTimeFormat timeFormat = MtkTvTimeFormat.getInstance();
    timeFormat.set(mill);
    return dateFormat(timeFormat.year, timeFormat.month + 1, timeFormat.monthDay);
  }

  public String getFromTime(MtkTvChannelInfoBase selChannel) {
    int mill = getSchBlockFromUTCTime(selChannel);
    MtkTvTimeFormat timeFormat = MtkTvTimeFormat.getInstance();
    timeFormat.set(mill);
    return timeFormat(timeFormat.hour, timeFormat.minute);
  }

  public String getToDate(MtkTvChannelInfoBase selChannel) {
    int mill = getSchBlockTOUTCTime(selChannel);
    MtkTvTimeFormat timeFormat = MtkTvTimeFormat.getInstance();
    timeFormat.set(mill);
    return dateFormat(timeFormat.year, timeFormat.month + 1, timeFormat.monthDay);
  }

  public String getToTime(MtkTvChannelInfoBase selChannel) {
    int mill = getSchBlockTOUTCTime(selChannel);
    MtkTvTimeFormat timeFormat = MtkTvTimeFormat.getInstance();
    timeFormat.set(mill);
    return timeFormat(timeFormat.hour, timeFormat.minute);
  }

  private String dateFormat(int year, int month, int monthday) {
    String dateString = year + "/";
    if (month < 10 && month > 0) {
      dateString = dateString + "0" + month;
    } else if (month == 0) {
      dateString = dateString + "01";
    } else {
      dateString = dateString + month;
    }
    if (monthday < 10) {
      dateString = dateString + "/0" + monthday;
    } else {
      dateString = dateString + "/" + monthday;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dateFormat dateString:" + dateString);
    return dateString;
  }

  private String timeFormat(int hour, int minute) {
    String timeString = "" + hour;
    if (hour < 10) {
      timeString = "0" + hour;
    }
    if (minute < 10) {
      timeString = timeString + ":0" + minute;
    } else {
      timeString = timeString + ":" + minute;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeFormat timeString:" + timeString);
    return timeString;
  }

  public int getSchBlockTOUTCTime(MtkTvChannelInfoBase selChannel) {
    int time = 0;
    if (selChannel instanceof MtkTvAnalogChannelInfo) {
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkEnable(1);
      time = ((MtkTvAnalogChannelInfo) selChannel).getSchBlkToTime();
    } else if (selChannel instanceof MtkTvISDBChannelInfo) {
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkEnable(1);
      time = ((MtkTvISDBChannelInfo) selChannel).getSchBlkToTime();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSchBlockToUTCTime selChannel id:" + selChannel.getChannelId() + "time:"
        + time);
    return time;
  }

  public int setSchBlockFromUTCTime(int chId, long mill) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSchBlockFromUTCTime chId:" + chId + "mill:" + mill);
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (selChannel instanceof MtkTvAnalogChannelInfo) {
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkEnable(1);
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkFromTime((int) mill);
    } else if (selChannel instanceof MtkTvISDBChannelInfo) {
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkEnable(1);
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkFromTime((int) mill);
    }
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    list.add(selChannel);
    mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    return 0;
  }

  public int setSchBlockTOUTCTime(int chId, long mill) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSchBlockTOUTCTime chId:" + chId + "mill:" + mill);
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (selChannel instanceof MtkTvAnalogChannelInfo) {
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkEnable(1);
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkToTime((int) mill);
    } else if (selChannel instanceof MtkTvISDBChannelInfo) {
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkEnable(1);
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkToTime((int) mill);
    }
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    list.add(selChannel);
    mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    return 0;
  }

  public int setSchBlock(int chId, long from, long end, int type) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSchBlockTime chId:" + chId + "from:" + from + "end:" + end);
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (selChannel instanceof MtkTvAnalogChannelInfo) {
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkEnable(1);
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkType(type);
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkFromTime((int) from);
      ((MtkTvAnalogChannelInfo) selChannel).setSchBlkToTime((int) end);
    } else if (selChannel instanceof MtkTvISDBChannelInfo) {
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkEnable(1);
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkType(type);
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkFromTime((int) from);
      ((MtkTvISDBChannelInfo) selChannel).setSchBlkToTime((int) end);
    }
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    list.add(selChannel);
    mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    return 0;
  }

  public void setChannelNumber(int chId, int number) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "number:" + number + "  selChannel:" + selChannel);
      selChannel.setChannelNumber(number);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "number:" + number + "  selChannel:" + selChannel);
      // int currentid = chId;
      // chId = chId & 0xf;
      // currentid = ((number&0x3fff)<<18)|((chId&0xf))|0x80;
      // com.mediatek.wwtv.tvcenter.util.MtkLog.i("channelsort", "currentid:"+currentid);
      // selChannel.setChannelId(currentid);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelNumber selChannel is null");
    }
  }

  public void setChannelNumber(int chId, int newId, int number) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "number:" + number + "  selChannel:" + selChannel);
      selChannel.setChannelNumber(number);
      selChannel.setChannelId(newId);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "number:" + number + "  selChannel:" + selChannel);
      // int currentid = chId;
      // chId = chId & 0xf;
      // currentid = ((number&0x3fff)<<18)|((chId&0xf))|0x80;
      // com.mediatek.wwtv.tvcenter.util.MtkLog.i("channelsort", "currentid:"+currentid);
      // selChannel.setChannelId(currentid);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelNumber selChannel is null");
    }
  }
  public void setChannelName(int chId, String name) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "name:" + name + "  selChannel:" + selChannel);
    if (null != selChannel) {
      selChannel.setServiceName(name);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelName selChannel is null");
    }
  }

  public void setChannelFreq(int chId, String freq) {
    MtkTvChannelInfoBase selChannel = mCommonInter.getChannelById(chId);
    if (null != selChannel) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "freq:" + freq);
      // fix CR DTV00583025
      // freq = freq.replace(".", "");
      selChannel.setFrequency((int) (Float.parseFloat(freq) * 1000000));
/*      if (selChannel instanceof MtkTvAnalogChannelInfo) {
        // ((MtkTvAnalogChannelInfo)selChannel).setCentralFreq((int)(Float.parseFloat(freq)*
        // 1000000));
      }*/
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
      if (chId == getCurrentChannelId()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "before stop:" + freq);
        mCommonInter.stopMainOrSubTv();
        selectChannel(getCurrentChannelId());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " after select:" + freq);
      }
      // setChannelAfterFreq();
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelName selChannel is null");
    }
  }

  public void setOpenVCHIP(int regionIndex, int dimIndex, int levIndex) {
    mTVContent.setOpenVChipSetting(regionIndex, dimIndex, levIndex);
  }

  public String getCurrentInput() {
    return "";// mInputManager.getCurrInputSource("main");
  }

  // ADD Start
  // public boolean isCurrentSourceUserBlocked() {
  // String ins = getCurrentInput();
  // if (ins != null) {
  // TVInput in = mInputManager.getInput(ins);
  // if (in != null) {
  // return in.isUsrUnblocked();
  // }
  // }
  //
  // return false;
  // }

  public boolean isCurrentSourceTv() {
    // String ins = mInputManager.getTypeFromInputSource(getCurrentInput());
    return mTVContent.isCurrentSourceTv();// TVInputManager.INPUT_TYPE_TV.equals(ins);
  }

  public boolean isCurrentSourceBlocking() {
    return mTVContent.isCurrentSourceBlocking();
  }

  public boolean isTvInputBlock() {
    return mTVContent.isTvInputBlock();
  }

  // ADD End
  public void setTVInput(String output) {
    // String[] inputSources = mInputManager.getInputSourceArray();
    // if (null != inputSources && inputSources.length > 0) {
    // String input = inputSources[0];
    // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Input: " + input + "   Output: " + output);
    // mInputManager.changeInputSource(output, input);
    // }
  }

  public void resetParental(final Context context, final Runnable runnable) {
    new Thread(new Runnable() {

      @Override
      public void run() {
        sv.saveStrValue("password", "1234");
        Handler handler = new Handler(context.getMainLooper());
        handler.post(runnable);
      }

    }).start();
  }

  public void setChannelAfterFreq() {

    Handler handler = new Handler(mContext.getMainLooper());
    handler.postDelayed(new Runnable() {

      @Override
      public void run() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "biaoqing getCurrentChannelId:" + getCurrentChannelId());
        mCommonInter.stopMainOrSubTv();
        selectChannel(getCurrentChannelId());
      }

    }, 2000);
  }

  public int getBlockChannelNum() {
	  return mCommonInter.getBlockChannelNum();
  }

  public int getBlockChannelNumForSource() {
      return mCommonInter.getBlockChannelNumForSource();
  }

  public List<MtkTvChannelInfoBase> getChannelList() {
    if (CommonIntegration.isSARegion()) {
      int length = mCommonInter.getChannelAllNumByAPI();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList length " + length);
      return mCommonInter.getChList(0, 0, length);
    } else {
      int length = mCommonInter.getChannelAllNumByAPI();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList length " + length);
      return mCommonInter.getChannelList(0, 0, length, MtkTvChCommonBase.SB_VNET_ALL);
    }
  }

  public boolean isHasAnalog() {
    List<MtkTvChannelInfoBase> chList = getChannelList();
    boolean hasAnalog = false;
    if (chList != null) {
      for(MtkTvChannelInfoBase mtkTvChannelInfoBase:chList){
        if (mTVContent.isAnalog(mtkTvChannelInfoBase)) {
          hasAnalog = true;
          break;
        }
      }
    }
    return hasAnalog;
  }

  // public void flush() {
  // // mTVManager.flush();
  // }

  public int getSignalLevel() {
    if (mTVContent != null) {
      int level = mTVContent.getSignalLevel();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSignalLevel: " + level);
      if (level < 0) {
        level = 0;
      } else if (level > 100) {
        level = 100;
      }
      return level;
    }
    return 0;
  }

  public int getSignalQuality() {
    int ber = 0;
    if (mTVContent != null) {
      if ((!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK) && CommonIntegration.isEURegion()) ||
              CommonIntegration.isUSRegion()) {
        ber = mTVContent.getSignalQuality();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "eu  or us signal quality --ber: " + ber);
        if (ber > 100) {
          ber = 100;
        }
        return ber;
      } else if (CommonIntegration.isSARegion() || CommonIntegration.isCNRegion() || MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK)) {
        String focus = CommonIntegration.getInstance().getCurrentFocus();
        int bervalue = appTV.GetSignalBER(focus);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sa  or cn signal quality --bervalue: " + bervalue);
        int quality = -1;
        // for CN ber is always 0
        if (bervalue >= 0 && bervalue <= 20) {
          quality = 2;
        } else if (bervalue > 20 && bervalue <= 380) {
          quality = 1;
        } else {
          quality = 0;
        }
        return quality;
      }

    }
    return 0;
  }

//  public void singleLevel(MtkTvChannelInfoBase selChannel) {
//    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "singleLevel enter");
//    int turn = mTVContent.getCurrentTunerMode();
//    int conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
//    int freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
//    int tunerMod = SaveValue.getInstance(mContext).readValue(
//        MenuConfigManager.TV_SINGLE_SCAN_MODULATION);// MtkTvFreqChgCommonBase.MOD_QAM_64 ;
//    if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_AIR) {
//      conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
//      if (mTVContent.isAnalog(selChannel)) {
//        freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_ANA_TER;
//      } else {
//        freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_TER;
//      }
//      if (mTVContent.isSARegion()) {
//        tunerMod = MtkTvFreqChgCommonBase.MOD_VSB_8;
//      }
//    } else if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_CABLE) {
//      conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_CAB;
//      if (mTVContent.isAnalog(selChannel)) {
//        freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_ANA_CAB;
//      } else {
//        freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
//      }
//    }
//    int symRate = 0;
//    int frequency = 123;
//    if (selChannel != null) {
//      frequency = selChannel.getFrequency();
//    } else {
//      // return;
//    }
//
//    MtkTvFreqChgParamBase freqInfo = new MtkTvFreqChgParamBase(conType, freqType, frequency,
//        tunerMod, symRate);
//    MtkTvAppTVBase apptv = new MtkTvAppTVBase();
//    apptv.changeFreq(mCommonInter.getCurrentFocus(), freqInfo);
//    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "singleLevel leave");
//
//  }

  public void tuneDVBTRFSignal() {
    // MtkTvScanDvbtBase.RfInfo rfInfo =
    // MtkTvScan.getInstance().getScanDvbtInstance().gotoDestinationRf(RfDirection.CURRENT);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "tuneSignal enter>>" + DVBTScanner.selectedRFChannelFreq + ">>>"
        + DVBTCNScanner.selectedRFChannelFreq);
    int turn = mTVContent.getCurrentTunerMode();
    int conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
    int freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
    int tunerMod = MtkTvFreqChgCommonBase.MOD_UNKNOWN;
    // int tunerMod =
    // SaveValue.getInstance(mContext)
    //.readValue(MenuConfigManager.TV_SINGLE_SCAN_MODULATION);//MtkTvFreqChgCommonBase.MOD_QAM_64
    // ;
    if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_AIR) {
      conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
      // if (mTVContent.isAnalog(selChannel)) {
      // freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_ANA_TER;
      // }else {
      freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_TER;
      // }
    } else if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_CABLE) {
      conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_CAB;
      // if (mTVContent.isAnalog(selChannel)) {
      // freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_ANA_CAB;
      // }else {
      freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
      // }
    }
    int symRate = 0;
    int frequency = DVBTScanner.selectedRFChannelFreq;
    if (CommonIntegration.isCNRegion()) {
      frequency = DVBTCNScanner.selectedRFChannelFreq;
    }
    MtkTvFreqChgParamBase freqInfo = new MtkTvFreqChgParamBase(conType, freqType, frequency,
        tunerMod, symRate);
    //MtkTvAppTVBase apptv = new MtkTvAppTVBase();
    MtkTvBroadcast.getInstance().changeFreq(mCommonInter.getCurrentFocus(), freqInfo);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "tuneSignal leave");
  }

  public void tuneDVBCRFSignal(int freq) {
      // MtkTvScanDvbtBase.RfInfo rfInfo =
      // MtkTvScan.getInstance().getScanDvbtInstance().gotoDestinationRf(RfDirection.CURRENT);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("EditChannel", "tuneDVBCRFSignal enter>>"+freq);
      int turn = mTVContent.getCurrentTunerMode();
      int conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
      int freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
      int tunerMod = MtkTvFreqChgCommonBase.MOD_UNKNOWN;
      // int tunerMod =
      // SaveValue.getInstance(mContext)
      //.readValue(MenuConfigManager.TV_SINGLE_SCAN_MODULATION);//MtkTvFreqChgCommonBase.MOD_QAM_64
      // ;
      if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_AIR) {
        conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
        // if (mTVContent.isAnalog(selChannel)) {
        // freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_ANA_TER;
        // }else {
        freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_TER;
        // }
      } else if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_CABLE) {
        conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_CAB;
        // if (mTVContent.isAnalog(selChannel)) {
        // freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_ANA_CAB;
        // }else {
        freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
        // }
      }
      int symRate = 0;

      MtkTvFreqChgParamBase freqInfo = new MtkTvFreqChgParamBase(conType, freqType, freq,
          tunerMod, symRate);
      //MtkTvAppTVBase apptv = new MtkTvAppTVBase();
      MtkTvBroadcast.getInstance().changeFreq(mCommonInter.getCurrentFocus(), freqInfo);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("EditChannel", "tuneSignal leave");
    }

  /**
   * tune us or sa when fac single-rf scan
   */
  public void tuneUSSAFacRFSignalLevel(int frqchannel) {
    int turn = mTVContent.getCurrentTunerMode();
    int modLation =MtkTvFreqChgCommonBase.MOD_UNKNOWN;
    int symRate = 0;
    int frequency = frqchannel;
    if (CommonIntegration.isUSRegion()) {
      if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_AIR) {
        frequency = mTVContent.calcATSCFreq(frqchannel);
      } else {
        frequency = mTVContent.calcCQAMFreq(frqchannel);
      }
    } else if (CommonIntegration.isSARegion()) {
      frequency = mTVContent.calcSAFreq(frqchannel);
    }
    int conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
    int freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
  //  int tunerMod = MtkTvFreqChgCommonBase.MOD_UNKNOWN;
    if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_AIR) {
      conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_TRSTRL;
      freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_TER;
      modLation = MtkTvFreqChgCommonBase.MOD_VSB_8;
    } else if (turn == MtkTvConfigTypeBase.ACFG_BS_SRC_CABLE) {
      conType = MtkTvFreqChgCommonBase.CON_SRC_TYPE_CAB;
      freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_CAB;
      if (CommonIntegration.isUSRegion()) {
        int modMask = mTVContent.getModulation();
        if (modMask == 0) {
          modLation = MtkTvFreqChgCommonBase.MOD_QAM_64;
          symRate = 5056941;
        } else {
          modLation = MtkTvFreqChgCommonBase.MOD_QAM_256;
          symRate = 5360537;
        }
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "tuneSignal leave conType:" + conType + "   freqType:" + freqType
        + "  frequency:" + frequency + "  modLation:" + modLation + "  symRate:" + symRate);
    MtkTvFreqChgParamBase freqInfo = new MtkTvFreqChgParamBase(conType, freqType, frequency,
        modLation, symRate);
    //MtkTvAppTVBase apptv = new MtkTvAppTVBase();
    MtkTvBroadcast.getInstance().changeFreq(mCommonInter.getCurrentFocus(), freqInfo);
  }

  public void resetDefAfterClean() {
    SaveValue saveV = SaveValue.getInstance(mContext);

    mcf.setScanValue(MenuConfigManager.COLOR_SYSTEM, 0);
    mcf.setScanValue(MenuConfigManager.TV_SYSTEM, 0);

    // TVConfigurer tvcfg = mTVContent.getConfigurer();
    // tvcfg.resetUser();
    // setTVInput("main");
    InputSourceManager.getInstance().resetDefault();
    // mTVContent.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF, 0,
    // saveV.readStrValue(MenuConfigManager.TIMER2));

    saveV.saveValue(MenuConfigManager.CAPTURE_LOGO_SELECT, 0);
    saveV.saveValue(MenuConfigManager.AUTO_SLEEP, 0);
    saveV.saveValue(MenuConfigManager.SLEEP_TIMER, 0);
    // MTKPowerManager powerManager = MTKPowerManager.getInstance(mContext);
    // powerManager.cancelPowOffTimer("timetosleep");

    // saveV.saveValue(MenuConfigManager.POWER_ON_TIMER, 0);
    // saveV.saveValue(MenuConfigManager.POWER_OFF_TIMER, 0);
    // saveV.saveValue(MenuConfigManager.AUTO_SYNC, 1);
 //   AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    // alarm.setTimeZone("Asia/Shanghai");
    saveV.saveBooleanValue("Zone_time", false);
    saveV.saveStrValue(MenuConfigManager.TIMER2, "00:00:00");
    saveV.saveStrValue(MenuConfigManager.TIMER1, "00:00:00");
    // saveV.saveValue(MenuConfigManager.NETWORK_CONNECTION, 0);
  }

}
