package com.mediatek.wwtv.setting.base.scan.model;

public interface ScannerListener {


    int COMPLETE_ERROR = 0;
    int COMPLETE_CANCEL = 1;
    int COMPLETE_OK = 2;

    /* Copy from MtkTvScanBase.CallBackType, */
    int CALL_BACK_TYPE_AUTO=0;
    int CALL_BACK_TYPE_ATV=1; /* 1 ATV */
    int CALL_BACK_TYPE_DTV=2; /* 2 DTV */
    int CALL_BACK_TYPE_DTV_DVBT_UI_OP=3; /* 3 DVBT UI operation */

    /* DVBS call back notify function */
    int CALL_BACK_TYPE_DTV_DVBS_SAT_NAME_NFY_FCT=4;
    int CALL_BACK_TYPE_DTV_DVBS_SVC_UPDATE_NFY_FCT=5;
    int CALL_BACK_TYPE_DTV_DVBS_NEW_SVC_NFY_FCT=6;
    int CALL_BACK_TYPE_DTV_DVBS_BOUQUET_INFO_NFY_FCT=7;
    int CALL_BACK_TYPE_DTV_DVBS_MDU_DETECT_NFY_FCT=9;

    int CALL_BACK_TYPE_NUM=9; /* number of scan Type */

    void onCompleted(int completeValue);

    void onFrequence(int freq);

    @Deprecated
    void onProgress(int progress, int channels);
    /**
     * @param progress
     * @param channels
     * @param type 1==atv;2==dtv
     */
    void onProgress(int progress, int channels,int type);
    void onDVBSInfoUpdated(int argv4, String name);

    void onDVBCNetworkNameUpdate(int argv4, String name);
	
	void onIPChannels(int channels,int type);
	
	void onFvpUserSelection();
	void onFvpScanError();
	void onFvpScanStart();

}
