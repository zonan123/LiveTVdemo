package com.mediatek.wwtv.setting.widget.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mediatek.dm.MountPoint;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.util.Util;
//import com.mediatek.wwtv.tvcenter.dvr.manager.Core;


public class StateInitDiskItemAdapter<T> extends BaseAdapter {

	private List<T> mDiskList;
	private final LayoutInflater mInflater;

	public final static int TYPE_PVR = 1;
	public final static int TYPE_TIMESHIFT = 2;
	public final static int TYPE_DEFAULT = 0;

	private int typeFlag = TYPE_DEFAULT;

	private int select = 0;
	private Context context;
	public int getSelect() {
		return select;
	}

	public void setSelect(int select) {
		this.select = select;
	}

	public StateInitDiskItemAdapter(Context mContext, List<T> mBtnList) {
		super();
		this.mDiskList = mBtnList;
		context =mContext;
		 mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setGroup(List<T> mBtnList){
	    this.mDiskList = mBtnList;
	}
	/*
	 *
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDiskList.size();
	}

	/*
	 *
	 */
	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mDiskList.get(position);
	}

	/*
	 *
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 *
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		 View view;

		if(convertView==null){
			view = mInflater.inflate(R.layout.pvr_timeshfit_deviceitem_layout,null);
		}else
		{
			view=convertView;
		}

		setItemValue(view,(MountPoint)getItem(position),position);
		return view;
	}

	/**
	 * @param view
	 * @param item
	 */
	private void setItemValue(View view, MountPoint item ,int position) {
		// TODO Auto-generated method stub
	    android.util.Log.d("StateInitDiskItem","posion="+position);
        TextView label=(TextView)view.findViewById(R.id.disk_label);

        TextView size=(TextView)view.findViewById(R.id.disk_size);

        TextView isTshift=(TextView)view.findViewById(R.id.disk_is_tshift);

        TextView na=(TextView)view.findViewById(R.id.disk_na);
	    if (context.getResources().getString(R.string.dvr_device_no).equals(item.mVolumeLabel)) {
	    	label.setText(item.mVolumeLabel);
	        size.setVisibility(View.INVISIBLE);
	        isTshift.setVisibility(View.INVISIBLE);
	        na.setVisibility(View.INVISIBLE);
        }else {
        	label.setText(item.mVolumeLabel==null ? "No name":item.mVolumeLabel+" ");
            size.setVisibility(View.VISIBLE);
            isTshift.setVisibility(View.VISIBLE);
            na.setVisibility(View.VISIBLE);
            size.setText(Util.getGBSizeOfDisk(item));
            isTshift.setText(Util.getIsTshift(item,context));
            float speed = DiskSettingSubMenuDialog.getUsbSpeed(item);
            na.setText(speed == 0 ? "NA" : String.format("%3.1fMB/S ", speed));
            /*
             * if("".equals(Util.getIsTshift(item))){ isTshift.setText("PVR/TSHIFT");
             * na.setVisibility(View.VISIBLE); }else{ na.setVisibility(View.INVISIBLE); }
             */
        }
	}

	public int getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(int typeFlag) {
		this.typeFlag = typeFlag;
	}


}
