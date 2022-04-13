package com.mediatek.wwtv.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.base.scan.ui.BaseCustomActivity;
import com.mediatek.wwtv.setting.util.SatDetailUI;


 public class LNBFreqEditActivity extends BaseCustomActivity implements TextWatcher, TextView.OnEditorActionListener {


     public static final String TAG = "LNBFreqEditActivity";
     public static final String EXTRA_DESC = "description";
     public static final String EXTRA_INITIAL_TEXT = "initialText";
     public static final String EXTRA_PASSWORD = "password";
     public static final String EXTRA_ITEMID = "itemId";
     public static final String EXTRA_DIGIT = "isDigit";
     public static final String EXTRA_CANFLOAT = "canFloat";
     public static final String EXTRA_LENGTH = "length";
     public static final String EXTRA_CAN_WATCH_TEXT = "canWatchText";
     public static final String EXTRA_ALLOW_EMPTY = "allowEmpty";
     public static final String EXTRA_LAYOUT_LNB_FREQ = "layout_lnb_freq";
     
     public static final int MAX_FREQ = 11800;
     public static final int MIN_FREQ = 8550;
     public static final int MIN_FREQ_SWITCH = 10700;
     public static final int MAX_FREQ_SWITCH = 12750;
     public static final int DEFAULT_SWITCH_FREQ = 11700;

     private TextView.OnEditorActionListener mEditorActionListener = null;

     private EditText mLowEditText;
     private EditText mHighEditText;
     private EditText mSwitchEditText;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.lnb_edittext_activity);
         mLowEditText = (EditText) this.findViewById(R.id.edittext_low_freq);
         mHighEditText = (EditText) this.findViewById(R.id.edittext_high_freq);
         mSwitchEditText = (EditText) this.findViewById(R.id.edittext_switch_freq);
         mSwitchEditText.setEnabled(false);
         if (mLowEditText != null) {
             int low = SatDetailUI.LNB_CONFIG[SatDetailUI.LNB_CONFIG.length - 1][0];
             int high = SatDetailUI.LNB_CONFIG[SatDetailUI.LNB_CONFIG.length - 1][1];
             int swit = SatDetailUI.LNB_CONFIG[SatDetailUI.LNB_CONFIG.length - 1][2];
             if(low != 0){
                 mLowEditText.setText(low+"");
                 mLowEditText.setSelection(mLowEditText.getText().length());
             }
             mLowEditText.addTextChangedListener(this);
             if(high != 0){
                 mHighEditText.setText(high+"");
             }
             if(swit != 0){
                 mSwitchEditText.setText(swit+"");
             }else {
                 mSwitchEditText.setText(DEFAULT_SWITCH_FREQ+"");
             }
             mLowEditText.setOnEditorActionListener(this);
             mHighEditText.setOnEditorActionListener(this);
             mSwitchEditText.setOnEditorActionListener(this);
             mLowEditText.requestFocus();
             if (getIntent().getBooleanExtra(EXTRA_PASSWORD, false)) {
                 mLowEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
             }
             if(getIntent().getBooleanExtra(EXTRA_DIGIT, false)){
                 mLowEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
             }
             if(getIntent().getBooleanExtra(EXTRA_CANFLOAT, false)){
                 mLowEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
             }
         }
     }

     public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
         mEditorActionListener = listener;
     }


     @Override
     public void afterTextChanged(Editable s) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "afterTextChanged:");
     }

     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "beforeTextChanged:");
     }

     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {
         mHighEditText.setText(s);
     }

     @Override
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEditorAction-actionId:"+actionId);
         if(actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE){
             switch (v.getId()) {
                 case R.id.edittext_low_freq:
                     if(!checkLengthIsEnough(v, false)){
                         return true;
                     }
                     break;
                 case R.id.edittext_high_freq:
                     if(checkLengthIsEnough(v, false)){
                         if(!mLowEditText.getText().toString().equals(mHighEditText.getText().toString())){
                             mSwitchEditText.setEnabled(true);
                             mSwitchEditText.requestFocus();
                         }else {
                             Intent data = new Intent();                     
                             data.putExtra("value-low", mLowEditText.getText().toString());
                             data.putExtra("value-high", mHighEditText.getText().toString());
                             this.setResult(RESULT_OK, data);
                             finish();
                             return true;
                        }
                     }else {
                        return true;
                    }
                     break;
                 case R.id.edittext_switch_freq:
                     if(checkLengthIsEnough(v, true)){
                         Intent data = new Intent();                     
                         data.putExtra("value-low", mLowEditText.getText().toString());
                         data.putExtra("value-high", mHighEditText.getText().toString());
                         if(!TextUtils.isEmpty(mSwitchEditText.getText().toString())){
                             data.putExtra("value-switch", mSwitchEditText.getText().toString());
                         }
                         this.setResult(RESULT_OK, data);
                         finish();
                     }else {
                         return true;
                    }
                     break;
                 default:
                     break;
             }
         }
         
         if (mEditorActionListener != null) {
             return mEditorActionListener.onEditorAction(v, actionId, event);
         } else {
             return false;
         }
     }

     private boolean checkLengthIsEnough(TextView et, boolean allowEmpty){
         String ret = et.getText().toString();
         boolean valid = true;
         if(!TextUtils.isEmpty(ret)){
             try {
                 int freq = Integer.parseInt(ret);

                 switch (et.getId()) {
                     case R.id.edittext_low_freq:
                         if(freq > MAX_FREQ || freq < MIN_FREQ){
                             valid = false;
                         }
                         break;
                    case R.id.edittext_high_freq:
                        if(freq > MAX_FREQ || freq < MIN_FREQ){
                            valid = false;
                        }
                        if(freq < Integer.parseInt(mLowEditText.getText().toString())){
                            valid = false;
                        }
                        break;
                    case R.id.edittext_switch_freq:
                        /*if(freq < Integer.parseInt(mLowEditText.getText().toString()) || freq > Integer.parseInt(mHighEditText.getText().toString())){
                            valid = false;
                        }*/
                        if(freq <= MIN_FREQ_SWITCH || freq >= MAX_FREQ_SWITCH){
                            valid = false;
                        }
                        break;
                    default:
                        break;
                }
                 
            } catch (Exception e) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception");
            }
         }else{
             if(allowEmpty){
                 return true;
             }
             valid = false;
         }
         
         if(!valid){
             Toast.makeText(this, R.string.menu_string_postal_code_toast_invalid_input, Toast.LENGTH_SHORT).show();
             return false;
         }
         return true;
     }
 }
