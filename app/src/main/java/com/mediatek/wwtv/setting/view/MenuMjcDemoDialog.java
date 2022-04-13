package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.util.MenuConfigManager;

public class MenuMjcDemoDialog extends Dialog {

	private Context mContext;
	private TextView vLeft;
	private TextView vRight;
	public MenuMjcDemoDialog(Context context) {
		super(context, R.style.Theme_ActivityDialog);
		mContext = context;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_mjc_demo);

//		Window window = getWindow();
//		WindowManager.LayoutParams lp = window.getAttributes();
//		lp.width= ScreenConstant.SCREEN_WIDTH;
//		lp.height= ScreenConstant.SCREEN_HEIGHT;
//		window.setAttributes(lp);

		vLeft = (TextView) findViewById(R.id.mjc_left);
		vRight = (TextView) findViewById(R.id.mjc_right);
		initData();
	}

	private void initData() {
		final MenuConfigManager mg = MenuConfigManager.getInstance(mContext);
		int value =mg.getDefault(MenuConfigManager.DEMO_PARTITION);
		switch (value) {
            case MtkTvConfigType.CFG_MJC_DEMO_RIGHT:
                vRight.setVisibility(View.VISIBLE);
                vLeft.setVisibility(View.VISIBLE);
                vLeft.setText(mContext.getString(R.string.menu_video_mjc_demo_off));
                vRight.setText(mContext.getString(R.string.menu_video_mjc_demo_on));
                break;
            case MtkTvConfigType.CFG_MJC_DEMO_LEFT:
                vRight.setVisibility(View.VISIBLE);
                vLeft.setVisibility(View.VISIBLE);
                vLeft.setText(mContext.getString(R.string.menu_video_mjc_demo_on));
                vRight.setText(mContext.getString(R.string.menu_video_mjc_demo_off));
                break;
            case MtkTvConfigType.CFG_MJC_DEMO_OFF:
                vRight.setVisibility(View.GONE);
                vLeft.setVisibility(View.GONE);
                break;
            default:
                break;
        }
		// set demo_partition right
		mg.setValue(MenuConfigManager.CFG_VIDEO_VID_MJC_DEMO_STATUS, 1);
	}

}
