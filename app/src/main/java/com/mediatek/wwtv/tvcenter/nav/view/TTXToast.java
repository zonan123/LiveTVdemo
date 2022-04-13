package com.mediatek.wwtv.tvcenter.nav.view;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

public class TTXToast extends Toast {

	private boolean isShowing = false;

	public TTXToast(Context context) {
		super(context);
	}
	
	public TTXToast(Context c, View v) {
		this(c);
		setView(v);
	}

	public void showAlways() {
		Field mTNField = null;
		try {
			mTNField = Toast.class.getDeclaredField("mTN");
			mTNField.setAccessible(true);
			Object mTN = mTNField.get(this);
			try {
				Field mNextViewField = mTN.getClass().getDeclaredField(
						"mNextView");
				mNextViewField.setAccessible(true);
				mNextViewField.set(mTN, getView());
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
			try{
				Method showMethod = mTN.getClass().getDeclaredMethod("show");
				showMethod.setAccessible(true);
				showMethod.invoke(mTN);
				isShowing = true;
			}catch(Exception e){
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void show() {
		super.show();
		isShowing = true;
	}

	public void show(long delayMillis) {
		showAlways();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				cancel();
			}
		}, delayMillis);
	}

	@Override
	public void cancel() {
		super.cancel();
		isShowing = false;

	}

	public boolean isShowing() {
		return isShowing;
	}
	
}