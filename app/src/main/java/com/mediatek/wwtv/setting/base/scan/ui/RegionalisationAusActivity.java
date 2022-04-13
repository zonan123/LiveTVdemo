package com.mediatek.wwtv.setting.base.scan.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.fragments.RegionListFragment;

import com.mediatek.wwtv.setting.fragments.SatAustrateFragment;
import com.mediatek.wwtv.setting.fragments.SatAustrateFragment.OnSatAustrateItemClick;
import com.mediatek.wwtv.setting.fragments.SatDiveoListFragment;
import com.mediatek.wwtv.setting.fragments.SatDiveoListFragment.OnSatDiveoItemClick;
import com.mediatek.wwtv.setting.fragments.SatDiveoRegionDetailFragment;
import com.mediatek.wwtv.setting.scan.EditChannel;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;

import com.mediatek.wwtv.tvcenter.util.KeyMap;

/**
 * show scan view's activity
 * for all scan which has scan condition
 *
 * @author hs_sishanxu
 *
 */
public class RegionalisationAusActivity extends BaseCustomActivity implements OnSatAustrateItemClick, OnSatDiveoItemClick{

	SatAustrateFragment satAustrateFragment;
	RegionListFragment regionListFragment;
	List<TIFChannelInfo> mList;
	Map<String, List<TIFChannelInfo>> maps;
	Map<String, TIFChannelInfo> defaultRegion = new LinkedHashMap<String, TIFChannelInfo>();
	int currentLevelIndex = 0;
	SatDiveoListFragment satDiveoListFragment;
	SatDiveoRegionDetailFragment satDiveoRegionDetailFragment;
	FragmentManager fragmentManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.sat_region_layout);
		if(ScanContent.isSatOpHDAustria()) {
		    mList = (ArrayList<TIFChannelInfo>)getIntent().getBundleExtra("regions").getSerializable("regions");
		} else if(ScanContent.isSatOpDiveo() || ScanContent.isSatOpFastScan()) {
		    maps = (Map<String, List<TIFChannelInfo>>) getIntent().getBundleExtra("regions").getSerializable("regions");
		    if(maps == null) {
		        finish();
		    } else {
		        initDefaultRegions(maps);
		    }
		}
		if(savedInstanceState == null){
		    fragmentManager = getFragmentManager();
		    FragmentTransaction tx = fragmentManager.beginTransaction();
		    if(ScanContent.isSatOpDiveo() || ScanContent.isSatOpFastScan()) {
		        satDiveoListFragment = new SatDiveoListFragment();
		        tx.add(R.id.container_layout,satDiveoListFragment,"SatAustrateFragment");
		    } else if(ScanContent.isSatOpHDAustria()) {
		        satAustrateFragment = new SatAustrateFragment();
		        tx.add(R.id.container_layout,satAustrateFragment,"SatAustrateFragment");
		    }
			tx.commit();
		}
	}

	private void initDefaultRegions(Map<String, List<TIFChannelInfo>> maps) {
	    Iterator<Entry<String, List<TIFChannelInfo>>> iterator = maps.entrySet().iterator();
	    defaultRegion.clear();
	    while (iterator.hasNext()) {
            Entry<String, List<TIFChannelInfo>> next = iterator.next();
            defaultRegion.put(next.getKey(), next.getValue().get(0));
        }
	}

	@Override
	public void satItemClick(int position) {
	    switch(position){
	        case 0:
	            finish();				
	            break;
	        case 1:
	            if(regionListFragment == null){
	                regionListFragment = new RegionListFragment();
	            }
	            FragmentTransaction ft = fragmentManager.beginTransaction();
	            ft.hide(satAustrateFragment);
	            Bundle bundle = new Bundle();
	            bundle.putSerializable("regions", (Serializable)mList);
	            regionListFragment.setArguments(bundle);
	            ft.add(R.id.container_layout,regionListFragment,"RegionListFragment");
	            ft.addToBackStack(null);
	            ft.commit();
	            break;
	        default:
	            break;
	    }
	}

	public Map<String, List<TIFChannelInfo>> getMaps() {
        return maps;
    }

	private List<String> filterServiceName(int position) {
	    List<String> result = new ArrayList<String>();
	    List<TIFChannelInfo> list = maps.get(maps.keySet().toArray()[position]);
	    for (TIFChannelInfo tifChannelInfo : list) {
            result.add(tifChannelInfo.mMtkTvChannelInfo.getServiceName());
        }
	    return result;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(ScanContent.isSatOpDiveo() || ScanContent.isSatOpFastScan()) {
	        if(keyCode == KeyMap.KEYCODE_DPAD_RIGHT) {
	            if(satDiveoListFragment.isResumed()) {
	                swapChannels(maps);
	                finish();
	            }
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}

	private void swapChannels(Map<String, List<TIFChannelInfo>> maps) {
	    Iterator<Entry<String, List<TIFChannelInfo>>> iterator = maps.entrySet().iterator();
	    while (iterator.hasNext()) {
            Entry<String, List<TIFChannelInfo>> next = iterator.next();
            int channelId = next.getValue().get(0).mMtkTvChannelInfo.getChannelId();
            int defaultChannelId = defaultRegion.get(next.getKey()).mMtkTvChannelInfo.getChannelId();
            if(channelId != defaultChannelId) {
                EditChannel.getInstance(this).channelSort(channelId,defaultChannelId);
            }
        }
	}

    @Override
    public void satDiveoItemClick(int level,int position) {
        if(level == 1) {
            currentLevelIndex = position;
            satDiveoRegionDetailFragment = new SatDiveoRegionDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("regions", (Serializable) filterServiceName(position));
            bundle.putString("title", (String)maps.keySet().toArray()[position]);
            satDiveoRegionDetailFragment.setArguments(bundle);
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.addToBackStack(null);
            ft.replace(R.id.container_layout,satDiveoRegionDetailFragment,"SatDiveoRegionDetailFragment");
            ft.commit();
        } else if(level == 2) {
            fragmentManager.popBackStack();
            if(position != 0) {
                List<TIFChannelInfo> list = maps.get(maps.keySet().toArray()[currentLevelIndex]);
                Collections.swap(list, 0, position);
                satDiveoListFragment.refreshList();
            }
        }
    }
}
