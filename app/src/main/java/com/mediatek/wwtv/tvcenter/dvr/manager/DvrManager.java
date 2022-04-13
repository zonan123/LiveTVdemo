package com.mediatek.wwtv.tvcenter.dvr.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.view.KeyEvent;
import android.view.View;
import java.io.File;
import android.widget.Toast;

import java.lang.ref.WeakReference;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.dm.MountPoint;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.nav.TurnkeyService;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrConfirmDialog;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
//import com.mediatek.wwtv.setting.widget.view.ScheduleListItemInfoDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateBase;
import com.mediatek.wwtv.tvcenter.dvr.controller.StatusType;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.wwtv.tvcenter.dvr.controller.IManagerInterface;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.controller.UImanager;
import com.mediatek.wwtv.tvcenter.dvr.controller.TVLogicManager;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateNormal;
//import com.mediatek.wwtv.tvcenter.dvr.manager.Controller;
//import com.mediatek.wwtv.tvcenter.dvr.manager.CoreHelper;
//import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.cn.EPGCnActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.epg.sa.EPGSaActivity;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsActivity;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager.MyHandler;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import android.media.tv.TvContract;
import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;

import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.MtkTvRecordBase.RecordNotifyMsgType;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvRecordBase;
import android.view.WindowManager;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

