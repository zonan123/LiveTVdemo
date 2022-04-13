package com.mediatek.wwtv.tvcenter.commonview;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;
//import android.widget.ProgressBar;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.R;

public class LoadingDialog extends android.app.Dialog {

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    public LoadingDialog(Context mContext){
        super(mContext, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init(){
		setContentView(R.layout.loading);

//        setWindowPosition(0, 0);
        setCancelable(false);
    }

   /* @Override
    public void show() {
        super.show();
    }*/

    /*@Override
    public void dismiss() {
        super.dismiss();
    }*/

    /**
     * the method is used to set the position & size of dialog
     */
    public void setWindowPosition(int x, int y, int width, int height) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width  = width * ScreenConstant.SCREEN_WIDTH / 1920;
        lp.height = height * ScreenConstant.SCREEN_HEIGHT / 1080;

        lp.x = x;
        lp.y = y;
        window.setAttributes(lp);
    }

    /**
     * the method is used to set the position & size of dialog
     */
    public void setWindowPosition(int x, int y) {
        setWindowPosition(x, y, 200, 200);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
          case KeyMap.KEYCODE_MTKIR_MUTE:
          case KeyMap.KEYCODE_VOLUME_UP:
          case KeyMap.KEYCODE_VOLUME_DOWN:
              return true;
          default:
              break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
