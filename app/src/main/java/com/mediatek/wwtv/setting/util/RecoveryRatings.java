package com.mediatek.wwtv.setting.util;

import android.content.Context;
import com.mediatek.twoworlds.tv.model.MtkTvUSTvRatingSettingInfoBase;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import java.util.ArrayList;
import java.util.List;

import android.media.tv.TvInputManager;
import android.media.tv.TvContentRating;

public abstract class RecoveryRatings {
    private final static String TAG ="RecoveryRatings";
//    private final static String DEFAULT_PARENTAL_CONTROLS = "vendor.mtk.pc_defvalue";
    private static Context mContext ;
    static TVContent mTV ;
    static TvInputManager mTvInputManager ;

    /*tv rating domain*/
    public static final String RATING_DOMAIN = "com.android.tv";

    /*us tv rating system*/
    public static final String RATING_SYS_US_TV = "US_TV";
    /*dvb tv rating system*/
    public static final String RATING_SYS_DVB_TV = "DVB";
    /*au tv rating system*/
    public static final String RATING_SYS_AU_TV = "AU_TV";
    /*us movie rating system*/
    public static final String RATING_SYS_US_MV = "US_MV";

    /*us canadian english language rating system*/
    public static final String RATING_SYS_US_CA_EN_TV = "CA_EN_TV";

    /*us canadian french language rating system*/
    public static final String RATING_SYS_US_CA_FR_TV = "CA_TV";
    /*sa tv rating system*/
    public static final String RATING_SYS_SA_TV = "BR_TV";

    /*us tv rating*/
    public static final String RATING_US_TV_Y = "US_TV_Y";
    public static final String RATING_US_TV_Y7 = "US_TV_Y7";
    public static final String RATING_US_TV_G = "US_TV_G";
    public static final String RATING_US_TV_PG = "US_TV_PG";
    public static final String RATING_US_TV_14 = "US_TV_14";
    public static final String RATING_US_TV_MA = "US_TV_MA";

    /*us tv sub rating */
    public static final String SUB_RATING_US_TV_A = "US_TV_A";//a custom sub rating
    public static final String SUB_RATING_US_TV_D = "US_TV_D";
    public static final String SUB_RATING_US_TV_L = "US_TV_L";
    public static final String SUB_RATING_US_TV_S = "US_TV_S";
    public static final String SUB_RATING_US_TV_V = "US_TV_V";
    public static final String SUB_RATING_US_TV_FV = "US_TV_FV";

