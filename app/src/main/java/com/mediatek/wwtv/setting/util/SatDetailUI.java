package com.mediatek.wwtv.setting.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.wwtv.setting.base.scan.model.DVBSScanner;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvScanTpInfo;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.TunerPolarizationType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter.SatItem;
import com.mediatek.wwtv.setting.base.scan.model.DVBSSettingsInfo;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.PartnerSettingsConfig;
import com.mediatek.wwtv.tvcenter.util.SatelliteConfigEntry;
import com.mediatek.wwtv.tvcenter.util.SatelliteProviderManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.OptionValuseChangedCallBack;
import com.mediatek.wwtv.setting.widget.detailui.Action.BeforeValueChangeCallback;

/**
 * for deal statellite's detail's UI
 * @author sin_biaoqinggao
 *
 */
public class SatDetailUI implements SpecialOptionDealer{

	static SatDetailUI mSelf ;
	public boolean mDvbsNeedTunerReset;
    public boolean mDvbsLNBSearched;

    public SatDetailUI(Context mContext){
		this.mContext = mContext ;
		ScanContent.getInstance(mContext);
	}

	public static synchronized SatDetailUI getInstance(Context mContext){
		if(mSelf == null){
			mSelf = new SatDetailUI(mContext);
		}

		return mSelf ;
	}

	public static String TAG = "SatDetailUI";
	Context mContext ;
	public final static String SAT_BRDCSTER = MtkTvConfigTypeBase.CFG_BS_BS_SAT_BRDCSTER; // DVBS OP
	public static final String SAT_ANTENNA_TYPE = MtkTvConfigTypeBase.CFG_BS_BS_SAT_ANTENNA_TYPE;
	public static int LNB_CONFIG[][];
	private static final int LNB_SINGLE_FREQ = 1;
	private static final int LNB_DUAL_FREQ = 2;

	private static void initLNBConfig() {
		LNB_CONFIG = new int[12][3];
		LNB_CONFIG[0] = new int[]{ 9750,  10600, 11700};
		LNB_CONFIG[1] = new int[]{ 9750,  10700, 11700};
		LNB_CONFIG[2] = new int[]{ 9750,  10750, 11700};
		LNB_CONFIG[3] = new int[]{ 5150,  0, 0};
		LNB_CONFIG[4] = new int[]{ 5750,  0, 0};
		LNB_CONFIG[5] = new int[]{ 9750,  0, 0};
		LNB_CONFIG[6] = new int[]{ 10600,  0, 0};
		LNB_CONFIG[7] = new int[]{ 10750,  0, 0};
		LNB_CONFIG[8] = new int[]{ 11250,  0, 0};
		LNB_CONFIG[9] = new int[]{ 11300,  0, 0};
		LNB_CONFIG[10] = new int[]{ 11475,  0, 0};
		LNB_CONFIG[11] = new int[]{ 0,  0, 0};//for user define
	}


	OptionValuseChangedCallBack channgeCallBack = new OptionValuseChangedCallBack(){

		@Override
		public void afterOptionValseChanged(String afterName) {
			Log.d(TAG, "afterOptionValseChanged");
		}

	};

    private SatelliteInfo updateValue(SatelliteInfo info, Action parentAction) {
        if (parentAction != null && parentAction.mSubChildGroup != null) {
            int antennaType = MtkTvConfig.getInstance().getConfigValue(
                    SAT_ANTENNA_TYPE);
            boolean isUniCable = (antennaType == MenuConfigManager.DVBS_ACFG_UNICABLE1 || antennaType == MenuConfigManager.DVBS_ACFG_UNICABLE2);
            
            int lnbFreq = 0;
            if (isUniCable) {
                lnbFreq = parentAction.mSubChildGroup.get(0).mInitValue;
                int position = parentAction.mSubChildGroup.get(1).mInitValue;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set position=" + (position + 1));
                info.setPosition(position + 1);
            } else if(antennaType >= MenuConfigManager.DVBS_ACFG_SINGLE && antennaType <= MenuConfigManager.DVBS_ACFG_DISEQC12){
                int lnbPower = parentAction.mSubChildGroup.get(0).mInitValue;// position from 1,lnb from 0
                lnbFreq = parentAction.mSubChildGroup.get(1).mInitValue;

                int tone22 = parentAction.mSubChildGroup.get(2).mInitValue;
                
                if(antennaType == MenuConfigManager.DVBS_ACFG_TONEBURST){
                    int toneburst = parentAction.mSubChildGroup.get(3).mInitValue;
                    if (toneburst == 2) {
                        toneburst = 255;
                    }
                    info.setToneBurst(toneburst);
                    Log.d(TAG, "status="+info.getEnable()+",lnbPower=" + lnbPower
                            + ",lnbFreq=" + lnbFreq + ",tone22=" + tone22
                            + ",toneburst=" + toneburst);
                }else{
                    int port = parentAction.mSubChildGroup.get(3).mInitValue;
                    int diseqcInputIntType = 0;

                    Log.d(TAG, "status="+info.getEnable()+",lnbPower=" + lnbPower
                            + ",lnbFreq=" + lnbFreq + ",tone22=" + tone22);
                    if(antennaType == MenuConfigManager.DVBS_ACFG_DISEQC10){
                        if (port == 0) {// disable
                            diseqcInputIntType = 0;
                            port = 255;
                        } else {
                            diseqcInputIntType = 2; // is 4x1
                            port = port - 1;
                        }
                        info.setDiseqcType(diseqcInputIntType);
                        info.setPort(port);
                        Log.d(TAG,"set diseqc10 port="+port+",setDiseqcTypeEx="+diseqcInputIntType);
                    }else if(antennaType == MenuConfigManager.DVBS_ACFG_DISEQC11){
                        if (port == 0) {// disable
                            diseqcInputIntType = 0;
                            port = 255;
                        } else {
                            diseqcInputIntType = 4;// 2 change to 4 For DTV01729720
                            port = port - 1;
                        }
                        info.setDiseqcTypeEx(diseqcInputIntType);
                        info.setPortEx(port);
                        Log.d(TAG,"set diseqc11 port="+port+",setDiseqcTypeEx="+diseqcInputIntType);
                    }else if(antennaType == MenuConfigManager.DVBS_ACFG_DISEQC12){
                        if (port == 0) {
                            diseqcInputIntType = 0;
                            port = 255;
                        } else {
                            diseqcInputIntType = 5;
                            port = 0;
                        }

                        Log.d(TAG, "setMotorType:" + diseqcInputIntType);
                        info.setMotorType(diseqcInputIntType);
                    }
                }
                info.setLnbPower(lnbPower);
                info.setM22k(tone22);
			}
			if (lnbFreq < LNB_CONFIG.length) {
				info.setLnbLowFreq(LNB_CONFIG[lnbFreq][0]);
				info.setLnbHighFreq(LNB_CONFIG[lnbFreq][1]);
				info.setLnbSwitchFreq(LNB_CONFIG[lnbFreq][2]);
				if (LNB_CONFIG[lnbFreq][1] == 0) {
					info.setLnbType(LNB_SINGLE_FREQ);
				} else {
					info.setLnbType(LNB_DUAL_FREQ);
				}
			}
		}
        if(mDvbsLNBSearched){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mDvbsLNBSearched, need clear detected mask");
            DVBSScanner.initLNBDetectedMast(mContext);
        }
		return info;
	}
	
