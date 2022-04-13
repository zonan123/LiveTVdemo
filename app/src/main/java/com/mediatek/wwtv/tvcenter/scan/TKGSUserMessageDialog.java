
package com.mediatek.wwtv.tvcenter.scan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;
import com.mediatek.wwtv.tvcenter.R;

/**
 * Only For EU-TKGS update scan in Menu.
 * After scan complete for check dvbs info or scan cancel,
 * should pop up dialog to confirm whether to continue scan country=turkey.
 */
public class TKGSUserMessageDialog {

  private final Context mContext;
  Handler mHandler = new Handler();
  Runnable hide;

  public TKGSUserMessageDialog(Context mContext) {
    super();
    this.mContext = mContext;
  }

  public void showConfirmDialog(String message) {

    final Dialog userMessageDialog = new AlertDialog.Builder(mContext)
        .setTitle(R.string.tkgs_user_msg)
        .setMessage(message)
        .setPositiveButton(R.string.menu_ok, null)
        .create();
    userMessageDialog.show();
    userMessageDialog.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface arg0) {
        // TVContent.getInstance(mContext).getScanManager().setDVBSTkgsUserMessage(null);
        mHandler.removeCallbacks(hide);
      }
    });
    userMessageDialog.setOnKeyListener(new OnKeyListener() {

      @Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          dialog.dismiss();
        }
        return true;
      }

    });

    final Runnable hide = new Runnable() {

      @Override
      public void run() {
        if(userMessageDialog != null && userMessageDialog.isShowing()) {
          userMessageDialog.dismiss();
        }
      }

    };
    mHandler.postDelayed(hide, 30000);
  }
}
