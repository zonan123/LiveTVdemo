package com.mediatek.wwtv.setting.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.wwtv.setting.base.scan.adapter.RegionListAdapter;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
/**
 * @author hs_sishanxu
 */
public class RegionListFragment extends Fragment{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.util.Log.d("RegionListFragment","Exception()");
	}
	TextView f_child_title;
	ListView region_list;
	List<TIFChannelInfo> mList;
	RegionListAdapter listAdapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.region_list_layout, container,false);
		f_child_title = (TextView)view.findViewById(R.id.f_child_title);
		region_list = (ListView)view.findViewById(R.id.region_list);
		String regionTitle = getResources().getString(R.string.select_region_title);
		f_child_title.setText(regionTitle);
		mList = new ArrayList<TIFChannelInfo>();
		Bundle regionBundle = getArguments();
		if(regionBundle != null){
			mList = (ArrayList<TIFChannelInfo>)regionBundle.getSerializable("regions");
		}
		listAdapter = new RegionListAdapter(getActivity(), mList);
		region_list.setAdapter(listAdapter);
		region_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			    changChannelPostion(arg2);
			}
		});
		
		return view;
	}
	
	public void changChannelPostion(int position){
		if(position == 0){
			getActivity().finish();
			return;
		}
		Collections.swap(mList, 0, position);
		EditChannel.getInstance(getActivity()).channelSort(mList.get(position).mMtkTvChannelInfo.getChannelId(), mList.get(0).mMtkTvChannelInfo.getChannelId());
		listAdapter.notifyDataSetChanged();
		getActivity().finish();
	}

}
