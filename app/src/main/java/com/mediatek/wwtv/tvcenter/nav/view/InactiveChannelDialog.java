package com.mediatek.wwtv.tvcenter.nav.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.CustListView;
import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter.DataItem;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.MtkTvChannelList;

public class InactiveChannelDialog extends Dialog{

	static String TAG = "InactiveChannelDialog";
	static final int NORMALPAGE_SIZE = 10;

	private Context mContext;
	public InactiveChannelDialog(Context context) {
		super(context, R.style.Theme_TurnkeyCommDialog);
		mContext = context ;
		mCommonInter = CommonIntegration.getInstanceWithContext(mContext);
	}

	List<MtkTvChannelInfoBase> mInactiveChannelList ;
	CustListView trdItemsListView;
	LayoutInflater trdLayoutInflater;
	SetConfigListViewAdapter tRDAdapter;
	CommonIntegration mCommonInter;

	public void showInactiveChannels(){
		initInactivechannelUI();
		show();
	}

	public  void setInactiveChnnaelList(List<MtkTvChannelInfoBase> inactiveChannelList) {
    	mInactiveChannelList = inactiveChannelList;
    }

	private CustListView.UpDateListView update = new CustListView.UpDateListView() {
		@Override
		public void updata() {
				tRDAdapter.setmGroup((List<DataItem>) trdItemsListView.getCurrentList());
			trdItemsListView.setAdapter(tRDAdapter);
		}

	};

