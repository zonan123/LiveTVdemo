package com.mediatek.wwtv.tvcenter.epg.us;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.KeyEvent;
import android.widget.ListView;


import com.mediatek.wwtv.tvcenter.util.PageImp;

public class EPGUsListView extends ListView{
    
	private static final String TAG = "EPGUsListView";
	/*
	 * Update the data to the interface, will require the development of
	 * personnel implementation
	 */
	private UpDateListView mUpdate;
	/*
	 * the implements class of Paging algorithm
	 */
	private PageImp mPageImp;

	private boolean sourceFlag = false;

	/*
	 * Construction method
	 */
	public EPGUsListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/*
	 * Construction method
	 */
	public EPGUsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * Construction method
	 */
	public EPGUsListView(Context context) {
		super(context);
	}

	/*
	 * initial
	 */
	public void initData(List<?> list, int perPage, UpDateListView update) {
		mPageImp = new PageImp(list, perPage);
		mUpdate = update;
		// this.setOnItemSelectedListener(mItemSelectedListener);

	}
	
	public void initData(List<?> list, int perPage, UpDateListView update, boolean flag) {
		mPageImp = new PageImp(list, perPage);
		mUpdate = update;
		sourceFlag = flag;
		// this.setOnItemSelectedListener(mItemSelectedListener);

	}

	/*
	 * initial
	 */
	public void initData(List<?> list, int perPage) {
		mPageImp = new PageImp(list, perPage);
		// this.setOnItemSelectedListener(mItemSelectedListener);
	}

	/*
	 * Access to current display data set
	 */
	public List<?> getCurrentList() {
		return mPageImp.getCurrentList();
	}

	/*
	 * Get the next page display data set
	 */
	public List<?> getNextList() {
		mPageImp.nextPage();
		return getCurrentList();
	}
	
	public List<?> getListWithPage(int page){
		mPageImp.gotoPage(page);
		return getCurrentList();
	}

	/*
	 * Get back page shows data set
	 */
	public List<?> getPreList() {
		mPageImp.prePage();
		return getCurrentList();
	}
	
	public boolean hasPrePage(){
		return mPageImp.hasPrePage();
	}
	
	public boolean hasNextPage(){
		return mPageImp.hasNextPage();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

    int mSelectItem = this.getSelectedItemPosition();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown mSelectItem:" + mSelectItem);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if(sourceFlag){
				if (mPageImp.getCount() <= 0) {
					return false;
				}
				if(mSelectItem == mPageImp.getCount()-1){
					setSelection(0);
				}
			}else{
				/*
				 * When in the current page last line, and not the last page, and it
				 * changes the page
				 */
				if (mPageImp.getCount() <= 0 || getVisibility() != View.VISIBLE) {
					return false;
				}

				if (mSelectItem >= 0 && ((mSelectItem + 1) % mPageImp.getPerPage() == 0
						|| (mSelectItem + 1) % mPageImp.getCount() == 0)) {
					mPageImp.nextPage();
					if (mUpdate != null && (mPageImp.getCount() % mPageImp.getPerPage() == 0)) {
						mUpdate.update(true);
					}
					// setOnItemSelectedListener(mItemSelectedListener);
//					setSelection(0);
				}
				/*
				 * When in the current page last line, and is the last page, then
				 * back to the first item
				 */
//				if ((mSelectItem + 1) % mPageImp.getPerPage() == 0
//						&& mPageImp.getPageNum() == 1) {
//					setSelection(0);
//				}
			}
			
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if(sourceFlag){
				if(mSelectItem == 0){
					setSelection(mPageImp.getCount()-1);
				}
			}else{
				
				/*
				 * When in the current page first line, and not the home page, is
				 * back to back
				 */

				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onkey  up mSelectItem  =   " + mSelectItem
						+ " mPageImp.getPerPage()" + mPageImp.getPerPage());
				if (mPageImp.getCount() <= 0 || getVisibility() != View.VISIBLE) {
					return false;
				}

				if (mSelectItem == 0) {

					if (mUpdate != null) {
						mUpdate.update(false);
					}
					// setOnItemSelectedListener(mItemSelectedListener);
//					if (mPageImp.getPerPage() < mPageImp.getCount()) {
//						setSelection(mPageImp.getPerPage() - 1);
//					} else {
//						setSelection(mPageImp.getCount() - 1);
//					}

				}
				/*
				 * When in the current page first line, and the current page is the
				 * home page, is back to the last line
				 */
//				if (mSelectItem % mPageImp.getPerPage() == 0
//						&& mPageImp.getPageNum() == 1) {
//					setSelection(mPageImp.getPerPage() - 1);
//				}
			}
			
			break;
//		case KeyEvent.KEYCODE_DPAD_CENTER:
//		case KeyEvent.KEYCODE_ENTER:
//			return false;
		case KeyEvent.KEYCODE_PAGE_DOWN:
			return false;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	// OnItemSelectedListener mItemSelectedListener = new
	// OnItemSelectedListener() {
	//
	// public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// mSelectItem = arg2;
	// com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mItemSelectedListener mSelectItem:" + mSelectItem);
	// }
	//
	// public void onNothingSelected(AdapterView<?> arg0) {
	// }
	// };

	/*
	 * Data update to the interface
	 */
	public interface UpDateListView {
		void update(boolean isNextPage);
	}

	public PageImp getPageImp() {
		return mPageImp;
	}

}
