package com.mediatek.wwtv.tvcenter.nav.input;

import java.util.List;

import android.content.Context;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.TvInputInfo;
import android.text.TextUtils;


import com.mediatek.twoworlds.tv.MtkTvInputSourceBase.InputSourceRecord;

public final class HdmiInput extends AbstractInput {
    private static final String TAG = ISource.GTAG + "HdmiInput";

    private int portId;// 1 ~ 4

    private int hdmiDeviceId = 0;

    public int getPortId() {
      return portId;
    }

    public HdmiInput(TvInputInfo tvInputInfo) {
        super(tvInputInfo, ISource.TYPE_HDMI);
    }

    @Override
    protected void preInit(TvInputInfo tvInputInfo, int type) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TvInputInfo." + tvInputInfo.getHdmiDeviceInfo());
        if (tvInputInfo.getHdmiDeviceInfo() != null) {
            hdmiDeviceId = tvInputInfo.getHdmiDeviceInfo().getPhysicalAddress() +
                    tvInputInfo.getHdmiDeviceInfo().getId();
            String parentIdString = tvInputInfo.getParentId();
            for (InputSourceRecord record : mHardwareList) {
                if(parentIdString.contains("/HW" + record.getId())) {
                    portId = record.getInternalIdx() + 1;
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TvInputInfo." + portId);
        }
        else {
            portId = -1;
        }
    }

    protected boolean isCurrentHardwareInfo(InputSourceRecord record) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, portId+" record.getId():" + record.getId()+
                " record.getInternalIdx():"+record.getInternalIdx()+
                " record.getInputType():"+record.getInputType());
        if(portId < 0) {
            if(mTvInputInfo!=null&&mTvInputInfo.getId().contains("/HW" + record.getId())) {
                portId = record.getInternalIdx() + 1;
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return portId == record.getInternalIdx() + 1;
        }
    }

    @Override
    protected int getHardwareId(InputSourceRecord record) {
      return (record.getId() << 16) | hdmiDeviceId;
    }

    public boolean isCEC() {
      return isHDMI() && mTvInputInfo != null && mTvInputInfo.getHdmiDeviceInfo() != null;
    }

    public String getParentHDMISourceName(Context context) {
      String result = "";
      if(isCEC()) {
        TvInputInfo parentInfo = InputUtil.getTvInputManager().
            getTvInputInfo(mTvInputInfo.getParentId());
          if (parentInfo != null) {
              result = String.valueOf(parentInfo.loadCustomLabel(context));
              if(TextUtils.isEmpty(result) || TextUtils.equals(result, "null")) {
                  result = String.valueOf(parentInfo.loadLabel(context));
              }
          }

      }
      return result;
    }

    @Override
    public String getSourceName(Context context) {
        if (mTvInputInfo != null) {
            HdmiDeviceInfo mHdmiDeviceInfo = mTvInputInfo.getHdmiDeviceInfo();
            String name = "";
            if(mHdmiDeviceInfo != null) {
              name = mHdmiDeviceInfo.getDisplayName();
            }
            if(TextUtils.isEmpty(name)) {
              name = String.valueOf(mTvInputInfo.loadLabel(context));
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadLabel()=" + name);
            }
            return name;
        }

        return "WEI";
    }

    @Override
    public boolean isHidden(Context context) {
        if (mTvInputInfo != null) {
            return mTvInputInfo.isHidden(context) || isHiddenForParent(context);
        }

        return false;
    }

    private boolean isHiddenForParent(Context context) {
        if (mTvInputInfo != null) {
            String parentId = mTvInputInfo.getParentId();
            if(!TextUtils.isEmpty(parentId) &&
                    !TextUtils.equals(parentId, "null") && InputUtil.getTvInputManager() != null) {
                List<TvInputInfo> inputList = InputUtil.getTvInputManager().getTvInputList();
                for (TvInputInfo tvInputInfo : inputList) {
                    if(tvInputInfo.getType() == TvInputInfo.TYPE_HDMI &&
                            TextUtils.equals(parentId, tvInputInfo.getId())) {
                        return tvInputInfo.isHidden(context);
                    }
                }
            }
        }
        return false;
    }

    //public void updateTvInputInfo(TvInputInfo tvInputInfo) {
    //    init(tvInputInfo, ISource.TYPE_HDMI);
    //}

    @Override
    public String toString(Context context) {
        return "HDMI Id:" + getHardwareId() + ", portId=" + portId +
            ",TvInputInfo=" + getTvInputInfo()+
            ", State=" + getState() + ", isHidden=" + isHidden(context) +
            ", CustomSourceName=" + getCustomSourceName(context) +
            ", SourceName=" + getSourceName(context) +
            ", isBlock=" + isBlock();
    }
}
