
package com.mediatek.wwtv.setting.base.scan.ui;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.ViewGroup;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.setting.EditTextActivity;
import com.mediatek.wwtv.setting.base.scan.adapter.ScanFactorAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.ScanFactorAdapter.ScanFactorItem;
import com.mediatek.wwtv.setting.base.scan.adapter.ScanFactorAdapter.ScanOptionChangeListener;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.tvcenter.commonview.Loading;
import com.mediatek.wwtv.setting.base.scan.model.TKGSContinueScanConfirmDialog;
import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.setting.base.scan.model.DVBSSettingsInfo;
import com.mediatek.wwtv.setting.base.scan.model.DVBTScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBCScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBCCNScanner;
import com.mediatek.wwtv.setting.base.scan.model.DVBTCNScanner;
import com.mediatek.wwtv.setting.base.scan.model.EUATVScanner;
import com.mediatek.wwtv.setting.base.scan.model.ScanCallback;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams.DvbcScanType;
import com.mediatek.wwtv.setting.base.scan.model.ScannerListener;
import com.mediatek.wwtv.setting.base.scan.model.ScannerManager;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.tvcenter.scan.TKGSUserMessageDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.SatDetailUI;
import com.mediatek.wwtv.tvcenter.util.MessageType;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnCancelClickListener;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import mediatek.sysprop.VendorProperties;

/**
* show scan view's activity for all scan which has scan condition if no condition use
* ScanDialogActivity
*
* @author sin_biaoqinggao
*/
public class ScanViewActivity extends BaseCustomActivity {

private static final String TAG = "ScanViewActivity";
/** SA/US factory single RF scan */
private static final String FAV_US_SINGLE_RF_CHANNEL = "fav_us_single_rf_channel";
/** SA/US factory range RF scan */
private static final String FAV_RANGE_RF_CHANNEL = "fav_range_rf_channel";
private String mScanType = FAV_US_SINGLE_RF_CHANNEL;
private Context mContext;
private TextView mStateTextView;
private TextView mAnaloguechannel;
private TextView mNumberChannel;
private TextView mTunerModeView;
private TextView mNumberFromTo;
private TextView mDvbcNetworkName;
private TextView mDVBSChannels;
private ListView mListView;
private ScanFactorAdapter mAdapter;

private TextView mFromChannel;
private TextView mToChannel;
private String[] mTnuerArr;
private String[] allRFChannels;
public ProgressBar progressBar;
public int mPercentage;
public TextView mFinishpercentage;
public Loading loading;
private boolean mCanCompleteScan;// for US scan
private TVContent mTv;
private SaveValue saveV;
private MenuConfigManager mConfigManager;
private int mLastChannelID;
private int mAddedChannelCount = -1;
public static boolean isSelectedChannel = true;
private int freFrom;
private int freTo;
private int mCurrntRFChannel;
private int tsLockTimes = 0;
private String mItemID = MenuConfigManager.FACTORY_TV_RANGE_SCAN_DIG;
private EditChannel editChannel;
private final static int SCANL_LIST_SElECTED_FOR_SETTING_TTS = 0x110;
private ScanParams dvbsParams;
private ScanParams dvbcParams;
private int mDvbsUpdateScanChNum = 0;
private boolean isFromFstVersionChgDialog = false;
private Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == TvCallbackConst.MSG_CB_SCAN_NOTIFY) {
            TvCallbackData backData = (TvCallbackData) msg.obj;
            switch (backData.param1) {
                case MenuConfigManager.MSG_SCAN_UNKNOW:
                    break;
                case MenuConfigManager.MSG_SCAN_COMPLETE:
                    isScanning = false;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[MSG_SCAN_COMPLETE]: "
                            + "  [Progress]: " + backData.param2 + "backData.param3:"
                            + backData.param3 + "backData.param4:" + backData.param4
                            + "scan mode:>" + mCanCompleteScan);
                    if (mCanCompleteScan || !CommonIntegration.isUSRegion()) {
                        setScanProgress(100);
                        loading.stopDraw();
                        loading.setVisibility(View.INVISIBLE);
                        mStateTextView.setText(mContext
                                .getString(R.string.menu_setup_channel_scan_done));
                        mTv.removeCallBackListener(mHandler);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[mScanType]: " + mScanType);
                        if (mScanType.equals(FAV_US_SINGLE_RF_CHANNEL)) {
                            mHandler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    int channelId = mTv.getChannelIDByRFIndex(freFrom);
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[MSG_SCAN_PROGRESS mLastChannelID]: "
                                            + channelId + ">>>>" + mLastChannelID);
                                    if (channelId <= 0) {
                                        channelId = mLastChannelID;
                                    } else {
                                        TIFChannelInfo tifChannelInfo = TIFChannelManager
                                                .getInstance(mContext)
                                                .getTIFChannelInfoById(channelId);
                                        if (tifChannelInfo != null && tifChannelInfo.mMtkTvChannelInfo != null
                                                && tifChannelInfo.mMtkTvChannelInfo
                                                        .getBrdcstType()
                                                    != MtkTvChCommonBase.BRDCST_TYPE_ANALOG) {
                                            if(!CommonIntegration.getInstance().isCurrentSourceTv()){
                                                String inputSourceName;
                                                if(CommonIntegration.isEURegion() || CommonIntegration.isCNRegion()){
                                                    inputSourceName = "DTV";
                                                }else{
                                                    inputSourceName = "TV";
                                                }
                                                InputSourceManager.getInstance().
                                                saveOutputSourceName(inputSourceName, CommonIntegration.getInstance().getCurrentFocus());
                                            }
                                            TIFChannelManager.getInstance(mContext)
                                                    .selectChannelByTIFInfo(tifChannelInfo);
                                        }
                                    }

                                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[getFreFrom]: " + freFrom + "channelId:"
                                            + channelId);
                                    if (!ScanViewActivity.this.isFinishing()) {
                                        sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH,0);
                                    }
                                }
                            }, 1000);
                            if(backData.param3 == 0){
                                isSelectedChannel = true;
                            }
                        } else {
                            mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                            mHandler.sendEmptyMessageDelayed(
                                    MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL,
                                    MessageType.delayMillis5);
                        }
                    } else {
                        mCanCompleteScan = true;
                    }

                    break;
                case MenuConfigManager.MSG_SCAN_PROGRESS:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[MSG_SCAN_PROGRESS]: " + "  [Progress]: "
                            + backData.param2 + "backData.param3:"
                            + backData.param3 + "backData.param4:"
                            + backData.param4 + "scan mode:"
                            + mAddedChannelCount);
                    if (mStateTextView.getText().equals(
                            mContext.getString(R.string.menu_tv_analog_manual_scan_cancel))) {
                        return;
                    }
                    mStateTextView.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    if (!loading.isLoading()) {
                        loading.drawLoading();
                    }
                    mStateTextView
                            .setText(mContext.getString(R.string.menu_setup_channel_scan));
                    if (CommonIntegration.isUSRegion() || CommonIntegration.isSARegion() || CommonIntegration.isEURegion()) {
                        if (backData.param4 == 1) {
                            mAnaloguechannel.setText(mContext
                                    .getString(R.string.menu_setup_channel_scan_ana)
                                    + backData.param3);
                        } else {
                            mNumberChannel.setText(mContext
                                    .getString(R.string.menu_setup_channel_scan_dig)
                                    + backData.param3);
                        }

                    }
                    mAddedChannelCount++;
                    if (!CommonIntegration.isEURegion()) {
                        mNumberFromTo.setText((freFrom + mAddedChannelCount)
                                + "/" + freTo);
                    }
                    setScanProgress(backData.param2);
                    break;
                case MenuConfigManager.MSG_SCAN_CANCEL:
                    isSelectedChannel = true;
                    isScanning = false;
                    loading.stopDraw();
                    loading.setVisibility(View.INVISIBLE);
                    mTv.removeCallBackListener(mHandler);
                    mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                    mHandler.sendEmptyMessageDelayed(
                            MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL,
                            MessageType.delayMillis5);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[Channels]: MSG_SCAN_CANCEL");
                    break;
                case MenuConfigManager.MSG_SCAN_ABORT:
                    isSelectedChannel = true;
                    isScanning = false;
                    mTv.removeCallBackListener(mHandler);
                    loading.stopDraw();
                    loading.setVisibility(View.INVISIBLE);
                    mStateTextView.setText(mContext
                            .getString(R.string.menu_setup_channel_scan_error));
                    mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                    mHandler.sendEmptyMessageDelayed(
                            MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL,
                            MessageType.delayMillis5);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[Channels]: MSG_SCAN_ABORT");
                    break;
                default:
                    break;
            }
        } else if (msg.what == MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL>>>");
            selectChannel();
            isSelectedChannel = true;
            MenuDataHelper.getInstance(mContext).changeEnable();
        } else if (msg.what == MessageType.MESSAGE_START_SCAN) {
            mTv.getScanManager().startATVAutoScan();
        } else if (msg.what == MessageType.MENU_DVBT_RF_SCAN_TUNESIGNAL) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ScanFactorItem itemLevel = (ScanFactorItem) mListView.getSelectedView()
                            .getTag(R.id.factor_name);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.isScanning()1>>" + mTv.isScanning() + ">>>"
                            + DVBTScanner.selectedRFChannelFreq);
                    if (!mTv.isScanning()
                            //&& !mTv.changeChannelByQueryFreq(DVBTScanner.selectedRFChannelFreq)
                            && itemLevel.id
                                    .equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)) {
                        editChannel.tuneDVBTRFSignal();
                    }
                    //removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
                    sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH,0);
                }
            }).start();
        } else if(msg.what == MessageType.MENU_CN_TV_RF_SCAN_CONNECTTURN) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handle MENU_CN_TV_RF_SCAN_CONNECTTURN");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ScanFactorItem itemLevel = (ScanFactorItem) mListView.getSelectedView()
                            .getTag(R.id.factor_name);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.isScanning()1>>" + mTv.isScanning() + ">>>"
                            + DVBTCNScanner.selectedRFChannelFreq);
                    if (!mTv.isScanning()
                            //&& !mTv.changeChannelByQueryFreq(DVBTCNScanner.selectedRFChannelFreq)
                            && itemLevel.id
                                    .equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)) {
                        editChannel.tuneDVBTRFSignal();
                    }
                    //removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
                    sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH,0);
                }
            }).start();
        } else if(msg.what == MessageType.MENU_USSA_TV_RF_SCAN_CONNECTTURN) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTv.isScanning()1>>" + mTv.isScanning()
                            + ",now RF Index:" + mCurrntRFChannel);
                    if (!mTv.isScanning()
                            /*&& !mTv.changeChannelByQueryFreq(mCurrntRFChannel)*/) {
                        editChannel.tuneUSSAFacRFSignalLevel(mCurrntRFChannel);
                    }
                    //removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
                    sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH,0);
                }
            }).start();
        } else if(msg.what == MessageType.MENU_TV_RF_SCAN_REFRESH) {
            int signalLevel = msg.arg1;
            int signalQuality = msg.arg2;
            // isSelectedChannel = true;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("fff", "handled--MENU_TV_RF_SCAN_REFRESH---signalLevel--->"
                    + signalLevel + "   signalQuality:" + signalQuality);
            if (CommonIntegration.isSARegion() || CommonIntegration.isCNRegion() && mActionID != null
                    && mActionID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CN)
                    && mTunerMode == CommonIntegration.DB_AIR_OPTID) {
                ScanFactorItem itemLevel = (ScanFactorItem) mListView.getChildAt(1).getTag(
                        R.id.factor_name);
                ScanFactorItem itemQual = (ScanFactorItem) mListView.getChildAt(2).getTag(
                        R.id.factor_name);
                if (itemLevel != null) {
                    itemLevel.progress = signalLevel;
                }
                if (CommonIntegration.isSARegion()) {
                    itemQual = (ScanFactorItem) mListView.getChildAt(2).
                            getTag(R.id.factor_name);

                    if (itemQual != null) {
                        itemQual.optionValue = signalQuality;
                    }
                }
            } else if (CommonIntegration.isUSRegion() && !mTv.isScanning()) {
                ScanFactorItem itemLevel = (ScanFactorItem) mListView.getChildAt(3).getTag(
                        R.id.factor_name);
                if (itemLevel != null) {
                    itemLevel.progress = signalLevel;
                }
            } else if (CommonIntegration.isEURegion() && !mTv.isScanning()) {
                // need totdo something
                int minValue = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);
                if (minValue == 1 || minValue == 0) {
                    ScanFactorItem itemLevel;
                    ScanFactorItem itemQual;
                    if (minValue == 0) {// antenna single-rf scan
                        itemLevel = (ScanFactorItem) mListView.getChildAt(1).getTag(
                                R.id.factor_name);
                        itemQual = (ScanFactorItem) mListView.getChildAt(2).getTag(
                                R.id.factor_name);
                    } else {// cable single-rf scan
                        itemLevel = (ScanFactorItem) mListView.getChildAt(2).getTag(
                                R.id.factor_name);
                        itemQual = (ScanFactorItem) mListView.getChildAt(3).getTag(
                                R.id.factor_name);
                    }
                    if (itemLevel != null) {
                        itemLevel.progress = signalLevel;
                    }
                    if (itemQual != null) {
                        itemQual.progress = signalQuality;
                    }
                }
            }
            if (!ScanViewActivity.this.isFinishing()) {
                mAdapter.notifyDataSetChanged();
                //removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
                sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH,
                        MessageType.delayMillis6);
            }
        } else if(msg.what == MessageType.MENU_TV_SCAN_TS_LOCK_STATUS) {
            boolean isLocked = mTv.isTsLocked();
            tsLockTimes ++;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isLocked:"+isLocked+" tsLockTimes:"+tsLockTimes+" index:"+mTv.getScanManager().getDVBSCurrentIndex());
            if(tsLockTimes > 55 && tsLockTimes < 65 || isLocked) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "position moveing done. start scan.");
                mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
                removeMessages(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS);
                mTv.getScanManager().startDvbsScanAfterTsLock();
            } else {
                sendEmptyMessageDelayed(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS,
                        MessageType.delayMillis6);
            }
        }else if(msg.what == MessageType.MENU_DVBC_SCAN_HOME_FREQUENCY_TIP){
            Toast.makeText(ScanViewActivity.this, R.string.cable_advance_scan_message, Toast.LENGTH_LONG).show();
        }else if(msg.what == MessageType.MENU_AUTO_EXIT_MESSAGE){
            ScanViewActivity.this.finish();
        }
    }
};

/**
 * select last or first channel after scan complete
 */
private void selectChannel() {
    selectChannel(false);
}

private void selectScanedRFChannel() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectScanedRFChannel>");
    if(mTv.getScanManager().hasChannels()){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(mTv.selectScanedRFChannel()) {
                    setChlistBroadcast();
                    tuneFrequency();
                }
            }
        }, 2000);
    }
}

    private void tuneFrequency(){
        if(mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)){
            editChannel.tuneDVBCRFSignal(mTv.getInitDVBCRFFreq() * 1000);
        }else if(mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)){
            editChannel.tuneDVBTRFSignal();
        }
    }

private void selectChannel(boolean needSelectCurrent) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel >>>>>" + getIntent().getBooleanExtra("need_select_channel",false)+","+LiveTvSetting.isBootFromLiveTV());
	if(!LiveTvSetting.isBootFromLiveTV() && !getIntent().getBooleanExtra("need_select_channel",false)){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannel return due to not from LiveTV");
        return;
    }
    int current = CommonIntegration.getInstance().getCurrentChannelId();
    boolean haveChannels = mTv.getScanManager().hasChannels();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"current >>>>>" + current+",haveChannes="+haveChannels);
    if(needSelectCurrent || !haveChannels){
        editChannel.selectChannel(current);
    }else {
        new Thread(){
            public void run() {
                TIFChannelInfo info = TIFChannelManager.getInstance(mContext).getFirstChannelForScan();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel>>>>>" + info);
                if(info != null){
                    TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(info);
                }else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel>>>>> null");
                    editChannel.selectChannel(current);
                }
            };
        }.start();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"set cam Scan complete");
    com.mediatek.twoworlds.tv.MtkTvCI.getInstance(0).setScanComplete();
    setChlistBroadcast();
    /*com.mediatek.wwtv.tvcenter.util.MtkLog.d(
            TAG,
            "selectChannel>>>>>" + mLastChannelID + ">>"
                    + TIFChannelManager.getInstance(mContext).isChannelsExist());
    if (!mTv.isScanning()) {
        // if (mNeedChangeToFirstChannel) {
        // editChannel.selectChannel(0);
        // } else {
        editChannel.selectChannel(mLastChannelID);
        // }
    }*/
}

