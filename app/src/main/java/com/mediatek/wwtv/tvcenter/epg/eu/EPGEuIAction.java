package com.mediatek.wwtv.tvcenter.epg.eu;

import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;

public interface EPGEuIAction {
	
	void getChannelList();
	
	void getProgramListByChId(EPGChannelInfo channelInfo,int dayNum, int startHour);
	
	void setActiveWindow(EPGChannelInfo channel, int dayNum, int startHour);
	
	void refreshDetailsInfo(EPGProgramInfo info,int channelId);
	
	boolean is3rdTVSource();
	
	boolean isCountryUK();
	
	boolean isCurrentSourceATV();
	
	void checkPWDShow();
	
	void clearActiveWindow();
	
	int getTimeType();//12_24
}
