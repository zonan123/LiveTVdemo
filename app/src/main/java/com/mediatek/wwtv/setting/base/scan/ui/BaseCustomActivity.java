
package com.mediatek.wwtv.setting.base.scan.ui;



import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Locale;

/**
 * this activity is defined as ChannelInfoActivity,ScanActivity's parent class
 *
 * @author sin_biaoqinggao
 */
public class BaseCustomActivity extends BaseActivity {
  private static final String TAG = "BaseCustomActivity";
  long ENTER_DELAYMILLS = 500;
  long enterPressTime = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setDisplayAttr();
  }

  protected void setDisplayAttr(){
    setDisplayAttr(0.625f, 1.0f);
  }
  protected void setDisplayAttr(float width, float height) {
    // set size
    WindowManager.LayoutParams lp = getWindow().getAttributes();
    DisplayMetrics outMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
    lp.width = width < 1 ? (int) (outMetrics.widthPixels * width + 1) : (int) (outMetrics.widthPixels * width);
    lp.height = (int) (outMetrics.heightPixels * height);
    if(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL){
      lp.gravity = Gravity.RIGHT;
    }else{
      lp.gravity = Gravity.LEFT;
    }
    this.getWindow().setAttributes(lp);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == 0
        && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
      long nowMills = SystemClock.uptimeMillis();
      if (nowMills - enterPressTime > ENTER_DELAYMILLS) {
        enterPressTime = nowMills;
      } else {
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  protected void onRestart() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "custom-activity now onRestart");
//    LiveApplication.tryToStartTV();
    super.onRestart();
  }

  @Override
  protected void onStop() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "custom-activity now onStop");
//    LiveApplication.checkIsNeedToStartTV();
    super.onStop();
  }

}
