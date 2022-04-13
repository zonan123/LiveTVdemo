package com.mediatek.wwtv.tvcenter.dvr.manager;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.media.PlaybackParams;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvRecordingClient;
import android.media.tv.TvRecordingClient.RecordingCallback;

import com.mediatek.twoworlds.tv.MtkTvPvrBrowserBase;
import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.model.MtkTvPvrBrowserItemBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.dvr.controller.DVRFiles;
import com.mediatek.wwtv.tvcenter.dvr.db.AlarmColumn;
import com.mediatek.wwtv.tvcenter.dvr.db.DBHelper;
import com.mediatek.wwtv.tvcenter.dvr.db.RecordedProgramInfo;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr.DvrCallback;

import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.SomeArgs;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.util.LanguageUtil;

import android.media.tv.TvContract;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import android.view.Display;
import android.hardware.display.DisplayManager;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.view.KeyEvent;

import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;


/**
 * manager dvr's record and playback.
 */
public class Controller {
    private static final String TAG = "Controller[dvr]";

    private final DvrManager mDvrManager;
    private final Context mContext;
    private List<Handler> mHandlers = null;
    private final TvRecordingClient mClient;

    public final static int UNKOWN = 0;
    public final static int ATV = 1;
    public final static int DTV = 2;
    public final static int COMPOSITE = 3;
    public final static int S_VIDEO = 4;
    public final static int SCART = 5;
    public final static int MMP = 6;
    public final static int COMPONENT = 7;
    public final static int VGA = 8;
    public final static int HDMI = 9;
    private LanguageUtil languageUtil;
    boolean flag = true;

    public Controller(Context context, DvrManager manager) {
        this.mDvrManager = manager;
        this.mContext = context;

        mClient = new TvRecordingClient(context, TAG, mRecordingCallback, null);
        languageUtil  = new LanguageUtil(mContext);
        mHandlers = new ArrayList<Handler>();
    }

    public List<DVRFiles> getPvrFiles() {
        List<DVRFiles> list = new ArrayList<DVRFiles>();
        List<RecordedProgramInfo> recordList = DBHelper.getRecordedList(mContext);
        if (recordList == null) {
            return null;
        }
        RecordedProgramInfo recordedProgramInfo = new RecordedProgramInfo();
         MtkTvPvrBrowserBase m = new MtkTvPvrBrowserBase();
        int count = m.getPvrBrowserRecordingFileCount()>0?0:-1;
        String mName = m.getPvrBrowserRecordingFileName(count);
        com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG,"name-->"+ m.getPvrBrowserRecordingFileName(count));
        for (RecordedProgramInfo i:recordList) {
            DVRFiles file = new DVRFiles();
            recordedProgramInfo = i;//recordList.get(i);
            file.setmId(recordedProgramInfo.mId);
            file.setProgarmUri(TvContract.buildRecordedProgramUri(recordedProgramInfo.mId));
            String uri = (recordedProgramInfo.mRecordingDataUri).replace( ContentResolver.SCHEME_FILE+"://","");
            file.setRecording(count == 0 && mName.equals(uri) && recordedProgramInfo.mRecordingDurationMills == 0);
            String chnumb = "";
            int channelId = 0;
            if(recordedProgramInfo.mChannelId == 0) {
                MtkTvPvrBrowserItemBase mm = m.getPvrBrowserItemByPath(uri);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ch name=" + mm.mChannelName + ",,ch id==" + mm.mChannelId + ",uri==" + uri
                        + ",ch n=" + mm.mMajorChannelNum + ",ch m=" + mm.mMinorChannelNum);
                chnumb = mm.mMinorChannelNum==0?""+mm.mMajorChannelNum:""+mm.mMinorChannelNum+"-"+
                        mm.mMajorChannelNum;
                channelId = (int) mm.mChannelId;
            }else {
                channelId = (int) recordedProgramInfo.mChannelId;
            }
            TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                    .queryChannelById(channelId);
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG,"channelId == "+channelId);
           if(tifChannelInfo != null){
                file.setChannelName(recordedProgramInfo.mTitle);
                file.setChannelNum(tifChannelInfo.mDisplayNumber);
            } else {
                file.setChannelName(recordedProgramInfo.mTitle);
                file.setChannelNum(chnumb);
//                if(recordedProgramInfo.mInternalData != null){
//                    file.setChannelNum(recordedProgramInfo.mInternalData.toString());
                }
