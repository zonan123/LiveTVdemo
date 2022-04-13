package com.mediatek.wwtv.setting.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvContentRatingSystemInfo;
import android.app.AlarmManager;
import android.app.ProgressDialog;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.twoworlds.tv.MtkTvATSCCloseCaption;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvTimeFormat;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.parental.ContentRatingsParser;
import com.mediatek.wwtv.setting.parental.ContentRatingSystem;
import com.mediatek.wwtv.setting.parental.ContentRatingsManager;
import com.mediatek.wwtv.setting.parental.ParentalControlSettings;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.preferences.SettingsPreferenceScreen;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;

public class MenuConfigManager {
     private static final String TAG = "MenuConfigManager";

    private static MenuConfigManager mConfigManager;
    ProgressDialog  pdialog;
    public static final int MSG_SCAN_UNKNOW = 0x00000000;
    public static final int MSG_SCAN_COMPLETE = 0x00000001;
    public static final int MSG_SCAN_PROGRESS = 0x0000002;
    public static final int MSG_SCAN_CANCEL = 0x00000004;
    public static final int MSG_SCAN_ABORT = 0x00000008;
    public static final int TV_SYS_MASK_B = 2;//(1<<1)
    public static final int TV_SYS_MASK_D = 8;//(1<<3)
    public static final int TV_SYS_MASK_G = 64;//(1<<6)
    public static final int TV_SYS_MASK_I = 256;//(1<<8)
    public static final int TV_SYS_MASK_K = 1024;//(1<<10)
    public static final int TV_SYS_MASK_L = 4096;//(1<<12)
    public static final int TV_SYS_MASK_L_PRIME = 8192;//(1<<13)
	public static final int TV_SYS_MASK_M = 0x4000;//(1<<14)
	public static final int TV_SYS_MASK_N = 0x8000;//(1<<15)

    public static final int AUDIO_SYS_MASK_AM        = 1;//(1<<0)
    public static final int AUDIO_SYS_MASK_FM_MONO   = 2;//(1<<1)
    public static final int AUDIO_SYS_MASK_FM_A2     = 8;//(1<<3)
    public static final int AUDIO_SYS_MASK_FM_A2_DK1 = 16;//(1<<4)
    public static final int AUDIO_SYS_MASK_FM_A2_DK2 = 32;//(1<<5)
    public static final int AUDIO_SYS_MASK_NICAM     = 128;//(1<<7)
	public static final int AUDIO_SYS_MASK_BTSC      = 256;//(1<<8)
    public static final int SCC_AUD_MTS_UNKNOWN = 0;
    public static final int SCC_AUD_MTS_MONO = 1;
    public static final int SCC_AUD_MTS_STEREO = 2;
    public static final int SCC_AUD_MTS_SUB_LANG = 3;
    public static final int SCC_AUD_MTS_DUAL1 = 4;
    public static final int SCC_AUD_MTS_DUAL2 = 5;
    public static final int SCC_AUD_MTS_NICAM_MONO = 6;
    public static final int SCC_AUD_MTS_NICAM_STEREO = 7;
    public static final int SCC_AUD_MTS_NICAM_DUAL1 = 8;
    public static final int SCC_AUD_MTS_NICAM_DUAL2 = 9;
    public static final int SCC_AUD_MTS_FM_MONO = 10;
    public static final int SCC_AUD_MTS_FM_STEREO = 11;

    public static final int EDIT_CHANNEL_LENGTH = 16;
    public static boolean PICTURE_MODE_dOVI = false;

    private final TVContent mTV;
    LanguageUtil osdLanguage; // set language
    SaveValue save; // save some UI value
    Action mItem;
    static final int minValue = 0;
    static final int maxValue = 0;
    static final int defaultValue = 0;
    static final int tvOptionValue = 0;
    private final Context mContext;
    private final MtkTvConfig mTvConfig;
    private final  com.mediatek.twoworlds.tv.MtkTvAVModeBase mtkMode;
    //cts verify start
    private TvInputManager mTvInputManager = null;
    private AsyncOSDLan att;
    private final List<ContentRatingSystem> mContentRatingSystems = new ArrayList<>();
    ParentalControlSettings mParentalControlSettings;
    ContentRatingsManager mContentRatingsManager = null;
    //cts verify end

    private MenuConfigManager(Context context) {
        mContext = context;
        mTV = TVContent.getInstance(context);
        save = SaveValue.getInstance(context);
        mTvConfig = MtkTvConfig.getInstance();
        mtkMode=new com.mediatek.twoworlds.tv.MtkTvAVModeBase();
        if(mScreenMode.isEmpty()){
            init();
        }
    }

    private void init() {
        String[] array = mContext.getResources().getStringArray(R.array.screen_mode_array_us);
        for(int i=0;i< array.length;i++){
            mScreenMode.put(array[i], i);
            mScreenModeReverse.put(i,array[i]);
        }

    }
    public void reloadScreenModes(){
        String[] array = mContext.getResources().getStringArray(R.array.screen_mode_array_us);
        for(int i=0;i< array.length;i++){
            mScreenMode.put(array[i], i);
            mScreenModeReverse.put(i,array[i]);
        }
    }

    public static synchronized MenuConfigManager getInstance(Context context) {
        if (mConfigManager == null) {
            mConfigManager = new MenuConfigManager(context.getApplicationContext());
        }
        return mConfigManager;
    }
    public static final String POWER_SETTING_CONFIG_VALUE="g_record_setting_power_setting";
    public static final String ACTION_SYNC_CONFIG_VALUE = "com.android.tv.settings.SYNC_CONFIG_VALUE";
    /** dobly vision */
    public static final String NOTIFY_SWITCH = MtkTvConfigTypeBase.CFG_VIDEO_DOVI_USER_SWITCH;// MtkTvConfigType.CFG_MISC_DOVI_NOTIFY_SWITCH;
    public static final String RESET_SETTING = MtkTvConfigTypeBase.CFG_VIDEO_DOVI_RESET_PIC_SETTING;// MtkTvConfigType.CFG_MISC_DOVI_RESET_PIC_SETTING;

    /** Video */
    public static final String PICTURE_MODE = MtkTvConfigType.CFG_VIDEO_PIC_MODE;
    public static final String BACKLIGHT = MtkTvConfigType.CFG_DISP_DISP_DISP_BACK_LIGHT;// CFG_DISP_DISP_DISP_BACK_LIGHT;
    public static final String BRIGHTNESS = MtkTvConfigType.CFG_VIDEO_VID_BRIGHTNESS;
    public static final String CONTRAST = MtkTvConfigType.CFG_VIDEO_VID_CONTRAST;
    public static final String SATURATION = MtkTvConfigType.CFG_VIDEO_VID_SAT;
    public static final String HUE = MtkTvConfigType.CFG_VIDEO_VID_HUE;
    public static final String SHARPNESS = MtkTvConfigType.CFG_VIDEO_VID_SHP;
    public static final String GAMMA = MtkTvConfigType.CFG_DISP_DISP_DISP_GAMMA;
    public static final String COLOR_TEMPERATURE = MtkTvConfigType.CFG_VIDEO_CLR_TEMP;
    public static final String SETUP_DIVX_REGISTRATION = "SETUP_divx_registration";
    public static final String SETUP_DIVX_DEACTIVATION = "SETUP_divx_deactivation";
    public static final String SETUP_VERSION_INFO = "SETUP_version_info";
    public static final String SETUP_LICENSE_INFO = "SETUP_license_info";
    public static final String COLOR_G_R = MtkTvConfigType.CFG_VIDEO_CLR_GAIN_R;
    public static final String COLOR_G_G = MtkTvConfigType.CFG_VIDEO_CLR_GAIN_G;
    public static final String COLOR_G_B = MtkTvConfigType.CFG_VIDEO_CLR_GAIN_B;
    public static final String DNR = MtkTvConfigType.CFG_VIDEO_VID_NR;
    public static final String MPEG_NR = MtkTvConfigType.CFG_VIDEO_VID_MPEG_NR;
    public static final String ADAPTIVE_LUMA_CONTROL = MtkTvConfigType.CFG_VIDEO_VID_LUMA;
    public static final String ADAPTIVE_LOCAL_CONTRAST_CONTROL = MtkTvConfigType.CFG_VIDEO_VID_LOCAL_CONTRAST;
    public static final String FLESH_TONE = MtkTvConfigType.CFG_VIDEO_VID_FLESH_TONE;
    public static final String DI_FILM_MODE = MtkTvConfigType.CFG_VIDEO_VID_DI_FILM_MODE;
    public static final String BLUE_STRETCH = MtkTvConfigType.CFG_VIDEO_VID_BLUE_STRETCH;
    public static final String GAME_MODE = MtkTvConfigType.CFG_VIDEO_VID_GAME_MODE;
    public static final String PQ_SPLIT_SCREEN_DEMO_MODE = MtkTvConfigType.CFG_VIDEO_VID_PQ_DEMO;// MtkTvConfigType.CFG_VIDEO_PQ_DEMO;
    public static final String USER_PREFERENCE = "TODO:MtkTvConfigTypeBase.CFG_MISC_CI_AMMI_PRIORITY_VALUE";
    public static final String BLACK_BAR_DETECTION = MtkTvConfigType.CFG_VIDEO_VID_BLACK_BAR_DETECT;
    public static final String SUPER_RESOLUTION = MtkTvConfigType.CFG_VIDEO_VID_SUPER_RESOLUTION;
    public static final String GRAPHIC = MtkTvConfigType.CFG_VIDEO_VID_SUPER_RESOLUTION;
    public static final String FV_VIDEO_VID_XVYCC = MtkTvConfigType.CFG_VIDEO_VID_XVYCC;
    public static final String CFG_MENU_XVYCC = "g_menu__xvYCC";
    public static final String BLUE_LIGHT = MtkTvConfigTypeBase.CFG_VIDEO_VID_BLUE_LIGHT;
    public static final String VIDEO_HDR = MtkTvConfigTypeBase.CFG_VIDEO_VID_HDR;
    public static final String NO_SIGNAL_AUTO_POWER_OFF = "no_signal_auto_power_off";

    //CFG_GRP_SUBTITLE_PREFIX
    public static final String SUBTITLE_GROUP = "SUBTITLE_GROUP";
    public static final String ANALOG_SUBTITLE = MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_ENABLED;
    public static final String DIGITAL_SUBTITLE_LANG = MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_LANG;
    public static final String DIGITAL_SUBTITLE_LANG_2ND = MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_LANG_2ND;
    public static final String DIGITAL_SUBTITLE_LANG_ENABLE = MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_ENABLED_EX;
    public static final String DIGITAL_SUBTITLE_LANG_ENABLE_2ND = MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_ENABLED_EX_2ND;
    public static final String SUBTITLE_TYPE = MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_ATTR;
    public static final String SUBTITLE_TRACKS = "preference_subtitle_track";
    // Config for vga input source
    public static final String VGA_MODE = MtkTvConfigType.CFG_VIDEO_VID_VGA_MODE;

    // HDMI mode
    public static final String HDMI_MODE = MtkTvConfigType.CFG_VIDEO_VID_HDMI_MODE;

    // Configs for VGA mode
    public static final String HPOSITION = MtkTvConfigType.CFG_VGA_VGA_POS_H;
    public static final String VPOSITION = MtkTvConfigType.CFG_VGA_VGA_POS_V;
    public static final String PHASE = MtkTvConfigType.CFG_VGA_VGA_PHASE;
    public static final String CLOCK = MtkTvConfigType.CFG_VGA_VGA_CLOCK;
    // VGA
    public static final String VGA = "SUB_VGA";
    // auto adjust
    public static final String AUTO_ADJUST = "SUB_AUTO_ADJUST";

    public static final String VIDEO_3D = "g_video__vid_3d_item";
    public static final String VIDEO_3D_MODE = MtkTvConfigType.CFG_VIDEO_VID_3D_MODE;// no
    public static final String VIDEO_3D_NAV = MtkTvConfigType.CFG_VIDEO_VID_3D_NAV_AUTO;
    public static final String VIDEO_3D_3T2 = MtkTvConfigType.CFG_VIDEO_VID_3D_TO_2D;
    public static final String VIDEO_3D_FIELD = MtkTvConfigType.CFG_VIDEO_VID_3D_FLD_DEPTH;
    public static final String VIDEO_3D_PROTRUDE = MtkTvConfigType.CFG_VIDEO_VID_3D_PROTRUDEN;
    public static final String VIDEO_3D_DISTANCE = MtkTvConfigType.CFG_VIDEO_VID_3D_DISTANCE;
    public static final String VIDEO_3D_IMG_SFTY = MtkTvConfigType.CFG_VIDEO_VID_3D_IMG_SFTY;
    public static final String VIDEO_3D_LF = MtkTvConfigType.CFG_VIDEO_VID_3D_LR_SWITCH;
    public static final String VIDEO_3D_OSD_DEPTH = MtkTvConfigType.CFG_VIDEO_VID_3D_OSD_DEPTH;
    /**
     * CECFUN
     */
    public static final String CEC_CEC_FUN = MtkTvConfigType.CFG_CEC_CEC_FUNC;
    public static final int CEC_FUNTION_ON = MtkTvConfigType.CEC_FUNC_ON;
    public static final int CEC_FUNTION_OFF = MtkTvConfigType.CEC_FUNC_OFF;
    public static final String CEC_SAC_OFUN = MtkTvConfigType.CFG_CEC_CEC_SAC_FUNC;
    public static final String CEC_AUTO_ON = MtkTvConfigType.CFG_CEC_CEC_AUTO_ON;
    public static final String CEC_AUTO_OFF = MtkTvConfigType.CFG_CEC_CEC_AUTO_OFF;
    public static final String CEC_DEVICE_DISCOVERY = "cec_device";

    // MJC
    public static final String MJC = "UNDEFINE_MJC";
    // EFFECT
    public static final String EFFECT = MtkTvConfigType.CFG_VIDEO_VID_MJC_EFFECT;
    public static final String MENU_MJC_MODE = MtkTvConfigType.CFG_VIDEO_VID_MJC_MODE;
    public static final String DEMO = "UNDEFINE_DEMO";
    // DEMO PARTITION
    public static final String DEMO_PARTITION = MtkTvConfigType.CFG_VIDEO_VID_MJC_DEMO;
    public static final String CFG_VIDEO_VID_MJC_DEMO_STATUS = "g_video__vid_mjc_status";

    // EU frequent
    public static final String FREQUENCY_LEN_6 = "frequency_length_six";
    public static final String TUNER_MODE_CN_USER_SET = MtkTvConfigType.CFG_BS_BS_USER_SRC;

    /** factory */
    /*
     * factory_video
     */
    // AUTO_COLOR
    public static final String FV_AUTOCOLOR = "SUB_FV_AUTOCOLOR";
    // COLOR_TEMPERATURE
    public static final String FV_COLORTEMPERATURE = "SUB_FV_COLORTEMPERATURE";
    // COLOR_TEMPERATURE_CHILD
    public static final String FV_COLORTEMPERATURECHILD = MtkTvConfigType.CFG_VIDEO_CLR_TEMP;
    // H.Position
    public static final String FV_HPOSITION = MtkTvConfigType.CFG_VIDEO_VID_POS_H;
    // V.Position
    public static final String FV_VPOSITION = MtkTvConfigType.CFG_VIDEO_VID_POS_V;
    // AUTO PHASE
    public static final String FV_AUTOPHASE = "SUB_FV_AUTOPHASE";
    // PHASE
    public static final String FV_VGA_PHASE = MtkTvConfigType.CFG_VGA_VGA_PHASE;
    public static final String FV_YPBPR_PHASE = MtkTvConfigType.CFG_VIDEO_VID_YPBPR_PHASE;
    // DIMA
    public static final String FV_DIMA = MtkTvConfigType.CFG_VIDEO_VID_DI_MA;
    // DIEDGE
    public static final String FV_DIEDGE = MtkTvConfigType.CFG_VIDEO_VID_DI_EDGE;
    // WCG
    public static final String FV_WCG = MtkTvConfigType.CFG_VIDEO_VID_WCG;
    // FLIP
    public static final String FV_FLIP = MtkTvConfigType.CFG_MISC_EX_FLIP;// TODO
    // MIRROR
    public static final String FV_MIRROR = MtkTvConfigType.CFG_MISC_EX_MIRROR;// TODO
    // Local dimming
    public static final String FV_LOCAL_DIMMING = MtkTvConfigType.CFG_MISC_EX_DIMMING;// TODO
    // factory_video_COLOR TEMPERATURE
    // r gain
    public static final String FV_COLOR_G_R = MtkTvConfigType.CFG_VIDEO_CLR_GAIN_R;
    // g gain
    public static final String FV_COLOR_G_G = MtkTvConfigType.CFG_VIDEO_CLR_GAIN_G;
    // b gain
    public static final String FV_COLOR_G_B = MtkTvConfigType.CFG_VIDEO_CLR_GAIN_B;
    // r offset
    public static final String FV_COLOR_O_R = MtkTvConfigType.CFG_VIDEO_CLR_OFFSET_R;
    // g offset
    public static final String FV_COLOR_O_G = MtkTvConfigType.CFG_VIDEO_CLR_OFFSET_G;
    // b offset
    public static final String FV_COLOR_O_B = MtkTvConfigType.CFG_VIDEO_CLR_OFFSET_B;