public class DvrManager extends NavBasicMisc implements DevListener,
        ComponentStatusListener.ICStatusListener, // AfterDealCIEnqListener,
        IManagerInterface<StateBase> {

    private final static String TAG = "DvrManager";
    private volatile static DvrManager mDvrManager = null;
    private Controller mController = null;
    private StateBase mCurrentState = null;
    private Stack<StateBase> mStates = null;
    private MountPoint mDvrDiskMountPoint = null;
    private final MyHandler topHandler;
    private static MtkTvBookingBase scheduleItem = null;
    private static MtkTvBookingBase newscheduleItem = null;
    public static boolean isBGM = false;
    public boolean isBGMState = false;
    private boolean menuExit = false;
    public boolean isPvrDialogShow = false;
    private TVLogicManager mTVLogicManager;
    public UImanager uiManager;
    private final int tvWidth = ScreenConstant.SCREEN_WIDTH;
    private final int tvHeight = ScreenConstant.SCREEN_HEIGHT;
    private SaveValue saveValue;
    private final TextToSpeechUtil ttUtil;
    /**
     * when press rootmenu key ,it stop dvr ,will resume turnkey after enter
     * launcher, record the state to make choice weather need to resume ~
     */
    private boolean isStopDvrNotResumeLauncher = false;

    //private String prompt;
    public static final int PRO_PIP_STATE = 0x1000;
    public static final int PRO_NONE_TV_SOURCE = 0x1001;
    public static final int PRO_FEATURE_NOT_SUPPORT = 0x1002;
    public static final int PRO_SIGNAL_LOSS = 0x1003;
    public static final int PRO_TSHIFT_RUNNING = 0x1004;
    public static final int PRO_DISK_NOT_READY = 0x1005;
    public static final int PRO_RECORDING = 0x1006;
    public static final int PRO_AV_STREAM_NOT_AVAILABLE = 0x1007;
    public static final int PRO_STREAM_NOT_AUTHORIZED = 0x1008;
    public static final int PRO_INPUT_LOCKED = 0x1009;
    public static final int PRO_NO_ENOUGH_SPACE = 0x1010;
    public static final int PRO_VIDEO_RESOLUTION_ERROR = 0x1011;
    public static final int PRO_INTERNAL_ERROR = 0x1012;
    public static final int PRO_UNKNOW_ERROR = 0x1013;
    public static final int PRO_PVR_STOP = 0x1014;
    public static final int PRO_PVR_RUNNING = 0x1015;
    public static final int PRO_DISK_REMOVE = 0x1016;
    public static final int PRO_TIME_SHIFT_DISK_NOT_READY = 0x1017;

    private static final int DISK_NOT_READY = 0;
    public static final int CREATE_FAIL = 1;
    private static final int CREATE_SUCCESS = 2;
    // private static final int FEATURE_NOT_AVAIABLE = 311;
    // private static final int FEATURE_NOT_AVAIABLE1 = 312;
    // private static final int FEATURE_NOT_AVAIABLE2 = 313;
    public static final int DISK_ATTENTION = 4;
    private static final int RECORD_FINISHED = 5;
    // private static final int DISK_IS_FULL = 6;

    // private static final int FEATURE_NOT_SUPPORT = 1001;
    private static final int CONTINUE_TO_SEEK_NOT_SUPPORT = 2005;
    private static final int FILE_NOT_SUPPORT = 10002;
    public static final int Channel_NOT_Support = 8;
    public static final int CHANGE_CHANNEL = 2002;
    public static final int ALLOW_SYSTEM_SUSPEND = 2003;
    public static final int UNMOUNT_EVENT = 2004;
    public static final int USB_DEVICE_MOUNT = 2006;
    public static final String UNMOUNT_EVENT_MSG_KEY = "UNMOUNT_EVENT_MSG_KEY";

    private static final int SCHEDULE_PVR_TASK = 7;
    private static final int SCHEDULE_PVR_TASK_STOP_TIME = 0x2008;
    private static final int SCHEDULE_PVR_CHANGE_CHANNEL = 0x2009;

    public static final String SCHEDULE_PVR_SRCTYPE = "SCHEDULE_PVR_SRCTYPE";
    public static final String SCHEDULE_PVR_CHANNELLIST = "SCHEDULE_PVR_CHANNELLIST";
    public static final String SCHEDULE_PVR_REMINDER_TYPE = "SCHEDULE_PVR_REMINDER_TYPE";
    public static final String SCHEDULE_PVR_REPEAT_TYPE = "SCHEDULE_PVR_REPEAT_TYPE";
    public static final String AUTO_SYNC = "SETUP_auto_syn";
    public static final String TIME_DATE = "SETUP_date";
    public static final String TIME_TIME = "SETUP_time";
    public static final int INVALID_VALUE = 10004;
    public static final int STEP_VALUE = 1;
    public static final String TIMER_powerOnTime = "powerOnTime";
    public static final String DVR_URI_ID = "dvr_backgroud_reord";
    public static final String DVR_BG_TUNER_TYPE = "dvr_bg_tuner_type";

    public static final String DTV_TSHIFT_OPTION = "DTV_TSHIFT_OPTION";
    public static final String DTV_DEVICE_INFO = "DTV_DEVICE_INFO";

    public static final String RECORD_START = "Recording_Session_event_recordingStart";
    private boolean isbgm = false;
    private ResetBroadcast broadcast = null;
    private static int index = -1;
    private long timeduration = 0;
    public static final String BGM_PVR_LOSS="BGM_PVR_LOSS_Duration";
    public static final String BGM_PVR_FAIL="BGM_PVR_FAIL";
    public static final String BGM_PVR_FAIL_STR="BGM_PVR_FAIL_STR";
    private String name;
    private boolean isNeedTune= true;
    private boolean istuneSource = false;


    public DvrManager(Context context) {
        super(context);
        componentID = NAV_COMP_ID_PVR_TIMESHIFT;
        mStates = new Stack<StateBase>();
        saveValue = SaveValue.getInstance(context);
        ttUtil = new TextToSpeechUtil(mContext);
        mController = new Controller(context, this);
        topHandler = new MyHandler(TurnkeyUiMainActivity.getInstance().getApplicationContext());
        mTVLogicManager =new TVLogicManager(context, this);
        uiManager = new UImanager();
        setState(setDefaultState(TurnkeyUiMainActivity.getInstance()));
        deviceHandler.sendEmptyMessageDelayed(0, 1000);
        initItemMsg(false);
        initDiskPath();
        TVAsyncExecutor.getInstance().execute(this::addListener);
    }

    private void addListener(){
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_RESUME, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_ENTER_LANCHER, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_INPUTS_PANEL_SHOW, this);
    }
    private void initDiskPath(){
        String pvrtag=SaveValue.readWorldStringValue(mContext,"PVR_TAG");
        List<MountPoint> deviceList = DeviceManager.getInstance().getMountPointList();
        for(MountPoint i:deviceList){
            File file = new File(Util.getPath(i.mMountPoint,mContext)+Core.PVR_DISK_TAG);
            if(pvrtag.contains(i.mMountPoint)&&file.exists()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTuned ==> "+pvrtag);
                TVAsyncExecutor.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        MtkTvRecordBase.setDisk((i.mMountPoint).replace("storage","mnt/media_rw"));
                    }
                });
                break;
            }
            if(file.exists()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTuned => "+i.mMountPoint);
                TVAsyncExecutor.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        MtkTvRecordBase.setDisk((i.mMountPoint).replace("storage","mnt/media_rw"));
                    }
                });
                break;
            }
        }

    }

    private void initItemMsg(boolean isget){
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if(getController().getDeletItem()==1|isget) {
                    (TurnkeyUiMainActivity.getInstance()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,mContext.getString(R.string.msg_delet_schedule_item),Toast.LENGTH_LONG).show();
                            StateScheduleList.getInstance().updateBooks();
                        }
                    });
                }
            }
        });
    }
    private void initBrodcast() {
        if (broadcast == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initBrodcast");
            broadcast = new ResetBroadcast();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MASTER_CLEAR");
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(broadcast, filter);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initBrodcast failed ,already exsist !");
        }
    }

    private void unregistBroadcast() {
        if (broadcast != null) {
            try {
                mContext.unregisterReceiver(broadcast);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // broadcast = null;
        }
    }

    class ResetBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onReceive android.intent.action.MASTER_CLEAR");

            if (Intent.ACTION_SCREEN_OFF.equals(arg1.getAction())) {
                isbgm = true;
            }
            if (Intent.ACTION_SCREEN_ON.equals(arg1.getAction())) {
                isbgm = false;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isbgm=" + isbgm);
            if ("android.intent.action.MASTER_CLEAR".equals(arg1.getAction())) {
                if (DvrManager.getInstance() != null) {
                    if (DvrManager.getInstance().pvrIsRecording()) {
                        getController().stopRecording();
                    }
                }
            }
        }
    }

    public boolean checkScrambled() {

        TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                .getCurrChannelInfo();
        if (tifChannelInfo != null) {
            MtkTvChannelInfoBase channel = tifChannelInfo.mMtkTvChannelInfo;
            int nwMask = channel.getNwMask();
            return ((nwMask & MtkTvChCommonBase.SB_VNET_SCRAMBLED) > 0) ? true
                    : false;
        }
        return false;
    }

    private StateNormal setDefaultState(Context context) {
        this.setVisibility(View.INVISIBLE);
        return new StateNormal(context.getApplicationContext(), this);
    }

    public void startDvr(Uri programUri) {
        mController.startRecording(programUri);
        index=-1;
        initBrodcast();
    }

    public void stopDvr() {
        mController.stopRecording();

        unregistBroadcast();
    }

    public void stopAllRunning() {

        if (null != mCurrentState
                && "StateNormal".equals(mCurrentState.getClass()
                        .getSimpleName())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("stopAllRunning", "mCurrentState:"
                    + mCurrentState.getClass().getSimpleName());
        } else {
            try {
                if (StateDvr.getInstance() != null
                        && StateDvr.getInstance().isRunning()) {
                    StateDvr.getInstance().getHandler()
                            .removeMessages(StateDvr.REFRESH_TIMER);
                    mController.stopRecording();
                    // restoreToDefault(StateDvr.getInstance());
                }
                if (StateDvrFileList.getInstance() != null
                        && StateDvrFileList.getInstance().isRunning()) {
                    restoreToDefault(StateDvrFileList.getInstance());
                }
                if (StateDvrPlayback.getInstance() != null
                        && StateDvrPlayback.getInstance().isRunning()) {
                    restoreToDefault(StateDvrPlayback.getInstance());
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "stopAllRunning error== " + e.toString());
                e.printStackTrace();
            }
            if (null != uiManager) {
                uiManager.dissmiss();
            }
        }
    }

    /**
     * getInstance
     */
    public static DvrManager getDvrmanage(Context context) {
        if (null == mDvrManager) {
             synchronized (DvrManager.class){
                 if (null == mDvrManager) {
                     mDvrManager = new DvrManager(context);
                 }
             }
        }
        return mDvrManager;
    }

    @Override
    public boolean deinitView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        unregistBroadcast();
        if(pvrIsRecording()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"pvr is running");
            return false;
        }
        synchronized (DvrManager.class) {
            mDvrManager = null;
        }
        deviceHandler.removeCallbacksAndMessages(null);
        ComponentStatusListener.getInstance().removeListener(this);
        DevManager.getInstance().removeDevListener(DvrManager.this);
        for(StateBase stateBase:mStates){
            stateBase.free();
        }
        mStates.clear();
        dumpStatesStack();
        return super.deinitView();
    }

    public synchronized void updateStateContext(Context mContext) {

        if (mStates != null) {
            for (int i = mStates.size() - 1; i >= 0; i--) {
                mStates.get(i).setmContext(mContext);
            }
        }
    }

    /**
     * getInstance
     */
    public synchronized static DvrManager getInstance() {
        //if (null == mDvrManager) {
        //    mDvrManager = new DvrManager(TurnkeyUiMainActivity.getInstance());
        //}
        return mDvrManager;
    }

    public static class MyHandler extends Handler {

        Context context;

        MyHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case USB_DEVICE_MOUNT:

                break;
            case DISK_NOT_READY:

                break;
            case SCHEDULE_PVR_CHANGE_CHANNEL:
                if (scheduleItem != null) {
                    int channelID = scheduleItem.getChannelId();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "prepareScheduleTask:" + channelID + ","
                            + isBGM);
                    if (channelID == -1 || channelID == 0) {
                        return;
                    }
                    if (!InputSourceManager.getInstance().isCurrentTvSource(
                            CommonIntegration.getInstance().getCurrentFocus())) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                "handleMessage,SCHEDULE_PVR_CHANGE_CHANNEL");
                        InputSourceManager.getInstance().saveOutputSourceName(
                                "TV",
                                CommonIntegration.getInstance()
                                        .getCurrentFocus());
                    }

                    if (isBGM) {
                        MtkTvChannelInfoBase channelInfo = CommonIntegration
                                .getInstance().getChannelById(channelID);
                        MtkTvBroadcast mBroadcast = CommonIntegration
                                .getInstance().getInstanceMtkTvBroadcast();
                        if (channelInfo != null && mBroadcast != null) {
                            if (mBroadcast.channelSelect(channelInfo, false) != 0) {
                                mBroadcast.channelSelect(
                                        channelInfo, false);
                            }
                        }
                    } else {
                        CommonIntegration.getInstance().selectChannelById(
                                channelID);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage,prepareScheduleTask, true");
                }
                break;

            case CREATE_FAIL:
                // mDvrManager.showCreateFileSuccessUI(false);
                break;

            case CREATE_SUCCESS:
                // mDvrManager.showCreateFileSuccessUI(true);
                break;
            // case FEATURE_NOT_SUPPORT:
            // mDvrManager.uiManager.showInfoBar(activity
            // .getString(R.string.feature_not_support));
            // break;
            case CONTINUE_TO_SEEK_NOT_SUPPORT:
                mDvrManager.uiManager.showInfoBar(context
                        .getString(R.string.continue_to_seek_not_support));
                break;
            case FILE_NOT_SUPPORT:
                mDvrManager.showPromptInfo(FILE_NOT_SUPPORT);
                break;
            // case FEATURE_NOT_AVAIABLE:
            // int errorID = MtkTvRecord.getInstance().getErrorID();
            // String errorMessage = "Feature Not Support !";
            // com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "errorID:" + errorID);
            // switch (errorID) {
            // case 0: // REC_PVR_ERR_ID_NONE
            // errorMessage = "Feature not available!";
            // break;
            // case 1:// REC_PVR_ERR_ID_RECORDING:
            // errorMessage = "Recording !";
            // break;
            // case 2:// REC_PVR_ERR_ID_UNKNOWN_SRC:
            // errorMessage = "Can not do pvr for the wrong video !";
            // break;
            // case 3:// REC_PVR_ERR_ID_FEATURE_NOT_SUPPORTED:
            // errorMessage = "Feature not support !";
            // break;
            // case 4:// REC_PVR_ERR_ID_INSUFFICIENT_RESOURCE:
            // errorMessage = "Feature not support !";//
            // "Insufficient resource !";
            // break;
            // case 5:// REC_PVR_ERR_ID_AV_STREAM_NOT_AVAILABLE:
            // errorMessage = "AV stream not available !";
            // break;
            // case 6:// REC_PVR_ERR_ID_STREAM_NOT_AUTHORIZED:
            // errorMessage = "Stream not authorized!";
            // break;
            // case 7:// REC_PVR_ERR_ID_INPUT_LOCKED:
            // errorMessage = "Input locked !";
            // break;
            // case 8:// REC_PVR_ERR_ID_DISK_NOT_READY:
            // case 24867948:
            // errorMessage = "Disk not ready!";
            // break;
            // case 9:// REC_PVR_ERR_ID_DISK_TOO_SMALL:
            // errorMessage = "There is no enough free space!";//
            // "Disk too small!";
            // break;
            // case 10:// REC_PVR_ERR_ID_DISK_FULL:
            // errorMessage = "There is no enough free space!";// "Disk full!";
            // break;
            // case 11:// REC_PVR_ERR_ID_VIDEO_RESOLUTION_ERROR:
            // case 16:
            // errorMessage = "Feature not available";//
            // "Video resolution error !";
            // break;
            // case 12:// REC_PVR_ERR_ID_INTERNAL_ERROR:
            // errorMessage = "Feature not support !";// "Internal error!";
            // break;
            // case 13:// REC_PVR_ERR_ID_LAST:
            // errorMessage = "Feature not support !";
            // break;
            // default:
            // break;
            // }
            // mDvrManager.uiManager.showInfoBar(errorMessage);
            // break;
            // case FEATURE_NOT_AVAIABLE1:
            // String errorMessage1 = "Feature not available";
            // PwdDialog mPwdDialog = (PwdDialog)
            // ComponentsManager.getInstance()
            // .getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
            // if (mPwdDialog != null && mPwdDialog.isVisible()) {
            // mPwdDialog.dismiss();
            // }
            // mDvrManager.uiManager.showInfoBar(errorMessage1);
            // break;
            // case FEATURE_NOT_AVAIABLE2:
            // String errorMessage2 = "Can not do pvr for the wrong video";
            // mDvrManager.uiManager.showInfoBar(errorMessage2);
            // break;
            // case Channel_NOT_Support:
            // Fix CR:DTV00586957
            // staticManager.uiManager.showInfoBar("Channel Not Support");
            // mDvrManager.showFeatureNotAvaiable();
            // break;
            // case DISK_ATTENTION:
            // mDvrManager.uiManager.showInfoBar(activity
            // .getString(R.string.attention_unplug_device));
            // break;

            case RECORD_FINISHED:
                break;

            // case DISK_IS_FULL:
            // mDvrManager.uiManager.showInfoBar(activity
            // .getString(R.string.not_enough_space));
            // break;

            case SCHEDULE_PVR_TASK:
                mDvrManager.prepareSchedulePvrTask(msg.arg1);
                break;
            case SCHEDULE_PVR_TASK_STOP_TIME:
                mDvrManager.setPVRStopTime(msg.getData().getLong("Duration"));
                break;

            case DvrConstant.RECORD_FINISH_AND_SAVE:
                mDvrManager.uiManager.showInfoBar(context
                        .getString(R.string.pvr_save_record));
                // dismiss channel Lock info when exit pvr
                if (ComponentsManager.getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_TWINKLE_MSG) != null) {
                    TwinkleDialog.hideTwinkle();
                }
                if (StateDvr.getInstance().isChangeSource()) {
                    CommonIntegration.getInstance().channelUp();
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, 0);
                    StateDvr.getInstance().setChangeSource(false);
                }
                break;

            // case DvrConstant.Disk_Disconnect:
            // mDvrManager.uiManager.showInfoBar(activity
            // .getString(R.string.disk_disconnect));
            // break;
            case DvrConstant.Dissmiss_Info_Bar:
                if (mDvrManager.uiManager != null) {
                    mDvrManager.uiManager.dissmiss();
                    if (StateDvrFileList.getInstance() != null
                            && StateDvrFileList.getInstance().isShowing()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dvrfilelist show");
                    } else {
                        PwdDialog mPWDDialog = (PwdDialog) ComponentsManager
                                .getInstance().getComponentById(
                                        NavBasic.NAV_COMP_ID_PWD_DLG);
                        if (MtkTvPWDDialog.getInstance().PWDShow() == 0
                                && !(DvrManager.getInstance() != null && DvrManager
                                        .getInstance().pvrIsRecording())) {
                            if (mPWDDialog != null) {
                                mPWDDialog.show();
                            }
                        } else {
                            if (!(DvrManager.getInstance() != null && DvrManager
                                    .getInstance().pvrIsRecording())) {
                                TwinkleDialog.showTwinkle();
                            }
                        }
                    }
                }
                boolean isRuning = false;
                if (mDvrManager.getState() instanceof StateDvr) {
                    isRuning = ((StateDvr) mDvrManager.getState()).isRunning();
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.e("timeshift", "timeshift:isRuning:" + isRuning);
                break;
            case DvrConstant.Finish_Timeshift_Activity:

                break;
            case CHANGE_CHANNEL:
                mDvrManager.getController().changeChannelByID(msg.arg2,
                        msg.arg1);
                break;

            case UNMOUNT_EVENT:
                mDvrManager.unmountEvent(msg.getData().getString(
                        UNMOUNT_EVENT_MSG_KEY));

                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }

    }

    public class ScheduleHandler extends Handler {
        private final DvrDialog dialog;
        private final MtkTvBookingBase item;

        public ScheduleHandler(DvrDialog dialog, MtkTvBookingBase item) {
            this.dialog = dialog;
            this.item = item;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ScheduleHandler, handleMessage");
            try {
                dialog.dismiss();
                if (scheduleItem != null) {
                    if (item.getRecordMode() != 1) {
                        TifTimeShiftManager mTimeShiftManager = TurnkeyUiMainActivity
                                .getInstance().getmTifTimeShiftManager();
                        if (mTimeShiftManager != null
                                && SaveValue
                                        .getInstance(mContext)
                                        .readLocalMemoryBooleanValue(
                                                MenuConfigManager.TIMESHIFT_START)) {
                            mTimeShiftManager.stop();
                            SaveValue.getInstance(mContext).setLocalMemoryValue(
                                    MenuConfigManager.TIMESHIFT_START, false);
                            /*SaveValue.saveWorldBooleanValue(mContext,
                                    MenuConfigManager.TIMESHIFT_START, false,
                                    false);*/
                            SystemClock.sleep(1000);
                        } else if (null != StateDvr.getInstance()
                                && StateDvr.getInstance().isRunning()) {
                            DvrManager.getInstance().stopDvr();
                            return;
                        }
                        // MtkTvRecord.getInstance().deleteBooking(index);
                        startSchedulePvr();
                    } else {
                        clearSchedulePvr();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private final Handler deviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "DevManager.getInstance()-------");
                DevManager.getInstance().addDevListener(DvrManager.this);
                if (!checkPvrMP()) {
                    setPvrMountPoint(null);
                }
                initItemMsg(false);
                com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "DevManager.getInstance()-----error--");
                deviceHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    };
    private final static int MSG_SCHEUDLE_START = 1;
    private final static int MSG_SCHEUDLE_CANCEL = 2;
    private final static int MSG_SCHEDULE_CHANGE_SOURCE = 3;
    private Handler mScheduleHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SCHEUDLE_START:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isbgm=" + isbgm);
                //handlerScheduleData(index);
                startSchedulePvr();
                break;
            case MSG_SCHEUDLE_CANCEL:
                handlerScheduleData(index);
                removeMessages(MSG_SCHEUDLE_START);
                SaveValue.saveWorldBooleanValue(mContext,"pvr_is_dialog_show",false,false);
                index = -1;
                break;
            case MSG_SCHEDULE_CHANGE_SOURCE:
                setDuration();
                if (scheduleItem != null) {
                    //handlerScheduleData(index);
                    if (scheduleItem.getRecordMode() == 1) {
                        TurnkeyUiMainActivity.getInstance().resumeTurnkeyActivity(mContext);
                        setTuneChannel(true);
                        index = -1;
                    } else {
                        setScheduleTypeAction();
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("1111+", "handler");
                }
                break;
            default:
                index = -1;
                break;
            }
            super.handleMessage(msg);
        }
    };

    private void setScheduleTypeAction() {
//        handlerScheduleData(index);
//        index = -1;
        Bundle mBundle = new Bundle();
        if (!DestroyApp.isCurActivityTkuiMainActivity()
           &&MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE)!=0 || isbgm) {
            //mDvrManager.getController().setBGM();
            mBundle.putBoolean("session_event_dvr_record_in_bgm",true);
        }else{
            mBundle.putBoolean("session_event_dvr_record_in_bgm",false);
        }

        TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                .getChannelInfoByChannelIdAndSvlId(scheduleItem.getChannelId(),scheduleItem.getSvlId());
        TIFChannelInfo currentChannelInfo = TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
        if(istuneSource){
            mBundle.putBoolean("session_event_dvr_record_is_onetouch",false);
        }else{
            if(tifChannelInfo!=null&&currentChannelInfo!=null&&
                    tifChannelInfo.mId==currentChannelInfo.mId){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"schedule pvr  is current channel");
                mBundle.putBoolean("session_event_dvr_record_is_onetouch",true);
            }
        }
        if(tifChannelInfo!=null){
            SaveValue.getInstance(mContext).saveValue(DVR_URI_ID,
                    tifChannelInfo.mId);
            mDvrManager.getController().tune(
                    TvContract.buildChannelUri(tifChannelInfo.mId), mBundle);
            SaveValue.saveWorldValue(mContext,DVR_URI_ID,(int)tifChannelInfo.mId,false);
            setScheduleModel();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"name="+name);
            SaveValue.writeWorldStringValue(mContext,BGM_PVR_FAIL_STR,
                    name,true);
        }else{
            index = -1;
        }
    }

    private void setScheduleModel(){
        Bundle modebundle = new Bundle();
        int model = 0;
        if((DestroyApp.isCurActivityTkuiMainActivity()
            ||!DestroyApp.isCurActivityTkuiMainActivity()
            &&MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE)==0)
            &&scheduleItem.getType()!=1){
            //MtkTvRecordBase.setRecordModeByHandle(0,1);//MtkTvRecordBase.RecordPvrMode.PVR_MODE_SCHEDULE_NORMAL);
            model=1;
        }
        if((DestroyApp.isCurActivityTkuiMainActivity()
            ||!DestroyApp.isCurActivityTkuiMainActivity()
            &&MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE)==0)
            &&scheduleItem.getType()==1){
            //MtkTvRecordBase.setRecordModeByHandle(0,4);//MtkTvRecordBase.RecordPvrMode.PVR_MODE_NORMAL_ACCURATE);
            model=4;
        }
        if(!DestroyApp.isCurActivityTkuiMainActivity()
            &&MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE)!=0
            &&scheduleItem.getType()!=1){
            //MtkTvRecordBase.setRecordModeByHandle(0,2);//MtkTvRecordBase.RecordPvrMode.PVR_MODE_SCHEDULE_BG);
            model=2;
        }

        if(!DestroyApp.isCurActivityTkuiMainActivity()
            &&MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE)!=0
            &&scheduleItem.getType()==1){
            //MtkTvRecordBase.setRecordModeByHandle(0,5);//MtkTvRecordBase.RecordPvrMode.PVR_MODE_SCHEDULE_BG_ACCURATE);
            model=5;
        }
        int eventId = scheduleItem.getEventId();
        modebundle.putInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_RECORD_PVR_MODE_VALUE,model);
        modebundle.putInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_RECORD_EVENT_ID_VALUE,eventId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mode==" + model +" eventId==" + eventId);
        getController().sendRecordCommand(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SET_PVR_MODE,modebundle);
    }

    private void getStartTime(int index) {
        List<MtkTvBookingBase> bookingBases = null;
        bookingBases = MtkTvRecord.getInstance().getBookingList();
        if (!bookingBases.isEmpty() ){//&& bookingBases.size() > 0) {
            MtkTvBookingBase bookingBase = bookingBases.get(index);
            this.newscheduleItem = bookingBase;
        }else{
            this.newscheduleItem = null;
        }
    }

    /**
     * recieve callback from turnkey
     *
     * @param data
     */
    public void handleRecordNTF(TvCallbackData data) {
        TvCallbackData dataRecord = data;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleRecordNotifyMsg, " + "dataRecord.param1:"
                + dataRecord.param1 + ",dataRecord.param2:" + dataRecord.param2
                + ",dataRecord.param2:" + dataRecord.param3);
        if ((RecordNotifyMsgType.values()[dataRecord.param1] == RecordNotifyMsgType.RECORD_PVR_NTFY_SCHEDULE_ITEM_EXPIRED_AND_DELETED)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"notify delet msg");
            initItemMsg(true);
        }
        // 13 = start
        if ((RecordNotifyMsgType.values()[dataRecord.param1] == RecordNotifyMsgType.RECORD_PVR_NTFY_VIEW_SCHEDULE_START)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index=" + index + ",param2=" + dataRecord.param2);
            if (index == dataRecord.param2) {
                return;
            }
            getStartTime(dataRecord.param2);// handlerScheduleData(dataRecord.param2);
            if(newscheduleItem==null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"newschedule== null");
                return;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"newschedule=="+newscheduleItem+",isNeedTune->"+isNeedTune+",pvrIsRecording->"+!pvrIsRecording());
            long duration = getDuration(newscheduleItem);
            if (duration > 15 || duration+newscheduleItem.getRecordDuration() < 0||scheduleItem == newscheduleItem) {
                return;
            }
            index = dataRecord.param2;
            this.scheduleItem = newscheduleItem;
            if (!isbgm && dataRecord.param3 != 0) {
                // mController.setBGM();
                isbgm = true;
               //mDvrManager.getController().setBGM();
            } else {
                isbgm = false;
            }
            Activity act = DestroyApp.getTopActivity();
            if (act != null
                    && (act instanceof EPGEuActivity
                            || act instanceof EPGSaActivity
                            || act instanceof EPGCnActivity || act instanceof EPGUsActivity)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "DestroyApp.getTopActivity(): "
                                + act.getComponentName());
                act.finish();
            }

            Intent menuintent = new Intent("finish_live_tv_settings");
            mContext.sendBroadcast(menuintent, "com.mediatek.tv.permission.BROADCAST");
            if (scheduleItem.getRecordMode() == 1) {// reminder
                mScheduleHandler.sendEmptyMessageDelayed(MSG_SCHEUDLE_START,
                        getDuration(scheduleItem) * 1000);
            } else {
                // 0 none
                // 2 record
                // 3 CI
                initBrodcast();
                mScheduleHandler.sendEmptyMessageDelayed(MSG_SCHEUDLE_START,
                        getDuration(scheduleItem) * 1000);
            }
            Intent intent = new Intent(mContext, TurnkeyService.class);
            Bundle bundle = new Bundle();
            TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                    .getChannelInfoByChannelIdAndSvlId(scheduleItem.getChannelId(),scheduleItem.getSvlId());
            MtkTvChannelInfoBase ch = null;
            if(tifChannelInfo !=null ) {
                 ch = tifChannelInfo.mMtkTvChannelInfo;
            }//CommonIntegration.getInstance().getChannelById(scheduleItem.getChannelId());
            name = ch!=null?ch.getServiceName():scheduleItem.getEventTitle();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"name--> "+ name);
            if(scheduleItem.getRecordMode() == 1){
                bundle.putString(
                        "PROMPT",
                        mContext.getResources().getString(
                                R.string.dvr_schedule_watch_content)
                                + "("
                                + name
                                + "), "
                                + mContext
                                        .getResources()
                                        .getString(
                                                R.string.dvr_schedule_watch_after));
            }else{
            bundle.putString(
                    "PROMPT",
                    mContext.getResources().getString(
                            R.string.dvr_dialog_message_shcedule_before)
                            + "("
                            + name
                            + "), "
                            + mContext
                                    .getResources()
                                    .getString(
                                            R.string.dvr_dialog_message_schedule_record));
            }
            bundle.putString(
                    "TITLE",
                    mContext.getResources().getString(
                            R.string.dvr_dialog_message_schedule_title));
            bundle.putInt("CONFIRM_TYPE",
                    DvrConfirmDialog.DVR_CONFIRM_TYPE_SCHEDULE);
            bundle.putLong("TIME", getDuration(scheduleItem) * 1000);
            intent.putExtra("SCHEDULE", bundle);
            if(!(isbgm&&(getDuration(scheduleItem)<=0))) {
                SaveValue.saveWorldBooleanValue(mContext,"pvr_is_dialog_show",true,false);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"pvr is dialog = "+SaveValue.readWorldBooleanValue(mContext,"pvr_is_dialog_show"));
                mContext.startForegroundService(intent);
            }
            if (ScheduleListDialog.getDialog() != null
                    && ScheduleListDialog.getDialog().isShowing()) {
                // CR 959741 ,follow linux ,dismiss schedule dialog when confirm
                // dialog show.
                ScheduleListDialog.getDialog().dismiss();
            }
            if (ScheduleListItemDialog
                    .getInstance() != null&&ScheduleListItemDialog.getInstance().isShowing()) {
                ScheduleListItemDialog.getInstance()
                        .dismiss();
            }
            if (StateDvrFileList.getInstance(DvrManager.getInstance()).isShowing()) {
                StateDvrFileList.getInstance(DvrManager.getInstance()).dissmiss();
            }
        }
    }

    private long getDuration(MtkTvBookingBase scheduleItem) {
        long start = scheduleItem.getRecordStartTime();
        long mUtcTime = MtkTvTime.getInstance()
                    .getCurrentTimeInUtcSeconds();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"Utc time== "+mUtcTime);
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "time duration==" + (start - mUtcTime));
        return start - mUtcTime;
    }

    /**
     * recieve data from dvrConfirm dialog
     *
     * @param isStart
     */
    public void handleRecordNTF(boolean isStart) {

        if (isStart) {
            //handlerScheduleData(index);
            mScheduleHandler.removeMessages(MSG_SCHEUDLE_START);
            StateNormal.setTuned(true);
            startSchedulePvr();
        } else {
            mScheduleHandler.sendEmptyMessage(MSG_SCHEUDLE_CANCEL);
        }
    }

    /**
     * schedule data arrangement
     *
     * @param index
     */
    private void handlerScheduleData(int index) {
        if (index == -1){
            return;
            }
        List<MtkTvBookingBase> bookingBases = null;
        bookingBases = MtkTvRecord.getInstance().getBookingList();
        if (!bookingBases.isEmpty()) {
            MtkTvBookingBase bookingBase = bookingBases.get(index);
            //this.newscheduleItem = bookingBase;
            if (bookingBase.getRepeatMode() == 128) {
                MtkTvRecord.getInstance().deleteBooking(index);
            }
            if (bookingBase.getRepeatMode() == 0) {// repeat type = daily
                MtkTvRecord.getInstance().deleteBooking(index);
                long startTime = bookingBase.getRecordStartTime() + 24 * 60 * 60;
                bookingBase.setRecordStartTime(startTime);
                MtkTvRecord.getInstance().addBooking(bookingBase);
            }
            if (bookingBase.getRepeatMode() != 0
                    && bookingBase.getRepeatMode() != 128) {
                int repeatcount = bookingBase.getRepeatMode();
                int weekday = MtkTvTime.getInstance().getLocalTime().weekDay;
                long startTime = 0;
                int count = -6;
                for (int i = 6; i >= 0; i--) {
                    if ((repeatcount & (1 << i)) == (1 << i)) {
                        if (i > weekday) {
                            count = i - weekday;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "count1==" + count);
                        } else {
                            if (count <= 0) {
                                count = i - weekday;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "count2==" + count);
                            }
                        }
                    }
                }
                if (count == 0) {
                    count = 7;
                }
                MtkTvRecord.getInstance().deleteBooking(index);
                startTime = bookingBase.getRecordStartTime() + count * 24 * 60
                        * 60;
                bookingBase.setRecordStartTime(startTime);
                MtkTvRecord.getInstance().addBooking(bookingBase);
            }
        }
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "updateComponentStatus:" + statusID);
        // TODO Auto-generated method stub
        if (isExitMenu() && statusID == ComponentStatusListener.NAV_RESUME) {
            setVisibility(View.VISIBLE);
            setExitMenu(false);
        }
        if (statusID == ComponentStatusListener.NAV_RESUME
                && StateDvr.getInstance() != null
                && StateDvr.getInstance().isRunning()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "updateComponentStatus: ====>BGM");
            setVisibility(View.VISIBLE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "activity2"
                    + ComponentsManager.getInstance().getActiveCompId());
        }
        // will set state to playback when resume turnkey .
        if (statusID == ComponentStatusListener.NAV_RESUME
                && StateDvrPlayback.getInstance() != null
                && StateDvrPlayback.getInstance().isRunning()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "updateComponentStatus: ====> StatedvrPlayback !");
            // DvrManager.getInstance().setState(StateDvrPlayback.getInstance());
        }
        if (statusID == ComponentStatusListener.NAV_INPUTS_PANEL_SHOW) {
            if (uiManager != null) {
                uiManager.hiddenAllViews();
            }
            if(ScheduleListItemDialog.getInstance()!=null&&ScheduleListItemDialog.getInstance().isShowing()){
                ScheduleListItemDialog.getInstance().dismiss();
            }
        }
    }

    public void toRecord() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "toRecord");
        if (scheduleItem != null) {
            Message msg = getTopHandler().obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putLong("Duration", scheduleItem.getRecordDuration());
            msg.setData(bundle);
            msg.what = SCHEDULE_PVR_TASK_STOP_TIME;
            getTopHandler().sendMessage(msg);
            scheduleItem = null;
            index = -1;
        }
    }

    private void unmountEvent(String mountPointPath) {

        if (getState() instanceof StateDvrFileList
                && ((StateDvrFileList) getState()).isShowing()) {
            DvrManager.getInstance().restoreToDefault(getState());
            showPromptInfo(PRO_DISK_REMOVE);
        }
        if (getState() instanceof StateDvrPlayback
                && ((StateDvrPlayback) getState()).isRunning()) {
            showPromptInfo(PRO_DISK_REMOVE);
            StateDvrPlayback.getInstance().stopDvrFilePlay();
        }

//        if (getPvrMountPoint() == null
//                || mountPointPath
//                        .equalsIgnoreCase(getPvrMountPoint().mMountPoint)) {
//            restoreToDefault(StatusType.DVR);
//            setPvrMountPoint(null);
//        }

    }

    public int getTVHeight() {
        return tvHeight;
    }

    public int getTVWidth() {
        return tvWidth;
    }

    public Context getContext() {

        return mContext;
    }

    /**
     * getController
     */
    public Controller getController() {
        return mController;
    }

    public boolean isExitMenu() {
        return menuExit;
    }

    public void setExitMenu(boolean value) {
        menuExit = value;
    }

    /**
     * @return the mTVLogicManager
     */
    public TVLogicManager getTvLogicManager() {
        return mTVLogicManager;
    }

    public UImanager getUiManager() {
        return uiManager;
    }

    public boolean checkPvrMP() {
        List<MountPoint> list = getDeviceList();
        if (list == null) {
            return false;
        }

        if (!list.isEmpty()) {
            String mp = getSaveValue().readStrValue(CoreHelper.PVR_DISK);
            mDvrDiskMountPoint = list.get(0);

            for (MountPoint i: list) {
                if (mp.equalsIgnoreCase(i.mMountPoint)) {
                    mDvrDiskMountPoint = i;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param mTVLogicManager
     *            the mTVLogicManager to set
     */
    public void setmTVLogicManager(TVLogicManager mTVLogicManager) {
        this.mTVLogicManager = mTVLogicManager;
    }

    public SaveValue getSaveValue() {
        if (saveValue == null) {
            saveValue = SaveValue.getInstance(mContext);
        }
        return saveValue;
    }

    public void setSaveValue(SaveValue saveValue) {
        this.saveValue = saveValue;
    }

    public boolean isPvrDialogShow() {
        return isPvrDialogShow;
    }

    public void setPvrDialogShow(boolean isPvrDialogShow) {
        this.isPvrDialogShow = isPvrDialogShow;
    }

    /**
     * @return the topHandler
     */
    public MyHandler getTopHandler() {
        return topHandler;
    }

    public static MtkTvBookingBase getScheduleItem() {
        return scheduleItem;
    }

    public static void setScheduleItem(MtkTvBookingBase scheduleItem) {
        DvrManager.scheduleItem = scheduleItem;
        DvrManager.newscheduleItem = scheduleItem;
        index=-1;
    }

    public static MtkTvBookingBase getnewScheduleItem() {
        return newscheduleItem;
    }

    /**
     * called by UImanager for show prompt message .
     *
     * @param prompt
     *            message on UI displayed
     */
    public void showPromptInfo(int cases) {
        if(!getController().isScreenOn(mContext)){
            return;
        }
        int a = 0;
        SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        if (simpleDialog!=null&&simpleDialog.isShowing()) {
            simpleDialog.dismiss();
        }
        String prompt="";
        switch (cases) {
        case PRO_PIP_STATE:
            prompt = mContext.getString(R.string.pro_pip_running);
            a = R.string.pro_pip_running;
            break;
        case PRO_NONE_TV_SOURCE:
            prompt = mContext.getString(R.string.pro_none_tv_source);
            a = R.string.pro_none_tv_source;
            break;
        case PRO_FEATURE_NOT_SUPPORT:
            prompt = mContext.getString(R.string.feature_not_support);
            a = R.string.feature_not_support;
            break;
        case PRO_SIGNAL_LOSS:
            prompt = mContext.getString(R.string.pro_signal_loss);
            a = R.string.pro_signal_loss;
            break;
        case PRO_TSHIFT_RUNNING:
            prompt = mContext.getString(R.string.pro_tshift_running);
            a = R.string.pro_tshift_running;
            break;
        case PRO_DISK_NOT_READY:
            prompt = mContext.getString(R.string.disk_not_ready);
            a = R.string.disk_not_ready;
            break;
        case PRO_TIME_SHIFT_DISK_NOT_READY:
            prompt = mContext.getString(R.string.pro_time_shift_disk_not_ready);
            a = R.string.pro_time_shift_disk_not_ready;
            break;
        case PRO_RECORDING:
            prompt = mContext.getString(R.string.state_pvr_recording);
            a = R.string.state_pvr_recording;
            break;
        case PRO_AV_STREAM_NOT_AVAILABLE:
            prompt = mContext.getString(R.string.pro_stream_not_available);
            a = R.string.pro_stream_not_available;
            break;
        case PRO_STREAM_NOT_AUTHORIZED:
            prompt = mContext.getString(R.string.pro_stream_not_authorized);
            a = R.string.pro_stream_not_authorized;
            break;
        case PRO_INPUT_LOCKED:
            prompt = mContext.getString(R.string.pro_input_lock);
            a = R.string.pro_input_lock;
            break;
        case PRO_NO_ENOUGH_SPACE:
            prompt = mContext.getString(R.string.not_enough_space);
            a = R.string.not_enough_space;
            break;
        case PRO_VIDEO_RESOLUTION_ERROR:
            prompt = mContext.getString(R.string.pro_video_resolution_error);
            a = R.string.pro_video_resolution_error;
            break;
        case PRO_INTERNAL_ERROR:
            prompt = mContext.getString(R.string.pro_internal_error);
            a = R.string.pro_internal_error;
            break;
        case PRO_UNKNOW_ERROR:
            prompt = mContext.getString(R.string.feature_not_support);
            a = R.string.feature_not_support;
            break;
        case PRO_PVR_STOP:
            prompt = mContext.getString(R.string.pvr_save_record);
            a = R.string.pvr_save_record;
            break;
        case PRO_PVR_RUNNING:
            prompt = mContext.getString(R.string.pro_pvr_running);
            a = R.string.pro_pvr_running;
            break;
        case PRO_DISK_REMOVE:
            prompt = mContext.getString(R.string.pro_disk_remove);
            a = R.string.pro_disk_remove;
            break;
        case DISK_ATTENTION:
            prompt = mContext.getString(R.string.attention_unplug_device);
            a = R.string.attention_unplug_device;
            break;
        case FILE_NOT_SUPPORT:
            a = R.string.file_notsupport;
            break;
        default:
            prompt = mContext.getString(R.string.feature_not_support);
            a = R.string.feature_not_support;
            break;
        }

        // ((Activity) mContext).runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // mTVLogicManager.removeTwinkView();
        // uiManager.showInfoBar(prompt);
        //
        // }
        // });
        if(simpleDialog != null) {
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//important
            simpleDialog.setScheduleDismissTime(5L * 1000);
            simpleDialog.setContent(a);
            simpleDialog.show();
        }
        speakText(prompt);
    }

    public void showContinueToSeekNotSupport() {
        topHandler.sendEmptyMessage(CONTINUE_TO_SEEK_NOT_SUPPORT);
    }

    public void showFileNotSupport() {
        topHandler.sendEmptyMessage(FILE_NOT_SUPPORT);
    }

    public boolean diskIsReady() {
        return hasRemovableDisk();
    }

    @Override
    public boolean isVisible() {
        if (StateDvrPlayback.getInstance() != null
                && StateDvrPlayback.getInstance().isRunning()) {
            return true;
        }
        return super.isVisible();
    }

    public void restoreToDefault(StateBase state) {
        if(state!=null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "restoreToDefault()");
            restoreToDefault(state.getType());
        }
    }

    public synchronized void restoreToDefault(StatusType... type) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "restoreToDefault(...)");

        List<StatusType> typeList = Arrays.asList(type);

        StateBase state;
        for (int i = mStates.size() - 1; i >= 0; i--) {
            state = mStates.get(i);

            if (typeList.contains(state.getType())) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "restoreToNormal(...),Name:"
                        + state.getType().name());
                mStates.remove(state);
                state.free();
                state.onRelease();
            }
        }

        updateVisibility();
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "isKeyHandler:" + keyCode);
        return onKeyDown(keyCode);
    }

    @Override
    public boolean isCoExist(int componentID) {
        switch (componentID) {
        // case NAV_COMP_ID_BANNER:
        case NAV_COMP_ID_INPUT_SRC:
        case NAV_COMP_ID_SUNDRY:

            // case NAV_COMP_ID_VOL_CTRL:
        case NAV_COMP_ID_PVR_TIMESHIFT:
        case NAV_COMP_ID_ZOOM_PAN:
        case NAV_COMP_ID_CH_LIST:
        case NAV_COMP_ID_POP:
            return true;
        case NAV_COMP_ID_FAV_LIST:
        case NAV_COMP_ID_PWD_DLG:
        case NAV_COMP_ID_VOL_CTRL:
            return false;

        case NAV_COMP_ID_TWINKLE_MSG:
        case NAV_COMP_ID_CEC:
            return true;
        default:
            break;
        }
        return super.isCoExist(componentID);
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler keyCode = " + keyCode);
        return onKeyDown(keyCode);
    }

    public boolean onKeyDown(int keyCode) {
        if(ComponentsManager.getNativeActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_FVP && keyCode == KeyMap.KEYCODE_MTKIR_MTKIR_SWAP ){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"nativeid-->NAV_NATIVE_COMP_ID_FVP");
            return true;
        }
        if (isBGMState) {
            DvrDialog pvrDialog = new DvrDialog(mContext, DvrDialog.TYPE_BGM);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyDown, timeshiftmanager:in");
            pvrDialog.show();
        }
        boolean handled = false;
        dumpStatesStack();
        synchronized (this) {
            for (int i = mStates.size() - 1; i >= 0; i--) {
                StateBase state = mStates.get(i);
                handled = state.onKeyDown(keyCode);
                if (handled) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler," + state.getType().name() + ","
                            + handled);
                    return true;
                }
            }
        }
        return handled;
    }

    /**
     * this method is used to set the visibility of component
     *
     * @param visibility
     */
    @Override
    public void setVisibility(int visibility) {
        // com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setVisibility = " + visibility);
        super.setVisibility(visibility);
        /*if (visibility == View.INVISIBLE) {
            if (StateDvrFileList.getInstance() != null
                    && StateDvrFileList.getInstance().isShowing()) {
                DvrManager.getInstance().restoreToDefault(
                        StateDvrFileList.getInstance());
            }
        }*/
    }

    /**
     * updateVisibility
     */
    public void updateVisibility() {
        dumpStatesStack();
        int value = getVisibility();
        if (View.VISIBLE == value) {
            this.getTvLogicManager().removeTwinkView();
        }
        setVisibility(value);
    }

    private int getVisibility() {
        Iterator<StateBase> states = mStates.iterator();
        StateBase state;
        StatusType type;
        while (states.hasNext()) {
            state = states.next();
            type = state.getType();
            switch (type) {
            case DVR:
            case FILELIST:
            case PLAYBACK:
                return View.VISIBLE;
            default:
                break;
            }
        };

        return View.INVISIBLE;
    }

    public void restoreAllToNormal() {
        do {
            StateBase state = mStates.lastElement();
            if (state.getType() != StatusType.NORMAL) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Name:" + state.getType().name());

                state.onRelease();
                state.free();
                mStates.remove(state);
            }
        } while (mStates.size() > 1);

        updateVisibility();
    }

    public void restoreAllAfterException() {
        if (getController() != null) {
            getController().stopRecording();
        }
    }

    public float getDiskSpeed() {
        float size = getSaveValue().readFloatValue(CoreHelper.DISK_SPEED);
        if (size == 0f) {
            size = CoreHelper.DEFAULT_DISK_SPEED;
        }
        return size;
    }


    /**
     * @param selectedDisk
     * @return unit:KB
     */
    public Long getDiskFreesize(MountPoint mp) {
        if (mp == null) {
            return 0L;
        }
        return mp.mFreeSize;
    }

    public String[] getSizeList(boolean auto, Long size) {
        return Util.covertFreeSizeToArray(auto, size);
    }

    public boolean isPipPopMode() {
        return getTvLogicManager().isPipPopMode();
    }

    public boolean isScanning() {
        return getTvLogicManager().isScanning();
    }

    /**
     * Get result by UI visible or not.
     *
     * @return
     */
    public boolean pvrIsRecording() {
        if (mDvrManager == null) {
            return false;
        }

        if (StateDvr.getInstance() != null) {
            if (StateDvr.getInstance().isRecording()) {
                return true;
            }
        }

        return false;
    }

    public void prepareSchedulePvrTask(int taskID) {
        /*
         * 1.switch source 2.set tv channel 3.check state 4.perform virtul key
         * to ap
         */
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "prepareSchedulePvrTask, " + taskID + ", " + scheduleItem);

        if (scheduleItem != null) {
            TifTimeShiftManager mTimeShiftManager = TurnkeyUiMainActivity
                    .getInstance().getmTifTimeShiftManager();
            if (mTimeShiftManager != null
                    && SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                            MenuConfigManager.TIMESHIFT_START)) {
                DvrDialog stopTshift = new DvrDialog((Activity) mContext,
                        DvrDialog.TYPE_Confirm, DvrDialog.TYPE_TSHIFT,
                        DvrDialog.TYPE_Record);
                stopTshift.setScheduleItem(scheduleItem);
                stopTshift.show();
                ScheduleHandler scheduleHandler = new ScheduleHandler(
                        stopTshift, scheduleItem);
                scheduleHandler.sendEmptyMessageDelayed(0, 60 * 1000);
            } else {
                DvrDialog conDialog = new DvrDialog((Activity) mContext,
                        DvrDialog.TYPE_Confirm, DvrDialog.TYPE_SCHEDULE,
                        DvrDialog.TYPE_Record);
                conDialog.setScheduleItem(scheduleItem);
                conDialog.show();
                ScheduleHandler scheduleHandler = new ScheduleHandler(
                        conDialog, scheduleItem);
                scheduleHandler.sendEmptyMessageDelayed(0, 60 * 1000);
            }
        }
    }

    public void startSchedulePvr() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startSchedulePvr");
        long programId =0;
        int svl = 0;
        if (DvrManager.getScheduleItem() != null) {
            TIFChannelInfo  mTIFChannelInfo =    TIFChannelManager.getInstance(DvrManager.getInstance().getContext())
                .getChannelInfoByChannelIdAndSvlId(DvrManager.getScheduleItem().getChannelId(),DvrManager.getScheduleItem().getSvlId());
            if(mTIFChannelInfo!=null){
                programId = mTIFChannelInfo.mId;
                svl = mTIFChannelInfo.mInternalProviderFlag1;
            }
        }else{
            TIFChannelInfo  mTIFChannelInfos =  TIFChannelManager.getInstance(DvrManager.getInstance().getContext()).getCurrChannelInfo();
            if(mTIFChannelInfos!=null){
                programId = mTIFChannelInfos.mId;
                svl = mTIFChannelInfos.mInternalProviderFlag1;
            }
        }
        SaveValue.getInstance(getContext()).saveValue(DvrManager.DVR_URI_ID,
            programId);
        SaveValue.getInstance(getContext()).saveValue(DvrManager.DVR_BG_TUNER_TYPE,
            CommonIntegration.getInstance().getTunerModeFromSvlId(svl));
        SaveValue.saveWorldValue(getContext(),DvrManager.DVR_URI_ID,(int)programId,false);
            if (!diskIsReady()&&scheduleItem!=null&&scheduleItem.getRecordMode()!=1) {
            showPromptInfo(PRO_DISK_NOT_READY);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "disk not ready");
            handlerScheduleData(index);
            index = -1;
            isNeedTune = true;
            scheduleItem = null;
            return;
        }
        if (DvrManager.getInstance().pvrIsRecording()) {
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d("1111+", "is recording");
                   StateDvr.getInstance().getHandler()
                           .removeMessages(StateDvr.REFRESH_TIMER);
                   getController().stopRecording();
                   return;
       }
        TifTimeShiftManager mTimeShiftManager = TurnkeyUiMainActivity
                .getInstance().getmTifTimeShiftManager();
        if (mTimeShiftManager != null
                && SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                MenuConfigManager.TIMESHIFT_START)) {
            mTimeShiftManager.stop();
            SaveValue.getInstance(mContext).setLocalMemoryValue(
                    MenuConfigManager.TIMESHIFT_START, false);
               /* SaveValue.saveWorldBooleanValue(mContext,
                        MenuConfigManager.TIMESHIFT_START, false, false);*/
            SystemClock.sleep(1000);
        }
        if (scheduleItem != null) {
            if (scheduleItem.getChannelId() > 0) {
                // will not start pvr when tuner mode not same.
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("1111+", "is ="
                        + CommonIntegration.getInstance().getTunerMode() + ","
                        + scheduleItem.getTunerType());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "scheduleItem.getTunerType() "
                                + MarketRegionInfo.getCurrentMarketRegion()
                                + "," + MarketRegionInfo.F_EU_PA);

                 if (DestroyApp.isCurActivityTkuiMainActivity()&&!CommonIntegration.getInstance().isCurrentSourceDTV()) {
                         istuneSource = true;
                         setTuneChannel(false);
                     }else  if(getController().isTargetAppRunning(mContext,"com.google.android.tvlauncher.MainActivity")){
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"is home page");
                     if (programId == -1) {
                         TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
                     } else {
                         mContext.startActivity(
                             new Intent(Intent.ACTION_VIEW)
                                 .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                 .setData(TvContract.buildChannelUri(programId)));
                     }
                         istuneSource = true;
                    }else{
                         istuneSource = false;
                     }
