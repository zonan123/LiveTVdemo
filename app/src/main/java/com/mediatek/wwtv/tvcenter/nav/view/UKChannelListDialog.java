package com.mediatek.wwtv.tvcenter.nav.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.drawable.Drawable;

import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.InternalHandler;
import com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog.ChannelAdapter.ViewHolder;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.dvr.ui.OnDVRDialogListener;
import com.mediatek.wwtv.tvcenter.scan.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration.ChannelChangedListener;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvHtmlAgentBase;

import android.database.ContentObserver;

public class UKChannelListDialog extends NavBasicDialog implements OnDismissListener, ComponentStatusListener.ICStatusListener {

    private static final String TAG = "UKChannelListDialog";
    private String CH_TYPE = "";
    private static final int SET_SELECTION_INDEX = 0x1001;
    private static final int TYPE_CHANGECHANNEL_UP = 0x1002;
    private static final int TYPE_CHANGECHANNEL_DOWN = 0x1003;
    private static final int TYPE_CHANGECHANNEL_PRE = 0x1004;
    private static final int TYPE_CHANGECHANNEL_ENTER = 0x1005;
    private static final int TYPE_RESET_CHANNELLIST = 0x1006;
    private static final int TYPE_REGET_CHANNELLIST = 0x1007;
    public static final int MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY = 0x1008;
    private static final int TYPE_UPDATE_CHANNELLIST = 0x1009;
    public static final int DEFAULT_CHANGE_CHANNEL_DELAY_TIME = 6 * 1000;
    public static final int CHANNEL_LIST_SElECTED_FOR_TTS = 0x1010;
    public static final int FIND_CHANNELLIST = 0x1011;
    public static final int CHANGE_TYPE_CHANGECHANNEL = 0x1012;
    public static final int REFRESHTIME = 0x1013;

    private String[] types;
    private String mTitlePre;
    View mChannelListFunctionLayout;
    View mChannelListLayout;
    private ListView mChannelListView;
    private View mChannelListPageUpDownLayout;
    private View mChannelListTipView;
    private TextView mTitleText;
    private TextView mYellowKeyText;
    private TextView mBlueKeyText;
    private TextView mTitleTime;
    private ProgressBar chanelListProgressbar;
    ImageView mBlueicon;
    ImageView fvpicon;

    private CommonIntegration commonIntegration;
    private SaveValue mSaveValue;

    private List<Integer> mChannelIdList;
    private int mLastSelection;
    private static int CHANNEL_LIST_PAGE_MAX = 8;

    private static final int ALL_CHANNEL = 0;
    private static final int FAVOURITE_CHANNEL = 1;
    private static final int HD_CHANNEL = 2;
    private static final int INTERACTIVE_CHANNEL = 3;
    private static final int IPAPP_CHANNEL = 4;
    private static final int RADIO_CHANNEL = 5;
    private int CURRENT_CHANNEL_TYPE = ALL_CHANNEL;
    private boolean SELECT_TYPE_CHANGE_CH = false;
    private static final int CHANNEL_SELECTION = 0;
    private static final int CHANNEL_LIST = 1;
    private boolean mSelectionShow = false;
    private int CURRENT_CHANNEL_MODE = CHANNEL_LIST;
    private int mCurMask = CommonIntegration.CH_LIST_MASK;
    private int mCurVal = CommonIntegration.CH_LIST_VAL;

    private int msdtservicetype = -1;

    private int mCurCategories = -1;
    //each check need loop channel list,so define save it.when show check,dismiss reset.
    private boolean hasNextPage = false;
    private MtkTvPWDDialog mtkTvPwd ;
    private ChannelAdapter mChannelAdapter;
    private TIFChannelManager mTIFChannelManager;
//    private TIFChannelContentObserver mTIFChannelContentObserver;
    MtkTvAppTVBase mTvAppTvBase;
    private boolean mCanChangeChannel;
    private boolean mIsTifFunction;
    private boolean mIsTuneChannel = true;
    private int preChId;
    private int mSendKeyCode;
   // private HandlerThread mHandlerThead;
    private Handler mHandler;
    private Handler mThreadHandler;
    public static int CHANGE_TYPE_NETWORK_INDEX = 5;
    BannerImplement mBannerImplement;
    private MtkTvHtmlAgentBase mMtkTvHtmlAgentBase;

