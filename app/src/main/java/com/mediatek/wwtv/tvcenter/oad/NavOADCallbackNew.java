package com.mediatek.wwtv.tvcenter.oad;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;

public class NavOADCallbackNew extends MtkTvTVCallbackHandler {

	private NavOADActivityNew mActivity;
	public static NavOADCallbackNew mNavOADCallback;


	public static NavOADCallbackNew getInstance(NavOADActivityNew activity) {
		if (mNavOADCallback != null) {
			mNavOADCallback.removeListener();
		}

		mNavOADCallback = new NavOADCallbackNew();
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
		msg.what = NavOADActivityNew.NOTIFY_CALL_BACK;

		if(getActivity()==null){
			return -1;
		}
		getActivity().getHandler().removeMessages(msg.what);
		getActivity().getHandler().sendMessage(msg);
		return super.notifyOADMessage(messageType, scheduleInfo, progress,
				autoDld, argv5);
	}

	public NavOADActivityNew getActivity() {
		return mActivity;
	}

	public void setActivity(NavOADActivityNew mActivity) {
		this.mActivity = mActivity;
	}
}