    /*
     * factory_audio
     */
    // DOLBY BANNER
    public static final String FA_DOLBYBANNER = MtkTvConfigType.CFG_AUD_DOLBY_CERT_MODE;
    // COMPRESSION
    public static final String FA_COMPRESSION = MtkTvConfigType.CFG_AUD_DOLBY_CMPSS;
    // COMPRESSION FACTOR
    public static final String FA_COMPRESSIONFACTOR = MtkTvConfigType.CFG_AUD_DOLBY_DRC;
    // MTS SYSTEM
    public static final String FA_MTS_SYSTEM = "SUB_FA_MTS_SYSTEM";
    // A2 SYSTEM
    public static final String FA_A2SYSTEM = "SUB_FA_A2SYSTEM";
    // PAL SYSTEM
    public static final String FA_PALSYSTEM = "SUB_FA_PALSYSTEM";
    // EU SYSTEM
    public static final String FA_EUSYSTEM = "SUB_FA_EUSYSTEM";
    // LATENCY
    public static final String FA_LATENCY = MtkTvConfigType.CFG_AUD_AUD_LATENCY;
    /*
     * factory_audio_MTS_system
     */
    // NUMBERS OF CHECK
    public static final String FAMTS_NUMBERSOFCHECK = MtkTvConfigType.CFG_MISC_EX_NUM_OF_CHECK;
    // NUMBERS OF Pilot
    public static final String FAMTS_NUMBERSOFPILOT = MtkTvConfigType.CFG_MISC_EX_NUM_OF_PILOT;
    // NUMBERS OF PILOT_THRESHOLD_HIGH
    public static final String FAMTS_PILOT_THRESHOLD_HIGH = MtkTvConfigType.CFG_MISC_EX_PILOT_THRESHOD_HIGH;
    // NUMBERS OF PILOT_THRESHOLD_LOW
    public static final String FAMTS_PILOT_THRESHOLD_LOW = MtkTvConfigType.CFG_MISC_EX_PILOT_THRESHOD_LOW;
    // NUMBERS OF FAMTS_NUMBERSOFSAP
    public static final String FAMTS_NUMBERSOFSAP = MtkTvConfigType.CFG_MISC_EX_NUM_OF_SAP;
    // NUMBERS OF FAMTS_SAP_THRESHOLD_HIGH
    public static final String FAMTS_SAP_THRESHOLD_HIGH = MtkTvConfigType.CFG_MISC_EX_SAP_THRESHOLD_HIGH;
    // NUMBERS OF FAMTS_SAP_THRESHOLD_HIGH
    public static final String FAMTS_SAP_THRESHOLD_LOW = MtkTvConfigType.CFG_MISC_EX_SAP_THRESHOLD_LOW;

    // NUMBERS OF HIGH deviation mode
    public static final String FAMTS_HIGH_DEVIATION_MODE = MtkTvConfigType.CFG_MISC_EX_HIGH_DEVIATION_MODE;

    // NUMBERS OF ARRIER_SHIFT_FUNCTION
    public static final String FAMTS_CARRIER_SHIFT_FUNCTION = MtkTvConfigType.CFG_MISC_EX_CARRIER_SHIFT_FUNCTION;

    // NUMBERS OF FM Stauration mute
    public static final String FAMTS_FM_STAURATION_MODE = MtkTvConfigType.CFG_MISC_EX_FM_SATURATION_MUTE;
    // NUMBERS OF FM Carrier mute
    public static final String FAMTS_FM_CARRIER_MUTE_MODE = MtkTvConfigType.CFG_MISC_EX_FM_CARRIER_MUTE_MODE;
    // NUMBERS OF FM Carrier mute threshold high
    public static final String FAMTS_FM_CARRIER_MUTE_THRESHOLD_HIGH = MtkTvConfigType.CFG_MISC_EX_FM_CARRIER_MUTE_THRESHOLD_HIGH;
    // NUMBERS OF FM Carrier mute threshold low
    public static final String FAMTS_FM_CARRIER_MUTE_THRESHOLD_LOW = MtkTvConfigType.CFG_MISC_EX_FM_CARRIER_MUTE_THRESHOLD_LOW;

    // NUMBERS OF Mono Stero Fine Tune Volume
    public static final String FAMTS_MONO_STERO_FINE_TUNE_VOLUME = MtkTvConfigType.CFG_MISC_EX_MONO_STERO_FINE_TUNE_VOLUME;
    // NUMBERS OF Mono Stero Fine Tune Volume
    public static final String FAMTS_SAP_FINE_TUNE_VOLUME = MtkTvConfigType.CFG_MISC_EX_SAP_FINE_TUNE_VOLUME;

    /*
     * factory_audio_A2_system
     */
    // NUMBERS OF CHECK
    public static final String FAA2_NUMBERSOFCHECK = MtkTvConfigType.CFG_MISC_EX_A2_SYS_NUM_CHECK;
    // NUMBERS OF DOUBLE
    public static final String FAA2_NUMBERSOFDOUBLE = MtkTvConfigType.CFG_MISC_EX_A2_SYS_NUM_DOUBLE;
    // MONO WEIGHT
    public static final String FAA2_MONOWEIGHT = MtkTvConfigType.CFG_MISC_EX_A2_SYS_NOMO_WIGHT;
    // STEREO WEIGHT
    public static final String FAA2_STEREOWEIGHT = MtkTvConfigType.CFG_MISC_EX_A2_SYS_STERO_WEIGHT;
    // DUAL WEIGHT
    public static final String FAA2_DUALWEIGHT = MtkTvConfigType.CFG_MISC_EX_A2_SYS_DUAL_WEIGHT;
    // HIGH DEVIATION MODE
    public static final String FAA2_HIGHDEVIATIONMODE = MtkTvConfigType.CFG_MISC_EX_A2_SYS_HIGHT_DVTN_MODE;
    // CARRIER SHIFT FUNCTION
    public static final String FAA2_CARRIERSHIFTFUNCTION = MtkTvConfigType.CFG_MISC_EX_A2_SYS_CRER_SHIIFT_FUN;
    // FM CARRIER MUTE MODE
    public static final String FAA2_FMCARRIERMUTEMODE = MtkTvConfigType.CFG_MISC_EX_A2_SYS_FM_CRER_MUTE_MODE;
    // FM CARRIER MUTE THRESHOLD HIGH
    public static final String FAA2_FMCARRIERMUTETHRESHOLDHIGH = MtkTvConfigType.CFG_MISC_EX_A2_SYS_FM_CRER_MUTE_THSHD_HIGHT;
    // FM CARRIER MUTE THRESHOLD LOW
    public static final String FAA2_FMCARRIERMUTETHRESHOLDLOW = MtkTvConfigType.CFG_MISC_EX_A2_SYS_FM_CRER_MUTE_THSHD_LOW;
    // FINE TUNE VOLUME
    public static final String FAA2_FINETUNEVOLUME = MtkTvConfigType.CFG_MISC_EX_A2_SYS_FINE_TUNE_VOLUME;

    /*
     * factory_audio_pal_system
     */
    // CORRECT THRESHOLD
    public static final String FAPAL_CORRECTTHRESHOLD = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_CORRECT_THSHD;
    // TOTAL SYNC LOOP
    public static final String FAPAL_TOTALSYNCLOOP = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_TOTAL_SYNC_LOOP;
    // ERROR THRESHOLD
    public static final String FAPAL_ERRORTHRESHOLD =MtkTvConfigType.CFG_MISC_EX_PAL_SYS_ERROR_THSHD;
    // PARITY ERROR THRESHOLD
    public static final String FAPAL_PARITYERRORTHRESHOLD = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_PARITY_ERROR_THSHD;
    // EVERY NUMBER FRAMES
    public static final String FAPAL_EVERYNUMBERFRAMES = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_EVERY_NUMBER_FRAMES;
    // HIGH DEVIATION MODE
    public static final String FAPAL_HIGHDEVIATIONMODE = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_HIGH_DEVIATION_MODE;
    // AM CARRIER MUTE MODE
    public static final String FAPAL_AMCARRIERMUTEMODE = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_AM_CARRIER_MUTE_MODE;
    // AM CARRIER MUTE THRESHOLD HIGH
    public static final String FAPAL_AMCARRIERMUTETHRESHOLDHIGH = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_AM_CARRIER_MUTE_THSHD_HIGH;
    // AM CARRIER MUTE THRESHOLD LOW
    public static final String FAPAL_AMCARRIERMUTETHRESHOLDLOW = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_AM_CARRIER_MUTE_THSHD_LOW;
    // CARRIER SHIFT FUNCTION
    public static final String FAPAL_CARRIERSHIFTFUNCTION = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_CARRIER_SHIFT_FUN;
    // FM CARRIER MUTE MODE
    public static final String FAPAL_FMCARRIERMUTEMODE = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_FM_CARRIER_MUTE_MODE;
    // FM CARRIER MUTE THRESHOLD HIGH
    public static final String FAPAL_FMCARRIERMUTETHRESHOLDHIGH = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_FM_CARRIER_MUTE_THSHD_HIGH;
    // FM CARRIER MUTE THRESHOLD LOW
    public static final String FAPAL_FMCARRIERMUTETHRESHOLDLOW = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_FM_CARRIER_MUTE_THSHD_LOW;
    // PAL FINE TUNE VOLUME
    public static final String FAPAL_PALFINETUNEVOLUME = MtkTvConfigType.CFG_MISC_EX_PAL_SYS_PAL_FINE_TUNE_VOL;
    // AM FINE TUNE VOLUME
    public static final String FAPAL_AMFINETUNEVOLUME =  MtkTvConfigType.CFG_MISC_EX_PAL_SYS_AM_FINE_TUNE_VOL;
    // NICAM FINE TUNE VOLUME
    public static final String FAPAL_NICAMFINETUNEVOLUME =  MtkTvConfigType.CFG_MISC_EX_PAL_SYS_NICAM_FINE_TUNE_VOL;

    /*
     * factory_audio_EU_system
     */
    // EU FM Saturation Mute
    public static final String FAEU_FM = MtkTvConfigType.CFG_MISC_EX_FM_SATURATION_MUTE;
    // EU FM EU NON EU SYSTEM
    public static final String FAEU_EU_NON = MtkTvConfigTypeBase.CFG_MISC_EX_NON_EU;

    /** Audio */
    public static final String BALANCE = MtkTvConfigType.CFG_AUD_AUD_BALANCE;
    public static final String BASS = MtkTvConfigType.CFG_AUD_AUD_BASS;
    public static final String SRS_MODE = MtkTvConfigType.CFG_AUD_AUD_SURROUND;
    public static final String EQUALIZE = MtkTvConfigType.CFG_AUD_AUD_EQUALIZER;
    public static final String SPEAKER_MODE = MtkTvConfigType.CFG_AUD_AUD_OUT_PORT;//fix cr DTV576838,not CFG_AUD_AUD_AD_VOLUME
    public static final String SPDIF_MODE = MtkTvConfigType.CFG_AUD_SPDIF;
    public static final String SPDIF_DELAY = MtkTvConfigType.CFG_AUD_SPDIF_DELAY;
    public static final String AVCMODE = MtkTvConfigType.CFG_AUD_AGC;
    public static final String TYPE = MtkTvConfigType.CFG_AUD_AUD_TYPE;

    public static final String DOWNMIX_MODE = MtkTvConfigType.CFG_AUD_DOLBY_DMIX;
    // Treble high-pitched voice
    public static final String TREBLE = MtkTvConfigType.CFG_AUD_AUD_TREBLE;
    // Speaker volume
    public static final String VISUALLY_SPEAKER = MtkTvConfigType.CFG_AUD_AUD_AD_SPEAKER;
    // Headphone volume
    public static final String VISUALLY_HEADPHONE = MtkTvConfigType.CFG_AUD_AUD_AD_HDPHONE;
    public static final String VISUALLY_VOLUME = MtkTvConfigType.CFG_AUD_AUD_AD_VOLUME;// TODO
    public static final String VISUALLY_PAN_FADE = MtkTvConfigType.CFG_AUD_AUD_AD_FADE_PAN;// TODO
    public static final String VISUALLY_IMPAIRED_AUDIO = "VISUALLY_IMPAIRED_AUDIO";// TODO
    // Visually Impaired
    public static final String VISUALLY_IMPAIRED = "SUB_VISUALLYIMPAIRED";
    public static final String SOUND_TRACKS = MtkTvConfigType.CFG_MENU_SOUNDTRACKS;
    public static final String SOUNDTRACKS_GET_ENABLE = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_ENABLE;
    public static final String SOUNDTRACKS_GET_TOTAL = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_TOTAL;
    public static final String SOUNDTRACKS_SET_INIT = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_SET_INIT;
    public static final String SOUNDTRACKS_SET_DEINIT = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_SET_DEINIT;
    public static final String SOUNDTRACKS_GET_CURRENT = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_CURRENT;
    public static final String SOUNDTRACKS_GET_STRING = "soundtracksgetstring";
    public static final String SOUNDTRACKS_SET_SELECT = MtkTvConfigType.CFG_MENU_SOUNDTRACKS_SET_SELECT;

    public static final String CFG_MENU_AUDIOINFO                        ="g_menu__audioinfo";//MtkTvConfigType.CFG_MENU_AUDIOINFO;
    public static final String CFG_MENU_AUDIOINFO_SET_INIT               ="g_menu__audioinfoinit";//MtkTvConfigType.CFG_MENU_AUDIOINFO_SET_INIT;
    public static final String CFG_MENU_AUDIOINFO_SET_DEINIT             ="g_menu__audioinfodeinit";//MtkTvConfigType.CFG_MENU_AUDIOINFO_SET_DEINIT;
    public static final String CFG_MENU_AUDIOINFO_SET_SELECT             ="g_menu__audioinfoselect";//MtkTvConfigType.CFG_MENU_AUDIOINFO_SET_SELECT;
    public static final String CFG_MENU_AUDIOINFO_GET_TOTAL              ="g_menu__audioinfototal";//MtkTvConfigType.CFG_MENU_AUDIOINFO_GET_TOTAL;
    public static final String CFG_MENU_AUDIOINFO_GET_CURRENT            ="g_menu__audioinfocurrent";//MtkTvConfigType.CFG_MENU_AUDIOINFO_GET_CURRENT;
    //public static final String CFG_MENU_AUDIOINFO_GET_STRING             ="g_menu__audioinfostring";//MtkTvConfigType.CFG_MENU_AUDIOINFO_GET_STRING;
    public static final String CFG_MENU_AUDIOINFO_GET_STRING             ="audioinfogetstring";

    public static final String CFG_AUD_AUD_BBE_MODE = MtkTvConfigType.CFG_AUD_AUD_BBE_MODE;

    /** TV */
    public static final String GENERALESATELITE= MtkTvConfigTypeBase.CFG_MISC_2ND_PREFER_CH_LIST;

    public static final String BRDCST_TYPE = MtkTvConfigTypeBase.CFG_BS_BS_BRDCST_TYPE;
    public static final String TUNER_MODE = MtkTvConfigType.CFG_BS_BS_SRC;
    public static final String DUAL_TUNER = MtkTvConfigTypeBase.CFG_MISC_2ND_TUNER_TYPE;
    public static final String TUNER_MODE2 = MtkTvConfigType.CFG_MISC_2ND_TUNER_TYPE;

    public static final String SVL_ID = MtkTvConfigTypeBase.CFG_BS_SVL_ID;
    public static final String TUNER_MODE_PREFER_SAT = MtkTvConfigTypeBase.CFG_TWO_SAT_CHLIST_PREFERRED_SAT;
    public static final String COUNTRY_REGION_ID = MtkTvConfigType.CFG_COUNTRY_COUNTRY_RID;
    public static final String TV_MTS_MODE = MtkTvConfigType.CFG_AUD_AUD_MTS;// TVConfigurer.MENU_MTS_OPTION;
    public static final String TV_AUDIO_LANGUAGE = MtkTvConfigType.CFG_GUI_AUD_LANG_AUD_LANGUAGE;
    public static final String CFG_MENU_AUDIO_LANGUAGE_ATTR = MtkTvConfigType.CFG_MENU_AUDIO_LANGUAGE_ATTR;
    public static final String CFG_MENU_AUDIO_AD_TYP = MtkTvConfigType.CFG_MENU_AUDIO_AD_TYP;
    public static final String TV_AUDIO_LANGUAGE_2 = MtkTvConfigType.CFG_GUI_AUD_LANG_AUD_2ND_LANGUAGE;
    public static final String TV_SYSTEM = "SCAN_OPTION_TV_SYSTEM";// TVScanner.SCAN_OPTION_TV_SYSTEM;
    public static final String COLOR_SYSTEM = "SCAN_OPTION_COLOR_SYSTEM";// TVScanner.SCAN_OPTION_COLOR_SYSTEM;
    public static final String SCAN_MODE = MtkTvConfigType.CFG_SCAN_MODE_SCAN_MODE;
    public static final String SCAN_MODE_DVBC = "cfg_scan_mode_scan_mode_dvbc";
    public static final String SYM_RATE = "dvbc_single_rf_scan_sym_rate";// TVScanner.SCAN_OPTION_SYM_RATE;
    public static final String TV_FREEZE_CHANNEL = MtkTvConfigType.CFG_MENU_CH_FRZ_CHG;
    public static final String DTV_TSHIFT_OPTION = "DTV_TSHIFT_OPTION";
    public static final String DTV_DEVICE_INFO = "DTV_DEVICE_INFO";
    public static final String US_SCAN_MODE = "us"+MtkTvConfigType.CFG_SCAN_MODE_SCAN_MODE;
    public static final String CHANNEL_LIST_TYPE = MtkTvConfigType.CFG_MISC_CH_LST_TYPE;
    public static final String CHANNEL_LIST_SLOT = MtkTvConfigType.CFG_MISC_CH_LST_SLOT;
    public static final String CHANNEL_CAM_PROFILE_SCAN = "g_misc__cam_profile_scan";
    public static final String FREQUENEY_PLAN = "US_single_RF_plan";// MtkTvConfigType.CFG_BS_BS_PLN
    public static final String FAV_US_RANGE_FROM_CHANNEL = "US_range_frome_channel";// TVScanner.SCAN_OPTION_SYM_RATE;
    public static final String FAV_US_RANGE_TO_CHANNEL = "US_range_to_channel";
    public static final String FAV_US_SINGLE_RF_CHANNEL = "US_single_rf_channel";
    public static final String FAV_SA_SINGLE_RF_CHANNEL = "SA_single_rf_channel";

