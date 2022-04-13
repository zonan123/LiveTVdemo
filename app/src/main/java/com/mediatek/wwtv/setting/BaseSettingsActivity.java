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

package com.mediatek.wwtv.setting;

import com.mediatek.wwtv.setting.AsyncLoader.DataLoadListener;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.SpecialOptionDealer;

import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.setting.widget.detailui.DialogActivity;
//import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.tvcenter.R;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class BaseSettingsActivity extends DialogActivity implements DataLoadListener {
  private static final String TAG = "BaseSettings";
  protected Object mState;
  protected Stack<Object> mStateStack = new Stack<Object>();
  protected Stack<Action> mActionLevelStack = new Stack<Action>();
  protected Resources mResources;
  protected Fragment mContentFragment;
  protected Fragment mActionFragment;
  protected List<Action> mActions;
  protected Action mCurrAction;
  protected Action mParentAction;
  protected MenuDataHelper mDataHelper;
  protected MenuConfigManager mConfigManager;
  protected TVContent mTV;
  protected CommonIntegration mIntegration;
  protected SpecialOptionDealer mSpecialOptionDealer;
  AsyncLoader mALoader;
  protected WindowManager.LayoutParams baseLp = null;

  /**
   * This method initializes the parameter and sets the initial state. <br/>
   * Activities extending {@link BaseSettingsActivity} should initialize their own local variables,
   * if any, before calling {@link #onCreate}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mResources = getResources();
    mActions = new ArrayList<Action>();
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    WindowManager.LayoutParams lp = getWindow().getAttributes();
    lp.width = (int) (SettingsUtil.SCREEN_WIDTH * 1.0);
    lp.height = (int) (SettingsUtil.SCREEN_HEIGHT * 1.0);
    lp.gravity = Gravity.RIGHT;
    baseLp = lp;
    this.getWindow().setAttributes(lp);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseSettingsActivity", "resume time00==" + System.currentTimeMillis());
    mDataHelper = MenuDataHelper.getInstance(this);
    mConfigManager = MenuConfigManager.getInstance(this);
    mTV = TVContent.getInstance(this);
    mIntegration = CommonIntegration.getInstanceWithContext(this);
    mALoader = AsyncLoader.getInstance();
    mALoader.bindDataLoadListener(this);
    setState(getInitialDataType(), true);
  }

  @Override
  protected void onResume() {
    super.onResume();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseSettingsActivity", "resume time==" + System.currentTimeMillis());
    SettingsUtil.loadStatus = SettingsUtil.LOAD_STATUS_FINISH;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    mResources = getResources();
    mActions = new ArrayList<Action>();
    mDataHelper = MenuDataHelper.getInstance(this);
    mConfigManager = MenuConfigManager.getInstance(this);
    mTV = TVContent.getInstance(this);
    mIntegration = CommonIntegration.getInstanceWithContext(this);
    setState(getInitialDataType(), true);
    super.onConfigurationChanged(newConfig);
  }

  protected abstract Object getInitialDataType();

  protected void setState(Object state, boolean updateStateStack) {
    if (updateStateStack && mState != null) {
      mStateStack.push(mState);
    }
    mState = state;

    updateView();
  }

  protected void setView(int titleResId, int breadcrumbResId, int descResId, int iconResId) {
    String title = titleResId != 0 ? mResources.getString(titleResId) : null;
    String breadcrumb = breadcrumbResId != 0 ? mResources.getString(breadcrumbResId) : null;
    String description = descResId != 0 ? mResources.getString(descResId) : null;
    setView(title, breadcrumb, description, iconResId);
  }

  protected void setView(String title, String breadcrumb, String description, int iconResId) {
    mContentFragment = ContentFragment.newInstance(title, breadcrumb, description, iconResId,
        getResources().getColor(R.color.icon_background));
    mActionFragment = ActionFragment.newInstance(mActions);
    setContentAndActionFragments(mContentFragment, mActionFragment);
  }

  /**
   * Set the view.
   *
   * @param uri Uri of icon resource.
   */
  protected void setView(String title, String breadcrumb, String description, Uri uri) {
    mContentFragment = ContentFragment.newInstance(title, breadcrumb, null, uri,
        getResources().getColor(R.color.icon_background));
    mActionFragment = ActionFragment.newInstance(mActions);
    setContentAndActionFragments(mContentFragment, mActionFragment);
  }

  protected void setView(int titleResId, String breadcrumb, int descResId, Uri uri) {
    String title = titleResId != 0 ? mResources.getString(titleResId) : null;
    String description = descResId != 0 ? mResources.getString(descResId) : null;
    setView(title, breadcrumb, description, uri);
  }

  /**
   * This method is called by {@link #setState}, and updates the layout based on the current state
   */
  protected abstract void updateView();

  /**
   * This method is called to update the contents of mActions to reflect the list of actions for the
   * current state.
   */
  protected void refreshActionList() {
    mActions.clear();
    switch ((Action.DataType) mState) {
      case TOPVIEW:
        mALoader.execute(null);
        // setActionsForTopView();
        break;
      case OPTIONVIEW:
      case SWICHOPTIONVIEW:
      case EFFECTOPTIONVIEW:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseSettings", "mCurrAction.mOptionValue.length=="
            + mCurrAction.mOptionValue.length);
        for (int i = 0; i < mCurrAction.mOptionValue.length; i++) {
          String newId = mCurrAction.mItemID + SettingsUtil.OPTIONSPLITER + i;
          Action option = new Action(newId, mCurrAction.mOptionValue[i],
              MenuConfigManager.INVALID_VALUE,
              MenuConfigManager.INVALID_VALUE,
              MenuConfigManager.INVALID_VALUE, null,
              MenuConfigManager.STEP_VALUE, Action.DataType.LASTVIEW);
          if (i == mCurrAction.mInitValue) {
            option.setmChecked(true);
          }
          mActions.add(option);
        }
        break;
      case HAVESUBCHILD:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseSettings",
            "mCurrAction.mSubChild.length==" + mCurrAction.mSubChildGroup.size());
        mActions.addAll(mCurrAction.mSubChildGroup);
        break;
      case PROGRESSBAR:
      case POSITIONVIEW:
        break;
      case LASTVIEW:
        break;
      default:
        break;
    }
  }

  /**
   * generate item for top view
   */
  protected abstract void setActionsForTopView();

  @Override
  public void onActionClicked(Action action) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent onActionClicked:" + action);
    if (action.mDataType != Action.DataType.LASTVIEW
        && action.mDataType != Action.DataType.SCANROOTVIEW
        && action.mDataType != Action.DataType.DIALOGPOP
        && action.mDataType != Action.DataType.SAVEDATA
        && action.mDataType != Action.DataType.PROGRESSBAR
        && action.hasRealChild) {
      mParentAction = mCurrAction;
      mCurrAction = action;
      mActionLevelStack.push(mParentAction);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "action.mDataType==" + action.mItemID + "," + action.mDataType);
      setState(action.mDataType, true);
    } else {// this is for lastView
      if (mCurrAction.mDataType == Action.DataType.OPTIONVIEW
          || mCurrAction.mDataType == Action.DataType.EFFECTOPTIONVIEW
          || mCurrAction.mDataType == Action.DataType.SWICHOPTIONVIEW) {
        String[] idValue = SettingsUtil.getRealIdAndValue(action.mItemID);
        if (idValue != null) {
          try {
            int value = Integer.parseInt(idValue[1]);
//            int lastValue = mCurrAction.mInitValue;
            mCurrAction.mInitValue = value;
            mCurrAction.setDescription(value);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseSettings", "des:" + mCurrAction.getDescription() + ",initVal:"
                + mCurrAction.mInitValue);
            if (mCurrAction.mItemID.startsWith("DVBS_DETAIL_")) {
              if (mCurrAction.getCallBack() != null) {
                mCurrAction.getCallBack().afterOptionValseChanged(mCurrAction.getDescription());
              }
            } else {
              if (mCurrAction.getCallBack() != null) {
                mCurrAction.getCallBack().afterOptionValseChanged(mCurrAction.getDescription());
              }
              mConfigManager.setValue(idValue[0], value, mCurrAction);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          if (mSpecialOptionDealer != null) {
            mSpecialOptionDealer.specialOptionClick(mCurrAction);
          }
        }
        if (mCurrAction.mDataType == Action.DataType.EFFECTOPTIONVIEW) {
          dealEffectOptionValues(mCurrAction);
        } else if (mCurrAction.mDataType == Action.DataType.SWICHOPTIONVIEW) {
          dealSwitchChildItemEnable(mCurrAction);
          if (mSpecialOptionDealer != null) {
            mSpecialOptionDealer.specialOptionClick(mCurrAction);
          }
        }
        // option type view clicked call goBack
        goBack();
      }
    }
    super.onActionClicked(action);
  }

  /**
   * This method is used to set boolean properties
   *
   * @param enable whether or not to enable the property
   */
  protected abstract void setProperty(boolean enable);

  long ENTER_DELAYMILLS = 500;
  long enterPressTime = 0;

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN &&
              (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
              || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
      long nowMills = System.currentTimeMillis();
      long spaceMills = nowMills - enterPressTime;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent nowMills:" + spaceMills);
      if (Math.abs(spaceMills) > ENTER_DELAYMILLS) {
        enterPressTime = nowMills;
      } else {
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }

  public void goBack() {
    if (mState.equals(getInitialDataType())) {
      // if task is running when press back key,cancel this task;
      if (mALoader.isTaskRunning()) {
        mALoader.cancelTask();
      }
      finish();
    } else if (getPrevState() != null) {
      mState = mStateStack.pop();
      mCurrAction = mActionLevelStack.pop();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curr action is== " + mCurrAction.mDataType);
      mParentAction = getPrevAction();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "back prevaction is null== " + (mParentAction == null));
      // Using the synchronous version of popBackStack so that we can get
      // the updated
      // instance of the action Fragment on the following line.
      getFragmentManager().popBackStackImmediate();
      mActionFragment = getActionFragment();
      // Update Current State Actions
      if (mActionFragment instanceof ActionFragment) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "action frag");
        ActionFragment actFrag = (ActionFragment) mActionFragment;
        refreshActionList();
        ((ActionAdapter) actFrag.getAdapter()).setActions(mActions);
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "which frag");
      }
    }
  }

  @Override
  public void finish() {
    Intent data = new Intent();
    setResult(RESULT_OK, data);
    super.finish();
  }

  protected Object getPrevState() {
    return mStateStack.isEmpty() ? null : mStateStack.peek();
  }

  protected Action getPrevAction() {
    return mActionLevelStack.isEmpty() ? null : mActionLevelStack.peek();
  }

  public void dealEffectOptionValues(Action mainAction) {
    List<Action> mEeffectGroup = mainAction.getmEffectGroup();
    int i = 0;
    int initValues[] = mDataHelper.getEffectGroupInitValues(mainAction);
    if (mActionFragment instanceof ActionFragment) {
      ActionFragment actFrag = (ActionFragment) mActionFragment;
      for (; i < mEeffectGroup.size(); i++) {
        Action effectChildData = mEeffectGroup.get(i);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "itemId,initValues[i]==" + effectChildData.mItemID + "," + initValues[i]);
        effectChildData.setmInitValue(initValues[i]);
        effectChildData.setDescription(initValues[i]);
      }
      ((ActionAdapter) actFrag.getAdapter()).notifyDataSetChanged();
    }
  }

  public void dealSwitchChildItemEnable(Action mainAction) {
    mDataHelper.dealSwitchChildGroupEnable(mainAction);
    // if ((mActionFragment != null) && (mActionFragment instanceof ActionFragment)) {
    // ActionFragment actFrag = (ActionFragment) mActionFragment;
    // ((ActionAdapter) actFrag.getAdapter()).notifyDataSetChanged();
    // }
  }

  @Override
  public void loadData() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadData begin....");
    // try{
    // Thread.sleep(3000);
    // }catch(Exception e){
    // e.printStackTrace();
    // }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadData ....");
    setActionsForTopView();
  }

  @Override
  public void loadFinished() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadFinished begin....");
    getWaitingBar().setVisibility(View.GONE);
    if (mActionFragment instanceof ActionFragment) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadFinished setDatas....");
      ActionFragment actFrag = (ActionFragment) mActionFragment;
      if((ActionAdapter) actFrag.getAdapter() != null){
          ((ActionAdapter) actFrag.getAdapter()).setActions(mActions);
      }
    }
  }

  @Override
  public void loadStarting() {
    ProgressBar waiting = getWaitingBar();
    if (waiting != null && waiting.getVisibility() != View.VISIBLE) {
      waiting.setVisibility(View.VISIBLE);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadStarting begin....");
  }

  protected void refreshListView() {
    if (mActionFragment instanceof ActionFragment) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadFinished setDatas....");
      ActionFragment actFrag = (ActionFragment) mActionFragment;
      ((ActionAdapter) actFrag.getAdapter()).notifyDataSetChanged();
    }
  }

  @Override
  protected void onRestart() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "base setting-now onRestart");
    LiveTvSetting.tryToStartTV(this);
    super.onRestart();
  }

  @Override
  protected void onStop() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "base setting-now onStop");
    super.onStop();
  }

}
