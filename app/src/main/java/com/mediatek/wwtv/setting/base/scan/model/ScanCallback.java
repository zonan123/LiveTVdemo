package com.mediatek.wwtv.setting.base.scan.model;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.os.RemoteException;
import com.mediatek.twoworlds.tv.MtkTvConfig;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.TargetRegion;
import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.wwtv.setting.base.scan.model.ScannerManager.Region;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;


public class ScanCallback extends MtkTvTVCallbackHandler {
    private final static String TAG = "ScanCallback";
    private int tkgsNfyInfoMask = 0;
    private final static int UNKONOWN = 0;
    private final static int COMPLETE = 1;
    private final static int PROGRESS = 2;
    private final static int CANCEL = 4;
    public final static int ABORT = 8;
    private final static int FREQUENCY = 16;
    private final static int DVBT_France_TNT = 16;
    private final static int DVBT_FVP_USER_SELECTION = 1<<9;
    private final static int DVBT_FVP_IP_CHANNELS = 1<<11;
    private final static int DVBT_FVP_SCAN_ERROR = 1<<12;
    private final static int DVBT_FVP_SCAN_START = 8192;
    private final static int DVBT_ITALY_NO_LCN_TABLE_MSG = 256;

    private ScannerManager mScannerManager;
    public ScanCallbackMsg mLastMsg;
    //private int channelType = -1;

    public static ScanCallback mScanCallBack;

    public boolean mScanComplete = false;
    public boolean mCheckDVBSInfo = false;
    private boolean mCanStartMduScan;

    public final static int HAS_OP = 3;

    private boolean hasOpTODO = false;
    public static int opType = -1;

    //private final int DVBT_THD_arg4 = 4;
    public final static int TYPE_UI_OP_UK_REGION = 0;
    public final static int TYPE_UI_OP_ITA_GROUP = 1;
    public final static int TYPE_UI_OP_NOR_FAV_NWK = 2;
    public final static int TYPE_UI_OP_LCNV2_CH_LST = 4;
    public final static int TYPE_UI_OP_SAVE_CHANNEL = 8;
    public final static int TYPE_UI_OP_SHOW_TIA_NO_CHANNELS = TYPE_UI_OP_SAVE_CHANNEL + 1;
    public boolean mCheckDVBSInfoGet = false;
    public final static int DVBT_France_TNT_arg4 = 4;
    public final static int ARG_DVBC_NWK_UPDATE = 8;
    public final static int TYPE_UI_OP_DVBS_BOUQUET_INFO = 16;
    public final static int TYPE_UI_OP_DVBS_TRICOLOR = 17;
    public final static int TYPE_UI_OP_DVBT_FVP_ERROR = 18;

    public final static int TYPE_DVBC_SCANNING_NETWORK_NAME = 15;

    @Override
    public int notifyScanNotification(int msgId, int scanProgress,
            int channelNum, int argv4) throws RemoteException {

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format(
                "msg_id:%4d,scanProgress:%4d,channelNum:%4d,argv4:%4d", msgId,
                scanProgress, channelNum, argv4));

