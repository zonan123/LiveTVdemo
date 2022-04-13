package com.mediatek.wwtv.setting.widget.view;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.setting.util.SettingsUtil;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
//import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * channel edit finetune
 * 
 * @author hs_haosun
 * 
 */
public class FinetuneDialog extends Dialog {
	static final String TAG = "FinetuneDialog";
	private TextView numText;
	private TextView nameText;
	private TextView adjustText;
	private int xOff;
	private int yOff;
	public int menuWidth = 0;
	public int menuHeight = 0;
	public int count = 2;

	public FinetuneDialog(Context context) {
		super(context, R.style.Theme_TurnkeyCommDialog);

	}

	/*public FinetuneDialog(Context context, int buttonCount) {
		super(context, R.style.Theme_TurnkeyCommDialog);
	}*/

	/*public FinetuneDialog(Context context, String title, String info) {
		super(context, R.style.Theme_TurnkeyCommDialog);
	}*/

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_comm_finetune);
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		menuWidth =SettingsUtil.SCREEN_WIDTH/3;
		menuHeight =SettingsUtil.SCREEN_HEIGHT/3;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SettingsUtil.SCREEN_WIDTH: " + SettingsUtil.SCREEN_WIDTH+"  SettingsUtil.SCREEN_HEIGHT:  "+SettingsUtil.SCREEN_HEIGHT);
		lp.width = menuWidth;
		lp.height= menuHeight;
		window.setAttributes(lp);
		init();
	}

	private void init() {
		numText = (TextView) findViewById(R.id.comm_finetune_numr);
		nameText = (TextView) findViewById(R.id.comm_finetune_namer);
		adjustText = (TextView) findViewById(R.id.comm_finetune_frequency);
	}

	public void setNumText(String num) {
		String chNum = this.getContext().getString(R.string.menu_tv_channel_no);
		numText.setText(chNum + num);
	}

	public void setNameText(String name) {
		nameText.setText(name);
	}

	public void setAdjustText(String hz) {
		String adjustFre = this.getContext().getString(R.string.menu_tv_freq);
		adjustText.setText(adjustFre + hz);
	}

	/**
	 * Set the dialog's position relative to the (0,0)
	 */
	public void setPositon(int xoff, int yoff) {
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.x = xoff;
		lp.y = yoff;
		this.xOff = xoff;
		this.yOff = yoff;
		window.setAttributes(lp);
	}

	public void setSize() {
		Window window = getWindow();
		Display d = window.getWindowManager().getDefaultDisplay();
		WindowManager.LayoutParams p = window.getAttributes();
		p.height = (int) (d.getHeight());
		p.width = (int) (d.getWidth());
		window.setAttributes(p);
	}

	public TextView getNumText() {
		return numText;
	}

	public void setNumText(TextView numText) {
		this.numText = numText;
	}

	public TextView getNameText() {
		return nameText;
	}

	public void setNameText(TextView nameText) {
		this.nameText = nameText;
	}

	public TextView getAdjustText() {
		return adjustText;
	}

	public void setAdjustText(TextView adjustText) {
		this.adjustText = adjustText;
	}

	public void setxOff(int xOff) {
		this.xOff = xOff;
	}

	public void setyOff(int yOff) {
		this.yOff = yOff;
	}

	public int getxOff() {
		return xOff;
	}

	public int getyOff() {
		return yOff;
	}
	
	// for volume down/up
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyMap.KEYCODE_MTKIR_MUTE:
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
