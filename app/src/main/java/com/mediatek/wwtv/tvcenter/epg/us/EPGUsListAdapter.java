package com.mediatek.wwtv.tvcenter.epg.us;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;



import java.util.ArrayList;
import java.util.List;

public class EPGUsListAdapter extends BaseAdapter{
    private Context mContext;
    private List<ListItemData> dataList = new ArrayList<ListItemData>();
    
    private int dayNum;
    
    public EPGUsListAdapter(Context context,List<ListItemData> dataList){
        mContext = context;
        this.dataList = dataList;
    }
    
    public List<ListItemData> getDataList() {
        return dataList;
    }

    public void setDataList(List<ListItemData> dataList) {
        this.dataList = dataList;
    }

    public int getDayNum() {
        return dayNum;
    }

    public void setDayNum(int dayNum) {
        this.dayNum = dayNum;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return dataList==null?0:dataList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return (dataList==null || (dataList.size()- 1) < position)?null:dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = new ViewHolder();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e("mListAdapter", "Epg getView:"+position);
        if (dataList!=null && !dataList.isEmpty()) {
            ListItemData itemData = dataList.get(position);
            if (holder.listItemView==null) {
                holder.listItemView = new ListItemView(mContext);
            }
            holder.listItemView.setAdapter(itemData);
            return holder.listItemView;
        }
        
        return convertView;
    }
    
    class ViewHolder{
        ListItemView listItemView;
//        TextView dayTextView;
//        TextView timeTextView;
//        TextView nameTextView;
//        ImageView CCimageView;
    }

}
