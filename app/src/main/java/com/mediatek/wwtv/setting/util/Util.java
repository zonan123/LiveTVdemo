package com.mediatek.wwtv.setting.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.text.format.Time;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.Environment;

import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.MountPoint;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTimeshift;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.tvcenter.dvr.manager.Core;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.MtkTvRecordBase;

public class Util {
    private final static String TAG = "TimeShift_PVR";

    public static final String TALKBACK_SERVICE = "com.google.android.marvin.talkback/.TalkBackService";

	public static void showELog(String string) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showELog(String string)");
	}
	public static void showDLog(String string){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDLog(String string)");
	}
	public static void showDLog(String tag, String string){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDLog(String tag, String string)");
	}
	public static void showDLog(String tag, int string){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDLog(String tag, int string)");
	}
	public static void showELog(String tag, String string){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showELog(String tag, String string)");
	}
	public static void showELog(String tag, int string) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showELog(String tag, int string)");
	}

	public static String secondToString(int second) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

		TimeZone gmtZone = TimeZone.getTimeZone("GMT");
		format.setTimeZone(gmtZone);

		Date date = new Date();
		date.setTime(second * 1000); // offset 8hs
		return format.format(date);
	}

	public static String dateToString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		TimeZone gmtZone = TimeZone.getTimeZone("GMT");
		format.setTimeZone(gmtZone);

		return format.format(date);
	}

	public static String formatCurrentTime() {
		String sbString = "";// format.format(gCalendar.getTime());
		MtkTvTimeFormatBase timeFormat = MtkTvTime.getInstance().getLocalTime();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("timeFormat", "time in year:" + timeFormat.toMillis());
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
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
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("strToDate", "strToDate:" + str);
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
				Locale.getDefault());
		format.setTimeZone(TimeZone.getDefault());
		Date date = null;

		try {
			date = format.parse(str.toString());
			com.mediatek.wwtv.tvcenter.util.MtkLog.e("setItemValue", "sItem:strToDate:" + date);
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
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("strToTime", "strToTime:" + str);
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


	/**
	 * @param str
	 *            like "978237236736",return 2000/12/31
	 */
	public static String longStrToDateStr(long time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
				Locale.getDefault());
		format.setTimeZone(TimeZone.getDefault());
		return format.format(new Date(time*1000));
	}

	public static Date getDateTime(String str) {
		Calendar calendar = Calendar.getInstance();
		String year = calendar.get(Calendar.YEAR) + "";
		String month = (calendar.get(Calendar.MONTH) + 1) + "";
		String day = calendar.get(Calendar.DAY_OF_MONTH) + "";
		str = year + "-" + month + "-" + day + "/" + str;
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("getDateTime", "sItem:getDateTime" + str);
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
		return date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds();
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
		Util.showDLog(format.format(date));
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

    public static String longStrToTimeStrN(Long msTime) {
    	MtkTvTimeFormatBase from = new MtkTvTimeFormatBase();
	    MtkTvTimeFormatBase to = new MtkTvTimeFormatBase();

	    from.setByUtc(msTime/1000);
	    MtkTvTimeBase time = new MtkTvTimeBase();

	    time.convertTime(time.MTK_TV_TIME_CVT_TYPE_SYS_UTC_TO_BRDCST_LOCAL, from, to );
        Date date = new Date(to.toSeconds()*1000);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
            Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//        format.setTimeZone(TimeZone.getDefault());
		com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"t0-> "+to.toSeconds()+" ,date->"+format.format(date));
        return format.format(date);}

	public static String longToHrMin(Long time) {
		Date date = new Date(time);
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.setTimeZone(TimeZone.getTimeZone("GMT"));
		return String.format("%dhr %dmin", ca.get(Calendar.HOUR_OF_DAY),
				ca.get(Calendar.MINUTE));
	}

	public static String longToHrMinN(Long time) {

        String sf = null;
        if (time >= 0) {
            sf = String.format("%dhr %dmin", time / 3600,
                    (time % 3600) / 60);
        } else {
            sf = "0hr 0min";
        }

        return sf;
    }

	public static long strToTime(String str,int i) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("strToTime", "strToTime:" + str);
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
				Locale.getDefault());
		format.setTimeZone(TimeZone.getDefault());
		Date date = null;

		try {
			date = format.parse(str.toString());
		} catch (ParseException e) {
			return 0l;
		}
		return date.getTime()/1000;
	}
