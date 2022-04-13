package com.mediatek.wwtv.setting;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.setting.view.ChannelOptionsNumDialog;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import com.mediatek.wwtv.setting.EditTextActivity;
import com.mediatek.wwtv.setting.base.scan.adapter.ChannelInfoAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.ChannelInfoAdapter.EnterEditDetailListener;
import com.mediatek.wwtv.setting.base.scan.adapter.EditDetailAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.EditDetailAdapter.EditItem;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.BaseCustomActivity;
import com.mediatek.wwtv.setting.scan.EditChannel;

import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;

import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnCancelClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.TransItem;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.setting.widget.view.FinetuneDialog;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
//import com.mediatek.wwtv.setting.base.scan.adapter.EditDetailAdapter;

import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.view.JumpPageDialog;


/**
 * this activity is used to show channel info 's listview
 * 
 * @author sin_biaoqinggao
 */
public class ChannelInfoActivity extends BaseCustomActivity implements
        EnterEditDetailListener {

    static final String TAG = "ChannelInfoActivity";
    static final int REQ_EDITTEXT = 0x23;
    Context mContext;
    // ChannelInfoListView mListView;
    ListView mListView;
    ListView mChannelInfoTypeView;
    private View mChannelInfoPagefunction;
//  private View mChannelInfoTipView;
    private View mCurrentInfoItem;
    private TextView mTitleTextView;
    private TextView mTitleChannelType;
    private TextView mChannelEditSelect;
    private TextView mChannelBlue;
    private TableRow pageDownUpRow;
    private LinearLayout mLinearLayoutTypeLayout;
    final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private boolean isClickFineturn = false;
    private boolean isEditTextFoused = false;

    // TextView page_mid_delete;
    // ImageView page_mid_img;
    // private RelativeLayout pagebtnLayout;
    // private ChannelMenuViewBottom mChannelMenuViewBottom;
    ChannelInfoAdapter mAdapter;
    EditDetailAdapter mDetailAdapter;
    MenuDataHelper mHelper;
    TransItem nowTItem;
    EditChannel mEditChannel;
    TVContent mTV;
    String mActionID;
    String[] mData;// store the selectview's channel's info
    String mCurrEditItemId;
    EditItem mCurrEditItem;
    ChannelOptionsNumDialog mChannelOptionsNumDialog;
    // for channel sort waiting tip dialog
    ProgressDialog pdialog;

    int theSelectChannelPosition;
    int theSelectChannelTypePosition;
    final static int MSG_SORT_SELECT_CHANNEL = 0x34;
    final static int MSG_SORT_DELAY_TIP_DIALOG_SHOW = 0x35;
    final static int MSG_SORT_DELAY_TIP_DIALOG_HIDE = 0x36;
    final static int CHANNEL_LIST_SElECTED_FOR_SETTING_TTS = 10;
    final static String MSG_MOVE_UPDATED_NED = "mtk.intent.TV_PROVIDER_UPDATED_END";
    TurnkeyCommDialog deleteCofirm;
    int mFocusId;
    Handler mSelCelHandler = new Handler() {

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
                    WindowManager.LayoutParams params = pdialog.getWindow()
                            .getAttributes();
                    if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                        params.x = 300;
                    } else {
                        params.x = -300;
                    }
                    params.width = (int) (pdialog.getWindow()
                            .getWindowManager().getDefaultDisplay().getWidth() * 0.3);
                    pdialog.getWindow().setAttributes(params);
                    this.sendEmptyMessageDelayed(
                            MSG_SORT_DELAY_TIP_DIALOG_HIDE, 3000);
                }
                break;
            case MSG_SORT_DELAY_TIP_DIALOG_HIDE:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channel MSG_SORT_DELAY_TIP_DIALOG_HIDE");
                bindData();
                if (pdialog != null){
                    pdialog.dismiss();
                }
                if (MenuConfigManager.TV_CHANNEL_MOVE.equals(mActionID) || MenuConfigManager.TV_CHANNEL_SORT.equals(mActionID)) {
                    focusMoveChannel();
                }
                break;

            case CHANNEL_LIST_SElECTED_FOR_SETTING_TTS:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        " hi CHANNEL_LIST_SElECTED_FOR_SETTING_TTS index  =="
                                + msg.arg1);
                mListView.setSelection(msg.arg1);
                break;
            default:
                break;

            }
        }

    };

    // just for LiveTVSetting's reset Default or clean storage and ...
    BroadcastReceiver updatedUIForMoveEnd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "updatedUIForMoveEnd");
            String action = intent.getAction();

            if (action.equals(MSG_MOVE_UPDATED_NED) && pdialog != null
                    && pdialog.isShowing()) {
                Log.d(TAG, "updatedUIForMoveEnd MSG_MOVE_UPDATED_NED");
                TIFChannelManager.getInstance(mContext).getAllChannels();
                if (MenuConfigManager.TV_CHANNEL_SORT.equals(mActionID)) {
                    mSelCelHandler
                            .removeMessages(MSG_SORT_DELAY_TIP_DIALOG_HIDE);
                    mSelCelHandler.sendEmptyMessageDelayed(
                            MSG_SORT_DELAY_TIP_DIALOG_HIDE, 2000);
                    return;
                } else if (MenuConfigManager.TV_CHANNEL_MOVE.equals(mActionID)) {
                    mSelCelHandler.sendEmptyMessageDelayed(
                            MSG_SORT_DELAY_TIP_DIALOG_HIDE, 2000);
                }else if (MenuConfigManager.TV_CHANNEL_DELETE.equals(mActionID)) {
                    mSelCelHandler.sendEmptyMessageDelayed(
                            MSG_SORT_DELAY_TIP_DIALOG_HIDE, 2000);
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
    public boolean isRCSRDSOp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        lp.width = outMetrics.widthPixels;
        lp.height = outMetrics.heightPixels;
        this.getWindow().setAttributes(lp);

        nowTItem = (TransItem) getIntent().getSerializableExtra("TransItem");
        mActionID = getIntent().getStringExtra("ActionID");
        mTV = TVContent.getInstance(mContext);
        mEditChannel = EditChannel.getInstance(mContext);
        mHelper = MenuDataHelper.getInstance(mContext);

        this.setContentView(R.layout.menu_channel_info_layout);
        initView();

        // mChannelMenuViewBottom.setVisibility(View.GONE);
        int tkgsmode = MenuConfigManager.getInstance(this).getDefault(
                MenuConfigManager.TKGS_OPER_MODE);
        boolean satOperOnly = CommonIntegration.getInstance().isPreferSatMode();
        boolean isTkgs = mTV.isTKGSOperator();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                TAG,
                "satOperOnly=" + satOperOnly + "mTV.isTurkeyCountry() :"
                        + mTV.isTurkeyCountry() + "mTV.isTKGSOperator(): "
                        + mTV.isTKGSOperator() + "tkgsmode=" + tkgsmode);

        if (tkgsmode == 1 && satOperOnly && isTkgs) {
            isTkgsEnable = true;
        }

        if (mTV.isM7ScanMode()) {
            isM7Enable = true;
        }

        if (mTV.isRomCountry() && ScanContent.isRCSRDSOp()) {
            isRCSRDSOp = true;
        }
        if (!mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT)
                || (isTkgsEnable && mTV.getTKGSOperatorMode() != 2)
                || isRCSRDSOp) {
            // page_mid_img.setVisibility(View.GONE);
            // page_mid_delete.setVisibility(View.GONE);
            mChannelInfoPagefunction.setVisibility(View.GONE);
        }
        switch (mActionID) {
        case MenuConfigManager.TV_SA_CHANNEL_EDIT:
        case MenuConfigManager.TV_CHANNEL_EDIT:
            mTitleTextView.setText(R.string.menu_tv_channel_edit);
            mTitleChannelType.setText(R.string.menu_tv_channel_edit_info);
            mChannelEditSelect.setText(R.string.nav_select);
            break;
        case MenuConfigManager.TV_CHANNEL_SKIP:
            mTitleTextView.setText(R.string.menu_tv_channel_skip);
            break;
        case MenuConfigManager.TV_CHANNEL_SORT:
            mTitleTextView.setText(R.string.menu_c_chanel_swap);
            mChannelInfoPagefunction.setVisibility(View.VISIBLE);
            mChannelEditSelect.setText(getResources().getString(R.string.nav_dialog_go_page));
            mChannelBlue.setText(getResources().getString(R.string.menu_exit));
            break;
        case MenuConfigManager.TV_CHANNEL_MOVE:
            mTitleTextView.setText(R.string.menu_c_chanel_Move);
            mChannelInfoPagefunction.setVisibility(View.VISIBLE);
            mChannelEditSelect.setText(getResources().getString(R.string.nav_dialog_go_page));
            mChannelBlue.setText(getResources().getString(R.string.menu_exit));
            break;
        case MenuConfigManager.TV_CHANNELFINE_TUNE:
            mTitleTextView.setText(R.string.menu_c_analog_tune);
            mTitleChannelType.setText(R.string.menu_c_analog_tune);
            break;
        case MenuConfigManager.TV_CHANNEL_DELETE:
            mTitleTextView.setText(R.string.menu_tv_channel_delete);
            mChannelInfoPagefunction.setVisibility(View.VISIBLE);
            mChannelEditSelect.setText(R.string.nav_select);
            break;
        case MenuConfigManager.POWER_ON_VALID_CHANNELS:
            mTitleTextView.setText(R.string.menu_title_channels);
            break;
        default:
            break;
        }
        mListView.setOnItemClickListener(onItemClickListener);
        mChannelInfoTypeView.setOnItemClickListener(onTypeItemClickListener);
        // mChannelInfoTypeView.setOnFocusChangeListener(onFocusChangeListener);
        bindData();
        registerTvReceiver();
    }

    private void initView() {
        mListView = (ListView) this.findViewById(R.id.channel_info_listview);
        mChannelInfoTypeView = (ListView) this
                .findViewById(R.id.nav_lv_channel_info_typeview);
        // pagebtnLayout = (RelativeLayout) this.findViewById(R.id.pagebutton);
        // mChannelMenuViewBottom = (ChannelMenuViewBottom)
        // findViewById(R.id.channelmenu_bottom);
        // page_mid_img= (ImageView) this.findViewById(R.id.page_mid_img);
        // page_mid_delete = (TextView) this.findViewById(R.id.page_mid_delete);

        // channel swap/insert/delete needn't show for CN
//      mChannelInfoTipView = this.findViewById(R.id.nav_channel_info_list_tip);
        mChannelInfoPagefunction = this.findViewById(R.id.nav_page_function);
        mTitleTextView = (TextView) this.findViewById(R.id.nav_c_list_title);
        mChannelBlue = (TextView) this.findViewById(R.id.channel_nav_exit);
        pageDownUpRow = (TableRow) this.findViewById(R.id.nav_info_page_up_down);
        mTitleChannelType = (TextView) this
                .findViewById(R.id.nav_tv_info_title);
        mChannelEditSelect = (TextView) this
                .findViewById(R.id.channel_nav_select_list);
        mLinearLayoutTypeLayout = (LinearLayout) this
                .findViewById(R.id.nav_ll_channel_info_typeview);
        mChannelInfoTypeView.setOnKeyListener(mKeyListener);
    }

    private View.OnKeyListener mKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == 0) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (mChannelInfoTypeView.isShown()
                            && theSelectChannelTypePosition != 0
                            && !isEditTextFoused) {
                        ImageView img = ((ImageView) mChannelInfoTypeView
                                .getSelectedView().findViewById(
                                        R.id.iv_editdetail));
                        List<EditItem> list = mDetailAdapter.getList();
                        for (int i = theSelectChannelTypePosition - 1; i > 0; i--) {
                            if (list.get(i).isEnable) {
                                theSelectChannelTypePosition = i;
                                Log.d(TAG, "theSelectChannelTypePosition銆�锛�"
                                        + theSelectChannelTypePosition);
                                EditItem item = (EditItem) mChannelInfoTypeView
                                        .getChildAt(
                                                theSelectChannelTypePosition)
                                        .getTag(R.id.editdetail_value);
                                if (!item.id
                                        .equals(MenuConfigManager.TV_FINETUNE)
                                        && !item.id
                                                .equals(MenuConfigManager.TV_STORE)
                                        && item.isEnable
                                        && item.dataType == DataType.INPUTBOX) {
                                    ((ImageView) mChannelInfoTypeView
                                            .getChildAt(
                                                    theSelectChannelTypePosition)
                                            .findViewById(R.id.iv_editdetail))
                                            .setVisibility(View.VISIBLE);
                                }
                                mChannelInfoTypeView
                                        .setSelection(theSelectChannelTypePosition);
                                img.setVisibility(View.GONE);
                                return true;
                            }
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (mChannelInfoTypeView.isShown()
                            && theSelectChannelTypePosition != (mDetailAdapter
                                    .getCount() - 1) && !isEditTextFoused) {
                        ImageView img = ((ImageView) mChannelInfoTypeView
                                .getSelectedView().findViewById(
                                        R.id.iv_editdetail));
                        List<EditItem> list = mDetailAdapter.getList();
                        for (int i = theSelectChannelTypePosition + 1; i < mDetailAdapter
                                .getCount(); i++) {
                            if (list.get(i).isEnable) {
                                theSelectChannelTypePosition = i;
                                Log.d(TAG, "theSelectChannelTypePosition銆�锛�"
                                        + theSelectChannelTypePosition);
                                EditItem item = (EditItem) mChannelInfoTypeView
                                        .getChildAt(
                                                theSelectChannelTypePosition)
                                        .getTag(R.id.editdetail_value);
                                if (!item.id
                                        .equals(MenuConfigManager.TV_FINETUNE)
                                        && !item.id
                                                .equals(MenuConfigManager.TV_STORE)
                                        && item.isEnable
                                        && item.dataType == DataType.INPUTBOX) {
                                    ((ImageView) mChannelInfoTypeView
                                            .getChildAt(
                                                    theSelectChannelTypePosition)
                                            .findViewById(R.id.iv_editdetail))
                                            .setVisibility(View.VISIBLE);
                                }
                                mChannelInfoTypeView
                                        .setSelection(theSelectChannelTypePosition);
                                img.setVisibility(View.GONE);
                                return true;
                            }
                        }
                    }
                    return true;
                default:
                    break;

                }
            }
            return false;
        }
    };

    private void bindData() {
        Log.d(TAG, "mActionID" + mActionID);
        mHelper.getTVData(mActionID);
        if (mActionID.equals(MenuConfigManager.SOUND_TRACKS)) {
            mAdapter = new ChannelInfoAdapter(mContext, getSoundTracks(),
                    nowTItem, mHelper.getGotoPage());
            // pagebtnLayout.setVisibility(View.INVISIBLE);
        } else if (mActionID.equals(MenuConfigManager.CFG_MENU_AUDIOINFO)) {
            mAdapter = new ChannelInfoAdapter(mContext, getSoundType(),
                    nowTItem, mHelper.getGotoPage());
            // pagebtnLayout.setVisibility(View.INVISIBLE);
        } else if (mActionID.equals(MenuConfigManager.POWER_ON_VALID_CHANNELS)) {// power
                                                                                    // on
                                                                                    // channel
            getPowerOnChannelsPageAndPos();
            mAdapter = new ChannelInfoAdapter(mContext,
                    mHelper.setChannelInfoList(mActionID), nowTItem,
                    mHelper.getGotoPage());
        } else {
            mAdapter = new ChannelInfoAdapter(mContext,
                    mHelper.setChannelInfoList(mActionID), nowTItem,
                    mHelper.getGotoPage());
            mAdapter.setChannelSortNum(channelSortNum);
        }
        if (mHelper.setChannelInfoList(mActionID).size() <= 10) {
            pageDownUpRow.setVisibility(View.INVISIBLE);
        }
        mListView.setAdapter(mAdapter);
        mListView.setAccessibilityDelegate(mAccDelegateForChList);
        mListView.setSelection(mHelper.getGotoPosition());
        // add tslId notify
        TvCallbackHandler.getInstance().addCallBackListener(
                TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
        // mListView.setAdapter(new
        // ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,getData()));
    }

    /**
     * only used for power on channel list
     */
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

    public boolean checkChannelNumExist(String channelNum){
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

    public boolean checkChannelIDExist(int channelId){
        int index = 0;
        for (int i = 0; i < mHelper.getChannelInfo().size(); i++) {
            String[] data = mHelper.getChannelInfo().get(i);
            if (Integer.parseInt(data[3]) == channelId) {
                index = i;
                mHelper.setGotoPage(index / 10 + 1);
                mHelper.setGotoPosition(index % 10);
                return true;
            }
        }
        return false;
    }

    private void focusMoveChannel() {
        Log.d(TAG, "focusMoveChannel: ");
        if (checkChannelIDExist(mFocusId)) {
            Log.d(TAG, "checkChannelIDExist: true : " + mFocusId);
            mAdapter = new ChannelInfoAdapter(mContext,
                    mHelper.setChannelInfoList(mActionID), nowTItem,
                    mHelper.getGotoPage());
            mAdapter.setChannelSortNum(channelSortNum);
            mListView.setAdapter(mAdapter);
            mListView.setAccessibilityDelegate(mAccDelegateForChList);
            mListView.setSelection(mHelper.getGotoPosition());
            // add tslId notify
            TvCallbackHandler.getInstance().addCallBackListener(
                    TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
            mFocusId = 0;
        }else {
            Toast.makeText(mContext, getResources().getString(R.string.nav_dialog_no_available), Toast.LENGTH_SHORT).show();
        }
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

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /* For TVAPI Callback */
            if ((msg.what != msg.arg1) || (msg.what != msg.arg2)) {
                super.handleMessage(msg);
                return;
            }
//          TvCallbackData data = (TvCallbackData) msg.obj;
            switch (msg.what) {
            case TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG:
                // DTV01009465
                /*
                 * if (mListView.getAdapter() instanceof EditDetailAdapter) {//
                 * for edit channel's option // view ((EditDetailAdapter)
                 * mListView.getAdapter()).optionTurnLeft(
                 * mListView.getSelectedView(), mData); String channelId =
                 * mData[3]; try { com.mediatek.wwtv.tvcenter.util.MtkLog.d("biaoqing", " hi channel id is  =="
                 * + channelId); int chid = Integer.parseInt(channelId); if
                 * (CommonIntegration.getInstance().getCurrentChannelId() ==
                 * chid) { MtkTvChannelInfoBase ch =
                 * CommonIntegration.getInstance().getChannelById(chid); if (ch
                 * instanceof MtkTvDvbChannelInfo) { MtkTvDvbChannelInfo dvb =
                 * (MtkTvDvbChannelInfo) ch; String nwName = dvb.getNwName();
                 * com.mediatek.wwtv.tvcenter.util.MtkLog.d("biaoqing",
                 * "MtkTvDvbChannelInfo  new  networkName==" + nwName); mData[5]
                 * = nwName; // refresh adapter to show new UI List<EditItem>
                 * eList = mHelper.getChannelEditDetail(mData);
                 * ((EditDetailAdapter)
                 * mListView.getAdapter()).setNewList(eList); } }
                 * 
                 * } catch (NumberFormatException e) { com.mediatek.wwtv.tvcenter.util.MtkLog.d("biaoqing",
                 * " error is ==" + e.getMessage()); e.printStackTrace(); } }
                 */

                break;
            default:
                break;
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
        TvCallbackHandler.getInstance().removeCallBackListener(
                TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
        mHandler = null;
        super.onDestroy();
        unRegisterTvReceiver();
    }

    @Override
    public void onPause() {
        // DTV00580191
        if (mEditChannel.getRestoreHZ() != 0 && numHz != 10
                && mEditChannel.getStoredFlag()) {
            mEditChannel.setStoredFlag(true);
            mEditChannel.restoreFineTune();
        }
        mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
        super.onPause();
        channelSortNum = 0;
        mAdapter.setChannelSortNum(channelSortNum);
        // finish();
    }

    private void turnCHByNumKey(final Context context, int keycode) {
        if (mChannelOptionsNumDialog == null) {
            mChannelOptionsNumDialog = new ChannelOptionsNumDialog(context,keycode);
            mChannelOptionsNumDialog.addTurnCHCallback(new ChannelOptionsNumDialog.OnTurnCHCallback(){

                @Override
                public void onTurnCH(String chDigit) {
                    if (checkChannelNumExist(chDigit)) {
                        mAdapter = new ChannelInfoAdapter(mContext,
                            mHelper.setChannelInfoList(mActionID), nowTItem,
                            mHelper.getGotoPage());
                        mAdapter.setChannelSortNum(channelSortNum);
                        mListView.setAdapter(mAdapter);
                        mListView.setAccessibilityDelegate(mAccDelegateForChList);
                        mListView.setSelection(mHelper.getGotoPosition());
                        // add tslId notify
                        TvCallbackHandler.getInstance().addCallBackListener(
                            TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
                    }else {
                        Toast.makeText(mContext, getResources().getString(R.string.nav_dialog_no_available), Toast.LENGTH_SHORT).show();
                    }
                }

            });
            mChannelOptionsNumDialog.show();
            return;
        }
        if (!mChannelOptionsNumDialog.isShowing()) {
            mChannelOptionsNumDialog.show();
            mChannelOptionsNumDialog.keyHandler(keycode);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                TAG,
                "dispatchKeyEvent==" + event.getAction() + ","
                        + event.getKeyCode());

        if (event.getAction() == 0) {
            switch (event.getKeyCode()) {
            case KeyMap.KEYCODE_0:
            case KeyMap.KEYCODE_1:
            case KeyMap.KEYCODE_2:
            case KeyMap.KEYCODE_3:
            case KeyMap.KEYCODE_4:
            case KeyMap.KEYCODE_5:
            case KeyMap.KEYCODE_6:
            case KeyMap.KEYCODE_7:
            case KeyMap.KEYCODE_8:
            case KeyMap.KEYCODE_9:
                if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SORT) || mActionID.equals(MenuConfigManager.TV_CHANNEL_MOVE)) {
                    turnCHByNumKey(mContext, event.getKeyCode() - KeyMap.KEYCODE_0);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:

                break;

            case KeyMap.KEYCODE_MTKIR_RED:
            case KeyEvent.KEYCODE_R:
                /* for channel edit (swap channel) */
                break;
            case KeyMap.KEYCODE_MTKIR_GREEN:
            case KeyEvent.KEYCODE_G:
                /* for channel edit (insert channel) */
                break;

            default:
                break;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    /**
     * goto the edittext activity in order to input text or number
     * 
     * @param selectedView
     */
    public void gotoEditTextAct(View selectedView) {
        EditItem item = (EditItem) selectedView.getTag(R.id.editdetail_value);
        mCurrEditItemId = item.id;
        mCurrEditItem = item;
        if (item.isEnable && item.dataType == DataType.INPUTBOX) {// this is
                                                                    // edit item
            Intent intent = new Intent(mContext, EditTextActivity.class);
            // intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
            intent.putExtra(EditTextActivity.EXTRA_DESC, item.title);
            intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, item.value);
            intent.putExtra(EditTextActivity.EXTRA_ITEMID, item.id);
            intent.putExtra(EditTextActivity.EXTRA_DIGIT, item.isDigit);
            if (item.isDigit) {
                intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 9);
            }
            if (mCurrEditItemId.equals(MenuConfigManager.TV_FREQ)
                    || mCurrEditItemId.equals(MenuConfigManager.TV_FREQ_SA)) {
                intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
                intent.putExtra(EditTextActivity.EXTRA_CANFLOAT, true);
            }
            if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_NO)) {
                intent.putExtra(EditTextActivity.EXTRA_DIGIT, false);
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 4);
            }
            if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_SA_NAME)) {
                intent.putExtra(EditTextActivity.TYPE_CLASS_TEXT, true);
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 32);
            }
            this.startActivityForResult(intent, REQ_EDITTEXT);
        } else {// this is option item
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
                        Toast.makeText(mContext,
                                "Number is not valid!please reInput",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                } else if (mCurrEditItemId
                        .equals(MenuConfigManager.TV_CHANNEL_SA_NAME)) {
                    if (!value.equals(mData[2])) {
                        mData[2] = value;
                        mHelper.updateChannelName(mData[3], mData[2]);
                    }

                } else if (mCurrEditItemId
                        .equals(MenuConfigManager.TV_CHANNEL_NO)) {
                    int now = Integer.parseInt(value);
                    if (now < mCurrEditItem.minValue) {
                        now = (int) mCurrEditItem.minValue;
                    } else if (now > mCurrEditItem.maxValue) {
                        now = (int) mCurrEditItem.maxValue;
                    }
                    value = now + "";
                    if (Integer.parseInt(mData[0]) != now) {
                        if ((CommonIntegration.isEURegion() || CommonIntegration
                                .isCNRegion()) && now == 0) {// not allow to
                                                                // change to
                            // 0000 in EU
                            Toast.makeText(
                                    mContext,
                                    mContext.getString(R.string.menu_dialog_numzero),
                                    Toast.LENGTH_LONG).show();
                        } else if (isM7Enable) {
                            Toast.makeText(
                                    mContext,
                                    mContext.getString(R.string.menu_tv_edit_M7_num_more4000),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            if (!mHelper.isChannelDeleted(value)) {
                                mData[0] = value;
                                if (CommonIntegration.isCNRegion()) {
                                    mHelper.updateChannelNumberCnRegion(
                                            mData[3], mData[0]);
                                } else {
                                    mHelper.updateChannelNumber(mData[3],
                                            mData[0]);
                                }
                            } else {
                                Toast.makeText(
                                        mContext,
                                        mContext.getString(R.string.menu_dialog_numrepeat),
                                        Toast.LENGTH_LONG).show();
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

    private void showSaveDialog() {
        final SimpleDialog mDialog = new SimpleDialog(mContext);
        mDialog.setContent(R.string.menu_tv_store_data);
        mDialog.setConfirmText(R.string.menu_ok);
        mDialog.setCancelText(R.string.menu_cancel);
        mDialog.setOnConfirmClickListener(new OnConfirmClickListener() {
            @Override
            public void onConfirmClick(int dialogId) {
                mEditChannel.setStoredFlag(false);
                mData[4] = numberFormat.format(numHz);
                mEditChannel.saveFineTune();
                if (CommonIntegration.isCNRegion()) {
                    EditItem editItem = (EditItem) mListView.getChildAt(2)
                            .getTag(R.id.editdetail_value);
                    editItem.value = mData[4];
                    mDetailAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.notifyDataSetChanged();
                }
                mDialog.dismiss();
                mListView.requestFocus();
                mLinearLayoutTypeLayout.setVisibility(View.INVISIBLE);
                ((ChannelInfoAdapter) mListView.getAdapter())
                        .dismissIMGEnter(mListView.getSelectedView());
            }
        }, SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
        mDialog.setOnCancelClickListener(new OnCancelClickListener() {
            @Override
            public void onCancelClick(int dialogId) {
                mEditChannel.setStoredFlag(true);
                mEditChannel.restoreFineTune();
                mDialog.dismiss();
                mListView.requestFocus();
                mLinearLayoutTypeLayout.setVisibility(View.INVISIBLE);
                ((ChannelInfoAdapter) mListView.getAdapter())
                        .dismissIMGEnter(mListView.getSelectedView());
            }
        }, SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
        mDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown==" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // now is show detail
                if (isClickFineturn && mLinearLayoutTypeLayout.isShown()) {
                    if (initNumHz == numHz) {
                        mEditChannel.setStoredFlag(true);
                        mEditChannel.restoreFineTune();
                    } else {
                        showSaveDialog();
                        return true;
                    }
                }

                if (mLinearLayoutTypeLayout.isShown()) {
                    mListView.requestFocus();
                    mLinearLayoutTypeLayout.setVisibility(View.INVISIBLE);
                    // int currPage = mAdapter.getCurrPage();
                    // List<String[]> editChannelInfos = mHelper
                    // .setChannelInfoList(mActionID);
                    // int selIndex = 0;
                    // for (int i = 0; i < editChannelInfos.size(); i++) {
                    // String[] infosarray = editChannelInfos.get(i);
                    // if (infosarray != null) {
                    // if (infosarray[0].equals(mData[0])) {// check now
                    // // channel 's ch
                    // // number
                    // selIndex = i;
                    // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "for adapter's selIndex==" + selIndex);
                    // break;
                    // }
                    // }
                    // }
                    // int page = selIndex / 10 + 1;
                    // theSelectChannelPosition = selIndex % 10;
                    // mAdapter = new ChannelInfoAdapter(mContext, editChannelInfos,
                    // nowTItem, page);
                    // mListView.setAdapter(mAdapter);
                    // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "theSelectChannelPosition=="
                    // + theSelectChannelPosition);
                    // mListView.setSelection(theSelectChannelPosition);
                    ((ChannelInfoAdapter) mListView.getAdapter())
                        .dismissIMGEnter(mListView.getSelectedView());
                    // pagebtnLayout.setVisibility(View.VISIBLE);
                /*if (CommonIntegration.isCNRegion()) {
                    // mChannelMenuViewBottom.setVisibility(View.VISIBLE);
                }*/
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyMap.KEYCODE_MTKIR_RED:
                if (mLinearLayoutTypeLayout.isShown() && changeFinetune(keyCode)) {
                    return true;
                }
                if (mLinearLayoutTypeLayout.isShown()) {
                    ((EditDetailAdapter) mChannelInfoTypeView.getAdapter())
                        .optionTurnLeft(mChannelInfoTypeView.getSelectedView(),
                            mData);
                    return true;
                } else {
                    ((ChannelInfoAdapter) mListView.getAdapter()).goToPrevPage();
                    return true;
                }
                // break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyMap.KEYCODE_MTKIR_GREEN:
                if (mLinearLayoutTypeLayout.isShown() && changeFinetune(keyCode)) {
                    return true;
                }
                if (mLinearLayoutTypeLayout.isShown()) {
                    ((EditDetailAdapter) mChannelInfoTypeView.getAdapter())
                        .optionTurnRight(
                            mChannelInfoTypeView.getSelectedView(), mData);
                    return true;
                } else {
                    ((ChannelInfoAdapter) mListView.getAdapter()).goToNextPage();
                    return true;
                }
                // break;
            case KeyMap.KEYCODE_MTKIR_YELLOW:
            case KeyEvent.KEYCODE_Y:
                if (mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT)
                    && !mChannelInfoTypeView.isShown()) {
                    mData = (String[]) mListView.getSelectedView().getTag(
                        R.id.channel_info_no);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondispatch:" + mData[0]);
                    mCurrentInfoItem = mListView.getSelectedView();
                    ((ChannelInfoAdapter) mListView.getAdapter()).onKeyEnter(
                        mData, mListView.getSelectedView());
                } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SORT) || mActionID.equals(MenuConfigManager.TV_CHANNEL_MOVE)) {
                    JumpPageDialog mJumpPageDialog = new JumpPageDialog(mContext, R.style.nav_dialog, new JumpPageDialog.OnJumpPageClickListener() {
                        @Override
                        public void onClick(String channelNum) {
                            if (checkChannelNumExist(channelNum)) {
                                mAdapter = new ChannelInfoAdapter(mContext,
                                    mHelper.setChannelInfoList(mActionID), nowTItem,
                                    mHelper.getGotoPage());
                                mAdapter.setChannelSortNum(channelSortNum);
                                mListView.setAdapter(mAdapter);
                                mListView.setAccessibilityDelegate(mAccDelegateForChList);
                                mListView.setSelection(mHelper.getGotoPosition());
                                // add tslId notify
                                TvCallbackHandler.getInstance().addCallBackListener(
                                    TvCallbackConst.MSG_CB_NFY_TSL_ID_UPDATE_MSG, mHandler);
                            } else {
                                Toast.makeText(mContext, getResources().getString(R.string.nav_dialog_no_available), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    mJumpPageDialog.show();
                    WindowManager.LayoutParams params = mJumpPageDialog.getWindow().getAttributes();
                    if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                        params.x = 300;
                    } else {
                        params.x = -300;
                    }
                    params.y = -100;
                    //params.width = (int)(mJumpPageDialog.getWindow().getWindowManager().getDefaultDisplay().getWidth() * 0.3);
                    mJumpPageDialog.getWindow().setAttributes(params);
                } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_DELETE)) {
                    if (mListView.getAdapter() instanceof ChannelInfoAdapter) {
                        if (mListView.getSelectedView() != null) {
                            mData = (String[]) mListView.getSelectedView().getTag(
                                R.id.channel_info_no);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondispatch:" + mData[0]);
                            mCurrentInfoItem = mListView.getSelectedView();
                            ((ChannelInfoAdapter) mListView.getAdapter()).onKeyEnter(mData,
                                mListView.getSelectedView());
                        }
                    }
                }
                return true;
            case KeyMap.KEYCODE_MTKIR_BLUE:
            case KeyEvent.KEYCODE_B:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "dispatchKeyEvent==KEYCODE_B" + event.getKeyCode());

                if ((mActionID.equals(MenuConfigManager.TV_CHANNEL_EDIT) || (mActionID.equals(MenuConfigManager.TV_CHANNEL_DELETE)))
                    && !mChannelInfoTypeView.isShown()) {
                    mData = (String[]) mListView.getSelectedView().getTag(
                        R.id.channel_info_no);
                    if ((((ChannelInfoActivity) mContext).isM7Enable && Integer
                        .parseInt(mData[0]) < CommonIntegration.M7BaseNumber)) {
                        Toast.makeText(
                            mContext,
                            mContext.getString(R.string.menu_tv_delete_not_forM7Number),
                            Toast.LENGTH_LONG).show();
                        break;
                    }

                    if (mActionID.equals(MenuConfigManager.TV_CHANNEL_DELETE) && deleteList.isEmpty()) {
                        return true;
                    }
                    SimpleDialog simpleDialog = new SimpleDialog(mContext);
                    simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
                    simpleDialog.setContent(R.string.menu_tv_delete_message);
                    simpleDialog.setConfirmText(R.string.common_dialog_msg_yes);
                    simpleDialog.setCancelText(R.string.common_dialog_msg_no);
                    simpleDialog.setOnConfirmClickListener(new OnConfirmClickListener() {
                        @Override
                        public void onConfirmClick(int dialogId) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteCofirm yes");
                            pdialog = null;
                            if (pdialog == null) {
                                initDeleteDelayDialog();
                            }
                            pdialog.show();
                            WindowManager.LayoutParams params = pdialog.getWindow().getAttributes();
                            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                                params.x = 300;
                            } else {
                                params.x = -300;
                            }
                            params.width = (int) (pdialog.getWindow().getWindowManager().getDefaultDisplay().getWidth() * 0.3);
                            pdialog.getWindow().setAttributes(params);
                            Message msgtip = mSelCelHandler
                                .obtainMessage();
                            msgtip.what = MSG_SORT_DELAY_TIP_DIALOG_SHOW;
                            mSelCelHandler.sendMessage(msgtip);
                            if (mListView.getAdapter() instanceof ChannelInfoAdapter
                                && (mActionID
                                .equals(MenuConfigManager.TV_CHANNEL_EDIT))
                                || mActionID
                                .equals(MenuConfigManager.TV_CHANNEL_DELETE)) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                    TAG,
                                    "dispatchKeyEvent==KEYCODE_B getAdapter() posotion is "
                                        + mListView
                                        .getSelectedItemPosition());
                                if (mListView.getSelectedView() != null) {

                                    int deleteId = Integer
                                        .parseInt(mData[3]);
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                        "==KEYCODE_B deleteId ="
                                            + deleteId);
                                    boolean deleteSuccess = false;
                                    if (mActionID
                                        .equals(MenuConfigManager.TV_CHANNEL_DELETE)) {
                                        Log.d(TAG,
                                            "How much channel need delete: "
                                                + deleteList
                                                .size());
                                        deleteSuccess = mEditChannel
                                            .deleteInactiveChannel(deleteList);// delete
                                        // list
                                        Log.d(TAG, "isDeleteSuccess: "
                                            + deleteSuccess);
                                        deleteList.clear();
                                    } else {
                                        // delete one
                                        List<Integer> deleteChannelIDList = new ArrayList<Integer>();
                                        deleteChannelIDList
                                            .add(deleteId);
                                        deleteSuccess = mEditChannel
                                            .deleteInactiveChannel(deleteChannelIDList);
                                    }
                                    if (deleteSuccess) {
                                        mHelper.getTVData(mActionID);
                                        if (mHelper.getChNum() > 0) {
                                            mAdapter = new ChannelInfoAdapter(
                                                mContext,
                                                mHelper.setChannelInfoList(mActionID),
                                                nowTItem,
                                                mHelper.getGotoPage());
                                            mListView
                                                .setAdapter(mAdapter);
                                            mListView
                                                .setSelection(mHelper
                                                    .getGotoPosition());
                                        } else {
                                            pdialog = null;
                                            mEditChannel
                                                .cleanChannelList();
                                            finish();
                                        }
                                    }
                                }
                            }
                        }
                    }, SimpleDialog.DIALOG_ID_SAVE_FINETUNE);
                    simpleDialog.show();
                    WindowManager.LayoutParams params = simpleDialog.getWindow().getAttributes();
                    if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                        params.x = 300;
                    } else {
                        params.x = -300;
                    }
                    simpleDialog.getWindow().setAttributes(params);
                } else if (mActionID.equals(MenuConfigManager.TV_CHANNEL_SORT) ||
                    mActionID.equals(MenuConfigManager.TV_CHANNEL_MOVE)) {
                    onBackPressed();
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean changeFinetune(int keyCode) {
        EditItem item = (EditItem) mChannelInfoTypeView.getSelectedView()
                .getTag(R.id.editdetail_value);
        if (item.id.equals(MenuConfigManager.TV_FINETUNE) && item.isEnable) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                Log.d("numhziswhatr:", numHz + "");
                numHz = mEditChannel.fineTune(numHz, keyCode);
                Log.d("numhzwaswhatr:", numHz + "");
                TextView tvOption = (TextView) mChannelInfoTypeView
                        .getSelectedView().findViewById(R.id.editdetail_option);
                tvOption.setText(String.format(getResources().getString(R.string.channel_finetune_MHz), numberFormat.format(numHz)));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        channelSortNum = 0;

    }

    public boolean channelMove(int chNumSrc) {
        if (channelSortNum != 0 && channelSortNum != chNumSrc) {
            //mFocusId = chNumSrc;
            MtkTvChannelInfoBase selChannel = CommonIntegration.getInstance().getChannelById(channelSortNum);
            initMoveDelayDialog();
            pdialog.show();
            WindowManager.LayoutParams params = pdialog.getWindow().getAttributes();
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
                params.x = 300;
            } else {
                params.x = -300;
            }
            params.width = (int)(pdialog.getWindow().getWindowManager().getDefaultDisplay().getWidth() * 0.3);
            pdialog.getWindow().setAttributes(params);
            
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelMoveNum:" + channelSortNum + " chNumSrc:"
                    + chNumSrc);
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

            EditChannel.getInstance(mContext).channelMoveForfusion(
                    channelSortNum, chNumSrc);
            String[] convertIDs = mHelper.updateChannelData(channelSortNum,
                    chNumSrc);
            channelSortNum = Integer.parseInt(convertIDs[0]);
            chNumSrc = Integer.parseInt(convertIDs[1]);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "new channelMoveNum:" + channelSortNum
                    + "new  chNumSrc:" + chNumSrc);
            // int position = getListSelectedPosition(chNumSrc,
            // channelSortNum);
            // mListView.setSelection(position);
            int newChId = mEditChannel.getCurrentChannelId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelMove oldChId:" + oldChId + ",newChId:"
                    + newChId);

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
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelMove perEq:" + powerONEqual + "new perchId:"
                    + poweronchannelId);

            channelSortNum = 0;
            mAdapter.setChannelSortNum(channelSortNum);
            // mSelCelHandler.sendEmptyMessageDelayed(MSG_SORT_DELAY_TIP_DIALOG_HIDE,
            // 5000);

            if (selChannel != null) {
                MtkTvChannelInfoBase infoBase = CommonIntegration.getInstance().getChannelByRecId(selChannel.getSvlId(), selChannel.getSvlRecId());
                if (infoBase != null) {
                    mFocusId = infoBase.getChannelId();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "old ID : = " + selChannel.getChannelId() + "new ID : =" + mFocusId);
                }
            }
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
            //mFocusId = chNumSrc;
            MtkTvChannelInfoBase selChannel = CommonIntegration.getInstance().getChannelById(channelSortNum);
            pdialog = null;
            if (pdialog == null) {
                initSortDelayDialog();
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelSortNum:" + channelSortNum + " chNumSrc:"
                    + chNumSrc);
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

            EditChannel.getInstance(mContext).channelSort(channelSortNum,
                    chNumSrc);
            String[] convertIDs = mHelper.updateChannelData(channelSortNum,
                    chNumSrc);
            channelSortNum = Integer.parseInt(convertIDs[0]);
            chNumSrc = Integer.parseInt(convertIDs[1]);
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "new channelSortNum:" + channelSortNum
                    + "new  chNumSrc:" + chNumSrc);
            int position = getListSelectedPosition(chNumSrc);
            mListView.setSelection(position);
            int newChId = mEditChannel.getCurrentChannelId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "channelSort pos:" + position + ",oldChId:" + oldChId
                    + ",newChId:" + newChId);

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
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelSort perEq:" + powerONEqual + "new perchId:"
                    + poweronchannelId);

            channelSortNum = 0;
            mAdapter.setChannelSortNum(channelSortNum);
            Message msgtip = mSelCelHandler.obtainMessage();
            msgtip.what = MSG_SORT_DELAY_TIP_DIALOG_SHOW;
            mSelCelHandler.sendMessage(msgtip);

            if (selChannel != null) {
                MtkTvChannelInfoBase infoBase = CommonIntegration.getInstance().getChannelByRecId(selChannel.getSvlId(), selChannel.getSvlRecId());
                if (infoBase != null) {
                    mFocusId = infoBase.getChannelId();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "old ID : = " + selChannel.getChannelId() + "new ID : =" + mFocusId);
                }
            }
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

    private List<Integer> deleteList = new ArrayList<Integer>();

    public void addDeleteChannelID(int channelID) {
        deleteList.add(channelID);
    }

    public boolean removeDeleteChannelID(int channelID) {
        return deleteList.remove(new Integer(channelID));
    }

    private int getListSelectedPosition(int id) {
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
            com.mediatek.wwtv.tvcenter.util.MtkLog.i("wangjinben", "position:" + position + " tempposition:"
                    + tempposition);
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
    float initNumHz;

    /**
     * fineture dialog
     */
    public void finetuneInfoDialog(final String[] mData) {
        final FinetuneDialog tcf = new FinetuneDialog(this);
        final NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(3);
        numberFormat.setGroupingUsed(false);
        tcf.show();
        if (CommonIntegration.isSARegion() || CommonIntegration.isEURegion()
                || CommonIntegration.isCNRegion()) {
            String chNum = mData[0];
            tcf.setNumText(chNum);
            numHz = Float.parseFloat(mData[4]);
            

            tcf.setNameText(mData[2]);
            int channelID = Integer.parseInt(mData[3]);
            for (int i = 0; i < mData.length; i++) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "finetuneInfoDialog" + mData + "[" + i + "]=="
                        + mData[i]);
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "childView != null" + numHz + "channelID:"
                    + channelID);
            if (channelID != mEditChannel.getCurrentChannelId()) {
                mEditChannel.selectChannel(channelID);
            }
        }

        String tem = numberFormat.format(numHz) + "MHz";
        tcf.setAdjustText(tem);
        mEditChannel.setOriginalFrequency(numHz);
        mEditChannel.setRestoreHZ(numHz);
        tcf.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                int action = event.getAction();
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                        && action == KeyEvent.ACTION_DOWN) {
                    mEditChannel.setStoredFlag(false);
                    tcf.dismiss();
                    // !!!!!!!!change freq need to set to adapter list
                    mData[4] = numberFormat.format(numHz);
                    mEditChannel.saveFineTune();
                    if (CommonIntegration.isCNRegion()) {
                        EditItem editItem = (EditItem) mListView.getChildAt(2)
                                .getTag(R.id.editdetail_value);
                        editItem.value = mData[4];
                        mDetailAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    // requestWhenExit();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK
                        && action == KeyEvent.ACTION_DOWN) {
                    mEditChannel.setStoredFlag(true);
                    mEditChannel.restoreFineTune();
                    tcf.dismiss();
                    return true;
                } else if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
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
        storeChannelDialog.setButtonYesName(mContext
                .getString(R.string.menu_ok));
        storeChannelDialog.setButtonNoName(mContext
                .getString(R.string.menu_cancel));
        OnKeyListener listener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (v.getId() == storeChannelDialog.getButtonYes()
                                .getId()) {
                            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_BACK));
                        } else if (v.getId() == storeChannelDialog
                                .getButtonNo().getId()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"v.getId()" + v.getId());
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
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "enterEditDetailItem:" + mListView.getSelectedItemPosition());
        isClickFineturn = false;
        this.theSelectChannelPosition = mListView.getSelectedItemPosition();
        mDetailAdapter = new EditDetailAdapter(mContext,
                mHelper.getChannelEditDetail(mData));

        // Show TypeList and set Adapter
        // if (mListView.getAdapter() != null) {
        // mListView.setAdapter(mDetailAdapter);
        // }
        mChannelInfoTypeView.setAdapter(mDetailAdapter);
        mLinearLayoutTypeLayout.setVisibility(View.VISIBLE);

        mChannelInfoTypeView.requestFocus();
        // mChannelInfoTypeView.setSelection(0);
        // setAdpter was asynchronous

        mChannelInfoTypeView.setSelection(1);
        mChannelInfoTypeView.post(new Runnable() {

            @Override
            public void run() {
                // setSelection锛堬級 and getSelectedItemPosition() was
                // asynchronization
                theSelectChannelTypePosition = 1;
                EditItem item1 = (EditItem) mDetailAdapter.getList().get(1);
                if (item1.isEnable) {
                    Log.d(TAG, "isItemEnable");
                    ((ImageView) mChannelInfoTypeView.getChildAt(1)
                            .findViewById(R.id.iv_editdetail))
                            .setVisibility(View.VISIBLE);
                } else {
                    mChannelInfoTypeView.setSelection(2);
                    theSelectChannelTypePosition = 2;
                    EditItem item2 = (EditItem) mDetailAdapter.getList().get(2);
                    if (item2.isEnable) {
                        Log.d(TAG, "isItemEnable");
                        ((ImageView) mChannelInfoTypeView.getChildAt(2)
                                .findViewById(R.id.iv_editdetail))
                                .setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // mChannelInfoTypeView.setSelection(1);

        // pagebtnLayout.setVisibility(View.GONE);
        /*if (CommonIntegration.isCNRegion()) {
            // mChannelMenuViewBottom.setVisibility(View.GONE);
        }*/
    }

    public void enterFinetuneItem() {
        this.theSelectChannelPosition = mListView.getSelectedItemPosition();
        isClickFineturn = true;
        mDetailAdapter = new EditDetailAdapter(mContext,
                mHelper.getChannelFreqDetail(mData));
        mChannelInfoTypeView.setAdapter(mDetailAdapter);
        mLinearLayoutTypeLayout.setVisibility(View.VISIBLE);
        mChannelInfoTypeView.requestFocus();
        // mChannelInfoTypeView.setSelection(0);
        mChannelInfoTypeView.post(new Runnable() {
            @Override
            public void run() {
                mChannelInfoTypeView.setSelection(2);
                theSelectChannelTypePosition = mChannelInfoTypeView
                        .getSelectedItemPosition();
                // EditItem item = (EditItem) mChannelInfoTypeView
                // .getSelectedView().getTag(R.id.editdetail_value);
                // if (item.isEnable) {
                // Log.d(TAG, "isItemEnable");
                // ((ImageView) mChannelInfoTypeView.getSelectedView()
                // .findViewById(R.id.iv_editdetail))
                // .setVisibility(View.VISIBLE);
                // }
            }
        });
        numberFormat.setMaximumFractionDigits(3);
        numberFormat.setGroupingUsed(false);
        numHz = Float.parseFloat(mData[4]);
        initNumHz = Float.parseFloat(mData[4]);
        int channelID = Integer.parseInt(mData[3]);
        for (int i = 0; i < mData.length; i++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "finetuneInfoDialog" + mData + "[" + i + "]=="
                    + mData[i]);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "childView != null" + numHz + "channelID:" + channelID);
        if (channelID != mEditChannel.getCurrentChannelId()) {
            mEditChannel.selectChannel(channelID);
        }
//      String tem = numberFormat.format(numHz) + "MHz";
        mEditChannel.setOriginalFrequency(numHz);
        mEditChannel.setRestoreHZ(numHz);
    }

    private List<String[]> getSoundTracks() {
        mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_INIT, 0);
        List<String[]> lists = new ArrayList<String[]>();
        int soundListsize = mTV
                .getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_TOTAL);
        for (int i = 0; i < soundListsize; i++) {
            String itemName = MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING
                    + "_" + i;
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

    private List<String[]> getSoundType() {
        List<String[]> lists = new ArrayList<String[]>();
        mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_INIT, 0);
        int soundListsize = mTV
                .getConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_TOTAL);
        for (int i = 0; i < soundListsize; i++) {
            String itemName = MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING
                    + "_" + i;
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
        pdialog = new ProgressDialog(this,R.style.TranslucentGrayProgressDialog);
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setTitle(mContext.getResources().getString(
                R.string.menu_tv_move_loading));
        pdialog.setCancelable(false);
    }
    
    private void initDeleteDelayDialog() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initDeleteDelayDialog()");
        pdialog = new ProgressDialog(this,R.style.TranslucentGrayProgressDialog);
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setTitle(mContext.getResources().getString(
                R.string.menu_tv_delete_loading));
        pdialog.setCancelable(false);
    }

    private void initSortDelayDialog() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSortDelayDialog()");
        pdialog = new ProgressDialog(this,R.style.TranslucentGrayProgressDialog);
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setTitle(mContext.getResources().getString(
                R.string.menu_tv_sort_loading));
        pdialog.setCancelable(false);
    }

    private AccessibilityDelegate mAccDelegateForChList = new AccessibilityDelegate() {

        public boolean onRequestSendAccessibilityEvent(ViewGroup host,
                View child, AccessibilityEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + ","
                    + child + "," + event);
            do {

                if (!mListView.equals(host)) {
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
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move
                                                                                                    // focus
                    int index = findSelectItem(texts.get(0).toString());
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + index);
                    if (index >= 0) {
                        mSelCelHandler
                                .removeMessages(CHANNEL_LIST_SElECTED_FOR_SETTING_TTS);
                        Message msg = Message.obtain();
                        msg.what = CHANNEL_LIST_SElECTED_FOR_SETTING_TTS;
                        msg.arg1 = index;
                        mSelCelHandler.sendMessageDelayed(msg,
                                CommonIntegration.TTS_TIME);
                    }
                }

            } while (false);

            try {// host.onRequestSendAccessibilityEventInternal(child, event);
                Class clazz = Class.forName("android.view.ViewGroup");
                java.lang.reflect.Method getter = clazz.getDeclaredMethod(
                        "onRequestSendAccessibilityEventInternal", View.class,
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
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + channelinfoList.get(i)[0]
                        + " text = " + text);
                if ((channelinfoList.get(i)[0]).equals(text)) {
                    return i;
                }
            }

            return -1;
        }
    };

    public void showSoftKeyboard(View view, Context mContext) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void closeSoftKeybord(EditText mEditText, Context mcContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (mListView.getAdapter() instanceof ChannelInfoAdapter) {
                if (mListView.getSelectedView() != null){
                    mData = (String[]) mListView.getSelectedView().getTag(
                            R.id.channel_info_no);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondispatch:" + mData[0]);
                    mCurrentInfoItem = mListView.getSelectedView();
                    ((ChannelInfoAdapter) mListView.getAdapter()).onKeyEnter(mData,
                            mListView.getSelectedView());
                }
            }
            /*
             * else if (mListView.getAdapter() instanceof EditDetailAdapter) {
             * EditItem item = (EditItem) mListView.getSelectedView().getTag(
             * R.id.editdetail_value); if (item.isEnable) { if
             * (item.id.equals(MenuConfigManager.TV_FINETUNE)) {
             * finetuneInfoDialog(mData); } if
             * (item.id.equals(MenuConfigManager.TV_STORE)) {
             * showStoreChannelDialog(); } else {
             * gotoEditTextAct(mListView.getSelectedView()); } }
             * 
             * }
             */
        }
    };

    private OnItemClickListener onTypeItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // if (mListView.getAdapter() instanceof ChannelInfoAdapter) {
            // mData = (String[]) mListView.getSelectedView().getTag(
            // R.id.channel_info_no);
            // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ondispatch:" + mData[0]);
            // ((ChannelInfoAdapter) mListView.getAdapter()).onKeyEnter(mData,
            // mListView.getSelectedView());
            // } else
            if (mChannelInfoTypeView.getAdapter() instanceof EditDetailAdapter && mChannelInfoTypeView.getSelectedView() != null ) {
                EditItem item = (EditItem) mChannelInfoTypeView
                        .getSelectedView().getTag(R.id.editdetail_value);
                if (item.isEnable) {
                    /*if (item.id.equals(MenuConfigManager.TV_FINETUNE)) {
                        // finetuneInfoDialog(mData);
                    }*/
                    if (item.id.equals(MenuConfigManager.TV_STORE)) {
                        showStoreChannelDialog();
                    } else {
                        // gotoEditTextAct(mChannelInfoTypeView.getSelectedView());
                        onClickTypeViewEdit(item);
                    }
                }

            }
        }
    };

    private void onClickTypeViewEdit(EditItem item) {
        final EditText editText = (EditText) mChannelInfoTypeView
                .getSelectedView().findViewById(R.id.editdetail_value);
        mCurrEditItemId = item.id;
        mCurrEditItem = item;
        if (editText.isShown()) {
            editText.setFocusable(true);
            isEditTextFoused = true;
            final String textOld = item.value;
            editText.setText(textOld);
            editText.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            if (editText.isFocusable()) {
                                editText.setFocusable(false);
                                isEditTextFoused = false;
                                editText.setTextColor(getResources().getColor(
                                        R.color.content_title_text_color));
                                editText.setBackgroundResource(0);
                                mChannelInfoTypeView.setFocusable(true);
                                mChannelInfoTypeView.requestFocus();
                                closeSoftKeybord(editText, mContext);
                                editTextPressEnter(editText.getText()
                                        .toString() + "");
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_BACK:
                            if (editText.isFocusable()) {
                                editText.setFocusable(false);
                                isEditTextFoused = false;
                                editText.setTextColor(getResources().getColor(
                                        R.color.content_title_text_color));
                                if (textOld.length() > 16) {
                                    editText.setText(textOld.substring(0, 16)
                                            + "...");
                                } else {
                                    editText.setText(textOld);
                                }
                                editText.setBackgroundResource(0);
                                mChannelInfoTypeView.setFocusable(true);
                                mChannelInfoTypeView.requestFocus();
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            if (editText.isFocusable()) {
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            if (editText.isFocusable()) {
                                return true;
                            }
                            break;

                        default:
                            break;
                        }
                        return false;
                    } else {
                        return false;
                    }
                }
            });
            mChannelInfoTypeView.setFocusable(false);
            // editText.postDelayed(new Runnable() {
            //
            // @Override
            // public void run() {
            // editText.requestFocus();
            // }
            // }, 500);
            editText.requestFocus();
            editText.setBackground(getResources().getDrawable(
                    R.drawable.nav_channel_editbgd_shape));
            editText.setTextColor(Color.BLACK);
            showSoftKeyboard(editText, mContext);

        }
    }

    private void editTextPressEnter(String value) {
        // String value = data.getStringExtra("value");
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
                Toast.makeText(mContext, "Number is not valid!please reInput",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } else if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_SA_NAME)) {
            if (!value.equals(mData[2])) {
                mData[2] = value;
                mHelper.updateChannelName(mData[3], mData[2]);
                // change channel info
                ((TextView) mCurrentInfoItem
                        .findViewById(R.id.channel_info_name)).setText(value);
            }

        } else if (mCurrEditItemId.equals(MenuConfigManager.TV_CHANNEL_NO)) {
            int now;
            if (value != null && !value.equals("")) {
                now = Integer.parseInt(value);
            }else {
                now = Integer.parseInt(mData[0]);
            }
            if (now < mCurrEditItem.minValue) {
                now = (int) mCurrEditItem.minValue;
            } else if (now > mCurrEditItem.maxValue) {
                now = (int) mCurrEditItem.maxValue;
            }
            //value = now + "";
            if (Integer.parseInt(mData[0]) != now) {
                if ((CommonIntegration.isEURegion() || CommonIntegration
                        .isCNRegion()) && now == 0) {// not allow to
                                                        // change to
                    // 0000 in EU
                    Toast.makeText(mContext,
                            mContext.getString(R.string.menu_dialog_numzero),
                            Toast.LENGTH_LONG).show();
                } else if (isM7Enable) {
                    Toast.makeText(
                            mContext,
                            mContext.getString(R.string.menu_tv_edit_M7_num_more4000),
                            Toast.LENGTH_LONG).show();
                } else {
                    if(CommonIntegration.isEUPARegion() && CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
                        now = now + CommonIntegration.DECREASE_NUM;
                    }
                    value = now + "";
                    if (!mHelper.isChannelDeleted(value)) {
                        mData[0] = value;
                        if (CommonIntegration.isCNRegion()) {
                            mHelper.updateChannelNumberCnRegion(mData[3],
                                    mData[0]);
                        } else {
                            mHelper.updateChannelNumber(mData[3], mData[0]);
                        }
                        ((TextView) mCurrentInfoItem
                                .findViewById(R.id.channel_info_no)).setText(value);
                    } else {
                        Toast.makeText(
                                mContext,
                                mContext.getString(R.string.menu_dialog_numrepeat),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        mDetailAdapter.setNewList(mHelper.getChannelEditDetail(mData));
    }

    // private OnFocusChangeListener onFocusChangeListener = new
    // OnFocusChangeListener() {
    //
    // @Override
    // public void onFocusChange(View v, boolean hasFocus) {
    // Log.d(TAG, "hasFocus: " + hasFocus);
    // if (mLinearLayoutTypeLayout.isShown() && mDetailAdapter != null
    // && mDetailAdapter.getCount() > 0) {
    // if (mChannelInfoTypeView.getSelectedView() != null) {
    // ImageView img = (ImageView) mChannelInfoTypeView
    // .getSelectedView().findViewById(R.id.iv_editdetail);
    // if (img != null) {
    // Log.d(TAG, "img NOT null");
    // if (hasFocus) {
    // img.setVisibility(View.VISIBLE);
    // } else {
    // img.setVisibility(View.INVISIBLE);
    // }
    // mDetailAdapter.notifyDataSetChanged();
    // }
    // }
    // }
    // }
    // };

}
