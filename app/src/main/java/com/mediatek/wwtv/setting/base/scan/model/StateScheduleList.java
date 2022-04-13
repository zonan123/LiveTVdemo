package com.mediatek.wwtv.setting.base.scan.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.setting.widget.view.ScheduleListItemInfoDialog;



import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;


public class StateScheduleList  {
    private static final String TAG = "StateScheduleList";

	private ScheduleListDialog mScheduleListWindow;
	private ScheduleListItemInfoDialog mItemDialogWindow;

	public enum StatusType {
		UNKNOWN,TIMESHIFT, PVR, NORMAL, DISKSETTING, INITDISK, FILELIST,SCHEDULELIST
	}

	private static StateScheduleList mStateSelf;
	public MyHandler mHandler;

	private final static int SHOW_LIST = 1;
	private final static int SHOW_LIST_ITEM = 2;
	public final static int Auto_Dismiss_Info_Dialog_Timer = 3;
	public final static int Auto_Dismiss_List_Dialog_Timer = 4;
	public final static int HIDDEN_CO_EXIST_COMP = 5;
    public static boolean mHasEditPvr = false;


	private MtkTvBookingBase mItem;
	private List<MtkTvBookingBase> books = null;

	public static class MyHandler extends Handler {
		WeakReference<Activity> mActivity;

		MyHandler(Activity activity) {
			mActivity = new WeakReference<Activity>(activity);
		}

		public void handleMessage(Message msg) {

			if (mStateSelf == null) {
				return;
			}

			switch (msg.what) {
			case SHOW_LIST:
				mStateSelf.showListDialog();
				break;

			case SHOW_LIST_ITEM:
				mStateSelf.showItemInfoDialog(mStateSelf.mItem);
				break;
			case Auto_Dismiss_Info_Dialog_Timer:
				mStateSelf.dimissItemInfoDialog();
				break;
			case Auto_Dismiss_List_Dialog_Timer:
//				mStateSelf.dimissListDialog();
				break;
			case HIDDEN_CO_EXIST_COMP:
				// mStateSelf.hiddenCoExistViews();
				break;

			default:
				break;
			}

			super.handleMessage(msg);
		}
	}

	private StateScheduleList(){
		books = MtkTvRecord.getInstance().getBookingList();
	}
	public synchronized static StateScheduleList getInstance() {
	    if (mStateSelf == null) {
            mStateSelf = new StateScheduleList();
        }
        return mStateSelf;
	}


	private void hiddenCoExistViews() {
		// TODO Auto-generated method stub
		// ComponentsManager.getInstance().hideAllComponents();
		//getManager().setVisibility(View.VISIBLE);
	}

/*	@Override
	public void initViews() {
		super.initViews();
	}*/

	public void initViews2() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.e("schedulelist", "initViews2:item:" + mItem);
		/*if(ComponentsManager.getActiveCompId()==NavBasicMisc.NAV_COMP_ID_BANNER){
    		((BannerView)ComponentsManager.getInstance().getComponentById(NavBasicMisc.NAV_COMP_ID_BANNER)).setVisibility(View.GONE);
    	}*/
		if (mItem == null) {
			showListDialog();
		} else {
			showItemInfoDialog(mItem);
		}

