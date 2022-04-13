package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InputUtil {
    private static final String TAG = ISource.GTAG + "InputUtil";
    private final static List<AbstractInput> mSourceList = new ArrayList<AbstractInput>();

    private static TvInputManager mTvInputManager = null;
    public static boolean isMultiAVInputs;
    
    private InputUtil() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"init InputUtil.");
    }
    public final static boolean F_TUNER_MODE_AS_SOURCE_SUPPORT = DataSeparaterUtil.getInstance().isSupportSeperateDTV();

    public static TvInputManager getTvInputManager() {
        return mTvInputManager;
    }

    public synchronized static void buildSourceList(Context context) {
        AbstractInput absInput;
        if(mTvInputManager == null) {
            try {
                mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (Iterator<AbstractInput> iterator = mSourceList.iterator(); iterator.hasNext();) {
          AbstractInput input = (AbstractInput) iterator.next();
          if(input.isHDMI()) {
            iterator.remove();
          }
        }

        absInput = new TvInput();
        if(!absInput.isHidden(context) && getInputByType(absInput.getType()) == null) {
            mSourceList.add(absInput);
        }

        if (F_TUNER_MODE_AS_SOURCE_SUPPORT) {
            absInput = new AntennaInput();
            if(!absInput.isHidden(context) && getInputByType(absInput.getType()) == null) {
                mSourceList.add(absInput);
            }

            AbstractInput antennaInput = getInputByType(absInput.getType());
            if(antennaInput != null && absInput.isHidden(context)) {
                mSourceList.remove(antennaInput);
            }

            absInput = new CableInput();
            if(!absInput.isHidden(context) && getInputByType(absInput.getType()) == null) {
                mSourceList.add(absInput);
            }

            AbstractInput cableInput = getInputByType(absInput.getType());
            if(cableInput != null && absInput.isHidden(context)) {
                mSourceList.remove(cableInput);
            }

            absInput = new SatelliteInput();
            if(!absInput.isHidden(context) && getInputByType(absInput.getType()) == null) {
                mSourceList.add(absInput);
            }

            AbstractInput satelliteInput = getInputByType(absInput.getType());
            if(satelliteInput != null && absInput.isHidden(context)) {
                mSourceList.remove(satelliteInput);
            }


        } else {
            absInput = new DtvInput();
            if(!absInput.isHidden(context) && getInputByType(absInput.getType()) == null) {
                mSourceList.add(absInput);
            }

            AbstractInput dtvInput = getInputByType(absInput.getType());
            if(absInput.isHidden(context) && dtvInput != null) {
                mSourceList.remove(dtvInput);
            }
        }

        absInput = new AtvInput();
        if(!absInput.isHidden(context) && getInputByType(absInput.getType()) == null) {
            mSourceList.add(absInput);
        }
        AbstractInput atvInput = getInputByType(absInput.getType());
        if(absInput.isHidden(context) && atvInput != null) {
            mSourceList.remove(atvInput);
        }

        List<TvInputInfo> tvInputList = mTvInputManager.getTvInputList();
        Collections.sort(tvInputList, new Comparator<TvInputInfo>() {

            @Override
            public int compare(TvInputInfo lhs, TvInputInfo rhs) {
                return lhs.getHdmiDeviceInfo() == null ? -1 : 1;
            }
        });
        Set<String> avInputIds = new HashSet<String>();
        for (TvInputInfo input : tvInputList) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"[TIF] "+input.toString());
            switch(input.getType())
            {
            case TvInputInfo.TYPE_COMPOSITE:
              absInput = null;
              for(AbstractInput abstractInput : mSourceList) {
                if(input.getId().equals(abstractInput.getId())) {
                  absInput = abstractInput;
                  break;
                }
              }

              if(absInput == null) {
                  absInput = new AVInput(input);
                  mSourceList.add(absInput);
              } else {
                  absInput.preInit(input, input.getType());
                  absInput.init(input, input.getType());
              }
              avInputIds.add(input.getId());
              break;
            case TvInputInfo.TYPE_COMPONENT:
            case TvInputInfo.TYPE_SCART:
            case TvInputInfo.TYPE_VGA:
            {
                absInput = getInputByType(input.getType());

                if(absInput == null) {
                    absInput = new AbstractInput(input, input.getType());
                    mSourceList.add(absInput);
                }
                else {
                    absInput.preInit(input, input.getType());
                    absInput.init(input, input.getType());
                }
                break;
            }
            case TvInputInfo.TYPE_HDMI:
            {
                absInput = new HdmiInput(input);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "absInput update 1." +input + input.getParentId());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "absInput update 2." + absInput.toString(context));

                if(getInputById(absInput.getHardwareId()) == null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "absInput update add.");
                    mSourceList.add(absInput);
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "absInput update update.");
                    absInput.preInit(input, input.getType());
                    absInput.init(input, input.getType());
                }
                //some port, if hdmi and cec coExist, remove hdmi
                for (Iterator<AbstractInput> iterator = mSourceList.iterator(); iterator.hasNext();) {
                  AbstractInput it = (AbstractInput) iterator.next();
                  if (it.getType() != TvInputInfo.TYPE_HDMI) {
                    continue;
                  }
                  TvInputInfo info = it.getTvInputInfo();
                  if(input.getHdmiDeviceInfo() != null &&
                      it.mTvInputInfo.getHdmiDeviceInfo() == null &&
                      TextUtils.equals(info.getId(), input.getParentId())) {
                    iterator.remove();
                    break;
                  }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "absInput update 3." + absInput.toString(context));
                break;
            }
            case TvInputInfo.TYPE_TUNER:
            default:
                break;
            }
        }//for
        Collections.sort(mSourceList);

        isMultiAVInputs = avInputIds.size() > 1;

        //debug
        dump(context, mSourceList);
        dump(context, getSourceList(context));
    }

    public static AbstractInput getInput(int id) {
      AbstractInput result = null;
      Iterator<AbstractInput> iterator = mSourceList.iterator();
      while (iterator.hasNext()) {
        AbstractInput input = iterator.next();
        if(id == input.getHardwareId()) {
          result = input;
          break;
        }
      }
      return result;
    }

    public static AbstractInput getInput(Integer id) {
        if(id == null) {
          return null;
        }
        return getInput(id.intValue());
    }

    public static AbstractInput getInput(String id) {
        if(id == null) {
          return null;
        }
        return getInput(Integer.parseInt(id));
    }

    public static AbstractInput getInputByType(int type) {
        AbstractInput result = null;
        Iterator<AbstractInput> iterator = mSourceList.iterator();
        while (iterator.hasNext()) {
            AbstractInput input = iterator.next();
            if(F_TUNER_MODE_AS_SOURCE_SUPPORT && type == AbstractInput.TYPE_DTV) {
                int tunerType = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getInputByType tunerType=" + tunerType);
                switch (tunerType) {
                    case 0:
                        type = AbstractInput.TYPE_AIR;
                        break;
                    case 1:
                        type = AbstractInput.TYPE_CAB;
                        break;
                    default:
                        type = AbstractInput.TYPE_SAT;
                        break;
                }
            }
            if (type == input.getType()) {
                result = input;
                break;
            }
        }
        return result;
    }

    public static AbstractInput getInputById(int hardwardId) {
        return getInput(hardwardId);
    }

    public static AbstractInput getInputBySourceName(Context context, String name) {
        AbstractInput result = null;
        Iterator<AbstractInput> iterator = mSourceList.iterator();
        while (iterator.hasNext()) {
            AbstractInput input = iterator.next();
            if(TextUtils.equals(name, input.getSourceNameForUI(context))) {
                result = input;
                break;
            }
        }
        return result;
    }

    /**
     * just for InputsPanelActivity
     */
    public static List<AbstractInput> getEnableSourceList(Context context) {
      List<AbstractInput> result = new ArrayList<AbstractInput>();
      Iterator<AbstractInput> iterator = mSourceList.iterator();
      while (iterator.hasNext()) {
        AbstractInput input = iterator.next();
        if(!input.isHidden(context)) {
          result.add(input);
        }
      }

      AndroidTVHomeEntry androidTVHomeEntry = new AndroidTVHomeEntry();
      if(!androidTVHomeEntry.isHidden(context)) {
        result.add(androidTVHomeEntry);
      }
      return result;
    }

    public static List<AbstractInput> getSourceList() {
        return mSourceList;
    }

    public static Map<Integer, String> getSourceListForInputsBlocked(Context context) {
        if(F_TUNER_MODE_AS_SOURCE_SUPPORT) {
            Map<Integer, String> nameMap = new ArrayMap<>();
            DtvInput dtvInput = new DtvInput();
            nameMap.put(dtvInput.getHardwareId(), dtvInput.getSourceNameForUI(context));
            Iterator<AbstractInput> iterator = mSourceList.iterator();
            while (iterator.hasNext()) {
                AbstractInput input = iterator.next();
                if(input.isDTV()) {
                    continue;
                }
                nameMap.put(input.getHardwareId(), input.getSourceNameForUI(context));
            }
            return nameMap;
        } else {
            return getSourceList(context);
        }
    }

    public static Map<Integer, String> getSourceList(Context context) {
        Map<Integer, String> nameMap = new ArrayMap<>();
        Iterator<AbstractInput> iterator = mSourceList.iterator();
        while (iterator.hasNext()) {
            AbstractInput input = iterator.next();
            nameMap.put(input.getHardwareId(), input.getSourceNameForUI(context));
        }
        return nameMap;
    }

    public static void updateState(String inputId, int state) {
      Iterator<AbstractInput> iterator = mSourceList.iterator();
      while (iterator.hasNext()) {
        AbstractInput input = iterator.next();
        if(input.getId().equals(inputId)) {
          input.updateState(state);
          break;
        }
      }
    }

    public static int checkInvalideInput(int id) {
      int result = id;
      AbstractInput input = getInput(id);
      if(input == null) {
        Iterator<AbstractInput> iterator = mSourceList.iterator();
        while (iterator.hasNext()) {
          AbstractInput abstractInput = iterator.next();
          if((id >> 16) == (abstractInput.getHardwareId() >> 16)) {
            result = abstractInput.getHardwareId();
            break;
          }
        }
      } else {
        result = input.getHardwareId();
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkInvalideInput " + id + " -> " + result);
      return result;
    }

    public static boolean isTunerTypeByInputId(String inputId) {
      TvInputInfo tvInputInfo = mTvInputManager.getTvInputInfo(inputId);
      return tvInputInfo != null && tvInputInfo.getType() == TvInputInfo.TYPE_TUNER;
    }

    public static void dump(Context context, List<AbstractInput> sourceList) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dump start list.");
      Iterator<AbstractInput> iterator = mSourceList.iterator();
      while (iterator.hasNext()) {
        AbstractInput input = iterator.next();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, input.toString(context));
      }
    }

    public static void dump(Context context, Map<Integer, String> sourceList) {
        if(sourceList == null) {
            return;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dump start map.");
        Iterator<Map.Entry<Integer, String>> it =
            sourceList.entrySet().iterator();

        while (it.hasNext()) {
          Map.Entry<Integer, String> entry = it.next();

          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key:" + entry.getKey() +
            "value:" + entry.getValue());
        }
    }
}
