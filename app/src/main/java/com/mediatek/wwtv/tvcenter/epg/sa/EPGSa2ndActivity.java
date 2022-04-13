package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.ViewTreeObserver;
import android.view.Gravity;

//import com.mediatek.twoworlds.tv.MtkTvBanner;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvIntent;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
//import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.epg.IPageCallback;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsManager;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGPwdDialog;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGChannelListView;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActionImpl;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuChannelAdapter;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuEventAdapter;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuIAction;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuIView;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEventListView;
import com.mediatek.wwtv.tvcenter.epg.eu.EpgType;
import com.mediatek.wwtv.tvcenter.epg.DigitTurnCHView;
import com.mediatek.wwtv.tvcenter.epg.DigitTurnCHView.OnDigitTurnCHCallback;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.PageImp;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;

public class EPGSa2ndActivity extends BaseActivity implements
		OnDismissListener, ComponentStatusListener.ICStatusListener, EPGEuIView {

	private static final String TAG = "EPGSa2ndActivity";

	private static final int PER_PAGE_LINE = 4;

	private EPGChannelListView mLvChannel;
	private EPGEventListView mLvEvent;
	private EPGEuChannelAdapter mChannelAdapter;
	private EPGEuEventAdapter mEventAdapter;
	private PageImp mPageChannel = null;
	private PageImp mPageEvent = null;
//	private MtkTvBanner mMtkTvBanner;
	private EPGEuIAction mDataAction;
	private int dayNum = 0;
	private DigitTurnCHView mDigitTurnCHView;
	private TextView mDataRetrievingShow; // data retrieving show
	private TextView mCurrentDateTv; // current date
	private TextView mSelectedDateTv; // selected date
	private LinearLayout mllayoutDetails;
	private TextView mProgramNameTv; // program name
	private TextView mProgramType; // program type
	private TextView mProgramRating; // program rating
	private TextView mProgramTimeTv; // program time
	private TextView mProgramDetailTv; // program detail info
	private TextView mPageInfoTv; // page info
	private TextView mPrevDayTv; // previous day
	private TextView mNextDayTv; // next day
	private TextView mViewDetailTv; // view detail
	private TextView mTypeFilter; // type filter
	private ImageView mLockImageView;
	private ImageView mSttlImageView;
	// private TVTimerListener listener;
	private EPGPwdDialog mEPGPwdDialog;
	private int mTotalPage; // total page
	private int mCurrentPage = 1; // current page
	private EPGProgramInfo mCurrentSelectedProgramInfo;
	// private EPGPasswordEditText mEPGPasswordEditText;
	private final String[] preValues = new String[3];

	private long lastTime = 0L;
	private long lastHourTime = 0L;
	// public boolean mIsChangeChannel;

	private int mStartHour;

	private EPGChannelInfo mSelectChannelInfo;

	// private SharedPreferences mSharedPreferences;
	// private Editor mEditor;
	// private boolean mCanKeyUpToExit, mMenuKeyEixt, mMenuKeyUpProcess;
	private int mLastChannelUpdateMsg;
	private HandlerThread mHandlerThead;
	private Handler mThreadHandler;
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGEuHandleMessage>>msg.what=" + msg.what
					+ ",msg.arg1=" + msg.arg1 + ",msg.arg2=" + msg.arg2);
			switch (msg.what) {
			case EPGUsManager.Message_Refresh_Time:
				mCurrentDateTv.setText((String) msg.obj);
				setCurrentDate();
				break;
			case TvCallbackConst.MSG_CB_EVENT_NFY:
				TvCallbackData data = (TvCallbackData) msg.obj;
				com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Epg MSG_CB_NFY_EVENT_UPDATE message:type:"
						+ data.param1 + "==>" + data.param2 + "==>"
						+ data.param3 + "==>" + data.param4);
				switch (data.param1) {
				// case 1: //insert
				// updateProgramList();
				// break;
				// case 2: //update
				// updateEvent(data);
				// break;
				// case 3: //delete
				// case 4: //current and next update
				case 5: // current window update
					break;
				case 6: // current window list update
					break;
				default:
					break;
				}
				break;
			case EPGConfig.EPG_UPDATE_API_EVENT_LIST:
				changeTimeViewsShow(EPGSa2ndActivity.this.dayNum, mStartHour);
				loadEventList();
				break;
			case TvCallbackConst.MSG_CB_CHANNELIST:
			case TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG update channel list>"
						+ ((TvCallbackData) msg.obj).param2);
				if (mLastChannelUpdateMsg == 8) {// modify
					removeMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST);
				} else if (mHandler
						.hasMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST)) {
					return;
				}
				mLastChannelUpdateMsg = ((TvCallbackData) msg.obj).param2;
				Message message = Message.obtain();
				message.what = EPGConfig.EPG_UPDATE_CHANNEL_LIST;
				message.arg1 = mLastChannelUpdateMsg;
				sendMessageDelayed(message, 1000);
				break;
			case EPGConfig.EPG_UPDATE_CHANNEL_LIST:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_UPDATE_CHANNEL_LIST1111>"
						+ msg.arg1);
				getChannelList();
				break;
			case TvCallbackConst.MSG_CB_BANNER_MSG:
            	TvCallbackData specialMsgData = (TvCallbackData) msg.obj;
				if(BannerView.BANNER_MSG_NAV == specialMsgData.param1){
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in handleMessage BANNER_MSG_NAV value=== " + specialMsgData.param2);
					switch (specialMsgData.param2) {
					case BannerView.SPECIAL_CHANNEL_LOCK:
					case BannerView.SPECIAL_PROGRAM_LOCK:
					case BannerView.SPECIAL_INPUT_LOCK:
						mDataAction.checkPWDShow();
						break;
					default:
						break;
					}
				}
            	break;
			default:
				break;
			}
		}

	};

	private BroadcastReceiver mUpdateEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!CommonIntegration.supportTIFFunction()) { // this function is
															// only for TIF ||
															// mIsChangeChannel
				return;
			}
			String actionName = intent.getAction();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "actionName>>>" + actionName);
			if (actionName
					.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN)
					|| actionName
							.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF)) { // actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF)
																				// ||
                    if (actionName
						.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN)) {// update
					int channelId = intent.getIntExtra("channel_id", 0);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getProgramListByChId--->channelId="+channelId);// event
					getCurCHEventList();
				}
			}
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window mWindow = getWindow();
		if (null != mWindow) {
			mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.epg_sa_2nd_main);
		initUI();
		initData();
		changeTimeViewsShow(dayNum, mStartHour);
		registerListeners();
		registerUpdateReceiver();
		setCurrentDate();
		mesureListView();
		
	}

	private void registerListeners() {
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_EVENT_NFY, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_CHANNELIST, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_BANNER_MSG, mHandler);
		ComponentStatusListener lister = ComponentStatusListener.getInstance();
		lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
		lister.addListener(ComponentStatusListener.NAV_ENTER_LANCHER, this);
		lister.addListener(ComponentStatusListener.NAV_POWER_OFF, this);
		mLvChannel.addPageCallback(new IPageCallback() {

			@Override
			public boolean hasPrePage() {
				if (mPageChannel == null) {
					return false;
				}
				boolean hasMorePage = mPageChannel.getPageNum() > 1;
				if (hasMorePage && !mPageChannel.hasPrePage()) {
					// goto last page
					mPageChannel.gotoPage(mPageChannel.getPageNum());
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasMorePage=" + hasMorePage);
				return hasMorePage;
			}

			@Override
			public boolean hasNextPage() {
				if (mPageChannel == null) {
					return false;
				}
				boolean hasMorePage = mPageChannel.getPageNum() > 1;
				if (hasMorePage && !mPageChannel.hasNextPage()) {
					// goto head page
					mPageChannel.gotoPage(1);
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasMorePage=" + hasMorePage);
				return hasMorePage;
			}

			@Override
			public void onRefreshPage() {
				updateChannelList();
			}

		});
		mLvChannel.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				boolean firstEnterEPG=mSelectChannelInfo==null;
				mSelectChannelInfo = (EPGChannelInfo) mChannelAdapter
						.getItem(position);
				// No need turn channel when enter EPG at first. 
				if(!firstEnterEPG){
					turnChannel(mSelectChannelInfo);
				}
				loadEventList();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelInfo=" + mSelectChannelInfo.getName());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onNothingSelected");
			}

		});
		mLvChannel.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(checkPwdShow()){
					return;
				}
			}
			
		});
		
		mLvEvent.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(checkPwdShow()){
					return;
				}
			}
			
		});

		mLvEvent.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				mCurrentSelectedProgramInfo = (EPGProgramInfo) mEventAdapter
						.getItem(position);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentSelectedProgramInfo="
						+ mCurrentSelectedProgramInfo.getmTitle()
						+ ",position=" + position);
				refreshDetailsInfo(mCurrentSelectedProgramInfo);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onNothingSelected");
			}

		});
		mLvChannel.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					mChannelAdapter.setSelPosNoFocus(-1);
				} else {
					int lastSelPos = mLvChannel.getSelectedItemPosition();
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lastSelPos=" + lastSelPos);
					mChannelAdapter.setSelPosNoFocus(lastSelPos);
				}

			}

		});

		mLvEvent.addPageCallback(new IPageCallback() {

			@Override
			public boolean hasPrePage() {
				if (mPageEvent == null) {
					return false;
				}
				boolean hasMorePage = mPageEvent.getPageNum() > 1;
				if (hasMorePage && !mPageEvent.hasPrePage()) {
					// goto last page
					mPageEvent.gotoPage(mPageEvent.getPageNum());
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasMorePage=" + hasMorePage);
				return hasMorePage;
			}

			@Override
			public boolean hasNextPage() {
				if (mPageEvent == null) {
					return false;
				}
				boolean hasMorePage = mPageEvent.getPageNum() > 1;
				if (hasMorePage && !mPageEvent.hasNextPage()) {
					// goto head page
					mPageEvent.gotoPage(1);
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasMorePage=" + hasMorePage);
				return hasMorePage;
			}

			@Override
			public void onRefreshPage() {
				updateEventList();
			}

		});
	}
	
	private boolean checkPwdShow(){
		if(mLockImageView.getVisibility()==View.VISIBLE){
			// If current channel is locked
			mEPGPwdDialog.show();
			mEPGPwdDialog.sendAutoDismissMessage();
			changeBottomViewText(true, KeyEvent.KEYCODE_ENTER);
			setProgramInfoViewsInVisiable();
			mLockImageView.setVisibility(View.INVISIBLE);
			return true;
		}
		return false;
	}

	private void getCurCHEventList() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurCHEventList start!");
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurCHEventList start mSelectChannelInfo>>>"+mSelectChannelInfo);
		if (mSelectChannelInfo != null){
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurCHEventList start displayname>>>"+mSelectChannelInfo.getDisplayNumber());
		  mDataAction.getProgramListByChId(mSelectChannelInfo, dayNum,
		      mStartHour);
		}
	}

	/**
	 * 3rd channel get data directly by TIF,but TV source call setActiveWindow
	 * at first,then get data by TIF after notify broadcast.
	 */
	private void loadEventList() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEventList start!");
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEventList mSelectChannelInfo>>>"+mSelectChannelInfo);
		if (mSelectChannelInfo == null) {
			return;
		}
		if (mDataAction.is3rdTVSource()) {
			getCurCHEventList();
		} else {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEventList dayNum>>>"+dayNum+", mStartHour>>>>"+mStartHour);
			mDataAction.setActiveWindow(mSelectChannelInfo, dayNum, mStartHour);
		}
	}

	private void initData() {
		mHandlerThead = new HandlerThread(TAG);
		mHandlerThead.start();
		mThreadHandler = new Handler(mHandlerThead.getLooper());
//		mMtkTvBanner = MtkTvBanner.getInstance();
		mEPGPwdDialog = new EPGPwdDialog(this);
		mEPGPwdDialog.setAttachView(findViewById(R.id.epg_content_layout));
		mEPGPwdDialog.setOnDismissListener(this);
		lastHourTime = EPGUtil.getCurrentDayHourMinute();
		mDataAction = new EPGEuActionImpl(this, this);
		mChannelAdapter = new EPGEuChannelAdapter(this);
		mEventAdapter = new EPGEuEventAdapter(this);
		setListViewItemHeight();
		mLvChannel.setAdapter(mChannelAdapter);
		mLvEvent.setAdapter(mEventAdapter);
		mStartHour = EPGUtil.getCurrentHour();
		getChannelList();
	}

	private void addDigitTurnCHView() {
		FrameLayout contentView = getWindow().getDecorView().findViewById(
				android.R.id.content);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				getResources().getDimensionPixelOffset(
						R.dimen.digit_turn_view_width),
				getResources().getDimensionPixelOffset(
						R.dimen.digit_turn_view_height));
		lp.gravity = Gravity.CENTER;
		mDigitTurnCHView = new DigitTurnCHView(this);
		mDigitTurnCHView.requestFocus();
		mDigitTurnCHView.setVisibility(View.GONE);
		mDigitTurnCHView.setOnDigitTurnCHCallback(new OnDigitTurnCHCallback() {

			@Override
			public void onTurnCH(int inputDigit) {
				// TODO Auto-generated method stub
				List<EPGChannelInfo> channelList = (List<EPGChannelInfo>) mPageChannel
						.getCurrentList();
				if(channelList==null){
					com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "channelList==null");
					return;
				}
				int index = -1;
				long channelId = 0;
				EPGChannelInfo changeChannel = null;
				for (int i = 0; i < channelList.size(); i++) {
					EPGChannelInfo channelInfo = channelList.get(i);
					if (channelInfo.getmChanelNum() == inputDigit) {
						changeChannel = channelInfo;
						index = i;
						channelId = channelInfo.mId;
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(
								TAG,
								"channelInfo=[num:"
										+ channelInfo.getmChanelNum()
										+ ",name:" + channelInfo.getName()
										+ "]");
						break;
					}
				}
				if (index < 0) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index<0");
					return;
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index=" + index + ",channelId=" + channelId);
				int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
				mLvChannel.setSelection(pageNum);
				turnChannel(changeChannel);
			}

		});
		mDigitTurnCHView.setBackgroundResource(R.drawable.nav_sundry_bg);
		contentView.addView(mDigitTurnCHView, lp);
	}

	private void turnChannel(EPGChannelInfo changeChannel) {
    CommonIntegration.getInstance().selectChannelById((int) changeChannel.mId);
  }

	private void registerUpdateReceiver() {
		IntentFilter updateIntentFilter = new IntentFilter();
		updateIntentFilter.addAction(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF);
		updateIntentFilter
				.addAction(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN);
		this.registerReceiver(mUpdateEventReceiver, updateIntentFilter);
	}

	private void initUI() {
		// mEPGPasswordEditText = (EPGPasswordEditText)
		// findViewById(R.id.epg_password);
		mLvChannel = (EPGChannelListView) findViewById(R.id.lv_channel);
		mLvEvent = (EPGEventListView) findViewById(R.id.lv_event);
		mDataRetrievingShow = (TextView) findViewById(R.id.epg_retrieving_data);
		mCurrentDateTv = (TextView) findViewById(R.id.epg_top_date_info_tv);
		mSelectedDateTv = (TextView) findViewById(R.id.epg_title_date_selected_tv);
		mllayoutDetails = (LinearLayout) findViewById(R.id.epg_content_layout);
		mProgramNameTv = (TextView) findViewById(R.id.epg_program_info_name);
		mProgramTimeTv = (TextView) findViewById(R.id.epg_program_info_time);
		mProgramDetailTv = (TextView) findViewById(R.id.epg_program_info_detail);
		mProgramDetailTv.setLines(PER_PAGE_LINE);
    mProgramDetailTv.setVisibility(View.INVISIBLE);
		mProgramType = (TextView) findViewById(R.id.epg_program_info_type);
		mProgramRating = (TextView) findViewById(R.id.epg_program_rating);
		mPageInfoTv = (TextView) findViewById(R.id.epg_info_page_tv);
		mPrevDayTv = (TextView) findViewById(R.id.epg_bottom_prev_day_tv);
		mNextDayTv = (TextView) findViewById(R.id.epg_bottom_next_day_tv);
		mViewDetailTv = (TextView) findViewById(R.id.epg_bottom_view_detail);
		mTypeFilter = (TextView) findViewById(R.id.epg_bottom_view_filter);

		mLockImageView = (ImageView) findViewById(R.id.epg_info_lock_icon);
		mSttlImageView = (ImageView) findViewById(R.id.epg_info_sttl_icon);


		mProgramDetailTv.setMovementMethod(ScrollingMovementMethod
				.getInstance());
		addDigitTurnCHView();
	}

	private void mesureListView() {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "EPG onGlobalLayout mesureListView");
		ViewTreeObserver vto = findViewById(R.id.epg_listview_layout)
				.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
		    findViewById(R.id.epg_listview_layout).getViewTreeObserver()
            .removeGlobalOnLayoutListener(this);
        setListViewItemHeight();
				getChannelList();
			}
		});

	}

	

	protected void setListViewItemHeight() {
    int mListHeight = findViewById(R.id.epg_listview_layout).getHeight();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG setListViewItemHeight mListHeight>> "+mListHeight);
    //getResources().getDisplayMetrics().density == divider height
    int itemHeight = mListHeight/EPGConfig.EPG_LIST_ITME_COUNT- (int)(getResources().getDisplayMetrics().density+0.7f);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG setListViewItemHeight itemHeight>> "+itemHeight);
    if(itemHeight > 0){
      mChannelAdapter.setItemHeight(itemHeight);
      mEventAdapter.setItemHeight(itemHeight);
    }
  }

  private void getChannelList() {
		mDataAction.getChannelList();
	}


	

	@Override
	protected void onPause() {
		super.onPause();
		com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "EPG on Pause");
	}

	@Override
	protected void onStop() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestroy()...");
		clearData();
	}

	private Runnable mGetCurrentTimeRunnable = new Runnable() {

		@Override
		public void run() {
			long curTime = EPGUtil.getCurrentTime(mDataAction.is3rdTVSource());
			if (curTime - lastTime < -60 * 10
					|| curTime - lastHourTime >= 60 * 60) {
				dayNum = 0;
				lastHourTime = EPGUtil.getCurrentDayHourMinute(mDataAction.is3rdTVSource());
				mStartHour = EPGUtil.getCurrentHour(mDataAction.is3rdTVSource());
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mGetCurrentTimeRunnable~mStartHour="+mStartHour);
				mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
				mHandler.sendEmptyMessageDelayed(
						EPGConfig.EPG_UPDATE_API_EVENT_LIST, 1000);
			}
			lastTime = curTime;
			String mDate = EPGUtil.formatCurrentTime(EPGSa2ndActivity.this,mDataAction.is3rdTVSource());// tmCvt.getDetailDate(DataReader.getCurrentDate());
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mGetCurrentTimeRunnable~mDate="+mDate);
			Message msg = Message.obtain();
			msg.obj = mDate;
			msg.what = EPGUsManager.Message_Refresh_Time;
			mHandler.sendMessageDelayed(msg, 1000);
		}
	};

	/**
	 * set the current date in the top
	 */
	private void setCurrentDate() {
		if (mDataAction.isCurrentSourceATV()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceATV~");
			mCurrentDateTv.setVisibility(View.INVISIBLE);
		} else {
			mCurrentDateTv.setVisibility(View.VISIBLE);
		}
		if (mThreadHandler != null && mGetCurrentTimeRunnable != null) {
			mThreadHandler.post(mGetCurrentTimeRunnable);
		}
	}

	private void clearData() {
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_CHANNELIST, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_BANNER_MSG, mHandler);
		if (mHandler != null) {
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST);
		}
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_EVENT_NFY, mHandler);
		ComponentStatusListener lister = ComponentStatusListener.getInstance();
		lister.removeListener(this);
		if (mUpdateEventReceiver != null) {
			unregisterReceiver(mUpdateEventReceiver);
			mUpdateEventReceiver = null;
		}
		if (mHandler != null) {
			mHandler.removeMessages(EPGUsManager.Message_Refresh_Time);
			mHandler.removeCallbacksAndMessages(null);
		}
		if (mThreadHandler != null) {
			mThreadHandler.removeCallbacks(mGetCurrentTimeRunnable);
			mThreadHandler.removeCallbacksAndMessages(null);
			mHandlerThead.quit();
			mGetCurrentTimeRunnable = null;
			mThreadHandler = null;
			mHandlerThead = null;
		}
		mDataAction.clearActiveWindow();
	}

	@Override
	public boolean onKeyDown(final int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event.getRepeatCount()>>>" + event.getRepeatCount());
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyCode=" + keyCode);
		switch (keyCode) {
		case KeyMap.KEYCODE_0:
		case KeyMap.KEYCODE_1:
		case KeyMap.KEYCODE_2:
		case KeyMap.KEYCODE_3:
		case KeyMap.KEYCODE_4:
		case KeyMap.KEYCODE_5:
		case KeyMap.KEYCODE_6:
		case KeyMap.KEYCODE_7:
		case KeyMap.KEYCODE_8:
		case KeyMap.KEYCODE_9:
			mDigitTurnCHView.keyHandler(keyCode, event);
			return true;
		case KeyMap.KEYCODE_BACK:
			if (mDigitTurnCHView.getVisibility() == View.VISIBLE) {
				mDigitTurnCHView.hideView();
			} else if (event.getRepeatCount() <= 0) {
				finish();
			}
			break;
		case KeyMap.KEYCODE_MTKIR_GUIDE:
			if (event.getRepeatCount() <= 0) {
				finish();
			}
			// TurnkeyUiMainActivity.setShowBannerInOnResume(false);
			return true;
		case KeyMap.KEYCODE_MENU:
			return true;
		case KeyMap.KEYCODE_MTKIR_SOURCE:
			return true;
			// yellow key
		case KeyEvent.KEYCODE_Y:
		case KeyMap.KEYCODE_MTKIR_YELLOW:
			// page info
			if (mTotalPage > 1) {
				mCurrentPage++;
				if (mCurrentPage > mTotalPage) {
					mCurrentPage = 1;
				}
				mProgramDetailTv.scrollTo(0, (mCurrentPage - 1) * PER_PAGE_LINE
						* mProgramDetailTv.getLineHeight());
				mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
			}
			return true;
		case KeyMap.KEYCODE_MTKIR_BLUE:
		case KeyEvent.KEYCODE_B:
			changeBottomViewText(true, keyCode);
			final EpgType mEpgType = new EpgType(this);
			mEpgType.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					changeBottomViewText(false, 0);
					if (mEpgType.mHasEditType) {
						mEventAdapter.notifyDataSetChanged();
						mEpgType.mHasEditType = false;
					}
				}
				
			});
			mEpgType.show();
			return true;
		case KeyMap.KEYCODE_MTKIR_GREEN:
		case KeyEvent.KEYCODE_G:
			if (dayNum == EPGConfig.mMaxDayNum) {
				return false;
			}
			dayNum = dayNum + 1;
			mStartHour = 0;
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(
					EPGConfig.EPG_UPDATE_API_EVENT_LIST, 500);
			return true;
		case KeyMap.KEYCODE_MTKIR_RED:
		case KeyEvent.KEYCODE_R:
			if (dayNum == 0) {
				return false;
			}
			dayNum = dayNum - 1;
			if (dayNum == 0) {
				mStartHour = EPGUtil.getCurrentHour();
			} else {
				mStartHour = 0;
			}
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(
					EPGConfig.EPG_UPDATE_API_EVENT_LIST, 500);
			return true;
		case KeyMap.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_E:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_CENTER");
			
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyUp>>>>" + keyCode + "  " + event.getAction());
		switch (keyCode) {
		case KeyMap.KEYCODE_VOLUME_UP:
		case KeyMap.KEYCODE_VOLUME_DOWN:
			return true;
		case KeyMap.KEYCODE_MTKIR_RECORD:
		case KeyMap.KEYCODE_PAGE_DOWN:
			calledByScheduleList();
			return true;
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}


	

	private void setLockIconVisibility(boolean isLocked) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockIconVisibility>>>" + isLocked);
		if (isLocked) {
			if (mEPGPwdDialog != null && mEPGPwdDialog.isShowing()) {
				mLockImageView.setVisibility(View.INVISIBLE);
			} else {
				mLockImageView.setVisibility(View.VISIBLE);
			}
			mSttlImageView.setVisibility(View.INVISIBLE);
			mProgramDetailTv.setVisibility(View.INVISIBLE);
		} else {
			mLockImageView.setVisibility(View.INVISIBLE);
		}
	}


	private void refreshDetailsInfo(EPGProgramInfo programInfo) {
		 if(mSelectChannelInfo==null){
			 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSelectChannelInfo==null");
			 return;
		 }
		 MtkTvChannelInfoBase tvChannel=mSelectChannelInfo.getTVChannel();
		 if(tvChannel==null){
			 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvChannel==null");
			 return;
		 }
		 mDataAction.refreshDetailsInfo(programInfo,tvChannel.getChannelId());
	}



	private void setProgramInfoViewsInVisiable() {
		mProgramNameTv.setVisibility(View.INVISIBLE);
		mProgramTimeTv.setVisibility(View.INVISIBLE);
		mProgramDetailTv.setVisibility(View.INVISIBLE);
		mSttlImageView.setVisibility(View.INVISIBLE);
		mPageInfoTv.setText("");
		mViewDetailTv.setText("");
		mProgramType.setText("");
		mProgramRating.setText("");
	}



	
	

