package com.mediatek.wwtv.tvcenter.epg.sa.db;

public class EPGBookListViewDataItem {
	public int mChannelId;
	public int mProgramId;
	public String mChannelNoName;
	public String mProgramName;
	public long mProgramStartTime;
	public boolean marked = true;

	public EPGBookListViewDataItem() {
    System.out.println(toString());
	}

	public EPGBookListViewDataItem(int channelId, int progranId, 
			String channelName, String programName, long startTime) {
		this.mChannelId = channelId;
		this.mProgramId = progranId;
		this.mChannelNoName = channelName;
		this.mProgramName = programName;
		this.mProgramStartTime = startTime;
	}

  @Override
  public String toString() {
    return "EPGBookListViewDataItem [mChannelId=" + mChannelId + ", mProgramId=" + mProgramId
        + ", mChannelNoName=" + mChannelNoName + ", mProgramName=" + mProgramName
        + ", mProgramStartTime=" + mProgramStartTime + ", marked=" + marked + "]";
  }
	
	
}
