package com.mediatek.wwtv.setting.view;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.LayoutDirection;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;

import androidx.core.text.TextUtilsCompat;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;

//import android.support.v7.preference.Preference.OnPreferenceClickListener;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;

import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.TvSettingsActivity;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.setting.preferences.PreferenceData;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.setting.util.MenuDataHelper;

import com.mediatek.twoworlds.tv.MtkTvUtil;

import java.util.Locale;

public final class FacSetup implements OnPreferenceClickListener{
    private static final String TAG = "FacSetup";

    private static final int MESSAGE_RESET = 0x22;
    private static final int MESSAGE_SEND_RESET = 0x23;
    private CommonIntegration mNavIntegration = null;
    private Context mContext;
    private PreferenceData mPrefData;
    private LiveTVDialog factroyCofirm = null;
    private EditChannel mEditChannel;
    private MenuDataHelper mDataHelper;
    private Handler mHandler;
    private ProgressDialog pdialog = null;

    public FacSetup(Context context) {
        mContext = context;
        mPrefData = PreferenceData.getInstance(mContext);
        mEditChannel = EditChannel.getInstance(mContext);
        mDataHelper = MenuDataHelper.getInstance(mContext);
        mNavIntegration = CommonIntegration.getInstance();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MESSAGE_RESET:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MESSAGE_RESET, " + System.currentTimeMillis());

                    mPrefData.mTV.cleanRatings();
                    mDataHelper.resetCallFlashStore();
                    MtkTvUtil.getInstance().resetFac();

                    mEditChannel.resetParental(mContext,
                        new Runnable() {
                            @Override
                            public void run() {
                                mHandler.sendEmptyMessage(MESSAGE_SEND_RESET);
                            }
                        });
                    break;

                default:
                    break;
                }
            }
        };
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceClick, " + preference);

        if(preference.getKey().equals(
            MenuConfigManager.FACTORY_SETUP_CI_UPDATE)) {
            factoryCi(1);
        }
        else if (preference.getKey().equals(
            MenuConfigManager.FACTORY_SETUP_CI_ECP_UPDATE)) {
            factoryCi(3);
        }
        else if(preference.getKey().equals(
            MenuConfigManager.FACTORY_SETUP_CI_ERASE)) {
            factoryCi(2);
        }
        else if(preference.getKey().equals(
            MenuConfigManager.FACTORY_SETUP_CLEAN_STORAGE)) {
            factoryCleanStorage();
        }
        else if(preference.getKey().equals(
            MenuConfigManager.RESET_DEFAULT)) {
            resetDefault();
        }
        else if (preference.getKey().equals(
            MenuConfigManager.PARENTAL_CLEAN_ALL)) {
            cleanParentalChannelConfirm();
        }else if(preference.getKey().equals(
            MenuConfigManager.TV_CHANNEL_CLEAR)){
        	cleanChannelList();
        }

        return true;
    }

    private void factoryCi(int state) {
        factroyCofirm = new LiveTVDialog(mContext, 0);
        switch (state) {
        //---
        case 0:
            factroyCofirm.setMessage(mContext.getString(
                R.string.menu_factory_setup_cleanstorage));
            break;
        //---
        case 1:
            int update = mPrefData.mTV.updateCIKey();
            if (update == 0) {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_update_success));
            } else if (update == -9) {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_key_false));
            } else {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_update_false));
            }
            break;
        case 3:
            int updateecp = mPrefData.mTV.updateCIECPKey();
            if (updateecp == 0) {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_update_success));
            } else if (updateecp == -9) {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_key_false));
            } else {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_update_false));
            }
            break;

        case 2:
            int erase = mPrefData.mTV.eraseCIKey();
            if (erase == 0) {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_erase_success));
            } else {
                factroyCofirm.setMessage(mContext.getString(
                    R.string.menu_factory_setup_ci_erase_false));
            }
            break;
        default:
            break;
        }

        factroyCofirm.show();
        factroyCofirm.setPositon(-20, 70);
    }

    private void factoryCleanStorage() {
        factroyCofirm = new LiveTVDialog(mContext, 3);
        factroyCofirm.setMessage(mContext.getString(
            R.string.menu_factory_setup_cleanstorage));
        factroyCofirm.setButtonYesName(mContext.getString(
            R.string.menu_ok));
        factroyCofirm.setButtonNoName(mContext.getString(
            R.string.menu_cancel));

        factroyCofirm.show();
        factroyCofirm.setPositon(-20, 70);
        factroyCofirm.getButtonNo().requestFocus();

        OnKeyListener listener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        if (v.getId() == factroyCofirm.getButtonYes().getId()) {
                            factroyCofirm.dismiss();
                            pdialog = ProgressDialog.show(mContext,
                                "Clean Storage", "Reseting please wait...", false, false);
                            mHandler.sendEmptyMessage(MESSAGE_RESET);
                            return true;
                        } else if (v.getId() == factroyCofirm.getButtonNo().getId()) {
                            factroyCofirm.dismiss();
                            return true;
                        }
                    }
                }
                return false;
            }
        };

        factroyCofirm.getButtonNo().setOnKeyListener(listener);
        factroyCofirm.getButtonYes().setOnKeyListener(listener);
    }

    private void resetDefault() {
        factroyCofirm = new LiveTVDialog(mContext, 3);
        factroyCofirm.setMessage(
            mContext.getString(R.string.menu_tv_reset_all));
        factroyCofirm.setButtonYesName(
            mContext.getString(R.string.menu_ok));
        factroyCofirm.setButtonNoName(
            mContext.getString(R.string.menu_cancel));
        factroyCofirm.show();
        factroyCofirm.getTextView().setTextSize(13.5f);
        factroyCofirm.getButtonNo().requestFocus();
        factroyCofirm.setPositon(-20, 70);
        factroyCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                int action = event.getAction();
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && action == KeyEvent.ACTION_DOWN) {
                    factroyCofirm.dismiss();
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
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        factroyCofirm.dismiss();
                        EditChannel.getInstance(mContext).resetDefAfterClean();
                        pdialog = ProgressDialog.show(mContext, "ResetDefault", "Reseting please wait...", false, false);
                        mPrefData.mTV.resetPub(mHandler);
                        mDataHelper.resetCallFlashStore();
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
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        factroyCofirm.dismiss();
                        return true;
                    }
                }
                return false;
            }
        };

        factroyCofirm.getButtonNo().setOnKeyListener(noListener);
        factroyCofirm.getButtonYes().setOnKeyListener(yesListener);
    }
    private void cleanParentalChannelConfirm() {
        factroyCofirm = new LiveTVDialog(mContext, 3);
        factroyCofirm.setMessage(mContext.getString(
                R.string.menu_tv_clear_channel_info));
        factroyCofirm.setButtonYesName(mContext
                .getString(R.string.menu_ok));
        factroyCofirm.setButtonNoName(mContext
                .getString(R.string.menu_cancel));
        factroyCofirm.show();
        factroyCofirm.getTextView().setTextSize(13.5f);
        factroyCofirm.getButtonNo().requestFocus();
        factroyCofirm.setPositon(-20, 70);
        factroyCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                int action = event.getAction();
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && action == KeyEvent.ACTION_DOWN) {
                    factroyCofirm.dismiss();
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
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        mDataHelper.resetCallFlashStore();
                        mEditChannel.resetDefAfterClean();
                        pdialog = ProgressDialog.show(mContext, "Clean All",
                                "Reseting please wait...",
                                false, false);
                        mEditChannel.resetParental(mContext,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        factroyCofirm.dismiss();
                                    }
                                });
                        mPrefData.mTV.resetPri(mHandler);
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
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        factroyCofirm.dismiss();
                        return true;
                    }
                }
                return false;
            }
        };

        factroyCofirm.getButtonNo().setOnKeyListener(noListener);
        factroyCofirm.getButtonYes().setOnKeyListener(yesListener);
    }

    public SimpleDialog getDvbsRescanDialog(Context mContext){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDvbsRescanDialog ");

        SimpleDialog simpleDialog = new SimpleDialog(mContext);
        simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
        simpleDialog.setContent(mContext.getString(R.string.dvbs_rescan_dialog_title));
        simpleDialog.setConfirmText(R.string.setup_next);
        simpleDialog.setCancelText(R.string.menu_cancel);
        simpleDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
            @Override
            public void onConfirmClick(int dialogId) {
                EditChannel.getInstance(mContext).cleanChannelList();
                ((TvSettingsActivity) mContext).setActionId(MenuConfigManager.DVBS_SAT_RE_SCAN);
                ((LiveTvSetting) mContext).show();
                simpleDialog.dismiss();
            }
        }, SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
        simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
            @Override
            public void onCancelClick(int dialogId) {
                simpleDialog.dismiss();
                ((TvSettingsActivity) mContext).setActionId(MenuConfigManager.TV_EU_CHANNEL);
                ((LiveTvSetting) mContext).show();
            }
        },SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
        simpleDialog.setOnBackListener(new SimpleDialog.OnBackListener() {
            @Override
            public void onBack(int dialogId) {
                ((TvSettingsActivity) mContext).setActionId(MenuConfigManager.TV_EU_CHANNEL);
                ((LiveTvSetting) mContext).show();
            }
        }, SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
        WindowManager.LayoutParams params = simpleDialog.getWindow().getAttributes();
        params.x = -300;
        simpleDialog.getWindow().setAttributes(params);
        return simpleDialog;
    }


    private void  cleanChannelList(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog start stop session getCurrentFocus "+mNavIntegration.getCurrentFocus());

        SimpleDialog simpleDialog = new SimpleDialog(mContext);
		simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
		simpleDialog.setContent(R.string.menu_tv_clear_channel_info);
		simpleDialog.setConfirmText(R.string.menu_ok);
		simpleDialog.setCancelText(R.string.menu_cancel);
		simpleDialog.setOnConfirmClickListener(new OnConfirmClickListener() {
			@Override
					public void onConfirmClick(int dialogId) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "clear all");
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog() start");
                    	EditChannel.getInstance(mContext).cleanChannelList();
                        mDataHelper.changePreferenceEnable();
                        mPrefData.invalidate(MenuConfigManager.TV_CHANNEL_CLEAR, 1);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog() start 1");
                        // if has install profile reset to broadcast
                        if (mPrefData.mTV.getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0) {
                        	mPrefData.mTV.setConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE, 0);
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog start stop session getCurrentFocus "+mNavIntegration.getCurrentFocus());
                        String mCurrentInputSourceName = "";
                        InputSourceManager inputSourceManager = InputSourceManager.getInstance();
                        if(mNavIntegration.getCurrentFocus().equals("sub")){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog sub");
                          inputSourceManager.stopPipSession();
                          mCurrentInputSourceName =
                                  inputSourceManager.getCurrentInputSourceName("sub");
                          inputSourceManager.changeCurrentInputSourceByName(mCurrentInputSourceName);

                        }else if(mNavIntegration.getCurrentFocus().equals("main")){
                            inputSourceManager.stopSession();
                            mCurrentInputSourceName =
                                    inputSourceManager.getCurrentInputSourceName("main");
                            inputSourceManager.changeCurrentInputSourceByName(mCurrentInputSourceName);
                        	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog main");
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog end stop session");
					}
		}, SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
		simpleDialog.show();
		WindowManager.LayoutParams params = simpleDialog.getWindow().getAttributes();
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
            params.x = 300;
        } else {
            params.x = -300;
        }
		simpleDialog.getWindow().setAttributes(params);