private void setChlistBroadcast() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"select broadcast chlist type");
    SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
}

    private void saveDvbcParam(){
        if(mTv.getCurrentTunerMode() == 1
                && (null != dvbcParams)
                && dvbcParams.dvbcScanMode != ScanParams.DvbcScanMode.FULL
                && mTv.getScanManager().hasDigitalChannels()){
            int freq = dvbcParams.freq;
            if(freq > 0){
                freq = freq / 1000;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDvbcParam livetv freq="+freq+",netId="+dvbcParams.networkID);
            SaveValue.saveWorldValue(this,  "dvbc_network_freq", freq, true);
            SaveValue.saveWorldValue(this,  "dvbc_network_id", dvbcParams.networkID, true);
        }
    }

private ScannerListener mScanListener = new ScannerListener() {

    String factorId;
    ScanFactorItem item;

    @Override
    public void onCompleted(final int completeValue) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"MenuMain mExitLevel onCompleted:" + completeValue
                + "mTv.isScanTaskFinish():" + mTv.isScanTaskFinish());
        mTv.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
        if(mListView.getChildCount() > 0){
            item = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            factorId = item.id;
        }
        if(factorId==null){
            if(mActionID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN)){
                factorId = MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN;
            }else {
                factorId = "";
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isFromFstVersionChgDialog="+isFromFstVersionChgDialog);
        if(isFromFstVersionChgDialog){
            mHandler.sendEmptyMessageDelayed(
                    MessageType.MENU_AUTO_EXIT_MESSAGE, MessageType.delayMillis5);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.stopDraw();
                if (completeValue == ScannerListener.COMPLETE_OK) {
                    if (mTv.isScanTaskFinish()) {
                        saveDvbcParam();
                        isScanning = false;
                            if(ScanContent.isSatOpHDAustria()) {
                            List<TIFChannelInfo> tifChannelList = TIFChannelManager
                                    .getInstance(mContext).queryRegionChannelForHDAustria();
                            if (tifChannelList.size() > 0) {// have channel ,then start activity
                                Intent intent = new Intent(ScanViewActivity.this,
                                        RegionalisationAusActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("regions", (Serializable) tifChannelList);
                                intent.putExtra("regions", bundle);
                                //intent.putExtra("regions", (Serializable) tifChannelList);
                                startActivity(intent);
                                }
                            } else if(ScanContent.isSatOpDiveo()) {
                                Map<String, List<TIFChannelInfo>> queryRegionChannelForDiveo =
                                        TIFChannelManager.getInstance(mContext).queryRegionChannelForDiveo();
                                if(queryRegionChannelForDiveo.size() > 0) {
                                    Intent intent = new Intent(ScanViewActivity.this,
                                            RegionalisationAusActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("regions", (Serializable) queryRegionChannelForDiveo);
                                    intent.putExtra("regions", bundle);
                                    startActivity(intent);
                                }
                            }
                        showTricolorChannelListDialog();
                        loading.setVisibility(View.INVISIBLE);
                        if (mTv.hasOPToDo()) {
                            // debug for TRD views. 2271
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onCompleted(),hasOPToDo....,show TRDviews");
                            // show TRD views.
                            showDVBSNFYInfoView();
                        } else {
                            mTv.uiOpEnd();
                        }
                        boolean isTKGS = ScanContent.isPreferedSat() && mTv.isTurkeyCountry()
                                && mTv.isTKGSOperator();
                        if (isTKGS) {
                            // for satellite tkgs's scan usermessage dialog
                            int mask = mTv.getScanManager().getTkgsNfyInfoMask();
                            String userMsg = mTv.getScanManager().getDVBSTkgsUserMessage();
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sanling scan userMsg 33==" + userMsg + ",mask=="
                                    + mask);
                            // mask == SB_DVBS_GET_INFO_MASK_TKGS_USER_MESSAGE && msg not empty
                            if (!TextUtils.isEmpty(userMsg)) {
                                mTv.getScanManager().setTkgsNfyInfoMask(0);
                                showTKGSUserMessageDialog(userMsg);
                            }
                        }
                        nowProgress = 0;
                        if (CommonIntegration.isEURegion()
                                && factorId != null
                                && (factorId
                                        .equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)
                                        || factorId
                                                .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ)
                                        || factorId
                                                .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION)
                                        || factorId.equals(MenuConfigManager.SYM_RATE))
                                || factorId
                                        .equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {// DTV00615026
                                                                                                 // for
                                                                                                 // cancel
                                                                                                 // scan
                                                                                                 // and
                                                                                                 // back
                                                                                                 // no
                                                                                                 // item
                                                                                                 // show
                            if (mTv.getScanManager().isSingleRFScan()) {// jump to new channel
                                                                        // DTV00617919
                                selectScanedRFChannel();
                            } else if (factorId != null
                                    && factorId
                                    .equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
                                selectChannel();
                            }
                        } else if (factorId != null
                                && (factorId
                                .equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN)
                                || factorId
                                        .equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG))) {
                            selectChannel();
                        } else if (CommonIntegration.isCNRegion()
                                && factorId != null
                                && (factorId
                                        .equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)
                                || factorId.equals(MenuConfigManager.TV_CHANNEL_END_FREQUENCY))) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                        } else if (CommonIntegration.isCNRegion()
                                && factorId != null
                                && (factorId
                                        .equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)
                                        || factorId.equals(MenuConfigManager.SYM_RATE)
                                        || factorId
                                                .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION)
                                        || factorId
                                                .equals(MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN)
                                        || factorId
                                                .equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                                        || factorId
                                            .equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN))) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TV_SINGLE_SCAN_RF_CHANNEL>>");
                            if (factorId.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN)
                                    || factorId
                                            .equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                                    || factorId.equals(MenuConfigManager.SYM_RATE)
                                    || factorId
                                            .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION)
                                    || factorId
                                            .equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)) {
                                if (mTv.getScanManager().isSingleRFScan()) {// jump to new
                                                                            // channel
                                                                            // DTV00617919
                                    selectScanedRFChannel();
                                    /*if (!selectScanedRFChannel()) {
                                        if (!factorId
                                                .equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)) {
                                           selectChannel(true);
                                        }
                                    }*/
                                }
                            }
                        } else if (factorId != null
                                && !factorId.equals(MenuConfigManager.TV_ANANLOG_SCAN_UP)
                                        && !factorId
                                                .equals(MenuConfigManager.TV_ANANLOG_SCAN_DOWN)
                                        && !factorId
                                                .equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)
                                        && !factorId
                                            .equals(MenuConfigManager.TV_CHANNEL_STARTSCAN)) {
                            selectChannel(true);
                        } else {
                            float freq = (float) (mATVFreq / 1e6);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ScanUP/ScanDown Finshed. please set frequency!"
                                    + mATVFreq + ">>" + freq);
                            if (item != null && item.factorType == 4) {// for do scan type
                                if (freq != 0) {
                                    if (mTv.changeChannelByFreq(mATVFreq)) {
                                        freq = Math.max(freq, 44);
                                        //String freqStr = String.format("%d%s", freq, ".00");
                                        EditText freqView = (EditText) mListView.getChildAt(0)
                                                .findViewById(R.id.factor_input);
                                        freqView.setText(new DecimalFormat("0.00").format(freq));
                                        ScanFactorItem freqitem = (ScanFactorItem) mListView
                                                .getChildAt(0).getTag(R.id.factor_name);
                                        freqitem.inputValue = (int)freq;
                                        mNeedChangeStartFrq = true;
                                        setChlistBroadcast();//atv direct tune,no need select channel
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ScanUP/ScanDown Finshed. freq is=="
                                                + freq);
                                    } else {
                                        mNumberFromTo
                                                .setText(mContext
                                                        .getString(R.string.menu_tv_freq_scan_no_channel));
                                    }
                                } else {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ScanUP/ScanDown freq == 0");
                                    mNumberFromTo.setText(mContext
                                            .getString(R.string.menu_tv_freq_scan_no_channel));
                                }
                            } else {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ScanUP/ScanDown Finshed. Not NumView");
                            }

                        }
                        setScanProgress(100);
                        mStateTextView.setText(mContext
                                .getString(R.string.menu_setup_channel_scan_done));
                        MenuDataHelper.getInstance(mContext).changeEnable();
                    } else if (!mStateTextView.getText().equals(
                            mContext.getString(R.string.menu_tv_analog_manual_scan_cancel))) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onCompleted(),task not finished,start another task to scan");
                        loading.setVisibility(View.VISIBLE);
                        loading.drawLoading();
                        // fix CR 00654421;after scan dtv also need to scan atv so set
                        // isScanning true
                        isScanning = true;
                        if (svl > CommonIntegration.DB_CAB_SVLID) {
                            mTv.startDVBSScanTask(mScanListener);
                            mAnaloguechannel
                                    .setText(getString(R.string.menu_setup_satellite_name)
                                            + mTv.getScanManager().getFirstSatName());
                            mNumberChannel.setText(getSatName());
                        } else {
                            nowProgress = 50;
                            mTv.startOtherScanTask(mScanListener);
                        }
                    } else {
                        mScanListener.onCompleted(ScannerListener.COMPLETE_CANCEL);
                    }
                } else if (completeValue == ScannerListener.COMPLETE_ERROR) {
                    isScanning = false;
                    if (CommonIntegration.isEURegion()
                            && factorId != null
                            && (factorId.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)
                                    || factorId
                                            .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ)
                                    || factorId
                                            .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION)
                                    || factorId.equals(MenuConfigManager.SYM_RATE))
                            || factorId.equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
                        if (factorId.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)) {// DVBT
                                                                                            // RF
                            refreshDVBTRFSignalQualityAndLevel(1000);
                        } else if (factorId
                                .equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
                            selectChannel();
                        }
                    } else if (CommonIntegration.isCNRegion() && factorId != null
                            && (factorId.equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)
                            || factorId.equals(MenuConfigManager.TV_CHANNEL_END_FREQUENCY))) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                    } else if (CommonIntegration.isCNRegion()
                            && factorId != null
                            && (factorId.equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)
                                    || factorId.equals(MenuConfigManager.SYM_RATE)
                                    || factorId
                                            .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION)
                                    || factorId
                                            .equals(MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN)
                                    || factorId
                                            .equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                                    || factorId
                                        .equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN))) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TV_SINGLE_SCAN_RF_CHANNEL>>");
                        if (factorId.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN)) {
                            selectChannel(true);
                        }
                    } else if (factorId != null
                            && !factorId.equals(MenuConfigManager.TV_ANANLOG_SCAN_UP)
                                    && !factorId.equals(MenuConfigManager.TV_ANANLOG_SCAN_DOWN)
                                    && !factorId
                                            .equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)
                                    && !factorId
                                            .equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN)
                                    && !factorId
                                            .equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG)
                                    && !factorId.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                    }
                    nowProgress = 0;
                    mStateTextView.setText(mContext
                            .getString(R.string.menu_setup_channel_scan_error));
                } else if (completeValue == ScannerListener.COMPLETE_CANCEL) {
                    // isScanning = false;
                    // mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_cancel));
                    // nowProgress = 0;
                    // need to fix
                    saveDvbcParam();
                    if (factorId.equals(MenuConfigManager.TV_ANANLOG_SCAN_UP)
                            || factorId.equals(MenuConfigManager.TV_ANANLOG_SCAN_DOWN)
                            || factorId.equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"ScanUP/ScanDown Canceled!");
                        if (!isScanning) {
                            if (mATVFreq != 0) {
                                float frequency = (float) (mATVFreq / 1.0e6);
                                String freqStr = String.format("%s%7.2f %s", mContext
                                        .getString(R.string.menu_tv_freq), frequency, mContext
                                        .getString(R.string.menu_tv_rf_scan_frequency_mhz));
                                mNumberFromTo.setText(freqStr);
                                // mTv.changeChannelByFreq(mATVFreq);
                                if (CommonIntegration.getInstance().isCurrentSourceATV()) {
                                    selectChannel(true);
                                }
                            }
                        }
                    } else if (CommonIntegration.isEURegion()
                            && (factorId.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)
                                    || factorId
                                            .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ)
                                    || factorId
                                            .equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION)
                                    || factorId.equals(MenuConfigManager.SYM_RATE))) {
                        if (factorId.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)) {// DVBT
                                                                                            // RF
                            refreshDVBTRFSignalQualityAndLevel(1000);
                        } else {// DVBC RF
                            if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
                                selectChannel(true);
                            }
                        }
                    } else {
                        selectChannel();
                    }
                    MenuDataHelper.getInstance(mContext).changeEnable();
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                }
            }
        }, 1000);
        /*if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
            writeAutotestScanFile();
        }*/
    }

    @Override
    public void onFrequence(final int freq) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFrequence:" + freq);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFrequence,Str," + mNumberFromTo.getText());
        mATVFreq = freq;

        final float frequency = (float) (freq / 1.0e6);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String freqStr = String.format("%s%7.2f %s", mContext
                        .getString(R.string.menu_tv_freq), frequency, mContext
                        .getString(R.string.menu_tv_rf_scan_frequency_mhz));
                mNumberFromTo.setText(freqStr);
            }
        }, 10);
    }

    @Override
    public void onIPChannels(int channels, int type) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onIPChannels");
    };

    @Override
    public void onFvpUserSelection() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFvpUserSelection");
    };

    public void onFvpScanStart() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFvpScanStart");
    };

    @Override
    public void onProgress(int progress, int channels) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onProgress");
    }

    @Override
    public void onProgress(final int progress, final int channels, final int type) {
        final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
        final String analogNum = mContext.getString(R.string.menu_tv_analog_channels);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int progressInt = progress;
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onProgress=" + progress + ",nowProgress=" + nowProgress +
                    ",channels=" + channels + ",type=" + type + ",mTv.getActionSize():"
                        + mTv.getActionSize());
                if (svl > CommonIntegration.DB_CAB_SVLID) {
                    int totalSize = mTv.getScanManager().getDVBSTotalSatSize();
                    int currentIndex = mTv.getScanManager().getDVBSCurrentIndex();

                    if (totalSize > 1) {
                        progressInt = currentIndex * 100 / totalSize + progress / totalSize;
                    } else {
                        progressInt = progress;
                    }
                    mTv.getScanManager().setDVBSScannedChannel(currentIndex, channels);
                    // DVBS, get sat size,count the whole the progress.
                } else {
                    if (mTv.getActionSize() > 0 || nowProgress == 50) {
                        progressInt = nowProgress + progress / 2;
                    }
                }

                mFinishpercentage.setText(String.format("%3d%s", progressInt, "%"));
                loading.setVisibility(View.VISIBLE);
                if (!loading.isLoading()) {
                    loading.drawLoading();
                }
                progressBar.setProgress(progressInt);
                switch (type) {
                    case 0:
                    case 1:
                        mAnaloguechannel.setText(String.format("%s:%3d", analogNum, channels));
                        break;
                    case 2:
                        if (svl > CommonIntegration.DB_CAB_SVLID) {// DVBS
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onProgress(),updateChannelNum:");
                            int dvbsChannels = mTv.getScanManager().getDVBSScannedChannel();
                            if(!MenuConfigManager.DVBS_SAT_UPDATE_SCAN.equals(mLocationID)) {
                                mDVBSChannels.setText(String.format("%s:%3d", digitalNum,
                                        dvbsChannels));
                            }
                            mAnaloguechannel.setText(mContext
                                    .getString(R.string.menu_setup_satellite_name)
                                    + mTv.getScanManager().getFirstSatName());
                            mNumberChannel.setText(getSatName());
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progressInt:"+progressInt+" "+getSatName());
                            mTv.getScanManager().setChannelsNum(dvbsChannels, 2);
                        } else {
                            mNumberChannel.setText(String
                                    .format("%s:%3d", digitalNum, channels));
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void beforeDvbsScan() {
        List<SatelliteInfo> lists = ScanContent.getDVBSEnablesatellites(mContext);
        SatelliteInfo satelliteInfo = lists.get(mTv.getScanManager().getDVBSCurrentIndex());
        updateSatelliteName();
        if(satelliteInfo.getMotorType() == 5) { //DiSEqC 1.2
            ScanContent.setDVBSFreqToGetSignalQuality(satelliteInfo.getSatlRecId());
            mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_positioner_moving));
            tsLockTimes = 0;
            mHandler.removeMessages(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS);
            mHandler.sendEmptyMessage(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS);
        } else {
            mTv.getScanManager().startDvbsScanAfterTsLock();
        }
    }

    public void onDVBCNetworkNameUpdate(int argv4, String name) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDVBCNetworkNameUpdate="+name);
        if(mDvbcNetworkName != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDvbcNetworkName.setText(mContext.getString(R.string.menu_net_name)+": "+name);
                }
            });
        }
    };

    @Override
    public void onDVBSInfoUpdated(final int argv4, final String name) {
        if (!TextUtils.isEmpty(name)) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if ("TKGS_LOGO_UPDATE".equals(name)) {// sat update-scan
                        showTKGSUpdateScanDialog(argv4);
                    } else if ("TKGS_LOGO".equals(name)) {// sat rescan
                        showTKGSRescanServiceListView(argv4);
                    } else if ("ChangeSatelliteFrequence".equals(name)) {
                        beforeDvbsScan();
                    } else if ("FAST_SCAN_GET_OPERATOR".equals(name)){
                        if(argv4 > 0){
                            showFastScanOpSelectDialog();
                        }else {
                            showNoOptDialog();
                        }
                    }else if ("FAST_SCAN_CHECK_REGION".equals(name)){
                        if(mIsNeedShowFstRegion){
                            mHandler.postDelayed(new Runnable() {
                                
                                @Override
                                public void run() {
                                    Map<String, List<TIFChannelInfo>> queryRegionChannelForFastScan =
                                            TIFChannelManager.getInstance(mContext).queryRegionChannelForFastScan();
                                    if(queryRegionChannelForFastScan.size() > 0) {
                                        Intent intent = new Intent(ScanViewActivity.this,
                                                RegionalisationAusActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("regions", (Serializable) queryRegionChannelForFastScan);
                                        intent.putExtra("regions", bundle);
                                        startActivity(intent);
                                    }
                                }
                            }, 1000);
                            
                        }
                        mIsNeedShowFstRegion = false;
                    }
                    else {
                        int satId = mTv.getScanManager().getCurrentSalId();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDVBSInfoUpdated="+name+",salId="+satId);
                        SatDetailUI.getInstance(mContext).updateOnlySatelliteName(name,satId);
                    }

                }
            });
            return;
        }
        switch (argv4) {
            case CALL_BACK_TYPE_DTV_DVBS_SVC_UPDATE_NFY_FCT:
                if (mLocationID != null && mLocationID.equals(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)) {
                    MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
                    base.dvbsGetNfySvcUpd();
                    mDvbsUpdateScanChNum += (base.nfySvcUpd_apAdd + base.nfySvcUpd_rdAdd + base.nfySvcUpd_tvAdd);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ap=" + base.nfySvcUpd_apAdd + ",rd=" + base.nfySvcUpd_rdAdd + ",tv=" + base.nfySvcUpd_tvAdd+",total="+mDvbsUpdateScanChNum);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDVBSChannels.setText(String.format("%s:%3d", mContext.getString(R.string.menu_tv_digital_channels),
                                    mDvbsUpdateScanChNum));
                        }
                    });
                }

                // mHandler.post(new Runnable() {
                // @Override
                // public void run() {
                // try {
                // if(mListViewSelectedItemData.mParent.mParent.getmItemID()==MenuConfigManager.DVBS_SAT_UPDATE_SCAN){
                // }
                // } catch (Exception e) {
                // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onDVBSInfoUpdated()"+e.toString());
                // }
                // }
                // });
                break;

            default:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mNumberChannel != null && !name.isEmpty()) {
                            mNumberChannel.setText(getSatName());
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void onFvpScanError() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFvpScanError");
    }

};

