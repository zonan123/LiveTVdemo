package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.GingaTvDialog;
import com.mediatek.wwtv.tvcenter.nav.view.MiscView;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr.CallbackHandler;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr.MyHandler;
import com.mediatek.wwtv.tvcenter.dvr.manager.Util;
import com.mediatek.wwtv.tvcenter.dvr.ui.DVRControlbar;
import com.mediatek.wwtv.tvcenter.dvr.ui.DVRTimerView;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.SomeArgs;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import java.io.File;
import com.mediatek.common.FileSystemPath;

import mediatek.sysprop.VendorProperties;
import com.mediatek.wwtv.setting.util.TVContent;


public class StateDvr extends StateBase {

    private static final String TAG = "StateDvr";
    public static StateDvr stateDvr;
    private MyHandler mHandler;
    private DVRControlbar mBigCtrlBar;
    private DVRTimerView mSmallCtrlBar;
    private TextView mBRecordTimer;
    private TextView mafterRecordTimer;
    private TextView mBTextViewDate;
    private TextView mBTextViewFileInfo;
    private String recordTimeStr = "00:00:00";
    //private String remainTimeStr = "00:00"; // Depend On Disk's free Size
    public int recordTimingLong = 30 * 60; // MAX time: 30 Minutes?
    public int schedulePvrTimeing = -1; // MAX time: 30 Minutes?
    private String recordTimingStr = getResource().getString(
            R.string.title_pvr_total_time);// "12:00:00";
    //private final static int INTERVAL_SECOND = 60;
    private final static int MAX_TIMING = 12 * 60 * 60;
    //private final static int MIN_TIMING = 60;
    private int recordTimer = 0;
    //private final static int MAX_RECORD_TIME = 100000*60*60;
    //private StringBuilder mAllTimerToStr = new StringBuilder();
    //private StringBuilder otherToStringBuilder = new StringBuilder();

    private final static int SHOW_BIG_CTRL_BAR = 1;
    private final static int SHOW_SMALL_CTRL_BAR = 2;
    private final static int Clear_All_View = 12;
    private final static int START_RECORD_TIMER = 3;
    private final static int PAUSE_RECORD_TIMER = 4;
    private final static int STOP_RECORD_TIMER = 5;
    public final static int REFRESH_TIMER = 6;
    private final static int RECORD_START = 10;
    private final static int RECORD_STOP = 11;
    private final static int RECORD_PAUSE = 13;
    private final static int DEBUGDEBUG = 111111;
    protected static final int MMP_PLAYER_ERROR = 20;
    public String isSameSource = "";
    private boolean mShowDiskAttention = true;
    private static boolean isTkActive = true;
    private boolean isChannellist = false;
    /**
     * change source when exit dvr and save files
     */
    private boolean isChangeSource = false;
    private boolean isDirection = false;
    private DvrDialog conDialog2;
    private ProgressBar progressBar;
    private String channelNumber;
    private String channelName;
    private int totalTime=0;
    private long actualTime = 0;
    private final Handler mCallbackHandler = new CallbackHandler(this);

    private StateDvr(Context context, DvrManager manager) {
        super(context, manager);
        setType(StatusType.DVR);
        getController().addEventHandler(mCallbackHandler);
    }

    @Override
    public void free(){
        getController().removeEventHandler(mCallbackHandler);
        stateDvr = null;
        if(mBigCtrlBar != null) {
            mBigCtrlBar.stopTimerTask();
        }
    }

    public synchronized static  StateDvr getStateDvr(Context mContext, DvrManager manager) {
        if (stateDvr == null) {
            manager = DvrManager.getInstance();// must keep manage in latest.
            stateDvr = new StateDvr(mContext.getApplicationContext(), manager);
        }
        return stateDvr;
    }

    public static StateDvr getInstance() {

        return stateDvr;
    }



    public Handler getHandler() {
        return mHandler;
    }

    public static boolean isTkActive() {
        return isTkActive;
    }

    public static void setTkActive(boolean isTkActive) {
        StateDvr.isTkActive = isTkActive;
    }

