package com.mediatek.wwtv.tvcenter.scan;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;

public class APTargetRegion {
    public static final String TAG="APTargetRegion";
	public int internalIdx;
	public int level;
	public int primary;
	public int secondary;
	public int tertiary;
	public String name;

    private Map<Integer, APTargetRegion> children = new HashMap<Integer, APTargetRegion>();

	public APTargetRegion(MtkTvScanDvbtBase.TargetRegion targetRegion) {
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

	/*public APTargetRegion() {
	}*/
	
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
        Log.d(TAG, "hashCode");
        return super.hashCode();
    }
}
