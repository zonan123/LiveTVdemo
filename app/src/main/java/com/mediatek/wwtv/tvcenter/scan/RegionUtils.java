package com.mediatek.wwtv.tvcenter.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	}

	public void dumpMaps(Map<Integer, APTargetRegion> regions) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("dumpMaps(...,...)");
		List<APTargetRegion> list1 = new ArrayList<APTargetRegion>();
		list1.addAll(regions.values());
		String regionInfoStr = "";
		for (APTargetRegion apTargetRegion : list1) {
			switch (apTargetRegion.level) {
			case 1:
				regionInfoStr = "Level1," + apTargetRegion.name;
				break;
			case 2:
				regionInfoStr = "---Level2," + apTargetRegion.name;
				break;
			case 3:
				regionInfoStr = "------Level3," + apTargetRegion.name;
				break;
			default:
			    break;
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(regionInfoStr);

			if (apTargetRegion.getChildren() != null
					&& apTargetRegion.getChildren().size() > 0) {
				dumpMaps(apTargetRegion.getChildren());
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
