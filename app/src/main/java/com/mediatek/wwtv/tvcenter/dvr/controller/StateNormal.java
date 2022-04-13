package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.app.Activity;
//import android.content.ContentResolver;
import android.content.Context;
//import android.database.Cursor;
//import android.view.KeyEvent;
import android.os.Bundle;
import android.view.View;

import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
//import com.mediatek.wwtv.tvcenter.dvr.controller.StatusType;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.GingaTvDialog;
import com.mediatek.wwtv.tvcenter.nav.view.MiscView;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
//import com.mediatek.wwtv.tvcenter.dvr.controller.IStateInterface;
//import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
//import com.mediatek.wwtv.tvcenter.dvr.controller.TVLogicManager;
//import com.mediatek.wwtv.tvcenter.dvr.manager.Controller;
//import com.mediatek.wwtv.tvcenter.dvr.controller.UImanager;
//import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
//import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
//import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.SomeArgs;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;

import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.lang.ref.WeakReference;
import java.util.List;
import android.widget.Toast;
import com.mediatek.wwtv.tvcenter.R;


public class StateNormal extends StateBase {

    private static final String TAG = "StateNormalDvr";
    private static boolean isTuned = false;
    private static boolean tvNotShow = true;
    private static boolean noVideo = false;
    private static boolean noAudio = false;
    private static int timeout = 0;
    private final CallBackHandler mCallBackHandler;

    public StateNormal(Context context, DvrManager manager) {
        super(context, manager);
        setType(StatusType.NORMAL);
        mCallBackHandler = new CallBackHandler(manager, this);
        getController().addEventHandler(mCallBackHandler);
    }

    @Override
    public void free(){
        getController().removeEventHandler(mCallBackHandler);
    }

    public static boolean isTuned() {
        return isTuned;
    }

    public static void setTuned(boolean isTuned) {
        StateNormal.isTuned = isTuned;
    }