    public static final String DVBC_SINGLE_RF_SCAN_FREQ = "dvbc_single_rf_scan_freq";
    public static final String DVBC_SINGLE_RF_SCAN_MODULATION = "dvbc_single_rf_scan_modulation";
    public static final String SCHEDULE_PVR_SRCTYPE = "SCHEDULE_PVR_SRCTYPE";
    public static final String SCHEDULE_PVR_CHANNELLIST = "SCHEDULE_PVR_CHANNELLIST";
    public static final String SCHEDULE_PVR_REMINDER_TYPE = "SCHEDULE_PVR_REMINDER_TYPE";
    public static final String SCHEDULE_PVR_REPEAT_TYPE = "SCHEDULE_PVR_REPEAT_TYPE";
    public static final String TV_DUAL_TUNER = MtkTvConfigTypeBase.CFG_MISC_2ND_CHANNEL_ENABLE;
    /** setup */
    public static final String OSD_LANGUAGE = MtkTvConfigType.CFG_GUI_LANG_GUI_LANGUAGE;// "SETUP_osd_language";
    public static final String SCREEN_MODE = MtkTvConfigType.CFG_VIDEO_SCREEN_MODE;
    public static final String DPMS = MtkTvConfigType.CFG_MISC_DPMS;
    public static final String WAKEUP_VGA = MtkTvConfigType.CFG_MISC_WAKEUP_REASON;
    public static final String BLUE_MUTE = MtkTvConfigType.CFG_VIDEO_VID_BLUE_MUTE;
    public static final String POWER_ON_MUSIC = "POWER_ON_MUSIC";
    public static final String POWER_OFF_MUSIC = "POWER_OFF_MUSIC";
    public static final String CAPTURE_LOGO_SELECT = "SETUP_capture_logo";
    public static final String FAST_BOOT = "SETUP_fast_boot";
    public static final String GINGA_ENABLE = MtkTvConfigType.CFG_GINGA_GINGA_ENABLE;
    public static final String AUTO_START_APPLICATION = MtkTvConfigType.CFG_GINGA_GINGA_AUTO_START;
    public static final String MODE_LIST_STYLE ="SETUP_sundry_mode_style";
    public static final String MODE_DMR_CONTROL ="SETUP_dmr_contrl";
    public static final String SAMBA_INTERACTIVE_TV ="SETUP_samba_interactive_tv";
   //for EU
    public static final String INTERACTION_CHANNEL = MtkTvConfigType.CFG_MISC_MHEG_INTER_CH;
    public static final String MHEG_PIN_PROTECTION = MtkTvConfigType.CFG_MISC_MHEG_PIN_PROTECTION;
    public static final String HBBTV_SUPPORT = MtkTvConfigType.CFG_MENU_HBBTV;
    public static final String SETUP_HBBTV = "SETUP_hbbtv";
    public static final String HBBTV_DO_NOT_TRACK = MtkTvConfigType.CFG_MENU_DO_NOT_TRACK;
    public static final String HBBTV_ALLOW_3RD_COOKIES = MtkTvConfigType.CFG_MENU_ALLOW_3RD_COOKIES;
    public static final String HBBTV_PERSISTENT_STORAGE = MtkTvConfigType.CFG_MENU_PERSISTENT_STORAGE;
    public static final String HBBTV_BLOCK_TRACKING_SITES = MtkTvConfigType.CFG_MENU_BLOCK_TRACKING_SITES;
    public static final String HBBTV_DEV_ID = MtkTvConfigType.CFG_MENU_DEV_ID;
    public static final String HBBTV_DEV_ID_TIMESTAMP = MtkTvConfigType.CFG_MENU_DHBBTV_DEV_ID_TIMESTAMP;
    public static final String HBBTV_RESET_DEVICE_ID = "SETUP_hbbtv_reset_device_id";
	public static final String FREEVIEW_SETTING = "SETUP_freeviewplay_setting";
    public static final String FREEVIEW_TERM_CONDITION = "SETUP_freview_term_condition";
    public static final String TKGS_OPER_MODE = MtkTvConfigType.CFG_MISC_TKGS_OPERATING_MODE;

    //for oceania
    public static final String OCEANIA_FREEVIEW = MtkTvConfigType.CFG_MISC_FREEVIEW_MODE;
    public static final String OCEANIA_POSTAL = MtkTvConfigType.CFG_EAS_LCT_ST;

    public static final String SETUP_US_TIME_ZONE = "SETUP_us_time_zone";
    public static final String SETUP_TIME_ZONE = "SETUP_time_zone";
    public static final String SETUP_US_SUB_TIME_ZONE = MtkTvConfigType.CFG_TIME_ZONE;
    public static final String SETUP_TIME_SET = "SETUP_time_set";
    public static final String AUTO_SYNC = "SETUP_auto_syn";
    public static final String POWER_ON_TIMER = MtkTvConfigType.CFG_TIMER_TIMER_ON;
    public static final String SETUP_POWER_ON_CH = "SETUP_PowerOnCh";
    public static final String POWER_OFF_TIMER = MtkTvConfigType.CFG_TIMER_TIMER_OFF;
    public static final String POWER_ON_CH_CABLE_MODE = MtkTvConfigType.CFG_NAV_CABLE_ON_TIME_CH;
    public static final String POWER_ON_CH_AIR_MODE = MtkTvConfigType.CFG_NAV_AIR_ON_TIME_CH;
    public static final String POWER_ON_VALID_CHANNELS = "SETUP_poweron_valid_channels";
    public static final String SLEEP_TIMER = "SETUP_sleep_timer";
    public static final String AUTO_SLEEP = MtkTvConfigType.CFG_MISC_AUTO_SLEEP;

    public static final String TIME_DATE = "SETUP_date";
    public static final String TIME_TIME = "SETUP_time";
    public static final String TIMER1 = "SETUP_timer1";
    public static final String TIMER2 = "SETUP_timer2";

    public static final String TIME_START_DATE = "SETUP_start_date";
    public static final String TIME_START_TIME = "SETUP_start_time";
    public static final String TIME_END_DATE = "SETUP_end_date";
    public static final String TIME_END_TIME = "SETUP_end_time";
    public static final String SELECT_MODE="SETUP_select_mode";

    public static final String ADDRESS_TYPE = "UNDEFINE_address_type";
    public static final String DLNA = "SETUP_dlna";
    public static final String MY_NET_PLACE = "SETUP_net_place";

    public static final String CAPTION = "SETUP_caption_setup";
    public static final String DIVX_REG = "SETUP_divx_reg";
    public static final String DIVX_DEA = "SETUP_divx_dea";
    public static final String SCART = MtkTvConfigType.CFG_SCARD_SCART;
    public static final String SCART1 = MtkTvConfigType.CFG_SCARD_SCART0;
    public static final String SCART2 = MtkTvConfigType.CFG_SCARD_SCART1;
    public static final String GINGA_SETUP = "SETUP_ginga_setup";
    public static final String COMMON_INTERFACE = "SETUP_common_interface";
    public static final String SETUP_TIME_SETUP = "SETUP_time_setup";
    public static final String SETUP_TELETEXT = "SETUP_teletext";
    public static final String SETUP_NETWORK = "SETUP_network";
    public static final String SETUP_HDMI ="SETUP_hdmi_setup";
    public static final String AUTOMATIC_CONTENT = "SETUP_automatic_content";
    public static final String RECOMMENDATIONS = "SETUP_recommendations";
    public static final String SETUP_SIGNAL_FORMAT=MtkTvConfigType.CFG_MENU_ONLY_HDMI_EDID_INDEX;
    public static final String SETUP_POWER_ONCHANNEL_LIST = "SETUP_power_onchannel";
    public static final String SETUP_DIGITAL_STYLE = "SETUP_digital_style";
    public static final String SETUP_RECORD_SETTING = "SETUP_recordSetting";
    public static final String SETUP_CHANNEL_UPDATE = "SETUP_channel_update";
    public static final String SETUP_REGION_PHILIPPINES_SETTING = "SETUP_regionSetting_philippines";
    public static final String SETUP_REGION_SETTING_SELECT = "SETUP_regionSetting_select";
    public static final String SETUP_REGION_SETTING = "SETUP_regionSetting";
    public static final String SETUP_REGION_SETTING_LUZON = "SETUP_regionSetting_LUZON";
    public static final String SETUP_REGION_SETTING_VISAYAS = "SETUP_regionSetting_VISAYAS";
    public static final String SETUP_REGION_SETTING_MINDANAO = "SETUP_regionSetting_MINDANAO";
    public static final String SETUP_DEVICE_INFO = "SETUP_deivce_info";
    public static final String SETUP_SCHEDUCE_LIST = "SETUP_schedule_list";
    public static final String SETUP_OAD_SETTING = "SETUP_OADSetting";
    public static final String SETUP_PIP_POP = "SETUP_pip_pop";
    public static final String SETUP_PIP_POP_MODE = "SETUP_pip_pop_mode";
    public static final String SETUP_PIP_POP_SOURCE = "SETUP_pip_pop_source";
    public static final String SETUP_PIP_POP_POSITION = "SETUP_pip_pop_position";
    public static final String SETUP_PIP_POP_SIZE = "SETUP_pip_pop_size";

    //pvr timeshift start
    public static final String PVR_START = "pvr_start";
    public static final String PVR_PLAYBACK_START = "pvr_playback_start";
    public static final String TIMESHIFT_START = "timeshift_start";

    public static final String POWER_SETTING_VALUE="livetv_power_setting_config";
    public static final String SETUP_OAD_DETECT = "SETUP_oad_detect";
    public static final String SETUP_OAD_SET_AUTO_DOWNLOAD = MtkTvConfigType.CFG_OAD_OAD_SEL_OPTIONS_AUTO_DOWNLOAD;
    public static final String SETUP_DIGITAL_TELETEXT_LANGUAGE = MtkTvConfigType.CFG_TTX_LANG_TTX_DIGTL_ES_SELECT;
    public static final String SETUP_DECODING_PAGE_LANGUAGE = MtkTvConfigType.CFG_TTX_LANG_TTX_DECODE_LANG;
    public static final String SETUP_TTX_PRESENTATION_LEVEL = MtkTvConfigType.CFG_TTX_LANG_TTX_PRESENTATION_LEVEL;

    public static final String SETUP_ENABLE_CAPTION = MtkTvConfigType.CFG_CC_CC_CAPTION;//fix CR DTV00581238
    public static final String SETUP_ANALOG_CAPTION = MtkTvConfigType.CFG_CC_ANALOG_CC;
    public static final String SETUP_DIGITAL_CAPTION = MtkTvConfigType.CFG_CC_DIGITAL_CC;
    public static final String SETUP_SUPERIMPOSE_SETUP = MtkTvConfigType.CFG_CC_CC_SI;
    public static final String SETUP_CAPTION_STYLE = MtkTvConfigType.CFG_CC_DCS;// no
    public static final String SETUP_FONT_SIZE = MtkTvConfigType.CFG_CC_DISP_OPT_FT_SIZE;
    public static final String SETUP_FONT_STYLE = MtkTvConfigType.CFG_CC_DISP_OPT_FT_STYLE;
    public static final String SETUP_FONT_COLOR = MtkTvConfigType.CFG_CC_DISP_OPT_FT_COLOR;
    public static final String SETUP_FONT_OPACITY = MtkTvConfigType.CFG_CC_DISP_OPT_FT_OPACITY;
    public static final String SETUP_BACKGROUND_COLOR = MtkTvConfigType.CFG_CC_DISP_OPT_BK_COLOR;
    public static final String SETUP_BACKGROUND_OPACITY = MtkTvConfigType.CFG_CC_DISP_OPT_BK_OPACITY;
    public static final String SETUP_WINDOW_COLOR = MtkTvConfigType.CFG_CC_DISP_OPT_WIN_COLOR;
    public static final String SETUP_WINDOW_OPACITY = MtkTvConfigType.CFG_CC_DISP_OPT_WIN_OPACITY;
//    public static final String SETUP_CHANNEL_NEW_SVC_ADDED = MtkTvConfigType.CFG_MENU_NEW_SVC_ADDED;;
    public static final String SETUP_SHIFTING_MODE = MtkTvConfigType.CFG_RECORD_REC_TSHIFT_MODE;
    public static final String SETUP_AUTO_CHANNEL_UPDATE = MtkTvConfigType.CFG_MISC_AUTO_SVC_UPDATE;//MtkTvConfigType.CFG_MENU_AUTO_CH_UPDATE;
    public static final String SETUP_CHANNEL_UPDATE_MSG = MtkTvConfigType.CFG_MENU_CH_UPDATE_MSG;
    public static final String SETUP_CHANNEL_NEW_SVC_ADDED = MtkTvConfigType.CFG_MENU_NEW_SVC_ADDED;
    public static final String SETUP_RECORD_MODE = MtkTvConfigType.CFG_RECORD_AV_REC_MODE;
    public static final String SETUP_RECORD_QUALITY = MtkTvConfigType.CFG_VIDEO_VID_REC_QUALITY;
    public static final String DTV_SCHEDULE_LIST = "DTV_SCHEDULE_LIST";
    public static final String LICENSE_INFO = "SETUP_license_info";
    public static final String SYSTEM_INFORMATION = "SETUP_system_information";
    public static final String VERSION_INFO = "SETUP_version_info";
    public static final String SETUP_MSI = "g_misc__msi";
    public static final String RESET_DEFAULT = "RESET_DEFAULT";
    public static final String SETUP_POSTAL_CODE = "SETUP_postal_code";
    public static final String DOWNLOAD_FIRMWARE = "download_firmware";
    public static final String BISS_KEY = "biss_key";
    public static final String BISS_KEY_ITEM = "biss_key_item";
    public static final String BISS_KEY_ITEM_ADD = "biss_key_item_add";
    public static final String BISS_KEY_ITEM_SAVE = "biss_key_item_save";
    public static final String BISS_KEY_ITEM_UPDATE = "biss_key_item_update";
    public static final String BISS_KEY_ITEM_DELETE = "biss_key_item_delete";

    public static final String BISS_KEY_FREQ = "biss_freqency";
    public static final String BISS_KEY_SYMBOL_RATE = "biss_sysbol_rate";
    public static final String BISS_KEY_POLAZATION = "biss_key_polazation";
    public static final String BISS_KEY_SVC_ID = "biss_key_sevice_id";
    public static final String BISS_KEY_CW_KEY = "biss_key_cw_key";
    public static final int BISS_KEY_SYMBOL_RATE_MIN = 2000;
    public static final int BISS_KEY_SYMBOL_RATE_MAX = 45000;
    public static final int BISS_KEY_FREQ_MIN = 3000;
    public static final int BISS_KEY_FREQ_MAX = 13000;
    public static final int BISS_KEY_SVC_ID_MIN = 0;
    public static final int BISS_KEY_SVC_ID_MAX = 0xFFFF;
    public static final int TKGS_FREQ_MIN = 10700;
    public static final int TKGS_FREQ_MAX = 12750;
    /*which biss key operate*/
    public static final int BISS_KEY_OPERATE_ADD = 0x00F1;
    public static final int BISS_KEY_OPERATE_UPDATE = 0x00F2;
    public static final int BISS_KEY_OPERATE_DELETE = 0x00F3;
    /**
     * TKGS config ID
     */

    public static final String TKGS_SETTING = "tkgs_setting";
    public static String TKGS_FAC_SETUP_AVAIL_CONDITION = MtkTvConfigType.CFG_MISC_TKGS_AVAILABILITY_COND;
    public static final String TKGS_HIDD_LOCS = "tkgs_hidden_locs";
    public static final String TKGS_RESET_TAB_VERSION = "tkgs_reset_tab_version";
    public static final String TKGS_PREFER_LIST = "tkgs_prefer_list";
    public static final String TKGS_LOC_LIST = "tkgs_loc_list";
    public static final String TKGS_LOC_ITEM = "tkgs_loc_item";
    public static final String TKGS_LOC_ITEM_ADD = "tkgs_loc_item_add";
    public static final String TKGS_LOC_ITEM_SAVE = "tkgs_loc_item_save";
    public static final String TKGS_LOC_ITEM_UPDATE = "tkgs_loc_item_update";
    public static final String TKGS_LOC_ITEM_DELETE = "tkgs_loc_item_delete";
    public static final String TKGS_LOC_ITEM_HIDD_CLEANALL = "tkgs_loc_item_clean_all_hidd";

