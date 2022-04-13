package com.android.tv.license;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

import com.mediatek.wwtv.tvcenter.R;



import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class LicensePositionDataSource extends PositionalDataSource<String> {
    private  final static String TAG = "LicensePositionDataSource";
    private LineNumberReader mLineNumberReader;

    public LicensePositionDataSource(Context context){
        mLineNumberReader = new LineNumberReader(new InputStreamReader(context.getResources().openRawResource(R.raw.third_party_licenses)));
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams loadInitialParams, @NonNull LoadInitialCallback<String> loadInitialCallback) {
        Log.v(TAG, "loadInitial " + loadInitialParams.requestedStartPosition + " " + loadInitialParams.pageSize + " " +
                loadInitialParams.requestedLoadSize);
        loadInitialCallback.onResult(loadLines(0,loadInitialParams.pageSize), 0);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams loadRangeParams, @NonNull LoadRangeCallback<String> loadRangeCallback) {
        Log.v(TAG, "loadRange");
        loadRangeCallback.onResult(loadLines(loadRangeParams.startPosition, loadRangeParams.loadSize));
    }

    private List<String> loadLines(int start, int count){
        ArrayList<String> lines =new ArrayList<>();
        if (start >= 0){
            mLineNumberReader.setLineNumber(start);
            for(int i = 0; i < count; ++i){
                try {
                    String aline = mLineNumberReader.readLine();
                    if(aline == null){
                        break;
                    }
                    lines.add(aline);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    break;
                }
            }
        }
        return lines;
    }

    public void close(){
        try {
            mLineNumberReader.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
