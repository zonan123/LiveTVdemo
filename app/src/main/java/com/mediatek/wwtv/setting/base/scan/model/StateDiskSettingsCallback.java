package com.mediatek.wwtv.setting.base.scan.model;

public interface StateDiskSettingsCallback {

	boolean showSetPVR();
	boolean showSetTSHIFT();
	boolean showFormatDisk();
	boolean showSpeedTest();
	boolean cancelFormat();
	boolean cancelSpeedTest();
}