//            }
            if (recordedProgramInfo.mStartTimeUtcMills > 0) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mStartTimeUtcMills$ = " + recordedProgramInfo.mStartTimeUtcMills);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mStartTimeUtcMills% = " + recordedProgramInfo.mStartTimeUtcMills);
                Date date = new Date(recordedProgramInfo.mStartTimeUtcMills * 1000);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "data = " + Util.dateToStringYMD(date));
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "time = " + Util.dateToString(date));
                file.setDate(Util.dateToStringYMD(date));
                file.setTime(formatCurrentTimeWith24Hours(recordedProgramInfo.mStartTimeUtcMills));//Util.dateToString(date));
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                calendar.setTimeInMillis(recordedProgramInfo.mStartTimeUtcMills * 1000);
                String week = Util.getWeek(mContext, calendar.get(Calendar.DAY_OF_WEEK));
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "week = " + week);
                file.setWeek(week);
            }
            file.setProgramName(recordedProgramInfo.mRecordingDataUri);
            file.setDuration(recordedProgramInfo.mRecordingDurationMills);
            //file.setRecording(!recordedProgramInfo.mSearchable);
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "recordedProgramInfo.mShortDescription = " + recordedProgramInfo.mShortDescription);
            file.setmDetailInfo(recordedProgramInfo.mShortDescription);
            file.dumpValues();
            list.add(file);
        }

        return list;
    }

    public int deletePvrFiles(Context content, long id) {

        return DBHelper.deleteRecordById(content, id);
    }

    public String getSrcType() {
        int type = 0;
        type = MtkTvRecord.getInstance().getSrcType();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "srcType = " + type);
        String src = "";
        switch (type) {
            case UNKOWN:
                src = "UNKOWN";
                break;
            case ATV:
                src = "ATV";
                break;
            case DTV:
                src = "TV";
                break;
            case COMPOSITE:
                src = "Composite";
                break;
            case S_VIDEO:
                src = "SVIDEO";
                break;
            case SCART:
                src = "SCART";
                break;
            case MMP:
                src = "MMP";
                break;
            case COMPONENT:
                src = "Component";
                break;
            case VGA:
                src = "VGA";
                break;
            case HDMI:
                src = "HDMI";
                break;
            default:
                break;
        }
        return src;
    }

    /**
     * get tv view.
     *
     */
    private TvSurfaceView getTvView() {
        return TurnkeyUiMainActivity.getInstance().getTvView();
    }

    /**
     * @param keyCode
     * @param arg1
     */
    public void changeChannelByID(int keyCode, int arg1) {
        NavBasicDialog channelListDialog = (NavBasicDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasicMisc.NAV_COMP_ID_CH_LIST);
       MtkTvChannelInfoBase chInfo = CommonIntegration.getInstance().getChannelById(arg1);
        if ( channelListDialog instanceof ChannelListDialog) {
            ChannelListDialog dialog = (ChannelListDialog) channelListDialog;
            dialog.selectChannel(keyCode, chInfo);
        } else if (channelListDialog instanceof UKChannelListDialog) {
            UKChannelListDialog dialog = (UKChannelListDialog) channelListDialog;
            dialog.selectChannel(keyCode, chInfo);
        }
    }

	public void getRecordDuration() {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRecordDuration");
	  mClient.sendAppPrivateCommand("session_event_dvr_record_duration", null);
	}
	public void setBGM() {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setBGM");
		  mClient.sendAppPrivateCommand("session_event_dvr_record_in_bgm", null);
		}
	/**
	 * get play position
	 */
	public long getDvrPlayDuration (){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "time-->"+getTvView().timeshiftGetCurrentPositionMs());

		return getTvView().timeshiftGetCurrentPositionMs();
	}

    /**
     * add callback handler.
     */
    public void addEventHandler(Handler handler) {
        mHandlers.add(handler);
    }

    /**
     * remove callback handler.
     *
     */
    public void removeEventHandler(Handler handler) {
        mHandlers.remove(handler);
    }

    /**
     * this method is used to send message to handlers
     * @param msg
     * @return
     */
    private int sendMessage(SomeArgs args){
        for(Handler handler : mHandlers){
            Message temp = Message.obtain();
            temp.what = args.argi1;
            temp.obj = args;

            handler.sendMessage(temp);
        }

        return 0;
    }

    /**
     * Tunes to a given channel for TV program recording. The first tune request will create a new
     * recording session for the corresponding TV input and establish a connection between the
     * application and the session. If recording has already started in the current recording
     * session, this method throws an exception. This can be used to provide domain-specific
     * features that are only known between certain client and their TV inputs.
     *
     * <p>The application may call this method before starting or after stopping recording, but not
     * during recording.
     *
     * <p>The recording session will respond by calling
     * {@link RecordingCallback#onTuned(Uri)} if the tune request was fulfilled, or
     * {@link RecordingCallback#onError(int)} otherwise.
     *
     * @param inputId The ID of the TV input for the given channel.
     * @param channelUri The URI of a channel.
     * @param params Domain-specific data for this tune request. Keys <em>must</em> be a scoped
     *            name, i.e. prefixed with a package name you own, so that different developers will
     *            not create conflicting keys.
     * @throws IllegalStateException If recording is already started.
     */
    public void tune(String inputId, Uri channelUri, Bundle params) {
        if(!mDvrManager.isSuppport()) {
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune," + inputId + "," + channelUri);
        try{
        mClient.tune(inputId, channelUri, params);
        }catch(Exception e){
        	e.printStackTrace();
        }
    }

    /**
     * Tunes to a given channel for TV program recording. The first tune request will create a new
     * recording session for the corresponding TV input and establish a connection between the
     * application and the session. If recording has already started in the current recording
     * session, this method throws an exception. This can be used to provide domain-specific
     * features that are only known between certain client and their TV inputs.
     *
     * <p>The application may call this method before starting or after stopping recording, but not
     * during recording.
     *
     * <p>The recording session will respond by calling
     * {@link RecordingCallback#onTuned(Uri)} if the tune request was fulfilled, or
     * {@link RecordingCallback#onError(int)} otherwise.
     *
     * @param channelUri The URI of a channel.
     * @param params Domain-specific data for this tune request. Keys <em>must</em> be a scoped
     *            name, i.e. prefixed with a package name you own, so that different developers will
     *            not create conflicting keys.
     * @throws IllegalStateException If recording is already started.
     */
    public void tune(Uri channelUri, Bundle params) {
    	if(SaveValue.getInstance(mContext).readValue(CommonIntegration.camUpgrade) == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"cam upgrade");
              return;
          }
        String inputId = null;

        try {
            TIFChannelInfo info = TIFChannelManager.getInstance(mContext)
                .getTIFChannelInfoByProviderId(ContentUris.parseId(channelUri));
            if (info != null) {
                inputId = info.mInputServiceName;
            }
        } catch (Exception ex) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception: " + ex);
            return ;
        }

        tune(inputId, channelUri, params);
    }

    /**
     * Starts TV program recording in the current recording session. Recording is expected to start
     * immediately when this method is called. If the current recording session has not yet tuned to
     * any channel, this method throws an exception.
     * <p>
     * The application may supply the URI for a TV program for filling in program specific data
     * fields in the {@link android.media.tv.TvContract.RecordedPrograms} table. A non-null
     * {@code programUri} implies the started recording should be of that specific program, whereas
     * null {@code programUri} does not impose such a requirement and the recording can span across
     * multiple TV programs. In either case, the application must call
     * {@link TvRecordingClient#stopRecording()} to stop the recording.
     * <p>
     * The recording session will respond by calling {@link RecordingCallback#onError(int)} if the
     * start request cannot be fulfilled.
     *
     * @param programUri The URI for the TV program to record, built by
     *            {@link TvContract#buildProgramUri(long)}. Can be {@code null}.
     * @throws IllegalStateException If {@link #tune} request hasn't been handled yet.
     */
    public void startRecording(Uri programUri) {
        if (!mDvrManager.isSuppport()) {
            return;
        }
        try {
        	mClient.startRecording(programUri);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

    }

    /**
     * Stops TV program recording in the current recording session. Recording is expected to stop
     * immediately when this method is called. If recording has not yet started in the current
     * recording session, this method does nothing.
     *
     * <p>The recording session is expected to create a new data entry in the
     * {@link android.media.tv.TvContract.RecordedPrograms} table that describes the newly
     * recorded program and pass the URI to that entry through to
     * {@link RecordingCallback#onRecordingStopped(Uri)}.
     * If the stop request cannot be fulfilled, the recording session will respond by calling
     * {@link RecordingCallback#onError(int)}.
     */
    public void stopRecording() {
        if(!mDvrManager.isSuppport()) {
            return ;
        }

        mClient.stopRecording();
    }

    /**
     * Releases the resources in the current recording session immediately. This may be called at
     * any time, however if the session is already released, it does nothing.
     */
    public void dvrRelease() {
        if (!mDvrManager.isSuppport()) {
            return;
        }

        mClient.release();
    }

    /**
     * Sends a private command to the underlying TV input. This can be used to provide
     * domain-specific features that are only known between certain clients and their TV inputs.
     *
     * @param action The name of the private command to send. This <em>must</em> be a scoped name,
     *            i.e. prefixed with a package name you own, so that different developers will not
     *            create conflicting commands.
     * @param data An optional bundle to send with the command.
     */
    public void sendRecordCommand(@NonNull String action, Bundle data) {
        mClient.sendAppPrivateCommand(action, data);
    }

    /**
     * Plays a given recorded TV program.
     *
     * @param recordedProgramUri The URI of a recorded program.
     */
    public void dvrPlay(Uri recordedProgramUri, boolean isNeedLastMemory) {
        String inputId = "com.mediatek.tvinput/.tuner.TunerInputService/HW0";
        getTvView().reset();
        try {
            RecordedProgramInfo recordinfo = DBHelper.getRecordedInfoById(mContext,
                ContentUris.parseId(recordedProgramUri));
            if (recordinfo != null) {
                TIFChannelInfo info = TIFChannelManager.getInstance(mContext)
                    .getTIFChannelInfoByProviderId(recordinfo.mChannelId);
                if (info != null) {
                    inputId = info.mInputServiceName;
                }
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "inputId: " + inputId);
        } catch (Exception ex) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception: " + ex);
//            if there is no input ID ,use default !
//            return ;
        }

        dvrPlay(inputId, recordedProgramUri, isNeedLastMemory);
    }

    /**
     * Plays a given recorded TV program.
     *
     * @param inputId The ID of the TV input that created the given recorded program.
     * @param recordedProgramUri The URI of a recorded program.
     */
    public void dvrPlay(String inputId, Uri recordedProgramUri, boolean isNeedLastMemory) {
        if(!mDvrManager.isSuppport()) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mDvrManager.isSuppport() = false !!");
            return ;
        }

        if (getTvView() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrPlay, TvView null");
            return ;
        }

        //getTvView().reset();

