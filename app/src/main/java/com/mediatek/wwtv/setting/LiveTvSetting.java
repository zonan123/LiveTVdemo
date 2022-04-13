package com.mediatek.wwtv.setting;

import java.text.DecimalFormat;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import com.android.tv.onboarding.SetupSourceActivity;
import com.android.tv.ui.sidepanel.ClosedCaptionFragment;
import com.android.tv.ui.sidepanel.MultiAudioFragment;
import com.android.tv.ui.sidepanel.RecordTShiftFragment;
import com.android.tv.ui.sidepanel.GingaFragment;
import com.android.tv.util.TvInputManagerHelper;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.preferences.SettingsPreferenceScreen;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
//import java.lang.Exception;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.fragments.BaseContentFragment;
import com.mediatek.wwtv.setting.fragments.MainFragment;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;

import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.util.EventHelper;
import com.mediatek.tv.ini.IniDocument;
import java.io.File;
//import java.lang.Integer;
//import java.lang.String;



public class LiveTvSetting extends TvSettingsActivity {
    private static final String TAG = "LiveTvSetting";

    private static boolean isStartTV;
    private static boolean mFactoryMode = false;
    public static boolean isRunning = false;
    private final static EventHelper mEventHelper = new EventHelper();

    private TVContent mTV;
    private final MtkTvAppTVBase appTV = new MtkTvAppTVBase();
    private static LiveTvSetting mLiveTvSetting = null;
    private CloseBroadcast broadcast = null ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate.");
        setContentView(R.layout.menu_main_layout);

