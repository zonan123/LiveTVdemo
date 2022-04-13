package com.mediatek.wwtv.tvcenter.epg;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;

public class EPGChannelInfo {
	private static final String TAG = "EPGChannelInfo";

	public long mId;
	private String mChannelName;
	private int mChannelNum;
	private int mSubChannelNum=0;
	private String mDisplayNumber;
	private MtkTvChannelInfoBase mTVChannel;
	private List<EPGProgramInfo> mTVProgramInfoList;
	private Drawable mIsdbIcon;
  private boolean mIsCiVirturalCh = false;

	public MtkTvChannelInfoBase getTVChannel() {
		return mTVChannel;
	}

	public void setTVChannel(MtkTvChannelInfo mTVChannel) {
		this.mTVChannel = mTVChannel;
	}


	public EPGChannelInfo(MtkTvChannelInfoBase mCurrentChannel) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "----EPGChannelInfo:"+mCurrentChannel);
	    if(mCurrentChannel==null){
	    	return;
	    }
        String value="";
        if(mCurrentChannel instanceof MtkTvATSCChannelInfo){
            MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo)mCurrentChannel;
            value = tmpAtsc.getMajorNum()+"-" +tmpAtsc.getMinorNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelNumCur1===>"+value);
            mChannelNum = tmpAtsc.getMajorNum();
            mSubChannelNum =tmpAtsc.getMinorNum();

        }else if(mCurrentChannel instanceof MtkTvISDBChannelInfo){
            MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo)mCurrentChannel;
            value = tmpIsdb.getMajorNum()+"-"+tmpIsdb.getMinorNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelNumCur2===>"+value);
            mChannelNum = tmpIsdb.getMajorNum();
            mSubChannelNum =tmpIsdb.getMinorNum();
        }else{
            value =  " "+mCurrentChannel.getChannelNumber();
            mChannelNum=mCurrentChannel.getChannelNumber();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelNumCur3===>"+value);
        }

		mChannelName = mCurrentChannel.getServiceName();
		if (mChannelName==null) {
		    mChannelName ="";
        }
		mTVChannel = mCurrentChannel;
	}

	/**
	 * for TIF construct
	 * @param tempChannelInfo
	 */
	public EPGChannelInfo(TIFChannelInfo tempChannelInfo) {
		if(tempChannelInfo==null){
	    	return;
	    }
		if(tempChannelInfo.mMtkTvChannelInfo==null){
			mChannelNum=tempChannelInfo.mServiceId;
		}
		else if(tempChannelInfo.mMtkTvChannelInfo!=null && tempChannelInfo.mMtkTvChannelInfo instanceof MtkTvATSCChannelInfo){
            MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo)tempChannelInfo.mMtkTvChannelInfo;
            mChannelNum = tmpAtsc.getMajorNum();
            mSubChannelNum =tmpAtsc.getMinorNum();
        } else if(tempChannelInfo.mMtkTvChannelInfo!=null &&  tempChannelInfo.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo) {
            MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo)tempChannelInfo.mMtkTvChannelInfo;
            mChannelNum = tmpIsdb.getMajorNum();
            mSubChannelNum =tmpIsdb.getMinorNum();
			String bmpPath = CommonIntegration.getInstance().getISDBChannelLogo(tmpIsdb);
			mIsdbIcon = Drawable.createFromPath(bmpPath);
        } else {
        	mChannelNum=tempChannelInfo.mMtkTvChannelInfo.getChannelNumber();
        }
        mId = tempChannelInfo.mId;
        mDisplayNumber = tempChannelInfo.mDisplayNumber;
        mChannelName = tempChannelInfo.mDisplayName;
        mTVChannel = tempChannelInfo.mMtkTvChannelInfo;
    long[] datas = tempChannelInfo.mDataValue;
    if (datas != null && datas.length > 5) {
      mIsCiVirturalCh = datas[5] == MtkTvChCommonBase.SVL_SERVICE_TYPE_CI14_VIRTUAL_CH;
    } else {
      mIsCiVirturalCh = false;
    }
	}

	public EPGChannelInfo(String name, short mChannelNum) {
		this.mChannelName = name;
		this.mChannelNum = mChannelNum;
	}

	public int getPlayingTVProgramPositon() {
		if (mTVProgramInfoList == null) {
			return 0;
		}

    MtkTvTimeBase timeBase = new MtkTvTimeBase();
    long time = timeBase.getBroadcastTimeInUtcSeconds();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"time=="+time);
    int i = 0;
    for (; i < mTVProgramInfoList.size(); i++) {
        long startTime = mTVProgramInfoList.get(i).getmStartTime();
        long endTime = mTVProgramInfoList.get(i).getmEndTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," startTime=="+startTime +",endTime>>>"+endTime);
        if (time >= startTime && time <= endTime) {
				    break;
			  }
		}
		if (i >= mTVProgramInfoList.size()) {
			return 0;
		}
		return i;
	}

	public int getNextPosition(EPGProgramInfo mTVProgramInfo) {
		final EPGProgramInfo local = mTVProgramInfo;
		if (local == null || local.isDrawLeftIcon()) {
			return 0;
		}
		if (mTVProgramInfoList == null || mTVProgramInfoList.isEmpty()) {
			return 0;
		}

//		Date mStartTime = local.getmStartTime();
		final Long time = local.getmStartTime();//mStartTime.getTime();
		int i = mTVProgramInfoList.size() - 1;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Current program start time: " + time);
		EPGProgramInfo child = mTVProgramInfoList.get(i);
		while (child != null && child.getmStartTime() > time) {
			if (i <= 0) {
				return 0;
			}
			child = mTVProgramInfoList.get(--i);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Next program start time: "
					+ child.getmStartTime());
		}
		return i;

	}

  public boolean isCiVirturalCh() {
    return mIsCiVirturalCh;
  }

	public Drawable getIsdbIcon() {
		return mIsdbIcon;
	}

	public String getName() {
		return mChannelName;
	}

	public void setName(String name) {
		this.mChannelName = name;
	}

	public String getDisplayNumber() {
		return mDisplayNumber;
	}

	public int getmChanelNum() {
		return mChannelNum;
	}
	public String getmChanelNumString() {
	    if (mChannelNum<10) {
	        return "0"+mChannelNum;
        }
	    return mChannelNum+"";
	}

    public String getmSubNum(){
        if (mSubChannelNum==0) {
            return "";
        }
        if (mSubChannelNum<10) {
            return "0"+mSubChannelNum;
        }
        return mSubChannelNum+"";
    }

	public void setmChanelNum(short mChanelNum) {
		this.mChannelNum = mChanelNum;
	}

	public List<EPGProgramInfo> getmTVProgramInfoList() {
		return mTVProgramInfoList;
	}

	public void setmTVProgramInfoList(List<EPGProgramInfo> mTVProgramInfoList) {
		this.mTVProgramInfoList = mTVProgramInfoList;
	}

	public List<EPGProgramInfo> getmGroup() {
		return mTVProgramInfoList;
	}


}