//	/**
//	 * get caption icon display state
//	 * 
//	 * @return
//	 */
//	private boolean isShowSTTLIcon() {
//		boolean showCaptionIcon = mMtkTvBanner.isDisplayCaptionIcon();
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowCaptionIcon, value == " + showCaptionIcon);
//		return showCaptionIcon;
//	}


	private void setProgramDetailTvState(EPGProgramInfo programInfo) {
    mDataAction.checkPWDShow();
		if (programInfo != null&& programInfo.getAppendDescription() != null) {
			String mDetailContent = programInfo.getAppendDescription();
			mProgramDetailTv.setText(mDetailContent);
			if (TextUtils.isEmpty(mDetailContent)) {
				mViewDetailTv.setText("");
				mPageInfoTv.setText("");
			}
			initProgramDetailContent();
		} else {
			mProgramDetailTv.setText("");
			mViewDetailTv.setText("");
			mPageInfoTv.setText("");
			mTotalPage = 0;
		}
	}
	
	
	
	
	

	private void initProgramDetailContent() {
		int line = mProgramDetailTv.getLineCount();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- initProgramDetailContent()---- Lines: " + line);
		if (line > 0) {
			mTotalPage = (line % PER_PAGE_LINE == 0) ? (line / PER_PAGE_LINE)
					: (line / PER_PAGE_LINE + 1);
			mCurrentPage = 1;
			mProgramDetailTv.scrollTo(0, (mCurrentPage - 1) * PER_PAGE_LINE
					* mProgramDetailTv.getLineHeight());
			if (mTotalPage > 1) {
				mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
				mViewDetailTv.setText(getResources().getString(
						R.string.epg_bottom_view_detail));
			} else {
				mViewDetailTv.setText("");
				mPageInfoTv.setText("");
			}
		} else {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					initProgramDetailContent();

				}
			}, 10);
		}
	}

	public void changeBottomViewText(boolean isEnter, int keyCode) {
		if (isEnter) {
			savePreValues();
			mPrevDayTv.setText("");
			mNextDayTv.setText("");
			mViewDetailTv.setText("");
			switch (keyCode) {
			case KeyMap.KEYCODE_MTKIR_BLUE:
			case KeyEvent.KEYCODE_B:
				mTypeFilter.setText(getResources().getString(
						R.string.setup_exit));
				break;
			case KeyMap.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_E:
				mTypeFilter.setText("");
				break;
			default:
				break;
			}
		} else {
			mPrevDayTv.setText(preValues[0]);
			mNextDayTv.setText(preValues[1]);
			mViewDetailTv.setText(preValues[2]);
			mTypeFilter.setText(getResources().getString(
					R.string.epg_bottom_type_filter));
		}
	}

	private void savePreValues() {
		preValues[0] = mPrevDayTv.getText().toString();
		preValues[1] = mNextDayTv.getText().toString();
		preValues[2] = mViewDetailTv.getText().toString();
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWD onDismiss!!>>");
		refreshDetailsInfo(mCurrentSelectedProgramInfo);
		changeBottomViewText(false, 0);
	}


	private void calledByScheduleList() {
		DvrManager.getInstance().startScheduleList(mCurrentSelectedProgramInfo);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("Timeshift_PVR", "calledByScheduleList()");
	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EU EPG updateComponentStatus>>>" + statusID + ">>"
				+ value);
		if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
			CommonIntegration.getInstance().setCHChanging(false);
		} else if (statusID == ComponentStatusListener.NAV_ENTER_LANCHER) {
			finish();
		} else if (statusID == ComponentStatusListener.NAV_POWER_OFF) {
			finish();
		}
	}

	@Override
	public void updateEventDetails(EPGProgramInfo programInfo) {
		// TODO Auto-generated method stub
		if (programInfo == null) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateEventDetails---->programInfo==null");
			mllayoutDetails.setVisibility(View.INVISIBLE);
			return;
		}
		mllayoutDetails.setVisibility(View.VISIBLE);
		String title=TextUtils.isEmpty(mCurrentSelectedProgramInfo.getTitle())?this.getResources().getString(R.string.nav_epg_no_program_title):mCurrentSelectedProgramInfo.getTitle();
		mProgramNameTv.setText(title);
		mProgramNameTv.setVisibility(View.VISIBLE);
		String startTime=mDataAction.getTimeType()==1?programInfo.getmStartTimeStr():Util.formatTime24To12(programInfo.getmStartTimeStr());
		String endTime=mDataAction.getTimeType()==1?programInfo.getmEndTimeStr():Util.formatTime24To12(programInfo.getmEndTimeStr());
		com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "regetProgramInfo------->startTime="+startTime+",endTime="+endTime);
		mProgramTimeTv.setText(String.format("%s-%s", startTime, endTime));
		mProgramTimeTv.setVisibility(View.VISIBLE);
		setProgramDetailTvState(programInfo);
		mProgramType.setText(programInfo.getProgramType());
		mProgramRating.setText(programInfo.getRatingType());
	}

	

	@Override
	public void updateChannelList(List<EPGChannelInfo> channelList,
			int selectIndex, int page) {
		mPageChannel = new PageImp(channelList,
				DataReader.PER_PAGE_CHANNEL_NUMBER);
		mPageChannel.gotoPage(page);
		updateChannelList();
		int position = selectIndex % DataReader.PER_PAGE_CHANNEL_NUMBER;
		mLvChannel.setSelection(position);
	}

	@Override
	public void updateProgramList(List<EPGProgramInfo> programList) {
		mPageEvent = new PageImp(programList,
				DataReader.PER_PAGE_CHANNEL_NUMBER);
		mPageEvent.gotoPage(1);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateProgramList------>programList.size: "
				+ programList.size());
		updateEventList();
	}

	private void updateChannelList() {
		List<EPGChannelInfo> curChannelInfos = (List<EPGChannelInfo>) mPageChannel
				.getCurrentList();
		mChannelAdapter.setGroup(curChannelInfos);
		mChannelAdapter.notifyDataSetChanged();
		mLvChannel.requestFocus();
	}

	private void updateEventList() {
		List<EPGProgramInfo> curEventInfos = (List<EPGProgramInfo>) mPageEvent
				.getCurrentList();
		mEventAdapter.setGroup(curEventInfos);
		mEventAdapter.notifyDataSetChanged();
		mCurrentSelectedProgramInfo = curEventInfos.isEmpty() ? null
				: curEventInfos.get(0);
		refreshDetailsInfo(mCurrentSelectedProgramInfo);
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		mDataRetrievingShow.setVisibility(View.VISIBLE);
	}

	@Override
	public void dismissLoading() {
		mDataRetrievingShow.setVisibility(View.INVISIBLE);
	}

	@Override
	public void updateLockStatus(boolean isLocked) {
		// TODO Auto-generated method stub
		if(!isLocked&&mCurrentSelectedProgramInfo!=null){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateEventDetails---->isHasSubTitle="+ mCurrentSelectedProgramInfo.isHasSubTitle());
			mSttlImageView.setVisibility(mCurrentSelectedProgramInfo.isHasSubTitle() ? View.VISIBLE: View.INVISIBLE);
		}
		setLockIconVisibility(isLocked);
		if (isLocked) {
			mProgramDetailTv.setVisibility(View.INVISIBLE);
			mViewDetailTv.setText("");
			mPageInfoTv.setText("");
		} 
		else{
			mProgramDetailTv.setVisibility(View.VISIBLE);
		}
	}
	private void changeTimeViewsShow(int dayNum, int startHour) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"starthour=="+startHour);
        // Date now = DataReader.getCurrentDate();
        Long time = 24 * 60 * 60L * dayNum;
        // Date day = new Date(now.getTime() + time);
        // Date day = new Date(EPGUtil.getCurrentTime() + time);
        String dateStr = EPGUtil.getSimpleDate(EPGUtil.getCurrentTime() + time);
        mSelectedDateTv.setText(dateStr);
        if (dayNum == 0) {
            mPrevDayTv.setText("");
        } else {
            mPrevDayTv.setText(getString(R.string.epg_bottom_prev_day));
        }
        if (dayNum == EPGConfig.mMaxDayNum) {
            mNextDayTv.setText("");
        } else {
            mNextDayTv.setText(getString(R.string.epg_bottom_next_day));
        }
	}
}
