/**   
 * @Description: TODO() 
 */
package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;

/**
 *
 */
public class NavCommonInfoBar extends NavBaseInfoBar {

	private TextView mInfo;
	//private ImageView mIconImage;
	
	private String mToastString = "";

	
	public final static int COMMON_INFO = 0;
	public final static int WARRNING_INFO = 1;
	public final static int ERROR_INFO = 2;

	private int infoType = COMMON_INFO;

	public NavCommonInfoBar(Activity context) {
		super(context, R.layout.nav_common_info_nf);
	}

	public NavCommonInfoBar(Activity context, String info) {
		super(context, R.layout.nav_common_info_nf);
		mToastString = info;
	}

	public NavCommonInfoBar(Activity context, String info, int type) {
		super(context, R.layout.nav_common_info_nf);
		mToastString = info;
		infoType=type;
	}

	public void setInfo(String info) {
		mInfo.setText(info);
	}

	@Override
	public void initView() {
		super.initView();
		ImageView mIconImage = (ImageView) (getContentView().findViewById(
				R.id.nav_common_info_icon));
		
		switch (infoType) {
		case COMMON_INFO:
			
			break;
		case WARRNING_INFO:
			mIconImage.setVisibility(View.VISIBLE);
			mIconImage.setBackgroundResource(R.drawable.nav_ib_warning_icon);
			break;
		case ERROR_INFO:
			break;
		default:
			mIconImage.setVisibility(View.VISIBLE);
			mIconImage.setBackgroundResource(R.drawable.nav_ib_warning_icon);
            break;
		}

		mInfo = (TextView) getContentView().findViewById(R.id.info);
		mInfo.setText(mToastString);
	}
}
