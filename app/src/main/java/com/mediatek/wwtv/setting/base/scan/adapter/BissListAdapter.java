package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.List;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter.ViewHolder;
import com.mediatek.wwtv.setting.util.MenuConfigManager;

import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter.Listener;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapter;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterBase;
import com.mediatek.wwtv.tvcenter.R;

public class BissListAdapter extends BaseAdapter implements ScrollAdapter,
    View.OnKeyListener{

    private static final String TAG = "BissListAdapter";
    private  Context mContext;
    public List<BissItem> mList;
    String[] onOffStr;
    private Listener mListener;


    public BissListAdapter(Context mContext, List<BissItem> mList) {
        super();
        this.mContext = mContext;
        this.mList = mList;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mList.size=="+mList.size());
    }

    public void setListener(Listener ler){
        mListener = ler ;
    }

    @Override
    public void viewRemoved(View view) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "viewRemoved");
    }

    @Override
    public View getScrapView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return inflater.inflate(R.layout.menu_biss_list_item, parent, false);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        BissItem item = mList.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = getScrapView(parent);
            holder.numText = (TextView) convertView.findViewById(R.id.biss_num);
            holder.threePryText = (TextView) convertView.findViewById(R.id.biss_three_pry);
            holder.progIdText = (TextView) convertView.findViewById(R.id.biss_prog_id);
            holder.cwKeyText = (TextView) convertView.findViewById(R.id.biss_cwkey);
            holder.scanIcon = (ImageView) convertView.findViewById(R.id.biss_scan_icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.scanIcon.setVisibility(View.GONE);
        if(item.isAddKey){
            holder.numText.setVisibility(View.GONE);
            holder.progIdText.setVisibility(View.GONE);
            holder.cwKeyText.setVisibility(View.GONE);
            holder.threePryText.setText(item.getTitle());
        }else{
            holder.progIdText.setVisibility(View.VISIBLE);
            holder.cwKeyText.setVisibility(View.VISIBLE);
            
            holder.numText.setText(String.valueOf(item.bnum));
            holder.threePryText.setText(item.threePry);
            holder.progIdText.setText(String.valueOf(item.progId));
            holder.cwKeyText.setText(item.cwKey);
        }

        convertView.setTag(R.id.biss_cwkey, item);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("BissListAdapter", "threePry:"+item.threePry);
        convertView.setOnKeyListener(this);
        return convertView;
    }

    int selNum;

    public int getSelectItemNum(){
        return selNum;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKey------"+keyCode);
        if (v == null) {
            return false;
        }
        boolean handled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_ENTER:
                if(event.getAction() == 0){
                    final BissItem item = (BissItem) v.getTag(R.id.biss_cwkey);
                    selNum = item.bnum;
                    if(mListener !=null){
                        mListener.onActionClicked(item);
                    }
                    handled = true;
                }
                break;
            default:break;
        }
        return handled;
    }

    @Override
    public ScrollAdapterBase getExpandAdapter() {
        return null;
    }

    class ViewHolder{
        TextView numText;
        TextView threePryText;
        TextView progIdText;
        TextView cwKeyText;
        ImageView scanIcon;
    }

    public static class BissItem extends Action{
        //for num 1,2,3...
        public int bnum;
        //for progid
        public int progId;
        //for freq & ploazation & symbolrate
        public String threePry;
        //for cwKey
        public String cwKey;
        //add cw key is a special item
        public boolean isAddKey;

        public BissItem(int bnum, int progId, String threePry, String cwKey) {
            super(MenuConfigManager.BISS_KEY_ITEM,"BissKey item",Action.DataType.BISSITEMVIEW);
            this.bnum = bnum;
            this.progId = progId;
            this.threePry = threePry;
            this.cwKey = cwKey;
            isAddKey = false;
        }

        public BissItem(boolean isAddKey){
            super(MenuConfigManager.BISS_KEY_ITEM_ADD,"Click To Add Biss Key",Action.DataType.HAVESUBCHILD);
            progId = -1;
            this.isAddKey = isAddKey;
        }
    }

}
