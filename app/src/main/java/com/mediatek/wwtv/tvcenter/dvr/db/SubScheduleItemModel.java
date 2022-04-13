package com.mediatek.wwtv.tvcenter.dvr.db;

public class SubScheduleItemModel {

	public String itemString;
	
	public  boolean isChecked;
	
	public boolean isVisible;

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public String getItemString() {
		return itemString;
	}

	public void setItemString(String itemString) {
		this.itemString = itemString;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	
	
	
}
