package com.mediatek.wwtv.setting.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;


import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter;
import com.mediatek.wwtv.setting.base.scan.model.OnValueChangedListener;
import com.mediatek.wwtv.setting.base.scan.model.RespondedKeyEvent;
import com.mediatek.wwtv.setting.base.scan.model.UpdateTime;
import com.mediatek.wwtv.tvcenter.R;


public class DateTimeInputView  extends ListViewItemView implements
RespondedKeyEvent  {
	private static final String TAG = "DateTimeInputView";
	public int mMinutes;
	public int mHour;
	public int second = 0;
	// private Thread mClockThread
	private TextView mTextViewName;
	private DateTimeView mDateTimeView;
	// private DateTimeView dateView;
	// private DateTimeView timeView;
	public static final int DATETYPE = 0x00000000;
	public static final int TIMETYPE = 0x00000001;

	public static final String SCHEDULE_PVR_SRCTYPE = "SCHEDULE_PVR_SRCTYPE";
	public static final String SCHEDULE_PVR_CHANNELLIST = "SCHEDULE_PVR_CHANNELLIST";
	public static final String SCHEDULE_PVR_REMINDER_TYPE = "SCHEDULE_PVR_REMINDER_TYPE";
	public static final String SCHEDULE_PVR_REPEAT_TYPE = "SCHEDULE_PVR_REPEAT_TYPE";
	public static final String AUTO_SYNC = "SETUP_auto_syn";
	public static final String TIME_DATE = "SETUP_date";
	public static final String TIME_TIME = "SETUP_time";
	public static final int INVALID_VALUE = 10004;
	public static final int STEP_VALUE = 1;

	// private DataItem mDataItem
	public UpdateTime updateProcess;
	private OnValueChangedListener mValueChangedListener;

	public OnValueChangedListener getValueChangedListener() {
		return mValueChangedListener;
	}

	public void setValueChangedListener(
			OnValueChangedListener mValueChangedListener) {
		this.mValueChangedListener = mValueChangedListener;
	}

	public DateTimeInputView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	public DateTimeInputView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public DateTimeInputView(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public void setAdapter(SetConfigListViewAdapter.DataItem mDataItem) {
		this.mDataItem = mDataItem;
		mTextViewName.setText(mDataItem.getmName());
		//com.mediatek.wwtv.tvcenter.util.MtkLog.d("timestring","come in set TIME_TIME");
		if (mDataItem.isAutoUpdate()) {
		      if (TIME_TIME.equals(mDataItem.getmItemID())) {
		            // final DateTimeView timeView = (DateTimeView)
		            // findViewById(R.id.common_datetimeview);
		            final UpdateTime updateProcess = new UpdateTime(
		                    TIME_TIME);
		            updateProcess.startprocess(new UpdateTime.UpdateListener() {
		                public void update(String mString) {
		                //com.mediatek.wwtv.tvcenter.util.MtkLog.d("timestring","come in set TIME_TIME end");
		                    //mDateTimeView.setDateStr(mString, updateProcess);
		                    mDateTimeView.postInvalidate();
		                }
		            },context);
		        } else if (TIME_DATE.equals(mDataItem.getmItemID())) {
		            // final DateTimeView dateView = (DateTimeView)
		            // findViewById(R.id.common_datetimeview);
		            final UpdateTime updateProcess = new UpdateTime(
		                    TIME_DATE);
		            updateProcess.startprocess(new UpdateTime.UpdateListener() {
		                public void update(String mString) {
		                //com.mediatek.wwtv.tvcenter.util.MtkLog.d("timestring","come in set TIME_TIME end");
		                    // TODO Auto-generated method stub
		                    //mDateTimeView.setDateStr(mString, updateProcess);
		                    mDateTimeView.postInvalidate();
		                }
		            },context);

		        }
		} else {
//            this.mDateTimeView.setDateStr(mDataItem.getmDateTimeStr(),
//                    new UpdateTime());
            mDateTimeView.postInvalidate();
        }

		this.mDateTimeView.mType = mDataItem.getmDateTimeType();

	}

	private void init() {
		LinearLayout lv = (LinearLayout) inflate(context,
				R.layout.menu_datetime_input_view, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(lv, params);

		mTextViewName = (TextView) findViewById(R.id.common_itemname);
		mDateTimeView = (DateTimeView) findViewById(R.id.common_datetimeview);
	}

	public void setWhiteColor(){
	    mDateTimeView.setDrawDone(true);
	    mDateTimeView.postInvalidate();
	}

	public void setFlag(){
		mDateTimeView.flag=true;
		mDateTimeView.setDrawDone(false);
	    mDateTimeView.postInvalidate();
	}

	public int getValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void onKeyEnter() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyEnter");
	}

	public void onKeyLeft() {
		if (mDateTimeView != null) {
			mDateTimeView.onKeyLeft();
		}
	}

	public void onKeyRight() {
		if (mDateTimeView != null) {
			mDateTimeView.onKeyRight();
		}
	}

	protected void onDraw(Canvas canvas) {
		mDateTimeView.postInvalidate();
		super.onDraw(canvas);
	}

	public void setCurrentSelectedPosition(int mCurrentSelectedPosition) {
		mDateTimeView.setCurrentSelectedPosition(mCurrentSelectedPosition);
	}

	public void setValue(int value) {
		// TODO Auto-generated method stub

	}

	public DateTimeView getmDateTimeView() {
		return mDateTimeView;
	}

	public void setmDateTimeView(DateTimeView mDateTimeView) {
		this.mDateTimeView = mDateTimeView;
	}

	public void showValue(int value) {
		// TODO Auto-generated method stub

	}

	public TextView getmTextViewName() {
		return mTextViewName;
	}

	public void setmTextViewName(TextView mTextViewName) {
		this.mTextViewName = mTextViewName;
	}

}