    public void prepareStart() {
        if (!CommonIntegration.getInstance().isCurrentSourceDTV()){
            MtkTvInputSource.getInstance().setScartAutoJump(false);
            }
        SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.PVR_START,
                true, false);
        SaveValue.getInstance(mContext).saveBooleanValue(
                MenuConfigManager.PVR_START, true);
        channelNumber = getManager().getTvLogicManager().getChannelNumStr();
        channelName = getManager().getTvLogicManager().getChannelName();
        mShowDiskAttention = true;
        mHandler = new MyHandler();
        mHandler.sendEmptyMessage(REFRESH_TIMER);
        actualTime = System.currentTimeMillis()/1000;
        clearAllWindow();
        boolean channellistd =false;
        NavBasicDialog channelListDialog = (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
        if( channelListDialog instanceof  ChannelListDialog){
            ChannelListDialog  dialog= (ChannelListDialog) channelListDialog;
            channellistd =dialog != null && dialog
                    .isShowing();
        }else if( channelListDialog instanceof  UKChannelListDialog){
            UKChannelListDialog  dialog= (UKChannelListDialog) channelListDialog;
            channellistd =dialog != null && dialog
                    .isShowing();
        }
        boolean ismenushow = ((com.android.tv.menu.MenuOptionMain) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG)).isShowing();
        if (channellistd
                || ismenushow) {
            isChannellist = true;
        }
        startPVRrecord();
        setRunning(true);
        if (!getManager().isPvrDialogShow() && !isChannellist && !ismenushow&&DestroyApp.isCurActivityTkuiMainActivity()) {
            showBigCtrlBar();
        } else {
            showCtrBar();
            isChannellist = false;
        }
    }

    @Override
    public void onResume() {
        if (isRunning()) {
            showBigCtrlBar();
        }

        clearWindow(false);

        mHandler.sendEmptyMessage(REFRESH_TIMER);
    }

    @Override
    public void onRelease() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onRelease");
    }

    /**
     * show ctr bar if you don not know witch bar you should show out. will show
     * default one
     */
    public void showCtrBar() {
        if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
            return;
        }
        if (mSmallCtrlBar != null && mSmallCtrlBar.isShowing()) {
            return;
        }
        showSmallCtrlBar();
    }

    public void showSmallCtrlBar() {
        /*
         * if (mShowDiskAttention) { getManager().showDiskAttention();
         * mShowDiskAttention = false; }
         */
        if (mSmallCtrlBar != null) {
            if (!mSmallCtrlBar.isShowing()) {
                mSmallCtrlBar.setInfo("["
                        + Util.secondToString(stateDvr.getRecordTimer()) + "]");
                mSmallCtrlBar.show();
            }
        } else {
            mSmallCtrlBar = new DVRTimerView(mContext);
            mSmallCtrlBar.getContentView()
                    .findViewById(R.id.pvr_rec_icon_small)
                    .setVisibility(View.VISIBLE);
            mSmallCtrlBar
                    .getContentView()
                    .findViewById(R.id.info)
                    .setBackgroundResource(
                            mContext.getResources().getColor(
                                    R.color.menu_main_MenuSetUpListView));
            if (stateDvr.getRecordTimer() == 0) {
                mSmallCtrlBar.setInfo("00:00:00");
            } else {
                mSmallCtrlBar.setInfo(Util.intToString(stateDvr
                        .getRecordTimer()));
            }
            mSmallCtrlBar.show();
        }
        DvrManager.getInstance().setVisibility(View.INVISIBLE);
    }

    /*
     *
     */
    public void showBigCtrlBar() {

        if (DvrManager.getInstance().isInPictureMode()) {
            showSmallCtrlBar();
            return;
        }
        if (mBigCtrlBar != null) {
            showProgramInfo();
        } else {
            initBigCtrlBar();
        }
        // if (TurnkeyUiMainActivity.getInstance().isInPictureInPictureMode()) {
        // showSmallCtrlBar();
        // return;
        // }
        mBigCtrlBar.show();
        refreshTimer();

    }

    private void initBigCtrlBar() {
        //if (!DestroyApp.isCurActivityTkuiMainActivity()) {
        //    mBigCtrlBar = new DVRControlbar((Activity) mContext,
        //            R.layout.pvr_timeshfit_pvrworking, 1L * 1000, this);
        //} else{
            mBigCtrlBar = new DVRControlbar(mContext,
                    R.layout.pvr_timeshfit_pvrworking, 10L * 1000, this);
        //    }
        mBRecordTimer = (TextView) mBigCtrlBar.getContentView().findViewById(
                R.id.pvr_working_rec_time);
        mafterRecordTimer = (TextView) mBigCtrlBar.getContentView()
                .findViewById(R.id.pvr_working_rec_time_other);
        mBTextViewDate = (TextView) mBigCtrlBar.getContentView().findViewById(
                R.id.pvr_working_currenttime);

        mBTextViewFileInfo = (TextView) mBigCtrlBar.getContentView()
                .findViewById(R.id.info);
        mBTextViewFileInfo.setSelected(true);
        progressBar = (ProgressBar) mBigCtrlBar.getContentView().findViewById(
                R.id.progressBar);
        mBigCtrlBar.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (isRunning()) {
                    mHandler.sendEmptyMessage(SHOW_SMALL_CTRL_BAR);
                }

            }
        });

        showProgramInfo();
        //if (!DestroyApp.isCurActivityTkuiMainActivity()){
        //    mHandler.sendEmptyMessageDelayed(SHOW_SMALL_CTRL_BAR,500);
        //}
    }

    private void showProgramInfo() {
        if (CommonIntegration.getInstance().isCurrentSourceATV()) {
            StringBuilder info = new StringBuilder();
            info.append("1");
            info.append("      ");
            info.append(getManager().getTvLogicManager().getChannelNumStr());
            // mPVRFile
            mBTextViewFileInfo.setText(info.toString());
        } else if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
            StringBuilder info = new StringBuilder();
            info.append(channelNumber);
            info.append("      ");
            if (channelName != null) {
                if (channelName.length() >= 30) {
                    channelName = channelName.substring(0, 27) + "...";
                }
                info.append(channelName);
            }
            info.append("      ");
            String pString = BannerImplement.getInstanceNavBannerImplement(
                    mContext).getProgramTitle();
            if (pString.isEmpty()) {
                pString = ("(No program title.)");
            }
            info.append(pString);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("showProgramInfo", "showProgramInfo:"
                    + getManager().getTvLogicManager().getChannelName());
            // mPVRFile
            mBTextViewFileInfo.setText(info.toString());
        } else {
            StringBuilder info = new StringBuilder();
            info.append(CommonIntegration.getInstance().getCurrentSource());
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("showProgramInfo", "showProgramInfo:"
                    + CommonIntegration.getInstance().getCurrentSource());
            // mPVRFile
            mBTextViewFileInfo.setText(info.toString());
        }
    }

    public void dissmissBigCtrlBar() {
        if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
            mBigCtrlBar.dismiss();
        }
    }

    public void recoveryView() {
        if (mBigCtrlBar != null) {
            mBigCtrlBar = null;
        }
    }

    /*
     * bigger=true? hidden bigger ctrl bar:hidden small ctrl bar.
     */
    public void clearWindow(boolean bigger) {
        if (bigger) {
            if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
                mBigCtrlBar.dismiss();
            }
        } else {
            if(mHandler!=null) {
                mHandler.removeMessages(SHOW_SMALL_CTRL_BAR);
            }
            if (mSmallCtrlBar != null && mSmallCtrlBar.isShowing()) {
                mSmallCtrlBar.dismiss();
            }
        }
    }

    /*
     *
     */
    public void clearAllWindow() {
        clearWindow(true);
        clearWindow(false);
        resetAllParams();
    }

    private void resetAllParams() {
        recordTimeStr = "00:00:00";
        //remainTimeStr = "00:00"; // Depend On Disk's free Size

        recordTimingLong = 30 * 60; // MAX time: 30 Minutes?
        schedulePvrTimeing = -1;

        recordTimingStr = getResource()
                .getString(R.string.title_pvr_total_time);// "12:00:00";
        isSameSource = "";
        recordTimer = 0;
    }

    public void setSchedulePVRDuration(int duration) {
        //if (duration >= MAX_TIMING) {
        //    schedulePvrTimeing = MAX_TIMING;
        //} else{
            schedulePvrTimeing = duration;
            totalTime=0;
        //    }
    }

    /**
     *
     */
    private void refreshTimer() {
        // get time from callback message ,then refresh it on UI
        // usually delayed for seconds.
        StringBuilder mAllTimerToStr = new StringBuilder();
        StringBuilder otherToStringBuilder = new StringBuilder();
        // mAllTimerToStr.append("[");
        recordTimeStr = Util.intToString(getRecordTimer());//
        if (schedulePvrTimeing != -1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                    TAG,
                    "schedulePvrTimeing==" + schedulePvrTimeing
                            + "Util.secondToString(recordTimingLong)=="
                            + Util.intToString(recordTimingLong));
            recordTimingLong = schedulePvrTimeing;
            setRecordTimingStr(Util.intToString(recordTimingLong));
            schedulePvrTimeing = -1;
        }
        if (mSmallCtrlBar != null && mSmallCtrlBar.isShowing()) {
            mAllTimerToStr.append(recordTimeStr);
            // mAllTimerToStr.append("]");
            mSmallCtrlBar.setInfo(mAllTimerToStr.toString());
        } else if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
            //remainTimeStr = countRemainSizeTime();
            mAllTimerToStr.append(recordTimeStr);
            /*otherToStringBuilder.append("/");
            otherToStringBuilder.append(remainTimeStr);
            otherToStringBuilder.append("/");
            otherToStringBuilder.append(getRecordTimingStr());*/
            // otherToStringBuilder.append("]");
            mBRecordTimer.setText(mAllTimerToStr);
            mafterRecordTimer.setText(otherToStringBuilder);
            mBTextViewDate.setText(Util.formatCurrentTime());
        }

        if(progressBar!=null){
            updateProgressStatus();
            }
        rollBackUIState();
    }

    private void updateProgressStatus() {
        int currentTime = Util.strToInt(recordTimeStr);
        int totalTime = Util.strToInt(getRecordTimingStr());
        if (totalTime != 0) {
            long pr = 1000000L * currentTime / totalTime;
            int pro = (int) Math.floor((double) pr);
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "pro=" + pro);
            progressBar.setProgress(pro);
        } else {
            progressBar.setProgress(0);
        }

    }

    /**
     * Double check UI items.
     */
    private void rollBackUIState() {
        if (!isRunning() || !isTkActive) {
            return;
        }
        try {
            if (!mBigCtrlBar.isShowing() && !mSmallCtrlBar.isShowing()) {
                showBigCtrlBar();
            }
        } catch (Exception e) {
            Util.showELog(e.toString());
        }
        if (mBigCtrlBar != null && mBigCtrlBar.isShowing()
                && mSmallCtrlBar != null && mSmallCtrlBar.isShowing()) {
            mSmallCtrlBar.dismiss();
        }
    }

    public boolean startPVRrecord() {
        if (mShowDiskAttention && !getManager().isPvrDialogShow()
                && !isChannellist&&DestroyApp.isCurActivityTkuiMainActivity()) {
            // getManager().showDiskAttention();
            getManager().showPromptInfo(DvrManager.DISK_ATTENTION);
            mShowDiskAttention = false;
        }
        totalTime=0;
        isSameSource = CommonIntegration.getInstance().getCurrentSource();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e("isSameSource", "isSameSource:" + isSameSource);
        return false;
    }

