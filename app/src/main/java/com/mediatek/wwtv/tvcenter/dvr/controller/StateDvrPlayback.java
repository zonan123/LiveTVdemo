package com.mediatek.wwtv.tvcenter.dvr.controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentUris;
import android.media.tv.TvTrackInfo;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.PlaybackParams;

import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckCallback;
import com.android.tv.menu.MenuOptionMain;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.TvBlockView;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.manager.Util;
import com.mediatek.wwtv.tvcenter.dvr.ui.DVRControlbar;
import com.mediatek.wwtv.tvcenter.dvr.ui.PinDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.MiscView;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.util.AudioBTManager;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.ScreenStatusManager;
import com.mediatek.wwtv.tvcenter.util.SomeArgs;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;

/**
 * the class use to control the dvr file playback and display playback status .
 *
 * @author sin_yajuncheng
 */
public class StateDvrPlayback extends StateBase implements ComponentStatusListener.ICStatusListener  {// NOPMD

    private final static String TAG = "StateDvrPlayback";

    public static StateDvrPlayback mStateDvrPlayback;

    private final static int MSG_CTRLBAR_DISMISS = 0x110;
    private final static int MSG_REFRESH_TIME = 0x111;
    private final static int MSG_DELAY_TTS = 0x112;
    private final static int MSG_SPEAK_DVR_PLAY = 0x113;

//    private final TvView mTvView = null;

    // dvr file base infos
    private Uri fileUri;
    private String fileName;
    private String progarmName;
    private long fileTotalTime;
    private long fileCurrentTime;// curent time
    private long fileHistoryTime;// play history time
    private float filePlaySpeed;
    private int mSpeedStepTemp;
    private boolean isPlaying = true;// current play state
    private boolean isPlayStart = false; // record if play already started .
    private boolean isReplayOK = true; // only replay done can dispatch key
                                       // event ,otherwise
                                       // interrupt key event.
    private boolean isUnlockPin = false;// weather need show pin dialog
    private boolean isFForFR = false;
    private boolean isSForSR = false;

    // control bar ui
    private DVRControlbar mControlbar;// control bar
    //private TextView name_tv;
    private TextView time_tv;
    //private ImageView rewind_iv;
    private ImageView rewindSpeed_iv;
    private ImageView playOrPause_iv;
    //private ImageView forward_iv;
    private ImageView forwardSpeed_iv;
    private ProgressBar progressBar;
    private TextView subtitleName_tv;
    private TextView audioName_tv;

    private PinDialog dialog;

    private DvrPlaybackHnadler mHandler;
    private Message msg;

    private int subtitleTrackIndex = 0;

    private List<TvTrackInfo> subTitleTrackInfos = null;
    private TvTrackInfo currentTvTrackInfo;
    private String subTitleTrackName = "";
    private String subTitleTrackId;
    private String subTitleTypeString="";

    private int audioTrackIndex = -1; // because the first time is updated
//    private boolean showAudioTrackFirst = true; // show audio track info in
                                                // first time.

    private List<TvTrackInfo> audioTrackInfos = null;
    private String audioTrackName;
    private String audioString="";
    private String audioTrackId;
    private LanguageUtil mLanguageUtil;
    private boolean iskeywithback = false;
    private boolean enableswitch = false;
//    private MtkTvPWDDialog mtkTvPwd;
    private final CallbackHandler callbackHandler = new CallbackHandler(this);
    private PinDialogFragment mPinDialogFragment;
    private int isaudioonly = 0;
    private boolean isSeek = false;
    private int updateDuration=0;

