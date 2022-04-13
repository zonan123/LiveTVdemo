/**
 * @Description: TODO()
 */

package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
//import android.drm.DrmStore.Playback;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
import android.net.Uri;
//import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

//import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
//import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

//import android.view.KeyEvent;
//import android.view.LayoutInflater;
import android.os.SystemClock;
import android.view.View;
//import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ListView;
//import android.widget.ProgressBar;

import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
//import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
//import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
//import com.mediatek.wwtv.tvcenter.dvr.manager.Core;
//import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
//import com.mediatek.wwtv.tvcenter.dvr.manager.Util;
//import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrFilelist;
//import java.io.File;
//import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import android.widget.Toast;
import com.mediatek.twoworlds.tv.model.MtkTvPvrBrowserItemBase;
import com.mediatek.twoworlds.tv.MtkTvPvrBrowserBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;

/**
 *
 */
public class StateDvrFileList extends StateBase implements OnItemClickListener {// NOPMD

    private static final String TAG = "StateFileListDvr";
    private StateFileReceive sfReceive = null;
    private final MyHandler mHandler;
    private static StateDvrFileList mStateSelf;
    private DvrFilelist mFileListWindow;
    private static List<DVRFiles> mPVRFileList;

    private final static int DELETE_FILE_ = 110;
//    private final static int STOP_PLAYING = 111;
    private final static int HIDE_BANNER = 112;

//    private final static int INIT_PLAYER = 1;
//    private final static int START_PLAYER = 2;
//    private final static int STOP_PLAYER = 3;
//    private final static int PROGRESS_CHANGED = 11;
//    private static final int MSG_GET_CUR_POS = 17;
//    private final static int Show_Progress = 18;

    public final static int AUTO_DISMISS_FILE_LIST = 21;

    public final static int DELAY_RELEASE_STATE = 0xa001;
    public final static int UPDATE_SPEED = 0xa002;
    public final static int RESTORE_TO_NORMAL = 0xa003;
    public final static int SET_PVR_FILE = 0xa004;
    public final static int SHOW_PVR_MSG = 0xa005;

    private static boolean getListFlag = false;
    private boolean flag = false;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DELETE_FILE_:
                    removeMessages(DELETE_FILE_);
                    mStateSelf.delete();
                    break;

