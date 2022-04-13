package com.mediatek.wwtv.tvcenter.commonview;

import android.app.Activity;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.tvcenter.nav.view.HbbtvAudioSubtitleDialog;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;

/**
 * This class is used to prepare basic activity
 *
 * @author MTK40707
 */
public class BaseActivity extends Activity {

  @Override
  protected void onResume() {
    super.onResume();

    DestroyApp.setActivityActiveStatus(true);
    DestroyApp.setRunningActivity(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    DestroyApp.setActivityActiveStatus(false);
    DestroyApp.setRunningActivity(null);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (ScheduleListDialog.getDialog() != null && ScheduleListDialog.getDialog().isShowing()) {
      // CR 959741 ,follow linux ,dismiss schedule dialog when confirm
      // dialog show.
      ScheduleListDialog.getDialog().dismiss();
    }
    if (ScheduleListItemDialog.getInstance() != null
        && ScheduleListItemDialog.getInstance().isShowing()) {
      ScheduleListItemDialog.getInstance().dismiss();
    }
    if (StateDvrFileList.getInstance() != null && StateDvrFileList.getInstance().isShowing()) {
      StateDvrFileList.getInstance().dissmiss();
    }
    if (HbbtvAudioSubtitleDialog.getHbbtvDialog() != null
        && HbbtvAudioSubtitleDialog.getHbbtvDialog().isShowing()) {
      HbbtvAudioSubtitleDialog.getHbbtvDialog().dismiss();
    }
  }
}