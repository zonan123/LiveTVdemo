package com.mediatek.wwtv.setting;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.tv.license.LicenseRecyclerAdapter;
import com.android.tv.license.LicenseRecyclerView;
import com.android.tv.license.LicenseViewModel;
import com.mediatek.wwtv.tvcenter.R;

public class WebActivity extends FragmentActivity  {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_notice);
        LicenseRecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        LicenseRecyclerAdapter adapter = new LicenseRecyclerAdapter();

        LicenseViewModel viewModel = getViewModel();

        viewModel.getSourceList().observe(this, adapter::submitList);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setStartPostion(getBottom());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    private int getBottom(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int hight = getResources().getDimensionPixelSize(R.dimen.dvr_15_dp);
        return  hight == 0 ? 0 : dm.heightPixels / hight;
    }

    private LicenseViewModel getViewModel() {
        return new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new LicenseViewModel(WebActivity.this, 50);
            }
        }).get(LicenseViewModel.class);
    }

}
