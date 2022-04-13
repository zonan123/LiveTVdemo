package com.mediatek.wwtv.setting.base.scan.ui;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.util.Arrays;
import java.util.List;

import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.base.scan.model.DVBSSettingsInfo;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.setting.base.scan.model.SaveChannelsConfirmDialog;
import com.mediatek.wwtv.setting.base.scan.model.ScanCallback;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.model.ScanParams;
import com.mediatek.wwtv.setting.base.scan.model.ScannerListener;
import com.mediatek.wwtv.setting.base.scan.model.ScannerManager;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.scan.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.commonview.Loading;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.MessageType;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import mediatek.sysprop.VendorProperties;

/**
 * for all scan which hasn't scan condition
 * if has condition use ScanViewActivity
 * @author sin_biaoqinggao
 *
 */
public class ScanDialogActivity extends BaseCustomActivity{

    private static final String TAG = "ScanDialogActivity";

        private Context mContext;
        private Loading loading;
        //private LinearLayout lineLay;
        // scan status
        private TextView mStateTextView;
        private TextView mDVBSChannels;
        private TextView mAnalogChannel;
        private TextView mIpChannel;
        private TextView mFvpWaitTip;
        private TextView mNumberChannel;
        // scan progress
        private ProgressBar mScanprogressbar;
        private TextView mFinishpercentage;
        // By Ese key, scan the number of units Dialog was dimiss
        private int count = 1;
        private boolean onScanning;
        private TVContent mTV;
        private boolean mIsATVSource;

        private String mItemId;
        private int mLastChannelId;
        private boolean mNeedChangeToFirstChannel;
        private Toast toast;


        private int nowProgress;
        private int satID = -1;
       // private boolean isScanning;

