package com.mediatek.wwtv.setting.base.scan.model;

import java.util.HashMap;
import java.util.Map;

import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvSbDvbsNetworkScanInfo;
import android.util.Log;

public class ScanParams {
	private String tag="ScanParams";
	
	public int singleRFChannel=0;
//	public int eu_analog_frequency = -1;

	public int freq = -1; // -1:Auto Others:Manual value
	public int startfreq = -1; // -1:Auto Others:Manual value
	public int endfreq = -1; // -1:Auto Others:Manual value
	public int networkID = -1; // -1:Auto Others:Manual value
	public DvbcScanMode dvbcScanMode = DvbcScanMode.FULL; // -1:Auto Others:Manual value
	public DvbcScanType dvbcScanType = DvbcScanType.DIGITAL; // -1:Auto Others:Manual value
	
	/*
	 * default scan mode & scan type for dvbs
	 */
	public int dvbsScanMode = DVBSSettingsInfo.FULL_SCAN_MODE; 
	public int dvbsScanType = DVBSSettingsInfo.CHANNELS_ALL; 
	public int dvbsStoreType = DVBSSettingsInfo.CHANNELS_ALL;
	public Map<Integer,MtkTvSbDvbsNetworkScanInfo>  networkInfo=new HashMap<Integer,MtkTvSbDvbsNetworkScanInfo>();
	
	public enum DvbcScanMode {
		ADVANCE, QUICK, FULL
	}
	
	public enum DvbcScanType {
        DIGITAL, ALL
    }


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		Log.d(tag, String.format("singleRFChannel: %d", singleRFChannel));
//		Log.d(tag, String.format("eu_analog_frequency: %d", eu_analog_frequency));
		Log.d(tag, String.format("mFreq: %d", freq));
		Log.d(tag, String.format("mNetWorkId: %d", networkID));
		Log.d(tag, String.format("mMode: %s", dvbcScanMode.name()));
		
		return super.toString();
	}
}
