package com.mediatek.wwtv.tvcenter.nav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvView.OnUnhandledInputEventListener;
import android.media.tv.TvAppView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.android.tv.menu.MenuOptionMain;
import com.android.tv.onboarding.SetupSourceActivity;
import com.mediatek.support.fvp.FreeviewPlayClient;
import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckedListener;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvEASBase;
import com.mediatek.twoworlds.tv.MtkTvEWSPABase;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvHighLevel;
import com.mediatek.twoworlds.tv.MtkTvMultiView;
import com.mediatek.twoworlds.tv.MtkTvOAD;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvUpgrade;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvNativeAppId;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvUpgradeDeliveryTypeBase;
import com.mediatek.wwtv.rxbus.MainActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.base.scan.model.ScannerManager;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanThirdlyDialog;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.RecoveryRatings;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.commonview.TvBlockView;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView.TvSurfaceLifeCycle;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGManager;
import com.mediatek.wwtv.tvcenter.epg.cn.EPGCnActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.epg.sa.EPGSaActivity;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.FocusLabelControl;
import com.mediatek.wwtv.tvcenter.nav.util.HDMIDAIManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.util.MtkTvEWSPA;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.util.OnLoadingListener;
import com.mediatek.wwtv.tvcenter.nav.util.PIPPOPSurfaceViewControl;
import com.mediatek.wwtv.tvcenter.nav.util.PipPopConstant;
import com.mediatek.wwtv.tvcenter.nav.view.BMLMain;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.CommonMsgDialog;
import com.mediatek.wwtv.tvcenter.nav.view.DvbtInactiveChannelConfirmDialog;
import com.mediatek.wwtv.tvcenter.nav.view.EWSDialog;
import com.mediatek.wwtv.tvcenter.nav.view.FVP;
import com.mediatek.wwtv.tvcenter.nav.view.AtscInteractive;
import com.mediatek.wwtv.tvcenter.nav.view.FloatView;
import com.mediatek.wwtv.tvcenter.nav.view.FocusLabel;
import com.mediatek.wwtv.tvcenter.nav.view.GingaTvDialog;
import com.mediatek.wwtv.tvcenter.nav.view.Hbbtv;
import com.mediatek.wwtv.tvcenter.nav.view.InfoBarDialog;
import com.mediatek.wwtv.tvcenter.nav.view.Mheg5;
import com.mediatek.wwtv.tvcenter.nav.view.MiscView;
import com.mediatek.wwtv.tvcenter.nav.view.OneKeyMenuDialog;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowWithDialog;
import com.mediatek.wwtv.tvcenter.nav.view.TTXMain;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.VgaPowerManager;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.scan.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.search.SearchManagerHelper;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.tvcenter.util.AudioFocusManager;
import com.mediatek.wwtv.tvcenter.util.Commands;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.tvcenter.util.Constants;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.NetflixUtil;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mediatek.wwtv.setting.preferences.SettingsPreferenceScreen;
import com.mediatek.wwtv.setting.preferences.PreferenceData;

//add by xun
import java.util.Map;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.internal.i.solution.source.IsSourceManager;
import com.ist.systemuilib.tvapp.TvToSystemManager;
import com.ist.systemuilib.tvapp.NosignalView;
//end xun

