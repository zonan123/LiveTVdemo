package com.mediatek.wwtv.setting.util;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.LiveTvSetting;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
//import android.support.v7.preference.Preference;
import android.util.Log;
import androidx.preference.Preference;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.setting.base.scan.adapter.EditDetailAdapter.EditItem;
import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter.SatItem;
import com.mediatek.wwtv.setting.base.scan.adapter.BissListAdapter.BissItem;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.base.scan.adapter.TkgsLocatorListAdapter.TkgsLocatorItem;
import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.setting.base.scan.model.CountrysIndex;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterView;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.ScanDvbsRet;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvScanTpInfo;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.TunerPolarizationType;
import com.mediatek.twoworlds.tv.model.MtkTvBisskeyInfoBase;
import com.mediatek.twoworlds.tv.MtkTvBisskeyBase;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.DvbsTableType;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.TKGSOneSvcList;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.TKGSOneLocator;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvGotoXXInfo;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;

public final class MenuDataHelper {

  public static final String TAG = "MenuDataHelper";
  public MenuConfigManager mConfigManager;
  private Context mContext;
  public static MenuDataHelper mSelf;
  // this map is used to store channel obj just like channelskip,channel sort and so on
  Map<String, Action> mChannelActionMap;
  Map<String, Preference> mChannelPreferenceMap;
  private TVContent mTV;
  private int chNum = 0;
  private List<MtkTvChannelInfoBase> chList = null;
  List<String[]> channelInfo;
  static final int NORMALPAGE_SIZE = 10;
  int gotoPage = 0;
  int gotoPosition = 0;
  CommonIntegration mCommonIntegration;

  private MenuDataHelper(Context context) {
    this.mContext = context;
    mConfigManager = MenuConfigManager.getInstance(context);
    mChannelActionMap = new HashMap<String, Action>();
    mChannelPreferenceMap = new HashMap<String, Preference>();
    // saveV = SaveValue.getInstance(context);
    mTV = TVContent.getInstance(mContext);
    mCommonIntegration = CommonIntegration.getInstance();
  }

  /**
   * string to show in UI for video related options
   */
  public static synchronized MenuDataHelper getInstance(Context context) {
    if (mSelf == null) {
      mSelf = new MenuDataHelper(context.getApplicationContext());
      RxBus.instance.onEvent(ActivityDestroyEvent.class)
          .filter(it -> it.activityClass == LiveTvSetting.class)
          .firstElement()
          .doOnSuccess(it -> setMySelfNull())
          .subscribe();
    }else{
        mSelf.mContext=context.getApplicationContext();
    }
    return mSelf;
  }

  public Map<String, Action> getChannelActionMap() {
    return mChannelActionMap;
  }
  public Map<String, Preference> getChannelPreferenceMap(){
      return mChannelPreferenceMap;
  }

  /**
   * get positions of data item which are effected by parent data item
   *
   * @return the position of data item
   */
  public int[] getEffectGroupInitValues(Action mEffectParentAction) {
    int mEffectGroupInitValues[];
    if (mEffectParentAction != null
        && mEffectParentAction.getmDataType() == Action.DataType.EFFECTOPTIONVIEW) {
      mEffectGroupInitValues = new int[mEffectParentAction.getmEffectGroup().size()];
      int i = 0;
      for (Action childAction : mEffectParentAction.getmEffectGroup()) {
        mEffectGroupInitValues[i] = mConfigManager
            .getDefault(childAction.getmItemId());
        i++;
      }
      return mEffectGroupInitValues;
    } else {
      throw new IllegalArgumentException(
          "type of mEffectParentAction is not EFFECTOPTIONVIEW or mEffectParentAction is null");
    }
  }

  /**
   * deal with switchview
   *
   * @param mainAction
   */
  public void dealSwitchChildGroupEnable(Action mainAction) {
    List<Action> mChildGroup = mainAction.mEffectGroup;
    if (MenuConfigManager.VIDEO_3D_MODE.equals(mainAction.mItemID)) {
      ArrayList<Boolean> m3DConfigList = (ArrayList<Boolean>) MenuConfigManager.getInstance(
          mContext).get3DConfig();
      mainAction.setEnabled(m3DConfigList.get(0));
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("OptionView",
          "mAction:" + mainAction.mItemID + ",isEnable:" + mainAction.isEnabled());
      int i = 1;
      for (Action childItem : mChildGroup) {
        childItem.setEnabled(m3DConfigList.get(i));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("OptionView",
            "childItem:" + childItem + "childItem.isEnable:" + childItem.isEnabled());
        i++;
      }
      return;
    }

    Map<Integer, Boolean[]> mHashMap = mainAction.getmSwitchHashMap();
    Boolean[] isEnables = mHashMap.get(mainAction.mInitValue);
    if (isEnables != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("SwitchOptionView", "isEnables[0]==:" + isEnables[0]);
    }
    int i = 0;
    for (Action childItem : mChildGroup) {
      if (isEnables != null) {
        childItem.setEnabled(isEnables[i++]);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
            "childItem.isEnbaled:" + childItem.mItemID + "isEnable:" + childItem.isEnabled());
        // special deal PowerOnCh
        if (childItem.isEnabled() && childItem.mItemID.equals(MenuConfigManager.SETUP_POWER_ON_CH)){
          if (mCommonIntegration.getChannelAllNumByAPI() <= 0) {
            childItem.setEnabled(false);
          }
        }

        if (MenuConfigManager.TYPE.equals(mainAction.mItemID) && mainAction.mInitValue == 2) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d("SwitchOptionView", "SwitchOptionView mItemID:" + childItem.mItemID
              + "isEnable:" + childItem.isEnabled() + "mInitValue:" + mainAction.mInitValue);
          if (CommonIntegration.isEURegion()) {
            if (TVContent.getInstance(mContext).iCurrentInputSourceHasSignal()) {
              childItem.setEnabled(true);
            } else {
              childItem.setEnabled(false);
            }
          }
        }
      }
    }
  }

  /**
   * load tune dialog info
   *
   * @param flag :load different type of dataIt
   */
  public void loadTuneDiagInfo(Action diagAction, boolean flag) {
    if (!flag){
      List<String> displayName = new ArrayList<String>();
      List<String> displayValue = new ArrayList<String>();
      if (mCommonIntegration.isCurrentSourceTv()
          && !mCommonIntegration.isCurrentSourceDTV()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTuneDiagInfo show atv ==" + mCommonIntegration.isCurrentSourceATV());
        MtkTvUtil.getInstance().tunerFacQuery(true, displayName,
            displayValue);
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTuneDiagInfo not show");
        MtkTvUtil.getInstance().tunerFacQuery(false, displayName,
            displayValue);
      }
      String itemName = MenuConfigManager.FACTORY_TV_TUNER_DIAGNOSTIC_NOINFO;
      for (int i = 0; i < displayName.size(); i++) {
        String[] display = {
            displayValue.get(i)
        };
        Action fvEventForm = new Action(itemName,
            displayName.get(i), MenuConfigManager.INVALID_VALUE,
            MenuConfigManager.INVALID_VALUE, 0, display,
            MenuConfigManager.STEP_VALUE,
            Action.DataType.FACTORYOPTIONVIEW);
        fvEventForm.hasRealChild = false;
        diagAction.mSubChildGroup.add(fvEventForm);
      }
      if (displayName.isEmpty()) {
        String[] nullArray = mContext.getResources().getStringArray(
            R.array.menu_setup_null_array);
        Action fvEventForm = new Action(itemName, mContext
            .getString(R.string.menu_factory_TV_tunerdiag_noinfo),
            MenuConfigManager.INVALID_VALUE,
            MenuConfigManager.INVALID_VALUE, 0, nullArray,
            MenuConfigManager.STEP_VALUE,
            Action.DataType.FACTORYOPTIONVIEW);
        fvEventForm.hasRealChild = false;
        diagAction.mSubChildGroup.add(fvEventForm);
      }
    }

  }

  public void changePreferenceEnable(){

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start changeEnable");
      Preference channelskip = mChannelPreferenceMap.get("channelskip");
      Preference channelSort = mChannelPreferenceMap.get("channelSort");
      Preference channelEdit = mChannelPreferenceMap.get("channelEdit");
      Preference cleanList = mChannelPreferenceMap.get("cleanList");
      Preference saChannelEdit = mChannelPreferenceMap.get("saChannelEdit");
      Preference saChannelFine = mChannelPreferenceMap.get("saChannelFine");
      if (CommonIntegration.isCNRegion()) {
        if (mCommonIntegration.hasActiveChannel() && !mCommonIntegration.is3rdTVSource()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable true");
          if (channelEdit != null && cleanList != null) {
            channelEdit.setEnabled(true);
            cleanList.setEnabled(true);
          }
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false");
          if (channelEdit != null && cleanList != null) {
            channelEdit.setEnabled(false);
            cleanList.setEnabled(false);
          }
        }
      } else {
        // fix CR DTV00584619
        if (channelskip == null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end changeEnable null return");
          return;
        }
        if (mCommonIntegration.hasActiveChannel() && !mCommonIntegration.is3rdTVSource()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable true");
          if(hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)){
              channelskip.setEnabled(true);
          }else{
              channelskip.setEnabled(false);
          }
          if (CommonIntegration.isSARegion() && saChannelEdit != null && saChannelFine != null) {
            saChannelEdit.setEnabled(true);
            if (TIFChannelManager.getInstance(mContext).hasATVChannels()) {
              saChannelFine.setEnabled(true);
            } else {
              saChannelFine.setEnabled(false);
            }
          }
          if (CommonIntegration.isEURegion() && saChannelFine != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable saChannelFine != nulltrue");
            channelskip.setEnabled(true);
            channelSort.setEnabled(true);
            channelEdit.setEnabled(true);
            // chanelDecode.setEnable(true);
            // fix cr 00621281
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "changeEnable saChannelFine != nulltrue-source:" + !mTV.isCurrentSourceDTV());
            if (!mTV.isCurrentSourceDTV()) {
              saChannelFine.setEnabled(true);
            } else {
              saChannelFine.setEnabled(false);
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.isTurkeyCountry()> "+mTV.isTurkeyCountry() +"dvbs_operator_name_tivibu > "+ScanContent.getDVBSCurrentOPStr(mContext)+"  >>   "+mContext
                    .getString(R.string.dvbs_operator_name_tivibu));

            if(mTV.isTurkeyCountry() && ScanContent.getDVBSCurrentOPStr(mContext).equalsIgnoreCase(mContext
                    .getString(R.string.dvbs_operator_name_tivibu))){
            	   channelEdit.setEnabled(false);
            	   channelskip.setEnabled(false);
            	   channelSort.setEnabled(false);
            	   cleanList.setEnabled(false);
            }else{
                 channelskip.setEnabled(true);
                 channelSort.setEnabled(true);
                 cleanList.setEnabled(true);
            }
            boolean isTKGS = ScanContent.isPreferedSat() && mTV.isTurkeyCountry() && mTV.isTKGSOperator();
            if(isTKGS && mTV.getTKGSOperatorMode() == 0){
                channelEdit.setEnabled(false);
                channelSort.setEnabled(false);
            }
          //  cleanList.setEnabled(true);
          }
          setSkipSortEditItemHid(channelskip, channelSort, channelEdit);
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false");
          channelskip.setEnabled(false);
          if (CommonIntegration.isSARegion() && saChannelEdit != null && saChannelFine != null) {
            saChannelEdit.setEnabled(false);
            saChannelFine.setEnabled(false);
          }
          if (CommonIntegration.isEURegion() && saChannelFine != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable saChannelFine != nullfalse");
            channelskip.setEnabled(false);
            channelSort.setEnabled(false);
            channelEdit.setEnabled(false);
            // chanelDecode.setEnable(false);
            saChannelFine.setEnabled(false);
            cleanList.setEnabled(false);
          }
        }
        // if not start from LiveTV ,can not do find tune
        //need to fix
//        if (!LiveApplication.isFromLiveTV && saChannelFine != null && saChannelFine.isEnabled()) {
//          saChannelFine.setEnabled(false);
//        }
      //need to fix
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end changeEnable set success");

  }
  /**
   * for US/SA/EU region TV channel items to be editable or not
   */
  public void changeEnable() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start changeEnable");
    Action channelskip = mChannelActionMap.get("channelskip");
    Action channelSort = mChannelActionMap.get("channelSort");
    Action channelEdit = mChannelActionMap.get("channelEdit");
    Action cleanList = mChannelActionMap.get("cleanList");
    Action saChannelEdit = mChannelActionMap.get("saChannelEdit");
    Action saChannelFine = mChannelActionMap.get("saChannelFine");
    if (CommonIntegration.isCNRegion()) {
      if (mCommonIntegration.hasActiveChannel()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable true");
        if (channelEdit != null && cleanList != null) {
          channelEdit.setEnabled(true);
          cleanList.setEnabled(true);
        }
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false");
        if (channelEdit != null && cleanList != null) {
          channelEdit.setEnabled(false);
          cleanList.setEnabled(false);
        }
      }
    } else {
      // fix CR DTV00584619
      if (channelskip == null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end changeEnable null return");
        return;
      }
      if (mCommonIntegration.hasActiveChannel()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable true");
        channelskip.setEnabled(true);
        if (CommonIntegration.isSARegion() && saChannelEdit != null && saChannelFine != null) {
          saChannelEdit.setEnabled(true);
          if (TIFChannelManager.getInstance(mContext).hasATVChannels()) {
            saChannelFine.setEnabled(true);
          } else {
            saChannelFine.setEnabled(false);
          }
        }
        if (CommonIntegration.isEURegion() && saChannelFine != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable saChannelFine != nulltrue");
          channelskip.setEnabled(true);
          channelSort.setEnabled(true);
          channelEdit.setEnabled(true);
          // chanelDecode.setEnable(true);
          // fix cr 00621281
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
              "changeEnable saChannelFine != nulltrue-source:" + !mTV.isCurrentSourceDTV());
          if (!mTV.isCurrentSourceDTV()) {
            saChannelFine.setEnabled(true);
          } else {
            saChannelFine.setEnabled(false);
          }

          cleanList.setEnabled(true);
        }
        setSkipSortEditItemHid(channelskip, channelSort, channelEdit);
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false");
        channelskip.setEnabled(false);
        if (CommonIntegration.isSARegion() && saChannelEdit != null && saChannelFine != null) {
          saChannelEdit.setEnabled(false);
          saChannelFine.setEnabled(false);
        }
        if (CommonIntegration.isEURegion() && saChannelFine != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable saChannelFine != nullfalse");
          channelskip.setEnabled(false);
          channelSort.setEnabled(false);
          channelEdit.setEnabled(false);
          // chanelDecode.setEnable(false);
          saChannelFine.setEnabled(false);
          cleanList.setEnabled(false);
        }
      }
      // if not start from LiveTV ,can not do find tune
      //need to fix