    private StateDvrPlayback(Context context, DvrManager manager) {
        super(context, manager);
        setType(StatusType.PLAYBACK);
        mLanguageUtil = new LanguageUtil(TurnkeyUiMainActivity.getInstance());

        // getController().addEventHandler(callbackHandler);
        if (callbackHandler == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "callbackHandler = null");
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "callbackHandler = " + callbackHandler);
        }
        ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_INPUTS_PANEL_SHOW,
                this);

    }

    /**
     * singleton
     *
     * @return
     */
    public static StateDvrPlayback getInstance() {
        return mStateDvrPlayback;
    }

    /**
     * singleton - StateDvrPlayback
     *
     * @param context
     * @param dvrManager
     * @return
     */
    public synchronized static StateDvrPlayback getInstance(Context context,
            DvrManager dvrManager) {

        if (mStateDvrPlayback == null){
            dvrManager = DvrManager.getInstance();// must keep manage in latest.
            mStateDvrPlayback = new StateDvrPlayback(context, dvrManager);
            }
        return mStateDvrPlayback;
    }

    public static StateDvrPlayback getInstance(DvrManager dvrManager) {
        mStateDvrPlayback = new StateDvrPlayback(TurnkeyUiMainActivity.getInstance(),
                dvrManager);
        return mStateDvrPlayback;
    }

    @Override
    public void free() {
        mStateDvrPlayback = null;
    }

    private TvSurfaceView getTvView() {
        return TurnkeyUiMainActivity.getInstance().getTvView();
    }

    /*--------------------------------------------------------------------*/
    /**
     * show dvr play info (big bar )
     */
    private void initBigCtrlBar() {
        mControlbar = new DVRControlbar((Activity) mContext,
                R.layout.dvr_playback_ctrlbar, 10L * 1000, mStateDvrPlayback);

        TextView nametv = (TextView) findView(R.id.name_tv);
        time_tv = (TextView) findView(R.id.time_tv);
        ImageView rewindiv = (ImageView) findView(R.id.rewind_iv);
        rewindSpeed_iv = (ImageView) findView(R.id.rewindSpeed_iv);
        playOrPause_iv = (ImageView) findView(R.id.playOrPause_iv);
        ImageView forwardiv = (ImageView) findView(R.id.forward_iv);
        forwardSpeed_iv = (ImageView) findView(R.id.forwardSpeed_iv);
        progressBar = (ProgressBar) findView(R.id.progressBar);
        subtitleName_tv = (TextView) findView(R.id.subtitleName_tv);
        audioName_tv = (TextView) findView(R.id.audioName_tv);

        progressBar.incrementProgressBy(1);
        progressBar.setProgress(0);
        
        nametv.setText(progarmName);
        nametv.setSelected(true);
        
        rewindiv.setVisibility(View.GONE);
        rewindSpeed_iv.setVisibility(View.GONE);

        forwardiv.setVisibility(View.GONE);
        forwardSpeed_iv.setVisibility(View.GONE);

        playOrPause_iv.setVisibility(View.VISIBLE);
        playOrPause_iv.setImageResource(R.drawable.dvr_play_icon);
        subtitleName_tv.setVisibility(View.INVISIBLE);
        audioName_tv.setVisibility(View.INVISIBLE);

        showBigCtrlBar();

    }

    /**
     * findviewbyId(id)
     */
    private View findView(int id) {
        return mControlbar.getContentView().findViewById(id);
    }


    private void showBigCtrlBar() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "showBigCtrlBar start--->");
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        // will do nothing in google pip mode on UI .
        if (TurnkeyUiMainActivity.getInstance().isInPictureInPictureMode()) {
            return;
        }

        if (mControlbar == null) {
            initBigCtrlBar();
            return;
        }
        // update for this cr DTV00840602
        // dismissBigCtrlBar();

        if (fileCurrentTime == 0 && fileTotalTime == 0) {
            time_tv.setText(mContext.getString(R.string.pvr_play_start_time));
            progressBar.setProgress(0);
        }

        if (subTitleTrackName != null && !subTitleTrackName.isEmpty()) {
            subtitleName_tv.setVisibility(View.VISIBLE);
            if(!getSubtitleType().isEmpty()){
                subtitleName_tv.setText(String.format("%s(%s)", subTitleTrackName, getSubtitleType()));
            }else{
                subtitleName_tv.setText(subTitleTrackName);
            }
        } else {
            subtitleName_tv.setVisibility(View.INVISIBLE);
        }
        if (audioTrackName != null && !audioTrackName.isEmpty()) {
            audioName_tv.setVisibility(View.VISIBLE);
            if(audioString.isEmpty()){
                audioName_tv.setText(audioTrackName);
            }else{
                audioName_tv.setText(String.format("%s%s", audioTrackName, audioString));
            }

        } else {
            audioName_tv.setVisibility(View.INVISIBLE);
        }

        if (isSForSR) {
            switch (mSpeedStepTemp) {
            case 0:
            case 1:
                // rewind_iv.setVisibility(View.GONE);
                rewindSpeed_iv.setVisibility(View.GONE);

                // forward_iv.setVisibility(View.GONE);
                forwardSpeed_iv.setVisibility(View.GONE);

                playOrPause_iv.setVisibility(View.VISIBLE);
                if (filePlaySpeed == 1) {

                    playOrPause_iv.setImageResource(R.drawable.dvr_play_icon);
                } else if (filePlaySpeed == 0) {
                    playOrPause_iv
                            .setImageResource(R.drawable.dvr_play_pause_icon);
                }

                break;
            case 2:
            case 4:
            case 8:
            case 16:
            case 32:

                // rewind_iv.setVisibility(View.GONE);
                rewindSpeed_iv.setVisibility(View.GONE);

                // forward_iv.setVisibility(View.VISIBLE);
                forwardSpeed_iv.setVisibility(View.VISIBLE);

                playOrPause_iv.setVisibility(View.GONE);

                if (mSpeedStepTemp == 2) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_12_icon);
                } else if (mSpeedStepTemp == 4) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_14_icon);
                } else if (mSpeedStepTemp == 8) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_playback_speed_18_icon);
                } else if (mSpeedStepTemp == 16) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_116_icon);
                } else if (mSpeedStepTemp == 32) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_132_icon);
                }

                break;

            case -2:
            case -4:
            case -8:
            case -16:
            case -32:

                // rewind_iv.setVisibility(View.VISIBLE);
                rewindSpeed_iv.setVisibility(View.VISIBLE);

                // forward_iv.setVisibility(View.GONE);
                forwardSpeed_iv.setVisibility(View.GONE);

                playOrPause_iv.setVisibility(View.GONE);

                if (mSpeedStepTemp == -2) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_12_speed_icon);
                } else if (mSpeedStepTemp == -4) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_14_speed_icon);
                } else if (mSpeedStepTemp == -8) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_18_speed_icon);
                } else if (mSpeedStepTemp == -16) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_116_speed_icon);
                } else if (mSpeedStepTemp == -32) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_132_speed_icon);
                }

                break;

            default:
                break;
            }
        } else {
            switch (mSpeedStepTemp) {
            case 0:
            case 1:
                // rewind_iv.setVisibility(View.GONE);
                rewindSpeed_iv.setVisibility(View.GONE);

                // forward_iv.setVisibility(View.GONE);
                forwardSpeed_iv.setVisibility(View.GONE);

                playOrPause_iv.setVisibility(View.VISIBLE);
                if (filePlaySpeed == 1) {

                    playOrPause_iv.setImageResource(R.drawable.dvr_play_icon);
                } else if (filePlaySpeed == 0) {
                    playOrPause_iv
                            .setImageResource(R.drawable.dvr_play_pause_icon);
                }

                break;

            case 2:
            case 4:
            case 8:
            case 16:
            case 32:

                // rewind_iv.setVisibility(View.GONE);
                rewindSpeed_iv.setVisibility(View.GONE);

                // forward_iv.setVisibility(View.VISIBLE);
                forwardSpeed_iv.setVisibility(View.VISIBLE);

                playOrPause_iv.setVisibility(View.GONE);

                if (mSpeedStepTemp == 2) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_2speed_icon);
                } else if (mSpeedStepTemp == 4) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_4speed_icon);
                } else if (mSpeedStepTemp == 8) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_8speed_icon);
                } else if (mSpeedStepTemp == 16) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_16speed_icon);
                } else if (mSpeedStepTemp == 32) {
                    forwardSpeed_iv
                            .setImageResource(R.drawable.dvr_play_32speed_icon);
                }

                break;

            case -2:
            case -4:
            case -8:
            case -16:
            case -32:

                // rewind_iv.setVisibility(View.VISIBLE);
                rewindSpeed_iv.setVisibility(View.VISIBLE);

                // forward_iv.setVisibility(View.GONE);
                forwardSpeed_iv.setVisibility(View.GONE);

                playOrPause_iv.setVisibility(View.GONE);

                if (mSpeedStepTemp == -2) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_2_icon);
                } else if (mSpeedStepTemp == -4) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_4_icon);
                } else if (mSpeedStepTemp == -8) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_8_icon);
                } else if (mSpeedStepTemp == -16) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_16_icon);
                } else if (mSpeedStepTemp == -32) {
                    rewindSpeed_iv
                            .setImageResource(R.drawable.dvr_play_speed_32_icon);
                }

                break;

            default:
                break;
            }
        }

        mControlbar.show();
        mHandler.removeMessages(MSG_CTRLBAR_DISMISS);
        msg = mHandler.obtainMessage(MSG_CTRLBAR_DISMISS);
        mHandler.sendMessageDelayed(msg, 10 * 1000L);

    }

    public void dismissBigCtrlBar() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismissBigCtrlBar-->");
        if (mControlbar != null && mControlbar.isShowing()){
            mControlbar.dismiss();
            }
    }

    /**
     * refresh time info
     */
    private void refreshTime() {
        updateTimeProgressBar();
        msg = mHandler.obtainMessage(MSG_REFRESH_TIME);
        mHandler.sendMessageDelayed(msg, 1000L);
com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"time-->"+time_tv.getText());
    }

    /**
     * update time and progress info in UI
     */
    private void updateTimeProgressBar() {

        StringBuilder sb = new StringBuilder();
        String tt = Util.longStrToTimeStr(fileTotalTime);
        String cr = Util.longStrToTimeStr(fileCurrentTime);
        // sb.append("[");
        sb.append(cr);
        sb.append("/");
        sb.append(tt);
        // sb.append("]");
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "controlBarInfo==> " + "time*** " + sb.toString());
        if(time_tv!=null){
        time_tv.setText(sb.toString());
        }
        if (fileTotalTime != 0L) {
            long pr = 1000 * fileCurrentTime / fileTotalTime;
            int pro = (int) Math.floor((double) pr);
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "pro=" + pro);
            progressBar.setProgress(pro);
        } else {
            progressBar.setProgress(0);
        }
    }

    @Override
    public boolean onKeyDown(int keycode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keycode==" + keycode);
        if (isSeek) {
            return true;
        }
        if (!isReplayOK) {
            // function preparing before replay ok .
            getManager().showContinueToSeekNotSupport();
            return true;
        }
        // Check if file locked
        if (isUnlockPin && keycode != KeyMap.KEYCODE_MTKIR_STOP
                && keycode != KeyMap.KEYCODE_BACK) {
            mPinDialogFragment.setShowing(true);
            mPinDialogFragment.show(TurnkeyUiMainActivity.getInstance()
                    .getFragmentManager(), "PinDialogFragment");
            return true;
        }

        switch (keycode) {
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
        case KeyMap.KEYCODE_MTKIR_RECORD:
            // case KeyMap.KEYCODE_MTKIR_EJECT:
        case KeyMap.KEYCODE_DPAD_UP:
        case KeyMap.KEYCODE_DPAD_DOWN:
        case KeyMap.KEYCODE_MTKIR_CHUP:
        case KeyMap.KEYCODE_MTKIR_CHDN:
        case KeyMap.KEYCODE_MTKIR_PRECH:
        case KeyMap.KEYCODE_MTKIR_GUIDE:
        case KeyMap.KEYCODE_MTKIR_FREEZE:
        case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
        case KeyMap.KEYCODE_MTKIR_PIPPOP:
        case KeyMap.KEYCODE_MTKIR_MTKIR_TTX:
            case KeyMap.KEYCODE_MTKIR_LIST:
         case KeyMap.KEYCODE_YEN:
            return true;
        case KeyMap.KEYCODE_MTKIR_INFO:
            showBigCtrlBar();
            return true;
        case KeyMap.KEYCODE_DPAD_CENTER:
        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        case KeyMap.KEYCODE_MTKIR_PLAY:
        case KeyMap.KEYCODE_MTKIR_PAUSE:
            // play or pause
            if (isPlayStart) {
                if (isPlaying
                        && ((!isFForFR && !isSForSR) || keycode == KeyMap.KEYCODE_MTKIR_PAUSE)) {
                    getController().dvrPause();//pauseDvrFilePlay();
                } else if (isFForFR || isSForSR) {
                    filePlaySpeed = 1;
                    mSpeedStepTemp = 1;
                    speakText("dvr play");
                    forwardDvrFilePlay(filePlaySpeed);
                } else {
                    // pause state
                    reSumeDvrFilePlay();
                }
            } /*else {
                // playDvrFile(fileUri);
            }*/
            isFForFR = false;
            isSForSR = false;
            dismissSundryView();
            dismissMiscView();
            showBigCtrlBar();
            return true;
        case KeyMap.KEYCODE_BACK:
            MenuOptionMain main = (MenuOptionMain) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG);
            if (main != null && main.isVisible()) {
                main.setVisibility(View.INVISIBLE);
                return true;
            }
            iskeywithback = true;
            mHandler.removeMessages(MSG_REFRESH_TIME);
            stopDvrFilePlay();
            break;
        case KeyMap.KEYCODE_MTKIR_STOP:
            // stop play
            iskeywithback = true;
            mHandler.removeMessages(MSG_REFRESH_TIME);
            stopDvrFilePlay();
            // ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER).startComponent();
            return true;
        case KeyMap.KEYCODE_MTKIR_RED:
//            if(keycode==KeyMap.KEYCODE_MTKIR_RED){
//                break;
//            }
//            if (mSpeedStepTemp >= 0 || filePlaySpeed < -1) {
//                mSpeedStepTemp = -1;
//                mSpeedStepTemp <<= 1;
//            } else {
//                if (mSpeedStepTemp * 2 > -64) {
//                    mSpeedStepTemp <<= 1;
//                } else {
//                    mSpeedStepTemp = 1;
//                }
//            }
//
//            switch (mSpeedStepTemp) {
//            case -2:
//                filePlaySpeed = -0.5f;// 1/2;//
//                break;
//            case -4:
//                filePlaySpeed = -0.25f;// 1/4;//
//                break;
//            case -8:
//                filePlaySpeed = -0.125f;// 1/8;//
//                break;
//            case -16:
//                filePlaySpeed = -0.0625f;// 1/16;//
//                break;
//            case -32:
//                filePlaySpeed = -0.03125f;// 1/32;//
//                break;
//            default:
//                filePlaySpeed = 1;
//                break;
//            }
//            // slow rewind ---
//            isSForSR = true;
//            isFForFR = false;
//            isPlaying = true;
//            speakText("dvr slow rewind");
//            rewindDvrFilePlay(filePlaySpeed);
//            dismissMiscView();
//            dismissSundryView();
            // showBigCtrlBar();
            return true;
        case KeyMap.KEYCODE_MTKIR_EJECT:
//            if(keycode==KeyMap.KEYCODE_MTKIR_EJECT){
//                break;
//            }
//            if (mSpeedStepTemp <= 0 || filePlaySpeed > 1) {
//                mSpeedStepTemp = 1;
//                mSpeedStepTemp <<= 1;
//            } else {
//                if (mSpeedStepTemp * 2 < 64) {
//                    mSpeedStepTemp <<= 1;
//                } else {
//                    mSpeedStepTemp = 1;
//                }
//            }
//
//            switch (mSpeedStepTemp) {
//            case 2:
//                filePlaySpeed = 0.5f;// 1/2;//
//                break;
//            case 4:
//                filePlaySpeed = 0.25f;// 1/4;//
//                break;
//            case 8:
//                filePlaySpeed = 0.125f;// 1/8;//
//                break;
//            case 16:
//                filePlaySpeed = 0.0625f;// 1/16;//
//                break;
//            case 32:
//                filePlaySpeed = 0.03125f;// 1/32;//
//                break;
//            default:
//                filePlaySpeed = 1;
//                break;
//            }
//            // slow forward ++++
//            isSForSR = true;
//            isFForFR = false;
//            isPlaying = true;
//            speakText("dvr slow forward");
//            forwardDvrFilePlay(filePlaySpeed);
//            dismissMiscView();
//            dismissSundryView();
//            // showBigCtrlBar();
            return true;
        case KeyMap.KEYCODE_MTKIR_REWIND:
            // rewind ---
            if (filePlaySpeed >= -1) {
                filePlaySpeed = -2;
               // mSpeedStepTemp = -2;
                // current is forward state
                /*
                 * if (filePlaySpeed == 1) { filePlaySpeed = -2; } else if
                 * (filePlaySpeed == 0) { // pause state filePlaySpeed = -2; }
                 * else { filePlaySpeed = -2; //filePlaySpeed / 2; }
                 */

            } else {
                // current is in rewind state
                if (filePlaySpeed * 2 > -64) {
                    filePlaySpeed = filePlaySpeed * 2;
                    //mSpeedStepTemp = mSpeedStepTemp * 2;
                } else {
                    filePlaySpeed = 1;
                    //mSpeedStepTemp = 1;
                }
            }
            isFForFR = true;
            isSForSR = false;
            //isPlaying = true;
            speakText("dvr rewind");
            rewindDvrFilePlay(filePlaySpeed);
            dismissMiscView();
            dismissSundryView();
            // showBigCtrlBar();
            return true;

        case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
            // forward ++++

            if (filePlaySpeed <= 1) {
                filePlaySpeed = 2;
               // mSpeedStepTemp = 2;
                // current is in rewind state
                /*
                 * if (filePlaySpeed == 0) { // pause state filePlaySpeed = 2; }
                 * else if (filePlaySpeed == -2) { filePlaySpeed = 2; } else {
                 * filePlaySpeed = 2; // filePlaySpeed / 2; }
                 */

            } else {
                // current is in forward state
                if (filePlaySpeed * 2 < 64) {
                    filePlaySpeed = filePlaySpeed * 2;
                    //mSpeedStepTemp = mSpeedStepTemp * 2;
                } else {
                    filePlaySpeed = 1;
                    //mSpeedStepTemp = 1;
                }
            }
            isFForFR = true;
            isSForSR = false;
            //isPlaying = true;
            speakText("dvr forward");
            forwardDvrFilePlay(filePlaySpeed);
            dismissMiscView();
            dismissSundryView();
            // showBigCtrlBar();
            return true;
        case KeyMap.KEYCODE_DPAD_LEFT:
            if (TurnkeyUiMainActivity.getInstance()!=null && TurnkeyUiMainActivity.getInstance().getTvOptionsManager() != null
                    && TurnkeyUiMainActivity.getInstance().getTvOptionsManager()
                    .isShowing()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Menu is show, and can not seek left pvr.");
                dismissBigCtrlBar();
                break;
            }
            // seek back
            if (prepareSeekAction()) {
                return true;
            }
            if (fileCurrentTime - 30 * 1000L <= 0) {
                fileCurrentTime = 0L;
                // Toast.makeText(mContext, "File playback !",
                // Toast.LENGTH_LONG).show();
            } else {
                fileCurrentTime = fileCurrentTime - 30 * 1000L;
            }
            isReplayOK = false;
            seekDvrFilePlay(fileCurrentTime);
            showBigCtrlBar();
            return true;

        case KeyMap.KEYCODE_DPAD_RIGHT:
            if (TurnkeyUiMainActivity.getInstance()!=null && TurnkeyUiMainActivity.getInstance().getTvOptionsManager() != null
                    && TurnkeyUiMainActivity.getInstance().getTvOptionsManager()
                    .isShowing()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Menu is show, and can not seek right pvr.");
                dismissBigCtrlBar();
                break;
            }
            // seek next
            if (prepareSeekAction()) {
                return true;
            }
            if (fileCurrentTime + 30 * 1000L > fileTotalTime) {
                // file total time too short
                Toast.makeText(mContext, mContext.getString(R.string.pvrplay_file_deficiency),
                        Toast.LENGTH_LONG).show();
            } else {
                fileCurrentTime = fileCurrentTime + 30 * 1000L;
                isReplayOK = false;
                seekDvrFilePlay(fileCurrentTime);
            }
            showBigCtrlBar();
            return true;

        case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
        case KeyMap.KEYCODE_MTKIR_SUBTITLE:
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,
                    "keycode KEYCODE_MTKIR_MTKIR_CC  or  KEYCODE_MTKIR_SUBTITLE");
            switchCc();
            showBigCtrlBar();
            return true;

        case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " KEYCODE_MTKIR_MTSAUDIO ");
            // audio track
            switchAudioTrack();
            dismissSundryView();
            dismissMiscView();
            showBigCtrlBar();
            return true;

        case KeyMap.KEYCODE_MTKIR_MUTE:
            return false;

        case KeyMap.KEYCODE_MTKIR_TIMER:
        case KeyMap.KEYCODE_MTKIR_SLEEP:
        case KeyMap.KEYCODE_MTKIR_ZOOM:
        case KeyMap.KEYCODE_MTKIR_PEFFECT:
        case KeyMap.KEYCODE_MTKIR_SEFFECT:
        case KeyMap.KEYCODE_MTKIR_ASPECT:
        case KeyMap.KEYCODE_MTKIR_PIPSIZE:
        case KeyMap.KEYCODE_MENU:
            TwinkleDialog.hideTwinkle();
            dismissBigCtrlBar();
            return false;
        default:
            break;
        }
        return super.onKeyDown(keycode);
    }

    private void dismissSundryView() {
        // TODO Auto-generated method stub
        SundryShowTextView sundryShowTextView = ((SundryShowTextView) ComponentsManager
                .getInstance()
                .getComponentById(NavBasicMisc.NAV_COMP_ID_SUNDRY));
        if (sundryShowTextView != null && sundryShowTextView.isVisible()) {
            sundryShowTextView.setVisibility(View.GONE);
        }
    }

    private void dismissMiscView() {
        // TODO Auto-generated method stub
        MiscView miscView = ((MiscView) ComponentsManager.getInstance()
                .getComponentById(NavBasicMisc.NAV_COMP_ID_MISC));
        if (miscView != null && miscView.isVisible()) {
            miscView.setVisibility(View.GONE);
        }
    }

    public void prepareDvrFilePlay(DVRFiles dvrFiles) {
        getController().addEventHandler(callbackHandler);
        getController().setmDvrCallback();
        mHandler = new DvrPlaybackHnadler();

        this.setRunning(true);
        getDvrBaseInfo(dvrFiles);
        getDvrFileState();

        if (isNeedLastMemory()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Black screen when seek pvr.");
            isSeek = true;
            TurnkeyUiMainActivity
                    .getInstance()
                    .getBlockScreenView()
                    .setVisibility(View.VISIBLE,
                            TvBlockView.BLOCK_PVR_LAST_MEMORY_BLOCK);
        }

        //if (isNeedLastMemory()) {
            // Bundle data = new Bundle();
            // data.putInt("seekValue", (int)fileHistoryTime);
            // com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "send cmd session_event_dvr_lastmemory_play_seek");
            // getController().sendDvrPlaybackCommand("session_event_dvr_lastmemory_play_seek",
            // data);
            // mute audio
            // getTvView().sendAppPrivateCommand("APP_PRIVATE_COMMAND_FOR_MUTE_AUDIO",
            // null);
        //}
        playDvrFile(fileUri);
    }

    public boolean isNeedLastMemory() {
        return fileHistoryTime != 0 && fileHistoryTime < fileTotalTime;
    }

    private void playDvrFile(Uri recordedProgramUri) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "playDvrFile play dvr");
        if (!mHandler.hasMessages(MSG_SPEAK_DVR_PLAY)) {
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(MSG_SPEAK_DVR_PLAY), 500);
        }
        SaveValue.saveWorldBooleanValue(mContext,
                MenuConfigManager.PVR_PLAYBACK_START, true, false);
        ScreenStatusManager.getInstance().setScreenOn(((TurnkeyUiMainActivity)mContext)
                .getWindow(),ScreenStatusManager.SCREEN_ON_PVR_PLAY);
        // 1 last memory 2 prepare ui 3 begin & save status
        // speakText("dvr play");
        Uri newrecordedProgramUri = ContentUris.withAppendedId(recordedProgramUri,isNeedLastMemory()?1:0);
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"newrecordedProgramUri->"+newrecordedProgramUri.toString());
        getController().dvrPlay(newrecordedProgramUri, isNeedLastMemory());
        createAudioPatchAsync(mContext.getApplicationContext());
    }

    private static void createAudioPatchAsync(Context context){
        TVAsyncExecutor.getInstance().execute(() -> AudioBTManager.getInstance(context).createAudioPatch());
    }

    private void pauseDvrFilePlay() {
        // reset ui
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "pause dvr pause");
        filePlaySpeed = 0;
        mSpeedStepTemp = 0;
        speakText("dvr pause");
        //mHandler.removeMessages(MSG_REFRESH_TIME);// MSG_REFRESH_TIME
       // getController().dvrPause();
        isPlaying = false;
        showBigCtrlBar();
    }

    private void reSumeDvrFilePlay() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " resume dvr file");
        // update for issue DTV00859262
        speakText("dvr play");
        filePlaySpeed = 1;
        mSpeedStepTemp = 1;
        showBigCtrlBar();
        if (!isUnlockPin) {
            refreshTime();
        } else{
            mHandler.sendEmptyMessage(MSG_REFRESH_TIME);
            }
        getController().dvrResume();
        isPlaying = true;

    }

    public void stopDvrFilePlay(){
        getController().resetsetmDvrCallback();
        stopDvrFilePlay(false);
    }
    /**
     * stop playback and select TV source same time
     */
    public void stopDvrFilePlay(boolean isLivetvstop) {
        // save play history 2 toast unsave status 3 exit
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "stop dvr stop");
        if(mPinDialogFragment!=null&&mPinDialogFragment.isShowing()) {
            mPinDialogFragment.dismiss();
        }
        saveStopMessages();
        if (TurnkeyUiMainActivity.getInstance().isInPictureInPictureMode()) {
            TurnkeyUiMainActivity.getInstance().finish();
        }
        TurnkeyUiMainActivity
                .getInstance()
                .getBlockScreenView()
                .setVisibility(View.GONE,
                        TvBlockView.BLOCK_PVR_LAST_MEMORY_BLOCK);
        ScreenStatusManager.getInstance().setScreenOff(((TurnkeyUiMainActivity)mContext)
                .getWindow(),ScreenStatusManager.SCREEN_ON_PVR_PLAY);
        if (!DestroyApp.isCurActivityTkuiMainActivity() && iskeywithback) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "Current Activity is not TkuiMainActivity.");
            TurnkeyUiMainActivity.getInstance().resumeTurnkeyActivity(mContext);
            iskeywithback = false;
        }
      if(!isLivetvstop) {
          String sourceName = InputSourceManager.getInstance()
                  .getCurrentInputSourceName(MtkTvInputSource.INPUT_OUTPUT_MAIN);
          int detail = InputSourceManager.getInstance()
                  .changeCurrentInputSourceByName(sourceName);
          com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "sourceName ==" + sourceName + "  detail ==" + detail);
      }
        CIMainDialog ciMainDialog = (CIMainDialog) ComponentsManager
                .getInstance().getComponentById(NavBasic.NAV_COMP_ID_CI_DIALOG);
        if (ciMainDialog!=null&&ciMainDialog.isShowing()){
            ciMainDialog.dismiss();
            }
        MenuOptionMain main = (MenuOptionMain) ComponentsManager
                                .getInstance().getComponentById(
                                    NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG);
        if (main != null && main.isVisible()) {
            main.setVisibility(View.INVISIBLE);
        }
        isaudioonly = 0;
    }

    /**
     * only used to stop playback state ,but not select TV source for some
     * exceptional circumstances.
     */
   public void saveStopMessages() {
        if (dialog != null) {
            dialog.dismiss();
        }
        setDvrFileState(fileName, fileCurrentTime);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "setDvrFileState == " + fileName + " -- "
                + fileCurrentTime);
        isPlaying = false;
        isFForFR = false;
        isSForSR = false;
        fileCurrentTime = 0;
        dismissBigCtrlBar();
        getManager().speakText("dvr stop");
        getController().dvrStop();
        mHandler.removeCallbacksAndMessages(null);
       ComponentStatusListener.getInstance().removeListener(this);
        getController().removeEventHandler(callbackHandler);
        this.setRunning(false);
        getManager().restoreToDefault(mStateDvrPlayback);
        restoreToDefault();
        fileHistoryTime = 0;
        fileTotalTime = 0;
        SaveValue.saveWorldBooleanValue(mContext,
                MenuConfigManager.PVR_PLAYBACK_START, false, false);
       if (MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
           MtkTvGinga.getInstance().startGinga();
       }
       mStateDvrPlayback = null;
    }

    private void forwardDvrFilePlay(float speed) {
        // get speed 2 play
        PlaybackParams params = new PlaybackParams();
        params.setSpeed(speed);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "forward play  speed==" + speed);
        if (time_tv != null){
            refreshTime();
            }
        // speakText("dvr forward");
        getController().dvrPlaybackParams(params);
    }

    private void rewindDvrFilePlay(float speed) {
        // get speed 2 play
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "rewind play  speed==" + (speed));
        // speakText("dvr rewind");
        forwardDvrFilePlay(speed);
    }

    private void seekDvrFilePlay(long timeMs) {
        // jump to a time lees than total time
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "seek dvr play  timeMs==" + timeMs);
        getController().dvrSeekTo(timeMs);
    }

    /**
     * get base dvr file info
     *
     * @param dvrFiles
     */
    private void getDvrBaseInfo(DVRFiles dvrFiles) {
        fileUri = dvrFiles.getProgarmUri();
        Pattern pattern = Pattern.compile("\\d{4}_\\d{6}.pvr");
        Matcher matcher = pattern.matcher(dvrFiles.getProgramName());
        while (matcher.find()) {
            fileName = matcher.group();
        }
        if(!dvrFiles.getmDetailInfo().isEmpty()){
            progarmName = 
                dvrFiles.getChannelName()+"_"+dvrFiles.getDate().replace("/", "")+
                "_"+dvrFiles.getmDetailInfo();
        }else{
            progarmName =
                dvrFiles.getChannelName()+"_"+dvrFiles.getDate().replace("/", "");
        }
        fileHistoryTime = 0L;
        filePlaySpeed = 1;
        mSpeedStepTemp = 1;
        fileTotalTime = dvrFiles.getDuration() * 1000L;
        isPlaying = true;
        isFForFR = false;
        isSForSR = false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,
                "getDvrBaseInfo :==> " + " fileUri= " + fileUri
                        + " fileName== " + fileName + " fileTotalTime== "
                        + fileTotalTime + " dvrFiles.getDurationStr()== "
                        + dvrFiles.getDurationStr());
    }

    /**
     * save file play history info value= "DVRi" " index , uri , currentTime .
     */
    private void setDvrFileState(String uri, long currentTime) {
        if (isUnlockPin) { // Can't save last memory when unlockpin is true, as
                           // DTV01047552
            return;
        }
        int max = Integer.MAX_VALUE;
        int in = 0;
        int is = 0;

        for (int i = 0; i < 5; i++) {
            String dvr1 = SaveValue.readWorldStringValue(mContext,
                    "DVRPLAYBACK" + String.valueOf(i));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set dvrfile=" + dvr1);
            if (dvr1 != null && dvr1.isEmpty()) {
                saveSharedP(i, i, uri, currentTime);
                return;
            } else if(dvr1 != null && !dvr1.isEmpty()) {
                String[] dvrs1 = dvr1.split(",");
                int index = Integer.valueOf(dvrs1[0]);
                String u = dvrs1[1];
                if (u.equals(fileName)) {
                    saveSharedP(i, index, uri, currentTime);
                    return;
                }
                // String duration = dvrs1[2];
                in = index;
            }
            if (max > in) {
                max = in;
                is = i;
            }
        }
        saveSharedP(is, max + 5, uri, currentTime);
    }

    private void saveSharedP(int index, int number, String uri, long currentTime) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        Editor edt = sp.edit();
        String contentString = String.valueOf(number) + "," + uri.toString()
                + "," + String.valueOf(currentTime);
        edt.putString("DVRPLAYBACK" + index, contentString);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "savefile=" + contentString);
        edt.commit();
        SaveValue.writeWorldStringValue(
                mContext,
                "DVRPLAYBACK" + index,
                String.valueOf(number) + "," + uri.toString() + ","
                        + String.valueOf(currentTime), true);
    }

    /**
     * get file play history info value= "DVRi" " index , uri , currentTime .
     */
    private void getDvrFileState() {

        if (fileUri == null) {
            return;
        }

        for (int i = 0; i < 5; i++) {
            String dvrList = SaveValue.readWorldStringValue(mContext,
                    "DVRPLAYBACK" + String.valueOf(i));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrfile=" + dvrList);
            if (dvrList != null && !dvrList.isEmpty()) {
                String[] abc = dvrList.split(",");
                String uu = abc[1];
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "uu=" + uu);
                // Uri uu = Uri.parse(u);
                if (uu != null && uu.equals(fileName)) {

                    fileHistoryTime = Long.valueOf(abc[2]);
                    if (fileHistoryTime >= fileTotalTime) {
                        fileHistoryTime = 0L;
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "getDvrFileState==> " + " index= " + i
                            + " DVRPLAYBACK= " + String.valueOf(i) + " URi= "
                            + uu + " fileHistoryTime = " + abc[2]
                            + " and fileUri= " + fileUri);
                }
            } else {
                return;
            }
        }
    }

    private void switchCc() {
        if (subTitleTrackInfos!=null&&!subTitleTrackInfos.isEmpty() ) {
            // switch
            if (subTitleTrackInfos.size() > subtitleTrackIndex) {
                enableswitch=false;
            } else {
                setSubtitleOff();
                currentTvTrackInfo = null;
                enableswitch=true;
                return;
            }
            subTitleTrackName = subTitleTrackInfos.get(subtitleTrackIndex)
                    .getLanguage();
            subTitleTrackId = subTitleTrackInfos.get(subtitleTrackIndex)
                    .getId();
            subTitleTrackName= (subtitleTrackIndex+1)+"/"+subTitleTrackInfos.size()
                +" "+  mLanguageUtil.getMtsNameByValue(subTitleTrackName);
            subTitleTypeString = subTitleTrackInfos.get(subtitleTrackIndex).getExtra()!=null?
                subTitleTrackInfos.get(subtitleTrackIndex).getExtra()
                .getString("key_SubtileMime"):"";
            getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subTitleTrackId);
            currentTvTrackInfo = subTitleTrackInfos.get(subtitleTrackIndex);

            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "switchCCTrack ==true and subTitleTrackId== "
                    + subTitleTrackId + ", subTitleTrackName: "
                    + subTitleTrackName);
            subtitleTrackIndex++;
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "subtitleTackInfo == null !! ");
        }
