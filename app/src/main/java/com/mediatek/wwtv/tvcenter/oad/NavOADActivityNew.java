package com.mediatek.wwtv.tvcenter.oad;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.commonview.Loading;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.ICStatusListener;

import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

public class NavOADActivityNew extends BaseActivity implements ICStatusListener{
	private static final String TAG = "NavOADActivityNewTV";
	private Intent mIntent;

	private static NavOADActivityNew navOADActivityNew = null;
	private Context mContext;
	private List<String> mList;
	private OadActionListAdapter mOadActionListAdapter;
	private ListView mListView;

	private int mCountDownInt = 15;
	private int mJumpChannelRemindTime = 10;

	private boolean isRemindMeLater = true;
	private NavOADControllerNew controller;
	private BaseOADState currentState;

	final static int AUTO_DOWNLOAD_COUNTDOWN = 0;
	final static int NOTIFY_CALL_BACK        = AUTO_DOWNLOAD_COUNTDOWN + 1;
	final static int DELAY_SHOW_CONFIRM_UI   = AUTO_DOWNLOAD_COUNTDOWN + 2;
	final static int DOWN_LOAD_TIME_OUT_MSG  = AUTO_DOWNLOAD_COUNTDOWN + 3;
	final static int AUTO_JUMP_CHANNEL       = AUTO_DOWNLOAD_COUNTDOWN + 4;
	final static int AUTO_EXIT_OAD           = AUTO_DOWNLOAD_COUNTDOWN + 5;
	final static int AUTO_EXIT_TIME 		 = 5 * 60 * 1000;

	private final int OAD_FROM_USER_CLICK 				= -1;
	private final static int OAD_INFORM_FILE_FOUND 		= 0;
	private final int OAD_INFORM_FILE_NOT_FOUND 		= OAD_INFORM_FILE_FOUND + 1;
	private final int OAD_INFORM_NEWEST_VERSION 		= OAD_INFORM_FILE_FOUND + 2;
	private final int OAD_INFORM_SCHEDULE 				= OAD_INFORM_FILE_FOUND + 3;
	private final int OAD_INFORM_LINKAGE 				= OAD_INFORM_FILE_FOUND + 4;
	private final int OAD_INFORM_DOWNLOAD_PROGRESS 		= OAD_INFORM_FILE_FOUND + 5;
	private final int OAD_INFORM_DOWNLOAD_FAIL 			= OAD_INFORM_FILE_FOUND + 6;
	private final int OAD_INFORM_INSTALL 				= OAD_INFORM_FILE_FOUND + 7;
	private final int OAD_INFORM_INSTALL_PROGRESS 		= OAD_INFORM_FILE_FOUND + 8;
	private final int OAD_INFORM_INST_FAIL 				= OAD_INFORM_FILE_FOUND + 9;
	private final int OAD_INFORM_SUCCESS 				= OAD_INFORM_FILE_FOUND + 10;
	private final int OAD_MTKTVAPI_INFORM_JUMP_SCHEDULE = OAD_INFORM_FILE_FOUND + 11;
	private final int OAD_MTKTVAPI_INFORM_AUTO_DOWNLOAD_WITH_BGM = OAD_INFORM_FILE_FOUND + 13;
  public final static int OAD_STATE_INACTIVE = 0;
  public final static int OAD_STATE_ACTIVE   = 1;

	private TextView mOadTitle;
	private TextView mOadDescription;
	private TextView mProgress;
	private Loading mLoadingPoint;

	private MyHandler mHandler;
	private int currentDownloadProgress = 0;
	private boolean downLoadRetry = false;
  //when user select reject to restart to install new version, set it to be true;
  private boolean rejectOadUpdated = false;

	private String mScheuleInfo = "";
	private String mPackagePathAndName = "";

	enum Step {
		DEFAULT, DETECT, DOWNLOAD_CONFIRM, DOWNLOADING, FLASH_CONFIRM, FLASHING, RESTART_CONFIRM
	};

	static class MyHandler extends Handler {
		WeakReference<NavOADActivityNew> mActivity;

