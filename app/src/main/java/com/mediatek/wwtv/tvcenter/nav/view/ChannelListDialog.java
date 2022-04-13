package com.mediatek.wwtv.tvcenter.nav.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.LayoutDirection;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.drawable.Drawable;

import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.InternalHandler;
import com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog.ChannelAdapter.ViewHolder;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.adapter.ChannelListMoreAdapter;
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
import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.setting.EditTextActivity;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import androidx.core.text.TextUtilsCompat;

import android.database.ContentObserver;
import android.widget.SimpleAdapter;
import android.media.tv.TvInputInfo;

import java.util.HashMap;

public class ChannelListDialog extends NavBasicDialog implements OnDismissListener, ComponentStatusListener.ICStatusListener {

    private static final String TAG = "ChannelListDialog";
    static final String SPNAME = "CHMODE";
 //   private static final String CH_TYPE_BASE = "type_";
    private String CH_TYPE = "";
    private static final String CH_SORT = "sort";
    static final String CH_OPTION = "more";
    private static final String CH_TYPE_SECOND = "typeSecond";
    private static final String CH_TYPE_FAV = "typefav";
    private static final String FAVOURITE_TYPE = "favouriteType";
    private static final String SATELLITE_RECORDID = "satellite_recordid";
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
    private static final int TYPE_CHANGE_CHANNEL = 0;
    private static final int TYPE_CHANGE_FAVORITE = 1;
    private static final int TYPE_CHANGE_SATELLITE = 2;
    private String[] types;
    private String[] favtypes;
    private String[] moreItems;
    private String[] moreTypes;
    private int addEraseChannelId = -1;
    private List<String> typesSecond;
    private String mTitlePre;
    private List<MtkTvDvbsConfigInfoBase> mSatelliteListinfo;
    private String[] mSatelliteRecords;
    View mChannelListFunctionLayout;
    TextView mExitAndSatTv;
    View mChannelListLayout;
    private ListView mChannelListView;
    private ListView mChannelTypeView;
    private ListView mChannelSatelliteView;
    ListView mChannelSatelliteSecondView;
    private ListView mChannelMoreView;
    FavSelectionAdapter selectionAdapter =null;
    private ListView mChannelSelectionView;
    ListView mChannelOptionView;
    ListView mChannelTypeFavView;
    private ListView mChannelSortView;
    private View mChannelListPageUpDownLayout;
    private View mChannelListTipView;
    private View mChannelDetailLayout;
    TextView mNavChannelDetailsChannelInfoTextView;
    private TextView mTitleText;
    private TextView mYellowKeyText;
    private TextView mChannelDetailTileText;
    private TextView mBlueKeyText;
    private ProgressBar chanelListProgressbar;
    private ImageView mBlueicon;
    String mNavChannelDetailsChannelInfoString;
    private List<Integer> mChannelTypePacksOrCategory;

    private CommonIntegration commonIntegration;
    private SaveValue mSaveValue;
    //private TvCallbackHandler mTvCallbackHandler ;
//    private MtkTvChannelList mChListCenter;
    private List<Integer> mChannelIdList;
    private int mLastSelection;
    private int CHANNEL_LIST_PAGE_MAX = 10;

    private static final int ALL_CHANNEL = 0;
    private static final int DIGITAL_CHANNEL = 1;
    private static final int RADIO_CHANNEL = 2;
    private static final int FREE_CHANNEL = 3;
    private static final int ENCRYPTED_CHANNEL = 4;
    private static final int ANALOG_CHANNEL = 5;
    private int CURRENT_CHANNEL_TYPE = ALL_CHANNEL;
    private int LAST_CHANNEL_TYPE = ALL_CHANNEL;
    private int CURRENT_CHANNEL_FAV = ALL_CHANNEL;
    private int LAST_CHANNEL_FAV = ALL_CHANNEL;
    private boolean SELECT_TYPE_CHANGE_CH = false;
    private int CURRENT_CHANNEL_SECOND_TYPE = -1;
    private int LAST_CHANNEL_SECOND_TYPE = ALL_CHANNEL;
    private String CURRENT_CHANNEL_SECOND_NAME = "";
    private String LAST_CHANNEL_SECOND_NAME = "";
    private int CURRENT_OPTION = ALL_CHANNEL;
    private int CURRENT_MORE_TYPE = 0;
    private int CURRENT_CHANNEL_SORT = ALL_CHANNEL;
    private String CURRENT_CHANNEL_FIND = "findChannels";
    private boolean isFavTypeState = false;
    private boolean isFavListMove=false ;
    private boolean isFindState=false ;
    private static final int PACKS_CHANNEL = 2;
    private static final int CATEGORIES_CHANNEL = 1;

    private static final int PACKS_BASE = 0x1101;
    private static final int CATEGORIES_BASE = 0x2101;
    private static final String categoriesOther="Other";

    private int mCurrentSatelliteRecordId=-1;
    private static final int CHANNEL_SELECTION = 0;
    private static final int CHANNEL_LIST = 1;
    private int CURRENT_CHANNEL_MODE = CHANNEL_LIST;
    private int mCurMask = CommonIntegration.CH_LIST_MASK;
    private int mCurVal = CommonIntegration.CH_LIST_VAL;

    private int mCurCategories = -1;
    //each check need loop channel list,so define save it.when show check,dismiss reset.
    private boolean hasNextPage = false;
    private MtkTvPWDDialog mtkTvPwd ;
    private ChannelAdapter mChannelAdapter;
    private TIFChannelManager mTIFChannelManager;
    MtkTvAppTVBase mTvAppTvBase;
    private boolean mCanChangeChannel;
    private boolean mIsTifFunction;
    private boolean mIsTuneChannel = true;
    private int preChId;
    private int mSendKeyCode;
    private HandlerThread mHandlerThead;
    private Handler mHandler;
    private Handler mThreadHandler;
    public static int CHANGE_TYPE_NETWORK_INDEX = 5;
    public static int CHANGE_TYPE_FAVORITE_INDEX = 4;
    public static boolean isUKCountry=false;
 // fav move
    TIFChannelInfo favmove =null;
    TIFChannelInfo favmovetag =null;
    int indexTo = -1;
    int moveIsUp = -1;
    int favCount = 0;
    private final FavChannelManager favChannelManager;
    private TIFChannelInfo mSelectionChannel;

    private boolean stoppvr = false ;

    public ChannelListDialog(Context context, int theme) {
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
        favChannelManager = FavChannelManager.getInstance(mContext);
        mTvAppTvBase = new MtkTvAppTVBase();
        mIsTifFunction = CommonIntegration.supportTIFFunction();
        ComponentStatusListener lister = ComponentStatusListener.getInstance();
        lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
        lister.addListener(ComponentStatusListener.NAV_COMPONENT_SHOW, this);
        lister.addListener(ComponentStatusListener.NAV_RESUME, this);
        mChannelTypePacksOrCategory =new ArrayList<Integer>();
//      mTIFChannelManager.getContentResolver().registerContentObserver(TvContract.Channels.CONTENT_URI, true, mTIFChannelContentObserver);
        //mTvCallbackHandler = TvCallbackHandler.getInstance();
        TypedValue sca = new TypedValue();
        mContext.getResources().getValue(R.dimen.nav_channellist_page_max,sca ,true);
        CHANNEL_LIST_PAGE_MAX  =(int) sca.getFloat();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_PAGE_MAX " + CHANNEL_LIST_PAGE_MAX);
    }

