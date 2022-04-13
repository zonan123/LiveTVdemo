package com.mediatek.wwtv.tvcenter.util;

import android.content.Context;
import android.util.Log;

import com.mediatek.mtkaudiopatchmanager.MtkAudioPatchManager;

public final class AudioBTManager {

	  private static final String TAG = "AudioBTManager";
	  private static AudioBTManager mInstance;
	  private Context mContext;
	  private MtkAudioPatchManager mMtkAudioPatchManager;

	  private AudioBTManager(Context context) {
	    mContext = context;
	    mMtkAudioPatchManager = new MtkAudioPatchManager(mContext);
	  }

	  public synchronized static AudioBTManager getInstance(Context context) {
	    if (mInstance == null) {
	      mInstance = new AudioBTManager(context);
	    }
	    return mInstance;
	  }

	  public boolean createAudioPatch() {
			synchronized(AudioBTManager.class) {
	    	boolean reslut = mMtkAudioPatchManager.createAudioPatch();
	      Log.i(TAG,"createAudioPatch reslut = " + reslut);
	      return reslut;
			}
	  }

	  public boolean releaseAudioPatch() {
			synchronized(AudioBTManager.class) {
				boolean reslut = mMtkAudioPatchManager.releaseAudioPatch();
				Log.i(TAG,"releaseAudioPatch reslut = " + reslut);
				mMtkAudioPatchManager = null;
				mContext = null;
				mInstance = null;
				return reslut;
			}
	  }
}