//    	String info = mContext.getString(R.string.menu_tv_clear_channel_info);
//    	factroyCofirm = new LiveTVDialog(mContext, 3);
//    	factroyCofirm.setMessage(info);
//    	factroyCofirm.setButtonYesName(mContext.getString(R.string.menu_ok));
//    	factroyCofirm.setButtonNoName(mContext.getString(R.string.menu_cancel));
//    	factroyCofirm.show();
//    	factroyCofirm.getTextView().setTextSize(13.5f);
//        factroyCofirm.getButtonNo().requestFocus();
//        factroyCofirm.setPositon(-20, 70);
//        factroyCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode,
//                    KeyEvent event) {
//                int action = event.getAction();
//                if (keyCode == KeyEvent.KEYCODE_BACK
//                        && action == KeyEvent.ACTION_DOWN) {
//                    factroyCofirm.dismiss();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        OnKeyListener yesListener = new OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (keyCode == KeyEvent.KEYCODE_ENTER
//                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
//                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
//                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog() start");
//                    	EditChannel.getInstance(mContext).cleanChannelList();
//                        factroyCofirm.dismiss();
//                        mDataHelper.changePreferenceEnable();
//                        mPrefData.invalidate(MenuConfigManager.TV_CHANNEL_CLEAR, 1);
//                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog() start 1");
//                        // if has install profile reset to broadcast
//                        if (mPrefData.mTV.getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE) > 0) {
//                        	mPrefData.mTV.setConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE, 0);
//                        }
//                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog start stop session getCurrentFocus "+mNavIntegration.getCurrentFocus());
//                        String mCurrentInputSourceName = "";
//                        InputSourceManager inputSourceManager = InputSourceManager.getInstance();
//                        if(mNavIntegration.getCurrentFocus().equals("sub")){
//                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog sub");
//                          inputSourceManager.stopPipSession();
//                          mCurrentInputSourceName =
//                                  inputSourceManager.getCurrentInputSourceName("sub");
//                          inputSourceManager.changeCurrentInputSourceByName(mCurrentInputSourceName);
//
//                        }else if(mNavIntegration.getCurrentFocus().equals("main")){
//                            inputSourceManager.stopSession();
//                            mCurrentInputSourceName =
//                                    inputSourceManager.getCurrentInputSourceName("main");
//                            inputSourceManager.changeCurrentInputSourceByName(mCurrentInputSourceName);
//                        	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog main");
//                        }
//                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCleanDialog end stop session");
//
//                        return true;
//                    }
//                }
//                return false;
//            }
//        };
//
//        OnKeyListener noListener = new OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (keyCode == KeyEvent.KEYCODE_ENTER
//                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
//                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
//                        factroyCofirm.dismiss();
//                        return true;
//                    }
//                }
//                return false;
//            }
//        };
//
//        factroyCofirm.getButtonNo().setOnKeyListener(noListener);
//        factroyCofirm.getButtonYes().setOnKeyListener(yesListener);

    }
}