        super.onCreate(savedInstanceState);
        getScreenWH();
        receiveIntent(getIntent());
        mLiveTvSetting = this;
        mTV = TVContent.getInstance(this);
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // fix CR DTV00578583
                if(TurnkeyUiMainActivity.getInstance() != null) {
                    Intent intent = new Intent("com.mediatek.tv.callcc");
                    intent.putExtra("ccvisible", false);
                    sendBroadcast(intent);
                }
            }
        });
        broadcast = new CloseBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("finish_live_tv_settings");
        registerReceiver(broadcast, filter);
        mFactoryMode = false;
        getFacCusInfo();
        TvInputManagerHelper inputManager = ((DestroyApp)getApplicationContext()).getTvInputManagerHelper();
        inputManager.getContentRatingsManager().update();
    }

    private boolean isFromCamMenu(Intent intent) {
        if(intent != null){
            String ext = intent.getStringExtra(EventHelper.MTK_EVENT_EXTRA_SUB_TYPE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromCamMenu: ext:  " + ext);
            if(ext != null && !ext.equals("")){
                if(ext.equals(EventHelper.MTK_EVENT_EXTRA_TYPE_CAMSCAN_SRC)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromCamMenu: return true");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFromsubtitle(Intent intent) {
        if(intent != null){
            String ext = intent.getStringExtra(EventHelper.MTK_EVENT_EXTRA_SUB_TYPE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromsubtitle: ext:  " + ext);
            if(ext != null && !ext.equals("")){
                if(ext.equals(EventHelper.MTK_EVENT_EXTRA_TYPE_SUBTITLE_SRC)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromsubtitle: return true");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * isFromTimeShiftMode
     * @param intent
     * @return
     */
    private boolean isFromTimeShiftMode(Intent intent){
        if(intent != null){
            String ext = intent.getStringExtra(EventHelper.MTK_EVENT_EXTRA_SUB_TYPE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromTimeShiftMode: ext:  " + ext);
            if(!TextUtils.isEmpty(ext)){
                if(ext.equals(EventHelper.MTK_EVENT_EXTRA_RECORD_TSHIFT_SRC)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromTimeShiftMode: return true");
                    return true;
                }
            }
        }
        return false;
    }


    public static LiveTvSetting getInstance(){
        return mLiveTvSetting;
    }

    @Override
    protected void onStart() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart.");
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "now onResume updatedSysStatus to RESUME");
                if(!isFromCamMenu(getIntent()) && !isFromsubtitle(getIntent()) && !isFromTimeShiftMode(getIntent())){
                    appTV.updatedSysStatus(MtkTvAppTVBase.SYS_MENU_RESUME);
                }
            }
        });
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      receiveIntent(intent);
    }

    @Override
    protected void onResume() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume.");
        super.onResume();
        isRunning = true;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromLiveTv: " + isBootFromLiveTV());
        if (isBootFromLiveTV()) {
           facTimes = 0;
           mFactoryMode = false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mFactoryMode" + mFactoryMode);
            setResult(com.mediatek.wwtv.tvcenter.nav.
                view.common.NavBasic.NAV_RESULT_CODE_MENU);
        }else{
        	//CR DTV02079014
        	 DestroyApp.getSingletons().getChannelDataManager().handleUpdateChannels();
        	  try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    }

    @Override
    protected void onPause() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPause.");
        super.onPause();
    }

    @Override
    protected void onStop() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop.");
        isRunning = false;
        PreferenceUtil.getInstance(this).isFromSubtitleTrackNoSignal = false;
        PreferenceUtil.isFromSubtitleTrackclick = false;
        if (ScheduleListDialog.getDialog() != null) {
            ScheduleListDialog.getDialog().dismiss();
        }
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if(!isFromCamMenu(getIntent()) && !isFromsubtitle(getIntent()) && !isFromTimeShiftMode(getIntent())){
                    appTV.updatedSysStatus(MtkTvAppTVBase.SYS_MENU_PAUSE);
                }
            }
        });
        super.onStop();
    }

    @Override
    public void onDestroy() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestroy.");
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
                if(TurnkeyUiMainActivity.getInstance()!=null) {
                    TurnkeyUiMainActivity.getInstance().resetLayout();
                    Intent intent = new Intent("com.mediatek.tv.callcc");
                    intent.putExtra("ccvisible", true);
                    sendBroadcast(intent);
                }
            }
        });
        if (broadcast != null) {
            unregisterReceiver(broadcast);
//            broadcast = null;
         }
        mLiveTvSetting = null;
        RxBus.instance.send(new ActivityDestroyEvent(getClass()));
        super.onDestroy();
        SundryImplement.setInstanceNavSundryImplementNull();
        SettingsPreferenceScreen.resetInstance();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown, keyCode: " + keyCode);

        if(handleKeyEvent(keyCode, event)) {
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected Fragment createSettingsFragment() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "createSettingsFragment.");
        SettingsFragment fragment = SettingsFragment.newInstance();
        fragment.mActionId = mActionId;
        //mActionId = null;
        return fragment;
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static String mActionId;
        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            //if(mFactoryMode) {
                //mFragment = MainFragment.newInstance(true);
                //never enter again before setting quit.
               // mFactoryMode = false;
            //}
            //else {
            MainFragment mFragment = MainFragment.newInstance(false);
            //}
            if(isForMultiAudioBootFromTVMenuOption()) {
                startPreferenceFragment(new MultiAudioFragment());
            } else if(isFor3rdCaptionsTVMenuOptionOrSaRegion()){
                if(CommonIntegration.getInstance().is3rdTVSource()){
                    startPreferenceFragment(new ClosedCaptionFragment());
                }else if(CommonIntegration.getInstance().isSARegion()){
                    startPreferenceFragment(mFragment);
                }
            } else if(isForTShiftBootFromTVMenuOption()){
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start RecordTShiftFragment");
                startPreferenceFragment(new RecordTShiftFragment());
            }else if(isForGingaOption()){
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start GingaFragment");
            	startPreferenceFragment(new GingaFragment());
			} else if(isForSubtitleGroup()){
                BaseContentFragment base = BaseContentFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putCharSequence(
                        PreferenceUtil.PARENT_PREFERENCE_ID, MenuConfigManager.SUBTITLE_GROUP);
                base.setArguments(bundle);
                startPreferenceFragment(base);
            }else if(!startPreferenceScanFragment()){
                startPreferenceFragment(mFragment);
            }
        }

        private boolean startPreferenceScanFragment(){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startPreferenceScanFragment mActionId "+mActionId);
            if(mActionId == null){
                return false;
            }
            if(!MenuConfigManager.CHANNEL_CHANNEL_SOURCES.equals(mActionId)){
                BaseContentFragment base = BaseContentFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putCharSequence(
                        PreferenceUtil.PARENT_PREFERENCE_ID, mActionId);
                base.setArguments(bundle);
                startPreferenceFragment(base);
            }else {
                getActivity().startActivity(new Intent(getActivity(), SetupSourceActivity.class));
            }
            return true;

        }

        public static boolean isForSubtitleGroup() {
            return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_SUBTITLE_SRC);
        }
    }

    /**
     * receiveIntent
     *
     */
    private void receiveIntent(Intent intent) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "receiveIntent: action:" + intent);
        mEventHelper.updateIntent(intent);
    }

    /**
     * handleKeyEvent
     *
     */
    private boolean handleKeyEvent(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleKeyEvent: keyCode=" + keyCode+",event="+event.getAction());

        switch(keyCode) {
        case KeyMap.KEYCODE_BACK:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleKeyEvent: getSideFragmentManager().getCount() >" + getSideFragmentManager().getCount());
            if(getSideFragmentManager().getCount() > 0){
                getSideFragmentManager().popStackNumber();
            }

            PreferenceUtil.isFromSubtitleTrackclick = false;
//            if(mFragment != null && mFragment.getActive()) {
//            }
            break;
        default:
            break;
        }

        if(isBootFromLiveTV()) {
            if(handlerFacKey(keyCode)){
                facTimes ++;
                if(facTimes == getSerialKeys().split(",").length){
                    mFactoryMode=true;
                    updateFragment(getPackageNames(),getMainActivity());
                    facTimes = -1;
                }
                return true;
            }else{
                facTimes = -1;
                return false;
            }
        }
        return false;
    }

    /**
     * isBootFromLiveTV
     *
     */
    public static boolean isBootFromLiveTV() {
        return mEventHelper.isEvent(EventHelper.INTENT_SRC_LIVE_TV);
    }

    public static boolean isForChannelBootFromTVSettingPlus() {
        return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_CHANNEL_SRC);
    }

    public static boolean isForMultiAudioBootFromTVMenuOption() {
        return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_MULTI_AUDIO_SRC);
    }

    public static boolean isFor3rdCaptionsTVMenuOptionOrSaRegion() {
        return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_3RD_CAPTIONS_SRC);
    }

    public static boolean isForTShiftBootFromTVMenuOption() {
        return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_TSHIFT_SRC);
    }

    public static boolean isForGingaOption() {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isForGingaOption");
        return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_GINGA_SRC);
    }

	public static boolean isForCAMScanFromTVMenuOption() {
        return mEventHelper.isEvent(EventHelper.INTENT_SUB_TYPE_CAMSCAN_SRC);
    }

    private String serialKeys;
    private String packageName;
    private String mainActivity;
    /**

    */
    private void getFacCusInfo (){
    try{
        String path = getFile();
        IniDocument idc = new IniDocument(path);
        serialKeys = idc.get("SerialKeys").toString().trim();
        packageName = idc.get("PackageName").toString().trim();
        mainActivity = idc.get("MainActivity").toString().trim();

        serialKeys = keyTool(serialKeys);
        packageName = keyTool(packageName);
        mainActivity = keyTool(mainActivity);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"idc.get(SerialKeys) = " + serialKeys);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"idc.get(PackageName) = " + packageName);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"idc.get(MainActivity) = " + mainActivity);
        }catch (Exception e){
            serialKeys = "7,7,7,7";
        }

        //String mSectionName
        //List<KeyValue> mKeys
    }

    private String getSerialKeys (){
        return serialKeys;
    }
    private String getPackageNames (){
        return packageName;
    }
    private String getMainActivity (){
        return mainActivity;
    }
    int facTimes = 0;
    /**  */
    private boolean handlerFacKey(int keyCode){
        if(facTimes < 0){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"handlerFacKey false");
            return false;
        }
        String [] facKeys = getSerialKeys().split(",");
        int key = Integer.parseInt(facKeys[facTimes].trim());
        return key == keyCode? true:false;
    }

    private String keyTool(String key){
        if(key != null && !key.isEmpty()){
            if(key.startsWith("[")){
              key = key.replace("[","");
            }
            if(key.endsWith("]")){
              key = key.replace("]","");
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " = " + key );
        return key;
    }

    private String getFile(){
        String path                    = "/vendor/etc/";
        String vendorPath             = "/vendor/tvconfig/apollo/";
        String configFile             = "ManualEnterFactory.ini";
        String[] paths;
        String cusdata = DataSeparaterUtil.getInstance().getIniFilePath("apk", "m_pManualEnterFactoryName");
        if(!TextUtils.isEmpty(cusdata)) {
            paths = new String[]{cusdata, vendorPath + configFile, path + configFile};
        } else {
            paths = new String[]{vendorPath + configFile, path + configFile};
        }

        for(String pat : paths) {
            File f = new File(pat);
            if (f != null && f.exists()) {
                return pat;
            }else {
                continue;
            }
        }
        return null;

    }

    /**
     * tryToStartTV
     *
     */
    public static void tryToStartTV(Context context) {
        if (isStartTV) {
            Intent mIntent = new Intent("com.mediatek.select.source");
            mIntent.putExtra("com.mediatek.select.sourcename", "");
            context.sendBroadcast(mIntent);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "start tv");
            isStartTV = false;
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "not start tv");
        }
    }

    private void getScreenWH() {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        SettingsUtil.SCREEN_WIDTH = dm.widthPixels;
        SettingsUtil.SCREEN_HEIGHT = dm.heightPixels;
        ScreenConstant.SCREEN_WIDTH = dm.widthPixels;
        ScreenConstant.SCREEN_HEIGHT = dm.heightPixels;
      }
    class CloseBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onReceive android.intent.action.MASTER_CLEAR");
          	if("finish_live_tv_settings".equals(arg1.getAction())){
          		finish();
          	}

        }
      }

    /*
     * for dvbs My longitude & My latitude
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        if(requestCode == 0x28){//longitude
            String value = data.getStringExtra("value");
            double tempValue = Double.parseDouble(value);
            DecimalFormat format = new DecimalFormat("#0.0");
            double doubleValue = Double.parseDouble(format.format(tempValue));
            int acfgValue = (int) (doubleValue * 10);
            MenuConfigManager.getInstance(this).setValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE, acfgValue);
        }else  if(requestCode == 0x29){ //latitude
            String value = data.getStringExtra("value");
            double tempValue = Double.parseDouble(value);
            DecimalFormat format = new DecimalFormat("#0.0");
            double doubleValue = Double.parseDouble(format.format(tempValue));
            int acfgValue = (int) (doubleValue * 10);
            MenuConfigManager.getInstance(this).setValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE, acfgValue);
        }else if(requestCode == 0x30){ //user define band frequency
            String value = data.getStringExtra("value");
            int freq = Integer.parseInt(value);
            freq = Math.min(Math.max(freq, 950), 2150);
            ScanContent.getInstance(this).saveDVBSBandFreq(this, freq);
        }
    }
}

