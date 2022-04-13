
package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.NavIntegration;
import com.mediatek.wwtv.tvcenter.nav.fav.TVChannel;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.util.PipPopConstant;
import com.mediatek.wwtv.tvcenter.nav.util.PIPPOPSurfaceViewControl;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
//import com.mediatek.wwtv.tvcenter.dvr.ui.Loading;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;

/**
 * dialog factory,get types of dialogs here
 *
 * @author hs_haosun
 */

public class DvrDialog extends AlertDialog implements Runnable {
    //private TextView titleView;
    private TextView textView;
    private TextView waitView;
    private Loading loading;
    private Button buttonYes;
    private Button buttonNo;
    public final static String DISSMISS_DIALOG = "com.mediatek.dialog.dismiss";
    // dialog title
    //private String title;
    // dialog display information
    private String message;
    private final static String TAG = "DvrDialog";
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;
    private int type = 0;
    private int keyCode = 0;
    public static final int TYPE_BGM = 0;
    public static final int TYPE_Confirm = 1;
    public static final int TYPE_Normal = 2;
    public static final int TYPE_Record = 1;
    public static final int OTHER_SOURCE = 3;
    public static final int TYPE_Timeshift = 2;
    public static final int TYPE_DVR = 5 ;
    public static final int TYPE_STOP_PVR_PLAYING_ENTER_MMP = 3;
    public static final int TYPE_Change_Source = 11234;
    private static final int TYPE_ChangeChanel_UP = 11235;
    private static final int TYPE_ChangeChanel_Down = 11236;
    private static final int TYPE_ChangeChanel_Pre = 11237;
    public static final int TYPE_Change_ChannelNum = 11238;
    public static final int TYPE_CHANNEL_Desramble = 11243;
    public static final int TYPE_CHANNEL_Desramble_SRC_KEY = 11247;
    public static final int TYPE_Change_ChannelNum_SRC = 11248;
    public static final int TIMESHIFT_RECORDLIST = 11249;
    public static final int TYPE_CHANNEL_NUMBER = 11244;
    public static final int TYPE_CHANNEL_CENTER = 11245;
    public static final int TYPE_Change_Source_By_URL = 11246;
    private static final int TYPE_PIPPOP = 11239;
    public static final int TYPE_SCHEDULE = 11240;
    public static final int TYPE_TSHIFT = 11241;
    public static final int TYPE_SCART = 11242;

    public static final int TYPE_Confirm_From_ChannelList = 0xA001;
    private static final int Msg_ID_Change_Channel = 0xA002;

    public static final int KEYCODE_FROM_FAV = 0xA002;

    public static final int TYPE_Change_Source_By_Src_Key = 0xA003;
    public static final int Menu_pip = 0x0f;
    private MtkTvBookingBase scheduleItem;

    private PvrReceiver receiver;

    private Uri uri;
    private OnDVRDialogListener onPVRDialogListener;
    //private DialogDismissRecevier dreceiver;
    private final Context mContext;
    private Activity mActivity;
    BannerView bannerView;
    int modeValue;
    int targetInputHardwareId = -1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 123:
                  InputSourceManager.getInstance().startLiveTV(mContext,targetInputHardwareId);
//                    dialog.handleCenterKey();
                    break;
                case TYPE_Change_Source_By_Src_Key:
//                    dialog.handleSourceKey();
                    String focus = CommonIntegration.getInstance().getCurrentFocus();
                    if(DestroyApp.isCurActivityTkuiMainActivity()) {
                      InputSourceManager.getInstance().autoChangeToNextInputSource(
                          CommonIntegration.getInstance().getCurrentFocus());
                    } else {
                      InputSourceManager.getInstance().startLiveTV(mActivity,
                          InputSourceManager.getInstance().getNextEnableInputSourceHardwareId(focus));
                    }
                    break;
                case TYPE_ChangeChanel_UP:
                    CommonIntegration.getInstance().channelUp();
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                    break;
                case TYPE_ChangeChanel_Down:
                    CommonIntegration.getInstance().channelDown();
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                    break;
                case TYPE_ChangeChanel_Pre:
                    CommonIntegration.getInstance().channelPre();
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                    break;
                case TYPE_PIPPOP:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e("pippop", "pippop:");
                    if (IntegrationZoom.ZOOM_1 != IntegrationZoom.getInstance(
                            mContext).getCurrentZoom()) {
                        IntegrationZoom.getInstance(mContext).setZoomModeToNormal();
                    }
                    if (MarketRegionInfo
                            .isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
                        ((MultiViewControl) ComponentsManager.getInstance()
                                .getComponentById(NavBasic.NAV_COMP_ID_POP))
                                .setModeToPIP();
                    } else {
                        if (MarketRegionInfo
                                .isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
                            PIPPOPSurfaceViewControl.getSurfaceViewControlInstance()
                                    .changeOutputWithTVState(
                                            PipPopConstant.TV_PIP_STATE);
                        }
                        KeyDispatch.getInstance().passKeyToNative(keyCode, null);
                    }
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                    break;

