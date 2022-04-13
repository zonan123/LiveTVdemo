package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.List;

import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;

public interface EPGEuIView {
	
	void updateEventDetails(EPGProgramInfo programInfo);
	
	void updateChannelList(List<EPGChannelInfo> channelList,int selectIndex,int page);
	
	void updateProgramList(List<EPGProgramInfo> programList);
	
	void showLoading();
	
	void dismissLoading();
	
	void updateLockStatus(boolean isLocked);

}
