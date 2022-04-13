package com.mediatek.wwtv.tvcenter.oad;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;

public class NavOADCallback extends MtkTvTVCallbackHandler {

	private NavOADActivity mActivity;
	public static NavOADCallback mNavOADCallback;


	public static NavOADCallback getInstance(NavOADActivity activity) {
		if (mNavOADCallback != null) {
			mNavOADCallback.removeListener();
		}

		mNavOADCallback = new NavOADCallback();
		mNavOADCallback.setActivity(activity);
		return mNavOADCallback;
	}

	@Override
	public int notifyOADMessage(int messageType, String scheduleInfo,
			int progress, boolean autoDld, int argv5) throws RemoteException {
		Bundle bundle = new Bundle();
		bundle.putInt("arg1", messageType);
		bundle.putString("arg2", scheduleInfo);
		bundle.putInt("arg3", progress);
		bundle.putBoolean("arg4", autoDld);
		bundle.putInt("arg5", argv5);

		Message msg = Message.obtain();
		msg.setData(bundle);
		msg.what = NavOADActivity.NOTIFY_CALL_BACK;

		if(getActivity()==null||getActivity().isDestroyed()){
			return -1;
		}
		getActivity().getmHandler().removeMessages(msg.what);
		getActivity().getmHandler().sendMessage(msg);
		return super.notifyOADMessage(messageType, scheduleInfo, progress,
				autoDld, argv5);
	}

	public NavOADActivity getActivity() {
		return mActivity;
	}

	public void setActivity(NavOADActivity mActivity) {
		this.mActivity = mActivity;
	}
}
