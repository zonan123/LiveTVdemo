/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import androidx.annotation.NonNull;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.view.DiskSettingDialog;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.GingaTvDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import java.util.List;

/** A class to define possible actions from main menu. */
public class MenuAction {
  // Actions in the TV option row.
  public static final MenuAction SELECT_SOURCE_ACTION =
      new MenuAction(
          R.string.option_item_source,
          MenuOptionMain.OPTION_SOURCE_CAPTIONS,
          R.drawable.ic_tvoption_sourcelist);
  public static final MenuAction SELECT_CLOSED_CAPTION_ACTION =
      new MenuAction(
          R.string.options_item_closed_caption,
          MenuOptionMain.OPTION_CLOSED_CAPTIONS,
          R.drawable.ic_tvoption_cc);
  /*public static final MenuAction SELECT_DISPLAY_MODE_ACTION =
      new MenuAction(
          R.string.options_item_display_mode,
          MenuOptionMain.OPTION_DISPLAY_MODE,
          R.drawable.ic_tvoption_aspect);*/
  public static final MenuAction PIP_IN_APP_ACTION =
      new MenuAction(
          R.string.options_item_pip, MenuOptionMain.OPTION_IN_APP_PIP, R.drawable.ic_tvoption_pip);
  public static final MenuAction SYSTEMWIDE_PIP_ACTION =
      new MenuAction(
          R.string.options_item_pip,
          MenuOptionMain.OPTION_SYSTEMWIDE_PIP,
          R.drawable.ic_tvoption_pip);
 /* public static final MenuAction SELECT_AUTO_PICTURE_ACTION =
      new MenuAction(
          R.string.options_item_picture_style,
          MenuOptionMain.OPTION_AUTO_PICTURE,
          R.drawable.ic_tvoption_auto_picture);*/
//  public static final MenuAction SELECT_SPEAKERS_ACTION =
//      new MenuAction(
//          R.string.options_item_speakers,
//          MenuOptionMain.OPTION_SPEAKERS,
//          R.drawable.ic_tvoption_multi_track);
  public static final MenuAction SELECT_AUDIO_LANGUAGE_ACTION =
      new MenuAction(
          R.string.menu_channel_multi_audio,
          MenuOptionMain.OPTION_MULTI_AUDIO,
          R.drawable.ic_music_note);
  public static final MenuAction MORE_CHANNELS_ACTION =
      new MenuAction(
          R.string.options_item_more_channels,
          MenuOptionMain.OPTION_MORE_CHANNELS,
          R.drawable.ic_store);
  public static final MenuAction DEV_ACTION =
      new MenuAction(
          R.string.options_item_developer,
          MenuOptionMain.OPTION_DEVELOPER,
          R.drawable.ic_developer_mode_tv_white_48dp);

  public static final MenuAction POWER_ACTION =
      new MenuAction(R.string.nav_upgrader_power, MenuOptionMain.OPTION_POWER, R.drawable.ic_power);
  public static final MenuAction SETTINGS_ACTION =
      new MenuAction(
          R.string.options_item_settings, MenuOptionMain.OPTION_SETTINGS, R.drawable.ic_settings);
  public static final MenuAction BROADCAST_TV_SETTINGS_ACTION =
      new MenuAction(
          R.string.menu_advanced_options,
          MenuOptionMain.OPTION_BROADCAST_TV_SETTINGS,
          R.drawable.ic_advanced);

  public static final MenuAction BROADCAST_TV_OAD_ACTION =
      new MenuAction(
          R.string.menu_advanced_oad, MenuOptionMain.OPTION_BROADCAST_TV_OAD, R.drawable.ic_oad);

  public static final MenuAction BROADCAST_TV_CI_ACTION =
      new MenuAction(
          R.string.menu_advanced_ci, MenuOptionMain.OPTION_BROADCAST_TV_CI, R.drawable.ic_ci);