    public static final String TKGS_LOC_FREQ = "tkgs_loc_freqency";
    public static final String TKGS_LOC_SYMBOL_RATE = "tkgs_loc_sysbol_rate";
    public static final String TKGS_LOC_POLAZATION = "tkgs_loc_polazation";
    public static final String TKGS_LOC_SVC_ID = "tkgs_loc_sevice_id";

    public static final int TKGS_LOC_SVC_ID_MIN = 0;
    public static final int TKGS_LOC_SVC_ID_MAX = 8191;
    // TKGS operate type
    public static final int TKGS_LOC_OPERATE_ADD = 0x00E1;
    public static final int TKGS_LOC_OPERATE_UPDATE = 0x00E2;
    public static final int TKGS_LOC_OPERATE_DELETE = 0x00E3;

    /** having sub page items are defined here */
    public static final String VIDEO_COLOR_TEMPERATURE = "colorTemprature";
    public static final String VIDEO_ADVANCED_VIDEO = "advancedVideo";
    public static final String TV_CHANNEL = "tv_channel";
    public static final String TV_EU_CHANNEL = "tveuChannel";
    public static final String TV_CHANNEL_SCAN = "channel_scan";
    public static final String TV_CHANNEL_SCAN_DVBT = "channel_scan_dvbt_full";
    public static final String TV_CHANNEL_SCAN_BGM = "channel_scan_dvbt_bgm";
    public static final String TV_CHANNEL_SCAN_DVBC = "channel_scan_dvbc_fulls";
    public static final String TV_CHANNEL_AFTER_SCAN_UK_REGION = "tv_channel_after_scan_UK_region";
    public static final String TV_CHANNEL_SCAN_DVBC_OPERATOR = "channel_scan_dvbc_fulls_operator";
    public static final String TV_UPDATE_SCAN_DVBT_UPDATE = "channel_scan_dvbt_UPDATE";
    public static final String TV_UPDATE_SCAN = "update_scan";
    public static final String TV_ANALOG_SCAN = "analog_scan";
    public static final String TV_SINGLE_RF_SCAN_CN = "single_rf_scan_cn";
    public static final String TV_DVBT_SINGLE_RF_SCAN = "tv_dvbt_single_rf_scan";
    public static final String TV_DVBC_SINGLE_RF_SCAN = "tv_dvbc_single_rf_scan";
    public static final String TV_CI_CAM_SCAN = "tv_ci_cam__scan";
    public static final String TV_CHANNEL_EDIT = "channel_edit";
    public static final String TV_SA_CHANNEL_EDIT = "channel_sa_edit";
    public static final String TV_CHANNEL_CLEAR = "channel_clean";
    public static final String TV_FAVORITE_NETWORK = "favorite_network_select";
    public static final String SETUP_UPGRADENET = "SETUP_upgradeNet";
    public static final String SETUP_APPLICATION = "application";
    public static final String TV_CHANNEL_SKIP = "channel_skip";
    public static final String TV_CHANNEL_DELETE = "channel_delete";
    public static final String TV_CHANNEL_SORT = "channel_sort";
    public static final String TV_CHANNEL_MOVE = "channel_move";
    public static final String TV_CHANNEL_DECODE = "channel_decode";
    public static final String TV_CHANNEL_DECODE_LIST = "channel_decode_list";

    public static final String TV_CHANNEL_START_FREQUENCY = "UNDEFINE_channel_start_frequency";
    public static final String TV_CHANNEL_END_FREQUENCY = "UNDEFINE_channel_end_frequency";
    public static final String TV_CHANNEL_STARTSCAN = "start_scan";
    public static final String TV_CHANNEL_STARTSCAN_CEC_CN = "start_scan_dvbc_cn";
    public static final String TV_CHANNEL_EDIT_LIST = "UNDEFINE_channel_edit_list";
    public static final String TV_CHANNEL_INACTIVE_LIST = "inactive_channel_list";
    public static final String TV_CHANNELFINE_TUNE_EDIT_LIST = "UNDEFINE_ChannelFine Tune";
    public static final String TV_CHANNEL_SORT_CHANNELLIST = "tv_channel_sort_channellist";
    public static final String TV_CHANNEL_MOVE_CHANNELLIST = "tv_channel_move_channellist";
    public static final String TV_CHANNEL_NW_NAME = "UNDEFINE_channel_nw_name";
    public static final String TV_CHANNEL_NW_ANALOG_NAME = "UNDEFINE_channel_nw_analog_name";
    public static final String TV_CHANNEL_NO = "UNDEFINE_channel_edit_no";
    public static final String TV_CHANNEL_NAME = "UNDEFINE_channel_edit_name";
    public static final String TV_FREQ = "UNDEFINE_channel_edit_frequency";
    public static final String TV_SINGLE_RF_SCAN_CHANNELS = "single_rf_scan_rf_channel";
    public static final String TV_DVBC_CHANNELS_START_SCAN = "dvbc_scan_channel_start";
    public static final String CHANNEL_FREEZESWITCH = MtkTvConfigType.CFG_VIDEO_FREEZE_VIDEO_ON_CHANGING_CHANNEL;

    public static final String TV_CHANNEL_SA_TSNAME = "UNDEFINE_channel_edit_sa_tsname";
    public static final String TV_CHANNEL_SA_NO = "UNDEFINE_channel_edit_sa_no";
    public static final String TV_CHANNEL_SA_NAME = "UNDEFINE_channel_edit_sa_name";
    public static final String TV_FREQ_SA = "UNDEFINE_channel_edit_frequency_sa";
    public static final String TV_CHANNEL_TYPE="UNDEFINE_channel_type";

    public static final String TV_CHANNEL_COLOR_SYSTEM = "CHANNELEDIT_color_system";
    public static final String TV_SOUND_SYSTEM = "CHANNELEDIT_sound_system";
    public static final String TV_AUTO_FINETUNE = "UNDEFINE_channel_edit_aft";
    public static final String TV_CHANNELFINE_TUNE = "Analog ChannelFine Tune";
    public static final String TV_FINETUNE = "channel_edit_finetune";
    public static final String TV_SKIP = "UNDEFINE_channel_edit_skip";
    public static final String TV_CHANNEL_SKIP_CHANNELLIST = "tv_channel_skip_channellist";
    public static final String TV_CHANNEL_DELETE_CHANNELLIST = "tv_channel_delete_channellist";
    public static final String TV_STORE = "channel_edit_store";
    public static final String TV_ANANLOG_SCAN_UP = "scan_up";
    public static final String TV_ANANLOG_SCAN_DOWN = "scan_down";
    public static final String TV_CHANNEL_SATELLITE_ADD = "Satellite Add";
    /** DTMB Single RF Channel */
    public static final String TV_SINGLE_SCAN_RF_CHANNEL = "UNDEFINE_tv_single_rf_channel";
    public static final String TV_SINGLE_SCAN_SIGNAL_LEVEL = "UNDEFINE_tv_single_scan_signal_level";
    public static final String TV_SINGLE_SCAN_MODULATION = "UNDEFINE_tv_single_scan_modu";
    public static final String TV_SINGLE_SCAN_SIGNAL_QUALITY = "UNDEFINE_tv_singl_scan_signal_quality";
    /** Parental Part */
    public static final String PARENTAL_ENTER_PASSWORD = "parental_enter_password";
    public static final String PARENTAL_PASSWORD = "parental_password";
    public static final String PARENTAL_CHANNEL_BLOCK = "parental_channel_block";
    public static final String PARENTAL_TIME_INTERVAL_BLOCK = "parental_time_interval_block";
    public static final String PARENTAL_PROGRAM_BLOCK = "parental_program_block";
    public static final String PARENTAL_CHANNEL_SCHEDULE_BLOCK = "parental_channel_schedule_block";

    public static final String PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE = "parental_channel_schedule_block_MOde";

    public static final String PARENTAL_US_TV_RATINGS = "parental_us_tv_ratings";
    public static final String PARENTAL_US_MOVIE_RATINGS = "parental_us_movie_ratings";
    public static final String PARENTAL_CANADIAN_ENGLISH_RATINGS = "parental_canadian_english_ratings";
    public static final String PARENTAL_CANADIAN_FRENCH_RATINGS = "parental_canadian_french_ratings";
    public static final String PARENTAL_AGE_RATINGS = "PARENTAL_AGE_RATINGS";// TVConfigurer.MENU_MTS_OPTION;
    public static final String PARENTAL_AGE_RATINGS_EU = "PARENTAL_AGE_RATINGS_EU";
    public static final String PARENTAL_AGE_RATINGS_EU_SGP = "PARENTAL_AGE_RATINGS_EU_SGP";
    public static final String PARENTAL_AGE_RATINGS_EU_THL = "PARENTAL_AGE_RATINGS_EU_THL";
    public static final String PARENTAL_AGE_RATINGS_EU_ZAF = "PARENTAL_AGE_RATINGS_EU_ZAF";
    public static final String PARENTAL_AGE_RATINGS_EU_OCEANIA_AUS = "PARENTAL_AGE_RATINGS_EU_EU_OCEANIA_AUS";
    public static final String PARENTAL_CONTENT_RATINGS = "PARENTAL_CONTENT_RATINGS";// TVConfigurer.MENU_MTS_OPTION;
    public static final String PARENTAL_OPEN_VCHIP = "parental_open_vchip";// MtkTvConfigType.CFG_RATING_VCHIP_CA;
    public static final String PARENTAL_BLOCK_UNRATED = "parental_block_unrated";// MtkTvConfigType.CFG_DPMS;
    public static final String PARENTAL_RATINGS_ENABLE = "parental_ratings_enable";// MtkTvConfigType.CFG_DPMS;
    public static final String PARENTAL_INPUT_BLOCK = "parental_input_block";
    public static final String PARENTAL_INPUT_BLOCK_SOURCE = "UNDEFINE_parental_input_block_source";
    public static final String PARENTAL_CHANGE_PASSWORD = "parental_change_password";
    public static final String PARENTAL_CLEAN_ALL = "parental_clean_all";
    public static final String PARENTAL_PASSWORD_NEW = "parental_password_new";
    public static final String PARENTAL_PASSWORD_NEW_RE = "parental_password_new_re";
    public static final String PARENTAL_CHANNEL_BLOCK_CHANNELLIST = "parental_block_channellist";
    public static final String PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST = "parental_channel_schedule_block_channellist";
    public static final String PARENTAL_OPEN_VCHIP_REGIN = "parental_open_vchip_regin";// MtkTvConfigType.CFG_RATING_VCHIP_CA;
    public static final String PARENTAL_OPEN_VCHIP_DIM = "parental_open_vchip_dim";// MtkTvConfigType.CFG_RATING_VCHIP_CA;
    public static final String PARENTAL_OPEN_VCHIP_LEVEL = "parental_open_vchip_level";// MtkTvConfigType.CFG_RATING_VCHIP_CA;
    public static final String PARENTAL_TIF_CONTENT_RATGINS = "parental_tif_content_ratings";
    public static final String PARENTAL_TIF_CONTENT_RATGINS_SYSTEM = "parental_tif_content_ratings_system";
    public static final String PARENTAL_TIF_RATGINS_SYSTEM_CONTENT = "parental_tif_ratings_system_cnt";

    public static final String PARENTAL_CFG_RATING_BL_TYPE =  MtkTvConfigType.CFG_RATING_BL_TYPE;
    public static final String PARENTAL_CFG_RATING_BL_START_TIME = MtkTvConfigType.CFG_RATING_BL_START_TIME;
    public static final String PARENTAL_CFG_RATING_BL_END_TIME = MtkTvConfigType.CFG_RATING_BL_END_TIME;

    /** Factory part */
    public static final String FACTORY_VIDEO = "SUB_factory_video";
    public static final String FACTORY_AUDIO = "SUB_factory_audio";
    public static final String FACTORY_TV = "SUB_factory_TV";
    public static final String FACTORY_SETUP = "SUB_factory_setup";
    public static final String FACTORY_PRESET_CH = "SUB_preset_ch";

    public static final String FACTORY_TV_RANGE_SCAN = "tuner_range_scan";
    public static final String FACTORY_TV_RANGE_SCAN_DIG = "tuner_range_scan_dig";
    public static final String FACTORY_TV_RANGE_SCAN_ANA = "tuner_range_scan_ana";
    public static final String FACTORY_TV_SINGLE_RF_SCAN = "tuner_single_rf_scan";
    public static final String FACTORY_TV_FACTORY_SCAN = "tuner_factory_scan";
    public static final String FACTORY_TV_TUNER_DIAGNOSTIC = "tuner_diagnostic";
    public static final String FACTORY_TV_TUNER_DIAGNOSTIC_NOINFO = "tuner_diagnostic_noinfo";
    public static final String FACTORY_TV_TUNER_DIAGNOSTIC_VERSION = "tuner_diagnostic_version";
    public static final String FACTORY_TV_TUNER_DIAGNOSTIC_RF = "tuner_diagnostic_rf";
    public static final String FACTORY_TV_TUNER_DIAGNOSTIC_LOCK = "tuner_diagnostic_lock";
    public static final String FACTORY_SETUP_EVENT_FORM = MtkTvConfigType.CFG_MISC_EVT_FORM;
    public static final String FACTORY_SETUP_BURNING_MODE = MtkTvConfigType.CFG_MISC_EX_BRUNING_MODE;
    public static final String FACTORY_SETUP_UART_MODE = MtkTvConfigType.CFG_MISC_EX_UART_FACTORY_MODE;
    public static final String FACTORY_SETUP_CLEAN_STORAGE = "factory_setup_clean_storage";
    public static final String FACTORY_PRESET_CH_DUMP = "preset_ch_dump";
    public static final String FACTORY_PRESET_CH_PRINT = "preset_ch_print";
    public static final String FACTORY_PRESET_CH_RESTORE = "preset_ch_restore";
    public static final String FACTORY_SETUP_CI_UPDATE = "factory_updateCi";
    public static final String FACTORY_SETUP_CI_ECP_UPDATE = "factory_updateCi_ecp";
    public static final String FACTORY_SETUP_CI_ERASE = "factory_eraseCi";
    public static final String FACTORY_SETUP_CI_QUERY = "factory_queryCi";
    public static final String FACTORY_SETUP_DATA_SERVICE_SUPPORT = "g_misc__fac_data_service";
    public static final String FACTORY_SETUP_CAPTION = "UNDEFINE_mts_factory_setup_cap";
    public static final String FACTORY_SETUP_EXTERN = "g_cc__cc_attr_ex_size_idx";//MtkTvConfigType.CFG_CC_CC_ATTR_EX_SIZE_IDX;
    public static final String FACTORY_SETUP_EQUAL = "g_cc__cc_attr_equal_width_idx";//MtkTvConfigType.CFG_CC_CC_ATTR_EQUAL_WIDTH_IDX;
    public static final String FACTORY_SETUP_AUTO = "g_cc__cc_attr_auto_line_feed_idx";//MtkTvConfigType.CFG_CC_CC_ATTR_AUTO_LINE_FEED_IDX;
    public static final String FACTORY_SETUP_ROLL = "g_cc__cc_attr_roll_up_mode_idx";//MtkTvConfigType.CFG_CC_CC_ATTR_ROLL_UP_MODE_IDX;
    public static final String FACTORY_SETUP_UTF8 =MtkTvConfigTypeBase.CFG_CC_CC_ATTR_SUPPORT_UTF8_IDX;

    public static final String CHANNEL_CHANNEL_SOURCES = "channel_channel_sources";
    public static final String CHANNEL_CUSTOMIZE_CHANNEL_LIST = "channel_customize_channel_list";
    public static final String CHANNEL_CHANNEL_INSTALLATION_MODE = "channel_channel_installation_mode";
    public static final String CHANNEL_PARENTAL_CONTROLS = "channel_parental_controls";
    public static final String CHANNEL_MULTI_AUDIO = "channel_multi_audio";
    public static final String CHANNEL_OPEN_SOURCE_LICENSES = "channel_open_source_licenses";
    public static final String CHANNEL_VERSION = "channel_version";
    public static final String CHANNEL_LCN = MtkTvConfigTypeBase.ACFG_FUSION_LCN;
    public static final String CHANNEL_DVBT_SCAN_TYPE = MtkTvConfigTypeBase.ACFG_FUSION_ENCRYPT_DVBT;
    public static final String CHANNEL_DVBT_STORE_TYPE = MtkTvConfigTypeBase.ACFG_FUSION_STORAGE_DVBT;
    public static final String CHANNEL_DVBC_SCAN_TYPE = MtkTvConfigTypeBase.ACFG_FUSION_ENCRYPT_DVBC;
    public static final String CHANNEL_DVBC_STORE_TYPE = MtkTvConfigTypeBase.ACFG_FUSION_STORAGE_DVBC;

    /*DVBS*/
    public static final String M7_LNB_Scan = "dvbs_m7_lnb_search";
    public static final String DVBS_SAT_OP_REGION = "DVBS_SAT_OP_REGION";
    public static final String DVBS_SAT_ATENNA_TYPE = MtkTvConfigTypeBase.CFG_BS_BS_SAT_ANTENNA_TYPE;

