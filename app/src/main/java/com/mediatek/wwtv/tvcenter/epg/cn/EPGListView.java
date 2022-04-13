package com.mediatek.wwtv.tvcenter.epg.cn;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.PageImp;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

public class EPGListView extends ListView {
	private static final String TAG = "EPGListView";
	private PageImp mPageImp = new PageImp();
	private int mCurrentSelectedPosition = 0;
	private int mLastRightSelectedPosition = 0;
//	private int pageSize;
	private int pageNum;
	private int mLastEnableItemPosition;
	private int mFirstEnableItemPosition;
	private UpDateListView mUpdate;

	private EPGLinearLayout mSelectedItemView;
	private EPGLinearLayout mNextSelectedItemView;
	private EPGListViewAdapter mListViewAdpter;
	private Handler mHandler;
	private EPGProgramInfo mLastSelectedTVProgram;
	public boolean mCanChangeChannel;
	public boolean mCanKeyUp;

	public EPGProgramInfo getLastSelectedTVProgram() {
		return mLastSelectedTVProgram;
	}

	private EPGChannelInfo mCurrentChannel;

	private DataReader mReader;
//	private EPGCnActivity mEPGAcivity;

//	private EventListener evListener = new EventListener() {
//
//		public void onChange(TVChannel ch) {
//
//			mSelectedItemView = getSelectedDynamicLinearLayout(getSelectedItemPosition());
//			if (mListViewAdpter != null && mSelectedItemView != null) {
//				int _position = mSelectedItemView.getmCurrentSelectPosition();
//				if (mCurrentChannel != null && _position != -1) {
//					List<EPGProgramInfo> childTVProgram = mCurrentChannel
//							.getmTVProgramInfoList();
//					if (childTVProgram != null && childTVProgram.size() > 0){
//						if(_position >= childTVProgram.size()){
//							_position = childTVProgram.size() - 1;
//							mSelectedItemView.setmCurrentSelectPosition(_position);
//						}
//						mLastSelectedTVProgram = childTVProgram.get(_position);
//					}
//				}
//				mListViewAdpter.setGroup(mListViewAdpter.getGroup());
//				mListViewAdpter.notifyDataSetChanged();
//				EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition();
//				EPGConfig.FROM_WHERE = EPGConfig.FROM_ANOTHER_STREAM;
//				mHandler.sendEmptyMessageDelayed(
//						EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
//			}
//		}
//	};;

	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public EPGListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		if (context instanceof EPGCnActivity) {
//			mEPGAcivity = (EPGCnActivity) context;
//		}
		mReader = DataReader.getInstance(context);
		mCanChangeChannel = true;
//		mReader.setChannelEventListener(evListener);
	}

	public EPGListView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		if (context instanceof EPGCnActivity) {
//			mEPGAcivity = (EPGCnActivity) context;
//		}
		mReader = DataReader.getInstance(context);
		mCanChangeChannel = true;
//		mReader.setChannelEventListener(evListener);
	}

	public EPGListView(Context context) {
		super(context);
//		if (context instanceof EPGCnActivity) {
//			mEPGAcivity = (EPGCnActivity) context;
//		}
		mReader = DataReader.getInstance(context);
		mCanChangeChannel = true;
//		mReader.setChannelEventListener(evListener);
	}

	public void setAdapter(ListAdapter adapter) {
		mFirstEnableItemPosition = 0;
		mLastEnableItemPosition = adapter.getCount() - 1;
		mListViewAdpter = (EPGListViewAdapter) adapter;
		mListViewAdpter.setEPGListView(this);
		super.setAdapter(adapter);
	}

	public void updateEnablePosition(ListAdapter adapter) {
		mFirstEnableItemPosition = 0;
		mLastEnableItemPosition = adapter.getCount() - 1;
	}

	public int getPageNum() {
		return pageNum;
	}

	/*
	 * init, loading data
	 */
	public void initData(List<?> list, int perPage, UpDateListView update,
			int pageIndex) {
		mPageImp = new PageImp(list, perPage);
//		this.pageSize = perPage;
		pageNum = mPageImp.getPageNum();
		mUpdate = update;
		if (pageIndex > 1) {
			mPageImp.gotoPage(pageIndex);
		}
	}

	/*
	 * init ,loading data
	 */
	public void initData(List<?> list, int perPage) {
		mPageImp = new PageImp(list, perPage);
	}

	public List<?> getList() {
		return mPageImp.getList();
	}

	/*
	 * Get the current display data set
	 */
	public List<?> getCurrentList() {
		return mPageImp.getCurrentList();
	}

