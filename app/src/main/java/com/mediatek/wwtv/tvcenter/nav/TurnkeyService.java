package com.mediatek.wwtv.tvcenter.nav;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.oad.NavOADActivity;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.SaveValue;


import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.mediatek.wwtv.tvcenter.R;
/**
 * service for turnkey shut down handler UI issue & state recover.
 * @author sin_yajuncheng
 *
 */
public class TurnkeyService extends Service {

    private static final String TAG = "TurnkeyService";
    private boolean isFromScanView=false;
    public static final String SCAN_VIEW_ACTION="com.mtk.messageFromScanView";
    public static final String SCAN_VIEW_LIFECIRLE="lifecircle";
    private Context mContext;
    private Intent mIntent;

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "dump");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onConfigurationChanged");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final NotificationManager noMan = getSystemService(NotificationManager.class);
        noMan.createNotificationChannel(new NotificationChannel(
                TAG,
                TAG,
                NotificationManager.IMPORTANCE_NONE));

        startForeground(100, new Notification.Builder(this, TAG).getNotification());

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "oncreate");

        Log.d(TAG,"Hi,this my first log test,Greeting from 2021-05-26.");
        initPowerBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondestroy");
        this.unregisterReceiver(mReciver);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onLowMemory");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRebind");
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStartCommand");
        final NotificationManager noMan = getSystemService(NotificationManager.class);
        noMan.createNotificationChannel(new NotificationChannel(
                TAG,
                TAG,
                NotificationManager.IMPORTANCE_NONE));
        startForeground(100, new Notification.Builder(this, TAG).getNotification());
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        Bundle bundle = intent.getBundleExtra("SCHEDULE");
        if (bundle != null ) {
            String prompt = bundle.getString("PROMPT");
//            String title = bundle.getString("TITLE");
//            int confirmType = bundle.getInt("CONFIRM_TYPE");
            long time = bundle.getLong("TIME");
//            DvrConfirmDialog mDialog = new DvrConfirmDialog(this, prompt,title,confirmType);
            SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
            if(simpleDialog!=null&&simpleDialog.isShowing()){
                simpleDialog.dismiss();
            }
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
            simpleDialog.setCancelText(R.string.pvr_confirm_no);
            simpleDialog.setOnConfirmClickListener(new RegistOnDvrDialog(),-1);
            simpleDialog.setOnConfirmResultClickListener(new RegistOnDvrDialog(),RegistOnDvrDialog.TYPE_SCHEDULE_RECORD);
            simpleDialog.setOnCancelClickListener(new RegistOnDvrDialog(),RegistOnDvrDialog.TYPE_SCHEDULE_RECORD);
            simpleDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//important
            simpleDialog.setContent(prompt);
            simpleDialog.setScheduleDismissTime(time);
            simpleDialog.show();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onTaskRemoved");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onTrimMemory");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onBind");
        return null;
    }

    private void initPowerBroadcast() {
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter1.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter1.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter1.addAction(SCAN_VIEW_ACTION);
        this.registerReceiver(mReciver, intentFilter1);
    }

    private void handlerBroadcastTurnkeyMessage(Context context, Intent intent) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "handlerBroadcastTurnkeyMessage context>>"+context);
        ComponentStatusListener instance = ComponentStatusListener.getInstance();
        if (null == instance) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("BootBroadcastReceiver", "instance is null!");
            // continue;
        } else if (instance != null) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                instance.updateStatus(
                        ComponentStatusListener.NAV_POWER_OFF, 99);
                
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if(DestroyApp.getSingletons()!=null && DestroyApp.getSingletons().getChannelDataManager() !=null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("BootBroadcastReceiver", "BGM scan update channel list ");
                    DestroyApp.getSingletons().getChannelDataManager().handleUpdateChannels();
                }
                instance.updateStatus(
                        ComponentStatusListener.NAV_POWER_ON, 99);

            }
//          if (instance.isInPictureInPictureMode()) {
        //      return;
        //  }

        }
    }

    private void handlerBroadcastOADMessage (Context context, Intent intent){
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "handlerBroadcastOADMessage context>>"+context);
        if (NavOADActivity.getInstance() != null) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())
                    || Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                // isRemindMeLater = false;
                NavOADActivity.getInstance().setRemindMeLater(false);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                // isRemindMeLater = true ;
                NavOADActivity.getInstance().setRemindMeLater(true);
            }
        }
    }
    private void handlerBroadcastDVRMessage (){
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
            if (StateDvr.getInstance() != null && StateDvr.getInstance().isRunning()) {
                StateDvr.getInstance().showSmallCtrlBar();
                StateDvr.getInstance().clearWindow(true);
            }
            // tifTimeShift is running case
        }
    }
    private void handlerBroadcastTimeshiftMessage(Intent intent) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "handlerBroadcastTimeshiftMessage intent.getAction>>"+intent.getAction());
        if (Intent.ACTION_SHUTDOWN.equals(mIntent.getAction())) {
            if (TifTimeShiftManager.getInstance() != null
                    && SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Stop timeshift when notify MASTER_CLEAR receiver.");
                TifTimeshiftView tifTimeshiftView = (TifTimeshiftView) ComponentsManager
                        .getInstance()
                        .getComponentById(NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
                tifTimeshiftView.setVisibility(View.GONE);
                tifTimeshiftView.stopTifTimeShift();
                TifTimeShiftManager.getInstance().stopAll();
            }
        }

    }

    private BroadcastReceiver mReciver = new BroadcastReceiver() {
    	//fix the anr that handlerBroadcastOADMessage may cause
        @Override
        public void onReceive(Context context, Intent intent) {
        	mContext = context;
        	mIntent = intent;
        	new Handler((Looper.getMainLooper())).post(runnable);
        }
    };
    
   private final Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onReceive||intentAction =" + mIntent.getAction());
            if(SCAN_VIEW_ACTION.equals(mIntent.getAction())){
                isFromScanView = mIntent.getBooleanExtra(SCAN_VIEW_LIFECIRLE, false);
            } else {
                handlerBroadcastOADMessage(mContext, mIntent);
                handlerBroadcastDVRMessage();
                handlerBroadcastTimeshiftMessage(mIntent);
                handlerBroadcastTurnkeyMessage(mContext, mIntent);
                if (Intent.ACTION_SCREEN_OFF.equals(mIntent.getAction()) && isFromScanView){
                   isFromScanView = false;
                   TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
                }
            }
		}
	};

}
