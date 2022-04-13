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

package com.mediatek.wwtv.tvcenter.tiftimeshift;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Range;
import android.net.Uri;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.mediatek.twoworlds.tv.MtkTvTimeshift;
import com.mediatek.wwtv.rxbus.MainActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.widget.view.DiskSettingDialog;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView.TimeShiftListener;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramInfo;
import com.mediatek.wwtv.tvcenter.util.WeakHandler;
//import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import com.mediatek.wwtv.tvcenter.dvr.manager.DevListener;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

/**
 * A class which manages the time shift feature in Live TV. It consists of two parts.
 * {@link PlayController} controls the playback such as play/pause, rewind and fast-forward using
 * {@link TvSurfaceView} which communicates with TvInputService through
 * {@link android.media.tv.TvInputService.Session}.
 * {@link ProgramManager} loads programs of the current channel in the background.
 */
public class TifTimeShiftManager implements DevListener{
    private static final String TAG = "TifTimeShiftManager";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLAY_STATUS_PAUSED, PLAY_STATUS_PLAYING})
    public @interface PlayStatus {}
    public static final int PLAY_STATUS_PAUSED  = 0;
    public static final int PLAY_STATUS_PLAYING = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLAY_SPEED_1X, PLAY_SPEED_2X, PLAY_SPEED_3X, PLAY_SPEED_4X, PLAY_SPEED_5X,PLAY_SPEED_6X})
    public @interface PlaySpeed{}
    public static final int PLAY_SPEED_1X = 1;
    public static final int PLAY_SPEED_2X = 2;
    public static final int PLAY_SPEED_3X = 3;
    public static final int PLAY_SPEED_4X = 4;
    public static final int PLAY_SPEED_5X = 5;
    public static final int PLAY_SPEED_6X = 6;

    private static final int SHORT_PROGRAM_THRESHOLD_MILLIS = 46 * 60 * 1000;  // 46 mins.
    private static final int[] SHORT_PROGRAM_SPEED_FACTORS = new int[] {2, 4, 8, 16,32};
    private static final int[] LONG_PROGRAM_SPEED_FACTORS = new int[] {2, 4,8,16,32};

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLAY_DIRECTION_FORWARD, PLAY_DIRECTION_BACKWARD})
    public @interface PlayDirection{}
    public static final int PLAY_DIRECTION_FORWARD  = 0;
    public static final int PLAY_DIRECTION_BACKWARD = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TIME_SHIFT_ACTION_ID_PLAY, TIME_SHIFT_ACTION_ID_PAUSE, TIME_SHIFT_ACTION_ID_REWIND,
        TIME_SHIFT_ACTION_ID_FAST_FORWARD, TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS,
        TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT})
    public @interface TimeShiftActionId{}
    public static final int TIME_SHIFT_ACTION_ID_PLAY = 1;
    public static final int TIME_SHIFT_ACTION_ID_PAUSE = 1 << 1;
    public static final int TIME_SHIFT_ACTION_ID_REWIND = 1 << 2;
    public static final int TIME_SHIFT_ACTION_ID_FAST_FORWARD = 1 << 3;
    public static final int TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS = 1 << 4;
    public static final int TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT = 1 << 5;

    private static final int MSG_GET_CURRENT_POSITION = 1000;
    private static final int MSG_PREFETCH_PROGRAM = 1001;
    private static final long REQUEST_CURRENT_POSITION_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    private static final long MAX_DUMMY_PROGRAM_DURATION = TimeUnit.MINUTES.toMillis(30);
    private static final long MIN_DUMMY_PROGRAM_DURATION = TimeUnit.MINUTES.toMillis(30);
    //private static final long INVALID_TIMESHIFT_DURATION = TimeUnit.HOURS.toMillis(24);
    @VisibleForTesting
    static final long INVALID_TIME = -1;
    private static final long PREFETCH_TIME_OFFSET_FROM_PROGRAM_END = TimeUnit.MINUTES.toMillis(1);
    private static final long PREFETCH_DURATION_FOR_NEXT = TimeUnit.HOURS.toMillis(2);

    @VisibleForTesting
    static final long REQUEST_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(3);

    /**
     * If the user presses the {@link android.view.KeyEvent#KEYCODE_MEDIA_PREVIOUS} button within
     * this threshold from the program start time, the play position moves to the start of the
     * previous program.
     * Otherwise, the play position moves to the start of the current program.
     * This value is specified in the UX document.
     */
    private static final long PROGRAM_START_TIME_THRESHOLD = TimeUnit.SECONDS.toMillis(3);
    /**
     * If the current position enters within this range from the recording start time, rewind action
     * and jump to previous action is disabled.
     * Similarly, if the current position enters within this range from the current system time,
     * fast forward action and jump to next action is disabled.
     * It must be three times longer than {@link #REQUEST_CURRENT_POSITION_INTERVAL} at least.
     */
    private static final long DISABLE_ACTION_THRESHOLD = 3 * REQUEST_CURRENT_POSITION_INTERVAL;
    /**
     * If the current position goes out of this range from the recording start time, rewind action
     * and jump to previous action is enabled.
     * Similarly, if the current position goes out of this range from the current system time,
     * fast forward action and jump to next action is enabled.
     * Enable threshold and disable threshold must be different because the current position
     * does not have the continuous value. It changes every one second.
     */
    private static final long ENABLE_ACTION_THRESHOLD =
            DISABLE_ACTION_THRESHOLD + 3 * REQUEST_CURRENT_POSITION_INTERVAL;
    /**
     * The current position sent from TIS can not be exactly the same as the current system time
     * due to the elapsed time to pass the message from TIS to Live TV.
     * So the boundary threshold is necessary.
     * The same goes for the recording start time.
     * It must be three times longer than {@link #REQUEST_CURRENT_POSITION_INTERVAL} at least.
     */
    private static final long RECORDING_BOUNDARY_THRESHOLD = 3 * REQUEST_CURRENT_POSITION_INTERVAL;

    public static final int ERROR_DATE_TIME = 1;

    private PlayController mPlayController;
    private final ProgramManager mProgramManager;
    private MtkTvTime mMtkTvTime;
    @VisibleForTesting
    final CurrentPositionMediator mCurrentPositionMediator = new CurrentPositionMediator();

    private Listener mListener;
    //private final OnCurrentProgramUpdatedListener mOnCurrentProgramUpdatedListener;
    private int mEnabledActionIds = TIME_SHIFT_ACTION_ID_PLAY | TIME_SHIFT_ACTION_ID_PAUSE
            | TIME_SHIFT_ACTION_ID_REWIND | TIME_SHIFT_ACTION_ID_FAST_FORWARD
            | TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS | TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT;
    @TimeShiftActionId
    private int mLastActionId = 0;
    private int mSpeed = -1;

    // TODO: Remove these variables once API level 23 is available.
    private Context mContext;

    private TIFProgramInfo mCurrentProgram;
    public boolean isTimeshiftStopped = false;
    public boolean isTimeshiftStarted = false;
    // This variable is used to block notification while changing the availability status.
    private boolean mNotificationEnabled;
    private boolean mAvailabilityChanged = false;

    private final Handler mHandler = new TimeShiftHandler(this);

    public TifTimeShiftManager(Context context, TvSurfaceView tvView){
            //ProgramDataManager programDataManager) {
        mContext = context;
        mPlayController = new PlayController(tvView);
        mProgramManager = new ProgramManager();
        mMtkTvTime = MtkTvTime.getInstance();

        DevManager.getInstance().addDevListener(this);

        RxBus.instance.onFirstEvent(MainActivityDestroyEvent.class)
                .doOnSuccess(it -> free())
                .subscribe();
    }

    private void free(){
        synchronized (TifTimeShiftManager.class) {
            mTifTimeShiftManager = null;
        }
        DevManager.getInstance().removeDevListener(this);
    }

    private static TifTimeShiftManager mTifTimeShiftManager ;
    public synchronized static TifTimeShiftManager getInstanceEx(Context context, TvSurfaceView tvView){
    	if (mTifTimeShiftManager == null ){
    	    mTifTimeShiftManager = new  TifTimeShiftManager(context, tvView);
    	}else{
    	    mTifTimeShiftManager.updatePlayController(context, tvView);
    	}

    	return mTifTimeShiftManager ;
    }

    public synchronized static TifTimeShiftManager getInstance(){
    	return mTifTimeShiftManager ;
    }

    public void  updatePlayController(Context context, TvSurfaceView tvView) {
        mContext = context;
        mPlayController = new PlayController(tvView);
    }
    /**
     * Sets a listener which will receive events from this class.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Checks if the trick play is available for the current channel.
     */
    public boolean isAvailable() {
        return mPlayController.isAvailable();
    }

    /**
     * Returns the current time position in milliseconds.
     */
    public long getCurrentPositionMs() {
        if (mCurrentPositionMediator.mCurrentPositionMs < mPlayController.getRecordTimeMs() && mPlayController.getTvView().timeshiftGetCurrentPositionMs() > mPlayController.getRecordTimeMs()) {
            return mPlayController.getTvView().timeshiftGetCurrentPositionMs();
        }
        return mCurrentPositionMediator.mCurrentPositionMs;
    }

    void setCurrentPositionMs(long currentTimeMs) {
        mCurrentPositionMediator.onCurrentPositionChanged(currentTimeMs);
    }

    /**
     * Returns the start time of the recording in milliseconds.
     */
    public long getRecordStartTimeMs() {
        long oldestProgramStartTime = mProgramManager.getOldestProgramStartTime();
        return oldestProgramStartTime == INVALID_TIME ? INVALID_TIME
                : mPlayController.mRecordStartTimeMs;
    }

    /**
     * Return utc time for millis seconds,Live
     * the same as system.currentTimeMillis() in augie p with time sync stream.
     */
    public long getBroadcastTimeInUtcSeconds() {
        long milliSeconds = mMtkTvTime.getCurrentTimeInUtcMilliSeconds();
        long seconds = mMtkTvTime.getLocalTime().toSeconds();
        long millis = mMtkTvTime.getLocalTime().toMillis();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Get time milliSeconds: " + milliSeconds);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Get time seconds: " + seconds);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Get time millis: " + millis);
        return milliSeconds; //mMtkTvTimeBase.getBroadcastTimeInUtcSeconds() * 1000;
    }

    /**
     * Plays the media.
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void play() {
        if (!isActionEnabled(TIME_SHIFT_ACTION_ID_PLAY)) {
            return;
        }
        mLastActionId = TIME_SHIFT_ACTION_ID_PLAY;
        mPlayController.play();
        updateActions();
    }


    public void speed(float speed){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "speed() speed= " + speed);
        if (mListener != null) {
        	mSpeed = (int)speed;
            mListener.onSpeedChange(speed);
        }
    }

    public int getTimeshiftSpeed() {
    	return mSpeed;
    }

    public boolean isTimeshiftStarted(){
        return isTimeshiftStarted;
    }

    /**
     * TTS open,speakText
     * @param speaktext
     */
    public void speakText(String speaktext){
        DvrManager.getInstance().speakText(speaktext);
    }

    /**
     * only stop play, not stop record
     */
    public void stop(){
        mCurrentPositionMediator.mCurrentPositionMs = 0;
        mAvailabilityChanged = false;
        mPlayController.stop();
    }

    /**
     * both stop play and record
     */
    public void stopAll(){
        mCurrentPositionMediator.mCurrentPositionMs = 0;
        mAvailabilityChanged = false;
        mPlayController.stopAll();
    }

    public boolean isForwarding(){
        return mPlayController.isForwarding();
    }

    public boolean isRewinding(){
        return mPlayController.isRewinding();
    }

    /**
     * Pauses the playback.
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void pause() {
        if (!isActionEnabled(TIME_SHIFT_ACTION_ID_PAUSE)) {
            return;
        }
        mLastActionId = TIME_SHIFT_ACTION_ID_PAUSE;
        mPlayController.pause();
        updateActions();
    }

    /**
     * Toggles the playing and paused state.
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void togglePlayPause() {
        mPlayController.togglePlayPause();
    }

    /**
     * Plays the media in backward direction. The playback speed is increased by 1x each time
     * this is called. The range of the speed is from 2x to 5x.
     * If the playing position is considered the same as the record start time, it does nothing
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void rewind() {
        if (!isActionEnabled(TIME_SHIFT_ACTION_ID_REWIND)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshift rewind action disabled.");
            return;
        }
        mLastActionId = TIME_SHIFT_ACTION_ID_REWIND;
        mPlayController.rewind();
        updateActions();
    }

    /**
     * Plays the media in forward direction. The playback speed is increased by 1x each time
     * this is called. The range of the speed is from 2x to 5x.
     * If the playing position is the same as the current time, it does nothing.
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void fastForward() {
        if (!isActionEnabled(TIME_SHIFT_ACTION_ID_FAST_FORWARD)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshift fastforward action disabled.");
            return;
        }
        mLastActionId = TIME_SHIFT_ACTION_ID_FAST_FORWARD;
        mPlayController.fastForward();
        updateActions();
    }

    /**
     * Jumps to the start of the current program.
     * If the currently playing position is within 3 seconds
     * (={@link #PROGRAM_START_TIME_THRESHOLD})from the start time of the program, it goes to
     * the start of the previous program if exists.
     * If the playing position is the same as the record start time, it does nothing.
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void jumpToPrevious() {
        if (!isActionEnabled(TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS)) {
            return;
        }
        TIFProgramInfo program = mProgramManager.getProgramAt(
                mCurrentPositionMediator.mCurrentPositionMs - PROGRAM_START_TIME_THRESHOLD);
        if (program == null) {
            return;
        }
        long seekPosition =
                Math.max(program.getmStartTimeUtcSec(), mPlayController.mRecordStartTimeMs);
        mLastActionId = TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS;
        mPlayController.seekTo(seekPosition);
        mCurrentPositionMediator.onSeekRequested(seekPosition);
        speakText("timeshift jump to previous");
        updateActions();
    }

    /**
     * Jumps to the start of the next program if exists.
     * If there's no next program, it jumps to the current system time and shows the live TV.
     * If the playing position is considered the same as the current time, it does nothing.
     *
     * @throws IllegalStateException if the trick play is not available.
     */
    public void jumpToNext() {
        if (!isActionEnabled(TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT)) {
            return;
        }
        TIFProgramInfo currentProgram = mProgramManager.getProgramAt(
                mCurrentPositionMediator.mCurrentPositionMs);
        if (currentProgram == null) {
            return;
        }
        TIFProgramInfo nextProgram = mProgramManager.getProgramAt(currentProgram.getmEndTimeUtcSec() );
        long currentTimeMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();
        mLastActionId = TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT;
        if (nextProgram == null || nextProgram.getmStartTimeUtcSec() > currentTimeMs) {
            mPlayController.seekTo(currentTimeMs);
            if (mPlayController.isForwarding()) {
                // The current position will be the current system time from now.
                mPlayController.mIsPlayOffsetChanged = false;
                mCurrentPositionMediator.initialize(currentTimeMs);
            } else {
                // The current position would not be the current system time.
                // So need to wait for the correct time from TIS.
                mCurrentPositionMediator.onSeekRequested(currentTimeMs);
            }
        } else {
            mPlayController.seekTo(nextProgram.getmStartTimeUtcSec());
            mCurrentPositionMediator.onSeekRequested(nextProgram.getmStartTimeUtcSec());
        }
        speakText("timeshift jump to next");
        updateActions();
    }

    /**
     * Returns the playback status. The value is PLAY_STATUS_PAUSED or PLAY_STATUS_PLAYING.
     */
    @PlayStatus public int getPlayStatus() {
        return mPlayController.mPlayStatus;
    }

    /**
     * Returns the displayed playback speed. The value is one of PLAY_SPEED_1X, PLAY_SPEED_2X,
     * PLAY_SPEED_3X, PLAY_SPEED_4X and PLAY_SPEED_5X.
     */
    @PlaySpeed public int getDisplayedPlaySpeed() {
        return mPlayController.mDisplayedPlaySpeed;
    }

    public int getmPlaybackSpeed() {
        return mPlayController.mPlaybackSpeed;
    }
    /**
     * Returns the playback speed. The value is PLAY_DIRECTION_FORWARD or PLAY_DIRECTION_BACKWARD.
     */
    @PlayDirection public int getPlayDirection() {
        return mPlayController.mPlayDirection;
    }

    /**
     * Returns the ID of the last action..
     */
    @TimeShiftActionId public int getLastActionId() {
        return mLastActionId;
    }

    /**
     * Enables or disables the time-shift actions.
     */
    @VisibleForTesting
    void enableAction(@TimeShiftActionId int actionId, boolean enable) {
        int oldEnabledActionIds = mEnabledActionIds;
        if (enable) {
            mEnabledActionIds |= actionId;
        } else {
            mEnabledActionIds &= ~actionId;
        }
        if (mNotificationEnabled && mListener != null
                && oldEnabledActionIds != mEnabledActionIds) {
            mListener.onActionEnabledChanged(actionId, enable);
        }
    }

    public boolean isActionEnabled(@TimeShiftActionId int actionId) {
        return (mEnabledActionIds & actionId) == actionId;
    }

    private void updateActions() {
        if (isAvailable()) {
            enableAction(TIME_SHIFT_ACTION_ID_PLAY, true);
            enableAction(TIME_SHIFT_ACTION_ID_PAUSE, true);
            // Rewind action and jump to previous action.
            long threshold = isActionEnabled(TIME_SHIFT_ACTION_ID_REWIND)
                    ? DISABLE_ACTION_THRESHOLD : ENABLE_ACTION_THRESHOLD;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateActions rewind threshold:" + threshold);
            boolean enabled = mCurrentPositionMediator.mCurrentPositionMs
                    - mPlayController.mRecordStartTimeMs > DISABLE_ACTION_THRESHOLD;
            enableAction(TIME_SHIFT_ACTION_ID_REWIND, true); // can not set enabled because QA testing after switch channel.
            enableAction(TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS, enabled);
            // Fast forward action and jump to next action
            threshold = isActionEnabled(TIME_SHIFT_ACTION_ID_FAST_FORWARD)
                    ? DISABLE_ACTION_THRESHOLD : ENABLE_ACTION_THRESHOLD;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateActions fastforward threshold:" + threshold);
            enabled = getBroadcastTimeInUtcSeconds()/*System.currentTimeMillis()*/ - mCurrentPositionMediator.mCurrentPositionMs
                    > DISABLE_ACTION_THRESHOLD;
            enableAction(TIME_SHIFT_ACTION_ID_FAST_FORWARD, true);
            enableAction(TIME_SHIFT_ACTION_ID_JUMP_TO_NEXT, enabled);
        } else {
            enableAction(TIME_SHIFT_ACTION_ID_PLAY, false);
            enableAction(TIME_SHIFT_ACTION_ID_PAUSE, false);
            enableAction(TIME_SHIFT_ACTION_ID_REWIND, false);
            enableAction(TIME_SHIFT_ACTION_ID_JUMP_TO_PREVIOUS, false);
            enableAction(TIME_SHIFT_ACTION_ID_FAST_FORWARD, false);
            enableAction(TIME_SHIFT_ACTION_ID_PLAY, false);
        }
    }

    private void updateCurrentProgram() {
        TIFProgramInfo currentProgram = getProgramAt(mCurrentPositionMediator.mCurrentPositionMs);

        if (!Objects.equals(mCurrentProgram, currentProgram)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Current program has been updated. " + currentProgram);
            mCurrentProgram = currentProgram;
            if (mNotificationEnabled ) {
                TIFChannelInfo channel = mPlayController.getCurrentChannel();
                if (channel != null) {
                    mPlayController.onCurrentProgramChanged();
                }
            }
        }
    }

    /**
     * Returns {@code true} if the trick play is available and it's playing to the forward direction
     * with normal speed, otherwise {@code false}.
     */
    public boolean isNormalPlaying() {
        return mPlayController.isAvailable()
                && mPlayController.mPlayStatus == PLAY_STATUS_PLAYING
                && mPlayController.mPlayDirection == PLAY_DIRECTION_FORWARD
                && mPlayController.mDisplayedPlaySpeed == PLAY_SPEED_1X;
    }

    /**
     * Checks if the trick play is available and it's playback status is paused.
     */
    public boolean isPaused() {
        return mPlayController.isAvailable() && mPlayController.mPlayStatus == PLAY_STATUS_PAUSED;
    }

    public boolean getAvailabilityChanged() {
        return mAvailabilityChanged;
    }

    /**
     * Returns the program which airs at the given time.
     */
    @NonNull
    public TIFProgramInfo getProgramAt(long timeMs) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getProgramAt timeMs: " + getTimeString(timeMs));
        TIFProgramInfo program = mProgramManager.getProgramAt(timeMs);
        if (program == null) {
            // Guard just in case when the program prefetch handler doesn't work on time.
            mProgramManager.addDummyProgramsAt(timeMs);
            program = mProgramManager.getProgramAt(timeMs);

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initializeTimeline, " + program);
        }
        return program;
    }

    void onAvailabilityChanged() {
        mProgramManager.onAvailabilityChanged(mPlayController.isAvailable(),
                mPlayController.getCurrentChannel(), mPlayController.mRecordStartTimeMs);
        updateActions();
        // Availability change notification should be always sent
        // even if mNotificationEnabled is false.
        if (mListener != null) {
            mListener.onAvailabilityChanged();
        }
    }

    void onRecordStartTimeChanged() {
        if (mPlayController.isAvailable()) {
            mProgramManager.onRecordStartTimeChanged(mPlayController.mRecordStartTimeMs);
        }
        updateActions();
        if (mNotificationEnabled && mListener != null) {
            mListener.onRecordStartTimeChanged();
        }
    }

    void onCurrentPositionChanged() {
        updateActions();
        updateCurrentProgram();
        if (mNotificationEnabled && mListener != null) {
            mListener.onCurrentPositionChanged();
        }
    }

    void onPlayStatusChanged(@PlayStatus int status) {
        if (mNotificationEnabled && mListener != null) {
            mListener.onPlayStatusChanged(status);
        }
    }

    void onTimeshiftSucceed(){
        if (mListener != null) {
            mListener.onTimeShiftSucceed();
        }
    }

    void onTimeshiftError(int errorStatus){
        if (mListener != null) {
            mListener.onTimeShiftError(errorStatus);
        }
    }

    void onProgramInfoChanged() {
        updateCurrentProgram();
        if (mNotificationEnabled && mListener != null) {
            mListener.onProgramInfoChanged();
        }
    }

    void onError(int errId) {
        if (mListener != null) {
            mListener.onError(errId);
        }
    }

    /**
     * Returns the current program which airs right now.<p>
     *
     * If the program is a dummy program, which means there's no program information,
     * returns {@code null}.
     */
    @Nullable
    public TIFProgramInfo getCurrentProgram() {
        if (isAvailable()) {
            return mCurrentProgram;
        }
        return null;
    }

    private int getPlaybackSpeed() {
        int[] playbackSpeedList;
        if (getCurrentProgram() == null || getCurrentProgram().getmEndTimeUtcSec()
                - getCurrentProgram().getmStartTimeUtcSec() > SHORT_PROGRAM_THRESHOLD_MILLIS) {
            playbackSpeedList = LONG_PROGRAM_SPEED_FACTORS;
        } else {
            playbackSpeedList = SHORT_PROGRAM_SPEED_FACTORS;
        }
        switch (mPlayController.mDisplayedPlaySpeed) {
            case PLAY_SPEED_1X:
                return 1;
            case PLAY_SPEED_2X:
                return playbackSpeedList[0];
            case PLAY_SPEED_3X:
                return playbackSpeedList[1];
            case PLAY_SPEED_4X:
                return playbackSpeedList[2];
            case PLAY_SPEED_5X:
                return playbackSpeedList[3];
            case PLAY_SPEED_6X:
                return playbackSpeedList[4];
            default:
                com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Unknown displayed play speed is chosen : "
                        + mPlayController.mDisplayedPlaySpeed);
                return 1;
        }
    }



    @Override
    public void onEvent(DeviceManagerEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "DeviceManagerEvent: " + event.getType());
        switch (event.getType()) {
            case DeviceManagerEvent.umounted:
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "DeviceManagerEvent.umounted");
                stopAll();
                SaveValue.getInstance(mContext).setLocalMemoryValue(
                        MenuConfigManager.TIMESHIFT_START, false);
              /*  SaveValue.saveWorldBooleanValue(mContext,
                        MenuConfigManager.TIMESHIFT_START, false, false);*/
                break;
            default:
                break;

        }
    }


    /**
     * A class which controls the trick play.
     */
    private class PlayController {
        private final TvSurfaceView mTvView;

        private long mPossibleStartTimeMs;
        private long mRecordStartTimeMs;
        private long mSystemTimeMs = 0;

        @PlayStatus private int mPlayStatus = PLAY_STATUS_PAUSED;
        @PlaySpeed private int mDisplayedPlaySpeed = PLAY_SPEED_1X;
        @PlayDirection private int mPlayDirection = PLAY_DIRECTION_FORWARD;
        private int mPlaybackSpeed;

        /**
         * Indicates that the trick play is not playing the current time position.
         * It is set true when {@link PlayController#pause}, {@link PlayController#rewind},
         * {@link PlayController#fastForward} and {@link PlayController#seekTo}
         * is called.
         * If it is true, the current time is equal to System.currentTimeMillis().
         */
        private boolean mIsPlayOffsetChanged;

        PlayController(TvSurfaceView tvView) {
            mTvView = tvView;
            mTvView.setTimeShiftListener(new TimeShiftListener() {

                @Override
                public void onAvailabilityChanged() {
                    // Do not send the notifications while the availability is changing,
                    // because the variables are in the intermediate state.
                    // For example, the current program can be null.
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onAvailabilityChanged start.");
                    mAvailabilityChanged = true;
                    mNotificationEnabled = false;
                    mDisplayedPlaySpeed = PLAY_SPEED_1X;
                    mPlaybackSpeed = 1;
                    mPlayDirection = PLAY_DIRECTION_FORWARD;
                    mIsPlayOffsetChanged = false;
                    mPossibleStartTimeMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();
                    mRecordStartTimeMs = mPossibleStartTimeMs;
                    mSystemTimeMs = mPossibleStartTimeMs;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onAvailabilityChanged mPossibleStartTimeMs: " + getTimeString(mPossibleStartTimeMs));
                    mCurrentPositionMediator.initialize(mPossibleStartTimeMs);
                    mHandler.removeMessages(MSG_GET_CURRENT_POSITION);

                    if (isAvailable()) {
                        // When the media availability message has come.
                        mPlayController.setPlayStatus(PLAY_STATUS_PLAYING);
                        mHandler.sendEmptyMessageDelayed(MSG_GET_CURRENT_POSITION,
                                3 * REQUEST_CURRENT_POSITION_INTERVAL); // Delayed 3 sec for rewind and fastforward action enabled.
                    } else {
                        // When the tune command is sent.
                        mPlayController.setPlayStatus(PLAY_STATUS_PAUSED);
                    }
                    TifTimeShiftManager.this.onAvailabilityChanged();
                    mNotificationEnabled = true;
                }

                @Override
                public void onRecordStartTimeChanged(long recordStartTimeMs) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRecordStartTimeChanged recordStartTimeMs: " + getTimeString(recordStartTimeMs));
//                    if (recordStartTimeMs < mPossibleStartTimeMs) {
//                        // Do not warn in this case because it can happen in normal cases.
//                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Record start time is less then the time when it became "
//                                + "available. {availableStartTime="
//                                + Utils.toTimeString(mPossibleStartTimeMs)
//                                + ", recordStartTimeMs=" + Utils.toTimeString(recordStartTimeMs)
//                                + "}");
//                        recordStartTimeMs = mPossibleStartTimeMs;
//                    }
                    if (mRecordStartTimeMs == recordStartTimeMs) {
                        return;
                    }
                    mRecordStartTimeMs = recordStartTimeMs;
                    TifTimeShiftManager.this.onRecordStartTimeChanged();

                    // According to the UX guidelines, the stream should be resumed if the
                    // recording buffer fills up while paused, which means that the current time
                    // position is the same as or before the recording start time.
                    // But, for this application and the TIS, it's an erroneous and confusing
                    // situation if the current time position is before the recording start time.
                    // So, we recommend the TIS to keep the current time position greater than or
                    // equal to the recording start time.
                    // And here, we assume that the buffer is full if the current time position
                    // is nearly equal to the recording start time.
                    /*if (mPlayStatus == PLAY_STATUS_PAUSED &&
                            getCurrentPositionMs() - mRecordStartTimeMs
                            < RECORDING_BOUNDARY_THRESHOLD) {
                        TifTimeShiftManager.this.play();
                    }*/
                }

                @Override
                public void onSpeechChange(float speech) {
                    TifTimeShiftManager.this.speed(speech);

                }

                @Override
                public void onPlayStatusChanged(int status) {
                    // setPlayStatus(status); // No need set mPlayStatus, and need to refresh UI.
                    TifTimeShiftManager.this.onPlayStatusChanged(status);
                }

                @Override
                public void onChannelChanged(String inputId, Uri channelUri) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onChannelChanged, inputId:"+inputId +", channelUri:"+channelUri);
                }

                @Override
                public void onTimeshiftRecordStart(boolean isStarted) {
                    if(DiskSettingSubMenuDialog.isFormat()&&isStarted){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"is formating");
                        MtkTvTimeshift.getInstance().setAutoRecord(false);
                        stopAll();
                    }else{
                        isTimeshiftStarted = isStarted;
                    }
                    if(!isStarted &&DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog()!=null
                            &&DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().isShowing()
                            &&DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().getDvrEventHandler()!=null){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"stop timeshift success");
                       Message args =  DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().getDvrEventHandler().obtainMessage();
                        args.what = DvrConstant.MSG_CALLBACK_TIMESHIFT_EVENT;
                        DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().getDvrEventHandler().sendMessage(args);
                    }
                }

                @Override
                public void onTimeshiftSucceed() {
                    TifTimeShiftManager.this.onTimeshiftSucceed();
                }

                @Override
                public void onTimeshiftError(int errorStatus) {
                    TifTimeShiftManager.this.onTimeshiftError(errorStatus);
                }
            });
        }

        boolean isAvailable() {
            return mTvView.isTimeShiftAvailable() ;
        }

        void handleGetCurrentPosition() {
            long currentTimeMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();
            /*if(mSystemTimeMs != 0) {
                if(Math.abs(mSystemTimeMs - currentTimeMs) >
                    INVALID_TIMESHIFT_DURATION) {

                    TifTimeShiftManager.this.onError(ERROR_DATE_TIME);
                    isTimeshiftStopped = true;

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleGetCurrentPosition, error");
                    return ;
                }
            }*/

            mSystemTimeMs = currentTimeMs;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleGetCurrentPosition, mIsPlayOffsetChanged: " + mIsPlayOffsetChanged);
            if (mIsPlayOffsetChanged) {
                long currentPositionMs = Math.max(mTvView.timeshiftGetCurrentPositionMs(),
                        //Math.min(mTvView.timeshiftGetCurrentPositionMs(), currentTimeMs),
                        // should use callback currentposition to update ui
                        mRecordStartTimeMs);
                boolean isCurrentTime =
                        currentTimeMs - currentPositionMs < RECORDING_BOUNDARY_THRESHOLD;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleGetCurrentPosition, currentPositionMs: " + getTimeString(currentPositionMs) + ", isCurrentTime:" + isCurrentTime);
                //long newCurrentPositionMs;
                if (isCurrentTime && isForwarding()) {
                    // It's playing forward and the current playing position reached
                    // the current system time. i.e. The live stream is played.
                    // Therefore no need to call TvView.timeshiftGetCurrentPositionMs
                    // any more.
                    //newCurrentPositionMs = currentTimeMs;
                    mIsPlayOffsetChanged = false;
                    // FF to end must stop timeshift, not play.
//                    if (mDisplayedPlaySpeed > PLAY_SPEED_1X) {
//                        TifTimeShiftManager.this.play();
//                    }
                } else {
                    //newCurrentPositionMs = currentPositionMs;
                    boolean isRecordStartTime = currentPositionMs - mRecordStartTimeMs
                            < RECORDING_BOUNDARY_THRESHOLD;
                    if (isRecordStartTime && isRewinding()) {
                        TifTimeShiftManager.this.play();
                    }
                }
                setCurrentPositionMs(mTvView.timeshiftGetCurrentPositionMs());
            } else {
                setCurrentPositionMs(getBroadcastTimeInUtcSeconds()/*System.currentTimeMillis()*/);
            }
            TifTimeShiftManager.this.onCurrentPositionChanged();
            // Need to send message here just in case there is no or invalid response
            // for the current time position request from TIS.
            mHandler.sendEmptyMessageDelayed(MSG_GET_CURRENT_POSITION,
                    REQUEST_CURRENT_POSITION_INTERVAL);
        }

        void play() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "play");
            //DvrManager.getInstance().speakText("timeshift play");
            mDisplayedPlaySpeed = PLAY_SPEED_1X;
            mPlaybackSpeed = 1;
            mPlayDirection = PLAY_DIRECTION_FORWARD;
            try {
                mTvView.timeshiftPlayEx();
            } catch (Exception e) {
                // TODO: handle exception
                return;
            }
            setPlayStatus(PLAY_STATUS_PLAYING);
        }

        void pause() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pause");
            speakText("timeshift pause");
            mDisplayedPlaySpeed = PLAY_SPEED_1X;
            mPlaybackSpeed = 1;
            try {
                mTvView.timeshiftPauseEx();
            } catch (Exception e) {
                // TODO: handle exception
                return;
            }
            setPlayStatus(PLAY_STATUS_PAUSED);
            mIsPlayOffsetChanged = true;
        }


        void stop(){
            mPossibleStartTimeMs = 0;
            mRecordStartTimeMs = 0;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stop");
            mTvView.sendAppPrivateCommand("session_event_timeshift_stop_mmp_and_select_tv",new Bundle());
        }

        void stopAll(){
            mPossibleStartTimeMs = 0;
            mRecordStartTimeMs = 0;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopAll");
            mTvView.sendAppPrivateCommand("session_event_timeshift_stop_rec",new Bundle());
        }

        void togglePlayPause() {
            if (mPlayStatus == PLAY_STATUS_PAUSED) {
                speakText("timeshift play");
                play();
            } else {
                pause();
            }
        }

        void rewind() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rewind");
            speakText("timeshift fast rewind");
            if (mPlayDirection == PLAY_DIRECTION_BACKWARD) {
                increaseDisplayedPlaySpeed();
            } else {
                mDisplayedPlaySpeed = PLAY_SPEED_2X;
            }
            mPlayDirection = PLAY_DIRECTION_BACKWARD;
            mPlaybackSpeed = getPlaybackSpeed();
            try {
                mTvView.timeshiftRewind(mPlaybackSpeed);
            } catch (Exception e) {
                // TODO: handle exception
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "rewind error " + e.getMessage());
                return;
            }
            setPlayStatus(PLAY_STATUS_PLAYING);
            mIsPlayOffsetChanged = true;
        }

        void fastForward() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "fastForward");
            speakText("timeshift fastForward");
            if (mPlayDirection == PLAY_DIRECTION_FORWARD) {
                increaseDisplayedPlaySpeed();
            } else {
                mDisplayedPlaySpeed = PLAY_SPEED_2X;
            }
            mPlayDirection = PLAY_DIRECTION_FORWARD;
            mPlaybackSpeed = getPlaybackSpeed();
            try {
                mTvView.timeshiftFastForward(mPlaybackSpeed);
            } catch (Exception e) {
                // TODO: handle exception
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "fastForward error " + e.getMessage());
                return;
            }
            setPlayStatus(PLAY_STATUS_PLAYING);
            mIsPlayOffsetChanged = true;
        }

        /**
         * Moves to the specified time.
         */
        void seekTo(long timeMs) {
            try {
                mTvView.timeshiftSeekTo(Math.min(getBroadcastTimeInUtcSeconds()/*System.currentTimeMillis()*/,
                        Math.max(mRecordStartTimeMs, timeMs)));
            } catch (Exception e) {
                // TODO: handle exception
                return;
            }
            mIsPlayOffsetChanged = true;
        }

        void onCurrentProgramChanged() {
            // Update playback speed
            if (mDisplayedPlaySpeed == PLAY_SPEED_1X) {
                return;
            }
            int playbackSpeed = getPlaybackSpeed();
            if (playbackSpeed != mPlaybackSpeed) {
                mPlaybackSpeed = playbackSpeed;
                if (mPlayDirection == PLAY_DIRECTION_FORWARD) {
                    try {
                        mTvView.timeshiftFastForward(mPlaybackSpeed);
                    } catch (Exception e) {
                        // TODO: handle exception
                        return;
                    }
                } else {
                    try {
                        mTvView.timeshiftRewind(mPlaybackSpeed);
                    } catch (Exception e) {
                        // TODO: handle exception
                        return;
                    }
                }
            }
        }

        private void increaseDisplayedPlaySpeed() {
            switch (mDisplayedPlaySpeed) {
                case PLAY_SPEED_1X:
                    mDisplayedPlaySpeed = PLAY_SPEED_2X;
                    break;
                case PLAY_SPEED_2X:
                    mDisplayedPlaySpeed = PLAY_SPEED_3X;
                    break;
                case PLAY_SPEED_3X:
                    mDisplayedPlaySpeed = PLAY_SPEED_4X;
                    break;
                case PLAY_SPEED_4X:
                    mDisplayedPlaySpeed = PLAY_SPEED_5X;
                    break;
                case PLAY_SPEED_5X:
                    mDisplayedPlaySpeed = PLAY_SPEED_6X;
                    break;
				case PLAY_SPEED_6X:
                    mDisplayedPlaySpeed = PLAY_SPEED_2X;
                    break;
                default:
                    break;
            }
        }

        private void setPlayStatus(@PlayStatus int status) {
            if (status != 4) { // value 4 is auto play status
                mPlayStatus = status;
            }
            // TifTimeShiftManager.this.onPlayStatusChanged(status); // ap set status is only to update mPlayStatus.
        }

        boolean isForwarding() {
            return mPlayStatus == PLAY_STATUS_PLAYING && mPlayDirection == PLAY_DIRECTION_FORWARD;
        }

        private boolean isRewinding() {
            return mPlayStatus == PLAY_STATUS_PLAYING && mPlayDirection == PLAY_DIRECTION_BACKWARD;
        }

        TIFChannelInfo getCurrentChannel() {
           int id =  CommonIntegration.getInstance().getCurrentChannelId();
            return TIFChannelManager.getInstance(mContext).getTIFChannelInfoById(id);
        }

        public TvSurfaceView getTvView() {
            return mTvView;
        }

        public long getRecordTimeMs() {
            return mRecordStartTimeMs;
        }
    }

    private class ProgramManager {
        //private final ProgramDataManager mProgramDataManager;
        private TIFChannelInfo mChannel;
        private final List<TIFProgramInfo> mPrograms = new ArrayList<TIFProgramInfo>();
        private final Queue<Range<Long>> mProgramLoadQueue = new LinkedList<>();
      //  private final LoadProgramsForCurrentChannelTask mProgramLoadTask = null;

        ProgramManager() {//ProgramDataManager programDataManager
           // mProgramDataManager = programDataManager;
        }

        void onAvailabilityChanged(boolean available, TIFChannelInfo channel, long currentPositionMs) {
            mProgramLoadQueue.clear();
           /* if (mProgramLoadTask != null) {
                mProgramLoadTask.cancel(true);
            }*/
            mHandler.removeMessages(MSG_PREFETCH_PROGRAM);
            mPrograms.clear();
            mChannel = channel;
            if (channel == null ) {
                return;
            }
            if (available) {
                //TIFProgramInfo program = null;mProgramDataManager.getCurrentProgram(channel.getId());
                long prefetchStartTimeMs;
//                if (program != null) { // converity program = null
//                    mPrograms.add(program);
//                    prefetchStartTimeMs = program.getmEndTimeUtcSec() ;
//                } else {
                 prefetchStartTimeMs = Utils.floorTime(currentPositionMs,
                            MAX_DUMMY_PROGRAM_DURATION);
//                }
                // Create dummy program
                mPrograms.addAll(createDummyPrograms(prefetchStartTimeMs,
                        currentPositionMs + PREFETCH_DURATION_FOR_NEXT));

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onAvailabilityChanged, createDummyPrograms, "
                        + prefetchStartTimeMs + ","
                        + currentPositionMs);
                schedulePrefetchPrograms();
                TifTimeShiftManager.this.onProgramInfoChanged();
            }
        }

        void onRecordStartTimeChanged(long startTimeMs) {
            if (mChannel == null ) {
                return;
            }
            long currentMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();

            long fetchStartTimeMs = Utils.floorTime(startTimeMs, MAX_DUMMY_PROGRAM_DURATION);
            addDummyPrograms(fetchStartTimeMs,
                currentMs + PREFETCH_DURATION_FOR_NEXT);
        }

        private void startTaskIfNeeded() {
            if (mProgramLoadQueue.isEmpty()) {
                return;
            }
           /* if (mProgramLoadTask == null || mProgramLoadTask.isCancelled()) {
                startNext();
            } else {
                switch (mProgramLoadTask.getStatus()) {
                    case PENDING:
                        if (mProgramLoadTask.overlaps(mProgramLoadQueue)) {
                            if (mProgramLoadTask.cancel(true)) {
                                mProgramLoadQueue.add(mProgramLoadTask.getPeriod());
                                mProgramLoadTask = null;
                                startNext();
                            }
                        }
                        break;
                    case RUNNING:
                        // Remove pending task fully satisfied by the current
                        Range<Long> current = mProgramLoadTask.getPeriod();
                        Iterator<Range<Long>> i = mProgramLoadQueue.iterator();
                        while (i.hasNext()) {
                            Range<Long> r = i.next();
                            if (current.contains(r)) {
                                i.remove();
                            }
                        }
                        break;
                    case FINISHED:
                        // The task should have already cleared it self, clear and restart anyways.
                        Log.w(TAG, mProgramLoadTask + " is finished, but was not cleared");
                        startNext();
                        break;
                }
            }*/
        }

        private void startNext() {
          /*  mProgramLoadTask = null;
            if (mProgramLoadQueue.isEmpty()) {
                return;
            }

            Range<Long> next = mProgramLoadQueue.poll();
            // Extend next to include any overlapping Ranges.
            Iterator<Range<Long>> i = mProgramLoadQueue.iterator();
            while(i.hasNext()) {
                Range<Long> r = i.next();
                if(next.contains(r.getLower()) || next.contains(r.getUpper())){
                    i.remove();
                    next = next.extend(r);
                }
            }
            if (mChannel != null) {
                mProgramLoadTask = new LoadProgramsForCurrentChannelTask(
                        mContext.getContentResolver(), next);
                mProgramLoadTask.executeOnDbThread();
            }*/
        }

        void addDummyProgramsAt(long timeMs) {
            addDummyPrograms(timeMs, timeMs + PREFETCH_DURATION_FOR_NEXT);
        }

        private boolean addDummyPrograms(Range<Long> period) {
            return addDummyPrograms(period.getLower(), period.getUpper());
        }

        private boolean addDummyPrograms(long startTimeMs, long endTimeMs) {
            boolean added = false;

            removeDummyPrograms(startTimeMs, endTimeMs);

            if (mPrograms.isEmpty()) {
                // Insert dummy program.
                mPrograms.addAll(createDummyPrograms(startTimeMs, endTimeMs));
                return true;
            }
            // Insert dummy program to the head of the list if needed.
            TIFProgramInfo firstProgram = mPrograms.get(0);
            if (startTimeMs < firstProgram.getmStartTimeUtcSec()) {
                mPrograms.addAll(0,
                    createDummyPrograms(startTimeMs, firstProgram.getmStartTimeUtcSec()));
                added = true;
            }
            // Insert dummy program to the tail of the list if needed.
            TIFProgramInfo lastProgram = mPrograms.get(mPrograms.size() - 1);
            if (endTimeMs - lastProgram.getmEndTimeUtcSec() >= MIN_DUMMY_PROGRAM_DURATION) {
                mPrograms.addAll(
                    createDummyPrograms(lastProgram.getmEndTimeUtcSec() , endTimeMs));
                added = true;
            }
            // Insert dummy programs if the holes exist in the list.
            for (int i = 1; i < mPrograms.size(); ++i) {
                long endOfPrevious = mPrograms.get(i - 1).getmEndTimeUtcSec();
                long startOfCurrent = mPrograms.get(i).getmStartTimeUtcSec();
                if (startOfCurrent > endOfPrevious) {
                    List<TIFProgramInfo> dummyPrograms =
                            createDummyPrograms(endOfPrevious, startOfCurrent);
                    mPrograms.addAll(i, dummyPrograms);
                    i += dummyPrograms.size();
                    added = true;
                }
            }

            if(com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag) {
                dumpPrograms();
            }
            return added;
        }

        /*private void removeDummyPrograms() {
            for (int i = 0; i < mPrograms.size(); ++i) {
                TIFProgramInfo program = mPrograms.get(i);
                if (!program.isValid()) {
                    mPrograms.remove(i--);
                }
            }
        }*/

        private void removeDummyPrograms(long startTimeMs, long endTimeMs) {
            for (int i = 0; i < mPrograms.size(); ++i) {
                TIFProgramInfo program = mPrograms.get(i);
                if (program.getmStartTimeUtcSec() >
                    endTimeMs + PREFETCH_DURATION_FOR_NEXT) {
                    mPrograms.remove(i--);
                }
                else if (program.getmEndTimeUtcSec() <
                    startTimeMs + PREFETCH_DURATION_FOR_NEXT) {
                    mPrograms.remove(i--);
                }
            }
        }

        public boolean isProgramExist(long startTimeMs, long endTimeMs) {
            if (mPrograms == null || mPrograms.isEmpty()) {
                return false;
            }

            for (TIFProgramInfo program : mPrograms) {
                if(program.getmStartTimeUtcSec() == startTimeMs &&
                    program.getmEndTimeUtcSec() == endTimeMs) {
                    return true;
                }
            }

            return false;
        }

        private void removeOverlappedPrograms(List<TIFProgramInfo> loadedPrograms) {
            if (mPrograms.isEmpty()) {
                return;
            }
            TIFProgramInfo program = mPrograms.get(0);
            for (int i = 0, j = 0; i < mPrograms.size() && j < loadedPrograms.size(); ++j) {
                TIFProgramInfo loadedProgram = loadedPrograms.get(j);
                // Skip previous programs.
                while (program.getmEndTimeUtcSec()  < loadedProgram.getmStartTimeUtcSec()) {
                    // Reached end of mPrograms.
                    ++i;
                    if (i == mPrograms.size()) {
                        return;
                    }
                    program = mPrograms.get(i);
                }
                // Remove overlapped programs.
                while (program.getmStartTimeUtcSec() < loadedProgram.getmEndTimeUtcSec()
                        && program.getmEndTimeUtcSec()  > loadedProgram.getmStartTimeUtcSec()) {
                    mPrograms.remove(i);
                    if (i >= mPrograms.size()) {
                        break;
                    }
                    program = mPrograms.get(i);
                }
            }
        }

        // Returns a list of dummy programs.
        // The maximum duration of a dummy program is {@link MAX_DUMMY_PROGRAM_DURATION}.
        // So if the duration ({@code endTimeMs}-{@code startTimeMs}) is greater than the duration,
        // we need to create multiple dummy programs.
        // The reason of the limitation of the duration is because we want the trick play viewer
        // to show the time-line duration of {@link MAX_DUMMY_PROGRAM_DURATION} at most
        // for a dummy program.
        //
        // the two programs will be created if duration is more than {@link MAX_DUMMY_PROGRAM_DURATION}
        //
        private List<TIFProgramInfo> createDummyPrograms(long startTimeMs, long endTimeMs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "createDummyPrograms. " + getTimeString(startTimeMs) + "," + getTimeString(endTimeMs));

            if (startTimeMs >= endTimeMs) {
                return Collections.emptyList();
            }
            List<TIFProgramInfo> programs = new ArrayList<>();
            long start = Utils.floorTime(startTimeMs, MAX_DUMMY_PROGRAM_DURATION);
            long end = Utils.ceilTime(startTimeMs, MAX_DUMMY_PROGRAM_DURATION);
            if (!isProgramExist(start, end) && end < endTimeMs) {
                programs.add(new TIFProgramInfo.Builder()
                        .setmStartTimeUtcSec(start)
                        .setmEndTimeUtcSec(end)
                        .build());
                // start = end;
                end += MAX_DUMMY_PROGRAM_DURATION;
            }

            if(endTimeMs >= end) {
                start = Utils.floorTime(endTimeMs, MAX_DUMMY_PROGRAM_DURATION);
                end = Utils.ceilTime(endTimeMs, MAX_DUMMY_PROGRAM_DURATION);
                if(!isProgramExist(start, end)) {
                    programs.add(new TIFProgramInfo.Builder()
                            .setmStartTimeUtcSec(start)
                            .setmEndTimeUtcSec(end)
                            .build());
                }
            }
            return programs;
        }

        TIFProgramInfo getProgramAt(long timeMs) {
            return getProgramAt(timeMs, 0, mPrograms.size() - 1);
        }

        private TIFProgramInfo getProgramAt(long timeMs, int start, int end) {
            if (start > end) {
                return null;
            }
            int mid = (start + end) / 2;
            TIFProgramInfo program = mPrograms.get(mid);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getProgramAt, " + getTimeString(program.getmStartTimeUtcSec()) + "," +
                    getTimeString(timeMs) + "," + getTimeString(program.getmEndTimeUtcSec()));
            if (program.getmStartTimeUtcSec() > timeMs) {
                return getProgramAt(timeMs, start, mid - 1);
            } else if (program.getmEndTimeUtcSec() <= timeMs) {
                return getProgramAt(timeMs, mid+1, end);
            } else {
                return program;
            }
        }

        private long getOldestProgramStartTime() {
            if (mPrograms.isEmpty()) {
                return INVALID_TIME;
            }
            return mPrograms.get(0).getmStartTimeUtcSec();
        }

        private TIFProgramInfo getLastValidProgram() {
            return mPrograms.get(mPrograms.size() - 1);
//            for (int i = mPrograms.size() - 1; i >= 0; --i) {
//                return mPrograms.get(i);
//            }
//
//            return null;
        }

        private void schedulePrefetchPrograms() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Scheduling prefetching programs.");
            if (mHandler.hasMessages(MSG_PREFETCH_PROGRAM)) {
                return;
            }
            TIFProgramInfo lastValidProgram = getLastValidProgram();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Last valid program = " + lastValidProgram);
            if (lastValidProgram != null) {
                long delay = lastValidProgram.getmEndTimeUtcSec()
                        - PREFETCH_TIME_OFFSET_FROM_PROGRAM_END - getBroadcastTimeInUtcSeconds()/*System.currentTimeMillis()*/;
                mHandler.sendEmptyMessageDelayed(MSG_PREFETCH_PROGRAM, delay);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Scheduling with " + delay + "(ms) delays.");
            } else {
                mHandler.sendEmptyMessage(MSG_PREFETCH_PROGRAM);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Scheduling promptly.");
            }
        }

        private void prefetchPrograms() {
            long startTimeMs;
            TIFProgramInfo lastValidProgram = getLastValidProgram();
            if (lastValidProgram == null) {
                startTimeMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();
            } else {
                // update for startTImeMs lower than endTimeMs
                startTimeMs = lastValidProgram.getmStartTimeUtcSec(); // lastValidProgram.getmEndTimeUtcSec();
            }
            long endTimeMs = getBroadcastTimeInUtcSeconds()/*System.currentTimeMillis()*/ + PREFETCH_DURATION_FOR_NEXT;

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Prefetch task starts: {startTime=" + Utils.toTimeString(startTimeMs)
                    + ", endTime=" + Utils.toTimeString(endTimeMs) + "}");

            try {
                mProgramLoadQueue.add(Range.create(startTimeMs, endTimeMs));
                startTaskIfNeeded();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void dumpPrograms() {
            long time1,
                 time2;
            for (TIFProgramInfo program : mPrograms) {
                time1 = program.getmStartTimeUtcSec();
                time2 = program.getmEndTimeUtcSec();

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "" + time1 +  "[" +
                    DateFormat.getTimeFormat(mContext).format(time1) +
                    "] - " + time2 +  "[" +
                    DateFormat.getTimeFormat(mContext).format(time2) + "]");
            }
        }
    }

    private String getTimeString(long timeMs) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeMs);