  public static final MenuAction DVR_LIST_ACTION =
      new MenuAction(
          R.string.pvr_playlist_record_list,
          MenuOptionMain.OPTION_DVR_LIST,
          R.drawable.ic_recordlist);

  public static final MenuAction TSHIFT_MODE_ACTION =
      new MenuAction(
          R.string.menu_setup_time_shifting_mode,
          MenuOptionMain.OPTION_TSHIFT_MODE,
          R.drawable.ic_advanced);

  public static final MenuAction SCHEDULE_LIST_ACTION =
      new MenuAction(
          R.string.menu_setup_schedule_list,
          MenuOptionMain.OPTION_SCHEDULE_LIST,
          R.drawable.ic_schedule_list);

  public static final MenuAction DEVICE_INFO_ACTION =
      new MenuAction(
          R.string.menu_setup_device_info,
          MenuOptionMain.OPTION_DEVICE_INFO,
          R.drawable.ic_device_info);

  public static final MenuAction PVR_START_ACTION =
      new MenuAction(
          R.string.title_pvr_start, MenuOptionMain.OPTION_PVR_START, R.drawable.menu_start_record);

  public static final MenuAction PVR_STOP_ACTION =
      new MenuAction(
          R.string.title_pvr_stop, MenuOptionMain.OPTION_PVR_STOP, R.drawable.menu_stop_record);

  // channels row item
 /* public static final MenuAction MY_FAVORITE_ACTION =
      new MenuAction(
          R.string.options_item_add_to_favorite_normal,
          MenuOptionMain.OPTION_MY_FAVORITE,
          R.drawable.ic_favorite_normal);

  public static final MenuAction MY_FAVORITE_ACTION_SELECTED =
      new MenuAction(
          R.string.options_item_add_to_favorite_selected,
          MenuOptionMain.OPTION_MY_FAVORITE,
          R.drawable.ic_favorite_selected);*/

  public static final MenuAction PROGRAM_GUIDE_ACTION =
      new MenuAction(
          R.string.channels_item_program_guide,
          MenuOptionMain.OPTION_PROGRAM_GUIDE,
          R.drawable.ic_program_guide);
  public static final MenuAction CHANNEL_UP_ACTION =
      new MenuAction(
          R.string.channels_item_up, MenuOptionMain.OPTION_CHANNEL_UP, R.drawable.ic_advanced);

  public static final MenuAction CHANNEL_DOWN_ACTION =
      new MenuAction(
          R.string.channels_item_down,
          MenuOptionMain.OPTION_CHANNEL_DOWN,
          R.drawable.ic_device_info);
  public static final MenuAction NEW_CHANNELS_ACTION =
      new MenuAction(
          R.string.channels_item_setup,
          MenuOptionMain.OPTION_NEW_CHANNELS,
          R.drawable.ic_new_channel);
  public static final MenuAction APP_LINK_ACTION =
      new MenuAction(
          R.string.menu_app_link, MenuOptionMain.OPTION_APP_LINK, R.drawable.ic_device_info);
  public static final MenuAction GINGA_SELECTION =
      new MenuAction(
          R.string.options_item_ginga, MenuOptionMain.OPTION_GINGA, R.drawable.ic_menu_ginga);

 /* public static final MenuAction SOUND_STYLE =
      new MenuAction(
          R.string.options_item_sounds_style,
          MenuOptionMain.OPTION_SOUND_STYLE,
          R.drawable.ic_tvoption_multi_track);*/

  public static final MenuAction CHANNEL =
      new MenuAction(
          R.string.menu_channel, MenuOptionMain.OPTION_CHANNEL, R.drawable.partner_ic_live_tv);

  public static final MenuAction PICTURE = new MenuAction(
          R.string.device_picture, MenuOptionMain.OPTION_PICTURE,
          R.drawable.ic_tvoption_auto_picture);

  public static final MenuAction SOUND = new MenuAction(
          R.string.sound_category_title, MenuOptionMain.OPTION_SOUND,
          R.drawable.ic_tvoption_multi_track);

