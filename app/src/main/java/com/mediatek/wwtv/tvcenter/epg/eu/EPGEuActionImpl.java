package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;


import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvEvent;

public  class EPGEuActionImpl implements EPGEuIAction {
	private static final String TAG = "EPGEuActionImpl";
	private DataReader mReader;
	private Activity mContext;
	private EPGEuIView iUIView;
	private boolean mIs3rdTVSource;
	private int timeType;
	private boolean mIsCountryUK;
	private boolean mIsCurrentSourceATV;

	public EPGEuActionImpl(Activity context, EPGEuIView view) {
		mContext = context;
		iUIView = view;
		mReader = DataReader.getInstanceWithoutContext();
		initData();
	}

	@Override
	public boolean is3rdTVSource() {
		return mIs3rdTVSource;
	}

	@Override
	public void getChannelList() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<EPGChannelInfo> channelList = null;
				if (CommonIntegration.supportTIFFunction()) {
					channelList = (ArrayList<EPGChannelInfo>) mReader
							.getAllChannelListByTIF(true);
				} else {
					channelList = (ArrayList<EPGChannelInfo>) mReader
							.getAllChannelList(true);
				}
				if (channelList == null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList------->channelList==null");
					return;
				}
				final ArrayList<EPGChannelInfo> tempChannelList = channelList;
				final int selectIndex = mReader.getCurrentPlayChannelPosition();
				final int pageNum = selectIndex
						/ DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelList------->selectIndex="
						+ selectIndex + ",pageNum=" + pageNum
						+ ",channelList.size=" + channelList.size());
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						iUIView.updateChannelList(tempChannelList, selectIndex,
								pageNum);
					}
				});
			}
		}).start();

	}

	private void initData() {
		mIsCurrentSourceATV=CommonIntegration.getInstance().isCurrentSourceATV();
		mIs3rdTVSource = CommonIntegration.getInstance().is3rdTVSource();
		timeType = EPGUtil.judgeFormatTime(mContext);
		mReader.loadProgramType();
		mReader.loadMonthAndWeekRes();
		String country = MtkTvConfig.getInstance().getCountry();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country=" + country);
		if (TextUtils.equals(EpgType.COUNTRY_UK, country)) {
			mIsCountryUK = true;
		} else {
			mIsCountryUK = false;
		}
	}
	
	
	@Override
	public int getTimeType(){
		return timeType;
	}

	@Override
	public void getProgramListByChId(final EPGChannelInfo channelInfo, final int dayNum,
			final int startHour) {
		iUIView.showLoading();
		new Thread(new Runnable() {

			@Override
			public void run() {
				final List<EPGProgramInfo> programList = mReader
						.getProgramListByChId(channelInfo, dayNum, startHour,
								24 - startHour);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programList.size =" + programList.size());
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						iUIView.dismissLoading();
						iUIView.updateProgramList(programList);
					}
				});
			}
		}).start();

	}

	@Override
	public void setActiveWindow(final EPGChannelInfo channel, final int dayNum,
			final int startHour) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiceWindow------->channel=[id=" + channel.mId
				+ ",name=" + channel.getName() + "],dayNum=" + dayNum
				+ ",startHour=" + startHour);
		new Thread(new Runnable() {

			@Override
			public void run() {
				mReader.setActiveWindow(channel, dayNum, startHour,
						24 - startHour);
			}
		}).start();

	}

	@Override
	public void refreshDetailsInfo(final EPGProgramInfo info,
			final int channelId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final EPGProgramInfo newInfo=regetProgramInfo(info,channelId);
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						iUIView.updateEventDetails(newInfo);
					}
				});
			}
		}).start();
	}
	
	
	
	
	private EPGProgramInfo regetProgramInfo(EPGProgramInfo info,int channelId){
		if (info == null) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "regetProgramInfo-------> info==null!");
			return info;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"regetProgramInfo------->MtkTvEventInfoBase_EventId=" + info.getProgramId()+",MtkTvChannelInfoBase.channelId="+channelId);
		if (mIsCountryUK&& info.getMainType() > mReader.getMainType().length) {
			info.setProgramType(mContext.getString(R.string.nav_epg_not_support));
		} else if (info.getMainType() >= 1) {
			info.setProgramType(mReader.getMainType()[info.getMainType() - 1]);
		} else {
			info.setProgramType(mContext.getString(R.string.nav_epg_unclassified));
		}
		String strRating=mReader.mapRating2CustomerStr(info.getRatingValue(),info.getRatingType());
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "regetProgramInfo------->strRating="+strRating);
		if(strRating!=null){
		  info.setRatingType(strRating);
		}
		return info;
	}
	
	@Override
	public boolean isCountryUK() {
		// TODO Auto-generated method stub
		return mIsCountryUK;
	}

	@Override
	public boolean isCurrentSourceATV() {
		// TODO Auto-generated method stub
		return mIsCurrentSourceATV;
	}

	@Override
	public void checkPWDShow() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
				com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "checkPWDShow------->showFlag="+showFlag);
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						iUIView.updateLockStatus(showFlag==0);
					}
				});
			}
		}).start();
		
	}

	@Override
	public void clearActiveWindow() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				MtkTvEvent.getInstance().clearActiveWindows();
			}
		}).start();
		
	}

}
