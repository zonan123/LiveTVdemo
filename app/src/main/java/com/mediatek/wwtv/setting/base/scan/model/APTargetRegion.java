package com.mediatek.wwtv.setting.base.scan.model;

import java.util.HashMap;
import java.util.Map;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.TargetRegion;

public class APTargetRegion {
	//private static final int MAX_REGION_NAME_LEN = 35;
	public int internalIdx;
	public int level;
	public int primary;
	public int secondary;
	public int tertiary;
	public String name;

	private Map<Integer, APTargetRegion> children = new HashMap<Integer, APTargetRegion>();

	public APTargetRegion(TargetRegion targetRegion) {
		this.internalIdx = targetRegion.internalIdx;
		this.level = targetRegion.level;
		this.primary = targetRegion.primary;
		this.secondary = targetRegion.secondary;
		this.tertiary = targetRegion.tertiary;
		this.name = targetRegion.name;
	}

	public APTargetRegion(int internalIdx, int level, int primary,
			int secondary, int tertiary, String name) {
		this.internalIdx = internalIdx;
		this.level = level;
		this.primary = primary;
		this.secondary = secondary;
		this.tertiary = tertiary;
		this.name = name;
	}
	
	public Map<Integer, APTargetRegion> getChildren() {
		return children;
	}

	public void setChildren(Map<Integer, APTargetRegion> children) {
		this.children = children;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof APTargetRegion) {
			if (((APTargetRegion) o).name == this.name) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	    android.util.Log.d("APTargetRegion", "hashCode()");
	    return super.hashCode();
	}
	
}
