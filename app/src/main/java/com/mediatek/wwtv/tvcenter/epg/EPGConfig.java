package com.mediatek.wwtv.tvcenter.epg;

public class EPGConfig {
  public static final int mMaxDayNum = 8;
	public static final int mTimeSpan = 2;
	public static final int EPG_SYNCHRONIZATION_MESSAGE = 0x103;
	public static final int EPG_PROGRAMINFO_SHOW = 0x104;
	public static final int EPG_PROGRAM_STTL_SHOW = 0x105;
	public static final int EPG_UPDATE_CHANNEL_LIST = 0x106;
	public static final int EPG_GET_TIF_EVENT_LIST = 0x107;
	public static final int EPG_SET_LIST_ADAPTER = 0x108;
	public static final int EPG_NOTIFY_LIST_ADAPTER = 0x109;
	public static final int EPG_SHOW_LOCK_ICON = 0x110;
	public static final int EPG_SELECT_CHANNEL_COMPLETE = 0x111;
	public static final int EPG_UPDATE_API_EVENT_LIST = 0x112;
	public static final int EPG_INIT_EVENT_LIST = 0x113;
	public static final int EPG_SET_LOCK_ICON = 0x114;
	public static final int EPG_GET_EVENT_LIST_DELAY_LONG = 0x115;
	public static final int EPG_REFRESH_CHANNEL_LIST = 0x116;
    public static final int EPG_SETMUTE_SOUND = 0x117;
    
    public static final int EPG_CHANGING_CHANNEL = 0x118;
    public static final int EPG_CHANGING_CH_TIMEOUT = 0x119;
    
    public static final int EPG_MUTE_VIDEO_AND_AUDIO = 0x120;
    
    public static final int EPG_REFRESH_DETAILS = 0x121;
    
    public static final int EPG_SET_DETAILS_TV_STATUS = 0x122;
    
    public static final int EPG_TRY_UNLOCK = 0x123;
    public static final int EPG_GET_EVENT_IMMEDIATELY = 0x124;
    public static final int EPG_NEED_FINISH_EPG = 0x125;
    public static final int EPG_REFRESH_PVR_MARK = 0x126;
    public static final int EPG_PWD_TIMEOUT_DISMISS = 0x127;
	public static final int EPG_NEED_UPDATE_LOCAL_DAYNUM = 0x128;

	public static boolean init = true;
	public static final int FROM_KEYCODE_DPAD_LEFT = 21;
	public static final int FROM_KEYCODE_DPAD_RIGHT = 22;
	public static final int FROM_KEYCODE_MTKIR_RED = 23;
	public static final int FROM_KEYCODE_MTKIR_GREEN = 24;
	public static final int FROM_ANOTHER_STREAM = 25;
	public static final int AVOID_PROGRAM_FOCUS_CHANGE = 26;
	public static final int SET_LAST_POSITION = 27;
	public static final int FROM_KEYCODE_MTKIR_OTHER = -1;
	public static int SELECTED_CHANNEL_POSITION = 0;
	public static int FROM_WHERE = SET_LAST_POSITION;

	public static final int EPG_DATA_RETRIEVING = 4;
	public static final int EPG_DATA_RETRIEVAL_FININSH = 5;
	public static final int EPG_EVENT_REASON_SCHEDULE_UPDATE_REPEAT = 6;
	public static boolean avoidFoucsChange = false;
	public static final int EPG_LIST_ITME_COUNT = 6;
	public static final String PREFIX = "EPG_";

	@Override
	public String toString() {
	  System.out.println(""+EPG_MUTE_VIDEO_AND_AUDIO);
	  return super.toString();
	}
}
