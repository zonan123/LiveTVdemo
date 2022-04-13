package com.mediatek.wwtv.setting.base.scan.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.TargetRegion;


public class RegionUtils {

	private Map<Integer, APTargetRegion> children = new HashMap<Integer, APTargetRegion>();
	private IRegionChangeInterface onRegionChangeListener;

	public void addChild(TargetRegion region) {
		addChild(new APTargetRegion(region));
	}

	public void addChild(APTargetRegion region) {
		switch (region.level) {
		case 1:
			getChildren().put(region.primary, region);
			break;
		case 2:
			if (getChildren().get(region.primary) != null) {
				getChildren().get(region.primary).getChildren()
						.put(region.secondary, region);
			}

			break;
		case 3:
			if (getChildren().get(region.primary) != null) {
				if (getChildren().get(region.primary).getChildren()
						.get(region.secondary) != null) {
					getChildren().get(region.primary).getChildren()
							.get(region.secondary).getChildren()
							.put(region.tertiary, region);
				}
			}
			break;
		 default:
			break;
		}
	}

	public void dumpRM() {
		//dumpMaps(getChildren());
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("dumpRM");
	}

	public void dumpMaps(Map<Integer, APTargetRegion> regions) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("dumpMaps(...,...)");
		ArrayList<APTargetRegion> list1 = new ArrayList<APTargetRegion>();
		list1.addAll(regions.values());
		String regionInfoStr = "";
		for (APTargetRegion region : list1) {

			switch (region.level) {
			case 1:
				regionInfoStr = "Level1," + region.name;
				break;
			case 2:
				regionInfoStr = "---Level2," + region.name;
				break;
			case 3:
				regionInfoStr = "------Level3," + region.name;
				break;
			default:
				 break;
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(regionInfoStr);

			if (region.getChildren() != null
					&& region.getChildren().size() > 0) {
				dumpMaps(region.getChildren());
			}
		}
	}

	public Map<Integer, APTargetRegion> getChildren() {
		return children;
	}

	public IRegionChangeInterface getOnRegionChangeListener() {
		return onRegionChangeListener;
	}

	public void setOnRegionChangeListener(IRegionChangeInterface onRegionChangeListener) {
		this.onRegionChangeListener = onRegionChangeListener;
	}
}