    public static final String DVBS_SAT_PREFIX = "DVBS_SAT_";
    public static final String DVBS_SAT_OP = "DVBS_SAT_OP";
	public static final String DVBS_SAT_MANUAL_TURNING_NEXT = "DVBS_SAT_MANUAL_TURNING_NEXT";
    public static final String DVBS_RESCAN_NEXT = "DVBS_RESCAN_NEXT";
    public static final String DVBS_RESCAN_MORE = "DVBS_RESCAN_MORE";
    public static final String DVBS_RESCAN_SINGLE = "DVBS_RESCAN_SINGLE";
    public static final String DVBS_RESCAN_TONEBURST = "DVBS_RESCAN_TONEBURST";
    public static final String DVBS_RESCAN_DISEQC10 = "DVBS_RESCAN_DISEQC10";
    public static final String DVBS_RESCAN_DISEQC11 = "DVBS_RESCAN_DISEQC11";
    public static final String DVBS_RESCAN_DISEQC12 = "DVBS_RESCAN_DISEQC12";
    public static final String DVBS_RESCAN_UNICABLE1 = "DVBS_RESCAN_UNICABLE1";
    public static final String DVBS_RESCAN_UNICABLE2 = "DVBS_RESCAN_UNICABLE2";
    public static final String DVBS_SAT_OP_CAM_SCAN = "DVBS_SAT_OP_CAM_SCAN";
	public static final String DVBS_SAT_SCAN_NORMAL = "DVBS_SAT_SCAN_NORMAL";
    public static final String DVBS_SAT_SCAN_SDX = "DVBS_SAT_SCAN_SDX";
    public static final String DVBS_SAT_SCAN_DOWNLOAD= "DVBS_SAT_SCAN_DOWNLOAD";
    public static final String DVBS_SAT_ATENNA_TYPE_SET = "Satellite atenna type set";
    public static final String DVBS_SAT_ATENNA_TYPE_TUNER = "Satellite atenna type tuner";
    public static final String DVBS_SAT_ATENNA_TYPE_SUB_TUNER = "Satellite atenna type sub tuner";
    public static final String DVBS_SAT_ATENNA_TYPE_SUB_USERDEF = "Satellite atenna type sub user define";
    public static final String DVBS_SAT_ATENNA_TYPE_SUB_BANDFREQ = "Satellite atenna type sub band freq";

    public static final String DVBS_SAT_ATENNA_TYPE_USERDEF = "Satellite atenna type user define";
    public static final String DVBS_SAT_ATENNA_TYPE_BANDFREQ = "Satellite atenna type band freq";
    public static final String DVBS_SAT_RE_SCAN = "Satellite Re-scan";
    public static final String DVBS_SAT_NORMAL_SCAN = "Satellite Normal Scan";
    public static final String DVBS_SAT_DEDATIL_INFO = "DVBS_SAT_DEDATIL_INFO";
    public static final String DVBS_SAT_DEDATIL_INFO_ITEMS = "DVBS_SAT_DEDATIL_INFO_ITEMS";
    public static final String DVBS_SAT_ADD = "Satellite Add";
    public static final String DVBS_SAT_UPDATE_SCAN = "Satellite Update";
    public static final String DVBS_SAT_MANUAL_TURNING = "DVBS_SAT_MANUAL_TURNING";
    public static final String DVBS_SAT_DEDATIL_INFO_SCAN = "DVBS_SAT_DEDATIL_INFO_SCAN";
    public static final String DVBS_SAT_DEDATIL_INFO_START_SCAN = "DVBS_SAT_DEDATIL_INFO_START_SCAN";
    public static final String DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG = "DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG";
    public static final String DVBS_SAT_DEDATIL_INFO_START_SCAN_SCAN_CONFIG = "DVBS_SAT_DEDATIL_INFO_START_SCAN_SCAN_CONFIG";
    public static final String DVBS_SAT_DEDATIL_INFO_START_SCAN_STORE_CONFIG = "DVBS_SAT_DEDATIL_INFO_START_SCAN_STORE_CONFIG";
    public static final String DVBS_SAT_DEDATIL_INFO_TP_ITEMS = "DVBS_SAT_DEDATIL_INFO_TP_ITEMS";
    public static final String DVBS_SAT_MANUAL_TURNING_TP = "DVBS_SAT_MANUAL_TURNING_TP";
    public static final String DVBS_SAT_COMMON_TP = "DVBS_SAT_COMMON_TP";
    public static final String DVBS_SIGNAL_QULITY = "DVBS_SIGNAL_QULITY";
    public static final String DVBS_SIGNAL_LEVEL = "DVBS_SIGNAL_LEVEL";
    public static final String DVBS_DETAIL_POSITION = "DVBS_DETAIL_POSITION";
    public static final String DVBS_DETAIL_LNB_POWER = "DVBS_DETAIL_LNB_POWER";
    public static final String DVBS_DETAIL_LNB_FREQUENCY = "DVBS_DETAIL_LNB_FREQUENCY";
    public static final String DVBS_DETAIL_DISEQC12_SET = "DVBS_DETAIL_DISEQC12_SET";
    public static final String DVBS_DETAIL_DISEQC10_PORT = "DVBS_DETAIL_DISEQC10_PORT";
    public static final String DVBS_DETAIL_DISEQC11_PORT = "DVBS_DETAIL_DISEQC11_PORT";
    public static final String DVBS_DETAIL_DISEQC_MOTOR = "DVBS_DETAIL_DISEQC_MOTOR";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL = "DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_DISABLE_LIMITS = "DVBS_DETAIL_DISEQC_MOTOR_DISABLE_LIMITS";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_LIMIT_EAST = "DVBS_DETAIL_DISEQC_MOTOR_LIMIT_EAST";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_LIMIT_WEST = "DVBS_DETAIL_DISEQC_MOTOR_LIMIT_WEST";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION = "DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION = "DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION";
    public static final String DVBS_DETAIL_DISEQC_MOTOR_GOTO_REFERENCE = "DVBS_DETAIL_DISEQC_MOTOR_GOTO_REFERENCE";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE = "DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS = "DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_EAST = "DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_EAST";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_WEST = "DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_WEST";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_STOP_MOVEMENT = "DVBS_DETAIL_DISEQC_MOVEMENT_STOP_MOVEMENT";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE = MtkTvConfigTypeBase.CFG_MISC_DVBS_SAT_LONGITUDE;
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE = MtkTvConfigTypeBase.CFG_MISC_DVBS_SAT_LATITUDE;
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION = "DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION";
    public static final String DVBS_DETAIL_DISEQC_MOVEMENT_GOTOXX = "DVBS_DETAIL_DISEQC_MOVEMENT_GOTOXX";
    public static final int DVBS_ACFG_SINGLE = MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_SINGLE;
    public static final int DVBS_ACFG_TONEBURST = 4;//MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_TONEBURST;
    public static final int DVBS_ACFG_DISEQC10 = 5;//MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_DISEQC1D0;
    public static final int DVBS_ACFG_DISEQC11 = 6;//MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_DISEQC1D1;
    public static final int DVBS_ACFG_DISEQC12 = 7;//MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_DISEQC1D2;
    public static final int DVBS_ACFG_UNICABLE1 = MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_SINGLECABLE;
    public static final int DVBS_ACFG_UNICABLE2 = MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_SINGLECABLE_JESS;
    public static final int TP_POLARIZATION_HORIZONTAL = 0;
    public static final int TP_POLARIZATION_VERTICAL = 1;
    public static final int TP_POLARIZATION_LEFT = 2;
    public static final int TP_POLARIZATION_RIGHT = 3;
    public static final int INVALID_VALUE = 10004;
    public static final int STEP_VALUE = 1;
    public static final int STEP_BIG_VALUE = 10;

    /** wifi part */
    public static final int WIFI_COMMON_BIND = 0;
    public static final int WIFI_COMMON_NODONGLE = 1;
    public static final int WIFI_COMMON_SCAN_FAIL = 2;
    public static final int WIFI_COMMON_SCAN_INVALID = 3;
    public static final int WIFI_COMMON_SCAN_SUCCESS = 4;
    public static final int WIFI_COMMON_MANUAL_INVASSID = 6;
    public static final int WIFI_COMMON_MANUAL_INVAPASS = 7;
    public static final int WIFI_COMMON_MANUAL_SUCCESS = 8;
    public static final int WIFI_COMMON_MANUAL_FAIL = 9;
    // public static final int WIFI_COMMON_MANUAL_PASSERR = 10;
    public static final int WIFI_COMMON_PIN_FAIL = 11;
    public static final int WIFI_COMMON_PIN_SUCCESS = 12;
    public static final int WIFI_COMMON_PBC_FAIL = 13;
    public static final int WIFI_COMMON_PBC_SUCCESS = 14;
    public static final int WIFI_COMMON_PBC_HINT = 15;
    public static final int WIFI_COMMON_SCAN_TIMEOUT = 16;
    public static final int WIFI_COMMON_MANUAL_TIMEOUT = 17;
    public static final int WIFI_COMMON_NO_AP = 18;
    public static final int WIFI_COMMON_NO_WPS_AP = 19;

    public static final int WIFI_INPUT_SCAN_PASS = 0;
    public static final int WIFI_INPUT_MANUAL_SSID = 1;
    public static final int WIFI_INPUT_MANUAL_PASS = 2;

    public static final int W_CONFIRM_UNKNOWN = 0;
    public static final int W_CONFIRM_NONE = 1;
    public static final int W_CONFIRM_WEP = 2;
    public static final int W_CONFIRM_WPA_PSK_TKIP = 7;
    public static final int W_CONFIRM_WPA_PSK_AES = 4;
    public static final int W_CONFIRM_WPA2_PSK_TKIP = 5;
    public static final int W_CONFIRM_WPA2_PSK_AES = 6;
    public static final int W_CONFIRM_AUTO = 3;

    public static final int W_SECURITY_NONE = 0;
    public static final int W_SECURITY_WEP = 1;
    public static final int W_SECURITY_TKIP = 2;
    public static final int W_SECURITY_AES = 3;

    public static final int WIFI_CONNECT_WPS_SCANING = 5;
    public static final int WIFI_CONNECT_SCANING = 0;
    public static final int WIFI_CONNECT_SCAN = 1;
    public static final int WIFI_CONNECT_MANUAL = 2;
    public static final int WIFI_CONNECT_PIN_AUTO = 3;
    public static final int WIFI_CONNECT_PIN_AP = 4;
    public static final int WIFI_CONNECT_PBC = 6;

    public static final int WIFI_SCAN_NORMAL = 0;
    public static final int WIFI_SCAN_WPS = 1;

    public static final int FOCUS_OPTION_CHANGE_CHANNEL = 0;
    public static final String TV_DVBC_SCAN_FREQUENCY = "tv_dvbc_scan_frequency";
    public static final String TV_DVBC_SCAN_NETWORKID = "tv_dvbc_scan_networkid";
    public static final String TV_DVBC_SCAN_TYPE = "tv_dvbc_scan_type";
    public static int MAX_TIME_ZONE = 34;
    public static Map<String,Integer> mScreenMode = new HashMap<String,Integer>();// MAX_TIME_ZONE = 34;
    public static Map<Integer,String> mScreenModeReverse = new HashMap<Integer,String>();// MAX_TIME_ZONE = 34;
    private String[] mScreenModeList;// = new HashMap<Integer,String>();// MAX_TIME_ZONE = 34;
    public static int[] zoneValue = {
            0,
             0,
            (1 * 3600),
            (2 * 3600),
            (3 * 3600),
            (3 * 3600 + 30 * 60),
            (4 * 3600),
            (4 * 3600 + 30 * 60),
            (5 * 3600),
            (5 * 3600 + 30 * 60),
            (5 * 3600 + 45 * 60),
            (6 * 3600),
            (6 * 3600 + 30 * 60),
            (7 * 3600),
            (8 * 3600),
            (9 * 3600),
            (9 * 3600 + 30 * 60),
            (10 * 3600),
            (11 * 3600),
            (12 * 3600),
            (12 * 3600 + 45 * 60),
            (13 * 3600),
            (-12 * 3600),
            (-11 * 3600),
            (-10 * 3600),
            (-9 * 3600),
            (-8 * 3600),
            (-7 * 3600),
            (-6 * 3600),
            (-5 * 3600),
            (-4 * 3600),
            (-3 * 3600 + (-30) * 60),
            (-3 * 3600),
            (-2 * 3600),
            (-1 * 3600)};

    public List<Boolean> get3DConfig() {
        List<Boolean> m3DList = new ArrayList<Boolean>();
        boolean m3DModeFlag = false;
        boolean m3DNavFlag = false;
        boolean m3D2DFlag = false;
        boolean m3DDepthFieldFlag = false;
        boolean m3DProtrudeFlag = false;
        boolean m3DDistanceFlag = false;
        boolean m3DImgSafetyFlag = false;
        boolean m3DLrSwitchFlag = false;
        boolean m3DOsdDepthFlag = false;
        m3DModeFlag = isConfigEnabled(VIDEO_3D_MODE);
        m3DNavFlag = isConfigEnabled(VIDEO_3D_NAV);
        m3D2DFlag = isConfigEnabled(VIDEO_3D_3T2);
        m3DDepthFieldFlag = isConfigEnabled(VIDEO_3D_FIELD);
        m3DProtrudeFlag = isConfigEnabled(VIDEO_3D_PROTRUDE);
        m3DDistanceFlag = isConfigEnabled(VIDEO_3D_DISTANCE);
        m3DImgSafetyFlag = isConfigEnabled(VIDEO_3D_IMG_SFTY);
        m3DLrSwitchFlag = isConfigEnabled(VIDEO_3D_LF);
        m3DOsdDepthFlag = isConfigEnabled(VIDEO_3D_OSD_DEPTH);

        m3DList.add(m3DModeFlag);
        m3DList.add(m3DNavFlag);
        m3DList.add(m3D2DFlag);
        m3DList.add(m3DDepthFieldFlag);
        m3DList.add(m3DProtrudeFlag);
        m3DList.add(m3DDistanceFlag);
        m3DList.add(m3DImgSafetyFlag);
        m3DList.add(m3DLrSwitchFlag);
        m3DList.add(m3DOsdDepthFlag);
        return m3DList;
    }

    public int getValueFromPrefer(String itemID) {
        return save.readValue(itemID);
      }

    public void setValueToPrefer(String itemID, int value) {
        save.saveValue(itemID, value);
        // if (null!=TimeShiftManager.getInstance()&value==0) {
        // TimeShiftManager.getInstance().stopAllRunning();
        // }
    }
    public int getMin(String itemID) {
      int value = mTvConfig.getMinMaxConfigValue(itemID);
      return MtkTvConfig.getMinValue(value);
    }

    public int getMax(String itemID) {
        if (itemID.equals(FA_LATENCY)) {
            return 680;
        }

        int value = mTvConfig.getMinMaxConfigValue(itemID);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getMax, value:" + value);
        return MtkTvConfig.getMaxValue(value);
    }

    public String[] getSupporScreenMode(int[] array){
        if(array == null){
            return null;
        }
        mScreenModeList = new String[array.length];
        for(int i=0;i<array.length;i++){
            mScreenModeList[i] = mScreenModeReverse.get(array[i]);
        }
        return mScreenModeList;
    }

    public String[] getScreenModeList() {
      if (mScreenModeList == null) {
        mScreenModeList = new String[0];
      }
      return mScreenModeList;
    }

