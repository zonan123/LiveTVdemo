package com.mediatek.wwtv.setting.fragments;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.setting.base.scan.adapter.TkgsLocatorListAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.TkgsLocatorListAdapter.TkgsLocatorItem;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterView;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.TKGSSettingActivity;


public class TkgsLocatorListFrag extends Fragment{

  String TAG = "TkgsLocatorListFrag";
	private Action mAction;

	private Context mContext;
	private ScrollAdapterView mListView;
	MenuDataHelper mHelper;
  TkgsLocatorListAdapter mAdapter;
  boolean isHiddLocs = false;
	int remeberPos ;

	boolean isFargResumed ;
	Handler mHandler = new Handler();

	public void setAction(Action action) {
		mAction = action;
	}

	public void remeberPos(int pos){
		remeberPos = pos;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate........");
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup mRootView;
		mRootView = (ViewGroup) inflater.inflate(R.layout.menu_sat_list, null);
		mListView = (ScrollAdapterView)mRootView.findViewById(R.id.list);
		//mRootView.findViewById(R.id.antenna_type_tip).setVisibility(View.GONE);
		bindData();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreateView........");
		return mRootView;
	}

	private void bindData() {
		mHelper = MenuDataHelper.getInstance(getActivity());
    List<TkgsLocatorItem> list = new ArrayList<TkgsLocatorItem>();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "itemID:" + mAction.mItemID);
		if(isFargResumed){
			if(mAdapter !=null){
        int normalPos = mAdapter.getSelectItemNum();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setsotth normal:"+(normalPos));
				if(normalPos < 0){
					normalPos = 0;
				}
				remeberPos(normalPos);
			}
		}
    isHiddLocs = false;
    if (mAction.mItemID.equals(MenuConfigManager.TKGS_HIDD_LOCS)) {
      list = mHelper.getHiddenTKGSLocatorList();
      if (!list.isEmpty()) {
        isHiddLocs = true;
        String itemId = MenuConfigManager.TKGS_LOC_ITEM_HIDD_CLEANALL;
        TkgsLocatorItem delAllKey = new TkgsLocatorItem(true, itemId,
            mContext.getString(R.string.tkgs_clear_all_locators), Action.DataType.DIALOGPOP);
        if (!list.contains(delAllKey)){
			list.add(delAllKey);
		}
      } else {
        String itemId = mContext.getString(R.string.tkgs_hidden_lacator_empty);
        TkgsLocatorItem emptyKey = new TkgsLocatorItem(true, itemId,
            mContext.getString(R.string.tkgs_no_hidden_locator), Action.DataType.DIALOGPOP);
        emptyKey.setEnabled(false);
        if (!list.contains(emptyKey)){
			list.add(emptyKey);
		}
      }

    } else if (mAction.mItemID.equals(MenuConfigManager.TKGS_LOC_LIST)) {
      list = mHelper.convertToTKGSLocatorList();
      String itemId = MenuConfigManager.TKGS_LOC_ITEM_ADD;
      TkgsLocatorItem addKey = new TkgsLocatorItem(true, itemId,
          mContext.getString(R.string.tkgs_click_add_locator), Action.DataType.HAVESUBCHILD);
      if (!list.contains(addKey)){
		  list.add(addKey);
	  }
    }

    mAdapter = new TkgsLocatorListAdapter(mContext, list);
		mAdapter.setListener((TKGSSettingActivity)(getActivity()));
		mListView.setAdapter(mAdapter);
		mHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				int toPos = -1;
				if(isFargResumed){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mHandler isFargResumed");
          if (isHiddLocs) {
            // hidden location select last item for default
            toPos = mListView.getCount() - 1;
          } else {
            toPos = remeberPos;
          }
				}else{
          if (isHiddLocs) {
            // hidden location select last item for default
            toPos = mListView.getCount() - 1;
          } else {
            toPos = 0;
          }
				}
				if(mListView.getChildCount()>toPos){
					//boolean req = mListView.getChildAt(toPos).requestFocus();
				    mListView.setSelectionSmooth(toPos);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setsotth:"+toPos);
				}
			}
		}, 500);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(isFargResumed){
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
		}else{
			isFargResumed = true;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume........");
	}

}
