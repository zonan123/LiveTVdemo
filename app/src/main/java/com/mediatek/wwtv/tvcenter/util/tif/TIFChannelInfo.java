
package com.mediatek.wwtv.tvcenter.util.tif;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;

import android.media.tv.TvContract;
import android.database.Cursor;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author sin_xinsheng
 */
public class TIFChannelInfo implements Serializable{

  /**
   * When a TIS doesn't provide any information about app link, and it doesn't have a leanback
   * launch intent, there will be no app link card for the TIS.
   */
  public static final int APP_LINK_TYPE_NONE = -1;
  /**
   * When a TIS provide a specific app link information, the app link card will be
   * {@code APP_LINK_TYPE_CHANNEL} which contains all the provided information.
   */
  public static final int APP_LINK_TYPE_CHANNEL = 1;
  /**
   * When a TIS doesn't provide a specific app link information, but the app has a leanback launch
   * intent, the app link card will be {@code APP_LINK_TYPE_APP} which launches the application.
   */
  public static final int APP_LINK_TYPE_APP = 2;

  private static final int APP_LINK_TYPE_NOT_SET = 0;

  public static final int LOAD_IMAGE_TYPE_CHANNEL_LOGO = 1;
  public static final int LOAD_IMAGE_TYPE_APP_LINK_ICON = 2;
  public static final int LOAD_IMAGE_TYPE_APP_LINK_POSTER_ART = 3;

  /** sqlite auto increment id, not same as tv api channel id */
  public long mId;
  public String mPackageName = "invalid package name";
  public String mInputServiceName;
  public String mType;
  public String mServiceType;
  public int mOriginalNetworkId;
  public int mTransportStreamId;
  public int mServiceId;
  public String mDisplayNumber;
  public String mDisplayName;
  public String mNetworkAffiliation;
  public String mDescription;
  public String mVideoFormat;
  public boolean mIsBrowsable;
  public boolean mSearchable;
  public boolean mLocked;
  public int mVersionNumber;
  public String mAppLinkIconUri;
  public String mAppLinkPosterArtUri;
  public String mAppLinkText;
  public int mAppLinkColor;
  public String mAppLinkIntentUri;
  public String mData;
  public int mInternalProviderFlag1;
  public int mInternalProviderFlag2;
  public int mInternalProviderFlag3;
  public int mInternalProviderFlag4;
  private Intent mAppLinkIntent;
  private int mAppLinkType;
  /**
   * int mSvlId = Integer.parseInt(value[1]); int mSvlRecId = Integer.parseInt(value[2]); unsigned
   * int mChannelId = Integer.parseInt(value[3]); int mHashcode = Integer.parseInt(value[4]); int
   * mKey = (mSvlId<<16)+mSvlRecId;
   */
  public long mDataValue[];
  public MtkTvChannelInfoBase mMtkTvChannelInfo;
  private static final String TAG = "TIFChannelInfo";
  @Override
  public String toString() {
    return "[TIFChannelInfo] mId:" + mId + ",  mInputServiceName:" + mInputServiceName
        + ",  mType:" + mType + ",  mServiceType:" + mServiceType
        + ",  mOriginalNetworkId:" + mOriginalNetworkId + ",  mTransportStreamId:"
        + mTransportStreamId
        + ",  mServiceId:" + mServiceId + ",  mDisplayNumber:" + mDisplayNumber
        + ",  mDisplayName:" + mDisplayName + ",  mNetworkAffiliation:" + mNetworkAffiliation
        + ",  mDescription:" + mDescription
        + ",  mVideoFormat:" + mVideoFormat + ",  mIsBrowsable:" + mIsBrowsable + ",  mSearchable:"
        + mSearchable
        + ",  mLocked:" + mLocked + ",  mVersionNumber:" + mVersionNumber + ",  mAppLinkIconUri:"
        + mAppLinkIconUri
        + ",  mAppLinkPosterArtUri:" + mAppLinkPosterArtUri + ",  mAppLinkText:" + mAppLinkText
        + ",  mAppLinkColor:" + mAppLinkColor
        + ",  mAppLinkIntentUri:" + mAppLinkIntentUri + ",  mAppLinkIntent:" + mAppLinkIntent
        + ",  mInternalProviderFlag1:"
        + mInternalProviderFlag1 + ",  mInternalProviderFlag2:" + mInternalProviderFlag2
        + ",  mInternalProviderFlag3:" + mInternalProviderFlag3 + ",  mInternalProviderFlag4:"
        + mInternalProviderFlag4
        + ",  mData:" + mData;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TIFChannelInfo) {
      TIFChannelInfo other = (TIFChannelInfo) o;
      if (other.mId == this.mId) {
        return true;
      }
    }
    return super.equals(o);
  }