    //fix cr DTV00596941
    public int getScreenMode(String[] screenMode,String itemID){
        int value = 0;
        value = mTvConfig.getConfigValue(itemID);

        String key = mScreenModeReverse.get(value);
        for(int i = 0; i < screenMode.length; i++){
            if(key.equals(screenMode[i])){
                value = i;
                break;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getScreenMode, value:" + key + "," + value);
        return value;
    }

    public int getDefault(String itemID) {
        //cts verify start
        if(itemID.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM) ||
           itemID.startsWith(MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT)){
            return getContentRatingValue(itemID);
        }

        //cts verify end
        if (itemID.startsWith(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE)) {
            String channelId = itemID.substring(
                    MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE.length());

            return save.readValue(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE+channelId);
        }
        int value = mTvConfig.getConfigValue(itemID);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDefault, itemID:" + itemID + ", value:" + value);

        if (itemID.equals(FA_COMPRESSION)) {
            switch(value)
            {
                case MtkTvConfigType.AUD_CMPSS_MDOE_LINE:
                    value = 0;
                    break;
                case MtkTvConfigType.AUD_CMPSS_MDOE_RF:
                    value = 1;
                    break;
                default:
                    value = 0;
                    if (CommonIntegration.isEURegion()) {
                        value = 1;
                    }
                    break;
            }

            return value;
        }

        if (itemID.equals(DI_FILM_MODE)) {
          if (CommonIntegration.isCNRegion()) {
            if (value > 2) {
              value = 2;
            }
          } else {
            if (value > 1) {
              value = 1;
            }
          }
          return value;
        }

        if (itemID.equals(NO_SIGNAL_AUTO_POWER_OFF)) {
            value = save.readValue(NO_SIGNAL_AUTO_POWER_OFF);
            if (value < 20) {
                value = 20;
            }
            return value;
        }

        if (itemID.equals(GAMMA)) {
            if (value<1) {
                return 0;
            }
            return value - 1;
        }
        if (itemID.equals(MenuConfigManager.TUNER_MODE)) {
            if(CommonIntegration.getInstance().isDualTunerEnable()){
                if(CommonIntegration.getInstance().getCurrentFocus().equals("sub")){
                   value = mTV.getConfigValue(DUAL_TUNER);
                }
            }
            if (CommonIntegration.isCNRegion()) {
              // use a new value for specail CN
              value = mTV.getConfigValue(TUNER_MODE_CN_USER_SET);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get default TUNER_MODE>>" + value);
            if (value >= 2 && ScanContent.getDVBSAllOperatorList(mContext).size() > 0) {
              if (CommonIntegration.getInstance().isPreferSatMode()) {
                value = 2;
              } else {
                value = 3;
              }
            }
            return value;
      }
        // fix CR DTV00581891 581809
        if (itemID.equals(SCREEN_MODE)) {
          if (!mTV.isCurrentSourceVGA()) {
            if (CommonIntegration.getInstance().isPOPState()) {
              if (value > 3) {
                value = 0;
              }
              return value;
            } else if (CommonIntegration.getInstance().isPIPState()
                && "sub".equalsIgnoreCase(CommonIntegration.getInstance().getCurrentFocus())) {
              if (CommonIntegration.isUSRegion()) {
                if (value > 3) {
                  value = 0;
                }
                return value;
              }
              if (value > 2) {
                value = 0;
              }
              return value;
            }

            if (CommonIntegration.isSARegion()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.i("OptionView", "sa value:" + value);
              if ("main".equalsIgnoreCase(CommonIntegration.getInstance().getCurrentFocus())) {
                if (value == 6) {
                  value = 4;
                } else if (value == 7) {
                  value = 0;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.i("OptionView", "sa sa value:" + value);
                return value;
              }

              if (value == 7) {
                value = 0;
              } else if (value == 5) {
                value = 3;
              } else if (value == 3) {
                value = 4;
              } else if (value == 6) {
                value = 5;
              } else if (value == 1 || value == 2) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
              } else {
                value = 0;
              }
              return value;
            } else if (CommonIntegration.isUSRegion()) {
              if (CommonIntegration.getInstance().getCurChInfo() instanceof MtkTvAnalogChannelInfo) {
                if (CommonIntegration.getInstance().isPIPState()) {
                  // R.array.screen_mode_array_us_pip_main_analog
                  if (value == 7) {
                    value = 0;
                  } else if (value == 6) {
                    value = 4;
                  } else if (value == 1 || value == 2 || value == 3) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                  } else {
                    value = 0;
                  }
                  return value;
                } else {
                  // R.array.screen_mode_array_sa_analog
                  if (value == 7) {
                    value = 0;
                  } else if (value == 5) {
                    value = 3;
                  } else if (value == 3) {
                    value = 4;
                  } else if (value == 6) {
                    value = 5;
                  } else if (value == 1 || value == 2) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                  } else {
                    value = 0;
                  }
                  return value;
                }

              } else {
                // R.array.screen_mode_array_us_pop
                if (value == 7) {
                  value = 0;
                } else if (value == 1) {
                  value = 1;
                } else if (value == 3) {
                  value = 2;
                } else {
                  value = 0;
                }
                return value;
              }
            }
          } else {
            if (value == 3) {
              value = 2;
            } else if (value == 6) {
              value = 2;
            } else if (value == 2) {
              value = 1;
            } else if (value == 1) {
              value = 0;
            } else {
              value = 0;
            }
            return value;
          }
        }
        if (itemID.equals(TV_MTS_MODE)) {
            /*switch (value) {
                case SCC_AUD_MTS_MONO:
                case SCC_AUD_MTS_NICAM_MONO:
                    value =0;
                    break;
                case SCC_AUD_MTS_STEREO:
                case SCC_AUD_MTS_NICAM_STEREO:
                    value =1;
                    break;
                case SCC_AUD_MTS_SUB_LANG:
                case SCC_AUD_MTS_NICAM_DUAL1:
                    value =2;
                    break;
                case SCC_AUD_MTS_NICAM_DUAL2:
                    value = 3;
                    break;
                default:
                    value =1;
                    break;
            }*/
            return value;
        }else if(itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_SGP)){
            value = mTV.getSgpTIFRatingPlus();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSgpRatingValue =="+value);
            return value;
        }else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_THL)) {
            value = mTV.getThlTIFRatingPlus();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getThlTIFRatingPlus =="+value);
            return value;
        }else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_ZAF)) {
            value = mTV.getZafTIFRatingPlus();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getZafRatingValue =="+value);
            return value;
        }else if(itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU)) {
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                value = mTV.getDVBTIFRatingPlus();
                if (MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FRA)) {
                    if (value >= 3) {
                        value = value -1;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("biaoqing", "value1 =="+value);
            }else{
                value = mTV.getDVBRating().getDVBAgeRatingSetting();
            }
            if(value < 3 || value > 18) {
                if (MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FRA)) {
                    if (value == 2) {
                        return value -2;
                    }
                    value = 18;
                    //france is no option named None so -3
                    return value - 3;
                }else {
                    return 0;
                }
            }
            return value - 2;
        } else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_OCEANIA_AUS)) {
             value = mTV.getDVBRating().getDVBAgeRatingSetting();
             if (value < 1) {
                 value = 0;
             } else if (value <= 1) {
                 value = 7;
             } else if (value <= 9) {
                 value = 6;
             } else if (value <= 11) {
                 value = 5;
             } else if (value <= 13) {
                 value = 4;
             } else if (value <= 15) {
                 value = 3;
             } else if (value <= 17) {
                 value = 2;
             } else if (value > 17) {
                 value = 1;
             }
             return value;
        }else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS)) {
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SA_RATING)){
                value = mTV.getSATIFAgeRating();
            }else{
                value = mTV.getIsdbRating().getISDBAgeRatingSetting();
            }
            return value;
        }else if (itemID.equals(MenuConfigManager.PARENTAL_CONTENT_RATINGS)) {
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SA_RATING)){
                value = mTV.getSATIFContentRating();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDefault F_TIF_SA_RATING = YES"+"Value = "+value);
            }else{
                value = mTV.getIsdbRating().getISDBContentRatingSetting();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDefault F_TIF_SA_RATING = NO"+"Value = "+value);
            }
            return value;
        }
        if (itemID.equals(MenuConfigManager.POWER_ON_TIMER)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "value:"+value+"ON_ONCE:"+mTV.getConfigValue(MtkTvConfigType.CFG_TIMER_TIMER_ON_ONCE));
            if (value < 0) {
                value = 1;
                if (mTV.getConfigValue(MtkTvConfigType.CFG_TIMER_TIMER_ON_ONCE) < 0) {
                    value = 2;
                }
            } else {
                value = 0;
                if (mTV.getConfigValue(MtkTvConfigType.CFG_TIMER_TIMER_ON_ONCE) < 0) {
                    value = 2;
                }
            }
            save.saveValue(itemID, value);
            return value;
        }

        if (itemID.equals(MenuConfigManager.POWER_OFF_TIMER)) {
            if (value < 0) {
                value = 1;
                if (mTV.getConfigValue(MtkTvConfigType.CFG_TIMER_TIMER_OFF_ONCE) < 0) {
                    value = 2;
                }
            } else {
                value = 0;
                if (mTV.getConfigValue(MtkTvConfigType.CFG_TIMER_TIMER_OFF_ONCE) < 0) {
                    value = 2;
                }
            }
            save.saveValue(itemID, value);
            return value;
        }


        if (itemID.equals(COUNTRY_REGION_ID)) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get COUNTRY_REGION_ID>>>" + value);
        	if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA) && mTV.isAusCountry()) {
        		if (value >=2 && value <= 8) {
        			value = value - 2;
        		} else {
        			value = 0;
        		}
        	} else {
        		switch(value)
        		{
        		case 1:
        			value = 0;
        			break;
        		case 2:
        			value = 1;
        			break;
        		default:
        			value = 0;
        			break;
        		}
        	}
            return value;
        }

            //fix CR DTV00580884
         if (itemID.equalsIgnoreCase(SPDIF_MODE)) {
                // in mw value 2,3 mean PCM16(2) PCM24(3)
                if (value == 3 || value == 2) {
                value = 2;
                } else if (value > 3) {
                value = 3;
                }
                return value;
           }
         if (itemID.equals(MenuConfigManager.SPDIF_DELAY)) {
             if (value > -1 && value < 26) {
               return value * 10;
             }
             return 140;
           }

        if (itemID.equalsIgnoreCase(DOWNMIX_MODE)) {
            value = mTV.getConfigValue(itemID);
            if (CommonIntegration.isEURegion()) {
                return value;
            } else {
                if (value == 2) {
                    value = 0;
                } else if (value == 11) {
                    value = 1;
                } else if (value == 1) {
                    value = 2;
                }
                return value;
            }
        }

        if (itemID.equalsIgnoreCase(VIDEO_3D_MODE)) {
        	value = mTV.getConfigValue(itemID);
        	if(mTV.getConfigValue(VIDEO_3D_NAV)==1){
        		if(value==0){
        			value = 0;
        		}else{
        			value = 1;
        		}
        	}else if(mTV.getConfigValue(VIDEO_3D_NAV)==0){
        		if(value==0){
        			value = 0;
        		}else if(value==2){
        			value = 1;
        		}else if(value==4){
        			value = 2;
        		}else if(value==5){
        			value = 3;
        		}else if(value==8){
        			value = 4;
        		}

        	}
        	   return value;
        }

        //default case
        return value;
    }

	public String getConfigString (String cfgID){
		return mTV.getConfigString(cfgID);
	}

    public int getDefaultScan(String itemID) {
        int value = 0;
        if (mTV != null) {
            value = mTV.getConfigValue(itemID);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDefaultScanvalue>>>" + value);
        return value;
    }

    /**
     *
     * newValue, Boolean or String
     */
    public void setValue(String itemID, Object newValue) {
        //need to fix
        if (itemID.equals(SCREEN_MODE)) {
            int index = Integer.valueOf((String)newValue);

            String[] temp = MenuDataHelper.getInstance(mContext).getScreenMode();
            if(temp==null||temp.length<index){
                return;
            }
            int storedValue = mScreenMode.get(temp[index]);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setValue, " + itemID + "," + newValue + "," + storedValue);
            if (mtkMode != null) {
                //mTvConfig.setConfigValue(itemID, storedValue);
                //use this method to set screen mode.
                mtkMode.setScreenMode(storedValue);
            }
            return;
        }
        //need to fix
        if (newValue instanceof Integer) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "instanceof Integer");
            Integer newName = (Integer) newValue;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "newName "+newName);
            setValue(itemID, newName, mItem);
        }
        else if (newValue instanceof String) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "instanceof String");
            setValue(itemID, Integer.valueOf((String)newValue), mItem);
        }
    }

    public void setValueDefault(String itemID) {
        if(itemID.equals(MenuConfigManager.FACTORY_PRESET_CH_DUMP)){
            mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_PRE_CH_DUMP_CH_INFO_2USB, 0);
        }else if(itemID.equals(MenuConfigManager.FACTORY_PRESET_CH_PRINT)){
            mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_PRE_CH_DUMP_CH_INFO_2TERM, 0);
        }else if(itemID.equals(MenuConfigManager.FACTORY_PRESET_CH_RESTORE)){
            mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_PRE_CH_LOAD_PRESET_CH, 0);
        }else if(itemID.equals(MenuConfigManager.RESET_SETTING)){
            mTV.setConfigValue(MtkTvConfigTypeBase.CFG_VIDEO_DOVI_RESET_PIC_SETTING, 0);
        }
    }
    public void setValue(String itemID, int value){
    	setValue(itemID, value, mItem);
    }

    public void setValue(String itemID, int value, Action item) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set value: " + itemID + "---" + value);
        mItem = item;
        if (itemID == null || mTV == null) {
            return;
        }
        /*
        if (itemID.equalsIgnoreCase(VISUALLY_SPEAKER) || itemID.equalsIgnoreCase(VISUALLY_HEADPHONE)) {
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            MenuDataContainer.getInstance(mContext).changevisuallyvolume(value,itemID);
            return;
        }
        */
        if (itemID.equals(NOTIFY_SWITCH)) {
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            return;
        }
        //cts verify start
        if(itemID.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM) ||
           itemID.startsWith(MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT)){
            setContentRatingValue(itemID, value);
            return;
        }
        //cts verify end

        if (itemID.equalsIgnoreCase(NO_SIGNAL_AUTO_POWER_OFF)) {
            save.saveValue(NO_SIGNAL_AUTO_POWER_OFF, value);
        }

        if (itemID.equalsIgnoreCase(COLOR_G_R) || itemID.equalsIgnoreCase(COLOR_G_G) ||itemID.equalsIgnoreCase(COLOR_G_B)) {
            if(value != getDefault(itemID)) {
            	 mTV.setConfigValue(COLOR_TEMPERATURE, 0);
            	 mTV.setConfigValue(itemID, value);
            }

            return;
        }


        if (itemID.startsWith(PARENTAL_CHANNEL_BLOCK_CHANNELLIST)){
            String channelNumber = itemID.split(":")[1];
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "itemID ="+itemID+ "ChannelNumber = "+channelNumber+"Value = "+value);
            String[] idValue = SettingsUtil.getRealIdAndValue(itemID);
            boolean block = EditChannel.getInstance(mContext).isChannelBlock(Integer.valueOf(channelNumber));
            if (value == 1 && !block) {
                EditChannel.getInstance(mContext).blockChannel(Integer.valueOf(channelNumber), true);
            }else if(value == 0 && block){
                EditChannel.getInstance(mContext).blockChannel(Integer.valueOf(channelNumber), false);
            }else if(value == 0 && !block){
                EditChannel.getInstance(mContext).blockChannel(Integer.valueOf(channelNumber), false);
            }else if(value == 1 && block){
                EditChannel.getInstance(mContext).blockChannel(Integer.valueOf(channelNumber), true);
            }
        }

        if (itemID !=null && itemID.equals(AUTO_SYNC)) {
            save.saveValue(AUTO_SYNC, value);
            return ;
        }
        if (itemID !=null && itemID.equals(SELECT_MODE)) {
            if(value == 0){
                EditChannel.getInstance(mContext).disablePowerOnChannel();
            }
            save.saveValue(SELECT_MODE, value);
            return ;
        }

        if (itemID.equals(TV_MTS_MODE)) {
            String[] allModes = SundryImplement.getInstanceNavSundryImplement(mContext).getAllMtsModes();
            if(allModes.length <= value) {
                return;
            }
            value = SundryImplement.getInstanceNavSundryImplement(mContext).getMtsByModeString(allModes[value]);
            /*if (CommonIntegration.isEURegion()||CommonIntegration.isCNRegion()) {
                switch (value) {
                    case 0:
                        value =SCC_AUD_MTS_MONO;
                        break;
                    case 1:
                        value =SCC_AUD_MTS_NICAM_STEREO;
                        break;
                    case 2:
                        value =SCC_AUD_MTS_NICAM_DUAL1;
                        break;
                    case 3:
                        value = SCC_AUD_MTS_NICAM_DUAL2;
                        break;
                    default:
                        value =SCC_AUD_MTS_NICAM_STEREO;
                        break;
                }
            }else {
                switch (value) {
                    case 0:
                        value =SCC_AUD_MTS_MONO;
                        break;
                    case 1:
                        value =SCC_AUD_MTS_STEREO;
                        break;
                    case 2:
                        value =SCC_AUD_MTS_SUB_LANG;
                        break;
                    default:
                        value =SCC_AUD_MTS_STEREO;
                        break;
                }
            }*/
            mTV.setConfigValue(itemID, value);
            return;
            }
        if (itemID.equals(FA_COMPRESSION)) {
            switch(value)
            {
                case 0:
                    value = MtkTvConfigType.AUD_CMPSS_MDOE_LINE;
                    break;
                case 1:
                    value = MtkTvConfigType.AUD_CMPSS_MDOE_RF;
                    break;
                default:
                    value = MtkTvConfigType.AUD_CMPSS_MDOE_LINE;
                    if (CommonIntegration.isEURegion()) {
                        value = MtkTvConfigType.AUD_CMPSS_MDOE_RF;
                    }
                    break;
            }
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            return;
        }
        if (itemID.equals(COUNTRY_REGION_ID)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set COUNTRY_REGION_ID>>>" + value);
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA) && mTV.isAusCountry()) {
                value = value + 2;
            } else {
                switch(value)
                {
                case 0:
                    value = 1;
                    break;
                case 1:
                    value = 2;
                    break;
                default:
                    value = 1;
                    break;
                }
            }
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            return;
        }
        if (itemID.equals(MenuConfigManager.SPDIF_DELAY)) {
            if (mTV != null) {
                mTV.setConfigValue(itemID, value/10);
            }
            return;
        }
        if(itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU)) {
            if (MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FRA)) {
                value = value + 1;
            }
            if(value >= 1 && value <= 16){
                value += 2;
            }else {
                value = 0;
            }

            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                mTV.genereateDVBTIFRatingPlus(value);
            }else{
                mTV.getDVBRating().setDVBAgeRatingSetting(value);
            }

            return;
        } else if(itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_SGP)){
            if (value>=0 && value<=6) {
                mTV.genereateSingaporeTIFRating(value);
            }
            return;
        }else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_THL)) {
            if (value>=0 && value<=7) {
                mTV.genereateThailandTIFRating(value);
            }
            return;
        } else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_ZAF)) {
            if (value>=0 && value<=6) {
                mTV.genereateZafTIFRating(value);
            }
            return;
        } else if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS_EU_OCEANIA_AUS)) {
            switch (value) {
            case 1:
                value = 18;
                break;
            case 2:
                value = 17;
                break;
            case 3:
                value = 15;
                break;
            case 4:
                value = 13;
                break;
            case 5:
                value = 11;
                break;
            case 6:
                value = 9;
                break;
            case 7:
                value = 1;
                break;
            default:
                break;
            }
            mTV.getDVBRating().setDVBAgeRatingSetting(value);
            return;
        }
        if (itemID.equals(SCREEN_MODE)) {
            String key = item.mOptionValue[value];
            int storedValue = mScreenMode.get(key);
            if (mTV != null) {
                  mTV.setConfigValue(itemID, storedValue);
            }
            return;

        }
        if (itemID.equalsIgnoreCase(FA_LATENCY)) {
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            return;
        }
