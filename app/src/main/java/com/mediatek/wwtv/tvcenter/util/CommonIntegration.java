
package com.mediatek.wwtv.tvcenter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.android.tv.parental.ContentRatingSystem;
import com.android.tv.parental.ContentRatingSystem.Rating;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.MtkTvCDTChLogoBase;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvHighLevel;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelQuery;
import com.mediatek.twoworlds.tv.model.MtkTvFavoritelistInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.NavCommonInfoBar;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.android.tv.util.Utils;
import com.mediatek.dm.MountPoint;
import com.mediatek.dm.DeviceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;
import android.media.tv.TvInputInfo;
import android.hardware.display.DisplayManager;
import android.view.Display;

public class CommonIntegration {

  private static final String TAG = "CommonIntegration";

  private static CommonIntegration instanceNavIntegration = null;
  public static final int ANALOG_CHANNEL_NUMBER_START = 1;
  //for customer special channel decrease 2000
  public final static int DECREASE_NUM = 0;
  private static MtkTvBroadcast chBroadCast = null;
  private static MtkTvUtil instanceMtkTvUtil = null;
  private static MtkTvHighLevel instanceMtkTvHighLevel;
  private final MtkTvCDTChLogoBase mMtkTvCDTChLogoBase = new MtkTvCDTChLogoBase();
  private final MtkTvAppTV mMtkTvAppTV;
  private final MtkTvDvbsConfigBase mMtkTvDvbsConfigBase;
  private final MtkTvScanDvbsBase mtkTvScanDvbsBase;
  private final MtkTvAppTVBase mMtkTvAppTVBase;

  public static final String TV_FOCUS_WIN_MAIN = "main";
  public static final String TV_FOCUS_WIN_SUB = "sub";

  public static final String SOURCE_ATV = "ATV";
  public static final String SOURCE_DTV = "DTV";
  public static final String SOURCE_TV = "TV";
  public static final String  channelListfortypeMask = "channelListfortype";
  public static final String  channelListfortypeMaskvalue = "channelListfortypeMaskvalue";
  public static  int COUNTCHLISTALL= 0;
  public static  int COUNTCHLISTACTIVE = 0;

  public static final int TTS_TIME = 90; //ms

  public static final int TV_NORMAL_MODE = 0;
  public static final int TV_PIP_MODE = 1;
  public static final int TV_POP_MODE = 2;

  public static final int SUPPORT_THIRD_PIP_MODE = 1;

  public static final int NOT_SUPPORT_THIRD_PIP_MODE = 0;

  public static final String ZOOM_CHANGE_BEFORE = MtkTvAppTV.SYS_BEFORE_ZOOM_MODE_CHG;
  public static final String ZOOM_CHANGE_AFTER = MtkTvAppTV.SYS_AFTER_ZOOM_MODE_CHG;
  public static final String SCREEN_MODE_CHANGE_BEFORE = MtkTvAppTV.SYS_BEFORE_ASPECT_RATIO_CHG;
  public static final String SCREEN_MODE_CHANGE_AFTER = MtkTvAppTV.SYS_AFTER_ASPECT_RATIO_CHG;

public static final int CH_UP_DOWN_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_VISIBLE | MtkTvChCommonBase.SB_VNET_FAKE;
  public static final int CH_UP_DOWN_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_VISIBLE;
  public static final int CH_LIST_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_FAKE;
  public static final int CH_LIST_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE;
  public static final int CH_LIST_DIGITAL_RADIO_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
  public static final int CH_LIST_DIGITAL_RADIO_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE;

  public static final int CH_LIST_RADIO_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE;
  public static final int CH_LIST_RADIO_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE;
  public static final int CH_LIST_ANALOG_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
  public static final int CH_LIST_ANALOG_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
  public static final int CH_LIST_DIGITAL_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE
      | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE
      | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
  public static final int CH_LIST_DIGITAL_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE;

  public static final int CH_LIST_SCRAMBLED_MASK = MtkTvChCommonBase.SB_VNET_SCRAMBLED
          |MtkTvChCommonBase.SB_VNET_ACTIVE;
  public static final int CH_LIST_SCRAMBLED_VAL = MtkTvChCommonBase.SB_VNET_SCRAMBLED
          | MtkTvChCommonBase.SB_VNET_ACTIVE;
  public static final int CH_LIST_FREE_MASK = MtkTvChCommonBase.SB_VNET_SCRAMBLED
          | MtkTvChCommonBase.SB_VNET_ACTIVE;
  public static final int CH_LIST_FREE_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE;

  public static final int CH_LIST_FAV1_MASK = MtkTvChCommonBase.SB_VNET_FAVORITE1;
  public static final int CH_LIST_FAV1_VAL = MtkTvChCommonBase.SB_VNET_FAVORITE1;
  public static final int CH_FAKE_MASK =  MtkTvChCommonBase.SB_VNET_FAKE;
  public static final int CH_FAKE_VAL = MtkTvChCommonBase.SB_VNET_FAKE;

  public static final int CH_SDT_SERVICE_TYPE_HD = 0x19;
  public static final int CH_SDT_SERVICE_TYPE_INTERACTIVE = 0x0c;
  public static final int CH_SDT_SERVICE_TYPE_RADIO = 0x0a;
  public static final int CH_SDT_SERVICE_TYPE_RADIO2 = 0x02;
  public static final int SVL_SERVICE_TYPE_IP_SVC = MtkTvChCommonBase.SVL_SERVICE_TYPE_IP_SVC;
  public static final String CH_TYPE_BASE = "type_";
  public static final String CH_TYPE_SECOND = "typeSecond";
  public static final int CH_LIST_3RDCAHNNEL_MASK = -1;
  public static final int CH_LIST_3RDCAHNNEL_VAL = -1;
  public static final int SVCTX_NTFY_CODE_VIDEO_SIGNAL_LOCKED = 4; // video only
  public static final int SVCTX_NTFY_CODE_CHANNEL_CHANGE = 7; // channel change
  public static final int SVCTX_NTFY_CODE_VIDEO_ONLY_SVC = 20; // video only
  public static final int SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE = 37;// video update
  public static final int SVCTX_NTFY_CODE_BLOCK = 9;// input/rating/channel block
  public static final int SVCTX_NTFY_CODE_UNBLOCK = 12;// input/rating/channel unblock
  public static final int SVCTX_NTFY_CODE_RATING = 18;// rating ready
  public static final int SVCTX_NTFY_SIGNA_LOSS = 5;// signa loss
  private static MtkTvChannelList mtkTvChList = MtkTvChannelList.getInstance();

  private static MtkTvChannelInfoBase mPreChInfo;

  public static final int  M7BaseNumber = 4001;
  public static final String OCEANIA_POSTAL = MtkTvConfigType.CFG_EAS_LCT_CT;
  private static final String LAST_CHANNEL_ID = MtkTvConfigType.CFG_NAV_AIR_LAST_CH;
  private static final String CUR_CHANNEL_ID = MtkTvConfigType.CFG_NAV_AIR_CRNT_CH;
  private static final String TV_FOCUS_WIN = MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN;
  private static final String TV_BRDCST_TYPE = MtkTvConfigType.CFG_BS_BS_BRDCST_TYPE;
  private static final String LAST_2ND_CHANNEL_ID = MtkTvConfigType.CFG_MISC_2ND_LAST_CH_ID;
  private static final String CUR_2ND_CHANNEL_ID = MtkTvConfigTypeBase.CFG_MISC_2ND_CRNT_CH_ID;
  private static final String TV_DUAL_TUNER_ENABLE = MtkTvConfigType.CFG_MISC_2ND_CHANNEL_ENABLE;

  private static final String TV_MODE = MtkTvConfigType.CFG_PIP_POP_TV_MODE;

  private static final String SCREEN_MODE = MtkTvConfigType.CFG_VIDEO_SCREEN_MODE;

  public static final int SCREEN_MODE_NORMAL = 1;

  public static final int SCREEN_MODE_DOT_BY_DOT = 6;

  private static final String THIRD_PIP_MODE = MtkTvConfigType.CFG_PIP_POP_ANDROID_POP_MODE;
  /** svl id value, 1:T, 2:C, 3:General S, 4: Prefer S, 5:T(CI), 6:C(CI), 7:S(CI) */
  public static final int DB_AIR_SVLID = 1;
  public static final int DB_CAB_SVLID = 2;
  public static final int DB_SAT_SVLID = 3;
  public static final int DB_SAT_PRF_SVLID = 4;
  public static final int DB_CI_PLUS_SVLID_AIR = 5;
  public static final int DB_CI_PLUS_SVLID_CAB = 6;
  public static final int DB_CI_PLUS_SVLID_SAT = 7;
  /** tuner mode value, 0:T, 1:C, 2:S, 3:used by set general S in wizard and menu */
  public static final int DB_AIR_OPTID = 0;
  public static final int DB_CAB_OPTID = 1;
  public static final int DB_SAT_OPTID = 2;
  public static final int DB_GENERAL_SAT_OPTID = 3;// maybe used by set tuner mode, but should not
                                                   // be used by get tuner mode

  //
  public static final String camUpgrade = "camUpgrade";

  public static final int BRDCST_TYPE_ATV = 1;
  public static final int BRDCST_TYPE_DTV = 0;

  private boolean iCurrentInputSourceHasSignal;
  private boolean iCurrentTVHasSignal;
  private boolean isVideoScrambled;
  private boolean showFAVListFullToastDealy = false;
  private boolean stopTIFSetupWizardFunction = false;
  private boolean doPIPPOPAction = false;
  private int hasVGASource = -1;

  private static int mCurrentTvMode = 0;
  public static final int CATEGORIES_CHANNELNUM_BASE = 2000;
  private ChannelChangedListener mChListener;
  private ContentRatingSystem mContentRatingSystem;
  private NavCommonInfoBar mPopup;
  MtkTvConfig mMtkTvConfig;
  public static final String SAT_BRDCSTER = MtkTvConfigType.CFG_BS_BS_SAT_BRDCSTER;
  public static final int[] favMask = new int[] {
      MtkTvChCommonBase.SB_VNET_FAVORITE1, MtkTvChCommonBase.SB_VNET_FAVORITE2,
      MtkTvChCommonBase.SB_VNET_FAVORITE3, MtkTvChCommonBase.SB_VNET_FAVORITE4
  };
  public static final int favMasks = MtkTvChCommonBase.SB_VNET_FAVORITE1 | MtkTvChCommonBase.SB_VNET_FAVORITE2 |
          MtkTvChCommonBase.SB_VNET_FAVORITE3 | MtkTvChCommonBase.SB_VNET_FAVORITE4;
  public static final int FAVOURITE_1 = 0;
  public static final int FAVOURITE_2 = 1;
  public static final int FAVOURITE_3 = 2;
  public static final int FAVOURITE_4 = 3;
  public static final String CH_TYPE_FAV = "typefav";
  private boolean mIsCHChanging=false;
  private boolean mAudioFMTUpdated=false;
  private String mPrivSimBFirstLine = "";
  private boolean mIsFavTypeState = false;
  private boolean isCreatedMonitor = false;
  private static String countryCode = "";

  public boolean getFavTypeState(){
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mIsFavTypeState>> "+mIsFavTypeState);
    if (isCurrentSourceATV()){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavTypeState is atv return false");
        return false;
    }
    return mIsFavTypeState;
  }

  public void setFavTypeState(boolean favState){
    mIsFavTypeState = favState;
  }

  public String getPrivSimBFirstLine(){
      return mPrivSimBFirstLine;
  }

  public void setPrivSimBFirstLine(String privSimBFirstLine){
      mPrivSimBFirstLine = privSimBFirstLine;
  }

  public boolean isAudioFMTUpdated(){
      return mAudioFMTUpdated;
  }

  public void setAudioFMTUpdate(boolean audioFMTUpdated){
      mAudioFMTUpdated=audioFMTUpdated;
  }

  public boolean isCHChanging(){
      return mIsCHChanging;
  }

  public void setCHChanging(boolean isCHChanging){
      mIsCHChanging=isCHChanging;
  }
  private boolean mIsTurnCHOfExitEPG=false;

  public boolean isTurnCHOfExitEPG(){
      return mIsTurnCHOfExitEPG;
  }

  public void setTurnCHOfExitEPG(boolean isTurnCHOfExitEPG){
      mIsTurnCHOfExitEPG=isTurnCHOfExitEPG;
  }

  public boolean isBarkChannel(MtkTvChannelInfoBase channelInfo){
      boolean isBarkerCH = false;
        if (channelInfo instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo dvbChannelInfo = (MtkTvDvbChannelInfo) channelInfo;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBarkChannel----->barkerMask=" + dvbChannelInfo.getBarkerMask());
            if (dvbChannelInfo.getBarkerMask() == 1) {
                isBarkerCH = true;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBarkChannel----->isBarkerCH=" + isBarkerCH);
        return isBarkerCH;
  }


  public boolean isRtl() {
      return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
  }

  public interface ChannelChangedListener {
    void onChannelChanged();
  }

  public void setChannelChangedListener(ChannelChangedListener listener) {
    mChListener = listener;
  }

  public static boolean supportTIFFunction() {
      return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT);
  }

  public boolean isShowFAVListFullToastDealy() {
    return showFAVListFullToastDealy;
  }

  public void setShowFAVListFullToastDealy(boolean showFAVListFullToastDealy) {
    this.showFAVListFullToastDealy = showFAVListFullToastDealy;
  }

  public boolean isDoPIPPOPAction() {
    return doPIPPOPAction;
  }

  public void setDoPIPPOPAction(boolean doPIPPOPAction) {
    this.doPIPPOPAction = doPIPPOPAction;
  }

  public boolean isStopTIFSetupWizardFunction() {
    return stopTIFSetupWizardFunction;
  }

  public void setStopTIFSetupWizardFunction(boolean stopTIFSetupWizardFunction) {
    this.stopTIFSetupWizardFunction = stopTIFSetupWizardFunction;
  }

  private static Context mContext;

  /*
   * Destroy initilize context
   */
  public void setContext(Context context) {
    if(mContext == null) {
      mContext = context;
    }
  }

  public boolean isContextInit() {
    return mContext != null;
  }

  private CommonIntegration() {
    mMtkTvAppTV = MtkTvAppTV.getInstance();
    mMtkTvDvbsConfigBase = new MtkTvDvbsConfigBase();
    mMtkTvConfig = MtkTvConfig.getInstance();
    mtkTvScanDvbsBase =new MtkTvScanDvbsBase();
    getChannelAllandActionNum();
    mMtkTvAppTVBase = new MtkTvAppTVBase();
  }

  private void initRatingSystem() {
        String country = MtkTvConfig.getInstance().getCountry();
        String convertCountry = Utils.convertConurty(country);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country=" + country + ",convertCountry="
                + convertCountry);
        mContentRatingSystem = TvSingletons.getSingletons()
                .getContentRatingsManager()
                .getContentRatingSystemByCountry(convertCountry);
    }

