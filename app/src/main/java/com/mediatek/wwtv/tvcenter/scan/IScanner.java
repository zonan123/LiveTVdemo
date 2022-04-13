package com.mediatek.wwtv.tvcenter.scan;

public interface IScanner {

	void fullScan();

	void fullDTVScan();

	void fullATVScan();

	void updateScan();

	void singleRFScan();

	void cancelScan();
	
	void scanUp(int frequency);
	void scanDown(int frequency);
	boolean isScanning();
}
