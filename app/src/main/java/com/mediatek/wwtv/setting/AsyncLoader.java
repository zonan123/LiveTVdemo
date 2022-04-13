package com.mediatek.wwtv.setting;

import android.os.AsyncTask;

public class AsyncLoader {

	private DataLoadListener mListener;
	DataAsyncTask mAsyncTask;
	static AsyncLoader mSelf ;

	static {
		mSelf = new AsyncLoader();
	}

	public static AsyncLoader getInstance(){
		return mSelf;
	}
	
	private AsyncLoader(){
		
	}
	
	public void execute(Object params){
		mAsyncTask = new DataAsyncTask();
		mAsyncTask.execute(params);
	}
	public void cancelTask(){
		if(mAsyncTask != null && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
			mAsyncTask.cancel(true);
		}
	}
	
	public void bindDataLoadListener(DataLoadListener listener){
		mListener = listener;
	}
	
	public boolean isTaskRunning(){
		return mAsyncTask != null && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING ? true:false;
	}
	
	public interface DataLoadListener{
		void loadData();
		void loadFinished();
		void loadStarting();
	}
	
	class DataAsyncTask extends AsyncTask<Object,Object,Object>{

		@Override
		protected Object doInBackground(Object... params) {
			mListener.loadData();
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			mListener.loadFinished();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			mListener.loadStarting();
			super.onPreExecute();
		}

	}
}