  private String mActionName;
  private int mActionNameResId;
  private final int mType;
  private String mActionDescription;
  private Drawable mDrawable;
  private int mDrawableResId;
  @NonNull private TIFChannelInfo mChannelInfo;
  private boolean mEnabled = true;

  private static final String TAG = "MenuAction";

  /** Sets the action description. Returns {@code trye} if the description is changed. */
  public static boolean setActionDescription(MenuAction action, String actionDescription) {
    String oldDescription = action.mActionDescription;
    action.mActionDescription = actionDescription;
    return !TextUtils.equals(action.mActionDescription, oldDescription);
  }

  /** Enables or disables the action. Returns {@code true} if the value is changed. */
  public static boolean setEnabled(MenuAction action, boolean enabled) {
    boolean changed = action.mEnabled != enabled;
    action.mEnabled = enabled;
    return changed;
  }

  public MenuAction(int actionNameResId, int type, int drawableResId) {
    mActionName = null;
    mActionNameResId = actionNameResId;
    mType = type;
    mDrawable = null;
    mDrawableResId = drawableResId;
  }

  public MenuAction(String actionName, int type, Drawable drawable) {
    mActionName = actionName;
    mActionNameResId = 0;
    mType = type;
    mDrawable = drawable;
    mDrawableResId = 0;
  }

  public MenuAction(
      String actionName, int type, Drawable drawable, @NonNull TIFChannelInfo channelInfo) {
    mActionName = actionName;
    mActionNameResId = 0;
    mType = type;
    mDrawable = drawable;
    mDrawableResId = 0;
    mChannelInfo = channelInfo;
  }

  public String getActionName(Context context) {
    if (!TextUtils.isEmpty(mActionName)) {
      return mActionName;
    }
    return context.getString(mActionNameResId);
  }

  public String getActionDescription() {
    return mActionDescription;
  }

  public int getType() {
    return mType;
  }

  /** Returns Drawable. */
  public Drawable getDrawable(Context context) {
    if (mDrawable == null) {
      mDrawable = context.getDrawable(mDrawableResId);
    }
    return mDrawable;
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public int getActionNameResId() {
    return mActionNameResId;
  }

  public void setActionNameResId(int resId) {
    mActionNameResId = resId;
  }

  public void setChannelInfo(TIFChannelInfo info) {
    mChannelInfo = info;
  }

  public TIFChannelInfo getChannelInfo() {
    return mChannelInfo;
  }

	public static void showSetting(Context context) {
		Intent intent = new Intent("android.settings.SETTINGS");
		intent.putExtra(
				com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC,
				com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC_LIVE_TV);
		context.startActivity(intent);
	}

  public static void showBroadcastTvSetting(Activity activity) {
    Intent intent = new Intent(activity, LiveTvSetting.class);
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC_LIVE_TV);

    activity.startActivityForResult(intent, NavBasic.NAV_REQUEST_CODE);
  }

  public static void enterAndroidPIP() {
    com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.getInstance()
        .updateStatus(
            com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.NAV_ENTER_ANDR_PIP, 0);
  }

  public static void showCCSetting(Context context) {
    CommonIntegration cig = CommonIntegration.getInstance();
    if (cig.is3rdTVSource() || cig.isSARegion()) { // add for sa
      Intent intent = new Intent(context, LiveTvSetting.class);
      intent.putExtra(
          com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
          com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_3RD_CAPTION_SRC);
      context.startActivity(intent);
      return;
    }
    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_CAPTIONS_SRC);
    context.startActivity(intent);
  }

  /*public static void showPictureFormatSetting(Context context) {

    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_DISPLAY_MODE_SRC);
    context.startActivity(intent);
  }*/

  /*public static void showSoundStyleSetting(Context context) {
    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_SOUND_STYLE_SRC);
    context.startActivity(intent);
  }
*/

  /**
   * Picture Setting
   * @param context
   */
  public static void showPictureSetting(Context context){
    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_TYPE_PICTURE_SRC);
    context.startActivity(intent);
  }

  /**
   * Sound Setting
   * @param context
   */
  public static void showSoundSetting(Context context){
    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_TYPE_SOUND_SRC);
    context.startActivity(intent);
  }

  public static void showPowerSetting(Context context) {
    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_POWER_SRC);
    context.startActivity(intent);
  }

  public static void showMultiAudioSetting(Context context) {
    Intent intent = new Intent(context, LiveTvSetting.class);
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_MULTI_AUDIO_SRC);
    context.startActivity(intent);
  }

  /*public static void showPictureModeSetting(Context context) {
    Intent intent = new Intent("android.settings.SETTINGS");
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_PICTURE_STYLE_SRC);
    context.startActivity(intent);
  }*/

  public static void showCamScanOperators(Context context) {
    Intent intent = new Intent(context, LiveTvSetting.class);
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_TYPE_CAMSCAN_SRC);
    context.startActivity(intent);
  }