//      if (!LiveApplication.isFromLiveTV && saChannelFine != null && saChannelFine.isEnabled()) {
//        saChannelFine.setEnabled(false);
//      }
    //need to fix
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end changeEnable set success");
  }

  public void setSkipSortEditItemHid(Preference channelskip,Preference channelSort,Preference channelEdit){

      CableOperator co = ScanContent.getCurrentOperator();
      int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig.getInstance().getCountry());
      int tunerMode = mCommonIntegration.getTunerMode();
      if (tunerMode == 1) {// dvbc has operator
        if (CableOperator.Ziggo.ordinal() == co.ordinal()
            || (CableOperator.OTHER.ordinal() == co.ordinal()
            && CountrysIndex.CTY_13_Netherlands_NLD == countryID)) {
          if (channelskip != null) {
            channelskip.setEnabled(false);
          }
          if (channelSort != null) {
            channelSort.setEnabled(false);
          }
          if (channelEdit != null) {
            channelEdit.setEnabled(false);
          }
        }
      }

  }

  public void setSkipSortEditItemHid(Preference channelitem){
      CableOperator co = ScanContent.getCurrentOperator();
      int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig.getInstance().getCountry());
      int tunerMode = mCommonIntegration.getTunerMode();
      if (tunerMode == 1) {// dvbc has operator
        if (CableOperator.Ziggo.ordinal() == co.ordinal()
            || (CableOperator.OTHER.ordinal() == co.ordinal()
            && CountrysIndex.CTY_13_Netherlands_NLD == countryID)) {
          if (channelitem != null) {
              channelitem.setEnabled(false);
          }
        }
      }

  }


  public void setSkipSortEditItemHid(Action channelskip, Action channelSort, Action channelEdit) {
    CableOperator co = ScanContent.getCurrentOperator();
    int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig.getInstance().getCountry());
    int tunerMode = mCommonIntegration.getTunerMode();
    if (tunerMode == 1) {// dvbc has operator
      if (CableOperator.Ziggo.ordinal() == co.ordinal()
          || (CableOperator.OTHER.ordinal() == co.ordinal()
          && CountrysIndex.CTY_13_Netherlands_NLD == countryID)) {
        if (channelskip != null) {
          channelskip.setEnabled(false);
        }
        if (channelSort != null) {
          channelSort.setEnabled(false);
        }
        if (channelEdit != null) {
          channelEdit.setEnabled(false);
        }
      }
    }
  }

  public int getChNum() {
    return chNum;
  }

  public void setChNum(int chNum) {
    this.chNum = chNum;
  }

  public List<MtkTvChannelInfoBase> getChList() {
    return chList;
  }

  public void setChList(List<MtkTvChannelInfoBase> chList) {
    this.chList = chList;
  }

  public List<String[]> getChannelInfo() {
    return channelInfo;
  }

  public void setChannelInfo(List<String[]> channelInfo) {
    this.channelInfo = channelInfo;
  }

  public void setGotoPage(int page) {
    gotoPage = page;
  }

  public void setGotoPosition(int pos) {
    gotoPosition = pos;
  }

  public int getGotoPage() {
    return gotoPage;
  }

  public int getGotoPosition() {
    return gotoPosition;
  }

  Map<Integer, String> mChNumberMap = new HashMap<Integer, String>();

  public String getDisplayChNumber(int chId) {
    return mChNumberMap.get(chId);
  }

  /**
   * get the tv's current channel list from tif or from mtktvapi
   *
   * @return
   */
  public List<MtkTvChannelInfoBase> getTVChannelList() {
    List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
    mChNumberMap.clear();
    if (CommonIntegration.supportTIFFunction()) {
      List<TIFChannelInfo> tifList =TIFChannelManager.getInstance(mContext).getCurrentSVLChannelListBase();
      for (TIFChannelInfo tifChannelInfo : tifList) {
          MtkTvChannelInfoBase mCurrentChannel = TIFChannelManager.getInstance(mContext).getAPIChannelInfoByChannelId(tifChannelInfo.mInternalProviderFlag3);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentChannel=" + mCurrentChannel);
        if(mCurrentChannel==null || !TIFFunctionUtil.checkChMask(mCurrentChannel, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL)){
            continue;
        }
        mCurrentChannel.setBlock(tifChannelInfo.mLocked);
        chList.add(mCurrentChannel);
        int keyChId = mCurrentChannel.getChannelId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentChannel- keyChId:" + keyChId);
        if (mCurrentChannel instanceof MtkTvATSCChannelInfo) {
          MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) mCurrentChannel;
          String tempStr = tmpAtsc.getMajorNum() + "-" + tmpAtsc.getMinorNum();
          mChNumberMap.put(keyChId, tempStr);
        } else if (mCurrentChannel instanceof MtkTvISDBChannelInfo) {
          MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) mCurrentChannel;
          int majorNum = tmpIsdb.getMajorNum();
          String tempStr = "";
//          if (majorNum < 10) {
//            tempStr = "0" + majorNum + "." + tmpIsdb.getMinorNum();
//          } else {
//            tempStr = tmpIsdb.getMajorNum() + "." + tmpIsdb.getMinorNum();
//          }

          tempStr = majorNum + "." + tmpIsdb.getMinorNum();
          mChNumberMap.put(keyChId, tempStr);
        } else if (mCurrentChannel instanceof MtkTvAnalogChannelInfo) {
          String tempStr = "" + mCurrentChannel.getChannelNumber();
          mChNumberMap.put(keyChId, tempStr);
        } else if (mCurrentChannel instanceof MtkTvDvbChannelInfo) {
          String tempStr = "" + mCurrentChannel.getChannelNumber();
          mChNumberMap.put(keyChId, tempStr);
        }
      }

    } else {
      chList = EditChannel.getInstance(mContext).getChannelList();
    }
    return chList;
  }

  private int mCurrentSkipSize;
  public void setCurrentSkipSize(int currentSize){
	  mCurrentSkipSize = currentSize;
  }
  public int getCurrentSkipSize(){
	  return mCurrentSkipSize;
  }
  public void resetCurrentSkipSize(){
      mCurrentSkipSize = 0;
  }

  /**
   * From the floor for any information
   */
  public void getTVData(String currActionId) {
      resetCurrentSkipSize();
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
  //    TIFChannelManager.getInstance(context).getAllChannels();
      List<TIFChannelInfo> tifList = TIFChannelManager.getInstance(mContext).
          queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVData Channel tifList Length: " + tifList.size());
      List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
      boolean isCNRegion = CommonIntegration.isCNRegion();
      for (TIFChannelInfo tifChannelInfo : tifList) {
        if(tifChannelInfo.mMtkTvChannelInfo == null || (tifChannelInfo.mMtkTvChannelInfo.isUserDelete() &&
                (CommonIntegration.isEURegion() || CommonIntegration.isCNRegion()))){
          continue;
        }
          if (isCNRegion) {
              chList.add(tifChannelInfo.mMtkTvChannelInfo);
          }else {
              if(currActionId.equals(MenuConfigManager.TV_CHANNEL_SKIP) && !tifChannelInfo.mMtkTvChannelInfo.isDigitalFavoritesService()){
                  chList.add(tifChannelInfo.mMtkTvChannelInfo);
                  if (tifChannelInfo.mMtkTvChannelInfo.isSkip()) {
                      setCurrentSkipSize(getCurrentSkipSize()+1);
                }
              }else if(currActionId.equals(MenuConfigManager.TV_CHANNEL_MOVE)){
                  chList.add(tifChannelInfo.mMtkTvChannelInfo);
              }else if( !currActionId.equals(MenuConfigManager.TV_CHANNEL_SKIP)){
                  mCurrentSkipSize = 0;
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVData() tifList.get(i).mMtkTvChannelInfo.isSkip(): " + tifChannelInfo.mMtkTvChannelInfo.isSkip());
                  if(!(tifChannelInfo.mMtkTvChannelInfo.isSkip())){
                      chList.add(tifChannelInfo.mMtkTvChannelInfo);
                  }
              }
          }

      }
      getTvDataByList(chList, currActionId);

    } else {
      List<MtkTvChannelInfoBase> chList = EditChannel.getInstance(mContext).getChannelList();
      getTvDataByList(chList, currActionId);
    }
  }
  /**
   * From the floor for any information
   */
  public boolean hasNoFavChannelForSkip(String currActionId) {
      List<TIFChannelInfo> tifList = TIFChannelManager.getInstance(mContext).
          queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasNoFavChannelForSkip tifList Length: " + tifList.size());
      int channelNum = 0;
		for (TIFChannelInfo tifChannelInfo : tifList) {
			if (!TIFFunctionUtil.checkChMask(tifChannelInfo,
					TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)
					&& tifChannelInfo.mMtkTvChannelInfo != null
					&& !tifChannelInfo.mMtkTvChannelInfo
							.isDigitalFavoritesService()) {
			    channelNum++;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasNoFavChannelForSkip List" + channelNum);
			}
			if (CommonIntegration.isUSRegion()) {
			    if (channelNum > 1) {
			        return true;
                }
            }else{
                if (channelNum >= 1) {
                    return true;
                }
            }

		}
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasNoFavChannelForSkip List Length:false ");
      return false;
  }

  public boolean isM7HasNumExceed4K() {
    if (mTV.isM7ScanMode()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isM7");
      //List<TIFChannelInfo> tifList = TIFChannelManager.getInstance(mContext)
      //        .queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK,
      //                TIFFunctionUtil.CH_LIST_VAL);
      //for (TIFChannelInfo tifChannelInfo : tifList) {
      //  int channelNum = Integer.parseInt(tifChannelInfo.getDisplayNumber());
      //  if (channelNum > CommonIntegration.M7BaseNumber) {
      //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasChannelNum > 4000");
      //    return true;
      //  }
      //}
      List<MtkTvChannelInfoBase> channelListByFilter =
          MtkTvChannelList.getInstance()
              .getChannelListByMask(mCommonIntegration.getSvl(), 1, 1, 1, 0, 1);
      if (channelListByFilter != null) {
        for (MtkTvChannelInfoBase mtkTvChannelInfoBase : channelListByFilter) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelNumber == " + mtkTvChannelInfoBase.getChannelNumber());
          if (mtkTvChannelInfoBase.getChannelNumber() > CommonIntegration.M7BaseNumber) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasChannelNum > 4000");
            return true;
          }
        }
      }
    }
    return false;
  }

  private void getTvDataByList(List<MtkTvChannelInfoBase> chList, String currId) {
    List<String[]> channelInfo = new ArrayList<String[]>();

    int chNum = 0;
    if (null != chList) {
      chNum = chList.size();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Channel List Length: " + chList.size());
      if (chNum == 0) {
        setChannelInfo(channelInfo);
        setChNum(chNum);
        setChList(chList);
        changeEnable();
        return;
      }
    }
    String[] tempStr;
    int currentChannelID = EditChannel.getInstance(mContext).getCurrentChannelId();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannelID: " + currentChannelID);
    int index = 0;
    int fineindex = 0;
    int analogNum = 0;
    // MtkTvChannelInfo tempChannel;
    int tunermodel = mTV.getCurrentTunerMode();
    boolean isDvbs = false;
    if (tunermodel >= CommonIntegration.DB_SAT_OPTID) {
      isDvbs = true;
    }
    String digital =
            mContext.getResources().getString(R.string.menu_arrays_Digital);
    String analog =
            mContext.getResources().getString(R.string.menu_arrays_Analog);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "digital: " + digital+"analog"+analog);
    for (int i = 0; i < chNum; i++) {
      tempStr = new String[8];
      tempStr[7] = "0";//isAddDeleteList
      tempStr[6] = "";
      tempStr[5] = "";
      MtkTvChannelInfoBase mCurrentChannel = chList.get(i);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentChannel: " + mCurrentChannel.getChannelId());
      if (mCurrentChannel.getChannelId() == currentChannelID) {
        index = i;
      }
      if (mCurrentChannel instanceof MtkTvATSCChannelInfo) {
        MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) mCurrentChannel;
        tempStr[0] = tmpAtsc.getMajorNum() + "-" + tmpAtsc.getMinorNum();
        tempStr[1] = digital;
      } else if (mCurrentChannel instanceof MtkTvISDBChannelInfo) {
        MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) mCurrentChannel;
        int majorNum = tmpIsdb.getMajorNum();
        int minorNum = tmpIsdb.getMinorNum();