//                 if(!DestroyApp.isCurActivityTkuiMainActivity()&&!CommonIntegration.getInstance().isCurrentSourceDTV()){
//                    InputSourceManager.getInstance().resetDefault();
//                    }
                if (CommonIntegration.getInstance().getTunerMode() == scheduleItem
                        .getTunerType()) {
                    //setScheduleStart();
                    //        .changeCurrentInputSourceByName("DTV");
                    //mScheduleHandler.sendEmptyMessageDelayed(
                    //        MSG_SCHEDULE_CHANGE_SOURCE, 1000);
                    //}else{
                     setScheduleStart();
                    //}
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scheduleItem.getTunerType() == > error");
                    boolean notOperator = ScanContent.getDVBSAllOperatorList(mContext).size() <= 0;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "notOperator"+notOperator);
                    int relType = 0;
                    if(scheduleItem.getTunerType()==2&&!notOperator
                        ){
                        relType = 3;
                        }else if(scheduleItem.getTunerType()==3){
                        relType = 2;
                        }else{
                        relType = scheduleItem.getTunerType();
                        }
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"relType-->"+relType);                
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_USER_SRC,
                                relType);
                    MenuConfigManager.getInstance(mContext).setTunerMode(relType
                        ,false);
                    if (relType == 3) {
                        relType = 2;
                    }
                    InputSourceManager.getInstance().saveInputSourceHardwareId(relType,"main");
                    istuneSource = true;
                    setScheduleStart();
                    //mScheduleHandler.sendEmptyMessage(MSG_SCHEUDLE_CANCEL);
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "scheduleItem.getChannelId() = "
                                + scheduleItem.getChannelId());
            }
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scheduleItem == null ");
        }
    }

    private void setScheduleStart() {
       handlerScheduleData(index);
        if (StateDvrPlayback.getInstance() != null
                && StateDvrPlayback.getInstance().isRunning()) {
            StateDvrPlayback.getInstance().stopDvrFilePlay();
            SystemClock.sleep(500);
        }
        /*if (DestroyApp.isCurActivityTkuiMainActivity()&&scheduleItem.getRecordMode()!=1) {
        -            setTuneChannel();
        }*/
        mScheduleHandler.sendEmptyMessageDelayed(
                MSG_SCHEDULE_CHANGE_SOURCE, 1000);
//            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "background recording");
//            setScheduleTypeAction();
//        }
    }

    private void setTuneChannel(boolean flag) {
//        handlerScheduleData(index);
        //index = -1;
        TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                .getChannelInfoByChannelIdAndSvlId(scheduleItem.getChannelId(),scheduleItem.getSvlId());
        if(tifChannelInfo!=null){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "uri==" + TvContract.buildChannelUri(tifChannelInfo.mId));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(TvContract.buildChannelUri(tifChannelInfo.mId));
        TurnkeyUiMainActivity.getInstance().processInputUri(intent);
        saveValue.saveValue(CommonIntegration.CH_TYPE_BASE
                + CommonIntegration.getInstance().getSvl(), 0);
        }
        if(flag){
            StateNormal.setTuned(false);
        }
    }

    public void clearSchedulePvr() {
        scheduleItem = null;
        DvrManager.setScheduleItem(null);
    }

    public void setPVRStopTime(Long duration) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setPVRStopTime, " + duration);

        Long seconds = (duration);
        int durationInt = new Long(seconds).intValue();
        StateDvr.getInstance().setSchedulePVRDuration(durationInt);
    }
    public void setDuration() {
        timeduration = getDuration(scheduleItem);
    }

    public long getDurations() {
        return timeduration;
    }

    /**
     * isSuppport
     */
    public static boolean isSuppport() {
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR);
    }

    /**
     * getDeviceList
     */
    public List<MountPoint> getDeviceList() {
        return DeviceManager.getInstance().getMountPointList();
    }

    /**
     * isSuppport dual tuner
     */
    public static boolean isSuppportDualTuner() {
        return MarketRegionInfo
                .isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT);
    }

    /**
     * isMountPointExist
     */
    public boolean isMountPointExist(MountPoint mp) {
        if (mp == null) {
            return false;
        }

        List<MountPoint> mpList = getDeviceList();
        for (MountPoint mps : mpList) {
            if (mp.mMountPoint.equalsIgnoreCase(mps.mMountPoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * hasRemovableDisk
     */
    public boolean hasRemovableDisk() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "hasRemovableDisk:" + getDeviceList().size());
        return !getDeviceList().isEmpty();
    }

    /**
     * getPvrMountPoint
     */
    public MountPoint getPvrMountPoint() {
        if (!isMountPointExist(mDvrDiskMountPoint)) {
            setDefaultPvrMountPoint();
        }

        return mDvrDiskMountPoint;
    }

    /**
     * setDefaultPvrMountPoint
     */
    public boolean setDefaultPvrMountPoint() {
        List<MountPoint> list = getDeviceList();
        if (list == null) {
            return false;
        }

        if (list.isEmpty()) {
            return false;
        } else {
            setPvrMountPoint(list.get(0));
        }

        return true;
    }

    /**
     * setPvrMountPoint
     */
    public void setPvrMountPoint(MountPoint pvrDisk) {
        this.mDvrDiskMountPoint = pvrDisk;
    }

    public boolean timeShiftIsEnable() {
        return MenuConfigManager.getInstance(mContext).getDefault(
                MenuConfigManager.SETUP_SHIFTING_MODE) == 1 ? true : false;
    }

    public boolean getTTSEnable() {
        return ttUtil.isTTSEnabled();
    }

    /**
     * getState
     */
    public StateBase getState(StatusType type) {
        Iterator<StateBase> statesIter = mStates.iterator();
        while (statesIter.hasNext()) {
            StateBase state = statesIter.next();
            if (state.getType() == type) {
                return state;
            }
        };

        return null;
    }

    public boolean getBGMState(){
        return isbgm;
    }

    public void setBGMState(boolean value) {
        isBGMState = value;
    }

    public StateBase getState() {
        return mCurrentState;
    }

    /**
     * setState
     */
    @Override
    public boolean setState(StateBase state) {
        boolean result = false;

        if (state != null) {
            int index = mStates.indexOf(state);
            if (index >= 0) {
                mStates.remove(index).free();
            }

            mStates.push(state);

            this.mCurrentState = state;
            result = true;
            updateVisibility();
        }

        return result;
    }

    /**
     * removeState
     */
    public boolean removeState(StateBase state) {
        boolean result = false;

        if (state != null) {
            int index = mStates.indexOf(state);
            if (index >= 0) {
                mStates.remove(index).free();
                result = true;
            }
        }

        updateVisibility();
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "removeState== current State is  " + state
                + " and result is " + result);
        return result;
    }

    /**
     * hideStateView
     */
    public void hideStateView() {
        Iterator<StateBase> statesIter = mStates.iterator();
        do {
            statesIter.next().hideView();
        } while (statesIter.hasNext());
    }

    /**
     * showStateView
     */
    public void showStateView(StatusType type) {
        Iterator<StateBase> statesIter = mStates.iterator();
        do {
            StateBase state = statesIter.next();
            if (state.getType() == type) {
                state.showView();
            }
        } while (statesIter.hasNext());
    }

    /**
     * textToSpeakUtil
     *
     * @param str
     */
    public void speakText(String str) {
        if (mDvrManager != null && ttUtil != null) {
            ttUtil.speak(str);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mDvrManager==NULL  or   textToSpeachUtil ==NULL");
        }
    }

    /**
     * dumpStatesStack
     */
    private synchronized void dumpStatesStack() {
        String log = "\n==dumpStatesStack==\n";

        StateBase state;
        for (int i = mStates.size() - 1; i >= 0; i--) {
            state = mStates.get(i);
            log += "==" + state.getType().name() + "\n";
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, log + "==end");
    }

    @Override
    public void onEvent(DeviceManagerEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "DeviceManagerEvent: " + event.getType());
        switch (event.getType()) {
        case DeviceManagerEvent.connected:

            break;
        case DeviceManagerEvent.disconnected:

            break;
        case DeviceManagerEvent.isomountfailed:

            break;
        case DeviceManagerEvent.mounted:
            Message msgs = mDvrManager.getTopHandler().obtainMessage();
            msgs.what = USB_DEVICE_MOUNT;
            mDvrManager.getTopHandler().sendMessage(msgs);
            initDiskPath();
            break;
        case DeviceManagerEvent.umounted:
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "DeviceManagerEvent.umounted");

            Message msg = mDvrManager.getTopHandler().obtainMessage();
            msg.what = UNMOUNT_EVENT;

            Bundle bundle = new Bundle();
            bundle.putString(UNMOUNT_EVENT_MSG_KEY, event.getMountPointPath());
            msg.setData(bundle);

            mDvrManager.getTopHandler().sendMessage(msg);

            break;
        case DeviceManagerEvent.unsupported:

            break;
        default:
            break;
        }
    }

    // @Override
    public void enqPinSuccess() {
        if (scheduleItem != null) {
            toRecord();
        }
    }

    // @Override
    public void enqPinWaitInputState(boolean isPincodeShow) {
        // TODO Auto-generated method stub
    }

    public boolean tshiftIsRunning() {
        if (mDvrManager == null){
            return false;
            }
        if (StateDvr.getInstance() != null){
            if (StateDvr.getInstance().isRunning()){
                return true;
                }
            }
        if (StateDvrFileList.getInstance() != null){
            if (StateDvrFileList.getInstance().isRunning()){
                return true;
                }
            }
        return false;
    }


    public String getPVRRecordingSrc() {
        if (StateDvr.getInstance() != null) {
            return StateDvr.getInstance().getPvrSource();
        } else {
            return null;
        }
    }

    public boolean isStopDvrNotResumeLauncher() {
        return isStopDvrNotResumeLauncher;
    }

    /**
     * @param isStopDvrNotResumeLauncher
     *            the isStopDvrNotResumeLauncher to set
     */
    public void setStopDvrNotResumeLauncher(boolean isStopDvrNotResumeLauncher) {
        this.isStopDvrNotResumeLauncher = isStopDvrNotResumeLauncher;
    }

    /**
     * google pip mode state
     *
     * */
    public boolean isInPictureMode() {
        if (TurnkeyUiMainActivity.getInstance() != null) {
            boolean is = TurnkeyUiMainActivity.getInstance()
                    .isInPictureInPictureMode();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isInPictureMode = " + is);
            return is;
        }
        return false;
    }

    /**
     *
     * @param programInfo
     */
    public void startScheduleList(EPGProgramInfo programInfo) {
        boolean isCurrentSourceDTV = CommonIntegration.getInstance()
                .isCurrentSourceDTV();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceDTV=" + isCurrentSourceDTV);
        if (!isCurrentSourceDTV) {
            Toast.makeText(mContext, R.string.nav_no_support_pvr_for_tvsource,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (programInfo == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "start scheduleList failed for programInfo==null");
            return;
        }
        Long startTime = programInfo.getmStartTime() * 1000;
        if (startTime == 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "startTime is invalid value!");
            return;
        }
        MtkTvBookingBase item = new MtkTvBookingBase();
        // Long startTime = EPGTimeConvert.getInstance().getStartTime(
        // mCurrentSelectedProgramInfo);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start time in epg:" + startTime);

        MtkTvTimeFormatBase from = new MtkTvTimeFormatBase();
        MtkTvTimeFormatBase to = new MtkTvTimeFormatBase();

        from.setByUtc(startTime / 1000);
        MtkTvTimeBase time = new MtkTvTimeBase();

        time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_BRDCST_LOCAL,
                from, to);

        startTime = to.toSeconds() * 1000;

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                TAG,
                "startTime="
                        + startTime
                        + " str = "
                        + Util
                                .timeToTimeStringEx(startTime * 1000, 0));
        if (startTime != -1L) {
            item.setRecordStartTime(startTime / 1000);
        }

        // Long endTime = EPGTimeConvert.getInstance().getEndTime(
        // mCurrentSelectedProgramInfo);
        Long endTime = programInfo.getmEndTime() * 1000;
        from.setByUtc(endTime / 1000);
        time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_BRDCST_LOCAL,
                from, to);
        endTime = to.toSeconds() * 1000;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "endTime=" + endTime);
        if (endTime != -1L) {
            item.setRecordDuration(endTime / 1000 - startTime / 1000);
        }
        item.setTunerType(CommonIntegration.getInstance().getTunerMode());
        item.setRepeatMode(128);
        item.setChannelId(programInfo.getChannelId());
        item.setEventId(programInfo.getProgramId());
        item.setType(1);
        ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(
                mContext, item);
        scheduleListItemDialog.setEventType(1);
        scheduleListItemDialog.setType(0);
        scheduleListItemDialog.setEventId(programInfo.getProgramId());
        scheduleListItemDialog.show();

    }

    public boolean Contains(StateBase stateBase){
        return mStates.contains(stateBase);
    }
}
