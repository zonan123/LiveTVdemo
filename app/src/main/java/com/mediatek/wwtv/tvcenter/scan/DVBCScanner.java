
package com.mediatek.wwtv.tvcenter.scan;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase.MtkTvScanDvbcParameter;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams.DvbcScanMode;

import com.mediatek.wwtv.setting.base.scan.model.ScanParams;


public class DVBCScanner implements IScanner {
    private MtkTvScanDvbcBase mtkTvScanDvbcBase;
    private final static String TAG = "DVBCScanner";
//    private MtkTvScanBase mScan;
    private MtkTvScanDvbcParameter mDvbcScanPara;
    private ScanParams mParams;

    public static int selectedRFChannelFreq=0;
    
    public DVBCScanner(ScanParams params) {
        super();
        MtkTvScanBase mScan = MtkTvScan.getInstance();
        mtkTvScanDvbcBase = mScan.getScanDvbcInstance();
        mDvbcScanPara = mtkTvScanDvbcBase.new MtkTvScanDvbcParameter();
        mParams = params;

        if (mParams != null && mParams.freq > 0) {
            mParams.freq = mParams.freq * 1000;
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
                mDvbcScanPara
                        .setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_EX_QUICK);
                mDvbcScanPara.setCfgFlag(0);
                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
                mtkTvScanDvbcBase.startAutoScan();
                break;
            case QUICK:
                // Quick
                mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_QUICK);
                mDvbcScanPara.setNetWorkID(mParams.networkID); 
                mDvbcScanPara.setStartFreq(mParams.freq);
                mDvbcScanPara.setEndFreq(-1);
                mDvbcScanPara.setCfgFlag(0);
                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
                mtkTvScanDvbcBase.startAutoScan();
                break;
            case FULL:
            default:
                // Full Scan
                mDvbcScanPara.setNitMode(MtkTvScanDvbcBase.ScanDvbcNitMode.DVBC_NIT_SEARCH_MODE_OFF);
                mDvbcScanPara.setNetWorkID(-1);
                mDvbcScanPara.setStartFreq(mParams.freq);
                mDvbcScanPara.setEndFreq(-1);
                mDvbcScanPara.setCfgFlag(0x0000);//for full scan DTV02178492 modify from 0x2000 to 0x0000
                mtkTvScanDvbcBase.setDvbcScanParas(mDvbcScanPara);
                mtkTvScanDvbcBase.startAutoScan();
                break;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format("Freq:%d,NetworkID:%d,Type:%s", mParams.freq,
                mParams.networkID, mParams.dvbcScanMode.name()) + ">>>" + mDvbcScanPara.getCfgFlag());
    }

    @Override
    public void fullDTVScan() {
        // TODO Auto-generated method stub

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
//        if (mScan != null && singleScanPara != null) {
//            singleScanPara.setScanStartFreq(mParams.freq);
//            singleScanPara.setScanEndFreq(mParams.freq);
//            singleScanPara.setScanUp(true);
//            mScan.setDvbcManualScanParas(singleScanPara);
//            mScan.startScan(ScanType.SCAN_TYPE_DVBC, ScanMode.SCAN_MODE_MANUAL_FREQ, false);
//            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, String.format("Freq:%d,NetworkID:%d,Type:%s", mParams.freq,
//                    mParams.networkID, mParams.dvbcScanMode.name()));
//        }
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

}