    public UKChannelListDialog(Context context, int theme) {
        super(context, theme);
        this.componentID = NAV_COMP_ID_CH_LIST;
        initHandler();
        commonIntegration = CommonIntegration.getInstance();
        CH_TYPE = CommonIntegration.CH_TYPE_BASE+commonIntegration.getSvl();
        mSaveValue = SaveValue.getInstance(context);
        mtkTvPwd = MtkTvPWDDialog.getInstance();
        mChannelIdList = new ArrayList<Integer>();
        mTIFChannelManager = TIFChannelManager.getInstance(mContext);
//      mTIFChannelContentObserver =  new TIFChannelContentObserver(new Handler());
        mTvAppTvBase = new MtkTvAppTVBase();
        mMtkTvHtmlAgentBase=new MtkTvHtmlAgentBase();
        mIsTifFunction = CommonIntegration.supportTIFFunction();
        ComponentStatusListener lister = ComponentStatusListener.getInstance();
        lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
        lister.addListener(ComponentStatusListener.NAV_COMPONENT_SHOW, this);
        lister.addListener(ComponentStatusListener.NAV_RESUME, this);
        mBannerImplement = BannerImplement.getInstanceNavBannerImplement(mContext);
//      mTIFChannelManager.getContentResolver().registerContentObserver(TvContract.Channels.CONTENT_URI, true, mTIFChannelContentObserver);
        //mTvCallbackHandler = TvCallbackHandler.getInstance();
      //  TypedValue sca = new TypedValue();
     //   mContext.getResources().getValue(R.dimen.nav_channellist_page_max,sca ,true);
     //   CHANNEL_LIST_PAGE_MAX  =(int) sca.getFloat();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_PAGE_MAX " + CHANNEL_LIST_PAGE_MAX);
        mTIFChannelManager.setCurrentChannelSort(6);
    }
    private void initHandler() {
        HandlerThread mHandlerThead = new HandlerThread(TAG);
        mHandlerThead.start();
        mThreadHandler = new Handler(mHandlerThead.getLooper());
        mHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel handler onResume()>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
                switch (msg.what) {
                case SET_SELECTION_INDEX:
                    ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_KEY_OCCUR, msg.arg1);
                    int curChId;
                    if(commonIntegration.isdualtunermode()){
                        curChId = commonIntegration.get2NDCurrentChannelId();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel handler onResume()>> isdualtunermode() " + curChId);
                    }else{
                        curChId = commonIntegration.getCurrentChannelId();
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel handler onResume()>> " + curChId);
                    if(CommonIntegration.isUSRegion()){
                        mChannelAdapter.updateData(updateCurrentChannelLlistByTIF());
                    }
                    int chIndex = mChannelAdapter.isExistCh(curChId);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SET_SELECTION_INDEX chIndex = "+chIndex);
                    if(chIndex >= 0){
                        mChannelListView.requestFocus();
                        mChannelListView.setSelection(chIndex);
                        mLastSelection = chIndex;
                    } else {
                     if(commonIntegration.isdualtunermode()){
                         if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHUP) {
                              List<TIFChannelInfo> mTifChannelList = getNextPrePageChListByTIF(false);
                              mChannelAdapter.updateData(mTifChannelList);
                            mChannelListView.requestFocus();
                            mChannelListView.setSelection(0);
                            mLastSelection = 0;
                          } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHDN) {
                              List<TIFChannelInfo> mTifChannelList = getNextPrePageChListByTIF(true);
                              mChannelAdapter.updateData(mTifChannelList);
                            mChannelListView.requestFocus();
                            mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
                            mLastSelection = mChannelAdapter.getCount() - 1;
                          } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_PRECH) {
                            int tempmask = mCurMask;
                            int tempVal = mCurVal;
                            if(mCurMask == CommonIntegration.CH_LIST_MASK && mCurVal == CommonIntegration.CH_LIST_VAL){
                                mCurMask = CommonIntegration.CH_LIST_DIGITAL_RADIO_MASK;
                                mCurVal = CommonIntegration.CH_LIST_DIGITAL_RADIO_VAL;
                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_PRECH chIndex =" + chIndex);
                            if (isSelectionMode() && !commonIntegration.checkCurChMask((mCurMask
                                & ~MtkTvChCommonBase.SB_VNET_ACTIVE), (mCurVal
                                & ~MtkTvChCommonBase.SB_VNET_ACTIVE))) {
                              exit();
                            } else {
                              List<TIFChannelInfo> chList = null;
                              if (msg.arg2 < 0) {
                                msg.arg2 = 0;
                              }
                              if (mIsTifFunction) {
                                chList = mTIFChannelManager.getTIFPreOrNextChannelList(curChId, false, true,
                                    CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                              } else {
                                List<MtkTvChannelInfoBase> tempApiChList = null;
                                if (CommonIntegration.isEURegion() && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                                  if (commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                                    tempApiChList = commonIntegration.getChannelListByMaskValuefilter(curChId,
                                        msg.arg2, CHANNEL_LIST_PAGE_MAX - msg.arg2, mCurMask, mCurVal, true);
                                  } else {
                                    tempApiChList = commonIntegration.getChannelListByMaskValuefilter(curChId,
                                        msg.arg2, CHANNEL_LIST_PAGE_MAX - msg.arg2, mCurMask, mCurVal, false);
                                  }
                                } else {
                                  tempApiChList = commonIntegration.getChList(curChId, msg.arg2,
                                      CHANNEL_LIST_PAGE_MAX - msg.arg2);
                                }
                                chList = TIFFunctionUtil.getTIFChannelList(tempApiChList);
                              }
                              mChannelAdapter.updateData(chList);
                              // commonIntegration.getChList(curChId, 0, CHANNEL_LIST_PAGE_MAX));
                              mChannelListView.requestFocus();
                              mChannelListView.setSelection(msg.arg2);
                              mLastSelection = msg.arg2;
                            }
                           mCurMask = tempmask;
                           mCurVal = tempVal;
                          }
                     }else {

                      if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHUP) {
                        if (mIsTifFunction) {
                          List<TIFChannelInfo> mTifChannelList = mTIFChannelManager
                              .getTIFPreOrNextChannelList(curChId,
                                  false, true, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                          mChannelAdapter.updateData(mTifChannelList);
                        } else {
                          List<MtkTvChannelInfoBase> tempApiChList = getChannelList(preChId,
                              MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,
                              CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                          mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(tempApiChList));
                        }
                        // commonIntegration.getChList(curChId, 0, CHANNEL_LIST_PAGE_MAX));
                        mChannelListView.requestFocus();
                        mChannelListView.setSelection(0);
                        mLastSelection = 0;
                      } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHDN) {
                        if (mIsTifFunction) {
                          List<TIFChannelInfo> mTifChannelList = mTIFChannelManager
                              .getTIFPreOrNextChannelList(curChId, true, true,
                                  CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                          mChannelAdapter.updateData(mTifChannelList);
                        } else {
                          List<MtkTvChannelInfoBase> tempApiChList = getChannelList(curChId + 1,
                              MtkTvChannelList.CHLST_ITERATE_DIR_PREV,
                              CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                          mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(tempApiChList));
                        }
                        mChannelListView.requestFocus();
                        mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
                        mLastSelection = mChannelAdapter.getCount() - 1;
                      } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_PRECH) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_PRECH chIndex =" + chIndex);
                        if (isSelectionMode() && !commonIntegration.checkCurChMask((mCurMask
                            & ~MtkTvChCommonBase.SB_VNET_ACTIVE), (mCurVal
                            & ~MtkTvChCommonBase.SB_VNET_ACTIVE))) {
                          exit();
                        } else {
                          List<TIFChannelInfo> chList = null;
                          if (msg.arg2 < 0) {
                            msg.arg2 = 0;
                          }
                          if (mIsTifFunction) {
                            chList = mTIFChannelManager.getTIFPreOrNextChannelList(curChId, false, true,
                                CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                          } else {
                            List<MtkTvChannelInfoBase> tempApiChList = null;
                            if (CommonIntegration.isEURegion() && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                              if (commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                                tempApiChList = commonIntegration.getChannelListByMaskValuefilter(curChId,
                                    msg.arg2, CHANNEL_LIST_PAGE_MAX - msg.arg2, mCurMask, mCurVal, true);
                              } else {
                                tempApiChList = commonIntegration.getChannelListByMaskValuefilter(curChId,
                                    msg.arg2, CHANNEL_LIST_PAGE_MAX - msg.arg2, mCurMask, mCurVal, false);
                              }
                            } else {
                              tempApiChList = commonIntegration.getChList(curChId, msg.arg2,
                                  CHANNEL_LIST_PAGE_MAX - msg.arg2);
                            }
                            chList = TIFFunctionUtil.getTIFChannelList(tempApiChList);
                          }

                          mChannelAdapter.updateData(chList);
                          // commonIntegration.getChList(curChId, 0, CHANNEL_LIST_PAGE_MAX));
                          mChannelListView.requestFocus();
                          mChannelListView.setSelection(msg.arg2);
                          mLastSelection = msg.arg2;
                        }
                      }
                     }
                }
                    if (mChannelAdapter != null) {
                        saveLastPosition(curChId, mChannelAdapter.getChannellist());
                    }
                    if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHUP) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_CHUP....end");
                    } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHDN) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_CHDN....end");
                    } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_PRECH) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_PRECH....end");
                    } else if(msg.arg1 == KeyEvent.KEYCODE_DPAD_CENTER){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tts_channellistselect....end");
                    }
                    mCanChangeChannel = true;
                    break;
                case TYPE_CHANGECHANNEL_UP:
                    mThreadHandler.post(mChannelUpRunnable);
                    break;
                case TYPE_CHANGECHANNEL_DOWN:
                    mThreadHandler.post(mChannelDownRunnable);
                    break;
                case TYPE_CHANGECHANNEL_PRE:
                    mThreadHandler.post(mChannelPreRunnable);
                    break;
                case TYPE_CHANGECHANNEL_ENTER:
                    CommonIntegration.getInstance().selectChannelById(msg.arg1);
                    break;
                case CHANGE_TYPE_CHANGECHANNEL:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANGE_TYPE_CHANGECHANNEL select first channel in the chlist");
                    if (mChannelListView.getAdapter() != null && mChannelAdapter.getChannellist().size() > 0) {
                       if(mCurCategories == -1){
                           mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                       }
                       mTIFChannelManager.selectChannelByTIFInfo(mChannelAdapter.getChannellist().get(0));
                       mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                       Message mess = Message.obtain();
                       mess.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                       mess.arg1 = R.id.nav_channel_listview;
                       mess.arg2 = 0;
                       mHandler.sendMessageDelayed(mess, 1000);
                    }

                    break;
                case TYPE_RESET_CHANNELLIST:
                    int selection = 0;
                    List<TIFChannelInfo> tempChlist = (List<TIFChannelInfo>)msg.obj;
                    if(chanelListProgressbar != null && chanelListProgressbar.getVisibility() == View.VISIBLE){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " chanelListProgressbar.GONE; ");
                        chanelListProgressbar.setVisibility(View.GONE);
                    }
                    mCanChangeChannel = true;
                    mChannelListView.setVisibility(View.VISIBLE);
                    if (tempChlist == null  || tempChlist.isEmpty()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChlist = null");
                        mChannelListView.setAdapter(null);
                        mTitleText.setFocusable(true);
                        mTitleText.requestFocus();
                        mBlueKeyText.setVisibility(View.INVISIBLE);
                        mBlueicon.setVisibility(View.INVISIBLE);
                    }else{
                        mChannelAdapter = new ChannelAdapter(mContext, tempChlist);
                        int index = mChannelAdapter.isExistCh(msg.arg1);
                        selection =  index< 0 ? mLastSelection : index;
                        if (selection > mChannelAdapter.getCount() - 1) {
                            selection = 0;
                        }
                        if (selection < 0){
                            selection = 0;
                        }
                        mLastSelection = selection;
                        if (mChannelAdapter != null) {
                            saveLastPosition(commonIntegration.getCurrentChannelId(), mChannelAdapter.getChannellist());
                        }
                        mChannelListView.setAdapter(mChannelAdapter);
                        mChannelListView.setFocusable(true);
                        mChannelListView.requestFocus();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChlist = " + tempChlist.size());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_RESET_CHANNELLIST index :"+index+" selection :"+selection);
                        if(index >-1){
                            mChannelListView.setSelection(selection);
                        }
                        if(!TIFFunctionUtil.checkChMask(commonIntegration.getCurChInfo(), CommonIntegration.CH_FAKE_MASK, CommonIntegration.CH_FAKE_VAL)){
                            mChannelListView.setFocusable(true);
                            mChannelListView.requestFocus();
                            mChannelListView.setSelection(selection);
                        }else{
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initHandler current channel is fake,no focus in the channel list ,channel name is "+commonIntegration.getCurChInfo().getServiceName());

                        }
                        if(SELECT_TYPE_CHANGE_CH){
                            SELECT_TYPE_CHANGE_CH =false;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey : change type then change chanenls ");
                            mHandler.sendEmptyMessage(CHANGE_TYPE_CHANGECHANNEL);
                        }
                        mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                        Message msglist = Message.obtain();
                        msglist.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                        msglist.arg1 = R.id.nav_channel_listview;
                        msglist.arg2 = selection;
                        mHandler.sendMessage(msglist);

                        if(selection < tempChlist.size() && tempChlist.get(selection).mDataValue != null && tempChlist.get(selection).mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                            mBlueKeyText.setVisibility(View.VISIBLE);
                            mBlueicon.setVisibility(View.VISIBLE);
                            if(tempChlist.get(selection).mMtkTvChannelInfo != null && tempChlist.get(selection).mMtkTvChannelInfo.isDigitalFavoritesService()){
                                mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_remove_fav));
                            }else if (CURRENT_CHANNEL_TYPE != FAVOURITE_CHANNEL){
                                mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_add_fav));
                            }
                        }else{
                            mBlueKeyText.setVisibility(View.INVISIBLE);
                            mBlueicon.setVisibility(View.INVISIBLE);
                        }
                    }
                    showPageUpDownView();
                    startTimeout(NAV_TIMEOUT_10);
                    break;
                case TYPE_REGET_CHANNELLIST:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_REGET_CHANNELLIST>isShowing()>>" + isShowing()+ ">>" + mSelectionShow);
                    if (isShowing() && !mSelectionShow) {
                        if (mIsTifFunction) {
                            if (mChannelAdapter != null) {
                                mThreadHandler.post(mUpdateChannelListRunnable);
                            }
                        }
                    }
                    break;
                case TYPE_UPDATE_CHANNELLIST:
                    List<TIFChannelInfo> tempUpdateChlist = (List<TIFChannelInfo>)msg.obj;
                    if (tempUpdateChlist != null && mChannelAdapter != null) {
                        mChannelAdapter.updateData(tempUpdateChlist);
                        showPageUpDownView();
                        if (mChannelListTipView.getVisibility() != View.VISIBLE) {
                            mChannelListTipView.setVisibility(View.VISIBLE);
                        }

                    }
                    if(chanelListProgressbar != null && chanelListProgressbar.getVisibility() == View.VISIBLE){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " chanelListProgressbar.GONE; ");
                        chanelListProgressbar.setVisibility(View.GONE);
                    }
                    break;
                case MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY reach");
                    mCanChangeChannel = true;
                    break;
                case CHANNEL_LIST_SElECTED_FOR_TTS:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_SElECTED_FOR_TTS data.parama1  = " + msg.arg1);
                    switch(msg.arg1){
                    default :
                        mChannelListView.setSelection(msg.arg2);
                        break;
                    }
                    startTimeout(NAV_TIMEOUT_10);
                    break;
                case TvCallbackConst.MSG_CB_SVCTX_NOTIFY:
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "data.parama1  = " + data.param1);
                    break;
                case REFRESHTIME:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "REFRESHTIME");
                    mTitleTime.setText(mBannerImplement.getCurrentTime());
                    mHandler.sendEmptyMessageDelayed(REFRESHTIME, 1*1000);
                    break;
                default:
                    break;
                }
            }

        };
    }

    private Runnable mResetChannelListRunnable = new Runnable() {

        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mResetChannelListRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
            List<TIFChannelInfo> tempList=null;
            int chId = TIFFunctionUtil.getCurrentChannelId();
            if(commonIntegration.is3rdTVSource() || (mCurMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && mCurVal ==CommonIntegration.CH_LIST_3RDCAHNNEL_VAL)){
            //  chId=(int)(mTIFChannelManager.getChannelInfoByUri()).mId;
                TIFChannelInfo mTIFChannelInfo=mTIFChannelManager.getChannelInfoByUri();
                if(mTIFChannelInfo !=null && mTIFChannelInfo.mMtkTvChannelInfo ==null ){
                    chId=(int) mTIFChannelInfo.mId;
                }
            //  tempList=getAllChannelListByTIFFor3rdSource(chId);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mResetChannelListRunnable>> chId " + chId);
            tempList = processTIFChListWithThread(chId);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mResetChannelListRunnable>>time" );

            Message msg = Message.obtain();
            msg.what = TYPE_RESET_CHANNELLIST;
            msg.arg1 = chId;
            msg.obj = tempList;
            mHandler.sendMessage(msg);
        }
    };

    private Runnable mUpdateChannelListRunnable = new Runnable() {

        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mUpdateChannelListRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
            List<TIFChannelInfo> tempList =null;
            //tempList = updateCurrentChannelLlistByTIF();
            int chId = TIFFunctionUtil.getCurrentChannelId();
            if(commonIntegration.is3rdTVSource() || (mCurMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && mCurVal ==CommonIntegration.CH_LIST_3RDCAHNNEL_VAL)){
                //  chId=(int)(mTIFChannelManager.getChannelInfoByUri()).mId;
                TIFChannelInfo mTIFChannelInfo=mTIFChannelManager.getChannelInfoByUri();
                if(mTIFChannelInfo !=null && mTIFChannelInfo.mMtkTvChannelInfo ==null ){
                    chId=(int) mTIFChannelInfo.mId;
                }
                //  tempList=getAllChannelListByTIFFor3rdSource(chId);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mResetChannelListRunnable>> chId " + chId);
            tempList = processTIFChListWithThread(chId);
            Message msg = Message.obtain();
            msg.what = TYPE_UPDATE_CHANNELLIST;
            msg.obj = tempList;
            mHandler.sendMessage(msg);
        }
    };

    private Runnable mChannelUpRunnable = new Runnable() {

        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mChannelUpRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName() +" mIsTuneChannel "+mIsTuneChannel);
           if(mCanChangeChannel){
               mCanChangeChannel = false;
               if (mIsTuneChannel) {
                   mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHUP;
                   if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
                       com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TIMESHIFT_START KEYCODE_MTKIR_CHUP");

                       ((Activity)mContext).runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               dismiss();
                           }
                         });
                       showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,mSendKeyCode,DvrDialog.TYPE_Timeshift,-1);
                       mCanChangeChannel = true;
                   }else{
                       if (channelUpDown(true)) {
                           mCanChangeChannel = true;
                           ((Activity)mContext).runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mChannelUpRunnable>>channelUpDown ");
                                   dismiss();
                               }
                             });
                       }
                   }
               } else {
                   if (channelUpDown(true)) {
                       ((Activity)mContext).runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               dismiss();
                           }
                         });

                   } else {
                       mCanChangeChannel = true;
                   }
               }
           }
        }
    };

    private Runnable mChannelPreRunnable = new Runnable() {

        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mChannelPreRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
            if(mCanChangeChannel){
                mCanChangeChannel = false;
                if (mIsTuneChannel) {
                    mSendKeyCode = KeyMap.KEYCODE_MTKIR_PRECH;
                    if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TIMESHIFT_START KEYCODE_MTKIR_PRECH");
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismiss();
                            }
                          });
                        showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,mSendKeyCode,DvrDialog.TYPE_Timeshift,-1);
                        mCanChangeChannel = true;
                    }else{
                        if (mTIFChannelManager.channelPre()) {
                            mCanChangeChannel = true;
                            ((Activity)mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismiss();
                                }
                              });
                        }

                    }
                } else {
                    int seletcPostion = mChannelListView.getSelectedItemPosition();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChannelPreRunnable seletcPostion = "+seletcPostion);
                    if (commonIntegration.channelPre()) {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismiss();
                            }
                          });
                       /* Message msg = Message.obtain();
                        msg.arg1 = KeyMap.KEYCODE_MTKIR_PRECH;
                        msg.arg2 = seletcPostion;
                        msg.what = SET_SELECTION_INDEX;
                        mHandler.sendMessage(msg);*/
                    } else {
                        mCanChangeChannel = true;
                    }
                }
            }
        }
    };

    private Runnable mChannelDownRunnable = new Runnable() {

        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mChannelDownRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
            if(mCanChangeChannel){
                mCanChangeChannel = false;
                if (mIsTuneChannel) {
                    mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHDN;
                    if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TIMESHIFT_START KEYCODE_MTKIR_CHDN");
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismiss();
                            }
                          });
                        showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,mSendKeyCode,DvrDialog.TYPE_Timeshift,-1);
                        mCanChangeChannel = true;
                    }else{
                       if (channelUpDown(false)) {
                           mCanChangeChannel = true;
                           ((Activity)mContext).runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mChannelUpRunnable>> channelUpDown ");
                                   dismiss();
                               }
                             });
                      }
                    }
                  } else {
                    if (channelUpDown(false)) {
                        Message msg = Message.obtain();
                        msg.arg1 = KeyMap.KEYCODE_MTKIR_CHDN;
                        msg.what = SET_SELECTION_INDEX;
                        mHandler.sendMessage(msg);
                    } else {
                        mCanChangeChannel = true;
                    }
                }
            }
        }
    };
    public UKChannelListDialog(Context context) {
        this(context, R.style.nav_dialog);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Constructor!");
    }


    private void  initTypes(){
        if (MarketRegionInfo.F_3RD_INPUTS_SUPPORT){
            types = mContext.getResources().getStringArray(R.array.nav_uk_channellist_type);
        }else{
            types = mContext.getResources().getStringArray(R.array.nav_uk_channellist_type_ip_channel);
        }
    }

    public int getChannelNetworkIndex() {
        initTypes();
        return IPAPP_CHANNEL;
    }

    private void initMaskAndSatellites() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initMaskAndSatellites ");
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_CN:
        case MarketRegionInfo.REGION_EU:
            CURRENT_CHANNEL_MODE = CHANNEL_SELECTION;
            CURRENT_CHANNEL_TYPE = mSaveValue.readValue(CH_TYPE, ALL_CHANNEL);
            initTypes();
            break;
        case MarketRegionInfo.REGION_US:
        case MarketRegionInfo.REGION_SA:
            CURRENT_CHANNEL_MODE = CHANNEL_SELECTION;
            initTypes();
            break;
        default :
            break;

        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSelectionMode CURRENT_CHANNEL_MODE ="
                + CURRENT_CHANNEL_MODE);
        if (isSelectionMode()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_TYPE>>>" + CURRENT_CHANNEL_TYPE);
            resetMask();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
        setContentView(R.layout.nav_duk_channellist);
        setWindowPosition();
        findViews();
    }

    protected void onStart() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart");
        mChannelListView.setVisibility(View.VISIBLE);
        this.startTimeout(NAV_TIMEOUT_10);
        super.onStart();
    }

    @Override
    public boolean isCoExist(int componentID) {
        boolean isCoExist = false;
        switch (componentID) {
        case NAV_COMP_ID_POP:
            isCoExist = true;
            break;
        case NAV_COMP_ID_BANNER:
            return false;
        default :
            break;
        }
        return isCoExist;
    }

    public class TIFChannelContentObserver extends ContentObserver {

        public TIFChannelContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIFChannelContentObserver onChange>selfChange>>" + selfChange);
            if (mIsTifFunction && UKChannelListDialog.this.isShowing()) {
                handleCallBack();
            }
        }

    }

    public void handleCallBack(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCallBack()");
        if (mSelectionShow) {
            return;
        }
        mHandler.removeMessages(TYPE_REGET_CHANNELLIST);
        mHandler.sendEmptyMessageDelayed(TYPE_REGET_CHANNELLIST, 1500);
    }

    public void handleUpdateCallBack(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUpdateCallBack()");
        mChannelListView.invalidateViews();
    }

   /**
    * true (up) ,false (down)
    */
     public boolean channelUpDown(boolean isUp){
        Log.d(TAG,"channelUpDown isUp = "+ isUp);
        initMaskAndSatellites();
        if(isUp){
            if (mIsTifFunction) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                 if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                     final int tempchid= commonIntegration.get2NDCurrentChannelId();
                     List<MtkTvChannelInfoBase> channelinfolist=null;
                     channelinfolist= commonIntegration.getChListByMask(tempchid, MtkTvChannelList.CHLST_ITERATE_DIR_NEXT, 1, mCurMask, mCurVal);
                     if(channelinfolist == null || channelinfolist.isEmpty()){
                             return false;
                     }
                     TurnkeyUiMainActivity.getInstance().getPipView().tune(InputSourceManager.getInstance().getTvInputInfo("sub").getId(), MtkTvTISMsgBase.createSvlChannelUri(channelinfolist.get(0).getChannelId()));
                     int  channelid= commonIntegration.get2NDCurrentChannelId();
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUp channelid = "+channelid);
                     return true;
                  }else{
                        return mTIFChannelManager.dukChannelUpDownByMask(isUp, mCurMask,mCurVal,msdtservicetype);
                  }
            }
        }else{
            if (mIsTifFunction) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                  if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                         final int tempchid= commonIntegration.get2NDCurrentChannelId();
                         List<MtkTvChannelInfoBase> channelinfolist=null;
                         channelinfolist= commonIntegration.getChListByMask(tempchid, MtkTvChannelList.CHLST_ITERATE_DIR_PREV,1, mCurMask, mCurVal);
                         if(channelinfolist == null || channelinfolist.isEmpty()){
                             return false;
                         }
                         TurnkeyUiMainActivity.getInstance().getPipView().tune(InputSourceManager.getInstance().getTvInputInfo("sub").getId(), MtkTvTISMsgBase.createSvlChannelUri(channelinfolist.get(0).getChannelId()));
                         int  channelid= commonIntegration.get2NDCurrentChannelId();
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUp channelid = "+channelid);
                         return true;
                  }else{
                     return mTIFChannelManager.dukChannelUpDownByMask(isUp, mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE,mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE,msdtservicetype);
               }
            }
        }
        return false;
    }

    private void resetMask(){
        int mask = CommonIntegration.CH_LIST_MASK;
        int val = CommonIntegration.CH_LIST_VAL;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"resetChList CURRENT_CHANNEL_TYPE = "+ CURRENT_CHANNEL_TYPE +"CURRENT_CHANNEL_MODE ="+CURRENT_CHANNEL_MODE);
        if(isSelectionMode()){

            switch(CURRENT_CHANNEL_TYPE){
            case ALL_CHANNEL:
                mask = CommonIntegration.CH_LIST_MASK;
                val = CommonIntegration.CH_LIST_VAL;
                msdtservicetype = -1;
                break;
            case FAVOURITE_CHANNEL:
                mask = CommonIntegration.CH_LIST_FAV1_MASK;
                val = CommonIntegration.CH_LIST_FAV1_VAL;
                msdtservicetype = -1;
                break;
            case HD_CHANNEL:
                mask = CommonIntegration.CH_LIST_MASK;
                val = CommonIntegration.CH_LIST_VAL;
                msdtservicetype = CommonIntegration.CH_SDT_SERVICE_TYPE_HD;
                break;
            case INTERACTIVE_CHANNEL:
                mask = CommonIntegration.CH_LIST_MASK;
                val = CommonIntegration.CH_LIST_VAL;
                msdtservicetype = CommonIntegration.CH_SDT_SERVICE_TYPE_INTERACTIVE;
                break;
            case IPAPP_CHANNEL:
                if (!MarketRegionInfo.F_3RD_INPUTS_SUPPORT){
                    mask = CommonIntegration.CH_LIST_MASK;
                    val = CommonIntegration.CH_LIST_VAL;
                    msdtservicetype = CommonIntegration.SVL_SERVICE_TYPE_IP_SVC;
                }else {
                    mask = CommonIntegration.CH_LIST_3RDCAHNNEL_MASK;
                    val = CommonIntegration.CH_LIST_3RDCAHNNEL_MASK;
                    msdtservicetype = -1;
                }
                break;
            case RADIO_CHANNEL:
                mask = CommonIntegration.CH_LIST_MASK;
                val = CommonIntegration.CH_LIST_VAL;
                msdtservicetype = CommonIntegration.CH_SDT_SERVICE_TYPE_RADIO;
                break;
            default :
                mask = CommonIntegration.CH_LIST_MASK;
                val = CommonIntegration.CH_LIST_VAL;
                msdtservicetype = -1;
                break;
            }

        }
        mCurMask = mask;
        mCurVal = val;
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMask,mCurMask);
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMaskvalue,mCurVal);
        mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
        Log.d(TAG, "resetMask mCurMask ="+mCurMask+" mCurVal: "+mCurVal);
    }
    private boolean isSelectionMode(){
        return CURRENT_CHANNEL_MODE == CHANNEL_SELECTION? true: false;
    }
    private void resetCurrentChanenlIcon() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetCurrentChanenlIcon");
        if(mChannelAdapter != null && mChannelAdapter.getCount() >0){
            List<TIFChannelInfo> channellist=mChannelAdapter.getChannellist();
            TIFChannelInfo favchannel=  channellist.get(mChannelListView.getSelectedItemPosition());
            favchannel.mMtkTvChannelInfo = FavChannelManager.getInstance(mContext).favAddOrErase(favchannel.mInternalProviderFlag3);
            channellist.set(mChannelListView.getSelectedItemPosition(), favchannel);
            mChannelAdapter.updateData(channellist);
            if(favchannel.mMtkTvChannelInfo != null && favchannel.mMtkTvChannelInfo.isDigitalFavoritesService()){
                mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_remove_fav));
            }else if (CURRENT_CHANNEL_TYPE != FAVOURITE_CHANNEL){
                mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_add_fav));
            }

        }
      }

     @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        boolean isHandled = true;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyHandler keyCode 111= "+keyCode);
        if (mContext == null || !mCanChangeChannel) {
            return false;
        }
        keyCode = KeyMap.getKeyCode(keyCode, event);
        this.startTimeout(NAV_TIMEOUT_10);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyHandler keyCode 222= "+keyCode);

        switch (keyCode) {
            case KeyMap.KEYCODE_BACK:
            case KeyMap.KEYCODE_YEN: {
                exit();
                break;
            }
        case KeyMap.KEYCODE_MTKIR_BLUE:
            if(mBlueKeyText.getVisibility() == View.VISIBLE){
                resetCurrentChanenlIcon();
            }
            break;
        case KeyMap.KEYCODE_MTKIR_YELLOW:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_YELLOW");
            dismiss();
            String jsonCmd="{\"subPage\":\"player\"}";
            mMtkTvHtmlAgentBase.goToPage(MtkTvHtmlAgentBase.HTML_AGENT_PAGE_MINI_GUIDE, jsonCmd);
            break;
        case KeyMap.KEYCODE_MTKIR_RED:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED");
           if( mChannelListPageUpDownLayout.getVisibility() != View.VISIBLE){
               break;
           }
            if (mChannelListView.getSelectedItemPosition() != 0) {
                mChannelListView.requestFocus();
                mChannelListView.setSelection(0);
            } else if (hasNextPage) {
                if (mIsTifFunction) {
                    mChannelAdapter.updateData(getNextPrePageChListByTIF(false));
                } else {
                    mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(false)));
                }
                mChannelListView.requestFocus();
                mChannelListView.setSelection(0);
            }

            break;
        case KeyMap.KEYCODE_MTKIR_GREEN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_GREEN");
            if( mChannelListPageUpDownLayout.getVisibility() != View.VISIBLE){
               break;
            }
            if (mChannelListView.getSelectedItemPosition() != mChannelAdapter.getCount() - 1) {
                mChannelListView.requestFocus();
                mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
            } else if (hasNextPage) {
                // mChannelDetailLayout.setVisibility(View.INVISIBLE);
                if (mIsTifFunction) {
                    mChannelAdapter.updateData(getNextPrePageChListByTIF(true));
                } else {
                    mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(true)));
                }
                mChannelListView.requestFocus();
                mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
            }
            break;
        case KeyMap.KEYCODE_MTKIR_CHUP:
            if (mCanChangeChannel) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
                mHandler.sendEmptyMessageDelayed(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
                preChId = commonIntegration.getCurrentChannelId();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_CHUP....start");
                mThreadHandler.post(mChannelUpRunnable);
            }
            break;
        case KeyMap.KEYCODE_MTKIR_PRECH:
            if(commonIntegration.is3rdTVSource()){
                return false;
            }
            if (mCanChangeChannel) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
                mHandler.sendEmptyMessageDelayed(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_PRECH....start");
                mThreadHandler.post(mChannelPreRunnable);
            }
            break;
        case KeyMap.KEYCODE_MTKIR_CHDN:
            if (mCanChangeChannel) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY)  ;
                mHandler.sendEmptyMessageDelayed(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_CHDN....start");
                mThreadHandler.post(mChannelDownRunnable);
            }
            break;

        case KeyMap.KEYCODE_MTKIR_STOP:
            mSendKeyCode = KeyMap.KEYCODE_MTKIR_STOP;
            isHandled = false;
            break;
        case KeyMap.KEYCODE_DPAD_LEFT:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_LEFT");
            if(CURRENT_CHANNEL_TYPE > 0){
                CURRENT_CHANNEL_TYPE -= 1;
            }else{
                CURRENT_CHANNEL_TYPE =types.length-1;
            }
            //if (!MarketRegionInfo.F_3RD_INPUTS_SUPPORT && CURRENT_CHANNEL_TYPE == IPAPP_CHANNEL){
                //CURRENT_CHANNEL_TYPE = INTERACTIVE_CHANNEL;
            //}
            resetMask();
            resetChListByTIF();
            break;
        case KeyMap.KEYCODE_DPAD_RIGHT:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_RIGHT");
            if(CURRENT_CHANNEL_TYPE < types.length-1){
                CURRENT_CHANNEL_TYPE += 1;
            }else{
                CURRENT_CHANNEL_TYPE = 0;
            }
            //if (!MarketRegionInfo.F_3RD_INPUTS_SUPPORT && CURRENT_CHANNEL_TYPE == IPAPP_CHANNEL){
                //CURRENT_CHANNEL_TYPE = RADIO_CHANNEL;
            //}
            resetMask();
            resetChListByTIF();
            break;
        case KeyMap.KEYCODE_MTKIR_FREEZE:
        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        case KeyMap.KEYCODE_MTKIR_RECORD:
            dismiss();
            isHandled = false;
            break;
        case KeyMap.KEYCODE_DPAD_CENTER:
            isHandled = true;
            break;
          default:

              isHandled = false;
              break;
        }
        if (isHandled == false && TurnkeyUiMainActivity.getInstance() != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
            return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
        }
        return isHandled;
    }

    /**
        * true (up) ,false (down)
        */
         public boolean channelUpDownNoVisible(boolean isUp){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"channelUpDown isUp = "+ isUp);
                if (mIsTifFunction) {
                    return mTIFChannelManager.dukChannelUpDownByMask(isUp, mCurMask, mCurVal,msdtservicetype);
                }
                return false;
        }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
       return onKeyHandler(keyCode, event,false);
    };

    //init view param
    private void findViews() {
        mChannelListLayout = findViewById(R.id.nav_channellist);
        mChannelListView = (ListView) findViewById(R.id.nav_channel_listview);
        mChannelListTipView = findViewById(R.id.nav_channel_list_tip);
        mChannelListFunctionLayout = findViewById(R.id.nav_page_function);
        mChannelListPageUpDownLayout = findViewById(R.id.nav_page_up_down);
        mTitleText = (TextView)findViewById(R.id.nav_channel_list_title);
        mTitleTime = (TextView)findViewById(R.id.nav_channel_list_time);
        mYellowKeyText = (TextView)findViewById(R.id.channel_nav_select_list);
        mBlueKeyText = (TextView)findViewById(R.id.channel_nav_exit);
        mBlueicon = (ImageView)findViewById(R.id.channel_nav_exit_icon);
        fvpicon =  (ImageView)findViewById(R.id.nav_channel_list_fvp_icon);
        chanelListProgressbar =(ProgressBar)findViewById(R.id.nav_channel_list_progressbar);
    }

    private List<TIFChannelInfo> updateCurrentChannelLlistByTIF() {
        List<TIFChannelInfo> mTifChannelList = new ArrayList<TIFChannelInfo>();
        TIFChannelInfo chInfo = null;
        int preNum = 0;
        int startChannelId = -1;
        preNum = mTIFChannelManager.getChannelListConfirmLength(
                CHANNEL_LIST_PAGE_MAX + 1, mCurMask, mCurVal);
        hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIF SATE hasNextPage =" + hasNextPage);
        if (hasNextPage) {
            int index = 0;
            if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
                for (int i = 0; i < mChannelAdapter.getCount(); i++) {
                    chInfo = mChannelAdapter.getItem(i);
                    if (chInfo != null) {
                        if (commonIntegration.is3rdTVSource()) {
                            chInfo = mTIFChannelManager
                                    .getTIFChannelInfoById((int) chInfo.mId);
                            if (chInfo != null) {
                                startChannelId = (int) chInfo.mId;
                                index = i;
                                break;
                            }
                        } else {
                            chInfo = mTIFChannelManager
                                    .getTIFChannelInfoById(chInfo.mInternalProviderFlag3);
                            if (chInfo != null) {
                                startChannelId = chInfo.mInternalProviderFlag3;
                                index = i;
                                break;
                            }
                        }

                    }
                }
                List<TIFChannelInfo> mPreChannelList = null;
                List<TIFChannelInfo> mNextChannelList = null;
                if (index > 0) {
                    mPreChannelList = mTIFChannelManager
                            .getTIFPreOrNextChannelList(startChannelId, true,
                                    false, index, mCurMask, mCurVal);
                }
                if (chInfo == null || startChannelId == -1) {
                    startChannelId = commonIntegration.getCurrentChannelId();
                    chInfo = mTIFChannelManager
                            .getTIFChannelInfoById(startChannelId);
                }
                if (CommonIntegration.isEURegion()
                        && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                    if (commonIntegration.checkChMask(chInfo.mMtkTvChannelInfo,
                            mCurMask, mCurVal)) {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextChannelList(startChannelId,
                                        false, true, CHANNEL_LIST_PAGE_MAX
                                                - index, mCurMask, mCurVal);
                    } else {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextChannelList(startChannelId,
                                        false, false, CHANNEL_LIST_PAGE_MAX
                                                - index, mCurMask, mCurVal);
                    }
                } else {
                    if (commonIntegration.checkChMask(chInfo.mMtkTvChannelInfo,
                            mCurMask, mCurVal)
                            || CommonIntegration.isUSRegion()) {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextChannelList(startChannelId,
                                        false, true, CHANNEL_LIST_PAGE_MAX
                                                - index, mCurMask, mCurVal);
                    } else {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextChannelList(startChannelId,
                                        false, false, CHANNEL_LIST_PAGE_MAX
                                                - index, mCurMask, mCurVal);
                    }
                }
                if (mPreChannelList != null) {
                    mTifChannelList.addAll(mPreChannelList);
                }
                if (mNextChannelList != null) {
                    mTifChannelList.addAll(mNextChannelList);
                }
                while (mTifChannelList.size() > CHANNEL_LIST_PAGE_MAX) {
                    mTifChannelList.remove(mTifChannelList.size() - 1);
                }
            }
        } else if (preNum > 0) {
            if (commonIntegration.checkCurChMask(mCurMask, mCurVal)
                    || CommonIntegration.isUSRegion()) {
                mTifChannelList = mTIFChannelManager
                        .getTIFPreOrNextChannelList(-1, false, true,
                                CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
            } else {
                mTifChannelList = mTIFChannelManager
                        .getTIFPreOrNextChannelList(-1, false, false,
                                CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
            }
        }
        return mTifChannelList;
    }

    private synchronized void resetChListByTIF(){
        mSelectionShow = false;
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setChannelListTitle();
            }
        });
        mThreadHandler.post(mResetChannelListRunnable);
    }

    private List<TIFChannelInfo> processTIFChListWithThread(int chId) {
        int preNum = 0;
        List<TIFChannelInfo> mTifChannelList;
        preNum = mTIFChannelManager.getDukChannelListConfirmLength(
                    CHANNEL_LIST_PAGE_MAX + 1, mCurMask, mCurVal,msdtservicetype);
        hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIF SATE hasNextPage =" + hasNextPage);
        if (hasNextPage) {
            int index = currentChannelInListIndex(chId);
            mTifChannelList = new ArrayList<TIFChannelInfo>();
            List<TIFChannelInfo> mPreChannelList = null;
            List<TIFChannelInfo> mNextChannelList = null;
            if (index != -1
                    && commonIntegration.checkDukCurChMask(mCurMask, mCurVal,msdtservicetype)) {
                if (index > 0) {
                    mPreChannelList = mTIFChannelManager
                            .getTIFPreOrNextDukChannelList(chId, true, false,
                                    index, mCurMask, mCurVal,msdtservicetype);
                }
                if (CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                    if (commonIntegration.checkDukCurChMask(mCurMask, mCurVal,msdtservicetype)) {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, false, true,
                                        CHANNEL_LIST_PAGE_MAX - index,
                                        mCurMask, mCurVal,msdtservicetype);
                    } else {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, false, false,
                                        CHANNEL_LIST_PAGE_MAX - index,
                                        mCurMask, mCurVal,msdtservicetype);
                    }
                } else {
                    mNextChannelList = mTIFChannelManager
                            .getTIFPreOrNextDukChannelList(chId, false, true,
                                    CHANNEL_LIST_PAGE_MAX - index, mCurMask,
                                    mCurVal,msdtservicetype);
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIF SATE mLastSelection =" + mLastSelection);
                if (mLastSelection < 0) {
                    mLastSelection = 0;
                }
                if (CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                    if (commonIntegration.checkDukCurChMask(mCurMask, mCurVal,msdtservicetype)) {
                        if (mLastSelection > 0) {
                            mPreChannelList = mTIFChannelManager
                                    .getTIFPreOrNextDukChannelList(chId, true,
                                            false, mLastSelection, mCurMask,
                                            mCurVal,msdtservicetype);
                        }
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, false, true,
                                        CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                        mCurMask, mCurVal,msdtservicetype);
                    } else {
                        mLastSelection = 0;// set selection to first
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, false, false,
                                        CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                        mCurMask, mCurVal,msdtservicetype);
                    }
                } else {
                    if (mLastSelection > 0) {
                        mPreChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, true, false,
                                        mLastSelection, mCurMask, mCurVal,msdtservicetype);
                    }
                    if (commonIntegration.checkDukCurChMask(mCurMask, mCurVal,msdtservicetype)) {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, false, true,
                                        CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                        mCurMask, mCurVal,msdtservicetype);
                    } else {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextDukChannelList(chId, false, false,
                                        CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                        mCurMask, mCurVal,msdtservicetype);
                    }
                }
            }
            if (mPreChannelList != null) {
                mTifChannelList.addAll(mPreChannelList);
            }
            if (mNextChannelList != null) {
                mTifChannelList.addAll(mNextChannelList);
            }
            while (mTifChannelList.size() > CHANNEL_LIST_PAGE_MAX) {
                mTifChannelList.remove(mTifChannelList.size() - 1);
            }
        } else if (preNum > 0) {
            if (commonIntegration.checkDukCurChMask(mCurMask, mCurVal,msdtservicetype)) {
                mTifChannelList = mTIFChannelManager
                        .getTIFPreOrNextDukChannelList(-1, false, true,
                                CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal,msdtservicetype);
            } else {
                mLastSelection = 0;// set selection to first
                mTifChannelList = mTIFChannelManager
                        .getTIFPreOrNextDukChannelList(-1, false, false,
                                CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal,msdtservicetype);
            }
        } else {
            mTifChannelList = null;
        }

        return mTifChannelList;
    }

    private synchronized void resetChList(){
        mSelectionShow = false;
        ((Activity)mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(commonIntegration.is3rdTVSource()){
                    mTitleText.setText(R.string.nav_channel_list);
                    mYellowKeyText.setText("");
                }else{
                  if(isSelectionMode()){
                    if (CommonIntegration.isEURegion()) {
                        mTitleText.setText(mTitlePre + types[CURRENT_CHANNEL_TYPE]);
                        mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_select_list));
                    } else {
                        mTitleText.setText(R.string.nav_channel_list);
                        mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_select_list_cn));
                    }
                  }else{
                    mTitleText.setText(R.string.nav_channel_list);
                 }
                }
             }
        });
        new Thread(new Runnable() {

            @Override
            public void run() {
                int chId;
                if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && commonIntegration.isDualTunerEnable() && commonIntegration.isCurrentSourceTv()){
                     chId= commonIntegration.get2NDCurrentChannelId();
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetChList dual tuner chId>> = "+chId);
                  }else{
                      chId = commonIntegration.getCurrentChannelId();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetChList chId>> = "+chId);
                  }

                List<TIFChannelInfo> tempList = processListWithThread(chId);
                Message msg = Message.obtain();
                msg.what = TYPE_RESET_CHANNELLIST;
                msg.arg1 = chId;
                msg.obj = tempList;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    public boolean channelPre(){
        Log.d(TAG,"channelPre start");
        initMaskAndSatellites();

        TIFChannelInfo tIFChannelInfo = mTIFChannelManager.getTIFChannelInfoById(commonIntegration
                .getLastChannelId());
        int lastId = commonIntegration.getLastChannelId();
        int currentId = commonIntegration.getCurrentChannelId();
        if (tIFChannelInfo == null || lastId == currentId) {
            return false;
        }

        MtkTvChannelInfoBase tempApiChannel =  mTIFChannelManager.getAPIChannelInfoByChannelId(lastId);
        TIFChannelInfo tempChannel = TIFChannelManager.getInstance(mContext).getTIFChannelInfoById(lastId);
        //int mCurCategories = TIFFunctionUtil.getmCurCategories();
        if (commonIntegration.isOperatorContain() || commonIntegration.isTELEKARTAContain()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre  >>>"+commonIntegration.isOperatorContain()+" pp:"+commonIntegration.isTELEKARTAContain());
            if (mCurCategories != -1){
                return TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories());
            }else{
                return TIFFunctionUtil.checkChMaskForDuk(tempChannel, mCurMask,mCurVal,msdtservicetype);
            }
        }else{
            return TIFFunctionUtil.checkChMaskForDuk(tempChannel, mCurMask,mCurVal,msdtservicetype);
        }
    }

    private List<TIFChannelInfo> processListWithThread(int chId) {
        List<TIFChannelInfo> tempChlist = null;
        List<MtkTvChannelInfoBase> tempApiChList = null;
        int preNum = 0;
            preNum = commonIntegration.hasNextPageChannel(CHANNEL_LIST_PAGE_MAX+1, mCurMask,mCurVal);
            hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasNextPage ="+hasNextPage + "    chId>>" + chId);
            if(hasNextPage){
                int index = currentChannelInListIndex(chId);   //DTV00595148
                if (index != -1 && commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                    if (CommonIntegration.isEURegion() && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList CURRENT_CHANNEL_TYPE 1 "+CURRENT_CHANNEL_TYPE);
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                            tempApiChList = commonIntegration.getChannelListByMaskValuefilter(chId, index, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal, true);
                        } else {
                            tempApiChList = commonIntegration.getChannelListByMaskValuefilter(chId, index, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal, false);
                        }
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList CURRENT_CHANNEL_TYPE 2 "+CURRENT_CHANNEL_TYPE);
                        tempApiChList = commonIntegration.getChList(chId, index, CHANNEL_LIST_PAGE_MAX - index);
                    }
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList CURRENT_CHANNEL_TYPE 2 "+commonIntegration.checkCurChMask(mCurMask, mCurVal));
                    if (CommonIntegration.isEURegion() && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList CURRENT_CHANNEL_TYPE 3 "+CURRENT_CHANNEL_TYPE);
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                            tempApiChList = commonIntegration.getChannelListByMaskValuefilter(chId, mLastSelection, CHANNEL_LIST_PAGE_MAX - mLastSelection, mCurMask, mCurVal, true);
                        } else {
                            tempApiChList = commonIntegration.getChannelListByMaskValuefilter(chId, mLastSelection, CHANNEL_LIST_PAGE_MAX - mLastSelection, mCurMask, mCurVal, false);
                        }
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList CURRENT_CHANNEL_TYPE 4 "+CURRENT_CHANNEL_TYPE);
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal) || CommonIntegration.isUSRegion()) {
                            tempApiChList = commonIntegration.getChList(chId, mLastSelection, CHANNEL_LIST_PAGE_MAX - mLastSelection);
                        } else {
                            tempApiChList = commonIntegration.getChannelListByMaskValuefilter(chId, mLastSelection, CHANNEL_LIST_PAGE_MAX - mLastSelection, mCurMask, mCurVal, false);
                        }
                    }
                }
                while (tempApiChList.size() > CHANNEL_LIST_PAGE_MAX) {
                    tempApiChList.remove(tempApiChList.size() - 1);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList length "+tempApiChList.size());
                tempChlist = mTIFChannelManager.getTIFChannelList(tempApiChList);
            }else if(preNum >0){
                tempApiChList = getChannelList(0,MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST, CHANNEL_LIST_PAGE_MAX,mCurMask,mCurVal);
                tempChlist = TIFFunctionUtil.getTIFChannelList(tempApiChList);
            }else{
                tempChlist = null;
            }
        return tempChlist;
    }

    private void saveLastPosition(int currentChannelId, List<TIFChannelInfo> tempChlist) {
        if (tempChlist != null) {
            mChannelIdList.clear();
            int size = tempChlist.size();
            for (int i = 0; i < size; i++) {
                int channelId;
                if(tempChlist.get(i).mMtkTvChannelInfo == null){
                    channelId =(int) tempChlist.get(i).mId;
                }else{
                    channelId = tempChlist.get(i).mMtkTvChannelInfo.getChannelId();
                }
                mChannelIdList.add(channelId);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannelId>>>" + currentChannelId + "   " + channelId);
            }
        }
    }

    private int currentChannelInListIndex(int currentChannelId) {
        List<Integer> tempList = new ArrayList<>();
        tempList.addAll(mChannelIdList);
        int size = tempList.size();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChannelIdList size: " + size);
        for (int i = 0; i < size; i++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChannelIdList  mChannelIdList.get("+i+"): " +tempList.get(i));
            if (currentChannelId == tempList.get(i)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i>>>>" + i);
                return i;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i>>>>" + -1);
        return -1;
    }
    private int ttsSelectchannelIndex=-1;
    private AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {

        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
            AccessibilityEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent.host.getId()" + host.getId());
            do {
                List<CharSequence> texts = event.getText();
                if(texts == null) {
                    break;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
                switch(host.getId()){
                case R.id.nav_channel_listview:
                  //confirm which item is focus
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "R.id.nav_channel_listview" );
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        ttsSelectchannelIndex = findSelectItem(texts);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":ttsSelectchannelIndex =" + ttsSelectchannelIndex);
                        if(ttsSelectchannelIndex >= 0) {
                            UKChannelListDialog.this.startTimeout(NAV_TIMEOUT_10);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"click item");
                         TIFChannelInfo selectedChannel;
                         if(ttsSelectchannelIndex > -1){
                             selectedChannel =mTIFChannelManager.getTIFChannelInfoById(mChannelIdList.get(ttsSelectchannelIndex));
                             TIFChannelInfo currentChannel=mTIFChannelManager.getCurrChannelInfo();
                             if(selectedChannel !=null && currentChannel !=null){
                                 if(selectedChannel.mId != currentChannel.mId){
                                         showPvrDialog(selectedChannel);
                                 }
                             }else if(selectedChannel !=null && currentChannel == null){
                                 showPvrDialog(selectedChannel);

                             }
                         }

                    }
                    break;
                 default:
                     break;
                }
            } while(false);

            try {//host.onRequestSendAccessibilityEventInternal(child, event);
                Class clazz = Class.forName("android.view.ViewGroup");
                java.lang.reflect.Method getter =
                    clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
                        View.class, AccessibilityEvent.class);
                return (boolean)getter.invoke(host, child, event);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        private void showPvrDialog(TIFChannelInfo selectedChannel){
                final MtkTvChannelInfoBase selectedChannels = selectedChannel.mMtkTvChannelInfo;
             if (DvrManager.getInstance() != null
                       && DvrManager.getInstance().pvrIsRecording()) {
                   String srctype = DvrManager.getInstance().getController().getSrcType();
                   if (!"TV".equals(srctype)
                           && !InputSourceManager.getInstance().getConflictSourceList()
                      .contains(srctype)
                      && !("1".equals(selectedChannel.mType))) {
                       UKChannelListDialog.this.selectTifChannel(KeyEvent.KEYCODE_DPAD_CENTER, selectedChannel);
                   } else {
                       DvrDialog conDialog = new DvrDialog((Activity) mContext,
                       DvrDialog.TYPE_Confirm_From_ChannelList,
                       KeyEvent.KEYCODE_DPAD_CENTER, DvrDialog.TYPE_Record);
                       com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-1,ID:" + selectedChannels.getChannelId());
                       com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-1,Name:" + selectedChannels.getServiceName());
                       conDialog.setMtkTvChannelInfoBase(selectedChannels.getChannelId());
                       conDialog.setOnPVRDialogListener(new OnDVRDialogListener() {

                           @Override
                      public void onDVRDialogListener(int keyCode) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "OnPVRDialogListener keyCode>>>" + keyCode);
                            switch (keyCode) {
                               case KeyMap.KEYCODE_DPAD_CENTER:
                                   Message msg = Message.obtain();
                                   msg.what = TYPE_CHANGECHANNEL_ENTER;
                                   msg.arg1 = selectedChannels.getChannelId();
                                   mHandler.sendMessageDelayed(msg, 3000);
                                   break;
                               default:
                            break;
                            }
                           }
                       });
                    conDialog.show();
                    dismiss();
                  }
               }else if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)){
                   DvrDialog conDialog = new DvrDialog((Activity) mContext,
                   DvrDialog.TYPE_Confirm_From_ChannelList,
                   KeyEvent.KEYCODE_DPAD_CENTER, DvrDialog.TYPE_Timeshift);
                   com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TYPE_Timeshift channelID:-1,ID:" + selectedChannels.getChannelId());
                   com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TYPE_Timeshift channelID:-1,Name:" + selectedChannels.getServiceName());
                   conDialog.setMtkTvChannelInfoBase(selectedChannels.getChannelId());
                   conDialog.setOnPVRDialogListener(new OnDVRDialogListener() {

                       @Override
                  public void onDVRDialogListener(int keyCode) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIMESHIFT_START keyCode>>>" + keyCode);
                        switch (keyCode) {
                           case KeyMap.KEYCODE_DPAD_CENTER:
                               Message msg = Message.obtain();
                               msg.what = TYPE_CHANGECHANNEL_ENTER;
                               msg.arg1 = selectedChannels.getChannelId();
                               mHandler.sendMessageDelayed(msg, 3000);
                               break;
                           default:
                        break;
                        }
                       }
                   });
                conDialog.show();
                dismiss();
               }else{
                     UKChannelListDialog.this.selectTifChannel(KeyEvent.KEYCODE_DPAD_CENTER, selectedChannel);
                     Message msg = Message.obtain();
                     msg.arg1 = KeyEvent.KEYCODE_DPAD_CENTER;
                     msg.what = SET_SELECTION_INDEX;
                     mHandler.sendMessageDelayed(msg, 1200);
               }
        }
        private int findSelectItem(List<CharSequence> texts) {

            if(mChannelIdList == null) {
                return -1;
            }
            if(texts.size() > 1){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItem texts.get(0) =" +texts.get(0).toString()+"findSelectItem texts.get(1) =" +texts.get(1).toString());
                for(int i = 0; i < mChannelIdList.size(); i++) {
                    TIFChannelInfo tifchannelinfo=mTIFChannelManager.getTIFChannelInfoById(mChannelIdList.get(i));
                    if(tifchannelinfo != null && (tifchannelinfo.mDisplayNumber).equals(texts.get(0).toString()) && (tifchannelinfo.mDisplayName).equals(texts.get(1).toString())) {
                        return i;
                    }
                }
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItem texts =" +texts.get(0).toString());
                for(int i = 0; i < mChannelIdList.size(); i++) {
                    TIFChannelInfo tifchannelinfo=mTIFChannelManager.getTIFChannelInfoById(mChannelIdList.get(i));
                    if(tifchannelinfo != null && (tifchannelinfo.mDisplayNumber).equals(texts.get(0).toString())) {
                        return i;
                    }
                }
            }


            return -1;
        }

    };

    private void init() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"init types.length = "+types.length );
      //  mTitlePre =  mContext.getResources().getString(R.string.nav_channel_list)+" - ";
        if (mIsTifFunction) {
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                    resetChList();
                }else {
                   resetChListByTIF();
                }
        }
        if(MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
              TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_SVCTX_NOTIFY, mHandler);
         }
        mChannelListView.setAccessibilityDelegate(mAccDelegate);
        mChannelListView.setOnKeyListener(new ChannelListOnKey());
    }

    /**
    * true(next),false(pre)
    */
    private List<MtkTvChannelInfoBase> getNextPrePageChList(boolean next){
        List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
        MtkTvChannelInfoBase chInfo = null;
        if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
            if(next){
                chInfo = mChannelAdapter.getItem(mChannelAdapter.getCount() - 1).mMtkTvChannelInfo;
                list = getChannelList(chInfo.getChannelId() ,MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,CHANNEL_LIST_PAGE_MAX,mCurMask,mCurVal);
            }else{
                chInfo = mChannelAdapter.getItem(0).mMtkTvChannelInfo;
                list = getChannelList(chInfo.getChannelId() ,MtkTvChannelList.CHLST_ITERATE_DIR_PREV,CHANNEL_LIST_PAGE_MAX,mCurMask,mCurVal);
            }
        }
        if (mChannelAdapter != null && chInfo != null) {
            List<TIFChannelInfo> mTifChannelList=TIFFunctionUtil.getTIFChannelList(list);
            saveLastPosition(chInfo.getChannelId(), mTifChannelList);
        }

        return list;
    }

    /**
    * true(next),false(pre)
    */
    private List<TIFChannelInfo> getNextPrePageChListByTIF(boolean next) {
        if (commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")
                && commonIntegration.isDualTunerEnable()
                && commonIntegration.isCurrentSourceTv()) {
            int tempmask = mCurMask;
            int tempVal = mCurVal;
            if (mCurMask == CommonIntegration.CH_LIST_MASK
                    && mCurVal == CommonIntegration.CH_LIST_VAL) {
                mCurMask = CommonIntegration.CH_LIST_DIGITAL_RADIO_MASK;
                mCurVal = CommonIntegration.CH_LIST_DIGITAL_RADIO_VAL;
            }
            List<MtkTvChannelInfoBase> mchannelbase = getNextPrePageChList(next);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "getNextPrePageChListByTIF getCurrentFocus() mchannelbase= "
                            + mchannelbase.size());
            mCurMask = tempmask;
            mCurVal = tempVal;
            return mTIFChannelManager.getTIFChannelList(mchannelbase);
        } else {
            List<TIFChannelInfo> mTifChannelList = null;
            TIFChannelInfo chInfo = null;
            if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
                if (next) {
                    chInfo = mChannelAdapter
                            .getItem(mChannelAdapter.getCount() - 1);
                    int channelId = 0;
                    if (chInfo.mMtkTvChannelInfo != null) {
                        channelId = chInfo.mMtkTvChannelInfo.getChannelId();
                    } else {
                        channelId = (int) chInfo.mId;
                    }
                    mTifChannelList = mTIFChannelManager
                            .getTIFPreOrNextDukChannelList(channelId, false,
                                    false, CHANNEL_LIST_PAGE_MAX, mCurMask,
                                    mCurVal,msdtservicetype);
                } else {
                    chInfo = mChannelAdapter.getItem(0);
                    int channelId = 0;
                    if (chInfo.mMtkTvChannelInfo != null) {
                        channelId = chInfo.mMtkTvChannelInfo.getChannelId();
                    } else {
                        channelId = (int) chInfo.mId;
                    }
                    mTifChannelList = mTIFChannelManager
                            .getTIFPreOrNextDukChannelList(channelId, true, false,
                                    CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal,msdtservicetype);

                }
            }
            if (mChannelAdapter != null && chInfo != null) {
                if (!commonIntegration.is3rdTVSource()) {
                    int channelId = 0;
                    if (chInfo.mMtkTvChannelInfo != null) {
                        channelId = chInfo.mMtkTvChannelInfo.getChannelId();
                    } else {
                        channelId = (int) chInfo.mId;
                    }
                    saveLastPosition(channelId, mTifChannelList);
                } else {
                    saveLastPosition((int) chInfo.mId, mTifChannelList);
                }
            }
            return mTifChannelList;
        }
    }

    private List<MtkTvChannelInfoBase> getAllChannelList(int chID,int dir,int count){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getAllChannelList chID = "+chID +"dir ="+ dir +"count = "+ count);
        return  commonIntegration.getChListByMask(chID,dir,count,CommonIntegration.CH_LIST_MASK,CommonIntegration.CH_LIST_VAL);
    }

    private List<TIFChannelInfo> getAllChannelListByTIF(int chID, boolean isPrePage, int count){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getAllChannelList chID = "+chID);
        return mTIFChannelManager.getTIFPreOrNextChannelList(chID, isPrePage, true, count, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
    }

    private List<MtkTvChannelInfoBase> getChannelList(int chID,int dir,int count,int mask,int val){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getChannelList chID = "+chID +"dir ="+ dir +"count = "+ count+" mask = "+ mask+" val ="+val);
        switch(MarketRegionInfo.getCurrentMarketRegion()){
        case MarketRegionInfo.REGION_CN:
        case MarketRegionInfo.REGION_EU:
        case MarketRegionInfo.REGION_SA:
            return  commonIntegration.getChListByMask(chID,dir,count,mask,val);
        case MarketRegionInfo.REGION_US:
            int prevCount = 0;
            int nextCount = 0;
            int id = 0;
            switch(dir){
            case MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST:
                prevCount = 0;
                nextCount = count;
                break;
            case MtkTvChannelList.CHLST_ITERATE_DIR_NEXT:
                id = chID+1;
                prevCount = 0;
                nextCount = count;
                break;
            case MtkTvChannelList.CHLST_ITERATE_DIR_PREV:
                id = chID;
                prevCount = count;
                nextCount = 0;
                break;
            default:
                prevCount = 0;
                nextCount = count;
                break;
            }
            return commonIntegration.getChList(id, prevCount,nextCount);
        default :
            return  commonIntegration.getChListByMask(chID,dir,count,mask,val);
        }
    }

    private boolean isLock() {
       int showFlag = mtkTvPwd.PWDShow();//if current tv video start error, this value is not correct
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isLock showFlag = "+showFlag);
       switch(showFlag){
       case PwdDialog.PASSWORD_VIEW_SHOW_PWD_INPUT:
       case PwdDialog.PASSWORD_VIEW_HINT_DVBS:
       case PwdDialog.PASSWORD_VIEW_PWD_ERROR:
          return true;
       default :
           break;

       }
       return false;
    }
    private void setChannelListTitle(){
        mTitleText.setText(types[CURRENT_CHANNEL_TYPE]);
//        if(mCurMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK){
//            mBlueKeyText.setVisibility(View.INVISIBLE);
//            mBlueicon.setVisibility(View.INVISIBLE);
//        }else{
//            mBlueKeyText.setVisibility(View.VISIBLE);
//            mBlueicon.setVisibility(View.VISIBLE);
//        }
    }
    private boolean isChannel(){
        boolean hasChannel = true;
        CH_TYPE = CommonIntegration.CH_TYPE_BASE+commonIntegration.getSvl();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Channel list dialog isKeyHandler CH_TYPE "+CH_TYPE);
        initTypes();
        if (mIsTifFunction) {
             if (commonIntegration.isDualChannellist()) {
                    if(!commonIntegration.hasActiveChannel()) {
                      hasChannel = false;
                    }
             }else {
                if(!mTIFChannelManager.hasActiveChannel()
                    ||!commonIntegration.isCurrentSourceTv()){
                    if(commonIntegration.isEURegion() && !commonIntegration.isEUPARegion()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasChannel  isEURegion= ");
                        if(mTIFChannelManager.get3RDChannelList().size() < 1 || !commonIntegration.isCurrentSourceTv()){
                            hasChannel = false;
                        }else{
                            if(types !=null && types.length > 0) {
                                CURRENT_CHANNEL_TYPE = IPAPP_CHANNEL;
                                resetMask();
                                mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                            }

                        }

                    }else{
                        hasChannel = false;
                    }
                }
            }

        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasChannel = "+hasChannel);
        return hasChannel;
    }

    @Override
    public void show() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show");
        super.show();
        setWindowPosition();
        mCanChangeChannel = false;
       if( chanelListProgressbar != null && chanelListProgressbar.getVisibility() != View.VISIBLE ){
           chanelListProgressbar.setVisibility(View.VISIBLE);
       }
        commonIntegration.setChannelChangedListener(listener);
        mChannelListView.setAdapter(null);
        mChannelListView.setVisibility(View.GONE);
        mHandler.sendEmptyMessage(REFRESHTIME);
        init();
    }

    private ChannelChangedListener listener = new ChannelChangedListener(){
        public void onChannelChanged(){
            if(UKChannelListDialog.this.isShowing() && !mSelectionShow ){
                if (mIsTifFunction) {
                    if (mThreadHandler != null && !mThreadHandler.hasCallbacks(mUpdateChannelListRunnable)) {
                        mThreadHandler.post(mUpdateChannelListRunnable);
                    }
//                  resetChListByTIF();
                } else {
                    resetChList();
                }
            }
        };
    };

    //init dialog position
    public void setWindowPosition() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        TypedValue sca = new TypedValue();
        mContext.getResources().getValue(R.dimen.nav_channellist_marginY,sca ,true);
        mContext.getResources().getValue(R.dimen.nav_channellist_marginX,sca ,true);
        mContext.getResources().getValue(R.dimen.nav_channellist_size_width,sca ,true);
        float chwidth  = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_height,sca ,true);
        mContext.getResources().getValue(R.dimen.nav_channellist_page_max,sca ,true);
        int menuWidth = (int) (display.getWidth() * chwidth);
        lp.width = menuWidth;
        lp.height = display.getHeight();
        int x = display.getWidth();
        int y =0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setWindowPosition menuWidth "+menuWidth+" x "+x +" display.getWidth() "+display.getWidth());
        lp.x = x;
        lp.y = y;
        window.setAttributes(lp);
    }

    private void showPageUpDownView() {
             if (hasNextPage) {
                if (mChannelListPageUpDownLayout.getVisibility() != View.VISIBLE) {
                    mChannelListPageUpDownLayout.setVisibility(View.VISIBLE);
                }
             } else {
                if (mChannelListPageUpDownLayout.getVisibility() != View.INVISIBLE) {
                    mChannelListPageUpDownLayout.setVisibility(View.INVISIBLE);
                }
             }
    }

    public void onDismiss(DialogInterface dialog) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDismiss!!!!!!!!!");
        mChannelListView.setOnKeyListener(null);
        mChannelListView.setOnItemSelectedListener(null);
