package com.mediatek.wwtv.tvcenter.util;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.tv.parental.ContentRatingsManager;
import com.android.tv.parental.ParentalControlSettings;
import com.android.tv.util.TvInputManagerHelper;
import com.mediatek.mtkmdsclient.MdsClient;
import com.mediatek.support.fvp.FreeviewPlayClient;
import com.mediatek.twoworlds.tv.MtkTvHBBTV;
import com.mediatek.twoworlds.tv.MtkTvHBBTVBase;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyService;
import com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import mediatek.sysprop.VendorProperties;

public class DestroyApp extends Application implements TvSingletons,Application.ActivityLifecycleCallbacks {
  private static final String TAG = "DestroyApp";
  private static final String AUTO_TEST_CHANGE_SOURCE_KEY = "auto_test_change_source";
  private static final Uri BASE_URI = Settings.Global.CONTENT_URI;

    private static Stack<Dialog> mainDialogs = new Stack<Dialog>();
  private static boolean isAppAlive = false;
  private static Activity mRunningActivity = null;

  public static Context appContext;
  private HashMap<Class<?>, Integer> clsCounter = new HashMap<>();
  private boolean mTurnkeyUiMainActivityActive = false;
    private static int activityCount = 0;//current apk activity account

  public static Activity getTopActivity() {
    return mRunningActivity;
  }

  public static void setRunningActivity(Activity activity) {
    mRunningActivity = activity;
  }

  public static String getRunningActivity() {
    if (mRunningActivity == null) {
      return "";
    } else {
      return mRunningActivity.getClass().getSimpleName();
    }
  }

  public static boolean isCurTaskTKUI() {
    return isAppAlive;
  }

  public static void setActivityActiveStatus(boolean isActive) {
    // Log.d(TAG, "setActivityActiveStatus: " + isActive);
    isAppAlive = isActive;
  }

  public static boolean isCurActivityTkuiMainActivity() {
    return getRunningActivity().equals("TurnkeyUiMainActivity");
  }

  public static boolean isCurActivityInputsPanel() {
    return getRunningActivity().equals("InputsPanelActivity");
  }

  public static boolean isCurOADActivityNew() {
    return getRunningActivity().equals("NavOADActivityNew");
  }

  public static boolean isCurEPGActivity() {
    String name = getRunningActivity();
    return "EPGUsActivity".equals(name)
        || "EPGEuActivity".equals(name)
        || "EPGCnActivity".equals(name)
        || "EPGSaActivity".equals(name);
  }

  public static boolean isCurLiveTvSettingActivity() {
      String name = getRunningActivity();
      return "LiveTvSetting".equalsIgnoreCase(name);
    }

  public static boolean isCurScanActivity() {
    String name = getRunningActivity();
    return "ScanDialogActivity".equals(name) || "ScanViewActivity".equals(name);
  }

  private RefWatcher refWatcher;

  public static RefWatcher getRefWatcher(Context context) {
    return ((DestroyApp) context.getApplicationContext()).refWatcher;
  }

  /** init some data to catch exceptions . */
  private void initDataFromSharedPreference() {
    // to prevent the timeshift power off  when data cannot be saved .
    SaveValue.setLocalMemoryValue("mPreConfigsFlags", 0);
    SaveValue.setLocalMemoryValue("showUpgradeMsg", true);
    SaveValue.getInstance(this).setLocalMemoryValue(MenuConfigManager.TIMESHIFT_START, false);
    //SaveValue.saveWorldBooleanValue(this, MenuConfigManager.TIMESHIFT_START, false, false);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    MdsClient.setContext(this);

        registerActivityLifecycleCallbacks(this);
    Log.i(TAG, getPackageName());
    FreeviewPlayClient.init(this);
    appContext = this;

    if ((new java.io.File("/data/tkui.memory")).exists()) {
      if (LeakCanary.isInAnalyzerProcess(this)) {
        return;
      }
      refWatcher = LeakCanary.install(this);
    }

    /* initial global info */
    try {
      getTvInputManagerHelper();
      KeyDispatch.getInstance();
      getCommonIntegration();
      getChannelDataManager();
      getProgramDataManager();
      getInputSourceManager();
      TvCallbackHandler.getInstance();
    } catch (Exception ex) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception: " + ex);
    }
    // Fixed CR  DTV00726760  Fixed by sin_yajuncheng April 27
    initDataFromSharedPreference();

