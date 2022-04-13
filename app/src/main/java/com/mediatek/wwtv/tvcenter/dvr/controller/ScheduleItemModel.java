package com.mediatek.wwtv.tvcenter.dvr.controller;

public class ScheduleItemModel {
	
	public String titleString;
	public String contentString;
	public boolean isEnabled ;
	
	public void setTitle(String titleString){
		this.titleString = titleString;
	}
	
	public void setContent(String contentString){
		this.contentString = contentString;
	}

	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
	}
	public String getTitle(){
		return titleString;
	}
	
	public String getContent(){
		return contentString;
	}

	public boolean isEnabled(){
		return isEnabled;
	}
}
