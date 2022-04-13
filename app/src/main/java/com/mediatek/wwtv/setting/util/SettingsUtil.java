
package com.mediatek.wwtv.setting.util;

import java.util.Calendar;



import android.app.AlarmManager;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.view.Gravity;
//import android.widget.TextView;
//import android.widget.Toast;

/**
 * @author sin_biaoqinggao
 */
public abstract class SettingsUtil {

  public static int SCREEN_WIDTH;
  public static int SCREEN_HEIGHT;
  public static final String ACTION_PREPARE_SHUTDOWN =
      "android.intent.action.ACTION_PREPARE_SHUTDOWN";

  public static final String TAG = "SettingsUtil";
  public static String OPTIONSPLITER = "#";

  public static String SPECIAL_SAT_DETAIL_INFO_ITEM_POL = "SAT_DETAIL_INFO_ITEM_POLAZation";
  private static final String SHUTDOWN_INTENT_EXTRA = "shutdown";
  public static final int SVCTX_NTFY_CODE_SIGNAL_LOCKED = 4;
  public static final int SVCTX_NTFY_CODE_SIGNAL_LOSS = 5;
  public static final int SVCTX_NTFY_CODE_SERVICE_BLOCKED = 9;
  public static final int SVCTX_NTFY_CODE_SERVICE_UNBLOCKED = 12;
  public static final int SVCTX_NTFY_CODE_NO_AUDIO_VIDEO_SVC = 19;
  public static final int SVCTX_NTFY_CODE_AUDIO_ONLY_SVC = 20;
  public static final int SVCTX_NTFY_CODE_VIDEO_ONLY_SVC = 21;
  public static final int SVCTX_NTFY_CODE_AUDIO_VIDEO_SVC = 22;
  public static final int SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_VIDEO_SVC = 23;
  public static final int SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_CLEAR_VIDEO_SVC = 24;
  public static final int SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_NO_VIDEO_SVC = 25;
  public static final int SVCTX_NTFY_CODE_SCRAMBLED_VIDEO_CLEAR_AUDIO_SVC = 26;
  public static final int SVCTX_NTFY_CODE_SCRAMBLED_VIDEO_NO_AUDIO_SVC = 27;
  public static final int SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE = 37;// video update

  public static final String NAV_COMPONENT_SHOW_FLAG = "NavComponentShow";
  public static final int NAV_COMP_ID_BASIC = 0x1000000;
  public static final int NAV_COMP_ID_EAS = NAV_COMP_ID_BASIC + 1;

  public static final String MAIN_SOURCE_NAME = "multi_view_main_source_name";
  public static final String SUB_SOURCE_NAME = "multi_view_sub_source_name";

  public static int loadStatus = -1;
  public static final int LOAD_STATUS_START = 0x15;
  public static final int LOAD_STATUS_FINISH = 0x16;

  /**
   * get the real CfgId and the Value to set
   *
   * @param newId
   * @return
   */
  public static String[] getRealIdAndValue(String newId) {
    String[] idValue = newId.split("#");
    if (idValue != null && idValue.length == 2) {
      return idValue;
    } else {
      Log.e(TAG, "something error,please check your newId:" + newId);
    }
    return new String[]{};
  }

  public static void setDate(Context context, int year, int month, int day) {
    Calendar c = Calendar.getInstance();

    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month);
    c.set(Calendar.DAY_OF_MONTH, day);
    long when = c.getTimeInMillis();
    Log.e(TAG, "time miss==" + when);
    if (when / 1000 < Integer.MAX_VALUE) {
      ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
    } else {
      Log.e(TAG, "this is too long...");
    }
  }

  public static void setTime(Context context, int hourOfDay, int minute) {
    Calendar c = Calendar.getInstance();

    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
    c.set(Calendar.MINUTE, minute);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    long when = c.getTimeInMillis();

    if (when / 1000 < Integer.MAX_VALUE) {
      ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "set Time== "+ c );
    }
  }
  public static void setTime(Context context, int hourOfDay, int minute,int second) {
      Calendar c = Calendar.getInstance();

      c.set(Calendar.HOUR_OF_DAY, hourOfDay);
      c.set(Calendar.MINUTE, minute);
      c.set(Calendar.SECOND, second);
      c.set(Calendar.MILLISECOND, 0);
      long when = c.getTimeInMillis();

      if (when / 1000 < Integer.MAX_VALUE) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "set Time== "+ c );
      }
    }
}
