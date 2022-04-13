package com.mediatek.wwtv.tvcenter.epg.us;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.mediatek.wwtv.tvcenter.epg.EPGUtil;


import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.mediatek.wwtv.tvcenter.R;
public class ListItemView extends RelativeLayout {

	private static final String TAG = "ListItemView";
    private Context mContext;
    public ListItemView(Context context) {
        super(context);
        mContext=context;
        // TODO Auto-generated constructor stub
        init();
    }

	public ListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        // TODO Auto-generated constructor stub
        init();
    }

    private TextView mNameTextView;
	private TextView textContent;
	private ImageView imageView ;
	private ImageView mProgramLockImageView;
//	private String mNum = "0000";
//	private String mFreq = "";
//	private String temp1 = "";
//	private String temp2 = "";

	public void init() {
		LinearLayout lv = (LinearLayout) inflate(mContext,
				R.layout.epg_us_listitem_view, null);
		float curDensity = mContext.getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, (int)(35*curDensity));
		addView(lv, params);
	      setFocusable(true);
	        setClickable(true);
	        setEnabled(true);
		mNameTextView = (TextView) findViewById(R.id.common_text_itemname);
		textContent = (TextView) findViewById(R.id.common_text_content);
		imageView =(ImageView)findViewById(R.id.program_imageview);
		mProgramLockImageView = (ImageView)findViewById(R.id.program_lock_imageview);
	}

	public void setAdapter(ListItemData itemData) {
		if (!itemData.isValid()) {
		    mNameTextView.setVisibility(View.INVISIBLE);
		    imageView.setVisibility(View.INVISIBLE);
        }else {
            mNameTextView.setVisibility(View.VISIBLE);
            setViewNameEx(itemData.getItemTime());
        }
		if (itemData.isCC()) {
		    imageView.setVisibility(View.VISIBLE);
        } else {
        	imageView.setVisibility(View.INVISIBLE);
        }
//		if (mItemData.isBlocked()) {
//			mProgramLockImageView.setVisibility(View.VISIBLE);
//		} else {
//			mProgramLockImageView.setVisibility(View.INVISIBLE);
//		}
    mProgramLockImageView.setVisibility(View.INVISIBLE);
		long millsStartTime=itemData.getMillsStartTime();
		String strStartTime=EPGUtil.formatTime(itemData.getMillsStartTime(),mContext);
		String programStartTime=millsStartTime>0?strStartTime:"";
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programStartTime="+programStartTime);
		textContent.setText(String.format("%s   %s", programStartTime, itemData.getItemProgramName()));
		itemData.setItemProgramContent(programStartTime + "   "+itemData.getItemProgramName());
	}
//
//	public void inputNum(String ch) {
//		textContent.setText(mNum);
//		mNum = mNum + ch;
//		mNum = mNum.substring(1, 5);
//		textContent.setText(mNum);
//	}
//
//	public void inputFreq(String ch) {
//		mFreq = mFreq + ch;
//		if (mFreq.length() > 3) {
//			int c = 0;
//			if (ch.equals(".")) {
//				c = mFreq.indexOf(".");
//				mFreq = mFreq.substring(0, c);
//				mFreq = mFreq + ch;
//			} else if (!mFreq.contains(".")) {
//				mFreq = mFreq.substring(1, 4);
//			} else if (mFreq.contains(".")) {
//				if (mFreq.length() > 8) {
//					temp1 = mFreq.substring(0, 4);
//					temp2 = mFreq.substring(5, 9);
//					mFreq = temp1 + temp2;
//				}
//			}
//		}
//		textContent.setText(mFreq);
//	}
//
	public void setViewNameEx(String name) {
		mNameTextView.setText(name);
	}
//
//	public int getValue() {
//		return 0;
//	}
//
//	public void onKeyEnter() {
//
//	}
//
//	public void onKeyLeft() {
//
//	}
//
//	public void onKeyRight() {
//	}
//
//	public void setValue(int value) {
//	}
//
//	public TextView getTextContent() {
//		return textContent;
//	}
//
//	public void setTextContent(TextView textContent) {
//		this.textContent = textContent;
//	}
//
//	public void showValue(int value) {
//		if (value < mDataItem.mStartValue || value > mDataItem.mEndValue) {
//			Log.v(TAG, "invalid value");
//		} else {
//			mDataItem.mInitValue = value;
//			textContent.setText(mDataItem.mOptionValue[value]);
//		}
//	}
//
//	public TextView getTextName() {
//		return mNameTextView;
//	}
}
