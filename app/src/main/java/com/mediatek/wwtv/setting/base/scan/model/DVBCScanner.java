
package com.mediatek.wwtv.setting.base.scan.model;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase.MtkTvScanDvbcParameter;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams.DvbcScanMode;


public class DVBCScanner implements IScanner {
    private MtkTvScanDvbcBase mtkTvScanDvbcBase;
    private static final String TAG = "DVBCScanner";
    
    private MtkTvScanDvbcParameter mDvbcScanPara;
    private ScanParams mParams;

    public static int selectedRFChannelFreq=0;

    public DVBCScanner(ScanParams params) {
        super();
         
        mtkTvScanDvbcBase = MtkTvScan.getInstance().getScanDvbcInstance();
        mDvbcScanPara = mtkTvScanDvbcBase.new MtkTvScanDvbcParameter();
        mParams = params;

		if (mParams != null && mParams.freq > 0) {
			mParams.freq = mParams.freq * 1000;
		}else if(mParams != null && mParams.freq == 0){
            mParams.freq = 1000;
            //set 0 as a illegal value to make ui show scan err when input 0 as freq
        }
	}

	@Override
	public void fullScan() {
		// TODO Auto-generated method stub

		if (mParams == null) {
			mParams = new ScanParams();
			mParams.dvbcScanMode = DvbcScanMode.FULL;
		}

		switch (mParams.dvbcScanMode) {
            case ADVANCE:
                // Advance
                mDvbcScanPara.setStartFreq(mParams.freq);
                mDvbcScanPara.setEndFreq(mParams.freq);
                mDvbcScanPara.setNetWorkID(mParams.networkID);
                mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_EX_QUICK);
                mDvbcScanPara.setCfgFlag(0);
                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
                mtkTvScanDvbcBase.startAutoScan();
                break;
            case QUICK:
                // Quick
                mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_QUICK);
                mDvbcScanPara.setNetWorkID(mParams.networkID); 
                mDvbcScanPara.setStartFreq(mParams.freq);
                mDvbcScanPara.setEndFreq(ScanContent.isTvoeEkaOp() ? mParams.freq : -1);//DTV03053114
                mDvbcScanPara.setCfgFlag(0);
                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
                mtkTvScanDvbcBase.startAutoScan();
                break;
            case FULL:
            default:
                // Full Scan
                mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_OFF);
                mDvbcScanPara.setNetWorkID(-1);
                mDvbcScanPara.setStartFreq(-1);
                mDvbcScanPara.setEndFreq(-1);
                mDvbcScanPara.setCfgFlag(0);////for full scan DTV02178492 modify from 0x2000 to 0
                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
                mtkTvScanDvbcBase.startAutoScan();
                break;
        }
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format("Freq:%d,NetworkID:%d,Type:%s", mParams.freq,
                mParams.networkID, mParams.dvbcScanMode.name()) + ">>>" + mDvbcScanPara.getCfgFlag());
    }

    @Override
    public void fullDTVScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBC fullDTVScan");
    	if (mParams == null) {
			mParams = new ScanParams();
			mParams.dvbcScanMode = DvbcScanMode.FULL;
		}
    	mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_OFF);
        mDvbcScanPara.setNetWorkID(-1);
        mDvbcScanPara.setStartFreq(-1);
        mDvbcScanPara.setEndFreq(-1);
        mDvbcScanPara.setCfgFlag(0x2000);//for full scan
        mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
        mtkTvScanDvbcBase.startAutoScan();
    }

    @Override
    public void fullATVScan() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateScan() {
        mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_OFF);
        mDvbcScanPara.setNetWorkID(mParams.networkID); 
        mDvbcScanPara.setStartFreq(mParams.freq);
        mDvbcScanPara.setEndFreq(mParams.freq);
        mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
        mtkTvScanDvbcBase.startUpdateScan();
    }

    @Override
    public void singleRFScan() {
        if (mParams == null) {
            mParams = new ScanParams();
            mParams.dvbcScanMode = DvbcScanMode.FULL;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format("singleRFScan Freq:%d,NetworkID:%d,Type:%s", mParams.freq,
                mParams.networkID, mParams.dvbcScanMode.name()));
        mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_OFF);
        mDvbcScanPara.setNetWorkID(mParams.networkID);
        mDvbcScanPara.setStartFreq(mParams.freq);
        mDvbcScanPara.setEndFreq(mParams.freq);
        mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
        mtkTvScanDvbcBase.startRfScan();
    }

    @Override
    public void cancelScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelScan");
        mtkTvScanDvbcBase.cancelScan();
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
        return MtkTvScan.getInstance().isScanning();
    }

	@Override
	public void rangeATVFreqScan(int startFreq, int endFreq) {
		// TODO Auto-generated method stub
		
	}

}