                case RESTORE_TO_NORMAL:
                    mStateSelf.getManager().restoreAllToNormal();
                    break;
                case SET_PVR_FILE:
                    getListFlag = false;
                    if (mFileListWindow != null&&mFileListWindow.isShowing()) {
                        mFileListWindow.setmFileList(mPVRFileList);
                        mFileListWindow.initList();
                    }
                    break;
                case HIDE_BANNER:
                    if(!TurnkeyUiMainActivity.getInstance().isUKCountry){
                       BannerView bannerView = ((BannerView) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasicMisc.NAV_COMP_ID_BANNER));
                       bannerView.setVisibility(View.GONE);
                    }else{
                        UkBannerView bannerView = ((UkBannerView) ComponentsManager
                                .getInstance().getComponentById(
                                        NavBasicMisc.NAV_COMP_ID_BANNER));
                           bannerView.setVisibility(View.GONE);
                    }
                    break;
                case AUTO_DISMISS_FILE_LIST:
                    if (mFileListWindow != null && mFileListWindow.isShowing()) {
                        // mFileListWindow.dismiss();
                        DvrManager.getInstance().restoreToDefault(StatusType.FILELIST);
                    }
                    break;
                case SHOW_PVR_MSG:
                    showUIMsg(msg.arg1,msg.arg2==1);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void updateRecordOrPlayFileParams() {
         com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "updateRecordOrPlayFileParams");
    }

    private void updateRecordOrPlayFileParams2() {

        mPVRFileList.clear();
        mPVRFileList = new ArrayList<DVRFiles>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!flag) {
                    flag = true;
                    List<DVRFiles> files = getManager().getController()
                            .getPvrFiles();
                    if (files != null) {
                        mPVRFileList.addAll(files);
                        updateRecordOrPlayFileParams();
                        mHandler.sendEmptyMessage(SET_PVR_FILE);
                        flag = false;
                    } else {
                        flag = false;
                        mHandler.sendEmptyMessage(SET_PVR_FILE);
                    }
                }
            }
        }).start();
    }

    public StateDvrFileList(Context mContext, DvrManager manager) {
        super(mContext, manager);
        setType(StatusType.FILELIST);
        // showPVRlist();
        this.mContext = mContext;
        //mStateSelf = this;
        mHandler = new MyHandler();

    }

    @Override
    public void free(){
        unRegister();
        synchronized (StateDvrFileList.class) {
            mStateSelf = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public void initControlView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "initcontrolview");
    }

    public void recoveryView() {
        if (mFileListWindow != null) {
            mFileListWindow = null;
        }
    }

    public void showPVRlist() {
        try {
            TifTimeshiftView tifTimeshiftView = (TifTimeshiftView) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
            if (tifTimeshiftView != null){
                tifTimeshiftView.startTimeout(0);
                }
            getManager().getTvLogicManager().removeChannelList(mContext);
            initPVRlist();
            if (mFileListWindow != null) {
                ZoomTipView mZoomTip = ((ZoomTipView) ComponentsManager
                        .getInstance().getComponentById(
                                NavBasic.NAV_COMP_ID_ZOOM_PAN));
                if (mZoomTip != null) {
                    mZoomTip.setVisibility(View.GONE);
                }
                SundryShowTextView stxtView = (SundryShowTextView) ComponentsManager
                        .getInstance().getComponentById(
                                NavBasic.NAV_COMP_ID_SUNDRY);
                if (stxtView != null) {
                    stxtView.setVisibility(View.GONE);
                }
                mHandler.sendEmptyMessageDelayed(HIDE_BANNER, 500);

                if(!TurnkeyUiMainActivity.getInstance().isFinishing()){
                    sfReceive = new StateFileReceive();
                    IntentFilter infiFilter = new IntentFilter("com.mtk.state.file");
                    this.mContext.registerReceiver(sfReceive, infiFilter);
                    mFileListWindow.show();
                    }else{
                    mManager.restoreToDefault(mStateSelf);
                }

            }
            if (!getListFlag) {
                getListFlag = true;
                mPVRFileList = new ArrayList<DVRFiles>();
                mPVRFileList.clear();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<DVRFiles> files = getManager().getController()
                                .getPvrFiles();
                        if (files != null) {
                            mPVRFileList.addAll(files);
                            updateRecordOrPlayFileParams();
                            mHandler.sendEmptyMessage(SET_PVR_FILE);
                        } else {
                            getListFlag = false;
                            mHandler.sendEmptyMessage(SET_PVR_FILE);
                        }
                    }

                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initPVRlist() {
//        if (mFileListWindow == null) {
            mFileListWindow = new DvrFilelist(TurnkeyUiMainActivity.getInstance(), this,
                    mHandler);
            mFileListWindow.setListener(this);
            mFileListWindow.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface arg0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onDismiss");
                    dissmiss();
                }
            });
