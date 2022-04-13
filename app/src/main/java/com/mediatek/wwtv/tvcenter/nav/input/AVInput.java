
package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;

import com.mediatek.twoworlds.tv.MtkTvInputSourceBase.InputSourceRecord;


public class AVInput extends AbstractInput {
  private static final String TAG = ISource.GTAG + "AVInput";

  private int portId;

  public int getPortId() {
    return portId;
  }

  public AVInput(TvInputInfo tvInputInfo) {
    super(tvInputInfo, ISource.TYPE_COMPOSITE);
  }

  @Override
  protected void preInit(TvInputInfo tvInputInfo, int type) {
    portId = -1;
    if (tvInputInfo != null) {
      for (InputSourceRecord record : mHardwareList) {
        if (tvInputInfo.getId().contains("/HW" + record.getId())) {
          portId = record.getInternalIdx() + 1;
        }
      }
    }
  }

  protected boolean isCurrentHardwareInfo(InputSourceRecord record) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, portId + " record.getId():" + record.getId() +
        " record.getInternalIdx():" + record.getInternalIdx() +
        " record.getInputType():" + record.getInputType());
    if (portId < 0) {
      if (mTvInputInfo != null && mTvInputInfo.getId().contains("/HW" + record.getId())) {
        portId = record.getInternalIdx() + 1;
        return true;
      } else {
        return false;
      }
    } else {
      return portId == record.getInternalIdx() + 1;
    }
  }

  @Override
  public String getSourceName(Context context) {
    if (mTvInputInfo != null) {
      String name = String.valueOf(mTvInputInfo.loadLabel(context));
      if(InputUtil.isMultiAVInputs) {
        name += " " + portId;
      }
      return name;
    }
    return "Composite";
  }

}
