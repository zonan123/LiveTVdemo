
package com.mediatek.wwtv.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.wwtv.setting.base.scan.model.DVBSScanner;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogActivity;
import com.mediatek.wwtv.setting.fragments.ProgressBarFrag;
import com.mediatek.wwtv.setting.fragments.SatListFrag;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.SatDetailUI;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnCancelClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.setting.util.SettingsUtil;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.twoworlds.tv.MtkTvConfig;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SatActivity extends BaseSettingsActivity {

    private Context mContext;
    private int selectPos = 0;
    private String mItemId = "";
    private String title = "";
    private String TAG = "SatActivity";
    
    final static int MESSAGE_UPDATE_TP = 0x21;
    private boolean showSatList = false;
    final static int MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL = 0x22;
    public static boolean isSatListShow = false;
    private int originalChannelListType = -1;
    //private int mAntennaType;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_TP:
                    Action action = (Action) msg.obj;
                    if (action.mDataType == DataType.LASTVIEW
                            &&
                            action.mItemID
                            .startsWith(SettingsUtil.SPECIAL_SAT_DETAIL_INFO_ITEM_POL)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "after goback for SAT POLA:" + action.mItemID);
                        ActionFragment frag = (ActionFragment) mActionFragment;
                        View selectView=frag.getScrollAdapterView().getSelectedView();
                        if(selectView!=null){
                            Action paction = (Action) selectView.
                                    getTag(R.id.action_title);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "after goback for SAT pAction:" +
                                    paction.mItemID + ",satID==" + paction.satID);
                            mDataHelper.
                            saveDVBSSatTPInfo(mContext, paction.satID,
                                    frag.getScrollAdapterView());
                        }
                    }
                    break;
                case MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL:
                    mHandler.removeMessages(MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL);
                    Action action1 = null;
                    Action action2 = null;
                    if (mActionFragment instanceof ProgressBarFrag) {
                        if(((ProgressBarFrag) mActionFragment).getView() == null) {
                            return;
                        }
                        if (((ProgressBarFrag) mActionFragment).getActionId() != null
                                && ((ProgressBarFrag) mActionFragment).getActionId().
                                equals(MenuConfigManager.DVBS_SIGNAL_LEVEL)) {
                            int value1 = msg.arg1;
                            value1 = Math.max(0, value1);
                            ((ProgressBarFrag) mActionFragment).showValue(value1);
                        } else if (((ProgressBarFrag) mActionFragment).getActionId() != null
                                && ((ProgressBarFrag) mActionFragment).getActionId().
                                equals(MenuConfigManager.DVBS_SIGNAL_QULITY)) {
                            int value2 = msg.arg2;
                            value2 = Math.max(0, value2);
                            ((ProgressBarFrag) mActionFragment).showValue(value2);
                        }
                    } else {
                        View convertView1 = null;
                        View convertView2 = null;
                        int antenaTypeIndex = MtkTvConfig.getInstance().
                                getConfigValue(ScanContent.SAT_ANTENNA_TYPE);
                        /* boolean isDiseqc12 = MarketRegionInfo.
                                isFunctionSupport(MarketRegionInfo.F_DISEQC12_IMPROVE);*/
                        if (antenaTypeIndex == 0 && false/*isDiseqc12*/) {
                            if (mActionFragment instanceof ActionFragment) {
                                if(((ActionFragment) mActionFragment).getView() == null) {
                                    return;
                                }
                                ScrollAdapterView listView = ((ActionFragment) mActionFragment).
                                        getScrollAdapterView();
                                int totalCount = listView.getChildCount();
                                if (totalCount >= 2) {
                                    convertView1 = listView.getChildAt(0);
                                    convertView2 = listView.getChildAt(totalCount - 1);
                                    action1 = (Action) convertView1.getTag(R.id.action_title);
                                    action2 = (Action) convertView2.getTag(R.id.action_title);
                                }

                            }
                        } else {
                            if (mActionFragment instanceof ActionFragment) {
                                if(((ActionFragment) mActionFragment).getView() == null) {
                                    return;
                                }
                                ScrollAdapterView listView = ((ActionFragment) mActionFragment).
                                        getScrollAdapterView();
                                int totalCount = listView.getChildCount();
                                if (totalCount >= 2) {
                                    convertView1 = listView.getChildAt(totalCount - 1);
                                    convertView2 = listView.getChildAt(totalCount - 2);
                                    action1 = (Action) convertView1.getTag(R.id.action_title);
                                    action2 = (Action) convertView2.getTag(R.id.action_title);
                                }

                            }
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refresh dvbs signal quality and level action1:" +
                                (action1 == null ? "" : action1.mItemID)
                                + ",action2==" + (action2 == null ? "" : action2.mItemID));
                        if (action1 != null
                                && action1.mItemID
                                .equalsIgnoreCase(MenuConfigManager.DVBS_SIGNAL_LEVEL)
                                && action2 != null && action2.mItemID.
                                equalsIgnoreCase(MenuConfigManager.DVBS_SIGNAL_QULITY)) {
                            int value1 = msg.arg1;
                            int value2 = msg.arg2;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refresh dvbs signal quality and level :value1:"
                                    + value1 + ",value2==" + value2);
                            value1 = Math.max(0, value1);
                            value2 = Math.max(0, value2);
                            action1.mInitValue = value1;
                            action2.mInitValue = value2;
                            action1.setDescription(value1+"%");
                            action2.setDescription(value2+"%");
                            TextView description1 = (TextView) convertView1.
                                    findViewById(R.id.action_description);
                            TextView description2 = (TextView) convertView2.
                                    findViewById(R.id.action_description);
                            description1.setText(action1.getDescription());
                            description1.setVisibility(
                                    TextUtils.isEmpty(action1.getDescription()) ?
                                            View.GONE : View.VISIBLE);
                            description2.setText(action2.getDescription());
                            description2.setVisibility(
                                    TextUtils.isEmpty(action2.getDescription()) ?
                                            View.GONE : View.VISIBLE);
                            // ActionAdapter meAdapter = (ActionAdapter)
                            // ((ActionFragment) mActionFragment).getAdapter();
                            // meAdapter.notifyDataSetChanged();
                        }
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refresh  mState:" + mState);
                    if (mState != null
                            && ((Action.DataType) mState == Action.DataType.SATELITEDETAIL
                            || (Action.DataType) mState == Action.DataType.PROGRESSBAR
                            || (Action.DataType) mState == Action.DataType.HAVESUBCHILD)) {
                        sendMessageDelayedThread(MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL, 1000);
                    }
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate()");
        mContext = this;
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        mItemId = intent.getStringExtra("mItemId");
        selectPos = intent.getIntExtra("selectPos", 0);

        int mAntennaType = TVContent.getInstance(this).getConfigValue(ScanContent.SAT_ANTENNA_TYPE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mItemId="+mItemId+",title="+title+",mAntennaType="+mAntennaType);
        if (showSatList) {
            mCurrAction = new Action(mItemId, title, MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE, null,
                    MenuConfigManager.STEP_VALUE, Action.DataType.SATELITEINFO);
            onActionClicked(mCurrAction);
            showSatList = false;
        }
        showSatList = true;
        SaveValue savevalue=SaveValue.getInstance(this);
        savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE,1);
        savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS,1);
        savevalue.saveValue(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_MOVEMENT_CONTROL,0);
        SatDetailUI.getInstance(mContext).mDvbsLNBSearched = false;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume()");
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        mItemId = intent.getStringExtra("mItemId");
        selectPos = intent.getIntExtra("selectPos", 0);

        if (showSatList) {
            mCurrAction = new Action(mItemId, title, MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE,
                    MenuConfigManager.INVALID_VALUE, null,
                    MenuConfigManager.STEP_VALUE, Action.DataType.SATELITEINFO);
            onActionClicked(mCurrAction);
            showSatList = false;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SatListonActionClicked");
        super.onResume();
    }

    @Override
    public void goBack() {
        if(mCurrAction==null){
            super.goBack();
            return;
            }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goBack 0mCurrAction.mItemID : " + mCurrAction.mItemID);
        String beforeBackItemId = mCurrAction.mItemID;
        if (mCurrAction != null
                && (mCurrAction.mItemID.equals(SettingsUtil.SPECIAL_SAT_DETAIL_INFO_ITEM_POL)
                    || (mCurrAction.mParent != null && getString(R.string.dvbs_lnb_configration).equals(mCurrAction.mParent.mItemID)))) {
            final int satId = mCurrAction.satID;
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    ScanContent.setDVBSFreqToGetSignalQuality(satId);
                }
            }, 1000);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goBack 1mCurrAction.mItemID : " + mCurrAction.mItemID);
        }else if (beforeBackItemId.equals(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_SET)) {
            // atenna type set
            saveBandFrequencyToAcfg();            
            //this.finish();
            }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goback isCamScanUI="+ScanContent.isCamScanUI+","+originalChannelListType+","+mCurrAction);
        if(ScanContent.isCamScanUI && originalChannelListType != -1 && mCurrAction != null
                && mCurrAction.mItemID.equals(MenuConfigManager.DVBS_SAT_OP_CAM_SCAN)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "set original type="+originalChannelListType);
            MtkTvConfig.getInstance().setConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE, originalChannelListType);
            originalChannelListType = -1;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goBack mCurrAction ="+mCurrAction.mItemID);
        
        super.goBack();
    }
    
    private void saveBandFrequencyToAcfg() {
        List<Action> childList = mCurrAction.mSubChildGroup;
        int atnatype = childList.get(0).mInitValue;
        int userBand = 0;
        int bandFreq = 0;
        int subUserBand = 0;
        int subBandFreq = 0;
        userBand = childList.get(1).mInitValue;
        String bandFreqDes = childList.get(2).getDescription();
        String userdef = mContext.getString(R.string.dvbs_band_freq_user_define);
        //SaveValue saveValue = SaveValue.getInstance(mContext);
        if(isDualTuner){
            subUserBand = childList.get(3).mInitValue;
            String subBandFreqDes = childList.get(4).getDescription();
            String subUserdef = mContext.getString(R.string.dvbs_band_freq_user_define);
            if (bandFreqDes.equalsIgnoreCase(userdef)) {
                bandFreq = userDefineFreq;//saveValue.readValue("userDefineFreq");
                if (subBandFreqDes.equalsIgnoreCase(subUserdef)) {
                    subBandFreq = subuserDefineFreq;//saveValue.readValue("subuserDefineFreq");
                }else {
                    try {
                        subBandFreq = Integer.parseInt(subBandFreqDes);
                    } catch (Exception e) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,e.getMessage());
                        subBandFreq = 950;
                    }
                }
            } else {
                try {
                    bandFreq = Integer.parseInt(bandFreqDes);
                } catch (Exception e) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,e.getMessage());
                    bandFreq = 950;
                }
                if (subBandFreqDes.equalsIgnoreCase(subUserdef)) {
                    subBandFreq = subuserDefineFreq;//saveValue.readValue("subuserDefineFreq");
                }else {
                    try {
                        subBandFreq = Integer.parseInt(subBandFreqDes);
                    } catch (Exception e) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,e.getMessage());
                        subBandFreq = 950;
                    }
                }            
            }                  
            ScanContent.getInstance(mContext).saveDVBSConfigSetting(mContext, atnatype, userBand, bandFreq
                    , subUserBand, subBandFreq);            
        }else {
            if (bandFreqDes.equalsIgnoreCase(userdef)) {
                bandFreq = childList.get(3).mInitValue;                    
            } else {                 
                bandFreqDes = childList.get(2).getDescription();
                try {
                    bandFreq = Integer.parseInt(bandFreqDes);
                } catch (Exception e) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,e.getMessage());
                    bandFreq = 950;
                }
            }
            ScanContent.getInstance(mContext).
            saveDVBSConfigSetting(mContext, atnatype, userBand, bandFreq);
        }
    }

    @Override
    protected void refreshActionList() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refreshActionList");
        mActions.clear();
        switch ((Action.DataType) mState) {
            case SATELITEDETAIL:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AddSatDetailUI");
                if(ScanContent.isCamScanUI) {
                    int channelListType = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE);
                    if (channelListType == 0) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AddSatDetailUI set channel_list_Type 1");
                        originalChannelListType = 0;
                        MtkTvConfig.getInstance().setConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE, 1);
                    }
                }
                SatDetailUI.getInstance(mContext).initSatelliteInfoViews(mCurrAction,
                        mCurrAction.satID);
                if(mCurrAction.getmSubChildGroup() != null) {
                    mActions.addAll(mCurrAction.mSubChildGroup);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrAction = " + mCurrAction.mItemID + "  mSubChildGroup ="
                            + mCurrAction.mSubChildGroup.size() + "satID =" + mCurrAction.satID);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mActions.size()=" + mActions.size());
                }
                break;
            case SWICHOPTIONVIEW:
                super.refreshActionList();
                if (mCurrAction.mItemID.equals(MenuConfigManager.CHANNEL_LIST_TYPE)) {
                    String profileName = MtkTvConfig.getInstance().
                            getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
                    // has profile name
                    if (!TextUtils.isEmpty(profileName)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change to profileName:" + profileName);
                        mActions.get(1).setmTitle(profileName);
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not change profileName");
                    }
                }
                break;
            case OPTIONVIEW:
                super.refreshActionList();
                if (mCurrAction.mItemID.equals(MenuConfigManager.DVBS_DETAIL_LNB_FREQUENCY)) {
                    mActions.get(mActions.size() - 1).setmDataType(Action.DataType.NUMVIEW);//user define
                }
                break;
            default:
                super.refreshActionList();
                break;
        }
        if ((Action.DataType) mState == Action.DataType.SATELITEDETAIL ||
                (mCurrAction != null && mCurrAction.getmSubChildGroup() != null && mCurrAction.getmSubChildGroup().size() != 0 && MenuConfigManager.DVBS_SIGNAL_LEVEL.equals(
                        mCurrAction.getmSubChildGroup().get(mCurrAction.getmSubChildGroup().size() - 1).getKey()))) {
            // send refresh quality and level
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "try to refresh dvbs signal quality and level");
            sendMessageDelayedThread(MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL, 2000);
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "remove refresh dvbs signal quality and level");
            mHandler.removeMessages(MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL);
        }

    }

    @Override
    public void onActionClicked(Action action) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onactionClick action id:"+action.mItemID+"," +
                "action name:"+action.getTitle()+"," +
                "action dis:"+action.getDescription()+"," +
                "action initValue:"+action.mInitValue);
        if (action.mDataType == Action.DataType.SCANROOTVIEW) {
            if (action.mItemID.startsWith(MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR)) {
                String op = action.getmTitle();
                ScanContent.setOperator(mContext, op);
                mDataHelper.changeEnable();
            }
        } else if (action.mDataType == Action.DataType.NUMVIEW
                || action.mDataType == Action.DataType.EDITTEXTVIEW) {
            gotoEditTextAct(action);
            return;
        } else if (action.mDataType == Action.DataType.LEFTRIGHT_VIEW
                || action.mDataType == Action.DataType.DISEQC12_SAVEINFO) {
            String mId = action.mItemID;
            if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC10_PORT)) {
               // mDataHelper.setDiseqc10TunerPort(action.mInitValue);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onactionClick action id:"+mId);
            }else if(mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC11_PORT)){
                //mDataHelper.setDiseqc11TunerPort(action.mInitValue);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onactionClick action id:"+mId);
            } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_DISABLE_LIMITS)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_LIMIT_EAST)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_LIMIT_WEST)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_STORE_POSITION)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_POSITION)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOTOR_GOTO_REFERENCE)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_GOTOXX)) {
                mDataHelper.setDiseqc12MotorPageTuner(mId, action.mInitValue);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "action id:"+action.mItemID+",action init value:"+action.mInitValue);
            } else if (mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_EAST)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_MOVE_WEST)
                    || mId.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STOP_MOVEMENT)) {
                mDataHelper.setDiseqc12MovementControlPageTuner(mId);
            } else if (action.mDataType == Action.DataType.LEFTRIGHT_HASCHILDVIEW) {
                if (action.getCallBack() != null) {
                    action.getCallBack().afterOptionValseChanged(action.getDescription());
                }
            } else if (action.mDataType == Action.DataType.NUMVIEW
                    || action.mDataType == Action.DataType.EDITTEXTVIEW) {
                gotoEditTextAct(action);
                return;
            }
            if (action.getBeforeCallBack() != null) {
                action.getBeforeCallBack().beforeValueChanged(action.mPrevInitValue);
            }
            if (action.getCallBack() != null) {
                action.getCallBack().afterOptionValseChanged(action.getDescription());
            }
            return;
        }else if(action.mDataType == Action.DataType.TEXTCOMMVIEW) {
            if (MenuConfigManager.DVBS_SAT_MANUAL_TURNING_NEXT.equals(action.mItemID)) {
                Intent intent = new Intent(mContext,ScanDialogActivity.class);
                intent.putExtra("ActionID", MenuConfigManager.DVBS_SAT_MANUAL_TURNING);
                intent.putExtra("SatID", action.satID);
                mContext.startActivity(intent);
            }
            return;
        }
        super.onActionClicked(action);
       /* Message msg = mHandler.obtainMessage();
        msg.what = this.MESSAGE_UPDATE_TP;
        msg.obj = action;
        mHandler.sendMessageDelayed(msg, 1000);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_EDITTEXT:
            case REQ_LNB_EDITTEXT:
                if (data != null) {
                    String value = data.getStringExtra("value");
                    if (mActionFragment instanceof ActionFragment) {
                        ActionFragment frag = (ActionFragment) mActionFragment;
                        //int pos = frag.getSelectedItemPosition();
                        if(frag.getScrollAdapterView().getSelectedView() == null){
                            return;
                        }
                        Action selectAction = (Action) frag.getScrollAdapterView().
                                getSelectedView().getTag(R.id.action_title);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onActivityResult ationId:"
                                + selectAction.mItemID);
                        if (selectAction.mDataType == DataType.NUMVIEW) {
                            if(selectAction.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE)||
                                    selectAction.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE)){
                                double tempValue = Double.parseDouble(value);
                                DecimalFormat format = new DecimalFormat("#0.0");
                                double doubleValue = Double.parseDouble(format.format(tempValue));
                                selectAction.mInitValue = (int) (doubleValue * 10);
                                MenuConfigManager.getInstance(mContext).setValue(selectAction.mItemID, selectAction.mInitValue);
                                selectAction.setDescription(MenuConfigManager.getInstance(mContext).getUSALSDescription(selectAction.mInitValue, selectAction.mItemID));
                            }else if(selectAction.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION)){
                                double tempValue = Double.parseDouble(value);
                                DecimalFormat format = new DecimalFormat("#0.0");
                                double doubleValue = Double.parseDouble(format.format(tempValue));
                                selectAction.mInitValue = (int) (doubleValue * 10);
                                SaveValue saveValue = SaveValue.getInstance(mContext);
                                saveValue.saveValue(selectAction.mItemID, selectAction.mInitValue);
                                selectAction.setDescription(MenuConfigManager.getInstance(mContext).getUSALSDescription(selectAction.mInitValue, selectAction.mItemID));
                            }else if(requestCode == REQ_LNB_EDITTEXT){
                                int low = Integer.parseInt(data.getStringExtra("value-low"));
                                int high = Integer.parseInt(data.getStringExtra("value-high"));
                                int swit = 0;
                                if(data.getStringExtra("value-switch") != null){
                                    swit = Integer.parseInt(data.getStringExtra("value-switch"));
                                }
                                String[] lnbFreqs = mCurrAction.mOptionValue;
                                List<String> list = new ArrayList<String>();
                                Collections.addAll(list, lnbFreqs);
                                if(list.size() > SatDetailUI.LNB_CONFIG.length){
                                    list.remove(lnbFreqs.length-1);
                                    list.remove(lnbFreqs.length-2);
                                }else {
                                    list.remove(lnbFreqs.length-1);
                                }
                                String showfreq = null;
                                String freq = null;
                                int existFreqIndex = -1;
                                if(low == high){ //single freq
                                    existFreqIndex = SatDetailUI.checkIfExistLnbFreq(low, 0, 0);
                                    freq = low+"";
                                    showfreq = low+"";
                                    high = 0;
                                    swit = 0;
                                }else { //dual freq
                                    if(swit != 0){
                                        existFreqIndex = SatDetailUI.checkIfExistLnbFreq(low, high, swit);
                                        showfreq = low+","+high/*+","+swit*/;
                                        freq = low+","+high+","+swit;
                                    }else {
                                        existFreqIndex = SatDetailUI.checkIfExistLnbFreq(low, high, LNBFreqEditActivity.DEFAULT_SWITCH_FREQ);
                                        freq = low+","+high;
                                        showfreq = low+","+high;
                                        swit = 0;
                                    }
                                }
                                if(existFreqIndex == -1){
                                    SatDetailUI.LNB_CONFIG[11][0] = low;
                                    SatDetailUI.LNB_CONFIG[11][1] = high;
                                    SatDetailUI.LNB_CONFIG[11][2] = swit;
                                    list.add(showfreq);
                                    list.add(lnbFreqs[lnbFreqs.length-1]);                                    
                                    mCurrAction.mOptionValue = list.toArray(new String[0]);
                                    mCurrAction.mInitValue = list.size() - 2;
                                }else {
                                    mCurrAction.mInitValue = existFreqIndex;
                                }
                                mCurrAction.setDescription( mCurrAction.mInitValue);
                                SatDetailUI.getInstance(mContext).saveLnbFreqUserDefine(freq, mCurrAction.satID, mCurrAction.mParent);
                                refreshActionList();
                                ActionAdapter meAdapter = (ActionAdapter) frag.getAdapter();
                                meAdapter.setActions(mActions);
                                break;
                            }else{
                                int now = Integer.parseInt(value);
                                if (now < selectAction.mStartValue) {
                                    now = selectAction.mStartValue;
                                } else if (now > selectAction.mEndValue) {
                                    now = selectAction.mEndValue;
                                }
                                selectAction.mInitValue = now;
                                if(selectAction.mItemID.endsWith(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_USERDEF)){
                                    userDefineFreq = now;
                                    //SaveValue.getInstance(mContext).saveValue("userDefineFreq",userDefineFreq);
                                }else if(selectAction.mItemID.equals(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_SUB_USERDEF)){
                                    subuserDefineFreq = now;
                                    //SaveValue.getInstance(mContext).saveValue("subuserDefineFreq",subuserDefineFreq);
                                }
                                selectAction.setDescription(now + "");
                                if (selectAction.getTitle().equals(
                                        mContext.getString(R.string.dvbs_tp_sys_rate))
                                        || selectAction.getTitle().equals(
                                                mContext.getString(R.string.dvbs_tp_fre))) {
                                    if(selectAction.getmParent() != null && selectAction.getmParent().getmParent().mItemID
                                            .equalsIgnoreCase(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)){//if user modify TP in update scan
                                        ScanContent.setTpHasModifiedForUpdateScan(true);      //Then use modified TP to scan,nor use BGM saved TP
                                    }
                                    MenuDataHelper.getInstance(mContext).
                                    saveDVBSSatTPInfo(mContext, selectAction.satID,
                                            frag.getScrollAdapterView());
                                    ScanContent.setDVBSFreqToGetSignalQuality(selectAction.satID);
                                } else if (selectAction.mItemID.
                                        equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_STEP_SIZE)
                                        || selectAction.mItemID
                                        .
                                        equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_TIMEOUTS)) {
                                    SaveValue saveValue = SaveValue.getInstance(mContext);
                                    saveValue.saveValue(selectAction.mItemID, selectAction.mInitValue);
                                }
                            }
                        } else if (selectAction.mDataType == DataType.EDITTEXTVIEW) {
                            // for string
                            selectAction.mOptionValue = new String[] {
                                    value
                            };
                            selectAction.setDescription(value);
                            String satNameId = mContext.getResources().getString(
                                    R.string.dvbs_satellite_name);
                            if (selectAction.mItemID.equals(satNameId)) {
                                SatDetailUI.getInstance(mContext).updateOnlySatelliteName(value);
                            }
                        }

                        ActionAdapter meAdapter = (ActionAdapter) frag.getAdapter();
                        meAdapter.notifyDataSetChanged();
                    }

                }
                break;
            case REQ_LNB_SEARCH_RESULT:
                SatDetailUI.getInstance(mContext).mDvbsLNBSearched = true;
                ((SatListFrag)mActionFragment).refreshSatList();
                break;
            default:
                break;
        }
    }

    static final int REQ_EDITTEXT = 0x23;
    static final int REQ_LNB_EDITTEXT = 0x24;
    static final int REQ_LNB_SEARCH_RESULT = 0x25;

    public void gotoEditTextAct(Action action) {
        // mCurrEditItemId = item.id;
        if (action.isEnabled()) {// this is edit item
            Intent intent = null;
            if(action.mItemID.startsWith(MenuConfigManager.DVBS_DETAIL_LNB_FREQUENCY)){
                intent = new Intent(mContext, LNBFreqEditActivity.class);
            }else {
                intent = new Intent(mContext, EditTextActivity.class);
                intent.putExtra(EditTextActivity.EXTRA_DESC, action.getTitle());
            }
            intent.putExtra(EditTextActivity.EXTRA_ITEMID, action.mItemID);
            intent.putExtra(EditTextActivity.EXTRA_PASSWORD, false);
            if (action.mDataType == DataType.NUMVIEW) {
                intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, "" + action.mInitValue);
                intent.putExtra(EditTextActivity.EXTRA_DIGIT, true);
                // dight limit input length 9
                if (action.mItemID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_TP_ITEMS)
                        || action.mItemID.equals(MenuConfigManager.DVBS_SAT_DEDATIL_INFO_TP_ITEMS)) {
                    intent.putExtra(EditTextActivity.EXTRA_LENGTH, 5);
                }else if(action.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE)||
                        action.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE)||
                        action.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_ORBIT_POSITION)){
                    intent.putExtra(EditTextActivity.EXTRA_CANFLOAT, true);
                    intent.putExtra(EditTextActivity.EXTRA_FLAG_SIGNED, true);
                    intent.putExtra(EditTextActivity.EXTRA_LENGTH, 6);
                    if(action.mItemID.equals(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE)){
                        intent.putExtra(EditTextActivity.EXTRA_HINT_TEXT, "[-90.0, 90.0]");
                    }else {
                        intent.putExtra(EditTextActivity.EXTRA_HINT_TEXT, "[-180.0, 180.0]");
                    }
                    if(action.mInitValue != 0){
                        intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, "" + action.mInitValue/10.0);
                    }else {
                        intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, "");
                    }
                }else{
                    intent.putExtra(EditTextActivity.EXTRA_LENGTH, 9);
                }

            } else if (action.mDataType == DataType.EDITTEXTVIEW) {
                // string
                intent.putExtra(EditTextActivity.EXTRA_INITIAL_TEXT, action.getDescription());
                intent.putExtra(EditTextActivity.EXTRA_LENGTH, 16);
            }
            if(action.mItemID.startsWith(MenuConfigManager.DVBS_DETAIL_LNB_FREQUENCY)){
                this.startActivityForResult(intent, REQ_LNB_EDITTEXT);
            }else {
                this.startActivityForResult(intent, REQ_EDITTEXT);
            }
        } else {// this is option item
            // do nothing
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Option Item needn't go to editText Activity");
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSatListShow) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyUp back");
                finish();
                return true;
            }else {
                goBack();
                return true;
            }
          }else {
              return super.onKeyDown(keyCode, event);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyMap.KEYCODE_MTKIR_RED:
                    /*com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Red key down=====isSatListShow:"+SatActivity.isSatListShow);
                    if(SatActivity.isSatListShow){
                         int antennaType = mTV.getConfigValue(ScanContent.SAT_ANTENNA_TYPE);
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "antennaType:"+antennaType);
                         if(antennaType != 0){
                             Action satelliteAtnaType = new Action(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_SET,
                                     mContext.getString(R.string.dvbs_satellite_antenna_type),
                                     MenuConfigManager.INVALID_VALUE,
                                     MenuConfigManager.INVALID_VALUE,
                                     MenuConfigManager.INVALID_VALUE, null,
                                     MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
                             satelliteAtnaType.mSubChildGroup = new ArrayList<Action>();
                             addDVBSAtennaTypeDetail(satelliteAtnaType);

                             if (true) {
                                 onActionClicked(satelliteAtnaType);
                             }
                         }
                    }*/
                    break;
                    default:
                        break;

                    }
            }
        return super.dispatchKeyEvent(event);
    }
    
    int userbandIndex = 0;
    int bandFreqTypeIndex = 0;
    int subUserbandIndex = 0;
    int subBandFreqTypeIndex = 0;
    int userDefineFreq = 0;
    int subuserDefineFreq = 0;
    boolean isDualTuner = CommonIntegration.getInstance().isDualTunerEnable();
    /*private void addDVBSAtennaTypeDetail(Action parentItem) {
        String[] tunerList=null;
        String[] tunerValueList=null;
        if(mAntennaType==1){
            tunerValueList = mResources.getStringArray(R.array.dvbs_user_band_arrays);
            tunerList = new String[tunerValueList.length];
            for(int i = 0; i < tunerValueList.length; i++){
                tunerList[i] = mResources.getString(R.string.dvbs_user_band_item_id, Integer.parseInt(tunerValueList[i]));
            }
        }else if(mAntennaType==2){
            tunerValueList = mResources.getStringArray(R.array.dvbs_user_jess_band_arrays);
            tunerList = new String[tunerValueList.length];
            for(int i = 0; i < tunerValueList.length; i++){
                tunerList[i] = mResources.getString(R.string.dvbs_user_band_item_id, Integer.parseInt(tunerValueList[i]));
            }
        }
        
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satid="+mCurrAction.satID);
        SatelliteInfo info = ScanContent.getDVBSsatellitesBySatID(mContext,mCurrAction.satID);
        if(info != null){
            if(isDualTuner){
                userbandIndex = info.getUserBand();
                subUserbandIndex = info.getSubUserBand();
            }else {
                userbandIndex = info.getUserBand();
            }
        }
        List<String> frequencyList = ScanContent.getSingleCableFreqsList(mContext, userbandIndex);
        String[] freqArray = (String[]) frequencyList.toArray(new String[0]);
        List<String> subFrequencyList = ScanContent.getSingleCableFreqsList(mContext,
                subUserbandIndex);
        String[] subFreqArray = (String[]) subFrequencyList.toArray(new String[0]);
        if(info != null){
            int bandFreq = info.getBandFreq();
            if(isDualTuner){
                if(frequencyList.contains(bandFreq+"")){
                    bandFreqTypeIndex = frequencyList.indexOf(bandFreq+"");
                }else {//user define
                    bandFreqTypeIndex = frequencyList.size() - 1;
                    userDefineFreq = bandFreq;
                }
                int subBandFreq = info.getSubBandFreq();
                if(subFrequencyList.contains(subBandFreq+"")){
                    subBandFreqTypeIndex = subFrequencyList.indexOf(subBandFreq+"");
                }else {//sub user define
                    subBandFreqTypeIndex = subFrequencyList.size() - 1;
                    subuserDefineFreq = subBandFreq;
                }
            }else {
                if(frequencyList.contains(bandFreq+"")){
                    bandFreqTypeIndex = frequencyList.indexOf(bandFreq+"");
                }else {//user define
                    bandFreqTypeIndex = frequencyList.size() - 1;
                    userDefineFreq = bandFreq;
                }
            }
        }
        
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"bandFreqTypeIndex="+bandFreqTypeIndex+",subUserbandIndex="+subUserbandIndex+
                ",subBandFreqTypeIndex="+subBandFreqTypeIndex+",userDefineFreq="+userDefineFreq+",subuserDefineFreq="+subuserDefineFreq);
        parentItem.mSubChildGroup.clear();
        String[] atenaTypeArray = new String[] {
            "Universal", "Single Cable","Jess SingleCable"
        };
        //int antennaType = mTV.getConfigValue(ScanContent.SAT_ANTENNA_TYPE);
        final Action satAtnaType = new Action(
            ScanContent.SAT_ANTENNA_TYPE, mResources.getString(R.string.dvbs_antenna_type),
            MenuConfigManager.INVALID_VALUE,
            MenuConfigManager.INVALID_VALUE,
            mAntennaType,
            atenaTypeArray,
            MenuConfigManager.STEP_VALUE, Action.DataType.SWICHOPTIONVIEW);
         satAtnaType.setEnabled(false);
       
        String tunerTitle = mResources.getString(R.string.dvbs_user_band);
        String[] subTunerValueList = mResources.getStringArray(R.array.dvbs_user_band_arrays);
        String[] subTunerList = new String[subTunerValueList.length];
        for(int i = 0; i < subTunerValueList.length; i++){
            subTunerList[i] = mResources.getString(R.string.dvbs_user_band_item_id, Integer.parseInt(subTunerValueList[i]));
        }
        String subTunerTitle = mResources.getString(R.string.dvbs_sub_user_band);
        final Action satUserBand = new Action(
            MenuConfigManager.DVBS_SAT_ATENNA_TYPE_TUNER, tunerTitle,
            MenuConfigManager.INVALID_VALUE,
            MenuConfigManager.INVALID_VALUE,
            userbandIndex,
            tunerList,
            MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

        final Action subSatUserBand = new Action(
                MenuConfigManager.DVBS_SAT_ATENNA_TYPE_SUB_TUNER, subTunerTitle,
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                subUserbandIndex,
                subTunerList,
                MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

        final Action satBandFreq = new Action(
            MenuConfigManager.DVBS_SAT_ATENNA_TYPE_BANDFREQ, mResources.getString(R.string.dvbs_band_freq),
            MenuConfigManager.INVALID_VALUE,
            MenuConfigManager.INVALID_VALUE,
            bandFreqTypeIndex,
            freqArray,
            MenuConfigManager.STEP_VALUE, Action.DataType.SWICHOPTIONVIEW);
        
        final Action subSatBandFreq = new Action(
                MenuConfigManager.DVBS_SAT_ATENNA_TYPE_SUB_BANDFREQ, mResources.getString(R.string.dvbs_sub_band_frequency),
                MenuConfigManager.INVALID_VALUE,
                MenuConfigManager.INVALID_VALUE,
                subBandFreqTypeIndex,
                subFreqArray,
                MenuConfigManager.STEP_VALUE, Action.DataType.SWICHOPTIONVIEW);

        final Action satUserDefineFreq = new Action(
            MenuConfigManager.DVBS_SAT_ATENNA_TYPE_USERDEF, mResources.getString(R.string.dvbs_user_define_frequency),
            950,
            2150,
            userDefineFreq,
            null,
            MenuConfigManager.STEP_VALUE,
            Action.DataType.NUMVIEW);
        
        final Action subSatUserDefineFreq = new Action(
                MenuConfigManager.DVBS_SAT_ATENNA_TYPE_SUB_USERDEF, mResources.getString(R.string.dvbs_band_freq_user_define),
        950,
        2150,
        subuserDefineFreq,
        null,
        MenuConfigManager.STEP_VALUE,
        Action.DataType.NUMVIEW);

        satAtnaType.mEffectGroup = new ArrayList<Action>();
        satAtnaType.mEffectGroup.add(satUserBand);
        satAtnaType.mEffectGroup.add(subSatUserBand);
        satAtnaType.mEffectGroup.add(satBandFreq);
        satAtnaType.mEffectGroup.add(subSatBandFreq);
        satAtnaType.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
        satAtnaType.mSwitchHashMap.put(0, new Boolean[] {
                false, false ,false ,false
        });
        satAtnaType.mSwitchHashMap.put(1, new Boolean[] {
                true, true,true,true
        });

        satBandFreq.mEffectGroup = new ArrayList<Action>();
        satBandFreq.mEffectGroup.add(satUserDefineFreq);
        subSatBandFreq.mEffectGroup = new ArrayList<Action>();
        subSatBandFreq.mEffectGroup.add(subSatUserDefineFreq);
        satBandFreq.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
        int len = satBandFreq.mOptionValue.length;
        for (int i = 0; i < len; i++) {
            satBandFreq.mSwitchHashMap.put(i, new Boolean[] {
                true
            });
        }
        subSatBandFreq.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
        int subLen = subSatBandFreq.mOptionValue.length;
        for (int i = 0; i < subLen; i++) {
            if (i == subLen - 1) {
                subSatBandFreq.mSwitchHashMap.put(i, new Boolean[] {
                        true
                });
            } else {
                subSatBandFreq.mSwitchHashMap.put(i, new Boolean[] {
                        false
                });
            }
        }

        satAtnaType.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

          @Override
          public void afterOptionValseChanged(String afterName) {
            userbandIndex = 0;
            bandFreqTypeIndex = 0;
            satUserBand.mInitValue = userbandIndex;
            satUserBand.setDescription(userbandIndex);
            List<String> freqlist = ScanContent.getSingleCableFreqsList(mContext, userbandIndex);
            String[] freqArray = (String[]) freqlist.toArray(new String[0]);
            satBandFreq.mInitValue = bandFreqTypeIndex;
            satBandFreq.mOptionValue = freqArray;
            satBandFreq.setDescription(bandFreqTypeIndex);
            if (satAtnaType.mInitValue == 0) {// universal
              List<Action> childList = satAtnaType.mParent.mSubChildGroup;
              if (childList.contains(satUserBand)) {
                childList.remove(satUserBand);
                childList.remove(satBandFreq);
                childList.remove(satUserDefineFreq);
                childList.remove(subSatUserBand);
                childList.remove(subSatBandFreq);
                childList.remove(subSatUserDefineFreq);
              }
            } else if (satAtnaType.mInitValue == 1) {// single cable
                List<Action> childList = satAtnaType.mParent.mSubChildGroup;
                if (!childList.contains(satUserBand)) {
                    childList.add(1,satUserBand);
                    childList.add(2,satBandFreq);
                    String bandFreqDes = satBandFreq.getDescription();
                    String userDefStr = mContext.getString(R.string.dvbs_band_freq_user_define);
                    String subBandFreqDes = subSatBandFreq.getDescription();
                    String subUserDefStr = mContext.getString(R.string.dvbs_band_freq_user_define);
                    if (bandFreqDes.equalsIgnoreCase(userDefStr)) {
                        childList.add(3,satUserDefineFreq);
                        childList.add(4,subSatUserBand);
                        childList.add(5,subSatBandFreq);
                        if (subBandFreqDes.equalsIgnoreCase(subUserDefStr)) {
                            childList.add(6,subSatUserDefineFreq);
                        }
                    }else {
                        childList.add(3,subSatUserBand);
                        childList.add(4,subSatBandFreq);
                        if (subBandFreqDes.equalsIgnoreCase(subUserDefStr)) {
                            childList.add(5,subSatUserDefineFreq);
                        }
                    }
                }
            }else if (satAtnaType.mInitValue == 2) {//jess single cable
                List<Action> childList = satAtnaType.mParent.mSubChildGroup;
                if (!childList.contains(satUserBand)) {
                  childList.add(satUserBand);
                  childList.add(satBandFreq);
                  String bandFreqDes = satBandFreq.getDescription();
                  String userDefStr = mContext.getString(R.string.dvbs_band_freq_user_define);
                  if (bandFreqDes.equalsIgnoreCase(userDefStr)) {
                    childList.add(satUserDefineFreq);
                  }
                }
              }
          }

        });

        satUserBand.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

          @Override
          public void afterOptionValseChanged(String afterName) {
            userbandIndex = satUserBand.mInitValue;
            //SaveValue.getInstance(mContext).saveValue("userbandIndex", userbandIndex);
            bandFreqTypeIndex = 0;
            //SaveValue.getInstance(mContext).saveValue("bandFreqTypeIndex", bandFreqTypeIndex);
            List<String> freqlist = ScanContent.getSingleCableFreqsList(mContext, userbandIndex);
            String[] freqArray = (String[]) freqlist.toArray(new String[0]);
            satBandFreq.mInitValue = bandFreqTypeIndex;
            satBandFreq.mOptionValue = freqArray;
            satBandFreq.setDescription(bandFreqTypeIndex);
            List<Action> childList = satAtnaType.mParent.mSubChildGroup;
            if (satBandFreq.mOptionValue[bandFreqTypeIndex].equalsIgnoreCase(mContext.getString(R.string.dvbs_band_freq_user_define))) {
              if (!childList.contains(satUserDefineFreq)) {
                satUserDefineFreq.setEnabled(true);
                childList.add(satUserDefineFreq);
              }
            } else {
              if (childList.contains(satUserDefineFreq)) {
                 childList.remove(satUserDefineFreq);
              }
            }
          }

        });

        subSatUserBand.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

            @Override
            public void afterOptionValseChanged(String afterName) {
                subUserbandIndex = subSatUserBand.mInitValue;
                //SaveValue.getInstance(mContext).saveValue("subUserbandIndex", subUserbandIndex);
                subBandFreqTypeIndex = 0;
                //SaveValue.getInstance(mContext).saveValue("subBandFreqTypeIndex", subBandFreqTypeIndex);
                List<String> freqList = ScanContent
                        .getSingleCableFreqsList(mContext, subUserbandIndex);
                String[] freqArray = (String[]) freqList.toArray(new String[0]);
                subSatBandFreq.mInitValue = subBandFreqTypeIndex;
                subSatBandFreq.mOptionValue = freqArray;
                subSatBandFreq.setDescription(subBandFreqTypeIndex);
            }
        });

        satBandFreq.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

          @Override
          public void afterOptionValseChanged(String afterName) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satBandFreq afterName:" + afterName);
            bandFreqTypeIndex = satBandFreq.mInitValue;
            //SaveValue.getInstance(mContext).saveValue("bandFreqTypeIndex", bandFreqTypeIndex);
            List<Action> childList = satAtnaType.mParent.mSubChildGroup;
            if (afterName.equalsIgnoreCase(mContext.getString(R.string.dvbs_band_freq_user_define))) {
              if (!childList.contains(satUserDefineFreq)) {
                satUserDefineFreq.setEnabled(true);
                if(childList.contains(subSatUserDefineFreq)){
                    childList.remove(subSatUserDefineFreq);
                    childList.add(satUserDefineFreq);
                    childList.add(subSatUserDefineFreq);
                }else {
                    childList.add(satUserDefineFreq);
                }
              }
            } else {
              if (childList.contains(satUserDefineFreq)) {
                childList.remove(satUserDefineFreq);
              }
            }
          }

        });

        subSatBandFreq.setOptionValueChangedCallBack(new OptionValuseChangedCallBack() {

            @Override
            public void afterOptionValseChanged(String afterName) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subSatBandFreq afterName:" + afterName);
                subBandFreqTypeIndex = subSatBandFreq.mInitValue;
                //SaveValue.getInstance(mContext).saveValue("subBandFreqTypeIndex", subBandFreqTypeIndex);
                List<Action> childList = satAtnaType.mParent.mSubChildGroup;
                if (afterName.equalsIgnoreCase(mContext
                        .getString(R.string.dvbs_band_freq_user_define))) {
                    if (!childList.contains(subSatUserDefineFreq)) {
                        childList.add(subSatUserDefineFreq);
                    }
                } else {
                    if (childList.contains(subSatUserDefineFreq)) {
                        childList.remove(subSatUserDefineFreq);
                    }
                }
            }
        });


        parentItem.mSubChildGroup.add(satAtnaType);

        if (mAntennaType == 0) {// universal
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mAntennaType == 0");
          
           * satUserBand.setEnabled(false); satBandFreq.setEnabled(false);
           * satUserDefineFreq.setEnabled(false);
           
        } else if (mAntennaType == 1||mAntennaType == 2) {// single cable
          parentItem.mSubChildGroup.add(satUserBand);
          parentItem.mSubChildGroup.add(satBandFreq);
          if(isDualTuner){
              parentItem.mSubChildGroup.add(subSatUserBand);
              parentItem.mSubChildGroup.add(subSatBandFreq);
          }
          if (bandFreqTypeIndex == len - 1) {
            parentItem.mSubChildGroup.add(satUserDefineFreq);
          }
          
          if (subBandFreqTypeIndex == len - 1 && isDualTuner) {
              parentItem.mSubChildGroup.add(subSatUserDefineFreq);
          }
        }

        satAtnaType.mParent = parentItem;
        satUserBand.mParent = parentItem;
        satBandFreq.mParent = parentItem;
        satUserDefineFreq.mParent = parentItem;
        subSatUserBand.mParent = parentItem;
        subSatBandFreq.mParent = parentItem;
        subSatUserDefineFreq.mParent = parentItem;

    }*/
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
    int mDVBSCurrentOP = -1;
    @Override
    protected void updateView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateView");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DataType=" + (Action.DataType) mState);
        switch ((Action.DataType) mState) {
            case TOPVIEW:
            case OPTIONVIEW:
            case SWICHOPTIONVIEW:
            case EFFECTOPTIONVIEW:
            case HAVESUBCHILD:
            case SATELITEDETAIL:
            case LEFTRIGHT_HASCHILDVIEW:
                refreshActionList();
                setView(mCurrAction == null ? "Menu TV" : mCurrAction.getTitle(),
                        mParentAction == null ? mContext.getResources().getString(
                                R.string.menu_interface_name) : mParentAction
                                .getTitle(), "", R.drawable.menu_tv_icon);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrAction.mItemID.equals(MenuConfigManager.DVBS_SAT_RE_SCAN)) {
                            // Set DVBS operator selection.
                            List<String> dvbsOPListStr = ScanContent.getDVBSOperatorList(mContext);
                            if (dvbsOPListStr != null && dvbsOPListStr.size() > 0) {
                                String opStr = ScanContent.getDVBSCurrentOPStr(mContext);
                                for (String dvbsOP : dvbsOPListStr) {
                                    if (dvbsOP.equalsIgnoreCase(opStr)) {
                                        break;
                                    }
                                }
                                // ((ActionFragment) mActionFragment).
                                // getScrollAdapterView().setSelectionSmooth(position);
                            }
                        } else if (mCurrAction.mItemID
                                .equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBC)) {
                            // Set DVBC operator selection.
                            List<String> operatorListStr = ScanContent
                                    .getCableOperationList(mContext);
                            if (operatorListStr != null && operatorListStr.size() > 0) {
                                int position = 0;
                                int size = operatorListStr.size();
                                String opStr = ScanContent.getCurrentOperatorStr(mContext);
                                for (int i = 0; i < size; i++) {
                                    if (operatorListStr.get(i).equalsIgnoreCase(opStr)) {
                                        position = i;
                                        break;
                                    }
                                }
                                ((ActionFragment) mActionFragment).getScrollAdapterView()
                                        .setSelectionSmooth(
                                                position);
                            }
                        }
                    }
                }, 500);
                break;
            case SATELITEINFO:
               /* mDVBSCurrentOP = ScanContent.setSelectedSatelliteOPFromMenu(mContext,
                        selectPos);*/
                //ScanContent.setDVBSCurroperator(mDVBSCurrentOP);
                SatListFrag frag = new SatListFrag();
                frag.setAction(mCurrAction);
                frag.setSelectPos(selectPos);
                if(MenuConfigManager.DVBS_SAT_OP_CAM_SCAN.equals(mItemId)) {
                    frag.setCamOpName(title);
                }
                mActionFragment = frag;
                setViewWithActionFragment(mCurrAction == null ? " TV" : mCurrAction.getTitle(),
                        mParentAction == null ? mContext.getResources().
                                getString(R.string.menu_interface_name) : mParentAction.getTitle(),
                        "", R.drawable.menu_tv_icon);
                if(MenuConfigManager.DVBS_SAT_OP.equals(mItemId)) {
                    frag.setNeedCheckM7Scan(true);
                }
                ScanContent.setTpHasModifiedForUpdateScan(false);
                /*if (mTV.isM7ScanMode()) {
                    showM7LNBScanConfirmDialog();
                }*/
                break;
            default:
                break;
        }

    }

    private SimpleDialog cleanDialog;

    public void showM7LNBScanConfirmDialog() {
        String m7confirmInfo = mContext.getString(R.string.menu_tv_m7_scan_confirm_new);
        cleanDialog = new SimpleDialog(mContext);
        cleanDialog.setConfirmText(R.string.menu_c_scan);
        cleanDialog.setCancelText(R.string.dvbs_lnb_skip_scan);
        cleanDialog.setContent(m7confirmInfo);
        cleanDialog.setOnCancelClickListener(new OnCancelClickListener() {
            @Override
            public void onCancelClick(int dialogId) {
                DVBSScanner.initLNBDetectedMast(mContext);
                cleanDialog.dismiss();
            }
        }, 1);
        cleanDialog.setOnConfirmClickListener(new OnConfirmClickListener() {
            @Override
            public void onConfirmClick(int dialogId) {
                startActivityToDoM7LNBScan();
                cleanDialog.dismiss();
            }
        }, 1);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        lp.gravity = Gravity.START;
        lp.x = (int)(display.getWidth() * 0.625 - mContext.getResources().getDimensionPixelSize(R.dimen.scan_third_dialog_width))/2 + 10;
        cleanDialog.getWindow().setAttributes(lp);
        cleanDialog.show();
    }

    private void startActivityToDoM7LNBScan(){
        Intent intent = new Intent(mContext, ScanDialogActivity.class);
        intent.putExtra("ActionID", MenuConfigManager.M7_LNB_Scan);
        intent.putExtra("lnb", true);
        this.startActivityForResult(intent,REQ_LNB_SEARCH_RESULT);
    }

    @Override
    protected void setActionsForTopView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActionsForTopView");
    }

    @Override
    protected void setProperty(boolean enable) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setProperty");
    }

    protected void setViewWithActionFragment(String title, String breadcrumb, String description,
            int iconResId) {
        mContentFragment = ContentFragment.newInstance(title, breadcrumb, description, iconResId,
                getResources().getColor(R.color.icon_background));
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }
    
    public void sendMessageDelayedThread(final int what, final long delayMillis) {
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            public void run() {
                int level = mTV.getSignalLevel();
                int quality = mTV.getSignalQuality();
                if(!isDestroyed()) {
                    Message msg = Message.obtain();
                    msg.what = what;
                    msg.arg1 = level;
                    msg.arg2 = quality;
                    mHandler.sendMessageDelayed(msg, delayMillis);
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        SatDetailUI.setMySelfNull();
		mDataHelper.setIsCamScanUI(false);
        super.onDestroy();
    }
}