	public void updateSatelliteNameAndOrbit(String newName, int orbit) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateOnlySatelliteName,mRecID=" + mRecID + ",new Name=" + newName+",orbit="+orbit);
        SatelliteInfo satInfo =
            new SatelliteInfo(ScanContent.getDVBSsatellitesBySatID(mContext, mRecID));
        satInfo.setSatName(newName);
        satInfo.setOrbPos(orbit);
        satInfo.setEnabled(true);
        ScanContent.saveDVBSSatelliteToSatl(satInfo, false);
      }
	
	public void updateOnlySatelliteName(String newName) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateOnlySatelliteName,mRecID=" + mRecID + ",new Name=" + newName);
	    SatelliteInfo satInfo =
	        new SatelliteInfo(ScanContent.getDVBSsatellitesBySatID(mContext, mRecID));
	    satInfo.setSatName(newName);
	    satInfo.setEnabled(true);
	    ScanContent.saveDVBSSatelliteToSatl(satInfo,false);
	  }
	
	public void updateOnlySatelliteName(String newName ,int salId) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateOnlySatelliteName,salId=" + salId + ",new Name=" + newName);
        SatelliteInfo satInfo =
            ScanContent.getDVBSsatellitesBySatID(mContext, salId);
        if(satInfo == null) {
            return;
        }
        satInfo.setSatName(newName);
        satInfo.setEnabled(true);
        ScanContent.saveDVBSSatelliteToSatl(satInfo, false);
      }
	/**
	 * prepare DVBS Scan parameter diseqc1.2.
	 */
	public SatelliteInfo updateDVBSSatInfoDiseqc12(SatelliteInfo satInfo,Action parentAction) {

		if(satInfo == null){
			return null;
		}
		int index = 0;
		int typeInt = parentAction.mSubChildGroup.get(index).mInitValue;
		++index; // skip Sat Name.
		int lnbPowerInt = parentAction.mSubChildGroup.get(++index).mInitValue;
		int antennaType = MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE);
		//boolean isSingleCable = (antennaType == 1 ? true : false);
		boolean isUnCable = (antennaType == 0? true : false);
		int lnbFreqInt = parentAction.mSubChildGroup.get(++index).mInitValue;
		int tone22KHZInt = 0;
		if (isUnCable) {
			++index;
			tone22KHZInt = parentAction.mSubChildGroup.get(++index).mInitValue;
		}
		SatelliteInfo info = satInfo;
		info.setEnabled(typeInt == 0 ? true : false);
		if (!isUnCable) {
			info.setPosition(lnbPowerInt);
		} else {
			info.setLnbPower(lnbPowerInt);
			info.setM22k(tone22KHZInt);
		}
		if (lnbFreqInt < LNB_CONFIG.length) {
			info.setLnbLowFreq(LNB_CONFIG[lnbFreqInt][0]);
			info.setLnbHighFreq(LNB_CONFIG[lnbFreqInt][1]);
			info.setLnbSwitchFreq(LNB_CONFIG[lnbFreqInt][2]);
			if (lnbFreqInt > 2) {
				info.setLnbType(LNB_SINGLE_FREQ);
			} else {
				info.setLnbType(LNB_DUAL_FREQ);
			}
		}
		return info;
	}

	/**
	 * prepare DVBS Scan diseqc 1.2 set  parameter
	 */
	public SatelliteInfo updateValueForDiseqc12Set(SatelliteInfo satInfo,Action parentAction) {

		if(satInfo == null){
			return null;
		}
		//int index = 0;
		Action diseqc10Item = parentAction.mSubChildGroup.get(0);
		int diseqc10Port = diseqc10Item.mInitValue;
		int diseqcInputIntType = 0;
		//int toneBrust = 2;//disable
		if (diseqc10Port == 0) {//disable
			diseqcInputIntType = 0;
			diseqc10Port = 255;
		} else {
			diseqcInputIntType = 2; //is 4x1
			diseqc10Port=diseqc10Port-1;
		}

		Action diseqc11Item = parentAction.mSubChildGroup.get(1);
		int diseqc11Port = diseqc11Item.mInitValue;
		int diseqc11InputIntType = 0;
		if (diseqc11Port == 0) {//disable
			diseqc11InputIntType = 0;
			diseqc11Port=255;
		} else {
			diseqc11InputIntType = 4;// 2 change to 4 For DTV01729720
			diseqc11Port=diseqc11Port-1;
		}


         boolean isDiseqc12 = MarketRegionInfo.
                                          isFunctionSupport(MarketRegionInfo.F_DISEQC12_IMPROVE);

        if(isDiseqc12){
            Action diseqcMotorItem = parentAction.mSubChildGroup.get(2);
            int motorPort = diseqcMotorItem.mInitValue;
            int motorPortIntType = 0;
            if(motorPort==0){
               motorPortIntType=0;
               motorPort=255;
            }else{
               motorPortIntType=5;
               motorPort=0;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "motor:"+motorPort);
            satInfo.setMotorType(motorPortIntType);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"diseqc10Port:"+diseqc10Port+
                    "diseqc11Port:"+diseqc11Port );
		satInfo.setDiseqcType(diseqcInputIntType);
		satInfo.setPort(diseqc10Port);
		satInfo.setDiseqcTypeEx(diseqc11InputIntType);
		satInfo.setPortEx(diseqc11Port);
		//satInfo.setMotorType(motorPortIntType);
		//satInfo.setMotorPosition(motorPort);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"updateDVBSSatInfoDiseqcSet12>>" + satInfo.toString());
		return satInfo;
	}

	String LNBFreqTitle;
	String diseqcInputTitle;
	String Tone22KHZTitle;
	String diseqcSetTitle;

	public List<Action> initSatelliteInfoViews(final Action parentItem, final int recID) {
		mRecID = recID;
        // boolean isDiseqc12 =
        // MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DISEQC12_IMPROVE);
        /*if (false isDiseqc12 ) {
            return initSatelliteInfoViewsDiseqc12(parentItem, recID, mContext);
        }*/
        final boolean atMenu = (parentItem == null) ? false : true;
        boolean isGerenalSat = !ScanContent.isPreferedSat();
        boolean isEnableScanType = ScanContent.enableScanStoreTypeForCustomer();
        boolean atManualTPInfo;
        if (atMenu
                && parentItem != null
                && parentItem.mItemID
                        .equalsIgnoreCase(MenuConfigManager.DVBS_SAT_MANUAL_TURNING)) {
            atManualTPInfo = true;
        } else {
            atManualTPInfo = false;
        }
        int antennaType = MtkTvConfig.getInstance().getConfigValue(
                SAT_ANTENNA_TYPE);
        List<Action> items = new ArrayList<Action>();
        final List<SatelliteInfo> mSatellites = ScanContent.getDVBSsatellites(mContext);
        final SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(
                mContext, recID, mSatellites);
        if (info == null) {
            return items;
        }
        String selectedSatelliteID = String.valueOf(info.getSatlRecId());
        String[] satelliteTypeList = mContext.getResources().getStringArray(
                R.array.dvbs_sat_on_off);
        String transponder;
        if(parentItem != null
                && parentItem.mItemID
                .equalsIgnoreCase(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)){
            transponder = ScanContent.getDVBSTransponderStrTitle(true, Integer.parseInt(selectedSatelliteID));
        }else{
            transponder = ScanContent.getDVBSTransponderStrTitle(false, Integer.parseInt(selectedSatelliteID));
        }

        String satelliteTypeTitle = mContext.getResources().getString(
                R.string.dvbs_satellite_status);
        String satelliteName = mContext.getResources().getString(
                R.string.dvbs_satellite_selection);

        int defaultType = info.getEnable() ? 0 : 1;

        if (atMenu) {
            parentItem.mSubChildGroup = new ArrayList<Action>();
        }
        
        if(atManualTPInfo){ //add next
            final Action nextAction = new Action(MenuConfigManager.DVBS_SAT_MANUAL_TURNING_NEXT,
                    mContext.getString(R.string.setup_next),
                    Action.DataType.TEXTCOMMVIEW);
            items.add(nextAction);
            nextAction.satID = recID;
            nextAction.setEnabled(true);
            nextAction.setmParentGroup(parentItem.mSubChildGroup);
            parentItem.mSubChildGroup.add(nextAction);
        }
        
        final Action satStatus = new Action(satelliteTypeTitle,
                satelliteTypeTitle, MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, defaultType,
                satelliteTypeList, // on/off
                MenuConfigManager.STEP_VALUE, Action.DataType.LEFTRIGHT_VIEW);
        items.add(satStatus);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSatelliteInfoViews parentid =" + parentItem.mItemID);
        /*
         * if(ScanContent.isPreferedSat()){ if (parentItem!=null &&
         * (parentItem.mItemID
         * .equalsIgnoreCase(MenuConfigManager.DVBS_SAT_RE_SCAN) ||
         * parentItem.mItemID.equalsIgnoreCase(MenuConfigManager.DVBS_SAT_OP)))
         * { satStatus.setEnabled(false); } else { satStatus.setEnabled(true); }
         * }
         */

        if (atMenu) {
            satStatus.setmParentGroup(parentItem.mSubChildGroup);
            satStatus.setEnabled(!atManualTPInfo && (!ScanContent.isPreferedSat() || TVContent.getInstance(mContext).isM7ScanMode() || DVBSScanner.isDsmartScanMode()
                    || DVBSScanner.isNCPlusScanMode()));
            parentItem.mSubChildGroup.add(satStatus);
        }
        final Action satelliteNameItem;
		if(!ScanContent.isPreferedSat()){
		    SatelliteProviderManager proManager = new SatelliteProviderManager(mContext);
	        List<SatelliteConfigEntry> mSatelliteConfigEntries = null;
	        try {
	            mSatelliteConfigEntries = PartnerSettingsConfig.parserSatelliteConfigFromXml();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
            final String[] satNameList = ScanContent.getAvailableSatelliteNameList(mSatellites).toArray(new String[0]);
            int defaultName = -1;
            for (int i = 0; mSatelliteConfigEntries != null && i < mSatelliteConfigEntries.size(); i++) {
                if (info.getSatName() != null && i < satNameList.length && info.getSatName().equals(satNameList[i])) {
                    defaultName = i;
                }
            }
            satelliteNameItem = new Action(satelliteName, satelliteName,
                    MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
                    defaultName, satNameList, MenuConfigManager.STEP_VALUE,
                    Action.DataType.OPTIONVIEW);
            if(defaultName == -1){
                satelliteNameItem.setDescription(info.getSatName());
            }
            satelliteNameItem.setEnabled(satNameList.length > 0);
            satelliteNameItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                @Override
                public void afterOptionValseChanged(String afterName) {
                    SatelliteConfigEntry entry = proManager.getSatelliteInfoByNameFromDB(afterName);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "afterName="+afterName+", entry="+entry);
                    SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, recID, mSatellites);
                    if(satInfo == null || entry == null) {
                        return;
                    }
                    ScanContent.initSatInfoFromEntry(satInfo, entry, antennaType);
                    satInfo.setEnabled(true);
                    ScanContent.saveDVBSSatelliteToSatl(satInfo, false);
                    ScanContent.setDVBSTPInfo(satInfo.getSatlRecId(), ScanContent.getDVBSTransponder(satInfo.getSatlRecId()), entry);
                }
            });
        }else if(!isGerenalSat && ScanContent.isSatOpFastScan()){//for FastScan
            String[] satNameList = mContext.getResources().getStringArray(R.array.dvbs_fast_scan_satellite_names);
            final String[] orbitsList = mContext.getResources().getStringArray(R.array.dvbs_fast_scan_satellites_orbits);
            int defaultName = -1;
            for(int i = 0; i<satNameList.length; i++){
                if(info.getSatName() != null && info.getSatName().equals(satNameList[i])){
                    defaultName = i;
                    break;
                }
            }
            satelliteNameItem = new Action(satelliteName, satelliteName,
                    MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
                    defaultName, satNameList, MenuConfigManager.STEP_VALUE,
                    Action.DataType.OPTIONVIEW);
            if(defaultName == -1){
                satelliteNameItem.setDescription(info.getSatName());
            }
            satelliteNameItem.setEnabled(true);
            satelliteNameItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                @Override
                public void afterOptionValseChanged(String afterName) {
                    SatelliteInfo satInfo = new SatelliteInfo(ScanContent.getDVBSsatellitesBySatID(mContext, recID));
                    int nameIndex = 0;
                    for(int i = 0; i < satNameList.length; i++){
                        if(afterName.equals(satNameList[i])){
                            nameIndex = i;
                            break;
                        }
                    }
                    String orbitString = orbitsList[nameIndex];
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satelliteNameItem afterOptionValseChanged" + recID + ",afterName=" + afterName+",orbit="+orbitsList[nameIndex]);
                    int orbit = 0;
                    if(orbitString.contains("E")){
                        orbit = (int)(Float.parseFloat(orbitString.substring(0, orbitString.length() - 1)) * 10f);
                    }else {
                        orbit = -(int)(Float.parseFloat(orbitString.substring(0, orbitString.length() - 1)) * 10f);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satelliteName orbit="+orbit);
                    updateSatelliteNameAndOrbit(afterName, orbit);
                }
            });
        }else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSatelliteConfigEntries null");
            satelliteNameItem = new Action(satelliteName, satelliteName,
                    MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
                    0, new String[] { info.getSatName() },
                    MenuConfigManager.STEP_VALUE, Action.DataType.TEXTCOMMVIEW);
            satelliteNameItem.setEnabled(false);
        }

        if (atMenu) {
            satelliteNameItem.satID = parentItem.satID;
            items.add(satelliteNameItem);
            satelliteNameItem.setmParentGroup(parentItem.mSubChildGroup);
            parentItem.mSubChildGroup.add(satelliteNameItem);
        }

        final List<String> scanModes = ScanContent.getDVBSScanMode(mContext);
        Action scanModeAction = new Action(
                mContext.getString(R.string.dvbs_scan_mode),
                mContext.getString(R.string.dvbs_scan_mode),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, info.dvbsScanMode,
                scanModes.toArray(new String[0]),
                MenuConfigManager.STEP_VALUE, Action.DataType.LEFTRIGHT_VIEW);
        scanModeAction.setEnabled(isGerenalSat && defaultType == 0 && scanModes.size() > 1);
        parentItem.mSubChildGroup.add(scanModeAction);
        scanModeAction.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
            
            @Override
            public void afterOptionValseChanged(String afterName) {
                if(afterName.equals(mContext.getString(R.string.dvbs_scan_mode_network))){
                    info.dvbsScanMode = DVBSSettingsInfo.NETWORK_SCAN_MODE;
                }else if(afterName.equals(mContext.getString(R.string.dvbs_scan_mode_full))){
                    info.dvbsScanMode = DVBSSettingsInfo.FULL_SCAN_MODE;
                }
                if(isGerenalSat) {
                    ScanContent.saveSatelliteInfoToDb(info);
                }
            }
        });
        
        final List<String> scanTypes = ScanContent.getDVBSConfigInfoChannels(mContext);
        Action scanTypeAction = new Action(
                mContext.getString(R.string.dvbs_satellite_channel),
                mContext.getString(R.string.dvbs_satellite_channel),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, info.dvbsScanType,
                scanTypes.toArray(new String[0]),
                MenuConfigManager.STEP_VALUE, Action.DataType.LEFTRIGHT_VIEW);
        scanTypeAction.setEnabled((isGerenalSat || isEnableScanType) && defaultType == 0 && scanTypes.size() > 1);
        parentItem.mSubChildGroup.add(scanTypeAction);
        scanTypeAction.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
            
            @Override
            public void afterOptionValseChanged(String afterName) {
                if(afterName.equals(mContext.getString(R.string.dvbs_channel_all))){
                    info.dvbsScanType = DVBSSettingsInfo.CHANNELS_ALL;
                }else if(afterName.equals(mContext.getString(R.string.dvbs_channel_encrypted))){
                    info.dvbsScanType = DVBSSettingsInfo.CHANNELS_ENCRYPTED;
                }else if(afterName.equals(mContext.getString(R.string.dvbs_channel_free))){
                    info.dvbsScanType = DVBSSettingsInfo.CHANNELS_FREE;
                }
                if(isGerenalSat) {
                    ScanContent.saveSatelliteInfoToDb(info);
                }else{
                    ScanContent.mCurrentScanType = info.dvbsScanType;
                }
            }
        });
        
        final List<String> storeTypes = ScanContent.getDVBSConfigInfoChannelStoreTypes(mContext);
        Action storeTypeAction = new Action(
                mContext.getString(R.string.dvbs_satellite_channel_store_type),
                mContext.getString(R.string.dvbs_satellite_channel_store_type),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, info.dvbsStoreType,
                storeTypes.toArray(new String[0]),
                MenuConfigManager.STEP_VALUE, Action.DataType.LEFTRIGHT_VIEW);
        storeTypeAction.setEnabled((isGerenalSat || isEnableScanType) && defaultType == 0 && storeTypes.size() > 1);
        parentItem.mSubChildGroup.add(storeTypeAction);
        storeTypeAction.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
            
            @Override
            public void afterOptionValseChanged(String afterName) {
                if(afterName.equals(mContext.getString(R.string.dvbs_channel_all))){
                    info.dvbsStoreType = DVBSSettingsInfo.CHANNELS_STORE_TYPE_ALL;
                }else if(afterName.equals(mContext.getString(R.string.dvbs_channel_story_type_digital))){
                    info.dvbsStoreType = DVBSSettingsInfo.CHANNELS_STORE_TYPE_DIGITAL;
                }else if(afterName.equals(mContext.getString(R.string.dvbs_channel_story_type_radio))){
                    info.dvbsStoreType = DVBSSettingsInfo.CHANNELS_STORE_TYPE_RADIO;
                }
                if(isGerenalSat) {
                    ScanContent.saveSatelliteInfoToDb(info);
                }else {
                    ScanContent.mCurrentStoreType = info.dvbsStoreType;
                }
            }
        });

        // add diseqc set
        /*String diseqcSetTitle = mContext.getResources().getString(
                R.string.dvbs_diseqc_set);
        Action.DataType diseqcType = Action.DataType.TEXTCOMMVIEW;
        if (atMenu) {
            diseqcType = Action.DataType.HAVESUBCHILD;
        }
        final Action diseqcSet = new Action(
                MenuConfigManager.DVBS_DETAIL_DISEQC12_SET, diseqcSetTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, 0, new String[] { "" },
                MenuConfigManager.STEP_VALUE, diseqcType);
        if (antennaType == 0) {
            items.add(diseqcSet);
            if (atMenu) {
                diseqcSet.mLocationId = parentItem.mLocationId;
                diseqcSet.satID = parentItem.satID;
                diseqcSet.mParent = parentItem;
                diseqcSet.setmParentGroup(parentItem.mSubChildGroup);
                // if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_DISEQC)){
                parentItem.mSubChildGroup.add(diseqcSet);
                // }
                if (!atManualTPInfo) {
                    diseqcSet.mSubChildGroup = new ArrayList<Action>();
                    initDiseqcSetPageSubItems(mContext, recID, diseqcSet,
                            parentItem, info);
                }
            }
        }*/
        
        //LNB configration
        String lnbconfigration = mContext.getString(R.string.dvbs_lnb_configration);
        final Action lnbCofigAction = new Action(
                lnbconfigration, lnbconfigration,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, 0, new String[] { "" },
                MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
            items.add(lnbCofigAction);
            if (atMenu) {
                lnbCofigAction.satID = parentItem.satID;
                lnbCofigAction.mParent = parentItem;
                lnbCofigAction.setmParentGroup(parentItem.mSubChildGroup);
                parentItem.mSubChildGroup.add(lnbCofigAction);
                lnbCofigAction.mSubChildGroup = new ArrayList<Action>();
                initlnbCofigActionPageSubItems(mContext, recID, lnbCofigAction,
                            parentItem, info, antennaType, atManualTPInfo);
            }

		String transponderTitle = mContext.getResources().getString(
				R.string.dvbs_trasponder);

		String transponderItemID = MenuConfigManager.DVBS_SAT_COMMON_TP;

        if (atManualTPInfo) {
            transponderItemID = MenuConfigManager.DVBS_SAT_MANUAL_TURNING_TP;
        }
        Action.DataType tpType = Action.DataType.TEXTCOMMVIEW;
        if (atMenu) {
            tpType = Action.DataType.HAVESUBCHILD;
        }
        final Action transponderItem = new Action(transponderItemID,
                transponderTitle, MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, null,
                MenuConfigManager.STEP_VALUE, tpType);
        transponderItem.setDescription(transponder);
        transponderItem.mSubChildGroup = new ArrayList<Action>();
        items.add(transponderItem);
        if (atMenu) {
            transponderItem.mLocationId = parentItem.mLocationId;
            transponderItem.mParent = parentItem;
            transponderItem.setmParentGroup(parentItem.mSubChildGroup);
            parentItem.mSubChildGroup.add(transponderItem);
        }

		if (atMenu) {// >>Transponder
			initTransponderItems(mContext, recID, transponderItem, parentItem);
		}
		items.addAll(initSignalLevelAndQualityItems(mContext, recID));
        ScanContent.setDVBSFreqToGetSignalQuality(recID);

		final Action signalQualityItem = items.get(items.size() - 2);
		final Action signalLevelItem = items.get(items.size() - 1);

		if (atMenu) {
			signalQualityItem.mLocationId = parentItem.mLocationId;
			signalQualityItem.mParent = parentItem;
			signalQualityItem.setmParentGroup(parentItem.mSubChildGroup);
			parentItem.mSubChildGroup.add(signalQualityItem);

			signalLevelItem.mLocationId = parentItem.mLocationId;
			signalLevelItem.mParent = parentItem;
			signalLevelItem.setmParentGroup(parentItem.mSubChildGroup);
			parentItem.mSubChildGroup.add(signalLevelItem);
		}

        if (!atManualTPInfo) {
            satStatus
                    .setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                        @Override
                        public void afterOptionValseChanged(String afterName) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satelliteSatus after name:"
                                    + afterName);
                            // satStatus.mInitValue =
                            // Arrays.asList(satStatus.mOptionValue).indexOf(afterName);
                            SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(
                                    mContext, recID);
                            if(satInfo == null) {
                                return;
                            }
                            if (afterName.equalsIgnoreCase(mContext.getString(R.string.dvbs_sat_on))) {
                                ((SatItem) parentItem).isOn = true;
                                satInfo.setEnabled(true);
                                satelliteNameItem.setEnabled(isGerenalSat && ScanContent.getAvailableSatelliteNameList(mSatellites).size() > 0);
                                lnbCofigAction.setEnabled(true);
                                transponderItem.setEnabled(true);
                                scanModeAction.setEnabled(isGerenalSat && scanModes.size() > 1);
                                scanTypeAction.setEnabled((isGerenalSat || isEnableScanType) && scanTypes.size() > 1);
                                storeTypeAction.setEnabled((isGerenalSat || isEnableScanType) && storeTypes.size() > 1);
                                //diseqcSet.setEnabled(false);
                                
                            } else if (afterName.equalsIgnoreCase(mContext.getString(R.string.dvbs_sat_off))) {
                                ((SatItem) parentItem).isOn = false;
                                satInfo.setEnabled(false);
                                satelliteNameItem.setEnabled(false);
                                lnbCofigAction.setEnabled(false);
                                transponderItem.setEnabled(false);
                                scanModeAction.setEnabled(false);
                                scanTypeAction.setEnabled(false);
                                storeTypeAction.setEnabled(false);
                                //diseqcSet.setEnabled(false);
                            }
                            
                            ScanContent.saveDVBSSatelliteToSatl(satInfo);
                        }
                    });
        }

        if (atManualTPInfo || defaultType == 1) {
            satelliteNameItem.setEnabled(false);
            scanModeAction.setEnabled(false);
            scanTypeAction.setEnabled(false);
            storeTypeAction.setEnabled(false);
            lnbCofigAction.setEnabled(false);
            transponderItem.setEnabled(true);

            if (defaultType == 1) {
                transponderItem.setEnabled(false);
            }
        }
        if (atMenu) {
            for (Action item : items) {
                item.mParent = parentItem;
            }
        }
        return items;
    }

	/**
//	 * for DVBS Transponder sub items
//	 * @param satID the id of satellite
//	 */
	public static List<Action> initTransponderItems(Context context, int satID, Action transponderItem,
			Action parentItem) {
		MtkTvScanTpInfo tpInfo = null;
		if(parentItem != null
                && parentItem.mItemID
                .equalsIgnoreCase(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)){
            tpInfo = ScanContent.getSavedBgmTPInfo(satID);
        }else {
            tpInfo = ScanContent.getDVBSTransponder(satID);
        }
		List<Action> items = new ArrayList<Action>();
		Action frequency = new Action(
				MenuConfigManager.DVBS_SAT_DEDATIL_INFO_TP_ITEMS,
				context.getString(R.string.dvbs_tp_fre), 0, 99999,
				tpInfo.i4Frequency, new String[] {},
				MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
		frequency.setInputLength(5);
		frequency.satID = satID;
		if (transponderItem != null && parentItem != null) {
			frequency.setmParentGroup(transponderItem.mSubChildGroup);
			frequency.mSubChildGroup = parentItem.mSubChildGroup;
			frequency.mLocationId = parentItem.mLocationId;
			frequency.mParent = transponderItem;
			transponderItem.mSubChildGroup.add(frequency);
		} else {
			items.add(frequency);
		}

		Action symbolRate = new Action(
				MenuConfigManager.DVBS_SAT_DEDATIL_INFO_TP_ITEMS,
				context.getString(R.string.dvbs_tp_sys_rate), 1000, 99999,
				tpInfo.i4Symbolrate, new String[] {},
				MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
		symbolRate.setInputLength(5);
		symbolRate.satID = satID;
		if (transponderItem != null && parentItem != null) {
			symbolRate.setmParentGroup(transponderItem.mSubChildGroup);
			symbolRate.mSubChildGroup = parentItem.mSubChildGroup;
			symbolRate.mLocationId = parentItem.mLocationId;
			symbolRate.mParent = transponderItem;
			transponderItem.mSubChildGroup.add(symbolRate);
		} else {
			items.add(symbolRate);
		}

		TunerPolarizationType position = tpInfo.ePol;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tpInfo.ePol:"+position.getValue());
        int positionInt = 0;
        if(position.getValue() > 0) {
          positionInt = position.getValue() - 1;
        }
//		positionInt = (position==TunerPolarizationType.POL_LIN_HORIZONTAL?0:1);
		Action polarization = new Action(
//				MenuConfigManager.DVBS_SAT_DEDATIL_INFO_TP_ITEMS,
				SettingsUtil.SPECIAL_SAT_DETAIL_INFO_ITEM_POL,
				context.getString(R.string.dvbs_tp_pol),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				/* MenuConfigManager.INVALID_VALUE */positionInt, context
						.getResources().getStringArray(R.array.dvbs_tp_pol_arrays),
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
		polarization.satID = satID;
		polarization.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

            @Override
            public void afterOptionValseChanged(String afterName) {
                MenuDataHelper.getInstance(context).saveDVBSSatTPInfo(context, satID, afterName);
            }
        });
		if (transponderItem != null && parentItem != null) {
			polarization.setmParentGroup(transponderItem.mSubChildGroup);
			polarization.mSubChildGroup = parentItem.mSubChildGroup;
			polarization.mLocationId = parentItem.mLocationId;
			polarization.mParent = transponderItem;
			transponderItem.mSubChildGroup.add(polarization);
		} else {
			items.add(polarization);
		}
		if (transponderItem != null && parentItem != null) {
			return transponderItem.mSubChildGroup;
		}
		return items;
	}

	/**
//	 * for DVBS signal level and signal quality
//	 * @param satID the id of satellite
//	 */
	private static List<Action> initSignalLevelAndQualityItems(Context mContext, int satID) {
		List<Action> items = new ArrayList<Action>();
		String signalQualityTitle = mContext.getResources().getString(R.string.menu_tv_single_signal_quality);
		int quality=0;
		int level=0;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("xinsheng", "quality>>>" + quality + ">>" + level);
		final Action signalQualityItem = new Action(MenuConfigManager.DVBS_SIGNAL_QULITY,
				signalQualityTitle, 0, 100, quality,
				new String[] { String.valueOf(quality) }, MenuConfigManager.STEP_VALUE,
				Action.DataType.PROGRESSBAR);

		signalQualityItem.setSupportModify(false);
		signalQualityItem.setDescription(quality+"%");
		signalQualityItem.signalType= Action.SIGNAL_QUALITY;

		items.add(signalQualityItem);

		String signalLevelTitle = mContext.getResources().getString(R.string.menu_tv_single_signal_level);
		final Action signalLevelItem = new Action(MenuConfigManager.DVBS_SIGNAL_LEVEL,
				signalLevelTitle, 0, 100, level,
				new String[] { String.valueOf(level) }, MenuConfigManager.STEP_VALUE,
				Action.DataType.PROGRESSBAR);
		signalLevelItem.setSupportModify(false);
		signalLevelItem.setDescription(level+"%");
		signalLevelItem.signalType=Action.SIGNAL_LEVEL;
		items.add(signalLevelItem);
		return items;
	}


    
    public List<Action> initlnbCofigActionPageSubItems(final Context context,
            final int satID, final Action lnbCofigAction, Action granPaItem,
            SatelliteInfo info, int antennaType, boolean atManualTPInfo) {
        List<Action> items = new ArrayList<Action>();
        if (info == null) {
            return items;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initlnbCofigActionPageSubItems name:" + info.getName() + "type:" + info.getType()
                + "enable:" + info.getEnable());

        final boolean isUnCable = (antennaType == MenuConfigManager.DVBS_ACFG_SINGLE ||
                                    antennaType == MenuConfigManager.DVBS_ACFG_TONEBURST ||
                                    antennaType == MenuConfigManager.DVBS_ACFG_DISEQC10 || 
                                        antennaType == MenuConfigManager.DVBS_ACFG_DISEQC11 ||
                                                antennaType == MenuConfigManager.DVBS_ACFG_DISEQC12 ? true : false);
        final boolean isSingleCable = (antennaType == MenuConfigManager.DVBS_ACFG_UNICABLE1 ? true : false);
        final boolean isJessCable = (antennaType == MenuConfigManager.DVBS_ACFG_UNICABLE2 ? true : false);
        
        // String[] LNBPowerList =
        // mContext.getResources().getStringArray(R.array.dvbs_sat_lnb_power);
        String[] lnbPowerList = new String[] {
                mContext.getResources().getString(R.string.common_off),
                mContext.getResources().getString(R.string.common_on) };
        String[] lnbFreqList = mContext.getResources().getStringArray(
                R.array.dvbs_sat_lnbfreq);
        final String[] diseqcInputList = mContext.getResources()
                .getStringArray(R.array.dvbs_diseqc_input_arrays);
        String[] tone22KHZList = mContext.getResources().getStringArray(
                R.array.dvbs_tone_22khz_arrays);
        String[] toneBurstList = mContext.getResources().getStringArray(
                R.array.dvbs_tone_burst_arrays);
        
        String lnbPowerTitle = mContext.getResources().getString(
                R.string.dvbs_lnb_power);
        int defaultLNBPower = info.getLnbPower();
        defaultLNBPower = Math.min(defaultLNBPower, lnbPowerList.length - 1);
        defaultLNBPower = Math.max(defaultLNBPower, 0);

        final Action lnbPower = new Action(
                MenuConfigManager.DVBS_DETAIL_LNB_POWER, lnbPowerTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, defaultLNBPower, lnbPowerList,
                MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
        if (isUnCable) {
            items.add(lnbPower);
            lnbPower.setmParentGroup(lnbCofigAction.mSubChildGroup);
            lnbCofigAction.mSubChildGroup.add(lnbPower);
            lnbPower.satID = lnbCofigAction.satID;
            lnbPower.mParent = lnbCofigAction;
            // LNBPower.setEnabled(false);
            lnbPower.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
                @Override
                public void afterOptionValseChanged(String afterName) {
                    /** tuner LNBPower */
                    SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                    // satInfo.setLnbPower(LNBPower.mInitValue);
                    if(satInfo == null) {
                        return;
                    }
                    ScanContent.saveDVBSSatelliteToSatl(updateValue(satInfo,
                            lnbCofigAction));

                    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
                    MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan
                            .dvbsSetTunerLnbPower(lnbPower.mInitValue);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "initSatelliteInfoViews>>dvbsSetTunerLnbPower>dvbsRet>"
                                    + dvbsRet);
                }
            });
        }

        if (LNB_CONFIG == null || LNB_CONFIG.length < 12) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSatelliteInfoViews>>initLNBConfig>");
            initLNBConfig();
        }
        LNBFreqTitle = mContext.getResources()
                .getString(R.string.dvbs_lnb_freq);
        int defaultLNBFreq = ScanContent.getDefaultLnbFreqIndex(info,
                LNB_CONFIG);
        if (LNB_CONFIG[11][0] != 0) {
            List<String> lnbFreqListArray = new ArrayList<String>();
            Collections.addAll(lnbFreqListArray, lnbFreqList);
            lnbFreqListArray.remove(lnbFreqList.length - 1);
            String userDefine = null;
            if (LNB_CONFIG[11][1] != 0) {
                /*
                 * if(LNB_CONFIG[11][2] != 0){ userDefine =
                 * LNB_CONFIG[11][0]+","+
                 * LNB_CONFIG[11][1]+","+LNB_CONFIG[11][2]; }else {
                 */
                userDefine = LNB_CONFIG[11][0] + "," + LNB_CONFIG[11][1];
                // }
            } else {
                userDefine = LNB_CONFIG[11][0] + "";
            }
            lnbFreqListArray.add(userDefine);
            lnbFreqListArray.add(lnbFreqList[lnbFreqList.length - 1]);
            lnbFreqList = lnbFreqListArray.toArray(new String[0]);
        }
        final Action lnbFreq = new Action(
                MenuConfigManager.DVBS_DETAIL_LNB_FREQUENCY, LNBFreqTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, defaultLNBFreq, lnbFreqList,
                MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
        items.add(lnbFreq);

        lnbFreq.setmParentGroup(lnbCofigAction.mSubChildGroup);
        lnbCofigAction.mSubChildGroup.add(lnbFreq);
        lnbFreq.satID = lnbCofigAction.satID;
        lnbFreq.mParent = lnbCofigAction;
        
        Tone22KHZTitle = mContext.getResources().getString(
                R.string.dvbs_tone_22khz);

        int defaultTone22KHZ = info.getM22k();
        defaultTone22KHZ = Math.min(defaultTone22KHZ, tone22KHZList.length - 1);
        defaultTone22KHZ = Math.max(defaultTone22KHZ, 0);

        final Action tone22KHZ = new Action(Tone22KHZTitle, Tone22KHZTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, defaultTone22KHZ,
                tone22KHZList, MenuConfigManager.STEP_VALUE,
                Action.DataType.OPTIONVIEW);
        if (isUnCable) {
            items.add(tone22KHZ);
            tone22KHZ
                    .setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
                        @Override
                        public void afterOptionValseChanged(String afterName) {
                            tone22KHZ.mInitValue = Arrays.asList(
                                    tone22KHZ.mOptionValue).indexOf(afterName);
                            /** tuner Tone22KHZ */
                            SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                            // satInfo.setM22k(Tone22KHZ.mInitValue);
                            // satInfo.setEnable(true);
                            if(satInfo == null) {
                                return;
                            }
                            ScanContent.saveDVBSSatelliteToSatl(updateValue(
                                    satInfo, lnbCofigAction));
                            MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
                            MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan
                                    .dvbsSetTuner22k(tone22KHZ.mInitValue);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "initSatelliteInfoViews>>dvbsSetTuner22k>dvbsRet>"
                                            + dvbsRet);
                        }
                    });
                tone22KHZ.setmParentGroup(lnbCofigAction.mSubChildGroup);
                lnbCofigAction.mSubChildGroup.add(tone22KHZ);
        }

        if(antennaType == MenuConfigManager.DVBS_ACFG_TONEBURST){
            String toneBurstTitle = mContext.getResources().getString(
                    R.string.tone_burst_port);
            int defaultToneBurstItem = info.getToneBurst();
            defaultToneBurstItem = Math.min(defaultToneBurstItem,
                    toneBurstList.length - 1);
            defaultToneBurstItem = Math.max(defaultToneBurstItem, 0);
            final Action toneBurstItem = new Action(toneBurstTitle, toneBurstTitle,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE, defaultToneBurstItem,
                    toneBurstList, MenuConfigManager.STEP_VALUE,
                    Action.DataType.OPTIONVIEW);
            if (isUnCable) {
                items.add(toneBurstItem);
                toneBurstItem
                .setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
                    @Override
                    public void afterOptionValseChanged(String afterName) {
                        toneBurstItem.mInitValue = Arrays.asList(
                                toneBurstItem.mOptionValue).indexOf(
                                        afterName);
                        /** tuner toneBurst */
                        SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                        // satInfo.setToneBurst(toneBurstItem.mInitValue);
                        if(satInfo == null) {
                            return;
                        }
                        ScanContent.saveDVBSSatelliteToSatl(updateValue(
                                satInfo, lnbCofigAction));
                        MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
                        if (toneBurstItem.mInitValue == toneBurstItem.mOptionValue.length - 1) {// tuner
                            // disable
                            // MtkTvScanDvbsBase.ScanDvbsRet dvbsRet =
                            // dvbsScan.dvbsSetTunerDiseqc10Reset();// no need
                            // reset
                            MtkTvScanDvbsBase.ScanDvbsRet dvbsRet2 = dvbsScan
                                    .dvbsSetTunerDiseqc10Disable();
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "initSatelliteInfoViews>>dvbsSetTunerDiseqc10Disable>dvbsRet>"
                                            + dvbsRet2);
                            
                        } else {
                            MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan
                                    .dvbsSetTunerDiseqc10ToneBurst(toneBurstItem.mInitValue);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "initSatelliteInfoViews>>dvbsSetTunerDiseqc10ToneBurst>dvbsRet>"
                                            + dvbsRet);                            
                        }
                    }
                });
                toneBurstItem.setmParentGroup(lnbCofigAction.mSubChildGroup);
                lnbCofigAction.mSubChildGroup.add(toneBurstItem);
            }
        }
        
        String[] positionList = mContext.getResources().getStringArray(
                R.array.dvbs_single_cable_position_arrays);
        if (antennaType == 2) {
            positionList = mContext.getResources().getStringArray(
                    R.array.dvbs_jess_cable_position_arrays);
        }
        /*String positionTitle = mContext.getResources().getString(
                R.string.dvbs_scan_positon);
        int defaultPosition = Math.max(info.getPosition() - 1, 0);
        defaultPosition = Math.min(defaultPosition, positionList.length - 1);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultPosition:" + defaultPosition
                + ",info.getPosition():" + info.getPosition());
        final Action positionItem = new Action(
                MenuConfigManager.DVBS_DETAIL_POSITION, positionTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, defaultPosition, positionList,
                MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
        if (isSingleCable || antennaType == 2) {
            items.add(positionItem);
                positionItem.satID = lnbCofigAction.satID;
                positionItem.setmParentGroup(lnbCofigAction.mSubChildGroup);
                lnbCofigAction.mSubChildGroup.add(positionItem);
            positionItem
                    .setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                        @Override
                        public void afterOptionValseChanged(String afterName) {
                            SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                            ScanContent.saveDVBSSatelliteToSatl(updateValue(
                                    satInfo, lnbCofigAction));
                        }
                    });
        }*/
        String diseqcPort = mContext.getResources().getString(
                R.string.diseqc_port);
        String[] portList = null;
        int defaultPortValue = 0;
        switch(antennaType){
            case MenuConfigManager.DVBS_ACFG_UNICABLE1:
                diseqcPort = mContext.getResources().getString(R.string.dvbs_scan_positon);
                portList = mContext.getResources().getStringArray(R.array.dvbs_single_cable_position_arrays);
                defaultPortValue = Math.max(info.getPosition() - 1, 0);
                defaultPortValue = Math.min(defaultPortValue, positionList.length - 1);
                break;
            case MenuConfigManager.DVBS_ACFG_UNICABLE2:
                diseqcPort = mContext.getResources().getString(R.string.dvbs_scan_positon);
                portList = mContext.getResources().getStringArray(R.array.dvbs_jess_cable_position_arrays);
                defaultPortValue = Math.max(info.getPosition() - 1, 0);
                defaultPortValue = Math.min(defaultPortValue, positionList.length - 1);
                break;
            case MenuConfigManager.DVBS_ACFG_DISEQC10: 
                portList = context.getResources().getStringArray(R.array.dvbs_diseqc_10_port_sub_arrays);
                if (info.getDiseqcType() == 0) {// 255 means disable,0 means diseqcA
                    defaultPortValue = 0;
                } else {
                    defaultPortValue = info.getPort() + 1;
                }
                break;
            case MenuConfigManager.DVBS_ACFG_DISEQC11:
                portList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_11_port_sub_arrays);
                if (info.getDiseqcTypeEx() == 0) {
                    defaultPortValue = 0;
                } else {
                    defaultPortValue = info.getPortEx() + 1;
                }
                break;
            case MenuConfigManager.DVBS_ACFG_DISEQC12:
                diseqcPort = mContext.getResources().getString(R.string.motor_settings);
                portList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_motor_arrays);
                if (info.getMotorType() == 5) {
                    defaultPortValue = 1;
                } else {
                    defaultPortValue = 0;
                }
                break;
                default:
                break;
        }
        if(antennaType != MenuConfigManager.DVBS_ACFG_SINGLE && antennaType != MenuConfigManager.DVBS_ACFG_TONEBURST){
            final Action diseqcPortAction = new Action(
                    MenuConfigManager.DVBS_DETAIL_DISEQC12_SET, diseqcPort,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE, defaultPortValue, portList,
                    MenuConfigManager.STEP_VALUE, antennaType == MenuConfigManager.DVBS_ACFG_DISEQC12 ? Action.DataType.HAVESUBCHILD : Action.DataType.OPTIONVIEW);
            items.add(diseqcPortAction);
            diseqcPortAction.mLocationId = lnbCofigAction.mLocationId;
            diseqcPortAction.satID = lnbCofigAction.satID;
            diseqcPortAction.mParent = lnbCofigAction;
            diseqcPortAction.setmParentGroup(lnbCofigAction.mSubChildGroup);
            if(antennaType != MenuConfigManager.DVBS_ACFG_SINGLE){
                lnbCofigAction.mSubChildGroup.add(diseqcPortAction);
            }
            diseqcPortAction.mSubChildGroup = new ArrayList<Action>();
            /*initDiseqcPortItems(mContext, satID, diseqcPortAction,
                        lnbCofigAction, info);*/
            diseqcPortAction.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                @Override
                public void afterOptionValseChanged(String afterName) {
                    switch (antennaType){
                        case MenuConfigManager.DVBS_ACFG_DISEQC10:
                            MenuDataHelper.getInstance(context).setDiseqc10TunerPort(diseqcPortAction.mInitValue);
                            break;
                        case MenuConfigManager.DVBS_ACFG_DISEQC11:
                            MenuDataHelper.getInstance(context).setDiseqc11TunerPort(diseqcPortAction.mInitValue);
                            break;
                        default:
                            break;
                    }
                    SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                    if(satInfo == null) {
                        return;
                    }
                    ScanContent.saveDVBSSatelliteToSatl(updateValue(
                            satInfo, lnbCofigAction));
                }
            });
            if(diseqcPortAction.mDataType == Action.DataType.HAVESUBCHILD){
                diseqcPortAction.setDescription("");
                initDiseqc12MotorPageItems(context, satID, diseqcPortAction,
                        lnbCofigAction, info);
            }else {
                diseqcPortAction.setDescription(portList[defaultPortValue]);
            }
        }
        
        
        lnbCofigAction.mSubChildGroup.addAll(initSignalLevelAndQualityItems(mContext, satID));
        
        if (!atManualTPInfo && isUnCable) {
            lnbFreq.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                @Override
                public void afterOptionValseChanged(String afterName) {
                    // LNBFreq.mInitValue =
                    // Arrays.asList(LNBFreq.mOptionValue).indexOf(afterName);
                    if (/*
                         * diseqcInput.mInitValue == diseqcInputList.length - 1
                         * &&
                         */lnbFreq.mInitValue > 2) {
                        tone22KHZ.setEnabled(true);
                    } else {
                        tone22KHZ.setEnabled(false);
                    }
                    SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                    if(satInfo == null) {
                        return;
                    }
                    ScanContent.saveDVBSSatelliteToSatl(updateValue(satInfo,
                            lnbCofigAction));
                }
            });
        } else if (isSingleCable || isJessCable) {
            lnbFreq.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                @Override
                public void afterOptionValseChanged(String afterName) {
                    // LNBFreq.mInitValue =
                    // Arrays.asList(LNBFreq.mOptionValue).indexOf(afterName);
                    SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                    if(satInfo == null) {
                        return;
                    }
                    // if (LNBFreq.mInitValue < LNB_CONFIG.length) {
                    // satInfo.setLnbLowFreq(LNB_CONFIG[LNBFreq.mInitValue][0]);
                    // satInfo.setLnbHighFreq(LNB_CONFIG[LNBFreq.mInitValue][1]);
                    // satInfo.setLnbSwitchFreq(LNB_CONFIG[LNBFreq.mInitValue][2]);
                    // if (LNBFreq.mInitValue > 2) {
                    // satInfo.setLnbType(LNB_SINGLE_FREQ);
                    // } else {
                    // satInfo.setLnbType(LNB_DUAL_FREQ);
                    // }
                    // }
                    ScanContent.saveDVBSSatelliteToSatl(updateValue(satInfo,
                            lnbCofigAction));
                }
            });
        }
        
        if (lnbFreq.mInitValue > 2) {
            tone22KHZ.setEnabled(true);
        } else {
            tone22KHZ.setEnabled(false);
        }
        
        /*String[] positionList = mContext.getResources().getStringArray(
                R.array.dvbs_single_cable_position_arrays);
        if (antennaType == 2) {
            positionList = mContext.getResources().getStringArray(
                    R.array.dvbs_jess_cable_position_arrays);
        }
        String positionTitle = mContext.getResources().getString(
                R.string.dvbs_scan_positon);
        int defaultPosition = Math.max(info.getPosition() - 1, 0);
        defaultPosition = Math.min(defaultPosition, positionList.length - 1);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultPosition:" + defaultPosition
                + ",info.getPosition():" + info.getPosition());
        final Action positionItem = new Action(
                MenuConfigManager.DVBS_DETAIL_POSITION, positionTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, defaultPosition, positionList,
                MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
        if (isSingleCable || antennaType == 2) {
            items.add(positionItem);
                positionItem.satID = lnbCofigAction.satID;
                positionItem.setmParentGroup(lnbCofigAction.mSubChildGroup);
                lnbCofigAction.mSubChildGroup.add(positionItem);
            positionItem
                    .setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

                        @Override
                        public void afterOptionValseChanged(String afterName) {
                            SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
                            ScanContent.saveDVBSSatelliteToSatl(updateValue(
                                    satInfo, lnbCofigAction));
                        }
                    });
        }*/
        
        return items;
    }
    
    public List<Action> initDiseqcSetPageSubItems(final Context context,
            final int satID, final Action diseqcSetItem, Action granPaItem,
            SatelliteInfo info) {
        ScanContent.setDVBSFreqToGetSignalQuality(satID);
        List<Action> items = new ArrayList<Action>();
       //SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(context, satID);
       if (info == null) {
           return items;
       }
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"name:"+info.getName()+"type:"+info.getType()+"enable:"+info.getEnable());
       final String[] diseqc10PortList = context.getResources().getStringArray(R.array.dvbs_diseqc_10_port_sub_arrays);
       String diseqc10portTitle = context.getResources().getString(R.string.dvbs_diseqc10_port);
       int  defaultDiseqc10Port = 0;
       if(info.getDiseqcType()==0){//255 means disable,0 means diseqcA
           defaultDiseqc10Port=0;
       }else{
           defaultDiseqc10Port=info.getPort()+1;
       }

       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultDiseqc10Port:"+defaultDiseqc10Port+",info.getDiseqcType():"+info.getDiseqcType());
       final Action diseqc10portItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC10_PORT, diseqc10portTitle,
               MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
               defaultDiseqc10Port, diseqc10PortList, MenuConfigManager.STEP_VALUE,
               Action.DataType.LEFTRIGHT_VIEW);
       if (diseqcSetItem != null && granPaItem != null) {
           diseqc10portItem.setmParentGroup(granPaItem.mSubChildGroup);
           diseqc10portItem.mParent = diseqcSetItem;
           diseqcSetItem.mSubChildGroup.add(diseqc10portItem);
       } else {
           items.add(diseqc10portItem);
       }
       diseqc10portItem.setBeforeChangedCallBack(new BeforeValueChangeCallback() {

           @Override
           public void beforeValueChanged(int lastValue) {
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultDiseqc10Port lastValue:"+lastValue);
               if (diseqc10portItem.mInitValue == 4 && lastValue == 5) {//(will to Tone burst A)
                   mDvbsNeedTunerReset = true;
               } else if (diseqc10portItem.mInitValue == 5 && lastValue == 4) {//(will to Tone burst B)
                   mDvbsNeedTunerReset = true;
               }else {
                   mDvbsNeedTunerReset = false;
               }

           }
       });
       diseqc10portItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

           @Override
           public void afterOptionValseChanged(String afterName) {//after value change, save sate info
               MenuDataHelper.getInstance(context).setDiseqc10TunerPort(diseqc10portItem.mInitValue);
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "diseqc10portItem>afterOptionValseChanged>" + diseqc10portItem.mInitValue);
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "diseqc10portItem>afterOptionValseChanged2>" + diseqc10portItem.mInitValue);
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultDiseqc10Port afterName:"+afterName);
               SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(context, satID);
               if(satInfo == null) {
                   return;
               }
               ScanContent.saveDVBSSatelliteToSatl(updateValueForDiseqc12Set(satInfo,diseqcSetItem));
           }
       });

       final String[] diseqc11PortList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_11_port_sub_arrays);
       String diseqc11portTitle = context.getResources().getString(R.string.dvbs_diseqc11_port);
       int defaultDiseqcInput11Port = 0;
       if(info.getDiseqcTypeEx()==0){
           defaultDiseqcInput11Port=0;
       }else{
           defaultDiseqcInput11Port=info.getPortEx()+1;
       }
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultDiseqcInput11Port:"+defaultDiseqcInput11Port);
       final Action diseqc11PortItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC11_PORT,
               diseqc11portTitle, MenuConfigManager.INVALID_VALUE,
               MenuConfigManager.INVALID_VALUE, defaultDiseqcInput11Port,
               diseqc11PortList, MenuConfigManager.STEP_VALUE,
               Action.DataType.LEFTRIGHT_VIEW);
       if (diseqcSetItem != null && granPaItem != null) {
           diseqc11PortItem.setmParentGroup(granPaItem.mSubChildGroup);
           diseqc11PortItem.mParent = diseqcSetItem;
           diseqcSetItem.mSubChildGroup.add(diseqc11PortItem);
       } else {
           items.add(diseqc11PortItem);
       }
       diseqc11PortItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

           @Override
           public void afterOptionValseChanged(String afterName) {//after value change, save sate info
                if(!"Disable".equalsIgnoreCase(afterName)){
                    diseqc10portItem.setDescription(0);
                    diseqc10portItem.mInitValue=0;
                }
                MenuDataHelper.getInstance(context).setDiseqc11TunerPort(diseqc11PortItem.mInitValue);
               diseqc11PortItem.mInitValue = Arrays.asList(diseqc11PortItem.mOptionValue).indexOf(afterName);
               SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
               if(satInfo == null) {
                   return;
               }
               ScanContent.saveDVBSSatelliteToSatl(updateValueForDiseqc12Set(satInfo,diseqcSetItem));
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "diseqc11PortItem>afterOptionValseChanged>" + diseqc11PortItem.mInitValue);
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "diseqc11PortItem afterName:"+afterName);

           }
       });
       diseqc11PortItem.setEnabled(!TVContent.getInstance(mContext).isM7ScanMode());

       int defaultMoto = 0;
       if(info.getMotorType()==5){
           defaultMoto=1;
       }else{
           defaultMoto=0;
       }
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defaultMoto:"+defaultMoto);
       final String[] diseqcMotorList = mContext.getResources().getStringArray(R.array.dvbs_diseqc_motor_arrays);
       String diseqcMotorTitle = context.getResources().getString(R.string.dvbs_diseqc_motor);
       final Action diseqcMotorItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR,
               diseqcMotorTitle, MenuConfigManager.INVALID_VALUE,
               MenuConfigManager.INVALID_VALUE,defaultMoto,
               diseqcMotorList, MenuConfigManager.STEP_VALUE,
               defaultMoto==0 ? Action.DataType.LEFTRIGHT_VIEW:Action.DataType.LEFTRIGHT_HASDETAILVIEW);
       boolean isDiseqc12 = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DISEQC12_IMPROVE);
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDiseqc12:"+isDiseqc12);
       if(isDiseqc12){
           if (diseqcSetItem != null && granPaItem != null) {
               diseqcMotorItem.mParent = diseqcSetItem;
               diseqcMotorItem.setmParentGroup(diseqcSetItem.mSubChildGroup);
               diseqcSetItem.mSubChildGroup.add(diseqcMotorItem);
               diseqcMotorItem.mSubChildGroup = new ArrayList<Action>();
               initDiseqc12MotorPageItems(context, satID, diseqcMotorItem, diseqcSetItem, info);
           } else {
               items.add(diseqcMotorItem);
           }
       }
       diseqcMotorItem.setEnabled(!TVContent.getInstance(mContext).isM7ScanMode());
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"motor item init...");
       diseqcMotorItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {
           @Override
           public void afterOptionValseChanged(String afterName) {
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"motor item aftername:"+afterName);
               diseqcMotorItem.setmDataType("Disable".equalsIgnoreCase(afterName) ? Action.DataType.LEFTRIGHT_VIEW:Action.DataType.LEFTRIGHT_HASDETAILVIEW);
               switchMotorType("Disable".equalsIgnoreCase(afterName)?0:5);
               diseqc10portItem.mInitValue=0;
               diseqc11PortItem.mInitValue =0; //Arrays.asList(diseqc11PortItem.mOptionValue).indexOf(afterName);
               SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
               if(satInfo == null) {
                   return;
               }
               ScanContent.saveDVBSSatelliteToSatl(updateValueForDiseqc12Set(satInfo,diseqcSetItem));
           }
       });
