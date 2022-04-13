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
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
//import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.widget.TextView;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.mediatek.twoworlds.tv.MtkTvTimeFormat;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.DateFormatUtil;
import com.mediatek.wwtv.tvcenter.util.SaveValue;


import android.util.Log;



public class DatePicker extends Picker {

    private static final String EXTRA_START_YEAR = "start_year";
    private static final String EXTRA_YEAR_RANGE = "year_range";
    private static final String EXTRA_DEFAULT_TO_CURRENT = "default_to_current";
    private static final String EXTRA_FORMAT = "date_format";
    private static final int DEFAULT_YEAR_RANGE = 24;
    private static final int DEFAULT_START_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private String[] mYears;
    private int mStartYear;
    private int mYearRange;
    private String[] mDayString = null;
    private int mColMonthIndex = 0;
    private int mColDayIndex = 1;
    private int mColYearIndex = 2;

    private boolean mPendingDate = false;
    private int mInitYear;
    private int mInitMonth;
    private int mInitDay;
    
    private int mSelectedYear = DEFAULT_START_YEAR;
    private String mSelectedMonth;
	Context context;

    public static DatePicker newInstance() {
        return newInstance("");
    }

    /**
     * Creates a new instance of DatePicker
     *
     * @param format         String containing a permutation of Y, M and D, indicating the order
     *                       of the fields Year, Month and Day to be displayed in the DatePicker.
     */
    public static DatePicker newInstance(String format) {
        return newInstance(format, DEFAULT_START_YEAR);
    }

    /**
     * Creates a new instance of DatePicker
     *
     * @param format         String containing a permutation of Y, M and D, indicating the order
     *                       of the fields Year, Month and Day to be displayed in the DatePicker.
     * @param startYear      The lowest number to be displayed in the Year selector.
     */
    public static DatePicker newInstance(String format, int startYear) {
        return newInstance(format, startYear, DEFAULT_YEAR_RANGE, true);
    }

    /**
     * Creates a new instance of DatePicker
     *
     * @param format         String containing a permutation of Y, M and D, indicating the order
     *                       of the fields Year, Month and Day to be displayed in the DatePicker.
     * @param startYear      The lowest number to be displayed in the Year selector.
     * @param yearRange      Number of entries to be displayed in the Year selector.
     * @param startOnToday   Indicates if the date should be set to the current date by default.
     */
    public static DatePicker newInstance(String format, int startYear, int yearRange,
            boolean startOnToday) {
        DatePicker datePicker = new DatePicker();
        if (startYear <= 0) {
            throw new IllegalArgumentException("The start year must be > 0. Got " + startYear);
        }
        if (yearRange <= 0) {
            throw new IllegalArgumentException("The year range must be > 0. Got " + yearRange);
        }
        Bundle args = new Bundle();
        args.putString(EXTRA_FORMAT, format);
        args.putInt(EXTRA_START_YEAR, startYear);
        Log.d("chuanfei", "startYear"+startYear);
        args.putInt(EXTRA_YEAR_RANGE, yearRange);
        args.putBoolean(EXTRA_DEFAULT_TO_CURRENT, startOnToday);
        datePicker.setArguments(args);
        return datePicker;
    }

