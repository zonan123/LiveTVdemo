package com.mediatek.wwtv.setting.util;

import java.util.List;




/**
 * used to do page divided
 * @author sin_biaoqinggao
 *
 */
public class Pager {
	
	//current page
	public int currentPage = 1;
    //total page number
	public int pageTotal = 1;
	//how many item show per one page
	public int ITEM_PER_PAGE = 10;
    //which position the list-view should selected
    public int selectPosition ;
    int allItemNum ;
    /*
	 * data source
	 */
	private List<?> list;
	/*
	 * show current data list
	 */
	private List<?> currentList;
	
	public Pager(List<?> list,int gotoPage){
		this.list = list ;
		if(list.size()%ITEM_PER_PAGE == 0){
			pageTotal = list.size()/ITEM_PER_PAGE;
		}else{
			pageTotal = list.size()/ITEM_PER_PAGE +1 ;
		}
		allItemNum = list.size();
		//default currentPage is 1
		if(currentPage < gotoPage){
			currentPage = gotoPage;
		}
	}
	
	public void setPagerList(List<?> list){
		this.list = list ;
	}
	
	public int getAllItemNum(){
        return allItemNum;
    }
	
	public List<?> getRealDataList(){
		if(currentPage == pageTotal){
			currentList = list.subList((currentPage-1)*ITEM_PER_PAGE, allItemNum);
		}else{
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("Pager", "currentPage=="+currentPage+",size =="+list.size());
			if(list.isEmpty()){
				return list;
			}
			currentList = list.subList((currentPage-1)*ITEM_PER_PAGE, currentPage*ITEM_PER_PAGE);
		}
		
		return currentList;
	}
}
