package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.Drawable;


import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGBaseAdapter;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.epg.PvrMarkRectangleImageView;

public class EPGListViewAdapter extends EPGBaseAdapter<EPGChannelInfo> {
	private static final String TAG = "EPGListViewAdpter";
	private int mStartHour;
	private int mDayNum;
//	private Handler mHandler;
	private DataReader mReader;
	private EPGListView mEPGListView;
	private List<Integer> mActiveWindowChannelIdList;
	private List<Integer> mAlreadyGetChannelIdList;
    private int mWidth;
    private int childHeight = -1;
    private List<PvrMarkRectangleImageView> pvrRects = null;

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
//        Drawable mAnalogIcon = mContext.getResources().getDrawable(R.drawable.epg_channel_icon);
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
			    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
			  }
			}
			long startTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), mDayNum, mStartHour);
			List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(group);
			TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime);
			EPGUtil.setIsNeverReceived(true);
		}
	}

	public boolean setActiveWindow() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow group>>");
		mActiveWindowChannelIdList.clear();
		mAlreadyGetChannelIdList.clear();
        if (group != null && group.size() > 0 &&
            (!CommonIntegration.getInstance().is3rdTVSource())) {
			for (EPGChannelInfo iiiii:group) {
				mActiveWindowChannelIdList.add(iiiii.getTVChannel().getChannelId());
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
			}
			long startTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), mDayNum, mStartHour);
			List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(group);
			TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime);
			EPGUtil.setIsNeverReceived(true);
			return true;
		}
        return false;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----getView----->[Position] " + position + "  " + EPGConfig.FROM_WHERE);
	  if (getCount() > 0) {
	    ViewHolder mViewHolder;
	    EPGChannelInfo mChannel = (EPGChannelInfo) getItem(position);
	    if (convertView == null) {
	      mViewHolder = new ViewHolder();
	      convertView = LayoutInflater.from(mContext).inflate(R.layout.epg_eu_listview_item_layout, null);
	      mViewHolder.number = (TextView) convertView.findViewById(R.id.epg_channel_number);
	      mViewHolder.name = (TextView) convertView.findViewById(R.id.epg_channel_name);
	      mViewHolder.icon = (ImageView) convertView.findViewById(R.id.epg_radio_icon);
	      mViewHolder.mDynamicLinearLayout = (EPGLinearLayout) convertView.findViewById(R.id.epg_program_forecast_linearlayout);
	      mViewHolder.pvrRoot = (RelativeLayout)convertView.findViewById(R.id.ll_pvrRoot);

	      if(childHeight != -1) {
	        LayoutParams layoutParams = mViewHolder.mDynamicLinearLayout.getLayoutParams();
	        layoutParams.height = childHeight;
	        mViewHolder.mDynamicLinearLayout.setLayoutParams(layoutParams);
	      }
	      mViewHolder.mDynamicLinearLayout.setWidth(mWidth);
	      convertView.setTag(mViewHolder);
	    } else {
	      mViewHolder = (ViewHolder) convertView.getTag();
	    }

        if(!CommonIntegration.getInstance().is3rdTVSource() && childHeight != -1 && mChannel != null){
            mViewHolder.pvrRoot.post(new Runnable() {
                @Override
                public void run() {
                    //get PVR mark Rectangle list
                    pvrRects = (ArrayList<PvrMarkRectangleImageView>)getRects(mChannel, mViewHolder.pvrRoot);
                    if(pvrRects != null && !pvrRects.isEmpty()){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pvrRects.size >> "+pvrRects.size());
                        for(int i = 0;i < pvrRects.size(); i++){
                            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.FILL_PARENT);
                            if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
                                itemParams.rightMargin = pvrRects.get(i).getLeftMargin();
                            }else {
                                itemParams.leftMargin = pvrRects.get(i).getLeftMargin();
                            }
                            itemParams.height = childHeight;
                            itemParams.width = pvrRects.get(i).getRectWidth();
                            pvrRects.get(i).setLayoutParams(itemParams);
                            mViewHolder.pvrRoot.addView(pvrRects.get(i), i);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----Refresh----->itemParams.leftMargin " + itemParams.leftMargin
                                    + ", itemParams.width:"+itemParams.width);
                        }
                        mViewHolder.pvrRoot.setVisibility(View.VISIBLE);
                    }else {
                        mViewHolder.pvrRoot.setVisibility(View.GONE);
                    }
                }
            });
      }
      if (mChannel != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "name: " + mChannel.getDisplayNumber());
          if (CommonIntegration.getInstance().is3rdTVSource()) {
              mViewHolder.icon.setVisibility(View.INVISIBLE);
          } else if (mChannel.getTVChannel() != null && mChannel.getTVChannel().isRadioService()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isRadioService>>"+mChannel.getTVChannel().isRadioService());
//              int length = (int)(mContext.getResources().getDimension(R.dimen.nav_chanenl_list_item_icon_widgh));
              Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.radio_channel_icon);
              radioIcon.setBounds(0, 0, radioIcon.getMinimumWidth(), radioIcon.getMinimumWidth());
              mViewHolder.icon.setImageDrawable(radioIcon);
              mViewHolder.icon.setVisibility(View.VISIBLE);
          } else {
              mViewHolder.icon.setVisibility(View.INVISIBLE);
          }
          mViewHolder.number.setCompoundDrawablePadding(10);
          mViewHolder.number.setText(String.format("   %s", mChannel.getDisplayNumber()));
          mViewHolder.name.setText(String.format("   %s", mChannel.getName()));
      }
			// The first time to enter EPG, highlight the current program
            if (EPGConfig.init && null != mChannel && mChannel.getTVChannel() != null
					&& mChannel.getTVChannel().getChannelId() == mReader.getCurrentChId()) {
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
                    mViewHolder.mDynamicLinearLayout.setBackgroundColor(mContext.getResources()
                            .getColor(R.color.epg_lv_item_bg_color_unselected));
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
          mViewHolder.mDynamicLinearLayout.setBackgroundColor(mContext.getResources().getColor(
              R.color.epg_lv_item_bg_color_unselected));
				}
			}
		}
		return convertView;
	}

	class ViewHolder {
		TextView number;
		TextView name;
        ImageView icon;
		EPGLinearLayout mDynamicLinearLayout;
        RelativeLayout pvrRoot;
	}

  public void setHeight(int height) {
    Log.d(TAG, "childHeight: " + childHeight);
    childHeight  = height;
  }

  private long convertTime(long time) {
      MtkTvTimeFormatBase from = new MtkTvTimeFormatBase();
      MtkTvTimeFormatBase to = new MtkTvTimeFormatBase();

      from.setByUtc(time);
      MtkTvTimeBase timeBase = new MtkTvTimeBase();

      timeBase.convertTime(timeBase.MTK_TV_TIME_CVT_TYPE_SYS_UTC_TO_BRDCST_LOCAL, from, to );
        return to.toMillis();
  }

    public void refreshPvrMarks(EPGChannelInfo channelInfo, RelativeLayout pvrRootView) {
        pvrRootView.post(new Runnable() {
            @Override
            public void run() {
                pvrRects = (ArrayList<PvrMarkRectangleImageView>)getRects(channelInfo,pvrRootView);
                if(pvrRects != null && !pvrRects.isEmpty()){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----Refresh----->pvrRects size: " + pvrRects.size());
                    for(int i = 0; i < pvrRects.size(); i++){
                        RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.FILL_PARENT);
                        if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
                            itemParams.rightMargin = pvrRects.get(i).getLeftMargin();
                        }else {
                            itemParams.leftMargin = pvrRects.get(i).getLeftMargin();
                        }
                        itemParams.height = childHeight;
                        itemParams.width = pvrRects.get(i).getRectWidth();
                        pvrRects.get(i).setLayoutParams(itemParams);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----Refresh----->itemParams.leftMargin "
                                + itemParams.leftMargin + ", itemParams.width:"+itemParams.width);
                        pvrRootView.addView(pvrRects.get(i),i);
                    }

                    if(pvrRects != null && !pvrRects.isEmpty()){
                        pvrRootView.setVisibility(View.VISIBLE);
                    } else {
                        pvrRootView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

  private List<PvrMarkRectangleImageView> getRects(EPGChannelInfo channelInfo, RelativeLayout pvrRootView) {
      if(pvrRects != null){
          pvrRootView.removeAllViews();
          pvrRects.clear();
      }
      pvrRects = new ArrayList<PvrMarkRectangleImageView>();

      int channelId = channelInfo.getTVChannel().getChannelId();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelId: " + channelId +", mStartHour: "+mStartHour +",mDayNum: "+mDayNum);
      long curPageStartTime = EPGTimeConvert.getInstance()
          .setDate(EPGUtil.getCurrentDateDayAsMills(), mDayNum, mStartHour);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curPageStartTime: " + curPageStartTime);

      try {
        List<MtkTvBookingBase> books = StateScheduleList.getInstance().queryItem();
        if (books != null) {
          for(MtkTvBookingBase bookItem : books){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----book item channel ID---->"+bookItem.getChannelId());
            if(bookItem.getChannelId() == channelId){
                PvrMarkRectangleImageView pvrItem = new PvrMarkRectangleImageView(mContext);
                int rectWidth = 0;
                int leftMargin = 0;

              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----111recordStartTime---->"+bookItem.getRecordStartTime());
              long recordStartTime = convertTime(bookItem.getRecordStartTime());
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----222recordStartTime---->"+recordStartTime);

              long recordEndTime = bookItem.getRecordStartTime() + bookItem.getRecordDuration();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----111recordEndTime---->"+recordEndTime);
              recordEndTime = convertTime(recordEndTime);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----222recordEndTime---->"+recordEndTime);


              //judge if pvr is daily or weekly or not.
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----repeat mode---->"+bookItem.getRepeatMode());
              if(bookItem.getRepeatMode() != 128){ //128 == once
                  //cal record start day
                  int recordDay = EPGUtil.getDayOffset(recordStartTime);
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----recordDay---->"+recordDay + ",mDayNum: "+ mDayNum);
                  if(recordDay >= 0 && recordDay < 9 && mDayNum > recordDay){
                      if(bookItem.getRepeatMode() == 0){// daily
                          recordStartTime += (mDayNum - recordDay) * 24 * 60 * 60L;
                          recordEndTime  += (mDayNum - recordDay) * 24 * 60 * 60L;

                      } else {// 0< value < 128 weekly
//                          if(recordDay < 2 || (recordDay > 6 && recordDay < 9)){
//                              if(Math.abs(mDayNum - recordDay) == 7){
//                                  recordStartTime += (mDayNum - recordDay) * 24 * 60 * 60;
//                                  recordEndTime  += (mDayNum - recordDay) * 24 * 60 * 60;
//                              }
//                          }
                          int repeatcount = bookItem.getRepeatMode();
                          List<Boolean> selectedDay = new ArrayList<Boolean>();
                          for (int i = 0; i < 7; i++) {//Sunday(0) Monday(1)....Saturday(6)
                              selectedDay.add((repeatcount & (1 << i)) == (1 << i));
                          }
                          if(mDayNum > recordDay){
                              int weekday = MtkTvTime.getInstance().getLocalTime().weekDay;
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect--weekday: " + weekday);
                              if(selectedDay.get((weekday + mDayNum)%7)){
                                  recordStartTime += (mDayNum - recordDay) * 24 * 60 * 60L;
                                  recordEndTime  += (mDayNum - recordDay) * 24 * 60 * 60L;
                              }
                          }
                      }
                  }
              }
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect--recordStartTime: " + recordStartTime + ", recordEndTime---->"+recordEndTime);

              if(recordStartTime >= curPageStartTime && recordStartTime < (curPageStartTime + EPGConfig.mTimeSpan * 60 *60)){
                  leftMargin = (int)((recordStartTime - curPageStartTime) * mWidth/(EPGConfig.mTimeSpan * 60 * 60));
                  if(leftMargin < 0){
                    leftMargin = 0;
                  }

                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----leftMargin---->"+leftMargin);
                  if(recordEndTime <= (curPageStartTime + EPGConfig.mTimeSpan * 60 *60)){
                      long duration = 0;
                      if(recordEndTime - recordStartTime < 10){
                          duration = 10;
                      } else {
                          duration = recordEndTime - recordStartTime;
                      }

                    rectWidth = (int)(duration * mWidth/ (EPGConfig.mTimeSpan * 60 * 60));
                  } else {
                      if((EPGConfig.mTimeSpan * 60 * 60 - (recordStartTime - curPageStartTime)) < 10){
                          rectWidth = 10 * mWidth/(EPGConfig.mTimeSpan * 60 * 60);
                      } else {
                          rectWidth = mWidth - (int)((recordStartTime - curPageStartTime) * mWidth/(EPGConfig.mTimeSpan * 60 * 60));
                      }
                  }
                }
                if( recordStartTime <= curPageStartTime && recordEndTime > curPageStartTime){
                  leftMargin = 0;
                  if(recordEndTime >= curPageStartTime + EPGConfig.mTimeSpan * 60 *60){
                    rectWidth = mWidth;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "111setRect-----full 2 hour---->");
                  } else {
                      long duration = 0;
                      if(recordEndTime - curPageStartTime < 10){
                          duration = 10;
                      } else {
                          duration = recordEndTime - curPageStartTime;
                      }
                    rectWidth = (int)(duration * mWidth/(EPGConfig.mTimeSpan * 60 * 60));
                  }
                }

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setRect-----rectWidth---->"+rectWidth);
              if(rectWidth > 0){
                  pvrItem.setRectWidth(rectWidth);
                  pvrItem.setLeftMargin(leftMargin);
                  pvrItem.set2Points(0, 0, rectWidth, childHeight);
                  pvrItem.setMarkColor(bookItem.getRecordMode());
                  pvrRects.add(pvrItem);
              }
            }
          }
        }
      } catch (Exception e) {
          e.printStackTrace();
      }

      return pvrRects;
    }
}
