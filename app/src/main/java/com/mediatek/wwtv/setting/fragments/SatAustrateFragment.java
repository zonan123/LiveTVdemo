package com.mediatek.wwtv.setting.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.wwtv.setting.base.scan.adapter.SatAustrateListAdapter;
import com.mediatek.wwtv.tvcenter.R;
/**
 * 
 * @author hs_sishanxu
 *
 */
public class SatAustrateFragment extends Fragment{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    android.util.Log.d("RegionListFragment","onCreate()");
		super.onCreate(savedInstanceState);
	}
	TextView f_child_title;
	ListView region_list;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.region_list_layout, container,false);
		f_child_title = (TextView)view.findViewById(R.id.f_child_title);
		region_list = (ListView)view.findViewById(R.id.region_list);
		String satTitle = getResources().getString(R.string.select_channel_title);
		f_child_title.setText(satTitle);
		String[] satAustrateArray = getResources().getStringArray(R.array.sat_austrate_array);
		SatAustrateListAdapter satAustrateAdapter = new SatAustrateListAdapter(getActivity(),satAustrateArray);
		region_list.setAdapter(satAustrateAdapter);
		region_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				((OnSatAustrateItemClick)getActivity()).satItemClick(arg2);
			}
		});
		return view;
	}
	
	
	public interface OnSatAustrateItemClick{
		void satItemClick(int position);
	}

}
