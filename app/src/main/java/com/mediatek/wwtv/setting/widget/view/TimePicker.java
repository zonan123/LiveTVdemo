/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.wwtv.setting.widget.view;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.mediatek.twoworlds.tv.MtkTvTimeFormat;

import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;

import com.mediatek.wwtv.tvcenter.util.SaveValue;

public class TimePicker extends Picker {

    private static final String EXTRA_24H_FORMAT = "24h_format";
    private static final String EXTRA_DEFAULT_TO_CURRENT = "delault_to_current";

    private static final int HOURS_IN_HALF_DAY = 12;

    private boolean mIs24hFormat = false;
    private boolean mPendingTime = false;
    private int mInitHour;
    private int mInitMinute;
    private boolean mInitIsPm;
    private Context context;

    // Column order varies by locale
    private static class ColumnOrder {
        int mHours = 0;
        int mMinutes = 1;
        int mAmPm = 2;
    }
    private ColumnOrder mColumnOrder = new ColumnOrder();

    public static TimePicker newInstance() {
        return newInstance(true, true);
    }

    public static TimePicker newInstance(boolean is24hFormat, boolean defaultToCurrentTime) {
        TimePicker picker = new TimePicker();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_24H_FORMAT, is24hFormat);
        args.putBoolean(EXTRA_DEFAULT_TO_CURRENT, defaultToCurrentTime);
        picker.setArguments(args);
        return picker;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
        	mItemId = bundle.getCharSequence(PreferenceUtil.PARENT_PREFERENCE_ID).toString();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mItemId:" + mItemId);
        mIs24hFormat = getArguments().getBoolean(EXTRA_24H_FORMAT, false);
        boolean useCurrent = getArguments().getBoolean(EXTRA_DEFAULT_TO_CURRENT, false);
        if(!TextUtils.isEmpty(mItemId)
        		&& (mItemId.contains(MenuConfigManager.TIME_START_TIME)
        		|| mItemId.contains(MenuConfigManager.TIME_END_TIME))){
            mIs24hFormat = true;
            useCurrent = true;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "useCurrent:" + useCurrent);
        if(!TextUtils.isEmpty(mItemId) && mItemId.length() > MenuConfigManager.TIME_END_TIME.length()){
        	if(mItemId.contains(MenuConfigManager.TIME_START_TIME)){
        		channelId = mItemId.substring(16);
        	} else {
				channelId = mItemId.substring(14);
			}
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "channelId:" + channelId);
        
        if (useCurrent) {
            mPendingTime = true;
            //Calendar cal = Calendar.getInstance();   
            String onTime = "00:00:00";
            if(mItemId.equals(MenuConfigManager.TIMER1)){
            	onTime = SaveValue.getInstance(context).readStrValue(MenuConfigManager.TIMER1);
            }else if(mItemId.equals(MenuConfigManager.TIMER2)){
            	onTime = SaveValue.getInstance(context).readStrValue(MenuConfigManager.TIMER2);
            }else if(mItemId.equals(MenuConfigManager.TIME_TIME)){
            	SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            	onTime = sdf2.format(new Date());
            }else if(mItemId.contains(MenuConfigManager.TIME_START_TIME)
            		|| mItemId.contains(MenuConfigManager.TIME_END_TIME)){
            	onTime = SaveValue.getInstance(context).readStrValue(mItemId);
            }
            if(onTime == null){
            	onTime = "00:00:00";
            }
            String [] strs = onTime.split(":");
            mInitHour = Integer.valueOf(strs[0]);//cal.get(Calendar.HOUR_OF_DAY);
            

            if (!mIs24hFormat) {
                if (mInitHour >= HOURS_IN_HALF_DAY) {
                    // PM case, valid hours: 12-23
                    mInitIsPm = true;
                    if (mInitHour > HOURS_IN_HALF_DAY) {
                        mInitHour = mInitHour - HOURS_IN_HALF_DAY-1;
                    }
                } else {
                    // AM case, valid hours: 0-11
                    mInitIsPm = false;
                    if (mInitHour == 0) {
                    	mInitHour = 0;
                        //mInitHour = HOURS_IN_HALF_DAY;
                    }
                }
            }

            mInitMinute = Integer.valueOf(strs[1]);//cal.get(Calendar.MINUTE);
        }

