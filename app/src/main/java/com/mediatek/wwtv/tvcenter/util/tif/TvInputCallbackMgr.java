package com.mediatek.wwtv.tvcenter.util.tif;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.rxbus.MainActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.nav.input.AtvInput;
import com.mediatek.wwtv.tvcenter.nav.input.DtvInput;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.OnLoadingListener;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.media.tv.TvView.TvInputCallback;

import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView.TvSurfaceLifeCycle;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
//add by xun
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.ist.systemuilib.tvapp.TvToSystemManager;
//end xun

public class TvInputCallbackMgr {
    private static final String TAG = "TvInputCallbackMgr";
    private final Context mContext;
    private final SundryImplement mSundryImplement;

    public static TvInputCallbackMgr mInstance;
    private TimeshiftCallback timeshiftCallback;
    private DvrCallback dvrCallback;
    private SubtitleCallback subtitleCallback;
    private boolean mIsViewSizeChanged = false;
    private boolean mIsLayoutChanged = true;
    private OnLoadingListener mOnLoadingListener;
    private TvSurfaceLifeCycle mTvSurfaceLifeCycle;
    private SoundTracksCallback soundTrackCallback;
    //add by xun
    private Handler mHandler = new Handler();
    private TvToSystemManager mTvToSystemManager;
    private BannerImplement mBannerImplement;
    private SourceInfoRunnable mSourceInfoRunnable = new SourceInfoRunnable();

    public TvToSystemManager newTvToSystemManager() {
        if(mTvToSystemManager == null){
            mTvToSystemManager = TvToSystemManager.getInstance();
        }
        return mTvToSystemManager;
    }

    public class SourceInfoRunnable implements Runnable {
        private String mInputId = "null";
        private int mType = -1;

        public SourceInfoRunnable() {

        }

        public void setSourceType(String inputId, int type) {
            this.mInputId = inputId;
            this.mType = type;
        }

        @Override
        public void run() {
            String resolution = mBannerImplement.getInputResolution();
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("resolution", resolution);
            msg.what = 0;
            msg.setData(b);
            mTvToSystemManager.sourceInfoViewMsg(mInputId, mType, msg);
        }
    }
    //end xun

    public interface TimeshiftCallback {
        void onChannelChanged(String inputId, Uri channelUri);

        void onTimeShiftStatusChanged(String inputId, int status);

        void onEvent(String inputId, String eventType, Bundle eventArgs);
    }

    public interface SubtitleCallback {
        void onTracksChanged(String inputId, List<TvTrackInfo> tracks);

        void onTrackSelected(String inputId, String trackId);
    }
    
    public interface SoundTracksCallback {
        void onTracksChanged(String inputId, List<TvTrackInfo> tracks);
    }

public interface DvrCallback {

        void onTimeShiftStatusChanged(String inputId, int status);

        void onEvent(String inputId, String eventType, Bundle eventArgs);

        void onTracksChanged(String inputId, List<TvTrackInfo> tracks);

        void onTrackSelected(String inputId, int type, String trackId);
    }

    private final TvView.TvInputCallback mTvInputCallback = new TvView.TvInputCallback (){
         /**
         * This is invoked when an error occurred while establishing a connection to the underlying
         * TV input.
         *
         * @param inputId The ID of the TV input bound to this view.
         */
        public void onConnectionFailed(String inputId) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onConnectionFailed inputId>>>" + inputId);
            TvSingletons.getSingletons().getInputSourceManager().retryLoadSourceListAfterStartSession();
        }

