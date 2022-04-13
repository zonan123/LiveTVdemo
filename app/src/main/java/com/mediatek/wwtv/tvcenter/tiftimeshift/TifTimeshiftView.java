package com.mediatek.wwtv.tvcenter.tiftimeshift;

import java.text.SimpleDateFormat;
import java.io.File;
import android.content.Context;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.GingaTvDialog;
import com.mediatek.wwtv.tvcenter.nav.view.InfoBarDialog;
import com.mediatek.wwtv.tvcenter.nav.view.MiscView;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.TTXMain;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager.TimeShiftActionId;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramInfo;
import com.mediatek.wwtv.tvcenter.util.WeakHandler;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.dm.MountPoint;
import com.mediatek.dm.DeviceManager;
import com.android.tv.menu.MenuOptionMain;
import com.mediatek.wwtv.tvcenter.nav.InternalHandler;
import com.mediatek.twoworlds.tv.MtkTvHBBTV;
import android.widget.RelativeLayout;

public class TifTimeshiftView extends NavBasicView {
    private static final String TAG = "TifTimeshiftView";
    public static final String ACTION_TIMESHIFT_STOP = "com.mediatek.wwtv.tvcenter.tiftimeshift.TIMESHIFT_STOP";
    public static final int MSG_PAUSE_TIMESHIFT = 1001;
    public static final int MSG_SYSTEM_TIME = 1002;
    public static final int FILTER_VIDEO = 1;
    public static final int PROGRESSBAR_WIDTH_AVAILABLE = 575 * 2; // params.width = 580 will full progressbar
    public static final int PROGRESSBAR_WIDTH = 995 * 2; // 900dp

    private TextView programStartTime,
                     programEndtime,
                     playingTime;

    private ImageView start,
                      watch,
                      buffer,
                      end,
                      mPlayIcon,
                      indicator;

    //private ComponentsManager comManager;

    private long mProgramStartTimeMs = 0;
    private long mProgramEndTimeMs = 0;

    private TifTimeShiftManager mTimeShiftManager;
    private RelativeLayout rLbackground;
    private static CommonIntegration mNavIntegration = null;
    private final Context context;
    private TimeshiftInternalHandler mTimeshiftHandler;
    private int mTFCallbackPlaybackStatus = -1;
    protected InternalHandler mHandler;
    private long mCurrentMillSeconds;
//    private boolean isBack = false;

    private long mCurrentRewindMillSeconds;
    private long mCurrentFastForawrdMillSeconds;
    private long mSystemUtcTime;

