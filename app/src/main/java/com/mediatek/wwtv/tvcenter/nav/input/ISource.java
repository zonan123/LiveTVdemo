//mtk40707
package com.mediatek.wwtv.tvcenter.nav.input;

import android.net.Uri;
import android.media.tv.TvView;
import android.media.tv.TvInputInfo;
import android.content.Context;

public interface ISource {
    String GTAG = "ISource^";

    int ANDROID_TV_HOME = 30000;

    /**
     * TV input type: the TV input service is a tuner which provides channels.
     */
    int TYPE_TV = TvInputInfo.TYPE_TUNER;// 0

    /**
     * TV input type: the TV input service is a tuner which provides channels, For Analog channels
     */
    int TYPE_ATV = 10000;

    /**
     * TV input type: the TV input service is a tuner which provides channels, For Digital channels
     */
    int TYPE_DTV = 20000;

    int TYPE_AIR = 20001;
    int TYPE_CAB = 20002;
    int TYPE_SAT = 20003;

    /**
     * TV input type: a generic hardware TV input type.
     */
    int TYPE_OTHER = TvInputInfo.TYPE_OTHER;// 1000

    /**
     * TV input type: the TV input service represents a composite port.
     */
    int TYPE_COMPOSITE = TvInputInfo.TYPE_COMPOSITE;// 1001
    /**
     * TV input type: the TV input service represents a SVIDEO port.
     */
    int TYPE_SVIDEO = TvInputInfo.TYPE_SVIDEO;// 1002
    /**
     * TV input type: the TV input service represents a SCART port.
     */
    int TYPE_SCART = TvInputInfo.TYPE_SCART;// 1003
    /**
     * TV input type: the TV input service represents a component port.
     */
    int TYPE_COMPONENT = TvInputInfo.TYPE_COMPONENT;// 1004
    /**
     * TV input type: the TV input service represents a VGA port.
     */
    int TYPE_VGA = TvInputInfo.TYPE_VGA;// 1005
    /**
     * TV input type: the TV input service represents a DVI port.
     */
    int TYPE_DVI = TvInputInfo.TYPE_DVI;// 1006
    /**
     * TV input type: the TV input service is HDMI. (e.g. HDMI 1)
     */
    int TYPE_HDMI = TvInputInfo.TYPE_HDMI;// 1007
    /**
     * TV input type: the TV input service represents a display port.
     */
    int TYPE_DISPLAY_PORT = TvInputInfo.TYPE_DISPLAY_PORT;// 1008

    /**
     * Get hardward id from tvapi
     *
     * @return hardward id
     */
    int getHardwareId();

    /**
     * Get id from tis
     *
     * @return tis id
     */
    String getId();

    /**
     * Get the support info about this source
     *
     * @return true if current platform is supported, others is false.
     */
    int getType();

    /**
     * Get the support info about this source
     *
     * @return true if current platform is supported, others is false.
     */
    boolean isHidden(Context context);

    /**
     * Get the state for a given input state.
     *
     * @return state The input state. Should be one of the followings.
     *         {@link TvInputManager#INPUT_STATE_CONNECTED},
     *         {@link TvInputManager#INPUT_STATE_CONNECTED_STANDBY} and
     *         {@link TvInputManager#INPUT_STATE_DISCONNECTED}.
     */
    int getState();

    /**
     * Get conflict info
     *
     * @return true is conflicted, others is false.
     */
    boolean getConflict(ISource source);

    /**
     * Get source name
     *
     * @return source name
     */
    String getSourceName(Context context);

    /**
     * Get custom source name
     *
     * @return source name
     */
    String getCustomSourceName(Context context);

    /**
     * Tune the source
     *
     * @param tvView a tvview to tune source.
     * @return the result
     */
    int tune(TvView tvView);

    /**
     * Tune the source
     *
     * @param tvView a tvview to tune source.
     * @param mId channel id.
     * @return the result
     */
    int tune(TvView tvView, String sourceId, Uri channelId);

    /**
     * Get TvInputInfo
     *
     * @return TvInputInfo
     */
    TvInputInfo getTvInputInfo();

    /**
     * Get TvInputInfo
     *
     * @param mId channel id.
     * @return TvInputInfo
     */
    TvInputInfo getTvInputInfo(long mId);

    /**
     * update state of TvInputInfo
     *
     */
    void updateState(int state);

    /**
     * check the block status
     *
     */
    boolean isBlock();

    /**
     * check the block status
     *
     */
    boolean isBlockEx();

    /**
     * check the current block status
     *
     */
    boolean isCurrentBlock();

    /**
     * block status
     *
     */
    int block(boolean block);
}
