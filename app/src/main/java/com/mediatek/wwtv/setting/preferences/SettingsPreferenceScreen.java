package com.mediatek.wwtv.setting.preferences;

//import java.lang.reflect.Array;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.util.ArrayList;
//import java.util.HashMap;
//import com.mediatek.wwtv.setting.view.DivxDialog;
import com.mediatek.wwtv.setting.view.MenuMjcDemoDialog;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.mediatek.wwtv.setting.view.BissKeyEditDialog;
import com.mediatek.wwtv.setting.view.BissKeyPreferenceDialog;
import com.mediatek.wwtv.setting.util.RegionConst;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.mediatek.wwtv.setting.TvSettingsActivity;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Toast;
import com.mediatek.wwtv.setting.parental.ContentRatingSystem;
import com.mediatek.wwtv.setting.view.FacSetup;

import android.R.integer;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.media.tv.TvTrackInfo;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.android.tv.onboarding.SetupSourceActivity;
import com.android.tv.menu.customization.CustomizeChanelListActivity;
import com.android.tv.ui.sidepanel.SideFragmentManager;
import com.android.tv.util.LicenseUtils;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr;

import com.mediatek.wwtv.setting.fragments.SatListFrag;
import com.mediatek.wwtv.setting.CNChannelInfoActivity;
import com.mediatek.wwtv.setting.SatActivity;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.TKGSSettingActivity;
import com.mediatek.wwtv.setting.WebActivity;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.setting.base.scan.adapter.BissListAdapter.BissItem;
import com.mediatek.wwtv.setting.ChannelInfoActivity;
import com.mediatek.wwtv.setting.EditTextActivity;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.setting.base.scan.model.DVBCCNScanner;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanThirdlyDialog;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.setting.util.MenuSystemInfo;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.TransItem;
import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.MessageType;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
//import com.mediatek.wwtv.setting.view.FacPreset;
//import com.mediatek.wwtv.setting.view.FacSetup;
//import com.mediatek.wwtv.setting.view.DivxDialog;
//import com.mediatek.wwtv.setting.parental.ContentRatingSystem;
//import com.mediatek.wwtv.setting.view.BissKeyEditDialog;
//import com.mediatek.wwtv.setting.view.BissKeyPreferenceDialog;
//import com.mediatek.wwtv.setting.view.MenuMjcDemoDialog;
//import com.mediatek.wwtv.setting.ChannelInfoActivity;
//import com.mediatek.wwtv.setting.TvSettingsActivity;
import com.mediatek.wwtv.setting.widget.view.DatePicker;
import com.mediatek.wwtv.setting.widget.view.DownloadFirmwareDialog;
import com.mediatek.wwtv.setting.view.NetUpdateGider;
import com.mediatek.wwtv.setting.widget.view.TimePicker;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.M7OneOPT;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPSettingInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPPara;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.model.TvProviderAudioTrackBase;
import com.mediatek.wwtv.setting.widget.view.DiskSettingDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.setting.view.PostalCodeEditDialog;
import com.android.tv.ui.sidepanel.TrackListFragment;
import mediatek.sysprop.VendorProperties;

public class SettingsPreferenceScreen {// NOPMD
    private static final String TAG = "SettingsPreferenceScreen";
    private Context mContext;
    public PreferenceManager mPreferenceManager;
    private final MenuDataHelper mHelper;
    private final TVContent mTV;
    private String ItemName;
    protected MenuDataHelper mDataHelper;
    private Preference channelskip;
    private Preference channelSort;
    private Preference channelMove;
    private Preference channelEdit;
    private Preference channelDelete;
    private Preference cleanList;
    private Preference saChannelEdit;
    public static Preference tvChannelListType;
    private final EditChannel mEditChannel;
    private Preference saChannelFine;
    Preference chanelDecode;
    private Preference updateScan;
    Preference openVchip;
    private LiveTVDialog cleanDialog;
    DownloadFirmwareDialog mDownloadFirmwareDialog;
    private String[] dTLanguage;
    private String[] dPLanguage;
    String[] tPLevel;
    private String[] mScreenMode;
    private ProgressDialog pdialog = null;
    static final int MESSAGE_SEND_RESET = 0x23;
    private static SettingsPreferenceScreen mPreference = null;
    private CommonIntegration mNavIntegration = null;

    public static final String FACTORY_HEAD = "factory__";
    static final int SCREEN_MODE_DOT_BY_DOT = 6;
    final static String tempDate = "def";
    private final MenuConfigManager mConfigManager;
    LiveTVDialog autoAdjustDialog;
    private int autoTimeOut = 0;
    private final MtkTvAppTVBase appTV;
    boolean haveScreenMode = false;
    public static boolean isSetPrefCret = false;
    private Preference pictureModePre;
    private Preference reset_setting;
    SideFragmentManager mSideFragmentManager;
    /**
    * load data for video
    */
    private String[] pictureMode;
    String[] doViRestore;
    String[] doViNoti;
    String[] gamma;
    private String[] colorTemperature2;
    private String[] dnr;
    private String[] videoMpegNr;
    private String[] luma;
    private String[] fleshTone;
    private String[] videoDiFilm;
    private String[] blueStretch;
    private String[] gameMode;
    private String[] pqsplitdemoMode;
    private String[] vgaMode;
    private String[] hdmiMode;
    String[] m3DModeStrArr;
    private String[] mBlackBar;
    private String[] mSuperResolution;
    private String[] blueMute;
    // effect
    private String[] videoEffect;
    // mjc mode
    private String[] videoMjcMode;
    private String[] hdr;
    private Preference videohdr;
    public List<BissItem> bissitems;
    private int bnum=0;
    private Preference frePreference;
    private Preference symPreference;
    private Preference progPreference;
    private Preference cwPreference;
    private Preference ploaPreference;
    private PreferenceScreen bisskeyGroup;
    private List<Preference> bisskeyPreferences;
    public static Preference bissPreference;
    public static Preference tkgsSetting;
    public PreferenceScreen regionSettingScreen;
    private Map<String, Integer> preOrder;
    private static final String KEY_PRE_ORDER_PICMODE = "picmode";
    private static final String KEY_PRE_ORDER_BACK_LIGHT = "back_light";
    private static final String KEY_PRE_ORDER_BRIGHTNESS = "brightness";
    private static final String KEY_PRE_ORDER_CONTRAST = "contrast";
    private static final String KEY_PRE_ORDER_SATURATION = "saturation";
    private static final String KEY_PRE_ORDER_HUE = "hue";
    private static final String KEY_PRE_ORDER_SHARPNESS = "sharpness";
    private static final String KEY_PRE_ORDER_DOVI_RESET = "reset";
    private FacSetup mFacSetup;
    private SettingsPreferenceScreen(Context context, PreferenceManager manager) {
        mContext = context;
        mFacSetup = new FacSetup(context);
        mPreferenceManager = manager;
        mHelper = MenuDataHelper.getInstance(mContext);
        mTV = TVContent.getInstance(mContext);
        mDataHelper = MenuDataHelper.getInstance(mContext);
        mEditChannel = EditChannel.getInstance(mContext);
        mConfigManager = MenuConfigManager.getInstance(mContext);
        mNavIntegration = CommonIntegration.getInstance();
        appTV = new MtkTvAppTVBase();
        bisskeyPreferences=new ArrayList<Preference>();
        mSideFragmentManager = new SideFragmentManager((LiveTvSetting)context, null, null);
    }

    public static synchronized SettingsPreferenceScreen getInstance(
            Context context, PreferenceManager manager) {
        if (null == mPreference) {
            mPreference = new SettingsPreferenceScreen(context, manager);
            isSetPrefCret = true;
            RxBus.instance.onEvent(ActivityDestroyEvent.class)
                .filter(it -> it.activityClass == LiveTvSetting.class)
                .firstElement()
                .doOnSuccess(it -> {
                    synchronized (SettingsPreferenceScreen.class) {
                        mPreference = null;
                        isSetPrefCret = false;
                        tvFirstVoice = null;
                        tvFirstLanguageEU = null;
                        tvSecondLanguageEU = null;
                    }
                })
                .subscribe();
        }

        mPreference.mContext = context;
        mPreference.mPreferenceManager = manager;

        return mPreference;
    }
    public static SettingsPreferenceScreen getInstance() {
        if (null != mPreference) {
            return mPreference;
        } else {
            return null;
        }
    }

    public static void resetInstance(){
        if(TvInputCallbackMgr.mInstance!=null) {
            TvInputCallbackMgr.mInstance.setSubtitleCallback(null);
        }
        tvChannelListType = null;
        mPreference = null;
    }

    public final Handler mSignalHandler = new Handler() {
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           if (msg.what == TvCallbackConst.MSG_CB_SVCTX_NOTIFY) {

               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"msg.what:"+msg.what);
               TvCallbackData backData = (TvCallbackData)msg.obj;
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "backData.param1:"+backData.param1+",param2:"+backData.param2);
               switch (backData.param1) {
                case SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOSS:
                    if (mScreenModeItem != null) {
                        if (isHaveScreenModeItem()) {
                            mainPreferenceScreen.removePreference(mScreenModeItem);
                            mScreenModeItem.setEnabled(false);
                            }
                    }
                       //sendBroadcast
                    /*Intent refreshIntent= new Intent(MenuSystemInfoFrag.REFRESH_RECEIVER);
                    refreshIntent.putExtra(MenuSystemInfoFrag.REFRESH_RECEIVER_BOOLEAN,false);
                    mContext.sendBroadcast(refreshIntent);*/
                    Log.d(TAG, "SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOSS");
                    break;
                case SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOCKED:
                    Log.d(TAG, "SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOCKED");
                    if (mScreenModeItem != null) {
                        if (!isHaveScreenModeItem()) {
                            if (mTV.isHaveScreenMode()) {
                                reloadScreenMode();
                            }else{
                                mainPreferenceScreen.removePreference(mScreenModeItem);
                                mScreenModeItem.setEnabled(false);
                            }
                        }
                    }
                    /*refreshIntent= new Intent(MenuSystemInfoFrag.REFRESH_RECEIVER);
                    refreshIntent.putExtra(MenuSystemInfoFrag.REFRESH_RECEIVER_BOOLEAN,true);
                    mContext.sendBroadcast(refreshIntent);*/
                    break;
                case SettingsUtil.SVCTX_NTFY_CODE_SERVICE_BLOCKED:
                    Log.d(TAG, "SettingsUtil.SVCTX_NTFY_CODE_SERVICE_BLOCKED");
                    if(mScreenModeItem != null){
                        if (isHaveScreenModeItem()) {
                            mainPreferenceScreen.removePreference(mScreenModeItem);
                            mScreenModeItem.setEnabled(false);
                        }
                    }
                    break;
                case SettingsUtil.SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_VIDEO_SVC :
                case SettingsUtil.SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_CLEAR_VIDEO_SVC:
                case SettingsUtil.SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_NO_VIDEO_SVC :
                case SettingsUtil.SVCTX_NTFY_CODE_SCRAMBLED_VIDEO_CLEAR_AUDIO_SVC :
                case SettingsUtil.SVCTX_NTFY_CODE_SCRAMBLED_VIDEO_NO_AUDIO_SVC:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("picasa", "SCRAMBLED param1 =="+backData.param1);
                    if (mScreenModeItem!=null) {
                        if (isHaveScreenModeItem()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d("picasa", "SCRAMBLED --- isCamCardPlugin =="+isCamCardPlugin);
                            if (!isCamCardPlugin) {
                                mainPreferenceScreen.removePreference(mScreenModeItem);
                                mScreenModeItem.setEnabled(false);
                            }
                        }else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d("picasa", "SCRAMBLED ### isCamCardPlugin =="+isCamCardPlugin);
                        }
                    }
                    break;
                case SettingsUtil.SVCTX_NTFY_CODE_AUDIO_ONLY_SVC:
                case SettingsUtil.SVCTX_NTFY_CODE_AUDIO_VIDEO_SVC:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("picasa", "CODE param1 =="+backData.param1);
                    if (!isHaveScreenModeItem()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("picasa", "CODE ### isCamCardPlugin =="+isCamCardPlugin);
                        if (isCamCardPlugin) {
                            mainPreferenceScreen.addPreference(mScreenModeItem);
                            mScreenModeItem.setEnabled(true);
                        }
                    }
                    break;

                case SettingsUtil.SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("picasa", "SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE = "+backData.param1);
                     if (videohdr!=null) {
                        if(mTV.iCurrentInputSourceHasSignal()){
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.VIDEO_HDR .isConfigVisible "+mTV.isConfigVisible(MenuConfigManager.VIDEO_HDR));
                          if(mTV.isConfigVisible(MenuConfigManager.VIDEO_HDR)){
                             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.VIDEO_HDR hide"+ true);
                             if (isHaveHDRItem()) {
                                 mainPreferenceScreen.removePreference(videohdr);
                                 videohdr.setEnabled(false);
                             }
                           }else{
                               if (!isHaveHDRItem()) {
                                 mainPreferenceScreen.addPreference(videohdr);
                                 videohdr.setEnabled(true);
                               }
                           }
                       }else{
                          if (!isHaveHDRItem()) {
                             mainPreferenceScreen.addPreference(videohdr);
                            videohdr.setEnabled(true);
                          }
                       }
                    }

                     break;
                default:
                    break;
            }

        }
       }
    };

    private void reloadScreenMode(){
        mainPreferenceScreen.addPreference(mScreenModeItem);
        mScreenModeItem.setEnabled(true);
        //Fix DTV00843406
//        getScreenMode();
//        ((ListPreference)mScreenModeItem).setEntries(mScreenMode);
//         String[] entryValues=PreferenceUtil.getCharSequence(mScreenMode.length);
//        ((ListPreference)mScreenModeItem).setEntryValues(entryValues);
//        String defValue= String.valueOf(mConfigManager.getScreenMode(mScreenMode,
//                MenuConfigManager.SCREEN_MODE));
//        ((ListPreference)mScreenModeItem).setValue(defValue);
    }
    boolean isCamCardPlugin = false;
    public final Handler mCAMHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TvCallbackData data = (TvCallbackData) msg.obj;
            if(data == null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "msg data null");
                return;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cam msg.what:"+msg.what+",data.param2=="+data.param2);
            if (msg.what == TvCallbackConst.MSG_CB_CI_MSG) {
                switch (data.param2) {
                case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_INSERT:// 0
                    break;
                case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_NAME:// 1
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cam card insert");
                    isCamCardPlugin = true;
                    break;
                case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_REMOVE:// 2
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cam card remove!!");
                    isCamCardPlugin = false;
                    break;
                default:
                    break;
                }
            }
        }
    };
  public   Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageType.MESSAGE_AUTOADJUST:
                case MessageType.MESSAGE_AUTOCOLOR:
                    Preference mobj = (Preference) msg.obj;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MESSAGE_AUTOADJUST fro id:" + mobj.getKey());
                    boolean flag = true;
                    autoTimeOut++;
                    if (mobj.getKey().equals(MenuConfigManager.FV_AUTOPHASE)) {
                        flag = appTV.AutoClockPhasePositionCondSuccess(mNavIntegration
                                .getCurrentFocus());
                    } else {
                        flag = appTV.AutoColorCondSuccess(mNavIntegration.getCurrentFocus());
                    }
                    if (flag || autoTimeOut >= 5) {
                        autoTimeOut = 0;
                        autoAdjustDialog.dismiss();
                        if (mobj.getKey().equals(MenuConfigManager.AUTO_ADJUST)) {

                            PreferenceData.getInstance(mContext.getApplicationContext())
                                    .invalidate(mobj.getKey(), 0);
                        }
                        mHandler.removeMessages(MessageType.MESSAGE_AUTOADJUST);
                        mHandler.removeMessages(MessageType.MESSAGE_AUTOCOLOR);
                    } else {
                        Message message = this.obtainMessage();
                        message.what = MessageType.MESSAGE_AUTOADJUST;
                        message.obj = mobj;
                        sendMessageDelayed(message,
                                MessageType.delayMillis6);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    PreferenceScreen mainPreferenceScreen;
    public PreferenceScreen getMainScreen() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceATV"+mTV.isCurrentSourceATV());
        final Context themedContext = mPreferenceManager.getContext();
        mainPreferenceScreen =
                mPreferenceManager.createPreferenceScreen(themedContext);
        mainPreferenceScreen.setTitle(R.string.menu_advanced_options);
        boolean isDvrPlaybackState = false ;//will forbiden set tv /setup /parental when play dvr .
        if (DvrManager.getInstance() != null&&
            (DvrManager.getInstance().getState())  instanceof StateDvrPlayback &&
                DvrManager.getInstance().getState().isRunning()) {
            isDvrPlaybackState = true;
        }

        // 1. video
        /*
        PreferenceCategory video = new PreferenceCategory(themedContext);
        video.setTitle(R.string.menu_tab_video);
        video.setKey(video.getTitle().toString());
        mainPreferenceScreen.addPreference(video);
        addVideoSettings(mainPreferenceScreen, themedContext);
        */

        PreferenceCategory audio = new PreferenceCategory(themedContext);
        audio.setTitle(R.string.menu_tab_audio);
        audio.setKey(audio.getTitle().toString());
        mainPreferenceScreen.addPreference(audio);
        addAudioSettings(mainPreferenceScreen, themedContext);
        // 3. tv
        /*
        if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GOOGLE_SETTINGS)) {
            PreferenceCategory tv = new PreferenceCategory(themedContext);
            tv.setTitle(R.string.menu_tab_tv);
            tv.setKey(tv.getTitle().toString());
            if (!isDvrPlaybackState) {
                mainPreferenceScreen.addPreference(tv);
                if (CommonIntegration.isEURegion()) {
                    addEUTvSettings(mainPreferenceScreen, themedContext);
                } else if (CommonIntegration.isUSRegion()) {
                    addTvSettings(mainPreferenceScreen, themedContext);
                } else if (CommonIntegration.isSARegion()) {
                    addUS_SATvSettings(mainPreferenceScreen, themedContext);
                } else if (CommonIntegration.isCNRegion()) {
                    addCNTvSettings(mainPreferenceScreen, themedContext);
                }
                PreferenceData.getInstance(mContext.getApplicationContext())
                        .setTVItemsVisibility(mainPreferenceScreen);
            }
        }*/

        // 4. setup
        PreferenceCategory setup = new PreferenceCategory(themedContext);
        setup.setTitle(R.string.menu_tab_setup);
        setup.setKey(setup.getTitle().toString());
        if (!isDvrPlaybackState) {
            mainPreferenceScreen.addPreference(setup);
            addSetupSettings(mainPreferenceScreen, themedContext);
        }

        // 5. parental
        /*
        if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GOOGLE_SETTINGS)) {
            PreferenceCategory parental = new PreferenceCategory(themedContext);
            parental.setTitle(R.string.menu_tab_parent);
            parental.setKey(parental.getTitle().toString());
            if (!isDvrPlaybackState) {
                mainPreferenceScreen.addPreference(parental);
                addParentalSettings(mainPreferenceScreen, themedContext);
            }
        }*/

        return mainPreferenceScreen;
    }
    private boolean isTVSource() {
        String path = CommonIntegration.getInstance().getCurrentFocus();
        boolean isCurrentTvSource = InputSourceManager.getInstance()
                .isCurrentTvSource(path);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentTvSource." + isCurrentTvSource);
        return isCurrentTvSource;
    }

    /**
     *
     * @param tag mark log
     * @param list local language list
     * @return after transform
     */
    private List<String> subtitleAudioLanguageTransform(String tag, String[] list){
        List<String> mSubtitleAudioUnormalList = new ArrayList<String>();
        List<String> mSubtitleAudioUnormalListstr = new ArrayList<String>();
        List<String> mSubAudioList = new ArrayList<>();

        mSubtitleAudioUnormalList = new LinkedList<String>(
                Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_audio_language_unnormal_array)));
        mSubtitleAudioUnormalListstr = new LinkedList<String>(
                Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_audio_language_unnormal_array_value)));


        for (String s : list) {
            String value = s.trim().toLowerCase(Locale.getDefault());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_value:" + value);

            if (TVContent.getInstance(mContext).isEspCountry() && "und".equalsIgnoreCase(value)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "esp--> und");
                value = mContext.getResources().getString(R.string.menu_arrays_other);
            } else if (TVContent.getInstance(mContext).isEspCountry() && "qaa".equalsIgnoreCase(value)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "esp--> qaa");
                value = mContext.getResources().getString(R.string.menu_arrays_Original_Version);
            } else if ("msa".equals(value)) {
                value = mContext.getResources().getString(R.string.menu_arrays_Bahasa_Melayu);
            } else if(mTV.getCurrentTunerMode() == 0 &&
                    MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_HUN) &&
                    "qaa".equalsIgnoreCase(value)){
                value = mContext.getResources().getString(R.string.menu_arrays_hun_Original_Audio);
            } else if(mTV.getCurrentTunerMode() == 0 &&
                    MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_HUN) &&
                    "mul".equalsIgnoreCase(value)){
                value = mContext.getResources().getString(R.string.menu_arrays_hun_Multiple_Languages);
            } else {
                Locale locale = new Locale(value);
                String localname = locale.getDisplayLanguage();
                if (!localname.equals(value)) {
                    value = localname;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_local:" + value);
                } else {
                    int mIndex = -1;
                    for (int mUNSubtitle = 0; mUNSubtitle < mSubtitleAudioUnormalListstr.size(); mUNSubtitle++) {
                        if (value.contains(mSubtitleAudioUnormalListstr.get(mUNSubtitle))) {
                            mIndex = mUNSubtitle;
                            break;
                        }
                    }

                    if (mIndex >= 0) {
                        value = mSubtitleAudioUnormalList.get(mIndex);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_unnormal:" + value);
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_last:" + value);
            mSubAudioList.add(value);
        }
        return mSubAudioList;
    }

    private int getIndexOfMtsMode(String[] modes,String mode){
        for(int i = 0; i<modes.length; i++){
            if(mode.equalsIgnoreCase(modes[i])){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getIndexOfMtsMode="+i);
                return i;
            }
        }
        return 0;
    }

    public PreferenceScreen getChannelMainScreen() {
        boolean isTVSource = isTVSource();
        final Context themedContext = mPreferenceManager.getContext();
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        mainPreferenceScreen =
                mPreferenceManager.createPreferenceScreen(themedContext);
        mainPreferenceScreen.setTitle(R.string.menu_channel);

        boolean isPvrOrTimeShiftRunning = mTV.isCanScan();
        // TV Channel
        if(isTVSource && !mTV.isTvInputBlock()){
            if(MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US) {
                //channel source
                Intent setupIntent = new Intent(themedContext, SetupSourceActivity.class);
                Preference channelSource = null;

                channelSource = util.createPreference(MenuConfigManager.CHANNEL_CHANNEL_SOURCES,
                        R.string.menu_channel_channel_sources ,setupIntent);
                mainPreferenceScreen.addPreference(channelSource);
                channelSource.setEnabled(!isPvrOrTimeShiftRunning);
                channelSource.setSelectable(!isPvrOrTimeShiftRunning);
            } else if(MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
                Preference tvChannel = util.createPreference(MenuConfigManager.TV_EU_CHANNEL,R.string.menu_tv_channels);
                mainPreferenceScreen.addPreference(tvChannel);
                tvChannel.setEnabled(!isPvrOrTimeShiftRunning);
                tvChannel.setSelectable(!isPvrOrTimeShiftRunning);
            }  else{ //sa
                Preference saChannel = util.createPreference(MenuConfigManager.TV_CHANNEL,R.string.menu_tv_channels);
                mainPreferenceScreen.addPreference(saChannel);
                    saChannel.setEnabled(!isPvrOrTimeShiftRunning);
                    saChannel.setSelectable(!isPvrOrTimeShiftRunning);
          }

          if(CommonIntegration.isUSRegion()) {
            //customize channel list
            Intent intent = new Intent(themedContext, CustomizeChanelListActivity.class);
            Preference customizeChannelList = util.createPreference(MenuConfigManager.CHANNEL_CUSTOMIZE_CHANNEL_LIST,
                R.string.menu_channel_customize_channel_list,intent);
            customizeChannelList.setSummary(R.string.menu_channel_customize_channel_list_summary);
            mainPreferenceScreen.addPreference(customizeChannelList);
            if (mNavIntegration.hasActiveChannel(true) && mHelper.hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)) {
                customizeChannelList.setEnabled(true);
            }else{
                customizeChannelList.setEnabled(false);
            }

            /*customizeChannelList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((TvSettingsActivity)mContext).hide();
                    mSideFragmentManager.show(
                            new CustomizeChanelListFragment(),
                            true);

                  return true;
                }
              });*/
            }


            /*String[] tunerModeStr = themedContext.getResources().getStringArray(
                R.array.menu_tv_tuner_mode_array);
            if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS)
                || MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
                tunerModeStrEu = tunerModeStr;
            } else if (ScanContent.getDVBSOperatorList(mContext).size() == 0) {
                tunerModeStrEu = themedContext.getResources().getStringArray(
                        R.array.menu_tv_tuner_mode_array_full_eu_sat_only);
            } else {
                tunerModeStrEu = themedContext.getResources().getStringArray(
                        R.array.menu_tv_tuner_mode_array_full_eu);
            }*/
            CommonIntegration.getInstance().setContext(themedContext);
          tunerModeStrEu = CommonIntegration.getInstanceWithContext(themedContext).getTunerModes();
          String[] tunerModeValues = CommonIntegration.getInstanceWithContext(themedContext).getTunerModeValues();
            int originalTunerModeSize = tunerModeStrEu.length;
            if(DataSeparaterUtil.getInstance().isSupportSeperateDTV() && tunerModeStrEu.length == 4){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isSupportSeperateDTV originalTunerModeSize ="+originalTunerModeSize);
                String[] satelliteTunerMode = new String[2];
                String[] satelliteTunerModeVals = new String[2];
                satelliteTunerMode[0] = tunerModeStrEu[2];
                satelliteTunerMode[1] = tunerModeStrEu[3];
                satelliteTunerModeVals[0] = tunerModeValues[2];
                satelliteTunerModeVals[1] = tunerModeValues[3];
                tunerModeStrEu = satelliteTunerMode;
                tunerModeValues = satelliteTunerModeVals;
            }
            int tunerMode = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);
            //Channel Installation mode
            Preference channelInstallationMode = util.createListPreference(MenuConfigManager.TUNER_MODE,
                    R.string.menu_channel_channel_installation_mode, true, tunerModeStrEu,tunerModeValues,
                    tunerMode+"");
            mainPreferenceScreen.addPreference(channelInstallationMode);
            channelInstallationMode.setEnabled(!isPvrOrTimeShiftRunning);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"Tuner mode size="+tunerModeValues.length);
            if((tunerModeValues.length <= 1) /*|| (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)&&mTV.isCurrentSourceATV())*/
                    || mTV.isChineseTWN() || MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK)
                    || (DataSeparaterUtil.getInstance().isSupportSeperateDTV() && mTV.getCurrentTunerMode() < 2)
                    || (DataSeparaterUtil.getInstance().isSupportSeperateDTV() && originalTunerModeSize < 4)) {
                mainPreferenceScreen.removePreference(channelInstallationMode);
            }

          //Channel update
            if(CommonIntegration.isEURegion() && !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK)){
                /*mainPreferenceScreen.addPreference(util.createPreference(
                        MenuConfigManager.SETUP_CHANNEL_UPDATE, R.string.menu_setup_channel_update));*/
              //Auto Channel Update
                mainPreferenceScreen.addPreference(util.createSwitchPreference(
                        MenuConfigManager.SETUP_AUTO_CHANNEL_UPDATE,
                        R.string.menu_setup_auto_channel_update,
                        mTV.getConfigValue(MenuConfigManager.SETUP_AUTO_CHANNEL_UPDATE)==1 ? true :false));

                //Channel Update Message
                mainPreferenceScreen.addPreference(util.createSwitchPreference(
                        MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG,
                        R.string.menu_setup_channel_update_msg,
                        mTV.getConfigValue(MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG)==1 ? true :false));

           // CHANNEL_LIST_TYPE
                String[] channelListType = themedContext.getResources().getStringArray(
                        R.array.menu_tv_channel_listtype);
                tvChannelListType = util.createListPreference(
                        MenuConfigManager.CHANNEL_LIST_TYPE,
                        R.string.menu_tv_channel_list_type, true, channelListType,
                        util.mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE));

                boolean isCLTypeShow = mTV.isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE);
                String profileName = MtkTvConfig.getInstance().
                        getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
                if ((!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)
                        || !CommonIntegration.getInstance().isCurrentSourceATVforEuPA())
                        && ((isCLTypeShow && !TextUtils.isEmpty(profileName)) || util.mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) > 0)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LIST_TYPE, isCLTypeShow : " +isCLTypeShow);
                    mainPreferenceScreen.addPreference(tvChannelListType);
                }
            //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName " +profileName);
                // has profile name && select cam list type
                if (util.mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) != 0) {
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName " +profileName);
                       if( TextUtils.isEmpty(profileName)){
                           profileName=channelListType[1];
                       }
                    channelListType[1]= profileName;
                    tvChannelListType.setSummary(profileName);
                }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName "+profileName );
                    channelListType[1]= profileName;
                    tvChannelListType.setSummary(channelListType[0]);
                }

                if(CommonIntegration.getInstance().isPvrRunningOrTimeshiftRuning(mContext)){
                  tvChannelListType.setEnabled(false);
                }else {
                  tvChannelListType.setEnabled(true);
                }
            }
        }
        //Parental Controls
        Preference parentalControls = util.createPreference(MenuConfigManager.CHANNEL_PARENTAL_CONTROLS,
            R.string.menu_channel_parental_controls);
        mainPreferenceScreen.addPreference(parentalControls);
        parentalControls.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

          @Override
          public boolean onPreferenceClick(Preference preference) {
              com.mediatek.tv.ui.pindialog.PinDialogFragment dialog =
                      com.mediatek.tv.ui.pindialog.PinDialogFragment.create(
                              com.mediatek.tv.ui.pindialog.PinDialogFragment.PIN_DIALOG_TYPE_ENTER_PIN);
            ((TvSettingsActivity)mContext).hide();
            dialog.show(((TvSettingsActivity)mContext).getFragmentManager(), "PinDialogFragment");
            return true;
          }
        });

        //open source licenses
        Intent intent = new Intent(themedContext, WebActivity.class);
        intent.putExtra(WebActivity.EXTRA_URL, LicenseUtils.LICENSE_FILE);
        Preference openSourceLicenses = util.createPreference(MenuConfigManager.CHANNEL_OPEN_SOURCE_LICENSES,
            R.string.settings_menu_licenses,intent);
        mainPreferenceScreen.addPreference(openSourceLicenses);

        //version
        /*Preference version = util.createPreference(MenuConfigManager.CHANNEL_VERSION,
            R.string.menu_channel_version);
        String versionName = ((DestroyApp)themedContext.getApplicationContext()).getVersionName();
        version.setSummary(versionName);
        version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

          @Override
          public boolean onPreferenceClick(Preference preference) {
            return true;
          }
        });
        mainPreferenceScreen.addPreference(version);*/

        return mainPreferenceScreen;
    }

    public PreferenceScreen getSubScreen(String parentId) {
        final Context themedContext = mPreferenceManager.getContext();
        PreferenceScreen preferenceScreen =
                mPreferenceManager.createPreferenceScreen(themedContext);
        preferenceScreen.setKey(parentId);

        /*
         * if(TextUtils.equals(MenuConfigManager.VISUALLY_IMPAIRED, parentId)) {
         * preferenceScreen.setTitle(R.string.menu_audio_visually_impaired); return
         * addAudioSubViSettings(preferenceScreen, themedContext); }
         */

        //Color Temperature
        if (TextUtils.equals(MenuConfigManager.VIDEO_COLOR_TEMPERATURE, parentId)) {
            preferenceScreen.setTitle(R.string.menu_video_color_temperature);
            return addVideoSubColorTemperatureSettings(preferenceScreen, themedContext);
        }

        //advanced video
        if (TextUtils.equals(MenuConfigManager.VIDEO_ADVANCED_VIDEO, parentId)) {
            preferenceScreen.setTitle(R.string.menu_video_advancedvideo);
            return addVideoSubAdvancedVideoSettings(preferenceScreen, themedContext);
        }

        //advanced video MJC
        if (TextUtils.equals(MenuConfigManager.MJC, parentId)) {
            preferenceScreen.setTitle(R.string.menu_video_mjc);
            return addVideoSubAdvancedVideoSubMJCSettings(preferenceScreen, themedContext);
        }

        if (TextUtils.equals(MenuConfigManager.VGA, parentId)) {
            preferenceScreen.setTitle(R.string.menu_video_vga);
            return addVideoSubVgaViSettings(preferenceScreen, themedContext);
        }

        if (TextUtils.equals(MenuConfigManager.VIDEO_3D, parentId)) {
            preferenceScreen.setTitle(R.string.menu_video_3d);
            return addVideoSub3DSettings(preferenceScreen, themedContext);
        }

        // Time Setup -- Time == Power On Channel
        if (TextUtils.equals(MenuConfigManager.SETUP_POWER_ON_CH, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_power_on_channel);
            return addSetupSubTimePowerOnChannelSettings(preferenceScreen, themedContext);
        }

        // Caption Setup
        if (TextUtils.equals(MenuConfigManager.CAPTION, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_caption_setup);
            return addSetupSubCaptionSetupSettings(preferenceScreen ,themedContext);
        }

        // caption setup --  Digital Caption Style
        if (TextUtils.equals(MenuConfigManager.SETUP_DIGITAL_STYLE, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_digital_style);
            return addSetupSubCaptionSubCaptionStyleSettings(preferenceScreen ,themedContext);
        }

        //NetWork
        if (TextUtils.equals(MenuConfigManager.SETUP_NETWORK,parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_network);
            return addSetupSubNetWorkSubSettings(preferenceScreen ,themedContext);
        }

        // BISS Key Setting
        if (TextUtils.equals(MenuConfigManager.BISS_KEY, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_biss_key);
            return addSetupSubBissKeySettings(preferenceScreen, themedContext);
        }

        // Click To Add Biss Key
        if (TextUtils.equals(MenuConfigManager.BISS_KEY_ITEM_ADD, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_biss_key_add_key);
            return addSetupSubAddBissKeySettings(preferenceScreen,
                    themedContext,0,0);
        }

        // Click To Update OR DELETE Biss Key
        if (parentId!=null&&parentId.contains(MenuConfigManager.BISS_KEY_ITEM_UPDATE)) {
            int index=Integer.valueOf(parentId.substring(MenuConfigManager.BISS_KEY_ITEM_UPDATE.length()));
            String title="Biss Key "+index;
            preferenceScreen.setTitle(title);
            return addSetupSubAddBissKeySettings(preferenceScreen,
                    themedContext,1,index-1);
        }

        //HDMI 2.0 Setting
       if (TextUtils.equals(MenuConfigManager.SETUP_HDMI, parentId)) {
           preferenceScreen.setTitle(R.string.menu_setup_hdmi_2_0_setting);
           return addSetupSubHDMISubSettings(preferenceScreen ,themedContext);
        }

       //Record Setting
       if (TextUtils.equals(MenuConfigManager.SETUP_RECORD_SETTING, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_record_setting);
                return addSetupSubRecordSettings(preferenceScreen ,themedContext);
        }


       //Channel Update
       if(TextUtils.equals(MenuConfigManager.SETUP_CHANNEL_UPDATE, parentId)){
           preferenceScreen.setTitle(R.string.menu_setup_channel_update);
           return addSetupSubChannelUpdate(preferenceScreen, themedContext);
       }

       //Version info
       if(TextUtils.equals(MenuConfigManager.SETUP_VERSION_INFO, parentId)){
           preferenceScreen.setTitle(R.string.menu_setup_version_info);
           return addVersionInfoSubPage(preferenceScreen, themedContext);
       }

       //System information
       if(TextUtils.equals(MenuConfigManager.SYSTEM_INFORMATION, parentId)){
           preferenceScreen.setTitle(R.string.menu_setup_system_info);
           return addSystemInfoSubPage(preferenceScreen, themedContext);
       }

       if (TextUtils.equals(MenuConfigManager.SETUP_REGION_SETTING, parentId)) {
           boolean isEcuador=mTV.isEcuadorCountry();
           if(isEcuador){
               preferenceScreen.setTitle(R.string.menu_setup_region_setting_ecuador);
               return addSetupSubRegionSettingsEcu(preferenceScreen ,themedContext);
           }else{

               preferenceScreen.setTitle(R.string.menu_setup_region_setting_philippines);
               return addSetupSubRegionSettingsPhi(preferenceScreen ,themedContext);
           }

        }
       if (TextUtils.equals(MenuConfigManager.SETUP_REGION_SETTING_LUZON, parentId)
               ||TextUtils.equals(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS, parentId)
               ||TextUtils.equals(MenuConfigManager.SETUP_REGION_SETTING_MINDANAO, parentId)
               ) {

          return addSetupSubRegionSettingsPhiNationwide(preferenceScreen, themedContext,parentId);
       }
       //Ginga Setup
       if (TextUtils.equals(MenuConfigManager.GINGA_SETUP, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_ginga_setup);
            return addSetupSubGingaSettings(preferenceScreen, themedContext);
        }

        /*
         * if(TextUtils.equals(MenuConfigManager.TYPE, parentId)) {
         * preferenceScreen.setTitle(R.string.menu_audio_type); return
         * addVideoSubTypeSettings(preferenceScreen, themedContext); }
         */

        // EUChannel
        if (TextUtils.equals(MenuConfigManager.TV_EU_CHANNEL, parentId)) {
            preferenceScreen.setTitle(R.string.menu_tv_channels);
            /*if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)&&mTV.isCurrentSourceATV()){
                 return loadDataTvCable(preferenceScreen, themedContext);
            }else */if(mTV.isChineseTWN()){
                return loadDataTvAntenna(preferenceScreen, themedContext);
            }else if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK) && mTV.isCurrentSourceDTV()){
               return addCNChannel(preferenceScreen, themedContext);
            } else{
            int tunerMode = CommonIntegration.getInstance().getTunerMode();
            switch (tunerMode) {
                case 0:
                    return loadDataTvAntenna(preferenceScreen, themedContext);
                case 1:
                    return loadDataTvCable(preferenceScreen, themedContext);
                case 2:
                case 3:
                    return loadDataTvPersonalSatllites(preferenceScreen, themedContext);
                default:
                    return loadDataTvAntenna(preferenceScreen, themedContext);
                }
            }
        }
        // SAChannel
        if (TextUtils.equals(MenuConfigManager.TV_CHANNEL, parentId)) {
            preferenceScreen.setTitle(R.string.menu_tv_channel_setup);
            if (CommonIntegration.isSARegion()) {
                return addSAChannel(preferenceScreen, themedContext);
            }else if(CommonIntegration.isCNRegion()){
                return addCNChannel(preferenceScreen, themedContext);
            }
        }
        // EUDVBSRescan
        if (TextUtils.equals(MenuConfigManager.DVBS_SAT_RE_SCAN, parentId)) {
            preferenceScreen.setTitle(R.string.menu_s_sate_rescan);
            if(/*mTV.isDEUCountry() && */!ScanContent.isPreferedSat()
                    && DataSeparaterUtil.getInstance().isSupportSDX()){//sdx condition
                return addDVBSNormalSdxScan(preferenceScreen, themedContext);
            }else {
                return addDVBSRescan(preferenceScreen, themedContext);
            }
        }

        if (TextUtils.equals(MenuConfigManager.DVBS_SAT_NORMAL_SCAN, parentId)) {
            preferenceScreen.setTitle(R.string.menu_s_sate_rescan);
            return addDVBSRescan(preferenceScreen, themedContext);
        }

        //FastScan update Scan
        if (TextUtils.equals(MenuConfigManager.DVBS_SAT_UPDATE_SCAN, parentId)) {
            preferenceScreen.setTitle(R.string.menu_s_sate_update);
            return addDVBSFastUpdateScan(preferenceScreen, themedContext);
        }
        
        
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_MORE, parentId)) {
            preferenceScreen.setTitle(R.string.menu_s_sate_rescan);
            return addDVBSRescanMore(preferenceScreen, themedContext);
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_NEXT, parentId)) {
            preferenceScreen.setTitle(R.string.menu_s_sate_rescan);
            if(mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE) == 0){//universal ,no used
                mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
            }
            ScanContent.setCamSatellitesMask();
            return addDVBSRescanNext(preferenceScreen, themedContext);
        }

        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_SINGLE, parentId)) {
            preferenceScreen.setTitle(R.string.dvbs_antenna_type_single);
            mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
            ScanContent.setCamSatellitesMask();
            return addDVBSRescanNext(preferenceScreen, themedContext);
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_TONEBURST, parentId)) {
            preferenceScreen.setTitle(R.string.dvbs_tone_burst);
            mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_TONEBURST);
            ScanContent.setCamSatellitesMask();
            return addDVBSRescanNext(preferenceScreen, themedContext);
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_DISEQC10, parentId)) {
            preferenceScreen.setTitle(R.string.dvbs_diseqc_10);
            mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC10);
            ScanContent.setCamSatellitesMask();
            return addDVBSRescanNext(preferenceScreen, themedContext);
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_DISEQC11, parentId)) {
            preferenceScreen.setTitle(R.string.dvbs_diseqc_11);
            mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC11 );
            ScanContent.setCamSatellitesMask();
            return addDVBSRescanNext(preferenceScreen, themedContext);
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_DISEQC12, parentId)) {
            preferenceScreen.removeAll();
            mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC12);
            ScanContent.setCamSatellitesMask();
            preferenceScreen.addPreference(getPreferenceCheckOperator(themedContext,
                    MenuConfigManager.DVBS_RESCAN_NEXT, R.string.setup_next));
            PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
            preferenceScreen.setTitle(R.string.dvbs_diseqc_motor_12);
            preferenceScreen.addPreference(util.createClickPreference(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE,
                    R.string.dvbs_diseqc12_movement_longitude, new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(mContext, EditTextActivity.class);
                    intent.putExtra(EditTextActivity.EXTRA_DESC, mContext.getString(R.string.dvbs_diseqc12_movement_longitude));
                    intent.putExtra(EditTextActivity.EXTRA_CANFLOAT, true);
                    intent.putExtra(EditTextActivity.EXTRA_FLAG_SIGNED, true);
                    intent.putExtra(EditTextActivity.EXTRA_LENGTH, 6);
                    intent.putExtra(EditTextActivity.EXTRA_HINT_TEXT, "[-180.0, 180.0]");
                    ((LiveTvSetting)mContext).startActivityForResult(intent, 0x28);
                    return true;
                }
            }));
            preferenceScreen.addPreference(util.createClickPreference(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE,
                    R.string.dvbs_diseqc12_movement_latitude,new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(mContext, EditTextActivity.class);
                    intent.putExtra(EditTextActivity.EXTRA_DESC, mContext.getString(R.string.dvbs_diseqc12_movement_latitude));
                    intent.putExtra(EditTextActivity.EXTRA_CANFLOAT, true);
                    intent.putExtra(EditTextActivity.EXTRA_FLAG_SIGNED, true);
                    intent.putExtra(EditTextActivity.EXTRA_LENGTH, 6);
                    intent.putExtra(EditTextActivity.EXTRA_HINT_TEXT, "[-90.0, 90.0]");
                    ((LiveTvSetting)mContext).startActivityForResult(intent, 0x29);
                    return true;
                }
            }));
            return preferenceScreen;
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_UNICABLE1, parentId)) {
            if(mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE) != MenuConfigManager.DVBS_ACFG_UNICABLE1){
                mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_UNICABLE1);
                ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_UNICABLE1);
            }
            ScanContent.setCamSatellitesMask();
            ScanContent.initCurrUserBandDefaultBandFreq(mContext, true);
            ScanContent.initCamSatelliteUserBandFreq(mContext);
            preferenceScreen.setTitle(R.string.dvbs_diseqc_unicable1);
            return addDVBSUnicable(preferenceScreen, themedContext);
        }
        if (TextUtils.equals(MenuConfigManager.DVBS_RESCAN_UNICABLE2, parentId)) {
            if(mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE) != MenuConfigManager.DVBS_ACFG_UNICABLE2){
                mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_UNICABLE2);
                ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_UNICABLE2);
            }
            ScanContent.setCamSatellitesMask();
            ScanContent.initCurrUserBandDefaultBandFreq(mContext, false);
            ScanContent.initCamSatelliteUserBandFreq(mContext);
            preferenceScreen.setTitle(R.string.dvbs_diseqc_unicable2);
            return addDVBSUnicable(preferenceScreen, themedContext);
        }


      //DVBS scan sdx
        if (TextUtils.equals(MenuConfigManager.DVBS_SAT_SCAN_SDX, parentId)) {
            preferenceScreen.setTitle(R.string.dvbs_scan_type_sdx);
            PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
            preferenceScreen.removeAll();
            Preference downloadPref = util.createPreference(
                    MenuConfigManager.DVBS_SAT_SCAN_DOWNLOAD,
                    R.string.dvbs_scan_download_sdx);
            preferenceScreen.addPreference(downloadPref);
            return preferenceScreen;
        }

        //DVBS_SAT_SCAN_DOWNLOAD
        if (TextUtils.equals(MenuConfigManager.DVBS_SAT_SCAN_DOWNLOAD, parentId)) {
            preferenceScreen.setTitle(R.string.dvbs_scan_download_sdx);
            return addDVBSDownload(preferenceScreen, themedContext);
        }

        // EUCableScan
        if (TextUtils.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBC, parentId)) {
            List<String> operatorListStr = ScanContent.getCableOperationList(mContext);
            List<CableOperator> operatorList = ScanContent
                    .convertStrOperator(mContext, operatorListStr);
            int size = operatorList.size();
            if (size > 0) {
                preferenceScreen.setTitle(R.string.menu_tv_channel_scan);
                return addCableScan(preferenceScreen, themedContext);
            }else {
                preferenceScreen.setTitle(R.string.menu_tv_channel_scan);
            }
        }
        // VISUALLY_IMPAIRED
        if (TextUtils.equals(MenuConfigManager.VISUALLY_IMPAIRED, parentId)) {
            preferenceScreen.setTitle(R.string.menu_audio_visually_impaired);
            return addAudioSubvisuallyimpairedSettings(preferenceScreen, themedContext);
        }

        // visually impaired audio
        if (TextUtils.equals(MenuConfigManager.CFG_MENU_AUDIOINFO, parentId)) {
            preferenceScreen.setTitle(R.string.menu_audio_visually_impaired_audio);
            return addAudioSubvisuallyimpairedAudioSettings(preferenceScreen, themedContext);
        }
       //  SOUND_TRACKS
        if (TextUtils.equals(MenuConfigManager.SOUND_TRACKS, parentId)) {
            preferenceScreen.setTitle(R.string.menu_audio_sound_tracks);
            return addAudioSubSoundTracks(preferenceScreen, themedContext);
        }

        // freview setting
        if(TextUtils.equals(MenuConfigManager.FREEVIEW_SETTING, parentId)){
            preferenceScreen.setTitle(R.string.menu_string_freeview_setting);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(themedContext);
            ComponentName cName = new ComponentName("com.mediatek.wwtv.setupwizard", "com.mediatek.wwtv.setupwizard.FVPAnnounceActivity");
            Intent intent = new Intent();
            intent.setComponent(cName);
            preferenceScreen.addPreference(preferenceUtil.createPreference(MenuConfigManager.FREEVIEW_TERM_CONDITION,
                    R.string.menu_string_term_condition,intent));
            return preferenceScreen;
        }
        // subtitle
        if (TextUtils.equals(MenuConfigManager.SUBTITLE_GROUP, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_subtitle);
            return addSetupSubSubtitleSettings(preferenceScreen, themedContext);
        }

        // Hbbtv settings
        if (TextUtils.equals(MenuConfigManager.SETUP_HBBTV, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_HBBTV_settings);
            return addSetupSubHbbtvSettings(preferenceScreen, themedContext);
        }

        // Teletext
        if (TextUtils.equals(MenuConfigManager.SETUP_TELETEXT, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_teletext);
            return addSetupSubTeletextSettings(preferenceScreen, themedContext);
        }

        // OAD
        if (TextUtils.equals(MenuConfigManager.SETUP_OAD_SETTING, parentId)) {
            preferenceScreen.setTitle(R.string.menu_setup_oad_setting);
            return addSetupSubOADSettings(preferenceScreen, themedContext);
        }

        // parental
        /* program lock
        if (TextUtils.equals(MenuConfigManager.PARENTAL_PROGRAM_BLOCK, parentId)) {
            preferenceScreen.setTitle(R.string.menu_parental_program_block);
            return addParentalSubProgramBlockSettings(preferenceScreen, themedContext);
        }*/

        //tif content ratings
        if (parentId.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS)) {
            String[] strInfo = parentId.split("\\|");
            preferenceScreen.setTitle(strInfo == null
                ? "ContentRatings" : strInfo[strInfo.length - 1]);
            loadContentRatingsSystems(
                        preferenceScreen,
                        themedContext,
                        parentId);

            return preferenceScreen;
        }

        // 01 channel schedule block
        if (TextUtils.equals(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK, parentId)) {
            preferenceScreen.setTitle(R.string.menu_parental_channel_schedule_block);
            return addParentalSecondChannelScheduleBlock(preferenceScreen, themedContext);
        }

        // 02 channel schedule block
        if (parentId.startsWith(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parentId:" + parentId);
            String tid = parentId.substring(
                    MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST.length());
            MtkTvChannelInfoBase channelInfo = mNavIntegration.getChannelById(Integer.valueOf(tid));
            if (channelInfo!=null) {
                String name = "" + ((mDataHelper.getDisplayChNumber(Integer.valueOf(tid)) == null) ? "" :
                    mDataHelper.getDisplayChNumber(Integer.valueOf(tid))) + "        "
                    + ((channelInfo.getServiceName() == null) ? "" : channelInfo.getServiceName());
            preferenceScreen.setTitle(name);
            }
            return addParentalLastSubChannelScheduleBlock(preferenceScreen, themedContext, parentId);
        }

        // factory video sub page
        if (TextUtils.equals(MenuConfigManager.FV_COLORTEMPERATURE, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_video_color_temperature);
            return addFacVidColorTmp(preferenceScreen, themedContext);
        }

        // factory audio sub page
        if (TextUtils.equals(MenuConfigManager.FA_MTS_SYSTEM, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_audio_mts_system);
            return addFacAudMts(preferenceScreen, themedContext);
        }

        if (TextUtils.equals(MenuConfigManager.FA_A2SYSTEM, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_audio_a2_system);
            return addFacAudA2System(preferenceScreen, themedContext);
        }

        if (TextUtils.equals(MenuConfigManager.FA_PALSYSTEM, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_audio_pal_system);
            return addFacAudPalSystem(preferenceScreen, themedContext);
        }

        if (TextUtils.equals(MenuConfigManager.FA_EUSYSTEM, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_audio_eu_system);
            return addFacAudEuSystem(preferenceScreen, themedContext);
        }

        // factory tv sub page
        if (TextUtils.equals(MenuConfigManager.FACTORY_TV_TUNER_DIAGNOSTIC, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_TV_tunerdiag);
            return addFacTVdiagnostic(preferenceScreen, themedContext);
        }

        // factory setup sub page
        if (TextUtils.equals(MenuConfigManager.FACTORY_SETUP_CAPTION, parentId)) {
            preferenceScreen.setTitle(R.string.menu_factory_setup_caption);
            return addFacSetupCaption(preferenceScreen, themedContext);
        }
        //channelBlock
        if (parentId.equals(MenuConfigManager.PARENTAL_CHANNEL_BLOCK)) {
            preferenceScreen.setTitle(R.string.menu_parental_channel_block);
            return addParentalChannelBlock(preferenceScreen, themedContext);
        }

      //Open V-Chip Dim
        if (parentId.startsWith(MenuConfigManager.PARENTAL_OPEN_VCHIP_REGIN)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parentId:" + parentId);
            regin = parentId.substring(
                    MenuConfigManager.PARENTAL_OPEN_VCHIP_REGIN.length());
            int reginNum = Integer.parseInt(regin);
            preferenceScreen.setTitle(mTV.getOpenVchip().getRegionText());
            return addParentalSubOpenVChipDim(preferenceScreen, themedContext, reginNum);
        }
        //Open V-Chip
        if (TextUtils.equals(MenuConfigManager.PARENTAL_OPEN_VCHIP, parentId)) {
            preferenceScreen.setTitle(R.string.menu_open_vchip);
            return addParentalSubOpenVChipSetting(preferenceScreen, themedContext);
        }
        //Open V-Chip Dim
//        if (TextUtils.equals(MenuConfigManager.PARENTAL_OPEN_VCHIP_REGIN, parentId)) {
//            preferenceScreen.setTitle(mTV.getOpenVchip().getRegionText());
//            return addParentalSubOpenVChipDim(preferenceScreen, themedContext);
//        }
        //Open V-Chip Level
        if (parentId.startsWith(MenuConfigManager.PARENTAL_OPEN_VCHIP_DIM)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parentId:" + parentId);
            dim = parentId.substring(
                    MenuConfigManager.PARENTAL_OPEN_VCHIP_DIM.length());
            int dimNum = Integer.parseInt(dim);
            preferenceScreen.setTitle(mTV.getOpenVchip().getDimText());
            return addParentalSubOpenVChipLevel(preferenceScreen, themedContext, dimNum);
        }
        //Open V-Chip Level
//        if (TextUtils.equals(MenuConfigManager.PARENTAL_OPEN_VCHIP_DIM, parentId)) {
//            preferenceScreen.setTitle(mTV.getOpenVchip().getDimText());
//            return addParentalSubOpenVChipLevel(preferenceScreen, themedContext);
//        }
        // Input Block sub page
        //if (TextUtils.equals(MenuConfigManager.PARENTAL_INPUT_BLOCK, parentId)) {
        //    preferenceScreen.setTitle(R.string.menu_parental_input_block);
        //    return addParentalSubInputBlockSettings(preferenceScreen, themedContext);
        //}
        return null;
    }
    String regin;
    String dim;
    public PreferenceScreen getFactoryScreen() {
        final Context themedContext = mPreferenceManager.getContext();

        PreferenceScreen screen =
                mPreferenceManager.createPreferenceScreen(themedContext);
        screen.setTitle(R.string.menu_factory_name);
        screen.setKey(MenuConfigManager.FACTORY_VIDEO);

        // 1. factory video
        PreferenceCategory video = new PreferenceCategory(themedContext);
        video.setTitle(R.string.menu_factory_video);
        video.setKey(FACTORY_HEAD + video.getTitle().toString());
        screen.addPreference(video);
        addFactoryVideoSettings(screen, themedContext);

        // 2. factory audio
        PreferenceCategory audio = new PreferenceCategory(themedContext);
        audio.setTitle(R.string.menu_factory_audio);
        audio.setKey(FACTORY_HEAD + audio.getTitle().toString());
        screen.addPreference(audio);
        addFactoryAudioSettings(screen, themedContext);

        // 3. factory tv
//        PreferenceCategory tv = new PreferenceCategory(themedContext);
//        tv.setTitle(R.string.menu_factory_TV);
//        tv.setKey(FACTORY_HEAD + tv.getTitle().toString());
//        screen.addPreference(tv);
//        if (CommonIntegration.getInstance().isCurrentSourceTv())
//            {addFactoryTVSettings(screen, themedContext);
//            Log.d(TAG, "factory mode :source not TV ,hide TV options ");}

        // 4. factory setup
        PreferenceCategory setup = new PreferenceCategory(themedContext);
        setup.setTitle(R.string.menu_factory_setup);
        setup.setKey(FACTORY_HEAD + setup.getTitle().toString());
        screen.addPreference(setup);
        addFactorySetupSettings(screen, themedContext);

        // 5. preset  sa have no the func
//        if (false){
//             PreferenceCategory preset = new PreferenceCategory(themedContext);
//             preset.setTitle(R.string.menu_factory_preset);
//             preset.setKey(FACTORY_HEAD + preset.getTitle().toString());
//             screen.addPreference(preset);
//             addFactoryPresetSettings(screen, themedContext);
//        }


        return screen;
    }

    private Preference backLightPre;
    private Preference brightnessPre;
    private Preference contrastPre;
    private Preference saturationPre;
    private Preference HUEPre;
    private Preference sharpnessPre;

    public void notifyPreferenceForVideo() {
        PreferenceUtil util = PreferenceUtil.getInstance(mPreferenceManager.getContext());
        if (pictureModePre == null || util == null) {
            return;
        }
        int cur = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
        if (cur == 5 || cur == 6) {
            mainPreferenceScreen.removePreference(pictureModePre);
            pictureMode = mContext.getResources().getStringArray(R.array.picture_effect_array_dovi);
            pictureModePre = util.createListPreference(MenuConfigManager.PICTURE_MODE, R.string.menu_video_picture_mode,
                    true, pictureMode, cur - 5);
            pictureModePre.setOrder(preOrder.get(KEY_PRE_ORDER_PICMODE));
            mainPreferenceScreen.addPreference(pictureModePre);
            // Reset_setting
            reset_setting = util.createClickPreference(MenuConfigManager.RESET_SETTING, R.string.menu_video_restore);
            int resetOrder = preOrder.get(KEY_PRE_ORDER_DOVI_RESET);
            Log.d(TAG, "reset_setting value is " + resetOrder);
            reset_setting.setOrder(resetOrder);
            mainPreferenceScreen.addPreference(reset_setting);
        } else {
            mainPreferenceScreen.removePreference(pictureModePre);
            mainPreferenceScreen.removePreference(reset_setting);
            pictureMode = mContext.getResources().getStringArray(R.array.picture_effect_array);
            pictureModePre = util.createListPreference(MenuConfigManager.PICTURE_MODE, R.string.menu_video_picture_mode,
                    true, pictureMode, util.mConfigManager.getDefault(MenuConfigManager.PICTURE_MODE)
                            - util.mConfigManager.getMin(MenuConfigManager.PICTURE_MODE));
            pictureModePre.setOrder(preOrder.get(KEY_PRE_ORDER_PICMODE));
            mainPreferenceScreen.addPreference(pictureModePre);
        }
        mainPreferenceScreen.removePreference(backLightPre);
        mainPreferenceScreen.removePreference(brightnessPre);
        mainPreferenceScreen.removePreference(contrastPre);
        mainPreferenceScreen.removePreference(saturationPre);
        mainPreferenceScreen.removePreference(HUEPre);
        mainPreferenceScreen.removePreference(sharpnessPre);

        // Back Light
        backLightPre = util.createProgressPreference(MenuConfigManager.BACKLIGHT,
                R.string.menu_video_backlight, true);
        backLightPre.setOrder(preOrder.get(KEY_PRE_ORDER_BACK_LIGHT));
        mainPreferenceScreen.addPreference(backLightPre);

        // Brightness
        brightnessPre = util.createProgressPreference(MenuConfigManager.BRIGHTNESS, R.string.menu_video_brighttness,
                true);
        brightnessPre.setOrder(preOrder.get(KEY_PRE_ORDER_BRIGHTNESS));
        mainPreferenceScreen.addPreference(brightnessPre);

        // Contrast
        contrastPre = util.createProgressPreference(MenuConfigManager.CONTRAST, R.string.menu_video_contrast, true);
        contrastPre.setOrder(preOrder.get(KEY_PRE_ORDER_CONTRAST));
        mainPreferenceScreen.addPreference(contrastPre);

        // Saturation
        saturationPre = util.createProgressPreference(MenuConfigManager.SATURATION, R.string.menu_video_saturation,
                true);

        // HUE
        HUEPre = util.createProgressPreference(MenuConfigManager.HUE, R.string.menu_video_hue, false);

        // Sharpness
        sharpnessPre = util.createProgressPreference(MenuConfigManager.SHARPNESS, R.string.menu_video_sharpness, false);

        saturationPre.setOrder(preOrder.get(KEY_PRE_ORDER_SATURATION));
        HUEPre.setOrder(preOrder.get(KEY_PRE_ORDER_HUE));
        sharpnessPre.setOrder(preOrder.get(KEY_PRE_ORDER_SHARPNESS));

        boolean isSub = mNavIntegration.getCurrentFocus().equalsIgnoreCase("sub");
        boolean isSA = CommonIntegration.isSARegion();
        if (!mTV.isCurrentSourceVGA()) {
            mainPreferenceScreen.addPreference(saturationPre);
            mainPreferenceScreen.addPreference(HUEPre);
            if (!isSub || isSA) {
                mainPreferenceScreen.addPreference(sharpnessPre);
            }
        } else {
            if (!isSA) {
                mainPreferenceScreen.addPreference(saturationPre);
                mainPreferenceScreen.addPreference(HUEPre);
            }
        }
        if (isSub) {
            if (!CommonIntegration.isSARegion()) {
                mainPreferenceScreen.removePreference(sharpnessPre);
            }
        }
        if (mTV.isCurrentSourceVGA()) {
            mainPreferenceScreen.removePreference(sharpnessPre);
        }
    }

    void addVideoSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        /*
        if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GOOGLE_SETTINGS)) {
            preOrder = new HashMap<String, Integer>();
            pictureMode = mContext.getResources().getStringArray(R.array.picture_effect_array);
            doViRestore = mContext.getResources().getStringArray(R.array.dolby_vision_restore);
            doViNoti = mContext.getResources().getStringArray(R.array.dolby_vision_notification);
            gamma = mContext.getResources().getStringArray(R.array.menu_video_gamma_array);
            colorTemperature2 = mContext.getResources().getStringArray(R.array.menu_video_color_temperature2_array);
            dnr = mContext.getResources().getStringArray(R.array.menu_video_dnr_array);
            videoMpegNr = mContext.getResources().getStringArray(R.array.menu_video_mpeg_nr_array);
            if (CommonIntegration.isCNRegion()) {
                videoDiFilm = mContext.getResources().getStringArray(R.array.menu_video_di_film_mode_array_cn);
            } else {
                videoDiFilm = mContext.getResources().getStringArray(R.array.menu_video_di_film_mode_array);
            }
            luma = mContext.getResources().getStringArray(R.array.menu_video_luma_array);
            fleshTone = mContext.getResources().getStringArray(R.array.menu_video_flesh_tone_us_array);
            mBlackBar = mContext.getResources().getStringArray(R.array.menu_video_black_bar_detection_array);
            mSuperResolution = mContext.getResources().getStringArray(R.array.menu_video_super_resolution_array);
            hdmiMode = mContext.getResources().getStringArray(R.array.menu_video_hdmi_mode_array);
            blueStretch = mContext.getResources().getStringArray(R.array.menu_video_blue_stretch_array);
            gameMode = mContext.getResources().getStringArray(R.array.menu_video_game_mode_array);
            blueMute = mContext.getResources().getStringArray(R.array.menu_setup_blue_mute_array);
            pqsplitdemoMode = mContext.getResources().getStringArray(R.array.menu_video_pq_split_mode_array);
            // effect
            videoEffect = mContext.getResources().getStringArray(R.array.menu_video_mjc_effect_array);
            // Mjc Mode
            if (CommonIntegration.isCNRegion()) {
                videoMjcMode = mContext.getResources().getStringArray(R.array.menu_video_mjc_mode);
            } else {
                videoMjcMode = mContext.getResources().getStringArray(R.array.menu_video_mjc_demo_partition_array);
            }
            // HDR
            hdr = mContext.getResources().getStringArray(R.array.menu_video_hdr_array);

            // Picture Mode
            int cur = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
            Log.d(TAG, "addVideoSettings cur: " + cur);
            boolean dovi = false;
            if (cur == 5 || cur == 6) {
                pictureMode = mContext.getResources().getStringArray(R.array.picture_effect_array_dovi);
                pictureModePre = util.createListPreference(MenuConfigManager.PICTURE_MODE, R.string.menu_video_picture_mode,
                        true, pictureMode, cur - 5);
                preferenceScreen.addPreference(pictureModePre);
                preOrder.put(KEY_PRE_ORDER_PICMODE, pictureModePre.getOrder());
                MenuConfigManager.PICTURE_MODE_dOVI = true;
                dovi = true;
            } else {
                pictureModePre = util.createListPreference(MenuConfigManager.PICTURE_MODE, R.string.menu_video_picture_mode,
                        true, pictureMode, util.mConfigManager.getDefault(MenuConfigManager.PICTURE_MODE)
                        - util.mConfigManager.getMin(MenuConfigManager.PICTURE_MODE));
                preferenceScreen.addPreference(pictureModePre);
                preOrder.put(KEY_PRE_ORDER_PICMODE, pictureModePre.getOrder());
                Log.d(TAG, "reset_setting value is " + pictureModePre.getOrder());
                MenuConfigManager.PICTURE_MODE_dOVI = false;
                dovi = false;
            }

            // Reset_setting
            reset_setting = util.createClickPreference(MenuConfigManager.RESET_SETTING, R.string.menu_video_restore);
            preferenceScreen.addPreference(reset_setting);
            preOrder.put(KEY_PRE_ORDER_DOVI_RESET, reset_setting.getOrder());
            Log.d(TAG, "reset_setting value is " + reset_setting.getOrder());

            if(!dovi) {
                preferenceScreen.removePreference(reset_setting);
            }

            // notify_switch
            Preference notify_switch = util.createSwitchPreference(MenuConfigManager.NOTIFY_SWITCH,
                    R.string.menu_video_notification,
                    mTV.getConfigValue(MenuConfigManager.NOTIFY_SWITCH) == 1 ? true : false);
            preferenceScreen.addPreference(notify_switch);

            // Back Light
            backLightPre = util.createProgressPreference(MenuConfigManager.BACKLIGHT,
                    R.string.menu_video_backlight, true);
            preferenceScreen.addPreference(backLightPre);
            preOrder.put(KEY_PRE_ORDER_BACK_LIGHT, backLightPre.getOrder());

            // Brightness
            brightnessPre = util.createProgressPreference(MenuConfigManager.BRIGHTNESS, R.string.menu_video_brighttness,
                    true);
            preferenceScreen.addPreference(brightnessPre);
            preOrder.put(KEY_PRE_ORDER_BRIGHTNESS, brightnessPre.getOrder());

            // Contrast
            contrastPre = util.createProgressPreference(MenuConfigManager.CONTRAST, R.string.menu_video_contrast, true);
            preferenceScreen.addPreference(contrastPre);
            preOrder.put(KEY_PRE_ORDER_CONTRAST, contrastPre.getOrder());

            // Saturation
            saturationPre = util.createProgressPreference(MenuConfigManager.SATURATION, R.string.menu_video_saturation,
                    true);

            // HUE
            HUEPre = util.createProgressPreference(MenuConfigManager.HUE, R.string.menu_video_hue, false);

            // Sharpness
            sharpnessPre = util.createProgressPreference(MenuConfigManager.SHARPNESS, R.string.menu_video_sharpness, false);

            boolean isSub = mNavIntegration.getCurrentFocus().equalsIgnoreCase("sub");
            boolean isSA = CommonIntegration.isSARegion();
            if (!mTV.isCurrentSourceVGA()) {
                preferenceScreen.addPreference(saturationPre);
                preferenceScreen.addPreference(HUEPre);

                if (!isSub || isSA) {
                    preferenceScreen.addPreference(sharpnessPre);
                }
            } else {
                if (!isSA) {
                    preferenceScreen.addPreference(saturationPre);
                    preferenceScreen.addPreference(HUEPre);
                }
            }
            preOrder.put(KEY_PRE_ORDER_SATURATION, saturationPre.getOrder());
            preOrder.put(KEY_PRE_ORDER_HUE, HUEPre.getOrder());
            preOrder.put(KEY_PRE_ORDER_SHARPNESS, sharpnessPre.getOrder());
            if (isSub) {
                preferenceScreen.removePreference(backLightPre);
                if (!CommonIntegration.isSARegion()) {
                    preferenceScreen.removePreference(sharpnessPre);
                }
            }

            if (mTV.isCurrentSourceVGA()) {
                preferenceScreen.removePreference(sharpnessPre);
            }

            // Gamma
            Preference gammaPre = util.createListPreference(MenuConfigManager.GAMMA, R.string.menu_video_gamma, true, gamma,
                    util.mConfigManager.getDefault(MenuConfigManager.GAMMA));

            // Color Temperature
            Preference colorTemperaturePre = util.createPreference(MenuConfigManager.VIDEO_COLOR_TEMPERATURE,
                    R.string.menu_video_color_temperature);

            if (CommonIntegration.isSARegion()) {
                preferenceScreen.addPreference(gammaPre);
                preferenceScreen.addPreference(colorTemperaturePre);
                if (isSub) {
                    colorTemperaturePre.setEnabled(false);
                }
            } else {
                if (!isSub) {
                    preferenceScreen.addPreference(gammaPre);
                    preferenceScreen.addPreference(colorTemperaturePre);
                }
            }

            //advance video
            Preference advancedVideoPre = util.createPreference(MenuConfigManager.VIDEO_ADVANCED_VIDEO,
                    R.string.menu_video_advancedvideo);
            if (CommonIntegration.isUSRegion()) {
                if (!isSub) {
                    preferenceScreen.addPreference(advancedVideoPre);
                }
            } else if (CommonIntegration.isSARegion() || CommonIntegration.isEURegion()) {
                //  preferenceScreen.addPreference(advancedVideoPre); fixed CRID:DTV00794725
                if (!isSub) {
                    preferenceScreen.addPreference(advancedVideoPre);
                }
            } else {//cn
                if (!mTV.isCurrentSourceVGA() && !isSub) {
                    preferenceScreen.addPreference(advancedVideoPre);
                }
            }

            // 3D
            Preference video3d = util.createPreference(MenuConfigManager.VIDEO_3D,
                    R.string.menu_video_3d);
            if (mTV.isConfigVisible(MenuConfigManager.VIDEO_3D)) {
                preferenceScreen.addPreference(video3d);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "this is not 3d panel so needn't 3D item");
                preferenceScreen.removePreference(video3d);
            }
        }*/

        if (CommonIntegration.isCNRegion()) {
            vgaMode = mContext.getResources().getStringArray(R.array.menu_video_vga_mode_array_cn);
        } else {
            vgaMode = mContext.getResources().getStringArray(R.array.menu_video_vga_mode_array);
        }
        //vga Mode
        Preference vgaModePre = util.createListPreference(MenuConfigManager.VGA_MODE,
            R.string.menu_video_vga_mode, true, vgaMode,
            util.mConfigManager.getDefault(MenuConfigManager.VGA_MODE));

        // VGA
        Preference videovga = util.createPreference(MenuConfigManager.VGA, R.string.menu_video_vga);

        preferenceScreen.addPreference(vgaModePre);
        preferenceScreen.addPreference(videovga);

        if (mTV.isCurrentSourceVGA()) {
            if (mTV.iCurrentInputSourceHasSignal()) {
                vgaModePre.setEnabled(true);
                videovga.setEnabled(true);
            } else {
                vgaModePre.setEnabled(false);
                videovga.setEnabled(false);
            }
            vgaModePre.setVisible(true);
            videovga.setVisible(true);
        } else {
            vgaModePre.setVisible(false);
            videovga.setVisible(false);
        }
    }

    private PreferenceScreen addVideoSubColorTemperatureSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        //Color Temperature
        Preference colorTemperaturePre = util.createListPreference(MenuConfigManager.COLOR_TEMPERATURE,
                R.string.menu_video_color_temperature, true, colorTemperature2,
                util.mConfigManager.getDefault(MenuConfigManager.COLOR_TEMPERATURE)
                -util.mConfigManager.getMin(MenuConfigManager.COLOR_TEMPERATURE));
        preferenceScreen.addPreference(colorTemperaturePre);

        //R Gain
        Preference rGainPre = util.createProgressPreference(MenuConfigManager.COLOR_G_R,
                R.string.menu_video_color_g_red, false);
        preferenceScreen.addPreference(rGainPre);

        //G Gain
        Preference gGainPre = util.createProgressPreference(MenuConfigManager.COLOR_G_G,
                R.string.menu_video_color_g_green, false);
        preferenceScreen.addPreference(gGainPre);

        //B Gain
        Preference bGainPre = util.createProgressPreference(MenuConfigManager.COLOR_G_B,
                R.string.menu_video_color_g_blue, false);
        preferenceScreen.addPreference(bGainPre);

        return preferenceScreen;
    }

    private PreferenceScreen addVideoSubAdvancedVideoSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        if (CommonIntegration.getInstance().getCurrentFocus()
            .equalsIgnoreCase("sub")) {
            if (mTV.isCurrentSourceVGA() && CommonIntegration.isUSRegion()) {
                /*//vga Mode
                Preference vgaModePre = util.createListPreference(MenuConfigManager.VGA_MODE,
                    R.string.menu_video_vga_mode, true, vgaMode,
                    util.mConfigManager.getDefault(MenuConfigManager.VGA_MODE));
                preferenceScreen.addPreference(vgaModePre);*/

                //Black Bar
                Preference blackBarPre = util.createListPreference(MenuConfigManager.BLACK_BAR_DETECTION,
                    R.string.menu_video_black_bar, true, mBlackBar,
                    util.mConfigManager.getDefault(MenuConfigManager.BLACK_BAR_DETECTION));
                preferenceScreen.addPreference(blackBarPre);

                return preferenceScreen;
            }

            /*if (mTV.isCurrentSourceVGA() && CommonIntegration.isEURegion()) {
                //vga Mode
                Preference vgaModePre = util.createListPreference(MenuConfigManager.VGA_MODE,
                    R.string.menu_video_vga_mode, true, vgaMode,
                    util.mConfigManager.getDefault(MenuConfigManager.VGA_MODE));
                preferenceScreen.addPreference(vgaModePre);

                return preferenceScreen;
            }*/

            if (!CommonIntegration.isSARegion() && !mTV.isCurrentSourceTv()) {
                return preferenceScreen;
            }
        }

        //DNR
        Preference dnrPre = util.createListPreference(MenuConfigManager.DNR,
                R.string.menu_video_dnr, true, dnr,
                util.mConfigManager.getDefault(MenuConfigManager.DNR)
                -util.mConfigManager.getMin(MenuConfigManager.DNR));

        //Adaptive Luma Control
        Preference lumaPre = util.createListPreference(MenuConfigManager.ADAPTIVE_LUMA_CONTROL,
                R.string.menu_video_luma, true, luma,
                util.mConfigManager.getDefault(MenuConfigManager.ADAPTIVE_LUMA_CONTROL)
                -util.mConfigManager.getMin(MenuConfigManager.ADAPTIVE_LUMA_CONTROL));

        //Adaptive local contrast Control
/*        Preference localPre = util.createListPreference(MenuConfigManager.ADAPTIVE_LOCAL_CONTRAST_CONTROL,
                R.string.menu_video_luma, true, localContrast,
                util.mConfigManager.getDefault(MenuConfigManager.ADAPTIVE_LOCAL_CONTRAST_CONTROL)
                -util.mConfigManager.getMin(MenuConfigManager.ADAPTIVE_LOCAL_CONTRAST_CONTROL));*/

        //Flesh Tone
        Preference fleshTonePre = util.createListPreference(MenuConfigManager.FLESH_TONE,
                R.string.menu_video_flesh_tone, true, fleshTone,
                util.mConfigManager.getDefault(MenuConfigManager.FLESH_TONE)
                -util.mConfigManager.getMin(MenuConfigManager.FLESH_TONE));

        if (CommonIntegration.isSARegion()) {
            if (CommonIntegration.getInstance().getCurrentFocus()
              .equalsIgnoreCase("sub")) {
                dnrPre.setEnabled(false);
                lumaPre.setEnabled(false);
            }
            if (!mTV.isCurrentSourceVGA()) {
                preferenceScreen.addPreference(dnrPre);
                preferenceScreen.addPreference(fleshTonePre);
                preferenceScreen.addPreference(lumaPre);
            }
            //blue mute
            Preference blueMutePre = util.createListPreference(MenuConfigManager.BLUE_MUTE,
                R.string.menu_setup_bluemute, true, blueMute,
                util.mConfigManager.getDefault(MenuConfigManager.BLUE_MUTE)
                -util.mConfigManager.getMin(MenuConfigManager.BLUE_MUTE));
            preferenceScreen.addPreference(blueMutePre);
        }

        if (CommonIntegration.isEURegion() || CommonIntegration.isUSRegion() || CommonIntegration.isCNRegion()) {
            /*if (mTV.isCurrentSourceVGA() && CommonIntegration.isEURegion()) {
                //vga Mode
                Preference vgaModePre = util.createListPreference(MenuConfigManager.VGA_MODE,
                    R.string.menu_video_vga_mode, true, vgaMode,
                    util.mConfigManager.getDefault(MenuConfigManager.VGA_MODE));
                preferenceScreen.addPreference(vgaModePre);

                return preferenceScreen;
            }*/
            preferenceScreen.addPreference(dnrPre);

            //MPEG NR
            Preference mpegNrPre = util.createListPreference(MenuConfigManager.MPEG_NR,
                R.string.menu_video_mpeg_nr, true, videoMpegNr,
                util.mConfigManager.getDefault(MenuConfigManager.MPEG_NR)
                -util.mConfigManager.getMin(MenuConfigManager.MPEG_NR));

            //Dl Film Mode
            Preference diFilmModePre = util.createListPreference(MenuConfigManager.DI_FILM_MODE,
                    R.string.menu_video_di_film_mode, true, videoDiFilm,
                    util.mConfigManager.getDefault(MenuConfigManager.DI_FILM_MODE));

            //Blue Stretch
            Preference blueStretchPre = util.createListPreference(MenuConfigManager.BLUE_STRETCH,
                    R.string.menu_video_blue_stretch, true, blueStretch,
                    mTV.getConfigValue(MenuConfigManager.BLUE_STRETCH));

            //Game Mode
            Preference gameModePre = util.createListPreference(MenuConfigManager.GAME_MODE,
                R.string.menu_video_wme_mode, true, gameMode,
                util.mConfigManager.getDefault(MenuConfigManager.GAME_MODE)
                -util.mConfigManager.getMin(MenuConfigManager.GAME_MODE));

            if (mTV.isConfigEnabled(MenuConfigManager.GAME_MODE)) {
                gameModePre.setEnabled(true);
            } else {
                gameModePre.setEnabled(false);
            }
            preferenceScreen.addPreference(mpegNrPre);
            preferenceScreen.addPreference(lumaPre);
            preferenceScreen.addPreference(fleshTonePre);
            if (!mTV.isCurrentSourceVGA()) {
                preferenceScreen.addPreference(diFilmModePre);
            }
            preferenceScreen.addPreference(blueStretchPre);
            preferenceScreen.addPreference(gameModePre);

            if (CommonIntegration.isCNRegion()) {
                Preference pqSplitModePre = util.createListPreference(MenuConfigManager.PQ_SPLIT_SCREEN_DEMO_MODE,
                    R.string.menu_video_pq_split_mode, true, pqsplitdemoMode,
                    util.mConfigManager.getDefault(MenuConfigManager.PQ_SPLIT_SCREEN_DEMO_MODE)
                    -util.mConfigManager.getMin(MenuConfigManager.PQ_SPLIT_SCREEN_DEMO_MODE));
                preferenceScreen.addPreference(pqSplitModePre);
            }

            //MJC
            Preference mjcPre = util.createPreference(MenuConfigManager.MJC,
                            R.string.menu_video_mjc);
            preferenceScreen.addPreference(mjcPre);

            /*//vga Mode
            Preference vgaModePre = util.createListPreference(MenuConfigManager.VGA_MODE,
                R.string.menu_video_vga_mode, true, vgaMode,
                util.mConfigManager.getDefault(MenuConfigManager.VGA_MODE));
            if (mTV.isCurrentSourceVGA() && CommonIntegration.isUSRegion()) {
                preferenceScreen.addPreference(vgaModePre);
            }*/

            int vgaModeValue =mTV.getConfigValue(MenuConfigManager.VGA_MODE);
            if (0 == vgaModeValue){
                dnrPre.setEnabled(false);
                mpegNrPre.setEnabled(false);
                lumaPre.setEnabled(false);
                fleshTonePre.setEnabled(false);
                blueStretchPre.setEnabled(false);
            } else {
                dnrPre.setEnabled(true);
                mpegNrPre.setEnabled(true);
                lumaPre.setEnabled(true);
                fleshTonePre.setEnabled(true);
                blueStretchPre.setEnabled(true);
            }

            int gameModeValue =mTV.getConfigValue(MenuConfigManager.GAME_MODE);
            if (0 == gameModeValue){
                mjcPre.setEnabled(true);
                if (CommonIntegration.getInstance().isPipOrPopState()) {
                    mjcPre.setEnabled(false);
                }
            } else {
                mjcPre.setEnabled(false);
            }

            if (mTV.isConfigEnabled(MenuConfigManager.DI_FILM_MODE)) {
                diFilmModePre.setEnabled(true);
            } else {
                diFilmModePre.setEnabled(false);
            }

            if (CommonIntegration.isUSRegion()) {
                //Black Bar Detection
                Preference blackBarDetectionPre = util.createListPreference(MenuConfigManager.BLACK_BAR_DETECTION,
                    R.string.menu_video_black_bar, true, mBlackBar,
                    util.mConfigManager.getDefault(MenuConfigManager.BLACK_BAR_DETECTION));
                preferenceScreen.addPreference(blackBarDetectionPre);
            }

            //super Resolution
            Preference superResolutionPre = util.createListPreference(MenuConfigManager.SUPER_RESOLUTION,
                R.string.menu_video_super_resolution, true, mSuperResolution,
                util.mConfigManager.getDefault(MenuConfigManager.SUPER_RESOLUTION));
            preferenceScreen.addPreference(superResolutionPre);

            if (CommonIntegration.isEURegion() && mTV.isCurrentSourceVGA()) {
                Preference graphicPre = util.createListPreference(MenuConfigManager.GRAPHIC,
                    R.string.menu_video_super_resolution, true, mSuperResolution,
                    util.mConfigManager.getDefault(MenuConfigManager.GRAPHIC));
                preferenceScreen.addPreference(graphicPre);
            }

            // HDMI Mode
            Preference hdmiModePre = util.createListPreference(MenuConfigManager.HDMI_MODE,
                R.string.menu_video_hdmi_mode, true, hdmiMode,
                util.mConfigManager.getDefault(MenuConfigManager.HDMI_MODE));

            if (mTV.isCurrentSourceHDMI()) {
                preferenceScreen.addPreference(hdmiModePre);
                if (util.mConfigManager.getDefault(MenuConfigManager.HDMI_MODE) != 1) {
                    dnrPre.setEnabled(true);
                    mpegNrPre.setEnabled(true);
                    lumaPre.setEnabled(true);
                    fleshTonePre.setEnabled(true);
                    blueStretchPre.setEnabled(true);
                } else {
                    dnrPre.setEnabled(false);
                    mpegNrPre.setEnabled(false);
                    lumaPre.setEnabled(false);
                    fleshTonePre.setEnabled(false);
                    blueStretchPre.setEnabled(false);
                }
            }
        }
        Preference bluelight = util.createProgressPreference(MenuConfigManager.BLUE_LIGHT,
                R.string.menu_video_blue_light, true);
            preferenceScreen.addPreference(bluelight);

        videohdr = util.createListPreference(MenuConfigManager.VIDEO_HDR,
                R.string.menu_video_hdr, true, hdr,
                util.mConfigManager.getDefault(MenuConfigManager.VIDEO_HDR));
        if(mTV.iCurrentInputSourceHasSignal()){
               if(mTV.isConfigVisible(MenuConfigManager.VIDEO_HDR)){
                 preferenceScreen.addPreference(videohdr);
               }

        }else{
            preferenceScreen.addPreference(videohdr);
        }
        return preferenceScreen;
    }

    Preference demoPre;
    private PreferenceScreen addVideoSubAdvancedVideoSubMJCSettings(
                PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        int effectDef =mTV.getConfigValue(MenuConfigManager.EFFECT);

        // effect
        Preference videoEffectPre = util.createListPreference(MenuConfigManager.EFFECT,
            R.string.menu_video_mjc_effect, true, videoEffect,
            util.mConfigManager.getDefault(MenuConfigManager.EFFECT));
        preferenceScreen.addPreference(videoEffectPre);

        // Demo Partition
        Preference demoPartitionPre = util.createListPreference(MenuConfigManager.DEMO_PARTITION,
            R.string.menu_video_mjc_demo_partition, true, videoMjcMode,
            util.mConfigManager.getDefault(MenuConfigManager.DEMO_PARTITION));
        demoPartitionPre.setOnPreferenceChangeListener(mvisuallyimpairedChangeListener);
        preferenceScreen.addPreference(demoPartitionPre);

        // Demo
        MenuMjcDemoDialog dialog = new MenuMjcDemoDialog(themedContext);
        demoPre = util.createDialogPreference(
                MenuConfigManager.DEMO,
                R.string.menu_video_mjc_demo, dialog);

        preferenceScreen.addPreference(demoPre);

        if (0 == effectDef){
            demoPartitionPre.setEnabled(false);
            demoPre.setEnabled(false);
        } else {
            demoPartitionPre.setEnabled(true);
            demoPre.setEnabled(true);
        }

        return preferenceScreen;
    }

    private boolean isHaveScreenModeItem(){
        return mainPreferenceScreen.findPreference((CharSequence)MenuConfigManager.SCREEN_MODE)!=null ? true:false;
    }
     private boolean isHaveHDRItem(){
        return mainPreferenceScreen.findPreference((CharSequence)MenuConfigManager.VIDEO_HDR)!=null?true:false;
    }

    /**
     * Power On Channel
     *
     * @return
     */
    private  PreferenceScreen addSetupSubTimePowerOnChannelSettings(
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        //Select Mode
        int def = SaveValue.getInstance(themedContext).readValue(MenuConfigManager.SELECT_MODE);
        String [] entries =mContext.getResources().getStringArray(R.array.menu_setup_power_on_channel_select_mode_array);
        preferenceScreen.addPreference(util.createListPreference(
                MenuConfigManager.SELECT_MODE,
                R.string.menu_setup_power_on_channel_mode, true, entries, def));

        //Show Channels

        boolean enable = CommonIntegration.getInstance().hasActiveChannel();


        Intent intent =new Intent(mContext, ChannelInfoActivity.class);
        String mItem= MenuConfigManager.SETUP_POWER_ONCHANNEL_LIST ;
        String itemID=MenuConfigManager.POWER_ON_VALID_CHANNELS;

        TransItem trans = new TransItem(mItem, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intent.putExtra("TransItem", trans);
        intent.putExtra("ActionID", itemID);
        Preference channel = util.createPreference(
                MenuConfigManager.SETUP_POWER_ONCHANNEL_LIST,
                R.string.menu_setup_power_on_show_channels, intent);
        channel.setEnabled(enable);
        preferenceScreen.addPreference(channel);

        return preferenceScreen;
    }

    public PreferenceScreen getCaptionSetupScreen() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCaptionSetupScreen");
        final Context themedContext = mPreferenceManager.getContext();
        mainPreferenceScreen =
                mPreferenceManager.createPreferenceScreen(themedContext);
        mainPreferenceScreen.setTitle(R.string.menu_setup_caption_setup);
        return addSetupSubCaptionSetupSettings(mainPreferenceScreen, themedContext);
    }

     //  setup -- caption setup
    private PreferenceScreen addSetupSubCaptionSetupSettings (
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        //Setup Enable Caption
        if (CommonIntegration.isSARegion()) {
            String [] enableCaption = mContext.getResources().getStringArray(
                    R.array.menu_setup_enable_caption_array);
            preferenceScreen.addPreference(util.createListPreference(
                    MenuConfigManager.SETUP_ENABLE_CAPTION,
                    R.string.menu_setup_caption_enable,true,
                    enableCaption,mTV.getConfigValue(MenuConfigManager.SETUP_ENABLE_CAPTION)));
        }
        //Analog Closed Caption
        String [] anaEntries= mContext.getResources().getStringArray(R.array.menu_setup_analog_caption_array);
        int anaDef= mTV.getConfigValue(MenuConfigManager.SETUP_ANALOG_CAPTION);
        Preference analog = util.createListPreference(
                MenuConfigManager.SETUP_ANALOG_CAPTION,
                R.string.menu_setup_analog_closed_caption, true, anaEntries,
                anaDef);
        preferenceScreen.addPreference(analog);

        if(DataSeparaterUtil.getInstance().isAtvOnly()|| CommonIntegration.getInstance().isCurrentSourceComposite()){
            return preferenceScreen;
        }

        //Digital Closed Caption
        String [] dgtEntries;
        if (CommonIntegration.isSARegion()) {
            dgtEntries =mContext.getResources().getStringArray(R.array.menu_setup_digital_caption_sa_array);
            dgtEntries = addSuffix(dgtEntries,R.string.menu_arrays_Service_x);
        }else {
            dgtEntries =mContext.getResources().getStringArray(R.array.menu_setup_digital_caption_array);
            dgtEntries = addSuffix(dgtEntries,R.string.menu_arrays_Language_x);
        }
        int dgtDef = mTV.getConfigValue(MenuConfigManager.SETUP_DIGITAL_CAPTION);
        Preference digitalCaption = util.createListPreference(
                MenuConfigManager.SETUP_DIGITAL_CAPTION,
                R.string.menu_setup_digital_caption, true, dgtEntries, dgtDef);
        preferenceScreen.addPreference(digitalCaption);
        analog.setEnabled(true);
        if (CommonIntegration.isUSRegion()) {
            digitalCaption.setEnabled(true);
        } else if (CommonIntegration.isSARegion()) {
            digitalCaption.setEnabled(false);
        }
        MtkTvChannelInfoBase channelInfoBase = null;
        if(CommonIntegration.getInstance().isCurrentSourceTv() || CommonIntegration.getInstance().isCurrentSourceComposite()){
            channelInfoBase = CommonIntegration.getInstance()
                    .getCurChInfo();
            if (channelInfoBase != null) {
                if (channelInfoBase instanceof MtkTvAnalogChannelInfo) {
                    analog.setEnabled(true);
                    //digitalCaption.setEnabled(false);fix cr00660873/75
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "digitalCaption.setEnable(true)");
                    if (CommonIntegration.isUSRegion()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "analog.setEnable(true)");
                        analog.setEnabled(true);
                    } else if (CommonIntegration.isSARegion()) {
                        analog.setEnabled(false);
                    }
                    digitalCaption.setEnabled(true);
                }
            }
            if (CommonIntegration.getInstance().getChannelAllNumByAPI() <= 0) {
                analog.setEnabled(false);
            }
        }else {
            analog.setEnabled(true);
            digitalCaption.setEnabled(false);
        }

        //Digital Caption Style
        String [] superCaption = mContext.getResources().getStringArray(
                R.array.menu_setup_superimpose_setup_array);
        if (CommonIntegration.isUSRegion()) {
            Preference digitalStyle = util.createPreference(
                    MenuConfigManager.SETUP_DIGITAL_STYLE,
                    R.string.menu_setup_digital_style);
            preferenceScreen.addPreference(digitalStyle);
        }else if(CommonIntegration.isSARegion()){
            superCaption = addSuffix(superCaption,R.string.menu_arrays_Language_x);
            Preference superimpose = util.createListPreference(
                    MenuConfigManager.SETUP_SUPERIMPOSE_SETUP,
                    R.string.menu_setup_superimpose_setup,
                    true, superCaption, mTV.getConfigValue(
                            MenuConfigManager.SETUP_SUPERIMPOSE_SETUP));
            preferenceScreen.addPreference(superimpose);
            superimpose.setEnabled(false);
            if(CommonIntegration.getInstance().isCurrentSourceTv()){
                if (channelInfoBase != null) {
                    if (channelInfoBase instanceof MtkTvAnalogChannelInfo) {
                        superimpose.setEnabled(false);
                    } else {
                        superimpose.setEnabled(true);
                    }
                }
            }
        }


        return preferenceScreen ;

    }

    //setup --caption setup -- digital caption style
    private PreferenceScreen addSetupSubCaptionSubCaptionStyleSettings (
           PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        //Caption Style
        String [] csEntries =mContext.getResources().getStringArray(R.array.menu_setup_caption_style_array);
        int csDef =mTV.getConfigValue(MenuConfigManager.SETUP_CAPTION_STYLE) ;
        preferenceScreen.addPreference(util.createListPreference(
                MenuConfigManager.SETUP_CAPTION_STYLE,
                R.string.menu_setup_caption_style, true, csEntries, csDef));

        //Font Size
        String [] ftEntries =mContext.getResources().getStringArray(R.array.menu_setup_font_size_array);
        int ftDef =mTV.getConfigValue(MenuConfigManager.SETUP_FONT_SIZE);
        Preference ftPreference = util.createListPreference(
                MenuConfigManager.SETUP_FONT_SIZE, R.string.menu_setup_font_size,
                true, ftEntries, ftDef);
        preferenceScreen.addPreference(ftPreference);

        //Font Style
        String [] fsEntries =mContext.getResources().getStringArray(R.array.menu_setup_font_style_array);
        fsEntries = addSuffix(fsEntries, R.string.menu_arrays_Style_x);
        int fsDef =mTV.getConfigValue(MenuConfigManager.SETUP_FONT_STYLE);
        Preference fsPreference = util.createListPreference(
                MenuConfigManager.SETUP_FONT_STYLE, R.string.menu_setup_font_style,
                true, fsEntries, fsDef);
        preferenceScreen.addPreference(fsPreference);

        //Font Color
        String [] fcEntries =mContext.getResources().getStringArray(R.array.menu_setup_font_color_array);
        int fcDef =mTV.getConfigValue(MenuConfigManager.SETUP_FONT_COLOR);
        Preference fcPreference = util.createListPreference(
                MenuConfigManager.SETUP_FONT_COLOR, R.string.menu_setup_font_color,
                true, fcEntries, fcDef);
        preferenceScreen.addPreference(fcPreference);

        //Font Opacity
        String [] foEntries =mContext.getResources().getStringArray(R.array.menu_setup_background_opacity_array);
        int foDef =mTV.getConfigValue(MenuConfigManager.SETUP_FONT_OPACITY);
        Preference foPreference = util.createListPreference(
                MenuConfigManager.SETUP_FONT_OPACITY, R.string.menu_setup_font_opacity,
                true, foEntries, foDef);
        preferenceScreen.addPreference(foPreference);

        //Background Color
        String [] bcEntries =mContext.getResources().getStringArray(R.array.menu_setup_window_color_array);
        int bcDef =mTV.getConfigValue(MenuConfigManager.SETUP_BACKGROUND_COLOR);
        Preference bcPreference = util.createListPreference(
                MenuConfigManager.SETUP_BACKGROUND_COLOR, R.string.menu_setup_background_color,
                true, bcEntries, bcDef);
        preferenceScreen.addPreference(bcPreference);

        //Background Opacity
        String [] boEntries =mContext.getResources().getStringArray(R.array.menu_setup_window_opacity_array);
        int boDef =mTV.getConfigValue(MenuConfigManager.SETUP_BACKGROUND_OPACITY);
        Preference boPreference = util.createListPreference(
                MenuConfigManager.SETUP_BACKGROUND_OPACITY, R.string.menu_setup_background_opacity,
                true, boEntries, boDef);
        preferenceScreen.addPreference(boPreference);

        //Window Color
        String [] wcEntries =mContext.getResources().getStringArray(R.array.menu_setup_window_color_array);
        int wcDef =mTV.getConfigValue(MenuConfigManager.SETUP_WINDOW_COLOR);
        Preference wcPreference = util.createListPreference(
                MenuConfigManager.SETUP_WINDOW_COLOR, R.string.menu_setup_window_color,
                true, wcEntries, wcDef);
        preferenceScreen.addPreference(wcPreference);

        //Window Opacity
        String [] woEntries =mContext.getResources().getStringArray(R.array.menu_setup_window_opacity_array);
        int woDef =mTV.getConfigValue(MenuConfigManager.SETUP_WINDOW_OPACITY);
        Preference woPreference = util.createListPreference(
                MenuConfigManager.SETUP_WINDOW_OPACITY, R.string.menu_setup_window_opacity,
                true, woEntries, woDef);
        preferenceScreen.addPreference(woPreference);

        if (0 == csDef){
            ftPreference.setEnabled(false);
            fsPreference.setEnabled(false);
            fcPreference.setEnabled(false);
            foPreference.setEnabled(false);
            bcPreference.setEnabled(false);
            boPreference.setEnabled(false);
            wcPreference.setEnabled(false);
            woPreference.setEnabled(false);
        } else {
            ftPreference.setEnabled(true);
            fsPreference.setEnabled(true);
            fcPreference.setEnabled(true);
            foPreference.setEnabled(true);
            bcPreference.setEnabled(true);
            boPreference.setEnabled(true);
            wcPreference.setEnabled(true);
            woPreference.setEnabled(true);
        }

        return preferenceScreen ;

    }

    //Net Work

    private PreferenceScreen addSetupSubNetWorkSubSettings (
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        //NetWork Upgrade

        OnPreferenceClickListener nuClick =new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                networkUpdate();

                return false;
            }
        };
        preferenceScreen.addPreference(util.createClickPreference(
                MenuConfigManager.SETUP_UPGRADENET,
                R.string.menu_setup_auto_network_upgrade, nuClick));

        return preferenceScreen;
    }

    // BISS KEY Setting
    private PreferenceScreen addSetupSubBissKeySettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceScreen.addPreference(util.createPreference(
                MenuConfigManager.BISS_KEY_ITEM_ADD,
                R.string.menu_setup_biss_key_add_item));
        bissitems= mHelper.convertToBissItemList();
        if(bissitems!=null){
            int size=bissitems.size();
            for(int i=0;i<size;i++){
                Preference bisskeyPreference=util.createPreference(
                        MenuConfigManager.BISS_KEY_ITEM_UPDATE+(i+1),
                        bissitems.get(i).progId+" "+bissitems.get(i).threePry+" "+bissitems.get(i).cwKey);
                preferenceScreen.addPreference(bisskeyPreference);
                bisskeyPreferences.add(bisskeyPreference);
            }
        }
        bisskeyGroup=preferenceScreen;
        return preferenceScreen;
    }

    // add BISS KEY Setting
    /**
     * @param preferenceScreen
     * @param themedContext
     * @param flag  <br>0 represent save,1 represent update.
     * @param index
     * @return
     */
    private PreferenceScreen addSetupSubAddBissKeySettings(
            PreferenceScreen preferenceScreen, Context themedContext,int flag,int index) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        int freq =-1;
        int rate=-1;
        int pola=-1;
        int progId = -1;
        String cwKeystr="0000000000000000";
        BissItem defItem=null;
        if(flag==0){
            defItem = mHelper.getDefaultBissItem();
         }else{
             defItem=bissitems.get(index);
         }
        if (defItem != null) {
            int findPola =defItem.threePry.indexOf('H');
            if(findPola == -1){
                findPola =defItem.threePry.indexOf('V');
                pola = 1;
            }else{
                pola = 0;
            }
            freq = Integer.parseInt(defItem.threePry.substring(0, findPola));
            rate = Integer.parseInt(defItem.threePry.substring(findPola+1));
            progId = defItem.progId;
            cwKeystr = defItem.cwKey;
        }
        Log.d(TAG, "get pola:"+pola+"");

     // Freqency
        String freqencyString=themedContext.getResources().getString(R.string.menu_setup_biss_key_freqency);
        BissKeyEditDialog fredialog=new BissKeyEditDialog(themedContext,freqencyString,MenuConfigManager.BISS_KEY_FREQ);
        fredialog.setLength(9);
        frePreference=  util.createDialogPreference(
                MenuConfigManager.BISS_KEY_FREQ,
                R.string.menu_setup_biss_key_freqency, fredialog);
        frePreference.setSummary(String.valueOf(freq));
        fredialog.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
        fredialog.setPreference(frePreference);
        preferenceScreen.addPreference(frePreference);

        // Symbol Rate(Ksys/s) fragment
        String symString=themedContext.getResources().getString(R.string.menu_c_rfscan_symbol_rate);
        BissKeyEditDialog symdialog=new BissKeyEditDialog(themedContext,symString,MenuConfigManager.BISS_KEY_SYMBOL_RATE);
        symdialog.setLength(5);
        symdialog.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
        symPreference=util.createDialogPreference(
                MenuConfigManager.BISS_KEY_SYMBOL_RATE,
                R.string.menu_c_rfscan_symbol_rate, symdialog);
        symPreference.setSummary(String.valueOf(rate));
        symdialog.setPreference(symPreference);
        preferenceScreen.addPreference(symPreference);

        // Polazation list
        String[] entries = themedContext.getResources().getStringArray(
                R.array.menu_setup_ploazation_array);
        String[] entriesValue = themedContext.getResources().getStringArray(
                R.array.menu_setup_ploazation_array_value);
        Log.d(TAG, "click:"+pola);
        BissKeyPreferenceDialog poladialog=new BissKeyPreferenceDialog(themedContext,pola);
        ploaPreference=util.createDialogPreference(
                MenuConfigManager.BISS_KEY_SVC_ID,
                R.string.menu_setup_biss_key_polazation, poladialog);
        if(pola==0){
            ploaPreference.setSummary(themedContext.getResources().getString(R.string.menu_setup_biss_key_horizonal));
        }else{
            ploaPreference.setSummary(themedContext.getResources().getString(R.string.menu_setup_biss_key_vertical));
        }
        poladialog.setPreference(ploaPreference);
        poladialog.setDefaultValue(pola);
        preferenceScreen.addPreference(ploaPreference);


        // Prog ID fragment
        String progString=themedContext.getResources().getString(R.string.menu_setup_biss_key_prog_id);
        BissKeyEditDialog progdialog=new BissKeyEditDialog(themedContext,progString,MenuConfigManager.BISS_KEY_SVC_ID);
        progdialog.setLength(5);
        progPreference=util.createDialogPreference(
                MenuConfigManager.BISS_KEY_SVC_ID,
                R.string.menu_setup_biss_key_prog_id, progdialog);
        progPreference.setSummary(String.valueOf(progId));
        progdialog.setPreference(progPreference);
        progdialog.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
        preferenceScreen.addPreference(progPreference);

        // CW Key fragment
        String cwString=themedContext.getResources().getString(R.string.menu_setup_biss_key_cw_key);
        BissKeyEditDialog cwdialog=new BissKeyEditDialog(themedContext,cwString,MenuConfigManager.BISS_KEY_CW_KEY);
        cwdialog.setLength(16);
        cwPreference=util.createDialogPreference(
                MenuConfigManager.BISS_KEY_CW_KEY,
                R.string.menu_setup_biss_key_cw_key, cwdialog);
        cwPreference.setSummary(String.valueOf(cwKeystr));
        cwdialog.setPreference(cwPreference);
        preferenceScreen.addPreference(cwPreference);
        if(flag==0){
            Preference savepreference = new Preference(themedContext);
            savepreference.setKey(MenuConfigManager.BISS_KEY_ITEM_SAVE);
            savepreference.setTitle(R.string.bisskey_savekey);
            savepreference.setOnPreferenceClickListener(mClickListener);
            preferenceScreen.addPreference(savepreference);
        }else{
            bnum=defItem.bnum;
            Preference updatepreference = new Preference(themedContext);
            updatepreference.setKey(MenuConfigManager.BISS_KEY_ITEM_UPDATE);
            updatepreference.setTitle(R.string.bisskey_updatekey);
            updatepreference.setOnPreferenceClickListener(mClickListener);
            preferenceScreen.addPreference(updatepreference);

            Preference deletepreference = new Preference(themedContext);
            deletepreference.setKey(MenuConfigManager.BISS_KEY_ITEM_DELETE);
            deletepreference.setTitle(R.string.bisskey_deletekey);
            deletepreference.setOnPreferenceClickListener(mClickListener);
            preferenceScreen.addPreference(deletepreference);

        }
        return preferenceScreen;
    }
    // HDMI 2.0 Setting
    private PreferenceScreen addSetupSubHDMISubSettings(
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        // Signal Format.
        String [] entries = mContext.getResources().getStringArray(R.array.menu_setup_signalformat_array);
        int defValue =mTV.getConfigValue(MenuConfigManager.SETUP_SIGNAL_FORMAT);
        preferenceScreen
                .addPreference(util.createListPreference(
                        MenuConfigManager.SETUP_SIGNAL_FORMAT,
                        R.string.menu_setup_hdmi_signalformat, true, entries,
                        defValue));
        return preferenceScreen ;
    }

    Preference tkgsResetTabver;
    //TKGS Setting
    PreferenceScreen addSetupSubTKGSSettings(
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        String[] operModes = new String[] {
                "Automatic", "Customisable", "TKGS Off"
            };
        //opertateMode
        Preference opertateMode = util.createListPreference(
                MenuConfigManager.TKGS_OPER_MODE, R.string.menu_setup_TKGS_setting,
                true, operModes,
                mConfigManager.getDefault(MenuConfigManager.TKGS_OPER_MODE));
        preferenceScreen.addPreference(opertateMode);
        //TKGS locat list
        Preference locatorlist = util.createPreference(
                MenuConfigManager.TKGS_LOC_LIST,
                R.string.menu_setup_TKGS_locate_list);
        preferenceScreen.addPreference(locatorlist);
        // hidden locations
        Preference hiddenLocations = util.createPreference(
                MenuConfigManager.TKGS_HIDD_LOCS,
                R.string.menu_setup_TKGS_hidden_locations);

        // reset table version
        tkgsResetTabver = util.createDialogPreference(
                MenuConfigManager.TKGS_RESET_TAB_VERSION,
                R.string.TKGS_reset_table_version,
                cleanDialog);
        int tversion = mHelper.getTKGSTableVersion();
        if (tversion == 0XFF) {// invalid version
          tkgsResetTabver.setSummary("None");
        } else {
          tkgsResetTabver.setSummary("" + tversion);
        }
        String cID = MenuConfigManager.TKGS_FAC_SETUP_AVAIL_CONDITION;
        boolean isCondNormal =  mConfigManager.getDefault(cID) == 0;

        if (isCondNormal) {
            preferenceScreen.addPreference(tkgsResetTabver);
          // tkgsResetTabver.setEnabled(false);
        } else {
            preferenceScreen.addPreference(hiddenLocations);
            preferenceScreen.addPreference(tkgsResetTabver);
        }
//        List<String> batStrList = mHelper.getTKGSOneServiceStrList(mHelper.getTKGSOneSvcList());
//        int defVal = 0;
//                mHelper.getTKGSOneServiceSelectValue();
//        String[] preferlistArray = batStrList.toArray(new String[batStrList.size()]);
//        if (preferlistArray != null && preferlistArray.length > 0) {
//            Preference preferredList = util.createListPreference(
//                    MenuConfigManager.TKGS_PREFER_LIST,
//                    R.string.TKGS_preferred_list, true,
//                    preferlistArray, defVal);
//            preferenceScreen.addPreference(preferredList);
//        }
        return preferenceScreen;
    }


    //Record Setting

    private PreferenceScreen addSetupSubRecordSettings(
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // Device Info
        DiskSettingDialog dialog = new DiskSettingDialog(themedContext,
                R.layout.pvr_timeshfit_deviceinfo);
        preferenceScreen.addPreference(util.createDialogPreference(
                MenuConfigManager.SETUP_DEVICE_INFO,
                R.string.menu_setup_device_info, dialog));

        // Schedule List
        ScheduleListDialog scheduleListDialog = new ScheduleListDialog(themedContext, 0);
        scheduleListDialog.setEpgFlag(false);
        preferenceScreen.addPreference(util.createDialogPreference(
                MenuConfigManager.SETUP_SCHEDUCE_LIST,
                R.string.menu_setup_schedule_list, scheduleListDialog));

        //Time Shift Mode
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_TIMESHIFT)) {
            preferenceScreen.addPreference(util.createSwitchPreference(
                    MenuConfigManager.SETUP_SHIFTING_MODE,
                    R.string.menu_setup_time_shifting_mode,
                    mTV.getConfigValue(MenuConfigManager.SETUP_SHIFTING_MODE)==1 ? true :false));
        }


        return preferenceScreen ;
    }

  //Channel update
    private PreferenceScreen addSetupSubChannelUpdate(
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        //Auto Channel Update
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.SETUP_AUTO_CHANNEL_UPDATE,
                R.string.menu_setup_auto_channel_update,
                mTV.getConfigValue(MenuConfigManager.SETUP_AUTO_CHANNEL_UPDATE)==1 ? true :false));

        //Channel Update Message
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG,
                R.string.menu_setup_channel_update_msg,
                mTV.getConfigValue(MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG)==1 ? true :false));

        return preferenceScreen ;
    }

    //Version info
    private PreferenceScreen addVersionInfoSubPage(PreferenceScreen preferenceScreen,
            Context themedContext) {
        TVContent tvContent = TVContent.getInstance(themedContext);
        String modelName ="";
        modelName = tvContent.getSysVersion(3, modelName);
        int index=modelName.lastIndexOf("_")+1;
        String modeNameshow = modelName.substring(index);
        String version = VendorProperties.customer_software_version().orElse("");
		if(version == null || version.equals("")){
            version = tvContent.getSysVersion(0, version);
        }
        String serialNum ="";
        serialNum = tvContent.getSysVersion(2, serialNum);
        PreferenceUtil util = PreferenceUtil.getInstance(mPreferenceManager.getContext());
        preferenceScreen.addPreference(util.createPreferenceWithSummary("modelName",R.string.menu_versioninfo_name, modeNameshow));
        preferenceScreen.addPreference(util.createPreferenceWithSummary("version",R.string.menu_versioninfo_version, version));
        //2019-10-30 remove serialNum for CR DTV01968092
        //preferenceScreen.addPreference(util.createPreferenceWithSummary("serialNum",R.string.menu_versioninfo_number, serialNum));
        return preferenceScreen;
    }

    //System information
    public MenuSystemInfo mMenuSystemInfo;
    private PreferenceScreen addSystemInfoSubPage(PreferenceScreen preferenceScreen,
            Context themedContext) {
        if(mMenuSystemInfo == null){
            mMenuSystemInfo = new MenuSystemInfo(themedContext);
        }
        return mMenuSystemInfo.getPreferenceScreen(preferenceScreen);
    }

    private PreferenceScreen addSetupSubRegionSettingsEcu(
            PreferenceScreen preferenceScreen, Context themedContext){
        boolean isEcuador=mTV.isEcuadorCountry();
        int proResArray=R.array.menu_setup_region_setting_ecuador_pro_array;
        String[] subtitleEntry= mContext.getResources().getStringArray(proResArray);
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        int itemPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING);
        int selectPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_SETTING_SELECT);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"item positon:"+itemPosition+",select position:"+selectPosition);
        for(int i=0;i<subtitleEntry.length;i++){
            int defaultValue= (i==itemPosition ? selectPosition:0);
            int cityIndex=RegionConst.getEcuadorCityArray(i);
            String entryVlaue[]=mContext.getResources().getStringArray(cityIndex);
            Preference proPreference=util.createListPreference(
                    MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING+i,
                    subtitleEntry[i], true, entryVlaue,defaultValue);
            proPreference.setSummary(entryVlaue[defaultValue]);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"region setting:"+entryVlaue[defaultValue]+",isEcuador:"+isEcuador);
            preferenceScreen.addPreference(proPreference);
        }
        regionSettingScreen=preferenceScreen;
        return preferenceScreen ;
    }
  //Region Setting phi
    private PreferenceScreen addSetupSubRegionSettingsPhi(
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        preferenceScreen.addPreference(util.createPreference(
                MenuConfigManager.SETUP_REGION_SETTING_LUZON, R.string.menu_setup_region_setting_philippines_luzon));

        preferenceScreen.addPreference(util.createPreference(
                MenuConfigManager.SETUP_REGION_SETTING_VISAYAS, R.string.menu_setup_region_setting_philippines_visayas));

        preferenceScreen.addPreference(util.createPreference(
                MenuConfigManager.SETUP_REGION_SETTING_MINDANAO, R.string.menu_setup_region_setting_philippines_mindanao));
        return preferenceScreen ;
    }
  //Region Setting phi
    private PreferenceScreen addSetupSubRegionSettingsPhiNationwide(
            PreferenceScreen preferenceScreen, Context themedContext,String parentId){
        int proResArray=0;
        int cityIndex[]=null;
        if (TextUtils.equals(MenuConfigManager.SETUP_REGION_SETTING_LUZON, parentId)){
            proResArray=R.array.menu_setup_region_setting_phi_pro_luzon_array;
            cityIndex=RegionConst.phiProsCityLuzong;
            preferenceScreen.setTitle(R.string.menu_setup_region_setting_philippines_luzon);
        }else if(TextUtils.equals(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS, parentId)){
            proResArray=R.array.menu_setup_region_setting_phi_pro_visayas_array;
            cityIndex=RegionConst.phiProsCityVisayas;
            preferenceScreen.setTitle(R.string.menu_setup_region_setting_philippines_visayas);
        }else{
            proResArray=R.array.menu_setup_region_setting_phi_pro_mindanao_array;
            cityIndex=RegionConst.phiProsCityMindanao;
            preferenceScreen.setTitle(R.string.menu_setup_region_setting_philippines_mindanao);
        }

        String[] subtitleEntry= mContext.getResources().getStringArray(proResArray);
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        int itemPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING);
        int selectPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_SETTING_SELECT);
        String pId=SaveValue.getInstance(mContext).readStrValue(MenuConfigManager.SETUP_REGION_SETTING);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"item positon:"+itemPosition+",select position:"+selectPosition+",pId:"+pId);
        for(int i=0;i<subtitleEntry.length;i++){
            int defaultValue= (i==itemPosition ? selectPosition:0);
            String entryVlaue[]=mContext.getResources().getStringArray(cityIndex[i]);
            Preference proPreference=util.createListPreference(
                    parentId+i,
                    subtitleEntry[i], true, entryVlaue,defaultValue);
            preferenceScreen.addPreference(proPreference);
        }
        regionSettingScreen=preferenceScreen;
        return preferenceScreen ;
    }
    // Ginga Setup
    private PreferenceScreen addSetupSubGingaSettings (
            PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // Ginga Enable

        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.GINGA_ENABLE,
                R.string.menu_setup_ginga_enable,
                mTV.getConfigValue(MenuConfigManager.GINGA_ENABLE) == 0 ? false : true));

        //Auto Start Application

        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.AUTO_START_APPLICATION,
                R.string.menu_setup_ginga_auto_start_app,
                mTV.getConfigValue(MenuConfigManager.AUTO_START_APPLICATION)==0? false :true));

        return preferenceScreen;
    }



    private PreferenceScreen addSetupSubOADSettings(
        PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        String autoKey="Auto_Download_aod";
        boolean isInit=SaveValue.getInstance(mContext).readBooleanValue(autoKey);
        if(!isInit){
            SaveValue.getInstance(mContext).saveBooleanValue(autoKey,true);
            mConfigManager.setValue(MenuConfigManager.SETUP_OAD_SET_AUTO_DOWNLOAD, 1);
        }
        //Auto Download
        preferenceScreen.addPreference(
                    util.createListPreference(MenuConfigManager.SETUP_OAD_SET_AUTO_DOWNLOAD,
                        R.string.menu_setup_oad_set_auto_dl, true,
                        mContext.getResources().getStringArray(R.array.menu_setup_oad_auto_download_array),
                        mConfigManager.getDefault(MenuConfigManager.SETUP_OAD_SET_AUTO_DOWNLOAD)));

        return preferenceScreen;
    }


    private PreferenceScreen addSetupSubSubtitleSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTvsource-----:"+DataSeparaterUtil.getInstance().getSourceDataByCountry(MtkTvConfig.getInstance().getCountry()));
        String string = DataSeparaterUtil.getInstance().getSourceDataByCountry(MtkTvConfig.getInstance().getCountry());
        boolean onlyATV = true;
        boolean onlyDTV = true;
        if(!TextUtils.isEmpty(string)) {
            String[] strings = string.split(",");
            if(strings.length == 3) {
                if("0".equals(strings[0])){

                    onlyATV = false;
                }

                if("0".equals(strings[1])){
                    onlyDTV = false;
                }
            }
        }
//        if(CommonIntegration.getInstance().isCurCHAnalog()){
            // Analog Subtitle
        if(onlyATV || CommonIntegration.getInstance().isCurrentSourceComposite()){
            preferenceScreen.addPreference(util.createListPreference(
                    MenuConfigManager.ANALOG_SUBTITLE,
                    R.string.menu_setup_analog_subtitle,
                    true,
                    mContext.getResources().getStringArray(
                            R.array.menu_setup_analog_subtitle_array),
                    mConfigManager.getDefault(MenuConfigManager.ANALOG_SUBTITLE)));
        }

//        }

        if(onlyDTV){
        preferenceScreen.addPreference(util.createListPreference(
                MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE,
                R.string.menu_setup_digital_subtitle,
                true,
                mContext.getResources().getStringArray(
                        R.array.menu_setup_digital_subtitle_array),
                mConfigManager.getDefault(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE)));

        // Subtitle Track
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUBTITLE)) {
            List<TvTrackInfo> tracks = null;
            String currentID = "";
            String offValue = String.valueOf(0XFF);
            LanguageUtil languageUtil = new LanguageUtil(themedContext);
            if (CommonIntegration.TV_FOCUS_WIN_MAIN == CommonIntegration.getInstance()
                    .getCurrentFocus()) {
                tracks = TurnkeyUiMainActivity.getInstance().getTvView()
                        .getTracks(TvTrackInfo.TYPE_SUBTITLE);
                currentID = TurnkeyUiMainActivity.getInstance().getTvView()
                        .getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            } else {
                tracks = TurnkeyUiMainActivity.getInstance().getPipView()
                        .getTracks(TvTrackInfo.TYPE_SUBTITLE);
                currentID = TurnkeyUiMainActivity.getInstance().getPipView()
                        .getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            }
            if (tracks != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentID" + currentID);
                try {
                    SaveValue.getInstance(themedContext).saveValue(
                            MenuConfigManager.SUBTITLE_TRACKS, Integer.valueOf(currentID));
                } catch (Exception e) {
                    SaveValue.getInstance(themedContext).saveValue(
                            MenuConfigManager.SUBTITLE_TRACKS, -1);
                }
                int trackCount = tracks.size();
                if (trackCount > 0) {
                    trackCount++;
                }
                String[] subKeys = new String[trackCount];
                String[] subValues = new String[trackCount];
                Preference subtitletrackItem = null;
                for (int i = 0; i < trackCount; i++) {
                    boolean trackOff = i == trackCount - 1;
                    if (trackOff) {
                        subKeys[i] = themedContext.getResources().getString(R.string.common_off);
                        subValues[i] = offValue;
                    } else {
                        TvTrackInfo track = tracks.get(i);
                        String lang = languageUtil.getSubitleNameByValue(track.getLanguage());
                        String langId = track.getExtra().getString("key_LangIdx");
                        String hearingImpaired = track.getExtra().getString("key_HearingImpaired");
                        String trackId = track.getId();
                        subKeys[i] = Integer.parseInt(langId) > 0 ? lang + " " + langId : lang;
                        if(!TextUtils.isEmpty(hearingImpaired)){
                            subKeys[i] = "true".equals(hearingImpaired)?subKeys[i]+"("+themedContext.getResources().getString(R.string.sound_tracks_hoh)+")":subKeys[i];
                        }
                        subValues[i] = trackId;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang :" + langId);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"subKeys-----------:"+subKeys[i]);
                    }
                }
                subtitletrackItem = util.createListPreference(MenuConfigManager.SUBTITLE_TRACKS,
                        R.string.menu_subtitle_track, true, subKeys, subValues, currentID);
                TvInputCallbackMgr.getInstance(mContext).setSubtitleCallback(
                        createSubtitleCallback(themedContext.getApplicationContext(), languageUtil, preferenceScreen));
                /*
                 * @Override public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
                 * if(tracks == null) return;
                 * com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onTracksChanged trackId : "+track.getId()); }
                 * @Override public void onTrackSelected(String inputId, String trackId) { try {
                 * SaveValue
                 * .getInstance(mContext).saveValue(MenuConfigManager.SUBTITLE_TRACKS,Integer
                 * .valueOf(trackId)); ListPreference subtitlePre =
                 * (ListPreference)preferenceScreen.
                 * findPreference(MenuConfigManager.SUBTITLE_TRACKS); CharSequence[] entryValues =
                 * subtitlePre.getEntryValues(); CharSequence[] entries = subtitlePre.getEntries();
                 * for (int subIndex = 0; subIndex < entryValues.length; subIndex++) { if
                 * (entryValues[subIndex].equals(trackId)) {
                 * com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onTrackSelected setSummary : "+entries[subIndex]);
                 * subtitlePre.setSummary(entries[subIndex]);
                 * subtitlePre.setValue(entryValues[subIndex].toString()); } } } catch (Exception e)
                 * { com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"onTrackSelected error:"+e.toString()); } } });
                 */
                preferenceScreen.addPreference(subtitletrackItem);
            }
        }

        int arrayID=R.array.menu_tv_subtitle_language_eu_array;
        if(mTV.isIDNCountry()||mTV.isMYSCountry()||mTV.isAUSCountry()||mTV.isVNMCountry()){
            arrayID=R.array.menu_tv_subtitle_language_in_mys_aus_tha_vnm_array_value;
        }else if(mTV.isSQPCountry()){
            arrayID=R.array.menu_tv_subtitle_language_sgp_array_value;
        }else if(mTV.isNZLCountry()){
            arrayID=R.array.menu_tv_subtitle_language_nzl_array_value;
        }else if(CommonIntegration.isEUPARegion()) {
            arrayID=R.array.menu_tv_subtitle_language_pa_array_value;
        }else{
            arrayID=R.array.menu_tv_subtitle_language_eu_array_value;
        }
        String[] subtitleEntry= mContext.getResources()
        .getStringArray(
                arrayID);
        int sublangIndex = util.mOsdLanguage
               .getSubtitleLanguage(MenuConfigManager.DIGITAL_SUBTITLE_LANG);
        boolean subtitleEnable = sublangIndex == subtitleEntry.length - 1;

        List<String> mSubtitleList = new ArrayList<>();

        mSubtitleList = subtitleAudioLanguageTransform("Subtitle",subtitleEntry);

        subtitleEntry = new String[mSubtitleList.size()];
        for (int i=0;i<mSubtitleList.size();i++){
            subtitleEntry[i] = mSubtitleList.get(i);
        }

        // Digital Subtitle Lang.
        preferenceScreen.addPreference(
                util.createListPreference(
                        MenuConfigManager.DIGITAL_SUBTITLE_LANG,
                        R.string.menu_setup_digital_subtitle_lang,
                        true,
                        subtitleEntry,
                        sublangIndex));

        // Digital Subtitle Lang. 2nd
        preferenceScreen.addPreference(
                util.createListPreference(
                        MenuConfigManager.DIGITAL_SUBTITLE_LANG_2ND,
                        R.string.menu_setup_digital_subtitle_lang_2nd,
                        !subtitleEnable,
                        subtitleEntry,
                        util.mOsdLanguage
                                .getSubtitleLanguage(MenuConfigManager.DIGITAL_SUBTITLE_LANG_2ND)));

        // Subtitle Type
        preferenceScreen.addPreference(
                util.createListPreference(
                        MenuConfigManager.SUBTITLE_TYPE,
                        R.string.menu_setup_subtitle_type,
                        !subtitleEnable,
                        mContext.getResources().getStringArray(
                                R.array.menu_setup_subtitle_type_array),
                        mConfigManager.getDefault(MenuConfigManager.SUBTITLE_TYPE)));

        }

        return preferenceScreen;
    }

    private PreferenceScreen addSetupSubTeletextSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        if (CommonIntegration.isCNRegion()) {
            dTLanguage = mContext.getResources().getStringArray(
                    R.array.menu_setup_digital_teletext_language_array_cn);
        } else {
            dTLanguage = mContext.getResources().getStringArray(
                    R.array.menu_setup_digital_teletext_language_array);
        }

        if (CommonIntegration.isCNRegion()) {
            dPLanguage = mContext.getResources().getStringArray(
                    R.array.menu_setup_decoding_page_language_array_cn);
        } else {
            dPLanguage = mContext.getResources().getStringArray(
                    R.array.menu_setup_decoding_page_language_array);
        }

        tPLevel = mContext.getResources().getStringArray(
                R.array.menu_setup_ttx_presentation_level_array);

        if (CommonIntegration.isEURegion() && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TTX)){

            //Digital TeleText Language
            preferenceScreen.addPreference(
                        util.createListPreference(MenuConfigManager.SETUP_DIGITAL_TELETEXT_LANGUAGE,
                            R.string.menu_setup_digital_teletext_language, true, dTLanguage,
                            mConfigManager.getDefault(MenuConfigManager.SETUP_DIGITAL_TELETEXT_LANGUAGE)));

            //Decoding Page Language
            preferenceScreen.addPreference(
                        util.createListPreference(MenuConfigManager.SETUP_DECODING_PAGE_LANGUAGE,
                            R.string.menu_setup_decoding_page_language, true, dPLanguage,
                            mConfigManager.getDefault(MenuConfigManager.SETUP_DECODING_PAGE_LANGUAGE)));

            //TTX Presentation Level
            /*preferenceScreen.addPreference(
                        util.createListPreference(MenuConfigManager.SETUP_TTX_PRESENTATION_LEVEL,
                            R.string.menu_setup_ttx_presentation_level, true, tPLevel,
                            mConfigManager.getDefault(MenuConfigManager.SETUP_TTX_PRESENTATION_LEVEL)));*/
        }else if (CommonIntegration.isCNRegion()){
            //Digital TeleText Language
            preferenceScreen.addPreference(
                        util.createListPreference(MenuConfigManager.SETUP_DIGITAL_TELETEXT_LANGUAGE,
                            R.string.menu_setup_digital_teletext_language, true, dTLanguage,
                            mConfigManager.getDefault(MenuConfigManager.SETUP_DIGITAL_TELETEXT_LANGUAGE)));

            //Decoding Page Language
            preferenceScreen.addPreference(
                        util.createListPreference(MenuConfigManager.SETUP_DECODING_PAGE_LANGUAGE,
                            R.string.menu_setup_decoding_page_language, true, dPLanguage,
                            mConfigManager.getDefault(MenuConfigManager.SETUP_DECODING_PAGE_LANGUAGE)));

            //TTX Presentation Level
            /*preferenceScreen.addPreference(
                        util.createListPreference(MenuConfigManager.SETUP_TTX_PRESENTATION_LEVEL,
                            R.string.menu_setup_ttx_presentation_level, true, tPLevel,
                            mConfigManager.getDefault(MenuConfigManager.SETUP_TTX_PRESENTATION_LEVEL)));*/
        }

        return preferenceScreen;
    }

    private PreferenceScreen addSetupSubHbbtvSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addSetupSubHbbtvSettings :> start..");
        String[] hbbtvStrings = mContext.getResources().getStringArray(
                R.array.menu_hbbtv_do_not_track);
        String[] hbbtvcookieStrs = mContext.getResources().getStringArray(
                R.array.menu_hbbtv_cookie_setting);
        //hbbtv support
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.HBBTV_SUPPORT, R.string.menu_setup_HBBTV_Support,
                getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.HBBTV_SUPPORT))));

        //Do Not Track
        preferenceScreen.addPreference(
                    util.createListPreference(MenuConfigManager.HBBTV_DO_NOT_TRACK,
                        R.string.menu_setup_HBBTV_not_track, true, hbbtvStrings,
                        mConfigManager.getDefault(MenuConfigManager.HBBTV_DO_NOT_TRACK)));

        //Cookie Settings
        preferenceScreen.addPreference(
                    util.createListPreference(MenuConfigManager.HBBTV_ALLOW_3RD_COOKIES,
                        R.string.menu_setup_HBBTV_cookie_settings, true, hbbtvcookieStrs,
                        mConfigManager.getDefault(MenuConfigManager.HBBTV_ALLOW_3RD_COOKIES)));

        //Persistent Storage
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.HBBTV_PERSISTENT_STORAGE, R.string.menu_setup_HBBTV_persistent_storage,
                getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.HBBTV_PERSISTENT_STORAGE))));

        //Block Tracking Sites
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.HBBTV_BLOCK_TRACKING_SITES, R.string.menu_setup_HBBTV_track_sites,
                getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.HBBTV_BLOCK_TRACKING_SITES))));

        //Device ID
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.HBBTV_DEV_ID, R.string.menu_setup_HBBTV_deviceid,
                getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.HBBTV_DEV_ID))));

        //Reset Device ID
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
        .setTitle(R.string.menu_setup_HBBTV_reset_deviceid)
        .setMessage(R.string.menu_setup_HBBTV_reset_deviceid_message)
        .setPositiveButton(R.string.menu_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mTV.setConfigValue(MenuConfigManager.HBBTV_DEV_ID_TIMESTAMP, (int)(System.currentTimeMillis()/1000));
            }
        });

        preferenceScreen.addPreference(util.createDialogPreference(MenuConfigManager.HBBTV_RESET_DEVICE_ID,
                R.string.menu_setup_HBBTV_reset_deviceid,builder.create()));

        return preferenceScreen;
    }

    private PreferenceScreen addVideoSubVgaViSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addVideoSubVgaViSettings :> start..");
        // auto adjust
        Preference vgaAutoAdjust = util.createClickPreference(MenuConfigManager.AUTO_ADJUST,
                R.string.menu_video_auto_adjust);
        preferenceScreen.addPreference(vgaAutoAdjust);

        // h.position
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.HPOSITION, R.string.menu_video_hposition, true));

        // v.position
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.VPOSITION, R.string.menu_video_vposition, true));

        // phase
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.PHASE, R.string.menu_video_phase, true));

        // clock
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.CLOCK, R.string.menu_video_clock, true));

        vgaAutoAdjust.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceClick " + preference);
                autoAdjustInfo(mContext.getString(R.string.menu_video_auto_adjust_info));
                Message message = mHandler.obtainMessage();
                message.obj = preference;
                if (preference.getKey().equals(MenuConfigManager.AUTO_ADJUST)) {
                    appTV.setAutoClockPhasePosition(mNavIntegration.getCurrentFocus());
                    message.what = MessageType.MESSAGE_AUTOADJUST;
                } else if (preference.getKey().equals(MenuConfigManager.FV_AUTOCOLOR)) {
                    appTV.setAutoColor(mNavIntegration.getCurrentFocus());
                    message.what = MessageType.MESSAGE_AUTOCOLOR;
                } else {
                    appTV.setAutoClockPhasePosition(mNavIntegration.getCurrentFocus());
                    message.what = MessageType.MESSAGE_AUTOADJUST;
                }
                mHandler.sendMessageDelayed(message,
                        MessageType.delayMillis6);

                mConfigManager.setValueDefault(preference.getKey());
                return true;
            }
        });

        return preferenceScreen;
    }

    private void autoAdjustInfo(String mShowMessage) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoAdjustInfo");
        autoAdjustDialog = new LiveTVDialog(mContext, 5);
        autoAdjustDialog.setMessage(mShowMessage);
        autoAdjustDialog.show();
        autoAdjustDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                return true;
            }
        });
    }
    public static int reginIndex;
    public static int dimIndex;
    private PreferenceScreen addParentalSubOpenVChipLevel(
            PreferenceScreen preferenceScreen,Context themedContext,int dimNum){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_TEXT);
        mTV.getOpenVCHIPPara().setDimIndex(dimNum);

        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_LVL_NUM);
        int levelNum = mTV.getOpenVchip().getLevelNum();
        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_LVL_ABBR);
        reginIndex = Integer.parseInt(regin);
        dimIndex = Integer.parseInt(dim);
        byte[] levelBlockStrings = mTV.getNewOpenVchipSetting(reginIndex,dimIndex).getLvlBlockData();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "levelNum:" + levelNum + "levelBlockStrings:"
                + levelBlockStrings);
        for (int m = 0; m < levelBlockStrings.length; m++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "print levelBlockStrings[ " + m + "]=" + levelBlockStrings[m]);
          }
        for (int k = 0; k < levelNum; k++) {
            mTV.getOpenVCHIPPara().setLevelIndex(k + 1);
            ItemName = MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL + k;
            String textString = mTV.getOpenVchip().getLvlAbbrText();
            if (textString != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "textString:" + textString);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "textString == null");
                textString = "";
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "levelBlockStrings[" + k
                    + "]:===levelBlockStrings:" + levelBlockStrings[k]);
            Preference openvchipItemLevel = util.createListPreference(
                    MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL + k,
                    textString, true,new String[]{"OFF","ON"}, levelBlockStrings[k]);
            byte[] levelBlicks = mTV.getNewOpenVchipSetting(reginIndex, dimIndex).getLvlBlockData();
            openvchipItemLevel.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = Integer.parseInt((String)preference.getKey().substring(
                            MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL.length(),
                            ((String)preference.getKey()).length()));
                    MtkTvOpenVCHIPSettingInfoBase info = TVContent.getInstance(mContext)
                            .getNewOpenVchipSetting(reginIndex, dimIndex);
                    byte[] block = info.getLvlBlockData();
                    int iniValue = block[index];
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("yiqinghuang", "reginIndex ="+reginIndex+",dimIndex ="+dimIndex+"index ="+index);
                    byte[] levelBlicks = mTV.getNewOpenVchipSetting(reginIndex, dimIndex).getLvlBlockData();
                    int value = Integer.parseInt((String)newValue);
                    SaveValue.getInstance(themedContext).saveValue((String)preference.getKey(), value);
                    if (value!=iniValue) {
                        EditChannel.getInstance(mContext).setOpenVCHIP(reginIndex,dimIndex,index);
                    }
                    return true;
                }
            });
        preferenceScreen.addPreference(openvchipItemLevel);
        }
        return preferenceScreen;
    }
    List<Preference> openVChipLevels = new ArrayList<Preference>();
    private PreferenceScreen addParentalSubOpenVChipDim(
            PreferenceScreen preferenceScreen,Context themedContext,int reginNum){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_TEXT);
        mTV.getOpenVCHIPPara().setRegionIndex(reginNum);

        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_NUM);
        int dimNum = mTV.getOpenVchip().getDimNum();
        for (int j = 0; j < dimNum; j++) {
            mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                    MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_TEXT);
            mTV.getOpenVCHIPPara().setDimIndex(j);
            Preference openvchipItemDim = util.createPreference(
                    MenuConfigManager.PARENTAL_OPEN_VCHIP_DIM+j,
                    mTV.getOpenVchip().getDimText());
            preferenceScreen.addPreference(openvchipItemDim);
        }
        return preferenceScreen;
    }
    private PreferenceScreen addParentalSubOpenVChipSetting(
            PreferenceScreen preferenceScreen,Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_NUM);
        int regionNum = mTV.getOpenVchip().getRegionNum();
        for (int i = 0; i < regionNum; i++) {
            mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                    MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_TEXT);
            mTV.getOpenVCHIPPara().setRegionIndex(i);
            Preference openvchipItemRegin = util.createPreference(
                    MenuConfigManager.PARENTAL_OPEN_VCHIP_REGIN+i,
                    mTV.getOpenVchip().getRegionText());
            preferenceScreen.addPreference(openvchipItemRegin);
        }
        return preferenceScreen;
    }

    /*
    private PreferenceScreen addParentalSubInputBlockSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        String nowSource = CommonIntegration.getInstance().getCurrentSource();
        List<String> sourceList = mTV.getSourceManager().getInputSourceList();
        if (sourceList!=null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addParentalSubInputBlockSettings :" + sourceList.size() + ",nowSource="
                    + nowSource);
        }
        String[] sourceArray = new String[] {
                "Off", "Block"
        };
        int initvalue = 0;
        int selectPos = 0;

        if (null != sourceList) {
            for (int i = 0; i < sourceList.size(); i++) {
                if (sourceList.get(i).equalsIgnoreCase(nowSource)) {
                    selectPos = i;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addParentalSubInputBlockSettings sourceList.get(" + i + "):"
                        + sourceList.get(i));
                initvalue = mEditChannel.isInputBlock(sourceList.get(i)) ? 1 : 0;
                preferenceScreen.addPreference(
                util.createListPreference(MenuConfigManager.PARENTAL_INPUT_BLOCK_SOURCE + "#" + String.valueOf(i),
                                sourceList.get(i), true, sourceArray, initvalue));
            }
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sourceList.get(i) == null:");
        }

        return preferenceScreen;
    }*/

    boolean isContainVchip(){
        return mainPreferenceScreen.findPreference((CharSequence)MenuConfigManager.SCREEN_MODE) ==null ? true:false;
    }
/*
    private PreferenceScreen addParentalSubProgramBlockSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("guanglei", "mTV.getRatingEnable():" + mTV.getRatingEnable());

        // EU
        if (CommonIntegration.isEURegion()) {
            String tempItemId = MenuConfigManager.PARENTAL_AGE_RATINGS_EU;
            String ageRating = mContext.getString(R.string.menu_age_ratings);
            int ageRatingId = R.string.menu_age_ratings;
            String[] ageArray = mContext.getResources().getStringArray(
                    R.array.menu_parental_age_ratings_array_eu);
            if (mTV.isAusCountry()) {
                tempItemId = MenuConfigManager.PARENTAL_AGE_RATINGS_EU_OCEANIA_AUS;
                ageRating = mContext.getString(R.string.menu_oceania_aus_ratings);
                ageRatingId = R.string.menu_oceania_aus_ratings;
                ageArray = mContext.getResources().getStringArray(
                        R.array.menu_parental_age_ratings_array_eu_ocean_aus);
            } else if (mTV.isFraCountry()) {// france 's age rating is no None
                String[] temp = new String[ageArray.length - 1];
                for (int i = 1; i < ageArray.length; i++) {
                    temp[i - 1] = ageArray[i];
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("biaoqing", "temp[" + (i - 1) + "]" + temp[i - 1]);
                }
                ageArray = temp;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ageArray length" + ageArray.length);
            }else if (mTV.isSgpCountry()){
                tempItemId = MenuConfigManager.PARENTAL_AGE_RATINGS_EU_SGP;
                ageArray = mContext.getResources().getStringArray(R.array.menu_parental_age_ratings_array_eu_sgp);
            }else if (mTV.isSouthAfricaCountry()) {
                    tempItemId = MenuConfigManager.PARENTAL_AGE_RATINGS_EU_ZAF;
                    ageArray = mContext.getResources().getStringArray(R.array.menu_parental_age_ratings_array_eu_zaf);
            } else if (mTV.isTHACountry()){
                tempItemId = MenuConfigManager.PARENTAL_AGE_RATINGS_EU_THL;
                ageArray = mContext.getResources().getStringArray(R.array.menu_parental_age_ratings_array_eu_thl);
            }
            Preference euProgramBlockPre = util.createListPreference(
                    tempItemId,
                    ageRating,
                    true,
                    ageArray,
                    mConfigManager.getDefault(tempItemId));
            preferenceScreen.addPreference(euProgramBlockPre);
            return preferenceScreen;
        }

        if (CommonIntegration.isUSRegion()) {
            // Rating Enable
            preferenceScreen.addPreference(util.createSwitchPreference(
                    MenuConfigManager.PARENTAL_RATINGS_ENABLE,
                    R.string.menu_rating_enable, mTV.getRatingEnable() == 1 ? true : false));
        }

        // U.S.TV Ratings
        preferenceScreen.addPreference(util.createFragmentPreference(
                MenuConfigManager.PARENTAL_US_TV_RATINGS,
                R.string.menu_us_tv_ratings,
                (mTV.getRatingEnable() == 1 ? true : false),
                RatingFragment.class.getName()));

        // U.S Movie Ratings
        preferenceScreen.addPreference(util.createFragmentPreference(
                MenuConfigManager.PARENTAL_US_MOVIE_RATINGS,
                R.string.menu_us_movie_ratings,
                (mTV.getRatingEnable() == 1 ? true : false),
                RatingOtherFragment.class.getName()));
        // Canadian English Ratings
        preferenceScreen.addPreference(util.createFragmentPreference(
                MenuConfigManager.PARENTAL_CANADIAN_ENGLISH_RATINGS,
                R.string.menu_canadian_english_ratings,
                (mTV.getRatingEnable() == 1 ? true : false),
                RatingOtherFragment.class.getName()));

        // Canadian French Ratings
        preferenceScreen.addPreference(util.createFragmentPreference(
                MenuConfigManager.PARENTAL_CANADIAN_FRENCH_RATINGS,
                R.string.menu_canadian_french_ratings,
                (mTV.getRatingEnable() == 1 ? true : false),
                RatingOtherFragment.class.getName()));
        boolean isOpenVChipEnable = false;

        if (mTV.getRatingEnable() == 0) {
            isOpenVChipEnable = false;
        }else {
            if (mTV.getATSCRating().isOpenVCHIPInfoAvailable()) {
                isOpenVChipEnable = true;
            }else {
                isOpenVChipEnable = false;
            }
        }
        if (CommonIntegration.isUSRegion()) {
            // Open V-Chip
            // mTV.getATSCRating().isOpenVCHIPInfoAvailable()
            // PARENTAL_OPEN_VCHIP menu_open_vchip
            openVchip = util.createPreference(
                    MenuConfigManager.PARENTAL_OPEN_VCHIP,
                    R.string.menu_open_vchip);
            openVchip.setEnabled(isOpenVChipEnable);
            if (mTV.isCurrentSourceTv()) {
                preferenceScreen.addPreference(openVchip);
            }

            //tif content ratings, only for cts verify.
            loadContentRatingsSystems(preferenceScreen, themedContext, null);

            // Block Unrated
            Preference buPre = util.createSwitchPreference(
                    MenuConfigManager.PARENTAL_BLOCK_UNRATED,
                    R.string.menu_block_unrated, mTV.getBlockUnrated() == 1 ? true : false);
            buPre.setEnabled(mTV.getRatingEnable() == 1 ? true : false);
            preferenceScreen.addPreference(buPre);

        }

        if (CommonIntegration.isSARegion()) {
            // Age Rating
            String[] AgeRatingVlaues = mContext.getResources().getStringArray(
                    R.array.menu_parental_age_ratings_array);
            preferenceScreen.addPreference(util.createListPreference(
                    MenuConfigManager.PARENTAL_AGE_RATINGS,
                    R.string.menu_age_ratings,
                    true,
                    AgeRatingVlaues,
                    MenuConfigManager.getInstance(themedContext).getDefault(
                            MenuConfigManager.PARENTAL_AGE_RATINGS)));

            // Content Rating
            String[] ContentRatingVlaues = mContext.getResources().getStringArray(
                    R.array.menu_parental_content_ratings_array);
            preferenceScreen.addPreference(util.createListPreference(
                    MenuConfigManager.PARENTAL_CONTENT_RATINGS,
                    R.string.menu_content_ratings,
                    true,
                    ContentRatingVlaues,
                    MenuConfigManager.getInstance(themedContext).getDefault(
                            MenuConfigManager.PARENTAL_CONTENT_RATINGS)));
        }
        return preferenceScreen;
    }*/
    public static int block = -1;
    private PreferenceScreen addParentalSecondChannelScheduleBlock(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(mContext).getTVChannelList();
        String[] optionValue = new String[] {
                "Off", "Block"
        };
        for (MtkTvChannelInfoBase infobase : list) {
            int tid = infobase.getChannelId();
            String name = "" + mDataHelper.getDisplayChNumber(tid) + "        "
                    + ((infobase.getServiceName() == null) ? "" : infobase.getServiceName());
            String description = "";
            if (mEditChannel.getSchBlockType(infobase.getChannelId()) == 0) {
                description = optionValue[0];
            } else {
                description = optionValue[1];
            }

            Preference refer = util.createPreference(
                    MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST + tid,
                    name);
            refer.setSummary(description);
            preferenceScreen.addPreference(refer);
        }
        return preferenceScreen;
    }

    private PreferenceScreen addParentalLastSubChannelScheduleBlock(
            PreferenceScreen preferenceScreen, Context themedContext, String parentId) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(mContext).getTVChannelList();
        MtkTvChannelInfoBase tempInfoBase = null;
        int channelId = -1;
        for (MtkTvChannelInfoBase infobase : list) {
            if (TextUtils.equals(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST +
                    infobase.getChannelId(), parentId)) {
                tempInfoBase = infobase;
                channelId = tempInfoBase.getChannelId();
                block = mEditChannel.getSchBlockType(infobase);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "block: " + block);
            }
        }
        if (tempInfoBase == null || channelId == -1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel info is null or something is wrong!");
            return preferenceScreen;
        }

        // Operation Mode
        final String[] valueStrings = mContext.getResources().
                getStringArray(R.array.menu_parental_block_channel_schedule_operation_array);
        int index = mConfigManager.getDefault(
                MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE + channelId);
        String operationModeSummary = valueStrings[index];
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index: " + index);
        Preference refer = util.createListPreference(
                MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE + channelId,
                R.string.menu_parental_channel_schedule_block_operation_mode,
                true, valueStrings, index);
        refer.setSummary(operationModeSummary);
        preferenceScreen.addPreference(refer);

        boolean isStartDateEnable = false;
        boolean isEndDateEnable = false;
        boolean isStartTimeEnable = false;
        boolean isEndTimeEnable = false;
        if (block == 0) {
            isStartDateEnable = false;
            isEndDateEnable = false;
            isStartTimeEnable = false;
            isEndTimeEnable = false;
        } else if (block == 1) {
            isStartDateEnable = false;
            isStartTimeEnable = true;
            isEndDateEnable = false;
            isEndTimeEnable = true;
        } else {
            isStartDateEnable = true;
            isStartTimeEnable = true;
            isEndDateEnable = true;
            isEndTimeEnable = true;
        }
        // Starting Date
        // String startDate = mEditChannel.getFromDate(tempInfoBase);
        // SaveValue.getInstance(mContext).saveStrValue(MenuConfigManager.TIME_START_DATE +
        // channelId,
        // startDate);
        String startDate = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_START_DATE + channelId);
        Log.d(TAG, "addParentalLastSubChannelScheduleBlock: startDate:" + startDate);
        Preference startDatePreference = util.createFragmentPreference(
                MenuConfigManager.TIME_START_DATE + channelId,
                R.string.menu_parental_channel_schedule_start_date,
                isStartDateEnable,
                DatePicker.class.getName());
        startDatePreference.setSummary(startDate);
        preferenceScreen.addPreference(startDatePreference);

        // start time
        String startTime = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_START_TIME + channelId);
        Preference startTimePreference = util.createFragmentPreference(
                MenuConfigManager.TIME_START_TIME + channelId,
                R.string.menu_parental_channel_schedule_start_time,
                isStartTimeEnable,
                TimePicker.class.getName());
        startTimePreference.setSummary(startTime);
        preferenceScreen.addPreference(startTimePreference);

        // Ending Date
        String endDate = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_END_DATE + channelId);
        Preference endDatePreference = util.createFragmentPreference(
                MenuConfigManager.TIME_END_DATE + channelId,
                R.string.menu_parental_channel_schedule_end_date,
                isEndDateEnable,
                DatePicker.class.getName());
        endDatePreference.setSummary(endDate);
        preferenceScreen.addPreference(endDatePreference);

        // end time
        String endTime = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_END_TIME + channelId);
        Preference endTimePreference = util.createFragmentPreference(
                MenuConfigManager.TIME_END_TIME + channelId,
                R.string.menu_parental_channel_schedule_end_time,
                isEndTimeEnable, TimePicker.class.getName());
        endTimePreference.setSummary(endTime);
        preferenceScreen.addPreference(endTimePreference);

        return preferenceScreen;
    }

    String[] m3DNavArr;
    String[] m3D2DArr;
    String[] m3DImgSafetyArr;
    String[] m3DLrSwitchArr;

    private PreferenceScreen addVideoSub3DSettings(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addVideoSub3DSettings  stsrt>>");
        String itemName;
        String[] m3DModeArr;
        // 3d
        m3DModeArr = mContext.getResources().getStringArray(
                R.array.menu_video_3d_mode_array);

        if(util.mConfigManager.getDefault(MenuConfigManager.VIDEO_3D_NAV)==1){
             m3DModeArr = mContext.getResources().getStringArray(
                       R.array.menu_video_3d_mode_array_for_1);
        }else if(util.mConfigManager.getDefault(MenuConfigManager.VIDEO_3D_NAV)==0)  {
             m3DModeArr = mContext.getResources().getStringArray(
                     R.array.menu_video_3d_mode_array_for_0);
        }
        // 3d navigation
        m3DNavArr = mContext.getResources().getStringArray(
                R.array.menu_video_3d_nav_array);
        m3D2DArr = mContext.getResources().getStringArray(
                R.array.menu_video_3d_3t2switch_array);
        m3DImgSafetyArr = mContext.getResources().getStringArray(
                R.array.menu_video_3d_image_safety_array);
        m3DLrSwitchArr = mContext.getResources().getStringArray(
                R.array.menu_video_3d_lrswitch_array);
        // 3d mode
        itemName = MenuConfigManager.VIDEO_3D_MODE;
        Preference m3DMode = util.createListPreference(itemName, R.string.menu_video_3d_mode,
                true, m3DModeArr, util.mConfigManager.getDefault(itemName));

        // 3d navigation
        itemName = MenuConfigManager.VIDEO_3D_NAV;
        Preference m3DNav = util.createListPreference(itemName, R.string.menu_video_3d_nav,
                true, m3DNavArr, util.mConfigManager.getDefault(itemName));

        // 3d-2d
        itemName = MenuConfigManager.VIDEO_3D_3T2;
        Preference m3D2D = util.createListPreference(itemName,
                R.string.menu_video_3d_3t2, true, m3D2DArr,
                util.mConfigManager.getDefault(itemName));

        // depth of field
        itemName = MenuConfigManager.VIDEO_3D_FIELD;
        Preference m3DDepthField = util.createProgressPreference(itemName,
                R.string.menu_video_3d_depth_field, false);

        // protrude
        itemName = MenuConfigManager.VIDEO_3D_PROTRUDE;
        Preference m3DProtrude = util.createProgressPreference(itemName,
                R.string.menu_video_3d_protrude, false);

        // distance to TV
        itemName = MenuConfigManager.VIDEO_3D_DISTANCE;
        Preference m3DDistance = util.createProgressPreference(itemName,
                R.string.menu_video_3d_distance, false);

        // image safety
        itemName = MenuConfigManager.VIDEO_3D_IMG_SFTY;
        Preference m3DImgSafety = util.createListPreference(itemName,
                R.string.menu_video_3d_image_safety, true
                , m3DImgSafetyArr, util.mConfigManager.getDefault(itemName));

        // L-R switch
        itemName = MenuConfigManager.VIDEO_3D_LF;
        Preference m3DLrSwitch = util.createListPreference(itemName,
                R.string.menu_video_3d_leftright, true, m3DLrSwitchArr,
                util.mConfigManager.getDefault(itemName));

        // // OSD Depth
        itemName = MenuConfigManager.VIDEO_3D_OSD_DEPTH;
        Preference m3DOsdDepth = util.createProgressPreference(itemName,
                R.string.menu_video_3d_osd, true);

        ArrayList<Boolean> m3DConfigList = (ArrayList<Boolean>)mConfigManager.get3DConfig();
        boolean m3DModeFlag = m3DConfigList.get(0);
        boolean m3DNavFlag = m3DConfigList.get(1);
        boolean m3D2DFlag = m3DConfigList.get(2);
        boolean m3DDepthFieldFlag = m3DConfigList.get(3);
        boolean m3DProtrudeFlag = m3DConfigList.get(4);
        boolean m3DDistanceFlag = m3DConfigList.get(5);
        boolean m3DImgSafetyFlag = m3DConfigList.get(6);
        boolean m3DLrSwitchFlag = m3DConfigList.get(7);
        boolean m3DOsdDepthFlag = m3DConfigList.get(8);

        m3DMode.setEnabled(m3DModeFlag);
        m3DNav.setEnabled(m3DNavFlag);
        m3D2D.setEnabled(m3D2DFlag);
        m3DDepthField.setEnabled(m3DDepthFieldFlag);
        m3DProtrude.setEnabled(m3DProtrudeFlag);
        m3DDistance.setEnabled(m3DDistanceFlag);
        m3DImgSafety.setEnabled(m3DImgSafetyFlag);
        m3DLrSwitch.setEnabled(m3DLrSwitchFlag);
        if (!CommonIntegration.isCNRegion()) {
            m3DOsdDepth.setEnabled(m3DOsdDepthFlag);
        }

        preferenceScreen.addItemFromInflater(m3DMode);
        preferenceScreen.addItemFromInflater(m3DNav);
        preferenceScreen.addItemFromInflater(m3D2D);
        preferenceScreen.addItemFromInflater(m3DDepthField);
        preferenceScreen.addItemFromInflater(m3DProtrude);
        preferenceScreen.addItemFromInflater(m3DDistance);
        preferenceScreen.addItemFromInflater(m3DImgSafety);
        preferenceScreen.addItemFromInflater(m3DLrSwitch);
        if (!CommonIntegration.isCNRegion()) {
            preferenceScreen.addItemFromInflater(m3DOsdDepth);
        }

        return preferenceScreen;
    }
    Preference visuallyvolume = null;
    Preference visuallyImpairedAudioInfo=null;
    private PreferenceScreen addAudioSubvisuallyimpairedSettings(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addAudioSubvisuallyimpairedSettings  stsrt>>");
        String[] mVisuallySpeaker = mContext.getResources().getStringArray(
                R.array.menu_audio_visually_speaker_array);
        String[] mVisuallyHeadphone = mContext.getResources().getStringArray(
                R.array.menu_audio_visually_headphone_array);

        ItemName = MenuConfigManager.VISUALLY_SPEAKER;
        Preference speaker = util.createSwitchPreference(ItemName,
                R.string.menu_audio_visually_speaker,
                util.mConfigManager.getDefault(ItemName)==0? false:true);
        speaker.setOnPreferenceChangeListener(mvisuallyimpairedChangeListener);

        ItemName = MenuConfigManager.VISUALLY_HEADPHONE;
        Preference headphone =util.createSwitchPreference(ItemName,
                R.string.menu_audio_visually_headphone,
                util.mConfigManager.getDefault(ItemName)==0? false:true);
        headphone.setOnPreferenceChangeListener(mvisuallyimpairedChangeListener);

        visuallyvolume = util.createProgressPreference(
                MenuConfigManager.VISUALLY_VOLUME, R.string.menu_audio_visually_volume, true);
        // for eu
        ItemName = MenuConfigManager.VISUALLY_PAN_FADE;
        Preference panaAndFade = util.createSwitchPreference(ItemName,
                R.string.menu_audio_visually_pan_and_fade,
                util.mConfigManager.getDefault(ItemName)==0? false:true);
        ItemName = MenuConfigManager.CFG_MENU_AUDIOINFO;
        visuallyImpairedAudioInfo = util.createPreference(ItemName,
                R.string.menu_audio_visually_impaired_audio);

        preferenceScreen.addPreference(speaker);
        preferenceScreen.addPreference(headphone);
        if (util.mConfigManager.getDefault(MenuConfigManager.VISUALLY_SPEAKER) == 0
                && util.mConfigManager.getDefault(MenuConfigManager.VISUALLY_HEADPHONE) == 0) {
               visuallyvolume.setEnabled(false);
            } else {
                visuallyvolume.setEnabled(true);
            }
        preferenceScreen.addPreference(visuallyvolume);
        if (CommonIntegration.isEURegion()) {
            // VISUALLY_PAN_FADE
            preferenceScreen.addPreference(panaAndFade);
            // VISUALLY_IMPAIRED_AUDIO
            preferenceScreen.addPreference(visuallyImpairedAudioInfo);
        }

        return preferenceScreen;
    }
    private PreferenceScreen  addAudioSubvisuallyimpairedAudioSettings(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_INIT, 0);
        int soundListsize = mTV.getConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_TOTAL);
        int viIndex = mTV.getConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_CURRENT);
        Log.d("MENUAudioActivity", "soundListsize: " + soundListsize);
        for (int i = 0; i < soundListsize; i++) {
          String itemName = MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING + "_" + i;
          String soundString = mTV.getConfigString(itemName);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d("MENUAudioActivity", "VisuallyImpaired:" + soundString);
          String[] itemValueStrings = new String[3];
          if (soundString != null) {
            itemValueStrings[0] = soundString;
            itemValueStrings[1] = "";
            itemValueStrings[2] = "";
          } else {
            itemValueStrings[0] = "";
            itemValueStrings[1] = "";
            itemValueStrings[2] = "";
          }
          Preference soundtrackItem;
          if(viIndex == i){
              soundtrackItem = util.createListPreference(itemName,new String("" + (i + 1)) , true, new String[] { soundString },
                      0);
          }else{
              soundtrackItem = util.createListPreference(itemName,new String("" + (i + 1)) , true, new String[] { soundString },
                      1);
          }

          preferenceScreen.addPreference(soundtrackItem);
        }
         return preferenceScreen;
    }
    private PreferenceScreen addAudioSubSoundTracks (PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        MtkTvAVMode navTvAvMode = MtkTvAVMode.getInstance();
        SundryImplement sundry = SundryImplement.getInstanceNavSundryImplement(themedContext);
        List filterTracks = null;
        String currentID = "";
        Preference soundtrackItem = null;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"start Log the audio track info ");
        /*util.mConfigManager.setValue(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_SET_DEINIT,0);
        util.mConfigManager.setValue(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_SET_INIT,0);
    //  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,""+util.mConfigManager.getDefault(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_TOTAL));
        int current = util.mConfigManager.getDefault(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_CURRENT);
        int total = util.mConfigManager.getDefault(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_TOTAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,""+ total);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,""+ current);
        for(int i = 0;i<util.mConfigManager.getDefault(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_TOTAL);i++){
            String s = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_STRING+"_"+i;
            //String s = "soundtracksgetstring_"+i;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"str = " + s);
            String[] s1 = util.mConfigManager.getConfigString(s).split("\\+");
            String lang = s1[0] + " " + s1[1];
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,""+lang);
            String itemKey = MenuConfigManager.SOUNDTRACKS_GET_STRING + "_" + i;
            if( total > 0 && current == i)
            {
                soundtrackItem = util.createListPreference(itemKey, lang, true, new String[] { lang }, 0);
            }else{
                soundtrackItem = util.createListPreference(itemKey, lang, true, new String[] { lang }, 1);
            }

            preferenceScreen.addPreference(soundtrackItem);
        }*/

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"end Log the audio track info ");
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
            List<TvTrackInfo> tracks = null;
            if (CommonIntegration.TV_FOCUS_WIN_MAIN == CommonIntegration.getInstance().getCurrentFocus()) {
                tracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
                if(tracks != null){
                    filterTracks = sundry.filterAudioTracks(tracks);   
                }
                currentID = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            } else {
                tracks = TurnkeyUiMainActivity.getInstance().getPipView().getTracks(TvTrackInfo.TYPE_AUDIO);
                if(tracks != null){
                    filterTracks = sundry.filterAudioTracks(tracks);   
                }
                currentID = TurnkeyUiMainActivity.getInstance().getPipView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            }
        } else {
            List<TvProviderAudioTrackBase> tracks = navTvAvMode.getAudioAvailableRecord();
            if(tracks != null){
                filterTracks = sundry.filterAudioTracksForNav(tracks); 
            }
            if (navTvAvMode.getCurrentAudio() != null) {
                currentID = String.valueOf(navTvAvMode.getCurrentAudio().getAudioId());
            }
        }
        if(filterTracks != null) {
            for (Object object : filterTracks) {
                String itemKey = "";
                String lang = "";
                String trackId = "";
                if(object instanceof TvTrackInfo) {
                    TvTrackInfo tvTrackInfo = (TvTrackInfo)object;
                    trackId = tvTrackInfo.getId();
                    lang = sundry.getAudioTrackLangByIdForSoundTrack(mContext,trackId, filterTracks);
                } else if(object instanceof TvProviderAudioTrackBase) {
                    TvProviderAudioTrackBase tp = (TvProviderAudioTrackBase)object;
                    trackId = String.valueOf(tp.getAudioId());
                    lang = sundry.getAudioTrackLangByIdNoNumbForNav(trackId, filterTracks);
                } else {
                    continue;
                }
                itemKey = MenuConfigManager.SOUNDTRACKS_GET_STRING + "_" + trackId;
                if(currentID != null && currentID.length() > 0 && currentID.equalsIgnoreCase(trackId) ) {
                    soundtrackItem = util.createListPreference(itemKey, lang, true, new String[] { lang }, 0);
                } else {
                    soundtrackItem = util.createListPreference(itemKey, lang, true, new String[] { lang }, 1);
                }
                preferenceScreen.addPreference(soundtrackItem);
            }
        }

        return preferenceScreen;
    }
/*    private PreferenceScreen  addAudioSubSoundTracksSettings(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_INIT, 0);
        int soundListsize = mTV.getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_TOTAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addAudioSubSoundTracksSettings  soundListsize = "+soundListsize);
        int stIndex = mTV.getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_CURRENT);
        for (int i = 0; i < soundListsize; i++) {
          String ItemName = MenuConfigManager.SOUNDTRACKS_GET_STRING + "_" + i;
          String soundString = mTV.getConfigString(ItemName);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ABC soundString:" + soundString);
          String[] itemValueStrings = new String[3];
          itemValueStrings[0] = "" + (i + 1);
          if (soundString != null) {
            String[] temp = soundString.split("\\+");
            if (temp.length >= 2) {
              itemValueStrings[1] = temp[0];
              itemValueStrings[2] = temp[1];

              if ((MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_CH_LST_TYPE) >
                  0) && ("qaa".equalsIgnoreCase(temp[0]) || "und".equalsIgnoreCase(temp[0]))) {
                itemValueStrings[1] = MtkTvCI.getInstance(0).getProfileISO639LangCode();
                if (temp[1].startsWith("QAA")) {
                  itemValueStrings[2] = itemValueStrings[2].replace("QAA",
                      itemValueStrings[1].toUpperCase());
                }
                if (temp[1].startsWith("UND")) {
                  itemValueStrings[2] = itemValueStrings[2].replace("UND",
                      itemValueStrings[1].toUpperCase());
                }
                soundString = itemValueStrings[1] + "+" +
                    itemValueStrings[2];
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ABC soundString:" +
                    soundString);
              }

            } else if (temp.length == 1) {
              itemValueStrings[1] = temp[0];
              itemValueStrings[2] = "";
            } else {
              itemValueStrings[1] = "";
              itemValueStrings[2] = "";
            }

          }
          Preference soundtrackItem;
          if(stIndex==i){
               soundtrackItem = util.createListPreference(ItemName,new String("" + (i + 1)) , true, new String[] { soundString },
                  0);
          }else{
               soundtrackItem = util.createListPreference(ItemName,new String("" + (i + 1)) , true, new String[] { soundString },
                      1);
          }
          preferenceScreen.addPreference(soundtrackItem);
        }
         return preferenceScreen;
    }
*/

    OnPreferenceChangeListener mvisuallyimpairedChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceChange " + preference + "," + preference.getKey() + "," + newValue);
              int tempvalue=-1;
              if (newValue instanceof Boolean) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "instanceof Boolean");
                  tempvalue = ((Boolean) newValue) ? 1 : 0;
              } else if (newValue instanceof String) {
                  tempvalue = Integer.parseInt((String) newValue);
              }
                if (preference.getKey().startsWith(MenuConfigManager.VISUALLY_SPEAKER)) {
                    if(tempvalue == 0){
                        int value=mConfigManager.getDefault(MenuConfigManager.VISUALLY_HEADPHONE);
                        if(value == 0){
                             visuallyvolume.setEnabled(false);
                        }else{
                             visuallyvolume.setEnabled(true);
                        }
                    }else{
                     visuallyvolume.setEnabled(true);
                    }
                    mConfigManager.setValue(preference.getKey(), tempvalue);
                }
                else if(preference.getKey().startsWith(MenuConfigManager.VISUALLY_HEADPHONE)){
                    if(tempvalue == 0){
                        int value=mConfigManager.getDefault(MenuConfigManager.VISUALLY_SPEAKER);
                        if(value == 0){
                             visuallyvolume.setEnabled(false);
                        }else{
                             visuallyvolume.setEnabled(true);
                        }
                    }else{
                     visuallyvolume.setEnabled(true);
                   }
                    mConfigManager.setValue(preference.getKey(), tempvalue);
                }else if(preference.getKey().startsWith(MenuConfigManager.SPDIF_MODE)){

                      if (CommonIntegration.isEURegion()) {
                          if (tempvalue == 2) {
                              audioSpdifDelay.setEnabled(true);
                          } else {
                              audioSpdifDelay.setEnabled(false);
                          }
                      } else if (CommonIntegration.isCNRegion()) {
                          if (tempvalue == 0) {
                              audioSpdifDelay.setEnabled(false);
                          } else {
                              audioSpdifDelay.setEnabled(true);
                          }
                      }
                      mConfigManager.setValue(preference.getKey(), tempvalue);
                }else if(preference.getKey().startsWith(MenuConfigManager.DEMO_PARTITION)){
                     final Context themedContext = mPreferenceManager.getContext();
                     mConfigManager.setValue(preference.getKey(), tempvalue);
                     MenuMjcDemoDialog dialog = new MenuMjcDemoDialog(themedContext);
                     DialogPreference  demoPretemp=( DialogPreference )demoPre;
                     demoPretemp.setDialog(dialog);
                     demoPre=demoPretemp;
               }


         PreferenceData.getInstance(mContext.getApplicationContext())
              .invalidate(preference.getKey(), newValue);
          return true;
        }
      };


    Preference visuallyImpaired = null;
    Preference audioSpdifDelay;
    Preference spdifType ;

    private void addAudioSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addAudioSettings  stsrt>>");


        //Audio Language
        if(isTVSource() && !mTV.isTvInputBlock()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudiolanguage--atv"+CommonIntegration.getInstance().isCurrentSourceATV());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudiolanguage--dtv"+CommonIntegration.getInstance().isCurrentSourceDTV());
            if(CommonIntegration.isUSRegion()) {
                //multi audio
                SundryImplement sundryIm = SundryImplement.getInstanceNavSundryImplement(themedContext);
                String[] multiAudio = themedContext.getResources().getStringArray(R.array.menu_tv_audio_channel_mts_array);
                String key = MenuConfigManager.TV_MTS_MODE;
                int defaultAudio = 0;
                if(!(CommonIntegration.getInstance().isCurrentSourceATV()
                        || (!mTV.iCurrentInputSourceHasSignal()))) {
                    multiAudio = mContext.getResources().getStringArray(R.array.menu_tv_audio_language_array_US_value);
                    key = MenuConfigManager.TV_AUDIO_LANGUAGE;
                    defaultAudio = util.mOsdLanguage.getAudioLanguage(key);

                    List<String> mAudioList = new ArrayList<>();
                    mAudioList = subtitleAudioLanguageTransform("Audio",multiAudio);

                    multiAudio = new String[mAudioList.size()];
                    for (int i=0;i<mAudioList.size();i++){
                        multiAudio[i] = mAudioList.get(i);
                    }

                } else {
                    multiAudio = sundryIm.getAllMtsModes();
                    defaultAudio = getIndexOfMtsMode(multiAudio, sundryIm.getMtsModeString(mTV.getConfigValue(MenuConfigManager.TV_MTS_MODE)));
                    //mConfigManager.getDefault(key);
                }
                Preference multiAudioPref = util.createListPreference(key,R.string.menu_channel_multi_audio,
                        true, multiAudio,defaultAudio);
                mainPreferenceScreen.addPreference(multiAudioPref);

            } else if(CommonIntegration.isEURegion()) {
                SundryImplement sundryIm = SundryImplement.getInstanceNavSundryImplement(themedContext);
                String[] audioLanuage = themedContext.getResources().getStringArray(
                        R.array.menu_tv_audio_language_eu_array_value);
               /* String[] audioLanuage2nd = themedContext.getResources().getStringArray(
                        R.array.menu_tv_audio_language_eu_array);*/
                // audio language
                if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
                    String[] langArray = mContext.getResources().getStringArray(
                            R.array.menu_tv_audio_language_array_PA_value);
                    if(MtkTvConfig.getInstance().getCountry()
                            .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) {
                        langArray = mContext.getResources().getStringArray(
                                R.array.menu_tv_audio_language_array_PA_NZL_value);
                    }

                    List<String> mAudioList = new ArrayList<>();
                    mAudioList = subtitleAudioLanguageTransform("Audio",langArray);

                    langArray = new String[mAudioList.size()];
                    for (int i=0;i<mAudioList.size();i++){
                        langArray[i] = mAudioList.get(i);
                    }

                    // PA Audio language
                    tvFirstLanguageEU = util.createListPreference(
                            MenuConfigManager.TV_AUDIO_LANGUAGE,
                            R.string.menu_tv_audio_language,
                            true,
                            langArray,
                            util.mOsdLanguage.getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE));

                    // audio language 2nd
                    tvSecondLanguageEU = util
                            .createListPreference(
                                    MenuConfigManager.TV_AUDIO_LANGUAGE_2,
                                    R.string.menu_tv_audio_language2nd,
                                    true,
                                    langArray,
                                    util.mOsdLanguage
                                            .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE_2));
                } else  {
                    List<String> mAudioList = new ArrayList<>();
                    mAudioList = subtitleAudioLanguageTransform("Audio",audioLanuage);

                    audioLanuage = new String[mAudioList.size()];
                    for (int i=0;i<mAudioList.size();i++){
                        audioLanuage[i] = mAudioList.get(i);
                    }

                    tvFirstLanguageEU = util.createListPreference(
                            MenuConfigManager.TV_AUDIO_LANGUAGE,
                            R.string.menu_tv_audio_language, true, audioLanuage,
                            util.mOsdLanguage
                                    .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE));

                    // audio language 2nd
                    tvSecondLanguageEU = util
                            .createListPreference(
                                    MenuConfigManager.TV_AUDIO_LANGUAGE_2,
                                    R.string.menu_tv_audio_language2nd,
                                    true,
                                    audioLanuage,
                                    util.mOsdLanguage
                                            .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE_2));
                }
                if (!(CommonIntegration.getInstance().isCurrentSourceATV()
                        || (!mTV.iCurrentInputSourceHasSignal()
                        && !CommonIntegration.getInstance().hasActiveChannel()))) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");//mainPreferenceScreen.removePreference(tvFirstVoice);
                }else {
                    String[] mts = sundryIm.getAllMtsModes();/*themedContext.getResources().getStringArray(
                    R.array.menu_tv_audio_channel_array);*/
                    // audio channel 1
                    int title = R.string.menu_tv_mts;
                    if(mTV.isCOLRegion()){
                        title = R.string.menu_tv_mts_us;
                    }
                    tvFirstVoice = util.createListPreference(MenuConfigManager.TV_MTS_MODE,
                            title, true, mts, getIndexOfMtsMode(mts, sundryIm.getMtsModeString(mTV.getConfigValue(MenuConfigManager.TV_MTS_MODE))));
                    mainPreferenceScreen.addPreference(tvFirstVoice);
                }
                mainPreferenceScreen.addPreference(tvFirstLanguageEU);
                mainPreferenceScreen.addPreference(tvSecondLanguageEU);
                if (!mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_LANGUAGE_ATTR)) {
                    mainPreferenceScreen.removePreference(tvFirstLanguageEU);
                    mainPreferenceScreen.removePreference(tvSecondLanguageEU);
                }
            } else if(CommonIntegration.isSARegion()) {
                if(CommonIntegration.getInstance().isCurrentSourceATV()
                        || (!mTV.iCurrentInputSourceHasSignal()
                        && !CommonIntegration.getInstance().hasActiveChannel())){
                    /*final String[] mts = themedContext.getResources().getStringArray(
                        R.array.menu_tv_audio_channel_mts_array);*/
                    SundryImplement sundryIm = SundryImplement.getInstanceNavSundryImplement(themedContext);
                    String[] mts = sundryIm.getAllMtsModes();
                    // Audio Channel 1
                    Preference tvFirstVoice =
                            util.createListPreference(MenuConfigManager.TV_MTS_MODE,
                                    R.string.menu_tv_mts_us, true, mts,
                                    getIndexOfMtsMode(mts, sundryIm.getMtsModeString(mTV.getConfigValue(MenuConfigManager.TV_MTS_MODE))));
                    /*util.mConfigManager.getDefault(MenuConfigManager.TV_MTS_MODE));*/
                    mainPreferenceScreen.addPreference(tvFirstVoice);
                }else {
                    String[] audioLang = themedContext.getResources().getStringArray(R.array.menu_tv_audio_language_array_SA_value);

                    List<String> mAudioList = new ArrayList<>();
                    mAudioList = subtitleAudioLanguageTransform("Audio",audioLang);

                    audioLang = new String[mAudioList.size()];
                    for (int i=0;i<mAudioList.size();i++){
                        audioLang[i] = mAudioList.get(i);
                    }

                    mainPreferenceScreen.addPreference(util.createListPreference(
                            MenuConfigManager.TV_AUDIO_LANGUAGE,
                            R.string.menu_tv_audio_language, true, audioLang,
                            util.mOsdLanguage
                                    .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE)));
                }
            }else if(CommonIntegration.isCNRegion()){
                // freeze switch
                Preference freezeSwitch = util.createSwitchPreference(MenuConfigManager.CHANNEL_FREEZESWITCH,
                        R.string.menu_channel_freeze_switch,
                        mTV.getConfigValue(MenuConfigManager.CHANNEL_FREEZESWITCH) == 1 ? true : false);
                mainPreferenceScreen.addPreference(freezeSwitch);

                //audio language
                SundryImplement sundryIm = SundryImplement.getInstanceNavSundryImplement(themedContext);
                String[] audioLang = themedContext.getResources().getStringArray(R.array.menu_tv_audio_language_array_CN);
                String[] audioLang2nd = themedContext.getResources().getStringArray(R.array.menu_tv_audio_language_array_CN);
                String key = MenuConfigManager.TV_AUDIO_LANGUAGE;
                int defaultAudio = 0;
                int title = R.string.menu_tv_audio_language;
                Preference audioLang2ndPre = null;
                if(!(CommonIntegration.getInstance().isCurrentSourceATVforEuPA())) {
                    audioLang = mContext.getResources().getStringArray(R.array.menu_tv_audio_language_array_CN);
                    key = MenuConfigManager.TV_AUDIO_LANGUAGE;
                    defaultAudio = util.mOsdLanguage.getAudioLanguage(key);

                    // audio language 2nd
                    audioLang2ndPre = util
                            .createListPreference(
                                    MenuConfigManager.TV_AUDIO_LANGUAGE_2,
                                    R.string.menu_tv_audio_language2nd,
                                    true,
                                    audioLang2nd,
                                    util.mOsdLanguage
                                            .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE_2));
                } else {
                    key = MenuConfigManager.TV_MTS_MODE;
                    title = R.string.menu_tv_mts;
                    audioLang = sundryIm.getAllMtsModes();
                    defaultAudio = getIndexOfMtsMode(audioLang, sundryIm.getMtsModeString(mTV.getConfigValue(MenuConfigManager.TV_MTS_MODE)));
                }
                Preference multiAudioPref = util.createListPreference(key,title,
                        true, audioLang,defaultAudio);
                mainPreferenceScreen.addPreference(multiAudioPref);
                if(audioLang2ndPre != null) {
                    mainPreferenceScreen.addPreference(audioLang2ndPre);
                }
            }
        }

        // for EU
        if(false == DataSeparaterUtil.getInstance().isAtvOnly()) {
            Preference soundtrack = util.createFragmentPreference(
                    MenuConfigManager.SOUND_TRACKS,
                    R.string.menu_audio_sound_tracks,
                    true,
                    TrackListFragment.class.getName());
           /* Preference soundtrack = util.createPreference(MenuConfigManager.SOUND_TRACKS,
                                                                R.string.menu_audio_sound_tracks);*/
            preferenceScreen.addPreference(soundtrack);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceDTV : "+mTV.isCurrentSourceDTV());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFunctionSupport : "+MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SET_AUDIO_TRACK_SUPP));
            if(CommonIntegration.isEURegion() &&
                    MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SET_AUDIO_TRACK_SUPP) &&
                    mTV.isCurrentSourceDTV()){
                soundtrack.setVisible(true);
//                category.setVisible(true);
            }else{
                soundtrack.setVisible(false);
//                category.setVisible(false);
            }
        }

    }

    PreferenceScreen addAudioSubViSettings(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // speaker
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.VISUALLY_SPEAKER, R.string.menu_audio_visually_speaker, true));

        // headphone
        preferenceScreen
                .addPreference(util.createSwitchPreference(
                        MenuConfigManager.VISUALLY_HEADPHONE,
                        R.string.menu_audio_visually_headphone, true));

        return preferenceScreen;
    }

    void addTvSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        final String[] tunerModeStr = mContext.getResources().
                getStringArray(R.array.menu_tv_tuner_mode_array);
        String[] mts = mContext.getResources().getStringArray(
                R.array.menu_tv_audio_channel_mts_array);
        String[] audioLanuage = mContext.getResources().getStringArray(
                R.array.menu_tv_audio_language_array);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addTvSettings  ");
        // TUNER MODE
        Preference tvTunerMode = util.createListPreference(MenuConfigManager.TUNER_MODE,
            R.string.menu_tv_tuner_mode, true, tunerModeStr,
            util.mConfigManager.getDefault(MenuConfigManager.TUNER_MODE));
        if (mTV.isTshitRunning() || mTV.isPVrRunning()) {
          tvTunerMode.setEnabled(false);
        } else {
          tvTunerMode.setEnabled(true);
        }
        preferenceScreen.addPreference(tvTunerMode);

        if (CommonIntegration.isUSRegion()) {
            // US channel scan
            Intent intentScanD = new Intent(mContext, ScanDialogActivity.class);
            intentScanD.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN);
            Preference channelScan = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN,
                R.string.menu_tv_channel_scan,
                intentScanD);
            if (mTV.isTshitRunning() || mTV.isPVrRunning()) {
              channelScan.setEnabled(false);
            } else {
              channelScan.setEnabled(true);
            }
            preferenceScreen.addPreference(channelScan);

            // US channel skip
            Intent intentChannelSkip = new Intent(mContext, ChannelInfoActivity.class);
            String mItem = MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST;
            TransItem trans = new TransItem(mItem, "",
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE);
            intentChannelSkip.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SKIP);
            intentChannelSkip.putExtra("TransItem", trans);
            channelskip = util.createPreference(MenuConfigManager.TV_CHANNEL_SKIP,
                R.string.menu_tv_channel_skip,
                intentChannelSkip);
            if (mTV.isCurrentSourceTv()
                && (CommonIntegration.getInstance().hasActiveChannel())
                && !mTV.isTshitRunning() && !mTV.isPVrRunning()) {
              channelskip.setEnabled(true);
            } else {
              channelskip.setEnabled(false);
            }
            preferenceScreen.addPreference(channelskip);
            // US MTS
            preferenceScreen.addPreference(
                    util.createListPreference(MenuConfigManager.TV_MTS_MODE,
                            R.string.menu_tv_mts_us, true, mts,
                            util.mConfigManager.getDefault(MenuConfigManager.TV_MTS_MODE)));

            // US Audio language
            preferenceScreen
                    .addPreference(
                    util.createListPreference(MenuConfigManager.TV_AUDIO_LANGUAGE,
                            R.string.menu_tv_audio_language, true, audioLanuage,
                            util.mOsdLanguage.getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE)));
        }
    }

    private PreferenceScreen addCNChannel(PreferenceScreen preferenceScreen,
            Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        Intent mScanIntent;
        if (DVBCCNScanner.mQuichScanSwitch && mTV.isCurrentSourceDTV()
                && CommonIntegration.getInstance().getTunerMode() == CommonIntegration.DB_CAB_OPTID) {
              // initDVBCCNQuickFullItem(channelScan);
              mScanIntent = new Intent(mContext, ScanViewActivity.class);
              mScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN);
            } else if (mTV.isCurrentSourceATV()) {
                mScanIntent = new Intent(mContext, ScanDialogActivity.class);
                mScanIntent.putExtra("ActionID", MenuConfigManager.TV_SYSTEM);
                mScanIntent.putExtra("ActionParentID", MenuConfigManager.TV_CHANNEL_SCAN);
            } else {
                mScanIntent = new Intent(mContext, ScanDialogActivity.class);
                mScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN);
            }
        //channelScan
        Preference channelScan = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN,
                mContext.getString(R.string.menu_tv_channel_scan), mScanIntent);
        preferenceScreen.addPreference(channelScan);

        //updateScan
        ItemName = MenuConfigManager.TV_UPDATE_SCAN;
        if (mTV.isCurrentSourceATV()) {
            mScanIntent = new Intent(mContext, ScanDialogActivity.class);
            mScanIntent.putExtra("ActionID", MenuConfigManager.TV_SYSTEM);
            mScanIntent.putExtra("ActionParentID", MenuConfigManager.TV_UPDATE_SCAN);
        }
        updateScan = util.createPreference(ItemName,
                mContext.getString(R.string.menu_tv_update_scan), mScanIntent);

        //analogScan
        ItemName = MenuConfigManager.TV_ANALOG_SCAN;
        if (mTV.isCurrentSourceATV()) {
            mScanIntent = new Intent(mContext, ScanViewActivity.class);
            mScanIntent.putExtra("ActionID", ItemName);
          }
        Preference analogScan = util.createPreference(ItemName,
                mContext.getString(R.string.menu_tv_analog_manual_scan), mScanIntent);

        //RFScan
        ItemName = MenuConfigManager.TV_SINGLE_RF_SCAN_CN;
        if (mTV.isCurrentSourceDTV()) {
            mScanIntent = new Intent(mContext, ScanViewActivity.class);
            mScanIntent.putExtra("ActionID", ItemName);
          }
        Preference rfScan = util.createPreference(ItemName,
                mContext.getString(R.string.menu_tv_single_rf_scan), mScanIntent);

        String[] freezeChangeCh = themedContext.getResources().getStringArray(R.array.menu_tv_freeze_channel_array);
        Preference freezeChannel = util.createListPreference(MenuConfigManager.TV_FREEZE_CHANNEL,
                themedContext.getString(R.string.menu_tv_freeze_channel), true,
                freezeChangeCh, mConfigManager.getDefault(MenuConfigManager.TV_FREEZE_CHANNEL));

        //ChannelEdit
        ItemName = MenuConfigManager.TV_CHANNEL_EDIT;
        mScanIntent = new Intent(mContext, CNChannelInfoActivity.class);
        TransItem transAntenaEdit = new TransItem(MenuConfigManager.TV_CHANNEL_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        mScanIntent.putExtra("TransItem", transAntenaEdit);
        mScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_EDIT);
        channelEdit = util.createPreference(ItemName,
                mContext.getString(R.string.menu_tv_channel_edit), mScanIntent);

        //clean List
      //  initCleanDialog();
        cleanList = util.createClickPreference(MenuConfigManager.TV_CHANNEL_CLEAR,
                mContext.getString(R.string.menu_tv_clear_channel_list),mFacSetup);
        if (!CommonIntegration.getInstance().isCurrentSourceDTV()) {
            preferenceScreen.addPreference(updateScan);
            preferenceScreen.addPreference(analogScan);
            preferenceScreen.addPreference(freezeChannel);
        } else {
              preferenceScreen.addPreference(rfScan);
          }
        preferenceScreen.addPreference(channelEdit);
        preferenceScreen.addPreference(cleanList);
        if (mNavIntegration.hasActiveChannel()) {
            channelEdit.setEnabled(true);
            cleanList.setEnabled(true);
        }else {
            channelEdit.setEnabled(false);
            cleanList.setEnabled(false);
        }


        return preferenceScreen;
    }

    private PreferenceScreen addSAChannel(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        final String[] tunerModeStr = mContext.getResources().
                getStringArray(R.array.menu_tv_tuner_mode_array);
        final String[] mts = mContext.getResources().getStringArray(
                R.array.menu_tv_audio_channel_mts_array);
        boolean isPVRSrt = mTV.isPVrRunning();

        // Channel Scan 2
        Intent sAChannelScanIntent = new Intent(mContext, ScanDialogActivity.class);
        sAChannelScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN);
        Preference channelScan = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN,
                R.string.menu_tv_channel_scan, sAChannelScanIntent);
        preferenceScreen.addPreference(channelScan);

        // update scan 2
        if(DataSeparaterUtil.getInstance().isAtvOnly()) {
            //skip
            updateScan = null;
        }
        else {
            Intent sAUpdateScanIntent = new Intent(mContext, ScanDialogActivity.class);
            sAUpdateScanIntent.putExtra("ActionID", MenuConfigManager.TV_UPDATE_SCAN);
            updateScan = util.createPreference(MenuConfigManager.TV_UPDATE_SCAN,
                    R.string.menu_tv_update_scan, sAUpdateScanIntent);
            preferenceScreen.addPreference(updateScan);
        }

        // channel skip 2
        Intent skipSAIntent = new Intent(mContext, ChannelInfoActivity.class);
        TransItem skipSATrans = new TransItem(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST,
                "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        skipSAIntent.putExtra("TransItem", skipSATrans);
        skipSAIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SKIP);
        channelskip = util.createPreference(MenuConfigManager.TV_CHANNEL_SKIP,
                R.string.menu_tv_channel_skip, skipSAIntent);
        preferenceScreen.addPreference(channelskip);

        // channel edit 2
        Intent editSAIntent = new Intent(mContext, ChannelInfoActivity.class);
        TransItem editSATrans = new TransItem(MenuConfigManager.TV_CHANNEL_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        editSAIntent.putExtra("TransItem", editSATrans);
        editSAIntent.putExtra("ActionID", MenuConfigManager.TV_SA_CHANNEL_EDIT);
        saChannelEdit = util.createPreference(MenuConfigManager.TV_SA_CHANNEL_EDIT,
                R.string.menu_tv_channel_edit, editSAIntent);
        preferenceScreen.addPreference(saChannelEdit);

        // channel fine
        Intent fineSAIntent = new Intent(mContext, ChannelInfoActivity.class);
        TransItem fineSATrans = new TransItem(MenuConfigManager.TV_CHANNELFINE_TUNE_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        fineSAIntent.putExtra("TransItem", fineSATrans);
        fineSAIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNELFINE_TUNE);
        saChannelFine = util.createPreference(MenuConfigManager.TV_CHANNELFINE_TUNE,
                R.string.menu_tv_channelfine_tune, fineSAIntent);
        preferenceScreen.addPreference(saChannelFine);

        if (isPVRSrt || mTV.isTshitRunning()) {
            channelScan.setEnabled(false);
            if(updateScan != null){
                updateScan.setEnabled(false);
            }
            channelskip.setEnabled(false);
            saChannelEdit.setEnabled(false);
            saChannelFine.setEnabled(false);
        } else {
            channelScan.setEnabled(true);
            if(updateScan != null){
                updateScan.setEnabled(true);
            }
            boolean isEnabled = mNavIntegration.hasActiveChannel();
            if(isEnabled && !CommonIntegration.getInstance().is3rdTVSource()){
                MtkTvChannelInfoBase mtktvChannelinfo = mNavIntegration.getCurChInfoByTIF();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSARegion,isEnabled,hasActivtChannel "+isEnabled);
                if(mtktvChannelinfo != null
    					&& mtktvChannelinfo.isAnalogService()
    					&& !TIFFunctionUtil
    							.checkChMask(mtktvChannelinfo, TIFFunctionUtil.CH_FAKE_MASK,
    									TIFFunctionUtil.CH_FAKE_VAL)){
                    saChannelFine.setEnabled(true);
                }else {
                    saChannelFine.setEnabled(false);
                }
            }else{
                saChannelFine.setEnabled(false);
            }
        }
        bindChannelPreference();
        mDataHelper.changePreferenceEnable();
        return preferenceScreen;
    }

    void addCNTvSettings(PreferenceScreen preferenceScreen,
            Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        String[] freezeChangeCh = themedContext.getResources().getStringArray(R.array.menu_tv_freeze_channel_array);
//        String[] tshiftOption = themedContext.getResources().getStringArray(R.array.menu_tv_tshift_config_array);
        String[] scanMode = themedContext.getResources().getStringArray(R.array.menu_tv_scan_mode_array);
        final String[] tunerModeStr = themedContext.getResources().getStringArray(R.array.menu_tv_tuner_mode_array);
        ItemName = MenuConfigManager.TUNER_MODE;
        Preference tvTunerMode = util.createListPreference(ItemName,
                themedContext.getString(R.string.menu_tv_tuner_mode), true, tunerModeStr,
                mConfigManager.getDefault(MenuConfigManager.TUNER_MODE));
        if (mTV.isPVrRunning() || mTV.isTshitRunning()) {
            tvTunerMode.setEnabled(false);
          } else {
            tvTunerMode.setEnabled(true);
          }
        if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
            preferenceScreen.addPreference(tvTunerMode);
        }
        ItemName = MenuConfigManager.TV_CHANNEL;
        Preference tvChannel = util.createPreference(ItemName,  themedContext
                .getString(R.string.menu_tv_channels));
        preferenceScreen.addPreference(tvChannel);

        ItemName = MenuConfigManager.TV_FREEZE_CHANNEL;
        Preference freezeChannel = util.createListPreference(ItemName,
                themedContext.getString(R.string.menu_tv_freeze_channel), true,
                freezeChangeCh, mConfigManager.getDefault(ItemName));
        if (CommonIntegration.getInstance().isCurrentSourceATV()) {
            preferenceScreen.addPreference(freezeChannel);
          }
//        ItemName = MenuConfigManager.DTV_TSHIFT_OPTION;
//        Preference tshiftoption = util.createListPreference(ItemName, themedContext.getString(R.string.menu_tv_tshift_config),
//                true, tshiftOption, mConfigManager.getValueFromPrefer(ItemName));
        ItemName = MenuConfigManager.DTV_DEVICE_INFO;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "show CN device info");
        DiskSettingDialog dialog = new DiskSettingDialog(themedContext, R.layout.pvr_timeshfit_deviceinfo);
        Preference deviceInfo = util.createDialogPreference(ItemName, themedContext.getString(R.string.menu_setup_device_info), dialog);
        if(mTV.isPVrRunning() || mTV.isTshitRunning()){
            deviceInfo.setEnabled(false);
        }
        if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
          // Disable tshift feature on CN.
//          if (false) {// TimeShiftManager.enableTshiftModule()
//            preferenceScreen.addPreference(tshiftoption);
//          }
          preferenceScreen.addPreference(deviceInfo);
        }
    }

    void addusSATvSettings(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        final String[] tunerModeStr = themedContext.getResources().
                getStringArray(R.array.menu_tv_tuner_mode_array);
        final String[] mts = themedContext.getResources().getStringArray(
                R.array.menu_tv_audio_channel_mts_array);
        boolean isPVRSrt = mTV.isPVrRunning();
        // TUNER MODE
        Preference tvTunerMode =
                util.createListPreference(MenuConfigManager.TUNER_MODE,
                        R.string.menu_tv_tuner_mode, true, tunerModeStr,
                        util.mConfigManager.getDefault(MenuConfigManager.TUNER_MODE));
        preferenceScreen.addPreference(tvTunerMode);
        // Audio Channel 1
        Preference tvFirstVoice =
                util.createListPreference(MenuConfigManager.TV_MTS_MODE,
                        R.string.menu_tv_mts_us, true, mts,
                        util.mConfigManager.getDefault(MenuConfigManager.TV_MTS_MODE));
        preferenceScreen.addPreference(tvFirstVoice);

        if (CommonIntegration.isUSRegion()) {
            String[] audioLanuage = themedContext.getResources().getStringArray(
                    R.array.menu_tv_audio_language_array);
            // Channel Scan
            Intent uSChannelScanIntent = new Intent(themedContext, ScanDialogActivity.class);
            uSChannelScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN);
            Preference channelScan = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN,
                    R.string.menu_tv_channel_scan,
                    uSChannelScanIntent);
            preferenceScreen.addPreference(channelScan);
            if (isPVRSrt || mTV.isTshitRunning()) {
                channelScan.setEnabled(false);
            } else {
                channelScan.setEnabled(true);
            }
            // Audio Language
            Preference tvFirstLanguage = util.createListPreference(
                    MenuConfigManager.TV_AUDIO_LANGUAGE,
                    R.string.menu_tv_audio_language, true, audioLanuage,
                    util.mOsdLanguage.getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE));
            preferenceScreen.addPreference(tvFirstLanguage);

            // channel skip 2
            Intent skipIntent = new Intent(themedContext, ChannelInfoActivity.class);
            TransItem trans = new TransItem(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST, "",
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE);
            skipIntent.putExtra("TransItem", trans);
            skipIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SKIP);
            channelskip = util.createPreference(MenuConfigManager.TV_CHANNEL_SKIP,
                    R.string.menu_tv_channel_skip, skipIntent);

            if (isPVRSrt || mTV.isTshitRunning()) {
                tvTunerMode.setEnabled(false);
                channelScan.setEnabled(false);
                channelskip.setEnabled(false);
            } else {
                tvTunerMode.setEnabled(true);
                channelScan.setEnabled(true);
            }
            preferenceScreen.addPreference(channelskip);

        }
        if (CommonIntegration.isSARegion()) {
            // Channel 1
            Preference tvChannel = util.createPreference(MenuConfigManager.TV_CHANNEL,
                    R.string.menu_tv_channel_setup);
            preferenceScreen.addPreference(tvChannel);
            if (isPVRSrt || mTV.isTshitRunning()) {
                tvChannel.setEnabled(false);
            } else {
                tvChannel.setEnabled(true);
            }
            if (isPVRSrt || mTV.isTshitRunning()) {
                tvTunerMode.setEnabled(false);
            } else {
                tvTunerMode.setEnabled(true);
            }
            bindChannelPreference();
            mDataHelper.changePreferenceEnable();

        }
    }

    private String[] tunerModeStrEu;
    public static Preference tvFirstVoice;
 // audio language
    public static Preference tvFirstLanguageEU;
    // audio language 2nd
    public static Preference tvSecondLanguageEU;
    void addEUTvSettings(PreferenceScreen preferenceScreen,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        String[] tunerModeStr = themedContext.getResources().getStringArray(
                R.array.menu_tv_tuner_mode_array);
        if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS)
                || MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
            tunerModeStrEu = tunerModeStr;
        } else if (ScanContent.getDVBSOperatorList(mContext).size() == 0) {
            tunerModeStrEu = themedContext.getResources().getStringArray(
                    R.array.menu_tv_tuner_mode_array_full_eu_sat_only);
        } else {
            tunerModeStrEu = themedContext.getResources().getStringArray(
                    R.array.menu_tv_tuner_mode_array_full_eu);
        }
        String[] mts = themedContext.getResources().getStringArray(
                R.array.menu_tv_audio_channel_array);
        String[] audioLanuage = themedContext.getResources().getStringArray(
                R.array.menu_tv_audio_language_eu_array);
        String[] audioLanuage2nd = themedContext.getResources().getStringArray(
                R.array.menu_tv_audio_language_eu_array);
        int tunerMode = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);
        // tuner mode EU
        Preference tvTunerModeEU = util.createListPreference(MenuConfigManager.TUNER_MODE,
                R.string.menu_tv_tuner_mode, true, tunerModeStrEu,
                tunerMode);
        // audio channel 1
          tvFirstVoice = util.createListPreference(MenuConfigManager.TV_MTS_MODE,
                R.string.menu_tv_mts, true, mts,
                util.mConfigManager.getDefault(MenuConfigManager.TV_MTS_MODE));
        // COUNTRY_REGION_ID
        Preference tvCountryRegion = util.createListPreference(MenuConfigManager.COUNTRY_REGION_ID,
                R.string.menu_tv_country_region_id, true, themedContext.getResources()
                        .getStringArray(R.array.menu_tv_country_region_id_eu),
                util.mConfigManager.getDefault(MenuConfigManager.COUNTRY_REGION_ID));

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA)
                && mTV.isAusCountry()) {
            tvCountryRegion = util.createListPreference(MenuConfigManager.COUNTRY_REGION_ID,
                    R.string.menu_tv_oceania_country_region, true,
                    themedContext.getResources().getStringArray(R.array.menu_tv_oceania_country_region),
                    util.mConfigManager.getDefault(MenuConfigManager.COUNTRY_REGION_ID));
        }

        // audio language
        if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
            String[] langArray = mContext.getResources().getStringArray(
                    R.array.menu_tv_audio_language_array_PA);
            if(MtkTvConfig.getInstance().getCountry()
                    .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) {
                langArray = mContext.getResources().getStringArray(
                        R.array.menu_tv_audio_language_array_PA_NZL);
            }
            // PA Audio language
            tvFirstLanguageEU = util.createListPreference(
                    MenuConfigManager.TV_AUDIO_LANGUAGE,
                    R.string.menu_tv_audio_language,
                    true,
                    langArray,
                    util.mOsdLanguage.getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE));

            // audio language 2nd
            tvSecondLanguageEU = util
                    .createListPreference(
                            MenuConfigManager.TV_AUDIO_LANGUAGE_2,
                            R.string.menu_tv_audio_language2nd,
                            true,
                            langArray,
                            util.mOsdLanguage
                            .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE_2));
        } else  {
            tvFirstLanguageEU = util.createListPreference(
                    MenuConfigManager.TV_AUDIO_LANGUAGE,
                    R.string.menu_tv_audio_language, true, audioLanuage,
                    util.mOsdLanguage
                    .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE));

            // audio language 2nd
            tvSecondLanguageEU = util
                    .createListPreference(
                            MenuConfigManager.TV_AUDIO_LANGUAGE_2,
                            R.string.menu_tv_audio_language2nd,
                            true,
                            audioLanuage2nd,
                            util.mOsdLanguage
                            .getAudioLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE_2));
        }

        // TV Channel
        Preference tvChannel = util.createPreference(MenuConfigManager.TV_EU_CHANNEL,
                R.string.menu_tv_channels);
        if (MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0
                && CommonIntegration.getInstance().isCIEnabled()) {
            tvTunerModeEU.setEnabled(false);
        } else {
            tvTunerModeEU.setEnabled(true);
        }
        if (mTV.isPVrRunning() || mTV.isTshitRunning()) {
            tvTunerModeEU.setEnabled(false);
            tvChannel.setEnabled(false);
        } else {
            tvTunerModeEU.setEnabled(true);
            tvChannel.setEnabled(true);
        }
        preferenceScreen.addPreference(tvTunerModeEU);

        if (mTV.isShowCountryRegion()) {
            preferenceScreen.addPreference(tvCountryRegion);
        }
        preferenceScreen.addPreference(tvFirstVoice);
        if (!(CommonIntegration.getInstance().isCurrentSourceATV()
                || (!mTV.iCurrentInputSourceHasSignal()
                && !CommonIntegration.getInstance().hasActiveChannel()))) {
            preferenceScreen.removePreference(tvFirstVoice);
        }
        preferenceScreen.addPreference(tvFirstLanguageEU);
        preferenceScreen.addPreference(tvSecondLanguageEU);
        if (!mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_LANGUAGE_ATTR)) {
              preferenceScreen.removePreference(tvFirstLanguageEU);
              preferenceScreen.removePreference(tvSecondLanguageEU);
        }
        String[] channelListType = themedContext.getResources().getStringArray(
                R.array.menu_tv_channel_listtype);
        tvChannelListType = util.createListPreference(
                MenuConfigManager.CHANNEL_LIST_TYPE,
                R.string.menu_tv_channel_list_type, true, channelListType,
                util.mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE));

        boolean isCLTypeShow = mTV.isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE);
        preferenceScreen.addPreference(tvChannelListType);
        if (!isCLTypeShow) {
        	tvChannelListType.setVisible(false);
        }
        String profileName = MtkTvConfig.getInstance().
                getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName " +profileName);
        // has profile name && select cam list type
        if (!TextUtils.isEmpty(profileName)) {
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName " +profileName);
            channelListType[1]= profileName;
            tvChannelListType.setSummary(profileName);
        }
        preferenceScreen.addPreference(tvChannel);
        //dual tuner
        Preference tvDualTuner=util.createSwitchPreference(
                MenuConfigManager.TV_DUAL_TUNER,
                        R.string.menu_tv_dual_tuner,
                        util.mConfigManager.getDefault(MenuConfigManager.TV_DUAL_TUNER)
                            == 0 ? false : true);

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT)
                && !CommonIntegration.getInstance().isPipOrPopState()) {
            preferenceScreen.addPreference(tvDualTuner);
        } else {
            preferenceScreen.removePreference(tvDualTuner);
        }

        bindChannelPreference();
        mDataHelper.changePreferenceEnable();

    }

    void initCleanDialog() {
        String info = mContext.getString(R.string.menu_tv_clear_channel_info);
        cleanDialog = new LiveTVDialog(mContext, "", info, 3);
        cleanDialog.setButtonYesName(mContext.getString(R.string.menu_ok));
        cleanDialog.setButtonNoName(mContext.getString(R.string.menu_cancel));
        OnKeyListener listener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        if (v.getId() == cleanDialog.getButtonYes().getId()) {
                            EditChannel.getInstance(mContext).cleanChannelList();
                            cleanDialog.dismiss();
                            // if has install profile reset to broadcast
                            if (mTV.getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0) {
                                mTV.setConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE, 0);
                            }
                            if(mNavIntegration.getCurrentFocus().equals("sub") && (mNavIntegration.isCurrentSourceATV() || mNavIntegration.isCurrentSourceDTV())){
                              InputSourceManager.getInstance().stopPipSession();
                            }else if(mNavIntegration.getCurrentFocus().equals("main") && (mNavIntegration.isCurrentSourceATV() || mNavIntegration.isCurrentSourceDTV())){
                                InputSourceManager.getInstance().stopSession();
                            }
                            changePreferenceEnable();

                            // TODO:update view
                        } else if (v.getId() == cleanDialog.getButtonNo().getId()) {
                            cleanDialog.dismiss();
                        }
                        return true;
                    }
                }
                return false;
            }
        };
        cleanDialog.bindKeyListener(listener);
        // cleanDialog.show();
        // cleanDialog.getButtonNo().requestFocus();
    }
    
    private LiveTVDialog getDvbsUpdateScanDialog(MtkTvScanDvbsBase.M7OneOPT oneOPT, String optName) {
        String info = mContext.getString(R.string.dvbs_fast_update_scan_msg);
        LiveTVDialog dialog = new LiveTVDialog(mContext, "", info, 3);
        dialog.setButtonYesName(mContext.getString(android.R.string.yes));
        dialog.setButtonNoName(mContext.getString(android.R.string.no));
        OnKeyListener listener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        if (v.getId() == dialog.getButtonYes().getId()) {
                            List<SatelliteInfo> allSats = ScanContent.getDVBSsatellites(mContext);
                            List<SatelliteInfo> enabledSats = ScanContent.getDVBSEnablesatellites(mContext, allSats);
                            int satid = allSats.get(0).getSatlRecId();
                            if(ScanContent.checkFastScanOrbitsError(oneOPT, optName, enabledSats)){
                                TVContent.getInstance(mContext).clearCurrentSvlChannelDB();
                                new MtkTvScanDvbsBase().dvbsM7SelPrefOpt(oneOPT.optNWId, oneOPT.optSLId);
                                SaveValue.writeWorldStringValue(mContext, "FAST_SCAN_SELECTED_OPT", oneOPT.optName, true);
                                ScanContent.getDvbsFastScanOrbitErrorDialog(mContext, oneOPT, optName).show();
                            }else {
                                TVContent.getInstance(mContext).clearCurrentSvlChannelDB();
                                new MtkTvScanDvbsBase().dvbsM7SelPrefOpt(oneOPT.optNWId, oneOPT.optSLId);
                                SaveValue.writeWorldStringValue(mContext, "FAST_SCAN_SELECTED_OPT", oneOPT.optName, true);

                                Intent intent = new Intent(mContext, ScanViewActivity.class);
                                intent.putExtra("ActionID", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
                                intent.putExtra("SatID", satid);
                                intent.putExtra("LocationID", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
                                intent.putExtra("SpclRegnSetup", oneOPT.spclRegnSetup);
                                intent.putExtra("SelectedOpName", oneOPT.subOptName);
                                mContext.startActivity(intent);
                            }
                            dialog.dismiss();
                        }
                        return true;
                    }else if(keyCode == KeyEvent.KEYCODE_BACK){
                        dialog.dismiss();
                        return true;
                    }
                }
                return false;
            }
        };
        dialog.bindKeyListener(listener);
        return dialog;
    }

    private LiveTVDialog getDvbcAutoScanDialog() {
        String info = mContext.getString(R.string.confirm_rescan_dialog_title);
        LiveTVDialog dialog = new LiveTVDialog(mContext, "", info, 3);
        dialog.contentTextSize = 14;
        dialog.setButtonYesName(mContext.getString(R.string.menu_cancel));
        dialog.setButtonNoName(mContext.getString(R.string.setup_next));
        OnKeyListener listener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        if (v.getId() == dialog.getButtonYes().getId()) {//cancel
                            dialog.dismiss();
                            ((TvSettingsActivity)mContext).setActionId(MenuConfigManager.TV_EU_CHANNEL);
                            ((LiveTvSetting)mContext).show();
                        } else if (v.getId() == dialog.getButtonNo().getId()) {//next
                            ((TvSettingsActivity)mContext).setActionId(MenuConfigManager.TV_CHANNEL_SCAN_DVBC);
                            ((LiveTvSetting)mContext).show();
                            dialog.dismiss();
                        }
                        return true;
                    }else if(keyCode == KeyEvent.KEYCODE_BACK){
                        ((TvSettingsActivity)mContext).setActionId(MenuConfigManager.TV_EU_CHANNEL);
                        ((LiveTvSetting)mContext).show();
                        dialog.dismiss();
                        return true;
                    }
                }
                return false;
            }
        };
        dialog.bindKeyListener(listener);
        return dialog;
    }

    public void changePreferenceEnable(){

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changePreferenceEnable() start changeEnable");
       /* Preference channelskip = mChannelPreferenceMap.get("channelskip");
        Preference channelSort = mChannelPreferenceMap.get("channelSort");
        Preference channelEdit = mChannelPreferenceMap.get("channelEdit");
        Preference cleanList = mChannelPreferenceMap.get("cleanList");
        Preference saChannelEdit = mChannelPreferenceMap.get("saChannelEdit");
        Preference saChannelFine = mChannelPreferenceMap.get("saChannelFine");*/
        if (CommonIntegration.isCNRegion()) {
          if (mNavIntegration.hasActiveChannel()) {
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
          if (mNavIntegration.hasActiveChannel()) {
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

              cleanList.setEnabled(true);
            }
            mDataHelper.setSkipSortEditItemHid(channelskip, channelSort, channelEdit);
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
//          if (!LiveApplication.isFromLiveTV && saChannelFine != null && saChannelFine.isEnabled()) {
//            saChannelFine.setEnabled(false);
//          }
        //need to fix
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end changeEnable set success");

    }


    public PreferenceScreen loadDataTvAntenna(PreferenceScreen preferenceCategory,
            Context themedContext) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadDataTvAntenna");
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        // ChannelScan
        Intent euAntennaChannelScanIntent = new Intent(mContext, ScanDialogActivity.class);
        euAntennaChannelScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBT);
        Preference channelScan = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN_DVBT,
                R.string.menu_tv_channel_scan,euAntennaChannelScanIntent);
        preferenceCategory.addPreference(channelScan);

        // UpdateScan
        Intent euAntennaUpdateScanIntent = new Intent(mContext, ScanDialogActivity.class);
        euAntennaUpdateScanIntent
                .putExtra("ActionID", MenuConfigManager.TV_UPDATE_SCAN_DVBT_UPDATE);
        updateScan = util.createPreference(MenuConfigManager.TV_UPDATE_SCAN_DVBT_UPDATE,
                R.string.menu_tv_update_scan, euAntennaUpdateScanIntent);
        if(!ScanContent.isCountryUK()){
            preferenceCategory.addPreference(updateScan);
        }

        // Analog Manual Scan
        Intent euAnalogScanIntent = new Intent(mContext, ScanViewActivity.class);
        euAnalogScanIntent.putExtra("ActionID", MenuConfigManager.TV_ANALOG_SCAN);
        Preference analogScan = util.createPreference(MenuConfigManager.TV_ANALOG_SCAN,
                R.string.menu_tv_analog_manual_scan, euAnalogScanIntent);
        if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)
                || !CommonIntegration.getInstance().isCurrentSourceDTVforEuPA()){
            if(!TVContent.getInstance(mContext).isCOLRegion()){
                preferenceCategory.addPreference(analogScan);
            }
        }

        // Single RF Scan
        Intent euRFScanIntent = new Intent(mContext, ScanViewActivity.class);
        euRFScanIntent.putExtra("ActionID", MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN);
        Preference rfScan = util.createPreference(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN,
                R.string.menu_tv_single_rf_scan, euRFScanIntent);

        //Manual Service Update
        Intent bgmIntent = new Intent(mContext, ScanDialogActivity.class);
        bgmIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_BGM);
        Preference serviceUpdate = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN_BGM,
                R.string.menu_tv_manual_service_update,bgmIntent);

        //LCN
        int lcnValue = mConfigManager.getDefault(MenuConfigManager.CHANNEL_LCN);
        Log.d("yupeng","lcn:"+lcnValue);
        Preference lcn = util.createListPreference(
            MenuConfigManager.CHANNEL_LCN,
            themedContext.getResources().getString(R.string.menu_channel_scan_lcn),
            true,
            themedContext.getResources().getStringArray(R.array.menu_tv_lcn),
            lcnValue);

        //channel scan type
        Preference channelScanType = util.createListPreference(
            MenuConfigManager.CHANNEL_DVBT_SCAN_TYPE,
            themedContext.getResources().getString(R.string.menu_channel_scan_type),
            true,
            themedContext.getResources().getStringArray(R.array.menu_tv_channel_scan_type),
            mConfigManager.getDefault(MenuConfigManager.CHANNEL_DVBT_SCAN_TYPE));

        //channel store type
        Preference channelStoreType = util.createListPreference(
            MenuConfigManager.CHANNEL_DVBT_STORE_TYPE,
            themedContext.getResources().getString(R.string.menu_channel_store_type),
            true,
            themedContext.getResources().getStringArray(R.array.menu_tv_channel_store_type),
            mConfigManager.getDefault(MenuConfigManager.CHANNEL_DVBT_STORE_TYPE));


       // favoriteNetworkSelect
        ScanThirdlyDialog  thirdDialog = new ScanThirdlyDialog(mContext,1);
        Preference favoriteNetworkSelect = util.createDialogPreference(MenuConfigManager.TV_FAVORITE_NETWORK,
                R.string.menu_c_favorite_net, thirdDialog);
        favoriteNetworkSelect.setEnabled(CommonIntegration.getInstance().isFavoriteNetworkEnable());

        if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)
                || !CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
            preferenceCategory.addPreference(rfScan);
            preferenceCategory.addPreference(serviceUpdate);
            preferenceCategory.addPreference(lcn);
            preferenceCategory.addPreference(channelScanType);
            preferenceCategory.addPreference(channelStoreType);
            if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK) && !CommonIntegration.isEUPARegion()){
            preferenceCategory.addPreference(favoriteNetworkSelect);
        }
        }
        // Channel Skip
        Intent intentAntenaSkip = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaSkip = new TransItem(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaSkip.putExtra("TransItem", transAntenaSkip);
        intentAntenaSkip.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SKIP);
        channelskip = util.createPreference(MenuConfigManager.TV_CHANNEL_SKIP,
                R.string.menu_tv_channel_skip, intentAntenaSkip);
        preferenceCategory.addPreference(channelskip);

        // Channel Sort
        Intent intentAntenaSort = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaSort = new TransItem(MenuConfigManager.TV_CHANNEL_SORT_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaSort.putExtra("TransItem", transAntenaSort);
        intentAntenaSort.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SORT);
        channelSort = util.createPreference(MenuConfigManager.TV_CHANNEL_SORT,
                R.string.menu_c_chanel_swap, intentAntenaSort);
        preferenceCategory.addPreference(channelSort);

        // Channel Move
        Intent intentAntenaMove = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaMove = new TransItem(MenuConfigManager.TV_CHANNEL_MOVE_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaMove.putExtra("TransItem", transAntenaMove);
        intentAntenaMove.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_MOVE);
        channelMove = util.createPreference(MenuConfigManager.TV_CHANNEL_MOVE,
                R.string.menu_c_chanel_Move, intentAntenaMove);
        preferenceCategory.addPreference(channelMove);

        // Channel Edit
        Intent intentAntenaEdit = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaEdit = new TransItem(MenuConfigManager.TV_CHANNEL_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaEdit.putExtra("TransItem", transAntenaEdit);
        intentAntenaEdit.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_EDIT);
        channelEdit = util.createPreference(MenuConfigManager.TV_CHANNEL_EDIT,
                R.string.menu_tv_channel_edit, intentAntenaEdit);
        preferenceCategory.addPreference(channelEdit);

      //Channel Delete
        Intent intentAntenaDelete = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaDelete = new TransItem(MenuConfigManager.TV_CHANNEL_DELETE_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaDelete.putExtra("TransItem", transAntenaDelete);
        intentAntenaDelete.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_DELETE);
        channelDelete = util.createPreference(MenuConfigManager.TV_CHANNEL_DELETE,
                R.string.menu_tv_channel_delete, intentAntenaDelete);
        preferenceCategory.addPreference(channelDelete);

        Intent intentAntenaFine = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaFine = new TransItem(MenuConfigManager.TV_CHANNELFINE_TUNE_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaFine.putExtra("TransItem", transAntenaFine);
        intentAntenaFine.putExtra("ActionID", MenuConfigManager.TV_CHANNELFINE_TUNE);
        saChannelFine = util.createPreference(MenuConfigManager.TV_CHANNELFINE_TUNE,
                R.string.menu_c_analog_tune, intentAntenaFine);
        if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA) || !mTV.isCurrentSourceDTV()){
            preferenceCategory.addPreference(saChannelFine);
        }

        // Clean List
    //    initCleanDialog();
        cleanList = util.createClickPreference(MenuConfigManager.TV_CHANNEL_CLEAR,
                R.string.menu_tv_clear_channel_list,mFacSetup );
        preferenceCategory.addPreference(cleanList);

        if (MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0) {
            channelScan.setEnabled(false);
            updateScan.setEnabled(false);
            analogScan.setEnabled(false);
            rfScan.setEnabled(false);
            saChannelFine.setEnabled(false);
            serviceUpdate.setEnabled(false);
        } else {
            channelScan.setEnabled(true);
            setPreferenceStatus(analogScan, rfScan);
            updateScan.setEnabled(true);
            saChannelFine.setEnabled(true);
            serviceUpdate.setEnabled(true);
        }
        if (mNavIntegration.hasActiveChannel() && !mNavIntegration.is3rdTVSource()) {
            channelEdit.setEnabled(true);
            channelSort.setEnabled(true);
            MtkTvChannelInfoBase mtktvChannelinfo =mNavIntegration.getCurChInfoByTIF();
            if(mtktvChannelinfo != null && mtktvChannelinfo.isAnalogService()){
                 saChannelFine.setEnabled(true);
            }else {
                 saChannelFine.setEnabled(false);
            }
            channelMove.setEnabled(true);
            cleanList.setEnabled(true);
           if(mHelper.hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)){
               channelskip.setEnabled(true);
           }else{
               channelskip.setEnabled(false);
           }
        } else {
            channelEdit.setEnabled(false);
            channelSort.setEnabled(false);
            saChannelFine.setEnabled(false);
            channelMove.setEnabled(false);
            cleanList.setEnabled(false);
            if (mNavIntegration.hasActiveChannel(true) && !mNavIntegration.is3rdTVSource()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelskip T true");
                channelskip.setEnabled(true);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelskip T false");
                channelskip.setEnabled(false);
            }
        }

        bindChannelPreference();
        mDataHelper.changePreferenceEnable();
        if(mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) != 0 &&
                !mNavIntegration.isCurrentSourceATVforEuPA()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LCN");
            channelSort.setEnabled(false);
            channelMove.setEnabled(false);
        }
        if(CommonIntegration.getInstance().isFavoriteNetworkEnable()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFavoriteNetworkEnable()");
            channelSort.setEnabled(false);
            channelMove.setEnabled(false);
            channelskip.setEnabled(false);
            channelEdit.setEnabled(false);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelDelete isEnabled"+channelDelete.isEnabled());
        return preferenceCategory;
    }

    private void setPreferenceStatus(Preference analogScan, Preference rfScan) {
        if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)){
            if(mTV.isCurrentSourceATV()){
                 analogScan.setEnabled(true);
                rfScan.setEnabled(false);
            }else{
                analogScan.setEnabled(false);
                rfScan.setEnabled(true);
            }
        }else{
             if(ScanContent.isCountryUK()) {
                 analogScan.setEnabled(false);
             } else {
                 analogScan.setEnabled(true);
             }
            rfScan.setEnabled(true);
        }
    }
    private PreferenceScreen addCableScan(PreferenceScreen preferenceCategory,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        List<String> operatorListStr = ScanContent.getCableOperationList(mContext);
        List<CableOperator> operatorList = ScanContent
                .convertStrOperator(mContext, operatorListStr);
        int size = operatorList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                CableOperator operator = operatorList.get(i);
                String name = ScanContent.getOperatorStr(mContext,
                        operator);
                //ScanContent.setOperator(mContext, name);
                Intent intent = new Intent(mContext, ScanViewActivity.class);
                intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR + "#"
                        + operator.ordinal());
                intent.putExtra("CableOperator", name);
                if(name.equalsIgnoreCase(mContext.getResources().getString(R.string.dvbc_operator_upc))){
                    if(ScanContent.isCountryIre()){
                        name = mContext.getResources().getString(R.string.dvbc_operator_virgin_media);
                    }else if(TVContent.getInstance(mContext).isRomCountry() || TVContent.getInstance(mContext).isHunCountry()){
                        name = mContext.getResources().getString(R.string.dvbc_operator_vodafone);
                    }else if(TVContent.getInstance(mContext).isAutCountry()){
                        name = mContext.getResources().getString(R.string.dvbc_operator_magenta);
                    }
                }
                if(name.equalsIgnoreCase(mContext.getResources().getString(R.string.dvbc_operator_canal_digital))){
                   if(TVContent.getInstance(mContext).isNorCountry()){
                        name = mContext.getResources().getString(R.string.dvbc_operator_telenor);
                    }
                }
                if(name.equalsIgnoreCase(mContext.getString(R.string.dvbc_operator_akado))){
                    if(MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_RUS)){
                        name = mContext.getString(R.string.dvbc_operator_akado_russia);
                    }
                }
                Preference cableOperatorItem = util.createPreference(
                        MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR,
                        name, intent);
                preferenceCategory.addPreference(cableOperatorItem);
            }
        }
        return preferenceCategory;
    }

    Preference channelScanItem;
    public PreferenceScreen loadDataTvCable(PreferenceScreen preferenceCategory,
            Context themedContext) {
        if ((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) ||
                MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK))
                && mTV.isCurrentSourceATV() || mTV.isCOLRegion()) {
            PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
            preferenceCategory.removeAll();
            // ChannelScan
            Intent euAntennaChannelScanIntent = new Intent(mContext,
                    ScanDialogActivity.class);
            euAntennaChannelScanIntent.putExtra("ActionID",
                    MenuConfigManager.TV_CHANNEL_SCAN_DVBT);
            Preference channelScan = util.createPreference(
                    MenuConfigManager.TV_CHANNEL_SCAN_DVBT,
                    R.string.menu_tv_channel_scan, euAntennaChannelScanIntent);
            preferenceCategory.addPreference(channelScan);
        } else {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        List<String> operatorListStr = ScanContent.getCableOperationList(mContext);
        List<CableOperator> operatorList = ScanContent
                .convertStrOperator(mContext, operatorListStr);
        int size = operatorList.size();
        if (size > 0 && !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
            if(CommonIntegration.getInstance().hasActiveChannel() && ScanContent.isVooOp()){
                channelScanItem = util.createDialogPreference(MenuConfigManager.TV_CHANNEL_SCAN_DVBC,
                        R.string.menu_tv_channel_scan, getDvbcAutoScanDialog(), true);
                channelScanItem.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if(CommonIntegration.getInstance().hasActiveChannel()){
                            ((TvSettingsActivity)mContext).hide();
                        }
                        return false;
                    }
                });
            }else {
                channelScanItem = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN_DVBC,
                        R.string.menu_tv_channel_scan);
            }

            preferenceCategory.addPreference(channelScanItem);
        } else {
            // set default operator to "Other"
            String others = mContext.getString(R.string.dvbc_operator_others);
            ScanContent.setOperator(mContext, others);
            Intent cableChannelScanIntent = new Intent(mContext, ScanViewActivity.class);
            cableChannelScanIntent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBC);
            channelScanItem = util.createPreference(MenuConfigManager.TV_CHANNEL_SCAN_DVBC,
                    R.string.menu_tv_channel_scan,cableChannelScanIntent);
            preferenceCategory.addPreference(channelScanItem);
            }
        }

        // Analog Manual Scan
        Intent cableAnalogScanIntent = new Intent(mContext, ScanViewActivity.class);
        cableAnalogScanIntent.putExtra("ActionID", MenuConfigManager.TV_ANALOG_SCAN);
        PreferenceUtil util = PreferenceUtil.getInstance(mPreferenceManager.getContext());
        Preference analogScan = util.createPreference(MenuConfigManager.TV_ANALOG_SCAN,
                R.string.menu_tv_analog_manual_scan, cableAnalogScanIntent);
        if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)
                || !CommonIntegration.getInstance().isCurrentSourceDTVforEuPA()){
            if(!TVContent.getInstance(mContext).isCOLRegion()){
                preferenceCategory.addPreference(analogScan);
            }
        }
        // Single RF Scan
        Intent cableRFScanIntent = new Intent(mContext, ScanViewActivity.class);
        cableRFScanIntent.putExtra("ActionID", MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN);
        Preference mCableRFScan = util.createPreference(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN,
                R.string.menu_tv_single_rf_scan, cableRFScanIntent);

        //LCN
        Preference lcn = util.createListPreference(
            MenuConfigManager.CHANNEL_LCN,
            themedContext.getResources().getString(R.string.menu_channel_scan_lcn),
            true,
            themedContext.getResources().getStringArray(R.array.menu_tv_lcn),
            mConfigManager.getDefault(MenuConfigManager.CHANNEL_LCN));
        //if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)){
        //}

        //channel scan type
        Preference channelScanType = util.createListPreference(
            MenuConfigManager.CHANNEL_DVBC_SCAN_TYPE,
            themedContext.getResources().getString(R.string.menu_channel_scan_type),
            true,
            themedContext.getResources().getStringArray(R.array.menu_tv_channel_scan_type),
            mConfigManager.getDefault(MenuConfigManager.CHANNEL_DVBC_SCAN_TYPE));

        //channel store type
        Preference channelStoreType = util.createListPreference(
            MenuConfigManager.CHANNEL_DVBC_STORE_TYPE,
            themedContext.getResources().getString(R.string.menu_channel_store_type),
            true,
            themedContext.getResources().getStringArray(R.array.menu_tv_channel_store_type),
            mConfigManager.getDefault(MenuConfigManager.CHANNEL_DVBC_STORE_TYPE));
        if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)
                || !CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
            if(!mTV.isCOLRegion()){
                preferenceCategory.addPreference(mCableRFScan);
            }
            preferenceCategory.addPreference(lcn);
            preferenceCategory.addPreference(channelScanType);
            preferenceCategory.addPreference(channelStoreType);
        }

        // Channel Skip 111
        Intent intentCableSkip = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transCableSkip = new TransItem(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentCableSkip.putExtra("TransItem", transCableSkip);
        intentCableSkip.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SKIP);
        channelskip = util.createPreference(MenuConfigManager.TV_CHANNEL_SKIP,
                R.string.menu_tv_channel_skip, intentCableSkip);
        preferenceCategory.addPreference(channelskip);

        // Channel Sort 111
        Intent intentCableSort = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transCableSort = new TransItem(MenuConfigManager.TV_CHANNEL_SORT_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentCableSort.putExtra("TransItem", transCableSort);
        intentCableSort.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SORT);
        channelSort = util.createPreference(MenuConfigManager.TV_CHANNEL_SORT,
                R.string.menu_c_chanel_swap, intentCableSort);
        preferenceCategory.addPreference(channelSort);

     // Channel Move
        Intent intentAntenaMove = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaMove = new TransItem(MenuConfigManager.TV_CHANNEL_MOVE_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaMove.putExtra("TransItem", transAntenaMove);
        intentAntenaMove.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_MOVE);
        channelMove = util.createPreference(MenuConfigManager.TV_CHANNEL_MOVE,
                R.string.menu_c_chanel_Move, intentAntenaMove);
        preferenceCategory.addPreference(channelMove);

        // Channel Edit 111
        Intent intentCableEdit = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transCableEdit = new TransItem(MenuConfigManager.TV_CHANNEL_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentCableEdit.putExtra("TransItem", transCableEdit);
        intentCableEdit.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_EDIT);
        channelEdit = util.createPreference(MenuConfigManager.TV_CHANNEL_EDIT,
                R.string.menu_tv_channel_edit, intentCableEdit);
        preferenceCategory.addPreference(channelEdit);

      //Channel Delete
        Intent intentAntenaDelete = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaDelete = new TransItem(MenuConfigManager.TV_CHANNEL_DELETE_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaDelete.putExtra("TransItem", transAntenaDelete);
        intentAntenaDelete.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_DELETE);
        channelDelete = util.createPreference(MenuConfigManager.TV_CHANNEL_DELETE,
                R.string.menu_tv_channel_delete, intentAntenaDelete);
        preferenceCategory.addPreference(channelDelete);

        // ChannelFine
        Intent intentCableFine = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transCableFine = new TransItem(MenuConfigManager.TV_CHANNELFINE_TUNE_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentCableFine.putExtra("TransItem", transCableFine);
        intentCableFine.putExtra("ActionID", MenuConfigManager.TV_CHANNELFINE_TUNE);
        saChannelFine = util.createPreference(MenuConfigManager.TV_CHANNELFINE_TUNE,
                R.string.menu_c_analog_tune, intentCableFine);
        if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA) || !mTV.isCurrentSourceDTV()){
            preferenceCategory.addPreference(saChannelFine);
        }

        // Clean List
      //  initCleanDialog();
        cleanList = util.createClickPreference(MenuConfigManager.TV_CHANNEL_CLEAR,
                R.string.menu_tv_clear_channel_list,mFacSetup);
        preferenceCategory.addPreference(cleanList);

        if (MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0) {
            if (channelScanItem != null) {
                channelScanItem.setEnabled(false);
            }
            analogScan.setEnabled(false);
            mCableRFScan.setEnabled(false);
            saChannelFine.setEnabled(false);
        } else {
            if (channelScanItem != null) {
                channelScanItem.setEnabled(true);
            }
            setPreferenceStatus(analogScan, mCableRFScan);
            saChannelFine.setEnabled(true);
        }
        if (ScanContent.isRCSRDSOp() || ScanContent.isTELNETOp()) {
            // Fix CR:DTV00595647
            mCableRFScan.setEnabled(false);
        }
        if (mNavIntegration.hasActiveChannel() && !mNavIntegration.is3rdTVSource()) {
            if (ScanContent.isRCSRDSOp() || ScanContent.isTELNETOp()) {
                // Fix DTV00997398
                channelEdit.setEnabled(false);
            }else{
                channelEdit.setEnabled(true);
            }
            channelSort.setEnabled(true);
            MtkTvChannelInfoBase mtktvChannelinfo =mNavIntegration.getCurChInfoByTIF();
            if(mtktvChannelinfo != null && mtktvChannelinfo.isAnalogService()){
                 saChannelFine.setEnabled(true);
            }else {
                 saChannelFine.setEnabled(false);
            }
            if(mHelper.hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)){
                channelskip.setEnabled(true);
            }else{
                channelskip.setEnabled(false);
            }
            channelMove.setEnabled(true);
            cleanList.setEnabled(true);
        } else {
            channelEdit.setEnabled(false);
            channelSort.setEnabled(false);
            saChannelFine.setEnabled(false);
            channelMove.setEnabled(false);
            if (mNavIntegration.hasActiveChannel(true) && !mNavIntegration.is3rdTVSource()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelskip cable true");
                if(mHelper.hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)){
                    channelskip.setEnabled(true);
                }else{
                    channelskip.setEnabled(false);
                }
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelskip cable false");
                channelskip.setEnabled(false);
            }
            cleanList.setEnabled(false);

        }

        bindChannelPreference();
      //  mDataHelper.changePreferenceEnable();
        if(mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) !=0 &&
                !mNavIntegration.isCurrentSourceATVforEuPA()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LCN");
            channelSort.setEnabled(false);
            channelMove.setEnabled(false);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelDelete isEnabled"+channelDelete.isEnabled());
        return preferenceCategory;
    }

    List<SatelliteInfo> satellites;
    Preference mSatelliteRescan;
    SatListFrag satListFrag = new SatListFrag();

    private PreferenceScreen addDVBSRescan(PreferenceScreen preferenceCategory,
            Context themedContext) {
        preferenceCategory.removeAll();
        preferenceCategory.addPreference(getPreferenceCheckOperator(themedContext,
                MenuConfigManager.DVBS_RESCAN_NEXT, R.string.setup_next));
        PreferenceUtil util = PreferenceUtil.getInstance(mPreferenceManager.getContext());
        preferenceCategory.addPreference(util.createPreference(MenuConfigManager.DVBS_RESCAN_MORE,
                R.string.dvbs_rescan_more));
        return preferenceCategory;
    }

    private PreferenceScreen addDVBSRescanNext(PreferenceScreen preferenceCategory,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        List<String> operatorListStr = ScanContent.getDVBSOperatorList(mContext);
        int size = operatorListStr.size();
        for (int i = 0; i < size; i++) {
            String name = operatorListStr.get(i);
            Intent intentOperator = new Intent(mContext, SatActivity.class);
            intentOperator.putExtra("mItemId", MenuConfigManager.DVBS_SAT_OP);
            intentOperator.putExtra("title", name);
            intentOperator.putExtra("selectPos", i);
            Preference cableOperatorItem = null;
            if(name.equalsIgnoreCase(themedContext.getString(R.string.dvbs_operator_name_m7_fast_scan))){
                cableOperatorItem = util.createPicturePreference(
                        MenuConfigManager.DVBS_SAT_OP,
                        name, intentOperator);
                preferenceCategory.addPreference(cableOperatorItem);
            }else{
                cableOperatorItem = util.createPreference(
                        MenuConfigManager.DVBS_SAT_OP,
                        name, intentOperator);
            }
            preferenceCategory.addPreference(cableOperatorItem);
        }
        return preferenceCategory;
    }
    
    private PreferenceScreen addDVBSFastUpdateScan(PreferenceScreen preferenceCategory,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        final MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
        MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan.dvbsM7GetOptList();
        String selectedOpName = SaveValue.readWorldStringValue(themedContext, "FAST_SCAN_SELECTED_OPT");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectedOpName = "+selectedOpName);
        if(dvbsRet == MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK
                && dvbsScan.M7_OptLists != null
                && dvbsScan.M7_OptLists.length == dvbsScan.M7_OptNum) {
              for(int i=0; i < dvbsScan.M7_OptLists.length; i++) {
                MtkTvScanDvbsBase.M7OneOPT data = dvbsScan.M7_OptLists[i];
                String showName = data.subOptName+" (" + data.optName + ")";
                  if(data.subOptName.equals(selectedOpName)){
                    List<SatelliteInfo> allSats = ScanContent.getDVBSsatellites(mContext);
                    List<SatelliteInfo> enabledSats = ScanContent.getDVBSEnablesatellites(mContext, allSats);
                    if(ScanContent.checkFastScanOrbitsError(data, showName, enabledSats)){
                        Preference updateScanItem = util.createDialogPreference(MenuConfigManager.DVBS_SAT_OP,
                                showName, ScanContent.getDvbsFastScanOrbitErrorDialog(mContext, data, showName));
                        preferenceCategory.addPreference(updateScanItem);
                    }else {
                        int satid = allSats.get(0).getSatlRecId();
                        Intent intent = new Intent(mContext, ScanViewActivity.class);
                        intent.putExtra("ActionID", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
                        intent.putExtra("SatID", satid);
                        intent.putExtra("LocationID", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
                        intent.putExtra("SelectedOpName", data.subOptName);
                        Preference updateScanItem = util.createPreference(
                                data.subOptName,
                                showName, intent);
                        //updateScanItem.requestFocus();
                        preferenceCategory.addPreference(updateScanItem);
                    }
                }else {
                    Preference updateScanItem = util.createDialogPreference(MenuConfigManager.DVBS_SAT_OP,
                            showName, getDvbsUpdateScanDialog(data, showName));
                    preferenceCategory.addPreference(updateScanItem);
                }
              }
            }
        return preferenceCategory;
    }

    private PreferenceScreen addDVBSRescanMore(PreferenceScreen preferenceCategory,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        preferenceCategory.addPreference(getPreferenceCheckOperator(themedContext,
                MenuConfigManager.DVBS_RESCAN_SINGLE, R.string.dvbs_antenna_type_single));
        preferenceCategory.addPreference(getPreferenceCheckOperator(themedContext,
                MenuConfigManager.DVBS_RESCAN_TONEBURST, R.string.dvbs_tone_burst));
        preferenceCategory.addPreference(getPreferenceCheckOperator(themedContext,
                MenuConfigManager.DVBS_RESCAN_DISEQC10, R.string.dvbs_diseqc_10));
        preferenceCategory.addPreference(getPreferenceCheckOperator(themedContext,
                MenuConfigManager.DVBS_RESCAN_DISEQC11, R.string.dvbs_diseqc_11));
        preferenceCategory.addPreference(util.createPreference(MenuConfigManager.DVBS_RESCAN_DISEQC12,
                R.string.dvbs_diseqc_motor_12));
        preferenceCategory.addPreference(util.createPreference(MenuConfigManager.DVBS_RESCAN_UNICABLE1,
                R.string.dvbs_diseqc_unicable1));
        preferenceCategory.addPreference(util.createPreference(MenuConfigManager.DVBS_RESCAN_UNICABLE2,
                R.string.dvbs_diseqc_unicable2));
        return preferenceCategory;
    }

    private PreferenceScreen addDVBSUnicable(PreferenceScreen preferenceCategory,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        preferenceCategory.addPreference(getPreferenceCheckOperator(themedContext,
                MenuConfigManager.DVBS_RESCAN_NEXT, R.string.setup_next));
        int attennaType = mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE);
        String[] tunerList=null;
        String[] tunerValueList=null;
        if(attennaType == 1){
            tunerValueList = themedContext.getResources().getStringArray(R.array.dvbs_user_band_arrays);
            tunerList = new String[tunerValueList.length];
            for(int i = 0; i < tunerValueList.length; i++){
                tunerList[i] =  themedContext.getResources().getString(R.string.dvbs_user_band_item_id, Integer.parseInt(tunerValueList[i]));
            }
        }else if(attennaType == 2){
            tunerValueList =  themedContext.getResources().getStringArray(R.array.dvbs_user_jess_band_arrays);
            tunerList = new String[tunerValueList.length];
            for(int i = 0; i < tunerValueList.length; i++){
                tunerList[i] =  themedContext.getResources().getString(R.string.dvbs_user_band_item_id, Integer.parseInt(tunerValueList[i]));
            }
        }
        List<SatelliteInfo> list = ScanContent.getDVBSsatellites(themedContext);
        int userBand = !list.isEmpty() ? list.get(0).getUserBand() : 0;
        int bandFreq = !list.isEmpty() ? list.get(0).getBandFreq() : 0;
        String defaultBandFreq = null;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addDVBSUnicable userBand="+userBand+",bandFreq="+bandFreq+",list.size()="+list.size());
        if(tunerList != null){
            preferenceCategory.addPreference(util.createListPreference(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_TUNER,
                    R.string.dvbs_user_band, true, tunerList, userBand));  
        }
        List<String> frequencyList = ScanContent.getSingleCableFreqsList(mContext, userBand);
        String[] freqArray = frequencyList.toArray(new String[0]);
        if(frequencyList.contains(bandFreq+"")){
            defaultBandFreq = bandFreq+"";
        }else {
            defaultBandFreq = freqArray[freqArray.length - 1];
        }
        preferenceCategory.addPreference(util.createListPreference(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_BANDFREQ,
                R.string.dvbs_band_freq, true, freqArray, freqArray, defaultBandFreq));

        Preference userDefine = util.createClickPreference(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_USERDEF,
                R.string.dvbs_band_freq_user_define,new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(mContext, EditTextActivity.class);
                intent.putExtra(EditTextActivity.EXTRA_DESC, mContext.getString(R.string.dvbs_band_freq_user_define));
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 4);
                intent.putExtra(EditTextActivity.EXTRA_HINT_TEXT, "[950, 2150]");
                ((LiveTvSetting)mContext).startActivityForResult(intent, 0x30);
                return true;
            }
        });
        preferenceCategory.addPreference(userDefine);
        return preferenceCategory;
    }

    private Preference getPreferenceCheckOperator(Context context, String key, int title) {
        PreferenceUtil util = PreferenceUtil.getInstance(context);
        List<String> operatorListStr = ScanContent.getDVBSOperatorList(mContext);
        int size = operatorListStr.size();
        Log.d(TAG, "getPreferenceCheckOperator value===="+mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE));
        if (ScanContent.isPreferedSat() && size > 0) {
            return util.createPreference(key,title);
        } else {
            Intent intentRescan = new Intent(mContext, SatActivity.class);
            intentRescan.putExtra("mItemId", MenuConfigManager.DVBS_SAT_RE_SCAN);
            intentRescan.putExtra("title", mContext.getString(R.string.menu_s_sate_rescan));
            intentRescan.putExtra("selectPos", 0);
            return util.createClickPreference(key, title, intentRescan,new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference p) {
                    int currentType = mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE);
                    String key = p.getKey();
                    if(key.equals(MenuConfigManager.DVBS_RESCAN_NEXT) && currentType == 0){
                        mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
                        ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_SINGLE);
                    }else if(key.equals(MenuConfigManager.DVBS_RESCAN_SINGLE) && currentType != MenuConfigManager.DVBS_ACFG_SINGLE){
                        mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
                        ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_SINGLE);
                    }else if(key.equals(MenuConfigManager.DVBS_RESCAN_TONEBURST) && currentType != MenuConfigManager.DVBS_ACFG_TONEBURST){
                        mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_TONEBURST);
                        ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_TONEBURST);
                    }else if(key.equals(MenuConfigManager.DVBS_RESCAN_DISEQC10) && currentType != MenuConfigManager.DVBS_ACFG_DISEQC10){
                        mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC10);
                        ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_DISEQC10);
                    }else if(key.equals(MenuConfigManager.DVBS_RESCAN_DISEQC11) && currentType != MenuConfigManager.DVBS_ACFG_DISEQC11){
                        mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC11);
                        ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_DISEQC11);
                    }else if(key.equals(MenuConfigManager.DVBS_RESCAN_DISEQC12) && currentType != MenuConfigManager.DVBS_ACFG_DISEQC12){
                        mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC12);
                        ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_DISEQC12);
                    }else{//refresh satl from DB
                        ScanContent.applyDBSatellitesToSatl(mContext, currentType);
                    }
                    ScanContent.setCamSatellitesMask();
                    return false;
                }
            });
        }
    }

	private PreferenceScreen addDVBSNormalSdxScan(PreferenceScreen preferenceCategory,
            Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        /*Intent intentRescan = new Intent(mContext, SatActivity.class);
        intentRescan.putExtra("mItemId", MenuConfigManager.DVBS_SAT_NORMAL_SCAN);
        intentRescan.putExtra("title", mContext.getString(R.string.menu_s_sate_rescan));
        intentRescan.putExtra("selectPos", 0); */
        mSatelliteRescan = util.createPreference(
                MenuConfigManager.DVBS_SAT_NORMAL_SCAN,
                R.string.dvbs_scan_type_normal/*, intentRescan*/);

        Preference sdxPref = util.createPreference(
                MenuConfigManager.DVBS_SAT_SCAN_SDX,
                R.string.dvbs_scan_type_sdx);
        sdxPref.setOnPreferenceClickListener(mClickListener);
        preferenceCategory.addPreference(mSatelliteRescan);
        preferenceCategory.addPreference(sdxPref);
        return preferenceCategory;
    }

    private PreferenceScreen addDVBSDownload(PreferenceScreen preferenceCategory,
            Context themedContext) {
        preferenceCategory.removeAll();
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        List<String> names = CommonIntegration.getInstance().getUDiskFileNames("sdx");
        for(String name : names){
            Intent intentOperator = new Intent(mContext, SatActivity.class);
            Preference fileItem = util.createPreference(
                    MenuConfigManager.DVBS_SAT_SCAN_DOWNLOAD,
                    name, intentOperator);
            intentOperator.putExtra("mItemId", MenuConfigManager.DVBS_SAT_SCAN_DOWNLOAD);
            intentOperator.putExtra("title", name);
            preferenceCategory.addPreference(fileItem);
        }
        return preferenceCategory;
    }

    public PreferenceScreen getCamScanOperatorScreen() {
        final Context themedContext = mPreferenceManager.getContext();
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        mainPreferenceScreen =
                mPreferenceManager.createPreferenceScreen(themedContext);
        mainPreferenceScreen.setTitle(R.string.menu_ci_cam_scan);
        String profileName = MtkTvConfig.getInstance().getConfigString(
                MenuConfigManager.CHANNEL_LIST_SLOT);
        String[] operators = null;
        if(profileName != null && profileName.length() != 0){
            operators = new String[]{profileName};//need to to
        }else{
            operators = new String[]{mContext.getString(R.string.menu_ci_cam_scan)};
        }
        for (int i = 0; i < operators.length; i++) {
            String name = operators[i];
            Intent intentOperator = new Intent(mContext, SatActivity.class);
            intentOperator.putExtra("mItemId", MenuConfigManager.DVBS_SAT_OP_CAM_SCAN);
            intentOperator.putExtra("title", name);
            intentOperator.putExtra("selectPos", i);

            Preference operatorItem = util.createPreference(
                    MenuConfigManager.DVBS_SAT_OP_CAM_SCAN,
                    name, intentOperator);
            mainPreferenceScreen.addPreference(operatorItem);

        }
        return mainPreferenceScreen;
    }

    public PreferenceScreen loadDataTvPersonalSatllites(
            PreferenceScreen preferenceCategory, Context themedContext) {
        TVContent.getInstance(mContext).loadDvbsDataBase();
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        preferenceCategory.removeAll();
        // Satellite Rescan
        Preference satelliteRescan = util.createPreference(MenuConfigManager.DVBS_SAT_RE_SCAN,
                R.string.menu_s_sate_rescan);
        satelliteRescan.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                if (CommonIntegration.getInstance().hasActiveChannel()) {
                    ((TvSettingsActivity) mContext).hide();
                    mFacSetup.getDvbsRescanDialog(mContext).show();
                    return true;
                }
                return false;
            }
        });
        preferenceCategory.addPreference(satelliteRescan);

        // Satellite add
        Intent intentAdd = new Intent(mContext, SatActivity.class);
        intentAdd.putExtra("mItemId", MenuConfigManager.DVBS_SAT_ADD);
        intentAdd.putExtra("title", mContext.getString(R.string.menu_s_sate_add));
        intentAdd.putExtra("selectPos", -1);
        Preference satelliteAdd = util.createPreference(
                MenuConfigManager.DVBS_SAT_ADD,
                R.string.menu_s_sate_add, intentAdd);
        preferenceCategory.addPreference(satelliteAdd);
        satelliteAdd.setOnPreferenceClickListener(mClickListener);
        satellites = ScanContent.getDVBSsatellites(mContext);

        // Satellite update Scan
        Preference satelliteUpdate = null;
        if(ScanContent.isSatOpFastScan()){
		    satelliteUpdate = util.createPreference(MenuConfigManager.DVBS_SAT_UPDATE_SCAN,
                    R.string.menu_s_sate_update);
            preferenceCategory.addPreference(satelliteUpdate);
        }else {
        Intent intentUpdate = new Intent(mContext, SatActivity.class);
        intentUpdate.putExtra("mItemId", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
        intentUpdate.putExtra("title", mContext.getString(R.string.menu_s_sate_update));
        intentUpdate.putExtra("selectPos", -1);
        satelliteUpdate = util.createPreference(
                MenuConfigManager.DVBS_SAT_UPDATE_SCAN,
                R.string.menu_s_sate_update, intentUpdate);
        preferenceCategory.addPreference(satelliteUpdate);
        }

        // Satellite satelliteManualTuning
        Intent intentManual = new Intent(mContext, SatActivity.class);
        intentManual.putExtra("mItemId", MenuConfigManager.DVBS_SAT_MANUAL_TURNING);
        intentManual.putExtra("title", mContext.getString(R.string.menu_s_sate_tuning));
        intentManual.putExtra("selectPos", -1);
        Preference satelliteManualTuning = util.createPreference(
                MenuConfigManager.DVBS_SAT_MANUAL_TURNING,
                R.string.menu_s_sate_tuning, intentManual);
        satelliteManualTuning.setOnPreferenceClickListener(mClickListener);
        preferenceCategory.addPreference(satelliteManualTuning);



        // Channel Skip
        Intent intentSatSkip = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transSatSkip = new TransItem(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentSatSkip.putExtra("TransItem", transSatSkip);
        intentSatSkip.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SKIP);
        channelskip = util.createPreference(MenuConfigManager.TV_CHANNEL_SKIP,
                R.string.menu_tv_channel_skip, intentSatSkip);
        preferenceCategory.addPreference(channelskip);

        // Channel Sort
        Intent intentSatSort = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transSatSort = new TransItem(MenuConfigManager.TV_CHANNEL_SORT_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentSatSort.putExtra("TransItem", transSatSort);
        intentSatSort.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SORT);
        channelSort = util.createPreference(MenuConfigManager.TV_CHANNEL_SORT,
                R.string.menu_c_chanel_swap, intentSatSort);
        preferenceCategory.addPreference(channelSort);

     // Channel Move
        Intent intentAntenaMove = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaMove = new TransItem(MenuConfigManager.TV_CHANNEL_MOVE_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaMove.putExtra("TransItem", transAntenaMove);
        intentAntenaMove.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_MOVE);
        channelMove = util.createPreference(MenuConfigManager.TV_CHANNEL_MOVE,
                R.string.menu_c_chanel_Move, intentAntenaMove);
        preferenceCategory.addPreference(channelMove);

        // Channel Edit
        Intent intentSatEdit = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transSatEdit = new TransItem(MenuConfigManager.TV_CHANNEL_EDIT_LIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentSatEdit.putExtra("TransItem", transSatEdit);
        intentSatEdit.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_EDIT);
        channelEdit = util.createPreference(MenuConfigManager.TV_CHANNEL_EDIT,
                R.string.menu_tv_channel_edit, intentSatEdit);
        preferenceCategory.addPreference(channelEdit);

      //Channel Delete
        Intent intentAntenaDelete = new Intent(mContext, ChannelInfoActivity.class);
        TransItem transAntenaDelete = new TransItem(MenuConfigManager.TV_CHANNEL_DELETE_CHANNELLIST, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intentAntenaDelete.putExtra("TransItem", transAntenaDelete);
        intentAntenaDelete.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_DELETE);
        channelDelete = util.createPreference(MenuConfigManager.TV_CHANNEL_DELETE,
                R.string.menu_tv_channel_delete, intentAntenaDelete);
        preferenceCategory.addPreference(channelDelete);

        cleanList = util.createClickPreference(MenuConfigManager.TV_CHANNEL_CLEAR,
                R.string.menu_tv_clear_channel_list,mFacSetup);
        preferenceCategory.addPreference(cleanList);
        if (MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0) {
            satelliteRescan.setEnabled(false);
            satelliteAdd.setEnabled(false);
            satelliteUpdate.setEnabled(false);
            satelliteManualTuning.setEnabled(false);
       //    saChannelFine.setEnabled(false);
        } else {
            satelliteRescan.setEnabled(true);
            satelliteAdd.setEnabled(true);
            satelliteUpdate.setEnabled(true);
            satelliteManualTuning.setEnabled(true);
        //    saChannelFine.setEnabled(true);
        }
        boolean isTKGS = ScanContent.isPreferedSat() && mTV.isTurkeyCountry() && mTV.isTKGSOperator();
        if (mNavIntegration.hasActiveChannel() && !mNavIntegration.is3rdTVSource()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.isTurkeyCountry()> "+mTV.isTurkeyCountry() +"dvbs_operator_name_tivibu > "+ScanContent.getDVBSCurrentOPStr(mContext)+"  >>   "+mContext
                    .getString(R.string.dvbs_operator_name_tivibu));
            if(mTV.isTurkeyCountry() && ScanContent.getDVBSCurrentOPStr(mContext).equalsIgnoreCase(mContext
                    .getString(R.string.dvbs_operator_name_tivibu))){
                   channelEdit.setEnabled(false);
                   channelskip.setEnabled(false);
                   channelSort.setEnabled(false);
                   channelMove.setEnabled(false);
                   cleanList.setEnabled(false);
            }else{
                 channelEdit.setEnabled(true);
                 if(mHelper.hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)){
                     channelskip.setEnabled(true);
                 }else{
                     channelskip.setEnabled(false);
                 }
                 channelSort.setEnabled(true);
                 channelMove.setEnabled(true);
                 cleanList.setEnabled(true);
            }
            if (isTKGS) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTKGS getTKGSOperatorMode");
                if (mTV.getTKGSOperatorMode() == 0) {// automatic
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTKGSoperator auto");
                  satelliteAdd.setEnabled(false);
                  channelSort.setEnabled(false);
                  channelMove.setEnabled(false);
                  channelEdit.setEnabled(false);
                  cleanList.setEnabled(false);
                }else if(mTV.getTKGSOperatorMode() == 1){ //customizable
                    channelEdit.setEnabled(false);
                    cleanList.setEnabled(false);
                }

              }
        } else {
            channelEdit.setEnabled(false);
        //    channelskip.setEnabled(false);
            channelSort.setEnabled(false);
            channelMove.setEnabled(false);
            cleanList.setEnabled(false);
            if (mNavIntegration.hasActiveChannel(true) && !mNavIntegration.is3rdTVSource()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelskip satllite true");
                if(mHelper.hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)){
                    channelskip.setEnabled(true);
                }else{
                    channelskip.setEnabled(false);
                }
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelskip satllite false");
                channelskip.setEnabled(false);
            }
        }
        if (isTKGS) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTKGS");
            if (mTV.getTKGSOperatorMode() == 0) {// automatic
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTKGSoperator auto");
              satelliteAdd.setEnabled(false);
            }

          } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is not TKGS");
          }
        bindChannelPreference();
      //  mDataHelper.changePreferenceEnable();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelDelete isEnabled"+channelDelete.isEnabled());
        return preferenceCategory;

    }
  private Preference mScreenModeItem;

    void getScreenMode(){
//      int[] screenList = SundryImplement.getInstanceNavSundryImplement(mContext).getSupportScreenModes();
        String[] arrayString = mContext.getResources().getStringArray(R.array.screen_mode_array_us);
        int[] screenList = mHelper.getSupportScreenModes();
        if(screenList==null){
            mScreenMode=arrayString;
            return;
        }
        String mScreenModeList[] = new String[screenList.length];
        for(int i=0;i<screenList.length;i++){
            mScreenModeList[i] = arrayString[screenList[i]];
        }
        mScreenMode =mScreenModeList ;
        if(mScreenMode != null){
            for(String s:mScreenMode){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "screen mode is :"+s);
            }
        }

    }
    private void addSetupSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        haveScreenMode =false;
        String[] screenMode = mContext.getResources().getStringArray(
                R.array.screen_mode_array_eu_analog);
      //  boolean isPipopScreen = false;
        if (CommonIntegration.isSARegion() || CommonIntegration.isUSRegion()) {
            if (mTV.isCurrentSourceVGA()) {
                screenMode = mContext.getResources().getStringArray(
                        R.array.screen_mode_array_vga);
            } else {
                screenMode = mContext.getResources().getStringArray(
                        R.array.screen_mode_array_sa_analog);
                if (CommonIntegration.isUSRegion()
                        && mTV.isCurrentSourceTv()
                        && !mTV.isAnalog(CommonIntegration.getInstance()
                                .getCurChInfo())) {
                    screenMode = mContext.getResources().getStringArray(
                            R.array.screen_mode_array_us_dtv);
                }
                if (CommonIntegration.getInstance().isPOPState()) {
                    screenMode = mContext.getResources().getStringArray(
                            R.array.screen_mode_array_sa_pop);
                } else if ((CommonIntegration.getInstance().isPIPState() && ("sub")
                        .equalsIgnoreCase(CommonIntegration.getInstance()
                                .getCurrentFocus()))) {
                    screenMode = mContext.getResources().getStringArray(
                            R.array.screen_mode_array_sa_pip);
                    if (CommonIntegration.isUSRegion()) {
                        screenMode = mContext.getResources().getStringArray(
                                R.array.screen_mode_array_us_pip_sub);
                    }
                } else if (CommonIntegration.getInstance().isPIPState()) {
                    screenMode = mContext.getResources().getStringArray(
                            R.array.screen_mode_array_us_pip_main_analog);
                }
            }
        }else if (CommonIntegration.isCNRegion()) {
            screenMode = mContext.getResources().getStringArray(
                    R.array.screen_mode_array);
        }else if (CommonIntegration.isEURegion()){
            if (CommonIntegration.getInstance().isPOPState()) {
            //    isPipopScreen = true;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do pip specail11");
                screenMode = mContext.getResources().getStringArray(
                        R.array.screen_mode_array_sa_pop);
            } else if ((CommonIntegration.getInstance().isPIPState() && ("sub")
                    .equalsIgnoreCase(CommonIntegration.getInstance()
                            .getCurrentFocus()))) {
            //    isPipopScreen = true;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do pip specail22");
                screenMode = mContext.getResources().getStringArray(
                        R.array.screen_mode_array_sa_pip);
            } else if (CommonIntegration.getInstance().isPIPState()) {
            //    isPipopScreen = true;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do pip specail33");
                screenMode = mContext.getResources().getStringArray(
                        R.array.screen_mode_array_us_pip_main_analog);
            }
        }

        //Region Setting
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"init region setting");
        if((mTV.isEcuadorCountry() || mTV.isPhiCountry()) &&
            MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SA_EWS_SUPPORT) &&
            false == DataSeparaterUtil.getInstance().isAtvOnly() &&
            (CommonIntegration.getInstance().isCurrentSourceTv()||
            CommonIntegration.getInstance().isCurrentSourceDTV()||
            CommonIntegration.getInstance().isCurrentSourceATV())) {
            preferenceScreen.addPreference(util.createPreference(
                MenuConfigManager.SETUP_REGION_SETTING, R.string.menu_setup_region_setting));
        }


        //dpms
        if(!CommonIntegration.isCNRegion() && mNavIntegration.isCurrentSourceVGA() &&
                InputSourceManager.getInstance().getCurrentChannelSourceName().equalsIgnoreCase("VGA")) {
            preferenceScreen.addPreference(util.createSwitchPreference(
                    MenuConfigManager.DPMS, R.string.menu_setup_dpms,
                    mTV.getConfigValue(MenuConfigManager.DPMS)==1 ? true :false));
        }

        // Blue Mute
        if (!CommonIntegration.isSARegion() && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_BLUE_MUTE_SUPPORT)) {
        String key = MenuConfigManager.BLUE_MUTE;
        boolean isChecked = util.mConfigManager.getDefault(key) == 1 ? true
                : false;
        preferenceScreen.addPreference(util.createSwitchPreference(key,
                R.string.menu_setup_bluemute, isChecked));
        }

        //Power On Channel
        if (!CommonIntegration.isUSRegion() &&
            MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_POWER_ON_CHANNEL_SUPPORT) &&
            (CommonIntegration.getInstance().isCurrentSourceTv()||
            CommonIntegration.getInstance().isCurrentSourceDTV()||
            CommonIntegration.getInstance().isCurrentSourceATV() ||
            CommonIntegration.getInstance().isCurrentSourceComposite())) {
            preferenceScreen.addPreference(util.createPreference(
                    MenuConfigManager.SETUP_POWER_ON_CH,
                    R.string.menu_setup_power_on_channel));
        }

        //DivX Registration
        /*if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DIVX_SUPPORT)){
            DivxDialog divxrdialog = new DivxDialog(themedContext);
            divxrdialog.setItemId(MenuConfigManager.DIVX_REG);
            preferenceScreen.addPreference(util.createDialogPreference(
                    MenuConfigManager.SETUP_DIVX_REGISTRATION,
                    R.string.menu_setup_divx_registration,
                    divxrdialog));
        }*/

        //DivX Deactivation
       /* if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DIVX_SUPPORT)){
            DivxDialog divxdeadialog = new DivxDialog(themedContext);
            preferenceScreen.addPreference(util.createDialogPreference(
                    MenuConfigManager.SETUP_DIVX_DEACTIVATION,
                    R.string.menu_setup_divx_deactivation,
                    divxdeadialog));
        }*/

        if (1 == VendorProperties.sys_samba_acr().orElse(0)) {
            Intent samba = new Intent("tv.samba.action.LAUNCH_SAMBA_TV_SETTINGS_ACTIVITY");
            samba.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            preferenceScreen.addPreference(util.createPreference(MenuConfigManager.SAMBA_INTERACTIVE_TV,
            R.string.menu_samba_setting,samba));
        }


        boolean istwn=TVContent.getInstance(mContext).isOneImageTWN() ;
        boolean isphl=TVContent.getInstance(mContext).isOneImagePHL();
        boolean issurone = TVContent.getInstance(mContext).isSupportOneImage();
        boolean iscol  = TVContent.getInstance(mContext).isCOLRegion();
        //Ginga Setup
        if( DataSeparaterUtil.getInstance() != null
                && DataSeparaterUtil.getInstance().isSupportGinga()
                && CommonIntegration.isSARegion()){
            if(issurone && !isphl){
                    preferenceScreen.addPreference(util.createPreference(
                            MenuConfigManager.GINGA_SETUP, R.string.menu_setup_ginga_setup));
            }else if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GINGA)
                    && mTV.isCurrentSourceDTV()) {
                preferenceScreen.addPreference(util.createPreference(
                    MenuConfigManager.GINGA_SETUP, R.string.menu_setup_ginga_setup));
            }
        }
        

        if (CommonIntegration.isEURegion()) {
            //Interaction Channel
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SET_INTERACTION_CH_SUPP)
                && DataSeparaterUtil.getInstance().isSupportMheg5()){

                if(isTVSource() || CommonIntegration.getInstance().isCurrentSourceATV() ||
                        CommonIntegration.getInstance().isCurrentSourceDTV()) {
                    preferenceScreen.addPreference(util.createSwitchPreference(
                            MenuConfigManager.INTERACTION_CHANNEL, R.string.menu_setup_interaction_channel,
                            getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.INTERACTION_CHANNEL))));
                }
            }

            if (!istwn&&MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MHEG5)
                && DataSeparaterUtil.getInstance().isSupportMheg5()) {
                //MHEG PIN Protection
                if (DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportMheg5()){
                    if(isTVSource() || CommonIntegration.getInstance().isCurrentSourceATV() ||
                            CommonIntegration.getInstance().isCurrentSourceDTV()) {
                        preferenceScreen.addPreference(util.createSwitchPreference(
                                MenuConfigManager.MHEG_PIN_PROTECTION, R.string.menu_setup_MHEG_PIN_Protection,
                                getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.MHEG_PIN_PROTECTION))));
                    }
                }
            }


//            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA)) {
//                //MHEG PIN Protection
//                preferenceScreen.addPreference(util.createSwitchPreference(
//                        MenuConfigManager.OCEANIA_FREEVIEW, R.string.menu_setup_oceania_freeview,
//                        getDefaultBoolean(mConfigManager.getDefault(MenuConfigManager.OCEANIA_FREEVIEW))));
//            }

            if (!iscol&&!istwn&&MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_HBBTV)
                && DataSeparaterUtil.getInstance().isSupportHbbtv()) {
                if (DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportHbbtv()){
                //HBBTV Support
                    if(isTVSource() || CommonIntegration.getInstance().isCurrentSourceATV() ||
                            CommonIntegration.getInstance().isCurrentSourceDTV()) {
                        //HBBTV Support
                        preferenceScreen.addPreference(
                                util.createPreference(MenuConfigManager.SETUP_HBBTV, R.string.menu_setup_HBBTV_settings));
                    }
                }
            }
            if(mTV.isUKCountry() &&
                (CommonIntegration.getInstance().isCurrentSourceTv()||
                CommonIntegration.getInstance().isCurrentSourceDTV()||
                CommonIntegration.getInstance().isCurrentSourceATV())){
                preferenceScreen.addPreference(util.createPreference(MenuConfigManager.FREEVIEW_SETTING, R.string.menu_string_freeview_setting));
            }

            //Subtitle
            if(isTVSource() || CommonIntegration.getInstance().isCurrentSourceATV() ||
                    CommonIntegration.getInstance().isCurrentSourceDTV() || CommonIntegration.getInstance().isCurrentSourceComposite()) {
                preferenceScreen.addPreference(
                        util.createPreference(MenuConfigManager.SUBTITLE_GROUP, R.string.menu_setup_subtitle));
            }
        }

        if (CommonIntegration.isEURegion() || CommonIntegration.isCNRegion()){
            //Teletext
            if (!istwn&&DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportTeletext()){
                if(isTVSource() || CommonIntegration.getInstance().isCurrentSourceATV() ||
                        CommonIntegration.getInstance().isCurrentSourceDTV()|| CommonIntegration.getInstance().isCurrentSourceComposite()) {
                    preferenceScreen.addPreference(
                            util.createPreference(MenuConfigManager.SETUP_TELETEXT, R.string.menu_setup_teletext));
                }
            }
        }


        if (MarketRegionInfo.isOadAvailable()&& SaveValue.readLocalMemoryBooleanValue("is_show_OAD_ui")) {
                Preference oadPreference = util.createPreference(MenuConfigManager.SETUP_OAD_SETTING, R.string.menu_setup_oad_setting);
                preferenceScreen.addPreference(oadPreference);
        }

        // BISS Key
        if(CommonIntegration.isEURegion() && isSupportBissKey()
                && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_BISS_KEY)
                && mTV.isTurkeyCountry()){
            bissPreference=  util.createPreference(
                    MenuConfigManager.BISS_KEY, R.string.menu_setup_biss_key);
            preferenceScreen.addPreference(bissPreference);
            int tunerMode = CommonIntegration.getInstance().getSvl();
             if (tunerMode == 3 || tunerMode == 4) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");// general satellite
              }else{
                preferenceScreen.removePreference(bissPreference);
              }
        }

        //postal code
         if(CommonIntegration.isEURegion() && (mTV.isIDNCountry()
                || (MtkTvConfig.getInstance().getCountry()
                        .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_IDN)
                        && 1 == VendorProperties.mtk_system_ews_support().orElse(0)))){
            String initVal = MtkTvConfig.getInstance().getConfigString(CommonIntegration.OCEANIA_POSTAL);
            PostalCodeEditDialog postalCodedialog=new PostalCodeEditDialog(themedContext,mContext.getResources().getString(R.string.menu_setup_postal_code_setting),MenuConfigManager.SETUP_POSTAL_CODE);
            postalCodedialog.setLength(5);
            Preference  postalCodeference=  util.createDialogPreference(
                    MenuConfigManager.SETUP_POSTAL_CODE,
                    R.string.menu_setup_postal_code_setting, postalCodedialog);
            postalCodeference.setSummary(initVal);
            postalCodedialog.setPreference( postalCodeference);
            preferenceScreen.addPreference( postalCodeference);
        }
        //remove to tv options
        /*//Record Setting
        if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SET_RECORD_SETTING_SUPP)){
            if (DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportPvr()){
                preferenceScreen.addPreference(util.createPreference(
                        MenuConfigManager.SETUP_RECORD_SETTING, R.string.menu_setup_record_setting));
            }
        }*/

      //POWER OFF Setting ==> move to TvSettingsPlus 5/6
//        if (DataSeparaterUtil.getInstance() != null&&DataSeparaterUtil.getInstance().getValueAutoSleep()!=1 ) {
//        String[] powerTime = mContext.getResources().getStringArray(
//                R.array.menu_setting_power_time);
//            powerTime = addSuffix(powerTime, R.string.menu_arrays_power_x_hours);
//        preferenceScreen.addPreference(util.createListPreference(MenuConfigManager.POWER_SETTING_CONFIG_VALUE,
//                R.string.menu_string_power_setting, true, powerTime,
//                util.mConfigManager.getDefaultPowerSetting(themedContext)));
//        }


        if (1 == VendorProperties.sys_alphonso_acr().orElse(0)) {
            //Automatic content Recognition
            //int def = SaveValue.getInstance(themedContext).readValue(MenuConfigManager.AUTOMATIC_CONTENT);
            SaveValue save = SaveValue.getInstance(mContext);
			int def = save.readWorldIntValue(mContext,"persist.vendor.sys.alphonso.acr");
            Log.d("ACR", "def:"+def);
            preferenceScreen.addPreference(util.createSwitchPreference(
                    MenuConfigManager.AUTOMATIC_CONTENT, R.string.menu_setup_automatic_content,
                    getDefaultBoolean(def)));
			int defRecommendations = save.readWorldIntValue(mContext,"persist.vendor.sys.alphonso.acr.recommendations");
            Preference recommendations = util.createSwitchPreference(
                    MenuConfigManager.RECOMMENDATIONS, R.string.menu_setup_recommendations,
                    getDefaultBoolean(defRecommendations));
                    recommendations.setEnabled(getDefaultBoolean(def));
            preferenceScreen.addPreference(recommendations);
        }

        if (CommonIntegration.isEURegion() && !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK)) {
            //TKGSSetting
            boolean satOperOnly = CommonIntegration.getInstance().isPreferSatMode();

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TKGSSetting:"+satOperOnly+ mTV.isTurkeyCountry()+ mTV.isTKGSOperator());
            if (!satOperOnly || !mTV.isTurkeyCountry()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");///need not
            }
            else {
                Intent mTKGSintent = new Intent(mContext,TKGSSettingActivity.class);
                tkgsSetting=util.createPreference(
                        MenuConfigManager.TKGS_SETTING, R.string.menu_tkgs,mTKGSintent);
                preferenceScreen.addPreference(tkgsSetting);
            }
            if((CommonIntegration.getInstance().isCurrentSourceTv()||
                    CommonIntegration.getInstance().isCurrentSourceDTV()||
                    CommonIntegration.getInstance().isCurrentSourceATV())) {
                preferenceScreen.addPreference(util.createPreference(MenuConfigManager.SYSTEM_INFORMATION,
                        R.string.menu_setup_system_info));
            }
        }

        WindowManager.LayoutParams lp = TurnkeyUiMainActivity.getInstance().getWindow().getAttributes();
        lp.width = (int) (SettingsUtil.SCREEN_WIDTH * 0.89);
        lp.height = (int) (SettingsUtil.SCREEN_HEIGHT * 0.94);

        Intent i = new Intent();
        i.setComponent(new ComponentName("tv.samba.ssm", "tv.samba.ssm.activities.SambaTVSettingsActivity"));
        i.putExtra("tv.samba.ssm.SETTINGS_TITLE", "Samba Interactive TV");
        i.putExtra("tv.samba.ssm.SETTINGS_TITLE_BREADCRUMB", "Menu Setup");
        i.putExtra("tv.samba.ssm.AUTH_TOKEN", "94e3e9edce62ec09ef3459576c513c8d76a4d8e614b0cb6e");
        i.putExtra("tv.samba.ssm.LAYOUT_PARAMS", lp);

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_ACR_SUPPORT)) {
            preferenceScreen.addPreference(util.createPreference(
                    "Samba_Settings", R.string.menu_samba_setting, i));
        }

    }

    private String[] addSuffix(String[] str,int strId) {
        if(mContext == null){
            return null;
        }
        for (int i = 0; i < str.length; i++) {
            String numStr = str[i];
            int num = numStr == null || numStr.isEmpty() || !numStr.matches("^[0-9]*$") ?
                    Integer.MAX_VALUE :Integer.parseInt(numStr);
            if(num != Integer.MAX_VALUE){
                str[i] = mContext.getResources().getString(strId,num);
            }
        }
        return str;
    }

    private PreferenceScreen addParentalChannelBlock(PreferenceScreen preferenceScreen, Context themedContext){
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(mContext).getTVChannelList();
        // fix CR DTV00580532
        if (mTV.isCurrentSourceTv()
            && (CommonIntegration.getInstance().hasActiveChannel())) {
            preferenceScreen.setEnabled(true);
        } else {
            preferenceScreen.setEnabled(false);
        }

        if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
            int currentChannelID = EditChannel.getInstance(themedContext).getCurrentChannelId();
            boolean isCurrentBlock = EditChannel.getInstance(themedContext).isChannelBlock(currentChannelID);
            EditChannel.getInstance(themedContext).blockChannel(currentChannelID, !isCurrentBlock);
        }
        for (MtkTvChannelInfoBase infobase : list) {
            int tid = infobase.getChannelId();
          //  EditChannel.getInstance(themedContext).isChannelBlock(infobase.getChannelId()) ? 1 : 0;
            String name = "" + mDataHelper.getDisplayChNumber(tid) + "        "
                    + ((infobase.getServiceName() == null) ? "" : infobase.getServiceName());
            Preference channelblock = util.createSwitchPreference(MenuConfigManager.PARENTAL_CHANNEL_BLOCK_CHANNELLIST
                    + ":" + infobase.getChannelId(), name, EditChannel.getInstance(themedContext).isChannelBlock(infobase.getChannelId()));
            if (infobase instanceof MtkTvAnalogChannelInfo) {
                channelblock.setSummary("ATV");
            }else{
                channelblock.setSummary("DTV");
            }
            preferenceScreen.addPreference(channelblock);
        }
        return preferenceScreen;
    }

/*
    private void addParentalSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // pwd
        PinDialog dialog = new PinDialog(
                PinDialog.PIN_DIALOG_TYPE_ENTER_PIN, null, mContext);
        Preference preference = util.createDialogPreference(
                MenuConfigManager.PARENTAL_ENTER_PASSWORD, "Password Dialog", dialog);
        preferenceScreen.addPreference(preference);

        // channel block
//        Intent intent = new Intent(mContext, ChannelInfoActivity.class);
//        String mItem = MenuConfigManager.PARENTAL_CHANNEL_BLOCK_CHANNELLIST;
//        TransItem trans = new TransItem(mItem, "",
//                MenuConfigManager.INVALID_VALUE,
//                MenuConfigManager.INVALID_VALUE,
//                MenuConfigManager.INVALID_VALUE);
//        intent.putExtra("TransItem", trans);
//        intent.putExtra("ActionID", MenuConfigManager.PARENTAL_CHANNEL_BLOCK);
        Preference cBPreference = util.createPreference(MenuConfigManager.PARENTAL_CHANNEL_BLOCK,
                R.string.menu_parental_channel_block);
//                intent);
//        boolean enable = false;
//        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
//            List<TIFChannelInfo> tif_list = TIFChannelManager.getInstance(mContext).
//                    queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
//            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "support TIf :tif_list.size(): " + tif_list.size());
//            if (tif_list.size() > 0) {
//                enable = true;
//            }
//        }
//        cBPreference.setVisible(false);
//        cBPreference.setEnabled(enable);
        if (mTV.isCurrentSourceTv()
                && (CommonIntegration.getInstance().hasActiveChannel())) {
            cBPreference.setEnabled(true);
        } else {
            cBPreference.setEnabled(false);
        }
        cBPreference.setVisible(false);
        preferenceScreen.addPreference(cBPreference);

        // program block
        if (!CommonIntegration.isCNRegion()) {
            Preference pBPreference = util.createPreference(MenuConfigManager.PARENTAL_PROGRAM_BLOCK,
                    R.string.menu_parental_program_block);
            pBPreference.setVisible(false);
            preferenceScreen.addPreference(pBPreference);
        }

        // Channel Schedule Block
        if (CommonIntegration.isSARegion()) {// need to fix. should remove '!'
            Preference cSBPreference = util.createPreference(
                    MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK,
                    R.string.menu_parental_channel_schedule_block);
            if (mTV.isCurrentSourceTv()
                    && (CommonIntegration.getInstance().hasActiveChannel())) {
                cSBPreference.setEnabled(true);
            } else {
                cSBPreference.setEnabled(false);
            }
            cSBPreference.setVisible(false);
            preferenceScreen.addPreference(cSBPreference);
        }

        // Input Block
        Preference inputBlockPreference = util.createPreference(
                MenuConfigManager.PARENTAL_INPUT_BLOCK, R.string.menu_parental_input_block);
        inputBlockPreference.setVisible(false);
        preferenceScreen.addPreference(inputBlockPreference);

        // Change Password
        PinDialog passwordChangeDialog = new PinDialog(
                PinDialog.PIN_DIALOG_TYPE_NEW_PIN, null, mContext);
        Preference passwordChangePreference = util.createDialogPreference(
                MenuConfigManager.PARENTAL_CHANGE_PASSWORD, R.string.menu_parental_change_password,
                passwordChangeDialog);
        passwordChangePreference.setVisible(false);
        preferenceScreen.addPreference(passwordChangePreference);

        // Clean All
        Preference cleanAllPreference = util.createClickPreference(
                MenuConfigManager.PARENTAL_CLEAN_ALL, R.string.menu_parental_clean_all,
                FacSetup.getInstance(mContext));
        cleanAllPreference.setVisible(false);
        preferenceScreen.addPreference(cleanAllPreference);
    }
*/
    private void addFactoryVideoSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        String[] mFvDIMA = mContext.getResources().getStringArray(
                R.array.menu_factory_auto_dima_array);

        String[] mFvDIEdge = mContext.getResources().getStringArray(
                R.array.menu_factory_video_diedge_array);
/*
        // . auto color
        preference = util.createClickPreference(
                MenuConfigManager.FV_AUTOCOLOR,
                R.string.menu_factory_video_auto_color,
                FacVideo.getInstance(mContext));

        // when VGA,available,other,can not use
        if ((mTV.isCurrentSourceVGA()
                || mTV.isCurrentSourceComponent()
                || mTV.isCurrentSourceScart())
                && mTV.iCurrentInputSourceHasSignal()) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }
        //preferenceScreen.addPreference(preference);

        // . ColorTemperature
        preference = util.createPreference(
                MenuConfigManager.FV_COLORTEMPERATURE,
                R.string.menu_factory_video_color_temperature);
        //preferenceScreen.addPreference(preference);

        // AUTO PHASE only component source and has signal
        if (mTV.isCurrentSourceComponent()
                && mTV.iCurrentInputSourceHasSignal()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceComponent");
            // . auto phase
             Preference autoPhase = util.createClickPreference(MenuConfigManager.FV_AUTOPHASE,
                     R.string.menu_factory_video_auto_phase);
             //preferenceScreen.addPreference(autoPhase);

            autoPhase.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                 @Override
                 public boolean onPreferenceClick(Preference preference) {
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceClick " + preference);
                     autoAdjustInfo(mContext.getString(R.string.menu_video_auto_adjust_info));
                     Message message = mHandler.obtainMessage();
                     message.obj = preference;
                     if (preference.getKey().equals(MenuConfigManager.AUTO_ADJUST)) {
                         appTV.setAutoClockPhasePosition(mNavIntegration.getCurrentFocus());
                         message.what = MessageType.MESSAGE_AUTOADJUST;
                     } else if (preference.getKey().equals(MenuConfigManager.FV_AUTOCOLOR)) {
                         appTV.setAutoColor(mNavIntegration.getCurrentFocus());
                         message.what = MessageType.MESSAGE_AUTOCOLOR;
                     } else {
                         appTV.setAutoClockPhasePosition(mNavIntegration.getCurrentFocus());
                         message.what = MessageType.MESSAGE_AUTOADJUST;
                     }
                     mHandler.sendMessageDelayed(message,
                             MessageType.delayMillis6);

                     mConfigManager.setValueDefault(preference.getKey());
                     return true;
                 }
             });
             */

/*            preference = util.createDialogPreference(
                    MenuConfigManager.FV_AUTOPHASE,
                    R.string.menu_factory_video_auto_phase,
                    null);
            preferenceScreen.addPreference(preference);
*/
/*            // . ypbpr phase
            preference = util.createProgressPreference(
                    MenuConfigManager.FV_YPBPR_PHASE,
                    R.string.menu_factory_video_phase,
                    false);
            //preferenceScreen.addPreference(preference);

            // . H POSITION
            ItemName = MenuConfigManager.FV_HPOSITION;
            Preference tmp1 = util.createProgressPreference(
                    ItemName,
                    R.string.menu_factory_video_h_position,
                    false);
            //preferenceScreen.addPreference(tmp1);

            // . V POSITION
            ItemName = MenuConfigManager.FV_VPOSITION;
            Preference tmp2 = util.createProgressPreference(
                    ItemName,
                    R.string.menu_factory_video_v_position,
                    false);
            //preferenceScreen.addPreference(tmp2);

            if (SCREEN_MODE_DOT_BY_DOT == mTV.getConfigValue(
                    MenuConfigManager.SCREEN_MODE)) {
                tmp1.setEnabled(false);
                tmp2.setEnabled(false);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dfasdfdgdgdgdgdd");
            }
        }

        // only TV and have channel,show H POSITION and V POSITION
        if (mTV.isCurrentSourceTv() && mTV.iCurrentInputSourceHasSignal()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceTv");
            // . H POSITION
            ItemName = MenuConfigManager.FV_HPOSITION;
            Preference tmp1 = util.createProgressPreference(
                    ItemName,
                    R.string.menu_factory_video_h_position,
                    false);
            //preferenceScreen.addPreference(tmp1);

            // . V POSITION
            ItemName = MenuConfigManager.FV_VPOSITION;
            Preference tmp2 = util.createProgressPreference(
                    ItemName,
                    R.string.menu_factory_video_v_position,
                    false);
            //preferenceScreen.addPreference(tmp2);

            if (SCREEN_MODE_DOT_BY_DOT == mTV.getConfigValue(
                    MenuConfigManager.SCREEN_MODE)) {
                tmp1.setEnabled(false);
                tmp2.setEnabled(false);
            }
        }

        // VGA have no DIMA,DI EDGE,WCG
        if (!mTV.isCurrentSourceVGA() && !CommonIntegration.isSARegion()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceVGA");
            // . DIMA
            ItemName = MenuConfigManager.FV_DIMA;
            preference = util.createListPreference(
                    ItemName,
                    R.string.menu_factory_video_di_ma,
                    true,
                    mFvDIMA,
                    util.mConfigManager.getDefault(ItemName) -
                            util.mConfigManager.getMin(ItemName));
            //preferenceScreen.addPreference(preference);

            // . DIEdge DI border
            ItemName = MenuConfigManager.FV_DIEDGE;
            preference = util.createListPreference(
                    ItemName,
                    R.string.menu_factory_video_di_edge,
                    true,
                    mFvDIEdge,
                    util.mConfigManager.getDefault(ItemName) -
                            util.mConfigManager.getMin(ItemName));

            //preferenceScreen.addPreference(preference);

            if (CommonIntegration.isEURegion() || CommonIntegration.isCNRegion()) {
                // . XVYCC
                ItemName = MenuConfigManager.FV_VIDEO_VID_XVYCC;
                preference = util.createSwitchPreference(
                        ItemName,
                        R.string.menu_video_xvycc,
                        util.mConfigManager.getDefault(ItemName)
                        == 1 ? true : false);

                preference.setVisible(mTV.isConfigVisible(
                        MenuConfigManager.CFG_MENU_XVYCC));
                //preferenceScreen.addPreference(preference);

                // . WCG
                ItemName = MenuConfigManager.FV_WCG;
                preferenceScreen.addPreference(util.createSwitchPreference(
                        ItemName,
                        R.string.menu_factory_video_wcg,
                        util.mConfigManager.getDefault(ItemName)
                        == 1 ? true : false));
            }
        }
        */

        // . Flip
        ItemName = MenuConfigManager.FV_FLIP;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_video_flip,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // . Mirror
        ItemName = MenuConfigManager.FV_MIRROR;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_video_mirror,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));
/*
        // . Local dimming
        if (CommonIntegration.isUSRegion()) {
            String[] mLocalDim = mContext.getResources().getStringArray(
                    R.array.menu_factory_video_local_array);

            ItemName = MenuConfigManager.FV_LOCAL_DIMMING;
            preference = util.createSwitchPreference(
                    ItemName,
                    R.string.menu_factory_video_local_dimming,
                    util.mConfigManager.getDefault(ItemName) == 1 ? true : false);

           // preferenceScreen.addPreference(preference);
        }
        */
    }

    private void addFactoryAudioSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        Preference preference;

        String[] mFaCompression = mContext.getResources().getStringArray(
                R.array.menu_factory_audio_compression_array);

        String[] mFaCompressionFactor = mContext.getResources().getStringArray(
                R.array.menu_factory_audio_compressionfactor_array);
        if (CommonIntegration.isCNRegion()) {
            mFaCompressionFactor = mContext.getResources().getStringArray(
                    R.array.menu_factory_audio_compressionfactor_array_cn);
        }

        // Dolby Banner
        ItemName = MenuConfigManager.FA_DOLBYBANNER;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_dolby_banner,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // Compression
        ItemName = MenuConfigManager.FA_COMPRESSION;
        preference = util.createListPreference(
                ItemName,
                R.string.menu_factory_audio_compression,
                true,
                mFaCompression,
                util.mConfigManager.getDefault(ItemName));
        preferenceScreen.addPreference(preference);

        // CompressionFactor
        ItemName = MenuConfigManager.FA_COMPRESSIONFACTOR;
        preference = util.createListPreference(
                ItemName,
                R.string.menu_factory_audio_compression_factor,
                true,
                mFaCompressionFactor,
                util.mConfigManager.getDefault(ItemName));
        preferenceScreen.addPreference(preference);

//        if (CommonIntegration.isUSRegion()) {
//            //**
//             * MTS system
//             */
//            preference = util.createPreference(
//                    MenuConfigManager.FA_MTS_SYSTEM,
//                    R.string.menu_factory_audio_mts_system);
//            preferenceScreen.addPreference(preference);
//        }
//        else if (CommonIntegration.isEURegion() || CommonIntegration.isCNRegion()) {
//            //**
//             * A2 system
//             */
//            preference = util.createPreference(
//                    MenuConfigManager.FA_A2SYSTEM,
//                    R.string.menu_factory_audio_a2_system);
//            preferenceScreen.addPreference(preference);
//
//            //**
//             * PAL system
//             */
//            preference = util.createPreference(
//                    MenuConfigManager.FA_PALSYSTEM,
//                    R.string.menu_factory_audio_pal_system);
//            preferenceScreen.addPreference(preference);
//
//            *//**
//             * EU system
//             */
//            preference = util.createPreference(
//                    MenuConfigManager.FA_EUSYSTEM,
//                    R.string.menu_factory_audio_eu_system);
//            preferenceScreen.addPreference(preference);
//        }
//
//        // LATENCY
//        ItemName = MenuConfigManager.FA_LATENCY;
//        preferenceScreen.addPreference(util.createProgressPreference(
//                ItemName,
//                R.string.menu_factory_audio_latency,
//                false));
    }

//    private void addFactoryTVSettings(PreferenceScreen preferenceScreen, Context themedContext) {
//        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
//        Preference preference;
//        Intent intent;
//        boolean isPVRSrt = mTV.isPVrRunning();
//        boolean isTSSrt = mTV.isTshitRunning();
//        boolean isDvrPlaying = StateDvrPlayback.getInstance() != null
//                && StateDvrPlayback.getInstance().isRunning();
//        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "isDvrPlaying = " + isDvrPlaying);
//        // TV
//        if (CommonIntegration.isUSRegion()) {
//            // range scan
//            ItemName = MenuConfigManager.FACTORY_TV_RANGE_SCAN;
//
//            intent = new Intent(mContext, ScanViewActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            preference = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_range_scan,
//                    intent);
//            preference.setEnabled(!(isPVRSrt || isTSSrt || isDvrPlaying));
//            preferenceScreen.addPreference(preference);
//
//            // single rf scan
//            ItemName = MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN;
//
//            intent = new Intent(mContext, ScanViewActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            preference = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_single_rf_scan,
//                    intent);
//            preference.setEnabled(!(isPVRSrt || isTSSrt || isDvrPlaying));
//            preferenceScreen.addPreference(preference);
//
//            // factory scan
//            ItemName = MenuConfigManager.FACTORY_TV_FACTORY_SCAN;
//
//            intent = new Intent(mContext, ScanDialogActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            preference = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_factory_scan,
//                    intent);
//            preference.setEnabled(!(isPVRSrt || isTSSrt || isDvrPlaying));
//            preferenceScreen.addPreference(preference);
//
//        } else if (CommonIntegration.isSARegion()) {
//            // digital range scan
//            ItemName = MenuConfigManager.FACTORY_TV_RANGE_SCAN_DIG;
//
//            intent = new Intent(mContext, ScanViewActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            Preference facTVDigitalChannelScan = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_digital_channel_scan,
//                    intent);
//            facTVDigitalChannelScan.setEnabled(!(isPVRSrt || isTSSrt ||
//                    (mTV.getCurrentTunerMode() >= 1) || isDvrPlaying));
//            preferenceScreen.addPreference(facTVDigitalChannelScan);
//
//            // analog range scan
//            ItemName = MenuConfigManager.FACTORY_TV_RANGE_SCAN_ANA;
//
//            intent = new Intent(mContext, ScanViewActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            preference = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_analog_channel_scan,
//                    intent);
//            preference.setEnabled(!(isPVRSrt || isTSSrt || isDvrPlaying));
//            preferenceScreen.addPreference(preference);
//
//            // single rf scan
//            ItemName = MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN;
//
//            intent = new Intent(mContext, ScanViewActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            preference = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_single_rf_scan,
//                    intent);
//            preference.setEnabled(!(isPVRSrt || isTSSrt ||
//                    (mTV.getCurrentTunerMode() >= 1) || isDvrPlaying));
//            if (mTV.getCurrentTunerMode() >= 1) {
//                facTVDigitalChannelScan.setEnabled(false);
//                preference.setEnabled(false);
//            }
//            preferenceScreen.addPreference(preference);
//
//        } else if (CommonIntegration.isEURegion()) {
//            // digital range scan
//            ItemName = MenuConfigManager.FACTORY_TV_RANGE_SCAN_DIG;
//
//            intent = new Intent(mContext, ScanViewActivity.class);
//            intent.putExtra("ActionID", ItemName);
//
//            preference = util.createPreference(ItemName,
//                    R.string.menu_factory_TV_digital_channel_scan,
//                    intent);
//            preference.setEnabled(!(isPVRSrt || isTSSrt || isDvrPlaying));
//            if (mTV.getCurrentTunerMode() >= 1) {
//                preference.setEnabled(false);
//            }
//            preferenceScreen.addPreference(preference);
//        }
//
//        if (CommonIntegration.isEURegion() || CommonIntegration.isUSRegion() || CommonIntegration.isCNRegion()) {
//            // tuner diagnostic
//            preference = util.createPreference(
//                    MenuConfigManager.FACTORY_TV_TUNER_DIAGNOSTIC,
//                    R.string.menu_factory_TV_tunerdiag);
//            preferenceScreen.addPreference(preference);
//            if (TifTimeShiftManager.getInstance() != null
//                    && TifTimeShiftManager.getInstance().isAvailable()) {
//                preference.setEnabled(false);
//            }else {
//                preference.setEnabled(true);
//            }
//        }
//    }

    private void addFactorySetupSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        String[] mFvEventForm = mContext.getResources().getStringArray(
                R.array.menu_factory_setup_eventform);

        // --clean storage
//        Preference cleanStorage = util.createClickPreference(
//                MenuConfigManager.FACTORY_SETUP_CLEAN_STORAGE,
//                R.string.menu_factory_setup_cleanstorage,
//                FacSetup.getInstance(mContext));

        // --burning mode
       // ItemName = MenuConfigManager.FACTORY_SETUP_BURNING_MODE;
       // Preference burningMode = util.createSwitchPreference(
        //        ItemName,
        //        R.string.menu_factory_setup_burningmode,
        //        util.mConfigManager.getDefault(ItemName) == 1 ? true : false);

        // --uart mode
        /*ItemName = MenuConfigManager.FACTORY_SETUP_UART_MODE;
        Preference uartMode = util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_setup_uart_factory_mode,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false);*/

       /* if (CommonIntegration.isUSRegion()) {
            //preferenceScreen.addPreference(burningMode);
//            preferenceScreen.addPreference(cleanStorage);
//            preferenceScreen.addPreference(uartMode);
        } else*/ if (CommonIntegration.isSARegion()) {
           // preferenceScreen.addPreference(burningMode);
//            preferenceScreen.addPreference(uartMode);
//            preferenceScreen.addPreference(cleanStorage);

            // event form
            //ItemName = MenuConfigManager.FACTORY_SETUP_DATA_SERVICE_SUPPORT;
            //preferenceScreen.addPreference(util.createSwitchPreference(
             //       ItemName,
            //        R.string.menu_factory_setup_data_service_support,
           //         util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

            // setup caption
            ItemName = MenuConfigManager.FACTORY_SETUP_CAPTION;
            preferenceScreen.addPreference(util.createPreference(
                    ItemName,
                    R.string.menu_factory_setup_caption));
        } else if (CommonIntegration.isEURegion()) {
        //remove CN region for CI ,suggested by xuefei zhao
            // event form
            ItemName = MenuConfigManager.FACTORY_SETUP_EVENT_FORM;
            preferenceScreen.addPreference(util.createListPreference(
                    ItemName,
                    R.string.menu_factory_setup_eventform,
                    true,
                    mFvEventForm,
                    util.mConfigManager.getDefault(ItemName)));

            // ci update
            ItemName = MenuConfigManager.FACTORY_SETUP_CI_UPDATE;
            Preference updateCi = util.createClickPreference(
                    ItemName,
                    R.string.menu_factory_setup_ci_update,
                    mFacSetup);
            // ci ecp update
            ItemName = MenuConfigManager.FACTORY_SETUP_CI_ECP_UPDATE;
            Preference updateCi1 = util.createClickPreference(
                    ItemName,
                    R.string.menu_factory_setup_ci_ecp_update,
                    mFacSetup);

            // ci update
            ItemName = MenuConfigManager.FACTORY_SETUP_CI_ERASE;
            Preference eraseCi = util.createClickPreference(
                    ItemName,
                    R.string.menu_factory_setup_ci_erase,
                    mFacSetup);

            // ci query
            /*
             * ItemName = MenuConfigManager.FACTORY_SETUP_CI_QUERY; Preference eraseCi =
             * util.createClickPreference( ItemName, R.string.menu_factory_setup_ci_query,
             * FacSetup.getInstance(mContext));
             */

            // burning mode
            //preferenceScreen.addPreference(burningMode);

            if (CommonIntegration.isEURegion()) {
//                preferenceScreen.addPreference(uartMode);
                preferenceScreen.addPreference(updateCi);
                preferenceScreen.addPreference(updateCi1);
                preferenceScreen.addPreference(eraseCi);
            }

//            preferenceScreen.addPreference(cleanStorage);

            if (CommonIntegration.isEURegion()) {
                // TKGS
                 ItemName = MenuConfigManager.TKGS_FAC_SETUP_AVAIL_CONDITION;
                 String[] tkgsconds = new String[] {"Normal", "Certification"};
                 preferenceScreen.addPreference(util.createListPreference( ItemName,
                 "TKGS Availability Condition", true, tkgsconds,
                 util.mConfigManager.getDefault(ItemName)));
            }
        }
    }

   /* private void addFactoryPresetSettings(PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // preset ch
        preferenceScreen.addPreference(util.createClickPreference(
                MenuConfigManager.FACTORY_PRESET_CH_DUMP,
                R.string.menu_factory_preset_dump,
                FacPreset.getInstance(mContext)));

        // preset ch
        preferenceScreen.addPreference(util.createClickPreference(
                MenuConfigManager.FACTORY_PRESET_CH_PRINT,
                R.string.menu_factory_preset_print,
                FacPreset.getInstance(mContext)));

        // preset ch
        preferenceScreen.addPreference(util.createClickPreference(
                MenuConfigManager.FACTORY_PRESET_CH_RESTORE,
                R.string.menu_factory_preset_restore,
                FacPreset.getInstance(mContext)));
    }*/

    private PreferenceScreen addFacVidColorTmp(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        Preference preference;
        String[] mFvColor = mContext.getResources().getStringArray(
                R.array.menu_factory_video_colortemperature_array);

        // Color Temperature
        ItemName = MenuConfigManager.FV_COLORTEMPERATURECHILD;
        preference = util.createListPreference(
                ItemName,
                R.string.menu_factory_video_color_temperature,
                true,
                mFvColor,
                util.mConfigManager.getDefault(ItemName) -
                        util.mConfigManager.getMin(ItemName));
        preferenceScreen.addPreference(preference);

        // R Gain
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.FV_COLOR_G_R,
                R.string.menu_factory_video_color_rgain,
                false));
        preferenceScreen.addPreference(preference);

        // G Gain
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.FV_COLOR_G_G,
                R.string.menu_factory_video_color_ggain,
                false));
        preferenceScreen.addPreference(preference);

        // B Gain
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.FV_COLOR_G_B,
                R.string.menu_factory_video_color_bgain,
                false));
        preferenceScreen.addPreference(preference);

        // R Offset
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.FV_COLOR_O_R,
                R.string.menu_factory_video_color_roffset,
                false));
        preferenceScreen.addPreference(preference);

        // G Offset
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.FV_COLOR_O_G,
                R.string.menu_factory_video_color_goffset,
                false));
        preferenceScreen.addPreference(preference);

        // B Offset
        preferenceScreen.addPreference(util.createProgressPreference(
                MenuConfigManager.FV_COLOR_O_B,
                R.string.menu_factory_video_color_boffset,
                false));
        preferenceScreen.addPreference(preference);

        return preferenceScreen;
    }

    private PreferenceScreen addFacAudMts(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // numbers of check
        ItemName = MenuConfigManager.FAMTS_NUMBERSOFCHECK;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_numbersofcheck,
                false,
                30,
                80,
                util.mConfigManager.getDefault(ItemName)));

        // numbers of pilot
        ItemName = MenuConfigManager.FAMTS_NUMBERSOFPILOT;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_numbersofpilot,
                false,
                0,
                50,
                util.mConfigManager.getDefault(ItemName)));

        // pilot threshold high
        ItemName = MenuConfigManager.FAMTS_PILOT_THRESHOLD_HIGH;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_pilot_threshold_high,
                false));

        // pilot threshold low
        ItemName = MenuConfigManager.FAMTS_PILOT_THRESHOLD_LOW;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_pilot_threshold_low,
                false,
                80,
                150,
                util.mConfigManager.getDefault(ItemName)));

        // numbers of sap
        ItemName = MenuConfigManager.FAMTS_NUMBERSOFSAP;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_number_of_sap,
                false,
                0,
                50,
                util.mConfigManager.getDefault(ItemName)));

        // sap threshold high
        ItemName = MenuConfigManager.FAMTS_SAP_THRESHOLD_HIGH;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_sap_threshold_high,
                false));

        // sap threshold low
        ItemName = MenuConfigManager.FAMTS_SAP_THRESHOLD_LOW;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_sap_threshold_low,
                false,
                70,
                130,
                util.mConfigManager.getDefault(ItemName)));

        // high deviation mode
        ItemName = MenuConfigManager.FAMTS_HIGH_DEVIATION_MODE;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_mts_high_deviation_mode,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // carrier shift function
        ItemName = MenuConfigManager.FAMTS_CARRIER_SHIFT_FUNCTION;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_mts_carriershiftfunction,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // stauration mode
        ItemName = MenuConfigManager.FAMTS_FM_STAURATION_MODE;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_mts_fmstaurationmute,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // carrier mute mode
        ItemName = MenuConfigManager.FAMTS_FM_CARRIER_MUTE_MODE;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_fmcarriermutemode,
                false));

        // carrier mute threshold high
        ItemName = MenuConfigManager.FAMTS_FM_CARRIER_MUTE_THRESHOLD_HIGH;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_fmcarriermutethreshold_high,
                false,
                20,
                180,
                util.mConfigManager.getDefault(ItemName)));

        // carrier mute threshold low
        ItemName = MenuConfigManager.FAMTS_FM_CARRIER_MUTE_THRESHOLD_LOW;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_fmcarriermutethreshold_high,
                false,
                110,
                180,
                util.mConfigManager.getDefault(ItemName)));

        // mono stereo fine tune volume
        ItemName = MenuConfigManager.FAMTS_MONO_STERO_FINE_TUNE_VOLUME;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_mono_stero_finetunevolume,
                false,
                0,
                40,
                util.mConfigManager.getDefault(ItemName)));

        // numbers of pilot threshold high
        ItemName = MenuConfigManager.FAMTS_SAP_FINE_TUNE_VOLUME;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_mts_sap_finetunevolume,
                false,
                0,
                40,
                util.mConfigManager.getDefault(ItemName)));

        return preferenceScreen;
    }

    private PreferenceScreen addFacAudA2System(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // numbers of check
        ItemName = MenuConfigManager.FAA2_NUMBERSOFCHECK;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_numbersofcheck,
                false));

        // numbers of double
        ItemName = MenuConfigManager.FAA2_NUMBERSOFDOUBLE;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_numbersofdouble,
                false));

        // mono weight
        ItemName = MenuConfigManager.FAA2_MONOWEIGHT;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_monoweight,
                false));

        // stereo weight
        ItemName = MenuConfigManager.FAA2_STEREOWEIGHT;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_stereoweight,
                false));

        // dual weight
        ItemName = MenuConfigManager.FAA2_DUALWEIGHT;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_dualweight,
                false));

        // HIGH DEVIATION MODE
        ItemName = MenuConfigManager.FAA2_HIGHDEVIATIONMODE;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_a2_highdeviationmode,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // CARRIER SHIFT FUNCTION
        ItemName = MenuConfigManager.FAA2_CARRIERSHIFTFUNCTION;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_a2_carriershiftfunction,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // FM CARRIER MUTE MODE
        ItemName = MenuConfigManager.FAA2_FMCARRIERMUTEMODE;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_fmcarriermutemode,
                false));

        // FM CARRIER MUTE THRESHOLD HIGH
        ItemName = MenuConfigManager.FAA2_FMCARRIERMUTETHRESHOLDHIGH;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_fmcarriermutethreshold_high,
                false));

        // FM CARRIER MUTE THRESHOLD LOW
        ItemName = MenuConfigManager.FAA2_FMCARRIERMUTETHRESHOLDLOW;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_fmcarriermutethreshold_low,
                false));

        // FINE TUNE VOLUME
        ItemName = MenuConfigManager.FAA2_FINETUNEVOLUME;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_a2_finetunevolume,
                false));

        return preferenceScreen;
    }

    private PreferenceScreen addFacAudPalSystem(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // CORRECT THRESHOLD
        ItemName = MenuConfigManager.FAPAL_CORRECTTHRESHOLD;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_correctthreshold,
                false));

        // TOTAL SYNC LOOP
        ItemName = MenuConfigManager.FAPAL_TOTALSYNCLOOP;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_totalsyncloop,
                false));

        // ERROR THRESHOLD
        ItemName = MenuConfigManager.FAPAL_ERRORTHRESHOLD;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_errorthreshold,
                false));

        // PARITY ERRORT HRESHOLD
        ItemName = MenuConfigManager.FAPAL_PARITYERRORTHRESHOLD;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_parityerrorthreshold,
                false));

        // EVERY NUMBER FRAMES
        ItemName = MenuConfigManager.FAPAL_EVERYNUMBERFRAMES;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_everynumberframes,
                false));

        // HIGH DEVIATION MODE
        ItemName = MenuConfigManager.FAPAL_HIGHDEVIATIONMODE;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_pal_highdeviationmode,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // AM CARRIER MUTE MODE
        ItemName = MenuConfigManager.FAPAL_AMCARRIERMUTEMODE;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_amcarriermutemode,
                false));

        // AM CARRIER MUTE THRESHOLD HIGH
        ItemName = MenuConfigManager.FAPAL_AMCARRIERMUTETHRESHOLDHIGH;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_amcarriermutethresholdhigh,
                false));

        // AM CARRIER MUTE THRESHOLD LOW
        ItemName = MenuConfigManager.FAPAL_AMCARRIERMUTETHRESHOLDLOW;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_amcarriermutethresholdlow,
                false));

        // CARRIER SHIFT FUNCTION
        ItemName = MenuConfigManager.FAPAL_CARRIERSHIFTFUNCTION;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_pal_carriershiftfunction,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // FM CARRIER MUTE MODE
        ItemName = MenuConfigManager.FAPAL_FMCARRIERMUTEMODE;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_fmcarriermutemode,
                false));

        // FM CARRIER MUTE THRESHOLD HIGH
        ItemName = MenuConfigManager.FAPAL_FMCARRIERMUTETHRESHOLDHIGH;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_fmcarriermutethresholdhigh,
                false));

        // FM CARRIER MUTE THRESHOLD LOW
        ItemName = MenuConfigManager.FAPAL_FMCARRIERMUTETHRESHOLDLOW;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_fmcarriermutethresholdlow,
                false));

        // PAL FINE TUNE VOLUME
        ItemName = MenuConfigManager.FAPAL_PALFINETUNEVOLUME;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_palfinetunevolume,
                false));

        // AM FINE TUNE VOLUME
        ItemName = MenuConfigManager.FAPAL_AMFINETUNEVOLUME;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_amfinetunevolume,
                false));

        // NICAM FINE TUNE VOLUME
        ItemName = MenuConfigManager.FAPAL_NICAMFINETUNEVOLUME;
        preferenceScreen.addPreference(util.createProgressPreference(
                ItemName,
                R.string.menu_factory_audio_pal_nicamfinetunevolume,
                false));

        return preferenceScreen;
    }

    private PreferenceScreen addFacAudEuSystem(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // FM SATURATION MUTE
        ItemName = MenuConfigManager.FAEU_FM;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_eu_fmsaturationmute,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // EU NON-EU SYSTEM
        ItemName = MenuConfigManager.FAEU_EU_NON;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_audio_eu_eunoneusystem,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        return preferenceScreen;
    }

    private void timeShiftStop() {
        Intent intent = new Intent(TifTimeshiftView.ACTION_TIMESHIFT_STOP);
        mContext.sendBroadcast(intent);
    }

    private PreferenceScreen addFacTVdiagnostic(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);
        timeShiftStop();
        List<String> displayName = new ArrayList<String>();
        List<String> displayValue = new ArrayList<String>();

        if (mTV.isCurrentSourceTv() && !mTV.isCurrentSourceDTV()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addFacTVdiagnostic show atv ==" +
                    mTV.isCurrentSourceATV());

            MtkTvUtil.getInstance().tunerFacQuery(true, displayName,
                    displayValue);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addFacTVdiagnostic not show");
            MtkTvUtil.getInstance().tunerFacQuery(false, displayName,
                    displayValue);
        }

        String itemName = MenuConfigManager.FACTORY_TV_TUNER_DIAGNOSTIC_NOINFO;
        for (int i = 0; i < displayName.size(); i++) {
            String[] display = {
                    displayValue.get(i)
            };

            preferenceScreen.addPreference(util.createListPreference(
                    itemName + i,
                    displayName.get(i),
                    true,
                    display,
                    0));
        }
        if (displayName.isEmpty()) {
            preferenceScreen.addPreference(util.createPreference(
                    itemName,
                    R.string.menu_factory_TV_tunerdiag_noinfo,
                    null));
        }

        return preferenceScreen;
    }

    private PreferenceScreen addFacSetupCaption(
            PreferenceScreen preferenceScreen, Context themedContext) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        // setup extern
        ItemName = MenuConfigManager.FACTORY_SETUP_EXTERN;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_setup_extern_size,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // setup equal
        ItemName = MenuConfigManager.FACTORY_SETUP_EQUAL;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_setup_equal_width,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // setup auto
        ItemName = MenuConfigManager.FACTORY_SETUP_AUTO;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_setup_auto_line,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        // setup roll
        ItemName = MenuConfigManager.FACTORY_SETUP_ROLL;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_setup_roll_up,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));
        // setup UTF-8
        ItemName = MenuConfigManager.FACTORY_SETUP_UTF8;
        preferenceScreen.addPreference(util.createSwitchPreference(
                ItemName,
                R.string.menu_factory_setup_utf8,
                util.mConfigManager.getDefault(ItemName) == 1 ? true : false));

        return preferenceScreen;
    }

    private void bindChannelPreference() {
        Map<String, Preference> storeMap = mDataHelper.getChannelPreferenceMap();
        storeMap.put("channelskip", channelskip);
        storeMap.put("channelSort", channelSort);
        storeMap.put("channelEdit", channelEdit);
        storeMap.put("cleanList", cleanList);
        storeMap.put("saChannelEdit", saChannelEdit);
        storeMap.put("saChannelFine", saChannelFine);
        storeMap.put("updateScan", updateScan);
    }

    Intent bindIntentForPreference(Preference binded) {
        if (binded == null) {
            return null;
        }

        String itemID = binded.getKey();
        String mItem = "";
        if (itemID.equals(MenuConfigManager.TV_SA_CHANNEL_EDIT)
                || itemID.equals(MenuConfigManager.TV_CHANNEL_EDIT)) {
            mItem = MenuConfigManager.TV_CHANNEL_EDIT_LIST;
        } else if (itemID.equals(MenuConfigManager.TV_CHANNELFINE_TUNE)) {
            mItem = MenuConfigManager.TV_CHANNELFINE_TUNE_EDIT_LIST;
        } else if (itemID.equals(MenuConfigManager.TV_CHANNEL_SORT)) {
            mItem = MenuConfigManager.TV_CHANNEL_SORT_CHANNELLIST;
        } else if (itemID.equals(MenuConfigManager.TV_CHANNEL_DECODE)) {
            mItem = MenuConfigManager.TV_CHANNEL_DECODE_LIST;
        } else if (itemID.equals(MenuConfigManager.TV_CHANNEL_SKIP)) {
            mItem = MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST;
        }

        Intent intent = new Intent(mContext, ChannelInfoActivity.class);
        TransItem trans = new TransItem(mItem, "",
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE);
        intent.putExtra("TransItem", trans);
        intent.putExtra("ActionID", itemID);
        binded.setIntent(intent);
        return intent;
    }

    Intent setSatActivityIntent(String mItemId, String title, int pos) {
        Intent intent = new Intent(mContext, SatActivity.class);
        intent.putExtra("mItemId", mItemId);
        intent.putExtra("title", title);
        intent.putExtra("selectPos", pos);
        return intent;
    }

    void setSatListFragArgument(String mItemId, String title, int pos) {
        Bundle bundle = new Bundle();
        bundle.putString("mItemId", mItemId);
        bundle.putString("title", title);
        bundle.putInt("selectPos", pos);
        satListFrag.setArguments(bundle);
    }

    private LiveTVDialog mCleanAllConfirmDialog;

    /**
     * parental clean all ,And hide the interface modification time: on July 20, the modifier:
     * ChenLe content: take initial main interface operation of the hidden code the tooltip window
     */
    void cleanParentalChannelConfirm() {
        String info = mContext.getString(R.string.menu_tv_clear_channel_info);
        mCleanAllConfirmDialog = new LiveTVDialog(mContext, "", info, 3);
        mCleanAllConfirmDialog.setButtonYesName(mContext
                .getString(R.string.menu_ok));
        mCleanAllConfirmDialog.setButtonNoName(mContext
                .getString(R.string.menu_cancel));
        mCleanAllConfirmDialog.getButtonNo().requestFocus();
        mCleanAllConfirmDialog.setPositon(-20, 70);

        OnKeyListener listener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        if (v.getId() == mCleanAllConfirmDialog.getButtonYes().getId()) {
                            mDataHelper.resetCallFlashStore();
                            mEditChannel.resetDefAfterClean();
                            pdialog = ProgressDialog.show(mContext, "Clean All",
                                    "reseting please wait...",
                                    false, false);
                            mEditChannel.resetParental(mContext,
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mCleanAllConfirmDialog.dismiss();
                                        }
                                    });
                            mTV.resetPri(mHandler);
                        } else if (v.getId() == mCleanAllConfirmDialog.getButtonNo().getId()) {
                            mCleanAllConfirmDialog.dismiss();
                        }
                        return true;
                    }
                }
                return false;
            }
        };

        mCleanAllConfirmDialog.bindKeyListener(listener);
    }

  private void networkUpdate() {
        TurnkeyCommDialog netWorkUpgradeDialog ;
        netWorkUpgradeDialog = new TurnkeyCommDialog(mContext, 0);
   //     Rect nuRect;
        netWorkUpgradeDialog.setMessage(mContext.getResources().getString(
                R.string.menu_setup_upgrade_info1));
        netWorkUpgradeDialog.show();
//      com.mediatek.wwtv.tvcenter.util.MtkLog.i("tag", "networkUpdate show");
//        nuRect = new Rect((ScreenConstant.SCREEN_WIDTH - netWorkUpgradeDialog.width) / 2,
//            (ScreenConstant.SCREEN_HEIGHT - netWorkUpgradeDialog.height) / 2,
//            (ScreenConstant.SCREEN_WIDTH + netWorkUpgradeDialog.width) / 2,
//            (ScreenConstant.SCREEN_HEIGHT + netWorkUpgradeDialog.height) / 2);
        final Handler handler = new Handler();
        final NetUpdateGider netUpdateGiderDialog = new NetUpdateGider(
            mContext, netWorkUpgradeDialog);
        handler.postDelayed(netUpdateGiderDialog, 1000);
        netUpdateGiderDialog.getShowDialog().setTitle(
            mContext.getResources().getString(R.string.menu_setup_upgrade_info2_title));
        netUpdateGiderDialog.getShowDialog().setMessage(
            mContext.getResources().getString(R.string.menu_setup_upgrade_info2));
        netUpdateGiderDialog.getShowDialog().setOnKeyListener(
            new DialogInterface.OnKeyListener() {

              @Override
              public boolean onKey(DialogInterface dialog, int keyCode,
                  KeyEvent event) {
                int action = event.getAction();
                if (keyCode == KeyEvent.KEYCODE_BACK
                    && action == KeyEvent.ACTION_DOWN) {
                  netUpdateGiderDialog.getShowDialog().dismiss();
                  // exit();
                  return true;
                }
                return true;
              }
            });
      }

    private boolean getDefaultBoolean(int value) {
        return 1== value ? true:false;
    }

    private void loadContentRatingsSystems(
        PreferenceScreen preferenceScreen,
        Context themedContext,
        String id) {
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);

        List<ContentRatingSystem> mContentRatingSystems =
            util.mConfigManager.loadContentRatingsSystems();

        if (mContentRatingSystems == null) {
            return;
        }

        if(mContentRatingSystems.isEmpty()) {
            return ;
        }

        if(id == null) {
            for (ContentRatingSystem info : mContentRatingSystems) {

                if(!info.isCustom()) {
                    continue;
                }

                Log.d(TAG, "[Ratings] contents:" + info.getTitle() +
                    "," + info.isCustom() + "," + info.getName());

                preferenceScreen.addPreference(util.createPreference(
                    MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS +
                        "|" + info.getId() + "|" + info.getTitle(),
                    info.getTitle()));
            }
        }
        else if(id.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS)) {
            boolean isEnabled = true;
            String[] strInfo = id.split("\\|");
            if(strInfo == null || strInfo.length < 2) {
                return ;
            }

            Log.d(TAG, "[Ratings] id: " + id + ", " + strInfo[strInfo.length - 2]);

            for (ContentRatingSystem info : mContentRatingSystems) {
                if(!info.isCustom()) {
                    continue ;
                }

                if(!strInfo[strInfo.length - 2].equals(info.getId())) {
                    continue ;
                }

                List<ContentRatingSystem.Rating> ratins = info.getRatings();

                ItemName = MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM +
                        "|" + info.getId() + "|" + info.getTitle();

                Log.d(TAG, "[Ratings] ItemName:" + ItemName);

                isEnabled = util.mConfigManager.getDefault(ItemName) == 1;

                preferenceScreen.addPreference(util.createSwitchPreference(
                        ItemName,
                        info.getTitle(),
                        isEnabled));

                //category
                PreferenceCategory category = new PreferenceCategory(themedContext);
                category.setTitle(info.getTitle());
                category.setKey(info.getTitle());
                preferenceScreen.addPreference(category);

                String[] contents = new String[ratins.size() + 1];
                contents[0] = "off";
                for (ContentRatingSystem.Rating rating:ratins){
                    ItemName = MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT +
                            "|" + info.getId() + "|" + rating.getTitle();

                    Preference pref = util.createSwitchPreference(
                            ItemName,
                            rating.getTitle(),
                            util.mConfigManager.getDefault(ItemName) == 1);
                    pref.setEnabled(isEnabled);
                    preferenceScreen.addPreference(pref);
                }
            }
        }
    }
    OnPreferenceClickListener mClickListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(preference.getKey().equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING)){
                int currentType = mTV.getConfigValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE);
                if(currentType == 0){
                    mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
                    ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_SINGLE);
                }else{
                    ScanContent.applyDBSatellitesToSatl(mContext, currentType);
                }
                return false;
            }

            if(preference.getKey().equals(MenuConfigManager.DVBS_SAT_ADD)){
                int currentType = mTV.getConfigValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE);
                if(currentType == 0){
                    mConfigManager.setValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
                    ScanContent.initSatellitesFromXML(mContext,MenuConfigManager.DVBS_ACFG_SINGLE);
                }else{
                    ScanContent.applyDBSatellitesToSatl(mContext, currentType);
                }
                return false;
            }
            
            if(preference.getKey().equals(MenuConfigManager.DVBS_SAT_SCAN_SDX)){
                if(CommonIntegration.getInstance().isUDiskEnabled()){
                    return false;
                }else {
                    //CommonIntegration.getInstance().showToast(mContext, R.string.pro_disk_remove);
                    Toast.makeText(mContext, R.string.pro_disk_remove, 0).show();
                    return true;
                } 
            }
            //save data
            int pola = 0;
            if(mContext.getString(R.string.menu_setup_biss_key_horizonal).equals(ploaPreference.getSummary().toString())){
                pola=1;
            }else{
                pola=2;
            }
            Log.d(TAG,"click set pola:"+pola);
            int freq =Integer.valueOf(frePreference.getSummary().toString());
            int rate =Integer.valueOf(symPreference.getSummary().toString());
            int progId =Integer.valueOf(progPreference.getSummary().toString());
            String threePry = "";
            if (pola <= 1) {
              threePry = freq + "H" + rate;
            } else {
              threePry = freq + "V" + rate;
            }
            Log.d(TAG, "do save data...threePry==" + threePry);
              String cwkey = cwPreference.getSummary().toString();
              if (preference.getKey().equals(MenuConfigManager.BISS_KEY_ITEM_SAVE)) {
                BissItem item = new BissItem(-1, progId, threePry, cwkey);
                int ret = mHelper.operateBissKeyinfo(item, 0);
                if (ret == -2) {// bisskey existed
                  Toast.makeText(mContext, "BissKey Existed!", Toast.LENGTH_SHORT).show();
                }else{
                  //refresh prefrence to add item
//                    List<BissItem> tempLists= mHelper.convertToBissItemList();
//                    BissItem tempitem= tempLists.get(tempLists.size()-1);
//                    bissitems.add(tempitem);
//                    int currentIndex=bissitems.size()-1;
//                    PreferenceUtil util = PreferenceUtil.getInstance(mContext);
//                    Preference bisskeyPreference=util.createPreference(
//                            MenuConfigManager.BISS_KEY_ITEM_UPDATE+(currentIndex+1),
//                            ""+tempitem.progId+" "+tempitem.threePry+" "+tempitem.cwKey);
//                    bisskeyGroup.addPreference(bisskeyPreference);
//                    bisskeyPreferences.add(bisskeyPreference);
                    updateBissKey();
                }
              } else if (preference.getKey().equals(MenuConfigManager.BISS_KEY_ITEM_UPDATE)) {
                BissItem item = new BissItem(bnum, progId, threePry, cwkey);
                int ret = mHelper.operateBissKeyinfo(item, 1);
                if (ret == -2) {// bisskey existed
                  Toast.makeText(mContext, "BissKey Existed!", Toast.LENGTH_SHORT).show();
                }else{
                    updateBissKey();
                }
              } else if (preference.getKey().equals(MenuConfigManager.BISS_KEY_ITEM_DELETE)) {
                BissItem item = new BissItem(bnum, progId, threePry, cwkey);
                mHelper.operateBissKeyinfo(item, 2);
                updateBissKey();
              }
          return false;
        }
      };

      private void updateBissKey(){
          for(Preference p:bisskeyPreferences){
              bisskeyGroup.removePreference(p);
              }
          bissitems= mHelper.convertToBissItemList();
          if(bissitems!=null){
              int size=bissitems.size();
              PreferenceUtil util = PreferenceUtil.getInstance(mPreferenceManager.getContext());
              for(int i=0;i<size;i++){
                  Preference bisskeyPreference=util.createPreference(
                          MenuConfigManager.BISS_KEY_ITEM_UPDATE+(i+1),
                          bissitems.get(i).progId+" "+bissitems.get(i).threePry+" "+bissitems.get(i).cwKey);
                  bisskeyGroup.addPreference(bisskeyPreference);
                  bisskeyPreferences.add(bisskeyPreference);
              }
          }

          InstrumentationHandler.getInstance()
             .sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
    }

    private boolean isSupportBissKey(){
        int freq =-1;
        int rate=-1;
        int progId = -1;
        BissItem defItem = mHelper.getDefaultBissItem();
        if (defItem != null) {
            int findPola =defItem.threePry.indexOf('H');
            if(findPola == -1){
                findPola =defItem.threePry.indexOf('V');
               // pola = 1;
            }/*else{
              //  pola = 0;
            }*/
            freq = Integer.parseInt(defItem.threePry.substring(0, findPola));
            rate = Integer.parseInt(defItem.threePry.substring(findPola+1));
            progId = defItem.progId;
        }
        return freq==0&&rate==0&&progId==0 ? false:true;
    }
    public ScanPreferenceClickListener mScanPreferenceClickListener = new ScanPreferenceClickListener();
    class ScanPreferenceClickListener implements Preference.OnPreferenceClickListener {

        String mActionId = null;
        public void setActionId(String actionId) {
            mActionId = actionId;
        }
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(MenuConfigManager.TV_CHANNEL_SCAN_DVBT.equals(preference.getKey())||
                    MenuConfigManager.TV_CHANNEL_SCAN_DVBC.equals(preference.getKey())){//for poatal code dialog
                String initVal = MtkTvConfig.getInstance().getConfigString(CommonIntegration.OCEANIA_POSTAL);
                PostalCodeEditDialog postalCodedialog=new PostalCodeEditDialog(mPreferenceManager.getContext(),mContext.getResources().getString(R.string.menu_setup_postal_code_setting),MenuConfigManager.SETUP_POSTAL_CODE);
                postalCodedialog.setLength(5);
                Intent euAntennaChannelScanIntent;
                if(MenuConfigManager.TV_CHANNEL_SCAN_DVBT.equals(preference.getKey())){
                    euAntennaChannelScanIntent = new Intent(mContext, ScanDialogActivity.class);
                }else{
                    euAntennaChannelScanIntent = new Intent(mContext, ScanViewActivity.class);
                }
                euAntennaChannelScanIntent.putExtra("ActionID", preference.getKey());
                postalCodedialog.setEditDoneIntent(euAntennaChannelScanIntent, initVal);
                postalCodedialog.show();
            }else {
                com.mediatek.tv.ui.pindialog.PinDialogFragment dialog =
                        com.mediatek.tv.ui.pindialog.PinDialogFragment.create(
                                com.mediatek.tv.ui.pindialog.PinDialogFragment.PIN_DIALOG_TYPE_START_SCAN);
                if(!MenuConfigManager.CHANNEL_CHANNEL_SOURCES.equals(mActionId)){
                    ((TvSettingsActivity)mContext).hide();
                }
                ((TvSettingsActivity)mContext).setActionId(mActionId);
                dialog.show(((TvSettingsActivity)mContext).getFragmentManager(), "PinDialogFragment");
            }
            return true;
        }
      }

      private static TvInputCallbackMgr.SubtitleCallback createSubtitleCallback(Context themedContext, LanguageUtil languageUtil,PreferenceScreen preferenceScreen){
          return new TvInputCallbackMgr.SubtitleCallback() {
              String offValue = String.valueOf(0XFF);
              @Override
              public void onTracksChanged(String inputId, List<TvTrackInfo> trackss) {
                  if (trackss == null){
                      return;
                  }

                  List<TvTrackInfo> tracks = null;
                  String currentID = "";
                  String offValue = String.valueOf(0XFF);
                  if(CommonIntegration.TV_FOCUS_WIN_MAIN == CommonIntegration.getInstance()
                          .getCurrentFocus()){
                      tracks = TurnkeyUiMainActivity.getInstance().getTvView()
                              .getTracks(TvTrackInfo.TYPE_SUBTITLE);
                      currentID = TurnkeyUiMainActivity.getInstance().getTvView()
                              .getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
                  }else {
                      tracks = TurnkeyUiMainActivity.getInstance().getPipView()
                              .getTracks(TvTrackInfo.TYPE_SUBTITLE);
                      currentID = TurnkeyUiMainActivity.getInstance().getPipView()
                              .getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
                  }
                  ListPreference subtitlePre = (ListPreference) preferenceScreen
                          .findPreference(MenuConfigManager.SUBTITLE_TRACKS);

                  if(tracks != null && !tracks.isEmpty()){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentID" + currentID);
                      try {
                          SaveValue.getInstance(themedContext).saveValue(
                                  MenuConfigManager.SUBTITLE_TRACKS, Integer.parseInt(currentID));
                      } catch (Exception e) {
                          SaveValue.getInstance(themedContext).saveValue(
                                  MenuConfigManager.SUBTITLE_TRACKS, -1);
                      }

                      int trackCount = tracks.size();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged size:" + trackCount);
                      for (TvTrackInfo track : tracks) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged track:" + track.getLanguage());
                      }

                      if(trackCount >0){
                          trackCount++;
                      }
                      String[] subKeys = new String[trackCount];
                      String[] subValues = new String[trackCount];
                      for (int i=0;i<trackCount;i++){
                          boolean trackOff = i == trackCount - 1;
                          if(trackOff){
                              subKeys[i] = themedContext.getResources().getString(R.string.common_off);
                              subValues[i] = offValue;
                          }else {
                              TvTrackInfo track = tracks.get(i);
                              String lang = languageUtil.getSubitleNameByValue(track.getLanguage());
                              String langId = track.getExtra().getString("key_LangIdx");
                              String hearingImpaired = track.getExtra().getString("key_HearingImpaired");
                              String trackId = track.getId();
                              if(!TextUtils.isEmpty(langId)){
                                  subKeys[i] = Integer.parseInt(langId) > 0 ? lang + " " + langId : lang;
                              }else{
                                  subKeys[i] = lang;
                              }

                              if(!TextUtils.isEmpty(hearingImpaired)){
                                  subKeys[i] = "true".equals(hearingImpaired)?subKeys[i]+"("+themedContext.getResources().getString(R.string.sound_tracks_hoh)+")":subKeys[i];
                              }
                              subValues[i] = trackId;
                              //com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang :" + langId);
                              //com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"subKeys-----------:"+subKeys[i]);
                          }
                      }


                      CharSequence[] entry =  subtitlePre.getEntries();
                      for (CharSequence charSequence : entry) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "charSequence:"+charSequence+"---:"+entry.length);
                      }
                      subtitlePre.setEntries(subKeys);
                      subtitlePre.setEntryValues(subValues);

                      CharSequence[] entryValues =subValues;
                      CharSequence[] entries =subKeys;
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged trackId:" + currentID+"----"+entries.length+"----"+entryValues.length);
                      int subEnable = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE);
                      if (offValue.equals(currentID)) {
                          subtitlePre.setSummary(entries[entries.length - 1]);
                          subtitlePre.setValue(entryValues[entryValues.length - 1]
                                  .toString());
                          if(subEnable != 0){
                              subtitlePre.setEnabled(true); 
                          }
                          if(PreferenceUtil.isFromSubtitleTrackclick){
                              InstrumentationHandler.getInstance()
                                      .sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                              PreferenceUtil.isFromSubtitleTrackclick = false;
                          }
                          return;
                      }
                      for (int subIndex = 0; subIndex < entryValues.length; subIndex++) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged setSummary : "
                                  + entries[subIndex]+"---"+entryValues[subIndex]);
                          if (entryValues[subIndex].equals(currentID)) {

                              subtitlePre.setSummary(entries[subIndex]);
                              subtitlePre.setValue(entryValues[subIndex].toString());
                              if(subEnable != 0){
                                  subtitlePre.setEnabled(true); 
                              }
                          }
                      }

                  }else{
                      subtitlePre.setSummary("");
                      subtitlePre.setEnabled(false);
                  }

                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged subtitle :" + PreferenceUtil.isFromSubtitleTrackclick);

                  if(PreferenceUtil.isFromSubtitleTrackclick){
                      InstrumentationHandler.getInstance()
                              .sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                      PreferenceUtil.isFromSubtitleTrackclick = false;
                  }

              }

              @Override
              public void onTrackSelected(String inputId, String trackId) {
                  try {
                      // SaveValue.getInstance(mContext).saveValue(MenuConfigManager.SUBTITLE_TRACKS,Integer.valueOf(trackId));
                      ListPreference subtitlePre = (ListPreference) preferenceScreen
                              .findPreference(MenuConfigManager.SUBTITLE_TRACKS);
                      CharSequence[] entryValues = subtitlePre.getEntryValues();
                      CharSequence[] entries = subtitlePre.getEntries();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTrackSelected trackId:" + trackId);
                      if (offValue.equals(trackId)) {
                          subtitlePre.setSummary(entries[entries.length - 1]);
                          subtitlePre.setValue(entryValues[entryValues.length - 1]
                                  .toString());
                          return;
                      }
                      for (int subIndex = 0; subIndex < entryValues.length; subIndex++) {
                          if (entryValues[subIndex].equals(trackId)) {
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTrackSelected setSummary : "
                                      + entries[subIndex]);
                              subtitlePre.setSummary(entries[subIndex]);
                              subtitlePre.setValue(entryValues[subIndex].toString());
                          }
                      }

                      List<TvTrackInfo> newtracks = null;
                      String newcurrentID = "";

                      if (CommonIntegration.TV_FOCUS_WIN_MAIN == CommonIntegration.getInstance().getCurrentFocus()) {
                          newtracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
                          newcurrentID = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
                      } else {
                          newtracks = TurnkeyUiMainActivity.getInstance().getPipView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
                          newcurrentID = TurnkeyUiMainActivity.getInstance().getPipView().getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
                      }
                      if(newtracks != null) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "newcurrentID" + newcurrentID);
                          try {
                              SaveValue.getInstance(themedContext).saveValue(MenuConfigManager.SUBTITLE_TRACKS, Integer.parseInt(newcurrentID));
                          } catch (Exception e) {
                              SaveValue.getInstance(themedContext).saveValue(MenuConfigManager.SUBTITLE_TRACKS, -1);
                          }
                      }

                  } catch (Exception e) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onTrackSelected error:" + e.toString());
                  }
              }
          };
    }
}
