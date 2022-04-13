/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tv.dialog.picker;

import android.content.Context;
//import android.support.annotation.Nullable;
//import android.support.annotation.VisibleForTesting;
//import android.support.v17.leanback.widget.picker.Picker;
//import android.support.v17.leanback.widget.picker.PickerColumn;
import com.mediatek.wwtv.tvcenter.util.KeyMap;


import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.leanback.widget.picker.Picker;
import androidx.leanback.widget.picker.PickerColumn;
import androidx.leanback.widget.VerticalGridView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** 4 digit picker */
public final class PinPicker extends Picker {
	private static final String TAG = "PinPicker";
    private final List<PickerColumn> mPickers = new ArrayList<>();
    private OnClickListener mOnClickListener;
    private OnKeyDownCallback mKeyDownCallback;
    /**
     * if enable press centerkey when first show PinPicker
     */
    private boolean mDisableFirstCenterKey=false;
    
    private boolean mIsFirstEnter=true;
    
   // the version of picker I link to does not have this constructor
    public PinPicker(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }
    
    public void setDisableFirstCenterKey(boolean disableFirstCenterKey){
    	mDisableFirstCenterKey=disableFirstCenterKey;
    }
    
    public void setFirstEnter(boolean isFirstEnter){
    	mIsFirstEnter=isFirstEnter;
    }
    
    public void setOnKeyDownCallback(OnKeyDownCallback keyDownCallback){
    	mKeyDownCallback=keyDownCallback;
    }


    public PinPicker(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        mIsFirstEnter=true;
        for (int i = 0; i < 4; i++) {
            PickerColumn pickerColumn = new PickerColumn();
            pickerColumn.setMinValue(0);
            pickerColumn.setMaxValue(9);
            pickerColumn.setLabelFormat("%d");
            mPickers.add(pickerColumn);
        }
        setSeparator(" ");
        setColumns(mPickers);
        setActivated(true);
        setFocusable(true);
        super.setOnClickListener(this::onClick);
        for (int j = 0; j < this.getChildCount(); j++) {
            View child = this.getChildAt(j);
            child.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        try {
            Class<?> superClass = this.getClass().getSuperclass();
            Field columeField = superClass.getDeclaredField("mColumnViews");
            columeField.setAccessible(true);
            List<VerticalGridView> columes = (ArrayList<VerticalGridView>)columeField.get(this);
            for (VerticalGridView verticalGirdView : columes) {
                //verticalGirdView.setVerticalMargin(13);
            	float dp = 11.0f;
            	float scale = context.getResources().getDisplayMetrics().density;
            	int px = (int)(dp*scale+0.5f);
            	verticalGirdView.setVerticalMargin(px);
            }
        } catch (NoSuchFieldException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NoSuchFieldException msg:" + e.getMessage());
        } catch (IllegalAccessException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "IllegalAccessException msg:" + e.getMessage());
        }
    }
    

    public String getPinInput() {
        String result = "";
        try {
            for (PickerColumn column : mPickers) {

                result += column.getCurrentValue();
            }
        } catch (IllegalStateException e) {
            result = "";
        }
        return result;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    private void onClick(View v) {
        int selectedColumn = getSelectedColumn();
        if(selectedColumn > 0){
            mIsFirstEnter=false;
        }
        if(mDisableFirstCenterKey&&mIsFirstEnter){
            mIsFirstEnter=false;
            return;
        }
        int nextColumn = selectedColumn + 1;
        // Only call the click listener if we are on the last column
        // Otherwise move to the next column
        if (nextColumn == getColumnsCount()) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v);
            }
        } else {
            setSelectedColumn(nextColumn);
            onRequestFocusInDescendants(ViewGroup.FOCUS_FORWARD, null);
        }
    }

    public void resetPinInput() {
        setActivated(false);
        for (int i = 0; i < 4; i++) {
            setColumnValue(i, 0, true);
        }
        setSelectedColumn(0);
        setActivated(true); // This resets the focus
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP) {
            int digit = digitFromKeyCode(keyCode);
            if (digit != -1) {
                int selectedColumn = getSelectedColumn();
                setColumnValue(selectedColumn, digit, false);
                int nextColumn = selectedColumn + 1;
                int columnsCount = getColumnsCount();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nextColumn="+nextColumn+",selectedColumn="+selectedColumn+",columnsCount="+columnsCount);
                if (nextColumn < columnsCount) {
                    setSelectedColumn(nextColumn);
                    onRequestFocusInDescendants(ViewGroup.FOCUS_FORWARD, null);
                } else {
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "callOnClick is called!");
                    callOnClick();
                }
                return true;
            }
        }
        else if(event.getAction() == KeyEvent.ACTION_DOWN){
        	switch (keyCode) {
            case KeyMap.KEYCODE_DPAD_DOWN:
            case KeyMap.KEYCODE_DPAD_UP:
            case KeyMap.KEYCODE_DPAD_LEFT:
            case KeyMap.KEYCODE_DPAD_RIGHT:
            case KeyMap.KEYCODE_0:
            case KeyMap.KEYCODE_1:
            case KeyMap.KEYCODE_2:
            case KeyMap.KEYCODE_3:
            case KeyMap.KEYCODE_4:
            case KeyMap.KEYCODE_5:
            case KeyMap.KEYCODE_6:
            case KeyMap.KEYCODE_7:
            case KeyMap.KEYCODE_8:
            case KeyMap.KEYCODE_9:
                if(mKeyDownCallback!=null) {
             	   mKeyDownCallback.onKeyDown(keyCode);
                }
                break;
            default:
            	break;
     	   }
        }
        return super.dispatchKeyEvent(event);
    }

    @VisibleForTesting
    static int digitFromKeyCode(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            return keyCode - KeyEvent.KEYCODE_0;
        } else if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            return keyCode - KeyEvent.KEYCODE_NUMPAD_0;
        }
        return -1;
    }
    
    public interface OnKeyDownCallback {
    	void onKeyDown(int keyCode);
    }
}