        private boolean mCanCompleteScan;//for us scan with atv and dtv complete msg
        public boolean isChannelSelected;
        private int tsLockTimes = 0;
        private Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                case MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL>>>");
                    selectChannel();
                    isChannelSelected = true;
                    break;
                case MessageType.MESSAGE_START_SCAN:
                    mTV.getScanManager().startATVAutoScan();
                    break;
                case MessageType.MESSAGE_START_ATV_SCAN:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MESSAGE_START_ATV_SCAN");
                    mTV.startScanByScanManager(ScannerManager.ATV_SCAN, mListener, null);
                    break;
                case MessageType.MENU_TV_SCAN_TS_LOCK_STATUS:
                    boolean isLocked = mTV.isTsLocked();
                    tsLockTimes ++;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isLocked:"+isLocked+" tsLockTimes:"+tsLockTimes+" index:"+mTV.getScanManager().getDVBSCurrentIndex());
                    if(tsLockTimes > 55 && tsLockTimes < 65 || isLocked) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "position moveing done. start scan.");
                        mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
                        removeMessages(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS);
                        mTV.getScanManager().startDvbsScanAfterTsLock();
                    } else {
                        sendEmptyMessageDelayed(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS,
                                MessageType.delayMillis6);
                    }
                    break;

                case MessageType.MESSAGE_FVP_SCAN_TIP_MSG_FIRST:
                    if(mFvpWaitTip != null){
                        mFvpWaitTip.setText(mContext.getString(R.string.fvp_scan_timeout_tip, mContext.getResources().getInteger(R.integer.fvp_scan_tip_msg_wait_time_first)));
                        mFvpWaitTip.setVisibility(View.VISIBLE);
                    }
                    break;

                case MessageType.MESSAGE_FVP_SCAN_TIP_MSG_SECOND:
                    mTV.getScanManager().setOPType(ScanCallback.TYPE_UI_OP_DVBT_FVP_ERROR);
                    mTV.getScanManager().cancelScan();
                    break;
                default:
                    break;
                }
            }

        };
        private ScannerListener mListener = new ScannerListener() {

            @Override
            public void onCompleted(int completeValue) {
                switch (completeValue) {
                    case COMPLETE_ERROR:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                count = 0;
                                onScanning = false;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- scan COMPLETE_ERROR----");
                                showCancelScanInfo();
                                nowProgress = 0;
                                mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                                mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL, MessageType.delayMillis5);
                            }
                        });

                        break;
                    case COMPLETE_CANCEL:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                count = 0;
                                onScanning = false;
                                nowProgress = 0;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- scan canceled----");
                                //                                if(LiveApplication.isSuspendMsg){
                                //                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCompleted,isSuspendMsg"+"allowSystemSuspend()");
                                //                                    ScanContent.allowSystemSuspend();
                                //                                } else {
                                //                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCompleted,isSuspendMsg false");
                                //                                }

                                showCancelScanInfo();
                                if(mTV.hasOPToDo()){
                                    showTRDUI();
                                }else {
                                    if (!mHandler.hasMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL)) {
                                        mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL, MessageType.delayMillis5);
                                        mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
                                    }
                                }
                            }
                        });
                        break;
                    case COMPLETE_OK:
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- scan COMPLETE_OK---- postDelayed");
                                if (mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBT)
                                        || mItemId.equals(MenuConfigManager.TV_UPDATE_SCAN_DVBT_UPDATE)
                                        || mItemId.equals(MenuConfigManager.TV_UPDATE_SCAN)){
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- mTV.isScanTaskFinish()----"+mTV.isScanTaskFinish());
                                    TvCallbackData backData = new TvCallbackData();
                                    if (mTV.isScanTaskFinish()) {
                                        loading.stopDraw();
                                        backData.param1 = 1;
                                        backData.param2 = 100; // 100%
                                        updateCommonScanProgress(backData);
                                        if(mTV.getScanManager().hasOPToDo()){
                                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onCompleted(),hasOPToDo....,show TRDviews");
                                            showTRDUI();
                                        } else {
                                            mTV.uiOpEnd();
                                            mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                                            mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL, MessageType.delayMillis5);
                                        }
                                        nowProgress = 0;
                                        mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
                                    } else if (!mStateTextView.getText().equals(mContext.getString(R.string.menu_tv_analog_manual_scan_cancel))) {
                                        nowProgress = 50;
                                        mHandler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                mTV.startOtherScanTask(mListener);
                                            }
                                        }, 1000);
                                        return;
                                    } else {
                                        if(mListener != null) {
                                            mListener.onCompleted(COMPLETE_CANCEL);
                                        }
                                    }
                                } else {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- scan COMPLETE_OK---- mCanCompleteScan>" + mCanCompleteScan);
                                    if (mCanCompleteScan || !CommonIntegration.isUSRegion()) {
                                        loading.stopDraw();
                                        TvCallbackData backData = new TvCallbackData();
                                        backData.param1 = 1;
                                        backData.param2 = 100;
                                        updateCommonScanProgress(backData);
                                        mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                                        mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL, MessageType.delayMillis5);
                                        if (mItemId.equals(MenuConfigManager.M7_LNB_Scan)){
                                            ScanDialogActivity.this.setResult(RESULT_OK);
                                        }
                                    } else {
                                        mCanCompleteScan = true;
                                    }
                                }
                                if(CommonIntegration.isEURegion()){
                                    showTRDUI();
                                    if(mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_BGM) && ScanContent.isCountryUK()){
                                        fvpIsAdviseScan();
                                    }
                                }
                                if(lnb){
                                    finish();
                                }
                                /*if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
                                    writeAutotestScanFile();
                                }*/
                            }
                        }, 0);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFrequence(int freq) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onFrequence()");
            }
 @Override
            public void onIPChannels(final int channels, int type) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onIPChannels() channels:"+channels);
                mHandler.removeMessages(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_FIRST);
                mHandler.removeMessages(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_SECOND);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mIpChannel != null){
                            mIpChannel.setText(mContext.getString(R.string.ip_channels)+channels);
                        }
                    }
                });
            };

            @Override
            public void onFvpUserSelection() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onFvpUserSelection()");
                clearFvpMsg();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTV.getScanManager().showDVBTFvpUserSelectDialog(ScanDialogActivity.this);
                    }
                });
            };

            @Override
            public void onFvpScanError() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onFvpScanError()");
                clearFvpMsg();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTV.getScanManager().showDVBTFvpScanErrorDialog(ScanDialogActivity.this);
                    }
                });
            };

            @Override
            public void onFvpScanStart() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onFvpScanStart()");
                resetFvpTipMsg();
            };

            @Override
            public void onProgress(int progress, int channels) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onProgress()");
            }

            @Override
            public void onProgress(final int progress, final int channels,
                    final int type) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TvCallbackData backData = new TvCallbackData();
                        backData.param2 = progress;
                        backData.param3 = channels;
                        backData.param4 = type;
                        updateScanProgress(backData);
                    }
                });
            }

            @Override
            public void onDVBSInfoUpdated(int argv4, final String name) {
                mHandler.post(new Runnable() {
                    public void run() {
                        if(!TextUtils.isEmpty(name)) {
                            if("ChangeSatelliteFrequence".equals(name)) {
                                beforeDvbsScan();
                            } else if(mNumberChannel!=null && !name.equalsIgnoreCase("null")){
                                mNumberChannel.setText(getSatName(name));
                            }
                        }
                    }
                });

            }
            public void onDVBCNetworkNameUpdate(int argv4, String name) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onDVBCNetworkNameUpdate()");
            };
        };

        private void fvpIsAdviseScan() {
            boolean isAdvise = MtkTvScan.getInstance().getScanDvbtInstance().IsAdviseScan();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"fvpIsAdviseScan() isAdvise="+isAdvise);
            if(isAdvise){
                ConfirmDialog.getInstance(this).showFvpAdviseScanDialog(this);
            }

        }

        public void resetFvpTipMsg(){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"resetFvpTipMsg() ");
            if(mFvpWaitTip != null){
                mFvpWaitTip.setVisibility(View.GONE);
            }
            mHandler.removeMessages(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_FIRST);
            mHandler.removeMessages(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_SECOND);
            mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_FIRST, mContext.getResources().getInteger(R.integer.fvp_scan_tip_msg_wait_time_first) * 1000);
            mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_SECOND, mContext.getResources().getInteger(R.integer.fvp_scan_tip_msg_wait_time_second) * 1000);
        }

        private void clearFvpMsg(){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"clearFvpMsg() ");
            mHandler.removeMessages(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_FIRST);
            mHandler.removeMessages(MessageType.MESSAGE_FVP_SCAN_TIP_MSG_SECOND);
        }

        private void beforeDvbsScan() {
            List<SatelliteInfo> lists = ScanContent.getDVBSEnablesatellites(mContext);
            SatelliteInfo satelliteInfo = lists.get(mTV.getScanManager().getDVBSCurrentIndex());
            updateSatelliteName();
            if(satelliteInfo.getMotorType() == 5) { //DiSEqC 1.2
                ScanContent.setDVBSFreqToGetSignalQuality(satelliteInfo.getSatlRecId());
                mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_positioner_moving));
                tsLockTimes = 0;
                mHandler.removeMessages(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS);
                mHandler.sendEmptyMessage(MessageType.MENU_TV_SCAN_TS_LOCK_STATUS);
            } else {
                mTV.getScanManager().startDvbsScanAfterTsLock();
            }
        }


        private void reMapRegions() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"reMapRegions()");
            MtkTvScanDvbtBase.TargetRegion[] regionList = MtkTvScan.getInstance()
                    .getScanDvbtInstance().uiOpGetTargetRegion();
            if (regionList != null && regionList.length > 0) {
                mTV.getScanManager().reMapRegions(Arrays.asList(regionList));
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"debugOP(),TargetRegion==null");
            }
        }

        ScanThirdlyDialog thirdDialog ;
        private void showTRDUI() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"reMapRegions()");
            if(!isResumed()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDUI return because activity not resumed");
                return;
            }
            if(!mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBT) &&
                    mTV.getScanManager().getOPType() == ScanCallback.TYPE_UI_OP_UK_REGION) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"UK only full scan show region list");
                mTV.getScanManager().uiOpEnd();
            }
            switch(mTV.getScanManager().getOPType()){
                case ScanCallback.TYPE_UI_OP_NOR_FAV_NWK:
                    thirdDialog = new ScanThirdlyDialog(mContext,1);
                    break;
                case ScanCallback.TYPE_UI_OP_UK_REGION:
                    if(mTV.getScanManager().mFvpScanErrorDialog != null && mTV.getScanManager().mFvpScanErrorDialog.isFvpErrorDialogShowing()){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDUI() TYPE_UI_OP_UK_REGION return for fvp error dialog showing");
                        mTV.getScanManager().uiOpEnd();
                        return;
                    }
                    reMapRegions();
                    thirdDialog = new ScanThirdlyDialog(mContext,2);
                    //thirdDialog.setCancelable(false);
                    break;
                case ScanCallback.TYPE_UI_OP_ITA_GROUP:
                    if(mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBT)){
                        thirdDialog = new ScanThirdlyDialog(mContext, 3);
                    }else {
                        thirdDialog = null;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Not dvbt auto scan, no show LCN confict dialog");
                    }
                    break;
                case ScanCallback.TYPE_UI_OP_SAVE_CHANNEL:
                    showTRDSaveChannelConfirmDialog();
                    break;
                case ScanCallback.TYPE_UI_OP_LCNV2_CH_LST:
                    thirdDialog = new ScanThirdlyDialog(mContext,4);
                    break;
                case ScanCallback.TYPE_UI_OP_SHOW_TIA_NO_CHANNELS:
                    ConfirmDialog.getInstance(mContext).showItalyNoChannelsDialog(mContext);
                    break;
                case ScanCallback.TYPE_UI_OP_DVBT_FVP_ERROR:
                    mTV.getScanManager().showDVBTFvpScanErrorDialog(mContext);
                    break;
                default:
                    break;
            }
            if(thirdDialog != null && !isDestroyed() && !isFinishing()){
                thirdDialog.show();
            }
            mTV.getScanManager().uiOpEnd();
        }

        String mActionID;
        String mActionParentID;
        boolean mIsBroadcastScan = false;
        boolean mIsAdviseScan = false;
        boolean lnb = false;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(!getIntent().getBooleanExtra("need_full_screen",false)) {
                setDisplayAttr();
            }else {
                setDisplayAttr(1f,1f);
            }
            mContext = this;
            mTV = TVContent.getInstance(mContext);
            mTV.stopTimeShift();
            mTV.mHandler = mHandler;
            mActionID = this.getIntent().getStringExtra("ActionID");
            mIsBroadcastScan = this.getIntent().getBooleanExtra("broadcastScan",false);
            mIsAdviseScan = this.getIntent().getBooleanExtra("adviseScan",false);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mIsBroadcastScan="+mIsBroadcastScan);
            lnb = this.getIntent().getBooleanExtra("lnb", false);
            if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
                MenuConfigManager mConfigManager = MenuConfigManager.getInstance(mContext);
                int deftunermode = mConfigManager.getDefault(MenuConfigManager.TUNER_MODE);
                int tunermode = this.getIntent().getIntExtra("tuner_mode", deftunermode);
                mConfigManager.setValue(MenuConfigManager.TUNER_MODE,
                        tunermode);
            }
            mActionParentID = this.getIntent().getStringExtra("ActionParentID");
            satID = this.getIntent().getIntExtra("SatID", -1);
            mIsATVSource = mTV.isCurrentSourceATV();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mIsATVSource"+mIsATVSource);
            initTvscan(mContext, mActionID);
            if (mTV.isItaCountry() && satID == -1
                    && !CommonIntegration.getInstance().isCurrentSourceATVforEuPA()) {
                ScanThirdlyDialog thirdDialog = new ScanThirdlyDialog(mContext,5);
                //thirdDialog.setCancelable(false);
                thirdDialog.show();
            }else{
                startScan();
            }
        }

        /*private void showToast(){
            if (toast == null) {
                toast = Toast.makeText(mContext, R.string.menu_setup_ci_10s_answer_tip, Toast.LENGTH_SHORT);
            }
            toast.show();
        }*/

        private void cancleToast(){
            if (toast != null) {
                toast.cancel();
            }
        }

        public void onPause(){
            if (mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan))){
                cancleScan();
                isChannelSelected = true;
            }
            cancleToast();
            //DTV00580191
            mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
            super.onPause();
        }

    private void initTvscan(final Context context, String itemId) {
            setContentView(R.layout.scan_dialog_layout);

            this.mItemId = itemId;

            this.mContext = context;
            //lineLay = (LinearLayout) findViewById(R.id.menu_scan_dialog_id);
            if(!LiveTvSetting.isBootFromLiveTV()){//from settings
                getWindow().setBackgroundDrawableResource(R.drawable.screen_background_black);
            }
            mStateTextView = (TextView) findViewById(R.id.state);
            mStateTextView.setText(context.getString(R.string.menu_setup_channel_scan));

            loading = (Loading) findViewById(R.id.setup_tv_scan_loading);
            mScanprogressbar = (ProgressBar) findViewById(R.id.scanprogressbar);
            mScanprogressbar.setMax(100);
            mScanprogressbar.setProgress(0);

            mFinishpercentage = (TextView) findViewById(R.id.finishpercentage);
            mFinishpercentage.setText(String.format("%3d%s",0,"%"));
            mDVBSChannels = (TextView) findViewById(R.id.dvbsusedigital_channels);
            mNumberChannel = (TextView) findViewById(R.id.numberchannel);
            mAnalogChannel = (TextView) findViewById(R.id.analoguechannel);
            mIpChannel = (TextView) findViewById(R.id.ip_channels);
            mFvpWaitTip = (TextView) findViewById(R.id.fvp_scan_tip);
            TextView mTunerModeView = (TextView) findViewById(R.id.trun_mode);
            if(lnb){
                 mNumberChannel.setVisibility(View.GONE);
                 mAnalogChannel.setVisibility(View.GONE);
            }
            if(satID == -1 && ScanContent.isCountryUK()){
                mAnalogChannel.setVisibility(View.GONE);
                if(TVContent.getInstance(mContext).isSupportFvpScan()){
                    mIpChannel.setVisibility(View.VISIBLE);
                    mIpChannel.setText(String.format("%s%3d",mContext.getString(R.string.ip_channels) , 0));
                }else {
                    mIpChannel.setVisibility(View.GONE);
                }
			}
            if(mTV.isCOLRegion() && mTV.getCurrentTunerMode() == 1){
                mNumberChannel.setVisibility(View.GONE);
            }
             if(/*MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT) ||*/

                     (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_HK) || DataSeparaterUtil.getInstance().isSupportSeperateDTV())
                    && mTV.isCurrentSourceATV()){
                mTunerModeView.setVisibility(View.GONE);
            }else {
                String[] mTnuerArr = mContext.getResources().getStringArray(
                        R.array.menu_tv_tuner_mode_array_full_eu);
                int tunerMode = mTV.getCurrentTunerMode();
                if (tunerMode >= CommonIntegration.DB_SAT_OPTID) {
                    if (CommonIntegration.getInstance().isPreferSatMode()) {
                        mTunerModeView.setText(mTnuerArr[2]);
                    } else {
                        mTunerModeView.setText(mTnuerArr[3]);
                    }
                } else {
                    /*if (CommonIntegration.isCNRegion() && mIsATVSource) {
                        tunerMode = 1;
                    }*/
                    mTunerModeView.setText(mTnuerArr[tunerMode]);
                }
            }

            nowProgress = 0;

        }

        public void startScan() {
            mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
            onScanning = true;
            nowProgress = 0;
            mFinishpercentage.setText(String.format("%3d%s",0,"%"));
            mScanprogressbar.setProgress(0);
            this.loading.drawLoading();
            if (CommonIntegration.isUSRegion()) {
                if(satID==-1){
                    mAnalogChannel.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_ana) , 0));
                    mNumberChannel.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_dig) , 0));
                }
            } else if (CommonIntegration.isCNRegion()) {
                mNumberChannel.setText(String.format("%s%3d",getChannelString() , 0));
                mAnalogChannel.setText("");
            } else if(CommonIntegration.isEURegion()){
                    if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
                        if (mTV.isCurrentSourceATV()) {
                              mNumberChannel.setText("");
                              mAnalogChannel.setText(String.format("%s%3d",getChannelString(), 0));
                            }else if(mTV.isCurrentSourceDTV()){
                              mNumberChannel.setText(String.format("%s%3d",getChannelString(), 0));
                              mAnalogChannel.setText("");
                            }
                    }else{
                        if(satID==-1){
                            if(mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_BGM)){
                                mNumberChannel.setText(String.format("%s%3d",getChannelString() , 0));
                                mAnalogChannel.setText("");
                            }else {
                                mAnalogChannel.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_ana), 0));
                                mNumberChannel.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_dig), 0));
                            }
                        }
                    }
            } else if (CommonIntegration.isSARegion()) {
                mAnalogChannel.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_ana), 0));
                if (mTV.getCurrentTunerMode() == 1) {
                    mNumberChannel.setVisibility(View.INVISIBLE);
                } else {
                    mNumberChannel.setVisibility(View.VISIBLE);
                    mNumberChannel.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_dig), 0));
                }
            }
            mLastChannelId = EditChannel.getInstance(mContext).getCurrentChannelId();
            if (mItemId.equals(MenuConfigManager.TV_UPDATE_SCAN)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- UPDATE_SCAN");
                isChannelSelected = false;
                mTV.startScanByScanManager(ScannerManager.UPDATE_SCAN, mListener, null);
            } else if (mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN)
                    || mItemId.equals(MenuConfigManager.FACTORY_TV_FACTORY_SCAN)) {
                if (mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {
                    isChannelSelected = false;
                    mNeedChangeToFirstChannel = true;
                }
                if (CommonIntegration.isCNRegion() || CommonIntegration.isTVSourceSeparation()) {//for CN or HK
//                  MtkTvScan.getInstance().getScanDvbtInstance().startAutoScan();
                    mTV.startScanByScanManager(ScannerManager.DTV_SCAN, mListener, null);
                } else {
                    if (CommonIntegration.isUSRegion()) {
                        mCanCompleteScan = false;
                    }
                    mTV.startScanByScanManager(ScannerManager.FULL_SCAN, mListener, new ScanParams());
                }
            } else if (mItemId.equals(MenuConfigManager.TV_UPDATE_SCAN_DVBT_UPDATE)) {
                isChannelSelected = false;
                if(mTV.isUKCountry()){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- DVBT Update Scan ONLY DTV");
                    mTV.startScanByScanManager(ScannerManager.DTV_UPDATE, mListener, null);
                }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---DVBT Update Scan startScan");
                    mTV.startScanByScanManager(ScannerManager.UPDATE_SCAN, mListener, null);
                }
            } else if (mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBT)) {
                isChannelSelected = false;
                mNeedChangeToFirstChannel = true;
                mTV.getScanManager().setIsBroadCastScan(mIsBroadcastScan);
                mTV.getScanManager().setmIsAdviseScan(mIsAdviseScan);
                if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)){
                    if (mTV.isCurrentSourceATV() && !mTV.isChineseTWN()) {
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---ATV_SCAN---");
                       //mTV.startScanByScanManager(ScannerManager.ATV_SCAN, mListener, null);
                       mHandler.removeMessages(MessageType.MESSAGE_START_ATV_SCAN);
                       mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_START_ATV_SCAN, 800);
                   } else {
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---DTV_SCAN---");
                       mTV.startScanByScanManager(ScannerManager.DTV_SCAN, mListener, null);
                   }
                }else{
                    if (mTV.isUKCountry()) {
                        mTV.startScanByScanManager(ScannerManager.DTV_SCAN, mListener, null);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---ONLY SCAN DTV");
                    }else if(mTV.isCOLRegion() && mTV.getCurrentTunerMode() == CommonIntegration.DB_CAB_OPTID){//cable only atv NTSC scan
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---COL country cable ATV_SCAN---");
                        mTV.getScanManager().setRollbackCleanChannel();
                        mHandler.removeMessages(MessageType.MESSAGE_START_ATV_SCAN);
                        mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_START_ATV_SCAN, 800);
                    }else{
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---DVBT Full Scan startScan");
                         mTV.startScanByScanManager(ScannerManager.FULL_SCAN, mListener, null);
                    }
              }
            }else if(mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_BGM)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---DTV_BGM_SCAN---");
                mTV.startScanByScanManager(ScannerManager.DTV_BGM, mListener, null);
            }
            else if (mItemId.equals(MenuConfigManager.M7_LNB_Scan)) {
                mTV.startScanByScanManager(ScannerManager.M7_LNB_Scan, mListener, null);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--start M7 LNB Scan");
            } else if (mItemId.equals(MenuConfigManager.TV_SYSTEM)) {
                if (mActionParentID != null) {
                    if (mActionParentID.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {
                        mNeedChangeToFirstChannel = true;
//                      ScanPalSecamRet ret = MtkTvScan.getInstance().getScanPalSecamInstance().startAutoScan();
                        mTV.startATVAutoOrUpdateScan(ScannerManager.ATV_SCAN, mListener, null);
                    } else if (mActionParentID.equals(MenuConfigManager.TV_UPDATE_SCAN)) {
                        mTV.startATVAutoOrUpdateScan(ScannerManager.UPDATE_ATV_SCAN, mListener, null);
                    }
                }
            } else if (mItemId.equals(MenuConfigManager.COLOR_SYSTEM)) {
                if (mActionParentID != null) {
                    if (mActionParentID.equals(MenuConfigManager.TV_CHANNEL_SCAN)) {
                        mNeedChangeToFirstChannel = true;
                        mTV.startATVAutoOrUpdateScan(ScannerManager.ATV_SCAN, mListener, null);
                    } else if (mActionParentID.equals(MenuConfigManager.TV_UPDATE_SCAN)) {
                        mTV.startATVAutoOrUpdateScan(ScannerManager.UPDATE_ATV_SCAN, mListener, null);
                    }
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- startScan");
            }
            // Only DVBS TP scan use TVScan dialog.
            if (mTV.getCurrentTunerMode() >= CommonIntegration.DB_SAT_OPTID
                    && (mItemId.equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING)
                            || mItemId.equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING_TP))) {
                startDVBSTPScan();
            }
        }

        private void updateSatelliteName() {
            if(satID!=-1){
                mDVBSChannels.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_dig), 0));
                mAnalogChannel.setText(mContext.getString(R.string.menu_setup_satellite_name) + mTV.getScanManager().getFirstSatName());
                mNumberChannel.setText(getSatName(mTV.getScanManager().getFirstSatName()));
            }
        }

        /**
         *
         */
        private void startDVBSTPScan() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startDVBSTPScan()");
            if(satID!=-1){
                mDVBSChannels.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_dig), 0));
                mAnalogChannel.setText(mContext.getString(R.string.menu_setup_satellite_name));
                mNumberChannel.setText(String.format("%s: ",getString(R.string.satellites)));
                ScanParams params = prepareDVBSTPParam(satID);
                mTV.getScanManager().startScan(ScannerManager.FULL_SCAN,
                        mListener, params);
            }
        }

        private ScanParams prepareDVBSTPParam(int satID) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareDVBSTPParam()");
            DVBSSettingsInfo params = new DVBSSettingsInfo();
            params.context = mContext;
            params.getSatelliteInfo().setSatlRecId(satID);
            params.scanMode=DVBSSettingsInfo.TP_SCAN_MODE;
            params.mIsDvbsNeedCleanChannelDB = false;
            return params;
        }

        private void updateScanProgress(TvCallbackData backData) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "[Channels ]: " + "  [Progress]: " + backData.param2
                    + "param3:" + backData.param3);
            if(onScanning){
                mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
            }
            switch (MarketRegionInfo.getCurrentMarketRegion()) {
                case MarketRegionInfo.REGION_US:
                case MarketRegionInfo.REGION_SA:
                    if (backData.param4 == 1) {
                        mAnalogChannel.setText(String.format("%s%3d",mContext
                                .getString(R.string.menu_setup_channel_scan_ana)
                                , backData.param3));
                    } else {
                        mNumberChannel.setText(String.format("%s%3d",mContext
                                .getString(R.string.menu_setup_channel_scan_dig)
                                , backData.param3));
                    }
                    updateCommonScanProgress(backData);
                    break;
                case MarketRegionInfo.REGION_CN:
                    mNumberChannel.setText(String.format("%s%3d",getChannelString() , backData.param3));
                    updateCommonScanProgress(backData);
                    break;
                case MarketRegionInfo.REGION_EU:
                    updateEUScanProgress(backData);
                    break;
                default:
                    return;
            }
        }

        private void updateCommonScanProgress(TvCallbackData backData) {
            int progress = backData.param2;
            if (mTV.getActionSize() > 0 || nowProgress == 50) {
                progress = nowProgress + progress/2;
            }
            progress=Math.max(progress, mScanprogressbar.getProgress());
            mScanprogressbar.setProgress(progress);
            mFinishpercentage.setText(String.format("%3d%s", progress , "%"));
            Log.d(TAG, "scanProgress=="+progress);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- updateCommonScanProgress----"+progress);
            if (progress >= 100 && backData.param1 == 1) {//progress is 100 don't mean scan complete
                onScanning = false;
//              if (isShowing()) {
//                  mSelectTextView.setVisibility(View.GONE);
//                  mEnterTextView.setVisibility(View.VISIBLE);
//                  mEnterTextView.setText(mContext.getString(R.string.menu_scan));
//                  mExitTextView.setText(mContext.getString(R.string.menu_back));
                    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_done));