//        if (majorNum < 10) {
//        	if (minorNum < 10) {
//        		tempStr[0] = "0" + majorNum + ".0" + minorNum;
//			}else {
//				tempStr[0] = "0" + majorNum + "." + minorNum;
//			}
//        } else {
//        	if (minorNum < 10) {
//        		tempStr[0] = tmpIsdb.getMajorNum() + ".0" + minorNum;
//			}else {
//				tempStr[0] = tmpIsdb.getMajorNum() + "." + minorNum;
//			}
//        }
        tempStr[0] = majorNum + "." + minorNum;
        tempStr[1] =digital;
        tempStr[5] = tmpIsdb.getTsName();
      } else if (mCurrentChannel instanceof MtkTvAnalogChannelInfo) {
        tempStr[0] = "" + mCurrentChannel.getChannelNumber();
        tempStr[1] = analog;
        analogNum++;
        if (mCurrentChannel.getChannelId() == currentChannelID) {
          fineindex = analogNum - 1;
        }
        int tvsys = ((MtkTvAnalogChannelInfo) mCurrentChannel).getTvSys();
        int colorsys = ((MtkTvAnalogChannelInfo) mCurrentChannel).getColorSys();
        int sndsys = ((MtkTvAnalogChannelInfo) mCurrentChannel).getAudioSys();
        int indexSound = getSoundSystemIndex(tvsys, sndsys);
        String[] tvSoundSystemArray =
            mContext.getResources().getStringArray(R.array.menu_tv_sound_system_array);
        tempStr[6] = tvSoundSystemArray[indexSound];
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fineindex: " + fineindex + "analogNum" + analogNum
            + ">>" + tvsys + ">>>" + colorsys + ">>>" + sndsys);
      } else if (mCurrentChannel instanceof MtkTvDvbChannelInfo) {
        MtkTvDvbChannelInfo dvb = (MtkTvDvbChannelInfo) mCurrentChannel;
        tempStr[1] = digital;
        tempStr[0] = "" + mCurrentChannel.getChannelNumber();
        tempStr[5] = dvb.getNwName();
      }

      tempStr[2] =TvSingletons.getSingletons().getCommonIntegration().
              getAvailableString(mCurrentChannel.getServiceName());
      tempStr[3] = "" + mCurrentChannel.getChannelId();
      float fre = (float) mCurrentChannel.getFrequency();
      tempStr[4] = "" + fre;
      if (!isDvbs) {
        fre = fre / 1000000;
        BigDecimal bigDecimal = new BigDecimal(fre + "");
        tempStr[4] = "" + bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP);
      }
      channelInfo.add(tempStr);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mListViewSelectedItemData.getmItemID(): " + currId);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index: " + index);
    if (currId.equals(MenuConfigManager.TV_CHANNELFINE_TUNE)
        || currId.equals(MenuConfigManager.TV_CHANNEL_DECODE)) {
      gotoPage = fineindex / (NORMALPAGE_SIZE) + 1;
      gotoPosition = fineindex % NORMALPAGE_SIZE;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TV_CHANNELFINE_TUNE: " + gotoPage + "gotoPosition:" + gotoPosition);
    } else {
      gotoPage = index / (NORMALPAGE_SIZE) + 1;
      gotoPosition = index % NORMALPAGE_SIZE;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "gotoPage: " + gotoPage + "gotoPosition:" + gotoPosition);
    setChannelInfo(channelInfo);
    setChNum(chNum);
    setChList(chList);
    for (String[] channelStrings : channelInfo) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("forupdate", "when bind:" + channelStrings[2]);
    }
  }

  /**
   * in order to set the listview's data list
   */
  public List<String[]> setChannelInfoList(String mActionID) {
    List<String[]> mChannelInfos = new ArrayList<String[]>();
    for (int i = 0; i < getChNum(); i++) {
      MtkTvChannelInfoBase ch = null;
      if (null != getChList()) {
        ch = getChList().get(i);
      }
      if (mActionID.equals(MenuConfigManager.TV_CHANNELFINE_TUNE)
          || mActionID.equals(MenuConfigManager.TV_CHANNEL_DECODE)) {
        if (ch != null && !mTV.isAnalog(ch)) {
          continue;
        }
      }
      mChannelInfos.add(getChannelInfo().get(i));
    }
    return mChannelInfos;

  }

  /**
   * generate channel Edit's Detail info only use for sa and eu region
   *
   * @return
   */
  public List<EditItem> getChannelEditDetail(String[] mData) {
    List<EditItem> retList = new ArrayList<EditItem>();
    if (CommonIntegration.isSARegion()) {
      int chId = Integer.parseInt(mData[3]);
      MtkTvChannelInfoBase ch = mCommonIntegration.getChannelById(chId);
      EditItem tsName = new EditItem(MenuConfigManager.TV_CHANNEL_SA_TSNAME,
          mContext.getString(R.string.menu_tv_channel_tsname), mData[5], false, false,
          DataType.INPUTBOX);
      EditItem clNo = new EditItem(MenuConfigManager.TV_CHANNEL_SA_NO,
          mContext.getString(R.string.menu_tv_channel_no), mData[0], false, false,
          DataType.INPUTBOX);
      clNo.minValue = 0;
      clNo.maxValue = 9999;

      EditItem clName = new EditItem(MenuConfigManager.TV_CHANNEL_SA_NAME,
          mContext.getString(R.string.menu_tv_channel_name), mData[2], true, false,
          DataType.INPUTBOX);
      EditItem channeltype = new EditItem(MenuConfigManager.TV_CHANNEL_TYPE,
              mContext.getString(R.string.menu_tv_channel_type), mData[1], false, false,
              DataType.INPUTBOX);
      EditItem clFreq = new EditItem(MenuConfigManager.TV_FREQ_SA,
          mContext.getString(R.string.menu_tv_freq), mData[4], true, true, DataType.INPUTBOX);
      float cenfreq = Float.parseFloat(mData[4]);
      if(ch !=null && mTV.isAnalog(ch)){
    	  clFreq.minValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000- 1.2f ;
    	  clFreq.maxValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000 + 1.8f ;
		}else{
			clFreq.minValue = cenfreq - 1.5f;
			clFreq.maxValue = cenfreq + 1.5f;
		}
      // this is analog channel
      if (mData[1] != null && mData[1].equals("Analog")) {
        clFreq.isEnable = true;
      } else {// this is digital
        clFreq.isEnable = false;
      }
      retList.add(tsName);
      retList.add(clNo);
      retList.add(clName);
      retList.add(channeltype);
      retList.add(clFreq);

    } else if (CommonIntegration.isEURegion() || CommonIntegration.isEUPARegion()) {// for eu
      MtkTvChannelInfoBase ch = null;
      if (chList != null) {
        for (MtkTvChannelInfoBase mtkChannelInfoBase : chList) {
          MtkTvChannelInfoBase mCurrentChannel = mtkChannelInfoBase;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "now data[3]:" + mData[3] + ",currID:" + mCurrentChannel.getChannelId());
          int chId = Integer.parseInt(mData[3]);
          if (chId == mCurrentChannel.getChannelId()) {
            ch = mCurrentChannel;
          }
        }
      }
      // network name 2
      String itemId = MenuConfigManager.TV_CHANNEL_NW_NAME;
      if (mData[1] != null && mData[1].equals("Analog")) {
        itemId = MenuConfigManager.TV_CHANNEL_NW_ANALOG_NAME;
      }
      EditItem nwName = new EditItem(itemId,
          mContext.getString(R.string.menu_tv_network_name), mData[5], false, false,
          DataType.INPUTBOX);
      boolean isTkgsEnable =true;
      boolean isNotLcn= true;
      // "Automatic", "Customisable", "TKGS Off"
	  int tkgsmode = mConfigManager.getDefault(MenuConfigManager.TKGS_OPER_MODE);
	  boolean satOperOnly = CommonIntegration.getInstance()
                .isPreferSatMode();
	  boolean isTkgs = mTV.isTKGSOperator();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satOperOnly="+satOperOnly+"mTV.isTurkeyCountry() :"+mTV.isTurkeyCountry()+"mTV.isTKGSOperator(): "+mTV.isTKGSOperator()+"tkgsmode="+tkgsmode);

      if(tkgsmode == 0&&satOperOnly&&isTkgs){
    	  isTkgsEnable = false;
      }
      if(!isTkgsEnable || (mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) < 2 &&
              mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) !=0 &&
              !CommonIntegration.getInstance().isCurrentSourceATVforEuPA())){
          isNotLcn=false;
      }
      EditItem chNum = new EditItem(MenuConfigManager.TV_CHANNEL_NO,
          mContext.getString(R.string.menu_tv_channel_no), mData[0], isNotLcn, true, DataType.INPUTBOX);
      chNum.minValue = 0;
      chNum.maxValue = 9999;

      EditItem clName = new EditItem(MenuConfigManager.TV_CHANNEL_SA_NAME,
          mContext.getString(R.string.menu_tv_channel_name), mData[2], isTkgsEnable, false,
          DataType.INPUTBOX);
      EditItem channeltype = new EditItem(MenuConfigManager.TV_CHANNEL_TYPE,
              mContext.getString(R.string.menu_tv_channel_type), mData[1], false, false,
              DataType.INPUTBOX);
      EditItem channelFre = new EditItem(MenuConfigManager.TV_FREQ,
          mContext.getString(R.string.menu_tv_freq), mData[4], true, true, DataType.INPUTBOX);
      float cenfreq = Float.parseFloat(mData[4]);
      if(ch !=null && mTV.isAnalog(ch)){
			channelFre.minValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000- 1.2f ;
			channelFre.maxValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000 + 1.8f ;
		}else{
			channelFre.minValue = cenfreq - 1.5f;
			channelFre.maxValue = cenfreq + 1.5f;
		}
      retList.add(nwName);
      retList.add(chNum);
      retList.add(clName);
      retList.add(channeltype);
      if (null != ch && !mTV.isAnalog(ch)) {
    	channelFre.isEnable=false;
        retList.add(channelFre);
      }else{
          itemId = MenuConfigManager.TV_CHANNEL_COLOR_SYSTEM;
          int tvColorNum = 0;
          if (null != ch && mTV.isAnalog(ch)) {
            tvColorNum = ((MtkTvAnalogChannelInfo) ch).getColorSys();
            tvColorNum++;
          }
          String[] array = mContext.getResources().getStringArray(
              R.array.menu_tv_color_system_array_eu_edit);
          if (tvColorNum < 0 || tvColorNum + 1 > array.length) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Warning: colorNum changed");
            tvColorNum = 0;
          }
          EditItem colorSys = new EditItem(itemId,
              mContext.getString(R.string.menu_tv_color_system), tvColorNum, array, true,
              DataType.OPTIONVIEW);

          // sound system 7
          itemId = MenuConfigManager.TV_SOUND_SYSTEM;
      //    int tvSysNum = 0;
      //    int minNum = 1;
       //   int maxNum = 8;

          array = mContext.getResources().getStringArray(
              R.array.menu_tv_sound_system_array);
          com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Sound System Num: " + array.length);

          //if (tvSysNum < minNum || tvSysNum > maxNum) {
        //   tvSysNum = minNum;
          //}
          int index = 0;
          if (ch instanceof MtkTvAnalogChannelInfo) {
            int tvsys = ((MtkTvAnalogChannelInfo) ch).getTvSys();
            int sndsys = ((MtkTvAnalogChannelInfo) ch).getAudioSys();
            index = getSoundSystemIndex(tvsys, sndsys);
          }

          EditItem soundSys = new EditItem(itemId,
              mContext.getString(R.string.menu_tv_sound_system), index, array, true,
              DataType.OPTIONVIEW);
          retList.add(channelFre);
          retList.add(colorSys);
          retList.add(soundSys);
      }

      if (mCommonIntegration.getTunerMode() == 1 &&
          null != ch && !mTV.isAnalog(ch)) {// cable DTV channel
//  		String path = mCommonIntegration.getCurrentFocus();
	 // 	MtkTvAppTVBase app = new MtkTvAppTVBase();
		    String[] scanMode = mContext.getResources().getStringArray(
					  R.array.menu_tv_scan_mode_array);
			//	String symRateString = String.format("%07d", app.GetSymRate(path));
			//	int modu = app.GetModulation(path);
			String symRateString = "";
		    int modu = 0;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start detail: symbolrate=="+symRateString+",modu=="+modu);
				  if (ch instanceof MtkTvDvbChannelInfo) {
					MtkTvDvbChannelInfo dvbcChannel=(MtkTvDvbChannelInfo) ch;
					   symRateString=""+dvbcChannel.getSymRate();
					   modu= dvbcChannel.getMod();
					}

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "edit detail: symbolrate==" + symRateString + ",modu==" + modu);
        // QAM16,32,64,128,256
        if (modu == 4 || modu == 5 || modu == 6 || modu == 10
            || modu == 14) {
          if (modu <= 6) {
            modu -= 3;
          } else if (modu == 10) {
            modu = 4;
          } else {
            modu = 5;
          }
        } else {
          modu = 0;
        }

        EditItem modulation = new EditItem(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION,
            mContext.getString(R.string.menu_tv_sigle_modulation),
            scanMode[modu], false, false, DataType.TEXTCOMMVIEW);
        // SymbolRate , sub of RF 222
        EditItem symbolRate = new EditItem(MenuConfigManager.SYM_RATE,
            mContext.getString(R.string.menu_c_rfscan_symbol_rate),
            symRateString, false, false, DataType.TEXTCOMMVIEW);
        retList.add(modulation);
        retList.add(symbolRate);
      }
    } else if (CommonIntegration.isCNRegion()) {
      int chId = Integer.parseInt(mData[3]);
      MtkTvChannelInfoBase ch = mCommonIntegration.getChannelById(chId);
      // channel number 4
      EditItem channelNum = new EditItem(MenuConfigManager.TV_CHANNEL_NO,
          mContext.getString(R.string.menu_tv_channel_no), mData[0], true, true, DataType.INPUTBOX);
      channelNum.minValue = 0;
      channelNum.maxValue = 9999;
      // channel name 5
      EditItem channelName = new EditItem(MenuConfigManager.TV_CHANNEL_SA_NAME,
          mContext.getString(R.string.menu_tv_channel_name), mData[2], true, false,
          DataType.INPUTBOX);
      // channel frequency 6
      EditItem channelFre = new EditItem(MenuConfigManager.TV_FREQ,
          mContext.getString(R.string.menu_tv_freq), mData[4], true, true, DataType.INPUTBOX);
     /* channelFre.minValue = 0;
      channelFre.maxValue = 999.9999f;*/
      float cenfreq = Float.parseFloat(mData[4]);
      if(ch !=null && mTV.isAnalog(ch)){
			channelFre.minValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000- 1.2f ;
			channelFre.maxValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000 + 1.8f ;
		}else{
			channelFre.minValue = cenfreq - 1.5f;
			channelFre.maxValue = cenfreq + 1.5f;
		}

      // color system 4
      String[] colorSystemarray = mContext.getResources().getStringArray(
          R.array.menu_tv_color_system_array_eu_edit);
      int tvColorNum = 0;
      if (null != ch && mTV.isAnalog(ch)) {
        tvColorNum = ((MtkTvAnalogChannelInfo) ch).getColorSys();
        tvColorNum++;
      }
      if (tvColorNum < 0 || tvColorNum + 1 > colorSystemarray.length) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Warning: colorNum changed");
        tvColorNum = 0;
      }
      EditItem colorSystem = new EditItem(MenuConfigManager.TV_CHANNEL_COLOR_SYSTEM,
          mContext.getString(R.string.menu_tv_color_system), tvColorNum, colorSystemarray, true,
          DataType.OPTIONVIEW);

      // sound system 4
      colorSystemarray = mContext.getResources().getStringArray(R.array.menu_tv_sound_system_array);
      int tvSysNum = 0;
      if (ch instanceof MtkTvAnalogChannelInfo) {
        int tvsys = ((MtkTvAnalogChannelInfo) ch).getTvSys();
        int sndsys = ((MtkTvAnalogChannelInfo) ch).getAudioSys();
        tvSysNum = getSoundSystemIndex(tvsys, sndsys);
      }
      EditItem soundSystem = new EditItem(MenuConfigManager.TV_SOUND_SYSTEM,
          mContext.getString(R.string.menu_tv_sound_system), tvSysNum, colorSystemarray, true,
          DataType.OPTIONVIEW);

      // aft 4
      int aft = 0;
      colorSystemarray = mContext.getResources().getStringArray(R.array.menu_tv_fine_tune_array);
      if (ch instanceof MtkTvAnalogChannelInfo) {
        aft = ((MtkTvAnalogChannelInfo) ch).isNoAutoFineTune() ? 0 : 1;
      } else {
        aft = 0;
      }
      EditItem autoFineTune = new EditItem(MenuConfigManager.TV_AUTO_FINETUNE,
          mContext.getString(R.string.menu_tv_auto_finetune), aft, colorSystemarray, true,
          DataType.OPTIONVIEW);

      // fine tune 4
      EditItem finetuneItem = new EditItem(MenuConfigManager.TV_FINETUNE,
          mContext.getString(R.string.menu_tv_finetune), aft, null, true, DataType.HAVESUBCHILD);

      // skip 4
      int skip = 0;
      if (null != ch) {
        skip = ch.isSkip() ? 1 : 0;
      }
      colorSystemarray = mContext.getResources().getStringArray(R.array.menu_tv_skip_ch_array);
      EditItem skipItem = new EditItem(MenuConfigManager.TV_SKIP,
          mContext.getString(R.string.menu_tv_skip), skip, colorSystemarray, true,
          DataType.OPTIONVIEW);
      // store 4
      EditItem storeItem = new EditItem(MenuConfigManager.TV_STORE,
          mContext.getString(R.string.menu_tv_store), skip, colorSystemarray, true,
          DataType.HAVESUBCHILD);
      retList.add(channelNum);
      retList.add(channelName);
      if (null != ch && !mTV.isAnalog(ch)) {
        channelFre.isEnable = false;
        colorSystem.isEnable = false;
        soundSystem.isEnable = false;
        autoFineTune.isEnable = false;
        finetuneItem.isEnable = false;
        retList.add(channelFre);
      }else{
          retList.add(channelFre);
          retList.add(colorSystem);
          retList.add(soundSystem);
          retList.add(autoFineTune);
          retList.add(finetuneItem);
      }
      retList.add(skipItem);
      retList.add(storeItem);
    }
    return retList;
  }

  private int getSoundSystemIndex(int ui4TvSys, int ui4AudioSys) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i("soundsystem", "tv_sys:" + ui4TvSys + " audio_sys:" + ui4AudioSys
        + "-------------------");

    int index = -1;
    ui4TvSys = ui4TvSys & 0x0000ffff;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i("soundsystem", "tv_sys:" + ui4TvSys);
    if (ui4TvSys == MenuConfigManager.TV_SYS_MASK_L) {
      index = 2;
    }
    else if (ui4TvSys == MenuConfigManager.TV_SYS_MASK_L_PRIME) {
      index = 3;
    }
    else if (ui4TvSys == MenuConfigManager.TV_SYS_MASK_I) {
      index = 4;
    }
	else if (ui4TvSys == 49152){//MenuConfigManager.TV_SYS_MASK_M | MenuConfigManager.TV_SYS_MASK_N) {
		index = 9;
	}
    else if (ui4TvSys == (MenuConfigManager.TV_SYS_MASK_D | MenuConfigManager.TV_SYS_MASK_K))
    {
      if (ui4AudioSys == MenuConfigManager.AUDIO_SYS_MASK_FM_A2) {
        index = 6;
      }
      else if (ui4AudioSys == MenuConfigManager.AUDIO_SYS_MASK_FM_A2_DK1) {
        index = 7;
      }
      else if (ui4AudioSys == MenuConfigManager.AUDIO_SYS_MASK_FM_A2_DK2) {
        index = 8;
      }
      else {
        index = 5;
      }
    } else { /* TV_SYS_MASK_B | TV_SYS_MASK_G */

      if (ui4AudioSys == MenuConfigManager.AUDIO_SYS_MASK_FM_A2) {
        index = 1;
      }
      else {
        index = 0;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.i("soundsystem", "tv_sys:" + ui4TvSys + " audio_sys:" + ui4AudioSys + " index:"
        + index);

    return index;
  }

  /**
   * update the channel's name
   *
   * @param chId
   * @param channelName
   */
  public void updateChannelName(String chId, String channelName) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chId:" + chId + " chname:" + channelName);
    for (String[] channStrings : channelInfo) {
      if (channStrings[3].equals(chId)) {
        EditChannel.getInstance(mContext).setChannelName(
            Integer.parseInt(chId), channelName);
        channStrings[2] = channelName;
        channelName = "";
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " channStrings[2]:" + channStrings[2] + " channStrings[0]:"
            + channStrings[0]);
        break;
      }
    }
  }

  /**
   * update the channel's freq
   *
   * @param channel id
   * @param channelFreq
   */
  public void updateChannelFreq(String channelId, String channelFreq) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chId:" + channelId + "  chfreq:" + channelFreq);
    for (String[] channStrings : channelInfo) {
      if (channStrings[3].equals(channelId)) {
        EditChannel.getInstance(mContext).setChannelFreq(
            Integer.parseInt(channelId), channelFreq);
        channStrings[4] = channelFreq;
        channelFreq = "";
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " channelInfo[4]:" + channStrings[4]);
        break;
      }
    }
  }
  public List<EditItem> getChannelFreqDetail(String[] mData) {
	    List<EditItem> retList = new ArrayList<EditItem>();
	    if (CommonIntegration.isEURegion() || CommonIntegration.isEUPARegion() || CommonIntegration.isSARegion()) {// for eu
	      MtkTvChannelInfoBase ch = null;
	      if (chList != null) {
	        for (MtkTvChannelInfoBase mtkTvChannelInfoBase : chList) {
	          MtkTvChannelInfoBase mCurrentChannel = mtkTvChannelInfoBase;
	          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "now data[3]:" + mData[3] + ",currID:" + mCurrentChannel.getChannelId());
	          int chId = Integer.parseInt(mData[3]);
	          if (chId == mCurrentChannel.getChannelId()) {
	            ch = mCurrentChannel;
	          }
	        }
	      }
	   /*   String itemId = MenuConfigManager.TV_CHANNEL_NW_NAME;
	      if (mData[1] != null && mData[1].equals("Analog")) {
	        itemId = MenuConfigManager.TV_CHANNEL_NW_ANALOG_NAME;
	      }*/   //PMD
	/*      EditItem nwName = new EditItem(itemId,
	          mContext.getString(R.string.menu_tv_network_name), mData[5], false, false,
	          DataType.INPUTBOX);*/
	      /*  boolean isTkgsEnable =true,isNotLcn= true;*/ // PMD
		  int tkgsmode = mConfigManager.getDefault(MenuConfigManager.TKGS_OPER_MODE);
		  boolean satOperOnly = CommonIntegration.getInstance()
	                .isPreferSatMode();
		  boolean isTkgs = mTV.isTKGSOperator();
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satOperOnly="+satOperOnly+" mTV.isTKGSOperator(): "+isTkgs+"tkgsmode="+tkgsmode);
	      /*if((tkgsmode == 0 && satOperOnly && isTkgs)){
	    	  isTkgsEnable = false;
	      }*/
	     /* if(!isTkgsEnable || (mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) < 2 &&
	              mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) !=0 &&
	              !CommonIntegration.getInstance().isCurrentSourceATVforEuPA())){   //PMD
	          isNotLcn=false;
	      }*/
	      EditItem chNum = new EditItem(MenuConfigManager.TV_CHANNEL_NO,
	          mContext.getString(R.string.menu_tv_channel_no), mData[0], false, true, DataType.INPUTBOX);
	      chNum.minValue = 0;
	      chNum.maxValue = 9999;
	      EditItem clName = new EditItem(MenuConfigManager.TV_CHANNEL_SA_NAME,
	          mContext.getString(R.string.menu_tv_channel_name), mData[2], false, false,
	          DataType.INPUTBOX);
//	      EditItem channeltype = new EditItem(MenuConfigManager.TV_CHANNEL_TYPE,
//	              mContext.getString(R.string.menu_tv_channel_type), mData[1], false, false,
//	              DataType.INPUTBOX);
	      EditItem channelFre = new EditItem(MenuConfigManager.TV_FREQ,
	          mContext.getString(R.string.menu_tv_freq), mData[4], true, true, DataType.INPUTBOX);
	      EditItem finetuneItem = new EditItem(MenuConfigManager.TV_FINETUNE,
		          mContext.getString(R.string.menu_tv_finetune), 0, new String[]{mData[4]}, true, DataType.OPTIONVIEW);
	      float cenfreq = Float.parseFloat(mData[4]);
	      if(ch !=null && mTV.isAnalog(ch)){
				channelFre.minValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000- 1.2f ;
				channelFre.maxValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000 + 1.8f ;
			}else{
				channelFre.minValue = cenfreq - 1.5f;
				channelFre.maxValue = cenfreq + 1.5f;
			}
	      retList.add(chNum);
	      retList.add(clName);
	      retList.add(finetuneItem);
	    } else if (CommonIntegration.isCNRegion()) {
	      int chId = Integer.parseInt(mData[3]);
	      MtkTvChannelInfoBase ch = mCommonIntegration.getChannelById(chId);
	      EditItem channelNum = new EditItem(MenuConfigManager.TV_CHANNEL_NO,
	          mContext.getString(R.string.menu_tv_channel_no), mData[0], true, true, DataType.INPUTBOX);
	      channelNum.minValue = 0;
	      channelNum.maxValue = 9999;
	      EditItem channelName = new EditItem(MenuConfigManager.TV_CHANNEL_SA_NAME,
	          mContext.getString(R.string.menu_tv_channel_name), mData[2], true, false,
	          DataType.INPUTBOX);
	      EditItem channelFre = new EditItem(MenuConfigManager.TV_FREQ,
	          mContext.getString(R.string.menu_tv_freq), mData[4], true, true, DataType.INPUTBOX);
	      float cenfreq = Float.parseFloat(mData[4]);
	      if(ch !=null && mTV.isAnalog(ch)){
				channelFre.minValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000- 1.2f ;
				channelFre.maxValue =((float) ((MtkTvAnalogChannelInfo) ch).getCentralFreq()) / 1000000 + 1.8f ;
			}else{
				channelFre.minValue = cenfreq - 1.5f;
				channelFre.maxValue = cenfreq + 1.5f;
			}
	      String[] colorSystemarray = mContext.getResources().getStringArray(
	          R.array.menu_tv_color_system_array);
	      int tvColorNum = 0;
	      if (null != ch && mTV.isAnalog(ch)) {
	        tvColorNum = ((MtkTvAnalogChannelInfo) ch).getColorSys();
	        tvColorNum++;
	      }
	      if (tvColorNum < 0 || tvColorNum + 1 > colorSystemarray.length) {
	        com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Warning: colorNum changed");
	        tvColorNum = 0;
	      }
	      EditItem colorSystem = new EditItem(MenuConfigManager.TV_CHANNEL_COLOR_SYSTEM,
	          mContext.getString(R.string.menu_tv_color_system), tvColorNum, colorSystemarray, true,
	          DataType.OPTIONVIEW);
	      colorSystemarray = mContext.getResources().getStringArray(R.array.menu_tv_sound_system_array);
	      int tvSysNum = 0;
	      if (ch instanceof MtkTvAnalogChannelInfo) {
	        int tvsys = ((MtkTvAnalogChannelInfo) ch).getTvSys();
	        int sndsys = ((MtkTvAnalogChannelInfo) ch).getAudioSys();
	        tvSysNum = getSoundSystemIndex(tvsys, sndsys);
	      }
	      EditItem soundSystem = new EditItem(MenuConfigManager.TV_SOUND_SYSTEM,
	          mContext.getString(R.string.menu_tv_sound_system), tvSysNum, colorSystemarray, true,
	          DataType.OPTIONVIEW);
	      int aft = 0;
	      colorSystemarray = mContext.getResources().getStringArray(R.array.menu_tv_fine_tune_array);
	      if (ch instanceof MtkTvAnalogChannelInfo) {
	        aft = ((MtkTvAnalogChannelInfo) ch).isNoAutoFineTune() ? 0 : 1;
	      } else {
	        aft = 0;
	      }
	      EditItem autoFineTune = new EditItem(MenuConfigManager.TV_AUTO_FINETUNE,
	          mContext.getString(R.string.menu_tv_auto_finetune), aft, colorSystemarray, true,
	          DataType.OPTIONVIEW);
	      EditItem finetuneItem = new EditItem(MenuConfigManager.TV_FINETUNE,
	          mContext.getString(R.string.menu_tv_finetune), aft, null, true, DataType.HAVESUBCHILD);
	      int skip = 0;
	      if (null != ch) {
	        skip = ch.isSkip() ? 1 : 0;
	      }
	      colorSystemarray = mContext.getResources().getStringArray(R.array.menu_tv_skip_ch_array);
	      EditItem skipItem = new EditItem(MenuConfigManager.TV_SKIP,
	          mContext.getString(R.string.menu_tv_skip), skip, colorSystemarray, true,
	          DataType.OPTIONVIEW);
	      EditItem storeItem = new EditItem(MenuConfigManager.TV_STORE,
	          mContext.getString(R.string.menu_tv_store), skip, colorSystemarray, true,
	          DataType.HAVESUBCHILD);
	      retList.add(channelNum);
	      retList.add(channelName);
	      if (null != ch && !mTV.isAnalog(ch)) {
	        channelFre.isEnable = false;
	        colorSystem.isEnable = false;
	        soundSystem.isEnable = false;
	        autoFineTune.isEnable = false;
	        finetuneItem.isEnable = false;
	        retList.add(channelFre);
	      }else{
	          retList.add(channelFre);
	          retList.add(colorSystem);
	          retList.add(soundSystem);
	          retList.add(autoFineTune);
	          retList.add(finetuneItem);
	      }
	      retList.add(skipItem);
	      retList.add(storeItem);
	    }
	    return retList;
	  }


	  public boolean isChannelDeleted(String channelNum) {
//        List<TIFChannelInfo> tifList = TIFChannelManager.getInstance(mContext).
//                queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
//        Log.d(TAG, "isChannelDeleted: ");
//        float f = Float.valueOf(channelNum);
//        for (TIFChannelInfo tifChannelInfo : tifList) {
//            String num = tifChannelInfo.mMtkTvChannelInfo.getChannelNumber() + "";
//            com.mediatek.wwtv.tvcenter.util.MtkLog.i("check", "num:" + num);
//            if (num != null && f == Float.valueOf(num)) {
//              com.mediatek.wwtv.tvcenter.util.MtkLog.i("check", "num:" + num + " chno:" + "--------------------");
//              return true;
//            }
//        }
//        return false;
        int channelID = CommonIntegration.getInstance()
                .getChannelIDByBannerNum(channelNum);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelID=" + channelID);
        MtkTvChannelInfoBase chanel = CommonIntegration.getInstance()
                .getChannelById(channelID);
        if (channelID == -1 || chanel == null || chanel.getChannelNumber() != Integer.valueOf(channelNum)) {
          Log.d(TAG, "isChannelDeleted: false");
          return false;
        }
        return true;
      }
  /**
   * update channel's number
   *
   * @param chId
   * @param channelNum
   */
  public void updateChannelNumber(String chId, String channelNum) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chId:" + chId + "   channelNum:" + channelNum);
    int position = -1;
    int setPosition = -1;
    String[] ch = null;
    MtkTvChannelInfoBase selChannel = null;
    for (int i = 0; i < channelInfo.size(); i++) {
      ch = channelInfo.get(i);
      if (-1 == position && Float.valueOf(ch[0]) > Float.valueOf(channelNum)) {
        position = i;
      }
      if (ch[3].equals(chId)) {
        int nb = 0;
        try {
          nb = Integer.parseInt(channelNum);
        } catch (Exception e) {
          return;
        }

        channelInfo.get(i)[0] = String.valueOf(nb);
        setPosition = i;
        int currentId = EditChannel.getInstance(mContext).getCurrentChannelId();

        int nowId = getCurrentIdFromIdAndNum(chId, channelNum);
        chList.get(i).setChannelId(nowId);
        channelInfo.get(i)[3] = "" + nowId;
        if (Integer.parseInt(chId) == currentId) {
          mCommonIntegration.setCurrentChannelId(nowId);
        }
        EditChannel.getInstance(mContext).setChannelNumber(
            Integer.parseInt(chId),nowId, nb);
      }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "position:" + position + " setPosition:" + setPosition);
    if (-1 == position) {
      position = channelInfo.size();// not size()-1;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "position:" + position + " setPosition:" + setPosition);
    ch = channelInfo.get(setPosition);
    selChannel = chList.get(setPosition);
    if (setPosition < position) {
      int i = setPosition;
      for (; i < (position - 1); i++) {
        channelInfo.set(i, channelInfo.get(i + 1));
        chList.set(i, chList.get(i + 1));
      }
      channelInfo.set(i, ch);
      chList.set(i, selChannel);
    } else {
      int i = setPosition;
      for (; i > position; i--) {
        channelInfo.set(i, channelInfo.get(i - 1));
        chList.set(i, chList.get(i - 1));
      }
      channelInfo.set(i, ch);
      chList.set(i, selChannel);
    }
  }

  /**
   * update channel's number
   *
   * @param chId
   * @param channelNum
   */
  public void updateChannelNumberCnRegion(String chId, String channelNum) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chId:" + chId + "   channelNum:" + channelNum);
    int position = -1;
    int setPosition = -1;
    String[] ch = null;
    MtkTvChannelInfoBase selChannel = null;
    for (int i = 0; i < channelInfo.size(); i++) {
      ch = channelInfo.get(i);
      if (-1 == position && Float.valueOf(ch[0]) > Float.valueOf(channelNum)) {
        position = i;
      }
      if (ch[3].equals(chId)) {
        int nb = 0;
        try {
          nb = Integer.parseInt(channelNum);
        } catch (Exception e) {
          return;
        }

        channelInfo.get(i)[0] = String.valueOf(nb);
        setPosition = i;
        int currentId = EditChannel.getInstance(mContext).getCurrentChannelId();
        if (mTV.isCurrentSourceATV()) {
          int nowId = getCurrentIdFromIdAndNum(chId, channelNum);
          chList.get(i).setChannelId(nowId);
          channelInfo.get(i)[3] = "" + nowId;
          if (Integer.parseInt(chId) == currentId) {
            mCommonIntegration.setCurrentChannelId(nowId);
          }
          EditChannel.getInstance(mContext).setChannelNumber(
              Integer.parseInt(chId), nb);
        } else {
          int nowId = Integer.parseInt(chId);
          if (mCommonIntegration.getTunerMode() == 0) {
             nowId = getCurrentIdFromIdAndNumForCNDTMB(chId,channelNum);
          } else {
             nowId = getCurrentIdFromIdAndNumForCNCE(chId, channelNum);
          }
          chList.get(i).setChannelId(nowId);
          channelInfo.get(i)[3] = "" + nowId;
          if (Integer.parseInt(chId) == currentId) {
            mCommonIntegration.setCurrentChannelId(nowId);
          }
          EditChannel.getInstance(mContext).setChannelNumber(
                  Integer.parseInt(chId),nowId, nb);
        }
        break;
      }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "position:" + position + " setPosition:" + setPosition);
    if (-1 == position) {
      position = channelInfo.size();// not size()-1;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "position:" + position + " setPosition:" + setPosition);
    ch = channelInfo.get(setPosition);
    selChannel = chList.get(setPosition);
    if (setPosition < position) {
      int i = setPosition;
      for (; i < (position - 1); i++) {
        channelInfo.set(i, channelInfo.get(i + 1));
        chList.set(i, chList.get(i + 1));
      }
      channelInfo.set(i, ch);
      chList.set(i, selChannel);
    } else {
      int i = setPosition;
      for (; i > position; i--) {
        channelInfo.set(i, channelInfo.get(i - 1));
        chList.set(i, chList.get(i - 1));
      }
      channelInfo.set(i, ch);
      chList.set(i, selChannel);
    }
  }

  public int getCurrentIdFromIdAndNumForCNDTMB(String strId, String strNum) {
    int major = Integer.parseInt(strNum);
    int index = Integer.parseInt(strId);
    int currentid = index;
    index = index & 0x3f;
    int lcn = (currentid >> 8) & 0x3ff;
    currentid = ((major & 0x3fff) << 18) | ((lcn & 0x3ff) << 8) | ((index & 0x3f)) | 0x80;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "getCurrentIdFromIdAndNumForCNDTMB:currentid:" + currentid);
    return currentid;
  }

  public int getCurrentIdFromIdAndNumForCNCE(String strId, String strNum) {
    int major = Integer.parseInt(strNum);
    int index = Integer.parseInt(strId);
    int currentid = index;
    index = index & 0xf;
    int lcn = ((currentid >> 8) & 0x3ff) | ((currentid & 0x60) << 5);
    currentid = ((major & 0x3fff) << 18) | ((lcn & 0x3ff) << 8) | ((index & 0xf)) | 0x80
        | ((lcn & 0xc00) >> 5);
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "getCurrentIdFromIdAndNumForCNCE:currentid:" + currentid);
    return currentid;
  }

  public int getCurrentIdFromIdAndNum(String strId, String strNum) {
    int major = Integer.parseInt(strNum);
    int index = Integer.parseInt(strId);
    int currentid = index;
    index = index & 0xf;
    currentid = ((major & 0x3fff) << 18) | ((index & 0xf)) | 0x80;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "getCurrentIdFromIdAndNum:currentid:" + currentid);
    return currentid;
  }

  public void updateChannelColorSystem(int colorSystemIndex, String[] mData) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "colorSystemIndex:" + colorSystemIndex);
    MtkTvChannelInfoBase mCurrentEditChannel = mCommonIntegration.getChannelById(
        Integer.parseInt(mData[3]));
    if (mCurrentEditChannel != null && mTV.isAnalog(mCurrentEditChannel)) {
      ((MtkTvAnalogChannelInfo) mCurrentEditChannel)
          .setColorSys(colorSystemIndex - 1);
    }
    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    list.add(mCurrentEditChannel);
    mCommonIntegration.setChannelList(
        MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    int chId=0;
    if(mCurrentEditChannel!=null){
        chId= mCurrentEditChannel.getChannelId();
    }
    for (MtkTvChannelInfoBase mtkTvChannelInfoBase: chList) {
      MtkTvChannelInfoBase mTempChannel = mtkTvChannelInfoBase;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateChannelColorSystem,currID:" + mTempChannel.getChannelId());
      if (chId == mTempChannel.getChannelId()) {
        if (mTV.isAnalog(mTempChannel)) {
          ((MtkTvAnalogChannelInfo) mTempChannel)
              .setColorSys(colorSystemIndex - 1);
          break;
        }
      }
    }
  }

  public void updateChannelSoundSystem(int ui2Idx, String[] mData) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Analog setSoundSystem:ui2Idx:" + ui2Idx);
    MtkTvChannelInfoBase mCurrentEditChannel = mCommonIntegration.getChannelById(
        Integer.parseInt(mData[3]));
    if (!(mCurrentEditChannel instanceof MtkTvAnalogChannelInfo)) {
        return;
    }
    int ui4ReserveMask = ((MtkTvAnalogChannelInfo) mCurrentEditChannel).getTvSys() & 0xffff0000;

    int ui4TvSys = 0;
    int ui4AudioSys = 0;

    switch (ui2Idx)
    {
      case 0:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_B | MenuConfigManager.TV_SYS_MASK_G;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_MONO
            | MenuConfigManager.AUDIO_SYS_MASK_NICAM;
        break;
      case 1:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_B | MenuConfigManager.TV_SYS_MASK_G;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_A2;
        break;
      case 2:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_L;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_AM
            | MenuConfigManager.AUDIO_SYS_MASK_NICAM;
        break;
      case 3:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_L_PRIME;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_AM
            | MenuConfigManager.AUDIO_SYS_MASK_NICAM;
        break;
      case 4:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_I;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_MONO
            | MenuConfigManager.AUDIO_SYS_MASK_NICAM;
        break;
      case 5:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_D | MenuConfigManager.TV_SYS_MASK_K;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_MONO
            | MenuConfigManager.AUDIO_SYS_MASK_NICAM;
        break;
      case 6:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_D | MenuConfigManager.TV_SYS_MASK_K;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_A2;
        break;
      case 7:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_D | MenuConfigManager.TV_SYS_MASK_K;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_A2_DK1;
        break;
      case 8:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_D | MenuConfigManager.TV_SYS_MASK_K;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_A2_DK2;
        break;
	  case 9:
		ui4TvSys = MenuConfigManager.TV_SYS_MASK_M | MenuConfigManager.TV_SYS_MASK_N;
		ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_BTSC;
		break;
      default:
        ui4TvSys = MenuConfigManager.TV_SYS_MASK_B | MenuConfigManager.TV_SYS_MASK_G;
        ui4AudioSys = MenuConfigManager.AUDIO_SYS_MASK_FM_MONO
            | MenuConfigManager.AUDIO_SYS_MASK_NICAM;
        break;
    }

    ui4TvSys = ui4TvSys | ui4ReserveMask;
