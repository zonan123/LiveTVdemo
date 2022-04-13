package com.mediatek.wwtv.setting.base.scan.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvHighLevel;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanBase.ScanMode;
import com.mediatek.twoworlds.tv.MtkTvScanBase.ScanType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.TargetRegion;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.base.scan.ui.ScanThirdlyDialog;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnCancelClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.scan.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.scan.TKGSUserMessageDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.MessageType;
import mediatek.sysprop.VendorProperties;

public class ScannerManager {
	private final static String TAG = "ScannerManager";
	private IScanner mEuScanner;
	private boolean rollback = false;
	private ScannerListener mScanListener;
	private MtkTvScan mScan;
	private ScanCallback callback;
	private TVContent mTV;
	private Context mContext;
	public final static int FULL_SCAN = 0;
	public final static int DTV_SCAN = 1;
	public final static int ATV_SCAN = 2;
	public final static int SIGNAL_RF_SCAN = 3;
	public final static int UPDATE_SCAN = 4;
	public final static int UPDATE_ATV_SCAN = 5;

	public final static int ATV_SCAN_UP = 6;
	public final static int ATV_SCAN_DOWN = 7;
	public final static int ADVANCE_SCAN = 8;

	public final static int DTV_UPDATE = 9;
	public final static int DTV_BGM = 10;
	public final static int DVBS_MULTI_SAT = 11;
	public final static int ATV_RANGE_FREQ_SCAN = 12;
	public final static int SA_ATV_UPDATE = 15;
	public final static int SA_DTV_UPDATE = 16;
	public final static int M7_LNB_Scan = 17;
	public final static int M7_Channel_Scan = 18;

	private int scanType = -1;

	enum Region {
		CN, US, SA, EU
	}

	public enum Action {
		DTV, ATV, DTV_UPDATE, ATV_UPDATE, SA_ATV_UPDATE, SA_DTV_UPDATE
	}

	private final static int LEVEL_1 = 1;
	private final static int LEVEL_2 = 2;
	private final static int LEVEL_3 = 3;