    Intent i = new Intent(this, TurnkeyService.class);
    startForegroundService(i);

    getContentResolver()
        .registerContentObserver(buildUri(AUTO_TEST_CHANGE_SOURCE_KEY), true, contentObserver);

    closeNativeUI();
  }

  /***
   * LiveTV opened EPG/miniGuide, if it crashes or be killed, then re-launch LiveTV.
   * LiveTV will show EPG/miniGuide and  Banner. and can't reactive with any keys.
   * closeNativeUI in process start can avoid this issue
   */
  private void closeNativeUI() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i("closeNativeUI");
    MtkTvHBBTV.getInstance()
        .setExternalData(
            MtkTvHBBTVBase.ExternalDataType.EXTERNAL_DATA_TYPE_LIVE_TV.ordinal(), new int[] {0});
  }

  private ContentObserver contentObserver =
      new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange, Uri uri) {
          super.onChange(selfChange, uri);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come to change source , slefChange:" + selfChange);
          if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
            new Handler(Looper.getMainLooper())
                .postDelayed(
                    new Runnable() {

                      @Override
                      public void run() {
                        autoTestChangeSource();
                      }
                    },
                    5000);
          }
        }
      };

  private void autoTestChangeSource() {
    String action = Settings.Global.getString(getContentResolver(), AUTO_TEST_CHANGE_SOURCE_KEY);
    Settings.Global.putString(getContentResolver(), AUTO_TEST_CHANGE_SOURCE_KEY, "");
    if (TextUtils.isEmpty(action)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "somethings error.");
      return;
    }
    String sourcename = action.substring(action.lastIndexOf(".") + 1);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectSourceReceiver,sourcename" + sourcename);
    if (mInputSourceManager != null
        && !sourcename.equalsIgnoreCase(
            mInputSourceManager.autoChangeTestGetCurrentSourceName(
                mCommonIntegration.getCurrentFocus()))) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectSourceReceiver,autoChangeTestSourceChange =" + sourcename);
      mInputSourceManager.autoChangeTestSourceChange(
          sourcename, mCommonIntegration.getCurrentFocus());
    }
  }

  private Uri buildUri(String uId) {
    return BASE_URI.buildUpon().appendPath(uId).build();
  }

  @Override
  public void onTerminate() {
    super.onTerminate();

    /* destroy global info */
    KeyDispatch.remove();
    TvCallbackHandler.getInstance().removeAll();
    CommonIntegration.remove();
    InputSourceManager.remove();

    mTvInputManagerHelper.stop();
    mTvInputManagerHelper = null;
  }

  public String getVersionName() {
    String versionName = "";
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      versionName = pInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Unable to find package '" + getPackageName() + "'.", e);
    }
    return versionName;
  }
  ///
  private static final ExecutorService DB_EXECUTOR =
      Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            public Thread newThread(Runnable r) {
              return new Thread(r, "tv-app-db");
            }
          });

  private TvInputManagerHelper mTvInputManagerHelper;
  private TIFChannelManager mTIFChannelManager;
  private TIFProgramManager mTIFProgramManager;
  private InputSourceManager mInputSourceManager;
  private CommonIntegration mCommonIntegration;
  private ParentalControlSettings mParentalControlSettings;

  @Override
  public TvInputManagerHelper getTvInputManagerHelper() {
    if (mTvInputManagerHelper == null) {
      mTvInputManagerHelper = new TvInputManagerHelper(appContext);
      mTvInputManagerHelper.start();
    }

    return mTvInputManagerHelper;
  }

  @Override
  public TIFChannelManager getChannelDataManager() {
    if (mTIFChannelManager == null) {
      mTIFChannelManager = TIFChannelManager.getInstance(appContext);
      mTIFChannelManager.start();
      FavChannelManager.getInstance(appContext);
    }

    return mTIFChannelManager;
  }

  @Override
  public TIFProgramManager getProgramDataManager() {
    if (mTIFProgramManager == null) {
      mTIFProgramManager = TIFProgramManager.getInstance(appContext);
    }

    return mTIFProgramManager;
  }

  @Override
  public InputSourceManager getInputSourceManager() {
    if (mInputSourceManager == null) {
      mInputSourceManager = InputSourceManager.getInstance();
      ComponentStatusListener.getInstance();
    }

    return mInputSourceManager;
  }

  @Override
  public CommonIntegration getCommonIntegration() {
    if (mCommonIntegration == null) {
      mCommonIntegration = CommonIntegration.getInstance();
      mCommonIntegration.setContext(this);
    }

    return mCommonIntegration;
  }

  @Override
  public ParentalControlSettings getParentalControlSettings() {
    if (mParentalControlSettings == null) {
      mParentalControlSettings = new ParentalControlSettings(appContext);
    }

    return mParentalControlSettings;
  }

  @Override
  public ContentRatingsManager getContentRatingsManager() {
    return getTvInputManagerHelper().getContentRatingsManager();
  }

  @Override
  public Executor getDbExecutor() {
    return DB_EXECUTOR;
  }

  @Override
  public boolean getTurnkeyUiMainActiviteActive() {
    return mTurnkeyUiMainActivityActive;
  }

  @Override
  public void setTurnkeyUiMainActiviteActive(Boolean isActive) {
    mTurnkeyUiMainActivityActive = isActive;
  }

  public static TvSingletons getSingletons() {
    return (TvSingletons) appContext;
  }
    public  static void add(Dialog act) {
        synchronized(mainDialogs) {
            mainDialogs.push(act);
        }
    }

    public static void remove(Dialog act) {
        synchronized(mainDialogs) {
            do {
              if(!mainDialogs.empty()){
                Dialog pre = mainDialogs.pop();
                if(pre != null && pre.equals(act)) {
                  break;
                }
              }
            } while (!mainDialogs.empty());
        }
    }

    public static Dialog getActiveDialog() {
        synchronized(mainDialogs) {
            if(!mainDialogs.empty()) {
              return mainDialogs.peek();
            }
        }
        return null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        int clsCount = increaseClsCount(activity.getClass());
        if (clsCount > 1){
            Log.w(TAG, "onActivityCreated clsCount=" +clsCount + " activity:" + activity);
        };
        Log.i(TAG, "onActivityCreated activityCount:" + activityCount + " activity:" + activity);
    }


    @Override
    public void onActivityStarted(Activity activity) {
        Log.i(TAG, "onActivityStarted activityCount:" + activityCount+",activity:"+activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        activityCount++;
        Log.i(TAG, "onActivityResumed activityCount:" + activityCount+ " activity:" + activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        activityCount--;
        Log.i(TAG, "onActivityPaused activityCount:" + activityCount+ " activity:" + activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i(TAG, "onActivityStopped activityCount:" + activityCount+ " activity:" + activity);
    }
    /**
     * app show
     */
    public static boolean isForeground() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "isForeground activityCount:" + activityCount);
      return activityCount > 0;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i(TAG, "onActivitySaveInstanceState activityCount:" + activityCount+ " activity:" + activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        int clsCount = decreaseClsCount(activity.getClass());
        if (clsCount > 0){
            Log.w(TAG, "onActivityDestroyed clsCount=" +clsCount + " activity:" + activity);
        };
        Log.i(TAG, "onActivityDestroyed activityCount:" + activityCount+ " activity:" + activity);
        RxBus.instance.send(new ActivityDestroyEvent(activity.getClass()));
    }
    private int getClsCount(Class<?> cls){
        if (!clsCounter.containsKey(cls)){
            return 0;
        }
        return clsCounter.get(cls);
    }
    private int increaseClsCount(Class<?> cls){
        int count = getClsCount(cls);
        ++ count;
        clsCounter.put(cls, count);
        return count;
    }
    private int decreaseClsCount(Class<?> cls){
        int count = getClsCount(cls);
        -- count;
        if (count < 0){
            Log.e(TAG, "decreaseClsCount can't find this cls=" + cls);
        }else if (count == 0){
            clsCounter.remove(cls);
        }else {
            clsCounter.put(cls, count);
        }
        return count;
    }
}