/*
    private String countRemainSizeTime() {
        Long diskFreeSize = MtkTvRecordBase.getStorageFreeSize();
        int remainTime = (int) (diskFreeSize / (1024 * 1024)
                / getManager().getDiskSpeed() + 0.5f); // default:6.0
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "short:" + remainTime);
        Long fileSize = MtkTvRecord.getRecordingFilesize();
        int duration = stateDvr.getRecordTimer();
        if (duration > 5 && fileSize > 10 * 1024 * 1024) {
            remainTime = (int) (diskFreeSize / (fileSize / duration));
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "short:" + remainTime);
        }
        return Util.secondToString(remainTime);
    }
*/
    private boolean checkTimer() {

        int currentTime = Util.strToInt(recordTimeStr);
        int recordTotalTime = Util.strToInt(getRecordTimingStr());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentTime=" + currentTime + ",totalTime=" + totalTime
                + ",getRecordTimingStr()=" + getRecordTimingStr());

        int actualTime =totalTime;
        if(actualTime>=recordTotalTime){
           // if(DvrManager.getInstance().getBGMState()){
                SaveValue.saveWorldValue(mContext,DvrManager.BGM_PVR_LOSS,
                        actualTime-currentTime,true);
             //   }
            return true;
            }
         return currentTime >= recordTotalTime;
    }

