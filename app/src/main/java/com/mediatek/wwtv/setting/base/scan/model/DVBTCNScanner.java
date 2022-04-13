package com.mediatek.wwtv.setting.base.scan.model;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDtmbBase;


public class DVBTCNScanner implements IScanner {
	private static DVBTCNScanner instance;
	private static MtkTvScanDtmbBase mtkTvScanDtmbBase;
	private ScannerManager mScannerManager;

	public final static int RF_CHANNEL_CURRENT = 0;
	public final static int RF_CHANNEL_PREVIOUS = 1;
	public final static int RF_CHANNEL_NEXT = 2;

	public static int selectedRFChannelFreq=0;

	public synchronized static DVBTCNScanner getScanInstance(ScannerManager scannerManager) {
		if (instance == null) {
			instance = new DVBTCNScanner(scannerManager);
		}
		return instance;
	}
	
	public synchronized static DVBTCNScanner getScanInstance() {
        if (instance == null) {
            instance = new DVBTCNScanner();
        }
        return instance;
    }

	public DVBTCNScanner(ScannerManager scannerManager) {
		super();
		mScannerManager = scannerManager;
		dvbtScanInit();
	}
	
	public DVBTCNScanner() {
        super();
        dvbtScanInit();
    }

	private void dvbtScanInit() {
		this.mtkTvScanDtmbBase = new MtkTvScanDtmbBase();
	}

	private void dvbtCNFullScan() {
		int ret = mtkTvScanDtmbBase.startAutoScan();
		if(ret == MtkTvScanDtmbBase.DTMB_RET_INTERNAL_ERR && mScannerManager != null){
		    mScannerManager.onScanOK();
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCNFullScan()>> onScanOK");
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCNFullScan()>>" + ret);
	}

	private void dvbtCNRfScan() {
		int ret = mtkTvScanDtmbBase.startRfScan();
		if(ret == MtkTvScanDtmbBase.DTMB_RET_INTERNAL_ERR && mScannerManager != null){
            mScannerManager.onScanOK();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCNRfScan()>> onScanOK");
        }
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCNRfScan()>>" + ret);
	}

	private void dvbtCNCancelScant() {
		int ret = this.mtkTvScanDtmbBase.cancelScan();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCNCancelScant()>>>" + ret);
	}

	public String getCNRFChannel(int type) {
		MtkTvScanDtmbBase.RfInfo rfInfo = mtkTvScanDtmbBase.new RfInfo();

		selectedRFChannelFreq=0;

		switch (type) {
		    case RF_CHANNEL_CURRENT:
		        rfInfo = mtkTvScanDtmbBase.gotoDestinationRf(MtkTvScanDtmbBase.RF_CURR);
		        break;
		    case RF_CHANNEL_PREVIOUS:
		        rfInfo = mtkTvScanDtmbBase.gotoDestinationRf(MtkTvScanDtmbBase.RF_PREV);
		        break;
		    case RF_CHANNEL_NEXT:
		        rfInfo = mtkTvScanDtmbBase.gotoDestinationRf(MtkTvScanDtmbBase.RF_NEXT);
		        break;
		    default:
		        break;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getdvbtCNRFChannel()>>>" + type + ">>>>" + rfInfo);
		if (rfInfo != null) {
			selectedRFChannelFreq=rfInfo.rfFreq;
			return rfInfo.rfChannelName;
		}
		return "";
	}

	@Override
	public void cancelScan() {
		dvbtCNCancelScant();
	}

	@Override
	public void fullScan() {
		dvbtCNFullScan();
	}

	@Override
	public void fullDTVScan() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("fullDTVScan()");
	}

	@Override
	public void fullATVScan() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("fullATVScan()");
	}

	@Override
	public void updateScan() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateScan()");
	}

	@Override
	public void singleRFScan() {
		dvbtCNRfScan();
	}

	@Override
	public boolean isScanning() {
		return MtkTvScan.getInstance().isScanning();
	}

	@Override
	public void rangeATVFreqScan(int startFreq, int endFreq) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("rangeATVFreqScan()");
	}

	@Override
	public void scanUp(int frequency) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("scanUp()");
	}

	@Override
	public void scanDown(int frequency) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("scanDown()");
	}
}
