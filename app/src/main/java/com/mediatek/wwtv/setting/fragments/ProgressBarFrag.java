package com.mediatek.wwtv.setting.fragments;

import java.util.List;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;

import android.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class ProgressBarFrag extends Fragment{
     /**
     * Object listening for adapter events.
     */
    public interface ResultListener {
        void onCommitResult(List<String> result);
    }
    private Action mAction;
    private ProgressBar mProgressView;
    private SeekBar mSeekBarView;
    private TextView mValueView;

    private int mPostion;
    private int mOffset;
    private MenuConfigManager mConfigManager;
    private String mId;
    boolean isPositionView = false ;
    /*
     * when press LEFT or RIGHT, the decrease or increase value every time
     * default value is 1
     */
    private int mStepValue;

    public void setAction(Action action){
        mAction = action;
        if(action.mDataType == DataType.POSITIONVIEW){
            isPositionView = true;
        }
    }

    public String getActionId() {
        return mId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "savedInstanceState="+savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup mRootView;
        mRootView = (ViewGroup)inflater.inflate(R.layout.progress_frag, null);
        mProgressView =(ProgressBar) mRootView.findViewById(R.id.progress_view);
        mSeekBarView = (SeekBar)mRootView.findViewById(R.id.seekbar_view);
        mValueView = (TextView) mRootView.findViewById(R.id.progress_value);
        if(isPositionView){
            mSeekBarView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
        }
        bindData();
        return mRootView;
    }
    int pMax = 0;
    private void bindData(){
        mConfigManager = MenuConfigManager.getInstance(getActivity());
        mId = mAction.mItemID;
        mOffset = -mAction.getmStartValue();
        if(!isPositionView){
            mProgressView.setMax(mAction.getmEndValue()
                    - mAction.getmStartValue());
        }else{
            mSeekBarView.setMax(mAction.getmEndValue()
                    - mAction.getmStartValue());
        }

        mPostion = mAction.getmInitValue() + mOffset;
        mStepValue = mAction.getmStepValue();
        setProgressAndValue(mPostion,false);

    }

    public void setValue(int mInitValue) {
        mPostion = mInitValue + mOffset;
        mAction.mInitValue = mInitValue;
        showValue(mAction.mInitValue);
        mConfigManager.setActionValue(mAction);
    }

    public void showValue(int value) {
        mValueView.setText(String.valueOf(value));
        if (MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_LEVEL.equals(mAction.mItemID)
                || MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_QUALITY.equals(mAction.mItemID)
                || MenuConfigManager.DVBS_SIGNAL_QULITY.equals(mAction.mItemID)
                || MenuConfigManager.DVBS_SIGNAL_LEVEL.equals(mAction.mItemID)) {
            if (CommonIntegration.isEURegion()) {
                mValueView.setText(getActivity().getResources().getString(R.string.nav_percent_sign, value));
            } else {
                mValueView.setText(String.valueOf(value));
            }
        }
        mAction.mInitValue = value;
        mPostion = value + mOffset;

        if(!isPositionView){
            mProgressView.setProgress(mPostion);
        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFlag", "seekbar progress:"+mPostion);
            mSeekBarView.setProgress(mPostion);
        }

        mAction.setDescription(value);
    }


    private void setProgressAndValue(int postion ,boolean fromUser){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFlag", "seekbar postion:"+postion+",fromUser="+fromUser);
//      mProgressView.setProgress(mPostion);
//      if(!fromUser){
        showValue(mAction.mInitValue);
//      }else{
//          showValue(mAction.mInitValue);
//      }
    }

    public void onKeyLeft() {
        if(mAction.isSupportModify()){
            switchValuePrevious();
        }

    }

    public void onKeyRight() {
        if(mAction.isSupportModify()){
            switchValueNext();
        }
    }

    private void switchValuePrevious() {
        resetColorTempUser();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressView", "switchValuePrevious mPostion:" + mPostion);
        if (mPostion > 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressView", "switchValuePrevious mAction.mInitValue >>" + mAction.mInitValue + " >>  " + mAction.mItemID
                    + "   " + MenuConfigManager.VPOSITION);
            if (mAction.mItemID.equals(MenuConfigManager.VPOSITION)) {
                int ret = setVPositionPrevious(mAction.mInitValue);
                setProgressAndValue(ret,true);
            }else{
                mPostion = mPostion - mStepValue;
                mAction.mInitValue = mPostion - mOffset;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "mAction.mInitValue=="+mAction.mInitValue);
                setProgressAndValue(mAction.mInitValue,true);
                mConfigManager.setActionValue(mAction);
            }

        }
    }

    private void switchValueNext() {
        resetColorTempUser();
        int max = -1;
        if(!isPositionView){
            max = mProgressView.getMax();
        }else{
            max = mSeekBarView.getMax();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("switchValueNext","getMax():" + max
                + "----mPostion:" + mPostion);
        if (mPostion < max) {

            if (mAction.mItemID.equals(MenuConfigManager.VPOSITION)) {
                int ret = setVPositionNext(mAction.mInitValue);
                setProgressAndValue(ret,true);
            }else{
                mPostion = mPostion + mStepValue;
                mAction.mInitValue = mPostion - mOffset;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "mAction.mInitValue=="+mAction.mInitValue);
                setProgressAndValue(mAction.mInitValue,true);
                mConfigManager.setActionValue(mAction);
            }

        }
    }

    private void resetColorTempUser() {
        if (mAction.mItemID.equals(MenuConfigManager.COLOR_G_R)
                || mAction.mItemID.equals(MenuConfigManager.COLOR_G_G)
                || mAction.mItemID.equals(MenuConfigManager.COLOR_G_B)) {
            if (mAction.mParentGroup.size() < 6) {// factory mode have six
                                                    // data item
                mConfigManager.setValue(MenuConfigManager.COLOR_TEMPERATURE, 0,
                        mAction);
            }
        }
    }

    /*public void removeCallback(){
    }*/

    private int setVPositionNext(int value){
        value ++;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "setVPositionNext value=="+value);
        do{
            mAction.mInitValue = value;
            mConfigManager.setActionValue(mAction);
            int newValue = MenuConfigManager.getInstance(getActivity()).getDefault(MenuConfigManager.VPOSITION);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "setVPositionNext after newValue=="+newValue);
            if(newValue >= value){
                value = newValue;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Next","biaoqing Next newValue=="+newValue+",bigger value=="+value);
                break;
            }else{
                value++;
                if(value >=100){
                    break;
                }
            }
        }while(true);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "setVPositionNext after value=="+value);
        mAction.mInitValue = value;
        return value;
    }


    private int setVPositionPrevious(int value){
        value--;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "setVPositionPrevious value=="+value);
        do{
            mAction.mInitValue = value;
            mConfigManager.setActionValue(mAction);
            int newValue = MenuConfigManager.getInstance(getActivity()).getDefault(MenuConfigManager.VPOSITION);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("ProgressFrag", "setVPositionPrevious getDefault=="+newValue+",value=="+value);
            if(newValue <= value){
                value = newValue;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Previous","biaoqing newValue=="+newValue);
                break;
            }else{
                value--;
                if(value <=0){
                    break;
                }
            }
        }while(true);
        mAction.mInitValue = value;
        return value;
    }


}