	private RegionUtils mRegionMgr;
	private ActionList<Action> actionList = new ActionList<Action>();
    private ScanParams mScanParams;
    Region region;
	public DvbsScanningData mDVBSData = new DvbsScanningData();
	private boolean mSAScanDTVFirst = true;
	private boolean mIsBroadcastScan = false;
	private boolean mIsAdviseScan = false;
	public ConfirmDialog mFvpScanErrorDialog = null;
	public ScannerManager() {
        super();
        mScan = MtkTvScan.getInstance();
        initRegion();
        setRegionMgr(new RegionUtils()); // only for EU-Full_Scan
        callback = ScanCallback.getInstance(this);
      }
	 public ScannerManager(Context context, TVContent tvcontent) {
	     super();
	     mContext = context;
	     mScan = MtkTvScan.getInstance();
	     initRegion();
	     mTV = tvcontent;
	     setRegionMgr(new RegionUtils()); // only for EU-Full_Scan
	     callback = ScanCallback.getInstance(this);
	   }
	public void startScan(final int type, final ScannerListener listener, final ScanParams params) {
		RxBus.instance.onEvent(ActivityDestroyEvent.class)
			.filter(it -> listener != null && listener.getClass().getName().startsWith(it.activityClass.getName()))
			.firstElement()
			.doOnSuccess(it ->{
				Log.i(TAG, "Rxbus free");
				mScanListener = null;
			})
			.subscribe();
	    new Thread(new Runnable() {
	        @Override
            public void run() {
                Log.d(TAG,"ScannerManager.startScan() "+type);

                if (isScanning()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is Scanning,return!!");
                }

                clearScanStates();
                //clearCallbackParams();

                mScanListener = listener;
                callback = ScanCallback.getInstance(ScannerManager.this);
                callback.clearLastCallbackMsg();
                scanType = type;
                getActionList().clear();
                mScanParams = null;

                switch (type) {
                    case FULL_SCAN:
                        startFullScan(params);
                        break;
                    case DTV_SCAN:
						if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) ||
								(mTV.isUKCountry() && !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT))) {
							setRollbackCleanChannel();
						}
                        startDTVScan(params);
                        break;
                    case ATV_SCAN:
                        if (isCN()) {
                            setRollbackCleanChannel();
                            if (mTV != null && mTV.mHandler != null) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start ATV scan delay MessageType.MESSAGE_START_SCAN");
                                mTV.mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_START_SCAN, 800);
                            } else {
                                startATVAutoScan();
                            }
                        } else {
							if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
								setRollbackCleanChannel();
							}
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "other startATVAutoScan");
                            startATVAutoScan();
                        }
                        break;
                    case ATV_SCAN_UP:
                        startATVScanUpOrDown(true, params);
                        break;
                    case ATV_SCAN_DOWN:
                        startATVScanUpOrDown(false, params);
                        break;
                    case ATV_RANGE_FREQ_SCAN:
                        startATVRangeFreqScan(params);
                        break;
                    case SIGNAL_RF_SCAN:
                        startSingleRFScan(params);
                        break;
                    case UPDATE_SCAN:
                        startUpdateScan(params);
                        break;
                    case UPDATE_ATV_SCAN:
                        startATVUpdateScan();
                        break;
                    case DTV_UPDATE:
                        startEuDTVUpdateScan(params);
                        break;
                    case DVBS_MULTI_SAT:
                        startDvbsMultiScan(params);
                        break;
                    case SA_ATV_UPDATE:
                        mScan.startScan(ScanType.SCAN_TYPE_NTSC, ScanMode.SCAN_MODE_ADD_ON, true);
                        break;
                    case SA_DTV_UPDATE:
                        mScan.startScan(ScanType.SCAN_TYPE_ISDB, ScanMode.SCAN_MODE_ADD_ON, true);
                        break;
                    case M7_LNB_Scan :
                        startDvbsLNBScan();
                        break;
                    case M7_Channel_Scan:
                        if(!isFastScanSecondScan){
                            setRollbackCleanChannel(true);
                        }
                        startDvbsM7ChannelScan(params);
                        break;
                    case DTV_BGM:
                        startDTVBGMScan();
                        break;
                    default:
                        break;
                }
            }

        }).start();

	}
	  DVBSScanner mDVBSScanner;
	  public void setRollbackCleanChannel() {
	      setRollbackCleanChannel(false);
      }
	  public void setRollbackCleanChannel(boolean clearLOL) {
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"set rollback true and clean channel");
	      setRollback(true);
	      TVContent.createChanneListSnapshot(); // backup channel DB.
	      clearChannelNum();
	      if(mIsAdviseScan) {
			  EditChannel.getInstance(mContext).cleanChannelListForAdviseScan();
		  }else {
			  TVContent.getInstance(mContext).clearCurrentSvlChannelDB(clearLOL);
		  }
	  }
	  private void startDvbsLNBScan(){
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDvbsLNBScan()");
	      mDVBSScanner = new DVBSScanner(this,null,callback);
	      DVBSScanner.initLNBDetectedMast(mContext);//need init LNB detected mask before lnb search
	      mDVBSScanner.dvbsM7LNBScan();
	  }
	  private void startDvbsM7ChannelScan(ScanParams params){
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDvbsM7ChannelScan()");
	      mDVBSData.satList=new int[1];
	      SatelliteInfo satelliteInfo = ((DVBSSettingsInfo)params).getSatelliteInfo();
	      mDVBSData.satList[0]=satelliteInfo.getSatlRecId();
	      mDVBSData.currentSatIndex=0;
	      /*MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
	      base.dvbsGetM7MainSatIdx(ScanContent.getDVBSCurrentOP());
	      MtkTvScanDvbsBase.DvbsM7MainSatelliteIdx idx = base.getM7MainSat_idx;
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DvbsM7MainSatelliteIdx ="+idx);*/
	      mDVBSData.currentSatName= getM7SatelliteName();
	      mDVBSData.orbPos = satelliteInfo.getOrbPos();
	      mDVBSData.totalSatSize=1;
	      mDVBSData.isUpdateScan=((DVBSSettingsInfo)params).isUpdateScan;
	      mDVBSData.params=params;
	      mDVBSScanner = new DVBSScanner(this,params,callback);
	      mDVBSScanner.dvbsM7ChannelScan();
	  }
	  /*private String getM7SatelliteName(MtkTvScanDvbsBase.DvbsM7MainSatelliteIdx idx) {
          String last = idx.toString().substring(idx.toString().lastIndexOf("_")+1);
          int satIndex = -1;
          try {
            satIndex = Integer.parseInt(last);
          } catch (Exception e) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d("Exception()");
        }
          if(satIndex == -1){
              return "";
          }else {
              List<SatelliteInfo> list = ScanContent.getDVBSsatellites(mContext);
              if(list.size() > satIndex){
                  return list.get(satIndex).getSatName();
              }else {
                return "";
            }
        }
      }*/

	  /*
	   * Check orbit 23.5 first then 19.2, if not of these two,
	   * return first enabled satellite name
	   */
	  private String getM7SatelliteName() {
	      List<SatelliteInfo> list = ScanContent.getDVBSEnablesatellites(mContext);
	      if(list.isEmpty()){
	          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getM7SatelliteName() return null");
	          return "";
	      }
          mDVBSData.satelliteInfoList.clear();
          mDVBSData.satelliteInfoList.addAll(list);

	      int index192 = -1;
	      for(int i = 0; i < list.size(); i++){
	          if(list.get(i).getOrbPos() == 235){
                  return list.get(i).getSatName();
              }else if(list.get(i).getOrbPos() == 192){
                  if(index192 == -1){ //only find first time
                      index192 = i;
                  }
              }
	      }

	      if(index192 != -1){
              return list.get(index192).getSatName();
          }else {
              return list.get(0).getSatName();
          }
	  }

	  public void cancleDvbsM7ChannelScan(){
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancleDvbsM7ChannelScan()");
	      if (mDVBSScanner!=null) {
	          mDVBSScanner.canceldvbsM7ChannelScan();
	    }
	  }
	  public void cancleDvbsM7LNBSearch(){
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancleDvbsM7LNBSearch()");
	      if (mDVBSScanner!=null) {
	          mDVBSScanner.cancelDvbsLnbScan();
	    }
	  }
	private void startDTVScan(ScanParams params) {
		// TODO Auto-generated method stub

		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mScan.startDTVScan()");
		clearCallbackParams();
		ScanType type = null;
		switch (region) {
		case US:
			type = ScanType.SCAN_TYPE_US;
			break;
		case CN:
			startCNDTVScan(params);
			return;
		case SA:
			type = ScanType.SCAN_TYPE_SA;
			break;

		default:
			type = ScanType.SCAN_TYPE_US;
			break;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "region?" + region.name());

		if (isEU()) {
			startEUDTVScan(params);
		} else {
			mScan.startScan(type, ScanMode.SCAN_MODE_FULL, true);
		}
	}

	private void startDTVBGMScan() {
        // TODO Auto-generated method stub

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDTVBGMScan()");
        int tuneMode = getTuneMode();

        switch (tuneMode) {
        case MtkTvConfigTypeBase.BS_SRC_AIR: // antenna
            mEuScanner = new DVBTScanner();
            ((DVBTScanner)mEuScanner).bgmScan();
            //
            break;
        default:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDTVBGMScan() fail");
            break;
        }
    }

	public void setListener(ScannerListener listener) {
		mScanListener = listener;
		RxBus.instance.onEvent(ActivityDestroyEvent.class)
			.filter(it -> listener != null && listener.getClass().getName().startsWith(it.activityClass.getName()))
			.firstElement()
			.doOnSuccess(it ->{
				Log.i(TAG, "Rxbus free");
				mScanListener = null;
			}).subscribe();
	}

	public ScannerListener getListener() {
		return mScanListener;
	}

	private void startATVScan() {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVScan()");
		if (isEU() || isCN()) {
			getActionList().remove(Action.ATV); // true? false? whatever. Just
			mEuScanner = new EUATVScanner(this);
			mEuScanner.fullATVScan();
		}
	}

	private void stopTVBeforeScanATVOnPIPMode() {
		// TODO Auto-generated method stub
		if (CommonIntegration.getInstance().isPipOrPopState()) {
			if (CommonIntegration.getInstance().getCurrentFocus()
					.equalsIgnoreCase("sub")) {
//				MtkTvHighLevel mHighLevel = new MtkTvHighLevel();
//				mHighLevel.stopTV();
				MtkTvBroadcast.getInstance().syncStop("sub", false);
			}
		}
	}

	private void startTVBeforeScanATVOnPIPMode() {
		// TODO Auto-generated method stub
		if (CommonIntegration.getInstance().isPipOrPopState()) {
			if (CommonIntegration.getInstance().getCurrentFocus()
					.equalsIgnoreCase("sub")) {
				MtkTvHighLevel mHighLevel = new MtkTvHighLevel();
				mHighLevel.startTV();
			}
		}
	}

	private void startATVUpdateScan() {
		if (isEU() || isCN()) {
			getActionList().remove(Action.ATV_UPDATE); // true? false? whatever.
														// Just
			if(TVContent.getInstance(mContext).isCOLRegion()){
			    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVUpdateScan NTSC for col region");
			    scanType = UPDATE_ATV_SCAN;
			    mScan.startScan(ScanType.SCAN_TYPE_NTSC, ScanMode.SCAN_MODE_ADD_ON, true);
			}else {
			    mEuScanner = new EUATVScanner(this);
			    mEuScanner.updateScan();
            }
		}
	}

	private void startATVScanUpOrDown(boolean up, ScanParams params) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVScanUpOrDown()");
		if (isEU()) {
			mEuScanner = new EUATVScanner(this);
			if (params == null || params.freq == -1) {
				startATVScan();
				return;
			}

			// Fix CR: DTV00598127
			stopTVBeforeScanATVOnPIPMode();

			clearChannelNum();
			if (up) {
				mEuScanner.scanUp(params.freq);
			} else {
				mEuScanner.scanDown(params.freq);
			}

		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVScanUpOrDown(),NotEU");
		}
	}

	private void startATVRangeFreqScan(ScanParams params) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVRangeFreqScan()");
		if (isCN()) {
			mEuScanner = new EUATVScanner(this);
			if (params == null || params.startfreq == -1 || params.endfreq == -1) {
				startATVScan();
				return;
			}
			// Fix CR: DTV00598127
			stopTVBeforeScanATVOnPIPMode();

			clearChannelNum();
			mEuScanner.rangeATVFreqScan(params.startfreq, params.endfreq);
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVRangeFreqScan(),NotCN");
		}
	}

	public boolean isScanning() {
		// TODO Auto-generated method stub
		boolean isScanning = MtkTvScan.getInstance().isScanning();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isScanning?" + isScanning);
		return isScanning;
	}

	/**
	 *
	 * @param listener
	 * @param params
	 *            If u won't set params,please set null.
	 */
	/*private void startScan(ScannerListener listener, ScanParams params) {
		startScan(FULL_SCAN, listener, params);
	}*/

	private void startSingleRFScan(ScanParams params) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startSingleRFScan()-Only EU/CN??");

		if (isEU()) {
			if (TVContent.getInstance(mContext).isHKRegion()) {
				startCNSingleRFScan(params);
			} else {
				startEUSingleRFScan(params);
			}
		} else if (isCN()) {
			startCNSingleRFScan(params);
		}
	}

	private void startUpdateScan(ScanParams params) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startUpdateScan()");
		ScanType type = null;
		switch (region) {
		case US:
			type = ScanType.SCAN_TYPE_US;
			break;
		case CN:
			type = ScanType.SCAN_TYPE_ATSC;
			break;
		case SA:
			int tunerMode = getTuneMode();
			if (tunerMode == 1) {//cable
				type = ScanType.SCAN_TYPE_NTSC;
			} else if (mSAScanDTVFirst) {//antenna DTV
				type = ScanType.SCAN_TYPE_ISDB;
			} else {//antenna ATV
				type = ScanType.SCAN_TYPE_NTSC;
			}
			break;

		default:
			type = ScanType.SCAN_TYPE_US;
			break;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "region?" + region.name());

		if (isEU()) {
			startEUUpdateScan(params);
		} else if (isSA()) {
			int tunerMode = getTuneMode();
			getActionList().clear();
			mScanParams = null;
			if (tunerMode == 0) {
				if (mSAScanDTVFirst) {
					getActionList().add(Action.SA_ATV_UPDATE);
				} else {
					getActionList().add(Action.SA_DTV_UPDATE);
				}
				getActionList().totalScanActionSize = 2;
			}
			mScan.startScan(type, ScanMode.SCAN_MODE_ADD_ON, true);
		} else {
			mScan.startScan(type, ScanMode.SCAN_MODE_ADD_ON, true);
		}

	}

	private void startFullScan(ScanParams params) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startFullScan()");
		clearCallbackParams();

		ScanType type = null;
		setRollback(getTuneMode() < 2);//dvbs no need rollback flow
		TVContent.createChanneListSnapshot(); // backup channel DB.
		TVContent.backUpDVBSOP();//backup DVBS OP
		clearChannelNum();

		switch (region) {
		case US:
			type = ScanType.SCAN_TYPE_US;
			break;
		case CN:
			startCNFullScan(params);
			return;
		case SA:
			type = ScanType.SCAN_TYPE_SA;
			break;

		default:
			type = ScanType.SCAN_TYPE_US;
			break;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "region?" + region.name());
		if (isEU()) {
			startEUFullScan(params);
		} else {
			mScan.startScan(type, ScanMode.SCAN_MODE_FULL, true);
		}
	}

	private void startCNFullScan(ScanParams params) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startCNFullScan()");
		getActionList().clear();
		mScanParams = null;
		//boolean atvFirst = true;
		TVContent.clearChannelDB(); // clear DVBT||DVBC||DVBS DB.
		//if (atvFirst) {
			getActionList().add(Action.DTV);
			mScanParams = params;
