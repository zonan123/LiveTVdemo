/**
 * @Description: TODO()
 */
package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.net.Uri;

import com.mediatek.wwtv.tvcenter.dvr.manager.Util;

/**
 *PVR file info.
 */
public class DVRFiles {

    private long mId = 0;
    private Uri progarmUri;
	private String channelNum = "";
	private String channelName = "";
	private String programName = "";
	private String date = "";
	private String week = "";
	private String time = "";
	private String fileName = "";
	private long duration = 0;
	private String durationStr = "";
	private String indexAndName="";
	public boolean isRecording=false;
	public boolean isPlaying=false;
	private String mDetailInfo = "";

    public void setmId(long mId) {
        this.mId = mId;
    }

    public long getmId() {
        return mId;
    }

    public Uri getProgarmUri() {
        return progarmUri;
    }

    public void setProgarmUri(Uri progarmUri) {
        this.progarmUri = progarmUri;
    }

    public long getDuration() {
        return duration;
    }

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getDurationStr() {
		if(durationStr.isEmpty()){
			return Util.secondToString((int)duration);
		}else
		{
			return durationStr;
		}
	}

	public void setDurationStr(String duration) {
		this.durationStr = duration;
	}

	public String getChannelNum() {
		return channelNum;
	}

	public void setChannelNum(String channelNum) {
		this.channelNum = channelNum;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIndexAndName() {
		return indexAndName;
	}

	public void setIndexAndName(String indexAndName) {
		this.indexAndName = indexAndName;
	}

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public void dumpValues() {
		StringBuilder sb=new StringBuilder();

		sb.append("channelNum:"+channelNum);
		sb.append(";channelName:"+channelName);
		sb.append(";programName:"+programName);
		sb.append(";date:"+date);
		sb.append(";week:"+week);
		sb.append(";time:"+time);
		sb.append(";fileName:"+fileName);
		sb.append(";duration:"+duration);
		sb.append(";durationStr:"+durationStr);
		sb.append(":mDetailInfo :" + mDetailInfo);
	}

	/**
	 * @return the mDetailInfo
	 */
	public String getmDetailInfo() {
		return mDetailInfo;
	}

	/**
	 * @param mDetailInfo the mDetailInfo to set
	 */
	public void setmDetailInfo(String mDetailInfo) {
		this.mDetailInfo = mDetailInfo;
	}
}
