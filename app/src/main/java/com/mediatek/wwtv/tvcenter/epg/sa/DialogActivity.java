package com.mediatek.wwtv.tvcenter.epg.sa;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

public class DialogActivity extends Activity {
	private static String TAG = "DialogActivity";
	private TurnkeyCommDialog mBookProgramConfirmDialog;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.epg_alarm_layout);
		mContext = getApplicationContext();
		Intent intent = getIntent();
		String programName = intent.getStringExtra("programname");
		int channelid = intent.getIntExtra("channelid", 0);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DialogActivity   come in>>" + programName + "   " + channelid);
		showConfirmDlg(programName, channelid);
	}

	private void showConfirmDlg(String programName, final int channelid) {
		if (mBookProgramConfirmDialog == null) {
			mBookProgramConfirmDialog = new TurnkeyCommDialog(this, 3);
			mBookProgramConfirmDialog.setMessage(programName + getString(R.string.nav_epg_book_program_coming_tip));
		} else {
			mBookProgramConfirmDialog.setMessage(programName + mContext.getString(R.string.nav_epg_book_program_coming_tip));
			mBookProgramConfirmDialog.setText();
		}
//		mBookProgramConfirmDialog.setMessage(this.getString(R.string.menu_tv_reset_all));
		mBookProgramConfirmDialog.setButtonYesName(this.getString(R.string.menu_ok));
		mBookProgramConfirmDialog.setButtonNoName(this.getString(R.string.menu_cancel));
		mBookProgramConfirmDialog.show();
		mBookProgramConfirmDialog.getButtonYes().requestFocus();
		mBookProgramConfirmDialog.setPositon(-20, 70);
		mBookProgramConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
					mBookProgramConfirmDialog.dismiss();
					finish();
					return true;
				}
				return false;
			}
		});

		OnKeyListener yesListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						if (channelid != 0) {
                            if(!CommonIntegration.getInstance().isCurrentSourceTv()){
                                CommonIntegration.getInstance().iSetSourcetoTv();
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change to TV Source");
                            }
							CommonIntegration.getInstanceWithContext(mContext.getApplicationContext()).selectChannelById(channelid);
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
                        //finish EPG
						finish();
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
						finish();
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