    public TifTimeshiftView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        componentID = NAV_COMP_ID_TIFTIMESHIFT_VIEW;
        this.context = context;
    }

    public TifTimeshiftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        componentID = NAV_COMP_ID_TIFTIMESHIFT_VIEW;
        this.context = context;
    }

    public TifTimeshiftView(Context context) {
        super(context);
        componentID = NAV_COMP_ID_TIFTIMESHIFT_VIEW;
        this.context = context;
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler keyCode = " + keyCode);
        switch (keyCode) {
            case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
            case KeyMap.KEYCODE_MTKIR_PAUSE:
            case KeyMap.KEYCODE_MTKIR_FREEZE:
            {
                int value = MtkTvConfig.getInstance().getConfigValue(
                    MtkTvConfigType.CFG_RECORD_REC_TSHIFT_MODE);
                if (value == 0){
                    return false;
                }

                // show pwdDialog when program locked for cr DTV00839266
                PwdDialog mPWDDialog = (PwdDialog) ComponentsManager
                        .getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
                int pwdStatus = MtkTvPWDDialog.getInstance().PWDShow();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MtkTvPWDDialog.getInstance().PWDShow(): " + pwdStatus);
                if (pwdStatus == 0) {
                    mPWDDialog.show();
                    return false;
                }

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ishbbtv----:"+(ComponentsManager.getActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_HBBTV));
                if(ComponentsManager.getNativeActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_HBBTV){
                    MtkTvHBBTV.getInstance().killHBBTVApp();
                }

                // dismiss dialog when show timeshift ui.
                hideOtherView();




                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getVisibility=" + getVisibility() + ",isAvailable="
                        + mTimeShiftManager.isAvailable());
                if (getVisibility() != View.VISIBLE && mTimeShiftManager.isAvailable() && (isCurrentSourceHasSignal()|| CommonIntegration.getInstance().is3rdTVSource())) {
                    //com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyhandler mTFCallbackPlaybackStatus:" + mTFCallbackPlaybackStatus);
                    mTFCallbackPlaybackStatus = -1;
                    //mPlayIcon.setImageResource(R.drawable.tif_play_pause_icon);
                    //mPlayFNum.setVisibility(View.INVISIBLE);
                    mTimeShiftManager.pause();
                    //SaveValue.getInstance(mContext).saveBooleanValue(MenuConfigManager.,
                    //        true);
                    //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, true, false);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyhandler mTFCallbackPlaybackStatus:" + mTFCallbackPlaybackStatus);
                } else {
                    // this update for this cr DTV00841766
                    if (!DvrManager.getInstance().diskIsReady()) {
//                        DvrManager.getInstance(context).showDiskNotReady();
                        DvrManager.getInstance().showPromptInfo(DvrManager.PRO_TIME_SHIFT_DISK_NOT_READY);
                    } else {
                        if(((mTimeShiftManager.getBroadcastTimeInUtcSeconds() - mCurrentMillSeconds)/1000) > Toast.LENGTH_SHORT){
                            Toast.makeText(context, R.string.timeshift_not_available, Toast.LENGTH_SHORT).show();
                            mTimeShiftManager.speakText("timeshift is not available");
                        }
                        mCurrentMillSeconds = mTimeShiftManager.getBroadcastTimeInUtcSeconds();
                    }
                    return false;
                }

                return true;
            }
            case KeyMap.KEYCODE_MTKIR_REWIND:
            case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
            case KeyMap.KEYCODE_MTKIR_PREVIOUS:
            case KeyMap.KEYCODE_MTKIR_NEXT:
            case KeyMap.KEYCODE_MTKIR_STOP:
            {
                int value = MtkTvConfig.getInstance().getConfigValue(
                    MtkTvConfigType.CFG_RECORD_REC_TSHIFT_MODE);
                if (value == 0) {
                    return false;
                }

                boolean isStart =
                    SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                    MenuConfigManager.TIMESHIFT_START);
                if(!isStart) {
                    return false;
                }

                if (getVisibility() != View.VISIBLE && mTimeShiftManager.isAvailable()) {
                    return true;
                }

                break;
            }


            default:
                break;
        }

        return super.isKeyHandler(keyCode);
    }

    //@Override
    //public boolean isCoExist(int componentID) {
    //    return super.isCoExist(componentID);
    //}

    @Override
    public boolean isVisible() {
        // TODO Auto-generated method stub
        return SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                MenuConfigManager.TIMESHIFT_START);
        //return isStart;
    }

    public boolean isCurrentSourceHasSignal() {
        mNavIntegration = CommonIntegration.getInstance();
        return mNavIntegration.isCurrentSourceHasSignal();
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler keyCode = " + keyCode);
        switch (keyCode) {
            case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
            //case KeyMap.KEYCODE_MTKIR_PLAY:
            //case KeyMap.KEYCODE_MTKIR_PAUSE:
            case KeyMap.KEYCODE_MTKIR_FREEZE:
            case KeyMap.KEYCODE_DPAD_CENTER:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "play/pause mTFCallbackPlaybackStatus:" + mTFCallbackPlaybackStatus);
                if(mTFCallbackPlaybackStatus == -1){
                    return true;
                }
                if (mTFCallbackPlaybackStatus != 0 && mTFCallbackPlaybackStatus != 4 && !CommonIntegration.getInstance().is3rdTVSource()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Freeze/PlayPause timeshift status error and return operation.");
                    if(((mTimeShiftManager.getBroadcastTimeInUtcSeconds() - mCurrentMillSeconds)/1000) > Toast.LENGTH_SHORT){
                        Toast.makeText(context, R.string.timeshift_not_available, Toast.LENGTH_SHORT).show();
                        mTimeShiftManager.speakText("timeshift is not available");
                    }
                    mCurrentMillSeconds = mTimeShiftManager.getBroadcastTimeInUtcSeconds();
                    return true;
                }
                if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler dvr running status:" + StateDvrPlayback.getInstance().isRunning());
                    return false;
                }
                //mPlayFNum.setVisibility(View.INVISIBLE);
//                isBack = false;
                setVisibility(View.VISIBLE);
                // display always when pause status.
                if(mTimeShiftManager.getDisplayedPlaySpeed() > mTimeShiftManager.PLAY_SPEED_1X ) {
                    startTimeout(NavBasicView.NAV_TIMEOUT_10);
                    mTimeShiftManager.speakText("timeshift play");
                    mTimeShiftManager.play();
                } else {
                    if (mTimeShiftManager.getPlayStatus() == TifTimeShiftManager.PLAY_STATUS_PAUSED) {
                        startTimeout(NavBasicView.NAV_TIMEOUT_10);
                    } else {
                        stopTimeout();
                    }
                    mTimeShiftManager.togglePlayPause();
                }
                hideOtherView();

                return true;
            case KeyMap.KEYCODE_MTKIR_PLAY:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "play mTFCallbackPlaybackStatus:=" + mTFCallbackPlaybackStatus);
                if (mTFCallbackPlaybackStatus != 0 && mTFCallbackPlaybackStatus != 4 && !CommonIntegration.getInstance().is3rdTVSource()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Play timeshift status error and return operation.");
                    return true;
                }
                if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler dvr running status:" + StateDvrPlayback.getInstance().isRunning());
                    return false;
                }
                //mPlayFNum.setVisibility(View.INVISIBLE);
