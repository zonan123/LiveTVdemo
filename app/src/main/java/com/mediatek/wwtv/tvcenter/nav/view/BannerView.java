package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.tv.menu.MenuOptionMain;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigMsgBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvGingaAppInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvVideoInfoBase;
import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.view.DiskSettingDialog;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateNormal;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.DetailTextReader;
import com.mediatek.wwtv.tvcenter.nav.util.DetailTextReader.TextReaderPageChangeListener;
import com.mediatek.wwtv.tvcenter.nav.util.FocusLabelControl;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.util.TVStateControl;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIStateChangedCallBack;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

import java.util.ArrayList;
import java.util.List;

//add by xun
import com.ist.systemuilib.tvapp.TvToSystemManager;
//end xun
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BannerView extends NavBasicView implements
    ComponentStatusListener.ICStatusListener {
    private final static String TAG = "BannerView";

    private static final int MSG_UPDATE_TIME = 0x111;

    //add by xun
    private TvToSystemManager mTvToSystemManager;
    //end xun
    // hide banner
    public final static int HBBTV_MSG_DISABLE_BANNER = 257;
    // no hide banner
    public final static int HBBTV_MSG_ENABLE_BANNER = 258;
    // 262 running 263 not running
    public final static int HBBTV_STATUS_3RD_APP_RUNNING = 262; // 264

    public final static int HBBTV_STATUS_3RD_APP_NOT_RUNNING = 263; // 265

    // whether disable or enable msg
    public final static int MHEG5_MSG_IF_DISABLE = 258;
    public final static int MHEG5_MSG_DISABLE_BANNER = 0;
    public final static int MHEG5_MSG_ENABLE_BANNER = 1;

    BannerView bannerView;
    // hide all panel
    public final static int CHILD_TYPE_INFO_NONE = 0;
    // show program panel
    public final static int CHILD_TYPE_INFO_SIMPLE = 1;
    // show channel and program panel
    public final static int CHILD_TYPE_INFO_BASIC = 2;
    // show channel program and detail panel
    public final static int CHILD_TYPE_INFO_DETAIL = 3;
    // whether show pwd
    public int PWD_SHOW_FLAG;

    //	private boolean isOnkeyInfo;
    private TextToSpeechUtil ttsUtil;

    private static CommonIntegration mNavIntegration = null;
    private boolean mNeedChangeSource;
    //	private int mTuneChannelId = -1;

    private static BannerImplement mNavBannerImplement = null;
    private String channelChangeTtsText = "";
    private String channelChangeTtsTextForKey = "";

    // private Context mContext; <refer parent class: contains same context>

    private View topBanner;

    private View bannerLayout;

    private ComponentsManager mComponentsManager;

    private ChannelListDialog mChannelListDialog;

    private FavoriteListDialog mFavoriteChannelListView;

    //	private CIStateChangedCallBack mCIState;

    // program panel
    private BasicBanner mBasicBanner;
    // channel panel
    private SimpleBanner mSimpleBanner;
    // detail panner
    private DetailBanner mDetailBanner;

    private StateManage mStateManage;

    //	private int totalState = -1;

    public final static int SPECIAL_NO_CHANNEL = 16;
    public final static int SPECIAL_EMPTY_SPECIAFIED_CH_LIST = 17;
    public final static int SPECIAL_NO_SIGNAL = 1;
    public final static int SPECIAL_INPUT_LOCK = 4;
    public final static int SPECIAL_CHANNEL_LOCK = 2;
    public final static int SPECIAL_RETRIVING = 19;
    public final static int SPECIAL_NO_SUPPORT = 20;
    public final static int SPECIAL_PROGRAM_LOCK = 3;

    public final static int BANNER_MSG_NAV = 0;
    public final static int BANNER_MSG_ITEM = 1;

    public final static int TV_STATE_NORMAL = 15;

    public final static int SPECIAL_NO_AUDIO_VIDEO = 6;
    public final static int SPECIAL_SCRAMBLED_VIDEO_NO_AUDIO = 7;
    public final static int SPECIAL_SCRAMBLED_AUDIO_VIDEO = 8;
    public final static int SPECIAL_SCRAMBLED_VIDEO_CLEAR_AUDIO = 9;
    public final static int SPECIAL_SCRAMBLED_AUDIO_NO_VIDEO = 10;
    public final static int SPECIAL_STATUS_AUDIO_ONLY = 11;
    public final static int SPECIAL_STATUS_VIDEO_ONLY = 13;
    public final static int SPECIAL_STATUS_AUDIO_VIDEO = 14;
    public final static int BANNER_MSG_NAV_UNLOCK = 18;
    public final static int BANNER_MSG_NAV_BEFORE_SVC = 21;
    public final static int BANNER_MSG_ITM_CURRENT_PROGRAM_TITLE = 8;
    public final static int BANNER_MSG_ITM_CURRENT_PROGRAM_TIME = 10;
    public final static int BANNER_MSG_ITM_CURRENT_PROGRAM_DETAIL = 13;
    public final static int BANNER_MSG_ITM_CURRENT_PROGRAM_CATEGORY = 37;
    public final static int BANNER_MSG_ITM_NEXT_PROGRAM_TITLE = 11;
    public final static int BANNER_MSG_ITM_NEXT_PROGRAM_TIME = 12;
    public final static int BANNER_MSG_ITM_NEXT_PROGRAM_CATEGORY = 43;
    public final static int BANNER_MSG_ITM_CAPTION = 16;
    public final static int BANNER_MSG_ITM_CAPTION_ICON_CHANGE = 27;
    public final static int BANNNER_MSG_ITEM_IPTS_NAME = 21;
    public final static int BANNNER_MSG_ITEM_ATMOS_CHNAGE = 36;
    public final static int BANNNER_MSG_ITEM_IPTS_RSLT_CHANGE = 22;
    public final static int BANNNER_MSG_ITEM_IPTS_LOCK_ICON = 24;
    public final static int BANNER_MSG_ITM_AD_EYE = 33;
    public final static int BANNER_MSG_ITM_AD_EAR = 34;

    private final static int MSG_NUMBER_KEY = 1111;

    private final static int MSG_TTS_SPEAK_DELAY = 1113;

    private final static int SHOW_BASIC_BANNER_AFTER_SVC_CHANGE = 1114;

    // Banner basic height 120dp
    private final static int BANNER_BASIC_HEIGHT = 120;
    private TvCallbackHandler mBannerTvCallbackHandler;
    private MtkTvCI mCi = null;

    private boolean audioScramebled = false;
    private boolean videoScramebled = false;
    private boolean noAudio = false;
    private boolean noVideo = false;

    private boolean isSpecialState = false;
    private boolean isBlockStateFromSVCTX = false;
    private String mSelectedChannelNumString = "";
    private SundryImplement mNavSundryImplement = null;
    private boolean isHostQuietTuneStatus = false;

    private int specialType = -1;
    private boolean mNextEvent;

    private boolean mNumputChangeChannel = false;

    private boolean interruptShowBannerWithDelay = false;

    private boolean bSourceOnTune = false;// source on tuning

    private boolean mIs3rdTVSource = false;

    private boolean mDisableBanner = false;

    private int mHDRIndex = 0;

    private boolean mIs3rdRunning = false;
    private boolean mOADIsActive = false;

    private HandlerThread mGetTimeThread;
    private Handler mGetTimeHandler;

    public static String channelNumSeparator = "-";

    private CompositeDisposable mDisposables = new CompositeDisposable();

    public void handlerHbbtvMessage(int type, int message) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, type=" + type + ", message="
            + message);
        switch (type) {
            case HBBTV_MSG_DISABLE_BANNER:// disable banner
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, HBBTV_MSG_DISABLE_BANNER");
                mDisableBanner = true;
                break;
            case HBBTV_MSG_ENABLE_BANNER:// enable banner
                mDisableBanner = false;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, HBBTV_MSG_ENABLE_BANNER");
                break;
            case HBBTV_STATUS_3RD_APP_RUNNING:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, HBBTV_STATUS_3RD_APP_RUNNING");
                mIs3rdRunning = true;
                break;
            case HBBTV_STATUS_3RD_APP_NOT_RUNNING:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, HBBTV_STATUS_3RD_APP_NOT_RUNNING");
                mIs3rdRunning = false;
                break;
            default:
                break;
        }
    }

    public void handlerMheg5Message(int type, int message) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerMheg5Message, type=" + type + ", message="
            + message);
        if (type == MHEG5_MSG_IF_DISABLE) {
            switch (message) {
                case MHEG5_MSG_DISABLE_BANNER:// disable banner for channel change
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerMheg5Message, MHEG5_MSG_DISABLE_BANNER");
                    mDisableBanner = true;
                    break;
                case MHEG5_MSG_ENABLE_BANNER:// enable banner
                    mDisableBanner = false;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerMheg5Message, MHEG5_MSG_ENABLE_BANNER");
                    break;
                default:
                    break;
            }
        }
    }

    public void controlBannerByEASMessage(boolean flag) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "controlBannerByEASMessage flag:" + flag);
        mDisableBanner = flag;
    }

    private final Handler navBannerHandler = new Handler(
        mContext.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (!DestroyApp.isCurActivityTkuiMainActivity()
                || disableShowBanner(false)) {
                if (msg.what == TvCallbackConst.MSG_CB_BANNER_MSG) {
                    TvCallbackData specialMsgData = (TvCallbackData) msg.obj;
                    if (BANNER_MSG_NAV == specialMsgData.param1) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in handleMessage BANNER_MSG_NAV value=== "
                                + specialMsgData.param2);
                        switch (specialMsgData.param2) {
                            case BANNER_MSG_NAV_UNLOCK:
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case SPECIAL_NO_CHANNEL:
                                if (!mNavIntegration.isCurrentSourceATVforEuPA()) {
                                    List<TIFChannelInfo> list3rd = TIFChannelManager.getInstance(mContext).get3RDChannelList();
                                    if (list3rd != null && !list3rd.isEmpty()) {
                                        break;
                                    }
                                }
                            case SPECIAL_NO_SIGNAL:
                            case SPECIAL_CHANNEL_LOCK:
                            case SPECIAL_PROGRAM_LOCK:
                            case SPECIAL_INPUT_LOCK:
                            case SPECIAL_RETRIVING:
                            case SPECIAL_NO_SUPPORT:
                            case SPECIAL_EMPTY_SPECIAFIED_CH_LIST:
                                isSpecialState = true;
                                specialType = specialMsgData.param2;
                                break;
                            case SPECIAL_NO_AUDIO_VIDEO:
                                noAudio = true;
                                noVideo = true;
                                audioScramebled = false;
                                videoScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case SPECIAL_SCRAMBLED_VIDEO_NO_AUDIO:
                                videoScramebled = true;
                                noVideo = false;
                                noAudio = true;
                                audioScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case SPECIAL_SCRAMBLED_AUDIO_VIDEO:
                                audioScramebled = true;
                                videoScramebled = true;
                                noAudio = false;
                                noVideo = false;
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case SPECIAL_SCRAMBLED_VIDEO_CLEAR_AUDIO:
                                videoScramebled = true;
                                noVideo = false;
                                audioScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case SPECIAL_SCRAMBLED_AUDIO_NO_VIDEO:
                                audioScramebled = true;
                                noAudio = false;
                                noVideo = true;
                                videoScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case SPECIAL_STATUS_AUDIO_ONLY:
                                noVideo = true;
                                noAudio = false;
                                audioScramebled = false;
                                // fix CR: DTV00703315
                                TIFChannelInfo info = TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
                                if (info == null) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                        "the current has no channel!!!!!!!!!!!!!!!");
                                    break;
                                }
                                // fix CR:DTV00636297
                                boolean isNotHiddenCH = TIFFunctionUtil
                                    .checkChMaskformDataValue(info,
                                        TIFFunctionUtil.CH_LIST_MASK,
                                        TIFFunctionUtil.CH_LIST_VAL);
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not hidden channel:" + isNotHiddenCH);
                                if (isNotHiddenCH) {
                                    isSpecialState = false;
                                    specialType = -1;
                                }
                                break;
                            case SPECIAL_STATUS_VIDEO_ONLY:
                                noAudio = true;
                                noVideo = false;
                                videoScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                break;
                            case TV_STATE_NORMAL:
                                if (specialType == SPECIAL_NO_SIGNAL) {
                                    specialType = -1;
                                }
                                break;
                            case SPECIAL_STATUS_AUDIO_VIDEO:
                                specialType = -1;
                                audioScramebled = false;
                                videoScramebled = false;
                                noAudio = false;
                                noVideo = false;
                                isSpecialState = false;
                                break;
                            default:
                                break;
                        }
                    }
                } else if (msg.what == MSG_NUMBER_KEY) {
                    turnCHByNumKey();
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in navBannerHandler, current apk not TKUI");
                return;
            }
            switch (msg.what) {
                case SHOW_BASIC_BANNER_AFTER_SVC_CHANGE:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBannerAfterSVCChange,try show basic.");
                    if (mStateManage.getState() == CHILD_TYPE_INFO_BASIC &&
                        mSimpleBanner.isVisible() &&
                        !mBasicBanner.isVisible() &&
                        !disableShowBanner(false)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBannerAfterSVCChange,show basic.");
                        show(false, -1, false);
                    }
                    break;
                case TvCallbackConst.MSG_CB_BANNER_MSG:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in handleMessage get msg.what === TvCallbackConst.MSG_CB_BANNER_MSG");
                    TvCallbackData msgData = (TvCallbackData) msg.obj;

                    if (CIStateChangedCallBack.getInstance(mContext)
                        .camUpgradeStatus()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cam card on upgrade, no permit banner show");
                        break;
                    }
                    if (BANNER_MSG_NAV == msgData.param1) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in handleMessage BANNER_MSG_NAV value=== "
                                + msgData.param2);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "bSourceOnTune:" + bSourceOnTune);
                        switch (msgData.param2) {
                            case BANNER_MSG_NAV_BEFORE_SVC:
                                isHostQuietTuneStatus = isZiggoOperator() && mCi.getHostTuneBrdcstStatus() == 1;
                                ComponentStatusListener.getInstance().updateStatus(
                                    ComponentStatusListener.NAV_HOST_TUNE_STATUS, isHostQuietTuneStatus ? 1 : 0);
                                break;
                            case BANNER_MSG_NAV_UNLOCK:
                                isSpecialState = false;
                                specialType = -1;
                                if (mBasicBanner.isVisible()) {
                                    mBasicBanner.mLockIcon.setVisibility(View.INVISIBLE);
                                    mBasicBanner.setSpecialVisibility(isSpecialState);
                                }
                                if (mSimpleBanner.isVisible()) {
                                    mSimpleBanner.mLockIcon.setVisibility(View.INVISIBLE);
                                }
                                break;
                            case SPECIAL_NO_SIGNAL:
                            case SPECIAL_CHANNEL_LOCK:
                            case SPECIAL_PROGRAM_LOCK:
                            case SPECIAL_INPUT_LOCK:
                            case SPECIAL_NO_CHANNEL:
                            case SPECIAL_RETRIVING:
                            case SPECIAL_NO_SUPPORT:
                            case SPECIAL_EMPTY_SPECIAFIED_CH_LIST:
                                specialType = msgData.param2;
                                isSpecialState = true;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "come in handleMessage BANNER_MSG_NAV showSpecialBar");

                                boolean condition1 = mNavIntegration
                                    .isCurrentSourceBlockEx();
                                if (msgData.param2 == SPECIAL_INPUT_LOCK && !condition1) {
                                    isSpecialState = false;
                                    specialType = -1;
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvapi shouldn't send this message!!");
                                    break;
                                }

                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "delay to show specialBar!");
                                if (!disableShowBanner(false)
                                    && specialType != -1
                                    && (!CommonIntegration.getInstance().isCHChanging() || specialType == 16 || specialType == 4)) {
                                    showSpecialBar(specialType);
                                }

                                break;
                            case SPECIAL_NO_AUDIO_VIDEO:
                                noAudio = true;
                                noVideo = true;
                                audioScramebled = false;
                                videoScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                channelInputChangeShowBanner();
                                break;
                            case SPECIAL_SCRAMBLED_VIDEO_NO_AUDIO:
                                videoScramebled = true;
                                noVideo = false;
                                noAudio = true;
                                audioScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                channelInputChangeShowBanner();
                                break;
                            case SPECIAL_SCRAMBLED_AUDIO_VIDEO:
                                audioScramebled = true;
                                videoScramebled = true;
                                noAudio = false;
                                noVideo = false;
                                isSpecialState = false;
                                specialType = -1;
                                channelInputChangeShowBanner();
                                break;
                            case SPECIAL_SCRAMBLED_VIDEO_CLEAR_AUDIO:
                                videoScramebled = true;
                                noVideo = false;
                                audioScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                channelInputChangeShowBanner();
                                break;
                            case SPECIAL_SCRAMBLED_AUDIO_NO_VIDEO:
                                audioScramebled = true;
                                noAudio = false;
                                noVideo = true;
                                videoScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                channelInputChangeShowBanner();
                                break;
                            case SPECIAL_STATUS_AUDIO_ONLY:
                                noVideo = true;
                                noAudio = false;
                                audioScramebled = false;
                                // fix DTV00703315
                                TIFChannelInfo info = TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
                                if (info == null) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                        "the current has no channel!!!!!!!!!!!!!!!");
                                    break;
                                }
                                // fix CR:DTV00636297
                                boolean isNotHiddenCH = TIFFunctionUtil
                                    .checkChMaskformDataValue(info,
                                        TIFFunctionUtil.CH_LIST_MASK,
                                        TIFFunctionUtil.CH_LIST_VAL);
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not hidden channel:" + isNotHiddenCH);
                                if (isNotHiddenCH) {
                                    isSpecialState = false;
                                    specialType = -1;
                                }
                                channelInputChangeShowBanner();
                                break;
                            case SPECIAL_STATUS_VIDEO_ONLY:
                                noAudio = true;
                                noVideo = false;
                                videoScramebled = false;
                                isSpecialState = false;
                                specialType = -1;
                                channelInputChangeShowBanner();
                                break;
                            case TV_STATE_NORMAL:
                                if (specialType == SPECIAL_NO_SIGNAL) {
                                    specialType = -1;
                                }
                                break;
                            case SPECIAL_STATUS_AUDIO_VIDEO:
                                specialType = -1;
                                audioScramebled = false;
                                videoScramebled = false;
                                noAudio = false;
                                noVideo = false;
                                isSpecialState = false;
                                channelInputChangeShowBanner();
                                break;
                            default:
                                break;
                        }
                    } else if (BANNER_MSG_ITEM == msgData.param1) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in handleMessage BANNER_MSG_ITEM value=== "
                                + msgData.param2);
                        switch (msgData.param2) {
                            case BANNER_MSG_ITM_CURRENT_PROGRAM_TITLE:
                                if (mBasicBanner.mCurProgramName != null && mBasicBanner.isVisible()) {
                                    mBasicBanner.mProgramTitle = mNavBannerImplement.getProgramTitle();
                                    mBasicBanner.mCurProgramName.setText(mBasicBanner.mProgramTitle);
                                }
                                break;
                            case BANNER_MSG_ITM_CURRENT_PROGRAM_TIME:
                                if (mBasicBanner.mCurProgramDuration != null && mBasicBanner.isVisible()) {
                                    mBasicBanner.mProgramTime = mNavBannerImplement.getProgramTime();
                                    mBasicBanner.mCurProgramDuration.setText(mBasicBanner.mProgramTime);
                                }
                                break;
                            case BANNER_MSG_ITM_CURRENT_PROGRAM_DETAIL:
                                if (mDetailBanner.getDetailBannerVisible()) {
                                    mDetailBanner.show();
                                }
                                break;
                            case BANNER_MSG_ITM_CURRENT_PROGRAM_CATEGORY:
                                if (mBasicBanner.mCurrentProgramType != null && !mNextEvent && mBasicBanner.isVisible()) {
                                    mBasicBanner.mProgramCategory = mNavBannerImplement.getProgramCategory();
                                    mBasicBanner.mCurrentProgramType.setText(mBasicBanner.mProgramCategory);
                                }
                                break;
                            case BANNER_MSG_ITM_NEXT_PROGRAM_TITLE:
                                if (mBasicBanner.mNextProgramName != null && mBasicBanner.isVisible()) {
                                    mBasicBanner.mNextProgramTitle = mNavBannerImplement.getNextProgramTitle();
                                    mBasicBanner.mNextProgramName.setText(mBasicBanner.mNextProgramTitle);
                                }
                                break;
                            case BANNER_MSG_ITM_NEXT_PROGRAM_TIME:
                                if (mBasicBanner.mNextProgramDuration != null && mBasicBanner.isVisible()) {
                                    mBasicBanner.mNextProgramTime = mNavBannerImplement.getNextProgramTime();
                                    mBasicBanner.mNextProgramDuration.setText(mBasicBanner.mNextProgramTime);
                                }
                                break;
                            case BANNER_MSG_ITM_NEXT_PROGRAM_CATEGORY:
                                if (mBasicBanner.mCurrentProgramType != null && mNextEvent && mBasicBanner.isVisible()) {
                                    mBasicBanner.mNextProgramCategory = mNavBannerImplement.getNextProgramCategory();
                                    mBasicBanner.mCurrentProgramType.setText(mBasicBanner.mNextProgramCategory);
                                }
                                break;
                            case BANNER_MSG_ITM_CAPTION:
                            case BANNER_MSG_ITM_CAPTION_ICON_CHANGE:
                                // fix CR:DTV00636292
                                if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU
                                    && isSpecialState
                                    && mContext.getString(
                                    R.string.nav_hidden_channel).equals(
                                    mBasicBanner.mSpecialInfo.getText()
                                        .toString())) {
                                    break;
                                }
                                updateCaptionInfo();
                                updateSimpleCaptionInfo();
                                break;
                            //case BANNER_MSG_ITM_AUDIO_INFO_CHANGE:
                            case BANNNER_MSG_ITEM_ATMOS_CHNAGE:
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                    TAG,
                                    "BANNNER_MSG_ITEM_ATMOS_CHNAGE or BANNER_MSG_ITM_AUDIO_INFO_CHANGE------>msgData.param2="
                                        + msgData.param2);
                                if (CommonIntegration.getInstance().isCHChanging()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                        "No need to update audio info in changing channel.");
                                    break;
                                }
                                if (mNavIntegration.isCurrentSourceHDMI() && CommonIntegration.getInstance().isAudioFMTUpdated()) {
                                    if (mSimpleBanner.getVisibility() == View.VISIBLE) {
                                        mSimpleBanner.showDoblyIconOrDts();
                                        startTimeout(NAV_TIMEOUT_5);
                                    }
                                }
                                break;
                            case BANNER_MSG_ITM_AD_EYE:
                            case BANNER_MSG_ITM_AD_EAR:
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Banner show eye or ear.");
                                updateCaptionInfo();
                                break;
                            case BANNNER_MSG_ITEM_IPTS_NAME:
                                if (TurnkeyUiMainActivity.getInstance()
                                    .getChangeSource()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BANNNER_MSG_ITEM_IPTS_NAME");
                                    TurnkeyUiMainActivity.getInstance()
                                        .setChangeSource(false);
                                    showSimpleBar(false);
                                }
                                break;
                            case BANNNER_MSG_ITEM_IPTS_RSLT_CHANGE:
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BANNNER_MSG_ITEM_IPTS_RSLT_CHANGE");
                                if (!mNavIntegration.isCurrentSourceTv() && !mNavIntegration.isCHChanging()) {
                                    showSimpleBar(false);
                                }
                                break;
                            case BANNNER_MSG_ITEM_IPTS_LOCK_ICON:
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BANNNER_MSG_ITEM_IPTS_LOCK_ICON");
                                if ((mNavIntegration.isCurrentSourceTv() && mNavIntegration.isCurrentSourceBlockEx())
                                    || mNavIntegration.isCurrentSourceHDMI()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BANNNER_MSG_ITEM_IPTS_LOCK_ICON to showSimpleSource.");
                                    mSimpleBanner.showSimpleSource();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case MSG_NUMBER_KEY:
                    turnCHByNumKey();
                    break;
                case MSG_TTS_SPEAK_DELAY:
                    String ttsStr = (String) msg.obj;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in MSG_TTS_SPEAK_DELAY, ttsStr==" + ttsStr);
                    ttsUtil.speak(ttsStr, TextToSpeechUtil.QUEUE_ADD);
                    break;
                case TvCallbackConst.MSG_CB_SCAN_NOTIFY:
                    TvCallbackData msgScanData = (TvCallbackData) msg.obj;
                    if (100 == msgScanData.param2) {
                        show(false, -1, false);
                    }
                    break;
                case TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO:
                    TvCallbackData msgLogoData = (TvCallbackData) msg.obj;
                    String picPath = mNavIntegration.getChannelLogoPicPath(
                        msgLogoData.param2, msgLogoData.param3,
                        msgLogoData.param4);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in MSG_CB_BANNER_CHANNEL_LOGO, picture path =="
                            + picPath);
                    mBasicBanner.showChannelLogo(picPath);
                    break;
                case TvCallbackConst.MSG_CB_CONFIG: {
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    if (data.param1 == MtkTvConfigMsgBase.ACFG_MSG_PRE_CHG_INPUT) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pre_chg_input");
                        // tune source end
                        bSourceOnTune = false;
                    } else if (data.param1 == MtkTvConfigMsgBase.ACFG_MSG_CHG_INPUT) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chg_input");
                        if (mNavIntegration.isPipOrPopState()) {
                            break;
                        }
                        //specialType = -1;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TvCallbackConst.MSG_CB_CONFIG");
                    }
                }
                break;

                case TvCallbackConst.MSG_CB_CI_MSG: {
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "MSG_CB_CI_MSG MTKTV_CI_BEFORE_SVC_CHANGE_SILENTLY :"
                            + data.param2);
                    if (data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_BEFORE_SVC_CHANGE_SILENTLY) {
                        int hostQuietTuneStatus = mCi.getHostQuietTuneStatus();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hostQuietTuneStatus = " + hostQuietTuneStatus);
                        if (hostQuietTuneStatus == 1) {
                            isHostQuietTuneStatus = true;
                        }
                    }
                }
                break;
                case TvCallbackConst.MSG_CB_VIDEO_INFO_MSG:
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    mHDRIndex = 0;
                    if (data.param1 == MtkTvVideoInfoBase.VIDEOINFO_NFY_TYPE_HDR &&
                        data.param2 == MtkTvVideoInfoBase.VIDEOINFO_HDR_COND_CHG) {
                        mHDRIndex = data.param3;
                        if (!mNavIntegration.isCHChanging()) {
                            if (mNavIntegration.isCurrentSourceTv()) {
                                changeSourceBannerTiming(mBasicBanner.mVideoFormat);
                            } else {
                                changeSourceBannerTiming(mSimpleBanner.mThirdMiddle);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void tuneChannelAfterTuneTVSource() {
        if (mNeedChangeSource) {
            mNeedChangeSource = false;
            // mNavIntegration.selectChannelById(mTuneChannelId);
            //			mTuneChannelId = -1;
        }
    }

    public BannerView(Context context) {
        super(context);
        init(context);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimpleBanner getSimpleBanner() {
        return mSimpleBanner;
    }

    public BasicBanner getBasicBanner() {
        return mBasicBanner;
    }

    public void updateBasicBarAudio() {
        if (mBasicBanner.getVisibility() == View.VISIBLE) {
            mBasicBanner.updateAudioLanguage();
            startTimeout(NAV_TIMEOUT_5);
        }
    }

    public void updateCaptionInfo() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateCaptionInfo");
        if (mBasicBanner.getVisibility() == View.VISIBLE) {
            mBasicBanner.setCaptionVisibility();
            mBasicBanner.setEyeOrEarHinderIconVisibility();
        }
    }

    public void updateSimpleCaptionInfo() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateSimpleCaptionInfo");
        if (mNavIntegration.isCurrentSourceHDMI()) {
            mSimpleBanner.showCCView();
        }
    }

    public boolean isAudioScrambled() {
        return audioScramebled;
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFinishInflate");
        super.onFinishInflate();
    }

    @Override
    public boolean isCoExist(int componentID) {
        switch (componentID) {
            case NAV_COMP_ID_TWINKLE_DIALOG:
            case NAV_COMP_ID_EAS:
            case NAV_COMP_ID_CH_LIST:
            case NAV_COMP_ID_MENU_OPTION_DIALOG:
                return false;
            default:
                return true;
        }
    }

    @Override
    public int getPriority() {
        if (isMenuOptionShow()) {
            return NAV_PRIORITY_LOW_1;
        }
        return super.getPriority();
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        // TODO Auto-generated method stub
        PWD_SHOW_FLAG = 0;
        switch (keyCode) {
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
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler number key getVisibility"
                    + getVisibility());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler number key isCurrentSourceTv"
                    + mNavIntegration.isCurrentSourceTv());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in isKeyHandler number key getChannelAllNumByAPI"
                        + mNavIntegration.getChannelAllNumByAPI());
                if (mNavIntegration.is3rdTVSource()
                    || !mNavIntegration.isCurrentSourceTv()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not handle digtal key.");
                    startTimeout(NAV_TIMEOUT_1);
                    return true;
                }
                if (null != StateDvrFileList.getInstance()
                    && StateDvrFileList.getInstance().isShowing()) {
                    StateDvrFileList.getInstance().dissmiss();
                }

                if (View.VISIBLE != getVisibility()) {
                    if (isSourceLockedOrEmptyChannel()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in isKeyHandler number key isCurrentSourceBlockEx or no channel");
                        PWD_SHOW_FLAG = 1;
                        show(false, -1, false);
                        return true;
                    }
                    if (mNavIntegration.isCurrentSourceTv()) {
                        if (mNavIntegration.getChannelAllNumByAPI() > 0) {
                            sendTurnCHMsg(keyCode);
                        }
                    } else {
                        if (mNavIntegration.isPipOrPopState()) {
                            return true;
                        }
                        sendTurnCHMsg(keyCode);
                    }
                }
                return true;
            case KeyMap.KEYCODE_PAGE_DOWN:
			/*if (mNavIntegration.isCurrentSourceTv()
					&& !mNavIntegration.isCurrentSourceBlockEx()
					&& (mNavIntegration.getChannelAllNumByAPI() > 0)
					&& !mNavIntegration.is3rdTVSource()) {
				ComponentsManager.getInstance().hideAllComponents();
				FavChannelManager.getInstance(mContext).favAddOrErase();
			}
			changeChannelFavoriteMark();*/
                return true;
            case KeyMap.KEYCODE_MTKIR_INFO:
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in isKeyHandler KEYCODE_MTKIR_INFO");
                if (disableShowBanner(true)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in isKeyHandler,noting to do!");
                    return false;
                }
                // using info instead of KEYCODE_PERIOD.
                if (!TextUtils.isEmpty(mSelectedChannelNumString)) {
                    this.onKeyHandler(KeyMap.KEYCODE_PERIOD, null, false);
                    break;
                }
                TwinkleDialog.hideTwinkle();
                hideSundryTextView();
                hideFavoriteChannelList();
                //			isOnkeyInfo = true;
                mSimpleBanner.showCCView();
                show(true, -1, true, true);
                if ((mNavBannerImplement != null) && (ttsUtil != null)
                    && mNavIntegration.isCurrentSourceTv()
                    || mNavIntegration.is3rdTVSource()) {
                    String channelChangeTtsTexttemp = "";
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelChangeTtsTexttemp ture");
                    if (TextUtils.isEmpty(mNavBannerImplement
                        .getCurrentChannelName())) {
                        channelChangeTtsTexttemp = "channel number is "
                            + mNavBannerImplement.getCurrentChannelNum();
                    } else {
                        channelChangeTtsTexttemp = "channel number is "
                            + mNavBannerImplement.getCurrentChannelNum()
                            + " channel name is "
                            + mNavBannerImplement.getCurrentChannelName();
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "channelChangeTtsTextForKey ="
                            + channelChangeTtsTextForKey
                            + " ``` "
                            + "".equals(channelChangeTtsTextForKey)
                            + " +++ "
                            + !channelChangeTtsTextForKey
                            .equals(channelChangeTtsTexttemp)
                            + " -- "
                            + (mNavIntegration.getCurrentChannelId() > 0));
                    if ((TextUtils.equals(channelChangeTtsTextForKey, "") || !TextUtils
                        .equals(channelChangeTtsTextForKey,
                            channelChangeTtsTexttemp))
                        && mNavIntegration.getCurrentChannelId() > 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelChangeTtsTextForKey ture");
                        if (mNavIntegration.isMenuInputTvBlock()) {
                            channelChangeTtsTextForKey = "Source name is TV";
                        } else {
                            channelChangeTtsTextForKey = channelChangeTtsTexttemp;
                        }
                        sendTTSSpeakMsg(channelChangeTtsTextForKey);
                    }
                } else if ((mNavBannerImplement != null) && (ttsUtil != null)
                    && !mNavIntegration.isCurrentSourceTv()
                    && !mNavIntegration.is3rdTVSource()) {
                    String sourceName = InputSourceManager.getInstance()
                        .getCurrentInputSourceName(
                            mNavIntegration.getCurrentFocus());
                    String notTVAnd3rdTVsource = "";
                    if (!TextUtils.equals(sourceName, "")) {
                        notTVAnd3rdTVsource = "Source name is " + sourceName;
                    } else {
                        notTVAnd3rdTVsource = "Source name is null";
                    }
                    sendTTSSpeakMsg(notTVAnd3rdTVsource);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,
                    "come in isKeyHandler KEYCODE_MTKIR_INFO show(true, -1, true)");
                //			isOnkeyInfo = false;
                return true;
            case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler KEYCODE_MTKIR_MTKIR_CC");
                if (mNavIntegration.is3rdTVSource()) {
                    mNavBannerImplement.changeNextCloseCaption();
                    return false;
                }
                if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL) && CommonIntegration.getInstance().isCurrentSourceATV()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ntsc");
                } else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU
                    || MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_CN) {
                    return this.isKeyHandler(KeyMap.KEYCODE_MTKIR_SUBTITLE);
                }
                hideSundryTextView();
                hideFavoriteChannelList();
                if (!mNavIntegration.isCurrentSourceTv()
                    && !mNavIntegration.is3rdTVSource()) {
                    String ccInfo = mNavBannerImplement.getBannerCaptionInfo();
                    if (!TextUtils.isEmpty(ccInfo)) {
                        mSimpleBanner.mCCView.setVisibility(View.VISIBLE);
                        mSimpleBanner.mCCView.setText(ccInfo);
                        if (null != ttsUtil) {
                            sendTTSSpeakMsg(ccInfo);
                        }
                    } else {
                        mSimpleBanner.mCCView.setVisibility(View.GONE);
                    }
                    show(false, CHILD_TYPE_INFO_SIMPLE, false, true);
                    return true;
                } else {
                    // mSimpleBanner.mCCView.setText("");
                    mSimpleBanner.mCCView.setVisibility(View.GONE);
                }
                if (View.VISIBLE != getVisibility()) {
                    if ((mStateManage.getState() < 2) && !isSpecialState) {
                        show(false, CHILD_TYPE_INFO_BASIC, false);
                        mStateManage.setState(CHILD_TYPE_INFO_BASIC);
                    } else {
                        show(false, -1, false, true);
                    }
                }
                if (mNavSundryImplement.isFreeze()) {// freeze off
                    mNavSundryImplement.setFreeze(false);
                }

                String curCCStr = mBasicBanner.mSubtitleOrCCIcon.getText()
                    .toString();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cc Iskeyhandler~ curCCStr =" + curCCStr);
                if (null != ttsUtil && null != curCCStr && curCCStr.length() > 0) {
                    sendTTSSpeakMsg(curCCStr);
                }
                return true;
            case KeyEvent.KEYCODE_KANA:
            case KeyMap.KEYCODE_MTKIR_SUBTITLE:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler KEYCODE_MTKIR_SUBTITLE mIs3rdRunning: " + mIs3rdRunning);
                break;
            default:
                break;
        }
        return false;
    }

    private void sendTTSSpeakMsg(String ttsStr) {
        if (ttsUtil != null && ttsUtil.isTTSEnabled()) {
            Message msg = Message.obtain();
            msg.what = MSG_TTS_SPEAK_DELAY;
            msg.obj = ttsStr;
            navBannerHandler.sendMessageDelayed(msg, 1000);
        }
    }

    private boolean isSourceLockedOrEmptyChannel() {
        boolean isSourceLocked = mNavIntegration.isCurrentSourceBlockEx() &&
            (MtkTvPWDDialog.getInstance().PWDShow() == 0);

        boolean isEmptyChannel = mNavIntegration.isCurrentSourceTv() && (
            TIFChannelManager.getInstance(mContext).getCurrentSVLChannelList().size() <= 0);

        return isSourceLocked || isEmptyChannel;
    }

    public void changeFavChannel() {
        if (mNavIntegration != null && mNavIntegration.isCurrentSourceTv()) {
            if (mNavIntegration.getChannelAllNumByAPI() > 0) {
                mNavIntegration.iSetCurrentChannelFavorite();
                mBasicBanner.setFavoriteVisibility();
                show(false, -1, false);
            }
        }
    }

    private void turnCHByNumKey() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "turnCHByNumKey---->mSelectedChannelNumString="
            + mSelectedChannelNumString);
        int favIndex = mNavIntegration.getFavStateIndex();
        MtkTvChannelInfoBase chanel = null;
        int channelID = -1;
        if (mNavIntegration.getFavTypeState()) {
            try {
                int index = Integer.parseInt(mSelectedChannelNumString);
                chanel = mNavIntegration.favoriteListbyindex(
                    mNavIntegration.getSvlFromACFG(), mNavIntegration.getFavStateIndex(), --index);
                if (chanel != null) {
                    channelID = chanel.getChannelId();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            channelID = mNavIntegration
                .getChannelIDByBannerNum(mSelectedChannelNumString);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelID=" + channelID);
            chanel = CommonIntegration.getInstance()
                .getChannelById(channelID);
        }
        if (isSupportTurnCHByNumKey(chanel)) {
            mNumputChangeChannel = false;
            mSelectedChannelNumString = "";
            return;
        }
        if (CommonIntegration.isEUPARegion()
            && MarketRegionInfo
            .isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
            MtkTvChannelInfoBase curMtkTvChannelInfoBase = mNavIntegration
                .getCurChInfoByTIF();
            if (curMtkTvChannelInfoBase != null
                && curMtkTvChannelInfoBase.getBrdcstType() != chanel
                .getBrdcstType()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "for PA,not tune other tv source channel.");
                mNumputChangeChannel = false;
                mSelectedChannelNumString = "";
                return;
            }
        }
        // MtkTvChannelInfoBase.isNumberSelectable(): this API only for EURegion
        // and DTV Source
        if (CommonIntegration.isEURegion() && !chanel.isAnalogService()) {
            boolean isNumberSelectable = chanel.isNumberSelectable();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isNumberSelectable=" + isNumberSelectable);
            if (!isNumberSelectable) {
                mNumputChangeChannel = false;
                mSelectedChannelNumString = "";
                return;
            }
        }
        if (StateDvr.getInstance() != null
            && StateDvr.getInstance().isRunning() && channelID != -1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change channel stop dvr");
            if ((mNavIntegration.getCurrentChannelId() != channelID)
                || !TextUtils.equals(
                InputSourceManager.getInstance()
                    .getCurrentInputSourceName(
                        InputSourceManager.MAIN), "TV")) {
                String srctype = DvrManager.getInstance().getController()
                    .getSrcType();
                SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager.getInstance().
                    getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
                simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
                simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
                simpleDialog.setCancelText(R.string.pvr_confirm_no);
                Bundle bundle = new Bundle();
                bundle.putString("mSelectedChannelNumString", mSelectedChannelNumString);
                simpleDialog.setBundle(bundle);
                simpleDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {

                    @Override
                    public void onConfirmClick(int dialogId) {
                        // TODO Auto-generated method stub
                        DvrManager.getInstance().stopAllRunning();
                        TifTimeshiftView tifTimeshiftView = (TifTimeshiftView) ComponentsManager
                            .getInstance().getComponentById(
                                NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
                        tifTimeshiftView.stopTifTimeShift();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelnumber==" + simpleDialog.getBundle().getString("mSelectedChannelNumString"));
                        pvrChangeNum(simpleDialog.getBundle().getString("mSelectedChannelNumString"));
                    }
                }, -1);
                simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick(int dialogId) {
                        mNumputChangeChannel = false;
                        mSelectedChannelNumString = "";
                    }
                }, -1);
                if (!TextUtils.equals(srctype, "TV")
                    && !(InputSourceManager.getInstance() != null && InputSourceManager
                    .getInstance().getConflictSourceList()
                    .contains(srctype))
                    && !(chanel instanceof MtkTvAnalogChannelInfo)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "srctypeTV");
                } else {
                    if (InputSourceManager.getInstance().isCurrentTvSource(
                        InputSourceManager.MAIN)) {
                        simpleDialog.setContent(R.string.dvr_dialog_message_record_channel);
                        simpleDialog.show();
                        return;
                    } else {
                        simpleDialog.setContent(R.string.dvr_dialog_message_record_source);
                        simpleDialog.show();
                        return;
                    }
                }
            }
        }

        if (MtkTvChCommonBase.INVALID_CHANNEL_ID == channelID) { // hideAllBanner();
            show(false, -1, false);
        } else {
            if (!mNavIntegration.isCurrentSourceTv()) {
                mNavIntegration.iSetSourcetoTv(channelID);
                // mNavIntegration.selectChannelById(channelID);
                //				mTuneChannelId = channelID;
                mNeedChangeSource = true;
            } else {
                // mNavIntegration.selectChannelById(channelID);
                if (canTurnCH(chanel) || canTurnCHForFaker(chanel)
                    || isHideCH(chanel)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start selectChannelByTIFInfo");
                    if (TVContent.getInstance(mContext).isTshitRunning()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start selectChannelByTIFInfo11");
                        showDialogForTimeshiftRunning(channelID);
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start selectChannelByTIFInfo22");
                        TvSingletons
                            .getSingletons()
                            .getChannelDataManager()
                            .selectChannelByTIFInfo(
                                TvSingletons.getSingletons()
                                    .getChannelDataManager()
                                    .getHideChannelById(channelID));
                    }
                }
            }
        }
        mNumputChangeChannel = false;
        mSelectedChannelNumString = "";
    }

    private void showDialogForTimeshiftRunning(int channelID) {
        SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager.getInstance().
            getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
        simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
        simpleDialog.setCancelText(R.string.pvr_confirm_no);
        simpleDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {

            @Override
            public void onConfirmClick(int dialogId) {
                DvrManager.getInstance().stopAllRunning();
                if (TifTimeShiftManager.getInstance() != null) {
                    TifTimeShiftManager.getInstance().stopAll();
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDialogForTimeshiftRunning");
                TvSingletons
                    .getSingletons()
                    .getChannelDataManager()
                    .selectChannelByTIFInfo(
                        TvSingletons.getSingletons()
                            .getChannelDataManager()
                            .getHideChannelById(channelID));
            }
        }, -1);
        simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
            @Override
            public void onCancelClick(int dialogId) {
                simpleDialog.dismiss();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start selectChannelByTIFInfo cancel");
            }
        }, -1);
        simpleDialog.setContent(R.string.dvr_dialog_message_timeshift_channel);
        simpleDialog.show();
    }

    private boolean isSupportTurnCHByNumKey(MtkTvChannelInfoBase chanel) {
        if (chanel == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chanel==null");
            return true;
        }
        if (chanel.isUserDelete() && (CommonIntegration.isEUPARegion() || CommonIntegration.isEURegion())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "chanel.isUserDelete(): " + chanel.isUserDelete());
            return true;
        }
        return false;
    }

    private boolean canTurnCH(MtkTvChannelInfoBase chanel) {
        final int mask;
        final int maskValue;
        if (mNavIntegration.isCurrentSourceATVforEuPA()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change analog mask!");
            mask = CommonIntegration.CH_LIST_ANALOG_MASK;
            maskValue = CommonIntegration.CH_LIST_ANALOG_VAL;
        } else {
            mask = SaveValue.getInstance(mContext).readValue(
                CommonIntegration.channelListfortypeMask,
                CommonIntegration.CH_LIST_MASK);
            maskValue = SaveValue.getInstance(mContext).readValue(
                CommonIntegration.channelListfortypeMaskvalue,
                CommonIntegration.CH_LIST_VAL);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mask=" + mask + ",maskValue=" + maskValue);
        }
        boolean canTurnCH = mNavIntegration
            .checkChMask(chanel, mask, maskValue);
        canTurnCH &= TIFFunctionUtil.checkChCategoryMask(chanel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "canTurnCH=" + canTurnCH);
        return canTurnCH;
    }

    private boolean isHideCH(MtkTvChannelInfoBase chanel) {
        boolean isHideCH = !mNavIntegration.checkChMask(chanel,
            CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHideCH=" + isHideCH);
        return isHideCH;
    }

    /**
     *
     */
    private boolean canTurnCHForFaker(MtkTvChannelInfoBase chanel) {
        if (!(mNavIntegration.isUSRegion() || mNavIntegration.isSARegion())) {
            return false;
        }
        boolean canTurnCHforFaker = mNavIntegration.checkChMask(chanel,
            CommonIntegration.CH_FAKE_MASK, CommonIntegration.CH_FAKE_VAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "canTurnCHforFaker=" + canTurnCHforFaker);
        return canTurnCHforFaker;
    }

    private void sendTurnCHMsg(int keyCode) {
        inputChannelNum(keyCode);
        mSimpleBanner.updateInputting(mSelectedChannelNumString);
        startTimeout(NAV_TIMEOUT_5);
        navBannerHandler.removeMessages(MSG_NUMBER_KEY);
        navBannerHandler.sendEmptyMessageDelayed(MSG_NUMBER_KEY, NAV_TIMEOUT_3);
    }

    private boolean cckeysend = false;

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        // TODO Auto-generated method stub
        int navcompid = ComponentsManager.getNativeActiveCompId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "navcompid=" + navcompid);
        if (navcompid == NavBasic.NAV_NATIVE_COMP_ID_MHEG5) {
            if (event != null) {
                ComponentsManager.nativeComponentReActive();
                return false;
            } else {
                ComponentsManager.updateActiveCompId(false, componentID);
            }
        }
        boolean isHandler = true;
        switch (keyCode) {
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
            case KeyMap.KEYCODE_PERIOD:

                if (mNavIntegration.is3rdTVSource()
                    || !mNavIntegration.isCurrentSourceTv()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not handle digtal key.");
                    startTimeout(NAV_TIMEOUT_1);
                    return true;
                }
                if (isSourceLockedOrEmptyChannel()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in onKeyHandler number key isCurrentSourceBlockEx or no channel");
                    startTimeout(NAV_TIMEOUT_5);
                    return true;
                }
                if (((MarketRegionInfo.REGION_EU == MarketRegionInfo
                    .getCurrentMarketRegion()) || (mSelectedChannelNumString
                    .length() == 0))
                    && (keyCode == KeyMap.KEYCODE_PERIOD)) {
                    return true;
                }
                if (mNavIntegration.isCurrentSourceTv()) {
                    if (mNavIntegration.getChannelAllNumByAPI() > 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in input key number 1");
                        sendTurnCHMsg(keyCode);
                    }
                } else {
                    if (mNavIntegration.isPipOrPopState()) {
                        return true;
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in input key number 2");
                    sendTurnCHMsg(keyCode);
                }
                break;
            case KeyMap.KEYCODE_BACK:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyhandle KEYCODE_BACK");
                isHandler = true;
                if (mNumputChangeChannel) {
                    cancelNumChangeChannel();
                }
                if (getVisibility() != View.GONE) {
                    setVisibility(View.GONE);
                }
                break;
            case KeyMap.KEYCODE_PAGE_DOWN:
			/*com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_EJECT");
			if (mNavIntegration.isCurrentSourceTv()
					&& !mNavIntegration.isCurrentSourceBlockEx()
					&& (mNavIntegration.getChannelAllNumByAPI() > 0)
					&& !mNavIntegration.is3rdTVSource()) {
				FavChannelManager.getInstance(mContext).favAddOrErase();
			}
			changeChannelFavoriteMark();*/
                break;
            case KeyMap.KEYCODE_MTKIR_INFO:
                // using info instead of KEYCODE_PERIOD.
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in onKeyHandler KEYCODE_MTKIR_INFO");
                if (!TextUtils.isEmpty(mSelectedChannelNumString)) {
                    this.onKeyHandler(KeyMap.KEYCODE_PERIOD, null, false);
                    break;
                }
                hideSundryTextView();
                hideFavoriteChannelList();
                //			isOnkeyInfo = true;
                show(true, -1, true, true);
                //			isOnkeyInfo = false;
                if (mNavIntegration.isPipOrPopState()) {
                    if (MarketRegionInfo
                        .isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
                        // FocusLabelControl in TIF
                        if (MarketRegionInfo
                            .isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
                            ((MultiViewControl) mComponentsManager
                                .getComponentById(NAV_COMP_ID_POP))
                                .reShowFocus();
                        } else {
                            ((TVStateControl) mComponentsManager
                                .getComponentById(NAV_COMP_ID_POP))
                                .reShowFocus();
                        }
                    } else {
                        // FocusLabelControl
                        ((FocusLabelControl) mComponentsManager
                            .getComponentById(NAV_COMP_ID_POP)).reShowFocus();
                    }
                }
                break;
            case KeyMap.KEYCODE_MTKIR_MTKIR_CC:

                if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL) && CommonIntegration.getInstance().isCurrentSourceATV()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ntsc");
                } else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU
                    || MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_CN) {
                    return onKeyHandler(KeyMap.KEYCODE_MTKIR_SUBTITLE, null, false);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in KeyHandler KEYCODE_MTKIR_MTKIR_CC");
                // mBasicBanner.changeNextCcOrSubtitleValue();
                hideSundryTextView();
                hideFavoriteChannelList();
                cckeysend = true;
                if ((mStateManage.getState() < 2) && !isSpecialState) {
                    show(false, CHILD_TYPE_INFO_BASIC, false, true);
                    mStateManage.setState(CHILD_TYPE_INFO_BASIC);
                } else {
                    mNavBannerImplement.changeNextCloseCaption();
                    mBasicBanner.setCaptionVisibility();
                }

                break;
            case KeyEvent.KEYCODE_KANA:
            case KeyMap.KEYCODE_MTKIR_SUBTITLE:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in keyHandle KEYCODE_MTKIR_SUBTITLE mIs3rdRunning: " + mIs3rdRunning);
                //			if (!mNavIntegration.isCurrentSourceTv() ||
                //			        (disableShowBanner(true) && (keyCode == KeyMap.KEYCODE_MTKIR_SUBTITLE))) {
                //				break;
                //			} else if ((MarketRegionInfo.REGION_EU == MarketRegionInfo
                //					.getCurrentMarketRegion())
                //					|| (MarketRegionInfo.REGION_CN == MarketRegionInfo
                //							.getCurrentMarketRegion())) {
                //				// event==null means passKeyToNative return keyEvent.
                //				if (mNavBannerImplement.isCurrentSourceATV() && event != null) {
                //					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "close teletext");
                //					KeyDispatch.getInstance().passKeyToNative(keyCode, event);
                //				} else {
                //					if ((mStateManage.getState() < 2) && !isSpecialState) {
                //						show(false, CHILD_TYPE_INFO_BASIC, false, true);
                //						mStateManage.setState(CHILD_TYPE_INFO_BASIC);
                //					} else {
                //						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                //								"subtitile keyevent~ change next close caption!");
                //						mNavBannerImplement.changeNextCloseCaption();
                //					}
                //				}
                //			} else {
                isHandler = false;
                //			}
                break;
            case KeyMap.KEYCODE_DPAD_CENTER:
                if (mNumputChangeChannel) {
                    // mNavIntegration.singleRFScan(Float.valueOf(mSelectedChannelNumString));
                    navBannerHandler.removeMessages(MSG_NUMBER_KEY);
                    navBannerHandler.sendEmptyMessage(MSG_NUMBER_KEY);
                } else {
                    isHandler = false;
                }
                break;
            case KeyMap.KEYCODE_DPAD_DOWN:
            case KeyMap.KEYCODE_DPAD_UP:
                mChannelListDialog = (ChannelListDialog) mComponentsManager
                    .getComponentById(NAV_COMP_ID_CH_LIST);
                mFavoriteChannelListView = (FavoriteListDialog) mComponentsManager
                    .getComponentById(NAV_COMP_ID_FAV_LIST);
                if (!mChannelListDialog.isShowing()
                    && !mFavoriteChannelListView.isShowing()
                    && mDetailBanner.getDetailBannerVisible()) {
                    if (KeyMap.KEYCODE_DPAD_UP == keyCode) {
                        mDetailBanner.pageUp();
                    } else {
                        mDetailBanner.pageDown();
                    }
                    //				int detailPageNum = mDetailBanner.detailTextReader
                    //						.getCurPagenum();
                    int detailTotalPageNum = mDetailBanner.detailTextReader
                        .getTotalPage();
                    // if(detailPageNum<= 1 || detailPageNum>=detailTotalPageNum){
                    // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DetailText now is first page or last page!!");
                    // isHandler = false;
                    // startTimeout(NAV_TIMEOUT_5);
                    // }
                    if (detailTotalPageNum <= 1) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "Detail page total num<=1,not to deal,pass to KEYCODE_DPAD_UP ");
                        isHandler = false;
                        startTimeout(NAV_TIMEOUT_5);
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "num>1,deal it,so it will change channel,but to change page.");
                        isHandler = true;
                    }
                } else {
                    isHandler = false;
                }
                break;
            case KeyMap.KEYCODE_DPAD_LEFT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in keyHandle KEYCODE_DPAD_LEFT mNextEvent: " + mNextEvent);
                if (MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
                    isHandler = false;
                    break;
                }
                mNextEvent = false;
                show(false, -1, true, true);
                break;
            case KeyMap.KEYCODE_DPAD_RIGHT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in keyHandle KEYCODE_DPAD_RIGHT mNextEvent: " + mNextEvent);
                if (MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
                    isHandler = false;
                    break;
                }
                mNextEvent = true;
                show(false, -1, true, true);
                break;
            default:
                isHandler = false;
                break;
        }
        if (isHandler) {
            startTimeout(NAV_TIMEOUT_5);
        }
        return isHandler;
    }

    @Override
    public void setVisibility(int visibility) {
        // if (StateFileList.getInstance() != null &&
        // StateFileList.getInstance().isShowing()) {
        // return;
        // }
        if (StateDvrPlayback.getInstance() != null
            && StateDvrPlayback.getInstance().isRunning()) {
            hideAllBanner();
            return;
        }
        MenuOptionMain menuOptionMain = (MenuOptionMain) mComponentsManager
            .getComponentById(NAV_COMP_ID_MENU_OPTION_DIALOG);
        if (menuOptionMain != null && menuOptionMain.isShowing()) {
            hideAllBanner();
            return;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in BannerView setVisibility ==" + visibility);
        if (View.VISIBLE == visibility) {
            if (mNavIntegration.isCurrentSourceDTV() &&
                isZiggoOperator() && (mNavBannerImplement.isCurrentHiddenChannel() ||
                isHostQuietTuneStatus ||
                mCi.getSasCbctState() == 1)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Ziggo operator and hidden channel will can not show banner.");
                super.setVisibility(GONE);
                return;
            }
            if (mNavIntegration.isCurrentSourceTv() &&
                TIFChannelManager.getInstance(mContext).getCurrChannelInfo() == null &&
                !isSpecialState) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "no current channel, return.");
                return;
            }
        } else {
            mDisposables.clear();
            navBannerHandler.removeMessages(SHOW_BASIC_BANNER_AFTER_SVC_CHANGE);
            hideAllBanner();
            channelChangeTtsTextForKey = "";
            mSelectedChannelNumString = "";
        }
        // if (getVisibility() != visibility) {
        super.setVisibility(visibility);
        // }
    }

    private void init(Context context) {
        //		mCIState = CIStateChangedCallBack.getInstance(context);
        mCi = CIStateChangedCallBack.getInstance(mContext).getCIHandle();
        componentID = NAV_COMP_ID_BANNER;
        bannerView = this;
        ttsUtil = new TextToSpeechUtil(context);
        mStateManage = new StateManage();
        mStateManage.addState(CHILD_TYPE_INFO_NONE);
        mStateManage.addState(CHILD_TYPE_INFO_SIMPLE);
        mStateManage.addState(CHILD_TYPE_INFO_BASIC);
        mStateManage.addState(CHILD_TYPE_INFO_DETAIL);
        mStateManage.setState(CHILD_TYPE_INFO_BASIC);
        inflate(mContext, R.layout.nav_banner_layout, this);
        mNavIntegration = CommonIntegration.getInstance();
        BannerImplement.setBannerImplement(mNavBannerImplement = new BannerImplement(mContext));
        mSimpleBanner = new SimpleBanner(mContext);
        mBasicBanner = new BasicBanner(mContext);
        mDetailBanner = new DetailBanner(mContext);
        bannerLayout = (View) findViewById(R.id.banner_info_layout);
        topBanner = (View) findViewById(R.id.banner_top_layout);
        float scale = mContext.getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
            (int) (ScreenConstant.SCREEN_WIDTH),
            (int) (BANNER_BASIC_HEIGHT * scale + 0.5f));

        topBanner.setLayoutParams(mParams);
        mBasicBanner.initView();
        mSimpleBanner.initView();
        mDetailBanner.initView();
        setAlpha(0.9f);
        mNavSundryImplement = SundryImplement
            .getInstanceNavSundryImplement(context);
        mComponentsManager = ComponentsManager.getInstance();

        mBannerTvCallbackHandler = TvCallbackHandler.getInstance();

        mBannerTvCallbackHandler.addCallBackListener(
            TvCallbackConst.MSG_CB_BANNER_MSG, navBannerHandler);
        mBannerTvCallbackHandler.addCallBackListener(
            TvCallbackConst.MSG_CB_CI_MSG, navBannerHandler);
        mBannerTvCallbackHandler.addCallBackListener(
            TvCallbackConst.MSG_CB_CONFIG, navBannerHandler);
        mBannerTvCallbackHandler.addCallBackListener(
            TvCallbackConst.MSG_CB_SCAN_NOTIFY, navBannerHandler);
        mBannerTvCallbackHandler.addCallBackListener(
            TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO, navBannerHandler);
        mBannerTvCallbackHandler.addCallBackListener(
            TvCallbackConst.MSG_CB_VIDEO_INFO_MSG, navBannerHandler);

        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_KEY_OCCUR, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_COMPONENT_SHOW, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_COMPONENT_HIDE, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_PAUSE, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_RESUME, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_CONTENT_ALLOWED, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_CH_CHANGE_BY_DIGITAL, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_OAD_STATE, this);
        if (CommonIntegration.isSARegion()) {
            channelNumSeparator = ".";
        } else if (CommonIntegration.isUSRegion()) {
            channelNumSeparator = "-";
        }
    }

    @Override
    public boolean startComponent() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in startComponent");
        if (mNavIntegration.is3rdTVSource()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in startComponent is 3rd source.");
            return true;
        }

        if (DestroyApp.isCurActivityTkuiMainActivity()) {
            if (!mNumputChangeChannel) {
                if (mNavIntegration.isCurrentSourceTv()) {
                    boolean hasChannel = true;
                    if (mNavIntegration.getChannelNumByAPIForBanner() <= 0) {
                        hasChannel = false;
                        if (!mNavIntegration.isCurrentSourceATVforEuPA()) {
                            List<TIFChannelInfo> list3rd = TIFChannelManager.getInstance(mContext).get3RDChannelList();
                            if (list3rd != null && !list3rd.isEmpty()) {
                                hasChannel = true;
                            }
                        }
                    }
                    if (!hasChannel) {
                        isSpecialState = true;
                        specialType = SPECIAL_NO_CHANNEL;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "please scan channel!!!");
                        return true;
                    } else {
                        showSimpleBar(false);
                        if (!isSpecialState) {
                            if (!mNavIntegration.isCurrentSourceBlockEx()) {
                                setBannerState(BannerView.CHILD_TYPE_INFO_BASIC);
                            }
                        }
                    }
                }
                show(false, -1, false);
            }
        }

        return true;
    }

    @Override
    public boolean deinitView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deinitView");
        mDisposables.clear();
        mBannerTvCallbackHandler.removeCallBackListener(
            TvCallbackConst.MSG_CB_BANNER_MSG, navBannerHandler);
        mBannerTvCallbackHandler.removeCallBackListener(
            TvCallbackConst.MSG_CB_CI_MSG, navBannerHandler);
        mBannerTvCallbackHandler.removeCallBackListener(
            TvCallbackConst.MSG_CB_CONFIG, navBannerHandler);
        mBannerTvCallbackHandler.removeCallBackListener(
            TvCallbackConst.MSG_CB_SCAN_NOTIFY, navBannerHandler);
        mBannerTvCallbackHandler.removeCallBackListener(
            TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO, navBannerHandler);
        mBannerTvCallbackHandler.removeCallBackListener(
            TvCallbackConst.MSG_CB_VIDEO_INFO_MSG, navBannerHandler);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_BANNER_MSG);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_CI_MSG);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_CONFIG);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_SCAN_NOTIFY);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_VIDEO_INFO_MSG);
        if (mGetTimeThread != null) {
            mGetTimeThread.quitSafely();
        }
        if (mGetTimeHandler != null) {
            mGetTimeHandler.removeCallbacksAndMessages(null);
        }
        return super.deinitView();
    }

    class StateManage {

        private final List<Integer> stateArray = new ArrayList<Integer>();
        private int curState = -1;

        private boolean orietation = true;

        private void addState(int state) {
            stateArray.add(state);
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "stateArray.add(state); ");
            curState = 0;
        }

        private void setState(int state) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "StateManage setState " + state);
            for (int temp : stateArray) {
                if (temp == state) {
                    curState = stateArray.indexOf(temp);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "StateManage setState curState " + curState);
                }
            }
        }

        private int getState() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "StateManage getState " + curState);
            return stateArray.get(curState);
        }

        private int getNextState() {
            int temp;
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "curState " + curState);
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "stateArray.size() " + stateArray.size());
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "orietation " + orietation);
            if (orietation) {
                temp = curState + 1;
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "orietation " + orietation);
                if (temp >= stateArray.size()) {
                    orietation = false;
                    temp = curState - 1;
                } else {
                    return stateArray.get(temp);
                }
            } else {
                temp = curState - 1;
                if (temp < 0) {
                    orietation = true;
                    temp = curState + 1;
                } else {
                    return stateArray.get(temp);
                }
            }
            return stateArray.get(temp);
        }

        private void setOrietation(boolean flag) {
            this.orietation = flag;
        }
    }

    // BannerView simple banner
    class SimpleBanner {

        private final Context mContext;

        private View mSelfLayout;

        /**
         * for channel number or source name
         */
        private TextView mFirstLine;
        private ImageView mImgDoblyType;
        private ImageView mImgDoblyVision;
        private TextView mDtsType;
        private View mSecondLine;
        private TextView mChannelName;
        private ImageView mLockIcon;

        private View mThirdLine;
        public TextView mCCView;
        /**
         * resolution
         */
        private TextView mThirdMiddle;
        private View mTypeTimeLayout;
        private TextView mReceiverType;
        private TextView mTime;
        private TextView mDate;

        public boolean isAnimation = false;
        public boolean isCanUpdateTime = true;

        public SimpleBanner(Context context) {
            // TODO Auto-generated constructor stub
            mContext = context;
        }

        private void initView() {

            mSelfLayout = (View) findViewById(R.id.banner_simple_layout);
            mSelfLayout
                .setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            mSelfLayout.setVisibility(View.INVISIBLE);
            mImgDoblyType = (ImageView) findViewById(R.id.banner_dolby_type_simple);
            mDtsType = (TextView) findViewById(R.id.banner_dts_type_simple);
            mImgDoblyVision = (ImageView) findViewById(R.id.banner_dolby_vision_simple);
            mFirstLine = (TextView) findViewById(R.id.banner_simple_first_line);
            mSecondLine = (View) findViewById(R.id.banner_simple_second_line_layout);
            mChannelName = (TextView) findViewById(R.id.banner_simple_channel_name);
            mLockIcon = (ImageView) findViewById(R.id.banner_simple_lock_icon);
            mThirdLine = (View) findViewById(R.id.banner_simple_third_line_layout);
            mThirdMiddle = (TextView) findViewById(R.id.banner_simple_third_middle);
            mCCView = (TextView) findViewById(R.id.banner_simple_cc);
            mTypeTimeLayout = (View) findViewById(R.id.banner_simple_type_time_layout);
            mReceiverType = (TextView) findViewById(R.id.banner_simple_receiver_type);
            mTime = (TextView) findViewById(R.id.banner_simple_time);
            String[] dateTime = mNavBannerImplement.getCurrentDateTime();
            if (MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
                mDate = (TextView) findViewById(R.id.banner_simple_time_date);
                mTime.setText(dateTime[1]);
                mDate.setText(dateTime[2]);
            } else {
                mTime.setText(dateTime[0]);
                mTime.setGravity(Gravity.CENTER_VERTICAL);
            }

            mGetTimeThread = new HandlerThread("timeThread");
            mGetTimeThread.start();
            mGetTimeHandler = new Handler(mGetTimeThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "try change time");
                    if (!isCanUpdateTime) {
                        removeMessages(MSG_UPDATE_TIME);
                        return;
                    }
                    updateTime();
                    sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                }
            };
        }

        private void updateTime() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "try change time.");
            final String[] values = mNavBannerImplement.getCurrentDateTime();
            mTime.post(new Runnable() {
                @Override
                public void run() {
                    if (!isCanUpdateTime) {
                        mGetTimeHandler.removeMessages(MSG_UPDATE_TIME);
                        return;
                    }
                    if (mDate != null) {
                        if (!TextUtils.equals(values[1], mTime.getText())) {
                            mTime.setText(values[1]);
                        }
                        if (!TextUtils.equals(values[2], mDate.getText())) {
                            mDate.setText(values[2]);
                        }
                    } else {
                        if (!TextUtils.equals(values[0], mTime.getText())) {
                            mTime.setText(values[0]);
                        }
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "try change time success");
                }
            });
        }

        public int getVisibility() {
            return mSelfLayout.getVisibility();
        }

        public boolean isVisible() {
            return getVisibility() == VISIBLE;
        }

        public int getChannelNameVisibility() {
            return mChannelName.getVisibility();
        }

        public String getFirstLineStr() {
            return mFirstLine.getText().toString();
        }

        private void show(boolean isBasicShow) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
            mImgDoblyType.setVisibility(View.GONE);
            mImgDoblyVision.setVisibility(View.GONE);
            mDtsType.setVisibility(View.GONE);
            boolean doShow = true;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in show SimpleBar||isBasicShow =" + isBasicShow);
            if (isHostQuietTuneStatus) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "silently change.");
                return;
            }
            if (mNavIntegration.isCurrentSourceTv()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "specialType: " + specialType);
                if ((isZiggoOperator() && mNavBannerImplement.isCurrentHiddenChannel())
                    || (specialType == SPECIAL_NO_CHANNEL)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Ziggo operator and hidden channel or no channel will can not show simple banner.");
                    return;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SimpleBar shown");
            }
            if (getVisibility() != View.VISIBLE) {
                mSelfLayout.setBackgroundResource(R.drawable.nav_infobar_basic_bg);
                mSelfLayout.setVisibility(View.VISIBLE);
            }
            //			if (ttsUtil != null && ttsUtil.isTTSEnabled()) {
            //                if (mChannelName.hasFocus()) {
            //                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FocusCheck--->clearFocus----");
            //                    mChannelName.clearFocus();
            //                }
            //            } else {
            //                if (!mChannelName.hasFocus()) {
            //                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FocusCheck--->requestFocus----");
            //                    mChannelName.setFocusable(true);
            //                    mChannelName.requestFocus();
            //                }
            //            }
            mChannelName.setSelected(true);
            if (mNavIntegration.is3rdTVSource()) {
                PwdDialog pwdDialog = (PwdDialog) ComponentsManager
                    .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_PWD_DLG);
                boolean isBlockFor3rd = pwdDialog.isContentBlock(true);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBlockFor3rd=" + isBlockFor3rd);
                if (mNavIntegration.isCurrentSourceBlockEx() && isBlockFor3rd) {
                    showSimpleSource(isBlockFor3rd);
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in test step1");
                    int channelSize3rd = mNavIntegration
                        .getAllChannelListByTIFFor3rdSource();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in test step1 channelSize3rd="
                        + channelSize3rd);
                    if (channelSize3rd <= 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in test step3");
                        showSimpleTime();
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in test step2");
                        showSimpleChannel();
                        showTime(doShow);
                    }
                }
            } else if (mNavIntegration.isCurrentSourceTv()) {
                if (mNavIntegration.isCurrentSourceBlockEx()) {
                    showSimpleSource();
                } else {
                    if (TIFChannelManager.getInstance(mContext).getCurrChannelInfo() == null) {
                        showSimpleTime();
                    } else {
                        showSimpleChannel();
                        showTime(doShow);
                    }
                }
            } else {
                InputSourceManager ism = InputSourceManager.getInstance();
                String curInputName = ism
                    .getCurrentInputSourceName(mNavIntegration
                        .getCurrentFocus());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curInputName: " + curInputName);
                if (curInputName != null && curInputName.contains("TV")
                    && !mNavIntegration.is3rdTVSource()
                    && !mNavIntegration.isCurrentSourceHDMI()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not TV source but get a 'TV' inputname");
                    return;// fix CR:DTV00700331 DTV00718383
                } else {
                    showSimpleSource();
                }
            }
        }

        private void showTime(boolean doShow) {
            isCanUpdateTime = doShow;
            if (doShow) {
                mTime.setVisibility(View.VISIBLE);
                if (mDate != null) {
                    mDate.setVisibility(View.VISIBLE);
                }
                mGetTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            } else {
                mTime.setVisibility(View.INVISIBLE);
                mTime.setText(null);
                if (mDate != null) {
                    mDate.setVisibility(View.INVISIBLE);
                    mDate.setText(null);
                }
                mGetTimeHandler.removeMessages(MSG_UPDATE_TIME);
            }
        }

        private void updateInputting(String num) {
            if (View.VISIBLE != bannerLayout.getVisibility()) {
                bannerLayout.setVisibility(View.VISIBLE);
            }
            hideChannelListDialog();
            hideFavoriteChannelList();
            mBasicBanner.hide();
            mDetailBanner.hide();
            mSelfLayout.setVisibility(View.VISIBLE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in updateInputting set visible == "
                + mSelfLayout.getVisibility());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in updateInputting topBanner visible == "
                + topBanner.getVisibility());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in updateInputting bannerLayout visible == "
                + bannerLayout.getVisibility());
            // .getAvailableWindowId();
            // mSimpleBannerViewRect = getRectInScreen(mSelfLayout);
            // Log.i(TAG,
            // "mSimpleBannerViewWindowId:"+mSimpleBannerViewWindowId);
            // mBypassWindowManager.setBypassWindow(true,
            // mSimpleBannerViewWindowId, mSimpleBannerViewRect);
            mSelfLayout.setBackgroundResource(R.drawable.nav_infobar_basic_bg);
            mFirstLine.setVisibility(View.VISIBLE);
            mFirstLine.setText(num);
            mLockIcon.setVisibility(View.GONE);
            mChannelName.setVisibility(View.INVISIBLE);
            mTypeTimeLayout.setVisibility(View.INVISIBLE);
            mThirdMiddle.setVisibility(View.GONE);

            // this may be changed if the type can be set
            mReceiverType.setVisibility(View.INVISIBLE);
            showTime(false);
        }

        public void hide() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in SimpleBanner hide()");
            if (mSelfLayout.getVisibility() == VISIBLE) {
                mSelfLayout.setVisibility(View.INVISIBLE);
            }
            mThirdMiddle.setText(null);
            isCanUpdateTime = false;
            mGetTimeHandler.removeMessages(MSG_UPDATE_TIME);
        }

        public void setSimpleBannerBg(boolean isShow) {
            if (isShow) {
                mSelfLayout
                    .setBackgroundResource(R.drawable.nav_infobar_basic_bg);
            } else {
                mSelfLayout
                    .setBackgroundResource(R.drawable.translucent_background);
            }
        }

        private void showCCView() {
            if (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_EU) {
                if (!mNavIntegration.isCurrentSourceTv()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCCView----> other source");
                    String ccInfo = mNavBannerImplement
                        .getBannerCaptionInfo();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCCView---->CCString: " + ccInfo);
                    if (!TextUtils.isEmpty(ccInfo)) {
                        mCCView.setVisibility(View.VISIBLE);
                        mCCView.setText(ccInfo);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCCView---->mCCView normal...");
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCCView---->mCCView gone1111...");
                        mCCView.setVisibility(View.GONE);
                    }
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCCView---->mCCView gone2222...");
                    mCCView.setVisibility(View.GONE);
                }
            }
        }

        private void showSimpleSource() {
            boolean showLock = showInputLockIcon();//mNavBannerImplement.isShowInputLockIcon();
            showSimpleSource(showLock);
        }

        private void showDoblyIconOrDts() {
            int doblyType = mNavBannerImplement.getDoblyType();
            String dtsAudioInfo = mNavBannerImplement.getDtsAudioInfo();
            boolean isDoblyVision = mNavBannerImplement.isDolbyVision();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDoblyIcon:doblyType=" + doblyType + ", dtsAudioInfo=" + dtsAudioInfo + ",isDoblyVision=" + isDoblyVision);
            mImgDoblyVision.setImageResource(R.drawable.icon_dobly_vision);
            mImgDoblyVision.setVisibility(isDoblyVision ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(dtsAudioInfo)) {
                mDtsType.setText(dtsAudioInfo);
                mDtsType.setVisibility(View.VISIBLE);
                mImgDoblyType.setVisibility(View.GONE);
            } else if (doblyType == BannerImplement.DOBLY_TYPE_ATOMS) {
                mImgDoblyType.setImageResource(R.drawable.icon_dobly_atoms);
                mImgDoblyType.setVisibility(View.VISIBLE);
                mDtsType.setVisibility(View.GONE);
            } else if (doblyType == BannerImplement.DOBLY_TYPE_AUDIO) {
                mImgDoblyType.setImageResource(R.drawable.icon_dobly_audio);
                mImgDoblyType.setVisibility(View.VISIBLE);
                mDtsType.setVisibility(View.GONE);
            } else {
                mImgDoblyType.setImageResource(0);
                mImgDoblyType.setVisibility(View.GONE);
                mDtsType.setText("");
                mDtsType.setVisibility(View.GONE);
            }
        }

        private boolean showInputLockIcon() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showLockIcon specialType=" + specialType);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showLockIcon source block=" + mNavIntegration.isCurrentSourceBlockEx());
            boolean sourceBlock = mNavIntegration.isCurrentSourceBlockEx();
            boolean isInputBlock = sourceBlock && (specialType == SPECIAL_INPUT_LOCK || mNavBannerImplement.isShowInputLockIcon());
            if (mNavIntegration.is3rdTVSource()) {
                PwdDialog pwdDialog = (PwdDialog) ComponentsManager
                    .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_PWD_DLG);
                boolean isBlockFor3rd = pwdDialog.isContentBlock(true);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBlockFor3rd=" + isBlockFor3rd);
                return isInputBlock && isBlockFor3rd;
            }
            return isInputBlock;
        }

        private void showSimpleSource(boolean showLock) {
            String sourceName = InputSourceManager.getInstance()
                .getCurrentInputSourceName(
                    mNavIntegration.getCurrentFocus());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentInputName: " + sourceName);
            String privSName = CommonIntegration.getInstance().getPrivSimBFirstLine();
            mImgDoblyVision.setVisibility(View.GONE);
            mBasicBanner.hide();
            mDetailBanner.hide();
            mImgDoblyType.setVisibility(View.GONE);
            mDtsType.setVisibility(View.GONE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isAudioFMTUpdated=" + CommonIntegration.getInstance().isAudioFMTUpdated());
            if (CommonIntegration.getInstance().isAudioFMTUpdated()
                && mNavIntegration.isCurrentSourceHDMI()) {
                showDoblyIconOrDts();
            }
            if (mNavIntegration.is3rdTVSource() && !showLock) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "only show source name~");
                mLockIcon.setVisibility(View.INVISIBLE);
                mCCView.setVisibility(View.GONE);
                mThirdMiddle.setVisibility(View.INVISIBLE);
                mChannelName.setText(mNavBannerImplement
                    .getCurrentChannelName());
                mChannelName.setVisibility(View.VISIBLE);
                mFirstLine.setVisibility(View.GONE);
                return;
            }
            mFirstLine.setVisibility(View.VISIBLE);
            mFirstLine.setText(sourceName);
            mFirstLine.setSelected(true);
            mChannelName.setVisibility(View.GONE);
            mTypeTimeLayout.setVisibility(View.GONE);
            if (sourceName != null && !sourceName.equals(privSName)) {
                mThirdMiddle.setText("");
                CommonIntegration.getInstance().setPrivSimBFirstLine(sourceName);
            }

            if (showLock) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "VISIBLE~ ");
                mLockIcon.setVisibility(View.VISIBLE);
                mThirdMiddle.setVisibility(View.GONE);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getIptsRslt INVISIBLE~ " + mThirdMiddle.getText());
                mLockIcon.setVisibility(View.INVISIBLE);
                if (mThirdMiddle.getVisibility() != View.VISIBLE) {
                    mThirdMiddle.setText(null);
                    mThirdMiddle.setVisibility(View.VISIBLE);
                }
                changeSourceBannerTiming(mSimpleBanner.mThirdMiddle);
            }
        }

        private void showSimpleChannel() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSimpleChannel~ ");
            mFirstLine.setVisibility(View.VISIBLE);
            if (mNavIntegration.isFavStateInChannelList()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSimpleChannel favChannelInfo start.");
                favChannelInfo();
            } else {
                mFirstLine.setText(mNavBannerImplement.getCurrentChannelNum());
            }
            mChannelName.setVisibility(View.VISIBLE);
            mChannelName.setText(mNavBannerImplement.getCurrentChannelName());
            if (CommonIntegration.getInstance().isCurCHAnalog()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ATV channel no need to show mReceiverType.");
                mReceiverType.setVisibility(View.INVISIBLE);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "It will show for others.");
                mReceiverType.setVisibility(View.VISIBLE);
                mReceiverType.setText(mNavBannerImplement.getTVTurnerMode());
            }
            mLockIcon.setVisibility(View.GONE);
            mCCView.setVisibility(View.GONE);
            mTypeTimeLayout.setVisibility(View.VISIBLE);
            mThirdMiddle.setVisibility(View.GONE);
        }

        private void favChannelInfo() {
            MtkTvChannelInfoBase mtkchannelinfo = mNavIntegration.getCurChInfoByTIF();
            int favType = SaveValue.getInstance(mContext).readValue(
                CommonIntegration.CH_TYPE_FAV,
                CommonIntegration.FAVOURITE_1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSimpleChannel .favType " + favType);
            if (mNavIntegration.checkChMask(mtkchannelinfo, CommonIntegration.favMask[favType], CommonIntegration.favMask[favType])) {
                switch (favType) {
                    case CommonIntegration.FAVOURITE_1:
                        mFirstLine.setText(String.valueOf(mtkchannelinfo.getFavorites1Index()));
                        break;
                    case CommonIntegration.FAVOURITE_2:
                        mFirstLine.setText(String.valueOf(mtkchannelinfo.getFavorites2Index()));
                        break;
                    case CommonIntegration.FAVOURITE_3:
                        mFirstLine.setText(String.valueOf(mtkchannelinfo.getFavorites3Index()));
                        break;
                    case CommonIntegration.FAVOURITE_4:
                        mFirstLine.setText(String.valueOf(mtkchannelinfo.getFavorites4Index()));
                        break;
                    default:
                        mFirstLine.setText(String.valueOf(mtkchannelinfo.getFavorites1Index()));
                        break;
                }
            } else {
                mFirstLine.setText(mNavBannerImplement.getCurrentChannelNum());
            }
        }

        private void showSimpleTime() {
            mFirstLine.setVisibility(View.INVISIBLE);
            mChannelName.setVisibility(View.INVISIBLE);
            mLockIcon.setVisibility(View.GONE);
            mTypeTimeLayout.setVisibility(View.INVISIBLE);
            mThirdMiddle.setVisibility(View.GONE);
            mReceiverType.setVisibility(View.INVISIBLE);
        }
    }

    class BasicBanner {

        private final Context mContext;

        private View mSelfLayout;
        private LinearLayout mCNBasicBannerLayout,
            mUSBasicBannerLayout,
            mSABasicBannerLayout,
            mEUBasicBannerLayout;

        private TextView mCurProgramName;

        private TextView mSpecialInfo;
        private View mSecondLineLayout;
        private TextView mCurProgramDuration;

        private TextView mAudioLanguage;

        private View mThirdLineLayout;
        private View mNextProgramLayout;
        private TextView mNextProgramName;
        private TextView mNextProgramDuration;

        private View mIconsLayout;
        private TextView mLockIcon;
        private TextView mFavoriteIcon;
        private TextView mSubtitleOrCCIcon;
        private TextView mTTXIcon;
        private TextView mGingaTVIcon;
        private TextView mEyeOrEarIcon;
        private TextView mCurrentProgramType;

        private TextView mVideoFormat;

        private TextView mRateTextView;

        private View mTransponderView;
        private TextView tpFrequency;
        private TextView tpSysbolRate;
        private TextView tpPolarization;

        String[] mtsAudioMode;

        public boolean isAnimation = false;

        private String mCurrentChannelLogoPath;
        private String mProgramTime;
        private String mProgramTitle;
        private String mProgramCategory;
        private String mNextProgramCategory;
        private String mAudioInfo;
        private String mNextProgramTitle;
        private String mNextProgramTime;
        private String mProgramRating;
        private String mNextProgramRating;
        private String mVideoInfo;
        private String mTransponder[] = new String[3];
        private int mDoblyType;

        public BasicBanner(Context context) {
            // TODO Auto-generated constructor stub
            mContext = context;
            mtsAudioMode = mContext.getResources().getStringArray(
                R.array.nav_mts_strings);
        }

        private void initView() {
            mSelfLayout = (View) findViewById(R.id.banner_basic_layout);
            mCNBasicBannerLayout = (LinearLayout) findViewById(R.id.banner_basic_layout_cn);
            mUSBasicBannerLayout = (LinearLayout) findViewById(R.id.banner_basic_layout_us);
            mSABasicBannerLayout = (LinearLayout) findViewById(R.id.banner_basic_layout_sa);
            mEUBasicBannerLayout = (LinearLayout) findViewById(R.id.banner_basic_layout_eu);
            if (MarketRegionInfo.REGION_US == MarketRegionInfo
                .getCurrentMarketRegion()) {
                mCNBasicBannerLayout.setVisibility(View.GONE);
                mSABasicBannerLayout.setVisibility(View.GONE);
                mEUBasicBannerLayout.setVisibility(View.GONE);
                mUSBasicBannerLayout.setVisibility(View.VISIBLE);
                mCurProgramName = (TextView) findViewById(R.id.banner_current_program_title_us);
                //mSpecialInfo = (TextView) findViewById(R.id.banner_special_info_us);
                mCurProgramDuration = (TextView) findViewById(R.id.banner_current_program_duration_us);
                mAudioLanguage = (TextView) findViewById(R.id.banner_audio_language_us);
                mEyeOrEarIcon = (TextView) findViewById(R.id.banner_channel_eys_ear_icon_us);
                mLockIcon = (TextView) findViewById(R.id.banner_channel_lock_icon_us);
                mFavoriteIcon = (TextView) findViewById(R.id.banner_channel_favorite_icon_us);
                mSubtitleOrCCIcon = (TextView) findViewById(R.id.banner_channel_cc_icon_us);
                mVideoFormat = (TextView) findViewById(R.id.banner_video_format_us);
                mSecondLineLayout = findViewById(R.id.banner_basic_second_line_us);
                mRateTextView = (TextView) findViewById(R.id.banner_program_rating_us);
                mThirdLineLayout = (View) findViewById(R.id.banner_basic_third_line_us);
            } else if (MarketRegionInfo.REGION_SA == MarketRegionInfo
                .getCurrentMarketRegion()) {
                mCNBasicBannerLayout.setVisibility(View.GONE);
                mUSBasicBannerLayout.setVisibility(View.GONE);
                mEUBasicBannerLayout.setVisibility(View.GONE);
                mSABasicBannerLayout.setVisibility(View.VISIBLE);
                mCurProgramName = (TextView) findViewById(R.id.banner_current_program_title_sa);
                //mSpecialInfo = (TextView) findViewById(R.id.banner_special_info_sa);
                mCurProgramDuration = (TextView) findViewById(R.id.banner_current_program_duration_sa);
                mAudioLanguage = (TextView) findViewById(R.id.banner_audio_language_sa);
                mEyeOrEarIcon = (TextView) findViewById(R.id.banner_channel_eys_ear_icon_sa);
                mLockIcon = (TextView) findViewById(R.id.banner_channel_lock_icon_sa);
                mFavoriteIcon = (TextView) findViewById(R.id.banner_channel_favorite_icon_sa);
                mSubtitleOrCCIcon = (TextView) findViewById(R.id.banner_channel_cc_icon_sa);
                mVideoFormat = (TextView) findViewById(R.id.banner_video_format_sa);
                mSecondLineLayout = findViewById(R.id.banner_basic_second_line_sa);
                mRateTextView = (TextView) findViewById(R.id.banner_program_rating_sa);
                mNextProgramName = (TextView) findViewById(R.id.banner_next_program_title_sa);
                mNextProgramDuration = (TextView) findViewById(R.id.banner_next_program_duration_sa);
                mGingaTVIcon = (TextView) findViewById(R.id.banner_ginga_tv_icon_sa);
                mCurrentProgramType = (TextView) findViewById(R.id.banner_current_program_type_sa);
                mThirdLineLayout = (View) findViewById(R.id.banner_basic_third_line_sa);
            } else if (MarketRegionInfo.REGION_EU == MarketRegionInfo
                .getCurrentMarketRegion()) {
                mCNBasicBannerLayout.setVisibility(View.GONE);
                mUSBasicBannerLayout.setVisibility(View.GONE);
                mSABasicBannerLayout.setVisibility(View.GONE);
                mEUBasicBannerLayout.setVisibility(View.VISIBLE);
                mCurProgramName = (TextView) findViewById(R.id.banner_current_program_title_eu);
                //mSpecialInfo = (TextView) findViewById(R.id.banner_special_info_eu);
                mEyeOrEarIcon = (TextView) findViewById(R.id.banner_channel_eys_ear_icon_eu);
                mCurProgramDuration = (TextView) findViewById(R.id.banner_current_program_duration_eu);
                mAudioLanguage = (TextView) findViewById(R.id.banner_audio_language_eu);
                mLockIcon = (TextView) findViewById(R.id.banner_channel_lock_icon_eu);
                mFavoriteIcon = (TextView) findViewById(R.id.banner_channel_favorite_icon_eu);
                mSubtitleOrCCIcon = (TextView) findViewById(R.id.banner_channel_subtitle_icon_eu);
                mVideoFormat = (TextView) findViewById(R.id.banner_video_format_eu);
                mSecondLineLayout = findViewById(R.id.banner_basic_second_line_eu);
                mRateTextView = (TextView) findViewById(R.id.banner_program_rating_eu);
                mNextProgramName = (TextView) findViewById(R.id.banner_next_program_title_eu);
                mNextProgramDuration = (TextView) findViewById(R.id.banner_next_program_duration_eu);
                mTTXIcon = (TextView) findViewById(R.id.banner_channel_ttx_icon_eu);
                mCurrentProgramType = (TextView) findViewById(R.id.banner_current_program_type_eu);
                mThirdLineLayout = (View) findViewById(R.id.banner_basic_third_line_eu);
                mNextProgramLayout = (View) findViewById(R.id.banner_next_program_layout_eu);
                mTransponderView = (View) findViewById(R.id.banner_dvbs_transponder_eu);
                tpFrequency = (TextView) findViewById(R.id.banner_dvbs_frequency_eu);
                tpSysbolRate = (TextView) findViewById(R.id.banner_dvbs_sysbol_eu);
                tpPolarization = (TextView) findViewById(R.id.banner_dvbs_pol_eu);
            } else if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                .getCurrentMarketRegion()) {
                mUSBasicBannerLayout.setVisibility(View.GONE);
                mSABasicBannerLayout.setVisibility(View.GONE);
                mEUBasicBannerLayout.setVisibility(View.GONE);
                mCNBasicBannerLayout.setVisibility(View.VISIBLE);
                mCurProgramName = (TextView) findViewById(R.id.banner_current_program_title);
                mSecondLineLayout = (View) findViewById(R.id.banner_basic_second_line);
				/*mSpecialInfo = (TextView) findViewById(R.id.banner_special_info);
				mSpecialInfo.setPadding(
						(int) (0.334 * ScreenConstant.SCREEN_WIDTH), 0, 0, 0);*/
                mCurProgramDuration = (TextView) findViewById(R.id.banner_current_program_duration);
                mAudioLanguage = (TextView) findViewById(R.id.banner_audio_language);
                mThirdLineLayout = (View) findViewById(R.id.banner_basic_third_line);
                mNextProgramLayout = (View) findViewById(R.id.banner_next_program_layout);
                mNextProgramName = (TextView) findViewById(R.id.banner_next_program_name);
                mNextProgramDuration = (TextView) findViewById(R.id.banner_next_program_duration);
                mIconsLayout = (View) findViewById(R.id.banner_basic_icons_layout);
                mLockIcon = (TextView) findViewById(R.id.banner_channel_lock_icon);
                mFavoriteIcon = (TextView) findViewById(R.id.banner_channel_favorite_icon);
                mSubtitleOrCCIcon = (TextView) findViewById(R.id.banner_channel_subtitle_icon);
                mTTXIcon = (TextView) findViewById(R.id.banner_channel_ttx_icon);
                mVideoFormat = (TextView) findViewById(R.id.banner_video_format);
            } else {
                mUSBasicBannerLayout.setVisibility(View.GONE);
                mSABasicBannerLayout.setVisibility(View.GONE);
                mEUBasicBannerLayout.setVisibility(View.GONE);
                mCNBasicBannerLayout.setVisibility(View.VISIBLE);
                mCurProgramName = (TextView) findViewById(R.id.banner_current_program_title);
                mSecondLineLayout = (View) findViewById(R.id.banner_basic_second_line);
                mCurProgramDuration = (TextView) findViewById(R.id.banner_current_program_duration);
                mAudioLanguage = (TextView) findViewById(R.id.banner_audio_language);
                mNextProgramLayout = (View) findViewById(R.id.banner_next_program_layout);
                mNextProgramName = (TextView) findViewById(R.id.banner_next_program_name);
                mNextProgramDuration = (TextView) findViewById(R.id.banner_next_program_duration);
                mIconsLayout = (View) findViewById(R.id.banner_basic_icons_layout);
                mLockIcon = (TextView) findViewById(R.id.banner_channel_lock_icon);
                mFavoriteIcon = (TextView) findViewById(R.id.banner_channel_favorite_icon);
                mSubtitleOrCCIcon = (TextView) findViewById(R.id.banner_channel_subtitle_icon);
                mVideoFormat = (TextView) findViewById(R.id.banner_video_format);
            }
            mSpecialInfo = (TextView) findViewById(R.id.banner_special_info);
            mSelfLayout.setVisibility(View.INVISIBLE);
			/*mSpecialInfo.setPadding(
					(int) (0.334 * ScreenConstant.SCREEN_WIDTH), 0, 0, 0);*/
			/*vfDelayTextTask = new DelayTextTask(mVideoFormat,
					videoFormatTimeChange);*/
        }

        public boolean isVisible() {
            return mSelfLayout.getVisibility() == View.VISIBLE;
        }

        private int getVisibility() {
            return mSelfLayout.getVisibility();
        }

        private boolean isAllowSpecialInfoShow() {
            if (StateDvrPlayback.getInstance() != null
                && StateDvrPlayback.getInstance().isRunning()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialBar dvr is running!");
                return false;
            }
            return true;
        }

        private void showSpecialInfo(int state) {
            if (!isAllowSpecialInfoShow()) {
                return;
            }
            String text = "";
            if (mNavIntegration.isCurrentSourceBlockEx()
                && !mNavIntegration.isCurrentSourceTv()
                && mSelfLayout.getVisibility() == VISIBLE) {
                mSelfLayout.setVisibility(View.INVISIBLE);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialInfo Basic hide");
                // if (mBasicBannerViewWindowId!=-1) {
                // mBypassWindowManager.setBypassWindow(false,
                // mBasicBannerViewWindowId, mBasicBannerViewRect);
                // }
                topBanner
                    .setBackgroundResource(R.drawable.translucent_background);
            } else {
                if (state == SPECIAL_NO_SIGNAL
                    && TIFChannelManager.getInstance(mContext)
                    .isCiVirtualChannel()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "the current channel is CI virtual channel!!!");
                    mSimpleBanner.setSimpleBannerBg(true);
                    return;
                }
                if (mSelfLayout.getVisibility() != VISIBLE) {
                    mSelfLayout.setVisibility(View.VISIBLE);
                    mSimpleBanner.setSimpleBannerBg(false);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in showSpecialInfo else");
                    topBanner
                        .setBackgroundResource(R.drawable.nav_infobar_basic_bg);
                }
                setSpecialVisibility(true);
                switch (state) {
                    case SPECIAL_NO_CHANNEL:
                    case SPECIAL_EMPTY_SPECIAFIED_CH_LIST:
                        if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                            .getCurrentMarketRegion()) {
                            mIconsLayout.setVisibility(View.INVISIBLE);
                        } else {
                            setIconsVisibility();
                        }
                        mBasicBanner.mSubtitleOrCCIcon
                            .setVisibility(View.INVISIBLE);
                        text = mContext
                            .getString(R.string.nav_please_scan_channels);
                        mSimpleBanner.mFirstLine.setVisibility(View.INVISIBLE);
                        break;
                    case SPECIAL_NO_SIGNAL:
                        if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                            .getCurrentMarketRegion()) {
                            mIconsLayout.setVisibility(View.VISIBLE);
                        }
                        setIconsVisibility();

                        //					if ((MarketRegionInfo.REGION_US == MarketRegionInfo
                        //							.getCurrentMarketRegion() || MarketRegionInfo.REGION_SA == MarketRegionInfo
                        //							.getCurrentMarketRegion())
                        //							&& !TIFChannelManager.getInstance(mContext)
                        //									.hasActiveChannel()) {
                        //						mSimpleBanner.mFirstLine.setVisibility(View.INVISIBLE);
                        //					}
                        text = mContext.getString(R.string.nav_no_signal);
                        break;
                    case SPECIAL_NO_SUPPORT:
                        if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                            .getCurrentMarketRegion()) {
                            mIconsLayout.setVisibility(View.VISIBLE);
                        }
                        setIconsVisibility();
                        text = mContext.getString(R.string.nav_no_support);
                        break;
                    case SPECIAL_INPUT_LOCK:
                        if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                            .getCurrentMarketRegion()) {
                            mIconsLayout.setVisibility(View.INVISIBLE);
                        } else {
                            setFavoriteVisibility();
                            setLockVisibility();
                        }
                        mBasicBanner.mSubtitleOrCCIcon
                            .setVisibility(View.INVISIBLE);
                        text = mContext.getString(R.string.nav_input_has_locked);
                        break;
                    case SPECIAL_CHANNEL_LOCK:
                        if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                            .getCurrentMarketRegion()) {
                            mIconsLayout.setVisibility(View.VISIBLE);
                        }
                        hideFavoriteChannelList();
                        setFavoriteVisibility();
                        setLockVisibility();
                        text = mContext.getString(R.string.nav_channel_has_locked);
                        break;
                    case SPECIAL_RETRIVING:
                        if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                            .getCurrentMarketRegion()) {
                            mIconsLayout.setVisibility(View.INVISIBLE);
                        } else {
                            setIconsVisibility();
                        }
                        text = mContext.getString(R.string.nav_channel_retrieving);
                        break;
                    case SPECIAL_PROGRAM_LOCK:
                        setFavoriteVisibility();
                        setLockVisibility();
                        hideFavoriteChannelList();
                        text = mContext.getString(R.string.nav_program_has_locked);
                        break;
                    //				case SPECIAL_HIDDEN_CHANNEL:
                    //					setIconsVisibility();
                    //					// fix CR:DTV00636292
                    //					// fix CR:DTV00700150
                    //					mBasicBanner.mSubtitleOrCCIcon
                    //							.setVisibility(View.INVISIBLE);
                    //text = mContext.getString(R.string.nav_hidden_channel);
                    //					break;
                    default:
                        break;
                }
                if (MarketRegionInfo.REGION_CN != MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialInfo222 mRateTextView INVISIBLE start");
                    if (mRateTextView.getVisibility() != View.INVISIBLE) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialInfo222 mRateTextView INVISIBLE");
                        mRateTextView.setVisibility(View.INVISIBLE);
                    }
                }
                if (MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion() ||
                    MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showSpecialInfo ProgramCategory:"
                        + mNavBannerImplement.getProgramCategory());

                    if ((specialType >= 1 && specialType <= 5)
                        || (specialType >= 16 && specialType <= 20)) {
                        mCurrentProgramType.setText("");
                        mNextProgramName.setText("");
                        mNextProgramDuration.setText("");
                    } else {
                        mNextProgramName.setText(mNavBannerImplement
                            .getNextProgramTitle());
                        mNextProgramDuration.setText(mNavBannerImplement
                            .getNextProgramTime());
                        mCurrentProgramType.setText(mNavBannerImplement
                            .getProgramCategory());
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showSpecialInfo state:" + state + ",visibility:"
                    + mSpecialInfo.getVisibility() + ",text:" + text
                    + " specialType : " + specialType);
                mSpecialInfo.setText(text);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mSpecialInfo.setText(): " + text);
            }
        }

        private void show() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BaiscBar show()." + mSelfLayout.getVisibility());
            if (isHostQuietTuneStatus) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "silently change.");
                return;
            }
            if (mSelfLayout.getVisibility() != View.VISIBLE) {
                resetBasicUI();
            }
            mSelfLayout.setVisibility(View.VISIBLE);
            mSimpleBanner.setSimpleBannerBg(false);
            topBanner.setBackgroundResource(R.drawable.nav_infobar_basic_bg);
            getBannerInfoWithThead();
        }

        private void resetBasicData() {
            mProgramTime = null;
            mProgramTitle = null;
            mCurrentChannelLogoPath = null;
            mProgramCategory = null;
            mNextProgramTitle = null;
            mNextProgramTime = null;
            mProgramRating = null;
            mVideoInfo = null;
            mAudioInfo = null;
        }

        private void resetBasicUI() {
            mAudioLanguage.setText("");
            mAudioLanguage.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null);
            mCurProgramName.setText("");
            mCurProgramDuration.setText("");
            if (mTTXIcon != null) {
                mTTXIcon.setVisibility(INVISIBLE);
            }
            if (mEyeOrEarIcon != null) {
                mEyeOrEarIcon.setVisibility(INVISIBLE);
            }
            if (mSubtitleOrCCIcon != null) {
                mSubtitleOrCCIcon.setVisibility(INVISIBLE);
            }
            mFavoriteIcon.setVisibility(INVISIBLE);
            mLockIcon.setVisibility(INVISIBLE);
            if (mRateTextView != null) {
                mRateTextView.setText("");
            }
            mVideoFormat.setText("");
            mVideoFormat.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null);
            if (MarketRegionInfo.REGION_US != MarketRegionInfo.getCurrentMarketRegion()) {
                mNextProgramName.setText("");
                mNextProgramDuration.setText("");
            }
            if (MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion() ||
                MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {
                mCurrentProgramType.setText("");
            }
            if (mTransponderView != null) {
                mTransponderView.setVisibility(INVISIBLE);
                tpFrequency.setText(null);
                tpSysbolRate.setText(null);
                tpPolarization.setText(null);
            }
        }

        private void getBannerInfoWithThead() {
            Disposable subscribe = Completable.create(emitter -> {
                resetBasicData();
                mProgramTime = mNavBannerImplement.getProgramTime();
                mProgramTitle = mNavBannerImplement.getProgramTitle();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgramTime=" + mProgramTime
                    + ",mProgramTitle=" + mProgramTitle);
                if (CommonIntegration.isSARegion()) {
                    MtkTvChannelInfoBase currentChannelInfo = mNavIntegration
                        .getChannelById(mNavIntegration
                            .getCurrentChannelId());
                    if ((currentChannelInfo instanceof MtkTvISDBChannelInfo)) {
                        mCurrentChannelLogoPath = mNavIntegration
                            .getISDBChannelLogo((MtkTvISDBChannelInfo) currentChannelInfo);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in basic banner show channel logo, mCurrentChannelLogoPath = "
                                + mCurrentChannelLogoPath);
                    }
                }
                if (CommonIntegration.isEURegion() || CommonIntegration.isSARegion()) {
                    mProgramCategory = mNavBannerImplement.getProgramCategory();
                    mNextProgramCategory = mNavBannerImplement.getNextProgramCategory();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgramCategory=" + mProgramCategory +
                        ", mNextProgramCategory=" + mNextProgramCategory);
                }
                getAudioLanguage();
                if (!CommonIntegration.isUSRegion()) {
                    mNextProgramTitle = mNavBannerImplement
                        .getNextProgramTitle();
                    mNextProgramTime = mNavBannerImplement
                        .getNextProgramTime();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextProgramTitle=" + mNextProgramTitle
                        + ",mNextProgramTime=" + mNextProgramTime);
                }
                if (!CommonIntegration.isCNRegion() && !isSpecialState) {
                    mProgramRating = mNavBannerImplement.getProgramRating();
                    mNextProgramRating = mNavBannerImplement.getNextProgramRating();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgramRating=" + mProgramRating + ", mNextProgramRating=" + mNextProgramRating);
                }
                if (ScanContent.isSatScan()) {
                    mNavBannerImplement.getDVBSTransponder(mTransponder);
                }
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    changeSourceBannerTiming(mVideoFormat);
                    mCurProgramName.setText(mProgramTitle);
                    showChannelLogo(mCurrentChannelLogoPath);
                    mCurProgramDuration.setText(mProgramTime);
                    changeAudioUI();
                    if (!CommonIntegration.isUSRegion()) {
                        mNextProgramName.setText(mNextProgramTitle);
                        mNextProgramDuration.setText(mNextProgramTime);
                    }
                    setNextEvent();
                    setIconsVisibility();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BaiscBar show()~~");
                    setSpecialVisibility(isSpecialState && specialType > 0);
                    if (ScanContent.isSatScan()) {
                        tpFrequency.setText(mTransponder[0]);
                        tpSysbolRate.setText(mTransponder[1]);
                        tpPolarization.setText(mTransponder[2]);
                    }
                }, Throwable::printStackTrace);
            mDisposables.add(subscribe);
        }

        private void setNextEvent() {
            refreshCurrentProgram();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setNextEvent region=" + MarketRegionInfo.getCurrentMarketRegion());
            if (CommonIntegration.isEURegion() || CommonIntegration.isSARegion()) {
                mCurrentProgramType.setText(!mNextEvent ? mProgramCategory : mNextProgramCategory);
            }

            if (!CommonIntegration.isCNRegion()) {
                if(!isSpecialState) {
                    mRateTextView.setVisibility(View.VISIBLE);
                    mRateTextView.setText(!mNextEvent ? mProgramRating : mNextProgramRating);
                } else {
                    mRateTextView.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void getAudioLanguage() {
            mIs3rdTVSource = mNavIntegration.is3rdTVSource();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mIs3rdTVSource=" + mIs3rdTVSource);
            mAudioInfo = mIs3rdTVSource ? mNavBannerImplement
                .getBannerAudioInfoFor3rd() : mNavBannerImplement
                .getBannerAudioInfo(audioScramebled);
            mDoblyType = mNavBannerImplement.getDoblyType();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mAudioInfo=" + mAudioInfo);
        }

        private void updateAudioLanguage() {
            TVAsyncExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    getAudioLanguage();
                    bannerLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            changeAudioUI();
                        }
                    });
                }
            });
        }

        private void changeAudioUI() {
            if (mSecondLineLayout.getVisibility() == View.VISIBLE) {
                if (mIs3rdTVSource) {
                    mAudioLanguage.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null);
                    mAudioLanguage.setText(mAudioInfo);
                } else {
                    mAudioLanguage.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null);
                    if (audioScramebled) {
                        if (!TextUtils.isEmpty(mAudioInfo)) {
                            mAudioLanguage.setText(mAudioInfo);
                        } else {
                            mAudioLanguage
                                .setText(R.string.nav_channel_audio_scrambled);
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "dismiss dolby icon,audioScramebled is true");
                    } else if (noAudio) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss dolby icon,noAudio is true");
                        StateNormal.setisAudio(true);
                        mAudioLanguage.setText(R.string.nav_channel_no_audio);
                    } else {
                        StateNormal.setisAudio(false);
                        String audioLanguage = mAudioLanguage.getText()
                            .toString();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audioLanguage=" + audioLanguage);
                        mAudioLanguage.setText(mAudioInfo);
                        Drawable drawable = null;
                        if (mDoblyType == BannerImplement.DOBLY_TYPE_ATOMS) {
                            drawable = getResources().getDrawable(
                                R.drawable.icon_dobly_atoms);
                            drawable.setBounds(
                                0,
                                0,
                                getResources().getDimensionPixelOffset(
                                    R.dimen.nav_banner_dobly_width),
                                getResources().getDimensionPixelOffset(
                                    R.dimen.nav_banner_dobly_height));
                        } else if (mDoblyType == BannerImplement.DOBLY_TYPE_AUDIO) {
                            drawable = getResources().getDrawable(
                                R.drawable.icon_dobly_audio);
                            drawable.setBounds(
                                0,
                                0,
                                getResources().getDimensionPixelOffset(
                                    R.dimen.nav_banner_dobly_width),
                                getResources().getDimensionPixelOffset(
                                    R.dimen.nav_banner_dobly_height));
                        }
                        mAudioLanguage.setCompoundDrawables(drawable, null,
                            null, null);
                        mAudioLanguage.setCompoundDrawablePadding(5);
                    }
                }
            }
        }

        private void hide() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BasicBar hide()");
            if (mSelfLayout.getVisibility() == VISIBLE) {

                // if (mBasicBannerViewWindowId!=-1) {
                // mBypassWindowManager.setBypassWindow(false,
                // mBasicBannerViewWindowId, mBasicBannerViewRect);
                // }
                // com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG," hide mBasicBannerViewWindowId:"+mBasicBannerViewWindowId);

                // if (isOnkeyInfo
                // && AnimationManager.getInstance().getIsAnimation()) {
                // com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BasicBar hide() 1");
                // AnimationManager.getInstance().startAnimation(
                // AnimationManager.TYPE_BASIC_BANNER_EXIT,
                // mSelfLayout, new AnimatorListenerAdapter() {
                // @Override
                // public void onAnimationStart(Animator animation) {
                // isAnimation = true;
                // mSelfLayout
                // .setBackgroundResource(R.drawable.nav_infobar_basic_bg);
                // mSimpleBanner.setSimpleBannerBg(true);
                // super.onAnimationStart(animation);
                // }
                //
                // @Override
                // public void onAnimationEnd(Animator animation) {
                // isAnimation = false;
                // mSelfLayout.setAnimation(null);
                // mSelfLayout.clearAnimation();
                // mSelfLayout.setVisibility(View.INVISIBLE);
                // com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BasicBar hide() 3");
                // super.onAnimationEnd(animation);
                // }
                // });
                // } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BasicBar hide() 2");
                mSimpleBanner.setSimpleBannerBg(true);
                mSelfLayout.setVisibility(View.INVISIBLE);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in BasicBar hide() 2 end");
                // }
            }
            topBanner.setBackgroundResource(R.drawable.translucent_background);
        }

        private void setSpecialVisibility(boolean isSpecial) {
            if (isSpecial) {
                mCurProgramName.setVisibility(View.INVISIBLE);
                mSecondLineLayout.setVisibility(View.GONE);
                if (mNextProgramLayout != null) {
                    mNextProgramLayout.setVisibility(View.INVISIBLE);
                }
                if (MarketRegionInfo.REGION_CN != MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSpecialVisibility mRateTextView INVISIBLE.");
                    mRateTextView.setVisibility(View.INVISIBLE);
                }
                mSpecialInfo.setVisibility(View.VISIBLE);
                mVideoFormat.setVisibility(View.INVISIBLE);
                if (mCurrentProgramType != null) {
                    mCurrentProgramType.setVisibility(View.INVISIBLE);
                }
                if (mSubtitleOrCCIcon != null) {
                    mSubtitleOrCCIcon.setVisibility(INVISIBLE);
                }
                if (mTTXIcon != null) {
                    mTTXIcon.setVisibility(INVISIBLE);
                }
                if (mGingaTVIcon != null) {
                    mGingaTVIcon.setVisibility(INVISIBLE);
                }
                if (mEyeOrEarIcon != null) {
                    mEyeOrEarIcon.setVisibility(INVISIBLE);
                }
                if (mTransponderView != null) {
                    mTransponderView.setVisibility(INVISIBLE);
                }
            } else {
                mCurProgramName.setVisibility(View.VISIBLE);
                mSecondLineLayout.setVisibility(View.VISIBLE);
                if (MarketRegionInfo.REGION_CN == MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    mIconsLayout.setVisibility(View.VISIBLE);
                }
                if (mNextProgramLayout != null) {
                    mNextProgramLayout.setVisibility(View.VISIBLE);
                }
                mSpecialInfo.setVisibility(View.GONE);
                mVideoFormat.setVisibility(View.VISIBLE);
                if (mCurrentProgramType != null) {
                    mCurrentProgramType.setVisibility(View.VISIBLE);
                }
                if (mTransponderView != null) {
                    mTransponderView.setVisibility(VISIBLE);
                }
                setIconsVisibility();
            }
        }

        private boolean isShowTVLockIcon() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isShowTVLockIcon---->specialType=" + specialType);
            return specialType == SPECIAL_CHANNEL_LOCK || specialType == SPECIAL_INPUT_LOCK || specialType == SPECIAL_PROGRAM_LOCK;
        }

        private void setLockVisibility() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockVisibility");
            if (isShowTVLockIcon()
                && !mNavIntegration.is3rdTVSource()) {
                mLockIcon.setVisibility(View.VISIBLE);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockVisibility VISIBLE");
            } else {
                mLockIcon.setVisibility(View.INVISIBLE);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockVisibility INVISIBLE");
            }
        }

        public void setFavoriteVisibility() {
            boolean isNeedShowFavicon = false;
            if (mNavIntegration.isCurrentSourceTv()
                && !mNavIntegration.isCurrentSourceBlockEx()) {
                isNeedShowFavicon = true;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setFavoriteVisibility isNeedShowFavicon=="
                + isNeedShowFavicon);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setFavoriteVisibility isFavChannel=="
                + FavChannelManager.getInstance(mContext).isFavChannel());
            if (!mNavIntegration.is3rdTVSource() && isNeedShowFavicon
                && FavChannelManager.getInstance(mContext).isFavChannel()) {
                mFavoriteIcon.setVisibility(View.VISIBLE);
                mFavoriteIcon.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(
                        R.drawable.nav_infobar_favorite), null, null,
                    null);
            } else {
                mFavoriteIcon.setVisibility(View.INVISIBLE);
                mFavoriteIcon.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, null, null);
            }
        }

        private void setCaptionVisibility() {
            String ccString = mNavBannerImplement.getBannerCaptionInfo();
            boolean showCCIcon = mNavBannerImplement.isShowCaptionIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility showCC =" + showCCIcon
                + " getCC = " + ccString);
            mSimpleBanner.mCCView.setVisibility(View.GONE);
            mSubtitleOrCCIcon.setVisibility(View.INVISIBLE);
            if (showCCIcon
                && ((TextUtils.isEmpty(ccString) && !mNavIntegration
                .is3rdTVSource()) || !TextUtils.isEmpty(ccString))) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility~ CCString 1");
                if (MarketRegionInfo.REGION_SA == MarketRegionInfo
                    .getCurrentMarketRegion()
                    || MarketRegionInfo.REGION_US == MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    mSubtitleOrCCIcon.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.nav_cc_icon),
                        null, null, null);
                } else {
                    mSubtitleOrCCIcon.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(
                            R.drawable.nav_banner_icon_sttl), null,
                        null, null);
                }
                mSubtitleOrCCIcon.setPadding(0, 0, 0, 0);
                mSubtitleOrCCIcon.setVisibility(View.VISIBLE);
                mSubtitleOrCCIcon.setText(ccString);
            } else if (!showCCIcon
                && !TextUtils.isEmpty(ccString)
                && (mNavIntegration.isCurrentSourceTv() || mNavIntegration
                .is3rdTVSource())) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility~ CCString 2");
                int textPadingLeft = 0;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility showCC 2");
                mSubtitleOrCCIcon.setCompoundDrawables(null, null, null, null);
                if (MarketRegionInfo.REGION_SA == MarketRegionInfo
                    .getCurrentMarketRegion()
                    || MarketRegionInfo.REGION_US == MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    textPadingLeft = mSubtitleOrCCIcon
                        .getCompoundDrawablePadding()
                        + getResources()
                        .getDrawable(R.drawable.nav_cc_icon)
                        .getIntrinsicWidth();
                } else {
                    textPadingLeft = mSubtitleOrCCIcon
                        .getCompoundDrawablePadding()
                        + getResources().getDrawable(
                        R.drawable.nav_banner_icon_sttl)
                        .getIntrinsicWidth();
                }
                mSubtitleOrCCIcon.setPadding(textPadingLeft, 0, 0, 0);
                mSubtitleOrCCIcon.setVisibility(View.VISIBLE);
                mSubtitleOrCCIcon.setText(ccString);
            } else if (mNavIntegration.isCurrentSourceTv()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility showCC 3");
                mSubtitleOrCCIcon.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, null, null);
                mSubtitleOrCCIcon.setText(ccString);
            } else if (!mNavIntegration.isCurrentSourceTv()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility showCC 4");
                mSimpleBanner.showCCView();
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cc setCaptionVisibility~ CCString =" + ccString);
            if (null != ttsUtil && cckeysend && !TextUtils.isEmpty(ccString)) {
                cckeysend = false;
                sendTTSSpeakMsg(ccString);
            }
        }

        private void setTtxIconVisibility() {
            if (MarketRegionInfo.REGION_EU == MarketRegionInfo
                .getCurrentMarketRegion()) {
                if (mNavBannerImplement.isShowTtxIcon()) {
                    mTTXIcon.setVisibility(View.VISIBLE);
                } else {
                    mTTXIcon.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void setGingaTVIconVisibility() {
            if (MarketRegionInfo.REGION_SA == MarketRegionInfo
                .getCurrentMarketRegion()) {
                if (mNavBannerImplement.isShowGingaIcon()) {
                    mGingaTVIcon.setVisibility(View.VISIBLE);
                } else {
                    mGingaTVIcon.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void setEyeOrEarHinderIconVisibility() {
            if ((MarketRegionInfo.REGION_SA == MarketRegionInfo
                .getCurrentMarketRegion())
                || (MarketRegionInfo.REGION_EU == MarketRegionInfo
                .getCurrentMarketRegion())
                || (MarketRegionInfo.REGION_US == MarketRegionInfo
                .getCurrentMarketRegion())) {
                if (mNavBannerImplement.isShowADEIcon()) {
                    mEyeOrEarIcon.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(
                            R.drawable.nav_banner_eye_icon), null,
                        null, null);
                    mEyeOrEarIcon.setVisibility(View.VISIBLE);
                } else if (mNavBannerImplement.isShowEARIcon()) {
                    mEyeOrEarIcon.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(
                            R.drawable.nav_banner_ear_icon), null,
                        null, null);
                    mEyeOrEarIcon.setVisibility(View.VISIBLE);
                } else {
                    mEyeOrEarIcon.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void showChannelLogo(String logoPath) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showChannelLogo");
            if (null != logoPath) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "null != logoPath , come in showChannelLogo");
                Drawable logoDrawable = Drawable.createFromPath(logoPath);
                mCurProgramName.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, logoDrawable, null);
            } else {
                mCurProgramName.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, null, null);
            }
        }

        private void setIconsVisibility() {
            setLockVisibility();
            setFavoriteVisibility();
            setCaptionVisibility();
            setTtxIconVisibility();
            setGingaTVIconVisibility();
            setEyeOrEarHinderIconVisibility();
        }

        /*
         * private void changeNextCcOrSubtitleValue(){ int result = -1; result =
         * mNavBannerImplement.changeNextCloseCaption();; if(0 == result){
         * mSubtitleOrCCIcon.setText(mNavBannerImplement
         * .getCurrentCCOrSubtitleValue()); } }
         */
    }

    class DetailBanner {

        private final Context mContext;

        private View mSelfLayout;

        private TextView mDetailInfo;

        private View mDetailIconLayout;
        private ImageView mDetailUpArrow;
        private ImageView mDetailDownArrow;
        private TextView mPageNum;

        private final DetailTextReader detailTextReader;

        public boolean isAnimation;

        private final TextReaderPageChangeListener detailPageChangeListener = new TextReaderPageChangeListener() {

            @Override
            public void onPageChanged(int page) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "page =" + page);
                // TODO Auto-generated method stub
                if (detailTextReader.getTotalPage() <= 1) {
                    mDetailIconLayout.setVisibility(View.INVISIBLE);
                } else {
                    mDetailIconLayout.setVisibility(View.VISIBLE);
                    if (page <= 1) {
                        mDetailUpArrow.setVisibility(View.INVISIBLE);
                        mDetailDownArrow.setVisibility(View.VISIBLE);
                    } else if (page >= detailTextReader.getTotalPage()) {
                        mDetailUpArrow.setVisibility(View.VISIBLE);
                        mDetailDownArrow.setVisibility(View.INVISIBLE);
                    } else {
                        mDetailUpArrow.setVisibility(View.VISIBLE);
                        mDetailDownArrow.setVisibility(View.VISIBLE);
                    }
                    String tpage = "" + page + "/"
                        + detailTextReader.getTotalPage();
                    mPageNum.setText(tpage);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "page =" + tpage);
                }
            }
        };

        public DetailBanner(Context context) {
            // TODO Auto-generated constructor stub
            mContext = context;
            detailTextReader = DetailTextReader.getInstance();
            detailTextReader
                .registerPageChangeListener(detailPageChangeListener);
            initView();
        }

        private void initView() {
            mSelfLayout = (View) findViewById(R.id.banner_detail_layout);
            float scale = mContext.getResources().getDisplayMetrics().density;
            LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                (int) (ScreenConstant.SCREEN_WIDTH),
                (int) (BANNER_BASIC_HEIGHT * scale + 0.5f));

            mSelfLayout.setLayoutParams(mParams);
            mSelfLayout.setVisibility(View.INVISIBLE);
            mDetailInfo = (TextView) findViewById(R.id.banner_detail_info);
            mDetailIconLayout = (View) findViewById(R.id.banner_detail_info_right_layout);
            mDetailUpArrow = (ImageView) findViewById(R.id.banner_detail_info_uparrow);
            mDetailDownArrow = (ImageView) findViewById(R.id.banner_detail_info_downarrow);
            mPageNum = (TextView) findViewById(R.id.banner_detail_info_pagenum);
            detailTextReader.setTextView(mDetailInfo);
        }

        private void show() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in detail banner show start");
            if (StateDvrPlayback.getInstance() != null
                && StateDvrPlayback.getInstance().isRunning()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvr is running!");
                return;
            }

            if (mSelfLayout.getVisibility() != VISIBLE) {
                if (isAnimation) {
                    return;
                }

                mSelfLayout.setVisibility(View.VISIBLE);

                String mEventDetail = !mNextEvent ? mNavBannerImplement.getProgramDetails() : mNavBannerImplement.getNextProgDetail();
                if (null == mEventDetail || mEventDetail.length() == 0) {
                    mDetailInfo.setText("");
                } else {
                    mDetailInfo.setText(mEventDetail);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in detail banner show");
                detailTextReader.resetCurPagenum();
                detailTextReader.setTextView(mDetailInfo);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in detailTextReader.setTextView(mDetailInfo)");

                mSelfLayout.setVisibility(View.VISIBLE);
            }
        }

        private void hide() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in DetailBanner hide");
            if (mSelfLayout.getVisibility() == VISIBLE) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in DetailBanner hide~~");
                mSelfLayout.setVisibility(View.INVISIBLE);
            }
        }

        private boolean getDetailBannerVisible() {
            return View.VISIBLE == mSelfLayout.getVisibility() ? true : false;
        }

        private void pageDown() {
            if (mSelfLayout.getVisibility() == View.VISIBLE) {
                detailTextReader.pageDown();
            }
        }

        private void pageUp() {
            if (mSelfLayout.getVisibility() == View.VISIBLE) {
                detailTextReader.pageUp();
            }
        }
    }

    public void reset() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Banner reset start.");
        audioScramebled = false;
        videoScramebled = false;
        noAudio = false;
        noVideo = false;
        isSpecialState = false;
        specialType = -1;
        isHostQuietTuneStatus = false;
        ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_HOST_TUNE_STATUS, 0);
        mNextEvent = false;
        mHDRIndex = 0;

        refreshCurrentProgram();
    }

    private void refreshCurrentProgram() {
        int colorYellow = getResources().getColor(R.color.dark_yellow);
        int colorWhite = getResources().getColor(R.color.white);
        mBasicBanner.mCurProgramName.setTextColor(!mNextEvent ? colorYellow : colorWhite);
        mBasicBanner.mCurProgramDuration.setTextColor(!mNextEvent ? colorYellow : colorWhite);
        if (!CommonIntegration.isUSRegion()) {
            mBasicBanner.mNextProgramName.setTextColor(mNextEvent ? colorYellow : colorWhite);
            mBasicBanner.mNextProgramDuration.setTextColor(mNextEvent ? colorYellow : colorWhite);
        }
    }

    private void changeSourceBannerTiming(final TextView tv) {
        mDisposables.add(
            Single.create((SingleOnSubscribe<String>) emitter -> {
                if (StateDvrPlayback.getInstance() != null
                    && StateDvrPlayback.getInstance().isRunning()) {
                    emitter.onError(new Throwable("Dvr is running and not get resolution."));
                    return;
                }
                String value;
                if (mNavIntegration.isCurrentSourceTv()) {
                    boolean is3rdTVSource = mNavIntegration.is3rdTVSource();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in videoFormatSourceTimeChange is3rdTVSource="
                        + is3rdTVSource);
                    if (!is3rdTVSource && videoScramebled) {
                        value = mContext
                            .getString(R.string.nav_channel_video_scrambled);
                    } else if (!is3rdTVSource && (noVideo || !mNavIntegration.isCurrentSourceHasSignal())) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in videoFormatSourceTimeChange == isCurrentSourceHasSignal");
                        value = mContext.getString(R.string.nav_resolution_null);
                    } else {
                        value = CommonIntegration.getSafeString(
                            mNavBannerImplement.getHDRVideoInfoByIndex(mHDRIndex),
                            " ",
                            mNavBannerImplement.getInputResolution());
                    }
                } else {
                    value = CommonIntegration.getSafeString(
                        mNavBannerImplement.getHDRVideoInfoByIndex(mHDRIndex),
                        " ",
                        mNavBannerImplement.getInputResolution());
                }
                emitter.onSuccess(value);
            })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( it -> {
                        tv.setText(it);
                        tv.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, null, null);
                        if (tv == mBasicBanner.mVideoFormat && mNavBannerImplement.isDolbyVision()) {
                            Drawable drawable = getResources().getDrawable(
                                    R.drawable.icon_dobly_vision);
                                drawable.setBounds(
                                    0,
                                    0,
                                    getResources().getDimensionPixelOffset(
                                        R.dimen.nav_banner_dobly_width),
                                    getResources().getDimensionPixelOffset(
                                        R.dimen.nav_banner_dobly_height));
                            tv.setCompoundDrawables(drawable, null,
                                null, null);
                            tv.setCompoundDrawablePadding(5);
                        }
                    }
                    , Throwable::printStackTrace)
        );
    }

    public void show(boolean isForward, int state, boolean manualClose) {
        show(isForward, state, manualClose, false);
    }

    /**
     * show panel according to the previous state
     *
     * @param isForward whether to move to the next state after showing
     * @param state whether to show the specific state ,if -1 ,will obey the
     * StateManager
     * @param manualClose whether close when it's already showed
     * @param isHandleKey whether show info by press key.
     */
    public void show(boolean isForward, int state, boolean manualClose,
        boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
            TAG,
            "show() isHandleKey=" + isHandleKey + ",isForward:" + isForward
                + ",state:" + state + ",state:" + manualClose
                + ",mBasicBanner.isAnimation ="
                + mBasicBanner.isAnimation
                + ",mSimpleBanner.isAnimation="
                + mSimpleBanner.isAnimation
                + ",mDetailBanner.isAnimation ="
                + mDetailBanner.isAnimation
                + ",DestroyApp.getRunningActivity()="
                + DestroyApp.isCurActivityTkuiMainActivity()
                + ",isHostQuietTuneStatus: " + isHostQuietTuneStatus);

        if (mNavIntegration.is3rdTVSource()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is 3rd source and reset tv data.");
            reset();
        }

        if (disableShowBanner(isHandleKey)) {
            return;
        }

        if (mBasicBanner.isAnimation || mSimpleBanner.isAnimation
            || mDetailBanner.isAnimation
            || !DestroyApp.isCurActivityTkuiMainActivity()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                TAG,
                "come in show(boolean isForward, int state, boolean manualClose) (mBasicBanner.isAnimation || mSimpleBanner.isAnimation");
            return;
        }

        if (isHostQuietTuneStatus) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHostQuietTuneStatus:" + isHostQuietTuneStatus);
            return;
        }
        //		if (bSourceOnTune) {// tuning // source   Resolve for DTV01747662
        //			bSourceOnTune = false;
        //			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tuning && isSpecialState:" + specialType);
        //			return;
        //		}
        int curState;
        boolean flag = false;

        if (bannerLayout.getVisibility() == View.VISIBLE) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "flag:" + flag);
            flag = true;
        }

        if (state >= 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "state:" + state);
            showByState(state, isHandleKey);
            //			totalState = -1;
            return;
        }

        if (mNavIntegration.isCurrentSourceTv()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "specialType: " + specialType);
            if (isZiggoOperator() && mNavBannerImplement.isCurrentHiddenChannel()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Ziggo operator and hidden channel will can not show banner.");
                return;
            }
        }
        boolean condition1 = mNavIntegration.isCurrentSourceBlockEx();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSourceBlocked: " + condition1 + ", specialType: "
            + specialType);
        if (specialType == SPECIAL_INPUT_LOCK && !condition1) {

            isSpecialState = false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "tvapi should send a message to set isSpecailState = false in time!!");
        }
        boolean is3rdTVSource = mNavIntegration.is3rdTVSource();
        PwdDialog pwdDialog = (PwdDialog) ComponentsManager.getInstance()
            .getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
        boolean isBlockFor3rd = pwdDialog.isContentBlock(true);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBlockFor3rd=" + isBlockFor3rd);
        if (condition1 && is3rdTVSource && isBlockFor3rd) {
            isSpecialState = true;
            showSimpleBar(isHandleKey);
            return;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
            "show(boolean isForward, int state, boolean manualClose) isSpecialState ="
                + isSpecialState);
        if (!isSpecialState && !mNavIntegration.isCurrentSourceTv()
            && !is3rdTVSource) {
            if (flag && manualClose) {
                if (getVisibility() != View.GONE) {
                    setVisibility(View.GONE);
                }
            } else {
                showSimpleBar(isHandleKey);
                //				totalState = -1;
            }
            return;
        }

        if ((condition1 || isSpecialState) && !mNavIntegration.is3rdTVSource()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in show(,,) isSpecialState");
            if (flag && manualClose) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in show(,,) isSpecialState falg = true");
                if (getVisibility() != View.GONE) {
                    setVisibility(View.GONE);
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in show(,,) isSpecialState falg = false");
                // DTV00660452
                if (specialType == SPECIAL_NO_CHANNEL) {
                    if (mNavIntegration.hasActiveChannel()) {
                        return;
                    }
                }
                showSpecialBar(specialType);
                if (getVisibility() != View.VISIBLE) {
                    setVisibility(View.VISIBLE);
                }
            }
            return;
        }

        //		totalState = -1;
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in show isForward && flag is " + isForward + " "
            + flag);
        if (isForward && flag) {
            curState = mStateManage.getNextState();
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in show next state is " + curState);
            setBannerState(curState);
        }
        curState = mStateManage.getState();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "get will show state is " + curState);
        switch (curState) {
            case CHILD_TYPE_INFO_NONE:
                showNoneBar(flag, isHandleKey);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in show showNoneBar(flag) + flag == " + flag);
                break;
            case CHILD_TYPE_INFO_SIMPLE:
                showSimpleBar(isHandleKey);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in show showSimpleBar()");
                break;
            case CHILD_TYPE_INFO_BASIC:
                showBasicBar(isHandleKey);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in show showBasicBar()");
                break;
            case CHILD_TYPE_INFO_DETAIL:
                showDetailBar(isHandleKey);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in show showDetailBar()");
                break;
            default:
                break;
        }
    }

    private boolean isZiggoOperator() {
        boolean isZiggo = ScanContent.getCurrentOperator().equals(CableOperator.Ziggo);
        boolean isNLD = "NLD".equals(MtkTvConfig.getInstance().getCountry());
        boolean isCable = ScanContent.isCurrentCable();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isZiggoOperator isZiggo=" + isZiggo + ", isNLD=" + isNLD + ", isCable=" + isCable);
        return isZiggo && isNLD && isCable;
    }

    /**
     * show channel program and detail panel
     */
    public void showDetailBar(boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showDetailBar");
        if (disableShowBanner(isHandleKey)) {
            return;
        }
        bannerLayout.setVisibility(View.VISIBLE);
        if (null != mBasicBanner) {
            showBasicBar(isHandleKey);
        }
        // if (null != mBasicBanner && mBasicBanner.getVisibility() !=
        // View.VISIBLE) {
        // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "basic bar not show up yet!");
        // return;
        // }
        mDetailBanner.show();
        ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_BANNER);
        startTimeout(NAV_TIMEOUT_5);
        // setVisibility(View.VISIBLE);
    }

    /**
     * show panel at special state
     */
    public void showSpecialBar(int state) {
        bannerLayout.setVisibility(View.VISIBLE);
        ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_BANNER);
        boolean condition = !mNavIntegration.isCurrentSourceTv();
        boolean condition1 = mNavIntegration.isCurrentSourceBlockEx();
        //		boolean condition2 = (state == SPECIAL_INPUT_LOCK
        //				|| state == SPECIAL_NO_SIGNAL || state == SPECIAL_PROGRAM_LOCK);
        if (condition) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in showSpecialBar :source not tv... ");
            mBasicBanner.hide();
            mDetailBanner.hide();
            mSimpleBanner.show(false);
        } else {
            if (condition1) {
                state = SPECIAL_INPUT_LOCK;
            }
            if (state == SPECIAL_INPUT_LOCK) {
                mBasicBanner.hide();
                mDetailBanner.hide();
                mSimpleBanner.show(false);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in showSpecialBar for input block state == "
                        + state + ",condition1==" + condition1);
            } else {
                mSimpleBanner.show(true);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in showSpecialBar state == " + state
                    + ",condition1==" + condition1);
                mBasicBanner.showSpecialInfo(state);
            }

            mDetailBanner.hide();
        }

        // ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_BANNER);
        startTimeout(NAV_TIMEOUT_5);
    }

    public void showByState(int state, boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showByState=" + state);
        switch (state) {
            case CHILD_TYPE_INFO_NONE:
                showNoneBar(true, isHandleKey);
                break;
            case CHILD_TYPE_INFO_SIMPLE:
                showSimpleBar(isHandleKey);
                break;
            case CHILD_TYPE_INFO_BASIC:
                showBasicBar(isHandleKey);
                break;
            case CHILD_TYPE_INFO_DETAIL:
                showDetailBar(isHandleKey);
                break;
            default:
                break;
        }
    }

    /**
     * show channel and program panel
     */
    public void showBasicBar(boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showBasicBar.");
        if (disableShowBanner(isHandleKey)) {
            return;
        }
        mBasicBanner.mSpecialInfo.setVisibility(View.GONE);
        bannerLayout.setVisibility(View.VISIBLE);
        //		boolean noChannel = false;
        //		if (mNavIntegration.isCurrentSourceTv()) {
        //			if (mNavIntegration.getChannelAllNumByAPI() <= 0) {
        //				noChannel = true;
        //			}
        //		}
        //
        //		if (mNavIntegration.is3rdTVSource()) {
        //			if (mNavIntegration.getAllChannelListByTIFFor3rdSource() <= 0) {
        //				noChannel = true;
        //			}
        //		}
        PwdDialog mPWDDialog = (PwdDialog) ComponentsManager
            .getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
        boolean pwdDialogIsShowing = mPWDDialog.isShowing();
        int showFlagtemp = MtkTvPWDDialog.getInstance().PWDShow();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showFlagtemp=" + showFlagtemp);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "pwdDialogIsShowing=" + pwdDialogIsShowing);
        if (pwdDialogIsShowing) {
            mDetailBanner.hide();
            return;
        }
        // if((null != mBasicBanner) &&(View.VISIBLE !=
        // mBasicBanner.getVisibility())){
        // if (!noChannel)
        mSimpleBanner.show(true);
        mBasicBanner.show();
        // }
        mDetailBanner.hide();
        ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_BANNER);
        startTimeout(NAV_TIMEOUT_5);
        // setVisibility(View.VISIBLE);
    }

    /**
     * hide three panels but show external panel layout
     */
    public void showNoneBar(boolean isVisible, boolean isHandleKey) {
        if (isVisible) {
            mBasicBanner.hide();
            mSimpleBanner.hide();
            mDetailBanner.hide();
            mStateManage.setOrietation(true);
            if (getVisibility() != View.INVISIBLE) {
                this.setVisibility(View.INVISIBLE);
            }
        } else {
            showSimpleBar(isHandleKey);
            setBannerState(CHILD_TYPE_INFO_SIMPLE);
        }
    }

    public void showBannerAfterSVCChange() {
        if (disableShowBanner(false)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBannerAfterSVCChange return.");
            return;
        }
        showSimpleBanner();
        if (!isSpecialState) {
            if (!mNavIntegration.isCurrentSourceBlockEx()) {
                setBannerState(BannerView.CHILD_TYPE_INFO_BASIC);
            }
        }
        navBannerHandler.removeMessages(SHOW_BASIC_BANNER_AFTER_SVC_CHANGE);
        navBannerHandler.sendEmptyMessageDelayed(SHOW_BASIC_BANNER_AFTER_SVC_CHANGE, 2000);//refer linux logic
    }

    public void showSimpleBanner() {
        showSimpleBar(false);
        ttsCurrentChannel();
    }

    public void ttsCurrentChannel() {
        if ((mNavBannerImplement != null)
            && (ttsUtil != null)
            && (mNavIntegration.isCurrentSourceTv() || mNavIntegration
            .is3rdTVSource())) {
            String channelChangeTtsTexttemp = "";
            if (TextUtils.isEmpty(mNavBannerImplement.getCurrentChannelName())) {
                channelChangeTtsTexttemp = "Channel number is "
                    + mNavBannerImplement.getCurrentChannelNum();
            } else {
                channelChangeTtsTexttemp = "Channel number is "
                    + mNavBannerImplement.getCurrentChannelNum()
                    + ", channel name is "
                    + mNavBannerImplement.getCurrentChannelName();
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ttsUtil  channelChangeTtsText - "
                + channelChangeTtsTexttemp);
            if (TextUtils.isEmpty(channelChangeTtsText)
                || !TextUtils.equals(channelChangeTtsText,
                channelChangeTtsTexttemp)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ttsUtil  start TTS speak!");
                channelChangeTtsText = channelChangeTtsTexttemp;
                sendTTSSpeakMsg(channelChangeTtsText);
            }
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "itv test  mNavBannerImplement------.....==null");
        }
    }

    public void setBlockStateFromSVCTX(boolean blockState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setBlockStateFromSVCTX blockState=" + blockState);
        isBlockStateFromSVCTX = blockState;
    }

    public void updateRatingInfo() {
        if (mBasicBanner.getVisibility() == View.VISIBLE
            && !CommonIntegration.isCNRegion() && !isSpecialState) {
            TVAsyncExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    mBasicBanner.mProgramRating = mNavBannerImplement
                        .getProgramRating();
                    mBasicBanner.mNextProgramRating = mNavBannerImplement
                        .getNextProgramRating();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateRatingInfo---->mProgramRating="
                        + mBasicBanner.mProgramRating);
                    bannerLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateRatingInfo mRateTextView VISIBLE");
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateRatingInfo mRateTextView isSpecialState=" + isSpecialState);
                            if (!isSpecialState && !isBlockStateFromSVCTX) {
                                mBasicBanner.mRateTextView.setVisibility(View.VISIBLE);
                                mBasicBanner.mRateTextView.setText(
                                    !mNextEvent ? mBasicBanner.mProgramRating : mBasicBanner.mNextProgramRating);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * show channel panel
     */
    private void showSimpleBar(boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSimpleBar");
        if (disableShowBanner(isHandleKey)) {
            return;
        }
        boolean isBasicShowing = isVisible() && mBasicBanner.isVisible();
        bannerLayout.setVisibility(View.VISIBLE);
        if (!mNavIntegration.isCurrentSourceTv() ||
            isBasicShowing && mStateManage.getState() == CHILD_TYPE_INFO_SIMPLE) {
            mBasicBanner.hide();
        }
        mDetailBanner.hide();
        mSimpleBanner.show(false);
        ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_BANNER);
        startTimeout(NAV_TIMEOUT_5);
        // setVisibility(View.VISIBLE);
    }

    private boolean isMHeg5Showing() {
        int getActiveCompId = ComponentsManager.getNativeActiveCompId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getActiveCompId=" + getActiveCompId);
        return getActiveCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5;
    }

    private boolean isNatvieCompShowing() {
        int getActiveCompId = ComponentsManager.getNativeActiveCompId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isNatvieCompShowing----->getActiveCompId="
            + getActiveCompId);
        return getActiveCompId == NavBasic.NAV_NATIVE_COMP_ID_FVP
            || (getActiveCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5 && mDisableBanner);
        // active is mheg5 and must disbale
    }

    private boolean disableShowBanner(boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHandleKey=" + isHandleKey + ",mDisableBanner="
            + mDisableBanner + ",mIs3rdRunning=" + mIs3rdRunning);
        //add by xun
        if(mTvToSystemManager == null){
            mTvToSystemManager = TvToSystemManager.getInstance();
        }
        if(mTvToSystemManager != null && mTvToSystemManager.isUseCustomizeUi()) {
            return true;
        }
        //end xun

        if (isHandleKey && isMHeg5Showing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NativeComp is showing,do not show bannerview!");
            return false;
        }

        if (isMenuOptionShow()) {
            return true;
        }

        // mIs3rdRunning All must be true.
        if (mDisableBanner || mIs3rdRunning || isNatvieCompShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Do not show bannerview!");
            return true;
        }
        if (mOADIsActive) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "OAD is active, do not show bannerview!");
            return true;
        }
        if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVR playback running,do not show bannerview!");
            return true;
        }
        if (StateDvrFileList.getInstance() != null && StateDvrFileList.getInstance().isShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVR recordlist show,do not show bannerview!");
            return true;
        }
        if (DiskSettingDialog.getDiskSettingDialog() != null && DiskSettingDialog.getDiskSettingDialog().isShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DiskSettingDialog show,do not show bannerview!");
            return true;
        }
        if (DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog() != null && DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().isShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DiskSettingSubMenuDialog show,do not show bannerview!");
            return true;
        }
        if (ComponentsManager.getInstance().isContainsComponentsForCurrentActiveComps(
            NAV_COMP_ID_CH_LIST,
            NAV_COMP_ID_FAV_LIST,
            NAV_COMP_ID_TELETEXT)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Can't show banner when ch-list/fav-list/ttx showing.");
            return true;
        }
        return false;
    }

    /**
     * update the banner state in sequence
     */
    public void setBannerState(int state) {
        mStateManage.setState(state);
    }

    public void refreshAfterAudioFMTUpdate() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refreshAfterAudioFMTUpdate");
        if (!mNavIntegration.isCurrentSourceTv()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refreshAfterAudioFMTUpdate to show simple banner.");
            showSimpleBar(false);
        }
    }

    /**
     * hide external panel layout
     */
    public void hideAllBanner() {
        mBasicBanner.hide();
        mSimpleBanner.hide();
        mDetailBanner.hide();
        if (View.VISIBLE == bannerLayout.getVisibility()) {
            bannerLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void channelInputChangeShowBanner() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
            "come in channelInputChangeShowBanner mNumputChangeChannel == "
                + mNumputChangeChannel
                + ",interruptShowBannerWithDelay =="
                + interruptShowBannerWithDelay);
        if (isGingaRunning()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelInputChangeShowBanner mtkTvGingaAppInfoBase is running.");
            return;
        }
        if (mNavIntegration.isCHChanging()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelInputChangeShowBanner isCHChanging.");
            return;
        }
        if (!mNumputChangeChannel
            && !interruptShowBannerWithDelay
            && ComponentsManager.getNativeActiveCompId() != NavBasic.NAV_NATIVE_COMP_ID_HBBTV
            && ComponentsManager.getNativeActiveCompId() != NavBasic.NAV_NATIVE_COMP_ID_MHEG5) {
            if (mNavIntegration.isCurrentSourceTv()) {
                show(false, -1, false);
            } else {
                showSimpleBar(false);
            }
        }
    }

    private void inputChannelNum(int keycode) {
        mNumputChangeChannel = true;
        if (keycode >= KeyMap.KEYCODE_0 && keycode <= KeyMap.KEYCODE_9) {
            // inputChannelNumStrBuffer.append("" + (keycode -
            // KeyMap.KEYCODE_0));
            if (mSelectedChannelNumString.indexOf(channelNumSeparator) == -1) {
                mSelectedChannelNumString = mSelectedChannelNumString
                    + (keycode - KeyMap.KEYCODE_0);
                if (MarketRegionInfo.REGION_EU == MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    if (mSelectedChannelNumString.length() <= 4) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "length() <= 5");
                    } else {
                        mSelectedChannelNumString = mSelectedChannelNumString
                            .substring(4, 5);
                    }
                } else {
                    if (mSelectedChannelNumString.length() <= 5) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "length() <= 5");
                    } else {
                        mSelectedChannelNumString = mSelectedChannelNumString
                            .substring(5, 6);
                    }
                }
                mSelectedChannelNumString = ""
                    + Integer.valueOf(mSelectedChannelNumString);
            } else if (mSelectedChannelNumString.indexOf(channelNumSeparator) != -1) {
                mSelectedChannelNumString = mSelectedChannelNumString
                    + (keycode - KeyMap.KEYCODE_0);
                String[] channelNum = mSelectedChannelNumString.split("-".equals(channelNumSeparator) ? channelNumSeparator : "\\.");
                if (mSelectedChannelNumString.length() <= 9) {
                    mSelectedChannelNumString = channelNum[0] + channelNumSeparator
                        + Integer.valueOf(channelNum[1]);
                } else {
                    mSelectedChannelNumString = channelNum[0]
                        + channelNumSeparator
                        + Integer.valueOf(mSelectedChannelNumString
                        .substring(channelNum[0].length() + 1 + 1,
                            10));
                }
            }
        } else if ((KeyMap.KEYCODE_PERIOD == keycode)
            && (mSelectedChannelNumString.indexOf(channelNumSeparator) == -1)) {
            // inputChannelNumStrBuffer.append("-");
            mSelectedChannelNumString = mSelectedChannelNumString + channelNumSeparator;
        } else if ((KeyMap.KEYCODE_PERIOD == keycode)
            && (mSelectedChannelNumString.indexOf(channelNumSeparator) != -1)) {
            String[] numString = mSelectedChannelNumString.split("-".equals(channelNumSeparator) ? channelNumSeparator : "\\.");
            mSelectedChannelNumString = numString[0];
        }
    }

    public void cancelNumChangeChannel() {
        mNumputChangeChannel = false;
        hideAllBanner();
        navBannerHandler.removeMessages(MSG_NUMBER_KEY);
        mSelectedChannelNumString = "";
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus statusID =" + statusID
            + ">>value=" + value);
        switch (statusID) {

            case ComponentStatusListener.NAV_KEY_OCCUR:
                break;
            case ComponentStatusListener.NAV_CONTENT_ALLOWED:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "content allowed:specialType==" + specialType
                    + ",isSpecialState==" + isSpecialState);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Banner_NAV_CONTENT_ALLOWED content allowed");
                if (mNavIntegration.is3rdTVSource()) {
                    reset();
                    return;
                }
                int showFlagtemp = MtkTvPWDDialog.getInstance().PWDShow();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFlagtemp=" + showFlagtemp);
                if (showFlagtemp == 0) {
                    return;
                }
                if (isGingaRunning()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_CONTENT_ALLOWED mtkTvGingaAppInfoBase is running.");
                    return;
                }
                // if specialType is really special keep isSpecialState value not
                // change!!
                if ((specialType >= 1 && specialType <= 5)
                    || (specialType >= 16 && specialType <= 20)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "content allowed:focus=="
                            + mNavIntegration.getCurrentFocus() + ",isTV=="
                            + mNavIntegration.isCurrentSourceTv());
                    if (mNavIntegration.getCurrentFocus().equalsIgnoreCase("sub")
                        && mNavIntegration.isPipOrPopState()
                        && mNavIntegration.isCurrentSourceTv()) {
                        isSpecialState = false;
                        specialType = -1;
                    }
                } else {
                    isSpecialState = false;
                    specialType = -1;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSpecialState -----------  = " + isSpecialState);
                if (mNavBannerImplement.isShowBannerBar()) {
                    bSourceOnTune = true;
                    //hideAllBanner();
                } else {
                    int visi = getBasicBanner().getVisibility();
                    if (visi != 0 && DestroyApp.isCurActivityTkuiMainActivity()
                        && !mNavIntegration.is3rdTVSource()) {// not visible
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "content allowed :now banner is not show in ap");
                        bSourceOnTune = true;
                    } else {// visible
                        bSourceOnTune = false;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "content allowed:bSourceOnTune==" + bSourceOnTune);
                if (DestroyApp.isCurActivityTkuiMainActivity()) {
                    show(false, -1, false);
                }
                break;
            case ComponentStatusListener.NAV_COMPONENT_SHOW:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "content NAV_COMPONENT_SHOW");
                int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
                PwdDialog mPWDDialog = (PwdDialog) ComponentsManager
                    .getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
                boolean pwdDialogIsShowing = mPWDDialog.isShowing();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFlag: " + showFlag);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pwdDialogIsShowing: " + pwdDialogIsShowing);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSpecialState: " + isSpecialState);
                if (showFlag == 0 && !isSpecialState) {
                    break;
                }
                if (value == NAV_COMP_ID_PWD_DLG
                    && DestroyApp.isCurActivityTkuiMainActivity()) {
                    show(false, -1, false);
                }
                break;
            case ComponentStatusListener.NAV_COMPONENT_HIDE:
                break;
            case ComponentStatusListener.NAV_PAUSE:
                if (mNavBannerImplement != null) {
                    mNavBannerImplement.setCCVisiable(false);
                }
                break;
            case ComponentStatusListener.NAV_RESUME:
                break;
            case ComponentStatusListener.NAV_OAD_STATE:
                if (value == 1) { // 1 is oad active
                    mOADIsActive = true;
                } else {
                    mOADIsActive = false;
                }
                break;
            case ComponentStatusListener.NAV_CH_CHANGE_BY_DIGITAL:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus--->NAV_CH_CHANGE_BY_DIGITAL");
                mSelectedChannelNumString = String.valueOf(value);
                turnCHByNumKey();
                break;
            default:
                break;
        }
    }

    private boolean isGingaRunning() {
        List<MtkTvGingaAppInfoBase> mtkTvGingaAppInfoBaseList = MtkTvGinga.getInstance().getApplicationInfoList();
        for (MtkTvGingaAppInfoBase mtkTvGingaAppInfoBase : mtkTvGingaAppInfoBaseList) {
            if (mtkTvGingaAppInfoBase.isRunning()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mtkTvGingaAppInfoBase is running.");
                return true;
            }
        }
        return false;
    }

    public void pvrChangeNum(String value) {
        startTimeout(NAV_TIMEOUT_5);
        mSelectedChannelNumString = value;
        navBannerHandler.removeMessages(MSG_NUMBER_KEY);
        navBannerHandler.sendEmptyMessageDelayed(MSG_NUMBER_KEY, NAV_TIMEOUT_1);
    }

    public void setInterruptShowBanner(boolean interrupt) {
        interruptShowBannerWithDelay = interrupt;
    }

    private void hideSundryTextView() {
        SundryShowTextView sundryTextView = (SundryShowTextView) mComponentsManager
            .getComponentById(NAV_COMP_ID_SUNDRY);
        if ((null != sundryTextView) && sundryTextView.isVisible()) {
            sundryTextView.setVisibility(View.GONE);
        }
    }

    private void hideChannelListDialog() {
        mChannelListDialog = (ChannelListDialog) mComponentsManager
            .getComponentById(NAV_COMP_ID_CH_LIST);
        if ((null != mChannelListDialog) && mChannelListDialog.isShowing()) {
            mChannelListDialog.dismiss();
        }
    }

    private void hideFavoriteChannelList() {
        mFavoriteChannelListView = (FavoriteListDialog) mComponentsManager
            .getComponentById(NAV_COMP_ID_FAV_LIST);
        if ((null != mFavoriteChannelListView)
            && mFavoriteChannelListView.isShowing()) {
            mFavoriteChannelListView.dismiss();
        }
    }

    public boolean isChangingChannelWithNum() {
        return mNumputChangeChannel;
    }

    public void changeChannelFavoriteMark() {
        if ((getVisibility() != View.VISIBLE)
            || (CHILD_TYPE_INFO_BASIC != mStateManage.getState())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in changeChannelFavoriteMark 1");
            mBasicBanner.setFavoriteVisibility();
            if (isSpecialState || !mNavIntegration.isCurrentSourceTv()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in changeChannelFavoriteMark show state == -1");
                show(false, -1, false);
            } else {
                show(false, CHILD_TYPE_INFO_BASIC, false);
                mStateManage.setState(CHILD_TYPE_INFO_BASIC);
                mStateManage.setOrietation(true);
            }
        } else {
            if (mNavIntegration.isCurrentSourceTv()
                && !mNavIntegration.isCurrentSourceBlockEx()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in changeChannelFavoriteMark 2");
                mBasicBanner.setFavoriteVisibility();
                mSimpleBanner.showSimpleChannel();
                startTimeout(NAV_TIMEOUT_5);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in changeChannelFavoriteMark ,source not tv not set favorite!!");
            }
        }
    }

    public void unScramebled() {
        audioScramebled = false;
        videoScramebled = false;
    }

    public boolean bVideoScramebled() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "bVideoScramebled:" + videoScramebled);
        return videoScramebled;
    }

    public void setOnTuningSource(boolean bSourceOnTune) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setOnTuningSource:" + bSourceOnTune);
        this.bSourceOnTune = bSourceOnTune;
    }

    private boolean isMenuOptionShow() {
        if ((mComponentsManager == null)
            || ((mComponentsManager
            .getComponentById(NAV_COMP_ID_MENU_OPTION_DIALOG)) == null)) {
            return false;
        } else {
            boolean ret = mComponentsManager.getComponentById(
                NAV_COMP_ID_MENU_OPTION_DIALOG).isVisible();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isMenuOptionShow:" + ret);
            return ret;
        }
    }
}