                case Msg_ID_Change_Channel:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e("FAV", "Msg_ID_Change_Channel:");
                    NavIntegration.getInstance(mContext).selectChannel(getFavChannel());

                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                    break;
                case TYPE_TSHIFT:
                    TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager().stop();
                    SaveValue.getInstance(mContext).setLocalMemoryValue(
                            MenuConfigManager.TIMESHIFT_START, false);
                    //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
                    SystemClock.sleep(1000);
                    DvrManager.getInstance().startSchedulePvr();
                    break;
                case TYPE_SCART:
                    InputSourceManager.getInstance().changeCurrentInputSourceByName("SCART");
                    break;
                case TYPE_CHANNEL_NUMBER:
                    if (bannerView != null && mSelectedChannelNumString != null){
                        bannerView.pvrChangeNum(mSelectedChannelNumString);
                        }
                    break;
                case TYPE_CHANNEL_CENTER:
                    if (mSelectedChannelID != -1){
                        CommonIntegration.getInstance().selectChannelById(mSelectedChannelID);
                        }
                    break;
                case TYPE_Change_Source_By_URL:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    TurnkeyUiMainActivity.getInstance().processInputUri(intent);
                    break;
                case TIMESHIFT_RECORDLIST:
                    if(!TurnkeyUiMainActivity.getInstance().isUKCountry){
                        BannerView bannerView = ((BannerView) ComponentsManager.getInstance()
                                .getComponentById(NavBasicMisc.NAV_COMP_ID_BANNER));
                        if (bannerView.isShown()) {
                            bannerView.setVisibility(View.GONE);
                        }
                    }else{
                        UkBannerView bannerView = ((UkBannerView) ComponentsManager.getInstance()
                                .getComponentById(NavBasicMisc.NAV_COMP_ID_BANNER));
                        if (bannerView.isShown()) {
                            bannerView.setVisibility(View.GONE);
                        }
                    }
                    DvrManager.getInstance().setState(StateDvrFileList.
                            getInstance(DvrManager.getInstance()));
                    ((StateDvrFileList) DvrManager.getInstance().getState()).showPVRlist();
                    break;
                default:
                    break;
            }
        };
    };

    public void registerBroadcast() {
        receiver = new PvrReceiver();
        IntentFilter filter = new IntentFilter(DISSMISS_DIALOG);
        mContext.registerReceiver(receiver, filter);
    }

    public DvrDialog(Context context, int type) {
        super(context, R.style.Theme_pvr_Dialog);
        window = getWindow();
        lp = window.getAttributes();
        this.type = type;
        mContext = context;
        mSelectedChannelID = -1;
    }

    public DvrDialog(Activity context, int type, int keyCode, int mode) {
        super(context, R.style.Theme_pvr_Dialog);
        window = getWindow();
        lp = window.getAttributes();
        this.type = type;
        this.keyCode = keyCode;
        mContext = context;
        mActivity = context;
        modeValue = mode;
    }

    public DvrDialog(Activity context, int type, int keyCode,
            int targetInputHardwareId, int mode) {
        super(context, R.style.Theme_pvr_Dialog);
        window = getWindow();
        lp = window.getAttributes();
        this.type = type;
        this.keyCode = keyCode;
        mContext = context;
        mActivity = context;
        this.targetInputHardwareId = targetInputHardwareId;
        modeValue = mode;
    }

    public DvrDialog(Activity context, int type, int keyCode,
            BannerView dialog, int mode) {
        super(context, R.style.Theme_pvr_Dialog);
        window = getWindow();
        lp = window.getAttributes();
        this.type = type;
        this.keyCode = keyCode;
        mContext = context;
        mActivity = context;
        this.bannerView = dialog;
        modeValue = mode;
        if (!CommonIntegration.getInstance().isContextInit()) {
            CommonIntegration.getInstance().setContext(mContext.getApplicationContext());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogDismissRecevier dreceiver = new DialogDismissRecevier();
        IntentFilter inf = new IntentFilter("com.mtk.dialog.dismiss");
        mContext.registerReceiver(dreceiver, inf);
        setContentView(R.layout.pvr_dialog_one_button);
        initview(type);
        setPositon(0, 0);
        registerBroadcast();
    }

    private void initview(int type) {
        TextView titleView = (TextView) findViewById(R.id.comm_dialog_title);
        textView = (TextView) findViewById(R.id.comm_dialog_text);
        buttonYes = (Button) findViewById(R.id.comm_dialog_buttonYes);
        buttonNo = (Button) findViewById(R.id.comm_dialog_buttonNo);

        buttonNo.setVisibility(View.VISIBLE);
        String title="";
        switch (type) {
            case TYPE_BGM:

                break;
            case TYPE_Confirm:
                title = "Warning:";
                titleView.setText(title);
                buttonNo.setVisibility(View.VISIBLE);

                switch (keyCode) {
                    case KeyMap.KEYCODE_MTKIR_GUIDE:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process,\n and enter EPG?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and enter EPG?";
                        }

                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);

                                if (CommonIntegration.getInstance().isCurrentSourceTv()) {
                                    EPGManager.getInstance(mActivity).startEpg(
                                            mActivity, NavBasic.NAV_REQUEST_CODE);
                                    DvrManager
                                            .getInstance()
                                            .getTopHandler()
                                            .removeMessages(
                                                    DvrConstant.RECORD_FINISH_AND_SAVE);

                                } 
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;

                    case KeyMap.KEYCODE_MTKIR_CHUP:
                    case KeyMap.KEYCODE_DPAD_UP:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change channel?";
                        } else if (modeValue == OTHER_SOURCE) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change channel?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                /*
                                 * DvrManager .getInstance() .getTopHandler() .removeMessages(
                                 * DvrConstant.RECORD_FINISH_AND_SAVE);
                                 */
                                stopTimeShift();
                                StateDvr.getInstance().setChangeSource(true);
                              mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_UP,
                                       100);
                            }

                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case KeyMap.KEYCODE_MTKIR_CHDN:
                    case KeyMap.KEYCODE_DPAD_DOWN:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change channel?";
                        } else if (modeValue == OTHER_SOURCE) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and change channel?";
                        }
                        textView.setText(message);
                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                stopTimeShift();
                                mHandler.sendEmptyMessageDelayed(
                                        TYPE_ChangeChanel_Down, 100);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case KeyMap.KEYCODE_MTKIR_PRECH:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change channel?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and change channel?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                stopTimeShift();
                                mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_Pre,
                                        100);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case DvrDialog.TYPE_Change_ChannelNum:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change channel?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and change channel?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);

                                if (mSelectedChannelID != -1) {
                                    Message msg = DvrManager.getInstance()
                                            .getTopHandler().obtainMessage();
                                    msg.what = DvrManager.CHANGE_CHANNEL;
                                    msg.arg1 = mSelectedChannelID;
                                    msg.arg2 = keyCode;
                                    DvrManager.getInstance().getTopHandler()
                                            .sendMessageDelayed(msg, 3000);
                                } else if (getOnPVRDialogListener() != null) {
                                    getOnPVRDialogListener().onDVRDialogListener(keyCode);
                                } else {
                                    mHandler.sendEmptyMessageDelayed(TYPE_CHANNEL_NUMBER, 3000);
                                }
                            }
                        });

                        buttonNo.setVisibility(View.VISIBLE);
                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case DvrDialog.TYPE_Change_ChannelNum_SRC:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                if (mSelectedChannelID != -1) {
                                    Message msg = DvrManager.getInstance()
                                            .getTopHandler().obtainMessage();
                                    msg.what = DvrManager.CHANGE_CHANNEL;
                                    msg.arg1 = mSelectedChannelID;
                                    msg.arg2 = keyCode;
                                    DvrManager.getInstance().getTopHandler()
                                            .sendMessageDelayed(msg, 3000);
                                } else if (getOnPVRDialogListener() != null) {
                                    getOnPVRDialogListener().onDVRDialogListener(keyCode);
                                } else {
                                    mHandler.sendEmptyMessageDelayed(TYPE_CHANNEL_NUMBER, 3000);
                                }
                            }
                        });

                        buttonNo.setVisibility(View.VISIBLE);
                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case KeyMap.KEYCODE_MTKIR_PIPPOP:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and enter PIP/POP?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and enter PIP/POP?";
                        } else if (modeValue == TYPE_DVR) {
							message = "Do you want to stop the dvr playback," +
                                    "\n and enter PIP/POP?";
						}
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                /*
                                 * DvrManager.getInstance().stopAllRunning(); DvrManager
                                 * .getInstance() .getTopHandler() .removeMessages(
                                 * DvrConstant.RECORD_FINISH_AND_SAVE);
                                 */
                                stopTimeShift();
                                if (null != StateDvr.getInstance()
                                        && StateDvr.getInstance().isRunning()) {
                                    DvrManager.getInstance().stopDvr();
                                }
                                if (null != StateDvrPlayback.getInstance()
                                		&& StateDvrPlayback.getInstance().isRunning()) {
                                	//stop dvr playback and enter pip/pop
									StateDvrPlayback.getInstance().stopDvrFilePlay();
								}
                                mHandler.sendEmptyMessageDelayed(TYPE_PIPPOP, 1000);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case DvrDialog.Menu_pip:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and enter PIP/POP?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and enter PIP/POP?";
                        } else if (modeValue == TYPE_DVR) {
                        	message = "Do you want to enter PIP/POP?";
						}
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                /*
                                 * DvrManager.getInstance().stopAllRunning(); DvrManager
                                 * .getInstance() .getTopHandler() .removeMessages(
                                 * DvrConstant.RECORD_FINISH_AND_SAVE);
                                 */
                                if (TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager() != null) {
                                    TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager()
                                            .stop();
                                    SaveValue.getInstance(mContext).setLocalMemoryValue(
                                            MenuConfigManager.TIMESHIFT_START, false);
                                    //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
                                    TifTimeshiftView tifTimeshiftView = (TifTimeshiftView) ComponentsManager
                                            .getInstance().getComponentById(
                                                    NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
                                    tifTimeshiftView.setVisibility(View.GONE);
                                }
                                if (null != StateDvr.getInstance()
                                        && StateDvr.getInstance().isRunning()) {
                                    DvrManager.getInstance().stopDvr();
                                }
                                // mHandler.sendEmptyMessageDelayed(TYPE_PIPPOP, 0);
                                ComponentStatusListener.getInstance().updateStatus(
                                        ComponentStatusListener.NAV_ENTER_ANDR_PIP, 0);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case TYPE_SCHEDULE:
                        if (scheduleItem != null) {
                            message = "There is a scheduler record( CH: " + scheduleItem.getEventTitle()
                                    + ")," +
                                    " do you want to  start record?";
                        }
                        titleView.setVisibility(View.INVISIBLE);
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                if (null != StateDvr.getInstance()
                                        && StateDvr.getInstance().isRunning()) {
                                    DvrManager.getInstance().stopDvr();
                                } else {
                                    DvrManager.getInstance().startSchedulePvr();
                                }
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().clearSchedulePvr();
                            }
                        });
                        break;
                    case TYPE_TSHIFT:
                        if (scheduleItem != null) {
                            message = "There is a scheduler record( CH: " + scheduleItem.getEventTitle()
                                    + "), do you want to stop timeshift ?";
                        }
                        titleView.setVisibility(View.INVISIBLE);
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                mHandler.sendEmptyMessageDelayed(TYPE_TSHIFT, 0);

                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().clearSchedulePvr();
                            }
                        });
                        break;
                    case TYPE_SCART:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);

                                mHandler.sendEmptyMessageDelayed(TYPE_SCART, 2500);

                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;

                    case TYPE_Change_Source:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                if (TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager() != null) {
                                    TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager().stop();
                                    SaveValue.getInstance(mContext).setLocalMemoryValue(
                                            MenuConfigManager.TIMESHIFT_START, false);
                                    //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
                                    SystemClock.sleep(1000);
                                }
                                mHandler.sendEmptyMessageDelayed(123, 2500);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case TYPE_Change_Source_By_URL:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(TYPE_Change_Source_By_URL, 2500);

                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case TYPE_CHANNEL_Desramble:
                        if (modeValue == TYPE_Record) {
                            message = "Desramble channel could interrupt," +
                                    "are you sure to continue?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(123, 2500);

                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case TYPE_CHANNEL_Desramble_SRC_KEY:
                        if (modeValue == TYPE_Record) {
                            message = "Desramble channel could interrupt," +
                                    "are you sure to continue?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(TYPE_Change_Source_By_Src_Key,
                                        2500);

                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;

                    case TYPE_Change_Source_By_Src_Key:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(TYPE_Change_Source_By_Src_Key,
                                        2500);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case DvrDialog.KEYCODE_FROM_FAV:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change channel?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change channel?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(Msg_ID_Change_Channel, 1000);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
                        if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift," +
                                    "\n and enter record list?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(TIMESHIFT_RECORDLIST, 2000);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    default:
                        break;
                }

                break;
            case TYPE_Normal:

                break;
            case TYPE_Confirm_From_ChannelList:
                switch (keyCode) {

                    case KeyMap.KEYCODE_MTKIR_CHUP:

                    case KeyMap.KEYCODE_MTKIR_CHDN:

                    case KeyMap.KEYCODE_MTKIR_PRECH:
                    case KeyMap.KEYCODE_DPAD_CENTER:
                    case DvrDialog.TYPE_Change_ChannelNum:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change channel?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change channel?";
                        }
                        textView.setText(message);
                        buttonNo.setVisibility(View.VISIBLE);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                stopTimeShift();
                                //DvrManager.getInstance().getTopHandler().removeMessages(DvrConstant.RECORD_FINISH_AND_SAVE);
                                if (keyCode == KeyMap.KEYCODE_MTKIR_CHUP) {
                                    mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_UP, 1000);
                                } else if (keyCode == KeyMap.KEYCODE_MTKIR_CHDN) {
                                    mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_Down, 1000);
                                } else if (keyCode == KeyMap.KEYCODE_MTKIR_PRECH) {
                                    mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_Pre, 3000);
                                } else if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
                                    mHandler.sendEmptyMessageDelayed(TYPE_CHANNEL_CENTER, 3000);
                                }

                                /*
                                 * if(getOnPVRDialogListener()!=null){
                                 * getOnPVRDialogListener().onPVRDialogListener(keyCode); }
                                 */

                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                  case KeyMap.KEYCODE_MTKIR_PIPPOP:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and enter PIP/POP?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and enter PIP/POP?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(TYPE_PIPPOP, 1500);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    case TYPE_Change_Source:
                        if (modeValue == TYPE_Record) {
                            message = "Do you want to stop the record process," +
                                    "\n and change source?";
                        } else if (modeValue == TYPE_Timeshift) {
                            message = "Do you want to stop the timeshift,\n and change source?";
                        }
                        textView.setText(message);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                                DvrManager.getInstance().stopAllRunning();
                                DvrManager
                                        .getInstance()
                                        .getTopHandler()
                                        .removeMessages(
                                                DvrConstant.RECORD_FINISH_AND_SAVE);
                                mHandler.sendEmptyMessageDelayed(123, 100);
                            }
                        });

                        buttonNo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                DvrDialog.this.dismiss();
                            }
                        });
                        break;
                    default:
                        break;
                }

                break;
            default:
                break;
        }

        buttonYes.setFocusable(true);
        buttonYes.requestFocus();
        titleView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private void stopTimeShift() {
        if(TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager() != null){
            TurnkeyUiMainActivity.getInstance().getmTifTimeShiftManager().stopAll();
            SaveValue.getInstance(mContext).setLocalMemoryValue(
                    MenuConfigManager.TIMESHIFT_START, false);
            //SaveValue.saveWorldBooleanValue(mContext, MenuConfigManager.TIMESHIFT_START, false, false);
            TifTimeshiftView tifTimeshiftView = (TifTimeshiftView) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
            tifTimeshiftView.setVisibility(View.GONE);
        }
    }

    /**
     * Set the dialog's position relative to the (0,0)
     */
    public void setPositon(int xoff, int yoff) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = xoff;
        lp.y = yoff;
        lp.width = 700;
        lp.height = 400;
        window.setAttributes(lp);
    }

    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        super.dismiss();
        DvrManager.getInstance().setBGMState(false);
        DvrManager.getInstance().isPvrDialogShow = false;
		DvrManager.getInstance().setVisibility(View.GONE);
        try {
            mContext.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Button getButtonYes() {
        return buttonYes;
    }

    public Button getButtonNo() {
        return buttonNo;
    }

//    public void setButtonYesName(String buttonYesName) {
//        this.buttonYesName = buttonYesName;
//    }
//
//    public void setButtonNoName(String buttonNoName) {
//        this.buttonNoName = buttonNoName;
//    }

    public void setMessage(String info) {
        this.message = info;
    }

    @Override
    public void run() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"run-->");
    }

    public TextView getTextView() {
        return textView;
    }

    public TextView getWaitView() {
        return waitView;
    }

    public Loading getLoading() {
        return loading;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	super.onKeyUp(keyCode, event);
        if (keyCode == KeyMap.KEYCODE_VOLUME_UP || keyCode == KeyMap.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        keyCode = KeyMap.getKeyCode(keyCode, event);

        switch (keyCode) {
            case KeyMap.KEYCODE_BACK:
                this.dismiss();
                break;
            case KeyMap.KEYCODE_MTKIR_GUIDE:
            case KeyMap.KEYCODE_MTKIR_CHUP:
            case KeyMap.KEYCODE_MTKIR_CHDN:
            case KeyMap.KEYCODE_MTKIR_PRECH:
            case KeyMap.KEYCODE_MTKIR_PIPPOP:
                this.dismiss();
                DvrManager.getInstance().onKeyHandler(keyCode, event);
                return true;
            case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
                return true;
            case KeyMap.KEYCODE_MTKIR_FREEZE:
            case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
            case KeyMap.KEYCODE_MTKIR_REWIND:
            case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
                DvrManager.getInstance().onKeyHandler(keyCode, event);
                return true;
            case KeyMap.KEYCODE_MTKIR_STOP:
                DvrManager.getInstance().onKeyHandler(keyCode, event);
                this.dismiss();
                return true;
            case KeyMap.KEYCODE_MTKIR_SOURCE:
            case KeyMap.KEYCODE_MENU:
                this.dismiss();
                TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String mSelectedChannelNumString = "0";
    private int mSelectedChannelID = -1;

    private TVChannel mFavChannel = new TVChannel();

    public void setChangeChannelNum(String value) {
        mSelectedChannelNumString = value;
    }

    public void setMtkTvChannelInfoBase(int channelID) {
        mSelectedChannelID = channelID;
    }

    public void setFavChannel(TVChannel selectChannel) {
        // TODO Auto-generated method stub
        mFavChannel = selectChannel;
    }

    public TVChannel getFavChannel() {
        // TODO Auto-generated method stub
        return mFavChannel;
    }

    public OnDVRDialogListener getOnPVRDialogListener() {
        return onPVRDialogListener;
    }

    public void setOnPVRDialogListener(OnDVRDialogListener onPVRDialogListener) {
        this.onPVRDialogListener = onPVRDialogListener;
    }

    class DialogDismissRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DvrDialog.this.dismiss();
        }

    }

    @Override
    public void show() {
        if (TurnkeyUiMainActivity.getInstance() != null) {
            TurnkeyUiMainActivity.getInstance().resumeTurnkeyActivity(mContext);
        }
        ZoomTipView mZoomTip = ((ZoomTipView) ComponentsManager.getInstance()
                .getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
        if (mZoomTip != null) {
            mZoomTip.setVisibility(View.GONE);
        }
        SundryShowTextView stxtView = (SundryShowTextView) ComponentsManager.getInstance()
                .getComponentById(NavBasic.NAV_COMP_ID_SUNDRY);
        if (stxtView != null) {
            stxtView.setVisibility(View.GONE);
        }
		if (StateDvrFileList.getInstance() != null && StateDvrFileList.getInstance().isShowing()) {
            StateDvrFileList.getInstance().dissmiss();
        }
        if (StateDvr.getInstance() != null && StateDvr.getInstance().isRunning()) {
            StateDvr.getInstance().getHandler()
                    .sendEmptyMessage(DvrConstant.Dissmiss_PVR_BigCtrlbar);
        }
        DvrManager.getInstance().setPvrDialogShow(true);
        super.show();
    }

    public MtkTvBookingBase getScheduleItem() {
        return scheduleItem;
    }

    public void setScheduleItem(MtkTvBookingBase scheduleItem) {
        this.scheduleItem = scheduleItem;
    }

    class PvrReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "pvrdialog==DISSMISS_DIALOG==");
            if (intent.getAction().equals(DISSMISS_DIALOG)) {
                dismiss();
            }
        }
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

}
