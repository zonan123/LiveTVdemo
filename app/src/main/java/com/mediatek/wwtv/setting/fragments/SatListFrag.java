
package com.mediatek.wwtv.setting.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter.SatItem;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteTPInfo;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.SatActivity;

import com.mediatek.twoworlds.tv.MtkTvConfig;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SatListFrag extends Fragment {
    String TAG = "SatListFrag";
    private Action mAction;
    List<SatelliteInfo> mSatellites;
    int selectPosForOP;
    String mCamOpName;
    // this property is used for when Frag is backed ans select the last select item
    int remeberPos;
    private Context mContext;
    private ScrollAdapterView mListView;
    private TVContent mTvContent;
    //private MenuConfigManager mConfigManager;
    MenuDataHelper mHelper;
    SatListAdapter mAdapter;
    boolean mNeedDisableItem = false;
    boolean isFargResumed;

    public void setAction(Action action) {
        mAction = action;
    }

    public void setSelectPos(int pos) {
        selectPosForOP = pos;
    }

    public void setCamOpName(String camOpName) {
        mCamOpName = camOpName;
    }

    public void remeberPos(int pos) {
        remeberPos = pos;
    }

    public void setSatellites(List<SatelliteInfo> satellites) {
        mSatellites = satellites;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate........");
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup mRootView;
        //TextView mAntennaTypeTip;
        mRootView = (ViewGroup) inflater.inflate(R.layout.menu_sat_list, null);
        mListView = (ScrollAdapterView) mRootView.findViewById(R.id.list);
        //mAntennaTypeTip = (TextView) mRootView.findViewById(R.id.antenna_type_tip);
        int antennaType = TVContent.getInstance(mContext).getConfigValue(ScanContent.SAT_ANTENNA_TYPE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "antennaType:"+antennaType);
        /*if(antennaType != 0){
            mAntennaTypeTip.setVisibility(View.VISIBLE);
        }else {
            mAntennaTypeTip.setVisibility(View.INVISIBLE);
        }*/
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreateView.......isFargResumed:" + isFargResumed);
        bindData();
        if(mNeedCheckM7Scan && mTvContent.isM7ScanMode()){
            ((SatActivity)mContext).showM7LNBScanConfirmDialog();
            mNeedCheckM7Scan = false;
        }
        return mRootView;
    }

    /** for save DVBS OP */
    private int mDVBSCurrentOP = -1;
    /** for save re-scan satellite list */
    private List<SatelliteInfo> mRescanSatLocalInfoList;
    /** for save re-scan satellite TP info list */
    private List<SatelliteTPInfo> mRescanSatLocalTPInfoList;
    private int mAddSatId = -1;
    //private int mNextSatId = 0;
    List<SatItem> list = null;
    private boolean mNeedCheckM7Scan; 

    private void bindData() {
        mTvContent = TVContent.getInstance(getActivity());
        //mConfigManager = MenuConfigManager.getInstance(getActivity());
        MenuDataHelper.setMySelfNull();
        mHelper = MenuDataHelper.getInstance(getActivity());
        //List<SatItem> list = null;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "itemID:" + mAction.mItemID);
        /*if (isFargResumed) {
            if (mAdapter != null) {
                int normalPos = mAdapter.getSelectItemNum() - 1;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setsotth normal:" + (normalPos));
                if (mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_ADD)) {// add
                    remeberPos(0);
                } else {
                    remeberPos(normalPos);
                }
            }
        }*/
        if (mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_ADD)) {// add
            mSatellites = ScanContent.getDVBSsatellites(mContext);
            mDVBSCurrentOP = -1;
            if (isFargResumed) {
                SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(mContext, mAddSatId);
                list = mHelper.buildDVBSInfoItem(mAction, info);
            } else {
                list = mHelper.buildDVBSSATDetailInfo(mAction, mSatellites, 2);
                // add only show the fist off sat
                if (!list.isEmpty()) {
                    list = list.subList(0, 1);
                    mAddSatId = list.get(0).satID;
                }
            }
        } else if (mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)
                || mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING)) {
            mSatellites = ScanContent.getDVBSsatellites(mContext);
            mDVBSCurrentOP = -1;
            list = mHelper.buildDVBSSATDetailInfo(mAction, mSatellites, 1);
        } else if(mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_OP_CAM_SCAN)){
            mHelper.setIsCamScanUI(true);
            ScanContent.setSelectedCamScanOP(mCamOpName.equals(mContext.getString(R.string.menu_ci_cam_scan)) ?
                    "NULL" : mCamOpName);
            mSatellites = ScanContent.getDVBSsatellites(mContext);
            list = mHelper.buildDVBSSATDetailInfo(mAction, mSatellites, 0);
        }else {// re-scan or operator
            if (!isFargResumed) {
                if (ScanContent.isPreferedSat()) {// before scan, not set prefer sat OP
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "backUpDVBSsatellites........");
                    TVContent.backUpDVBSsatellites();//only prefer need backup
                    mRescanSatLocalInfoList = ScanContent.getDVBSsatellites(mContext);
                    mRescanSatLocalTPInfoList = ScanContent
                            .getDVBSTransponderList(mRescanSatLocalInfoList);
                    if (mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_OP)) {
                        //int currentOP = ScanContent.getDVBSCurrentOP();
                        mDVBSCurrentOP = ScanContent.setSelectedSatelliteOPFromMenu(mContext,
                                selectPosForOP);
                    }
                    mNeedDisableItem = true;
                } else {// is general sat, so set general sat OP to 0
                    mDVBSCurrentOP = -1;
                    ScanContent.setSelectedSatelliteOPFromMenu(mContext, 0);
                }
            }

            mSatellites = ScanContent.getDVBSsatellites(mContext);
            list = mHelper.buildDVBSSATDetailInfo(mAction, mSatellites, 0);
        }
        ScanContent.setDVBSCurroperator(mDVBSCurrentOP);
        int nextSatId = 0;
        if(!list.isEmpty()){
            nextSatId = list.get(0).satid;
        }
        if(!mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING) &&
                !mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_ADD)){
            SatItem nextAction = new SatItem(nextSatId, mAction, getString(R.string.setup_next));
            nextAction.setEnabled(ScanContent.getDVBSEnablesatellites(mContext,mSatellites).size() > 0);
            list.add(0, nextAction);
        }
        mAdapter = new SatListAdapter(mContext, list);
        mAdapter.setNeedDisableWhenisOff(mNeedDisableItem);
        checkCanScan();
        mAdapter.setListener((SatActivity) (getActivity()));
        if(MenuConfigManager.DVBS_SAT_SCAN_DOWNLOAD.equals(mAction.mItemID)){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSdxFileName "+mAction.getTitle());
            mAdapter.setSdxFileName(mAction.getTitle());
            mAdapter.setCanScan(checkSdxSelectSatellite(mSatellites));
        }
        mListView.setAdapter(mAdapter);
        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFargResumed) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setsotth for:" + mListView.getChildCount() + ",pos=="
                            + remeberPos);
                    if (mListView.getChildCount() > remeberPos) {
                        if (remeberPos < 0) {
                            remeberPos = 0;
                        }
                        boolean req = mListView.getChildAt(remeberPos).requestFocus();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setsotth:" + req + "," + remeberPos);
                    }

                } else {
                    if (mListView.getChildCount() > 0) {
                        boolean req = mListView.getChildAt(0).requestFocus();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setsotth00:" + req);
                    }

                }

            }
        }, 500);*/

    }

    private void checkCanScan() {
        boolean samePort = checkIfSamePortOfLnb(mSatellites);
        boolean diseqcConflict = checkIfDiseqcConfict(mSatellites);
        String sameName = null;
        if(ScanContent.isSatOpFastScan()){
            sameName = checkSameOrbitForFastScan(mSatellites);
        }

        if(samePort){
            mAdapter.setCanScanTip(mContext.getString(R.string.dvbs_same_port_lnb_toast));
        }else if(diseqcConflict){
            mAdapter.setCanScanTip(mContext.getString(R.string.dvbs_conflict_diseqc_toast));
        }else if(sameName != null){
            mAdapter.setCanScanTip(mContext.getString(R.string.dvbs_same_orbits_lnb_toast, sameName));
        }
        mAdapter.setCanScan(!samePort && !diseqcConflict && sameName == null);
    }

    private void whenBack() {
        TVContent.restoreDVBSsatellites();
        TVContent.freeBachUpDVBSsatellites();
        ScanContent.restoreSatTpInfo(mRescanSatLocalInfoList, mRescanSatLocalTPInfoList);
        mRescanSatLocalInfoList = null;
        mRescanSatLocalTPInfoList = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        SatActivity.isSatListShow = true;
        if (isFargResumed) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
        	/*if(mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_OP)){
            	list.clear();
            	mSatellites = ScanContent.getDVBSsatellites(mContext);
            	list.addAll(mHelper.buildDVBSSATDetailInfo(mAction, mSatellites, 0));
            	mAdapter.setNeedDisableWhenisOff(mNeedDisableItem);
            	mAdapter.setCanScan(!checkIfSamePortOfLnb(mSatellites));
            	mAdapter.notifyDataSetChanged();
            }*/
        } else {
            isFargResumed = true;
        }


        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume........");
    }

    @Override
    public void onStop() {
        SatActivity.isSatListShow = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ScanContent.isPreferedSat() && (mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_OP)
                || mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_RE_SCAN))) {
            whenBack();
        }
    }

    public void refreshSatList() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refreshSatList");
        mSatellites = ScanContent.getDVBSsatellites(mContext);
        list = mHelper.buildDVBSSATDetailInfo(mAction, mSatellites, 0);
        int nextSatId = list.get(0).satid;
        if(!mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING) &&
                !mAction.mItemID.equals(MenuConfigManager.DVBS_SAT_ADD)){
            SatItem nextAction = new SatItem(nextSatId, mAction, getString(R.string.setup_next));
            nextAction.setEnabled(ScanContent.getDVBSEnablesatellites(mContext,mSatellites).size() > 0);
            list.add(0, nextAction);
        }
        ScanContent.setDVBSCurroperator(mDVBSCurrentOP);
        mAdapter = new SatListAdapter(mContext, list);
        mAdapter.setNeedDisableWhenisOff(mNeedDisableItem);
        checkCanScan();
        mAdapter.setListener((SatActivity) (getActivity()));
        mListView.setAdapter(mAdapter);
    }

    public void setNeedCheckM7Scan(boolean check) {
        mNeedCheckM7Scan = check;
    }

    private boolean checkIfSamePortOfLnb(List<SatelliteInfo> satellites){
        if(MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_ANTENNA_TYPE) == MenuConfigManager.DVBS_ACFG_UNICABLE1
                || MtkTvConfig.getInstance().getConfigValue(ScanContent.SAT_ANTENNA_TYPE) == MenuConfigManager.DVBS_ACFG_UNICABLE2){
            return false;
        }
        List<Integer> ports = new ArrayList<Integer>();
        List<Integer> portExs = new ArrayList<Integer>();
        for (SatelliteInfo satelliteInfo:satellites){
            SatelliteInfo info = satelliteInfo;
            int port = info.getPort();
            int portEx = info.getPortEx();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "info.getEnable()>>" + info.getEnable()+",port="+port+",portEx="+portEx);
            if(info.getEnable()){
                if(port == 0 || port == 255){
                    if(info.getDiseqcType() != 0){
                        if(ports.contains(port)){
                            return true;
                        }else {
                            ports.add(port);
                        }
                    }
                }else {
                    if(ports.contains(port)){
                        return true;
                    }else {
                        ports.add(port);
                    }
                }

                if(portEx != 255){
                    if(portExs.contains(portEx)){
                        return true;
                    }else {
                        portExs.add(portEx);
                    }
                }
            }
        }
        return false;
    }

    private boolean checkSdxSelectSatellite(List<SatelliteInfo> satellites) {
        List<String> satNames = Arrays.asList(mContext.getResources().getStringArray(R.array.dvbs_satllite_name_arrays));
        for (SatelliteInfo satellite : satellites) {
            if(satellite.getEnable() && satNames.contains(satellite.getSatName())){
                return true;
            }
        }
        return false;
    }

    /*
     * 0 ~ single, 1 ~ diseqc 1.0, 2 ~ diseqc 1.1
     */
    private boolean checkIfDiseqcConfict(List<SatelliteInfo> satellites) {
        List<Integer> lnbDiseqc = new ArrayList<Integer>();
        for (SatelliteInfo info : satellites) {
            int port = info.getPort();
            int portEx = info.getPortEx();
            if(info.getEnable()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkIfDiseqcConfict info.getEnable()>>" + info.getEnable()+",port="+port+",portEx="+portEx);
                if(port == 0 || port == 255){
                    if(info.getDiseqcType() != 0){//diseqc A
                        if(portEx != 255 && info.getDiseqcTypeEx() != 0){ //same lnb should not set diseqc 1.0 and 1.1 at same time
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1checkIfDiseqcConfict lnbDiseqc="+lnbDiseqc);
                            return true;
                        }else {//this lnb assert to diseqc 1.0
                            if(!lnbDiseqc.isEmpty() && !lnbDiseqc.contains(1)){//other lnb already assert to not diseqc 1.0
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "2checkIfDiseqcConfict lnbDiseqc="+lnbDiseqc);
                                return true;
                            }else {
                                lnbDiseqc.add(1);
                            }
                        }
                    }else { //disable
                        if(portEx != 255 && info.getDiseqcTypeEx() != 0){//this lnb assert to diseqc 1.1
                            if(!lnbDiseqc.isEmpty() && !lnbDiseqc.contains(2)){//other lnb already assert to not diseqc 1.1
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "3checkIfDiseqcConfict lnbDiseqc="+lnbDiseqc);
                                return true;
                            }else {
                                lnbDiseqc.add(2);
                            }
                        }else { //this lnb assert to single
                            lnbDiseqc.add(0);
                        }
                    }
                }else {
                    if(portEx != 255 && info.getDiseqcTypeEx() != 0){//same lnb should not set diseqc 1.0 and 1.1 at same time
                        return true;
                    }else{
                        if(!lnbDiseqc.isEmpty() && !lnbDiseqc.contains(1)){//other lnb already assert to diseqc 1.0
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "4checkIfDiseqcConfict lnbDiseqc="+lnbDiseqc);
                            return true;
                        }else {
                            lnbDiseqc.add(1);
                        }
                    }
                }
            }
        }

        return false;
    }

    private String checkSameOrbitForFastScan(List<SatelliteInfo> satellites) {
        List<Integer> orbits = new ArrayList<Integer>();
        for (SatelliteInfo satelliteInfo:satellites){
            SatelliteInfo info = satelliteInfo;
            int orbit = info.getOrbPos();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "info.getEnable()>>" + info.getEnable()+",orbit="+orbit);
            if(info.getEnable()){
                if(orbits.contains(orbit)){
                    return info.getSatName();
                }else {
                    orbits.add(orbit);
                }
            }
        }
        return null;
    }
}
