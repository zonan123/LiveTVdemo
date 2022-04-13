/**
 * @Description:
 */
package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateBase;
import com.mediatek.wwtv.tvcenter.dvr.manager.CoreHelper;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

/**
 *
 */
@SuppressLint("ViewConstructor")
public class DVRControlbar extends BaseInfoBar {

	private static float wScale = 1f;
	private static float hScale = 0.11f; //hight change to 0.2f to 0.23f fix DTV00585450

	StateBase state;

	public DVRControlbar(Context context) {
		super(context, R.layout.pvr_timeshfit_nf);
	}

	public DVRControlbar(Context context, int layoutID, Long duration, StateBase state) {
		super(context, layoutID, duration,(int)(ScreenConstant.SCREEN_WIDTH * wScale), (int)(ScreenConstant.SCREEN_HEIGHT * hScale));

		this.state=state;
	}

	public void doSomething() {
		super.doSomething();
		android.util.Log.d("DVRControlbar", "dosomething");
	}

	public void show() {
		super.show();
		android.util.Log.d("DVRControlbar", "show");
	}

	@Override
	public void setLocation() {

		showAtLocation(
			(View)TurnkeyUiMainActivity.getInstance().findViewById(CoreHelper.ROOT_VIEW),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
				(int)0, 
				(int)0);
	}
}
