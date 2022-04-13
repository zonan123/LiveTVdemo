/**   
 * @Description: TODO() 
 */
package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;

/**
 *
 */
public class CommonInfoBar extends BaseInfoBar {

	//private TextView mInfo;
	private String mToastString="";
	
	public CommonInfoBar(Context context) {
		super(context,R.layout.pvr_timeshfit_nf);
	}
	
	public CommonInfoBar(Activity context,String info) {
		super(context,R.layout.pvr_timeshfit_nf);
		mToastString=info;
	}

	public CommonInfoBar(Activity context, int layoutID, Long duration, String strInfo) {
		super(context,layoutID);

		this.mDefaultDuration = duration;
		this.mToastString = strInfo;
	}

	public void setInfo(String info) {
		mToastString = info;
//		mInfo.setText(mToastString);
	}

	@Override
	public void initView() {
		super.initView();
		TextView mInfo = (TextView) getContentView().findViewById(R.id.info);
		mInfo.setText(mToastString);
	}

	public void doSomething() {
		android.util.Log.d("CommonInfoBar", "do something");
		super.doSomething();
	}
	@Override
	public void show() {
		initView();
		super.show();
	}
	
	
	
}