//       items.addAll(initSignalLevelAndQualityItems(context, satID));
//       Action signalQualityItem = items.get(items.size() - 2);
//       Action signalLevelItem = items.get(items.size() - 1);
//       if (diseqcSetItem != null && granPaItem != null) {
//           signalQualityItem.mParent = diseqcSetItem;
//           signalQualityItem.setmParentGroup(granPaItem.mSubChildGroup);
//           //diseqcSetItem.mSubChildGroup.add(signalQualityItem);
//
//           signalLevelItem.mParent = diseqcSetItem;
//           signalLevelItem.setmParentGroup(granPaItem.mSubChildGroup);
//           //diseqcSetItem.mSubChildGroup.add(signalLevelItem);
//       }
//     signalQualityItem.setEnabled(false);
//     signalLevelItem.setEnabled(false);
       if (diseqcSetItem != null && granPaItem != null) {
           for (Action tempItem:diseqcSetItem.mSubChildGroup) {
               tempItem.mLocationId = diseqcSetItem.mLocationId;
           }
           return diseqcSetItem.mSubChildGroup;
       } else {
           return items;
       }
   }
    private void switchMotorType(int type){
        MtkTvDvbsConfigBase mSatl = new MtkTvDvbsConfigBase();
        int svlID = CommonIntegration.getInstance().getSvl();
        MtkTvChannelInfoBase info = CommonIntegration.getInstance().getCurChInfo();
        if (info instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo channel = (MtkTvDvbChannelInfo)info;
            int satRecId = channel.getSatRecId();
            List<MtkTvDvbsConfigInfoBase> list = mSatl.getSatlRecord(svlID,
                    satRecId);
            if (list != null && list.isEmpty()) {
                MtkTvDvbsConfigInfoBase dvbsConfigInfoBase = list.get(0);
                dvbsConfigInfoBase.setMotorType(type);
                mSatl.updateSatlRecord(svlID, dvbsConfigInfoBase, true);
            }
        }
    }

    private int getMotorType(){
        MtkTvDvbsConfigBase mSatl = new MtkTvDvbsConfigBase();
        int svlID = CommonIntegration.getInstance().getSvl();
        MtkTvChannelInfoBase info = CommonIntegration.getInstance().getCurChInfo();
        if (info instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo channel = (MtkTvDvbChannelInfo)info;
            int satRecId = channel.getSatRecId();
            List<MtkTvDvbsConfigInfoBase> list = mSatl.getSatlRecord(svlID,
                    satRecId);
            if (list != null && list.isEmpty()) {
                MtkTvDvbsConfigInfoBase dvbsConfigInfoBase = list.get(0);
                int motorType= dvbsConfigInfoBase.getMotorType();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"motorType:"+motorType);
                if(5==motorType){
                    return 1;
                }
                return motorType;
            }
        }
       return 0;
    }
	/**
	 * for diseqc 1.2 motor items
	 */
	public List<Action> initDiseqc12MotorPageItems(Context context, int satID,
			Action diseqcMotorItem, Action parentItem, final SatelliteInfo info) {
		List<Action> items = new ArrayList<Action>();
		//final SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(context, satID);
		final SaveValue savevalue = SaveValue.getInstance(mContext);
		//int defaultMovementControl = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL);
		String[] movementControlList = context.getResources().getStringArray(R.array.dvbs_diseqc_movement_control_arrays);
		String movementControlTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_movementcontrol);
		Action.DataType mcType = Action.DataType.TEXTCOMMVIEW;
		if (diseqcMotorItem != null && parentItem != null) {
			mcType = Action.DataType.HAVESUBCHILD;
		}
		Action movementControlItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL,
                movementControlTitle,mcType);
        movementControlItem.setEnabled(true);
        //movementControlItem.setDescription(movementControlList[defaultMovementControl]);
		if (diseqcMotorItem != null && parentItem != null) {
			movementControlItem.mParent = diseqcMotorItem;
			movementControlItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(movementControlItem);
			movementControlItem.mSubChildGroup = new ArrayList<Action>();
			initDiseqc12MovementControlPageItmes(context, satID, movementControlItem, diseqcMotorItem, info);
		} else {
			items.add(movementControlItem);
		}

		String disableLimitsTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_disablelimits);
		Action disableLimitsItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_DISABLE_LIMITS, disableLimitsTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMotorItem != null && parentItem != null) {
			disableLimitsItem.mParent = diseqcMotorItem;
			disableLimitsItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(disableLimitsItem);
		} else {
			items.add(disableLimitsItem);
		}

		String limitEastTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_limiteast);
		Action limitEastItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_LIMIT_EAST, limitEastTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMotorItem != null && parentItem != null) {
			limitEastItem.mParent = diseqcMotorItem;
			limitEastItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(limitEastItem);
		} else {
			items.add(limitEastItem);
		}

		String limitWestTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_limitwest);
		Action limitWestItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_LIMIT_WEST, limitWestTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMotorItem != null && parentItem != null) {
			limitWestItem.mParent = diseqcMotorItem;
			limitWestItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(limitWestItem);
		} else {
			items.add(limitWestItem);
		}

		int defaultStorePos = Math.max(0, info.getMotorPosition()-1);//int defaultStorePos = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION);
		String[] storePositionList = context.getResources().getStringArray(R.array.dvbs_diseqc_motor_store_position_arrays);
		String storePositionTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_storeposition);
		final Action storePositionItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION, storePositionTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				defaultStorePos, storePositionList, MenuConfigManager.STEP_VALUE,
				Action.DataType.LEFTRIGHT_VIEW);
		if (diseqcMotorItem != null && parentItem != null) {
			storePositionItem.mParent = diseqcMotorItem;
			storePositionItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(storePositionItem);
		} else {
			items.add(storePositionItem);
		}
		storePositionItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

			@Override
			public void afterOptionValseChanged(String afterName) {
				storePositionItem.mInitValue = Arrays.asList(storePositionItem.mOptionValue).indexOf(afterName);
				savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION, storePositionItem.mInitValue);
				info.setMotorPosition( storePositionItem.mInitValue+1);
				info.setMotorType(5);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"info.toString():"+info.toString());
				ScanContent.saveDVBSSatelliteToSatl(info);
			}
		});

		int defaultGotoPos = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION);
		String[] gotoPositionList = context.getResources().getStringArray(R.array.dvbs_diseqc_motor_store_position_arrays);
		String gotoPositionTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_gotoposition);
		final Action gotoPositionItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION, gotoPositionTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				defaultGotoPos, gotoPositionList, MenuConfigManager.STEP_VALUE,
				Action.DataType.LEFTRIGHT_VIEW);
		if (diseqcMotorItem != null && parentItem != null) {
			gotoPositionItem.mParent = diseqcMotorItem;
			gotoPositionItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(gotoPositionItem);
		} else {
			items.add(gotoPositionItem);
		}
		gotoPositionItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

			@Override
			public void afterOptionValseChanged(String afterName) {
				gotoPositionItem.mInitValue = Arrays.asList(gotoPositionItem.mOptionValue).indexOf(afterName);
				savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION, gotoPositionItem.mInitValue);
			}
		});

		String gotoReferenceTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_gotoreference);
		Action gotoReferenceItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_REFERENCE, gotoReferenceTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMotorItem != null && parentItem != null) {
			gotoReferenceItem.mParent = diseqcMotorItem;
			gotoReferenceItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
			diseqcMotorItem.mSubChildGroup.add(gotoReferenceItem);
		} else {
			items.add(gotoReferenceItem);
		}