/* used for mutil-clock , DateTime convert to seconds without care timezone*/
   	public static long strToTimeEx(String str,int i) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("strToTime", "strToTime:" + str);
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
				Locale.getDefault());
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		Date date = null;

		try {
			date = format.parse(str.toString());
		} catch (ParseException e) {
			return 0l;
		}
		return date.getTime()/1000;
	}
   	public static String timeToDateStringEx(long timeSc , int i){
   		com.mediatek.wwtv.tvcenter.util.MtkLog.d("timeToStringEx", "timeToStringEx:" + timeSc);
   		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
				Locale.getDefault());
   		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
   		String date = null;
   		try {
   			date = format.format(timeSc);
		} catch (Exception e) {
			return "";
		}
   		com.mediatek.wwtv.tvcenter.util.MtkLog.d("timeToStringEx", "date = " + date);
   		return date;
   	}
   	public static String timeToTimeStringEx (long timeSc ,int i){
   		com.mediatek.wwtv.tvcenter.util.MtkLog.d("timeToTimeStringEx", "timeToTimeStringEx:" + timeSc);
//		 HH:mm:ss
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",
			Locale.getDefault());
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		String time = null;
		try {
			time = format.format(timeSc);
	} catch (Exception e) {
		return "";
	}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("timeToTimeStringEx", "time = " + time);
		return time;

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
	/*public static float speedTest(TimeShiftManager manager) {

		if (!manager.hasRemovableDisk()) {
			return -1f;
		}
		int index = (int) (Math.random() * 1000);

		if (manager.getPvrMP() == null&&manager.getDeviceList().size()>0) {
			manager.setPvrMP(manager.getDeviceList().get(0));

		}
		String path = String.format(manager.getPvrMP().mMountPoint + "/"
				+ "speedTest%d.dat", index);
		Util.showDLog("path:" + path);
		File testFile = new File(path);

		float maxSpeed = 0.0f;
		Long MinTime = Long.MAX_VALUE;

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

		byte[] writeByte = new byte[(int) (bufferSize)];

		FileOutputStream fis;
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

				if (MinTime > startTime2 - startTime1
						&& (startTime2 - startTime1) > 0) {
					MinTime = startTime2 - startTime1;
				}

				counts--;
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0f;
		} catch (IOException e) {
			e.printStackTrace();
			return 0f;
		} finally {
			testFile.delete();
		}

		endTime = System.currentTimeMillis();

		maxSpeed = (float) (bufferSize * 1000f / MinTime / 1024 / 1024);
		// System.out.println(String.format("MaxSpeed:%3.5f MB/s ", maxSpeed));
		maxSpeed = (new BigDecimal(maxSpeed).setScale(1,
				BigDecimal.ROUND_HALF_UP)).floatValue();
		System.out.println(String.format("MaxSpeed:%3.1f MB/s ", maxSpeed));

		float average = bufferSize * defaultCount * 1000L
				/ (endTime - startTime) / 1024 / 1024;
		System.out.println(String.format("average:%3.1f MB/s ", average));

		return average;
	}*/

	/**
	 * @param mp
	 * @return freeSize/Total Size +"GB"
	 */
	public static String getGBSizeOfDisk(MountPoint mp) {
		float tSize;
		float fSize;
		String str;
		if (mp != null) {
            StatFs stat = new StatFs(mp.mMountPoint);
            tSize = (float) stat.getTotalBytes() / (1024 * 1024 * 1024);
            fSize = (float) stat.getAvailableBytes() / (1024 * 1024 * 1024);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tSize=" + stat.getTotalBytes() + " fSize=" + stat.getAvailableBytes());
			str = String.format("%.1f/%.1f GB", fSize, tSize);
		} else {
			tSize = 0f;
			fSize = 0f;
			str = String.format(TurnkeyUiMainActivity.getInstance().getResources().getString(R.string.dvr_device_no));
		}

		return str;
	}

	/**
	 * @param item
	 * @return
	 */
	public static CharSequence getIsTshift(MountPoint item,Context context) {
		// TODO Auto-generated method stub
		StringBuilder diskProperityStr = new StringBuilder();
		CharSequence pvrStr = isPvrDisk(item,context);
		CharSequence tshiftStr = isTshiftDisk(item,context);
	 if (DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportPvr()){
		diskProperityStr.append(pvrStr);
		if (pvrStr.length() > 0 && tshiftStr.length() > 0) {
			diskProperityStr.append("/");
		}
	}
		diskProperityStr.append(tshiftStr);
		/*if(diskProperityStr.length()==0){
			diskProperityStr.append("PVR/TSHIFT");
		}*/
		return diskProperityStr.toString();

	}

	/*
	 * public static boolean isPvrSelectedDisk(MountPoint item){
	 * if(isPvrDisk(item).equalsIgnoreCase("")){ return false; }else { return
	 * true; }
	 *
	 * } public static boolean isTshiftSelectedDisk(MountPoint item){
	 * if(isTshiftDisk(item).equalsIgnoreCase("")){ return false; }else { return
	 * true; } }
	 */
	private static CharSequence isPvrDisk(MountPoint item,Context context) {
		String mountPoint = getPath(item.mMountPoint,context);
		File file = new File(mountPoint + Core.PVR_DISK_TAG);
		if (file.exists()) {
			Util.showDLog("isPvrDisk(),true, " + file.getAbsolutePath());
			return "PVR";
		} else {
			Util.showDLog("isPvrDisk(),false, " + file.getAbsolutePath());
			return "";
		}
	}

	private static CharSequence isTshiftDisk(MountPoint item,Context context) {
		String mountPoint = getPath(item.mMountPoint,context);
		File file = new File(mountPoint + Core.TSHIFT_DISK_TAG);
		if (file.exists()) {
			Util.showDLog("isTshiftDisk(),true, " + file.getAbsolutePath());
			return "TSHIFT";
		} else {
			Util.showDLog("isTshiftDisk(),false, " + file.getAbsolutePath());
			return "";
		}
	}

	/*public static void itemToStringArray(ScheduleItem item) {
		System.out.println(item.getDate().toLocaleString());

		Calendar ca = Calendar.getInstance();
		ca.clear();
		ca.set(2012, 9, 05);

		System.out.println(ca.get(Calendar.YEAR));
		System.out.println(ca.get(Calendar.MONTH));
		System.out.println(ca.get(Calendar.DATE));

		dataFormat(ca.getTime());
	}
*/
	public static void dataFormat(Date date) {
		SimpleDateFormat format = new SimpleDateFormat(
				"HH:mm:ss,EEEEEEEE,yyyy/MM/dd");
		System.out.println(format.format(date));
	}

	public static void longToDate(Long time) {
		Date date = new Date(time);
		dataFormat(date);
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

		Util.showDLog("FreeSize:" + fsize);

		if (count <= 1) {
			Util.showELog("There is not enough space.");
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

	public static void tempSetPVR(String diskPath) {
         android.util.Log.d("disksetting",diskPath);
		String changeDeleteFile = fixDiskPath(diskPath);
		File file = new File(diskPath+ Core.PVR_DISK_TAG);
        android.util.Log.d("disksetting","file-->"+file);
		boolean setSuccessFul = false;
		if (!file.exists()) {
			try {
				setSuccessFul = file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Util.showDLog("tempSetPVR(),setSuccessFul:" + setSuccessFul);
		} else {
			Util.showDLog("tempSetPVR(),file.exists():" + changeDeleteFile);
		}
        if(setSuccessFul){
            if(TurnkeyUiMainActivity.getInstance()!=null){
                SaveValue.writeWorldStringValue(TurnkeyUiMainActivity.getInstance(),"PVR_TAG",diskPath,true);
                } 
            String path = diskPath.replace("Android/data/com.mediatek.wwtv.tvcenter/files/Movies", "");
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"diskPath-->"+path+",-->"+path.replace("storage","mnt/media_rw"));
            MtkTvRecordBase.setDisk( path.replace("storage","mnt/media_rw"));         
        }
        /*
         * File file1 = new File(diskPath + Core.TSHIFT_DISK_TAG); if(file1.exists()){ try {
         * setSuccessFul = file1.delete(); } catch (Exception e) { e.printStackTrace(); }
         * Util.showDLog("deletetime(),setSuccessFul:" + setSuccessFul); }
         */
	}

	public static void tempSetTHIFT(String diskPath) {
		//String changeDeleteFile = fixDiskPath(diskPath);
		File file = new File(diskPath+ Core.TSHIFT_DISK_TAG);
		boolean setSuccessFul = false;
		if (!file.exists()) {
			try {
				setSuccessFul = file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Util.showDLog("tempSetTHIFT(),setSuccessFul:" + setSuccessFul);
		} else {
			Util.showDLog("tempSetTHIFT(),file.exists():" + diskPath);
		}
        /*
         * File file1 = new File(diskPath + Core.PVR_DISK_TAG); if(file1.exists()){ try {
         * setSuccessFul = file1.delete(); } catch (Exception e) { e.printStackTrace(); }
         * Util.showDLog("delete(),setSuccessFul:" + setSuccessFul); }
         */
	}

	/**
	 * check diskPath contains TSHIF_TAG or not
	 * @param diskPath
	 * @return
	 */
	public static boolean tempIsTSHIFT (String diskPath){
		File file =new File(diskPath+Core.TSHIFT_DISK_TAG);
		return file.exists() ;
	}
	/**
	 * check diskPath contains PVR_TAG  or not
	 * @param diskPath
	 * @return
	 */
	public static boolean tempIsPVR (String diskPath){
		File file =new File(diskPath+Core.PVR_DISK_TAG);
		return file.exists() ;
	}

	/**
	 * delete the specified TSHIFT file path
	 * @param diskPath
	 */
	public static void tempDelTSHIFT(MountPoint point, StorageManager storageManager,String diskPath) {
		String changeDeleteFile = fixDiskPath(point.mMountPoint);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteSelectedFile,changeDeleteFile=" + changeDeleteFile);
		File tshiftTag = new File(diskPath + Core.TSHIFT_DISK_TAG);
		File tshiftDir = new File(diskPath + Core.TSHIFT_DIR_DISK);
        if(tshiftTag.exists()){
            boolean result = tshiftTag.delete();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deletetime(),delete timeshift tag file setSuccessFul:" + result);
        }

		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deletetime(),delete dir exists: " + tshiftDir.exists() + ", isDirectory: " + tshiftDir.isDirectory());
		if(tshiftDir.exists() && tshiftDir.isDirectory()){
		    for (File f : tshiftDir.listFiles()) {
                f.delete();
            }
		    boolean setSuccessFul2 = tshiftDir.delete();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deletetime(),delete timeshift directory setSuccessFul:" + setSuccessFul2);
 
            if (setSuccessFul2) {
                TifTimeShiftManager.getInstance().stop();
                TifTimeShiftManager.getInstance().stopAll();
                MtkTvTimeshift.getInstance().setAutoRecord(false);
                MtkTvTimeshift.getInstance().setAutoRecord(true);
                unmountAndMountDisk(point.mDeviceName, storageManager);
            }
        }
	}

	private static void unmountAndMountDisk(final String diskPath, final StorageManager storageManager) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("unmountAndMountDisk", "usb_path:" + diskPath);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("unmountAndMountDisk", "start unmountVolume.");
                    storageManager.unmount(diskPath);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("unmountAndMountDisk", "end unmountVolume.");
                    Thread.sleep(1000);
                    storageManager.mount(diskPath);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("unmountAndMountDisk", "result_mount:");
                } catch (Exception e) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e("unmountAndMountDisk", "Exception:" + e.getMessage());
                }

            }
        }).start();
    }

	/**
	 * delete the specified PVR file path
	 * @param diskPath
	 */
	public static void tempDelPVR (String diskPath){
		//String changeDeleteFile = fixDiskPath(diskPath);
		File file1 = new File(diskPath+ Core.PVR_DISK_TAG);
		boolean setSuccessFul =false;
		if(file1.exists()){
			try {
				setSuccessFul = file1.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Util.showDLog("delete(),setSuccessFul:" + setSuccessFul);
		}
	}
	/**
	 * change /storage/ to /mnt/media_rw/, /storage/ can not write
	 * @param diskPath path get from mountPoint
	 * @return path should action on
	 */
	private static String fixDiskPath(String diskPath){
        //change /storage/ to /mnt/media_rw/, /storage/ can not write
        String subPath = diskPath.substring(9, diskPath.length());
		return "/mnt/media_rw/" + subPath;
	}

	public static void showRecordToast(Activity activity) {
		// Toast.makeText(activity,
		// "Prepare pvr files,will start record after 10s",
		// Toast.LENGTH_LONG).show();
	}



	public static void commonAimationIn(Context context, View view, int animID) {
		Animation anim = AnimationUtils.loadAnimation(context, animID);
		anim.setFillAfter(true);
		anim.setFillBefore(false);
		anim.setRepeatCount(-1);
		// anim.setStartOffset(startOffset);

		view.startAnimation(anim);
		Util.showDLog("commonAimationIn:" + view.getId());
	}

	/**
	 * @param context
	 * @param rect
	 *            (float fromXDelta, float toXDelta, float fromYDelta, float
	 *            toYDelta)
	 * @param duration
	 * @return
	 */
	public static TranslateAnimation commonAimationIn(Context context,
			float fromX,float endX, Long duration) {
		TranslateAnimation anim = new TranslateAnimation(fromX, endX, 0f, 0f);

		anim.setFillAfter(true);
		anim.setFillBefore(true);
		anim.setDuration(duration);
		anim.setRepeatCount(-1);
		return anim;
	}

	/**
	 * @param context
	 * @param rect
	 *            (float fromXDelta, float toXDelta, float fromYDelta, float
	 *            toYDelta)
	 * @param duration
	 * @return
	 */
	public static TranslateAnimation commonAimationIn(Context context,
			float fromX, Long duration) {
		TranslateAnimation anim = new TranslateAnimation(fromX, -500f, 0f, 0f);

		anim.setFillAfter(true);
		anim.setFillBefore(true);
		anim.setDuration(duration);
		anim.setRepeatCount(-1);
		return anim;
	}

	public static TranslateAnimation commonAimationIn(Context context,
			float offsetX) {
		TranslateAnimation anim = new TranslateAnimation(0, offsetX, 0f, 0f);

		Util.showDLog("commonAimationIn,offsetX: " + offsetX);

		anim.setFillAfter(true);
		anim.setFillBefore(true);
		anim.setDuration(0L);
		anim.setRepeatCount(-1);
		return anim;
	}

	public static boolean fomatDisk(MountPoint mountPoint) {
		try {
				DeviceManager dm = DeviceManager.getInstance();
				if(mountPoint==null){
					return false;
				}
					
            String externSd = mountPoint.mDeviceName;
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "label:" + mountPoint.mVolumeLabel);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "mp:" + mountPoint.mMountPoint);
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "devname:" + mountPoint.mDeviceName);
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "usb_path:" + externSd);
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "start unmountVolume.");
				DiskSettingSubMenuDialog.setFormat(true);
				dm.umountVol(externSd);
				DiskSettingSubMenuDialog.setFormat(false);
				//dm.unmountVolume(extern_sd, true, true);
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "end unmountVolume.");
				Thread.sleep(4000);
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "start formatVolume");
				int resultFormat =dm.formatVol("fat32", mountPoint.mDeviceName); //DMRemoteservice.formatVol(extern_sd);
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "end formatVolume");
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "format result_:" + resultFormat);
				if (resultFormat == 0) {
					Thread.sleep(4000);
					dm.mountVol(externSd);
					com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "result_mount:");
				}
			//}
			return true;
		} catch (Exception e) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.e("fomatDisk", "Exception:" + e.getMessage());
			return false;
		}
	}

	public static void removeTshiftTag(String diskPath) {
		Util.showDLog("removeTshiftTag(),diskPath:?," + diskPath);
		File file = new File(diskPath + Core.TSHIFT_DISK_TAG);
		file.delete();
	}

    public static boolean isTTSEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enableServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo mEnableService : enableServices) {
            if (mEnableService.getId().contains(TALKBACK_SERVICE)) {
                return true;
            }
        }

        return false;
    }
	  public static byte [] stringToByte(String s){
          byte[] b = new byte[3];
          byte[] bytes = s.getBytes();
          for (int i = 0; i < bytes.length; i++) {
              System.out.println(i+"  = "+Integer.toBinaryString(bytes[i]-48));
          }
          b[0] = (byte)(((bytes[0]-48)*16)|(bytes[1]-48));
          b[1] = (byte)(((bytes[2]-48)*16)|(bytes[3]-48));
          b[2] = (byte)(((bytes[4]-48)*16)|(0x0F));
          return b;
      }
	  
	  public static String formatTime24To12(String strTime){
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime24To12-----> strTime="+strTime );
	        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
	        String formatTime="";
	        try{
	            Date time=sdf.parse(strTime);
	            SimpleDateFormat formatSdf=new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
	            formatTime=formatSdf.format(time);
	        }
	        catch(Exception e){
	            e.printStackTrace();
	        }
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime24To12-----> formatTime="+formatTime );
	        return formatTime;
	    }

	  public static String formatTime12T24(String strTime){
	        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
	        String formatTime="";
	        try{
	            Date time=sdf.parse(strTime);
	            SimpleDateFormat formatSdf=new SimpleDateFormat("HH:mm");
	            formatTime=formatSdf.format(time);
	        }
	        catch(Exception e){
	            e.printStackTrace();
	        }
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime12_24-----> formatTime="+formatTime );
	        return formatTime;
	    }
	  public String toString(){
		  return "Util";
	  }

  public static String getPath(String selectPath,Context mContext){
    String path = "";
    File[]  files = mContext.getExternalFilesDirs(Environment.DIRECTORY_MOVIES);
    for(File f:files){
        android.util.Log.d(TAG,"files-->"+f);
        path = ""+f;
        if(path.contains(selectPath)){    
           return path;
        }
    }
    path = path.replace("storage","mnt/media_rw");
    android.util.Log.d(TAG,"path->"+path);
    return path;
    }
}
