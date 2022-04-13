package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.epg.sa.db.DBMgrProgramList;
import com.mediatek.wwtv.tvcenter.epg.sa.db.EPGBookListViewDataItem;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

public class AlarmMgr {
	private static String TAG = "AlarmMgr";
	private static AlarmMgr instance;
	private boolean mIsStarted;
	public static String ALARM_EPG_ACTION= "com.mediatek.wwtv.tvcenter.saepgreceiver";
	private EPGBookListViewDataItem mGotoChannelProgram;
	private Context mContext;
	private PendingIntent pendingIntent;
	private long currentMills;
	private long readMills;
	public static long timeInterval;
	private Timer mTimer;
	private TurnkeyCommDialog mBookProgramConfirmDialog;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "111currentMills>DialogActivity>" + currentMills);
			if (mGotoChannelProgram != null && mGotoChannelProgram.mChannelId != 0) {
				if (TIFChannelManager.getInstance(mContext).getTIFChannelInfoById(mGotoChannelProgram.mChannelId) != null) {
					showBookConfirm(mContext);
				}
			} else {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "111currentMills>handleMessage>" + mGotoChannelProgram);
				if (mGotoChannelProgram != null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "111currentMills>handleMessage>" + mGotoChannelProgram.mChannelId);
				}
			}
//			Intent t = new Intent(mContext, DialogActivity.class);
//			t.putExtra("currentMills", currentMills);
//			t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "222currentMills>DialogActivity>" + currentMills);
//			mContext.startActivity(t);
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "333after currentMills>DialogActivity>" + currentMills);
		}

	};

	private AlarmMgr(Context context) {
		mContext = context;
		mTimer = new Timer();
	}

	public static synchronized AlarmMgr getInstance(Context context) {
		if (instance == null) {
			instance = new AlarmMgr(context);
		}
		return instance;
	}

	public void startTimer() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "before startTimer>>>" + mTimer);
		if (mTimer != null && !mIsStarted) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTimer>>>" + mTimer);
			mIsStarted = true;
			mTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					currentMills = EPGUtil.getCurrentTime();
					MtkTvTimeFormatBase mtkTvTimeFormatBase = MtkTvTime.getInstance().getBroadcastTime();
					mtkTvTimeFormatBase.set(currentMills);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTimer>>" + mtkTvTimeFormatBase.hour + "   " + mtkTvTimeFormatBase.minute + "   " + mtkTvTimeFormatBase.second);
					currentMills = SaveValue.getInstance(mContext).readLongValue(currentMills + "", 0L);
					if (currentMills != 0) {
						readMills = currentMills;
						DBMgrProgramList.getInstance(mContext).getWriteableDB();
						List<EPGBookListViewDataItem> tempList= DBMgrProgramList.getInstance(mContext).getProgramList();
						EPGBookListViewDataItem tempInfo = null;
						for (EPGBookListViewDataItem tempItem:tempList) {
							if (tempItem.mProgramStartTime <= readMills) {
								if (tempItem.mProgramStartTime == readMills) {
									tempInfo = tempItem;
								}
								DBMgrProgramList.getInstance(mContext).deleteProgram(tempItem);
//								SaveValue.getInstance(mContext).removekey(currentMills + "");
							}
						}
						DBMgrProgramList.getInstance(mContext).closeDB();
						if (tempInfo != null && tempInfo.mChannelId != 0) {
							mGotoChannelProgram = tempInfo;
							mHandler.removeMessages(0);
							mHandler.sendEmptyMessage(0);
						}
					}
				}
			}, 1000, 1000);
		}
	}

	public void cancelTimer() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelTimer>>>" + mTimer);
		if (mTimer != null) {
			mTimer.cancel();
			mIsStarted = false;
		}
	}

	public void startAlarm() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startAlarm  mIsStarted>>>" + mIsStarted);
		if (!mIsStarted) {
			mIsStarted = true;
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(ALARM_EPG_ACTION);
			intent.addFlags(0x01000000);//for fix DTV01982284
			//create BroadcastReceiver pendingIntent
			pendingIntent = PendingIntent.getBroadcast(mContext, 0,intent, 0);
//			alarmManager.cancel(pendingIntent);
			//start timer every one minitue
//                      alarmManager.setRepeating(AlarmManager.RTC,
                 // EPGUtil.getCurrentTime() + 40000, 60*1000, pendingIntent);
            alarmManager.set(AlarmManager.RTC, EPGUtil.getCurrentTime() * 1000, pendingIntent);
		}
	}

	public void cancelAlarm() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelAlarm>>>" + pendingIntent);
		if (mIsStarted && pendingIntent != null) {
			mIsStarted = false;
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}
	}

	private void showBookConfirm(Context context) {
		if (mBookProgramConfirmDialog == null) {
			mBookProgramConfirmDialog = new TurnkeyCommDialog(context, 3);
			mBookProgramConfirmDialog.setMessage(mGotoChannelProgram.mProgramName + context.getString(R.string.nav_epg_book_program_coming_tip));
		} else {
			mBookProgramConfirmDialog.setMessage(mGotoChannelProgram.mProgramName + context.getString(R.string.nav_epg_book_program_coming_tip));
			mBookProgramConfirmDialog.setText();
		}
//		mBookProgramConfirmDialog.setMessage(context.getString(R.string.nav_epg_book_program_coming_tip));
		mBookProgramConfirmDialog.setButtonYesName(context.getString(R.string.menu_ok));
		mBookProgramConfirmDialog.setButtonNoName(context.getString(R.string.menu_cancel));
		mBookProgramConfirmDialog.show();
		mBookProgramConfirmDialog.getButtonYes().requestFocus();
		mBookProgramConfirmDialog.setPositon(-20, 70);
		mBookProgramConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
					mBookProgramConfirmDialog.dismiss();
					return true;
				}
				return false;
			}
		});

		OnKeyListener yesListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						if (mGotoChannelProgram != null && mGotoChannelProgram.mChannelId != 0) {
							CommonIntegration.getInstanceWithContext(mContext.getApplicationContext()).selectChannelById(mGotoChannelProgram.mChannelId);
						}
//						EPGChannelInfo tvChannel = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
//						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvChannel>>>" + tvChannel);
//						if (isBooking) {
//							saveBookProgram(tvChannel, currentBookedProgram);
//						} else {
//							deleteBookProgram(tvChannel, currentBookedProgram);
//						}
//						notifyEPGLinearlayoutRefresh();
						mBookProgramConfirmDialog.dismiss();
						return true;
					}
				}
				return false;
			}
		};

		OnKeyListener noListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						mBookProgramConfirmDialog.dismiss();
						return true;
					}
				}
				return false;
			}
		};
		mBookProgramConfirmDialog.getButtonNo().setOnKeyListener(noListener);
		mBookProgramConfirmDialog.getButtonYes().setOnKeyListener(yesListener);
	}

}
