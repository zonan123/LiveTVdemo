package com.mediatek.wwtv.tvcenter.nav.fav;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import java.util.ArrayList;
import java.util.List;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

/**
 * @Add: Favorite Channel Manager.
 *
 * @author hs_yataozhang
 */
public class FavChannelManager {

  private static final String TAG = "FavChannelManager";
  private static FavChannelManager favChannelManager = null;
  private  CommonIntegration commonIntegration = null;
  private static final int WRITE_CHANNEL_FLASH = 0x01;
  private int CURRENT_FAVOURITE_TYPE = 0;
  private Context mContext;
  static final String SPNAME = "CHMODE";
  private static final String FAVOURITE_TYPE = "favouriteType";
  private int saveFlashDealyTime = 10000;
  public static String ADD_ERASE_ACTION = "com.favlist.addorerase.broadcast";
  private final int[] favMask = new int[] {
      MtkTvChCommonBase.SB_VNET_FAVORITE1, MtkTvChCommonBase.SB_VNET_FAVORITE2,
      MtkTvChCommonBase.SB_VNET_FAVORITE3, MtkTvChCommonBase.SB_VNET_FAVORITE4
  };

  private FavChannelManager(Context context) {
      mContext = context;
      commonIntegration = CommonIntegration.getInstance();
      SaveValue mSaveValue = SaveValue.getInstance(context);
      CURRENT_FAVOURITE_TYPE = mSaveValue.readValue(FAVOURITE_TYPE, 0);
      saveFlashDealyTime = context.getResources().getInteger(R.integer.nav_favorite_list_saveflash_delay_time);
  }

  public static synchronized FavChannelManager getInstance(Context context) {

    if(favChannelManager == null){

      favChannelManager = new FavChannelManager(context.getApplicationContext());
    }else{
        favChannelManager.mContext= context.getApplicationContext();
    }
    return favChannelManager;
  }

  public void setFavoriteType(int currentFavoriteType) {

    CURRENT_FAVOURITE_TYPE = currentFavoriteType;
  }

