package com.android.tv.ui.sidepanel;

import android.util.Log;

public abstract class OpenVchipSubMenu extends SubMenuItem {
    private final SideFragmentManager mSideFragmentManager;
    private static final String TAG = "OpenVchipSubMenu";
    private int mRegionId = 0;
    private int mDimId = 0;
    private int mLevelId = 0;
    private int mTypeId = 0;

    public OpenVchipSubMenu(String title, SideFragmentManager fragmentManager) {
        super(title, fragmentManager);
        mSideFragmentManager = fragmentManager;
    }

    public int getmTypeId() {
        return mTypeId;
    }

    public int getmRegionId() {
        return mRegionId;
    }

    public int getmDimId() {
        return mDimId;
    }

    public int getmLevelId() {
        return mLevelId;
    }

    //add by jg_jianwang for DTV01100987
    public String getTitle(){
        return mTitle;
    }

    public OpenVchipSubMenu(String title, String description, SideFragmentManager fragmentManager, int index, int id) {
        super(title, description,fragmentManager);
        mSideFragmentManager = fragmentManager;
        Log.d(TAG, "OpenVchipSubMenu mSideFragmentManager :"
                + mSideFragmentManager.toString()
                + "index :" + index + "id :" + id);
    }
}
