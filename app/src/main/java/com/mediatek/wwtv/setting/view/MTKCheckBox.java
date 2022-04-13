package com.mediatek.wwtv.setting.view;

import android.util.AttributeSet;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;

import android.widget.CheckBox;
public class MTKCheckBox extends CheckBox{
    private boolean mIsChecked;
    
    public MTKCheckBox(Context context,AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public void setChecked(boolean checked){
        super.setChecked(checked);
        mIsChecked = checked;
    }
    
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event){
        super.onInitializeAccessibilityEvent(event);
        event.setChecked(!mIsChecked);
    }

    @Override
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setChecked(!mIsChecked);
    }
}
