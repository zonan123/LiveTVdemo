
package com.mediatek.wwtv.setting.base.scan.model;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;

import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.DvbsTableType;

/**
 * Only For EU-TKGS update scan in Menu.
 * After scan complete for check dvbs info or scan cancel,
 * should pop up dialog to confirm whether to continue scan country=turkey.
 */
public class TKGSContinueScanConfirmDialog {

  private final Context mContext;
  private final int mSatId;

  public TKGSContinueScanConfirmDialog(Context mContext, int satId) {
    super();
    this.mContext = mContext;
    this.mSatId = satId;
  }

  public void showConfirmDialog() {

    final LiveTVDialog confirmDialog = new LiveTVDialog(mContext,
        3);
    confirmDialog
        .setMessage(mContext.getString(R.string.scan_trd_turkey_tkgs_update_scan));
    confirmDialog.setButtonYesName(mContext
        .getString(R.string.menu_setup_button_yes));
    confirmDialog.setButtonNoName(mContext
        .getString(R.string.menu_setup_button_no));
    confirmDialog.show();

    confirmDialog.getButtonNo().requestFocus();

    confirmDialog.setPositon(-20, 70);
    confirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
      @Override
      public boolean onKey(DialogInterface dialog, int keyCode,
          KeyEvent event) {
        int action = event.getAction();
        if (keyCode == KeyEvent.KEYCODE_BACK
            && action == KeyEvent.ACTION_DOWN) {
          confirmDialog.dismiss();
          cancelContinueScan();
          return true;
        }
        return false;
      }
    });

    OnKeyListener yesListener = new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          if (keyCode == KeyEvent.KEYCODE_ENTER
              || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            confirmDialog.dismiss();
            resetTableVersion();
            continueScan();
            return true;
          }
        }
        return false;
      }
    };

    OnKeyListener noListener = new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          if (keyCode == KeyEvent.KEYCODE_ENTER
              || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            confirmDialog.dismiss();
            cancelContinueScan();
            return true;
          }
        }
        return false;
      }
    };
    confirmDialog.getButtonNo().setOnKeyListener(noListener);
    confirmDialog.getButtonYes().setOnKeyListener(yesListener);
  }

  private void resetTableVersion() {
    MtkTvScanDvbsBase mtkTvScanDvbsBase = new MtkTvScanDvbsBase();
    mtkTvScanDvbsBase.dvbsSetTableVersion(DvbsTableType.DVBS_TABLE_TYPE_TKGS,
        MtkTvScanDvbsBase.SB_DVBS_TABLE_INVALID_VERSION);
  }

  private void continueScan() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("continueScan()");
    ((ScanViewActivity) mContext).startDVBSFullScan(mSatId, -1, 1,
        MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
  }

  private void cancelContinueScan() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("cancelContinueScan()");
  }
}
