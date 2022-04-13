package com.mediatek.wwtv.tvcenter.nav.util;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvTeletext;
import com.mediatek.twoworlds.tv.MtkTvKeyEvent;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopBlockBase;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextPageBase;
/**
 * util class for ttx module.
 * @author hs_lizhipi
 *
 */
public class TeletextImplement {

	private MtkTvTeletext mTeletext;
	private MtkTvKeyEvent mtkKeyEvent;
	private static TeletextImplement mInstance;

	private TeletextImplement(){
		mTeletext = MtkTvTeletext.getInstance();
		mtkKeyEvent = MtkTvKeyEvent.getInstance();
	}

	public static synchronized TeletextImplement getInstance(){
		if(mInstance == null){
			mInstance = new TeletextImplement();
		}
		return mInstance;
	}

	public int startTTX(int keycode){
	    int ret = 0;
        int dfbkeycode;
        dfbkeycode = mtkKeyEvent.androidKeyToDFBkey(keycode);
        ret = mtkKeyEvent.sendKeyClick(dfbkeycode);
        return ret;
	}

	public void stopTTX(final OnStopTTXCallback callback){
		new Thread(new Runnable(){
			@Override
			public void run() {
				int resultCode= mTeletext.stop();
				if(callback!=null){
					callback.onStopTTX(resultCode);
				}
			}
		}).start(); 
		
	}
	
	public void stopTTX(){
		stopTTX(null);
	}

	public boolean hasTopInfo(){
		return mTeletext.teletextHasTopInfo();
	}

	public MtkTvTeletextPageBase getCurrentTeletextPage() {
		return mTeletext.getCurrentTeletextPage();
	}

	public int setTeletextPage(MtkTvTeletextPageBase teletextPageAddr){
		return mTeletext.setTeletextPage(teletextPageAddr);
	}

	public List<TeletextTopItem> getTopList(){
		List<TeletextTopItem> blockList = new ArrayList<TeletextTopItem>();
		List<MtkTvTeletextTopBlockBase> tmpBlockList = mTeletext.getTeletextTopBlockList();
		if(tmpBlockList != null && !tmpBlockList.isEmpty()){
			for( MtkTvTeletextTopBlockBase block : tmpBlockList){
				blockList.add(new TeletextTopItem(block));
			}
		}
		return blockList;
	}
	public interface OnStopTTXCallback{
		void onStopTTX(int resultCode);
	}
}