		hiddenCoExistViews();
//		mHandler.sendEmptyMessageDelayed(HIDDEN_CO_EXIST_COMP, 2000);
	}

	/**
	 *
	 */
	private void showListDialog() {
		/*mScheduleListWindow = new ScheduleListDialog(mContext);
		mScheduleListWindow.setCallback(this);
		mScheduleListWindow.setOnDismissListener(this);
		mScheduleListWindow.show();

		List<ScheduleItem> itemList = new ArrayList<ScheduleItem>();
		itemList = queryItem();

		mScheduleListWindow.setItemList(itemList);*/

	}

	/**
	 * @return
	 */
	public List<MtkTvBookingBase> queryItem() {
		if(books == null){
			books = MtkTvRecord.getInstance().getBookingList();
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "books=="+books);
		return books;
	}

	public void updateBooks(){
		books = MtkTvRecord.getInstance().getBookingList();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateBooks books=="+books);
	}

	/**
	 * @return
	 */
	public static void deleteAllItems(Context context) {
		List<MtkTvBookingBase> itemLists = queryItemList();
		if(itemLists !=null && !itemLists.isEmpty()){
			for(MtkTvBookingBase item : itemLists){
				MtkTvRecord.getInstance().deleteBooking(item.getBookingId());
			}
		}
	}

	/**
	 * @return
	 */
	public static List<MtkTvBookingBase> queryItem(Context context, int taskid) {
		return queryItemList();
	}

	public MtkTvBookingBase getMtkTvBookingBase() {
		List<MtkTvBookingBase> itemList = new ArrayList<MtkTvBookingBase>();
		itemList = queryItem();
		return itemList.get(0);
	}

	public int getChannelToStart() {

		return 0;
	}

	/**
	 * @param resolver
	 * @return
	 */
	private static List<MtkTvBookingBase> queryItemList() {
		List<MtkTvBookingBase> list = new ArrayList<MtkTvBookingBase>();
		list = MtkTvRecord.getInstance().getBookingList();
		return list;
	}




	/*

	@Override
	public void onRelease() {
		try {
			// TODO Auto-generated method stub
			if (mItemDialogWindow != null && mItemDialogWindow.isShowing()) {
				mItemDialogWindow.dismiss();
			}
			if (mScheduleListWindow != null && mScheduleListWindow.isShowing()) {
				mScheduleListWindow.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public void tryToRelease() {
		boolean listWindow = false;
		boolean infoWindow = false;

		if (mItemDialogWindow == null || !mItemDialogWindow.isShowing()) {
			infoWindow = true;
		}
		if (mScheduleListWindow == null || !mScheduleListWindow.isShowing()) {
			listWindow = true;
		}

		if (listWindow && infoWindow) {
			if (mItemDialogWindow != null) {
				mItemDialogWindow.setOnDismissListener(null);
			}
			if (mScheduleListWindow != null) {
				mScheduleListWindow.setOnDismissListener(null);
			}
			//mStateSelf.getManager().restoreToDefault(mStateSelf);
		}
	}

	/*


	/**
	 * @param t
	 */
	private void showItemInfoDialog(MtkTvBookingBase t) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("stateScheduleList","MtkTvBookingBase="+t);
		/*mItemDialogWindow = new ScheduleListItemInfoDialog(mContext, this, t);
		mItemDialogWindow.setCallback(this);
		mItemDialogWindow.setOnDismissListener(this);
		mItemDialogWindow.show();
*/
	}

	/*
	 *
	 */

	private void dimissListDialog() {
		if (mScheduleListWindow != null && mScheduleListWindow.isShowing()) {
			mScheduleListWindow.dismiss();
		}
	}

	private void dimissItemInfoDialog() {
		if (mItemDialogWindow != null && mItemDialogWindow.isShowing()) {
			mItemDialogWindow.dismiss();
		}
	}

	/**
	 *
	 */
	public boolean insertItem(MtkTvBookingBase item) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertItem + item = " + item.toString() + " books: " + books);
		int result = MtkTvRecord.getInstance().addBooking(item);
        if(result == 0){
            mHasEditPvr = true;
        }
		if(books == null){
			books = MtkTvRecord.getInstance().getBookingList();
		}
		if(books != null){
			books.add(item);
		}
		return true;
	}

	/**
	 *
	 */
	public void deleteItem(MtkTvBookingBase item) {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteItem item=="+item.toString() + " books: " + books);
	  int result = MtkTvRecord.getInstance().deleteBooking(item.getBookingId());
      if(result == 0){
          mHasEditPvr = true;
      }
      if(books == null){
      	  books = MtkTvRecord.getInstance().getBookingList();
      }
		if(books != null){
			books.remove(item);
		}
	}

	/**
	 * @param item
	 *            ,item's task id can't be 0.
	 */
	public void replaceItem(MtkTvBookingBase item) {
	  int result = MtkTvRecord.getInstance().replaceBooking(item.getBookingId(), item);
      if(result == 0){
        mHasEditPvr = true;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "replaceItem item=="+item.toString() + ",result " + result + "books: " + books);
      if(books == null){
		books = MtkTvRecord.getInstance().getBookingList();
	  }
      if(books != null){
		  int target = -1;
		  for(int i = 0; i < books.size(); i++){
			  if (books.get(i).getBookingId() == item.getBookingId()){
			  	target = i;
			  	break;
			  }
		  }
		  if(target != -1){
		  	books.remove(target);
		  	books.add(item);
		  }
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " target: "+  target);
	  }

	}




}
