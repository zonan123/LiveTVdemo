package com.android.tv.license;

import android.content.Context;

import androidx.paging.DataSource;

public class LicenseFactory extends DataSource.Factory<Integer, String> {

    private final Context mContext;

    public LicenseFactory(Context context) {
        this.mContext = context;
    }

    public LicensePositionDataSource mSource = null;

    @Override
    public DataSource<Integer, String> create() {
        LicensePositionDataSource source = new LicensePositionDataSource(mContext);
        mSource = source;
        return source;
    }


}