@Override
public int hashCode() {
    return 1;
}
  /**
   * Returns the type of app link for this channel. It returns {@link #APP_LINK_TYPE_CHANNEL} if the
   * channel has a non null app link text and a valid app link intent, it returns
   * {@link #APP_LINK_TYPE_APP} if the input service which holds the channel has leanback launch
   * intent, and it returns {@link #APP_LINK_TYPE_NONE} otherwise.
   */
  public int getAppLinkType(Context context) {
    if (mAppLinkType == APP_LINK_TYPE_NOT_SET) {
      initAppLinkTypeAndIntent(context);
    }
    return mAppLinkType;
  }

  /**
   * Returns the app link intent for this channel. If the type of app link is
   * {@link #APP_LINK_TYPE_NONE}, it returns {@code null}.
   */
  public Intent getAppLinkIntent(Context context) {
    if (mAppLinkType == APP_LINK_TYPE_NOT_SET) {
      initAppLinkTypeAndIntent(context);
    }
    return mAppLinkIntent;
  }

  private void initAppLinkTypeAndIntent(Context context) {
    mAppLinkType = APP_LINK_TYPE_NONE;
    mAppLinkIntent = null;
    PackageManager pm = context.getPackageManager();
    if (!TextUtils.isEmpty(mAppLinkText) && !TextUtils.isEmpty(mAppLinkIntentUri)) {
      try {
        Intent intent = Intent.parseUri(mAppLinkIntentUri, Intent.URI_INTENT_SCHEME);
        if (intent.resolveActivityInfo(pm, 0) != null) {
          mAppLinkIntent = intent;
          mAppLinkIntent.putExtra(TIFFunctionUtil.EXTRA_APP_LINK_CHANNEL_URI,
              getUri().toString());
          mAppLinkType = APP_LINK_TYPE_CHANNEL;
          return;
        }
      } catch (URISyntaxException e) {
          e.printStackTrace();
      }
    }
    if (mPackageName.equals(context.getApplicationContext().getPackageName())) {
      return;
    }
    mAppLinkIntent = pm.getLeanbackLaunchIntentForPackage(mPackageName);
    if (mAppLinkIntent != null) {
      mAppLinkIntent.putExtra(TIFFunctionUtil.EXTRA_APP_LINK_CHANNEL_URI,
          getUri().toString());
      mAppLinkType = APP_LINK_TYPE_APP;
    }
  }

    public Uri getUri() {
       // if (true) {
        return TvContract.buildChannelUriForPassthroughInput(mInputServiceName);
      /*  } else {
            return TvContract.buildChannelUri(mId);
        }*/
    }

    public long getId(){
        return mId;
    }

    public boolean isBrowsable(){
        return mIsBrowsable;
    }

    public String getDisplayNumber(){
        return mDisplayNumber;
    }

    public String getImageUriString(int type) {
        switch (type) {
            case LOAD_IMAGE_TYPE_CHANNEL_LOGO:
                return TvContract.buildChannelLogoUri(mId).toString();
            case LOAD_IMAGE_TYPE_APP_LINK_ICON:
                return mAppLinkIconUri;
            case LOAD_IMAGE_TYPE_APP_LINK_POSTER_ART:
                return mAppLinkPosterArtUri;
            default:
                break;
        }
        return null;
    }

    public boolean hasSameReadOnlyInfo(TIFChannelInfo other) {
        return other != null
                && Objects.equals(mId, other.mId)
                && Objects.equals(mPackageName, other.mPackageName)
                && Objects.equals(mInputServiceName, other.mInputServiceName)
                && Objects.equals(mType, other.mType)
                && Objects.equals(mDisplayNumber, other.mDisplayNumber)
                && Objects.equals(mDisplayName, other.mDisplayName)
                && Objects.equals(mDescription, other.mDescription)
                && Objects.equals(mVideoFormat, other.mVideoFormat)
                && Objects.equals(mAppLinkText, other.mAppLinkText)
                && mAppLinkColor == other.mAppLinkColor
                && Objects.equals(mAppLinkIconUri, other.mAppLinkIconUri)
                && Objects.equals(mAppLinkPosterArtUri, other.mAppLinkPosterArtUri)
                && Objects.equals(mAppLinkIntentUri, other.mAppLinkIntentUri);
    }

    void copyFrom(TIFChannelInfo other) {
        if (this == other) {
            return;
        }

        this.mId                    = other.mId;
        this.mPackageName           = other.mPackageName;
        this.mInputServiceName      = other.mInputServiceName;
        this.mType                  = other.mType;
        this.mServiceType           = other.mServiceType;
        this.mOriginalNetworkId     = other.mOriginalNetworkId;
        this.mTransportStreamId     = other.mTransportStreamId;
        this.mServiceId             = other.mServiceId;
        this.mDisplayNumber         = other.mDisplayNumber;
        this.mDisplayName           = other.mDisplayName;
        this.mNetworkAffiliation    = other.mNetworkAffiliation;
        this.mDescription           = other.mDescription;
        this.mVideoFormat           = other.mVideoFormat;
        this.mIsBrowsable           = other.mIsBrowsable;
        this.mSearchable            = other.mSearchable;
        this.mLocked                = other.mLocked;
        this.mVersionNumber         = other.mVersionNumber;
        this.mAppLinkIconUri        = other.mAppLinkIconUri;
        this.mAppLinkPosterArtUri   = other.mAppLinkPosterArtUri;
        this.mAppLinkText           = other.mAppLinkText;
        this.mAppLinkColor          = other.mAppLinkColor;
        this.mAppLinkIntentUri      = other.mAppLinkIntentUri;
        this.mInternalProviderFlag1 = other.mInternalProviderFlag1;
        this.mInternalProviderFlag2 = other.mInternalProviderFlag2;
        this.mInternalProviderFlag3 = other.mInternalProviderFlag3;
        this.mInternalProviderFlag4 = other.mInternalProviderFlag4;
        this.mData                  = other.mData;
        this.mDataValue             = other.mDataValue;
    }

    /*
     * parser public property
     */
    public static TIFChannelInfo parse(Cursor c) {
        TIFChannelInfo info = new TIFChannelInfo();
        parse(info, c);
        return info;
    }

    /*
     * parser public property
     */
    public static void parse(TIFChannelInfo temTIFChannel, Cursor c) {
        if(temTIFChannel == null || c == null){
            return ;
        }

        temTIFChannel.mId = c.getLong(
            c.getColumnIndex(TvContract.Channels._ID));
        temTIFChannel.mPackageName = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_PACKAGE_NAME));
        temTIFChannel.mInputServiceName = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_INPUT_ID));
        temTIFChannel.mType = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_TYPE));
        temTIFChannel.mServiceType = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_TYPE));
        temTIFChannel.mOriginalNetworkId = c.getInt(
            c.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID));
        temTIFChannel.mTransportStreamId = c.getInt(
            c.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID));
        temTIFChannel.mServiceId = c.getInt(
            c.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID));
        if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
            temTIFChannel.mDisplayNumber = ""+CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(c.getString(c
                       .getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)));
       }else{
            temTIFChannel.mDisplayNumber = c.getString(c
                   .getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER));
       }
        temTIFChannel.mDisplayName =c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME));
        if (temTIFChannel.mDisplayName == null) {
            temTIFChannel.mDisplayName = "";
        } else {
            temTIFChannel.mDisplayName =
                TvSingletons.getSingletons().getCommonIntegration().
                    getAvailableString(temTIFChannel.mDisplayName);
        }
        temTIFChannel.mNetworkAffiliation = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_NETWORK_AFFILIATION));
        temTIFChannel.mDescription = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_DESCRIPTION));
        temTIFChannel.mVideoFormat = c.getString(
            c.getColumnIndex(TvContract.Channels.COLUMN_VIDEO_FORMAT));

        int isBrowsable = c.getInt(
            c.getColumnIndex(TvContract.Channels.COLUMN_BROWSABLE));
        if (isBrowsable == 1) {
            temTIFChannel.mIsBrowsable = true;
        } else {
            temTIFChannel.mIsBrowsable = false;
        }

        isBrowsable = c.getInt(c.getColumnIndex(TvContract.Channels.COLUMN_SEARCHABLE));
        if (isBrowsable == 1) {
            temTIFChannel.mSearchable = true;
        } else {
            temTIFChannel.mSearchable = false;
        }

        isBrowsable = c.getInt(c.getColumnIndex(TvContract.Channels.COLUMN_LOCKED));
        if (isBrowsable == 1) {
            temTIFChannel.mLocked = true;
        } else {
            temTIFChannel.mLocked = false;
        }

        temTIFChannel.mVersionNumber = c.getInt(
            c.getColumnIndex(TvContract.Channels.COLUMN_VERSION_NUMBER));
        if (c.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_ICON_URI) > -1) {
            temTIFChannel.mAppLinkIconUri = c.getString(
                c.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_ICON_URI));
            temTIFChannel.mAppLinkPosterArtUri = c.getString(
                c.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_POSTER_ART_URI));
            temTIFChannel.mAppLinkText = c.getString(
                c.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_TEXT));
            temTIFChannel.mAppLinkColor = c.getInt(
                c.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_COLOR));
            temTIFChannel.mAppLinkIntentUri = c.getString(
                c.getColumnIndex(TvContract.Channels.COLUMN_APP_LINK_INTENT_URI));
            temTIFChannel.mInternalProviderFlag1 = c.getInt(
                c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_FLAG1));
            temTIFChannel.mInternalProviderFlag2 = c.getInt(
                c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_FLAG2));
            temTIFChannel.mInternalProviderFlag3 = c.getInt(
                c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_FLAG3));
            temTIFChannel.mInternalProviderFlag4 = c.getInt(
                c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_FLAG4));
        }

        try {
            byte[] mData = c.getBlob(c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA));
            if (mData != null && mData.length !=0){
	            temTIFChannel.mData = new String(mData);
	            parserTIFChannelData(temTIFChannel,temTIFChannel.mData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // printProviderInfo(temTIFChannel);
    }
    public static void parserTIFChannelData(TIFChannelInfo temTIFChannel, String data) {

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "data:" + data);
        if (data == null) {
          return ;
        }

        String value[] = data.split(",");
        if (!(value.length == TIFFunctionUtil.channelDataValuelength || value.length  ==6)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parserTIFChannelData data.length != 6 or 10" );
          return ;
        }
        int length = value.length;
        long v[] = new long[length];
        long mSvlId = Long.parseLong(value[1]);
        long mSvlRecId = Long.parseLong(value[2]);
        long channelId = Long.parseLong(value[3]);
        // int mHashcode = Integer.parseInt(value[4]);
        //long mKey = (mSvlId << 16) + mSvlRecId;
        v[0] = mSvlId;
        v[1] = mSvlRecId;
        v[2] = channelId;
        // v[3] = mHashcode;
        v[4] = (mSvlId << 16) + mSvlRecId;
/*        value[5]  //servicetype
        value[6]  //newmask
        value[7]  //maskopt1
        value[8]  //maskopt2
        value[9]  //sdtserviecetype
*/        for(int i=5;i<value.length;i++){
            v[i] =Integer.parseInt(value[i]);
        }
        temTIFChannel.mDataValue = v;
      }

    /**
     * The API is used to confirm if this channel is deleted by user.
     * @return true (deleted by user)/false (not deleted by user)
     */
     public boolean isUserDelete()
     {
        if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
            return ((mDataValue[7] & MtkTvChCommonBase.SB_VOPT_DELETED_BY_USER) > 0) ? true : false;
        }
        return false;
     }

     /**
     * The API is used to confirm if this channel is visible.
     * @return true (visible)/false (invisible)
     */
     public boolean isVisible()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_VISIBLE) > 0) ? true : false;
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is visible in EPG.
     * @return true (visible in EPG)/false (invisible in EPG)
     */
     public boolean isEpgVisible()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_EPG) > 0) ? true : false;
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a radio service.
     * @return true (Radio)/false (Not Radio)
     */
     public boolean isRadioService()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_RADIO_SERVICE) > 0) ? true : false;
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a analog service.
     * @return true (analog)/false (Not analog)
     */
     public boolean isAnalogService()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE) > 0) ? true : false;
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a TV service.
     * @return true (TV service)/false (Not TV service)
     */
     public boolean isTvService()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_TV_SERVICE) > 0) ? true : false;
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a service of Favorites1.
     * @return true (Favorites1 service)/false (Not Favorites1 service)
     */
     public boolean isDigitalFavorites1Service()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_FAVORITE1) > 0);
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a service of Favorites2.
     * @return true (Favorites2 service)/false (Not Favorites2 service)
     */
     public boolean isDigitalFavorites2Service()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_FAVORITE2) > 0);
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a service of Favorites3.
     * @return true (Favorites3 service)/false (Not Favorites3 service)
     */
     public boolean isDigitalFavorites3Service()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_FAVORITE3) > 0);
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a service of Favorites4.
     * @return true (Favorites4 service)/false (Not Favorites4 service)
     */
     public boolean isDigitalFavorites4Service()
     {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_FAVORITE4) > 0);
         }
         return false;
     }

     /**
     * The API is used to confirm if this channel is a digital Favorites service.
     * @return true (digital Favorites service)/false (Not digital Favorites service)
     */
     public boolean isDigitalFavoritesService()
     {
         return (this.isDigitalFavorites1Service() ||
                 this.isDigitalFavorites2Service() ||
                 this.isDigitalFavorites3Service() ||
                 this.isDigitalFavorites4Service());
     }

     /**
     * This API return whether the channel skipped status.
     * @return return whether the channel skipped status: true, skipped; false , not skipped
     */
     public boolean isSkip() {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_VISIBLE) > 0) ? false : true;
         }
         return false;
     }

     public boolean isNumberSelectable() {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_NUMERIC_SELECTABLE) > 0) ? true : false;
         }
         return false;
     }


     /**
     * This API return whether the channel isBlock status.
     * @return return whether the channel isBlock status: true, Block; false , not Block
     */
     public boolean isBlock() {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[6] & MtkTvChCommonBase.SB_VNET_BLOCKED) > 0) ? true : false;
         }
         return false;
     }


     /**
     * This API return whether the channel isUserTmpLock status.
     * @return return whether the channel isUserTmpLock status: true, UserTmpLock; false , not UserTmpLock
     */
     public boolean isUserTmpLock() {
         if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
             return ((mDataValue[7] & MtkTvChCommonBase.SB_VOPT_USER_TMP_UNLOCK) > 0) ? true : false;
         }
         return false;

     }
     /**
      * This API return whether the channel hd status.
      * @return return whether the channel hd status: true, hd; false , not hd
      */
      public boolean isHDchannel() {
          if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
              return ((mDataValue[9] & CommonIntegration.CH_SDT_SERVICE_TYPE_HD) == CommonIntegration.CH_SDT_SERVICE_TYPE_HD) ? true : false;
          }
          return false;

      }

      /**
       * This API return whether the channel InterActive status.
       * @return return whether the channel InterActive status: true, InterActive; false , not InterActive
       */
       public boolean isInterActive() {
           if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
               return (mDataValue[9] == CommonIntegration.CH_SDT_SERVICE_TYPE_INTERACTIVE) ? true : false;
           }
           return false;

       }
       /**
        * This API return whether the channel Radio status.
        * @return return whether the channel Radio status: true, Radio; false , not Radio
        */
        public boolean isRadiochannel() {
            if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                return (mDataValue[9] == CommonIntegration.CH_SDT_SERVICE_TYPE_RADIO
                        || mDataValue[9] == CommonIntegration.CH_SDT_SERVICE_TYPE_RADIO2) ? true : false;
            }
            return false;
        }
        /**
         * This API return whether the channel network status.
         * @return return whether the channel network status: true, network; false , not network
         */
         public boolean isIPAPPchannel() {
           //  if (mDataValue ==null || mDataValue.length != TIFFunctionUtil.channelDataValuelength){
                 return (mDataValue ==null || mDataValue.length != TIFFunctionUtil.channelDataValuelength)? true:false;
           
         }
    public boolean isIPChannel() {
        if (mDataValue !=null && mDataValue.length == TIFFunctionUtil.channelDataValuelength){
            return (mDataValue[5] == CommonIntegration.SVL_SERVICE_TYPE_IP_SVC) ? true : false;
        }
        return false;
    }
