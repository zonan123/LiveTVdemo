package com.mediatek.wwtv.tvcenter.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public final class DateFormatUtil {
    private static final String TAG = "DateFormatUtil";

    private DateFormatUtil() {
    }

    /*
     * this string to parse
     */
    public static Date getDate(String string) {
        Date tempDate = null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat
                .getDateTimeInstance();
        simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
        try {
            tempDate = null;
            String dateStr;
            dateStr = SimpleDateFormat.getDateInstance(
                    SimpleDateFormat.DEFAULT, Locale.CHINA).format(
                    new Date(System.currentTimeMillis()));

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDate()--------dateStr---powoffTimeStr--------->"
                    + dateStr + "  " + string);
            tempDate = simpleDateFormat.parse(dateStr + "  " + string);
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT,
                    SimpleDateFormat.LONG, Locale.CHINA);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDate()-----tempDate--1111111111-----"
                    + simpleDateFormat.format(tempDate));
            Date currentSystem = new Date(System.currentTimeMillis());
            Long poweroff = tempDate.getTime();
            Long currentTime = currentSystem.getTime();
            if (poweroff < currentTime) {
                SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT,
                        SimpleDateFormat.LONG, Locale.CHINA);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "poweroff---->"
                        + simpleDateFormat.format(new Date(poweroff)));
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentTime---->"
                        + simpleDateFormat.format(new Date(currentTime)));
                tempDate = new Date(System.currentTimeMillis() + 24 * 60 * 60
                        * 1000 * 1L);
                dateStr = SimpleDateFormat.getDateInstance(
                        SimpleDateFormat.DEFAULT, Locale.CHINA)
                        .format(tempDate);
                tempDate = simpleDateFormat.parse(dateStr + "  " + string);
            }
        } catch (ParseException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "time is invalida");
            // If you set the shutdown time string is not illegal, say the time
            // to set off the current time for tomorrow
            tempDate = new Date(System.currentTimeMillis() + 24 * 60 * 60
                    * 1000L);
            e.printStackTrace();
        }
        SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT,
                SimpleDateFormat.LONG, Locale.CHINA);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDate()-----tempDate---222222222----"
                + simpleDateFormat.format(tempDate));
        return tempDate;
    }

    public static boolean checkPowOffTimerInvalid(String string) {
        Date tempDate = null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat
                .getDateTimeInstance();
        simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
        try {
            tempDate = null;
            String dateStr;
            dateStr = SimpleDateFormat.getDateInstance(
                    SimpleDateFormat.DEFAULT, Locale.CHINA).format(
                    new Date(System.currentTimeMillis()));
            tempDate = simpleDateFormat.parse(dateStr + "  " + string);
            Date currentSystem = new Date(System.currentTimeMillis());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "poweroff---->" + simpleDateFormat.format(tempDate));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentTime---->"
                    + simpleDateFormat.format(currentSystem));
            Long poweroff = tempDate.getTime();
            Long currentTime = currentSystem.getTime();
            if (poweroff <= currentTime) {
                return true;
            }
        } catch (ParseException e) {
            return false;
        }
        return false;
    }

    public static boolean checkPowOnTimerInvalid(String string) {
        Date tempDate = null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat
                .getDateTimeInstance();
        simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
        try {
            tempDate = null;
            String dateStr;
            dateStr = SimpleDateFormat.getDateInstance(
                    SimpleDateFormat.DEFAULT, Locale.CHINA).format(
                    new Date(System.currentTimeMillis()));
            tempDate = simpleDateFormat.parse(dateStr + "  " + string);
            Date currentSystem = new Date(System.currentTimeMillis());
            Long poweron = tempDate.getTime();
            Long currentTime = currentSystem.getTime();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "poweron---->" + simpleDateFormat.format(tempDate));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentTime---->"
                    + simpleDateFormat.format(currentSystem));
            if (poweron >= currentTime - 30000 && poweron <= currentTime) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "----power--on--start--system------");
                return true;
            }
        } catch (ParseException e) {
            return false;
        }
        return false;
    }

    public static String getCurrentTime(){
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat
                .getDateTimeInstance();
        simpleDateFormat.applyPattern("yyyy/MM/dd");
        Date currentSystem = new Date(System.currentTimeMillis());
        String currentTime= simpleDateFormat.format(currentSystem);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentTime="+currentTime);
        return currentTime;
    }

    /*
     * this string to parse
     */
    public static long getDate2Long(String date) {
        long tempDate = 0;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat
                .getDateTimeInstance();
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
        try {
            tempDate = simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "time is invalida :"+e.toString());
            simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
            e.printStackTrace();
        }
        if(tempDate == 0){
            simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
            try {
                tempDate = simpleDateFormat.parse(date).getTime();
            } catch (ParseException e) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "time is invalida :"+e.toString());
                e.printStackTrace();
            }
        }
        return tempDate;
    }
}
