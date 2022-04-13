package com.mediatek.wwtv.setting.base.scan.model;

public interface StateScheduleListCallback <T>{


	boolean switchToScheduleItemInfo(T t);
	boolean switchToScheduleList(T t);
	boolean onItemClick(T t);
}
