package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
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

	private final DataReader mReader;
	private EPGEuActivity mEPGAcivity;
	private boolean isFirst = true;

	// private EventListener evListener = new EventListener() {
	//
	// public void onChange(TVChannel ch) {
	//
	// mSelectedItemView =
	// getSelectedDynamicLinearLayout(getSelectedItemPosition());
	// if (mListViewAdpter != null && mSelectedItemView != null) {
	// int _position = mSelectedItemView.getmCurrentSelectPosition();
	// if (mCurrentChannel != null && _position != -1) {
	// List<EPGProgramInfo> childTVProgram = mCurrentChannel
	// .getmTVProgramInfoList();
	// if (childTVProgram != null && childTVProgram.size() > 0){
	// if(_position >= childTVProgram.size()){
	// _position = childTVProgram.size() - 1;
	// mSelectedItemView.setmCurrentSelectPosition(_position);
	// }
	// mLastSelectedTVProgram = childTVProgram.get(_position);
	// }
	// }
	// mListViewAdpter.setGroup(mListViewAdpter.getGroup());
	// mListViewAdpter.notifyDataSetChanged();
	// EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition();
	// EPGConfig.FROM_WHERE = EPGConfig.FROM_ANOTHER_STREAM;
	// mHandler.sendEmptyMessageDelayed(
	// EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
	// }
	// }
	// };;

	@Override
	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public EPGListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (context instanceof EPGEuActivity) {
			mEPGAcivity = (EPGEuActivity) context;
		}
		mReader = DataReader.getInstance(context);
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"mReader>>>>" + mReader);
		mCanChangeChannel = true;
		// mReader.setChannelEventListener(evListener);
	}

	public EPGListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context instanceof EPGEuActivity) {
			mEPGAcivity = (EPGEuActivity) context;
		}
		mReader = DataReader.getInstance(context);
		mCanChangeChannel = true;
		// mReader.setChannelEventListener(evListener);
	}

	public EPGListView(Context context) {
		super(context);
		if (context instanceof EPGEuActivity) {
			mEPGAcivity = (EPGEuActivity) context;
		}
		mReader = DataReader.getInstance(context);
		mCanChangeChannel = true;
		// mReader.setChannelEventListener(evListener);
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"mEPGAcivity>>>>" + mEPGAcivity);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		mFirstEnableItemPosition = 0;
		mLastEnableItemPosition = adapter.getCount() - 1;
		mListViewAdpter = (EPGListViewAdapter) adapter;
		mListViewAdpter.setEPGListView(this);
		if (isFirst) {
			isFirst = false;
			int heightPixels = getContext().getResources().getDisplayMetrics().heightPixels;
			mListViewAdpter
					.setHeight((int) ((heightPixels * 0.52f) / 6 - 1.5f));
		}
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

	public EPGChannelInfo getCurrentChannel() {
		int currentPosition = getSelectedItemPosition();
		return (EPGChannelInfo) getItemAtPosition(currentPosition);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		boolean handled = false;
//		if (this == null || this.hasFocus() == false || !mCanChangeChannel) {
//			return false;
//		}
		if (getCurrentList() == null || getCurrentList().isEmpty()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentList()==null or getCurrentList is empty!");
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
		case KeyEvent.KEYCODE_DPAD_LEFT:{
			boolean isRtl = CommonIntegration.getInstance().isRtl();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isRtl=" + isRtl);
			if (isRtl) {
				if (onRightKey()) {
					return true;
				}
			} else {
				if (onLeftKey()) {
					return true;
				}
			}
		}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:{
			boolean isRtl = CommonIntegration.getInstance().isRtl();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isRtl=" + isRtl);
			if (isRtl) {
				if (onLeftKey()) {
					return true;
				}
			} else {
				if (onRightKey()) {
					return true;
				}
			}
		}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
      if(ScanContent.isZiggo() && !CommonIntegration.getInstance().isALLOWChangeForZiggo(true)){
        mHandler.sendEmptyMessage(EPGConfig.EPG_NEED_FINISH_EPG);
        return true;
      }
//			if (mCanChangeChannel) {
				EPGConfig.init = false;
				// mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
				mCanChangeChannel = false;
				// mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
				// ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
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
					int position = mSelectedItemView
							.getmCurrentSelectPosition();
					mSelectedItemView.clearSelected();
					// next EPGLinearLayout should choose child view
					if (mCurrentChannel != null && position != -1) {
						List<EPGProgramInfo> childTVProgram = mCurrentChannel
								.getmTVProgramInfoList();
						if (childTVProgram != null && !childTVProgram.isEmpty() &&position<childTVProgram.size()){
						  mLastSelectedTVProgram = childTVProgram.get(position);
						}
					} else {
						mLastSelectedTVProgram = null;
					}
//				}

				// if postion is last item of the page,when keycode is
				// KeyEvent.KEYCODE_DPAD_DOWN
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN mCurrentSelectedPosition>>"
						+ mCurrentSelectedPosition + "   "
						+ mLastEnableItemPosition + "  "
						+ mFirstEnableItemPosition + "  " + pageNum);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN>> mPageImp.getCurrentPage()"
						+ mPageImp.getCurrentPage());
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
					 * if the current page is last page,the first position of
					 * first page should be selected
					 */
					if (mPageImp.getCurrentPage() == mPageImp.getPageNum()) {
						mPageImp.headPage();
						mUpdate.updata(true);
						setSelection(mFirstEnableItemPosition);
						break;
					}
				} else {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key dowm>getSelectedItemPosition>"
							+ getSelectedItemPosition());
					EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition() + 1;
				}
			} else {
				return true;
			}
			break;

		case KeyEvent.KEYCODE_DPAD_UP:
      if(ScanContent.isZiggo() && !CommonIntegration.getInstance().isALLOWChangeForZiggo(true)){
        mHandler.sendEmptyMessage(EPGConfig.EPG_NEED_FINISH_EPG);
        return true;
      }
