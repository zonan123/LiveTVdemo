package com.mediatek.wwtv.tvcenter.dvr.manager;


import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.MountPoint;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.wwtv.tvcenter.R;


import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 */
public final class Util {

    private final static String TAG = "Util[dvr]";
    private final static boolean PRINT_TRACE = true;

    public static void showDLog(String string) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,",string-->"+string);
    }
    public static void showELog(String string) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,",string-->"+string);
    }
    public static void showDLog(String tag, String string) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tag-->"+tag+",string-->"+string);
    }
    public static void showDLog(String tag, int string) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tag-->"+tag+",string-->"+string);
    }
    public static void showELog(String tag, String string) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tag-->"+tag+",string-->"+string);
    }
    public static void showELog(String tag, int string) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tag-->"+tag+",string-->"+string);
    }

    public static void printStackTrace() {
        if (PRINT_TRACE) {
            Throwable tr = new Throwable();
            Log.getStackTraceString(tr);
            tr.printStackTrace();
        }
    }

    private Util(){
    }

    public static String getWeek(Context context, int week) {
        String weekStr;
        switch (week) {
            case 2:
                weekStr = context.getString(R.string.nav_Monday);
                break;
            case 3:
                weekStr = context.getString(R.string.nav_Tuesday);
                break;
            case 4:
                weekStr = context.getString(R.string.nav_Wednesday);
                break;
            case 5:
                weekStr = context.getString(R.string.nav_Thursday);
                break;
            case 6:
                weekStr = context.getString(R.string.nav_Friday);
                break;
            case 7:
                weekStr = context.getString(R.string.nav_Saturday);
                break;
            default:
                weekStr = context.getString(R.string.nav_Sunday);
                break;
        }
        return weekStr;
    }

    public static String secondToString(int second) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());

        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
        format.setTimeZone(gmtZone);

        Date date = new Date();
        date.setTime(second * 1000); // offset 8hs
        return format.format(date);
    }

    public static String dateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
