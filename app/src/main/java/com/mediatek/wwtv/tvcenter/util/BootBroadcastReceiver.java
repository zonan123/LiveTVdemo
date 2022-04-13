package com.mediatek.wwtv.tvcenter.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.tv.TvContract;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;

import com.android.tv.menu.MenuAction;
import com.android.tv.menu.MenuOptionMain;
import com.mediatek.tv.ini.IniDocument;
import com.mediatek.twoworlds.tv.MtkTvKeyEvent;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.wwtv.setting.widget.view.DiskSettingDialog;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.setting.widget.view.ScheduleListItemInfoDialog;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.db.DBHelper;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.softkey.SoftKeyBoard;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.HbbtvAudioSubtitleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.OneKeyMenuDialog;
import com.mediatek.wwtv.tvcenter.nav.view.TTXMain;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIStateChangedCallBack;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;

import java.io.File;
import java.util.Optional;

import mediatek.sysprop.VendorProperties;


public class BootBroadcastReceiver extends BroadcastReceiver {
	private static final String PACKAGE_NAME = "com.mediatek.wwtv.tvcenter";
	private static boolean keyFlag = false;
	private static final String TAG = "BootBroadcastReceiverTv";
  public static final String KEY_POWER_PICTURE_OFF = "power_picture_off";
	private OneKeyMenuDialog mOneKeyMenuDialog = null;
	private static boolean isLongPress = false;