//        enableswitch = true;
    }

    private void switchAudioTrack() {
        if (audioTrackInfos!=null&&!audioTrackInfos.isEmpty() ) {
            // switch
             ++audioTrackIndex;
             com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"<--audioTrackIndex-->"+audioTrackIndex);
            if (audioTrackInfos.size() > audioTrackIndex ) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"audioTrack size"+audioTrackInfos.size());
            } else {
                audioTrackIndex = 0;
            }
            // audioTrackName
            // =audioTrackInfos.get(audioTrackIndex).getLanguage();
            // audioTrackName=audioTrackInfos.get(audioTrackIndex).getId();
            audioTrackName = 
                    (audioTrackIndex + 1)
                    + "/"
                    + audioTrackInfos.size()
                    + " "
                    + mLanguageUtil.getMtsNameByValue(audioTrackInfos.get(
                            audioTrackIndex).getLanguage());
            audioString=getAudioType(audioTrackInfos.get(audioTrackIndex));
            // SundryImplement.getInstanceNavSundryImplement(mContext).getCurrentAudioLang();
            getTvView().selectTrack(TvTrackInfo.TYPE_AUDIO,
                    audioTrackIndex + "");
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "switchAudioTrack ==true and audioTrackIndex== "
                    + audioTrackIndex);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "audioTackInfo == null !! ");
        }
    }

    private void getSubTitleTrackInfo() {
        subTitleTrackInfos = getTvView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
        if (subTitleTrackInfos == null) {
            subTitleTrackInfos = new ArrayList<TvTrackInfo>();
        } else {
            if (subTitleTrackInfos!=null&&!subTitleTrackInfos.isEmpty()) {
                for(TvTrackInfo trackinfo : subTitleTrackInfos){
                    String  subName = trackinfo.getLanguage();
                    if(trackinfo.getExtra()!=null){
                        trackinfo.getExtra().putString("Sub_Language",
                        mLanguageUtil.getMtsNameByValue(subName));
                    }
                }
                if(enableswitch){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"set off");
                    setSubtitleOff();
                }else if(!isCurrentSubtitle()){
                    setSubtitleState();
                }else {
                    subTitleTrackName = subTitleTrackInfos.get(subtitleTrackIndex).getLanguage();
                    subTitleTrackId = subTitleTrackInfos.get(subtitleTrackIndex).getId();
                    subTitleTrackName = (subtitleTrackIndex+1) + "/" + subTitleTrackInfos.size() + " "
                        + mLanguageUtil.getMtsNameByValue(subTitleTrackName);
                    subTitleTypeString =subTitleTrackInfos.get(subtitleTrackIndex).getExtra()!=null?
                        subTitleTrackInfos.get(subtitleTrackIndex)
                        .getExtra().getString("key_SubtileMime"):"";
                    getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subTitleTrackId);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "subTitleTrackName == " + subTitleTrackName
                        + "subTitleTrackId == " + subTitleTrackId
                        + "currentTrackIndex == 0");
                    subtitleTrackIndex++;
                }
            } else {
                subTitleTrackName = null;
            }
            // subtitleName_tv.setVisibility(View.VISIBLE);
        }
