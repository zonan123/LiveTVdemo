/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.wwtv.setting.widget.detailui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mediatek.wwtv.setting.util.MenuConfigManager;

/**
 * An action within an {@link ActionAdapter}.
 */
public class Action implements Parcelable {

  private static final String TAG = "Action";

  public static final int NO_DRAWABLE = 0;
  public static final int NO_CHECK_SET = 0;
  public static final int DEFAULT_CHECK_SET_ID = 1;

  private String mKey;
  private String mTitle;
  private String mDescription;
  private Intent mIntent;

  public int satID;
  public String mLocationId;

  /**
   * If not {@code null}, the package name to use to retrieve {{@link #mDrawableResource}.
   */
  private String mResourcePackageName;

  private int mDrawableResource;
  private Uri mIconUri;
  private boolean mChecked;
  private boolean mMultilineDescription;
  private boolean mHasNext;
  private boolean mInfoOnly;
  private int mCheckSetId;
  private boolean mEnabled;
  private boolean mHasLeftRight;

  // add
  public String mItemID;// config id
  public DataType mDataType;// data Type
  // OptionView's parameter item name
  public String[] mOptionValue;
  // parameter's minValue of ProgressView ,PositionView
  public int mStartValue;
  // parameter's maxValue of ProgressView ,PositionView
  public int mEndValue;
  // ProgressView ,PositionView initial data and OptionView initial data
  public int mInitValue;
  public int mPrevInitValue;
  public int mStepValue; // progressbar step length
  // A cascade effect, a subclass of reference data kept the father
  public Action mParent;
  // A cascade effect, the parent class save it affected parameters of
  // collections of data
  public List<Action> mEffectGroup;
  // Used to identify a cascade effect, the parent parameters do not
  // produce a cascade effect of the value (image mode subscript user,
  // coordinate values 0) under
  // public int mNoEffectIndex;
  // SwichOptionView selection of a value for some of the parameters to
  // control it is available
  public Map<Integer, Boolean[]> mSwitchHashMap;

  // A cascade of options under the father can coordinate values
  // correspond to each subclasses parameters (such as image mode
  // configuration value, sports (2), with the value of the brightness of
  // the 50)
  public Map<Integer, int[]> mHashMap;

  public static Map<String, String[]> mHashMapForMax = new HashMap<String, String[]>();

  // With the many XiangZi configuration parameters configuration
  // parameters, such as HaveChildSubView
  public List<Action> mSubChildGroup;

  // Tape of a menu, keep a parent menu List are issued its reference, and
  // realize the flip back to the show
  public List<Action> mParentGroup;

  public boolean hasRealChild = true;
  private int inputLength = 3;
  private int userDefined = 0;

  public static final int SIGNAL_QUALITY = 1;
  public static final int SIGNAL_LEVEL = 2;
  private boolean supportModify = true;
  public int signalType;

  public boolean isSupportModify() {
    return supportModify;
  }

  public void setSupportModify(boolean supportModify) {
    this.supportModify = supportModify;
  }

  public enum DataType {
    OPTIONVIEW, POSITIONVIEW, PROGRESSBAR, HAVESUBCHILD, HAVETEXTSUBCHILD, INPUTBOX, EFFECTOPTIONVIEW, SWICHOPTIONVIEW, CHANNELLISTVIEW, TEXTCOMMVIEW, SCANCHANNELSOPTIONVIEW, NUMVIEW, NUMADJUSTVIEW, DATETIMEVIEW, CHANNELPOWERONVIEW, PASSWORDVIEW, FACTORYOPTIONVIEW, FACTORYPROGRESSVIEW, TVSOURCEVIEW, CHANNELPOWERNOCAHNNEL, SATELITEINFO, CHANNELEUEDIT
    , BISSITEMVIEW, BISSKEYVIEW, TKGSLOCITEMVIEW, EDITTEXTVIEW, LEFTRIGHTITEMVIEW// future code
    , TOPVIEW // the top menu action
    , LASTVIEW// the last menu action
    , SCANROOTVIEW// for all scan type's root action
    , DIALOGPOP// for the data type with a dialog pop
    , SATELITEDETAIL// for show satellite's detail view
    , LICENSEINFO
    , DEVICE_INFO
    , SCHEDULE_PVR
    , SAVEDATA
    ,LEFTRIGHT_HASDETAILVIEW
    , DISEQC12_SAVEINFO// for only diseqc12 press enter to save
    , LEFTRIGHT_VIEW// for press left/right to change value,press enter to call method. now only for
                    // diseqc12
    , LEFTRIGHT_HASCHILDVIEW// for press left/right to change value,press enter to show child item
                            // now only for diseqc12
  }

