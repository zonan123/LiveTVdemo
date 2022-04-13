
package com.mediatek.wwtv.tvcenter.oad;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.TypedValue;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.commonview.CustListView;
import com.mediatek.wwtv.tvcenter.commonview.Loading;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;

public class NavOADActivity extends BaseActivity {

    private static final String TAG = "NavOADActivity";
    private final static boolean DEBUG = false;

    private Context mContext;
    private static NavOADActivity oadActivity = null;
    private boolean mIsStop;
    private boolean isRemindMeLater = true ;
    private NavOADController controller;
    private BaseOADState currentState;

    private MyHandler mHandler;

    private TextView mOADSubTitle;
    private TextView mOADWarnningMsg;
    private TextView mOADSubTitle2;
    private ProgressBar mProgress;
    private TextView mProgressStr;
    private CustListView mBtnList;
    private Loading mLoadingPoint;

    private TextView mTvFootSelect;
    private TextView mTvFootNext;

    private int mListViewWidth = 0;

    private int mCountDownInt = 15;
    private int mJumpChannelRemindTime = 10;

    private String mPackagePathAndName = null;

    final static int AUTO_DOWNLOAD_COUNTDOWN = 0;
    final static int NOTIFY_CALL_BACK = 1;
    final static int DELAY_SHOW_CONFIRM_UI = 2;
    final static int DOWN_LOAD_TIME_OUT_MSG = 3;
    final static int AUTO_JUMP_CHANNEL = 4;
    final static int AUTO_EXIT_OAD = 5;
    final static int AUTO_EXIT_TIME = 5*60*1000;

    private int currentDownloadProgress = 0;
    private boolean downLoadRetry = false;

    Intent intent;

    private String mScheuleInfo;

    /*
     * notifyOADMessage This API is used to notify OAD to show correct UI. Overrides:
     * notifyOADMessage(...) in MtkTvTVCallbackHandler Parameters: messageType 0:
     * OAD_INFORM_FILE_FOUND 1: OAD_INFORM_FILE_NOT_FOUND 2: OAD_INFORM_NEWEST_VERSION 3:
     * OAD_INFORM_SCHEDULE 4: OAD_INFORM_LINKAGE 5: OAD_INFORM_DOWNLOAD_PROGRESS 6:
     * OAD_INFORM_DOWNLOAD_FAIL 7: OAD_INFORM_INSTALL 8: OAD_INFORM_INSTALL_PROGRESS 9:
     * OAD_INFORM_INST_FAIL 10: OAD_INFORM_SUCCESS scheduleInfo if messageType ==
     * OAD_INFORM_SCHEDULE, scheduleInfo contains the schedule time info progress if messageType ==
     * OAD_INFORM_DOWNLOAD_PROGRESS, progress indicates the download progress if messageType ==
     * OAD_INFORM_INSTALL_PROGRESS, progress indicates the install progress autoDld if messageType
     * == OAD_INFORM_FILE_FOUND 0 -- manual select to download, 1 -- auto count down to download
     * argv5 reserved Returns: 0: callback success, others: callback fail.
     */

    private final static int OAD_INFORM_FILE_FOUND = 0;
    private final static int OAD_INFORM_FILE_NOT_FOUND = 1;
    private final static int OAD_INFORM_NEWEST_VERSION = 2;
    private final static int OAD_INFORM_SCHEDULE = 3;
    private final static int OAD_INFORM_LINKAGE = 4;
    private final static int OAD_INFORM_DOWNLOAD_PROGRESS = 5;
    private final static int OAD_INFORM_DOWNLOAD_FAIL = 6;
    private final static int OAD_INFORM_INSTALL = 7;
    private final static int OAD_INFORM_INSTALL_PROGRESS = 8;
    private final static int OAD_INFORM_INST_FAIL = 9;
    private final static int OAD_INFORM_SUCCESS = 10;
    private final static int OAD_MTKTVAPI_INFORM_JUMP_SCHEDULE = 11;
    private final static int OAD_MTKTVAPI_INFORM_AUTO_DOWNLOAD_WITH_BGM = 13;

//    BootBroadcastReceiver mReceiver = new BootBroadcastReceiver();

    static class MyHandler extends Handler {
        WeakReference<NavOADActivity> mActivity;

