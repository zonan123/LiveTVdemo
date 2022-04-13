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

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;

public class CheckBoxItem extends CompoundButtonItem {
    private static final String TAG = "CheckBoxItem";
    private MtkTvChannelInfoBase mChannel;
    private final boolean mLayoutForLargeDescription;
    private TextView mChannelNumberView;
    protected MtkTvChannelInfoBase getChannel() {
        return mChannel;
    }
    public CheckBoxItem(String title) {
        this(title, null);
    }
  

    public CheckBoxItem(String title, String description) {
        this(title, description, false);
    }

    public CheckBoxItem(String title, String description, boolean layoutForLargeDescription) {
        super(title, description);
        mChannel=null;
        mLayoutForLargeDescription = layoutForLargeDescription;
    }
    public CheckBoxItem(
            MtkTvChannelInfoBase channel/*,
            TIFChannelManager channelDataManager,
            TIFProgramManager programDataManager*/) {
        super(""+ ((channel.getServiceName() == null) ? "" : channel.getServiceName())
                        , "");
        mChannel = channel;
        mLayoutForLargeDescription = false;
    }
    @Override
    protected void onBind(View view) {
        super.onBind(view);
        mChannelNumberView = (TextView) view.findViewById(R.id.channel_number);
//        mProgramTitleView = (TextView) view.findViewById(R.id.program_title);
        if (mLayoutForLargeDescription) {
            Log.d(TAG, "onBind()");
            CompoundButton checkBox = (CompoundButton) view.findViewById(getCompoundButtonId());
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) checkBox.getLayoutParams();
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.topMargin =
                    view.getResources()
                            .getDimensionPixelOffset(R.dimen.option_item_check_box_margin_top);
            checkBox.setLayoutParams(lp);

            TypedValue outValue = new TypedValue();
            view.getResources()
                    .getValue(
                            R.dimen.option_item_check_box_line_spacing_multiplier, outValue, true);

            TextView descriptionTextView = (TextView) view.findViewById(getDescriptionViewId());
            descriptionTextView.setMaxLines(Integer.MAX_VALUE);
            descriptionTextView.setLineSpacing(0, outValue.getFloat());
        }
    }

    @Override
    protected int getResourceId() {
        return R.layout.option_item_check_box;
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
    protected void onSelected() {
        setChecked(!isChecked());
    }
    

    @Override
    protected void onUpdate() {
        super.onUpdate();
        if(mChannel!=null&&mChannelNumberView!=null){
            String number ="";
            if (mChannel instanceof MtkTvATSCChannelInfo) {
                MtkTvATSCChannelInfo uschannel=(MtkTvATSCChannelInfo) mChannel;
                number = uschannel.getMajorNum()+"-"+uschannel.getMinorNum();
            }else{
                number =mChannel.getChannelNumber()+"";
            }
            mChannelNumberView.setText(number);
        }
        updateProgramTitle();
    }
    
    private void updateProgramTitle() {
        //deleted by jg_jianwang for DTV01049845
    	/*if(mProgramTitleView!=null){
	        String title = null;
	        
	        if (TextUtils.isEmpty(title)) {
	            title = mProgramTitleView.getContext().getString(R.string.no_program_information);
	        }
	        mProgramTitleView.setText(title);
    	}*/
    }

}
