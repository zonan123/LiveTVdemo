package com.mediatek.wwtv.tvcenter.nav.util;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvTeletext;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopBlockBase;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopGroupBase;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopPageBase;

public class TeletextTopAdapter {
	
	private Object mTopAdapter;
	private MtkTvTeletext mTeletext;
	
	public TeletextTopAdapter(Object obj){
		mTopAdapter = obj;
		mTeletext = MtkTvTeletext.getInstance();
	}
	
	
	public Object getObject(){
		return mTopAdapter;
	}
	
	public String getName(){
		String result = "";
		if(mTopAdapter instanceof MtkTvTeletextTopBlockBase){
			MtkTvTeletextTopBlockBase tmpBlock = (MtkTvTeletextTopBlockBase)mTopAdapter;
			if(tmpBlock.isBlockHasName()){
				result = tmpBlock.getBlockName();
			}else{
				result = Integer.toHexString(tmpBlock.getBlockPageAddr().getPageNumber());
			}
			
			
		}else if(mTopAdapter instanceof MtkTvTeletextTopGroupBase){
			MtkTvTeletextTopGroupBase tmpGroup = (MtkTvTeletextTopGroupBase)mTopAdapter;
			if(tmpGroup.isGroupHasName()){
				result = tmpGroup.getGroupName();
			}else{
				result = Integer.toHexString(tmpGroup.getGroupPageAddr().getPageNumber());
			}
		}else if(mTopAdapter instanceof MtkTvTeletextTopPageBase){
			MtkTvTeletextTopPageBase tmpPage = (MtkTvTeletextTopPageBase)mTopAdapter;
			if(tmpPage.isNormalPageHasName()){
				result = tmpPage.getNormalPageName();
			}else{
				result = Integer.toHexString(tmpPage.getNormalPageAddr().getPageNumber());
			}
			
		}
		
		return result;
	}
	
	
	public List<TeletextTopAdapter> getNextList(){
		List<TeletextTopAdapter> resultList = new ArrayList<TeletextTopAdapter>();
		if(mTopAdapter instanceof MtkTvTeletextTopBlockBase){
			MtkTvTeletextTopBlockBase tmpBlock = (MtkTvTeletextTopBlockBase)mTopAdapter;
			List<MtkTvTeletextTopGroupBase> tmpGroupList = mTeletext.getTeletextTopGroupList(tmpBlock);
			
			if(tmpGroupList != null && !tmpGroupList.isEmpty()){
				for(MtkTvTeletextTopGroupBase group: tmpGroupList){
					
					resultList.add(new TeletextTopAdapter(group));
				}
			}
		}else if(mTopAdapter instanceof MtkTvTeletextTopGroupBase){
			MtkTvTeletextTopGroupBase tmpGroup = (MtkTvTeletextTopGroupBase)mTopAdapter;
			List<MtkTvTeletextTopPageBase> tmpPageList = mTeletext.getTeletextTopPageList(tmpGroup);
			
			if(tmpPageList != null && !tmpPageList.isEmpty()){
				for(MtkTvTeletextTopPageBase page: tmpPageList){
					
					resultList.add(new TeletextTopAdapter(page));
				}
			}
		}
		
		return resultList;
	}
	

}
