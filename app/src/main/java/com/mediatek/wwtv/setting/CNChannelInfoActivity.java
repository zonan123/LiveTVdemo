package com.mediatek.wwtv.setting;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.setting.base.scan.adapter.CNChannelInfoAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.CNChannelInfoAdapter.EnterEditDetailListener;
import com.mediatek.wwtv.setting.base.scan.adapter.EditDetailAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.EditDetailAdapter.EditItem;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.BaseCustomActivity;
import com.mediatek.wwtv.setting.scan.EditChannel;
// import com.mediatek.wwtv.setting.view.ChannelMenuViewBottom;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.TransItem;
import com.mediatek.wwtv.setting.view.JumpPageDialog;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.setting.widget.view.FinetuneDialog;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * this activity is used to show channel info 's listview
 *
 * @author sin_biaoqinggao
 */
public class CNChannelInfoActivity extends BaseCustomActivity implements EnterEditDetailListener {

  static final String TAG = "CNChannelInfoActivity";
  static final int REQ_EDITTEXT = 0x23;
  Context mContext;
  // ChannelInfoListView mListView;
  ListView mListView;
  TextView page_mid_delete;
  ImageView page_mid_img;
  private RelativeLayout pagebtnLayout;
  // private ChannelMenuViewBottom mChannelMenuViewBottom;
  CNChannelInfoAdapter mAdapter;
  EditDetailAdapter mDetailAdapter;
  MenuDataHelper mHelper;
  TransItem nowTItem;
  EditChannel mEditChannel;
  TVContent mTV;
  String mActionID;
  String[] mData; // store the selectview's channel's info
  String mCurrEditItemId;
  EditItem mCurrEditItem;
  // for channel sort waiting tip dialog
  ProgressDialog pdialog;