  /**
   * Builds a Action object.
   */
  public static class Builder {
    private String mKey;
    private String mTitle;
    private String mDescription;
    private Intent mIntent;
    private String mResourcePackageName;
    private int mDrawableResource = NO_DRAWABLE;
    private Uri mIconUri;
    private boolean mChecked;
    private boolean mMultilineDescription;
    private boolean mHasNext;
    private boolean mInfoOnly;
    private int mCheckSetId = NO_CHECK_SET;
    private boolean mEnabled = true;

    public Action build() {
      Action action = new Action();
      action.mKey = mKey;
      action.mTitle = mTitle;
      action.mDescription = mDescription;
      action.mIntent = mIntent;
      action.mResourcePackageName = mResourcePackageName;
      action.mDrawableResource = mDrawableResource;
      action.mIconUri = mIconUri;
      action.mChecked = mChecked;
      action.mMultilineDescription = mMultilineDescription;
      action.mHasNext = mHasNext;
      action.mInfoOnly = mInfoOnly;
      action.mCheckSetId = mCheckSetId;
      action.mEnabled = mEnabled;
      return action;
    }

    public Builder key(String key) {
      mKey = key;
      return this;
    }

    public Builder title(String title) {
      mTitle = title;
      return this;
    }

    public Builder description(String description) {
      mDescription = description;
      return this;
    }

    public Builder intent(Intent intent) {
      mIntent = intent;
      return this;
    }

    public Builder resourcePackageName(String resourcePackageName) {
      mResourcePackageName = resourcePackageName;
      return this;
    }

    public Builder drawableResource(int drawableResource) {
      mDrawableResource = drawableResource;
      return this;
    }

    public Builder iconUri(Uri iconUri) {
      mIconUri = iconUri;
      return this;
    }

    public Builder checked(boolean checked) {
      mChecked = checked;
      return this;
    }

    public Builder multilineDescription(boolean multilineDescription) {
      mMultilineDescription = multilineDescription;
      return this;
    }

    public Builder hasNext(boolean hasNext) {
      mHasNext = hasNext;
      return this;
    }

    public Builder infoOnly(boolean infoOnly) {
      mInfoOnly = infoOnly;
      return this;
    }

    public Builder checkSetId(int checkSetId) {
      mCheckSetId = checkSetId;
      return this;
    }

    public Builder enabled(boolean enabled) {
      mEnabled = enabled;
      return this;
    }
  }

  private Action() {
  }

  protected Action(String key, String title, String description, String resourcePackageName,
      int drawableResource, boolean checked, boolean multilineDescription, boolean hasNext,
      boolean infoOnly, Intent intent, int checkSetId) {
    mKey = key;
    mTitle = title;
    mDescription = description;
    mResourcePackageName = resourcePackageName;
    mDrawableResource = drawableResource;
    mChecked = checked;
    mMultilineDescription = multilineDescription;
    mHasNext = hasNext;
    mInfoOnly = infoOnly;
    mIntent = intent;
    mCheckSetId = checkSetId;
    mEnabled = true;
  }

