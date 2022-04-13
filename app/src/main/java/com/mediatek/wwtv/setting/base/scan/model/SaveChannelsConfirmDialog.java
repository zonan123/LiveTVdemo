package com.mediatek.wwtv.setting.base.scan.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;

/**
 * Only For EU-PT-Channel Full Scan on Wizard & Menu. (Wizard ?)
 * After scan complete or scan cancel, should pop up dialog to confirm whether save channels when country=PT.
 */
public class SaveChannelsConfirmDialog {

	private Context mContext;

	public SaveChannelsConfirmDialog(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void showConfirmDialog() {

		final LiveTVDialog confirmDialog = new LiveTVDialog(mContext,
				3);
		confirmDialog
				.setMessage(mContext.getString(R.string.scan_trd_pt_save_channel));
		confirmDialog.setButtonYesName(mContext
				.getString(R.string.menu_setup_button_yes));
		confirmDialog.setButtonNoName(mContext
				.getString(R.string.menu_setup_button_no));
		confirmDialog.show();

		confirmDialog.getButtonYes().requestFocus();

		confirmDialog.setPositon(-20, 70);
		confirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& action == KeyEvent.ACTION_DOWN) {
					confirmDialog.dismiss();
					saveChannels();
					return true;
				}
				return false;
			}
		});

		OnKeyListener yesListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						confirmDialog.dismiss();
						saveChannels();
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
						confirmDialog.dismiss();
						clearChannels();
						return true;
					}
				}
				return false;
			}
		};
		confirmDialog.getButtonNo().setOnKeyListener(noListener);
		confirmDialog.getButtonYes().setOnKeyListener(yesListener);
	}

	private void saveChannels() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveChannels()");
		TVContent.releaseChanneListSnapshot();
	}

	private void clearChannels() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("clearChannels()");
		TVContent.restoreChanneListSnapshot();
		TVContent.releaseChanneListSnapshot();
	}
}
