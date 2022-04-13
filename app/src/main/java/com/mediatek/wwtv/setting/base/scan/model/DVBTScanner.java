
package com.mediatek.wwtv.setting.base.scan.model;

import android.content.Context;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.RfDirection;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;

public class DVBTScanner implements IScanner {
    private MtkTvScanDvbtBase mtkTvScanDvbtBase;

    private String TAG = "DVBTScanner";

    public final static int RF_CHANNEL_CURRENT = 0;
    public final static int RF_CHANNEL_PREVIOUS = 1;
    public final static int RF_CHANNEL_NEXT = 2;

    public static int selectedRFChannelFreq = 0;

    public DVBTScanner() {
        super();
        dvbtScanInit();
    }

    private void dvbtScanInit() {
        this.mtkTvScanDvbtBase = MtkTvScan.getInstance().getScanDvbtInstance();
    }

    private void dvbtFullScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtFullScan()");
        this.mtkTvScanDvbtBase.startAutoScan();
    }
	
	private void dvbtFullScan(boolean setIpInstall) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbtFullScan(setIpInstall) "+setIpInstall);
        MtkTvScanDvbtBase.ScanDvbtRet dvbtRet = this.mtkTvScanDvbtBase
                .startAutoScan(setIpInstall);
        if (MtkTvScanDvbtBase.ScanDvbtRet.SCAN_DVBT_RET_OK == dvbtRet) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"SCAN_DVBT_RET_OK");
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"SCAN_DVBT_RET not OK");
        }
    }

    private void dvbtFullScan(boolean setIpInstall, boolean isAdviseScan) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvbtFullScan(setIpInstall,isAdviseScan) "+setIpInstall+","+isAdviseScan);
        MtkTvScanDvbtBase.ScanDvbtRet dvbtRet = this.mtkTvScanDvbtBase
                .startAutoScan(setIpInstall, isAdviseScan);
        if (MtkTvScanDvbtBase.ScanDvbtRet.SCAN_DVBT_RET_OK == dvbtRet) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"SCAN_DVBT_RET_OK");
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"SCAN_DVBT_RET not OK");
        }
    }

    private void dvbtUpdateScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtUpdateScan()");
       this.mtkTvScanDvbtBase.startUpdateScan();
    }

    public boolean isUKCountry() {
        MtkTvConfig mTvConfig = MtkTvConfig.getInstance();
        String country = mTvConfig.getCountry();
        if (country
                .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_GBR)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isUKCountry");
            return true;
        }
        return false;
    }

    private void dvbtRfScan() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtRfScan()");
        // MtkTvScanDvbtBase.RfInfo rfInfo = this.mtkTvScanDvbtBase
        // .gotoDestinationRf(RfDirection.CURRENT);
        // if (null != rfInfo) {
        // // UI todo
        // }
        //
        // rfInfo = this.mtkTvScanDvbtBase.gotoDestinationRf(RfDirection.PREVIOUS);
        // if (null != rfInfo) {
        // // UI todo
        // }
        //
        // rfInfo = this.mtkTvScanDvbtBase.gotoDestinationRf(RfDirection.NEXT);
        // if (null != rfInfo) {
        // // UI todo
        // }

        MtkTvScanDvbtBase.ScanDvbtRet dvbtRet = this.mtkTvScanDvbtBase
                .startRfScan();
        if (MtkTvScanDvbtBase.ScanDvbtRet.SCAN_DVBT_RET_OK == dvbtRet) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtRfScan(),SCAN_DVBT_RET_OK");
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtRfScan(),SCAN_DVBT_RET_FAIL");
        }
    }

    private void dvbtCancelScant() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCancelScant()");
        MtkTvScanDvbtBase.ScanDvbtRet dvbtRet = this.mtkTvScanDvbtBase
                .cancelScan();
        if (MtkTvScanDvbtBase.ScanDvbtRet.SCAN_DVBT_RET_OK == dvbtRet) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCancelScant(),OK");
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCancelScant(),fail");
        }
    }

    public static String getRFChannel(int type, Context context) {
        MtkTvScanDvbtBase.RfInfo rfInfo = new MtkTvScanDvbtBase().new RfInfo();

        //selectedRFChannelFreq = 0;

        switch (type) {
            case RF_CHANNEL_CURRENT:
                rfInfo = MtkTvScan.getInstance().getScanDvbtInstance()
                        .gotoDestinationRf(RfDirection.CURRENT);
                break;
            case RF_CHANNEL_PREVIOUS:
                rfInfo = MtkTvScan.getInstance().getScanDvbtInstance()
                        .gotoDestinationRf(RfDirection.PREVIOUS);
                break;
            case RF_CHANNEL_NEXT:
                rfInfo = MtkTvScan.getInstance().getScanDvbtInstance()
                        .gotoDestinationRf(RfDirection.NEXT);
                break;
            default:
                break;
        }
        selectedRFChannelFreq = rfInfo.rfFrequence;
        return context.getResources().getString(R.string.dvbt_single_rf_frequency,rfInfo.rfChannelName,selectedRFChannelFreq/1000);
    }
    
    public void bgmScan(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("bgmScan()");
        MtkTvScanDvbtBase.ScanDvbtRet dvbtRet = this.mtkTvScanDvbtBase
                .startBGMScan();
        if (MtkTvScanDvbtBase.ScanDvbtRet.SCAN_DVBT_RET_OK == dvbtRet) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCancelScant(),OK");
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbtCancelScant(),fail");
        }
    }

    @Override
    public void cancelScan() {
        // TODO Auto-generated method stub
        dvbtCancelScant();
    }

    @Override
    public void fullScan() {
        // TODO Auto-generated method stub
        dvbtFullScan();
    }
    
    public void fullScan(boolean setIpInstall) {
        dvbtFullScan(setIpInstall);
    }

    public void fullScan(boolean setIpInstall, boolean isAdviseScan) {
        dvbtFullScan(setIpInstall, isAdviseScan);
    }

    @Override
    public void fullDTVScan() {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fullDTVScan nothing");
    }

    @Override
    public void fullATVScan() {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fullATVScan nothing");
    }

    @Override
    public void updateScan() {
        // TODO Auto-generated method stub
        dvbtUpdateScan();
    }

    @Override
    public void singleRFScan() {
        // TODO Auto-generated method stub
        dvbtRfScan();
    }

    @Override
    public boolean isScanning() {
        // TODO Auto-generated method stub
        return MtkTvScan.getInstance().isScanning();
    }

    @Override
    public void scanUp(int frequency) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanUp nothing");
    }

    @Override
    public void scanDown(int frequency) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanDown nothing");
    }

    @Override
    public void rangeATVFreqScan(int startFreq, int endFreq) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rangeATVFreqScan nothing");
    }
}