//    ui4AudioSys = ui4AudioSys;
    ((MtkTvAnalogChannelInfo) mCurrentEditChannel).setTvSys(ui4TvSys);
    ((MtkTvAnalogChannelInfo) mCurrentEditChannel).setAudioSys(ui4AudioSys);

    int chId = mCurrentEditChannel.getChannelId();
    for (MtkTvChannelInfoBase mtkTvChannelInfoBase : chList) {
      MtkTvChannelInfoBase mTempChannel = mtkTvChannelInfoBase;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateChannelSoundSystem,currID:" + mTempChannel.getChannelId());
      if (chId == mTempChannel.getChannelId()) {
        if (mTV.isAnalog(mTempChannel)) {
          ((MtkTvAnalogChannelInfo) mTempChannel).setTvSys(ui4TvSys);
          ((MtkTvAnalogChannelInfo) mTempChannel).setAudioSys(ui4AudioSys);
          break;
        }
      }
    }

    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    list.add(mCurrentEditChannel);
    mCommonIntegration.setChannelList(
        MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    for (String[] cStrings : channelInfo) {
      if (cStrings[3].equals(mData[3])) {
        int tvsys = ((MtkTvAnalogChannelInfo) mCurrentEditChannel).getTvSys();
      //  int colorsys = ((MtkTvAnalogChannelInfo) mCurrentEditChannel).getColorSys();
        int sndsys = ((MtkTvAnalogChannelInfo) mCurrentEditChannel).getAudioSys();
        int indexSound = getSoundSystemIndex(tvsys, sndsys);
        String[] tvSoundSystemArray = mContext.getResources().getStringArray(
            R.array.menu_tv_sound_system_array);
        cStrings[6] = tvSoundSystemArray[indexSound];
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " channelInfo.get(i)[6]:" + cStrings[6] + " channelInfo.get(i)[0]:"
            + cStrings[0]);
        break;
      }
    }
  }

  /**
   * @param chNumSrc
   * @param chNumDst
   */
  public String[] updateChannelData(int chNumSrc, int chNumDst) {
    int src = -1;
    int dst = -1;
    for (int i = 0; i < channelInfo.size(); i++) {
      if (channelInfo.get(i)[3].equals(String.valueOf(chNumSrc))) {
        src = i;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort src:" + src + ",[" + i + "]="
            + channelInfo.get(i)[2]);
        if (dst != -1) {
          break;
        }
      }
      if (channelInfo.get(i)[3].equals(String.valueOf(chNumDst))) {
        dst = i;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort dst:" + dst + ",[" + i + "]="
            + channelInfo.get(i)[2]);
        if (src != -1) {
          break;
        }
      }

    }
    String[] newChannelIds = new String[2];
    if (src != -1 && dst != -1) {
      //int srcid = EditChannel.getInstance(mContext).getNewChannelId(chNumSrc, chNumDst);
      //int dstid = EditChannel.getInstance(mContext).getNewChannelId(chNumDst, chNumSrc);
      int dstid = chNumSrc;
      int srcid = chNumDst;
      channelInfo.get(src)[3] = "" + srcid;
      channelInfo.get(dst)[3] = "" + dstid;
      chList.get(src).setChannelId(srcid);
      chList.get(dst).setChannelId(dstid);
      String[] termpStr = channelInfo.get(src);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort src:" + termpStr[3] + ",dest==" + channelInfo.get(dst)[3]);
      MtkTvChannelInfoBase selChannel = chList.get(src);
      channelInfo.set(src, channelInfo.get(dst));
      channelInfo.set(dst, termpStr);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          "channelSort 22src:" + channelInfo.get(src)[3] + ",dest==" + channelInfo.get(dst)[3]);

      newChannelIds[0] = channelInfo.get(src)[3];
      newChannelIds[1] = channelInfo.get(dst)[3];

      chList.set(src, chList.get(dst));
      chList.set(dst, selChannel);
      // String name = channelInfo.get(src)[2];
      // String type = channelInfo.get(src)[1];
      String listno = channelInfo.get(src)[0];
      // channelInfo.get(src)[2] = channelInfo.get(dst)[2];
      // channelInfo.get(src)[1] = channelInfo.get(dst)[1];
      channelInfo.get(src)[0] = channelInfo.get(dst)[0];
      // channelInfo.get(dst)[2] = name;
      // channelInfo.get(dst)[1] = type;
      channelInfo.get(dst)[0] = listno;
    }

    for (String[] chanStrings : channelInfo) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dump num:" + chanStrings[3] + ",name:" + chanStrings[2]);
    }
    return newChannelIds;
  }

  /**
   * for analog channel
   *
   * @param chId
   * @param isFinetune is or not support fine tune
   */
  public void updateChannelIsFine(String chId, int isFinetune) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chId:" + chId + "  isFinetune:" + isFinetune);
    for (String[] chanStrings : channelInfo) {
      if (chanStrings[3].equals(chId)) {
        MtkTvChannelInfoBase selChannel = mCommonIntegration.getChannelById(Integer.parseInt(chId));
        if (selChannel instanceof MtkTvAnalogChannelInfo) {
          ((MtkTvAnalogChannelInfo) selChannel).setNoAutoFineTune((isFinetune == 0) ? true : false);
          List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
          list.add(selChannel);
          mCommonIntegration.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " chanStrings[4]:" + chanStrings[4]);
        break;
      }
    }
  }

  public void updateChannelSkip(String chId, int skip) {
    MtkTvChannelInfoBase selChannel = mCommonIntegration.getChannelById(Integer.parseInt(chId));
    if (null != selChannel) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSkip:" + skip);
      selChannel.setSkip(skip == 0 ? false : true);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(selChannel);
      mCommonIntegration.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chId + "setChannelSkip selChannel is null");
    }
  }

  /**
   * store channel data
   *
   * @param channelNumber channel number
   * @param channelName channel name
   * @param channelFrequency channel frequency
   * @param colorSystem color system
   * @param soundSystem sound system
   * @param aft auto fine tune
   * @param skip skip flag
   */
  public void storeChannel(int channelNumber, String channelName,
      String channelFrequency, int colorSystem, int soundSystem,
      int autoFineTune, int skip) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelNumber>>" + channelNumber + ">>" + channelName + ">>" + channelFrequency
        + ">>>" + colorSystem
        + ">>>" + soundSystem + ">>>" + autoFineTune + ">>>" + skip);
    MtkTvChannelInfoBase currentChannel = mCommonIntegration.getCurChInfo();
    if (null != currentChannel) {
      currentChannel.setChannelNumber(channelNumber);
      currentChannel.setServiceName(channelName);
      // int pwdShow = MtkTvPWDDialog.getInstance().PWDShow();
      if (currentChannel instanceof MtkTvAnalogChannelInfo) {
        currentChannel.setFrequency((int) (Float.parseFloat(channelFrequency) * 1000000));
        // if (pwdShow != 1) {
        // ((MtkTvAnalogChannelInfo)currentChannel).setNoAutoFineTune(true);
        // } else {
        ((MtkTvAnalogChannelInfo) currentChannel).setNoAutoFineTune(autoFineTune == 0 ? true
            : false);
        // }
      }
      currentChannel.setSkip(skip != 0 ? true : false);
      List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
      list.add(currentChannel);
      mCommonIntegration.setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
    }
  }


  /**
   * @param parentItem
   */
  public List<SatItem> buildDVBSInfoItem(Action parentItem, SatelliteInfo info) {
    List<SatItem> lists = new ArrayList<SatItem>();
    parentItem.mSubChildGroup = new ArrayList<Action>();
    if(info == null){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "info null return!!!");
      return  lists;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "info.getEnable()>>" + info.getEnable());
    int index = 0;
  /*  String title = "";
    if (parentItem.mItemID == MenuConfigManager.DVBS_SAT_MANUAL_TURNING) {
      title = MenuConfigManager.DVBS_SAT_MANUAL_TURNING;
    }*/
    parentItem.satID = info.getSatlRecId();
    SatItem item =null;
    int antennaType = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SAT_ANTENNA_TYPE);
    if(antennaType==MenuConfigManager.DVBS_ACFG_SINGLE ||
            antennaType==MenuConfigManager.DVBS_ACFG_TONEBURST ||
            antennaType==MenuConfigManager.DVBS_ACFG_DISEQC10 ||
            antennaType==MenuConfigManager.DVBS_ACFG_DISEQC11 ||
            antennaType==MenuConfigManager.DVBS_ACFG_DISEQC12){
        item = new SatItem(info.getSatlRecId(), "" + index, info.getSatName(),
                buildStatusString(info), info.getEnable(), parentItem, mContext.getResources().getString(R.string.dvbs_satellite_detail));
    }else if(antennaType==1){
        item = new SatItem(info.getSatlRecId(), "" + index, info.getSatName(),
                info.getType(), info.getEnable(), parentItem,mContext.getResources().getString(R.string.dvbs_satellite_detail));
    }else if(antennaType==2){
        item = new SatItem(info.getSatlRecId(), "" + index, info.getSatName(),
                info.getType(), info.getEnable(), parentItem,mContext.getResources().getString(R.string.dvbs_satellite_detail));
    }
    lists.add(item);
    return lists;
  }

  /**
   * @param parentItem
   * @param satellites
   * @param fliterType 0:all sat;1:enable sat; 2:disable sat.
   */
  public List<SatItem> buildDVBSSATDetailInfo(Action parentItem, List<SatelliteInfo> satellites,
      int fliterType) {
    List<SatItem> lists = new ArrayList<SatItem>();
    final int allSat = 0;
    final int onlyEnableSat = 1;
    final int onlyDisableSat = 2;

    parentItem.mSubChildGroup = new ArrayList<Action>();
    int antennaType = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SAT_ANTENNA_TYPE);
    String[]  positionArray= mContext.getResources().getStringArray(R.array.dvbs_single_cable_position_arrays);
    if(antennaType==2){
        positionArray = mContext.getResources().getStringArray(R.array.dvbs_jess_cable_position_arrays);
    }
    // for(SatelliteInfo info:satellites){
    int index = 0;
    int size = satellites.size();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "buildDVBSSATDetailInfo antennaType="+antennaType+",size="+size);
    for (int i = 0; i < size; i++) {
      SatelliteInfo info = satellites.get(i);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "info.getEnable()>>" + info.getEnable() + ">>>" + fliterType);
      switch (fliterType) {
        case onlyEnableSat:
          if (!info.getEnable()) {
            continue;
          }
          break;
        case onlyDisableSat:
          if (info.getEnable()) {
            continue;
          }
          break;
        case allSat:
        default:
          break;
      }
      index++;

    /*  String title = "";
      if (parentItem.mItemID == MenuConfigManager.DVBS_SAT_MANUAL_TURNING) {
        title = MenuConfigManager.DVBS_SAT_MANUAL_TURNING;
      }*/
      parentItem.satID = info.getSatlRecId();
      SatItem item =null;
      String type = "";//new UI no need this infomation
      int defaultPosition = Math.max(info.getPosition()-1, 0);
      defaultPosition = Math.min(defaultPosition, positionArray.length-1);

      if(antennaType==0 ||
              antennaType==MenuConfigManager.DVBS_ACFG_SINGLE ||
              antennaType==MenuConfigManager.DVBS_ACFG_TONEBURST ||
              antennaType==MenuConfigManager.DVBS_ACFG_DISEQC10 ||
              antennaType==MenuConfigManager.DVBS_ACFG_DISEQC11 ||
              antennaType==MenuConfigManager.DVBS_ACFG_DISEQC12){
          item = new SatItem(info.getSatlRecId(), "" + index, info.getSatName(),
                  buildStatusString(info), info.getEnable(), parentItem, mContext.getResources().getString(R.string.dvbs_satellite_detail));
      }else if(antennaType==1){
          //type = positionArray[defaultPosition];
          item = new SatItem(info.getSatlRecId(), "" + index, info.getSatName(),
                  type, info.getEnable(), parentItem,mContext.getResources().getString(R.string.dvbs_satellite_detail));
      }else if(antennaType==2){
          //type = positionArray[defaultPosition];
          item = new SatItem(info.getSatlRecId(), "" + index, info.getSatName(),
                  type, info.getEnable(), parentItem,mContext.getResources().getString(R.string.dvbs_satellite_detail));
      }
      lists.add(item);
      if (fliterType == onlyDisableSat) {
        break;
      }
    }
    return lists;
  }

  private String buildStatusString(SatelliteInfo info){
      //new UI no need this infomation
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "buildStatusString ()>>"+info);
      return "";

      /*if(com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag) com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"sat:"+info.toString());
      final String[] diseqc10PortList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_10_port_sub_arrays);
      final String[] diseqc11PortList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_11_port_sub_arrays);
      final String[] diseqcMotorList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_motor_arrays);;

      int motoType=0;
      int diseqc10Port=0;
      int diseqc11Port=0;
      if(info.getMotorType()==5){
          motoType=1;
      }else{
          motoType=0;
      }
      if(info.getDiseqcType()==0||info.getPort()==255){
          diseqc10Port=0;
      }else{
          diseqc10Port=info.getPort()+1 > diseqc10PortList.length - 1 ? diseqc10PortList.length - 1 : info.getPort()+1;
      }
      if(info.getDiseqcTypeEx()==0||info.getPortEx()==255){
          diseqc11Port=0;
      }else{
          diseqc11Port=info.getPortEx()+1 > diseqc11PortList.length - 1 ? diseqc11PortList.length -1 : info.getPortEx()+1;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "id:"+info.getSatlRecId()+",10port:"+diseqc10Port+
              ",11port:"+diseqc11Port+
              ",MotorType:"+motoType
              +",enable:"+info.getEnable());
      return diseqcMotorList[motoType]+"+"+
             diseqc11PortList[diseqc11Port]+"+"+
             diseqc10PortList[diseqc10Port];*/
  }
  List<Action> buildDVBSScanItem(Action parentItem, int satID) {
    List<Action> items = new ArrayList<Action>();

    List<String> scanModeList = ScanContent.getDVBSScanMode(mContext);
    List<String> scanChannels = ScanContent.getDVBSConfigInfoChannels(mContext);

    String scanModeTitle = mContext.getResources().getString(R.string.dvbs_scan_mode);
    final Action scanMode = new Action(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG,
        scanModeTitle,
        MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
        0, scanModeList.toArray(new String[0]),
        MenuConfigManager.STEP_VALUE,
        Action.DataType.OPTIONVIEW);
    scanMode.satID = satID;
    scanMode.mLocationId = parentItem.mLocationId;
    scanMode.setmParent(parentItem);
    scanMode.setmParentGroup(parentItem.mParent.mSubChildGroup);
    items.add(scanMode);
    if (scanModeList.size() > 1) {
      scanMode.setEnabled(true);
    } else {
      scanMode.setEnabled(false);
    }
    String channelsTitle = mContext.getResources().getString(R.string.dvbs_satellite_channel);
    final Action scanChannel = new Action(
        MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG, channelsTitle,
        MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
        1, scanChannels.toArray(new String[0]),
        MenuConfigManager.STEP_VALUE,
        Action.DataType.OPTIONVIEW);
    scanChannel.satID = satID;
    scanChannel.mLocationId = parentItem.mLocationId;
    scanChannel.setmParent(parentItem);
    scanChannel.setmParentGroup(parentItem.mParent.mSubChildGroup);
    items.add(scanChannel);

    String scanTitle = mContext.getResources().getString(R.string.menu_c_scan);
    Action satelliteType = new Action(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN,
        scanTitle, MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE, null,
        MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
    satelliteType.satID = satID;
    satelliteType.mLocationId = parentItem.mLocationId;
    satelliteType.setmParent(parentItem);
    satelliteType.setmParentGroup(parentItem.mParent.mSubChildGroup);
    items.add(satelliteType);

    return items;
  }

  /**
   * check chaannel-no is existed in cu
   *
   * @param channelinfo
   * @param ch
   * @return
   */
//  public boolean checkDuplicate(String chno) {
//    boolean isduplicate = false;
//    if (channelInfo == null) {
//      return false;
//    }
//
//    float f = Float.valueOf(chno);
//    for (String[] chanStrings: channelInfo) {
//      String num = chanStrings[0];
//      com.mediatek.wwtv.tvcenter.util.MtkLog.i("check", "num:" + num);
//      if (num != null && f == Float.valueOf(num)) {
//        com.mediatek.wwtv.tvcenter.util.MtkLog.i("check", "num:" + num + " chno:" + chno + "--------------------");
//        isduplicate = true;
//      }
//    }
//    return isduplicate;
//  }

  /**
   * action item will be add or remove when change turnermode
   *
   * @param adapter
   * @param tvFirstVoice
   * @param tvFirstLanguageEU
   * @param tvSecondLanguageEU
   */
  public void changeItemAfterTurn(ActionFragment frag, Action tvFirstVoice,
      Action tvFirstLanguageEU,
      Action tvSecondLanguageEU) {
    if (frag != null) {
      ActionAdapter adapter = (ActionAdapter) frag.getAdapter();
      List<Action> group = adapter.getActions();
      if (CommonIntegration.isCNRegion()) {
        if (mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_LANGUAGE_ATTR)) {
          if (!group.contains(tvFirstLanguageEU)) {
            group.add(1, tvFirstLanguageEU);
            group.add(2, tvSecondLanguageEU);
          }
        } else {
          group.remove(tvFirstLanguageEU);
          group.remove(tvSecondLanguageEU);
        }
      } else {
        if (mCommonIntegration.isCurrentSourceDTV()) {
          group.remove(tvFirstVoice);
        }
        if (mCommonIntegration.isCurrentSourceATV() || !mTV.iCurrentInputSourceHasSignal()) {
          if (!group.contains(tvFirstVoice)) {
            if (mTV.isAusCountry()) {
              group.add(2, tvFirstVoice);
            } else {
              group.add(1, tvFirstVoice);
            }
          }
        }
        if (mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_LANGUAGE_ATTR)) {
          if (!group.contains(tvFirstLanguageEU)) {
            if (mTV.isAusCountry()) {
              group.add(2, tvFirstLanguageEU);
              group.add(3, tvSecondLanguageEU);
            } else {
              group.add(1, tvFirstLanguageEU);
              group.add(2, tvSecondLanguageEU);
            }
          }
        } else {
          group.remove(tvFirstLanguageEU);
          group.remove(tvSecondLanguageEU);
        }
        adapter.notifyDataSetChanged();
      }
    }
  }

  /**
   * // * @param item // * @param atTPInfo // * true: TP page, false: detail info page. //
   */
  public void saveDVBSSatTPInfo(Context context, int satID, ScrollAdapterView listview) {
    MtkTvScanDvbsBase.MtkTvScanTpInfo tpInfo = new MtkTvScanDvbsBase().new MtkTvScanTpInfo();
    int[] transInfos = new int[3];
    for (int i = 0; i < 3; i++) {
      Action action = (Action) listview.getChildAt(i).getTag(R.id.action_title);
      transInfos[i] = action.mInitValue;
      if (i < 2) {
        action.setDescription("" + transInfos[i]);
      }
    }
    tpInfo.i4Frequency = transInfos[0];
    tpInfo.i4Symbolrate = transInfos[1];
    switch (transInfos[2]) {
      case 0:
        tpInfo.ePol = TunerPolarizationType.POL_LIN_HORIZONTAL;
        break;
      case 1:
        tpInfo.ePol = TunerPolarizationType.POL_LIN_VERTICAL;
        break;
      case 2:
        tpInfo.ePol = TunerPolarizationType.POL_CIR_LEFT;
        break;
      case 3:
        tpInfo.ePol = TunerPolarizationType.POL_CIR_RIGHT;
        break;
      default:
        break;
    }
    ScanContent.setDVBSTPInfo(context,satID, tpInfo);

  }

  public void saveDVBSSatTPInfo(Context context, int satID, String value) {
      MtkTvScanDvbsBase.MtkTvScanTpInfo tpInfo = ScanContent.getDVBSTransponder(satID);
      String[] tpStrings = context.getResources().getStringArray(R.array.dvbs_tp_pol_arrays);
      if(tpStrings[0].equals(value)){
          tpInfo.ePol = TunerPolarizationType.POL_LIN_HORIZONTAL;
      }else if(tpStrings[1].equals(value)){
          tpInfo.ePol = TunerPolarizationType.POL_LIN_VERTICAL;
      }else if(tpStrings[2].equals(value)){
          tpInfo.ePol = TunerPolarizationType.POL_CIR_LEFT;
      }else if(tpStrings[3].equals(value)){
          tpInfo.ePol = TunerPolarizationType.POL_CIR_RIGHT;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tpInfo.ePol="+tpInfo.ePol+",tpInfo="+tpInfo);
      ScanContent.setDVBSTPInfo(context, satID, tpInfo);
    }

  /**
   * for diseqc 1.2 set tuner port
   */
  public void setDiseqc10TunerPort(int value) {
      final int initValue = value - 1;
      TVAsyncExecutor.getInstance().execute(new Runnable() {
          @Override
          public void run() {
              MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
              MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = ScanDvbsRet.SCAN_DVBS_RET_INTERNAL_ERROR;
              boolean isNeedReset = SatDetailUI.getInstance(mContext).mDvbsNeedTunerReset;
              switch (initValue) {
                  case -1:
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc10TunerPort>>dvbsSetTunerDiseqc10Disable>dvbsRet>" + dvbsRet);
                      dvbsRet = dvbsScan.dvbsSetTunerDiseqc10Disable();
                      break;
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                      dvbsRet = dvbsScan.dvbsSetTunerDiseqc10Port(initValue);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc10TunerPort>>dvbsSetTunerDiseqc10Port>dvbsRet>" + dvbsRet);
                      break;
                  case 4:
                      if (isNeedReset) {
                          dvbsRet = dvbsScan.dvbsSetTunerDiseqc10Reset();
                      }
                      dvbsRet = dvbsScan.dvbsSetTunerDiseqc10ToneBurst(0);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                              "setDiseqc10TunerPort>>mDvbsNeedTunerReset dvbsSetTunerDiseqc10ToneBurst A>dvbsRet>"
                                      + dvbsRet
                                      + ">>>" + isNeedReset);
                      break;
                  case 5:
                      if (isNeedReset) {
                          dvbsRet = dvbsScan.dvbsSetTunerDiseqc10Reset();
                      }
                      dvbsRet = dvbsScan.dvbsSetTunerDiseqc10ToneBurst(1);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                              "setDiseqc10TunerPort>>mDvbsNeedTunerReset dvbsSetTunerDiseqc10ToneBurst B>dvbsRet>"
                                      + dvbsRet
                                      + ">>>" + isNeedReset);
                      break;
                  case 6:
                      dvbsRet = dvbsScan.dvbsSetTunerDiseqc10Disable();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc10TunerPort>>dvbsSetTunerDiseqc10Disable>dvbsRet>" + dvbsRet);
                      break;
                  default:
                      break;
              }
          }
      });
  }
  /**
   * for diseqc 1.1 set tuner port
   */
  public void setDiseqc11TunerPort(int initValue) {
      TVAsyncExecutor.getInstance().execute(new Runnable() {
          @Override
          public void run() {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setDiseqc11TunerPort initValue:"+initValue);
              MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
              if(initValue==0){
                  dvbsScan.dvbsSetTunerDiseqc11Disable();
              }else{
                  dvbsScan.dvbsSetTunerDiseqc11Port(initValue-1);
              }
          }
      });
  }
  /**
   * for diseqc 1.2 set motor page tuner
   */
  public void setDiseqc12MotorPageTuner(String mId, int initValue) {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = ScanDvbsRet.SCAN_DVBS_RET_INTERNAL_ERROR;
    //need to fix
    if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_DISABLE_LIMITS)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12DisableLimits();
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_LIMIT_EAST)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12LimitEast();
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_LIMIT_WEST)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12LimitWest();
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12StorePos(initValue + 1);
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12GotoPos(initValue + 1);
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_REFERENCE)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12GotoPos(0);
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_GOTOXX)){
        int longitude = MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE);
        int latitude = MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE);
        int orbitPos = MenuConfigManager.getInstance(mContext).getValueFromPrefer(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION);
        //orbit -1800~1800  longitude -1800~1800 latitude -900~900
        MtkTvScanDvbsBase.MtkTvGotoXXInfo xxInfo = dvbsScan.new MtkTvGotoXXInfo();
        xxInfo.i4SatOrbit = orbitPos;
        xxInfo.i4MyLongitude = longitude;
        xxInfo.i4MyLatitude = latitude;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc12MotorPageTuner GOTOXX" + xxInfo.i4SatOrbit + ">>" + xxInfo.i4MyLongitude
                + ">>>" + xxInfo.i4MyLatitude);
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12GotoPosXX(xxInfo);
    }
  //need to fix
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc12MotorPageTuner>>dataItem>dvbsRet>" + dvbsRet + ">>" + mId
        + ">>>" + initValue);
  }

  /**
   * for diseqc 1.2 set movement control page tuner
   */
  public void setDiseqc12MovementControlPageTuner(String mId) {
    SaveValue savevalue = SaveValue.getInstance(mContext);
    int defaultMovementControl = savevalue
        .readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL);
    int stepSize = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE);
    int timeout = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS);
    if (stepSize == 0){
      stepSize = 1;
    }
    if (timeout == 0) {
      timeout = 1;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc12MovementControlPageTuner>>dataItem>>" + defaultMovementControl
        + ">>>" + stepSize + ">>" + timeout);
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = ScanDvbsRet.SCAN_DVBS_RET_INTERNAL_ERROR;
  //need to fix
    if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_EAST)) {
      if (defaultMovementControl == 0) {
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12MoveEast(0);
      } else if (defaultMovementControl == 1) {// step
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12MoveEast(stepSize - stepSize * 2);
      } else if (defaultMovementControl == 2) {// timeout
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12MoveEast(timeout);
      }
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_WEST)) {
      if (defaultMovementControl == 0) {
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12MoveWest(0);
      } else if (defaultMovementControl == 1) {// step
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12MoveWest(stepSize - stepSize * 2);
      } else if (defaultMovementControl == 2) {// timeout
        dvbsRet = dvbsScan.dvbsSetTunerDiseqc12MoveWest(timeout);
      }
    } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STOP_MOVEMENT)) {
      dvbsRet = dvbsScan.dvbsSetTunerDiseqc12StopMove();
    }
    //need to fix
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setDiseqc12MovementControlPageTuner>>dataItem>dvbsRet>" + dvbsRet + ">>>"
        + defaultMovementControl + ">>" + mId
        + ">>>");
  }

  public void setTimeZone(int value) {
    if (value >= 0 && value < 13) {
      value = value + MtkTvConfigTypeBase.TIMEZONE_GMT_M_1200;
    } else if (value == 13) {
      value = MtkTvConfigTypeBase.TIMEZONE_AS_BROADCAST;
    } else if (value > 13 && value < 35) {
      value = value - 13;
    } else {
      value = 13;
    }
    int tzOffset = MenuConfigManager.zoneValue[value];
    if (value == MtkTvConfigTypeBase.TIMEZONE_AS_BROADCAST) {
      com.mediatek.wwtv.tvcenter.util.MtkLog
          .v(TAG,
              "*etConfigValue(MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS*");
      TVContent.getInstance(mContext).setConfigValue(MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS, 1);
    } else {
      TVContent.getInstance(mContext).setConfigValue(MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS, 0);
      boolean dsl = TVContent.getInstance(mContext).getConfigValue(
          MtkTvConfigType.CFG_TIME_AUTO_DST) == 1 ? true : false;
      if (CommonIntegration.isSARegion()) {
        tzOffset = tzOffset + 3 * 3600;
        if (tzOffset == 16 * 3600) {
          tzOffset = -16 * 3600;
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "value:" + value + "dsl:" + dsl);
      TVContent.getInstance(mContext).setConfigValue(MtkTvConfigType.CFG_TIME_ZONE, tzOffset, dsl);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("zone", "tzOffset:" + tzOffset);
    TVContent.getInstance(mContext).setConfigValue(MtkTvConfigType.CFG_TIME_ZONE, tzOffset);
  }

  /**
   * get the support value array of the screen mode with the current action
   *
   * @return
   */
  public int[] getSupportScreenModes() {
    MtkTvAVMode navMtkTvAVMode = MtkTvAVMode.getInstance();
    return navMtkTvAVMode.getAllScreenMode();
  }

    public String[] getScreenMode() {
        int[] screenList = getSupportScreenModes();
        String[] mScreenMode = mConfigManager.getSupporScreenMode(screenList);
        if (mScreenMode != null) {
            for (String s : mScreenMode) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "screen mode is :" + s);
            }
        }

        return mScreenMode;
    }