    private boolean isFavInCurrentFavType(TIFChannelInfo mCurrentChannel){
        boolean isfav = false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " yiran  CURRENT_CHANNEL_FAV>>> "+CURRENT_CHANNEL_FAV);
        switch(CURRENT_CHANNEL_FAV){
            case 0:
                if (mCurrentChannel.mMtkTvChannelInfo !=null){
                    isfav = mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites1Service();
                }
                break;
            case 1:
                if (mCurrentChannel.mMtkTvChannelInfo !=null){
                    isfav = mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites2Service();
                }
                break;
            case 2:
                if (mCurrentChannel.mMtkTvChannelInfo !=null){
                    isfav = mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites3Service();
                }
                break;
            case 3:
                if (mCurrentChannel.mMtkTvChannelInfo !=null){
                    isfav = mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites4Service();
                }
                break;
            default:
                if (mCurrentChannel.mMtkTvChannelInfo !=null){
                    isfav = mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites1Service();
                }
                break;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " yiran  isfav>>> "+isfav+"  mMtkTvChannelInfo:"+mCurrentChannel.mMtkTvChannelInfo);
        return isfav;
    }

    private boolean isFavInFavType(TIFChannelInfo mCurrentChannel){
        boolean isfav = false;
        isfav = mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites1Service() || mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites2Service() || mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites3Service() || mCurrentChannel.mMtkTvChannelInfo.isDigitalFavorites4Service();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " yiran  isfav>>> "+isfav+"  mMtkTvChannelInfo:"+mCurrentChannel.mMtkTvChannelInfo);
        return isfav;
    }

    private void initHandler() {
        mHandlerThead = new HandlerThread(TAG);
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
                    if (mChannelAdapter == null){
                        return ;
                    }
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
                         //   if (mIsTifFunction) {
                              List<TIFChannelInfo> mTifChannelList = getNextPrePageChListByTIF(false);
                              mChannelAdapter.updateData(mTifChannelList);
                          /*  } else {
                              List<MtkTvChannelInfoBase> tempApiChList = getChannelList(preChId,
                                  MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,
                                  CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                              mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(tempApiChList));
                            }*/
                            // commonIntegration.getChList(curChId, 0, CHANNEL_LIST_PAGE_MAX));
                            mChannelListView.requestFocus();
                            mChannelListView.setSelection(0);
                            mLastSelection = 0;
                          } else if (msg.arg1 == KeyMap.KEYCODE_MTKIR_CHDN) {
                          //  if (mIsTifFunction) {
                              List<TIFChannelInfo> mTifChannelList = getNextPrePageChListByTIF(true);
                              mChannelAdapter.updateData(mTifChannelList);
                           /* } else {
                              List<MtkTvChannelInfoBase> tempApiChList = getChannelList(curChId + 1,
                                  MtkTvChannelList.CHLST_ITERATE_DIR_PREV,
                                  CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                              mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(tempApiChList));
                            }*/
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
                            }else if(msg.arg1 == -1){
                                resetChListByTIF();
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
                          // if(commonIntegration.checkCurChMask(mCurMask ,(mCurVal
                          // & ~MtkTvChCommonBase.SB_VNET_ACTIVE))){
                          // chList.add(commonIntegration.getCurChInfo());
                          // if(chList.size() > CHANNEL_LIST_PAGE_MAX ){
                          // chList.remove(chList.size()-1);
                          // }
                          // }
                          mChannelAdapter.updateData(chList);
                          // commonIntegration.getChList(curChId, 0, CHANNEL_LIST_PAGE_MAX));
                          mChannelListView.requestFocus();
                          mChannelListView.setSelection(msg.arg2);
                          mLastSelection = msg.arg2;
                        } /*
                           * else { mChannelAdapter.updateData(getChannelList(curChId+1,
                           * MtkTvChannelList.CHLST_ITERATE_DIR_PREV
                           * ,CHANNEL_LIST_PAGE_MAX,mCurMark,mCurVal));
                           * //commonIntegration.getChList(curChId, CHANNEL_LIST_PAGE_MAX - 1, 1));
                           * mChannelListView.requestFocus(); mChannelListView.setSelection(mChannelAdapter
                           * .getCount() - 1); }
                           */
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
                        int channelId = getTypeLastChannelId(CURRENT_CHANNEL_TYPE,CURRENT_CHANNEL_SECOND_TYPE);
                        TIFChannelInfo channelInfo = mTIFChannelManager.getTIFChannelInfoById(channelId);
                        if (channelInfo == null){
                            channelInfo = mChannelAdapter.getChannellist().get(0);
                            mTIFChannelManager.selectChannelByTIFInfo(channelInfo);
                            channelId = channelInfo.mInternalProviderFlag3;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran CHANGE_TYPE_CHANGECHANNEL  select 0");
                        }else{
                            if (commonIntegration.checkChMask(channelInfo.mMtkTvChannelInfo, mCurMask, mCurVal)){
                                mTIFChannelManager.selectChannelByTIFInfo(channelInfo);
                            }else{
                                channelInfo = mChannelAdapter.getChannellist().get(0);
                                mTIFChannelManager.selectChannelByTIFInfo(channelInfo);
                                channelId = channelInfo.mInternalProviderFlag3;
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran CHANGE_TYPE_CHANGECHANNEL  select 0");
                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran CHANGE_TYPE_CHANGECHANNEL num:"+channelInfo.mDisplayNumber+" name:"+channelInfo.mDisplayName);
                        }
                        int index = mChannelAdapter.isExistCh(channelId);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran CHANGE_TYPE_CHANGECHANNEL  --- index:"+index);
                       mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                       Message mess = Message.obtain();
                       mess.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                       mess.arg1 = R.id.nav_channel_listview;
                       mess.arg2 = index;
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
                    mCanChangeChannel = true;
                    if (tempChlist == null || tempChlist.isEmpty()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChlist = null");
                        mChannelListView.setAdapter(null);
                        mChannelAdapter = null;
                    }else{
                        mChannelAdapter = new ChannelAdapter(mContext, tempChlist);
                        int index = mChannelAdapter.isExistCh(msg.arg1);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran mChlist === index:" + index);
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
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initHandler current channel is fake,no focus in the channel list ,channel name is "+(commonIntegration.getCurChInfo()==null?commonIntegration.getCurChInfo():commonIntegration.getCurChInfo().getServiceName()));

                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran ChannelListOnKey === :SELECT_TYPE_CHANGE_CH:"+SELECT_TYPE_CHANGE_CH);
                        if(SELECT_TYPE_CHANGE_CH){
                            SELECT_TYPE_CHANGE_CH =false;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey : change type then change chanenls ");
                            if (!stoppvr){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey sendEmptyMessage CHANGE_TYPE_CHANGECHANNEL");
                                mHandler.sendEmptyMessage(CHANGE_TYPE_CHANGECHANNEL);
                            }
                        }
                        mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                        Message msglist = Message.obtain();
                        msglist.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                        msglist.arg1 = R.id.nav_channel_listview;
                        msglist.arg2 = selection;
                        mHandler.sendMessage(msglist);
                        if(tempChlist.get(selection).mDataValue != null && tempChlist.get(selection).mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                            mBlueKeyText.setVisibility(View.VISIBLE);
                            mBlueicon.setVisibility(View.VISIBLE);
                        }else{
                            mBlueKeyText.setVisibility(View.INVISIBLE);
                            mBlueicon.setVisibility(View.INVISIBLE);
                        }
                    }

                    showPageUpDownView();
                    if( commonIntegration.isDisableColorKey()){
                        mChannelListTipView.setVisibility(View.INVISIBLE);
                    }else{
                        mChannelListTipView.setVisibility(View.VISIBLE);
                    }
                    mChannelDetailLayout.setVisibility(View.INVISIBLE);
                    startTimeout(NAV_TIMEOUT_300);
                    break;
                case TYPE_REGET_CHANNELLIST:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_REGET_CHANNELLIST>isShowing()>>" + isShowing()+" visible:"+mChannelListView.getVisibility());
                    if (isShowing() && mChannelListView.getVisibility() == View.VISIBLE) {
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
                        if (mChannelDetailLayout.getVisibility() != View.INVISIBLE) {
                            mChannelDetailLayout.setVisibility(View.INVISIBLE);
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
                    case R.id.nav_channel_typeview:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_SElECTED_FOR_TTS nav_channel_typeview data.arg2  = " + msg.arg2);
                        mChannelTypeView.setSelection(msg.arg2);
                        break;
                    case R.id.nav_channel_sort:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_SElECTED_FOR_TTS nav_channel_sortdata.arg2  = " + msg.arg2);
                        mChannelSortView.setSelection(msg.arg2);
                        break;
                    case R.id.nav_channel_fav_view:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_SElECTED_FOR_TTS nav_channel_fav_view data.arg2  = " + msg.arg2);
                        mChannelTypeFavView.setSelection(msg.arg2);
                        break;
                    case R.id.nav_channel_list_option:
                        mChannelOptionView.setSelection(msg.arg2);
                        break;
                    case R.id.nav_channel_more:
                        mChannelMoreView.setSelection(msg.arg2);
                        break;
                    case R.id.nav_channel_selection:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_SElECTED_FOR_TTS nav_channel_selection data.arg2  = " + msg.arg2);
                        mChannelSelectionView.setSelection(msg.arg2);
                        break;
                    default :
                      /*  View rootview = this.getWindow().getDecorView();
                        int focusId = rootview.findFocus().getId();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," focusid = 0x"+Integer.toHexString(focusId));*/
                        mChannelListView.setSelection(msg.arg2);
                        break;
                    }
                    startTimeout(NAV_TIMEOUT_300);
                    break;
                case TvCallbackConst.MSG_CB_SVCTX_NOTIFY:
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "data.parama1  = " + data.param1);
                    /*  if(data.param1 == CommonIntegration.SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE
                        || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_VIDEO_ONLY_SVC
                        || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_SCRAMBLED_AUDIO_VIDEO_SVC
                        || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_SCRAMBLED_VIDEO_NO_AUDIO_SVC
                        || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_STREAM_STARTED
                        || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_AUDIO_FMT_UPDATE
                        || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_CONN_PMT_UPDATED) {
                      MtkTvConfig config = MtkTvConfig.getInstance();
                      int chanelUpdateMsg = config.getConfigValue(MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG);
                      int channelNewSvcAdded = config.getConfigValue(MenuConfigManager.SETUP_CHANNEL_NEW_SVC_ADDED);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "chanelUpdateMsg:" + chanelUpdateMsg+" channelNewSvcAdded:"+channelNewSvcAdded);
                      if(chanelUpdateMsg == 1 && channelNewSvcAdded > 0) {
                        final LiveTVDialog liveTVDialog = new LiveTVDialog(mContext, 6);
                        liveTVDialog.setMessage(mContext.getResources().getString(R.string.nav_channel_new_channels));
                        liveTVDialog.show();
                        postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if(liveTVDialog != null && liveTVDialog.isShowing()) {
                                    liveTVDialog.dismiss();
                                }
                            }
                        }, 5000);
                        config.setConfigValue(MenuConfigManager.SETUP_CHANNEL_NEW_SVC_ADDED,0);
                      }
                    } */
                    break;


                /*case TvCallbackConst.MSG_CB_CHANNELIST:
                case TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE:
                case TvCallbackConst.MSG_CB_NFY_UPDATE_TV_PROVIDER_LIST:
                    commonIntegration.getChannelAllandActionNum();
                    break;*/
                default:
                    break;
                }
            }

        };
    }

    private Runnable mResetTypeChannelListRunnable = new Runnable() {
        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran thread channel mResetTypeChannelListRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
            List<TIFChannelInfo> tempList=null;
            int chId = getTypeLastChannelId(CURRENT_CHANNEL_TYPE,CURRENT_CHANNEL_SECOND_TYPE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran thread channel mResetTypeChannelListRunnable>> chId " + chId);
            TIFChannelInfo mTIFChannelInfo =mTIFChannelManager.getTIFChannelInfoById(chId);
            if (mTIFChannelInfo != null && commonIntegration.checkChMask(mTIFChannelInfo.mMtkTvChannelInfo, mCurMask, mCurVal)){
                tempList = processTIFChListWithThread(mTIFChannelInfo);
            }else{
                tempList = processTIFChListWithThread(null);
                if (tempList != null && tempList.size() > 0){
                    mTIFChannelInfo = tempList.get(0);
                    if (mTIFChannelInfo != null){
                        chId = TIFFunctionUtil.is3rdTVSource(mTIFChannelInfo)? (int)mTIFChannelInfo.mId: mTIFChannelInfo.mInternalProviderFlag3;
                    }else{
                        chId = 0;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran thread channel mResetTypeChannelListRunnable  else  chId " + chId);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran thread channel mResetTypeChannelListRunnable>>time" );
            Message msg = Message.obtain();
            msg.what = TYPE_RESET_CHANNELLIST;
            msg.arg1 = chId;
            msg.obj = tempList;
            mHandler.sendMessage(msg);
        }
    };
    private Runnable mResetChannelListRunnable = new Runnable() {

        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mResetChannelListRunnable>>" + Thread.currentThread().getId() + ">>" + Thread.currentThread().getName());
            List<TIFChannelInfo> tempList=null;
            int chId = TIFFunctionUtil.getCurrentChannelId();
            TIFChannelInfo mTIFChannelInfo ;
            if(commonIntegration.is3rdTVSource() || (mCurMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && mCurVal ==CommonIntegration.CH_LIST_3RDCAHNNEL_VAL)){
            //  chId=(int)(mTIFChannelManager.getChannelInfoByUri()).mId;
                mTIFChannelInfo=mTIFChannelManager.getChannelInfoByUri();
            //  tempList=getAllChannelListByTIFFor3rdSource(chId);
            }else{
                mTIFChannelInfo =mTIFChannelManager.getTIFChannelInfoById(chId);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mResetChannelListRunnable>> chId " + chId);
            tempList = processTIFChListWithThread(mTIFChannelInfo);
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
            int chId = -1;
            //if (addEraseChannelId != -1 && !isFavTypeState){
            List<TIFChannelInfo> channellist=mChannelAdapter.getChannellist();
            if (addEraseChannelId != -1 && channellist.size() > 1){
                //chId = addEraseChannelId;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mUpdateChannelListRunnable channelid:"+addEraseChannelId);
                int position = mChannelListView.getSelectedItemPosition();
                TIFChannelInfo favchannel=  channellist.get(position);
                chId = favchannel.mInternalProviderFlag3;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mUpdateChannelListRunnable position:"+position);
                if (chId == addEraseChannelId && isFavTypeState){
                    if (position > 0 ){
                        position --;
                    }else if (position == 0 && channellist.size() > 1){
                        position = 1;
                    }
                    if (position < 0){
                        position = 0;
                    }
                    favchannel=  channellist.get(position);
                    chId = favchannel.mInternalProviderFlag3;
                }
                addEraseChannelId = -1;
            }else{
                chId = TIFFunctionUtil.getCurrentChannelId();
            }
            TIFChannelInfo mTIFChannelInfo ;
            if(commonIntegration.is3rdTVSource() || (mCurMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && mCurVal ==CommonIntegration.CH_LIST_3RDCAHNNEL_VAL)){
            //  chId=(int)(mTIFChannelManager.getChannelInfoByUri()).mId;
                mTIFChannelInfo=mTIFChannelManager.getChannelInfoByUri();
            //  tempList=getAllChannelListByTIFFor3rdSource(chId);
            }else{
                mTIFChannelInfo =mTIFChannelManager.getTIFChannelInfoById(chId);
            }
            if (mTIFChannelInfo == null){
                return ;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thread channel mUpdateChannelListRunnable >> chId " + chId);
            tempList = processTIFChListWithThread(mTIFChannelInfo);
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
                          /* ComponentStatusListener.getInstance().updateStatus(
                                   ComponentStatusListener.NAV_CHANNEL_CHANGED, 0);*/
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
                     /*  Message msg = Message.obtain();
                       msg.arg1 = KeyMap.KEYCODE_MTKIR_CHUP;
                       msg.what = SET_SELECTION_INDEX;
                       mHandler.sendMessage(msg);*/
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
                         /*  ComponentStatusListener.getInstance().updateStatus(
                                ComponentStatusListener.NAV_CHANNEL_CHANGED, 0);*/
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
    public ChannelListDialog(Context context) {
        this(context, R.style.nav_dialog);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Constructor!");
    }


    private void  initTypes(){
        String[] originalTypes;
        if (mContext == null){
            return ;
        }
        if((commonIntegration.isNTVPLUSContain()) && commonIntegration.getTunerMode() == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initMaskAndSatellites isOperatorNTVPLUS or isOperatorTKGS" );
            originalTypes = mContext.getResources().getStringArray(R.array.nav_channel_type_for_russia_for_ntv);
        }else if(commonIntegration.isOperatorTKGS() && commonIntegration.getTunerMode() == 2){
            originalTypes = mContext.getResources().getStringArray(R.array.nav_channel_type_for_russia_for_tkgs);
        }else if(commonIntegration.isTELEKARTAContain() && commonIntegration.getTunerMode() == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initMaskAndSatellites isTELEKARTAContain");
            originalTypes = mContext.getResources().getStringArray(R.array.nav_channel_type_for_russia_for_telekarta);
        }else if(commonIntegration.isTVSourceSeparation() || commonIntegration.isCNRegion()){
            originalTypes = mContext.getResources().getStringArray(R.array.nav_channel_type_for_pa);
        }else if(commonIntegration.isUSRegion() || commonIntegration.isSARegion()){
            originalTypes = mContext.getResources().getStringArray(R.array.nav_channel_type_for_us);
        }else {
            originalTypes = mContext.getResources().getStringArray(R.array.nav_channel_type);
        }
        int typesLength = originalTypes.length;
        if (!MarketRegionInfo.F_3RD_INPUTS_SUPPORT){
            typesLength = originalTypes.length - 1;
            CHANGE_TYPE_NETWORK_INDEX = -1;
            CHANGE_TYPE_FAVORITE_INDEX = typesLength-1;
        }else{
            CHANGE_TYPE_NETWORK_INDEX = originalTypes.length - 1;
            CHANGE_TYPE_FAVORITE_INDEX = typesLength-2;
        }
        types = new String[typesLength];
        for (int i =0;i<typesLength;i++){
            types[i] = originalTypes[i];
        }

        String[] menuChannelListType = mContext.getResources().getStringArray(
                R.array.menu_tv_channel_listtype);
        String profileName = MtkTvConfig.getInstance().
                getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);

        if (PreferenceUtil.getInstance(mContext).mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) != 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName " +profileName);
            if( TextUtils.isEmpty(profileName)){
                profileName=menuChannelListType[1];
            }
            menuChannelListType[1]= profileName;
            types[0] = profileName;
        }
    }

    public int getChannelNetworkIndex() {
        initTypes();
        return CHANGE_TYPE_NETWORK_INDEX;
    }

    private void initMaskAndSatellites() {
        CH_TYPE = CommonIntegration.CH_TYPE_BASE+commonIntegration.getSvl();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initMaskAndSatellites CH_TYPE:"+CH_TYPE );
        int lastSort = CURRENT_CHANNEL_SORT;
        switch(MarketRegionInfo.getCurrentMarketRegion()){
            case MarketRegionInfo.REGION_CN:
            case MarketRegionInfo.REGION_EU:
                CURRENT_CHANNEL_MODE = CHANNEL_SELECTION;

                CURRENT_CHANNEL_SORT = mSaveValue.readValue(CH_SORT, 0);
           //     CURRENT_OPTION = mSaveValue.readValue(SEL_MORE, 0);
                CURRENT_CHANNEL_SECOND_TYPE =mSaveValue.readValue(CH_TYPE_SECOND, -1);
                saveCurrentSecondName(CURRENT_CHANNEL_SECOND_TYPE);
                CURRENT_CHANNEL_TYPE = mSaveValue.readValue(CH_TYPE,ALL_CHANNEL);
                if (commonIntegration.isCurrentSourceATVforEuPA()){
                    CURRENT_CHANNEL_FAV =mSaveValue.readValue(FAVOURITE_TYPE, 0);
                }else{
                    CURRENT_CHANNEL_FAV= mSaveValue.readValue(CH_TYPE_FAV,0);
                }
                initTypes();
                break;
            case MarketRegionInfo.REGION_US:
            case MarketRegionInfo.REGION_SA:
                CURRENT_CHANNEL_MODE = CHANNEL_SELECTION;
                CURRENT_CHANNEL_SORT = mSaveValue.readValue(CH_SORT, 0);
          //      CURRENT_OPTION = mSaveValue.readValue(SEL_MORE, 0);
                CURRENT_CHANNEL_SECOND_TYPE =mSaveValue.readValue(CH_TYPE_SECOND, -1);
                saveCurrentSecondName(CURRENT_CHANNEL_SECOND_TYPE);
                CURRENT_CHANNEL_TYPE = mSaveValue.readValue(CH_TYPE,ALL_CHANNEL);
                if (commonIntegration.isCurrentSourceATVforEuPA()){
                    CURRENT_CHANNEL_FAV =mSaveValue.readValue(FAVOURITE_TYPE, 0);
                }else{
                    CURRENT_CHANNEL_FAV= mSaveValue.readValue(CH_TYPE_FAV,0);
                }
                initTypes();
                break;
            default :
                break;
        }

        if (lastSort != CURRENT_CHANNEL_SORT){
            mTIFChannelManager.setCurrentChannelSort(CURRENT_CHANNEL_SORT);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isSelectionMode CURRENT_CHANNEL_TYPE ="+ CURRENT_CHANNEL_TYPE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isSelectionMode CURRENT_CHANNEL_SORT ="+ CURRENT_CHANNEL_SORT);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isSelectionMode CURRENT_CHANNEL_MODE ="+ CURRENT_CHANNEL_MODE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isSelectionMode CURRENT_CHANNEL_SECOND_TYPE ="+ CURRENT_CHANNEL_SECOND_TYPE);
        favChannelManager.setFavoriteType(CURRENT_CHANNEL_FAV);
        if (isSelectionMode()) {
            if(CURRENT_CHANNEL_SECOND_TYPE != -1 && commonIntegration.isEURegion() && commonIntegration.getTunerMode() == 2){
                resetCategories();
            }else{
                CURRENT_CHANNEL_SECOND_TYPE =-1;
                if(typesSecond != null){
                    typesSecond.clear();
                }
             //   CURRENT_CHANNEL_TYPE = mSaveValue.readValue(CH_TYPE,ALL_CHANNEL);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_TYPE>>>" + CURRENT_CHANNEL_TYPE);
                resetMask();
            }

            /*if (commonIntegration.isGeneralSatMode()) {
                resetSatellites();
            }*/
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_TYPE>>mCurrentSatelliteRecordId>" + mCurrentSatelliteRecordId);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
        setContentView(R.layout.nav_channellist);
        setWindowPosition();
        findViews();
        isUKCountry=TVContent.getInstance(mContext).isUKCountry()&&CommonUtil.isSupportFVP();
        if(CommonIntegration.isUSRegion()){
             loadChannelTypeResForUS();
        }else{
            loadChannelTypeRes();
        }
        initFavtypes();
    }

    protected void onStart() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart");
       /* if(AnimationManager.getInstance().getIsAnimation()){
            AnimationManager.getInstance().channelListEnterAnimation(mChannelListLayout);
        }*/
        mChannelDetailTileText.setText(mContext.getResources().getString(R.string.nav_channel_details));
      /*  if (commonIntegration.isGeneralSatMode()) {
            mExitAndSatTv.setText(R.string.nav_list_satellite);
            updateSatelliteList();
        } else {
            mExitAndSatTv.setText(R.string.nav_exit);
        }*/
        mChannelListView.setVisibility(View.VISIBLE);
        mChannelTypeView.setVisibility(View.GONE);
        mChannelMoreView.setVisibility(View.GONE);
        mChannelSatelliteView.setVisibility(View.GONE);
        mChannelSatelliteSecondView.setVisibility(View.GONE);
        mChannelSelectionView.setVisibility(View.GONE);
        this.startTimeout(NAV_TIMEOUT_300);
        super.onStart();
    }

    @Override
    public boolean isCoExist(int componentID) {
        boolean isCoExist = false;
        switch (componentID) {
//remove tshift useless code !
//      case NAV_COMP_ID_PVR_TIMESHIFT:
//          if(TimeShiftManager.getInstance().pvrIsPlaying()){
//              isCoExist = false;
//          }else{
//              isCoExist = true;
//          }
//          break;
//remove tshift useless code !
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
            if (mIsTifFunction && ChannelListDialog.this.isShowing()) {
                handleCallBack();
            }
        }

    }

    private int indexOfSatReId(int recId) {
        int index = -1;
        if (mSatelliteListinfo != null) {
            for (MtkTvDvbsConfigInfoBase configInfo:mSatelliteListinfo) {
                index++;
                if (recId == configInfo.getSatlRecId()) {
                    return index;
                }
            }
        }
        return -1;
    }

    private void resetSatellites() {
        mCurrentSatelliteRecordId = mSaveValue.readValue(SATELLITE_RECORDID,0);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentSatelliteRecordId>updateSatelliteList>>" + mCurrentSatelliteRecordId);
        mSatelliteListinfo = commonIntegration.getSatelliteListInfo(commonIntegration.getSatelliteCount());
        mSatelliteListinfo.add(0, commonIntegration.getDefaultSatellite("All"));
        mSatelliteRecords = commonIntegration.getSatelliteNames(mSatelliteListinfo);
        if (indexOfSatReId(mCurrentSatelliteRecordId) == -1) {
            mCurrentSatelliteRecordId = 0;
            mSaveValue.saveValue(SATELLITE_RECORDID,mCurrentSatelliteRecordId);
        }
    }

    public void updateSatelliteList() {
        resetSatellites();
//      mSatelliteRecords = mContext.getResources().getStringArray(R.array.nav_channel_type);
        ArrayList<HashMap<String,String>> recordList = new ArrayList<HashMap<String,String>>();
        for(String type: mSatelliteRecords){
            HashMap<String,String> tmpType = new HashMap<String,String>();
            tmpType.put(SATELLITE_RECORDID,type);
            recordList.add(tmpType);
        }
        SimpleAdapter satelliteListAdapter = new SimpleAdapter(mContext,recordList,
            R.layout.nav_channel_type_item,new String[]{SATELLITE_RECORDID},new int[]{R.id.nav_channel_type_list_item});
        mChannelSatelliteView.setAdapter(satelliteListAdapter);
        mChannelSatelliteView.setOnKeyListener(new ChannelListOnKey());
    }

    public void handleCallBack(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCallBack()");
        mHandler.removeMessages(TYPE_REGET_CHANNELLIST);
        mHandler.sendEmptyMessageDelayed(TYPE_REGET_CHANNELLIST, 1500);
    }

    public void handleUpdateCallBack(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUpdateCallBack()");
        mChannelListView.invalidateViews();
    }

    public boolean channelPre(){
        Log.d(TAG,"channelPre start");
        if(commonIntegration.isCurrentSourceATVforEuPA()
                || (commonIntegration.isCNRegion() && commonIntegration.isCurrentSourceATV())){
            mCurMask=commonIntegration.CH_LIST_ANALOG_MASK;
            mCurVal =commonIntegration.CH_LIST_ANALOG_VAL;
            setFavTypeState(false);
            Log.d(TAG,"channelUpDown mCurMask :"+mCurMask +" ,mCurVal :"+mCurVal);
        }else{
            initMaskAndSatellites();
        }

        TIFChannelInfo tIFChannelInfo = mTIFChannelManager.getTIFChannelInfoById(commonIntegration
                  .getLastChannelId());
        int lastId = commonIntegration.getLastChannelId();
        int currentId = commonIntegration.getCurrentChannelId();
        if (tIFChannelInfo == null || lastId == currentId) {
            return false;
        }

        MtkTvChannelInfoBase tempApiChannel =  mTIFChannelManager.getAPIChannelInfoByChannelId(lastId);
        //int mCurCategories = TIFFunctionUtil.getmCurCategories();
        if (commonIntegration.isOperatorContain() || commonIntegration.isTELEKARTAContain()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre  >>>"+commonIntegration.isOperatorContain()+" pp:"+commonIntegration.isTELEKARTAContain());
          if (mCurCategories != -1){
              return TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories());
          }else{
              return TIFFunctionUtil.checkChMask(tempApiChannel, mCurMask,mCurVal);
          }
        }else{
            return TIFFunctionUtil.checkChMask(tempApiChannel, mCurMask,mCurVal);
        }
    }

    public boolean checkChannelType(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChannelType start");
        int channelid = commonIntegration.getCurrentChannelId();
        MtkTvChannelInfoBase tempApiChannel =  mTIFChannelManager.getAPIChannelInfoByChannelId(channelid);
        if (commonIntegration.isOperatorContain() || commonIntegration.isOperatorTELEKARTA()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelPre  >>>"+commonIntegration.isOperatorContain()+" pp:"+commonIntegration.isOperatorTELEKARTA());
            if (mCurCategories != -1){
                return TIFFunctionUtil.checkChCategoryMask(tempApiChannel, TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories());
            }else{
                return commonIntegration.checkCurChMask(mCurMask,mCurVal);
            }
        }else{
            return commonIntegration.checkCurChMask(mCurMask,mCurVal);
        }
    }

    public void currentChannelTypeWithoutView(){
        if (!checkChannelType()) {
            CURRENT_CHANNEL_TYPE = ALL_CHANNEL;
            mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
            typesSecond = null;
            setFavTypeState(false);
            //initMaskAndSatellites();
            resetMask();
        }
    }

    /**
     * if current type not contain current channel ,change type to broadcast
     */
    public void currentChannelType(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannelType start");
        if (!checkChannelType()){
            CURRENT_CHANNEL_TYPE = ALL_CHANNEL;
            resetMask();
            typesSecond = null;

            if (mIsTifFunction) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                    resetChList();
                }else {
                    if(mTIFChannelManager.getChannelListConfirmLength(1, mCurMask, mCurVal) > 0){
                        if (mChannelListView != null){
                            mChannelListView.setVisibility(View.GONE);
                        }
                        if (mChannelTypeView != null){
                            mChannelTypeView.setVisibility(View.GONE);
                        }
                        SELECT_TYPE_CHANGE_CH = false;
                        if( chanelListProgressbar != null && chanelListProgressbar.getVisibility() != View.VISIBLE ){
                            chanelListProgressbar.setVisibility(View.VISIBLE);
                        }
                        resetTypeChListByTIF();
                        mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                        CURRENT_CHANNEL_SECOND_TYPE = -1;
                        mSaveValue.saveValue(CH_TYPE_SECOND,CURRENT_CHANNEL_SECOND_TYPE);
                        //  mSaveValue.saveValue(CH_TYPE_FAV,-1);
                        CURRENT_OPTION=ALL_CHANNEL;
                    }else{
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANGE_TYPE_CHANGECHANNEL current type no chanenls,return last type");
                        Toast.makeText(mContext,  mContext.getResources().getString(R.string.nav_select_type_no_channel) , Toast.LENGTH_SHORT).show();
                        initMaskAndSatellites();
                    }
                }
            } else {
                resetChList();
            }
        }
    }
   /**
    * true (up) ,false (down)
    */
     public boolean channelUpDown(boolean isUp){
         currentChannelTypeWithoutView();
        Log.d(TAG,"channelUpDown isUp = "+ isUp);
        if(commonIntegration.isCurrentSourceATVforEuPA()
                || (commonIntegration.isCNRegion() && commonIntegration.isCurrentSourceATV())){
            mCurMask=commonIntegration.CH_LIST_ANALOG_MASK;
            mCurVal =commonIntegration.CH_LIST_ANALOG_VAL;
            setFavTypeState(false);
            Log.d(TAG,"channelUpDown mCurMask :"+mCurMask +" ,mCurVal :"+mCurVal);
        }else{
            initMaskAndSatellites();
        }
        if(isUp){
            if (mIsTifFunction) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                 if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                     final int tempchid= commonIntegration.get2NDCurrentChannelId();
                     List<MtkTvChannelInfoBase> channelinfolist=null;
                     if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                             && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                              channelinfolist = commonIntegration.getChannelListByMaskAndSat(tempchid, MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,
                                      1, mCurMask, mCurVal,mCurrentSatelliteRecordId);
                      } else {
                              channelinfolist= commonIntegration.getChListByMask(tempchid, MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,
                                      1, mCurMask, mCurVal);
                      }
                     if(channelinfolist== null || channelinfolist.isEmpty()){
                             return false;
                     }
                     TurnkeyUiMainActivity.getInstance().getPipView().tune(InputSourceManager.getInstance().getTvInputInfo("sub").getId(), MtkTvTISMsgBase.createSvlChannelUri(channelinfolist.get(0).getChannelId()));
                     int  channelid= commonIntegration.get2NDCurrentChannelId();
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUp channelid = "+channelid);
                     return true;
                  }else{
                    if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                            && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                        return mTIFChannelManager.channelUpDownByMaskAndSat(mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurrentSatelliteRecordId, isUp);
                    } else {
                        if(isFavTypeState){
                            return getFavDownOrUPCh(isUp);
                        }else{
                            return mTIFChannelManager.channelUpDownByMask(isUp,
                                    mCurMask, mCurVal);
                        }
                    }
                  }
            } else {
                if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                        && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                    return commonIntegration.channelUpDownByMaskAndSat(mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurrentSatelliteRecordId, isUp);
                } else {
                    if(isFavTypeState){
                        return getFavDownOrUPCh(isUp);
                    }else{
                        return mTIFChannelManager.channelUpDownByMask(isUp,
                                mCurMask, mCurVal);
                    }
                }
            }
        }else{
            if (mIsTifFunction) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                  if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                         final int tempchid= commonIntegration.get2NDCurrentChannelId();
                         List<MtkTvChannelInfoBase> channelinfolist=null;
                         if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                                 && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                                  channelinfolist = commonIntegration.getChannelListByMaskAndSat(tempchid, MtkTvChannelList.CHLST_ITERATE_DIR_PREV,
                                          1, mCurMask, mCurVal,mCurrentSatelliteRecordId);
                          } else {
                                  channelinfolist= commonIntegration.getChListByMask(tempchid, MtkTvChannelList.CHLST_ITERATE_DIR_PREV,
                                          1, mCurMask, mCurVal);
                          }
                         if(channelinfolist ==null || channelinfolist.isEmpty()){
                             return false;
                         }
                         TurnkeyUiMainActivity.getInstance().getPipView().tune(InputSourceManager.getInstance().getTvInputInfo("sub").getId(), MtkTvTISMsgBase.createSvlChannelUri(channelinfolist.get(0).getChannelId()));
                         int  channelid= commonIntegration.get2NDCurrentChannelId();
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelUp channelid = "+channelid);
                         return true;
                  }else{
                        if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                                && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                            return mTIFChannelManager.channelUpDownByMaskAndSat(mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurrentSatelliteRecordId, isUp);
                        } else {
                            if(isFavTypeState){
                                return  getFavDownOrUPCh(isUp);
                            }else{
                                return mTIFChannelManager.channelUpDownByMask(isUp,
                                        mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE,
                                        mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE);
                            }
                        }
               }
            } else {
                if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                        && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                    return commonIntegration.channelUpDownByMaskAndSat(mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE, mCurrentSatelliteRecordId, isUp);
                } else {
                    if(isFavTypeState){
                        return  getFavDownOrUPCh(isUp);
                    }else{
                        return mTIFChannelManager.channelUpDownByMask(isUp,
                                mCurMask | MtkTvChCommonBase.SB_VNET_VISIBLE,
                                mCurVal | MtkTvChCommonBase.SB_VNET_VISIBLE);
                    }

                }
            }
        }
    }

    /**
     * fav  down or up ,true(down),false(up)
     */
    boolean getFavDownOrUPCh(boolean isDown) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavDownOrUPCh isDown " + isDown);
      List<MtkTvChannelInfoBase> tempApiChList = null;
      TIFChannelInfo chInfo = null;
      if (isDown) {
          tempApiChList= commonIntegration.getFavoriteListByFilter(mCurMask,
                    TIFFunctionUtil.getCurrentChannelId() ,false, 0, 1,CURRENT_CHANNEL_FAV);
      } else {
          tempApiChList= commonIntegration.getFavoriteListByFilter(mCurMask,
                    TIFFunctionUtil.getCurrentChannelId(),false, 1, 0,CURRENT_CHANNEL_FAV);
      }
      if(tempApiChList != null && !tempApiChList.isEmpty()){
          chInfo=mTIFChannelManager.getTIFChannelInfoById(tempApiChList.get(0).getChannelId());
      }
     return mTIFChannelManager.selectChannelByTIFInfo(chInfo);
    }
    private void resetMask(){
        int mask = CommonIntegration.CH_LIST_MASK;
        int val = CommonIntegration.CH_LIST_VAL;
        setFavTypeState(false);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"resetChList CURRENT_CHANNEL_TYPE = "+ CURRENT_CHANNEL_TYPE +"CURRENT_CHANNEL_MODE ="+CURRENT_CHANNEL_MODE);
        int tmpCurrentChannelType=0;
        if((commonIntegration.isOperatorNTVPLUS() || commonIntegration.isOperatorTKGS()) && CURRENT_CHANNEL_TYPE > CATEGORIES_CHANNEL && commonIntegration.getTunerMode() == 2){
            tmpCurrentChannelType =CURRENT_CHANNEL_TYPE - CATEGORIES_CHANNEL;
        }else if(commonIntegration.isTELEKARTAContain() && CURRENT_CHANNEL_TYPE > PACKS_CHANNEL && commonIntegration.getTunerMode() == 2){
            tmpCurrentChannelType =CURRENT_CHANNEL_TYPE - PACKS_CHANNEL;
        }else{
            tmpCurrentChannelType = CURRENT_CHANNEL_TYPE;
        }
        if(isSelectionMode()){

            switch(tmpCurrentChannelType){
            case ALL_CHANNEL:
                mask = CommonIntegration.CH_LIST_MASK;
                val = CommonIntegration.CH_LIST_VAL;
                break;
            case DIGITAL_CHANNEL:
                mask = CommonIntegration.CH_LIST_DIGITAL_MASK;
                val = CommonIntegration.CH_LIST_DIGITAL_VAL;
                break;
            case RADIO_CHANNEL:
                mask = CommonIntegration.CH_LIST_RADIO_MASK;
                val = CommonIntegration.CH_LIST_RADIO_VAL;
                break;
            case FREE_CHANNEL:
                mask = CommonIntegration.CH_LIST_FREE_MASK;
                val = CommonIntegration.CH_LIST_FREE_VAL;
                break;
            case ENCRYPTED_CHANNEL:
                mask = CommonIntegration.CH_LIST_SCRAMBLED_MASK;
                val = CommonIntegration.CH_LIST_SCRAMBLED_VAL;
                break;
            case ANALOG_CHANNEL:
                mask = CommonIntegration.CH_LIST_ANALOG_MASK;
                val = CommonIntegration.CH_LIST_ANALOG_VAL;
                break;
             default:
                    mask = CommonIntegration.CH_LIST_MASK;
                    val = CommonIntegration.CH_LIST_VAL;
                    break;
            }
            if (MarketRegionInfo.F_3RD_INPUTS_SUPPORT){
                if( types !=null && (types.length-1)== CURRENT_CHANNEL_TYPE){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetMask types.length-1 = "+(types.length-1));
                    mask = CommonIntegration.CH_LIST_3RDCAHNNEL_MASK;
                    val = CommonIntegration.CH_LIST_3RDCAHNNEL_VAL;
                }else if(types !=null && (types.length-2)== CURRENT_CHANNEL_TYPE){
                    resetFavMask();
                    return ;
                }
            }else{
                if(types !=null && (types.length-1)== CURRENT_CHANNEL_TYPE){
                    resetFavMask();
                    return ;
                }
            }
        }
        mCurCategories = -1;
        TIFFunctionUtil.setmCurCategories(mCurCategories);
        mCurMask = mask;
        mCurVal = val;
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMask,mCurMask);
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMaskvalue,mCurVal);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetMask mCurCategories ="+mCurCategories);
        Log.d(TAG, "resetMask mCurMask ="+mCurMask+" mCurVal: "+mCurVal);
    }


    private void resetFavMask(){
        int mask = CommonIntegration.CH_LIST_MASK;
        int val = CommonIntegration.CH_LIST_VAL;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"resetFavMask CURRENT_CHANNEL_TYPE = "+ CURRENT_CHANNEL_TYPE +" CURRENT_CHANNEL_FAV="+CURRENT_CHANNEL_FAV);

            switch(CURRENT_CHANNEL_FAV){
            case CommonIntegration.FAVOURITE_1:
            case CommonIntegration.FAVOURITE_2:
            case CommonIntegration.FAVOURITE_3:
            case CommonIntegration.FAVOURITE_4:
                mask = commonIntegration.favMask[CURRENT_CHANNEL_FAV];
                val = commonIntegration.favMask[CURRENT_CHANNEL_FAV];
                break;
           default:
               mask = commonIntegration.favMask[CommonIntegration.FAVOURITE_1];
               val = commonIntegration.favMask[CommonIntegration.FAVOURITE_1];
               break;
            }
        setFavTypeState(true);
        mCurCategories = -1;
        TIFFunctionUtil.setmCurCategories(mCurCategories);
        mCurMask = mask;
        mCurVal = val;
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMask,mCurMask);
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMaskvalue,mCurVal);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetFavMask mCurCategories ="+mCurCategories);
        Log.d(TAG, "resetFavMask mCurMask ="+mCurMask+" mCurVal: "+mCurVal);
        favChannelManager.setFavoriteType(CURRENT_CHANNEL_FAV);
    }
    private void resetMaskForCategories(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"resetMaskForCategories CURRENT_CHANNEL_TYPE = "+ CURRENT_CHANNEL_TYPE +"CURRENT_CHANNEL_MODE ="+CURRENT_CHANNEL_MODE);
        mCurMask = CommonIntegration.CH_LIST_MASK;
        mCurVal =  CommonIntegration.CH_LIST_VAL;
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMask,mCurMask);
        mSaveValue.saveValue(CommonIntegration.channelListfortypeMaskvalue,mCurVal);
        setFavTypeState(false);
        Log.d(TAG, "resetMaskForCategories mCurMask ="+mCurMask+" mCurVal: "+mCurVal);
    }
    private void resetCategories(){
        int tmpCurrentChannelSecondType = CURRENT_CHANNEL_SECOND_TYPE;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"resetCategories CURRENT_CHANNEL_SECOND_TYPE = "+ tmpCurrentChannelSecondType);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetCategories mCurCategories ="+mCurCategories);
        if (tmpCurrentChannelSecondType == -1){
            return ;
        }
        resetMaskForCategories();
        if(typesSecond == null || typesSecond.isEmpty() || mChannelTypePacksOrCategory.isEmpty()){
            initSecondType();
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetCategories typesSecond  "+ (typesSecond == null?typesSecond:typesSecond.size()) +" mChannelTypePacksOrCategory "+mChannelTypePacksOrCategory.size());
        if(typesSecond !=null && typesSecond.size()>tmpCurrentChannelSecondType && typesSecond.get(tmpCurrentChannelSecondType).equalsIgnoreCase(categoriesOther)){
            mCurCategories= 0;
        }else if(mChannelTypePacksOrCategory.size() >tmpCurrentChannelSecondType){
            mCurCategories = 1 << mChannelTypePacksOrCategory.get(tmpCurrentChannelSecondType);
        }
        TIFFunctionUtil.setmCurCategories(mCurCategories);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetCategories mCurCategories ="+mCurCategories);
    }

    private boolean isSelectionMode(){
        if((commonIntegration.isTVSourceSeparation() && commonIntegration.isCurrentSourceATVforEuPA()) ||
                (commonIntegration.isCNRegion() && commonIntegration.isCurrentSourceATV())){
            return false;
        }else{
            return true;
        }
    }

    private void showSelectMore(){
        loadSelectMoreRes();
        mTitleText.setText(mContext.getResources().getString(R.string.nav_select_more));
        mChannelListTipView.setVisibility(View.GONE);
    }
    private void showSelectType(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSelectType start ");
        if ((CommonIntegration.isEURegion() && !commonIntegration.isTVSourceSeparation()) ||CommonIntegration.isSARegion() || (commonIntegration.isTVSourceSeparation() && commonIntegration.isCurrentSourceDTV())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_YELLOW eu sa ");
            loadChannelTypeRes();
            mChannelListView.setVisibility(View.GONE);
            mChannelListTipView.setVisibility(View.GONE);
            mChannelMoreView.setVisibility(View.GONE);
            mChannelSatelliteSecondView.setVisibility(View.GONE);
            if (mChannelDetailLayout.getVisibility() == View.VISIBLE) {
                mChannelDetailLayout.setVisibility(View.INVISIBLE);
            }
            mTitleText.setText(mContext.getResources().getString(R.string.nav_select_type));
        } else if (CommonIntegration.isUSRegion()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_YELLOW eu us ");
            loadChannelTypeResForUS();
            mChannelListView.setVisibility(View.GONE);
            mChannelMoreView.setVisibility(View.GONE);
            mChannelTypeView.setVisibility(View.VISIBLE);
            mChannelListTipView.setVisibility(View.GONE);
            mChannelTypeView.setFocusable(true);
            mChannelTypeView.requestFocus();
            mChannelSatelliteSecondView.setVisibility(View.GONE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_YELLOW eu us CURRENT_CHANNEL_TYPE is "+CURRENT_CHANNEL_TYPE);
            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
            Message msg = Message.obtain();
            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
            msg.arg1 = R.id.nav_channel_typeview;
            msg.arg2 = CURRENT_CHANNEL_TYPE;
            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
            if (mChannelDetailLayout.getVisibility() == View.VISIBLE) {
                mChannelDetailLayout.setVisibility(View.INVISIBLE);
            }
            mTitleText.setText(mContext.getResources().getString(R.string.nav_select_type));
           /* mChannelListView.setVisibility(View.GONE);
            mChannelListTipView.setVisibility(View.INVISIBLE);
            mChannelSatelliteSecondView.setVisibility(View.GONE);
            loadChannelTypeRes();
            mTitleText.setText(mTitlePre);*/
        }
        /*else if (CommonIntegration.isCNRegion()) {
            //mChannelListView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
            //        KeyEvent.KEYCODE_DPAD_CENTER));
//          dismiss();
        }*/
    }
     @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        boolean isHandled = true;
        if (mContext == null || !mCanChangeChannel) {
            return false;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyHandler keyCode 111= "+keyCode);
        keyCode = KeyMap.getKeyCode(keyCode, event);
        this.startTimeout(NAV_TIMEOUT_300);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyHandler keyCode 222= "+keyCode);

        switch (keyCode) {
        case KeyMap.KEYCODE_BACK:
            exit();
            break;
        case KeyMap.KEYCODE_MTKIR_BLUE:
            if( commonIntegration.isDisableColorKey()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDisableColorKey is not deal");
                return false;
            }
            if(mBlueKeyText.getVisibility() == View.VISIBLE){
                List<TIFChannelInfo> channellist=mChannelAdapter.getChannellist();
                TIFChannelInfo favchannel = null;
                if (ttsSelectchannelIndex >= 0){
                    favchannel = mChannelAdapter.getItem(ttsSelectchannelIndex);
                }else{
                    favchannel =  channellist.get(mChannelListView.getSelectedItemPosition());
                }
                if (favchannel == null){
                    return true;
                }
                loadFavSelection(favchannel);
            }
            break;
        case KeyMap.KEYCODE_MTKIR_YELLOW:
      //    if(!commonIntegration.is3rdTVSource()){
            if( commonIntegration.isDisableColorKey()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDisableColorKey is not deal");
                return false;
            }
            if(isSelectionMode()){
                showSelectMore();
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_YELLOW PA ATV ");
                mChannelListView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_CENTER));
                if (TurnkeyUiMainActivity.getInstance() != null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity blue to favlist");
                    return TurnkeyUiMainActivity.getInstance().onKeyHandler(KeyMap.KEYCODE_RO, event);
               }
            }
  //        }
            break;
        case KeyMap.KEYCODE_MTKIR_RED:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED");
            if( commonIntegration.isDisableColorKey()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDisableColorKey is not deal");
                return false;
            }
           if( mChannelListPageUpDownLayout.getVisibility() != View.VISIBLE){
               break;
           }
            if (mChannelListView.getSelectedItemPosition() != 0) {
                if(isFavTypeState && isFavListMove){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED!!!!! isFavListMove is start");
                        if (mChannelListView.getSelectedItemPosition() >0){
                            List<TIFChannelInfo> favlist=  mChannelAdapter.getChannellist();
                            favmove = favlist.get(0);
                            favlist.remove(mChannelListView.getSelectedItemPosition());
                            favlist.add(0,favmovetag);
                            mChannelAdapter.setFavlistMovepostion(0);
                            mChannelAdapter.updateData(favlist);
                        }
                    }
                mChannelListView.requestFocus();
                mChannelListView.setSelection(0);
                startTimeout(NAV_TIMEOUT_300);
                return true;
            } else if (hasNextPage) {
                mChannelDetailLayout.setVisibility(View.INVISIBLE);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED!!!!! isFavTypeState +"+isFavTypeState+" isFavListMove is "+isFavListMove);
                if(isFavTypeState && isFavListMove){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED!!!!! isFavListMove is start");
                    if (mChannelListView.getSelectedItemPosition() == 0 ) {
                        List<TIFChannelInfo> favlist=  getFavNextPrePageChList(false);
                        if (favlist == null || favlist.isEmpty()){
                            return true;
                        }
                        for(int i=0;i<favlist.size();i++){
                            if(favmovetag.mInternalProviderFlag3 == favlist.get(i).mInternalProviderFlag3){
                                favlist.remove(i);
                                break;
                            }
                        }
                        if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                            favlist.remove(0);
                        }
                        favmove = favlist.get(0);
                        favlist.remove(favlist.size()-1);
                        favlist.add(0,favmovetag);
                        mChannelAdapter.setFavlistMovepostion(0);
                        mChannelAdapter.updateData(favlist);
                        mChannelListView.setSelection(0);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    }
                }else{
                    mChannelDetailLayout.setVisibility(View.INVISIBLE);
                    if(mChannelListView.getSelectedItemPosition() == 0){
                        if (mIsTifFunction) {
                            if(isFavTypeState){
                                List<TIFChannelInfo> favlist=  getFavNextPrePageChList(false);
                                if (favlist == null || favlist.isEmpty()){
                                    return true;
                                }
                                if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                                    favlist.remove(0);
                                }
                                mChannelAdapter.updateData(favlist);
                            }else{
                                mChannelAdapter.updateData(getNextPrePageChListByTIF(false));
                            }
                        } else {
                            mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(false)));
                        }
                        mChannelListView.setSelection(0);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    }else{
                        startTimeout(NAV_TIMEOUT_300);
                        if(mChannelListView.getSelectedItemPosition() == 0){
                            mChannelListView.setSelection(0);
                            return true;
                        }
                        return false;
                    }
                }
            }

            break;
        case KeyMap.KEYCODE_MTKIR_GREEN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_GREEN");
            if( commonIntegration.isDisableColorKey()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDisableColorKey is not deal");
                return false;
            }
            if( mChannelListPageUpDownLayout.getVisibility() != View.VISIBLE){
               break;
            }
            if (mChannelListView.getSelectedItemPosition() != mChannelAdapter.getCount() - 1) {
                if(isFavTypeState && isFavListMove){
                    List<TIFChannelInfo> favlist=  mChannelAdapter.getChannellist();
                    favmove = favlist.get(mChannelAdapter.getCount() - 1);
                    favlist.remove(mChannelListView.getSelectedItemPosition());
                    favlist.add(favmovetag);
                    mChannelAdapter.setFavlistMovepostion(mChannelAdapter.getCount() - 1);
                    mChannelAdapter.updateData(favlist);
                }
                mChannelListView.requestFocus();
                mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
                startTimeout(NAV_TIMEOUT_300);
                return true;
            } else if (hasNextPage) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_GREEN!!!!! isFavTypeState is "+isFavTypeState+" isFavListMove is "+isFavListMove);
                if(isFavTypeState && isFavListMove){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_GREEN!!!!! isFavListMove is start");
                    if (mChannelListView.getSelectedItemPosition()  == mChannelAdapter.getCount() - 1 ) {
                        List<TIFChannelInfo> favlist=  getFavNextPrePageChList(true);
                        if (favlist == null || favlist.isEmpty()){
                            return true;
                        }
                        for(int i=0;i<favlist.size();i++){
                            if(favmovetag.mInternalProviderFlag3 == favlist.get(i).mInternalProviderFlag3){
                                favlist.remove(i);
                                break;
                            }
                        }
                        if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                            favlist.remove(favlist.size()-1);
                        }
                        favmove=favlist.get(favlist.size()-1);
                        favlist.remove(0);
                        favlist.add(favmovetag);
                        mChannelAdapter.setFavlistMovepostion(favlist.size()-1);
                        mChannelAdapter.updateData(favlist);
                        mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    }
                        return false;

                }else{
                    mChannelDetailLayout.setVisibility(View.INVISIBLE);
                    if(mChannelListView.getSelectedItemPosition()  == mChannelAdapter.getCount()-1 && hasNextPage){
                        if (mIsTifFunction) {
                            if(isFavTypeState){
                                List<TIFChannelInfo> favlist=  getFavNextPrePageChList(true);
                                if (favlist == null || favlist.isEmpty()){
                                    return true;
                                }
                                if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                                    favlist.remove(favlist.size()-1);
                                }
                                mChannelAdapter.updateData(favlist);
                            }else{
                                mChannelAdapter.updateData(getNextPrePageChListByTIF(true));
                            }
                        } else {
                            mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(true)));
                        }
                        mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    }else{
                        startTimeout(NAV_TIMEOUT_300);
                        if(mChannelListView.getSelectedItemPosition() == mChannelAdapter.getCount()-1){
                            mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                            return true;
                        }
                        return false;
                    }
                }
            }
            break;
        case KeyMap.KEYCODE_MTKIR_CHUP:
            if (mCanChangeChannel) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
       //         mCanChangeChannel = false;
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
            if (mCanChangeChannel && channelPre()) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
             //   mCanChangeChannel = false;
                mHandler.sendEmptyMessageDelayed(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_PRECH....start");
                mThreadHandler.post(mChannelPreRunnable);
            }
            break;
        case KeyMap.KEYCODE_MTKIR_CHDN:
            if (mCanChangeChannel) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
            //    mCanChangeChannel = false;
                mHandler.sendEmptyMessageDelayed(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_CHDN....start");
                mThreadHandler.post(mChannelDownRunnable);
            }
            break;
         /* case KeyMap.KEYCODE_MTKIR_EJECT:
              commonIntegration.iSetCurrentChannelFavorite();
              ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
              return true;*/
        case KeyMap.KEYCODE_MTKIR_STOP:
     //       mSendKeyCode = KeyMap.KEYCODE_MTKIR_STOP;
       //     isHandled = false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_STOP break.");
            break;
        case  KeyMap.KEYCODE_PAGE_DOWN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_PAGE_DOWN!!!!!");
            return true;
       case KeyMap.KEYCODE_MTKIR_FREEZE:
        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        case KeyMap.KEYCODE_MTKIR_RECORD:
        //case KeyMap.KEYCODE_MTKIR_REWIND:
        //case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
            dismiss();
            isHandled = false;
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
                    if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                            && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                        return mTIFChannelManager.channelUpDownByMaskAndSat(mCurMask, mCurVal, mCurrentSatelliteRecordId, isUp);
                    } else {
                        return mTIFChannelManager.channelUpDownByMask(isUp, mCurMask, mCurVal);
                    }
                } else {
                    if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                            && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                        return commonIntegration.channelUpDownByMaskAndSat(mCurMask, mCurVal, mCurrentSatelliteRecordId, isUp);
                    } else {
                        return commonIntegration.channelUpDownByMask(isUp, mCurMask, mCurVal);
                    }
                }

        }
    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
       return onKeyHandler(keyCode, event,false);
    };

    private void loadSelectMoreRes(){
        if (isSelectionMode()){
            moreTypes = mContext.getResources().getStringArray(R.array.nav_channel_select_more_two);
            //mTitlePre =  mContext.getResources().getString(R.string.nav_channel_list);
            ArrayList<String> typeList = new ArrayList<String>();
            for(String type: moreTypes){
                typeList.add(type);
            }
            ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,0,typeList);
            mChannelMoreView.setVisibility(View.VISIBLE);
            mChannelListView.setVisibility(View.GONE);
            mChannelMoreView.setAdapter(listAdapter);
            mChannelMoreView.setOnKeyListener(new ChannelListOnKey());
            mChannelMoreView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"yiran loadSelectMoreRes parent " + parent+" ,view = "+view+",position is "+position);
                    CURRENT_MORE_TYPE = position;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran nav_channel_list_option>>> CURRENT_OPTION is " + CURRENT_MORE_TYPE);
                    if (CURRENT_MORE_TYPE == 0){
                        showSelectType();
                    }else if (CURRENT_MORE_TYPE == 1){
                        loadOptionRes();
                        mTitleText.setText(mContext.getResources().getString(R.string.nav_channel_list_options));
                    }
                    ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_MORE_TYPE,typeList);
                    mChannelMoreView.setAdapter(listAdapter);
                    mChannelMoreView.setSelection(CURRENT_MORE_TYPE);
                    startTimeout(NAV_TIMEOUT_300);
                }
            });
            mChannelMoreView.setFocusable(true);
            mChannelMoreView.requestFocus();
            mChannelMoreView.setSelection(0);

            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
            Message msg = Message.obtain();
            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
            msg.arg1 = R.id.nav_channel_more;
            msg.arg2 = 0;
            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
        }
    }

    private void loadChannelTypeRes(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelTypeRes CURRENT_CHANNEL_TYPE" + CURRENT_CHANNEL_TYPE);
        if(isSelectionMode()){
            mTitlePre =  mContext.getResources().getString(R.string.nav_channel_list)+" - ";
            ArrayList<String> typeList = new ArrayList<String>();
            for(String type: types){
                typeList.add(type);
            }
            ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_CHANNEL_TYPE,typeList);
            mChannelMoreView.setVisibility(View.GONE);
            mChannelTypeView.setAdapter(listAdapter);
            mChannelTypeView.setOnKeyListener(new ChannelListOnKey());
            mChannelTypeView.setVisibility(View.VISIBLE);
            mChannelTypeView.setFocusable(true);
            mChannelTypeView.requestFocus();
            mChannelTypeView.setSelection(CURRENT_CHANNEL_TYPE);

            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
            Message msg = Message.obtain();
            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
            msg.arg1 = R.id.nav_channel_typeview;
            msg.arg2 = CURRENT_CHANNEL_TYPE;
            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);

        }
    }

    private String[] addSuffix(String[] strtemp,int strId) {
        if(mContext == null || strtemp.length == 0){
            return null;
        }
        String[]  str=new String[strtemp.length];
        for (int i = 0; i < strtemp.length; i++) {
            String numStr = strtemp[i];
            int num = numStr == null || numStr.isEmpty() ?
                    Integer.MAX_VALUE :Integer.parseInt(numStr);
            if(num != Integer.MAX_VALUE){
                str[i] = mContext.getResources().getString(strId,num);
            }
        }
        return str;
    }
    private void initFavtypes(){
        String[] typestemp = mContext.getResources().getStringArray(R.array.nav_favourite_type);
        favtypes = addSuffix(typestemp, R.string.str);
    }
    private void loadChannelTypefavsRes(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelTypefavsRes CURRENT_CHANNEL_FAV" + CURRENT_CHANNEL_FAV);
            initFavtypes();
            ArrayList<String> typeList = new ArrayList<String>();
            for(String type: favtypes){
                typeList.add(type);
            }
            mChannelTypeView.setVisibility(View.GONE);
            mTitleText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_type_fav));
            ChannelListMoreAdapter listAdapter =null;
         //   if(isFavTypeState){
                listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_CHANNEL_FAV,typeList);
         //   }else{
         //       listAdapter = new ChannelListMoreAdapter(mContext,-2,typeList);
         //   }
            mChannelTypeFavView.setAdapter(listAdapter);
            mChannelTypeFavView.setOnKeyListener(new ChannelListOnKey());
            mChannelTypeFavView.setVisibility(View.VISIBLE);
            mChannelTypeFavView.setFocusable(true);
            mChannelTypeFavView.requestFocus();
            mChannelTypeFavView.setSelection(CURRENT_CHANNEL_FAV);

            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
            Message msg = Message.obtain();
            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
            msg.arg1 = R.id.nav_channel_fav_view;
            msg.arg2 = CURRENT_CHANNEL_FAV;
            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
    }

    private void loadFavSelection(TIFChannelInfo minfo){
        mSelectionChannel = minfo;
        initFavtypes();
        ArrayList<String> typeList = new ArrayList<String>();
        for(String type: favtypes){
            typeList.add(type);
        }
        mChannelListView.setVisibility(View.GONE);
        mChannelListTipView.setVisibility(View.GONE);
        mTitleText.setText(mContext.getResources().getString(R.string.nav_favourite_selection));

        selectionAdapter = new FavSelectionAdapter(mContext,typeList,minfo);

        mChannelSelectionView.setAdapter(selectionAdapter);
        mChannelSelectionView.setOnKeyListener(new ChannelListOnKey());
        mChannelSelectionView.setVisibility(View.VISIBLE);
        mChannelSelectionView.setFocusable(true);
        mChannelSelectionView.requestFocus();
        mChannelSelectionView.setSelection(0);
    }

    private void loadChannelTypeResForUS(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelTypeRes CURRENT_CHANNEL_TYPE" + CURRENT_CHANNEL_TYPE);
        if(isSelectionMode()){
            //types = mContext.getResources().getStringArray(R.array.nav_channel_type_for_us);
            mTitlePre =  mContext.getResources().getString(R.string.nav_channel_list)+" - ";
            ArrayList<String> typeList = new ArrayList<String>();
            for(String type: types){
                typeList.add(type);
            }
            ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_CHANNEL_TYPE,typeList);
            mChannelTypeView.setAdapter(listAdapter);
            mChannelTypeView.setOnKeyListener(new ChannelListOnKey());
        }
    }


    private void initSecondType(){
        mChannelTypePacksOrCategory.clear();
        if(commonIntegration.isTELEKARTAContain() && commonIntegration.getTunerMode() == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType isTELEKARTAContain = " + commonIntegration.isTELEKARTAContain());
            int categoriesnum = commonIntegration.dvbsGetCategoryNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType categoriesnum" + categoriesnum);
            typesSecond =new ArrayList<String>();
            for(int i=0;i < categoriesnum;i++){
                String categoriesinfo =commonIntegration.dvbsGetCategoryInfoByIdx(i);
                String[] categoriesname=categoriesinfo.split("_");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType true categoriesname" + categoriesname[0]+"categoriesname id ="+categoriesname[1]);
                if(CURRENT_CHANNEL_TYPE == PACKS_CHANNEL){
                    if( Integer.parseInt(categoriesname[1]) >= CATEGORIES_BASE){
                        mChannelTypePacksOrCategory.add(i);
                        typesSecond.add(categoriesname[0]);
                   //     typeList.add(categoriesname[0]);
                    }
                }else if(CURRENT_CHANNEL_TYPE == CATEGORIES_CHANNEL){
                    if(Integer.parseInt(categoriesname[1]) >= PACKS_BASE && Integer.parseInt(categoriesname[1]) < CATEGORIES_BASE){
                        mChannelTypePacksOrCategory.add(i);
                        typesSecond.add(categoriesname[0]);
                    //    typeList.add(categoriesname[0]);
                    }

                }

            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType types.length == 5 typeList.size " );

        }else if(commonIntegration.isOperatorContain() && commonIntegration.getTunerMode() == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType isOperatorNTVPLUS or isOperatorTKGS");
            int categoriesnum = commonIntegration.dvbsGetCategoryNum();
            typesSecond =new ArrayList<String>();
     //       typeList =new ArrayList<String>();
            for(int i=0;i < categoriesnum;i++){
                String categoriesinfo =commonIntegration.dvbsGetCategoryInfoByIdx(i);
                String groupname = "";
                if (categoriesinfo.contains("_")){
                    String[] categoriesname=categoriesinfo.split("_");
                    groupname = categoriesinfo.substring(0,categoriesinfo.length() - categoriesname[categoriesname.length-1].length() -1);
                }else{
                    groupname = categoriesinfo;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType false categoriesname" + groupname);
                mChannelTypePacksOrCategory.add(i);
                typesSecond.add(groupname);
             //  typeList.add(categoriesname[0]);
            }
            if(commonIntegration.isOperatorNTVPLUS()){
                List<TIFChannelInfo> tifChaList=mTIFChannelManager.queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
                String channelNum=tifChaList.get(tifChaList.size()-1).mDisplayNumber;
                String[] chaNum=channelNum.split("-");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType channelNum = " + channelNum);
                if(Integer.parseInt(chaNum[0]) == CommonIntegration.CATEGORIES_CHANNELNUM_BASE || Integer.parseInt(chaNum[0]) > CommonIntegration.CATEGORIES_CHANNELNUM_BASE){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView false categoriesOther" + categoriesOther);
                    mChannelTypePacksOrCategory.add(categoriesnum);
                    typesSecond.add(categoriesOther);
                 //   typeList.add(categoriesOther);

                }

            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"initSecondType types.length == 4 typeList.size " );
        }else{
            return ;
        }
    }
    private void  loadChannelListTypeSecondeView(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView CURRENT_CHANNEL_TYPE" + CURRENT_CHANNEL_TYPE);
        ArrayList<String> typeList = null;
        mChannelTypePacksOrCategory.clear();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView mChannelTypePacksOrCategory size " + mChannelTypePacksOrCategory.size());
        if(commonIntegration.isTELEKARTAContain() && commonIntegration.getTunerMode() == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView isTELEKARTAContain = " + commonIntegration.isTELEKARTAContain());
            int categoriesnum = commonIntegration.dvbsGetCategoryNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView categoriesnum" + categoriesnum);
            typesSecond =new ArrayList<String>();
            typeList = new ArrayList<String>();
            for(int i=0;i < categoriesnum;i++){
                String categoriesinfo =commonIntegration.dvbsGetCategoryInfoByIdx(i);
                String[] categoriesname=categoriesinfo.split("_");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView true categoriesname" + categoriesname[0]+"categoriesname id ="+categoriesname[1]);
                if(CURRENT_CHANNEL_TYPE == PACKS_CHANNEL){
                    if( Integer.parseInt(categoriesname[1]) >= CATEGORIES_BASE){
                     //   HashMap<String,String> tmpType = new HashMap<String,String>();
                     //   tmpType.put(CH_TYPE_SECOND,categoriesname[0]);
                        mChannelTypePacksOrCategory.add(i);
                        typesSecond.add(categoriesname[0]);
                        typeList.add(categoriesname[0]);
                    }
                }else if(CURRENT_CHANNEL_TYPE == CATEGORIES_CHANNEL){
                    if(Integer.parseInt(categoriesname[1]) >= PACKS_BASE && Integer.parseInt(categoriesname[1]) < CATEGORIES_BASE){
                       /* HashMap<String,String> tmpType = new HashMap<String,String>();
                        tmpType.put(CH_TYPE_SECOND,categoriesname[0]);*/
                        mChannelTypePacksOrCategory.add(i);
                        typesSecond.add(categoriesname[0]);
                        typeList.add(categoriesname[0]);
                    }

                }

            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView types.length == 5 typeList.size " + typeList.size());

        }else if(commonIntegration.isOperatorContain() && commonIntegration.getTunerMode() == 2){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView isOperatorNTVPLUS or isOperatorTKGS");
            int categoriesnum = commonIntegration.dvbsGetCategoryNum();
            typesSecond =new ArrayList<String>();
            typeList =new ArrayList<String>();
            for(int i=0;i < categoriesnum;i++){
            //    HashMap<String,String> tmpType = new HashMap<String,String>();
                String categoriesinfo =commonIntegration.dvbsGetCategoryInfoByIdx(i);
                String groupname = "";
                if (categoriesinfo.contains("_")){
                    String[] categoriesname=categoriesinfo.split("_");
                    groupname = categoriesinfo.substring(0,categoriesinfo.length() - categoriesname[categoriesname.length-1].length() -1);
                }else{
                    groupname = categoriesinfo;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView false categoriesname" + groupname);
           //     tmpType.put(CH_TYPE_SECOND,categoriesname[0]);
                mChannelTypePacksOrCategory.add(i);
                typesSecond.add(groupname);
                typeList.add(groupname);
            }
            if(commonIntegration.isOperatorNTVPLUS()){
                List<TIFChannelInfo> tifChaList=mTIFChannelManager.queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
                String channelNum=tifChaList.get(tifChaList.size()-1).mDisplayNumber;
                String[] chaNum=channelNum.split("-");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView channelNum = " + channelNum);
                if(Integer.parseInt(chaNum[0]) == CommonIntegration.CATEGORIES_CHANNELNUM_BASE || Integer.parseInt(chaNum[0]) > CommonIntegration.CATEGORIES_CHANNELNUM_BASE){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView false categoriesOther" + categoriesOther);
                    mChannelTypePacksOrCategory.add(categoriesnum);
                    typesSecond.add(categoriesOther);
                    typeList.add(categoriesOther);

                }

            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelListTypeSecondeView types.length == 4 typeList.size " +typeList.size());
        }else{
            return ;
        }
        ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_CHANNEL_SECOND_TYPE,typeList);
            mChannelTypeView.setAdapter(listAdapter);
        mChannelTypeView.setVisibility(View.GONE);
        mChannelSatelliteSecondView.setVisibility(View.VISIBLE);
        mChannelSatelliteSecondView.setAdapter(listAdapter);
        mChannelSatelliteSecondView.setOnKeyListener(new ChannelListOnKey());
        mChannelSatelliteSecondView.setFocusable(true);
        mChannelSatelliteSecondView.requestFocus();
        mChannelSatelliteSecondView.setSelection(CURRENT_CHANNEL_SECOND_TYPE);
    }
    ChannelListMoreAdapter optionlistAdapter;
    private void loadOptionRes(){
        CURRENT_OPTION = 0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadOptionRes()  ");
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadOptionRes isSelectionMode ture");
           int operationtype = 0;
           ArrayList<String> typeList = new ArrayList<String>();
           if(isFavTypeState){
               moreItems = mContext.getResources().getStringArray(R.array.nav_channel_list_option_for_fav);
             //  ArrayList<String> typeList = new ArrayList<String>();
               for(String type: moreItems){
                   typeList.add(type);
               }
               int count = 0;
               if (mChannelAdapter != null){
                   count = mChannelAdapter.getCount();
               }
               operationtype = 1;
               optionlistAdapter = new ChannelListMoreAdapter(mContext,count,typeList);
               optionlistAdapter.setType(operationtype);
           }else{
               moreItems = mContext.getResources().getStringArray(R.array.nav_channel_list_option_for_broadcast);
            //   ArrayList<String> typeList = new ArrayList<String>();
               for(String type: moreItems){
                   typeList.add(type);
               }
               optionlistAdapter = new ChannelListMoreAdapter(mContext,CURRENT_OPTION,typeList);
               optionlistAdapter.setType(operationtype);
           }

            /*optionlistAdapter = new ChannelListMoreAdapter(mContext,mChannelAdapter.getCount(),typeList);
            optionlistAdapter.setType(operationtype);*/
            mChannelListView.setVisibility(View.GONE);
            mChannelTypeView.setVisibility(View.GONE);
            mChannelMoreView.setVisibility(View.GONE);
            mChannelListTipView.setVisibility(View.GONE);
            mChannelOptionView.setAdapter(optionlistAdapter);
            mChannelOptionView.setOnKeyListener(new ChannelListOnKey());
            mChannelOptionView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                        long id) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadSelectMoreRes parent " + parent+" ,view = "+view+",position is "+position);
                    if(mContext != null && TextToSpeechUtil.isTTSEnabled(mContext)){
                        return ; // select channel not support focus when tts is true
                    }

                    mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
                    CURRENT_OPTION = position;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> CURRENT_OPTION is " + CURRENT_OPTION);
              //      mSaveValue.saveValue(SEL_MORE,CURRENT_OPTION);

                    if(isFavTypeState){
                        if(CURRENT_OPTION == 0){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> loadChannelTypeRes() move");

                            if(mChannelAdapter !=null && mChannelAdapter.getCount() > 1){
                                mChannelOptionView.setVisibility(View.GONE);
                                mChannelMoreView.setVisibility(View.GONE);
                                setChannelListTitle();
                                View favView =(View) mChannelListView.getSelectedView();
                                mChannelListView.setVisibility(View.VISIBLE);
                                mChannelListTipView.setVisibility(View.VISIBLE);
                                mChannelAdapter.updateView(favView, mChannelListView.getSelectedItemPosition(),View.VISIBLE);
                                favmovetag = mChannelAdapter.getItem(mChannelListView.getSelectedItemPosition());
                                favmove = favmovetag;
                                //indexTo = mChannelListView.getSelectedItemPosition();
                                indexTo = favChannelManager.getFavoriteIdx( commonIntegration.favMask[CURRENT_CHANNEL_FAV],favmove.mInternalProviderFlag3,CURRENT_CHANNEL_FAV);
                                favCount = getFavListCount();
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran init indexTo:"+indexTo+" favCount:"+favCount);
                                isFavListMove =true;
                            }
                        }else if(CURRENT_OPTION == 1){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> loadChannelSortRes() delete");
                            if(mChannelAdapter !=null && mChannelAdapter.getCount() > 0){
                                mChannelListView.setVisibility(View.VISIBLE);
                                mChannelOptionView.setVisibility(View.GONE);
                                mChannelMoreView.setVisibility(View.GONE);
                                TIFChannelInfo mTifCh=  mChannelAdapter.getItem(mChannelListView.getSelectedItemPosition());
                                favChannelManager.deleteFavorite(mTifCh.mMtkTvChannelInfo);
                                resetChListByTIF();
                            }

                        }
                        startTimeout(NAV_TIMEOUT_300);
                    }else{
                        if(CURRENT_OPTION == 0){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> loadChannelTypeRes() sort");
                            mChannelOptionView.setVisibility(View.GONE);
                            loadChannelSortRes();
                            mTitleText.setText(sortTitle);
                            startTimeout(NAV_TIMEOUT_300);
                        }else if(CURRENT_OPTION == 1){
                            ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_OPTION,typeList);
                            mChannelOptionView.setAdapter(listAdapter);
                            mChannelOptionView.setSelection(CURRENT_OPTION);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> gotoSeacherTextAct() find");
                            gotoSeacherTextAct();
                            stopTimeout();
                        }
                    }

                }
            });
            mChannelOptionView.setVisibility(View.VISIBLE);
            mChannelOptionView.setFocusable(true);
            mChannelOptionView.requestFocus();
         //   mChannelOptionView.setSelection(0);
            if(mChannelAdapter!=null && mChannelAdapter.getCount() == 1){
                mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                Message msg = Message.obtain();
                msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                msg.arg1 = R.id.nav_channel_list_option;
                msg.arg2 = 1;
                mHandler.sendMessageDelayed(msg, 0);
            }else if(mChannelAdapter!=null && mChannelAdapter.getCount() != 0){
                mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                Message msg = Message.obtain();
                msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                msg.arg1 = R.id.nav_channel_list_option;
                msg.arg2 = 0;
                mHandler.sendMessageDelayed(msg, 0);
            }

        }
    String[] sorts;
    String sortTitle;
    private void loadChannelSortRes(){

            sortTitle =  mContext.getResources().getString(R.string.nav_select_sort);
            if((commonIntegration.isTVSourceSeparation() && commonIntegration.isCurrentSourceDTVforEuPA()) ||
                    (commonIntegration.isCNRegion() && commonIntegration.isCurrentSourceDTV())){
                sorts = mContext.getResources().getStringArray(R.array.nav_channel_sort_pa);
            }else{
                sorts = mContext.getResources().getStringArray(R.array.nav_channel_sort);
            }
            ArrayList<String> typeList = new ArrayList<String>();
            for(String type: sorts){
                typeList.add(type);
            }
            ChannelListMoreAdapter listAdapter = new ChannelListMoreAdapter(mContext,CURRENT_CHANNEL_SORT,typeList);
            mChannelSortView.setAdapter(listAdapter);
            mChannelSortView.setOnKeyListener(new ChannelListOnKey());
            mChannelSortView.setVisibility(View.VISIBLE);
            mChannelSortView.setFocusable(true);
            mChannelSortView.requestFocus();

            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
            Message msg = Message.obtain();
            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
            msg.arg1 = R.id.nav_channel_sort;
            msg.arg2 = CURRENT_CHANNEL_SORT;
            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
    }

    public void gotoSeacherTextAct() {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "gotoSeacherTextAct()");
          isFindState=true;
          Intent intent = new Intent(mContext, EditTextActivity.class);
          intent.putExtra(EditTextActivity.TYPE_CLASS_TEXT,true);
          intent.putExtra(EditTextActivity.EXTRA_DESC, mContext.getResources().getString(R.string.nav_select_find_text));
          intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, "");
          intent.putExtra(EditTextActivity.EXTRA_ITEMID, CURRENT_CHANNEL_FIND);
          intent.putExtra(EditTextActivity.EXTRA_LENGTH, 32);
          Message msg = Message.obtain();
          msg.what = FIND_CHANNELLIST;
          msg.arg1 = FIND_CHANNELLIST;
          msg.obj = intent;
          TurnkeyUiMainActivity.getInstance().getHandler().sendMessage(msg);
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "gotoSeacherTextAct() end");
      }


    //init view param
    private void findViews() {
        mChannelListLayout = findViewById(R.id.nav_channellist);
        mChannelListView = (ListView) findViewById(R.id.nav_channel_listview);
        mChannelTypeView = (ListView) findViewById(R.id.nav_channel_typeview);
        mChannelMoreView = (ListView) findViewById(R.id.nav_channel_more);
        mChannelSelectionView = (ListView) findViewById(R.id.nav_channel_selection);
        mChannelOptionView=(ListView) findViewById(R.id.nav_channel_list_option);
        mChannelSortView=(ListView) findViewById(R.id.nav_channel_sort);
        mChannelSatelliteView = (ListView)findViewById(R.id.nav_channel_satellitetypeview);
        mChannelTypeFavView =(ListView)findViewById(R.id.nav_channel_fav_view);
        mChannelSatelliteSecondView =(ListView)findViewById(R.id.nav_channel_satellitetypesecondview);
        mChannelListTipView = findViewById(R.id.nav_channel_list_tip);
        mChannelListFunctionLayout = findViewById(R.id.nav_page_function);
     //   mExitAndSatTv = (TextView)findViewById(R.id.channel_nav_exit);
        mChannelListPageUpDownLayout = findViewById(R.id.nav_page_up_down);
        mChannelDetailLayout = findViewById(R.id.nav_channel_details_layout);
        mChannelDetailLayout.setVisibility(View.INVISIBLE);
        mChannelDetailTileText = (TextView)findViewById(R.id.nav_channel_details_title);
        mNavChannelDetailsChannelInfoTextView = (TextView) findViewById(R.id.nav_channel_details_channel_info);
        mTitleText = (TextView)findViewById(R.id.nav_channel_list_title);
        mYellowKeyText = (TextView)findViewById(R.id.channel_nav_select_list);
        mBlueKeyText = (TextView)findViewById(R.id.channel_nav_exit);
        mBlueicon = (ImageView)findViewById(R.id.channel_nav_exit_icon);
        chanelListProgressbar =(ProgressBar)findViewById(R.id.nav_channel_list_progressbar);
        mTitleText.setSelected(true);
    }

    private List<TIFChannelInfo> updateCurrentChannelLlistByTIF() {
        List<TIFChannelInfo> mTifChannelList = new ArrayList<TIFChannelInfo>();
        TIFChannelInfo chInfo = null;
        int preNum = 0;
        int startChannelId = -1;
        if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0) {
            if (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL) {
                preNum = mTIFChannelManager.getSatelliteChannelConfirmCount(mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX + 1, mCurMask, mCurVal);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "preNum>mCurrentSatelliteRecordId>>>" + preNum);
                hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tif isGeneralSatMode hasNextPage ="+hasNextPage + "    startChannelId>>" + startChannelId);
                if(hasNextPage){ // channel id maybe small than zero
                    mTifChannelList = new ArrayList<TIFChannelInfo>();
                    int index = 0;
                    if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
                        for (int i = 0; i < mChannelAdapter.getCount(); i++) {
                            chInfo = mChannelAdapter.getItem(i);
                            if (chInfo != null) {
                                chInfo = mTIFChannelManager.getTIFChannelInfoById(chInfo.mMtkTvChannelInfo.getChannelId());
                                if (chInfo != null) {
                                    startChannelId = chInfo.mMtkTvChannelInfo.getChannelId();
                                    index = i;
                                    break;
                                }
                            }
                        }
                        List<TIFChannelInfo> mPreChannelList = null;
                        List<TIFChannelInfo> mNextChannelList = null;
                        if (chInfo == null || startChannelId == -1) {
                            startChannelId = commonIntegration.getCurrentChannelId();
                            chInfo = mTIFChannelManager.getTIFChannelInfoById(startChannelId);
                        }
                        if (index > 0) {
                            mPreChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(startChannelId, true, false, mCurrentSatelliteRecordId, index, mCurMask, mCurVal);
                        }
                        mNextChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(startChannelId, false, false, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal);
                        if (mPreChannelList != null) {
                            mTifChannelList.addAll(mPreChannelList);
                        }
                        TIFChannelInfo tifCurrentChannel = mTIFChannelManager.getTIFChannelInfoById(startChannelId);
                        if (tifCurrentChannel != null) {
                            MtkTvChannelInfoBase mCurrentChannel = tifCurrentChannel.mMtkTvChannelInfo;
                            if(mCurrentChannel instanceof MtkTvDvbChannelInfo){
                                MtkTvDvbChannelInfo curInfo = (MtkTvDvbChannelInfo)mCurrentChannel;
                                if (curInfo != null && commonIntegration.checkChMask(tifCurrentChannel.mMtkTvChannelInfo, mCurMask, mCurVal)
                                        && curInfo.getSatRecId() == mCurrentSatelliteRecordId) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tif curInfo.getSatRecId()>>" + curInfo.getSatRecId() + "  " + mCurrentSatelliteRecordId);
                                    mTifChannelList.add(tifCurrentChannel);
                                }
                            }
                        }
                        if (mNextChannelList != null) {
                            mTifChannelList.addAll(mNextChannelList);
                        }
                        while (mTifChannelList.size() > CHANNEL_LIST_PAGE_MAX) {
                            mTifChannelList.remove(mTifChannelList.size() - 1);
                        }
                    } else {
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal) || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                            mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(commonIntegration.getCurrentChannelId(), false, true, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                        } else {
                            mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(commonIntegration.getCurrentChannelId(), false, false, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                        }
                    }
                }else if(preNum >0){
                    if (commonIntegration.checkCurChMask(mCurMask, mCurVal) || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                        mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(-1, false, true, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                    } else {
                        mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(-1, false, false, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                    }
                }
            } else {
                hasNextPage = false;
            }
        } else {
            preNum = mTIFChannelManager.getChannelListConfirmLength(CHANNEL_LIST_PAGE_MAX+1, mCurMask, mCurVal);
            hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"TIF SATE hasNextPage ="+hasNextPage);
            if(hasNextPage){
                int index = 0;
                if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
                    for (int i = 0; i < mChannelAdapter.getCount(); i++) {
                        chInfo = mChannelAdapter.getItem(i);
                        if (chInfo != null) {
                            if (commonIntegration.is3rdTVSource()){
                                chInfo = mTIFChannelManager.getTIFChannelInfoById((int)chInfo.mId);
                                if (chInfo != null) {
                                    startChannelId = (int) chInfo.mId;
                                    index = i;
                                    break;
                                }
                            }else{
                                chInfo = mTIFChannelManager.getTIFChannelInfoById(chInfo.mInternalProviderFlag3);
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
                        mPreChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(startChannelId, true, false, index, mCurMask, mCurVal);
                    }
                    if (chInfo == null || startChannelId == -1) {
                        startChannelId = commonIntegration.getCurrentChannelId();
                        chInfo = mTIFChannelManager.getTIFChannelInfoById(startChannelId);
                    }
                    if (CommonIntegration.isEURegion() && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                        if (commonIntegration.checkChMask(chInfo.mMtkTvChannelInfo, mCurMask, mCurVal)) {
                            mNextChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(startChannelId, false, true, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal);
                        } else {
                            mNextChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(startChannelId, false, false, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal);
                        }
                    } else {
                        if (commonIntegration.checkChMask(chInfo.mMtkTvChannelInfo, mCurMask, mCurVal) || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                            mNextChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(startChannelId, false, true, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal);
                        } else {
                            mNextChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(startChannelId, false, false, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal);
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
                } else {
                    if (commonIntegration.checkCurChMask(mCurMask, mCurVal) || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                        mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(commonIntegration.getCurrentChannelId(), false, true, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                    } else {
                        mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(commonIntegration.getCurrentChannelId(), false, false, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                    }
                }
            }else if(preNum >0){
                if (commonIntegration.checkCurChMask(mCurMask, mCurVal) || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                    mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(-1, false, true, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                } else {
                    mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(-1, false, false, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                }
            }
        }
        return mTifChannelList;
    }

    private synchronized void resetTypeChListByTIF(){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setChannelListTitle();
            }
        });
        mThreadHandler.post(mResetTypeChannelListRunnable);
    }
    private synchronized void resetChListByTIF(){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setChannelListTitle();
            }
        });
        mThreadHandler.post(mResetChannelListRunnable);
    }

    private List<TIFChannelInfo> processTIFChListWithThread(TIFChannelInfo mTIFChannelInfo) {
        int chId = -1;
        int mId = -1;
        if (mTIFChannelInfo != null){
            chId = TIFFunctionUtil.is3rdTVSource(mTIFChannelInfo)? (int)mTIFChannelInfo.mId: mTIFChannelInfo.mInternalProviderFlag3;
            mId = (int)mTIFChannelInfo.mId;
        }else{
            chId = 0;
            mId = 0;
        }
        int preNum = 0;
        List<TIFChannelInfo> mTifChannelList;
        if(isFavTypeState){
            MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
            List<MtkTvChannelInfoBase> tempApiChList = null;
            if (mLastSelection < 0) {
                mLastSelection = 0;
            }
            if (currentChannel != null) {
              if ((currentChannel.getNwMask() & mCurMask) > 0) {
                preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
                hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
                if (hasNextPage) {
                  int index = currentChannelInListIndex(mId);
                  if (index >= 0) {
                    tempApiChList = commonIntegration.getFavoriteListByFilter(
                            mCurMask, chId,true,index, CHANNEL_LIST_PAGE_MAX - index,CURRENT_CHANNEL_FAV);
                  } else {
                    tempApiChList = commonIntegration.getFavoriteListByFilter(
                            mCurMask, chId,true, mLastSelection, CHANNEL_LIST_PAGE_MAX
                            - mLastSelection,CURRENT_CHANNEL_FAV);
                  }
                } else if (preNum > 0) {
                  tempApiChList = commonIntegration.getFavoriteListByFilter(
                          mCurMask, 0,false, 0, preNum,CURRENT_CHANNEL_FAV);
                }
              } else if ((currentChannel.getNwMask() & mCurMask) == 0) {
                preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
                hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
                if (preNum > 0) {
                  tempApiChList = commonIntegration.getFavoriteListByFilter(mCurMask,
                      0,false, 0, preNum > CHANNEL_LIST_PAGE_MAX ? CHANNEL_LIST_PAGE_MAX : preNum,CURRENT_CHANNEL_FAV);
                }
              }
            }
           // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempApiChList.size" + tempApiChList.size());
            if (ScanContent.isCountryIta() && MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER) == 2){
                return mTIFChannelManager.getTIFChannelList(tempApiChList);
            }else{
                return mTIFChannelManager.getTIFChannelListForFavIndex(tempApiChList, CURRENT_CHANNEL_FAV);
            }
        }else{
            if (mCurCategories != -1) {
                preNum = mTIFChannelManager
                        .getChannelListConfirmLengthForSecondType(
                                CHANNEL_LIST_PAGE_MAX + 1, mCurMask, mCurVal);
            } else {
                preNum = mTIFChannelManager.getChannelListConfirmLength(
                        CHANNEL_LIST_PAGE_MAX + 1, mCurMask, mCurVal);
            }

            hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIF SATE hasNextPage =" + hasNextPage);
            if (hasNextPage) {
                int index = currentChannelInListIndex(mId); // DTV00595148
                mTifChannelList = new ArrayList<TIFChannelInfo>();
                List<TIFChannelInfo> mPreChannelList = null;
                List<TIFChannelInfo> mNextChannelList = null;
                if (index != -1
                        && commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                    if (index > 0) {
                        mPreChannelList = mTIFChannelManager
                                .getTIFPreOrNextChannelList(chId, true, false,
                                        index, mCurMask, mCurVal);
                    }
                    if (CommonIntegration.isEURegion()
                            && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                            mNextChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, false, true,
                                            CHANNEL_LIST_PAGE_MAX - index,
                                            mCurMask, mCurVal);
                        } else {
                            mNextChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, false, false,
                                            CHANNEL_LIST_PAGE_MAX - index,
                                            mCurMask, mCurVal);
                        }
                    } else {
                        mNextChannelList = mTIFChannelManager
                                .getTIFPreOrNextChannelList(chId, false, true,
                                        CHANNEL_LIST_PAGE_MAX - index, mCurMask,
                                        mCurVal);
                    }
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIF SATE mLastSelection =" + mLastSelection);
                    if (mLastSelection < 0) {
                        mLastSelection = 0;
                    }
                    if (CommonIntegration.isEURegion()
                            && CURRENT_CHANNEL_TYPE != ALL_CHANNEL) {
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal)) {
                            if (mLastSelection > 0) {
                                mPreChannelList = mTIFChannelManager
                                        .getTIFPreOrNextChannelList(chId, true,
                                                false, mLastSelection, mCurMask,
                                                mCurVal);
                            }
                            mNextChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, false, true,
                                            CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                            mCurMask, mCurVal);
                        } else {
                            mLastSelection = 0;// set selection to first
                            mNextChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, false, false,
                                            CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                            mCurMask, mCurVal);
                        }
                    } else {
                        if (mLastSelection > 0) {
                            mPreChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, true, false,
                                            mLastSelection, mCurMask, mCurVal);
                        }
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal)
                                || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                            mNextChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, false, true,
                                            CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                            mCurMask, mCurVal);
                        } else {
                            mNextChannelList = mTIFChannelManager
                                    .getTIFPreOrNextChannelList(chId, false, false,
                                            CHANNEL_LIST_PAGE_MAX - mLastSelection,
                                            mCurMask, mCurVal);
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
                if (commonIntegration.checkCurChMask(mCurMask, mCurVal)
                        || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                    mTifChannelList = mTIFChannelManager
                            .getTIFPreOrNextChannelList(-1, false, true,
                                    CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                } else {
                    mLastSelection = 0;// set selection to first
                    mTifChannelList = mTIFChannelManager
                            .getTIFPreOrNextChannelList(-1, false, false,
                                    CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                }
            } else {
                mTifChannelList = null;
            }

        }

        return mTifChannelList;
    }

    private synchronized void resetChList(){
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
                        mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_type_fav));
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

    private List<TIFChannelInfo> processListWithThread(int chId) {
        List<TIFChannelInfo> tempChlist = null;
        List<MtkTvChannelInfoBase> tempApiChList = null;
        int preNum = 0;
        if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0) {
            if (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL) {
                preNum = commonIntegration.getSatelliteChannelCount(mCurrentSatelliteRecordId, mCurMask, mCurVal);
                hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isGeneralSatMode hasNextPage ="+hasNextPage + "    chId>>" + chId);
                if(hasNextPage){
                    tempApiChList = new ArrayList<MtkTvChannelInfoBase>();
                    List<MtkTvChannelInfoBase> preList = null;
                    List<MtkTvChannelInfoBase> nextList = null;
                    int index = currentChannelInListIndex(chId);   //DTV00595148
                    if (index < 0) {
                        index = 0;
                    }
                    if (index > 0) {
                        preList = commonIntegration.getChannelListByMaskAndSat(chId, MtkTvChannelList.CHLST_ITERATE_DIR_PREV, index, mCurMask, mCurVal, mCurrentSatelliteRecordId);
                    }
                    nextList = commonIntegration.getChannelListByMaskAndSat(chId, MtkTvChannelList.CHLST_ITERATE_DIR_NEXT, CHANNEL_LIST_PAGE_MAX - index, mCurMask, mCurVal, mCurrentSatelliteRecordId);
                    if (preList != null) {
                        tempApiChList.addAll(preList);
                    }
                    MtkTvChannelInfoBase mCurrentChannel = commonIntegration.getCurChInfo();
                    if(mCurrentChannel instanceof MtkTvDvbChannelInfo){
                        MtkTvDvbChannelInfo curInfo = (MtkTvDvbChannelInfo)mCurrentChannel;
                        if (curInfo != null && commonIntegration.checkCurChMask(mCurMask, mCurVal)
                                && curInfo.getSatRecId() == mCurrentSatelliteRecordId) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curInfo.getSatRecId()>>" + curInfo.getSatRecId() + "  " + mCurrentSatelliteRecordId);
                            tempApiChList.add(curInfo);
                        }
                    }
                    if (nextList != null) {
                        tempApiChList.addAll(nextList);
                    }
                    while (tempApiChList.size() > CHANNEL_LIST_PAGE_MAX) {
                        tempApiChList.remove(tempApiChList.size() - 1);
                    }
                    tempChlist = TIFFunctionUtil.getTIFChannelList(tempApiChList);
                }else if(preNum >0){
                    tempApiChList = commonIntegration.getChannelListByMaskAndSat(chId, MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal, mCurrentSatelliteRecordId);
                    tempChlist = TIFFunctionUtil.getTIFChannelList(tempApiChList);
                }else{
                    tempChlist = null;
                }
            } else {
                hasNextPage = false;
                tempChlist = null;
            }
        } else {
            preNum = commonIntegration.hasNextPageChannel(CHANNEL_LIST_PAGE_MAX+1, mCurMask,mCurVal);
            hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasNextPage ="+hasNextPage + "    chId>>" + chId);
            if(hasNextPage){
            	if(SELECT_TYPE_CHANGE_CH && !commonIntegration.checkCurChMask(mCurMask, mCurVal)){
                    Log.d(TAG,"TIF SATE hasNextPage SELECT_TYPE_CHANGE_CH ");
                    tempChlist = mTIFChannelManager.getTIFPreOrNextChannelList(-1, false, true, CHANNEL_LIST_PAGE_MAX , mCurMask, mCurVal);
                    Log.d(TAG,"TIF SATE hasNextPage SELECT_TYPE_CHANGE_CH mTifChannelList "+tempChlist.size());
                    while (tempChlist.size() > CHANNEL_LIST_PAGE_MAX) {
                    	tempChlist.remove(tempChlist.size() - 1);
                    }
                    return tempChlist;
                }
                SELECT_TYPE_CHANGE_CH = false;
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
                        if (commonIntegration.checkCurChMask(mCurMask, mCurVal) || CommonIntegration.isUSRegion() || CommonIntegration.isSARegion()) {
                            tempApiChList = commonIntegration.getChList(chId, mLastSelection, CHANNEL_LIST_PAGE_MAX - mLastSelection);
                        } else {
                            tempApiChList = commonIntegration.getChannelListByMaskValuefilter(chId, mLastSelection, CHANNEL_LIST_PAGE_MAX - mLastSelection, mCurMask, mCurVal, false);
                        }
                    }
                }
                if (tempApiChList != null){
                    while (tempApiChList.size() > CHANNEL_LIST_PAGE_MAX) {
                        tempApiChList.remove(tempApiChList.size() - 1);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tempApiChList length "+ tempApiChList.size());
                }
                tempChlist = mTIFChannelManager.getTIFChannelList(tempApiChList);
            }else if(preNum >0){
                tempApiChList = getChannelList(0,MtkTvChannelList.CHLST_ITERATE_DIR_FROM_FIRST, CHANNEL_LIST_PAGE_MAX,mCurMask,mCurVal);
                tempChlist = TIFFunctionUtil.getTIFChannelList(tempApiChList);
            }else{
                tempChlist = null;
            }
        }
        return tempChlist;
    }

    private void saveLastPosition(int currentChannelId, List<TIFChannelInfo> tempChlist) {
        if (tempChlist != null) {
            mChannelIdList.clear();
            int size = tempChlist.size();
            for (int i = 0; i < size; i++) {
                int channelId;
                channelId =(int) tempChlist.get(i).mId;
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
                            ChannelListDialog.this.startTimeout(NAV_TIMEOUT_300);
                            TIFChannelInfo channelInfo = mChannelAdapter.getItem(ttsSelectchannelIndex);

                            /*mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_listview;
                            msg.arg2 = ttsSelectchannelIndex;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);*/
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"click item ttsSelectchannelIndex:"+ttsSelectchannelIndex);
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
                case R.id.nav_channel_list_option:
                    //confirm which item is focus
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "R.id.nav_channel_list_option" );
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforMore(texts.get(0).toString());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option:index =" + index +"  texts:"+texts.get(0).toString());
                        if(index >= 0) {
                            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_list_option;
                            msg.arg2 = index;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ": v.getId() = " +host.getId());
                         selectedItem(host);
                    }
                    break;
                case R.id.nav_channel_typeview:
                    //confirm which item is focus
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "R.id.nav_channel_typeview" );
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforType(texts.get(0).toString());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_typeview:index =" + index);
                        if(index >= 0) {
                            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_typeview;
                            msg.arg2 = index;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ": v.getId() = " +host.getId());
                        selectedItem(host);
                    }
                    break;
                case R.id.nav_channel_sort:
                    //confirm which item is focus
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "R.id.nav_channel_sort" );
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforSort(texts.get(0).toString());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_sort:index =" + index);
                        if(index >= 0) {
                            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_sort;
                            msg.arg2 = index;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ": v.getId() = " +host.getId());
                         selectedItem(host);
                    }
                    break;
                case R.id.nav_channel_more:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " yiran R.id.nav_channel_more" );
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_more:  texts =" + texts);
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforMoreType(texts.get(0).toString());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_sort:index =" + index);
                        if(index >= 0) {
                            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_more;
                            msg.arg2 = index;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ": v.getId() = " +host.getId());
                         selectedItem(host);
                    }
                    break;
                case R.id.nav_channel_fav_view:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " yiran R.id.nav_channel_fav_view" );
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_fav_view:  texts =" + texts);
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforFavType(texts.get(0).toString());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_fav_view:index =" + index);
                        if(index >= 0) {
                            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_fav_view;
                            msg.arg2 = index;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ": v.getId() = " +host.getId());
                        selectedItem(host);
                    }
                    break;
                case R.id.nav_channel_selection:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " yiran R.id.nav_channel_selection" );
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_selection:  texts =" + texts);
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforMoreType(texts.get(0).toString());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_selection:index =" + index);
                        if(index >= 0) {
                            mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                            Message msg = Message.obtain();
                            msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
                            msg.arg1 = R.id.nav_channel_selection;
                            msg.arg2 = index;
                            mHandler.sendMessageDelayed(msg, CommonIntegration.TTS_TIME);
                        }
                    }else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ": v.getId() = " +host.getId());
                        selectedItem(host);
                    }
                    break;
                 default :
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
                      && !"1".equals(selectedChannel.mType)) {
                       ChannelListDialog.this.selectTifChannel(KeyEvent.KEYCODE_DPAD_CENTER, selectedChannel);
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
                     ChannelListDialog.this.selectTifChannel(KeyEvent.KEYCODE_DPAD_CENTER, selectedChannel);
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
            if (isFavTypeState){
                for (int i = 0; i < mChannelIdList.size(); i++){
                    TIFChannelInfo tifchannelinfo=mTIFChannelManager.getTIFChannelInfoById(mChannelIdList.get(i));
                    if (tifchannelinfo == null){
                        return -1;
                    }
                    String indexStr = "";
                    switch (CURRENT_CHANNEL_FAV) {
                        case CommonIntegration.FAVOURITE_1:
                            indexStr=""+tifchannelinfo.mMtkTvChannelInfo.getFavorites1Index();
                            break;
                        case CommonIntegration.FAVOURITE_2:
                            indexStr=""+tifchannelinfo.mMtkTvChannelInfo.getFavorites2Index();
                            break;
                        case CommonIntegration.FAVOURITE_3:
                            indexStr=""+tifchannelinfo.mMtkTvChannelInfo.getFavorites3Index();
                            break;
                        case CommonIntegration.FAVOURITE_4:
                            indexStr=""+tifchannelinfo.mMtkTvChannelInfo.getFavorites4Index();
                            break;
                        default:
                            indexStr=""+tifchannelinfo.mMtkTvChannelInfo.getFavorites1Index();
                            break;
                    }
                    if (texts.size() > 1){
                        if(indexStr.equals(texts.get(0).toString()) && (tifchannelinfo.mDisplayName).equals(texts.get(1).toString())) {
                            return i;
                        }
                    }else{
                        if(indexStr.equals(texts.get(0).toString())) {
                            return i;
                        }
                    }
                }
            }else{
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
            }
            return -1;
        }
        private int findSelectItemforMore(String text) {
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItemforMore texts =" +text) ;
           if(moreItems == null) {
               return -1;
           }
           for(int i = 0; i < moreItems.length; i++) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + moreItems[i] + " text = " +text);
               if(moreItems[i].equals(text)) {
                   return i;
               }
           }

           return -1;
       }
        private int findSelectItemforType(String text) {
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItemforType texts =" +text) ;
           if(types == null) {
               return -1;
           }
           for(int i = 0; i < types.length; i++) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + types[i] + " text = " +text);
               if(types[i].equals(text)) {
                   return i;
               }
           }

           return -1;
       }

        private int findSelectItemforFavType(String text) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItemforMoreType texts =" +text) ;
            if(favtypes == null) {
                return -1;
            }
            for(int i = 0; i < favtypes.length; i++) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + favtypes[i] + " text = " +text);
                if(favtypes[i].equals(text)) {
                    return i;
                }
            }
            return -1;
        }

        private int findSelectItemforMoreType(String text) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItemforMoreType texts =" +text) ;
            if(favtypes == null) {
                return -1;
            }
            for(int i = 0; i < favtypes.length; i++) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + favtypes[i] + " text = " +text);
                if(favtypes[i].equals(text)) {
                    return i;
                }
            }
            return -1;
        }

        private int findSelectItemforSort(String text) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findSelectItemforSort texts =" +text) ;
            if(sorts == null) {
                return -1;
            }
            for(int i = 0; i < sorts.length; i++) {
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + sorts[i] + " text = " +text);
                if(sorts[i].equals(text)) {
                    return i;
                }
            }

            return -1;
        }
    };
    private boolean selectedItem(ViewGroup v) {
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":selectedchanneltype v.getId() = " +v.getId());
       switch(v.getId()){
          case R.id.nav_channel_typeview:
              mHandler.removeMessages(CHANGE_TYPE_CHANGECHANNEL);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position = "+CURRENT_CHANNEL_TYPE);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER commonIntegration.isOperatorNTVPLUS() = "+commonIntegration.isOperatorNTVPLUS());
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER commonIntegration.isTELEKARTAContain() = "+commonIntegration.isTELEKARTAContain());
              if((commonIntegration.isOperatorNTVPLUS() ||commonIntegration.isOperatorTKGS()) && mChannelTypeView.getSelectedItemPosition() >0  && commonIntegration.getTunerMode() == 2){
                  if(mChannelTypeView.getSelectedItemPosition() == CATEGORIES_CHANNEL){
                      LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
                      CURRENT_CHANNEL_TYPE = mChannelTypeView.getSelectedItemPosition();
                      mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                      loadChannelListTypeSecondeView();
                      return true;
                  }else if(mChannelTypeView.getSelectedItemPosition() == CHANGE_TYPE_FAVORITE_INDEX){
                      loadChannelTypefavsRes();
                      return true;
                  }else{

                      if(mSaveValue.readValue(CH_TYPE,0) == mChannelTypeView.getSelectedItemPosition()){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER isOperatorNTVPLUS position == CURRENT_CHANNEL_TYPE noy deal");
                          return true;
                      }
                  }
              }else if(commonIntegration.isTELEKARTAContain() &&  mChannelTypeView.getSelectedItemPosition() > 0  && commonIntegration.getTunerMode() == 2){
                  if(mChannelTypeView.getSelectedItemPosition() == PACKS_CHANNEL || mChannelTypeView.getSelectedItemPosition() == CATEGORIES_CHANNEL){
                      LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
                      CURRENT_CHANNEL_TYPE = mChannelTypeView.getSelectedItemPosition();
                      mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                      loadChannelListTypeSecondeView();
                      return true;
                  }else if(mChannelTypeView.getSelectedItemPosition() == CHANGE_TYPE_FAVORITE_INDEX){
                      loadChannelTypefavsRes();
                      return true;
                  }else{
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER isTELEKARTAContain mSaveValue.readValue(CH_TYPE,-1) "+mSaveValue.readValue(CH_TYPE,-1));
                      if(mSaveValue.readValue(CH_TYPE,0) == mChannelTypeView.getSelectedItemPosition()){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER isTELEKARTAContain == CURRENT_CHANNEL_TYPE noy deal");
                          return true;
                      }
                  }

              }else{

                  if(mChannelTypeView.getSelectedItemPosition() == CHANGE_TYPE_FAVORITE_INDEX){
                      loadChannelTypefavsRes();
                      return true;
                  }else{
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER mSaveValue.readValue(CH_TYPE,-1) "+mSaveValue.readValue(CH_TYPE,-1));
                      if(mSaveValue.readValue(CH_TYPE,-1) == mChannelTypeView.getSelectedItemPosition()){
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position == CURRENT_CHANNEL_TYPE noy deal");
                          return true;
                      }
                  }

              }
              saveTypeLastChannelId(CURRENT_CHANNEL_TYPE,LAST_CHANNEL_SECOND_TYPE);
              LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
              CURRENT_CHANNEL_TYPE = mChannelTypeView.getSelectedItemPosition();
              resetMask();
              typesSecond = null;
              if (mTIFChannelManager.getChannelListConfirmLength(1, mCurMask, mCurVal) > 0){
                  chooseType(TYPE_CHANGE_CHANNEL);
              }else{
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANGE_TYPE_CHANGECHANNEL current type no chanenls,return last type");
                  Toast.makeText(mContext,  mContext.getResources().getString(R.string.nav_select_type_no_channel) , Toast.LENGTH_SHORT).show();
                  initMaskAndSatellites();
              }
            return true;

        case R.id.nav_channel_satellitetypeview:
                    mCurrentSatelliteRecordId = mSatelliteListinfo.get(mChannelSatelliteView.getSelectedItemPosition()).getSatlRecId();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentSatelliteRecordId>>>" + mCurrentSatelliteRecordId);
                    mSaveValue.saveValue(SATELLITE_RECORDID,mCurrentSatelliteRecordId);
                    mChannelListView.setAdapter(null);
                    mChannelAdapter = null;
                    mChannelListView.setVisibility(View.VISIBLE);
                    mChannelSatelliteView.setVisibility(View.GONE);
                    if (mIsTifFunction) {
                        resetChListByTIF();
                    } else {
                        resetChList();
                    }
                    return true;

         case R.id.nav_channel_list_option:
             mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
             CURRENT_OPTION = mChannelOptionView.getSelectedItemPosition();
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> CURRENT_OPTION is " + CURRENT_OPTION);
            if(CURRENT_OPTION == 0){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> loadChannelSortRes() ");
                loadChannelSortRes();
                mTitleText.setText(sortTitle);
            }else if(CURRENT_OPTION == 1){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_list_option>>> gotoSeacherTextAct() ");
                mChannelListView.setAdapter(null);
                mChannelAdapter = null;
                mChannelListView.setVisibility(View.VISIBLE);
                gotoSeacherTextAct();
            }
            startTimeout(NAV_TIMEOUT_300);
             return true;

         case R.id.nav_channel_sort:
             mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
             CURRENT_CHANNEL_SORT = mChannelSortView.getSelectedItemPosition();
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_SORT>>>" + CURRENT_CHANNEL_SORT);
             mSaveValue.saveValue(CH_SORT,CURRENT_CHANNEL_SORT);
             mChannelListView.setAdapter(null);
             mChannelAdapter = null;
             mChannelListView.setVisibility(View.VISIBLE);
             mChannelSortView.setVisibility(View.GONE);
             mTIFChannelManager.setCurrentChannelSort(CURRENT_CHANNEL_SORT);
             if (mIsTifFunction) {
                 resetChListByTIF();
             } else {
                 resetChList();
             }
             return true;
         case R.id.nav_channel_more:
             int chooseIndex = mChannelMoreView.getSelectedItemPosition();
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"nav_channel_more --- choose_index:"+chooseIndex );
             if (chooseIndex == 0){
                 showSelectType();
             }else if (chooseIndex == 1){
                 loadOptionRes();
                 mTitleText.setText(mContext.getResources().getString(R.string.nav_channel_list_options));
             }
             break;
         case R.id.nav_channel_fav_view:
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"nav_channel_fav_view --- choose_index:"+  mChannelTypeFavView.getSelectedItemPosition() );
             if(CURRENT_CHANNEL_FAV == mChannelTypeFavView.getSelectedItemPosition() && CURRENT_CHANNEL_FAV != 0){
                 chooseFavChannel();
             }else{
                 chooseType(TYPE_CHANGE_FAVORITE);
             }
             break;
         case R.id.nav_channel_selection:
             List<TIFChannelInfo> channellist=mChannelAdapter.getChannellist();
             if (mSelectionChannel == null){
                 return true;
             }
             addEraseChannelId = mSelectionChannel.mInternalProviderFlag3;
             int index = mChannelSelectionView.getSelectedItemPosition();
             mSelectionChannel.mMtkTvChannelInfo = FavChannelManager.getInstance(mContext).favAddOrEraseIntoIndex(mSelectionChannel.mInternalProviderFlag3,index);
             selectionAdapter.updateData(mSelectionChannel);
             if (isFavTypeState && addEraseChannelId == commonIntegration.getCurrentChannelId()){
                 changeChannelToFirstFav();
             }
             break;
         default :
             break;
        }
        return false;
    }

    public String transMapToString(Map map){
        Entry entry;
        StringBuffer sb = new StringBuffer();
        for(Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Entry)iterator.next();
            sb.append(entry.getKey().toString()).append( "'" ).append(null==entry.getValue()?"":
                    entry.getValue().toString()).append (iterator.hasNext() ? "^" : "");
        }
        return sb.toString();
    }

    public static Map transStringToMap(String mapString){
        Map map = new HashMap();
        StringTokenizer items;
        for(StringTokenizer entrys = new StringTokenizer(mapString, "^");entrys.hasMoreTokens();
            map.put(items.nextToken(), items.hasMoreTokens() ? ((Object) (items.nextToken())) : null)) {
            items = new StringTokenizer(entrys.nextToken(), "'");
        }
        return map;
    }

    private void init() {

       /* if(commonIntegration.isOperatorNTVPLUS()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelTypeRes isOperatorNTVPLUS" );
            types = mContext.getResources().getStringArray(R.array.nav_channel_type_for_russia_for_ntv);
        }else if(commonIntegration.isTELEKARTAContain()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"loadChannelTypeRes isTELEKARTAContain");
            types = mContext.getResources().getStringArray(R.array.nav_channel_type_for_russia_for_telekarta);
        }else if(commonIntegration.isEURegion()){
            types = mContext.getResources().getStringArray(R.array.nav_channel_type);
        }else if(commonIntegration.isUSRegion()){
            types = mContext.getResources().getStringArray(R.array.nav_channel_type_for_us);
        }else if(commonIntegration.isSARegion()){
            types = mContext.getResources().getStringArray(R.array.nav_channel_type);
        }else{
            types = mContext.getResources().getStringArray(R.array.nav_channel_type);
        }*/
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"init types.length = "+types.length );

        mTitlePre =  mContext.getResources().getString(R.string.nav_channel_list)+" - ";
        if (mIsTifFunction) {
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentFocus = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                    resetChList();
                }else {
                   resetChListByTIF();
                }
        } else {
            resetChList();
        }
      //  mTvCallbackHandler.addCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST,mHandler );
      //  mTvCallbackHandler.addCallBackListener(TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE,mHandler );
        if(MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
              TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_SVCTX_NOTIFY, mHandler);
         }

        mChannelListView.setAccessibilityDelegate(mAccDelegate);
        mChannelOptionView.setAccessibilityDelegate(mAccDelegate);
        mChannelSortView.setAccessibilityDelegate(mAccDelegate);
        mChannelTypeView.setAccessibilityDelegate(mAccDelegate);
        mChannelMoreView.setAccessibilityDelegate(mAccDelegate);
        mChannelTypeFavView.setAccessibilityDelegate(mAccDelegate);
        mChannelSelectionView.setAccessibilityDelegate(mAccDelegate);

        mChannelSortView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        mChannelTypeView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        mChannelOptionView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        mChannelListView.setOnKeyListener(new ChannelListOnKey());
        mChannelMoreView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        mChannelTypeFavView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        mChannelSelectionView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);

        showPageUpDownView();
    }


    public List<TIFChannelInfo>getTIFPreOrNextchlistFor3rdSource(boolean next){
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextchlistFor3rdSource");
        List<TIFChannelInfo> mTIFChannelInfoList =new ArrayList<TIFChannelInfo>();
        List<TIFChannelInfo> mTIFChList=null;
        TIFChannelInfo chInfo=null;
        if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
             if(commonIntegration.is3rdTVSource()) {
                   TvInputInfo tvInputInfo = commonIntegration.getTvInputInfo();
                   if(tvInputInfo != null) {
                      mTIFChList = mTIFChannelManager.getTIFChannelInfoBySource(tvInputInfo.getId());
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTIFChList length= "+ (mTIFChList == null? mTIFChList:mTIFChList.size()));

                   } else {
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannelListByTIF, tvInputInfo");
                   }
               }

             if(mTIFChList == null){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTIFPreOrNextchlistFor3rdSource mTIFChList is null");
                 return mTIFChannelInfoList;
             }
             if(mTIFChList.size() <= 7){
                   hasNextPage =false;
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTIFChList.size() <= 7");
                   return mTIFChList;
             }else{
                 hasNextPage =true;
                 if(next){
                        chInfo = mChannelAdapter.getItem(mChannelAdapter.getCount() - 1);
                        boolean startflag=false;
                        for(TIFChannelInfo mTifch : mTIFChList){
                            if(chInfo.mId==mTifch.mId){
                                startflag=true;
                                continue;
                            }
                            if(startflag && mTIFChannelInfoList.size()<7){
                                mTIFChannelInfoList.add(mTifch);
                            }else{
                                startflag=false;
                            }
                        }
                        if( mTIFChannelInfoList.size()<7){
                            int tp=7-mTIFChannelInfoList.size();
                            for(int i=0;i<tp;i++){
                                mTIFChannelInfoList.add(mTIFChList.get(i));
                            }
                        }

                    } else {
                        chInfo = mChannelAdapter.getItem(0);
                        boolean startflag=false;
                        List<TIFChannelInfo> temp=new ArrayList<TIFChannelInfo>();
                        for(int i=mTIFChList.size()-1;i>-1;i--){
                            if(chInfo.mId==mTIFChList.get(i).mId){
                                startflag=true;
                                continue;
                            }
                            if(startflag && temp.size()<7){
                                temp.add(mTIFChList.get(i));
                            }else{
                                startflag=false;
                            }
                        }
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "temp.size()= "+temp.size());
                        if(temp.size()<7){
                            int tp=mTIFChList.size()-(7-temp.size());
                            for(int i=mTIFChList.size()-1;i>=tp;i--){
                                temp.add(mTIFChList.get(i));
                            }

                        }
                        for(int i=temp.size()-1;i>-1;i--){
                            mTIFChannelInfoList.add(temp.get(i));
                        }
                    }
             }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTIFChannelInfoList.size()= "+ mTIFChannelInfoList.size());
            return mTIFChannelInfoList;
        }
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "false mTIFChannelInfoList.size()= "+ mTIFChannelInfoList.size());
        return mTIFChannelInfoList;
    }

    /**
     * get 3rdsource all channel list by tif
     * @return
     */
    public List<TIFChannelInfo> getAllChannelListByTIFFor3rdSource(int mId) {
         List<TIFChannelInfo> mTIFChannelInfoList =new ArrayList<TIFChannelInfo>();
         List<TIFChannelInfo> mTIFChList=null;
        if(commonIntegration.is3rdTVSource()) {
            TvInputInfo tvInputInfo = commonIntegration.getTvInputInfo();
            if(tvInputInfo != null) {
                mTIFChList = mTIFChannelManager.getTIFChannelInfoBySource(tvInputInfo.getId());
            }
            else {

                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getAllChannelListByTIF, tvInputInfo");
            }
        }
        if(mTIFChList !=null && !mTIFChList.isEmpty()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTIFChList.size() = "+ mTIFChList.size());
            if(mTIFChList.size()<=7){
                hasNextPage =false;
                mTIFChannelInfoList=mTIFChList;
            }else{
                hasNextPage =true;
                boolean startflag=false;
                for(TIFChannelInfo mTifch:mTIFChList){
                    if(mId == mTifch.mId){
                        startflag=true;
                    }
                    if(startflag && mTIFChannelInfoList.size() < 7){
                        mTIFChannelInfoList.add(mTifch);
                    }else{
                        startflag=false;
                    }
                }
                if(mTIFChannelInfoList.size()<7){
                    int tp=7-mTIFChannelInfoList.size();
                    for(int i=0;i<tp;i++){
                        mTIFChannelInfoList.add(mTIFChList.get(i));
                    }
                }
            }
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannelListByTIFFor3rdSource mTIFChannelInfoList.size()= "+mTIFChannelInfoList.size());
        return mTIFChannelInfoList;
    }

    public List<TIFChannelInfo> getChannelListByTIFFor3rdSource() {
         List<TIFChannelInfo> mTIFChannelInfoList =new ArrayList<TIFChannelInfo>();
         TvInputInfo tvInputInfo = commonIntegration.getTvInputInfo();
         if(tvInputInfo != null) {
             mTIFChannelInfoList = mTIFChannelManager.getTIFChannelInfoBySource(tvInputInfo.getId());
         }else {

             com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getAllChannelListByTIF, tvInputInfo");
         }
         return mTIFChannelInfoList;
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
                if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                        && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                    list = commonIntegration.getChannelListByMaskAndSat(chInfo.getChannelId(), MtkTvChannelList.CHLST_ITERATE_DIR_NEXT, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal, mCurrentSatelliteRecordId);
                } else {
                    list = getChannelList(chInfo.getChannelId() ,MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,CHANNEL_LIST_PAGE_MAX,mCurMask,mCurVal);
                }
            }else{
                chInfo = mChannelAdapter.getItem(0).mMtkTvChannelInfo;
                if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                        && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                    list = commonIntegration.getChannelListByMaskAndSat(chInfo.getChannelId(), MtkTvChannelList.CHLST_ITERATE_DIR_PREV, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal, mCurrentSatelliteRecordId);
                } else {
                    list = getChannelList(chInfo.getChannelId() ,MtkTvChannelList.CHLST_ITERATE_DIR_PREV,CHANNEL_LIST_PAGE_MAX,mCurMask,mCurVal);
                }
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
    private List<TIFChannelInfo> getNextPrePageChListByTIF(boolean next){
        if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && commonIntegration.isDualTunerEnable() && commonIntegration.isCurrentSourceTv()){
            int tempmask = mCurMask;
            int tempVal = mCurVal;
            if(mCurMask == CommonIntegration.CH_LIST_MASK && mCurVal == CommonIntegration.CH_LIST_VAL){
                mCurMask = CommonIntegration.CH_LIST_DIGITAL_RADIO_MASK;
                mCurVal = CommonIntegration.CH_LIST_DIGITAL_RADIO_VAL;
            }
             List<MtkTvChannelInfoBase> mchannelbase = getNextPrePageChList(next);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNextPrePageChListByTIF getCurrentFocus() mchannelbase= " +(mchannelbase == null? mchannelbase:mchannelbase.size()));
              mCurMask = tempmask;
              mCurVal = tempVal;
             return mTIFChannelManager.getTIFChannelList(mchannelbase);
          }else{
                List<TIFChannelInfo> mTifChannelList = null;
                TIFChannelInfo chInfo = null;
                if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
                    if(next){
                        chInfo = mChannelAdapter.getItem(mChannelAdapter.getCount() - 1);
                        if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                                && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                            mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(chInfo.mMtkTvChannelInfo.getChannelId(), false, false, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                        } else {
                            //if(!commonIntegration.is3rdTVSource()){
                                int channelId=0;
                                if(chInfo.mMtkTvChannelInfo != null){
                                    channelId =chInfo.mMtkTvChannelInfo.getChannelId();
                                }else{
                                    channelId =(int)chInfo.mId;
                                }
                                mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(channelId, false, false, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                        /*  }else{
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is3rdTVSource getTIFPreOrNextchlistFor3rdSource");
                                mTifChannelList= getTIFPreOrNextchlistFor3rdSource(true);
                            }*/
                        }
                    } else {
                        chInfo = mChannelAdapter.getItem(0);
                        if (commonIntegration.isGeneralSatMode() && mCurrentSatelliteRecordId > 0
                                && (CURRENT_CHANNEL_TYPE != ANALOG_CHANNEL)) {
                            mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelListBySateRecId(chInfo.mMtkTvChannelInfo.getChannelId(), true, false, mCurrentSatelliteRecordId, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                        } else {
                        //  if(!commonIntegration.is3rdTVSource()){
                                int channelId=0;
                                if(chInfo.mMtkTvChannelInfo != null){
                                    channelId =chInfo.mMtkTvChannelInfo.getChannelId();
                                }else{
                                    channelId =(int)chInfo.mId;
                                }
                                 mTifChannelList = mTIFChannelManager.getTIFPreOrNextChannelList(channelId, true, false, CHANNEL_LIST_PAGE_MAX, mCurMask, mCurVal);
                        /*  }else{
                                mTifChannelList= getTIFPreOrNextchlistFor3rdSource(false);
                            }*/
                        }
                    }
                }
                if (mChannelAdapter != null && chInfo != null) {
                    if(!commonIntegration.is3rdTVSource()){
                        int channelId=0;
                        if(chInfo.mMtkTvChannelInfo != null){
                            channelId =chInfo.mMtkTvChannelInfo.getChannelId();
                        }else{
                            channelId =(int)chInfo.mId;
                        }
                       saveLastPosition(channelId, mTifChannelList);
                    }else{
                       saveLastPosition((int)chInfo.mId, mTifChannelList);
                    }
                }
                return mTifChannelList;
        }
    }
    /**
     * fav page down or up ,true(next),false(pre)
     */
    private List<TIFChannelInfo> getFavNextPrePageChList(boolean next) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFavNextPrePageChList next "+next);
      TIFChannelInfo chInfo = null;
      List<MtkTvChannelInfoBase> tempApiChList=null;
      if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
        if (next) {
          if(isFavListMove){
              chInfo = favmove;
           }else{
               chInfo = mChannelAdapter.getItem(mChannelAdapter.getCount() - 1);
           }
          tempApiChList = commonIntegration.getFavoriteListByFilter(mCurMask,
                      chInfo.mInternalProviderFlag3 ,false, 0, CHANNEL_LIST_PAGE_MAX+1,CURRENT_CHANNEL_FAV);
        } else {
         if(isFavListMove){
            chInfo = favmove;
            }else{
               chInfo = mChannelAdapter.getItem(0);
            }
         tempApiChList = commonIntegration.getFavoriteListByFilter(mCurMask,
                      chInfo.mInternalProviderFlag3,false, CHANNEL_LIST_PAGE_MAX+1, 0,CURRENT_CHANNEL_FAV);
        }
      }
      return mTIFChannelManager.getTIFChannelListForFavIndex(tempApiChList, CURRENT_CHANNEL_FAV);
    }
    /**
     * fav list count
     * @return
     */
    private int getFavListCount(){
        //List<MtkTvFavoritelistInfoBase> tempApiChList=null;
        //tempApiChList = commonIntegration.getFavoriteListByFilter();
        int count = commonIntegration.getFavoriteCount(commonIntegration.getSvl(),mCurMask,mCurVal);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"yyiran  tempApiChList size:"+count);
        //return tempApiChList.size();
        return count;
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
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setChannelListTitle isFavTypeState: "+isFavTypeState);
        if(isSelectionMode()){
            if ((CommonIntegration.isEURegion() && !commonIntegration.isTVSourceSeparation()) ||CommonIntegration.isSARegion() || (commonIntegration.isTVSourceSeparation() && commonIntegration.isCurrentSourceDTV())) {
                if(isFavTypeState){
                    mTitleText.setText(mTitlePre + favtypes[CURRENT_CHANNEL_FAV]);
                }else{
                    mTitleText.setText(mTitlePre + types[CURRENT_CHANNEL_TYPE]);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setChannelListTitle eu eupadtv sa!!! ");
                }

               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setChannelListTitle eu eupadtv sa!!! ");
               mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_select_more));

           }else if(CommonIntegration.isUSRegion()){
               if(commonIntegration.isDisableColorKey()){
                   mTitleText.setText( mContext.getResources().getString(R.string.nav_channel_list));
               }else{
                   if(isFavTypeState){
                       mTitleText.setText(mTitlePre + favtypes[CURRENT_CHANNEL_FAV]);
                   }else{
                       mTitleText.setText(mTitlePre + types[CURRENT_CHANNEL_TYPE]);
                   }
                 //  mTitleText.setText(mTitlePre + types[CURRENT_CHANNEL_TYPE]);
                   mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_select_more));
                   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setChannelListTitle us!!! ");
               }

           } else {
               mTitleText.setText(R.string.nav_channel_list);
               mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_type_fav));
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setChannelListTitle cn!!! ");
           }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setChannelListTitle mYellowKeyText "+mYellowKeyText.getVisibility());
        }else{
           mTitleText.setText(R.string.nav_channel_list);
           mYellowKeyText.setText(mContext.getResources().getString(R.string.nav_uk_channellist_type_fav));
       }
        if(mCurMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK){
            mBlueKeyText.setVisibility(View.INVISIBLE);
            mBlueicon.setVisibility(View.INVISIBLE);
        }
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
                    if((commonIntegration.isEURegion() && !commonIntegration.isTVSourceSeparation())
                            || commonIntegration.isUSRegion()
                            || commonIntegration.isSARegion()
                            || (commonIntegration.isTVSourceSeparation() && commonIntegration.isCurrentSourceDTVforEuPA())
                            || (commonIntegration.isCNRegion() && commonIntegration.isCurrentSourceDTV())) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasChannel  isEURegion= ");
                        if(mTIFChannelManager.get3RDChannelList().size() < 1 || !commonIntegration.isCurrentSourceTv()){
                            hasChannel = false;
                        }else{
                            if(types !=null && types.length > 0) {
                                CURRENT_CHANNEL_TYPE = types.length-1;
                                resetMask();
                                typesSecond = null;
                                mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                            }

                        }

                    }else{
                        hasChannel = false;
                    }
                }else{
                    if(mTIFChannelManager.get3RDChannelList().size() == 0){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTIFChannelManager.get3RDChannelList().size() is 0 deal channel list type ");
                        int mask =   mSaveValue.readValue(CommonIntegration.channelListfortypeMask,0);
                        int val =    mSaveValue.readValue(CommonIntegration.channelListfortypeMaskvalue,0);
                        if(mask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK && val ==CommonIntegration.CH_LIST_3RDCAHNNEL_VAL){
                            Log.d(TAG, "mChannelsFor3RDSource channel list type set broadcast 0");
                            mSaveValue.saveValue(CH_TYPE,0);
                         //   mSaveValue.saveValue(CommonIntegration.channelListfortypeMask,CommonIntegration.CH_LIST_MASK);
                         //   mSaveValue.saveValue(CommonIntegration.channelListfortypeMaskvalue,CommonIntegration.CH_LIST_VAL);
                        }
                    }
                }
            }

        } else {
            if(!commonIntegration.hasActiveChannel()
                    ||!commonIntegration.isCurrentSourceTv()){
                hasChannel = false;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hasChannel = "+hasChannel);
        return hasChannel;
    }

    @Override
    public void show() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show");
        super.show();
        isFavListMove=false;
        mTitleText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        setWindowPosition();
        if (mChannelDetailLayout != null) {
            mChannelDetailLayout.setVisibility(View.INVISIBLE);
        }
       if( commonIntegration.isDisableColorKey()){
           mChannelListTipView.setVisibility(View.GONE);
       }
        mCanChangeChannel = false;
       if( chanelListProgressbar != null && chanelListProgressbar.getVisibility() != View.VISIBLE ){
           chanelListProgressbar.setVisibility(View.VISIBLE);
       }
        commonIntegration.setChannelChangedListener(listener);
       /* if (commonIntegration.isGeneralSatMode()) {
            resetSatellites();
        }*/
        mChannelListView.setAdapter(null);
        mChannelAdapter = null;
        mChannelListView.setVisibility(View.GONE);
        init();
        currentChannelType();
    }

    private ChannelChangedListener listener = new ChannelChangedListener(){
        public void onChannelChanged(){
            if(ChannelListDialog.this.isShowing() && mChannelListView.getVisibility() == View.VISIBLE ){
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
        CHANNEL_LIST_PAGE_MAX  =(int) sca.getFloat();
        int menuWidth = (int) (display.getWidth() * chwidth);
        lp.width = menuWidth;
        lp.height = display.getHeight();
        int x = display.getWidth();
        int y =0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setWindowPosition menuWidth "+menuWidth+" x "+x +" display.getWidth() "+display.getWidth());
        /*lp.x = x;
        lp.y = y;
        window.setAttributes(lp);*/
        if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
            lp.gravity = Gravity.START;
        }else {
            lp.gravity = Gravity.END;
        }
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
             if (mChannelAdapter==null || mChannelAdapter.getChannellist() == null || mChannelAdapter.getChannellist().size() == 0){
                 mBlueKeyText.setVisibility(View.INVISIBLE);
                 mBlueicon.setVisibility(View.INVISIBLE);
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showPageUpDownView bluekey is invisible ");
             }else{
                 mBlueKeyText.setVisibility(View.VISIBLE);
                 mBlueicon.setVisibility(View.VISIBLE);
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showPageUpDownView bluekey is visible ");
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
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        if(DestroyApp.getTopActivity() != null && DestroyApp.getTopActivity().equals("EditTextActivity") || isFindState){
            isFindState=false;
            return ;
        }
        isFindingForChannelList =false;
        addEraseChannelId = -1;
        ttsSelectchannelIndex = -1;
        if (mChannelOptionView != null){
            mChannelOptionView.setVisibility(View.GONE);
        }
        if (mChannelSortView != null){
            mChannelSortView.setVisibility(View.GONE);
        }
        if (mChannelTypeView != null){
            mChannelTypeView.setVisibility(View.GONE);
        }
        if (mChannelTypeFavView != null){
            mChannelTypeFavView.setVisibility(View.GONE);
        }
        mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
        mCanChangeChannel = true;


        if (mChannelListView != null) {
            mChannelListView.setAdapter(null);
            mChannelAdapter = null;
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
        if (isFavTypeState){
            MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
        }
    }

    private final Handler dvrEventHandler = new Handler(){
        int mm = 0;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int callBack = msg.what;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dvrEventHandler  = " + callBack);
            switch (callBack) {
                case DvrConstant.MSG_CALLBACK_RECORD_STOPPED://4100
                    DvrManager.getInstance().getController().removeEventHandler(dvrEventHandler);
                    if (stoppvr){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"yiran handleMessage ===" );
                        stoppvr = false;
                        mHandler.sendEmptyMessage(CHANGE_TYPE_CHANGECHANNEL);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * show dialog when pvr or timeshift
     * @param change_type  0 channel 1 fav 2 category
     */
    public void showDialogChangeTypeForPVRAndTShift(final int changeType,int typeTimeShift) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDialogChangeTypeForPVRAndTShift  changeType:"+changeType);
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleDialog simpleDialog = (SimpleDialog)ComponentsManager.getInstance().
                        getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
                simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
                simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
                simpleDialog.setCancelText(R.string.pvr_confirm_no);
                Bundle bundle = new Bundle();
                simpleDialog.setBundle(bundle);
                simpleDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {

                    @Override
                    public void onConfirmClick(int dialogId) {
                        if (DvrManager.getInstance() != null && DvrManager.getInstance().pvrIsRecording()){
                            DvrManager.getInstance().getController().addEventHandler(dvrEventHandler);
                            DvrManager.getInstance().stopAllRunning();
                            stoppvr = true;
                        }else if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
                            if (TifTimeShiftManager.getInstance() != null) {
                                TifTimeShiftManager.getInstance().stopAll();
                            }
                        }
                          switch(changeType){
                          case TYPE_CHANGE_CHANNEL:
                              chooseChannelType();
                              break;
                          case TYPE_CHANGE_FAVORITE:
                              chooseFavChannel();
                              break;
                          case TYPE_CHANGE_SATELLITE:
                              chooseSatelliteType();
                              break;
                          default:
                              break;
                          }
                    }
                },-1);
                simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick(int dialogId) {
                        // TODO Auto-generated method stub
                        simpleDialog.dismiss();
                    }
                },-1);
                if(typeTimeShift==DvrDialog.TYPE_Record){
                    simpleDialog.setContent(R.string.dvr_dialog_message_record_type);
                }else {
                      simpleDialog.setContent(R.string.dvr_dialog_message_timeshift_type);
                }
                simpleDialog.show();
            }
          });
    }

    public void showDialogForPVRAndTShift(int tYPEConfirmFromChannelList,int keyCode,int typeTimeShift,int channelId) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDialogForPVRAndTShift keyCode "+keyCode);
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
                if(typeTimeShift==DvrDialog.TYPE_Record){
                	simpleDialog.setContent(R.string.dvr_dialog_message_record_channel);
                }else {
                	  simpleDialog.setContent(R.string.dvr_dialog_message_timeshift_channel);
				}
                simpleDialog.show();
            }
          });

    }

    private void saveCurrentSecondName(int secondType){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveCurrentSecondName secondType = "+secondType);
        if (secondType != -1){
            if (typesSecond == null){
                initSecondType();
            }
            if (typesSecond != null){
                CURRENT_CHANNEL_SECOND_NAME = typesSecond.get(secondType);
            }
        }
    }

    public void chooseSatelliteType(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position = "+mChannelSatelliteSecondView.getSelectedItemPosition());
        LAST_CHANNEL_SECOND_TYPE = CURRENT_CHANNEL_SECOND_TYPE;
        LAST_CHANNEL_SECOND_NAME = CURRENT_CHANNEL_SECOND_NAME;
        CURRENT_CHANNEL_SECOND_TYPE = mChannelSatelliteSecondView.getSelectedItemPosition();
        saveCurrentSecondName(CURRENT_CHANNEL_SECOND_TYPE);
        saveTypeLastChannelId(LAST_CHANNEL_TYPE,LAST_CHANNEL_SECOND_TYPE);
        resetCategories();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER mCurCategories = "+mCurCategories);
        mSaveValue.saveValue(CH_TYPE_SECOND,CURRENT_CHANNEL_SECOND_TYPE);
     //   mSaveValue.saveValue(CH_TYPE_FAV,-1);
        mChannelListView.setAdapter(null);
        mChannelAdapter = null;
        mChannelListView.setVisibility(View.VISIBLE);
        mChannelSatelliteSecondView.setVisibility(View.GONE);
        if (mIsTifFunction) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
            if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                resetChList();
            }else {
                mChannelListView.setVisibility(View.GONE);
               //if( !TIFFunctionUtil.checkChCategoryMask(commonIntegration.getCurChInfo(), TIFFunctionUtil.getmCurCategories(), TIFFunctionUtil.getmCurCategories())){
                  SELECT_TYPE_CHANGE_CH= true ;
               /*}else{
                   SELECT_TYPE_CHANGE_CH= false ;
               }*/
                if( chanelListProgressbar != null && chanelListProgressbar.getVisibility() != View.VISIBLE ){
                    chanelListProgressbar.setVisibility(View.VISIBLE);
                }
                CURRENT_OPTION=ALL_CHANNEL;
                resetTypeChListByTIF();
            }
        } else {
            resetChList();
        }
    }

    public void chooseFavChannel(){
        LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
        CURRENT_CHANNEL_TYPE = CHANGE_TYPE_FAVORITE_INDEX;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"chooseFavChannel start");
        mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
        // save fav type,some time favlist is empty
        LAST_CHANNEL_FAV = CURRENT_CHANNEL_FAV;
        CURRENT_CHANNEL_FAV = mChannelTypeFavView.getSelectedItemPosition();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_FAV>>>" + CURRENT_CHANNEL_FAV);
        saveTypeLastChannelId(LAST_CHANNEL_TYPE,LAST_CHANNEL_SECOND_TYPE);
        resetFavMask();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
        if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
            resetChList();
        }else {
           int favChannelCount = commonIntegration.getFavouriteChannelCount(mCurMask);
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_fav_view favChannelCount have "+favChannelCount);
            if(favChannelCount > 0){
               mChannelListView.setAdapter(null);
               mChannelAdapter = null;
               mChannelListView.setVisibility(View.GONE);
               mChannelTypeView.setVisibility(View.GONE);
               mChannelTypeFavView.setVisibility(View.GONE);
               /*if(commonIntegration.checkCurChMask(mCurMask, mCurVal)){
                   SELECT_TYPE_CHANGE_CH=false;
               }else{*/
                   SELECT_TYPE_CHANGE_CH= true ;
               //}
               if( chanelListProgressbar != null && chanelListProgressbar.getVisibility() != View.VISIBLE ){
                   chanelListProgressbar.setVisibility(View.VISIBLE);
               }
               resetTypeChListByTIF();
               CURRENT_OPTION=ALL_CHANNEL;
               mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
               mSaveValue.saveValue(CH_TYPE_FAV,CURRENT_CHANNEL_FAV);
               mSaveValue.saveValue(CH_TYPE_SECOND,-1);
            }else{
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_FAV current type no chanenls,return last type");
             //  Toast.makeText(mContext,  mContext.getResources().getString(R.string.nav_select_type_no_channel) , Toast.LENGTH_SHORT).show();
           //    initMaskAndSatellites();
                if (mChannelAdapter!=null){
                    mChannelAdapter.updateData(new ArrayList<TIFChannelInfo>());
                }
               mChannelListView.setAdapter(null);
               mChannelAdapter = null;
               mChannelListView.setVisibility(View.VISIBLE);
               mChannelTypeView.setVisibility(View.GONE);
               mChannelTypeFavView.setVisibility(View.GONE);
               mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
               mSaveValue.saveValue(CH_TYPE_FAV,CURRENT_CHANNEL_FAV);
               mSaveValue.saveValue(CH_TYPE_SECOND,-1);
               setChannelListTitle();
               mChannelListTipView.setVisibility(View.VISIBLE);
               hasNextPage=false;
               showPageUpDownView();
            }
        }
    }

    public void chooseChannelTypeByMask(int type,int curmask,int curval){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey ,curmask: " +curmask +" curval "+curval);
        if (mIsTifFunction) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
            if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                resetChList();
            }else {
                if(mTIFChannelManager.getChannelListConfirmLength(1, curmask, curval) > 0){
                    resetChListByTIF();
                    mSaveValue.saveValue(CH_TYPE,type);
                    CURRENT_CHANNEL_SECOND_TYPE = -1;
                    mSaveValue.saveValue(CH_TYPE_SECOND,CURRENT_CHANNEL_SECOND_TYPE);
                    //  mSaveValue.saveValue(CH_TYPE_FAV,-1);
                    CURRENT_OPTION=ALL_CHANNEL;
                }
            }
        } else {
            resetChList();
        }
    }

    public void chooseChannelType(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey ,mCurMask: " +mCurMask +" mCurVal "+mCurVal);
        if (mIsTifFunction) {
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListOnKey = "+commonIntegration.getCurrentFocus().equalsIgnoreCase("sub")+"  isCurrentSourceTv = "+commonIntegration.isCurrentSourceTv());
                if(commonIntegration.getCurrentFocus().equalsIgnoreCase("sub") && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DUAL_TUNER_SUPPORT) && commonIntegration.isCurrentSourceTv()){
                    resetChList();
                }else {
                    if(mTIFChannelManager.getChannelListConfirmLength(1, mCurMask, mCurVal) > 0){
                       mChannelListView.setVisibility(View.GONE);
                       mChannelTypeView.setVisibility(View.GONE);
                        SELECT_TYPE_CHANGE_CH= true ;
                       if( chanelListProgressbar != null && chanelListProgressbar.getVisibility() != View.VISIBLE ){
                           chanelListProgressbar.setVisibility(View.VISIBLE);
                       }
                       resetTypeChListByTIF();
                       mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                       CURRENT_CHANNEL_SECOND_TYPE = -1;
                       mSaveValue.saveValue(CH_TYPE_SECOND,CURRENT_CHANNEL_SECOND_TYPE);
                     //  mSaveValue.saveValue(CH_TYPE_FAV,-1);
                       CURRENT_OPTION=ALL_CHANNEL;
                    }else{
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANGE_TYPE_CHANGECHANNEL current type no chanenls,return last type");
                       Toast.makeText(mContext,  mContext.getResources().getString(R.string.nav_select_type_no_channel) , Toast.LENGTH_SHORT).show();
                       initMaskAndSatellites();
                      /* setChannelListTitle();
                       showPageUpDownView();
                       mChannelListTipView.setVisibility(View.VISIBLE);
                       mChannelListView.setVisibility(View.VISIBLE);
                       CURRENT_CHANNEL_TYPE = mSaveValue.readValue(CH_TYPE,ALL_CHANNEL);
                       mChannelTypeView.setSelection(CURRENT_CHANNEL_TYPE);
                       mChannelTypeView.setVisibility(View.GONE);*/
                    }

                }

        } else {
            resetChList();
        }
    }

    public void chooseType(int chooseType){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"choose_type start");
        if (DvrManager.getInstance() != null && DvrManager.getInstance().pvrIsRecording()){
            /*if ((types.length - 1) == mChannelTypeView.getSelectedItemPosition()){
                chooseChannelType();
            }else{*/
                showDialogChangeTypeForPVRAndTShift(chooseType,DvrDialog.TYPE_Record);
            //}
        }else if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
            showDialogChangeTypeForPVRAndTShift(chooseType,DvrDialog.TYPE_Timeshift);
        }else{
            switch(chooseType){
            case TYPE_CHANGE_CHANNEL:
                chooseChannelType();
                break;
            case TYPE_CHANGE_FAVORITE:
                chooseFavChannel();
                break;
            case TYPE_CHANGE_SATELLITE:
                chooseSatelliteType();
                break;
            default:
                break;
            }
        }
    }

    public synchronized void saveTypeLastChannelId(int type,int secondType){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran saveTypeLastChannelId type:"+type+" secondType:"+secondType);
        int channelId = commonIntegration.getCurrentChannelId();
        String mapStr = mSaveValue.readStrValue("type_save_"+commonIntegration.getSvl(),"");
        Map map = null;
        if (mapStr.length() == 0){
            map = new HashMap();
        }else{
            map = transStringToMap(mapStr);
        }
        // map.put(key, channelId);
        if((commonIntegration.isOperatorNTVPLUS() ||commonIntegration.isOperatorTKGS()) && type == CATEGORIES_CHANNEL && commonIntegration.getTunerMode() == 2){
            map.put("type_"+LAST_CHANNEL_SECOND_NAME, channelId);
        }else if(commonIntegration.isTELEKARTAContain() &&  (type == PACKS_CHANNEL || type == CATEGORIES_CHANNEL ) && commonIntegration.getTunerMode() == 2){
            map.put("type_"+LAST_CHANNEL_SECOND_NAME, channelId);
        }else if (type == CHANGE_TYPE_FAVORITE_INDEX  && commonIntegration.checkCurChMask(mCurMask, mCurVal)){
            map.put("type_"+type+"_"+LAST_CHANNEL_FAV,channelId);
        } else{
            map.put("type_"+type, channelId);
        }
        mapStr = transMapToString(map);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran saveTypeLastChannelId mapStr:"+mapStr);
        mSaveValue.saveValue("type_save_"+commonIntegration.getSvl(),mapStr);
    }

    public int getTypeLastChannelId(int type,int secondType){
        //int channelId = commonIntegration.getCurrentChannelId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran getTypeLastChannelId type:"+type+" secondType:"+secondType);
        String mapStr = mSaveValue.readStrValue("type_save_"+commonIntegration.getSvl(),"");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran getTypeLastChannelId mapStr:"+mapStr);
        if (mapStr.length() == 0){
            return -1;
        }else{
            int channelid = -1;
            Map map = transStringToMap(mapStr);
            Object obj = null;
            if((commonIntegration.isOperatorNTVPLUS() ||commonIntegration.isOperatorTKGS()) && type == CATEGORIES_CHANNEL && commonIntegration.getTunerMode() == 2){
                if (typesSecond == null){
                    initSecondType();
                }
                if (typesSecond != null){
                    obj = map.get("type_"+typesSecond.get(secondType));
                }
            }else if(commonIntegration.isTELEKARTAContain() &&  (type == PACKS_CHANNEL || type == CATEGORIES_CHANNEL ) && commonIntegration.getTunerMode() == 2){
                if (typesSecond == null){
                    initSecondType();
                }
                if (typesSecond != null){
                    obj = map.get("type_"+typesSecond.get(secondType));
                }
            }else if (type == CHANGE_TYPE_FAVORITE_INDEX){
                obj = map.get("type_"+type+"_"+CURRENT_CHANNEL_FAV);
            } else{
                obj = map.get("type_"+type);
            }
            if (obj == null){
                return -1;
            }else{
                channelid = Integer.parseInt(obj.toString());
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yiran getTypeLastChannelId channelid:"+channelid);
            return channelid;
        }
    }
    class ChannelListOnKey implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "yiran ChannelListOnKey --- keycode:"+keyCode);
        switch(v.getId()){
        case R.id.nav_channel_listview:
            int selectPosition = mChannelListView.getSelectedItemPosition();
             com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"ChannelListOnKey keyCode ="+keyCode+" selectPosition = "+
                selectPosition +"mChannelListView ="+mChannelListView+" count:"+mChannelListView.getCount()+" adapter.count:"+mChannelAdapter.getCount());
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    startTimeout(NAV_TIMEOUT_300);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mChannelItemKeyLsner*********** selectPosition = "
                                    + mChannelListView.getSelectedItemPosition());
                            if(isFavTypeState && isFavListMove){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mChannelItemKeyLsner*********** selectPosition");
                                isFavListMove=false;
                                int indexFrom = favChannelManager.getFavoriteIdx( commonIntegration.favMask[CURRENT_CHANNEL_FAV],favmovetag.mInternalProviderFlag3,CURRENT_CHANNEL_FAV);
                                /*if (moveIsUp == 0 || moveIsUp == 1){
                                    indexTo = favChannelManager.getFavoriteIdx( commonIntegration.favMask[CURRENT_CHANNEL_FAV],favmove.mInternalProviderFlag3,CURRENT_CHANNEL_FAV);
                                }*/
                                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "yyiran  move  indexFrom:"+indexFrom+"  indexTo:"+indexTo);
                                favChannelManager.favoriteListInsertMove(indexFrom, indexTo);
                                resetChannelListMoveViw();
                                return true;
                            }else{
                                TIFChannelInfo tIFChannelInfo;
                                if(mContext != null && TextToSpeechUtil.isTTSEnabled(mContext)){
                                    //tIFChannelInfo  =mTIFChannelManager.getTIFChannelInfoById(mChannelIdList.get(ttsSelectchannelIndex));
                                     return true; // select channel not support focus when tts is true
                                }else{
                                    tIFChannelInfo = (TIFChannelInfo) mChannelListView.getSelectedItem();
                                    dismiss();
                                    selectTifChannel(keyCode, tIFChannelInfo);
                                }
                            }

                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!! isFavTypeState is "+isFavTypeState+" isFavListMove is "+isFavListMove);
                    if(isFavTypeState && isFavListMove){

                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!! isFavListMove is start");
                        if (hasNextPage && selectPosition == mChannelAdapter.getCount() - 1 ) {
                            List<TIFChannelInfo> favlist=  getFavNextPrePageChList(true);
                            if (favlist == null || favlist.isEmpty()){
                                return true;
                            }
                            for(int i=0;i<favlist.size();i++){
                                if(favmovetag.mInternalProviderFlag3 == favlist.get(i).mInternalProviderFlag3){
                                    favlist.remove(i);
                                    break;
                                }
                            }
                            if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                                favlist.remove(favlist.size()-1);
                            }
                            favlist.add(0,favmovetag);
                            if (indexTo >= favCount - 1){
                                indexTo = 0;
                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran qqqqq set indexTo "+indexTo);
                            moveIsUp = -1;
                            mChannelAdapter.setFavlistMovepostion(0);
                            mChannelAdapter.updateData(favlist);
                            mChannelListView.setSelection(0);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }else {
                            if (mChannelListView.getSelectedItemPosition() < mChannelAdapter.getCount() - 1){
                                List<TIFChannelInfo> favlist=  mChannelAdapter.getChannellist();
                                favmove = favlist.remove(selectPosition+1);
                                favlist.add(selectPosition,favmove);
                                if (indexTo < favCount - 1){
                                    indexTo = indexTo+1;
                                }else if(favCount <= CHANNEL_LIST_PAGE_MAX){
                                    indexTo = 0;
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran no need indexTo:"+indexTo);
                                }else{
                                    indexTo = 1;
                                }
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran  gggww set indexTo:"+indexTo);
                                moveIsUp = 0;
                                mChannelAdapter.setFavlistMovepostion(selectPosition+1);
                                mChannelAdapter.updateData(favlist);
                                mChannelListView.setSelection(selectPosition+1);
                                startTimeout(NAV_TIMEOUT_300);
                                return true;
                            }else if(mChannelListView.getSelectedItemPosition() == mChannelAdapter.getCount() - 1){
                                List<TIFChannelInfo> favlist=  mChannelAdapter.getChannellist();
                                favmove = favlist.remove(favlist.size()-1);
                                favlist.add(0,favmove);
                                if (indexTo >= favCount - 1){
                                    indexTo = 0;
                                }
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran dddd set indexTo -- :"+indexTo);
                                moveIsUp = -1;
                                mChannelAdapter.setFavlistMovepostion(0);
                                mChannelAdapter.updateData(favlist);
                                mChannelListView.setSelection(0);
                                startTimeout(NAV_TIMEOUT_300);
                                return true;
                            }

                        }
                            return false;

                    }else{
                        mChannelDetailLayout.setVisibility(View.INVISIBLE);
                        if(selectPosition == mChannelAdapter.getCount()-1 && hasNextPage){
                            if (mIsTifFunction) {
                                if(isFavTypeState){
                                    List<TIFChannelInfo> favlist=  getFavNextPrePageChList(true);
                                    if (favlist == null || favlist.isEmpty()){
                                        return true;
                                    }
                                    if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                                        favlist.remove(favlist.size()-1);
                                    }
                                    mChannelAdapter.updateData(favlist);
                                }else{
                                    mChannelAdapter.updateData(getNextPrePageChListByTIF(true));
                                }
                            } else {
                                mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(true)));
                            }
                            mChannelListView.setSelection(0);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }else{
                            startTimeout(NAV_TIMEOUT_300);
                            if(mChannelListView.getSelectedItemPosition() == mChannelAdapter.getCount()-1){
                                mChannelListView.setSelection(0);
                            }else{
                                mChannelListView.setSelection(mChannelListView.getSelectedItemPosition()+1);
                            }
                        }
                        TIFChannelInfo downFavchannel= mChannelAdapter.getChannellist().get(mChannelListView.getSelectedItemPosition());
                        if(downFavchannel.mDataValue != null && downFavchannel.mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                            mBlueKeyText.setVisibility(View.VISIBLE);
                            mBlueicon.setVisibility(View.VISIBLE);
                        }else{
                            mBlueKeyText.setVisibility(View.INVISIBLE);
                            mBlueicon.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    }

                case KeyEvent.KEYCODE_DPAD_UP:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_UP!!!!! isFavTypeState +"+isFavTypeState+" isFavListMove is "+isFavListMove);
                    if(isFavTypeState && isFavListMove){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!! isFavListMove is start");
                        if (hasNextPage &&  selectPosition == 0 ) {
                            List<TIFChannelInfo> favlist=  getFavNextPrePageChList(false);
                            if (favlist == null || favlist.isEmpty()){
                                return true;
                            }
                            for(int i=0;i<favlist.size();i++){
                                if(favmovetag.mInternalProviderFlag3 == favlist.get(i).mInternalProviderFlag3){
                                    favlist.remove(i);
                                    break;
                                }
                            }
                            if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                                favlist.remove(0);
                            }
                            mChannelAdapter.setFavlistMovepostion(mChannelAdapter.getCount() - 1);
                            favlist.add(favmovetag);
                            if (indexTo <= 0){ // has next page and top and indexto = 0 -> favcout - 1 ,beause < last channel
                                indexTo = favCount - 1;
                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran qqww set indexTo "+indexTo);
                            moveIsUp = -1;
                            mChannelAdapter.updateData(favlist);
                            mChannelListView.setSelection(mChannelAdapter.getCount() - 1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        } else { // !hasNextPage ||  selectPosition != 0
                            if (mChannelListView.getSelectedItemPosition() >0){ // not top
                                List<TIFChannelInfo> favlist=  mChannelAdapter.getChannellist();
                                favmove = favlist.remove(selectPosition-1);
                                favlist.add(selectPosition,favmove);
                                if (indexTo > 0) { //not first
                                    indexTo = indexTo - 1;
                                }else if(favCount <= CHANNEL_LIST_PAGE_MAX){// indexto = 0 first  !hasnext
                                    indexTo = favCount-1;
                                }else{   //has next
                                    indexTo = favCount -2;
                                }

                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran bbbbb set indexTo:"+indexTo);
                                moveIsUp = 1;
                                mChannelAdapter.setFavlistMovepostion(selectPosition-1);
                                mChannelAdapter.updateData(favlist);
                                mChannelListView.setSelection(selectPosition-1);
                                startTimeout(NAV_TIMEOUT_300);
                                return true;
                            }else if(mChannelListView.getSelectedItemPosition() == 0){//is top
                                List<TIFChannelInfo> favlist=  mChannelAdapter.getChannellist();
                                favmove = favlist.remove(0);
                                favlist.add(favmove);
                                if (indexTo <= 0){
                                    indexTo = favCount - 1 ;
                                }
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yyiran set indexTo -- "+indexTo);
                                moveIsUp = -1;
                                mChannelAdapter.setFavlistMovepostion(favlist.size()-1);
                                mChannelAdapter.updateData(favlist);
                                mChannelListView.setSelection(favlist.size()-1);
                                startTimeout(NAV_TIMEOUT_300);
                                return true;
                            }
                            return false;
                        }


                    }else{
                        mChannelDetailLayout.setVisibility(View.INVISIBLE);
                        if(selectPosition == 0 && hasNextPage){
                            if (mIsTifFunction) {
                                if(isFavTypeState){
                                    List<TIFChannelInfo> favlist=  getFavNextPrePageChList(false);
                                    if (favlist == null || favlist.isEmpty()){
                                        return true;
                                    }
                                    if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                                        favlist.remove(0);
                                    }
                                    mChannelAdapter.updateData(favlist);
                                }else{
                                    mChannelAdapter.updateData(getNextPrePageChListByTIF(false));
                                }
                            } else {
                                mChannelAdapter.updateData(TIFFunctionUtil.getTIFChannelList(getNextPrePageChList(false)));
                            }
                            mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }else{
                            startTimeout(NAV_TIMEOUT_300);
                            if(mChannelListView.getSelectedItemPosition() == 0){
                                mChannelListView.setSelection(mChannelAdapter.getCount()-1);
                            }else{
                                mChannelListView.setSelection(mChannelListView.getSelectedItemPosition()-1);
                            }
                        }
                        TIFChannelInfo upFavchannel= mChannelAdapter.getChannellist().get(mChannelListView.getSelectedItemPosition());
                        if(upFavchannel.mDataValue != null && upFavchannel.mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                            mBlueKeyText.setVisibility(View.VISIBLE);
                            mBlueicon.setVisibility(View.VISIBLE);
                        }else{
                            mBlueKeyText.setVisibility(View.INVISIBLE);
                            mBlueicon.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    }

                case  KeyMap.KEYCODE_PAGE_DOWN:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_PAGE_DOWN!!!!!");
                    //dismiss();
                  //  FavChannelManager.getInstance(mContext).favAddOrErase();
                    return true;
                    default:
                        break;
                }
            }
                    break;
            case R.id.nav_channel_typeview:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimeout(NAV_TIMEOUT_300);
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(TextToSpeechUtil.isTTSEnabled(mContext)){
                             return true; // select channel not support focus when tts is true
                        }

                        mHandler.removeMessages(CHANGE_TYPE_CHANGECHANNEL);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position = "+CURRENT_CHANNEL_TYPE);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER commonIntegration.isOperatorNTVPLUS() = "+commonIntegration.isOperatorNTVPLUS());
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER commonIntegration.isTELEKARTAContain() = "+commonIntegration.isTELEKARTAContain());
                        if((commonIntegration.isOperatorNTVPLUS() ||commonIntegration.isOperatorTKGS()) && mChannelTypeView.getSelectedItemPosition() >0  && commonIntegration.getTunerMode() == 2){
                             if(mChannelTypeView.getSelectedItemPosition() == CATEGORIES_CHANNEL){
                                 LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
                                 CURRENT_CHANNEL_TYPE = mChannelTypeView.getSelectedItemPosition();
                                 mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                                loadChannelListTypeSecondeView();
                                return true;
                             }else if(mChannelTypeView.getSelectedItemPosition() == CHANGE_TYPE_FAVORITE_INDEX){
                                 loadChannelTypefavsRes();
                                 return true;
                             }else{

                                 if(mSaveValue.readValue(CH_TYPE,0) == mChannelTypeView.getSelectedItemPosition()){
                                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER isOperatorNTVPLUS position == CURRENT_CHANNEL_TYPE noy deal");
                                     return true;
                                 }
                             }
                        }else if(commonIntegration.isTELEKARTAContain() &&  mChannelTypeView.getSelectedItemPosition() > 0  && commonIntegration.getTunerMode() == 2){
                             if(mChannelTypeView.getSelectedItemPosition() == PACKS_CHANNEL || mChannelTypeView.getSelectedItemPosition() == CATEGORIES_CHANNEL){
                                 LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
                                 CURRENT_CHANNEL_TYPE = mChannelTypeView.getSelectedItemPosition();
                                 mSaveValue.saveValue(CH_TYPE,CURRENT_CHANNEL_TYPE);
                                 loadChannelListTypeSecondeView();
                                 return true;
                             }else if(mChannelTypeView.getSelectedItemPosition() == CHANGE_TYPE_FAVORITE_INDEX){
                                 loadChannelTypefavsRes();
                                 return true;
                             }else{
                                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER isTELEKARTAContain mSaveValue.readValue(CH_TYPE,-1) "+mSaveValue.readValue(CH_TYPE,-1));
                                 if(mSaveValue.readValue(CH_TYPE,0) == mChannelTypeView.getSelectedItemPosition()){
                                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER isTELEKARTAContain == CURRENT_CHANNEL_TYPE noy deal");
                                     return true;
                                 }
                             }

                        }else{

                           if(mChannelTypeView.getSelectedItemPosition() == CHANGE_TYPE_FAVORITE_INDEX){
                               loadChannelTypefavsRes();
                               return true;
                            }else{
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER mSaveValue.readValue(CH_TYPE,-1) "+mSaveValue.readValue(CH_TYPE,-1));
                                if(mSaveValue.readValue(CH_TYPE,-1) == mChannelTypeView.getSelectedItemPosition()){
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position == CURRENT_CHANNEL_TYPE noy deal");
                                    return true;
                                }
                            }

                        }
                        saveTypeLastChannelId(CURRENT_CHANNEL_TYPE,LAST_CHANNEL_SECOND_TYPE);
                        LAST_CHANNEL_FAV = CURRENT_CHANNEL_FAV;
                        LAST_CHANNEL_TYPE = CURRENT_CHANNEL_TYPE;
                        CURRENT_CHANNEL_TYPE = mChannelTypeView.getSelectedItemPosition();
                        resetMask();
                        typesSecond = null;
                        if (mTIFChannelManager.getChannelListConfirmLength(1, mCurMask, mCurVal) > 0){
                            chooseType(TYPE_CHANGE_CHANNEL);
                        }else{
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANGE_TYPE_CHANGECHANNEL current type no chanenls,return last type");
                            Toast.makeText(mContext,  mContext.getResources().getString(R.string.nav_select_type_no_channel) , Toast.LENGTH_SHORT).show();
                            initMaskAndSatellites();
                        }

                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelTypeView.getSelectedItemPosition());
                        if(mChannelTypeView.getSelectedItemPosition() == mChannelTypeView.getCount()-1){
                            mChannelTypeView.setSelection(ALL_CHANNEL);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        startTimeout(NAV_TIMEOUT_300);
                        if(mChannelTypeView.getSelectedItemPosition() == ALL_CHANNEL){
                            mChannelTypeView.setSelection(mChannelTypeView.getCount()-1);
                             startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyMap.KEYCODE_BACK:
                        mChannelTypeView.setVisibility(View.GONE);
                        mTitleText.setText(mContext.getResources().getString(R.string.nav_select_more));
                        mChannelMoreView.setVisibility(View.VISIBLE);
                        return true;
                case KeyMap.KEYCODE_MTKIR_BLUE:
                case KeyMap.KEYCODE_MTKIR_YELLOW:
                case KeyMap.KEYCODE_MTKIR_RED:
                case KeyMap.KEYCODE_MTKIR_GREEN:
                    return true;
                default:
                    break;
                    }
                }
                break;
            case R.id.nav_channel_selection:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimeout(NAV_TIMEOUT_300);
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            List<TIFChannelInfo> channellist=mChannelAdapter.getChannellist();
                            if (mSelectionChannel == null){
                                return true;
                            }
                            addEraseChannelId = mSelectionChannel.mInternalProviderFlag3;
                            int index = mChannelSelectionView.getSelectedItemPosition();
                            mSelectionChannel.mMtkTvChannelInfo = FavChannelManager.getInstance(mContext).favAddOrEraseIntoIndex(mSelectionChannel.mInternalProviderFlag3,index);
                            selectionAdapter.updateData(mSelectionChannel);
                            if (isFavTypeState && addEraseChannelId == commonIntegration.getCurrentChannelId() && !isFavInCurrentFavType(mSelectionChannel)){
                                changeChannelToFirstFav();
                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelSelectionView.getSelectedItemPosition());
                            if(mChannelSelectionView.getSelectedItemPosition() == mChannelSelectionView.getChildCount() - 1){
                                mChannelSelectionView.setSelection(0);
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            if(mChannelSelectionView.getSelectedItemPosition() == 0){
                                mChannelSelectionView.setSelection(mChannelSelectionView.getChildCount() - 1);
                                return true;
                            }
                            break;
                        case KeyMap.KEYCODE_BACK:
                            mChannelSelectionView.setVisibility(View.GONE);
                            setChannelListTitle();
                            mChannelListView.setVisibility(View.VISIBLE);
                            mChannelListTipView.setVisibility(View.VISIBLE);
                            return true;
                        case KeyMap.KEYCODE_MTKIR_BLUE:
                        case KeyMap.KEYCODE_MTKIR_YELLOW:
                        case KeyMap.KEYCODE_MTKIR_RED:
                        case KeyMap.KEYCODE_MTKIR_GREEN:
                            return true;
                        default:
                            break;
                    }
                }
                break;
            case R.id.nav_channel_satellitetypeview:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimeout(NAV_TIMEOUT_300);
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        mCurrentSatelliteRecordId = mSatelliteListinfo.get(mChannelSatelliteView.getSelectedItemPosition()).getSatlRecId();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentSatelliteRecordId>>>" + mCurrentSatelliteRecordId);
                        mSaveValue.saveValue(SATELLITE_RECORDID,mCurrentSatelliteRecordId);
                        mChannelListView.setAdapter(null);
                        mChannelAdapter = null;
                        mChannelListView.setVisibility(View.VISIBLE);
                        mChannelSatelliteView.setVisibility(View.GONE);
                        if (mIsTifFunction) {
                            resetChListByTIF();
                        } else {
                            resetChList();
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelSatelliteView.getSelectedItemPosition());
                        if(mChannelSatelliteView.getSelectedItemPosition() == mChannelSatelliteView.getChildCount() - 1){
                            mChannelSatelliteView.setSelection(0);
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if(mChannelSatelliteView.getSelectedItemPosition() == 0){
                            mChannelSatelliteView.setSelection(mChannelSatelliteView.getChildCount() - 1);
                            return true;
                        }
                        break;
                case KeyMap.KEYCODE_MTKIR_BLUE:
                case KeyMap.KEYCODE_MTKIR_YELLOW:
                case KeyMap.KEYCODE_MTKIR_RED:
                case KeyMap.KEYCODE_MTKIR_GREEN:
                    return true;
                default:
                    break;
                    }
                }
                break;

                case R.id.nav_channel_satellitetypesecondview:
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nav_channel_satellitetypesecondview !!!!!");
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        startTimeout(NAV_TIMEOUT_300);
                        switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            if(CURRENT_CHANNEL_SECOND_TYPE == mChannelSatelliteSecondView.getSelectedItemPosition() &&
                                    mChannelSatelliteSecondView.getSelectedItemPosition() != 0){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position == CURRENT_CHANNEL_SECOND_TYPE not deal");
                                return true;
                            }
                            chooseType(TYPE_CHANGE_SATELLITE);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelTypeView.getSelectedItemPosition());
                            if(mChannelSatelliteSecondView.getSelectedItemPosition() == (typesSecond.size()-1)){
                                mChannelSatelliteSecondView.setSelection(ALL_CHANNEL);
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            if(mChannelSatelliteSecondView.getSelectedItemPosition() == ALL_CHANNEL){
                                mChannelSatelliteSecondView.setSelection(typesSecond.size()-1);
                                return true;
                            }
                            break;
                    case KeyMap.KEYCODE_MTKIR_BLUE:
                    case KeyMap.KEYCODE_MTKIR_YELLOW:
                    case KeyMap.KEYCODE_MTKIR_RED:
                    case KeyMap.KEYCODE_MTKIR_GREEN:
                        return true;
                    default:
                        break;
                        }
                    }
                    break;

            case R.id.nav_channel_fav_view:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimeout(NAV_TIMEOUT_300);
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(TextToSpeechUtil.isTTSEnabled(mContext)){
                            return true; // select channel not support focus when tts is true
                        }

                        if(CURRENT_CHANNEL_FAV == mChannelTypeFavView.getSelectedItemPosition() && CURRENT_CHANNEL_FAV != 0){
                            chooseFavChannel();
                        }else{
                            chooseType(TYPE_CHANGE_FAVORITE);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelTypeFavView.getSelectedItemPosition());
                        if(mChannelTypeFavView.getSelectedItemPosition() == mChannelTypeFavView.getChildCount() - 1){
                            mChannelTypeFavView.setSelection(0);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if(mChannelTypeFavView.getSelectedItemPosition() == 0){
                            mChannelTypeFavView.setSelection(mChannelTypeFavView.getChildCount() - 1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyMap.KEYCODE_BACK:
                        mChannelTypeFavView.setVisibility(View.GONE);
                        mTitleText.setText(mContext.getResources().getString(R.string.nav_select_type));
                        mChannelTypeView.setVisibility(View.VISIBLE);
                        return true;
                case KeyMap.KEYCODE_MTKIR_BLUE:
                case KeyMap.KEYCODE_MTKIR_YELLOW:
                case KeyMap.KEYCODE_MTKIR_RED:
                case KeyMap.KEYCODE_MTKIR_GREEN:
                    return true;
                default:
                    break;
                    }
                }
                break;

            case R.id.nav_channel_sort:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimeout(NAV_TIMEOUT_300);
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(TextToSpeechUtil.isTTSEnabled(mContext)){
                            return true; // select channel not support focus when tts is true
                       }
                        if(CURRENT_CHANNEL_SORT == mChannelSortView.getSelectedItemPosition()){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_CENTER position == CURRENT_CHANNEL_SORT not deal");
                            return true;
                        }
                        mTIFChannelManager.findChanelsForlist("",mCurMask,mCurVal);
                        CURRENT_CHANNEL_SORT = mChannelSortView.getSelectedItemPosition();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_CHANNEL_SORT>>>" + CURRENT_CHANNEL_SORT);
                        mSaveValue.saveValue(CH_SORT,CURRENT_CHANNEL_SORT);
                        mChannelListView.setAdapter(null);
                        mChannelAdapter = null;
                        mChannelListView.setVisibility(View.VISIBLE);
                        mChannelSortView.setVisibility(View.GONE);
                        mChannelTypeFavView.setVisibility(View.GONE);
                        mChannelMoreView.setVisibility(View.GONE);
                        mTIFChannelManager.setCurrentChannelSort(CURRENT_CHANNEL_SORT);
                        if (mIsTifFunction) {
                            resetChListByTIF();
                        } else {
                            resetChList();
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelSortView.getSelectedItemPosition());
                        if(mChannelSortView.getSelectedItemPosition() == mChannelSortView.getChildCount() - 1){
                            mChannelSortView.setSelection(0);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if(mChannelSortView.getSelectedItemPosition() == 0){
                            mChannelSortView.setSelection(mChannelSortView.getChildCount() - 1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyMap.KEYCODE_BACK:
                        mChannelSortView.setVisibility(View.GONE);
                        mTitleText.setText(mContext.getResources().getString(R.string.nav_channel_list_options));
                        mChannelOptionView.setVisibility(View.VISIBLE);
                        return true;
                case KeyMap.KEYCODE_MTKIR_BLUE:
                case KeyMap.KEYCODE_MTKIR_YELLOW:
                case KeyMap.KEYCODE_MTKIR_RED:
                case KeyMap.KEYCODE_MTKIR_GREEN:
                    return true;
                default:
                    break;
                    }
                }
                break ;
            case R.id.nav_channel_list_option:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startTimeout(NAV_TIMEOUT_300);
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN nav_channel_list_option = ");
                        // isn't fav or channel num > 1, can move
                        if( (mChannelAdapter != null && mChannelAdapter.getCount() >1) || !isFavTypeState  ){
                            if(mChannelOptionView.getSelectedItemPosition() == (mChannelOptionView.getChildCount() - 1) ){
                                mChannelOptionView.setSelection(0);
                                startTimeout(NAV_TIMEOUT_300);
                                return true;
                            }
                            break;
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_UP nav_channel_list_option = ");
                        // isn't fav or channel num > 1, can move
                        if( (mChannelAdapter != null && mChannelAdapter.getCount() >1) || !isFavTypeState){
                            if(mChannelOptionView.getSelectedItemPosition() == 0){
                                mChannelOptionView.setSelection(mChannelOptionView.getChildCount() - 1);
                                startTimeout(NAV_TIMEOUT_300);
                                return true;
                            }
                            break;
                        }
                        return true;
                    case KeyMap.KEYCODE_BACK:
                          mChannelOptionView.setVisibility(View.GONE);
                          mTitleText.setText(mContext.getResources().getString(R.string.nav_select_more));
                          mChannelMoreView.setVisibility(View.VISIBLE);
                    return true;
                    case KeyMap.KEYCODE_MTKIR_BLUE:
                    case KeyMap.KEYCODE_MTKIR_YELLOW:
                    case KeyMap.KEYCODE_MTKIR_RED:
                    case KeyMap.KEYCODE_MTKIR_GREEN:
                        return true;
                    default:
                        return false;
                    }
                }
                break;
            case R.id.nav_channel_more:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," yiran nav_channel_more keyCode = "+keyCode);

                    startTimeout(NAV_TIMEOUT_10);
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if(TextToSpeechUtil.isTTSEnabled(mContext)){
                            return true; // select channel not support focus when tts is true
                       }
                        CURRENT_MORE_TYPE = mChannelMoreView.getSelectedItemPosition();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CURRENT_MORE_TYPE>>>" + CURRENT_MORE_TYPE);
                        if (CURRENT_MORE_TYPE == 1){
                            mTitleText.setText(mContext.getResources().getString(R.string.nav_channel_list_options));
                        }
                        return false;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"KEYCODE_DPAD_DOWN position = "+mChannelMoreView.getSelectedItemPosition());
                        if(mChannelMoreView.getSelectedItemPosition() == mChannelMoreView.getChildCount() - 1){
                            mChannelMoreView.setSelection(0);
                            startTimeout(NAV_TIMEOUT_10);
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if(mChannelMoreView.getSelectedItemPosition() == 0){
                            mChannelMoreView.setSelection(mChannelMoreView.getChildCount() - 1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        break;
                    case KeyMap.KEYCODE_BACK:
                        mChannelMoreView.setVisibility(View.GONE);
                        setChannelListTitle();
                        mChannelListView.setVisibility(View.VISIBLE);
                        mChannelListTipView.setVisibility(View.VISIBLE);
                  return true;
                case KeyMap.KEYCODE_MTKIR_BLUE:
                case KeyMap.KEYCODE_MTKIR_YELLOW:
                case KeyMap.KEYCODE_MTKIR_RED:
                case KeyMap.KEYCODE_MTKIR_GREEN:
                    return true;
                default:
                    break;
                    }
                }
                default:
                break;
            }
            return false;
        }
    }

    private void resetChannelListMoveViw() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetFavouriteListMoveViw");
        if(mChannelAdapter != null && mChannelAdapter.getCount() >1){
            View favView =(View) mChannelListView.getSelectedView();
            mChannelAdapter.updateView(favView, -1,View.INVISIBLE);

        }
      }
    private void selectTifChannel(int keyCode, TIFChannelInfo selectedChannel) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
            mChannelDetailLayout.setVisibility(View.INVISIBLE);
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
                if(selectedChannel.mMtkTvChannelInfo != null){
                    final MtkTvChannelInfoBase apiSelectedChannel = selectedChannel.mMtkTvChannelInfo;
                    if (!apiSelectedChannel.equals(commonIntegration.getCurChInfo())) {
                        if (DvrManager.getInstance() != null
                                && DvrManager.getInstance().pvrIsRecording()) {
                            String srctype = DvrManager.getInstance().getController().getSrcType();
                            if (!"TV".equals(srctype)
                                    && !InputSourceManager.getInstance().getConflictSourceList()
                               .contains(srctype)
                               && !("1".equals(selectedChannel.mType))) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-1,ID:" + apiSelectedChannel.getChannelId());
                                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-1,Name:" + apiSelectedChannel.getServiceName());
                                mTIFChannelManager.selectChannelByTIFInfo(selectedChannel);
                                dismiss();
                            } else {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TYPE_Record " + apiSelectedChannel.getChannelId());
                                showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,keyCode,DvrDialog.TYPE_Record,apiSelectedChannel.getChannelId());
                           }
                         }else if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START )){
                             com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mContext:" + mContext);
                             com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TIMESHIFT_START " + apiSelectedChannel.getChannelId()+" TIMESHIFT_START "+SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START ));
                             showDialogForPVRAndTShift( DvrDialog.TYPE_Confirm_From_ChannelList,keyCode,DvrDialog.TYPE_Timeshift,apiSelectedChannel.getChannelId());
                         } else {
                             com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-2,ID:" + apiSelectedChannel.getChannelId());
                             com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-2,Name:" + apiSelectedChannel.getServiceName());
                             mTIFChannelManager.selectChannelByTIFInfo(selectedChannel);
                             dismiss();
                         }
                    }
             }else{
                 TIFChannelInfo currentTIFChannelInfo=  mTIFChannelManager.getCurrChannelInfo();
                 if ((currentTIFChannelInfo !=null && selectedChannel.mId != currentTIFChannelInfo.mId)) {
                     com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "is3rdTVSource channelID:-2,ID:" + selectedChannel.mId);
                     com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "is3rdTVSource channelID:-2,Name:" + selectedChannel.mDisplayName);
                     boolean changeSuccess = mTIFChannelManager.selectChannelByTIFInfo(selectedChannel);
                     if (changeSuccess) {
                         dismiss();
                     }
                 }else{
                     com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "is3rdTVSource channelis null ");
                 }

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
            mChannelDetailLayout.setVisibility(View.INVISIBLE);
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
            default:
                break;
        }
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
         if(((keyCode == KeyMap.KEYCODE_DPAD_CENTER && !isLock()) || keyCode == KeyMap.KEYCODE_YEN) && isChannel()){
        	 if(isShowing()){
        		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " Channel list dialog isKeyHandler dialog is show.");
        		return false;
        	 }
             if((commonIntegration.isTVSourceSeparation() && commonIntegration.isCurrentSourceATVforEuPA()) ||
                     (commonIntegration.isCNRegion() && commonIntegration.isCurrentSourceATV())){
                 mCurMask = commonIntegration.CH_LIST_ANALOG_MASK;
                 mCurVal = commonIntegration.CH_LIST_ANALOG_VAL;
                 mCurCategories = -1;
                 setFavTypeState(false);
                 types= new String[1];
                 CURRENT_CHANNEL_MODE = CHANNEL_LIST;
                 TIFFunctionUtil.setmCurCategories(mCurCategories);
             }else{
                 initMaskAndSatellites();

             }
            //   mChannelListView.setAdapter(null);
             //  init();
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Channel list dialog isKeyHandler return true");
             return true;
         }
         if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
             if(!TurnkeyUiMainActivity.getInstance().isUKCountry){
             BannerView bannerView = (BannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
             if (bannerView != null && !bannerView.isVisible()) {
                 bannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
             }
             }
         }
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Channel list dialog isKeyHandler return false");
         return false;
    }
    //get channel list for homechannel
    public List<TIFChannelInfo> getChannelListForHomeChannels(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType()");
        initMaskAndSatellites();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType() not fav");
        return mTIFChannelManager.getTIFPreOrNextChannelList(-1, false, false,
                           10000, CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);

    }
    //get channel list for homechannel
    public List<TIFChannelInfo> getFavChannelListForHomeChannels(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType()");
        initMaskAndSatellites();
        if(isFavTypeState){
           int preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
           if(preNum > 0){
               List<MtkTvChannelInfoBase> tempApiChList = commonIntegration.getChannelListByMaskFilter(
                       -1, MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,preNum , mCurMask, mCurVal);
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType() tempApiChList.size() "+tempApiChList.size());
               return mTIFChannelManager.getTIFChannelList(tempApiChList);
           }else{
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() tempApiChList.size() 0");
               return null;
           }

        }else{
            int preNum = commonIntegration.getFavouriteChannelCount(commonIntegration.favMasks);
            if(preNum > 0){
                List<MtkTvChannelInfoBase> tempApiChList = commonIntegration.getChannelList(
                        -1,0, preNum , commonIntegration.favMasks);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType() tempApiChList.size() "+tempApiChList.size());
                return mTIFChannelManager.getTIFChannelList(tempApiChList);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() tempApiChList.size() 0");
                return null;
            }
        }
    }
  //get channel list for epg
    public List<TIFChannelInfo> getChannelListForEPG(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForEPG()");
        initMaskAndSatellites();
        if(isFavTypeState){
           int preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
           if(preNum > 0){
               List<MtkTvChannelInfoBase> tempApiChList = commonIntegration.getFavoriteListByFilter(
                       mCurMask, 0,false, 0, preNum,CURRENT_CHANNEL_FAV);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType() tempApiChList.size() "+tempApiChList.size());
            return mTIFChannelManager.getTIFChannelListForFavIndexForEPG(tempApiChList, CURRENT_CHANNEL_FAV);
           }else{
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() tempApiChList.size() 0");
               return null;
           }

        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForEPG() not fav");
           return mTIFChannelManager.queryChanelListAllForEPG(mCurMask, mCurVal);
        }

    }

    //get channel list for epg
    public List<TIFChannelInfo> getChannelListForType(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType()");
        initMaskAndSatellites();
        if(isFavTypeState){
           int preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
           if(preNum > 0){
               List<MtkTvChannelInfoBase> tempApiChList = commonIntegration.getFavoriteListByFilter(
                       mCurMask, 0,false, 0, preNum,CURRENT_CHANNEL_FAV);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType() tempApiChList.size() "+ (tempApiChList== null? tempApiChList:tempApiChList.size()));
            return mTIFChannelManager.getTIFChannelListForFavIndex(tempApiChList, CURRENT_CHANNEL_FAV);
           }else{
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() tempApiChList.size() 0");
               return null;
           }

        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForType() not fav");
           return mTIFChannelManager.queryChanelListAll(mCurMask, mCurVal);
        }

    }

    public List<TIFChannelInfo> getChannelListForTypeFav(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav()");
        initMaskAndSatellites();
        if(isFavTypeState){
           int preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
           if(preNum > 0){
               List<MtkTvChannelInfoBase> tempApiChList = commonIntegration.getFavoriteListByFilter(
                       mCurMask, 0,false, 0, preNum,CURRENT_CHANNEL_FAV);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() tempApiChList.size() "+ (tempApiChList==null?tempApiChList:tempApiChList.size()));
            return mTIFChannelManager.getTIFChannelListForFavIndex(tempApiChList, CURRENT_CHANNEL_FAV);
           }else{
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() tempApiChList.size() 0");
               return null;
           }

        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getChannelListForTypeFav() not fav");
           return mTIFChannelManager.queryChanelListAll(commonIntegration.CH_LIST_MASK, commonIntegration.CH_LIST_VAL);
        }

    }
    public boolean isFavStateInChannelList(){
        initMaskAndSatellites();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFavStateInChannelList() isFavTypeState "+isFavTypeState);
        return isFavTypeState;
    }
    boolean isSignal(){
        return true;
    }

    class ChannelAdapter extends BaseAdapter{
        static final String TAG = "ChannelListDialog.ChannelAdapter";
        private Context mContext;
        private LayoutInflater mInflater;
        private List<TIFChannelInfo> mCurrentTifChannelList;
        private int favlistMovepostion = -1;
        int icon_w ;
        int icon_h ;
        public ChannelAdapter(Context context, List<TIFChannelInfo> currentTifChannelList) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            this.mCurrentTifChannelList = currentTifChannelList;
            icon_w = (int)mContext.getResources().getDimension(R.dimen.nav_chanenl_list_item_icon_widgh);
            icon_h = (int)mContext.getResources().getDimension(R.dimen.nav_chanenl_list_item_icon_widgh);
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

        public void setFavlistMovepostion(int favlistMovepostion) {
            this.favlistMovepostion=favlistMovepostion;
          }

        public long getItemId(int position) {
            return position;
        }

        public void updateData(List<TIFChannelInfo> currentChannelList) {
            this.mCurrentTifChannelList = currentChannelList;
            notifyDataSetChanged();
        }
        public void updateView(View view ,int index,int visible) {
            if(view == null){
              return;
            }
            this.favlistMovepostion= index;
            ViewHolder hodler=(ViewHolder) view.getTag();
            hodler.mFavListMove.setVisibility(visible);
          }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder hodler;
            TIFChannelInfo mCurrentChannel;
            if (convertView == null) {
               if(commonIntegration.isUSRegion() || commonIntegration.isSARegion()) {
                   convertView = mInflater.inflate(R.layout.nav_channel_item_for_us_sa, null);
               }else{
                   convertView = mInflater.inflate(R.layout.nav_channel_item, null);
               }
                hodler = new ViewHolder();
                hodler.mIcon=(ImageView) convertView.findViewById(R.id.nav_chanel_list_item_icon);
                hodler.mChannelNumberTextView = (TextView) convertView.findViewById(R.id.nav_channel_list_item_NumberTV);
                hodler.mChannelNameTextView = (TextView) convertView.findViewById(R.id.nav_channel_list_item_NameTV);
                hodler.mFavListMove= (ImageView) convertView.findViewById(R.id.nav_channel_list_move_icon);
                convertView.setTag(hodler);
            } else {
                hodler = (ViewHolder) convertView.getTag();
            }
            hodler.mIcon.setVisibility(View.INVISIBLE);
            mCurrentChannel = mCurrentTifChannelList.get(position);
            if (mIsTifFunction) {
                if(!commonIntegration.is3rdTVSource() || mCurrentChannel.mMtkTvChannelInfo !=null){
                    boolean isfav = isFavInFavType(mCurrentChannel);
                    if (mCurrentChannel.mMtkTvChannelInfo !=null && isfav){
                        Drawable favIcon = mContext.getResources().getDrawable(R.drawable.nav_fav_un_selected);
                        favIcon.setBounds(0, 0, icon_w, icon_h);
                        hodler.mIcon.setImageDrawable(favIcon);
                        hodler.mIcon.setVisibility(View.VISIBLE);
                    }else if(mCurrentChannel.mMtkTvChannelInfo !=null && mCurrentChannel.mMtkTvChannelInfo.isScrambled()){
                      Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.channel_screamable_icon);
                      radioIcon.setBounds(0, 0, icon_w, icon_h);
                      hodler.mIcon.setImageDrawable(radioIcon);
                      hodler.mIcon.setVisibility(View.VISIBLE);
                  }else{
                      if (mCurrentChannel.mMtkTvChannelInfo !=null && mCurrentChannel.mMtkTvChannelInfo.isRadioService()
                              && MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
                          Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.radio_channel_icon);
                          radioIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(radioIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else if(mCurrentChannel.mMtkTvChannelInfo !=null && mCurrentChannel.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo){
                          MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo)mCurrentChannel.mMtkTvChannelInfo;
                          String bmpPath = commonIntegration.getISDBChannelLogo(tmpIsdb);
                          //             hodler.mIcon.setCompoundDrawablesWithIntrinsicBounds(Drawable.createFromPath(bmpPath),null,null,null);
                          hodler.mIcon.setImageDrawable(Drawable.createFromPath(bmpPath));
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else if(mCurrentChannel.mMtkTvChannelInfo !=null && mCurrentChannel.mMtkTvChannelInfo instanceof MtkTvAnalogChannelInfo ){
                          if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
                                  || MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU
                                  || MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US) {
                              Drawable analogIcon = mContext.getResources().getDrawable(R.drawable.channel_atv_icon);
                              analogIcon.setBounds(0, 0, icon_w, icon_h);
                              hodler.mIcon.setImageDrawable(analogIcon);
                              hodler.mIcon.setVisibility(View.VISIBLE);
                          } else {
                              hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                          }
                      }else if(mCurrentChannel !=null && mCurrentChannel.isInterActive()){
                          Drawable dataIcon = mContext.getResources().getDrawable(R.drawable.channel_list_data);
                          dataIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(dataIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }else{
                          Drawable tvIcon = mContext.getResources().getDrawable(R.drawable.channel_list_tv);
                          tvIcon.setBounds(0, 0, icon_w, icon_h);
                          hodler.mIcon.setImageDrawable(tvIcon);
                          hodler.mIcon.setVisibility(View.VISIBLE);
                      }
                  }

                }else{
                    hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                hodler.mChannelNumberTextView.setText(mCurrentChannel.mDisplayNumber);
                hodler.mChannelNameTextView.setText(mCurrentChannel.mDisplayName);
            } else {
                if (mCurrentChannel.mMtkTvChannelInfo.isRadioService()
                        && MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
                    Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.epg_radio_channel_icon);
                    radioIcon.setBounds(0, 0, radioIcon.getMinimumWidth(), radioIcon.getMinimumWidth());
                    hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(radioIcon, null, null, null);
                    hodler.mChannelNumberTextView.setText("" + mCurrentChannel.mMtkTvChannelInfo.getChannelNumber());
                }else if(mCurrentChannel.mMtkTvChannelInfo instanceof MtkTvATSCChannelInfo){
                    MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo)mCurrentChannel.mMtkTvChannelInfo;
                    hodler.mChannelNumberTextView.setText(tmpAtsc.getMajorNum()+"-" +tmpAtsc.getMinorNum());
                    hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }else if(mCurrentChannel.mMtkTvChannelInfo instanceof MtkTvISDBChannelInfo){
                    MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo)mCurrentChannel.mMtkTvChannelInfo;
                    hodler.mChannelNumberTextView.setText(tmpIsdb.getMajorNum()+"-"+tmpIsdb.getMinorNum());
                    String bmpPath = commonIntegration.getISDBChannelLogo(tmpIsdb);
                    hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(Drawable.createFromPath(bmpPath),null,null,null);

                }else if(mCurrentChannel.mMtkTvChannelInfo instanceof MtkTvAnalogChannelInfo ){
                    if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
                            || MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
                        Drawable analogIcon = mContext.getResources().getDrawable(R.drawable.epg_channel_icon);
                        analogIcon.setBounds(0, 0, analogIcon.getMinimumWidth(), analogIcon.getMinimumWidth());
                        hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(analogIcon, null, null, null);
                    } else {
                        hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }
                    hodler.mChannelNumberTextView.setText(""+ mCurrentChannel.mMtkTvChannelInfo.getChannelNumber());
                }else{
                    hodler.mChannelNumberTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    hodler.mChannelNumberTextView.setText(""+ mCurrentChannel.mMtkTvChannelInfo.getChannelNumber());
                }
                if(mCurrentChannel.mMtkTvChannelInfo instanceof MtkTvDvbChannelInfo){
                    MtkTvDvbChannelInfo tmpdvb = (MtkTvDvbChannelInfo)mCurrentChannel.mMtkTvChannelInfo;
                    String name = tmpdvb.getShortName();
                    if(name == null || name.trim().length() <= 0  ){
                        name = tmpdvb.getServiceName();
                        if (name == null) {
                            name = "";
                        }
                    }
                    hodler.mChannelNameTextView.setText(name);
                }else{
                    hodler.mChannelNameTextView.setText(mCurrentChannel.mMtkTvChannelInfo.getServiceName());
                }
            }
            if(favlistMovepostion != -1 && favlistMovepostion == position){
                hodler.mFavListMove.setVisibility(View.VISIBLE);
             }else{
                 hodler.mFavListMove.setVisibility(View.INVISIBLE);
             }
            return convertView;
        }

         class ViewHolder {
            ImageView mIcon;
            TextView mChannelNumberTextView;
            TextView mChannelNameTextView;
            ImageView mFavListMove;
        }

    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
        if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
            if (isVisible()) {
                mHandler.removeMessages(MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
                if (mSendKeyCode == KeyMap.KEYCODE_MTKIR_STOP || mSendKeyCode == KeyMap.KEYCODE_MTKIR_CHUP
                        || mSendKeyCode == KeyMap.KEYCODE_MTKIR_CHDN
                        || mSendKeyCode == KeyMap.KEYCODE_MTKIR_PRECH
                        || ComponentsManager.getNativeActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_HBBTV) {
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
                            if (ComponentsManager.getNativeActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_HBBTV){
                                mSendKeyCode = -1;
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
            if(chanelUpdateMsg == 1 && channelNewSvcAdded > 0 && CommonIntegration.getInstance().isCurrentSourceTv()) {
                if (DestroyApp.isCurActivityTkuiMainActivity() && mContext instanceof TurnkeyUiMainActivity){
                    addChannelsDialog();
                    config.setConfigValue(MenuConfigManager.SETUP_CHANNEL_NEW_SVC_ADDED,0);
                }
            }
        }
    }

    public void addChannelsDialog(){
        final ConfirmDialog liveTVDialog = ConfirmDialog.getInstance(mContext);
        liveTVDialog.showBgScanAddedDialog();
    }

    boolean isFindingForChannelList =false;
    // deal input return result
    public void onActivityResult(Intent data) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult>>>");
         if (data != null) {
             String value = data.getStringExtra("value");
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult value:" + value);
             isFindingForChannelList =true;
             List<TIFChannelInfo> mFindChannels = mTIFChannelManager.findChanelsForlist(value,mCurMask,mCurVal);
             if(mFindChannels == null || mFindChannels.isEmpty()){
                 Toast.makeText(mContext,  mContext.getResources().getString(R.string.nav_select_find_no_channel) , Toast.LENGTH_SHORT).show();
             }
             if (mIsTifFunction) {
                 resetChListByTIF();
             } else {
                 resetChList();
             }
         }
     }

    public void setFavTypeState(boolean favState){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setFavTypeState:"+favState);
        isFavTypeState = favState;
        commonIntegration.setFavTypeState(favState);
    }
    public void setFavIndexFromFavDialog(int index){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setFavIndexFromFavDialog index:"+index);
        CURRENT_CHANNEL_FAV = index;
        resetFavMask();
    }

    @Override
    public boolean deinitView(){
        super.deinitView();
        mHandlerThead.quit();
        commonIntegration.setChannelChangedListener(null);
        mHandler.removeCallbacksAndMessages(null);
        if(MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
            TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_SVCTX_NOTIFY, mHandler);
        }
        return false;
    }

    public void channelListRearrangeFav(){
        mTIFChannelManager.channelListRearrangeFav();
    }

    public void resetVariable(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start resetVariable svl:"+CommonIntegration.getInstance().getSvl());
        setFavTypeState(false);
        SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
        SaveValue.getInstance(mContext).saveValue(CommonIntegration.CH_TYPE_SECOND,-1);
        CURRENT_CHANNEL_TYPE = ALL_CHANNEL;
        CURRENT_CHANNEL_SECOND_TYPE = -1;
        //CURRENT_CHANNEL_SECOND_NAME = "";
        mCurCategories = -1;
        CURRENT_CHANNEL_FAV = ALL_CHANNEL;
        favChannelManager.setFavoriteType(CURRENT_CHANNEL_FAV);
    }

    public void changeChannelToFirstFav(){
        if (isFavTypeState){
            int preNum = commonIntegration.getFavouriteChannelCount(mCurMask);
            List<MtkTvChannelInfoBase> tempApiChList = commonIntegration.getFavoriteListByFilter(
                    mCurMask, 0,false, 0, preNum > CHANNEL_LIST_PAGE_MAX ? CHANNEL_LIST_PAGE_MAX : preNum,CURRENT_CHANNEL_FAV);
            List<TIFChannelInfo> tempList = mTIFChannelManager.getTIFChannelListForFavIndex(tempApiChList, CURRENT_CHANNEL_FAV);
            if (tempList.size() > 0){
                mTIFChannelManager.selectChannelByTIFInfo(tempList.get(0));
                dismiss();
            }
        }
    }

    class FavSelectionAdapter extends BaseAdapter{
        static final String TAG = "ChannelListDialog.FavSelectionAdapter";
        private Context mContext;
        private LayoutInflater mInflater;
        private List<String> mfavList;
        private TIFChannelInfo mChannel;
        private Drawable mCheckBoxSelectedIcon;
        private Drawable mCheckBoxUnSelectedIcon;
        int icon_w ;
        int icon_h ;
        public FavSelectionAdapter(Context context,List<String> mConflictList,TIFChannelInfo mCurrentChannel) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            this.mfavList = mConflictList;
            this.mChannel = mCurrentChannel;
            icon_w = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_widgh);
            icon_h = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_widgh);

            mCheckBoxSelectedIcon = mContext.getResources().getDrawable(
                    R.drawable.nav_channel_info_checked);
            int iconW = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_widgh);
            int iconH = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_height);
            mCheckBoxSelectedIcon.setBounds(0, 0,iconW,iconH);
            mCheckBoxUnSelectedIcon = mContext.getResources().getDrawable(R.drawable.nav_channel_info_unchecked);
            mCheckBoxUnSelectedIcon.setBounds(0, 0,iconH,iconH);
        }

        public void updateData(TIFChannelInfo mCurrentChannel) {
            this.mChannel = mCurrentChannel;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mfavList.size();
        }

        @Override
        public String getItem(int position) {
            return mfavList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder hodler;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.nav_channel_fav_selection_item, null);
                hodler = new ViewHolder();
                hodler.mTextView = (TextView) convertView
                        .findViewById(R.id.nav_channl_fav_selection_text);
                hodler.mCheck = (ImageView) convertView
                        .findViewById(R.id.nav_channl_fav_selection_check);
                convertView.setTag(hodler);
            } else {
                hodler = (ViewHolder) convertView.getTag();
            }
            hodler.mTextView.setText(getItem(position));
            boolean isfav = false;
            switch (position){
            case 0:
                if (mChannel.mMtkTvChannelInfo !=null){
                    isfav = mChannel.mMtkTvChannelInfo.isDigitalFavorites1Service();
                }
                break;
                case 1:
                    if (mChannel.mMtkTvChannelInfo !=null){
                        isfav = mChannel.mMtkTvChannelInfo.isDigitalFavorites2Service();
                    }
                    break;
                case 2:
                    if (mChannel.mMtkTvChannelInfo !=null){
                        isfav = mChannel.mMtkTvChannelInfo.isDigitalFavorites3Service();
                    }
                    break;
                case 3:
                    if (mChannel.mMtkTvChannelInfo !=null){
                        isfav = mChannel.mMtkTvChannelInfo.isDigitalFavorites4Service();
                    }
                    break;
                default:
                    if (mChannel.mMtkTvChannelInfo !=null){
                        isfav = mChannel.mMtkTvChannelInfo.isDigitalFavorites1Service();
                    }
                    break;
            }
            if (isfav){
                hodler.mCheck.setImageDrawable(mCheckBoxSelectedIcon);
            }else{
                hodler.mCheck.setImageDrawable(mCheckBoxUnSelectedIcon);
            }
            return convertView;
        }

        private class ViewHolder {
            ImageView mCheck;
            TextView mTextView;
        }
    }
}
