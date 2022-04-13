package com.mediatek.wwtv.setting.base.scan.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.DvbsM7MainSatelliteIdx;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvSbDvbsScanData;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvScanTpInfo;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.ScanDvbsRet;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;


public class DVBSScanner implements IScanner {

	private static final String TAG = "DVBSScanner";
	private final boolean DEV_TAG = com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag;
	
	private static final int LNB_DETCTED_MASK = 1 << 5;

	private MtkTvScanDvbsBase mScan;
	private DVBSSettingsInfo mParams;

	// private final int frequency = 9750 + 1200; // debug
	// private final int symbolRate = 29950; // debug

	public MtkTvScanDvbsBase dvbsScan;
	public MtkTvScanDvbsBase.MtkTvSbDvbsScanData scanData;
	public ScanCallback mCallback;

	public ScannerManager mScanManager;

	public DVBSScanner(ScannerManager manager, ScanParams params,
			ScanCallback callback) {
		super();
		mScanManager = manager;

		mScan = new MtkTvScanDvbsBase();
		mCallback = callback;

		if (params != null) {
			mParams = (DVBSSettingsInfo) params;
		}
	}

	@Override
	public void fullScan() {
		// TODO Auto-generated method stub
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fullScan()");
		if (DEV_TAG) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
		}

		if (mParams == null) {
			mParams = new DVBSSettingsInfo();
		}

		dvbsScan = new MtkTvScanDvbsBase();
		scanData = initScanData();