  public Action(String mItemID, String mName, int mStartValue,
      int mEndValue, int mInitValue, String[] mOptionVaule,
      int mStepValue, DataType mDataType) {
    this.mItemID = mItemID;
    mKey = mItemID;
    mTitle = mName;
    mInfoOnly = false;
    mEnabled = true;
    this.mDataType = mDataType;

    if (mItemID
        .startsWith(MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL)) {
      this.mStartValue = mStartValue;
      this.mEndValue = mEndValue;
    }
    if (mItemID.equals(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE)) {
      this.mStartValue = mStartValue;
    }
    if (mItemID.equals(MenuConfigManager.TIME_END_TIME)
        || mItemID.equals(MenuConfigManager.TIME_END_DATE) ||
        mItemID.equals(MenuConfigManager.TIME_START_DATE)
        || mItemID.equals(MenuConfigManager.TIME_START_TIME)) {
      this.mInitValue = mInitValue;
    }
    // Item control is SeekBar, ProgressBar
    if (mDataType == DataType.POSITIONVIEW
        || mDataType == DataType.PROGRESSBAR
        || mDataType == DataType.NUMVIEW
        || mDataType == DataType.FACTORYPROGRESSVIEW
        || mDataType == DataType.NUMADJUSTVIEW) {
      this.mStartValue = mStartValue;
      this.mEndValue = mEndValue;
      this.mInitValue = mInitValue;
      this.mStepValue = mStepValue;
      // set description
      mDescription = "" + mInitValue;
    }
    // Item control is OptionView
    if (mDataType == DataType.OPTIONVIEW
        || mDataType == DataType.EFFECTOPTIONVIEW
        || mDataType == DataType.SWICHOPTIONVIEW
        || mDataType == DataType.CHANNELLISTVIEW
        || mDataType == DataType.TEXTCOMMVIEW
        || mDataType == DataType.CHANNELPOWERNOCAHNNEL
        || mDataType == DataType.SCANCHANNELSOPTIONVIEW
        || mDataType == DataType.CHANNELPOWERONVIEW
        || mDataType == DataType.FACTORYOPTIONVIEW
        || mDataType == DataType.FACTORYPROGRESSVIEW
        || mDataType == DataType.CHANNELEUEDIT
        || mDataType == DataType.TVSOURCEVIEW
        || mDataType == DataType.HAVETEXTSUBCHILD
        || mDataType == DataType.BISSITEMVIEW
        || mDataType == DataType.BISSKEYVIEW
        || mDataType == DataType.TKGSLOCITEMVIEW
        || mDataType == DataType.EDITTEXTVIEW
        || mDataType == DataType.LEFTRIGHTITEMVIEW
        || mDataType == DataType.SAVEDATA) {
      this.mOptionValue = mOptionVaule;
      this.mInitValue = mInitValue;
      // set description
      if (mOptionValue != null && mOptionValue.length > mInitValue && mInitValue >= 0) {
        mDescription = mOptionValue[mInitValue];
      }
    }
    if (mDataType == DataType.INPUTBOX || mDataType == DataType.NUMVIEW
        || mDataType == DataType.SATELITEINFO) {
      this.mOptionValue = mOptionVaule;
    }

    if (mDataType == DataType.LEFTRIGHT_HASCHILDVIEW ||
            mDataType == DataType.LEFTRIGHT_VIEW||mDataType == DataType.LEFTRIGHT_HASDETAILVIEW) {
          this.mOptionValue = mOptionVaule;
          this.mInitValue = mInitValue;
          // set description
          if (mOptionValue != null && mOptionValue.length > mInitValue) {
              mDescription = mOptionValue[mInitValue];
          }
          this.mHasLeftRight = true;
          if(mDataType == DataType.LEFTRIGHT_HASDETAILVIEW ){
              this.mDataType = DataType.HAVESUBCHILD;
          }
    }
  }

  public Action(String mItemId, String mName, DataType mDataType) {
    this.mItemID = mItemId;
    mKey = mItemID;
    mTitle = mName;
    this.mDataType = mDataType;
  }

  /**
   * Returns a list of {@link Action} with the specified keys and titles matched up.
   * <p>
   * The key and title arrays must be of equal length.
   */
  public static List<Action> createActionsFromArrays(String[] keys, String[] titles) {
    return createActionsFromArrays(keys, titles, NO_CHECK_SET, null);
  }

  /**
   * Returns a list of {@link Action} with the specified keys and titles matched up.
   * <p>
   * The key and title arrays must be of equal length.
   */
  public static List<Action> createActionsFromArrays(
      String[] keys, String[] titles, String checkedItemKey) {
    return createActionsFromArrays(keys, titles, DEFAULT_CHECK_SET_ID, checkedItemKey);
  }

  /**
   * Returns a list of {@link Action} with the specified keys and titles matched up and a given
   * check set ID so that they are related.
   * <p>
   * The key and title arrays must be of equal length.
   */
  public static List<Action> createActionsFromArrays(String[] keys, String[] titles,
      int checkSetId) {
    return createActionsFromArrays(keys, titles, checkSetId, null);
  }