//		} else {
//			getActionList().add(Action.ATV);
//		}
		getActionList().totalScanActionSize = 2;
		//if (atvFirst) {
			startATVScan();
//		} else {
//			startCNDTVScan(params);
//		}
	}

	private void startDvbsMultiScan(ScanParams params){//maybe has more than one satellite to scan
		getDVBSData().currentSatIndex++;
		int satId = getDVBSData().satList[getDVBSData().currentSatIndex];
		SatelliteInfo info= getDVBSData().satelliteInfoList.get(getDVBSData().currentSatIndex);//ScanContent.getDVBSsatellitesBySatID(((DVBSSettingsInfo)params).context,satId);
		((DVBSSettingsInfo)params).getSatelliteInfo().setSatlRecId(satId);
		((DVBSSettingsInfo)params).scanMode = info.dvbsScanMode;
		((DVBSSettingsInfo)params).scanChannels = info.dvbsScanType;
		((DVBSSettingsInfo)params).scanStoreType = info.dvbsStoreType;
		getDVBSData().currentSatName = info.getSatName();
		getDVBSData().orbPos = info.getOrbPos();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,String.format("%s,SatName:%s,SatID:%d", "startDvbsMultiScan()",info.getSatName(),info.getSatlRecId()));
		setRollback(false);//dvbs no need rollback flow
		if(getListener()!=null) {
		    getListener().onDVBSInfoUpdated(-1, "ChangeSatelliteFrequence");
		}
		mEuScanner = new DVBSScanner(this, params, callback);
