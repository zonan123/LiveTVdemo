package com.mediatek.wwtv.tvcenter.dvr.manager;

import com.mediatek.wwtv.tvcenter.R;

public class DvrConstant {
    public final static int ROOT_VIEW = R.id.linear_glview;

    public final static int MSG_CALLBACK_BASE           = 0x1000;

    /**
     * MSG_CALLBACK_CONNECT_FAIL
     *
     * RecordingCallback.onConnectionFailed(String inputId)
     *
     *
     * @param args.arg1 = inputId;
     */
    public final static int MSG_CALLBACK_CONNECT_FAIL   = MSG_CALLBACK_BASE + 1;

    /**
     * MSG_CALLBACK_DISCONNECT
     *
     * RecordingCallback.onDisconnected(String inputId)
     *
     *
     * @param args.arg1 = inputId;
     */
    public final static int MSG_CALLBACK_DISCONNECT     = MSG_CALLBACK_BASE + 2;

    /**
     * MSG_CALLBACK_TUNED
     *
     * RecordingCallback.onTuned(Uri channelUri)
     *
     *
     * @param args.arg1 = channelUri;
     */
    public final static int MSG_CALLBACK_TUNED          = MSG_CALLBACK_BASE + 3;

    /**
     * MSG_CALLBACK_RECORD_STOPPED
     *
     * RecordingCallback.onRecordingStopped(Uri recordedProgramUri)
     *
     *
     * @param args.arg1 = recordedProgramUri;
     */
    public final static int MSG_CALLBACK_RECORD_STOPPED = MSG_CALLBACK_BASE + 4;

    /**
     * MSG_CALLBACK_ERROR
     *
     * RecordingCallback.onError(int error)
     *
     *
     * @param args.argi2 = error;
     */
    public final static int MSG_CALLBACK_ERROR          = MSG_CALLBACK_BASE + 5;

    /**
     * MSG_CALLBACK_RECORD_EVENT
     *
     * RecordingCallback.onEvent(String inputId, String eventType, Bundle eventArgs)
     *
     *
     * @param args.arg1 = inputId;
     * @param args.arg2 = eventType;
     * @param args.arg3 = eventArgs;
     */
    public final static int MSG_CALLBACK_RECORD_EVENT   = MSG_CALLBACK_BASE + 6;

    /**
     * MSG_CALLBACK_TIMESHIFT_STATE
     *
     * TimeshiftCallback.onTimeShiftStatusChanged(String inputId, int status)
     *
     *
     * @param args.arg1 = inputId;
     * @param args.argi2 = status;
     */
    public final static int MSG_CALLBACK_TIMESHIFT_STATE= MSG_CALLBACK_BASE + 7;

    /**
     * MSG_CALLBACK_TIMESHIFT_EVENT
     *
     * TimeshiftCallback.onEvent(String inputId, String eventType, Bundle eventArgs)
     *
     *
     * @param args.arg1 = inputId;
     * @param args.arg2 = eventType;
     * @param args.arg3 = eventArgs;
     */
    public final static int MSG_CALLBACK_TIMESHIFT_EVENT= MSG_CALLBACK_BASE + 8;

    /**
     * MSG_CALLBACK_TVAPI_NOTIFY
     *
     * MtkTvTVCallbackHandler.notifyRecordNotification(
     *     int updateType, int argv1, int argv2)
     *
     * @param args.argi2 = updateType;
     * @param args.argi3 = argv1;
     * @param args.argi4 = argv2;
     */
    public final static int MSG_CALLBACK_TVAPI_NOTIFY   = MSG_CALLBACK_BASE + 9;

    /**
     * MSG_CALLBACK_ALARM_GO_OFF
     *
     * @param args.arg1 = intent;
     */
    public final static int MSG_CALLBACK_ALARM_GO_OFF   = MSG_CALLBACK_BASE + 10;
    /** MSG_CALLBACK_TRACK_CHANGED
     * DvrCallback.onTracksChanged(String inputId, List<TvTrackInfo> tracks)
     * 
     * @param args.arg1=inputId ;
     * @param args.arg2=tracks ;
     * 
     */
    public final static int MSG_CALLBACK_TRACK_CHANGED  = MSG_CALLBACK_BASE + 11 ;
    /**
    *DVR Duration update 
    *
    */
    public final static int MSG_CALLBACK_UPDATE_DURATION = MSG_CALLBACK_BASE + 12;

    public final static int MSG_CALLBACK_TRACK_SELECTED = MSG_CALLBACK_BASE + 13;

    public final static int MSG_CALLBACK_STOP_REASON = MSG_CALLBACK_BASE + 14;

	public static final int FEATURE_NOT_SUPPORT = 1001;
    public static final int RECORD_FINISH_AND_SAVE= 1002;
    
    public static final int Disk_Disconnect = 1012;

    public static final int Dissmiss_PVR_BigCtrlbar = 10001;
    public static final int Dissmiss_Tshift_BigCtrlbar = 10003;
    
    public static final int Dissmiss_Info_Bar = 10005;
    public static final int Finish_Timeshift_Activity = 10006;
	public static final int tv_show = 10007;

    @Override
    public String toString() {
        android.util.Log.d("Dvrconst","DvrconsttoString");
        return "DvrconsttoString"; 
    }

}
