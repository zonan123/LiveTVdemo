package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.base.scan.model.DVBSScanner;
import com.mediatek.wwtv.setting.SatActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogSdxActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIStateChangedCallBack;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;

import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter.Listener;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapter;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterBase;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterView.OnScrollListener;

public class SatListAdapter extends BaseAdapter implements ScrollAdapter,
	OnScrollListener, View.OnKeyListener{

	private static final String TAG = "SatListAdapter";
	private final Context mContext;
	public List<SatItem> mList ;
	String[] onOffStr;
	private Listener mListener;
	LayoutInflater mInflater;
	public SatListAdapter(Context mContext, List<SatItem> mList) {
		super();
		this.mContext = mContext;
		this.mList = mList;
		onOffStr=mContext.getResources().getStringArray(R.array.dvbs_sat_on_off);
		mInflater = LayoutInflater.from(mContext);

	}

	public void setListener(Listener ler){
		mListener = ler ;
	}

//	 private float getFloat(int resourceId) {
//	        TypedValue buffer = new TypedValue();
//	        mContext.getResources().getValue(resourceId, buffer, true);
//	        return buffer.getFloat();
//	}

	@Override
	public void viewRemoved(View view) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("viewRemoved");
	}

	@Override
	public View getScrapView(ViewGroup parent) {
        return mInflater.inflate(R.layout.menu_sat_list_item, parent, false);
	}

	@Override
	public int getItemViewType(int position) {
	    if(position == 0){
	        return 0;
	    }
	    return 1;
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
		ViewHolder1 holder1;
		SatItem item = mList.get(position);

		if(Action.DataType.TEXTCOMMVIEW.equals(item.getmDataType())){
		    if(convertView == null){
		        holder1 = new ViewHolder1();
		        convertView = mInflater.inflate(R.layout.menu_sat_list_item_next, parent, false);
		        holder1.title = (TextView) convertView.findViewById(R.id.satellite_next);
		        convertView.setTag(holder1);
		    }else {
	            holder1 = (ViewHolder1)convertView.getTag();
	        }
		    if(!item.isEnabled()){
                convertView.setAlpha(0.4f);
                convertView.setOnKeyListener(null);
            }else {
                convertView.setAlpha(1.0f);
                convertView.setOnKeyListener(this);
            }
		}else {
		    int disbaleIndex = 0;
		    if(Action.DataType.TEXTCOMMVIEW.equals(mList.get(0).getmDataType())) {
		    	if(TVContent.getInstance(mContext).isM7ScanMode()){
					disbaleIndex = 4;
				}else if(DVBSScanner.isDsmartScanMode()){
					disbaleIndex = 3;
				}
			}else {
				if(TVContent.getInstance(mContext).isM7ScanMode()){
					disbaleIndex = 3;
				}else if(DVBSScanner.isDsmartScanMode()){
					disbaleIndex = 2;
				}
			}
			if (convertView == null) {
		        holder = new ViewHolder();
		        convertView = mInflater.inflate(R.layout.menu_sat_list_item, parent, false);
		        holder.nameText = (TextView) convertView.findViewById(R.id.satellite_name);
		        //holder.abilityText = (TextView) convertView.findViewById(R.id.satellite_able);
		        holder.onOffText = (TextView) convertView.findViewById(R.id.satellite_state);
		        //holder.scanIcon = (ImageView) convertView.findViewById(R.id.satellite_scan_icon);
		        convertView.setTag(holder);
		    }else{
		        holder = (ViewHolder)convertView.getTag();
		    }
		    holder.nameText.setText(item.name);
		    //holder.abilityText.setText(item.abilityType);
		    convertView.setOnKeyListener(this);
		    if(item.isOn){
		        holder.onOffText.setText(onOffStr[0]);
		        //holder.scanIcon.setVisibility(View.INVISIBLE);//new UI no need
		        convertView.setAlpha(1.0f);
		        //convertView.setFocusable(true);
		    }else{
		        if(mNeedDisable && position > disbaleIndex){
		            convertView.setAlpha(0.4f);
		            //convertView.setFocusable(false);
		            convertView.setOnKeyListener(null);
		        }else {
		            convertView.setAlpha(1.0f);
		        }
		        holder.onOffText.setText(onOffStr[1]);
		        //holder.scanIcon.setVisibility(View.INVISIBLE);
		    }
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdapter", "num:"+item.satNum);
		}
		convertView.setTag(R.id.satellite_num, item);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdapter", "num:"+item.satNum);
		return convertView;
	}

	int selNum;
	public int getSelectItemNum(){
		return selNum;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdpter", "onKey------"+keyCode);
		if (v == null) {
            return false;
        }
        boolean handled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_ENTER:
            	if(event.getAction() == 0){
            		final SatItem item = (SatItem) v.getTag(R.id.satellite_num);
            		if(item.mDataType == DataType.TEXTCOMMVIEW){//next
            		    if(item.parentAction.mItemID.equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING)){
                            Intent intent = new Intent(mContext,ScanDialogActivity.class);
                            intent.putExtra("ActionID", item.parentAction.mItemID);
                            intent.putExtra("SatID", item.satid);
                            mContext.startActivity(intent);
                        }else if(item.parentAction.mItemID.equals(MenuConfigManager.DVBS_SAT_SCAN_DOWNLOAD)){
                            Intent intent = new Intent(mContext,ScanDialogSdxActivity.class);
                            intent.putExtra("ActionID", item.parentAction.mItemID);
                            if(mSdxFileName != null) {
                            	intent.putExtra("FileName", mSdxFileName);
							}
                            mContext.startActivity(intent);
                        }else if(item.parentAction.mItemID.equals(MenuConfigManager.DVBS_SAT_OP_CAM_SCAN)){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdpter", "startCamScan()");
                            ((SatActivity)mContext).finish();
                            CIStateChangedCallBack.getInstance(mContext).getCIHandle().startCamScan(true);
                            Intent intent = new Intent("finish_live_tv_settings");
                            mContext.sendBroadcast(intent, "com.mediatek.tv.permission.BROADCAST");
                            ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_CI_DIALOG);
                        }else{
                            if(mCanScan){
                                Intent intent = new Intent(mContext,ScanViewActivity.class);
                                intent.putExtra("ActionID", MenuConfigManager.DVBS_SAT_DEDATIL_INFO_SCAN);
                                intent.putExtra("SatID", item.satid);
                                //this locationID may be add/update-scan/re-scan
                                intent.putExtra("LocationID", item.parentAction.mItemID);
                                mContext.startActivity(intent);
                            }else {
                                if("".equals(mCanScanTip)){
                                    Toast.makeText(mContext, R.string.dvbs_same_port_lnb_toast, 0).show();
                                 }else {
                                    Toast.makeText(mContext, mCanScanTip, 0).show();
                                 }
                            }
                        }
            		}else {
            		    selNum = Integer.parseInt(item.satNum);
            		    if(mListener !=null){
            		        com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdpter", "mListener!=null,onActionClicked");
            		        mListener.onActionClicked(item);
            		    }
                    }
                    handled = true;
            	}
            	break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            	if(event.getAction() == 0){
            		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdpter", "press right key to enter scan view------");
	               	 final SatItem item2 = (SatItem) v.getTag(R.id.satellite_num);
	               	 if(!item2.isOn){
	               	  com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdpter", "item2.isOff");
	               	 }
	               	 handled = true;
            	}
            	break;
            	default:
            	break;
        }
		return handled;
	}

	private View mSelectedView = null;
	@Override
	public void onScrolled(View view, int position, float mainPosition,
			float secondPosition) {
		boolean hasFocus = mainPosition == 0.0;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SatAdapter", "scroll...."+hasFocus+",");
        if (hasFocus) {
            if (view != null) {
//                changeFocus(view, true /* hasFocus */, true /* shouldAniamte */);
                mSelectedView = view;
            }
        } else if (mSelectedView != null) {
//            changeFocus(mSelectedView, false /* hasFocus */, true /* shouldAniamte */);
            mSelectedView = null;
        }
	}


	@Override
	public ScrollAdapterBase getExpandAdapter() {
		return null;
	}

	class ViewHolder{
		TextView nameText;
		//TextView abilityText;
		TextView onOffText;
		//ImageView scanIcon;
	}

	class ViewHolder1{
        TextView title;
    }

	public static class SatItem extends Action{
		public int satid;
		public String satNum;
		public String name;
		public String abilityType;
		public boolean isOn;
		public Action parentAction ;

		public SatItem(int satid, String satNum, String name,
				String abilityType, boolean isOn,Action parentAction,String title) {
			super(parentAction.mItemID,title,Action.DataType.SATELITEDETAIL);
			this.satID = satid;
			this.satid = satid;
			this.satNum = satNum;
			this.name = name;
			this.abilityType = abilityType;
			this.isOn = isOn;
			this.parentAction = parentAction;
		}

		public SatItem(int satId, Action parentAction,String title) {
            super(parentAction.mItemID,title,Action.DataType.TEXTCOMMVIEW);
            this.parentAction = parentAction;
            this.satid = satId;
        }
	}

	boolean mNeedDisable = false;
	public void setNeedDisableWhenisOff(boolean status){
	    mNeedDisable = status;
	}
	boolean mCanScan = true;
	public void setCanScan(boolean canScan) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCanScan="+canScan);
	    mCanScan = canScan;
    }

	String mSdxFileName = null;
	public void setSdxFileName(String name) {
	    mSdxFileName = name;
    }

	String mCanScanTip = "";
    public void setCanScanTip(String tip) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCanScanTip="+tip);
        mCanScanTip = tip;
    }
}