		switch (mParams.scanMode) {
		case DVBSSettingsInfo.NETWORK_SCAN_MODE:
			networkScanAndTpInfo();
			break;
		case DVBSSettingsInfo.FULL_SCAN_MODE:
			autoScan();
			break;
		case DVBSSettingsInfo.TP_SCAN_MODE:
			manualTurningScan();
			break;
		default:
			autoScan();
			break;
		}
	}
	  public boolean cancelDvbsLnbScan(){
	      MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;
	      MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
	      dvbsRet = dvbsScan.dvbsM7LNBSearchCancel();
	      return MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK == dvbsRet;
	  }
	  
	  public void dvbsM7LNBScan() {
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsM7LNBScan");
	      MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;
	      MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
	      dvbsRet = dvbsScan.dvbsM7LNBSearch();
	      if(MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK != dvbsRet){
	         //error handling
	          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsM7LNBScan Error");
	      }
	  }
	  
	  public void dvbsM7ChannelScan() {
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsM7ChannelScan");
	      if (mParams == null) {
	            mParams = new DVBSSettingsInfo();
	      }
	      MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;
          MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
	      int dvbsScanCFGSetting = 0;//dvbsScan.SB_DVBS_CONFIG_INSTALL_FREE_SVC_ONLY;
		  if(mParams.isUpdateScan){
			  dvbsScanCFGSetting = dvbsScan.SB_DVBS_CONFIG_BGM_SCAN;
		  }
	      dvbsRet = dvbsScan.dvbsM7ChannelSearch(dvbsScanCFGSetting);//default is 0,need fix
	      if(MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK != dvbsRet){
	          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsM7ChannelScan Error");
	      }
	  }
	  
	  public void canceldvbsM7ChannelScan() {
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "canceldvbsM7ChannelScan");
	      MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;
	      MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
	      dvbsRet = dvbsScan.dvbsM7ChannelSearchCancel();
	      if(MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK != dvbsRet){
	          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "canceldvbsM7ChannelScan Error");
	      }
	  }
	private MtkTvSbDvbsScanData initScanData() {
		// TODO Auto-generated method stub
		MtkTvSbDvbsScanData scanData = dvbsScan.new MtkTvSbDvbsScanData();

		scanData.i4SatlID = CommonIntegration.getInstance().getSvl();
		scanData.i4SatlRecID = mParams.getSatelliteInfo().getSatlRecId();
		String satName = mParams.getSatelliteInfo().getSatName();
		scanData.bIsBgm = mParams.isUpdateScan;
		scanData.bIsMduDetect = false;
		scanData.bIsGetInfoStep = false;

		if (ScanContent.isPreferedSat()) {
			scanData.i4DvbsOperatorName = MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_BRDCSTER);
		} else {
			scanData.i4DvbsOperatorName = MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_OTHERS;
		}

		scanData.i4EngCfgFlag = calculatorI4EngCfgFlag(mParams,scanData.i4EngCfgFlag);

		if (scanData.i4DvbsOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FRANSAT) {
			// FRANSAT LOGO
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d("FRANSAT LOGO,BATId:" + mParams.BATId);
			if (mParams.BATId == -1) {
				scanData.bIsGetInfoStep = true;
			} else {
				scanData.bIsGetInfoStep = false;
				scanData.scanInfo.networkScanInfo.i4BatID = mParams.BATId;
				mParams.BATId = -1;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("FRANSAT LOGO,BATId-2:" + scanData.scanInfo.networkScanInfo.i4BatID);
			}
		} else if (scanData.i4DvbsOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBS_OPERATOR_NAME_TRICOLOR,i4BatID:" + mParams.i4BatID+",scanData.i4SatlRecID:"+scanData.i4SatlRecID);
          MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
          MtkTvScanDvbsBase.MtkTvSbDvbsBGMData bgmData = base.new MtkTvSbDvbsBGMData();
          base.dvbsGetSaveBgmData(bgmData);
          int batId = -1;
          for(MtkTvScanDvbsBase.MtkTvSbDvbsBGMData.OneBGMData one : bgmData.bgmData_List){
              if(one != null){
                  if(one.i4SatRecID == scanData.i4SatlRecID){
                      if(one.networkScanInfo != null){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "one i4Batid"+one.networkScanInfo.i4BatID);
                          batId = one.networkScanInfo.i4BatID;
                          break;
                      }
                  }

              }
          }         
          
          if(!mParams.isUpdateScan){
              if(mParams.i4BatID < 0) {
                  scanData.bIsGetInfoStep = true;
              } else {
                  scanData.bIsGetInfoStep = false;
                  scanData.scanInfo.networkScanInfo.i4BatID = mParams.i4BatID;
                  mParams.i4BatID = -1;
              }
          }else {
              scanData.bIsGetInfoStep = false;
              scanData.scanInfo.networkScanInfo.i4BatID = batId;
          }       
          
        } else if (scanData.i4DvbsOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS) {
            // for both sat-re-scan and sat-update-scan
            // TKGS LOGO
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("TKGS LOGO, callback tkgsType info:" + mParams.tkgsType);
            if (mParams.tkgsType == -1) {
              scanData.bIsGetInfoStep = true;
            } else {
              scanData.bIsGetInfoStep = false;
              mParams.tkgsType = -1;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d("TKGS LOGO,tkgsType#:" + mParams.tkgsType);
            }
              } else if (scanData.i4DvbsOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_EUTELSAT) {
			// MDU LOGO.
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("MDU LOGO,mduType:" + mParams.mduType);
			if (mParams.mduType == -1) {
				scanData.bIsMduDetect = true;
			} else {
				scanData.bIsMduDetect = false;
				mParams.mduType = -1;
			}
			dvbsScan.dvbsGetBatId(MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_EUTELSAT);
			scanData.scanInfo.networkScanInfo.i4BatID = dvbsScan.getBat_id;
		} else {
			dvbsScan.dvbsGetBatId(scanData.i4DvbsOperatorName);
			scanData.scanInfo.networkScanInfo.i4BatID = dvbsScan.getBat_id;
		}
		if (isM7ScanMode() || isDsmartScanMode() || scanData.i4DvbsOperatorName == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NC_PLUS) {
		    int mainIndex = 0;
		    if(isM7ScanMode()){
		        dvbsScan.dvbsGetM7MainSatIdx(scanData.i4DvbsOperatorName);
		        DvbsM7MainSatelliteIdx mainSatID = dvbsScan.getM7MainSat_idx;
		        mainIndex = Arrays.asList(DvbsM7MainSatelliteIdx.values()).indexOf(mainSatID) - 1;
		    }
			List<SatelliteInfo> list = ScanContent.getDVBSsatellites(mParams.context);
			mainIndex = Math.min(mainIndex, list.size() - 1);
			mainIndex = Math.max(mainIndex, 0);
			List<SatelliteInfo> enableList = new ArrayList<SatelliteInfo>();
			for (SatelliteInfo info : list) {
			    if (info.getEnable()) {
			        enableList.add(info);
			    }
            }
			if (list.get(mainIndex).getEnable()) {
				scanData.i4SatlRecID = list.get(mainIndex).getSatlRecId();
			} else {
				scanData.i4SatlRecID = enableList.get(0).getSatlRecId(); // enable
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ">>>" + mScanManager.getDVBSData().satList[0] + ">>>" + scanData.i4SatlRecID);
			mScanManager.getDVBSData().satList[0] = scanData.i4SatlRecID;
			SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(((DVBSSettingsInfo)mParams).context, scanData.i4SatlRecID);
			mScanManager.getDVBSData().currentSatName = info.getSatName();
			if (enableList.size() > 1) {
				scanData.scanInfo.networkScanInfo.i4AssocSatlRecNum = enableList.size() - 1;
				int index = 0;
				for (SatelliteInfo satelliteInfo : enableList) {
				    if (satelliteInfo.getSatlRecId() != scanData.i4SatlRecID) {
				        scanData.scanInfo.networkScanInfo.aAssocSatlRec[index] = satelliteInfo.getSatlRecId();
				        index++;
				    }
                }
			} else {
				scanData.scanInfo.networkScanInfo.i4AssocSatlRecNum = 0;
			}
//			// For save BGM info.
//			mScanManager.getDVBSData().params.networkInfo.put(
//					scanData.i4SatlRecID, scanData.scanInfo.networkScanInfo);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("isM7ScanMode()," + "list.size:" + list.size() + ",mainIndex:" + mainIndex);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("isM7ScanMode()," + "enableList.size:" + enableList.size());
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("isM7ScanMode()i4SatlRecID:," + scanData.i4SatlRecID);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("isM7ScanMode()," + "AssocSatNum:" + scanData.scanInfo.networkScanInfo.i4AssocSatlRecNum);
			for (int i = 0; i < 3; i++) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("isM7ScanMode(),Assoc:" + "Index:" + i + "," + scanData.scanInfo.networkScanInfo.aAssocSatlRec[i]);
			}
		}

		// For save BGM info.
		mScanManager.getDVBSData().params.networkInfo.put(scanData.i4SatlRecID, scanData.scanInfo.networkScanInfo);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format(
				"ScanMode:%d,Channels:%d,svlID:%d,op:%d,satID:%d,satName:%s",
				mParams.scanMode, mParams.scanChannels, scanData.i4SatlID,
				scanData.i4DvbsOperatorName, scanData.i4SatlRecID, satName));
		return scanData;
	}

	private static int calculatorI4EngCfgFlag(DVBSSettingsInfo mParams,int sourceValue) {
	    return calculatorI4EngCfgFlag(mParams.scanChannels, mParams.scanStoreType, sourceValue);
	}
	private static int calculatorI4EngCfgFlag(int scanChannels, int scanStoreType, int sourceValue) {
	    if (scanChannels == DVBSSettingsInfo.CHANNELS_ENCRYPTED) {
	        sourceValue |= MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_SCRAMBLE_SVC_ONLY;
	    } else if (scanChannels == DVBSSettingsInfo.CHANNELS_FREE) {
	        sourceValue |= MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_FREE_SVC_ONLY;
	    } else {
	        sourceValue &= ~MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_SCRAMBLE_SVC_ONLY;
	        sourceValue &= ~MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_FREE_SVC_ONLY;
	    }
	    
	    if (scanStoreType == DVBSSettingsInfo.CHANNELS_STORE_TYPE_DIGITAL) {
	        sourceValue |= MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_TV_SVC_ONLY;
	    } else if (scanStoreType == DVBSSettingsInfo.CHANNELS_STORE_TYPE_RADIO) {
	        sourceValue |= MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_RADIO_SVC_ONLY;
	    } else {
	        sourceValue &= ~MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_TV_SVC_ONLY;
	        sourceValue &= ~MtkTvScanDvbsBase.SB_DVBS_CONFIG_INSTALL_RADIO_SVC_ONLY;
	    }
	    return sourceValue;
	}

	private void autoScan() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoScan()");
		MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;

		scanData.i4DvbsOperatorName = MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_OTHERS;
		scanData.eSbDvbsScanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_AUTO_MODE;

		dvbsRet = dvbsScan.dvbsStartScan(scanData);
		checkResult(dvbsRet);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoScan(),dvbsRet:" + dvbsRet.name());
	}

	private void networkScanAndTpInfo() {
		// Ask MtkAPI to do default network scan
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "networkScanAndTpInfo()");
		MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;
		dvbsScan = new MtkTvScanDvbsBase();

		scanData.eSbDvbsScanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_NETWORK_SCAN;

		// scanData.i4EngCfgFlag = 0;
		// scanData.bIsGetInfoStep = false;
		scanData.bIsM7PortDetect = false;
		// scanData.bIsMduDetect = false;

		MtkTvScanTpInfo tpInfo = null;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isUpdateScan=" + mParams.isUpdateScan);
		if(mParams.isUpdateScan){ // Dvbs Update Scan will use saved bgm TP info, nor satl TP info
			tpInfo = ScanContent.getSavedBgmTPInfo(scanData.i4SatlRecID);
		}else{
			tpInfo = ScanContent.getDVBSTransponder(scanData.i4SatlRecID);
		}
		if (tpInfo != null) {
			scanData.scanInfo.networkScanInfo.tpInfo.i4Frequency = tpInfo.i4Frequency;
			scanData.scanInfo.networkScanInfo.tpInfo.ePol = tpInfo.ePol;
			scanData.scanInfo.networkScanInfo.tpInfo.i4Symbolrate = tpInfo.i4Symbolrate;
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("networkScanAndTpInfo(),i4Frequency:"
				+ scanData.scanInfo.networkScanInfo.tpInfo.i4Frequency);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("networkScanAndTpInfo(),ePol:"
				+ scanData.scanInfo.networkScanInfo.tpInfo.ePol.name());
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("networkScanAndTpInfo(),i4Symbolrate:"
				+ scanData.scanInfo.networkScanInfo.tpInfo.i4Symbolrate);
		dvbsRet = dvbsScan.dvbsStartScan(scanData);
		checkResult(dvbsRet);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "networkScanAndTpInfo(),dvbsRet:" + dvbsRet.name());
	}

	private void manualTurningScan() {
		// Ask MtkAPI to do manual tuning scan
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "singleTPScan()");
		MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK;
		scanData.eSbDvbsScanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_SINGLE_TP_SCAN;

		MtkTvScanTpInfo tpInfo = ScanContent.getDVBSTransponder(scanData.i4SatlRecID);
		if (tpInfo != null) {
			scanData.scanInfo.singleTpScanInfo.i4Frequency = tpInfo.i4Frequency;
			scanData.scanInfo.singleTpScanInfo.ePol = tpInfo.ePol;
			scanData.scanInfo.singleTpScanInfo.i4Symbolrate = tpInfo.i4Symbolrate;
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"singleTPScan(),tpInfo==null,scan canceled!!!!");
			return;
		}
		dvbsRet = dvbsScan.dvbsStartScan(scanData);
		checkResult(dvbsRet);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "manualTurningScan(),dvbsRet:" + dvbsRet.name());
	}

	private void checkResult(MtkTvScanDvbsBase.ScanDvbsRet dvbsRet) {
		if (MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK != dvbsRet) {
			mCallback.dealScanMsg(mCallback.ABORT, 0, 0, 0);
		}
	}

	public static boolean isMDUScanMode() {
		int op = -1;

		if (ScanContent.isPreferedSat()) {
			op = MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_BRDCSTER);
		} else {
			return false;
		}
		return op == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_EUTELSAT;
	}
	
	public static boolean isDsmartScanMode() {
        int op = -1;

        if (ScanContent.isPreferedSat()) {
            op = MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_BRDCSTER);
        } else {
            return false;
        }
        return op == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_D_SMART;
    }

	public static boolean isNCPlusScanMode() {
        int op = -1;

        if (ScanContent.isPreferedSat()) {
            op = MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_BRDCSTER);
        } else {
            return false;
        }
        return op == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NC_PLUS;
    }

	public static boolean isM7ScanMode() {
		int op = -1;

		if (ScanContent.isPreferedSat()) {
			op = MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_BRDCSTER);
		} else {
			return false;
		}

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
		case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS:
		default:
			return false;
		}
	}

	@Override
	public void fullDTVScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fullATVScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public void singleRFScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelScan() {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan()");
		mScan.dvbsCancelScan();
	}

	@Override
	public void scanUp(int frequency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scanDown(int frequency) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isScanning() {
		// TODO Auto-generated method stub
		return false;
	}

	public static void forceSaveBGMData(ScannerManager manager, DvbsScanningData data, int svl) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData()");
		if (data != null) {
			if(data.params != null && (((DVBSSettingsInfo)data.params).scanMode == DVBSSettingsInfo.TP_SCAN_MODE
				/*|| (data.params != null && ((DVBSSettingsInfo)data.params).isUpdateScan)*/)){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData() TP scan mode Return!");
				return;
			}
			MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
			MtkTvScanDvbsBase.MtkTvSbDvbsBGMData bgmData = base.new MtkTvSbDvbsBGMData();

			com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),svl:"+svl);

			if (svl == 3) {
				bgmData.i4SatlID = MtkTvScanDvbsBase.SB_DVBS_GENERAL_LIST_ID;
			} else {
				bgmData.i4SatlID = MtkTvScanDvbsBase.SB_DVBS_PREFERRED_LIST_ID;
			}

			bgmData.i4DvbsOperatorName = ScanContent.getDVBSCurrentOP();
			bgmData.i4ScanTimes = data.totalSatSize;

			com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),op:"+bgmData.i4DvbsOperatorName+",scanTimes:"+bgmData.i4ScanTimes);

			for (int i = 0; i < data.totalSatSize; i++) {
			    if(i >= MtkTvScanDvbsBase.MAX_SAT_NUM){//max num is 4
			        com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData() out of MAX_SAT_NUM break");
			        break;
			    }
				bgmData.bgmData_List[i] = bgmData.new OneBGMData();
				SatelliteInfo info = manager.getDVBSData().satelliteInfoList.get(i);
				if (info.dvbsScanMode == DVBSSettingsInfo.FULL_SCAN_MODE) {
					bgmData.bgmData_List[i].eSbDvbsScanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_AUTO_MODE;
				} else if (info.dvbsScanMode == DVBSSettingsInfo.NETWORK_SCAN_MODE) {
					bgmData.bgmData_List[i].eSbDvbsScanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_NETWORK_SCAN;
				} else {
					bgmData.bgmData_List[i].eSbDvbsScanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_AUTO_MODE;
				}

				bgmData.bgmData_List[i].i4EngCfgFlag = calculatorI4EngCfgFlag(info.dvbsScanType, info.dvbsStoreType, bgmData.bgmData_List[i].i4EngCfgFlag);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBSScanner", "scanMode="+info.dvbsScanMode+",scanType="+info.dvbsScanType+",storeType="+info.dvbsStoreType);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),scanMode:"+bgmData.bgmData_List[i].eSbDvbsScanType+",scanChannels:"+bgmData.bgmData_List[i].i4EngCfgFlag
						+ ">>>" + data.satList[i]);

				bgmData.bgmData_List[i].i4SatRecID = data.satList[i];
				if(data.params != null) {
					bgmData.bgmData_List[i].networkScanInfo = data.params.networkInfo
							.get(bgmData.bgmData_List[i].i4SatRecID);
				}
				if (bgmData.bgmData_List[i].networkScanInfo == null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),networkScanInfo == null");
					bgmData.bgmData_List[i].networkScanInfo=base.new MtkTvSbDvbsNetworkScanInfo();
				} else {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),networkScanInfo != null>>" + bgmData.bgmData_List[i].networkScanInfo.i4BatID);
				}

				MtkTvScanTpInfo tpInfo = ScanContent
						.getDVBSTransponder(bgmData.bgmData_List[i].i4SatRecID);
				if (tpInfo != null) {
					bgmData.bgmData_List[i].networkScanInfo.tpInfo = tpInfo;
				}

				com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),i4Frequency:"+tpInfo.i4Frequency+",i4Symbolrate:"+tpInfo.i4Symbolrate+",tpInfo.ePol"+tpInfo.ePol.name());
			}

			ScanDvbsRet rect=base.dvbsSetSaveBgmData(bgmData);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("forceSaveBGMData(),rect:"+rect);
		}
	}

	@Override
	public void rangeATVFreqScan(int startFreq, int endFreq) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("rangeATVFreqScan()");

	}
	
	public static void initLNBDetectedMast(Context context) {
	  //init LNB detected mask 
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initLNBDetectedMast context="+context);
        List<SatelliteInfo> list = ScanContent.getDVBSsatellites(context);
        if(list != null && !list.isEmpty()){
            for(int i = 0; i < list.size(); i++){
                if(i < 4){
                    SatelliteInfo info = list.get(i);
                    if (info.getEnable()) {
                        info.setMask(info.getMask() | LNB_DETCTED_MASK);
                    }else {
                        info.setMask(info.getMask() & ~LNB_DETCTED_MASK);
                    }
                    ScanContent.saveDVBSSatelliteToSatl(info);
                }else {
                    break;
                }
            }

        }else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbsM7ChannelScan list empty");
        }
    }
}