//		mEuScanner.fullScan();
	}

	private void startEUFullScan(ScanParams params) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startEUFullScan()");
		int tuneMode = getTuneMode();
		getActionList().clear();
		mScanParams = null;
		boolean atvFirst = ScanContent.isATVScanFirst();
		if (tuneMode >= 2) {
			int scanMode=((DVBSSettingsInfo)params).scanMode;
			mDVBSData =new DvbsScanningData();
			if(scanMode==DVBSSettingsInfo.NETWORK_SCAN_MODE || scanMode==DVBSSettingsInfo.FULL_SCAN_MODE){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "((DVBSSettingsInfo)params).menuSelectedOP?" + ((DVBSSettingsInfo)params).menuSelectedOP
						+ ">>>" + ((DVBSSettingsInfo)params).mIsDvbsNeedCleanChannelDB);
				if (((DVBSSettingsInfo)params).mIsDvbsNeedCleanChannelDB) {
					TVContent.clearChannelDB(); // clearDVBS DB.
				} else {
					setRollback(false);	//not clear channel DB,not rollback.
				}
				if(((DVBSSettingsInfo)params).menuSelectedOP != -1){
					MtkTvConfig.getInstance().setConfigValue(ScanContent.SAT_BRDCSTER,((DVBSSettingsInfo)params).menuSelectedOP);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).getDVBSCurrentOP:"+ScanContent.getDVBSCurrentOP());
				}
				List<SatelliteInfo> list=ScanContent.getDVBSEnablesatellites(((DVBSSettingsInfo)params).context);
				mDVBSData.satelliteInfoList.clear();
				mDVBSData.satelliteInfoList.addAll(list);
				if(!list.isEmpty()){
					 if (DVBSScanner.isMDUScanMode()) {
						 if (((DVBSSettingsInfo)params).mIsOnlyScanOneSatellite) {
							 mDVBSData.satList=new int[1];
							 SatelliteInfo satelliteInfo = ((DVBSSettingsInfo)params).getSatelliteInfo();
							 mDVBSData.satList[0]=satelliteInfo.getSatlRecId();
							 mDVBSData.currentSatIndex=0;
							 mDVBSData.currentSatName=satelliteInfo.getSatName();
							 mDVBSData.orbPos = list.get(0).getOrbPos();
							 mDVBSData.totalSatSize=1;
							 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).isMDUScanMode add scan:" + mDVBSData.satList[0]);
						 } else {
							 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).isMDUScanMode:re scan or update scan");
							 mDVBSData.satList = new int[list.size()];
							 mDVBSData.currentSatIndex = 0;
							 mDVBSData.currentSatName = list.get(0).getSatName();
							 mDVBSData.orbPos = list.get(0).getOrbPos();
							 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mDVBSData.currentSatName-1:"+mDVBSData.currentSatName);
							 mDVBSData.totalSatSize = list.size();
							 for(int i=0;i<list.size();i++){
								mDVBSData.satList[i]=list.get(i).getSatlRecId();
								com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isMDUScanModemDVBSData.satID:"+i+","+list.get(i).getSatlRecId());
							 }
							 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).isMDUScanModerescan:" + mDVBSData.satList[0]);
						 }
					} else if (((DVBSSettingsInfo)params).mIsOnlyScanOneSatellite) {
						mDVBSData.satList=new int[1];
						SatelliteInfo satelliteInfo = ((DVBSSettingsInfo)params).getSatelliteInfo();
						mDVBSData.satList[0] = satelliteInfo.getSatlRecId();
						mDVBSData.currentSatIndex = 0;
						mDVBSData.currentSatName = satelliteInfo.getSatName();
						mDVBSData.orbPos = satelliteInfo.getOrbPos();
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).mIsOnlyScanOneSatellite:>>" + mDVBSData.satList[0]);
						mDVBSData.totalSatSize = 1;
					} else if (DVBSScanner.isM7ScanMode() || DVBSScanner.isDsmartScanMode() || DVBSScanner.isNCPlusScanMode()) {
						mDVBSData.satList=new int[1];
						SatelliteInfo satelliteInfo = ((DVBSSettingsInfo)params).getSatelliteInfo();
						mDVBSData.satList[0]=satelliteInfo.getSatlRecId();
						mDVBSData.currentSatIndex=0;
						mDVBSData.orbPos = satelliteInfo.getOrbPos();
//						mDVBSData.currentSatName=satelliteInfo.getSatName();
						mDVBSData.totalSatSize=1;
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).isM7ScanMode:" + mDVBSData.satList[0]);
					} else {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"((DVBSSettingsInfo)params).commonScanMode:");
						mDVBSData.satList=new int[list.size()];
						mDVBSData.currentSatIndex = 0;
						mDVBSData.currentSatName = list.get(0).getSatName();
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mDVBSData.currentSatName-1:"+mDVBSData.currentSatName);
						mDVBSData.totalSatSize = list.size();
						mDVBSData.orbPos = list.get(0).getOrbPos();
						for(int i=0;i<list.size();i++){
							mDVBSData.satList[i]=list.get(i).getSatlRecId();
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mDVBSData.satID:"+i+","+list.get(i).getSatlRecId());
						}
					}
					((DVBSSettingsInfo)params).getSatelliteInfo().setSatlRecId(mDVBSData.satList[0]);
					mEuScanner = new DVBSScanner(this, params, callback);
				}
			}else if(scanMode==DVBSSettingsInfo.TP_SCAN_MODE){
				setRollback(false);	//not clear channel DB,not rollback.
				mDVBSData.satList = new int[1];
				mDVBSData.satList[0]=((DVBSSettingsInfo)params).getSatelliteInfo().getSatlRecId();
				mDVBSData.currentSatIndex=0;
				mDVBSData.totalSatSize=1;
				SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(((DVBSSettingsInfo)params).context,mDVBSData.satList[0]);
				mDVBSData.satelliteInfoList.clear();
				mDVBSData.satelliteInfoList.add(info);
				mDVBSData.currentSatName = info.getSatName();
				mDVBSData.orbPos = info.getOrbPos();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mDVBSData.currentSatName-2:" + mDVBSData.currentSatName + ">>>" + mDVBSData.satList[0]);
				mEuScanner = new DVBSScanner(this,params, callback);
			}

			mDVBSData.isUpdateScan=((DVBSSettingsInfo)params).isUpdateScan;
			mDVBSData.params=params;
			if(getListener() != null) {
			    getListener().onDVBSInfoUpdated(-1, "ChangeSatelliteFrequence");
			}