    public synchronized String mapRating2CustomerStr(int ratingValue, String strRating) {
//        if (ratingValue <= 0 && TextUtils.isEmpty(strRating)) {
//            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "ratingValue<=0  && strRating is empty!");
//            return (ratingValue < 0) ? "" : mContext.getString(R.string.rating_not_defined);
//        }
        String country = MtkTvConfig.getInstance().getCountry();
        boolean isSpecialCountry = false;
        if (country.equals(MtkTvConfigTypeBase.S3166_CFG_COUNT_AUS) || country.equals(MtkTvConfigTypeBase.S3166_CFG_COUNT_FRA)) {
            isSpecialCountry = true;
        }
        if (ratingValue <= 0 && !isSpecialCountry) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "ratingValue == " + ratingValue + "isSpecialCountry == " + isSpecialCountry);
            return "";
        }
        initRatingSystem();
        if (mContentRatingSystem == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "mContentRatingSystem==null!");
            return "";
        }
        List<Rating> ratings = mContentRatingSystem.getRatings();
        if (ratings == null || ratings.isEmpty()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "ratings==null or ratings is empty!");
            return "";
        }
        Rating rating = getRatingByRatingValue(ratings, ratingValue, strRating);
        if (rating != null && rating.getValue() != null) {
            Log.d(TAG, "add rating value :" + rating.getValue());
            return rating.getValue();
        }
        return rating == null ? mContext.getString(R.string.rating_not_defined) : rating.getTitle();
    }
    private Rating getRatingByRatingValue(List<Rating> ratings,int ratingValue,String strRating){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRatingByRatingValue---->ratingValue="+ratingValue+",strRating="+strRating);
        Rating firstRating=ratings.get(0);
        Rating lastRating=ratings.get(ratings.size()-1);
//        if(ratingValue==0&&!TextUtils.isEmpty(strRating)){
//            return lastRating;
//        }
        if(ratingValue<firstRating.getAgeHint()||ratingValue>lastRating.getAgeHint()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Out of bound rating age.");
            return null;
        }
        Rating resultRating=null;
        for (Rating rating : ratings) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "getRatingByRatingValue--->rating.title=" + rating.getTitle()+",getAgeHint="+rating.getAgeHint());
            if (ratingValue<=rating.getAgeHint()) {
                resultRating=rating;
                break;
            }
        }
        return resultRating;
    }

  public synchronized static CommonIntegration getInstanceWithContext(Context context) {
    if (null == instanceNavIntegration) {
      instanceNavIntegration = new CommonIntegration();
    }
    if (mContext == null) {
      mContext = context;
    }
    return instanceNavIntegration;
  }

  public synchronized static CommonIntegration getInstance() {
    if (null == instanceNavIntegration) {
      instanceNavIntegration = new CommonIntegration();
    }

    return instanceNavIntegration;
  }

  /**
   * this method is used to remove the object
   */
  protected synchronized static void remove() {
    instanceNavIntegration = null;
    chBroadCast = null;
    instanceMtkTvUtil = null;
    instanceMtkTvHighLevel = null;
    mtkTvChList = null;
  }

  /**
   * get MtkTvBroadcast instance
   *
   * @return
   */
  public synchronized static MtkTvBroadcast getInstanceMtkTvBroadcast() {
    if (null == chBroadCast) {
      chBroadCast = MtkTvBroadcast.getInstance();
    }
    return chBroadCast;
  }

  public synchronized static MtkTvUtil getInstanceMtkTvUtil() {
    if (null == instanceMtkTvUtil) {
      instanceMtkTvUtil = MtkTvUtil.getInstance();
    }
    return instanceMtkTvUtil;
  }

  public synchronized static MtkTvHighLevel getInstanceMtkTvHighLevel() {
    if (null == instanceMtkTvHighLevel) {
      instanceMtkTvHighLevel = new MtkTvHighLevel();
    }
    return instanceMtkTvHighLevel;
  }

  /**
   * if current channel is analog
   *
   * @return
   */
  public boolean isCurCHAnalog() {
    boolean isCurCHAnalog=false;
    TIFChannelInfo tIFChannelInfo=TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
	if(tIFChannelInfo != null){
        isCurCHAnalog=tIFChannelInfo.isAnalogService();
    }else{
        MtkTvChannelInfoBase info = getCurChInfo();
        if (info != null){
            isCurCHAnalog = info.isAnalogService();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurCHAnalog hhh " + isCurCHAnalog);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurCHAnalog = " + isCurCHAnalog);
    return isCurCHAnalog;
  }


  /**
   * if current source is tv or not
   *
   * @return
   */
  public boolean isCurrentSourceTv() {
    boolean isTv = InputSourceManager.getInstance().isCurrentTvSource(getCurrentFocus());
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTv = " + isTv);
    return isTv;
  }

  public boolean is3rdTVSource() {
     boolean is3rdTVSource=false;
     TIFChannelInfo tIFChannelInfo=TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
     if(tIFChannelInfo != null && TIFFunctionUtil.is3rdTVSource(tIFChannelInfo)){
         is3rdTVSource=true;
     }
     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is3rdTVSource = " + is3rdTVSource);
     return is3rdTVSource;
  }

  public static boolean is3rdTVSource(TvInputInfo input) {
      if (input == null) {
          return false;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is3rdTVSource, input.getId() = " + input.getId()
              + "  :" + input.getHdmiDeviceInfo());
      if (!input.getId().contains("/HW")) {
          return input.getHdmiDeviceInfo() == null;
      }

      return false;
  }

  public TvInputInfo getTvInputInfo() {
      return InputSourceManager.getInstance().getTvInputInfo();
  }

  public boolean isCurrentSourceHDMI() {
    return InputSourceManager.getInstance().isCurrentHDMISource(getCurrentFocus());
  }

  public String getCurrentSource() {
    String current = InputSourceManager.getInstance().getCurrentInputSourceName(getCurrentFocus());
    if (TextUtils.isEmpty(current)) {
      return "";
    }
    return current;
  }

  /**
   * get current output source is tv or not
   *
   * @param output "main" or "sub"
   * @return
   */
  public boolean isCurrentSourceTv(String output) {
    return InputSourceManager.getInstance().isCurrentTvSource(output);
  }

  public int getAnalogChannelDisplayNumInt(int orignalNum) {
      if (orignalNum < ANALOG_CHANNEL_NUMBER_START) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelDisplayNumInt invalid original channel number:" + orignalNum);
        return orignalNum;
      }
      int displayNum = -1;
      displayNum = orignalNum - ANALOG_CHANNEL_NUMBER_START + 1;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelDisplayNumInt channel number:" + displayNum);
      return displayNum;
    }

  //  public String getAnalogChannelDisplayNumStr(int orignalNum) {
  //
  //  }

    public int getAnalogChannelDisplayNumInt(String orignalNum) {
      int displayNum = -1;
      try {
        displayNum = Integer.parseInt(orignalNum);
      } catch (Exception e) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelDisplayNumInt invalid channel number:" + orignalNum);
        return displayNum;
      }
      if (displayNum < ANALOG_CHANNEL_NUMBER_START) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelDisplayNumInt invalid original channel number:" + displayNum);
        return displayNum;
      }
      displayNum = displayNum - ANALOG_CHANNEL_NUMBER_START + 1;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelDisplayNumInt channel number:" + displayNum);
      return displayNum;
    }

  //  public String getAnalogChannelDisplayNumStr(String orignalNum) {
  //
  //  }

    public int getAnalogChannelOrignalNumInt(int displayNum) {
      if (displayNum < 1) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelOrignalNumInt invalid displayNum:" + displayNum);
        return displayNum;
      }
      int originalNum = -1;
      originalNum = displayNum + ANALOG_CHANNEL_NUMBER_START - 1;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelOrignalNumInt channel number:" + originalNum);
      return originalNum;
    }

  //  public String getAnalogChannelOrignalNumStr(int displayNum) {
  //
  //  }

    public int getAnalogChannelOrignalNumInt(String displayNum) {
      int originalNum = -1;
      try {
        originalNum = Integer.parseInt(displayNum);
      } catch (Exception e) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelOrignalNumInt invalid channel number:" + displayNum);
        return originalNum;
      }
      if (originalNum < 1) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelOrignalNumInt invalid originalNum:" + originalNum);
        return originalNum;
      }
      originalNum = originalNum + ANALOG_CHANNEL_NUMBER_START - 1;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAnalogChannelOrignalNumInt channel number:" + originalNum);
      return originalNum;
    }




  /**
   * if current input source is blocked
   *
   * @return
   */
  public boolean isCurrentSourceBlocked() {
    return InputSourceManager.getInstance().isCurrentSourceBlocked(getCurrentFocus());
  }

  public boolean isCurrentSourceBlockEx() {
      int status = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_NAV_BLOCKED_STATUS);
      int mask = status & 0xff;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("isCurrentSourceBlockEx, status:" + status + " , mask:" + mask);
      return /*mask == 1 || mask == 2 || */mask == 3;
      //return InputSourceManager.getInstance().isCurrentInputBlockEx(getCurrentFocus());
  }

  /**
   * if tv inputsource is blocked
   *
   * @return
   */
  public boolean isMenuInputTvBlock() {
    //modified begin by jg_jianwang for DTV01523774
    //boolean ret = InputSourceManager.getInstance().isTvInputBlocked();
    boolean ret = false;
    PwdDialog pwdDialog = (PwdDialog) ComponentsManager
            .getInstance().getComponentById(
                NavBasic.NAV_COMP_ID_PWD_DLG);
    if(InputSourceManager.getInstance().isTvInputBlocked() || (pwdDialog != null && pwdDialog.isContentBlock())){
        ret = true;
    }
    //modified end by jg_jianwang for DTV01523774
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("isMenuInputTvBlock", "biaoqing ret ==" + ret);
    return ret;
  }

  /**
   * set input source to tv
   */
  public void iSetSourcetoTv() {
    if (supportTIFFunction()) {
      TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {
        @Override
        public void run() {
            int type;
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
                MtkTvChannelInfoBase channelInfo = CommonIntegration.getInstance().getCurChInfo();
                if (channelInfo instanceof MtkTvAnalogChannelInfo) {
                    type = AbstractInput.TYPE_ATV;
                } else {
                    type = AbstractInput.TYPE_DTV;
                }
            } else {
                type = AbstractInput.TYPE_TV;
            }
            AbstractInput input = InputUtil.getInputByType(type);
            if (input != null) {
                InputSourceManager.getInstance().changeInputSourceByHardwareId(input.getHardwareId());
            }
        }
      });
    }
    else {
      InputSourceManager.getInstance().changeCurrentInputSourceByName(
          MtkTvInputSource.INPUT_TYPE_TV);
    }
  }

  /**
   * set input source to tv by number key.
   */
  public void iSetSourcetoTv(int channelId) {
    if (supportTIFFunction()) {
      TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          InputSourceManager.getInstance().changeToTVAndSelectChannel("TV", channelId);
        }
      });
    }
    else {
      InputSourceManager.getInstance().changeCurrentInputSourceByName(
          MtkTvInputSource.INPUT_TYPE_TV);
    }
  }

  /**
   * current TV mode is PIP or POP mode or not
   *
   * @return
   */
  public boolean isPipOrPopState() {
    if (mCurrentTvMode != TV_NORMAL_MODE) {
      if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
        mCurrentTvMode = getInstanceMtkTvHighLevel().getCurrentTvMode();
      }
    }
    else {
      return false;
    }

    return TV_PIP_MODE == mCurrentTvMode || TV_POP_MODE == mCurrentTvMode;
  }

  /**
   * current TV mode is PIP state or not
   *
   * @return
   */
  public boolean isPIPState() {
    if (mCurrentTvMode != TV_NORMAL_MODE) {
      if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
        mCurrentTvMode = getInstanceMtkTvHighLevel().getCurrentTvMode();
      }
    }
    else {
      return false;
    }

    return TV_PIP_MODE == mCurrentTvMode;
  }

  /**
   * current TV mode is POP state or not
   *
   * @return
   */
  public boolean isPOPState() {
    if (mCurrentTvMode != TV_NORMAL_MODE) {
      if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
        mCurrentTvMode = getInstanceMtkTvHighLevel().getCurrentTvMode();
      }
    }
    else {
      return false;
    }

    return TV_POP_MODE == mCurrentTvMode;
  }

  /**
   * current TV mode is normal state
   *
   * @return
   */
  public boolean isTVNormalState() {
    if (mCurrentTvMode != TV_NORMAL_MODE) {
      if (!MarketRegionInfo
          .isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
        mCurrentTvMode = getInstanceMtkTvHighLevel().getCurrentTvMode();
      }
    }

    return TV_NORMAL_MODE == mCurrentTvMode;
  }

  public static boolean isUSRegion() {
      return MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US;
  }

  public static boolean isSARegion() {
      return MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA;
  }

  public static boolean isEURegion() {
      return MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU;
  }

  public static boolean isEUPARegion() {
      if (isEURegion()) {
          return MarketRegionInfo.isEU_PA_Region();
      }else {
          return false;
      }
  }

  public static boolean isTVSourceSeparation() {
      return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT);
  }

  public static boolean isCNRegion() {
      return MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_CN;
  }

  /**
   * if current source is dtv or not
   *
   * @return
   */
  public boolean isCurrentSourceDTV() {
    boolean isDTV = false;
    AbstractInput input = getCurrentAbstractInput();
    if(input != null) {
        if (input.isTV()) {
            MtkTvChannelInfoBase chInfo = getCurChInfo();
            if (chInfo != null && chInfo.getBrdcstType() > MtkTvChCommonBase.BRDCST_TYPE_ANALOG) {
                isDTV = true;
            }
        } else {
            isDTV = input.isDTV();
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceDTV isDTV =" + isDTV);
    return isDTV;
  }

  /**
   * if current source is atv or not
   *
   * @return
   */
  public boolean isCurrentSourceATV() {
    boolean isATV = false;
    AbstractInput input = getCurrentAbstractInput();
    if(input != null) {
        if(input.isTV()) {
        	isATV =  isCurCHAnalog();
        } else {
            isATV = input.isATV();
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceATV isATV =" + isATV);
    return isATV;

  }

  public boolean isCurrentSourceDTVforEuPA() {
      AbstractInput input = getCurrentAbstractInput();
      return input != null && input.isDTV();
  }

  public boolean isCurrentSourceATVforEuPA() {
      AbstractInput input = getCurrentAbstractInput();
      return input != null && input.isATV();
  }

  public boolean isCurrentSourceVGA() {
      AbstractInput input = getCurrentAbstractInput();
      return input != null && input.isVGA();
  }

  public boolean isCurrentSourceComposite(){
      AbstractInput input = getCurrentAbstractInput();
      return input != null && input.isComposite();
  }

    private AbstractInput getCurrentAbstractInput() {
        int id = TvSingletons.getSingletons().getInputSourceManager().getCurrentInputSourceHardwareId();
        return InputUtil.getInput(id);
    }

  public void stopMainOrSubTv() {
    if (getCurrentFocus().equalsIgnoreCase("main")) {
      MtkTvBroadcast.getInstance().syncStop("main", false);
    } else if (getCurrentFocus().equalsIgnoreCase("sub")) {
      MtkTvBroadcast.getInstance().syncStop("sub", false);
    }
  }

  public void startMainOrSubTv() {
    if (getCurrentFocus().equalsIgnoreCase("main")) {
      getInstanceMtkTvHighLevel().startTV();
    } else if (getCurrentFocus().equalsIgnoreCase("sub")) {
      selectChannelById(getCurrentChannelId());
    }
  }

  /**
   * set cur channel is favorite channel
   */
  public void iSetCurrentChannelFavorite() {
    int chId = getCurrentChannelId();

    List<MtkTvFavoritelistInfoBase> favList = MtkTvChannelList
        .getInstance().getFavoritelistByFilter();
    for (int i = 0; i < favList.size(); i++) {
      if (favList.get(i).getChannelId() == chId) {
        MtkTvChannelList.getInstance().removeFavoritelistChannel(i);
        MtkTvChannelList.getInstance().storeFavoritelistChannel();
        return;
      }
    }

    if (!CommonIntegration.getInstance().isFavListFull()) {
      MtkTvChannelList.getInstance().addFavoritelistChannel();
      MtkTvChannelList.getInstance().storeFavoritelistChannel();
    } else {
    	NavBasicDialog mChannelListDialog = (NavBasicDialog) ComponentsManager
          .getInstance().getComponentById(
              NavBasic.NAV_COMP_ID_CH_LIST);
      if (!(null != mChannelListDialog && mChannelListDialog.isShowing())) {
        showFavFullMsg();
      } else {
        setShowFAVListFullToastDealy(true);
      }
    }
  }

  /**
   * select channel with channe1 number This API is used to select a channel by the whole channel
   * number.
   *
   * @param [in] majorNo The major part of the channel number.
   * @param [in] minorNo The minor part of the channel number. {-1 for no minorNo}
   *
   *        <pre>
   *             Generally, the range of major and minor channel number depends on the region,
   *             county and broadcast type.
   *
   *             EU => Doesn't support minor channel No.
   *             NAFTA=>Air:      1 <= majorNo <= 99
   *                                     1 <= minorNo <= 65535
   *                          Cable:  0 <= majorNo <= 999
   *                                     0 <= minorNo <= 65535
   *             LATIN =>Doesn't support minor channel No.
   *
   * </pre>
   * @return This API will return 0 (OK), others (Fail).
   */
  public boolean selectChannelByNum(int majorNo, int minorNo) {
    int result = -1;
    MtkTvChannelInfoBase curCh = getCurChInfo();
    if (minorNo == -1) {
      result = getInstanceMtkTvBroadcast().channelSelectByChannelNumber(majorNo);
    } else {
      result = getInstanceMtkTvBroadcast().channelSelectByChannelNumber(majorNo, minorNo);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByNum majorNo = " + majorNo + " minorNo = " + minorNo
        + "result =" + result);
    if (result == 0) {
      if (mChListener != null
          && (checkChMask(curCh, CH_LIST_MASK, 0) || checkCurChMask(CH_LIST_MASK, 0))) {
        mChListener.onChannelChanged();
      }
      mPreChInfo = curCh;
      return true;
    }
    return false;
  }

  /**
   * select channel with channe1 number
   *
   * @param num
   * @return true (OK) false (Error)
   */
  public boolean selectChannelByInfo(MtkTvChannelInfoBase chInfo) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByInfo chInfo = " + chInfo);
    if (supportTIFFunction()) {
      TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
          .getTIFChannelInfoById(chInfo.getChannelId());
      if (tifChannelInfo != null) {
        return TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(tifChannelInfo);
      } else {
        return false;
      }
    } else {
      if (null != chInfo && chInfo.getChannelId() != getCurrentChannelId()) {
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_CHANNEL_CHANGED, 0);
      }
      int result = -1;
      MtkTvChannelInfoBase curCh = getCurChInfo();
      result = getInstanceMtkTvBroadcast().channelSelect(chInfo, false);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByInfo selected curInfo  = " + getCurChInfo());
      if (result == 0) {
        if (mChListener != null
            && (checkChMask(curCh, CH_LIST_MASK, 0) || checkCurChMask(CH_LIST_MASK, 0))) {
          mChListener.onChannelChanged();
        }
        mPreChInfo = curCh;
        return true;
      }
      return false;
    }
  }

  /**
   * get pre-ch id
   *
   * @return
   */
  public int getLastChannelId() {
    int chId = MtkTvConfig.getInstance().getConfigValue(LAST_CHANNEL_ID);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getLastChannelId chId = " + chId);
    return chId;
  }

  public int getCurrentChannelId() {
        int chId = -1;
        chId = MtkTvConfig.getInstance().getConfigValue(CUR_CHANNEL_ID);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"CUR_CHANNEL_ID:"+chId);
       if(!isCurrentSourceATVforEuPA()){
           if(mContext !=null){
               TIFChannelInfo mTIFChannelInfo=  TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
               if (is3rdTVSource()) {
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelId 3rd chId");
                   chId =(int) mTIFChannelInfo.mId;
               }
           }
       }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelId chId = " + chId);
        return chId;
      }

  public int get2NDLastChannelId() {
    return MtkTvConfig.getInstance().getConfigValue(LAST_2ND_CHANNEL_ID);
  }

  public int setCurrentChannelId(int numId) {
    return MtkTvConfig.getInstance().setConfigValue(CUR_CHANNEL_ID, numId);
  }

  public int get2NDCurrentChannelId() {
      int chId = MtkTvConfig.getInstance().getConfigValue(CUR_2ND_CHANNEL_ID);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelId chId = " + chId);
      return chId;
  }
    /**
     *
     * @return
     */
  public boolean isFakerChannel(MtkTvChannelInfoBase curCh) {
     if (!(isUSRegion() || isSARegion())) {
        return false;
     }
     boolean isFakerChannel = checkChMask(curCh,
                CommonIntegration.CH_FAKE_MASK, CommonIntegration.CH_FAKE_VAL);
     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFakerChannel=" + isFakerChannel);
     return isFakerChannel;
  }
  public MtkTvChannelInfoBase getCurChInfo() {
      int chId=0;
        if(isdualtunermode()){
            chId = get2NDCurrentChannelId();
        }else {
            MtkTvChannelInfoBase curCh=MtkTvChannelList.getCurrentChannel();
            if (curCh != null && (curCh.getChannelId() == -1 || curCh.getSvlId() == 0)){
                return null;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurChInfo for mtkapi chId = " + chId + "curCh=" + curCh);
            return curCh;
        }
  //  MtkTvChannelInfoBase curCh = getChannelById(chId);
    MtkTvChannelInfoBase curCh = TIFChannelManager.getInstance(mContext)
        .getAPIChannelInfoById(chId);
    if (curCh != null && (curCh.getChannelId() == -1 || curCh.getSvlId() == 0)){
        return null;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurChInfo chId = " + chId + "curCh=" + curCh);
    return curCh;
  }

  public void selectTIFChannelInfoByChannelIdForTuneMode() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectTIFChannelInfoByChannelIdForTuneMode  = ");
      TVAsyncExecutor.getInstance().execute(new Runnable() {
          @Override
          public void run() {
              TIFChannelManager.getInstance(mContext).selectTIFChannelInfoByChannelIdForTuneMode();

         }
      });
  }
  public int getChannelIdByConfigId() {
      int chId=0;
        if(isdualtunermode()){
            chId = get2NDCurrentChannelId();
        }else {
            chId =MtkTvConfig.getInstance().getConfigValue(CUR_CHANNEL_ID);// getInstanceMtkTvBroadcast().getCurrentChannelId();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelIdByConfigId chId = " + chId );
    return chId;
  }
    public MtkTvChannelInfoBase getCurChInfoByTIF() {
        int chId = getCurrentChannelId();// getInstanceMtkTvBroadcast().getCurrentChannelId();
        // MtkTvChannelInfoBase curCh = getChannelById(chId);
        MtkTvChannelInfoBase curCh = TIFChannelManager.getInstance(mContext)
            .getAPIChannelInfoById(chId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurChInfoByTIF chId = " + chId + "curCh=" + curCh);
        return curCh;
    }

  public boolean checkCurChMask(int mask, int val) {
    int chId = getCurrentChannelId();// getInstanceMtkTvBroadcast().getCurrentChannelId();
    MtkTvChannelInfoBase curCh = TIFChannelManager.getInstance(mContext).getAPIChannelInfoByChannelId(chId);

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkCurChMask chId = " + chId + "curCh=" + curCh);
    if (curCh != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkCurChMask curCh.getNwMask() = " + curCh.getNwMask() + "mask=" + mask
          + "val=" + val);
      if ((curCh.getNwMask() & mask) == val) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkCurChMask true");
        return true;
      }
    }else if(chId > 0){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkCurChMask 3rd chId ");
        TIFChannelInfo mTIFChannelInfo= TIFChannelManager.getInstance(mContext).getTifChannelInfoByUri(TIFChannelManager.getInstance(mContext).buildChannelUri(chId));
        if(mTIFChannelInfo !=null && mTIFChannelInfo.mMtkTvChannelInfo ==null && mask == CH_LIST_3RDCAHNNEL_MASK){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkCurChMask 3rd chId true");
            return true;
        }
    }
    return false;
  }

  public boolean checkDukCurChMask(int mask, int val,int sdtServiceType) {
      int chId = getCurrentChannelId();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkDukCurChMask chId = " + chId);

          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkCurChMask 3rd chId ");
          TIFChannelInfo mTIFChannelInfo= TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
          if(mTIFChannelInfo !=null && TIFFunctionUtil.checkChMaskForDuk(mTIFChannelInfo, mask, val, sdtServiceType)){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkDukCurChMask true");
              return true;
          }else{
              if(TIFFunctionUtil.checkChMaskForDuk(TIFChannelManager.getInstance(mContext).getTIFChannelInfoById(chId), mask, val, sdtServiceType)){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkDukCurChMask true");
                  return true;
              }
          }
      return false;
    }

  public boolean checkChMask(MtkTvChannelInfoBase chinfo, int mask, int val) {
    // int chId = getCurrentChannelId();//getInstanceMtkTvBroadcast().getCurrentChannelId();
    MtkTvChannelInfoBase curCh = chinfo;// getChannelById(chId);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chId = " + "curCh=" + curCh);
    if (curCh != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask curCh.getNwMask() = " + curCh.getNwMask() + "mask=" + mask
          + "val=" + val);
      if ((curCh.getNwMask() & mask) == val) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
        return true;
      }
    }
    return false;
  }

  /**
   * same with getSvl() function svlID 1:air, 2:cable, 3:general sat, 4:prefer sat, 5:CAM-air,
   * 6:CAM-cable, 7:CAM-sat
   *
   * @return
   */
  public int getSvlFromACFG() {
    int svlId = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.SVL_ID);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSvlFromACFG>>>>" + svlId);
    return svlId;
  }

  public int getTunerModeFromSvlId(int svlId){
      switch (svlId) {
          case DB_CI_PLUS_SVLID_AIR:
          case DB_AIR_SVLID:
              return DB_AIR_OPTID;

          case DB_CI_PLUS_SVLID_CAB:
          case DB_CAB_SVLID:
              return DB_CAB_OPTID;

          case DB_CI_PLUS_SVLID_SAT:
          case DB_SAT_SVLID:
              if(ScanContent.getDVBSAllOperatorList(mContext).size() == 0){
                  return DB_SAT_OPTID; //satellite
              }
              return DB_GENERAL_SAT_OPTID;  //general

          case DB_SAT_PRF_SVLID:
              return DB_SAT_OPTID;//prefer

          default:
              return DB_AIR_OPTID;


      }
  }

    public void setChanenlListType(boolean isNetwork){
        int type = 0;
        int mask = CH_LIST_MASK;
        int maskvalue = CH_LIST_VAL;
        int networkType = 0;
        NavBasicDialog mNavBasicDialog = (NavBasicDialog) ComponentsManager.getInstance()
                .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
        if(mNavBasicDialog instanceof ChannelListDialog){
            ChannelListDialog mChListDialog =(ChannelListDialog) mNavBasicDialog;
            networkType = mChListDialog.getChannelNetworkIndex();
        }else if(mNavBasicDialog instanceof UKChannelListDialog){
            UKChannelListDialog mUKChListDialog =(UKChannelListDialog) mNavBasicDialog;
            networkType = mUKChListDialog.getChannelNetworkIndex();
        }
        if(isNetwork){
            if(networkType != 0){
                SaveValue.getInstance(mContext).saveValue("type_"+getSvl(),networkType);
                SaveValue.getInstance(mContext).saveValue(channelListfortypeMask,-1);
                SaveValue.getInstance(mContext).saveValue(channelListfortypeMaskvalue,-1);
            }else{
                return ;
            }
        }else{
            int lastType = SaveValue.getInstance(mContext).readValue("type_"+getSvl(),type);
            if (lastType == networkType){
                SaveValue.getInstance(mContext).saveValue("type_"+getSvl(),type);
                SaveValue.getInstance(mContext).saveValue(channelListfortypeMask,mask);
                SaveValue.getInstance(mContext).saveValue(channelListfortypeMaskvalue,maskvalue);
            }
        }
    }
    public void clearTypeLastChannel(){
        SaveValue.getInstance(mContext).saveValue("type_save_"+getSvl(),"");
    }
  /**
   * same with getSvlFromACFG() function svlID 1:air, 2:cable, 3:general sat, 4:prefer sat,
   * 5:CAM-air, 6:CAM-cable, 7:CAM-sat
   *
   * @return
   */
  public int getSvl() {
    return getSvlFromACFG();
  }

  /**
   * tuner mode value, 0:T, 1:C, 2:S
   */
  public int getTunerMode() {
    return MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
  }

  public String getCountryCode(){
      if("".equals(countryCode)){
          countryCode = MtkTvConfig.getInstance().getCountry();
      }
      Log.d(TAG, "countryCode:" + countryCode);
      return countryCode;
  }

  public void setCountryCode(String code){
      countryCode = code;
  }

  public int get2ndTunerMode() {
      return MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.DUAL_TUNER);
    }
  public boolean isdualtunermode(){
      return getCurrentFocus().equalsIgnoreCase("sub") &&
        MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) &&
        isCurrentSourceTv();
  }

  public boolean isPreferSatMode() {
      if(isdualtunermode()){
          int  prefer = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.GENERALESATELITE);
          return prefer != 0 && get2ndTunerMode() >= DB_SAT_OPTID;
      }else {
          int  prefer = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE_PREFER_SAT);
          return prefer != 0 && getTunerMode() >= DB_SAT_OPTID;
      }
  }

  public static void resetBlueMuteFor3rd(Context mContext) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("LiveTvSetting", "resetBlueMuteFor3rd BlueMute defvalue !=0");
      Intent intent=new Intent("mtk.intent.blue.mute");
      intent.putExtra("status", "disable");
      mContext.sendBroadcast(intent);
  }

  public static void resetBlueMuteForLiveTv(Context mContext) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("LiveTvSetting", "resetBlueMuteForLiveTv defValue==0 && oldValue!=0");
      Intent intent=new Intent("mtk.intent.blue.mute");
      intent.putExtra("status", "update");
      mContext.sendBroadcast(intent);
  }

  public boolean isGeneralSatMode() {
    int prefer = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE_PREFER_SAT);
    return prefer == 0 && getTunerMode() >= DB_SAT_OPTID;
  }

  public int getChUpDownFilter() {
    int filter = 0;
    switch (MarketRegionInfo.getCurrentMarketRegion()) {
      case MarketRegionInfo.REGION_CN:
      case MarketRegionInfo.REGION_EU:
      case MarketRegionInfo.REGION_US:
      case MarketRegionInfo.REGION_SA:
        filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_VISIBLE;
        // MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
        break;
      default:
        break;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChUpDownFilter filter =" + filter);
    return filter;
  }

  public int getChListFilter() {
    int filter = 0;
    switch (MarketRegionInfo.getCurrentMarketRegion()) {
      case MarketRegionInfo.REGION_CN:
      case MarketRegionInfo.REGION_EU:
      case MarketRegionInfo.REGION_SA:
        filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
        // |MtkTvChCommonBase.SB_VNET_VISIBLE;
        break;
      case MarketRegionInfo.REGION_US:
          filter = MtkTvChCommonBase.SB_VNET_ACTIVE ;
          break;
      default:
        break;
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChListFilter filter =" + filter);
    return filter;
  }

  public int getChUpDownFilterEPG() {
    int filter = 0;
    switch (MarketRegionInfo.getCurrentMarketRegion()) {
      case MarketRegionInfo.REGION_CN:
      case MarketRegionInfo.REGION_EU:
      case MarketRegionInfo.REGION_SA:
        filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
        // |MtkTvChCommonBase.SB_VNET_VISIBLE;
        break;
      case MarketRegionInfo.REGION_US:
        filter = MtkTvChCommonBase.SB_VNET_EPG | MtkTvChCommonBase.SB_VNET_VISIBLE;
        // MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
        break;
      default:
        break;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChUpDownFilter filter =" + filter);
    return filter;
  }

  public int getChListFilterEPG() {
    int filter = 0;
    switch (MarketRegionInfo.getCurrentMarketRegion()) {
      case MarketRegionInfo.REGION_CN:
      case MarketRegionInfo.REGION_EU:
      case MarketRegionInfo.REGION_SA:
        filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
        // |MtkTvChCommonBase.SB_VNET_VISIBLE;
        break;
      case MarketRegionInfo.REGION_US:
        filter = MtkTvChCommonBase.SB_VNET_EPG | MtkTvChCommonBase.SB_VNET_FAKE;
        // |MtkTvChCommonBase.SB_VNET_VISIBLE;
        break;
      default:
        break;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChListFilter filter =" + filter);
    return filter;
  }


  public int getChannelActiveNumByAPI() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getchannellength getChannelNumByAPI COUNTCHLISTACTIVE = " + COUNTCHLISTACTIVE);
      return COUNTCHLISTACTIVE;
  }

  public int getChannelActiveNumByAPIForScan() {
      int num = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ACTIVE);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelActiveNumByAPIForScan num = " + num);
      return num;
  }

  public int getChannelNumByAPIForBanner() {
      int num = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ALL);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelNumByAPIForBanner num = " + num);
      return num;
  }

  public int getChannelAllNumByAPI() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getchannellength getChannelAllNumByAPI COUNTCHLISTALL = " + COUNTCHLISTALL);
      return COUNTCHLISTALL;
  }
  public int getBlockChannelNum() {
      int len = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_BLOCKED);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getchannellength getBlockChannelNum len = " + len);
      return len;
  }

  public int getBlockChannelNumForSource() {
      int mask = 0;
      int len = 0;
      if(isCNRegion() || isEUPARegion()){
          if(isCurrentSourceATVforEuPA()){
              mask = MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE | MtkTvChCommonBase.SB_VNET_BLOCKED;
          } else if(isCurrentSourceDTVforEuPA()){
              mask = MtkTvChCommonBase.SB_VNET_BLOCKED;
          }
          if (mask != 0) {
              len = mtkTvChList.getChannelCountByMask(getSvl(), MtkTvChCommonBase.SB_VNET_BLOCKED|MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE,mask);
          }
      }

      if(mask == 0){
          len = mtkTvChList.getChannelCountByMask(getSvl(), MtkTvChCommonBase.SB_VNET_BLOCKED,MtkTvChCommonBase.SB_VNET_BLOCKED);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getchannellength getBlockChannelNumForSource len = " + len);
      return len;
  }

  public void getChannelAllandActionNum() {
      TVAsyncExecutor.getInstance().execute(new Runnable() {
          @Override
          public void run() {
                  COUNTCHLISTALL = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ALL);
                  COUNTCHLISTACTIVE = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ACTIVE);
         }
      });

    }


  /**
     * get 3rdsource all channel list by tif
     * @return
     */
    public int getAllChannelListByTIFFor3rdSource() {
        List<TIFChannelInfo> mTIFChannelInfoList = TIFChannelManager.getInstance(mContext).get3RDChannelList();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getchannellength mTIFChannelInfoList= " + mTIFChannelInfoList);

        return (mTIFChannelInfoList != null) ? mTIFChannelInfoList.size(): 0;
    }

  public int getCurrentSvlChannelNum(int mask, int val) {
    return mtkTvChList.getChannelCountByMask(getSvl(), mask, val);
  }
  public int getAllEPGChannelLength() {
     return getAllEPGChannelLength(false);
  }
  /*
   * only for epg channel to show.
   */
  public int getAllEPGChannelLength(boolean showSkip) {
    int len = 0;
    if (supportTIFFunction()) {
      if (TIFChannelManager.getInstance(mContext).hasActiveChannel(showSkip)) {
        len = 1;
      }
    } else {
      len = mtkTvChList.getChannelCountByFilter(getSvl(), getChListFilterEPG());
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getchannellength getAllEPGChannelLength len = " + len);
    return len;
  }

  public boolean hasDTVChannels() {
    if (supportTIFFunction()) {
      return TIFChannelManager.getInstance(mContext).hasDTVChannels();
    }
    return false;
  }

  public boolean isDualChannellist() {
      return MtkTvConfig.getInstance().getConfigValue(
          MtkTvConfigTypeBase.CFG_MISC_2ND_CHANNEL_ENABLE) == 1
          && getCurrentFocus().equalsIgnoreCase("sub") && isCurrentSourceTv();
    }

  public boolean hasActiveChannel() {
      return hasActiveChannel(false);
  }

  /*
   * only for channellistDialog check if has active channel to show.
   */
  public boolean hasActiveChannel(boolean showSkip) {
      if(isDualChannellist()) {
          List<MtkTvChannelInfoBase> chList = getChannelListByMaskFilter(
                          0,
                          MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST,
                          1,
                          CH_LIST_MASK,
                          CH_LIST_VAL);
          return chList != null && !chList.isEmpty();
      }

    if (supportTIFFunction()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannel tif ");
      return TIFChannelManager.getInstance(mContext).hasActiveChannel(showSkip);
    }
    List<MtkTvChannelInfoBase> chList = getChannelListByMaskFilter(0,
        MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST, 1, CH_LIST_MASK, CH_LIST_VAL);
    // getChannelListByFakefilter(0,0,1);
    if (chList != null && !chList.isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannel true~ ");
      return true;
    }
    // add for check current Channel if Hidden channel, channellistDialog need show Hidden channel
    // if it's current play channel.
    // for US
    if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US) {
      if (checkCurChMask(CH_LIST_MASK, 0)) {
        return true;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasActiveChannel false ");
    return false;
  }

  public boolean hasChannels() {
    if (supportTIFFunction()) {
      return TIFChannelManager.getInstance(mContext).isChannelsExist();
    }
    List<MtkTvChannelInfoBase> chList = getChannelListByMaskFilter(0,
        MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST, 1, TIFFunctionUtil.CH_MASK_ALL,
        TIFFunctionUtil.CH_MASK_ALL);// getChannelListByFakefilter(0,0,1);
    if (chList != null && !chList.isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasChannels true~ ");
      return true;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasChannels false ");
    return false;
  }

  /*
   * only for channellistDialog check if has next page channel to show.
   */

  public int hasNextPageChannel(int count, int mask, int val) {
    int value = 0;
    List<MtkTvChannelInfoBase> chList = getChannelListByMaskFilter(0,
        MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST, count, mask, val);
    if (chList != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasNextPageChannel true~ ");
      value = chList.size();
      if (value == count) {
        return value;
      }
    }
    // add for check current Channel if Hidden channel, channellistDialog need show Hidden channel
    // if it's current play channel.
    // for US
    if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US) {
      if (checkCurChMask(CH_LIST_MASK, 0)) {
        value += 1;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasNextPageChannel false~ value = " + value);
    return value;

  }

  public boolean channelUpDownByMaskAndSat(int mask, int val, int satRecId, boolean isUp) {
      boolean result=doChannelUpDownByMaskAndSat(mask,val,satRecId,isUp);
      if(result){
          mIsCHChanging=true;
      }
      return result;
  }

  /**
   * selected channel up by satellite true(OK),false(error)
   */
  private boolean doChannelUpDownByMaskAndSat(int mask, int val, int satRecId, boolean isUp) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMaskAndSat");
    // PIP/POP
    if (isPipOrPopState()) {
      // current input is conflict with TV source, pre_channel fail
      List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
      if (sourceList != null && sourceList.contains("TV")) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict");
        return false;
      }
    }
    if (isCurrentSourceTv()) {
      if (isMenuInputTvBlock()) {
        return false;
      }
      int curChId = getCurrentChannelId();
      MtkTvChannelInfoBase chInfo = null;
      List<MtkTvChannelInfoBase> chInfoList = getChannelListByMaskAndSat(curChId,
          isUp ? MtkTvChannelList.CHLST_ITERATE_DIR_NEXT : MtkTvChannelList.CHLST_ITERATE_DIR_PREV,
          1, mask, val, satRecId);
      if (chInfoList != null && !chInfoList.isEmpty()) {
        chInfo = chInfoList.get(0);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMaskAndSat chInfo = " + chInfo);
      if (chInfo != null) {
        return selectChannelByInfo(chInfo);
      }
    } else {// non TV source, just jump to TV source, not change channel
      iSetSourcetoTv();
    }
    return false;
  }
  public boolean channelUpDownByMask(boolean isUp, int mask, int val) {
      boolean result=doChannelUpDownByMask(isUp,mask,val);
      if(result){
          mIsCHChanging=true;
      }
      return result;
  }
  /**
   * selected channel up true(OK),false(error)
   */
  private boolean doChannelUpDownByMask(boolean isUp, int mask, int val) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUpDownByMask");
    // PIP/POP
    if (isPipOrPopState()) {
      // current input is conflict with TV source, pre_channel fail
      List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
      if (sourceList != null && sourceList.contains("TV")) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict");
        return false;
      }
    }
    if (isCurrentSourceTv()) {
      if (isMenuInputTvBlock()) {
        return false;
      }

      MtkTvChannelInfoBase chInfo = getUpDownChInfoByMask(isUp, mask, val);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUp chInfo = " + chInfo);
      if (chInfo != null) {
        return selectChannelByInfo(chInfo);
      }
    } else {// non TV source, just jump to TV source, not change channel
      iSetSourcetoTv();
    }
    return false;
  }

  /**
   * selected channel up true(OK),false(error)
   */
  public boolean channelUp() {
    boolean result=false;
    NavBasicDialog mChListDialog = (NavBasicDialog) ComponentsManager.getInstance()
        .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
    if (mChListDialog instanceof ChannelListDialog) {
        result= ((ChannelListDialog)mChListDialog).channelUpDown(true);
    }else if ( mChListDialog instanceof UKChannelListDialog) {
        result= ((UKChannelListDialog)mChListDialog).channelUpDown(true);
    }
    if(result){
        mIsCHChanging=true;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUp result=" + result);
    return result;
  }

  /**
   * selected channel up true(OK),false(error)
   */
  public boolean channelUpDownNoVisible(boolean isUp) {
	  NavBasicDialog mChListDialog = (NavBasicDialog) ComponentsManager.getInstance()
        .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
    if (mChListDialog instanceof ChannelListDialog) {
      return ((ChannelListDialog)mChListDialog).channelUpDownNoVisible(isUp);
    }else if (mChListDialog instanceof UKChannelListDialog) {
        return ((UKChannelListDialog)mChListDialog).channelUpDownNoVisible(isUp);
      }
    return false;
  }


  public boolean channelPre(){
      if (isCurrentSourceTv()) {
          boolean result = false;

          NavBasicDialog mChListDialog = (NavBasicDialog) ComponentsManager.getInstance()
                  .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
          if ( mChListDialog instanceof UKChannelListDialog) {
              result= ((UKChannelListDialog)mChListDialog).channelPre();
          }else if (mChListDialog instanceof ChannelListDialog) {
              result= ((ChannelListDialog)mChListDialog).channelPre();
          }
          if (!result){
              return false;
          }

          result=doChannelPre();
          if(result){
             mIsCHChanging=true;
          }
          return result;
      }else
      {
          return TIFChannelManager.getInstance(mContext).channelPre();
      }
  }

  private boolean doChannelPre() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre mPreChInfo =" + mPreChInfo);
    if (supportTIFFunction()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre supportTIFFunction");
        if(!is3rdTVSource()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre supportTIFFunction !is3rdTVSource()");
            return TIFChannelManager.getInstance(mContext).channelPre();
        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre supportTIFFunction is3rdTVSource()");
            return false;
        }
    } else {
      if (isCurrentSourceTv() && isMenuInputTvBlock()) {
        return false;
      }
      // PIP/POP
      if (isPipOrPopState()) {
        // current input is conflict with TV source, pre_channel fail
        List<String> sourceList = InputSourceManager.getInstance().getConflictSourceList();
        if (sourceList != null && sourceList.contains("TV")) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source in conflict");
          return false;
        }
      }
      if (mPreChInfo != null) {
        return selectChannelByInfo(mPreChInfo);
      } else {
        return selectChannelByInfo(getCurChInfo());
      }
    }
  }

  /**
   * selected channel down true(OK),false(error)
   */
  public boolean channelDown() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelDown");
    boolean result=false;

    	  NavBasicDialog mChListDialog = (NavBasicDialog) ComponentsManager.getInstance()
                  .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
          if ( mChListDialog instanceof UKChannelListDialog) {
              result= ((UKChannelListDialog)mChListDialog).channelUpDown(false);
          }else if (mChListDialog instanceof ChannelListDialog) {
              result= ((ChannelListDialog)mChListDialog).channelUpDown(false);
          }
    if(result){
        mIsCHChanging=true;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelDown result=" + result);
    return result;

  }
  /**
   * isFavStateInChannelList is fav true(OK),false(false)
   */
  public boolean isFavStateInChannelList() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFavStateInChannelList");
    ChannelListDialog mChListDialog = (ChannelListDialog) ComponentsManager.getInstance()
        .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
    if (mChListDialog != null) {
        return mChListDialog.isFavStateInChannelList();
    }
    return false;
   // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelDown result=" + result);

  }

  public int getFavStateIndex() {
    int favType = SaveValue.getInstance(mContext).readValue(
            CommonIntegration.CH_TYPE_FAV,
            CommonIntegration.FAVOURITE_1);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavStateIndex: "+ favType);
    return favType;
  }

  public MtkTvChannelInfoBase favoriteListbyindex(int svlId, int favIndex,int index) {
    return mtkTvChList.favoriteListbyindex(svlId, favIndex, index);
  }

  /**
   * in according to channel ID to select channel
   *
   * @param channelID
   */
  public boolean selectChannelById(int channelId) {
    if (supportTIFFunction()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+isCurrentSourceTv());
       if(isdualtunermode()){
         TurnkeyUiMainActivity.getInstance().getPipView().tune(InputSourceManager.getInstance().getTvInputInfo("sub").getId(), MtkTvTISMsgBase.createSvlChannelUri(channelId));
         return true;
       }else{

         TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                      .queryChannelById(channelId);
            if (tifChannelInfo != null) {
                if(tifChannelInfo.mMtkTvChannelInfo != null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById tifChannelInfo channelId = " + tifChannelInfo.mMtkTvChannelInfo.getChannelId());
                }
                    return TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(tifChannelInfo);
            } else {
                ScanViewActivity.isSelectedChannel = true;
               return false;
            }
      }
    } else {
      if (channelId != getCurrentChannelId()) {
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_CHANNEL_CHANGED, 0);
      }
      int result = -1;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById channelId =" + channelId);
      MtkTvChannelInfoBase curCh = getCurChInfo();
      MtkTvChannelInfoBase chInfo = getChannelById(channelId);
      if (chInfo != null) {
        result = getInstanceMtkTvBroadcast().channelSelect(chInfo, false);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById chInfo =" + chInfo + "result =" + result);

      if (result == 0) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById mPreChInfo =" + curCh);
        if (mChListener != null
            && (checkChMask(curCh, CH_LIST_MASK, 0) || checkCurChMask(CH_LIST_MASK, 0))) {
          mChListener.onChannelChanged();
        }
        mPreChInfo = curCh;
        return true;
      }
      return false;
    }
  }

  /**
   * in according to channel ID to select channel
   *
   * @param channelID
   */
  public boolean selectChannelByIdSvl(int channelId) {

    int currentRegion = MarketRegionInfo.getCurrentMarketRegion();
    if ((currentRegion == MarketRegionInfo.REGION_SA)
        || (currentRegion == MarketRegionInfo.REGION_US)) {
      if (channelId != getCurrentChannelId()) {
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_CHANNEL_CHANGED, 0);
      }
      int result = -1;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById channelId =" + channelId);
      MtkTvChannelInfoBase curCh = getCurChInfo();
      MtkTvChannelInfoBase chInfo = getChannelById(channelId);
      if (chInfo != null) {
        result = getInstanceMtkTvBroadcast().channelSelect(chInfo, false);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById chInfo =" + chInfo + "result =" + result);

      if (result == 0) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelById mPreChInfo =" + curCh);
        if (mChListener != null
            && (checkChMask(curCh, CH_LIST_MASK, 0) || checkCurChMask(CH_LIST_MASK, 0))) {
          mChListener.onChannelChanged();
        }
        mPreChInfo = curCh;
        return true;
      }
      return false;
    } else {
      return selectChannelById(channelId);
    }
  }

  /**
   * This API allows the user to set some specified channels to the database BY MASS.
   *
   * @param [in] channelOperator CHLST_OPERATOR_ADD, CHLST_OPERATOR_MOD, CHLST_OPERATOR_DEL
   * @param [in] channelsToSet A list of Channels which the user wants to set
   *        (CHLST_OPERATOR_ADD/CHLST_OPERATOR_MOD/CHLST_OPERATOR_DEL) to the database.
   *
   *        <pre>
   *            There is at least one channel in 'channelsToSet' and these channels must be in
   *            the same database.
   *                   E.g. If 'channelOperator' == CHLST_OPERATOR_ADD, then the channel(s)
   *                        in  'channelsToSet' will be appended to the database.
   *                         If 'channelOperator' == CHLST_OPERATOR_MOD, then the channel(s)
   *                         in  'channelsToSet' will be updated to the database.
   *                         If 'channelOperator' == CHLST_OPERATOR_DEL, then the channel(s)
   *                         in  'channelsToSet' will be deleted from the database.
   * </pre>
   * @return CHLST_RET_OK, sucessful ; CHLST_RET_FAIL, failed.
   * @see com.mediatek.twoworlds.tv.model.MtkTvChannelInfo.
   */
  public void setChannelList(int channelOperator, List<MtkTvChannelInfoBase> chList) {
      TIFChannelManager.getInstance(mContext).updateMapsChannelInfoByList(chList);
      mtkTvChList.setChannelList(channelOperator, chList);
  }

    public void setChannelSwap(List<MtkTvChannelInfoBase> chList, int firstId, int secondId) {
        TIFChannelManager.getInstance(mContext).updateMapsChannelInfoByList(chList);
        MtkTvChannelList.getInstance().swapChannelInfo(firstId, secondId, getSvl());
    }

    public void setBlockAll(List<MtkTvChannelInfoBase> chList, boolean blocked) {
        TIFChannelManager.getInstance(mContext).updateMapsChannelInfoByList(chList);
        MtkTvChannelList.getInstance().channellistBlockAll(getSvl(), blocked);
    }

