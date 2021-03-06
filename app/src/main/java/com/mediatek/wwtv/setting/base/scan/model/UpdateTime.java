package com.mediatek.wwtv.setting.base.scan.model;

import java.util.Calendar;

import android.content.Context;
import android.provider.Settings;
import android.text.format.Time;

import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;

public class UpdateTime  implements Runnable{
	private UpdateListener mListener;
	static boolean isRun = true;
	private Time timeModify;
	private int monthDay = 0;
	private int month = 0;
	private int year = 0;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private String type;// flag of time
	private Time time = new Time();
	private Context mContext;

	public UpdateTime() {
	    android.util.Log.d("UpdateTime", "UpdateTime()");
	}

	public UpdateTime(String type) {
		this.type = type;
	}
	public synchronized  Time getTime(){
		time.setToNow();
		return time;
	}
	public synchronized void getDetailTime(){
		if(SaveValue.getInstance(mContext).readValue(
				MenuConfigManager.AUTO_SYNC) == 1){
		Calendar mCalendar = Calendar.getInstance();
//		mCalendar.setTimeInMillis(TVTimerManager.getInstance(mContext).getBroadcastUTC());
		year = mCalendar.get(mCalendar.YEAR);
		if (MenuConfigManager.TIME_TIME.equals(type)) {
			month = mCalendar.get(mCalendar.MONTH);
		}else {
			month = mCalendar.get(mCalendar.MONTH) + 1;
		}
		monthDay = mCalendar.get(mCalendar.DATE);
		hour = mCalendar.get(mCalendar.HOUR_OF_DAY);
		minute =mCalendar.get(mCalendar.MINUTE);
		second =mCalendar.get(mCalendar.SECOND);
		}else{
			timeModify = getTime();
		year = timeModify.year;
		if (MenuConfigManager.TIME_TIME.equals(type)) {
			month = timeModify.month;
		}else {
			month = timeModify.month+1;
		}
		monthDay = timeModify.monthDay;
		hour = timeModify.hour;
		minute = timeModify.minute;
		second = timeModify.second;
	}
		
		
	}
	
	public void modifyTime(){
		time.set(second, minute, hour, monthDay, month, year);
//			SystemClock.setCurrentTimeMillis(when);
			
		getTime();
	}

	public void run() {
		while (isRun) {
			getDetailTime();
			if (MenuConfigManager.TIME_TIME.equals(type)) {
				String hourStr = hour + "";
				String minStr = minute + "";
				String secStr = second + "";
				if (hour < 10) {
					hourStr = 0 + hourStr;
				}
				if (minute < 10) {
					minStr = 0 + minStr;
				}
				if (second < 10) {
					secStr = 0 + secStr;
				}
				String mString = hourStr + ":" + minStr + ":" + secStr;
				//Log.d("timestring","come in set TIME_TIME"+mString);
				mListener.update(mString);
			}
			if (MenuConfigManager.TIME_DATE.equals(type)) {
				String monString = "" + month;
				String dayString = "" + monthDay;
				if (month < 10) {
					monString = 0 + monString;
				}
				if (monthDay < 10) {
					dayString = 0 + dayString;
				}
				//Log.d("timestring","come in set TIME_DATE"+monString);
				String mString = year + "/" + monString + "/" + dayString;
				mListener.update(mString);
			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
	}

	public void startprocess(UpdateListener listener,Context context) {
		this.mListener = listener;
		isRun = true;
//		new Thread(this).start();
		TVAsyncExecutor.getInstance().execute(this);
		this.mContext = context;
	}

	public interface UpdateListener {
		void update(String mString);
	}

	public void shutDownThread() {
		isRun = false;
	}

	public void startThread() {
		isRun = true;
	}
	public void setAutoState(Context context, int value){
		Settings.System.putInt(context.getContentResolver(), Settings.System.AUTO_TIME,value);
	}

	public void onTimeModified(String time) {
		if (MenuConfigManager.TIME_TIME.equals(type)) {
			int hour = Integer.parseInt(time.substring(0, 2));
			int minute = Integer.parseInt(time.substring(3, 5));
			int second = 0;
			if (time.length() > 5) {
			    second = Integer.parseInt(time.substring(6));
            }
			this.hour = hour;
			this.minute = minute;
			this.second = second;
		}
		if (MenuConfigManager.TIME_DATE.equals(type)) {
			int year = Integer.parseInt(time.substring(0, 4));
			int month = Integer.parseInt(time.substring(5, 7));
			int monthDay = Integer.parseInt(time.substring(8));
			this.year = year;
			this.month = month-1;
			this.monthDay = monthDay;
		}
		modifyTime();
	}



}
