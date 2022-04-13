package com.mediatek.wwtv.setting.view;

import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceScreen;

//import android.support.v7.preference.Preference.OnPreferenceClickListener;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;

import com.mediatek.wwtv.setting.preferences.PreferenceData;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.setting.util.TVContent;

import com.mediatek.twoworlds.tv.MtkTvAppTVBase;

public final class FacVideo implements OnPreferenceClickListener{
    private static final String TAG = "FacVideo";

    private static FacVideo mInstance = null;

    private Context mContext;
    private MtkTvAppTVBase appTV;
    private LiveTVDialog autoAdjustDialog;
    private CommonIntegration mCommonIntegration;
    private PreferenceData mPrefData;
    private int autoTimeOut = 0;
    private TVContent mTv;
    public static final int MESSAGE_AUTOADJUST = 1000;
    public static final int MESSAGE_AUTOCOLOR  = MESSAGE_AUTOADJUST + 1;
    public static final int ONE_SECOND = 1000;
    public static final int BUTTON_COUNT = 5;

    private FacVideo(Context context) {
        mContext = context;
        appTV = new MtkTvAppTVBase();
        mCommonIntegration = CommonIntegration.getInstance();
        mPrefData = PreferenceData.getInstance(mContext);
        mTv = TVContent.getInstance(mContext);
    }

    public static synchronized FacVideo getInstance(Context context) {
        if(null == mInstance) {
            mInstance = new FacVideo(context);
        }

        mInstance.mContext = context;
        return mInstance;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceClick, " + preference);

        if(preference.getKey().equals(
            mPrefData.mConfigManager.FV_AUTOCOLOR)) {
            //
            autoAdjustInfo(mContext.getString(R.string.menu_video_auto_color_info));
            appTV.setAutoColor(mCommonIntegration.getCurrentFocus());

            Message message = mHandler.obtainMessage();
            message.obj = preference;
            message.what = MESSAGE_AUTOCOLOR;
            mHandler.sendMessageDelayed(message, ONE_SECOND);
        }
        else if(preference.getKey().equals(
            mPrefData.mConfigManager.AUTO_ADJUST)) {
            //
            autoAdjustInfo(mContext.getString(R.string.menu_video_auto_adjust_info));
            appTV.setAutoClockPhasePosition(mCommonIntegration.getCurrentFocus());

            Message message = mHandler.obtainMessage();
            message.what = MESSAGE_AUTOADJUST;
            mHandler.sendMessageDelayed(message, ONE_SECOND);
        }

        return true;
    }

    public void addListener() {
        mPrefData.mTV.addSingleLevelCallBackListener(mSignalHandler);
    }

    public void removeListener() {
        mPrefData.mTV.removeSingleLevelCallBackListener(mSignalHandler);
    }