/*  This api allow channel move ,
 * E.g ch1,ch2,ch3,ch4,ch5 ,ch2 and ch5 replase, result:ch1,ch5,ch2,ch3,ch4,
 * @param chNumbegin start channelid
 * @param chNumEnd end channelid
 * */
  public void setChannelListForfusion(int chNumbegin, int chNumEnd ) {
      mtkTvChList.slideForOnePartChannelId(getSvl(),chNumbegin, chNumEnd);
  }
  /*
   * chlist filter default :MtkTvChannelList.CHLST_FLT_ACTIVE | MtkTvChannelList.CHLST_FLT_VISIBLE
   * chId: ch cursor prevCount: prev chId channel count,except chId itself. nextCount: next chId
   * channel count, if chId exist contain itself ,or not contain. note: default filter fake channel;
   */
  public List<MtkTvChannelInfoBase> getChannelList(int chId, int prevCount, int nextCount,
      int filter) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList chId =" + chId + "pre =" + prevCount + "nextCOunt ="
        + nextCount + "filter =" + filter);
    List<MtkTvChannelInfoBase> chList = mtkTvChList.getChannelListByFilter(getSvl(),
        filter, chId, prevCount, nextCount);
    // fakefilter(chId,prevCount,nextCount);
    /*
     * new ArrayList<MtkTvChannelInfoBase>(); mtkTvChList.getChannelListByFilter(getSvl(),
     * getDefFilter(),chId,prevCount, nextCount);
     */
    if (chList != null && !chList.isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList chId = " + chId + "chList.get(0).getchId"
          + chList.get(0).getChannelId());
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList chId = " + chId + "chList == null,or size == 0");
    }

    return chList;
  }

  private List<MtkTvChannelInfoBase> getChannelListByFakefilter(int chId, int prevCount,
      int nextCount) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chId" + chId + "prevCount =" + prevCount
        + "nextCount =" + nextCount);
    List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
    List<MtkTvChannelInfoBase> chListPre = new ArrayList<MtkTvChannelInfoBase>();
    List<MtkTvChannelInfoBase> chListNext = new ArrayList<MtkTvChannelInfoBase>();

    int chLen = mtkTvChList.getChannelCountByFilter(getSvl(), getChListFilter());
    int curChId = MtkTvConfig.getInstance().getConfigValue(CUR_CHANNEL_ID);
    if (chLen <= 0) {
      return null;
    }
    int tmpLen = 7;

    int loopTime = chLen / tmpLen + (chLen % tmpLen == 0 ? 0 : 1);

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chLen = " + chLen + "loopTime =" + loopTime);
    boolean loopEnd = false;

    if (chId >= 0) {
      if (prevCount > 0) {
        int loopPreId = chId;
        int loops = loopTime;
        while ((loops--) > 0 || !loopEnd) {
          List<MtkTvChannelInfoBase> chListTmp = mtkTvChList.getChannelListByFilter(
                  getSvl(), getChListFilter(), loopPreId, chLen, 0);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre  loops = " + loops + "loopEnd ="
              + loopEnd + "loopPreId =" + loopPreId + " pre chListTmp size ="
              + (chListTmp == null ? 0 : chListTmp.size()));
          if (chListTmp != null && !chListTmp.isEmpty()) {
              for (int index = chListTmp.size() - 1; index >= 0; index--) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CH_LIST_ANALOG_MASK pre" + chListTmp.get(index).getNwMask()+" "+(chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) );
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre loop info id ="
                          + chListTmp.get(index).getChannelId() + "number = "
                          + chListTmp.get(index).getChannelNumber() + "nwMask ="
                          + Integer.toHexString(chListTmp.get(index).getNwMask()));
                if(isdualtunermode() && (chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) == CH_LIST_ANALOG_VAL ){
                    chListTmp.remove(index);
                }

            }
            loopPreId = chListTmp.get(0).getChannelId();
            for (int index = chListTmp.size() - 1; index >= 0; index--) {
              MtkTvChannelInfoBase chTmp = chListTmp.get(index);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                  "getChannelListByFakefilter Prev chTmp.getChannelId()=" + chTmp.getChannelId());
              if (chListPre != null
                  && !chListPre.isEmpty()
                  && chTmp.getChannelId() == chListPre.get(chListPre.size() - 1)
                      .getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Prev LoopEnd");
                loopEnd = true;
                break;
              } else if (chTmp.getChannelId() == chId) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter loop to start id LoopEnd");
                loopEnd = true;
                break;
              } else if ((chTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                    != MtkTvChCommonBase.SB_VNET_FAKE
                  || curChId == chTmp.getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Prev loop chListPre size = "
                    + chListPre.size());
                if (chListPre.size() < prevCount) {
                  chListPre.add(0, chTmp);
                } else {
                  loops = 0;
                  break;
                }
              }
            }
            if (chListTmp.size() < tmpLen) {
            //  loopEnd = true; fix DTV00935933
              break;
            }
          } else {
            break;
          }
        }
      }

      loopEnd = false;
      if (nextCount > 0) {
        int loops = loopTime;
        int loopNextId = chId + 1;
        while ((loops--) > 0 && !loopEnd) {
          List<MtkTvChannelInfoBase> chListTmp = mtkTvChList.getChannelListByFilter(
                  getSvl(), getChListFilter(), loopNextId, 0, chLen);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next  loops = " + loops + "loopEnd ="
              + loopEnd + "loopNextId =" + loopNextId + " pre chListTmp size ="
              + (chListTmp == null ? 0 : chListTmp.size()));
          if (chListTmp != null && !chListTmp.isEmpty()) {
              for (int index = chListTmp.size() - 1; index >= 0; index--) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CH_LIST_ANALOG_MASK next" + chListTmp.get(index).getNwMask()+" "+(chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) );
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre loop info id ="
                          + chListTmp.get(index).getChannelId() + "number = "
                          + chListTmp.get(index).getChannelNumber() + "nwMask ="
                          + Integer.toHexString(chListTmp.get(index).getNwMask()));
                  if(isdualtunermode() && (chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) == CH_LIST_ANALOG_VAL ){
                    chListTmp.remove(index);
                }

            }
            loopNextId = chListTmp.get(chListTmp.size() - 1).getChannelId() + 1;
            for (MtkTvChannelInfoBase chTmp : chListTmp) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next chTmp.getChannelId()="
                  + chTmp.getChannelId());
              if (chListNext != null && !chListNext.isEmpty() &&
                  chTmp.getChannelId() == chListNext.get(0).getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next LoopEnd");
                loopEnd = true;
                break;
              } else if (chTmp.getChannelId() == chId) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter loop to start id LoopEnd");
                loopEnd = true;
                break;
              } else if ((chTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                    != MtkTvChCommonBase.SB_VNET_FAKE
                  || curChId == chTmp.getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next loop chListNext size = "
                    + chListNext.size());
                if (chListNext.size() < nextCount) {
                  chListNext.add(chTmp);
                } else {
                  loops = 0;
                  break;
                }
              }
            }
            if (chListTmp.size() < tmpLen) {
              loopEnd = true;
              break;
            }
          } else {
            loopEnd = true;
            break;
          }
        }
      }
    }

    MtkTvChannelInfoBase baseCh = getChannelById(chId);

    if (baseCh != null) {
      chList.addAll(chListPre);
      chList.add(baseCh);
      chList.addAll(chListNext);
    } else {
      chList.addAll(chListPre);
      chList.addAll(chListNext);
    }

    if (chListPre != null && !chListPre.isEmpty() && chListNext != null
        && !chListNext.isEmpty()) {
      int pre = 0;
      for (MtkTvChannelInfoBase tmpPreInfo : chListPre) {
        for (MtkTvChannelInfoBase tmpNextInfo : chListNext) {
          if (tmpPreInfo.getChannelId() == tmpNextInfo.getChannelId()) {
            chList.remove(pre);
          }
        }
        pre += 1;
      }
    }

    if (chList != null && chList.size() > prevCount + nextCount) {
      chList.remove(chList.size() - 1);
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chListPre = " + chListPre);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter baseCh = " + baseCh);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chListNext = " + chListNext);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chList = " + chList);
    return chList;

  }

  public List<MtkTvChannelInfoBase> getChannelListByMaskFilter(int chId, int dir, int count,
      int mask, int val) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByMaskFilter chId = " + chId + "  dir = " + dir + " count = "
        + count + "mask = " + mask + " val = " + val);

    int chLen = mtkTvChList.getChannelCountByMask(getSvl(), mask, val);
    if (chLen <= 0) {
      return new ArrayList<MtkTvChannelInfoBase>();
    }

    return mtkTvChList.getChannelListByMask(getSvl(), mask, val, dir,
        chId, count > chLen ? chLen : count);
  }

  /**
   * get channel list by query info
   *
   * @param mtkTvChannelQuery
   * @return
   */
  public List<MtkTvChannelInfoBase> getChnnelListByQueryInfo(MtkTvChannelQuery mtkTvChannelQuery) {
    return mtkTvChList.getChannelListByQueryInfo(getSvl(),
        mtkTvChannelQuery);
  }

  /**
   * for EPG
   *
   * @param chId
   * @param prevCount
   * @param nextCount
   * @return
   */
  private List<MtkTvChannelInfoBase> getChannelListByFakefilter(int chId, int prevCount,
      int nextCount, int filter) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chId" + chId + "prevCount =" + prevCount
        + "nextCount =" + nextCount);
    List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
    List<MtkTvChannelInfoBase> chListPre = new ArrayList<MtkTvChannelInfoBase>();
    List<MtkTvChannelInfoBase> chListNext = new ArrayList<MtkTvChannelInfoBase>();
    int chLen = mtkTvChList.getChannelCountByFilter(getSvl(), filter);
    int curChId = getCurrentChannelId();
    if (chLen <= 0) {
      return null;
    }
    int tmpLen = 7;
    int loopTime = chLen / tmpLen + (chLen % tmpLen == 0 ? 0 : 1);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chLen = " + chLen + "loopTime =" + loopTime);
    boolean loopEnd = false;
    if (chId >= 0) {
      if (prevCount > 0) {
        int loopPreId = chId;
        int loops = loopTime;
        while ((loops--) > 0 || !loopEnd) {
          List<MtkTvChannelInfoBase> chListTmp = mtkTvChList.getChannelListByFilter(
              getSvl(), filter, loopPreId, tmpLen, 0);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre  loops = " + loops + "loopEnd ="
              + loopEnd + "loopPreId =" + loopPreId + " pre chListTmp size ="
              + (chListTmp == null ? 0 : chListTmp.size()));
          if (chListTmp != null && !chListTmp.isEmpty()) {
            for (MtkTvChannelInfoBase info : chListTmp) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre loop info id ="
                  + info.getChannelId() + "number = "
                  + info.getChannelNumber() + "nwMask ="
                  + Integer.toHexString(info.getNwMask()));
            }
            loopPreId = chListTmp.get(0).getChannelId();
            for (int index = chListTmp.size() - 1; index >= 0; index--) {
              MtkTvChannelInfoBase chTmp = chListTmp.get(index);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Prev chTmp.getChannelId()="
                  + chTmp.getChannelId());
              if (chListPre != null
                  && !chListPre.isEmpty()
                  && chTmp.getChannelId() == chListPre.get(chListPre.size() - 1)
                      .getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Prev LoopEnd");
                loopEnd = true;
              } else if (chTmp.getChannelId() == chId) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter loop to start id LoopEnd");
                loopEnd = true;
              } else if ((chTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                    != MtkTvChCommonBase.SB_VNET_FAKE
                  || curChId == chTmp.getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "getChannelListByFakefilter Prev loop chListPre size = " + chListPre.size());
                if (chListPre.size() < prevCount) {
                  chListPre.add(0, chTmp);
                } else {
                  loops = 0;
                }
              }
            }
            if (chListTmp.size() < tmpLen) {
              loopEnd = true;
            }
          } else {
            loopEnd = true;
          }
        }
      }
      loopEnd = false;
      if (nextCount > 0) {
        int loops = loopTime;
        int loopNextId = chId + 1;
        while ((loops--) > 0 && !loopEnd) {
          List<MtkTvChannelInfoBase> chListTmp = mtkTvChList.getChannelListByFilter(
              getSvl(), filter, loopNextId, 0, tmpLen);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next  loops = " + loops + "loopEnd ="
              + loopEnd + "loopNextId =" + loopNextId + " pre chListTmp size ="
              + (chListTmp == null ? 0 : chListTmp.size()));
          if (chListTmp != null && !chListTmp.isEmpty()) {
            for (MtkTvChannelInfoBase info : chListTmp) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next loop info id ="
                  + info.getChannelId() + "number = "
                  + info.getChannelNumber() + "nwMask ="
                  + Integer.toHexString(info.getNwMask()));
            }
            loopNextId = chListTmp.get(chListTmp.size() - 1).getChannelId() + 1;
            for (MtkTvChannelInfoBase chTmp : chListTmp) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                  "getChannelListByFakefilter Next chTmp.getChannelId()=" + chTmp.getChannelId());
              if (chListNext != null && !chListNext.isEmpty() &&
                  chTmp.getChannelId() == chListNext.get(0).getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next LoopEnd");
                loopEnd = true;
              } else if (chTmp.getChannelId() == chId) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter loop to start id LoopEnd");
                loopEnd = true;
              } else if ((chTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                    != MtkTvChCommonBase.SB_VNET_FAKE
                  || curChId == chTmp.getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next loop chListNext size = "
                    + chListNext.size());
                if (chListNext.size() < nextCount) {
                  chListNext.add(chTmp);
                } else {
                  loops = 0;
                }
              }
            }
            if (chListTmp.size() < tmpLen) {
              loopEnd = true;
            }
          } else {
            loopEnd = true;
          }
        }
      }
    }
    MtkTvChannelInfoBase baseCh = getChannelById(chId);
    if (baseCh != null) {
      chList.addAll(chListPre);
      chList.add(baseCh);
      chList.addAll(chListNext);
    } else {
      chList.addAll(chListPre);
      chList.addAll(chListNext);
    }
    if (chListPre != null && !chListPre.isEmpty() && chListNext != null
        && !chListNext.isEmpty()) {
      int pre = 0;
      for (MtkTvChannelInfoBase tmpPreInfo : chListPre) {
        for (MtkTvChannelInfoBase tmpNextInfo : chListNext) {
          if (tmpPreInfo.getChannelId() == tmpNextInfo.getChannelId()) {
            chList.remove(pre);
          }
        }
        pre += 1;
      }
    }
    if (chList != null && chList.size() > prevCount + nextCount) {
      chList.remove(chList.size() - 1);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chListPre = " + chListPre);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter baseCh = " + baseCh);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chListNext = " + chListNext);
    return chList;
  }

  /**
   * for Mask EU
   *
   * @param chId
   * @param prevCount
   * @param nextCount
   * @return
   */
  public List<MtkTvChannelInfoBase> getChannelListByMaskValuefilter(int chId, int prevCount,
      int nextCount, int filter, int value, boolean contaisBaseCh) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByMaskValuefilter chId" + chId + "prevCount =" + prevCount
        + "nextCount =" + nextCount);
    List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
    List<MtkTvChannelInfoBase> chListPre = new ArrayList<MtkTvChannelInfoBase>();
    List<MtkTvChannelInfoBase> chListNext = new ArrayList<MtkTvChannelInfoBase>();
    int chLen = mtkTvChList.getChannelCountByFilter(getSvl(), filter);
    if (chLen <= 0) {
      return null;
    }
    int tmpLen = 7;
    int loopTime = chLen / tmpLen + (chLen % tmpLen == 0 ? 0 : 1);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chLen = " + chLen + "loopTime =" + loopTime);
    boolean loopEnd = false;
    if (chId >= 0) {
      if (prevCount > 0) {
        int loopPreId = chId;
        int loops = loopTime;
        while ((loops--) > 0 || !loopEnd) {
          List<MtkTvChannelInfoBase> chListTmp = mtkTvChList.getChannelListByFilter(
                  getSvl(), filter, loopPreId, chLen, 0);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre  loops = " + loops + "loopEnd ="
              + loopEnd + "loopPreId =" + loopPreId + " pre chListTmp size ="
              + (chListTmp == null ? 0 : chListTmp.size()));
          if (chListTmp != null && !chListTmp.isEmpty()) {
              for (int index = chListTmp.size() - 1; index >= 0; index--) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "eu CH_LIST_ANALOG_MASK pre" + chListTmp.get(index).getNwMask()+" "+(chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) );
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre loop info id ="
                          + chListTmp.get(index).getChannelId() + "number = "
                          + chListTmp.get(index).getChannelNumber() + "nwMask ="
                          + Integer.toHexString(chListTmp.get(index).getNwMask()));
                  if(isdualtunermode() && (chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) == CH_LIST_ANALOG_VAL ){
                    chListTmp.remove(index);
                }
            }
            loopPreId = chListTmp.get(0).getChannelId();
            for (int index = chListTmp.size() - 1; index >= 0; index--) {
              MtkTvChannelInfoBase chTmp = chListTmp.get(index);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Prev chTmp.getChannelId()="
                  + chTmp.getChannelId());
              if (chListPre != null && !chListPre.isEmpty() &&
                  chTmp.getChannelId() == chListPre.get(chListPre.size() - 1).getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Prev LoopEnd");
                loopEnd = true;
              } else if (chTmp.getChannelId() == chId) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter loop to start id LoopEnd");
                loopEnd = true;
              } else if ((chTmp.getNwMask() & filter) == value) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "getChannelListByFakefilter Prev loop chListPre size = " + chListPre.size());
                if (chListPre.size() < prevCount) {
                  chListPre.add(0, chTmp);
                } else {
                  loops = 0;
                }
              }
            }
            if (chListTmp.size() < tmpLen) {
              loopEnd = true;
            }
          } else {
            loopEnd = true;
          }
        }
      }
      loopEnd = false;
      if (nextCount > 0) {
        int loops = loopTime;
        int loopNextId = chId + 1;
        while ((loops--) > 0 && !loopEnd) {
          List<MtkTvChannelInfoBase> chListTmp = mtkTvChList.getChannelListByFilter(
                  getSvl(), filter, loopNextId, 0, chLen);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next  loops = " + loops + "loopEnd ="
              + loopEnd + "loopNextId =" + loopNextId + " pre chListTmp size ="
              + (chListTmp == null ? 0 : chListTmp.size()));
          if (chListTmp != null && !chListTmp.isEmpty()) {
              for (int index = chListTmp.size() - 1; index >= 0; index--) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "eu CH_LIST_ANALOG_MASK next" + chListTmp.get(index).getNwMask() +" "+(chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK));
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Pre loop info id ="
                          + chListTmp.get(index).getChannelId() + "number = "
                          + chListTmp.get(index).getChannelNumber() + "nwMask ="
                          + Integer.toHexString(chListTmp.get(index).getNwMask()));
                  if(isdualtunermode() &&  (chListTmp.get(index).getNwMask() & CH_LIST_ANALOG_MASK) == CH_LIST_ANALOG_VAL ){
                   chListTmp.remove(index);
               }

            }
            loopNextId = chListTmp.get(chListTmp.size() - 1).getChannelId() + 1;
            for (MtkTvChannelInfoBase chTmp : chListTmp) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                  "getChannelListByFakefilter Next chTmp.getChannelId()=" + chTmp.getChannelId());
              if (chListNext != null && !chListNext.isEmpty() &&
                  chTmp.getChannelId() == chListNext.get(0).getChannelId()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next LoopEnd");
                loopEnd = true;
              } else if (chTmp.getChannelId() == chId) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter loop to start id LoopEnd");
                loopEnd = true;
              } else if ((chTmp.getNwMask() & filter) == value) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter Next loop chListNext size = "
                    + chListNext.size());
                if (chListNext.size() < nextCount) {
                  chListNext.add(chTmp);
                } else {
                  loops = 0;
                }
              }
            }
            if (chListTmp.size() < tmpLen) {
              loopEnd = true;
            }
          } else {
            loopEnd = true;
          }
        }
      }
    }
    MtkTvChannelInfoBase baseCh = getChannelById(chId);
    if (baseCh != null && contaisBaseCh) {
      chList.addAll(chListPre);
      chList.add(baseCh);
      chList.addAll(chListNext);
    } else {
      chList.addAll(chListPre);
      chList.addAll(chListNext);
    }
    // if (chListPre != null && chListPre.size() > 0 && chListNext != null
    // && chListNext.size() > 0) {
    // int pre = 0;
    // for (MtkTvChannelInfoBase tmpPreInfo : chListPre) {
    // for (MtkTvChannelInfoBase tmpNextInfo : chListNext) {
    // if (tmpPreInfo.getChannelId() == tmpNextInfo.getChannelId()) {
    // chList.remove(pre);
    // }
    // }
    // pre += 1;
    // }
    // }
    if (chList != null && chList.size() > prevCount + nextCount) {
      chList.remove(chList.size() - 1);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chListPre = " + chListPre);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter baseCh = " + baseCh);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByFakefilter chListNext = " + chListNext);
    return chList;
  }

  /*
   * chlist filter default :MtkTvChannelList.CHLST_FLT_ACTIVE | MtkTvChannelList.CHLST_FLT_VISIBLE
   * chId: ch cursor prevCount: prev chId channel count,except chId itself. nextCount: next chId
   * channel count, if chId exist contain itself ,or not contain.
   */
  public MtkTvChannelInfoBase getChannelById(int chId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelById chID = " + chId);
    List<MtkTvChannelInfoBase> chList = getChannelList(chId, 0, 1, MtkTvChCommonBase.SB_VNET_ALL);

    MtkTvChannelInfoBase chInfo = null;
    if (chList != null && !chList.isEmpty()) {
      if (chId == chList.get(0).getChannelId()) {
        chInfo = chList.get(0);
      }
    }
    if (chInfo != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelById chId = " + chId + "chInfo.getchId" + chInfo.getChannelId());
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList chId = " + chId + "chInfo == null");
    }

    return chInfo;
  }


    public MtkTvChannelInfoBase getChannelByRecId(int svlId, int svlRecId) {
        Log.d(TAG, "getChannelByRecId: svlId :" + svlId + "svlRecId :" + svlRecId);
        MtkTvChannelInfoBase chInfo = MtkTvChannelList.getInstance().getChannelInfoBySvlRecId(svlId, svlRecId);
        if (chInfo != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelByRecId chId = " + "chInfo.getchId" + chInfo.getChannelId());
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelByRecId chId = " + "chInfo == null");
        }

        return chInfo;
    }

  // just for test skip SA minorNo channel.
  //public static final String WW_SKIP_MINOR = "ww.sa.skip";

  /*
   * only for ch+/- to get up or down channelInfo. US/EU:loop get first not skip channel SA option
   * (true): loop get first not skip majorNo channel. tip: major/minor only for SA (ISDB) DTV
   * Channel, true(UP),false(Down);
   */
  public MtkTvChannelInfoBase getUpDownChInfo(boolean prev) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo prev =" + prev);
    int chLen = mtkTvChList.getChannelCountByFilter(getSvl(), getChUpDownFilter());
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo chLen =" + chLen);
    if (chLen <= 1) {
      return null;
    }
    // this option for skip SA minorNo channel.
    final boolean isOption = false;
    //if (getProperty(WW_SKIP_MINOR) == 1) {
    //  isOption = true;
    //}
    // MtkTvChannelInfoBase chInfo = null;
    int curChId = getCurrentChannelId();// getInstanceMtkTvBroadcast().getCurrentChannelId();
    int tmpLen = 7;
    int loopTime = chLen / tmpLen + (chLen % tmpLen == 0 ? 0 : 1);
    MtkTvChannelInfoBase curChInfo = getChannelById(curChId);
    int loopChId = prev ? curChId : curChId + 1;
    int prevCount = prev ? tmpLen : 0;
    int nextCount = prev ? 0 : tmpLen;

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chLen = " + chLen + "loopTime =" + loopTime);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo prev =" + prev + " curChId =" + curChId + " prevCount ="
        + prevCount + "nextCount =" + nextCount);
    while ((loopTime--) > 0) {
      List<MtkTvChannelInfoBase> chList = getChannelList(loopChId, prevCount, nextCount,
          getChUpDownFilter());
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo loopTime =" + loopTime + "chList size ="
          + (chList != null ? chList.size() : 0));
      if (chList != null && !chList.isEmpty()) {
        if (prev) {
          loopChId = chList.get(0).getChannelId();
        } else {
          loopChId = chList.get(chList.size() - 1).getChannelId() + 1;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo loop loopChId = " + loopChId + " chList.size() ="
            + chList.size());
        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
            && isOption && curChInfo instanceof MtkTvISDBChannelInfo) {
          MtkTvISDBChannelInfo isdbCurChInfo = (MtkTvISDBChannelInfo) curChInfo;
          List<MtkTvChannelInfoBase> tmpList = new ArrayList<MtkTvChannelInfoBase>();
          for (MtkTvChannelInfoBase info : chList) {
            if (info instanceof MtkTvISDBChannelInfo) {
              MtkTvISDBChannelInfo isdbChInfo = (MtkTvISDBChannelInfo) info;
              if (isdbChInfo.getMajorNum() != isdbCurChInfo.getMajorNum()) {
                tmpList.add(info);
              }
            } else {
              tmpList.add(info);
            }
          }
          if (tmpList != null && !tmpList.isEmpty()) {
            chList = tmpList;
          } else {
            continue;
          }
        }

        // for (MtkTvChannelInfoBase info : chList) {
        // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo loop info id =" + info.getChannelId() + "number = "
        // + info.getChannelNumber() + "nwMask ="
        // + Integer.toHexString(info.getNwMask()));
        // }

        if (prev) {
          for (int index = chList.size() - 1; index >= 0; index--) {
            MtkTvChannelInfoBase upTmp = chList.get(index);
            if (upTmp.getChannelId() == curChId) {
              return null;
            } else if (!upTmp.isSkip()
                && (upTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                  != MtkTvChCommonBase.SB_VNET_FAKE
                && (upTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_VISIBLE)
                  == MtkTvChCommonBase.SB_VNET_VISIBLE
                && (upTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_ACTIVE)
                  == MtkTvChCommonBase.SB_VNET_ACTIVE) {
              return upTmp;

            }
          }
        } else {
          for (MtkTvChannelInfoBase downTmp : chList) {
            if (downTmp.getChannelId() == curChId) {
              return null;
            } else if (!downTmp.isSkip()
                && (downTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                  != MtkTvChCommonBase.SB_VNET_FAKE
                && (downTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_VISIBLE)
                  == MtkTvChCommonBase.SB_VNET_VISIBLE
                && (downTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_ACTIVE)
                  == MtkTvChCommonBase.SB_VNET_ACTIVE) {
              return downTmp;
            }
          }
        }
      } else {
        loopTime = 0;
      }
    }
    return null;
  }

  /*
   * only for ch+/- to get up or down channelInfo. US/EU:loop get first not skip channel SA option
   * (true): loop get first not skip majorNo channel. tip: major/minor only for SA (ISDB) DTV
   * Channel, true(UP),false(Down);
   */
  public MtkTvChannelInfoBase getUpDownChInfoByMask(boolean prev, int mask, int val) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfoByMask prev =" + prev);
    int chLen = mtkTvChList.getChannelCountByMask(getSvl(), mask, val);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfoByMask chLen =" + chLen);
    if (chLen < 1) {
      return null;
    }

    // this option for skip SA minorNo channel.
    final boolean isOption = false;
    //if (getProperty(WW_SKIP_MINOR) == 1) {
    //  isOption = true;
    //}
    int curChId = getCurrentChannelId();// getInstanceMtkTvBroadcast().getCurrentChannelId();
    MtkTvChannelInfoBase curChInfo = getChannelById(curChId);
    MtkTvChannelInfoBase tmpChInfo = null;

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfoByMask isOption =" + isOption + " curChId = " + curChId);

    while ((chLen--) > 0) {
      List<MtkTvChannelInfoBase> chInfoList = mtkTvChList.getChannelListByMask(getSvl(), mask, val,
          prev ? MtkTvChannelList.CHLST_ITERATE_DIR_NEXT : MtkTvChannelList.CHLST_ITERATE_DIR_PREV,
          curChId, 1);
      if (chInfoList != null && !chInfoList.isEmpty()) {
        tmpChInfo = chInfoList.get(0);
      }
      if (tmpChInfo != null
          && MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
          && isOption && curChInfo instanceof MtkTvISDBChannelInfo
          && tmpChInfo instanceof MtkTvISDBChannelInfo) {
        if (((MtkTvISDBChannelInfo) curChInfo).getMajorNum() != ((MtkTvISDBChannelInfo) tmpChInfo)
            .getMajorNum()) {
          return tmpChInfo;
        }
      }
    }

    return tmpChInfo;
  }

  /*
   * only for ch+/- to get up or down channelInfo. US/EU:loop get first not skip channel SA option
   * (true): loop get first not skip majorNo channel. tip: major/minor only for SA (ISDB) DTV
   * Channel, true(UP),false(Down);
   */
  public MtkTvChannelInfoBase getUpDownChInfoByFilter(boolean prev, int filter) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo prev =" + prev);
    int chLen = mtkTvChList.getChannelCountByFilter(getSvl(), filter);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo chLen =" + chLen);
    if (chLen <= 1) {
      return null;
    }
    // this option for skip SA minorNo channel.
    final boolean isOption = false;
    //if (getProperty(WW_SKIP_MINOR) == 1) {
    //  isOption = true;
    //}
    // MtkTvChannelInfoBase chInfo = null;
    int curChId = getCurrentChannelId();// getInstanceMtkTvBroadcast().getCurrentChannelId();
    int tmpLen = 7;
    int loopTime = chLen / tmpLen + (chLen % tmpLen == 0 ? 0 : 1);
    MtkTvChannelInfoBase curChInfo = getChannelById(curChId);
    int loopChId = prev ? curChId : curChId + 1;
    int prevCount = prev ? tmpLen : 0;
    int nextCount = prev ? 0 : tmpLen;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chLen = " + chLen + "loopTime =" + loopTime);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo prev =" + prev + " curChId =" + curChId + " prevCount ="
        + prevCount + "nextCount =" + nextCount);
    while ((loopTime--) > 0) {
      List<MtkTvChannelInfoBase> chList = getChannelList(loopChId, prevCount, nextCount,
          filter);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo loopTime =" + loopTime + "chList size ="
          + (chList != null ? chList.size() : 0));
      if (chList != null && !chList.isEmpty()) {
        if (prev) {
          loopChId = chList.get(0).getChannelId();
        } else {
          loopChId = chList.get(chList.size() - 1).getChannelId() + 1;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo loop loopChId = " + loopChId + " chList.size() ="
            + chList.size());
        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
            && isOption && curChInfo instanceof MtkTvISDBChannelInfo) {
          MtkTvISDBChannelInfo isdbCurChInfo = (MtkTvISDBChannelInfo) curChInfo;
          List<MtkTvChannelInfoBase> tmpList = new ArrayList<MtkTvChannelInfoBase>();
          for (MtkTvChannelInfoBase info : chList) {
            if (info instanceof MtkTvISDBChannelInfo) {
              MtkTvISDBChannelInfo isdbChInfo = (MtkTvISDBChannelInfo) info;
              if (isdbChInfo.getMajorNum() != isdbCurChInfo.getMajorNum()) {
                tmpList.add(info);
              }
            } else {
              tmpList.add(info);
            }
          }
          if (tmpList != null && !tmpList.isEmpty()) {
            chList = tmpList;
          } else {
            continue;
          }
        }
        for (MtkTvChannelInfoBase info : chList) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUpDownChInfo loop info id =" + info.getChannelId() + "number = "
              + info.getChannelNumber() + "nwMask ="
              + Integer.toHexString(info.getNwMask()));
        }
        if (prev) {
          for (int index = chList.size() - 1; index >= 0; index--) {
            MtkTvChannelInfoBase upTmp = chList.get(index);
            if (upTmp.getChannelId() == curChId) {
              return null;
            } else if (!upTmp.isSkip()
                && (upTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                  != MtkTvChCommonBase.SB_VNET_FAKE
                && (upTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_VISIBLE)
                  == MtkTvChCommonBase.SB_VNET_VISIBLE
                && (upTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_EPG)
                  == MtkTvChCommonBase.SB_VNET_EPG) {
              return upTmp;
            }
          }
        } else {
          for (MtkTvChannelInfoBase downTmp : chList) {
            if (downTmp.getChannelId() == curChId) {
              return null;
            } else if (!downTmp.isSkip()
                && (downTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE)
                  != MtkTvChCommonBase.SB_VNET_FAKE
                && (downTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_VISIBLE)
                  == MtkTvChCommonBase.SB_VNET_VISIBLE
                && (downTmp.getNwMask() & MtkTvChCommonBase.SB_VNET_EPG)
                  == MtkTvChCommonBase.SB_VNET_EPG) {
              return downTmp;
            }
          }
        }
      } else {
        loopTime = 0;
      }
    }
    return null;
  }

  /*
   * for ChannelListDialog get channel list use defFilter.
   */
  public List<MtkTvChannelInfoBase> getChListByMask(int chId, int dir, int count, int mask,
      int val) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChListByMask chId =" + chId + "dir =" + dir + " count = " + count
        + " mask = " + mask + " val = " + val);
    return getChannelListByMaskFilter(chId, dir, count, mask, val);
    // fakefilter(getCurChInfo().getChannelId(),pageSize, 0);
  }

  public List<MtkTvChannelInfoBase> getChannelListByMaskAndSat(int chId, int dir, int count,
      int mask, int val, int satRecId) {
    int chLen = mtkTvChList.getChannelCountByMaskAndSat(getSvl(), mask, val, satRecId);
    if (chLen <= 0) {
      return null;
    }
    List<MtkTvChannelInfoBase> chList = mtkTvChList.getChannelListByMaskAndSat(getSvl(), mask, val,
        satRecId, dir, chId, count > chLen ? chLen : count);
    for (MtkTvChannelInfoBase chInfo : chList) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListByMaskAndSat chInfo = " + chInfo);
    }
    return chList;
  }

    public List<MtkTvChannelInfoBase> getFavoriteListByFilter(int filter, int channelId,
            boolean isCurChannel,int prevCount, int nextCount,int favtype) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavoriteListByFilter channelId =" + channelId);
        int chLen = mtkTvChList.getChannelCountByFilter(getSvl(), filter);
        if (chLen <= 0) {
            return null;
        }
        List<MtkTvChannelInfoBase> chList = mtkTvChList.getFavoriteListByFilter(getSvl(), filter,
                channelId, isCurChannel,prevCount, nextCount,favtype);

        if(isCurrentSourceATVforEuPA()){
            for(MtkTvChannelInfoBase info : chList){
                info.setChannelNumber(getAnalogChannelDisplayNumInt(info.getChannelNumber()));
            }
         }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavoriteListByFilter chList.size() =" + chList.size());
        return chList;
    }

    public int getFavoriteCount(int svlId, int mask,int value){
        //int count = mtkTvChList.favoriteListNumber(svlId,favIndex);
      return mtkTvChList.getChannelCountByMask(svlId,mask,value);
    }
  /*
   * for ChannelListDialog get channel list use defFilter.
   */
  public List<MtkTvChannelInfoBase> getChList(int chId, int prevCount, int nextCount) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChList chId =" + chId + "prevCount =" + prevCount + "nextCount = "
        + nextCount);
    return getChannelListByFakefilter(chId, prevCount, nextCount);
    // fakefilter(getCurChInfo().getChannelId(),pageSize,
  }


  public List<MtkTvChannelInfoBase> getChannelList() {
      if (isSARegion()) {
        int length = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ALL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList length " + length);
        return getChList(0, 0, length);
      } else {
        int length = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ALL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList length " + length);
        return getChannelList(0, 0, length, MtkTvChCommonBase.SB_VNET_ALL);
      }
    }
  public List<MtkTvChannelInfoBase> getChannelListForMap() {
        int length = mtkTvChList.getChannelCountByFilter(getSvl(), MtkTvChCommonBase.SB_VNET_ALL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList length " + length);
        return getChannelList(0, 0, length, MtkTvChCommonBase.SB_VNET_ALL);
    }

  /*
   * for EPGChannel list Filter.
   */
  public List<MtkTvChannelInfoBase> getEPGChList(int chId, int prevCount, int nextCount) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChList chId =" + chId + "prevCount =" + prevCount + "nextCount = "
        + nextCount);
    return getChannelListByFakefilter(chId, prevCount, nextCount, getChListFilterEPG());
    // fakefilter(getCurChInfo().getChannelId(),pageSize,
  }

  /**
   * get current focus output, has "main" or "sub"
   *
   * @return
   */
  public String getCurrentFocus() {
    String focusWin = "";
    /*
     * if (mCurrentTvMode != TV_NORMAL_MODE){ mCurrentTvMode =
     * getInstanceMtkTvHighLevel().getCurrentTvMode(); }
     */
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus(), mCurrentTvMode =" + mCurrentTvMode);
    if (mCurrentTvMode == TV_NORMAL_MODE) {
      focusWin = "main";
    } else {
      int result = MtkTvConfig.getInstance().getConfigValue(TV_FOCUS_WIN);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus(), TV_FOCUS_WIN =" + result);
      if (0 == result) {
        focusWin = "main";
      } else if (1 == result) {
        focusWin = "sub";
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentFocus,focusWin ==" + focusWin);
    return focusWin;
  }

  /**
   * set current TV mode
   *
   * @param tvMode :TV_NORMAL_MODE,TV_PIP_MODE,TV_POP_MODE
   */
  public void setCurrentTVMode(int tvMode) {
    MtkTvConfig.getInstance().setConfigValue(TV_MODE, tvMode);
  }

  public int getCurrentScreenMode() {
    return MtkTvConfig.getInstance().getConfigValue(SCREEN_MODE);
  }

  public void setCurrentScreenMode(int screenMode) {
    MtkTvConfig.getInstance().setConfigValue(SCREEN_MODE, screenMode);
  }

  /**
   * @param TVMode is 0== not support 1==support
   */
  public void setSupportThirdPIPMode(int supportMode) {
    MtkTvConfig.getInstance().setConfigValue(THIRD_PIP_MODE, supportMode);

  }

  /**
   * @param chInfo
   * @return
   */
  public String getISDBChannelLogo(MtkTvISDBChannelInfo chInfo) {
    return getChannelLogoPicPath(chInfo.getOnID(), chInfo.getProgId(),
        chInfo.getFrequency());
  }

  /**
   * get current channel Logo picture path for SA TV
   *
   * @param onid
   * @param svcid
   * @param freq
   * @return
   */
  public String getChannelLogoPicPath(int onid, int svcid, int freq) {
    String picturePath = null;
    picturePath = mMtkTvCDTChLogoBase.getChLogoPNGFilePath(onid, svcid, freq);
    return picturePath;

  }

  public boolean isCurrentInputSourceHasSignal() {
    return iCurrentInputSourceHasSignal;
  }

  public void setCurrentInputSourceHasSignal(boolean iCurrentInputSourceHasSignal) {
    this.iCurrentInputSourceHasSignal = iCurrentInputSourceHasSignal;
  }

  public boolean isCurrentTVHasSignal() {
    return iCurrentTVHasSignal;
  }

  public void setCurrentTVHasSignal(boolean iCurrentTVHasSignal) {
    this.iCurrentTVHasSignal = iCurrentTVHasSignal;
  }

  public boolean isVideoScrambled() {
    return isVideoScrambled;
  }

  public void setVideoScrambled(boolean isVideoScrambled) {
    this.isVideoScrambled = isVideoScrambled;
  }

  public boolean isFavListFull() {
    List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList.getInstance()
        .getFavoritelistByFilter();
    for (MtkTvFavoritelistInfoBase channel : list) {
      if (channel.getChannelId() == -1) {
        return false;
      }
    }
    return true;
  }

  public void showFavFullMsg() {
    showCommonInfo(TurnkeyUiMainActivity.getInstance(),
        TurnkeyUiMainActivity.getInstance().getString(R.string.fav_list_is_full));
  }

  public void showCommonInfo(Activity activity, String info) {
    Toast mToast = Toast.makeText(activity.getApplicationContext(), info,
        2000);
    TextView toastTextView = (TextView) (mToast.getView()
        .findViewById(com.android.internal.R.id.message));
    toastTextView.setCompoundDrawablesWithIntrinsicBounds(
        activity.getApplicationContext().getResources()
            .getDrawable(R.drawable.nav_ib_warning_icon), null,
        null, null);
    mToast.show();
  }

  public void closeFavFullMsg() {
    if (mPopup != null && mPopup.isShowing()) {
      mPopup.dismiss();
    }
  }

  /**
   * if the current source has signal or not
   *
   * @return
   */
  public boolean isCurrentSourceHasSignal() {
      return !getInstanceMtkTvBroadcast().isSignalLoss();
  }

  /**
   * return the channel ID when input the channel number of the banner view
   *
   * @param channelNumString
   * @return
   */
  public int getChannelIDByBannerNum(String channelNumString) {
    int result = -1;
    int euPAATVCHNum = 0;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum, the channelNumString = "
        + channelNumString);
    if(TextUtils.isEmpty(channelNumString)){
        return -1;
    }
    if (isCurrentSourceATVforEuPA()) {
        euPAATVCHNum = DECREASE_NUM;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,euPAATVCHNum = " + euPAATVCHNum);
    String[] num = channelNumString.split("-".equals(BannerView.channelNumSeparator) ? BannerView.channelNumSeparator : "\\.");
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,getSvl() =" + getSvl());
    if (num.length > 1 && !num[0].isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,num1 ="
          + Integer.valueOf(num[0]));
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,num2 ="
          + Integer.valueOf(num[1]));
      result = mMtkTvAppTV.getMatchedChannel(getSvl(), true,
          Integer.valueOf(num[0]) + euPAATVCHNum, Integer.valueOf(num[1]));
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,result =" + result);
    } else if(num.length ==1){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,num1 ="
          + Integer.valueOf(num[0]) + ",num2 = null");
      result = mMtkTvAppTV.getMatchedChannel(getSvl(), false,
          Integer.valueOf(num[0]) + euPAATVCHNum, 0);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getChannelIDByBannerNum,result =" + result);
    }
    return result;
  }

  /**
   * get current tv mode,0-->normal,1-->pip,2-->pop;
   *
   * @return
   */
  public int getCurrentTVState() {
    if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
      if (mCurrentTvMode != TV_NORMAL_MODE) {
        mCurrentTvMode = getInstanceMtkTvHighLevel().getCurrentTvMode();
      }
    }
    return mCurrentTvMode;
  }

  public void recordCurrentTvState(int value) {
    if (value < TV_NORMAL_MODE || value > TV_POP_MODE) {
      return;
    }

    mCurrentTvMode = value;
  }

  /**
   * when Ginga mode, ap change output with zoom or screen mode and so,need call this api
   *
   * @param changeState
   */
  public void updateOutputChangeState(String changeState) {
    mMtkTvAppTV.updatedSysStatus(getCurrentFocus(), changeState);
  }

  /**
   * get the satellite count
   *
   * @return
   */
  public int getSatelliteCount() {
    int count = 0;
    count = mMtkTvDvbsConfigBase.getSatlNumRecs(getSvl());
    return count;
  }

  /**
   * get satellite chanel list
   *
   * @param count
   * @return
   */
  public List<MtkTvDvbsConfigInfoBase> getSatelliteListInfo(int count) {
    List<MtkTvDvbsConfigInfoBase> tempResultList = new ArrayList<MtkTvDvbsConfigInfoBase>();
    List<MtkTvDvbsConfigInfoBase> tempSatelliteList = null;
    int satelliteChannelCount = 0;
    for (int i = 0; i < count; i++) {
      tempSatelliteList = mMtkTvDvbsConfigBase.getSatlRecordByRecIdx(getSvl(), i);
      if (tempSatelliteList != null) {
        for (MtkTvDvbsConfigInfoBase tempSatelliteChannel : tempSatelliteList) {
          satelliteChannelCount = getSatelliteChannelCount(tempSatelliteChannel.getSatlRecId(),
              MtkTvChCommonBase.SB_VNET_ALL, MtkTvChCommonBase.SB_VNET_ALL);
          if (satelliteChannelCount > 0) {
            tempResultList.add(tempSatelliteChannel);
          }
        }
      }
    }
    return tempResultList;
  }

  /**
   * get satellite names
   *
   * @param tempResultList
   * @return
   */
  public String[] getSatelliteNames(List<MtkTvDvbsConfigInfoBase> tempResultList) {
    if (tempResultList == null) {
      return new String[0];
    }
    int size = tempResultList.size();
    String[] names = new String[size];
    for (int i = 0; i < size; i++) {
      names[i] = tempResultList.get(i).getSatName();
    }
    return names;
  }

  /**
   * get a default satellite channel
   *
   * @param name
   * @return
   */
  public MtkTvDvbsConfigInfoBase getDefaultSatellite(String name) {
    MtkTvDvbsConfigInfoBase tempSatellite = new MtkTvDvbsConfigInfoBase();
    tempSatellite.setSatlRecId(0);
    tempSatellite.setSatName(name);
    return tempSatellite;
  }

  /**
   * get channel count by satellite record Id
   *
   * @param sateRecordId
   * @return
   */
  public int getSatelliteChannelCount(int sateRecordId, int mask, int val) {
    int count = 0;
    count = mtkTvChList.getChannelCountByMaskAndSat(getSvl(), mask, val, sateRecordId);
    return count;
  }

    /**
     * get channel count by satellite record Id
     *
     * @param sateRecordId
     * @return
     */
    public int getFavouriteChannelCount(int filter) {
        int count = 0;
        count = mtkTvChList.getChannelCountByFilter(getSvl(), filter);
        return count;
    }
    public void favoriteListInsertMove(int favIndex, int idxMoveFrom,int idxMoveTo) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favoriteListInsertMove favIndex" +favIndex +"; idxMoveFrom is "+idxMoveFrom+"; idxMoveTo is "+idxMoveTo);
          mtkTvChList.favoriteListInsertMove(getSvl(), favIndex, idxMoveFrom,idxMoveTo);
       // return count;
    }

    public int getFavoriteIdx(int filter, int channelId, int favIndex){
        int index = 0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavoriteIdx filter" +filter +"; channelId is "+channelId+"; favIndex is "+favIndex);
        index =  mtkTvChList.getFavoriteIdx(getSvl(), filter, channelId,favIndex);
        return index;
    }


    public void favoriteListAddTail(int favIndex, int channelId) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favoriteListAddTail favIndex" +favIndex +"; channelId is "+channelId);
          mtkTvChList.favoriteListAddTail(getSvl(), favIndex,channelId);
      //  return count;
    }
  /**
   * get the available String from the illegal String of the TV_API
   *
   * @param illegalString
   * @return
   */
  public String getAvailableString(String illegalString) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getAvailableString, start illegalString ==" + illegalString);
    String resultString = "";
    if (null != illegalString && !("").equals(illegalString)) {
      byte[] illegalByte = illegalString.getBytes();
      int j = 0;
      byte[] availableByte = new byte[illegalByte.length];
      for (byte mByte : illegalByte) {
        if (((mByte & 0xff) >= 32 && (mByte & 0xff) != 127)
            || ((mByte & 0xff) == 10) || ((mByte & 0xff) == 13)) {
          availableByte[j] = mByte;
          j++;
        }
      }
      if (((availableByte[availableByte.length - 1] & 0xff) == 10)
          || ((availableByte[availableByte.length - 1] & 0xff) == 13)) {
        j--;
      }
      if (null != availableByte) {
        resultString = new String(availableByte, 0, j);
      }
    }
    if(resultString.length() > 61){
        resultString =resultString.substring(0,61)+"...";
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getAvailableString,end resultString ==" + resultString);
    return resultString;
  }

  public String getAvailableStringForProg(String illegalString) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getAvailableString, start illegalString ==" + illegalString);
      String resultString = "";
      if (null != illegalString && !("").equals(illegalString)) {
        byte[] illegalByte = illegalString.getBytes();
        int j = 0;
        byte[] availableByte = new byte[illegalByte.length];
        for (byte mByte : illegalByte) {
          if (((mByte & 0xff) >= 32 && (mByte & 0xff) != 127)
              || ((mByte & 0xff) == 10) || ((mByte & 0xff) == 13)) {
            availableByte[j] = mByte;
            j++;
          }
        }
        if (((availableByte[availableByte.length - 1] & 0xff) == 10)
            || ((availableByte[availableByte.length - 1] & 0xff) == 13)) {
          j--;
        }
        if (null != availableByte) {
          resultString = new String(availableByte, 0, j);
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getAvailableString,end resultString ==" + resultString);
      return resultString;
    }

  /**
   * multi_view set current focus need this api,the focus will be record by ap
   *
   * @param currentFocus
   */
  public void setCurrentFocus(String currentFocus) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in setCurrentFocus,currentFocus ==" + currentFocus);
    if (currentFocus.equals(TV_FOCUS_WIN_MAIN)) {
      MtkTvConfig.getInstance().setConfigValue(TV_FOCUS_WIN, 0);
    } else if (currentFocus.equals(TV_FOCUS_WIN_SUB)) {
      MtkTvConfig.getInstance().setConfigValue(TV_FOCUS_WIN, 1);
    }
  }

  public void setBrdcstType(int brdcstType) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in setBrdcstType, brdcstType = " + brdcstType);
    MtkTvConfig.getInstance().setConfigValue(TV_BRDCST_TYPE, brdcstType);
  }
  public int getBrdcstType() {
      int  brdcstType= MtkTvConfig.getInstance().getConfigValue(TV_BRDCST_TYPE);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getConfigValue, brdcstType = " + brdcstType);
      return brdcstType;
    }
  /**
   * restore Fine tune
   */
  public void restoreFineTune(float mRestoreHz) {
    MtkTvChannelInfoBase channel = getCurChInfo();
    channel.setFrequency((int) (mRestoreHz * 1000000));
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    list.add(channel);
    setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "restoreFineTune,mRestoreHz =" + mRestoreHz);
    if (getCurrentFocus().equalsIgnoreCase("sub")) {
      mMtkTvAppTV.setFinetuneFreq("sub", (int) mRestoreHz * 1000000, true);
    } else {
      mMtkTvAppTV.setFinetuneFreq("main", (int) mRestoreHz * 1000000, true);
    }
  }
  public boolean isDualTunerEnable() {
      boolean result = false;
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT)) {
          int dualTunerValue = MtkTvConfig.getInstance().getConfigValue(TV_DUAL_TUNER_ENABLE);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDualTunerEnable(), dualTunerValue =" + dualTunerValue);
          if (1 == dualTunerValue) {
              result = true;
          }
      }
      return result;
  }

    public boolean selectPowerOnChannel() {
    int result = -1;
    int def = SaveValue.getInstance(mContext).readValue(MenuConfigManager.SELECT_MODE);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectPowerOnChannel SELECT_MODE ==" + def);
    int powerOnChannelID = 0;
    if (def == 1) {
      int tunerMode = mMtkTvConfig.getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC);
      if (0 == tunerMode) {
        powerOnChannelID = mMtkTvConfig
            .getConfigValue(MtkTvConfigType.CFG_NAV_AIR_ON_TIME_CH);
      } else {
        powerOnChannelID = mMtkTvConfig
            .getConfigValue(MtkTvConfigType.CFG_NAV_CABLE_ON_TIME_CH);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectPowerOnChannel powerOnChannelID==" + powerOnChannelID);
      if (powerOnChannelID > 0 && !isPvrRunning()) {
        TIFChannelInfo channelInfo = TIFChannelManager.getInstance(mContext)
            .queryChannelById(powerOnChannelID);
        result = InputSourceManager.getInstance().tuneChannelByTIFChannelInfoForAssistant(channelInfo);
      }
    }
    return result == 0;

  }
    public boolean hasVGASource() {
        if(hasVGASource == -1) {
            hasVGASource = 0;

            hasVGASource = InputUtil.getInputByType(AbstractInput.TYPE_VGA) != null ? 1 : 0;

        }

        return hasVGASource == 1;
    }
    public boolean isActiveChannel() {
        TIFChannelInfo channelInfo = TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
        if(channelInfo == null){
            MtkTvChannelInfoBase channel= getCurChInfo();
            if(TIFFunctionUtil.checkChMask(channel,TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL)){
                return TIFFunctionUtil.checkChMask(channel,TIFFunctionUtil.CH_LIST_EPG_US_MASK, TIFFunctionUtil.CH_LIST_EPG_US_VAL);
            }else {
                return false;
            }
        }
        if(TIFFunctionUtil.checkChMaskformDataValue(channelInfo,TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL)){
            return TIFFunctionUtil.checkChMaskformDataValue(channelInfo,TIFFunctionUtil.CH_LIST_EPG_US_MASK, TIFFunctionUtil.CH_LIST_EPG_US_VAL);
        }else {
            return false;
        }
    }
    public int dvbsGetCategoryNum() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsGetCategoryNum");
        mtkTvScanDvbsBase.dvbsGetCategoryNum();
       return mtkTvScanDvbsBase.category_num;

   }
   public String dvbsGetCategoryInfoByIdx(int categoryIdx) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsGetCategoryInfoByIdx");
        return mtkTvScanDvbsBase.dvbsGetCategoryInfoByIdx(categoryIdx);

   }

   public boolean isOperatorNTVPLUS() {
        int mOperatorName = MtkTvConfig.getInstance().getConfigValue(
                   CommonIntegration.SAT_BRDCSTER);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorNTVPLUS OperatorName "+mOperatorName+" ,MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS ="+MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS);
        return mOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS;
   }

   public boolean isOperatorFREESAT() {
       int mOperatorName = MtkTvConfig.getInstance().getConfigValue(
                  CommonIntegration.SAT_BRDCSTER);
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorFREESAT OperatorName "+mOperatorName+" ,MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT ="+MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT);
       return mOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT;
  }

    public boolean isOperatorNTVPLUS56E() {
        int OperatorName = MtkTvConfig.getInstance().getConfigValue(
                CommonIntegration.SAT_BRDCSTER);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorNTVPLUS56E OperatorName "+OperatorName+" ,MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS ="+MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_56E);
        return OperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_56E;
    }

    public boolean isOperatorNTVPLUS140E() {
        int OperatorName = MtkTvConfig.getInstance().getConfigValue(
                CommonIntegration.SAT_BRDCSTER);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorNTVPLUS140E OperatorName "+OperatorName+" ,MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS ="+MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_140E);
        return OperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_140E;
    }

    public boolean isOperatorTELEKARTA140E() {
        int OperatorName = MtkTvConfig.getInstance().getConfigValue(
                CommonIntegration.SAT_BRDCSTER);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorTELEKARTA140E OperatorName "+OperatorName+" ,MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS ="+MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA_140E);
        return OperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA_140E;
    }

   public boolean isOperatorContain(){
	   return isOperatorNTVPLUS56E() || isOperatorNTVPLUS140E() || isOperatorNTVPLUS() || isOperatorTKGS() || isOperatorFREESAT();
   }

    public boolean isNTVPLUSContain(){
        return isOperatorNTVPLUS56E() || isOperatorNTVPLUS140E() || isOperatorNTVPLUS();
    }

    public boolean isTELEKARTAContain(){
        return isOperatorTELEKARTA140E() || isOperatorTELEKARTA();
    }

   public boolean isOperatorTELEKARTA() {
      int mOperatorName = MtkTvConfig.getInstance().getConfigValue(
               CommonIntegration.SAT_BRDCSTER);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorTELEKARTA OperatorName "+mOperatorName+" ,MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA ="+MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA);
      return mOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA;
  }
   public boolean isOperatorTKGS() {
  //     int tkgsmode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TKGS_OPER_MODE);
       boolean satOperOnly = isPreferSatMode();
       boolean isTkgs =TVContent.getInstance(mContext).isTKGSOperator();
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorTKGSsatOperOnly "+satOperOnly);
       if(satOperOnly && isTkgs){
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorTKGS  true");
            return true;
         }else{
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOperatorTKGS  false");
             return false;
         }

   }
   public boolean isFavoriteNetworkEnable(){
       MtkTvScanDvbtBase.UiOpSituation opSituation = MtkTvScan.getInstance().getScanDvbtInstance().uiOpGetSituation();
       MtkTvScanDvbtBase.FavNwk[] nwkList = MtkTvScan.getInstance().getScanDvbtInstance().uiOpGetFavNwk();
       boolean favNetworkel=false;
       if (opSituation.favouriteNeteorkPopUp && nwkList != null
               && nwkList.length > 0) {
           favNetworkel=true;
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favNetworkel true :"+favNetworkel);
       } else {
           favNetworkel=false;
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favNetworkel false :"+favNetworkel);
       }
       return favNetworkel;
   }

   public int getDoblyVersion(){
       int doblyVersion=mMtkTvConfig.getConfigValue(MtkTvConfigTypeBase.CFG_AUD_DOLBY_INFO);
       com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "doblyVersion="+doblyVersion);
       return doblyVersion;
   }

   public String[] getTunerModes(){
       if(!DataSeparaterUtil.getInstance().isTunerModeIniExist()){
           String[] tunerModeStr;
           if(isEUPARegion()){
               if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT) && isCurrentSourceDTV()){
                   if (ScanContent.getDVBSAllOperatorList(mContext).size() == 0) {
                       tunerModeStr = mContext.getResources().getStringArray(
                               R.array.menu_tv_tuner_mode_array_full_eu_sat_only);
                   } else {
                       tunerModeStr = mContext.getResources().getStringArray(
                               R.array.menu_tv_tuner_mode_array_full_eu);
                   }
               }else {
                   tunerModeStr = mContext.getResources().getStringArray(
                           R.array.menu_tv_tuner_mode_array);
               }
           }else{
               if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS)
			   || TVContent.getInstance(mContext).isCOLRegion()) {
                   tunerModeStr = mContext.getResources().getStringArray(
                           R.array.menu_tv_tuner_mode_array);
               } else if((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) && isCurrentSourceDTV())||
                       !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
                   if (ScanContent.getDVBSAllOperatorList(mContext).size() == 0) {
                       tunerModeStr = mContext.getResources().getStringArray(
                               R.array.menu_tv_tuner_mode_array_full_eu_sat_only);
                   } else {
                       tunerModeStr = mContext.getResources().getStringArray(
                               R.array.menu_tv_tuner_mode_array_full_eu);
                   }
               }else {
                   tunerModeStr = mContext.getResources().getStringArray(
                           R.array.menu_tv_tuner_mode_array);
               }
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"TunerModeIni not Exist tunerModeStr.size="+tunerModeStr.length);
           }
           return tunerModeStr;
       }else {
           List<String> tunerModes = new ArrayList<String>();
           if(DataSeparaterUtil.getInstance().isDVBTSupport()){
               tunerModes.add(mContext.getString(R.string.menu_arrays_Antenna));
           }
           if(DataSeparaterUtil.getInstance().isDVBCSupport()){
               tunerModes.add(mContext.getString(R.string.menu_arrays_Cable));
           }
           if((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) && isCurrentSourceDTV())||
                   !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
               if(isEUPARegion()){
                   if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT) &&
                           DataSeparaterUtil.getInstance().isDVBSSupport()){
                       if(ScanContent.getDVBSAllOperatorList(mContext).size() == 0){
                           tunerModes.add(mContext.getString(R.string.menu_arrays_Satellite));
                       }else {
                           tunerModes.add(mContext.getString(R.string.dvbs_preferred_satellite));
                           tunerModes.add(mContext.getString(R.string.dvbs_general_satellite));
                       }
                   }
               }else{
               if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS) &&
                       DataSeparaterUtil.getInstance().isDVBSSupport() &&
                           !TVContent.getInstance(mContext).isCOLRegion()){
                       if(ScanContent.getDVBSAllOperatorList(mContext).size() == 0){
                           tunerModes.add(mContext.getString(R.string.menu_arrays_Satellite));
                       }else {
                           tunerModes.add(mContext.getString(R.string.dvbs_preferred_satellite));
                           tunerModes.add(mContext.getString(R.string.dvbs_general_satellite));
                       }
                   }
               }
           }
           return tunerModes.toArray(new String[0]);
       }
   }

   public String[] getTunerModeValues(){
        if(TVContent.getInstance(mContext).isCOLRegion()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"COLRegion");
            return new String[]{"0","1"};
            }
       if(TVContent.getInstance(mContext).isSupportOneImage()){
           if(MarketRegionInfo.isEU_PA_Region()){
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"EUPA_oneimage");
               return new String[]{"0","1"};
           }else {
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"EU_common");
               return getCommonTunerModeValues();
        }
       }
       return getCommonTunerModeValues();
   }

   public String[] getCommonTunerModeValues(){
       if(!DataSeparaterUtil.getInstance().isTunerModeIniExist()){
           String[] tunerModeValue;
           if(isEUPARegion()){
               if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT) && isCurrentSourceDTV()){
                   if (ScanContent.getDVBSAllOperatorList(mContext).size() == 0) {
                       tunerModeValue = new String[]{"0","1","2"};
                   } else {
                       tunerModeValue = new String[]{"0","1","2","3"};
                   }
               }else {
                   tunerModeValue = new String[]{"0","1"};
               }
           }else{
               if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS)
			   || TVContent.getInstance(mContext).isCOLRegion()) {
                   tunerModeValue = new String[]{"0","1"};
               } else if((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) && isCurrentSourceDTV())||
                       !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
                   if (ScanContent.getDVBSAllOperatorList(mContext).size() == 0) {
                       tunerModeValue = new String[]{"0","1","2"};
                   } else {
                       tunerModeValue = new String[]{"0","1","2","3"};
                   }
               }else {
                   tunerModeValue = new String[]{"0","1"};
               }
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"TunerModeIni not Exist tunerModeValue.size="+tunerModeValue.length);
           }
           return tunerModeValue;
       }
       List<String> values = new ArrayList<String>();
       if(DataSeparaterUtil.getInstance().isDVBTSupport()){
           values.add("0");
       }
       if(DataSeparaterUtil.getInstance().isDVBCSupport()){
           values.add("1");
       }
       if((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) && isCurrentSourceDTV())||
               !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
           if(isEUPARegion()){
               if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT) &&
                       DataSeparaterUtil.getInstance().isDVBSSupport()){
                   if(ScanContent.getDVBSAllOperatorList(mContext).size() == 0){
                       values.add("2");
                   }else {
                       values.add("2");
                       values.add("3");
                   }
               }
           }else {
           if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS) &&
                   DataSeparaterUtil.getInstance().isDVBSSupport() &&
                   !TVContent.getInstance(mContext).isCOLRegion()){
                   if(ScanContent.getDVBSAllOperatorList(mContext).size() == 0){
                       values.add("2");
                   }else {
                       values.add("2");
                       values.add("3");
                   }
               }
           }
       }
       return values.toArray(new String[0]);
   }

  public static boolean isSupportDvbs() {
    boolean result = false;
    if(DataSeparaterUtil.getInstance().isDVBSSupport()){
      if((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS) && !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) ||
              (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT) && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA))) {
        result = true;
      }
    }
    return result;
  }

   public boolean isCIEnabled() {
       boolean isCLTypeShow = TVContent.getInstance(mContext).isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE);
       String profileName = MtkTvConfig.getInstance().
               getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
       if (isCLTypeShow && !TextUtils.isEmpty(profileName)) {
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LIST_TYPE, isCLTypeShow : " +isCLTypeShow);
           return true;
       }
       return false;
       }

   public void showToast(Context context,int resId) {
       Toast result = new Toast(context);
       LayoutInflater inflate = (LayoutInflater)
               context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       View v = inflate.inflate(R.layout.transient_notification, null);
       TextView tv = (TextView)v.findViewById(com.android.internal.R.id.message);
       tv.setText(context.getResources().getText(resId));
       result.setView(v);
       result.setDuration(Toast.LENGTH_SHORT);
       result.show();
   }

   public boolean  isDisableColorKey(){
       return DataSeparaterUtil.getInstance().isDisableColorKey();
//       return true;
   }


   public boolean deleteAllInactiveChannels() {
       List<MtkTvChannelInfoBase> list = getChannelListByMaskFilter(-1,
               MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,9999 , TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK, TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL);
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteAllInactiveChannels list.size()>>>" + list.size());
       if (!list.isEmpty()) {
           setChannelList(MtkTvChannelList.CHLST_OPERATOR_DEL, list);
           return true;
       }
       return false;
   }

   public boolean clearMaskForInactiveChannels() {
       List<MtkTvChannelInfoBase> list = getChannelListByMaskFilter(-1,
               MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,9999 , TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK, TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL);
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "clearMaskForInactiveChannels list.size()>>>" + list.size());
       if (!list.isEmpty()) {
           for(MtkTvChannelInfoBase channel : list){
               channel.setNwMask(channel.getNwMask() & ~TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK);
               channel.setNwMask(channel.getNwMask() & ~MtkTvChCommonBase.SB_VNET_REMOVAL);
           }
           setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
           return true;
       }
       return false;
   }

  public boolean isUDiskEnabled() {
    ArrayList<MountPoint> list = DeviceManager.getInstance().getMountPointList();
    if (list == null || list.isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isUsbEnabled list size 0");
      return false;
    }else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isUsbEnabled list size:" + list.size() + list.get(0).mMountPoint);
      return true;
    }
  }

  public List<File> getUDiskFiles(String extension){
    ArrayList<MountPoint> list = DeviceManager.getInstance().getMountPointList();
    List<File> files = new ArrayList<File>();
    if (list == null || list.isEmpty()) {
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getUDiskFile list size:"+list.size() + list.get(0).mMountPoint);
      try {
          String uPath = list.get(0).mMountPoint + File.separator;
          File main = new File(uPath);
          File[] temFiles = main.listFiles();
          if(files != null){
              for(File f : temFiles){
                  if(f.isFile()){
                      String name = f.getName();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUDiskFiles add name="+name);
                      if(name.lastIndexOf(".") != -1 && extension.equals(name.substring(name.lastIndexOf(".")+1))){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getUDiskFiles add "+name);
                          files.add(f);
                      }
                  }
              }
          }
      } catch (Exception e) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception:"+e.getMessage());
      }
    }
    return files;
  }

   public List<String> getUDiskFileNames(String extension){
       List<File> files = getUDiskFiles(extension);
       List<String> namesList = new ArrayList<String>();
       for(File f : files){
           namesList.add(f.getName());
       }
       return namesList;
   }
   public List<TIFChannelInfo> getChannelListForEPG() {
       NavBasicDialog mNavBasicDialog = (NavBasicDialog) ComponentsManager
           .getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
       if (mNavBasicDialog instanceof ChannelListDialog) {
        ChannelListDialog mChannelListDialog = (ChannelListDialog) mNavBasicDialog;
        return mChannelListDialog.getChannelListForEPG();
       }
       return null;
     }

   public void selectCurFirstChannel() {
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCurFirstChannel ");
       MtkTvChannelInfoBase channelInfo = new MtkTvChannelInfoBase(0xffff, 0);
       channelInfo.setChannelId(0xffffffff);
       MtkTvBroadcast.getInstance().channelSelect(channelInfo, false);
   }

    public List<TIFChannelInfo> getChannelListForType() {
        NavBasicDialog mNavBasicDialog = (NavBasicDialog) ComponentsManager
            .getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
        if (mNavBasicDialog instanceof ChannelListDialog) {
            ChannelListDialog mChannelListDialog = (ChannelListDialog) mNavBasicDialog;
            return mChannelListDialog.getChannelListForType();
        }
        return null;
    }

   public static String getSafeString(String... args) {
     StringBuilder sb = new StringBuilder();
     for (String string : args) {
       if(!TextUtils.isEmpty(string)) {
         sb.append(string);
       }
     }
     return sb.toString().trim();
   }

   public boolean isPvrRunningOrTimeshiftRuning(Context context) {
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isPvrRunningOrTimeshiftRuning~");
       return (isPvrRunning() || isTimeShiftRunning(context));
   }

   public boolean isPvrRunning() {
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isPvrRunning ?");
       return ((DvrManager.getInstance() != null && DvrManager.getInstance().pvrIsRecording())//pvr recording
               || (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning())); //pvr playing
   }

   public boolean isTimeShiftRunning(Context context) {
       return SaveValue.getInstance(context).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START); //timeshift running
   }

   public boolean isALLOWChangeForZiggo(boolean isTuner) {
     int capability = mMtkTvAppTVBase.getCapabilityEx(isTuner ? 1 : 2);
     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCapabilityEx:" + capability);
     boolean result = true;
     if(isTuner) {
       result = (capability & (1 << 10)) != 0;
     } else {
       result = (capability & (1 << 11)) != 0;
     }
     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isALLOWChangeForZiggo, isTuner:" + isTuner + "  result:" + result);
     return result;
   }

   public boolean isScreenOn(Context context) {
		DisplayManager dm = (DisplayManager) context
				.getSystemService(Context.DISPLAY_SERVICE);
		for (Display displayManager : dm.getDisplays()) {
			if (displayManager.getState() != Display.STATE_OFF) {
				Log.d(TAG, "isScreenOn||true");
				return true;
			}
		}

		return false;
	}
    public boolean getIsCreatedMonitor(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCreatedMonitor>>  "+ isCreatedMonitor);
        return isCreatedMonitor;
    }

    public void setIsCreatedMonitor(boolean isCreated){
        isCreatedMonitor = isCreated;
    }
}
