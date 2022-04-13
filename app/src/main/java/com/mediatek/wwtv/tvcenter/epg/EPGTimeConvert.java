/**
 * this class is used for calculate time and format date in EPG
 */
package com.mediatek.wwtv.tvcenter.epg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.wwtv.tvcenter.epg.us.ListItemData;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;

import com.mediatek.wwtv.setting.util.Util;

public final class EPGTimeConvert {
	private static final String TAG = "EPGTimeConvert";
	private static EPGTimeConvert tmConvert;

	private EPGTimeConvert() {

	}

	public static synchronized EPGTimeConvert getInstance() {
		if (tmConvert == null) {
			tmConvert = new EPGTimeConvert();
		}
		return tmConvert;
	}

	/**
	 * convert hour to milliseconds
	 *
	 * @param hour
	 * @return milliseconds
	 */
	public long getHourtoMsec(int hour) {
    return (long) hour * 60 * 60 ;
	}

	/**
	 * calculate EPG program width ,total width is 1
	 *
	 * @param endTime
	 *            program end time
	 * @param startTime
	 *            program start time
	 * @return width
	 */
	public static float countShowWidth(long endTime, long startTime) {
		return (float) (endTime - startTime) / (60 * EPGConfig.mTimeSpan * 60L);
	}

	/**
	 * calculate EPG program width
	 *
	 * @param duration
	 *            program playing time
	 * @return width
	 */
	public float countShowWidth(long duration) {
		return (float) duration / (60 * EPGConfig.mTimeSpan * 60L);
	}

	/**
	 * convert date to special string
	 *
	 * @param date
	 * @return
	 */
	public String getDetailDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("E,dd-MM-yyyy HH:mm:ss", EPGUtil.getLocaleLan());
		return formatter.format(date);
	}

	/**
	 * calculate the time to set
	 *
	 * @param curTime
	 *            current time
	 * @param day
	 *            current day is 0
	 * @param startHour
	 * @return
	 */
	public long setDate(long curTime, int day, long startHour) {
	    long dateTime = (day * 24 + startHour)* 60 * 60+ curTime ;
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setDate:"+day+"==>"+startHour + ",curTime>>"+curTime + ",  dateTime>>"+dateTime);   //curTime  already modify hour,no need - startHour
		return dateTime;   //fix CR DTV00584308
	}

	/**
	 * get special date string, format (21-03-2011)
	 *
	 * @param date
	 * @return
	 */
	public String getSimpleDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",
		        EPGUtil.getLocaleLan());
		return formatter.format(date);
	}

