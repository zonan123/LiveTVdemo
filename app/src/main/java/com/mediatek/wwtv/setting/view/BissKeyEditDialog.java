package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.preference.Preference;


import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

//import android.support.v7.preference.Preference;

public class BissKeyEditDialog extends Dialog implements TextView.OnEditorActionListener,DialogInterface.OnShowListener{
    private Context mContext;
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;

    public static final String TAG = "EditTextActivity";
    public static final String EXTRA_DESC = "description";
    public static final String EXTRA_INITIAL_TEXT = "initialText";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_ITEMID = "itemId";
    public static final String EXTRA_DIGIT = "isDigit";
    public static final String EXTRA_CANFLOAT = "canFloat";
    public static final String EXTRA_LENGTH = "length";
    public static final String EXTRA_CAN_WATCH_TEXT = "canWatchText";
    private EditText mEditText ;
    private String itemID ;
    private String mDescString;
    private Preference preference;
    private int length=-1;
    private int mInputType = -1;
    public BissKeyEditDialog(Context context,String descString, String itemId) {
        super(context, R.style.Theme_TurnkeyCommDialog);
        mContext = context;
        mDescString=descString;
        this.itemID=itemId;
        window = getWindow();
        lp = window.getAttributes();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edittext_activity);
        width = (int) (ScreenConstant.SCREEN_WIDTH * 0.55);
        lp.width = width;
        height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.6);
        lp.height = height;
        lp.x = 0 - lp.width / 3;
        window.setAttributes(lp);
        this.setOnShowListener(this);

        init();
    }

    public void setPositon(int xoff, int yoff) {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.x = xoff;
            lp.y = yoff;
            window.setAttributes(lp);
      }

    public void setPreference(Preference preference){
        this.preference=preference;
        if(!TextUtils.isEmpty(preference.getSummary())&&mEditText!=null) {
            mEditText.setText(preference.getSummary());
            mEditText.setSelection(preference.getSummary().length());
        }
    }
    public void setLength(int length){
        this.length=length;
    }
    public int getLength(){
        return this.length;
    }
    public void init() {
        TextView description = (TextView) this.findViewById(R.id.description);
        description.setText(mDescString);
        description.setVisibility(View.VISIBLE);

        mEditText = (EditText) this.findViewById(R.id.edittext);
        mEditText.requestFocus();
        mEditText.setOnEditorActionListener(this);
        if(mInputType != -1){
        	mEditText.setInputType(mInputType);	
        }
        //mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
        if(preference!=null&&!TextUtils.isEmpty(preference.getSummary())) {
            mEditText.setText(preference.getSummary());
            mEditText.setSelection(preference.getSummary().length());
        }
        if(length !=-1){
            InputFilter [] filter ={new InputFilter.LengthFilter(length)};
            mEditText.setFilters(filter);
        }
    }
    public void setInputType(int inputtype) {
        mInputType = inputtype;
    }

    @Override
    public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
        if(EditorInfo.IME_ACTION_DONE==actionId){
            if(checkLengthIsEnough()){
                 if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_FREQ)){
                    initPreferece(MenuConfigManager.BISS_KEY_FREQ_MIN,MenuConfigManager.BISS_KEY_FREQ_MAX);
                 }else if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_SYMBOL_RATE)){
                    initPreferece(MenuConfigManager.BISS_KEY_SYMBOL_RATE_MIN,MenuConfigManager.BISS_KEY_SYMBOL_RATE_MAX);
                 }else if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_SVC_ID)){
                     initPreferece(MenuConfigManager.BISS_KEY_SVC_ID_MIN,MenuConfigManager.BISS_KEY_SVC_ID_MAX);
                 }else if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_CW_KEY)){
                     preference.setSummary(String.valueOf(mEditText.getText().toString()));
                 }
                 cancel();
               }
            return true;
        }
        return false;
    }
    private void initPreferece(int min,int max) {
        String value=mEditText.getText().toString();
        int now = Integer.parseInt(value);
        if(now <min){
            now = MenuConfigManager.BISS_KEY_FREQ_MIN;
        }else if(now >max){
            now = max;
        }
         preference.setSummary(String.valueOf(now));
    }

    private boolean checkLengthIsEnough(){
        String ret = mEditText.getText().toString();
        if(!TextUtils.isEmpty(ret)){
            if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_CW_KEY)){
                if(ret.length()<16){
                    Toast.makeText(mContext, "cwkey can't less than 16", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }else{
            Toast.makeText(mContext, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onShow(DialogInterface arg0) {
        if(preference!=null&&!TextUtils.isEmpty(preference.getSummary())) {
            mEditText.setText(preference.getSummary());
            mEditText.setSelection(preference.getSummary().length());
        }
    }
}
