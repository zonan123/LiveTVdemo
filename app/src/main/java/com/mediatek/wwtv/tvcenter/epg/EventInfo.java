package com.mediatek.wwtv.tvcenter.epg;

public class EventInfo{
	public static int MAX_COMPONENT_INFO = 8;
	public static int MAX_EVENT_LINKAGE_INFO = 4;

	private int svlId;
	private int channelId;
	private int eventId;
	private long startTime;
	private long duration;
	private boolean caption;
	private boolean freeCaMode;
	private String eventTitle;
	private String eventDetail;
	private int guidanceMode;
	private String guidanceText;
	private int[] caSystemId = new int[4];
	private int eventCategoryNum;
	private int[] eventCategory = new int[8];
//	private EventComponent[] eventComponents = new EventComponent[MAX_COMPONENT_INFO];
//	private EventLinkage[] eventLinkage = new EventLinkage[MAX_EVENT_LINKAGE_INFO];

//	public EventInfo() {
//		super();
//	}

	public int getSvlId() {
		return svlId;
	}

	public void setSvlId(int svlId) {
		this.svlId = svlId;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isCaption() {
		return caption;
	}

	public void setCaption(boolean caption) {
		this.caption = caption;
	}

	public boolean isFreeCaMode() {
		return freeCaMode;
	}

	public void setFreeCaMode(boolean freeCaMode) {
		this.freeCaMode = freeCaMode;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(String eventTitle) {
		this.eventTitle = eventTitle;
	}

	public String getEventDetail() {
		return eventDetail;
	}

	public void setEventDetail(String eventDetail) {
		this.eventDetail = eventDetail;
	}

	public int getGuidanceMode() {
		return guidanceMode;
	}

	public void setGuidanceMode(int guidanceMode) {
		this.guidanceMode = guidanceMode;
	}

	public String getGuidanceText() {
		return guidanceText;
	}

	public void setGuidanceText(String guidanceText) {
		this.guidanceText = guidanceText;
	}

	public int[] getCaSystemId() {
		return caSystemId;
	}

	public void setCaSystemId(int[] caSystemId) {
		this.caSystemId = caSystemId;
	}

	public int getEventCategoryNum() {
		return eventCategoryNum;
	}

	public void setEventCategoryNum(int eventCategoryNum) {
		this.eventCategoryNum = eventCategoryNum;
	}

	public int[] getEventCategory() {
		return eventCategory;
	}

	public void setEventCategory(int[] eventCategory) {
		this.eventCategory = eventCategory;
	}

//	public EventComponent[] getEventComponents() {
//		return eventComponents;
//	}
//
//	public void setEventComponents(EventComponent[] eventComponents) {
//		this.eventComponents = eventComponents;
//	}
//
//	public EventLinkage[] getEventLinkage() {
//		return eventLinkage;
//	}
//
//	public void setEventLinkage(EventLinkage[] eventLinkage) {
//		this.eventLinkage = eventLinkage;
//	}


	public int describeContents() {
		return 0;
	}

}
