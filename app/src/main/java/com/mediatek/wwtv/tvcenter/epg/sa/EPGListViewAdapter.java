package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGBaseAdapter;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

public class EPGListViewAdapter extends EPGBaseAdapter<EPGChannelInfo> {
	private static final String TAG = "EPGListViewAdpter";
	private int mStartHour;
	private int mDayNum;
	private Handler mHandler;
	private DataReader mReader;
	private EPGListView mEPGListView;
	private Drawable mAnalogIcon;
	private List<Integer> mActiveWindowChannelIdList;
  private List<Integer> mAlreadyGetChannelIdList;

	public EPGListView getEPGListView() {
		return mEPGListView;
	}

	public void setEPGListView(EPGListView epgListView) {
		mEPGListView = epgListView;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public int getDayNum() {
		return mDayNum;
	}

	public void setDayNum(int mDayNum) {
		this.mDayNum = mDayNum;
	}

	private int mWidth;
	private int childHeight = -1;

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
			mAlreadyGetChannelIdList.add(channelId);
		}
	}

	public boolean isAlreadyGetAll() {
		if (mAlreadyGetChannelIdList != null && mActiveWindowChannelIdList != null) {
			return mAlreadyGetChannelIdList.size() >= mActiveWindowChannelIdList.size();
		}
		return true;
	}

	  public int getChannelCount() {
          if (mAlreadyGetChannelIdList != null) {
               return mAlreadyGetChannelIdList.size();
    	  }
    	  return 0;
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
    if (group != null && !group.isEmpty() &&
        (!CommonIntegration.getInstance().is3rdTVSource())) {
			for (EPGChannelInfo iiiii:group) {
			  if(iiiii.getTVChannel() != null){
			    mActiveWindowChannelIdList.add(iiiii.getTVChannel().getChannelId());
			    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
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
    if (group != null && group.size() > 0 &&
        (!CommonIntegration.getInstance().is3rdTVSource())) {
			for (EPGChannelInfo iiiii:group) {
				mActiveWindowChannelIdList.add(iiiii.getTVChannel().getChannelId());
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
			}
			long startTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), mDayNum, mStartHour);
			List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(group);
			TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime);
		}
	}

	@SuppressLint("NewApi")
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----getView----->[Position] " + position + "   " + EPGConfig.init);
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.epg_sa_listview_item_layout, null);
			mViewHolder.number = (TextView) convertView.findViewById(R.id.epg_channel_number);
			mViewHolder.name = (TextView) convertView.findViewById(R.id.epg_channel_name);
			mViewHolder.icon = (ImageView) convertView.findViewById(R.id.epg_radio_icon);
			mViewHolder.mDynamicLinearLayout = (EPGLinearLayout) convertView.findViewById(R.id.epg_program_forecast_linearlayout);
			if(childHeight != -1) {
			  LayoutParams layoutParams = mViewHolder.mDynamicLinearLayout.getLayoutParams();
	      layoutParams.height = childHeight;
	      mViewHolder.mDynamicLinearLayout.setLayoutParams(layoutParams);
			}
			mViewHolder.mDynamicLinearLayout.setWidth(mWidth);
			com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setAdpter-----layoutParams.width--getview-->"+mWidth);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		EPGChannelInfo mChannel = (EPGChannelInfo) getItem(position);
        if (mChannel == null) {
            return convertView;
        }
        mViewHolder.icon.setVisibility(View.INVISIBLE);
        if (CommonIntegration.getInstance().is3rdTVSource()) {
            mViewHolder.icon.setVisibility(View.INVISIBLE);
        } else if (mChannel.getTVChannel() != null
                && mChannel.getTVChannel() instanceof MtkTvISDBChannelInfo) {
                Drawable isdbIcon = mChannel.getIsdbIcon();
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setAdpter-----isdbIcon-->"+isdbIcon);
                if (isdbIcon != null) {
                    isdbIcon.setBounds(0, 0, mAnalogIcon.getMinimumWidth(),
                            (int)((float)mAnalogIcon.getMinimumWidth()/isdbIcon.getMinimumWidth()*isdbIcon.getMinimumHeight()));
                    mViewHolder.icon.setImageDrawable(isdbIcon);
                    mViewHolder.icon.setVisibility(View.VISIBLE);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setAdpter-----isdbIcon-->");
            }
        }
			com.mediatek.wwtv.tvcenter.util.MtkLog.e("listadapter", "getview:"+ mChannel.getmChanelNumString()+"."+mChannel.getmSubNum()
					+ "   " + mChannel.getName());
//			if (""==mChannel.getmSubNum()) {
//				mViewHolder.name.setText("  " + mChannel.getmChanelNumString()+mChannel.getmSubNum()
//						+ "   " + mChannel.getName());
//			}else {
				mViewHolder.number.setText(String.format("  %s", mChannel.getDisplayNumber()));
				mViewHolder.name.setText(String.format("   %s", mChannel.getName()));
//			}
			List<EPGProgramInfo> mChildViewData = mChannel.getmTVProgramInfoList();
      if (mChannel.getTVChannel() != null
          && mChannel.getTVChannel().getChannelId() == mReader.getCurrentChId()
          && EPGConfig.init) {
				if (mChildViewData != null && !mChildViewData.isEmpty()) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "---- getView--------------------------1--->"+mChildViewData.size());
					int index = mChannel.getPlayingTVProgramPositon();
					mViewHolder.mDynamicLinearLayout.setmCurrentSelectPosition(index);
					mViewHolder.mDynamicLinearLayout.setBackground(null);
					if (EPGConfig.SELECTED_CHANNEL_POSITION != position) {
						mViewHolder.mDynamicLinearLayout.setAdapterByEpgProgramItemView(mChildViewData, false);
					} else {
						mViewHolder.mDynamicLinearLayout.setAdapterByEpgProgramItemView(mChildViewData, true);
					}
				} else {
					mViewHolder.mDynamicLinearLayout.setBackgroundResource(R.drawable.epg_analog_channel_bg);
					mViewHolder.mDynamicLinearLayout.setAdpterByLayout(mStartHour, mDayNum);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----layoutParams.leftMargin----channel_bg> true");
				}
			} else {
				if (null != mChildViewData && !mChildViewData.isEmpty()) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---- getView---2------>[Child View Size] " + mChildViewData.size() + ">>" + EPGConfig.FROM_WHERE);
					mViewHolder.mDynamicLinearLayout.setBackground(null);
					mViewHolder.mDynamicLinearLayout.setAdapterByEpgProgramItemView(mChildViewData, false);
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
						} else if(EPGConfig.FROM_WHERE == EPGConfig.SET_LAST_POSITION){
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
					mViewHolder.mDynamicLinearLayout.setBackgroundResource(R.drawable.epg_analog_channel_bg);
					mViewHolder.mDynamicLinearLayout.setAdpterByLayout(mStartHour, mDayNum);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----layoutParams.leftMargin----channel_bg> false");
				}
		}
		return convertView;
	}

	class ViewHolder {
		TextView number;
		TextView name;
        ImageView icon;
		EPGLinearLayout mDynamicLinearLayout;
	}

  public void setHeight(int height) {
    childHeight = height;
  }

}
