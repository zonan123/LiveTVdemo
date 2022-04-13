package com.mediatek.wwtv.setting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.mediatek.wwtv.setting.base.scan.model.DVBTScanner;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;

public class ScanOptionView extends TextView{

    Context mContext ;
    String[] mOptionValues ;
    String mItemID;
    int initValue ;
    MenuConfigManager mConfigManager ;
    TVContent mTv;
    boolean isEnable;

    public ScanOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context ;
    }

    //must call after view init
    public void bindData(String id,String[] optionValues,int defVal){
        mConfigManager = MenuConfigManager.getInstance(mContext);
        mTv = TVContent.getInstance(mContext);
        mItemID = id;
        mOptionValues = optionValues;
        initValue = defVal ;
        this.setText(mOptionValues[initValue]);

    }

    public void  updateValue(int initVal){
        initValue = initVal;
        this.setText(mOptionValues[initValue]);
    }

    //key left
    public void onKeyLeft(){
        //dvbt single rf-scan-channels special handle
        if(mItemID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)){
            String value = mTv.getRFChannel(DVBTScanner.RF_CHANNEL_PREVIOUS);
            mOptionValues[initValue] = value;
            this.setText(mOptionValues[initValue]);
            return;
        }
        initValue -- ;
        if(initValue < 0){
            initValue = mOptionValues.length -1;
        }
        this.setText(mOptionValues[initValue]);
        //mConfigManager.setValue(mItemID, initValue,null);

    }

    //key right
    public void onKeyRight(){
        //dvbt single rf-scan-channels special handle
        if(mItemID.equals(MenuConfigManager.TV_SINGLE_RF_SCAN_CHANNELS)){
            String value = mTv.getRFChannel(DVBTScanner.RF_CHANNEL_NEXT);
            mOptionValues[initValue] = value;
            this.setText(mOptionValues[initValue]);
            return;
        }
        initValue ++ ;
        if(initValue > mOptionValues.length -1){
            initValue = 0;
        }
        this.setText(mOptionValues[initValue]);
        //mConfigManager.setValue(mItemID, initValue,null);
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }



}