//        enableswitch = true;
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "subtitletrackInfos.size == " + subTitleTrackInfos.size());
    }

    private void getAudioTrackInfo() {
        audioTrackInfos = getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
        if (audioTrackInfos == null) {
            audioTrackInfos = new ArrayList<TvTrackInfo>();
        } else {
            if (audioTrackInfos!=null&&!audioTrackInfos.isEmpty()&& audioTrackIndex != -1&&audioTrackIndex < audioTrackInfos.size()) {
                audioTrackName = (audioTrackIndex+1)+"/" + audioTrackInfos.size()+" "+
                    mLanguageUtil.getMtsNameByValue(audioTrackInfos.get(audioTrackIndex).getLanguage()); 
                audioString=getAudioType(audioTrackInfos.get(audioTrackIndex));
                audioTrackId = audioTrackInfos.get(audioTrackIndex).getId();

                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "audioTrackId == " + audioTrackId);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "audioTrackName == null" );
                audioTrackName="";
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "audiotrackInfos.size == " + audioTrackInfos.size());
    }
    
    private boolean isCurrentSubtitle(){
        for(int i=0;i<subTitleTrackInfos.size();i++){
            if((subTitleTrackInfos.get(i)).equals(currentTvTrackInfo)){
            	subtitleTrackIndex=i;
            	return true;
            }
        }
        return false;
    }

    private void setSubtitleState(){

        subtitleTrackIndex=getController().setSelectSubtitle(subTitleTrackInfos);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"subtitleindex="+subtitleTrackIndex);
        if(subtitleTrackIndex==-1){
            setSubtitleOff();
        }else{
            subTitleTrackName = subTitleTrackInfos.get(subtitleTrackIndex).getLanguage();
            subTitleTrackId = subTitleTrackInfos.get(subtitleTrackIndex).getId();
            subTitleTrackName= (subtitleTrackIndex+1)+"/"+subTitleTrackInfos.size()+" "+  
                mLanguageUtil.getMtsNameByValue(subTitleTrackName);
            subTitleTypeString = subTitleTrackInfos.get(subtitleTrackIndex).getExtra()!=null?
                subTitleTrackInfos.get(subtitleTrackIndex).getExtra().getString("key_SubtileMime"):"";
            getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subTitleTrackId);
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "setSubtitleState == " + subTitleTrackName + "subTitleTrackId == " + subTitleTrackId
                + "currentTrackIndex == 0");
            subtitleTrackIndex++;

        }

    }

    private void setSubtitleOff(){
        subtitleTrackIndex = 0;
        subTitleTrackName = mContext.getResources().getString(R.string.common_off);
        subTitleTypeString="";
        showBigCtrlBar();
        //getTvView().setCaptionEnabled(false);
        Bundle subBundle = new Bundle();
        subBundle.putBoolean(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SUBTITLE_VALUE, false);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "set subAction info.");
        getTvView().sendAppPrivateCommand(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_CONTROLSUBTITLE, subBundle);
    }

    /**
     * restore values to default .
     */
    private void restoreToDefault() {
        isPlayStart = false;
        isReplayOK = true;
        isUnlockPin = false;
        subtitleTrackIndex = 0;
        subTitleTrackInfos = null;
        subTitleTrackName = null;
        subTitleTrackId = null;

        audioTrackIndex = -1;
//        showAudioTrackFirst = true;
        audioTrackInfos = null;
        audioTrackName = null;
        audioTrackId = null;
        // update for cr DTV00848067
        if (subtitleName_tv != null) {
            subtitleName_tv.setVisibility(View.GONE);
        }
        if (audioName_tv != null) {
            audioName_tv.setVisibility(View.GONE);
        }
    }

    /**
     * like mmp flow ,forbidden seek action when speed invalid( not 1).
     *
     * @return
     */
    private boolean prepareSeekAction() {
        if (filePlaySpeed > 1 || filePlaySpeed < -1) {
            // will not do seek when speed invalid
            Toast.makeText(mContext, mContext.getString(R.string.dvr_play_speed_invalied), Toast.LENGTH_SHORT)
                    .show();
            return true;
        } else {
            return false;
        }
    }

    private void speakText(String str) {
        getManager().speakText(str);
        if (getManager().getTTSEnable()) {
            // TODO delay action
            mHandler.removeMessages(MSG_DELAY_TTS);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_TTS),
                    1000);
        }
    }

    /*-----------------------------------------------------**/
    class DvrPlaybackHnadler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case MSG_CTRLBAR_DISMISS:
                dismissBigCtrlBar();
                break;

            case MSG_REFRESH_TIME:
                mHandler.removeMessages(MSG_REFRESH_TIME);
                if (filePlaySpeed >= 0) {
                    fileCurrentTime = fileCurrentTime
                            + (long) (1000L * filePlaySpeed);
                } else {
                    if (fileCurrentTime <= 0) {
                        fileCurrentTime = 0;
                        filePlaySpeed = 0;
                        mSpeedStepTemp = 0;
                    } else {
                        fileCurrentTime = fileCurrentTime
                                + (long) (1000L * filePlaySpeed);
                    }
                }
                fileCurrentTime = getController().getDvrPlayDuration();
                fileCurrentTime = fileCurrentTime%1000>500?((fileCurrentTime/1000+1)*1000):fileCurrentTime;
                if (fileCurrentTime < 0 || fileCurrentTime > fileTotalTime) {

                    fileCurrentTime = 0L;
                }
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "fileCurrentTime from surface is "
                            + fileCurrentTime);
                boolean sun = ((SundryShowTextView) ComponentsManager
                        .getInstance().getComponentById(
                                NavBasic.NAV_COMP_ID_SUNDRY)).isVisible();
                if (isaudioonly == 9
                        && !SaveValue.readWorldBooleanValue(mContext,
                                "is_menu_show") && !sun) {
                    setHandleUI();
                }
                refreshTime();
                // will reset key event when UI refresh.
                isReplayOK = true;
                break;
            case MSG_SPEAK_DVR_PLAY:
                speakText("dvr play");
                break;
            case MSG_DELAY_TTS:
                showBigCtrlBar();// delay 1s to make sure if current action has
                break;          // been read out in tts and show UI later .
            default:
                break;
            }

            super.handleMessage(msg);
        }
    }

    class CallbackHandler extends Handler {
        WeakReference<StateDvrPlayback> mstaReference;

        public CallbackHandler(StateDvrPlayback mStateDvrPlayback) {
            this.mstaReference = new WeakReference<StateDvrPlayback>(
                    mStateDvrPlayback);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mstaReference == null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mstaReference == null" );
                return;
            }
            if (msg != null) {
                int callBack = msg.what;
                SomeArgs args = (SomeArgs) msg.obj;
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "msg =" + msg.what);

                switch (callBack) {

                case DvrConstant.MSG_CALLBACK_TIMESHIFT_EVENT:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TIMESHIFT_EVENT 1 " + args.arg1);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TIMESHIFT_EVENT 2 " + args.arg2);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TIMESHIFT_EVENT 3 " + args.arg3);

                    Bundle bundle = (Bundle) args.arg3;
                    int setSpeedResult = bundle
                            .getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SPEED_SET_KEY,
                                    -2);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "setSpeedResult== " + setSpeedResult);

                    if (setSpeedResult == 0) {
                        isPlaying = true;
                        showBigCtrlBar();
                    } else if (setSpeedResult == -1 && isRunning()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "set speed fail.");
                        isSForSR = false;
                        isFForFR = false;
                        if(filePlaySpeed<0){
                            Toast.makeText(mContext,mContext.getString(R.string.not_support_fb),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext,mContext.getString(R.string.not_support_ff),Toast.LENGTH_SHORT).show();
                        }
                        if(!isPlaying){
                        filePlaySpeed = 0;
                        mSpeedStepTemp = 0;
                        }else{
                        filePlaySpeed = 1;
                        mSpeedStepTemp = 1;
                        }
                        showBigCtrlBar();

                    }

                    int speedExtra = bundle
                            .getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SPEED_CHANGED_KEY);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "speedExtra== " + speedExtra
                            + ", filePlaySpeed== " + filePlaySpeed);

                    // play status is forward or rewind to normal and normal to
                    // forward or rewind.
                    if (speedExtra !=0) {
                        filePlaySpeed = speedExtra;
                        mSpeedStepTemp = speedExtra;
                        isFForFR = false;
                        isSForSR = false;
                        showBigCtrlBar();
                    }
                    //duration update
                    updateDuration = bundle.getInt(MtkTvTISMsgBase
                    .MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_DURATION_CHANGED_KEY);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"duaration=="+updateDuration);
                    if(updateDuration>0&&isPlayStart){
                        if (mControlbar == null) {
                           initBigCtrlBar();
                           }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"update=="+updateDuration);
                        fileTotalTime = updateDuration;
                        updateTimeProgressBar();
                        }
                    int audioonly = bundle
                            .getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_AUDIO_ONLY_SERVICE,
                                    -2);
                    if (audioonly == 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "audio only");
                        isaudioonly = 9;
                    }
                    String mString = bundle
                            .getString(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_KEY);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "mString== " + mString);
                    mString = mString==null?"":mString;
                    // play complete
                    //if (mString != null&&
                            if(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_PLAY_COMPLETE_VALUE
                                    .equals(mString)) {
                        fileCurrentTime = 0;
                        stopDvrFilePlay();
                        if (getManager().setState(
                                StateDvrFileList.getInstance(DvrManager
                                        .getInstance()))) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,
                                    "Show file list when pvr play completed.");
                            StateDvrFileList.getInstance().showPVRlist();
                        }
                        return;
                    }

                    // start play
                    //if (mString != null&&
                            if( MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_PLAY_VALUE
                                    .equals(mString)) {
                        isPlayStart = true;
                        if (fileHistoryTime != 0
                                && fileHistoryTime < fileTotalTime) {
                            seekDvrFilePlay(fileHistoryTime);
                            isSeek = false;
                            fileHistoryTime = 0;
                        }
                        setHandleUI();
                        showBigCtrlBar();
                        if (!isUnlockPin){
                            mHandler.sendEmptyMessage(MSG_REFRESH_TIME);
                            }
                        // if (fileTotalTime <= 0) {
                        // DvrManager.getInstance().showFeatureNotAvaiable();
                        // stopDvrFilePlay();
                        // return;
                        // }

                    }

                    // file locked
                    //if (mString != null&&
                            if( (MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_FILE_LOCKED)
                                    .equals(mString)) {
                        if (isUnlockPin) {
                            return;
                        }

                        isUnlockPin = true;
                        // dialog = new PinDialog(
                        // PinDialog.PIN_DIALOG_TYPE_ENTER_PIN,
                        // new ResultListener() {
                        //
                        // @Override
                        // public void play(String pin) {
                        // Bundle mBundle = new Bundle();
                        // try {
                        // int p = Integer.valueOf(pin);
                        // mBundle.putInt(
                        // MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_PIN_VALUE,
                        // p);
                        // getController()
                        // .onAppPrivateCommand(
                        // MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SETPIN,
                        // mBundle);
                        // } catch (Exception e) {
                        // }
                        // }
                        //
                        // @Override
                        // public void done(boolean success) {
                        // isUnlockPin = false;
                        // reSumeDvrFilePlay();
                        // }
                        // }, mContext);
                        // dialog.show();
                        mPinDialogFragment = PinDialogFragment
                                .create(PinDialogFragment.PIN_DIALOG_TYPE_COMMON_UNLOCK);
                        mPinDialogFragment
                                .setOnPinCheckCallback(new OnPinCheckCallback() {

                                    @Override
                                    public boolean onCheckPIN(String pin) {
                                        return checkPWD(pin);
                                    }

                                    @Override
                                    public void startTimeout() {
                                        // TODO Auto-generated method stub
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"starttimeout");
                                    }

                                    @Override
                                    public void stopTimeout() {
                                        // TODO Auto-generated method stub
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"stoptimeout");
                                    }

                                    @Override
                                    public void pinExit() {
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"pinexit");
                                    }

                                    @Override
                                    public void onKey(int keyCode,
                                            KeyEvent event) {
                                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"keycode="+keyCode);
                                    }
                                });
                        mPinDialogFragment.setShowing(true);
                        mPinDialogFragment.show(TurnkeyUiMainActivity
                                .getInstance().getFragmentManager(),
                                "PinDialogFragment");
                        return;
                    }

                    // reply to begin (when msg comes ,will not do seek again )
                    //if (mString != null&&
                            if((MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_ON_REPLY_VALUE)
                                    .equals(mString)) {
                        isReplayOK = false;
                        fileCurrentTime = 0L;
                        filePlaySpeed = 1;
                        mSpeedStepTemp = 1;
                        isPlaying = true;
                        isFForFR = false;
                        isSForSR = false;
//                        showAudioTrackFirst = true;
                        subtitleTrackIndex = 0;
                        // playDvrFile(fileUri); no need play ,it start
                        // automatically
                    }

                    // reply to begin complete , (can seek again )
                    //if (mString != null&&
                            if((MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_REPLY_DONE_VALUE)
                                    .equals(mString)) {
                        isReplayOK = true;
                        showBigCtrlBar();
                    }

                    // seek complete msg (can not do action before compelte)
                    //if (mString != null&&
                           if( (MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_SEEK_COMPLETE_VALUE)
                                    .equals(mString)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "seek done and the view gone.");
                        try {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "sleep 1500ms to mute video.");
                            Thread.sleep(1500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        TurnkeyUiMainActivity
                                .getInstance()
                                .getBlockScreenView()
                                .setVisibility(View.GONE,
                                        TvBlockView.BLOCK_PVR_LAST_MEMORY_BLOCK);
                        isReplayOK = true;
                    }

                    // CI card unplug and will stop pvr.
                    //if (mString != null&&
                            if( (MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_ERROR_UNKNOWN)
                                    .equals(mString)) {
                        stopDvrFilePlay();
                    }

                    // file bad or can not play normally .
                    //if (mString != null) {
                        if (mString
                                .equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_FILE_CORRUPT)) {
                            DvrManager.getInstance().showFileNotSupport();
                            stopDvrFilePlay();
                        } else if (mString
                                .equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_FILE_NOTSUPPORT)) {
                            DvrManager.getInstance().showFileNotSupport();
                            stopDvrFilePlay();
                        //}
                        // else if
                        // (mString.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_FILE_VIDEO_ENCODE_FORMAT_UNSUPPORT))
                        // {
                        // DvrManager.getInstance().showFeatureNotSupport();
                        // stopDvrFilePlay();
                        // }
                    }
                        
                    if(mString.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_FILE_MISMATCH)){

                    	Toast.makeText(mContext, mContext.getString(R.string.file_notsupport), Toast.LENGTH_SHORT).show();
                    }
                    
                    if(mString.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_STATUS_PAUSE_VALUE)){
                        pauseDvrFilePlay();
                    }

                    break;

                case DvrConstant.MSG_CALLBACK_TIMESHIFT_STATE:

                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TIMESHIFT_STATE 1 " + args.arg1);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TIMESHIFT_STATE 2 "
                            + args.argi2);

                    break;
                case DvrConstant.MSG_CALLBACK_TRACK_CHANGED:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TRACK_CHANGED i1 " + args.argi1);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TRACK_CHANGED 1 " + args.arg1);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TRACK_CHANGED 2 " + args.arg2);
                    // track info
                    if (isRunning()) {
                        getSubTitleTrackInfo();
                        getAudioTrackInfo();
                        /*
                         * if (showAudioTrackFirst) { // the msg call twice
                         * times audio index will error. switchAudioTrack();
                         * showAudioTrackFirst = false; }
                         */
                        showBigCtrlBar();
                    }
                    break;
                case DvrConstant.MSG_CALLBACK_TRACK_SELECTED:
                   com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TRACK_SELECTED i1 " + args.argi1);
                   com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TRACK_SELECTED 1 " + args.arg1);
                   com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CALLBACK_TRACK_SELECTED 2 " + args.arg2);
                   if(((int)(args.arg1)==TvTrackInfo.TYPE_AUDIO) && isRunning()){
                    audioTrackIndex= Integer.valueOf(((String)args.arg2));
                    getAudioTrackInfo();
                    showBigCtrlBar();
                    }
                    break;

                default:
                    break;
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "callback handler msg ==null !! ");
            }
        }

    }

    public boolean checkPWD(String pwd) {
        Bundle mBundle = new Bundle();

        if (MtkTvPWDDialog.getInstance().checkPWD(pwd)) {
            try {
                int p = Integer.valueOf(pwd);
                mBundle.putInt(
                        MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_PIN_VALUE,
                        p);
                getController()
                        .onAppPrivateCommand(
                                MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SETPIN,
                                mBundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isUnlockPin = false;
            reSumeDvrFilePlay();
            return true;
        }
        return false;
    }

    private void setHandleUI() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "audio only");
        TwinkleDialog twinkleDialog = ((TwinkleDialog) ComponentsManager.getInstance()
                .getComponentById(NavBasic.NAV_COMP_ID_TWINKLE_DIALOG));
        if(twinkleDialog != null && isaudioonly == 9) {

            twinkleDialog.handleCallBack(isaudioonly);
        }
    }

    private String getAudioType(TvTrackInfo tvTrackInfo){
        boolean visulImpaire = tvTrackInfo.getExtra()!=null&&tvTrackInfo.getExtra().getBoolean("key_VisualImpaired");
        boolean hearImpaire= tvTrackInfo.getExtra()!=null&&tvTrackInfo.getExtra().getBoolean("key_HeardImpaired");
        String doblyString = tvTrackInfo.getExtra()!=null?tvTrackInfo.getExtra().getString("key_AudioMimeType"):"";
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"visu="+visulImpaire+",hear="+hearImpaire+",dobly="+doblyString);
        if(visulImpaire){
            return "("+mContext.getString(R.string.menu_arrays_Visually_Impaired)+")";  
        }
        if(hearImpaire){
            return"("+ mContext.getString(R.string.menu_arrays_Hearing_Impaired)+")";  
        }
        if(doblyString.contains("audio/ac3")||doblyString.contains("audio/ac4")
            || doblyString.contains("audio/eac3")){
            return "("+mContext.getString(R.string.dobly_audio)+")";
        }

        return "";
    }
        @Override
    public void updateComponentStatus(int statusID, int value) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus statusID =" + statusID);
      if(statusID ==  ComponentStatusListener.NAV_INPUTS_PANEL_SHOW) {
        if(mPinDialogFragment!=null&&mPinDialogFragment.isShowing()) {
          mPinDialogFragment.dismiss();
        }
      }
     }

        
    private String getSubtitleType(){
        if(subTitleTypeString.isEmpty()){
            return "";
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"subtypestring->"+subTitleTypeString);
        if(subTitleTypeString.contains("teltext")){
        return mContext.getString(R.string.menu_setup_teletext);
        }
        if(subTitleTypeString.contains("hi")){
            return mContext.getString(R.string.menu_arrays_Hearing_Impaired);
        }
        return "";
    }

}