        Locale locale = Locale.getDefault();
        String hmaPattern = DateFormat.getBestDateTimePattern(locale, "hma");
        boolean isAmPmAtEnd = hmaPattern.indexOf("a") > hmaPattern.indexOf("m");
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
        // Note ordering of calculation below is important
        if (isRtl) {
            // Hours and minutes are always shown with hours on the left
            mColumnOrder.mHours = 1;
            mColumnOrder.mMinutes = 0;
        }
        if (!isAmPmAtEnd && !mIs24hFormat) {
            // Rotate AM/PM indicator to front, if visible
            mColumnOrder.mAmPm = 0;
            mColumnOrder.mHours++;
            mColumnOrder.mMinutes++;
        }
    }

    @Override
    public void onResume() {
        if (mPendingTime) {
            mPendingTime = false;
            setTime(mInitHour, mInitMinute, mInitIsPm);
        }
        super.onResume();
    }

    protected boolean setTime(int hour, int minute, boolean isPm) {
        if (minute < 0 || minute > 59) {
            return false;
        }

        if (mIs24hFormat) {
            if (hour < 0 || hour > 23) {
                return false;
            }
        } else {
            if (hour < 1 || hour > 12) {
                return false;
            }
        }

        updateSelection(mColumnOrder.mHours, mIs24hFormat ? hour : hour - 1);
        updateSelection(mColumnOrder.mMinutes, minute);
        if (!mIs24hFormat) {
            updateSelection(mColumnOrder.mAmPm, isPm ? 1 : 0);
        }

        return true;
    }

    @Override
    protected ArrayList<PickerColumn> getColumns() {
        ArrayList<PickerColumn> ret = new ArrayList<PickerColumn>();
        // Fill output with nulls, then place actual columns according to order
        int capacity = mIs24hFormat ? 2 : 3;
        for (int i = 0; i < capacity; i++) {
            ret.add(null);
        }
        PickerColumn hours = new PickerColumn(mIs24hFormat ? mConstant.hours24 : mConstant.hours12);
        PickerColumn minutes = new PickerColumn(mConstant.minutes);
        ret.set(mColumnOrder.mHours, hours);
        ret.set(mColumnOrder.mMinutes, minutes);

        if (!mIs24hFormat) {
            PickerColumn ampm = new PickerColumn(mConstant.ampm);
            ret.set(mColumnOrder.mAmPm, ampm);
        }
        return ret;
    }

    @Override
    protected String getSeparator() {
        return mConstant.timeSeparator;
    }

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
    
	@Override
	protected void recordResult(List<String> mResult) {
		super.recordResult(mResult);
		if(!TextUtils.isEmpty(mItemId)
        		&& (mItemId.contains(MenuConfigManager.TIME_START_TIME)
        		|| mItemId.contains(MenuConfigManager.TIME_END_TIME))){
			int hour = Integer.parseInt(mResult.get(0));
			int minute = Integer.parseInt(mResult.get(1));
			String hours1 = "";
			String minutes1 = "";
			if (hour < 10) {
				hours1 = "0" + hour;
			} else {
				hours1 += hour;
			}
			if (minute < 10) {
				minutes1 = "0" + minute;
			} else {
				minutes1 += minute;
			}
			String tempTime = hours1 + ":" + minutes1;
			SaveValue.getInstance(context).saveStrValue(
					mItemId, tempTime);
			saveShceduleDateTime();
//        goBack();
		}
        mBackStack.accept(this);
  }

	private void saveShceduleDateTime() {

	    String[][] twoDateTimeArrays = compareDateTime();

		 int year1 = Integer.parseInt(twoDateTimeArrays[0][0].substring(0, 4));// start year
		 int month1 = Integer.parseInt(twoDateTimeArrays[0][0].substring(5, 7));// start month
		 int day1 = Integer.parseInt(twoDateTimeArrays[0][0].substring(8));// start date
		 int hour1 = Integer.parseInt(twoDateTimeArrays[0][1].substring(0, 2));
		 int minute1 = Integer.parseInt(twoDateTimeArrays[0][1].substring(3, 5));
		 int second1 = 0;// Integer.parseInt(timeString.substring(6));
		 
		 int year2 = Integer.parseInt(twoDateTimeArrays[1][0].substring(0, 4));// start year
		 int month2 = Integer.parseInt(twoDateTimeArrays[1][0].substring(5, 7));// start month
		 int day2 = Integer.parseInt(twoDateTimeArrays[1][0].substring(8));// start date
		 int hour2 = Integer.parseInt(twoDateTimeArrays[1][1].substring(0, 2));
		 int minute2 = Integer.parseInt(twoDateTimeArrays[1][1].substring(3, 5));
		 int second2 = 0;// Integer.parseInt(timeString.substring(6));

	    MtkTvTimeFormat.getInstance().set(second1, minute1, hour1, day1, month1 - 1, year1);
	    EditChannel.getInstance(context).setSchBlockFromUTCTime(Integer.valueOf(channelId),
	        MtkTvTimeFormat.getInstance().toMillis());

	    MtkTvTimeFormat.getInstance().set(second2, minute2, hour2, day2, month2 - 1, year2);
	    EditChannel.getInstance(context).setSchBlockTOUTCTime(Integer.valueOf(channelId),
	        MtkTvTimeFormat.getInstance().toMillis());
	  
		
	}

	private synchronized String[][] compareDateTime() {
	    String dateStart = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_START_DATE + channelId);
	    String dateEnd = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_END_DATE + channelId);
	    String timeStart = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_START_TIME + channelId);
	    String timeEnd = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_END_TIME + channelId);
	    Log.d(TAG, "compareDateTime:dstart:" + dateStart + ",dend:" + dateEnd + ",tstart:" + timeStart
	        + ",tend:" + timeEnd);
	    Log.d(TAG, "mItemId="+mItemId);
	    if (mItemId.startsWith(MenuConfigManager.TIME_END_TIME)) {
	      if (dateStart.compareTo(dateEnd)==0&&timeStart.compareTo(timeEnd) > 0) {// starttime > endtime
	        String[] tarray = timeStart.split(":");
	        int nHour = Integer.parseInt(tarray[0]);
	        if (nHour == 23) {// special
	          timeEnd = timeStart;
	        } else if (nHour > 0 && nHour < 9) {
	          timeEnd = "0" + (nHour + 1) + ":" + tarray[1];
	        } else {
	          timeEnd = (nHour + 1) + ":" + tarray[1];
	        }
	      }
	      SaveValue.getInstance(context).saveStrValue(
	          MenuConfigManager.TIME_END_TIME + channelId, timeEnd);
	      Log.d(TAG, "mResultListener="+mResultListener);
	      if(mResultListener!=null){
	    	  mResultListener.onCommitResult(timeEnd); 
	      }
		    	
	    } else if (mItemId.startsWith(MenuConfigManager.TIME_START_TIME)) {
	      if (dateStart.compareTo(dateEnd)==0&&timeStart.compareTo(timeEnd) > 0) {// starttime>endtime
	        String[] tarray = timeEnd.split(":");
	        int nHour = Integer.parseInt(tarray[0]);
	        if (nHour == 0) {// special
	          timeStart = timeEnd;
	        } else if (nHour > 0 && nHour <= 10) {
	          timeStart = "0" + (nHour - 1) + ":" + tarray[1];
	        } else {
	          timeStart = (nHour - 1) + ":" + tarray[1];
	        }
	      }
	      SaveValue.getInstance(context).saveStrValue(
	          MenuConfigManager.TIME_START_TIME + channelId, timeStart);
	      Log.d(TAG, "mResultListener="+mResultListener);
	      if(mResultListener!=null){
	    	  mResultListener.onCommitResult(timeStart);
	      }
		    	
	    }
	    Log.d(TAG, "compareDateTime222:dstart:" + dateStart + ",dend:" + dateEnd + ",tstart:"
	        + timeStart + ",tend:" + timeEnd);
      return new String[][] {
          {
            dateStart, timeStart
          }, {
            dateEnd, timeEnd
          }
      };
    }

}
