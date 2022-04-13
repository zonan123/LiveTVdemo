
package com.mediatek.wwtv.tvcenter.nav.fav;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

public class FavoriteListDialog extends NavBasicDialog implements
    OnDismissListener, ComponentStatusListener.ICStatusListener {

  private static final String TAG = "ShowFavoriteChannelListView";
  private static ListView mFavoriteListView;
  private ListView mFavouriteTypeView;
  private View mFavouriteListPageUpDownView;
  private View mFavouriteListTipView;
  private boolean isFavListMove = false;
  MtkTvChannelInfoBase favmove =null;
  int indexTo = 0;
  MtkTvChannelInfoBase favmovetag =null;
  
  private static final int FAVOURITE_1 = 0;
  private static final int FAVOURITE_2 = 1;
  private static final int FAVOURITE_3 = 2;
  private static final int FAVOURITE_4 = 3;
  private int CURRENT_FAVOURITE_TYPE = FAVOURITE_1;
  private String[] types;
  private String mTitlePre;
  private final int[] favMask = new int[] {
      MtkTvChCommonBase.SB_VNET_FAVORITE1, MtkTvChCommonBase.SB_VNET_FAVORITE2,
      MtkTvChCommonBase.SB_VNET_FAVORITE3, MtkTvChCommonBase.SB_VNET_FAVORITE4
  };
  private final FavChannelManager favChannelManager;
  static final String SPNAME = "CHMODE";
  private static final String FAVOURITE_TYPE = "favouriteType";
  private SaveValue mSaveValue;
  private static final int SET_SELECTION_INDEX = 0x1001;
  private static final int TYPE_CHANGECHANNEL_ENTER = 0x1005;
  private static final int TYPE_RESET_FAVOURITELIST = 0x1006;
  private int mLastSelection = 0;
  private final List<Integer> mChannelIdList;
  private final CommonIntegration commonIntegration;
  private boolean hasNextPage = false;
  private int preNum = 0;
  int CHANNEL_LIST_PAGE_MAX = 10;
  private boolean canContinueChangeChannel = true;
  private ChannelAdapter mChannelAdapter;
  private TextView mTitleTextView;
  TextView mNavPageUpTextView;
  TextView mNavFavoriteSelectTextView;
  TextView mNavExitTextView;
  TableRow mPageView;
  TextView mYellowView;
  ImageView mYellowImg;
  String mFavoriteChannelListTitle;
  String mNavFavoriteSelectString;
  String mNavExitString;
  ImageView navFavType;
  public FavoriteListDialog(Context context, int theme) {

    super(context, theme);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Constructor!");
    mContext = context;
    componentID = NAV_COMP_ID_FAV_LIST;
    commonIntegration = CommonIntegration.getInstance();
    favChannelManager = FavChannelManager.getInstance(mContext);
    mChannelIdList = new ArrayList<Integer>();
    mSaveValue = SaveValue.getInstance(context);
    CURRENT_FAVOURITE_TYPE =mSaveValue.readValue(FAVOURITE_TYPE, FAVOURITE_1);
    setFavIndexToChannelList(CURRENT_FAVOURITE_TYPE);
    ComponentStatusListener lister = ComponentStatusListener.getInstance();
    lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
    lister.addListener(ComponentStatusListener.NAV_COMPONENT_SHOW, this);
    TypedValue sca = new TypedValue();
    mContext.getResources().getValue(R.dimen.nav_channellist_page_max,sca ,true);
    CHANNEL_LIST_PAGE_MAX  =(int) sca.getFloat();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL_LIST_PAGE_MAX " + CHANNEL_LIST_PAGE_MAX);
  }

  public FavoriteListDialog(Context context) {

    this(context, R.style.nav_dialog);
  }

  @Override
  public void show() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show");
    super.show();
    setWindowPosition();
    canContinueChangeChannel = true;
    isFavListMove = false;
    mTitleTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    // commonIntegration.setChannelChangedListener(listener);
    resetFavouriteListViw();
    if(TextToSpeechUtil.isTTSEnabled(mContext)){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTS enable");
        mFavoriteListView.setAccessibilityDelegate(mFavListViewDelegate);
        mFavouriteTypeView.setAccessibilityDelegate(mFavListViewDelegateforType);
    } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTS disEnable");
        mFavoriteListView.setAccessibilityDelegate(null);
        mFavouriteTypeView.setAccessibilityDelegate(null);
    }
  }

  Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {

      switch (msg.what) {
        case SET_SELECTION_INDEX:
          int curChId = commonIntegration.getCurrentChannelId();
          int chIndex = mChannelAdapter.isExistCh(curChId);
          if (chIndex >= 0) {
            mFavoriteListView.requestFocus();
            mFavoriteListView.setSelection(chIndex);
            mLastSelection = chIndex;
          } else {
            mChannelAdapter.updateData(getNextPrePageChList(true));
            mFavoriteListView.requestFocus();
            mFavoriteListView.setSelection(0);
          }
          break;
        case TYPE_CHANGECHANNEL_ENTER:
          commonIntegration.selectChannelById(msg.arg1);
          break;
        case TYPE_RESET_FAVOURITELIST:
          int selection = 0;
          List<MtkTvChannelInfoBase> tempChlist = (List<MtkTvChannelInfoBase>) msg.obj;
          if (tempChlist == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChlist = null");
            mChannelAdapter =null;
            mFavoriteListView.setAdapter(null);
          } else {
            mChannelAdapter = new ChannelAdapter(mContext, tempChlist);
            mChannelAdapter.setCurrentFavouriteType(CURRENT_FAVOURITE_TYPE);
            int index = mChannelAdapter.isExistCh(msg.arg1);
            selection = index < 0 ? mLastSelection : index;
            if (selection > mChannelAdapter.getCount() - 1) {
              selection = 0;
            }
            mLastSelection = selection;
            if (mChannelAdapter != null) {
              saveLastPosition(commonIntegration.getCurrentChannelId(),
                  mChannelAdapter.getChannellist());
            }
            mFavoriteListView.setAdapter(mChannelAdapter);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChlist = " + tempChlist.size());
            mFavoriteListView.setFocusable(true);
            mFavoriteListView.requestFocus();
            mFavoriteListView.setSelection(selection);
          }
          showPageUpDownView();
          mFavouriteListTipView.setVisibility(View.VISIBLE);
          break;
        default:
          break;
      }
    };
  };

  @Override
  public boolean isCoExist(int componentID) {

    switch (componentID) {
      case NAV_COMP_ID_PVR_TIMESHIFT:
//        if (null != StatePVR.getInstance() && StatePVR.getInstance().isRecording()) {
//          StatePVR.getInstance().dissmissBigCtrlBar();
//        }
        return true;
      case NAV_COMP_ID_BANNER:
        return false;
      case NAV_COMP_ID_POP:
        return true;
      default:
        break;
    }
    return super.isCoExist(componentID);
  }

  public boolean dispatchKeyToTimeshift() {

    int setUpTShiftValue = MtkTvConfig.getInstance().getConfigValue(
        MenuConfigManager.SETUP_SHIFTING_MODE);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "value:fav:" + setUpTShiftValue);
    if (setUpTShiftValue != 0/* 0 close */) {
      // SETUP_SHIFTING_MODE default close when SETUP_SHIFTING_MODE is opened, freeze function
      // close.
      return true;
    }
    if (CommonIntegration.getInstance().isCurrentSourceATV()) {
      return false;
    }
      return MtkTvConfig.getInstance().getConfigValue(
              MtkTvConfigType.CFG_RECORD_REC_TSHIFT_MODE) != 0;