    /*us tv sub rating array for every rating */
    public static final String[] US_TV_Y_SUB_RATINGS = {SUB_RATING_US_TV_A};
    public static final String[] US_TV_Y7_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_FV};
    public static final String[] US_TV_G_SUB_RATINGS = {SUB_RATING_US_TV_A};
    public static final String[] US_TV_PG_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
    public static final String[] US_TV_14_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
    public static final String[] US_TV_MA_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};

    /*us tv sub rating array for every rating without US_TV_A*/
    public static final String[] US_TV_Y_SUB_RATINGS_N_A = {};
    public static final String[] US_TV_Y7_SUB_RATINGS_N_A = {SUB_RATING_US_TV_FV};
    public static final String[] US_TV_G_SUB_RATINGS_N_A = {};
    public static final String[] US_TV_PG_SUB_RATINGS_N_A = {SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
    public static final String[] US_TV_14_SUB_RATINGS_N_A = {SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
    public static final String[] US_TV_MA_SUB_RATINGS_N_A = {SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};

    /********************************australia tv rating****************************************/
    /*au tv rating*/
    public static final String RATING_AU_TV_P = "AU_TV_P";
    public static final String RATING_AU_TV_C = "AU_TV_C";
    public static final String RATING_AU_TV_G = "AU_TV_G";
    public static final String RATING_AU_TV_PG = "AU_TV_PG";
    public static final String RATING_AU_TV_M = "AU_TV_M";
    public static final String RATING_AU_TV_MA = "AU_TV_MA";
    public static final String RATING_AU_TV_AV = "AU_TV_AV";
    public static final String RATING_AU_TV_R = "AU_TV_R";
    //custom type
    public static final String RATING_AU_TV_ALL = "ALL";

    /************US Movie rating*************/
    public static final String RATING_US_MV_G = "US_MV_G";
    public static final String RATING_US_MV_PG = "US_MV_PG";
    public static final String RATING_US_MV_PG13 = "US_MV_PG13";
    public static final String RATING_US_MV_R = "US_MV_R";
    public static final String RATING_US_MV_NC17 = "US_MV_NC17";
    public static final String RATING_US_MV_X = "US_MV_X";

    /************US Canadian english rating*************/
    public static final String RATING_US_CA_EN_TV_EXEMPT = "CA_EN_TV_EXEMPT";
    public static final String RATING_US_CA_EN_TV_C = "CA_EN_TV_C";
    public static final String RATING_US_CA_EN_TV_C8 = "CA_EN_TV_C8";
    public static final String RATING_US_CA_EN_TV_G = "CA_EN_TV_G";
    public static final String RATING_US_CA_EN_TV_PG = "CA_EN_TV_PG";
    public static final String RATING_US_CA_EN_TV_14 = "CA_EN_TV_14";
    public static final String RATING_US_CA_EN_TV_18 = "CA_EN_TV_18";

    /*************US Canadian french rating***************/
    public static final String RATING_US_CA_FR_TV_E = "CA_FR_TV_E";
    public static final String RATING_US_CA_FR_TV_G = "CA_FR_TV_G";
    public static final String RATING_US_CA_FR_TV_8 = "CA_FR_TV_8";
    public static final String RATING_US_CA_FR_TV_13 = "CA_FR_TV_13";
    public static final String RATING_US_CA_FR_TV_16 = "CA_FR_TV_16";
    public static final String RATING_US_CA_FR_TV_18 = "CA_FR_TV_18";
    /*************SA Age Rating***************************/
    /*sa age rating*/
    public static final String RATING_SA_TV_AGE_10 = "BR_TV_10";
    public static final String RATING_SA_TV_AGE_12 = "BR_TV_12";
    public static final String RATING_SA_TV_AGE_14 = "BR_TV_14";
    public static final String RATING_SA_TV_AGE_16 = "BR_TV_16";
    public static final String RATING_SA_TV_AGE_18 = "BR_TV_18";
    public static final String RATING_SA_TV_AGE_L = "BR_TV_L";
    /*sa content rating*/
    public static final String RATING_SA_TV_DRAG = "BR_TV_D";
    public static final String RATING_SA_TV_SEX = "BR_TV_S";
    public static final String RATING_SA_TV_VIOLENCE = "BR_TV_V";

    public static void recoveryFromTVAPI(Context context){
        boolean isRating = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING);
        boolean isRatingSA = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING_SA);
        if(!(isRating || isRatingSA)){
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "not support TIF so needn't to recovery rating!");
            return ;
        }
        mContext = context;
        mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
        if(mTvInputManager.getBlockedRatings().size()>0){
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "needn't to recovery rating!");
            for(TvContentRating xRating:mTvInputManager.getBlockedRatings()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "exist Rating.String=="+xRating.flattenToString());
            }
            return;
        }

        mTV = TVContent.getInstance(context);
        if(CommonIntegration.isEURegion()){
            /*String curCountry = Util.convertConurty(MtkTvConfig.getInstance().getCountry());
            ParentalControlSettings parentControl = TvSingletons.getSingletons().getParentalControlSettings();
            ContentRatingSystem currRating = null;
            ContentRatingSystem otherRating = null;
            ContentRatingSystem displayRating = null;

            parentControl.loadRatings();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "current Country :"+curCountry);
            for (ContentRatingSystem contentRatingSystem :
                    TvSingletons.getSingletons()
                    .getContentRatingsManager().getContentRatingSystems()) {
                List<String> list = contentRatingSystem.getCountries();

                if(list == null) {
                    continue;
                } else if(list.contains(curCountry)){
                    currRating = contentRatingSystem;
                } else if(list.size() > 2){
                    otherRating = contentRatingSystem;
                }
            }

            displayRating = currRating != null ? currRating : otherRating;

            Order order = displayRating.getOrders().get(0);
            int maxAge = 0;
            int maxAgeRatingIndex = order.getRatingOrder().size();
            Rating maxRating = order.getRatingOrder().get(maxAgeRatingIndex - 1);
            maxAge = maxRating.getAgeHint();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Max Age="+maxAge);
            parentControl.setRatingBlocked(displayRating, maxRating , true);
            recoverContentRatingLevel(maxAge);
            recoverEURatingEnabled(maxAge > 0);*/
        	//fix DTV02029280
            boolean defEnabled =  TVContent.getInstance(context).getRatingEnable() == 1;
            recoverEURatingEnabled(defEnabled);
        } else if(CommonIntegration.isSARegion()){
            int ageValue = mTV.getIsdbRating().getISDBAgeRatingSetting();
            int cntValue = mTV.getIsdbRating().getISDBContentRatingSetting();
            if(ageValue >= 0){

                //add tif content rating
                String[] subRatings = null ;
                if (cntValue == 1) {
                    subRatings = new String[] {
                    RATING_SA_TV_DRAG
                    };
                } else if (cntValue == 2) {
                  subRatings = new String[] {
                    RATING_SA_TV_VIOLENCE
                  };
                } else if (cntValue == 3) {
                  subRatings = new String[] {
                    RATING_SA_TV_SEX
                  };
                } else if (cntValue == 4) {
                  subRatings = new String[] {
                      RATING_SA_TV_DRAG, RATING_SA_TV_VIOLENCE
                  };
                } else if (cntValue == 5) {
                  subRatings = new String[] {
                      RATING_SA_TV_DRAG, RATING_SA_TV_SEX
                  };
                } else if (cntValue == 6) {
                  subRatings = new String[] {
                      RATING_SA_TV_SEX, RATING_SA_TV_VIOLENCE
                  };
                } else if (cntValue == 7) {
                  subRatings = new String[] {
                      RATING_SA_TV_DRAG, RATING_SA_TV_SEX, RATING_SA_TV_VIOLENCE
                  };
                }

                String ratingPrefix = "BR_TV_";
                if (ageValue > 0) {
                  for (int i = ageValue * 2 + 8; i <= 18; i += 2) {
                    TvContentRating mRating = TvContentRating.createRating(RATING_DOMAIN,
                        RATING_SYS_SA_TV, ratingPrefix + i, subRatings);
                    mTvInputManager.addBlockedRating(mRating);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SA mRating for age " + i + " toString==" + mRating.flattenToString());
                  }
                } else {
                  if (cntValue > 0) {
                    TvContentRating mRatingL =
                        TvContentRating.createRating(RATING_DOMAIN, RATING_SYS_SA_TV, RATING_SA_TV_AGE_L,
                            subRatings);
                    mTvInputManager.addBlockedRating(mRatingL);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SA mRating for add L toString==" + mRatingL.flattenToString());
                    for (int i = (ageValue + 1) * 2 + 8; i <= 18; i += 2) {
                      TvContentRating mRating = TvContentRating.createRating(RATING_DOMAIN,
                          RATING_SYS_SA_TV, ratingPrefix + i, subRatings);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SA mRating for age " + i + " toString==" + mRating.flattenToString());
                      mTvInputManager.addBlockedRating(mRating);
                    }
                  }
                }

            }
        }else if(CommonIntegration.isUSRegion()){
            recoverUSRatingEnabled();
            recoverUSTVRating();
            recoveryUSMVRating();
            recoveryUSCAENTVRating();
            recoveryUSCAFRTVRating();
        }
    }
