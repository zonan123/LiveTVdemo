package com.mediatek.wwtv.tvcenter.nav.util;

import java.util.ArrayList;
import java.util.List;
import com.mediatek.twoworlds.tv.MtkTvTeletext;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopBlockBase;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopGroupBase;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopPageBase;

public class TeletextTopItem {
	
	private Object mTopItem;
	private MtkTvTeletext mTeletext;
	
	public TeletextTopItem(Object obj){
		mTopItem = obj;
		mTeletext = MtkTvTeletext.getInstance();
	}
	
	
	public Object getObject(){
		return mTopItem;
	}
	
	public String getName(){
		String result = "";
		if(mTopItem instanceof MtkTvTeletextTopBlockBase){
			MtkTvTeletextTopBlockBase tmpBlock = (MtkTvTeletextTopBlockBase)mTopItem;
			if(tmpBlock.isBlockHasName()){
				result = tmpBlock.getBlockName();
			}else{
				result = Integer.toHexString(tmpBlock.getBlockPageAddr().getPageNumber());
			}
			
			
		}else if(mTopItem instanceof MtkTvTeletextTopGroupBase){
			MtkTvTeletextTopGroupBase tmpGroup = (MtkTvTeletextTopGroupBase)mTopItem;
			if(tmpGroup.isGroupHasName()){
				result = tmpGroup.getGroupName();
			}else{
				result = Integer.toHexString(tmpGroup.getGroupPageAddr().getPageNumber());
			}
		}else if(mTopItem instanceof MtkTvTeletextTopPageBase){
			MtkTvTeletextTopPageBase tmpPage = (MtkTvTeletextTopPageBase)mTopItem;
			/*if(tmpPage.isNormalPageHasName()){
				try{
					result = new String((tmpPage.getNormalPageName()).getBytes(),"UTF-8");
				}catch(Exception e){
					e.printStackTrace();
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.e("chengcl", "result0=="+result);
			}else{*/
				result = Integer.toHexString(tmpPage.getNormalPageAddr().getPageNumber());
			//}
			
		}
		
		return result;
	}
	
	
	public List<TeletextTopItem> getNextList(){
		List<TeletextTopItem> resultList = new ArrayList<TeletextTopItem>();
		if(mTopItem instanceof MtkTvTeletextTopBlockBase){
			MtkTvTeletextTopBlockBase tmpBlock = (MtkTvTeletextTopBlockBase)mTopItem;
			List<MtkTvTeletextTopGroupBase> tmpGroupList = mTeletext.getTeletextTopGroupList(tmpBlock);
			
			if(tmpGroupList != null && !tmpGroupList.isEmpty()){
				for(MtkTvTeletextTopGroupBase group: tmpGroupList){
					
					resultList.add(new TeletextTopItem(group));
				}
			}
		}else if(mTopItem instanceof MtkTvTeletextTopGroupBase){
			MtkTvTeletextTopGroupBase tmpGroup = (MtkTvTeletextTopGroupBase)mTopItem;
			List<MtkTvTeletextTopPageBase> tmpPageList = mTeletext.getTeletextTopPageList(tmpGroup);
			
			if(tmpPageList != null && !tmpPageList.isEmpty()){
				for(MtkTvTeletextTopPageBase page: tmpPageList){
					
					resultList.add(new TeletextTopItem(page));
				}
			}
		}
		
		return resultList;
	}
	

}
