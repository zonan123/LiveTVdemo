package com.mediatek.wwtv.setting.base.scan.model;

public interface IScanner {

	void fullScan();

	void fullDTVScan();

	void fullATVScan();

	void updateScan();

	void singleRFScan();

	void cancelScan();
	
	void scanUp(int frequency);
	void scanDown(int frequency);
	void rangeATVFreqScan(int startFreq, int endFreq);
	boolean isScanning();
}
