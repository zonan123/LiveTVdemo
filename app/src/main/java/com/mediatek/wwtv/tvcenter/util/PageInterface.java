package com.mediatek.wwtv.tvcenter.util;

import java.util.List;

public interface PageInterface {

	int getCount();

	int getPageNum();

	int getCurrentPage();

	void setPerPageNum(int prePage);

	void nextPage();

	void prePage();

	void headPage();

	void lastPage();

	void gotoPage(int n);

	int getPerPage();

	boolean hasNextPage();

	boolean hasPrePage();

	List<?> getCurrentList();

}