if (itemID.equalsIgnoreCase(SPDIF_MODE)) {
  // set prop
  if (value == 1 || value == 3){
      android.provider.Settings.Global.putInt(
              mContext.getContentResolver(),
              "nrdp_external_surround_sound_enabled", 1);
  } else {
      android.provider.Settings.Global.putInt(
              mContext.getContentResolver(),
              "nrdp_external_surround_sound_enabled", 0);
  }
  // index >=2 mean PCM(2) & DDP(3),but in mw value mean (PCM24)3 &(DDP)4
  if (value >= 2) {
    value += 1;
  }

  if (mTV != null) {
    mTV.setConfigValue(itemID, value);
  }
  return;
}
        if (itemID.equalsIgnoreCase(DOWNMIX_MODE)) {
            if (CommonIntegration.isEURegion()) {
                if (mTV != null) {
                    mTV.setConfigValue(itemID, value);
                }
                return;
            }else {
                if (value == 0) {
                    value = 2;
                }else if (value == 1) {
                    value = 11;
                }else if (value == 2) {
                    value = 1;
                }else {
                    value = 0;
                }
                if (mTV != null) {
                    mTV.setConfigValue(itemID, value);
                }
                return;
            }
        }

        if(itemID.equalsIgnoreCase(MenuConfigManager.BLUE_MUTE)){
            SaveValue.getInstance(mContext).saveWorldValue(mContext,MtkTvConfigType.CFG_VIDEO_VID_BLUE_MUTE, value,true);
        }

        if (itemID.equals(GAMMA)) {
            value = value + 1;
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            return;
        }
        if(itemID.equals(VGA_MODE)){
            //vga's cfg value is not same as middleware
            //we can't send 0,1 must change to 1,2
            value += 1;
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
            return;
        }
        if(itemID.equals(VIDEO_3D_NAV)){
            //VIDEO_3D_NAV

            if (value == 2) {
            	mTV.setConfigValue(MenuConfigManager.VIDEO_3D_MODE, 1);
            }else{
            	mTV.setConfigValue(MenuConfigManager.VIDEO_3D_MODE, 0);
            }
            mTV.setConfigValue(itemID, value);
            return;
        }

        if (itemID.equalsIgnoreCase(VIDEO_3D_MODE)) {
        	if(mTV.getConfigValue(VIDEO_3D_NAV)==1){
        		if(value==0){
        			value = 0;
        		}else{
        			value = 2;
        		}
        	}else if(mTV.getConfigValue(VIDEO_3D_NAV)==0){
        		if(value==0){
        			value = 0;
        		}else if(value==1){
        			value = 2;
        		}else if(value==2){
        			value = 4;
        		}else if(value==3){
        			value = 5;
        		}else if(value==4){
        			value = 6;
        		}
        	}
            mTV.setConfigValue(itemID, value);
        }

        if (itemID.contains(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE)) {
            //fix CR DTV00581157
            String channelId = itemID.substring(
                    MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE.length());
            SettingsPreferenceScreen.block = value;
            save.saveValue(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE+channelId, value);
            setSchBlock(Integer.valueOf(channelId),value);
            return;
        }
        if (itemID.equals(MenuConfigManager.PARENTAL_RATINGS_ENABLE)) {
            if (value == 0) {
                mTV.setRatingEnable(false);
            } else {
                mTV.setRatingEnable(true);
            }
            return;
        }

        if (itemID.equals(MenuConfigManager.PARENTAL_AGE_RATINGS)) {
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SA_RATING)){
                mTV.setSATIFAgeRating(value);
            }else{
                mTV.getIsdbRating().setISDBAgeRatingSetting(value);
            }

            return;
        }

        if (itemID.equals(MenuConfigManager.PARENTAL_CONTENT_RATINGS)) {
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SA_RATING)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setValue F_TIF_SA_RATING = YES"+"Value = "+value);
                mTV.setSATIFContentRating(value);
            }else{
                //fix CR DTV00781633 the value from ap to IsdbRating is different
                if (value == 3) {
                    value = 4;
                }else if(value == 4){
                    value = 3;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setValue F_TIF_SA_RATING = NO"+"Value = "+value);
                mTV.getIsdbRating().setISDBContentRatingSetting(value);
            }
            return;
        }
        if (itemID.equals(MenuConfigManager.PARENTAL_BLOCK_UNRATED)) {
            if (value == 0) {
                mTV.setBlockUnrated(false);
            } else {
                mTV.setBlockUnrated(true);
            }
            return;
        }

        if (itemID.equals(OSD_LANGUAGE)) {
            if(att!=null&&att.getStatus()==Status.RUNNING){
                att.cancel(true);
                att=null;
            }
             att=new AsyncOSDLan();
             att.execute(String.valueOf(value));
            return;
        }

        if (itemID.equals(TV_AUDIO_LANGUAGE) || itemID.equals(TV_AUDIO_LANGUAGE_2)) {
            osdLanguage = new LanguageUtil(mContext.getApplicationContext());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.setAudioLanguage  = " +itemID+" Value = "+value);
            try {
                osdLanguage.setAudioLanguage(itemID,value);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if(itemID.equalsIgnoreCase(MenuConfigManager.SETUP_TIME_ZONE)){
            save.saveValue(itemID, value);
            AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            GetTimeZone getZone = GetTimeZone.getInstance(mContext);
            String[] timezones = getZone.generateTimeZonesArray();
            if (timezones[value].equals("As Broadcast")) {// value ==0
              SaveValue.getInstance(mContext).saveBooleanValue(
                  "Zone_time", true);
            } else {
              String olsonId = getZone.getTimeZoneOlsonID(value - 1);
              alarm.setTimeZone(olsonId);
              SaveValue.getInstance(mContext).saveBooleanValue(
                      "Zone_time", false);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timezone -value:" + value);
            getZone.setTimeZone(value, timezones[value]);
            return;
        }
        if (itemID.equals(MenuConfigManager.SETUP_US_SUB_TIME_ZONE)) {
            osdLanguage = new LanguageUtil(mContext.getApplicationContext());

            try {
                osdLanguage.setTimeZone(value);
                int max = 8;
                if (value == 0) {
                    mTV.setConfigValue(itemID, value);
                }else {
                    mTV.setConfigValue(itemID, max - value);
                }
                return;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (itemID.equals(MenuConfigManager.POWER_ON_TIMER)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[" + itemID + "] POWER_ON_TIMER value: " + value);
            if (value == 0) {
                mTV.updatePowerOn(MtkTvConfigType.CFG_TIMER_TIMER_ON_ONCE, 0,
                        save.readStrValue(MenuConfigManager.TIMER1));
                mTV.updatePowerOn(MtkTvConfigType.CFG_TIMER_TIMER_ON, 0,
                        save.readStrValue(MenuConfigManager.TIMER1));
            } else if (value == 1) {
                mTV.updatePowerOn(MtkTvConfigType.CFG_TIMER_TIMER_ON_ONCE, 0,
                        save.readStrValue(MenuConfigManager.TIMER1));
                mTV.updatePowerOn(MtkTvConfigType.CFG_TIMER_TIMER_ON, 1,
                        save.readStrValue(MenuConfigManager.TIMER1));
            } else {
                mTV.updatePowerOn(MtkTvConfigType.CFG_TIMER_TIMER_ON, 1,
                        save.readStrValue(MenuConfigManager.TIMER1));
                mTV.updatePowerOn(MtkTvConfigType.CFG_TIMER_TIMER_ON_ONCE, 1,
                        save.readStrValue(MenuConfigManager.TIMER1));
            }
            save.saveValue(itemID, value);
            return;
        }
        if (itemID.equals(MenuConfigManager.POWER_OFF_TIMER)) {
            if (value == 0) {
                 mTV.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF_ONCE,
                         0,
                         save.readStrValue(MenuConfigManager.TIMER2));
                mTV.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF, 0,
                        save.readStrValue(MenuConfigManager.TIMER2));
            } else if (value == 1) {
                mTV.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF, 1,
                        save.readStrValue(MenuConfigManager.TIMER2));
                mTV.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF_ONCE, 0,
                        save.readStrValue(MenuConfigManager.TIMER2));
            } else {
                mTV.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF_ONCE, 1,
                        save.readStrValue(MenuConfigManager.TIMER2));
                mTV.updatePowerOff(MtkTvConfigType.CFG_TIMER_TIMER_OFF, 1,
                        save.readStrValue(MenuConfigManager.TIMER2));
            }
            save.saveValue(itemID, value);
            return;
        }
        if (itemID.equalsIgnoreCase(MenuConfigManager.PARENTAL_CFG_RATING_BL_TYPE)) {
            mTV.setTimeInterval(value);
            return;
        }
        if(itemID.equals(MenuConfigManager.SETUP_CAPTION_STYLE)){
            //DEMO_TYPE_IS_SHOWING,   /*0, 0: hide,1:show*/
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "captionStyle, " + value);
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(0, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_FONT_SIZE)){
            //DEMO_TYPE_FONT_SIZE,      /*1, Font-szie  0:small, 1:middle, 2:large */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(1, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_FONT_STYLE)){
            //DEMO_TYPE_FONT_STYLE,     /*2, Font-style 0~1: style1 ~ style7 */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(2, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_FONT_COLOR)){
            //DEMO_TYPE_FONT_COLOR,
            /*3, Font-color 0:black,1:white,2:green,3:blue,4:red,5:cyan,6:yellow,7:magenta */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(3, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_FONT_OPACITY)){
            //DEMO_TYPE_FONT_OPACITY,   /*4, Font-opacity 0:solid,1:transl,2:transp,3:solid flash */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(4, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_BACKGROUND_COLOR)){
            //DEMO_TYPE_BG_COLOR
            /*5, backgroud-color 0:black,1:white,2:green,3:blue,4:red,5:cyan,6:yellow,7:magenta */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(5, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_BACKGROUND_OPACITY)){
            //DEMO_TYPE_BG_OPACITY,     /*6, backgroud-opacity 0:solid,1:transl,2:transp,3:solid flash */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(6, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_WINDOW_COLOR)){
            //DEMO_TYPE_WC_COLOR,       /*7, window-color 0:black,1:white,2:green,3:blue,4:red,5:cyan,6:yellow,7:magenta */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(7, value);
        }
        else if(itemID.equals(MenuConfigManager.SETUP_WINDOW_OPACITY)){
            //DEMO_TYPE_WC_OPACITY,     /*8, window-opacity 0:solid,1:transl,2:transp,3:solid flash */
            MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(8, value);
        }

        if (itemID.equals(AUTO_SLEEP)) {
            int autoSleep = 0;
            if (CommonIntegration.isEURegion()) {
                switch (value) {
                    case 0:
                        autoSleep = 0;
                        break;
                    case 1:
                        autoSleep = 14400;
                        break;
                    case 2:
                        autoSleep = 21600;
                        break;
                    case 3:
                        autoSleep = 28800;
                        break;
                    default:
                        autoSleep = 14400;
                        break;
                }
            }else {
                switch (value) {
                    case 0:
                        autoSleep = 0;
                        break;
                    case 1:
                        autoSleep = 3600;
                        break;
                    case 2:
                        autoSleep = 7200;
                        break;
                    case 3:
                        autoSleep = 18000;
                        break;
                    default:
                        autoSleep = 0;
                        break;
                }
            }
            mTV.setConfigValue(itemID, autoSleep);
            return;
        }

        if (itemID.equals(POWER_ON_CH_AIR_MODE) || itemID.equals(POWER_ON_CH_CABLE_MODE)) {
            return ;
        }


        if (itemID.equals(MenuConfigManager.SHARPNESS)) {
            int min = getMin(itemID);
            int max = getMax(itemID);

            if (value >= min && value <= max) { // hzy fix
                                                                      // CR:363304
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Normal Case.--------------");
                if (mTV != null) {
                    mTV.setConfigValue(itemID, value);
                }
            } else if (value < min) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Minimum Case.--------------");
                if (mTV != null) {
                    mTV.setConfigValue(itemID, max);
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Maximum Case.--------------");
                if (mTV != null) {
                    mTV.setConfigValue(itemID, min);
                }
            }
            return;
        } else if (itemID.equals(TUNER_MODE)) {
            setTunerMode(value, true);
            return;
        }
        if (DIGITAL_SUBTITLE_LANG_2ND.equals(itemID) || DIGITAL_SUBTITLE_LANG.equals(itemID)) {
            osdLanguage = new LanguageUtil(mContext.getApplicationContext());

            try {
                osdLanguage.setSubtitleLanguage(itemID,value);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (itemID.startsWith(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING)) {
            String[] spls = itemID.split("_");
            if (spls != null && spls.length == 2) {
              int index = Integer.parseInt(spls[1]);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set CFG_MENU_AUDIOINFO_GET_STRING" + index);
              mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_SELECT, index);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mID is not correct:" + itemID);
            }
          }
        if (itemID.startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING)) {
        	String trackId = "";
        	String[] spls = itemID.split("_");
        	if (spls != null && spls.length == 2) {
        		trackId = spls[1];
        	}
        	if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
        		if (CommonIntegration.TV_FOCUS_WIN_MAIN == CommonIntegration.getInstance().getCurrentFocus()) {
        			TurnkeyUiMainActivity.getInstance().getTvView()
        							.selectTrack(TvTrackInfo.TYPE_AUDIO, trackId);
        		} else {
        			TurnkeyUiMainActivity.getInstance().getPipView()
        							.selectTrack(TvTrackInfo.TYPE_AUDIO, trackId);
        		}
        	} else {
        		MtkTvAVMode.getInstance().selectAudioById(trackId);
        	}


        /*	else {
        		String[] spls = itemID.split("_");
        		if (spls != null && spls.length == 2) {
        			int index = Integer.parseInt(spls[1]);
        			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set SOUNDTRACKS_SET_SELECT" + index);
        			mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_SELECT, index);
        		}
        	} */

        }
        if (itemID.startsWith(MenuConfigManager.CHANNEL_LIST_TYPE)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"CHANNEL_LIST_TYPE");
            if(!CommonIntegration.getInstance().is3rdTVSource() &&
                    (LiveTvSetting.isBootFromLiveTV() || DestroyApp.isCurActivityTkuiMainActivity())){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"CHANNEL_LIST_TYPE isBootFromLiveTV");
                mTV.setConfigValue(itemID, value,3);// select channel
                CommonIntegration.getInstance().selectCurFirstChannel();
                SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
            }else{
                mTV.setConfigValue(itemID, value,1);// not select channel
            }
           return ;
        }

        if (value >= getMin(itemID) && value <= getMax(itemID)) {
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[" + itemID + "] set value: " + value
                    + "   Min value: " + getMin(itemID) + "   Max value"
                    + getMax(itemID));
            if (mTV != null) {
                mTV.setConfigValue(itemID, value);
            }
        }

    }

    public void setTunerModeFromSvlId(int svlId){
        int value = CommonIntegration.getInstance().getTunerModeFromSvlId(svlId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setTunerModeFromSvlId value="+value);
        setTunerMode(value, false);
    }

    public void setTunerModeForInputsPanel(int value, boolean needSelectChannel) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setTunerModeForInputsPanel,"+value);
        if(value < 0) {
            return;
        }
        NavBasic basic = ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
        if (basic instanceof UkBannerView) {
            UkBannerView banner = (UkBannerView) basic;
            banner.reset();
        } else if (basic instanceof BannerView) {
            BannerView banner = (BannerView) basic;
            banner.reset();
            banner.setVisibility(View.GONE);
        }
        if(CommonIntegration.isEURegion()){
            if(value >= 2) {
                boolean notOperator = ScanContent.getDVBSAllOperatorList(mContext).size() <= 0;
                int preferSat = mTV.getConfigValue(TUNER_MODE_PREFER_SAT);//1:prefer S
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setTunerModeForInputsPanel, preferSat=" + preferSat);
                value = preferSat == 1 ? 2 : 3;
                if(!notOperator && value == 2 && ScanContent.isOnlyM7OperatorCountry(mContext)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Prefered satellite set diseqc1.0 in onlyM7 country");
                    mTV.setConfigValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC10);
                }
            }
            mTV.setConfigValue(MenuConfigManager.TUNER_MODE, value ,true);//not select channel
            if (CommonIntegration.isEUPARegion()) {
                if(needSelectChannel){
                    mTV.setConfigValue(TUNER_MODE_CN_USER_SET,value,true);
                    CommonIntegration.getInstance().selectCurFirstChannel();
                }else {
                    mTV.setConfigValue(TUNER_MODE_CN_USER_SET, value ,true);//not select channel
                }
            }
        }
    }

    public void setTunerMode(int value, boolean needSelectChannel){
        PwdDialog mPWDDialog = (PwdDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
        if (mPWDDialog != null) {
            mPWDDialog.setSvctxBlocked(false);
        }
        String itemID = MenuConfigManager.TUNER_MODE;
        if (mTV != null) {
            /*if(TurnkeyUiMainActivity.getInstance() != null && TurnkeyUiMainActivity.getInstance().getTvView() != null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "TvView reset");
                TurnkeyUiMainActivity.getInstance().getTvView().reset();
            } */
            int networkIndex = 0;
            if(needSelectChannel){
                NavBasicDialog channelListDialog = (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
                if(channelListDialog instanceof  ChannelListDialog){
                    ChannelListDialog  dialog= (ChannelListDialog) channelListDialog;
                    networkIndex =dialog.getChannelNetworkIndex();
                }
            }
            if(CommonIntegration.getInstance().isDualTunerEnable()){
                if(CommonIntegration.getInstance().getCurrentFocus().equals("sub")){
                    itemID = MenuConfigManager.DUAL_TUNER;
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "set TUNER_MODE,"+value);
            //For DVBS  (air/cable/Sat,or /air/cable/PreSat/GenSat)
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isBootFromLiveTV ,"+LiveTvSetting.isBootFromLiveTV()+","+networkIndex);
            if(CommonIntegration.isEURegion()){
                boolean notOperator = ScanContent.getDVBSAllOperatorList(mContext).size() <= 0;
                if(!notOperator &&  value == 2 && ScanContent.isOnlyM7OperatorCountry(mContext)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Prefered satellite set diseqc1.0 in onlyM7 country");
                    mTV.setConfigValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE, MenuConfigManager.DVBS_ACFG_DISEQC10);
                }
                int preferSat = 0;//1:prefer S
                if(needSelectChannel && LiveTvSetting.isBootFromLiveTV()){
                    if(CommonIntegration.getInstance().is3rdTVSource()){
                        if(value == 2 && notOperator) {
                          value = 3;
                        }
                        mTV.setConfigValue(itemID, value,true);//not select channel
                        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),networkIndex);
                    }else {
                        if(value == 2 && notOperator) {
                          value = 3;
                        }
                        mTV.setConfigValue(itemID, value,true);
                        CommonIntegration.getInstance().selectCurFirstChannel();
                        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                    }
                    //CommonIntegration.getInstanceWithContext(mContext).selectTIFChannelInfoByChannelIdForTuneMode();
                }else {
                    if(value == 2 && notOperator) {
                        value = 3;
                      }
                    mTV.setConfigValue(itemID, value ,true);//not select channel
                }
                if (com.mediatek.wwtv.tvcenter.TvSingletons.getSingletons().
                    getCommonIntegration().isEUPARegion()) {
                    if(needSelectChannel && LiveTvSetting.isBootFromLiveTV()){
                        if(CommonIntegration.getInstance().is3rdTVSource()){

                            mTV.setConfigValue(TUNER_MODE_CN_USER_SET,value,true);//not select channel
                            SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),networkIndex);
                        }else {
                            mTV.setConfigValue(TUNER_MODE_CN_USER_SET,value,true);
                            CommonIntegration.getInstance().selectCurFirstChannel();
                            SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                        }
                        //CommonIntegration.getInstanceWithContext(mContext).selectTIFChannelInfoByChannelIdForTuneMode();
                    }else {
                        mTV.setConfigValue(TUNER_MODE_CN_USER_SET, value ,true);//not select channel
                    }
                }
                int currentOp = ScanContent.getDVBSCurrentOP();
                preferSat = mTV.getConfigValue(MenuConfigManager.TUNER_MODE_PREFER_SAT);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "set TUNER_MODE-EU111," + mTV.getConfigValue(itemID) + ">>>" + preferSat + ">>>" + currentOp
                        + ">>>" + mTV.getConfigValue(SVL_ID));
                if (currentOp == 0 && preferSat != 0) {//if first change to prefer S, OP should be 0, so need select one OP
                    ScanContent.setSelectedSatelliteOPFromMenu(mContext, 0);
                }

            }else if(com.mediatek.wwtv.tvcenter.TvSingletons.getSingletons().
                getCommonIntegration().isCNRegion()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "set TUNER_MODE_USER_SET,"+value);
                if(needSelectChannel && LiveTvSetting.isBootFromLiveTV()){
                    if(CommonIntegration.getInstance().is3rdTVSource()){

                        mTV.setConfigValue(TUNER_MODE_CN_USER_SET,value,true);//not select channel
                        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),networkIndex);
                    }else {
                        mTV.setConfigValue(TUNER_MODE_CN_USER_SET,value,true);
                        CommonIntegration.getInstance().selectCurFirstChannel();
                        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                    }
                    //CommonIntegration.getInstanceWithContext(mContext).selectTIFChannelInfoByChannelIdForTuneMode();
                }else {
                    mTV.setConfigValue(TUNER_MODE_CN_USER_SET, value ,true);//not select channel
                }
            } else {
                if(needSelectChannel && LiveTvSetting.isBootFromLiveTV()){
                    if(CommonIntegration.getInstance().is3rdTVSource()){

                        mTV.setConfigValue(itemID,value,true);//not select channel
                        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),networkIndex);
                    }else {
                        mTV.setConfigValue(itemID,value,true);
                        CommonIntegration.getInstance().selectCurFirstChannel();
                        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                    }
                    //CommonIntegration.getInstanceWithContext(mContext).selectTIFChannelInfoByChannelIdForTuneMode();
                }else {
                    mTV.setConfigValue(itemID, value ,true);//not select channel
                }
            }
