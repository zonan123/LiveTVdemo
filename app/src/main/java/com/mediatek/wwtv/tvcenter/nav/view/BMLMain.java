package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.twoworlds.tv.MtkTvKeyEvent;

import com.mediatek.wwtv.tvcenter.util.KeyMap;

public class BMLMain extends NavBasicMisc implements
		ComponentStatusListener.ICStatusListener {

	private static final String TAG = "BMLMain";
	private static BMLMain instance;
	private MtkTvKeyEvent mtkKeyEvent;
	private boolean isActive = false;
	private ProgressBar progressBar = null;

	public BMLMain(Context mContext) {
		super(mContext);
		// TODO Auto-generated constructor stub
		this.componentID = NAV_NATIVE_COMP_ID_BML;
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_COMPONENT_HIDE, this);
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
		mtkKeyEvent = MtkTvKeyEvent.getInstance();
		init(mContext);
	}

	public synchronized static BMLMain getInstance(Context context) {
		if (instance == null) {
			instance = new BMLMain(context);
		}
		return instance;
	}

	private void init(Context context) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initView");
		ViewGroup layoutGroup = (ViewGroup) ((Activity) context).findViewById(
				android.R.id.content).getRootView();
		progressBar = new ProgressBar(context, null,
				android.R.attr.progressBarStyleLarge);
		progressBar.setIndeterminate(true);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout rLayout = new RelativeLayout(context);
		rLayout.setGravity(Gravity.CENTER);
		rLayout.addView(progressBar);
		layoutGroup.addView(rLayout, params);
		progressBar.setVisibility(ViewGroup.GONE);
	}

	public void handleBMLMessage(int type, int message) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleBMLMessage||type =" + type);
		switch (type) {
		case 0:
			break;
		case 1:
			isActive = true;
			ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_BML);
			ComponentStatusListener.getInstance().updateStatus(
					ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
			break;
		case 2:
			isActive = false;
			if (ComponentsManager.getNativeActiveCompId() == NAV_NATIVE_COMP_ID_BML) {
				ComponentsManager.updateActiveCompId(true, 0);
				ComponentStatusListener.getInstance().updateStatus(
						ComponentStatusListener.NAV_COMPONENT_HIDE, 0);
			}
			break;
		case 3:
			progressBar.setVisibility(View.VISIBLE);
			break;
		case 4:
			progressBar.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	public boolean isBMLActive() {
		return isActive;
	}

	@Override
	public boolean isKeyHandler(int keyCode) {
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_MTKIR_TTX:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler||ttx");
			if (SundryImplement.getInstanceNavSundryImplement(mContext) != null
					&& SundryImplement.getInstanceNavSundryImplement(mContext)
							.isFreeze()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler||setFreeze(false)");
				SundryImplement.getInstanceNavSundryImplement(mContext)
						.setFreeze(false);
			}
			return processKey(keyCode);
		default:
			break;
		}
		return false;
	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BML||updateStatus =" + statusID + "||value =" + value);
		switch (statusID) {
		case ComponentStatusListener.NAV_CHANNEL_CHANGED:
			// TODO
			break;
		case ComponentStatusListener.NAV_COMPONENT_HIDE:
			if (!ComponentsManager.getInstance().isComponentsShow()) {
				ComponentsManager.nativeComponentReActive();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "bml||visibility =" + visibility);
		if (!isActive) {
			return;
		}
		super.setVisibility(visibility);
	}

	private boolean processKey(int keycode) {
		int dfbkeycode = mtkKeyEvent.androidKeyToDFBkey(keycode);
		mtkKeyEvent.sendKeyClick(dfbkeycode);
		return true;
	}
}