        /**
         * This is invoked when the existing connection to the underlying TV input is lost.
         *
         * @param inputId The ID of the TV input bound to this view.
         */
        public void onDisconnected(String inputId) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDisconnected inputId>>>" + inputId);
        }

        /**
         * This is invoked when the view is tuned to a specific channel and starts decoding video
         * stream from there. It is also called later when the video size is changed.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param width The width of the video.
         * @param height The height of the video.
         */
        public void onVideoSizeChanged(String inputId, int width, int height) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onVideoSizeChanged inputId>>>" + inputId + ">>" + width + ">>" + height);
        }

        /**
         * This is invoked when the channel of this TvView is changed by the underlying TV input
         * with out any {@link TvView#tune(String, Uri)} request.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param channelUri The URI of a channel.
         */
        public void onChannelRetuned(String inputId, Uri channelUri) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onChannelRetuned inputId>>>" + inputId + ">>" + channelUri.toString());

            if (timeshiftCallback != null) {
                timeshiftCallback.onChannelChanged(inputId, channelUri);
            }
            if (TurnkeyUiMainActivity.getInstance() != null && TurnkeyUiMainActivity.getInstance().getTvView() != null){
                TurnkeyUiMainActivity.getInstance().getTvView().setCurrentChannelUrl(channelUri);
            }
        }

        /**
         * This is called when the track information has been changed.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param tracks A list which includes track information.
         */
        public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
            List<TvTrackInfo> tvTrackInfos = new ArrayList<TvTrackInfo>();
            tvTrackInfos = tracks;
            for (TvTrackInfo track : tracks) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged track>>>" + track.getType()+"---"+track.getLanguage());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"type = "+track.getType()+"TYPE_AUDIO = "+TvTrackInfo.TYPE_AUDIO);
                if (TvTrackInfo.TYPE_SUBTITLE == track.getType()) {
                    if (subtitleCallback != null) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged subtitle>>>" + track.getType());
                        subtitleCallback.onTracksChanged(inputId, tracks);
                    }
                }else if(TvTrackInfo.TYPE_AUDIO == track.getType()){
                    if(soundTrackCallback != null){
                        soundTrackCallback.onTracksChanged(inputId, tracks);
                    }
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTracksChanged inputId>>>" + inputId + ">>" + tvTrackInfos.size());
            if(dvrCallback!=null){
                 dvrCallback.onTracksChanged(inputId, tvTrackInfos);
                }
            if(!tvTrackInfos.isEmpty() && TvTrackInfo.TYPE_AUDIO == tvTrackInfos.get(0).getType()){
                mSundryImplement.setmMtsAudioTracks(tvTrackInfos);
            }
            /*if (!tvTrackInfos.isEmpty()
                    && TvTrackInfo.TYPE_SUBTITLE == tvTrackInfos.get(0).getType()) {
                if (subtitleCallback != null) {
                    subtitleCallback.onTracksChanged(inputId, tracks);
                }
            }*/
        }

        /**
         * This is called when there is a change on the selected tracks.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param type The type of the track selected. The type can be
         *            {@link TvTrackInfo#TYPE_AUDIO}, {@link TvTrackInfo#TYPE_VIDEO} or
         *            {@link TvTrackInfo#TYPE_SUBTITLE}.
         * @param trackId The ID of the track selected.
         */
        public void onTrackSelected(String inputId, int type, String trackId) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTrackSelected inputId>>>" + inputId + ">>" + type + ">>" + trackId);
            //add by xun
            if(newTvToSystemManager() != null) {
                mHandler.removeCallbacks(mSourceInfoRunnable);
                String resolution = mBannerImplement.getInputResolution();
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("resolution", resolution);
                msg.what = 1;
                msg.setData(b);
                mTvToSystemManager.sourceInfoViewMsg(inputId, type, msg);
            }
            //end xun

            if (subtitleCallback != null && TvTrackInfo.TYPE_SUBTITLE == type) {
                subtitleCallback.onTrackSelected(inputId, trackId);
            }
            if(dvrCallback!=null){
                dvrCallback.onTrackSelected(inputId, type, trackId);
                }
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)){
                if(TvTrackInfo.TYPE_AUDIO == type){
                    if (ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_SUNDRY) != null) {
                        ((SundryShowTextView) ComponentsManager.getInstance()
                                .getComponentById(NavBasic.NAV_COMP_ID_SUNDRY))
                                .updateTrackChanged(type, trackId);
                    }
                    CommonIntegration.getInstance().setAudioFMTUpdate(true);
                    if(!TurnkeyUiMainActivity.isUKCountry){
                        BannerView bannerView =
                            (BannerView)
                                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                        if (bannerView != null) {
                            bannerView.updateBasicBarAudio();
                            bannerView.refreshAfterAudioFMTUpdate();
                        }
                    }
                }
                else if(TvTrackInfo.TYPE_VIDEO == type){
                    //For CTS verify only
                    handleVideoSizeChanged(inputId, trackId);
                }
            }
        }

        /**
         * This is called when the video is available, so the TV input starts the playback.
         *
         * @param inputId The ID of the TV input bound to this view.
         */
        public void onVideoAvailable(String inputId) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onVideoAvailable inputId>>>" + inputId);
            int current3rdMId = SaveValue.getInstance(mContext).readValue(TIFFunctionUtil.current3rdMId, -1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"current3rdId:"+current3rdMId);
            if(mOnLoadingListener != null) {
                mOnLoadingListener.onHideLoading();
            }
            if(mIsViewSizeChanged) {
                //should reset tvview size
                resetViewSize();
            }
            //add by xun
            if(newTvToSystemManager() != null) {
                mTvToSystemManager.signalAvailable(inputId);

                mSourceInfoRunnable.setSourceType(inputId, 0);
                mHandler.removeCallbacks(mSourceInfoRunnable);
                mHandler.postDelayed(mSourceInfoRunnable, 1000);
            }
            //end xun
        }

        private boolean isSpeicalSource(String inputId) {
          if(InputUtil.getTvInputManager() != null) {
            TvInputInfo tv = InputUtil.getTvInputManager().getTvInputInfo(inputId);
            return tv != null &&
                (tv.getType() == TvInputInfo.TYPE_COMPONENT ||
                tv.getType() == TvInputInfo.TYPE_COMPOSITE ||
                tv.getType() == TvInputInfo.TYPE_VGA);
          }
          return false;
        }

        private boolean isNetworkChannel(String inputId) {
          if(InputUtil.getTvInputManager() != null) {
            TvInputInfo tv = InputUtil.getTvInputManager().getTvInputInfo(inputId);
            if(tv != null && tv.getType() == TvInputInfo.TYPE_TUNER &&
                !DtvInput.DEFAULT_ID.equals(inputId)) {
              return true;
            }
          }
          return false;
        }

        /**
         * This is called when the video is not available, so the TV input stops the playback.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param reason The reason why the TV input stopped the playback:
         * <ul>
         * <li>{@link TvInputManager#VIDEO_UNAVAILABLE_REASON_UNKNOWN}
         * <li>{@link TvInputManager#VIDEO_UNAVAILABLE_REASON_TUNING}
         * <li>{@link TvInputManager#VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL}
         * <li>{@link TvInputManager#VIDEO_UNAVAILABLE_REASON_BUFFERING}
         * </ul>
         */
        public void onVideoUnavailable(String inputId, int reason) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onVideoUnavailable inputId>>>" + inputId + ">>" + reason);
            //add by xun
            if(newTvToSystemManager() != null) {
                mTvToSystemManager.signalUnAvailable(inputId, reason);
            }
            //end xun
            int current3rdMId = SaveValue.getInstance(mContext).readValue(TIFFunctionUtil.current3rdMId, -1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"current3rdId:"+current3rdMId);
            if(mOnLoadingListener != null) {
              if(InputUtil.isTunerTypeByInputId(inputId) && !AtvInput.DEFAULT_ID.equals(inputId) && current3rdMId != -1) {
                mOnLoadingListener.onShowLoading();
              } else {
                mOnLoadingListener.onHideLoading();
              }
            }
        }

        /**
         * This is called when the current program content turns out to be allowed to watch since
         * its content rating is not blocked by parental controls.
         *
         * @param inputId The ID of the TV input bound to this view.
         */
        public void onContentAllowed(String inputId) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onContentAllowed inputId>>>" + inputId);

            PwdDialog.setContentBlockRating(null);

            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_PWD)) {
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_CONTENT_ALLOWED, 0);
            }
        }

        /**
         * This is called when the current program content turns out to be not allowed to watch
         * since its content rating is blocked by parental controls.
         *
         * @param inputId The ID of the TV input bound to this view.
         * @param rating The content rating of the blocked program.
         */
        public void onContentBlocked(String inputId, TvContentRating rating) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onContentBlocked inputId>>>" + inputId + ">>" + rating.getMainRating());

            PwdDialog.setContentBlockRating(rating);

            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_PWD)) {
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_CONTENT_BLOCKED, 0);
            }
        }

        /**
         * This is invoked when a custom event from the bound TV input is sent to this view.
         *
         * @param eventType The type of the event.
         * @param eventArgs Optional arguments of the event.
         * @hide
         */
        public void onEvent(String inputId, String eventType, Bundle eventArgs) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onEvent inputId>>>" + inputId + ">>" + eventType + ">>" + eventArgs.toString());
            if (timeshiftCallback != null) {
                timeshiftCallback.onEvent(inputId, eventType, eventArgs);
            }

            if (dvrCallback != null) {
                dvrCallback.onEvent(inputId, eventType, eventArgs);
            }

            if(TextUtils.equals("session_event_current resource have been preempted",
                eventType) && mTvSurfaceLifeCycle != null) {
                mTvSurfaceLifeCycle.surfaceDestroyed();
            }
        }

        @Override
        public void onTimeShiftStatusChanged(String inputId, int status) {
            if (timeshiftCallback != null) {
                timeshiftCallback.onTimeShiftStatusChanged(inputId, status);
            }

            if (dvrCallback != null) {
                dvrCallback.onTimeShiftStatusChanged(inputId, status);
            }
        }
    };

    public synchronized static TvInputCallbackMgr getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TvInputCallbackMgr(context.getApplicationContext());
        }
        return mInstance;
    }

    private TvInputCallbackMgr(Context context) {
        mContext = context;
        mSundryImplement = SundryImplement.getInstanceNavSundryImplement(mContext);
        if (context instanceof OnLoadingListener) {
            mOnLoadingListener = (OnLoadingListener) context;
        }
        if(context instanceof TvSurfaceLifeCycle) {
            mTvSurfaceLifeCycle = (TvSurfaceLifeCycle) context;
        }
        RxBus.instance.onFirstEvent(MainActivityDestroyEvent.class)
                .doOnSuccess(it -> free())
                .subscribe();

        //add by xun
        mBannerImplement = new BannerImplement(mContext);
        //end xun
    }

    private static void free(){
        mInstance = null;
    }

    public TvInputCallback getTvInputCallback() {
        return mTvInputCallback;
    }

    public TimeshiftCallback getTimeshiftCallback() {
        return timeshiftCallback;
    }

    public void setTimeshiftCallback(TimeshiftCallback timeshiftCallback) {
        this.timeshiftCallback = timeshiftCallback;
    }

    public DvrCallback getDvrCallback() {
        return dvrCallback;
    }

    public void setDvrCallback(DvrCallback dvrCallback) {
        this.dvrCallback = dvrCallback;
    }

    public void setSubtitleCallback(SubtitleCallback callback) {
        this.subtitleCallback = callback;
    }
    public void setSoundTracksCallback(SoundTracksCallback callback) {
        this.soundTrackCallback = callback;
    }

    public void handleVideoSizeChanged(String inputId, String trackId) {
        TvSurfaceView view = TurnkeyUiMainActivity.getInstance().getTvView();
        List<TvTrackInfo> list = view .getTracks(TvTrackInfo.TYPE_VIDEO);

        if(list == null  || list.isEmpty() ||
            trackId == null || trackId.length() == 0 ||
            false == CommonIntegration.getInstance().is3rdTVSource()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleVideoSizeChanged, " + trackId);
            return ;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleVideoSizeChanged, " + list);

        for(TvTrackInfo info : list) {
            if(trackId.equals(info.getId())) {
                float ratio = info.getVideoPixelAspectRatio();
                int width   = info.getVideoWidth();
                int height  = info.getVideoHeight();
                int outputWidth = width;
                int outputHeight = height;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleVideoSizeChanged, " + ratio + "," + width + "," + height);
                if(Math.abs(ratio - 1.0) < 0.00001) { continue; }
                if(height != 480 &&
                   height != 576 &&
                   height != 720 &&
                   height != 1080&&
                   height != 2160) {
                   outputHeight = (int)(height * ratio);
                }

                if(width != 640 &&
                   width != 768 &&
                   width != 960 &&
                   width != 1280&&
                   width != 1920&&
                   width != 3840) {
                   outputWidth = (int)(width * ratio);
                }

                FrameLayout.LayoutParams mLayoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();

                mLayoutParams.gravity = Gravity.LEFT|Gravity.TOP;
                mLayoutParams.leftMargin = (int) (ScreenConstant.SCREEN_WIDTH - outputWidth) / 2;
                mLayoutParams.topMargin = (int) (ScreenConstant.SCREEN_HEIGHT - outputHeight) / 2;
                mLayoutParams.width = outputWidth;
                mLayoutParams.height = outputHeight;
                view.setLayoutParams(mLayoutParams);
                view.invalidate();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleVideoSizeChanged, " + outputWidth + outputHeight);
                mIsViewSizeChanged = true;
            }
        }
    }

    public void resetViewSize() {
        TvSurfaceView view = TurnkeyUiMainActivity.getInstance().getTvView();
        FrameLayout.LayoutParams mLayoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();

        mIsViewSizeChanged = false;

        mLayoutParams.gravity = Gravity.LEFT|Gravity.TOP;
        mLayoutParams.leftMargin = 0;
        mLayoutParams.topMargin = 0;
        mLayoutParams.width = ScreenConstant.SCREEN_WIDTH;
        mLayoutParams.height = ScreenConstant.SCREEN_HEIGHT;
        view.setLayoutParams(mLayoutParams);
        view.invalidate();

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetViewSize, ");
        mIsViewSizeChanged = false;
    }

    public void handleLayoutChanged(int width, int height) {

        if(width == ScreenConstant.SCREEN_WIDTH &&
           height == ScreenConstant.SCREEN_HEIGHT) {
            if(false == mIsLayoutChanged) {
                ComponentStatusListener.getInstance().
                            updateStatus(ComponentStatusListener.NAV_EXIT_ANDR_PIP, 0);

                mIsLayoutChanged = true;
            }
        }
        else if(width == 480 && height == 270) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mIsLayoutChanged, false");
            mIsLayoutChanged = false;
        }
    }
}