	/*
	 * get the current page num
	 */
	public int getCurrentPageNum() {
		return mPageImp.getCurrentPage();
	}
	public EPGChannelInfo getCurrentChannel(){
		int currentPosition = getSelectedItemPosition();
		return (EPGChannelInfo) getItemAtPosition(currentPosition);
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		boolean handled = false;
		if (this == null || this.hasFocus() == false || !mCanChangeChannel) {
			return false;
		}

		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_CHUP:
			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_DPAD_DOWN));
			break;
		case KeyMap.KEYCODE_MTKIR_CHDN:
			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_DPAD_UP));
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mCanChangeChannel) {
//				mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
				mCanChangeChannel = false;
//				mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
				mCurrentSelectedPosition = getSelectedItemPosition();
				if (mCurrentSelectedPosition < 0) {
					mCurrentSelectedPosition = mLastRightSelectedPosition;
				}
				mLastRightSelectedPosition = mCurrentSelectedPosition;
				mSelectedItemView = getSelectedDynamicLinearLayout(mCurrentSelectedPosition);
				if (mSelectedItemView != null) {
					int boundHours = 0;
					if (mListViewAdpter.getDayNum() == 0) {
						boundHours = EPGUtil.getCurrentHour();
					}
					int index = mSelectedItemView.getmCurrentSelectPosition();
					com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "KeyEvent.KEYCODE_DPAD_LEFT---------index--->"
							+ index);
					if (mListViewAdpter.getDayNum() == 0
							&& mListViewAdpter.getStartHour() <= boundHours
							&& index <= 0) {
						mCanChangeChannel = true;
						return true;
					}
					mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
					handled = mSelectedItemView.onKeyLeft();

					if (handled == false) {
						handled = changeTimeZoom(keyCode);
						if (!handled) {
							mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
							mCanChangeChannel = true;
						}
					} else {
						EPGConfig.init = false;
						EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
						mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
						mCanChangeChannel = true;
					}
				} else {
					mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
					mCanChangeChannel = true;
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mCanChangeChannel) {
//				mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
				mCanChangeChannel = false;
//				mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
				mCurrentSelectedPosition = getSelectedItemPosition();
				if (mCurrentSelectedPosition < 0) {
					mCurrentSelectedPosition = mLastRightSelectedPosition;
				}
				mLastRightSelectedPosition = mCurrentSelectedPosition;
				mSelectedItemView = getSelectedDynamicLinearLayout(mCurrentSelectedPosition);

				if (mSelectedItemView != null) {
					long boundMills = 0;
					long lastMills = 0;
					if (mListViewAdpter.getDayNum() == EPGConfig.mMaxDayNum) {
						boundMills = EPGUtil.getEpgLastTimeMills(mListViewAdpter.getDayNum(), 0, true);
						lastMills = EPGUtil.getEpgLastTimeMills(mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour() + 2, false);
					}
					int index = mSelectedItemView.getmCurrentSelectPosition();
					com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "KeyEvent.KEYCODE_DPAD_RIGHT---------index--->" + index + "     getChildCount:>>" + mSelectedItemView.getChildCount()
							+ "    mListViewAdpter.getDayNum()>>" + mListViewAdpter.getDayNum() + "   " + (lastMills - boundMills));
					if (mListViewAdpter.getDayNum() == EPGConfig.mMaxDayNum
							&& lastMills >= boundMills
							&& (index == mSelectedItemView.getChildCount() - 1 || mSelectedItemView.getChildCount() == 0)) {
						mCanChangeChannel = true;
						return true;
					}
					mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
					handled = mSelectedItemView.onKeyRight();
					if (handled == false) {
						handled = changeTimeZoom(keyCode);
						if (!handled) {
							mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
							mCanChangeChannel = true;
						}
					}  else {
						EPGConfig.init = false;
						mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
						EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
						mCanChangeChannel = true;
					}
				} else {
					mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
					mCanChangeChannel = true;
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (mCanChangeChannel) {
				EPGConfig.init = false;
//				mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
				mCanChangeChannel = false;
//				mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
				mCanKeyUp = true;
				EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
				mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);

				mCurrentSelectedPosition = getSelectedItemPosition();
				if (mCurrentSelectedPosition < 0) {
					mCurrentSelectedPosition = mLastRightSelectedPosition;
				}
				mLastRightSelectedPosition = mCurrentSelectedPosition;
				mCurrentChannel = (EPGChannelInfo) getItemAtPosition(mCurrentSelectedPosition);

				mSelectedItemView = getSelectedDynamicLinearLayout(mCurrentSelectedPosition);
				if (mSelectedItemView != null) {
					int position = mSelectedItemView.getmCurrentSelectPosition();
					mSelectedItemView.clearSelected();
					// next EPGLinearLayout should choose child view
					if (mCurrentChannel != null && position != -1) {
						List<EPGProgramInfo> childTVProgram = mCurrentChannel.getmTVProgramInfoList();
						if (childTVProgram != null && !childTVProgram.isEmpty()){
						  mLastSelectedTVProgram = childTVProgram.get(position);
						}
					} else {
						mLastSelectedTVProgram = null;
					}
				}

				// if postion is last item of the page,when keycode is
				// KeyEvent.KEYCODE_DPAD_DOWN
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN mCurrentSelectedPosition>>" + mCurrentSelectedPosition + "   "
						+ mLastEnableItemPosition + "  " + mFirstEnableItemPosition + "  " + pageNum);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN>> mPageImp.getCurrentPage()" + mPageImp.getCurrentPage());
				if (mCurrentSelectedPosition == mLastEnableItemPosition) {
					/*
					 * if the current page is not last page
					 */
					if (mPageImp.getCurrentPage() != pageNum) {
						mPageImp.nextPage();
						mUpdate.updata(true);
						setSelection(mFirstEnableItemPosition);
						break;
					}

					/*
					 * if the count page num is one,the first position should be
					 * selected
					 */
					if (pageNum == 1) {
						EPGConfig.SELECTED_CHANNEL_POSITION = 0;
						setAdapter(mListViewAdpter);
						setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
						break;
					}
					/*
					 * if the current page is last page,the first position of first
					 * page should be selected
					 */
					if (mPageImp.getCurrentPage() == mPageImp.getPageNum()) {
						mPageImp.headPage();
						mUpdate.updata(true);
						setSelection(mFirstEnableItemPosition);
						break;
					}
				} else {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key dowm>getSelectedItemPosition>" + getSelectedItemPosition());
					EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition() + 1;
				}
			} else {
				return true;
			}
			break;

		case KeyEvent.KEYCODE_DPAD_UP:
			if (mCanChangeChannel) {
				EPGConfig.init = false;
//				mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
				mCanChangeChannel = false;
//				mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY, ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
				mCanKeyUp = true;
				EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
				mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);

				mCurrentSelectedPosition = getSelectedItemPosition();
				if (mCurrentSelectedPosition < 0) {
					mCurrentSelectedPosition = mLastRightSelectedPosition;
				}
				mLastRightSelectedPosition = mCurrentSelectedPosition;
				mCurrentChannel = (EPGChannelInfo) getItemAtPosition(mCurrentSelectedPosition);

				mSelectedItemView = getSelectedDynamicLinearLayout(mCurrentSelectedPosition);
				if (mSelectedItemView != null) {
					int position = mSelectedItemView.getmCurrentSelectPosition();
					mSelectedItemView.clearSelected();
					if (mCurrentChannel != null && position != -1) {
						List<EPGProgramInfo> childTVProgram = mCurrentChannel.getmTVProgramInfoList();
						if (childTVProgram != null && !childTVProgram.isEmpty()){
						  mLastSelectedTVProgram = childTVProgram.get(position);
						}
					} else {
						mLastSelectedTVProgram = null;
					}
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_UP mCurrentSelectedPosition>>" + mCurrentSelectedPosition + "   "
						+ mLastEnableItemPosition + "  " + mFirstEnableItemPosition + "  " + pageNum);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN>> mPageImp.getCurrentPage()" + mPageImp.getCurrentPage());
				if (mCurrentSelectedPosition == mFirstEnableItemPosition) {
					if (mPageImp.getCurrentPage() != 1) {
						mPageImp.prePage();
						mUpdate.updata(false);
						setSelection(mLastEnableItemPosition);
						break;
					}
					if (pageNum == 1) {
						EPGConfig.SELECTED_CHANNEL_POSITION = mLastEnableItemPosition;
						setAdapter(mListViewAdpter);
						setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
						break;
					}
					if (pageNum > 1 && mPageImp.getCurrentPage() == 1) {
						mPageImp.lastPage();
						mUpdate.updata(false);
						setSelection(mLastEnableItemPosition);
						break;
					}
				} else {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key up>getSelectedItemPosition>" + getSelectedItemPosition());
					EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition() - 1;
				}
			} else {
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			return false;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean changeTimeZoom(int keyCode) {
		int mStartTime = mListViewAdpter.getStartHour();
		int mDayNum = mListViewAdpter.getDayNum();

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_DPAD_LEFT;

			if (mStartTime >= EPGConfig.mTimeSpan) {
				mStartTime = mStartTime - EPGConfig.mTimeSpan;
				mListViewAdpter.setStartHour(mStartTime);
			} else {
				if (mDayNum > 0) {
					mDayNum = mDayNum - 1;
					mListViewAdpter.setDayNum(mDayNum);
					mStartTime = 24 - EPGConfig.mTimeSpan + mStartTime;
					mListViewAdpter.setStartHour(mStartTime);
				} else {
					return false;
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_DPAD_RIGHT;

			if (mStartTime < 24 - EPGConfig.mTimeSpan) {
				mStartTime = mStartTime + EPGConfig.mTimeSpan;
				mListViewAdpter.setStartHour(mStartTime);
			} else {
				if (mDayNum < EPGConfig.mMaxDayNum) {
					mDayNum = mDayNum + 1;
					mListViewAdpter.setDayNum(mDayNum);
					mStartTime = (mStartTime + EPGConfig.mTimeSpan) % 24;
					mListViewAdpter.setStartHour(mStartTime);
				} else {
					return false;
				}
			}
		}
		mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
		mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
		Message message = mHandler.obtainMessage();
		message.what = EPGConfig.EPG_SYNCHRONIZATION_MESSAGE;
		message.arg1 = mDayNum;
		message.arg2 = mStartTime;
		mHandler.sendMessage(message);
		mListViewAdpter.clearWindowList();
		mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
		mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
		mListViewAdpter.setActiveWindow();
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (this == null || this.hasFocus() == false || !mCanKeyUp) {
			return false;
		}
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_CHUP:
			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_DPAD_DOWN));
			break;
		case KeyMap.KEYCODE_MTKIR_CHDN:
			dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_DPAD_UP));
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (mCanKeyUp) {
				mCanKeyUp = false;
				EPGConfig.avoidFoucsChange = true;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSelectedItemPosition()>>>" + getSelectedItemPosition());
				EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition();
				if (EPGConfig.SELECTED_CHANNEL_POSITION < 0) {
					EPGConfig.SELECTED_CHANNEL_POSITION = 0;
				}
				mCurrentChannel = (EPGChannelInfo) getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
        if (CommonIntegration.getInstance().is3rdTVSource()) {
          mReader.selectChannelByTIF(mCurrentChannel.mId);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByTIF");
        }
        if (mCurrentChannel != null
              && mCurrentChannel.getTVChannel() != null
              && mCurrentChannel.getTVChannel().getChannelId() != mReader.getCurrentChId()) {
					mNextSelectedItemView = getSelectedDynamicLinearLayout(EPGConfig.SELECTED_CHANNEL_POSITION);
					int postion = mCurrentChannel.getNextPosition(mLastSelectedTVProgram);
					if (mNextSelectedItemView != null) {
						mNextSelectedItemView.setSelectedPosition(postion);
					}
					mReader.selectChannelByTIF(mCurrentChannel.mId);
					mHandler.sendEmptyMessage(EPGConfig.EPG_SELECT_CHANNEL_COMPLETE);
				} else {
					mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
					mCanChangeChannel = true;
				}
			} else {
				return true;
			}
			break;
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	/*
	 * update data interface
	 */
	public interface UpDateListView {
		void updata(boolean isNext);
	}

	public EPGLinearLayout getSelectedDynamicLinearLayout(int mPosition) {
		EPGLinearLayout mDynamicLinearLayout = null;
		LinearLayout mListViewChildView = (LinearLayout) getChildAt(mPosition);
		if (mListViewChildView != null) {
			mDynamicLinearLayout = (EPGLinearLayout) mListViewChildView
					.getChildAt(1);
		} else {
			mListViewChildView = (LinearLayout) getSelectedView();
			if (mListViewChildView != null){
			  mDynamicLinearLayout = (EPGLinearLayout) mListViewChildView
			      .getChildAt(1);
			}
		}
		return mDynamicLinearLayout;
	}

	public void rawChangedOfChannel(){
		mSelectedItemView = getSelectedDynamicLinearLayout(getSelectedItemPosition());
		if (mListViewAdpter != null && mSelectedItemView != null) {
			int position = mSelectedItemView.getmCurrentSelectPosition();
			if (mCurrentChannel != null && position != -1) {
				List<EPGProgramInfo> childTVProgram = mCurrentChannel
						.getmTVProgramInfoList();
				if (childTVProgram != null && !childTVProgram.isEmpty()){
					if(position >= childTVProgram.size()){
						position = childTVProgram.size() - 1;
						mSelectedItemView.setmCurrentSelectPosition(position);
					}
					mLastSelectedTVProgram = childTVProgram.get(position);
				}
			}
			mSelectedItemView.clearSelected();
			mListViewAdpter.setGroup(mListViewAdpter.getGroup());
			setAdapter(mListViewAdpter);
			setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
//			mListViewAdpter.notifyDataSetChanged();

			EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition();
			if(EPGConfig.avoidFoucsChange){
				EPGConfig.FROM_WHERE = EPGConfig.FROM_ANOTHER_STREAM;
			}else{
				EPGConfig.FROM_WHERE = EPGConfig.AVOID_PROGRAM_FOCUS_CHANGE;
			}
			mHandler.sendEmptyMessageDelayed(
					EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
		}
	}
}