//        MtkTvHighLevel mHighLevel = new MtkTvHighLevel();
//        mHighLevel.stopTV();
        //reset mmp mode
        MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
        MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrPlay");
        //don't need to send privatecmd,TIS read acfg for handle it
        /*if (MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.TYPE) == 2) { //Visually Impaired
            String adAction = MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SETADINFO;
            Bundle adBundle = new Bundle();
            adBundle.putInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_ADINFO_VALUE, 1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "set adAction info.");
            getTvView().sendAppPrivateCommand(adAction, adBundle);
        }

        if (TVContent.getInstance(mContext).isFinCountry()) { // Finland country
            String adCountryAction = MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SETADCOUNTRYINFO;
            Bundle adCountryBundle = new Bundle();
            adCountryBundle.putInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_ADINFO_VALUE, 1);
            adCountryBundle.putInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_COUNTRYINFO_VALUE, 1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "set adCountryAction info.");
            getTvView().sendAppPrivateCommand(adCountryAction, adCountryBundle);
            }
            String  audioinfoString = MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_SETAUDIOPREINFO;
            Bundle allaudioBundle = new Bundle();
            String  firstLanguageString = MtkTvConfig.getInstance().getLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE);
            String secondLanguageString = MtkTvConfig.getInstance().getLanguage(MenuConfigManager.TV_AUDIO_LANGUAGE_2);
            allaudioBundle.putString(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_AUDIOPREINFO_FIRST_LANGUAGE, firstLanguageString);
            allaudioBundle.putString(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_DVR_PLAYBACK_AUDIOPREINFO_SECOND_LANGUAGE, secondLanguageString);

            getTvView().sendAppPrivateCommand(audioinfoString,allaudioBundle);*/



        getTvView().timeShiftPlay(inputId, recordedProgramUri);
    }

    /**
     * Pauses playback.
     */
    public void dvrPause() {
        if (getTvView() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrPause, TvView null");
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrPause");

        getTvView().timeShiftPause();
    }
    /**
     * stop dvr play
     */
    public void dvrStop(){
        getTvView().reset();
    }

    /**
     * Resumes playback.
     */
    public void dvrResume() {
        if (getTvView() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrResume, TvView null");
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrResume");

        getTvView().timeShiftResume();
    }

    /**
     * Seeks to a specified time position. {@code timeMs} must be equal to or greater than the start
     * position returned by {@link TimeShiftPositionCallback#onTimeShiftStartPositionChanged} and
     * equal to or less than the current time.
     *
     * @param timeMs The time position to seek to, in milliseconds since the epoch.
     */
    public void dvrSeekTo(long timeMs) {
        if (getTvView() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrSeekTo, TvView null");
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrSeekTo");
        try {
            getTvView().timeShiftSeekTo(timeMs);
        } catch (Exception e) {
            // TODO: handle exception
            return;
        }
    }

    /**
     * Sets playback rate using {@link android.media.PlaybackParams}.
     *
     * @param params The playback params.
     */
    public void dvrPlaybackParams(@NonNull PlaybackParams params) {
        if (getTvView() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrPlaybackParams, TvView null");
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dvrPlaybackParams");

        getTvView().timeShiftSetPlaybackParams(params);
    }

    /**
     * Sends a private command to the underlying TV input. This can be used to provide
     * domain-specific features that are only known between certain clients and their TV inputs.
     *
     * @param action The name of the private command to send. This <em>must</em> be a scoped name,
     *            i.e. prefixed with a package name you own, so that different developers will not
     *            create conflicting commands.
     * @param data An optional bundle to send with the command.
     */
    public void sendDvrPlaybackCommand(@NonNull String action, Bundle data) {
        if (getTvView() == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendDvrPlaybackCommand, TvView null");
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendDvrPlaybackCommand," + action);

        getTvView().sendAppPrivateCommand(action, data);
    }

    /**
     * handle record notify
     *
     * @param data
     */
    public void handleRecordNotify(TvCallbackData data) {
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = DvrConstant.MSG_CALLBACK_TVAPI_NOTIFY;
        args.argi2 = data.param1;
        args.argi3 = data.param2;
        args.argi4 = data.param3;

        sendMessage(args);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleRecordNotify," +
            data.param1 + "," + data.param2 + "," + data.param3);
    }

    public void handleRecordNotify(Intent intent) {
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = DvrConstant.MSG_CALLBACK_ALARM_GO_OFF;
        args.arg1 = intent;

        sendMessage(args);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleRecordNotify, " +
            intent.getLongExtra(AlarmColumn.STARTTIME, 0L) + "," +
            intent.getLongExtra(AlarmColumn.ENDTIME, 0L) + "," +
            intent.getStringExtra(AlarmColumn.CHANNELID));
    }

    RecordingCallback mRecordingCallback = new RecordingCallback(){
        /**
         * This is called when an error occurred while establishing a connection to the recording
         * session for the corresponding TV input.
         *
         * @param inputId The ID of the TV input bound to the current TvRecordingClient.
         */
        public void onConnectionFailed(String inputId) {
            if(!mDvrManager.isSuppport()) {
                return ;
            }

            SomeArgs args = SomeArgs.obtain();
            args.argi1 = DvrConstant.MSG_CALLBACK_CONNECT_FAIL;
            args.arg1 = inputId;

            sendMessage(args);

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onConnectionFailed," + inputId);
        }

        /**
         * This is called when the connection to the current recording session is lost.
         *
         * @param inputId The ID of the TV input bound to the current TvRecordingClient.
         */
        public void onDisconnected(String inputId) {
            if(!mDvrManager.isSuppport()) {
                return ;
            }

            SomeArgs args = SomeArgs.obtain();
            args.argi1 = DvrConstant.MSG_CALLBACK_DISCONNECT;
            args.arg1 = inputId;

            sendMessage(args);

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDisconnected," + inputId);
        }

        /**
         * This is called when the recording session has been tuned to the given channel and is
         * ready to start recording.
         *
         * @param channelUri The URI of a channel.
         */
        public void onTuned(Uri channelUri) {
            if(!mDvrManager.isSuppport()) {
                return ;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTuned," + channelUri);

            SomeArgs args = SomeArgs.obtain();
            args.argi1 = DvrConstant.MSG_CALLBACK_TUNED;
            args.arg1 = channelUri;

            sendMessage(args);
        }

        /**
         * This is called when the current recording session has stopped recording and created a
         * new data entry in the {@link TvContract.RecordedPrograms} table that describes the newly
         * recorded program.
         *
         * @param recordedProgramUri The URI for the newly recorded program.
         */
        public void onRecordingStopped(Uri recordedProgramUri) {
            if(!mDvrManager.isSuppport()) {
                return ;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRecordingStopped," + recordedProgramUri);

            SomeArgs args = SomeArgs.obtain();
            args.argi1 = DvrConstant.MSG_CALLBACK_RECORD_STOPPED;
            args.arg1 = recordedProgramUri;

            sendMessage(args);
        }

        /**
         * This is called when an issue has occurred. It may be called at any time after the current
         * recording session is created until it is released.
         *
         * @param error The error code. Should be one of the followings.
         * <ul>
         * <li>{@link TvInputManager#RECORDING_ERROR_UNKNOWN}
         * <li>{@link TvInputManager#RECORDING_ERROR_INSUFFICIENT_SPACE}
         * <li>{@link TvInputManager#RECORDING_ERROR_RESOURCE_BUSY}
         * </ul>
         */
        public void onError(int error) {
            if(!mDvrManager.isSuppport()) {
                return ;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onError," + error);

            SomeArgs args = SomeArgs.obtain();
            args.argi1 = DvrConstant.MSG_CALLBACK_ERROR;
            args.argi2 = error;

            sendMessage(args);
        }

        /**
         * This is invoked when a custom event from the bound TV input is sent to this client.
         *
         * @param inputId The ID of the TV input bound to this client.
         * @param eventType The type of the event.
         * @param eventArgs Optional arguments of the event.
         * @hide
         */
        public void onEvent(String inputId, String eventType, Bundle eventArgs) {
            if(!mDvrManager.isSuppport()) {
                return ;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent," + inputId + "," + eventType+","+eventArgs.containsKey("errId"));

            SomeArgs args = SomeArgs.obtain();
            if(eventType.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_RECORDING_REMAIN_DURATION_UPDATE)){
                Bundle bundle = new Bundle();
                bundle = eventArgs;
                args.argi1= DvrConstant.MSG_CALLBACK_UPDATE_DURATION;
                args.argi2 = bundle.getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_PVR_MAX_DURATION_VALUE_KEY);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent,"+bundle.toString()+"==="+args.argi2);
                }
            else if(eventType.equals(MtkTvTISMsgBase.MSG_START_RECORDING_ERROR_EVENT)){
            	Bundle bundle = new Bundle();
            	bundle = eventArgs;
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent,"+bundle.toString()+"==="+bundle.get("errId"));
            	args.argi1 = DvrConstant.MSG_CALLBACK_ERROR;
            	args.argi2 = bundle.getInt("errId");
            }else if(eventType.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_RECORDING_TUNED_STATUS)){
                Bundle bundle = new Bundle();
                bundle = eventArgs;
                args.argi1 = DvrConstant.MSG_CALLBACK_ERROR;
                args.argi2=bundle.getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_RECORDING_TUNED_KEY);
            }else if(eventType.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_PVR_STOPED)){
                Bundle bundle = new Bundle();
                bundle = eventArgs;
                args.argi1 = DvrConstant.MSG_CALLBACK_STOP_REASON;
                args.argi2=bundle.getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_PVR_STOPED_REASON_VALUE_KEY);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent,"+bundle.toString()+"==="+args.argi2);
            }else{
            	args.argi1 = DvrConstant.MSG_CALLBACK_RECORD_EVENT;
            	args.arg2 = eventType;
            }
            args.arg1 = inputId;
            args.arg3 = eventArgs;
            sendMessage(args);
        }
    };



 private final DvrCallback mDvrCallback = new DvrCallback() {

        @Override
		public void onTrackSelected(String inputId, int type, String trackId) {
			// TODO Auto-generated method stub
            if (!mDvrManager.isSuppport()) {
                return;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "onEvent type , "+ type + ",id=="+ trackId);

            SomeArgs args = SomeArgs.obtain();

            args.argi1 =DvrConstant.MSG_CALLBACK_TRACK_SELECTED ;
            args.arg1=type;
            args.arg2=trackId;

            sendMessage(args);

		}

    @Override
      public void onTimeShiftStatusChanged(String inputId, int status) {
          if(!mDvrManager.isSuppport()) {
              return ;
          }

          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTimeShiftStatusChanged," + inputId + "," + status);

          SomeArgs args = SomeArgs.obtain();
          args.argi1 = DvrConstant.MSG_CALLBACK_TIMESHIFT_STATE;
          args.arg1 = inputId;
          args.argi2 = status;

          sendMessage(args);
      }

      @Override
      public void onEvent(String inputId, String eventType, Bundle eventArgs) {
          if(!mDvrManager.isSuppport()) {
              return ;
          }

          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent[Timeshift]," + inputId + "," + eventType);

          SomeArgs args = SomeArgs.obtain();
          args.argi1 = DvrConstant.MSG_CALLBACK_TIMESHIFT_EVENT;
          args.arg1 = inputId;
          args.arg2 = eventType;
          args.arg3 = eventArgs;

          sendMessage(args);
      }

		@Override
		public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
			if (!mDvrManager.isSuppport()) {
				return ;
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "onEvent [DVR] , "+ inputId + ","+ tracks.size());

			SomeArgs args = SomeArgs.obtain();

			args.argi1 =DvrConstant.MSG_CALLBACK_TRACK_CHANGED ;
			args.arg1=inputId;
			args.arg2=tracks ;

			sendMessage(args);
		}
	};

    public void onAppPrivateCommand(String action, Bundle data) {
    	getTvView().sendAppPrivateCommand(action, data);
    }

	public void onTvShow() {

		SomeArgs args = SomeArgs.obtain();
		args.argi1 = DvrConstant.tv_show;
		sendMessage(args);
	}


    public int setSelectSubtitle(List<TvTrackInfo> subTitleTrackInfos){

        int firstSub = languageUtil.getSubtitleLanguage(MenuConfigManager.DIGITAL_SUBTITLE_LANG);
        int secodSub = languageUtil.getSubtitleLanguage(MenuConfigManager.DIGITAL_SUBTITLE_LANG_2ND);
        String[] subtitleEntry= mContext.getResources()
                .getStringArray( R.array.menu_tv_subtitle_language_eu_array_value);
        String firstSubString = languageUtil.getSubitleNameByValue(subtitleEntry[firstSub]);
        String secondSubString = languageUtil.getSubitleNameByValue(subtitleEntry[secodSub]);
        boolean isnordic = TVContent.getInstance(mContext).isNordicCountry();
        int subtype = MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.SUBTITLE_TYPE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"fisrtsublang="+firstSubString+",secondsublan="+secondSubString+",isnordic="+isnordic+",subtype="+subtype);
        int index=0;
        if(subtype==1){
            for(int i=0;i<subTitleTrackInfos.size();i++){
                if(subTitleTrackInfos.get(i).getExtra()!=null){
                if(subTitleTrackInfos.get(i).getExtra().getString("key_SubtileMime").contains("hi")&&
                    firstSubString.contains(subTitleTrackInfos.get(i).getExtra().getString("Sub_Language"))){
                   index=i;
                    return index;
                }
                }
            }

            for(int m=0;m<subTitleTrackInfos.size();m++){
                if(subTitleTrackInfos.get(m).getExtra()!=null){
                if(subTitleTrackInfos.get(m).getExtra().getString("key_SubtileMime").contains("hi")&&
                    secondSubString.contains(subTitleTrackInfos.get(m).getExtra().getString("Sub_Language"))){
                    index=m;
                    return index;
                }
                }
            }
            for(int j=0;j<subTitleTrackInfos.size();j++){
                if(subTitleTrackInfos.get(j).getExtra()!=null){
                if(subTitleTrackInfos.get(j).getExtra().getString("key_SubtileMime").contains("hi")){
                    index=j;
                    return index;
                 }
                }
              }
            for(int k=0;k<subTitleTrackInfos.size();k++){
                if(subTitleTrackInfos.get(k).getExtra()!=null){
                if(firstSubString.contains(subTitleTrackInfos.get(k).getExtra().getString("Sub_Language"))){
                    index=k;
                    return index;
                 }
                }
            }

               for(int l=0;l<subTitleTrackInfos.size();l++){
                   if(subTitleTrackInfos.get(l).getExtra()!=null){
                    if(secondSubString.contains(subTitleTrackInfos.get(l).getExtra().getString("Sub_Language"))){
                        index=l;
                        return index;
                    }
                   }
                }

        }
            if(subtype==0){

                for(int i=0;i<subTitleTrackInfos.size();i++){
                    if(subTitleTrackInfos.get(i).getExtra()!=null){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,subTitleTrackInfos.get(i).getExtra().getString("Sub_Language")+"<--lagu-->"+subTitleTrackInfos.get(i).getExtra().getString("key_SubtileMime"));
                    if(!subTitleTrackInfos.get(i).getExtra().getString("key_SubtileMime").contains("hi")&&
                        firstSubString.contains(subTitleTrackInfos.get(i).getExtra().getString("Sub_Language"))){
                        index=i;
                        return index;
                    }
                    }
                }

                for(int m=0;m<subTitleTrackInfos.size();m++){
                    if(subTitleTrackInfos.get(m).getExtra()!=null){
                    if(!subTitleTrackInfos.get(m).getExtra().getString("key_SubtileMime").contains("hi")&&
                        secondSubString.contains(subTitleTrackInfos.get(m).getExtra().getString("Sub_Language"))){
                        index=m;
                        return index;
                    }
                    }
                }
                if(isnordic){
                    return -1;
                }else {
                    for(int j=0;j<subTitleTrackInfos.size();j++){
                        if(subTitleTrackInfos.get(j).getExtra()!=null){
                        if(!subTitleTrackInfos.get(j).getExtra().getString("key_SubtileMime").contains("hi")){
                            index=j;
                            return index;
                        }
                        }
                    }
                }
        }
        return index;
    }

    public boolean isScreenOn(Context context) {
        DisplayManager dm = (DisplayManager) context
            .getSystemService(Context.DISPLAY_SERVICE);
            for (Display displayManager : dm.getDisplays()) {
                if (displayManager.getState() != Display.STATE_OFF) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isScreenOn||true");
                return true;
            }
        }
        return false;
    }

    public static boolean isTargetAppRunning(Context context, String... packages) {
        boolean result = false;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            String classString = cn.getClassName();
            for (String runningPKG : packages) {
                if(classString.equalsIgnoreCase(runningPKG)) {
                    result = true;
                    break;
                    }
                }
            }    catch (Exception ex) {
            result = false;
            }
            return result;
            }

    public static String formatCurrentTimeWith24Hours(long time) {
        String sbString = "";
        MtkTvTimeFormatBase timeformat = new MtkTvTimeFormatBase();
        timeformat.set(time);
        String hour = String.valueOf(timeformat.hour);
        String min = String.valueOf(timeformat.minute);
        String sec = String.valueOf(timeformat.second);
        if(timeformat.hour < 10){
            hour = "0" + hour;
        }
        if (timeformat.minute < 10) {
            min = "0" + min;
        }
        if (timeformat.second < 10) {
            sec = "0" + sec;
        }
        sbString =
                hour + ":" + min + ":" + sec;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"time->"+sbString);
        return sbString;
    }

    public int getDeletItem(){
        //MtkTvRecordAdaptorBase   mRecordAdaptor = new MtkTvRecordAdaptorBase();
        MtkTvBookingBase item = MtkTvRecord.getInstance().getBookingByIndex(-2);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"delet item 1"+item.getInfoData());
        if(item.getInfoData()==1){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"delet item ");
            return 1;
        }
        return 0;
    }


    public boolean setExitOrStopKey(KeyEvent event){
        int keyCode = KeyMap.getKeyCode(event);
        boolean isSupportBackLongPress = DataSeparaterUtil.getInstance() != null&&DataSeparaterUtil.getInstance().isLongPressBackSupport();
        boolean isSupportPlayLongPress = DataSeparaterUtil.getInstance() != null&&DataSeparaterUtil.getInstance().isLongPressPlaySupport();
        boolean isSupportPauseLongPress = DataSeparaterUtil.getInstance() != null&&DataSeparaterUtil.getInstance().isLongPressPauseSupport();

        //if(keyCode==KeyMap.KEYCODE_MTKIR_PAUSE||keyCode==KeyMap.KEYCODE_BACK){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSupportBackLongPress =" + isSupportBackLongPress+",isSupportBackLongPress="+isSupportBackLongPress+",isSupportBackLongPress="+isSupportBackLongPress);
        long duration = event.getEventTime() - event.getDownTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchTest||duration =" + duration);
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            if (duration >= 3000L&&flag) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchTest >keycode==" +keyCode);
                switch(keyCode){
                    case KeyMap.KEYCODE_MTKIR_PAUSE:
                        if(isSupportPauseLongPress){
                            InstrumentationHandler.getInstance()
                                    .sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_STOP);
                        }
                        break;
                    case KeyMap.KEYCODE_MTKIR_PLAY:
                    case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
                        if(isSupportPlayLongPress){
                            InstrumentationHandler.getInstance()
                                    .sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_STOP);
                        }
                        break;
                    case KeyMap.KEYCODE_BACK:
                        if(isSupportBackLongPress){
                            InstrumentationHandler.getInstance()
                                    .sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_EPG);
                        }
                        break;
                    default:
                        break;

                }

                flag = false;
            }
            if((isSupportPlayLongPress&&event.getRepeatCount()>=1&&KeyMap.getKeyCode(event)==KeyMap.KEYCODE_MTKIR_PLAYPAUSE)||
                    (isSupportPlayLongPress&&event.getRepeatCount()>=1&&KeyMap.getKeyCode(event)==KeyMap.KEYCODE_MTKIR_PLAY)||
                    (isSupportPlayLongPress&&event.getRepeatCount()>=1&&KeyMap.getKeyCode(event)==KeyMap.KEYCODE_MTKIR_PAUSE)
            ){
                return true;
            }

        }else {
            flag = true;
        }
        return false;
        //}else{flag = true;}

    }

    public void setmDvrCallback(){
        TvInputCallbackMgr.getInstance(mContext).setDvrCallback(mDvrCallback);
    }
    
    public void resetsetmDvrCallback(){
        TvInputCallbackMgr.getInstance(mContext).setDvrCallback(null);
    }
    
}