  int theSelectChannelPosition;
  static final int MSG_SORT_SELECT_CHANNEL = 0x34;
  static final int MSG_SORT_DELAY_TIP_DIALOG_SHOW = 0x35;
  static final int MSG_SORT_DELAY_TIP_DIALOG_HIDE = 0x36;
  static final int CHANNEL_LIST_SElECTED_FOR_SETTING_TTS = 10;
  static final String MSG_MOVE_UPDATED_NED = "mtk.intent.TV_PROVIDER_UPDATED_END";
  TurnkeyCommDialog deleteCofirm;
  Handler mSelCelHandler =
      new Handler() {

        @Override
        public void handleMessage(Message msg) {
          switch (msg.what) {
            case MSG_SORT_SELECT_CHANNEL:
              Boolean bol = (Boolean) msg.obj;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort mSelCelHandler bol value is :" + bol);
              if (bol) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort select the child -x:" + msg.arg2);
                mEditChannel.selectChannel(msg.arg2);
              } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort select the child -y:" + msg.arg1);
                mEditChannel.selectChannel(msg.arg1);
              }
              break;
            case MSG_SORT_DELAY_TIP_DIALOG_SHOW:
              if (pdialog != null) {
                pdialog.show();
              }
              this.sendEmptyMessageDelayed(MSG_SORT_DELAY_TIP_DIALOG_HIDE, 3000);
              break;
            case MSG_SORT_DELAY_TIP_DIALOG_HIDE:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel MSG_SORT_DELAY_TIP_DIALOG_HIDE");
              bindData();
              if (pdialog != null) {
                pdialog.dismiss();
              }
              break;

            case CHANNEL_LIST_SElECTED_FOR_SETTING_TTS:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " hi CHANNEL_LIST_SElECTED_FOR_SETTING_TTS index  ==" + msg.arg1);
              mListView.setSelection(msg.arg1);
              break;
            default:
              break;
          }
        }
      };

  // just for LiveTVSetting's reset Default or clean storage and ...
  BroadcastReceiver updatedUIForMoveEnd =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          Log.d(TAG, "updatedUIForMoveEnd");
          String action = intent.getAction();

          if (action.equals(MSG_MOVE_UPDATED_NED) && pdialog != null && pdialog.isShowing()) {
            Log.d(TAG, "updatedUIForMoveEnd MSG_MOVE_UPDATED_NED");
            TIFChannelManager.getInstance(mContext).getAllChannels();
            if (MenuConfigManager.TV_CHANNEL_SORT.equals(mActionID)) {
              mSelCelHandler.removeMessages(MSG_SORT_DELAY_TIP_DIALOG_HIDE);
              mSelCelHandler.sendEmptyMessageDelayed(MSG_SORT_DELAY_TIP_DIALOG_HIDE, 2000);
              return;
            } else if (MenuConfigManager.TV_CHANNEL_MOVE.equals(mActionID)) {
              mSelCelHandler.sendEmptyMessageDelayed(MSG_SORT_DELAY_TIP_DIALOG_HIDE, 2000);
            }
          }
        }
      };

  private void registerTvReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(MSG_MOVE_UPDATED_NED);
    this.registerReceiver(updatedUIForMoveEnd, filter);
  }

  private void unRegisterTvReceiver() {
    this.unregisterReceiver(updatedUIForMoveEnd);
  }

  public boolean isTkgsEnable = false;
  public boolean isM7Enable = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    nowTItem = (TransItem) getIntent().getSerializableExtra("TransItem");
    mActionID = getIntent().getStringExtra("ActionID");
    mTV = TVContent.getInstance(mContext);
    MenuConfigManager mConfigManager = MenuConfigManager.getInstance(mContext);
    mEditChannel = EditChannel.getInstance(mContext);
    mHelper = MenuDataHelper.getInstance(mContext);
    this.setContentView(R.layout.cn_menu_channel_info_layout);
    mListView = (ListView) this.findViewById(R.id.channel_info_listview);
    pagebtnLayout = (RelativeLayout) this.findViewById(R.id.pagebutton);
    // mChannelMenuViewBottom = (ChannelMenuViewBottom) findViewById(R.id.channelmenu_bottom);
    page_mid_img = (ImageView) this.findViewById(R.id.page_mid_img);
    page_mid_delete = (TextView) this.findViewById(R.id.page_mid_delete);
    // channel swap/insert/delete needn't show for CN
    // mChannelMenuViewBottom.setVisibility(View.GONE);
    int tkgsmode = MenuConfigManager.getInstance(this).getDefault(MenuConfigManager.TKGS_OPER_MODE);
    boolean satOperOnly = CommonIntegration.getInstance().isPreferSatMode();
    boolean isTkgs = mTV.isTKGSOperator();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
        TAG,
        "satOperOnly="
            + satOperOnly
            + "mTV.isTurkeyCountry() :"
            + mTV.isTurkeyCountry()
            + "mTV.isTKGSOperator(): "
            + mTV.isTKGSOperator()
            + "tkgsmode="
            + tkgsmode);

    if (tkgsmode == 1 && satOperOnly && isTkgs) {
      isTkgsEnable = true;
    }

    if (mTV.isM7ScanMode()) {
      isM7Enable = true;
    }
    if (!mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT)
        || (isTkgsEnable && mTV.getTKGSOperatorMode() != 2)) {
      page_mid_img.setVisibility(View.GONE);
      page_mid_delete.setVisibility(View.GONE);
    }
    if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SORT)
        || mActionID.equals(MenuConfigManager.TV_CHANNEL_MOVE)) {
      page_mid_img.setVisibility(View.VISIBLE);
      page_mid_delete.setText(getResources().getString(R.string.nav_dialog_go_page));
      page_mid_delete.setVisibility(View.VISIBLE);
    }
    if (mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT)
        && mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) == 1
        && mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) != 0) {
      page_mid_img.setVisibility(View.GONE);
      page_mid_delete.setVisibility(View.GONE);
    }
    if (ScanContent.isZiggoUPCOp()) {
      page_mid_img.setVisibility(View.GONE);
      page_mid_delete.setVisibility(View.GONE);
    }
    mListView.setOnItemClickListener(onItemClickListener);
    bindData();
    registerTvReceiver();
  }

  private void bindData() {
    mHelper.getTVData(mActionID);
    if (mActionID.equals(MenuConfigManager.SOUND_TRACKS)) {
      mAdapter =
          new CNChannelInfoAdapter(mContext, getSoundTracks(), nowTItem, mHelper.getGotoPage());
      pagebtnLayout.setVisibility(View.INVISIBLE);
    } else if (mActionID.equals(MenuConfigManager.CFG_MENU_AUDIOINFO)) {
      mAdapter =
          new CNChannelInfoAdapter(mContext, getSoundType(), nowTItem, mHelper.getGotoPage());
      pagebtnLayout.setVisibility(View.INVISIBLE);
    } else if (mActionID.equals(MenuConfigManager.POWER_ON_VALID_CHANNELS)) { // power on channel
      getPowerOnChannelsPageAndPos();
      mAdapter =
          new CNChannelInfoAdapter(
              mContext, mHelper.setChannelInfoList(mActionID), nowTItem, mHelper.getGotoPage());
    } else {
      mAdapter =
          new CNChannelInfoAdapter(
              mContext, mHelper.setChannelInfoList(mActionID), nowTItem, mHelper.getGotoPage());
      mAdapter.setChannelSortNum(channelSortNum);
    }
    mListView.setAdapter(mAdapter);
    mListView.setAccessibilityDelegate(mAccDelegateForChList);
    mListView.setSelection(mHelper.getGotoPosition());
    // add tslId notify
    TvCallbackHandler.getInstance()
        .addCallBackListener(TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
    // mListView.setAdapter(new
    // ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,getData()));
  }

  /** only used for power on channel list */
  void getPowerOnChannelsPageAndPos() {
    String cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
    if (mTV.getCurrentTunerMode() == 0) {
      cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
    } else {
      cfgId = MenuConfigManager.POWER_ON_CH_CABLE_MODE;
    }
    int channelId = mTV.getConfigValue(cfgId);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getPowerOnChannelsPageAndPos power on-id:" + channelId);
    int idx = 0;
    for (int i = 0; i < mHelper.getChannelInfo().size(); i++) {
      String[] data = mHelper.getChannelInfo().get(i);
      if (data[3].equals("" + channelId)) {
        idx = i;
        break;
      }
    }
    mHelper.setGotoPage(idx / 10 + 1);
    mHelper.setGotoPosition(idx % 10);
  }

  List<String> getData() {
    List<String> data = new ArrayList<String>();
    data.add("11111");
    data.add("111221");
    data.add("1116771");
    data.add("11344411");
    data.add("11ffg11");

    data.add("113345f11");
    data.add("1ggt411");
    data.add("11iuuuu1");
    data.add("1nnnbn11");

    return data;
  }

  Handler mHandler =
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          /* For TVAPI Callback */
          if ((msg.what != msg.arg1) || (msg.what != msg.arg2)) {
            super.handleMessage(msg);
            return;
          }
        }
      };

  @Override
  protected void onStart() {
    super.onStart();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart==");
  }

  @Override
  protected void onDestroy() {
    TvCallbackHandler.getInstance()
        .removeCallBackListener(TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
    mHandler = null;
    super.onDestroy();
    unRegisterTvReceiver();
  }

  @Override
  public void onPause() {
    // DTV00580191
    if (mEditChannel.getRestoreHZ() != 0 && numHz != 10 && mEditChannel.getStoredFlag()) {
      mEditChannel.setStoredFlag(true);
      mEditChannel.restoreFineTune();
    }
    mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
    super.onPause();
    channelSortNum = 0;
    mAdapter.setChannelSortNum(channelSortNum);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent==" + event.getAction() + "," + event.getKeyCode());
    if (event.getAction() == 0) {
      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
          if (CommonIntegration.isCNRegion()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "focus : " + mListView.hasFocus() + mListView.getSelectedItemPosition());
            if (mListView.getAdapter() instanceof CNChannelInfoAdapter) {
              mData = (String[]) mListView.getSelectedView().getTag(R.id.channel_info_no);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondispatch:" + mData[0]);
              ((CNChannelInfoAdapter) mListView.getAdapter())
                  .onKeyEnter(mData, mListView.getSelectedView());
            } else if (mListView.getAdapter() instanceof EditDetailAdapter) {
              EditItem item = (EditItem) mListView.getSelectedView().getTag(R.id.editdetail_value);
              if (item.isEnable) {
                if (item.id.equals(MenuConfigManager.TV_FINETUNE)) {
                  finetuneInfoDialog(mData);
                }
                if (item.id.equals(MenuConfigManager.TV_STORE)) {
                  showStoreChannelDialog();
                } else {
                  gotoEditTextAct(mListView.getSelectedView());
                }
              }
            }
            return true;
          }

          break;
        case KeyMap.KEYCODE_MTKIR_RED:
        case KeyEvent.KEYCODE_R:
          /* for channel edit (swap channel) */
          break;
        case KeyMap.KEYCODE_MTKIR_GREEN:
        case KeyEvent.KEYCODE_G:
          /* for channel edit (insert channel) */
          break;
        case KeyMap.KEYCODE_MTKIR_BLUE:
        case KeyEvent.KEYCODE_B:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent==KEYCODE_B" + event.getKeyCode());
          // for channel edit (delete channel)
          /* if(mListView.getAdapter() instanceof CNChannelInfoAdapter && mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT)){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent==KEYCODE_B getAdapter() posotion is "+mListView.getSelectedItemPosition());
           if(mListView.getSelectedView() !=null){
               mData =(String[])mListView.getSelectedView().getTag(R.id.channel_info_no);
               int deleteId =Integer.parseInt(mData[3]);
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "==KEYCODE_B deleteId ="+deleteId);
               boolean deleteSuccess =mEditChannel.deleteInactiveChannel(deleteId);
               if (deleteSuccess) {
                   mHelper.getTVData(mActionID);
                   if (mHelper.getChNum() > 0) {
                       mAdapter = new CNChannelInfoAdapter(mContext,mHelper.setChannelInfoList(mActionID),nowTItem,mHelper.getGotoPage());
                       mListView.setAdapter(mAdapter); mListView.setSelection(mHelper.getGotoPosition()); }
                   else {
                       mEditChannel.cleanChannelList();
                       finish();

                   }
               }

           }
          }*/

          if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SORT)
              || mActionID.equals(MenuConfigManager.TV_CHANNEL_MOVE)) {
            JumpPageDialog mJumpPageDialog =
                new JumpPageDialog(
                    mContext,
                    R.style.nav_dialog,
                    new JumpPageDialog.OnJumpPageClickListener() {
                      @Override
                      public void onClick(String channelNum) {
                        if (checkChannelNumExist(channelNum)) {
                          mAdapter =
                              new CNChannelInfoAdapter(
                                  mContext,
                                  mHelper.setChannelInfoList(mActionID),
                                  nowTItem,
                                  mHelper.getGotoPage());
                          mAdapter.setChannelSortNum(channelSortNum);
                          mListView.setAdapter(mAdapter);
                          mListView.setAccessibilityDelegate(mAccDelegateForChList);
                          mListView.setSelection(mHelper.getGotoPosition());
                          // add tslId notify
                          TvCallbackHandler.getInstance()
                              .addCallBackListener(
                                  TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
                        } else {
                          Toast.makeText(
                                  mContext,
                                  getResources().getString(R.string.nav_dialog_no_available),
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                      }
                    });
            mJumpPageDialog.show();
            return true;
          }

          if (page_mid_delete.getVisibility() != View.VISIBLE
              || !(mListView.getAdapter() instanceof CNChannelInfoAdapter)) {
            break;
          }
          mData = (String[]) mListView.getSelectedView().getTag(R.id.channel_info_no);
          if ((((CNChannelInfoActivity) mContext).isM7Enable
              && Integer.parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.menu_tv_delete_not_forM7Number),
                    Toast.LENGTH_LONG)
                .show();
            break;
          }
          deleteCofirm = new TurnkeyCommDialog(mContext, 3);
          deleteCofirm.setMessage(mContext.getString(R.string.menu_tv_delete_message));
          deleteCofirm.setButtonYesName(mContext.getString(R.string.common_dialog_msg_yes));
          deleteCofirm.setButtonNoName(mContext.getString(R.string.common_dialog_msg_no));
          deleteCofirm.show();
          //              camScanCofirm.setPositon(-20, 70);
          deleteCofirm.setOnKeyListener(
              new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                  int action = event.getAction();
                  if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteCofirm false back");
                    deleteCofirm.dismiss();
                    return true;
                  }
                  return false;
                }
              });

          View.OnKeyListener yesListener =
              new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                  if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteCofirm yes");
                      if (mListView.getAdapter() instanceof CNChannelInfoAdapter
                          && mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                            TAG,
                            "dispatchKeyEvent==KEYCODE_B getAdapter() posotion is "
                                + mListView.getSelectedItemPosition());
                        if (mListView.getSelectedView() != null) {
                          pdialog = null;
                          if (pdialog == null) {
                            initDeleteDelayDialog();
                          }
                          pdialog.show();
                          Message msgtip = mSelCelHandler.obtainMessage();
                          msgtip.what = MSG_SORT_DELAY_TIP_DIALOG_SHOW;
                          mSelCelHandler.sendMessage(msgtip);
                          int deleteId = Integer.parseInt(mData[3]);
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "==KEYCODE_B deleteId =" + deleteId);
                          deleteList.clear();
                          deleteList.add(deleteId);
                          boolean deleteSuccess = mEditChannel.deleteInactiveChannel(deleteList);
                          if (deleteSuccess) {
                            mHelper.getTVData(mActionID);
                            if (mHelper.getChNum() > 0) {
                              mAdapter =
                                  new CNChannelInfoAdapter(
                                      mContext,
                                      mHelper.setChannelInfoList(mActionID),
                                      nowTItem,
                                      mHelper.getGotoPage());
                              mListView.setAdapter(mAdapter);
                              mListView.setSelection(mHelper.getGotoPosition());
                            } else {
                              pdialog = null;
                              mEditChannel.cleanChannelList();
                              finish();
                            }
                          }
                        }
                      }
                      deleteCofirm.dismiss();
                      return true;
                    }
                  }
                  return false;
                }
              };

          View.OnKeyListener noListener =
              new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                  if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteCofirm no");
                      deleteCofirm.dismiss();
                      return true;
                    }
                  }
                  return false;
                }
              };

          deleteCofirm.getButtonNo().setOnKeyListener(noListener);
          deleteCofirm.getButtonYes().setOnKeyListener(yesListener);
          break;
        default:
          break;
      }
    }

    return super.dispatchKeyEvent(event);
  }

  private List<Integer> deleteList = new ArrayList<Integer>();
  /**
   * goto the edittext activity in order to input text or number
   *
   * @param selectedView
   */
  public void gotoEditTextAct(View selectedView) {
    EditItem item = (EditItem) selectedView.getTag(R.id.editdetail_value);
    mCurrEditItemId = item.id;
    mCurrEditItem = item;
    if (item.isEnable && item.dataType == DataType.INPUTBOX) { // this is edit item
      Intent intent = new Intent(mContext, EditTextActivity.class);
      intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
      intent.putExtra(EditTextActivity.EXTRA_DESC, item.title);
      intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, item.value);
      intent.putExtra(EditTextActivity.EXTRA_ITEMID, item.id);
      intent.putExtra(EditTextActivity.EXTRA_DIGIT, item.isDigit);
      if (item.isDigit) {
        intent.putExtra(EditTextActivity.EXTRA_LENGTH, 9);
      }
      if (mCurrEditItemId.equals(MenuConfigManager.TV_FREQ)
          || mCurrEditItemId.equals(MenuConfigManager.TV_FREQ_SA)) {
        intent.putExtra(EditTextActivity.EXTRA_CANFLOAT, true);
      }
      if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_NO)) {
        intent.putExtra(EditTextActivity.EXTRA_LENGTH, 4);
      }
      if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_SA_NAME)) {
        intent.putExtra(EditTextActivity.EXTRA_LENGTH, 32);
      }
      this.startActivityForResult(intent, REQ_EDITTEXT);
    } else { // this is option item
      // do nothing
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Option Item needn't go to editText Activity");
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (resultCode) {
      case RESULT_OK:
        if (data != null) {
          String value = data.getStringExtra("value");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult value:" + value);
          if (mCurrEditItemId.equals(MenuConfigManager.TV_FREQ_SA)
              || mCurrEditItemId.equals(MenuConfigManager.TV_FREQ)) {
            try {
              float now = Float.parseFloat(value);
              if (now < mCurrEditItem.minValue) {
                if (Float.parseFloat(mData[4]) >= mCurrEditItem.maxValue) {
                  now = Float.parseFloat(mData[4]) - 1.2f;
                } else {
                  now = mCurrEditItem.minValue;
                }
              } else if (now > mCurrEditItem.maxValue) {
                if (Float.parseFloat(mData[4]) <= mCurrEditItem.minValue) {
                  now = Float.parseFloat(mData[4]) + 1.8f;
                } else {
                  now = mCurrEditItem.maxValue;
                }
              }
              value = now + "";
              if (mData[4] != value) {
                mData[4] = value;
                mHelper.updateChannelFreq(mData[3], mData[4]);
              }
            } catch (NumberFormatException e) {
              Toast.makeText(mContext, "Number is not valid!please reInput", Toast.LENGTH_LONG)
                  .show();
              e.printStackTrace();
            }

          } else if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_SA_NAME)) {
            if (!value.equals(mData[2])) {
              mData[2] = value;
              mHelper.updateChannelName(mData[3], mData[2]);
            }

          } else if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_NO)) {
            int now = Integer.parseInt(value);
            if (now < mCurrEditItem.minValue) {
              now = (int) mCurrEditItem.minValue;
            } else if (now > mCurrEditItem.maxValue) {
              now = (int) mCurrEditItem.maxValue;
            }
            //           value = now + "";
            if (Integer.parseInt(mData[0]) != now) {
              if ((CommonIntegration.isEURegion() || CommonIntegration.isCNRegion())
                  && now == 0) { // not allow to change to
                // 0000 in EU
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.menu_dialog_numzero),
                        Toast.LENGTH_LONG)
                    .show();
              } else if (isM7Enable) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.menu_tv_edit_M7_num_more4000),
                        Toast.LENGTH_LONG)
                    .show();
              } else {
                if (CommonIntegration.isEUPARegion()
                    && CommonIntegration.getInstance().isCurrentSourceATVforEuPA()) {
                  now = now + CommonIntegration.DECREASE_NUM;
                }
                value = now + "";
                if (!mHelper.isChannelDeleted(value)) {
                  mData[0] = value;
                  if (CommonIntegration.isCNRegion()) {
                    mHelper.updateChannelNumberCnRegion(mData[3], mData[0]);
                  } else {
                    mHelper.updateChannelNumber(mData[3], mData[0]);
                  }
                } else {
                  Toast.makeText(
                          mContext,
                          mContext.getString(R.string.menu_dialog_numrepeat),
                          Toast.LENGTH_LONG)
                      .show();
                }
              }
            }
          }
          mDetailAdapter.setNewList(mHelper.getChannelEditDetail(mData));
        }
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown==" + keyCode);
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        // now is show detail
        if (mListView.getAdapter() instanceof EditDetailAdapter) {
          int currPage = mAdapter.getCurrPage();
          Log.d(TAG,"currentpage" + currPage);
          List<String[]> editChannelInfos = mHelper.setChannelInfoList(mActionID);
          int selIndex = 0;
          for (int i = 0; i < editChannelInfos.size(); i++) {
            String[] infosarray = editChannelInfos.get(i);
            if (infosarray != null) {
              if (infosarray[0].equals(mData[0])) { // check now channel 's ch number
                selIndex = i;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "for adapter's selIndex==" + selIndex);
                break;
              }
            }
          }
          int page = selIndex / 10 + 1;
          theSelectChannelPosition = selIndex % 10;
          mAdapter = new CNChannelInfoAdapter(mContext, editChannelInfos, nowTItem, page);
          mListView.setAdapter(mAdapter);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "theSelectChannelPosition==" + theSelectChannelPosition);
          mListView.setSelection(theSelectChannelPosition);
          pagebtnLayout.setVisibility(View.VISIBLE);
          return true;
        }
        break;
      case KeyEvent.KEYCODE_DPAD_LEFT:
      case KeyMap.KEYCODE_MTKIR_RED:
        if (mListView.getAdapter() instanceof CNChannelInfoAdapter) {
          ((CNChannelInfoAdapter) mListView.getAdapter()).goToPrevPage();
          return true;
        } else if (mListView.getAdapter() instanceof EditDetailAdapter) { // for edit channel's
          // option view
          ((EditDetailAdapter) mListView.getAdapter())
              .optionTurnLeft(mListView.getSelectedView(), mData);
          return true;
        }
        break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
      case KeyMap.KEYCODE_MTKIR_GREEN:
        if (mListView.getAdapter() instanceof CNChannelInfoAdapter) {
          ((CNChannelInfoAdapter) mListView.getAdapter()).goToNextPage();
          return true;
        } else if (mListView.getAdapter() instanceof EditDetailAdapter) { // for edit channel's
          // option view
          ((EditDetailAdapter) mListView.getAdapter())
              .optionTurnRight(mListView.getSelectedView(), mData);
          return true;
        }
        break;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onResume() {
    super.onResume();
    channelSortNum = 0;
  }

  public boolean channelMove(int chNumSrc) {
    if (channelSortNum != 0 && channelSortNum != chNumSrc) {
      initMoveDelayDialog();
      pdialog.show();
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelMoveNum:" + channelSortNum + " ch_num_src:" + chNumSrc);
      int oldChId = mEditChannel.getCurrentChannelId();
      boolean playNowChannel = (channelSortNum == oldChId);
      boolean playNowChannelSrc = (chNumSrc == oldChId);

      // check power-on channel id is the channel be swap
      String cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
      if (mTV.getCurrentTunerMode() == 0) {
        cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
      } else {
        cfgId = MenuConfigManager.POWER_ON_CH_CABLE_MODE;
      }
      int poweronchannelId = mTV.getConfigValue(cfgId);
      int powerONEqual = 0;
      if (poweronchannelId == channelSortNum) {
        powerONEqual = 1;
      } else if (poweronchannelId == chNumSrc) {
        powerONEqual = 2;
      }

      EditChannel.getInstance(mContext).channelMoveForfusion(channelSortNum, chNumSrc);
      String[] convertIDs = mHelper.updateChannelData(channelSortNum, chNumSrc);
      channelSortNum = Integer.parseInt(convertIDs[0]);
      chNumSrc = Integer.parseInt(convertIDs[1]);
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "new channelMoveNum:" + channelSortNum + "new  ch_num_src:" + chNumSrc);
      //   int position = getListSelectedPosition(ch_num_src, channelSortNum);
      // mListView.setSelection(position);
      int newChId = mEditChannel.getCurrentChannelId();
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelMove oldChId:" + oldChId + ",newChId:" + newChId);

      if (newChId != oldChId) {
        if (playNowChannel || playNowChannelSrc) {
          Message msg = mSelCelHandler.obtainMessage();
          msg.obj = playNowChannel;
          msg.what = this.MSG_SORT_SELECT_CHANNEL;
          msg.arg1 = channelSortNum;
          msg.arg2 = chNumSrc;
          mSelCelHandler.sendMessageDelayed(msg, 1000);
        }
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelMove chid changed needn't select again");
      }

      // reset power-on channel id
      if (powerONEqual == 1) {
        poweronchannelId = chNumSrc;
        mTV.setConfigValue(cfgId, poweronchannelId);
      } else if (powerONEqual == 2) {
        poweronchannelId = channelSortNum;
        mTV.setConfigValue(cfgId, poweronchannelId);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelMove perEq:" + powerONEqual + "new perchId:" + poweronchannelId);

      channelSortNum = 0;
      mAdapter.setChannelSortNum(channelSortNum);
      // mSelCelHandler.sendEmptyMessageDelayed(MSG_SORT_DELAY_TIP_DIALOG_HIDE, 5000);
      return true;
    } else if (channelSortNum == chNumSrc) {
      channelSortNum = 0;
      mAdapter.setChannelSortNum(channelSortNum);
      return true;
    } else {
      channelSortNum = chNumSrc;
      return false;
    }
  }

  int channelSortNum = 0;

  public boolean channelSort(int chNumSrc) {
    if (channelSortNum != 0 && channelSortNum != chNumSrc) {
      pdialog = null;
      if (pdialog == null) {
        initSortDelayDialog();
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelSortNum:" + channelSortNum + " ch_num_src:" + chNumSrc);
      int oldChId = mEditChannel.getCurrentChannelId();
      boolean playNowChannel = (channelSortNum == oldChId);
      boolean playNowChannelSrc = (chNumSrc == oldChId);

      // check power-on channel id is the channel be swap
      String cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
      if (mTV.getCurrentTunerMode() == 0) {
        cfgId = MenuConfigManager.POWER_ON_CH_AIR_MODE;
      } else {
        cfgId = MenuConfigManager.POWER_ON_CH_CABLE_MODE;
      }
      int poweronchannelId = mTV.getConfigValue(cfgId);
      int powerONEqual = 0;
      if (poweronchannelId == channelSortNum) {
        powerONEqual = 1;
      } else if (poweronchannelId == chNumSrc) {
        powerONEqual = 2;
      }

      EditChannel.getInstance(mContext).channelSort(channelSortNum, chNumSrc);
      String[] convertIDs = mHelper.updateChannelData(channelSortNum, chNumSrc);
      channelSortNum = Integer.parseInt(convertIDs[0]);
      chNumSrc = Integer.parseInt(convertIDs[1]);
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "new channelSortNum:" + channelSortNum + "new  ch_num_src:" + chNumSrc);
      int position = getListSelectedPosition(chNumSrc, channelSortNum);
      mListView.setSelection(position);
      int newChId = mEditChannel.getCurrentChannelId();
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelSort pos:" + position + ",oldChId:" + oldChId + ",newChId:" + newChId);

      if (newChId != oldChId) {
        if (playNowChannel || playNowChannelSrc) {
          Message msg = mSelCelHandler.obtainMessage();
          msg.obj = playNowChannel;
          msg.what = this.MSG_SORT_SELECT_CHANNEL;
          msg.arg1 = channelSortNum;
          msg.arg2 = chNumSrc;
          mSelCelHandler.sendMessageDelayed(msg, 1000);
        }
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelSort chid changed needn't select again");
      }

      // reset power-on channel id
      if (powerONEqual == 1) {
        poweronchannelId = chNumSrc;
        mTV.setConfigValue(cfgId, poweronchannelId);
      } else if (powerONEqual == 2) {
        poweronchannelId = channelSortNum;
        mTV.setConfigValue(cfgId, poweronchannelId);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelSort perEq:" + powerONEqual + "new perchId:" + poweronchannelId);

      channelSortNum = 0;
      mAdapter.setChannelSortNum(channelSortNum);
      Message msgtip = mSelCelHandler.obtainMessage();
      msgtip.what = MSG_SORT_DELAY_TIP_DIALOG_SHOW;
      mSelCelHandler.sendMessage(msgtip);
      return true;
    } else if (channelSortNum == chNumSrc) {
      channelSortNum = 0;
      mAdapter.setChannelSortNum(channelSortNum);
      return true;
    } else {
      channelSortNum = chNumSrc;
      return false;
    }
  }

  private int getListSelectedPosition(int id, int did) {
    int position = -1;
    List<String[]> channelinfo = mHelper.getChannelInfo();
    if (channelinfo != null) {
      int chNum = -1;
      int tempposition = -1;
      for (int i = 0; i < channelinfo.size(); i++) {
        chNum = Integer.parseInt(channelinfo.get(i)[3]);
        if (Math.abs(chNum - id) <= 1) {
          position = i;
          if (tempposition != -1) {
            break;
          }
        }
        if (Math.abs(chNum - channelSortNum) <= 1) {
          tempposition = i;
          if (position != -1) {
            break;
          }
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.i("wangjinben", "position:" + position + " tempposition:" + tempposition + "did" + did);
      position = position > tempposition ? tempposition : position;
      if (position < 0) {
        position = 0;
      }
      position = mAdapter.updatePage(position, channelinfo);
      mAdapter.clearCheckMap();
      mAdapter.notifyDataSetChanged();
    }

    return position;
  }

  float numHz = 10;

  /** fineture dialog */
  public void finetuneInfoDialog(final String[] mData) {
    final FinetuneDialog tcf = new FinetuneDialog(this);
    final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    numberFormat.setMaximumFractionDigits(3);
    numberFormat.setGroupingUsed(false);
    tcf.show();
    if (CommonIntegration.isSARegion()
        || CommonIntegration.isEURegion()
        || CommonIntegration.isCNRegion()) {
      String chNum = mData[0];
      tcf.setNumText(chNum);
      numHz = Float.parseFloat(mData[4]);

      tcf.setNameText(mData[2]);
      int channelID = Integer.parseInt(mData[3]);
      for (int i = 0; i < mData.length; i++) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "finetuneInfoDialog" + mData + "[" + i + "]==" + mData[i]);
      }

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "childView != null" + numHz + "channelID:" + channelID);
      if (channelID != mEditChannel.getCurrentChannelId()) {
        mEditChannel.selectChannel(channelID);
      }
    }

    String tem = numberFormat.format(numHz) + "MHz";
    tcf.setAdjustText(tem);
    mEditChannel.setOriginalFrequency(numHz);
    mEditChannel.setRestoreHZ(numHz);
    tcf.setOnKeyListener(
        new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                && action == KeyEvent.ACTION_DOWN) {
              mEditChannel.setStoredFlag(false);
              tcf.dismiss();
              // !!!!!!!!change freq need to set to adapter list
              mData[4] = numberFormat.format(numHz);
              mEditChannel.saveFineTune();
              if (CommonIntegration.isCNRegion()) {
                EditItem editItem =
                    (EditItem) mListView.getChildAt(2).getTag(R.id.editdetail_value);
                editItem.value = mData[4];
                mDetailAdapter.notifyDataSetChanged();
              } else {
                mAdapter.notifyDataSetChanged();
              }
              // requestWhenExit();
              return true;
            } else if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
              mEditChannel.setStoredFlag(true);
              mEditChannel.restoreFineTune();
              tcf.dismiss();
              return true;
            } else if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                && action == KeyEvent.ACTION_DOWN) {
              numHz = mEditChannel.fineTune(numHz, keyCode);
              tcf.setAdjustText(numberFormat.format(numHz) + "MHz");
            }
            return false;
          }
        });
  }

  LiveTVDialog storeChannelDialog;

  private void showStoreChannelDialog() {
    String info = mContext.getString(R.string.menu_tv_store_data);
    storeChannelDialog = new LiveTVDialog(mContext, "", info, 3);
    storeChannelDialog.setButtonYesName(mContext.getString(R.string.menu_ok));
    storeChannelDialog.setButtonNoName(mContext.getString(R.string.menu_cancel));
    OnKeyListener listener =
        new OnKeyListener() {
          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
              if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (v.getId() == storeChannelDialog.getButtonYes().getId()) {
                  dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                } else if (v.getId() == storeChannelDialog.getButtonNo().getId()) {
                  Log.d(TAG,"not dispatch");
                }
                storeChannelDialog.dismiss();
                return true;
              }
            }
            return false;
          }
        };
    storeChannelDialog.bindKeyListener(listener);
    storeChannelDialog.show();
  }

  @Override
  public void enterEditDetailItem(String[] mData) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enterEditDetailItem:" + mListView.getSelectedItemPosition());
    this.theSelectChannelPosition = mListView.getSelectedItemPosition();
    mDetailAdapter = new EditDetailAdapter(mContext, mHelper.getChannelEditDetail(mData));
    if (mListView.getAdapter() != null) {
      mListView.setAdapter(mDetailAdapter);
    }
    pagebtnLayout.setVisibility(View.GONE);
    //    if (CommonIntegration.isCNRegion()) {
    //      mChannelMenuViewBottom.setVisibility(View.GONE);
    //    }
  }

  private List<String[]> getSoundTracks() {
    mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_INIT, 0);
    List<String[]> lists = new ArrayList<String[]>();
    int soundListsize = mTV.getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_TOTAL);
    for (int i = 0; i < soundListsize; i++) {
      String itemName = MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING + "_" + i;
      String soundString = mTV.getConfigString(itemName);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "soundString:" + soundString);
      String[] itemValueStrings = new String[3];
      itemValueStrings[0] = "" + (i + 1);
      if (soundString != null) {
        String[] temp = soundString.split("\\+");
        if (temp.length >= 2) {
          itemValueStrings[1] = temp[0];
          itemValueStrings[2] = temp[1];
        } else if (temp.length == 1) {
          itemValueStrings[1] = temp[0];
          itemValueStrings[2] = "";
        } else {
          itemValueStrings[1] = "";
          itemValueStrings[2] = "";
        }
      }
      lists.add(itemValueStrings);
    }
    return lists;
  }

  public boolean checkChannelNumExist(String channelNum) {
    int index = 0;
    for (int i = 0; i < mHelper.getChannelInfo().size(); i++) {
      String[] data = mHelper.getChannelInfo().get(i);
      if (data[0].equals("" + channelNum)) {
        index = i;
        mHelper.setGotoPage(index / 10 + 1);
        mHelper.setGotoPosition(index % 10);
        return true;
      }
    }
    return false;
  }

  private List<String[]> getSoundType() {
    List<String[]> lists = new ArrayList<String[]>();
    mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_INIT, 0);
    int soundListsize = mTV.getConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_TOTAL);
    for (int i = 0; i < soundListsize; i++) {
      String itemName = MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING + "_" + i;
      String soundString = mTV.getConfigString(itemName);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "VisuallyImpaired:" + soundString);
      String[] itemValueStrings = new String[3];
      if (soundString != null) {
        itemValueStrings[0] = soundString;
        itemValueStrings[1] = "";
        itemValueStrings[2] = "";
      } else {
        itemValueStrings[0] = "";
        itemValueStrings[1] = "";
        itemValueStrings[2] = "";
      }
      lists.add(itemValueStrings);
    }
    return lists;
  }

  private void initMoveDelayDialog() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initMoveDelayDialog()");
    pdialog = new ProgressDialog(this);
    pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pdialog.setTitle(mContext.getResources().getString(R.string.menu_tv_move_loading));
    pdialog.setCancelable(false);
  }

  private void initDeleteDelayDialog() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initDeleteDelayDialog()");
    pdialog = new ProgressDialog(this, R.style.TranslucentGrayProgressDialog);
    pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pdialog.setTitle(mContext.getResources().getString(R.string.menu_tv_delete_loading));
    pdialog.setCancelable(false);
  }

  private void initSortDelayDialog() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSortDelayDialog()");
    pdialog = new ProgressDialog(this);
    pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pdialog.setTitle(mContext.getResources().getString(R.string.menu_tv_sort_loading));
    pdialog.setCancelable(false);
  }

  private AccessibilityDelegate mAccDelegateForChList =
      new AccessibilityDelegate() {

        public boolean onRequestSendAccessibilityEvent(
            ViewGroup host, View child, AccessibilityEvent event) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
          do {

            if (mListView.equals(host)) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "host:" + mListView + "," + host);
              break;
            } else {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":host =" + false);
            }

            List<CharSequence> texts = event.getText();
            if (texts == null) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
              break;
            }

            // confirm which item is focus
            if (event.getEventType()
                == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) { // move focus
              int index = findSelectItem(texts.get(0).toString());
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + index);
              if (index >= 0) {
                mSelCelHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_SETTING_TTS);
                Message msg = Message.obtain();
                msg.what = CHANNEL_LIST_SElECTED_FOR_SETTING_TTS;
                msg.arg1 = index;
                mSelCelHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
              }
            }

          } while (false);

          try { // host.onRequestSendAccessibilityEventInternal(child, event);
            Class clazz = Class.forName("android.view.ViewGroup");
            java.lang.reflect.Method getter =
                clazz.getDeclaredMethod(
                    "onRequestSendAccessibilityEventInternal",
                    View.class,
                    AccessibilityEvent.class);
            return (boolean) getter.invoke(host, child, event);
          } catch (Exception e) {
            Log.d(TAG, "Exception " + e);
          }

          return true;
        }

        private int findSelectItem(String text) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts =" + text);
          List<String[]> channelinfoList = mHelper.getChannelInfo();
          if (channelinfoList == null) {
            return -1;
          }

          for (int i = 0; i < channelinfoList.size(); i++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + channelinfoList.get(i)[0] + " text = " + text);
            if ((channelinfoList.get(i)[0]).equals(text)) {
              return i;
            }
          }

          return -1;
        }
      };
  private OnItemClickListener onItemClickListener =
      new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          if (mListView.getAdapter() instanceof CNChannelInfoAdapter) {
            mData = (String[]) mListView.getSelectedView().getTag(R.id.channel_info_no);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondispatch:" + mData[0]);
            ((CNChannelInfoAdapter) mListView.getAdapter())
                .onKeyEnter(mData, mListView.getSelectedView());
          } else if (mListView.getAdapter() instanceof EditDetailAdapter) {
            EditItem item = (EditItem) mListView.getSelectedView().getTag(R.id.editdetail_value);
            if (item.isEnable) {
              if (item.id.equals(MenuConfigManager.TV_FINETUNE)) {
                finetuneInfoDialog(mData);
              }
              if (item.id.equals(MenuConfigManager.TV_STORE)) {
                showStoreChannelDialog();
              } else {
                gotoEditTextAct(mListView.getSelectedView());
              }
            }
          }
        }
      };
}
