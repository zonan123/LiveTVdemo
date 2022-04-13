package com.mediatek.wwtv.setting;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.base.scan.ui.BaseCustomActivity;
import com.mediatek.wwtv.setting.util.CWKeyTextWatcher;
import com.mediatek.wwtv.setting.util.MenuConfigManager;


/**
 * this activity is used to do text input
 * @author sin_biaoqinggao
 *
 */
public class EditTextActivity extends BaseCustomActivity implements TextWatcher, TextView.OnEditorActionListener {

	
	public static final String TAG = "EditTextActivity";
    //private static final String EXTRA_LAYOUT_RES_ID = "layout_res_id";
    //private static final String EXTRA_EDIT_TEXT_RES_ID = "edit_text_res_id";
    public static final String EXTRA_DESC = "description";
    public static final String EXTRA_INITIAL_TEXT = "initialText";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_ITEMID = "itemId";
    public static final String EXTRA_DIGIT = "isDigit";
    public static final String EXTRA_CANFLOAT = "canFloat";
    public static final String EXTRA_LENGTH = "length";
    public static final String EXTRA_CAN_WATCH_TEXT = "canWatchText";
    public static final String EXTRA_ALLOW_EMPTY = "allowEmpty";
    public static final String TYPE_CLASS_TEXT = "class_text";
	public static final String EXTRA_FLAG_SIGNED = "flag_signed";
	public static final String EXTRA_HINT_TEXT = "hint_text";
    
    private TextWatcher mTextWatcher = null;
    private TextView.OnEditorActionListener mEditorActionListener = null;

    private EditText mEditText ;
    private String itemID ;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.edittext_activity);
        mEditText = (EditText) this.findViewById(R.id.edittext);

        itemID = getIntent().getStringExtra(EXTRA_ITEMID);
        String descString = getIntent().getStringExtra(EXTRA_DESC);
        if (!TextUtils.isEmpty(descString)) {
            TextView description = (TextView) this.findViewById(R.id.description);
            if (description != null) {
                description.setText(descString);
                description.setVisibility(View.VISIBLE);
            }
        }

        if (mEditText != null) {
        	mEditText.setOnEditorActionListener(this);
        	mEditText.addTextChangedListener(this);
        	mEditText.requestFocus();
            if (getIntent().getBooleanExtra(EXTRA_PASSWORD, false)) {
            	mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            if(getIntent().getBooleanExtra(EXTRA_DIGIT, false)){
            	mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
            }
            if(getIntent().getBooleanExtra(EXTRA_CANFLOAT, false)){
            	mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
			if(getIntent().getBooleanExtra(EXTRA_FLAG_SIGNED, false)){
                mEditText.setInputType(mEditText.getInputType()|InputType.TYPE_NUMBER_FLAG_SIGNED);
            }
            if(getIntent().getBooleanExtra(TYPE_CLASS_TEXT, false)){
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_NORMAL);
            }
            int length = getIntent().getIntExtra(EXTRA_LENGTH, -1);
            if(length !=-1){
            	InputFilter [] filter ={new InputFilter.LengthFilter(length)};
            	mEditText.setFilters(filter);
            }
            String initialText = getIntent().getStringExtra(EXTRA_INITIAL_TEXT);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initialText:" + initialText);
            if(!TextUtils.isEmpty(initialText)) {
            	mEditText.setText(initialText);
            	mEditText.setSelection(initialText.length());
            }
            String hint = getIntent().getStringExtra(EXTRA_HINT_TEXT);
            if(!TextUtils.isEmpty(hint)){
                mEditText.setHint(hint);
            }
            if(getIntent().getBooleanExtra(EXTRA_CAN_WATCH_TEXT, false)){
            	if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_CW_KEY)){
            		setTextWatcher(new CWKeyTextWatcher(this));
            	}
            }
        }
	}

	public void setTextWatcher(TextWatcher textWatcher) {
        mTextWatcher = textWatcher;
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        mEditorActionListener = listener;
    }


    @Override
    public void afterTextChanged(Editable s) {
        if (mTextWatcher != null) {
            mTextWatcher.afterTextChanged(s);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mTextWatcher != null) {
            mTextWatcher.beforeTextChanged(s, start, count, after);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mTextWatcher != null) {
            mTextWatcher.onTextChanged(s, start, before, count);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEditorAction-actionId:"+actionId);
    	switch(actionId){
    	case EditorInfo.IME_ACTION_DONE:
    		if(checkLengthIsEnough()){
                hideInputMethod();
    			Intent data = new Intent();
    			if("".equals(mEditText.getText().toString()) && getIntent().getBooleanExtra(EXTRA_ALLOW_EMPTY, false)){
    			    data.putExtra("value", "-1");//empty value
    			}else {
    			    data.putExtra("value", mEditText.getText().toString());
                }
            	this.setResult(RESULT_OK, data);
            	finish();
    		}
    		return true;
    	default: break;
    	}
        if (mEditorActionListener != null) {
            return mEditorActionListener.onEditorAction(v, actionId, event);
        } else {
            return false;
        }
    }
    
    private boolean checkLengthIsEnough(){
    	String ret = mEditText.getText().toString();
    	if(getIntent().getBooleanExtra(EXTRA_ALLOW_EMPTY, false)){
    	    return true;
    	}
		if(!TextUtils.isEmpty(ret)){
			if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_CW_KEY)){
	    		if(ret.length()<16){
	    			Toast.makeText(this, "cwkey can't less than 16", Toast.LENGTH_SHORT).show();
	    			return false;
	    		}
	    	}
		}else{
			Toast.makeText(this, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
    }

    private void hideInputMethod(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }
}