//        }
    }

    public void reShowPVRlist() {
        if (mFileListWindow != null && mFileListWindow.isShowing()) {
          mHandler.removeMessages(AUTO_DISMISS_FILE_LIST);
          mHandler.sendEmptyMessageDelayed(AUTO_DISMISS_FILE_LIST, 10 * 1000);
        } else {
            mFileListWindow = null;
            showPVRlist();
        }
    }

    public void deletePvrFile() {
        mHandler.sendEmptyMessage(DELETE_FILE_);
    }

    public void delete() {
        mFileListWindow.deleteFile(0);
    }
    public void delete(int index) {
            mFileListWindow.deleteFile(index);
        }

    public DVRFiles getSelectedFile() {
        return mFileListWindow.getSelectedFile();
    }

    @Override
    public boolean onKeyDown(int keycode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"keycode== "+keycode);
        if (mFileListWindow!=null&&!mFileListWindow.isShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"state error");
            dissmiss();
            return  false;
        }
        switch (keycode) {
            case KeyMap.KEYCODE_MENU:
                if (mFileListWindow!=null&&mFileListWindow.isShowing()) {
                    dissmiss();
                }
                break;
            case KeyMap.KEYCODE_DPAD_CENTER:
            case KeyMap.KEYCODE_DPAD_UP:
            case KeyMap.KEYCODE_DPAD_DOWN:
            case KeyMap.KEYCODE_MTKIR_GREEN:
            case KeyMap.KEYCODE_MTKIR_YELLOW:
            case KeyMap.KEYCODE_MTKIR_RED:
            case KeyMap.KEYCODE_MTKIR_INFO:
            case KeyMap.KEYCODE_MTKIR_BLUE:
                return true;
            case KeyMap.KEYCODE_MTKIR_STOP:
//                if (null != mFileListWindow && mFileListWindow.isShowing()) {
//                    return false;
//                }
                return false;
                // Fix CR: DTV00583834
            case KeyMap.KEYCODE_MTKIR_TIMER:
            case KeyMap.KEYCODE_MTKIR_SLEEP:
            case KeyMap.KEYCODE_MTKIR_ZOOM:
            case KeyMap.KEYCODE_MTKIR_PEFFECT:
            case KeyMap.KEYCODE_MTKIR_SEFFECT:
            case KeyMap.KEYCODE_MTKIR_ASPECT:
                // Fix CR:DTV00583855,DTV00583474
                if (mFileListWindow!=null&&mFileListWindow.isShowing()) {
                    mFileListWindow.dimissInfobar();
                }
                getManager().restoreToDefault(mStateSelf);
                unRegister();
                return false;
            case KeyMap.KEYCODE_BACK:
                // Fix CR:DTV00583855,DTV00583474
                if (mFileListWindow!=null&&mFileListWindow.isShowing()) {
                    mFileListWindow.dimissInfobar();
                }
                unRegister();
                getManager().restoreToDefault(mStateSelf);
                return true;
            case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
                DvrManager.getInstance().uiManager.hiddenAllViews();

                if (mFileListWindow!=null&&!mFileListWindow.isShowing()) {
                    PwdDialog mPwdDialog = (PwdDialog) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasic.NAV_COMP_ID_PWD_DLG);
                    if (mPwdDialog != null && mPwdDialog.isVisible()) {
                        mPwdDialog.dismiss();
                    }
                    this.reShowPVRlist();
                    return true;
                }
                unRegister();
                // Fix CR:DTV00583855,DTV00583474
                if (mFileListWindow!=null&&mFileListWindow.isShowing()) {
                    if (mFileListWindow != null) {
                        mFileListWindow.dimissInfobar();
                        mHandler.removeMessages(AUTO_DISMISS_FILE_LIST);
                        getManager().restoreToDefault(mStateSelf);
                    }
                    return true;
                }
                return false;
            case KeyMap.KEYCODE_MTKIR_SUBTITLE:
            case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
                return true;
            case KeyMap.KEYCODE_MTKIR_PRECH:
            case KeyMap.KEYCODE_MTKIR_CHUP:
            case KeyMap.KEYCODE_MTKIR_CHDN:
            case KeyMap.KEYCODE_MTKIR_SOURCE:
            case KeyMap.KEYCODE_MTKIR_PIPPOP:
            case KeyMap.KEYCODE_MTKIR_PIPPOS:
            case KeyMap.KEYCODE_MTKIR_PIPSIZE:
            case KeyMap.KEYCODE_DPAD_LEFT:
            case KeyMap.KEYCODE_DPAD_RIGHT:
            case KeyMap.KEYCODE_MTKIR_RECORD:
            case KeyMap.KEYCODE_MTKIR_EJECT:
            case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
            case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
            case KeyMap.KEYCODE_MTKIR_REWIND:
                return true;
                // Fix CR: DTV00584842
            case KeyMap.KEYCODE_MTKIR_GUIDE:
            case KeyMap.KEYCODE_MTKIR_FREEZE:
            case KeyMap.KEYCODE_VOLUME_DOWN:
            case KeyMap.KEYCODE_VOLUME_UP:
                return true;
            default:
                break;
        }
        return false;
    }

    public void unRegister() {

        if (sfReceive == null) {
            return;
        }
        try {
            mContext.unregisterReceiver(sfReceive);
            sfReceive = null;
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * @param keycode
     * @return
     */
    /*private*/ boolean dispatchTheOnkeyDown(int keycode) {

        switch (keycode) {
            case KeyMap.KEYCODE_DPAD_CENTER:
                break;
            case KeyMap.KEYCODE_DPAD_UP:
                break;
            case KeyMap.KEYCODE_DPAD_DOWN:
                break;
            case KeyMap.KEYCODE_MTKIR_GREEN:
                break;
            case KeyMap.KEYCODE_MTKIR_YELLOW:
                break;
            case KeyMap.KEYCODE_BACK:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onResume() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onResume");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        if (mFileListWindow != null && mFileListWindow.isShowing()) {
            mFileListWindow.setOnDismissListener(null);
            mFileListWindow.dimissInfobar();
        }
    }

    @Override
    public void onStop() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onStop");
    }

    @Override
    public void onRelease() {
        if (mFileListWindow != null && mFileListWindow.isShowing()) {
            mFileListWindow.setOnDismissListener(null);
            mFileListWindow.dimissInfobar();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        MultiViewControl mMultiViewControl = (MultiViewControl)
			ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POP);
        if(mMultiViewControl != null){
            mMultiViewControl.setNormalTvModeWithLauncher(false);
            }
        showSpeciaToast(parent,position);
        if (mFileListWindow != null && mFileListWindow.isShowing()) {
            dissmiss();
        }

        if (StateDvrFileList.getInstance() != null) {
            DvrManager.getInstance().removeState(StateDvrFileList.getInstance());
        }
        ComponentsManager.getInstance().hideAllComponents();
        playDvrFile(parent, position);
    }

    private void showSpeciaToast(AdapterView<?> parent,int position){
        String uri = ((DVRFiles) parent.getItemAtPosition(position)).getProgramName().replace( ContentResolver.SCHEME_FILE+"://","");
        boolean isRecording =  !((DVRFiles) parent.getItemAtPosition(position)).isRecording();
        if(isRecording) {
            TVAsyncExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    MtkTvPvrBrowserBase mtkpvr = new MtkTvPvrBrowserBase();
                    MtkTvPvrBrowserItemBase mmm = mtkpvr.getPvrBrowserItemByPath(uri);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopreason==" + mmm.mStopReason + ",mmm=" + mmm.toString());
                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = (int) mmm.mStopReason;
                    msg.arg2 = isRecording ? 1 : 0;
                    msg.what = SHOW_PVR_MSG;
                    mHandler.sendMessage(msg);
                }
            });
        }
    }

    private void showUIMsg(int reason,boolean flag){
        if(flag){
            switch(reason){
                case 0:
                case 2:
                    Toast.makeText(mContext, mContext.getString(R.string.pvr_file_list_specia_toast), Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(mContext, mContext.getString(R.string.pvr_file_list_singal_loss), Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(mContext, mContext.getString(R.string.pvr_file_list_no_eit), Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    Toast.makeText(mContext, mContext.getString(R.string.pvr_file_list_no_eit_singal_loss), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * play dvr file
     */
    private void playDvrFile(AdapterView<?> parent, int position) {

        DVRFiles dvrFiles = ((DVRFiles) parent.getItemAtPosition(position));
        Uri uri = dvrFiles.getProgarmUri();
        if (uri != null) {
        	  if (MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
        	MtkTvGinga.getInstance().stopGinga();}
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "dvr file play start !" + "  uri==" + uri);
            DvrManager.getInstance().setState(StateDvrPlayback.getInstance(
                    getManager()));
            stopTimeshift();
            StateDvrPlayback.getInstance().prepareDvrFilePlay(dvrFiles);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "uri==null");
        }

    }

    private void stopTimeshift(){
        TifTimeShiftManager mTimeShiftManager = TurnkeyUiMainActivity
                .getInstance().getmTifTimeShiftManager();
        if (mTimeShiftManager != null
                && SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                MenuConfigManager.TIMESHIFT_START)) {
            SaveValue.getInstance(mContext).setLocalMemoryValue(
                    MenuConfigManager.TIMESHIFT_START, false);
               /* SaveValue.saveWorldBooleanValue(mContext,
                        MenuConfigManager.TIMESHIFT_START, false, false);*/
        }
    }
    // this method to play pvr files
   /* private void playPvrFileByMMPModule(AdapterView<?> parent, int position) {
    }*/

    public static StateDvrFileList getInstance() {
        if(DvrManager.getInstance()!=null && DvrManager.getInstance().getState() instanceof StateDvrFileList){
            return (StateDvrFileList) DvrManager.getInstance().getState();
        }
        return null;
    }

    public  synchronized static StateDvrFileList getInstance(DvrManager manager) {
		if (mStateSelf == null){
			manager = DvrManager.getInstance();//must keep manage in latest.
			mStateSelf = new StateDvrFileList(TurnkeyUiMainActivity.getInstance(), manager);
            RxBus.instance.onEvent(ActivityDestroyEvent.class)
                .filter(it -> {
                    synchronized (StateDvrFileList.class) {
                        if (mStateSelf == null){
                            return true;
                        }
                        if(mStateSelf.mContext == null){
                            return true;
                        }
                        if(it.activityClass == mStateSelf.mContext.getClass()){
                            mStateSelf.free();
                            return true;
                        }
                        return false;
                    }
                })
                .firstElement()
                .subscribe();

		}
		return mStateSelf;
	}

    public boolean isShowing() {
        if (mFileListWindow != null) {
            return mFileListWindow.isShowing();
        }
        return false;
    }

    public void dissmiss() {
        if (mFileListWindow == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mFileListWindow is null.");
            return;
        }
        if (mFileListWindow.isShowing()) {
            mFileListWindow.dimissInfobar();
        }
        unRegister();
        mHandler.removeCallbacksAndMessages(null);
        getManager().restoreToDefault(mStateSelf);
        mStateSelf = null;
    }

    public Handler getHandler() {
        return mHandler;
    }

    class StateFileReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRecordOrPlayFileParams2();
            // mHandler.sendEmptyMessage(SET_PVR_FILE);
        }
    }

    public void setFlag(boolean flag){
        getListFlag = flag;
    }
}