/*
    private static void recoverContentRatingLevel(int level){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "recoverContentRatingLevel:level="+level);
        int defaultLevel=TvSettings.getContentRatingLevel(mContext);
        //when first set level value .
        if(defaultLevel==TvSettings.CONTENT_RATING_LEVEL_UNKNOWN){
            if(level>0)
                    TvSettings.setContentRatingLevel(mContext, TvSettings.CONTENT_RATING_LEVEL_CUSTOM);
                else
                    TvSettings.setContentRatingLevel(mContext, TvSettings.CONTENT_RATING_LEVEL_NONE);
        }
    }
*/
    private static void recoverEURatingEnabled(boolean enable){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "recoverEURatingEnabled:enable="+enable);
        mTvInputManager.setParentalControlsEnabled(enable);
    }

    private static void recoverUSRatingEnabled(){
        boolean enable = mTV.getATSCRating().getRatingEnable();
        if(enable){
            mTvInputManager.setParentalControlsEnabled(enable);
        }
    }

    private static void recoverUSTVRating(){
        List<String> subRatingsList = new ArrayList<String>();
        MtkTvUSTvRatingSettingInfoBase info= mTV.getATSCRating().getUSTvRatingSettingInfo();
        String sys = RATING_SYS_US_TV;
        //String ratingName = "";
        String [] subRatings = null;
        int[] indexs = null;
        subRatings = US_TV_Y_SUB_RATINGS;
        /** for Rating--Y*/
        //Y
        if(info.isUsAgeTvYBlock()){
            TvContentRating usTvY = TvContentRating.createRating(RATING_DOMAIN,sys,RATING_US_TV_Y,subRatings);
            mTvInputManager.addBlockedRating(usTvY);
        }

        subRatingsList.clear();
        subRatings = US_TV_Y7_SUB_RATINGS;
        indexs = new int[]{-1,-1};
        /** for Rating--Y7*/
        //Y7
        if(info.isUsAgeTvY7Block()){
            indexs[0] = 0;
        }
        //Y7-FV
        if(info.isUsCntTvY7FVBlock()){
            indexs[1] = 1;
        }

        for(int index : indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
        subRatings = subRatingsList.toArray(new String[0]);
        if(subRatings != null){
            for(String sub:subRatings){
                TvContentRating usTvY7 = TvContentRating.createRating(RATING_DOMAIN,sys,RATING_US_TV_Y7,new String[]{sub});
                mTvInputManager.addBlockedRating(usTvY7);
            }
        }

        /** for Rating--G*/
        subRatings = US_TV_G_SUB_RATINGS;
        //G
        if(info.isUsAgeTvGBlock()){
            TvContentRating usTvG = TvContentRating.createRating(RATING_DOMAIN,sys,RATING_US_TV_G,subRatings);
            mTvInputManager.addBlockedRating(usTvG);
        }


        /** for Rating--PG*/
        subRatingsList.clear();
        subRatings = US_TV_PG_SUB_RATINGS;
        indexs = new int[]{-1,-1,-1,-1,-1};
        //PG
        if(info.isUsAgeTvPGBlock()){
            indexs[0] = 0;
        }
        //PG-D
        if(info.isUsCntTvPGDBlock()){
            indexs[1] = 1;
        }
        //PG-L
        if(info.isUsCntTvPGLBlock()){
            indexs[2] = 2;
        }
        //PG-S
        if(info.isUsCntTvPGSBlock()){
            indexs[3] = 3;
        }
        //PG-V
        if(info.isUsCntTvPGVBlock()){
            indexs[4] = 4;
        }
        for(int index : indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
        subRatings = subRatingsList.toArray(new String[0]);

        if(subRatings != null){
            for(String sub:subRatings){
                TvContentRating usTvPg = TvContentRating.createRating(RATING_DOMAIN,sys,RATING_US_TV_PG,new String[]{sub});
                mTvInputManager.addBlockedRating(usTvPg);
            }
        }


        /** for Rating--14*/
        subRatingsList.clear();
        subRatings = US_TV_14_SUB_RATINGS;
        indexs = new int[]{-1,-1,-1,-1,-1};
        //14-A
        if(info.isUsAgeTv14Block()){
            indexs[0] = 0;
        }
        //14-D
        if(info.isUsCntTv14DBlock()){
            indexs[1] = 1;
        }
        //14-L
        if(info.isUsCntTv14LBlock()){
            indexs[2] = 2;
        }
        //14-S
        if(info.isUsCntTv14SBlock()){
            indexs[3] = 3;
        }
        //14-V
        if(info.isUsCntTv14VBlock()){
            indexs[4] = 4;
        }
        for(int index : indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
        subRatings = subRatingsList.toArray(new String[0]);
        if(subRatings != null){
            for(String sub:subRatings){
                TvContentRating usTv14 = TvContentRating.createRating(RATING_DOMAIN,sys,RATING_US_TV_14,new String[]{sub});
                mTvInputManager.addBlockedRating(usTv14);
            }
        }

        /** for Rating--MA*/
        subRatingsList.clear();
        subRatings = US_TV_MA_SUB_RATINGS;
        indexs = new int[]{-1,-1,-1,-1};
        //MA-A
        if(info.isUsAgeTvMABlock()){
            indexs[0] = 0;
        }
        //MA-L
        if(info.isUsCntTvMALBlock()){
            indexs[1] = 1;
        }
        //MA-S
        if(info.isUsCntTvMASBlock()){
            indexs[2] = 2;
        }
        //MA-V
        if(info.isUsCntTvMAVBlock()){
            indexs[3] = 3;
        }
        for(int index : indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
        subRatings = subRatingsList.toArray(new String[0]);
        if(subRatings != null){
            for(String sub:subRatings){
                TvContentRating usTvMa = TvContentRating.createRating(RATING_DOMAIN,sys,RATING_US_TV_MA,new String[]{sub});
                mTvInputManager.addBlockedRating(usTvMa);
            }
        }

        for(TvContentRating xRating:mTvInputManager.getBlockedRatings()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "USTV-xRating.String=="+xRating.flattenToString());
        }
    }

    private static void recoveryUSMVRating(){
        String [] ratingNames = new String[]{RATING_US_MV_G,RATING_US_MV_PG,RATING_US_MV_PG13,RATING_US_MV_R,RATING_US_MV_NC17,RATING_US_MV_X};
        int value = mTV.getATSCRating().getUSMovieRatingSettingInfo();
        for(;value<6;value ++){
            TvContentRating usMv = TvContentRating.createRating(RATING_DOMAIN,RATING_SYS_US_MV,ratingNames[value]);
            mTvInputManager.addBlockedRating(usMv);
        }
        for(TvContentRating xRating:mTvInputManager.getBlockedRatings()){
            if(xRating.getRatingSystem().equals(RATING_SYS_US_MV)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "USMV-xRating.String=="+xRating.flattenToString());
            }
        }
    }

    private static void recoveryUSCAENTVRating(){
        String [] ratingNames = new String[]{RATING_US_CA_EN_TV_C,RATING_US_CA_EN_TV_C8,RATING_US_CA_EN_TV_G,
                RATING_US_CA_EN_TV_PG,RATING_US_CA_EN_TV_14,RATING_US_CA_EN_TV_18};
        int value = mTV.getATSCRating().getCANEngRatingSettingInfo();
        for(;value>0 && value<7;value ++){
            TvContentRating usMv = TvContentRating.createRating(RATING_DOMAIN,RATING_SYS_US_CA_EN_TV,ratingNames[value-1]);
            mTvInputManager.addBlockedRating(usMv);
        }

        for(TvContentRating xRating:mTvInputManager.getBlockedRatings()){
            if(xRating.getRatingSystem().equals(RATING_SYS_US_CA_EN_TV)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "USCAEN-xRating.String=="+xRating.flattenToString());
            }
        }
    }

    private static void recoveryUSCAFRTVRating(){
        String [] ratingNames = new String[]{RATING_US_CA_FR_TV_G,RATING_US_CA_FR_TV_8,RATING_US_CA_FR_TV_13,
                RATING_US_CA_FR_TV_16,RATING_US_CA_FR_TV_18};
        int value = mTV.getATSCRating().getCANFreRatingSettingInfo();
        for(;value>0 && value<6;value ++){
            TvContentRating usMv = TvContentRating.createRating(RATING_DOMAIN,RATING_SYS_US_CA_FR_TV,ratingNames[value-1]);
            mTvInputManager.addBlockedRating(usMv);
        }
        for(TvContentRating xRating:mTvInputManager.getBlockedRatings()){
            if(xRating.getRatingSystem().equals(RATING_SYS_US_CA_FR_TV)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "USCAFR-xRating.String=="+xRating.flattenToString());
            }

        }
    }

}