/*
    private void addRecordTiming() {
        recordTimingLong = INTERVAL_SECOND + recordTimingLong;

        if (recordTimingLong > MAX_TIMING) {
            recordTimingLong = MAX_TIMING;
        }
        setRecordTimingStr(Util.secondToString(recordTimingLong));
        refreshTimer();
    }

    private void decreaseRecordTiming() {
        recordTimingLong = recordTimingLong - INTERVAL_SECOND;

        if (recordTimingLong < MIN_TIMING || recordTimingLong < recordTimer) {
            recordTimingLong = Math.max(MIN_TIMING, (recordTimer
                    / INTERVAL_SECOND + 1)
                    * INTERVAL_SECOND);
        }
        setRecordTimingStr(Util.secondToString(recordTimingLong));
        refreshTimer();
    }

    private void decreaseHourTiming() {
        recordTimingLong = recordTimingLong - INTERVAL_SECOND * 60;

        if (recordTimingLong < MIN_TIMING || recordTimingLong < recordTimer) {
            recordTimingLong = Math.max(MIN_TIMING, (recordTimer
                    / INTERVAL_SECOND + 1)
                    * INTERVAL_SECOND);
        }
        setRecordTimingStr(Util.secondToString(recordTimingLong));
        refreshTimer();
    }

    private void addHourTiming() {
        recordTimingLong = INTERVAL_SECOND * 60 + recordTimingLong;

        if (recordTimingLong > MAX_TIMING) {
            recordTimingLong = MAX_TIMING;
        }
        setRecordTimingStr(Util.secondToString(recordTimingLong));
        refreshTimer();
    }
*/
    public boolean isRecording() {
        boolean running = false;
        if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
            running = true;
        }
        if (mSmallCtrlBar != null && mSmallCtrlBar.isShowing()) {
            running = true;
        }
        running = running || isRunning();
        return running;
    }

    public boolean isBigCtrlBarShow() {
        boolean running = false;
        if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
            running = true;
        }
        return running;
    }

    public boolean isSmallCtrlBarShow() {
        boolean running = false;
        if (mSmallCtrlBar != null && mSmallCtrlBar.isShowing()) {
            running = true;
        }
        return running;
    }

    public Handler getStatePVRHandler() {
        return mHandler;
    }

    public String getPvrSource() {
        return isSameSource;
    }

    public int getRecordTimer() {
        return recordTimer;
    }

    public void setRecordTimer(int recordTimer) {
        this.recordTimer = recordTimer;
    }

    /**
     * @return the recordTimingStr
     */
    public String getRecordTimingStr() {
        //com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTraceEx();
        return recordTimingStr;
    }

    /**
     * @param recordTimingStr
     *            the recordTimingStr to set
     */
    public void setRecordTimingStr(String recordTimingStr) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "recordTimingStr==" + recordTimingStr);
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTraceEx();
        this.recordTimingStr = recordTimingStr;
    }

    public void stopDvrRecord(int messageType) {

        Intent intent = new Intent("com.mediatek.pvr.file");
        mContext.sendBroadcast(intent);
        if (isRunning()) {
            FavoriteListDialog favlist = (FavoriteListDialog) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasicMisc.NAV_COMP_ID_FAV_LIST);
            if (favlist != null && favlist.isShowing()) {
                favlist.dismiss();
            }
            getManager().getTvLogicManager().removeChannelList(mContext);
            if (StateDvrFileList.getInstance() != null
                    && StateDvrFileList.getInstance().isShowing()) {
                StateDvrFileList.getInstance().dissmiss();
            }
            // DvrManager.getInstance().showRecordFinish(messageType);


        setRunning(false);
        clearAllWindow();
        isSameSource = "";
        mHandler.sendEmptyMessage(Clear_All_View);
        }
        if (messageType == DvrConstant.RECORD_FINISH_AND_SAVE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getManager().showPromptInfo(DvrManager.PRO_PVR_STOP);
                }
            }, 500);
        }

        // getController().stopRecording();
        MtkTvInputSource.getInstance().setScartAutoJump(true);
        SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.PVR_START,
                false, false);
        SaveValue.getInstance(mContext).saveBooleanValue(
                MenuConfigManager.PVR_START, false);
        DvrManager.getInstance().restoreToDefault(stateDvr);
        free();
    }

    /**
     * hide dvr dialog with EPG called out.
     */
    public void hideDvrdialog() {
        if (DvrManager.getInstance() != null && conDialog2 != null
                && conDialog2.isShowing()) {
            conDialog2.dismiss();
        }
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_BIG_CTRL_BAR:
                if (stateDvr.isRunning()) {
                    stateDvr.clearWindow(false);
                    stateDvr.showBigCtrlBar();
                }
                break;
            case SHOW_SMALL_CTRL_BAR:
                if (stateDvr.isRunning()) {
                    stateDvr.showSmallCtrlBar();
                    stateDvr.clearWindow(true);
                }
                break;
            case START_RECORD_TIMER:
                break;
            case PAUSE_RECORD_TIMER:
                break;
            case STOP_RECORD_TIMER:
                break;
            case REFRESH_TIMER:
                removeMessages(REFRESH_TIMER);
                // recordTimer++;
                if(stateDvr==null){
                    break;
                }
                if (stateDvr.checkTimer()) {
                    // stateDvr.stopDvrRecord(DvrConstant.RECORD_FINISH_AND_SAVE);
                    getController().stopRecording();
                    Intent intent = new Intent("com.mtk.state.file");
                    mContext.sendBroadcast(intent);
                    return;
                }
                getController().getRecordDuration();
                if (VendorProperties.mtk_auto_test().orElse(0) == 1) {
                    if (recordTimer >= 15) {
                        try {
                            File file = new File(FileSystemPath.LINUX_TMP_PATH
                                    + "/autotest/pvr_pass");
                            file.createNewFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                stateDvr.refreshTimer();
                // if (stateDvr.isRunning()) {
                // sendEmptyMessageDelayed(REFRESH_TIMER, 1 * 1000);
                // }
                break;
            case MMP_PLAYER_ERROR:
                break;
            case RECORD_START:
                stateDvr.setRunning(true);
                break;
            case DEBUGDEBUG:
                break;
            case RECORD_STOP:
                stateDvr.setRunning(false);
                stateDvr.getController().stopRecording();
                break;
            case RECORD_PAUSE:
                stateDvr.setRunning(false);
                break;
            case DvrConstant.Dissmiss_PVR_BigCtrlbar:
                if (stateDvr!=null&&!isDirection) {
                    stateDvr.dissmissBigCtrlBar();
                }
                isDirection = false;
                break;
            case Clear_All_View:
                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public boolean onKeyDown(int keycode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keycode==" + keycode);
        if(!stateDvr.isRunning()){
            return false;
            }
        switch (keycode) {
        case KeyMap.KEYCODE_MENU:
            getManager().uiManager.hiddenAllViews();
            showSmallCtrlBar();
            clearWindow(true);
            break;
        case KeyMap.KEYCODE_MTKIR_SUBTITLE:
        case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
            getManager().uiManager.hiddenAllViews();
            showSmallCtrlBar();
            clearWindow(true);
            break;
        case KeyMap.KEYCODE_MTKIR_STOP:
            getManager().speakText("stop record");
            if (mHandler != null) {
                mHandler.removeMessages(REFRESH_TIMER);
            }
            getController().stopRecording();
            // stopDvrRecord(PvrConstant.RECORD_FINISH_AND_SAVE);
            return true;
            // Fix CR: DTV00583717
        /*case KeyMap.KEYCODE_DPAD_UP:
            if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
                addHourTiming();
                showBigCtrlBar();
                isDirection = true;
                mHandler.removeMessages(DvrConstant.Dissmiss_PVR_BigCtrlbar);
                mHandler.sendEmptyMessageDelayed(
                        DvrConstant.Dissmiss_PVR_BigCtrlbar, 10 * 1000);
            }
            return true;
        case KeyMap.KEYCODE_DPAD_DOWN:
            if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
                decreaseHourTiming();
                showBigCtrlBar();
                isDirection = true;
                mHandler.removeMessages(DvrConstant.Dissmiss_PVR_BigCtrlbar);
                mHandler.sendEmptyMessageDelayed(
                        DvrConstant.Dissmiss_PVR_BigCtrlbar, 10 * 1000);
            }
            return true;
        case KeyMap.KEYCODE_DPAD_LEFT:
            if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
                decreaseRecordTiming();
                showBigCtrlBar();
                isDirection = true;
                mHandler.removeMessages(DvrConstant.Dissmiss_PVR_BigCtrlbar);
                mHandler.sendEmptyMessageDelayed(
                        DvrConstant.Dissmiss_PVR_BigCtrlbar, 10 * 1000);
            }
            return true;
        case KeyMap.KEYCODE_DPAD_RIGHT:
            if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
                addRecordTiming();
                showBigCtrlBar();
                isDirection = true;
                mHandler.removeMessages(DvrConstant.Dissmiss_PVR_BigCtrlbar);
                mHandler.sendEmptyMessageDelayed(
                        DvrConstant.Dissmiss_PVR_BigCtrlbar, 10 * 1000);
            }
            return true;*/
        case KeyMap.KEYCODE_MTKIR_RECORD:
            GingaTvDialog gingaTvDialog = (GingaTvDialog) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasic.NAV_COMP_ID_GINGA_TV);
            if (null != gingaTvDialog && gingaTvDialog.isVisible()) {
                gingaTvDialog.dismiss();
            }
            if (ScheduleListDialog.getDialog() != null
                    && ScheduleListDialog.getDialog().isShowing()) {
                // CR 959741 ,follow linux ,dismiss schedule dialog when confirm
                // dialog show.
                ScheduleListDialog.getDialog().dismiss();
            }
            DvrManager manager = getManager();
            if (isRunning()) {
                MiscView miscView = (MiscView) ComponentsManager.getInstance()
                        .getComponentById(NavBasic.NAV_COMP_ID_MISC);
                if (miscView != null && miscView.isVisible()) {
                    miscView.setVisibility(View.GONE);
                }
                if(mHandler!=null){
                    mHandler.sendEmptyMessage(SHOW_BIG_CTRL_BAR);
                    }
                // manager.showPvrIsRecording();
                manager.showPromptInfo(DvrManager.PRO_RECORDING);
            }
            return true;
        case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP: // play list
            if (mBigCtrlBar != null && mBigCtrlBar.isShowing()) {
                return true;
            } else {
                PwdDialog mPwdDialog = (PwdDialog) ComponentsManager
                        .getInstance().getComponentById(
                                NavBasic.NAV_COMP_ID_PWD_DLG);
                if (mPwdDialog != null && mPwdDialog.isVisible()) {
                    mPwdDialog.dismiss();
                }
                if (!TurnkeyUiMainActivity.getInstance().isUKCountry) {
                    BannerView baberView = (BannerView) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasicMisc.NAV_COMP_ID_BANNER);
                    if (baberView.isVisible()) {
                        baberView.setVisibility(View.GONE);
                    }
                } else {
                    UkBannerView baberView = (UkBannerView) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasicMisc.NAV_COMP_ID_BANNER);
                    if (baberView.isVisible()) {
                        baberView.setVisibility(View.GONE);
                    }
                }
                // getManager().getTvLogicManager().reSetZoomValues(mContext);
                if (getManager().setState(
                        StateDvrFileList.getInstance(DvrManager.getInstance()))) {
                    StateDvrFileList.getInstance().showPVRlist();
                }
            }
            return true;
        case KeyMap.KEYCODE_0:
        case KeyMap.KEYCODE_1:
        case KeyMap.KEYCODE_2:
        case KeyMap.KEYCODE_3:
        case KeyMap.KEYCODE_4:
        case KeyMap.KEYCODE_5:
        case KeyMap.KEYCODE_6:
        case KeyMap.KEYCODE_7:
        case KeyMap.KEYCODE_8:
        case KeyMap.KEYCODE_9:
            return false;
        case KeyMap.KEYCODE_MTKIR_PIPPOP:
            return false;
        case KeyMap.KEYCODE_MTKIR_PRECH:
        case KeyMap.KEYCODE_MTKIR_CHUP:
        case KeyMap.KEYCODE_MTKIR_CHDN:
        case KeyMap.KEYCODE_DPAD_DOWN:
        case KeyMap.KEYCODE_DPAD_UP:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(SHOW_SMALL_CTRL_BAR);
            }
            String srctype = getController().getSrcType();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "srctype = " + srctype);
            TIFChannelInfo channelInfo = null;
            if (keycode == KeyMap.KEYCODE_MTKIR_CHUP||keycode ==  KeyMap.KEYCODE_DPAD_UP) {
                channelInfo = TIFChannelManager.getInstance(mContext)
                        .getUpAndDownChannel(true);
            } else if (keycode == KeyMap.KEYCODE_MTKIR_CHDN|| keycode == KeyMap.KEYCODE_DPAD_DOWN) {
                channelInfo = TIFChannelManager.getInstance(mContext)
                        .getUpAndDownChannel(false);
            } else if (keycode == KeyMap.KEYCODE_MTKIR_PRECH) {
                channelInfo = TIFChannelManager.getInstance(mContext)
                        .getPreChannelInfo();
            }
            if (!"TV".equals(srctype)
                    && !InputSourceManager.getInstance()
                            .getConflictSourceList().contains(srctype)
                    && !"1".equals(channelInfo.mType)) {
                break;
            } else {
                if (TIFChannelManager.getInstance(mContext).hasOneChannel()
                        || !InputSourceManager.getInstance().isCurrentTvSource(
                                InputSourceManager.MAIN)||CommonIntegration.getInstance().is3rdTVSource()) {
                    break;
                } else {
                    FavoriteListDialog fav = ((FavoriteListDialog) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasicMisc.NAV_COMP_ID_FAV_LIST));
                    if (fav != null && fav.isShowing()) {
                        fav.dismiss();
                    }
                    setDialogType(1, keycode);
                    return true;
                }
            }
        case KeyMap.KEYCODE_MTKIR_GUIDE:
            boolean success = true;
            if (CommonIntegration.getInstance().isCurrentSourceTv()) {
                if (CommonIntegration.getInstance().isCurrentSourceBlocked()) {
                    if (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_US) {
                        success = false;
                    }
                } else {
                    if (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_US
                            && CommonIntegration.getInstance()
                                    .getAllEPGChannelLength() <= 0) {
                        success = false;
                    }
                }
            } else {
                success = false;
            }
            if (success) {
                setDialogType(2, keycode);
            }
            return true;
        case KeyMap.KEYCODE_MTKIR_RED:
            return true;
        case KeyMap.KEYCODE_MTKIR_EJECT:

            return false;
        case KeyMap.KEYCODE_DPAD_CENTER:
        case KeyMap.KEYCODE_MTKIR_TIMER:
        case KeyMap.KEYCODE_MTKIR_SLEEP:
        case KeyMap.KEYCODE_MTKIR_ZOOM:
        case KeyMap.KEYCODE_MTKIR_PEFFECT:
        case KeyMap.KEYCODE_MTKIR_SEFFECT:
        case KeyMap.KEYCODE_MTKIR_ASPECT:
        case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(SHOW_SMALL_CTRL_BAR);
            }
            return false;
        case KeyMap.KEYCODE_MTKIR_GREEN:
            return true;
        case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
            return true;
        case KeyMap.KEYCODE_MTKIR_REWIND:
        case KeyMap.KEYCODE_VOLUME_UP:
        case KeyMap.KEYCODE_VOLUME_DOWN:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(SHOW_SMALL_CTRL_BAR);
            }
            return false;
        case KeyMap.KEYCODE_BACK:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(SHOW_SMALL_CTRL_BAR);
            }
            return false;
        case KeyMap.KEYCODE_MTKIR_FREEZE:
        case KeyMap.KEYCODE_MTKIR_PAUSE:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(SHOW_SMALL_CTRL_BAR);
            }
            if (DvrManager.getInstance().timeShiftIsEnable()) {
                // DvrManager.getInstance().showFeatureNotAvaiable1();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mManager.showPromptInfo(DvrManager.PRO_PVR_RUNNING);
                    }
                }, 500);
                return true;
            }
            return false;
        default:
            break;
        }
        return false;
    }

    public void setDialogType(int type, int keycode) {
        ComponentsManager.getInstance().hideAllComponents();
        SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        if (simpleDialog!=null&&simpleDialog.isShowing()) {
            simpleDialog.dismiss();
        }
        if(simpleDialog != null ) {
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
            simpleDialog.setCancelText(R.string.pvr_confirm_no);
            simpleDialog
                    .setOnConfirmClickListener(new RegistOnDvrDialog(), keycode);
            simpleDialog.setOnCancelClickListener(new RegistOnDvrDialog(), keycode);
            if (type == 1) {
                simpleDialog.setContent(R.string.dvr_dialog_message_record_channel);
            } else {
                simpleDialog.setContent(R.string.dvr_dialog_message_record_epg);
            }
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    simpleDialog.show();
                }
            }, 100);
        }
    }

    /**
     * @return the isChangeSource
     */
    public boolean isChangeSource() {
        return isChangeSource;
    }

    /**
     * @param isChangeSource
     *            the isChangeSource to set
     */
    public void setChangeSource(boolean isChangeSource) {
        this.isChangeSource = isChangeSource;
    }

    static class CallbackHandler extends Handler {

        StateDvr stateDvr;

        CallbackHandler(StateDvr stateDvr) {
            this.stateDvr = stateDvr; // reserve.
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "callBack = " + msg.what);
                int callBack = msg.what;
                SomeArgs args = (SomeArgs) msg.obj;

                switch (callBack) {
                case DvrConstant.MSG_CALLBACK_CONNECT_FAIL:

                    break;
                case DvrConstant.MSG_CALLBACK_DISCONNECT:

                    break;
                case DvrConstant.MSG_CALLBACK_TUNED:

                    break;
                case DvrConstant.MSG_CALLBACK_RECORD_STOPPED:
                    StateNormal.setTuned(false);
                    StateNormal.setTvNotShow(true);
                    // stop will release client
                    if (stateDvr.getHandler() != null) {
                        stateDvr.getHandler().removeMessages(REFRESH_TIMER);
                    }

                    stateDvr.showToast();
                    DvrManager.getInstance().getController().dvrRelease();
                    stateDvr.stopDvrRecord(DvrConstant.RECORD_FINISH_AND_SAVE);
                    if (DvrManager.getScheduleItem() != null) {
                        DvrManager.getInstance().startSchedulePvr();
                    }
                    break;
                case DvrConstant.MSG_CALLBACK_ERROR:
                    // DvrManager.getInstance().showPromptInfo(getErrorId(errorid));
                    //DvrManager.getInstance().restoreToDefault(stateDvr);
                    //DvrManager.getInstance().restoreToDefault();
                    boolean issingalloss =args.argi2== MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_RECORDING_TUNED_VALUE_FAILED;
                    if(stateDvr.isRecording()&&issingalloss){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"is running singal loss");
                    }else{
                        stateDvr.stopDvrRecord(-1);
                    }
                    break;
                case DvrConstant.MSG_CALLBACK_RECORD_EVENT:
                    String eventType = (String) args.arg2;
                    Bundle bundle = (Bundle) args.arg3;
                    if ("session_event_dvr_record_duration".equals(eventType)) {
                        int recordingTime = bundle
                                .getInt("session_event_dvr_record_duration_value");
                        stateDvr.setRecordTimer(recordingTime);
                        // stateDvr.recordTimeStr =
                        // Util.secondToString(stateDvr.getRecordTimer());//
                        stateDvr.refreshTimer();
                        stateDvr.totalTime=(int)(System.currentTimeMillis()/1000 -stateDvr.actualTime);//stateDvr.totalTime+1;
                        if (stateDvr.checkTimer()) {
                            // stateDvr.stopDvrRecord(DvrConstant.RECORD_FINISH_AND_SAVE);
                            stateDvr.getController().stopRecording();
                            Intent intent = new Intent("com.mtk.state.file");
                            stateDvr.mContext.sendBroadcast(intent);
                            return;
                        }
                        if(stateDvr.mHandler!=null){
                            stateDvr.mHandler.removeMessages(REFRESH_TIMER);
                            stateDvr.mHandler.sendEmptyMessageDelayed(
                                REFRESH_TIMER, 1000);
                            }
                        // }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                TAG,
                                "callBack recordTimer = "
                                        + stateDvr.getRecordTimer());
                    }
                    break;
                case DvrConstant.MSG_CALLBACK_UPDATE_DURATION:
                    int remaintotalTime = args.argi2;
                    stateDvr.recordTimingLong = remaintotalTime;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "callBack remaintotalTime = " + stateDvr.getRecordTimingStr());
                    stateDvr.setRecordTimingStr(Util.secondToString(stateDvr.recordTimingLong));
                    break;
                default:
                    break;
                }
            }
        };
    }

    private void showToast(){
        if(TVContent.getInstance(mContext).isNordicCountry()&&getController().isScreenOn(mContext)){
            int errorDuration = SaveValue.readWorldIntValue(mContext,DvrManager.BGM_PVR_LOSS);
            String content = SaveValue.readWorldStringValue(mContext,DvrManager.BGM_PVR_FAIL_STR);
            String nameString =mContext.getString(R.string.bgm_fail_record_before, content);
            String durationLossString = mContext.getString(R.string.bgm_loss_time_pvr, content, errorDuration);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"type=="+nameString+","+durationLossString);
            if(errorDuration>3){
                Toast.makeText(mContext, durationLossString,Toast.LENGTH_LONG).show();
                SaveValue.saveWorldValue(mContext,DvrManager.BGM_PVR_LOSS,
                        0,true);
            }
        }

    }

}
