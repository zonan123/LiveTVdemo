package com.mediatek.wwtv.tvcenter.scan;

import java.util.ArrayList;

public class ActionList<T> extends ArrayList<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * fixed size, only for scanning progress.
	 */
	public int totalScanActionSize=1;	

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		
		totalScanActionSize=1;
	}
	
	
}
