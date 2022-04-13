package com.mediatek.wwtv.tvcenter.epg;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.util.AttributeSet;
import android.content.Context;
import android.view.Gravity;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;


/**
 * Turn channel by digit key.
 * 
 * @author jg_guohuicui
 * 
 */
public class DigitTurnCHView extends FrameLayout {

	private static final String TAG="DigitTurnCHView";
	private static final int NAV_TIMEOUT= 3 * 1000;//3S
	private static final int MSG_INPUT_DIGIT_NUM=1;
	private OnDigitTurnCHCallback mDigitTurnCHCallback;
	private TextView mTxtNumKey;
	private Context mContext ;
	
	private String mStrInputNum = "";
	private final Handler mHandler = new Handler(){
		 @Override
		    public void handleMessage(Message msg) {
			 	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "msg.what="+msg.what);
			 	switch(msg.what){
			 		case MSG_INPUT_DIGIT_NUM:
			 			int inputDigit=0;
			 			try{
			 				inputDigit=Integer.parseInt(mStrInputNum);
			 				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "inputDigit="+inputDigit);
				 			mDigitTurnCHCallback.onTurnCH(inputDigit);
			 			}catch(Exception e){
			 				e.printStackTrace();
			 			}
			 			hideView();
			 			break;
			 			default:
			 			  break;
			 	}
		 }
	};
	public DigitTurnCHView(Context context) {
		super(context);
		mContext=context;
		initView();
	}

	public DigitTurnCHView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		initView();
	}

	public DigitTurnCHView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		initView();
	}

	private void initView() {
		mTxtNumKey=new TextView(mContext);
		mTxtNumKey.setTextColor(mContext.getResources().getColor(R.color.white));
		mTxtNumKey.setTextSize(mContext.getResources().getDimensionPixelOffset(R.dimen.digit_turn_text_size));
		FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		mTxtNumKey.setGravity(Gravity.CENTER);
		addView(mTxtNumKey,lp);
	}
	
	public void setOnDigitTurnCHCallback(OnDigitTurnCHCallback callback){
		mDigitTurnCHCallback=callback;
	}

	public interface OnDigitTurnCHCallback {
		void onTurnCH(int chDigit);
	}

    public void keyHandler(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyCode="+keyCode);
		inputNumKey(keyCode);
		if(getVisibility()!=View.VISIBLE){
      setVisibility(View.VISIBLE);
    }
		mTxtNumKey.setText(mStrInputNum);
		mHandler.removeMessages(MSG_INPUT_DIGIT_NUM);
		mHandler.sendEmptyMessageDelayed(MSG_INPUT_DIGIT_NUM,
				NAV_TIMEOUT);
    }
	public void resetData(){
		mStrInputNum="";
	}
	private void inputNumKey(int keyCode){
		int realNum=keyCode - KeyMap.KEYCODE_0;
		mStrInputNum=mStrInputNum+realNum;
		mStrInputNum=trimStartsWith0(mStrInputNum);
		mStrInputNum=mStrInputNum.length() > 4?mStrInputNum.substring(1, 5):mStrInputNum;
		
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mStrInputNum="+mStrInputNum);
	}
	public void hideView(){
		setVisibility(View.GONE);
		resetData();
		mHandler.removeMessages(MSG_INPUT_DIGIT_NUM);
	}
	
	
	private String trimStartsWith0(String inputStr){
		if(inputStr.startsWith("0")){
		  if(inputStr.length() == 1){
		    return "0";
		  }
			inputStr=inputStr.substring(1);
			trimStartsWith0(inputStr);
		}
		return inputStr;
	}
	
	
	
	

}
