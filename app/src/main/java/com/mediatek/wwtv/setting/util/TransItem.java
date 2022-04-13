package com.mediatek.wwtv.setting.util;

import java.io.Serializable;

public class TransItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -115591532090430814L;
	
	public TransItem(String id,String title,int initVlaue,int startValue,int endValue){
		mItemID = id ;
		mTitle = title;
		mStartValue = startValue;
		mInitValue = initVlaue;
		mEndValue = endValue;
	}
	
	public String mItemID;//config id
	public String mTitle ;
	
	// parameter's minValue of ProgressView ,PositionView
	public int mStartValue;
	// parameter's maxValue of ProgressView ,PositionView
	public int mEndValue;
	// ProgressView ,PositionView initial data and OptionView initial data
	public int mInitValue;
	
	public String getmItemId() {
		return mItemID;
	}
	public void setmItemId(String mItemID) {
		this.mItemID = mItemID;
	}
	
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	
	
	
	

}
