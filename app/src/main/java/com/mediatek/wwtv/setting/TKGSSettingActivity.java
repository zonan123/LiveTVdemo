
package com.mediatek.wwtv.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Toast;

import com.mediatek.wwtv.setting.base.scan.adapter.TkgsLocatorListAdapter.TkgsLocatorItem;
import com.mediatek.wwtv.setting.fragments.TkgsLocatorListFrag;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.setting.widget.view.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

import java.util.ArrayList;
import java.util.List;

public class TKGSSettingActivity extends BaseSettingsActivity {
    private String rootTitle;
    private Context mContext;
    private String TAG = "TKGSSettingActivity";
    private boolean isTKGSSettingShow = false;
    final static int MESSAGE_UPDATE_TP = 0x21;

    Handler mHandler = new Handler();

    private Action mTKGSSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate()");
        mContext = this;

        mActions = new ArrayList<Action>();
        mConfigManager = MenuConfigManager.getInstance(mContext);
        mDataHelper = MenuDataHelper.getInstance(mContext);
        rootTitle = mContext.getString(R.string.menu_setup_TKGS_setting);
        mTKGSSetting = new Action(
                MenuConfigManager.TKGS_SETTING,
                rootTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, null,
                MenuConfigManager.STEP_VALUE,
                Action.DataType.HAVESUBCHILD);
        mTKGSSetting.mSubChildGroup = new ArrayList<Action>();
        mCurrAction = mTKGSSetting;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume()");
        
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTKGSSettingShow" + isTKGSSettingShow);
        if (!isTKGSSettingShow) {
            onActionClicked(mTKGSSetting);
            isTKGSSettingShow = true;
        }
        super.onResume();
    }

    private void handleSpecialItem(Action action) {
        if (action.mDataType == Action.DataType.LASTVIEW&&mCurrAction!=null) {
            if (mCurrAction.mDataType == Action.DataType.OPTIONVIEW
                    || mCurrAction.mDataType == Action.DataType.EFFECTOPTIONVIEW
                    || mCurrAction.mDataType == Action.DataType.SWICHOPTIONVIEW) {
                String[] idValue = SettingsUtil.getRealIdAndValue(action.mItemID);
                if (idValue != null) {
                    try {
                        String selId = mCurrAction.mItemID;
                        int value = Integer.parseInt(idValue[1]);
                        int lastValue = mCurrAction.mInitValue;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "value===" + value + ",lastvalue==" + lastValue + " selId="
                                + selId);

                        if (selId.equals(MenuConfigManager.TKGS_PREFER_LIST)) {
                            mDataHelper.setTKGSOneServiceListValue(value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    TurnkeyCommDialog tkgsLocConfirm;

    /**
     * flag :1 == clear hidden locators,2== reset table version clear all hidden locator all reset
     * table version dialog
     */
    public void tkgsLocConfirmShow(final int flag) {
        String message = "";
        if (flag == 1) {
            message = mContext.getString(R.string.tkgs_reset_hidden_msg);
        } else if (flag == 2) {
            message = mContext.getString(R.string.tkgs_reset_table_msg);
        }
        tkgsLocConfirm = new TurnkeyCommDialog(this, 3);
        tkgsLocConfirm.setMessage(message);
        tkgsLocConfirm.setButtonYesName(this
                .getString(R.string.menu_ok));
        tkgsLocConfirm.setButtonNoName(this
                .getString(R.string.menu_cancel));
        tkgsLocConfirm.show();
        tkgsLocConfirm.getButtonNo().requestFocus();
        tkgsLocConfirm.setPositon(-20, 70);
        tkgsLocConfirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                int action = event.getAction();
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && action == KeyEvent.ACTION_DOWN) {
                    tkgsLocConfirm.dismiss();
                    return true;
                }
                return false;
            }
        });

        OnKeyListener yesListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (flag == 1) {
                            // clear hidden
                            int ret = mDataHelper.cleanAllHiddenLocs();
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "clean hidden locs ret ==" + ret);
                            goBack();
                        } else if (flag == 2) {
                            // reset version
                            boolean ret = mDataHelper.resetTKGSTableVersion(0Xff);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reset table version ret ==" + ret);
                            if (ret) {
                                int resetVersion = mDataHelper.getTKGSTableVersion();
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reset table version retver ==" + resetVersion);
                                if (resetVersion == 0XFF) {// invalid version
                                    tkgsResetTabver.setDescription("None");
                                    final ActionFragment frag = (ActionFragment) mActionFragment;
                                    ActionAdapter meAdapter = (ActionAdapter) frag.getAdapter();
                                    meAdapter.notifyDataSetChanged();
                                    mHandler.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            frag.getScrollAdapterView().setSelection(3);
                                        }

                                    }, 500);

                                } else {
                                    tkgsResetTabver.setDescription("" + resetVersion);
                                }
                            }

                        }
                        tkgsLocConfirm.dismiss();
                        return true;
                    }
                }
                return false;
            }
        };

        OnKeyListener noListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        tkgsLocConfirm.dismiss();
                        return true;
                    }
                }
                return false;
            }
        };
        tkgsLocConfirm.getButtonNo().setOnKeyListener(noListener);
        tkgsLocConfirm.getButtonYes().setOnKeyListener(yesListener);
    }

    boolean tkgslocGoBack = false;

    @Override
    public void goBack() {
        if (mCurrAction!=null&&mCurrAction.mItemID.equals(MenuConfigManager.TKGS_SETTING)) {
            mDataHelper.enableMonitor();
            mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
            this.finish();
        } else if (mCurrAction!=null&&mCurrAction.mItemID.equals(MenuConfigManager.TKGS_LOC_POLAZATION)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goBack for TKGS_LOC_POLAZATION");
            tkgslocGoBack = true;
        }
        if(mCurrAction != null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goBack 0mCurrAction.mItemID : " + mCurrAction.mItemID);
        }

        super.goBack();
    }

    @Override
    protected void refreshActionList() {
        if(mCurrAction!=null){
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refreshActionList mCurrAction" + mCurrAction.mItemID);
        }
        mActions.clear();
        switch ((Action.DataType) mState) {
            case HAVESUBCHILD:
                if (mCurrAction!=null&&mCurrAction.mItemID.equals(MenuConfigManager.TKGS_SETTING)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTKGSSetting mActions.addAll");
                    mActions.addAll(mCurrAction.mSubChildGroup);
                } else if (mCurrAction!=null&&mCurrAction.mItemID.equals(MenuConfigManager.TKGS_LOC_ITEM_ADD)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTKGSLocatorInfo add:" + mDataHelper.TKGSVisibleLocSize);
                    if (mDataHelper.TKGSVisibleLocSize < 5) {
                        loadTKGSLocatorInfo(1, mCurrAction);
                        mActions.addAll(mCurrAction.mSubChildGroup);
                    } else {
                        Toast.makeText(mContext, R.string.tkgs_less_than_toast, Toast.LENGTH_LONG)
                                .show();
                    }
                }
                break;
            case TKGSLOCITEMVIEW:
                if(mCurrAction!=null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTKGSLocatorInfo update");
                    loadTKGSLocatorInfo(2, mCurrAction);
                    mActions.addAll(mCurrAction.mSubChildGroup);
                }
                break;
            default:
                if(mCurrAction!=null){
                    super.refreshActionList();
                }
                break;
        }

    }

    /**
     * @param flag = 1 add tkgs locator/flag =2 update tkgs locator
     * @param parentAction
     */
    private void loadTKGSLocatorInfo(int flag, Action parentAction) {
        // when show a tkgsitem's detail when change pola may cause data reset
        // but now i don't want reset
        if (tkgslocGoBack && parentAction.mSubChildGroup.size() > 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTKGSLocatorInfo not refresh return");
            tkgslocGoBack = false;
            return;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTKGSLocatorInfo");
        parentAction.mSubChildGroup = new ArrayList<Action>();
        int freq = -1;
        int rate = -1;
        int pola = -1;
        int progid = -1;
        int recId = -1;
        List<Action> childGroup = new ArrayList<Action>();

        if (flag == 1) {
            TkgsLocatorItem defItem = mDataHelper.getDefaultTKGSLocItem();
            int findPola = defItem.threePry.indexOf('H');
            if (findPola == -1) {
                findPola = defItem.threePry.indexOf('V');
                pola = 1;
            } else {
                pola = 0;
            }
            freq = Integer.parseInt(defItem.threePry.substring(0, findPola));
            rate = Integer.parseInt(defItem.threePry.substring(findPola + 1));
            progid = defItem.progId;
        } else if (flag == 2) {
            if (parentAction instanceof TkgsLocatorItem) {
                TkgsLocatorItem defItem = (TkgsLocatorItem) parentAction;
                int findPola = defItem.threePry.indexOf('H');
                if (findPola == -1) {
                    findPola = defItem.threePry.indexOf('V');
                    pola = 1;
                } else {
                    pola = 0;
                }
                freq = Integer.parseInt(defItem.threePry.substring(0, findPola));
                rate = Integer.parseInt(defItem.threePry.substring(findPola + 1));
                progid = defItem.progId;
                recId = defItem.bnum;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadTKGSLocatorInfo:prog_id:" + progid + ",rec:" + recId);
        String itemName = mContext.getString(R.string.menu_setup_biss_key_freqency);
        Action freqency = new Action(MenuConfigManager.TKGS_LOC_FREQ,
                itemName,
                MenuConfigManager.TKGS_FREQ_MIN,
                MenuConfigManager.TKGS_FREQ_MAX,
                freq,
                null,
                MenuConfigManager.STEP_VALUE,
                Action.DataType.NUMVIEW);
        freqency.setInputLength(5);
        childGroup.add(freqency);
        freqency.setmParentGroup(childGroup);
        itemName = mContext.getString(R.string.setup_dvbc_config_symbolrate);
        Action symbolRate = new Action(MenuConfigManager.TKGS_LOC_SYMBOL_RATE,
                itemName,
                MenuConfigManager.BISS_KEY_SYMBOL_RATE_MIN,
                MenuConfigManager.BISS_KEY_SYMBOL_RATE_MAX,
                rate,
                null,
                MenuConfigManager.STEP_VALUE,
                Action.DataType.NUMVIEW);
        symbolRate.setInputLength(5);
        childGroup.add(symbolRate);
        symbolRate.setmParentGroup(childGroup);
        itemName = mContext.getString(R.string.menu_setup_biss_key_polazation);

        Action polazation = new Action(MenuConfigManager.TKGS_LOC_POLAZATION,
                itemName,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                pola,
                new String[] {
                        mContext.getString(R.string.menu_arrays_Horizonal), mContext.getString(R.string.menu_arrays_Vertical)
                },// option value
                MenuConfigManager.STEP_VALUE,
                Action.DataType.OPTIONVIEW);
        childGroup.add(polazation);
        polazation.setmParentGroup(childGroup);
        itemName = mContext.getString(R.string.menu_setup_biss_key_prog_id);
        Action progId = new Action(MenuConfigManager.TKGS_LOC_SVC_ID,
                itemName,
                MenuConfigManager.TKGS_LOC_SVC_ID_MIN,
                MenuConfigManager.TKGS_LOC_SVC_ID_MAX,
                progid,
                null,
                MenuConfigManager.STEP_VALUE,
                Action.DataType.NUMVIEW);
        progId.setInputLength(4);
        childGroup.add(progId);
        progId.setmParentGroup(childGroup);

        if (flag == 1) {
            Action addKey = new Action(MenuConfigManager.TKGS_LOC_ITEM_SAVE,
                    mContext.getString(R.string.tkgs_save_locator),
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    null,
                    MenuConfigManager.STEP_VALUE,
                    Action.DataType.SAVEDATA);
            childGroup.add(addKey);
            addKey.setmParentGroup(childGroup);
        } else if (flag == 2) {
            Action uptKey = new Action(MenuConfigManager.TKGS_LOC_ITEM_UPDATE,
                    mContext.getString(R.string.tkgs_update_locator),
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    recId,
                    null,
                    MenuConfigManager.STEP_VALUE,
                    Action.DataType.SAVEDATA);
            childGroup.add(uptKey);
            uptKey.setmParentGroup(childGroup);

            Action delKey = new Action(MenuConfigManager.TKGS_LOC_ITEM_DELETE,
                    mContext.getString(R.string.tkgs_delete_locator),
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    recId,
                    null,
                    MenuConfigManager.STEP_VALUE,
                    Action.DataType.SAVEDATA);
            childGroup.add(delKey);
            delKey.setmParentGroup(childGroup);
        }

        parentAction.mSubChildGroup.addAll(childGroup);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult requestCode:" + requestCode + ",resultCode:" + resultCode
                + ",data:" + data);
        switch (resultCode) {
            case RESULT_OK:
                if (data != null) {
                    String value = data.getStringExtra("value");
                    if (mActionFragment instanceof ActionFragment && value != null) {
                        ActionFragment frag = (ActionFragment) mActionFragment;
                        int pos = frag.getSelectedItemPosition();
                        Action selectAction = (Action) frag.getScrollAdapterView()
                                .getSelectedView().getTag(R.id.action_title);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult pos:" + pos + ",ationId:"
                                + selectAction.mItemID);
                        int now = Integer.parseInt(value);
                        if (now < selectAction.mStartValue) {
                            now = selectAction.mStartValue;
                        } else if (now > selectAction.mEndValue) {
                            now = selectAction.mEndValue;
                        }
                        selectAction.mInitValue = now;
                        selectAction.setDescription(now + "");

                        ActionAdapter meAdapter = (ActionAdapter) frag.getAdapter();
                        meAdapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActionClicked(Action action) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActionClicked " + action.mItemID);
        handleSpecialItem(action);

        if (action.mItemID.equals(MenuConfigManager.TKGS_SETTING)) {
            loadTKGSSetting();
            mDataHelper.disableMonitor();
        } else if (action.mItemID.equals(MenuConfigManager.TKGS_LOC_ITEM_ADD)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TKGS_LOC_ITEM_ADD");
            if (mDataHelper.TKGSVisibleLocSize >= 5) {
                Toast.makeText(mContext, R.string.tkgs_less_than_toast, Toast.LENGTH_LONG).show();
                return;
            }
        }
        switch (action.mDataType) {
            case SAVEDATA:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do save data...");
                int freq = action.getmParentGroup().get(0).mInitValue;
                int rate = action.getmParentGroup().get(1).mInitValue;
                int pola = action.getmParentGroup().get(2).mInitValue;
                int progId = action.getmParentGroup().get(3).mInitValue;
                String threePry = "";
                if (pola <= 0) {
                    threePry = freq + "H" + rate;
                } else {
                    threePry = freq + "V" + rate;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do save data...threePry==" + threePry);
                // TKGS-LOC
                if (action.mItemID.equals(MenuConfigManager.TKGS_LOC_ITEM_SAVE)) {
                    TkgsLocatorItem item = new TkgsLocatorItem(-1, progId, threePry, getString(R.string.tkgs_locator_item));
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "freq" + freq + "rate" + rate + "pola" + pola + "progId" + progId);
                    int ret = mDataHelper.operateTKGSLocatorinfo(item, 0);
                    if (ret == -2) {// tkgs locator existed
                        Toast.makeText(mContext, R.string.tkgs_existed_toast, Toast.LENGTH_SHORT)
                                .show();
                    } else if (ret == -9) {
                        Toast.makeText(mContext, R.string.tkgs_less_than_toast, Toast.LENGTH_SHORT)
                                .show();
                    } else if (ret == 0) {
                        goBack();
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addTKGSLocatorinfo ret is:" + ret);
                    }
                } else if (action.mItemID.equals(MenuConfigManager.TKGS_LOC_ITEM_UPDATE)) {
                    TkgsLocatorItem item = new TkgsLocatorItem(action.mInitValue, progId,
                            threePry, mContext.getString(R.string.tkgs_locator_item));
                    int ret = mDataHelper.operateTKGSLocatorinfo(item, 1);
                    if (ret == -2) {// tkgs locator existed
                        Toast.makeText(mContext, R.string.tkgs_existed_toast, Toast.LENGTH_SHORT)
                                .show();
                    } else if (ret == 0) {
                        goBack();
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateTKGSLocatorinfo ret is:" + ret);
                } else if (action.mItemID.equals(MenuConfigManager.TKGS_LOC_ITEM_DELETE)) {
                    TkgsLocatorItem item = new TkgsLocatorItem(action.mInitValue, progId,
                            threePry, mContext.getString(R.string.tkgs_locator_item));
                    int ret = mDataHelper.operateTKGSLocatorinfo(item, 2);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteTKGSLocatorinfo ret is:" + ret);
                    if (ret == 0) {
                        goBack();
                    }
                }

                break;
            case NUMVIEW:
                gotoEditTextAct(action);
                break;
            case BISSKEYVIEW:
                gotoEditTextAct(action);
                break;
            case DIALOGPOP:
                if (action.mItemID.equals(MenuConfigManager.TKGS_LOC_ITEM_HIDD_CLEANALL)) {
                    tkgsLocConfirmShow(1);
                } else if (action.mItemID.equals(MenuConfigManager.TKGS_RESET_TAB_VERSION)) {
                    tkgsLocConfirmShow(2);
                }
                break;
            default:
                super.onActionClicked(action);
                break;
        }
    }

    Action tkgsResetTabver;

    private void loadTKGSSetting() {
        String[] operModes = new String[] {
                mContext.getString(R.string.tkgs_mode_auto), mContext.getString(R.string.tkgs_mode_customisable), 
                mContext.getString(R.string.tkgs_off)
        };
        Action opertateMode = new Action(MenuConfigManager.TKGS_OPER_MODE,
                mContext.getString(R.string.tkgs_operator_mode),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                mConfigManager.getDefault(MenuConfigManager.TKGS_OPER_MODE),
                operModes,// option value
                MenuConfigManager.STEP_VALUE,
                Action.DataType.OPTIONVIEW);
        // opertateMode.setEnabled(false);
        mTKGSSetting.mSubChildGroup.add(opertateMode);

        // tkgs locator list
        Action locatorlist = new Action(MenuConfigManager.TKGS_LOC_LIST,
                mContext.getString(R.string.menu_setup_TKGS_locate_list),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, null,
                MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
        locatorlist.mSubChildGroup = new ArrayList<Action>();
        mTKGSSetting.mSubChildGroup.add(locatorlist);

        // hidden locations
        Action hiddenLocations = new Action(MenuConfigManager.TKGS_HIDD_LOCS,
                mContext.getString(R.string.menu_setup_TKGS_hidden_locations),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, null,
                MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
        hiddenLocations.mSubChildGroup = new ArrayList<Action>();
        // reset table version
        tkgsResetTabver = new Action(MenuConfigManager.TKGS_RESET_TAB_VERSION,
                mContext.getString(R.string.TKGS_reset_table_version),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE, null,
                MenuConfigManager.STEP_VALUE, Action.DataType.DIALOGPOP);
        tkgsResetTabver.mSubChildGroup = new ArrayList<Action>();
        int tversion = mDataHelper.getTKGSTableVersion();
        if (tversion == 0XFF) {// invalid version
            tkgsResetTabver.setDescription(mContext.getString(R.string.menu_arrays_None));
        } else {
            tkgsResetTabver.setDescription("" + tversion);
        }

        String cID = MenuConfigManager.TKGS_FAC_SETUP_AVAIL_CONDITION;
        boolean isCondNormal = mConfigManager.getDefault(cID) == 0;

        if (isCondNormal) {
            mTKGSSetting.mSubChildGroup.add(tkgsResetTabver);
            // tkgsResetTabver.setEnabled(false);
        } else {
            mTKGSSetting.mSubChildGroup.add(hiddenLocations);
            mTKGSSetting.mSubChildGroup.add(tkgsResetTabver);
        }

        List<String> batStrList = mDataHelper.getTKGSOneServiceStrList(mDataHelper
                .getTKGSOneSvcList());
        int defVal = mDataHelper.getTKGSOneServiceSelectValue();
        String[] preferlistArray = batStrList.toArray(new String[0]);
        if (preferlistArray != null && preferlistArray.length > 0) {
            Action preferredList = new Action(MenuConfigManager.TKGS_PREFER_LIST,
                    mContext.getString(R.string.TKGS_preferred_list),
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    defVal,
                    preferlistArray,// option values
                    MenuConfigManager.STEP_VALUE,
                    Action.DataType.OPTIONVIEW);
            mTKGSSetting.mSubChildGroup.add(preferredList);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected Object getInitialDataType() {
        return DataType.TOPVIEW;
    }

    @Override
    protected void updateView() {
        if(mCurrAction!=null){
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateView CurrentAction =" + mCurrAction.mItemID);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DataType=" + (Action.DataType) mState);

        switch ((Action.DataType) mState) {
            case TOPVIEW:
            case OPTIONVIEW:
            case SWICHOPTIONVIEW:
            case EFFECTOPTIONVIEW:
            case HAVESUBCHILD:
                refreshActionList();
//                if (mActions != null) {
//                    for (int i = 0; i < mActions.size(); i++) {
//                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSubChildGroup ="
//                                + mActions.get(i).mItemID);
//                    }
//                }

                setView(mCurrAction == null ? mContext.getString(R.string.menu_setup_TKGS_setting) : mCurrAction.getTitle(),
                        mParentAction == null ? mContext.getResources().getString(
                                R.string.menu_interface_name) : mParentAction
                                .getTitle(), "", R.drawable.menu_tv_icon);
                if (mCurrAction!=null&&(mCurrAction.mItemID.equals(MenuConfigManager.TKGS_LOC_LIST)
                        || mCurrAction.mItemID.equals(MenuConfigManager.TKGS_HIDD_LOCS))) {
                    TkgsLocatorListFrag tkgsfrag = new TkgsLocatorListFrag();
                    tkgsfrag.setAction(mCurrAction);
                    mActionFragment = tkgsfrag;
                    setViewWithActionFragment(
                            mCurrAction == null ? mContext.getString(R.string.menu_tab_setup) : mCurrAction.getTitle(),
                            mParentAction == null ? rootTitle : mParentAction.getTitle(),
                            "", R.drawable.menu_setup_icon);
                }
                break;
            case TKGSLOCITEMVIEW:
                refreshActionList();
                setView(mCurrAction == null ? mContext.getString(R.string.menu_tab_setup) : mCurrAction.getTitle(),
                        mParentAction == null ? rootTitle : mParentAction.getTitle(),
                        "", R.drawable.menu_setup_icon);
                break;
            default:
                break;
        }

    }

    @Override
    protected void setActionsForTopView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setActionsForTopView");
    }

    @Override
    protected void setProperty(boolean enable) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setProperty");
    }

    protected void setViewWithActionFragment(String title, String breadcrumb, String description,
            int iconResId) {
        mContentFragment = ContentFragment.newInstance(title, breadcrumb, description, iconResId,
                getResources().getColor(R.color.icon_background));
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }

    static final int REQ_EDITTEXT = 0x23;

    /**
     * goto the edittext activity in order to input text or number
     *
     * @param selectedView
     */
    public void gotoEditTextAct(Action action) {
        if (action.isEnabled()) {// this is edit item
            Intent intent = new Intent(mContext, EditTextActivity.class);
            intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
            intent.putExtra(EditTextActivity.EXTRA_DESC, action.getTitle());
            if (action.mItemID.equals(MenuConfigManager.SETUP_POSTAL_CODE)
                    || action.mItemID.equals(MenuConfigManager.BISS_KEY_CW_KEY)) {
                intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, action.getDescription());
            } else {
                intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, "" + action.mInitValue);
            }

            intent.putExtra(EditTextActivity.EXTRA_ITEMID, action.mItemID);
            if (action.mDataType == DataType.NUMVIEW) {
                intent.putExtra(EditTextActivity.EXTRA_DIGIT, true);
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 5);
            } else if (action.mDataType == DataType.BISSKEYVIEW) {
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 16);
                intent.putExtra(EditTextActivity.EXTRA_CAN_WATCH_TEXT, true);
            }

            this.startActivityForResult(intent, REQ_EDITTEXT);
        } else {// this is option item
                // do nothing
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Option Item needn't go to editText Activity");
        }

    }

}