        MyHandler(NavOADActivity activity) {
            mActivity = new WeakReference<NavOADActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessageMsg.what = "+ msg.what);
            switch (msg.what) {
                case AUTO_DOWNLOAD_COUNTDOWN:

                    if (mActivity != null && mActivity.get() != null
                            && mActivity.get().getCurrentState() != null) {
                        if (mActivity.get().getCurrentState().step == Step.DETECT) {
                            mActivity.get().mCountDownInt = 15;
                            return;
                        }

                        mActivity.get().mCountDownInt--;
                        if (mActivity.get().mCountDownInt > 0) {
                            sendEmptyMessageDelayed(AUTO_DOWNLOAD_COUNTDOWN, 1000);
                            mActivity.get().updateAutoDownloadMsg(
                                    mActivity.get().mCountDownInt);
                        } else {
                            mActivity.get().getCurrentState().nextPage();
                            mActivity.get().mCountDownInt = 15;
                        }
                    }
                    break;
                case NOTIFY_CALL_BACK:
                    Bundle bundle = msg.getData();
                    mActivity.get().onNotifyOADMessage(bundle.getInt("arg1"),
                            bundle.getString("arg2"), bundle.getInt("arg3"),
                            bundle.getBoolean("arg4"), bundle.getInt("arg5"));
                    break;

                case DELAY_SHOW_CONFIRM_UI:
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    mActivity.get().onNotifyOADMessage(data.param1, data.paramStr1,
                            data.param2, data.paramBool1, data.param3);
                    break;
                case DOWN_LOAD_TIME_OUT_MSG:
                    // mActivity.get().setCurrentState(new DownloadFailOADState());
                    mActivity.get().setDownloadFailState();
                    break;
                case AUTO_JUMP_CHANNEL:
                    if (mActivity != null && mActivity.get() != null
                            && mActivity.get().getCurrentState() != null) {
                        mActivity.get().mJumpChannelRemindTime--;
                        if (mActivity.get().mJumpChannelRemindTime > 0) {
                            sendEmptyMessageDelayed(AUTO_JUMP_CHANNEL, 1000);
                            mActivity.get().updateAutoJumpChannelMsg(
                                    mActivity.get().mJumpChannelRemindTime);
                        } else {
                            ((JumpChannelState) mActivity.get().getCurrentState())
                                    .nextPage();
                            mActivity.get().mJumpChannelRemindTime = 10;
                        }
                    }
                    break;

                case AUTO_EXIT_OAD:
                    mActivity.get().stopOAD();
                    break;
                default:
                    // mSelf.doSomething();
                    break;
            }
            super.handleMessage(msg);
        }

    }

    enum Step {
        DEFAULT, DETECT, DOWNLOAD_CONFIRM, DOWNLOADING, FLASH_CONFIRM, FLASHING, RESTART_CONFIRM
    };

    interface IStepSquence {
        void lastPage();

        void nextPage();

        void updateUi();
    }

    class BaseOADState implements IStepSquence {

        boolean onBackPress = true;
        Step step = Step.DEFAULT;

        public BaseOADState() {
            super();
            step = Step.DEFAULT;
            // updateUi();
        }

        @Override
        public void nextPage() {
            // TODO Auto-generated method stub
            if (getmHandler() != null) {
                getmHandler().removeMessages(AUTO_DOWNLOAD_COUNTDOWN);
                getmHandler().removeMessages(AUTO_EXIT_OAD);
            }
            mCountDownInt = 15;
        }

        @Override
        public void updateUi() {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateUi");
        }

        public void clearTask() {
            // TODO Auto-generated method stub

        }

        @Override
        public void lastPage() {
            // TODO Auto-generated method stub

        }
    }

    class DetectOADState extends BaseOADState {

        @Override
        public void updateUi() {
            step = Step.DETECT;
            showScanning();
            getController().remindMeLater();
            getController().manualDetect();
        }

        @Override
        public void clearTask() {
            // TODO Auto-generated method stub
            super.clearTask();
            getController().cancelManualDetect();
            finish();
        }

    }

    class DownloadConfirmOADState extends BaseOADState {

        @Override
        public void nextPage() {
            // TODO Auto-generated method stub
            super.nextPage();
            getController().acceptDownload();
            setCurrentState(new DownloadingOADState());
        }

        @Override
        public void updateUi() {
            step = Step.DOWNLOAD_CONFIRM;
//            getController();
            boolean enable = NavOADController.getOADAutoDownload(mContext);
            showDownloadConfirm(enable);
        }

        @Override
        public void clearTask() {
            // TODO Auto-generated method stub
            super.clearTask();
            stopOAD();
        }

    }

    class DownloadingOADState extends BaseOADState {

        @Override
        public void updateUi() {
            getmHandler().sendEmptyMessageDelayed(DOWN_LOAD_TIME_OUT_MSG, 60 * 60 * 1000);
            step = Step.DOWNLOADING;
            showProgress();
        }