//		Action signalQualityItem = items.get(items.size() - 2);
//		Action signalLevelItem = items.get(items.size() - 1);
//		if (diseqcMotorItem != null && parentItem != null) {
//			signalQualityItem.mParent = diseqcMotorItem;
//			signalQualityItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
//			//diseqcMotorItem.mSubChildGroup.add(signalQualityItem);
//
//			signalLevelItem.mParent = diseqcMotorItem;
//			signalLevelItem.setmParentGroup(diseqcMotorItem.mSubChildGroup);
//			//diseqcMotorItem.mSubChildGroup.add(signalLevelItem);
//		}
//		signalQualityItem.setEnable(false);
//		signalLevelItem.setEnable(false);
		if (diseqcMotorItem != null && parentItem != null) {
			for (Action tempItem:diseqcMotorItem.mSubChildGroup) {
				tempItem.mLocationId = diseqcMotorItem.mLocationId;
			}
			diseqcMotorItem.mSubChildGroup.addAll(initSignalLevelAndQualityItems(context, satID));
			return diseqcMotorItem.mSubChildGroup;
		} else {
			return items;
		}
	}

	/**
	 * for diseqc 1.2 movement control items
	 */
	public List<Action> initDiseqc12MovementControlPageItmes(final Context context, final int satID
			, final Action diseqcMCItem, final Action parentItem, SatelliteInfo info) {
		List<Action> items = new ArrayList<Action>();

		final SaveValue savevalue = SaveValue.getInstance(mContext);
		int defaultMovementControl = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL);
		final String[] movementControlList = context.getResources().getStringArray(R.array.dvbs_diseqc_movement_control_arrays);
		String movementControlTitle = context.getResources().getString(R.string.dvbs_diseqc12_motor_movementcontrol);
		final Action movementControlItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL, movementControlTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				defaultMovementControl, movementControlList, MenuConfigManager.STEP_VALUE,
				Action.DataType.OPTIONVIEW);
		if (diseqcMCItem != null && parentItem != null) {
			movementControlItem.mParent = diseqcMCItem;
			movementControlItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
			diseqcMCItem.mSubChildGroup.add(movementControlItem);
		} else {
			items.add(movementControlItem);
		}

		int defaultStepSize = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE);
		if(defaultStepSize == 0){
			defaultStepSize = 1;
		}
		String stepSizeTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_stepsize);
		final Action stepSizeItem = new Action(
				MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE,
				stepSizeTitle, 1, 127,
				defaultStepSize, new String[] {},
				MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
		stepSizeItem.setInputLength(3);
		if (diseqcMCItem != null && parentItem != null) {
			stepSizeItem.mParent = diseqcMCItem;
			stepSizeItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
			diseqcMCItem.mSubChildGroup.add(stepSizeItem);
		} else {
			items.add(stepSizeItem);
		}

		int defaultTimeout = savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS);
		if(defaultTimeout == 0){
			defaultTimeout = 1;
		}
		String tiemoutTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_timeout);
		final Action timeoutSItem = new Action(
				MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS,
				tiemoutTitle, 1, 126,
				defaultTimeout, new String[] {},
				MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
		timeoutSItem.setInputLength(3);
		if (diseqcMCItem != null && parentItem != null) {
			timeoutSItem.mParent = diseqcMCItem;
			timeoutSItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
			diseqcMCItem.mSubChildGroup.add(timeoutSItem);
		} else {
			items.add(timeoutSItem);
		}

		if (movementControlItem.mInitValue == 0) {
			stepSizeItem.setEnabled(false);
			timeoutSItem.setEnabled(false);
		} else if (movementControlItem.mInitValue == 1) {
			stepSizeItem.setEnabled(true);
			timeoutSItem.setEnabled(false);
		} else if (movementControlItem.mInitValue == 2) {
			stepSizeItem.setEnabled(false);
			timeoutSItem.setEnabled(true);
		}

		String moveEastTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_moveeast);
		final Action moveEastItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_EAST, moveEastTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMCItem != null && parentItem != null) {
			moveEastItem.mParent = diseqcMCItem;
			moveEastItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
			diseqcMCItem.mSubChildGroup.add(moveEastItem);
		} else {
			items.add(moveEastItem);
		}

		String moveWestTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_movewest);
		final Action moveWestItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_WEST, moveWestTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMCItem != null && parentItem != null) {
			moveWestItem.mParent = diseqcMCItem;
			moveWestItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
			diseqcMCItem.mSubChildGroup.add(moveWestItem);
		} else {
			items.add(moveWestItem);
		}

		String stopMovementTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_stopmovement);
		final Action stopMovementItem = new Action(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STOP_MOVEMENT, stopMovementTitle,
				MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
				0, new String[] { "" }, MenuConfigManager.STEP_VALUE,
				Action.DataType.DISEQC12_SAVEINFO);
		if (diseqcMCItem != null && parentItem != null) {
			stopMovementItem.mParent = diseqcMCItem;
			stopMovementItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
			diseqcMCItem.mSubChildGroup.add(stopMovementItem);
		} else {
			items.add(stopMovementItem);
		}
