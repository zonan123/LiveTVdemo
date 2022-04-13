package com.mediatek.wwtv.tvcenter.epg;

import java.util.Date;



public class EPGProgramInfo {

	private static final String TAG = "EPGProgramInfo";

	private int channelId;
	private int programId;
	private long mStartTime;
	private String mStartTimeStr;
	private long mEndTime;
	private String mEndTimeStr;
	private String mTitle;
	private String describe;
	public String mLongDescription;
	private int categoryType[];
	private int mainType;
	private int subType;
	
	
	private String appendDescription;
	private String mProgramType;
	private String mRatingType;
	private int mRatingValue;
	
	private boolean mCancel = false;
	private boolean hasSubTitle = false;
	private float mScale;
	private float mLeftMargin;
	private boolean mProgramBlock;

	private final static int NULL_VALUE = -1;

	private boolean isDrawRightIcon = false;
	private boolean isDrawLeftIcon = false;
	private boolean isDrawBgHiL;

	public EPGProgramInfo(int channelId, int programId, long mStartTime, long mEndTime, String mTitle, String mRatingType) {
		this.channelId = channelId;
		this.programId = programId;
		this.mStartTime = mStartTime;
		this.mEndTime = mEndTime;
		this.mTitle = mTitle;
		this.mRatingType = mRatingType;
	}
	
	public EPGProgramInfo(Long mStartTime, Long mEndTime, String mTitle) {
		this(mStartTime, mEndTime, mTitle, "", NULL_VALUE, NULL_VALUE, false);
	}

	public EPGProgramInfo(Long mStartTime, Long mEndTime, String mTitle,
			String describe, int mainType, int subType, boolean mCancel) {
		this.mStartTime = mStartTime;
		this.mEndTime = mEndTime;
		this.mTitle = mTitle;
		this.describe = describe;
		this.mainType = mainType;
		this.subType = subType;
		this.mCancel = mCancel;
	}

	public EPGProgramInfo(String mStartTimeStr, String mEndTimeStr,
			String mTitle, String describe, int mainType, int subType,
			boolean mCancel, float mScale) {
		this.mStartTimeStr = mStartTimeStr;
		this.mEndTimeStr = mEndTimeStr;
		this.mTitle = mTitle;
		this.describe = describe;
		this.mainType = mainType;
		this.subType = subType;
		this.mCancel = mCancel;
		this.mScale = mScale;
//		this.mStartTime = EPGTimeConvert.getNormalDate(mStartTimeStr);
//		this.mEndTime = EPGTimeConvert.getNormalDate(mEndTimeStr);
	}

	public EPGProgramInfo(String mStartTimeStr, String mEndTimeStr,
			String mTitle) {
		this(mStartTimeStr, mEndTimeStr, mTitle, "", NULL_VALUE, NULL_VALUE,
				false, 0.0f);
	}
	
	public void setProgramBlock(boolean block) {
		mProgramBlock = block;
	}
	
	public boolean isProgramBlock() {
		return mProgramBlock;
	}

	public void setChannelId(int id) {
		channelId = id;
	}
	
	public int getChannelId() {
		return channelId;
	}
	
	public void setProgramId(int id) {
		programId = id;
	}
	
	public int getProgramId() {
		return programId;
	}
	
	public boolean isDrawLeftIcon() {
		return isDrawLeftIcon;
	}

	public void setDrawLeftIcon(boolean isDrawLeftIcon) {
		this.isDrawLeftIcon = isDrawLeftIcon;
	}

	public boolean isDrawRightwardIcon() {
		return isDrawRightIcon;
	}

	public void setDrawRightIcon(boolean isDrawRightIcon) {
		this.isDrawRightIcon = isDrawRightIcon;
	}

	public void setBgHighLigth(boolean isHighligth) {
		isDrawBgHiL = isHighligth;
	}

	public boolean isBgHighLight() {
		return isDrawBgHiL;
	}
	
	public String getmStartTimeStr() {
		return mStartTimeStr;
	}

	public void setmStartTimeStr(String mStartTimeStr) {
		this.mStartTimeStr = mStartTimeStr;
	}

	public String getmEndTimeStr() {
		return mEndTimeStr;
	}

	public void setmEndTimeStr(String mEndTimeStr) {
		this.mEndTimeStr = mEndTimeStr;
	}

	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getDescribe() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG event detail: " + describe);
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public int getMainType() {
		return mainType;
	}

	public void setMainType(int mainType) {
		this.mainType = mainType;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public boolean ismCancel() {
		return mCancel;
	}

	public void setmCancel(boolean mCancel) {
		this.mCancel = mCancel;
	}

	public boolean isHasSubTitle() {
		return hasSubTitle;
	}

	public void setHasSubTitle(boolean mHasSubTilte) {
		this.hasSubTitle = mHasSubTilte;
	}

	public float getmScale() {
		return mScale;
	}

	public void setmScale(float mScale) {
		this.mScale = mScale;
	}

	public Long getmStartTime() {
		return mStartTime;
	}

	public float getLeftMargin() {
		return mLeftMargin;
	}

	public void setLeftMargin(float mLeftMargin) {
		this.mLeftMargin = mLeftMargin;
	}

	public void setmStartTime(Long mStartTime) {
		this.mStartTime = mStartTime;
	}

	public Long getmEndTime() {
		return mEndTime;
	}

	public void setmEndTime(Long mEndTime) {
		this.mEndTime = mEndTime;
	}

	public void setCategoryType(int category[]) {
		categoryType = category;
	}
	
	public int[] getCategoryType() {
		return categoryType;
	}
	
	public void setRatingType(String ratingType) {
		mRatingType = ratingType;
	}
	
	public String getRatingType() {
		return mRatingType == null?"":mRatingType; 
	}
	
	
	
	public int getRatingValue() {
		return mRatingValue;
	}

	public void setRatingValue(int ratingValue) {
		this.mRatingValue = ratingValue;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}
	
	
	

	public String getProgramType() {
		return mProgramType;
	}

	public void setProgramType(String programType) {
		this.mProgramType = programType;
	}

	public String getLongDescription() {
		return mLongDescription;
	}

	public void setLongDescription(String longDescription) {
		this.mLongDescription = longDescription;
	}
	
	

	public String getAppendDescription() {
		return appendDescription;
	}

	public void setAppendDescription(String appendDescription) {
		this.appendDescription = appendDescription;
	}

	public int countTimeZoom(int hours, int timeZoomSpan) {
		// Subscript is to start from zero, so to minus 1
      return hours % timeZoomSpan == 0 ? hours / timeZoomSpan - 1
        : hours / timeZoomSpan;
	}

	public float dateToMinutes(Date time, int mTimeZoomSpan, boolean flag) {
		int timeZoom = countTimeZoom(time.getHours(), mTimeZoomSpan);
		if (flag == true) {
			return (time.getHours() - timeZoom * mTimeZoomSpan - 1) * 60.0f
					+ time.getMinutes();
		}
		return ((timeZoom + 1) * mTimeZoomSpan + 1 - time.getHours()) * 60.0f
				- time.getMinutes();
	}
}
