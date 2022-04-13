package com.mediatek.wwtv.setting.base.scan.model;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanPalSecamBase;
import com.mediatek.twoworlds.tv.MtkTvScanPalSecamBase.ScanPalSecamFreqRange;
import com.mediatek.twoworlds.tv.MtkTvScanPalSecamBase.ScanPalSecamRet;


public class EUATVScanner implements IScanner{
	private ScannerManager mScannerManager;
	public static final int FREQ_HIGH_VALUE = 865;
	public static final int FREQ_LOW_VALUE = 47;

	public EUATVScanner(ScannerManager scannerManager) {
		// TODO Auto-generated constructor stub
		mScannerManager=scannerManager;

	}

	@Override
	public void fullScan() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("fullScan()");
	}

	@Override
	public void fullDTVScan() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("fullDTVScan()");
	}

	@Override
	public void fullATVScan() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("fullATVScan()");
		ScanPalSecamRet rect=MtkTvScan.getInstance().getScanPalSecamInstance().startAutoScan();
		checkStartScanResult(rect);
	}

	@Override
	public void updateScan() {
		// TODO Auto-generated method stub
		ScanPalSecamRet rect=MtkTvScan.getInstance().getScanPalSecamInstance().startUpdateScan();
		checkStartScanResult(rect);
	}

	private void checkStartScanResult(ScanPalSecamRet rect) {
		if(rect==ScanPalSecamRet.SCAN_PAL_SECAM_RET_INTERNAL_ERROR){
			mScannerManager.onScanError();
		}
	}

	@Override
	public void singleRFScan() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("singleRFScan()");
	}

	@Override
	public void cancelScan() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("cancelScan()hhhhhhhhhhhh");
		// TODO Auto-generated method stub
		MtkTvScan.getInstance().getScanPalSecamInstance().cancelScan();
	}

	@Override
	public boolean isScanning() {
		// TODO Auto-generated method stub
		return MtkTvScan.getInstance().isScanning();
	}

	@Override
	public void scanUp(int frequency) {
		frequency=(int) (frequency*1E6);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("scanUp().frequency:"+frequency);

		MtkTvScanPalSecamBase.ScanPalSecamFreqRange range = new MtkTvScanPalSecamBase().new ScanPalSecamFreqRange();
		MtkTvScan.getInstance().getScanPalSecamInstance()
				.getFreqRange(range);
		ScanPalSecamRet rect=MtkTvScan.getInstance().getScanPalSecamInstance().startRangeScan(frequency, Integer.MAX_VALUE);
		checkStartScanResult(rect);
	}

	@Override
	public void scanDown(int frequency) {
		frequency=(int) (frequency*1E6);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("scanDown().frequency:"+frequency);

		MtkTvScanPalSecamBase.ScanPalSecamFreqRange range = new MtkTvScanPalSecamBase().new ScanPalSecamFreqRange();
		MtkTvScan.getInstance().getScanPalSecamInstance()
				.getFreqRange(range);
		int minFreq = range.lower_freq;

		ScanPalSecamRet rect=MtkTvScan.getInstance().getScanPalSecamInstance().startRangeScan(frequency, minFreq);

		checkStartScanResult(rect);
	}

	@Override
	public void rangeATVFreqScan(int startFreq, int endFreq) {
		startFreq = (int)(startFreq*1E6);
		endFreq = (int)(endFreq*1E6);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("rangeATVFreqScan().frequency:"+startFreq + ">>" + endFreq);

		MtkTvScanPalSecamBase.ScanPalSecamFreqRange range = new MtkTvScanPalSecamBase().new ScanPalSecamFreqRange();
		MtkTvScan.getInstance().getScanPalSecamInstance()
				.getFreqRange(range);
		ScanPalSecamRet rect=MtkTvScan.getInstance().getScanPalSecamInstance().startRangeScan(startFreq, endFreq);

		checkStartScanResult(rect);
	}

}