        @Override
        public void clearTask() {
            // TODO Auto-generated method stub
            super.clearTask();
            // stop download
            getController().cancelDownload();
            // setCurrentState(new DownloadFailOADState());
            stopOAD();
        }
    }

    class DownloadFailOADState extends BaseOADState {

        @Override
        public void updateUi() {
            // getController().cancelDownload();
            showDownloadFail();
        }
    }

    class FlashConfirmOADState extends BaseOADState {

        @Override
        public void nextPage() {
            super.nextPage();
            getController().acceptFlash();
            setCurrentState(new FlashingState());
        }

        @Override
        public void updateUi() {
            showFlashConfirm();
        }
    }

    class FlashingState extends BaseOADState {

        @Override
        public void updateUi() {
            step = Step.FLASHING;
            showProgress();
        }
    }

    class FlashFailState extends BaseOADState {

        @Override
        public void updateUi() {
            showFlashFail();
        }
    }

    class RestartConfirmOADState extends BaseOADState {

        @Override
        public void updateUi() {
            showRestartConfirm();
        }
    }

    class ScheduleInfoState extends BaseOADState {

        @Override
        public void updateUi() {
            mScheuleInfo = getController().getScheduleInfo();
            showScheduleInfo(mScheuleInfo);// acceptScheduleOAD
        }
    }

    class JumpChannelState extends BaseOADState {

        @Override
        public void updateUi() {
            mJumpChannelRemindTime = 10;
            showJumpChannelInfo();
        }

        @Override
        public void nextPage() {
//            super.nextPage();
            if (null != getmHandler()) {
                getmHandler().removeMessages(AUTO_JUMP_CHANNEL);
            }
            getController().acceptJumpChannel();
            stopOAD();
        }

    }

    class BGMAutoDownloadState extends BaseOADState {
        @Override
        public void updateUi() {
            // TODO Auto-generated method stub
            showBGMAutoDownloadToast();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        oadActivity = this;
        this.setCurrentState(new BaseOADState());
        intent = getIntent();

        setWindowParams();

        initViews();

        setmHandler(new MyHandler(this));
        setController(new NavOADController(this));
    }

    private void setWindowParams() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nav_oad_layout);

