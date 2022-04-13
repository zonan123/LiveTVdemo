
package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvHtmlAgentBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvGingaAppInfoBase;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.DetailTextReader;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
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
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;

import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UkBannerView extends NavBasicView implements ComponentStatusListener.ICStatusListener {
    private final static String TAG = "UkBannerView";

    //hide banner
    public final static int HBBTV_MSG_DISABLE_BANNER = 257;
    //no hide banner
    public final static int HBBTV_MSG_ENABLE_BANNER = 258;

    //whether disable or enable msg
    public final static int MHEG5_MSG_IF_DISABLE = 258;
    public final static int MHEG5_MSG_DISABLE_BANNER = 0;
    public final static int MHEG5_MSG_ENABLE_BANNER = 1;
    // 262 running 263 not running
    public final static int HBBTV_STATUS_3RD_APP_RUNNING = 262;

    public final static int HBBTV_STATUS_3RD_APP_NOT_RUNNING = 263;
    public final static int HBBTV_STATUS_APP_VISIBLE = 264; // 264

    public final static int HBBTV_STATUS_APP_INVISIBLE = 265; // 265

    UkBannerView bannerView;
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

    private TextToSpeechUtil ttsUtil;

    private static CommonIntegration mNavIntegration = null;
    //    private boolean mNeedChangeSource;
    //    private int mTuneChannelId = -1;

    private static BannerImplement mNavBannerImplement = null;
    private String channelChangeTtsText = "";

    private View bannerLayout;

    private ComponentsManager mComponentsManager;

    public final static int SPECIAL_NO_CHANNEL = 16;
    public final static int SPECIAL_EMPTY_SPECIAFIED_CH_LIST = 17;
    public final static int SPECIAL_NO_SIGNAL = 1;
    public final static int SPECIAL_INPUT_LOCK = 4;
    public final static int SPECIAL_CHANNEL_LOCK = 2;
    public final static int SPECIAL_RETRIVING = 19;
    public final static int SPECIAL_NO_SUPPORT = 20;
    public final static int SPECIAL_PROGRAM_LOCK = 3;
    public final static int SPECIAL_HIDDEN_CHANNEL = 5;

    public final static int BANNER_MSG_NAV = 0;
    public final static int BANNER_MSG_ITEM = 1;

    public final static int TV_STATE_NORMAL = 15;

    public final static int SPECIAL_NO_AUDIO_VIDEO = 6;
    public final static int SPECIAL_SCRAMBLED_VIDEO_NO_AUDIO = 7;
    public final static int SPECIAL_SCRAMBLED_AUDIO_VIDEO = 8;
    public final static int SPECIAL_SCRAMBLED_VIDEO_CLEAR_AUDIO = 9;
    public final static int SPECIAL_SCRAMBLED_AUDIO_NO_VIDEO = 10;
    public final static int SPECIAL_STATUS_AUDIO_ONLY = 11;
    public final static int SPECIAL_STATUS_UNKNOWN = 12;
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
    public final static int BANNER_MSG_ITM_AUDIO_INFO_CHANGE = 19;
    public final static int BANNNER_MSG_ITEM_IPTS_NAME = 21;
    public final static int BANNNER_MSG_ITEM_IPTS_RSLT_CHANGE = 22;
    public final static int BANNNER_MSG_ITEM_ATMOS_CHNAGE = 36;
    public final static int BANNNER_MSG_SECOND_TITLE_CHNAGE = 38;
    public final static int BANNNER_MSG_NEXT_SECOND_TITLE_CHNAGE = 39;
    public final static int BANNNER_MSG_LOGO_CHNAGE = 40;
    private final static int MSG_NUMBER_KEY = 1111;

    private final static int MSG_SHOW_SOURCE_BANNER_TIMING = 1112;
    private final static int MSG_TTS_SPEAK_DELAY = 1113;
    private final static int MSG_TIME_OUT = 1114;
    //    private boolean audioScramebled = false;
    //    private boolean videoScramebled = false;
    //    private boolean noAudio = false;
    //    private boolean noVideo = false;
    private boolean isFvpVisible = false;
    private boolean isSpecialState = false;
    private String mSelectedChannelNumString = "";
    //    private SundryImplement mNavSundryImplement = null;

    private int specialType = -1;

    // private StringBuffer inputChannelNumStrBuffer = new StringBuffer();

    private boolean mNumputChangeChannel = false;

    private boolean isShowEmptyVideoInfoFirst = false;

    private boolean bSourceOnTune = false;// source on tuning
    private MtkTvHtmlAgentBase mMtkTvHtmlAgentBase;
    private boolean isCurrentProgrmma = true;
    // private boolean mHeg5IsActived = false;
    // private boolean mHeg5IsActived = false;

    private boolean mDisableBanner = false;

    private boolean mIs3rdRunning = false;
    private boolean mOADIsActive = false;
    private boolean isHostQuietTuneStatus = false;

    private TextView currentProgrammeNumberTV,
        currentProgrammeFirstNameTV,
        currentProgrammeSecondNameTV,
        currentProgrammeDurationTV,
        currentProgrammeLabelSubtitleTV,
        mVisualImpairedIcon,
        mHearingImpairedIcon,
        currentProgrammeLabelRatingTV,
        currentProgrammeTimeTV,
        programmeTotalMinTV,
        currentChannelName,
        programmeIndicationTextTv,
        bannerCategoryTv;
    private ImageView mLockIcon,
        mFavoriteIcon;
    private TextView simpleChannelNumberView,
        bannerSpecialInfoTV;
    private ImageView currentProgrammeIconIV,
        programmeIndicationIV;
    private TextView guidance;
    private ProgressBar durationProgressPR;
    private RelativeLayout basicBannerAll,
        itemOneLayout,
        itmeTwoLayout,
        lockIconAllLayout;
    private String mProgramTime,
        mCurrentProgrammeSecondName,
        mCurrentProgrammeTime,
        mNextProgrammeFirstName,
        mNextProgrammeSecondName,
        mNextProgrammeDuration,
        mNextCategory,
        mNextRating,
        mProgrammeIconString,
        mCurrentProgrammeFirstName,
        mDetailBannerCategory,
        mCurrentProgrammeNumber,
        mProgramRating,
        mCurrentChannelName;
    private int mDurationProgress;
    //    private int mNextDurationProgress;
    //simple source
    private View mSelfLayout;
    private TextView mFirstLine;
    private ImageView mImgDoblyType;
    private ImageView mImgDoblyVision;
    private TextView mDtsType;
    private ImageView mImgDoblyLogo;
    private ImageView mImgDoblyVisionLogo;
    //    private View mSecondLine;
    private TextView mChannelName;
    private ImageView mSimpleLockIcon;
    private boolean isHasSignal = true;
    private boolean isHasChannel = true;
    private boolean isHasLocked = false;
    private int mLockInfoId = 0;
    //    private View mThirdLine;
    public TextView mCCView;
    /**
     * resolution
     */
    private TextView mThirdMiddle;
    private View mTypeTimeLayout;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    //    private TextView mReceiverType;
    //    private TextView mTime;
    public UkBannerView(Context context) {
        super(context);
        init(context);
    }

    public UkBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        componentID = NAV_COMP_ID_BANNER;
        bannerView = this;
        ttsUtil = new TextToSpeechUtil(context);
        ((Activity) mContext).getLayoutInflater().inflate(
            R.layout.uk_nav_banner_layout, this);
        mNavIntegration = CommonIntegration.getInstance();

        BannerImplement.setBannerImplement(mNavBannerImplement = new BannerImplement(mContext));

        bannerLayout = (View) findViewById(R.id.banner_info_layout);
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
            (int) (ScreenConstant.SCREEN_WIDTH),
            LinearLayout.LayoutParams.WRAP_CONTENT);

        bannerLayout.setLayoutParams(mParams);
        setAlpha(0.9f);
        initView();
        //        mNavSundryImplement = SundryImplement.getInstanceNavSundryImplement(context);
        mComponentsManager = ComponentsManager.getInstance();
        mMtkTvHtmlAgentBase = new MtkTvHtmlAgentBase();
        TvCallbackHandler mBannerTvCallbackHandler = TvCallbackHandler.getInstance();

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
            ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_CONTENT_ALLOWED, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_CH_CHANGE_BY_DIGITAL, this);
        ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_OAD_STATE, this);
        initSimpleSource();
        initIcons();
        initSimpleBannerView();
        //initDetailBannerView();
        initBasicBannerView();
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
                    if (mNavIntegration.getChannelNumByAPIForBanner() <= 0) {
                        isSpecialState = true;
                        specialType = SPECIAL_NO_CHANNEL;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "please scan channel!!!");
                        return true;
                    }
                }
                showBasicBanner(false);
            }
        }

        return true;
    }

    void initSimpleSource() {
        mSelfLayout = (View) findViewById(R.id.banner_simple_source_layout);
        mSelfLayout
            .setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        mSelfLayout.setVisibility(View.INVISIBLE);
        mImgDoblyType = (ImageView) findViewById(R.id.banner_dolby_type_simple);
        mDtsType = (TextView) findViewById(R.id.banner_dts_type_simple);
        mImgDoblyVision = (ImageView) findViewById(R.id.banner_dolby_vision_simple);
        mImgDoblyLogo = (ImageView) findViewById(R.id.banner_channel_dolby_icon);
        mImgDoblyVision = (ImageView) findViewById(R.id.banner_channel_dolby_vision_icon);
        mFirstLine = (TextView) findViewById(R.id.banner_simple_first_line);
        //        mSecondLine = (View) findViewById(R.id.banner_simple_second_line_layout);
        mChannelName = (TextView) findViewById(R.id.banner_simple_channel_name);
        mSimpleLockIcon = (ImageView) findViewById(R.id.banner_simple_lock_icon);
        //        mThirdLine = (View) findViewById(R.id.banner_simple_third_line_layout);
        mThirdMiddle = (TextView) findViewById(R.id.banner_simple_third_middle);
        mCCView = (TextView) findViewById(R.id.banner_simple_cc);
        mTypeTimeLayout = (View) findViewById(R.id.banner_simple_type_time_layout);
        //        mReceiverType = (TextView) findViewById(R.id.banner_simple_receiver_type);
        TextView mTime = (TextView) findViewById(R.id.banner_simple_time);
        String strTime = mNavBannerImplement.getCurrentTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SimpleBanner strTime" + strTime);
        mTime.setText(strTime);
    }

    void initIcons() {
        mLockIcon = (ImageView) findViewById(R.id.banner_channel_lock_icon);
        mFavoriteIcon = (ImageView) findViewById(R.id.banner_channel_favorite_icon);
        currentProgrammeLabelSubtitleTV = (TextView) findViewById(R.id.banner_current_programme_label_subtitle);
        currentProgrammeLabelRatingTV = (TextView) findViewById(R.id.banner_current_programme_label_rating);
        mVisualImpairedIcon = (TextView) findViewById(R.id.banner_current_programme_label_visual_impaired);
        mHearingImpairedIcon = (TextView) findViewById(R.id.banner_current_programme_label_hearing_impaired);
    }

    void initSimpleBannerView() {
        simpleChannelNumberView = (TextView) findViewById(R.id.simple_channel_number);
        simpleChannelNumberView.setVisibility(View.VISIBLE);
    }

    void initBasicBannerView() {
        currentProgrammeNumberTV = (TextView) findViewById(R.id.banner_current_programme_number_text);
        currentProgrammeIconIV = (ImageView) findViewById(R.id.banner_current_programme_icon);//from other
        currentProgrammeFirstNameTV = (TextView) findViewById(R.id.banner_current_programme_first_name);
        currentProgrammeSecondNameTV = (TextView) findViewById(R.id.banner_current_programme_second_name);//from other

        currentProgrammeDurationTV = (TextView) findViewById(R.id.banner_current_programme_duration);
        programmeTotalMinTV = (TextView) findViewById(R.id.banner_current_programme_total_min);
        bannerCategoryTv = (TextView) findViewById(R.id.detail_banner_category);
        currentProgrammeTimeTV = (TextView) findViewById(R.id.banner_current_programme_current_time);
        currentChannelName = (TextView) findViewById(R.id.banner_current_channel_name);

        durationProgressPR = (ProgressBar) findViewById(R.id.uk_banner_current_programme_duration_progress);

        //        currentProgrammeLogoOne=(ImageView)findViewById(R.id.banner_current_programme_logo_one);//from other

        programmeIndicationIV = (ImageView) findViewById(R.id.banner_current_programme_indication);
        programmeIndicationTextTv = (TextView) findViewById(R.id.banner_current_programme_indication_text);

        bannerSpecialInfoTV = (TextView) findViewById(R.id.banner_special_info);
        basicBannerAll = (RelativeLayout) findViewById(R.id.basic_banner_all);
        itmeTwoLayout = (RelativeLayout) findViewById(R.id.itme_two);
        itemOneLayout = (RelativeLayout) findViewById(R.id.item_one);
        lockIconAllLayout = (RelativeLayout) findViewById(R.id.lock_icon_all);
        guidance = (TextView) findViewById(R.id.banner_current_programme_label_guidance);
    }

    private void showSimpleSource() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSimpleSource");
        setVisibility(View.VISIBLE);
        basicBannerAll.setVisibility(View.GONE);
        bannerLayout.setVisibility(View.VISIBLE);
        simpleChannelNumberView.setVisibility(View.INVISIBLE);
        mSelfLayout.setVisibility(View.VISIBLE);
        boolean showLock = mNavBannerImplement.isShowInputLockIcon()
            && !isShowEmptyVideoInfoFirst;
        String sourceName = InputSourceManager.getInstance()
            .getCurrentInputSourceName(
                mNavIntegration.getCurrentFocus());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentInputName: " + sourceName);
        String privSName = CommonIntegration.getInstance().getPrivSimBFirstLine();
        mImgDoblyType.setVisibility(View.GONE);
        mImgDoblyVision.setVisibility(View.GONE);
        mDtsType.setVisibility(View.GONE);
        if (CommonIntegration.getInstance().isAudioFMTUpdated()) {
            showDoblyIconOrDts();
        }
        if (mNavIntegration.is3rdTVSource() && !showLock) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "only show source name~");
            mSimpleLockIcon.setVisibility(View.INVISIBLE);
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
        if (!TextUtils.equals(sourceName, privSName)) {
            mThirdMiddle.setText("");
            CommonIntegration.getInstance().setPrivSimBFirstLine(sourceName);
        }

        if (showLock) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "VISIBLE~ ");
            mSimpleLockIcon.setVisibility(View.VISIBLE);
            mThirdMiddle.setVisibility(View.GONE);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getIptsRslt INVISIBLE~ " + mThirdMiddle.getText());
            mSimpleLockIcon.setVisibility(View.INVISIBLE);
            if (mThirdMiddle.getVisibility() != View.VISIBLE) {
                mThirdMiddle.setText(null);
                mThirdMiddle.setVisibility(View.VISIBLE);
                navBannerHandler.removeMessages(MSG_SHOW_SOURCE_BANNER_TIMING);
                navBannerHandler.sendEmptyMessageDelayed(MSG_SHOW_SOURCE_BANNER_TIMING, 500);
            }
        }
        setTimeOut();
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

    private void showDoblyLogo() {
        int doblyType = mNavBannerImplement.getDoblyType();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDoblyLogo:doblyType=" + doblyType);
        int resId = R.drawable.icon_dobly_atoms;
        if (doblyType == BannerImplement.DOBLY_TYPE_ATOMS) {
            resId = R.drawable.icon_dobly_atoms;
        } else if (doblyType == BannerImplement.DOBLY_TYPE_AUDIO) {
            resId = R.drawable.icon_dobly_audio;
        }
        if (doblyType == BannerImplement.DOBLY_TYPE_ATOMS ||
            doblyType == BannerImplement.DOBLY_TYPE_AUDIO) {
            mImgDoblyLogo.setImageResource(resId);
            mImgDoblyLogo.setVisibility(View.VISIBLE);
        } else {
            mImgDoblyLogo.setVisibility(View.GONE);
        }
        if (mNavBannerImplement.isDolbyVision()) {
            mImgDoblyVision.setImageResource(R.drawable.icon_dobly_vision);
            mImgDoblyVision.setVisibility(VISIBLE);
        } else {
            mImgDoblyVision.setVisibility(GONE);
        }
    }

    private void setIconsVisibility() {
        setLockVisibility();
        setFavoriteVisibility();
        setCaptionVisibility();
        setEyeOrEarHinderIconVisibility();
        showDoblyLogo();
    }

    private void setEyeOrEarHinderIconVisibility() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setEyeOrEarHinderIconVisibility");
        if (mNavBannerImplement.isShowADEIcon(isCurrentProgrmma)) {
            mVisualImpairedIcon.setText(R.string.str_uk_banner_ad_icon);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setEyeOrEarHinderIconVisibility, show AD");
            mVisualImpairedIcon.setVisibility(View.VISIBLE);
        } else {
            mVisualImpairedIcon.setVisibility(View.GONE);
        }
        if (mNavBannerImplement.isShowEARIcon(isCurrentProgrmma)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setEyeOrEarHinderIconVisibility, show SL");
            mHearingImpairedIcon.setText(R.string.str_uk_banner_sl_icon);
            mHearingImpairedIcon.setVisibility(View.VISIBLE);
        } else {
            mHearingImpairedIcon.setVisibility(View.GONE);
        }
    }

    private void setCaptionVisibility() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility");
        currentProgrammeLabelSubtitleTV.setVisibility(View.INVISIBLE);
        if (mNavBannerImplement.isIPChannel()) {
            if (mNavBannerImplement.isShowCaptionIcon(isCurrentProgrmma)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility, ipchannel VISIBLE");
                currentProgrammeLabelSubtitleTV.setVisibility(View.VISIBLE);
            }
        } else if (mNavBannerImplement.isShowCaptionIconForUK(isCurrentProgrmma)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCaptionVisibility, VISIBLE");
            currentProgrammeLabelSubtitleTV.setVisibility(View.VISIBLE);
        }
    }

    private void setLockVisibility() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockVisibility");
        if (isShowTVLockIcon()
            && !mNavIntegration.is3rdTVSource()) {
            mLockIcon.setVisibility(View.VISIBLE);
            lockIconAllLayout.setVisibility(View.VISIBLE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockVisibility VISIBLE");
        } else {
            mLockIcon.setVisibility(View.GONE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockVisibility INVISIBLE");
            lockIconAllLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setFavoriteVisibility() {
        boolean isNeedShowFavicon = false;
        if (mNavIntegration.isCurrentSourceTv()
            && !mNavIntegration.isCurrentSourceBlocked()) {
            isNeedShowFavicon = true;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setFavoriteVisibility isNeedShowFavicon=="
            + isNeedShowFavicon);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setFavoriteVisibility isFavChannel=="
            + FavChannelManager.getInstance(mContext).isFavChannel());
        if (!mNavIntegration.is3rdTVSource() && isNeedShowFavicon
            && FavChannelManager.getInstance(mContext).isFavChannel()) {
            mFavoriteIcon.setVisibility(View.VISIBLE);
        } else {
            mFavoriteIcon.setVisibility(View.GONE);
        }
    }

    private boolean isShowTVLockIcon() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isShowTVLockIcon---->specialType=" + specialType);
        boolean isLocked = specialType == SPECIAL_CHANNEL_LOCK || specialType == SPECIAL_INPUT_LOCK || specialType == SPECIAL_PROGRAM_LOCK;
        if (!isLocked) {
            return false;
        }
        int showFlagtemp = MtkTvPWDDialog.getInstance().PWDShow();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isShowTVLockIcon---->showFlagtemp=" + showFlagtemp);
        return isLocked && showFlagtemp == 0;
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        // TODO Auto-generated method stub
        PWD_SHOW_FLAG = 0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in isKeyHandler keyCode===" + keyCode);
        switch (keyCode) {
            case KeyMap.KEYCODE_MTKIR_INFO:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler KEYCODE_MTKIR_INFO start.");

                //             requestIPMdsInfo();
                if (mNavIntegration.is3rdTVSource()) {
                    reset();
                }
                if (disableShowBanner(true)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in isKeyHandler,noting to do!");
                    return false;
                }
                if (getVisibility() != View.VISIBLE) {
                    showBasicBanner(true);
                } else {
                    gotoPage();
                    navBannerHandler.removeMessages(MSG_TIME_OUT);
                    navBannerHandler.sendEmptyMessage(MSG_TIME_OUT);
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
            case KeyMap.KEYCODE_PERIOD:

                if (mNavIntegration.is3rdTVSource()
                    || !mNavIntegration.isCurrentSourceTv()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not handle digtal key.");
                    return true;
                }
                if (isMenuOptionShow()) {
                    return true;
                }
                //             if (isSourceLockedOrEmptyChannel()) {
                //                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                //                         "come in onKeyHandler number key isCurrentSourceBlocked or no channel");
                //                 setTimeOut();
                //                 return true;
                //             }
                //             if (((MarketRegionInfo.REGION_EU == MarketRegionInfo
                //                     .getCurrentMarketRegion()) || (mSelectedChannelNumString
                //                     .length() == 0))
                //                     && (keyCode == KeyMap.KEYCODE_PERIOD)) {
                //                 return true;
                //             }
                if (mNavIntegration.isCurrentSourceTv()) {
                    if (mNavIntegration.getChannelAllNumByAPI() > 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "come in input key number 1");
                        sendTurnCHMsg(keyCode);
                    }
                }
                return true;
            case KeyMap.KEYCODE_BACK:
                if (getVisibility() != View.GONE) {
                    navBannerHandler.removeMessages(MSG_TIME_OUT);
                    navBannerHandler.sendEmptyMessage(MSG_TIME_OUT);
                }
                break;
            case KeyMap.KEYCODE_PAGE_DOWN:
                if (mNavIntegration.isCurrentSourceTv()
                    && !mNavIntegration.isCurrentSourceBlockEx()
                    && (mNavIntegration.getChannelAllNumByAPI() > 0)
                    && !mNavIntegration.is3rdTVSource()) {
                    ComponentsManager.getInstance().hideAllComponents();
                    FavChannelManager.getInstance(mContext).favAddOrErase();
                    setFavoriteVisibility();
                    showBasicBanner(false);
                    return true;
                }
            default:
                break;
        }
        return false;
    }

    public void showBasicBanner(boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBasicBanner isFvpVisible = " + isFvpVisible);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBasicBanner isHasSignal = " + isHasSignal);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBasicBanner isHasChannel = " + isHasChannel);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBasicBanner isHasLocked = " + isHasLocked);
        if (isHostQuietTuneStatus) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "silently change.");
            return;
        }
        if (disableShowBanner(isHandleKey) || isFvpVisible) {
            return;
        }
        if (!mNavIntegration.isCurrentSourceTv() && !mNavIntegration.is3rdTVSource()) {
            showSimpleSource();
            return;
        } else if (!isHasSignal) {
            showNoSignal();
            return;
        } else if (!isHasChannel) {
            showSpecialInfo(R.string.nav_please_scan_channels);
            return;
        } else if (isHasLocked) {
            showSpecialInfo(mLockInfoId);
            return;
        }
        isCurrentProgrmma = true;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBasicBanner");
        bannerSpecialInfoTV.setVisibility(View.GONE);
        simpleChannelNumberView.setVisibility(View.GONE);
        setVisibility(View.VISIBLE);
        mSelfLayout.setVisibility(View.INVISIBLE);
        bannerLayout.setVisibility(View.VISIBLE);
        basicBannerAll.setVisibility(View.VISIBLE);
        currentProgrammeFirstNameTV.setVisibility(View.VISIBLE);
        itemOneLayout.setVisibility(VISIBLE);
        itmeTwoLayout.setVisibility(View.VISIBLE);
        showCurrentProgrammIndication();
        if (!mNavBannerImplement.isIPChannel()) {
            getBannerInfoWithThead();
        } else {
            requestIPMdsInfo();
        }
        setTimeOut();
    }

    private void ttsCurrentChannel() {
        if ((mNavBannerImplement != null)
            && (ttsUtil != null)
            && (mNavIntegration.isCurrentSourceTv() || mNavIntegration
            .is3rdTVSource())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ttsUtil ttsCurrentChannel number:"
                + mNavBannerImplement.getCurrentChannelNum());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ttsUtil ttsCurrentChannel name:"
                + mNavBannerImplement.getCurrentChannelName());
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

    private void sendTTSSpeakMsg(String ttsStr) {
        if (ttsUtil != null && ttsUtil.isTTSEnabled()) {
            Message msg = Message.obtain();
            msg.what = MSG_TTS_SPEAK_DELAY;
            msg.obj = ttsStr;
            navBannerHandler.sendMessageDelayed(msg, 1000);
        }
    }

    void hideBasicBanner() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hideBasicBanner");
        bannerLayout.setVisibility(View.GONE);
    }

    void resetTitleColor() {
        currentProgrammeFirstNameTV.setTextColor(mContext.getResources().getColor(R.color.white));
        currentProgrammeSecondNameTV.setTextColor(mContext.getResources().getColor(R.color.white));
        currentProgrammeDurationTV.setTextColor(mContext.getResources().getColor(R.color.white));
        currentProgrammeLabelSubtitleTV.setTextColor(mContext.getResources().getColor(R.color.white));
        currentProgrammeLabelRatingTV.setTextColor(mContext.getResources().getColor(R.color.white));
        currentProgrammeLabelSubtitleTV.setBackgroundResource(R.drawable.nav_uk_banner_view_label_stroke_bg);
        currentProgrammeLabelRatingTV.setBackgroundResource(R.drawable.nav_uk_banner_view_label_stroke_bg);
    }

    void showCurrentProgrammInfo() {
        showCurrentProgrammIndication();
        isCurrentProgrmma = true;
        setIconsVisibility();
        //xian
        if (!TextUtils.isEmpty(mProgramRating)) {
            currentProgrammeLabelRatingTV.setVisibility(View.VISIBLE);
            currentProgrammeLabelRatingTV.setText(mProgramRating);
        } else {
            currentProgrammeLabelRatingTV.setVisibility(View.GONE);
        }
        guidance.setVisibility(mNavBannerImplement.isPFHasGuidance(true) ? View.VISIBLE : View.GONE);
        currentProgrammeNumberTV.setText(mCurrentProgrammeNumber);
        currentProgrammeFirstNameTV.setText(mCurrentProgrammeFirstName);
        bannerCategoryTv.setText(mDetailBannerCategory);
        //xian
        showProgrammeSecondName(mCurrentProgrammeSecondName);
        currentProgrammeTimeTV.setText(mCurrentProgrammeTime);
        if (!TextUtils.isEmpty(mProgramTime)) {
            durationProgressPR.setVisibility(View.VISIBLE);
            durationProgressPR.setProgress(mDurationProgress);
            currentProgrammeDurationTV.setText(mProgramTime);
            if (mNavBannerImplement.isIPChannel()) {
                programmeTotalMinTV.setText(mNavBannerImplement.getIPProgramTimeMins());
            } else {
                programmeTotalMinTV.setText(mNavBannerImplement.getChannelDurationMin(true, mProgramTime));
            }
            currentProgrammeDurationTV.setVisibility(View.VISIBLE);
            programmeTotalMinTV.setVisibility(View.VISIBLE);
        } else {
            durationProgressPR.setVisibility(View.INVISIBLE);
            currentProgrammeDurationTV.setVisibility(View.INVISIBLE);
            programmeTotalMinTV.setVisibility(View.INVISIBLE);
        }
        initLogoOrChannelName();
        setTimeOut();
    }

    void showCurrentProgrammIndication() {
        programmeIndicationIV.setImageResource(R.drawable.icon_next_programme);
        programmeIndicationTextTv.setText(R.string.setup_next);
    }

    void showNextProgrammIndication() {
        programmeIndicationIV.setImageResource(R.drawable.icon_pre_programme);
        programmeIndicationTextTv.setText(R.string.str_uk_banner_indication_previous);
    }

    void showNextProgrammInfo() {
        isCurrentProgrmma = false;
        showNextProgrammIndication();
        setIconsVisibility();
        if (!TextUtils.isEmpty(mNextRating)) {
            currentProgrammeLabelRatingTV.setVisibility(View.VISIBLE);
            currentProgrammeLabelRatingTV.setText(mNextRating);
        } else {
            currentProgrammeLabelRatingTV.setVisibility(View.GONE);
        }
        guidance.setVisibility(mNavBannerImplement.isPFHasGuidance(false) ? View.VISIBLE : View.GONE);
        currentProgrammeNumberTV.setText(mCurrentProgrammeNumber);
        currentProgrammeFirstNameTV.setText(mNextProgrammeFirstName);
        bannerCategoryTv.setText(mNextCategory);
        showProgrammeSecondName(mNextProgrammeSecondName);
        currentProgrammeTimeTV.setText(mCurrentProgrammeTime);
        if (!TextUtils.isEmpty(mNextProgrammeDuration)) {
            currentProgrammeDurationTV.setText(mNextProgrammeDuration);
            if (mNavBannerImplement.isIPChannel()) {
                programmeTotalMinTV.setText(mNavBannerImplement.getIPNextProgramTimeMins());
            } else {
                programmeTotalMinTV.setText(mNavBannerImplement.getChannelDurationMin(false, mNextProgrammeDuration));
            }
            currentProgrammeDurationTV.setVisibility(View.VISIBLE);
            programmeTotalMinTV.setVisibility(View.VISIBLE);
        } else {
            currentProgrammeDurationTV.setVisibility(View.INVISIBLE);
            programmeTotalMinTV.setVisibility(View.INVISIBLE);
        }
        durationProgressPR.setVisibility(View.INVISIBLE);
        initLogoOrChannelName();
        setTimeOut();
    }

    void showProgrammeSecondName(String name) {
        if (TextUtils.isEmpty(name)) {
            currentProgrammeSecondNameTV.setText(null);
            currentProgrammeSecondNameTV.setVisibility(GONE);
        } else {
            currentProgrammeSecondNameTV.setText(name);
            currentProgrammeSecondNameTV.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean isCoExist(int componentID) {
        switch (componentID) {
            case NAV_COMP_ID_TWINKLE_DIALOG:
            case NAV_COMP_ID_CH_LIST:
            case NAV_COMP_ID_MENU_OPTION_DIALOG:
            case NAV_NATIVE_COMP_ID_FVP:
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

    private void getBannerSimpleInfoWithThead() {
        Disposable subscribe = Completable.create(emitter -> {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBannerInfoWithThead start get");
            mCurrentProgrammeNumber = mNavBannerImplement.getCurrentChannelNum();
            mCurrentProgrammeSecondName = mNavBannerImplement.getProgSecondaryTitle();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentProgrammeNumber=" + mCurrentProgrammeNumber);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentProgrammeSecondName=" + mCurrentProgrammeSecondName);
            mProgrammeIconString = mNavBannerImplement.getChannelLogo();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBannerInfoWithThead subscribe");
                currentProgrammeNumberTV.setText(mCurrentProgrammeNumber);
                showProgrammeSecondName(mCurrentProgrammeSecondName);
                initLogoOrChannelName();
            }, Throwable::printStackTrace);
        mDisposables.add(subscribe);
    }

    private void getBannerInfoWithThead() {
        Disposable subscribe = Completable.create(emitter -> {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBannerInfoWithThead start get");
            mProgramRating = mNavBannerImplement.getProgramRating();
            mNextRating = mNavBannerImplement.getNextProgramRating();
            mProgramTime = mNavBannerImplement.getProgramTime();
            mCurrentProgrammeNumber = mNavBannerImplement.getCurrentChannelNum();
            mCurrentProgrammeFirstName = mNavBannerImplement.getProgramTitle();
            mCurrentProgrammeSecondName = mNavBannerImplement.getProgSecondaryTitle();
            mCurrentProgrammeTime = mNavBannerImplement.getCurrentTime();
            mNextProgrammeFirstName = mNavBannerImplement.getNextProgramTitle();
            mNextProgrammeSecondName = mNavBannerImplement.getNextProgSecondaryTitle();
            mDurationProgress = mNavBannerImplement.getCurrentChannelProgress();
            //                mNextDurationProgress=mNavBannerImplement.getNextChannelProgress();
            mNextProgrammeDuration = mNavBannerImplement.getNextProgramTime();
            //                mNextProgDetail=mNavBannerImplement.getNextProgDetail();
            mNextCategory = mNavBannerImplement.getNextProgramCategory();

            mProgrammeIconString = mNavBannerImplement.getChannelLogo();
            //                mDetailBannerDis=mNavBannerImplement.getProgramDetails();
            mDetailBannerCategory = mNavBannerImplement.getProgramCategory();
            mCurrentChannelName = mNavBannerImplement.getCurrentChannelName();
            ttsCurrentChannel();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgramTime=" + mProgramTime);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentProgrammeNumber=" + mCurrentProgrammeNumber);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentProgrammeFirstName=" + mCurrentProgrammeFirstName);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentProgrammeSecondName=" + mCurrentProgrammeSecondName);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentProgrammeTime=" + mCurrentProgrammeTime);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextProgrammeFirstName=" + mNextProgrammeFirstName);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextProgrammeSecondName=" + mNextProgrammeSecondName);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mDurationProgress=" + mDurationProgress);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextProgrammeDuration=" + mNextProgrammeDuration);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextCategory=" + mNextCategory);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mDetailBannerCategory=" + mDetailBannerCategory);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgramRating=" + mProgramRating);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextRating=" + mNextRating);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentChannelName=" + mCurrentChannelName);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBannerInfoWithThead subscribe");
                setIconsVisibility();
                //xian
                if (!TextUtils.isEmpty(mProgramRating)) {
                    currentProgrammeLabelRatingTV.setVisibility(View.VISIBLE);
                    currentProgrammeLabelRatingTV.setText(mProgramRating);
                } else {
                    currentProgrammeLabelRatingTV.setVisibility(View.GONE);
                }
                guidance.setVisibility(mNavBannerImplement.isPFHasGuidance(true) ? View.VISIBLE : View.GONE);
                currentProgrammeNumberTV.setText(mCurrentProgrammeNumber);
                currentProgrammeFirstNameTV.setText(mCurrentProgrammeFirstName);
                bannerCategoryTv.setText(mDetailBannerCategory);
                showProgrammeSecondName(mCurrentProgrammeSecondName);
                currentProgrammeTimeTV.setText(mCurrentProgrammeTime);
                if (!TextUtils.isEmpty(mProgramTime)) {
                    durationProgressPR.setVisibility(View.VISIBLE);
                    durationProgressPR.setProgress(mDurationProgress);
                    currentProgrammeDurationTV.setText(mProgramTime);
                    if (mNavBannerImplement.isIPChannel()) {
                        programmeTotalMinTV.setText(mNavBannerImplement.getIPProgramTimeMins());
                    } else {
                        programmeTotalMinTV.setText(mNavBannerImplement.getChannelDurationMin(true, mProgramTime));
                    }
                    currentProgrammeDurationTV.setVisibility(View.VISIBLE);
                    programmeTotalMinTV.setVisibility(View.VISIBLE);
                } else {
                    durationProgressPR.setVisibility(View.INVISIBLE);
                    currentProgrammeDurationTV.setVisibility(View.INVISIBLE);
                    programmeTotalMinTV.setVisibility(View.INVISIBLE);
                }
                initLogoOrChannelName();
            }, Throwable::printStackTrace);
        mDisposables.add(subscribe);

        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //
        //                bannerLayout.post(new Runnable() {
        //                    @Override
        //                    public void run() {
        //
        //                    }
        //                });
        //            }
        //        }).start();
    }

    void initLogoOrChannelName() {
        mCurrentChannelName = mNavBannerImplement.getCurrentChannelName();
        if (!TextUtils.isEmpty(mProgrammeIconString)) {
            mProgrammeIconString = mProgrammeIconString.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,", "");
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgrammeIconString==" + mProgrammeIconString);
            byte[] bytes = null;
            try {
                bytes = Base64.decode(mProgrammeIconString, Base64.DEFAULT);
            } catch (Exception e) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "decode exception");
            }
            if (bytes != null) {
                currentProgrammeIconIV.setVisibility(View.VISIBLE);
                currentProgrammeIconIV.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        } else {
            currentProgrammeIconIV.setVisibility(View.INVISIBLE);
        }
        currentChannelName.setText(mCurrentChannelName);
        currentChannelName.setVisibility(View.VISIBLE);
    }

    public void updateProgramTime() {
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "try updateProgramTime," + isCurrentProgrmma);
                if (isCurrentProgrmma) {
                    mProgramTime = mNavBannerImplement.getProgramTime();
                    mProgramRating = mNavBannerImplement.getProgramRating();
                } else {
                    mNextProgrammeDuration = mNavBannerImplement.getNextProgramTime();
                    mNextRating = mNavBannerImplement.getNextProgramRating();
                }
                if (basicBannerAll.getVisibility() == VISIBLE) {
                    basicBannerAll.post(new Runnable() {

                        @Override
                        public void run() {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgramTime");
                            if (isCurrentProgrmma) {
                                if (!TextUtils.isEmpty(mProgramTime)) {
                                    durationProgressPR.setVisibility(View.VISIBLE);
                                    mDurationProgress = mNavBannerImplement.getCurrentChannelProgress();
                                    durationProgressPR.setProgress(mDurationProgress);
                                    currentProgrammeDurationTV.setText(mProgramTime);
                                    if (mNavBannerImplement.isIPChannel()) {
                                        programmeTotalMinTV.setText(mNavBannerImplement.getIPProgramTimeMins());
                                    } else {
                                        programmeTotalMinTV.setText(mNavBannerImplement.getChannelDurationMin(true, mProgramTime));
                                    }
                                    currentProgrammeDurationTV.setVisibility(View.VISIBLE);
                                    programmeTotalMinTV.setVisibility(View.VISIBLE);
                                } else {
                                    durationProgressPR.setVisibility(View.INVISIBLE);
                                    currentProgrammeDurationTV.setVisibility(View.INVISIBLE);
                                    programmeTotalMinTV.setVisibility(View.INVISIBLE);
                                }
                                if (!TextUtils.isEmpty(mProgramRating)) {
                                    currentProgrammeLabelRatingTV.setVisibility(View.VISIBLE);
                                    currentProgrammeLabelRatingTV.setText(mProgramRating);
                                } else {
                                    currentProgrammeLabelRatingTV.setVisibility(View.GONE);
                                }
                            } else {
                                if (!TextUtils.isEmpty(mNextProgrammeDuration)) {
                                    durationProgressPR.setVisibility(View.VISIBLE);
                                    mDurationProgress = mNavBannerImplement.getNextChannelProgress();
                                    durationProgressPR.setProgress(mDurationProgress);
                                    currentProgrammeDurationTV.setText(mNextProgrammeDuration);
                                    if (mNavBannerImplement.isIPChannel()) {
                                        programmeTotalMinTV.setText(mNavBannerImplement.getIPNextProgramTimeMins());
                                    } else {
                                        programmeTotalMinTV.setText(mNavBannerImplement.getChannelDurationMin(false, mNextProgrammeDuration));
                                    }
                                    currentProgrammeDurationTV.setVisibility(View.VISIBLE);
                                    programmeTotalMinTV.setVisibility(View.VISIBLE);
                                } else {
                                    currentProgrammeDurationTV.setVisibility(View.INVISIBLE);
                                    programmeTotalMinTV.setVisibility(View.INVISIBLE);
                                }
                                if (!TextUtils.isEmpty(mNextRating)) {
                                    currentProgrammeLabelRatingTV.setVisibility(View.VISIBLE);
                                    currentProgrammeLabelRatingTV.setText(mNextRating);
                                } else {
                                    currentProgrammeLabelRatingTV.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void hideBanner() {
        if (bannerLayout.getVisibility() == View.VISIBLE) {
            bannerLayout.setVisibility(View.INVISIBLE);
        }
    }

    void showSpecialInfo(int toastId) {
        if (disableShowBanner(false) || isFvpVisible) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialInfo start, can not show ukbanner.");
            return;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialInfo start. is3rd source=" + mNavIntegration.is3rdTVSource());
        if (mNavIntegration.is3rdTVSource()) {
            reset();
            return;
        } else if (!mNavIntegration.isCurrentSourceTv()) {
            showSimpleSource();
            return;
        }
        setVisibility(View.VISIBLE);
        basicBannerAll.setVisibility(View.VISIBLE);
        itmeTwoLayout.setVisibility(View.INVISIBLE);
        bannerSpecialInfoTV.setVisibility(View.VISIBLE);
        bannerSpecialInfoTV.setText(toastId);
        setSpecialInfo(toastId);
    }

    void showNoSignal() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showNoSignal");
        setVisibility(View.VISIBLE);
        basicBannerAll.setVisibility(View.VISIBLE);
        bannerSpecialInfoTV.setVisibility(View.VISIBLE);
        itmeTwoLayout.setVisibility(View.INVISIBLE);
        bannerSpecialInfoTV.setText(R.string.nav_no_signal);
        setSpecialInfo(R.string.nav_no_signal);
        mCurrentProgrammeNumber = mNavBannerImplement.getCurrentChannelNum();
        currentProgrammeNumberTV.setText(mCurrentProgrammeNumber);
        currentProgrammeNumberTV.setVisibility(View.VISIBLE);

        mProgrammeIconString = mNavBannerImplement.getChannelLogo();
        initLogoOrChannelName();
    }

    void setSpecialInfo(int toastId) {
        if (toastId == R.string.nav_please_scan_channels || toastId == R.string.nav_input_has_locked) {
            itemOneLayout.setVisibility(INVISIBLE);
        } else {
            getBannerSimpleInfoWithThead();
            itemOneLayout.setVisibility(VISIBLE);
        }
        if (toastId == R.string.nav_no_signal) {
            setIconsVisibility();
        } else if (toastId == R.string.nav_channel_has_locked || toastId == R.string.nav_program_has_locked) {
            setFavoriteVisibility();
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

    private final Handler navBannerHandler = new Handler(mContext.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "msg.what==" + msg.what);
            if (msg.what == TvCallbackConst.MSG_CB_BANNER_MSG && !mNavIntegration.is3rdTVSource()) {
                TvCallbackData specialMsgData = (TvCallbackData) msg.obj;
                if (BANNER_MSG_NAV == specialMsgData.param1) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in handleMessage BANNER_MSG_NAV value=== "
                        + specialMsgData.param2);
                    switch (specialMsgData.param2) {
                        case BANNER_MSG_NAV_BEFORE_SVC:
                            reset();
                            break;
                        case BANNER_MSG_NAV_UNLOCK:
                            isSpecialState = false;
                            isHasLocked = false;
                            if (bannerSpecialInfoTV.getVisibility() == VISIBLE) {
                                bannerSpecialInfoTV.setVisibility(GONE);
                                bannerSpecialInfoTV.setText(null);
                                showBasicBanner(false);
                            }
                            break;
                        case SPECIAL_SCRAMBLED_VIDEO_NO_AUDIO:
                        case SPECIAL_SCRAMBLED_AUDIO_VIDEO:
                        case SPECIAL_SCRAMBLED_VIDEO_CLEAR_AUDIO:
                        case SPECIAL_SCRAMBLED_AUDIO_NO_VIDEO:
                            isHasChannel = true;
                            break;
                        case SPECIAL_EMPTY_SPECIAFIED_CH_LIST:
                            isSpecialState = true;
                            isHasChannel = true;
                            isHasLocked = false;
                            if (mNavBannerImplement.isIPChannel()) {
                                break;
                            }
                            showBasicBanner(false);
                            break;
                        //                              case SPECIAL_NO_AUDIO_VIDEO:
                        //                                  isHasSignal=true;
                        //                                  isHasChannel=true;
                        //                                  isHasLocked=false;
                        //                                  if(mNavBannerImplement.isIPChannel()){
                        //                                      break;
                        //                                  }
                        //                                  if(ComponentsManager.getNativeActiveCompId() != NavBasic.NAV_NATIVE_COMP_ID_HBBTV){
                        //                                      showSpecialInfo(R.string.nav_No_Audio_and_Video);
                        //                                  }
                        //                                    break;
                        case SPECIAL_NO_SIGNAL:
                            isHasSignal = false;
                            isHasChannel = true;
                            isHasLocked = false;
                            if (mNavBannerImplement.isIPChannel()) {
                                break;
                            }
                            showBasicBanner(false);
                            break;
                        case SPECIAL_CHANNEL_LOCK:
                            mLockInfoId = R.string.nav_channel_has_locked;
                            showSpecialInfoAfterMsg(mLockInfoId);
                            break;
                        case SPECIAL_PROGRAM_LOCK:
                            mLockInfoId = R.string.nav_program_has_locked;
                            showSpecialInfoAfterMsg(mLockInfoId);
                            break;
                        case SPECIAL_INPUT_LOCK:
                            mLockInfoId = R.string.nav_input_has_locked;
                            showSpecialInfoAfterMsg(mLockInfoId);
                            break;
                        case SPECIAL_NO_CHANNEL:
                            //isHasSignal=true;
                            //isHasChannel=false;
                            //isHasLocked=false;
                            //showSpecialInfo(R.string.nav_please_scan_channels);
                        case SPECIAL_RETRIVING:
                        case SPECIAL_NO_SUPPORT:
                            isHasSignal = true;
                            isHasChannel = false;
                            isHasLocked = false;
                            showSpecialInfo(R.string.nav_please_scan_channels);
                            break;
                        case SPECIAL_HIDDEN_CHANNEL:
                            isHasSignal = true;
                            isHasChannel = true;
                            if (!mNavBannerImplement.isIPChannel()) {
                                showSpecialInfo(R.string.nav_hidden_channel);
                            }
                            break;

                        case SPECIAL_STATUS_AUDIO_ONLY:
                            isHasSignal = true;
                            isHasLocked = false;
                            if (CommonIntegration.getInstance().getCurChInfo() == null) {
                                isHasChannel = false;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "the current has no channel!!!!!!!!!!!!!!!");
                                break;
                            }
                            if (mNavBannerImplement.isIPChannel()) {
                                break;
                            }
                            showBasicBanner(false);
                            break;
                        case SPECIAL_STATUS_VIDEO_ONLY:
                        case SPECIAL_STATUS_AUDIO_VIDEO:
                            isHasSignal = true;
                            isHasChannel = true;
                            isHasLocked = false;
                            showBasicBanner(false);
                            break;
                        case TV_STATE_NORMAL:
                            if (specialType == SPECIAL_NO_SIGNAL) {
                                specialType = -1;
                            }
                            break;
                        default:
                            break;
                    }
                } else if (BANNER_MSG_ITEM == specialMsgData.param1) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in handleMessage BANNER_MSG_ITEM value=== "
                            + specialMsgData.param2);
                    switch (specialMsgData.param2) {
                        case BANNER_MSG_ITM_CURRENT_PROGRAM_TITLE:
                            if (isCurrentProgrmma && basicBannerAll.getVisibility() == VISIBLE) {
                                mCurrentProgrammeFirstName = mNavBannerImplement.getProgramTitle();
                                mCurrentProgrammeSecondName = mNavBannerImplement.getProgSecondaryTitle();
                                currentProgrammeFirstNameTV.setText(mCurrentProgrammeFirstName);
                                showProgrammeSecondName(mCurrentProgrammeSecondName);
                            }
                            break;
                        case BANNER_MSG_ITM_CURRENT_PROGRAM_TIME:
                            if (isCurrentProgrmma && basicBannerAll.getVisibility() == VISIBLE) {
                                updateProgramTime();
                            }
                            break;
                        case BANNER_MSG_ITM_CURRENT_PROGRAM_DETAIL:
                            break;
                        case BANNER_MSG_ITM_CURRENT_PROGRAM_CATEGORY:
                            if (isCurrentProgrmma && basicBannerAll.getVisibility() == VISIBLE) {
                                mDetailBannerCategory = mNavBannerImplement.getProgramCategory();
                                bannerCategoryTv.setText(mDetailBannerCategory);
                            }
                            break;
                        case BANNER_MSG_ITM_NEXT_PROGRAM_TITLE:
                            if (!isCurrentProgrmma && basicBannerAll.getVisibility() == VISIBLE) {
                                mNextProgrammeFirstName = mNavBannerImplement.getNextProgramTitle();
                                mNextProgrammeSecondName = mNavBannerImplement.getNextProgSecondaryTitle();
                                currentProgrammeFirstNameTV.setText(mNextProgrammeFirstName);
                                showProgrammeSecondName(mNextProgrammeSecondName);
                            }
                            break;
                        case BANNER_MSG_ITM_NEXT_PROGRAM_TIME:
                            if (!isCurrentProgrmma && basicBannerAll.getVisibility() == VISIBLE) {
                                updateProgramTime();
                            }
                            break;
                        case BANNER_MSG_ITM_NEXT_PROGRAM_CATEGORY:
                            if (!isCurrentProgrmma && basicBannerAll.getVisibility() == VISIBLE) {
                                mNextCategory = mNavBannerImplement.getNextProgramCategory();
                                bannerCategoryTv.setText(mNextCategory);
                            }
                            break;
                        case BANNNER_MSG_SECOND_TITLE_CHNAGE:
                        case BANNNER_MSG_NEXT_SECOND_TITLE_CHNAGE:
                        case BANNNER_MSG_LOGO_CHNAGE:
                        case BANNER_MSG_ITM_CAPTION:
                        case BANNER_MSG_ITM_CAPTION_ICON_CHANGE:
                        case BANNNER_MSG_ITEM_IPTS_NAME:
                            showBasicBanner(false);
                            break;
                        //                       case BANNER_MSG_ITM_AUDIO_INFO_CHANGE:
                        case BANNNER_MSG_ITEM_ATMOS_CHNAGE:
                            if (mNavIntegration.isCHChanging()) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "No need to update dobly info in changing channel.");
                                break;
                            }
                            if (mNavIntegration.isCurrentSourceHDMI() && isVisible()) {
                                showDoblyIconOrDts();
                                startTimeout(NAV_TIMEOUT_5);
                            } else {
                                updateDoblyLogo();
                            }
                            break;
                        case BANNNER_MSG_ITEM_IPTS_RSLT_CHANGE:
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BANNNER_MSG_ITEM_IPTS_RSLT_CHANGE");
                            if (!mNavIntegration.isCurrentSourceTv()) {
                                showSimpleSource();
                                navBannerHandler.removeMessages(MSG_SHOW_SOURCE_BANNER_TIMING);
                                navBannerHandler.sendEmptyMessage(MSG_SHOW_SOURCE_BANNER_TIMING);
                            }
                            break;
                        default:
                            break;
                    }
                }
            } else if (msg.what == MSG_TTS_SPEAK_DELAY) {
                String ttsStr = (String) msg.obj;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in MSG_TTS_SPEAK_DELAY, ttsStr==" + ttsStr);
                ttsUtil.speak(ttsStr, TextToSpeechUtil.QUEUE_ADD);
            } else if (msg.what == MSG_NUMBER_KEY) {
                turnCHByNumKey();
            } else if (msg.what == MSG_SHOW_SOURCE_BANNER_TIMING) {
                changeSourceBannerTiming();
            } else if (msg.what == MSG_TIME_OUT) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "case MSG_TIME_OUT getVisibility()" + getVisibility());
                if (getVisibility() != View.GONE) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set banner gone.");
                    setVisibility(View.GONE);
                }
            } else if (msg.what == TvCallbackConst.MSG_CB_CI_MSG) {
                TvCallbackData specialMsgData = (TvCallbackData) msg.obj;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "MSG_CB_CI_MSG MTKTV_CI_BEFORE_SVC_CHANGE_SILENTLY :"
                        + specialMsgData.param2);
                if (specialMsgData.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_BEFORE_SVC_CHANGE_SILENTLY) {
                    isHostQuietTuneStatus = true;
                }
            }
        }

        private void showSpecialInfoAfterMsg(int lockInfoId) {
            isHasSignal = true;
            isHasChannel = true;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialInfoAfterMsg isHasLocked=true");
            isHasLocked = true;
            showSpecialInfo(mLockInfoId);
        }
    };

    private void changeSourceBannerTiming() {
        mDisposables.add(
            Single.create((SingleOnSubscribe<String>) emitter -> {
                if (StateDvrPlayback.getInstance() != null
                    && StateDvrPlayback.getInstance().isRunning()) {
                    emitter.onError(new Throwable("Dvr is running and not get resolution."));
                    return;
                }
                String value = mNavBannerImplement.getInputResolution();
                emitter.onSuccess(value);
            })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    s -> mThirdMiddle.setText(s)
                    , Throwable::printStackTrace)
        );
    }

    @Override
    public void setVisibility(int visibility) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in UkBannerView setVisibility ==" + visibility);
        if (getVisibility() != View.VISIBLE && visibility == View.VISIBLE) {
            if (!DestroyApp.isCurActivityTkuiMainActivity()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "top activity not turnkey, return.");
                return;
            }
            if (!mNavIntegration.isCurrentSourceTv()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getIptsRslt mSimpleBanner.mThirdMiddle ==" + mThirdMiddle.getText());
                mThirdMiddle.setText(null);
                navBannerHandler.removeMessages(MSG_SHOW_SOURCE_BANNER_TIMING);
                navBannerHandler.sendEmptyMessageDelayed(MSG_SHOW_SOURCE_BANNER_TIMING, 500);
            }
        }
        if (visibility != VISIBLE) {
            mDisposables.clear();
        }
        super.setVisibility(visibility);
        setTimeOut();
    }

    @Override
    public boolean deinitView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deinitView");
        mDisposables.clear();
        TvCallbackHandler mBannerTvCallbackHandler = TvCallbackHandler.getInstance();
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
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_BANNER_MSG);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_CI_MSG);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_CONFIG);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_SCAN_NOTIFY);
        navBannerHandler.removeMessages(TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO);
        navBannerHandler.removeMessages(MSG_TIME_OUT);
        navBannerHandler.removeMessages(MSG_TTS_SPEAK_DELAY);
        navBannerHandler.removeMessages(MSG_NUMBER_KEY);
        navBannerHandler.removeMessages(MSG_SHOW_SOURCE_BANNER_TIMING);
        return super.deinitView();
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus statusID =" + statusID
            + ">>value=" + value);
        // TODO Auto-generated method stub
        switch (statusID) {

            case ComponentStatusListener.NAV_KEY_OCCUR:
                return;
            case ComponentStatusListener.NAV_CONTENT_ALLOWED:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "content allowed:specialType==" + specialType
                    + ",isSpecialState==" + isSpecialState);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Banner_NAV_CONTENT_ALLOWED content allowed");
                if (mNavIntegration.is3rdTVSource()) {
                    reset();
                    return;
                }
                int showFlagtemp = MtkTvPWDDialog.getInstance().PWDShow();
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
                    }
                } else {
                    isSpecialState = false;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSpecialState -----------  = " + isSpecialState);
                if (mNavBannerImplement.isShowBannerBar()) {
                    bSourceOnTune = true;
                    //                hideAllBanner();
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isShowBannerBar|false");
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "content allowed:bSourceOnTune==" + bSourceOnTune);
                if (DestroyApp.isCurActivityTkuiMainActivity()) {
                    //show(false, -1, false);
                    showBasicBanner(false);
                }
                break;
            case ComponentStatusListener.NAV_CHANNEL_CHANGED:
                //              requestIPMdsInfo();
                // fix CR:DTV00700642
                DetailTextReader.getInstance().resetCurPagenum();
                if (value == 0) {
                    if (DestroyApp.isCurActivityTkuiMainActivity()
                        && mNavIntegration.getChannelAllNumByAPI() > 0
                        && !EPGEuActivity.mIsEpgChannelChange
                        && !CommonIntegration.getInstance().isCHChanging()) { // DTV00617844
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "itv test  hide banner------.....");
                        showBasicBanner(false);
                    } else {
                        EPGEuActivity.mIsEpgChannelChange = false;
                        return;
                    }
                } else {
                    if (DestroyApp.isCurActivityTkuiMainActivity()
                        && mNavIntegration.getChannelAllNumByAPI() > 0
                        && !EPGEuActivity.mIsEpgChannelChange) { // DTV00617844
                        showBasicBanner(false);
                    } else {
                        EPGEuActivity.mIsEpgChannelChange = false;
                        return;
                    }
                }
                // }
                break;
            case ComponentStatusListener.NAV_COMPONENT_SHOW:
                break;
            case ComponentStatusListener.NAV_COMPONENT_HIDE:
                List<Integer> cpmIDs = ComponentsManager.getInstance()
                    .getCurrentActiveComps();
                boolean coexitComp = true;
                for (Integer id : cpmIDs) {
                    if (id != NAV_COMP_ID_PVR_TIMESHIFT && id != NAV_COMP_ID_POP
                        && id != NAV_COMP_ID_MUTE) {
                        coexitComp = false;
                        break;
                    }
                }
                boolean isComponentsShow = ComponentsManager.getInstance()
                    .isComponentsShow();
                boolean isCurTKMain = DestroyApp.isCurActivityTkuiMainActivity();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMPONENT_HIDE isComponentsShow ="
                    + isComponentsShow + "isCurTKMain =" + isCurTKMain);
                if (mNavBannerImplement != null && coexitComp && isCurTKMain
                    && value == NAV_COMP_ID_BANNER) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "ONLYFORCC updateComponentStatus setCCVisiable(true) ");
                    mNavBannerImplement.setCCVisiable(true);
                }
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
        boolean isHandler = false;
        switch (keyCode) {
            case KeyMap.KEYCODE_DPAD_LEFT:
                showCurrentProgrammInfo();
                break;
            case KeyMap.KEYCODE_DPAD_RIGHT:
                showNextProgrammInfo();
                break;
            case KeyMap.KEYCODE_MTKIR_INFO:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler KEYCODE_MTKIR_INFO start.");

                if (mNavIntegration.is3rdTVSource()) {
                    reset();
                }

                isHandler = true;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_INFO start getVisibility()=" + getVisibility());
                if (getVisibility() != View.VISIBLE) {
                    showBasicBanner(true);
                } else {
                    gotoPage();
                    navBannerHandler.removeMessages(MSG_TIME_OUT);
                    navBannerHandler.sendEmptyMessage(MSG_TIME_OUT);
                }
                break;
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
                isHandler = true;
                if (mNavIntegration.is3rdTVSource()
                    || !mNavIntegration.isCurrentSourceTv()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not handle digtal key.");
                    return true;
                }
                if (isSourceLockedOrEmptyChannel()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in onKeyHandler number key isCurrentSourceBlocked or no channel");
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
                isHandler = false;
                if (mNumputChangeChannel) {
                    cancelNumChangeChannel();
                }
                if (getVisibility() != View.GONE) {
                    navBannerHandler.removeMessages(MSG_TIME_OUT);
                    navBannerHandler.sendEmptyMessage(MSG_TIME_OUT);
                }
                break;
            case KeyMap.KEYCODE_DPAD_CENTER:
                if (mNumputChangeChannel) {
                    isHandler = true;
                    // mNavIntegration.singleRFScan(Float.valueOf(mSelectedChannelNumString));
                    navBannerHandler.removeMessages(MSG_NUMBER_KEY);
                    navBannerHandler.sendEmptyMessage(MSG_NUMBER_KEY);
                }
                break;
            case KeyMap.KEYCODE_PAGE_DOWN:
                isHandler = true;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_EJECT");
                if (mNavIntegration.isCurrentSourceTv()
                    && !mNavIntegration.isCurrentSourceBlockEx()
                    && (mNavIntegration.getChannelAllNumByAPI() > 0)
                    && !mNavIntegration.is3rdTVSource()) {
                    FavChannelManager.getInstance(mContext).favAddOrErase();
                    setFavoriteVisibility();
                    setTimeOut();
                }
                break;
            default:
                break;
        }
        return isHandler;
    }

    public void reset() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reset tv state for 3rd source.");
        //          audioScramebled = false;
        //          videoScramebled = false;
        //          noAudio = false;
        //          noVideo = false;
        isHasChannel = true;
        isHasSignal = true;
        isHasLocked = false;
        //          isFvpVisible = false;
        isSpecialState = false;
        specialType = -1;
        isHostQuietTuneStatus = false;

        mCurrentProgrammeNumber = null;
        mCurrentChannelName = null;
        mProgrammeIconString = null;

        resetUIView();
        mNavBannerImplement.reset();
    }

    private void resetUIView() {
        currentChannelName.setText("");
        bannerCategoryTv.setText("");
        currentProgrammeIconIV.setVisibility(View.INVISIBLE);
        currentProgrammeTimeTV.setText("");
        mFavoriteIcon.setVisibility(View.INVISIBLE);
        currentProgrammeNumberTV.setText("");
        currentProgrammeFirstNameTV.setText("");
        showProgrammeSecondName("");
        currentProgrammeDurationTV.setText("");
        programmeTotalMinTV.setText("");
        currentProgrammeLabelSubtitleTV.setVisibility(View.INVISIBLE);
        currentProgrammeLabelRatingTV.setText("");
        currentProgrammeLabelRatingTV.setVisibility(View.GONE);
        currentChannelName.setText("");
        simpleChannelNumberView.setText("");
        bannerSpecialInfoTV.setText("");
        mFirstLine.setText("");
        mDtsType.setText("");
        mChannelName.setText("");
        mCCView.setText("");
        mThirdMiddle.setText("");
        durationProgressPR.setProgress(0);
        durationProgressPR.setVisibility(View.INVISIBLE);
        guidance.setVisibility(View.INVISIBLE);
        mVisualImpairedIcon.setVisibility(View.GONE);
        mHearingImpairedIcon.setVisibility(View.GONE);
    }

    private void gotoPage() {
        if (!mNavIntegration.isCurrentSourceTv() || mNavIntegration.is3rdTVSource() ||
            TIFChannelManager.getInstance(mContext).getCurrChannelInfo() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "gotoPage is not tv source.");
            return;
        }

        String jsonCmd = "";
        if (isCurrentProgrmma) {
            jsonCmd = "{\"nownextType\":\"now\"}";
        } else {
            jsonCmd = "{\"nownextType\":\"next\"}";
        }
        mMtkTvHtmlAgentBase.goToPage(MtkTvHtmlAgentBase.HTML_AGENT_PAGE_EPG, jsonCmd);
    }

    private boolean isSourceLockedOrEmptyChannel() {
        boolean isSourceLocked = mNavIntegration.isCurrentSourceBlocked() &&
            (MtkTvPWDDialog.getInstance().PWDShow() == 0);

        boolean isEmptyChannel = mNavIntegration.isCurrentSourceTv() && (mNavIntegration
            .getChannelAllNumByAPI() <= 0);

        return isSourceLocked || isEmptyChannel;
    }

    private void sendTurnCHMsg(int keyCode) {
        inputChannelNum(keyCode);
        UKChannelListDialog channellist = (UKChannelListDialog) ComponentsManager.getInstance().getComponentById(NAV_COMP_ID_CH_LIST);
        if (channellist != null && channellist.isShowing()) {
            channellist.dismiss();
        }
        showSimpleBanner();
        setTimeOut();
        navBannerHandler.removeMessages(MSG_NUMBER_KEY);
        navBannerHandler.sendEmptyMessageDelayed(MSG_NUMBER_KEY, NAV_TIMEOUT_3);
    }

    void setTimeOut() {
        navBannerHandler.removeMessages(MSG_TIME_OUT);
        navBannerHandler.sendEmptyMessageDelayed(MSG_TIME_OUT, NAV_TIMEOUT_5);
    }

    void showSimpleBanner() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSimpleBanner");
        if (TextUtils.isEmpty(mSelectedChannelNumString)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
            return;
        }
        basicBannerAll.setVisibility(View.GONE);
        setVisibility(View.VISIBLE);
        mSelfLayout.setVisibility(View.INVISIBLE);
        bannerLayout.setVisibility(View.VISIBLE);
        simpleChannelNumberView.setVisibility(View.VISIBLE);
        simpleChannelNumberView.setText(mSelectedChannelNumString);
    }

    private void inputChannelNum(int keycode) {
        mNumputChangeChannel = true;
        if (keycode >= KeyMap.KEYCODE_0 && keycode <= KeyMap.KEYCODE_9) {
            // inputChannelNumStrBuffer.append("" + (keycode -
            // KeyMap.KEYCODE_0));
            if (mSelectedChannelNumString.indexOf("-") == -1) {
                mSelectedChannelNumString = mSelectedChannelNumString
                    + (keycode - KeyMap.KEYCODE_0);
                if (MarketRegionInfo.REGION_EU == MarketRegionInfo
                    .getCurrentMarketRegion()) {
                    if (mSelectedChannelNumString.length() <= 4) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "length() <= 4");
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
            } else if (mSelectedChannelNumString.indexOf("-") != -1) {
                mSelectedChannelNumString = mSelectedChannelNumString
                    + (keycode - KeyMap.KEYCODE_0);
                String[] channelNum = mSelectedChannelNumString.split("-");
                if (mSelectedChannelNumString.length() <= 9) {
                    mSelectedChannelNumString = channelNum[0] + "-"
                        + Integer.valueOf(channelNum[1]);
                } else {
                    mSelectedChannelNumString = channelNum[0]
                        + "-"
                        + Integer.valueOf(mSelectedChannelNumString
                        .substring(channelNum[0].length() + 1 + 1,
                            10));
                }
            }
        } else if ((KeyMap.KEYCODE_PERIOD == keycode)
            && (mSelectedChannelNumString.indexOf("-") == -1)) {
            // inputChannelNumStrBuffer.append("-");
            mSelectedChannelNumString = mSelectedChannelNumString + "-";
        } else if ((KeyMap.KEYCODE_PERIOD == keycode)
            && (mSelectedChannelNumString.indexOf("-") != -1)) {
            String[] numString = mSelectedChannelNumString.split("-");
            mSelectedChannelNumString = numString[0];
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSelectedChannelNumString=" + mSelectedChannelNumString);
    }

    private void turnCHByNumKey() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "turnCHByNumKey---->mSelectedChannelNumString="
            + mSelectedChannelNumString);
        int channelID = mNavIntegration
            .getChannelIDByBannerNum(mSelectedChannelNumString);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelID=" + channelID);
        MtkTvChannelInfoBase chanel = CommonIntegration.getInstance()
            .getChannelById(channelID);
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
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "INVALID_CHANNEL_ID");
            //show(false, -1, false);
        } else {
            if (!mNavIntegration.isCurrentSourceTv()) {
                mNavIntegration.iSetSourcetoTv(channelID);
                // mNavIntegration.selectChannelById(channelID);
                //                  mTuneChannelId = channelID;
                //                  mNeedChangeSource = true;
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
                        setVisibility(GONE);
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

    private boolean canTurnCHForFaker(MtkTvChannelInfoBase chanel) {
        if (!(mNavIntegration.isUSRegion() || mNavIntegration.isSARegion())) {
            return false;
        }
        boolean canTurnCHforFaker = mNavIntegration.checkChMask(chanel,
            CommonIntegration.CH_FAKE_MASK, CommonIntegration.CH_FAKE_VAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "canTurnCHforFaker=" + canTurnCHforFaker);
        return canTurnCHforFaker;
    }

    private boolean isHideCH(MtkTvChannelInfoBase chanel) {
        boolean isHideCH = !mNavIntegration.checkChMask(chanel,
            CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHideCH=" + isHideCH);
        return isHideCH;
    }

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

    public void handlerFVPMessage(int type, int message) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerFVPMessage, type=" + type + ", message=" + message);
        switch (type) {
            //           case 0:
            case 2:
                isFvpVisible = true;
                navBannerHandler.sendEmptyMessage(MSG_TIME_OUT);
                break;
            case 1:
                //           case 3:
                isFvpVisible = false;
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
                case MHEG5_MSG_DISABLE_BANNER:// disable banner
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

    private boolean disableShowBanner(boolean isHandleKey) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHandleKey=" + isHandleKey + ",mDisableBanner="
            + mDisableBanner + ",mIs3rdRunning=" + mIs3rdRunning);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isIPChannel=" + mNavBannerImplement.isIPChannel());
        if (isHandleKey && isMHeg5Showing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MHeg5 NativeComp is showing.");
            return false;
        }

        if (isMenuOptionShow()) {
            return true;
        }

        MtkTvHtmlAgentBase.HtmlAgentStatus status = mMtkTvHtmlAgentBase.getCurrStatus();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MtkTvHtmlAgentBase.HtmlAgentStatus status :" + status);
        if (status == MtkTvHtmlAgentBase.HtmlAgentStatus.HTML_AGENT_STATUS_ACTIVE) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MtkTvHtmlAgentBase.HtmlAgentStatus status active, and not show banner.");
            return true;
        }

        // && mIs3rdRunning) All must be true.
        if (mDisableBanner || (mIs3rdRunning && !mNavBannerImplement.isIPChannel()) || isNatvieCompShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NativeComp is showing,do not show bannerview!");
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
        List<Integer> currentComponentIDList = ComponentsManager.getInstance().getCurrentActiveComps();
        for (int currentComponentID : currentComponentIDList) {
            if ((currentComponentID == NavBasic.NAV_COMP_ID_CH_LIST)
                || currentComponentID == NavBasic.NAV_COMP_ID_FAV_LIST) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Can't show banner when ch-list and fav-list showing.");
                return true;
            }
        }
        return false;
    }

    private boolean isMHeg5Showing() {
        int getActiveCompId = ComponentsManager.getNativeActiveCompId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNativeActiveCompId=" + getActiveCompId);
        return getActiveCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5;
    }

    private boolean isNatvieCompShowing() {
        int getActiveCompId = ComponentsManager.getNativeActiveCompId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isNatvieCompShowing----->getActiveCompId="
            + getActiveCompId);
        return getActiveCompId == NavBasic.NAV_NATIVE_COMP_ID_FVP
            || getActiveCompId == NavBasic.NAV_COMP_ID_CH_LIST
            || (getActiveCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5 && mDisableBanner);
    }

    public void cancelNumChangeChannel() {
        mNumputChangeChannel = false;
        navBannerHandler.removeMessages(MSG_NUMBER_KEY);
        mSelectedChannelNumString = "";
    }

    public void changeChannelFavoriteMark() {
        if (mNavIntegration.isCurrentSourceTv()
            && !mNavIntegration.isCurrentSourceBlocked()) {
            showBasicBanner(false);
        }
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

    public void updateDoblyLogo() {
        if (getVisibility() == VISIBLE) {
            showDoblyLogo();
            setTimeOut();
        }
    }

    public void requestIPMdsInfo() {
        mDisposables.add(
            mNavBannerImplement.fetchProgramInfo()
                .subscribe(b -> {
                    if (b) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "requestIPMdsInfo success.");
                        getBannerInfoWithThead();
                    }
                }, Throwable::printStackTrace)
        );
    }
}