    private void initYearsArray(int startYear, int yearRange) {
        mYears = new String[yearRange];
        for (int i = 0; i < yearRange; i++) {
            mYears[i] = String.valueOf(startYear + i);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        Bundle bundle = getArguments();
        if(bundle != null){
        	mItemId = bundle.getCharSequence(PreferenceUtil.PARENT_PREFERENCE_ID).toString();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mItemId:" + mItemId);
        if(!TextUtils.isEmpty(mItemId) && mItemId.length() > MenuConfigManager.TIME_END_DATE.length()){
        	if(mItemId.contains(MenuConfigManager.TIME_START_DATE)){
        		channelId = mItemId.substring(16);
        	} else {
				channelId = mItemId.substring(14);
			}
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelId:" + channelId);
        Calendar calendar = Calendar.getInstance();
        int mDefYear = calendar.get(Calendar.YEAR);
        if (mDefYear > (1970 + 15)) {
          mDefYear -= 15;
        }
        if(mItemId.contains("date")){
        	newInstance(new String(DateFormat.getDateFormatOrder(context)), mDefYear, 36, false);
        }
        
        mStartYear = getArguments().getInt(EXTRA_START_YEAR, DEFAULT_START_YEAR);
        mYearRange = getArguments().getInt(EXTRA_YEAR_RANGE, DEFAULT_YEAR_RANGE);
        boolean startOnToday = getArguments().getBoolean(EXTRA_DEFAULT_TO_CURRENT, false);
        mSelectedMonth = mConstant.months[0];
        initYearsArray(mStartYear, mYearRange);

        mDayString = mConstant.days30;

        String sFormat = getArguments().getString(EXTRA_FORMAT);
        if (sFormat != null && !sFormat.isEmpty()) {
            String format = sFormat.toUpperCase(Locale.ROOT);

            int yIndex = format.indexOf('Y');
            int mIndex = format.indexOf('M');
            int dIndex = format.indexOf('D');
            if (yIndex < 0 || mIndex < 0 || dIndex < 0 || yIndex > 2 || mIndex > 2 || dIndex > 2) {
                // Badly formatted input. Use default order.
                mColMonthIndex = 0;
                mColDayIndex = 1;
                mColYearIndex = 2;
            } else {
                mColMonthIndex = mIndex;
                mColDayIndex = dIndex;
                mColYearIndex = yIndex;
            }
        }
        if (startOnToday) {
            mPendingDate = true;
            Calendar cal = Calendar.getInstance();
            mInitYear = cal.get(Calendar.YEAR);
            mInitMonth = cal.get(Calendar.MONTH);
            mInitDay = cal.get(Calendar.DATE);
        }else{
        	mPendingDate = false;
        	String selectDate = SaveValue.getInstance(context).readStrValue(mItemId);
//            if(mItemId.equals(MenuConfigManager.TIME_START_DATE )){
//                selectDate = SaveValue.getInstance(context).readStrValue(MenuConfigManager.TIME_START_DATE + mParentId);//+mAction.mInitValue
//            }else if(mItemId.equals(MenuConfigManager.TIME_END_DATE)){
//                selectDate = SaveValue.getInstance(context).readStrValue(MenuConfigManager.TIME_END_DATE + mParentId);//+mAction.mInitValue
//            }
			Log.d("DatePicker","selectDate=="+selectDate);
        	if(!TextUtils.isEmpty(selectDate)){
        		String[] dates =  selectDate.split("/");
        		if(dates != null && dates.length == 3){
        			mInitYear = Integer.parseInt(dates[0]);
        			mInitMonth = Integer.parseInt(dates[1])-1;
        			mInitDay = Integer.parseInt(dates[2]);

					Log.d("DatePicker","mInitYear=="+mInitYear+",mInitMonth=="+mInitMonth+",mInitDay=="+mInitDay);
        		}
        	}
        	
        }
    }

    @Override
    public void onResume() {
        if (mPendingDate) {
            mPendingDate = false;
            setDate(mInitYear, mInitMonth, mInitDay);
        }
        super.onResume();
    }

    @Override
    protected ArrayList<PickerColumn> getColumns() {
        ArrayList<PickerColumn> ret = new ArrayList<PickerColumn>();
        // TODO orders of these columns might need to be localized
        PickerColumn months = new PickerColumn(mConstant.months);
        PickerColumn days = new PickerColumn(mDayString);
        PickerColumn years = new PickerColumn(mYears);

        for (int i = 0; i < 3; i++) {
            if (i == mColYearIndex) {
                ret.add(years);
            } else if (i == mColMonthIndex) {
                ret.add(months);
            } else if (i == mColDayIndex) {
                ret.add(days);
            }
        }

        return ret;
    }

	@Override
    protected String getSeparator() {
        return mConstant.dateSeparator;
    }

    protected boolean setDate(int year, int month, int day) {
        boolean isLeapYear = false;

        if (year < mStartYear || year > (mStartYear + mYearRange)) {
            return false;
        }

        // Test to see if this is a valid date
        try {
            GregorianCalendar cal = new GregorianCalendar(year, month, day);
            cal.setLenient(false);
            cal.getTime();
        } catch (IllegalArgumentException e) {
            return false;
        }

        mSelectedYear = year;
        mSelectedMonth = mConstant.months[month];

        updateSelection(mColYearIndex, year - mStartYear);
        updateSelection(mColMonthIndex, month);

        String[] dayString = null;
        // This is according to http://en.wikipedia.org/wiki/Leap_year#Algorithm
        if (year % 400 == 0) {
            isLeapYear = true;
        } else if (year % 100 == 0) {
            isLeapYear = false;
        } else if (year % 4 == 0) {
            isLeapYear = true;
        }

        if (month == 1) {
            if (isLeapYear) {
                dayString = mConstant.days29;
            } else {
                dayString = mConstant.days28;
            }
        } else if (month == 3 || month == 5 || month == 8 || month == 10) {
            dayString = mConstant.days30;
        } else {
            dayString = mConstant.days31;
        }

        if (mDayString != dayString) {
            mDayString = dayString;
            updateAdapter(mColDayIndex, new PickerColumn(mDayString));
        }

        updateSelection(mColDayIndex, day - 1);
        return true;
    }

    @Override
    public void onScroll(View v) {
        int column = (Integer) v.getTag();
        String text = ((TextView) v).getText().toString();
        if (column == mColMonthIndex) {
            mSelectedMonth = text;
        } else if (column == mColYearIndex) {
            mSelectedYear = Integer.parseInt(text);
        } else {
            return;
        }

        String[] dayString = null;

        boolean isLeapYear = false;
        // This is according to http://en.wikipedia.org/wiki/Leap_year#Algorithm
        if (mSelectedYear % 400 == 0) {
            isLeapYear = true;
        } else if (mSelectedYear % 100 == 0) {
            isLeapYear = false;
        } else if (mSelectedYear % 4 == 0) {
            isLeapYear = true;
        }
        if (mSelectedMonth.equals(mConstant.months[1])) {
            if (isLeapYear) {
                dayString = mConstant.days29;
            } else {
                dayString = mConstant.days28;
            }
        } else if (mSelectedMonth.equals(mConstant.months[3])
                || mSelectedMonth.equals(mConstant.months[5])
                || mSelectedMonth.equals(mConstant.months[8])
                || mSelectedMonth.equals(mConstant.months[10])) {
            dayString = mConstant.days30;
        } else {
            dayString = mConstant.days31;
        }
        if (!mDayString.equals(dayString)) {
            mDayString = dayString;
            updateAdapter(mColDayIndex, new PickerColumn(mDayString));
        }
    }
    
    @Override
    protected void recordResult(List<String> mResult){
    	super.recordResult(mResult);
    	if(mItemId.contains("date")){
            Log.d(TAG, "recordResult:" + mResult.get(0) + "," + mResult.get(1));
            String format = new String(
                DateFormat.getDateFormatOrder(context));
            String formatOrder = format.toUpperCase(Locale.ROOT);
            int yIndex = formatOrder.indexOf('Y');
            int mIndex = formatOrder.indexOf('M');
            int dIndex = formatOrder.indexOf('D');
            if (yIndex < 0 || mIndex < 0 || dIndex < 0 ||
                yIndex > 2 || mIndex > 2 || dIndex > 2) {
              // Badly formatted input. Use default order.
              mIndex = 0;
              dIndex = 1;
              yIndex = 2;
            }

            String month = mResult.get(mIndex);
            int day = Integer.parseInt(mResult.get(dIndex));
            int year = Integer.parseInt(mResult.get(yIndex));
            int monthInt = 0;
            String[] months = PickerConstant.getInstance(context.getResources()).months;
            int totalMonths = months.length;
            for (int i = 0; i < totalMonths; i++) {
              if (months[i].equals(month)) {
                monthInt = i;
              }
            }
            String dayStr = "" + day;
            String monStr = "" + (monthInt + 1);
            if (monthInt < 9) {
              monStr = "0" + monStr;
            }
           if (day < 10) {
              dayStr = "0" + day;
            }
            /*SaveValue.getInstance(context).saveStrValue(
                    mItemId, year + "/" + monStr + "/" + dayStr);*/
            String tempDate = year + "-" + monStr + "-" + dayStr;
            saveShceduleDateTime(tempDate);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempDate: "+tempDate);
        }
        mBackStack.accept(this);
    }

	private void saveShceduleDateTime(String tempDate) {
		 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveShceduleDateTime: "+tempDate);
		 String[][] twoDateTimeArrays = compareDateTime(tempDate);
		 if(twoDateTimeArrays==null){
			 return;
		 }
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

	private synchronized String[][] compareDateTime(String tempDate) {
	    long start = 0;
        long end = 0;
	    String dateStart = SaveValue.getInstance(context).readStrValue(
	    		MenuConfigManager.TIME_START_DATE + channelId);
	    String dateEnd = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_END_DATE + channelId);
	    String timeStart = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_START_TIME + channelId);
	    String timeEnd = SaveValue.getInstance(context).readStrValue(
	        MenuConfigManager.TIME_END_TIME + channelId);
	    Log.d(TAG, "compareDateTime:dstart:" + dateStart + ",dend:" + dateEnd + ",tstart:" + timeStart+ ",tend:" + timeEnd);
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mItemId:" + mItemId);
	    if (mItemId.startsWith(MenuConfigManager.TIME_START_DATE)) {
	        dateStart = tempDate;
	        start = DateFormatUtil.getDate2Long(dateStart+" "+timeStart+":00");
            end = DateFormatUtil.getDate2Long(dateEnd+" "+timeEnd+":00");
	        Log.d(TAG, "TIME_START_DATE:dstart:" + start + ",dend:" + end);
	      if (start>end) {// startdate >= enddate
	          Log.d(TAG, ">0 1");
	    	  return null;
	      }/*else if(dateStart.compareTo(dateEnd) ==0){
	    	 if (timeStart.compareTo(timeEnd) >= 0) {// starttime>endtime
	    		 return null;
	    	 }
	      }*/
	      if(mResultListener!=null){
	    	  mResultListener.onCommitResult(dateStart);
	      }
		    	
	      SaveValue.getInstance(context).saveStrValue(
	          MenuConfigManager.TIME_START_DATE + channelId, dateStart);
	      SaveValue.getInstance(context).saveStrValue(
	          MenuConfigManager.TIME_START_TIME + channelId, timeStart);
	    } else if (mItemId.startsWith(MenuConfigManager.TIME_END_DATE)) {
	        dateEnd = tempDate;
	        start = DateFormatUtil.getDate2Long(dateStart+" "+timeStart+":00");
            end = DateFormatUtil.getDate2Long(dateEnd+" "+timeEnd+":00");
            Log.d(TAG, "TIME_START_DATE:dstart:" + start + ",dend:" + end);
	        if (start>end) {// startdate >= enddate
	              Log.d(TAG, ">0");
	              return null;
	          }
	      Log.d(TAG, "mResultListener="+mResultListener);
	      if(mResultListener!=null){
	    	  mResultListener.onCommitResult(dateEnd);  
	      }
		    	
	      SaveValue.getInstance(context).saveStrValue(
	          MenuConfigManager.TIME_END_DATE + channelId, dateEnd);
	      SaveValue.getInstance(context).saveStrValue(
	          MenuConfigManager.TIME_END_TIME + channelId, timeEnd);
	    }
//	    else if (mCurrAction.mItemID.equals(MenuConfigManager.TIME_START_TIME)) {
//	      if (timeStart.compareTo(timeEnd) > 0) {// starttime > endtime
//	        String[] tarray = timeStart.split(":");
//	        int nHour = Integer.parseInt(tarray[0]);
//	        if (nHour == 23) {// special
//	          timeEnd = timeStart;
//	        } else if (nHour > 0 && nHour < 9) {
//	          timeEnd = "0" + (nHour + 1) + ":" + tarray[1];
//	        } else {
//	          timeEnd = (nHour + 1) + ":" + tarray[1];
//	        }
//	      }
//	      SaveValue.getInstance(context).saveStrValue(
//	          MenuConfigManager.TIME_END_TIME + mCurrAction.mInitValue, timeEnd);
//	      timeSet_endtime.setDescription(timeEnd);
//	    } else if (mCurrAction.mItemID.equals(MenuConfigManager.TIME_END_TIME)) {
//	      if (timeStart.compareTo(timeEnd) > 0) {// starttime>endtime
//	        String[] tarray = timeEnd.split(":");
//	        int nHour = Integer.parseInt(tarray[0]);
//	        if (nHour == 0) {// special
//	          timeStart = timeEnd;
//	        } else if (nHour > 0 && nHour <= 10) {
//	          timeStart = "0" + (nHour - 1) + ":" + tarray[1];
//	        } else {
//	          timeStart = (nHour - 1) + ":" + tarray[1];
//	        }
//	      }
//	      SaveValue.getInstance(context).saveStrValue(
//	          MenuConfigManager.TIME_START_TIME + mCurrAction.mInitValue, timeStart);
//	      timeSet_satrttime.setDescription(timeStart);
//	    }
	    Log.d(TAG, "compareDateTime222:dstart:" + dateStart + ",dend:" + dateEnd
	    		+ ",tstart:"+ timeStart + ",tend:" + timeEnd);
        return new String[][] {
            {
                dateStart, timeStart
            }, {
                dateEnd, timeEnd
            }
        };
    }
}
















