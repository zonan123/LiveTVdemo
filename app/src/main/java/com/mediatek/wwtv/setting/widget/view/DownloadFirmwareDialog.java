package com.mediatek.wwtv.setting.widget.view;

import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;
import com.mediatek.twoworlds.tv.MtkTvUpgrade;
import com.mediatek.twoworlds.tv.model.MtkTvUpgradeDeliveryTypeBase;
import com.mediatek.wwtv.tvcenter.R;

//import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DownloadFirmwareDialog extends Dialog{
    private static final String TAG = "DownloadFirmwareDialog";

    private Button cancelButton;
    private Button okButton;
    private TextView downloadProgressTv;
    private TextView downloadProgressValueTv;
    private TextView downloadStatusTv;
    private TextView downloadStatusValueTv;
    private TextView downloadUserToastTv;
    private TextView downloadOperateEnterTv;
    private TextView downloadOperateExitTv;
    private LinearLayout progressAndStatusLy;
    Window window;
    WindowManager.LayoutParams lp;

    private String firmwareDownloadPath = "http://www.mediatek.com/upgrade/upgrade_loader.pkg";
    private String firmwareStorePath = "/upgrade/upgrade_loader.pkg";

    public int width = 0;
    public int height = 0;

    private static final int DOWNLOAD_STATE = 0;
    private static final int VALIDATING_STATE = 1;
    private static final int UPGRADE_STATE = 2;

    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_UPDATE_DOWNLOAD = 2;

    private static final int DOWNLOAD_START = 0;
    private static final int DOWNLOAD_DOWNLOADING = 1;
    private static final int DOWNLOAD_COMPLETE = 2;
    private static final int DOWNLOAD_VALIDATING = 3;
    private static final int DOWNLOAD_VALIDATE_COMPLETE = 4;
    private static final int DOWNLOAD_EXIT = 5;

    private static final int DOWNLOAD_EXIT_NONE = 0;
    private static final int DOWNLOAD_EXIT_CANCEL_DOWN = 1;
    private static final int DOWNLOAD_EXIT_NETWORK_ERROR = 2;
    private static final int DOWNLOAD_EXIT_VALIDATE_ERROR = 3;
    private static final int DOWNLOAD_EXIT_FAIL = 4;

    private Handler downloadFirmWareHandler;

    private static final int MSG_DOWNLOAD_FIRMWARE_STATE = 4545;

    private boolean isDownloadingOrValidating = false;

    public boolean isDownloadingOrValidating() {
        return isDownloadingOrValidating;
    }

    public void setDownloadingOrValidating(boolean isDownloadingOrValidating) {
        this.isDownloadingOrValidating = isDownloadingOrValidating;
    }

    private int currentState;
    private MtkTvUpgrade tvUpgrade;
//    private FrimwareUpgradeCallBack mUpgradeCallBack;

    public DownloadFirmwareDialog(Context context) {
        super(context, R.style.Theme_TurnkeyCommDialog);
        // TODO Auto-generated constructor stub
        window = getWindow();
        lp = window.getAttributes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in onCreate");
        setContentView(R.layout.menu_download_firmware_layout);
        downloadFirmWareHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                case MSG_DOWNLOAD_FIRMWARE_STATE:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "come in downloadFirmWareHandler,MSG_DOWNLOAD_FIRMWARE_STATE");
                    Bundle bundleData = msg.getData();
                    firmwareUpgradeStateChange(bundleData.getInt("msgType"),
                            bundleData.getInt("argv1"),
                            bundleData.getInt("argv2"),
                            bundleData.getInt("argv3"));
                    break;

                default:
                    break;
                }
                super.handleMessage(msg);
            }
        };

        initView();
        tvUpgrade = MtkTvUpgrade.getInstance();