//              }
                loading.stopDraw();
                loading.setVisibility(View.INVISIBLE);
//              //fix CR DTV00581360
//              mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
//              mHandler.sendEmptyMessageDelayed(MessageType.MENU_AUTO_EXIT_MESSAGE, MessageType.delayMillis2);
            }
        }

        private void updateEUScanProgress(TvCallbackData backData) {
            /* backData.param4 == 1==DTV;backData.param4 == 2==ATV; */
            if(satID==-1){
                if (backData.param4 == 1 || backData.param4 == 0) {
                    mAnalogChannel.setText(String.format("%s%3d", mContext
                            .getString(R.string.menu_setup_channel_scan_ana)
                            , backData.param3));
                } else if (backData.param4 == 2) {
                    mNumberChannel.setText(String.format("%s%3d", mContext
                            .getString(R.string.menu_setup_channel_scan_dig)
                            , backData.param3));
                }
            }else{
                 if (backData.param4 == 2) {
                     mDVBSChannels.setText(String.format("%s%3d",mContext
                            .getString(R.string.menu_setup_channel_scan_dig)
                            , backData.param3));
                }
            }
            updateCommonScanProgress(backData);
        }

       /* private int countWholeProgress(TvCallbackData backData) {
            if (CommonIntegration.isEURegion()) {
                if (scanProgressDigital != 100) {
                    scanProgressDigital = backData.param2;
                } else {
                    scanProgressAnalog = backData.param2;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"countWholeProgress(),itemID:"+mItemId);
                    // debug:
                if (mItemId.equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBT)
                        || mItemId.equals(MenuConfigManager.TV_UPDATE_SCAN_DVBT_UPDATE)) {
                    return scanProgressAnalog / 2 + scanProgressDigital / 2;
                }
                return backData.param2;
            } else {
                return backData.param2;
            }
        }*/

        /*private boolean writeAutotestScanFile(){
            Log.d(TAG, "writeAutotestScanFile()");
            boolean succ = true;
            try {
              succ = false;
              File atvfile = new File(FileSystemPath.LINUX_TMP_PATH + "/autotest/scan_atv_with_signal");
              File dtvFile = new File(FileSystemPath.LINUX_TMP_PATH + "/autotest/scan_dtv_with_signal");
              if (TIFChannelManager.getInstance(mContext).hasATVChannels()) {
                  succ = atvfile.createNewFile();
              }
              if(TIFChannelManager.getInstance(mContext).hasDTVChannels()){
                  succ = dtvFile.createNewFile();
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
            return succ;
        }*/

        public void cancleScan() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- call cancel scan");
            mTV.cancelScan();
            count = 0;
            onScanning = false;
            showCancelScanInfo();
            clearFvpMsg();
        }

        public void continueScan() {
            if (count != 2) {
                count = 2;
            }
            startScan();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ---- continueScan----");
            mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
            mHandler.postDelayed(runnable, 1000);
        }

        public Runnable runnable = new Runnable() {
            public void run() {
                // TODO continue to scan,change the TextView
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "runnable>>continue scan");
                if (mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan_cancel))
                        || mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan_done))) {
                    return;
                }
                onScanning=true;
                mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
                loading.drawLoading();
                if (loading.getVisibility() != View.VISIBLE) {
                    loading.setVisibility(View.VISIBLE);
                }
                if (mNumberChannel.getVisibility() != View.VISIBLE) {
                    mNumberChannel.setVisibility(View.VISIBLE);
                }
            }
        };

        private String getChannelString() {
            if (!mIsATVSource) {
                return mContext.getString(R.string.menu_setup_channel_scan_dig);
            } else {
                return mContext.getString(R.string.menu_setup_channel_scan_ana);
            }
        }

        /**
         * show cancel scan information
         */
        private void showCancelScanInfo() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCancelScanInfo");
            if (mHandler.hasCallbacks(runnable)) {
                mHandler.removeCallbacks(runnable);
            }
            if(!mContext.getString(R.string.menu_setup_channel_scan_done).equals(mStateTextView.getText())){
                mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_cancel));
            }
            loading.stopDraw();
            loading.setVisibility(View.INVISIBLE);
        }

        public void showCompleteInfo() {
            mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_done));
            mScanprogressbar.setProgress(100);
            mFinishpercentage.setText(String.format("%3d%s",100,"%"));
            mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
            mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL, MessageType.delayMillis6);
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                  finish();
                }
              }, 1000);
        }

        public void setScanComplete() {
            mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_done));
        }

        /**
         * show scanning state
         * @return TRUE if scan is completed, FALSE if scan is canceled
         */
        public boolean showScanStateInfo(int keyCode, KeyEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- showScanStateInfo");
            if (mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan_done))) {
                loading.stopDraw();
                loading.setVisibility(View.INVISIBLE);
//                finish();
                return super.onKeyDown(keyCode, event);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- showScanStateInfo cancelScan");
                cancleScan();
                return false;
            }
        }

        // for volume down/up
        public boolean onKeyDown(int keyCode, KeyEvent event) {
//          if (mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan))) {
//              mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
//          }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown"+keyCode);
            switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*****Scan Count =  *******"+ count);
                        if (mHandler.hasMessages(MessageType.MESSAGE_START_SCAN)) {
                            mHandler.removeMessages(MessageType.MESSAGE_START_SCAN);
                            mTV.getScanManager().rollbackChannelsWhenScanNothingOnUIThread();
                            mHandler.removeMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL);
                            mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL,
                                MessageType.delayMillis5);
                            showScanStateInfo(keyCode,event);
                            return true;
                          }
                        if (count == 0) {
                            /*if(!isChannelSelected){
    //                          toast.setGravity(Gravity.CENTER, 180, 50);
                                showToast();
                                Log.d(TAG, "toast show");
                                return true;
                            }*/
                            loading.stopDraw();
                            loading.setText("");
                            //isScanning = false;
//                            finish();
                            return super.onKeyDown(keyCode, event);
                        } else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*****showScanStateInfo  *******"+mItemId + "   mTV.isScanning():" + mTV.isScanning());
                            /*if (!isChannelSelected && !mHandler.hasMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL)) {
                                mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL, MessageType.delayMillis5);
                            }*/
                            if (showScanStateInfo(keyCode,event)) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
                            }