        LinearLayout setupLayout = (LinearLayout) findViewById(R.id.nav_oad_layout);
        TypedValue sca = new TypedValue();
        getResources().getValue(R.dimen.nav_oad_window_size_width,sca ,true);
        float w  = sca.getFloat();
        int width = (int)(ScreenConstant.SCREEN_WIDTH * w);
        getResources().getValue(R.dimen.nav_oad_window_size_height,sca ,true);
        float h  = sca.getFloat();
        int height = (int)(ScreenConstant.SCREEN_HEIGHT * h);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width,
                height);
        lp.gravity = Gravity.CENTER;
        setupLayout.setLayoutParams(lp);

        mListViewWidth = width / 3;
    }

    private void initViews() {
        // TODO Auto-generated method stub
    	TextView mTvFootExit;

        mOADSubTitle = (TextView) this.findViewById(R.id.nav_oad_intro);
        mOADSubTitle2 = (TextView) this
                .findViewById(R.id.nav_oad_download_wait);

        mOADWarnningMsg = (TextView) this
                .findViewById(R.id.nav_oad_warning_msg);
        mOADWarnningMsg.setVisibility(View.VISIBLE);

        mProgressStr = (TextView) this
                .findViewById(R.id.nav_oad_progress_percent);

        mLoadingPoint = (Loading) this.findViewById(R.id.nav_oad_programming);

        mProgress = (ProgressBar) this
                .findViewById(R.id.nav_oad_progress_progressbar);
        mProgress.setMax(100);

        mProgress.setVisibility(View.INVISIBLE);
        mProgressStr.setVisibility(View.INVISIBLE);

        updateProgress(0);

        mBtnList = (CustListView) this.findViewById(R.id.nav_oad_op_list);

        mBtnList.setDivider(null);
        mBtnList.setVisibility(View.INVISIBLE);
        mBtnList.getLayoutParams().width = mListViewWidth;

        mTvFootSelect = (TextView) this
                .findViewById(R.id.nav_oad_bottom_select);
        mTvFootNext = (TextView) this.findViewById(R.id.nav_oad_bottom_next);
        mTvFootExit = (TextView) this.findViewById(R.id.nav_oad_bottom_exit);
        mTvFootExit.setVisibility(View.VISIBLE);
    }

    private void hiddenAll() {
        if (getmHandler() != null) {
            getmHandler().removeMessages(AUTO_DOWNLOAD_COUNTDOWN);
            getmHandler().removeMessages(AUTO_EXIT_OAD);
        }

        mOADSubTitle.setText("");
        mOADSubTitle2.setText("");

        mOADSubTitle.setVisibility(View.INVISIBLE);
        mOADSubTitle2.setVisibility(View.INVISIBLE);

        mOADWarnningMsg.setText("");
        mOADWarnningMsg.setVisibility(View.INVISIBLE);

        try {
            mLoadingPoint.setVisibility(View.INVISIBLE);
            mLoadingPoint.stopDraw();
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception");
        }
        if (mProgress != null) {
        	mProgress.setVisibility(View.INVISIBLE);
        }
        mProgressStr.setText("");
        mProgressStr.setVisibility(View.INVISIBLE);

        mBtnList.setVisibility(View.INVISIBLE);

        mTvFootSelect.setVisibility(View.INVISIBLE);
        mTvFootNext.setVisibility(View.INVISIBLE);

        clearTextViewStr();
    }

    private void showScanning() {
        final long scanCancel = 0;
        hiddenAll();

        mOADSubTitle.setText(getString(R.string.nav_oad_scanning));
        mOADSubTitle.setVisibility(View.VISIBLE);

        mLoadingPoint.drawLoading();
        mLoadingPoint.setVisibility(View.VISIBLE);

        mBtnList.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_cancel);

        // mBtnList.setAdapter(new StateInitDiskBtnAdapter(this, btns));
        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (scanCancel == id) {
                    stopOAD();
                }
            }
        });
    }

    /**
     * after receiver fail MSG from MW. will show this page.
     */
    private void showScanningFailResult() {
        hiddenAll();

        mOADSubTitle.setVisibility(View.VISIBLE);
        mOADSubTitle.setText(getString(R.string.nav_oad_scanning_result_fail));
    }

    /**
     * after receiver fail MSG from MW. will show this page.
     */
    private void showScanningFailResult(String str) {
        showScanningFailResult();
        mOADSubTitle.setText(str);
    }

    private void showDownloadConfirm(boolean auto) {
        hiddenAll();

        final int accept = 0;
        final int remindMeLater = 1;

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDownloadConfirm||auto =" + auto);
        if (auto) {
            updateAutoDownloadMsg(mCountDownInt);
            getmHandler().sendEmptyMessage(AUTO_DOWNLOAD_COUNTDOWN);
        } else {
            mOADSubTitle
                    .setText(getString(R.string.nav_oad_manual_download_confirm));
          //exit in 5 min
            getmHandler().sendEmptyMessageDelayed(AUTO_EXIT_OAD,AUTO_EXIT_TIME);
        }
        mOADSubTitle.setVisibility(View.VISIBLE);

        mBtnList.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_accept_remind_me_later);

        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDownloadConfirm||position =" + position);
				switch (position) {
				case accept:
					if (getCurrentState() != null
							&& getCurrentState().step == Step.DOWNLOAD_CONFIRM) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDownloadConfirm||CurrentState:DOWNLOAD_CONFIRM");
						getCurrentState().nextPage();
					} else {
						getController().acceptDownload();
						setCurrentState(new DownloadingOADState());
					}
					break;
				case remindMeLater:
					// getController().remindMeLater();
					stopOAD();
					break;
				default:
					break;
				}
            }
        });

        if (mBtnList.getChildAt(0) != null) {
            mBtnList.getChildAt(0).requestFocus();
        }

        mTvFootSelect.setVisibility(View.VISIBLE);
        mTvFootNext.setVisibility(View.VISIBLE);
    }

    private void updateAutoDownloadMsg(int seconds) {
        String str = getString(R.string.nav_oad_auto_download_confirm);
        mOADSubTitle.setText(String.format(str, seconds));
    }

    private void updateAutoJumpChannelMsg(int lastTime) {
        String str = getString(R.string.nav_oad_schedule_oad_change_channel_confirm);
        mOADSubTitle.setText(String.format(str, lastTime));
    }

    private void showFlashConfirm() {
        hiddenAll();

        final int accept = 0;
        final int reject = 1;

        mOADSubTitle.setVisibility(View.VISIBLE);
        mOADSubTitle.setText(getString(R.string.nav_oad_flash_confirm));

        mBtnList.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_accept_reject);

        // mBtnList.setAdapter(new StateInitDiskBtnAdapter(this, btns));
        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFlashConfirm||position =" + position);
				switch (position) {
				case accept:
					if (getCurrentState() != null
							&& getCurrentState().step == Step.FLASH_CONFIRM) {
						getCurrentState().nextPage();
					} else {
						getController().acceptFlash();
						setCurrentState(new FlashingState());
					}
					break;
				case reject:
					// getController().remindMeLater();
					stopOAD();
					break;
				default:
					break;
				}
            }
        });
        mTvFootSelect.setVisibility(View.VISIBLE);
        mTvFootNext.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        hiddenAll();

        mBtnList.setVisibility(View.GONE);

        mOADSubTitle.setText("");
        mLoadingPoint.drawLoading();
        mLoadingPoint.setVisibility(View.VISIBLE);
        if (getCurrentState().step == Step.DOWNLOADING) {
            mOADSubTitle.setText(getString(R.string.nav_oad_downloading));
            mOADSubTitle2.setText(getString(R.string.nav_oad_downloading_info));
        } else {
            mOADSubTitle.setText(getString(R.string.nav_oad_programming));
        }

        mOADSubTitle2.setVisibility(View.VISIBLE);
        mOADSubTitle.setVisibility(View.VISIBLE);

        mOADWarnningMsg.setText(getString(R.string.nav_oad_warning_msg_str));
        mOADWarnningMsg.setVisibility(View.VISIBLE);

        mOADWarnningMsg.setTextColor(Color.WHITE);

        mProgress.setVisibility(View.VISIBLE);
        mProgressStr.setVisibility(View.VISIBLE);
        if (downLoadRetry) {
            updateProgress(currentDownloadProgress);
            downLoadRetry = false;
        } else {
            updateProgress(0);
        }
    }

    private void showRestartConfirm() {
        hiddenAll();

        final int accept = 0;
        final int reject = 1;

        mOADSubTitle.setText(getString(R.string.nav_oad_restart_confirm));
        mOADSubTitle.setVisibility(View.VISIBLE);

        mBtnList.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_accept_reject);

        // mBtnList.setAdapter(new StateInitDiskBtnAdapter(this, btns));
        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case accept:
					getController().acceptRestart();
					NavOADActivity.this.finish();
					// verifyPackage(null);
					// installPackage();
					break;
				case reject:
					stopOAD();
					break;
				default:
					break;
				}
            }
        });
        mTvFootSelect.setVisibility(View.VISIBLE);
        mTvFootNext.setVisibility(View.VISIBLE);
    }

    private void showFlashFail() {
        // TODO Auto-generated method stub
        hiddenAll();

        final int yes = 0;
        final int no = 1;

        mOADSubTitle.setVisibility(View.VISIBLE);
        mOADSubTitle.setText(getString(R.string.nav_oad_flash_fail));

        mBtnList.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_yes_no);

        // mBtnList.setAdapter(new StateInitDiskBtnAdapter(this, btns));
        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				switch (position) {
				case yes:
					getController().acceptFlash();
					setCurrentState(new FlashingState());
					break;
				case no:
					stopOAD();
					break;
				default:
					break;
				}
            }
        });
    }

    private void showDownloadFail() {
        // TODO Auto-generated method stub
        hiddenAll();

        final int yes = 0;
        final int no = 1;

        mOADSubTitle.setVisibility(View.VISIBLE);
        mOADSubTitle.setText(getString(R.string.nav_oad_manual_download_fail));

        mBtnList.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_yes_no);

        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				switch (position) {
				case yes:
					downLoadRetry = true;
					// getController().acceptDownload();
					setCurrentState(new DetectOADState());
					break;
				case no:
					currentDownloadProgress = 0;
					setRemindMeLater(false);
					stopOAD();
					break;
				default:
					break;
				}
            }
        });
    }

    private void showScheduleInfo(String scheduleInfo) {
        hiddenAll();

        final int yes = 0;
        final int no = 1;

        mOADSubTitle
                .setText(String.format(
                        mContext.getString(R.string.nav_oad_schedule_oad_accept_schedule_confirm),
                        scheduleInfo));
        mOADSubTitle.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_yes_no);

        mBtnList.setVisibility(View.VISIBLE);

        List<String> mDataList = Arrays.asList(btns);
        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showScheduleInfo||position =" + position);
                if (-1 == id) {
                    return;
                }
				switch (position) {
				case yes:
					getController().acceptScheduleOAD();
					stopOAD();
					break;
				case no:
					// getController().remindMeLater();
					stopOAD();
					break;
				default:
					break;
				}
            }
        });
        mBtnList.setVisibility(View.VISIBLE);

        mTvFootSelect.setVisibility(View.VISIBLE);
        mTvFootNext.setVisibility(View.INVISIBLE);
    }

    private void showJumpChannelInfo() {
        hiddenAll();

        final int yes = 0;
        final int no = 1;

        updateAutoJumpChannelMsg(mJumpChannelRemindTime);
        getmHandler().sendEmptyMessage(AUTO_JUMP_CHANNEL);
        mOADSubTitle.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_yes_no);

        List<String> mDataList = Arrays.asList(btns);

        mBtnList.setVisibility(View.VISIBLE);

        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);
        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				switch (position) {
				case yes:
					getmHandler().removeMessages(AUTO_JUMP_CHANNEL);
					getController().acceptJumpChannel();
					stopOAD();
					break;
				case no:
					// getController().remindMeLater();
					stopOAD();
					break;
				default:
					break;
				}
            }
        });

        mBtnList.setVisibility(View.VISIBLE);

        mTvFootSelect.setVisibility(View.VISIBLE);
        mTvFootNext.setVisibility(View.INVISIBLE);
    }

    private void showBGMAutoDownloadToast() {
        hiddenAll();

        final int yes = 0;
        final int no = 1;

        mOADSubTitle.setText(mContext
                .getString(R.string.nav_oad_atuo_download_with_bgm));
        mOADSubTitle.setVisibility(View.VISIBLE);

        String[] btns = getResources().getStringArray(
                R.array.nav_oad_btns_yes_no);

        List<String> mDataList = Arrays.asList(btns);

        mBtnList.setVisibility(View.VISIBLE);

        mBtnList.initData(mDataList, mDataList.size());
        MyAdapter listAdapter = new MyAdapter(this, mDataList);

        mBtnList.setAdapter(listAdapter);

        mBtnList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				switch (position) {
				case yes:
					getController().setOADAutoDownload(true);
					stopOAD();
					break;
				case no:
					// getController().remindMeLater();
					stopOAD();
					break;
				default:
					break;
				}
            }
        });

        mBtnList.setVisibility(View.VISIBLE);

        mTvFootSelect.setVisibility(View.VISIBLE);
        mTvFootNext.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        MtkTvUtil.IRRemoteControl(5);
        getController().registerCallback(this);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart||intent =" + intent.getAction());
		if (TextUtils.equals("start_detect_oad", intent.getAction())) {
            setCurrentState(new DetectOADState());
        } else {
            if (intent.getExtras() != null) {
                int messageType = intent.getExtras().getInt("updateType");
                mScheuleInfo = "";
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart||messageType =" + messageType);
				switch (messageType) {
				case OAD_INFORM_SCHEDULE:
					mScheuleInfo = intent.getExtras().getString("scheduleInfo");
					break;
				case OAD_MTKTVAPI_INFORM_JUMP_SCHEDULE:
					updateAutoJumpChannelMsg(mJumpChannelRemindTime);
					break;
				case OAD_MTKTVAPI_INFORM_AUTO_DOWNLOAD_WITH_BGM:
					mOADSubTitle
							.setText(mContext
									.getString(R.string.nav_oad_atuo_download_with_bgm));
					break;
				default:
					break;
				}
				Message msg = getmHandler().obtainMessage();
				msg.what = DELAY_SHOW_CONFIRM_UI;

                TvCallbackData data = new TvCallbackData();
                data.param1 = messageType;
                data.param2 = 0;
                data.param3 = -1;

                data.paramStr1 = mScheuleInfo;
                data.paramBool1 = false;

                msg.obj = data;

                getmHandler().sendMessageDelayed(msg, 1000);
            }
        }
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (!mIsStop) {
            setRemindMeLater(false);
            stopOAD();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        getController().unRegisterCallback();
        String sourceName = InputSourceManager.getInstance().getCurrentInputSourceName(
                MtkTvInputSource.INPUT_OUTPUT_MAIN);
        int detail = InputSourceManager.getInstance().changeCurrentInputSourceByName(sourceName);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sourceName ==" + sourceName + "  detail ==" + detail);
    }

    public MyHandler getmHandler() {
        return mHandler;
    }

    public void setmHandler(MyHandler mHandler) {
        this.mHandler = mHandler;
    }

    private void updateProgress(int progress) {
        if (getCurrentState().step == Step.DOWNLOADING
                || getCurrentState().step == Step.FLASHING) {
            mProgress.setProgress(progress);
            mProgressStr.setText(String.format("%d%%", progress));
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (mBtnList.getVisibility() == View.VISIBLE && mBtnList.getCount() > 0) {
                int position = mBtnList.getSelectedItemPosition();
                position = Math.max(position, 0);
                mBtnList.performItemClick(mBtnList.getChildAt(position), position,
                        mBtnList.getChildAt(position).getId());
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDownKeyCode =" + keyCode);
        if (DEBUG) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_0:
                    setCurrentState(new DetectOADState());
                    break;
                case KeyEvent.KEYCODE_1:
                    showScanningFailResult();
                    break;
                case KeyEvent.KEYCODE_2:
                    setCurrentState(new DownloadConfirmOADState());
                    break;
                case KeyEvent.KEYCODE_3:
                    setCurrentState(new DownloadConfirmOADState());
                    break;
                case KeyEvent.KEYCODE_4:
                    setCurrentState(new DownloadingOADState());
                    break;
                case KeyEvent.KEYCODE_5:
                    setCurrentState(new FlashConfirmOADState());
                    break;
                case KeyEvent.KEYCODE_6:
                    setCurrentState(new FlashingState());
                    break;
                case KeyEvent.KEYCODE_7:
                    setCurrentState(new RestartConfirmOADState());
                    break;
                case KeyEvent.KEYCODE_8:
                    hiddenAll();
                    break;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    getCurrentState().nextPage();
                    return true;
                case 331:// case KeyEvent.KEYCODE_MTKIR_CHDN:
                    setCurrentState(new ScheduleInfoState());
                    return true;

                case 328:// case KeyEvent.KEYCODE_MTKIR_CHUP:
                    setCurrentState(new JumpChannelState());
                    return true;
                default:
                	break;
            }
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // getCurrentState().nextPage();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                /*
                 * if (getCurrentState().onLeftKeyBackPress) { getCurrentState().lastPage(); }
                 */
                return true;
            case KeyEvent.KEYCODE_BACK:
                TurnkeyUiMainActivity.setShowBannerInOnResume(false);
                stopOAD();
                return true;
            default:
            	break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public NavOADController getController() {
        return controller;
    }

    public void setController(NavOADController controller) {
        this.controller = controller;
    }

    public BaseOADState getCurrentState() {
        return currentState;
    }

    public void setDownloadFailState() {
        if (null != mProgress) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "OAD_INFORM_DOWNLOAD_FAIL,mProgress.getProgress() =" + mProgress.getProgress());
            currentDownloadProgress = mProgress.getProgress();
        }
        setCurrentState(new DownloadFailOADState());
    }

    public void setCurrentState(BaseOADState currentState) {
        this.currentState = currentState;
        if (mTvFootNext != null) {
            try {
                hiddenAll();
            } catch (Exception e) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "setCurrentState exception");
            }
        }
        currentState.updateUi();
    }

	public void stopOAD() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
		MtkTvUtil.IRRemoteControl(3);
		if (getmHandler() != null) {
			getmHandler().removeMessages(AUTO_DOWNLOAD_COUNTDOWN);
			getmHandler().removeMessages(DELAY_SHOW_CONFIRM_UI);
			getmHandler().removeMessages(DOWN_LOAD_TIME_OUT_MSG);
			getmHandler().removeMessages(AUTO_EXIT_OAD);
			getmHandler().removeCallbacksAndMessages(null);
		}

		if (getCurrentState() != null) {
			if(getCurrentState().step == Step.DOWNLOADING) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopOAD||CurrentState DOWNLOADING");
				getController().cancelDownload();
			} else if(getCurrentState().step == Step.DETECT){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopOAD||CurrentState DETECT");
				getController().cancelManualDetect();
			}
		}
		if (getController() != null) {
			// getController().unRegisterCallback();
			// getController().cancelManualDetect();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopOAD||IsRemindMeLater =" + isRemindMeLater());
			if (isRemindMeLater()) {
				getController().remindMeLater();
			}
		}

		mCountDownInt = 15;
		mIsStop = true;