//      return false;
    
  }

  @Override
  public boolean isKeyHandler(int keyCode) {

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler keyCode=" + keyCode);
    if (keyCode == KeyMap.KEYCODE_RO) {
        if(commonIntegration.is3rdTVSource()){
            return false;
        }
      boolean isTvSrc = CommonIntegration.getInstance().isCurrentSourceTv();
      if (isTvSrc) {
       /* if (dispatchKeyToTimeshift()) {
          return false;   // timeshfit and favlist key is same
        }*/
        if (CommonIntegration.getInstance().isCurrentSourceBlockEx()) {
          return false;
        }
        // Fix CR:DTV00587161
        if (CommonIntegration.getInstance().hasActiveChannel()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler keyCode=" + keyCode);
    boolean isHandled = true;
    startTimeout(NAV_TIMEOUT_300);
    if (mContext == null) {
      return false;
    }
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
      case KeyMap.KEYCODE_BACK:
          if(isFavListMove){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_BACK favlistmove is stop ");
              resetFavouriteListMoveViw();
              updateFavList();
              isFavListMove=false;
              return true;
          }else{
              dismiss();
              break;
          }
       
      case KeyMap.KEYCODE_MENU:
      case KeyMap.KEYCODE_MTKIR_GUIDE:
      case KeyMap.KEYCODE_MTKIR_SOURCE:
        isHandled = false;
        break;
      case KeyMap.KEYCODE_PAGE_DOWN:
        return true;
      case KeyMap.KEYCODE_DPAD_CENTER:
        enterKeySelectChannel();
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
        break;
      case KeyMap.KEYCODE_MTKIR_STOP:
//        if (TimeShiftManager.getInstance() != null
//            && TimeShiftManager.getInstance().pvrIsRecording()) {
//          isHandled = false;
//          break;
//        }
//        if (TimeShiftManager.getInstance() != null
//            && TimeShiftManager.getInstance().tshiftIsRunning()) {
//          isHandled = false;
//          break;
//        }
        if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
          // channel list and current channel not null
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in KEYCODE_MTKIR_STOP,canContinueChangeChannel ="
              + canContinueChangeChannel);
          if (canContinueChangeChannel) {
            if (favChannelManager.changeChannelToNextFav()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in KEYCODE_MTKIR_STOP,canContinueChangeChannel = false");
              canContinueChangeChannel = false;
            } else {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in KEYCODE_MTKIR_STOP,canContinueChangeChannel = true");
              canContinueChangeChannel = true;
            }
          }
        }
        break;
      case KeyMap.KEYCODE_MTKIR_PRECH:
          if(isFavListMove){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favlistmove runing ");
              return true;
          }
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
        updateFavList();
        break;
      case KeyMap.KEYCODE_MTKIR_CHUP:
      case KeyMap.KEYCODE_MTKIR_CHDN:
          if(isFavListMove){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favlistmove runing ");
              return true;
          }
        isHandled = false;
        dismiss();
        break;
      case KeyMap.KEYCODE_DPAD_LEFT:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_LEFT");
          if(isFavListMove){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favlistmove runing ");
              return true;
          }
          if(CURRENT_FAVOURITE_TYPE > 0){
              CURRENT_FAVOURITE_TYPE -= 1;
              setFavIndexToChannelList(CURRENT_FAVOURITE_TYPE);
              resetFavouriteListViw();
          }
          break;
      case KeyMap.KEYCODE_DPAD_RIGHT: 
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_RIGHT");
          if(isFavListMove){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favlistmove runing KEYCODE_DPAD_RIGHT ");
              return true;
          }
          if(CURRENT_FAVOURITE_TYPE < 3){
              CURRENT_FAVOURITE_TYPE += 1;
              setFavIndexToChannelList(CURRENT_FAVOURITE_TYPE);
              resetFavouriteListViw();
          }
          break;
      case KeyMap.KEYCODE_MTKIR_BLUE:
          if(isFavListMove){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favlistmove runing ");
              return true;
          }
        MtkTvChannelInfoBase selectChannel = (MtkTvChannelInfoBase) mFavoriteListView
            .getSelectedItem();
        favChannelManager.deleteFavorite(selectChannel, favoriteListListener);
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
        if(!TurnkeyUiMainActivity.getInstance().isUKCountry){
        BannerView bannerView = (BannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
             if (bannerView != null/* && bannerView.isShown()*/) {
                 bannerView.changeChannelFavoriteMark();
             }
        }
        break;
      case KeyMap.KEYCODE_MTKIR_YELLOW:
/*        mFavoriteListView.setVisibility(View.GONE);
        mFavouriteTypeView.setVisibility(View.VISIBLE);
        mFavouriteListTipView.setVisibility(View.INVISIBLE);
        mFavouriteTypeView.setFocusable(true);
        mFavouriteTypeView.requestFocus();
        mFavouriteTypeView.setSelection(CURRENT_FAVOURITE_TYPE);
        mTitleTextView.setText(mContext.getResources().getString(R.string.nav_favourite_selection));*/
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_YELLOW");
          if(mChannelAdapter !=null && mChannelAdapter.getCount() > 1){
              View favView =(View) mFavoriteListView.getSelectedView();
              mChannelAdapter.updateView(favView, mFavoriteListView.getSelectedItemPosition(),View.VISIBLE);
              favmovetag = mChannelAdapter.getChannellist().get(mFavoriteListView.getSelectedItemPosition());
              favmove = favmovetag;
              indexTo = mFavoriteListView.getSelectedItemPosition();
              isFavListMove =true; 
              
          }
        break;
      case KeyMap.KEYCODE_MTKIR_RED:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED");
        if (hasNextPage && mFavoriteListView.getSelectedItemPosition() != 0) {
          mFavoriteListView.requestFocus();
          mFavoriteListView.setSelection(0);
        } else if (hasNextPage) {
          mChannelAdapter.updateData(getNextPrePageChList(false));
          mFavoriteListView.requestFocus();
          mFavoriteListView.setSelection(0);
        }
        break;
      case KeyMap.KEYCODE_MTKIR_GREEN:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_GREEN");
        if (hasNextPage && (mFavoriteListView.getSelectedItemPosition() != mChannelAdapter.getCount() - 1)) {
          mFavoriteListView.requestFocus();
          mFavoriteListView.setSelection(mChannelAdapter.getCount() - 1);
        } else if (hasNextPage) {
          mChannelAdapter.updateData(getNextPrePageChList(true));
          mFavoriteListView.requestFocus();
          mFavoriteListView.setSelection(mChannelAdapter.getCount() - 1);
        }
        break;
      default:
        isHandled = false;
        break;
    }
    if (isHandled == false && TurnkeyUiMainActivity.getInstance() != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Back Key To TurnkeyUiMainActivity");
      return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
    }
    return isHandled;
  }

  FavoriteListListener favoriteListListener = new FavoriteListListener() {

    @Override
    public void updateFavoriteList() {
      // TODO Auto-generated method stub
      updateFavList();
    }
  };

  /**
   * Handle favList's Enter key,select channel.
   *
   * @param select
   * @param listener
   */
  public void enterKeySelectChannel() {

    MtkTvChannelInfoBase selectedChannel = (MtkTvChannelInfoBase) mFavoriteListView
        .getSelectedItem();
    MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
    if (selectedChannel != null && currentChannel != null) {
      if (!selectedChannel.equals(currentChannel)) {
        boolean changeSuccess = commonIntegration.selectChannelByInfo(selectedChannel);
        if (changeSuccess) {
          mLastSelection = mFavoriteListView.getSelectedItemPosition();
          if (mChannelAdapter != null) {
            saveLastPosition(selectedChannel.getChannelId(),
                mChannelAdapter.getChannellist());
          }
          dismiss();
        }
      }
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

  private void loadChannelTypeRes() {

    String[] typestemp = mContext.getResources().getStringArray(R.array.nav_favourite_type);
    types = addSuffix(typestemp, R.string.str);
    mTitlePre = mContext.getResources().getString(R.string.nav_channel_list)+" - ";
   // mTitleTextView.setText(mTitlePre + types[CURRENT_FAVOURITE_TYPE]);
/*    ArrayList<HashMap<String, String>> typeList = new ArrayList<HashMap<String, String>>();
    for (String type : types) {
      HashMap<String, String> tmpType = new HashMap<String, String>();
      tmpType.put(FAVOURITE_TYPE, type);
      typeList.add(tmpType);
    }
    SimpleAdapter listAdapter = new SimpleAdapter(mContext, typeList,
        R.layout.nav_channel_type_item, new String[] {
            FAVOURITE_TYPE
        },
        new int[] {
            R.id.nav_channel_type_list_item
        });
    mFavouriteTypeView.setAdapter(listAdapter);
    mFavouriteTypeView.setOnKeyListener(new ChannelListOnKey());*/
  }

  private void updateFavList() {
      ((Activity) mContext).runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mTitleTextView.setText(mTitlePre + types[CURRENT_FAVOURITE_TYPE]);
/*            Drawable favtype = mContext.getResources().getDrawable(R.drawable.tv_favtype);
            favtype.setBounds(-20, 0, favtype.getMinimumHeight(), favtype.getMinimumHeight());
            mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, favtype, null);*/
          }
        });
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateFavList");
    new Thread(new Runnable() {
      @Override
      public void run() {
        int chId = CommonIntegration.getInstance().getCurrentChannelId();
        List<MtkTvChannelInfoBase> tempList = processListWithThread(chId);
        Message msg = Message.obtain();
        msg.what = TYPE_RESET_FAVOURITELIST;
        msg.arg1 = chId;
        msg.obj = tempList;
        handler.sendMessage(msg);
      }
    }).start();
  }

  private List<MtkTvChannelInfoBase> processListWithThread(int chId) {

    MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
    List<MtkTvChannelInfoBase> tempApiChList = null;
    preNum = 0;
    if (currentChannel != null) {
      if ((currentChannel.getNwMask() & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
        preNum = commonIntegration.getFavouriteChannelCount(favMask[CURRENT_FAVOURITE_TYPE]);
        hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
        if (hasNextPage) {
          int index = currentChannelInListIndex(chId);
          if (index >= 0) {
            tempApiChList = commonIntegration.getFavoriteListByFilter(
                favMask[CURRENT_FAVOURITE_TYPE], chId,true,index, CHANNEL_LIST_PAGE_MAX - index,CURRENT_FAVOURITE_TYPE);
          } else {
            tempApiChList = commonIntegration.getFavoriteListByFilter(
                favMask[CURRENT_FAVOURITE_TYPE], chId,true, mLastSelection, CHANNEL_LIST_PAGE_MAX
                    - mLastSelection,CURRENT_FAVOURITE_TYPE);
          }
        } else if (preNum > 0) {
          tempApiChList = commonIntegration.getFavoriteListByFilter(
              favMask[CURRENT_FAVOURITE_TYPE], 0,false, 0, preNum,CURRENT_FAVOURITE_TYPE);
        }
      } else if ((currentChannel.getNwMask() & favMask[CURRENT_FAVOURITE_TYPE]) == 0) {
        preNum = commonIntegration.getFavouriteChannelCount(favMask[CURRENT_FAVOURITE_TYPE]);
        hasNextPage = preNum > CHANNEL_LIST_PAGE_MAX ? true : false;
        if (preNum > 0) {
          tempApiChList = commonIntegration.getFavoriteListByFilter(
              favMask[CURRENT_FAVOURITE_TYPE],
              0,false, 0, preNum > CHANNEL_LIST_PAGE_MAX ? CHANNEL_LIST_PAGE_MAX : preNum,CURRENT_FAVOURITE_TYPE);
        }
      }
    }

    return tempApiChList;
  }

  private int currentChannelInListIndex(int currentChannelId) {
    int size = mChannelIdList.size();
    for (int i = 0; i < size; i++) {
      if (currentChannelId == mChannelIdList.get(i)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i>>>>" + i);
        return i;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i>>>>" + -1);
    return -1;
  }

  private void saveLastPosition(int currentChannelId, List<MtkTvChannelInfoBase> tempChlist) {
    if (tempChlist != null) {
      mChannelIdList.clear();
      int size = tempChlist.size();
      for (int i = 0; i < size; i++) {
        int channelId = tempChlist.get(i).getChannelId();
        mChannelIdList.add(channelId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannelId>>>" + currentChannelId + " chid=" + channelId);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
    setContentView(R.layout.nav_favoritelist);
    setWindowPosition();
    findViews();
    register();
  //  updateFavList();
    loadChannelTypeRes();
  }

  @Override
  protected void onStart() {
    startTimeout(NAV_TIMEOUT_300);
    super.onStart();
  }
  
  public void handleCallBack(){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCallBack()");
      updateFavList();
  }

  private void findViews() {

    mFavoriteListView = (ListView) findViewById(R.id.nav_favorite_listview);
    mFavouriteTypeView = (ListView) findViewById(R.id.nav_favourite_typeview);
    mFavouriteListPageUpDownView = findViewById(R.id.nav_fav_page_up_down);
    mFavouriteListTipView = findViewById(R.id.nav_favourite_list_tip);
    mTitleTextView = (TextView) findViewById(R.id.nav_favorite_list_title);
    mNavPageUpTextView = (TextView) findViewById(R.id.fav_nav_page_up);
    mNavFavoriteSelectTextView = (TextView) findViewById(R.id.fav_nav_favorite_select);
    mNavExitTextView = (TextView) findViewById(R.id.fav_nav_favorite_exit);
    mPageView = (TableRow)findViewById(R.id.nav_fav_page_function);
  //  navFavType= (ImageView) findViewById(R.id.nav_fav_type);
    mYellowImg = (ImageView) findViewById(R.id.channel_nav_select_list_icon);
    mYellowView = (TextView)findViewById(R.id.channel_nav_select_list);
  }

  @Override
  public boolean deinitView() {
    mFavoriteListView = null;
    handler.removeCallbacksAndMessages(null);
    return super.deinitView();
  }

  private void register() {

    mFavoriteListView.setOnKeyListener(new ChannelListOnKey());
  }

  private void showPageUpDownView() {

    if (hasNextPage) {
      if (mFavouriteListPageUpDownView.getVisibility() != View.VISIBLE) {
        mFavouriteListPageUpDownView.setVisibility(View.VISIBLE);
      }
    } else {
      if (mFavouriteListPageUpDownView.getVisibility() != View.INVISIBLE) {
        mFavouriteListPageUpDownView.setVisibility(View.INVISIBLE);
      }
    }

    if (preNum > 0){
      mPageView.setVisibility(View.VISIBLE);
    }else{
      mPageView.setVisibility(View.GONE);
    }

    if (preNum > 1){
      mYellowView.setVisibility(View.VISIBLE);
      mYellowImg.setVisibility(View.VISIBLE);
    }else{
      mYellowView.setVisibility(View.GONE);
      mYellowImg.setVisibility(View.GONE);
    }
  }


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

  public int dip2px(Context context, int dp) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dp / scale + 0.5f);
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    mContext = null;
  }

  @Override
  public void dismiss() {
    mFavouriteTypeView.setVisibility(View.GONE);
    mFavoriteListView.setVisibility(View.VISIBLE);
    super.dismiss();
    resetFavouriteListMoveViw();
    MtkTvConfig.getInstance().setConfigValue(
            MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
  }

  class ChannelListOnKey implements View.OnKeyListener {
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      // int selectPosition = mFavoriteListView.getSelectedItemPosition();
      switch (v.getId()) {
        case R.id.nav_favorite_listview:
          int slectPosition = mFavoriteListView.getSelectedItemPosition();
          if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_CENTER:
                startTimeout(NAV_TIMEOUT_300);
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mChannelItemKeyLsner*********** selectPosition"
                    + mFavoriteListView.getSelectedItemPosition());
                if(isFavListMove){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mChannelItemKeyLsner*********** selectPosition");
                    isFavListMove=false;
                    int indexFrom = 0;
                    switch (CURRENT_FAVOURITE_TYPE) {
                    case FAVOURITE_1:
                        indexFrom = favChannelManager.getFavoriteIdx( favMask[0],favmovetag.getChannelId(),0);
                        favChannelManager.favoriteListInsertMove(indexFrom, indexTo);
                        break;
                    case FAVOURITE_2:
                        indexFrom = favChannelManager.getFavoriteIdx( favMask[1],favmovetag.getChannelId(),1);
                        favChannelManager.favoriteListInsertMove(indexFrom, indexTo);
                        break;
                    case FAVOURITE_3:
                        indexFrom = favChannelManager.getFavoriteIdx( favMask[2],favmovetag.getChannelId(),2);
                        favChannelManager.favoriteListInsertMove(indexFrom, indexTo);
                        break;
                    case FAVOURITE_4:
                        indexFrom = favChannelManager.getFavoriteIdx( favMask[3],favmovetag.getChannelId(),3);
                        favChannelManager.favoriteListInsertMove(indexFrom, indexTo);
                        break;
                    default:
                        indexFrom = favChannelManager.getFavoriteIdx( favMask[0],favmovetag.getChannelId(),0);
                        favChannelManager.favoriteListInsertMove(indexFrom, indexTo);

                        break;
                    }
                    resetFavouriteListMoveViw();
                    return true;
                }else{
                    final MtkTvChannelInfoBase selectedChannel = (MtkTvChannelInfoBase) mFavoriteListView
                            .getSelectedItem();
                        if (!selectedChannel.equals(commonIntegration.getCurChInfo())) {
                              if (StateDvr.getInstance() !=null && StateDvr.getInstance().isRunning()) {
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVR is running !");

                              DvrDialog conDialog = new DvrDialog((Activity) StateDvr.getInstance().getmContext(),
                                  DvrDialog.TYPE_Confirm_From_ChannelList,
                                  keyCode, DvrDialog.TYPE_Record);
                              com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-1,ID:" + selectedChannel.getChannelId());
                              com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "channelID:-1,Name:" + selectedChannel.getServiceName());
                              conDialog.setMtkTvChannelInfoBase(selectedChannel.getChannelId());
                              conDialog.show();
                              dismiss();
                          } else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "no pvr and time shift");
                            enterKeySelectChannel();
                          }
                        }
                        return true;
                }
                
              case KeyEvent.KEYCODE_DPAD_DOWN:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!!");
                if(isFavListMove){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!! isFavListMove is start");
                    if (mFavoriteListView != null && mFavoriteListView.getChildAt(slectPosition) != null){
                    	mFavoriteListView.getChildAt(slectPosition).requestFocusFromTouch();
                    }
                    if (hasNextPage &&  slectPosition == mChannelAdapter.getCount() - 1 ) {
                        List<MtkTvChannelInfoBase> favlist=  getNextPrePageChList(true);
                        for(int i=0;i<favlist.size();i++){
                            if(favmovetag.getChannelId() ==favlist.get(i).getChannelId()){
                                favlist.remove(i);
                                break;
                            }
                        }
                        if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                            favlist.remove(favlist.size()-1);
                        }
                        favlist.add(0,favmovetag);
                        indexTo = 0;
                        mChannelAdapter.setFavlistMovepostion(0);
                        mChannelAdapter.updateData(favlist);
                        mFavoriteListView.setSelection(0);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    } else {
                        if (mFavoriteListView.getSelectedItemPosition() < mChannelAdapter.getCount() - 1){
                            List<MtkTvChannelInfoBase> favlist=  mChannelAdapter.getChannellist();
                            favmove = favlist.remove(slectPosition+1);
                            favlist.add(slectPosition,favmove);
                            indexTo = slectPosition+1;
                            mChannelAdapter.setFavlistMovepostion(slectPosition+1);
                            mChannelAdapter.updateData(favlist);
                            mFavoriteListView.setSelection(slectPosition+1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }else if(mFavoriteListView.getSelectedItemPosition() == mChannelAdapter.getCount() - 1){
                            List<MtkTvChannelInfoBase> favlist=  mChannelAdapter.getChannellist();
                            favmove = favlist.remove(favlist.size()-1);
                            favlist.add(0,favmove);
                            indexTo = 0;
                            mChannelAdapter.setFavlistMovepostion(0);
                            mChannelAdapter.updateData(favlist);
                            mFavoriteListView.setSelection(0);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        
                    }
                        return false;
                    
                   
                }else{
                	if (mFavoriteListView != null && mFavoriteListView.getChildAt(slectPosition) != null){
                		mFavoriteListView.getChildAt(slectPosition).requestFocusFromTouch();
                	}
                    if (hasNextPage &&  slectPosition == mChannelAdapter.getCount() - 1 ) {
                        List<MtkTvChannelInfoBase> favlist=  getNextPrePageChList(true);
                        if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                            favlist.remove(favlist.size()-1);
                        }
                        mChannelAdapter.updateData(favlist);
                        mFavoriteListView.setSelection(0);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    } else {
                        startTimeout(NAV_TIMEOUT_300);
                        if (mFavoriteListView.getSelectedItemPosition() == mChannelAdapter.getCount() - 1) {
                            mFavoriteListView.setSelection(0);
                            return true;
                        }
                        return false;
                    }
                }
              case KeyEvent.KEYCODE_DPAD_UP:
                // the same root cause to DTV00720877
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_UP!!!!!");
                if(isFavListMove){

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN!!!!! isFavListMove is start");
                    if (mFavoriteListView != null && mFavoriteListView.getChildAt(slectPosition) != null){
                    	mFavoriteListView.getChildAt(slectPosition).requestFocusFromTouch();
                    }
                    if (hasNextPage &&  slectPosition == 0 ) {
                        List<MtkTvChannelInfoBase> favlist=  getNextPrePageChList(false);
                        for(int i=0;i<favlist.size();i++){
                            if(favmovetag.getChannelId() ==favlist.get(i).getChannelId()){
                                favlist.remove(i);
                                break;
                            }
                        }
                        if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                            favlist.remove(0);
                        }
                        mChannelAdapter.setFavlistMovepostion(mChannelAdapter.getCount() - 1);
                        favlist.add(favmovetag);
                        indexTo = mChannelAdapter.getCount();
                        mChannelAdapter.updateData(favlist);
                        mFavoriteListView.setSelection(mChannelAdapter.getCount() - 1);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    } else {
                        if (mFavoriteListView.getSelectedItemPosition() >0){               
                            List<MtkTvChannelInfoBase> favlist=  mChannelAdapter.getChannellist();
                            favmove = favlist.remove(slectPosition-1);
                            favlist.add(slectPosition,favmove);
                            indexTo = slectPosition-1;
                            mChannelAdapter.setFavlistMovepostion(slectPosition-1);
                            mChannelAdapter.updateData(favlist);
                            mFavoriteListView.setSelection(slectPosition-1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }else if(mFavoriteListView.getSelectedItemPosition() == 0){
                            List<MtkTvChannelInfoBase> favlist=  mChannelAdapter.getChannellist();
                            favmove = favlist.remove(0);
                            favlist.add(favmove);
                            indexTo = favlist.size()+1;
                            mChannelAdapter.setFavlistMovepostion(favlist.size()-1);
                            mChannelAdapter.updateData(favlist);
                            mFavoriteListView.setSelection(favlist.size()-1);
                            startTimeout(NAV_TIMEOUT_300);
                            return true;
                        }
                        return false;
                    }
                
                }else{
                	if (mFavoriteListView != null && mFavoriteListView.getChildAt(slectPosition) != null){
                		mFavoriteListView.getChildAt(slectPosition).requestFocusFromTouch();
                	}
                    if (hasNextPage &&  slectPosition == 0 ) {
                        List<MtkTvChannelInfoBase> favlist=  getNextPrePageChList(false);
                        if(favlist.size() > CHANNEL_LIST_PAGE_MAX){
                            favlist.remove(0);
                        }
                        mChannelAdapter.updateData(favlist);
                        mFavoriteListView.setSelection(mChannelAdapter.getCount() - 1);
                        startTimeout(NAV_TIMEOUT_300);
                        return true;
                    } else {
                        startTimeout(NAV_TIMEOUT_300);
                        if (mFavoriteListView.getSelectedItemPosition() == 0) {
                            mFavoriteListView.setSelection(mChannelAdapter.getCount() - 1);
                            return true;
                        }
                        return false;
                    }
                    
                }
              case KeyEvent.KEYCODE_MEDIA_EJECT:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MEDIA_EJECT!!!!!");
                startTimeout(NAV_TIMEOUT_300);
                return false;
              default:
                return FavoriteListDialog.this.onKeyHandler(keyCode,
                    event);
            }
          }
          return false;
        case R.id.nav_favourite_typeview:
          if (event.getAction() == KeyEvent.ACTION_DOWN) {
            startTimeout(NAV_TIMEOUT_300);
            switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_CENTER:
                resetFavouriteListViw();
                return true;
              case KeyEvent.KEYCODE_DPAD_DOWN:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN position = "
                    + mFavouriteTypeView.getSelectedItemPosition());
                if (mFavouriteTypeView.getSelectedItemPosition() == FAVOURITE_4) {
                  mFavouriteTypeView.setSelection(FAVOURITE_1);
                  return true;
                }
                break;
              case KeyEvent.KEYCODE_DPAD_UP:
                if (mFavouriteTypeView.getSelectedItemPosition() == FAVOURITE_1) {
                  mFavouriteTypeView.setSelection(FAVOURITE_4);
                  return true;
                }
                break;
              case KeyMap.KEYCODE_MTKIR_BLUE:
              case KeyMap.KEYCODE_MTKIR_YELLOW:
              case KeyMap.KEYCODE_MTKIR_RED:
              case KeyMap.KEYCODE_MTKIR_GREEN:
                return true;
              default:
                  return false;
            }
          }
          default:
          return false;
      }
    }
  }

  private void resetFavouriteListViw() {
  //      CURRENT_FAVOURITE_TYPE = mFavouriteTypeView.getSelectedItemPosition();
        favChannelManager.setFavoriteType(CURRENT_FAVOURITE_TYPE);
		mSaveValue.saveValue(FAVOURITE_TYPE,CURRENT_FAVOURITE_TYPE);
        mFavoriteListView.setAdapter(null);
        mFavoriteListView.setVisibility(View.VISIBLE);
//        mFavouriteTypeView.setVisibility(View.GONE);
        resetFavouriteList();
    }
  private void resetFavouriteListMoveViw() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetFavouriteListMoveViw");
      if(mChannelAdapter != null && mChannelAdapter.getCount() >1){
          View favView =(View) mFavoriteListView.getSelectedView();
          mChannelAdapter.updateView(favView, -1,View.INVISIBLE);
          
      }
    }
  void selectTifChannel(int keyCode, TIFChannelInfo selectedChannel) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_CENTER:
        boolean changeSuccess = TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(
            selectedChannel);
        if (changeSuccess) {
          mLastSelection = mFavoriteListView.getSelectedItemPosition();
          if (mChannelAdapter != null) {
            saveLastPosition(selectedChannel.mMtkTvChannelInfo.getChannelId(),
                mChannelAdapter.getChannellist());
          }
        }
        break;
      default:
        break;
    }
  }

  /**
   * true(next),false(pre)
   */
  private List<MtkTvChannelInfoBase> getNextPrePageChList(boolean next) {

    List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    MtkTvChannelInfoBase chInfo = null;
    if (mChannelAdapter != null && mChannelAdapter.getCount() > 0) {
      if (next) {
        if(isFavListMove){
            chInfo = favmove; 
         }else{
             chInfo = mChannelAdapter.getItem(mChannelAdapter.getCount() - 1);
         }
        list = commonIntegration.getFavoriteListByFilter(favMask[CURRENT_FAVOURITE_TYPE],
                    chInfo.getChannelId() ,false, 0, CHANNEL_LIST_PAGE_MAX+1,CURRENT_FAVOURITE_TYPE);
      } else {
       if(isFavListMove){
          chInfo = favmove; 
          }else{
             chInfo = mChannelAdapter.getItem(0); 
          }        
         list = commonIntegration.getFavoriteListByFilter(favMask[CURRENT_FAVOURITE_TYPE],
                    chInfo.getChannelId(),false, CHANNEL_LIST_PAGE_MAX+1, 0,CURRENT_FAVOURITE_TYPE);
      }
    }
    return list;
  }

  private synchronized void resetFavouriteList() {
    // when get favourite channels send this message
    updateFavList();
  }

  @Override
  public void updateComponentStatus(int statusID, int value) {
    if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
      if (isVisible()) {
        if (mFavoriteListView.getChildCount() > 0 && value == 0) {
          Message msg = Message.obtain();
          msg.what = SET_SELECTION_INDEX;
          handler.sendMessage(msg);
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in ComponentStatusListener.NAV_CHANNEL_CHANGED");
      canContinueChangeChannel = true;
    } else if (statusID == ComponentStatusListener.NAV_COMPONENT_SHOW) {
      if (value == NAV_COMP_ID_TELETEXT && isVisible()) {
        dismiss();
      }
    }
  }

  private AccessibilityDelegate mFavListViewDelegate = new AccessibilityDelegate(){

        @Override
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                AccessibilityEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
            do {
                if(mFavoriteListView.equals(host)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "host:" + mFavoriteListView + "," + host);
                    break;
                }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":host =" + false);
                }
                List<CharSequence> texts = event.getText();
                if(texts == null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
                    break;
                }
                if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                    int index = findSelectItem(texts.get(0).toString());
                    if(index>=0){
                        mFavoriteListView.setSelection(index);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + index);
                        startTimeout(NAV_TIMEOUT_300);
                    }
                } else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent: enterKeySelectChannel");
                    enterKeySelectChannel();
                }

            } while(false);

            try {//host.onRequestSendAccessibilityEventInternal(child, event);
                Class clazz = Class.forName("android.view.ViewGroup");
                java.lang.reflect.Method getter =
                    clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
                        View.class, AccessibilityEvent.class);
                return (boolean)getter.invoke(host, child, event);
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception " + e);
            }

            return true;
        }
      };

        private int findSelectItem(String string) {
            if(mChannelAdapter!=null && mChannelAdapter.getChannellist()!=null){
                List<MtkTvChannelInfoBase> channellist = mChannelAdapter.getChannellist();
                for (int i = 0; i < channellist.size(); i++) {
                    MtkTvChannelInfoBase mCurrentChannel = channellist.get(i);
                    if (mCurrentChannel instanceof MtkTvATSCChannelInfo) {
                      MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) mCurrentChannel;
                      if(string.equals(tmpAtsc.getMajorNum() + "-" + tmpAtsc.getMinorNum())){
                            return i;
                        }
                    } else if (mCurrentChannel instanceof MtkTvISDBChannelInfo) {
                      MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) mCurrentChannel;
                      if(string.equals(tmpIsdb.getMajorNum() + "-" + tmpIsdb.getMinorNum())){
                            return i;
                      }
                    } else {
                      if(string.equals("" + mCurrentChannel.getChannelNumber())){
                            return i;
                      }
                    }
                }
            }
            return -1;
        }

        private AccessibilityDelegate mFavListViewDelegateforType = new AccessibilityDelegate(){

            @Override
            public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                    AccessibilityEvent event) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
                do {
                    if(mFavouriteTypeView.equals(host)){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "host:" + mFavouriteTypeView + "," + host);
                        break;
                    }else{
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":host =" + false);
                    }
                    List<CharSequence> texts = event.getText();
                    if(texts == null) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
                        break;
                    }
                    if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                        int index = findSelectItemforType(texts.get(0).toString());
                        if(index>=0 && index<=3){
                            mFavouriteTypeView.setSelection(index);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + index);
                            startTimeout(NAV_TIMEOUT_300);
                        }
                    } else if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//select item
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent: enterKeySelectChannel");
                        resetFavouriteListViw();
                    }

                } while(false);

                try {//host.onRequestSendAccessibilityEventInternal(child, event);
                    Class clazz = Class.forName("android.view.ViewGroup");
                    java.lang.reflect.Method getter =
                        clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
                            View.class, AccessibilityEvent.class);
                    return (boolean)getter.invoke(host, child, event);
                } catch (Exception e) {
                    android.util.Log.d(TAG, "Exception " + e);
                }

				return true;
            }
      };

      private int findSelectItemforType(String string) {
          if(types!=null){
              for (int i = 0; i < types.length; i++) {
                    if(types[i].equals(string)){
                        return i;
                    }
                }
          }
          return -1;
      }

    private void setFavIndexToChannelList(int index){
      NavBasicDialog dialog = (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
      if (dialog instanceof ChannelListDialog){
        ChannelListDialog channelListDialog =  (ChannelListDialog)dialog;
        channelListDialog.setFavIndexFromFavDialog(index);
      }
    }
}
