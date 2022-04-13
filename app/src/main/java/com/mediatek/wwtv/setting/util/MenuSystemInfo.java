package com.mediatek.wwtv.setting.util;

import java.math.BigDecimal;
import java.util.List;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.setting.base.scan.model.DVBSScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBTScanner;
import com.mediatek.wwtv.setting.preferences.ProgressPreference;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
//import android.support.v7.preference.PreferenceScreen;
//import android.support.v7.preference.Preference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

public class MenuSystemInfo {
    Preference vPreInfo,vFreqInfo,vCnInfo,// NOPMD
    vProgInfo,vUecInfo,vServiceIdInfo,vSymbolrateInfo,vtsInfo,vModulationInfo,
    vOnidInfo,vAgcInfo,vNetworkIDInfo,vChannelId,vLNBInfo,
    nSvcTypeInfo2,vSvcTypeInfo2,vSvcTypeInfo,vNetworkName,vBandWidth;
    private ProgressPreference vSignalQualityProgress;
    private ProgressPreference vSignalProgress;

    private boolean mvLNBInfoShow = false;
    private static final String TAG = "MenuSystemInfo";

    private TVContent mTVContent;
    String[] scanMode;
    String[] mduType;
    Context mContext;
    PreferenceScreen mPreferenceScreen;
    private String[] mBandWidth = new String[]{"BW_UNKNOWN","BW_6_MHz","BW_7_MHz","BW_8_MHz","BW_5_MHz","BW_10_MHz","BW_1_712_MHz"};
    int[] berValues = new int[]{6,7,8,9,10,11,12,13};
    String berDefaultValue = null;

