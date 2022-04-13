
package com.mediatek.wwtv.setting.util;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.sa.db.DBMgrProgramList;

import com.mediatek.wwtv.setting.base.scan.model.ScannerManager;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams;
import com.mediatek.wwtv.setting.base.scan.model.ScannerListener;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.model.DVBCScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBTScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBCCNScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBTCNScanner;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;

import com.mediatek.twoworlds.tv.MtkTvATSCRating;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.MtkTvChannelListBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.MtkTvISDBRating;
import com.mediatek.twoworlds.tv.MtkTvDVBRating;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanPalSecamBase;
import com.mediatek.twoworlds.tv.MtkTvScanBase.ScanMode;
import com.mediatek.twoworlds.tv.MtkTvScanBase.ScanType;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.RfInfo;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelQuery;
import com.mediatek.twoworlds.tv.model.MtkTvATSCScanPara;
import com.mediatek.twoworlds.tv.model.MtkTvATSCScanParaBase;
import com.mediatek.twoworlds.tv.model.MtkTvCQAMScanParaBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbcScanParaBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsSatelliteSettingBase;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBScanPara;
import com.mediatek.twoworlds.tv.model.MtkTvISDBScanParaBase;
import com.mediatek.twoworlds.tv.model.MtkTvNTSCScanPara;
import com.mediatek.twoworlds.tv.model.MtkTvNTSCScanParaBase;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPPara;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPSettingInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvUSTvRatingSettingInfo;
import com.mediatek.twoworlds.tv.MtkTvMultiMediaBase;
import com.mediatek.wwtv.tvcenter.util.SatelliteProviderManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import android.media.tv.TvInputManager;
import android.media.tv.TvContentRating;

import java.util.ArrayList;
import mediatek.sysprop.VendorProperties;

public class TVContent {
    private static TVContent instance;
    public static final int VSH_SRC_TAG3D_2D = 0;
    public static final int VSH_SRC_TAG3D_MVC = 1; // MVC = Multi-View Codec
    public static final int VSH_SRC_TAG3D_FP = 2; // FP = Frame Packing
    public static final int VSH_SRC_TAG3D_FS = 3; // FS = Frame Sequential
    public static final int VSH_SRC_TAG3D_TB = 4; // TB = Top-and-Bottom
    public static final int VSH_SRC_TAG3D_SBS = 5; // SBS = Side-by-Side
    public static final int VSH_SRC_TAG3D_REALD = 6; //
    public static final int VSH_SRC_TAG3D_SENSIO = 7; //
    public static final int VSH_SRC_TAG3D_LA = 8; // LA = Line Alternative
    public static final int VSH_SRC_TAG3D_TTDO = 9; // TTD only. It is 2D mode
    public static final int VSH_SRC_TAG3D_NOT_SUPPORT = 10;
    private MtkTvScan mScan;
    private MtkTvConfig mTvConfig;
    private MtkTvATSCRating mTvRatingSettingInfo;
    private MtkTvOpenVCHIPSettingInfoBase mOpenVCHIPSettingInfoBase;
    private MtkTvOpenVCHIPPara para;
    private MtkTvCI mCIBase;
    public Handler mHandler;
    private MtkTvMultiMediaBase mMtkTvMultiMediaBase;
    private TvCallbackHandler mCallbackHandler;
    private InputSourceManager mSourceManager;
    private final MtkTvISDBScanPara mISDBScanPara = new MtkTvISDBScanPara();
    private final MtkTvATSCScanPara mATSCScanPara = new MtkTvATSCScanPara();
    private final MtkTvNTSCScanPara mNTSCScanPara = new MtkTvNTSCScanPara();
    private static final boolean dumy = false;
    private final Map<String, Integer> dumyData = new HashMap<String, Integer>();
    private final SaveValue saveV;
    private final Context mContext;
    public ScanSimpleParam mScanSimpleParam;
    private static final String TAG = "TVContent";

    private ScannerManager mScanManager;
    // add by sin_biaoqinggao
    // last input source's Name;
    private String lastInputSourceName = "";
    // current input source's Name;
    private String currInputSourceName = "";

    //for Nordic with sort and move
    public static final String[] mNordicCountry = new String[]{MtkTvConfigType.S3166_CFG_COUNT_SWE,
        MtkTvConfigType.S3166_CFG_COUNT_NOR,
        MtkTvConfigType.S3166_CFG_COUNT_FIN,
        MtkTvConfigType.S3166_CFG_COUNT_DNK,
        MtkTvConfigType.S3166_CFG_COUNT_ISL};

    protected TVContent(Context context) {
        mContext = context;
        saveV = SaveValue.getInstance(context);
        init();
    }

    private void init() {
        dumyData.clear();
        mScan = MtkTvScan.getInstance();
        mTvConfig = MtkTvConfig.getInstance();
        mCIBase = MtkTvCI.getInstance(0);
        mTvRatingSettingInfo = MtkTvATSCRating.getInstance();
        mCallbackHandler = TvCallbackHandler.getInstance();
        mScanManager = new ScannerManager(mContext,this);
        mMtkTvMultiMediaBase = new MtkTvMultiMediaBase();
    }

    static public synchronized TVContent getInstance(Context context) {
        if (instance == null) {
            if(context == null){
                context = DestroyApp.appContext;
            }
            instance = new TVContent(context.getApplicationContext());
        }
        return instance;
    }

    public InputSourceManager getSourceManager() {
        if (mSourceManager == null) {
            mSourceManager = InputSourceManager.getInstance();
        }
        return mSourceManager;
    }

    public String getSysVersion(int eType, String sVersion) {
        String version = MtkTvUtil.getInstance().getSysVersion(eType, sVersion);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSysVersion" + version);
        return version;
    }

    /*
     * US Rating function
     */
    public MtkTvATSCRating getATSCRating() {
        if (mTvRatingSettingInfo == null) {
            mTvRatingSettingInfo = MtkTvATSCRating.getInstance();
        }
        return mTvRatingSettingInfo;
    }

    public MtkTvISDBRating getIsdbRating() {
        return MtkTvISDBRating.getInstance();
    }

    public MtkTvDVBRating getDVBRating() {
        return MtkTvDVBRating.getInstance();
    }

    public MtkTvUSTvRatingSettingInfo getUsRating() {
        return new MtkTvUSTvRatingSettingInfo();
    }

    public int getDVBTIFRatingPlus() {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
        if (!dvbRatings.isEmpty()) {
            List<Integer> ageList = new ArrayList<Integer>();
            for (TvContentRating rating : dvbRatings) {
                String ratingName = rating.getMainRating();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDVBTIFRating:ratingName ==" + ratingName);
                int pos = ratingName.indexOf("_");
                if (pos != -1) {
                    ageList.add(Integer.parseInt(ratingName.substring(pos + 1)));
                } else {
                    return 2;
                }
            }
            int[] ageArray = new int[ageList.size()];
            for (int i = 0; i < ageList.size(); i++) {
                ageArray[i] = ageList.get(i);
            }
            Arrays.sort(ageArray);
            return ageArray[0];
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDVBTIFRating:no ratings getted");
            return 2;
        }

    }

