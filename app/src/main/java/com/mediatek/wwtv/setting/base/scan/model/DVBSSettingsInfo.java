package com.mediatek.wwtv.setting.base.scan.model;

import java.util.List;

import android.content.Context;

import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;


public class DVBSSettingsInfo extends ScanParams{

	public Context context;

	public static final int NETWORK_SCAN_MODE=0;
	public static final int FULL_SCAN_MODE=1;
	public static final int TP_SCAN_MODE=2;

	
	public final static int CHANNELS_ALL=0;
	public final static int CHANNELS_ENCRYPTED=1;
	public final static int CHANNELS_FREE=2;

	public final static int CHANNELS_STORE_TYPE_ALL=0;
	public final static int CHANNELS_STORE_TYPE_DIGITAL=1;
	public final static int CHANNELS_STORE_TYPE_RADIO=2;
	
	/* DVBS_SATELLITE_TYPE */
	public int type = -1; // Preferred/General

	/* DVBS_SELECT_OPERATION */
	private int operator = -1; // ORS/Sky Deutshland/...

	/* DVBS_CONFIG_SETTING */
	public String antenaType = "";
	public String tuner = "";
	public String bandFreq = "";
	/**for save re-scan satellite list*/
	public List<SatelliteInfo> mRescanSatLocalInfoList;
	/**for save re-scan satellite TP info list*/
	public List<SatelliteTPInfo> mRescanSatLocalTPInfoList;
	/* DVBS_SATELLITE_CONFIG */
	private SatelliteInfo satelliteInfo=new SatelliteInfo();

	/* DVBS_SATELLITE_CONFIG_INFO ,For Scan */
	public int scanMode = -1;
	public int scanChannels = -1;
	public int scanStoreType = -1;

	public static int svlID = 3;

	public boolean checkBATInfo=false;
	public int BATId=-1;
	public int mduType=-1;
	public int tkgsType = -1;
	public int menuSelectedOP = -1;
	public boolean isUpdateScan;
	public boolean mIsOnlyScanOneSatellite;
	public boolean mIsDvbsNeedCleanChannelDB = true;
    public int i4BatID = -1;

	public MtkTvScanDvbsBase.SbDvbsScanType scanType = MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_AUTO_MODE; 

	public void clearDVBSSatelliteType() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("clearDVBSSatelliteType()");
		antenaType = "";
		tuner = "";
		bandFreq = "";
	}

	public void clearDVBSOperator() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("clearDVBSOperator()");
		antenaType = "";
		tuner = "";
		bandFreq = "";
	}

	public void clearDVBSConfigInfo() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("clearDVBSConfigInfo()");
		antenaType = "";
		tuner = "";
		bandFreq = "";
	}

	public void clearScanConfigInfo() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("clearScanConfigInfo()");
		scanMode = 0;
		scanChannels = 0;
	}

	public void dumpInfo() {
		String dumpInfoStr=String.format(
		"type=%3d,operator=%3d,antenaType=%s,tuner=%s,bandFreq=%s,scanMode=%3d,scanChannels=%3d,",
		type, operator, antenaType, tuner, bandFreq,  scanMode, scanChannels);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(dumpInfoStr);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(satelliteInfo.toString());
	}

	public SatelliteInfo getSatelliteInfo() {
		return satelliteInfo;
	}

	public void setSatelliteInfo(SatelliteInfo satelliteInfo) {
		this.satelliteInfo = satelliteInfo;
	}
}