//        mUpgradeCallBack = new FrimwareUpgradeCallBack();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in onStart");
        showDialogWithState(DOWNLOAD_STATE);
    }

    private void initView() {
        progressAndStatusLy = (LinearLayout) findViewById(R.id.download_firmware_progress_and_status_ly);
        okButton = (Button) findViewById(R.id.download_firmware_ok_bt);
        okButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "okButton click, " + currentState);
                switch (currentState) {
                case DOWNLOAD_STATE:
                    progressAndStatusLy.setVisibility(View.VISIBLE);
                    downloadUserToastTv.setVisibility(View.GONE);
                    okButton.setEnabled(false);
                    okButton.setFocusable(false);
                    okButton.setTextColor(Color.GRAY);
                    cancelButton.setEnabled(true);
                    cancelButton.setFocusable(true);
                    cancelButton.setTextColor(DownloadFirmwareDialog.this
                            .getContext().getResources()
                            .getColor(R.color.white));
                    cancelButton.requestFocus();
                    tvUpgrade.startDownloadFirmware(
                            MtkTvUpgradeDeliveryTypeBase.INTERNET,
                            firmwareDownloadPath, firmwareStorePath);
                    setDownloadingOrValidating(true);
                    downloadOperateExitTv.setVisibility(View.INVISIBLE);
                    break;
                case VALIDATING_STATE:
                    break;
                case UPGRADE_STATE:
                    tvUpgrade.startRebootUpgrade(
                        MtkTvUpgradeDeliveryTypeBase.INTERNET);
                    break;
                default:
                    break;
                }
            }
        });
        cancelButton = (Button) findViewById(R.id.download_firmware_cancel_bt);
        cancelButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelButton click, " + currentState);
                switch (currentState) {
                case DOWNLOAD_STATE:
                    cancelButton.setEnabled(false);
                    cancelButton.setFocusable(false);
                    cancelButton.setTextColor(Color.GRAY);
                    tvUpgrade.cancelDownloadFirmware(
                        MtkTvUpgradeDeliveryTypeBase.INTERNET);
                    break;
                case VALIDATING_STATE:
                    break;
                case UPGRADE_STATE:
                    //MenuMain.getInstance().exitDownloadFirmware();
                    break;
                default:
                    break;
                }
            }
        });

        setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog,
                int keyCode, KeyEvent event) {
                if(event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKey, " + keyCode);

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (isDownloadingOrValidating()) {
                        return true;
                    }
                    dismiss();
                }

                return false;
            }
        });

        downloadProgressTv = (TextView) findViewById(R.id.download_firmware_progress_tv);
        downloadStatusTv = (TextView) findViewById(R.id.download_firmware_status_tv);
        downloadUserToastTv = (TextView) findViewById(R.id.download_firmware_user_toast);
        downloadProgressValueTv = (TextView) findViewById(R.id.download_firmware_progress_value_tv);
        downloadStatusValueTv = (TextView) findViewById(R.id.download_firmware_status_value_tv);
        downloadOperateEnterTv = (TextView) findViewById(R.id.download_firmware_enter_toast);
        downloadOperateExitTv = (TextView) findViewById(R.id.download_firmware_exit_toast);
        Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
        width = (int) (outSize.x * 0.45);
        lp.width = width;
        height = (int) (outSize.y * 0.2);
        lp.height = height;
        window.setAttributes(lp);
        showDialogWithState(DOWNLOAD_STATE);
    }

    private void showDialogWithState(int dialogState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDialogWithState, " + dialogState);
        switch (dialogState) {
        case DOWNLOAD_STATE:
            progressAndStatusLy.setVisibility(View.GONE);
            okButton.setText(R.string.download_firmware_start_button_text);
            cancelButton.setText(R.string.menu_cancel);
            cancelButton.setFocusable(false);
            cancelButton.setEnabled(false);
            cancelButton.setTextColor(Color.GRAY);
            downloadProgressTv
                    .setText(R.string.download_firmware_progress_textview_text);
            downloadStatusTv
                    .setText(R.string.download_firmware_status_textview_text);
            downloadUserToastTv
                    .setText(R.string.download_firmware_toast_textview_to_start_text);
            downloadUserToastTv.setVisibility(View.VISIBLE);
            currentState = DOWNLOAD_STATE;
            break;
        case VALIDATING_STATE:
            okButton.setText(R.string.download_firmware_reboot_button_text);
            okButton.setEnabled(false);
            okButton.setFocusable(false);
            okButton.setTextColor(Color.GRAY);
            cancelButton.setEnabled(false);
            cancelButton.setFocusable(false);
            cancelButton.setTextColor(Color.GRAY);
            if (View.VISIBLE != downloadUserToastTv.getVisibility()) {
                downloadUserToastTv.setVisibility(View.VISIBLE);
                progressAndStatusLy.setVisibility(View.GONE);
            }
            downloadUserToastTv
                    .setText(R.string.download_firmware_toast_textview_validating_text);
            downloadOperateEnterTv.setVisibility(View.INVISIBLE);
            downloadOperateExitTv.setVisibility(View.INVISIBLE);
            currentState = VALIDATING_STATE;
            break;
        case UPGRADE_STATE:
            okButton.setEnabled(true);
            okButton.setFocusable(true);
            okButton.requestFocus();
            okButton.setTextColor(this.getContext().getResources()
                    .getColor(R.color.white));
            cancelButton.setEnabled(true);
            cancelButton.setFocusable(true);
            cancelButton.setTextColor(this.getContext().getResources()
                    .getColor(R.color.white));
            downloadUserToastTv
                    .setText(R.string.download_firmware_toast_textview_upgrade_text);
            downloadOperateEnterTv.setVisibility(View.VISIBLE);
            downloadOperateExitTv.setVisibility(View.VISIBLE);
            currentState = UPGRADE_STATE;
            break;
        default:
            break;
        }
    }

    public Button getButtonOk() {
        return okButton;
    }

    public Button getButtonCancel() {
        return cancelButton;
    }

    /**
     * Set the dialog's position relative to the (0,0)
     */
    public void setPositon(int xoff, int yoff) {
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = xoff;
        lp.y = yoff;
        window.setAttributes(lp);
    }

    /**
     * @param msgType
     *            0: UPDATE_STATE 1: UPDATE_PROGRESS 2: UPDATE_DOWNLOAD
     *
     * @param argv1
     *            if msgType == UPDATER_STATE, argv1 is present the upgrade
     *            status<BR>
     *            0: STATE_INIT<BR>
     *            1: STATE_PREPROCESSING<BR>
     *            2: STATE_VALIDATING<BR>
     *            3: STATE_PARSING<BR>
     *            4: STATE_INITED<BR>
     *            5: STATE_DOWNLOADING<BR>
     *            6: STATE_COMPLETE_ONE<BR>
     *            7: STATE_COMPLETE<BR>
     *            8: STATE_FAILED<BR>
     *            9: STATE_DEAD<BR>
     *            10: STATE_EXIT<BR>
     *
     *            else if msgType == UPDATE_PROGRESS, agrv1 is present the
     *            upgrade tag index<BR>
     *
     *            else if msgType == UPDATE_DOWNLOAD, argv1 is present the
     *            download state 0: DOWNLOAD_START<BR>
     *            1: DOWNLOAD_DOWNLOADING<BR>
     *            2: DOWNLOAD_COMPLETE<BR>
     *            3: DOWNLOAD_VALIDATING<BR>
     *            4: DOWNLOAD_VALIDATE_COMPLETE<BR>
     *            5: DOWNLOAD_EXIT<BR>
     *
     * @param argv2
     * <BR>
     *            if msgType == UPDATE_STATE && argv1 == STATE_EXIT, argv2 is
     *            present the exit reason<BR>
     *            0: EXIT_REASON_NONE<BR>
     *            1: EXIT_REASON_USB_DEV_FAIL<BR>
     *            2: EXIT_REASON_NETWORK_FAIL<BR>
     *            3: EXIT_REASON_CHECKING_UPDATE_FAIL<BR>
     *            4: EXIT_REASON_NO_UPDATE<BR>
     *            5: EXIT_REASON_DECRYP_FAIL<BR>
     *            6: EXIT_REASON_VALIDATE_FAIL<BR>
     *            7: EXIT_REASON_PARSE_FAIL<BR>
     *            8: EXIT_REASON_INTRNL_ERR<BR>
     *            9: EXIT_REASON_USB_NOT_READY<BR>
     *            10: EXIT_REASON_USB_SPACE_NOT_ENOUGH<BR>
     *
     *            else if msgType == UPDATE_PROGRESS, argv2 is present the the
     *            percent of retriving data or updatering data<BR>
     *
     *            else if msgType == UPDATE_DOWNLOAD && argv1 ==
     *            DOWNLOAD_DOWNLOADING, argv2 is present the percent of download
     *            data <BR>
     *
     *            else if msgType == UPDATE_DOWNLOAD && argv1 == DOWNLOAD_EXIT <BR>
     *            0: DOWNLOAD_EXIT_NONE<BR>
     *            1: DOWNLOAD_EXIT_CANCEL_DOWN<BR>
     *            2: DOWNLOAD_EXIT_NETWORK_ERROR<BR>
     *            2: DOWNLOAD_EXIT_VALIDATE_ERROR<BR>
     *            3: DOWNLOAD_EXIT_FAIL<BR>
     */

    private void firmwareUpgradeStateChange(int msgType, int argv1, int argv2,
            int argv3) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in firmwareUpgradeStateChange,msgType =" + msgType
                + ", argv1 =" + argv1 + ", argv2 =" + argv2 + ", argv3 ="
                + argv3);
        switch (msgType) {
        case MSG_UPDATE_STATE:
            break;
        case MSG_UPDATE_PROGRESS:
            break;
        case MSG_UPDATE_DOWNLOAD:
            switch (argv1) {
            case DOWNLOAD_START:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in firmwareUpgradeStateChange MSG_UPDATE_DOWNLOAD,DOWNLOAD_START");
                downloadStatusValueTv
                        .setText(R.string.download_firmware_status_value_start);
                setDownloadingOrValidating(true);
                break;
            case DOWNLOAD_DOWNLOADING:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in firmwareUpgradeStateChange MSG_UPDATE_DOWNLOAD,DOWNLOAD_DOWNLOADING");
                downloadStatusValueTv
                        .setText(R.string.download_firmware_status_value_downloading);
                downloadProgressValueTv.setText(String.format("%d%%", argv2));
                setDownloadingOrValidating(true);
                break;
            case DOWNLOAD_COMPLETE:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in firmwareUpgradeStateChange MSG_UPDATE_DOWNLOAD,DOWNLOAD_COMPLETE");
                downloadStatusValueTv
                        .setText(R.string.download_firmware_status_value_complete);
                setDownloadingOrValidating(true);
                break;
            case DOWNLOAD_VALIDATING:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in firmwareUpgradeStateChange MSG_UPDATE_DOWNLOAD,DOWNLOAD_VALIDATING");
                showDialogWithState(VALIDATING_STATE);
                setDownloadingOrValidating(true);
                break;
            case DOWNLOAD_VALIDATE_COMPLETE:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "come in firmwareUpgradeStateChange MSG_UPDATE_DOWNLOAD,DOWNLOAD_VALIDATE_COMPLETE");
                downloadUserToastTv
                        .setText(R.string.download_firmware_toast_textview_validate_complete);
                setDownloadingOrValidating(true);
                break;
            case DOWNLOAD_EXIT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in firmwareUpgradeStateChange MSG_UPDATE_DOWNLOAD,DOWNLOAD_EXIT,argv2 ="
                                + argv2);
                switch (argv2) {
                case DOWNLOAD_EXIT_NONE:
                    // downloadUserToastTv
                    // .setText(R.string.download_firmware_toast_textview_download_exit_none);
                    showDialogWithState(UPGRADE_STATE);
                    // downloadExitWithError();
                    break;
                case DOWNLOAD_EXIT_CANCEL_DOWN:
                    downloadStatusValueTv
                            .setText(R.string.download_firmware_status_value_cancel_down);
                    okButton.setEnabled(true);
                    okButton.setFocusable(true);
                    okButton.setTextColor(getContext().getResources()
                            .getColor(R.color.white));
                    okButton.requestFocus();
                    downloadOperateExitTv.setVisibility(View.VISIBLE);
                    setDownloadingOrValidating(false);
                    break;
                case DOWNLOAD_EXIT_NETWORK_ERROR:
                    downloadUserToastTv
                            .setText(R.string.download_firmware_toast_textview_download_exit_network_error);
                    downloadExitWithError();
                    break;
                case DOWNLOAD_EXIT_VALIDATE_ERROR:
                    downloadUserToastTv
                            .setText(R.string.download_firmware_toast_textview_download_exit_validate_error);
                    downloadExitWithError();
                    break;
                case DOWNLOAD_EXIT_FAIL:
                    downloadUserToastTv
                            .setText(R.string.download_firmware_toast_textview_download_exit_fail);
                    downloadExitWithError();
                    break;
                default:
                    break;
                }

                break;
            default:
                break;
            }
            break;
        default:
            break;
        }

    }

    private void downloadExitWithError() {
        setDownloadingOrValidating(false);
        if (View.VISIBLE != downloadOperateExitTv.getVisibility()) {
            downloadOperateExitTv.setVisibility(View.VISIBLE);
        }
        okButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
    }

   /* @Override
    public void show() {
        super.show();
    }*/

    /*@Override
    public void dismiss() {
        super.dismiss();
    }*/

    private class FrimwareUpgradeCallBack extends MtkTvTVCallbackHandler {

        public FrimwareUpgradeCallBack() {
            super();
        }

        @Override
        public int notifyUpgradeMessage(int msgType, int argv1, int argv2,
                int argv3) throws RemoteException {
            // TODO Auto-generated method stub
            Bundle bundle = new Bundle();
            bundle.putInt("msgType", msgType);
            bundle.putInt("argv1", argv1);
            bundle.putInt("argv2", argv2);
            bundle.putInt("argv3", argv3);

            Message msg = Message.obtain();
            msg.setData(bundle);
            msg.what = MSG_DOWNLOAD_FIRMWARE_STATE;

            downloadFirmWareHandler.sendMessage(msg);
            return super.notifyUpgradeMessage(msgType, argv1, argv2, argv3);
        }
    }
}