//                          mHandler.sendEmptyMessageDelayed(MessageType.MENU_AUTO_EXIT_MESSAGE,
//                                  MessageType.delayMillis2);
                            return true;
                        }
                    /*case KeyEvent.KEYCODE_DPAD_CENTER:
                        Log.e(TAG, "22222"+onScanning+",tvscanning"+mTV.isScanning()+",isChannelSelected"+isChannelSelected);
                        if(onScanning||mTV.isScanning()){
                            return false;
                        }else{
                            onScanning = true;
                        }
//                      mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
                        if (mStateTextView.getText().toString().equals(
                                getString(R.string.menu_setup_channel_scan_done))) {
                            continueScan();
                            return true;
                        }
                        if (count == 1) {
                            return true;
                        }
                        ++count;
                        mStateTextView.setText(mContext.getString(R.string.menu_tv_scann_allchannels));
                        continueScan();
//                      mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
                        return true;*/
                    case KeyEvent.KEYCODE_MENU:
//                        cancleScan();
//                        loading.stopDraw();
//                        finish();
                        return true;
                default:
                    break;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if(keyCode==KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }

        /*@Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchcode"+keyCode);
            if(isFocused){
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)&&event.getRepeatCount() == 0) {
                    Log.e(TAG, "11111"+onScanning+",tvscanning"+mTV.isScanning()+",isChannelSelected"+isChannelSelected);
                    if(onScanning||mTV.isScanning()){
                         return false;
                     }else{
                         onScanning = true;
                     }
                     if (mStateTextView.getText().toString().equals(
                             getString(R.string.menu_setup_channel_scan_done))) {
                         continueScan();
                         return true;
                     }
                     if (count == 1) {
                         return true;
                     }
                     ++count;
                     mStateTextView.setText(mContext.getString(R.string.menu_tv_scann_allchannels));
                     continueScan();
                     return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }*/

        /**
         * select last or first channel after scan complete
         */
        private void selectChannel() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel >>>>>" + getIntent().getBooleanExtra("need_select_channel",false)+","+LiveTvSetting.isBootFromLiveTV());
            if(!LiveTvSetting.isBootFromLiveTV() && !getIntent().getBooleanExtra("need_select_channel",false)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannel>>>>> return due to not from LiveTV");
                isChannelSelected = true;
                return;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannel>>>>>" + mLastChannelId + ">>>" + mNeedChangeToFirstChannel + ">>" + TIFChannelManager.getInstance(mContext).isChannelsExist());
            new Thread(){
                public void run() {
                    if (!mTV.isScanning()) {
                        if(satID != -1){
                            if(mTV.getScanManager().hasChannels()){
                                int frequency = ScanContent.getDVBSTransponder(satID).i4Frequency;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TP selectChannel frequency="+frequency);
                                TVContent.getInstance(mContext).changeChannelByQueryFreq(frequency);
                            }
                        }else {
                            if (mNeedChangeToFirstChannel) {
                                //EditChannel.getInstance(mContext).selectChannel(-1);

                                TIFChannelInfo info = TIFChannelManager.getInstance(mContext).getFirstChannelForScan();
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel>>>>>" + info);
                                if(info != null){
                                    TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(info);
                                    SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                                }else {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel>>>>> null");
                                    EditChannel.getInstance(mContext).selectChannel(mLastChannelId);
                                }

                            } else {
                                EditChannel.getInstance(mContext).selectChannel(mLastChannelId);
                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"select broadcast chlist type");
                            SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"set cam Scan complete");
                        com.mediatek.twoworlds.tv.MtkTvCI.getInstance(0).setScanComplete();
                    }
                };
            }.start();
        }

        /**
         * for DVBS scan get
         * @param satName current satellite name
         * @return current sat index/total num, such as 1/4
         */
        public CharSequence getSatName(String satName) {
            // TODO Auto-generated method stub
            String scanIndex=String.format("%d/%d", mTV.getScanManager().getDVBSCurrentIndex()+1,mTV.getScanManager().getDVBSTotalSatSize());
            return String.format("%s: %4s",getString(R.string.satellites),scanIndex);
        }

       /* private DialogInterface.OnKeyListener TvScanOnKeyListener() {
            return new DialogInterface.OnKeyListener() {

                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    int action = event.getAction();
                    if (keyCode == KeyEvent.KEYCODE_MENU) {
                        cancleScan();
                        loading.stopDraw();
                        finish();
                        return true;
                    }

                    if (action != KeyEvent.ACTION_DOWN) {
                        return false;
                    }
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        return onBackPressOnTvScan();
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        return onEnterPressOnTvScan();
                    default:
                        break;
                    }

                    return false;
                }

                private boolean onEnterPressOnTvScan() {
                    if (onScanning) {
                        return false;
                    }

                    onScanning = true;
//                  mHandler.removeMessages(MessageType.MENU_AUTO_EXIT_MESSAGE);
                    if (mStateTextView.getText().toString().equals(getString(R.string.menu_setup_channel_scan_done))) {
                        continueScan();
                        return true;
                    }
                    if (count == 1) {
                        return true;
                    }
                    ++count;
                    mStateTextView.setText(mContext
                            .getString(R.string.menu_tv_scann_allchannels));
                    continueScan();
                    return true;
                }

                private boolean onBackPressOnTvScan() {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*****Scan Count =  *******" + count);
                    if (count == 0) {
                        if(!isChannelSelected){
        //                  toast.setGravity(Gravity.CENTER, 180, 50);
                            showToast();
                            Log.d(TAG, "toast show");
                            return true;
                        }
                        loading.stopDraw();
                        loading.setText("");
                        finish();

//                      mHandler.sendEmptyMessageDelayed(MessageType.MENU_AUTO_EXIT_MESSAGE, MessageType.delayMillis2);
                        isScanning = false;
                        return true;
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*****showScanStateInfo  *******");
//                      mHandler.sendEmptyMessageDelayed(MessageType.MENU_AUTO_EXIT_MESSAGE, MessageType.delayMillis2);
                        return true;
                    }
                }

            };
        }*/

        /**
         * UI Module: PT
         */
        public void showTRDSaveChannelConfirmDialog() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDSaveChannelConfirmDialog()");
            SaveChannelsConfirmDialog dialog=new SaveChannelsConfirmDialog(mContext);
            dialog.showConfirmDialog();
        }
        @Override
        protected void onStop() {
            Log.d(TAG, "onStop()");
            // TODO Auto-generated method stub

            super.onStop();
        }
        @Override
        protected void onDestroy() {
            //mTV.setScanListener(null);
            mListener = null;
            mHandler.removeCallbacksAndMessages(null);
            mTV.mHandler = null;
            RxBus.instance.send(new ActivityDestroyEvent(getClass()));
            super.onDestroy();
        }
        boolean isFocused;

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            super.onWindowFocusChanged(hasFocus);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*****onWindowFocusChanged  *******"+hasFocus);
              isFocused = hasFocus;
        }
}
