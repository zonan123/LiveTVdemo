package com.mediatek.wwtv.tvcenter.epg.us;

public class ListItemData {
    
    //day
    //time
    //program name
    //isBlocked
    //isCC
	private int eventId;
    private int itemId;
    private String itemDay;
    private String itemTime;
    private String itemProgramName;
    private String itemProgramDetail;
    private String itemProgramType;
    private String programTime;
    private String programStartTime;
    private boolean isBlocked;
    private boolean isCC;
    private boolean valid=true;
    private long millsStartTime;
    private long millsDurationTime;
    private String textContent;
    
    public void setMillsStartTime(long startTime) {
    	millsStartTime = startTime;
    }
    
    public long getMillsStartTime() {
    	return millsStartTime;
    }
    
    public void setMillsDurationTime(long durationTime) {
    	millsDurationTime = durationTime;
    }
    
    public long getMillsDurationTime() {
    	return millsDurationTime;
    }
    
    public void setEventId(int eventid) {
    	eventId = eventid;
    }
    
    public int getEventId() {
    	return eventId;
    }
    
    public String getProgramStartTime() {
        return programStartTime;
    }
    public void setProgramStartTime(String programStartTime) {
        this.programStartTime = programStartTime;
    }
    public String getProgramTime() {
        return programTime;
    }
    public void setProgramTime(String programTime) {
        this.programTime = programTime;
    }
    public String getItemProgramDetail() {
        if (itemProgramDetail==null||itemProgramDetail.replace(" ", "").equals("")) {
            return "(No program detail).";
        }
        return itemProgramDetail;
    }
    public void setItemProgramDetail(String itemProgramDetail) {
        this.itemProgramDetail = itemProgramDetail;
    }
    public String getItemProgramType() {
        return itemProgramType;
    }
    public void setItemProgramType(String itemProgramType) {
        this.itemProgramType = itemProgramType;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean value) {
        this.valid = value;
    }
    public int getItemId() {
        return itemId;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    public String getItemDay() {
        return itemDay;
    }
    public void setItemDay(String itemDay) {
        this.itemDay = itemDay;
    }
    public String getItemTime() {
        return itemTime;
    }
    public void setItemTime(String itemTime) {
        this.itemTime = itemTime;
    }
    public String getItemProgramName() {
        return itemProgramName;
    }
    public void setItemProgramName(String itemProgramName) {
        this.itemProgramName = itemProgramName;
    }
    public boolean isBlocked() {
        return isBlocked;
    }
    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
    public boolean isCC() {
        return isCC;
    }
    public void setCC(boolean isCC) {
        this.isCC = isCC;
    }
    
    public String getItemProgramContent() {
      return textContent;
  }

    public void setItemProgramContent(String content) {
      textContent = content;
  }
}