//need to fix
  public List<MtkTvBisskeyInfoBase> getBisskeyInfoList() {
    int bslId = 3;
    List<MtkTvBisskeyInfoBase> list = new ArrayList<MtkTvBisskeyInfoBase>();
    MtkTvBisskeyBase bisskeyBase = new MtkTvBisskeyBase();
    int num = bisskeyBase.getRecordsNumber(bslId);// for general satellite
    Log.d(TAG, "getBisskeyInfoList:num==" + num);
    for (int i = 0; i < num; i++) {
      MtkTvBisskeyInfoBase bisskeyInfo = bisskeyBase.getRecordByIndex(bslId, i);
      if (bisskeyInfo != null) {
        Log.d(TAG, "getBisskeyInfoList:info progID==" + bisskeyInfo.getProgramId());
      }
      list.add(bisskeyInfo);
    }
    return list;
  }

  public List<BissItem> convertToBissItemList() {
    List<BissItem> bissList = new ArrayList<BissItem>();
    List<MtkTvBisskeyInfoBase> list = getBisskeyInfoList();
    for (MtkTvBisskeyInfoBase mtkTvBisskeyInfoBase : list) {
      MtkTvBisskeyInfoBase info = mtkTvBisskeyInfoBase;
      int recId = info.getBslRecId();
      int progId = info.getProgramId();
      int freq = info.getFrequency();
      int symRate = info.getSymRate();
      int pola = info.getPolarization();
      byte[] cwkeyarr = info.getServiceCwKey();
      String threePry = "";
      if (pola <= 1) {
        threePry = "H";
      } else {
        threePry = "V";
      }
      threePry = freq + threePry + symRate;
      String cwKey = byte2HexStr(cwkeyarr);
      Log.d(TAG, "convertToBissItemList:recId,progId,threePry,cwKey,pola:" + recId + "|" + progId
          + "|" + threePry + "|" + cwKey+ "|" + pola);
      BissItem item = new BissItem(recId, progId, threePry, cwKey);
      bissList.add(item);
    }

    return bissList;
  }

  public BissItem getDefaultBissItem() {
    MtkTvBisskeyBase bisskeyBase = new MtkTvBisskeyBase();
    MtkTvBisskeyInfoBase info = bisskeyBase.bisskeyGetDefaultRecord();
    int recId=0;
    int progId=0;
    int freq=0;
    int symRate=0;
    int pola=0;
    byte[] cwkeyarr=null;
    if(info!=null){
     recId = info.getBslRecId();
     progId = info.getProgramId();
     freq = info.getFrequency();
     symRate = info.getSymRate();
     pola = info.getPolarization();
     cwkeyarr = info.getServiceCwKey();
    }
    String threePry = "";
    if (pola <= 1) {
      threePry = "H";
    } else {
      threePry = "V";
    }
    threePry = freq + threePry + symRate;
    if (null != cwkeyarr ) {
	    String cwKey = byte2HexStr(cwkeyarr);
	    Log.d(TAG, "getDefaultBissItem:progId,threePry,cwKey:" + recId + "|" + progId + "|"
	        + threePry + "|" + cwKey);
	    return new BissItem(recId, progId, threePry, cwKey);
    }else {
    	Log.d(TAG,"cwkeyarr == null ");
		return null;
	}

  }

  /**
   * when add bisskey check is existed
   *
   * @param bisskeyBase
   * @param info
   * @return
   */
  private boolean checkBissKeyInfoExist(MtkTvBisskeyBase bisskeyBase, MtkTvBisskeyInfoBase info) {
    List<MtkTvBisskeyInfoBase> list = getBisskeyInfoList();
    if (list != null) {
      for (MtkTvBisskeyInfoBase beComp : list) {
        // if progId is same can check is same each other
        if (info.getProgramId() == beComp.getProgramId()) {
          boolean isSame = bisskeyBase.bisskeyIsSameRecord(info, beComp);
          if (isSame) {
            return true;
          }
        }
      }
    }
    return false;
  }
  /**
   * @param item
   * @param flag
   * @return
   */
  public int operateBissKeyinfo(BissItem item, int flag) {
    int bslId = 3;
    MtkTvBisskeyBase bisskeyBase = new MtkTvBisskeyBase();
    MtkTvBisskeyInfoBase info = null;
    if (flag > 0) {
      info = new MtkTvBisskeyInfoBase(bslId, item.bnum);
    } else {
      info = new MtkTvBisskeyInfoBase();
    }

    int freq = -1;
    int symrate = -1;
    int pola = -1;
    if (item.threePry.contains("H")) {
      String[] spls = item.threePry.split("H");
      freq = Integer.parseInt(spls[0]);
      symrate = Integer.parseInt(spls[1]);
      pola = 1;
    } else {
      String[] spls = item.threePry.split("V");
      freq = Integer.parseInt(spls[0]);
      symrate = Integer.parseInt(spls[1]);
      pola = 2;
    }
    Log.d(TAG, "setup value pola:"+pola+",freq:"+freq+",symrate:"+symrate);
    info.setFrequency(freq);
    info.setSymRate(symrate);
    info.setPolarization(pola);
    info.setProgramId(item.progId);
    hexStr2Bytes(item.cwKey);
    info.setServiceCwKey(hexStr2Bytes(item.cwKey));
    if (flag == 0) {// add
      boolean exist = checkBissKeyInfoExist(bisskeyBase, info);
      if (exist) {
        return -2;
      }
    }
    int ret = bisskeyBase.setBisskeyInfo(bslId, info, flag);
    bisskeyBase.bisskeySetKeyForCurrentChannel();
    return ret;
  }