//		items.addAll(initSignalLevelAndQualityItems(context, satID));
//		Action signalQualityItem = items.get(items.size() - 2);
//		Action signalLevelItem = items.get(items.size() - 1);
//		if (diseqcMCItem != null && parentItem != null) {
//			signalQualityItem.mParent = diseqcMCItem;
//			signalQualityItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
//			//diseqcMCItem.mSubChildGroup.add(signalQualityItem);
//
//			signalLevelItem.mParent = diseqcMCItem;
//			signalLevelItem.setmParentGroup(diseqcMCItem.mSubChildGroup);
//			//diseqcMCItem.mSubChildGroup.add(signalLevelItem);
//		}
//		signalQualityItem.setEnabled(false);
//		signalLevelItem.setEnabled(false);
		/*String longitudeTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_longitude);
		int defaultLongitude = MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE);
		final Action longitudeItem = new Action(
                MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE,
                longitudeTitle, -1800, 1800,//0~180 E  -180~0 W
                defaultLongitude, 
                new String[] {},
                MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
		longitudeItem.setDescription(MenuConfigManager.getInstance(mContext).getUSALSDescription(defaultLongitude, longitudeItem.mItemID));
        
        String latidudeTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_latitude);
        int defaultLatitude = MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE);
        final Action latidudeItem = new Action(
                MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE,
                latidudeTitle, -900, 900,//0~90 N  -90~0 S
                defaultLatitude, 
                new String[] {},
                MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
        latidudeItem.setDescription(MenuConfigManager.getInstance(mContext).getUSALSDescription(defaultLatitude, latidudeItem.mItemID));*/
        
        String orbitPositionTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_orbit_position);
        int defaultOrbit = info.getOrbPos();//savevalue.readValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION);
        savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION, defaultOrbit);
        final Action orbitPositionItem = new Action(
                MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION,
                orbitPositionTitle, -1800, 1800,//0~180 E  -180~0 W
                defaultOrbit, 
                new String[] {},
                MenuConfigManager.STEP_VALUE, Action.DataType.NUMVIEW);
        orbitPositionItem.setDescription(MenuConfigManager.getInstance(mContext).getUSALSDescription(defaultOrbit, orbitPositionItem.mItemID));
        orbitPositionItem.setEnabled(!ScanContent.isSatelliteNameExistInXml(info.getSatName()));
        
        String gotoXXTitle = context.getResources().getString(R.string.dvbs_diseqc12_movement_GotoXX);
        final Action gotoXXItem = new Action(
                MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_GOTOXX,
                gotoXXTitle, MenuConfigManager.INVALID_VALUE, MenuConfigManager.INVALID_VALUE,
                0, new String[] {},
                MenuConfigManager.STEP_VALUE, Action.DataType.DISEQC12_SAVEINFO);

		movementControlItem.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

            @Override
            public void afterOptionValseChanged(String afterName) {
                movementControlItem.mInitValue = Arrays.asList(movementControlItem.mOptionValue).indexOf(afterName);
                savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL, movementControlItem.mInitValue);
                if (diseqcMCItem != null && parentItem != null) {
                    diseqcMCItem.mOptionValue = new String[] { movementControlList[movementControlItem.mInitValue] };
                }
                if(movementControlItem.mInitValue != 3 && diseqcMCItem != null){
                    diseqcMCItem.mSubChildGroup.clear();
                    diseqcMCItem.mSubChildGroup.add(movementControlItem);
                    diseqcMCItem.mSubChildGroup.add(stepSizeItem);
                    diseqcMCItem.mSubChildGroup.add(timeoutSItem);
                    diseqcMCItem.mSubChildGroup.add(moveEastItem);
                    diseqcMCItem.mSubChildGroup.add(moveWestItem);
                    diseqcMCItem.mSubChildGroup.add(stopMovementItem);
                    diseqcMCItem.mSubChildGroup.addAll(initSignalLevelAndQualityItems(context, satID));
                }
                switch (movementControlItem.mInitValue) {
                case 0:
                    stepSizeItem.setEnabled(false);
                    timeoutSItem.setEnabled(false);
                    break;
                case 1:
                    stepSizeItem.setEnabled(true);
                    timeoutSItem.setEnabled(false);
                    break;
                case 2:
                    stepSizeItem.setEnabled(false);
                    timeoutSItem.setEnabled(true);
                    break;
                case 3:
                    diseqcMCItem.mSubChildGroup.clear();
                    diseqcMCItem.mSubChildGroup.add(movementControlItem);
                    //diseqcMCItem.mSubChildGroup.add(longitudeItem);
                    //diseqcMCItem.mSubChildGroup.add(latidudeItem);
                    diseqcMCItem.mSubChildGroup.add(orbitPositionItem);
                    diseqcMCItem.mSubChildGroup.add(gotoXXItem);
                    diseqcMCItem.mSubChildGroup.addAll(initSignalLevelAndQualityItems(context, satID));
                    break;
                default:
                    break;
                }