public class TurnkeyUiMainActivity extends BaseActivity
    implements OnUnhandledInputEventListener,
        OnLoadingListener,
        OnPinCheckedListener,
        TvSurfaceLifeCycle {
  private static final String TAG = "TurnkeyUiMainActivity";

  private TifTimeShiftManager mTifTimeShiftManager;
  /* data declaration */
  private static TurnkeyUiMainActivity mainActivity = null;
  protected InternalHandler mHandler;
  protected ComponentsManager mNavCompsMagr;
  protected CommonIntegration mCommonIntegration;
  private TextToSpeechUtil mTTS;
  Long chupordowntime = 0l;
  protected KeyDispatch mKeyDispatch;
  protected MtkTvEASBase mEas;

  /* view declaration */
  protected FrameLayout OriginalView;
  private BannerView mBannerView;
  private UkBannerView mUkBannerView;
  private LinearLayout mSundryLayout = null;
  private LinearLayout mainSurfaceViewLy;
  private LinearLayout subSurfaceViewLy;
  private TvSurfaceView mTvView = null;
  private TvSurfaceView mPipView = null;
  private TvBlockView mBlockScreenForTuneView = null;
  /* system declaration */
  protected AudioFocusManager mAudioManager = null;
  private ProgressBar mProgressBar;

  /* TVAPI */
  protected MtkTvHighLevel mHighLevel = new MtkTvHighLevel();
  protected boolean isShutdownFlow = false;
  private boolean isNeedStartSourceWithNotResume = false;
  private boolean reqMhegResume = false;
  private static boolean showBannerInOnResume = true;
  protected boolean mNoNeedResetSource;
  protected boolean mNoNeedChangeChannel;

  protected static boolean isStartTv = true;
  public static boolean isUKCountry = false;

  protected InputSourceManager mInputSourceManager;
  private boolean bChangeSource = false; // switch source,banner use it to judge the reason of show
  // banner.

  protected Handler mThreadHandler;
  protected TurnkeyReceiver mTurnkeyReceiver;

  private boolean mIsLiveTVPause = false;
  private String curLang = "";

  private static final String MAIN_SOURCE_HARDWARE_ID = "multi_view_main_source_hardware_id";

  public HDMIDAIManager mHdmidaiManager;
  private VgaPowerManager mVgaPowerManager;

  private Runnable mSetMMPMode =
    new Runnable() {
    @Override
    public void run() {
        MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
    }
  };
  private HandlerThread mHandlerThread;
  private CompositeDisposable mDisposables = new CompositeDisposable();
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onActivityResult, requestCode=" + requestCode + ",resultCode=" + resultCode);

    if (NavBasic.NAV_REQUEST_CODE != requestCode) {
      return;
    }

    if (NavBasic.NAV_RESULT_CODE_MENU == resultCode) {
      if (mHandler != null) {
        mHandler.resetTurnkeyLayout();
      }
    } else if (RESULT_OK == resultCode) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onActivityResult, ChannelListDialog resultCode = " + resultCode);
      ChannelListDialog mChListDialog =
          (ChannelListDialog)
              ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_CH_LIST);
      if (mChListDialog != null && mChListDialog.isVisible()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onActivityResult, resultCode=" + resultCode);
        mChListDialog.onActivityResult(data);
      }
    }
  }

  public boolean isLiveTVPause() {
    return mIsLiveTVPause;
  }

  public void resetLayout() {
    resetLayout(0);
  }

  public void resetLayout(int delaytime) {
    if (mHandler != null) {
      Message msg = mHandler.obtainMessage();
      msg.what = InternalHandler.RESET_LAYOUT;
      mHandler.sendEmptyMessageDelayed(InternalHandler.RESET_LAYOUT, delaytime);
    }
  }

  public void selectCurrentChannelDelay(long time) {
    if (mHandler != null) {
      mHandler.sendEmptyMessageDelayed(InternalHandler.MSG_SELECT_CURRENT_CHANNEL, time);
    }
  }

  public static void setStartTv(boolean isStartTv) {
    TurnkeyUiMainActivity.isStartTv = isStartTv;
  }

  //add by xun
  private BannerImplement mBannerImplement;
  private NosignalView mNosignalView;
  private TvToSystemManager mTvToSystemManager;
  public TvToSystemManager newTvToSystemManager() {
      if(mTvToSystemManager == null){
          mTvToSystemManager = TvToSystemManager.getInstance();
          if(mTvToSystemManager != null) {
              //from：IstSystemUI/app/src/main/java/com/ist/istsystemui/inputsource/tvapp/TvAppViewCtl.java
              //  IstSystemUI/app/src/main/java/com/ist/istsystemui/uiservice/SystemToTvManager.java
              //  IstSystemUI/app/src/main/aidl/com/ist/systemuilib/tvapp/IServiceToTv.aidl --> TvToSystemManager
              mTvToSystemManager.onCallback(new TvToSystemManager.ToTvCallback() {
                  @Override
                  public void sourceChange(String inputsource, String sourceName) {
                      Log.i(TAG, " sourceChange: " + inputsource + " - " + sourceName);
                      mHandler.postDelayed(new Runnable() {
                          @Override
                          public void run() {
                              mNosignalView.sourceChange(inputsource, sourceName);
                          }
                      },0);
                  }
                  @Override
                  public void signalAvailable(String inputId) {
                      Log.i(TAG, " signalAvailable: " + inputId);
                      mHandler.postDelayed(new Runnable() {
                          @Override
                          public void run() {
                              showNosignalView(false);
                          }
                      },0);
                  }
                  @Override
                  public void signalUnAvailable(String inputId, int reason) {
                      Log.i(TAG, " signalUnAvailable: " + inputId + " - " + reason);
                      mHandler.postDelayed(new Runnable() {
                          @Override
                          public void run() {
                              showNosignalView(true);
                              mNosignalView.signalUnAvailable(inputId, reason);
                          }
                      },0);
                  }
                  @Override
                  public String getSourceInfo(String inputsource) {
                      String msg = mBannerImplement.getInputResolution();
                      Log.i(TAG, " showBootNosignalView: show= " + inputsource);
                      return msg;
                  }
              });
          }
      }
      return mTvToSystemManager;
  }

  /**
   * 无信号页面显示
   * @param show
   */
  public void showNosignalView(boolean show) {
    Log.d(TAG,"showNosignalView  = "+ show);
    if(mNosignalView == null) {
      mNosignalView = new NosignalView(this);
      IsSourceManager sourceManager = IsSourceManager.getInstance();
      if(sourceManager != null) {
        String inputsource = sourceManager.getCurSourceChannel();
        Map<String, String> sourceMap = sourceManager.getInputSrcMap();
        if (!sourceMap.isEmpty() && sourceMap.containsKey(inputsource)) {
          String sourceName = sourceManager.getInputSrcMap().get(inputsource);
          mNosignalView.sourceChange(inputsource, sourceName);
        }
      }
      FrameLayout tvAppframe = (FrameLayout)OriginalView.findViewById(R.id.nav_tv_app_view);
      tvAppframe.addView(mNosignalView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT));
    }
    mNosignalView.showView(show);
  }
  //end xun

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    Log.i(TAG, " onCreate():" + this.toString() + " intent=" + getIntent());
    // store the object
    SaveValue.setLocalMemoryValue("tkOnCreate", true);
    isUKCountry = TVContent.getInstance(this).isUKCountry() && CommonUtil.isSupportFVP(true);
    mainActivity = this;
    mKeyDispatch = KeyDispatch.getInstance();
    Point outSize = new Point();
    getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    ScreenConstant.SCREEN_WIDTH = outSize.x;
    ScreenConstant.SCREEN_HEIGHT = outSize.y;
    //add by xun 20200522 for touch mode
    WindowManager.LayoutParams p = getWindow().getAttributes();
    p.setTitle("IstSourceWindow");
    getWindow().setAttributes(p);
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onCreate("");
    }
    mBannerImplement = new BannerImplement(mainActivity);
    //end xun 20200522
    /* system info */
    mAudioManager = AudioFocusManager.getInstance(getApplicationContext());
    curLang = Locale.getDefault().getLanguage();

    /* pre init */
    preInit();
    /* initial */
    init();
    //        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
    //            AlarmMgr.getInstance(getApplicationContext()).startAlarm();
    //        }
    /* intent */
    if ((SaveValue.readLocalMemoryIntValue("mPreConfigsFlags")
            & (ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_ORIENTATION
                | ActivityInfo.CONFIG_SCREEN_SIZE))
        == 0) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          TAG,
          " onCreate, mPreConfigsFlags=" + SaveValue.readLocalMemoryIntValue("mPreConfigsFlags"));
      receiveIntent(getIntent());
    }

    reqMhegResume = false;
    /* init the commonIntegratioin context */
    CommonIntegration.getInstance().setContext(this);

    if (DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportDAI()) {
      mHdmidaiManager = new HDMIDAIManager(this);
    }

    mInputSourceManager = TvSingletons.getSingletons().getInputSourceManager();
    if (MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
      toastResultOfFirmwareUpgrade();
    }

    if (SaveValue.getInstance(this).readValue(MAIN_SOURCE_HARDWARE_ID, -1) == -1) {
      Log.d(TAG, "updateParserRating");
      TvSingletons.getSingletons().getTvInputManagerHelper().updateParserRating();
    }
    TVContent.getInstance(this).loadDvbsDataBase();
    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, " onCreate() end ~");
  }

  private void initConfig(boolean bStartTv) {
    final boolean isstart = bStartTv;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initConfig, isstart>>>>" + isstart);
    TVAsyncExecutor.getInstance()
        .execute(
            () -> {
              if (MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_EX_BRUNING_MODE)
                  == 0) {
                mHighLevel.setBroadcastUiVisibility(false);
              } else {
                mHighLevel.setBroadcastUiVisibility(true);
              }
              if ((ComponentsManager.getActiveCompId() & NavBasic.NAV_NATIVE_COMP_ID_BASIC) == 0) {
                mHighLevel.launchInternalApp(MtkTvNativeAppId.MTKTV_NATIVE_APP_ID_NAV);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initConfig, launchInternalApp");
              }
            });
  }

  private void toastResultOfFirmwareUpgrade() {
    int result =
        MtkTvUpgrade.getInstance().queryUpgradeResult(MtkTvUpgradeDeliveryTypeBase.INTERNET);
    Toast firmwareUpgradeToast = null;
    switch (result) {
      case 1: // UPGRADE_SUCCESSFULLY
        firmwareUpgradeToast =
            Toast.makeText(
                this,
                getResources().getString(R.string.download_firmware_upgrade_successfully),
                Toast.LENGTH_LONG);
        break;
      case 2: // UPGRADE_UNSUCCESSFULLY
        firmwareUpgradeToast =
            Toast.makeText(
                this,
                getResources().getString(R.string.download_firmware_upgrade_unsuccessfully),
                Toast.LENGTH_LONG);
        break;
      default:
        break;
    }
    if (null != firmwareUpgradeToast) {
      firmwareUpgradeToast.setGravity(Gravity.CENTER, 0, 0);
      firmwareUpgradeToast.show();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    if (!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()) {
      mBlockScreenForTuneView.setVisibility(
          View.VISIBLE, TvBlockView.BLOCK_UNTIL_TUNE_INPUT_AFTER_BOOT);
    }
    mNoNeedChangeChannel = false;
    // add flow in receiveIntent
    receiveIntent(intent);
  }

  @Override
  protected void onStart() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onStart()");
    //add by xun
    if(IsSourceManager.getInstance() != null) {
      IsSourceManager.getInstance().setTvApkRunning(true);
    }
    showNosignalView(false);
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onStart("");
    }
    //end xun
    // fix CR:DTV00613549
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
      TvCallbackHandler.getInstance().removeCallBackListener(mHandler);
      mHandler = null;
    }
    mThreadHandler.removeCallbacks(mSetMMPMode);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart()" + DestroyApp.getRunningActivity());
    mHandler = new InternalHandler(this);
    mHandler.sendEmptyMessage(InternalHandler.MSG_RESUME_NATIVE_HBBTV);
    TvCallbackHandler.getInstance().addCallBackListener(mHandler);
    isNeedStartSourceWithNotResume = true;
    showBannerInOnResume = true;
    TVAsyncExecutor.getInstance()
        .execute(
            () -> {
              Commands.startCmd();
            });

    TvSingletons.getSingletons().setTurnkeyUiMainActiviteActive(true);
    super.onStart();
    mAudioManager.requestAudioFocus();
    mAudioManager.createAudioTrack();
    mAudioManager.createMediaSession(getApplicationContext());

    /* post initial */
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onResume isShutdownFlow=" + isShutdownFlow);
    if (!(isShutdownFlow)) {
      CommonIntegration.resetBlueMuteForLiveTv(this);
      if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
        if(!DvrManager.getInstance().Contains(StateDvrPlayback.getInstance())){
          DvrManager.getInstance().setState(StateDvrPlayback.getInstance(DvrManager.getInstance()));
        }
      } else {
        postInit(false);
      }
    } else {
      checkNetflixResume();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onStart() end ~");
  }

  @Override
  protected void onResume() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume()");
    //add by xun
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onResume("");
    }
    //end xun
    isNeedStartSourceWithNotResume = false;
    mIsLiveTVPause = false;
    // TODO Auto-generated method stub
    super.onResume();
    if (mInputSourceManager != null) {
      DvrManager.getInstance().setStopDvrNotResumeLauncher(false);
      mInputSourceManager.resetScreenOffFlag();
      mInputSourceManager.setShutDownFlag(false);
    }
    TIFChannelManager.getInstance(this).handleUpdateChannels();
    // only use to judge weather need resume turnkey . (set false : no need ,from dvrplayback)
    Boolean isInPictureInPicureMode =
        SaveValue.getInstance(this).readBooleanValue("isInPictureInPicureMode");
    if (!isInPictureInPictureMode() && isInPictureInPicureMode) {
      ComponentStatusListener.getInstance()
          .updateStatus(ComponentStatusListener.NAV_EXIT_ANDR_PIP, 0);
    }
    receiveFVPIntent(getIntent());
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() isUKCountry>>"+isUKCountry);

    if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
      if(!DvrManager.getInstance().Contains(StateDvrPlayback.getInstance())){
        DvrManager.getInstance().setState(StateDvrPlayback.getInstance(DvrManager.getInstance()));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() end ~");
      }
      return;
    }

    if (MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
      MtkTvGinga.getInstance().startGinga();
    }

    mNavCompsMagr = ComponentsManager.getInstance();
    if (reqMhegResume) {
      reqMhegResume = false;
      ComponentsManager.updateActiveCompId(true, NavBasic.NAV_NATIVE_COMP_ID_MHEG5);
    }

    /* post initial */
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onResume isShutdownFlow=" + isShutdownFlow);
    if (!(isShutdownFlow)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume()-3");
      if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
        if(!DvrManager.getInstance().Contains(StateDvrPlayback.getInstance())) {
          DvrManager.getInstance().setState(StateDvrPlayback.getInstance(DvrManager.getInstance()));
        }
      } else {
        postInit(false);
      }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "will resume banner, showBannerInOnResume:"+showBannerInOnResume);
    if (!ComponentsManager.getInstance().isComponentsShow()) {
      if (showBannerInOnResume && !SaveValue.readLocalMemoryBooleanValue("tkOnCreate")) {
        boolean isTurnCHOfExitEPG = CommonIntegration.getInstance().isTurnCHOfExitEPG();
        boolean isChChanging = CommonIntegration.getInstance().isCHChanging();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Test_handleResume_isTurnCHOfExitEPG=" + isTurnCHOfExitEPG + ",isChChanging="+isChChanging);
        if (!isTurnCHOfExitEPG && !isChChanging) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "will resume banner.");
          Intent intent = this.getIntent();
          boolean epg = intent.getBooleanExtra("EPG", false);
          boolean fvp = intent.getBooleanExtra("FVP", false);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG:" + epg + ",fvp:" + fvp);
          if (!(epg || fvp)) {
            mNavCompsMagr.getComponentById(NavBasic.NAV_COMP_ID_BANNER).startComponent();
          }
        }
      }
    }
    showBannerInOnResume = true;

    showDVBTInActiveDialog();

    if (!SaveValue.readLocalMemoryBooleanValue("isFirstStart")) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "when resume recovery ratings");
      RecoveryRatings.recoveryFromTVAPI(getApplicationContext());
      SaveValue.setLocalMemoryValue("isFirstStart", true);
    }

    if (SaveValue.readLocalMemoryBooleanValue("tkOnCreate")) {
      SaveValue.setLocalMemoryValue("tkOnCreate", false);
    }

    if (CommonIntegration.isEURegion()) {
      if (mCommonIntegration.getSvl() == 4) {
        // preferred satellite
        int operatorName = MtkTvConfig.getInstance().getConfigValue(CommonIntegration.SAT_BRDCSTER);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "OperatorName==" + operatorName);
        if (operatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS) {
          mThreadHandler.postDelayed(
              () -> {
                String tkgsChUpdateId = "g_misc__dvbs_tkgs_chlst_update";
                int ret = MtkTvConfig.getInstance().getConfigValue(tkgsChUpdateId);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is need to show dialog==" + (ret == 1));
                if (ret == 1) {
                  // has msg,the reset configId
                  MtkTvConfig.getInstance().setConfigValue(tkgsChUpdateId, 0);
                  MtkTvScanDvbsBase scanDvbs = new MtkTvScanDvbsBase();
                  String userMsg = scanDvbs.dvbsGetTKGSUserMessage(); // get from api
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is need to show dialog userMsg" + userMsg);
                  ScannerManager.showDvbsTKGSUserMsgDialog(userMsg);
                }
              },
              5000);
        }
      }
    }

    if (isShutdownFlow) {
      showDVBTRegionListDialog();
    }

    //long uriId = SaveValue.getInstance(this).readLongValue(DvrManager.DVR_URI_ID);
    //TIFChannelInfo mTIFChannelInfo = TIFChannelManager.getInstance(this).getCurrChannelInfo();
    //if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()) {
    //  if (uriId!=-1&&mTIFChannelInfo != null && uriId != mTIFChannelInfo.mId&&!CommonIntegration.getInstance().isCurrentSourceATV()) {
    //    Intent intent = new Intent(Intent.ACTION_VIEW);
    //    intent.setData(TvContract.buildChannelUri(uriId));
    //    processInputUri(intent);
    //  }
    //  StateDvr.setTkActive(true);
    //  StateDvr.getInstance().showCtrBar();
    //}

    if (isShutdownFlow) {
      showItalyLcnConflictDialog();
      showFvpAdviseScanDialog();
    }
    if (mBlockScreenForTuneView != null) {
      mBlockScreenForTuneView.setVisibility(
          View.GONE, TvBlockView.BLOCK_UNTIL_TUNE_INPUT_AFTER_BOOT);
    }
    mAudioManager.setMediaPlaybackPlaying();
    getIntent().removeExtra("EPG");
    getIntent().removeExtra("FVP");
    getIntent().removeExtra("SetupSource");

    if (TVContent.getInstance(this).isIDNCountry()) {

    if(!mCommonIntegration.getIsCreatedMonitor()){
        mNavCompsMagr.addDialog(new EWSDialog(this));
      }
      TVAsyncExecutor.getInstance().execute(
              () -> {
                MtkTvEWSPA mMtkTvEWSPA = MtkTvEWSPA.getInstance();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() mMtkTvEWSPA ="+mMtkTvEWSPA);
                if(mMtkTvEWSPA != null){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() ewsInfo ~ ");
                  MtkTvEWSPABase.EwsInfo ewsInfo = mMtkTvEWSPA.getEWSInfo((byte) 0);
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() ewsInfo = "+ ewsInfo);
                  if(ewsInfo != null){
                    TvCallbackData tdata = new TvCallbackData();
                    tdata.param1 = 2;//need show ews again.
                    Message msg = mHandler.obtainMessage();
                    msg.what = msg.arg1 = msg.arg2 = TvCallbackConst.MSG_CB_EWS_MSG;
                    msg.obj = tdata;
                    mHandler.sendMessage(msg);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() sendMessage MSG_CB_EWS_MSG~");
                  }
                }
              });
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onResume() end ~");
  }

  @Override
  public void onConfigurationChanged(android.content.res.Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String newLang = newConfig.locale.getLanguage();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curLang:" + curLang + "newLang: " + newLang);
    if (!curLang.equals(newLang)) {
      // only navdialog needs to be re_constructed
      //      init();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ConfigurationChanged");
      PwdDialog mPWDDialog = (PwdDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
      boolean isSvctxBlock = mPWDDialog.isSvctxBlocked();
      ComponentsManager.getInstance().deinitNavDialog();
      addNavDialog();
      mPWDDialog = (PwdDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
      mPWDDialog.setSvctxBlocked(isSvctxBlock);
        RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        if(!isUKCountry){
          mBannerView = new BannerView(this);
          mBannerView.setLayoutParams(mParams);
        }else{
            mUkBannerView = new UkBannerView(this);
            mUkBannerView.setLayoutParams(mParams);
        }

      ComponentsManager.getInstance().deinitMiscsComponent(NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG);

      // Menu option
      mNavCompsMagr.addMisc(new MenuOptionMain(this));

      ComponentStatusListener.getInstance()
          .updateStatus(ComponentStatusListener.NAV_LANGUAGE_CHANGED, 0);

      SettingsPreferenceScreen settingsPreferenceScreen = SettingsPreferenceScreen.getInstance();
      if(settingsPreferenceScreen != null && settingsPreferenceScreen.mMenuSystemInfo != null){
        settingsPreferenceScreen.mMenuSystemInfo = null;
      }

      PreferenceData.setPreferenceDataNull();

      //add by xun for change language
      if(mNosignalView == null) {
        mNosignalView.onConfigurationChanged(null);
      }
      //end xun
    }
    curLang = newLang;

    Log.d(TAG, "updateParserRating for language change");
    TvSingletons.getSingletons().getTvInputManagerHelper().updateParserRatingForChangeLanguage();
  }

  public static void setShowBannerInOnResume(boolean isSet) {
    showBannerInOnResume = isSet;
  }

  public void setChangeSource(boolean isChange) {
    bChangeSource = isChange;
  }

  public boolean getChangeSource() {
    return bChangeSource;
  }

  @Override
  protected void onPause() {
    //add by xun
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onPause("");
    }
    //end xun
    mNavCompsMagr = ComponentsManager.getInstance();
    if (mNavCompsMagr.getNativeActiveCompId() != 0) {
      FreeviewPlayClient.getInstance().disableTalkBack(true);
    } else {
      FreeviewPlayClient.getInstance().disableTalkBack(false);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onPause()");
    super.onPause();
    mIsLiveTVPause = true;
    mNavCompsMagr.hideAllComponents();
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onPause isShutdownFlow=" + isShutdownFlow);
    if (isShutdownFlow) {
      isStartTv = true;
    }
    if(mHdmidaiManager != null) {
      mHdmidaiManager.onPause();
    }
  }

  public void resetInputFlag() {
    mNoNeedChangeChannel = false;
    showBannerInOnResume = false;
    isStartTv = true;
    if (mTvView != null) {
      mTvView.setStart(false);
    }
  }

  @Override
  protected void onStop() {
    MtkTvMultiView.getInstance().setChgSource(false);
    mAudioManager.abandonAudioFocus();
    mThreadHandler.post(mSetMMPMode);
    mVgaPowerManager.hideDialog();
    super.onStop();
    FreeviewPlayClient.getInstance().disableTalkBack(false);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onStop()");

    if (mTifTimeShiftManager != null) {
      if (mTifTimeShiftManager.isAvailable()
          && SaveValue.getInstance(this)
              .readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
        Toast.makeText(
                this,
                getResources().getString(R.string.timeshift_stopped),
                Toast.LENGTH_SHORT)
            .show();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "timeshiftstopped");
        SaveValue.getInstance(this)
            .setLocalMemoryValue(MenuConfigManager.TIMESHIFT_START, false);
        mHandler.removeMessages(InternalHandler.MSG_TIME_SHIFT_NOT_AVAILABLE);
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "timeshiftunstopped");
        Message msg = mHandler.obtainMessage();
        msg.arg1 = 99;
        msg.what = InternalHandler.MSG_TIME_SHIFT_NOT_AVAILABLE;
        mHandler.sendMessage(msg);
      }
    }

    resetInputFlag();
    mThreadHandler.removeCallbacks(mHotKeyRun);
    mHandler.sendEmptyMessage(InternalHandler.MSG_PAUSE_NATIVE_HBBTV);

    if (!mHandler.hasMessages(InternalHandler.MSG_CLOSE_SOURCE)) {
      Message msg = mHandler.obtainMessage();
      msg.what = InternalHandler.MSG_CLOSE_SOURCE;
      mHandler.sendMessage(msg);
    }
    mCommonIntegration.resetBlueMuteFor3rd(getApplicationContext());
    if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()) {
      StateDvr.setTkActive(false);
      StateDvr.getInstance().clearWindow(true);
      StateDvr.getInstance().recoveryView();
    }
    if (StateDvrFileList.getInstance() != null) {
      StateDvrFileList.getInstance().recoveryView();
    }
    if (DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog() != null
        && DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().isShowing()) {
      DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().setHome(true);
    }

    if (isInPictureInPictureMode()) {
      finish();
    }
    TvSingletons.getSingletons().setTurnkeyUiMainActiviteActive(false);
    mAudioManager.releaseAudioTrackAndMediaSession();
    showBannerInOnResume = false;

    //add by xun
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onStop("");
    }
    //end xun
  }

  @Override
  public void finish() {
    super.finish();
    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "finish()");
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
      if(DvrManager.getInstance() != null && DvrManager.getInstance().uiManager != null){
        DvrManager.getInstance().uiManager.hiddenAllViews();
      }
    }
  }

  @Override
  public void onDestroy() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onDestroy()");
    //add by xun
    if(IsSourceManager.getInstance() != null) {
      IsSourceManager.getInstance().setTvApkRunning(false);
    }

    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onDestroy("");
    }
    //end xun
    mDisposables.clear();
    /* record the destroy reason */
    SaveValue.setLocalMemoryValue("mPreConfigsFlags", getChangingConfigurations());

    TvCallbackHandler.getInstance().removeCallBackListener(mHandler);
    mHandler.removeCallbacksAndMessages(null);
    mTurnkeyReceiver.unregisterTvReceiver();
    mThreadHandler.removeCallbacksAndMessages(null);
    mHandlerThread.quit();
    mThreadHandler = null;

    mNavCompsMagr.deinitComponents();
    mNavCompsMagr.clear();
    ComponentStatusListener.getInstance().removeAll();
    //        AlarmMgr.getInstance(getApplicationContext()).cancelAlarm();
    isStartTv = true;
    if (mHdmidaiManager != null) {
      mHdmidaiManager.release();
    }
    mainActivity = null;
    RxBus.instance.send(new MainActivityDestroyEvent());
    super.onDestroy();
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "keyCode:" + keyCode);
    //add by xun 20200522 for touch mode
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onKey(keyCode, event);
    }
    //end xun 20200522

    /* For replease repeat down key */
    if (mKeyDispatch.isLongPressed() ||
        keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
        keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
      mKeyDispatch.passKeyToNative(1, keyCode, event);
    }
    if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
      SearchManagerHelper.getInstance(this).launchAssistAction();
      return true;
    }
    keyCode = KeyMap.getKeyCode(event);
    if ((keyCode == KeyMap.KEYCODE_MTKIR_CHUP
            || keyCode == KeyMap.KEYCODE_MTKIR_CHDN
            || keyCode == KeyMap.KEYCODE_DPAD_UP
            || keyCode == KeyMap.KEYCODE_DPAD_DOWN)
        && event.getRepeatCount() > 0) {
      return true;
    }
    switch (keyCode) {
      case KeyMap.KEYCODE_BACK:
      case KeyMap.KEYCODE_MTKIR_PIPPOP:
      case KeyMap.KEYCODE_VOLUME_UP:
      case KeyMap.KEYCODE_VOLUME_DOWN:
        return true;
      default:
        break;
    }

    return super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    Log.i(TAG, "onKeyDown");
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "keyCode:" + keyCode);
    //add by xun 20200522 for touch mode
    if(newTvToSystemManager() != null) {
      mTvToSystemManager.onKey(keyCode, event);
    }
    //end xun 20200522
    
    if (mTurnkeyReceiver.mCanChangeChannel) {
      mTurnkeyReceiver.mIsFromTK = false;
    }

    keyCode = KeyMap.getKeyCode(event);
    /*if ((keyCode == KeyMap.KEYCODE_MTKIR_CHUP
            || keyCode == KeyMap.KEYCODE_MTKIR_CHDN
            || keyCode == KeyMap.KEYCODE_DPAD_UP
            || keyCode == KeyMap.KEYCODE_DPAD_DOWN)
        && event.getRepeatCount() > 0) {
      return true;
    }*/
    if (keyCode == KeyMap.KEYCODE_MTKIR_PAUSE && event.getScanCode() == 0) {
      return true;
    }
    onKeyHandler(keyCode, event, false);
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_PLAY:
      case KeyMap.KEYCODE_MTKIR_PAUSE:
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Return ture to intercept event.");
        return true;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean onKeyHandler(int keyCode, KeyEvent event, boolean noKeyDown) {
    Log.i(
        TAG,
        "KeyHandler keycode="
            + keyCode
            + " event="
            + Optional.ofNullable(event).map(t -> t.toString()).orElse(""));

    boolean isActiveCompHandled = false;
    int currCompId = 0;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "onKeyHandler:(" + keyCode + "," + noKeyDown + ")\n" + mNavCompsMagr.toString());

    onHideLoading();

    keyCode = KeyMap.getKeyCode(keyCode, event);

    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
      if (DvrManager.getInstance()!=null && !DvrManager.getInstance().pvrIsRecording()) {
        DvrManager.getInstance().uiManager.hiddenAllViews();
      }
    }
    if (event != null) {
      // step 1. check active component
      currCompId = ComponentsManager.getActiveCompId();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "step 1. check active component: " + currCompId);
      // for CR:00586734
      if ((currCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5)
          && (keyCode == KeyMap.KEYCODE_MTKIR_GUIDE)) {
        reqMhegResume = true;
      }
      if(DvrManager.getInstance().pvrIsRecording() && ComponentsManager.getNativeActiveCompId()== NavBasic.NAV_NATIVE_COMP_ID_HBBTV &&
              (keyCode == KeyMap.KEYCODE_MTKIR_RED ||
                      keyCode == KeyMap.KEYCODE_MTKIR_BLUE ||
                      keyCode == KeyMap.KEYCODE_MTKIR_YELLOW ||
                      keyCode == KeyMap.KEYCODE_MTKIR_GREEN)){
        Toast.makeText(this, R.string.nav_tip_AD_feature_not_available, Toast.LENGTH_LONG).show();
      }
      // native component active
      if ((currCompId & NavBasic.NAV_NATIVE_COMP_ID_BASIC) != 0) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "native component active");
        //
        if ((MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT))
            && (currCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5)
            && (keyCode == KeyMap.KEYCODE_MTKIR_PIPPOP)) {
          PIPPOPSurfaceViewControl.getSurfaceViewControlInstance()
              .changeOutputWithTVState(PipPopConstant.TV_PIP_STATE);
        }

        if (mKeyDispatch.passKeyToNative(keyCode, event) == true) {
          return true;
        } else {
          // some key should not pass to native UI or pass fail, android handle it again
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "android handle it again");
        }
      } else if (((currCompId & NavBasic.NAV_COMP_ID_BASIC) != 0) && (!noKeyDown)) {
        // current active comp is not dialog
  
        if (mNavCompsMagr.getComponentById(currCompId).onKeyHandler(keyCode, event)) {
          return true; // handled, returned directly
        }
      }

      // android component active
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "android component active");
      if (noKeyDown) {
        isActiveCompHandled = mNavCompsMagr.dispatchKeyToActiveComponent(keyCode, event);
      } else {
        isActiveCompHandled = mNavCompsMagr.dispatchKeyToActiveComponent(keyCode, event, noKeyDown);
      }
    } else { // key from native
      // step 2. contains android active component
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "step 2. contains android active component");
      isActiveCompHandled = mNavCompsMagr.dispatchKeyToActiveComponent(keyCode, event, noKeyDown);
    }

    // step 3. the active component do not handle the key, call isKeyHandler
    if (isActiveCompHandled) {
      return true;
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "step 3. the active component do not handle the key, call isKeyHandler");
      NavBasic comp = mNavCompsMagr.showNavComponent(keyCode, event);
      if (comp != null) {
        // comp.onKeyHandler(keyCode, event);
        return true;
      }
    }

    // step 4. default handled
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "step 4. default handled");

    keyCode = KeyMap.getKeyCode(keyCode, event);

    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_YELLOW:
        break;
      case KeyMap.KEYCODE_MTKIR_SUBTITLE:
      case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
        if (!CommonIntegration.isEURegion()
            || !CommonIntegration.getInstance().isCurrentSourceATV()
                && !CommonIntegration.getInstance().isCurrentSourceDTV()
                && !CommonIntegration.getInstance().isCurrentSourceComposite()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "current region don't support subtitle, return.");
          break;
        }
        Intent subIntent = new Intent(this, LiveTvSetting.class);
        subIntent.putExtra(
            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_TYPE_SUBTITLE_SRC);
        startActivity(subIntent);
        break;
      case KeyMap.KEYCODE_DPAD_UP:
        if (!mCommonIntegration.isCurrentSourceTv()) {
          break;
        }
      case KeyMap.KEYCODE_MTKIR_CHUP:
        {
          if (DataSeparaterUtil.getInstance() != null
              && !DataSeparaterUtil.getInstance().isCHUPDOWNACTIONSupport()
              && !mCommonIntegration.isCurrentSourceTv()) {
            break;
          }
          if (mTTS.isTTSEnabled()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                TAG,
                "KEYCODE_MTKIR_CHUP~ chupordowntime"
                    + chupordowntime
                    + " System.currentTimeMillis() "
                    + SystemClock.uptimeMillis());
            if ((SystemClock.uptimeMillis() - chupordowntime) < 1000) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_CHUP~ chupordowntime" + chupordowntime);
              break;
            } else {
              chupordowntime = SystemClock.uptimeMillis();
            }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(
              TAG,
              "KEYCODE_MTKIR_CHUP mTurnkeyReceiver.mCanChangeChannel"
                  + mTurnkeyReceiver.mCanChangeChannel);
          if (mTurnkeyReceiver.mCanChangeChannel) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel changed ~isUKCountry :" + isUKCountry);
            if (isUKCountry) {
              mHandler.removeMessages(UKChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
              mTurnkeyReceiver.mCanChangeChannel = false;
              mHandler.sendEmptyMessageDelayed(
                  UKChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                  UKChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
              mThreadHandler.post(mTurnkeyReceiver.mChannelUpRunnable);
            } else {
              dismissPwddialog();
              mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
              mTurnkeyReceiver.mCanChangeChannel = false;
              mHandler.sendEmptyMessageDelayed(
                  ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                  ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
              mThreadHandler.post(mTurnkeyReceiver.mChannelUpRunnable);
            }
          }
          break;
        }
      case KeyMap.KEYCODE_DPAD_DOWN:
        if (!mCommonIntegration.isCurrentSourceTv()) {
          break;
        }
      case KeyMap.KEYCODE_MTKIR_CHDN:
        {
          if (DataSeparaterUtil.getInstance() != null
              && !DataSeparaterUtil.getInstance().isCHUPDOWNACTIONSupport()
              && !mCommonIntegration.isCurrentSourceTv()) {
            break;
          }
          if (mTTS.isTTSEnabled()) {
            if ((SystemClock.uptimeMillis() - chupordowntime) < 1000) {
              break;
            } else {
              chupordowntime = SystemClock.uptimeMillis();
            }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(
              TAG, "KEYCODE_MTKIR_CHDN.mCanChangeChannel" + mTurnkeyReceiver.mCanChangeChannel);
          if (mTurnkeyReceiver.mCanChangeChannel) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel changed ~");
            if (isUKCountry) {
              mHandler.removeMessages(UKChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
              mTurnkeyReceiver.mCanChangeChannel = false;
              mHandler.sendEmptyMessageDelayed(
                  UKChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                  UKChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
              mThreadHandler.post(mTurnkeyReceiver.mChannelDownRunnable);
            } else {
              dismissPwddialog();
              mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
              mTurnkeyReceiver.mCanChangeChannel = false;
              mHandler.sendEmptyMessageDelayed(
                  ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                  ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
              mThreadHandler.post(mTurnkeyReceiver.mChannelDownRunnable);
            }
          }
          break;
        }
      case KeyMap.KEYCODE_MTKIR_PRECH:
        {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(
              TAG, "KEYCODE_MTKIR_PRECH mCanChangeChannel" + mTurnkeyReceiver.mCanChangeChannel);
          if (mTurnkeyReceiver.mCanChangeChannel) {
            mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
            mTurnkeyReceiver.mCanChangeChannel = false;
            mHandler.sendEmptyMessageDelayed(
                ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
            mThreadHandler.post(mTurnkeyReceiver.mChannelPreRunnable);
          }
          break;
        }
      case KeyMap.KEYCODE_MTKIR_GUIDE:
        {
          if (event == null || event.getRepeatCount() <= 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Gudie key received!");
            showEPG();

          }
          break;
        }
        /*case KeyMap.KEYCODE_MTKIR_STOP: {
            if (mTurnkeyReceiver.mCanChangeChannel) {
                mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
                mTurnkeyReceiver.mCanChangeChannel = false;
                mHandler.sendEmptyMessageDelayed(
                        ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                        ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
                ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_KEY_OCCUR, keyCode);

                if (DvrManager.getInstance() != null
                        && !DvrManager.getInstance().tshiftIsRunning()) {
                    if (!FavChannelManager.getInstance(this).changeChannelToNextFav()) {
                        mTurnkeyReceiver.mCanChangeChannel = true;
                    }
                } else {
                    mTurnkeyReceiver.mCanChangeChannel = true;
                    return false;
                }
            }
            break;
        }*/
      case KeyMap.KEYCODE_BACK:
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
          DvrManager.getInstance().uiManager.hiddenAllViews();
        }

        TwinkleDialog.showTwinkle();
        if (DataSeparaterUtil.getInstance().isCHRTNEnabled()){
          if (isComponentShow()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isComponentsShow");
          }else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isComponentsShow  false");
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_LAST_CHANNEL));
          }
        }
        break;
      default:
        break;
    }

    return true;
  }

  boolean isComponentShow(){
      List<Integer> cpmIDs = ComponentsManager.getInstance()
              .getCurrentActiveComps();
      boolean show = false;
      for (Integer id : cpmIDs) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isComponentsShow id:"+id);
          com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isComponentsShow NavBasic.NAV_COMP_ID_TWINKLE_DIALOG:"+NavBasic.NAV_COMP_ID_TWINKLE_DIALOG);
          if ((id == NavBasic.NAV_COMP_ID_TWINKLE_DIALOG && !SaveValue.getInstance(this).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START))
                   || (id == NavBasic.NAV_COMP_ID_PVR_TIMESHIFT && (StateDvr.getInstance() != null
                   && StateDvr.getInstance().isSmallCtrlBarShow())
                   && ((StateDvrFileList.getInstance() == null)
                           || (StateDvrFileList.getInstance() != null
                               && !StateDvrFileList.getInstance().isShowing())) )){
              show = false;
          }else{
              show = true;
              break;
          }
      }
      return show;
  }

  /**
   * event != null && noKeyDown=true, key from dialog
   * event == null && noKeyDown=true, key from native linux UI
   * noKeyDown=false, key from android framework or other view
   */
  public boolean onKeyHandler(int keyCode, KeyEvent event) {
    return onKeyHandler(keyCode, event, true);
  }

  /** check pre initial status, may have to start other activity */
  private void preInit() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, " preInit()");
    mHandlerThread = new HandlerThread(TAG);
    mHandlerThread.start();
    mThreadHandler = new Handler(mHandlerThread.getLooper());

    LayoutInflater inflater =
        (LayoutInflater) this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    OriginalView = (FrameLayout) inflater.inflate(R.layout.nav_main_layout_test, null);
    setContentView(OriginalView);

    mTvView = (TvSurfaceView) OriginalView.findViewById(R.id.nav_tv_base_view);
    mPipView = (TvSurfaceView) OriginalView.findViewById(R.id.nav_pip_base_view);

    FrameLayout tvAppframe = (FrameLayout)OriginalView.findViewById(R.id.nav_tv_app_view);
    TvAppView tvAppView = new TvAppView(this, null);
    tvAppView.run(null, null, null);
    tvAppframe.addView(tvAppView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    mBlockScreenForTuneView = (TvBlockView) OriginalView.findViewById(R.id.block_screen_for_tune);
    mTvView.setBlockView(mBlockScreenForTuneView);
    mBlockScreenForTuneView.setVisibility(
        View.VISIBLE, TvBlockView.BLOCK_UNTIL_TUNE_INPUT_AFTER_BOOT);
    mTifTimeShiftManager = TifTimeShiftManager.getInstanceEx(this, mTvView);
    mTvView.setZOrderMediaOverlay(false);
    mPipView.setZOrderMediaOverlay(true);
    mSundryLayout = (LinearLayout) OriginalView.findViewById(R.id.nav_sundry_layout);
    mProgressBar = (ProgressBar) OriginalView.findViewById(R.id.fbm_mode_progressbar);
    mTvView.setCallback(TvInputCallbackMgr.getInstance(this).getTvInputCallback());
    mPipView.setCallback(TvInputCallbackMgr.getInstance(this).getTvInputCallback());
    mTvView.setOnUnhandledInputEventListener(this);

    SaveValue.getInstance(this).saveBooleanValue(MenuConfigManager.PVR_START, false);
    if (Settings.Global.getInt(
            getContentResolver(), VgaPowerManager.KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF, -1)
        == -1) {
      Settings.Global.putInt(
          getContentResolver(), VgaPowerManager.KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF, 3);
    }
  }

  /** Initial Turnkey UI resources */
  private void init() {
    /* Add components to NavComponentsManager */
    mNavCompsMagr = ComponentsManager.getInstance();
    mNavCompsMagr.deinitComponents();
    mNavCompsMagr.clear();
    mCommonIntegration = TvSingletons.getSingletons().getCommonIntegration();
    mTTS = new TextToSpeechUtil(this);
    mEas = new MtkTvEASBase();
    mTurnkeyReceiver = new TurnkeyReceiver(this);
    boolean istwn = TVContent.getInstance(this).isOneImageTWN();
    boolean isphl = TVContent.getInstance(this).isOneImagePHL();
    boolean issurone = TVContent.getInstance(this).isSupportOneImage();
    boolean iscol  = TVContent.getInstance(this).isCOLRegion();
    /* add dialog */
    addNavDialog();

    /* add view */
    {
      // Zoom
      mNavCompsMagr.addView((ZoomTipView) findViewById(R.id.nav_zoomview));
      // SundryShowText
      mNavCompsMagr.addView((SundryShowTextView) findViewById(R.id.nav_tv_shortTip_textview));
      // BannerView
      // BannerView
      RelativeLayout.LayoutParams mParams =
          new RelativeLayout.LayoutParams(
              RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
      RelativeLayout bannerLL = (RelativeLayout) findViewById(R.id.nav_banner_info_bar_all);
      bannerLL.removeAllViews();
      if (!isUKCountry) {
        mBannerView = new BannerView(this);
        mBannerView.setLayoutParams(mParams);
        bannerLL.addView(mBannerView);
        mNavCompsMagr.addView(mBannerView);
        mBannerView.setVisibility(View.GONE);
      } else {
        mUkBannerView = new UkBannerView(this);
        mUkBannerView.setLayoutParams(mParams);
        bannerLL.addView(mUkBannerView);
        mNavCompsMagr.addView(mUkBannerView);
        mUkBannerView.setVisibility(View.GONE);
      }

      TifTimeshiftView timeshiftView = (TifTimeshiftView) findViewById(R.id.nav_tiftimeshift);
      mNavCompsMagr.addView(timeshiftView);
      timeshiftView.setListener(getmTifTimeShiftManager());

      // misc, should be always the last component
      mNavCompsMagr.addView((MiscView) findViewById(R.id.nav_misc_textview));
    }

    /* add object */
    {
      // Menu option
      mNavCompsMagr.addMisc(new MenuOptionMain(this));
      // TimeShiftManager
      mNavCompsMagr.addMisc(DvrManager.getDvrmanage(getApplicationContext()));
      // TTX
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TTX) && !istwn) {
        mNavCompsMagr.addMisc(new TTXMain(this));
      }
      // HBBTV
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_HBBTV) && !istwn &&!iscol) {
        mNavCompsMagr.addMisc(new Hbbtv(this));
      }
      // MHEG5
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MHEG5) && !istwn) {
        mNavCompsMagr.addMisc(new Mheg5(this));
      }
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_FVP)) {
        mNavCompsMagr.addMisc(new FVP(this));
      }

      // EAS
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EAS)) {
        mNavCompsMagr.addMisc(new FloatView(this));
      }

      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
        mTvView.setHandleEvent(true);
        mPipView.setHandleEvent(true);

        // FocusLabelControl in TIF
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add MultiViewControl");
          MultiViewControl mMultiViewControl = new MultiViewControl(this);
          mMultiViewControl.setFocusLable((FocusLabel) findViewById(R.id.nav_pip_focus_picture));
          mNavCompsMagr.addMisc(mMultiViewControl);
          mMultiViewControl.setOutputView(mTvView, mPipView, mainSurfaceViewLy, subSurfaceViewLy);
        }
      } else {
        // FocusLabelControl
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add FocusLabelControl");
        FocusLabelControl mFocusLabelControl = new FocusLabelControl(this);
        mFocusLabelControl.setFocusLable((FocusLabel) findViewById(R.id.nav_pip_focus_picture));
        mNavCompsMagr.addMisc(mFocusLabelControl);
      }

      // Power
      mVgaPowerManager = new VgaPowerManager(this);
      mNavCompsMagr.addMisc(mVgaPowerManager);
      // BML
      if (issurone) {
        if (isphl) {
          mNavCompsMagr.addMisc(BMLMain.getInstance(this));
        }
      } else if (MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
        mNavCompsMagr.addMisc(BMLMain.getInstance(this));
      }
       mNavCompsMagr.addMisc(new AtscInteractive(this));
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, mNavCompsMagr.toString()); // delete

    // add broadcast
    mTurnkeyReceiver.registerTvReceiver();
  }

  /** check initial status and default resume components in post initial step */
  private void postInit(boolean isShutFlow) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, " postInit()");

    TVAsyncExecutor.getInstance().execute(
            () -> {
              // call api to start notifying oad status
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "postInit||getPowerOnStatus");
              MtkTvOAD.getInstance().getPowerOnStatus();

              String temp = SaveValue.readWorldStringValue(this, "isFirstEnterTVAfterACON");
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "postInit||temp>>"+temp);
              if(TextUtils.isEmpty(temp)) {
                SaveValue.writeWorldStringValue(this,"isFirstEnterTVAfterACON", "false", false);
              }
              int msgType = MtkTvCI.getInstance(0).getCamScanReqTypeACON();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "postInit|| CI cam scan msgType>> "+msgType);
              if(msgType > 10 && msgType < 15){
                TvCallbackData tdata = new TvCallbackData();
                tdata.param1 = 0;//only one slot by default
                tdata.param2 = msgType;
                Message msg = mHandler.obtainMessage();
                msg.what = msg.arg1 = msg.arg2 = TvCallbackConst.MSG_CB_CI_MSG;
                msg.obj = tdata;
                mHandler.sendMessage(msg);
              }
            });

    if (mNavCompsMagr.isCompsDestroyed()) {
      init();
    }

    // if native app actived, this API may affect its action
    initConfig(isStartTv);

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " postInit(),onResume()-6,isStartTv = " + isStartTv);
    if (isStartTv) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " postInit(),onResume()-6");
      if (MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
            TAG,
            " postInit(),onResume()-6,mEas.EASGetAndroidLaunchStatus()="
                + mEas.EASGetAndroidLaunchStatus());
        if (mEas.EASGetAndroidLaunchStatus()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " postInit(),onResume()-6,EASSetAndroidLaunchStatus(false)");
          mEas.EASSetAndroidLaunchStatus(false);
        }
      }
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
        boolean liveTVCreated = SaveValue.readWorldBooleanValue(this, "LiveTVCreated");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "LiveTVCreated:" + liveTVCreated);
        if ((!liveTVCreated || isShutFlow) && mCommonIntegration.selectPowerOnChannel()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " postInit(),onResume()-7," + "mCommonIntegration.selectPowerOnChannel()");
          if (!liveTVCreated) {
            SaveValue.saveWorldBooleanValue(this, "LiveTVCreated", true, false);
          }
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(
              TAG, " postInit(),onResume()-8," + "mNoNeedChangeChannel:" + mNoNeedChangeChannel);
          if (!mNoNeedChangeChannel && getTvView() != null && !getTvView().isStart()) {
            mInputSourceManager.resetCurrentInput();
          }
        }

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
          getPipView().setStreamVolume(0.0f);
          getTvView().setStreamVolume(1.0f);
        }
      }
      if (!isNeedStartSourceWithNotResume) {
        isStartTv = false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "777777");
      }
    } else if (getTvView() != null && getTvView().isStart()) {
          int curChannelId = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_NAV_AIR_CRNT_CH);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"curChannelId:"+curChannelId);
          if(curChannelId == -1 && mCommonIntegration.isCurrentSourceTv()) {
            int current3rdMId = SaveValue.getInstance(this).readValue(TIFFunctionUtil.current3rdMId, -1);
            if(current3rdMId == -1) {
//              getTvView().reset();
              mInputSourceManager.resetCurrentInput();
              isStartTv = false;
            }
          }
        }

    /* send navigator resume message */
    ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_RESUME, 0);

    // select channel and resume banner...
    if (!TurnkeyUiMainActivity.getInstance().isUKCountry) {
      ((BannerView) mNavCompsMagr.getComponentById(NavBasic.NAV_COMP_ID_BANNER))
          .setInterruptShowBanner(false);
    }
  }

  private void receiveIntent(Intent intent) {
    String action = intent.getAction();
    FloatView floatView =
        (FloatView) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_EAS);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onNewIntent: action:" + intent + ", floatView: " + floatView);
    if (floatView != null && floatView.isEasPlaying()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "eas message is playing!!!");
      return;
    }

    if (Intent.ACTION_VIEW.equals(action)) {
      mNoNeedResetSource = false;

      boolean bLiveTv = intent.getBooleanExtra("livetv", false);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "bLiveTv:" + bLiveTv);
      if (bLiveTv) {
        showBannerInOnResume = false;
      }
      do {
        if(InputSourceManager.getInstance().isNotProcessInputUri(intent, getTvView())) {
          break;
        }
        showBannerInOnResume = false;
        processInputUri(intent);
      } while (false);
    } else if ("android.mtk.intent.action.ACTION_REQUEST_TOP_RESUME".equals(action)) {
      if (intent.getBooleanExtra("FORCE_LAUNCH_ON_BOOT", false)) {
        NetflixUtil.checkNetflixKeyWhenForceLaunchonBoot(this);
      }
      Boolean showSourceList = intent.getBooleanExtra("showSourceList", false);
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onResume showSourceList=" + showSourceList);
      if (showSourceList) {
        isStartTv = true;
      }
    } else if ("android.media.tv.action.SETUP_INPUTS".equals(action)) {
      startActivity(
          new Intent(this, SetupSourceActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    } else if ("com.android.tv.action.FORCE_LAUNCH_ON_BOOT".equals(action)) {
      NetflixUtil.checkNetflixKeyWhenForceLaunchonBoot(this);
    }

    if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
      StateDvrPlayback.getInstance().stopDvrFilePlay();
    }
  }

  private Runnable mHotKeyRun;

  private void receiveFVPIntent(Intent intent) {
    final Intent newintent = new Intent(intent);
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "receiveFVPIntent=" + newintent.toString());

    Disposable disposeable = Single.defer(() -> Single.just(InputSourceManager.getInstance().isBlock("DTV")))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .onErrorReturn(throwable -> {
          throwable.printStackTrace();
          return true;
        })
        .doOnSuccess(isBlocked -> {
          if (isBlocked) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "receiveFVPIntent isMenuInputTvBlock");
            return;
          }

          String action = newintent.getAction();

          //channel list hot-key
          boolean hasChannel = newintent.getBooleanExtra("CHANNEL_LIST", false);
          if (Intent.ACTION_VIEW.equals(action)
              && newintent.getData().toSafeString().startsWith(TvContract.Channels.CONTENT_URI.toSafeString())
              && hasChannel) {
            toggleChannelList();
            intent.putExtra("CHANNEL_LIST", false);
            return;
          }
          mHotKeyRun =
              () -> {
                String action1 = newintent.getAction();
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "receiveFVPIntent=" + newintent.toString());
                if (newintent.getBooleanExtra("SetupSource", false)) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FVP: SetupSource");
                  return;
                }
                if (Intent.ACTION_VIEW.equals(action1)) {
                  boolean epg = newintent.getBooleanExtra("EPG", false);
                  final String sGLOBALTAG = "GLOBAL_TAG";
                  if (newintent.hasExtra(sGLOBALTAG)) {
                    int tag = newintent.getIntExtra(sGLOBALTAG, -1);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG:" + epg + newintent.getData() + tag);
                    if (tag == SaveValue.readLocalMemoryIntValue(sGLOBALTAG)) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FVP: repeat message");
                      return;
                    }
                    SaveValue.setLocalMemoryValue(sGLOBALTAG, tag);
                  }

                  if (epg
                      && newintent
                      .getData()
                      .toString()
                      .startsWith(TvContract.Channels.CONTENT_URI.toString())) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "send to onKeyDown");
                    KeyEvent guideEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyMap.KEYCODE_MTKIR_GUIDE);
                    getMainExecutor().execute(() -> onKeyDown(guideEvent.getKeyCode(), guideEvent));
                    return;
                  }

                  boolean fvp = newintent.getBooleanExtra("FVP", false);
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FVP:" + fvp);

                  if (fvp && !CommonIntegration.getInstance().is3rdTVSource()) { // FVP key
                    mKeyDispatch.passKeyToNative(KeyMap.KEYCODE_MTKIR_SUBCODE, null);
                  }
                }
              };
          mThreadHandler.post(mHotKeyRun);
        }).subscribe();
    mDisposables.add(disposeable);
  }

  public boolean processInputUri(Intent intent) {
    mNoNeedResetSource = InputSourceManager.getInstance().processInputUri(intent);
    mNoNeedChangeChannel = mNoNeedResetSource;
    return mNoNeedResetSource;
  }

  private void checkNetflixResume() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkNetflixResume");
    if (!NetflixUtil.isNetflixKeyResume()) {
      postInit(true);
    }
  }

  public LinearLayout getSundryLayout() {
    return mSundryLayout;
  }

  public TvSurfaceView getTvView() {
    return mTvView;
  }

  public TvSurfaceView getPipView() {
    return mPipView;
  }

  public TvBlockView getBlockScreenView() {
    return mBlockScreenForTuneView;
  }

  /**
   * this method is used to get the handler of TurnkeyUiMainActivity
   *
   * @return
   */
  public Handler getHandler() {
    return this.mHandler;
  }

  /**
   * this method is used to get the object of TurnkeyUiMainActivity
   *
   * @return
   */
  public static TurnkeyUiMainActivity getInstance() {
    return mainActivity;
  }

  public static void resumeTurnkeyActivity(Context content) {
    if (DestroyApp.isCurActivityTkuiMainActivity()) {
      return;
    }

    if (content == null) {
      return;
    }

    if (DvrManager.getInstance().isStopDvrNotResumeLauncher()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "isStopDvrNotResumeLauncher = true ");
      return;
    }
    Intent intent = new Intent("android.mtk.intent.action.ACTION_REQUEST_TOP_RESUME");
    intent.addCategory(Intent.CATEGORY_DEFAULT);

    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "resumeTurnkeyActivity");
    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
    content.startActivity(intent);
  }

  private ConfirmDialog mConfirmDialog;

  public ConfirmDialog getConfirmDialog() {
    return mConfirmDialog;
  }

  public void setConfirmDialog(ConfirmDialog dialog) {
    mConfirmDialog = dialog;
  }

  /** show confirm DVBT new auto scan dialog */
  public void showDVBTRegionListDialog() {
    if (TVContent.getInstance(this).isUKCountry() && mCommonIntegration.getSvl() == 1) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog() not UK dvbt return");
      mThreadHandler.postDelayed(
          () -> {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog()-umsg");
            boolean isTVSource = CommonIntegration.getInstance().isCurrentSourceTv();

            if (!isTVSource) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog(),Not TV Source");
              return;
            }

            if (ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
                    != null
                && ComponentsManager.getInstance()
                    .getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
                    .isVisible()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog(),PVR is Running");
              return;
            }

            if (TurnkeyUiMainActivity.getInstance() != null) {
              if (!TurnkeyUiMainActivity.getInstance().isResumed()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog(),Nav Not Resumed");
                return;
              }

              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog>>>");
              if (TVContent.getInstance(this).checkIfNeedShowRegionList()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTRegionListDialog()");
                ScanThirdlyDialog dialog = new ScanThirdlyDialog(this, 2);
                dialog.setCancelable(true);
                dialog.positionCenter = true;
                dialog.show();
              }
            }
          },
          2000);
    }
  }

  public void showDVBTInActiveDialog() {
    DvbtInactiveChannelConfirmDialog dialog = DvbtInactiveChannelConfirmDialog.getInstance(this);
    if (dialog.isShowing()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DvbtInactiveChannelConfirmDialog is showing return");
      return;
    }
    if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
      TVAsyncExecutor.getInstance()
          .execute(
              () -> {
                List<MtkTvChannelInfoBase> inactiveChannelList =
                    mCommonIntegration.getChannelListByMaskFilter(
                        -1,
                        MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,
                        9999,
                        TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK,
                        TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "inactiveChannelList>>>" + inactiveChannelList.size());
                if (inactiveChannelList.size() > 0) {
                  mHandler.showDVBTInactiveChannelsConfirmDialog();
                }
              });
    }
  }

  public void showFvpAdviseScanDialog() {
    if (TVContent.getInstance(this).isSupportFvpScan() && mCommonIntegration.getSvl() == 1) {
      boolean isAdvise = MtkTvScan.getInstance().getScanDvbtInstance().IsAdviseScan();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFvpAdviseScanDialog() dvbt isAdvise=" + isAdvise);
      if (isAdvise) {
        ConfirmDialog.getInstance(this).showFvpAdviseScanDialog(this);
      } /*else {
            boolean isValidAnswer = MtkTvScan.getInstance().getScanDvbtInstance().IsValidAnswerToken();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isValidAnswer="+isValidAnswer);
            if(!isValidAnswer){
                ConfirmDialog.getInstance(this).showFvpAdviseScanDialog(this);
            }
        }*/
    }
  }

  public void showItalyLcnConflictDialog() {
    if (TVContent.getInstance(this).isItaCountry() && mCommonIntegration.getSvl() == 1) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog() Italy dvbt");
      mThreadHandler.postDelayed(
          () -> {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog()-umsg");
            boolean isTVSource = CommonIntegration.getInstance().isCurrentSourceTv();

            if (!isTVSource) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog(),Not TV Source");
              return;
            }

            if (ComponentsManager.getInstance()
                .getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
                .isVisible()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog(),PVR is Running");
              return;
            }

            if (TurnkeyUiMainActivity.getInstance() != null) {
              if (!TurnkeyUiMainActivity.getInstance().isResumed()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog(),Nav Not Resumed");
                return;
              }

              boolean isAutoType =
                  MtkTvConfig.getInstance()
                          .getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER)
                      == 1; //
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog isAutoType>>>" + isAutoType);
              if (isAutoType) {
                MtkTvScanDvbtBase.LcnConflictGroup[] lcnList =
                    MtkTvScan.getInstance().getScanDvbtInstance().uiOpGetLcnConflictGroup();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog lcnList>>>" + lcnList);
                if (lcnList != null && lcnList.length > 0) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showItalyLcnConflictDialog lcnList length>>>" + lcnList.length);
                  ScanThirdlyDialog thirdDialog = new ScanThirdlyDialog(this, 3);
                  thirdDialog.show();
                }
              }
            }
          },
          2000);
    }
  }

  public boolean onUnhandledInputEvent(InputEvent event) {
    if (event instanceof KeyEvent) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onUnhandledInputEvent " + event);

      if (((KeyEvent) event).getAction() == KeyEvent.ACTION_DOWN) {
        onKeyDown(((KeyEvent) event).getKeyCode(), (KeyEvent) event);
      }
    }
    return false;
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent event:" + event);
    if(DvrManager.getInstance() != null&&DvrManager.getInstance().getController().setExitOrStopKey(event)){
      return true;
    }
    if (false == MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_ACR_SUPPORT)) {
      TvInputInfo tvInputInfo =
          InputSourceManager.getInstance().getTvInputInfo(InputSourceManager.MAIN);
      if (tvInputInfo != null
          && tvInputInfo.getType() == tvInputInfo.TYPE_HDMI
          && tvInputInfo.getHdmiDeviceInfo() != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Cec Device connected. dispatch key to cec.");
        MenuOptionMain menuOption =
            ((MenuOptionMain)
                ComponentsManager.getInstance()
                    .getComponentById(NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG));
        if (menuOption != null && menuOption.isShowing()) {
          return super.dispatchKeyEvent(event);
        }
      } else {
        if (event.getKeyCode() == KeyMap.KEYCODE_MENU) {
          if (StateDvrFileList.getInstance() != null
              && StateDvrFileList.getInstance().isShowing()) {
            DvrManager.getInstance().restoreToDefault(StateDvrFileList.getInstance());
          }
        }
        return super.dispatchKeyEvent(event);
      }
    }

    if (CommonIntegration.getInstance().isPipOrPopState()) {
      return super.dispatchKeyEvent(event);
    }

    if (CommonIntegration.getInstance().isCurrentSourceBlockEx()
        && event.getKeyCode() == KeyMap.KEYCODE_DPAD_CENTER) {
      return super.dispatchKeyEvent(event);
    }

    int keyCode = KeyMap.getKeyCode(event);
    if (false == MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_ACR_SUPPORT)) {
      if (Constants.BLACKLIST_KEYCODE_TO_TIS.contains(keyCode)) {
        return super.dispatchKeyEvent(event);
      }
    }
    ZoomTipView mZoomTip =
        ((ZoomTipView)
            ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
    if (IntegrationZoom.getInstance(this).screenModeZoomShow()
        && (IntegrationZoom.getInstance(this).getCurrentZoom()) == 2
        && (mZoomTip != null && mZoomTip.getVisibility() == View.VISIBLE)) {
      if (keyCode == KeyMap.KEYCODE_DPAD_DOWN
          || keyCode == KeyMap.KEYCODE_DPAD_UP
          || keyCode == KeyMap.KEYCODE_DPAD_LEFT
          || keyCode == KeyMap.KEYCODE_DPAD_RIGHT) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "if zoom show , send key to tv not cec");
        return super.dispatchKeyEvent(event);
      }
    }

    return dispatchKeyEventToSession(event) || super.dispatchKeyEvent(event);
  }

  private void dismissPwddialog() {
    PwdDialog mPWDDialog =
        (PwdDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
    if (mPWDDialog != null && mPWDDialog.isVisible()) {
      mPWDDialog.dismiss();
    }
  }

  /**
   * dispatch key to HdmiCec Deivce.
   *
   * @param event
   * @return
   */
  private boolean dispatchKeyEventToSession(final KeyEvent event) {
    boolean handled = false;
    if (mTvView != null && MtkTvPWDDialog.getInstance().PWDShow() != 0) {
      if (event.getKeyCode() == KeyMap.KEYCODE_PAGE_DOWN) {
        KeyEvent newEvent =
            new KeyEvent(
                event.getDownTime(),
                event.getEventTime(),
                event.getAction(),
                KeyEvent.KEYCODE_MEDIA_EJECT,
                event.getRepeatCount(),
                event.getMetaState(),
                event.getDeviceId(),
                event.getScanCode(),
                event.getFlags(),
                event.getSource());
        handled = mTvView.dispatchKeyEvent(newEvent);
      } else if (event.getKeyCode() == KeyEvent.KEYCODE_MUHENKAN) {
        KeyEvent newEvent =
            new KeyEvent(
                event.getDownTime(),
                event.getEventTime(),
                event.getAction(),
                KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK,
                event.getRepeatCount(),
                event.getMetaState(),
                event.getDeviceId(),
                event.getScanCode(),
                event.getFlags(),
                event.getSource());
        handled = mTvView.dispatchKeyEvent(newEvent);
      } else {
        handled = mTvView.dispatchKeyEvent(event);
      }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEventToSession event (" + event + ") handled=(" + handled + ").");
    return handled;
  }

  public TifTimeShiftManager getmTifTimeShiftManager() {
    return mTifTimeShiftManager;
  }

  public MenuOptionMain getTvOptionsManager() {
    if (mNavCompsMagr != null) {
      return (MenuOptionMain)
          mNavCompsMagr.getComponentById(NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG);
    }
    return null;
  }

  public void startActivitySafe(Intent intent) {
    this.startActivity(intent);
  }

  public boolean showEPG() {
    boolean show = false;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showEPG~");
    if (mCommonIntegration.isCurrentSourceTv() || mCommonIntegration.is3rdTVSource()) {
      // Fix CR:
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
        if (StateDvr.getInstance() != null
            && StateDvr.getInstance().isRunning()
            && StateDvrFileList.getInstance() != null
            && StateDvrFileList.getInstance().isRunning()) {
          return show;
        }
      }
      if (SaveValue.getInstance(this).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
        Toast.makeText(this, getResources().getString(R.string.warning_time_shifting_recording), Toast.LENGTH_SHORT).show();
        return true;
      }
      if (CommonIntegration.getInstance().isMenuInputTvBlock()) {
        if (mBannerView != null && !mBannerView.isVisible()) {
          mBannerView.showSimpleBanner();
        }
      } else {
        // fix CR DTV00698269
        Activity act = DestroyApp.getTopActivity();
        if (act != null
            && (act instanceof EPGEuActivity
                || act instanceof EPGSaActivity
                || act instanceof EPGCnActivity
                || act instanceof EPGUsActivity)) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DestroyApp.getTopActivity(): " + act.getComponentName());

          Toast.makeText(this, "Please wait ...", 0).show();
          return show;
        }
        show = EPGManager.getInstance(this).startEpg(this, NavBasic.NAV_REQUEST_CODE);
        if (!show && mBannerView != null && !mBannerView.isVisible()) {
          mBannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
        }
      }
    }

    return show;
  }

  public void sendMuteMsg(boolean isMute) {
    mHandler.removeMessages(EPGConfig.EPG_MUTE_VIDEO_AND_AUDIO);
    Message msg = Message.obtain();
    msg.what = EPGConfig.EPG_MUTE_VIDEO_AND_AUDIO;
    msg.obj = isMute;
    mHandler.sendMessage(msg);
  }

  protected void muteVideoAndAudio(boolean isBarkerChannel) {
    if (isBarkerChannel) {
      if (!mBlockScreenForTuneView.isBlock(TvBlockView.BLOCK_BY_EVENT)) {
        mBlockScreenForTuneView.setVisibility(View.VISIBLE, TvBlockView.BLOCK_BY_EVENT);
      }
      mAudioManager.muteTVAudio(AudioFocusManager.AUDIO_EPG_BARKER);
    } else if (!isBarkerChannel) {
      if (mBlockScreenForTuneView.isBlock(TvBlockView.BLOCK_BY_EVENT)) {
        mBlockScreenForTuneView.setVisibility(View.GONE, TvBlockView.BLOCK_BY_EVENT);
      }
      mAudioManager.unmuteTVAudio(AudioFocusManager.AUDIO_EPG_BARKER);
    }
  }

  @Override
  public void onShowLoading() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvInputCallbackMgr", "onShowLoading");
    if (mProgressBar != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          "TvInputCallbackMgr",
          "onShowLoading " + mProgressBar.getVisibility() + " " + mProgressBar.isShown());
      mProgressBar.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onHideLoading() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onHideLoading");
    if (mProgressBar != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          "TvInputCallbackMgr",
          "onHideLoading " + mProgressBar.getVisibility() + " " + mProgressBar.isShown());
      mProgressBar.setVisibility(View.GONE);
    }
  }

  @Override
  public void onPinChecked(boolean checked, int type, String rating) {
    if (type == PinDialogFragment.PIN_DIALOG_TYPE_START_SCAN) {
      if (checked) {
        int tuneMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SRC);
        if (tuneMode == 0) {
          Intent intent = new Intent(this, ScanDialogActivity.class);
          intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBT);
          this.startActivity(intent);
        } else if (tuneMode == 1) {
          Intent intent = new Intent(this, ScanViewActivity.class);
          intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBC);
          this.startActivity(intent);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("onPinChecked showDTVScan");
      }
    }
  }

  @Override
  public void surfaceDestroyed() {
    resetInputFlag();
  }

  private void addNavDialog() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addNavDialog");
    ComponentsManager mNavCompsMagr = ComponentsManager.getInstance();
    // InfoBar
    mNavCompsMagr.addDialog(new InfoBarDialog(this, R.layout.nav_ib_view));
    // Password Dialog
    mNavCompsMagr.addDialog(new PwdDialog(this));
    // channel list
    if (!isUKCountry) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onConfigurationChanged language updated and refresh mChannelListDialog.");
      mNavCompsMagr.addDialog(new ChannelListDialog(this));
    } else {
      mNavCompsMagr.addDialog(new UKChannelListDialog(this));
    }
    // Fav list
    mNavCompsMagr.addDialog((new FavoriteListDialog(this)));
    // CI Dialog
    mNavCompsMagr.addDialog(new CIMainDialog(this));
    // sundry dialog
    mNavCompsMagr.addDialog(new SundryShowWithDialog(this));

    // Ginga TV
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GINGA)
        && DataSeparaterUtil.getInstance() != null
        && DataSeparaterUtil.getInstance().isSupportGinga()) {
      mNavCompsMagr.addDialog(new GingaTvDialog(this));
    }
    // CommonMsgDialog
    if (MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
      mNavCompsMagr.addDialog(new CommonMsgDialog(this));
    }
    // EWSDialog
    if (MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(
      MtkTvConfigType.S3166_CFG_COUNT_IDN)) {
      mNavCompsMagr.addDialog(new EWSDialog(this));
    }
    // SimpleDialog
    mNavCompsMagr.addDialog(new SimpleDialog(this));
    // OneKeyMenuDialog
    mNavCompsMagr.addDialog(new OneKeyMenuDialog(this));
    mNavCompsMagr.addDialog(new TwinkleDialog(this));
  }

  public void toggleChannelList(){
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "toggleChannelList");
    if(DestroyApp.isCurActivityTkuiMainActivity()
            && InputSourceManager.getInstance().isCurrentTvSource(CommonIntegration.getInstance().getCurrentFocus())){
      NavBasicDialog chListDialog = (NavBasicDialog) ComponentsManager.getInstance()
              .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
      if (!chListDialog.isVisible()){
        mNavCompsMagr = ComponentsManager.getInstance();
        mNavCompsMagr.hideAllComponents();
        if (chListDialog.isKeyHandler(KeyEvent.KEYCODE_YEN)){
          chListDialog.show();
        }else{
          Toast.makeText(this, getString(R.string.nav_please_scan_channels), Toast.LENGTH_LONG).show();
        }
      }else{
        chListDialog.dismiss();
      }
    }
  }
}