	private static void showSetting(Context context) {
		Intent intent = new Intent("android.settings.SETTINGS");
		intent.putExtra(
				com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC,
				com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC_LIVE_TV);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);        
		context.startActivity(intent);
	}

    private static void showSimpleSetting(Context context) {
        Intent intent = new Intent("android.settings.PQAQ.SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);        
        context.startActivity(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final Context mContext = context;
		Log.d(TAG, "intent:" + intent);
		KeyEvent localKeyEvents = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		int keycode = Optional.ofNullable(localKeyEvents).map(KeyEvent::getKeyCode).orElse(-1);
		Log.i(TAG, "key:"+ localKeyEvents);
		if(Intent.ACTION_GLOBAL_BUTTON.equals(intent.getAction())
				&& keycode == KeyEvent.KEYCODE_YEN){
			if (handleChannelListButton(localKeyEvents, context)){
				return;
			}
		}

		if (intent.getAction().compareTo(Intent.ACTION_GLOBAL_BUTTON) == 0) {

			int keycodes = localKeyEvents.getKeyCode();
			Log.d(TAG, "dispatchTest||localkey =" + localKeyEvents + "||getKeypadNum ="
					+ DataSeparaterUtil.getInstance().getKeypadNum());

      //fix DTV02162469
      if (Settings.Global.getInt(context.getContentResolver(), KEY_POWER_PICTURE_OFF, 1) == 0) {
          if (localKeyEvents.getAction() == KeyEvent.ACTION_UP) {
              Settings.Global.putInt(context.getContentResolver(), KEY_POWER_PICTURE_OFF, 1);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEY_POWER_PICTURE_OFF is 0, return");
          }
          return;
      }

            if (DataSeparaterUtil.getInstance() != null
                    && (DataSeparaterUtil.getInstance().getKeypadNum() == 7)) {
                if (isScreenOn(context) && (localKeyEvents.getDeviceId() == 3)
                        && (keycodes == KeyEvent.KEYCODE_TV_INPUT)
                        && (localKeyEvents.getAction() == KeyEvent.ACTION_UP)) {
                    long duration = localKeyEvents.getEventTime()
                            - localKeyEvents.getDownTime();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Key Pad 7, input key duration :" + duration);
                    if (1000L <= duration) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Key Pad 7, send center key");
                        InstrumentationHandler.getInstance().sendKeyDownUpSync(
                                KeyEvent.KEYCODE_DPAD_CENTER);// KEYCODE_DPAD_CENTER
                        return;
                    }
                }
            }

            if (DataSeparaterUtil.getInstance() != null
					&& (DataSeparaterUtil.getInstance().getKeypadNum() == 1)) {

                if ((localKeyEvents.getDeviceId() == 3) && (keycodes == KeyEvent.KEYCODE_TV_INPUT)) {
					if (isScreenOn(context)) {

						long duration = localKeyEvents.getEventTime() - localKeyEvents.getDownTime();
						Log.d(TAG, "dispatchTest||duration =" + duration);

                        if (localKeyEvents.getAction() == KeyEvent.ACTION_DOWN) {
                            if (duration >= 3000L) {
                                Log.d(TAG, "dispatchTest||KEYCODE_POWER");
                                InstrumentationHandler.getInstance()
                                        .sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
                                return;
                            }
                        }

						mOneKeyMenuDialog = ((OneKeyMenuDialog) ComponentsManager.getInstance()
								.getComponentById(NavBasic.NAV_COMP_ID_ONE_KEY_DIALOG));

                        if (null == mOneKeyMenuDialog) {
                            return;
                        }

						boolean isShowing = mOneKeyMenuDialog.isShowing();
						Log.d(TAG, "dispatchTest||isShowing =" + isShowing);
						TvSingletons.getSingletons().getInputSourceManager().removeMessageForOnKey();
						if (localKeyEvents.getAction() == KeyEvent.ACTION_DOWN) {
							if (isShowing) {
								mOneKeyMenuDialog.resetHandler();
							}
							if(duration < 3000L && DestroyApp.isCurActivityInputsPanel()) {
							  return;
							}
						} else if (localKeyEvents.getAction() == KeyEvent.ACTION_UP) {
						  if(DestroyApp.isCurActivityInputsPanel()) {
						    if (duration > 0L && duration < 1500L) {
						      Log.d(TAG, "inputs panel dispatchTest||DPAD_DOWN");
						      TvSingletons.getSingletons().getInputSourceManager().notifyInputsPanelFocusToNext();
						    } else if (1500L <= duration && duration < 3000L){
						      Log.d(TAG, "inputs panel dispatchTest||DPAD_CENTER");
                  InstrumentationHandler.getInstance()
                      .sendKeyDownUpSync(KeyMap.KEYCODE_DPAD_CENTER);
						    }
						    return;
						  }
							if (!isShowing) {
								if (0L <= duration && duration < 3000L) {
									mOneKeyMenuDialog.show();
								}
							} else {
								Log.d(TAG,"dispatchTest||showing||duration =" + duration);
								if (0L < duration && duration < 1500L) {
									Log.d(TAG, "dispatchTest||DPAD_RIGHT");
									InstrumentationHandler.getInstance()
											.sendKeyDownUpSync(KeyMap.KEYCODE_DPAD_RIGHT);
									return;
								} else if (1500L <= duration && duration < 3000L) {
									Log.d(TAG,"dispatchTest||click||peformClick");
									mOneKeyMenuDialog.performClick();
									return;
								}
							}
						}
					}
					return;
				}

			}
            if (DataSeparaterUtil.getInstance() != null
                    && (DataSeparaterUtil.getInstance().getKeypadNum() == 5)) {
                if (localKeyEvents.getDeviceId() == 3) {
                    if (keycodes == KeyEvent.KEYCODE_TV_INPUT) {
                        if (localKeyEvents.getAction() == KeyEvent.ACTION_DOWN) {
                            if (localKeyEvents.getRepeatCount() >= 5) {
                                keyFlag = true;
                                if (isScreenOn(context)) {
                                    InstrumentationHandler.getInstance()
                                            .sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
                                    return;
                                }
                            } else {
                                keyFlag = false;
                            }

                        }

					if (!keyFlag
							&& !isScreenOn(context)
							&& (localKeyEvents.getAction() == KeyEvent.ACTION_UP)) {
                            Log.d(TAG, "power: ON");
						((PowerManager) (context
								.getSystemService(Context.POWER_SERVICE)))
								.wakeUp(SystemClock.uptimeMillis());
						return;
					}
//                    if (!isScreenOn(context)
//                            && (localKeyEvents.getAction() == KeyEvent.ACTION_UP)) {
//                        keyFlag = false;
//					}
                        if (!DestroyApp.isCurActivityTkuiMainActivity() && !DestroyApp.isCurActivityInputsPanel()) {
                            if (localKeyEvents.getRepeatCount() == 0
								&& (localKeyEvents.getAction() == KeyEvent.ACTION_UP)) {
							Log.d(TAG, "enter key");
							InstrumentationHandler.getInstance()
									.sendKeyDownUpSync(
											KeyEvent.KEYCODE_DPAD_CENTER);// KEYCODE_DPAD_CENTER
							return;
                            }

                        }
                    }
                }
            }
			if (!SystemsApi.isUserSetupComplete(context)) {
				Log.d(TAG, "User Setup not Complete!");
				return;
			}

			SystemsApi.dayDreamAwaken(context);

			if (MtkTvScan.getInstance().isScanning() || SDXFileParser.getInstance(context).isScanning()) {
				return;
			}
        String istimewizard = VendorProperties.mtk_tshift_disk_setup_testing().orElse("0");
        Log.d(TAG,"istimewizard-->"+istimewizard);
        if("0".equals(istimewizard)){
			setTVKey(intent, mContext);
            }
			if(keycodes == KeyEvent.KEYCODE_SETTINGS){
				Log.d(TAG, "Setting key process");
				if (localKeyEvents.getAction() == KeyEvent.ACTION_DOWN){
					if(localKeyEvents.getRepeatCount() == 5) {
						isLongPress = true;
						Log.d(TAG, "localKeyEvent.getRepeatCount() ACTION_DOWN" + localKeyEvents.getRepeatCount());
						if(isTargetAppRunning(context,"com.mediatek.wwtv.tvcenter")){
							if(DataSeparaterUtil.getInstance().isSoftKeySupport()){
								if(!SoftKeyBoard.getInstance(context).isShowing()){
									SoftKeyBoard.getInstance(context).show();
								}
							}

						}
						return;
					}
				}

                if (localKeyEvents.getAction() == KeyEvent.ACTION_UP) {
                    if(localKeyEvents.getRepeatCount() == 0) {
                        Log.d(TAG, "localKeyEvent.getRepeatCount() ACTION_UP" + localKeyEvents.getRepeatCount());
                        if(isLongPress){
                            isLongPress = false;
                            return;
                        }
                        Activity activity = DestroyApp.getTopActivity();
                        if(activity instanceof TurnkeyUiMainActivity){
                            //key menu
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "turnkey ui show menu");
                            ((TurnkeyUiMainActivity)activity).onKeyHandler(KeyMap.KEYCODE_MENU, new KeyEvent(KeyEvent.ACTION_UP,KeyMap.KEYCODE_MENU),false);
                        } else {
                            // TvSettings
                            if(!isTargetAppRunning(context,"com.android.tv.settings")){
                                if (isTargetAppRunning(context,"com.google.android.tvlauncher")) {
                                    showSetting(context);
                                 } else {
                                    int action = DataSeparaterUtil.getInstance().getSettingsKeyActionInOtherApps();
                                    if (DataSeparaterUtil.SETTINGS_KEY_SHOW_PIC_SOUND == action) {
                                        showSimpleSetting(context);
                                    } else if (DataSeparaterUtil.SETTINGS_KEY_NO_ACTION == action) {
                                        // do nothing
                                    } else {
                                        showSetting(context);
                                    }
                                 }
                            }
                        }
                    }
                }
			}

			ComponentsManager mNavCompsMagr = ComponentsManager.getInstance();
		      boolean channellistd=false;

		      NavBasicDialog channelListDialog = (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
		      if(channelListDialog != null && channelListDialog.isShowing()){
		          channellistd =true;
		      }

			StateDvrFileList stateDvrFileList = DvrManager.getInstance() == null ? null
					: StateDvrFileList.getInstance(DvrManager.getInstance());
			CIMainDialog ciMainDialog = ((CIMainDialog) mNavCompsMagr
					.getComponentById(CIMainDialog.NAV_COMP_ID_CI_DIALOG));
			MenuOptionMain menuOptionMain = ((MenuOptionMain) mNavCompsMagr
					.getComponentById(NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG));
			boolean isshow = channellistd
					|| (stateDvrFileList != null && stateDvrFileList
							.isShowing())
					|| (ciMainDialog != null && ciMainDialog.isShowing())
					|| (menuOptionMain != null && menuOptionMain.isShowing());
			if (MarketRegionInfo
					.isFunctionSupport(MarketRegionInfo.F_ACR_SUPPORT)) {
				Intent globalBtnIntent = new Intent(
						"tv.samba.ssm.GLOBAL_BUTTON");
				globalBtnIntent
						.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				context.sendBroadcast(globalBtnIntent);
				Log.d(TAG, "send tv.samba.ssm.GLOBAL_BUTTON intent");
			}
            if (DataSeparaterUtil.getInstance() != null
                    && (DataSeparaterUtil.getInstance().getKeypadNum() == 5)
                    && !DestroyApp.isCurActivityInputsPanel()) {
                Log.d(TAG, "localKeyEvents =" + localKeyEvents);
                if (localKeyEvents.getDeviceId() == 3) {
                    if (localKeyEvents.getRepeatCount() == 0
                            && keycodes == KeyEvent.KEYCODE_TV_INPUT
                            && localKeyEvents.getAction() == KeyEvent.ACTION_UP) {
                        if (!isshow) {
                            Log.d(TAG, "Key Num 5, Show Menu");
                            InstrumentationHandler.getInstance()
                                    .sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
                        } else {
                            Log.d(TAG, "Key Num 5, send enter key");
                            InstrumentationHandler.getInstance()
                                    .sendKeyDownUpSync(
                                            KeyEvent.KEYCODE_DPAD_CENTER);
                        }
                        return;
                    }
                }
            }

            if (!DestroyApp.isCurActivityInputsPanel()) {
                if (!MtkTvScan.getInstance().isScanning()
                        && !isCamUpgradeing(context)) {
                    Log.d(TAG,
                            "event="
                                    + localKeyEvents.getFlags()
                                    + ",keycode="
                                    + keycodes
                                    + ",is true="
                                    + (localKeyEvents.getFlags() & KeyEvent.FLAG_LONG_PRESS));
                    /*if (DataSeparaterUtil.getInstance() != null
                            && (DataSeparaterUtil.getInstance().getKeypadNum() == 5)
                            && localKeyEvents.getDeviceId() == 3) {

                        if (localKeyEvents.getAction() == KeyEvent.ACTION_UP)
                            if (localKeyEvents.getRepeatCount() == 0
                                    && keycodes == KeyEvent.KEYCODE_TV_INPUT) {

                                InstrumentationHandler.getInstance()
                                        .sendKeyDownUpSync(
                                                KeyEvent.KEYCODE_DPAD_CENTER);

                            }
                    } else*/
                    if (DataSeparaterUtil.getInstance() != null
                            && (DataSeparaterUtil.getInstance().getKeypadNum() == 7)
                            && localKeyEvents.getDeviceId() == 3) {
                        if (localKeyEvents.getRepeatCount() == 0
                                && keycodes == KeyEvent.KEYCODE_TV_INPUT
                                && (localKeyEvents.getAction() == KeyEvent.ACTION_UP)) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "Key Pad 7, !isCurActivityTkuiMainActivity,send center key");
                            InstrumentationHandler.getInstance()
                                    .sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                            return;
                        }
                    } else if (keycodes == KeyEvent.KEYCODE_TV_INPUT && (localKeyEvents.getAction() == KeyEvent.ACTION_UP)) {
                        PowerManager pm = (PowerManager) context
                                .getSystemService(mContext.POWER_SERVICE);
                        boolean interactive = pm.isInteractive();
                        boolean oadActivity = DestroyApp.isCurOADActivityNew();
                        int factory = VendorProperties.mtk_factory_disable_input().orElse(0);
                        Log.d(TAG, "PowerManager.isInteractive=" + interactive
                                + " oadActivity:" + oadActivity + " factory:"
                                + factory);
                        if (!interactive
                                || oadActivity
                                || factory == 1
//                                || ComponentsManager.getInstance().isContainsComponentsForCurrentActiveComps(NavBasicMisc.NAV_COMP_ID_TELETEXT)
                                || InputSourceManager.mInputKeyDownTime == localKeyEvents.getDownTime()) {
                            return;
                        }
                        startInputsPanel(mContext);
                        InputSourceManager.mInputKeyDownTime = localKeyEvents.getDownTime();
                    }
                    return;// no TkuiMainActivity, no handl
                } else {
                    return;
                }
            }

			KeyEvent localKeyEvent = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (keycode == KeyEvent.KEYCODE_TV_INPUT) {
				handleInputKey(keycode, localKeyEvent);
			}
			return;
		} else if (intent.getAction().compareTo(
				"android.mediatek.intent.logcattousb") == 0) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag = intent.getBooleanExtra("status", false);
		} else if (intent.getAction().compareTo(DBHelper.SCHEDULE_ALARM_ACTION) == 0) {
			DvrManager.getInstance().getController()
					.handleRecordNotify(intent);
			return;
		}

		Log.d(TAG, PACKAGE_NAME);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Intent intent = mContext.getPackageManager()
							.getLaunchIntentForPackage(PACKAGE_NAME);
					if (intent != null) {
						Log.d(TAG, intent.getAction());
					}
					// mContext.startActivity(intent);
				} catch (Exception e) {
					Log.d(TAG, e.getMessage(), e);
				}
			}
		}).start();
	}

	public void startInputsPanel(Context context) {
		Intent intent = new Intent("com.android.tv.action.VIEW_INPUTS");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setPackage("com.mediatek.wwtv.tvcenter");
		intent.putExtra("extra_is_turnkey_top_running", DestroyApp.isCurActivityTkuiMainActivity());
		context.getApplicationContext().startActivity(intent);
	}

	private static int isKeyUpTwice = 0;

	private void handleInputKey(int keycode, KeyEvent event) {
		if (keycode == KeyEvent.KEYCODE_TV_INPUT) {
			Log.d(TAG,
					"event.getAction =" + event.getAction() + " "
							+ event.getScanCode());

			if (event.getAction() == KeyEvent.ACTION_UP
					&& event.getScanCode() != 0) {
				Log.d(TAG, "ACTION_UP =" + event);
				isKeyUpTwice = 0;
				return;
			} else if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getScanCode() != 0) {
				Log.d(TAG, "ACTION_down =" + event);
			} else if ((event.getAction() == KeyEvent.ACTION_UP || event
					.getAction() == KeyEvent.ACTION_DOWN)
					&& event.getScanCode() == 0) {
				Log.d(TAG, "isKeyUpTwice =" + isKeyUpTwice);
				++isKeyUpTwice;

				if (isKeyUpTwice == 2) {
					isKeyUpTwice = 0;
					Log.d(TAG, "isKeyUp == 2");
					return;
				}

				Log.d(TAG, "ACTION_UP twice" + isKeyUpTwice);
			}
		} else {
			Log.d(TAG, "should never happen! keycode = " + keycode + event);
		}

		// if teletext is active input key no response
