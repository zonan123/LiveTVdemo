package com.mediatek.wwtv.setting.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

/**
 * verify the cwkey input char
 * @author sin_biaoqinggao
 *
 */
public class CWKeyTextWatcher implements TextWatcher{

	final static String TAG = "CWKeyTextWatcher" ;
	Context mContext ;

	/*public CWKeyTextWatcher(){

	}*/

	public CWKeyTextWatcher(Context context){
		mContext = context;
	}

	@Override
	public void afterTextChanged(Editable s) {
		Log.d(TAG, "afterTextChanged-editString :"+s.toString());
		if(s.length() > 0){
			int pos = s.length()-1;
			char ch = s.charAt(pos);
			if(ch >='0' && ch <='9'){
				Log.d(TAG, ":"+ch);
			}else if(ch >='A' && ch <='F'){
				Log.d(TAG, ":"+ch);
			}else if(ch >='a' && ch <='f'){
				Log.d(TAG, ":"+ch);
			}else{
				s.delete(pos, pos+1);
				Toast.makeText(mContext, genTipStr(ch), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,int after) {
		Log.d(TAG, "beforeTextChanged :"+s.toString());

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.d(TAG, "onTextChanged :"+s.toString());
	}

	private String genTipStr(char ch){
		String f = "Charactor '";
		String e = "' is invalid ! you only can input 0-9 or a-f or A-F";
		return f+ch+e;
	}

}