//              adapter.notifyDataSetChanged();
            }
        });
		
		if(diseqcMCItem != null && movementControlItem.mInitValue != 3){
            diseqcMCItem.mSubChildGroup.clear();
            diseqcMCItem.mSubChildGroup.add(movementControlItem);
            diseqcMCItem.mSubChildGroup.add(stepSizeItem);
            diseqcMCItem.mSubChildGroup.add(timeoutSItem);
            diseqcMCItem.mSubChildGroup.add(moveEastItem);
            diseqcMCItem.mSubChildGroup.add(moveWestItem);
            diseqcMCItem.mSubChildGroup.add(stopMovementItem);
        }
        switch (movementControlItem.mInitValue) {
        case 0:
            stepSizeItem.setEnabled(false);
            timeoutSItem.setEnabled(false);
            break;
        case 1:
            stepSizeItem.setEnabled(true);
            timeoutSItem.setEnabled(false);
            break;
        case 2:
            stepSizeItem.setEnabled(false);
            timeoutSItem.setEnabled(true);
            break;
        case 3:
            diseqcMCItem.mSubChildGroup.clear();
            diseqcMCItem.mSubChildGroup.add(movementControlItem);
            //diseqcMCItem.mSubChildGroup.add(longitudeItem);
            //diseqcMCItem.mSubChildGroup.add(latidudeItem);
            diseqcMCItem.mSubChildGroup.add(orbitPositionItem);
            diseqcMCItem.mSubChildGroup.add(gotoXXItem);
            break;
        default:
            break;
        }
		
		if (diseqcMCItem != null && parentItem != null) {
			for (Action tempItem:diseqcMCItem.mSubChildGroup) {
				tempItem.mLocationId = diseqcMCItem.mLocationId;
			}
			diseqcMCItem.mSubChildGroup.addAll(initSignalLevelAndQualityItems(context, satID));
			return diseqcMCItem.mSubChildGroup;
		} else {
			return items;
		}
	}

	int mRecID;

	@Override
	public void specialOptionClick(Action currAction) {
		Log.d(TAG, "specialOptionClick");
	}

	public static synchronized void setMySelfNull() {
	    mSelf = null;
    }

	public void saveLnbFreqUserDefine(String freqValue, int satId, Action parent) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveLnbFreqUserDefine "+freqValue +",satId="+satId);
	    String[] freqs = freqValue.split(",");
	    if (freqs.length == 1) {
	        parent.mSubChildGroup.get(2).setEnabled(true); //Tone22KHZ
        } else {
            parent.mSubChildGroup.get(2).setEnabled(false);
        }
        SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(mContext, satId);
        if(info == null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveLnbFreqUserDefine info == null");
            return;
        }
        switch (freqs.length) {
            case 1:
                info.setLnbLowFreq(Integer.parseInt(freqs[0]));
                info.setLnbHighFreq(0);
                info.setLnbSwitchFreq(0);
                info.setLnbType(LNB_SINGLE_FREQ);
                break;
            case 2:
                info.setLnbLowFreq(Integer.parseInt(freqs[0]));
                info.setLnbHighFreq(Integer.parseInt(freqs[1]));
                info.setLnbSwitchFreq(0);
                info.setLnbType(LNB_DUAL_FREQ);
                break;
            case 3:
                info.setLnbLowFreq(Integer.parseInt(freqs[0]));
                info.setLnbHighFreq(Integer.parseInt(freqs[1]));
                info.setLnbSwitchFreq(Integer.parseInt(freqs[2]));
                info.setLnbType(LNB_DUAL_FREQ);
                break;
            default:
                break;
        }
        ScanContent.saveDVBSSatelliteToSatl(info);
    }

	public static int checkIfExistLnbFreq(int low, int high, int swit) {
        for(int i = 0; i < LNB_CONFIG.length - 1; i++){
            if(low == LNB_CONFIG[i][0] && high == LNB_CONFIG[i][1] && swit == LNB_CONFIG[i][2]){
                return i;
            }
        }
        return -1;
    }
}