private int nowProgress;
private int mATVFreq;
private int mTunerMode;
private int svl;
private String mActionID="";
// locationID and satID is used for DVBS scan
private String mLocationID;
private int mSatID;
private String cableOperator;
private boolean mIsNeedShowFstRegion = false;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(!getIntent().getBooleanExtra("need_full_screen",false)) {
        setDisplayAttr();
    }else {
        setDisplayAttr(1f,1f);
    }
    this.setContentView(R.layout.scan_view_layout);
    mContext = this;
    /*Intent lifeIntent = new Intent(TurnkeyService.SCAN_VIEW_ACTION);
    lifeIntent.putExtra(TurnkeyService.SCAN_VIEW_LIFECIRLE, true);
    this.sendBroadcast(lifeIntent);*/
    svl = CommonIntegration.getInstance().getSvlFromACFG();
    mActionID = this.getIntent().getStringExtra("ActionID");
    //isAutoScan = this.getIntent().getBooleanExtra("isAutoScan", false);
    cableOperator = this.getIntent().getStringExtra("CableOperator");
    if (cableOperator != null) {
        ScanContent.setOperator(this, cableOperator);
    }
    if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
        MenuConfigManager mConfigManager = MenuConfigManager.getInstance(mContext);
        int deftunermode = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);
        int tunermode = this.getIntent().getIntExtra("tuner_mode", deftunermode);
        mConfigManager.setValue(MenuConfigManager.TUNER_MODE,
                tunermode);
    }
    init();
 // if actionID is dvbs-detail-ifo-scan get another intent data
    if (mActionID != null) {
        if (mActionID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN)) {
            mSatID = getIntent().getIntExtra("SatID", -1);
            mLocationID = this.getIntent().getStringExtra("LocationID");
            mTv.getScanManager().setFastScanSecondScan(false);
            startDVBSFullScan(mSatID, -1, mLocationID);//new dvbs UI scan directly
        }else if (mActionID.equals(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)) {
            mSatID = getIntent().getIntExtra("SatID", -1);
            boolean spclRegnSetup = getIntent().getBooleanExtra("SpclRegnSetup",false);
            isFromFstVersionChgDialog = getIntent().getBooleanExtra("isFromFstVersionChgDialog",false);
            String selectedOpName = getIntent().getStringExtra("SelectedOpName");
            mLocationID = this.getIntent().getStringExtra("LocationID");
            restartFastDVBSFullScan(spclRegnSetup, selectedOpName);//new dvbs UI scan directly
        }
    }
    mTv.mHandler = mHandler;
    mTv.stopTimeShift();
    if (mActionID != null) {
        if (CommonIntegration.isEURegion()) {
            if (mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN)) {
                if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
                    CommonIntegration.getInstance().stopMainOrSubTv();
                }
            } else if (mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)
                    || mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)) {
                if (CommonIntegration.getInstance().isCurrentSourceATV()) {// not tune signal
                    CommonIntegration.getInstance().stopMainOrSubTv();
                } else {
                    tuneFrequency();
                    sendMessageDelayedThread(
                            MessageType.MENU_TV_RF_SCAN_REFRESH, MessageType.delayMillis6);
                }
            }
        }
        if (CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
            if (mActionID.equals(MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN)) {
                if (CommonIntegration.getInstance().isCurrentSourceATV()) {
                    CommonIntegration.getInstance().stopMainOrSubTv();
                }
            }
        }

        if (mActionID.equals(MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN)
                || mActionID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CN)
                && mTunerMode == CommonIntegration.DB_AIR_OPTID) {
            //mHandler.removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
            sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH,
                    MessageType.delayMillis6);
        }
    }
    // add broadcast
    IntentFilter intentFilter1 = new IntentFilter();
    intentFilter1.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    intentFilter1.addAction(Intent.ACTION_SCREEN_OFF);
    intentFilter1.addAction(Intent.ACTION_SCREEN_ON);
    this.registerReceiver(mReceiver, intentFilter1);

    /*if (mTv.isItaCountry()) {
        if (mActionID!=null&&!mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN) &&
                !mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN) &&
                !mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)) {
            ScanThirdlyDialog thirdDialog = new ScanThirdlyDialog(mContext, 5);
            thirdDialog.setCancelable(false);
            thirdDialog.show();
        }
    }*/
}

private BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
            if (mActionID != null) {
                if (CommonIntegration.isEURegion()) {
                    if (mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN)
                            || mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)
                            || mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)) {
                        finish();
                    }
                }
            }
        }
    }
};

protected void onPause() {
    if (mTv.isScanning()) {
        cancelScan();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPause() mActionID:" + mActionID);
    }
    super.onPause();
}

@Override
protected void onDestroy() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestory....");
    this.unregisterReceiver(mReceiver);
    mTv.removeCallBackListener(mHandler);
    mTv.setScanListener(null);
    mHandler.removeCallbacksAndMessages(null);
    mTv.mHandler = null;
    mScanListener = null;

    /*if (mActionID != null) {
        if (CommonIntegration.isEURegion()) {
            if (mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN)
                    || mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)
                    || mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)) {
                // CommonIntegration.getInstance().startMainOrSubTv();
                if (turnkey!=null) {
                    turnkey.selectCurrentChannelDelay(0);
                }
            }
        } else if (CommonIntegration.isCNRegion()) {
            if (mActionID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CN) ||
                    mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN)) {
                // CommonIntegration.getInstance().startMainOrSubTv();
                if (turnkey!=null) {
                    turnkey.selectCurrentChannelDelay(0);
                }
            }
        } else if (CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
            if (mActionID.equals(MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN)
                    || mActionID.startsWith(MenuConfigManager.FACTORY_TV_RANGE_SCAN)) {
                // CommonIntegration.getInstance().startMainOrSubTv();
                if (turnkey!=null) {
                    turnkey.selectCurrentChannelDelay(0);
                }
            }

        }
    }*/
    /*Intent lifeIntent = new Intent(TurnkeyService.SCAN_VIEW_ACTION);
    lifeIntent.putExtra(TurnkeyService.SCAN_VIEW_LIFECIRLE, false);
    this.sendBroadcast(lifeIntent);*/
    super.onDestroy();
}

private void init() {
    if(!LiveTvSetting.isBootFromLiveTV()){//from settings
        getWindow().setBackgroundDrawableResource(R.drawable.screen_background_black);
    }
    mTv = TVContent.getInstance(mContext);
    saveV = SaveValue.getInstance(mContext);
    mConfigManager = MenuConfigManager.getInstance(mContext);
    editChannel = EditChannel.getInstance(mContext);
    mStateTextView = (TextView) findViewById(R.id.state);
    progressBar = (ProgressBar) findViewById(R.id.scanprogressbar);
    mFinishpercentage = (TextView) findViewById(R.id.finishpercentage);
    loading = (Loading) findViewById(R.id.setup_tv_scan_loading);
    mFinishpercentage.setText(String.format("%3d%s",mPercentage,"%"));
    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    mAnaloguechannel = (TextView) findViewById(R.id.analoguechannel);
    mAnaloguechannel.setText(mContext.getString(R.string.menu_setup_channel_scan_ana));
    mNumberChannel = (TextView) findViewById(R.id.numberchannel);
    mNumberChannel.setText(mContext.getString(R.string.menu_setup_channel_scan_dig));
    mTunerModeView = (TextView) findViewById(R.id.trun_mode);
    mTnuerArr = mContext.getResources()
            .getStringArray(R.array.menu_tv_tuner_mode_array_full_eu);
    mTunerModeView.setText(mTnuerArr[mTv.getCurrentTunerMode()]);

    mNumberFromTo = (TextView) findViewById(R.id.numberchannel_from_to);
    mNumberFromTo.setVisibility(View.INVISIBLE);

    mDvbcNetworkName = (TextView) findViewById(R.id.dvbc_network_name);
    mDvbcNetworkName.setVisibility(isNeedShowNetworkName() ? View.VISIBLE : View.GONE);

    mDVBSChannels = (TextView) findViewById(R.id.dvbsusedigital_channels);
    if (CommonIntegration.isEURegion() &&mActionID!=null&& (mActionID.equals(MenuConfigManager.FACTORY_TV_RANGE_SCAN_DIG)
            || mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN))) {
        allRFChannels = mTv.getDvbtAllRFChannels();
    }
    initFactorListView();
    mTunerMode = mTv.getCurrentTunerMode();
    if (mTunerMode >= CommonIntegration.DB_SAT_OPTID) {
        mAnaloguechannel.setText("");
        mNumberChannel.setText("");
    }
    initText(mActionID);
}

private void initFactorListView() {
    List<ScanFactorItem> list = generateFactors();
    mListView = (ListView) (ListView) findViewById(R.id.scan_factor_listview);
    if(list.isEmpty()) {
        mListView.setVisibility(View.INVISIBLE);
        return;
    }
    mAdapter = new ScanFactorAdapter(this, list);
    mListView.setAdapter(mAdapter);
    mListView.setAccessibilityDelegate(mAccDelegateForChList);
    mListView.setOnItemClickListener(onItemClickListener);
}

private OnItemClickListener onItemClickListener = new OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onItemClick: position:" + position);
        if(mActionID==null || isScanning){
            return;
        }
        if (mActionID.startsWith(MenuConfigManager.FACTORY_TV_RANGE_SCAN)) {
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            startFacRangeScan(sleItem.id, mActionID);
        } else if (mActionID.equals(MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN)) {
            startFacSingleScan(MenuConfigManager.FAV_US_SINGLE_RF_CHANNEL);
        } else if (mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)) {
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            if (sleItem.id.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)) {
                startSingleRFScan(sleItem.optionValue);
            }
        } else if (mActionID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN)) {// dvbs full
                                                                                    // scan
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            if (sleItem.id.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN)) {// can
                                                                                        // start
                                                                                        // scan
                startDVBSFullScan(mSatID, -1, mLocationID);
            }
        } else if (mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN)) {
            if (CommonIntegration.isCNRegion()) {
                ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                        R.id.factor_name);
                ScanFactorItem freqStartItem = (ScanFactorItem) mListView.getChildAt(0).getTag(
                        R.id.factor_name);
                ScanFactorItem freqEndItem = (ScanFactorItem) mListView.getChildAt(1).getTag(
                        R.id.factor_name);
                if (sleItem.id.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN)) {
                    startAnalogATVFreqRangeScan(freqStartItem.inputValue,
                            freqEndItem.inputValue);
                } else {
                    gotoEditTextAct(sleItem);
                }
            } else {
                ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                        R.id.factor_name);
                ScanFactorItem freqItem = (ScanFactorItem) mListView.getChildAt(0).getTag(
                        R.id.factor_name);
                if (sleItem.id.equals(MenuConfigManager.TV_ANANLOG_SCAN_UP)) {
                    startAnalogScan(true, "" + freqItem.inputValue);
                } else if (sleItem.id.equals(MenuConfigManager.TV_ANANLOG_SCAN_DOWN)) {
                    startAnalogScan(false, "" + freqItem.inputValue);
                } else {
                    gotoEditTextAct(sleItem);
                }
            }
        } else if (mActionID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CN)) {// CN single RF
                                                                              // scan
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            if (sleItem.id.equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)) {
                startSingleRFScan(sleItem.optionValue);
            } else if (sleItem.id.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN)) {
                startDVBCSingleRFScan();
            } else if (sleItem.id.equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                    || sleItem.id.equals(MenuConfigManager.SYM_RATE)) {
                gotoEditTextAct(sleItem);
            }
        } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBC)
                || mActionID.startsWith(MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR)) {// eu
                                                                                           // dvbc
                                                                                           // channel-scan
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            if (sleItem.id.equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
//                    if(isBelgiumCableVoo()) {
//                        //belgium ,cable, voo  show inform dialog
//                        showInformDialogForVoo();
//                    } else {
                    startDVBCFullScan();
//                    }
            } else {
                /*if (cableOperator != null && cableOperator.equals("RCS RDS")) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing RCS RDS");
                } else {*/
                    gotoEditTextAct(sleItem,true);
                //}
            }
        } else if (mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)) {// eu dvbc
                                                                                // single-rf
                                                                                // scan
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            if (sleItem.id.equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
                startDVBCSingleRFScan();
            } else {
                gotoEditTextAct(sleItem);
            }
        } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {// cn dvbc quick/full
                                                                         // scan
            ScanFactorItem sleItem = (ScanFactorItem) mListView.getSelectedView().getTag(
                    R.id.factor_name);
            if (sleItem.id.equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)) {
                gotoEditTextAct(sleItem);
            } else if (sleItem.id.equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
                startDVBCCNQuickFullScan();
            }
        }
    }
};


/*private void showInformDialogForVoo() {
    String info = mContext.getString(R.string.menu_c_scan_clear_channel);
    dialog = new LiveTVDialog(this, "", info, 3);
    dialog.setButtonYesName(mContext.getString(R.string.menu_ok));
    dialog.setButtonNoName(mContext.getString(R.string.menu_cancel));
    OnKeyListener onListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                    if (v.getId() == dialog.getButtonYes().getId()) {
                        dialog.dismiss();
                        startDVBCFullScan();
                    } else if (v.getId() == dialog.getButtonNo().getId()) {
                        dialog.dismiss();
                    }
                    return true;
                }
            }
            return false;
        }
    };
    dialog.bindKeyListener(onListener);
    dialog.show();
    dialog.getButtonNo().requestFocus();
}*/

private AccessibilityDelegate mAccDelegateForChList = new AccessibilityDelegate() {

    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
            AccessibilityEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
        do {
            if (mListView.equals(host)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "host:" + mListView + "," + host);
                break;
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":host =" + false);
            }
            List<CharSequence> texts = event.getText();
            if (texts == null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
                break;
            }
            for (int i = 0; i < texts.size(); i++) {
                System.out.println("" + i + "===" + texts.get(i));
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enventtype :" + event.getEventType());
            // confirm which item is focus
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move
                                                                                             // focus
                int index = findSelectItem(texts.get(texts.size() - 1).toString());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + index);
                if (index >= 0) {
                    mSelCelHandler.removeMessages(SCANL_LIST_SElECTED_FOR_SETTING_TTS);
                    Message msg = Message.obtain();
                    msg.what = SCANL_LIST_SElECTED_FOR_SETTING_TTS;
                    msg.arg1 = index;
                    mSelCelHandler.sendMessageDelayed(msg, 400);
                }
            }
        } while (false);

        try {//host.onRequestSendAccessibilityEventInternal(child, event);
            Class clazz = Class.forName("android.view.ViewGroup");
            java.lang.reflect.Method getter =
                clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
                    View.class, AccessibilityEvent.class);
            return (boolean)getter.invoke(host, child, event);
        } catch (Exception e) {
            Log.d(TAG, "Exception " + e);
        }
        return true;
    }

    private int findSelectItem(String text) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts =" + text);
        List<ScanFactorItem> scanList = mAdapter.getList();
        if (scanList == null) {
            return -1;
        }

        for (int i = 0; i < scanList.size(); i++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + scanList.get(i).title + " text = " + text);
            if (scanList.get(i).title.equals(text)) {
                return i;
            }
        }
        return -1;
    }
};

