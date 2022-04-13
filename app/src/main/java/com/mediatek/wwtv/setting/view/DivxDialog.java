package com.mediatek.wwtv.setting.view;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
//import android.view.LayoutInflater;
import android.view.View;
//import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class DivxDialog extends Dialog {
    protected static final String TAG = "DivxDialog";
    private Context context;
    private Button vButtonOK;
    private Button vButtonCancel;
    private TextView vTextView;
    private LinearLayout mLayout;
    public static int flag = 0;

    boolean isPositionView = false;
    static final String CODE = "BX9ASIKQEI";
    static final String DEREGISTER_COLON = ":";
    private String itemId="";
    TVContent mtvcontent = TVContent.getInstance(context);
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;
    public DivxDialog(Context context) {
        super(context, R.style.Theme_TurnkeyCommDialog);
        this.context = context;
        window = getWindow();
        lp = window.getAttributes();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_divx_info);
        width = (int) (ScreenConstant.SCREEN_WIDTH * 0.55);
        lp.width = width;
        height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.35);
        lp.height = height;
        lp.x = 0 - lp.width / 3;
        window.setAttributes(lp);
//        init();
//        setDivxInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        setDivxInfo();
    }

    public void setItemId(String itemId){
          this.itemId=itemId;
    }
    public void setPositon(int xoff, int yoff) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.x = xoff;
            lp.y = yoff;
            window.setAttributes(lp);
      }

    public void setDivxInfo() {
      if (itemId.equals(MenuConfigManager.DIVX_REG)) {// register
        mLayout.setVisibility(View.GONE);
        vButtonCancel.setVisibility(View.GONE);

        String registrationCode = mtvcontent.getDrmRegistrationCode();
        if (registrationCode == null) {
          registrationCode = "Invalid";
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "registeration:" + registrationCode);
        String message = context.getResources().getString(R.string.menu_setup_divxreg);
        message = message.replace(CODE, registrationCode);
        vTextView.setText(message);
      } else {// deregister
        mLayout.setVisibility(View.VISIBLE);
        vButtonCancel.setVisibility(View.VISIBLE);

        long uiHelpInfo = mtvcontent.getDrmUiHelpInfo();
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "uihelp:" + uiHelpInfo);
        if (((uiHelpInfo) & (1 << 1)) == 0)
        {
          String deactivation = mtvcontent.setDrmDeactivation();
          com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "deactivation:" + deactivation);
          String deactiveMsg = context.getResources().getString(R.string.menu_setup_divxdea);
          if (deactivation != null) {
            int pos = deactiveMsg.indexOf(DEREGISTER_COLON);
            StringBuffer sbuf = new StringBuffer(deactiveMsg);
            sbuf.insert(pos + 1, deactivation);
            deactiveMsg = sbuf.toString();
          }

          vTextView.setText(deactiveMsg);
        }
        else
        {
          vTextView
              .setText("Your device is already registered.\nAre you sure you wish\nto deregister?");
          flag = 1;
        }
        // vTextView.setText(context.getResources().getString(R.string.menu_setup_divxdea));
      }
      vButtonOK.requestFocus();
    }

    public void init() {
        vTextView = (TextView) findViewById(R.id.common_divx_name);
        vButtonOK = (Button) findViewById(R.id.common_divx_btn_ok);
        vButtonOK.requestFocus();
        vButtonCancel = (Button) findViewById(R.id.common_divx_btn_cancel);
        mLayout = (LinearLayout) findViewById(R.id.common_divx_ll_cancel);
        vButtonOK.setOnKeyListener(new View.OnKeyListener() {

          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
              switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    return onEnter();

                case KeyMap.KEYCODE_MTKIR_RED:
                    return onEnter();
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "KEYCODE_DPAD_RIGHT");
                  if (vButtonOK.hasFocus()
                      && vButtonCancel.getVisibility() == View.VISIBLE) {
                    vButtonCancel.requestFocus();
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "KEYCODE_DPAD_RIGHT vButtonOK requestFocus");
                    vButtonOK.requestFocus();
                  }
                  return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "KEYCODE_DPAD_LEFT");
                  if (vButtonOK.hasFocus() && vButtonCancel.getVisibility() == View.VISIBLE) {
                    vButtonCancel.requestFocus();
                  } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "KEYCODE_DPAD_LEFT vButtonOK requestFocus");
                    vButtonOK.requestFocus();
                  }
                  return true;
                case KeyEvent.KEYCODE_BACK:
                    DivxDialog.this.cancel();
                    return true;
                default:
                  return false;
              }
            } else {
              return true;
            }

          }
        });

        vButtonCancel.setOnKeyListener(new View.OnKeyListener() {

          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
              switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "onKey");
                  DivxDialog.this.cancel();
                  return false;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                  if (vButtonCancel.hasFocus()) {
                    vButtonOK.requestFocus();
                  }
                  return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                  if (vButtonCancel.hasFocus()) {
                    vButtonOK.requestFocus();
                  }
                  return true;
                case KeyEvent.KEYCODE_BACK:
                    DivxDialog.this.cancel();
                    return true;
                case KeyMap.KEYCODE_MTKIR_RED:
                    DivxDialog.this.cancel();
                    return false;
                default:
                  return false;
              }
            } else {

              return true;
            }

          }
        });
      }

    private  boolean onEnter(){
       com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "onKey");
       if (mLayout.getVisibility() == View.VISIBLE) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d("MenuMain", "vButtonCancel.getVisibility() == View.VISIBLE");
         mLayout.setVisibility(View.GONE);
         vButtonCancel.setVisibility(View.GONE);
         if (flag == 0)
         {
           String registrationCode = mtvcontent.getDrmRegistrationCode();
           if (registrationCode == null) {
             registrationCode = "Invalid";
           }
           com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "registeration:" + registrationCode);
           String message = context.getResources().getString(R.string.menu_setup_divxreg);
           message = message.replace(CODE, registrationCode);
           vTextView.setText(message);
         }
         else
         {
           mLayout.setVisibility(View.VISIBLE);
           vButtonCancel.setVisibility(View.VISIBLE);
           com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "vButtonCancel:" + "visible?");
           String deactivation = mtvcontent.setDrmDeactivation();
           com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "deactivation:" + deactivation);
           String drmMsg = context.getResources().getString(R.string.menu_setup_divxdea);
           if (deactivation != null) {
             int pos = drmMsg.indexOf(DEREGISTER_COLON);
             StringBuffer sbuf = new StringBuffer(drmMsg);
             sbuf.insert(pos + 1, deactivation);
             drmMsg = sbuf.toString();
           }

           vTextView.setText(drmMsg);
           flag = 0;
         }
       } else {
         cancel();
       }
       return true;
   }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
