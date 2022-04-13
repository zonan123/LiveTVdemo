package com.mediatek.wwtv.tvcenter.commonview;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.PlaybackParams;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import androidx.annotation.IntDef;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.OnLoadingListener;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.AudioFocusManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr.TimeshiftCallback;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SurfaceView to show TV through TvInputServiceSession.
 *
 * @author MTK40707
 */
public class TvSurfaceView extends TvView implements TimeshiftCallback {
  private static final String TAG = "TvSurfaceView";

  private static final boolean DEBUG = true;
  private final Context mContext;

  private boolean mIsHandleEvent = false;
  private String mLocalInputId = null;
  private Uri mUrl = null;
  // TvView's mCaptionEnabled is private so we generate a new mCaptionEnabled
  private int mCaptionEnabled;

  //    private static final int CAPTION_DEFAULT = 0;
  private static final int CAPTION_ENABLED = 1;
  private static final int CAPTION_DISABLED = 2;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    TIME_SHIFT_STATE_NONE,
    TIME_SHIFT_STATE_PLAY,
    TIME_SHIFT_STATE_PAUSE,
    TIME_SHIFT_STATE_REWIND,
    TIME_SHIFT_STATE_FAST_FORWARD
  })
  public @interface TimeShiftState {}

  public static final int TIME_SHIFT_STATE_NONE = 0;
  public static final int TIME_SHIFT_STATE_PLAY = 1;
  public static final int TIME_SHIFT_STATE_PAUSE = 2;
  public static final int TIME_SHIFT_STATE_REWIND = 3;
  public static final int TIME_SHIFT_STATE_FAST_FORWARD = 4;

  @TimeShiftState private int mTimeShiftState = TIME_SHIFT_STATE_NONE;
  private TimeShiftListener mTimeShiftListener;
  private boolean mTimeShiftAvailable;
  private long mTimeShiftCurrentPositionMs = INVALID_TIME;

  private static final long INVALID_TIME = -1;
  private SaveValue mSaveValue;
  //    private static final String SPNAME = "CHMODE";
  private TIFChannelManager mTIFChannelManager;

  public static final int BLOCK_STATE_NONE = -1;
  public static final int BLOCK_STATE_LOCK = 0;
  public static final int BLOCK_STATE_UNLOCK = 1;

  private TvBlockView mBlockView = null;
  private int mBlockContent = BLOCK_STATE_NONE;
  private boolean isStart;
  private OnLoadingListener mOnLoadingListener;

  private SurfaceView mSurfaceView = null;
  private TvSurfaceLifeCycle mTvSurfaceLifeCycle = null;

  private MtkTvConfig mMtkTvConfig;

  public void setStart(boolean flag) {
    isStart = flag;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setStart:" + isStart);
  }

  public boolean isStart() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isStart:" + isStart);
    return isStart;
  }

  public TvSurfaceView(Context context) {
    this(context, null);
  }

  public TvSurfaceView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TvSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    mContext = context;
    mSaveValue = SaveValue.getInstance(context);
    mTIFChannelManager = TIFChannelManager.getInstance(mContext);
    if (mContext instanceof TvSurfaceLifeCycle) {
      mTvSurfaceLifeCycle = (TvSurfaceLifeCycle) mContext;
    }
    if (mContext instanceof OnLoadingListener) {
      mOnLoadingListener = (OnLoadingListener) mContext;
    }
    mMtkTvConfig = MtkTvConfig.getInstance();
  }

  public void setBlockView(TvBlockView view) {
    synchronized (TvSurfaceView.class) {
      mBlockView = view;
    }
    mTIFChannelManager.setBlockCheck(this::checkBlockFor3rd); // add by jg_jianwang for DTV01570224
  }

  public void setHandleEvent(boolean isHandleEvent) {
    mIsHandleEvent = isHandleEvent;
  }

  @Override
  public void tune(String inputId, Uri channelUri, Bundle params) {
    if (mSaveValue.readValue(CommonIntegration.camUpgrade) == 2) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cam upgrade");
      return;
    }
    long mid = -1;
    if (TextUtils.isEmpty(inputId)) {
      Log.e(TAG, "inputId can not empty!!!");
      return;
    }
    if (mOnLoadingListener != null) {
      mOnLoadingListener.onHideLoading();
    }

    PwdDialog mPWDDialog =
        (PwdDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
    if (mPWDDialog != null) {
      mPWDDialog.dismiss();
      mPWDDialog.setSvctxBlocked(false);
    }

    TvInputCallbackMgr.getInstance(mContext).setTimeshiftCallback(this);
    mTimeShiftCurrentPositionMs = INVALID_TIME;
    this.setTimeShiftPositionCallback(null);
    setTimeShiftAvailable(false);
    Log.d(TAG, "tune inputId : " + inputId);
    if (!inputId.contains("com.mediatek.tvinput/.tuner.TunerInputService")) {
      try {
        mid = ContentUris.parseId(channelUri);
        if (mid != -1) {
          mSaveValue.saveValue(TIFFunctionUtil.current3rdMId, (int) mid);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune inputId mTIFChannelInfo.mId: " + mid);
          // first tune 3rd channel from broadcast channel, set mmp mode to 1.
          int mmpMode = mMtkTvConfig.getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE);
          if (mmpMode != 1) {
            MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
          }
        }
      } catch (Exception e) {
        //   e.printStackTrace();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune inputId tv e.printStackTrace();");
      }

    } else if (!CommonIntegration.getInstance().isCurrentSourceATVforEuPA()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune inputId tv ");
      mSaveValue.saveValue(TIFFunctionUtil.current3rdMId, -1);
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set CHChanging is true.");
    CommonIntegration.getInstance().setCHChanging(true);
    CommonIntegration.getInstance().setAudioFMTUpdate(false);
    if (!TurnkeyUiMainActivity.isUKCountry) {
      BannerView bannerView =
          (BannerView)
              ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
      if (bannerView != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reset banner info before tune.");
        bannerView.reset();
        bannerView.setVisibility(GONE);
      }
    } else {
      UkBannerView bannerView =
          (UkBannerView)
              ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
      if (bannerView != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reset ukbanner info before tune.");
        bannerView.reset();
      }
    }
    TwinkleDialog twinkleDialog = ((TwinkleDialog) ComponentsManager.getInstance()
            .getComponentById(NavBasic.NAV_COMP_ID_TWINKLE_DIALOG));
    if(twinkleDialog != null) {
      twinkleDialog.reset();
    }

    /* this is for 3rd network channels, but network channels is never happened now.
     * the flow could be removed.
    if (mid == -1) {
      AudioFocusManager.getInstance(mContext).requestAudioFocus();
    }*/

    super.tune(inputId, channelUri, params);
    synchronized (TvSurfaceView.class) {
      mBlockView.setVisibility(View.GONE, TvBlockView.BLOCK_UNTIL_TUNE_INPUT_AFTER_BOOT);
    }
    // record inputId
    mLocalInputId = inputId;
    mUrl = channelUri;

    mBlockContent = BLOCK_STATE_NONE;
    checkBlock(mid != -1);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, inputId + ", mid = " + mid);
    setStart(true);
    // 3rd source can not show banner in tune.
    if (TurnkeyUiMainActivity.isUKCountry) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ukbanner gone before tune.");
      UkBannerView bannerView =
          (UkBannerView)
              ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
      if (bannerView != null && CommonIntegration.getInstance().is3rdTVSource()) {
        bannerView.setVisibility(View.GONE);
      }
    }
  }

  @Override
  public void removeView(View view) {
    super.removeView(view);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "removeView");
    if (mSurfaceView != null && mSurfaceView.getHolder() != null) {
      mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
    }
  }

  @Override
  public void addView(View child) {
    super.addView(child);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addView");
    if (child instanceof SurfaceView) {
      mSurfaceView = (SurfaceView) child;
    }

    if (mSurfaceView != null && mSurfaceView.getHolder() != null) {
      mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
    }
  }

  private final SurfaceHolder.Callback mSurfaceHolderCallback =
      new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "surfaceDestroyed");
          if (mTvSurfaceLifeCycle != null) {
            mTvSurfaceLifeCycle.surfaceDestroyed();
          }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "surfaceChanged");
        }
      };

  public interface TvSurfaceLifeCycle {
    void surfaceDestroyed();
  }

  public void reset() {
    super.reset();
    setStart(false);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reset()");
  }

  /**
   * Plays a given recorded TV program.
   *
   * @param inputId The ID of the TV input that created the given recorded program.
   * @param recordedProgramUri The URI of a recorded program.
   */
  public void timeShiftPlay(String inputId, Uri recordedProgramUri) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "timeShiftPlay, setTimeShiftPositionCallback");
    super.timeShiftPlay(inputId, recordedProgramUri);
    this.setTimeShiftPositionCallback(
        new TvView.TimeShiftPositionCallback() {
          @Override
          public void onTimeShiftStartPositionChanged(String inputId, long timeMs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onTimeShiftStartPositionChanged, ");
          }

          @Override
          public void onTimeShiftCurrentPositionChanged(String inputId, long timeMs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onTimeShiftCurrentPositionChanged, " + timeMs);
            mTimeShiftCurrentPositionMs = timeMs;
          }
        });
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (false == mIsHandleEvent) {
      return super.onTouchEvent(event);
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onTouchEvent, " + event);

    switch (event.getAction()) {
      case MotionEvent.ACTION_MOVE:
        break;

      case MotionEvent.ACTION_DOWN:
        if (mLocalInputId != null) {
          try {
            TvInputManager mTvInputManager =
                (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);

            List<TvInputInfo> inputs = mTvInputManager.getTvInputList();
            for (TvInputInfo input : inputs) {
              if (input.getId().equals(mLocalInputId) && input.createSettingsIntent() != null) {
                Intent intent = input.createSettingsIntent();
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "dispatchGenericMotionEvent, intent" + intent);

                mContext.startActivity(intent);
              } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(
                    TAG,
                    "dispatchGenericMotionEvent, input.getId(): "
                        + input.getId()
                        + ", mLocalInputId: "
                        + mLocalInputId);
              }
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        break;

      case MotionEvent.ACTION_UP:
        break;

      default:
        break;
    }

    return super.onTouchEvent(event);
  }

  @Override
  public void setCaptionEnabled(boolean enabled) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setCaptionEnabled:" + enabled);
    mCaptionEnabled = enabled ? CAPTION_ENABLED : CAPTION_DISABLED;
    super.setCaptionEnabled(enabled);
  }

  public boolean isCaptionEnable() {
    return CAPTION_DISABLED != mCaptionEnabled;
  }
  /**
   * Sets the TimeShiftListener
   *
   * @param listener The instance of {@link TimeShiftListener}.
   */
  public void setTimeShiftListener(TimeShiftListener listener) {
    mTimeShiftListener = listener;
  }

  private void setTimeShiftAvailable(boolean isTimeShiftAvailable) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.i(
        TAG,
        "setTimeShiftAvailable mTimeShiftAvailable: "
            + mTimeShiftAvailable
            + ", isTimeShiftAvailable: "
            + isTimeShiftAvailable);
    if (mTimeShiftAvailable == isTimeShiftAvailable) {
      return;
    }
    mTimeShiftAvailable = isTimeShiftAvailable;
    if (isTimeShiftAvailable) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "setTimeShiftAvailable isTimeShiftAvailable setTimeShiftPositionCallback()");
      this.setTimeShiftPositionCallback(
          new TvView.TimeShiftPositionCallback() {
            @Override
            public void onTimeShiftStartPositionChanged(String inputId, long timeMs) {
              if (mTimeShiftListener != null && mLocalInputId.equals(inputId)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTimeShiftStartPositionChanged timeMs: " + timeMs);
                mTimeShiftListener.onRecordStartTimeChanged(timeMs);
              }
            }

            @Override
            public void onTimeShiftCurrentPositionChanged(String inputId, long timeMs) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTimeShiftCurrentPositionChanged timeMs: " + timeMs);
              mTimeShiftCurrentPositionMs = timeMs;
            }
          });
    } else {
      StateDvrPlayback stateDvrPlayback = StateDvrPlayback.getInstance();
      if (stateDvrPlayback == null || (stateDvrPlayback != null && !stateDvrPlayback.isRunning())) {
        this.setTimeShiftPositionCallback(null);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(
            TAG, "setTimeShiftAvailable setTimeShiftPositionCallback(null)== timeshift available");
      }
    }
    if (mTimeShiftListener != null) {
      mTimeShiftListener.onAvailabilityChanged();
    }
    if(DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog()!=null
            &&DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().isShowing()
            &&DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().getDvrEventHandler()!=null){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"stop timeshift success");
      Message args =  DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().getDvrEventHandler().obtainMessage();
      args.what = DvrConstant.MSG_CALLBACK_TIMESHIFT_STATE;
      DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().getDvrEventHandler().sendMessage(args);
    }
  }

  /** Returns if the time shift is available for the current channel. */
  public boolean isTimeShiftAvailable() {
    return mTimeShiftAvailable;
  }

  /**
   * Returns the current time-shift state. It returns one of {@link #TIME_SHIFT_STATE_NONE}, {@link
   * #TIME_SHIFT_STATE_PLAY}, {@link #TIME_SHIFT_STATE_PAUSE}, {@link #TIME_SHIFT_STATE_REWIND},
   * {@link #TIME_SHIFT_STATE_FAST_FORWARD} or {@link #TIME_SHIFT_STATE_PAUSE}.
   */
  @TimeShiftState
  public int getTimeShiftState() {
    return mTimeShiftState;
  }

  /** Plays the media, if the current input supports time-shifting. */
  public void timeshiftPlayEx() {
    if (!isTimeShiftAvailable()) {
      throw new IllegalStateException("Time-shift is not supported for the current channel");
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshiftPlayEx mTimeShiftState=" + mTimeShiftState);
    if (mTimeShiftState == TIME_SHIFT_STATE_PLAY) {
      return;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeShiftResume");
    this.timeShiftResume();
  }

  /** Pauses the media, if the current input supports time-shifting. */
  public void timeshiftPauseEx() {
    if (!isTimeShiftAvailable()) {
      throw new IllegalStateException("Time-shift is not supported for the current channel");
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshiftPauseEx mTimeShiftState=" + mTimeShiftState);
    if (mTimeShiftState == TIME_SHIFT_STATE_PAUSE) {
      return;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeShiftPause");
    this.timeShiftPause();
  }

  /**
   * Rewinds the media with the given speed, if the current input supports time-shifting.
   *
   * @param speed The speed to rewind the media. e.g. 2 for 2x, 3 for 3x and 4 for 4x.
   */
  public void timeshiftRewind(int speed) {
    if (!isTimeShiftAvailable()) {
      throw new IllegalStateException("Time-shift is not supported for the current channel");
    }
    if (speed <= 0) {
      throw new IllegalArgumentException("The speed should be a positive integer.");
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshiftRewind");
    mTimeShiftState = TIME_SHIFT_STATE_REWIND;
    PlaybackParams params = new PlaybackParams();
    params.setSpeed(speed * -1);
    this.timeShiftSetPlaybackParams(params);
  }

  /**
   * Fast-forwards the media with the given speed, if the current input supports time-shifting.
   *
   * @param speed The speed to forward the media. e.g. 2 for 2x, 3 for 3x and 4 for 4x.
   */
  public void timeshiftFastForward(int speed) {
    if (!isTimeShiftAvailable()) {
      throw new IllegalStateException("Time-shift is not supported for the current channel");
    }
    if (speed <= 0) {
      throw new IllegalArgumentException("The speed should be a positive integer.");
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshiftFastForward");
    mTimeShiftState = TIME_SHIFT_STATE_FAST_FORWARD;
    PlaybackParams params = new PlaybackParams();
    params.setSpeed(speed);
    this.timeShiftSetPlaybackParams(params);
  }

  /**
   * Seek to the given time position.
   *
   * @param timeMs The time in milliseconds to seek to.
   */
  public void timeshiftSeekTo(long timeMs) {
    if (!isTimeShiftAvailable()) {
      throw new IllegalStateException("Time-shift is not supported for the current channel");
    }
    this.timeShiftSeekTo(timeMs);
  }

  /** Returns the current playback position in milliseconds. */
  public long timeshiftGetCurrentPositionMs() {
    if (DEBUG) {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          TAG,
          "timeshiftGetCurrentPositionMs: current position ="
              + format.format(mTimeShiftCurrentPositionMs));
    }
    return mTimeShiftCurrentPositionMs;
  }

  /** Used to receive the time-shift events. */
  public abstract static class TimeShiftListener {
    /**
     * Called when the availability of the time-shift for the current channel has been changed. It
     * should be guaranteed that this is called only when the availability is really changed.
     */
    public abstract void onAvailabilityChanged();

    /** Called when the record start time has been changed.. */
    public abstract void onRecordStartTimeChanged(long recordStartTimeMs);

    /** Called when the speech time changed.. */
    public abstract void onSpeechChange(float speech);

    /** Called when the play status changed */
    public abstract void onPlayStatusChanged(int status);

    /** Called when the channel changed.. */
    public abstract void onChannelChanged(String inputId, Uri channelUri);

    /** Called when the timeshift record start or not. */
    public abstract void onTimeshiftRecordStart(boolean isStarted);

    /** Called when the timeshift succeed. */
    public abstract void onTimeshiftSucceed();

    /** Called when the timeshift error. */
    public abstract void onTimeshiftError(int errorStatus);
  }

  /** A listener which receives the notification when the screen is blocked/unblocked. */
  public abstract static class OnScreenBlockingChangedListener {
    /** Called when the screen is blocked/unblocked. */
    public abstract void onScreenBlockingChanged(boolean blocked);
  }

  @Override
  public void onChannelChanged(String inputId, Uri channelUri) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "onChannelChanged, [" + mUrl + "]" + "[" + channelUri + "]");

    if (channelUri != null
        && mUrl != null
        && (!mTimeShiftAvailable)
        && 0 != channelUri.compareTo(mUrl)) {
      setTimeShiftAvailable(false);
    }

    if (mTimeShiftListener != null) {
      mTimeShiftListener.onChannelChanged(inputId, channelUri);
    }
  }

  @Override
  public void onTimeShiftStatusChanged(String inputId, int status) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.w(
        TAG,
        "onTimeShiftStatusChanged timeshiftAvailable: "
            + (status == TvInputManager.TIME_SHIFT_STATUS_AVAILABLE));
    setTimeShiftAvailable(status == TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
  }

  @Override
  public void onEvent(String inputId, String eventType, Bundle eventArgs) {
    if (mTimeShiftListener == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent--> mTimeShiftListener is null.");
      return;
    }
      com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG,"eventType== "+eventType);
    if ("session_event_timeshift_speedupdate".equals(eventType)) {
      float speed = eventArgs.getFloat("SpeedUpdate");
      if (mTimeShiftListener != null) {
        mTimeShiftListener.onSpeechChange(speed);
      }
    } else if ("session_event_timeshift_playbackstatusupdate".equals(eventType)) {
      int status = eventArgs.getInt("PlaybackStatusUpdate");
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent--> timeshift playback status: " + status);
      mTimeShiftListener.onPlayStatusChanged(status);
    } else if ("TimeshiftRecordStarted".equals(eventType)) {
      mTimeShiftListener.onTimeshiftRecordStart(true);
    } else if ("TimeshiftRecordNotStarted".equals(eventType)) {
      mTimeShiftListener.onTimeshiftRecordStart(false);
    } else if (eventType.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_TIMESHIFT_SUCCEED)) {
      mTimeShiftListener.onTimeshiftSucceed();
    } else if (eventType.equals(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_TIMESHIFT_ERROR)) {
      int errorStatus = eventArgs.getInt(MtkTvTISMsgBase.MTK_TIS_SESSION_EVENT_TIMESHIFT_ERROR_KEY);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent--> timeshift  error: " + errorStatus);
      mTimeShiftListener.onTimeshiftError(errorStatus);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    Rect rect = new Rect();

    super.onLayout(changed, left, top, right, bottom);

    getGlobalVisibleRect(rect);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
        TAG,
        "onLayout,"
            + left
            + ","
            + top
            + ","
            + right
            + ","
            + bottom
            + ","
            + changed
            + ",getGlobalVisibleRect "
            + rect.left
            + ","
            + rect.top
            + ","
            + rect.right
            + ","
            + rect.bottom);

    TvInputCallbackMgr.getInstance(mContext)
        .handleLayoutChanged(rect.right - rect.left, rect.bottom - rect.top);
  }

  @Override
  public void unblockContent(TvContentRating unblockedRating) {
    if (unblockedRating != null) {
      super.unblockContent(unblockedRating);
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "unblockContent = " + unblockedRating);
    // unblock
    synchronized (TvSurfaceView.class) {
      mBlockContent = BLOCK_STATE_UNLOCK;
      if (mBlockView != null) {
        mBlockView.setVisibility(View.GONE, TvBlockView.BLOCK_3RD_CHANNEL_INPUT_BLOCK);
        if (!mBlockView.isBlock()) {
          this.setStreamVolume(1.0f);
        }
      }
    }
  }

  public void blockContent() {
    synchronized (TvSurfaceView.class) {
      mBlockContent = BLOCK_STATE_LOCK;
      if (mBlockView != null) {
        mBlockView.setVisibility(View.VISIBLE, TvBlockView.BLOCK_3RD_CHANNEL_INPUT_BLOCK);
        this.setStreamVolume(0.0f);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "blockContent = " + mBlockContent + mBlockView);
    }
  }

  public boolean isContentBlock(boolean is3rdChannel) {
    if (is3rdChannel) {
      com.mediatek.wwtv.tvcenter.nav.input.AbstractInput input =
          com.mediatek.wwtv.tvcenter.nav.input.InputUtil.getInputByType(
              com.mediatek.wwtv.tvcenter.nav.input.AbstractInput.TYPE_TV);

      if (input == null) {
        input =
            com.mediatek.wwtv.tvcenter.nav.input.InputUtil.getInputByType(
                com.mediatek.wwtv.tvcenter.nav.input.AbstractInput.TYPE_DTV);
        if (input == null) {
          Log.d(TAG, "input list wrong!");
          return false;
        }
      }

      if (mBlockContent != BLOCK_STATE_UNLOCK && input.isBlock()) {
        return true;
      }
    }

    return false;
  }

  private boolean checkBlock(boolean is3rdChannel) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkBlock = " + is3rdChannel + mBlockContent);
    if (is3rdChannel) {
      TVAsyncExecutor.getInstance()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  if (isContentBlock(true)) {
                    ComponentStatusListener.getInstance()
                        .updateStatus(ComponentStatusListener.NAV_CONTENT_BLOCKED, 0);
                  }
                }
              });
    } else {
      // unblock
      synchronized (TvSurfaceView.class) {
        mBlockContent = BLOCK_STATE_UNLOCK;
        if (mBlockView != null) {
          mBlockView.setVisibility(View.GONE, TvBlockView.BLOCK_3RD_CHANNEL_INPUT_BLOCK);
          /*if(!mBlockView.isBlock())
          this.setStreamVolume(1.0f);*/
        }
      }
    }

    return mBlockContent == BLOCK_STATE_LOCK;
  }

  public Uri getCurrentChannelUrl() {
    return mUrl;
  }

  public void setCurrentChannelUrl(Uri uri) {
    mUrl = uri;
  }

  public long getCurrentChannelId() {
    long id = -1;
    //        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelId,mUrl " + mUrl);
    /*
            String[] projection = {
                TvContract.Channels._ID,
                TvContract.Channels.COLUMN_DISPLAY_NUMBER,
            };

            if (mUrl == null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "getCurrentChannelId, mUrl");
                return -1;
            }

            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(mUrl, projection, null, null, null);
            if (cursor == null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "getCurrentChannelId, cursor," + mUrl);
                return -1;
            }

            if (cursor.getCount() < 1) {
                cursor.close();
                com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "getCurrentChannelId, cursor getCount");
                return -1;
            }

            cursor.moveToFirst();

            long id = cursor.getInt(0);

            cursor.close();
    */
    if (mUrl != null) {
      id = ContentUris.parseId(mUrl);
    }

    return id;
  }

  // add BEGIN by jg_jianwang for DTV01570224
  private void checkBlockFor3rd() {
    synchronized (TvSurfaceView.class) {
      if (mBlockView != null) {
        mBlockView.post(
            () -> {
              checkBlock(CommonIntegration.getInstance().is3rdTVSource());
            });
      }
    }
  }

  @FunctionalInterface
  public interface BlockChecker {
    void check();
  }
  // add END by jg_jianwang for DTV01570224

  @Override
  public List<TvTrackInfo> getTracks(int type) {
    List<TvTrackInfo> tracks = super.getTracks(type);
    if (tracks == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "not tracks, generate a empty tracks.");
      tracks = new ArrayList<>();
    }
    return tracks;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mTIFChannelManager.setBlockCheck(null);
    reset();
  }
}