private Handler mSelCelHandler = new Handler() {
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SCANL_LIST_SElECTED_FOR_SETTING_TTS:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " hi SCAN_LIST_SElECTED_FOR_SETTING_TTS index  ==" + msg.arg1);
                mListView.setSelection(msg.arg1);
                break;
            default:
                break;
        }
    };
};

private List<ScanFactorItem> generateFactors() {
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    if(mActionID==null){
        return list;
    }
    if (mActionID.startsWith(MenuConfigManager.FACTORY_TV_RANGE_SCAN)) {
        list = loadUSSAEURangeScan();
    } else if (mActionID.equals(MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN)) {// for us&sa fac
                                                                               // sigle-rd scan
        list = loadUSSASingleScan();
    } else if (mActionID.equals(MenuConfigManager.TV_ANALOG_SCAN)) {
        list = loadAnalogManualScan();
    } else if (mActionID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CN)) {
        list = loadCEDTMBSingleRFScan();
    } else if (mActionID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)) {// eu dvbt single-rf
                                                                            // scan
        ScanFactorItem mSignalLevel;
        ScanFactorItem mSignalQuality;
        ScanFactorItem mSingleRFChannel;
        mSingleRFChannel = new ScanFactorItem(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS,
                mContext.getString(R.string.menu_tv_single_rf_channel), 0,
                new String[] {
                    mTv.getRFChannel(DVBTScanner.RF_CHANNEL_CURRENT)
                }, true);
        int mLevelValue = 0;
        int mQualValue = 0;
        mSignalLevel = new ScanFactorItem(MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_LEVEL,
                mContext.getString(R.string.menu_tv_single_signal_level), mLevelValue, false);
        mSignalQuality = new ScanFactorItem(MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_QUALITY,
                mContext.getString(R.string.menu_tv_single_signal_quality), mQualValue, false);
        list.add(mSingleRFChannel);
        list.add(mSignalLevel);
        list.add(mSignalQuality);
    } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBC)) {
        // eu dvbc channel-scan no operator
        list.addAll(initDVBCOperatorItem(CableOperator.OTHER));
        if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
            String others = mContext.getString(R.string.dvbc_operator_others);
            ScanContent.setOperator(mContext, others);
            startDVBCFullScan();
        }
    } else if (mActionID.startsWith(MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR)) {
        // eu dvbc channel-scan with operator
        String[] splt = mActionID.split("#");
        int val = Integer.parseInt(splt[1]);
        list.addAll(initDVBCOperatorItem(CableOperator.values()[val]));
    } else if (mActionID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)) {
        list.addAll(genDVBCRfScanItem());
    } else if (mActionID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e("DVBS_SAT_DEDATIL_INFO_SCAN");
        //list.addAll(genDVBSScanItem());
    } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {
        list = initDVBCCNQuickFullItem();
    }
    return list;
}

/*private boolean writeAutotestScanFile() {
    Log.d(TAG, "writeAutotestScanFile()");
    boolean succ = true;
    try {
        succ = false;
        File atvfile = new File(FileSystemPath.LINUX_TMP_PATH + "/autotest/scan_atv_with_signal");
        File dtvFile = new File(FileSystemPath.LINUX_TMP_PATH + "/autotest/scan_dtv_with_signal");
        if (TIFChannelManager.getInstance(mContext).hasATVChannels()) {
            succ = atvfile.createNewFile();
        }
        if (TIFChannelManager.getInstance(mContext).hasDTVChannels()) {
            succ = dtvFile.createNewFile();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return succ;
}*/

/**
 * DVBC CN quick/full scan
 *
 * @param parentItem
 */
private List<ScanFactorItem> initDVBCCNQuickFullItem() {
    // final String advanceStr = mContext.getString(R.string.menu_c_scan_mode_advance);
    final String fullStr = mContext.getString(R.string.menu_arrays_Full);
    final String quickStr = mContext.getString(R.string.menu_arrays_Quick);
    String[] scanModeArrayForDVBCCN = mContext.getResources().getStringArray(
            R.array.menu_scan_mode_cn);
    List<ScanFactorItem> addList = new ArrayList<ScanFactorItem>();
    final ScanFactorItem scanMode = new ScanFactorItem(MenuConfigManager.SCAN_MODE_DVBC,
            mContext.getString(R.string.menu_tv_sigle_scan_mode),
            0, scanModeArrayForDVBCCN, true);
    scanMode.value = scanModeArrayForDVBCCN[0];
    addList.add(scanMode);

    int scanLowFrq = DVBCCNScanner.mLowerFreq / 1000000;
    int scanUpperFreq = DVBCCNScanner.mHighFreq / 1000000;
    final ScanFactorItem frequecy = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_SCAN_FREQUENCY,
            mContext.getString(R.string.menu_c_rfscan_frequency_cn),
            scanLowFrq, scanLowFrq, scanUpperFreq, true);
    // addList.add(frequecy);

    final ScanFactorItem startScan = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN,
            mContext.getString(R.string.menu_c_scan), true);
    addList.add(startScan);

    scanMode.addScanOptionChangeListener(new ScanOptionChangeListener() {
        public void onScanOptionChange(String afterName) {
            if (afterName == null || afterName.equals("")) {
                return;
            }
            scanMode.value = afterName;
            List<ScanFactorItem> list = mAdapter.getList();
            list.remove(frequecy);

            if (afterName.equalsIgnoreCase(quickStr)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e("OptionValuseChangedCallBack>>>>", "notifcy ------");
                list.add(frequecy);
                list.remove(startScan);
                list.add(startScan);
            } else if (afterName.equalsIgnoreCase(fullStr)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing fullStr");
            }
            mAdapter.notifyDataSetChanged();
        }
    });
    return addList;
}

/**
 * load operator item and subitems
 *
 * @param parentItem operator item's parent
 * @param operator which operator to load
 * @param operatorList current country operator list
 * @return
 */
private List<ScanFactorItem> initDVBCOperatorItem(final CableOperator operator) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "start initDVBCOperatorItem() operator:" + operator.name());
    final String advanceStr = mContext.getString(R.string.menu_arrays_Advance);
    final String fullStr = mContext.getString(R.string.menu_arrays_Full);
    final String quickStr = mContext.getString(R.string.menu_arrays_Quick);

    //String[] scanModeArray = mContext.getResources().getStringArray(R.array.menu_scan_mode);
    final String[] scanModeArray = ScanContent.initScanModesForOperator(mContext, operator, null);
    //String name = ScanContent.getOperatorStr(mContext, operator);
    //String defaultName = MenuConfigManager.SCAN_MODE;
    List<ScanFactorItem> addList = new ArrayList<ScanFactorItem>();
    int scanModeIndex = getDVBCScanmodeIndex(scanModeArray);
    final ScanFactorItem scanMode = new ScanFactorItem(MenuConfigManager.SCAN_MODE_DVBC,
            mContext.getString(R.string.menu_tv_sigle_scan_mode),
            getDVBCScanmodeIndex(scanModeArray)/*mConfigManager.getDefaultScan(defaultName)*/, scanModeArray, true);
    addList.add(scanMode);
    if (scanModeArray.length == 1 || isBelgiumCableVoo()) {
        scanMode.isEnable = false;
    }

    String scanModeString = scanModeArray[scanModeIndex];
    scanMode.value = scanModeString;
    if(mContext.getString(R.string.menu_arrays_Advance).equals(scanModeString)){
        showHomeFrequencyToast();
    }

    if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
        final String[] scanTypeArray = mContext.getResources().getStringArray(R.array.menu_scan_type);
        final ScanFactorItem scanType = new ScanFactorItem(MenuConfigManager.TV_DVBC_SCAN_TYPE,
                mContext.getString(R.string.menu_channel_scan_type),
                0, scanTypeArray, true);
        scanType.value = scanTypeArray[0];
        scanType.addScanOptionChangeListener(new ScanOptionChangeListener() {
            public void onScanOptionChange(String afterName) {
                if (afterName == null || afterName.equals("")) {
                    return;
                }
                scanType.value = afterName;
                mAdapter.notifyDataSetChanged();
            }
        });
        addList.add(scanType);
    }

    int frequencyValue = ScanContent.getFrequencyOperator(mContext, scanModeString, operator);
    String autoFreqStr = "";
    if (frequencyValue == -1) {
        autoFreqStr = mContext.getString(R.string.menu_arrays_Auto);
    }else if(frequencyValue == -3){
        autoFreqStr = "00000";//DTV02413131
    }

    final ScanFactorItem frequecy = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_SCAN_FREQUENCY,
            mContext.getString(R.string.menu_c_rfscan_frequency),
            frequencyValue, 0, 999999, true);
    frequecy.value = autoFreqStr;
    /*if (cableOperator.equals(mContext.getString(R.string.dvbc_operator_digi))) {
        frequecy.isEnable = false;
    }*/
    if (frequencyValue != -2 && !fullStr.equals(scanMode.value)) {
        addList.add(frequecy);
    }

    int netWorkIdValue = ScanContent
            .getNetWorkIDOperator(mContext, scanModeString, operator);
    String autoNetworkIDStr = "";
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA)) {
        netWorkIdValue = -1;
    }
    if (netWorkIdValue == -1) {
        autoNetworkIDStr = mContext.getString(R.string.menu_arrays_Auto);
    } else if (netWorkIdValue == -3) {
        autoNetworkIDStr = "00000";//DTV02413131
    }
    if(isBelgiumCableVoo()) {//DTV02240515
        //netWorkIdValue = -1;
        autoNetworkIDStr = "";
    }
    final ScanFactorItem netWorkID = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_SCAN_NETWORKID,
            mContext.getString(R.string.menu_c_net),
            netWorkIdValue, 0, 999999, true);
    //if(isBelgiumCableVoo()) {
    //   netWorkID.value = "0000";
    //} else {
        netWorkID.value = autoNetworkIDStr;
    //}
    /*if (cableOperator.equals(mContext.getString(R.string.dvbc_operator_digi))) {
        netWorkID.isEnable = false;
    }*/
    if (netWorkIdValue != -2 && !fullStr.equals(scanMode.value)) {
        addList.add(netWorkID);
    }

    final ScanFactorItem scan = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN,
            mContext.getString(R.string.menu_c_scan), true);
    addList.add(scan);
    scanMode.addScanOptionChangeListener(new ScanOptionChangeListener() {
        public void onScanOptionChange(String afterName) {
            if (afterName == null || afterName.equals("")) {
                return;
            }
            scanMode.value = afterName;
            SaveValue.setLocalMemoryValue("DVBCScanMode",getIndexFromScanmodeName(afterName,scanModeArray));
            int frequencyValue = ScanContent
                    .getFrequencyOperator(mContext, afterName, operator);
            int netWorkIdValue = ScanContent
                    .getNetWorkIDOperator(mContext, afterName, operator);

            frequecy.inputValue = frequencyValue;
            netWorkID.inputValue = netWorkIdValue;

            frequecy.value = frequencyValue == -1 ? mContext.getString(R.string.menu_arrays_Auto) : "";
            netWorkID.value = netWorkIdValue == -1 ? mContext.getString(R.string.menu_arrays_Auto) : "";

            List<ScanFactorItem> list = mAdapter.getList();
            list.remove(frequecy);
            list.remove(netWorkID);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,String.format("afterName%s,frequencyValue:%s,netWorkIdValue:%s",
                    afterName, frequencyValue, netWorkIdValue));

            if (afterName.equalsIgnoreCase(quickStr)) {
                if (frequencyValue != -2) {
                    list.add(frequecy);
                }
                if (netWorkIdValue != -2) {
                    list.add(netWorkID);
                }
                list.remove(scan);
                list.add(scan);
            } else if (afterName.equalsIgnoreCase(advanceStr)) {
                if (frequencyValue != -2) {
                    list.add(frequecy);
                }
                if (netWorkIdValue != -2) {
                    list.add(netWorkID);
                }
                list.remove(scan);
                list.add(scan);
                showHomeFrequencyToast();
            } else if (afterName.equalsIgnoreCase(fullStr)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
            }
            mAdapter.notifyDataSetChanged();
        }
    });
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "end initDVBCOperatorItem() operator:" + operator.name());
    return addList;
}

private void showHomeFrequencyToast(){
    Message msg = Message.obtain();
    msg.what = MessageType.MENU_DVBC_SCAN_HOME_FREQUENCY_TIP;
    mHandler.sendMessageDelayed(msg, 1500);
}

/**
 * for some country that has no operator list
 */
int netWorkIdValue;
int frequencyValue;

/*private List<ScanFactorItem> initNoOparatorItem() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "start initNoOparatorItem()");
    final String advanceStr = mContext.getString(R.string.menu_arrays_Advance);
    final String fullStr = mContext.getString(R.string.menu_arrays_Full);
    final String quickStr = mContext.getString(R.string.menu_arrays_Quick);

    String[] scanModeArray = mContext.getResources().getStringArray(R.array.menu_scan_mode);
    CableOperator operator = CableOperator.OTHER;
    // List<CableOperator> operatorList = new ArrayList<CableOperator>();
    scanModeArray = ScanContent.initScanModesForOperator(mContext, operator, null);
    String defaultName = MenuConfigManager.SCAN_MODE;

    List<ScanFactorItem> addList = new ArrayList<ScanFactorItem>();
    final ScanFactorItem scanMode = new ScanFactorItem(MenuConfigManager.SCAN_MODE_DVBC,
            mContext.getString(R.string.menu_tv_sigle_scan_mode),
            mConfigManager.getDefaultScan(defaultName), scanModeArray, true);
    addList.add(scanMode);

    String scanModeString = scanModeArray[mConfigManager.getDefaultScan(defaultName)];
    scanMode.value = scanModeString;

    if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
        final String[] scanTypeArray = mContext.getResources().getStringArray(R.array.menu_scan_type);
        final ScanFactorItem scanType = new ScanFactorItem(MenuConfigManager.TV_DVBC_SCAN_TYPE,
                mContext.getString(R.string.menu_channel_scan_type),
                0, scanTypeArray, true);
        scanType.value = scanTypeArray[0];
        if(ScanContent.isCountryUK()){
            scanType.isEnable = false;
        }else {
            scanType.isEnable = true;
        }
        scanType.addScanOptionChangeListener(new ScanOptionChangeListener() {
            public void onScanOptionChange(String afterName) {
                if (afterName == null || afterName.equals("")) {
                    return;
                }
                scanType.value = afterName;
                mAdapter.notifyDataSetChanged();
            }
        });
        addList.add(scanType);
    }

    frequencyValue = ScanContent.getFrequencyOperator(mContext, scanModeString, operator);
    String autoFreqStr = "";
    if (frequencyValue == -1) {
        autoFreqStr = mContext.getString(R.string.menu_arrays_Auto);
    }

    final ScanFactorItem frequecy = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_SCAN_FREQUENCY,
            mContext.getString(R.string.menu_c_rfscan_frequency),
            frequencyValue, 0, 999999, true);
    frequecy.value = autoFreqStr;
    if (frequencyValue != -2) {
        addList.add(frequecy);
    }

    netWorkIdValue = ScanContent.getNetWorkIDOperator(mContext, scanModeString, operator);
    String autoNetworkIDStr = "";
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA)) {
        netWorkIdValue = -1;
    }
    if (netWorkIdValue == -1) {
        autoNetworkIDStr = mContext.getString(R.string.menu_arrays_Auto);
    } else if (netWorkIdValue == -3) {
        autoNetworkIDStr = "";
    }
    final ScanFactorItem netWorkID = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_SCAN_NETWORKID,
            mContext.getString(R.string.menu_c_net),
            netWorkIdValue, 0, 999999, true);
    netWorkID.value = autoNetworkIDStr;
    if (netWorkIdValue != -2) {
        addList.add(netWorkID);
    }
    final ScanFactorItem scan = new ScanFactorItem(
            MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN,
            mContext.getString(R.string.menu_c_scan), true);
    addList.add(scan);
    scanMode.addScanOptionChangeListener(new ScanOptionChangeListener() {
        public void onScanOptionChange(String afterName) {
            if (afterName == null || afterName.equals("")) {
                return;
            }
            scanMode.value = afterName;
            netWorkID.inputValue = netWorkIdValue;
            frequecy.inputValue = frequencyValue;
            List<ScanFactorItem> list = mAdapter.getList();
            if (afterName.equalsIgnoreCase(advanceStr) || afterName.equalsIgnoreCase(quickStr)) {
                if (!list.contains(frequecy)) {
                    if (list.contains(netWorkID)) {
                        list.remove(netWorkID);
                    }
                    list.add(frequecy);
                }
                if (!list.contains(netWorkID)) {
                    list.add(netWorkID);
                }
                list.remove(scan);
                list.add(scan);
            } else if (afterName.equalsIgnoreCase(fullStr)) {
                list.remove(frequecy);
                list.remove(netWorkID);
            }
            mAdapter.notifyDataSetChanged();
        }
    });
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "end initNoOparatorItem()");
    return addList;
}*/

/**
 * load US/SA/EU factory range scan
 */