//      try {
//          mTIFChannelManager.getContentResolver().unregisterContentObserver(mTIFChannelContentObserver);
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "unregisterContentObserver!!!!!!!!!");
//      } catch (Exception e) {
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "unregisterContentObserver!!!!!!Exception!!!");
//          e.printStackTrace();
//      }
       // mTvCallbackHandler.removeCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST,mHandler );
        //addCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST,mHandler );
      //  mTvCallbackHandler.removeCallBackListener(TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE,mHandler );
    }

    public void exit(){
       /* if(AnimationManager.getInstance().getIsAnimation()){
        AnimationManager.getInstance().channelListExitAnimation(mChannelListLayout , new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dismiss();
            }
        });
        }else{*/
            dismiss();
       // }
    }

    @Override
    public void dismiss() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss!!!!!!!");
        if(DestroyApp.getTopActivity()!= null && DestroyApp.getTopActivity().equals("EditTextActivity")){
            return ;
        }
        if(mHandler.hasMessages(REFRESHTIME)){
            mHandler.removeMessages(REFRESHTIME);
          }
        mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
        mCanChangeChannel = true;
        if (mChannelListView != null) {
            mChannelListView.setAdapter(null);
            mChannelListView.setVisibility(View.VISIBLE);
            mLastSelection = mChannelListView.getSelectedItemPosition();
            if(mContext != null && TextToSpeechUtil.isTTSEnabled(mContext)){
                mChannelListView.setFocusable(true);
                mChannelListView.requestFocus();
                mChannelListView.setSelection(mLastSelection);
            }
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mLastSelection>>>>>" + mLastSelection);
        mThreadHandler.removeCallbacks(mResetChannelListRunnable);
        mThreadHandler.removeCallbacks(mUpdateChannelListRunnable);
        mThreadHandler.removeCallbacks(mChannelUpRunnable);
        mThreadHandler.removeCallbacks(mChannelPreRunnable);
        mThreadHandler.removeCallbacks(mChannelDownRunnable);
        if (mChannelAdapter != null) {
            saveLastPosition(commonIntegration.getCurrentChannelId(), mChannelAdapter.getChannellist());
        }

        super.dismiss();
        //com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss!!!!!!!+ isShowFAVListFullToastDealy" + commonIntegration.isShowFAVListFullToastDealy());
        if (commonIntegration.isShowFAVListFullToastDealy()
                && commonIntegration.isFavListFull()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss!!!!!!!+ show fav list is full");
            TurnkeyUiMainActivity.getInstance().getHandler().removeMessages(InternalHandler.MSG_SHOW_FAV_LIST_FULL_TOAST);
            TurnkeyUiMainActivity.getInstance().getHandler().sendEmptyMessage(InternalHandler.MSG_SHOW_FAV_LIST_FULL_TOAST);
            commonIntegration.setShowFAVListFullToastDealy(false);
        }
    }

    public void showDialogForPVRAndTShift(final int tYPEConfirmFromChannelList,final int keyCode,int tYPETimeshift,int channelId) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	SimpleDialog simpleDialog = (SimpleDialog)ComponentsManager.getInstance().
        				getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
                simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
                simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
                simpleDialog.setCancelText(R.string.pvr_confirm_no);
                Bundle bundle = new Bundle();
                bundle.putInt("mSelectChannelId",channelId);
                simpleDialog.setBundle(bundle);
                simpleDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {

					@Override
					public void onConfirmClick(int dialogId) {
						// TODO Auto-generated method stub
						DvrManager.getInstance().stopAllRunning();
                        if (TifTimeShiftManager.getInstance() != null) {
                            TifTimeShiftManager.getInstance().stopAll();
                        }
						  dialogId = simpleDialog.getBundle().getInt("mSelectChannelId");
						  if (dialogId != -1){
						      Message msg = Message.obtain();
                              msg.what = TYPE_CHANGECHANNEL_ENTER;
                              msg.arg1 = dialogId;
                              mHandler.sendMessageDelayed(msg, 2000);
						  }else if(keyCode == KeyMap.KEYCODE_MTKIR_CHDN){
						      channelUpDown(false);
						  }else if(keyCode == KeyMap.KEYCODE_MTKIR_CHUP){
						      channelUpDown(true);
                          }else if(keyCode == KeyMap.KEYCODE_MTKIR_PRECH){
                              mTIFChannelManager.channelPre();
                          }
					}
				},channelId);
                simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick(int dialogId) {
                        // TODO Auto-generated method stub
                        simpleDialog.dismiss();
                    }
                },channelId);
                if(tYPETimeshift==DvrDialog.TYPE_Record){
                	simpleDialog.setContent(R.string.dvr_dialog_message_record_channel);
                }else {
                	  simpleDialog.setContent(R.string.dvr_dialog_message_timeshift_channel);
				}
                simpleDialog.show();
            }
          });

    }
    class ChannelListOnKey implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch(v.getId()){
        case R.id.nav_channel_listview:
            int slectPosition = mChannelListView.getSelectedItemPosition();
             com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"ChannelListOnKey keyCode ="+keyCode+" slectPosition = "+
                slectPosition +"mChannelListView ="+mChannelListView);
             if(mChannelListView != null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKey mChannelListView.getChildAt(slectPosition) ="+mChannelListView.getChildAt(slectPosition));
             }
            if (slectPosition < 0 || mChannelListView.getChildAt(slectPosition) == null) {
                //slectPosition = 0;
                return false;
            }
            //mChannelListView.getChildAt(slectPosition).setFocusable(true);
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    startTimeout(NAV_TIMEOUT_10);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mChannelItemKeyLsner*********** slectPosition = "
                                    + mChannelListView.getSelectedItemPosition());
                            TIFChannelInfo tIFChannelInfo;
                            if(TextToSpeechUtil.isTTSEnabled(mContext)){
                                //tIFChannelInfo  =mTIFChannelManager.getTIFChannelInfoById(mChannelIdList.get(ttsSelectchannelIndex));
                                 return true; // select channel not support focus when tts is true
                            }else{
                                tIFChannelInfo = (TIFChannelInfo) mChannelListView.getSelectedItem();
                            }
                            if(tIFChannelInfo.mMtkTvChannelInfo != null){
                               final MtkTvChannelInfoBase selectedChannel = tIFChannelInfo.mMtkTvChannelInfo;
                               if (!selectedChannel.equals(mTIFChannelManager.getCurrChannelInfo())) {
                                   if (DvrManager.getInstance() != null
                                           && DvrManager.getInstance().pvrIsRecording()) {
                                       String srctype = DvrManager.getInstance().getController().getSrcType();
                                       if (!"TV".equals(srctype)
                                               && !InputSourceManager.getInstance().getConflictSourceList()
                                          .contains(srctype)
                                          && !"1".equals(tIFChannelInfo.mType)) {
                                           selectTifChannel(keyCode, tIFChannelInfo);
                                       } else {
                                           com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TYPE_Record " + selectedChannel.getChannelId());
                                           dismiss();
                                           showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,keyCode,DvrDialog.TYPE_Record,selectedChannel.getChannelId());
                                      }
                                    }else if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mContext:" + mContext);
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TIMESHIFT_START " + selectedChannel.getChannelId()+" TIMESHIFT_START "+SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START ));
                                        dismiss();
                                        showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,keyCode,DvrDialog.TYPE_Timeshift,selectedChannel.getChannelId());
                                    } else {

                                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-2,ID:" + selectedChannel.getChannelId());
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-2,Name:" + selectedChannel.getServiceName());
                                        selectTifChannel(keyCode, tIFChannelInfo);
                                    }
                               }
                        }else{
                            TIFChannelInfo currentTIFChannelInfo=  mTIFChannelManager.getCurrChannelInfo();
                            if ( currentTIFChannelInfo == null || (currentTIFChannelInfo !=null && tIFChannelInfo.mId != currentTIFChannelInfo.mId)) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "is3rdTVSource channelID:-2,ID:" + tIFChannelInfo.mId);
                                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "is3rdTVSource channelID:-2,Name:" + tIFChannelInfo.mDisplayName);
                                selectTifChannel(keyCode, tIFChannelInfo);
                            }else{
                                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "is3rdTVSource channelis null ");
                            }

                        }
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!!");
                    mChannelListView.getChildAt(slectPosition).requestFocusFromTouch();
                    if(slectPosition == mChannelAdapter.getCount()-1 && hasNextPage){
                        if (mIsTifFunction) {
                            mChannelAdapter.updateData(getNextPrePageChListByTIF(true));
                        } else {
                            mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(true)));
                        }
                        mChannelListView.setSelection(0);

                    }else{
                  //      mChannelAdapter.updateView(mChannelListView.getSelectedView(), 0, 1);
                        if(mChannelListView.getSelectedItemPosition() == mChannelAdapter.getCount()-1){
                            mChannelListView.setSelection(0);
                        }else{
                            mChannelListView.setSelection(mChannelListView.getSelectedItemPosition()+1);
                        }
                    }
                //    mChannelAdapter.updateView(mChannelListView.getSelectedView(), 1, 1);
                    TIFChannelInfo downFavchannel= mChannelAdapter.getChannellist().get(mChannelListView.getSelectedItemPosition());
                    if(downFavchannel.mDataValue != null && downFavchannel.mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                        mBlueKeyText.setVisibility(View.VISIBLE);
                        mBlueicon.setVisibility(View.VISIBLE);
                        if(downFavchannel.mMtkTvChannelInfo != null && downFavchannel.mMtkTvChannelInfo.isDigitalFavoritesService()){
                            mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_remove_fav));
                        }else{
                            mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_add_fav));
                        }
                    }else{
                        mBlueKeyText.setVisibility(View.INVISIBLE);
                        mBlueicon.setVisibility(View.INVISIBLE);
                    }

                    startTimeout(NAV_TIMEOUT_10);
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_UP!!!!!");
                    mChannelListView.getChildAt(slectPosition).requestFocusFromTouch();
                    if(slectPosition == 0 && hasNextPage){
                        if (mIsTifFunction) {
                            mChannelAdapter.updateData(getNextPrePageChListByTIF(false));
                        } else {
                            mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(false)));
                        }
                        mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                    }else{
                   //     mChannelAdapter.updateView(mChannelListView.getSelectedView(), 0, 1);
                        if(mChannelListView.getSelectedItemPosition() == 0){
                            mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                        }else{
                            mChannelListView.setSelection(mChannelListView.getSelectedItemPosition()-1);
                        }
                    }
                 //   mChannelAdapter.updateView(mChannelListView.getSelectedView(), 1, 1);
                    if (mChannelAdapter.getChannellist().size() <= mChannelListView.getSelectedItemPosition()){
                        return true;
                    }
                    TIFChannelInfo upFavchannel= mChannelAdapter.getChannellist().get(mChannelListView.getSelectedItemPosition());
                    if(upFavchannel.mDataValue != null && upFavchannel.mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                        mBlueKeyText.setVisibility(View.VISIBLE);
                        mBlueicon.setVisibility(View.VISIBLE);
                        if(upFavchannel.mMtkTvChannelInfo != null && upFavchannel.mMtkTvChannelInfo.isDigitalFavoritesService()){
                            mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_remove_fav));
                        }else{
                            mBlueKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_add_fav));
                        }
                    }else{
                        mBlueKeyText.setVisibility(View.INVISIBLE);
                        mBlueicon.setVisibility(View.INVISIBLE);
                    }

                    startTimeout(NAV_TIMEOUT_10);
                    return true;
                case  KeyMap.KEYCODE_PAGE_DOWN:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_PAGE_DOWN!!!!!");
                    //dismiss();
                  //  FavChannelManager.getInstance(mContext).favAddOrErase();
                    return true;
                    default :
                        break;
                }
            }
        default :
            break;
        }
          return false;
        }
    }
    private void selectTifChannel(int keyCode, TIFChannelInfo selectedChannel) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
            // mChannelDetailLayout.setVisibility(View.INVISIBLE);
            if(commonIntegration.isdualtunermode()){

                if(selectedChannel.mMtkTvChannelInfo != null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectTifChannel isUp = TIFChannelInfo  " + selectedChannel.mMtkTvChannelInfo.getChannelId()+"  " +MtkTvTISMsgBase.createSvlChannelUri(selectedChannel.mMtkTvChannelInfo.getChannelId()));
                     TurnkeyUiMainActivity.getInstance().getPipView().tune(InputSourceManager.getInstance().getTvInputInfo("sub").getId(), MtkTvTISMsgBase.createSvlChannelUri(selectedChannel.mMtkTvChannelInfo.getChannelId()));
                     int  channelid= commonIntegration.get2NDCurrentChannelId();
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectTifChannel channelid = "+channelid);
                     if(channelid == selectedChannel.mMtkTvChannelInfo.getChannelId()){
                          mLastSelection = mChannelListView.getSelectedItemPosition();
                          if (mChannelAdapter != null) {
                            saveLastPosition(selectedChannel.mMtkTvChannelInfo.getChannelId(),
                                mChannelAdapter.getChannellist());
                          }
                     }
                }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectTifChannel isdualtunermode is support netowrk channels");
                }

            }else{
                boolean changeSuccess = mTIFChannelManager.selectChannelByTIFInfo(selectedChannel);
                if (changeSuccess) {
                    dismiss();
//                    mLastSelection = mChannelListView.getSelectedItemPosition();
//                    if(TIFFunctionUtil.checkChMask(currentChannel, TIFFunctionUtil.CH_FAKE_MASK, TIFFunctionUtil.CH_FAKE_VAL)){
//                        Message msg = Message.obtain();
//                        msg.what = SET_SELECTION_INDEX;
//                        msg.arg1 = keyCode;
//                        mHandler.sendMessageDelayed(msg, 1000);
//                    }
//                    if (mChannelAdapter != null) {
//                        if(selectedChannel.mMtkTvChannelInfo == null){
//                            saveLastPosition((int)selectedChannel.mId, mChannelAdapter.getChannellist());
//                        }else{
//                            saveLastPosition(selectedChannel.mMtkTvChannelInfo.getChannelId(), mChannelAdapter.getChannellist());
//                        }
//                    }
                }
            }
            break;
        default:
            break;
        }
    }

    public void selectChannel(int keyCode, MtkTvChannelInfoBase selectedChannel) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "selectChannel(),keyCode:" + keyCode
                + ",selectedChannel:" + selectedChannel.getChannelId());
        int curChId = -1;
        int chIndex = -1;
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case DvrDialog.TYPE_Change_ChannelNum:
            // mChannelDetailLayout.setVisibility(View.INVISIBLE);
            boolean changeSuccess = commonIntegration.selectChannelByInfo(selectedChannel);
            if (changeSuccess) {
                mLastSelection = mChannelListView.getSelectedItemPosition();
                if (mChannelAdapter != null) {
                    saveLastPosition(selectedChannel.getChannelId(), mChannelAdapter.getChannellist());
                }
//              ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
            }
            break;
        case KeyMap.KEYCODE_MTKIR_CHUP:
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_CHUP");
            boolean isUp = false;
            isUp = commonIntegration.channelUp();
            if (isUp) {
                ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                curChId = commonIntegration.getCurrentChannelId();
                if (mChannelAdapter != null) {
                chIndex = mChannelAdapter.isExistCh(curChId);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_CHUP chIndex = " +chIndex);
                if (chIndex >= 0) {
                    mChannelListView.requestFocus();
                    mChannelListView.setSelection(chIndex);
                    mLastSelection = chIndex;
                } else {
                    if (mIsTifFunction) {
                        mChannelAdapter.updateData(getAllChannelListByTIF(curChId, false, CHANNEL_LIST_PAGE_MAX));
                    } else {
                        mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getAllChannelList(curChId,MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,CHANNEL_LIST_PAGE_MAX)));
                    }
                    mChannelListView.requestFocus();
                    mChannelListView.setSelection(0);
                    mLastSelection = 0;
                    }
                        saveLastPosition(curChId, mChannelAdapter.getChannellist());
                }
            }
            break;
        case KeyMap.KEYCODE_MTKIR_PRECH:
            int seletcPostion = mChannelListView.getSelectedItemPosition();
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_PRECH");
            if (commonIntegration.channelPre()) {
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                curChId = commonIntegration.getCurrentChannelId();
                if (mChannelAdapter != null) {
                chIndex = mChannelAdapter.isExistCh(curChId);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_PRECH  chIndex = "+chIndex);
                if (chIndex >= 0) {
                    mChannelListView.requestFocus();
                    mChannelListView.setSelection(chIndex);
                    mLastSelection = chIndex;

                } else {
                    if (seletcPostion != 0) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_PRECH  2step ");
                        if (mIsTifFunction) {
                            mChannelAdapter.updateData(getAllChannelListByTIF(curChId, false, CHANNEL_LIST_PAGE_MAX));
                        } else {
                            mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getAllChannelList(curChId,MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,CHANNEL_LIST_PAGE_MAX)));
                        }
                        mChannelListView.requestFocus();
                        mChannelListView.setSelection(0);
                        mLastSelection = 0;

                    } else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_PRECH  3step ");
                        if (mIsTifFunction) {
                            mChannelAdapter.updateData(getAllChannelListByTIF(curChId, true, CHANNEL_LIST_PAGE_MAX));
                        } else {
                            mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getAllChannelList(curChId,MtkTvChannelList.CHLST_ITERATE_DIR_PREV,CHANNEL_LIST_PAGE_MAX)));
                        }
                        mChannelListView.requestFocus();
                        mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
                        mLastSelection = mChannelAdapter.getCount() - 1;
                        }
                    }
                    saveLastPosition(curChId, mChannelAdapter.getChannellist());
                }
            }
            break;
        case KeyMap.KEYCODE_MTKIR_CHDN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_CHDN");
            boolean isDown = false;
            isDown = commonIntegration.channelDown();
            if (isDown) {
                ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                curChId = commonIntegration.getCurrentChannelId();
                if (mChannelAdapter != null) {
                chIndex = mChannelAdapter.isExistCh(curChId);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_CHDN chIndex = "+chIndex);
                if (chIndex >= 0) {
                    mChannelListView.requestFocus();
                    mChannelListView.setSelection(chIndex);
                    mLastSelection = chIndex;
                } else {
                    if (mIsTifFunction) {
                        mChannelAdapter.updateData(getAllChannelListByTIF(curChId, true, CHANNEL_LIST_PAGE_MAX));
                    } else {
                        mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getAllChannelList(curChId,MtkTvChannelList.CHLST_ITERATE_DIR_PREV,CHANNEL_LIST_PAGE_MAX)));
                    }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_CHDN step 0");
                    mChannelListView.requestFocus();
                        mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
                    mLastSelection = mChannelAdapter.getCount() - 1;
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler(),keycode==KeyMap.KEYCODE_MTKIR_CHDN step 1");
                    saveLastPosition(curChId, mChannelAdapter.getChannellist());
                }

            }
            break;
        default :
            break;
        }
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
         if(((keyCode == KeyMap.KEYCODE_DPAD_CENTER && !isLock()) || keyCode == KeyMap.KEYCODE_YEN) && isChannel()){
             initMaskAndSatellites();
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Channel list dialog isKeyHandler return true");
             return true;
         }
         if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
             UkBannerView ukbannerView = (UkBannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
             if (ukbannerView != null && !ukbannerView.isVisible()) {
                 ukbannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
             }
         }
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Channel list dialog isKeyHandler return false");
         return false;
    }


    class ChannelAdapter extends BaseAdapter{
        private static final String TAG = "ChannelListDialog.ChannelAdapter";
        private Context mContext;
        private LayoutInflater mInflater;
        private List<TIFChannelInfo> mCurrentTifChannelList;
        int icon_w ;
        int icon_h ;
        public ChannelAdapter(Context context, List<TIFChannelInfo> currentTifChannelList) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            this.mCurrentTifChannelList = currentTifChannelList;
            icon_w = (int)mContext.getResources().getDimension(R.dimen.nav_duk_chanenl_list_item_icon_width);
            icon_h = (int)mContext.getResources().getDimension(R.dimen.nav_duk_chanenl_list_item_icon_height);
        }

        public List<TIFChannelInfo> getChannellist() {
            return mCurrentTifChannelList;
        }

        public int getCount() {
            int s = 0;
            if (mCurrentTifChannelList != null) {
                s = mCurrentTifChannelList.size();
            }
            return s;
        }

        public int isExistCh(int chId){
            if(mCurrentTifChannelList != null){
              int size = mCurrentTifChannelList.size();
              for(int index  = 0;index < size; index++ ){
                if((mCurrentTifChannelList.get(index).mMtkTvChannelInfo !=null && mCurrentTifChannelList.get(index).mMtkTvChannelInfo.getChannelId()== chId) || mCurrentTifChannelList.get(index).mId == chId){
                    return index;
                }
              }
            }
            return -1;
        }

        public TIFChannelInfo getItem(int position) {
            return mCurrentTifChannelList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void updateData(List<TIFChannelInfo> currentChannelList) {
            this.mCurrentTifChannelList = currentChannelList;
            notifyDataSetChanged();
        }
        public void updateView(View view ,int isnormal,int visible) {
            if(view == null){
              return;
            }
            ViewHolder hodler=(ViewHolder) view.getTag();
            if(isnormal == 0){
                hodler.mChannelNameTextView.getPaint().setFakeBoldText(false);
                hodler.mChannelNameTextView.getPaint().setFakeBoldText(false);
            }else{
                hodler.mChannelNameTextView.getPaint().setFakeBoldText(true);
                hodler.mChannelNameTextView.getPaint().setFakeBoldText(true);
            }

          }
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder hodler;
            TIFChannelInfo mCurrentChannel;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.nav_uk_channel_item, null);
                hodler = new ViewHolder();
                hodler.mIcon=(ImageView) convertView.findViewById(R.id.nav_chanel_list_item_icon);
                hodler.mChannelNumberTextView = (TextView) convertView.findViewById(R.id.nav_channel_list_item_NumberTV);
                hodler.mChannelNameTextView = (TextView) convertView.findViewById(R.id.nav_channel_list_item_NameTV);
                convertView.setTag(hodler);
            } else {
                hodler = (ViewHolder) convertView.getTag();
            }
            hodler.mIcon.setVisibility(View.INVISIBLE);
            mCurrentChannel = mCurrentTifChannelList.get(position);
            if (mIsTifFunction) {
                  if(mCurrentChannel.mMtkTvChannelInfo != null && mCurrentChannel.mMtkTvChannelInfo.isDigitalFavoritesService()){
                      Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_fav_icon);
                      radioIcon.setBounds(0, 0, icon_w, icon_h);
                      hodler.mIcon.setImageDrawable(radioIcon);
                      hodler.mIcon.setVisibility(View.VISIBLE);
                  }else{
                      if (mCurrentChannel !=null && mCurrentChannel.isRadiochannel()) {
                          Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_uk_radio_icon);
                          radioIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(radioIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else if(mCurrentChannel !=null && mCurrentChannel.isIPChannel()){
                          Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_uk_ipapp_icon);
                          radioIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(radioIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else if(mCurrentChannel !=null && mCurrentChannel.isIPAPPchannel()){
                          Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_uk_ipapp_icon);
                          radioIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(radioIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else if(mCurrentChannel !=null && mCurrentChannel.isInterActive()){
                          Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_uk_interactive_icon);
                          radioIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(radioIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else if(mCurrentChannel !=null && mCurrentChannel.isHDchannel()){
                          Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_uk_hd_icon);
                          radioIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(radioIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else{
                          hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                      }
                  }
                hodler.mChannelNumberTextView.setText(mCurrentChannel.mDisplayNumber);
                hodler.mChannelNameTextView.setText(mCurrentChannel.mDisplayName);
            }

            return convertView;
        }

         class ViewHolder {
            ImageView mIcon;
            TextView mChannelNumberTextView;
            TextView mChannelNameTextView;
        }

    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
        if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
            if (isVisible()) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
                if (mSendKeyCode == KeyMap.KEYCODE_MTKIR_STOP || mSendKeyCode == KeyMap.KEYCODE_MTKIR_CHUP
                        || mSendKeyCode == KeyMap.KEYCODE_MTKIR_CHDN
                        || mSendKeyCode == KeyMap.KEYCODE_MTKIR_PRECH) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus>>>" + statusID + ">>" + value + ">>" + mSendKeyCode);
                    if (value == 0) {
                        if (TIFFunctionUtil.checkChMask(mTIFChannelManager.getPreChannelInfo(),TIFFunctionUtil.CH_LIST_MASK,0)) {//for US hidden channel
                            resetChListByTIF();
                            mCanChangeChannel = true;
                        } else {
                            Message msg = Message.obtain();
                            if (mSendKeyCode == KeyMap.KEYCODE_MTKIR_CHUP) {
                                msg.arg2 = preChId;
                            }
                            if (mSendKeyCode == KeyMap.KEYCODE_MTKIR_STOP) {
                                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHUP;
                            }
                            msg.arg1 = mSendKeyCode;
                            msg.what = SET_SELECTION_INDEX;
                            mHandler.sendMessage(msg);
                        }
                    } else {
                        mCanChangeChannel = true;
                    }
                }
            }
        } else if (statusID == ComponentStatusListener.NAV_COMPONENT_SHOW) {
            if (value == NAV_COMP_ID_TELETEXT && isVisible()) {
                dismiss();
            }
        } else if (statusID == ComponentStatusListener.NAV_RESUME) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "updateComponentStatus statusID:" +statusID );
            MtkTvConfig config = MtkTvConfig.getInstance();
            int chanelUpdateMsg = config.getConfigValue(MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG);
            int channelNewSvcAdded = config.getConfigValue(MenuConfigManager.SETUP_CHANNEL_NEW_SVC_ADDED);
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "chanelUpdateMsg:" + chanelUpdateMsg+" channelNewSvcAdded:"+channelNewSvcAdded);
            if(chanelUpdateMsg == 1 && channelNewSvcAdded > 0) {
                addChannelsDialog();
                config.setConfigValue(MenuConfigManager.SETUP_CHANNEL_NEW_SVC_ADDED,0);
            }
        }
    }

    public void addChannelsDialog(){
        final ConfirmDialog liveTVDialog = ConfirmDialog.getInstance(mContext);
        liveTVDialog.showBgScanAddedDialog();
    }

    public void channelListRearrangeFav(){
        mTIFChannelManager.channelListRearrangeFav();
    }

    @Override
    public boolean deinitView(){
        super.deinitView();
        commonIntegration.setChannelChangedListener(null);
        if(MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
            TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_SVCTX_NOTIFY, mHandler);
        }
        return false;
    }
}