//                isBack = false;
                setVisibility(View.VISIBLE);
                // display always when pause status.
                if (mTimeShiftManager.getPlayStatus() == TifTimeShiftManager.PLAY_STATUS_PAUSED) {
                    startTimeout(NavBasicView.NAV_TIMEOUT_10);
                } else {
                    stopTimeout();
                }
                hideOtherView();
                mTimeShiftManager.speakText("timeshift play");
                mTimeShiftManager.play();
                return true;
            case KeyMap.KEYCODE_MTKIR_PAUSE:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pause mTFCallbackPlaybackStatus:" + mTFCallbackPlaybackStatus);
                if(mTimeShiftManager.getPlayStatus() == TifTimeShiftManager.PLAY_STATUS_PAUSED){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pause currentispause:");
                    return true;
                }
                if (mTFCallbackPlaybackStatus != 0 && mTFCallbackPlaybackStatus != 4 && !CommonIntegration.getInstance().is3rdTVSource()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Pause timeshift status error and return operation.");
                    if(((mTimeShiftManager.getBroadcastTimeInUtcSeconds() - mCurrentMillSeconds)/1000) > Toast.LENGTH_SHORT){
                        Toast.makeText(context, R.string.timeshift_not_available, Toast.LENGTH_SHORT).show();
                        mTimeShiftManager.speakText("timeshift is not available");
                    }
                    mCurrentMillSeconds = mTimeShiftManager.getBroadcastTimeInUtcSeconds();
                    return true;
                }
                if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler dvr running status:" + StateDvrPlayback.getInstance().isRunning());
                    return false;
                }
                //mPlayFNum.setVisibility(View.INVISIBLE);
//                isBack = false;
                setVisibility(View.VISIBLE);
                // display always when pause status.
                if (mTimeShiftManager.getPlayStatus() == TifTimeShiftManager.PLAY_STATUS_PAUSED) {
                    startTimeout(NavBasicView.NAV_TIMEOUT_10);
                } else {
                    stopTimeout();
                }
                hideOtherView();
                mTimeShiftManager.pause();
                return true;
            case KeyMap.KEYCODE_MTKIR_REWIND:
            case KeyMap.KEYCODE_DPAD_LEFT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rewind mTFCallbackPlaybackStatus:" + mTFCallbackPlaybackStatus);
                if (mTFCallbackPlaybackStatus != 0 && mTFCallbackPlaybackStatus != 4 && !CommonIntegration.getInstance().is3rdTVSource()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Rewind timeshift status error and return operation.");
                    return true;
                }
  //              isBack = false;
				setVisibility(View.VISIBLE);
//                this.startTimeout(NavBasicView.NAV_TIMEOUT_10);
                hideOtherView();

                if((mTimeShiftManager.getBroadcastTimeInUtcSeconds() - mCurrentRewindMillSeconds) > 200){
                    mTimeShiftManager.rewind();
                }
                mCurrentRewindMillSeconds = mTimeShiftManager.getBroadcastTimeInUtcSeconds();
                return true;
            case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
            case KeyMap.KEYCODE_DPAD_RIGHT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fastforward mTFCallbackPlaybackStatus:" + mTFCallbackPlaybackStatus);
                if (mTFCallbackPlaybackStatus != 0 && mTFCallbackPlaybackStatus != 4 && !CommonIntegration.getInstance().is3rdTVSource()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Fastforward timeshift status error and return operation.");
                    return true;
                }
//                isBack = false;
				setVisibility(View.VISIBLE);
//                this.startTimeout(NavBasicView.NAV_TIMEOUT_10);
                hideOtherView();

                if((mTimeShiftManager.getBroadcastTimeInUtcSeconds() - mCurrentFastForawrdMillSeconds) > 200){
                    mTimeShiftManager.fastForward();
                }
                mCurrentFastForawrdMillSeconds = mTimeShiftManager.getBroadcastTimeInUtcSeconds();
                return true;
            case KeyMap.KEYCODE_MTKIR_PREVIOUS:
				setVisibility(View.VISIBLE);
                this.startTimeout(NavBasicView.NAV_TIMEOUT_10);
                if (mTimeShiftManager.isForwarding()
                        && mTimeShiftManager.getDisplayedPlaySpeed() != mTimeShiftManager.PLAY_SPEED_1X) {
                    showToast(true);
                    return true;
                } else if (mTimeShiftManager.isRewinding()
                        && mTimeShiftManager.getDisplayedPlaySpeed() != mTimeShiftManager.PLAY_SPEED_1X) {
                  showToast(false);
                    return true;
                }
                hideOtherView();
                mTimeShiftManager.jumpToPrevious();
                return true;
            case KeyMap.KEYCODE_MTKIR_NEXT:
				setVisibility(View.VISIBLE);
                this.startTimeout(NavBasicView.NAV_TIMEOUT_10);
                if (mTimeShiftManager.isForwarding()
                        && mTimeShiftManager.getDisplayedPlaySpeed() != mTimeShiftManager.PLAY_SPEED_1X) {
                    showToast(true);
                    return true;
                } else if (mTimeShiftManager.isRewinding()
                        && mTimeShiftManager.getDisplayedPlaySpeed() != mTimeShiftManager.PLAY_SPEED_1X) {
                    showToast(false);
                    return true;
                }
                hideOtherView();
                mTimeShiftManager.jumpToNext();
                return true;
            case KeyMap.KEYCODE_BACK:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Press BACK key getVisibility(): " + getVisibility());
                if (getVisibility() != View.VISIBLE) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Current timeshift view is not show, and rePress BACK key to stop timeshift play.");
                    reset();
                    mTimeshiftHandler.sendEmptyMessage(MSG_PAUSE_TIMESHIFT);
                    return true;
                }

                //mPlayFNum.setVisibility(View.INVISIBLE);
