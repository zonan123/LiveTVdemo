package com.mediatek.wwtv.tvcenter.util;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.tvcenter.R;

/** */
public final class AudioFocusManager {
  public static final String TAG = "MediaFocusManager";

  public static final int AUDIO_NAVIGATOR = 0x00000001;
  public static final int AUDIO_EPG_BARKER = 0x00000002;
  public static final int AUDIO_EWBS = 0x00000004;

  private static AudioFocusManager mAudioFocusManager = null;

  private AudioManager mAudioManager = null;
  private Handler mHandler = null;
  private AudioFocusRequest mRequest = null;
  private Instrumentation mInst = null;

  /** flag to record tv audio status,
   * true means tv audio is stopped; false means tv audio playback.
   * to avoid stop tv aduio repeatly
   */
  private boolean mLoss = true;

  /** flag to record the audio transinent loss,
   * APP could not request audio focus during transinent loss
   */
  private boolean mTransientLossNotify = false;
  private int mBits = 0;

  private AudioTrack mAudioTrack;
  private MediaSession mMediaSession;
  //    private MediaMetadata.Builder mMetadataBuilder;

  public void createAudioTrack() {
    AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
    attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
    attributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
    AudioAttributes attributes = attributesBuilder.build();
    AudioFormat.Builder formatBuilder = new AudioFormat.Builder();
    formatBuilder.setSampleRate(48000);
    formatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
    formatBuilder.setChannelMask(AudioFormat.CHANNEL_OUT_STEREO);
    AudioFormat format = formatBuilder.build();
    try {
      mAudioTrack =
          new AudioTrack(
              attributes,
              format,
              1024,
              AudioTrack.MODE_STREAM,
              AudioManager.AUDIO_SESSION_ID_GENERATE);
      if (mAudioTrack.getState() == mAudioTrack.STATE_INITIALIZED) {
        mAudioTrack.play();
      }
    } catch (Exception e) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "audiotrack init fail");
    }
  }

  public void createMediaSession(Context context) {
    // Create the MediaSession
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "createMediaSession");
    mMediaSession =
        new MediaSession(context, "com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity");

    mMediaSession.setFlags(
        MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActive true");
    mMediaSession.setActive(true);

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setMetadata title");
    MediaMetadata.Builder mMetadataBuilder = new MediaMetadata.Builder();
    mMetadataBuilder.putString(
        MediaMetadata.METADATA_KEY_TITLE, context.getResources().getString(R.string.app_name));
    mMediaSession.setMetadata(mMetadataBuilder.build());

    setMediaPlaybackPlaying();
  }

  public void setMediaPlaybackPlaying() {
    if (mMediaSession != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setPlaybackState 3");
      PlaybackState.Builder mPb = new PlaybackState.Builder();
      mPb.setState(PlaybackState.STATE_PLAYING, 0, 1);
      mMediaSession.setPlaybackState(mPb.build());
    }
  }

  public void releaseAudioTrackAndMediaSession() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "releaseAudioTrackAndMediaSession");
    if (mAudioTrack != null) {
      if (mAudioTrack.getState() == mAudioTrack.STATE_INITIALIZED) {
        mAudioTrack.pause();
      }
      mAudioTrack.flush();
      mAudioTrack.release();
    }
    if (mMediaSession != null && mMediaSession.isActive()) {
      PlaybackState.Builder mPb = new PlaybackState.Builder();
      mPb.setState(PlaybackState.STATE_NONE, 0, 0);
      mMediaSession.setPlaybackState(mPb.build());
      mMediaSession.release();
    }
  }

  AudioAttributes mAttributeMusic =
      new AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build();

  private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener =
      new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
          Log.d(TAG, "onAudioFocusChange, " + focusChange);

          if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mTransientLossNotify = false;

            // 3rd channels, ignore
            if (CommonIntegration.getInstance().is3rdTVSource()
                && DestroyApp.isCurActivityTkuiMainActivity()) {
              Log.d(TAG, "3rd channel playing, ignore");
              return;
            }
            muteTVAudioInternal(AUDIO_NAVIGATOR);

            showAlertDialog(DestroyApp.appContext);
          } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
              || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            mTransientLossNotify = true;

            // 3rd channels, ignore
            if (CommonIntegration.getInstance().is3rdTVSource()
                && DestroyApp.isCurActivityTkuiMainActivity()) {
              Log.d(TAG, "3rd channel playing, ignore");
              return;
            }

            muteTVAudioInternal(AUDIO_NAVIGATOR);
          } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mTransientLossNotify = false;
            unmuteTVAudioInternal(AUDIO_NAVIGATOR);
          }
        }
      };

  public static synchronized AudioFocusManager getInstance(Context context) {
    if (mAudioFocusManager == null) {
      mAudioFocusManager = new AudioFocusManager(context);
    }

    return mAudioFocusManager;
  }

  private AudioFocusManager(Context context) {
    init(context);
  }

  private void init(Context context) {
    synchronized (AudioFocusManager.class) {
      if (mHandler == null) {
        HandlerThread mThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_DEFAULT);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
      }
    }

    mHandler.post(
        () -> {
          synchronized (AudioFocusManager.class) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            mRequest =
                new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAttributeMusic)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mAudioFocusListener, mHandler)
                    .setWillPauseWhenDucked(true)
                    .build();
          }
        });
  }

  public void deinit() {
    synchronized (AudioFocusManager.class) {
      mAudioManager = null;
    }
  }

  public void requestAudioFocus() {
    if (mHandler == null || mTransientLossNotify) {
      Log.d(TAG, "requestAudioFocus, rejected!");
      return;
    }

    mHandler.post(
        () -> {
          synchronized (AudioFocusManager.class) {
            try {
              /* Audio Manager */
              mAudioManager.requestAudioFocus(mRequest);
              Log.d(TAG, "requestAudioFocus, " + mRequest + "," + mBits);
              unmuteTVAudioInternal(AUDIO_NAVIGATOR);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
  }

  public void abandonAudioFocus() {
    if (mHandler == null) {
      return;
    }

    mHandler.post(
        () -> {
          synchronized (AudioFocusManager.class) {
            mTransientLossNotify = false;
            mAudioManager.abandonAudioFocus(mAudioFocusListener, mAttributeMusic);
            SaveValue.saveWorldValue(DestroyApp.appContext, "tune_livechannel", 0, true);
          }
        });
  }

  public void muteTVAudio(int bitFlag) {
    if (mHandler == null) {
      return;
    }

    mHandler.post(
        () -> {
          muteTVAudioInternal(bitFlag);
        });
  }

  public void unmuteTVAudio(int bitFlag) {
    if (mHandler == null) {
      return;
    }

    mHandler.post(
        () -> {
          unmuteTVAudioInternal(bitFlag);
        });
  }

  private void muteTVAudioInternal(int bitFlag) {
    Log.d(TAG, "muteTVAudio, " + bitFlag + "," + mBits + ", mLoss=" + mLoss);
    mBits = mBits | bitFlag;

    if (SaveValue.readWorldIntValue(DestroyApp.appContext, "tune_livechannel", 0) == 1) {
      Log.d(TAG, "tune live channel do not mute");
      return;
    }

    if (!mLoss) {
      MtkTvConfig.getInstance()
          .setAndroidWorldInfoToLinux(
              MtkTvConfigType.ANDROID_WORLD_INFO_AUDIO_FOCUS,
              MtkTvConfigType.ANDROID_WORLD_INFO_AUDIOFOCUS_LOSS);
    }

    mLoss = true;
  }

  private void unmuteTVAudioInternal(int bitFlag) {
    Log.d(TAG, "muteTVAudio, " + bitFlag + "," + mBits + mLoss);
    if(mBits == 0) {
      if(mLoss) {
        mTransientLossNotify = false;
        mLoss = false;
      }
      return ;
    }

    mBits = mBits & (~bitFlag);
    if (mLoss && (mBits == 0)) {
      mTransientLossNotify = false;
      mLoss = false;
      MtkTvConfig.getInstance()
          .setAndroidWorldInfoToLinux(
              MtkTvConfigType.ANDROID_WORLD_INFO_AUDIO_FOCUS,
              MtkTvConfigType.ANDROID_WORLD_INFO_AUDIOFOCUS_GAIN);
    }
  }

  private void showAlertDialog(Context context) {
    if(MarketRegionInfo.REGION_CN != MarketRegionInfo.getCurrentMarketRegion()) {
      Log.e(TAG, "region invalid");
      return ;
    }

    if(!DestroyApp.isCurTaskTKUI()) {
      Log.e(TAG, "tv app already exited.");
      return ;
    }

    Log.i(TAG, "showAlertDialog.");
    AlertDialog dialog =
        new AlertDialog.Builder(context)
            .setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick ok");
                    if(!DestroyApp.isCurTaskTKUI()) {
                      Log.e(TAG, "tv app already exited.");
                      return ;
                    }
                    requestAudioFocus();
                  }
                })
            .setNegativeButton(
                android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "onClick cancel");
                    TVAsyncExecutor.getInstance()
                        .execute(
                            () -> {
                              if(!DestroyApp.isCurTaskTKUI()) {
                                Log.e(TAG, "tv app already exited.");
                                return ;
                              }
                              try {
                                if(mInst == null) {
                                  mInst = new Instrumentation();
                                }
                                mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
                              } catch (Exception e) {
                                Log.d(TAG, e.toString());
                              }
                            });
                  }
                })
            .create();
    dialog.setTitle(android.R.string.dialog_alert_title);
    dialog.setMessage(context.getResources().getString(R.string.audio_focus_lost_warnning_info));
    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    dialog.show();
  }
}