  /**
   * Returns a list of {@link Action} with the specified keys and titles matched up and a given
   * check set ID so that they are related.
   * <p>
   * The key and title arrays must be of equal length.
   */
  public static List<Action> createActionsFromArrays(String[] keys, String[] titles,
      int checkSetId, String checkedItemKey) {
    int keysLength = keys.length;
    int titlesLength = titles.length;

    if (keysLength != titlesLength) {
      throw new IllegalArgumentException("Keys and titles dimensions must match");
    }

    List<Action> actions = new ArrayList<Action>();
    for (int i = 0; i < keysLength; i++) {
      Action.Builder builder = new Action.Builder();
      builder.key(keys[i]).title(titles[i]).checkSetId(checkSetId);
      if (checkedItemKey != null) {
        if (checkedItemKey.equals(keys[i])) {
          builder.checked(true);
        } else {
          builder.checked(false);
        }
      }
      Action action = builder.build();
      actions.add(action);
    }
    return actions;
  }

  public String getKey() {
    return mKey;
  }

  public String getTitle() {
    return mTitle;
  }

  public String getDescription() {
    return mDescription;
  }

  public void setDescription(int val) {
    if (this.mOptionValue != null && mOptionValue.length > val) {
      mDescription = this.mOptionValue[val];
    }
    if (mDataType == DataType.PROGRESSBAR || mDataType == DataType.POSITIONVIEW) {
      mDescription = "" + val;
    }
  }

  public void setDescription(String des) {
    mDescription = des;
  }

  public Intent getIntent() {
    return mIntent;
  }

  public boolean isChecked() {
    return mChecked;
  }

  public int getDrawableResource() {
    return mDrawableResource;
  }

  public Uri getIconUri() {
    return mIconUri;
  }

  public String getResourcePackageName() {
    return mResourcePackageName;
  }

  /**
   * Returns the check set id this action is a part of. All actions in the same list with the same
   * check set id are considered linked. When one of the actions within that set is selected that
   * action becomes checked while all the other actions become unchecked.
   *
   * @return an integer representing the check set this action is a part of or         {@NO_CHECK_SET
   *
   * } if this action isn't a part of a check set.
   */
  public int getCheckSetId() {
    return mCheckSetId;
  }

