package com.mediatek.wwtv.setting.base.scan.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.wwtv.setting.CNChannelInfoActivity;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.Pager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.TransItem;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this adapter is used for show channel skip/sort and so on 's channel info
 *
 * @author sin_biaoqinggao
 */
public class CNChannelInfoAdapter extends BaseAdapter implements View.OnKeyListener {

  private static final String TAG = "CNChannelInfoAdapter";
  Context mContext;
  List<String[]> mList;
  String cfgId;
  EditChannel editChannel;
  private int channelSortNum = 0;
  TransItem mAction;
  int currselectPosition = 0;
  boolean needHighLightPos = false;
//  private View mSelectedView = null;
//  private static Integer sDescriptionMaxHeight = null;
  private Pager mPager;
  EnterEditDetailListener mDetailListener;

  public CNChannelInfoAdapter(Context context, TransItem action) {
    mContext = context;
    mAction = action;
  }

  public CNChannelInfoAdapter(
      Context context, List<String[]> list, TransItem action, int gotoPage) {
    super();
    mContext = context;
    mPager = new Pager(list, gotoPage);
    mList = (List<String[]>) mPager.getRealDataList();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mList.size:" + mList.size());
    mAction = action;
    mDetailListener = (CNChannelInfoActivity) mContext;
    bindData();
  }

