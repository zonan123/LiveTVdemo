package com.mediatek.wwtv.tvcenter.nav.util;


import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import com.mediatek.wwtv.tvcenter.R;

/**
 *
 * @author sin_biaoqinggao
 *
 */
public class SleepTimerTipDialog extends Dialog{

//	private Context context;
	String itemId;

	public SleepTimerTipDialog(Context context) {
		super(context, R.style.Theme_SleepTipDialog);
	}

	public SleepTimerTipDialog(Context context,String itemId) {
		super(context, R.style.Theme_SleepTipDialog);
		this.itemId = itemId;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTipDialog", "hi sleep dialog init");
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_sleep_tip_dialog);
	}



	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		this.dismiss();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTipDialog", "hi dialog dispatchKeyEvent");
		SleepTimerTask.doCancelTask(itemId);
		//should set sleep timer to off(if in current menu UI maybe refresh UI)
		if(isShowing()) {
		    return super.dispatchKeyEvent(event);
		} else {
		    return true;
		}
    // return this.getOwnerActivity().dispatchKeyEvent(event);
	}

	public void setPositon(int xoff, int yoff) {
		Window window = this.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.x = xoff * ScreenConstant.SCREEN_WIDTH/1280;
		lp.y = yoff*ScreenConstant.SCREEN_HEIGHT/720;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTipDialog", "sleep position =="+lp.x+","+lp.y);
		window.setAttributes(lp);
	}


}