  // write channel info to flash
  Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      removeMessages(WRITE_CHANNEL_FLASH);
      if (msg.what == WRITE_CHANNEL_FLASH) {
        MtkTvConfig.getInstance().setConfigValue(
            MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
      }
    };
  };

  /**
   * Handle favList's add_erase key,if current channel is favourite channel remove it ,if current
   * channel is not favourite channel add it,just by current favpurite list.
   *
   * @param select
   * @param listener
   */
  public void favAddOrErase(FavoriteListListener favoriteListListener) {

    MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
    if (currentChannel != null) {
        if(currentChannel.isSkip()){
            Toast.makeText(mContext, mContext.getString(R.string.nav_fav_skip_flow), Toast.LENGTH_LONG)
            .show(); 
            return ;
        }
      int nwMask = currentChannel.getNwMask();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
      if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) == 0) {
        nwMask |= favMask[CURRENT_FAVOURITE_TYPE];
        favoriteListAddTail(currentChannel.getChannelId());
      } else if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
        nwMask &= ~favMask[CURRENT_FAVOURITE_TYPE];
        favoriteListAddTail(currentChannel.getChannelId());
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask after = " + nwMask);
      currentChannel.setNwMask(nwMask);
      List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
      chList.add(currentChannel);
      CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, chList);
      favoriteListListener.updateFavoriteList();
      handler.sendEmptyMessageDelayed(WRITE_CHANNEL_FLASH, saveFlashDealyTime);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannel is null");
    }
  }

  /**
   * Handle favList's add_erase key,if current channel is favourite channel remove it ,if current
   * channel is not favourite channel add it,just by current favpurite list.
   *
   * @param select
   * @param listener
   */
  public void favAddOrErase() {

    MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();

    if (currentChannel != null) {
        if(currentChannel.isSkip()){
            Toast.makeText(mContext, mContext.getString(R.string.nav_fav_skip_flow), Toast.LENGTH_LONG)
            .show(); 
            return ;
        }
      int nwMask = currentChannel.getNwMask();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
      if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) == 0) {
        nwMask |= favMask[CURRENT_FAVOURITE_TYPE];
        favoriteListAddTail(currentChannel.getChannelId());
      } else if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
        nwMask &= ~favMask[CURRENT_FAVOURITE_TYPE];
        changeChannelToFirst();
        favoriteListAddTail(currentChannel.getChannelId());
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask after = " + nwMask);
      currentChannel.setNwMask(nwMask);
      List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
      chList.add(currentChannel);
      CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, chList);
      handler.sendEmptyMessageDelayed(WRITE_CHANNEL_FLASH, saveFlashDealyTime);
      Intent intent = new Intent(ADD_ERASE_ACTION);
      mContext.sendBroadcast(intent);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannel is null");
    }
  }

  public void changeChannelToFirst(){
    if (!commonIntegration.getFavTypeState()){
      return ;
    }
    int totalCount = commonIntegration.getChannelActiveNumByAPI();
    int preNum = commonIntegration.getFavouriteChannelCount(favMask[CURRENT_FAVOURITE_TYPE]);
    if (totalCount <= 0 || preNum <= 0) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "totalCount = " + totalCount + " preNum = " + preNum);
      return ;
    }
    MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
    List<MtkTvChannelInfoBase> mfavChannelList = commonIntegration.getFavoriteListByFilter(
            favMask[CURRENT_FAVOURITE_TYPE], 0,false, 0, preNum,CURRENT_FAVOURITE_TYPE);
    int index = mfavChannelList.indexOf(currentChannel);
    if (index == 0 && preNum > 1){
      commonIntegration.selectChannelByInfo(mfavChannelList.get(1));
    }else if(index != 0){
      commonIntegration.selectChannelByInfo(mfavChannelList.get(0));
    }

  }

  /**
   * Handle favList's add_erase key,if current channel is favourite channel remove it ,if current
   * channel is not favourite channel add it,just by current favpurite list.
   *
   * @param select
   * @param listener
   */
  public MtkTvChannelInfoBase favAddOrErase(int channelId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask favAddOrErase = " + CURRENT_FAVOURITE_TYPE);
    MtkTvChannelInfoBase currentChannel = commonIntegration.getChannelById(channelId);
    if (currentChannel != null) {
        if(currentChannel.isSkip()){
            Toast.makeText(mContext, mContext.getString(R.string.nav_fav_skip_flow), Toast.LENGTH_LONG)
            .show();
            return null;
        }
      int nwMask = currentChannel.getNwMask();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
      if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) == 0) {
        nwMask |= favMask[CURRENT_FAVOURITE_TYPE];
        favoriteListAddTail(currentChannel.getChannelId());
      } else if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
        nwMask &= ~favMask[CURRENT_FAVOURITE_TYPE];
        favoriteListAddTail(currentChannel.getChannelId());
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask after = " + nwMask);
      currentChannel.setNwMask(nwMask);
      List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
      chList.add(currentChannel);
      CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, chList);
      handler.sendEmptyMessageDelayed(WRITE_CHANNEL_FLASH, saveFlashDealyTime);
      Intent intent = new Intent(ADD_ERASE_ACTION);
      mContext.sendBroadcast(intent);
      return currentChannel;
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannel is null");
    }
    return null;
  }
    public boolean isFavChannel() {
        MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
        if (currentChannel != null) {
            int nwMask = currentChannel.getNwMask();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
            if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) == 0) {
                nwMask |= favMask[CURRENT_FAVOURITE_TYPE];
                return false;
            } else if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
                nwMask &= ~favMask[CURRENT_FAVOURITE_TYPE];
                return true;
            }
        }
        return false;
    }

  public void deleteFavorite(MtkTvChannelInfoBase selectChannel,
      FavoriteListListener favoriteListListener) {

    if (selectChannel != null) {
      int nwMask = selectChannel.getNwMask();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
      if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
        nwMask &= ~favMask[CURRENT_FAVOURITE_TYPE];
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask after = " + nwMask);
      selectChannel.setNwMask(nwMask);
      List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
      chList.add(selectChannel);
      CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, chList);
      favoriteListListener.updateFavoriteList();
      handler.sendEmptyMessageDelayed(WRITE_CHANNEL_FLASH, saveFlashDealyTime);
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannel is null");
    }
  }
  
  public void deleteFavorite(MtkTvChannelInfoBase selectChannel) {
        if (selectChannel != null) {
          int nwMask = selectChannel.getNwMask();
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
          if ((nwMask & favMask[CURRENT_FAVOURITE_TYPE]) > 0) {
            nwMask &= ~favMask[CURRENT_FAVOURITE_TYPE];
            if (selectChannel.getChannelId() == commonIntegration.getCurChInfo().getChannelId()){
              changeChannelToFirst();
            }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask after = " + nwMask);
          selectChannel.setNwMask(nwMask);
          List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
          chList.add(selectChannel);
          CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, chList);
          handler.sendEmptyMessageDelayed(WRITE_CHANNEL_FLASH, saveFlashDealyTime);
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannel is null");
        }
      }
  public boolean changeChannelToNextFav() {

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeChannelToNextFav");
    if (!commonIntegration.isCurrentSourceTv()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeChannelToNextFav,not tv sourece!");
      return false;
    }
    int totalCount = commonIntegration.getChannelActiveNumByAPI();
    int preNum = commonIntegration.getFavouriteChannelCount(favMask[CURRENT_FAVOURITE_TYPE]);
    if (totalCount <= 0 || preNum <= 0) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "totalCount = " + totalCount + " preNum = " + preNum);
      return false;
    }
    MtkTvChannelInfoBase currentChannel = commonIntegration.getCurChInfo();
    List<MtkTvChannelInfoBase> mfavChannelList = commonIntegration.getFavoriteListByFilter(
        favMask[CURRENT_FAVOURITE_TYPE], 0,false, 0, preNum,CURRENT_FAVOURITE_TYPE);
    int index = mfavChannelList.indexOf(currentChannel);
    if (index >= 0) {
      if (preNum == 1) {
        return false;
      } else {
        if (index == (preNum - 1)) {
          return commonIntegration.selectChannelByInfo(mfavChannelList.get(0));
        } else if (index < (preNum - 1)) {
          return commonIntegration.selectChannelByInfo(mfavChannelList.get(index + 1));
        }
      }
    } else {
      return commonIntegration.selectChannelByInfo(mfavChannelList.get(0));
    }
    return false;
  }
  public void favoriteListInsertMove( int idxMoveFrom,int idxMoveTo) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favoriteListInsertMove idxMoveFrom is "+idxMoveFrom+"; idxMoveTo is "+idxMoveTo);
      commonIntegration.favoriteListInsertMove( CURRENT_FAVOURITE_TYPE, idxMoveFrom,idxMoveTo);
    //  return count;
  }
  public int getFavoriteIdx(int filter, int channelId, int favIndex){
      int index = 0;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFavoriteIdx filter is "+filter+"; channelId is "+channelId);
      index = commonIntegration.getFavoriteIdx(filter,channelId, CURRENT_FAVOURITE_TYPE);
      return index;
  }
  
  public void favoriteListAddTail(int channelId) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "favoriteListAddTail channelId is "+channelId);
      commonIntegration.favoriteListAddTail( CURRENT_FAVOURITE_TYPE, channelId);
   //   return count;
  }

  /**
   * Handle favList's add_erase key,if current channel is favourite channel remove it ,if current
   * channel is not favourite channel add it,just by current favpurite list.
   *
   * @param select
   * @param listener
   */
  public MtkTvChannelInfoBase favAddOrEraseIntoIndex(int channelId,int index) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask favAddOrEraseIntoIndex index= " + index);
    MtkTvChannelInfoBase currentChannel = commonIntegration.getChannelById(channelId);
    if (currentChannel != null) {
      if(currentChannel.isSkip()){
        Toast.makeText(mContext, mContext.getString(R.string.nav_fav_skip_flow), Toast.LENGTH_LONG)
                .show();
        return null;
      }
      int nwMask = currentChannel.getNwMask();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask before = " + nwMask);
      if ((nwMask & favMask[index]) == 0) {
        nwMask |= favMask[index];
        commonIntegration.favoriteListAddTail(index,currentChannel.getChannelId());
      } else if ((nwMask & favMask[index]) > 0) {
        nwMask &= ~favMask[index];
        commonIntegration.favoriteListAddTail(index,currentChannel.getChannelId());
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nwMask after = " + nwMask);
      currentChannel.setNwMask(nwMask);
      List<MtkTvChannelInfoBase> chList = new ArrayList<MtkTvChannelInfoBase>();
      chList.add(currentChannel);
      CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, chList);
      handler.sendEmptyMessageDelayed(WRITE_CHANNEL_FLASH, saveFlashDealyTime);
      Intent intent = new Intent(ADD_ERASE_ACTION);
      mContext.sendBroadcast(intent);
      return currentChannel;
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentChannel is null");
    }
    return null;
  }
}
