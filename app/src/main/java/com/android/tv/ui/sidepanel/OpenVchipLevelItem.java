package com.android.tv.ui.sidepanel;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;

public class OpenVchipLevelItem extends CompoundButtonItem {
    private TextView mProgramTitleView;
    private TextView mChannelNumberView;
    private int mOpenVchipRegion = 0;
    private int mOpenVchipDim = 0;
    private int mOpenVchipLevel = 0;
    private static final String TAG = "OpenVchipLevelItem";



    public OpenVchipLevelItem(
            int mRegion,
            int mDim,
            int mLevel,
            String mVchipLevelString) {
        super(mVchipLevelString,"");
        mOpenVchipRegion = mRegion;
        mOpenVchipDim = mDim;
        mOpenVchipLevel = mLevel;
    }

    public int getmOpenVchipDim() {
        return mOpenVchipDim;
    }
    
    public int getmOpenVchipLevel() {
        return mOpenVchipLevel;
    }
    
    public int getmOpenVchipRegion() {
        return mOpenVchipRegion;
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
        mProgramTitleView = (TextView) view.findViewById(R.id.program_title);
        Log.d(TAG, "onBind: " + mChannelNumberView.toString() + mProgramTitleView.toString());
    }

    @Override
    protected void onUpdate() {
        Log.v(TAG,"onUpdate() ");
        super.onUpdate();
    }

    @Override
    protected void onUnbind() {
        mProgramTitleView = null;
        mChannelNumberView = null;
        Log.v(TAG,"onUnbind() ");
        super.onUnbind();
    }

    @Override
    protected void onSelected() {
        setChecked(!isChecked());
    }

}
