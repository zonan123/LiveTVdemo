
package com.mediatek.wwtv.setting.base.scan.model;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanCeBase;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams.DvbcScanMode;


public class DVBCCNScanner implements IScanner {
	private static final String TAG = "DVBCCNScanner";
	private static DVBCCNScanner instance;
    private MtkTvScanCeBase mtkTvScanCeBase;
    public static int mLowerFreq = 48000000;
    public static int mHighFreq = 858000000;
    public static boolean mQuichScanSwitch = false;

    private ScanParams mParams;

    public static int selectedRFChannelFreq=0;
    
    public synchronized static DVBCCNScanner getScanInstance(ScanParams params) {
		if (instance == null) {
			instance = new DVBCCNScanner(params);
		}
		return instance;
	}
    
    public DVBCCNScanner(ScanParams params) {
        super();
        mtkTvScanCeBase = new MtkTvScanCeBase();
        mParams = params;

		if (mParams != null && mParams.freq > 0) {
			mParams.freq = mParams.freq * 1000000;
		}
	}
    
    public ScanParams getScanParams() {
    	return mParams;
    }
    
    public void setScanParams(ScanParams parms) {
    	this.mParams = parms;
    }

	@Override
	public void fullScan() {
		if (mParams == null) {
			mParams = new ScanParams();
			mParams.dvbcScanMode = DvbcScanMode.FULL;
		}
		int ret = -2;
		switch (mParams.dvbcScanMode) {
            case ADVANCE:
                // Advance
//                mDvbcScanPara.setStartFreq(mParams.freq);
//                mDvbcScanPara.setEndFreq(mParams.freq);
//                mDvbcScanPara.setNetWorkID(mParams.networkID);
//                mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_EX_QUICK);
//                mDvbcScanPara.setCfgFlag(0);
//                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
//                ret = mtkTvScanDvbcBase.startAutoScan();
                break;
            case QUICK:
                // Quick
            	mtkTvScanCeBase.setNitMode(MtkTvScanCeBase.NIT_SEARCH_MODE_QUICK);
            	mtkTvScanCeBase.setNetworkID(0); 
            	mtkTvScanCeBase.setStartFreq(mParams.freq);
            	mtkTvScanCeBase.setEndFreq(mParams.freq);
            	mtkTvScanCeBase.setCfgFlag(0);
            	ret = mtkTvScanCeBase.startQuickScan(mParams.freq);
                break;
            case FULL:
            default:
                // Full Scan
            	mtkTvScanCeBase.setNitMode(MtkTvScanCeBase.NIT_SEARCH_MODE_OFF);
            	mtkTvScanCeBase.setNetworkID(0);
            	mtkTvScanCeBase.setStartFreq(mLowerFreq);
            	mtkTvScanCeBase.setEndFreq(mHighFreq);
            	mtkTvScanCeBase.setCfgFlag(0x2000);//for full scan
            	ret = mtkTvScanCeBase.startAutoScan();
                break;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format("Freq:%d,NetworkID:%d,Type:%s", mParams.freq,
                mParams.networkID, mParams.dvbcScanMode.name()) + ">>>" + ret);
    }

    @Override
    public void fullDTVScan() {
    	if (mParams == null) {
			mParams = new ScanParams();
			mParams.dvbcScanMode = DvbcScanMode.FULL;
		}
    	mtkTvScanCeBase.setNitMode(MtkTvScanCeBase.NIT_SEARCH_MODE_OFF);
    	mtkTvScanCeBase.setNetworkID(mParams.networkID);
    	mtkTvScanCeBase.setStartFreq(mLowerFreq);
    	mtkTvScanCeBase.setEndFreq(mHighFreq);
    	mtkTvScanCeBase.setCfgFlag(0x2000);//for full scan
    	int ret = mtkTvScanCeBase.startAutoScan();
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBC CN fullDTVScan>" + ret);
    }

    @Override
    public void fullATVScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fullATVScan");
    }

    @Override
    public void updateScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateScan");
    }

    @Override
    public void singleRFScan() {
        if (mParams == null) {
            mParams = new ScanParams();
            mParams.dvbcScanMode = DvbcScanMode.FULL;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format("singleRFScan Freq:%d,NetworkID:%d,Type:%s", mParams.freq,
                mParams.networkID, mParams.dvbcScanMode.name()));
        mtkTvScanCeBase.setNitMode(MtkTvScanCeBase.NIT_SEARCH_MODE_OFF);
        mtkTvScanCeBase.setNetworkID(mParams.networkID);
        mtkTvScanCeBase.setStartFreq(mParams.freq);
        mtkTvScanCeBase.setEndFreq(mParams.freq);
        mtkTvScanCeBase.startRfScan(mParams.freq);
    }

    @Override
    public void cancelScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan");
        mtkTvScanCeBase.cancelScan();
    }

    @Override
	public void rangeATVFreqScan(int startFreq, int endFreq) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rangeATVFreqScan");
	}

	@Override
    public void scanUp(int frequency) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanUp");
    }

    @Override
    public void scanDown(int frequency) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanDown");
    }

    @Override
    public boolean isScanning() {
        return MtkTvScan.getInstance().isScanning();
    }

}