//        return DateFormat.getTimeFormat(mContext).format(timeMs);
    }

    @VisibleForTesting
    final class CurrentPositionMediator {
        long mCurrentPositionMs;
        long mSeekRequestTimeMs;

        void initialize(long timeMs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CurrentPositionMediator-->initialize: " + getTimeString(timeMs));
            mSeekRequestTimeMs = INVALID_TIME;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TifTimeShiftManager.CurrentPositionMediator initialize: " + TifTimeShiftManager.this.getTimeString(timeMs));
            if (timeMs < mPlayController.getRecordTimeMs() && mPlayController.getTvView().timeshiftGetCurrentPositionMs() > mPlayController.getRecordTimeMs()) {
                mCurrentPositionMs = mPlayController.getTvView().timeshiftGetCurrentPositionMs();
            } else {
                mCurrentPositionMs = timeMs;
            }
            TifTimeShiftManager.this.onCurrentPositionChanged();
        }

        void onSeekRequested(long seekTimeMs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CurrentPositionMediator-->onSeekRequested: " + getTimeString(seekTimeMs));
            mSeekRequestTimeMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TifTimeShiftManager.CurrentPositionMediator onSeekRequested: " + TifTimeShiftManager.this.getTimeString(seekTimeMs));
            mCurrentPositionMs = seekTimeMs;
            TifTimeShiftManager.this.onCurrentPositionChanged();
        }

        void onCurrentPositionChanged(long currentPositionMs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CurrentPositionMediator-->onCurrentPositionChanged: " + getTimeString(currentPositionMs));
            if (mSeekRequestTimeMs == INVALID_TIME) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TifTimeShiftManager.CurrentPositionMediator onCurrentPositionChanged: " + TifTimeShiftManager.this.getTimeString(currentPositionMs));
                mCurrentPositionMs = currentPositionMs;
                TifTimeShiftManager.this.onCurrentPositionChanged();
                return;
            }
            long currentTimeMs = getBroadcastTimeInUtcSeconds(); //System.currentTimeMillis();
            boolean isValid = Math.abs(currentPositionMs - mCurrentPositionMs) < REQUEST_TIMEOUT_MS;
            boolean isTimeout = currentTimeMs > mSeekRequestTimeMs + REQUEST_TIMEOUT_MS;
            if (isValid || isTimeout) {
                initialize(currentPositionMs);
            } else {
                if (getPlayStatus() == PLAY_STATUS_PLAYING) {
                    if (getPlayDirection() == PLAY_DIRECTION_FORWARD) {
                        mCurrentPositionMs += (currentTimeMs - mSeekRequestTimeMs)
                                * getPlaybackSpeed();
                    } else {
                        mCurrentPositionMs -= (currentTimeMs - mSeekRequestTimeMs)
                                * getPlaybackSpeed();
                    }
                }
                TifTimeShiftManager.this.onCurrentPositionChanged();
            }
        }
    }

    /**
     * The listener used to receive the events by the time-shift manager
     */
    public interface Listener {
        /**
         * Called when the availability of the time-shift for the current channel has been changed.
         * If the time shift is available, {@link TimeShiftManager#getRecordStartTimeMs} should
         * return the valid time.
         */
        void onAvailabilityChanged();

        /**
         * Called when the play status is changed between {@link #PLAY_STATUS_PLAYING} and
         * {@link #PLAY_STATUS_PAUSED}
         *
         * @param status The new play state.
         */
        void onPlayStatusChanged(int status);
      
        void onTimeShiftSucceed();

        void onTimeShiftError(int errorStatus);

        /**
         * Called when the recordStartTime has been changed.
         */
        void onRecordStartTimeChanged();

        /**
         * Called when the current position is changed.
         */
        void onCurrentPositionChanged();

        /**
         * Called when the program information is updated.
         */
        void onProgramInfoChanged();

        /**
         * Called when an action becomes enabled or disabled.
         */
        void onActionEnabledChanged(@TimeShiftActionId int actionId, boolean enabled);

        /**
         * Called when speed changed..
         */
        void onSpeedChange(float speed);

        /**
         * Called when error happened
         */
        void onError(int errId);
    }

    private static class TimeShiftHandler extends WeakHandler<TifTimeShiftManager> {
        public TimeShiftHandler(TifTimeShiftManager ref) {
            super(ref);
        }

        @Override
        public void handleMessage(Message msg, @NonNull TifTimeShiftManager timeShiftManager) {
            switch (msg.what) {
                case MSG_GET_CURRENT_POSITION:
                    timeShiftManager.mPlayController.handleGetCurrentPosition();
                    break;
                case MSG_PREFETCH_PROGRAM:
                    timeShiftManager.mProgramManager.prefetchPrograms();
                    break;
                default:
                    break;
            }
        }
    }
}