// 		exit oad and resume turnkey normally
		if (DvrManager.getInstance() != null) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopOAD||setStopDvrNotResumeLauncher");
			DvrManager.getInstance().setStopDvrNotResumeLauncher(false);
		}
		finish();
	}

    /**
     * @see com.mediatek.twoworlds.tv#notifyOADMessage
     */
    public void onNotifyOADMessage(int messageType, String scheduleInfo,
            int progress, boolean autoDld, int argv5) {

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onNotifyOADMessageType =" + messageType + "||scheduleInfo =" + scheduleInfo);
        getmHandler().removeMessages(DOWN_LOAD_TIME_OUT_MSG);

        switch (messageType) {
            case OAD_INFORM_FILE_FOUND:
                if (oadActivity != null) {
                    if (getCurrentState().step != Step.DOWNLOAD_CONFIRM) {
                        setCurrentState(new DownloadConfirmOADState());
                    }
                }
                break;
            case OAD_INFORM_FILE_NOT_FOUND:
                showScanningFailResult();
                break;
            case OAD_INFORM_NEWEST_VERSION:
//                String str = "Newest PKG!!!";
            	String str = this.getString(R.string.nav_oad_latest_version_already);
                showScanningFailResult(str);
                break;
            case OAD_INFORM_SCHEDULE:
                setCurrentState(new ScheduleInfoState());
                break;
            case OAD_INFORM_LINKAGE:
                stopOAD();
                break;
            case OAD_INFORM_DOWNLOAD_PROGRESS:
            case OAD_INFORM_INSTALL_PROGRESS:
                if (getCurrentState().step == Step.DOWNLOADING
                        || getCurrentState().step == Step.FLASHING) {
                    if (mProgressStr.getVisibility() != View.VISIBLE) {
                        mProgressStr.setVisibility(View.VISIBLE);
                    }
                }
                getmHandler().sendEmptyMessageDelayed(DOWN_LOAD_TIME_OUT_MSG, 1 * 1200 * 1000);
                // Timeout time should 20 minutes.
                updateProgress(progress);
                break;
            case OAD_INFORM_DOWNLOAD_FAIL:
                if (null != mProgress) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"OAD_INFORM_DOWNLOAD_FAIL,mProgress.getProgress() ="
                                    + mProgress.getProgress());
                    currentDownloadProgress = mProgress.getProgress();
                }
                setCurrentState(new DownloadFailOADState());
                break;
            case OAD_INFORM_INSTALL:
                break;
            case OAD_INFORM_INST_FAIL:
                setCurrentState(new FlashFailState());
                break;

            case OAD_INFORM_SUCCESS:
                synchronized(this){
                    mPackagePathAndName = scheduleInfo;
                }
                setCurrentState(new RestartConfirmOADState());
                break;
            case OAD_MTKTVAPI_INFORM_JUMP_SCHEDULE:
                setCurrentState(new JumpChannelState());
                break;
            case OAD_MTKTVAPI_INFORM_AUTO_DOWNLOAD_WITH_BGM:
                setCurrentState(new BGMAutoDownloadState());
                break;
            default:
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {
        private final List<String> mList;
        private final LayoutInflater mInflater;

        private MyAdapter(Context context, List<String> data) {
            mInflater = LayoutInflater.from(context);
            mList = data;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.nav_oad_setup_data,
                        null);
            }
            TextView tv = (TextView) convertView
                    .findViewById(R.id.common_tv_setup_dialog_data_index);
            tv.setText(mList.get(position));
            return convertView;
        }
    }

    /**
     * Verify the cryptographic signature of a system update package before installing it.
     *
     * @return true if verification success.
     */
    public synchronized boolean verifyPackage(ProgressListener listener) {
        if (mPackagePathAndName == null) {
            return false;
        }

        try {
            RecoverySystem.verifyPackage(new File(mPackagePathAndName),
                    listener, new File("/system/etc/security/otacerts.zip"));

        } catch (IOException e) {
            return false;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "verify Package error " + e);
            return false;
        }

        return true;
    }

    /**
     * Reboots the device in order to install the given update package.
     *
     * @return true if operation success.
     */
    public synchronized boolean installPackage() {
        if (mPackagePathAndName == null) {
            return false;
        }

        try {
            RecoverySystem.installPackage(mContext, new File(
                    mPackagePathAndName));

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * this method is used to get the object of NavOADActivity
     *
     * @return
     */
    public static NavOADActivity getInstance() {
        return oadActivity;
    }

    private void clearTextViewStr() {
        mOADSubTitle.setText("");
        mOADSubTitle2.setText("");
        mOADWarnningMsg.setText("");
        mProgressStr.setText("");
        mScheuleInfo = "";
    }

    /**
	 * @return the isRemindMeLater
	 */
	public boolean isRemindMeLater() {
		return isRemindMeLater;
	}

	/**
	 * @param isRemindMeLater the isRemindMeLater to set
	 */
	public void setRemindMeLater(boolean isRemindMeLater) {
		this.isRemindMeLater = isRemindMeLater;
	}

//	private final class MyBroadcastReceiver extends BroadcastReceiver {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d("yajun", "onReceive = " + intent.getAction());
//			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) ||
//                    Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
//				setRemindMeLater(false);
//			}else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//				setRemindMeLater(true) ;
//			}
//		}
//    };
}