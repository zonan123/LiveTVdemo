package com.mediatek.wwtv.setting.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.wwtv.setting.base.scan.ui.RegionalisationAusActivity;

import java.util.Collections;
import java.util.List;

import com.mediatek.wwtv.tvcenter.R;

public class SatDiveoRegionDetailFragment extends Fragment{
    
    private List<String> datas;
    private String title;
    TextView f_child_title;
    ListView region_list;
    ArrayAdapter<String> adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datas = (List<String>) getArguments().getSerializable("regions");
        title = getArguments().getString("title");
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.region_list_layout, container,false);
        f_child_title = (TextView)view.findViewById(R.id.f_child_title);
        region_list = (ListView)view.findViewById(R.id.region_list);
        view.findViewById(R.id.finish_tip).setVisibility(View.GONE);
        String satTitle = getResources().getString(R.string.select_region_detail_title,title);
        f_child_title.setText(satTitle);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), 
                android.R.layout.simple_list_item_1, 
                android.R.id.text1, 
                datas);
        region_list.setAdapter(adapter);
        region_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RegionalisationAusActivity)getActivity()).satDiveoItemClick(2,position);
            }
        });
        return view;
    }
    
    public void swapAdapterDatas(int position) {
        Collections.swap(datas, 0, position);
        adapter.notifyDataSetChanged();
    }

}
