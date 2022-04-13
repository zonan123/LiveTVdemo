package com.mediatek.wwtv.tvcenter.epg;

public interface IPageCallback {
	boolean hasPrePage();

	boolean hasNextPage();
	
	void onRefreshPage();

}