		MyHandler(NavOADActivityNew activityNew) {
			mActivity = new WeakReference<NavOADActivityNew>(activityNew);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessageMsg.what = " + msg.what);
			final NavOADActivityNew activityNew = mActivity.get();
			switch (msg.what) {
			case AUTO_DOWNLOAD_COUNTDOWN:
				if (activityNew != null
						&& activityNew.getCurrentState() != null) {
					if (activityNew.getCurrentState().step == Step.DETECT) {
						activityNew.mCountDownInt = 15;
						return;
					}

					activityNew.mCountDownInt--;
					Log.d(TAG, "handleMessageMsg||mCountDownInt = "
							+ activityNew.mCountDownInt);
					if (activityNew.mCountDownInt > 0) {
						sendEmptyMessageDelayed(AUTO_DOWNLOAD_COUNTDOWN, 1000);
						activityNew
								.updateDownloadCountDownMsg(activityNew.mCountDownInt);
					} else {
						activityNew.getCurrentState().nextStep();
						activityNew.mCountDownInt = 15;
					}
				}
				break;
			case NOTIFY_CALL_BACK:
				Bundle bundle = msg.getData();
				if (activityNew != null) {
					activityNew.onNotifyOADMessage(bundle.getInt("arg1"),
							bundle.getString("arg2"), bundle.getInt("arg3"),
							bundle.getBoolean("arg4"), bundle.getInt("arg5"));
				}
				break;
			case DELAY_SHOW_CONFIRM_UI:
				TvCallbackData data = (TvCallbackData) msg.obj;
				if (activityNew != null) {
					activityNew.onNotifyOADMessage(data.param1, data.paramStr1,
							data.param2, data.paramBool1, data.param3);
				}
				break;
			case DOWN_LOAD_TIME_OUT_MSG:
				if (activityNew != null) {
					activityNew.setDownloadFailState();
				}
				break;
			case AUTO_JUMP_CHANNEL:
				if (activityNew != null
						&& activityNew.getCurrentState() != null) {
					activityNew.mJumpChannelRemindTime--;
					if (activityNew.mJumpChannelRemindTime > 0) {
						sendEmptyMessageDelayed(AUTO_JUMP_CHANNEL, 1000);
						activityNew
								.updateAutoJumpChannelMsg(activityNew.mJumpChannelRemindTime);
					} else {
						((JumpChannelState) activityNew.getCurrentState())
								.nextStep();
						activityNew.mJumpChannelRemindTime = 10;
					}
				}
				break;
			case AUTO_EXIT_OAD:
				if (activityNew != null) {
					activityNew.stopOAD();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();
		navOADActivityNew = this;
		setCurrentState(new BaseOADState());
		setContentView(R.layout.oad_fragment_layout);
		initViews();

		setHandler(new MyHandler(this));
		setController(new NavOADControllerNew(this));
		mIntent = getIntent();
		ComponentStatusListener lister = ComponentStatusListener.getInstance();
		lister.addListener(ComponentStatusListener.NAV_POWER_OFF, this);
		lister.updateStatus(ComponentStatusListener.NAV_OAD_STATE,OAD_STATE_ACTIVE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		 MtkTvUtil.IRRemoteControl(5);
		if (mIntent != null) {
				int actionType = -2;
				actionType = mIntent.getExtras().getInt("updateType");
				Log.d(TAG, "onStart||actionType =" + actionType);
				switch (actionType) {
				case OAD_FROM_USER_CLICK:
					rejectOadUpdated = SaveValue.getInstance(this).readBooleanValue("rejectOadUpdated");
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart||rejectOadUpdated:"+rejectOadUpdated);
					if(rejectOadUpdated){
					    setCurrentState(new RestartConfirmOADState());
					}else {
						setCurrentState(new CheckingOADState());
					}
					break;
				case OAD_INFORM_SCHEDULE:
					mScheuleInfo = mIntent.getExtras()
							.getString("scheduleInfo");
					break;
				case OAD_MTKTVAPI_INFORM_JUMP_SCHEDULE:
					updateAutoJumpChannelMsg(mJumpChannelRemindTime);
					break;
				case OAD_MTKTVAPI_INFORM_AUTO_DOWNLOAD_WITH_BGM:
					mOadTitle
							.setText(getString(R.string.nav_oad_atuo_download_with_bgm));
					break;
				default:
					break;
				}
				Message msg = getHandler().obtainMessage();
				msg.what = DELAY_SHOW_CONFIRM_UI;

				TvCallbackData data = new TvCallbackData();
				data.param1 = actionType;
				data.param2 = 0;
				data.param3 = -1;
				data.paramStr1 = mScheuleInfo;
				data.paramBool1 = false;
				msg.obj = data;
				getHandler().sendMessageDelayed(msg, 1000);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "OAD||onResume");
	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		Log.d(TAG, "OADComponentStatus||statusID =" + statusID);
		switch (statusID) {
		case ComponentStatusListener.NAV_POWER_OFF:
			stopOAD();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown||code =" + keyCode);
      switch (keyCode) {
          case KeyEvent.KEYCODE_BACK:
              TurnkeyUiMainActivity.setShowBannerInOnResume(false);
              stopOAD();
              return true;
          default:
              break;
          }
		return super.onKeyDown(keyCode, event);
	}

	public static NavOADActivityNew getInstance() {
		return navOADActivityNew;
	}

	private void initViews() {
		TextView mOadVersion;
		mOadTitle = (TextView) findViewById(R.id.oad_title);
		mOadVersion = (TextView) findViewById(R.id.oad_version);
		mOadVersion.setText(String.format("%s: 0x%s", getString(R.string.menu_versioninfo_version), getOadVersion()));
		rejectOadUpdated = SaveValue.getInstance(this).readBooleanValue("rejectOadUpdated");
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initViews rejectOadUpdated:"+rejectOadUpdated);
		mOadDescription = (TextView) findViewById(R.id.oad_description);
		mLoadingPoint = (Loading) findViewById(R.id.nav_oad_processing);
		mListView = (ListView) findViewById(R.id.oad_action_list);
		mProgress = (TextView) findViewById(R.id.oad_download_propgress);

	}

	public NavOADControllerNew getController() {
		return controller;
	}

	public void setController(NavOADControllerNew controller) {
		this.controller = controller;
	}

	public BaseOADState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(BaseOADState currentState) {
		Log.d(TAG, "setCurrentState||currentState =" + currentState.step);
		this.currentState = currentState;
		currentState.updateUi();
	}

	public MyHandler getHandler() {
		return mHandler;
	}

	public void setHandler(MyHandler mHandler) {
		this.mHandler = mHandler;
	}

	private void showScanning() {
		Log.d(TAG, "showScanning");
		resetAllToDefault();
        final int cancel = 0;

		mOadTitle.setText(getString(R.string.nav_oad_scanning));
		mOadDescription.setText(getString(R.string.oad_scanning_tip));
		mLoadingPoint.setVisibility(View.VISIBLE);
		mLoadingPoint.drawLoading();

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);

		mListView.setVisibility(View.VISIBLE);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showDownloadFail() {
		resetAllToDefault();

        final int ok     = 0;
        final int cancel = 1;

		mOadTitle.setText(getString(R.string.nav_oad_dialog_title));
		mOadDescription
				.setText(getString(R.string.nav_oad_manual_download_fail));
		mListView.setVisibility(View.VISIBLE);

        String[] actionArray = getResources().getStringArray(
                R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));

		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "showDownloadFail||position =" + position);
				switch (position) {
				case ok:
					downLoadRetry = true;
					setCurrentState(new CheckingOADState());
					break;
				case cancel:
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

	private void showFlashFail() {
		resetAllToDefault();

        final int ok     = 0;
        final int cancel = 1;

		mOadTitle.setVisibility(View.VISIBLE);
		mOadTitle.setText(getString(R.string.oad_flash_failed_title));

		mOadDescription.setText(getString(R.string.nav_oad_flash_fail));
		mListView.setVisibility(View.VISIBLE);

        String[] actionArray = getResources().getStringArray(
                R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));

		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "showFlashFail||position =" + position);
				switch (position) {
				case ok:
					getController().acceptFlash();
					setCurrentState(new FlashingState());
					break;
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showScheduleInfo(String scheduleInfo) {
		Log.d(TAG, "showScheduleInfo");
        final int ok     = 0;
        final int cancel = 1;

		String str = getString(R.string.nav_oad_schedule_oad_accept_schedule_confirm);
		mOadDescription.setText(String.format(str, scheduleInfo));

		mOadTitle.setText(getString(R.string.oad_schedule_title));

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);

		mListView.setVisibility(View.VISIBLE);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "showScheduleInfo||position =" + arg2);
				switch (arg2) {
				case ok:
					getController().acceptScheduleOAD();
					stopOAD();
					break;
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showJumpChannelInfo() {
		Log.d(TAG, "showJumpChannelInfo");
		resetAllToDefault();

        final int ok     = 0;
        final int cancel = 1;

		updateAutoJumpChannelMsg(mJumpChannelRemindTime);

		getHandler().sendEmptyMessage(AUTO_JUMP_CHANNEL);

		mOadTitle.setText(getString(R.string.oad_channel_jump_title));
		mOadDescription.setText(getString(R.string.oad_channel_jump_title));

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);

		mListView.setVisibility(View.VISIBLE);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "showJumpChannelInfo||position =" + arg2);
				switch (arg2) {
				case ok:
					getHandler().removeMessages(AUTO_JUMP_CHANNEL);
					getController().acceptJumpChannel();
					stopOAD();
					break;
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showRestartConfirm() {
		Log.d(TAG, "showRestartConfirm");
		resetAllToDefault();

        final int accept = 0;
        final int reject = 1;

		mOadTitle.setText(getString(R.string.oad_restart_to_proceed_title));
		mOadDescription.setText(getString(R.string.nav_oad_restart_confirm));

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_accept_reject);
		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);

		mListView.setVisibility(View.VISIBLE);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "showRestartConfirm||position =" + arg2);
				switch (arg2) {
				case accept:
					getController().acceptRestart();
					stopOAD();
					break;
				case reject:
                    SaveValue.getInstance(NavOADActivityNew.this).saveBooleanValue("rejectOadUpdated", true);
                    rejectOadUpdated = true;
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showBGMAutoDownloadToast() {
		Log.d(TAG, "showBGMAutoDownloadToast");
		resetAllToDefault();

        final int ok     = 0;
        final int cancel = 1;

		mOadTitle.setText(getString(R.string.oad_download_bgm_title));

		mOadDescription
				.setText(getString(R.string.nav_oad_atuo_download_with_bgm));

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);

		mListView.setVisibility(View.VISIBLE);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "showBGMAutoDownloadToast||position =" + arg2);
				switch (arg2) {
				case ok:
					getController().setOADAutoDownload(true);
					stopOAD();
					break;
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * show when scan fail
	 */
	private void showScanFail() {
		resetAllToDefault();

        final int ok     = 0;
        final int cancel = 1;

		mOadTitle
				.setText(this.getString(R.string.nav_oad_scanning_result_fail));
		mOadDescription
				.setText(getString(R.string.oad_scan_failed_description));
		mListView.setVisibility(View.VISIBLE);

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));

		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "showScanFail||position =" + arg2);
				switch (arg2) {
				case ok:
					setCurrentState(new CheckingOADState());
					showScanning();
					break;
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * show when scan finish with up_to_date already
	 */
	private void showUpToDate() {
		resetAllToDefault();

		final int ok = 0;

		String str = this.getString(R.string.nav_oad_latest_version_already);
		mOadTitle.setText(str);

		mListView.setVisibility(View.VISIBLE);
		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_ok);
		mList = new ArrayList<String>(Arrays.asList(actionArray));

		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG, "showUpToDate||position =" + arg2);
				switch (arg2) {
				case ok:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showDownloadConfirm(boolean auto) {
		resetAllToDefault();

		final int accept          = 0;
		final int remindMeLater = 1;

		Log.d(TAG, "showDownloadConfirm||auto =" + auto);
		mOadTitle.setText(getString(R.string.nav_oad_dialog_title));
		if (auto) {
			updateDownloadCountDownMsg(mCountDownInt);
			getHandler().sendEmptyMessage(AUTO_DOWNLOAD_COUNTDOWN);
		} else {
			mOadDescription.setText(this.getString(R.string.nav_oad_manual_download_confirm));
			// exit in 5 min
			getHandler().sendEmptyMessageDelayed(AUTO_EXIT_OAD, AUTO_EXIT_TIME);
		}

		mListView.setVisibility(View.VISIBLE);
		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_accept_remind_me_later);
		mList = new ArrayList<String>(Arrays.asList(actionArray));

		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "showDownloadConfirm||position =" + position);
				switch (position) {
				case accept:
					if (getCurrentState() != null
							&& getCurrentState().step == Step.DOWNLOAD_CONFIRM) {
						getCurrentState().nextStep();
					} else {
						Log.d(TAG, "CurrentState||DOWNLOAD_CONFIRM");
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
	}

	private void showFlashConfirm() {
		resetAllToDefault();

		final int accept = 0;
		final int reject = 1;

		Log.d(TAG, "showFlashConfirm");
		mOadTitle.setVisibility(View.VISIBLE);
		mOadTitle.setText(this.getString(R.string.nav_oad_flash_confirm));
		mOadDescription.setText(this.getString(R.string.nav_oad_flash_confirm));

		mListView.setVisibility(View.VISIBLE);
		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_accept_reject);
		mList = new ArrayList<String>(Arrays.asList(actionArray));

		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "showFlashConfirm||position =" + position);
				switch (position) {
				case accept:
					if (getCurrentState() != null
							&& getCurrentState().step == Step.FLASH_CONFIRM) {
						getCurrentState().nextStep();
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
	}

	private void showLinkageFound() {
		resetAllToDefault();

		final int ok     = 0;
		final int cancel = 1;

		mOadTitle.setVisibility(View.VISIBLE);
		mOadTitle.setText(this.getString(R.string.nav_oad_dialog_title));

		mOadDescription.setVisibility(View.VISIBLE);
		mOadDescription.setText(getString(R.string.oad_linkage_found));

		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_ok_cancel);
		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);

		mListView.setVisibility(View.VISIBLE);
		mListView.setAdapter(mOadActionListAdapter);
		mListView.requestFocus();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "showLinkageFound||position =" + position);
				switch (position) {
				case ok:
					getController().acceptJumpChannel();
					MtkTvUtil.IRRemoteControl(3);
					NavOADActivityNew.this.finish();
					break;
				case cancel:
					stopOAD();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * @see com.mediatek.twoworlds.tv#notifyOADMessage
	 */
	public void onNotifyOADMessage(int messageType, String scheduleInfo,
			int progress, boolean autoDld, int argv5) {

		Log.d(TAG, "onNotifyOADMessageType =" + messageType
				+ "||scheduleInfo =" + scheduleInfo);
		getHandler().removeMessages(DOWN_LOAD_TIME_OUT_MSG);

		switch (messageType) {
		case OAD_INFORM_FILE_FOUND:
			if (getCurrentState() != null
					&& getCurrentState().step != Step.DOWNLOAD_CONFIRM) {
				setCurrentState(new DownloadConfirmOADState());
			}
			break;
		case OAD_INFORM_FILE_NOT_FOUND:
			showScanFail();
			break;
		case OAD_INFORM_NEWEST_VERSION:
			showUpToDate();
			break;
		case OAD_INFORM_SCHEDULE:
			setCurrentState(new ScheduleInfoState());
			break;
		case OAD_INFORM_LINKAGE:
			setCurrentState(new LinkageFound());
			break;
		case OAD_INFORM_DOWNLOAD_PROGRESS:
		case OAD_INFORM_INSTALL_PROGRESS:
			// Timeout time set as 20 minutes.
			getHandler().sendEmptyMessageDelayed(DOWN_LOAD_TIME_OUT_MSG,
					20 * 60 * 1000);
			updateProgress(progress);
			break;
		case OAD_INFORM_DOWNLOAD_FAIL:
			setCurrentState(new DownloadFailOADState());
			break;
		case OAD_INFORM_INSTALL:
			break;
		case OAD_INFORM_INST_FAIL:
			setCurrentState(new FlashFailState());
			break;
		case OAD_INFORM_SUCCESS:
			synchronized (this) {
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

	public void stopOAD() {
		Log.d(TAG, "stopOAD");
		MtkTvUtil.IRRemoteControl(3);
		if (getHandler() != null) {
			getHandler().removeMessages(AUTO_DOWNLOAD_COUNTDOWN);
			getHandler().removeMessages(DELAY_SHOW_CONFIRM_UI);
			getHandler().removeMessages(DOWN_LOAD_TIME_OUT_MSG);
			getHandler().removeMessages(AUTO_EXIT_OAD);
			getHandler().removeCallbacksAndMessages(null);
		}

		if (getCurrentState() != null) {
			if (getCurrentState().step == Step.DOWNLOADING) {
				Log.d(TAG, "stopOAD||CurrentState DOWNLOADING");
				getController().cancelDownload();
			} else if (getCurrentState().step == Step.DETECT) {
				Log.d(TAG, "stopOAD||CurrentState DETECT");
				getController().cancelManualDetect();
			}
		}
		if (getController() != null) {
			// getController().unRegisterCallback();
			// getController().cancelManualDetect();
			Log.d(TAG, "stopOAD||IsRemindMeLater =" + getRemindMeLater());
			if (getRemindMeLater()) {
				getController().remindMeLater();
			}
		}

		mCountDownInt = 15;
		// mIsStop = true;
		// exit oad and resume turnkey normally
		if (DvrManager.getInstance() != null) {
			Log.d(TAG, "stopOAD||setStopDvrNotResumeLauncher");
			DvrManager.getInstance().setStopDvrNotResumeLauncher(false);
		}
		 ComponentStatusListener.getInstance().
           updateStatus(ComponentStatusListener.NAV_OAD_STATE,OAD_STATE_INACTIVE);
		this.finish();

	}

	private void updateDownloadProgress() {
		resetAllToDefault();

		final int cancel = 0;

		mListView.setVisibility(View.VISIBLE);
		String[] actionArray = getResources().getStringArray(
				R.array.nav_oad_btns_cancel);

		mList = new ArrayList<String>(Arrays.asList(actionArray));
		mOadActionListAdapter = new OadActionListAdapter(this, mList);
		mListView.setAdapter(mOadActionListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "updateDownloadProgress||position =" + position);
				switch (position) {
				case cancel:
					getController().cancelDownload();
					stopOAD();
					break;
				default:
					break;
				}
			}
		});

		mProgress.setVisibility(View.VISIBLE);

		mOadTitle.setText(getString(R.string.oad_downloading_title));
		mOadDescription.setText(getString(R.string.nav_oad_warning_msg_str));

		mLoadingPoint.drawLoading();
		mLoadingPoint.setVisibility(View.VISIBLE);

		if (downLoadRetry) {
			updateProgress(currentDownloadProgress);
			downLoadRetry = false;
		} else {
			updateProgress(0);
		}

	}

	private void updateProgress(int progress) {
		Log.d(TAG, "updateProgress||progress =" + progress);
		mProgress.setVisibility(View.VISIBLE);
		currentDownloadProgress = progress;
		if (getCurrentState().step == Step.DOWNLOADING
				|| getCurrentState().step == Step.FLASHING) {
			Log.d(TAG, "updateProgress||processing");
			String progressFormat = getString(R.string.nav_oad_progress);
			mProgress.setText(String.format(progressFormat, progress));
		}
	}

	private void resetAllToDefault() {
		if (getHandler() != null) {
			getHandler().removeMessages(AUTO_DOWNLOAD_COUNTDOWN);
			getHandler().removeMessages(AUTO_EXIT_OAD);
		}
		mOadTitle.setText("");
		mOadDescription.setText("");

		try {
			mLoadingPoint.setVisibility(View.INVISIBLE);
			mLoadingPoint.stopDraw();
		} catch (Exception e) {
		    Log.d(TAG, "Exception");
		}

		mListView.setVisibility(View.INVISIBLE);
		mProgress.setVisibility(View.GONE);
	}

	private void updateDownloadCountDownMsg(int seconds) {
		if (this != null) {
			String str = this.getString(R.string.nav_oad_auto_download_confirm);
			mOadDescription.setText(String.format(str, seconds));
		}
	}

	/**
	 * @param isRemindMeLater
	 *            the isRemindMeLater to set
	 */
	public void setRemindMeLater(boolean isRemindMeLater) {
		this.isRemindMeLater = isRemindMeLater;
	}

	/**
	 * @return the isRemindMeLater
	 */
	public boolean getRemindMeLater() {
		return isRemindMeLater;
	}

	/**
	 * Verify the cryptographic signature of a system update package before
	 * installing it.
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

	private void updateAutoJumpChannelMsg(int lastTime) {
		String str = getString(R.string.nav_oad_schedule_oad_change_channel_confirm);
		mOadDescription.setText(String.format(str, lastTime));
	}

	public void setDownloadFailState() {
		setCurrentState(new DownloadFailOADState());
	}

	private String getOadVersion() {
		String currentVersion = "90";
		int oadVersion = MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigTypeBase.CFG_OAD_OAD_SW_NUM);
		Log.d(TAG, "getOadVersion||oadVersion =" + oadVersion);
		if (oadVersion != 0) {
			try {
				currentVersion = Integer.toHexString(oadVersion);
			} catch (Exception e) {
				Log.e(TAG, "toHexString exception");
			}
		}
		return currentVersion;
	}

	interface IStepSquence {
		void updateUi();

		void nextStep();

		void lastStep();
	}

	class BaseOADState implements IStepSquence {

		boolean onBackPress = true;
		Step step = Step.DEFAULT;

		public BaseOADState() {
			super();
			step = Step.DEFAULT;
		}

		@Override
		public void updateUi() {
			// TODO Auto-generated method stub
		}

		@Override
		public void nextStep() {
			if (getHandler() != null) {
				getHandler().removeMessages(AUTO_DOWNLOAD_COUNTDOWN);
				getHandler().removeMessages(AUTO_EXIT_OAD);
			}
			mCountDownInt = 15;
		}

		@Override
		public void lastStep() {
			// TODO Auto-generated method stub

		}

		public void clearTask() {
			// TODO Auto-generated method stub

		}
	}

	class CheckingOADState extends BaseOADState {

		@Override
		public void updateUi() {
			step = Step.DETECT;
			showScanning();
			getController().remindMeLater();
			getController().manualDetect();
		}

		@Override
		public void clearTask() {
			super.clearTask();
			getController().cancelManualDetect();
			stopOAD();
		}

	}

	class DownloadConfirmOADState extends BaseOADState {

		@Override
		public void nextStep() {
			super.nextStep();
			getController().acceptDownload();
			setCurrentState(new DownloadingOADState());
		}

		@Override
		public void updateUi() {
			step = Step.DOWNLOAD_CONFIRM;
			boolean enable = NavOADControllerNew.getOADAutoDownload(mContext);
			showDownloadConfirm(enable);
		}

		@Override
		public void clearTask() {
			super.clearTask();
			stopOAD();
		}
	}

	class DownloadingOADState extends BaseOADState {

		@Override
		public void updateUi() {
			getHandler().sendEmptyMessageDelayed(DOWN_LOAD_TIME_OUT_MSG,
					60 * 60 * 1000);
			step = Step.DOWNLOADING;
			updateDownloadProgress();
		}


		@Override
		public void clearTask() {
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
			showDownloadFail();
		}
	}

	class FlashConfirmOADState extends BaseOADState {

		@Override
		public void nextStep() {
			super.nextStep();
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
			updateDownloadProgress();
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
			step = Step.RESTART_CONFIRM;
		}
	}

	class ScheduleInfoState extends BaseOADState {

		@Override
		public void updateUi() {
			mScheuleInfo = getController().getScheduleInfo();
			// acceptScheduleOAD
			showScheduleInfo(mScheuleInfo);
		}
	}

	class JumpChannelState extends BaseOADState {

		@Override
		public void updateUi() {
			mJumpChannelRemindTime = 10;
			showJumpChannelInfo();
		}

		@Override
		public void nextStep() {
			// super.nextPage();
			if (null != getHandler()) {
				getHandler().removeMessages(AUTO_JUMP_CHANNEL);
			}
			getController().acceptJumpChannel();
			stopOAD();
		}

	}

	class BGMAutoDownloadState extends BaseOADState {
		@Override
		public void updateUi() {
			showBGMAutoDownloadToast();
		}
	}

	class LinkageFound extends BaseOADState {
		@Override
		public void updateUi() {
			showLinkageFound();
		}

		@Override
		public void nextStep() {
			getController().acceptJumpChannel();
			stopOAD();
		}
	}

}