private List<ScanFactorItem> loadUSSAEURangeScan() {
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    ScanFactorItem mScanMode;
    ScanFactorItem mfromChItem;
    ScanFactorItem mtoChItem;
    if (!CommonIntegration.isEURegion()) {
        mfromChItem = new ScanFactorItem(MenuConfigManager.FAV_US_RANGE_FROM_CHANNEL,
                mContext.getString(R.string.menu_tv_sigle_from_channel)
                , "" + mTv.getFirstScanIndex(), true);
        mtoChItem = new ScanFactorItem(MenuConfigManager.FAV_US_RANGE_TO_CHANNEL,
                mContext.getString(R.string.menu_tv_sigle_to_channcel)
                , "" + mTv.getLastScanIndex(), true);
        this.freFrom = mTv.getFirstScanIndex();
        this.freTo = mTv.getLastScanIndex();
        if (CommonIntegration.isUSRegion() &&mActionID!=null&& mActionID.equals(MenuConfigManager.FACTORY_TV_RANGE_SCAN)) {
            String[] mScanModes = mContext.getResources().getStringArray(
                    R.array.menu_tv_us_scan_mode_array);
            mScanMode = new ScanFactorItem(MenuConfigManager.US_SCAN_MODE,
                    mContext.getString(R.string.menu_tv_sigle_scan_mode)
                    , mTv.getUSRangeScanMode(), mScanModes, true);
            list.add(mScanMode);
        }

    } else {
        char[] chs = allRFChannels[allRFChannels.length - 1].toCharArray();
        for (char ch : chs) {
            int x = chs[2];
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convert ch:" + ch + "after:" + x);
        }
        mfromChItem = new ScanFactorItem(MenuConfigManager.FAV_US_RANGE_FROM_CHANNEL,
                mContext.getString(R.string.menu_tv_sigle_from_channel)
                , allRFChannels[0], true);
        mtoChItem = new ScanFactorItem(MenuConfigManager.FAV_US_RANGE_TO_CHANNEL,
                mContext.getString(R.string.menu_tv_sigle_to_channcel)
                , allRFChannels[allRFChannels.length - 1], true);
        this.freFrom = 0;
        this.freTo = allRFChannels.length - 1;
    }
    list.add(mfromChItem);
    list.add(mtoChItem);
    return list;
}

/**
 * for CN CE/DTMB single RF scan
 */
private List<ScanFactorItem> loadCEDTMBSingleRFScan() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start call loadCEDTMBSingleRF:");
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    String[] scanModulation = mContext.getResources().getStringArray(
            R.array.menu_tv_scan_mode_array);
    int tunerMode = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tunerMode:" + tunerMode);
    int scanLowFrq = DVBCCNScanner.mLowerFreq / 1000000;
    int scanUpperFreq = DVBCCNScanner.mHighFreq / 1000000;
    if (tunerMode == CommonIntegration.DB_CAB_OPTID) {
        list.add(new ScanFactorItem(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY,
                mContext.getString(R.string.menu_tv_rf_scan_frequency),
                scanLowFrq, scanLowFrq, scanUpperFreq, true));
        // symbol rate
        list.add(new ScanFactorItem(MenuConfigManager.SYM_RATE,
                mContext.getString(R.string.menu_tv_rf_sym_rate),
                ScanContent.getSystemRate(), 1000, 9999, true));
        // scan modulation
        list.add(new ScanFactorItem(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION,
                mContext.getString(R.string.menu_tv_rf_scan_mode),
                saveV.readValue(MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION),
                scanModulation, true));
        // begin scan
        ScanFactorItem startScan = new ScanFactorItem(
                MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN,
                mContext.getString(R.string.menu_tv_status_value), true);
        list.add(startScan);
    } else {
        ScanFactorItem rfFChannel = new ScanFactorItem(
                MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL,
                mContext.getString(R.string.menu_tv_single_rf_channel),
                0, new String[] {
                    mTv.getCNRFChannel(DVBTCNScanner.RF_CHANNEL_CURRENT)
                }, true);
        list.add(rfFChannel);

        int mSignalLevel = editChannel.getSignalLevel();
        ScanFactorItem signalLevelDateItem = new ScanFactorItem(
                MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_LEVEL,
                mContext.getString(R.string.menu_tv_single_signal_level),
                mSignalLevel, false);
        list.add(signalLevelDateItem);

        int mSignalQuality = editChannel.getSignalQuality();
        String[] mSignalQualityStr = mContext.getResources()
                .getStringArray(R.array.menu_setup_tv_single_signal_quality);
        ScanFactorItem signalQuality = new ScanFactorItem(
                MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_QUALITY,
                mContext.getString(R.string.menu_tv_single_signal_quality),
                mSignalQuality, mSignalQualityStr, false);
        list.add(signalQuality);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end call loadCEDTMBSingleRF:");
    return list;
}

/**
 * EU/CN analog manual scan items
 *
 * @return
 */
private List<ScanFactorItem> loadAnalogManualScan() {
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    if (CommonIntegration.isCNRegion()) {
        ScanFactorItem mScanStratFreqency;
        ScanFactorItem mScanEndFreqency;
        ScanFactorItem mStartScan;
        int startFrq = EUATVScanner.FREQ_LOW_VALUE;
        // setFreFrom(startFrq * 1000000);
        mScanStratFreqency = new ScanFactorItem(MenuConfigManager.TV_CHANNEL_START_FREQUENCY,
                mContext.getString(R.string.menu_tv_start_frequency), startFrq,
                EUATVScanner.FREQ_LOW_VALUE, EUATVScanner.FREQ_HIGH_VALUE, true);
        // int endFrq = 865;
        // setFreTo(endFrq * 1000000);
        mScanEndFreqency = new ScanFactorItem(MenuConfigManager.TV_CHANNEL_END_FREQUENCY,
                mContext.getString(R.string.menu_tv_end_frequency),
                EUATVScanner.FREQ_HIGH_VALUE,
                EUATVScanner.FREQ_LOW_VALUE, EUATVScanner.FREQ_HIGH_VALUE, true);
        mStartScan = new ScanFactorItem(MenuConfigManager.TV_CHANNEL_STARTSCAN,
                mContext.getString(R.string.menu_tv_status_value), true);
        list.add(mScanStratFreqency);
        list.add(mScanEndFreqency);
        list.add(mStartScan);
    } else if (CommonIntegration.isEURegion()) {
        ScanFactorItem mScanFreqency;
        ScanFactorItem mScanUp;
        ScanFactorItem mScanDown;
        int startFrq = mTv.getInitATVFreq();
        String freqStr = String.format("%s%7.2f %s", mContext
                .getString(R.string.menu_tv_freq), (float) (startFrq / 1.0e6), mContext
                .getString(R.string.menu_tv_rf_scan_frequency_mhz));
        mNumberFromTo.setText(freqStr);
        mNeedChangeStartFrq = mTv.isCurrentAtvPlaying();
        mScanFreqency = new ScanFactorItem(MenuConfigManager.TV_CHANNEL_START_FREQUENCY,
                mContext.getString(R.string.menu_tv_start_frequency)
                , startFrq / 1000000, mTv.getAtvLowFreq() / 1000000, EUATVScanner.FREQ_HIGH_VALUE, true);
        mScanUp = new ScanFactorItem(MenuConfigManager.TV_ANANLOG_SCAN_UP,
                mContext.getString(R.string.menu_c_analogscan_scanup), true);
        mScanDown = new ScanFactorItem(MenuConfigManager.TV_ANANLOG_SCAN_DOWN,
                mContext.getString(R.string.menu_c_analogscan_scandown), true);
        list.add(mScanFreqency);
        list.add(mScanUp);
        list.add(mScanDown);
    }
    return list;
}

/**
 * load US/SA factory single RF scan items
 */
private List<ScanFactorItem> loadUSSASingleScan() {
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    ScanFactorItem mSignalLevel;
    ScanFactorItem mSignalQuality;
    ScanFactorItem mFreqPlan;
    ScanFactorItem mSingleMod;
    ScanFactorItem mSingleRFChannel;

    int rfIndex = mTv.getRFScanIndex();
    mSingleRFChannel = new ScanFactorItem(MenuConfigManager.FAV_US_SINGLE_RF_CHANNEL,
            mContext.getString(R.string.menu_tv_single_rf_channel), String.valueOf(rfIndex),
            true);
    this.freFrom = rfIndex;

    int mlevel = editChannel.getSignalLevel();
    mSignalLevel = new ScanFactorItem(MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_LEVEL,
            mContext.getString(R.string.menu_tv_single_signal_level), mlevel, false);

    if (CommonIntegration.isSARegion()) {
        int qualityValue = editChannel.getSignalQuality();
        String[] mSignalQualityStrs = mContext.getResources().getStringArray(
                R.array.menu_setup_tv_single_signal_quality);
        mSignalQuality = new ScanFactorItem(MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_QUALITY,
                mContext.getString(R.string.menu_tv_single_signal_quality)
                , qualityValue, mSignalQualityStrs, false);
        list.add(mSingleRFChannel);
        list.add(mSignalLevel);
        list.add(mSignalQuality);
    } else if (CommonIntegration.isUSRegion()) {
        // scan frequency
        String[] freplans = mContext.getResources().getStringArray(
                R.array.menu_tv_scan_frequency_plan_array);
        String[] modulationarrys = mContext.getResources().getStringArray(
                R.array.menu_tv_scan_modulation_array);

        int tunerMode = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);

        String name = MenuConfigManager.TV_SINGLE_SCAN_MODULATION;
        mSingleMod = new ScanFactorItem(name,
                mContext.getString(R.string.menu_tv_sigle_modulation)
                , saveV.readValue(name), modulationarrys, false);
        name = MenuConfigManager.FREQUENEY_PLAN;
        mFreqPlan = new ScanFactorItem(name,
                mContext.getString(R.string.menu_tv_sigle_frequency_plan)
                , mConfigManager.getDefault(name), freplans, false);

        if (tunerMode == MtkTvConfigTypeBase.BS_SRC_AIR) {
            mSingleMod.isEnable = false;
            mFreqPlan.isEnable = false;
        } else {
            mSingleMod.isEnable = true;
            mFreqPlan.isEnable = true;
        }
        list.add(mSingleRFChannel);
        list.add(mFreqPlan);
        list.add(mSingleMod);
        list.add(mSignalLevel);
    }
    return list;
}

/**
 * generate dvbc's single-rf scan 's factor item
 *
 * @return
 */
private List<ScanFactorItem> genDVBCRfScanItem() {
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    ScanFactorItem freqency = new ScanFactorItem(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ,
            mContext.getString(R.string.menu_c_rfscan_frequency),
            mTv.getInitDVBCRFFreq(), 100, 999999, true);
    // Modulation , sub of RF
    String[] scanMode = mContext.getResources().getStringArray(
            R.array.menu_tv_scan_mode_array);
    /*ScanFactorItem modulation = new ScanFactorItem(
            MenuConfigManager.DVBC_SINGLE_RF_SCAN_MODULATION,
            mContext.getString(R.string.menu_tv_sigle_modulation),
            saveV.readValue(MenuConfigManager.SCAN_MODE), scanMode, true);*/
    // SymbolRate , sub of RF 222
    /*ScanFactorItem symbolRate = new ScanFactorItem(MenuConfigManager.SYM_RATE,
            mContext.getString(R.string.menu_c_rfscan_symbol_rate),
            ScanContent.getSystemRate(), 1000, 9999, true);*/
    ScanFactorItem scan = new ScanFactorItem(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN,
            mContext.getString(R.string.menu_c_scan), true);

    // signal level , sub of RF 222
    int mLevelValue = editChannel.getSignalLevel();
    if (CommonIntegration.getInstance().isCurrentSourceATV()) {
        mLevelValue = 0;
    }
    ScanFactorItem mSignalLevel = new ScanFactorItem(
            MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_LEVEL,
            mContext.getString(R.string.menu_tv_single_signal_level), mLevelValue, false);

    int mQualValue = editChannel.getSignalQuality();
    if (CommonIntegration.getInstance().isCurrentSourceATV()) {
        mQualValue = 0;
    }
    ScanFactorItem mSignalQuality = new ScanFactorItem(
            MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_QUALITY,
            mContext.getString(R.string.menu_tv_single_signal_quality), mQualValue, false);
    list.add(freqency);
    //list.add(modulation);
    //list.add(symbolRate);
    list.add(scan);
    list.add(mSignalLevel);
    list.add(mSignalQuality);
    return list;
}

private void showTKGSUserMessageDialog(String message) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTKGSUserMessageDialog>>>");
    TKGSUserMessageDialog dialog = new TKGSUserMessageDialog(mContext);
    dialog.showConfirmDialog(message);

}

/**
 * gen for dvbs 's scan item
 *
 * @return
 */
/*private List<ScanFactorItem> genDVBSScanItem() {
    List<ScanFactorItem> list = new ArrayList<ScanFactorItem>();
    List<String> scanModeList = ScanContent.getDVBSScanMode(mContext);
    List<String> scanChannels = ScanContent.getDVBSConfigInfoChannels(mContext);
    List<String> scanStores = ScanContent.getDVBSConfigInfoChannelStoreTypes(mContext);
    String scanModeTitle = mContext.getResources().getString(R.string.dvbs_scan_mode);
    ScanFactorItem scanMode = new ScanFactorItem(
            MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_CONFIG,
            scanModeTitle, 0,
            (String[]) scanModeList.toArray(new String[0]), true);
    if (scanModeList.size() > 1) {
        scanMode.isEnable = true;
    } else {
        scanMode.isEnable = false;
    }
    String channelsTitle = mContext.getResources().getString(R.string.dvbs_satellite_channel);
    String storeTypeTitle = mContext.getResources().getString(R.string.dvbs_satellite_channel_store_type);
    ScanFactorItem scanType = new ScanFactorItem(
            MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_SCAN_CONFIG,
            channelsTitle, 0,
            (String[]) scanChannels.toArray(new String[0]), true);
    //String storeTypes = mContext.getResources().getString(R.string.dvbs_satellite_channel_store_type);
    ScanFactorItem storeType = new ScanFactorItem(
        MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN_STORE_CONFIG,
        storeTypeTitle, 0,
        (String[]) scanStores.toArray(new String[0]), true);
    ScanFactorItem scan = new ScanFactorItem(
            MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN,
            mContext.getString(R.string.menu_c_scan), true);
    list.add(scanMode);
    list.add(scanType);
    list.add(storeType);
    list.add(scan);
    return list;
}*/

private void initText(String itemID) {
    mItemID = itemID;
    if (itemID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN)) {
        this.mStateTextView.setText(mContext.getString(R.string.menu_tv_scann_allchannels));
        final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
        this.mDVBSChannels.setText(digitalNum);
        this.mAnaloguechannel.setText("");
    } else if (mActionID!=null&&mActionID.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {
        this.mStateTextView.setText(mContext
                .getString(R.string.menu_tv_scan_digital_channel_init));
    } else if (mTv.isCurrentSourceDTV()) {
        this.mStateTextView.setText(mContext.getString(R.string.menu_tv_single_rf_scan_init));
    } else {
        this.mStateTextView.setText(mContext
                .getString(R.string.menu_tv_analog_manual_scan_init));
    }
    this.loading.setVisibility(View.INVISIBLE);
    this.setScanProgress(0);

    progressBar.setVisibility(View.VISIBLE);
    mFinishpercentage.setVisibility(View.VISIBLE);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"test-0:" + itemID);
    mDVBSChannels.setVisibility(View.GONE);
    if (itemID.equals(MenuConfigManager.FACTORY_TV_RANGE_SCAN)) {
        mStateTextView.setText(R.string.menu_tv_us_range_scan);
        mTunerModeView.setText(mTnuerArr[mTv.getCurrentTunerMode()]);
        checkScanMode();
        mNumberFromTo.setVisibility(View.INVISIBLE);
    } else if (itemID.equals(MenuConfigManager.FACTORY_TV_SINGLE_RF_SCAN)
            || itemID.equals(MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN)
            || itemID.equals(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN)
            || itemID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CN)) {
        mStateTextView.setText(R.string.menu_tv_single_rf_scan_init);
        mTunerModeView.setText(mTnuerArr[mTv.getCurrentTunerMode()]);
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
        mNumberFromTo.setVisibility(View.INVISIBLE);
    } else if (itemID.equals(MenuConfigManager.FACTORY_TV_RANGE_SCAN_DIG)) {
        mStateTextView.setText(R.string.menu_tv_us_range_scan);
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
        mTunerModeView.setVisibility(View.INVISIBLE);
        mNumberFromTo.setVisibility(View.VISIBLE);
        mNumberFromTo.setText("");
    } else if (itemID.equals(MenuConfigManager.FACTORY_TV_RANGE_SCAN_ANA)) {
        mStateTextView.setText(R.string.menu_tv_us_range_scan);
        mAnaloguechannel.setVisibility(View.VISIBLE);
        mNumberChannel.setVisibility(View.INVISIBLE);
        mTunerModeView.setVisibility(View.INVISIBLE);
        mNumberFromTo.setVisibility(View.INVISIBLE);
    } else if (itemID.equals(MenuConfigManager.TV_ANALOG_SCAN)) {
        mStateTextView.setText(R.string.menu_tv_analog_manual_scan_init);
        mNumberChannel.setVisibility(View.INVISIBLE);
        mTunerModeView.setVisibility(View.INVISIBLE);
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        mFinishpercentage.setVisibility(View.INVISIBLE);
        mNumberFromTo.setVisibility(View.VISIBLE);
    } else if (itemID.startsWith(MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR)) {
        mStateTextView.setText(R.string.menu_tv_scann_allchannels);
        mNumberChannel.setVisibility(View.INVISIBLE);
        mTunerModeView.setVisibility(View.INVISIBLE);
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        mNumberChannel.setVisibility(View.INVISIBLE);
        mNumberFromTo.setVisibility(View.GONE);
    } else if (itemID.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBC)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"test-1:" + MenuConfigManager.TV_CHANNEL_SCAN_DVBC);
        mStateTextView.setText(R.string.menu_tv_scann_allchannels);
        mNumberChannel.setVisibility(View.VISIBLE);
        mTunerModeView.setVisibility(View.VISIBLE);
        mAnaloguechannel.setVisibility(View.VISIBLE);
        mNumberFromTo.setVisibility(View.GONE);
    } else if (mActionID!=null&&mActionID.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {
        mAnaloguechannel.setVisibility(View.INVISIBLE);
    } else {
        mNumberChannel.setVisibility(View.INVISIBLE);
        mTunerModeView.setVisibility(View.INVISIBLE);
        mNumberFromTo.setVisibility(View.INVISIBLE);
    }
    mNumberChannel.setText(mContext.getString(R.string.menu_setup_channel_scan_dig));
    mAnaloguechannel.setText(mContext.getString(R.string.menu_setup_channel_scan_ana));

    mTunerMode = mTv.getCurrentTunerMode();
    if (mTunerMode >= CommonIntegration.DB_SAT_OPTID) {
        mAnaloguechannel.setText("");
        mNumberChannel.setText("");
    }
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
        if (mTv.isCurrentSourceATV()) {
            mNumberChannel.setText("");
        } else if (mTv.isCurrentSourceDTV()) {
            mAnaloguechannel.setText("");
        }
    }
    if(ScanContent.isCountryUK()){
        mAnaloguechannel.setText("");
    }
}

