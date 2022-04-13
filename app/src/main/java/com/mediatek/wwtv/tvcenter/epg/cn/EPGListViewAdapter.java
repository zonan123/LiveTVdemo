package com.mediatek.wwtv.tvcenter.epg.cn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;


import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGBaseAdapter;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;

public class EPGListViewAdapter extends EPGBaseAdapter<EPGChannelInfo> {
	private static final String TAG = "EPGListViewAdpter";
	private int mStartHour;
	private int mDayNum;
//	private Handler mHandler;
	private DataReader mReader;
	private EPGListView mEPGListView;
	private Drawable mAnalogIcon;
  private List<Integer> mActiveWindowChannelIdList;
  private List<Integer> mAlreadyGetChannelIdList;
	private Map<Integer, EPGChannelInfo> mUpdateHashGroup = new HashMap<Integer, EPGChannelInfo>();
	public Map<Integer, EPGChannelInfo> mHashGroup;

	public EPGListView getEPGListView() {
		return mEPGListView;
	}

	public void setEPGListView(EPGListView epgListView) {
		mEPGListView = epgListView;
	}

//	public Handler getHandler() {
//		return mHandler;
//	}
//
//	public void setHandler(Handler mHandler) {
//		this.mHandler = mHandler;
//	}

	public int getDayNum() {
		return mDayNum;
	}

	public void setDayNum(int mDayNum) {
		this.mDayNum = mDayNum;
	}

	private int mWidth;

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public int getStartHour() {
		return mStartHour;
	}

	public void setStartHour(int mStartTime) {
		this.mStartHour = mStartTime;
	}

	public boolean containsChannelId(int channelId) {
		if (mActiveWindowChannelIdList != null) {
			return mActiveWindowChannelIdList.contains(channelId);
		}
		return false;
	}
	