    private void autoAdjustInfo(String mShowMessage) {
        autoAdjustDialog = new LiveTVDialog(mContext, BUTTON_COUNT);
        autoAdjustDialog.setMessage(mShowMessage);
        autoAdjustDialog.show();
        autoAdjustDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog,
                                int keyCode, KeyEvent event) {
                return true;
            }
        });
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean flag;
            switch (msg.what) {
            case MESSAGE_AUTOADJUST:
                autoTimeOut ++;

                flag = appTV.AutoClockPhasePositionCondSuccess(
                    mCommonIntegration.getCurrentFocus());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MESSAGE_AUTOADJUST: " + flag + "," + autoTimeOut);

                if (flag || autoTimeOut >= 10) {
                    autoTimeOut = 0;
                    autoAdjustDialog.dismiss();
                    Toast.makeText(mContext, "Adjust Success", Toast.LENGTH_LONG).show();

                    mHandler.removeMessages(MESSAGE_AUTOADJUST);
                } else {
                    if (autoTimeOut >= 10) {
                        autoTimeOut = 0;
                        autoAdjustDialog.dismiss();
                        Toast.makeText(mContext, "Adjust Fail!!", Toast.LENGTH_LONG).show();
                    } else {
                        Message message = this.obtainMessage();
                        message.copyFrom(msg);
                        sendMessageDelayed(message, ONE_SECOND);
                    }
                }
                break;
            case MESSAGE_AUTOCOLOR:
                autoTimeOut++;

                flag = appTV.AutoColorCondSuccess(
                    mCommonIntegration.getCurrentFocus());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MESSAGE_AUTOCOLOR:" + flag + "," + autoTimeOut);

                if (flag || autoTimeOut >= 10) {
                    autoTimeOut = 0;
                    autoAdjustDialog.dismiss();
                    Toast.makeText(mContext, "Adjust Success", Toast.LENGTH_LONG).show();

                    mHandler.removeMessages(MESSAGE_AUTOCOLOR);
                } else {
                    if (autoTimeOut >= 10) {
                        autoTimeOut = 0;
                        autoAdjustDialog.dismiss();
                        Toast.makeText(mContext, "Adjust Fail!!", Toast.LENGTH_LONG).show();
                    } else {
                        Message message = this.obtainMessage();
                        message.copyFrom(msg);
                        sendMessageDelayed(message, ONE_SECOND);
                    }
                }
                break;
            default:
                break;
        }
      }
    };

    private final Handler mSignalHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TvCallbackConst.MSG_CB_SVCTX_NOTIFY) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "msg.what:" + msg.what);
                TvCallbackData backData = (TvCallbackData) msg.obj;
                switch (backData.param1) {
                    //---
                    case SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOSS:
                    {
                        PreferenceScreen mScreen = mPrefData.getData();

                        if(mPrefData.mConfigManager.FACTORY_VIDEO.equals(mScreen.getKey())) {
                            for(int i = 0; i < mScreen.getPreferenceCount(); i++) {
                                Preference tempPre = mScreen.getPreference(i);
                                if(mPrefData.mConfigManager.FV_AUTOCOLOR.equals(
                                    tempPre.getKey())) {
                                    tempPre.setEnabled(false);
                                }
                            }
                        }

                        if (autoAdjustDialog != null && autoAdjustDialog.isShowing()) {
                            autoAdjustDialog.dismiss();
                        }
                        mHandler.removeMessages(MESSAGE_AUTOADJUST);
                        mHandler.removeMessages(MESSAGE_AUTOCOLOR);
                    }
                        break;
                    //---
                    case SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOCKED:
                    {
                        PreferenceScreen mScreen = mPrefData.getData();

                        if(mPrefData.mConfigManager.FACTORY_VIDEO.equals(mScreen.getKey())) {
                            for(int i = 0; i < mScreen.getPreferenceCount(); i++) {
                                Preference tempPre = mScreen.getPreference(i);
                                if(mPrefData.mConfigManager.FV_AUTOCOLOR.equals(
                                    tempPre.getKey())) {
                                    if ((mTv.isCurrentSourceVGA()
                                            || mTv.isCurrentSourceComponent()
                                            || mTv.isCurrentSourceScart())
                                            && mTv.iCurrentInputSourceHasSignal()) {
                                        tempPre.setEnabled(true);
                                    }
                                }
                            }
                        }
                    }
                        break;
                    //---
                    case SettingsUtil.SVCTX_NTFY_CODE_SERVICE_BLOCKED:
                    {
                        PreferenceScreen mScreen = mPrefData.getData();

                        if(mPrefData.mConfigManager.FACTORY_VIDEO.equals(mScreen.getKey())) {
                            for(int i = 0; i < mScreen.getPreferenceCount(); i++) {
                                Preference tempPre = mScreen.getPreference(i);
                                if(mPrefData.mConfigManager.FV_HPOSITION.equals(
                                    tempPre.getKey()) ||
                                   mPrefData.mConfigManager.FV_VPOSITION.equals(
                                    tempPre.getKey())) {
                                    tempPre.setEnabled(false);
                                }
                            }
                        }
                    }
                        break;
                    //
                    case SettingsUtil.SVCTX_NTFY_CODE_SERVICE_UNBLOCKED:
                    {
                        PreferenceScreen mScreen = mPrefData.getData();

                        if(mPrefData.mConfigManager.FACTORY_VIDEO.equals(mScreen.getKey())) {
                            for(int i = 0; i < mScreen.getPreferenceCount(); i++) {
                                Preference tempPre = mScreen.getPreference(i);
                                if(mPrefData.mConfigManager.FV_HPOSITION.equals(
                                    tempPre.getKey()) ||
                                   mPrefData.mConfigManager.FV_VPOSITION.equals(
                                    tempPre.getKey())) {
                                    tempPre.setEnabled(true);
                                }
                            }
                        }
                    }
                        break;
                    default:
                        break;
                }//switch
            }//if
        }
    };
}

