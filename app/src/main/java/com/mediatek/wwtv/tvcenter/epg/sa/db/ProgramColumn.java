package com.mediatek.wwtv.tvcenter.epg.sa.db;

import android.provider.BaseColumns;

public class ProgramColumn implements BaseColumns {
	// Cloumns
	public static final String CHANNEL_ID = "channelId";
	public static final String PROGRAM_ID = "programId";
	public static final String CHANNEL_NO_NAME = "channelNoName";
	public static final String PROGRAM_NAME = "programName";
	public static final String PROGRAM_START_TIME = "programStartTime";

	public static final String[] PROJECTION = { _ID,// 0
			CHANNEL_ID,// 1
			PROGRAM_ID,// 2
			CHANNEL_NO_NAME,// 3
			PROGRAM_NAME,// 4
			PROGRAM_START_TIME, // 5
	};

}