//                isBack = true;
                setVisibility(View.GONE);
                return true;
            case KeyMap.KEYCODE_MTKIR_STOP:
                reset();
                hideMenuOptions();
                //stopTifTimeShift();
                mTimeshiftHandler.sendEmptyMessage(MSG_PAUSE_TIMESHIFT);
                return true;
            case KeyMap.KEYCODE_MTKIR_PRECH:
                int lastId = CommonIntegration.getInstance().getLastChannelId();
                int currentId = CommonIntegration.getInstance().getCurrentChannelId();
                if (lastId == currentId) {
                  Toast.makeText(context, R.string.timeshfit_no_pre_channel,
                          Toast.LENGTH_SHORT).show();
                  return true;
                }
            case KeyMap.KEYCODE_MTKIR_CHDN:
            case KeyMap.KEYCODE_MTKIR_CHUP:
             //   stopTifTimeShift();
                break;
            case KeyMap.KEYCODE_MTKIR_PIPPOP:
                /*DvrDialog conDialog = new DvrDialog(TurnkeyUiMainActivity.getInstance(),
                        DvrDialog.TYPE_Confirm, keyCode, DvrDialog.TYPE_Timeshift);
                conDialog.show();*/
                return true;
            case KeyMap.KEYCODE_MTKIR_GUIDE:
            	Toast.makeText(context, R.string.warning_time_shifting_recording,
						Toast.LENGTH_SHORT).show();
            	return true ;
            default:
                break;
        }
        return super.onKeyHandler(keyCode, event, fromNative);
    }

    private void hideOtherView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hideOtherView start.");
        dismissGingaDialog();
        dismissGingaloadingDialog();
        dismissChannelListDialog();
        dismissSundryShowTextView();
        dismissMiscView();
        dismissTTXMain();
        dismissZoomTipView();
        if (StateDvrFileList.getInstance() != null) {
            StateDvrFileList.getInstance().dissmiss();
        }
    }

    private void dismissSundryShowTextView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissSundryShowTextView start.");
        SundryShowTextView sundryShowTextView = (SundryShowTextView) ComponentsManager.getInstance()
                .getComponentById(NavBasicView.NAV_COMP_ID_SUNDRY);
        if (sundryShowTextView != null && sundryShowTextView.isVisible()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissSundryShowTextView end.");
            sundryShowTextView.setVisibility(View.GONE);
        }
    }

    private void dismissMiscView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissMiscView start.");
        MiscView miscView = (MiscView) ComponentsManager.getInstance()
                .getComponentById(NavBasicView.NAV_COMP_ID_MISC);
        if (miscView != null && miscView.isVisible()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissMiscView end.");
            miscView.setVisibility(View.GONE);
        }
    }

    private void dismissTTXMain() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissTTXMain start.");
        TTXMain ttxMain = (TTXMain) ComponentsManager.getInstance()
                .getComponentById(NavBasicView.NAV_COMP_ID_TELETEXT);
        if (ttxMain != null && ttxMain.isVisible()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissTTXMain end.");
            ttxMain.setVisibilityForNotify(View.GONE);
        }
    }

    private void dismissZoomTipView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissZoomTipView start.");
        ZoomTipView zoomTipView = (ZoomTipView) ComponentsManager.getInstance()
                .getComponentById(NavBasicView.NAV_COMP_ID_ZOOM_PAN);
        if (zoomTipView != null && zoomTipView.isVisible()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissZoomTipView end.");
            zoomTipView.setVisibility(View.GONE);
        }
    }

    private void dismissChannelListDialog() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isUKCountry==="+TurnkeyUiMainActivity.getInstance().isUKCountry);
        if(TurnkeyUiMainActivity.getInstance().isUKCountry){
            UKChannelListDialog chListDialog = (UKChannelListDialog) ComponentsManager.getInstance()
                    .getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
            if (chListDialog != null && chListDialog.isVisible()) {
                chListDialog.dismiss();
            }
        } else {
            ChannelListDialog chListDialog = (ChannelListDialog) ComponentsManager.getInstance()
                    .getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
            if (chListDialog != null && chListDialog.isVisible()) {
                chListDialog.dismiss();
            }
        }

    }

    private void dismissGingaDialog() {
        GingaTvDialog gingaTvDialog = ((GingaTvDialog) ComponentsManager.getInstance()
                .getComponentById(NavBasicMisc.NAV_COMP_ID_GINGA_TV));
        if (gingaTvDialog != null && gingaTvDialog.isVisible()) {
            gingaTvDialog.dismiss();
        }
    }

    private void dismissGingaloadingDialog() {
        InfoBarDialog infoBar = ((InfoBarDialog) ComponentsManager.getInstance()
                .getComponentById(NavBasicMisc.NAV_COMP_ID_INFO_BAR));
        if (infoBar != null) {
            infoBar.handlerMessage(4);
        }
    }

    public void showToast(boolean isForwarding) {
        Toast toast = null;
        if (isForwarding) {
            toast = Toast.makeText(mContext, R.string.timeshfit_forwarding, Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(mContext, R.string.timeshfit_rewinding, Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public boolean initView() {
        View view = inflate(mContext, R.layout.tif_timeshift_layout, null);
        mPlayIcon = (ImageView) view.findViewById(
                R.id.tshift_plcontorl_btn);
        /*mPlayFNum = (ImageView) view.findViewById(
                R.id.tshift_plcontorl_btn_num);*/
        programStartTime = (TextView) view.findViewById(R.id.program_start_time);
        playingTime = (TextView) view.findViewById(R.id.tf_playing_time);
        programEndtime = (TextView) view.findViewById(R.id.program_end_time);
        start = (ImageView) view.findViewById(R.id.timeline_bg_start);
        watch = (ImageView) view.findViewById(R.id.watched);
        buffer = (ImageView) view.findViewById(R.id.buffered);
        end = (ImageView) view.findViewById(R.id.timeline_bg_end);
        indicator = (ImageView) view.findViewById(R.id.time_indicator);
        rLbackground = (RelativeLayout) view.findViewById(R.id.rl_background);
        setAlpha(0.9f);
        this.addView(view);
        mTimeshiftHandler = new TimeshiftInternalHandler(this);
        mHandler = new InternalHandler(TurnkeyUiMainActivity.getInstance());
        return super.initView();
    }

    @Override
    public void setVisibility(int visibility) {
        boolean isStart = SaveValue.getInstance(context).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setVisibility-->visibility: " + visibility+"---available:"+mTimeShiftManager.isAvailable()+";isStart ="+ isStart);


        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
//        if(mTimeShiftManager.isAvailable() && !isBack){
//            visibility = View.VISIBLE;
//        }

        if(!CommonIntegration.getInstance().is3rdTVSource()){

        if((!isStart) && (visibility == View.VISIBLE)){
            return;
        }

        if(!mTimeShiftManager.isAvailable()){
            visibility = View.GONE;
        }else {
            ArrayList<MountPoint> deviceList = DeviceManager.getInstance().getMountPointList();
            if(deviceList == null || deviceList.isEmpty()){
                visibility = View.GONE;
            }

            boolean isGone = true;
            if(deviceList != null && !deviceList.isEmpty()) {
                for (MountPoint point : deviceList) {

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mMountPoint: " + point.mMountPoint);

                    File file = new File(point.mMountPoint + "/tshift");
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempIsTSHIFT: " + file.exists());
                    if (file.exists()) {
                        isGone = false;
                    }
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isGone-------: " +   isGone);
            if(isGone){
                visibility = View.GONE;
            }

        }

        }
        super.setVisibility(visibility);
    }

  public void stopTifTimeShift() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopTifTimeShift-->start.");
    if (mTimeShiftManager != null) {
      mTimeShiftManager.stop();
      SaveValue.getInstance(context).setLocalMemoryValue(
          MenuConfigManager.TIMESHIFT_START, false);
      //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
      reset();
      //mPlayFNum.setVisibility(View.INVISIBLE);
      setVisibility(View.GONE);
    }
  }

    public void hideMenuOptions(){
        ComponentsManager mNavCompsMagr = ComponentsManager.getInstance();
        MenuOptionMain menuOptionMain = ((MenuOptionMain) mNavCompsMagr
                .getComponentById(NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG));
        if(menuOptionMain != null && menuOptionMain.isShowing()){
            menuOptionMain.setVisibility(View.GONE);
        }
    }

  private void reset() {
      mProgramStartTimeMs = 0;
      mProgramEndTimeMs = 0;
      mTFCallbackPlaybackStatus = -1;
  }

    private void layoutProgress(View progress, long progressStartTimeMs, long progressEndTimeMs) {
        int progressTime = Math.max(0, convertDurationToPixel(progressEndTimeMs - progressStartTimeMs));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress-->layoutProgress progressTime: " + getTimeStringForLog(progressTime));
        layoutProgress(progress, progressTime,progressEndTimeMs-progressStartTimeMs);
    }

    private void layoutProgress(View progress, int width,long duration) {
        int percent = PROGRESSBAR_WIDTH_AVAILABLE * 100 / PROGRESSBAR_WIDTH; // * 100 because of "a/b", a must more than b
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) progress.getLayoutParams();
        if(rLbackground.getWidth() > 0 && (mProgramEndTimeMs - mProgramStartTimeMs)!=0){
            params.width = (int)((duration * rLbackground.getWidth())/(mProgramEndTimeMs - mProgramStartTimeMs));
        }else {
            params.width = width * percent  / 100;
        }
        if(params.width < 0){
            params.width = 0;
        }
        progress.setLayoutParams(params);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "layoutProgress--: " +progress+"---:"+width+"---"+rLbackground.getWidth()+" , params.width= "+ params.width);
    }

    private int convertDurationToPixel(long duration) {
        if (mProgramEndTimeMs <= mProgramStartTimeMs) {
            return 0;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress-->convertDurationToPixel duration: " + getTimeStringForLog(duration) + ", -to: " +
                getTimeStringForLog(mProgramEndTimeMs - mProgramStartTimeMs));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress-->duration: " + duration + ", -to: " +
                (mProgramEndTimeMs - mProgramStartTimeMs));
        if((mProgramEndTimeMs - mProgramStartTimeMs) != 0) {
            int percent = (int) (duration * PROGRESSBAR_WIDTH / (mProgramEndTimeMs - mProgramStartTimeMs));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress-->convertDurationToPixel percent: " +
                    percent);
            return percent;
        }
        return 0;
    }

    private void initializeTimeline() {
        long currentPosition = mTimeShiftManager.getCurrentPositionMs();
        TIFProgramInfo program = mTimeShiftManager.getProgramAt(currentPosition);
        if (program != null) {
            mProgramStartTimeMs = program.getmStartTimeUtcSec();
            mProgramEndTimeMs = program.getmEndTimeUtcSec();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initializeTimeline, program: " + program + ", " + getTimeStringForLog(mProgramStartTimeMs) + "," +
                getTimeStringForLog(mTimeShiftManager.getCurrentPositionMs()) + "," + getTimeStringForLog(mProgramEndTimeMs));
    }

    int progressStartTimeMs = 1;
    private void updateProgress() {
        if(!mTimeshiftHandler.hasMessages(MSG_SYSTEM_TIME)){
            mTimeshiftHandler.sendEmptyMessage(MSG_SYSTEM_TIME);
        }
        long progressStartTimeMs = Math.min(mProgramEndTimeMs,
                Math.max(mProgramStartTimeMs, mTimeShiftManager.getRecordStartTimeMs()));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"end time =="+mProgramStartTimeMs+" , mTimeShiftManager.getCurrentPositionMs() = "+mTimeShiftManager.getCurrentPositionMs()+", mProgramEndTimeMs =="+mProgramEndTimeMs);
        long currentPlayingTimeMs = Math.min(mProgramEndTimeMs,
                Math.max(mProgramStartTimeMs, mTimeShiftManager.getCurrentPositionMs()));
        if(currentPlayingTimeMs >= mProgramEndTimeMs){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"need update start and end time");
            initializeTimeline();
            return;
        }
        long progressEndTimeMs = Math.min(mProgramEndTimeMs,
                Math.max(mProgramStartTimeMs, mSystemUtcTime/*System.currentTimeMillis()*/));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress getBroadcastTimeInUtcSeconds: " + mSystemUtcTime);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress System.currentTimeMillis(): " + System.currentTimeMillis() + ", " + getTimeStringForLog(System.currentTimeMillis()));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress mProgramStartTimeMs: " + getTimeStringForLog(mProgramStartTimeMs) + "," + getTimeStringForLog(mProgramEndTimeMs));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgress, " + getTimeStringForLog(progressStartTimeMs) + "," +
            getTimeStringForLog(currentPlayingTimeMs) + "," + getTimeStringForLog(progressEndTimeMs) + "," +
            getTimeStringForLog(mTimeShiftManager.getRecordStartTimeMs()) + "," +
            getTimeStringForLog(mTimeShiftManager.getCurrentPositionMs()) + "," +
            getTimeStringForLog(mSystemUtcTime));

        layoutProgress(start, mProgramStartTimeMs, progressStartTimeMs);
        layoutProgress(watch, progressStartTimeMs, currentPlayingTimeMs);
        layoutProgress(buffer, currentPlayingTimeMs, progressEndTimeMs);
        layoutProgress(end,progressEndTimeMs,mProgramEndTimeMs);
    }

    public void setListener(TifTimeShiftManager tifTimeShiftManager) {
        mTimeShiftManager = tifTimeShiftManager;

        mTimeShiftManager.setListener(new TifTimeShiftManager.Listener() {

            @Override
            public void onAvailabilityChanged() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onAvailabilityChanged");

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "libo_isAvailable"+mTimeShiftManager.isAvailable());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "libo_isTIMESHIFT_START"+SaveValue.getInstance(TurnkeyUiMainActivity.getInstance()).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START));

                if (!mTimeShiftManager.isAvailable()) {

                    if(SaveValue.getInstance(TurnkeyUiMainActivity.getInstance()).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {

                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "===sendEmptyMessageDelayed");

                        Message message = mHandler.obtainMessage();
                        message.arg1 = 77;
                        message.what = InternalHandler.MSG_TIME_SHIFT_NOT_AVAILABLE;
                        mHandler.sendMessage(message);

                        Message msg = mHandler.obtainMessage();
                        msg.arg1 = 88;
                        msg.what = InternalHandler.MSG_TIME_SHIFT_NOT_AVAILABLE;
                        mHandler.sendMessageDelayed(msg, 3000);
                    }

                    SaveValue.getInstance(mContext).setLocalMemoryValue(
                            MenuConfigManager.TIMESHIFT_START,
                            false);
                    //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
                    setVisibility(View.GONE);
                }
                initializeTimeline();
                // updateMenuVisibility();
                // PlayControlsRowView.this.onAvailabilityChanged();
            }


            @Override
            public void onPlayStatusChanged(int status) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPlayStatusChanged, status: " + status);

                mTFCallbackPlaybackStatus = status;

                // updateMenuVisibility();
                if (mTimeShiftManager.isAvailable() && mTimeShiftManager.getAvailabilityChanged()) {
                    initializeTimeline();
                    updateAll();
                }

                if (status == 4) { // auto play status
                    Toast.makeText(context, R.string.timeshfit_record_full, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTimeShiftSucceed() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTimeShiftSucceed" );

                mPlayIcon.setImageResource(R.drawable.tif_play_pause_icon);

                SaveValue.getInstance(mContext).setLocalMemoryValue(MenuConfigManager.TIMESHIFT_START,
                        true);
                //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, true, false);

                setVisibility(View.VISIBLE);
            }


            @Override
            public void onTimeShiftError(int errorStatus) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTimeShiftError:"+errorStatus);
                if(errorStatus != 1){
                    Toast.makeText(context, R.string.timeshift_not_available, Toast.LENGTH_SHORT).show();
                    mTimeShiftManager.speakText("timeshift is not available");
                    setVisibility(View.GONE);
                    SaveValue.getInstance(mContext).setLocalMemoryValue(MenuConfigManager.TIMESHIFT_START, false);
                    //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
                }
            }

            @Override
            public void onRecordStartTimeChanged() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRecordStartTimeChanged");
                // update for this cr DTV00840873
                //if (mTimeShiftManager.getmPlaybackSpeed() == 1) {
                    //mPlayFNum.setVisibility(View.INVISIBLE);
                //}
                if (!mTimeShiftManager.isAvailable()) {
                    return;
                }
                initializeTimeline();
                updateAll();
            }

            @Override
            public void onCurrentPositionChanged() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCurrentPositionChanged");
                if (!mTimeShiftManager.isAvailable()) {
                    return;
                }