//			mEuScanner.fullScan();
		} else {
			TVContent.clearChannelDB(); // clear DVBT||DVBC||DVBS DB.
			if (atvFirst) {
		        getActionList().add(Action.DTV);
		        mScanParams = params;
		      } else {
				getActionList().add(Action.ATV);
			}

			getActionList().totalScanActionSize = 2;

			if (atvFirst) {
		        if (mTV != null && mTV.mHandler != null) {
		            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start ATV scan delay MessageType.MESSAGE_START_SCAN");
		            mTV.mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_START_SCAN, 800);
		          } else {
		              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start ATV scan not delay!!!");
		             startATVAutoScan();
		          }
		        } else {
				startEUDTVScan(params);
			}
		}
	}

  public void startDvbsScanAfterTsLock() {
    if(mEuScanner!=null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startDvbsScanAfterTsLock()");
        mEuScanner.fullScan();
    }
  }
  
  public void prepareSdxScan(Context context) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareSdxScan");
      //setRollbackCleanChannel();
      clearChannelNum();
      List<SatelliteInfo> list=ScanContent.getDVBSEnablesatellites(context);
      if(list.isEmpty()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareSdxScan no Satellite!");
          return;
      }
      mDVBSData.satList = new int[list.size()];
      mDVBSData.currentSatIndex = 0;
      mDVBSData.currentSatName = list.get(0).getSatName();
      mDVBSData.allOrbPos = new int[list.size()];
      mDVBSData.allSatNames = new String[list.size()];
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mDVBSData.currentSatName-sdx:"+mDVBSData.currentSatName);
      mDVBSData.totalSatSize = list.size();
      for(int i=0;i<list.size();i++){
          mDVBSData.satList[i]=list.get(i).getSatlRecId();
          mDVBSData.allOrbPos[i]=list.get(i).getOrbPos();
          mDVBSData.allSatNames[i]=list.get(i).getName();
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isMDUScanModemDVBSData.satID:"+list.get(i).getSatlRecId()+",orbit:"+list.get(i).getOrbPos());
      }
  }
  
  /*public void rollbackChannelsForSdx() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsForSdx");
      int svl = CommonIntegration.getInstance().getSvl();
      if (isRollback()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsForSdx(),isRollback!!");
          if (!hasChannels()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsForSdx(),hasnoChannels!!");
              TVContent.restoreChanneListSnapshot();
              TVContent.restoreDVBSOP();
          }
      } 
      TVContent.releaseChanneListSnapshot();
      TVContent.freeBackupDVBSOP();
  }*/
  
  public int[] getDVBSallOrbPos() {
      return mDVBSData.allOrbPos;
  }
  
  public int[] getDVBSsatIds(){
      return mDVBSData.satList;
  }
  
  public String[] getDVBSallSatNames(){
      return mDVBSData.allSatNames;
  }
  
  public void setDvbsCurSatIndex(int index) {
      mDVBSData.currentSatIndex = index;
  }
  
  public void setDvbsCurSatName(String name) {
      mDVBSData.currentSatName = name;
  }

	 public void startATVAutoScan() {
	     // TODO Auto-generated method stub
	     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startATVAutoScan()");
	     if (isEU()) {
	         getActionList().remove(Action.ATV); // true? false? whatever. Just
	         if(TVContent.getInstance(mContext).isCOLRegion()){
	             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"COL country start NTSC FULL scan");
	             mScan.startScan(ScanType.SCAN_TYPE_NTSC, ScanMode.SCAN_MODE_FULL, true);
	             return;
	         }
	         mEuScanner = new EUATVScanner(this);
	         mEuScanner.fullATVScan();
	     } else if (isCN()) {
               getActionList().remove(Action.ATV); // true? false? whatever. Just
	       mEuScanner = new EUATVScanner(this);
	       mEuScanner.fullATVScan();
	     }
	   }

	private void startEUDTVScan(ScanParams params) {
	    if(TVContent.getInstance(mContext).isHKRegion()) {
	        startCNDTVScan(params);
	        return;
	    }
		int tuneMode = getTuneMode();

		switch (tuneMode) {
		case MtkTvConfigTypeBase.BS_SRC_AIR: // antenna
			mEuScanner = new DVBTScanner();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startEUDTVScan mIsBroadcastScan="+mIsBroadcastScan+", mIsAdviseScan="+mIsAdviseScan);
			if(!mIsBroadcastScan && TVContent.getInstance(mContext).isSupportFvpScan()){
				if(mIsAdviseScan) {
					((DVBTScanner) mEuScanner).fullScan(true, true);
				}else {
					((DVBTScanner) mEuScanner).fullScan(true);
				}
			}else {
			    mEuScanner.fullScan();
            }
			mIsBroadcastScan = false;
			//
			break;
		case MtkTvConfigTypeBase.BS_SRC_CABLE: // cable
			mEuScanner = new DVBCScanner(params);
			mEuScanner.fullScan();
			break;
		// case MtkTvConfigTypeBase.BS_SRC_SATELLITE: //satellite
		// break;
		default:
			break;
		}
	}

	private void startCNDTVScan(ScanParams params) {

		int tuneMode = getTuneMode();

		switch (tuneMode) {
		case MtkTvConfigTypeBase.BS_SRC_AIR: // antenna
			mEuScanner = DVBTCNScanner.getScanInstance(this);
			mEuScanner.fullScan();
			break;
		case MtkTvConfigTypeBase.BS_SRC_CABLE: // cable
			mEuScanner = DVBCCNScanner.getScanInstance(null);
			if (params == null) {
				params = new ScanParams();
				params.dvbcScanMode = ScanParams.DvbcScanMode.FULL;
			}
			((DVBCCNScanner)mEuScanner).setScanParams(params);
			mEuScanner.fullScan();
			break;
		default:
			break;
		}
	}
	boolean atvFirst = ScanContent.isATVScanFirst();
	/**
	 * 1.check ATV first or DTV first.
	 * <P>
	 * 2.Do ATV update Scan or DTV update scan.
	 *
	 * @param params
	 */
	private void startEUUpdateScan(ScanParams params) {
		// TODO Auto-generated method stub
	    atvFirst = ScanContent.isATVScanFirst();
		getActionList().clear();
		mScanParams = null;

		if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
			boolean isATVSource = TVContent.getInstance(mContext).isCurrentSourceATV();
			if (isATVSource) {
				startATVUpdateScan();
			} else {
				startEuDTVUpdateScan(params);
			}
		}else{

		if (atvFirst) {
			getActionList().add(Action.DTV_UPDATE);
		} else {
			getActionList().add(Action.ATV_UPDATE);
		}

		getActionList().totalScanActionSize = 2;

		if (atvFirst) {
			startATVUpdateScan();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start ATV UpdateScan");
			atvFirst=false;
		} else {
			startEuDTVUpdateScan(params);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start DTV UpdateScan");
			atvFirst=true;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"atvFirst:"+atvFirst);
	}
	}

	/**
	 * Only do DTV Update Scan
	 *
	 * @param params
	 */
	private void startEuDTVUpdateScan(ScanParams params) {

		int tuneMode = getTuneMode();

		switch (tuneMode) {
		case MtkTvConfigTypeBase.BS_SRC_AIR: // antenna
			mEuScanner = new DVBTScanner();
			mEuScanner.updateScan();
			break;
		case MtkTvConfigTypeBase.BS_SRC_CABLE: // cable
			mEuScanner = new DVBCScanner(params);
			mEuScanner.updateScan();
			break;
		// case MtkTvConfigTypeBase.BS_SRC_SATELLITE: //satellite
		// break;
		default:
			break;
		}
	}

	private void startCNSingleRFScan(ScanParams params) {
		int tuneMode = getTuneMode();
		switch (tuneMode) {
		case MtkTvConfigTypeBase.BS_SRC_AIR: // antenna
			mEuScanner = DVBTCNScanner.getScanInstance();
			if (callback != null) {//reset last callback msg since new scan msg maybe same with last scan msg
				callback.mLastMsg = null;
			}
			mEuScanner.singleRFScan();
			break;
		case MtkTvConfigTypeBase.BS_SRC_CABLE: // cable
			mEuScanner = DVBCCNScanner.getScanInstance(null);
			((DVBCCNScanner)mEuScanner).setScanParams(params);
			if (callback != null) {//reset last callback msg since new scan msg maybe same with last scan msg
				callback.mLastMsg = null;
			}
			mEuScanner.singleRFScan();
			break;
		default:
			break;
		}
	}

	private void startEUSingleRFScan(ScanParams params) {
		// TODO Auto-generated method stub
		int tuneMode = getTuneMode();
		switch (tuneMode) {
		case MtkTvConfigTypeBase.BS_SRC_AIR: // antenna
			mEuScanner = new DVBTScanner();
			if (callback != null) {//reset last callback msg since new scan msg maybe same with last scan msg
				callback.mLastMsg = null;
			}
			mEuScanner.singleRFScan();
			//
			break;
		case MtkTvConfigTypeBase.BS_SRC_CABLE: // cable
			mEuScanner = new DVBCScanner(params);
			mEuScanner.singleRFScan();
			break;
		// case MtkTvConfigTypeBase.BS_SRC_SATELLITE: //satellite
		// break;
		default:
			break;
		}
	}

	public void onMduDetectNfy(){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onMdu_detect_nfy()");
		if(getTuneMode() >= CommonIntegration.DB_SAT_OPTID){	//avoid error msg.
			MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
			base.dvbsGetNfyMduDetect();
			int mduType=Arrays.asList(MtkTvScanDvbsBase.TunerMduType.values()).indexOf(base.nfyMduDetect_mduType);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onMdu_detect_nfy():" + mduType);
			mduType=Math.max(mduType, 0);
			((DVBSSettingsInfo)mDVBSData.params).mduType = mduType;
			SatelliteInfo info=ScanContent.getDVBSsatellitesBySatID(((DVBSSettingsInfo)mDVBSData.params).context, ((DVBSSettingsInfo)mDVBSData.params).getSatelliteInfo().getSatlRecId());
			info.setMduType(mduType);
			ScanContent.saveDVBSSatelliteToSatl(info);
		}
	}

	/**
	 * this scan is for mdu scan after check mduType
	 */
	public void startSecondMduScan() {
		mEuScanner = new DVBSScanner(this, mDVBSData.params, callback);
		mEuScanner.fullScan();
	}

	/**
	 * tuner is 0:air, 1:cable, 2:prefer sat or general sat
	 * @return
	 */
	public int getTuneMode() {
		int tuneMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SRC);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tuneMode?" + tuneMode);
		return tuneMode;
	}

	public void cancelScan() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				if (isEU() || isCN()) {
					if (getScanType() == ATV_SCAN || getScanType() == UPDATE_ATV_SCAN) {
						if (TVContent.getInstance(mContext).isCOLRegion()) {
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan  ATV_SCAN");
							mScan.cancelScan();
						} else {
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PalSecam cancelScan  ATV_SCAN");
							mScan.getScanPalSecamInstance().cancelScan();
						}
					} else if (mTV.isM7ScanMode()) {
						cancleDvbsM7LNBSearch();
						cancleDvbsM7ChannelScan();
					} else {
						if (mEuScanner != null) {
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan 222 ATV_SCAN");
							mEuScanner.cancelScan();
						}
					}
				} else {
					if (mScan != null) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan  3333ATV_SCAN");
						mScan.cancelScan();
					}
				}
			}
		}.start();
	}

	public boolean isEU() {
		return region == Region.EU;
	}

	public boolean isSA() {
		return region == Region.SA;
	}

	public boolean isCN() {
		return region == Region.CN;
	}

	private void clearScanStates() {
		setRollback(false);
		scanType = -1;
	}

	private void initRegion() {
		if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US) {
			region = Region.US;
		} else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
			region = Region.EU;
		} else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
			region = Region.SA;
		} else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_CN) {
			region = Region.CN;
		} else {
			region = Region.CN;
		}
	}

	private int getScanType() {
		return scanType;
	}

	public void resumeTV() {
		startTVBeforeScanATVOnPIPMode();
	}

	public boolean isDVBTSingleRFScan() {
		return scanType == SIGNAL_RF_SCAN && getTuneMode() == MtkTvConfigTypeBase.BS_SRC_AIR;
	}

	public boolean isSingleRFScan() {
		return scanType == SIGNAL_RF_SCAN;
	}

	public boolean needStartTVAfterATVScanUpDown() {
		return scanType == ATV_SCAN_UP || scanType == ATV_SCAN_DOWN
				&& !hasChannels();
	}

	public boolean isATVScan() {
		return scanType == ATV_SCAN;
	}

	public ActionList<Action> getActionList() {
		return actionList;
	}

	public void setActionList(ActionList<Action> actionList) {
		this.actionList = actionList;
	}

	public void clearActionList() {
		if (actionList != null) {
			actionList.clear();
		}
	}

	public ScanParams getstoredParams(){
		return mScanParams;
	}

	public boolean hasOPToDo() {
		return callback.isHasOpTODO() || getOPType() != -1;
	}

	public void setOPType(int type) {
		callback.setOpType(type);
	}

	public int getOPType() {
		return callback.getOpType();
	}

	private void clearCallbackParams() {
		callback.setHasOpTODO(false);
		callback.setOpType(-1);
	}

	public void uiOpEnd() {
		// MtkTvScan.getInstance().getScanDvbtInstance().uiOpEnd();
		clearCallbackParams();
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		callback.removeListener();
	}

	public void onScanError() {
		if (mScanListener != null) {
			this.mScanListener.onCompleted(ScannerListener.COMPLETE_ERROR);
		}
	}

	public void onScanOK() {
        if (mScanListener != null) {
            this.mScanListener.onCompleted(ScannerListener.COMPLETE_OK);
        }
    }

	public void reMapRegions(List<TargetRegion> regions) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"reMapRegions()");
		List<TargetRegion> targetRegionList = new ArrayList<TargetRegion>();
		targetRegionList.addAll(regions);

		// add Level1 Node.
		for (int i = targetRegionList.size() - 1; i >= 0; i--) {
		    TargetRegion targetRegion = targetRegionList.get(i);
			switch (targetRegion.level) {
			    case LEVEL_1:
			        getRegionMgr().addChild(targetRegion);
			        targetRegionList.remove(i);
			        break;
			    default:
			        break;
			}
		}

		// add Level2 Node.
		for (int i = targetRegionList.size() - 1; i >= 0; i--) {
		    TargetRegion targetRegion = targetRegionList.get(i);
			switch (targetRegion.level) {
			    case LEVEL_2:
			        getRegionMgr().addChild(targetRegion);
			        targetRegionList.remove(i);
			        break;
			    default:
			        break;
			}
		}

		// add Level3 Node.
		for (int i = targetRegionList.size() - 1; i >= 0; i--) {
		    TargetRegion targetRegion = targetRegionList.get(i);
			switch (targetRegion.level) {
			    case LEVEL_3:
			        getRegionMgr().addChild(targetRegion);
			        break;
			    default:
			        break;
			}
		}
		getRegionMgr().dumpRM();
	}

	public Map<Integer, APTargetRegion> getRegionsOfGBR() {
		return getRegionMgr().getChildren();
	}

	public RegionUtils getRegionMgr() {
		return mRegionMgr;
	}

	public void setRegionMgr(RegionUtils mRegionMgr) {
		this.mRegionMgr = mRegionMgr;
	}

    /**
     * show tkgs DVBS user message dialog
     */
    protected static void showDvbsTKGSUserMsgDialogInternal(String umsg) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBS_TKGS_UserMsgDialog()-umsg");
        boolean isTVSource = CommonIntegration.getInstance().isCurrentSourceTv();

        if (!isTVSource) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBS_TKGS_UserMsgDialog(),Not TV Source");
            return;
        }

        if (ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
                .isVisible()) {

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBS_TKGS_UserMsgDialog(),PVR is Running");
            return;
        }

        if (TurnkeyUiMainActivity.getInstance() != null) {
            if (!TurnkeyUiMainActivity.getInstance().isResumed()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBS_TKGS_UserMsgDialog(),Nav Not Resumed");
                return;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTKGSUserMessageDialog>>>");
            TKGSUserMessageDialog dialog = new TKGSUserMessageDialog(
                    TurnkeyUiMainActivity.getInstance());
            dialog.showConfirmDialog(umsg);

        }
    }

    public static void showDvbsTKGSUserMsgDialog(final String umsg) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBS_TKGS_UserMsgDialog()-umsg==" + umsg);

         if (TurnkeyUiMainActivity.getInstance() != null) {
           TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {

             @Override
             public void run() {
               ScannerManager.showDvbsTKGSUserMsgDialogInternal(umsg);
             }
           });
         }
       }
    
    public static void showDvbsFSTVersionChangeDialog() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDvbsFSTVersionChangeDialog()");
        
        if (TurnkeyUiMainActivity.getInstance() != null) {
            TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDvbsFSTVersionChangeDialog()");
                    boolean isTVSource = CommonIntegration.getInstance().isCurrentSourceTv();
                    
                    if (!isTVSource) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDvbsFSTVersionChangeDialog(),Not TV Source");
                        return;
                    }
                    
                    if (ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
                            .isVisible()) {
                        
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDvbsFSTVersionChangeDialog(),PVR is Running");
                        return;
                    }
                    
                    if (TurnkeyUiMainActivity.getInstance() != null) {
                        if (!TurnkeyUiMainActivity.getInstance().isResumed()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDvbsFSTVersionChangeDialog(),Nav Not Resumed");
                            return;
                        }
                        
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDvbsFSTVersionChangeDialog>>>");
                        showFstVersionChgDialog(TurnkeyUiMainActivity.getInstance());
                    }
                }
            });
        }
    }
    
    private static void showFstVersionChgDialog(Context context) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFstVersionChgDialog");
        final SimpleDialog dialog = new SimpleDialog(context);
        dialog.setCancelText(android.R.string.no);
        dialog.setConfirmText(android.R.string.ok);
        dialog.setContent(context.getString(R.string.dvbs_fast_version_change_msg));
        dialog.setOnCancelClickListener(new OnCancelClickListener() {
            @Override
            public void onCancelClick(int dialogId) {
                new MtkTvScanDvbsBase().dvbsM7DisableVerChgNfy();
                dialog.dismiss();
            }
        }, 1000);
        dialog.setOnConfirmClickListener(new OnConfirmClickListener() {
            
            @Override
            public void onConfirmClick(int dialogId) {
                MtkTvScanDvbsBase dvbsBase = new MtkTvScanDvbsBase();
                dvbsBase.dvbsM7GetOptList();
                MtkTvScanDvbsBase.M7OneOPT[] opList = dvbsBase.M7_OptLists;
                String subOptName = SaveValue.readWorldStringValue(context, "FAST_SCAN_SELECTED_OPT");
                if(opList == null || opList.length == 0 || "".equals(subOptName)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFstVersionChgDialog update scan fail opList="+opList+",subOptName="+subOptName);
                    return;
                }
                MtkTvScanDvbsBase.M7OneOPT selectedOpt = null;
                for(MtkTvScanDvbsBase.M7OneOPT one : opList){
                    if(one.subOptName.equals(subOptName)){
                        selectedOpt = one;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFstVersionChgDialog selected opt="+selectedOpt);
                if(selectedOpt == null) {
                    return;
                }
                int satid = ScanContent.getDVBSsatellites(context).get(0) == null ?
						0 : ScanContent.getDVBSsatellites(context).get(0).getSatlRecId();
                Intent intent = new Intent(context,ScanViewActivity.class);
                intent.putExtra("ActionID", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
                intent.putExtra("SatID", satid);
                intent.putExtra("LocationID", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
                intent.putExtra("SpclRegnSetup", selectedOpt.spclRegnSetup);
                intent.putExtra("SelectedOpName", selectedOpt.subOptName);
				intent.putExtra("isFromFstVersionChgDialog", true);
                context.startActivity(intent);
            }
        }, 1000);
        dialog.show();
    }
	/**
	 * means "Full scan "
	 *
	 * @return
	 */
	public boolean isRollback() {
		return rollback;
	}

	public void setRollback(boolean rollback) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRollback>>>" + rollback);
		this.rollback = rollback;
	}

	public void rollbackChannelsWhenScanNothingOnUIThread(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsWhenScanNothingOnUIThread()");
       /* if (TurnkeyUiMainActivity.getInstance() != null) {
            TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {*/
                    // TODO Auto-generated method stub
                    rollbackChannelsWhenScanNothing();
                /*}
            });
        }*/

    }

	public void saveBGMDataInWizard(int svl) {
		if (isRollback()) {
			if (!hasChannels()) {
			    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing ");
			} else {
				DVBSScanner.forceSaveBGMData(this, mDVBSData,svl);
			}
		}
	}

	public void rollbackChannelsWhenScanNothing() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsWhenScanNothing()");
		int svl = CommonIntegration.getInstance().getSvl();
		int tunerMode = getTuneMode();
		if (isRollback()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsWhenScanNothing(),isRollback!!");
			if (!hasChannels()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsWhenScanNothing(),hasnoChannels!!");
				TVContent.restoreChanneListSnapshot();
				TVContent.restoreDVBSOP();
//				if(svl>2){//maybe user start second time scan, so can not restore DVBS info in this
//					TVContent.restoreDVBSsatellites();
//				}
			} else {
				if(tunerMode >= CommonIntegration.DB_SAT_OPTID){
					DVBSScanner.forceSaveBGMData(this, mDVBSData,svl);
				}
			}
		} else {
			if (hasChannels()) {
				if(tunerMode >= CommonIntegration.DB_SAT_OPTID){
					DVBSScanner.forceSaveBGMData(this, mDVBSData,svl);
				}
			}
		}
		TVContent.releaseChanneListSnapshot();
		TVContent.freeBackupDVBSOP();
		if (!isRollback() || hasChannels()) {// if no need to roolback or has already scan channels, no need to restore and free backup info
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsWhenScanNothing after scan, free DVBS info");
			TVContent.freeBachUpDVBSsatellites();
			/*if (tunerMode >= CommonIntegration.DB_SAT_OPTID
					&& mDVBSData !=null && mDVBSData.params != null
					&& mDVBSData.params instanceof DVBSSettingsInfo) {//free TP info when needed
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rollbackChannelsWhenScanNothing free TP info");
				if (((DVBSSettingsInfo)mDVBSData.params).mRescanSatLocalInfoList != null) {
					((DVBSSettingsInfo)mDVBSData.params).mRescanSatLocalInfoList.clear();
				}
				if (((DVBSSettingsInfo)mDVBSData.params).mRescanSatLocalTPInfoList != null) {
					((DVBSSettingsInfo)mDVBSData.params).mRescanSatLocalTPInfoList.clear();
				}
			}*/
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"rollbackChannelsWhenScanNothing after scan, has no free DVBS info");
		}
//		//if DVBS updateScan,show the changes of channels to User.
//		if(tunerMode >= CommonIntegration.DB_SAT_OPTID){
//			this.getListener().onDVBSInfoUpdated(ScannerListener.SHOW_CHANNEL_CHANGES_NUM, "");
//		}

	}

	int mAnalogCHs = 0;
	int mDigitalCHs = 0;
	int mIpchannels = 0;

	/**
	 * Channel length equal 0 by TVAPI after CancelScan or ScanComplete
	 * immediately.
	 *
	 * @return
	 */
	public boolean hasChannels() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasChannels(),mAnalogCHs:"+mAnalogCHs);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasChannels(),mDigitalCHs:"+mDigitalCHs+",mIpchannels:"+mIpchannels);
		return mAnalogCHs != 0 || mDigitalCHs != 0 || mIpchannels != 0;
	}

	public boolean hasDigitalChannels() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasChannels(),mDigitalCHs:" + mDigitalCHs);
		return mDigitalCHs != 0;
	}

	/**
	 * need clean scan channel number before start scan
	 */
	private void clearChannelNum() {
		mAnalogCHs = 0;
		mDigitalCHs = 0;
		mIpchannels = 0;
	}

	public void cleanDvbsNum() {
		mDVBSData.channelNum.clear();
	}

	/**
	 * For DVBS only. after first scan,will show bat list,let user select.
	 *
	 * @return
	 */
	public boolean isBATCountry() {
		return MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FRA);
	}

	public String getFirstSatName() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFirstSatName()");
		if(getDVBSData()==null){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFirstSatName():"+"getDVBSData()==null");
			return "";
		}
		return getDVBSData().currentSatName;
	}

	public String getScanningSatName() {
		//no need change sat mame for new dvbs UI
	    if(true/*!ScanContent.isPreferedSat()*/){// NOPMD
	        return null;
	    }
		MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
		dvbsScan.dvbsGetNfySatName();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"original orbPos="+mDVBSData.orbPos+",now orbPos="+dvbsScan.nfySatName_orbPos);
		if(mDVBSData.orbPos == dvbsScan.nfySatName_orbPos){
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"orbPos is same,return");
		    return null;
		}
		if(dvbsScan.nfySatName_satName!=null&&dvbsScan.nfySatName_satName.equalsIgnoreCase("null")){
			mDVBSData.currentSatName=dvbsScan.nfySatName_satName;
		} else {
          // >0 East, <0 Wes
          int nfySatNameOrbPos = dvbsScan.nfySatName_orbPos;
          if(nfySatNameOrbPos > 0) {
            mDVBSData.currentSatName = convertSatNameByPos(nfySatNameOrbPos,"E");
          } else {
            mDVBSData.currentSatName = convertSatNameByPos(-nfySatNameOrbPos,"W");
          }
        }
		return mDVBSData.currentSatName;
	}

    private String convertSatNameByPos(int pos, String suf) {
      StringBuilder sb = new StringBuilder();
      sb.append(pos / 10);
      if(pos % 10 != 0) {
        sb.append(".").append(pos % 10);
      }
      return sb.append("\u00B0").append(suf).toString();
	}

	/**
	 * For Rollback channel.(check channel length)
	 *
	 * @param channels
	 * @param channelType
	 */
	public void setChannelsNum(int channels, int channelType) {
		switch (channelType) {
		case 0:
		case 1:
			mAnalogCHs = channels;
			break;
		case 2:
			mDigitalCHs = channels;
			break;
		default:
		    break;
		}
	}

	public void setIpChannels(int channels){
	    mIpchannels = channels;
	}

	public void setIsBroadCastScan(boolean isBroadcast){
	    mIsBroadcastScan = isBroadcast;
	}

	public void setmIsAdviseScan(boolean isAdviseScan){
		mIsAdviseScan = isAdviseScan;
	}

	public boolean showPTSaveChannelDialog() {
		return isEU() && ScanContent.isCountryPT()
				&& isRollback() && hasChannels();
	}

    /**
     * show confirm DVBT new auto scan dialog
     */
    public void showDvbtTHDConfirmDialogInternal() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBT_THD_ConfirmDialog()-TKUI");
        if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
            return;
        }
        if (CommonIntegration.isCNRegion()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBT_THD_ConfirmDialog(),is CN ,not support");
            return;
        }
        boolean isTVSource = CommonIntegration.getInstance().isCurrentSourceTv();

        if (!isTVSource) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBT_THD_ConfirmDialog(),Not TV Source");
            return;
        }

        if (ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT) != null &&
				ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
                .isVisible()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBT_THD_ConfirmDialog(),PVR is Running");
            return;
        }

		TwinkleDialog.hideTwinkle();

        if (TurnkeyUiMainActivity.getInstance() != null) {
            if (!TurnkeyUiMainActivity.getInstance().isResumed()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBT_THD_ConfirmDialog(),Nav Not Resumed");
                return;
            }

            ConfirmDialog mConfirmDialog = ConfirmDialog.getInstance(
                    TurnkeyUiMainActivity.getInstance());
            if(mConfirmDialog.isConfirmDialogShowing()){
            	mConfirmDialog.dismissConfirmDialog();
            }
			mConfirmDialog.showConfirmDialog();
		}
    }

	public void showDvbtTHDConfirmDialog() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ConfirmDialog()-ScannerManager");

		if (TurnkeyUiMainActivity.getInstance() != null) {
			TurnkeyUiMainActivity.getInstance().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					ScannerManager.this.showDvbtTHDConfirmDialogInternal();
				}
			});
		}
	}

	public void showDVBTFvpUserSelectDialog(Context context) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBTFvpUserSelectDialog() ScannerManager");

	    if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
	        return;
	    }
	    new ScanThirdlyDialog(context, 6).show();
	}
	public void showDVBTFvpScanErrorDialog(Context context) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBTFvpScanErrorDialog() ScannerManager");

	    if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
	        return;
	    }
	    if(mFvpScanErrorDialog == null){
	        mFvpScanErrorDialog = ConfirmDialog.getInstance(context);
	    }
	    mFvpScanErrorDialog.showDVBTFvpScanErrorDialog(context);
	}

	public int getDVBSCurrentIndex() {
		return mDVBSData.currentSatIndex;
	}

	public int getDVBSTotalSatSize() {
		return mDVBSData.getSatSize();
	}

	public ScanParams getDVBSDataParams() {
		return mDVBSData.params;
	}
	  public void setTkgsNfyInfoMask(int mask) {
	      callback.setTkgsNfyInfoMask(mask);
	    }

	  public int getTkgsNfyInfoMask() {
	      return callback.getTkgsNfyInfoMask();
	    }
	  public String getDVBSTkgsUserMessage() {
	        return mDVBSData.tkgsUserMessage;
	      }

	  public void setDVBSTkgsUserMessage(String msg) {
	        mDVBSData.tkgsUserMessage = msg;
	      }
	public int getDVBSScannedChannel() {
		int sumSize=0;
		Iterator<Integer> numKeySet=mDVBSData.channelNum.keySet().iterator();
		while(numKeySet.hasNext()){
			Integer key=numKeySet.next();
			sumSize+=mDVBSData.channelNum.get(key);
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getDVBSScannedChannel(),size:"+sumSize);
		return sumSize;
	}

	public void setDVBSScannedChannel(int index,int num) {
		mDVBSData.channelNum.put(index, num);
	}

	public DvbsScanningData getDVBSData() {
		return mDVBSData;
	}

	public void clearDVBSData() {
		if(getTuneMode() >= CommonIntegration.DB_SAT_OPTID){
			mDVBSData = new DvbsScanningData();
		}
	}

	public boolean isScanTaskFinish() {
		if(getTuneMode() >= CommonIntegration.DB_SAT_OPTID){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isScanTaskFinish getDVBSCurrentIndex>>>" + getDVBSCurrentIndex() + ">>>" + getDVBSTotalSatSize()
					+ ">>>" + getActionList().size());
			if((getDVBSCurrentIndex() + 1) >= getDVBSTotalSatSize()){
				return getActionList().size() <= 0;
					//clearDVBSData();
			}else
			{
				return false;
			}
		}else{
			return getActionList().size() <= 0 ? true : false;
		}
	}

	public boolean checkIfNeedShowRegionList() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"checkIfNeedShowRegionList()");
        MtkTvScanDvbtBase.TargetRegion[] regionList = MtkTvScan.getInstance()
                .getScanDvbtInstance().uiOpGetTargetRegion();
        if (regionList != null && regionList.length > 0) {
            reMapRegions(Arrays.asList(regionList));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"checkIfNeedShowRegionList return true");
            return true;
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"checkIfNeedShowRegionList return false");
            return false;
        }
    }

	public int getCurrentSalId() {
        if(getDVBSData() == null){
            return 0;
        }else {
            return getDVBSData().satList[getDVBSCurrentIndex()];
        }
    }
	
	boolean isFastScanSecondScan = false;
	public void setFastScanSecondScan(boolean isSecondScan){
	    isFastScanSecondScan = isSecondScan;
	}
	public boolean isFastScanSecondScan(){
        return isFastScanSecondScan;
    }

}

class DvbsScanningData{
	public int totalSatSize=0;
	public int currentSatIndex=0;
	public String currentSatName="";
	public int[] satList;
	public ScanParams params;
	public int scannedChannel=0;
	public boolean isUpdateScan;
	public String tkgsUserMessage;
	public int orbPos;
	public int[] allOrbPos;
	public String[] allSatNames;
	public List<SatelliteInfo> satelliteInfoList = new ArrayList<SatelliteInfo>();

	public Map<Integer,Integer> channelNum= new HashMap<Integer,Integer>();

	public int getSatSize(){
		if(satList==null){
			return 0;
		}
		return satList.length;
	}
}