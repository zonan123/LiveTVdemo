package com.mediatek.wwtv.tvcenter.epg.sa.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * booked progranlist db
 * @author sin_xinsheng
 *
 */
public class DBHelperProgramList extends SQLiteOpenHelper {

	public static final String  DATABASE_NAME 		= "bookedprogranlist.db";
	public static final int 	DATABASE_VERSION 	= 1;
	public static final String  PROGRAM_LIST_TABLE 		= "programlist";
	
	private static final String DATABASE_CREATE = "CREATE TABLE " + PROGRAM_LIST_TABLE +" ("
					+ ProgramColumn._ID+" integer primary key autoincrement,"
					+ ProgramColumn.CHANNEL_ID+" INTEGER,"
					+ ProgramColumn.PROGRAM_ID+" INTEGER,"
					+ ProgramColumn.CHANNEL_NO_NAME+" text,"
					+ ProgramColumn.PROGRAM_NAME+" text,"
					+ ProgramColumn.PROGRAM_START_TIME+" INTEGER);";
	
	public DBHelperProgramList(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + PROGRAM_LIST_TABLE);
		onCreate(db);
	}

}
