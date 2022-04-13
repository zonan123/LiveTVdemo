package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
//import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import androidx.preference.Preference;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

//import android.support.v7.preference.Preference;
public class BissKeyPreferenceDialog extends Dialog implements TextView.OnEditorActionListener,DialogInterface.OnShowListener{
//    private TextView modelNameShow, versionShow, serialNumShow;
    private Context mContext;
//    private int xOff;
//    private int yOff;
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;

    public static final String TAG = "BissKeyPreferenceDialog";
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
//    private TextWatcher mTextWatcher = null;
//    private TextView.OnEditorActionListener mEditorActionListener = null;
//    private EditText mEditText ;
//    private String itemID ;
//    private String mDescString;
    private int defaultValue;
    private Preference preference;
//    private int length=-1;
    private RadioButton rh;
    private RadioButton rv;
    public BissKeyPreferenceDialog(Context context,int defaultValue) {
        super(context, R.style.Theme_TurnkeyCommDialog);
        mContext = context;
        this.defaultValue=defaultValue;
        window = getWindow();
        lp = window.getAttributes();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_dialog);
        width = (int) (ScreenConstant.SCREEN_WIDTH * 0.55);
        lp.width = width;
        height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.35);
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
            //this.xOff = xoff;
            //this.yOff = yoff;
            window.setAttributes(lp);
      }
    public void setPreference(Preference preference){
        this.preference=preference;

    }
    public void setDefaultValue(int defaultValue){
        this.defaultValue=defaultValue;
        if(defaultValue==0){
          if(rh!=null){
            rh.setChecked(true);
           }
         }else{
           if(rv!=null){
             rv.setChecked(true);
            }
         }
        Log.d(TAG,"dialog set pola:"+defaultValue);
        Log.d(TAG, "set defaultValue:"+defaultValue);
    }
    public void init() {
        Log.d(TAG, "defaultValue:"+defaultValue);
        RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup_pola);
        rh=(RadioButton)findViewById(R.id.btn_h);
        rv=(RadioButton)findViewById(R.id.btn_v);
        if(defaultValue==0){
            rh.setChecked(true);
        }else{
            rv.setChecked(true);
        }
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                Log.d(TAG, "arg1:"+arg1);
                if(arg1==R.id.btn_h){
                    preference.setSummary(mContext.getResources().getString(R.string.menu_setup_biss_key_horizonal));
                }else{
                    preference.setSummary(mContext.getResources().getString(R.string.menu_setup_biss_key_vertical));
                }
                cancel();
            }
        });
    }

    @Override
    public void onShow(DialogInterface arg0) {
    	Log.d(TAG, "onShow");
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }
}
