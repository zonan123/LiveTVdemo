package com.mediatek.wwtv.tvcenter.nav.view;

import java.lang.ref.WeakReference;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.content.Context;
import android.database.ContentObserver;
import android.view.View;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
//import android.view.WindowManager;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;

import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.wwtv.tvcenter.util.ScreenStatusManager;

public class VgaPowerManager extends NavBasicMisc implements
        ComponentStatusListener.ICStatusListener {
    private static final String TAG = "VgaPowerManager";

    private static final int GO_VGA_POWER_OFF_DIALOG = 1;
    private static final int GO_VGA_POWER_OFF = 2;
    private static final int CHECK_IF_POWER_OFF = 3;
    private static final int GO_VGA_START_TIMER = 4;
    private static final String mSourceName = "VGA";
    public static final String KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF = "no_signal_auto_power_off";

    protected static final String FIRST_POWER_ON = "first_power_on";
    private final Uri BASE_URI = Settings.Global.CONTENT_URI;
    private boolean isSignalLoss = false;
    private boolean isLocked = true;
    private InternalHandler handler;
    private ConfirmDialog mConfirmDialog;
    private ContentResolver mContentResolver;
    private final IDreamManager mDreamManager = IDreamManager.Stub.asInterface(
            ServiceManager.getService(DreamService.DREAM_SERVICE));
    private static CountDownTimer countDownTimer;
    private TextView timer;

    private ContentObserver autoPowerOffObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onChange"+ " isSignalLoss:"+isSignalLoss);
            sendMessage(isSignalLoss);
            updateScreen();
        }
    };

    public VgaPowerManager(Context context) {
        super(context);
        SaveValue.writeWorldStringValue(context, "vendor.mtk.svctx.stoped", "1", false);
        this.componentID = NAV_COMP_ID_POWER_OFF;
        handler = new InternalHandler(this, context.getMainLooper());
        mConfirmDialog = new ConfirmDialog(mContext, R.layout.nav_simple_dialog,
                mContext.getString(R.string.vga_no_sinal_info) +
                mContext.getString(R.string.vga_no_sinal_info_part2));

        // Add component status Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_HIDE, this);
        // Add component status Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_SHOW, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_POWER_ON, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_POWER_OFF, this);
        ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_VGA_NO_SIGNAL, this);
        mContentResolver = context.getContentResolver();
        mContentResolver.registerContentObserver(BASE_URI.buildUpon().appendPath(KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF).build(),
                true,autoPowerOffObserver);
    }

    @Override
    public boolean deinitView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "deinitView");
        mContentResolver.unregisterContentObserver(autoPowerOffObserver);
        handler.removeCallbacksAndMessages(null);
        return super.deinitView();
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isKeyHandler isSignalLoss: "+isSignalLoss);
        if (isSignalLoss) {// no signal
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);

            sendMessage(true);
        }

        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyHandler keyCode: "+keyCode);
        if (handler != null) {
            handler.removeMessages(GO_VGA_POWER_OFF);
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);
            if (mConfirmDialog != null) {
                mConfirmDialog.dismiss();
            }
            setVisibility(View.INVISIBLE);
        }

        return false;
    }
    private void updateScreen(){
         if(isSignalLoss) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(mContext == null ||
                            ((TurnkeyUiMainActivity)mContext) == null ||
                            ((TurnkeyUiMainActivity)mContext).getWindow() == null) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"can't get turnkey window.");
                        ScreenStatusManager.getInstance().setScreenOn(((TurnkeyUiMainActivity)mContext).getWindow(),ScreenStatusManager.SCREEN_ON_BROADCAST);
                        return;
                    }
                    if(Settings.Global.getInt(mContentResolver,
                            KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF, 0) == 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"clearFlags FLAG_KEEP_SCREEN_ON");
                        //((TurnkeyUiMainActivity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        ScreenStatusManager.getInstance().setScreenOff(((TurnkeyUiMainActivity)mContext).getWindow(),ScreenStatusManager.SCREEN_ON_BROADCAST);
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"addFlags FLAG_KEEP_SCREEN_ON");
                        //((TurnkeyUiMainActivity)mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        ScreenStatusManager.getInstance().setScreenOn(((TurnkeyUiMainActivity)mContext).getWindow(),ScreenStatusManager.SCREEN_ON_BROADCAST);
                    }
                }
            });
        }else{
            ScreenStatusManager.getInstance().setScreenOn(((TurnkeyUiMainActivity)mContext).getWindow(),ScreenStatusManager.SCREEN_ON_BROADCAST);
        }
    }

    // {{setVisibility(int visibility)
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            if (null != mConfirmDialog && (!mConfirmDialog.isShowing())) {
                mConfirmDialog.show();
            }
        } else {
            if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
                mConfirmDialog.dismiss();
            }
        }
    }

    // }}
    public void hideDialog(){
        if (handler != null) {
            handler.removeMessages(GO_VGA_POWER_OFF);
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);
            if (mConfirmDialog != null) {
                mConfirmDialog.dismiss();
            }
            setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void updateComponentStatus(int statusID, int value) {
        if (MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_DPMS) == 0 &&
                TvSingletons.getSingletons().getCommonIntegration().isCurrentSourceVGA()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus, dpms disabled~");
            return;
        }

        // Block Check
        if (InputSourceManager.getInstance().isBlock(mSourceName) && isLocked) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus, VGA blocked~");
            return;
        }

        if (statusID == ComponentStatusListener.NAV_COMPONENT_SHOW) {
            if (NAV_COMP_ID_TWINKLE_DIALOG == value) {
                if (isSignalLoss) {// no signal
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus, statusID=" + statusID);
                    sendMessage(true);
                }
            }
        } else if (statusID == ComponentStatusListener.NAV_COMPONENT_HIDE) {
            if (isSignalLoss && (!ComponentsManager.getInstance().isComponentsShow()) && !isDreaming()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus, statusID=" + statusID);
                sendMessage(true);
            }
        } else if (statusID == ComponentStatusListener.NAV_POWER_ON) {
            TVAsyncExecutor.getInstance().execute(new Runnable() {
                public void run() {
                    boolean isFirstPowerOn = SaveValue.getInstance(mContext).readBooleanValue(FIRST_POWER_ON);
                    if(isFirstPowerOn) {
                        int defaultValue = 1;
                        int value = MtkTvConfig.getInstance().getConfigValue(
                                MtkTvConfigType.CFG_VIDEO_VID_ENABLE_SGNL_WAKEUP);

                        if((value & 0x1) != 0&&isOnDPMS()) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"reset CFG_VIDEO_VID_ENABLE_SGNL_WAKEUP flag");
                            MtkTvConfig.getInstance().setConfigValue(
                                    MtkTvConfigType.CFG_VIDEO_VID_ENABLE_SGNL_WAKEUP,
                                    value & (~defaultValue));
                              //for fix DTV02000724, you can check email
//                            value = MtkTvUtil.getInstance().getPclWakeupSetup();
//                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"reset wakeup flag");
//                            MtkTvUtil.getInstance().setPclWakeupSetup(value & (~defaultValue));
                        }
                    } else {
                        SaveValue.getInstance(mContext).saveBooleanValue(FIRST_POWER_ON,true);
                    }
                }
            });
        } else if(statusID == ComponentStatusListener.NAV_POWER_OFF) {
            handler.removeMessages(GO_VGA_POWER_OFF);
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);
        }
        else if(statusID==ComponentStatusListener.NAV_VGA_NO_SIGNAL){
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "VGAPower handle message NAV_VGA_NO_SIGNAL!");
        	isSignalLoss = true;
            sendMessage(isSignalLoss);
        }
    }

    // For Svctx message
    public void handlerMessage(int type) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "VgaPowerManager_handlerMessage, type=" + type);

        if(type == 13) {
            android.util.Log.d(TAG, "svctx notify stoped, set property 1");
            SaveValue.writeWorldStringValue(mContext, "vendor.mtk.svctx.stoped", "1", false);
        } else if(type == 4 || type == 5) {
            SaveValue.writeWorldStringValue(mContext, "vendor.mtk.svctx.stoped", "0", false);
        }

        if (type == 5 || type == 10) {// SVCTX_NTFY_CODE_SIGNAL_LOSS
            isSignalLoss = true;
            sendMessage(isSignalLoss);
            updateScreen();
        } else if (type == 12) {// SVCTX_NTFY_CODE_SERVICE_UNBLOCKED
            if (InputSourceManager.getInstance().isBlock(mSourceName)) {
                isLocked = false;
            }
        } else if (type == 9 || type == 11) {
            isLocked = true;
        } else if (type == 0 || type == 20 || type == 21 || type == 37 || type == 38) {
            isSignalLoss = false;
            sendMessage(isSignalLoss);
            updateScreen();
        }// else
       
    }

    // CR DTV00714197
    public void handleSourceKey() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "+++++ handleSourceKey +++++");
        if (handler != null) {
            handler.removeMessages(GO_VGA_POWER_OFF);
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);

            if (mConfirmDialog != null && mConfirmDialog.isVisible()) {
                mConfirmDialog.dismiss();
            }
            setVisibility(View.INVISIBLE);
        }
    }

    private void sendMessage(boolean bStart) {
        if (bStart) {
        	if(!enablePowerOff()){
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"sendMessage disable power off!");
            	return;
            }
            // Input source check
            // if(MtkTvInputSource.getInstance().getCurrentInputSourceName().equalsIgnoreCase(mSourceName)){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerMessage, GO_VGA_POWER_OFF_DIALOG");
            int value = Settings.Global.getInt(mContentResolver,
                    KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF, 0);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEY_POWER_NO_SIGNAL_AUTO_POWER_OFF:" + value);
            int delaytime = 0;
            switch (value) {
            case 1:
                delaytime = 300;
                break;
            case 2:
                delaytime = 600;
                break;
            case 3:
                delaytime = 900;
                break;
            case 4:
                delaytime = 1800;
                break;
            case 5:
                delaytime = 3600;
                break;
            default:
                break;
            }
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);
            if (value != 0) {
                long lDelayTime=delaytime * 1000- NAV_TIMEOUT_60;//Power off after 1 Min.
                handler.sendEmptyMessageDelayed(GO_VGA_POWER_OFF_DIALOG, lDelayTime);
            }

            // }
        } else {
            if (handler != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerMessage, clean message");

                handler.removeMessages(GO_VGA_POWER_OFF);
                handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);
            }

            if (null != mConfirmDialog && mConfirmDialog.isShowing()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendMessage false == setVisibility(View.INVISIBLE)");
                setVisibility(View.INVISIBLE);
            }
        }
    }

    private class ConfirmDialog extends NavBasicDialog {
        private TextView mInfo;
        String mInfoTip = "";

        public ConfirmDialog(Context context, int theme, String info) {
            super(context, R.style.dialog);
            setContentView(theme);

            mInfoTip = info;
        }

        @Override
        public boolean initView() {
            super.initView();

//            ((ImageView) findViewById(R.id.ib_image_icon)).setVisibility(View.GONE);
//            mInfo = (TextView) findViewById(R.id.ib_text);
            mInfo = (TextView) findViewById(R.id.simple_dialog_content);
            mInfo.setText(mInfoTip);

            LinearLayout containerLinearlLyout  = (LinearLayout)findViewById(R.id.simple_dialog_layout);

            timer = new TextView(mContext);
            timer.setTextSize(mInfo.getTextSize());
            timer.setTextColor(mInfo.getTextColors());
            timer.setText(R.string.vga_power_default_time);
            timer.setGravity(Gravity.CENTER);
            timer.setPadding(0, 0, 0, 20);
            containerLinearlLyout.addView(timer);

            // set location
//            Window window = getWindow();
//            WindowManager.LayoutParams lp = window.getAttributes();
//
//            lp.width = 450 * ScreenConstant.SCREEN_WIDTH / 1280;
//            lp.height = 180 * ScreenConstant.SCREEN_HEIGHT / 720;
//            window.setAttributes(lp);

            return true;
        }

        public void show() {
            handler.sendEmptyMessageDelayed(GO_VGA_POWER_OFF, NAV_TIMEOUT_60);
            handler.sendEmptyMessage(GO_VGA_START_TIMER);
            super.show();
        }

        @Override
        public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
            handler.removeMessages(GO_VGA_POWER_OFF);
            handler.removeMessages(GO_VGA_POWER_OFF_DIALOG);
            stopTimer();
            VgaPowerManager.this.setVisibility(View.INVISIBLE);

            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_TWINKLE_DIALOG);
                return true;
            default:
                break;
            }

            /* Back Key to TurnkeyUiMainActivity */
            if (TurnkeyUiMainActivity.getInstance() != null) {
                return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
            }

            return false;
        }
    }

    private void checkPowerOff(){
    	boolean isTurnkey = false;
        try {
            Context context = mContext;
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            isTurnkey = am.getRunningTasks(1).get(0).topActivity.getShortClassName()
                    .contains("TurnkeyUiMainActivity");
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	boolean isTurnkeyActive = TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMP_ID_POWER_OFF_current activity is TurnkeyUiMainActivity ? " + isTurnkey+" isTurnkeyActive:"+isTurnkeyActive);
        if (isTurnkey || isTurnkeyActive) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"show dialog");
            ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_POWER_OFF);
        }
    }

    private class InternalHandler extends Handler {
        private final WeakReference<VgaPowerManager> mPowerManager;

        public InternalHandler(VgaPowerManager manager, Looper looper) {
            super(looper);
            mPowerManager = new WeakReference<VgaPowerManager>(manager);
        }

        public void handleMessage(Message msg) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~");
            if (mPowerManager.get() == null) {
                return;
            }

            if (CommonIntegration.getInstance().is3rdTVSource()) {
                return;
            }

            if (GO_VGA_POWER_OFF_DIALOG == msg.what) {
                if(!mPowerManager.get().enablePowerOff()){
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"handleMessage disable power off!");
                	return;
                }
//                String inputsource = MtkTvInputSource.getInstance().getCurrentInputSourceName();
                try {
                    if(mPowerManager.get().isDreaming()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"NAV_COMP_ID_POWER_OFF_awaken");
                        mPowerManager.get().mDreamManager.awaken();
                        mPowerManager.get().handler.removeMessages(CHECK_IF_POWER_OFF);
                        mPowerManager.get().handler.sendEmptyMessageDelayed(CHECK_IF_POWER_OFF, 2000);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPowerManager.get().handler.sendEmptyMessage(CHECK_IF_POWER_OFF);

            } else if (GO_VGA_POWER_OFF == msg.what) {
                // power off
                ComponentsManager.getInstance().hideAllComponents();
                if (null != mConfirmDialog && mConfirmDialog.isShowing()) {
                    mConfirmDialog.dismiss();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in GO_VGA_POWER_OFF msg to dismiss dialog");
                }
                //
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] GO_VGA_POWER_OFF");

                TVAsyncExecutor.getInstance().execute(new Runnable() {
                    public void run() {
                    	if(isOnDPMS()){
                    		int value = MtkTvUtil.getInstance().getPclWakeupSetup();
                            MtkTvUtil.getInstance().setPclWakeupSetup(value | 1);

                            value = MtkTvConfig.getInstance().getConfigValue(
                                    MtkTvConfigType.CFG_VIDEO_VID_ENABLE_SGNL_WAKEUP);
                            MtkTvConfig.getInstance().setConfigValue(
                                    MtkTvConfigType.CFG_VIDEO_VID_ENABLE_SGNL_WAKEUP, value | 1);
                    	}
                    	if(mPowerManager.get().mContext != null){
                    	    ((PowerManager) (mPowerManager.get().mContext
                                    .getSystemService(Context.POWER_SERVICE))).goToSleep(SystemClock
                                    .uptimeMillis());
                    	}

                    }
                });
            }else if(CHECK_IF_POWER_OFF == msg.what){
            	mPowerManager.get().checkPowerOff();
            } else if(GO_VGA_START_TIMER == msg.what){
              stopTimer();//clear timer before start
              countDownTimer = new CountDownTimer(1*60*1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                  // TODO Auto-generated method stub
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] millisUntilFinished: "+millisUntilFinished);
                  int min = (int)(millisUntilFinished/1000/60);
                  int sec = (int)(millisUntilFinished/1000%60);
                  if(sec<10){
                    timer.setText(mContext.getResources().getString(R.string.vga_power_time,min,sec));
                  } else {
                    timer.setText(mContext.getResources().getString(R.string.vga_power_time_two,min,sec));
                  }
                }

                @Override
                public void onFinish() {
                  stopTimer();
                }
              };

              countDownTimer.start();
            }
        }

    }

    private static boolean isOnDPMS(){
    	boolean isOnDPMS=MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_DPMS) == 1;
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isOnDPMS="+isOnDPMS);
    	return isOnDPMS;
    }

    private boolean isDreaming() {
        boolean result = false;
        try {
            result = mDreamManager != null && mDreamManager.isDreaming();
        } catch (Exception e) {
            e.printStackTrace();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isDreaming:"+result);
        return result;
    }


    private boolean enablePowerOff(){
    	// DPMS check
        if (MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_DPMS) == 0 &&
                TvSingletons.getSingletons().getCommonIntegration().isCurrentSourceVGA()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff, dpms disabled~");
            return false;
        }
        // Block Check
        if (InputSourceManager.getInstance().isBlock(mSourceName) && isLocked) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff, VGA blocked~");
            return false;
        }

        // PIP/POP check
        if (CommonIntegration.getInstance().isPipOrPopState()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff, in pip/pop state~");
            return false;
        }
        // Channel Scanning
        if(MtkTvScan.getInstance().isScanning()){
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff isScanning~");
            return false;
        }
        // ChannelList count==0,please scan channel
        if (CommonIntegration.getInstance().isCurrentSourceTv()
                    && (CommonIntegration.getInstance().getChannelAllNumByAPI() <= 0)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff, please scan channel!");
            return false;
        }

        if (ComponentsManager.getActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_MHEG5
                || ComponentsManager.getActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_HBBTV
                || ComponentsManager.getActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_GINGA) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff native component running~ ");
              return false;
         }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enablePowerOff, enable power off!");
    	return true;
    }

    private synchronized static void stopTimer() {
      if(countDownTimer != null) {
          countDownTimer.cancel();
          countDownTimer = null;
      }
  }

}