//  public static void showSoundSpeakersSetting(Context context) {
//    Intent intent = new Intent("android.settings.SETTINGS");
//    intent.putExtra(
//        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
//        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_SPEAKER_SRC);
//    context.startActivity(intent);
//  }

  public static void showOAD(Activity activity) {
    if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
      // stop all
      DvrManager.getInstance().setStopDvrNotResumeLauncher(true);
      StateDvrPlayback.getInstance().saveStopMessages();
    }

    Intent intent = new Intent(activity, com.mediatek.wwtv.tvcenter.oad.NavOADActivityNew.class);

    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra("updateType", -1);
    activity.startActivity(intent);
  }

  public static void showCI(Activity activity) {
    CIMainDialog dialog =
        (CIMainDialog)
            ComponentsManager.getInstance().getComponentById(CIMainDialog.NAV_COMP_ID_CI_DIALOG);

    if (dialog != null) {
      dialog.setCurrCIViewType(
          com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog.CIViewType.CI_DATA_TYPE_CAM_MENU);
      ComponentsManager.getInstance().showNavComponent(CIMainDialog.NAV_COMP_ID_CI_DIALOG);
    }
  }

  public static void enterAndroidSource(Activity activity) {
    Intent intent = new Intent("com.android.tv.action.VIEW_INPUTS");
    intent.setPackage("com.mediatek.wwtv.tvcenter");
    intent.putExtra("extra_is_turnkey_top_running", DestroyApp.isCurActivityTkuiMainActivity());
    activity.startActivity(intent);
  }

  public static void showStartPvr(Context context) {
    ComponentsManager.getInstance().hideAllComponents();
    InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_RECORD);
  }

  public static void showStopPvr(Context context) {
    ComponentsManager.getInstance().hideAllComponents();
    InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_STOP);
  }

  public static void showRecordList() {
    if (DvrManager.getInstance().diskIsReady()) {
      ComponentsManager.getInstance().hideAllComponents();
      if (DvrManager.getInstance()
          .setState(StateDvrFileList.getInstance(DvrManager.getInstance()))) {
        StateDvrFileList.getInstance().showPVRlist();
      }
    } else {
      DvrManager.getInstance().showPromptInfo(DvrManager.PRO_TIME_SHIFT_DISK_NOT_READY);
    }
  }

  public static void showRecordDeviceInfo(Context context) {
    ComponentsManager.getInstance().hideAllComponents();
    DiskSettingDialog dialog = new DiskSettingDialog(context, R.layout.pvr_timeshfit_deviceinfo);
    dialog.show();
  }

  public static void showScheduleList(Context context) {

    List<MtkTvBookingBase> books = StateScheduleList.getInstance().queryItem();
    if (books.isEmpty()) {
      SimpleDialog simpleDialog =
          (SimpleDialog)
              ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
      simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_INFO);
      simpleDialog.setContent(R.string.pvr_schedulepvr_add_schedule);
      simpleDialog.setConfirmText(R.string.pvr_schedule_add);
      simpleDialog.setCancelText(R.string.pvr_schedule_cancel);
      simpleDialog.setOnConfirmClickListener(new RegistOnDvrDialog(), KeyMap.KEYCODE_MTKIR_RECORD);
      simpleDialog.setOnCancelClickListener(new RegistOnDvrDialog(), -1);
      simpleDialog.show();
      return;
    }
    ComponentsManager.getInstance().hideAllComponents();
    ScheduleListDialog scheduleListDialog = new ScheduleListDialog(context, 0);
    scheduleListDialog.setEpgFlag(false);
    scheduleListDialog.show();
  }

  public static void showTShiftMode(Context context) {
    Intent intent = new Intent(context, LiveTvSetting.class);
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_RECORD_TSHIFT_SRC);
    context.startActivity(intent);
  }

  public static void showGinga(Context context) {
    GingaTvDialog gingaDlg =
        (GingaTvDialog)
            ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_GINGA_TV);
    if (gingaDlg != null) {
      ComponentsManager.getInstance().hideAllComponents();
      gingaDlg.show();
    }
  }

  public static void onProgramGuideClicked(TurnkeyUiMainActivity activity) {
    Log.d(TAG, "onProgramGuideClicked");
    if (DvrManager.getInstance().pvrIsRecording()) {
      ComponentsManager.getInstance().hideAllComponents();
      StateDvr.getInstance().onKeyDown(KeyMap.KEYCODE_MTKIR_GUIDE);
    } else {
      if (activity != null) {
        activity.showEPG();
      }
    }
  }

  public static void onNewChannelsClicked(TurnkeyUiMainActivity mainActivity) {
    Log.d(TAG, "onNewChannelsClicked");
    if (SaveValue.getInstance(mainActivity).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
      SimpleDialog mSimpleDialog = new SimpleDialog(mainActivity);
      mSimpleDialog.setContent(R.string.warning_time_shifting_recording);
      mSimpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_INFO);
      mSimpleDialog.setScheduleDismissTime(1000);
      mSimpleDialog.show();
      return;
    }
    if (DvrManager.getInstance().pvrIsRecording()) {
      SimpleDialog mSimpleDialog = new SimpleDialog(mainActivity);
      mSimpleDialog.setContent(R.string.warning_pvr_runnning);
      mSimpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_INFO);
      mSimpleDialog.setScheduleDismissTime(1000);
      mSimpleDialog.show();
      return;
    }
    if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
      SimpleDialog mSimpleDialog = new SimpleDialog(mainActivity);
      mSimpleDialog.setContent(R.string.warning_pvr_playback_runnning);
      mSimpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_INFO);
      mSimpleDialog.setScheduleDismissTime(1000);
      mSimpleDialog.show();
      return;
    }
    Intent intent = new Intent(mainActivity, com.android.tv.onboarding.SetupSourceActivity.class);

    mainActivity.startActivity(intent);
  }

  public static void onChannelUpClicked() {
    Log.d(TAG, "onChannelUpClicked");
    InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyEvent.KEYCODE_CHANNEL_UP);
  }

  public static void onChannelDownClicked() {
    Log.d(TAG, "onChannelDownClicked");
    InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyEvent.KEYCODE_CHANNEL_DOWN);
  }

  public static void onAppLinkClicked(TurnkeyUiMainActivity activity, TIFChannelInfo channelInfo) {
    if (activity == null || channelInfo == null) {
      Log.e(TAG, "onAppLinkClicked||return");
      return;
    }
    Intent intent = channelInfo.getAppLinkIntent(activity);
    Log.d(TAG, "onAppLinkClicked||channelInfo =" + channelInfo.getDisplayNumber());
    if (intent != null) {
      activity.startActivitySafe(intent);
    }
  }

  public static void onChannelClicked(Context context) {
    Log.d(TAG, "onChannelClicked");
    Intent intent = new Intent(context, LiveTvSetting.class);
    intent.putExtra(
              com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC,
              com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC_LIVE_TV);
    intent.putExtra(
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_CHANNEL_SRC);
    context.startActivity(intent);
  }
}