//		if (ComponentsManager.getInstance().isContainsComponentsForCurrentActiveComps(NavBasicMisc.NAV_COMP_ID_TELETEXT)) {
//			return;
//		}

		if (DvrManager.getInstance() != null
				&& DvrManager.getInstance().getState() instanceof StateDvrFileList) {
			StateDvrFileList stateFileList = (StateDvrFileList) DvrManager
					.getInstance().getState();
			if (stateFileList.isShowing()) {
				return;
			}
		}

		if (DestroyApp.isCurActivityInputsPanel()) {
			ComponentStatusListener.getInstance().updateStatus(
					ComponentStatusListener.NAV_INPUTS_PANEL_SOURCE_KEY);
      	    InputSourceManager.mInputKeyDownTime = event.getDownTime();
		}
	}

	private boolean isCamUpgradeing(Context context) {
		return CIStateChangedCallBack.getInstance(context).camUpgradeStatus();
	}

	private boolean isScreenOn(Context context) {
		DisplayManager dm = (DisplayManager) context
				.getSystemService(Context.DISPLAY_SERVICE);
		for (Display displayManager : dm.getDisplays()) {
			if (displayManager.getState() != Display.STATE_OFF) {
				Log.d(TAG, "isScreenOn||true");
				return true;
			}
		}
		return false;
	}

	private void setTVKey(Intent intent, Context mContext) {
		Log.d(TAG, "set TV key");
		KeyEvent localKeyEvents = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		int keycodes = localKeyEvents.getKeyCode();
		if (keycodes == KeyEvent.KEYCODE_TV || keycodes == KeyEvent.KEYCODE_GUIDE) {
		    TTXMain ttxMain = (TTXMain) ComponentsManager.getInstance().getComponentById(
                    NavBasicMisc.NAV_COMP_ID_TELETEXT);
            Log.d("BootBroadcastReceiver", "setTVKey||isActive");
            if (ttxMain != null && ttxMain.isActive) {
                ttxMain.stopTTX();
            }
			if (!DestroyApp.isCurActivityTkuiMainActivity()) {
			  Intent tvInput = new Intent("android.mtk.intent.action.ACTION_REQUEST_TOP_RESUME");
			  tvInput.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  mContext.getApplicationContext().startActivity(tvInput);
			} else {
				if ((localKeyEvents.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyFlag)) {
					Log.d(TAG, "dismiss all ui");
					dismissUI(keycodes);
				} else if (localKeyEvents.getAction() == KeyEvent.ACTION_DOWN) {
					if (localKeyEvents.getRepeatCount() >= 5) {
						keyFlag = true;
					} else {
						keyFlag = false;
					}
				} else if (!keyFlag
						&& localKeyEvents.getAction() == KeyEvent.ACTION_UP
						&& localKeyEvents.getRepeatCount() == 0) {
					Log.d(TAG, "dismiss all ui +");
					dismissUI(keycodes);
				}
			}
		}
	}

	private void dismissUI(int keycodes) {
		if (ComponentsManager.getNativeActiveCompId() != 0) {
			Log.d(TAG, "native ui");
			MtkTvKeyEvent mtkKeyEvent = MtkTvKeyEvent.getInstance();
			int dfbkeycode = mtkKeyEvent.androidKeyToDFBkey(keycodes);
			mtkKeyEvent.sendKeyClick(dfbkeycode);
		}
		if (ComponentsManager.getInstance().isComponentsShow()) {
			ComponentsManager.getInstance().hideAllComponents(NavBasic.NAV_COMP_ID_BANNER,NavBasic.NAV_COMP_ID_TWINKLE_DIALOG, NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
		}
		if (StateDvrFileList.getInstance()!=null&&StateDvrFileList.getInstance().isShowing()) {
			StateDvrFileList.getInstance().dissmiss();
		}
		if (ScheduleListDialog.getDialog() != null
				&& ScheduleListDialog.getDialog().isShowing()) {
			// CR 959741 ,follow linux ,dismiss schedule dialog when confirm
			// dialog show.
			ScheduleListDialog.getDialog().dismiss();
		}
		if (ScheduleListItemInfoDialog.getscheduleListItemInfoDialog() != null
				&& ScheduleListItemInfoDialog.getscheduleListItemInfoDialog()
						.isShowing()) {
			ScheduleListItemInfoDialog.getscheduleListItemInfoDialog()
					.dismiss();
		}
        if(HbbtvAudioSubtitleDialog.getHbbtvDialog()!=null
                &&HbbtvAudioSubtitleDialog.getHbbtvDialog().isShowing()){
            HbbtvAudioSubtitleDialog.getHbbtvDialog().dismiss();
        }
        if(DiskSettingDialog.getDiskSettingDialog()!=null
            &&DiskSettingDialog.getDiskSettingDialog().isShowing()){
            DiskSettingDialog.getDiskSettingDialog().dismiss();
        }
        if (DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog() != null
        && DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().isShowing()) {
          InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_BACK);
        }
	}


	private boolean handleChannelListButton(KeyEvent event, Context context){
		Log.i(TAG, "handleChannelListButton");
		if (!CommonUtil.isSupportFVP(true)){
			return true;
		}
		if(StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()){
			Log.i(TAG, "pvr is playing");
			return true;
		}
		if(event.getAction() == KeyEvent.ACTION_UP){
			if(DestroyApp.isCurActivityTkuiMainActivity()
					&& InputSourceManager.getInstance().isCurrentTvSource(CommonIntegration.getInstance().getCurrentFocus())){
				((TurnkeyUiMainActivity)DestroyApp.getTopActivity()).toggleChannelList();
			}else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(TvContract.Channels.CONTENT_URI);
				intent.putExtra(Constants.INTENT_EXTRA_CHANNEL_LIST, true);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
			return true;
		}
		return false;

	}

	public boolean isTargetAppRunning(Context context, String... packages) {
		boolean result = false;
		try {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			String packString = cn.getPackageName();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "packString:"+packString);
			for (String runningPKG : packages) {
				if(packString.equalsIgnoreCase(runningPKG)) {
					result = true;
					break;
				}
			}
        } catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "result->:"+result);
		return result;
	}
}