//			if (mCanChangeChannel) {
				EPGConfig.init = false;
				// mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
				mCanChangeChannel = false;
				// mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
				// ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
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
					int  position = mSelectedItemView
							.getmCurrentSelectPosition();
					mSelectedItemView.clearSelected();
					if (mCurrentChannel != null &&  position != -1) {
						List<EPGProgramInfo> childTVProgram = mCurrentChannel
								.getmTVProgramInfoList();
						if (childTVProgram != null && !childTVProgram.isEmpty() && position < childTVProgram.size()){
						  mLastSelectedTVProgram = childTVProgram.get( position);
						}
					} else {
						mLastSelectedTVProgram = null;
					}
//				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_UP mCurrentSelectedPosition>>"
						+ mCurrentSelectedPosition + "   "
						+ mLastEnableItemPosition + "  "
						+ mFirstEnableItemPosition + "  " + pageNum);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_DOWN>> mPageImp.getCurrentPage()"
						+ mPageImp.getCurrentPage());
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
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key up>getSelectedItemPosition>"
							+ getSelectedItemPosition());
					EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition() - 1;
				}
			} else {
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			return false;
		case KeyEvent.KEYCODE_PAGE_DOWN:
			return false;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean onRightKey() {
		boolean handled = false;
//		if (mCanChangeChannel) {
			// mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
			mCanChangeChannel = false;
			// mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
			// ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
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
					boundMills = EPGUtil.getEpgLastTimeMills(
							EPGConfig.mMaxDayNum, 0, true);
					lastMills = EPGUtil.getEpgLastTimeMills(
							mListViewAdpter.getDayNum(),
							mListViewAdpter.getStartHour() + 2, false);
				}
				int index = mSelectedItemView.getmCurrentSelectPosition();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(
						TAG,
						"KeyEvent.KEYCODE_DPAD_RIGHT---------index--->" + index
								+ "     getChildCount:>>"
								+ mSelectedItemView.getChildCount()
								+ "    mListViewAdpter.getDayNum()>>"
								+ mListViewAdpter.getDayNum() + "   "
								+ (lastMills - boundMills));
				if (mListViewAdpter.getDayNum() == EPGConfig.mMaxDayNum
						&& lastMills >= boundMills
						&& (index == mSelectedItemView.getChildCount() - 1 || mSelectedItemView
								.getChildCount() == 0)) {
					mCanChangeChannel = true;
					return true;
				}
				mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
				handled = mSelectedItemView.onKeyRight();
				if (handled == false) {
					handled = changeTimeZoom(KeyEvent.KEYCODE_DPAD_RIGHT);
					if (!handled) {
						mHandler.sendEmptyMessageDelayed(
								EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
						mCanChangeChannel = true;
					}
				} else {
					EPGConfig.init = false;
					mHandler.sendEmptyMessageDelayed(
							EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
					EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
					mCanChangeChannel = true;
				}
			} else {
				mHandler.sendEmptyMessageDelayed(
						EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
				mCanChangeChannel = true;
			}
//		}
		return false;
	}

	private boolean onLeftKey() {
		boolean handled = false;
//		if (mCanChangeChannel) {
			// mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
			mCanChangeChannel = false;
			// mHandler.sendEmptyMessageDelayed(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
			// ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
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
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KeyEvent.KEYCODE_DPAD_LEFT---------index--->"
						+ index + " , getStartHour>> " + mListViewAdpter.getStartHour() + ",boundHours>> "+ boundHours);
				if (mListViewAdpter.getDayNum() == 0
						&& mListViewAdpter.getStartHour() <= boundHours
						&& index <= 0) {
					mCanChangeChannel = true;
					return true;
				}
				mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
				handled = mSelectedItemView.onKeyLeft();

				if (handled == false) {
					handled = changeTimeZoom(KeyEvent.KEYCODE_DPAD_LEFT);
					if (!handled) {
						mHandler.sendEmptyMessageDelayed(
								EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
						mCanChangeChannel = true;
					}
				} else {
					EPGConfig.init = false;
					EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
					mHandler.sendEmptyMessageDelayed(
							EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
					mCanChangeChannel = true;
				}
			} else {
				mHandler.sendEmptyMessageDelayed(
						EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
				mCanChangeChannel = true;
			}
//		}
		return false;
	}

	private boolean changeTimeZoom(int keyCode) {
		int mStartTime = mListViewAdpter.getStartHour();
		int mDayNum = mListViewAdpter.getDayNum();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeTimeZoom "+ mStartTime + " " + mDayNum);

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_DPAD_LEFT;
			int startBound = EPGUtil.getCurrentHour();
			if(startBound == 0){
				startBound = 24;
			}
			if (mDayNum > 0) {
				if(mStartTime >= EPGConfig.mTimeSpan){
					int mBeginTimeBefore = EPGUtil.getEUIntervalHour(mDayNum, mStartTime, 0) % 24;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei changeTimeZoom mBeginTimeBefore>> "+mBeginTimeBefore);
					mStartTime = mStartTime - EPGConfig.mTimeSpan;
					int mBeginTimeAfter = EPGUtil.getEUIntervalHour(mDayNum, mStartTime, 0) % 24;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei changeTimeZoom mBeginTimeAfter>> "+mBeginTimeAfter);
					if(mBeginTimeAfter > mBeginTimeBefore){
						//local day num need -1
						mHandler.sendEmptyMessage(EPGConfig.EPG_NEED_UPDATE_LOCAL_DAYNUM);
					}
				} else { // this case, need day num -1
					mDayNum = mDayNum - 1;
					mListViewAdpter.setDayNum(mDayNum);
					mStartTime = 24 - EPGConfig.mTimeSpan + mStartTime;
					if(mDayNum == 0 && mStartTime == startBound - 1){// for cover hour 1 3 5 7...
						mStartTime = mStartTime + 1;
					}
				}
				mListViewAdpter.setStartHour(mStartTime%24);
			} else if(mDayNum == 0){
				if(mStartTime >= EPGConfig.mTimeSpan){
					int mBeginTimeBefore = EPGUtil.getEUIntervalHour(mDayNum, mStartTime, 0) % 24;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei changeTimeZoom mBeginTimeBefore>> "+mBeginTimeBefore);
					mStartTime = mStartTime - EPGConfig.mTimeSpan;
					int mBeginTimeAfter = EPGUtil.getEUIntervalHour(mDayNum, mStartTime, 0) % 24;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei changeTimeZoom mBeginTimeAfter>> "+mBeginTimeAfter);
					if(mBeginTimeAfter > mBeginTimeBefore){
						//local day num need -1
						mHandler.sendEmptyMessage(EPGConfig.EPG_NEED_UPDATE_LOCAL_DAYNUM);
					}
				} else {
					mStartTime = 24 - EPGConfig.mTimeSpan + mStartTime;
				}
				if(mStartTime == startBound - 1){// for cover hour 1 3 5 7...
					mStartTime = mStartTime + 1;
				} else if((mStartTime >= startBound%24) && (mStartTime < (startBound%24 + EPGConfig.mTimeSpan))){
				}
				mListViewAdpter.setStartHour(mStartTime%24);
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
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeTimeZoom end>>"+ mStartTime + " " + mDayNum);
		mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
		mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
		Message message = mHandler.obtainMessage();
		message.what = EPGConfig.EPG_SYNCHRONIZATION_MESSAGE;
		message.arg1 = mDayNum;
		message.arg2 = mStartTime;
		mHandler.sendMessage(message);
		mListViewAdpter.setActiveWindow();
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		if (this == null || this.hasFocus() == false || !mCanKeyUp) {
//			return false;
//		}
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
      if(ScanContent.isZiggo() && !CommonIntegration.getInstance().isALLOWChangeForZiggo(true)){
        mHandler.sendEmptyMessage(EPGConfig.EPG_NEED_FINISH_EPG);
        return true;
      }
//			if (mCanKeyUp) {
				mCanKeyUp = false;
				EPGConfig.avoidFoucsChange = true;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSelectedItemPosition()>>>"
						+ getSelectedItemPosition());
				EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition();
				if (EPGConfig.SELECTED_CHANNEL_POSITION < 0) {
					EPGConfig.SELECTED_CHANNEL_POSITION = 0;
				}
				mCurrentChannel = (EPGChannelInfo) getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
				if (CommonIntegration.getInstance().is3rdTVSource()) {
					// mReader.selectChannelByTIF(mCurrentChannel.mId);
					sendChangeCHMsg(mCurrentChannel);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelByTIF");
				}
				if (mCurrentChannel != null) {
					mNextSelectedItemView = getSelectedDynamicLinearLayout(EPGConfig.SELECTED_CHANNEL_POSITION);
					int postion = mCurrentChannel
							.getNextPosition(mLastSelectedTVProgram);
					if (mNextSelectedItemView != null) {
						mNextSelectedItemView.setSelectedPosition(postion);
					}
					sendChangeCHMsg(mCurrentChannel);
				} else {
					mHandler.sendEmptyMessageDelayed(
							EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
					mCanChangeChannel = true;
				}
//			} else {
//				return true;
//			}
			break;
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void sendChangeCHMsg(EPGChannelInfo channelInfo) {
		mHandler.removeMessages(EPGConfig.EPG_CHANGING_CHANNEL);
		Message msg = Message.obtain();
		msg.what = EPGConfig.EPG_CHANGING_CHANNEL;
		msg.obj = channelInfo;
		mHandler.sendMessage(msg);
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
		  FrameLayout frameLayout = (FrameLayout) mListViewChildView.getChildAt(1);
			mDynamicLinearLayout = (EPGLinearLayout) frameLayout.getChildAt(0);
		} else {
			mListViewChildView = (LinearLayout) getSelectedView();
			if (mListViewChildView != null){
	      FrameLayout frameLayout = (FrameLayout) mListViewChildView.getChildAt(1);
	      mDynamicLinearLayout = (EPGLinearLayout) frameLayout.getChildAt(0);
      }
   }
		return mDynamicLinearLayout;
	}

  public RelativeLayout getSelectedChannelLayout(int mPosition) {
      RelativeLayout mPvrView = null;
      LinearLayout mListViewChildView = (LinearLayout) getChildAt(mPosition);
      if (mListViewChildView != null){
        FrameLayout frameLayout = (FrameLayout) mListViewChildView.getChildAt(1);
        mPvrView = (RelativeLayout) frameLayout.getChildAt(1);
      }
      return mPvrView;
    }

	public void rawChangedOfChannel() {
		mSelectedItemView = getSelectedDynamicLinearLayout(getSelectedItemPosition());
		if (mListViewAdpter != null && mSelectedItemView != null) {
			int position = mSelectedItemView.getmCurrentSelectPosition();
			if (mCurrentChannel != null && position != -1) {
				List<EPGProgramInfo> childTVProgram = mCurrentChannel
						.getmTVProgramInfoList();
				if (childTVProgram != null && !childTVProgram.isEmpty()&&position<childTVProgram.size()) {
					if (position >= childTVProgram.size()) {
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
			// mListViewAdpter.notifyDataSetChanged();

			EPGConfig.SELECTED_CHANNEL_POSITION = getSelectedItemPosition();
			if (EPGConfig.avoidFoucsChange) {
				EPGConfig.FROM_WHERE = EPGConfig.FROM_ANOTHER_STREAM;
			} else {
				EPGConfig.FROM_WHERE = EPGConfig.AVOID_PROGRAM_FOCUS_CHANGE;
			}
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW,
					1000);
		}
	}
}
