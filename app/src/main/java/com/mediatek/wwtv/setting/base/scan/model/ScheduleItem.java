package com.mediatek.wwtv.setting.base.scan.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;




public class ScheduleItem  implements Comparable<Object>{


	public final static int SCHEDULE_TYPE_RECORD = 0;
	public final static int SCHEDULE_TYPE_REMINDER = 1;

	public final static int REPEAT_TYPE_ONCE = 0;
	public final static int REPEAT_TYPE_WEEK = 1;
	public final static int REPEAT_TYPE_DAILY = 2;

	public final static String CHNNEL_NUM = "CHNNEL_NUM";
	public final static String START_TIME = "START_TIME";
	public final static String END_TIME = "END_TIME";

	private int taskID = 0;

	private String srcType = "DTV";
	private String channelName = "";
	private int channelNum = 0;
	private Date date = new Date();
	private Date startTime = new Date();
	private Date endTime = new Date();
	private Long duration = 0L;
	private int scheduleType = 0;
	private int repeatType = 0;
	private int channelID = 0;

	private int weeklyRepeat = 0;
	private boolean isEnble = true;

	private boolean isNewItem = false;

	private Map<String, Boolean> weekList = new HashMap<String, Boolean>();

	private final static String TAG = "ScheduleItem";
	private final static Boolean DEBUG = true;

	public Map<String, Boolean> getWeekList() {
		weekList.put("Monday", true);
		return weekList;

	}

	public void setWeekList(Map<String, Boolean> weekList) {
		this.weekList = weekList;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public int getChannelNum() {
		return channelNum;
	}

	public void setChannelNum(int channelNum) {
		this.channelNum = channelNum;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public int getRemindType() {
		return scheduleType;
	}

	/**
	 * schedule item:scheduleType
	 * 
	 * @param scheduleType
	 */
	public void setRemindType(int scheduleType) {
		this.scheduleType = scheduleType;
	}

	public int getRepeatType() {
		return repeatType;
	}

	public void setRepeatType(int repeatType) {
		this.repeatType = repeatType;
	}

	/**
	 * @return the weeklyRepeat
	 */
	public int getWeeklyRepeat() {
		return weeklyRepeat;
	}

	/**
	 * @param weeklyRepeat
	 *            the weeklyRepeat to set
	 */
	public void setWeeklyRepeat(int weeklyRepeat) {
		this.weeklyRepeat = weeklyRepeat;
	}

	/**
	 * @return the srcType
	 */
	public String getSrcType() {
		return srcType;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the dbID
	 */
	public int getTaskID() {
		return taskID;
	}

	/**
	 * @param dbID
	 *            the dbID to set
	 */
	public void setTaskID(int dbID) {
		this.taskID = dbID;
	}

	/**
	 * @param srcType
	 *            the srcType to set
	 */
	public void setSrcType(String srcType) {
		this.srcType = srcType;
	}

	/**
	 * @return the isEnble
	 */
	public boolean isEnble() {
		return isEnble;
	}

	/**
	 * @param isEnble
	 *            the isEnble to set
	 */
	public void setEnble(boolean isEnble) {
		this.isEnble = isEnble;
	}

	public int getChannelID() {
		return channelID;
	}

	public void setChannelID(int channelID) {
		this.channelID = channelID;
	}

	/**
	 * @return the isNewItem
	 */
	public boolean isNewItem() {
		return isNewItem;
	}

	/**
	 * @param isNewItem
	 *            the isNewItem to set
	 */
	public void setNewItem(boolean isNewItem) {
		this.isNewItem = isNewItem;
	}

	public void showDebugInfo() {
		if (DEBUG) {
			try {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "srcType:" + srcType);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelNum:" + channelNum);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "date:" + date.toGMTString());
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime:" + startTime.toGMTString());
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "endTime:" + endTime.toGMTString());
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "duration:" + duration);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "srcType:" + srcType);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scheduleType:" + scheduleType);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "repeatType:" + repeatType);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "weeklyRepeat:" + weeklyRepeat);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnble:" + isEnble);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelID:" + channelID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int compareTo(Object another) {
		// TODO Auto-generated method stub
		long one = getStartTime().getTime();
		long two = ((ScheduleItem) another).getStartTime().getTime();
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("compareTo", "sItem:" + one + "____:" + two);
		return (int) (one - two);
	}


}
