package com.mediatek.wwtv.setting.view;


import java.util.TimerTask;

import com.mediatek.wwtv.setting.base.scan.model.OnValueChangedListener;
import com.mediatek.wwtv.setting.base.scan.model.RespondedKeyEvent;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter.DataItem;


public class OptionView  extends ListViewItemView implements RespondedKeyEvent{

	private static final String TAG = "OptionView";
	// the item name, value of list view item
	private TextView mNameView;
	private TextView mValueView;
	private OnValueChangedListener mValueChangedListener;
    private ImageView mRightImageIv;

	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mValueChangedListener != null) {
				mValueChangedListener.onValueChanged(OptionView.this,
						mDataItem.mInitValue);
			}
		}
	}

	public OptionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	public OptionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public OptionView(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public int getValue() {
		return this.mDataItem.getmInitValue();
	}

	public void setValue(int mPositon) {
		this.mDataItem.setmInitValue(mPositon);
		mValueView.setText(this.mDataItem.getmOptionValue()[mPositon]);
		if (mValueChangedListener != null) {
			mValueChangedListener.onValueChanged(this, mPositon);
		}
	}

	private void setViewNameEx(String viewName) {
		mNameView.setText(viewName);
	}

	public TextView getNameView() {
		return mNameView;
	}

	public TextView getValueView() {
		return mValueView;
	}

	public void setValueChangedListener(
			OnValueChangedListener mValueChangedListener) {
		this.mValueChangedListener = mValueChangedListener;
	}

	private void init() {
		LinearLayout lv = (LinearLayout) inflate(context,
				R.layout.menu_option_view, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(lv, params);
		mNameView = (TextView) findViewById(R.id.common_tv_itemname);
		mValueView = (TextView) findViewById(R.id.common_tv_itemshow);
        mRightImageIv = (ImageView) findViewById(R.id.common_iv_itemimage);
	}

	public void setAdapter(DataItem mDataItem) {
//		com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter.DataItem
		this.mDataItem = mDataItem;
		mId = mDataItem.mItemID;
		setViewNameEx(this.mDataItem.getmName());
		try{
			mValueView
					.setText(this.mDataItem.mOptionValue[this.mDataItem.mInitValue]);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	protected void switchValueNext() {
		try{
			if (mDataItem.mInitValue != mDataItem.mOptionValue.length - 1) {
				mValueView
						.setText(mDataItem.mOptionValue[++mDataItem.mInitValue]);
			} else {
				mDataItem.mInitValue = 0;
				mValueView
						.setText(mDataItem.mOptionValue[mDataItem.mInitValue]);
			}

			mValueChangedListener.onValueChanged(this, mDataItem.mInitValue);

			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "" + mDataItem.mInitValue);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected void switchValuePrevious() {
		try{
			if (mDataItem.mInitValue != 0) {
				mValueView
						.setText(mDataItem.mOptionValue[--mDataItem.mInitValue]);
			} else {
				mDataItem.mInitValue = mDataItem.mOptionValue.length - 1;
				mValueView
						.setText(mDataItem.mOptionValue[mDataItem.mInitValue]);
			}

			mValueChangedListener.onValueChanged(this, mDataItem.mInitValue);
		}catch(Exception e){
			e.printStackTrace();
		}


	}

	public void onKeyEnter() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyEnter");
	}

	public void onKeyLeft() {
		if (mDataItem.isEnable) {
			// if ( mId.equals(MenuConfigManager.NETWORK_CONNECTION)) {
			// TimerTask tt = new MyTimerTask();
			// Timer timer = new Timer();
			// timer.schedule(tt, 500);
			// timers.add(timer);
			// com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "size:" + timers.size() + "********************");
			// for (int i = 0; i < timers.size() - 1; i++) {
			// timers.get(i).cancel();
			// }
			// }
			switchValuePrevious();
		}
	}

	public void onKeyRight() {
		if (mDataItem.isEnable) {
			// if (mId.equals(MenuConfigManager.NETWORK_INTERFACE)) {
			// TimerTask tt = new MyTimerTask();
			// Timer timer = new Timer();
			// timer.schedule(tt, 500);
			// timers.add(timer);
			// com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "size:" + timers.size() + "********************");
			// for (int i = 0; i < timers.size() - 1; i++) {
			// timers.get(i).cancel();
			// }
			// }
			switchValueNext();
		}
	}

	public void showValue(int value) {
		if (value < 0 || value > mDataItem.mOptionValue.length - 1) {
			throw new IllegalArgumentException("value is Illegal value");
		}
		mDataItem.mInitValue = value;
		mValueView.setText(mDataItem.mOptionValue[value]);
	}

    public void setRightImageSource(boolean isHighlight) {
		if (isHighlight) {
			mRightImageIv.setImageResource(R.drawable.menu_icon_select_hi);
		} else {
			mRightImageIv.setImageResource(R.drawable.menu_icon_select_nor);
		}
	}
}