/*
    private static void printProviderInfo(TIFChannelInfo temTIFChannel) {
        android.util.Log.d("TIFChannelInfo",
            "parserTIFRowChannelInfo[temTIFChannel.mId:" + temTIFChannel.mId
          + "  temTIFChannel.mInputServiceName:" + temTIFChannel.mInputServiceName
          + "  temTIFChannel.mType:" + temTIFChannel.mType
          + "  temTIFChannel.mServiceType:" + temTIFChannel.mServiceType
          + "  temTIFChannel.mOriginalNetworkId:" + temTIFChannel.mOriginalNetworkId
          + "  temTIFChannel.mTransportStreamId:" + temTIFChannel.mTransportStreamId
          + "  temTIFChannel.mServiceId:" + temTIFChannel.mServiceId
          + "  temTIFChannel.mDisplayNumber:" + temTIFChannel.mDisplayNumber
          + "  temTIFChannel.mDisplayName:" + temTIFChannel.mDisplayName
          + "  temTIFChannel.mNetworkAffiliation:" + temTIFChannel.mNetworkAffiliation
          + "  temTIFChannel.mDescription:" + temTIFChannel.mDescription
          + "  temTIFChannel.mVideoFormat:" + temTIFChannel.mVideoFormat
          + "  temTIFChannel.mIsBrowsable:" + temTIFChannel.mIsBrowsable
          + "  temTIFChannel.mSearchable:" + temTIFChannel.mSearchable
          + "  temTIFChannel.mLocked:" + temTIFChannel.mLocked
          + "  temTIFChannel.mVersionNumber:" + temTIFChannel.mVersionNumber
          + "  temTIFChannel.mData:" + temTIFChannel.mData);
    }
*/

    public static class CustomerComparator implements Comparator<TIFChannelInfo> {
        private final Context mContext;
        private int mSortType;
        private boolean mDetectDuplicatesEnabled = false;
     /*   <item>Default</item>
        <item>Name Up</item>
        <item>Name Down</item>
        <item>Encrypted</item>
        <item>DTV/DATA/RADIO/ATV</item>
        <item>HD/SD</item>
        <item>Broadcast/Network</item>**/
        public CustomerComparator(Context context, int sortType) {
            mContext = context;
            mSortType = sortType;
        }

        public void setSortType(int sortType) {
            mSortType = sortType;
        }
        public void setDetectDuplicatesEnabled(boolean detectDuplicatesEnabled) {
            mDetectDuplicatesEnabled = detectDuplicatesEnabled;
        }
        
        private int compareByNumber(String leftNum,String rightNum){
            String[] lhsNumberlist=leftNum.split("-");
            String[] rhsNumberlist=rightNum.split("-");
            if(lhsNumberlist.length ==1 && rhsNumberlist.length==1){
              return  channelnum(lhsNumberlist[0])-channelnum(rhsNumberlist[0]);
            }else if(lhsNumberlist.length ==1 && rhsNumberlist.length >1){
               if(channelnum(lhsNumberlist[0]) == channelnum(rhsNumberlist[0])){
                   return 0-channelnum(rhsNumberlist[1]);
               }else{
                   return channelnum(lhsNumberlist[0])-channelnum(rhsNumberlist[0]);
               }
            }else if(lhsNumberlist.length >1 && rhsNumberlist.length == 1){
                if(channelnum(lhsNumberlist[0])== channelnum(rhsNumberlist[0])){
                    return channelnum(lhsNumberlist[1])-0;
                }else{
                    return channelnum(lhsNumberlist[0])- channelnum(rhsNumberlist[0]);
                }
            }else if(lhsNumberlist.length >1 && rhsNumberlist.length > 1){
                if(channelnum(lhsNumberlist[0])== channelnum(rhsNumberlist[0])){
                    return channelnum(lhsNumberlist[1])- channelnum(rhsNumberlist[1]);
                }else{
                    return channelnum(lhsNumberlist[0])- channelnum(rhsNumberlist[0]);
                }
            }
           return 0;
        }

        @Override
        public int compare(TIFChannelInfo lhs, TIFChannelInfo rhs) {
            if (lhs != null && lhs.equals(rhs)) {
                return 0;
            }else if (lhs == null && rhs != null){
                return 1;
            }else if(lhs != null && rhs == null){
                return -1;
            }else if (lhs == null && rhs == null){
                return 0;
            }

            if(mSortType == 0){    //nav_select_sort_default
                if(lhs.mDisplayNumber ==null || rhs.mDisplayNumber== null){
                    return 0;
                }
               return compareByNumber(lhs.mDisplayNumber,rhs.mDisplayNumber);
            }else if(mSortType == 1){   //   nav_select_sort_nameup
                return  lhs.mDisplayName.compareTo(rhs.mDisplayName);
            }else if(mSortType == 2){ // nav_select_sort_namedown
                return  rhs.mDisplayName.compareTo(lhs.mDisplayName);
            }else if(mSortType == 3){   // nav_select_sort_encrypted
                if(lhs.mMtkTvChannelInfo != null && rhs.mMtkTvChannelInfo != null){
                   boolean lhsScrambled= TIFFunctionUtil.checkChMask(lhs.mMtkTvChannelInfo, TIFFunctionUtil.CH_SCRAMBLED_MASK, TIFFunctionUtil.CH_SCRAMBLED_VAL);
                   boolean rhsScrambled= TIFFunctionUtil.checkChMask(rhs.mMtkTvChannelInfo, TIFFunctionUtil.CH_SCRAMBLED_MASK, TIFFunctionUtil.CH_SCRAMBLED_VAL);
                   if(lhsScrambled && rhsScrambled){
                       return 0;
                   }
                   if(lhsScrambled){
                       return -1;
                   }
                   if(rhsScrambled){
                       return 1;
                   }
                    return 0;
                }else if(lhs.mMtkTvChannelInfo == null && rhs.mMtkTvChannelInfo != null){
                    boolean rhsScrambled= TIFFunctionUtil.checkChMask(rhs.mMtkTvChannelInfo, TIFFunctionUtil.CH_SCRAMBLED_MASK, TIFFunctionUtil.CH_SCRAMBLED_VAL);
                    if(rhsScrambled){
                        return 1;
                    }
                }
                return 0;
            }else if(mSortType == 4){ // nav_select_sort_ddra
                if(lhs.mMtkTvChannelInfo != null && rhs.mMtkTvChannelInfo != null){
                    if(lhs.mMtkTvChannelInfo.getServiceType() == rhs.mMtkTvChannelInfo.getServiceType()){
                        return 0;
                    }
                    if( lhs.mMtkTvChannelInfo.getServiceType() == 1){
                        return -1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() == 1){
                        return 1;
                    }
                    if( lhs.mMtkTvChannelInfo.getServiceType() == 3 || lhs.mMtkTvChannelInfo.getServiceType() == 13
                            || lhs.mMtkTvChannelInfo.getServiceType() == 15){
                        return -1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() == 3 || rhs.mMtkTvChannelInfo.getServiceType() == 13
                            || rhs.mMtkTvChannelInfo.getServiceType() == 15){
                        return 1;
                    }
                    if( lhs.mMtkTvChannelInfo.getServiceType() == 2){
                        return -1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() == 2){
                        return 1;
                    }
                    if( lhs.mMtkTvChannelInfo.getServiceType() != 0 && TIFFunctionUtil.checkChMask(lhs.mMtkTvChannelInfo, TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_ANALOG_VAL)){
                        return -1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() != 0 && TIFFunctionUtil.checkChMask(rhs.mMtkTvChannelInfo,  TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_ANALOG_VAL)){
                        return 1;
                    }
                    if( lhs.mMtkTvChannelInfo.getServiceType() == 0 || rhs.mMtkTvChannelInfo.getServiceType() == 0  ){
                        return -1;
                    }

                }else if(lhs.mMtkTvChannelInfo == null && rhs.mMtkTvChannelInfo != null){
                    if( rhs.mMtkTvChannelInfo.getServiceType() == 1){
                        return 1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() == 3 || rhs.mMtkTvChannelInfo.getServiceType() == 13
                            || rhs.mMtkTvChannelInfo.getServiceType() == 15){
                        return 1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() == 2){
                        return 1;
                    }
                    if( rhs.mMtkTvChannelInfo.getServiceType() != 0 && TIFFunctionUtil.checkChMask(rhs.mMtkTvChannelInfo,  TIFFunctionUtil.CH_LIST_ANALOG_MASK, TIFFunctionUtil.CH_LIST_ANALOG_VAL)){
                        return 1;
                    }
                }
                return 0;
            }else if(mSortType == 5){//nav_select_sort_hs
                if(lhs.mDataValue ==null && rhs.mDataValue !=null){
                    return 1;    
                }else if(lhs.mDataValue !=null && rhs.mDataValue ==null){
                    return -1;
                }else if(lhs.mDataValue ==null && rhs.mDataValue ==null){
                    return 0;
                }
                if(lhs.mDataValue.length == TIFFunctionUtil.channelDataValuelength && rhs.mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                        boolean lhssd= lhs.mDataValue[9]== 0x1B || lhs.mDataValue[9] == 0x19 || lhs.mDataValue[9]== 0x11 
                                || lhs.mDataValue[9] == 0x20 || lhs.mDataValue[9] == 0x1F;
                        boolean rhssd= rhs.mDataValue[9]== 0x1B || rhs.mDataValue[9]== 0x19 || rhs.mDataValue[9]== 0x11
                                || rhs.mDataValue[9] == 0x20 || rhs.mDataValue[9]== 0x1F;
                        
                        if(lhssd && rhssd){
                            return compareByNumber(lhs.mDisplayNumber,rhs.mDisplayNumber);
                        }
                        if(lhssd){
                            return -1;
                        }
                        if(rhssd){
                            return 1;
                        } 
                        return compareByNumber(lhs.mDisplayNumber,rhs.mDisplayNumber);
                 }else if(lhs.mDataValue.length != TIFFunctionUtil.channelDataValuelength && rhs.mDataValue.length == TIFFunctionUtil.channelDataValuelength){
                         boolean rhshd= rhs.mDataValue[9]== 0x20 || rhs.mDataValue[9]== 0x1F;
                         if( rhshd){
                             return 1;
                         }
                         boolean rhssd=rhs.mDataValue[9]== 0x1B || rhs.mDataValue[9]== 0x19 || rhs.mDataValue[9]== 0x11;
                         if( rhssd){
                             return 1;
                         }
                         return 1;
                 }else if (lhs.mMtkTvChannelInfo != null && rhs.mMtkTvChannelInfo == null){
                    return -1;
                }else{
                     return 0; 
                 }
            }else if (mSortType == 6){
                    if (TIFFunctionUtil.is3rdTVSource(lhs) && !TIFFunctionUtil.is3rdTVSource(rhs) )
                    {
                        return 1;
                    }else if ( (TIFFunctionUtil.is3rdTVSource(lhs) && TIFFunctionUtil.is3rdTVSource(rhs)) || ( !TIFFunctionUtil.is3rdTVSource(lhs) && !TIFFunctionUtil.is3rdTVSource(rhs) ) ){
                        return compareByNumber(lhs.mDisplayNumber,rhs.mDisplayNumber);
                    }else{
                        return -1;
                    }
            }
            return 0;
        }

        private int channelnum(String numstr) {
            if(numstr !=null && numstr.trim().length() >0){
                return  (int) Double.parseDouble(numstr.trim());
            }
            return 0;
        }
    }
}
