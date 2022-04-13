package com.android.tv.license;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class LicenseViewModel extends ViewModel {
    private final LicenseFactory concertFactory;
    private LiveData<PagedList<String>> sourceList;

    public LicenseViewModel(Context context, int pageSize) {
        concertFactory = new LicenseFactory(context);
        sourceList = new LivePagedListBuilder<>(concertFactory, initConfig(pageSize))
                .setInitialLoadKey(0)
                .build();
    }

    public LiveData<PagedList<String>> getSourceList() {
        return sourceList;
    }

    private PagedList.Config initConfig(int pageSize){
        return new PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)//设置首次加载的数量
                .setPageSize(pageSize)//设置每页加载的数量
                .setPrefetchDistance(2)//设置距离每页最后数据项来时预加载下一页数据
                .setEnablePlaceholders(false)//设置是否启用UI占用符
                .build();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (concertFactory.mSource != null){
            concertFactory.mSource.close();
        }
    }
}