	private void initInactivechannelUI() {
		this.setTitle(R.string.menu_tab_inactive_channels);
		trdLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = trdLayoutInflater.inflate(R.layout.menu_main_scan_trd_fav_network, null);
		TextView titleView = (TextView) view.findViewById(R.id.scan_fav_network_str);
		titleView.setVisibility(View.GONE);
		trdItemsListView = (CustListView) view.findViewById(R.id.scan_fav_network_list);
		//mInactiveChannelList = TIFChannelManager.getInstance(mContext).getAttentionMaskChannels(TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK, TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL, -1);
		mInactiveChannelList = mCommonInter.getChannelListByMaskFilter(-1, 
		        MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,9999 , TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK, TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL);
//		mInactiveChannelList = TIFChannelManager.getInstance(mContext).queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "inactiveChannelList>>>" + mInactiveChannelList.size());
        List<String[]> channelInfo = new ArrayList<String[]>();
        int  currentChannelID = mCommonInter.getCurrentChannelId();
        int currentIndex = 0;
        int size = mInactiveChannelList.size();
        MtkTvChannelInfoBase tempInfo = null;
        String[] tempStr;
        List<DataItem> dataItems = new ArrayList<DataItem>();
        for (int i = 0; i < size; i++) {
        	tempInfo = mInactiveChannelList.get(i);
        	if (tempInfo.getChannelId() == currentChannelID) {
        		currentIndex = i ;
            }
        	tempStr = new String[5];
        	tempStr[0] = tempInfo.getChannelNumber()+"";
        	if(tempInfo instanceof MtkTvAnalogChannelInfo){
        		tempStr[1] = "Analog";
        	}else if(tempInfo instanceof MtkTvDvbChannelInfo){
//        		MtkTvDvbChannelInfo dvb = (MtkTvDvbChannelInfo)tempInfo;
        		tempStr[1] = "Digital";
        	}

        	tempStr[2] = tempInfo.getServiceName();
        	tempStr[3] = String.valueOf(tempInfo.getChannelId());
        	float fre = ((float)tempInfo.getFrequency());
        	tempStr[4] = String.valueOf(fre);
        	channelInfo.add(tempStr);
        	DataItem channelListItem = new DataItem(MenuConfigManager.TV_CHANNEL_INACTIVE_LIST,
        			" ", MenuConfigManager.INVALID_VALUE,
        			MenuConfigManager.INVALID_VALUE,
        			MenuConfigManager.INVALID_VALUE, channelInfo.get(i),
        			MenuConfigManager.STEP_VALUE,
        			DataItem.DataType.CHANNELEUEDIT);
        	dataItems.add(channelListItem);
		}
        int gotoPage = currentIndex / (NORMALPAGE_SIZE) + 1;
        int gotoPosition = currentIndex % NORMALPAGE_SIZE;
		try {
			tRDAdapter = new SetConfigListViewAdapter(mContext);
			trdItemsListView.initData(dataItems, NORMALPAGE_SIZE, update);
			List<DataItem> mGroup = (List<DataItem>) trdItemsListView.getListWithPage(gotoPage);
			tRDAdapter.setmGroup(mGroup);
			trdItemsListView.setAdapter(tRDAdapter);
		} catch (Exception e) {
			e.printStackTrace();

		}
		tRDAdapter.setSelectPos(gotoPosition);
		trdItemsListView.setSelection(gotoPosition);

		trdItemsListView.setOnKeyListener(new View.OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
						cleanInactiveChannelConfirm(false, trdItemsListView.getSelectedItemPosition());
						return true;
					} else if (keyCode==KeyEvent.KEYCODE_BACK) {
						dismiss();
						return true;
					} else if(keyCode==KeyMap.KEYCODE_MTKIR_RED || keyCode==KeyEvent.KEYCODE_R) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KeyMap.KEYCODE_MTKIR_REDshow remove all confirm dialog");
//						dismiss();
						cleanInactiveChannelConfirm(true, -1);
						return true;
					}
				}
				return false;
			}
		});
		trdItemsListView.requestFocus();
		getWindow().setContentView(view);
	}


	/**
	 * for user to confirm clean inactive channel
	 */
	private void cleanInactiveChannelConfirm(final boolean isRemoveAll, final int removeOnePos) {
		String msg = null;
		if (isRemoveAll) {
			msg = mContext.getString(R.string.menu_tv_remove_all_inactive_channels);
		} else {
			msg = mContext.getString(R.string.menu_tv_remove_inactive_channel);
		}
		final TurnkeyCommDialog factroyCofirm = new TurnkeyCommDialog(mContext, 3);
		factroyCofirm.setMessage(msg);
		factroyCofirm.setButtonYesName(mContext.getString(R.string.menu_ok));
		factroyCofirm.setButtonNoName(mContext.getString(R.string.menu_cancel));
		factroyCofirm.show();
		factroyCofirm.setPositon(-20, 70);
		factroyCofirm.getButtonNo().requestFocus();

		factroyCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& action == KeyEvent.ACTION_DOWN) {
					factroyCofirm.dismiss();
					trdItemsListView.requestFocus();
					return true;
				}
				return false;
			}
		});
		View.OnKeyListener listener = new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						if (v.getId() == factroyCofirm.getButtonYes().getId()) {
							factroyCofirm.dismiss();
							if (isRemoveAll) {
								if (mInactiveChannelList != null) {
									boolean result = deleteAllInactiveChannels(mInactiveChannelList);
									if (result) {
										mInactiveChannelList.clear();
									}
								}
							} else {
								if (mInactiveChannelList != null && removeOnePos < mInactiveChannelList.size()) {
									boolean result = deleteInactiveChannel(mInactiveChannelList.get(removeOnePos).getChannelId());
									if (result) {
										mInactiveChannelList.remove(removeOnePos);
									}
								}
							}
							dismiss();
							if (mInactiveChannelList.isEmpty() ) {
								com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "size = 0");
							} else {
								trdItemsListView.requestFocus();
							}
						} else if (v.getId() == factroyCofirm.getButtonNo().getId()) {
							factroyCofirm.dismiss();
							trdItemsListView.requestFocus();
						}
						return true;
					}
				}
				return false;
			}
		};
		factroyCofirm.getButtonNo().setOnKeyListener(listener);
		factroyCofirm.getButtonYes().setOnKeyListener(listener);
	}


	 /**
     * for user to delete inactive channel(logo)
     * @param channelId
     */
    public boolean deleteInactiveChannel(int channelId) {
    	boolean deleteSucess = false;
    	MtkTvChannelInfoBase selChannel = TIFChannelManager.getInstance(mContext).getAPIChannelInfoById(channelId);
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteInactiveChannel selChannel>>>" + selChannel);
    	if (null != selChannel) {
    		List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
            list.add(selChannel);
            List<TIFChannelInfo> tifChannelInfoList = null;
            if (channelId == mCommonInter.getCurrentChannelId()) {
            	tifChannelInfoList = TIFChannelManager.getInstance(mContext).getTIFPreOrNextChannelList(channelId, false, false, 1, TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);
            }
            mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_DEL, list);
            if (tifChannelInfoList != null && tifChannelInfoList.size() > 1) {
            	TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(tifChannelInfoList.get(0));
            }
            deleteSucess = true;
    	}
    	return deleteSucess;
    }

    /**
     * for user to delete all inactive channels(logo)
     * @param channelId
     */
    public boolean deleteAllInactiveChannels(List<MtkTvChannelInfoBase> inactiveChannelList) {
    	boolean deleteSucess = false;
    	/*List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
    	for (TIFChannelInfo tempInfo:inactiveChannelList) {
    		list.add(TIFChannelManager.getInstance(mContext).getAPIChannelInfoById(tempInfo.mMtkTvChannelInfo.getChannelId()));
		}*/
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deleteAllInactiveChannels list.size()>>>" + inactiveChannelList.size());
    	if (!inactiveChannelList.isEmpty()) {
            mCommonInter.setChannelList(MtkTvChannelList.CHLST_OPERATOR_DEL, inactiveChannelList);
            deleteSucess = true;
    	}
    	return deleteSucess;
    }

    public boolean deleteAllInactiveChannels() {
        //List<TIFChannelInfo> attentionMaskChannels = TIFChannelManager.getInstance(mContext).getAttentionMaskChannels(TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK, TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL, -1);
        List<MtkTvChannelInfoBase> list = mCommonInter.getChannelListByMaskFilter(-1, 
                MtkTvChannelList.CHLST_ITERATE_DIR_NEXT,9999 , TIFFunctionUtil.CH_CONFIRM_REMOVE_MASK, TIFFunctionUtil.CH_CONFIRM_REMOVE_VAL);
        return deleteAllInactiveChannels(list);
    }
}
