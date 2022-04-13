package com.mediatek.wwtv.tvcenter.nav.input;

import android.net.Uri;
import android.media.tv.TvView;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvContract;
import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvInputSourceBase.InputDeviceType;

import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;


public class AbstractInput implements ISource, Comparable<AbstractInput> {
    private static final String TAG = ISource.GTAG + "AbstractInput";

    protected static final MtkTvInputSource mTvInputSource = MtkTvInputSource.getInstance();
    private   static final List<ConflictInputInfo> mConflictList = new ArrayList<ConflictInputInfo>();
    private   static final int mSize = mTvInputSource.getInputSourceTotalNumber();
    protected   static final List<MtkTvInputSource.InputSourceRecord> mHardwareList = new
        ArrayList<MtkTvInputSource.InputSourceRecord>();

    protected TvInputInfo mTvInputInfo;
    protected int mHardwareId;
    protected int mInputState = TvInputManager.INPUT_STATE_CONNECTED;

    protected int mType;

    public AbstractInput(TvInputInfo tvInputInfo, int type) {
        preInit(tvInputInfo, type);
        init(tvInputInfo, type);
    }

    //for child class
    protected void preInit(TvInputInfo tvInputInfo, int type) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "preInit");
    }

    protected void init(TvInputInfo tvInputInfo, int type) {
        mTvInputInfo = tvInputInfo;
        mType = type;

        if (mTvInputInfo != null &&
                mTvInputInfo.getType() >= TvInputInfo.TYPE_COMPOSITE &&
                mTvInputInfo.getType() <= TvInputInfo.TYPE_HDMI) {
            mType = mTvInputInfo.getType();
        }

        if(mTvInputInfo != null) {
            mInputState = InputUtil.getTvInputManager().getInputState(mTvInputInfo.getId());
        }

        for (int i = 0, isJump = 0; i < mSize; i++) {
            MtkTvInputSource.InputSourceRecord record;
            if(mHardwareList.size() <= i) {
                record = new MtkTvInputSource.InputSourceRecord();
                mTvInputSource.getInputSourceRecbyidx(i, record);
                mHardwareList.add(record);
            }
            else {
                record = mHardwareList.get(i);
            }
            if(record==null){
            	continue;
            }

            switch (mType) {
                case ISource.TYPE_TV:
                    if (record.getInputType() == InputDeviceType.TV) {
                        mTvInputInfo = null;
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_ATV:
                    if (MtkTvInputSource.INPUT_TYPE_ATV.
                            equalsIgnoreCase(mTvInputSource.getInputSourceNamebySourceid(record.getId()))) {
                        mTvInputInfo = null;
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_DTV:
                case ISource.TYPE_AIR:
                case ISource.TYPE_CAB:
                case ISource.TYPE_SAT:
                    if (MtkTvInputSource.INPUT_TYPE_DTV.
                            equalsIgnoreCase(mTvInputSource.getInputSourceNamebySourceid(record.getId()))) {
                        mTvInputInfo = null;
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_COMPOSITE:
                    if (record.getInputType() == InputDeviceType.COMPOSITE && isCurrentHardwareInfo(record)) {
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_COMPONENT:
                    if (record.getInputType() == InputDeviceType.COMPONENT) {
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_SCART:
                    if (record.getInputType() == InputDeviceType.SCART) {
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_VGA:
                    if (record.getInputType() == InputDeviceType.VGA) {
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                case ISource.TYPE_HDMI:
                    if (record.getInputType() == InputDeviceType.HDMI && isCurrentHardwareInfo(record)) {
                        mHardwareId = getHardwareId(record);
                        isJump = 1;
                    }
                    break;
                default:
                    break;
            }

            if (isJump > 0) {
                break;
            }
        }
    }

    @Override
    public void updateState(int state) {
        mInputState = state;
    }

    protected boolean isCurrentHardwareInfo(MtkTvInputSource.InputSourceRecord record) {
        return false;
    }

    protected int getHardwareId(MtkTvInputSource.InputSourceRecord record) {
        return record.getId() << 16;
    }

    @Override
    public int getHardwareId() {
        return mHardwareId;
    }

    @Override
    public String getId() {
        if(mTvInputInfo != null) {
            return mTvInputInfo.getId();
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getId failed! " + this.toString());
        return "<INVALID>";
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public boolean isHidden(Context context) {
        if (mTvInputInfo != null) {
            return mTvInputInfo.isHidden(context);
        }

        return false;
    }

    @Override
    public int getState() {
        return mInputState;
    }

    @Override
    public boolean getConflict(ISource source) {
        // for PIP/POP
        //1. same source
        if(source.getHardwareId() == getHardwareId()) {
            return !(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) &&
                (getType() == TYPE_TV || getType() == TYPE_DTV || getType() == TYPE_ATV));
        }

        //2. same type(HDMI1,HDMI 2)
        if(source.getType() == getType()) {
            return true;
        }

        //3. check exist list
        for(ConflictInputInfo info : mConflictList) {
            if(info.isMap(source.getHardwareId(),
                getHardwareId())) {
                return info.getConflictInfo();
            }
        }

        //4. create new info
        ConflictInputInfo crnt = new ConflictInputInfo(
            source.getHardwareId(), getHardwareId(),
            mTvInputSource.queryConflict(source.getHardwareId() >> 16, getHardwareId() >> 16));
        mConflictList.add(crnt);

        return crnt.getConflictInfo();
    }

    @Override
    public String getSourceName(Context context) {
        if (mTvInputInfo != null) {
            return String.valueOf(mTvInputInfo.loadLabel(context));
        }

        return "WEI";
    }

    @Override
    public String getCustomSourceName(Context context) {
        if (mTvInputInfo != null) {
            return String.valueOf(mTvInputInfo.loadCustomLabel(context));
        }

        return "WEI";
    }

    public String getSourceNameForUI(Context context) {
        String customLabel = getCustomSourceName(context);
        if(TextUtils.isEmpty(customLabel) ||
                TextUtils.equals(customLabel, "null")) {
            return getSourceName(context);
        } else {
            return customLabel;
        }
    }

    @Override
    public int tune(TvView tvView) {
        if (tvView != null) {
            if (mTvInputInfo != null) {
                tvView.tune(mTvInputInfo.getId(),
                        TvContract.buildChannelUriForPassthroughInput(mTvInputInfo.getId()));
                return 0;
            }
            else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune, mTvInputInfo is null!");
            }
        }
        else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune, tvView is null!");
        }

        return -1;
    }

    /**
     * Tune the source
     *
     * @param tvView a tvview to tune source.
     * @param mId channel id.
     * @return the result
     */
    public int tune(TvView tvView, String sourceId, Uri channelId) {
        if (tvView != null) {
            tvView.tune(sourceId, channelId);
            return 0;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune, tvView is null!");
        return -1;
    }

    /**
     * Get TvInputInfo
     *
     * @return TvInputInfo
     */
    @Override
    public TvInputInfo getTvInputInfo() {
        return mTvInputInfo;
    }

    /**
     * Get TvInputInfo
     *
     * @param mId channel id.
     * @return TvInputInfo
     */
    @Override
    public TvInputInfo getTvInputInfo(long mId) {
        return null;// will implement
    }

    /**
     * check the block status
     *
     */
    @Override
    public boolean isBlock() {
        return mTvInputSource.isBlock(getHardwareId() >> 16);
    }
    /**
     * check the current block status
     *
     */
    @Deprecated
    @Override
    public boolean isBlockEx() {
        return mTvInputSource.isBlockEx(getHardwareId() >> 16);
    }
    /**
     * check the current block status
     *
     */
    @Override
    public boolean isCurrentBlock() {
        if(getType() == TYPE_TV ||
           getType() == TYPE_DTV ||
           getType() == TYPE_ATV) {
            return mTvInputSource.checkIsMenuTvBlock();
        }
        else {
            return mTvInputSource.isBlockEx(getHardwareId() >> 16);
        }
    }

    /**
     * block status
     *
     */
    @Override
    public int block(boolean block) {
        return mTvInputSource.block(getHardwareId() >> 16, block);
    }

    @Override
    public int compareTo(AbstractInput another) {
        return mHardwareId - another.getHardwareId();
    }

    @Override
    public boolean equals(Object o) {
      if(o instanceof AbstractInput) {
        AbstractInput input = (AbstractInput) o;
        return mHardwareId == input.mHardwareId;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return mHardwareId;
    }

    public boolean isNeedAbortTune() {
        boolean result = false;
        /*if(getType() == TYPE_SCART || getType() == TYPE_COMPONENT || getType() == TYPE_COMPOSITE || getType() == TYPE_VGA) {
            result = mInputState != TvInputManager.INPUT_STATE_CONNECTED;
        }*/
        return result;
    }


    public boolean isTV() {
        return getType() == TYPE_TV;
    }

    public boolean isATV() {
        return getType() == TYPE_ATV;
    }

    public boolean isDTV() {
        return getType() == TYPE_DTV || isAIR() || isCAB() || isSAT();
    }

    public boolean isAIR() {
        return getType() == TYPE_AIR;
    }

    public boolean isCAB() {
        return getType() == TYPE_CAB;
    }

    public boolean isSAT() {
        return getType() == TYPE_SAT;
    }

    public boolean isVGA() {
        return getType() == TYPE_VGA;
    }

    public boolean isHDMI() {
        return getType() == TYPE_HDMI;
    }
    public boolean isComponent() {
        return getType() == TYPE_COMPONENT;
    }
    public boolean isComposite() {
        return getType() == TYPE_COMPOSITE;
    }
    public boolean isTVHome() {
      return getType() == ANDROID_TV_HOME;
    }

    public String toString(Context context) {
        return "Source:" + getType() + " Id:" + getHardwareId() +
            ",TvInputInfo=" + getTvInputInfo()+
            ", State=" + getState() + ", isHidden=" + isHidden(context) +
            ", CustomSourceName=" + getCustomSourceName(context) +
            ", SourceName=" + getSourceName(context) +
            ", isBlock=" + isBlock();
    }

    private class ConflictInputInfo {
        protected int mhardwareId1 = -1;
        protected int mhardwareId2 = -1;
        protected boolean mConflict = false;

        public ConflictInputInfo(int id1, int id2, boolean conflict) {
            mhardwareId1 = id1;
            mhardwareId2 = id2;
            mConflict = conflict;
        }

        public boolean isMap(int id1, int id2) {
            return id1 == mhardwareId1 && id2 == mhardwareId2 ||
                   id1 == mhardwareId2 && id2 == mhardwareId1;
        }

        public boolean getConflictInfo() {
            return mConflict;
        }
    }
}