    private Handler mRefreshHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg) {
            setValue(msg.arg1,msg.arg2);
            sendMessageDelayedThread(1,1000);
        };
    };

    private void sendMessageDelayedThread(final int what, final long delayMillis) {
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            public void run() {
                int level = EditChannel.getInstance(mContext).getSignalLevel();
                int quality = EditChannel.getInstance(mContext).getSignalQuality();
                Message msg = Message.obtain();
                msg.what = what;
                msg.arg1 = level;
                msg.arg2 = quality;
                mRefreshHandler.sendMessageDelayed(msg, delayMillis);
            }
        });
    }

    public MenuSystemInfo(Context context){
        mContext = context;
    }
    
    public void stopRefresh(){
        mRefreshHandler.removeMessages(1);
    }

    public PreferenceScreen getPreferenceScreen(PreferenceScreen preferenceScreen) {
        init(preferenceScreen, mContext);
        return preferenceScreen;
    }

    public void init(PreferenceScreen preferenceScreen, Context context) {
    	int signalquality;
        mPreferenceScreen = preferenceScreen;
        mPreferenceScreen.removeAll();
        vSignalProgress = new ProgressPreference(context);
        vSignalQualityProgress = new ProgressPreference(context);
        vChannelId = new Preference(context);
        vPreInfo = new Preference(context);
        vFreqInfo = new Preference(context);
        vCnInfo = new Preference(context);
        vProgInfo = new Preference(context);
        vUecInfo = new Preference(context);
        vServiceIdInfo = new Preference(context);
        vSymbolrateInfo = new Preference(context);
        vtsInfo = new Preference(context);
        vModulationInfo = new Preference(context);
        vOnidInfo = new Preference(context);
        vAgcInfo = new Preference(context);
        vNetworkIDInfo = new Preference(context);
        vNetworkName = new Preference(context);
        vBandWidth = new Preference(context);
        vLNBInfo = new Preference(context);
        vSvcTypeInfo2 = new Preference(context);
        vSvcTypeInfo = new Preference(context);

        signalquality = EditChannel.getInstance(mContext).getSignalQuality();
        mTVContent = TVContent.getInstance(mContext);
        scanMode = mContext.getResources().getStringArray(
                R.array.menu_tv_scan_mode_array);
        mduType = mContext.getResources().getStringArray(R.array.menu_tv_mdu_type_array);

        vSignalProgress.setMinValue(0);
        vSignalProgress.setMaxValue(100);
        vSignalProgress.setKey("vSignalProgress");
        vSignalProgress.setTitle(R.string.menu_tv_single_signal_level);
        vSignalProgress.setClickable(false);
        mPreferenceScreen.addPreference(vSignalProgress);

        vSignalQualityProgress.setKey("vSignalQualityProgress");
        vSignalQualityProgress.setMinValue(0);
        vSignalQualityProgress.setMaxValue(100);
        vSignalQualityProgress.setTitle(R.string.menu_tv_single_signal_quality);
        vSignalQualityProgress.setClickable(false);
        mPreferenceScreen.addPreference(vSignalQualityProgress);

        vChannelId.setKey("vChannelId");
        vChannelId.setTitle(R.string.menu_system_info_channel_title);
        mPreferenceScreen.addPreference(vChannelId);

        vPreInfo.setKey("vPreInfo");
        vPreInfo.setTitle(R.string.menu_system_info_ber);
        mPreferenceScreen.addPreference(vPreInfo);

        vFreqInfo.setKey("vFreqInfo");
        vFreqInfo.setTitle(R.string.menu_setup_biss_key_freqency);
        mPreferenceScreen.addPreference(vFreqInfo);

        vCnInfo.setKey("vCnInfo");
        vCnInfo.setTitle(R.string.menu_system_info_cn);
        mPreferenceScreen.addPreference(vCnInfo);

        vProgInfo.setKey("vProgInfo");
        vProgInfo.setTitle(R.string.menu_system_info_prog);
        mPreferenceScreen.addPreference(vProgInfo);

        vUecInfo.setKey("vUecInfo");
        vUecInfo.setTitle(R.string.menu_system_info_uec);
        mPreferenceScreen.addPreference(vUecInfo);

        vServiceIdInfo.setKey("vServiceIdInfo");
        vServiceIdInfo.setTitle(R.string.menu_system_info_service_id);
        mPreferenceScreen.addPreference(vServiceIdInfo);

        vSymbolrateInfo.setKey("vSymbolrateInfo");
        vSymbolrateInfo.setTitle(R.string.menu_tv_rf_sym_rate);
        mPreferenceScreen.addPreference(vSymbolrateInfo);

        vtsInfo.setKey("vtsInfo");
        vtsInfo.setTitle(R.string.menu_system_info_symbol_tsid);
        mPreferenceScreen.addPreference(vtsInfo);

        vModulationInfo.setKey("vModulationInfo");
        vModulationInfo.setTitle(R.string.menu_tv_sigle_modulation);
        mPreferenceScreen.addPreference(vModulationInfo);

        vOnidInfo.setKey("vOnidInfo");
        vOnidInfo.setTitle(R.string.menu_system_info_symbol_onid);
        mPreferenceScreen.addPreference(vOnidInfo);

        vAgcInfo.setKey("vAgcInfo");
        vAgcInfo.setTitle(R.string.menu_system_info_symbol_agc);
        mPreferenceScreen.addPreference(vAgcInfo);

        vNetworkIDInfo.setKey("vNetworkIDInfo");
        vNetworkIDInfo.setTitle(R.string.menu_c_net);
        mPreferenceScreen.addPreference(vNetworkIDInfo);

        vNetworkName.setKey("vNetworkName");
        vNetworkName.setTitle(R.string.menu_net_name);
        mPreferenceScreen.addPreference(vNetworkName);

        vBandWidth.setKey("vBandWidth");
        vBandWidth.setTitle(R.string.menu_band_width);
        mPreferenceScreen.addPreference(vBandWidth);

        vLNBInfo.setKey("vLNBInfo");
        vLNBInfo.setTitle(R.string.menu_system_info_symbol_lnb);
        mPreferenceScreen.addPreference(vLNBInfo);
        mvLNBInfoShow = true;

        vSvcTypeInfo2.setKey("vSvcTypeInfo2");
        vSvcTypeInfo2.setTitle(R.string.menu_system_info_symbol_svctype);
        mPreferenceScreen.addPreference(vSvcTypeInfo2);

        vSvcTypeInfo.setKey("vSvcTypeInfo");
        vSvcTypeInfo.setTitle(R.string.menu_system_info_symbol_svctype);
        mPreferenceScreen.addPreference(vSvcTypeInfo);

        if(mTVContent.getCurrentTunerMode() != 0){
            mPreferenceScreen.removePreference(vChannelId);
        }

        if (mTVContent.getCurrentTunerMode() == 1) {
            //vChannelId.setVisibility(View.INVISIBLE);
            vPreInfo.setTitle(R.string.menu_system_info_ber);
            vSymbolrateInfo.setTitle(R.string.menu_tv_rf_sym_rate);
            vModulationInfo.setTitle(R.string.menu_tv_sigle_modulation);
        }else {
            //vChannelId.setVisibility(View.VISIBLE);
            vPreInfo.setTitle(R.string.menu_system_info_ber);
            vSymbolrateInfo.setTitle(R.string.menu_system_info_symbol_post_viterbi);
            vModulationInfo.setTitle(R.string.menu_system_info_symbol_5s);
        }

        setValue(EditChannel.getInstance(mContext).getSignalLevel(),signalquality);
        mRefreshHandler.removeMessages(1);
        sendMessageDelayedThread(1, 1000);
    }

    /**
     * read Model Name, Version and Serial Number from common logic
     */
    public void setValue(int level,int signalquality) {
        int signalLevel = level;//EditChannel.getInstance(mContext).getSignalLevel();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setValue signalquality="+signalquality+",level="+signalLevel);

        if(vSignalProgress != null){
            vSignalProgress.setValue(signalLevel);
        }
        if(vSignalQualityProgress != null){
            vSignalQualityProgress.setValue(signalquality);
        }

        MtkTvAppTVBase app = new MtkTvAppTVBase();
        String path = CommonIntegration.getInstance().getCurrentFocus();
        int bervalue = app.GetConnAttrBER(path);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo","path:"+path + " bervalue:"+bervalue);
        if (bervalue < 0) {
            bervalue = -bervalue;
        }
//       bervalue /= 100 * 1000;

       com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo","app.GetConnAttrDBMSNR(path):"+app.GetConnAttrDBMSNR(path));
       com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo","app.GetConnAttrUEC(path):"+app.GetConnAttrUEC(path));

        if(bervalue == 0){
            berDefaultValue = "0E-6";
        } else {
            berDefaultValue = ""+bervalue/Math.pow(10,berValues[(bervalue+"").length()-1]);
        }

       if (mTVContent.getCurrentTunerMode() == 1) {
//           vSignalQualityProgress.setVisibility(View.INVISIBLE);
//           vSignalQuality.setVisibility(View.INVISIBLE);

    	   //2019-10-14 recover signal quality for authentication
           //mPreferenceScreen.removePreference(vSignalQualityProgress);

           ///* Pre ber is same as post viterbi */
           //if (bervalue < 0) {
           //    String berString = String.format("-%3.2e", (double)bervalue/(100 * 1000));
           //    vPreInfo.setText(berString);
           //}else {
           vPreInfo.setSummary(berDefaultValue);
           //}
           /* C/N */
           String cnString = String.format("%02d", app.GetConnAttrDBMSNR(path));
           vCnInfo.setSummary(cnString);

           /* 11. UEC */
           String uecString = String.format("%04d", app.GetConnAttrUEC(path));
           vUecInfo.setSummary(uecString);
           /* Smybol Rate  */
           String symRateString = String.format("%07d", app.GetSymRate(path));
           vSymbolrateInfo.setSummary(symRateString);
           /* Modulation */
           com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo","app.GetSymRate(path):"+app.GetSymRate(path));
           com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo","app.GetModulation(path):"+app.GetModulation(path));
           int modu =app.GetModulation(path);
           //QAM16,32,64,128,256
           if(modu==4 ||modu==5 ||modu==6 ||modu==10 ||modu==14){
               if(modu<=6){
                   modu -= 3 ;
               }else if(modu==10){
                   modu = 4;
               }else{
                   modu = 5;
               }
           }else{
               modu = 0;
           }
           /*
           if (modu < 0 || modu > scanMode.length - 1) {
               modu = 0;
           }
           */
           vModulationInfo.setSummary(scanMode[modu]);
           /* AGC */
           String agcString = String.format("%03d", app.GetConnAttrAGC(path));
           vAgcInfo.setSummary(agcString);
        } else {
            //vSignalQualityProgress.setVisibility(View.VISIBLE);
            //mPreferenceScreen.addPreference(vSignalQualityProgress);
            /* Pre viterbi */
            vPreInfo.setSummary(berDefaultValue);

            /* C/N */
            String cnString = String.format("%02d", app.GetConnAttrDBMSNR(path));
            vCnInfo.setSummary(cnString);

            /* UEC */
            String uecString = String.format("%04d", app.GetConnAttrUEC(path));
            vUecInfo.setSummary(uecString);

            /* Post viterbi */
            //if (bervalue < 0) {
            //    String berString = String.format("-%3.2e", (double)bervalue/(100 * 1000));
            //    vSymbolrateInfo.setText(berString);
            //}else {
            String berString = String.format("%3.2e", (double)bervalue/(100 * 1000));
            vSymbolrateInfo.setSummary(berString);
            //}

            /* 5S */
            //if (bervalue < 0) {
            //    String berString = String.format("-%3.2e", (double)bervalue/(100 * 1000));
            //    vModulationInfo.setText(berString);
            //}else {
            //String berString = String.format("%3.2e", (double)bervalue/(100 * 1000));
            vModulationInfo.setSummary(berString);
            //}

            /* cofdm AGC */
            String agcString = String.format("%03d", app.GetConnAttrAGC(path));
            vAgcInfo.setSummary(agcString);
        }

        MtkTvChannelInfoBase info = CommonIntegration.getInstance().getCurChInfo();
        int tunermodel = TVContent.getInstance(mContext).getCurrentTunerMode();
        boolean isDVBS = tunermodel >= CommonIntegration.DB_SAT_OPTID ? true : false;
        if (info instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo channel = (MtkTvDvbChannelInfo)info;
            if (vChannelId != null) {
                String rfChannelName = TVContent.getInstance(mContext).getRFChannel(DVBTScanner.RF_CHANNEL_CURRENT);
                vChannelId.setSummary(""+rfChannelName);
                /*if ("".equals(channel.getShortName()) || null == channel.getShortName()) {
                    vChannelId.setSummary("SO");
                }else {
                    vChannelId.setSummary(""+channel.getShortName());
                }*/
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo", "freq=="+channel.getFrequency());
            if(!isDVBS){
                float fre = ((float)channel.getFrequency())/1000000;
                BigDecimal bigDecimal = new BigDecimal(fre + "");
                String freq = ""+bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                vFreqInfo.setSummary(freq+"MHz");
            }else{
                vFreqInfo.setSummary((float)channel.getFrequency()+"MHz");
            }

//            vProgInfo.setText(channel.getProgId()+"");
            vProgInfo.setSummary(info.getChannelNumber()+"");
            vServiceIdInfo.setSummary(parseHex(channel.getSvlRecId()));
            vtsInfo.setSummary(parseHex(channel.getTsId()));
            vOnidInfo.setSummary(parseHex(channel.getOnId()));
            vNetworkIDInfo.setSummary(parseHex(channel.getNwId()));
            vNetworkName.setSummary(channel.getNwName());
            if(channel.getBandWidth() <= mBandWidth.length){
                vBandWidth.setSummary(mBandWidth[channel.getBandWidth()]+"");
            }
            if(DVBSScanner.isMDUScanMode()){//turkey
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo", "this is turkey logo ");
                int type = checkLNBInfo(channel);
                if(type == -1){
                    vLNBInfo.setSummary("-");
                }else{
                    vLNBInfo.setSummary(mduType[type]);
                }

            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo", "not turkey logo");
                //vLNBInfo.setVisibility(View.INVISIBLE);
                mPreferenceScreen.removePreference(vLNBInfo);
                mvLNBInfoShow = false;
            }
            checkSvcType(channel);
        }else {
            if (info != null){
                if(info instanceof MtkTvAnalogChannelInfo){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo", "this is analog channel");
                }
                float fre = ((float)info.getFrequency())/1000000;
                BigDecimal bigDecimal = new BigDecimal(fre + "");
                String freq = ""+bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                vFreqInfo.setSummary(freq+"MHz");
//                vProgInfo.setText(channel.getProgId()+"");
                vProgInfo.setSummary(info.getChannelNumber()+"");
            }else {
                vFreqInfo.setSummary("-");
                vProgInfo.setSummary("-");
            }

            vServiceIdInfo.setSummary("0x0000");
            vtsInfo.setSummary("0x0000");
            vOnidInfo.setSummary("0x0000");
            vNetworkIDInfo.setSummary("0x0000");

            //vLNBInfo.setVisibility(View.INVISIBLE);
            mPreferenceScreen.removePreference(vLNBInfo);
            mPreferenceScreen.removePreference(vSvcTypeInfo);
            mPreferenceScreen.removePreference(vSvcTypeInfo2);
            mvLNBInfoShow = false;
        }

    }

    private String parseHex(int parsevalue) {
        String hexValue = Integer.toHexString(parsevalue);
        switch (hexValue.length()) {
            case 0:
                hexValue ="0x0000";
                break;
            case 1:
                hexValue ="0x000"+hexValue;
                break;
            case 2:
                hexValue ="0x00"+hexValue;
                break;
            case 3:
                hexValue ="0x0"+hexValue;
                break;
            case 4:
                hexValue ="0x"+hexValue;
                break;
            default:
                hexValue ="0x0000";
                break;
        }
        return hexValue;
    }

    private int checkLNBInfo(MtkTvDvbChannelInfo mCurrentChannel) {
        MtkTvDvbsConfigBase mSatl = new MtkTvDvbsConfigBase();
        int svlID = CommonIntegration.getInstance().getSvl();
        MtkTvDvbChannelInfo curInfo = (MtkTvDvbChannelInfo) mCurrentChannel;
        int satRecId = curInfo.getSatRecId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo", "svlID is ==" + svlID + ",satRecId==" + satRecId);
        List<MtkTvDvbsConfigInfoBase> list = mSatl.getSatlRecord(svlID,
                satRecId);
        if (!list.isEmpty()) {
            int type = list.get(0).getMduType();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("SystemInfo", "type is " + type);
            return type;
        }
        return -1 ;
    }

    private boolean checkSvcType(MtkTvDvbChannelInfo mCurrentChannel){
        int svcType = mCurrentChannel.getSdtServiceType();
        if(svcType == 0x1f){//deal only when svctype is this value
            //if(vLNBInfo.isShown()){

            if(mvLNBInfoShow){
                //rowForSvcType.setVisibility(View.VISIBLE);
                //mPreferenceScreen.addPreference(vSvcTypeInfo);
                mPreferenceScreen.removePreference(vSvcTypeInfo2);
                vSvcTypeInfo.setSummary("0x1fHEVC TV");
            }else{
                //vSvcTypeInfo2.setVisibility(View.VISIBLE);
                //mPreferenceScreen.addPreference(vSvcTypeInfo);
                mPreferenceScreen.removePreference(vSvcTypeInfo);
                vSvcTypeInfo2.setSummary("0x1fHEVC TV");
            }
            return true;
        }
        mPreferenceScreen.removePreference(vSvcTypeInfo);
        mPreferenceScreen.removePreference(vSvcTypeInfo2);
        return false;
    }

}