//need to fix
  private byte uniteBytes(String src0, String src1) {
    byte b0 = Byte.decode("0x" + src0).byteValue();
    b0 = (byte) (b0 << 4);
    byte b1 = Byte.decode("0x" + src1).byteValue();
    return (byte) (b0 | b1);
  }

  public byte[] hexStr2Bytes(String src) {
    int m = 0;
    int n = 0;
    int l = src.length() / 2;
    System.out.println(l);
    byte[] ret = new byte[l];
    for (int i = 0; i < l; i++) {
      m = i * 2 + 1;
      n = m + 1;
      ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
    }
    return ret;
  }

  public String byte2HexStr(byte[] b) {
    String hs = "";
    String stmp = "";
    for (byte by : b) {
      stmp = (Integer.toHexString(by & 0XFF));
      if (stmp.length() == 1){
        hs = hs + "0" + stmp;
      } else{
        hs = hs + stmp;
      }
      // if (n<b.length-1) hs=hs+":";
    }
    return hs.toUpperCase(Locale.ROOT);
  }

  public void resetCallFlashStore() {
    MtkTvConfig.getInstance().setConfigValue(
        MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
  }
  /**
   * simulate data
   *
   * @return
   */
  List<TkgsLocatorItem> tkgsList = new ArrayList<TkgsLocatorItem>();
  public int TKGSVisibleLocSize = 0;
  List<TkgsLocatorItem> tempTKGSLocList() {
    TkgsLocatorItem data1 = new TkgsLocatorItem(1, 8001, "12423V27500", mContext.getString(R.string.tkgs_locator_item));
    TkgsLocatorItem data2 = new TkgsLocatorItem(2, 8002, "11593H27500", mContext.getString(R.string.tkgs_locator_item));
    TkgsLocatorItem data3 = new TkgsLocatorItem(3, 8181, "12559V27500", mContext.getString(R.string.tkgs_locator_item));
    if (!tkgsList.contains(data1)){
      tkgsList.add(data1);
    }

    if (!tkgsList.contains(data2)){
      tkgsList.add(data2);
    }

    if (!tkgsList.contains(data3)){
      tkgsList.add(data3);
    }

    return tkgsList;
  }

  /**
   * get TKGS locator list
   *
   * @return
   */
  public List<TkgsLocatorItem> convertToTKGSLocatorList() {
    tkgsList.clear();
    TKGSVisibleLocSize = 0;
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    int dvbsRet = dvbsScan.dvbsTKGSGetAllVisibleLocators().ordinal();
    if (dvbsRet != 0) {
      return tkgsList;
    }
    TKGSOneLocator[] locArrays = dvbsScan.TKGS_visibleLocatorsList;
    sortLocArrays(locArrays);
    if (locArrays == null) {
      return tkgsList;
    }
    for (TKGSOneLocator tkgsOneLocator : locArrays) {
      TKGSOneLocator oneLoc = tkgsOneLocator;
      int recId = oneLoc.recordID;
      int progId = oneLoc.PID;
      int freq = oneLoc.tpInfo.i4Frequency;
      int symRate = oneLoc.tpInfo.i4Symbolrate;
      int pola = oneLoc.tpInfo.ePol.ordinal();
      String threePry = "";
      if (pola <= 1) {
        threePry = "H";
      } else {
        threePry = "V";
      }
      threePry = freq + threePry + symRate;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertToTKGSItemList:recId,progId,threePry:" + recId + "|" + progId + "|"
          + threePry);
      TkgsLocatorItem item = new TkgsLocatorItem(recId, progId, threePry, mContext.getString(R.string.tkgs_locator_item));
      tkgsList.add(item);
      TKGSVisibleLocSize++;
    }
    List<TkgsLocatorItem> retList = new ArrayList<TkgsLocatorItem>();
    retList.addAll(tkgsList);
    // bissList = tempTKGSLocList();
    return retList;
  }
  private TKGSOneLocator[] sortLocArrays(TKGSOneLocator[] locArrays){
      int minIndex = 0;
      TKGSOneLocator temp;
      if(locArrays!=null&&locArrays.length!=0){
          for(int i=0;i<locArrays.length-1;i++){
              minIndex = i;
              for(int j=i+1;j<locArrays.length;j++){
                  if(locArrays[j].tpInfo.i4Frequency<locArrays[minIndex].tpInfo.i4Frequency){
                      minIndex=j;
                  }
              }
              if(minIndex!=i){
                  temp=locArrays[i];
                  locArrays[i]=locArrays[minIndex];
                  locArrays[minIndex]=temp;
              }
          }
    }
    return locArrays;
  }
  public List<TkgsLocatorItem> getHiddenTKGSLocatorListDev() {
    List<TkgsLocatorItem> hiddList = new ArrayList<TkgsLocatorItem>();
    TkgsLocatorItem data1 = new TkgsLocatorItem(1, 8101, "11413V27411", mContext.getString(R.string.tkgs_locator_item));
    TkgsLocatorItem data2 = new TkgsLocatorItem(2, 8112, "12573H27412", mContext.getString(R.string.tkgs_locator_item));
    TkgsLocatorItem data3 = new TkgsLocatorItem(3, 8091, "13599V27413", mContext.getString(R.string.tkgs_locator_item));
    data1.setEnabled(false);
    data2.setEnabled(false);
    data3.setEnabled(false);
    if (!hiddList.contains(data1)){
      hiddList.add(data1);
    }

    if (!hiddList.contains(data2)){
      hiddList.add(data2);
    }

    if (!hiddList.contains(data3)){
      hiddList.add(data3);
    }

    return hiddList;
  }

  public List<TkgsLocatorItem> getHiddenTKGSLocatorList() {
    List<TkgsLocatorItem> hiddList = new ArrayList<TkgsLocatorItem>();
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    int dvbsRet = dvbsScan.dvbsTKGSGetAllHiddenLocators().ordinal();
    if (dvbsRet != 0) {
      return hiddList;
    }
    TKGSOneLocator[] locArrays = dvbsScan.TKGS_hiddenLocatorsList;
    if (locArrays == null) {
      return hiddList;
    }
    for (TKGSOneLocator loc : locArrays) {
      TKGSOneLocator oneLoc = loc;
      int recId = oneLoc.recordID;
      int progId = oneLoc.PID;
      int freq = oneLoc.tpInfo.i4Frequency;
      int symRate = oneLoc.tpInfo.i4Symbolrate;
      int pola = oneLoc.tpInfo.ePol.ordinal();
      String threePry = "";
      if (pola <= 1) {
        threePry = "H";
      } else {
        threePry = "V";
      }
      threePry = freq + threePry + symRate;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getHiddenTKGSLocatorList:recId,progId,threePry:" + recId + "|" + progId + "|"
          + threePry);
      TkgsLocatorItem item = new TkgsLocatorItem(recId, progId, threePry, mContext.getString(R.string.tkgs_locator_item));
      item.setEnabled(false);
      hiddList.add(item);
    }
    return hiddList;
  }

  public TkgsLocatorItem getDefaultTKGSLocItem() {
    // MtkTvBisskeyBase bisskeyBase = new MtkTvBisskeyBase();
    // MtkTvBisskeyInfoBase info = bisskeyBase.bisskeyGetDefaultRecord();
    int recId = -1;// info.getBslRecId();
    int progId = 8181;// info.getProgramId();
    int freq = 12423;// info.getFrequency();
    int symRate = 27500;// info.getSymRate();
    String threePry = "";
//    if (pola <= 1) {
//      threePry = "H";
//    } else {
      threePry = "V";
   // }
    threePry = freq + threePry + symRate;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDefaultTKGSItem:progId,threePry:" + recId + "|" + progId + "|"
        + threePry);
    return new TkgsLocatorItem(recId, progId, threePry,mContext.getString(R.string.tkgs_locator_item));
  }

  /**
   * when add tkgs one locator check is existed
   *
   * @param bisskeyBase
   * @param info
   * @return
   */
  private boolean checkTKGSLocatorInfoExist(TKGSOneLocator oneLoc) {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    int dvbsRet = dvbsScan.dvbsTKGSGetAllVisibleLocators().ordinal();
    if (dvbsRet == 0) {
      TKGSOneLocator[] locArrays = dvbsScan.TKGS_visibleLocatorsList;
      if (locArrays == null) {
        return false;
      }
      for (TKGSOneLocator tkgsOneLocator : locArrays) {
        if (oneLoc.recordID == tkgsOneLocator.recordID) {
          continue;
        }
        dvbsRet = dvbsScan.dvbsTKGSIsSameVisibleLocator(tkgsOneLocator, oneLoc).ordinal();
        if (dvbsRet == 0) {
          if (dvbsScan.TKGS_isSameLocator) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public int operateTKGSLocatorinfo(TkgsLocatorItem item, int flag) {
    int freq = -1;
    int symrate = -1;
    int pola = -1;
    if (item.threePry.contains("H")) {
      String[] spls = item.threePry.split("H");
      freq = Integer.parseInt(spls[0]);
      symrate = Integer.parseInt(spls[1]);
      pola = 1;
    } else {
      String[] spls = item.threePry.split("V");
      freq = Integer.parseInt(spls[0]);
      symrate = Integer.parseInt(spls[1]);
      pola = 2;
    }
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    TKGSOneLocator oneLoc = dvbsScan.new TKGSOneLocator(item.progId, freq, symrate, pola);
    for (TkgsLocatorItem tkgsLocatorItem : tkgsList) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tkgsList:" + tkgsLocatorItem.getTitle());
    }
    if (flag == 0) {// add
      boolean exist = checkTKGSLocatorInfoExist(oneLoc);
      if (exist) {
        return -2;// exist
      } else if (tkgsList.size() >= 5) {
        return -9;// size outofbounds
      } else {
          return dvbsScan.dvbsTKGSAddOneVisibleLocator(oneLoc).ordinal();
      }
    } else if (flag == 1) {// update
      oneLoc.recordID = item.bnum;// set update recordID
      boolean exist = checkTKGSLocatorInfoExist(oneLoc);
      if (exist) {
        return -2;// exist
      }
      return dvbsScan.dvbsTKGSUpdOneVisibleLocator(oneLoc).ordinal();

    } else if (flag == 2) {// delete
        return dvbsScan.dvbsTKGSDelOneVisibleLocator(item.bnum).ordinal();
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "operateTKGSLocatorinfo:should not be here ");
      return -1;
    }

  }

  public boolean operateTKGSLocatorinfoDev(TkgsLocatorItem item, int flag) {

    boolean ret = false;
    if (flag == 0) {// add
      if (!tkgsList.contains(item)) {
        tkgsList.add(item);
        ret = true;
      }
    } else if (flag == 1) {// update
      for (TkgsLocatorItem eitem : tkgsList) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "operateTKGSLocatorinfoDev num=" + item.bnum);
        if (eitem.bnum == item.bnum) {
          eitem = item;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "operateTKGSLocatorinfoDev match:"+eitem.toString());
          ret = true;
        }
      }
    } else if (flag == 2) {// delete
      ret = tkgsList.remove(item);
    }
    return ret;
  }

  public int getTKGSTableVersion() {
    int ret = -1;
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();

    int dvbsRet = dvbsScan.dvbsGetTableVersion(DvbsTableType.DVBS_TABLE_TYPE_TKGS).ordinal();
    // 0==ok,1==error,2==invalid
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTKGSTableVersion dvbsRet ==" + dvbsRet);
    if (dvbsRet == 0) {
      return dvbsScan.getTable_version;
    }
    return ret;
  }

  public boolean resetTKGSTableVersion(int version) {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();

    int dvbsRet = dvbsScan.dvbsSetTableVersion(DvbsTableType.DVBS_TABLE_TYPE_TKGS, version)
        .ordinal();

    // 0==ok,1==error,2==invalid
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetTKGSTableVersion dvbsRet ==" + dvbsRet);
    return dvbsRet == 0;
  }

  public int cleanAllHiddenLocs() {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    return dvbsScan.dvbsTKGSCleanAllHiddenLocators().ordinal();
  }

  public int disableMonitor() {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    return dvbsScan.dvbsSetDisableAutoUpdateForCrnt().ordinal();
  }

  public int enableMonitor() {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    return dvbsScan.dvbsSetEnableAutoUpdateForCrnt().ordinal();
  }

  int tkgsSvcListSelPos = 0;
  List<TKGSOneSvcList> tkgsSvcList = null;

  public List<TKGSOneSvcList> getTKGSOneSvcList() {
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    int ret = dvbsScan.dvbsGetNfyGetInfo().ordinal();
    if (ret == 0) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTKGSOneSvcList nfyGetInfo_mask:" + dvbsScan.nfyGetInfo_mask);
      if (true || dvbsScan.nfyGetInfo_mask == dvbsScan.SB_DVBS_GET_INFO_MASK_TKGS_SVC_LIST) {
        ret = dvbsScan.dvbsTKGSGetAllSvcList().ordinal();
        if (ret == 0) {
          int num = dvbsScan.TKGS_SvclistNum;
          tkgsSvcListSelPos = dvbsScan.TKGS_PrefSvcListNo;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTKGSOneSvcList num:" + num);
          TKGSOneSvcList[] data = dvbsScan.TKGS_AllSvcLists;
          tkgsSvcList = Arrays.asList(data);
          return tkgsSvcList;
        }
      }
    }
    return null;
  }

  public List<String> getTKGSOneServiceStrList(List<TKGSOneSvcList> svclist) {
    List<String> list = new ArrayList<String>();
    if (svclist != null && !svclist.isEmpty()) {
      for (int i = 0; i < svclist.size(); i++) {
        if (tkgsSvcListSelPos == svclist.get(i).svcListNo) {
          tkgsSvcListSelPos = i;
        }
        list.add(svclist.get(i).svcListName);
      }
    }

    return list;
  }

  public int getTKGSOneServiceSelectValue() {
    return tkgsSvcListSelPos;
  }

  public void setTKGSOneServiceListValue(int pos) {
    int svcListNo = -1;
    if (tkgsSvcList != null && !tkgsSvcList.isEmpty()) {
      svcListNo = tkgsSvcList.get(pos).svcListNo;
    }
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    int ret = dvbsScan.dvbsTKGSSelSvcList(svcListNo).ordinal();
    if (ret == 0) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setTKGSOneServiceListValue set svcListNo to:" + svcListNo);

    }
  }

  String tkgsUserMessage = null;

  public void setTKGSUserMessage(String message) {
    tkgsUserMessage = message;
  }

  public String getTKGSUserMessage() {
    return tkgsUserMessage;
  }

  public void setIsCamScanUI(boolean isCamScanUI) {
    ScanContent.isCamScanUI = isCamScanUI;
  }

  public synchronized static void setMySelfNull() {
      mSelf = null;
}

}
