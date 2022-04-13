package com.mediatek.wwtv.tvcenter.commonview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

//import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.setting.util.TVContent;
//import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.tvcenter.R;
//
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import mediatek.sysprop.VendorProperties;

public class DiagnosticDialog extends Dialog implements View.OnKeyListener{

    private Context mContext;
//    private int xOff;
//    private int yOff;
    private Window window;
    private WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;
    public DiagnosticDialog(Context context) {
        super(context, R.style.Theme_TurnkeyCommDialog);
        mContext = context;
        window = getWindow();
        lp = window.getAttributes();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diagnostic_screen_dialog);
        width = (int) (ScreenConstant.SCREEN_WIDTH * 0.45);
        lp.width = width;
        height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.30);
        lp.height = height;
        //lp.x = 0 - lp.width / 3;
        window.setAttributes(lp);
        init();
    }

    public void init() {
        /*        String modelName ="";
        modelName = TVContent.getInstance(mContext).getSysVersion(3, modelName);
        int index=modelName.lastIndexOf("_")+1;
        String modeNameshow = modelName.substring(index);*/

        String version = VendorProperties.customer_software_version().orElse("");
        if(version == null || version.equals("")){
            version = TVContent.getInstance(mContext).getSysVersion(0, version);
        }
        TextView mToastMessage = (TextView)findViewById(R.id.diagnostic_screen_toast_message);
      //  String mess= mContext.getString(R.string.menu_versioninfo_name) +":"+modeNameshow+"\n";
        String mess = (mContext.getString(R.string.menu_versioninfo_version) +": "+version);

        mToastMessage.setText(mess);
        Button cancelBt=(Button)findViewById(R.id.diagnostic_screen_toast_message_btn_cancel);
        cancelBt.setOnKeyListener(this);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_CENTER:
              case KeyEvent.KEYCODE_ENTER:
                cancel();
                return true;
              case KeyEvent.KEYCODE_DPAD_RIGHT:

                return true;
              case KeyEvent.KEYCODE_DPAD_LEFT:

                return true;
              case KeyEvent.KEYCODE_BACK:
                  cancel();
                  return true;
              default:
                return false;
            }
          } else {

            return true;
          }
    }

}