    public int getZafTIFRatingPlus() {
        int value = 0;
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> zafRatings = mTvInputManager.getBlockedRatings();
        allzafRatings = getZAFRatings();
        if (zafRatings.isEmpty()) {
            return 6;
        }
        for (int i = allzafRatings.size()-1; i >=0; i--) {
            if (zafRatings.contains(allzafRatings.get(i))) {
                value = i;
            }
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get zafRatings == "+value);
        return value;
    }

    List<TvContentRating> allzafRatings;
    public List<TvContentRating> getZAFRatings(){
        if (allzafRatings==null) {
            allzafRatings = new ArrayList<TvContentRating>();
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_PG10));
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_10));
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_PG13));
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_13));
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_16));
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_18));
            allzafRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_ZA_TV, RatingConstHelper.RATING_ZA_TV_R18));
      }
        return allzafRatings;
    }

    public void genereateZafTIFRating(int age) {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> zafRatings = mTvInputManager.getBlockedRatings();
        allzafRatings = getZAFRatings();
        if (!zafRatings.isEmpty()) {
            for (int i = age; i >= 0; i--) {
              TvContentRating oldRating = allzafRatings.get(i);
              if (zafRatings.contains(oldRating)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "remove oldRating.flattenToString==" + oldRating.flattenToString());
                mTvInputManager.removeBlockedRating(oldRating);
              }
            }

          }
        for (int i = age; i <= 6; i++) {
            zafRatings = mTvInputManager.getBlockedRatings();
            TvContentRating newRating = allzafRatings.get(i);
            if (!zafRatings.contains(newRating)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add new Ratings"+newRating.flattenToString());
              mTvInputManager.addBlockedRating(newRating);
            }
          }
        for (TvContentRating xRating : mTvInputManager.getBlockedRatings()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "xRating.String==" + xRating.flattenToString());
          }

    }



    List<TvContentRating> allsgpRatings;

    public List<TvContentRating> getSGPRatings() {
        if (allsgpRatings == null) {
            allsgpRatings = new ArrayList<TvContentRating>();
            allsgpRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SG_TV, RatingConstHelper.RATING_SG_TV_G));
            allsgpRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SG_TV, RatingConstHelper.RATING_SG_TV_PG));
            allsgpRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SG_TV, RatingConstHelper.RATING_SG_TV_PG13));
            allsgpRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SG_TV, RatingConstHelper.RATING_SG_TV_NC16));
            allsgpRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SG_TV, RatingConstHelper.RATING_SG_TV_M18));
            allsgpRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SG_TV, RatingConstHelper.RATING_SG_TV_R21));
        }
        return allsgpRatings;
    }
    public int getSgpTIFRatingPlus() {
        int value = 0;
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> sgpRatings = mTvInputManager.getBlockedRatings();
        allsgpRatings = getSGPRatings();
        if (sgpRatings.isEmpty()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getSgpTIFRatingPlus() = 0");
            return 0;
        }
        for (int i = allsgpRatings.size()-1; i >=0; i--) {
            if (sgpRatings.contains(allsgpRatings.get(i))) {
                value = i;
            }
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get sgpRatings == "+value+1);
        return value+1;
    }
    /**
     * tif singapore age rating
     */
    public void genereateSingaporeTIFRating(int age) {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> sgpRatings = mTvInputManager.getBlockedRatings();
        allsgpRatings = getSGPRatings();
        if (age == 0) {
            for (TvContentRating tvContentRating : sgpRatings) {
                mTvInputManager.removeBlockedRating(tvContentRating);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "0 removeBlockedRating" + tvContentRating);
            }
            return;
        }else {
            age = age -1;
        }
        if (!sgpRatings.isEmpty()) {
            for (int i = age; i >= 0; i--) {
                TvContentRating oldRating = allsgpRatings.get(i);
                if (sgpRatings.contains(oldRating)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "remove oldRating.flattenToString==" + oldRating.flattenToString());
                    mTvInputManager.removeBlockedRating(oldRating);
                }
            }

        }
        for (int i = age; i <= 5; i++) {
            sgpRatings = mTvInputManager.getBlockedRatings();
            TvContentRating newRating = allsgpRatings.get(i);
            if (!sgpRatings.contains(newRating)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add new Ratings" + newRating.flattenToString());
                mTvInputManager.addBlockedRating(newRating);
            }
        }
        for (TvContentRating xRating : mTvInputManager.getBlockedRatings()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "xRating.String==" + xRating.flattenToString());
        }
    }
    public int getThlTIFRatingPlus() {
        int value = 0;
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> thlRatings = mTvInputManager.getBlockedRatings();
        allThRatings = getThRatings();
        if (thlRatings.isEmpty()) {
            return 0;
        }
        for (int i = allThRatings.size() - 1; i >= 0; i--) {
            if (thlRatings.contains(allThRatings.get(i))) {
                value = i+1;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get thlRatings == " + value);
        return value;
    }


    List<TvContentRating> allThRatings;

    public List<TvContentRating> getThRatings() {
        if (allThRatings == null) {
            allThRatings = new ArrayList<TvContentRating>();
            allThRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_TH_TV, RatingConstHelper.RATING_TH_TV_P));
            allThRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_TH_TV, RatingConstHelper.RATING_TH_TV_C));
            allThRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_TH_TV, RatingConstHelper.RATING_TH_TV_G));
            allThRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_TH_TV, RatingConstHelper.RATING_TH_TV_PG13));
            allThRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_TH_TV, RatingConstHelper.RATING_TH_TV_PG18));
            allThRatings.add(TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_TH_TV, RatingConstHelper.RATING_TH_TV_X));
        }
        return allThRatings;
    }

    /**
     * tif Thailand rating
     */
    public void genereateThailandTIFRating(int age) {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> thlRatings = mTvInputManager.getBlockedRatings();
        allThRatings = getThRatings();
        if (age == 0 && !thlRatings.isEmpty()) {
            for (TvContentRating tvContentRating : thlRatings) {
                TvContentRating oldRating = tvContentRating;
                mTvInputManager.removeBlockedRating(oldRating);
            }
            return;
        }else {
            age = age-1;
        }
        if (!thlRatings.isEmpty()) {
            for (int i = age; i >= 0; i--) {
                TvContentRating oldRating = allThRatings.get(i);
                if (thlRatings.contains(oldRating)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "remove oldRating.flattenToString==" + oldRating.flattenToString());
                    mTvInputManager.removeBlockedRating(oldRating);
                }
            }
        }

        for (int i = age; i < 6; i++) {
            thlRatings = mTvInputManager.getBlockedRatings();
            TvContentRating newRating = allThRatings.get(i);
            if (!thlRatings.contains(newRating)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add new Ratings" + newRating.flattenToString());
                mTvInputManager.addBlockedRating(newRating);
            }
        }
        for (TvContentRating xRating : mTvInputManager.getBlockedRatings()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "xRating.String==" + xRating.flattenToString());
        }
    }

    /**
     * tif tv content rating
     *
     * @param age
     */
    public void genereateDVBTIFRatingPlus(int age) {
        String rate = null;
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        if (age == 0) {
            List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
            if (!dvbRatings.isEmpty()) {
                for (TvContentRating rating : dvbRatings) {
                    mTvInputManager.removeBlockedRating(rating);
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "remove ratings to none");
            return;

        } else {
            List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
            if (!dvbRatings.isEmpty()) {
                for (int i = age - 1; i >= 3; i--) {
                    rate = String.valueOf(i);
                    TvContentRating oldRating = TvContentRating.createRating(
                            RatingConstHelper.RATING_DOMAIN,
                            RatingConstHelper.RATING_SYS_DVB_TV, "DVB_" + rate);
                    if (dvbRatings.contains(oldRating)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                "remove oldRating.flattenToString==" + oldRating.flattenToString());
                        mTvInputManager.removeBlockedRating(oldRating);
                    }
                }
            }
            for (int i = age; i <= 18; i++) {
                rate = String.valueOf(i);
                TvContentRating newRating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                        RatingConstHelper.RATING_SYS_DVB_TV, "DVB_" + rate);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "newRating.flattenToString==" + newRating.flattenToString());
                if (!dvbRatings.contains(newRating)) {
                    mTvInputManager.addBlockedRating(newRating);
                }
            }
            for (TvContentRating xRating : mTvInputManager.getBlockedRatings()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "xRating.String==" + xRating.flattenToString());
            }
        }
    }

    public int getSATIFAgeRating() {
        int value = -1;
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> saRatings = mTvInputManager.getBlockedRatings();
        if (!saRatings.isEmpty()) {// only one
            List<Integer> ageList = new ArrayList<Integer>();
            for (TvContentRating rating : saRatings) {
                String ratingName = rating.getMainRating();
                List<String> subs = rating.getSubRatings();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSATIFAgeRating:ratingName ==" + ratingName);
                // get not subrating's column's object
                if (subs == null || subs.isEmpty()) {
                    String age = ratingName.substring(ratingName.lastIndexOf("_") + 1);
                    if ("L".equals(age)) {// special for AGE_L
                        ageList.add(0);
                    } else {
                        ageList.add(Integer.parseInt(age));
                    }
                }
            }
            int[] ageArray = new int[ageList.size()];
            for (int i = 0; i < ageList.size(); i++) {
                ageArray[i] = ageList.get(i);
            }
            Arrays.sort(ageArray);
            if (ageArray[0] == 0) {// AGE_L
                value = 0;
            } else if (ageArray[0] == 10) {// AGE_10
                value = 1;
            } else if (ageArray[0] == 12) {// AGE_12
                value = 2;
            } else if (ageArray[0] == 14) {// AGE_14
                value = 3;
            } else if (ageArray[0] == 16) {// AGE_16
                value = 4;
            } else if (ageArray[0] == 18) {// AGE_18
                value = 5;
            }
        } else {
            value = 0;
        }
        return value;
    }

    public void setSATIFAgeRating(int value) {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> saRatings = mTvInputManager.getBlockedRatings();
        // remove all old age rating objects(which has no subratings column)
        if (!saRatings.isEmpty()) {
            for (TvContentRating rating : saRatings) {
                List<String> subRatings = rating.getSubRatings();
                if (subRatings == null || subRatings.isEmpty()) {
                    mTvInputManager.removeBlockedRating(rating);
                }
            }
        }
        // check value and add new age rating object
        if (value == 0) {
            String rate = RatingConstHelper.RATING_SA_TV_AGE_L;
            TvContentRating newRating = null;
            newRating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                    RatingConstHelper.RATING_SYS_SA_TV, rate);
            mTvInputManager.addBlockedRating(newRating);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                    TAG,
                    "SA age Rating:only L --newRating.flattenToString=="
                            + newRating.flattenToString());
        } else {
            String ratingPrefix = "BR_TV_";
            for (int i = (value * 2 + 8); i <= 18; i += 2) {
                TvContentRating newRating = null;
                String rate = ratingPrefix + i;
                newRating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                        RatingConstHelper.RATING_SYS_SA_TV, rate);
                mTvInputManager.addBlockedRating(newRating);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "SA age Rating:" + i + " --newRating.flattenToString=="
                                + newRating.flattenToString());
            }
        }

    }

    public int getSATIFContentRating() {
        int value = 0;
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> saRatings = mTvInputManager.getBlockedRatings();
        if (!saRatings.isEmpty()) {
            List<String> subRatings = null;
            for (TvContentRating rating : saRatings) {
                subRatings = rating.getSubRatings();
                // all content rating object's sub-rating is same
                if (subRatings != null && !subRatings.isEmpty()) {
                    break;
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSATIFContentRating:subratings:" + subRatings);
            if (subRatings != null) {
                if (subRatings.size() == 1) {
                    String sub = subRatings.get(0);
                    if (sub.equals(RatingConstHelper.RATING_SA_TV_DRAG)) {
                        value = 1;
                    } else if (sub.equals(RatingConstHelper.RATING_SA_TV_VIOLENCE)) {
                        value = 2;
                    } else if (sub.equals(RatingConstHelper.RATING_SA_TV_SEX)) {
                        value = 3;
                    }
                } else if (subRatings.size() == 3) {// D&S&V
                    value = 7;
                } else if (subRatings.size() == 2) {
                    if (subRatings.contains(RatingConstHelper.RATING_SA_TV_DRAG)) {
                        if (subRatings.contains(RatingConstHelper.RATING_SA_TV_VIOLENCE)) {// D&V
                            value = 4;
                        } else {// D&S
                            value = 5;
                        }
                    } else {// V&S
                        value = 6;
                    }
                } else {
                    value = 0;
                }
            }
        } else {
            value = 0;
        }
        return value;
    }

    public void setSATIFContentRating(int value) {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> saRatings = mTvInputManager.getBlockedRatings();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setSATIFContentRating:saRatings.size=" + saRatings.size() + ",value=="
                + value);

        // first clean all old content ratings
        for (TvContentRating rating : saRatings) {
            List<String> subRatings = rating.getSubRatings();
            // all content rating object's sub-rating is same
            if (subRatings != null && !subRatings.isEmpty()) {
                mTvInputManager.removeBlockedRating(rating);
            }
        }

        if (value == 0) {// needn't add new content rating object
            return;
        } else {
            String[] subRatings = null;
            if (value == 1) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_DRAG
                };
            } else if (value == 2) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_VIOLENCE
                };
            } else if (value == 3) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_SEX
                };
            } else if (value == 4) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_DRAG, RatingConstHelper.RATING_SA_TV_VIOLENCE
                };
            } else if (value == 5) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_DRAG, RatingConstHelper.RATING_SA_TV_SEX
                };
            } else if (value == 6) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_SEX, RatingConstHelper.RATING_SA_TV_VIOLENCE
                };
            } else if (value == 7) {
                subRatings = new String[] {
                        RatingConstHelper.RATING_SA_TV_DRAG, RatingConstHelper.RATING_SA_TV_SEX,
                        RatingConstHelper.RATING_SA_TV_VIOLENCE
                };
            }
            // add age L first
            String ratingPrefix = "BR_TV_";
            for (int i = 10; i <= 18; i += 2) {// age 10-18
                TvContentRating newRating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,
                        RatingConstHelper.RATING_SYS_SA_TV, ratingPrefix + i, subRatings);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "setSATIFContentRating:newRating.flattenToString=="
                                + newRating.flattenToString());
                mTvInputManager.addBlockedRating(newRating);
            }
        }
    }

    public int getRatingEnable() {
//        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)) {
            TvInputManager mTvInputManager = (TvInputManager) mContext
                    .getSystemService(Context.TV_INPUT_SERVICE);
            boolean ret = mTvInputManager.isParentalControlsEnabled();
            Log.d("TVContent", "TIF.isParentalControlsEnabled():" + ret);
            return ret ? 1 : 0;
//        } else {
//            Log.d("TVContent", "mTvRatingSettingInfo.getRatingEnable():"
//                    + mTvRatingSettingInfo.getRatingEnable());
//            return mTvRatingSettingInfo.getRatingEnable() ? 1 : 0;
//        }
    }

    //fix
    public void setRatingEnable(boolean isRatingEnable) {
//        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)) {
            TvInputManager mTvInputManager = (TvInputManager) mContext
                    .getSystemService(Context.TV_INPUT_SERVICE);
            mTvInputManager.setParentalControlsEnabled(isRatingEnable);
            Log.d("TVContent", "TIF.setParentalControlsEnabled():" + isRatingEnable);
        if (isRatingEnable && ScanContent.isDVBSForTivusatOp()) {
            MtkTvChannelList.getInstance().channellistResetTmpUnlock(CommonIntegration.getInstance().getSvl(), 0, true, false);
        }
//        } else {
//            mTvRatingSettingInfo.setRatingEnable(isRatingEnable);
//        }
    }

    public int getBlockUnrated() {
        return mTvRatingSettingInfo.getBlockUnrated() ? 1 : 0;
    }

    public void setBlockUnrated(boolean isBlockUnrated) {
        mTvRatingSettingInfo.setBlockUnrated(isBlockUnrated);
    }
    public MtkTvOpenVCHIPInfoBase getOpenVchip() {
    	para=getOpenVCHIPPara();
        return mTvRatingSettingInfo.getOpenVCHIPInfo(para);
    }

    public MtkTvOpenVCHIPPara getOpenVCHIPPara() {
        if (para == null) {
            para = new MtkTvOpenVCHIPPara();
        }
        return para;
    }

    public MtkTvOpenVCHIPSettingInfoBase getOpenVchipSetting() {
        if (mOpenVCHIPSettingInfoBase == null) {
            mOpenVCHIPSettingInfoBase = mTvRatingSettingInfo
                    .getOpenVCHIPSettingInfo();
        }
        return mOpenVCHIPSettingInfoBase;
    }

    public MtkTvOpenVCHIPSettingInfoBase getNewOpenVchipSetting(int regionIndex, int dimIndex) {

        return  mTvRatingSettingInfo.getOpenVCHIPSettingInfo(regionIndex, dimIndex);
    }

    public void resetRRT5(){
        getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_NUM);
        int regionNum = getOpenVchip().getRegionNum();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "regionNum="+regionNum);
        for (int i = 0; i < regionNum; i++) {
            getOpenVCHIPPara().setOpenVCHIPParaType(
                 MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_TEXT);
            getOpenVCHIPPara().setRegionIndex(i);
            getOpenVCHIPPara().setOpenVCHIPParaType(
                 MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_NUM);
            int dimNum = getOpenVchip().getDimNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dimNum="+dimNum);
            for(int j = 0; j < dimNum; j++){
                getOpenVCHIPPara().setOpenVCHIPParaType(
                        MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_TEXT);
                getOpenVCHIPPara().setDimIndex(j);
//              getOpenVCHIPPara().setOpenVCHIPParaType(
//              MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_LVL_ABBR);
                getOpenVCHIPPara().setOpenVCHIPParaType(
                        MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_LVL_NUM);
                int levelNum = getOpenVchip().getLevelNum();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "levelNum="+levelNum);
                for (int k = 0; k < levelNum; k++) {
                    getOpenVCHIPPara().setLevelIndex(k + 1);
                    MtkTvOpenVCHIPSettingInfoBase info = getNewOpenVchipSetting(i, j);
                    byte[] block = info.getLvlBlockData();
                    int iniValue = block[k];
                    for (int l = 0; l < block.length; l++) {
                    	block[l] = 0;
                    	if (iniValue == 0) {
                    		if (l >= k) {
                    		    block[l] = 0;
                    		}
                    	} else if (iniValue == 1) {
                    		if (i <= k) {
                    		    block[l] = 0;
                    		}
                    	}
                    }
                    info.setRegionIndex(i);
                    info.setDimIndex(j);
                    info.setLvlBlockData(block);
                    mTvRatingSettingInfo.setOpenVCHIPSettingInfo(info);
                    mTvRatingSettingInfo.setAtscStorage(false);
                }
            }
        }
    }

    public void setOpenVChipSetting(int regionIndex, int dimIndex, int levIndex) {
        MtkTvOpenVCHIPSettingInfoBase info = getNewOpenVchipSetting(regionIndex, dimIndex);
        byte[] block = info.getLvlBlockData();
        int iniValue = block[levIndex];
        getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_GRAD);
        getOpenVCHIPPara().setRegionIndex(regionIndex);
        getOpenVCHIPPara().setDimIndex(dimIndex);
        getOpenVCHIPPara().setLevelIndex(levIndex);
        if(getOpenVchip().isDimGrad()){
            for (int i = 0; i < block.length; i++) {
                if (iniValue == 0) {
                    if (i >= levIndex) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i >= levIndex set block 1");
                        block[i] = 1;
                    }
                } else if (iniValue == 1) {
                    if (i <= levIndex) {
                        block[i] = 0;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i <= levIndex set block 0");
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "block[i]:" + block[i] + "---i:" + i);
            }
        }else {
            if (iniValue == 0) {
                block[levIndex] = 1;
            } else if (iniValue == 1) {
                block[levIndex] = 0;
            }
            for (int i = 0; i < block.length; i++) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "block[i]:" + block[i] + "---i:" + i);
            }
        }
        info.setRegionIndex(regionIndex);
        info.setDimIndex(dimIndex);
        info.setLvlBlockData(block);
        mTvRatingSettingInfo.setOpenVCHIPSettingInfo(info);
        mTvRatingSettingInfo.setAtscStorage(true);
    }

    public boolean isM7ScanMode() {
        int op = -1;
        if (CommonIntegration.getInstance().isPreferSatMode()) {
            op = getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SAT_BRDCSTER);
            return isM7ScanMode(op);
//              op = ScanContent.getDVBSCurroperator();
        } else {
            return false;
        }
    }
    public boolean isM7ScanMode(int op) {
        switch (op) {
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_AUSTRIASAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANALDIGITAAL_HD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANALDIGITAAL_SD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TV_VLAANDEREN_HD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TV_VLAANDEREN_SD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SYKLINK_CZ:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SYKLINK_SK:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELESAT_BELGIUM:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELESAT_LUXEMBOURG:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_HELLO:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DEUTSCHLAND:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_CZ:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_CZ_SD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_SK:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_SK_SD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_UPC_DIRECT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_UPC_DIRECT_SD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FOCUSSAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FOCUSSAT_SD:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_M7_FAST_SCAN:
            return true;

        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ORS:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANAL_DIGITAL:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NNK:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_TURKSAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_EUTELSAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FRANSAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CYFRA_PLUS:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CYFROWY_POLSAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_D_SMART:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ASTRA_INTERNATIONAL_LCN:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SMART_HD_PLUS:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NC_PLUS:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TIVU_SAT:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_56E:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_140E:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR_SIBERIA:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR_FAR_EAST:
        case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA_140E:
        default:
            return false;
        }
    }

    /*
     * scan notify
     */
    public boolean addScanCallBackListener(Handler listerner) {
        return mCallbackHandler.addCallBackListener(
                TvCallbackConst.MSG_CB_SCAN_NOTIFY, listerner);
    }

    public void removeCallBackListener(Handler listerner) {
        mCallbackHandler.removeCallBackListener(
                TvCallbackConst.MSG_CB_SCAN_NOTIFY, listerner);
    }

    /*
     * svctx notify
     */
    public boolean addSingleLevelCallBackListener(Handler listerner) {
        return mCallbackHandler.addCallBackListener(
                TvCallbackConst.MSG_CB_SVCTX_NOTIFY, listerner);
    }

    public void removeSingleLevelCallBackListener(Handler listerner) {
        mCallbackHandler.removeCallBackListener(
                TvCallbackConst.MSG_CB_SVCTX_NOTIFY, listerner);
    }

    /*
     * notify
     */
    public boolean addConfigCallBackListener(Handler listerner) {
        return mCallbackHandler.addCallBackListener(
                TvCallbackConst.MSG_CB_CONFIG, listerner);
    }

    public void removeConfigCallBackListener(Handler listerner) {
        mCallbackHandler.removeCallBackListener(
                TvCallbackConst.MSG_CB_CONFIG, listerner);
    }

    public boolean addCallBackListener(int msg, Handler listerner) {
        return mCallbackHandler.addCallBackListener(
                msg, listerner);
    }

    public void removeCallBackListener(int msg, Handler listerner) {
        mCallbackHandler.removeCallBackListener(
                msg, listerner);
    }

    public void stopTimeShift(){
        if (TifTimeShiftManager.getInstance()!=null) {
            if(isTshitRunning()){
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, e.getMessage());
                }
            }
            TifTimeShiftManager.getInstance().stopAll();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopTimeShift!!!!!!");

        }
    }

    public void startEURangleScan(int fromchanel, int tochannel) {
        stopTimeShift();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("startEURangleScan(),Range scan", "firstScanIndex:"
                + fromchanel + ",lastScanIndex:" + tochannel);
        RfInfo[] rfChannels = MtkTvScan.getInstance().getScanDvbtInstance().getAllRf();
        MtkTvScan.getInstance().getScanDvbtInstance()
                .startRangeScan(rfChannels[Math.min(fromchanel, tochannel)],
                        rfChannels[Math.max(fromchanel, tochannel)]);
    }

    public void startSaRangleScan(int fromchanel, int tochannel, String itemID) {
        stopTimeShift();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan", "firstScanIndex:" + fromchanel
                + ",lastScanIndex:" + tochannel + "itemID:" + itemID);
        ScanType mScanType = ScanType.SCAN_TYPE_ISDB;
        if (itemID.equals(MenuConfigManager.FACTORY_TV_RANGE_SCAN_DIG)) {
            mISDBScanPara.setStartIndex(fromchanel);
            mISDBScanPara.setEndIndex(tochannel);
            mATSCScanPara.setFreqPlan(0);
            mATSCScanPara.setModMask(0);
            mScan.setISDBScanParas(mISDBScanPara);
        } else {
            mScanType = ScanType.SCAN_TYPE_NTSC;
            mNTSCScanPara.setStartIndex(fromchanel);
            mNTSCScanPara.setEndIndex(tochannel);
            mATSCScanPara.setFreqPlan(0);
            mATSCScanPara.setModMask(0);
            mScan.setNTSCScanParas(mNTSCScanPara);
        }
        mScan.startScan(mScanType, ScanMode.SCAN_MODE_RANGE, true);
    }

    public void startUsRangeScan(int fromchanel, int tochannel) {
        stopTimeShift();
        int mScanMode = getUSRangeScanMode();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan", "fromchanel:" + fromchanel + ",tochannel:"
                + tochannel + "mScanMode:" + mScanMode);
        ScanType mScanType = ScanType.SCAN_TYPE_US;
        if (mScanMode == 0) {
            mATSCScanPara.setStartIndex(fromchanel);
            mATSCScanPara.setEndIndex(tochannel);
            mATSCScanPara.setFreqPlan(0);
            mATSCScanPara.setModMask(0);
            mScan.setATSCScanParas(mATSCScanPara);

            mNTSCScanPara.setStartIndex(fromchanel);
            mNTSCScanPara.setEndIndex(tochannel);
            mScan.setNTSCScanParas(mNTSCScanPara);

            mScanType = ScanType.SCAN_TYPE_US;
        } else if (mScanMode == 1) {
            mNTSCScanPara.setStartIndex(fromchanel);
            mNTSCScanPara.setEndIndex(tochannel);
            mATSCScanPara.setFreqPlan(0);
            mATSCScanPara.setModMask(0);
            mScan.setNTSCScanParas(mNTSCScanPara);

            mScanType = ScanType.SCAN_TYPE_NTSC;
        } else {
            mATSCScanPara.setStartIndex(fromchanel);
            mATSCScanPara.setEndIndex(tochannel);
            mATSCScanPara.setFreqPlan(0);
            mATSCScanPara.setModMask(0);
            mScan.setATSCScanParas(mATSCScanPara);

            if (getCurrentTunerMode() == 0) {
                mScanType = ScanType.SCAN_TYPE_ATSC;
            } else {
                mScanType = ScanType.SCAN_TYPE_CQAM;
            }

        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan", "mScanType:" + mScanType);
        mScan.startScan(mScanType, ScanMode.SCAN_MODE_RANGE, true);
    }

    public void startUsSaSingleScan(int rfchannel) {
        stopTimeShift();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan startUsSingleScan", "rfchannel:" + rfchannel);
        if (CommonIntegration.isUSRegion()) {
            mATSCScanPara.setStartIndex(rfchannel);
            mATSCScanPara.setEndIndex(rfchannel);
            ScanType mScanType = ScanType.SCAN_TYPE_US;
            if (getCurrentTunerMode() == 0) {
                mScanType = ScanType.SCAN_TYPE_ATSC;
                mATSCScanPara.setFreqPlan(0);
                mATSCScanPara.setModMask(0);
            } else {
                int plan = saveV.readValue(MenuConfigManager.FREQUENEY_PLAN) + 1;
                int modMask = saveV.readValue(MenuConfigManager.TV_SINGLE_SCAN_MODULATION) + 2;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan startUsSingleScan", "setModMask:" + plan + ">>>" + modMask);
                mScanType = ScanType.SCAN_TYPE_CQAM;
                mATSCScanPara.setFreqPlan(plan);
                mATSCScanPara.setModMask(modMask);
            }
            mScan.setATSCScanParas(mATSCScanPara);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan mScanType", "mScanType:" + mScanType);
            mScan.startScan(mScanType, ScanMode.SCAN_MODE_SINGLE_RF_CHANNEL, true);
        } else if (CommonIntegration.isSARegion()) {
            mISDBScanPara.setStartIndex(rfchannel);
            mISDBScanPara.setEndIndex(rfchannel);
            ScanType mScanType = ScanType.SCAN_TYPE_ISDB;
            mScan.setISDBScanParas(mISDBScanPara);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("Range scan mScanType", "mScanType:" + mScanType);
            mScan.startScan(mScanType, ScanMode.SCAN_MODE_SINGLE_RF_CHANNEL, true);
        }
    }

    public int getUSRangeScanMode() {
        return saveV.readValue(MenuConfigManager.US_SCAN_MODE);
    }

    public int getChannelIDByRFIndex(int rfIndex) {
        if (CommonIntegration.isUSRegion()) {
            return mScan.getScanATSCInstance().getChannelIDByRFIndex(rfIndex);
        } else if (CommonIntegration.isSARegion()) {
            return mScan.getScanISDBInstance().getChannelIDByRFIndex(rfIndex);
        }
        return 0;
    }

    public int getRFScanIndex() {
        if (CommonIntegration.isUSRegion()) {
            return mScan.getScanATSCInstance().getCurrentRFIndex();
        } else if (CommonIntegration.isSARegion()) {
            return mScan.getScanISDBInstance().getCurrentRFIndex();
        }
        return 0;
    }

    public int getFirstScanIndex() {
        if (CommonIntegration.isUSRegion()) {
            return mScan.getScanATSCInstance().getFirstScanIndex();
        } else if (CommonIntegration.isSARegion()) {
            return mScan.getScanISDBInstance().getFirstScanIndex();
        } else if (CommonIntegration.isEURegion()) {
            return 0;
        }
        return 0;
    }

    public int getLastScanIndex() {
        if (CommonIntegration.isUSRegion()) {
            return mScan.getScanATSCInstance().getLastScanIndex();
        } else if (CommonIntegration.isSARegion()) {
            return mScan.getScanISDBInstance().getLastScanIndex();
        } else if (CommonIntegration.isEURegion()) {
            if (MtkTvScan.getInstance().getScanDvbtInstance().getAllRf() != null) {
                return MtkTvScan.getInstance().getScanDvbtInstance().getAllRf().length - 1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    public int startScan(ScanType scanType, ScanMode mode, Object param) {
        stopTimeShift();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "startScan:");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "mScanType:" + scanType);

        Throwable tr = new Throwable();
        Log.getStackTraceString(tr);
        tr.printStackTrace();

        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_US:
                scanType = ScanType.SCAN_TYPE_US;
                mode = ScanMode.SCAN_MODE_FULL;
                return 0;
            case MarketRegionInfo.REGION_CN:
                scanType = ScanType.SCAN_TYPE_ISDB;
                mode = ScanMode.SCAN_MODE_FULL;
                return 0;
            case MarketRegionInfo.REGION_SA:
                scanType = ScanType.SCAN_TYPE_ISDB;
                mode = ScanMode.SCAN_MODE_FULL;
                return 0;
            case MarketRegionInfo.REGION_EU:
                if (param == null) {
                    mScan.startScan(scanType, mode, true);
                    setSimpleScanParam(new ScanSimpleParam(scanType, mode));
                    return 0;
                }
                switch (scanType) {
                    case SCAN_TYPE_PAL:
                        break;
                    case SCAN_TYPE_DVBC:
                        if (param instanceof MtkTvDvbcScanParaBase) {
                            mScan.setDvbcScanParas((MtkTvDvbcScanParaBase) param);
                        }
                        break;
                    case SCAN_TYPE_NTSC:
                        if (param instanceof MtkTvNTSCScanParaBase) {
                            mScan.setNTSCScanParas((MtkTvNTSCScanParaBase) param);
                        }
                        break;
                    case SCAN_TYPE_ATSC:
                        if (param instanceof MtkTvATSCScanParaBase) {
                            mScan.setATSCScanParas((MtkTvATSCScanParaBase) param);
                        }
                        break;
                    case SCAN_TYPE_ISDB:
                        if (param instanceof MtkTvISDBScanParaBase) {
                            mScan.setISDBScanParas((MtkTvISDBScanParaBase) param);
                        }
                        break;
                    case SCAN_TYPE_CQAM:
                        if (param instanceof MtkTvCQAMScanParaBase) {
                            mScan.setCQAMScanParas((MtkTvCQAMScanParaBase) param);
                        }
                        break;
                    case SCAN_TYPE_DVBT2:

                        break;
                    case SCAN_TYPE_DVBS:
                        if (param instanceof MtkTvDvbsSatelliteSettingBase) {
                            mScan.setSatelliteSetting((MtkTvDvbsSatelliteSettingBase) param);
                        }
                        break;
                    case SCAN_TYPE_DTMB:
                        break;
                    case SCAN_TYPE_US:
                        break;
                    case SCAN_TYPE_NUM:
                        break;
                        default:
                        	break;
                }
                mScan.startScan(scanType, mode, false);
                setSimpleScanParam(new ScanSimpleParam(scanType, mode));
                return 0;
                default:
                	break;
        }

        mScan.startScan(scanType, mode, true);
        setSimpleScanParam(new ScanSimpleParam(scanType, ScanMode.SCAN_MODE_FULL));
        return 0;
    }

    public void forOnlyEUdvbtCancescan() {
        MtkTvScan.getInstance().getScanDvbtInstance().cancelScan();
    }

    public int cancelScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan()");
        setSimpleScanParam(null);
        mScanManager.cancelScan();
        return 0;
    }

    public void setScanListener(ScannerListener listener) {
        mScanManager.setListener(listener);
        if(listener != null) {
            RxBus.instance.onEvent(ActivityDestroyEvent.class)
                .filter(it -> {
                    return listener != null && it.activityClass == listener.getClass();
                })
                .firstElement()
                .doOnSuccess(it -> {
                    Log.i(TAG, "Rxbus free");
                    mScanManager.setListener(null);
                }).subscribe();
        }

    }

    /*
     * ACFG Function
     */
    public int getMinValue(String cfgId) {
        if (dumy) {
            return 0;
        } else {
            int value = mTvConfig.getMinMaxConfigValue(cfgId);
            return MtkTvConfig.getMinValue(value);
        }
    }

    public int getMaxValue(String cfgId) {
        if (dumy) {
            return 100;
        } else {
            int value = mTvConfig.getMinMaxConfigValue(cfgId);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "value:" + value);
            return MtkTvConfig.getMaxValue(value);
        }
    }

    public int getConfigValue(String cfgId) {
        int value = mTvConfig.getConfigValue(cfgId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "getConfigValue(cfgId):" + value + "cfgId:" + cfgId);
        return value;
    }

    public String getConfigString(String cfgId) {
        return mTvConfig.getConfigString(cfgId);
    }

    public void setConfigValue(String cfgId, int value) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "setConfigValue cfgId:" + cfgId + "----value:"
                + value);
        if (dumy) {
            dumyData.put(cfgId, value);
        } else {
            if (cfgId.equalsIgnoreCase(MtkTvConfigType.CFG_VIDEO_VID_MJC_DEMO)) {
                mTvConfig.setConfigValue(cfgId, value, 1);
            } else {
                mTvConfig.setConfigValue(cfgId, value);
            }
        }
    }

    public void setConfigValue(String cfgId, int value, boolean isUpate) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "setConfigValue cfgId:" + cfgId + "----value:"
                + value+"isUpate:"+isUpate);
        if (dumy) {
            dumyData.put(cfgId, value);
        } else {
            int update = 0;
            if (isUpate) {
                update = 1;
            }
            mTvConfig.setConfigValue(cfgId, value, update);
        }
    }
    public void setConfigValue(String cfgId, int value, int setFlag) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "setConfigValue cfgId:" + cfgId + "----value:"
                + value+" setFlag:"+setFlag);
        if (dumy) {
            dumyData.put(cfgId, value);
        } else {
            mTvConfig.setConfigValue(cfgId, value, setFlag);
        }
    }

    public boolean isConfigEnabled(String cfgId) {
        return mTvConfig.isConfigEnabled(cfgId) == MtkTvConfigType.CFGR_ENABLE ? true
                : false;
    }

    public String getCurrentInputSourceName() {
        // String sourcename = "tv";fix CR DTV00580677 & DTV00581180 &
        // DTV00581188
        String sourcename = InputSourceManager.getInstance()
                .getCurrentInputSourceName(
                        CommonIntegration.getInstance().getCurrentFocus());
        lastInputSourceName = currInputSourceName;
        currInputSourceName = sourcename;
        return sourcename;
    }

    // public String getCurrInputSourceNameByApi() {
    // String focus = CommonIntegration.getInstance().getCurrentFocus();
    // return InputSourceManager.getInstance().getInputSourceByAPI(focus);
    // }

    public boolean isCurrentSourceTv() {
        return CommonIntegration.getInstance().isCurrentSourceTv();
    }

    public boolean isCurrentSourceVGA() {
        return CommonIntegration.getInstance().isCurrentSourceVGA();
    }

    public boolean isCurrentSourceHDMI() {
       return CommonIntegration.getInstance().isCurrentSourceHDMI();
    }

    // fix CR DTV00580462
    public boolean iCurrentInputSourceHasSignal() {
        return isSignalLoss() ? false : true;
    }

    public boolean isCurrentSourceComponent() {
        String sourceName = InputSourceManager.getInstance()
                .getCurrentInputSourceName(
                        CommonIntegration.getInstance().getCurrentFocus());
        return sourceName
                .equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_COMPONENT);
    }

    public boolean isCurrentSourceComposite() {
        String sourceName = InputSourceManager.getInstance()
                .getCurrentInputSourceName(
                        CommonIntegration.getInstance().getCurrentFocus());
        return sourceName
                .equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_COMPOSITE);
    }

    public boolean isCurrentSourceDTV() {
        return CommonIntegration.getInstance().isCurrentSourceDTV();
    }

    public boolean isCurrentSourceATV() {
        return CommonIntegration.getInstance().isCurrentSourceATV();
    }

    public boolean isCurrentSourceScart() {
        String sourceName = InputSourceManager.getInstance()
                .getCurrentInputSourceName(
                        CommonIntegration.getInstance().getCurrentFocus());
        return sourceName
                .equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_SCART);
    }

    public boolean isCurrentSourceBlocking() {
        return InputSourceManager.getInstance().isCurrentSourceBlocked(
                CommonIntegration.getInstance().getCurrentFocus());
    }

    public boolean isTvInputBlock() {
        return CommonIntegration.getInstance().isMenuInputTvBlock();
    }

    public int getCurrentTunerMode() {
        return getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC);
    }

    public boolean isAnalog(MtkTvChannelInfoBase channel) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "isAnalog\n");
        if (channel instanceof MtkTvAnalogChannelInfo) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "isAnalog yes\n");
            return true;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "isAnalog no\n");
        return false;
    }

    public boolean isHaveScreenMode() {
        boolean flag = isConfigVisible(MenuConfigManager.SCREEN_MODE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "isHaveScreenMode flag:" + flag);
        return flag;
    }

    /**
     * check is config visible
     *
     * @return
     */
    public boolean isConfigVisible(String cfgid) {
        return mTvConfig.isConfigVisible(cfgid) == MtkTvConfigType.CFGR_VISIBLE ? true : false;
    }

    public boolean isFilmModeEnabled() {
//        if (isConfigEnabled(MenuConfigManager.GAME_MODE)) {
//            return false;
//        }
        return false;
    }

    /**
     * Get current signal level
     *
     * @return true(no signal)/false(with signal)
     */
    public boolean isSignalLoss() {

        boolean hasSignal = false;
        hasSignal = MtkTvBroadcast.getInstance().isSignalLoss();

        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "isSignalLoss()?," + hasSignal);
        return hasSignal;
    }

    public boolean isTsLocked() {
      int ret = MtkTvBroadcast.getInstance().getConnectAttr(
                  CommonIntegration.getInstance().getCurrentFocus(),
                  MtkTvBroadcast.BRDCST_CM_CTRL_GET_TS_LOCK_STATUS);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lock:"+ret);
      return ret == 1;
    }

    /**
     * Get current signal level
     *
     * @return 0-100
     */
    public int getSignalLevel() {

        return MtkTvBroadcast.getInstance().getSignalLevel();
    }

    /**
     * Get current signal Quality
     *
     * @return 0-100
     */
    public int getSignalQuality() {

        return MtkTvBroadcast.getInstance().getSignalQuality();
    }

    public void updatePowerOn(String cfgID, int enable, String date) {
        int daySec = onTimeModified(date);
        int timerValue = ((((((enable)) & 0x01) << 31) & 0x80000000) | ((daySec) & 0x0001ffff));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "timerValue:" + timerValue + "cfgID:" + cfgID);
        mTvConfig.setConfigValue(cfgID, timerValue);
    }

    public void updatePowerOff(String cfgID, int enable, String date) {
        int daySec = onTimeModified(date);
        int timerValue = ((((((enable)) & 0x01) << 31) & 0x80000000) | ((daySec) & 0x0001ffff));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "timerValue:" + timerValue + "cfgID:" + cfgID);
        mTvConfig.setConfigValue(cfgID, timerValue);
    }

    public int onTimeModified(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(3, 5));

        //post process
        if (0 == VendorProperties.mtk_system_timesync_existed().orElse(0)) {
        TimeZone tz = TimeZone.getTimeZone("Etc/UTC");
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);


        cal.add(Calendar.HOUR_OF_DAY, 0 - tz.getRawOffset()/3600/1000);
        long mills = cal.getTimeInMillis();
        MtkTvTimeFormatBase tb = new MtkTvTimeFormatBase();
        tb.setByUtcAndConvertToLocalTime( mills / 1000);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d("{DT}{onTimeModified}", "Hour:" + tb.hour + "  Min:" + tb.minute);
        hour = tb.hour;
        minute = tb.minute;

       }
        return hour * 3600 + minute * 60;// + second;
    }

    public void setTimeInterval(int value) {
        mTvConfig.setConfigValue(MenuConfigManager.PARENTAL_CFG_RATING_BL_TYPE,
                value);
    }

    public void setTimeIntervalTime(String cfgID, String date) {
        int daySec = onTimeModified(date);
        mTvConfig.setConfigValue(cfgID, daySec * 1000);
    }

    /*
     * true is right,false is left
     */
    public int setSleepTimer(boolean direction) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "direction:" + direction);
        int valueIndex = 0;
        int leftmill = getSleepTimerRemaining();
        int mill = MtkTvTime.getInstance().getSleepTimer(direction);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "leftmill:" + leftmill + ",mill==" + mill);
        if (leftmill > 0) {
            int minute = leftmill / 60;
            if (minute >= 1 && minute < 9) {
                valueIndex = direction ? 1 : 0;
            } else if (minute >= 9 && minute < 11) {
                valueIndex = direction ? 2 : 0;
            } else if (minute >= 11 && minute < 19) {
                valueIndex = direction ? 2 : 1;
            } else if (minute >= 19 && minute < 21) {
                valueIndex = direction ? 3 : 1;
            } else if (minute >= 21 && minute < 29) {
                valueIndex = direction ? 3 : 2;
            } else if (minute >= 29 && minute < 31) {
                valueIndex = direction ? 4 : 2;
            } else if (minute >= 31 && minute < 39) {
                valueIndex = direction ? 4 : 3;
            } else if (minute >= 39 && minute < 41) {
                valueIndex = direction ? 5 : 3;
            } else if (minute >= 41 && minute < 49) {
                valueIndex = direction ? 5 : 4;
            } else if (minute >= 49 && minute < 51) {
                valueIndex = direction ? 6 : 4;
            } else if (minute >= 51 && minute < 59) {
                valueIndex = direction ? 6 : 5;
            } else if (minute >= 59 && minute < 61) {
                valueIndex = direction ? 7 : 5;
            } else if (minute >= 61 && minute < 89) {
                valueIndex = direction ? 7 : 6;
            } else if (minute >= 89 && minute < 91) {
                valueIndex = direction ? 8 : 6;
            } else if (minute >= 91 && minute < 119) {
                valueIndex = direction ? 8 : 7;
            } else if (minute >= 119 && minute <= 120) {
                valueIndex = direction ? 0 : 7;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVContent", "minute:" + minute + "valueIndex:"
                    + valueIndex);
        } else {
            switch (mill / 60) {
                case 10:
                    valueIndex = 1;
                    break;
                case 20:
                    valueIndex = 2;
                    break;
                case 30:
                    valueIndex = 3;
                    break;
                case 40:
                    valueIndex = 4;
                    break;
                case 50:
                    valueIndex = 5;
                    break;
                case 60:
                    valueIndex = 6;
                    break;
                case 90:
                    valueIndex = 7;
                    break;
                case 120:
                    valueIndex = 8;
                    break;
                default:
                    valueIndex = 0;
                    break;
            }
        }
        return valueIndex;
    }

    /*
     * true is right,false is left
     */
    public int getSleepTimerRemaining() {
        return MtkTvTime.getInstance().getSleepTimerRemainingTime();
    }

    public void resetConfigValues() {
        mTvConfig.resetConfigValues(MtkTvConfigType.CFGU_FACTORY_RESET_ALL);
        // mTvConfig.resetConfigValues(MtkTvConfigType.CFGU_AUDIO_ITEMS);
        // mTvConfig.resetConfigValues(MtkTvConfigType.CFGU_SCREEN_ITEMS);
    }

    public void resetPub(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e("gitmaster", "resetPub........." + System.currentTimeMillis());
                MtkTvUtil.getInstance().resetPub();
                com.mediatek.wwtv.tvcenter.util.MtkLog.e("gitmaster", "resetPub ended........." + System.currentTimeMillis());
                final int msagSendReset = 0x23;
                handler.sendEmptyMessage(msagSendReset);
            }
        }).start();
        // TimeShiftManager.forceSetToDefault(); temp for biaoqinggao
    }

    public void resetPri(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "resetPri........." + System.currentTimeMillis());
                MtkTvUtil.getInstance().resetPri();
                final int msagSendReset = 0x23;
                handler.sendEmptyMessage(msagSendReset);
            }
        }).start();
        // TimeShiftManager.forceSetToDefault(); temp for biaoqinggao
    }

    public ScanSimpleParam getSimpleScanParam() {
        if (mScanSimpleParam == null) {
            return new ScanSimpleParam(null, null);
        }
        return mScanSimpleParam;
    }

    public void setSimpleScanParam(ScanSimpleParam mScanSimpleParam) {
        this.mScanSimpleParam = mScanSimpleParam;
    }

    public String getRFChannel(int type) {
        return DVBTScanner.getRFChannel(type,mContext);
    }

    public String getCNRFChannel(int type) {
        return DVBTCNScanner.getScanInstance().getCNRFChannel(type);
    }

    public String getRFChannelByIndex(int index) {
        return MtkTvScan.getInstance().getScanDvbtInstance().getAllRf()[index].rfChannelName;
    }

    public String[] getDvbtAllRFChannels() {
        RfInfo[] rfChannels = MtkTvScan.getInstance().getScanDvbtInstance().getAllRf();

        String[] rfStrChannels = new String[rfChannels.length];
        for (int i = 0; i < rfStrChannels.length; i++) {
            rfStrChannels[i] = rfChannels[i].rfChannelName;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllRFChannels()," + rfStrChannels.length);

        return rfStrChannels;
    }

    public static class ScanSimpleParam {
        public ScanType type = null;
        public static ScanMode mode;

        public ScanSimpleParam(ScanType type, ScanMode mode) {
            super();
            this.type = type;
            this.mode = mode;// NOPMD
        }

    }

    public int getActionSize() {
        return mScanManager.getActionList().size();
    }

    public void clearActionList() {
        mScanManager.clearActionList();
    }

    public boolean hasOPToDo() {
        return mScanManager.hasOPToDo();
    }

    public void uiOpEnd() {
        mScanManager.uiOpEnd();
    }

    public int getOriginalActionSize() {
        return mScanManager.getActionList().totalScanActionSize;
    }

    public void startDVBSScanTask(ScannerListener listener) {
        stopTimeShift();

        mScanManager.startScan(ScannerManager.DVBS_MULTI_SAT, listener,
                mScanManager.getDVBSDataParams());
    }

    public void startOtherScanTask(ScannerListener listener) {
        stopTimeShift();
        if (mScanManager.getActionList().size() <= 0) {
            listener.onCompleted(ScannerListener.COMPLETE_OK);
            return;
        }

        ScannerManager.Action action = mScanManager.getActionList().get(0);

        switch (action) {
            case DTV:
                mScanManager.startScan(ScannerManager.DTV_SCAN, listener,
                        mScanManager.getstoredParams());
                break;
            case ATV:
                mScanManager.startScan(ScannerManager.ATV_SCAN, listener, null);
                break;
            case DTV_UPDATE:
                mScanManager.startScan(ScannerManager.DTV_UPDATE, listener, null);
                break;
            case ATV_UPDATE:
                mScanManager.startScan(ScannerManager.UPDATE_ATV_SCAN, listener,
                        null);
                break;
            case SA_ATV_UPDATE:
                startScanByScanManager(ScannerManager.SA_ATV_UPDATE, listener, null);
                break;
            case SA_DTV_UPDATE:
                startScanByScanManager(ScannerManager.SA_DTV_UPDATE, listener, null);
                break;
                default:
                	break;
        }

        if (action.equals(ScannerManager.Action.DTV) || action.equals(ScannerManager.Action.ATV)) {
            mScanManager.setRollback(true);
        }
    }

    public void startSingleRFScan(int rfChannel, ScannerListener listener) {
        stopTimeShift();
        ScanParams params = new ScanParams();
        params.singleRFChannel = rfChannel;
        mScanManager.startScan(ScannerManager.SIGNAL_RF_SCAN, listener, params);
    }

    public void startAnalogScanUpOrDown(boolean isScanUp, int frequence,
            ScannerListener listener) {
        stopTimeShift();
        ScanParams params = new ScanParams();
        params.freq = frequence;
        if (isScanUp) {
            mScanManager.startScan(ScannerManager.ATV_SCAN_UP, listener, params);
        } else {
            mScanManager.startScan(ScannerManager.ATV_SCAN_DOWN, listener,
                    params);
        }
    }

    public void startATVAutoOrUpdateScan(int type, ScannerListener listener, ScanParams params) {
        stopTimeShift();
        mScanManager.startScan(type, listener, params);
    }

    public static void clearChannelDB() {
        int svl = CommonIntegration.getInstance().getSvl();
        if (svl == 3 || svl == 4 || svl == 7) {
            MtkTvChannelListBase.cleanChannelList(svl, false);
        } else {
            MtkTvChannelListBase.cleanChannelList(svl);
        }
        MtkTvConfig.getInstance().setConfigValue(
                MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
    }
    public void clearCurrentSvlChannelDB() {
        clearCurrentSvlChannelDB(false);
    }
    public void clearCurrentSvlChannelDB(boolean clearLOL) {
        int svl = CommonIntegration.getInstance().getSvl();
        if (CommonIntegration.isCNRegion()) {
          int brdType = 7;
          if (svl == 1) {
            brdType = 7;// DTMB
          } else {
            if (InputSourceManager.getInstance().isCurrentATvSource(
                CommonIntegration.getInstance().getCurrentFocus())) {
              brdType = 1;// ATV
            } else if (InputSourceManager.getInstance().isCurrentDTvSource(
                CommonIntegration.getInstance().getCurrentFocus())) {
              brdType = 2;// DVB
            }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "brd_type:" + brdType);
          MtkTvChannelListBase mtkchLst = new MtkTvChannelListBase();
          mtkchLst.deleteChannelByBrdcstType(svl, brdType);
        } else if (CommonIntegration.isEUPARegion()) {
            if (svl == 1) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Svl == 1,Dvbt cleanChannelList");
                MtkTvChannelListBase.cleanChannelList(svl);
            }else {
                int brdType = 1;
                if (InputSourceManager.getInstance().isCurrentATvSource(
                    CommonIntegration.getInstance().getCurrentFocus())) {
                  brdType = 1;// ATV
                } else if (InputSourceManager.getInstance().isCurrentDTvSource(
                    CommonIntegration.getInstance().getCurrentFocus())) {
                  brdType = 2;// DVB
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "brd_type:" + brdType);
                MtkTvChannelListBase mtkchLst = new MtkTvChannelListBase();
                mtkchLst.deleteChannelByBrdcstType(svl, brdType);
            }

        } else {
          if (svl == 3 || svl == 4 || svl == 7) {
            MtkTvChannelListBase.cleanChannelList(svl, clearLOL);
          } else {
            MtkTvChannelListBase.cleanChannelList(svl);
          }
        }
        MtkTvConfig.getInstance().setConfigValue(
            MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
      }
    public void startScanByScanManager(int scanType,
            ScannerListener mListener, ScanParams param) {
        stopTimeShift();
        if(param == null) {
          param = new ScanParams();
        }
        mScanManager.startScan(scanType, mListener, param);
    }

    public boolean isScanTaskFinish() {
        return mScanManager.isScanTaskFinish();
    }

    public boolean isScanning() {
        return MtkTvScan.getInstance().isScanning();
    }

    public boolean isTshitRunning() {
        // Fix CR DTV00583447
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "isTshitRunning:" + saveV.readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START));
        return saveV.readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START);
    }

    public boolean isPVrRunning() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isPVRRunning:" + saveV.readBooleanValue(MenuConfigManager.PVR_START));
        return saveV.readBooleanValue(MenuConfigManager.PVR_START);
    }

    public boolean isCanScan() {
        boolean pvrStart = SaveValue.readWorldBooleanValue(mContext, "pvr_start");
        boolean timeshiftStart = saveV.readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START);
        boolean pvrPlaybackStart = SaveValue.readWorldBooleanValue(mContext, "pvr_playback_start");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pvrStart:"+pvrStart+",timeshiftStart:"+timeshiftStart+",pvrPlaybackStart:"+pvrPlaybackStart);
        return pvrStart || timeshiftStart || pvrPlaybackStart;
    }

    /*public int getDefaultNetWorkID() {
        // TODO Auto-generated method stub
        return 104000;
    }*/

    public ScannerManager getScanManager() {
        return mScanManager;
    }

    public int updateCIKey() {
        return mCIBase.updateCIKey();
    }
     /**
     *
     */
    public int updateCIECPKey(){
        return mCIBase.updateCIKeyEx(1);
    }

    public int eraseCIKey() {
        return mCIBase.eraseCIKey();
    }

    public String getCIKeyinfo() {
        return mCIBase.getCIKeyinfo();
    }

    /*
     * for judge is NordicCountry
     * @return
     */
    public boolean isNordicCountry(){
    String currentCountry = mTvConfig.getCountry();
    for (String country : mNordicCountry) {
        if (country.equals(currentCountry)) {
            return true;
            }
        }
        return false;
    }

    public boolean isShowCountryRegion() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isShowCountryRegion country*****************" + country);
        boolean isShow = false;
        if (country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUS)
                || country
                        .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ESP)
                || country.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_POR)
                ||country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_PRT)) {
            if (country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ESP)) {
                if (getConfigValue(MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS) == 1) {
                    isShow = true;
                }
            } else {
                isShow = true;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isShowCountryRegion*****************" + isShow);
        return isShow;
    }

    public boolean isAusCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUS);
    }
    //Austria
    public boolean isAutCountry() {
        return mTvConfig.getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUT);
    }
    //Hungary
    public boolean isHunCountry() {
        return mTvConfig.getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_HUN);
    }
    /**
     * is Italy
     * @return
     */
    public boolean isItaCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ITA);
    }

    public boolean isEcuadorCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"country:"+country);
        return "ecu".equalsIgnoreCase(country);
    }
    public boolean isPhiCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"country:"+country);
        return "phl".equalsIgnoreCase(country);
    }
    public boolean isNorCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NOR);
    }

    public boolean isKoreaCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isUSRegion()
                && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_KOR);
    }

    /**
     * 0:Auto,1:Custom,2:Off
     *
     * @return
     */
    public int getTKGSOperatorMode() {
      int ret = 0;
      ret = mTvConfig.getConfigValue(MtkTvConfigType.CFG_MISC_TKGS_OPERATING_MODE);
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "TKGSOperatormode==" + ret);
      return ret;
    }

    public boolean isTKGSOperator() {
        int op = mTvConfig.getConfigValue(ScanContent.SAT_BRDCSTER);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isTKGSOperator,now op==" + op);
        if (op == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isTKGSOperator,true");
          return true;
        }
        return false;
      }

    // is BEL
    public boolean isBelgiumCountry() {
        String country = mTvConfig.getCountry();
        return country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_BEL);
      }
    // is turkey
    public boolean isTurkeyCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
            && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_TUR);
      }

    //is Romania
    public boolean isRomCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ROU);
    }

    // Spain
    public boolean isEspCountry(){
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ESP);
    }

    public boolean isSgpCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_SQP);
      }

    public boolean isSouthAfricaCountry() {
        String country = mTvConfig.getCountry();
            return country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ZAF);
    }

    // is france
    public boolean isFraCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FRA);
    }
    //is indonesia
    public boolean isIDNCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_IDN);
    }

    //is MYS
    public boolean isMYSCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_MYS);
    }

    //is New Zealan
    public boolean isNZLCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL);
    }

    //is Austraila
    public boolean isAUSCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUS);
    }

    //is Finland
    public boolean isFinCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FIN);
    }

    //is Singapore
    public boolean isSQPCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_SQP);
    }

    //is Thailand
    public boolean isTHACountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_THA);
    }

    //is vietnam
    public boolean isVNMCountry() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_VNM);
    }

    public boolean isUKCountry() {
        String country = mTvConfig.getCountry();
        return CommonIntegration.isEURegion()
                && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_GBR);
    }

    public boolean isSupportFvpScan() {
        boolean network = isNetworkConnected();
        int tou = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_MISC_MDS_TOU_STATE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "network:"+network+",tou:"+tou);
        return isUKCountry() && network && tou == 1;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager com = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = com.getActiveNetworkInfo();
        if(nInfo != null){
            return nInfo.isAvailable();
        }
        return false;
    }

    public boolean isCOLRegion() {//two country COL and PAN
        return  MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL);
    }

    public boolean isHKRegion() {
        String country = mTvConfig.getCountry();
        return country.equals(MtkTvConfigTypeBase.S3166_CFG_COUNT_HKG);
    }

    //is TWN
    public boolean isChineseTWN() {
        String country = mTvConfig.getCountry();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country:"+country);
        return MarketRegionInfo.isEU_PA_Region()
                && country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_TWN);
    }
  
 //issupport oneimage

    public boolean isSupportOneImage(){

    	return MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_ONE_IMAGE);
    }

    public  boolean isOneImageTWN(){

    	return  isSupportOneImage()&&(MarketRegionInfo.isEU_PA_Region()&&isChineseTWN());
    }

    public boolean isOneImagePHL(){
    	return isSupportOneImage()&&((MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion())
        		&&isPhiCountry());
    }
    public static int snapshotID = -1;
    public static int dvbsLastOP = -1;
    public static int mDvbsSatSnapShotId = -1;

    public static void createChanneListSnapshot() {
        releaseChanneListSnapshot();
        if (CommonIntegration.getInstance().getChannelAllNumByAPI() > 0) {
            snapshotID = MtkTvChannelListBase.createSnapshot(CommonIntegration.getInstance()
                    .getSvl());
        }
    }

    public static void restoreChanneListSnapshot() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        if (snapshotID != -1) {
            MtkTvChannelListBase.restoreSnapshot(snapshotID);
        }
    }

    public static void releaseChanneListSnapshot() {
        if (snapshotID != -1) {
            MtkTvChannelListBase.freeSnapshot(snapshotID);
            snapshotID = -1;
        }
    }

    public static void backUpDVBSOP() {
        dvbsLastOP = -1;
        int tunerMode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
        if (tunerMode >= CommonIntegration.DB_SAT_OPTID) {
            int currentOP = ScanContent.getDVBSCurrentOP();
            if (currentOP != 0) {
                dvbsLastOP = currentOP;
            }
        }
    }

    public static void restoreDVBSOP() {
        if (dvbsLastOP != -1) {
            MtkTvConfig.getInstance().setConfigValue(ScanContent.SAT_BRDCSTER, dvbsLastOP);
        }
    }

    public static void freeBackupDVBSOP() {
        dvbsLastOP = -1;
    }

    public static void backUpDVBSsatellites() {
        freeBachUpDVBSsatellites();
        int svlID = CommonIntegration.getInstance().getSvl();
        mDvbsSatSnapShotId = MtkTvDvbsConfigBase.createSatlSnapshot(svlID);
    }

    public static void restoreDVBSsatellites() {
        if (mDvbsSatSnapShotId != -1) {
            MtkTvDvbsConfigBase.restoreSatlSnapshot(mDvbsSatSnapShotId);
        }
    }

    public static void freeBachUpDVBSsatellites() {
        if (mDvbsSatSnapShotId != -1) {
            MtkTvDvbsConfigBase.freeSatlSnapshot(mDvbsSatSnapShotId);
            mDvbsSatSnapShotId = -1;
        }
    }

    public boolean selectScanedRFChannel() {
        int frequency = 0;
        if (CommonIntegration.isEURegion()) {
            if (getScanManager().isDVBTSingleRFScan()) {
                frequency = DVBTScanner.selectedRFChannelFreq;
            } else {
                frequency = DVBCScanner.selectedRFChannelFreq;
            }
        } else if (CommonIntegration.isCNRegion()) {
            if (getScanManager().isDVBTSingleRFScan()) {
                frequency = DVBTCNScanner.selectedRFChannelFreq;
            } else {
                frequency = DVBCCNScanner.selectedRFChannelFreq;
            }
        }
        if(TurnkeyUiMainActivity.getInstance() != null){
        	TurnkeyUiMainActivity.getInstance().resetLayout();
        }
        return changeChannelByQueryFreq(frequency);
    }

    public boolean changeChannelByFreq(int frequency) {
        int length = CommonIntegration.getInstance().getChannelActiveNumByAPIForScan();
        if (CommonIntegration.isCNRegion()) {
            length = CommonIntegration.getInstance().getCurrentSvlChannelNum(
                    TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
        }
        List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration.getInstance()
                .getChannelListByMaskFilter(0, MtkTvChannelListBase.CHLST_ITERATE_DIR_FROM_FIRST,
                        length,
                        TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
        for (MtkTvChannelInfoBase mtkTvChannelInfoBase : channelBaseList) {
            if (mtkTvChannelInfoBase.getFrequency() <= frequency + 2500000
                    && mtkTvChannelInfoBase.getFrequency() >= frequency - 250000) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeChannelByFreq channel id = "+mtkTvChannelInfoBase.getChannelId());
                CommonIntegration.getInstance().selectChannelById(
                		mtkTvChannelInfoBase.getChannelId());
                return true;
            }
        }
        return false;
    }

    public boolean changeChannelByQueryFreq(int frequency) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeChannelByQueryFreq(),frequency:" + frequency);
        MtkTvChannelQuery mMtkTvChannelQuery = new MtkTvChannelQuery();
        mMtkTvChannelQuery.setQueryType(MtkTvChannelQuery.QUERY_BY_FREQ);
        mMtkTvChannelQuery.setFrequency(frequency);
        List<MtkTvChannelInfoBase> tempList = CommonIntegration.getInstance()
                .getChnnelListByQueryInfo(
                        mMtkTvChannelQuery);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel:tempList>>" + (tempList == null ? tempList : tempList.size()));
        if (tempList != null && !tempList.isEmpty()) {
            int findId = tempList.get(0).getChannelId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findId>>" + findId + ">>>"
                    + tempList.get(0).getServiceName());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.isScanning()2>>" + isScanning());
            if (!isScanning()) {
                CommonIntegration.getInstance().selectChannelById(findId);
            }
            return true;
        }
        return false;
    }

    public boolean isSourceType3D() {
        MtkTvAppTVBase apptv = new MtkTvAppTVBase();
        int type = apptv.GetVideoSrcTag3DType(CommonIntegration.getInstance().getCurrentFocus());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSourceType3D type:" + type);
        switch (type) {
            case VSH_SRC_TAG3D_TTDO:
            case VSH_SRC_TAG3D_NOT_SUPPORT:

                return false;
            case VSH_SRC_TAG3D_2D:
            case VSH_SRC_TAG3D_MVC:
            case VSH_SRC_TAG3D_FP:
            case VSH_SRC_TAG3D_FS:
            case VSH_SRC_TAG3D_TB:
            case VSH_SRC_TAG3D_SBS:
            case VSH_SRC_TAG3D_REALD:
            case VSH_SRC_TAG3D_SENSIO:
            case VSH_SRC_TAG3D_LA:
                return true;
            default:
                return false;
        }
    }

    public int[] dvbsGetNfySvcUpd() {
        MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
        base.dvbsGetNfySvcUpd();

        int addedChannel = 0;
        addedChannel += base.nfySvcUpd_tvAdd;
        addedChannel += base.nfySvcUpd_rdAdd;
        addedChannel += base.nfySvcUpd_apAdd;

        int deleteChannel = 0;
        deleteChannel += base.nfySvcUpd_tvDel;
        deleteChannel += base.nfySvcUpd_rdDel;
        deleteChannel += base.nfySvcUpd_apDel;
        return new int[] {
                addedChannel, deleteChannel
        };
    }

    // check last input source name is vga
    public boolean isLastInputSourceVGA() {
        if (lastInputSourceName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_VGA)) {
            lastInputSourceName = "";// reset to aviod
            return true;
        }
        return false;
    }

    public int getInitATVFreq() {
        int startFrq = 0;
        MtkTvChannelInfoBase tvCh = CommonIntegration.getInstance().getCurChInfo();
        if ((tvCh != null && CommonIntegration.getInstance().isCurrentSourceATV())) {
            startFrq = tvCh.getFrequency();
        } else {
            // freq=44;
            startFrq = getAtvLowFreq();
        }
        return startFrq;
    }

    public int getAtvLowFreq(){
        MtkTvScanPalSecamBase.ScanPalSecamFreqRange range =
                new MtkTvScanPalSecamBase().new ScanPalSecamFreqRange();
        MtkTvScan.getInstance().getScanPalSecamInstance().getFreqRange(range);
        return Math.max(range.lower_freq, 44 * 1000000);
    }

    public boolean isCurrentAtvPlaying(){
        return  CommonIntegration.getInstance().getCurChInfo() != null
                && CommonIntegration.getInstance().isCurrentSourceATV();
    }

    public int getInitDVBCRFFreq() {
        int dvbcSingleRFScan = 0;
        MtkTvChannelInfoBase currentCH = CommonIntegration.getInstance().getCurChInfo();
        if ((currentCH != null && CommonIntegration.getInstance().isCurrentSourceDTV())) {
            dvbcSingleRFScan = currentCH.getFrequency() / 1000;
        } else {
            dvbcSingleRFScan = 306000;
        }
        return dvbcSingleRFScan;
    }

    public String getCurrInputSourceNameByApi() {
        String focus = CommonIntegration.getInstance().getCurrentFocus();
        return InputSourceManager.getInstance().getInputSourceByAPI(focus);
    }

    public String getDrmRegistrationCode() {
        return mMtkTvMultiMediaBase.GetDrmRegistrationCode();
    }

    public String setDrmDeactivation() {
        return mMtkTvMultiMediaBase.SetDrmDeactivation();
    }

    public long getDrmUiHelpInfo() {
        return mMtkTvMultiMediaBase.GetDrmUiHelpInfo();
    }

    /**
     * clean SA region booked events
     */
    public void cleanLocalData() {
        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
            new Thread(new Runnable(){
                @Override
                public void run() {
                    DBMgrProgramList.getInstance(mContext).getWriteableDB();
                    DBMgrProgramList.getInstance(mContext).deleteAllPrograms();
                    DBMgrProgramList.getInstance(mContext).closeDB();
                    DataReader.getInstance(mContext).cleanMStypeDB();
                }
            }).start();
        } else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
            new Thread(new Runnable(){
                @Override
                public void run() {
                    DataReader.getInstance(mContext).cleanMStypeDB();
                }
            }).start();
        }
    }

    /**
     * clean rating this method may be called when clean storage or reset default now only use when
     * clean storage
     */
    public void cleanRatings() {
        TvInputManager mTvInputManager = (TvInputManager) mContext
                .getSystemService(Context.TV_INPUT_SERVICE);
        List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
        if (dvbRatings != null) {
            for (TvContentRating rating : dvbRatings) {
                mTvInputManager.removeBlockedRating(rating);
            }
        }
    }

    public void restartSelfActivity(Context context, Class cls) {
        ((Activity) context).finish();
        Intent intent = new Intent();
        intent.setClass(context, cls);
        context.startActivity(intent);
    }

    public int calcATSCFreq(int rfCh) {
        int resultFreq = 0;
        if (rfCh < getFirstScanIndex() || rfCh > getLastScanIndex()) {
          resultFreq = 0;
        } else if (rfCh < 5) {
          resultFreq = rfCh * 6 + 45;
        } else if (rfCh == 5) {
          resultFreq = 79;
        } else if (rfCh == 6) {
          resultFreq = 85;
        } else if (rfCh < 14) {
          resultFreq = rfCh * 6 + 135;
        } else if (rfCh <= getLastScanIndex()) {
          resultFreq = rfCh * 6 + 389;
        }
        return resultFreq * 1000000;
	}

	public int calcCQAMFreq(int rfCh) {
		int resultFreq = 0;
        int freqPlan = saveV.readValue(MenuConfigManager.FREQUENEY_PLAN);
        final int bandWidthIrc = 6000000;
        final int bandWidthHrc = 6000300;
        switch (freqPlan) {
          case 0:
            if (rfCh == 1)
            {
              resultFreq = 0;
            }
            else if (rfCh == 5)
            {
              resultFreq = 79000000;
            }
            else if (rfCh == 6)
            {
              resultFreq = 85000000;
            }
            break;
        case 1:
            if (rfCh == 1)
            {
              resultFreq = 75000000;
            }
            else if (rfCh == 5)
            {
              resultFreq = 81000000;
            }
            else if (rfCh == 6)
            {
              resultFreq = 87000000;
            }
            else if (rfCh < 5)
            {
              resultFreq = rfCh * bandWidthIrc + 45000000;
            }
            else if (rfCh < 14)
            {
              resultFreq = rfCh * bandWidthIrc + 135000000;
            }
            else if (rfCh < 23)
            {
              resultFreq = rfCh * bandWidthIrc + 39000000;
            }
            else if (rfCh < 95)
            {
              resultFreq = rfCh * bandWidthIrc + 81000000;
            }
            else if (rfCh < 100)
            {
              resultFreq = (rfCh - 95) * bandWidthIrc + 93000000;
            }
            else if (rfCh <= getLastScanIndex())
            {
              resultFreq = rfCh * bandWidthIrc + 51000000;
            }
            break;
        case 2:
            if (rfCh == 1)
            {
              resultFreq = 73753750;
            }
            else if (rfCh == 5)
            {
              resultFreq = 79754050;
            }
            else if (rfCh == 6)
            {
              resultFreq = 85754350;
            }
            else if (rfCh < 5)
            {
              resultFreq = rfCh + 7 * bandWidthHrc + 1750150;
            }
            else if (rfCh < 14)
            {
              resultFreq = rfCh + 22 * bandWidthHrc + 1750150;
            }
            else if (rfCh < 23)
            {
              resultFreq = rfCh + 6 * bandWidthHrc + 1750150;
            }
            else if (rfCh < 95)
            {
              resultFreq = rfCh + 13 * bandWidthHrc + 1750150;
            }
            else if (rfCh < 100)
            {
              resultFreq = rfCh - 80 * bandWidthHrc + 1750150;
            }
            else if (rfCh <= getLastScanIndex())
            {
              resultFreq = rfCh + 8 * bandWidthHrc + 1750150;
            }
            break;
          default:
            break;
        }
		return resultFreq;
	}

    public int calcSAFreq(int rfCh) {
        int resultFreq = 0;
        final int bandWidth = 6000000;
        final int baseFreq7 = 177143;
        final int baseFreq14 = 473143;
        if (rfCh >= 7 && rfCh <= 13) {
          resultFreq = baseFreq7 + (rfCh - 7) * bandWidth;
        } else if (rfCh <= 69) {
          resultFreq = baseFreq14 + (rfCh - 14) * bandWidth;
        }
        return resultFreq;
    }

	public int getModulation() {
		return saveV.readValue(MenuConfigManager.TV_SINGLE_SCAN_MODULATION);
	}

	public boolean checkIfNeedShowRegionList(){
	    return mScanManager.checkIfNeedShowRegionList();
	}

    public boolean needDisableLcnUI(){//DTV2968372 & DTV2200417
        return  (isBelgiumCountry() && getCurrentTunerMode() == 1 && ScanContent.isTELNETOp())
                || (ScanContent.isZiggoUPCOp() && getCurrentTunerMode() == 1);
    }

    public boolean needDisableChannelUpdateUI(){//DTV03055901 DTV03055898
        return  (getCurrentTunerMode() == 1 && (ScanContent.isOnlimeOp() || ScanContent.isRostelecomSpbOp()
                || ScanContent.isZiggoUPCOp()));
    }

    public void loadDvbsDataBase(){
        if(CommonIntegration.isSupportDvbs()) {
            SatelliteProviderManager satelliteProviderManager = new SatelliteProviderManager(DestroyApp.appContext);
            boolean isSync = SaveValue.readWorldBooleanValue(DestroyApp.appContext, "syncSatelliteToDBFromXML");
            Log.d(TAG, "loadDvbsDataBase isSync:" + isSync);
            if (!isSync) {
                TVAsyncExecutor.getInstance().execute(new Runnable() {
                    public void run() {
                        boolean result = satelliteProviderManager.syncSatellitesToDBFromXML();
                        MtkTvConfig.getInstance().setConfigValue(
                                MtkTvConfigTypeBase.CFG_BS_BS_SAT_ANTENNA_TYPE, MenuConfigManager.DVBS_ACFG_SINGLE);
                        Log.d(TAG, "11loadDvbsDataBase isSync:" + isSync);
                        ScanContent.initSatellitesFromXML(DestroyApp.appContext, MenuConfigManager.DVBS_ACFG_SINGLE);
                        SaveValue.saveWorldBooleanValue(DestroyApp.appContext, "syncSatelliteToDBFromXML", result, true);
                    }
                });
            }
        }
    }
}