	public void addAlreadyChnnelId(int channelId) {
		if (mAlreadyGetChannelIdList != null && !mAlreadyGetChannelIdList.contains(channelId)) {
			for (int id:mActiveWindowChannelIdList) {
				if (mAlreadyGetChannelIdList.contains(id)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "id>>"+id);
				} else if (id == channelId) {
					mAlreadyGetChannelIdList.add(channelId);
				} else {
					return;
				}
			}
		}
	}
	
	public boolean isAlreadyGetAll() {
		if (mAlreadyGetChannelIdList != null && mActiveWindowChannelIdList != null) {
			return mAlreadyGetChannelIdList.size() >= mActiveWindowChannelIdList.size();
		}
		return true;
	}
	
	public void clearWindowList() {
		if (mActiveWindowChannelIdList != null) {
			mActiveWindowChannelIdList.clear();
		}
		if (mAlreadyGetChannelIdList != null) {
			mAlreadyGetChannelIdList.clear();
		}
	}
	
	public EPGListViewAdapter(Context mContext, int mStartTime) {
		super(mContext);
		this.mStartHour = mStartTime;
		mReader = DataReader.getInstance(mContext);
		mAnalogIcon = mContext.getResources().getDrawable(R.drawable.epg_channel_icon);
		mActiveWindowChannelIdList = new ArrayList<Integer>();
		mAlreadyGetChannelIdList = new ArrayList<Integer>();
	}

	@Override
	public void setGroup(List<EPGChannelInfo> group) {
		super.setGroup(group);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow setGroup>>");
		mActiveWindowChannelIdList.clear();
		mAlreadyGetChannelIdList.clear();
		if (group != null && !group.isEmpty()) {
			if (mHashGroup == null) {
				mHashGroup = new HashMap<Integer, EPGChannelInfo>();
			} else {
				mHashGroup.clear();
			}
			for (EPGChannelInfo iiiii:group) {
			  if(iiiii.getTVChannel() != null){
			    mHashGroup.put(iiiii.getTVChannel().getChannelId(), iiiii);
			    mActiveWindowChannelIdList.add(iiiii.getTVChannel().getChannelId());
			    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "syncActiveWindowProgram setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
			  }
			}
			long startTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), mDayNum, mStartHour);
			List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(group);
			TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime);
		}
	}
	
	public void setActiveWindow() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow group>>");
		mActiveWindowChannelIdList.clear();
		mAlreadyGetChannelIdList.clear();
		if (group != null && group.size() > 0) {
			if (mHashGroup == null) {
				mHashGroup = new HashMap<Integer, EPGChannelInfo>();
			} else {
				mHashGroup.clear();
			}
			for (EPGChannelInfo iiiii:group) {
				mActiveWindowChannelIdList.add(iiiii.getTVChannel().getChannelId());
				mHashGroup.put(iiiii.getTVChannel().getChannelId(), iiiii);
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "syncActiveWindowProgram setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
			}
			long startTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), mDayNum, mStartHour);
			List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(group);
			TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime);
		}
	}
	
	public int getIndexOfChannel(EPGChannelInfo channel) {
		return mActiveWindowChannelIdList.indexOf(channel.getTVChannel().getChannelId());
	}
	
	public void putUpdateChannel(int channelId) {
		mUpdateHashGroup.put(channelId, mHashGroup.get(channelId));
	}
	
	public void clearUpdateChannels() {
		mUpdateHashGroup.clear();
	}
	
	public List<EPGChannelInfo> getActivewindowChannels() {
		List<EPGChannelInfo> channels = new ArrayList<EPGChannelInfo>();
		channels = group;
//		List<Integer> keys = new ArrayList<Integer>();
//		Iterator it = mUpdateHashGroup.keySet().iterator();
//    	boolean hasFind = false;
//        while(it.hasNext()){
//            int key = Integer.parseInt(it.next().toString());
//            channels.add(mUpdateHashGroup.get(key));
//            keys.add(key);
//		}
//        for (Integer key:keys) {
//        	mUpdateHashGroup.remove(key);
//		}
        return channels;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "-----getView----->[Position] " + position + "  " + EPGConfig.FROM_WHERE);
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.epg_cn_listview_item_layout, null);
			mViewHolder.number = (TextView) convertView.findViewById(R.id.epg_channel_number);
			mViewHolder.name = (TextView) convertView.findViewById(R.id.epg_channel_name);
			mViewHolder.mDynamicLinearLayout = (EPGLinearLayout) convertView.findViewById(R.id.epg_program_forecast_linearlayout);
			mViewHolder.mDynamicLinearLayout.setWidth(mWidth);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		if (getCount() > 0) {
			// mViewHolder.mDynamicLinearLayout.requestLayout();
			EPGChannelInfo mChannel = (EPGChannelInfo) getItem(position);
			if (mChannel != null) {
        boolean isRadioService = mChannel.getTVChannel().isRadioService();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isRadioService>>" + isRadioService);
				if (isRadioService) {
					Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.epg_radio_channel_icon);
					radioIcon.setBounds(0, 0, mAnalogIcon.getMinimumWidth(), mAnalogIcon.getMinimumWidth());
					mViewHolder.number.setCompoundDrawables(radioIcon, null, null, null);
				} else if (mChannel.getTVChannel() instanceof MtkTvAnalogChannelInfo) {
					Drawable analogIcon = mContext.getResources().getDrawable(R.drawable.epg_channel_icon);
					analogIcon.setBounds(0, 0, analogIcon.getMinimumWidth(), analogIcon.getMinimumWidth());
					mViewHolder.number.setCompoundDrawables(analogIcon, null, null, null);
				} else {
					Drawable nothingIcon = mContext.getResources().getDrawable(R.drawable.translucent_background);
					nothingIcon.setBounds(0, 0, mAnalogIcon.getMinimumWidth(), mAnalogIcon.getMinimumWidth());
					mViewHolder.number.setCompoundDrawables(nothingIcon, null, null, null);
				}
				mViewHolder.number.setCompoundDrawablePadding(10);
				mViewHolder.number.setText(String.format("   %s", mChannel.getDisplayNumber()));
				mViewHolder.name.setText(String.format("   %s", mChannel.getName()));
			}
			// The first time to enter EPG, highlight the current program
			if (null != mChannel && mChannel.getTVChannel().getChannelId() == mReader.getCurrentChId()
					&& EPGConfig.init) {
				if (mChannel.getmTVProgramInfoList() != null && mChannel.getmTVProgramInfoList().size() > 0) {
					int index = mChannel.getPlayingTVProgramPositon();
					mViewHolder.mDynamicLinearLayout.setmCurrentSelectPosition(index);
					List<EPGProgramInfo> mChildViewData = mChannel.getmTVProgramInfoList();
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---- getView EPGConfig.init == true----->[Playing TVProgram Position] " + index + ">>"+ mChildViewData.size());
					mViewHolder.mDynamicLinearLayout.setBackground(null);
					if (EPGConfig.SELECTED_CHANNEL_POSITION != position) {
						mViewHolder.mDynamicLinearLayout.setAdpter(mChildViewData, false);
					} else {
						mViewHolder.mDynamicLinearLayout.setAdpter(mChildViewData, true);
					}
				} else {
					mViewHolder.mDynamicLinearLayout.removeAllViews();
					mViewHolder.mDynamicLinearLayout.setBackgroundResource(R.drawable.epg_analog_channel_bg);
				}
			} else {
				// for digital TV
				if (null != mChannel && null != mChannel.getmTVProgramInfoList() 
						&& mChannel.getmTVProgramInfoList().size() > 0) {
					List<EPGProgramInfo> mChildViewData = mChannel.getmTVProgramInfoList();
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---- getView----->[Child View Size] " + mChildViewData.size() + ">>" + EPGConfig.FROM_WHERE);
					mViewHolder.mDynamicLinearLayout.setBackground(null);
					mViewHolder.mDynamicLinearLayout.setAdpter(mChildViewData, false);
					if (position == EPGConfig.SELECTED_CHANNEL_POSITION) {
						if (EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_DPAD_LEFT) {
							mViewHolder.mDynamicLinearLayout.setSelectedPosition(mChildViewData.size() - 1);
						} else if (EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_DPAD_RIGHT) {
							mViewHolder.mDynamicLinearLayout.setSelectedPosition(0);
						} else if (EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_MTKIR_GREEN
								|| EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_MTKIR_RED) {
							mViewHolder.mDynamicLinearLayout.setSelectedPosition(0);
						} else if (EPGConfig.FROM_WHERE == EPGConfig.FROM_ANOTHER_STREAM) {
							int index = mChannel.getNextPosition(mEPGListView.getLastSelectedTVProgram());
							mViewHolder.mDynamicLinearLayout.setSelectedPosition(index);
						}else if(EPGConfig.FROM_WHERE == EPGConfig.AVOID_PROGRAM_FOCUS_CHANGE){
							int index = mChannel.getPlayingTVProgramPositon();
							mViewHolder.mDynamicLinearLayout.setSelectedPosition(index);
						}else if(EPGConfig.FROM_WHERE == EPGConfig.SET_LAST_POSITION){
							int index = mViewHolder.mDynamicLinearLayout.getmCurrentSelectPosition();
							if (index < 0) {
								index = 0;
							} else if (index >= mChildViewData.size()) {
								index = mChildViewData.size() - 1;
							}
							mViewHolder.mDynamicLinearLayout.setSelectedPosition(index);
						}
					}
				} else {
					mViewHolder.mDynamicLinearLayout.removeAllViews();
					mViewHolder.mDynamicLinearLayout.setBackgroundResource(R.drawable.epg_analog_channel_bg);
				}
			}
		}
		return convertView;
	}

	class ViewHolder {
		TextView number;
		TextView name;
		EPGLinearLayout mDynamicLinearLayout;
	}

}