//        TimeZone gmtZone = TimeZone.getTimeZone("GMT");
//        format.setTimeZone(gmtZone);

        return format.format(date);
    }

    public static String formatCurrentTime() {
        String sbString = "";// format.format(gCalendar.getTime());
        MtkTvTimeFormatBase timeFormat = MtkTvTime.getInstance().getLocalTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "time in year:" + timeFormat.toMillis());
        String hour = timeFormat.hour + "";
        String min = timeFormat.minute + "";
        String sec = timeFormat.second + "";
        if (timeFormat.hour < 10) {
            hour = "0" + hour;
        }
        if (timeFormat.minute < 10) {
            min = "0" + min;
        }
        if (timeFormat.second < 10) {
            sec = "0" + sec;
        }
        sbString = hour + ":" + min + ":" + sec;
        return sbString;
    }

    /**
     * covert date to year/month/day,
     *
     * @param date
     * @return
     */
    public static String dateToStringYMD(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
               Locale.getDefault());
        return format.format(date);
    }

    /**
     * covert date to YearMonthDay,
     *
     * @param
     * @return yyyyMMdd_HHmm
     */
    public static String dateToStringYMD2(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
        return format.format(date);
    }

    /**
     * @param date
     * @return yyyy-MM-dd/HH:mm
     */
    public static String dateToStringYMD3(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd/HH:mm",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }

    public static String timeMillisToChar() {
        String str = Long.toString(System.currentTimeMillis());
        return str.substring(str.length() - 4);
    }

    public static Date strToDate(String str) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "strToDate:" + str);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;

        try {
            date = format.parse(str.toString());
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "sItem:strToDate:" + date);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public static long dateToMills(Date date) {
        Time time = new Time();

        time.set(date.getSeconds(), date.getMinutes(), date.getHours(),
                date.getDay(), date.getMonth(), date.getYear());
        return time.toMillis(true);
    }

    public static Date strToTime(String str) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "strToTime:" + str);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;

        try {
            date = format.parse(str.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public static Date getDateTime(String str) {
        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
        String month = (calendar.get(Calendar.MONTH) + 1) + "";
        String day = calendar.get(Calendar.DAY_OF_MONTH) + "";
        str = year + "-" + month + "-" + day + "/" + str;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "sItem:getDateTime" + str);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;

        try {
            date = format.parse(str.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public static Date getDateTime(long time) {
        Date date = new Date(time);
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.setTime(date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        String sbString = format.format(gCalendar.getTime());
        Date date2 = null;

        try {
            date2 = format.parse(sbString.toString());
        } catch (ParseException e) {
            return null;
        }
        return date2;
    }

    public static int strToSecond(String str) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;

        try {
            date = format.parse(str.toString());
        } catch (ParseException e) {
            return 0;
        }

        return date.getHours() * 3600 + date.getMinutes() * 60
                + date.getSeconds();
    }

    /**
     * @param str
     *            like "978237236736",return 2000/12/31
     */
    public static String longStrToDateStr(String str) {
        Date date = new Date(Long.parseLong(str));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "longStrToDateStr," + format.format(date));
        return format.format(date);
    }

    /**
     * @param str
     *            like "978237236736",return "12:33:56"
     */
    public static String longStrToTimeStr(String str) {
        Date date = new Date(Long.parseLong(str));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }

    public static String longStrToTimeStr(Long msTime) {
        Date date = new Date(msTime);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    public static String longToHrMin(Long time) {
        Date date = new Date(time);
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.setTimeZone(TimeZone.getTimeZone("GMT"));


        return String.format("%dhr %dmin", ca.get(Calendar.HOUR_OF_DAY),
                ca.get(Calendar.MINUTE));
    }

    public static Date addDateAndTime(Date date1, Date date2) {
        Date newDate = new Date();

        newDate.setYear(date1.getYear());
        newDate.setMonth(date1.getMonth());
        newDate.setDate(date1.getDate());

        newDate.setHours(date2.getHours());
        newDate.setMinutes(date2.getMinutes());
        newDate.setSeconds(date2.getSeconds());

        return newDate;
    }

    /**
     * Must have 300MB free size.
     *
     * @param manager
     * @return
     */
    public static float speedTest(DvrManager manager) {
        if (!manager.hasRemovableDisk()) {
            return -1f;
        }
        int index = (int) (Math.random() * 1000);

        if (manager.getPvrMountPoint() == null&&manager.getDeviceList().size()>0) {
            manager.setPvrMountPoint(manager.getDeviceList().get(0));

        }
        String path = String.format(manager.getPvrMountPoint().mMountPoint + "/"
                + "speedTest%d.dat", index);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "speedTest," + path);
        File testFile = new File(path);

        float maxSpeed = 0.0f;
        Long minTime = Long.MAX_VALUE;

        if (testFile.exists()) {
            testFile.delete();
        }
        try {
            testFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return 0f;
        }

        int bufferSize = 1024 * 120; // 7.7
        final Long defaultCount = 300L;
        Long counts = defaultCount;

        byte[] writeByte = new byte[(bufferSize)];

        FileOutputStream fis=null;
        Long startTime;
        Long endTime;
        try {
            fis = new FileOutputStream(testFile);
            startTime = System.currentTimeMillis();
            Long startTime1 = 0L;
            Long startTime2 = 0L;

            while (counts > 0) {
                startTime1 = System.currentTimeMillis();
                fis.write(writeByte);
                startTime2 = System.currentTimeMillis();

                if (minTime > startTime2 - startTime1
                        && (startTime2 - startTime1) > 0) {
                    minTime = startTime2 - startTime1;
                }

                counts--;
            }
            fis.close();
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //    return 0f;
        } catch (IOException e) {
            e.printStackTrace();
            return 0f;
        } finally {
        	 if (testFile.exists()) {
                testFile.delete();
        	 }
         try{
             if(fis!=null){
                 fis.close();
                }
            }catch(IOException e){
                 e.printStackTrace();
                }
        }

        endTime = System.currentTimeMillis();

        maxSpeed = bufferSize * 1000f/ minTime / 1024 / 1024;
        // System.out.println(String.format("MaxSpeed:%3.5f MB/s ", maxSpeed));
        maxSpeed = (new BigDecimal(String.valueOf(maxSpeed)).setScale(1,
                BigDecimal.ROUND_HALF_UP)).floatValue();
        System.out.println(String.format("MaxSpeed:%3.1f MB/s ", maxSpeed));

        float average = bufferSize * defaultCount * 1000L
                / (endTime - startTime) / 1024L / 1024L;
        System.out.println(String.format("average:%3.1f MB/s ", average));

        return average;
    }

    /**
     * @param mp
     * @return freeSize/Total Size +"GB"
     */
    public static String getGBSizeOfDisk(MountPoint mp) {
        float tSize;
        float fSize;
        String str;
        if (mp != null) {
            tSize = (float) mp.mTotalSize / 1024 / 1024;
            fSize = (float) mp.mFreeSize / 1024 / 1024;
            str = String.format(mp.mVolumeLabel+": %.1f/%.1f GB", fSize, tSize);
        } else {
            tSize = 0f;
            fSize = 0f;
            str = String.format("No Device");
        }

        return str;
    }

    public static void longToDate(Long time) {
        Date date = new Date(time);
        dataFormat(date);
    }

    public static void dataFormat(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(
                "HH:mm:ss,EEEEEEEE,yyyy/MM/dd");
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, format.format(date));
    }

    public static String[] covertFreeSizeToArray(boolean auto, Long freeSize) {
        String[] list;
        long fsize = freeSize;
        if(fsize<0){
            fsize = -fsize;
        }
        int count = 0;
        count = (int) (fsize / (1024 * 512L));

        float size = 500L;

        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "FreeSize:" + fsize);

        if (count <= 1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "There is not enough space.");
            return null;
        }
        if (count >= 7 && auto) {
            count = 7;
        }
        list = new String[count];

        for (int i = 0; i < list.length; i++) {

            if (size < 1000) {
                list[i] = String.format("%dMB", 512);
            } else {
                list[i] = String.format("%.1fGB", size / 1000);
            }
            size += 500L;
        }
        return list;
    }

    public static boolean makeDIR(String path) {
        File folder = new File(path.toString());
        if (!folder.exists() || !folder.isDirectory()) {
            if (!folder.mkdir()) {
                return false;
            }
        }
        return true;
    }

    public static boolean fomatDisk() {
        try {
            DeviceManager dm = DeviceManager.getInstance();
            List<MountPoint> mps = dm.getMountPointList();
            if(mps.isEmpty()){//|| mps.size()<=0){ 
                return false;
                }
            String externsd = mps.get(0).mDeviceName;
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mp:" + mps.get(0).mDeviceName +
                ", usb_path:" + externsd);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "unmountVolume.");
            dm.umountVol(externsd);
            Thread.sleep(4000);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "start formatVolume");
            int resultformat =dm.formatVol("fat32",externsd);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "format result_:" + resultformat);
            if (resultformat == 0) {
                Thread.sleep(4000);
                dm.mountVol(externsd);
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "result_mount:");
            }
            return true;
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Exception:" + e.getMessage());
            return false;
        }
    }

    public static int getTVHeight() {
        return com.mediatek.wwtv.tvcenter.util.ScreenConstant.SCREEN_HEIGHT;
    }

    public static int getTVWidth() {
        return com.mediatek.wwtv.tvcenter.util.ScreenConstant.SCREEN_WIDTH;
    }
        @Override
    public String toString() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"UtiltoString");
        return "UtiltoString"; 
    }

    public static int strToInt(String time){
        String[] times = time.split(":");
        int hours = Integer.valueOf(times[0]);
        int mins = Integer.valueOf(times[1]);
        int seconds = Integer.valueOf(times[2]);
        //int result = hours*60*60+mins*60+seconds;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"strToInt->"+hours*60*60+mins*60+seconds);
        return hours*60*60+mins*60+seconds;
    }

    public static String intToString(int times){
        int hours = times/(60*60);
        int mins = (times-hours*60*60)/60;
        int second = (times-hours*60*60)%60;
        StringBuilder time = new StringBuilder();
        if(hours<10){
            time.append("0"+hours+":");
        }else{
            time.append(hours+":");
        }
        if(mins<10){
            time.append("0"+mins+":");
        }else{
            time.append(mins+":");
        }
        if(second<10){
            time.append("0"+second);
        }else{
            time.append(second);
        }

         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"intToString->"+time.toString());
        return time.toString();
    }
}