private int checkScanMode() {
    int scanmode = mTv.getUSRangeScanMode();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkScanMode:" + scanmode);
    if (scanmode == 0) {
        mAnaloguechannel.setVisibility(View.VISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
    } else if (scanmode == 1) {
        mAnaloguechannel.setVisibility(View.VISIBLE);
        mNumberChannel.setVisibility(View.INVISIBLE);
    } else {
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
    }
    return scanmode;
}

private void setScanProgress(int progress) {
    this.mFinishpercentage.setText(String.format("%3d%s",progress,"%"));
    progressBar.setProgress(progress);
}

public void cancelScan() {
    mTv.cancelScan();
    if (CommonIntegration.isEURegion() && mItemID != null
            && (mItemID.equals(MenuConfigManager.FAV_US_RANGE_FROM_CHANNEL)
            || mItemID.equals(MenuConfigManager.FAV_US_RANGE_TO_CHANNEL))) {
        mTv.forOnlyEUdvbtCancescan();
    }
    isScanning = false;
    loading.stopDraw();
    nowProgress = 0;
    mStateTextView.setText(mContext.getString(R.string.menu_tv_analog_manual_scan_cancel));
}

private void facRangeScan(String itemID, String actionId) {
    mTv.addScanCallBackListener(mHandler);
    mTv.setScanListener(null);
    mLastChannelID = editChannel.getCurrentChannelId();
    mScanType = FAV_RANGE_RF_CHANNEL;
    mItemID = itemID;
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "addScanCallBackListener mLastChannelID:" + mLastChannelID + ">>" + mItemID);
    if (actionId.startsWith(MenuConfigManager.FACTORY_TV_RANGE_SCAN)) {
        if (CommonIntegration.isUSRegion()) {
            int scanMode = checkScanMode();
            if (scanMode == 0) {
                mCanCompleteScan = false;
            } else {
                mCanCompleteScan = true;
            }
            mTv.startUsRangeScan(this.freFrom, this.freTo);
        } else if (CommonIntegration.isSARegion()) {
            mAddedChannelCount = -1;
            mNumberFromTo.setText(this.freFrom + "/" + this.freTo);
            mTv.startSaRangleScan(this.freFrom, this.freTo, actionId);
        } else if (CommonIntegration.isEURegion()) {
            mAddedChannelCount = -1;
            mTv.startEURangleScan(this.freFrom, this.freTo);
        }

    }
}

private void singleRFChannelScan(int rfchannel, String itemID) {
    mTv.stopTimeShift();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopTimeShift!!!!!!");
    mTv.addScanCallBackListener(mHandler);
    mTv.setScanListener(null);
    // mTv.addSingleLevelCallBackListener(mHandler);
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "addScanCallBackListene itemID:" + itemID + ">>>" + rfchannel);
    mLastChannelID = editChannel.getCurrentChannelId();
    mScanType = FAV_US_SINGLE_RF_CHANNEL;
    if (itemID.equals(MenuConfigManager.FAV_US_SINGLE_RF_CHANNEL)
            || itemID.equals(MenuConfigManager.FREQUENEY_PLAN)
            || itemID.equals(MenuConfigManager.TV_SINGLE_SCAN_MODULATION)) {
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
        mNumberChannel.setText(mContext.getString(R.string.menu_setup_channel_scan_dig));
        this.setScanProgress(0);
        mCanCompleteScan = true;
        mTv.startUsSaSingleScan(rfchannel);
    }
}

private boolean isScanning = false;

/**
 * for US/SA/EU factory range scan
 *
 * @param itemID action id
 */
private void startFacRangeScan(String itemID, String actionId) {
    mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
    isScanning = true;
    // fix cr 00619735 some times scan callback progress not immediately
    this.mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    this.mFinishpercentage.setText(String.format("%3d%s",0,"%"));
    this.setScanProgress(0);
    this.loading.setVisibility(View.VISIBLE);
    this.loading.drawLoading();
    if (freFrom > freTo) {
        int from = freFrom;
        int to = freTo;
        mFromChannel = (TextView) mListView.getChildAt(0).findViewById(R.id.factor_num);
        mToChannel = (TextView) mListView.getChildAt(1).findViewById(R.id.factor_num);
        mFromChannel.setText(allRFChannels[to]);
        mToChannel.setText(allRFChannels[from]);
        this.freFrom = to;
        this.freTo = from;
    }
    this.facRangeScan(itemID, actionId);
}

/**
 * for US/SA factory single RF scan
 *
 * @param itemID the id of selected item
 */
private void startFacSingleScan(String itemID) {
    if(!mTv.isScanning()){
        if (isSelectedChannel) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("yiqinghuang", "startFacSingleScan" + isSelectedChannel);
            isSelectedChannel = false;
            mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
            isScanning = true;
            // fix cr 00619735 some times scan callback progress not immediately
            mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));

            loading.setVisibility(View.VISIBLE);
            loading.drawLoading();
            // setHandler(mHandler);
            singleRFChannelScan(this.freFrom, itemID);
        }
    }else {
        Toast.makeText(this, R.string.menu_string_toast_scanning_background, Toast.LENGTH_SHORT).show();
    }
}

/**
 * TK hasn't "SCAN UP/SCAN DOWN" for EU Analog Manual Scan
 *
 * @param scanUp (true==scanUp,false==ScanDown)
 */
private void startAnalogScan(boolean scanUp, String freq) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startAnalogScan()");
    isScanning = true;
    mATVFreq = 0;
    prepareUIBeforeScan();
    mAnaloguechannel.setVisibility(View.INVISIBLE);
    mNumberChannel.setVisibility(View.INVISIBLE);
    ScanParams params = prepareScanUpDownParam(scanUp, freq);
    loading.setVisibility(View.VISIBLE);
    loading.drawLoading();
    if (scanUp) {
        mAnaloguechannel.setText(mContext.getString(R.string.menu_c_analogscan_scanup));
        setScanProgress(0);
        // Register listener, write tv-api
        mTv.getScanManager().startScan(ScannerManager.ATV_SCAN_UP, mScanListener, params);
    } else {
        mAnaloguechannel.setText(mContext.getString(R.string.menu_c_analogscan_scandown));
        setScanProgress(0);
        // Register listener, write tv-api
        mTv.getScanManager().startScan(ScannerManager.ATV_SCAN_DOWN, mScanListener, params);
    }
}

/**
 * for CN Analog Manual Scan
 */
private void startAnalogATVFreqRangeScan(int startFreq, int endFreq) {
    if (startFreq == endFreq) {
        Toast.makeText(mContext, "Start Frenqency is same as End Frequency.Please Change.",
                Toast.LENGTH_SHORT).show();
        return;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startAnalogATVFreqRangeScan()");
    isScanning = true;
    mNumberChannel.setVisibility(View.INVISIBLE);
    mNumberFromTo.setVisibility(View.INVISIBLE);
    mAnaloguechannel.setVisibility(View.VISIBLE);
    mAnaloguechannel.setText(this.getString(R.string.menu_setup_channel_scan_ana));
    ScanParams params = prepareATVFreqRangeScanParam(startFreq, endFreq);
    setScanProgress(0);
    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    this.loading.drawLoading();
    // Register listener, write tv-api
    mTv.getScanManager().startScan(ScannerManager.ATV_RANGE_FREQ_SCAN, mScanListener, params);
}

/**
 * for EU/CN DVBT single RF scan
 */
private void startSingleRFScan(int rfChannel) {
    isScanning = true;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG + "_EU", "startSingleRFScan()");
    prepareUIBeforeScan();
    mTv.startSingleRFScan(rfChannel, mScanListener);
}

/**
 * to do EU/CN DVBC single RF scan
 */
private void startDVBCSingleRFScan() {
    isScanning = true;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBCSingleRFScan()");
    prepareUIBeforeScan();
    mTv.startScanByScanManager(ScannerManager.SIGNAL_RF_SCAN,
            mScanListener, prepareSingleRFScanParam());
}

/**
 * for DVBC CN scan full/quick
 */
private void startDVBCCNQuickFullScan() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBCCNQuickScan()");
    isScanning = true;
    dvbcParams = new ScanParams();
    dvbcParams.dvbcScanMode = ScanParams.DvbcScanMode.FULL;
    try {
        // full/qucik/
        dvbcParams = prepareDVBCScanParam();
        if (dvbcParams.freq > 0) {
            dvbcParams.freq = dvbcParams.freq * 1000000;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    prepareUIBeforeScan();
    // only scan DVBC CN DTV channels. only quick and full
    mTv.startScanByScanManager(ScannerManager.DTV_SCAN, mScanListener, dvbcParams);
}

/**
 * for EU DVBC full scan since no cable operator
 */
private void startDVBCFullScan() {
    isScanning = true;
    dvbcParams = new ScanParams();
    dvbcParams.dvbcScanMode = ScanParams.DvbcScanMode.FULL;
    dvbcParams.freq = 306000;
    dvbcParams.networkID = 999;
    // List<DataItem> items=mListViewSelectedItemData.getmParentGroup();
    try {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----------------------");
        // debug:
        // full/qucik/advance

        // prepare Value.
        // id==network, get value
        // id==frequency ,get value
        dvbcParams = prepareDVBCScanParam();

        if (dvbcParams.freq == -3 || dvbcParams.networkID == -3) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"Please set frequency/networkID firstly!!!");
            isScanning = false;
            Toast.makeText(this, "Please set frequency/networkID firstly!", 0).show();
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBCFullScan()");
    prepareUIBeforeScan();
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
        if (mTv.isCurrentSourceATV()) {
            mAnaloguechannel.setVisibility(View.VISIBLE);
            mNumberChannel.setVisibility(View.INVISIBLE);
        } else if (mTv.isCurrentSourceDTV()) {
            mAnaloguechannel.setVisibility(View.INVISIBLE);
            mNumberChannel.setVisibility(View.VISIBLE);
        }
    }else if(dvbcParams.dvbcScanType == DvbcScanType.DIGITAL){
        mAnaloguechannel.setVisibility(View.INVISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
    } else {
        mAnaloguechannel.setVisibility(View.VISIBLE);
        mNumberChannel.setVisibility(View.VISIBLE);
    }
    final String analogNum = mContext.getString(R.string.menu_tv_analog_channels);

    mAnaloguechannel.setText(String.format("%s%3d", analogNum + ":", 0));

    // only scan DVBC DTV channels.
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
        if (mTv.isCurrentSourceATV()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startATV_SCAN!!");
            mTv.startScanByScanManager(ScannerManager.ATV_SCAN, mScanListener, dvbcParams);
        } else if (mTv.isCurrentSourceDTV()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDTV_SCAN!!");
            mTv.startScanByScanManager(ScannerManager.DTV_SCAN, mScanListener, dvbcParams);
        }
    } else if(mTv.isUKCountry()){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "uk only startDTV_SCAN!!");
        //EditChannel.getInstance(mContext).cleanChannelList();
        mTv.startScanByScanManager(ScannerManager.DTV_SCAN, mScanListener, dvbcParams);
    }else if(dvbcParams.dvbcScanType == DvbcScanType.DIGITAL){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DIGITAL dvbcScanType startDTV_SCAN!!!");
        mTv.getScanManager().setRollbackCleanChannel();
        mTv.startScanByScanManager(ScannerManager.DTV_SCAN, mScanListener, dvbcParams);
    }else{
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FULL_SCAN!!");
        mTv.startScanByScanManager(ScannerManager.FULL_SCAN, mScanListener, dvbcParams);
    }
}

    public void startDVBSFullScan(int satID, int batID, int tkgsId) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBSFullScan mLocationID="+mLocationID);
        startDVBSFullScan(satID,batID,tkgsId,mLocationID);
    }

public void restartFastDVBSFullScan(boolean spclRegnSetup, String selectedOpName){
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "restartDVBSFullScan() spclRegnSetup="+spclRegnSetup+","+selectedOpName+",isFromFstVersionChgDialog="+isFromFstVersionChgDialog);
    if(mSatID != -1){
        mIsNeedShowFstRegion = spclRegnSetup;
        mTv.getScanManager().setFastScanSecondScan(true);
        SaveValue.writeWorldStringValue(this, "FAST_SCAN_SELECTED_OPT", selectedOpName, true);
        startDVBSFullScan(mSatID, -1, mLocationID);
    }else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "restartDVBSFullScan() fail, satId ="+mSatID+","+mLocationID);
    }
}

/**
 * DVBS Full/Network Scan
 */
public void startDVBSFullScan(int satID, int batID, String mLocationId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBSFullScan(),batID:" + batID + ">>>" + satID + ">>>" + mLocationId);
    isScanning = true;
    prepareUIBeforeDVBSScan();
    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    mNumberFromTo.setVisibility(View.INVISIBLE);
    dvbsParams = prepareDVBSParam(satID, mLocationId);
    mDvbsUpdateScanChNum = 0;
    mTv.getScanManager().cleanDvbsNum();

    if (batID != -1) {
        ((DVBSSettingsInfo) dvbsParams).BATId = batID;
    }
    //final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
    setScanProgress(0);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTv.isM7ScanMode():" + mTv.isM7ScanMode());
    if (mTv.isM7ScanMode()) {
        mTv.getScanManager().startScan(ScannerManager.M7_Channel_Scan, mScanListener, dvbsParams);
    } else {
        mTv.getScanManager().startScan(ScannerManager.FULL_SCAN, mScanListener, dvbsParams);
    }
}

private void updateSatelliteName() {
    mAnaloguechannel.setText(getString(R.string.menu_setup_satellite_name)
            + mTv.getScanManager().getFirstSatName());
    mNumberChannel.setText(getSatName());
}

public void startDVBSFullScan(int satID, int batID, int tkgsId, String mLocationId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBSFullScan(),batID:" + batID + ">>>" + satID + ">>>" + tkgsId + ">>>"
            + mLocationId);
    isScanning = true;
    prepareUIBeforeScan();
    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    mNumberFromTo.setVisibility(View.INVISIBLE);
    dvbsParams = prepareDVBSParam(satID, mLocationId);
    mDvbsUpdateScanChNum = 0;
    mTv.getScanManager().cleanDvbsNum();

    if (batID != -1) {
        ((DVBSSettingsInfo) dvbsParams).BATId = batID;
    }
    if (tkgsId != -1) {
        ((DVBSSettingsInfo) dvbsParams).tkgsType = tkgsId;
    }
    final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
    setScanProgress(0);
    mTv.getScanManager().startScan(ScannerManager.FULL_SCAN, mScanListener, dvbsParams);
    mDVBSChannels.setVisibility(View.VISIBLE);
    mDVBSChannels.setText(String.format("%s%3d", digitalNum + ":", 0));
    mAnaloguechannel.setVisibility(View.VISIBLE);
    mAnaloguechannel.setText(getString(R.string.menu_setup_satellite_name) +
            mTv.getScanManager().getFirstSatName());
    mNumberChannel.setText(getSatName());
}

