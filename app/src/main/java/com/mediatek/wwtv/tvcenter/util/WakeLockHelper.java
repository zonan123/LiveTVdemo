package com.mediatek.wwtv.tvcenter.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

public final class WakeLockHelper {
  public final static String TAG = "WakeLockHelper";
  public final static Map<String, PowerManager.WakeLock> mWakeLockMap = new HashMap<String, PowerManager.WakeLock>();

  private WakeLockHelper() {

  }

  public static PowerManager.WakeLock acquireWakeLock(Context context, String tag) {
    return acquireWakeLock(context, tag, 8000);
  }

  public static PowerManager.WakeLock acquireWakeLock(Context context, String tag, long timeout) {
    try {
      String finalTag = TAG + tag;
      PowerManager.WakeLock wl = mWakeLockMap.get(finalTag);
      if(wl != null) {
        finalTag = finalTag + SystemClock.uptimeMillis();
      }

      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      wl = pm.newWakeLock(
              PowerManager.PARTIAL_WAKE_LOCK, finalTag);

      wl.acquire();

      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "acquireWakeLock " + finalTag + wl);

      final String releaseTag = finalTag;
      mWakeLockMap.put(releaseTag, wl);

      TVAsyncExecutor.getInstance().execute(() -> {
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "releaseTag  " + releaseTag);
        try {
        Thread.sleep(timeout);
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        releaseWakeLock(mWakeLockMap.get(releaseTag));
      mWakeLockMap.remove(releaseTag);
      });

      return wl;
    } catch (Exception ex) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Exception " + ex);
    }

    return null;
  }

  public static void releaseWakeLock(PowerManager.WakeLock wl) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "releaseWakeLock " + wl);
    try {
      wl.release();
    } catch (Exception ex) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Exception " + ex);
    }
  }
}
