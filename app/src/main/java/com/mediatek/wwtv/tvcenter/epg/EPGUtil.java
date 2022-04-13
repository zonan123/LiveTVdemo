package com.mediatek.wwtv.tvcenter.epg;

import android.app.Dialog;
import android.content.Context;



import android.text.format.DateFormat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public abstract class EPGUtil {
    private static final String TAG = "EPGUtil";
    private static boolean isNeverReceived = true;

    public static String formatCurrentTime(Context context){
    	return EPGUtil.judgeFormatTime(context)==1?EPGUtil.formatCurrentTimeWith24Hours():EPGUtil.formatCurrentTimeWith12Hours();
    }
    
    public static String formatCurrentTime(Context context,boolean is3rdTVSource){
    	return EPGUtil.judgeFormatTime(context)==1?EPGUtil.formatCurrentTimeWith24Hours(is3rdTVSource):EPGUtil.formatCurrentTimeWith12Hours(is3rdTVSource);
    }

    public static String formatCurrentTimeWith24Hours() {
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return formatCurrentTimeWith24Hours(is3rdTVSource);
    }
    /**
     * Time for top
     * @return
     */
    public static String formatCurrentTimeWith24Hours(boolean is3rdTVSource) {
        String sbString = "";
        MtkTvTimeFormatBase timeFormat =is3rdTVSource ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastLocalTime();
        String hour = String.valueOf(timeFormat.hour);
        String min = String.valueOf(timeFormat.minute);
        String sec = String.valueOf(timeFormat.second);
        if (timeFormat.minute < 10) {
            min = "0" + min;
        }
        if (timeFormat.second < 10) {
            sec = "0" + sec;
        }
        sbString = getWeekFull(timeFormat.weekDay) + ",  " +
                //timeFormat.monthDay + "-" +
                //getEngMonthFull(timeFormat.month+1) + "-" +
                //timeFormat.year + " "+
                getYMDBySysLang() + " " +
                hour + ":" + min + ":" + sec;
        return sbString;
    }

    public static String getEngMonthFull(int month) {
        if (month > DataReader.getInstanceWithoutContext().getMonthFullArray().length||month<1) {
            month = 1;
        }
        return DataReader.getInstanceWithoutContext().getMonthFullArray()[month - 1];
    }

    public static String getEngMonthSimple(int month) {
        if (month > DataReader.getInstanceWithoutContext().getMonthSimpleArray().length||month<1) {
            month = 1;
        }
        return DataReader.getInstanceWithoutContext().getMonthSimpleArray()[month - 1];
    }

    public static long getCurrentLocalTimeMills() {
        MtkTvTimeFormatBase timeFormat = MtkTvTime.getInstance().getLocalTime();
        return timeFormat.toMillis();
    }
    
    public static String formatCurrentTimeWith12Hours() {
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return formatCurrentTimeWith12Hours(is3rdTVSource);
    }

    /**
     * Time for top
     * @return
     */
    public static String formatCurrentTimeWith12Hours(boolean is3rdTVSource) {
        String sbString = "";
        MtkTvTimeFormatBase timeFormat =is3rdTVSource ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastLocalTime();
        String amp = "";
        if (timeFormat.hour < 12) {//"AM"
            amp = " " + "AM";
        } else {
            timeFormat.hour = timeFormat.hour - 12;
            amp = " " + "PM";
        }
        String hour = String.valueOf(timeFormat.hour);
        String min = String.valueOf(timeFormat.minute);
        String sec = String.valueOf(timeFormat.second);
        if (timeFormat.hour == 0) {
            hour = "12";
//        } else if (timeFormat.hour < 10) {
//            hour="0"+hour;
        }
        if (timeFormat.minute < 10) {
            min = "0" + min;
        }
        if (timeFormat.second < 10) {
            sec = "0" + sec;
        }
        sbString = getWeekFull(timeFormat.weekDay) + ",  " +
                getYMDBySysLang()
//        timeFormat.monthDay+"-" + getEngMonthFull(timeFormat.month+1)+"-" + timeFormat.year
        +"  " + hour+":" + min + ":" + sec + " " + amp;
        return sbString;
    }

    /**
     * date format by system language
     * @param value
     * @return
     */
    public static String getYMDBySysLang(){
        String sbString = "";
        MtkTvTimeFormatBase timeFormat =
            CommonIntegration.getInstance().is3rdTVSource() ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastLocalTime();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(
                timeFormat.year, timeFormat.month, timeFormat.monthDay);
        sbString = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM)
                .format(gregorianCalendar.getTime());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sbString: "+sbString);
        return sbString;
    }

    public static String getYMDLocalTime(){
        String sbString = "";
        MtkTvTimeFormatBase timeFormat =
            CommonIntegration.getInstance().is3rdTVSource() ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastLocalTime();
        sbString = timeFormat.year +"/"+
                    (timeFormat.month+1)+"/"+
                     timeFormat.monthDay+" ";

        return sbString;
    }

    public static String getSimpleDate(long date) {
        String sbString = "";//format.format(gCalendar.getTime());
        MtkTvTimeFormatBase timeFormat = new MtkTvTimeFormatBase();
        timeFormat.setByUtcAndConvertToLocalTime(date);
        sbString = timeFormat.year + "/"+
                (timeFormat.month+1) + "/" +
                timeFormat.monthDay +" ";
        return sbString;
    }

    public static int getLocalHourByUtc(long time) {
        MtkTvTimeFormatBase timeFormat = new MtkTvTimeFormatBase();
        timeFormat.setByUtcAndConvertToLocalTime(time);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei getLocalHourByUtc>>"+timeFormat.hour);
        return timeFormat.hour;
    }

    public static String getWeekFull(int value) {
        if (value > DataReader.getInstanceWithoutContext().getWeekFullArray().length - 1) {
            value = 0;
        }
        return DataReader.getInstanceWithoutContext().getWeekFullArray()[value];
    }

    public static String getWeek(int value) {
        if (value > DataReader.getInstanceWithoutContext().getWeekSimpleArray().length - 1) {
            value = 0;
        }
        return DataReader.getInstanceWithoutContext().getWeekSimpleArray()[value];
    }
    
    public static String formatTimeFor24(long time){
    	 MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
         timeformat.set(time);
         String minString = "";
         String hourString = "";
         if (timeformat.hour<10) {
        	 hourString="0"+timeformat.hour;
         }
         else{
        	 hourString=""+timeformat.hour;
         }
         if (timeformat.minute<10) {
             minString="0"+timeformat.minute;
         }else {
             minString=""+timeformat.minute;
         }
         return hourString+":"+minString;
    }
    
    /***
     * ' HH:mm AM'
     * @param time
     * @return startTime
     */
    public static String formatStartTime(long time){
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        String minString = "";
        String hourString = "";
        String amp = "";
        if (timeformat.hour<=12) {//"AM"
            amp = " "+"AM";
        }else {
            timeformat.hour = timeformat.hour - 12;
            amp = " "+"PM";
        }
        if (timeformat.minute<10) {
            minString="0"+timeformat.minute;
        }else {
            minString=""+timeformat.minute;
        }
        if (timeformat.hour == 0) {
            hourString = "12";
//        } else if (timeformat.hour<10) {
//            hourString="0"+timeformat.hour;
        }else {
            hourString=""+timeformat.hour;
        }
//        if (timeformat.hour<=12) {//"AM"
//            startTime = startTime + " " + "AM";
//        }else {
//            startTime = startTime + " " + "PM";
//        }
        return hourString+":"+minString + amp;
    }
    
    
    public static String formatTimeFor24(int hour,int minute){
    	String minString = "";
        String hourString = "";
        if (hour<10) {
       	 hourString="0"+hour;
        }
        else{
       	 hourString=""+hour;
        }
        if (minute<10) {
            minString="0"+minute;
        }else {
            minString=""+minute;
        }
        return hourString+":"+minString;
    }
    
    public static String formatTimeFor12(int hour,int minute){
        String minString = "";
        String hourString = "";
        String amp = "";
        if (hour<12) {//"AM"
            amp = " "+"AM";
        }else {
        	hour = hour - 12;
            amp = " "+"PM";
        }
        if (minute<10) {
            minString="0"+minute;
        }else {
            minString=""+minute;
        }
        if (hour == 0) {
            hourString = "12";
        }else {
            hourString=""+hour;
        }
        return hourString+":"+minString + amp;
    }
    
    
    public static String formatTime(int hour,int minute,Context context){
    	String strTime=judgeFormatTime(context)==1?EPGUtil.formatTimeFor24(hour,minute):EPGUtil.formatTimeFor12(hour,minute);
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime>>strTime"+strTime);
    	return strTime;
    }

    public static int judgeFormatTime(Context context){
    	boolean is24HourFormat=DateFormat.is24HourFormat(context);
    	com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "judgeFormatTime>>is24HourFormat="+is24HourFormat);
    	if(is24HourFormat){
    		return 1;
    	}
    	else{
    		return 0;
    	}
    }
    
    
    /***
     * ' HH:mm AM'
     * @param time
     * @return startTime
     */
    public static String formatTime(long time,Context context){
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
		String strTime=EPGUtil.judgeFormatTime(context)==1?EPGUtil.formatTimeFor24(timeformat.hour,timeformat.minute):EPGUtil.formatTimeFor12(timeformat.hour,timeformat.minute);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime>>strTime"+strTime);
		return strTime;
    }

    /***
     * ' HH:mm AM E,dd-MM'
     * @param time
     * @return enfTimeStr
     */
    public static String formatEndTime(long startTime,long duration){
        long endTime = startTime + duration;
        Date date = new Date(endTime);
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm E,dd-MM",Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        String endTimeStr = format.format(gCalendar.getTime());
        if (gCalendar.get(GregorianCalendar.AM_PM)==0) {//"AM"
            endTimeStr = endTimeStr + " " + "AM";
        }else {
            endTimeStr = endTimeStr + " " + "PM";
        }
        return endTimeStr;
    }

    /***
     * 'E,dd-MM'
     * @param time
     * @return enfTimeStr
     */
    public static String formatEndTimeDay(long startTime,long duration){
        long endTime = startTime + duration;
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(endTime);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeformat.month11>>" + timeformat.month + ">>>" + timeformat.monthDay + ">>>" + timeformat.weekDay + ">>>");
        if (timeformat.month < 0 || timeformat.monthDay  < 0 || timeformat.weekDay < 0) {
            return "";
        }
        return getWeek(timeformat.weekDay)+","+" "+timeformat.monthDay+"-"+getEngMonthSimple(timeformat.month + 1);
    }

    public static String getWeekDayofTime(long time, long curDay) {//fix CR DTV00584353
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getWeekDayofTimetimeformat.toMillis()222>>" + time + "   " + timeformat.toMillis() + "  " + curDay);
        if (timeformat.month < 0 || timeformat.monthDay  < 0 || timeformat.weekDay < 0) {
            return "";
        }
        time = time - timeformat.hour * 3600 - timeformat.minute * 60 - timeformat.second;
        timeformat.set(time);
        if (timeformat.toMillis() - curDay == 0) {
            return "Today";
        } else if (timeformat.toMillis() - curDay == 24 * 60 * 60) {
            return "Tomorrow";
        } else if (curDay - timeformat.toMillis() == 24 * 60 * 60) {
            return "Yesterday";
        }
        return getWeek(timeformat.weekDay);
    }

    public static int getDayOffset(long time) {
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        time = time - timeformat.hour * 3600 - timeformat.minute * 60 - timeformat.second;
        timeformat.set(time);
        long curDay = getCurrentDayStartTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDayOffsettimeformat.toMillis()>>" + time + "   " + timeformat.toMillis() + "  " + curDay + "  " + ((timeformat.toMillis() - curDay) / (24 * 60 * 60)));
        return (int)((timeformat.toMillis() - curDay) / (24 * 60 * 60));
    }

    public static String getTodayOrTomorrow(long startTime,long curTime){
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getTodayOrTomorrow:starttime:"+startTime+"==>curtime:"+curTime);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "getTodayOrTomorrow:value:"+(startTime-curTime));
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(startTime);
        int startDay= timeformat.monthDay;
        timeformat.set(curTime);
        int curDay= timeformat.monthDay;
        int dvalue=curDay-startDay;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getTodayOrTomorrow:day:"+dvalue);
        String dayString = "Toady";
        if (dvalue==0) {
            dayString = "Toady";
        }else if(dvalue<0){
            dayString = "Tomorrow";
        }else if(dvalue>0){
            dayString = "Yesterday";
        }
        return dayString;
    }

    public static String getProTime(long second,long duration){
        long endTime = second +duration;
        String v1= formatStartTime(second);
        String v2= formatStartTime(endTime);
        String v3= formatEndTimeDay(second,0);
        return v1+" "+"-"+" "+v2+" "+v3;
    }
    
    public static long getCurrentTime(){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getCurrentTime(is3rdTVSource);
    }

    public static long getCurrentTime(boolean is3rdTVSource){
        MtkTvTimeFormatBase tvTime =is3rdTVSource ?
            MtkTvTime.getInstance().getLocalTime():
            MtkTvTime.getInstance().getBroadcastUtcTime();
        return is3rdTVSource?tvTime.toMillis():tvTime.toSeconds();
    }

    public static long getCurrentDayStartTime(){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getCurrentDayStartTime(is3rdTVSource);
    }
    public static long getCurrentDayStartTime(boolean is3rdTVSource){
        MtkTvTimeFormatBase tvTime =is3rdTVSource?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "time in year:"+tvTime.hour);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "time in year:"+tvTime.minute);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "time in year:"+tvTime.second);
        long todayStart = 0;
        todayStart = tvTime.toMillis()-tvTime.hour*3600-tvTime.minute*60-tvTime.second;
        return todayStart;
    }

    public static int getCurrentHour(){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getCurrentHour(is3rdTVSource);
    }

    public static int getCurrentHour(boolean is3rdTVSource){
        MtkTvTimeFormatBase tvTime =is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() :
                    MtkTvTime.getInstance().getBroadcastUtcTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "getCurrentHour ,tvTime.month+1: "+tvTime.month+1);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "getCurrentHour ,tvTime.monthDay: "+tvTime.monthDay);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "getCurrentHour ,tvTime.hour: "+tvTime.hour + ", utc Seconds:"+tvTime.toSeconds());

        return isNZL() ? tvTime.hour -1 : tvTime.hour;
    }

    public static int getCurrentLocalHour(){
        MtkTvTimeFormatBase tvTime = MtkTvTime.getInstance().getBroadcastLocalTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "getCurrentLocalHour ,hour: "+tvTime.hour);
        return tvTime.hour;
    }

    public static boolean isNZL() {
        MtkTvTimeFormatBase tvTime = MtkTvTime.getInstance().getBroadcastLocalTime();

        String countryCode = CommonIntegration.getInstance().getCountryCode();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "getCurrentHour ,countryCode: "+countryCode);
        if(countryCode.equals(MtkTvConfigTypeBase.S3166_CFG_COUNT_NZL)){//fix summer time issue DTV02023934 DTV01998369
            if (tvTime.month+1 == 4) {
                if(tvTime.monthDay <=7){
                    if(tvTime.hour <=2 && tvTime.hour>=1){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isNZL~");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /*
     * this func is mainly for fix NZL summer time cases
     * case 1 NZL, beginHour=1(12); cur = 2(13)
     * case 2 NZL,beginHour=2(13); cur = 2(13)
     * case 3 NZL,beginHour=2(13); cur = 2(14)
     * case 4: no-NZL, other cty or no-summer time
     */
    public static int getTimeAlixsWidthWithHourMinuteSecond(int beginHour){
        MtkTvTimeFormatBase tvUtcTime = MtkTvTime.getInstance().getBroadcastUtcTime();
        MtkTvTimeFormatBase tvLocalTime = MtkTvTime.getInstance().getBroadcastTime();

        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "TimeAlixs ,startHour: "+beginHour);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "TimeAlixs ,tvUtcTime.hour: "+tvUtcTime.hour);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "TimeAlixs ,tvLocalTime.hour: "+tvLocalTime.hour);

        int diffHour = 0;
        if(isNZL()){
            if((tvUtcTime.hour == 13 && tvLocalTime.hour == 2 && beginHour == 2) ){
                diffHour = 0;
            }else {
                diffHour = 1;
            }
        } else {
            diffHour = (tvLocalTime.hour == beginHour) ? 0 : 1;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "TimeAlixs ,diffHour: "+diffHour + ", utc mills:"+tvUtcTime.toSeconds());
        return 60 * 60 * diffHour + tvUtcTime.minute * 60 + tvUtcTime.second;
    }

    public static int getCurrentMinuteSecond() {
        boolean is3rdTVSource = CommonIntegration.getInstance().is3rdTVSource();
        return getCurrentMinuteSecond(is3rdTVSource);
      }

      public static int getCurrentMinuteSecond(boolean is3rdTVSource) {
        MtkTvTimeFormatBase tvTime = is3rdTVSource ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastTime();
        Log.d(TAG, "tvTime.minute: " + tvTime.minute + ", second: " + tvTime.second);
        return tvTime.minute * 60 + tvTime.second;
      }

    
    public static long getCurrentDayHourMinute(){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getCurrentDayHourMinute(is3rdTVSource);
    }
    public static long getCurrentDayHourMinute(boolean is3rdTVSource){
        MtkTvTimeFormatBase tvTime =is3rdTVSource?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastTime();
        return tvTime.toMillis() - (tvTime.minute * 60) - tvTime.second;
    }

    public static long getCurrentDateDayAsMills(){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getCurrentDateDayAsMills(is3rdTVSource);
    }
    /*
     * get current date day as Second
     */
    public static long getCurrentDateDayAsMills(boolean is3rdTVSource){
        MtkTvTimeFormatBase mtkTvTimeFormatBase = is3rdTVSource ?
                MtkTvTime.getInstance().getLocalTime() : MtkTvTime.getInstance().getBroadcastUtcTime();
        long curTimeMillSeconds = mtkTvTimeFormatBase.toSeconds();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"curTimeMillSeconds>>" + curTimeMillSeconds + ", toMillis()>>"+mtkTvTimeFormatBase.toMillis());
        return curTimeMillSeconds - mtkTvTimeFormatBase.hour * 60 * 60 - mtkTvTimeFormatBase.minute * 60 - mtkTvTimeFormatBase.second;
    }

    public static long getEpgLastTimeMills(int dayNum, int startHour, boolean withHour){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getEpgLastTimeMills(dayNum,startHour,withHour,is3rdTVSource);
    }
    /*
     * get epg last time
     */
    public static long getEpgLastTimeMills(int dayNum, int startHour, boolean withHour,boolean is3rdTVSource){
        MtkTvTimeFormatBase mtkTvTimeFormatBase =is3rdTVSource ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastUtcTime();
        long curTimeMillSeconds = mtkTvTimeFormatBase.toSeconds();
        if (!withHour) {
            curTimeMillSeconds = curTimeMillSeconds - mtkTvTimeFormatBase.hour * 60 * 60 - mtkTvTimeFormatBase.minute * 60 - mtkTvTimeFormatBase.second;
        }
        return curTimeMillSeconds + (dayNum * 24 + startHour) * 60 * 60;
    }

    public static int getEUIntervalHour(int dayNum, int startHour, int intervalHour){
    	boolean is3rdTVSource=CommonIntegration.getInstance().is3rdTVSource();
    	return getEUIntervalHour(dayNum,startHour,intervalHour,is3rdTVSource);
    }
    /*
     * get current date as Second
     */
    public static int getEUIntervalHour(int dayNum, int startHour, int intervalHour,boolean is3rdTVSource){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dayNum>>>" + dayNum + "  " + startHour + "  " + intervalHour);
        MtkTvTimeFormatBase mtkTvTimeFormatBase =is3rdTVSource ?
            MtkTvTime.getInstance().getLocalTime() :
            MtkTvTime.getInstance().getBroadcastUtcTime();
        long curTimeMillSeconds = mtkTvTimeFormatBase.toSeconds() - (mtkTvTimeFormatBase.hour * 60 * 60) - mtkTvTimeFormatBase.minute * 60 - mtkTvTimeFormatBase.second;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEUIntervalHour curTimeMillSeconds>>>" + curTimeMillSeconds);
        mtkTvTimeFormatBase.setByUtcAndConvertToLocalTime(curTimeMillSeconds + (dayNum * 24 + startHour + intervalHour) * 60 * 60);
        return mtkTvTimeFormatBase.hour;
    }

    public static Locale getLocaleLan() {
//        if (sv.readValue(MenuConfigManager.OSD_LANGUAGE) == 1) {
//            return Locale.CHINA;
//        } else {
            return Locale.US;
//        }
    }

    public static void setPositon(int xxxx,int yyyy,Dialog dialog) {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = xxxx;// * ScreenConstant.SCREEN_WIDTH/CecManager.tvWidth;
        lp.y = yyyy;//yoff*ScreenConstant.SCREEN_HEIGHT/CecManager.tvHeight;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "cec:xxxx:"+xxxx+"_yyyy:"+yyyy);
        window.setAttributes(lp);
    }

    public static void setIsNeverReceived(boolean flag) {
      isNeverReceived = flag;
    }

    public static boolean getIsNeverReceived() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isNeverReceived:"+isNeverReceived);
      return isNeverReceived;
    }
}