    public static void setisVideo(boolean noVideo) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "novideo=" + noVideo);
        StateNormal.noVideo = noVideo;
    }

    public static boolean isVideo() {
        return noVideo;
    }

    public static void setisAudio(boolean noAudio) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "noaudio=" + noAudio);
        StateNormal.noAudio = noAudio;
    }

    public static boolean isAudio() {
        return noAudio;
    }

    public static boolean isTvNotShow() {
        return tvNotShow;
    }

    public static void setTvNotShow(boolean tvNotShow) {
        StateNormal.tvNotShow = tvNotShow;
    }

    static class CallBackHandler extends Handler {
        private int mRecordStatus = 2;// 0 - start Record; 1 - Record Success; 2
                                      // - Record Failed

        WeakReference<DvrManager> refDvrManager;
        WeakReference<StateNormal> refStateNormal;
        CallBackHandler(DvrManager dvrManager, StateNormal stateNormal) {
            this.refDvrManager = new WeakReference<DvrManager>(dvrManager); // reserve.
            refStateNormal = new WeakReference<>(stateNormal);
        }

        @Override
        public void handleMessage(Message msg) {

            if (msg != null) {
                DvrManager dvrManager = refDvrManager.get();
                int callBack = msg.what;
                SomeArgs args = (SomeArgs) msg.obj;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "callBack = " + callBack);
                switch (callBack) {
                case DvrConstant.MSG_CALLBACK_CONNECT_FAIL:
                    isTuned = false;
                    dvrManager.restoreToDefault();
                    mRecordStatus = 2;
                    break;
                case DvrConstant.MSG_CALLBACK_DISCONNECT:
                    isTuned = false;
                    dvrManager.restoreToDefault();
                    mRecordStatus = 2;
                    break;
                case DvrConstant.MSG_CALLBACK_TUNED:
                    isTuned = true;
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
                    SaveValue.getInstance(dvrManager.getContext()).saveValue(DvrManager.DVR_URI_ID,
                        programId);
                    SaveValue.getInstance(dvrManager.getContext()).saveValue(DvrManager.DVR_BG_TUNER_TYPE,
                        CommonIntegration.getInstance().getTunerModeFromSvlId(svl));
                    SaveValue.saveWorldValue(dvrManager.getContext(),DvrManager.DVR_URI_ID,(int)programId,false);
                    Uri programUri = null;
                    if (programId != -1){
                        programUri = TvContract.buildProgramUri(programId);
                        }
                    if (!(DvrManager.getInstance().pvrIsRecording())){
                        if (mRecordStatus > 1) {
                            if(timeout!=0){
//                            }else{
                                //timeout--;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"retry more=="+timeout);
                                sendEmptyMessageDelayed(DvrConstant.MSG_CALLBACK_TUNED,1000);
                            }
                            try {
                                DvrManager.getInstance().startDvr(programUri);
                            } catch (Exception e) {
                                // TODO: handle exception
                                e.printStackTrace();
                                }
                                mRecordStatus = 0;
                            }
                        }
                    TIFChannelManager.getInstance(DvrManager.getInstance().getContext()).changeTypeToBroadCast();
                    break;
                case DvrConstant.MSG_CALLBACK_RECORD_STOPPED:
                    mRecordStatus = 3;
                    removeMessages(DvrConstant.MSG_CALLBACK_TUNED);
                    SaveValue.getInstance(dvrManager.getContext()).saveValue(DvrManager.DVR_URI_ID,
                        -1L);
                    SaveValue.getInstance(dvrManager.getContext()).saveValue(DvrManager.DVR_BG_TUNER_TYPE,
                        -1);
                    SaveValue.saveWorldValue(dvrManager.getContext(),DvrManager.DVR_URI_ID,0,false);
                    SaveValue.saveWorldBooleanValue(dvrManager.getContext(),"pvr_is_dialog_show",false,false);
                    break;
                case DvrConstant.MSG_CALLBACK_ERROR:
                    int error = args.argi2;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "error = " + error+",time->"+timeout);
                    boolean issingalloss =error==MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_RECORDING_TUNED_VALUE_FAILED;
                    if(DvrManager.getInstance().pvrIsRecording()&&issingalloss){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"is running singal loss");
                        break;
                    }
                    mRecordStatus = 3;
                    if (timeout == 0 || (error == 1)||issingalloss) {
                        dvrManager.showPromptInfo(getErrorId(error));
                        dvrManager.getController().dvrRelease();
                        removeMessages(DvrConstant.MSG_CALLBACK_TUNED);
                        timeout = 0;
                        isTuned = false;
                        tvNotShow = true;
                        if(DvrManager.getInstance().getBGMState()){
                            SaveValue.saveWorldBooleanValue
                            (dvrManager.getContext(),DvrManager.BGM_PVR_FAIL,
                                    true,true);
                        }
                        if (DvrManager.getScheduleItem() != null) {
                            DvrManager.setScheduleItem(null);
                        }
                        dvrManager.restoreToDefault();
                        SaveValue.getInstance(dvrManager.getContext()).saveValue(DvrManager.DVR_URI_ID,
                            -1L);
                        SaveValue.getInstance(dvrManager.getContext()).saveValue(DvrManager.DVR_BG_TUNER_TYPE,
                            -1);
                        SaveValue.saveWorldValue(dvrManager.getContext(),DvrManager.DVR_URI_ID,0,false);
                        SaveValue.saveWorldBooleanValue(dvrManager.getContext(),"pvr_is_dialog_show",false,false);
                        return;
                    }
                    // dvrManager.clearSchedulePvr();
                    break;
                case DvrConstant.MSG_CALLBACK_RECORD_EVENT:
                    timeout = 0;
                    String eventType = (String) args.arg2;
                    if (eventType.equals(DvrManager.RECORD_START)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "RECORD_START");
                        mRecordStatus = 1;
                        removeMessages(DvrConstant.MSG_CALLBACK_TUNED);
                        dvrManager.speakText("start record");
                        SystemClock.sleep(500);
                        StateDvr stateDvr =StateDvr.getStateDvr(
                                TurnkeyUiMainActivity.getInstance(), dvrManager);
                        if (DvrManager.getScheduleItem() != null) {
                            long deafult = DvrManager.getInstance()
                                    .getDurations();
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d("1111+", "is recording="
                                    + DvrManager.getScheduleItem()
                                            .getRecordDuration() + ",default="
                                    + deafult);
                            deafult = deafult>0?deafult:0;
                            DvrManager.getScheduleItem().setRecordDuration(
                                    DvrManager.getScheduleItem()
                                            .getRecordDuration() + deafult);
                            dvrManager.toRecord();
                        }
                        stateDvr.prepareStart();
                        dvrManager.setState(stateDvr);
                        isTuned = false;
                        PwdDialog mPwdDialog = (PwdDialog) ComponentsManager
                                .getInstance().getComponentById(
                                        NavBasic.NAV_COMP_ID_PWD_DLG);
                        if (mPwdDialog != null && mPwdDialog.isVisible()) {
                            mPwdDialog.dismiss();
                        }
                        GingaTvDialog gingaTvDialog = (GingaTvDialog) ComponentsManager
                                .getInstance().getComponentById(
                                        NavBasic.NAV_COMP_ID_GINGA_TV);
                        if (gingaTvDialog != null && gingaTvDialog.isVisible()) {
                            gingaTvDialog.dismiss();
                        }
                        FavoriteListDialog showFavoriteChannelListView = (FavoriteListDialog) ComponentsManager
                                .getInstance().getComponentById(
                                        NavBasic.NAV_COMP_ID_FAV_LIST);
                        if (showFavoriteChannelListView != null
                                && showFavoriteChannelListView.isVisible()) {
                            showFavoriteChannelListView.dismiss();
                        }
                        MiscView miscView = (MiscView) ComponentsManager
                                .getInstance().getComponentById(
                                        NavBasic.NAV_COMP_ID_MISC);
                        if (miscView != null && miscView.isVisible()) {
                            miscView.setVisibility(View.GONE);
                        }
                    }
                    break;
                case DvrConstant.tv_show:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvNotShow = " + false);
                    tvNotShow = false;
                    /*if (!isTuned && StateDvr.getInstance() != null
                        &&!(StateDvrPlayback.getInstance() != null
                            && StateDvrPlayback.getInstance().isRunning())) {
                        dvrManager.restoreToDefault(StateDvr.getInstance());
                    }*/
                    break;
               case DvrConstant.MSG_CALLBACK_STOP_REASON:
                    refStateNormal.get().showStopMsg(args.argi2);
                    break;
                default:
                    break;
                }
            }

        }
    }

    private void showStopMsg(int type){
        switch(type){
        case 21:
            Toast.makeText(mContext,mContext.getString(R.string.dvr_stop_reason_fail),Toast.LENGTH_LONG).show();
            break;
        case 12://no enough stop
            Toast.makeText(mContext,mContext.getString(R.string.not_enough_space),Toast.LENGTH_LONG).show();
            break;
        case 22:
            Toast.makeText(mContext,mContext.getString(R.string.dvr_msg_disk_speed_slow),Toast.LENGTH_LONG).show();
            break;
        default:
            break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown, " + keyCode);
        switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_RECORD:
            if (DataSeparaterUtil.getInstance() != null
                    && !DataSeparaterUtil.getInstance().isSupportPvr()) {
                break;
            }
            List<Integer> activesComps = ComponentsManager.getInstance()
                    .getCurrentActiveComps();
            if (!activesComps.isEmpty()){//&& activesComps.size() > 0) {
                for (Integer compid : activesComps) {
                    if (compid == NavBasic.NAV_COMP_ID_INPUT_SRC) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input source is active");
                        break;
                    }
                }
            }
            if (CommonIntegration.getInstance().is3rdTVSource()) {
                mManager.showPromptInfo(DvrManager.PRO_FEATURE_NOT_SUPPORT);
                break;
            }
            if (CommonIntegration.getInstance().isPipOrPopState()) {
                // mManager.showFeatureNotSupport();
                mManager.showPromptInfo(DvrManager.PRO_PIP_STATE);
                break;
            }
            if(ComponentsManager.getNativeActiveCompId()==NavBasic.NAV_NATIVE_COMP_ID_FVP){
                break;
            }

            //
            Single.defer(()->{
                final boolean isSignalLoss = com.mediatek.wwtv.setting.util.TVContent
                    .getInstance(mContext).isSignalLoss();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSignalLoss=" + isSignalLoss);
               return Single.just(isSignalLoss);
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(isSignalLoss ->{
                    if (isSignalLoss) {
                        mManager.showPromptInfo(DvrManager.PRO_SIGNAL_LOSS);
                    } else {
                        onKeyStartRecord();
                    }
                })
                .subscribe();

            // if (com.mediatek.wwtv.setting.util.TVContent
            // .getInstance(mContext).isSignalLoss()) {
            // // mManager.showFeatureNotAvaiable2();
            // mManager.showPromptInfo(DvrManager.PRO_SIGNAL_LOSS);
            // break;
            // }

            break;
        case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
            if (DataSeparaterUtil.getInstance() != null
                    && !DataSeparaterUtil.getInstance().isSupportPvr()) {
                break;
            }
            // getManager().getTvLogicManager().reSetZoomValues(mContext);
            if (getManager().diskIsReady()) {
                PwdDialog mPwdDialog = (PwdDialog) ComponentsManager
                        .getInstance().getComponentById(
                                NavBasic.NAV_COMP_ID_PWD_DLG);
                if (mPwdDialog != null && mPwdDialog.isVisible()) {
                    mPwdDialog.dismiss();
                }
                if (!TurnkeyUiMainActivity.getInstance().isUKCountry) {
                    BannerView bannerView = ((BannerView) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasicMisc.NAV_COMP_ID_BANNER));
                    bannerView.setVisibility(View.GONE);
                } else {
                    UkBannerView bannerView = ((UkBannerView) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasicMisc.NAV_COMP_ID_BANNER));
                    bannerView.setVisibility(View.GONE);
                }
                getManager().uiManager.hiddenAllViews();
                if (getManager().setState(
                        StateDvrFileList.getInstance(DvrManager.getInstance()))) {
                    GingaTvDialog g = ((GingaTvDialog) ComponentsManager
                            .getInstance().getComponentById(
                                    NavBasic.NAV_COMP_ID_GINGA_TV));
                    if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
                            && g != null) {
                        g.dismiss();
                    }
                    StateDvrFileList.getInstance().showPVRlist();
                    return true;
                }
                onRelease();
            } else {
                // getManager().showDiskNotReady();
                getManager().showPromptInfo(
                        DvrManager.PRO_TIME_SHIFT_DISK_NOT_READY);
            }
            break;

        case KeyMap.KEYCODE_MTKIR_PRECH:
        case KeyMap.KEYCODE_MTKIR_CHUP:
        case KeyMap.KEYCODE_MTKIR_CHDN:
        case KeyMap.KEYCODE_DPAD_DOWN:
        case KeyMap.KEYCODE_DPAD_UP:
            if(isTuned){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ch change");
            Toast.makeText(mContext,mContext.getString(R.string.dvr_starting_not_switch_channel),Toast.LENGTH_SHORT).show();
            return true;
                }
            break;
            case KeyMap.KEYCODE_MTKIR_FREEZE:
            case KeyMap.KEYCODE_MTKIR_PAUSE:
                if (isTuned&&DvrManager.getInstance().timeShiftIsEnable()) {
                    // DvrManager.getInstance().showFeatureNotAvaiable1();
                    mManager.showPromptInfo(DvrManager.PRO_PVR_RUNNING);
                    return true;
                }
                break;
        default:
            break;
        }
        return super.onKeyDown(keyCode);
    }

    private void onKeyStartRecord() {
        if (CommonIntegration.getInstance().isCurrentSourceTv()) {
            if (TVLogicManager.getInstance() != null
                    && TVLogicManager.getInstance().getCurrentChannel() == null) {
                // mManager.getTopHandler().sendEmptyMessage(
                // mManager.Channel_NOT_Support);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isvideo = " + isVideo() + ",isaudio="
                        + isAudio());
                // if(isVideo()&&isAudio()){
                mManager.showPromptInfo(DvrManager.PRO_AV_STREAM_NOT_AVAILABLE);
                return;
            }
            // if(isAudio()&&isVideo()){
            // mManager.showPromptInfo(DvrManager.PRO_FEATURE_NOT_SUPPORT);
            // break;
            // }
        } else {
            // fix DTV00852943, show can not do pvr with new spec change .
            // mManager.showFeatureNotAvaiable2();
            mManager.showPromptInfo(DvrManager.PRO_NONE_TV_SOURCE);
            return;
        }

        boolean isStart = SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                MenuConfigManager.TIMESHIFT_START);
        if (isStart) {
            // mManager.showFeatureNotSupport();
//            mManager.showPromptInfo(DvrManager.PRO_TSHIFT_RUNNING);
            showDialogForTshift();
            return;
        }
        if (CommonIntegration.getInstance().isCurrentSourceATV()) {
            mManager.showPromptInfo(DvrManager.PRO_NONE_TV_SOURCE);
            return;
        }
        if (mManager.diskIsReady()) {
            // mManager.getController().dvrRelease();
            if (!isTuned) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTuned = false");
                tvNotShow = true;
                Bundle mBundle = new Bundle();
                mBundle.putBoolean("session_event_dvr_record_is_onetouch",true);
                TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(
                        mContext).getTIFChannelInfoById(
                        CommonIntegration.getInstance().getCurrentChannelId());
                if(tifChannelInfo!=null) {
                    mManager.getController().tune(
                            TvContract.buildChannelUri(tifChannelInfo.mId), mBundle);
                    timeout = 0;
//                    DvrManager.getInstance().setState(
//                            StateDvr.getStateDvr(DvrManager.getInstance()
//                                    .getContext(), DvrManager.getInstance()));
                }else{
                    mManager.showPromptInfo(DvrManager.PRO_AV_STREAM_NOT_AVAILABLE);
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTuned = true");
            }
        } else {
            // mManager.showDiskNotReady();
            mManager.showPromptInfo(DvrManager.PRO_DISK_NOT_READY);
        }
    }

    private void showDialogForTshift() {
        SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        if (simpleDialog != null && simpleDialog.isShowing()) {
            simpleDialog.dismiss();
        }
        if (simpleDialog != null) {
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
            simpleDialog.setCancelText(R.string.pvr_confirm_no);
            simpleDialog
                    .setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
                        @Override
                        public void onConfirmClick(int dialogId) {
                            TifTimeShiftManager mTimeShiftManager = TurnkeyUiMainActivity
                                    .getInstance().getmTifTimeShiftManager();
                            if (mTimeShiftManager != null) {
                                mTimeShiftManager.stop();
                                SaveValue.getInstance(mContext).setLocalMemoryValue(
                                        MenuConfigManager.TIMESHIFT_START, false);
               /* SaveValue.saveWorldBooleanValue(mContext,
                        MenuConfigManager.TIMESHIFT_START, false, false);*/
                                SystemClock.sleep(1000);
                                onKeyStartRecord();
                            }
                        }
                    }, -1);
            simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
                @Override
                public void onCancelClick(int dialogId) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "cancel");
                }
            }, -1);
            simpleDialog.setContent(R.string.dvr_dialog_message_schedule_record);
            simpleDialog.show();
        }
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        setRunning(true);
    }

    public void removeZoomTips() {
        ZoomTipView mZoomTip = ((ZoomTipView) ComponentsManager.getInstance()
                .getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
        mZoomTip.setVisibility(View.GONE);
        if (IntegrationZoom.getInstance(mContext).screenModeZoomShow()) {// fix
                                                                         // CR
                                                                         // DTV00587179
            SundryShowTextView stxtView = (SundryShowTextView) ComponentsManager
                    .getInstance()
                    .getComponentById(NavBasic.NAV_COMP_ID_SUNDRY);
            if (stxtView != null) {
                stxtView.setVisibility(View.GONE);
            }
        }
    }

    public void reSetZoomValues(Context mContext) {
        IntegrationZoom.getInstance(mContext).setZoomMode(
                IntegrationZoom.ZOOM_1);
        removeZoomTips();
    }

    private static int getErrorId(int errorId) {
        int promptId = -1;
        switch (errorId) {
        case 0: // REC_PVR_ERR_ID_NONE
            promptId = DvrManager.PRO_FEATURE_NOT_SUPPORT;
            break;
        case 1:// REC_PVR_ERR_ID_RECORDING:
            promptId = DvrManager.PRO_RECORDING;
            break;
        case 2:// REC_PVR_ERR_ID_UNKNOWN_SRC:
            promptId = DvrManager.PRO_AV_STREAM_NOT_AVAILABLE;
            break;
        case 3:// REC_PVR_ERR_ID_FEATURE_NOT_SUPPORTED:
            promptId = DvrManager.PRO_FEATURE_NOT_SUPPORT;
            break;
        case 4:// REC_PVR_ERR_ID_INSUFFICIENT_RESOURCE:
            promptId = DvrManager.PRO_FEATURE_NOT_SUPPORT;// "Insufficient resource !";
            break;
        case 5:// REC_PVR_ERR_ID_AV_STREAM_NOT_AVAILABLE:
            promptId = DvrManager.PRO_AV_STREAM_NOT_AVAILABLE;
            break;
        case 6:// REC_PVR_ERR_ID_STREAM_NOT_AUTHORIZED:
            promptId = DvrManager.PRO_STREAM_NOT_AUTHORIZED;
            break;
        case 7:// REC_PVR_ERR_ID_INPUT_LOCKED:
            promptId = DvrManager.PRO_INPUT_LOCKED;
            break;
        case 8:// REC_PVR_ERR_ID_DISK_NOT_READY:
        case 24867948:
            promptId = DvrManager.PRO_DISK_NOT_READY;
            break;
        case 9:// REC_PVR_ERR_ID_DISK_TOO_SMALL:
            promptId = DvrManager.PRO_NO_ENOUGH_SPACE;// "Disk too small!";
            break;
        case 10:// REC_PVR_ERR_ID_DISK_FULL:
            promptId = DvrManager.PRO_NO_ENOUGH_SPACE;// "Disk full!";
            break;
        case 11:// REC_PVR_ERR_ID_VIDEO_RESOLUTION_ERROR:
        case 16:
            promptId = DvrManager.PRO_VIDEO_RESOLUTION_ERROR;// "Video resolution error !";
            break;
        case 12:// REC_PVR_ERR_ID_INTERNAL_ERROR:
            promptId = DvrManager.PRO_INTERNAL_ERROR;// "Internal error!";
            break;
        case 13:// REC_PVR_ERR_ID_LAST:
            promptId = DvrManager.PRO_FEATURE_NOT_SUPPORT;
            break;
        default:
            break;
        }
        return promptId;
    }

    // static Runnable runnable = new Runnable() {
    //
    // @Override
    // public void run() {
    // for (int i = 0; i < 151; i++) {
    // if (tvNotShow) {
    // SystemClock.sleep(100);
    // } else {
    // break;
    // }
    // }
    // if (CommonIntegration.getInstance().isPipOrPopState()) {
    // DvrManager.getInstance().showFeatureNotAvaiable();
    // isTuned = false;
    // tvNotShow = true;
    // return;
    // }
    // long programId =
    // TIFProgramManager.getInstance(DvrManager.getInstance().getContext())
    // .queryCurrentProgram();
    // Uri programUri = null;
    // if (programId != -1)
    // programUri = TvContract.buildProgramUri(programId);
    // DvrManager.getInstance().startDvr(programUri);
    // }
    // };
}
