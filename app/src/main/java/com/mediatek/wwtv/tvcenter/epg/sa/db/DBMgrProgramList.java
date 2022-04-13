package com.mediatek.wwtv.tvcenter.epg.sa.db;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.tvcenter.epg.EPGUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * program list db manager 
 * @author sin_xinsheng
 *
 */
public class DBMgrProgramList {
	private static DBMgrProgramList instance;
	private DBHelperProgramList mDBHelperProgramList;
	private SQLiteDatabase mProgramDb;
	
	public DBMgrProgramList(Context context) {
		mDBHelperProgramList = new DBHelperProgramList(context);
	}
	
	public static synchronized DBMgrProgramList getInstance(final Context context) {
		if (instance == null) {
			instance = new DBMgrProgramList(context);
			RxBus.instance.onEvent(ActivityDestroyEvent.class)
				.filter(it -> it.activityClass == context.getClass())
				.firstElement()
				.doOnSuccess(it ->{
					synchronized (DBMgrProgramList.class) {
						instance = null;
					}
				}).subscribe();
		}
		return instance;
	}
	
	public void getReadableDB() {
		mProgramDb = mDBHelperProgramList.getReadableDatabase();
	}
	
	public void getWriteableDB() {
		mProgramDb = mDBHelperProgramList.getWritableDatabase();
	}
	
	public void addProgram(EPGBookListViewDataItem bookedProgram) {
		List<EPGBookListViewDataItem> mBookedList = getProgramListWithDelete();
		for (EPGBookListViewDataItem tempItem:mBookedList) {
			if (tempItem.mProgramStartTime == bookedProgram.mProgramStartTime) {
				deleteProgram(tempItem);
			}
		}
		ContentValues cv = new ContentValues();
		cv.put(ProgramColumn.CHANNEL_ID, bookedProgram.mChannelId);
		cv.put(ProgramColumn.PROGRAM_ID, bookedProgram.mProgramId);
		cv.put(ProgramColumn.CHANNEL_NO_NAME, bookedProgram.mChannelNoName);
		cv.put(ProgramColumn.PROGRAM_NAME, bookedProgram.mProgramName);
		cv.put(ProgramColumn.PROGRAM_START_TIME, bookedProgram.mProgramStartTime);
		mProgramDb.insert(DBHelperProgramList.PROGRAM_LIST_TABLE, null, cv);
	}
	
	public void deleteProgram(EPGBookListViewDataItem bookedProgram) {
		mProgramDb.delete(DBHelperProgramList.PROGRAM_LIST_TABLE, ProgramColumn.PROGRAM_START_TIME + "=? and " + ProgramColumn.CHANNEL_ID + "=? and " + ProgramColumn.PROGRAM_ID + "=?", new String[]{bookedProgram.mProgramStartTime + "", bookedProgram.mChannelId + "", bookedProgram.mProgramId + ""});
	}
	
	public void deleteAllPrograms() {
		mProgramDb.delete(DBHelperProgramList.PROGRAM_LIST_TABLE, null, null);
	}
	
	public List<EPGBookListViewDataItem> getProgramList() {
		Cursor c = mProgramDb.rawQuery("select * from " + DBHelperProgramList.PROGRAM_LIST_TABLE, null);
		List<EPGBookListViewDataItem> mBookedList = new ArrayList<EPGBookListViewDataItem>();
		EPGBookListViewDataItem tempInfo = null;
		while (c.moveToNext()) {
			tempInfo = new EPGBookListViewDataItem();
			tempInfo.mChannelId = c.getInt(c.getColumnIndex(ProgramColumn.CHANNEL_ID));
			tempInfo.mProgramId = c.getInt(c.getColumnIndex(ProgramColumn.PROGRAM_ID));
			tempInfo.mChannelNoName = c.getString(c.getColumnIndex(ProgramColumn.CHANNEL_NO_NAME));
			tempInfo.mProgramName = c.getString(c.getColumnIndex(ProgramColumn.PROGRAM_NAME));
			tempInfo.mProgramStartTime = c.getLong(c.getColumnIndex(ProgramColumn.PROGRAM_START_TIME));
			tempInfo.marked = true;
			mBookedList.add(tempInfo);
		}
		c.close();
		return mBookedList;
	}
	
	public List<EPGBookListViewDataItem> getProgramListWithDelete() {
		Cursor c = mProgramDb.rawQuery("select * from " + DBHelperProgramList.PROGRAM_LIST_TABLE, null);
		List<EPGBookListViewDataItem> mBookedList = new ArrayList<EPGBookListViewDataItem>();
		List<EPGBookListViewDataItem> mDeleteList = new ArrayList<EPGBookListViewDataItem>();
		EPGBookListViewDataItem tempInfo = null;
		while (c.moveToNext()) {
			tempInfo = new EPGBookListViewDataItem();
			tempInfo.mChannelId = c.getInt(c.getColumnIndex(ProgramColumn.CHANNEL_ID));
			tempInfo.mProgramId = c.getInt(c.getColumnIndex(ProgramColumn.PROGRAM_ID));
			tempInfo.mChannelNoName = c.getString(c.getColumnIndex(ProgramColumn.CHANNEL_NO_NAME));
			tempInfo.mProgramName = c.getString(c.getColumnIndex(ProgramColumn.PROGRAM_NAME));
			tempInfo.mProgramStartTime = c.getLong(c.getColumnIndex(ProgramColumn.PROGRAM_START_TIME));
			tempInfo.marked = true;
			if (tempInfo.mProgramStartTime < EPGUtil.getCurrentTime()) {
				mDeleteList.add(tempInfo);
			} else {
				mBookedList.add(tempInfo);
			}
		}
		for (EPGBookListViewDataItem tpInfo:mDeleteList) {
			deleteProgram(tpInfo);
		}
		c.close();
		return mBookedList;
	}
	
	public void deleteDB() {
		mProgramDb.execSQL("DROP TABLE IF EXISTS " + DBHelperProgramList.PROGRAM_LIST_TABLE);
	}
	
	public void deleteTable() {
		mProgramDb.execSQL("DROP TABLE IF EXISTS " + DBHelperProgramList.PROGRAM_LIST_TABLE);
	}
	
	public void closeDB() {
		mProgramDb.close();
	}

}