        dealScanMsg(msgId, scanProgress, channelNum, argv4);
        return super.notifyScanNotification(msgId, scanProgress, channelNum, argv4);
    }

    public synchronized void dealScanMsg(int msgId, int scanProgress,
            int channelNum, int argv4) {

        int callbackHash = this.getClass().hashCode();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("callbackHash=? " + callbackHash);

        // pass same message.
        if (mLastMsg != null && msgId!=ABORT && msgId!=UNKONOWN && msgId!=DVBT_France_TNT &&  msgId != DVBT_FVP_USER_SELECTION) {//tkgs msg no need filter
            if (mLastMsg.equals(new ScanCallbackMsg(msgId, scanProgress,
                    channelNum, argv4))) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("This msg equals last msg.");
                return;
            }
        }
        mLastMsg = new ScanCallbackMsg(msgId, scanProgress, channelNum, argv4);
        
        //DVBC scanning network name
        if(msgId == 0 && scanProgress == 0 && channelNum == 0 && argv4 == TYPE_DVBC_SCANNING_NETWORK_NAME){
            String netWrork =  MtkTvScan.getInstance().getScanDvbcInstance().getScanningNwName();
            android.util.Log.d(TAG, "get dvbc scanning network name = "+netWrork);
            getScannerManager().getListener().onDVBCNetworkNameUpdate(argv4, netWrork);
            return;
        }

        // Fix Bug:DTV00597797 (France TNT,tell user to scan channel again)
        if (msgId == DVBT_France_TNT && scanProgress == 0
                && channelNum == 0 && (argv4 == DVBT_France_TNT_arg4||argv4 == ARG_DVBC_NWK_UPDATE)) {
            getScannerManager().showDvbtTHDConfirmDialog();
            return;
        }
        
        if (msgId == UNKONOWN && scanProgress == 0
                && channelNum == 0 && argv4 == ARG_DVBC_NWK_UPDATE) {
            getScannerManager().showDvbtTHDConfirmDialog();
            return;
        }
        // TKGS user msg dialog
        if (msgId == UNKONOWN && scanProgress == 0
            && channelNum == 0 && argv4 == MtkTvScanBase.CallBackType.CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT.ordinal()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT");
          int operatorName = MtkTvConfig.getInstance().getConfigValue(
              CommonIntegration.SAT_BRDCSTER);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT:OperatorName=" + operatorName);
          if (operatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS) {
            MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
            int ret = dvbsScan.dvbsGetNfyGetInfo().ordinal();
            if (ret == 0) {
              int getMask = dvbsScan.nfyGetInfo_mask;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT nfyGet_mask==" + getMask);
              if (getMask == dvbsScan.SB_DVBS_GET_INFO_MASK_TKGS_USER_MESSAGE) {
                try {
                  String utfStr = new String(dvbsScan.nfyGetInfo_usrMessage.getBytes("UTF-8"));
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT nfyGetInfo_umsg==" + utfStr);
                  getScannerManager().showDvbsTKGSUserMsgDialog(utfStr);
                } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
                }
              }
            }
          }else if(operatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_M7_FAST_SCAN){//fast scan version changed
              MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
              int ret = dvbsScan.dvbsGetNfyGetInfo().ordinal();
              if (ret == 0) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("fast scan DVBS_INFO_GET_NFY_FCT nfyGetInfo_mask==" + dvbsScan.nfyGetInfo_mask);
                  if ((dvbsScan.nfyGetInfo_mask & dvbsScan.SB_DVBS_GET_INFO_MASK_VERSION_CHANGE) != 0) {
                      getScannerManager().showDvbsFSTVersionChangeDialog();
                  }
              }
          }
        }
        if (getListener() == null) {
            switch (msgId) {
            case COMPLETE:
                if (mScannerManager.isScanTaskFinish()) {
                    mScannerManager.rollbackChannelsWhenScanNothingOnUIThread();
                }
                break;
            case CANCEL:
            case ABORT:
                mScannerManager.rollbackChannelsWhenScanNothingOnUIThread();
                break;
            default:
                break;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("getListener() == null");
            return;
        }

        if (msgId != 1 && scanProgress != 100) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("mScanComplete--1 = false");
            mScanComplete = false;
        }
        if (msgId == 1) {
            mScanComplete = false;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("mScanComplete--2 = " + mScanComplete);
        switch (msgId) {
        case UNKONOWN:
            switch (MtkTvScanBase.CallBackType.values()[argv4]) {
            case CALL_BACK_TYPE_DTV_DVBS_SAT_NAME_NFY_FCT:
                String name=mScannerManager.getScanningSatName();
                if(name!=null && !name.equalsIgnoreCase("null")){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("SAT_NAME_NFY_FCT,name:" + name);
                    getListener().onDVBSInfoUpdated(argv4,name);
                }
                break;
            case CALL_BACK_TYPE_DTV_DVBS_SVC_UPDATE_NFY_FCT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("CALL_BACK_TYPE_DTV_DVBS_SVC_UPDATE_NFY_FCT,");
                getListener().onDVBSInfoUpdated(ScannerListener.CALL_BACK_TYPE_DTV_DVBS_SVC_UPDATE_NFY_FCT, null);
                break;
            case CALL_BACK_TYPE_DTV_DVBS_NEW_SVC_NFY_FCT:

                break;
            case CALL_BACK_TYPE_DTV_DVBS_BOUQUET_INFO_NFY_FCT:
                mCheckDVBSInfo = true;
                break;
            case CALL_BACK_TYPE_DTV_DVBS_MDU_DETECT_NFY_FCT:
                mScannerManager.onMduDetectNfy();
                mCanStartMduScan = true;
                break;

            case CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT,");
                int operatorId = MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_BRDCSTER);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT:" + mScannerManager.mDVBSData);
                if (mScannerManager.mDVBSData != null) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT mScannerManager.mDVBSData.params :"
                + mScannerManager.mDVBSData.params);
                  if (mScannerManager.mDVBSData.params != null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT ((DVBSSettingsInfo) " +
                        "(mScannerManager.mDVBSData).params).isUpdateScan :"
                        + ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).isUpdateScan
                        + ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).tkgsType);
                  }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT OperatorName:" + operatorId);
                if (operatorId == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS
                    && mScannerManager.mDVBSData != null && mScannerManager.mDVBSData.params != null) {
                  DVBSSettingsInfo param = (DVBSSettingsInfo) mScannerManager.mDVBSData.params;
                  if (param.isUpdateScan) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT is tkgs update scan:");
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("CALL_BACK_TYPE_DTV_DVBS_INFO_GET_NFY_FCT is tkgs re-scan:");
                  }
                  MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
                  int ret = dvbsScan.dvbsGetNfyGetInfo().ordinal();
                  if (ret == 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT nfyGetInfo_mask==" + dvbsScan.nfyGetInfo_mask);
                    setTkgsNfyInfoMask(dvbsScan.nfyGetInfo_mask);
                    if (dvbsScan.nfyGetInfo_mask == dvbsScan.SB_DVBS_GET_INFO_MASK_TKGS_USER_MESSAGE) {
                      try {
                        String utfStr = new String(dvbsScan.nfyGetInfo_usrMessage.getBytes("UTF-8"));
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBS_INFO_GET_NFY_FCT nfyGetInfo_umsg==" + utfStr);
                        mScannerManager.mDVBSData.tkgsUserMessage = utfStr;
//                        mScannerManager.showDVBS_TKGS_UserMsgDialog(utfStr);
                      } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                      }

                    } else {
                      // show update scan continue scan-dialog or show re-scan's servicelist dialog
                      ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).tkgsType = 1;
                    }
                  }
                  mCheckDVBSInfoGet = true;
                } else if (operatorId == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR) {
                  setOpType(TYPE_UI_OP_DVBS_TRICOLOR);
                  setHasOpTODO(true);
                }
                break;
             default:
                break;
            }
            break;
        case COMPLETE:
            if (mScanComplete) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(".onCompleted(ScannerListener.COMPLETE_OK);mScanComplete==OK,return");
                break;
              }
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(".onCompleted(ScannerListener.COMPLETE_OK);.");
              if (mScannerManager.mDVBSData != null && mScannerManager.mDVBSData.params != null
                  && ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).mduType != -1) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCompleted startMdu new scan");
                if (mCanStartMduScan) {
                  mCanStartMduScan = false;
                  mScannerManager.startSecondMduScan();
                } else {
                  ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).mduType = -1;
                }
                break;
              }
              mScanComplete = true;
              // if (mScannerManager.region == Region.EU && argv4 == HAS_OP) {
              if (mScannerManager.region == Region.EU) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("dealScanMsg(),complete,has operation to do");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("complete mScannerManager.mDVBSData :" + mScannerManager.mDVBSData);
                if (mScannerManager.mDVBSData != null) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("complete mScannerManager.mDVBSData.params :"
                + mScannerManager.mDVBSData.params);
                  if (mScannerManager.mDVBSData.params != null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("complete ((DVBSSettingsInfo) " +
                        "(mScannerManager.mDVBSData).params).isUpdateScan :"
                        + ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).isUpdateScan
                        + ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).tkgsType);
                  }
                }
                if (mScannerManager.mDVBSData != null && mScannerManager.mDVBSData.params != null
                && ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).tkgsType == 1) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("complete tkgs mCheckDVBSInfoGet:" + mCheckDVBSInfoGet);
                  if (mCheckDVBSInfoGet) {
                    mCheckDVBSInfoGet = false;
                    boolean isupdate = ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).isUpdateScan;
                    String name = null;
                      if(getTkgsNfyInfoMask() == MtkTvScanDvbsBase.SB_DVBS_GET_INFO_MASK_TKGS_SVC_LIST){
                          name = "TKGS_LOGO";
                      }else if (isupdate && getTkgsNfyInfoMask() == MtkTvScanDvbsBase.SB_DVBS_GET_INFO_MASK_TKGS_SAME_VERSION) {
                          name = "TKGS_LOGO_UPDATE";
                      }
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTkgsNfyInfoMask:" + getTkgsNfyInfoMask()+",isupdate="+isupdate+",name="+name);
                      if(name != null) {
                        getListener().onDVBSInfoUpdated(mScannerManager.mDVBSData.satList[0], name);
                    }
                      setTkgsNfyInfoMask(0);
                  } else {
                    ((DVBSSettingsInfo) (mScannerManager.mDVBSData).params).tkgsType = -1;
                  }
                  getListener().onCompleted(ScannerListener.COMPLETE_OK);
                  break;
                }else if(ScanContent.isSatOpFastScan() && getScannerManager().getTuneMode() >= 2){
                    final MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
                    MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan.dvbsM7GetOptList();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "debugOP() M7_OptNum="+dvbsScan.M7_OptNum+","+mScannerManager.isFastScanSecondScan()+",dvbsRet="+dvbsRet);
                    if(!mScannerManager.isFastScanSecondScan()){
                        getListener().onDVBSInfoUpdated(dvbsScan.M7_OptNum, "FAST_SCAN_GET_OPERATOR");
                        if(dvbsScan.M7_OptNum > 0){
                            break;
                        }
                    }else {
                        getListener().onDVBSInfoUpdated(dvbsScan.M7_OptNum, "FAST_SCAN_CHECK_REGION");
                    }
                } else {
                  debugOP();
                }
              } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("dealScanMsg(),No operation!!!");
              }
              if (mScannerManager.isScanTaskFinish()) {
                mScannerManager.rollbackChannelsWhenScanNothingOnUIThread();
              }
              getListener().onCompleted(ScannerListener.COMPLETE_OK);
              //channelType = argv4;
              break;
        case PROGRESS:
            /*if((channelType == 0) && (argv4 == 1) && scanProgress == 100){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "specail case, DTV00612094");
                return;//fix issue:DTV00612094
            }*/
            if (mScannerManager.mDVBSData != null && mScannerManager.mDVBSData.params != null
                    && ((DVBSSettingsInfo)(mScannerManager.mDVBSData).params).mduType != -1) {//for MDU scan, no update progress
                if (mCanStartMduScan) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PROGRESS need startMdu new scan, so return update progress");
                    return;
                }
            }
            mScannerManager.setChannelsNum(channelNum, argv4);
            getListener().onProgress(scanProgress, channelNum, argv4);

            /*if(scanProgress==30){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("-----hangup-----");
                mScannerManager.getScanningSatName();
            }*/
            break;
        case CANCEL:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("onCompleted(ScannerListener.COMPLETE_CANCEL)");

            if (mScannerManager.region == Region.EU) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("dealScanMsg(),complete,has operation to do");
                debugOP();


                if (mScannerManager.needStartTVAfterATVScanUpDown()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(",needStartTVAfterATVScanUpDown()");
                    mScannerManager.resumeTV();
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("dealScanMsg(),No operation!!!");
            }
            mCheckDVBSInfo = false;

            mScannerManager.rollbackChannelsWhenScanNothingOnUIThread();
            getListener().onCompleted(ScannerListener.COMPLETE_CANCEL);
            break;
        case ABORT:
            mCheckDVBSInfo = false;
            mScannerManager.rollbackChannelsWhenScanNothingOnUIThread();
            getListener().onCompleted(ScannerListener.COMPLETE_ERROR);
            break;
        case FREQUENCY:
            getListener().onFrequence(scanProgress);
            break;
        case DVBT_FVP_IP_CHANNELS:
            mScannerManager.setIpChannels(scanProgress);//progress means channel num in this case
            getListener().onIPChannels(scanProgress, argv4);
            break;
        case DVBT_FVP_USER_SELECTION:
            getListener().onFvpUserSelection();
            break;
        case DVBT_FVP_SCAN_ERROR:
            getListener().onFvpScanError();
            break;
        case DVBT_FVP_SCAN_START:
            getListener().onFvpScanStart();
            break;
        case DVBT_ITALY_NO_LCN_TABLE_MSG:
            setOpType(TYPE_UI_OP_SHOW_TIA_NO_CHANNELS);
            setHasOpTODO(true);
            break;
        default:
            // throw new IllegalArgumentException("unknonw msg_id: " + msg_id);

            break;
        }
    }

    private void debugOP() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"debugOP()");
        // Fix CR: DTV00600618
        if(hasOpTODO && getOpType() == TYPE_UI_OP_SHOW_TIA_NO_CHANNELS){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "debugOP() DVBT_ITALY_NO_LCN_TABLE_MSG return");
            return;
        }

        if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "debugOP() atv source return!!!");
            return;
        }

        MtkTvScanDvbtBase.UiOpSituation opSituation = MtkTvScan.getInstance().getScanDvbtInstance().uiOpGetSituation();

        setHasOpTODO(false);
        do {
            int tunerMode = getScannerManager().getTuneMode();
            if (tunerMode >= CommonIntegration.DB_SAT_OPTID) {
                if (mCheckDVBSInfo) {
                    mCheckDVBSInfo = false;
                    setOpType(TYPE_UI_OP_DVBS_BOUQUET_INFO);
                    setHasOpTODO(true);
                }
            } else {
                MtkTvScanDvbtBase.FavNwk[] nwkList = MtkTvScan.getInstance()
                        .getScanDvbtInstance().uiOpGetFavNwk();
                if (opSituation.favouriteNeteorkPopUp && nwkList != null
                        && nwkList.length > 0) {
                    // if (nwkList != null && nwkList.length > 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("nwkList!=null,print----->");
                    setOpType(TYPE_UI_OP_NOR_FAV_NWK);
                    for (MtkTvScanDvbtBase.FavNwk fav : nwkList) {
                        String nwkName = fav.networkName;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("networkName:" + nwkName);
                    }
                    setHasOpTODO(true);
                    break;
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("debugOP(),FavNwk==null || favouriteNeteorkPopUp=false");
                }

                MtkTvScanDvbtBase.LcnConflictGroup[] lcnList = MtkTvScan
                        .getInstance().getScanDvbtInstance()
                        .uiOpGetLcnConflictGroup();
                if (opSituation.lcnConflictPopUp && lcnList != null
                        && lcnList.length > 0) {
                    // if (lcnList != null && lcnList.length > 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("lcnList!=null,print----->");
                    setOpType(TYPE_UI_OP_ITA_GROUP);
                    for (MtkTvScanDvbtBase.LcnConflictGroup group : lcnList) {
                        String nwkName = group.channelName[0];
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("channelName:" + nwkName);
                    }
                    setHasOpTODO(true);
                    break;
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("debugOP(),LcnConflictGroup==null || lcnConflictPopUp=false");
                }

                MtkTvScanDvbtBase.LCNv2ChannelList[] lcnV2List = MtkTvScan
                        .getInstance().getScanDvbtInstance()
                        .uiOpGetLCNv2ChannelList();
                if (opSituation.lcnv2PopUp && lcnV2List != null
                        && lcnV2List.length > 0) {
                    // if (lcnV2List != null && lcnV2List.length > 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("lcnV2List!=null,print----->");
                    setOpType(TYPE_UI_OP_LCNV2_CH_LST);
                    for (MtkTvScanDvbtBase.LCNv2ChannelList channel : lcnV2List) {
                        String nwkName = channel.channelListName;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("channelListName:" + nwkName);
                    }
                    setHasOpTODO(true);
                    break;
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("debugOP(),LCNv2ChannelList==null || lcnv2PopUp=false");
                }

                MtkTvScanDvbtBase.TargetRegion[] regionList = MtkTvScan
                        .getInstance().getScanDvbtInstance()
                        .uiOpGetTargetRegion();
                if (opSituation.targetRegionPopUp && regionList != null
                        && regionList.length > 0) {
                    // if (regionList != null && regionList.length > 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("regionList!=null,print----->");
                    if(getOpType() == TYPE_UI_OP_DVBT_FVP_ERROR){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("regionList!=null,show fvp error first");
                    }else {
                        setOpType(TYPE_UI_OP_UK_REGION);
                    }
                    for (int i = 0; i < regionList.length; i++) {
                        String nwkName = regionList[i].name;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(String.format(
                                "-------regionList[%d]-------", i));
                        dumpRegionInfo(regionList[i]);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("name:" + nwkName);
                    }
                    setHasOpTODO(true);
                    mScannerManager.reMapRegions(Arrays.asList(regionList));
                    break;
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("debugOP(),TargetRegion==null || targetRegionPopUp=false");
                }
                if (opSituation.storeSvcPopUp
                        && mScannerManager.showPTSaveChannelDialog()) {
                    // if (mScannerManager.showPTSaveChannelDialog()) {
                    setHasOpTODO(true);
                    setOpType(TYPE_UI_OP_SAVE_CHANNEL);
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("debugOP(),not show PT save channel dialog");
                }
            }
        } while (false);
    }

    private void dumpRegionInfo(TargetRegion targetRegion) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TargetRegion", "internalIdx," + targetRegion.internalIdx);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TargetRegion", "level," + targetRegion.level);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TargetRegion", "primary," + targetRegion.primary);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TargetRegion", "secondary," + targetRegion.secondary);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TargetRegion", "tertiary," + targetRegion.tertiary);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TargetRegion", "name," + targetRegion.name);
    }

    public ScannerListener getListener() {
        return getScannerManager().getListener();
    }

    public ScannerManager getScannerManager() {
        return mScannerManager;
    }

    public synchronized void setScannerManager(ScannerManager scannerManager) {
        this.mScannerManager = scannerManager;
    }

    public static synchronized ScanCallback getInstance(ScannerManager scannerManager) {
        if (mScanCallBack == null) {
            mScanCallBack = new ScanCallback();
        }
        mScanCallBack.setScannerManager(scannerManager);
        return mScanCallBack;
    }

    public boolean isHasOpTODO() {
        return hasOpTODO;
    }

    public void setHasOpTODO(boolean hasOpTODO) {
        this.hasOpTODO = hasOpTODO;
    }

    public int getOpType() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("getOpType()," + opType);
        return opType;
    }

    public void setOpType(int opType) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("setOpType()," + opType);
        if(com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        }
        this.opType = opType;
    }
    public int getTkgsNfyInfoMask() {
        return tkgsNfyInfoMask;
      }

      public void setTkgsNfyInfoMask(int tkgsNfyInfoMask) {
        this.tkgsNfyInfoMask = tkgsNfyInfoMask;
      }
    class ScanCallbackMsg {
        int msg_id;
        int scanProgress;
        int channelNum;
        int argv4;

        public ScanCallbackMsg(int msgId, int scanProgress, int channelNum,
                int argv4) {
            super();
            this.msg_id = msgId;
            this.scanProgress = scanProgress;
            this.channelNum = channelNum;
            this.argv4 = argv4;
        }

        @Override
        public boolean equals(Object o) {
            // TODO Auto-generated method stub

            if (o instanceof ScanCallbackMsg) {
                ScanCallbackMsg tmp = (ScanCallbackMsg) o;
                if (tmp.msg_id == msg_id
                        && tmp.scanProgress == scanProgress
                        && tmp.channelNum == channelNum
                        && tmp.argv4 == argv4) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public int hashCode() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("hashCode()");
            return super.hashCode();
        }
    }
    
    public void clearLastCallbackMsg() {
        mLastMsg = null;
    }
}
