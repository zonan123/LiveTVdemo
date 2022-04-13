package com.mediatek.wwtv.tvcenter.util;

import java.util.List;


public class PageImp implements PageInterface {

    private static final String TAG = "PageImp";
    /*
     * data source
     */
    private List<?> list;

    private List<?> currentList;

    private int perPage = 6;

    private int currentPage = 1;

    private int pageNum = 1;
    /*
     * The total number of records
     */
    private int allNum;

    public PageImp() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "");
    }

    public PageImp(List<?> list, int perPage) {
        init(list, perPage);
    }

    /*
     * init
     */
    private void init(List<?> list, int perPage) {
        this.list = list;
        if (perPage >= 1) {
            this.perPage = perPage;
        }
        if (list != null && list.size() > this.perPage) {
            this.currentList = list.subList(0, this.perPage);
        }
        if (list != null && this.perPage != 0) {
            this.allNum = list.size();
            this.pageNum = (this.allNum + this.perPage - 1) / this.perPage;
            if (pageNum == 0) {
                pageNum = 1;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "init()" + "perPage:" + this.perPage + "allnum:" + allNum);
    }

    /*
     * total records
     */
    public int getCount() {
        return this.allNum;
    }

    public void setCount(int count){
        this.allNum = count;
    }

    public List<?> getList() {
        return list;
    }

    public List<?> getCurrentList() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentList()" + "currentPage:" + currentPage
                + "pageNum:" + pageNum);
        if(list==null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "list==null!");
            return null;
        }
        if (this.currentPage == this.pageNum) {
            this.currentList = this.list.subList((this.currentPage - 1)
                    * this.perPage, this.allNum);
        } else {
            this.currentList = this.list.subList((this.currentPage - 1)
                    * this.perPage, this.perPage * this.currentPage);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentList size" + currentList.size());
        return currentList;
    }


    public int getCurrentPage() {
        return this.currentPage;
    }

    /*
     * get total page num
     */
    public int getPageNum() {
        return this.pageNum;
    }

    /*
     * get page size
     */
    public int getPerPage() {
        return this.perPage;
    }

    /*
     * Jump to page n
     */
    public void gotoPage(int n) {
        if (n > this.pageNum) {
            n = this.pageNum;
            this.currentPage = n;
        } else {
            this.currentPage = n;
        }
    }

    /*
     * Is there Next
     */
    public boolean hasNextPage() {
        this.currentPage++;
        if (this.currentPage <= this.pageNum) {
            return true;
        } else {
            this.currentPage = this.pageNum;
        }
        return false;
    }

    /*
     * Have previous
     */
    public boolean hasPrePage() {
        this.currentPage--;
        if (this.currentPage > 0) {
            return true;
        } else {
            this.currentPage = 1;
        }
        return false;
    }

    /*
     * first page
     */
    public void headPage() {
        this.currentPage = 1;
    }

    /*
     * Last page
     */
    public void lastPage() {
        this.currentPage = this.pageNum;
    }

    /*
     * next page
     */
    public void nextPage() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nextPage");
        this.hasNextPage();
    }

    /*
     * Previous page
     */
    public void prePage() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "prePage");
        this.hasPrePage();
    }

    /*
     * set page size
     */
    public void setPerPageNum(int perPage) {
        this.perPage = perPage;
    }

}