  public void updateList(List<?> ulist) {
    if (mPager == null) {
      return;
    }
    mPager.setPagerList(ulist);
    mList = (List<String[]>) mPager.getRealDataList();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateList---mList.size:" + mList.size());
    for (String[] strings : mList) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("clasica", "when update:" + strings[2]);
    }
  }

  public int updatePage(int pos, List<?> ulist) {
    int pageNum = pos / mPager.ITEM_PER_PAGE + 1;
    mPager.currentPage = pageNum;
    updateList(ulist);
    if (pos % mPager.ITEM_PER_PAGE == 0) {
      pos = 0;
    } else {
      pos = pos % mPager.ITEM_PER_PAGE;
    }
    return pos;
  }

  private void bindData() {
    editChannel = EditChannel.getInstance(mContext);
    if (TVContent.getInstance(mContext).getCurrentTunerMode() == 0) {
      cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
    } else {
      cfgId = MenuConfigManager.POWER_ON_CH_CABLE_MODE;
    }
  }

  @Override
  public int getCount() {
    return mList.size();
  }

  public void setChannelSortNum(int channelSortNum) {
    this.channelSortNum = channelSortNum;
  }

  @Override
  public Object getItem(int position) {
    return mList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public int getSelectPos() {
    return currselectPosition;
  }

  Map<Integer, Boolean> checkMap = new HashMap<Integer, Boolean>();

  public void putCheckMap(int pos, boolean flag) {
    checkMap.put(pos, flag);
  }

  public void clearCheckMap() {
    checkMap.clear();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    String[] mInfo = mList.get(position);
    if (mInfo == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mInfo is null");
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getView---mInfo.length:" + mInfo.length);
      for (String s : mInfo) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getView---mInfo:" + s);
      }
    }
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(mContext);
      holder = new ViewHolder();
      if (CommonIntegration.isCNRegion()) {
        convertView = inflater.inflate(R.layout.menu_channel_view, parent, false);
        holder.num = (TextView) convertView.findViewById(R.id.common_channelview_text_num);
        holder.type = (TextView) convertView.findViewById(R.id.common_channelview_text_type);
        holder.freq = (TextView) convertView.findViewById(R.id.common_channelview_text_freq);
        holder.system = (TextView) convertView.findViewById(R.id.common_channelview_text_system);
        holder.name = (TextView) convertView.findViewById(R.id.common_channelview_text_name);
        holder.isCheck = (CheckBox) convertView.findViewById(R.id.channel_info_check);
      } else {
        convertView = inflater.inflate(R.layout.channelinfo_list_item, parent, false);
        holder.channelNo = (TextView) convertView.findViewById(R.id.channel_info_no);
        holder.channelType = (TextView) convertView.findViewById(R.id.channel_info_type);
        holder.channelName = (TextView) convertView.findViewById(R.id.channel_info_name);
        holder.isCheck = (CheckBox) convertView.findViewById(R.id.channel_info_check);
      }
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    if (mInfo != null) {
      if (holder == null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "holder is null");
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("forupdate", "getView:" + mInfo[2]);
        if (CommonIntegration.isCNRegion()) {
          holder.num.setText(mInfo[0]);
          holder.type.setText(mInfo[1]);
          holder.freq.setText(mInfo[4]);
          holder.system.setText(mInfo[6]);
          com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, ":" + mInfo[4]);
          if (mInfo[2] == null || mInfo[2].length() == 0) {
            holder.name.setText(" ");
          } else {
            holder.name.setText(mInfo[2]);
          }
        } else {
          holder.channelNo.setText(mInfo[0]);
          holder.channelType.setText(mInfo[1]);
          holder.channelName.setText(mInfo[2]);
          if (mInfo[2] == null || "".equals(mInfo[2])) {
            holder.channelName.setText(" ");
          } else {
            holder.channelName.setText(mInfo[2]);
          }
        }
      }
      if (holder != null) {
        showData(mInfo, holder);
      }
    }

    convertView.setTag(R.id.channel_info_no, mInfo);
    return convertView;
  }

  class ViewHolder {
    TextView channelNo;
    TextView channelType; // digital or analog
    TextView channelName;
    CheckBox isCheck;
    TextView num;
    TextView type;
    TextView freq;
    TextView system;
    TextView name;
  }

  public void goToPrevPage() {
    mPager.currentPage--;
    if (mPager.currentPage < 1) {
      mPager.currentPage = 1;
      return;
    }
    mList = (List<String[]>) mPager.getRealDataList();
    this.notifyDataSetChanged();
  }

  public void goToNextPage() {
    mPager.currentPage++;
    if (mPager.currentPage > mPager.pageTotal) {
      mPager.currentPage = mPager.pageTotal;
      return;
    }
    mList = (List<String[]>) mPager.getRealDataList();
    this.notifyDataSetChanged();
  }

  public int getCurrPage() {
    return mPager.currentPage;
  }

  private void showData(String[] mData, ViewHolder holder) {
    if (mAction.getmItemId().equals(MenuConfigManager.SETUP_POWER_ONCHANNEL_LIST)) {
      int chNum = Integer.parseInt(mData[3]);

      int value = TVContent.getInstance(mContext).getConfigValue(cfgId);
      if (chNum == value) {
        holder.isCheck.setChecked(true);
      } else {
        holder.isCheck.setChecked(false);
      }
    } else if (mAction.getmItemId().equals(MenuConfigManager.PARENTAL_CHANNEL_BLOCK_CHANNELLIST)) {
      int chNum = Integer.parseInt(mData[3]);
      if (editChannel.isChannelBlock(chNum)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chNum + " channel is blocked");
        holder.isCheck.setChecked(true);
      } else {
        holder.isCheck.setChecked(false);
      }
    } else if (mAction.getmItemId().equals(MenuConfigManager.TV_CHANNEL_DECODE_LIST)) {
      int chNum = Integer.parseInt(mData[3]);
      if (editChannel.isChannelDecode(chNum)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chNum + " channel is decode");
        holder.isCheck.setChecked(true);
      } else {
        holder.isCheck.setChecked(false);
      }
    } else if (mAction.getmItemId().equals(MenuConfigManager.TV_CHANNELFINE_TUNE_EDIT_LIST)) {
      holder.isCheck.setVisibility(View.GONE);
      if (CommonIntegration.getInstance().getCurChInfo() instanceof MtkTvAnalogChannelInfo) {
        holder.channelType.setVisibility(View.VISIBLE);
        if (mData[2] == null || "".equals(mData[2])) {
          holder.channelType.setText(" ");
        } else {
          holder.channelType.setText(mData[2]);
        }
      } else {
        holder.channelType.setVisibility(View.VISIBLE);
      }

      holder.channelName.setText(mData[4]);
    } else if (mAction.getmItemId().equals(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST)) {
      if ((((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
        holder.isCheck.setVisibility(View.INVISIBLE);
      } else {
        int chNum = Integer.parseInt(mData[3]);
        if (editChannel.getCurrentChannelId() == chNum) {
          holder.isCheck.setVisibility(View.INVISIBLE);
        } else {
          holder.isCheck.setVisibility(View.VISIBLE);
          if (editChannel.isChannelSkip(chNum)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chNum + " channel is skipped");
            holder.isCheck.setChecked(true);
          } else {
            holder.isCheck.setChecked(false);
          }
        }
      }
    } else if (mAction.getmItemId().equals(MenuConfigManager.TV_CHANNEL_EDIT_LIST)) {
      if (CommonIntegration.isCNRegion()) {
        int chNum = Integer.parseInt(mData[3]);
        if (editChannel.isChannelSkip(chNum)) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chNum + " channel is skipped");
          holder.isCheck.setChecked(true);
        } else {
          holder.isCheck.setChecked(false);
        }
      } else {
        holder.isCheck.setVisibility(View.GONE);
        if ((((CNChannelInfoActivity) mContext).isM7Enable
            && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
          holder.channelNo.setTextColor(Color.GRAY);
          holder.channelName.setTextColor(Color.GRAY);
          holder.channelType.setTextColor(Color.GRAY);
        }
      }
    } else if (mAction.getmItemId().equals(MenuConfigManager.TV_CHANNEL_SORT_CHANNELLIST)) {
      if ((((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
        holder.isCheck.setVisibility(View.INVISIBLE);
      } else {
        int chNum = Integer.parseInt(mData[3]);
        if (channelSortNum != 0 && chNum == channelSortNum) {
          this.putCheckMap(chNum, true);
          holder.isCheck.setChecked(checkMap.get(chNum));
        } else {
          if (checkMap.containsKey(chNum)) {
            holder.isCheck.setChecked(checkMap.get(chNum));
          } else {
            holder.isCheck.setChecked(false);
          }
        }
      }
    } else if (mAction.getmItemId().equals(MenuConfigManager.TV_CHANNEL_MOVE_CHANNELLIST)) {
      if ((((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
        holder.isCheck.setVisibility(View.INVISIBLE);
      } else {
        int chNum = Integer.parseInt(mData[3]);
        if (channelSortNum != 0 && chNum == channelSortNum) {
          this.putCheckMap(chNum, true);
          holder.isCheck.setChecked(checkMap.get(chNum));
        } else {
          if (!(editChannel.isChannelSkip(chNum))) {
            holder.isCheck.setVisibility(View.VISIBLE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, chNum + " channel is skipped");
            if (checkMap.containsKey(chNum)) {
              holder.isCheck.setChecked(checkMap.get(chNum));
            } else {
              holder.isCheck.setChecked(false);
            }
          } else {
            holder.isCheck.setVisibility(View.INVISIBLE);
          }
        }
      }
    } else if (mAction
        .getmItemId()
        .equals(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST)) {
      int chNum = Integer.parseInt(mData[3]);
      if (editChannel.getSchBlockType(chNum) == 0) {
        holder.isCheck.setChecked(false);
      } else {
        holder.isCheck.setChecked(true);
      }
    } else if (mAction.getmItemId().startsWith(MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL)) {
      Log.d(TAG, "showData: PARENTAL_OPEN_VCHIP_LEVEL");
//      if (mAction.getTitle().equals("")) {
//        holder.channelNo.setText("(No Text.)");
//      } else {
//        holder.channelNo.setText(mAction.getTitle());
//      }
//
//      if (mAction.mInitValue == 1) {
//        holder.isCheck.setChecked(true);
//      } else {
//        holder.isCheck.setChecked(false);
//      }
    } else if (mAction.getmItemId().startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING)
        || mAction.getmItemId().startsWith(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING)) {
      holder.isCheck.setVisibility(View.GONE);
    }
  }

  public void onKeyEnter(String[] mData, View view) {
    String mId = mAction.getmItemId();
    CheckBox box = (CheckBox) view.findViewById(R.id.channel_info_check);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyEnter mId:" + mId);
    // current options is the start channel
    if (mId.equals(MenuConfigManager.SETUP_POWER_ONCHANNEL_LIST)) {
      int chNum = Integer.parseInt(mData[3]);

      boolean enable = editChannel.setPowerOnChannel(chNum);
      changeBoxCheck(enable, box);
      this.notifyDataSetChanged();
    } else if (mId.equals(MenuConfigManager.PARENTAL_CHANNEL_BLOCK_CHANNELLIST)) {
      // current options is the block channel

      int chNum = Integer.parseInt(mData[3]);

      // judge selected channel state whether block
      boolean isChannelBlock = editChannel.isChannelBlock(chNum);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " channel block previous state: " + isChannelBlock);

      // selected channel is block state
      if (isChannelBlock) {
        editChannel.blockChannel(chNum, false);
      } else {
        editChannel.blockChannel(chNum, true);
      }
      // to judge the selected channel block state
      isChannelBlock = editChannel.isChannelBlock(chNum);
      changeBoxCheck(isChannelBlock, box);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel block current state: " + isChannelBlock);
    } else if (mId.equals(MenuConfigManager.TV_CHANNELFINE_TUNE_EDIT_LIST)) {
      ((CNChannelInfoActivity) mContext).finetuneInfoDialog(mData);
    } else if (mId.equals(MenuConfigManager.TV_CHANNEL_SKIP_CHANNELLIST)) {
      if ((((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " isM7Enable mData[0] " + mData[0]);
      } else {
        int chNum = Integer.parseInt(mData[3]);
        if (editChannel.getCurrentChannelId() == chNum) {
          return;
        } else {
          boolean isSkip = editChannel.isChannelSkip(chNum);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, mId + ",isSkip: " + isSkip);
          editChannel.setChannelSkip(chNum, !isSkip);
          changeBoxCheck(!isSkip, box);
        }
      }
    } else if (mId.equals(MenuConfigManager.TV_CHANNEL_DECODE_LIST)) {
      int chNum = Integer.parseInt(mData[3]);
      boolean isDecode = editChannel.isChannelDecode(chNum);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, mId + ",isdecode: " + isDecode);
      editChannel.setChannelDecode(chNum, !isDecode);
      changeBoxCheck(!isDecode, box);
    } else if (mId.equals(MenuConfigManager.TV_CHANNEL_SORT_CHANNELLIST)) {

      if ((((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTkgsEnable or  isM7Enable mData[0] " + mData[0]);
      } else {
        int chNum = Integer.parseInt(mData[3]);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, mId + ",chNum: " + chNum);

        if (((CNChannelInfoActivity) mContext).channelSort(chNum)) {
          box.setChecked(false);
          this.putCheckMap(chNum, false);
        } else {
          this.putCheckMap(chNum, true);
          box.setChecked(true);
        }
      }
    } else if (mId.equals(MenuConfigManager.TV_CHANNEL_MOVE_CHANNELLIST)) {
      if ((((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTkgsEnable or  isM7Enable mData[0] " + mData[0]);
      } else {
        int chNum = Integer.parseInt(mData[3]);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, mId + ",chNum: " + chNum);
        boolean isSkip = editChannel.isChannelSkip(chNum);
        if (!isSkip) {
          if (((CNChannelInfoActivity) mContext).channelMove(chNum)) {
            box.setChecked(false);
            this.putCheckMap(chNum, false);
          } else {
            this.putCheckMap(chNum, true);
            box.setChecked(true);
          }
        }
      }
    } else if (mId.equals(MenuConfigManager.TV_CHANNEL_EDIT_LIST)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, mId);
      if (((CNChannelInfoActivity) mContext).isM7Enable
          && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isM7Enable mData[0] " + mData[0]);
      } else {
        mDetailListener.enterEditDetailItem(mData);
      }
    } else if (mId.equals(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, mId + "mDataItem.mInitValue:" + mAction.mInitValue);
      // temp for biaoqing MenuMain.getInstance().channelEditForSA();
    } else if (mId.startsWith(MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL)) {
      if (mAction.mInitValue == 1) {
        mAction.mInitValue = 0;
        box.setChecked(false);
      } else {
        mAction.mInitValue = 1;
        box.setChecked(true);
      }
      int index =
          Integer.parseInt(
              mId.substring(MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL.length(), mId.length()));
      editChannel.setOpenVCHIP(mAction.mStartValue, mAction.mEndValue, index);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PARENTAL_OPEN_VCHIP_LEVEL regionIndex: " + mAction.mStartValue);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          TAG, "PARENTAL_OPEN_VCHIP_LEVEL dimIndex: " + mAction + "mDataItem:" + mAction.mEndValue);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PARENTAL_OPEN_VCHIP_LEVEL levIndex: " + index);
    } else if (mId.startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING)) {
      int index =
          Integer.parseInt(
              mId.substring(MenuConfigManager.SOUNDTRACKS_GET_STRING.length() + 1, mId.length()));
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set SOUNDTRACKS_SET_SELECT" + index);
      TVContent.getInstance(mContext)
          .setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_SELECT, index);
    } else if (mId.startsWith(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING)) {
      int index =
          Integer.parseInt(
              mId.substring(
                  MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING.length() + 1, mId.length()));
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set CFG_MENU_AUDIOINFO_GET_STRING" + index);
      TVContent.getInstance(mContext)
          .setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_SELECT, index);
    }
  }

  private void changeBoxCheck(boolean isSelected, CheckBox box) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeBackGround isSelected:" + isSelected);
    if (isSelected) {
      box.setChecked(true);
    } else {
      box.setChecked(false);
    }
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "adapter onkey listener");
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          String[] mData = (String[]) v.getTag(R.id.channel_info_no);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKEY:" + mData[0]);
          this.onKeyEnter(mData, v);
          return true;
        }
        break;
      default:
        break;
    }
    return false;
  }

  public interface EnterEditDetailListener {
    void enterEditDetailItem(String[] mData);
  }
}
