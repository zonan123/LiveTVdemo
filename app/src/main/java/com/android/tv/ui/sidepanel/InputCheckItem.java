/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.tv.ui.sidepanel;

import android.view.View;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;

public abstract class InputCheckItem extends CompoundButtonItem {
//    private TextView mProgramTitleView;
    private TextView mChannelNumberView;
    private String mSourceName;


    public InputCheckItem(
            String inputName) {
        super(inputName,null);
        mSourceName = inputName;
    }

    protected String getInputName() {
        return mSourceName;
    }

    @Override
    protected int getResourceId() {
        return R.layout.option_item_channel_check;
    }

    @Override
    protected int getCompoundButtonId() {
        return R.id.check_box;
    }

    @Override
    protected int getTitleViewId() {
        return R.id.channel_name;
    }

    @Override
    protected int getDescriptionViewId() {
        return R.id.program_title;
    }

    @Override
    protected void onBind(View view) {
        super.onBind(view);
        mChannelNumberView = (TextView) view.findViewById(R.id.channel_number);
        //mProgramTitleView = (TextView) view.findViewById(R.id.program_title);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
//        mChannelNumberView.setText(mChannel.getChannelNumber()+"");
        mChannelNumberView.setText("");
//        updateProgramTitle();
    }

    @Override
    protected void onUnbind() {
        //mChannelDataManager.removeChannelListener(mChannel.getId(), mChannelListener);
        //mProgramDataManager.removeOnCurrentProgramUpdatedListener(
        //        mChannel.getId(), mOnCurrentProgramUpdatedListener);
        //mProgramTitleView = null;
        mChannelNumberView = null;
        super.onUnbind();
    }

    @Override
    protected void onSelected() {
        setChecked(!isChecked());
    }

//    private void updateProgramTitle() {
//        String title = null;
       
//        if (TextUtils.isEmpty(title)) {
//            title = mProgramTitleView.getContext().getString(R.string.no_program_information);
//        }
//        mProgramTitleView.setText(title);
//    }
}