//                initializeTimeline();
                updateAll();
            }

            @Override
            public void onProgramInfoChanged() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onProgramInfoChanged");
                if (!mTimeShiftManager.isAvailable()) {
                    return;
                }
                initializeTimeline();
                updateAll();
            }

            @Override
            public void onActionEnabledChanged(@TimeShiftActionId
            int actionId, boolean enabled) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActionEnabledChanged");
                // Move focus to the play/pause button when the PREVIOUS, NEXT, REWIND or
                // FAST_FORWARD button is clicked and the button becomes disabled.
                // No need to update the UI here because the UI will be updated by other callbacks.
                /*
                 * if (!enabled && ((actionId ==
                 * TifTimeShiftManager.TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS &&
                 * mJumpPreviousButton.hasFocus()) || (actionId ==
                 * TimeShiftManager.TIME_SHIFT_ACTION_ID_REWIND && mRewindButton.hasFocus()) ||
                 * (actionId == TimeShiftManager.TIME_SHIFT_ACTION_ID_FAST_FORWARD &&
                 * mFastForwardButton.hasFocus()) || (actionId ==
                 * TimeShiftManager.TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT &&
                 * mJumpNextButton.hasFocus()))) { mPlayPauseButton.requestFocus(); }
                 */
            }


            @Override
            public void onSpeedChange(float speed) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onSpeedChange speed====" + speed);
                if (speed == 1.0f) {
                    //mPlayFNum.setVisibility(View.INVISIBLE);
                    mPlayIcon.setImageResource(R.drawable.tif_play_icon);
                } else if (speed == 0.0f) {
                    //mPlayFNum.setVisibility(View.INVISIBLE);
                    mPlayIcon.setImageResource(R.drawable.tif_play_pause_icon);
                } else {
                    updateFastAndRewindIcon(isFastForward((int)speed), (int) speed);
                }
            }

            private boolean isFastForward(int speed) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFastOrRewindStatus speed=" + speed);
                return (speed>0)?true:false;
            }

            @Override
            public void onError(int errId) {
                switch(errId) {
                    case TifTimeShiftManager.ERROR_DATE_TIME:
                        mTimeshiftHandler.sendEmptyMessage(MSG_PAUSE_TIMESHIFT);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void updateAll() {
        updateTime();
        updateProgress();
        updateRecTimeText();
        updateButtons();
    }

    private void updateButtons() {
        if (CommonIntegration.getInstance().is3rdTVSource()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateButtons");
            int speed = mTimeShiftManager.getmPlaybackSpeed();
            int playStatus = mTimeShiftManager.getPlayStatus();
            int playDirection = mTimeShiftManager.getPlayDirection();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateButtons speed: " + speed + ", playStatus: " + playStatus);
            if (speed == 1) {
                //mPlayFNum.setVisibility(View.INVISIBLE);
                if (playStatus == TifTimeShiftManager.PLAY_STATUS_PLAYING) {
                    mPlayIcon.setImageResource(R.drawable.tif_play_icon);
                } else {
                    mPlayIcon.setImageResource(R.drawable.tif_play_pause_icon);
                }
            } else {
                updateFastAndRewindIcon(isFastForward(playDirection), speed);
            }
        }
    }

    private boolean isFastForward(int playDirection) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFastForward2 playDirection=" + playDirection);
        return playDirection == TifTimeShiftManager.PLAY_DIRECTION_BACKWARD?true:false;
    }

    private void updateTime() {
        long currentPositionMs = mTimeShiftManager.getCurrentPositionMs();

        playingTime.setText(getTimeString(currentPositionMs));
        int currentTimePositionPixel =
                convertDurationToPixel(currentPositionMs - mProgramStartTimeMs);
        int percent = PROGRESSBAR_WIDTH_AVAILABLE * 100 / PROGRESSBAR_WIDTH; // * 100 because of "a/b", a must more than b
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
        if(rLbackground.getWidth() > 0 && (mProgramEndTimeMs - mProgramStartTimeMs)!=0){
            params.setMarginStart((int)((currentPositionMs - mProgramStartTimeMs) *rLbackground.getWidth()/(mProgramEndTimeMs - mProgramStartTimeMs)));
        }else {
            params.setMarginStart(currentTimePositionPixel * percent  / 100);
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, params.width + " updateTime, " + currentPositionMs + "," +
            currentTimePositionPixel + "," + params.leftMargin);

        indicator.setLayoutParams(params);
    }

    private void updateRecTimeText() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateRecTimeText, " + getTimeStringForLog(mProgramStartTimeMs) +
            "," + getTimeStringForLog(mProgramEndTimeMs));
        programStartTime.setText(getTimeString(mProgramStartTimeMs));
        programEndtime.setText(getTimeString(mProgramEndTimeMs));
    }

    private String getTimeStringForLog(long timeMs) {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(timeMs);
    }

    private String getTimeString(long timeMs) {
        return DateFormat.getTimeFormat(getContext()).format(timeMs);
    }

    /*public void showFastPlayInfo2(boolean isFastForward, int speed) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFastPlayInfo2 FastForward: " + isFastForward + ", speed: " + speed);
        //mPlayFNum.setVisibility(View.VISIBLE);

        if (isFastForward) {
            //mPlayIcon.setImageResource(R.drawable.timeshift_ff);
            onFastForward(isFastForward, speed);
        } else {
            //mPlayIcon.setImageResource(R.drawable.timeshift_fb);
            onRewind(isFastForward, speed);
        }

    }*/

    public void updateFastAndRewindIcon(boolean isFastForward, int speed) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateFastAndRewindIcon isFastForward:" + isFastForward + ", speed="+ speed);
        if (speed == 1) {
            //mPlayFNum.setVisibility(View.INVISIBLE);
            mPlayIcon.setImageResource(R.drawable.tif_play_icon);
            return;
        }
        int switchValue = speed;
        if (switchValue < 0) {
            switchValue = -switchValue;
        }

        switch (switchValue) {
            case 2:
                mPlayIcon.setImageResource(isFastForward?
                        R.drawable.tif_play_2speed_icon:R.drawable.tif_play_speed_2_icon);
                break;
            case 4:
                mPlayIcon.setImageResource(isFastForward?
                        R.drawable.tif_play_4speed_icon:R.drawable.tif_play_speed_4_icon);
                break;
            case 8:
                mPlayIcon.setImageResource(isFastForward?
                        R.drawable.tif_play_8speed_icon:R.drawable.tif_play_speed_8_icon);
                break;
            case 16:
                mPlayIcon.setImageResource(isFastForward?
                        R.drawable.tif_play_16speed_icon:R.drawable.tif_play_speed_16_icon);
                break;
            case 32:
                mPlayIcon.setImageResource(isFastForward?
                        R.drawable.tif_play_32speed_icon:R.drawable.tif_play_speed_32_icon);
                break;
            default:
                break;
        }
    }

    /**
     * TimeshiftInternalHandler
     */
    private static class TimeshiftInternalHandler extends WeakHandler<TifTimeshiftView> {
        public TimeshiftInternalHandler(TifTimeshiftView ref) {
            super(ref);
        }
        @Override
        public void handleMessage(Message msg, @NonNull TifTimeshiftView timeShiftview) {
            switch (msg.what) {
                case MSG_PAUSE_TIMESHIFT:
                    if(DvrManager.getInstance().getTTSEnable()){
                        timeShiftview.mTimeShiftManager.speakText("timeshift stop");
                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    timeShiftview.mTimeShiftManager.stop();
                    SaveValue.getInstance(timeShiftview.context).setLocalMemoryValue(
                            MenuConfigManager.TIMESHIFT_START,false);
                    //SaveValue.saveWorldBooleanValue(timeShiftview.context, MenuConfigManager.TIMESHIFT_START, false, false);
                    //timeShiftview.mPlayFNum.setVisibility(View.INVISIBLE);
                    timeShiftview.setVisibility(View.GONE);
                    break;
                case MSG_SYSTEM_TIME:
                    timeShiftview.mSystemUtcTime = timeShiftview.mTimeShiftManager.getBroadcastTimeInUtcSeconds();
                    if(MenuConfigManager.getInstance(timeShiftview.mContext).getDefault(MenuConfigManager.SETUP_SHIFTING_MODE)==1) {
                        sendEmptyMessageDelayed(MSG_SYSTEM_TIME, 1000);
                    }
                    break;
                 default:
                    break;
            }
        }
    }


}