public void reStartDVBSFullScanAfterTricolorChannelList(int i4BatID) {
  if(dvbsParams!=null && ((DVBSSettingsInfo) dvbsParams).i4BatID == -1) {
    isScanning = true;
    prepareUIBeforeScan();
    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
    setScanProgress(0);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reStartDVBSFullScanAfterTricolorChannelList i4BatID:"+i4BatID);
    ((DVBSSettingsInfo) dvbsParams).i4BatID = i4BatID;
    mTv.getScanManager().startScan(ScannerManager.FULL_SCAN, mScanListener, dvbsParams);
    mDVBSChannels.setVisibility(View.VISIBLE);
    mDVBSChannels.setText(String.format("%s%3d", digitalNum + ":", 0));
    mAnaloguechannel.setVisibility(View.VISIBLE);
    mAnaloguechannel.setText(getString(R.string.menu_setup_satellite_name) +
            mTv.getScanManager().getFirstSatName());
    mNumberChannel.setText(getSatName());
  } else {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reStartDVBSFullScanAfterTricolorChannelList error");
  }
}

/**
 * prepare DVBS scan params
 *
 * @param satID the id of will scan satellite
 * @param mLocationId
 * @return
 */
private ScanParams prepareDVBSParam(int satID, String mLocationId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareDVBSParam()>>>" + satID + ">>>" + mLocationId);
    // mContext.
    DVBSSettingsInfo params = new DVBSSettingsInfo();
    params.context = mContext;

    /*
     * SatelliteInfo satInfo =
     * com.mediatek.ui.wizard.TVContent.getDVBSsatellitesBySatID(mContext,satID);
     * if(satInfo!=null){ params.setSatelliteInfo(satInfo); }
     */
    params.getSatelliteInfo().setSatlRecId(satID);
    params.menuSelectedOP = ScanContent.getDVBSCurroperator();
    SatelliteInfo currentInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
    /*List<SatelliteInfo> mRescanSatLocalInfoList = ScanContent.getDVBSsatellites(mContext);
    params.mRescanSatLocalInfoList = mRescanSatLocalInfoList;
    params.mRescanSatLocalTPInfoList = ScanContent
            .getDVBSTransponderList(mRescanSatLocalInfoList);*/

    /*String scanMode = "";
    String channels = "";
    String storeType = "";
    ScanFactorItem item0 = (ScanFactorItem) mListView.getChildAt(0).getTag(R.id.factor_name);
    ScanFactorItem item1 = (ScanFactorItem) mListView.getChildAt(1).getTag(R.id.factor_name);
    ScanFactorItem item2 = (ScanFactorItem) mListView.getChildAt(2).getTag(R.id.factor_name);
    scanMode = item0.optionValues[item0.optionValue];
    channels = item1.optionValues[item1.optionValue];
    storeType = item2.optionValues[item2.optionValue];

    if (scanMode.equalsIgnoreCase(mContext.getResources().getString(
            R.string.dvbs_scan_mode_network))) {
        params.scanMode = DVBSSettingsInfo.NETWORK_SCAN_MODE;
    } else {
        params.scanMode = DVBSSettingsInfo.FULL_SCAN_MODE;
    }

    if (channels
            .equalsIgnoreCase(mContext.getResources().getString(R.string.dvbs_channel_encrypted))) {
        params.scanChannels = DVBSSettingsInfo.CHANNELS_ENCRYPTED;
    } else if (channels
        .equalsIgnoreCase(mContext.getResources().getString(R.string.dvbs_channel_free))) {
        params.scanChannels = DVBSSettingsInfo.CHANNELS_FREE;
    }else{
        params.scanChannels = DVBSSettingsInfo.CHANNELS_ALL;
    }

    if (storeType
        .equalsIgnoreCase(mContext.getResources().getString(R.string.dvbs_channel_story_type_digital))) {
        params.scanStoreType = DVBSSettingsInfo.CHANNELS_STORE_TYPE_DIGITAL;
    } else if (storeType
        .equalsIgnoreCase(mContext.getResources().getString(R.string.dvbs_channel_story_type_radio))) {
        params.scanStoreType = DVBSSettingsInfo.CHANNELS_STORE_TYPE_RADIO;
    }else{
        params.scanStoreType = DVBSSettingsInfo.CHANNELS_STORE_TYPE_ALL;
    }*/
    if(currentInfo != null) {
        params.scanMode = currentInfo.dvbsScanMode;
        params.scanChannels = currentInfo.dvbsScanType;
        params.scanStoreType = currentInfo.dvbsStoreType;
    }
    
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"scanMode: " + params.scanMode);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"scanChannels: " + params.scanChannels);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"scanStoreType: " + params.scanStoreType);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"menuSelectedOP: " + params.menuSelectedOP);

    if (mTv.getScanManager().isBATCountry()) {
        params.checkBATInfo = true;
    } else {
        params.checkBATInfo = false;
    }

    if (mLocationId != null && mLocationId.equals(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)) {
        params.isUpdateScan = true;
        params.mIsDvbsNeedCleanChannelDB = false;
    }
    if (mLocationId != null) {
        if (mLocationId.equalsIgnoreCase(MenuConfigManager.DVBS_SAT_ADD)) {
            SatelliteInfo satInfo = ScanContent.getDVBSsatellitesBySatID(mContext, satID);
            if (satInfo != null) {
                params.setSatelliteInfo(satInfo);
            }
            params.mIsOnlyScanOneSatellite = true;
            params.mIsDvbsNeedCleanChannelDB = false;
        }
    }
    return params;
}

/**
 * prepare DVBC single RF scan param
 *
 * @return
 */
private ScanParams prepareSingleRFScanParam() {
    // debug:
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareSingleRFScanParam()");
    // mContext.
    ScanParams params = new ScanParams();
    String frequency = "";
    ScanFactorItem item = (ScanFactorItem) mListView.getChildAt(0).getTag(R.id.factor_name);
    frequency = "" + item.inputValue;
    if ("".equalsIgnoreCase(frequency)) {
        params.freq = -1;
    } else {
        params.freq = Integer.valueOf(frequency);
        if (CommonIntegration.isCNRegion()) {
            params.freq = params.freq * 1000000;
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"frequency: " + frequency);
    if (CommonIntegration.isCNRegion()) {
        DVBCCNScanner.selectedRFChannelFreq = params.freq;
    } else {
        DVBCScanner.selectedRFChannelFreq = params.freq * 1000;
    }
    return params;
}

/**
 * prepare DVBC full scan params
 *
 * @return
 */
private ScanParams prepareDVBCScanParam() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareDVBCScanParam()");
    ScanParams params = new ScanParams();
    String scanmode = "";
    String scantype = "";
    String frequency = "";
    String networkID = "";
    try {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            ScanFactorItem item = (ScanFactorItem) mListView.getChildAt(i).getTag(
                    R.id.factor_name);
            if (item.id.equalsIgnoreCase(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)) {
                frequency = String.valueOf(item.inputValue);
            } else if (item.id.equalsIgnoreCase(MenuConfigManager.TV_DVBC_SCAN_NETWORKID)) {
                networkID = String.valueOf(item.inputValue);
            } else if (item.id.equalsIgnoreCase(MenuConfigManager.SCAN_MODE_DVBC)) {
                scanmode = item.value;
            } else if(item.id.equalsIgnoreCase(MenuConfigManager.TV_DVBC_SCAN_TYPE)){
                scantype = item.value;
            }
        }
    } catch (Exception e) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Exception()");
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"scanmode: " + scanmode + "   frequency: " + frequency + "   networkID: "
            + networkID);
    if (scanmode.equalsIgnoreCase(mContext
            .getString(R.string.menu_arrays_Advance))) {
        params.dvbcScanMode = ScanParams.DvbcScanMode.ADVANCE;
    } else if (scanmode.equalsIgnoreCase(mContext
            .getString(R.string.menu_arrays_Full))) {
        params.dvbcScanMode = ScanParams.DvbcScanMode.FULL;
    } else if (scanmode.equalsIgnoreCase(mContext
            .getString(R.string.menu_arrays_Quick))) {
        params.dvbcScanMode = ScanParams.DvbcScanMode.QUICK;
    }

    if (scantype.equalsIgnoreCase(mContext
            .getString(R.string.menu_arrays_Only_Digital_Channels))) {
        params.dvbcScanType = ScanParams.DvbcScanType.DIGITAL;
    } else if (scantype.equalsIgnoreCase(mContext
            .getString(R.string.menu_arrays_All_channels))) {
        params.dvbcScanType = ScanParams.DvbcScanType.ALL;
    }

    if ("".equalsIgnoreCase(frequency)) {
        params.freq = ScanContent.getFrequencyOperator(mContext, scanmode);
    } else {
        params.freq = Integer.valueOf(frequency);
    }
    if (CommonIntegration.isEURegion()) {
        if ("".equalsIgnoreCase(networkID)) {
            params.networkID = ScanContent.getNetWorkIDOperator(mContext, scanmode);
        } else {
            params.networkID = Integer.valueOf(networkID);
        }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"scanmodeName(): " + params.dvbcScanMode.name() +" scantypeName(): "+params.dvbcScanType.name());
    return params;
}

private void prepareUIBeforeScan() {
    mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
    if(mTv.getScanManager().getTuneMode() < 2) {
      mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
    }
    loading.setVisibility(View.VISIBLE);
    mAnaloguechannel.setVisibility(View.INVISIBLE);
    mNumberChannel.setVisibility(View.VISIBLE);
    this.setScanProgress(0);
    loading.drawLoading();
    final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
    mNumberChannel.setText(String.format("%s%3d", digitalNum + ":", 0));
}

private void prepareUIBeforeDVBSScan() {
    mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
    loading.setVisibility(View.VISIBLE);
    final String digitalNum = mContext.getString(R.string.menu_tv_digital_channels);
    mAnaloguechannel.setText(getString(R.string.menu_setup_satellite_name));
    mAnaloguechannel.setVisibility(View.VISIBLE);
    mNumberChannel.setText(String.format("%s: ", getString(R.string.satellites)));
    mNumberChannel.setVisibility(View.VISIBLE);
    mDVBSChannels.setText(String.format("%s%3d", digitalNum + ":", 0));
    mDVBSChannels.setVisibility(View.VISIBLE);
    this.setScanProgress(0);
    loading.drawLoading();
}

boolean mNeedChangeStartFrq = false;

/**
 * prepare Analog Manual Scan param(EU)
 *
 * @param scanUp true:scan up, false:scan down
 * @return
 */
private ScanParams prepareScanUpDownParam(boolean scanUp, String frequency) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareScanUpDownParam()");
    // mContext.
    ScanParams params = new ScanParams();
    if ("".equalsIgnoreCase(frequency)) {
        params.freq = -1;
    } else {
        params.freq = Integer.valueOf(frequency);
        if (scanUp) {
            if (mNeedChangeStartFrq) {
                params.freq = params.freq + 2;
            }
            if (params.freq >= EUATVScanner.FREQ_HIGH_VALUE) {
                params.freq = EUATVScanner.FREQ_HIGH_VALUE;
            }
        } else {
            if (mNeedChangeStartFrq) {
                params.freq = params.freq - 2;
            }
            if (params.freq <= EUATVScanner.FREQ_LOW_VALUE) {
                params.freq = EUATVScanner.FREQ_LOW_VALUE;
            }
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"frequency: " + params.freq);
    return params;
}

/**
 * prepare Analog Manual Scan param(CN)
 *
 * @return
 */
private ScanParams prepareATVFreqRangeScanParam(int startFreq, int endFreq) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareATVFreqRangeScanParam()");
    ScanParams params = new ScanParams();
    params.startfreq = startFreq;
    params.endfreq = endFreq;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"frequency: " + startFreq + ">>>" + endFreq);
    return params;
}

private void handleRightForStartScan(){
    if(mListView == null || mListView.getSelectedView() == null || mListView.getChildAt(0) == null) {
        return;
    }
    ScanFactorItem item = (ScanFactorItem) mListView.getSelectedView().getTag(R.id.factor_name);
    ScanFactorItem freqItem = (ScanFactorItem) mListView.getChildAt(0).getTag(R.id.factor_name);
    if(item.id.equals(MenuConfigManager.TV_ANANLOG_SCAN_UP)){
        startAnalogScan(true, ""+freqItem.inputValue);
    }else if(item.id.equals(MenuConfigManager.TV_ANANLOG_SCAN_DOWN)){
        startAnalogScan(false, ""+freqItem.inputValue);
    }else if (item.id.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN_CEC_CN) || item.id.equals(MenuConfigManager.TV_DVBC_CHANNELS_START_SCAN)) {
        if (MenuConfigManager.TV_SINGLE_RF_SCAN_CN.equals(mActionID) || MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN.equals(mActionID)){
            startDVBCSingleRFScan();
        }else if (MenuConfigManager.TV_CHANNEL_SCAN_DVBC.equals(mActionID)
                || mActionID.startsWith(MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR)){
            startDVBCFullScan();
        }
    }else if (mActionID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN)) {// dvbs full
        if (item.id.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_START_SCAN)) {// can
            startDVBSFullScan(mSatID, -1, mLocationID);
        }
    }else if(item.id.equals(MenuConfigManager.TV_CHANNEL_STARTSCAN)){
        if (CommonIntegration.isCNRegion()) {
            ScanFactorItem freqEndItem = (ScanFactorItem) mListView.getChildAt(1).getTag(
                    R.id.factor_name);
                startAnalogATVFreqRangeScan(freqItem.inputValue,
                        freqEndItem.inputValue);
        }
    }
}

private void handleWhenLeftRight(boolean direction) {
    if(mListView == null || mListView.getSelectedView() == null || mListView.getChildAt(0) == null) {
        return;
    }
    ScanFactorItem item = (ScanFactorItem) mListView.getSelectedView().getTag(R.id.factor_name);
    if(item.id.equals(MenuConfigManager.TV_ANANLOG_SCAN_UP) || item.id.equals(MenuConfigManager.TV_ANANLOG_SCAN_DOWN)){
        return;
    }
    if (item.id.equals(MenuConfigManager.FAV_US_RANGE_FROM_CHANNEL)) {
        setFromToChannelNumber(item, direction, true);
    } else if (item.id.equals(MenuConfigManager.FAV_US_RANGE_TO_CHANNEL)) {
        setFromToChannelNumber(item, direction, false);
    } else if (item.id.equals(MenuConfigManager.FAV_US_SINGLE_RF_CHANNEL)) {
        int now = Integer.parseInt(item.value);
        if (!direction) {
            now--;
            if (now < mTv.getFirstScanIndex()) {
                now = mTv.getLastScanIndex();
            }
        } else {
            now++;
            if (now > mTv.getLastScanIndex()) {
                now = mTv.getFirstScanIndex();
            }
        }
        item.value = "" + now;
        // rf chanel also store to frefrom channel
        this.freFrom = now;
        final int freq = now;
        /*new Thread(new Runnable() {

            @Override
            public void run() {*/
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTv.isScanning()1>>" + mTv.isScanning() + "   now RF Index:"
                        + freq);
                if (!mTv.isScanning()) {
                    editChannel.tuneUSSAFacRFSignalLevel(freq);
                }
                //mHandler.removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
                sendMessageDelayedThread(MessageType.MENU_TV_RF_SCAN_REFRESH, 0);
            //}
        //}).start();

    } else if (item.isEnable && item.factorType == 1
    // ||item.id.equals(MenuConfigManager.US_SCAN_MODE)
    // ||item.id.equals(MenuConfigManager.FREQUENEY_PLAN)
    // ||item.id.equals(MenuConfigManager.TV_SINGLE_SCAN_MODULATION)
    ) {
        if (!direction) {
            if (item.id.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)) {
                String value = mTv.getRFChannel(DVBTScanner.RF_CHANNEL_PREVIOUS);
                item.optionValues[item.optionValue] = value;
                refreshDVBTRFSignalQualityAndLevel(1000);
            } else if (item.id.equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)) {
                String value = mTv.getCNRFChannel(DVBTCNScanner.RF_CHANNEL_PREVIOUS);
                item.optionValues[item.optionValue] = value;
                refreshDVBTRFSignalQualityAndLevel(1000);
            } else {
                item.optionValue--;
                if (item.optionValue < 0) {
                    item.optionValue = item.optionValues.length - 1;
                }
                if (item.id.equals(MenuConfigManager.SCAN_MODE_DVBC) ||
                        item.id.equals(MenuConfigManager.TV_DVBC_SCAN_TYPE)) {
                    if (item.listener != null) {
                        item.listener.onScanOptionChange(item.optionValues[item.optionValue]);
                    }
                }
            }
        } else {
            if (item.id.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)) {
                String value = mTv.getRFChannel(DVBTScanner.RF_CHANNEL_NEXT);
                item.optionValues[item.optionValue] = value;
                refreshDVBTRFSignalQualityAndLevel(1000);
            } else if (item.id.equals(MenuConfigManager.TV_SINGLE_SCAN_RF_CHANNEL)) {
                String value = mTv.getCNRFChannel(DVBTCNScanner.RF_CHANNEL_NEXT);
                item.optionValues[item.optionValue] = value;
                refreshDVBTRFSignalQualityAndLevel(1000);
            } else {
                item.optionValue++;
                if (item.optionValue > item.optionValues.length - 1) {
                    item.optionValue = 0;
                }
                if (item.id.equals(MenuConfigManager.SCAN_MODE_DVBC) ||
                        item.id.equals(MenuConfigManager.TV_DVBC_SCAN_TYPE)) {
                    if (item.listener != null) {
                        item.listener.onScanOptionChange(item.optionValues[item.optionValue]);
                    }
                }
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "item.id>>" + item.id + ">>" + item.optionValue);
        // for store some special item to share-preference
        mConfigManager.setScanValue(item.id, item.optionValue);
        if (MenuConfigManager.TV_SINGLE_SCAN_MODULATION.equals(item.id)
                || MenuConfigManager.FREQUENEY_PLAN.equals(item.id)) {
            refreshUSSASignalQualityAndLevel(1000);
        }
    }
    mAdapter.notifyDataSetChanged();
}