//temp for biaoqing                 MenuDataHelper.getInstance(mContext).changeEnable();
            return;
        }

    }

    public void setActionValue(Action action){
        setValue(action.mItemID, action.mInitValue,action);
    }

    private long getBlockFromTime(int channelID) {
        String dateString = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_START_DATE+channelID);
        int year = Integer.parseInt(dateString.substring(0, 4));
        int month = Integer.parseInt(dateString.substring(5, 7));
        int monthDay = Integer.parseInt(dateString.substring(8));
        String timeString = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_START_TIME+channelID);
        int hour = Integer.parseInt(timeString.substring(0, 2));
        int minute = Integer.parseInt(timeString.substring(3, 5));
        int second = 0;//Integer.parseInt(timeString.substring(6));
        if (year == 1970 && (month == 0 || month == 1) && monthDay == 1) {
            year = 2014;
            month = 2;
            monthDay = 27;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("EidtChannel setSchBlockFromUTCTime", "year:"+year+"month:"+month+"monthDay:"+monthDay+"hour:"+hour+"minute:"+minute+"second:"+second);
        MtkTvTimeFormat.getInstance().set(second, minute, hour, monthDay, month-1, year);
        return MtkTvTimeFormat.getInstance().toMillis();
    }

    private long getBlockEndTime(int channelID) {
        String dateString = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_END_DATE+channelID);
        int year = Integer.parseInt(dateString.substring(0, 4));
        int month = Integer.parseInt(dateString.substring(5, 7));
        int monthDay = Integer.parseInt(dateString.substring(8));
        String timeString = SaveValue.getInstance(mContext).readStrValue(
                MenuConfigManager.TIME_END_TIME+channelID);
        int hour = Integer.parseInt(timeString.substring(0, 2));
        int minute = Integer.parseInt(timeString.substring(3, 5));
        int second = 0;//Integer.parseInt(timeString.substring(6));
        if (year == 1970 && (month == 0 || month == 1) && monthDay == 1) {
            year = 2014;
            month = 2;
            monthDay = 27;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("EidtChannel setSchBlockTOUTCTime", "year:"+year+"month:"+month+"monthDay:"+monthDay+"hour:"+hour+"minute:"+minute+"second:"+second);
        MtkTvTimeFormat.getInstance().set(second, minute, hour, monthDay, month-1, year);
        return MtkTvTimeFormat.getInstance().toMillis();
    }

    private void setSchBlock(int chid,int value) {
        long from = getBlockFromTime(chid);
        long end = getBlockEndTime(chid);
        EditChannel.getInstance(mContext).setSchBlock(chid,from,end,value);
    }

    public void setScanValue(String itemID, int value) {
        if (TV_SINGLE_SCAN_MODULATION.equals(itemID)
                || FREQUENEY_PLAN.equals(itemID)
                || US_SCAN_MODE.equals(itemID)
                || DVBC_SINGLE_RF_SCAN_MODULATION.equals(itemID)) {
            save.saveValue(itemID, value);
        }
    }

    // 3d,when 3d mode change to 2d-to-3d,toast a dialog
    public void toastWearGlass() {
        Toast toast = new Toast(mContext);
        toast.setGravity(Gravity.CENTER, 6, 20);
        toast.setDuration(Toast.LENGTH_SHORT);
        TextView view = new TextView(mContext);
        view.setBackgroundColor(R.color.pin_dialog_background);
        view.setGravity(Gravity.CENTER);
        view.setText(mContext.getString(R.string.menu_video_3d_wear_glass));
        toast.setView(view);
        toast.show();
    }

    //cts verify start
    private TvInputManager getTvInputManager(Context context) {
      if(mTvInputManager == null) {
      mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
      }

      return mTvInputManager;
    }

    private ContentRatingsManager getContentRatingsManager(Context context){
          if(mContentRatingsManager == null) {
            mContentRatingsManager =
                new ContentRatingsManager(context);
          }

          return mContentRatingsManager;
    }

    private ParentalControlSettings getParentalControlSettings(Context context) {
        if(mParentalControlSettings == null) {
            mParentalControlSettings = new ParentalControlSettings(context);
        }

        mParentalControlSettings.loadRatings();
        return mParentalControlSettings;
    }

    public List<ContentRatingSystem> loadContentRatingsSystems() {
        List<TvContentRatingSystemInfo> mRatingSystem = getTvInputManager(mContext).getTvContentRatingSystemList();

        if(mRatingSystem == null || mRatingSystem.isEmpty()) {
            return null;
        }

        ContentRatingsParser parser = new ContentRatingsParser(mContext);
        mContentRatingSystems.clear();

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[Ratings] mRatingSystem size:" + mRatingSystem.size());

        for (TvContentRatingSystemInfo info : mRatingSystem) {
            List<ContentRatingSystem> list = parser.parse(info);
            if(list != null) {
                mContentRatingSystems.addAll(list);
                Log.d(TAG, "[Ratings] list:" + list.size());
            }
        }

        return mContentRatingSystems;
    }

    private void setContentRatingValue(String id, int value) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[setContentRatingValue] id:" + id + ",value = " + value);

        if(id.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM)){
            for (ContentRatingSystem info : mContentRatingSystems) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[setContentRatingValue] m info:" + info.getId());
                if(!id.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM +
                        "|" + info.getId())) {
                    continue;
                }

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[setContentRatingValue] id:" + info);

                getParentalControlSettings(mContext).setContentRatingSystemEnabled(
                    getContentRatingsManager(mContext),
                    info, value == 1 ? true : false);
            }

        }
        else if(id.startsWith(MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT)){
            for (ContentRatingSystem info : mContentRatingSystems) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[setContentRatingValue] c info:" + info.getId());
                if(!id.startsWith(MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT +
                        "|" + info.getId())) {
                    continue;
                }

                for(int i = 0; i < info.getRatings().size(); i++) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[setContentRatingValue] getTitle:" + info.getRatings().get(i).getTitle());

                    if(!id.endsWith(info.getRatings().get(i).getTitle())) {
                        continue;
                    }

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[setContentRatingValue] i:" + i);

                    getParentalControlSettings(mContext).setRatingBlocked(
                        info, info.getRatings().get(i), value == 1 ? true : false);
                }
            }
        }
    }

    private int getContentRatingValue(String id) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] id:" + id);
        int value = 0;

        if(id.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM)){
            for (ContentRatingSystem info : mContentRatingSystems) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] m info:" + info);
                if(!id.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM +
                        "|" + info.getId())) {
                    continue;
                }else{
                	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] id:" + info);
                     value = getParentalControlSettings(mContext).
                             isContentRatingSystemEnabled(info) ? 1 : 0;
                     break;
                }
            }
            return value;
        }
        else if(id.startsWith(MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT)){
        	lableA:
            for (ContentRatingSystem info : mContentRatingSystems) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] c info:" + info);
                if(!id.startsWith(MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT +
                        "|" + info.getId())) {
                    continue;
                }

                for(int i = 0; i < info.getRatings().size(); i++) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] getTitle:" + info.getRatings().get(i).getTitle());
                    if(!id.endsWith(info.getRatings().get(i).getTitle())) {
                        continue;
                    }else {
                    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] id:" + i);
                        value = getParentalControlSettings(mContext).isRatingBlocked(
                                info, info.getRatings().get(i)) ? 1 : 0;
                        break lableA;
					}
                }
            }
        return value;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[getContentRatingValue] failed.");
        return value;
    }
    //cts verify end

    public boolean isConfigEnabled(String cfgId) {
        return mTvConfig.isConfigEnabled(cfgId) ==
            MtkTvConfigType.CFGR_ENABLE ? true : false;
    }

    public boolean isConfigVisible(String cfgid){
        return mTvConfig.isConfigVisible(cfgid) ==
            MtkTvConfigType.CFGR_VISIBLE ? true : false;
    }

 private  class AsyncOSDLan extends AsyncTask<String,String,String>{
     AsyncOSDLan(){
         Log.d(TAG, "AsyncOSDLan: ");
        }
          @Override
          protected String doInBackground(String... arg0) {
              int value = Integer.parseInt(arg0[0]);
                osdLanguage = new LanguageUtil(mContext.getApplicationContext());
            try {
                //Thread.sleep(500);
                osdLanguage.setLanguage(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
              return arg0[0];
          }
  }
 public String getDefaultPowerSettingValue(Context context){
	String value="4 hours";
	switch(getPowerOffSettingTime(context)){
        case 0:
            value = context.getResources().getString(R.string.menu_arrays_power_never);
            break;
        case 1:
            value = context.getResources().getString(R.string.menu_arrays_power_x_hours, 4);
            break;
        case 2:
            value = context.getResources().getString(R.string.menu_arrays_power_x_hours, 6);
            break;
        case 3:
            value = context.getResources().getString(R.string.menu_arrays_power_x_hours, 8);
            break;
        default:
            break;
	}
	 return value;
 }
 public int getDefaultPowerSetting(Context context){
	 int powersetting = 0;
	int def= getAPDefaultValue();
	int old = SaveValue.readWorldIntValue(context,MenuConfigManager.POWER_SETTING_VALUE);
	if(!(old==0&&def!=-1)){
	 if(old==0){
		 powersetting = 1;
	 }else{
		 powersetting = SaveValue.readWorldIntValue(context,MenuConfigManager.POWER_SETTING_VALUE)-1;
	 }
	}
	else{
		powersetting = def;
	}
	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"powersetting="+powersetting);
	 return powersetting;
 }

 public int getPowerOffSettingTime(Context context){
	 int time = 0;
	 int leftmin = Integer.valueOf(context.getResources().getString(R.string.menu_string_power_left_minite));
	 int four = Integer.valueOf(context.getResources().getString(R.string.menu_string_power_four));
	 int six = Integer.valueOf(context.getResources().getString(R.string.menu_string_power_six));
	 int eight = Integer.valueOf(context.getResources().getString(R.string.menu_string_power_eight));
	  switch(getDefaultPowerSetting(context)){
          case 0:
              time = 0;
              break;
          case 1:
              time = four * 60 * 60 - leftmin * 60;
              break;
          case 2:
              time = six * 60 * 60 - leftmin * 60;
              break;
          case 3:
              time = eight * 60 * 60 - leftmin * 60;
              break;
          default:
              break;
	 }

	 return time;
 }

 public String getUSALSDescription(int value, String itemId) {
        String ret = null;
        switch (itemId) {
            case DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE:
                if(value >= 0){
                    ret = value/10.0 + " N";
                }else{
                    ret = (-value)/10.0 + " S";
                }
                break;
            case DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE:
            case DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION:
                if(value >= 0){
                    ret = value/10.0 + " E";
                }else{
                    ret = (-value)/10.0 + " W";
                }
                break;

            default:
                break;
        }
        return ret;
    }

private int getAPDefaultValue(){
	int result = -1;
	  if (DataSeparaterUtil.getInstance() != null ){
		  switch(DataSeparaterUtil.getInstance().getValueAutoSleep()){
		  case 0:
			  result =0;
          break;
		  case 4:
			  result = 1;
			  break;
		  case 6:
			  result = 2;
			  break;
		  case 8:
			  result = 3;
			  break;
			  default:
				  break;
		  }
      }
	return result;
}


}
