package com.mediatek.wwtv.setting.base.scan.model;

import java.util.List;

import com.mediatek.wwtv.setting.base.scan.adapter.ThirdItemAdapter.ThirdItem;

public interface IRegionChangeInterface {

	void onRegionChange(List<ThirdItem> items);
}
