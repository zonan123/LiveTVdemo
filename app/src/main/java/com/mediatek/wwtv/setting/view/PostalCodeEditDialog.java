package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
//import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.preference.Preference;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvEWSPABase;
//import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.nav.util.MtkTvEWSPA;
//import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.util.MenuConfigManager;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
//import android.support.v7.preference.Preference;
public class PostalCodeEditDialog extends Dialog implements TextView.OnEditorActionListener,DialogInterface.OnShowListener{
    private Context mContext;
//    private int xOff;
//    private int yOff;
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;

    public static final String TAG = "EditTextActivity";
//    private static final String EXTRA_LAYOUT_RES_ID = "layout_res_id";
//    private static final String EXTRA_EDIT_TEXT_RES_ID = "edit_text_res_id";
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
    private Intent mIntent = null;
    private String defaultValue;
    private int length=-1;
    public PostalCodeEditDialog(Context context,String descString, String itemId) {
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
//      Window window = getWindow();
//      WindowManager.LayoutParams lp = window.getAttributes();
//      lp.width= ScreenConstant.SCREEN_WIDTH;
//      lp.height= ScreenConstant.SCREEN_HEIGHT;
//      window.setAttributes(lp);

        init();
    }

    public void setPositon(int xoff, int yoff) {
            Window window = this.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.x = xoff;
            lp.y = yoff;
//            this.xOff = xoff;
//            this.yOff = yoff;
            window.setAttributes(lp);
      }

    public void setPreference(Preference preference){
        this.preference=preference;
        if(!TextUtils.isEmpty(preference.getSummary())&&mEditText!=null) {
            mEditText.setText(preference.getSummary());
            mEditText.setSelection(preference.getSummary().length());
        }
    }
    
    public void setEditDoneIntent(Intent intent, String initValue) {
        mIntent = intent;
        defaultValue = initValue;
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
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
        if(preference!=null&&!TextUtils.isEmpty(preference.getSummary())) {
            mEditText.setText(preference.getSummary());
            mEditText.setSelection(preference.getSummary().length());
        }else if(defaultValue != null){
            mEditText.setText(defaultValue);
            mEditText.setSelection(defaultValue.length());
        }
        if(length !=-1){
            InputFilter [] filter ={new InputFilter.LengthFilter(length)};
            mEditText.setFilters(filter);
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
        if(EditorInfo.IME_ACTION_DONE==actionId){
            if(checkLengthIsEnough()){
                String value=mEditText.getText().toString();
                String descript = ""+value;
                if(descript.length()<5){
                    int i = 5-descript.length();
                    while(i>0){
                        descript ="0"+descript;
                        i--;
                    }
                }
                MtkTvConfig.getInstance().setConfigString(CommonIntegration.OCEANIA_POSTAL, descript);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getConfigString,postal code:"+MtkTvConfig.getInstance().getConfigString(CommonIntegration.OCEANIA_POSTAL));
                if(preference != null){
                    preference.setSummary(descript);
                }
                MtkTvEWSPABase.EwspaRet ret = MtkTvEWSPA.getInstance().setLocationCode(Util.stringToByte(descript));
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLocationCode response:"+ret);
                 cancel();
                 if(mIntent != null){
                     mContext.startActivity(mIntent);
                 }
               }
            return true;
        }
        return false;
    }

    private boolean checkLengthIsEnough(){
        String ret = mEditText.getText().toString();
        if(!TextUtils.isEmpty(ret)){
            if(itemID!= null && itemID.equals(MenuConfigManager.BISS_KEY_CW_KEY)){
                if(ret.length()<16){
                    Toast.makeText(mContext, R.string.menu_string_postal_code_toast_less_than16, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else if(itemID != null && itemID.equals(MenuConfigManager.SETUP_POSTAL_CODE)){
                if(ret.length()<5 || "00000".equals(ret)){
                    Toast.makeText(mContext, R.string.menu_string_postal_code_toast_invalid_input, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }else{
            Toast.makeText(mContext, R.string.menu_string_postal_code_toast_cannot_empty, Toast.LENGTH_SHORT).show();
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

