package com.mediatek.wwtv.setting.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.wwtv.setting.base.scan.ui.RegionalisationAusActivity;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mediatek.wwtv.tvcenter.R;

public class SatDiveoListFragment extends Fragment{
    
    TextView f_child_title;
    ListView region_list;
    List<String> datas = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.region_list_layout, container,false);
        f_child_title = (TextView)view.findViewById(R.id.f_child_title);
        region_list = (ListView)view.findViewById(R.id.region_list);
        String satTitle = getResources().getString(R.string.select_region_title);
        f_child_title.setText(satTitle);
        datas = buildDatas();
        adapter = new ArrayAdapter<String>(getActivity(), 
                android.R.layout.simple_list_item_1, 
                android.R.id.text1, 
                datas);
        region_list.setAdapter(adapter);
        region_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RegionalisationAusActivity)getActivity()).satDiveoItemClick(1,position);
            }
        });
        return view;
    }
    
    public void refreshList() {
        datas = buildDatas();
        adapter.notifyDataSetChanged();
    }
    
    private List<String> buildDatas() {
        List<String> result = new ArrayList<String>();
        Map<String, List<TIFChannelInfo>> maps = ((RegionalisationAusActivity)getActivity()).getMaps();
        Iterator<Entry<String, List<TIFChannelInfo>>> iterator = maps.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, List<TIFChannelInfo>> next = iterator.next();
            result.add(next.getKey()+ "    ["+next.getValue().get(0).mMtkTvChannelInfo.getServiceName()+"]");
        }
        return result;
    }
    
    public interface OnSatDiveoItemClick{
        void satDiveoItemClick(int level,int position);
    }

}