//	public static Date getNormalDate(String time) {
//		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
//				"dd/MM/yyyy HH:mm:ss");
//		try {
//			return mSimpleDateFormat.parse(time);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	

	/**
	 * getspecial date string, format (03:15 - 04:30 Mon, 03-Jan)
	 *
	 * @param mTVProgramInfo
	 * @return
	 */
	public String formatProgramTimeInfo(EPGProgramInfo mTVProgramInfo,int timeFormat) {
		if (mTVProgramInfo == null || mTVProgramInfo.getmTitle() == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>mTVProgramInfo.getmStartTime()="+mTVProgramInfo.getmStartTime());
		timeFormatBase.set(mTVProgramInfo.getmStartTime());
        String shour = String.valueOf(timeFormatBase.hour);
        int startDay=timeFormatBase.monthDay;
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "formatProgramTimeInfoForTVSource------>timeFormatBase.hour="+timeFormatBase.hour+",shour="+shour+",startDay="+startDay);
        String smin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.minute < 10) {
            smin = "0" + smin;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "formatProgramTimeInfoForTVSource------>timeFormatBase.minute="+timeFormatBase.minute+",smin="+smin);
		String startTime = shour + ":" + smin;
        String monthDay = String.valueOf(timeFormatBase.monthDay);
        String month = String.valueOf(timeFormatBase.month + 1);
		if(monthDay.startsWith("0")){
			monthDay = monthDay.substring(1);
		}
		if(month.startsWith("0")){
			month = month.substring(1);
		}
        String dayTime = EPGUtil.getWeekFull(timeFormatBase.weekDay)+" , "+ month + "-" + monthDay;

		timeFormatBase.set(mTVProgramInfo.getmEndTime());
		int endDay=timeFormatBase.monthDay;
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "formatProgramTimeInfoForTVSource------>endDay="+endDay);
        String ehour = String.valueOf(timeFormatBase.hour);
        String emin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.minute < 10) {
            emin = "0" + emin;
        }
		String endTime = ehour + ":" + emin;
		if(timeFormat==0){
			startTime=Util.formatTime24To12(startTime);
			endTime=Util.formatTime24To12(endTime);
        }
		sb.append(startTime + " - " + endTime);
		if((mTVProgramInfo.getmEndTime()-mTVProgramInfo.getmStartTime())>24*60*60){
			sb.append(BannerImplement.STR_TIME_SPAN_TAG);
		}
		sb.append("   " + dayTime);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatProgramTimeInfoForTVSource------>sb.toString()="+sb.toString());
		return sb.toString();
	}
	
	public static String converTimeByLong2Str(long time){
		MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
		timeFormatBase.set(time);
        String shour = String.valueOf(timeFormatBase.hour);
        String smin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.hour < 10) {
        	shour = "0" + shour;
        }
        if (timeFormatBase.minute < 10) {
            smin = "0" + smin;
        }
        String strTime = shour + ":" + smin;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "converTimeByLong2Str------>strTime="+strTime);
        return strTime;
	}

	public Long getStartTime(ListItemData mTVProgramInfo){
		String dateStr=egpInfoToStr(mTVProgramInfo.getMillsStartTime());
		return egpDataToDate(dateStr).getTime();
	}

	public Long getEndTime(ListItemData mTVProgramInfo){
		String dateStr=egpInfoToStr(mTVProgramInfo.getMillsStartTime()+mTVProgramInfo.getMillsDurationTime());
		return egpDataToDate(dateStr).getTime();
	}

	public Long getStartTime(EPGProgramInfo mTVProgramInfo){
		String dateStr=egpInfoToStr(mTVProgramInfo.getmStartTime());
		return egpDataToDate(dateStr).getTime();
	}

	public Long getEndTime(EPGProgramInfo mTVProgramInfo){
		String dateStr=egpInfoToStr(mTVProgramInfo.getmEndTime());
		return egpDataToDate(dateStr).getTime();
	}

	public String egpInfoToStr(Long epgTime) {
		MtkTvTimeFormatBase timeFormatBase = new MtkTvTimeFormatBase();
		timeFormatBase.set(epgTime);
        String shour = String.valueOf(timeFormatBase.hour);
        String smin = String.valueOf(timeFormatBase.minute);
        if (timeFormatBase.hour < 10) {
            shour = "0" + shour;
        }
        if (timeFormatBase.minute < 10) {
            smin = "0" + smin;
        }
        String monthDay = String.valueOf(timeFormatBase.monthDay);
        String month = String.valueOf(timeFormatBase.month + 1);
        if (timeFormatBase.monthDay < 10) {
            monthDay = "0" + monthDay;
        }
        if ((timeFormatBase.month + 1) < 10) {
            month = "0" + month;
        }

		int year=timeFormatBase.year;
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Year:"+year);
    return String.format("%d/%s/%s,%s:%s:%d", year,month,monthDay,shour,smin,timeFormatBase.second);
	}

	public Date egpDataToDate(String str) {
		Date date=new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss",
				Locale.getDefault());
		try {
			date = format.parse(str.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	/**
	 * get hour and minute of date
	 *
	 * @param date
	 * @return
	 */
	public String getHourMinite(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",
				EPGUtil.getLocaleLan());
		return formatter.format(date);
	}

	public String getHourMinite(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",
		        EPGUtil.getLocaleLan());
		return formatter.format(new Date(time));
	}

}