  public boolean hasMultilineDescription() {
    return mMultilineDescription;
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public void setChecked(boolean checked) {
    mChecked = checked;
  }

  public void setEnabled(boolean enabled) {
    mEnabled = enabled;
  }

  /**
   * @return true if the action will request further user input when selected (such as showing
   *         another dialog or launching a new activity). False, otherwise.
   */
  public boolean hasNext() {
    return mHasNext;
  }

  public void setHasNext(boolean mNext) {
    mHasNext = mNext;
  }

  public boolean hasLeftRight() {
    return mHasLeftRight;
  }

  /**
   * @return true if the action will only display information and is thus unactionable. If both this
   *         and {@link #hasNext()} are true, infoOnly takes precedence. (default is false) e.g.
   *         Play balance, or cost of an app.
   */
  public boolean infoOnly() {
    return mInfoOnly;
  }

  public String getmItemId() {
    return mItemID;
  }

  public void setmItemId(String mItemId) {
    this.mItemID = mItemId;
  }

  public DataType getmDataType() {
    return mDataType;
  }

  public void setmDataType(DataType mDataType) {
      this.mDataType = mDataType == DataType.LEFTRIGHT_HASDETAILVIEW ?DataType.HAVESUBCHILD:mDataType;
  }

  public String[] getmOptionValue() {
    return mOptionValue;
  }

  public void setmOptionValue(String[] mOptionValue) {
    this.mOptionValue = mOptionValue;
  }

  public int getmStartValue() {
    return mStartValue;
  }

  public void setmStartValue(int mStartValue) {
    this.mStartValue = mStartValue;
  }

  public int getmEndValue() {
    return mEndValue;
  }

  public void setmEndValue(int mEndValue) {
    this.mEndValue = mEndValue;
  }

  public int getmInitValue() {
    return mInitValue;
  }

  public void setmInitValue(int mInitValue) {
    this.mInitValue = mInitValue;
  }

  public int getmStepValue() {
    return mStepValue;
  }

  public void setmStepValue(int mStepValue) {
    this.mStepValue = mStepValue;
  }

  public Action getmParent() {
    return mParent;
  }

  public void setmParent(Action mParent) {
    this.mParent = mParent;
  }

  public List<Action> getmEffectGroup() {
    return mEffectGroup;
  }

  public void setmEffectGroup(List<Action> mEffectGroup) {
    this.mEffectGroup = mEffectGroup;
  }

  public Map<Integer, Boolean[]> getmSwitchHashMap() {
    return mSwitchHashMap;
  }

  public void setmSwitchHashMap(Map<Integer, Boolean[]> mSwitchHashMap) {
    this.mSwitchHashMap = mSwitchHashMap;
  }

  public List<Action> getmSubChildGroup() {
    return mSubChildGroup;
  }

  public void setmSubChildGroup(List<Action> mSubChildGroup) {
    this.mSubChildGroup = mSubChildGroup;
  }

  public List<Action> getmParentGroup() {
    return mParentGroup;
  }

  public void setmParentGroup(List<Action> mParentGroup) {
    this.mParentGroup = mParentGroup;
  }

  public String getmKey() {
    return mKey;
  }

  public void setmKey(String mKey) {
    this.mKey = mKey;
  }

  public String getmTitle() {
    return mTitle;
  }

  public void setmTitle(String mTitle) {
    this.mTitle = mTitle;
  }

  public Intent getmIntent() {
    return mIntent;
  }

  public void setmIntent(Intent mIntent) {
    this.mIntent = mIntent;
  }

  public boolean ismChecked() {
    return mChecked;
  }

  public void setmChecked(boolean mChecked) {
    this.mChecked = mChecked;
  }

  public int getmCheckSetId() {
    return mCheckSetId;
  }

  public void setmCheckSetId(int mCheckSetId) {
    this.mCheckSetId = mCheckSetId;
  }

  public int getInputLength() {
    return inputLength;
  }

  public void setInputLength(int inputLength) {
    this.inputLength = inputLength;
  }

  public int getUserDefined() {
    return userDefined;
  }

  public void setUserDefined(int defind) {
    userDefined = defind;
  }

  public /*static*/ interface OptionValuseChangedCallBack {
    void afterOptionValseChanged(String afterName);
  }

  private OptionValuseChangedCallBack mOptionChangedCallBack;

  public void setOptionValueChangedCallBack(
      OptionValuseChangedCallBack back) {
    mOptionChangedCallBack = back;
  }

  public OptionValuseChangedCallBack getCallBack() {
    return mOptionChangedCallBack;
  }

  public /*static*/ interface BeforeValueChangeCallback {
    void beforeValueChanged(int lastValue);
  }

  private BeforeValueChangeCallback mBeforeChangedCallBack;

  public void setBeforeChangedCallBack(
      BeforeValueChangeCallback back) {
    mBeforeChangedCallBack = back;
  }

  public BeforeValueChangeCallback getBeforeCallBack() {
    return mBeforeChangedCallBack;
  }

  /**
   * Returns an indicator to be drawn. If null is returned, no space for the indicator will be made.
   *
   * @param context the context of the Activity this Action belongs to
   * @return an indicator to draw or null if no indicator space should exist.
   */
  public Drawable getIndicator(Context context) {
    if (mDrawableResource == NO_DRAWABLE) {
      return null;
    }
    if (mResourcePackageName == null) {
      return context.getResources().getDrawable(mDrawableResource);
    }
    // If we get to here, need to load the resources.
    Drawable icon = null;
    try {
      Context packageContext = context.createPackageContext(mResourcePackageName, 0);
      icon = packageContext.getResources().getDrawable(mDrawableResource);
    } catch (NameNotFoundException e) {
      if (Log.isLoggable(TAG, Log.WARN)) {
        Log.w(TAG, "No icon for this action.");
      }
    }
    return icon;
  }

  public static Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {

    @Override
    public Action createFromParcel(Parcel source) {

      return new Action.Builder()
          .key(source.readString())
          .title(source.readString())
          .description(source.readString())
          .intent((Intent) source.readParcelable(Intent.class.getClassLoader()))
          .resourcePackageName(source.readString())
          .drawableResource(source.readInt())
          .iconUri((Uri) source.readParcelable(Uri.class.getClassLoader()))
          .checked(source.readInt() != 0)
          .multilineDescription(source.readInt() != 0)
          .checkSetId(source.readInt())
          .build();
    }

    @Override
    public Action[] newArray(int size) {
      return new Action[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mKey);
    dest.writeString(mTitle);
    dest.writeString(mDescription);
    dest.writeParcelable(mIntent, flags);
    dest.writeString(mResourcePackageName);
    dest.writeInt(mDrawableResource);
    dest.writeParcelable(mIconUri, flags);
    dest.writeInt(mChecked ? 1 : 0);
    dest.writeInt(mMultilineDescription ? 1 : 0);
    dest.writeInt(mCheckSetId);
  }
}