private void refreshUSSASignalQualityAndLevel(long delayMills) {
    if (CommonIntegration.isUSRegion()) {
        ProgressBar singalLevel = (ProgressBar) mListView.getChildAt(3).
                findViewById(R.id.factor_progress);
        singalLevel.setProgress(0);
        ScanFactorItem itemLevel = (ScanFactorItem) mListView.getChildAt(3).
                getTag(R.id.factor_name);
        if (itemLevel != null) {
            itemLevel.progress = 0;
        }
    } else if (CommonIntegration.isSARegion()) {
        ProgressBar singalLevel = (ProgressBar) mListView.getChildAt(1).
                findViewById(R.id.factor_progress);
        ProgressBar singalQual = (ProgressBar) mListView.getChildAt(2).
                findViewById(R.id.factor_progress);
        singalLevel.setProgress(0);
        singalQual.setProgress(0);
        ScanFactorItem itemLevel = (ScanFactorItem) mListView.getChildAt(1).
                getTag(R.id.factor_name);
        if (itemLevel != null) {
            itemLevel.progress = 0;
        }
    }
    // mAdapter.notifyDataSetChanged();
    mHandler.removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
    mHandler.removeMessages(MessageType.MENU_USSA_TV_RF_SCAN_CONNECTTURN);
    mHandler.sendEmptyMessageDelayed(MessageType.MENU_USSA_TV_RF_SCAN_CONNECTTURN,
            delayMills);
}

private void setFromToChannelNumber(ScanFactorItem item, boolean direction, boolean isFrom) {
    if (CommonIntegration.isEURegion()) {
        String fcstr = item.value;
        int idx = 0;
        while (idx < allRFChannels.length) {
            if (fcstr.equals(allRFChannels[idx])) {
                if (!direction) {
                    idx--;
                    if (isFrom) {
                        if (idx < 0) {
                            idx = this.freTo;
                        }
                    } else {
                        if (idx < this.freFrom) {
                            idx = allRFChannels.length - 1;
                        }
                    }
                    break;
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "compared:" + fcstr + "|");
                    idx++;
                    if (isFrom) {
                        if (idx > this.freTo) {
                            idx = 0;
                        }
                    } else {
                        if (idx > allRFChannels.length - 1) {
                            idx = this.freFrom;
                        }
                    }
                    break;
                }
            }
            idx++;
        }
        item.value = allRFChannels[idx];
        if (isFrom) {
            this.freFrom = idx;
        } else {
            this.freTo = idx;
        }
    } else {
        if (isFrom) {
            int now = Integer.parseInt(item.value);
            ScanFactorItem item2 = null;
            if (CommonIntegration.isUSRegion()) {
                item2 = (ScanFactorItem) mListView.getChildAt(2).getTag(R.id.factor_name);
            } else {
                item2 = (ScanFactorItem) mListView.getChildAt(1).getTag(R.id.factor_name);
            }
            int freqTo = Integer.parseInt(item2.value);
            if (!direction) {
                now--;
                if (now < mTv.getFirstScanIndex()) {
                    now = freqTo;
                }
            } else {
                now++;
                if (now > freqTo) {
                    now = mTv.getFirstScanIndex();
                }
            }
            item.value = "" + now;
            this.freFrom = now;
        } else {
            int now = Integer.parseInt(item.value);
            ScanFactorItem item1 = null;
            if (CommonIntegration.isUSRegion()) {
                item1 = (ScanFactorItem) mListView.getChildAt(1).getTag(R.id.factor_name);
            } else {
                item1 = (ScanFactorItem) mListView.getChildAt(0).getTag(R.id.factor_name);
            }
            int freqFrom = Integer.parseInt(item1.value);
            if (!direction) {
                now--;
                if (now < freqFrom) {
                    now = mTv.getLastScanIndex();
                }
            } else {
                now++;
                if (now > mTv.getLastScanIndex()) {
                    now = freqFrom;
                }
            }
            item.value = "" + now;
            this.freTo = now;
        }
    }
}

@Override
public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == 0) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isScanning) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
            default:
                if (isScanning) {
                    return true;
                }
                break;
        }
    }
    return super.dispatchKeyEvent(event);
}

private String mCurrFactorItemId;
//private ScanFactorItem mFactorItem;
private static final int REQ_EDITTEXT = 0x23;

/**
 * goto the edittext activity in order to input text or number
 *
 * @param selectedView
 */
private void gotoEditTextAct(ScanFactorItem item) {
    gotoEditTextAct(item, false);
}

private void gotoEditTextAct(ScanFactorItem item, boolean allowEmpty) {
    mCurrFactorItemId = item.id;
    if (item.isEnable && item.factorType == 3) {// this is edit item
        mNeedChangeStartFrq = false;
        Intent intent = new Intent(mContext, EditTextActivity.class);
        intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
        intent.putExtra(EditTextActivity.EXTRA_DESC, item.title);
        String showText = "" + item.inputValue;
        if (MenuConfigManager.TV_DVBC_SCAN_FREQUENCY.equals(item.id)
                || MenuConfigManager.TV_DVBC_SCAN_NETWORKID.equals(item.id)) {
            TextView textView = (TextView) mListView.getSelectedView().findViewById(
                    R.id.factor_input);
            showText = textView.getText().toString();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showText:" + showText + "  isnull:" + TextUtils.isEmpty(showText));
            if (!TextUtils.isEmpty(showText)) {
                if (item.inputValue < item.minValue) {
                    if(showText.equals(mContext.getString(R.string.menu_arrays_Auto))){
                        showText = "";
                    }else {
                        showText = item.minValue + "";
                    }
                    /*if(isBelgiumCableVoo()){
                        showText = "0000";
                    }*/
                }
            }
        } else if (item.inputValue < item.minValue) {
            showText = item.minValue + "";
        }
        intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, showText);
        intent.putExtra(EditTextActivity.EXTRA_ITEMID, item.id);
        intent.putExtra(EditTextActivity.EXTRA_DIGIT, true);
        intent.putExtra(EditTextActivity.EXTRA_ALLOW_EMPTY, allowEmpty);
        // dight limit input length 9
        if (item.id.equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                || item.id.equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ)) {
            intent.putExtra(EditTextActivity.EXTRA_LENGTH, 6);
        } else if (item.id.equals(MenuConfigManager.SYM_RATE) ||
                (item.id.equals(MenuConfigManager.TV_DVBC_SCAN_NETWORKID) && isBelgiumCableVoo())) {
            intent.putExtra(EditTextActivity.EXTRA_LENGTH, 4);
        } else {
            intent.putExtra(EditTextActivity.EXTRA_LENGTH, 6);
        }
        findViewById(R.id.menu_scan_dialog_id).setVisibility(View.INVISIBLE);
        this.startActivityForResult(intent, REQ_EDITTEXT);
    } else {// this is option item
            // do nothing
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Option Item needn't go to editText Activity");
    }
}

private boolean isBelgiumCableVoo() {
    return ScanContent.isCountryBel() && (mContext.getString(R.string.dvbc_operator_voo).equals(cableOperator));
}


private boolean isNeedShowNetworkName() {
    return ScanContent.isCountryDenmark() && (mContext.getString(R.string.dvbc_operator_stofa).equals(cableOperator));
}

@Override
protected void onStop() {
    super.onStop();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop....");
}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown...." + isScanning);
    switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            /*if (cableOperator != null) {
                if ("RCS RDS".equals(cableOperator)) {
                    break;
                }
            }*/
            handleWhenLeftRight(false);
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            /*if (cableOperator != null) {
                if ("RCS RDS".equals(cableOperator)) {
                    break;
                }
            }*/
            handleRightForStartScan();
            handleWhenLeftRight(true);
            break;
        case KeyMap.KEYCODE_MTKIR_RED:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown....red" + KeyMap.KEYCODE_MTKIR_RED);
            /*if (cableOperator != null) {
                if ("RCS RDS".equals(cableOperator)) {
                    break;
                }
            }*/
            handleWhenLeftRight(false);
            break;
        case KeyMap.KEYCODE_MTKIR_BLUE:
            if (isScanning) {
                this.cancelScan();
                return true;
            }
            finish();
            break;
        case KeyMap.KEYCODE_MTKIR_GREEN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown....green" + KeyMap.KEYCODE_MTKIR_GREEN);
           /* if (cableOperator != null) {
                if ("RCS RDS".equals(cableOperator)) {
                    break;
                }
            }*/
            handleWhenLeftRight(true);
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
            break;
        case KeyEvent.KEYCODE_BACK:
            if (isScanning) {
                this.cancelScan();
                return true;
            }
            beforeBack();
            break;
        default:
            break;
    }
    return super.onKeyDown(keyCode, event);
}

private void beforeBack() {
    mHandler.removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
}

@Override
protected void onResume() {
    isSelectedChannel = true;
    findViewById(R.id.menu_scan_dialog_id).setVisibility(View.VISIBLE);
    super.onResume();
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (resultCode) {
        case RESULT_OK:
            if (data != null) {
                String value = data.getStringExtra("value");
                if (mCurrFactorItemId.equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)
                        || mCurrFactorItemId.equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                        || mCurrFactorItemId.equals(MenuConfigManager.TV_DVBC_SCAN_NETWORKID)
                        || mCurrFactorItemId.equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ)
                        || mCurrFactorItemId.equals(MenuConfigManager.SYM_RATE)
                        || mCurrFactorItemId.equals(MenuConfigManager.TV_CHANNEL_END_FREQUENCY)) {
                    ScanFactorItem item = (ScanFactorItem) mListView.getSelectedView().getTag(
                            R.id.factor_name);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult value="+value +",item.value="+item.value);
                    item.inputValue = Integer.parseInt(value);
                    if((mCurrFactorItemId.equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
                        || mCurrFactorItemId.equals(MenuConfigManager.TV_DVBC_SCAN_NETWORKID))
                        && item.inputValue == -1){
                        if(ScanContent.isNLDOtherOp() || ScanContent.isZiggoUPCOp() || ScanContent.isTELNETOp() || ScanContent.isVooOp()){
                            String scanMode = ((ScanFactorItem)mListView.getAdapter().getItem(0)).value;
                            if(mCurrFactorItemId.equals(MenuConfigManager.TV_DVBC_SCAN_NETWORKID)){
                                item.value = ScanContent.getNetWorkIDOperator(mContext, scanMode)+"";
                            }else{
                                item.value = ScanContent.getFrequencyOperator(mContext, scanMode)+"";
                            }
                            item.inputValue = Integer.parseInt(item.value);
                        }else {
                            item.value = mContext.getString(R.string.menu_arrays_Auto);
                        }
                    }else {
                        if (item.inputValue > item.maxValue) {
                            item.inputValue = item.maxValue;
                            Toast.makeText(mContext, mContext.getString(R.string.menu_string_postal_code_toast_invalid_input), 0).show();
                        } else if (item.inputValue < item.minValue) {
                            item.inputValue = item.minValue;
                            Toast.makeText(mContext, mContext.getString(R.string.menu_string_postal_code_toast_invalid_input), 0).show();
                        }
                    }

                    if(mCurrFactorItemId.equals(MenuConfigManager.DVBC_SINGLE_RF_SCAN_FREQ)&&
                            item.inputValue != -1){
                        editChannel.tuneDVBCRFSignal(item.inputValue * 1000);
                        //mHandler.removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
                        sendMessageDelayedThread(
                                MessageType.MENU_TV_RF_SCAN_REFRESH, MessageType.delayMillis6);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
            break;
        default:
            break;
    }
    super.onActivityResult(requestCode, resultCode, data);
}

/**
 * for DVBS scan get
 *
 * @param satName current satellite name
 * @return current sat index/total num, such as 1/4
 */
private CharSequence getSatName() {
    String scanIndex = String.format("%d/%d", mTv.getScanManager().getDVBSCurrentIndex() + 1,
            mTv.getScanManager().getDVBSTotalSatSize());
    return String.format("%s: %4s", getString(R.string.satellites), scanIndex);
}

private void refreshDVBTRFSignalQualityAndLevel(long delayMills) {
    ProgressBar singalLevel = (ProgressBar) mListView.getChildAt(1).findViewById(
            R.id.factor_progress);
    ProgressBar singalQual = (ProgressBar) mListView.getChildAt(2).findViewById(
            R.id.factor_progress);
    singalLevel.setProgress(0);
    singalQual.setProgress(0);

    mHandler.removeMessages(MessageType.MENU_TV_RF_SCAN_REFRESH);
    if (CommonIntegration.isCNRegion()) {
        mHandler.removeMessages(MessageType.MENU_CN_TV_RF_SCAN_CONNECTTURN);
        mHandler.sendEmptyMessageDelayed(MessageType.MENU_CN_TV_RF_SCAN_CONNECTTURN,
                delayMills);
    } else {
        mHandler.removeMessages(MessageType.MENU_DVBT_RF_SCAN_TUNESIGNAL);
        mHandler.sendEmptyMessageDelayed(MessageType.MENU_DVBT_RF_SCAN_TUNESIGNAL,
                delayMills);
    }

}

private void showTKGSUpdateScanDialog(int satId) {
  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTKGSUpdateScanDialog satId>>>" + satId);
  TKGSContinueScanConfirmDialog dialog = new TKGSContinueScanConfirmDialog(mContext, satId);
  dialog.showConfirmDialog();
}

private void showTricolorChannelListDialog() {
com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTricolorChannelListDialog satId>>>");
if (mTv.getScanManager().getOPType() == ScanCallback.TYPE_UI_OP_DVBS_TRICOLOR) {
  loading.stopDraw();
  ScanThirdlyDialog dialog = new ScanThirdlyDialog(this, 3, 0);
  dialog.show();
}
}

private void showFastScanOpSelectDialog() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFastScanOpSelectDialog");
    loading.stopDraw();
    ScanThirdlyDialog dialog = new ScanThirdlyDialog(this, 4, 0);
    dialog.show();
}

private void showNoOptDialog() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showNoOptDialog");
    final SimpleDialog dialog = new SimpleDialog(mContext);
    dialog.setConfirmText(android.R.string.ok);
    dialog.setContent(mContext.getString(R.string.dvbs_fast_scan_operator_dialog_msg));
    dialog.setOnCancelClickListener(new OnCancelClickListener() {
        @Override
        public void onCancelClick(int dialogId) {
            dialog.dismiss();
        }
    }, 1);
    dialog.show();
}

/**
* show dvbs's tkgs rescan service list dialog
*/
private void showTKGSRescanServiceListView(int satId) {
  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTKGSRescanServiceListView>>>");
  loading.stopDraw();
  ScanThirdlyDialog dialog = new ScanThirdlyDialog(this, 2, satId);
  dialog.show();
}

private void showDVBSNFYInfoView() {
  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBSNFYInfoView>>>" + mTv.getScanManager().getOPType());
  if (mTv.getScanManager().getOPType() == ScanCallback.TYPE_UI_OP_DVBS_BOUQUET_INFO) {
      loading.stopDraw();
      ScanThirdlyDialog dialog = new ScanThirdlyDialog(this, 1, mSatID);
      dialog.show();
  }
}

private int getDVBCScanmodeIndex(String[] scanModes) {
    /*int scanMode = SaveValue.readLocalMemoryIntValue("DVBCScanMode");
    if(scanMode == -1 || scanMode > scanModes.length - 1){
        SaveValue.setLocalMemoryValue("DVBCScanMode",0);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDVBCScanmodeIndex first index");
        return 0;
    }else{
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDVBCScanmodeIndex ="+scanMode);
        return scanMode;
    }*/
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDVBCScanmodeIndex ="+scanModes.length);
    return 0;
}

private int getIndexFromScanmodeName(String name, String[] scanModes) {
    for(int i = 0; i < scanModes.length; i++){
        if(name.equals(scanModes[i])){
            return i;
        }
    }
    return 0;
}

public void sendMessageDelayedThread(final int what, final long delayMillis) {
    TVAsyncExecutor.getInstance().execute(new Runnable() {
        public void run() {
            int level = editChannel.getSignalLevel();
            int quality = editChannel.getSignalQuality();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "level="+level+",quality="+quality);
            if(!isDestroyed()) {
                Message msg = Message.obtain();
                msg.what = what;
                msg.arg1 = level;
                msg.arg2 = quality;
                mHandler.removeMessages(what);
                if(delayMillis > 0){
                    mHandler.sendMessageDelayed(msg, delayMillis);
                }else {
                    mHandler.sendMessage(msg);
                }
            }
        }
    });
}

}
