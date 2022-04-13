package com.mediatek.wwtv.tvcenter.epg.cn;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.twoworlds.tv.MtkTvEvent;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvBanner;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvIntent;
import com.mediatek.twoworlds.tv.common.MtkTvConfigMsgBase;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.wwtv.setting.widget.view.ScheduleListItemInfoDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsManager;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGPwdDialog;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;

public class EPGCnActivity extends BaseActivity implements OnDismissListener {

	private static final String TAG = "EPGCnActivity";
	private static final int PER_PAGE_LINE = 4;
	private EPGListView mListView;
	private DataReader mReader;
	private MtkTvBanner mMtkTvBanner;

	private EPGListViewAdapter mListViewAdpter;
	private MyUpdata mMyUpData;
	private EPGChannelInfo mListViewSelectedChild;
	private int dayNum = 0;

	private TextView mDataRetrievingShow; // data retrieving show
	private TextView mCurrentDateTv; // current date
	private TextView mSelectedDateTv; // selected date
	private TextView mBeginTimeTv; // begin time
	private TextView mEndTimeTv; // end time
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
	private EPGPwdDialog mEPGPwdDialog;
	private int mTotalPage; // total page
	private int mCurrentPage = 1; // current page
	private EPGProgramInfo mCurrentSelectedProgramInfo;
	private String[] preValues = new String[3];

	private long lastTime = 0L;
	private long lastHourTime = 0L;
	private long curTime = 0L;
	private boolean mHasInitListView;
	private boolean mNeedNowGetData;
	private boolean mCanChangeTimeShow;
//, mUpdateOneByOne = true;
  private boolean isSpecialState = false;
  private boolean needFirstShowLock = true;
  private boolean isGetingData;
	private int mLastChannelUpdateMsg;
	private AsyncTaskEventsLoad eventLoad;
	private HandlerThread mHandlerThead;
    private Handler mThreadHandler;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EPGConfig.EPG_INIT_EVENT_LIST:
				if (mListViewAdpter != null && mListView != null) {
					setAdapter((List<EPGChannelInfo>)msg.obj, msg.arg2);
					changeTimeViewsShow(dayNum, mListViewAdpter.getStartHour());
					EPGConfig.SELECTED_CHANNEL_POSITION = msg.arg1 % DataReader.PER_PAGE_CHANNEL_NUMBER;
					mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
				}
				break;
			case EPGConfig.EPG_REFRESH_CHANNEL_LIST:
				if (mListViewAdpter != null && mListView != null) {
					updateAdapter((List<EPGChannelInfo>)msg.obj, msg.arg2);
					changeTimeViewsShow(dayNum, mListViewAdpter.getStartHour());
					EPGConfig.SELECTED_CHANNEL_POSITION = msg.arg1 % DataReader.PER_PAGE_CHANNEL_NUMBER;
					mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
				}
				break;
			case EPGConfig.EPG_SYNCHRONIZATION_MESSAGE:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_SYNCHRONIZATION_MESSAGE>>" + msg.arg1 + "  " + msg.arg2);
				int dayNum = msg.arg1;
				int mStartTime = msg.arg2;
				changeTimeViewsShow(dayNum, mStartTime);
				break;
			case EPGConfig.EPG_SELECT_CHANNEL_COMPLETE:
				if (mReader != null && null != mListView) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_SELECT_CHANNEL_COMPLETE");
					mHandler.removeMessages(EPGConfig.EPG_SET_LOCK_ICON);
//					mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_SET_LOCK_ICON, 2000);
					if (mLockImageView.getVisibility() == View.VISIBLE) {
						mLockImageView.setVisibility(View.INVISIBLE);
					}
					if (mReader.isTvSourceLock()) {
//						EPGManager.getInstance().startActivity(EPGCnActivity.this, com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
						finish();
//						TurnkeyUiMainActivity.setShowBannerInOnResume(false);
					} else {
              // isSpecialState = false;
						mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
						mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 1000);
						mListView.mCanChangeChannel = true;
					}
				}
				break;
			case EPGConfig.EPG_PROGRAMINFO_SHOW:
				showSelectedProgramInfo();
				break;
			case EPGConfig.EPG_PROGRAM_STTL_SHOW:
				if (mCurrentSelectedProgramInfo != null) {
					setSubTitleImageViewState(mCurrentSelectedProgramInfo);
				}
				break;
			case EPGConfig.EPG_DATA_RETRIEVING:
//				mDataRetrievingShow.setText(getString(R.string.epg_retrieving_show));
				mDataRetrievingShow.setVisibility(View.VISIBLE);
				break;
			case EPGConfig.EPG_DATA_RETRIEVAL_FININSH:
//				mDataRetrievingShow.setText("");
				mDataRetrievingShow.setVisibility(View.INVISIBLE);
				break;
			case EPGConfig.EPG_EVENT_REASON_SCHEDULE_UPDATE_REPEAT:
				if(null != mListView){
					mListView.rawChangedOfChannel();
				}
				break;
			case EPGUsManager.Message_Refresh_Time:
				mCurrentDateTv.setText((String)msg.obj);
                setCurrentDate();
                break;
			case TvCallbackConst.MSG_CB_EVENT_NFY:
				TvCallbackData data = (TvCallbackData)msg.obj;
				com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Epg MSG_CB_NFY_EVENT_UPDATE message:type:"+data.param1+"==>"+data.param2+"==>"+data.param3+"==>"+data.param4);
					switch (data.param1) {
        			case 5:  //current window update
        				if (!CommonIntegration.supportTIFFunction()) {
        					if (data.param2 > 0 || data.param3 > 0) {
        						updateProgramList();
        					}
        				}
        				break;
					case 6: //current window list update
							if (CommonIntegration.supportTIFFunction()) {
								new Thread(new Runnable() {

									@Override
									public void run() {
										if (mListView != null && mReader != null && mListViewAdpter != null && mListViewAdpter.getGroup() != null) {
											com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "6start read event in provider>" + mListViewAdpter.getDayNum() + "   " + mListViewAdpter.getStartHour());
										}
									}
								}).start();
							} else {
								new Thread(new Runnable() {

									@Override
									public void run() {
										if (mListView != null && mReader != null && mListViewAdpter != null && mListViewAdpter.getGroup() != null) {
											mReader.readChannelProgramInfoByTime(mListViewAdpter.getGroup(), mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour(), EPGConfig.mTimeSpan);
											mHandler.removeMessages(EPGConfig.EPG_SET_LIST_ADAPTER);
											mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_SET_LIST_ADAPTER, 100);
										}
									}
								}).start();
							}
						break;
					default:
						break;
					}
				break;
			case EPGConfig.EPG_UPDATE_API_EVENT_LIST:
				if (mListViewAdpter != null) {
					if (mCanChangeTimeShow) {
						mCanChangeTimeShow = false;
						changeTimeViewsShow(mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour());
						updateEventList(true);
					} else {
						updateEventList(false);
					}
				}
				break;
			case EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG:
			case EPGConfig.EPG_GET_TIF_EVENT_LIST:
				if (mListView != null && mReader != null && mListViewAdpter != null) {
					if (isGetingData) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "AsyncTaskEventsLoad is readig event in provider>");
//						mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
//						mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
//						mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_GET_TIF_EVENT_LIST, 3000);
						return;
					} else {
						isGetingData = true;
					}
//					final List<EPGChannelInfo> channels = mListViewAdpter.getActivewindowChannels();
//					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start read event in provider>" + mListViewAdpter.getDayNum() + "   " + mListViewAdpter.getStartHour()
//							+ "    " + channels.size());
//					if (channels.size() <= 0) {
//						return;
//					}
//					new Thread(new Runnable() {
//
//						@Override
//						public void run() {
							if (mListView != null && mListViewAdpter != null && mListViewAdpter.getGroup() != null) {
//								com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start ss read event in provider>" + channels.size()
//										+ ">>" + (mListView.getChildCount() != mListViewAdpter.getGroup().size()));
								loadEvents();
//								mReader.readProgramInfoByTIF(channels, mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour());
////								mHandler.removeMessages(EPGConfig.EPG_NOTIFY_LIST_ADAPTER);
////								mHandler.sendEmptyMessage(EPGConfig.EPG_NOTIFY_LIST_ADAPTER);
//								if (mListView != null && mListViewAdpter != null && mListViewAdpter.getGroup() != null) {
//									if (mListView.getChildCount() != mListViewAdpter.getGroup().size()) {
//										mHandler.sendEmptyMessage(EPGConfig.EPG_SET_LIST_ADAPTER);
//									} else {
//										mHandler.post(new Runnable() {
//											public void run() {
//												refreshUpdateLayout(channels);
//												showSelectedProgramInfo();
//												mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
//												mListView.mCanChangeChannel = true;
//												isGetingData = false;
//											}
//										});
//									}
//								}
							}
//						}
//					}).start();
				}
				break;
			case EPGConfig.EPG_SET_LIST_ADAPTER:
				if (mListViewAdpter != null && mListView != null) {
					mListView.setAdapter(mListViewAdpter);
					mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
					showSelectedProgramInfo();
					mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
					mListView.mCanChangeChannel = true;
					isGetingData = false;
				}
				break;
			case EPGConfig.EPG_NOTIFY_LIST_ADAPTER:
				if (mListViewAdpter != null) {
					mListViewAdpter.notifyDataSetChanged();
					mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
					mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 800);
					mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
					mListView.mCanChangeChannel = true;
				}
				break;
            case TvCallbackConst.MSG_CB_CONFIG:
            {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_CONFIG" + ((TvCallbackData)msg.obj).param1);
                if(MtkTvConfigMsgBase.ACFG_MSG_CHG_INPUT == ((TvCallbackData)msg.obj).param1 &&
                    DestroyApp.isCurEPGActivity()){
                	finish();
                }
            }
                break;
            case TvCallbackConst.MSG_CB_CHANNELIST:
            case TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE:
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG update channel list>" + ((TvCallbackData)msg.obj).param2);
            	if (mLastChannelUpdateMsg == 8) {//modify
            		removeMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST);
            	} else if (mHandler.hasMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST)) {
            		return;
            	}
            	mLastChannelUpdateMsg = ((TvCallbackData)msg.obj).param2;
            	Message message = Message.obtain();
            	message.what = EPGConfig.EPG_UPDATE_CHANNEL_LIST;
            	message.arg1 = mLastChannelUpdateMsg;
            	sendMessageDelayed(message, 3000);
//            	sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_CHANNEL_LIST, 3000);
            	break;
            case EPGConfig.EPG_UPDATE_CHANNEL_LIST:
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_UPDATE_CHANNEL_LIST1111>" + mListViewAdpter + ">>" + msg.arg1);
            	if (mReader != null && mListViewAdpter != null && mListView != null) {
            		if (msg.arg1 == 8 && mListView.getList() != null && mListViewAdpter.getGroup() != null) {//modify
            			modifyChannelListWithThread();
            		} else {//add, delete
            			refreshChannelListWithThread();
            		}
            	}
            	break;
            case TvCallbackConst.MSG_CB_BANNER_MSG:
            	TvCallbackData specialMsgData = (TvCallbackData) msg.obj;
				if(BannerView.BANNER_MSG_NAV == specialMsgData.param1){
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in handleMessage BANNER_MSG_NAV value=== " + specialMsgData.param2);
					switch (specialMsgData.param2) {
//					case BannerView.SPECIAL_NO_SIGNAL:
					case BannerView.SPECIAL_CHANNEL_LOCK:
					case BannerView.SPECIAL_PROGRAM_LOCK:
					case BannerView.SPECIAL_INPUT_LOCK:
//					case BannerView.SPECIAL_NO_CHANNEL:
//					case BannerView.SPECIAL_RETRIVING:
//					case BannerView.SPECIAL_NO_SUPPORT:
//					case BannerView.SPECIAL_HIDDEN_CHANNEL:
//					case BannerView.SPECIAL_EMPTY_SPECIAFIED_CH_LIST:
						if (specialMsgData.param2 == BannerView.SPECIAL_INPUT_LOCK) {
							if (mReader.isTvSourceLock()) {
//								EPGManager.getInstance().startActivity(EPGCnActivity.this, com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
								finish();
//								TurnkeyUiMainActivity.setShowBannerInOnResume(false);
							}
						} else {
							isSpecialState = true;
							needFirstShowLock = false;
							mHandler.removeMessages(EPGConfig.EPG_SET_LOCK_ICON);
							mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_SET_LOCK_ICON, 2000);
						}
						break;
					case BannerView.SPECIAL_NO_AUDIO_VIDEO:
					case BannerView.SPECIAL_SCRAMBLED_VIDEO_NO_AUDIO:
					case BannerView.SPECIAL_SCRAMBLED_AUDIO_VIDEO:
					case BannerView.SPECIAL_SCRAMBLED_VIDEO_CLEAR_AUDIO:
					case BannerView.SPECIAL_SCRAMBLED_AUDIO_NO_VIDEO:
					case BannerView.SPECIAL_STATUS_AUDIO_ONLY:
					case BannerView.SPECIAL_STATUS_VIDEO_ONLY:
					case BannerView.SPECIAL_STATUS_AUDIO_VIDEO:
						isSpecialState = false;
						needFirstShowLock = false;
						if (mLockImageView.getVisibility() == View.VISIBLE) {
							mLockImageView.setVisibility(View.INVISIBLE);
						}
						if (mProgramNameTv.getVisibility() == View.VISIBLE && mProgramDetailTv.getVisibility() != View.VISIBLE) {
							mProgramDetailTv.setVisibility(View.VISIBLE);
							if (mCurrentSelectedProgramInfo != null) {
								setProgramDetailTvState(mCurrentSelectedProgramInfo);
							}
						}
						mHandler.removeMessages(EPGConfig.EPG_SET_LOCK_ICON);
						mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_SET_LOCK_ICON, 2000);
						break;
					default:
						break;
					}
				}
            	break;
            case EPGConfig.EPG_SET_LOCK_ICON:
				setLockIconVisibility(isSpecialState);
				break;
			default:
				break;
			}
		}

	};

	private BroadcastReceiver mUpdateEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!CommonIntegration.supportTIFFunction()) {  // this function is only for TIF
				return;
			}
			String actionName = intent.getAction();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "actionName>>>" + actionName);
			if (actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN)
					|| actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF)) { //actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF) ||
				int channelId = intent.getIntExtra("channel_id", 0);
				if (mListViewAdpter != null && mListViewAdpter.containsChannelId(channelId)) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "containsChannelId true find the channel id");
				} else {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "containsChannelId false find the channel id");
					return;
				}
//				EPGChannelInfo updateEPGChannel = null;
//				if (mGroup != null && channelId != 0) {
//					for (EPGChannelInfo tempEPGChannel:mGroup) {
//						if (tempEPGChannel.getTVChannel().getChannelId() == channelId) {
//							updateEPGChannel = tempEPGChannel;
//							break;
//						}
//					}
//				}
//				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "actionName>>>" + actionName + "    channelId>>" + channelId + "  updateEPGChannel>>" + updateEPGChannel);
//				if (actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF)) { //update PF channel event
//
//				} else 
        if (actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN)) {// update active window event
//					mListView.mCanChangeChannel = false;
					com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "AsyncTaskEventsLoad read mHandler.hasMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST)>>" + mHandler.hasMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST)
							+ ">>>" + mHandler.hasMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG)
							+ ">>>" + isGetingData + ">>>" + mListViewAdpter.isAlreadyGetAll());
					int delayTime = 1000;
					if (mListViewAdpter.isAlreadyGetAll()) {
						if (!mHandler.hasMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG)) {
							delayTime = 6000;
							mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG, delayTime);
						}
					} else {
						mListViewAdpter.addAlreadyChnnelId(channelId);
//						mListViewAdpter.putUpdateChannel(channelId);
						if (!mHandler.hasMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST)) {
							if (mDataRetrievingShow != null && mDataRetrievingShow.getVisibility() != View.VISIBLE) {
								mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
							}
							if (isGetingData) {
								delayTime = 3000;
							}
							mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_GET_TIF_EVENT_LIST, delayTime);
						}
					}
				}
			}
		}

	};

	private void updateProgramList() {
		if (!mNeedNowGetData) {
			EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_API_EVENT_LIST, 1000);
		}
	}

//	private void updateEvent(TvCallbackData data) {
//		if (mListViewAdpter != null) {
//			List<EPGChannelInfo> mGroup = mListViewAdpter.getGroup();
//			if (mGroup == null) {
//				return;
//			}
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateEvent()");
//			for (EPGChannelInfo tempChannelInfo:mGroup) {
//				List<EPGProgramInfo> tempInfoList = tempChannelInfo.getmTVProgramInfoList();
//				if (tempInfoList != null) {
//					for (EPGProgramInfo tempPrograminfo:tempInfoList) {
//						if (tempPrograminfo.getChannelId() == data.param2
//								&& tempPrograminfo.getProgramId() == data.param3) {
//							List<EPGProgramInfo> programList = mReader.getChannelProgramList(tempChannelInfo.getTVChannel(), mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour(),EPGConfig.mTimeSpan);
//							if (programList != null) {
//								for (EPGProgramInfo tempNewinfo:programList) {
//									if (tempNewinfo.getProgramId() == data.param3) {
//										int index = tempInfoList.indexOf(tempPrograminfo);
//										if (index != -1) {
//											tempInfoList.set(index, tempNewinfo);
//											showSelectedProgramInfo();
//											return;
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window mWindow = getWindow();
		if(null != mWindow){
			mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.epg_cn_main);
//		mLinearLayout = (LinearLayout) findViewById(R.id.epg_root_layout);
		if (!CommonIntegration.getInstance().isContextInit()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "init common integergration context");
			CommonIntegration.getInstance().setContext(this.getApplicationContext());
		}
		mHandlerThead = new HandlerThread(TAG);
    	mHandlerThead.start();
    	mThreadHandler = new Handler(mHandlerThead.getLooper());
		mReader = DataReader.getInstance(this);
		mReader.loadProgramType();
		mReader.loadMonthAndWeekRes();
		mMtkTvBanner = MtkTvBanner.getInstance();
		mEPGPwdDialog = new EPGPwdDialog(this);
		mEPGPwdDialog.setAttachView(findViewById(R.id.epg_content_layout));
		mEPGPwdDialog.setOnDismissListener(this);
		lastHourTime = EPGUtil.getCurrentDayHourMinute();
		initUI();
		mListViewAdpter.setStartHour(EPGUtil.getCurrentHour());
		changeTimeViewsShow(dayNum, mListViewAdpter.getStartHour());
//		TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_EVENT_NFY, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_CONFIG, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_BANNER_MSG, mHandler);
		registerUpdateReceiver();
		setCurrentDate();
		EPGConfig.init = true;
		EPGConfig.avoidFoucsChange = false;
		//new feature to send action.view when finish wizard
	}

	private Runnable mGetCurrentTimeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mListViewAdpter != null) {
				curTime = EPGUtil.getCurrentTime();
				if (curTime - lastTime < -60 * 10 || curTime - lastHourTime >= 60 * 60) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "=====curTime - last time: " + (curTime - lastHourTime) + "  " + EPGUtil.getCurrentHour());
					dayNum = 0;
					lastHourTime = EPGUtil.getCurrentDayHourMinute();
					if (curTime - lastTime < 0) {
						EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
					} else {
						EPGConfig.FROM_WHERE = EPGConfig.AVOID_PROGRAM_FOCUS_CHANGE;
					}
					if (mListViewAdpter == null) {
						return;
					}
					mListViewAdpter.setDayNum(dayNum);
					mListViewAdpter.setStartHour(EPGUtil.getCurrentHour());
					mNeedNowGetData = true;
					mCanChangeTimeShow = true;
					mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
					mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_API_EVENT_LIST, 1000);
				}
				lastTime = curTime;
				String mDate = EPGUtil.formatCurrentTimeWith24Hours();//tmCvt.getDetailDate(DataReader.getCurrentDate());
				Message msg = Message.obtain();
				msg.obj = mDate;
				msg.what = EPGUsManager.Message_Refresh_Time;
				mHandler.sendMessageDelayed(msg, 1000);
			}
		}
	};

	private Runnable mGetEventsRunnable = new Runnable() {

		@Override
		public void run() {
			mReader.readProgramInfoByTIF(mListViewAdpter.getGroup(), mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour());
//			mHandler.sendEmptyMessage(EPGConfig.EPG_SET_LIST_ADAPTER);
			mHandler.post(new Runnable() {
				public void run() {
					if (mListViewAdpter != null && mListView != null) {
						refreshUpdateLayout(mListViewAdpter.getGroup());
						showSelectedProgramInfo();
						mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
						mListView.mCanChangeChannel = true;
						isGetingData = false;
					}
				}
			});
		}
	};

	private Runnable mModifyChannelListRunnable = new Runnable() {

		@Override
		public void run() {
			ArrayList<EPGChannelInfo> mChannelList = null;
    		if (CommonIntegration.supportTIFFunction()) {
    			mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelListByTIF();
    		} else {
    			mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelList();
    		}
    		if (mListView != null && mListView.getList() != null && mChannelList != null && mListViewAdpter != null) {
    			mReader.updateChannList((List<EPGChannelInfo>)mListView.getList(), mChannelList);
    			EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
    			EPGCnActivity.this.runOnUiThread(new Runnable() {

    				@Override
    				public void run() {
    					if (mListViewAdpter != null) {
    						mListViewAdpter.notifyDataSetChanged();
    					}
    				}
    			});
    		}
		}
	};

	private Runnable mRefreshChannelListRunnable = new Runnable() {

		@Override
		public void run() {
			ArrayList<EPGChannelInfo> mChannelList = null;
			if (CommonIntegration.supportTIFFunction()) {
				mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelListByTIF();
			} else {
				mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelList();
			}
			if (mChannelList != null && !mChannelList.isEmpty()) {
				int index = 0;
				if (mListView != null) {
					EPGChannelInfo currentChannel = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
					if (currentChannel != null && mReader.isChannelExit(currentChannel.getTVChannel().getChannelId())) {
						index = mReader.getChannelPosition(currentChannel.getTVChannel());
					} else {
						index = mReader.getCurrentPlayChannelPosition();
					}
				} else {
					index = mReader.getCurrentPlayChannelPosition();
				}
				int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
				com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "re setAdapter.>index>>>" + index + "   pageNum>>" + pageNum);
				Message msg = Message.obtain();
				msg.arg1 = index;
				msg.arg2 = pageNum;
				msg.obj = mChannelList;
				msg.what = EPGConfig.EPG_REFRESH_CHANNEL_LIST;
				mHandler.sendMessage(msg);
			}
		}
	};

	private void registerUpdateReceiver() {
		IntentFilter updateIntentFilter = new IntentFilter();
		updateIntentFilter.addAction(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF);
		updateIntentFilter.addAction(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN);
        this.registerReceiver(mUpdateEventReceiver, updateIntentFilter);
	}

	private void initUI() {
		//mEPGPasswordEditText = (EPGPasswordEditText) findViewById(R.id.epg_password);
		mDataRetrievingShow = (TextView) findViewById(R.id.epg_retrieving_data);
		mCurrentDateTv = (TextView) findViewById(R.id.epg_top_date_info_tv);
		mSelectedDateTv = (TextView) findViewById(R.id.epg_title_date_selected_tv);
		mBeginTimeTv = (TextView) findViewById(R.id.epg_title_time_begin_tv);
		mEndTimeTv = (TextView) findViewById(R.id.epg_title_time_end_tv);
		mProgramNameTv = (TextView) findViewById(R.id.epg_program_info_name);
		mProgramTimeTv = (TextView) findViewById(R.id.epg_program_info_time);
		mProgramDetailTv = (TextView) findViewById(R.id.epg_program_info_detail);
		mProgramDetailTv.setLines(PER_PAGE_LINE);
		mProgramType = (TextView) findViewById(R.id.epg_program_info_type);
		mProgramRating = (TextView) findViewById(R.id.epg_program_rating);
		mPageInfoTv = (TextView) findViewById(R.id.epg_info_page_tv);
		mPrevDayTv = (TextView) findViewById(R.id.epg_bottom_prev_day_tv);
		mNextDayTv = (TextView) findViewById(R.id.epg_bottom_next_day_tv);
		mViewDetailTv = (TextView) findViewById(R.id.epg_bottom_view_detail);
		mTypeFilter = (TextView) findViewById(R.id.epg_bottom_view_filter);

		mLockImageView = (ImageView) findViewById(R.id.epg_info_lock_icon);
		mSttlImageView = (ImageView) findViewById(R.id.epg_info_sttl_icon);

		LinearLayout mRootLayout = (LinearLayout) findViewById(R.id.epg_root_layout);
		LinearLayout mListViewLayout = (LinearLayout) findViewById(R.id.epg_listview_layout);
		mListView = (EPGListView) findViewById(R.id.epg_program_forecast_listview);
		mListView.setHandler(mHandler);

		mListViewAdpter = new EPGListViewAdapter(this, EPGUtil.getCurrentHour());
		mProgramDetailTv.setMovementMethod(ScrollingMovementMethod.getInstance());
//		mListViewAdpter.setHandler(mHandler);
		mMyUpData = new MyUpdata();
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		display.getMetrics(mDisplayMetrics);
		int width = (int) ((display.getWidth() - 46 * mDisplayMetrics.density) * 0.75f) - 2;
		mListViewAdpter.setWidth(width);
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "List View Program Info Total Width----------------->" + width);
		mRootLayout.requestFocus();
		mListViewLayout.requestFocus();
		if (!mListView.hasFocus()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "[ListView has not focus]");
			mListView.requestFocus();
			mListView.setSelection(0);
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "[ListView has  focus]");
		}
		if (mListView.hasFocus()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "[The second time, ListView has focus]");
		}
	}

	private void modifyChannelListWithThread() {
		if (mThreadHandler != null && mModifyChannelListRunnable != null) {
			mThreadHandler.post(mModifyChannelListRunnable);
		}
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				ArrayList<EPGChannelInfo> mChannelList = null;
//        		if (CommonIntegration.supportTIFFunction()) {
//        			mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelListByTIF();
//        		} else {
//        			mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelList();
//        		}
//        		if (mListView != null && mListView.getList() != null && mChannelList != null && mListViewAdpter != null) {
//        			mReader.updateChannList((List<EPGChannelInfo>)mListView.getList(), mChannelList);
//        			EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
//        			EPGCnActivity.this.runOnUiThread(new Runnable() {
//
//        				@Override
//        				public void run() {
//        					if (mListViewAdpter != null) {
//        						mListViewAdpter.notifyDataSetChanged();
//        					}
//        				}
//        			});
//        		}
//			}
//		}).start();
	}

	private void refreshChannelListWithThread() {
		if (mThreadHandler != null && mRefreshChannelListRunnable != null) {
			mThreadHandler.post(mRefreshChannelListRunnable);
		}
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				ArrayList<EPGChannelInfo> mChannelList = null;
//				if (CommonIntegration.supportTIFFunction()) {
//					mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelListByTIF();
//				} else {
//					mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelList();
//				}
//				if (mChannelList != null && mChannelList.size() > 0) {
//					int index = 0;
//					if (mListView != null) {
//						EPGChannelInfo currentChannel = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
//						if (currentChannel != null && mReader.isChannelExit(currentChannel.getTVChannel().getChannelId())) {
//							index = mReader.getChannelPosition(currentChannel.getTVChannel());
//						} else {
//							index = mReader.getCurrentPlayChannelPosition();
//						}
//					} else {
//						index = mReader.getCurrentPlayChannelPosition();
//					}
//					int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
//					com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "re setAdapter.>index>>>" + index + "   pageNum>>" + pageNum);
//					Message msg = Message.obtain();
//					msg.arg1 = index;
//					msg.arg2 = pageNum;
//					msg.obj = mChannelList;
//					msg.what = EPGConfig.EPG_REFRESH_CHANNEL_LIST;
//					mHandler.sendMessage(msg);
//				}
//			}
//		}).start();
	}

	private void getChannelListWithThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<EPGChannelInfo> mChannelList = null;
				if (CommonIntegration.supportTIFFunction()) {
					mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelListByTIF();
				} else {
					mChannelList = (ArrayList<EPGChannelInfo>) mReader.getAllChannelList();
				}
				if (mChannelList != null && !mChannelList.isEmpty()) {
					int index = mReader.getCurrentPlayChannelPosition();
					int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
					Message msg = Message.obtain();
					msg.arg1 = index;
					msg.arg2 = pageNum;
					msg.obj = mChannelList;
					msg.what = EPGConfig.EPG_INIT_EVENT_LIST;
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();

		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onResume mListViewAdpter>>" + mListViewAdpter);
		if (mListViewAdpter != null && mListViewAdpter.getGroup() == null) {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume goto getChannelListWithThread~~");
			getChannelListWithThread();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"EPG on Pause");
	}

	@Override
	protected void onStop() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"EPG on onStop");
		super.onStop();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestroy()");
        clearData();
    }

	class MyUpdata implements EPGListView.UpDateListView {
		@SuppressWarnings("unchecked")
		public void updata(final boolean isNext) {
			if (mHasInitListView) {
				mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
				mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
				mListViewAdpter.clearWindowList();
				mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
				mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
				if (eventLoad != null) {
					eventLoad.cancel(true);
					eventLoad = null;
				}
				isGetingData = false;
				List<EPGChannelInfo> mGroup = (List<EPGChannelInfo>) mListView.getCurrentList();
				if (isNext) {
					EPGConfig.SELECTED_CHANNEL_POSITION = 0;
				} else {
					EPGConfig.SELECTED_CHANNEL_POSITION = mGroup.size() - 1;
				}
				mListViewAdpter.setGroup(mGroup);
				mListView.setAdapter(mListViewAdpter);
				mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
			}
		}
	}

	private void updateAdapter(List<?> adpter, int pageNum) {
		mListView.initData(adpter, DataReader.PER_PAGE_CHANNEL_NUMBER, mMyUpData, pageNum);
		List<EPGChannelInfo> mGroup = (List<EPGChannelInfo>) mListView.getCurrentList();
		mListViewAdpter.setGroup(mGroup);
		mListView.updateEnablePosition(mListViewAdpter);
	}

	private void setAdapter(List<?> adpter, int pageNum) {
		if (mListView != null && mListViewAdpter != null) {
			mListView.initData(adpter, DataReader.PER_PAGE_CHANNEL_NUMBER, mMyUpData, pageNum);
			mHasInitListView = true;
			List<EPGChannelInfo> mGroup = (List<EPGChannelInfo>) mListView.getCurrentList();
			mListViewAdpter.setGroup(mGroup);
			mListView.setAdapter(mListViewAdpter);
		}
	}

	/**
	 * set the current date in the top
	 */
	private void setCurrentDate() {
        if (CommonIntegration.getInstance().isCurrentSourceATV()) {
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
		TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST,mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE,mHandler);
		if (mHandler != null) {
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST);
		}
		EPGLinearLayout.mCurrentSelectPosition = 0;
//		TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_EVENT_NFY, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_CONFIG, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_BANNER_MSG, mHandler);
		if (mUpdateEventReceiver != null) {
			unregisterReceiver(mUpdateEventReceiver);
			mUpdateEventReceiver = null;
		}
		if (mListViewAdpter != null) {
			mListViewAdpter.clearWindowList();
		}
		if (mHandler!= null) {
            mHandler.removeMessages(EPGUsManager.Message_Refresh_Time);
            mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
			mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
			mHandler.removeMessages(EPGConfig.EPG_SET_LIST_ADAPTER);
			mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.removeCallbacksAndMessages(null);
        }
		if (mThreadHandler != null) {
			mThreadHandler.removeCallbacks(mGetEventsRunnable);
			mThreadHandler.removeCallbacks(mGetCurrentTimeRunnable);
			mThreadHandler.removeCallbacks(mModifyChannelListRunnable);
			mThreadHandler.removeCallbacks(mRefreshChannelListRunnable);
			mThreadHandler.removeCallbacksAndMessages(null);
			mHandlerThead.quit();
			mGetEventsRunnable = null;
			mGetCurrentTimeRunnable = null;
			mModifyChannelListRunnable = null;
			mRefreshChannelListRunnable = null;
			mThreadHandler = null;
		}
		if (eventLoad != null) {
			eventLoad.cancel(true);
			eventLoad = null;
		}
		MtkTvEvent.getInstance().clearActiveWindows();
		if (mListViewAdpter != null) {
			mListViewAdpter = null;
		}
		if (mListView != null) {
			mListView = null;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event.getRepeatCount()>>>" + event.getRepeatCount());
		switch (keyCode) {
		case KeyMap.KEYCODE_BACK:
		case KeyMap.KEYCODE_MTKIR_GUIDE:
			if (event.getRepeatCount() <= 0) {
//				EPGManager.getInstance().startActivity(this, com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
				finish();
			}
//			TurnkeyUiMainActivity.setShowBannerInOnResume(false);
			return true;
		case KeyMap.KEYCODE_MENU:
//			if (event.getRepeatCount() <= 0) {
//				clearData();
//				mHandler.postDelayed(new Runnable() {
//
//					@Override
//					public void run() {
//						EPGManager.getInstance().startActivity(TurnkeyUiMainActivity.getInstance(), MenuMain.class, 0);
//						EPGCnActivity.this.finish();
//					}
//				}, 500);
//			}
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
			EpgType mEpgType = new EpgType(this);
			mEpgType.show();
			return true;
		case KeyMap.KEYCODE_MTKIR_GREEN:
		case KeyEvent.KEYCODE_G:
			dayNum = mListViewAdpter.getDayNum();
			if (dayNum == EPGConfig.mMaxDayNum) {
				return false;
			}
			EPGConfig.init = false;
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_MTKIR_RED;
			dayNum = dayNum + 1;
			mListViewAdpter.setDayNum(dayNum);
			mListViewAdpter.setStartHour(0);
			mCanChangeTimeShow = true;
			mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_API_EVENT_LIST, 500);
			return true;
		case KeyMap.KEYCODE_MTKIR_RED:
		case KeyEvent.KEYCODE_R:
			dayNum = mListViewAdpter.getDayNum();
			if (dayNum == 0) {
				return false;
			}
			EPGConfig.init = false;
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_MTKIR_GREEN;
			dayNum = dayNum - 1;
			mListViewAdpter.setDayNum(dayNum);
			if (dayNum == 0) {
				mListViewAdpter.setStartHour(EPGUtil.getCurrentHour());
			} else {
				mListViewAdpter.setStartHour(0);
			}
			mCanChangeTimeShow = true;
			mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_API_EVENT_LIST, 500);
			return true;
		case KeyMap.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_E:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_CENTER");
			mListViewSelectedChild = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
			if (mListViewSelectedChild != null) {
				// If current channel is locked
				int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
		    	if (showFlag == 0) {
		    		mEPGPwdDialog.show();
		    		mEPGPwdDialog.sendAutoDismissMessage();
		    		changeBottomViewText(true, keyCode);
					setProgramInfoViewsInVisiable();
					mLockImageView.setVisibility(View.INVISIBLE);
		    	}
			}
			return true;
	    case KeyMap.KEYCODE_MTKIR_EJECT:
	    case KeyMap.KEYCODE_MTKIR_RECORD:
	    	if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
	    		calledByScheduleList();
			}else {
				Toast.makeText(getApplicationContext(),
						"Source not available,please change to DTV source", Toast.LENGTH_SHORT).show();
			}
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
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void changeTimeViewsShow(int dayNum, int startHour) {
//		Date now = DataReader.getCurrentDate();
		Long time = 24 * 60 * 60L * dayNum;
//		Date day = new Date(now.getTime() + time);
//		Date day = new Date(EPGUtil.getCurrentTime() + time);
		String dateStr =  EPGUtil.getSimpleDate(EPGUtil.getCurrentTime() + time);
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
		int mBeginTime = EPGUtil.getEUIntervalHour(dayNum, startHour,0) % 24;
		mBeginTimeTv.setText(String.format("%d:00", mBeginTime));
		int mEndTime = EPGUtil.getEUIntervalHour(dayNum, startHour,1) % 24;
		mEndTimeTv.setText(String.format("%d:00", mEndTime));
	}

	private void updateEventList(boolean needSetActiveWindow) {
		if (mListViewAdpter != null) {
			mListView.mCanChangeChannel = false;
			mNeedNowGetData = false;
			if (needSetActiveWindow) {
				mListViewAdpter.setActiveWindow();
			} else if (!CommonIntegration.supportTIFFunction()) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "5start read event from api>" + mListViewAdpter.getDayNum() + "   " + mListViewAdpter.getStartHour());
						mReader.readChannelProgramInfoByTime(mListViewAdpter.getGroup(), mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour(), EPGConfig.mTimeSpan);
//						mHandler.sendEmptyMessage(EPGConfig.EPG_NOTIFY_LIST_ADAPTER);
						mHandler.removeMessages(EPGConfig.EPG_SET_LIST_ADAPTER);
						mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_SET_LIST_ADAPTER, 100);
					}
				}).start();
			}
		}
	}

	private void setLockIconVisibility(boolean isVisible) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockIconVisibility>>>" + isVisible);
		if (isVisible) {
			if (mEPGPwdDialog != null && mEPGPwdDialog.isShowing()) {
				mLockImageView.setVisibility(View.INVISIBLE);
			} else {
				mLockImageView.setVisibility(View.VISIBLE);
			}
			mSttlImageView.setVisibility(View.INVISIBLE);
			if (mProgramDetailTv.getVisibility() == View.VISIBLE) {
				mProgramDetailTv.setVisibility(View.INVISIBLE);
			}
		} else {
			if (mLockImageView.getVisibility() == View.VISIBLE) {
				mLockImageView.setVisibility(View.INVISIBLE);
			}
			mHandler.removeMessages(EPGConfig.EPG_PROGRAM_STTL_SHOW);
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAM_STTL_SHOW, 1000);
		}
	}

	private void showSelectedProgramInfo() {
		if (mListViewAdpter != null) {
			mListViewSelectedChild = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
			if (mListViewSelectedChild != null) {
				EPGLinearLayout childView = mListView.getSelectedDynamicLinearLayout(EPGConfig.SELECTED_CHANNEL_POSITION);
				List<EPGProgramInfo> mProgramList = mListViewSelectedChild.getmTVProgramInfoList();
                if(mEPGPwdDialog !=null && mEPGPwdDialog.isShowing()){
                    mEPGPwdDialog.dismiss();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do dismiss pwd dialog!");//fix cr:DTV00705729
                }
				if (needFirstShowLock) {
					setLockImageViewState(mListViewSelectedChild.getTVChannel());
				}
				if (mProgramList != null && childView != null
						&& !mProgramList.isEmpty() && childView.getmCurrentSelectPosition() < mProgramList.size()
						&& childView.getmCurrentSelectPosition() >= 0) {
					mCurrentSelectedProgramInfo = mProgramList.get(childView.getmCurrentSelectPosition());
					if (mCurrentSelectedProgramInfo != null) {
						mProgramNameTv.setVisibility(View.VISIBLE);
						mProgramNameTv.setText(mCurrentSelectedProgramInfo.getTitle());
						mProgramTimeTv.setVisibility(View.VISIBLE);
						int timeType= EPGUtil.judgeFormatTime(this);
						mProgramTimeTv.setText(EPGTimeConvert.getInstance().formatProgramTimeInfo(mCurrentSelectedProgramInfo,timeType));
						setProgramDetailTvState(mCurrentSelectedProgramInfo);
//						if (mIsCountryUK && mCurrentSelectedProgramInfo.getMainType() > mReader.getMainType().length) {
//							mProgramType.setText(getString(R.string.nav_epg_not_support));
//						} else
							if(mCurrentSelectedProgramInfo.getMainType() >=1) {
							mProgramType.setText(mReader.getMainType()[mCurrentSelectedProgramInfo.getMainType()-1]);
						}else{
							mProgramType.setText(getString(R.string.epg_info_program_type));
						}
						mProgramRating.setVisibility(View.VISIBLE);
						mProgramRating.setText(mCurrentSelectedProgramInfo.getRatingType());
//						int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
						if (!isSpecialState) {
							setSubTitleImageViewState(mCurrentSelectedProgramInfo);
						} else if (mSttlImageView.getVisibility() != View.INVISIBLE) {
							mSttlImageView.setVisibility(View.INVISIBLE);
						}
						if (mEPGPwdDialog != null && mEPGPwdDialog.isShowing()) {
							setProgramInfoViewsInVisiable();
						}
					} else {
						setProgramInfoViewsInVisiable();
					}
				} else if (childView == null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "childView == null this.isFinishing():" + this.isFinishing());
					if (!this.isFinishing()) {
						mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
						mHandler.sendEmptyMessage(EPGConfig.EPG_PROGRAMINFO_SHOW);
					}
				} else {
					setProgramInfoViewsInVisiable();
				}
			} else {
				setProgramInfoViewsInVisiable();
			}
		} else {
			setProgramInfoViewsInVisiable();
		}
	}

	private void setProgramInfoViewsInVisiable() {
		mProgramNameTv.setVisibility(View.INVISIBLE);
		mProgramTimeTv.setVisibility(View.INVISIBLE);
		mProgramDetailTv.setVisibility(View.INVISIBLE);
		mSttlImageView.setVisibility(View.INVISIBLE);
		mProgramRating.setVisibility(View.INVISIBLE);
		mPageInfoTv.setText("");
		mViewDetailTv.setText("");
		mProgramType.setText("");
		mProgramRating.setText("");
	}

	private void setLockImageViewState(MtkTvChannelInfoBase mChannel) {
		if (mChannel != null) {
			int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
	    	if (showFlag == 0) {
	    		isSpecialState = true;
	    		mLockImageView.setVisibility(View.VISIBLE);
	    	} else {
	    		isSpecialState = false;
	    		mLockImageView.setVisibility(View.INVISIBLE);
	    	}
		}
	}

	/**
	 * get caption icon display state
	 *
	 * @return
	 */
	private boolean isShowSTTLIcon() {
		boolean showCaptionIcon = mMtkTvBanner.isDisplayCaptionIcon();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowCaptionIcon, value == " + showCaptionIcon);
		return showCaptionIcon;
	}

	private void setSubTitleImageViewState(EPGProgramInfo childProgramInfo) {
		if (childProgramInfo != null && mListView != null && mListView.getCurrentChannel() != null) {
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSubTitleImageViewState== " + childProgramInfo.isHasSubTitle() + "  " + mMtkTvBanner.isDisplayCaptionIcon());
			final Long time = EPGUtil.getCurrentTime();
			List<EPGProgramInfo> mChildViewData = mListView.getCurrentChannel().getmTVProgramInfoList();
			if (mChildViewData != null) {
				boolean hasFind = false;
				for (EPGProgramInfo tempEpgProgram:mChildViewData) {
					if (tempEpgProgram.getChannelId() == childProgramInfo.getChannelId()
							&& tempEpgProgram.getProgramId() == childProgramInfo.getProgramId()) {
						hasFind = true;
						break;
					}
				}
				if (hasFind) {
					if (time < childProgramInfo.getmStartTime()
							|| time > childProgramInfo.getmEndTime()) {  //check is outof current time
						if (childProgramInfo.isHasSubTitle()) {
							mSttlImageView.setVisibility(View.VISIBLE);
						} else {
							mSttlImageView.setVisibility(View.INVISIBLE);
						}
					} else if (isShowSTTLIcon()) {                  //check whether show ttx icon
						mSttlImageView.setVisibility(View.VISIBLE);
					} else {
						mSttlImageView.setVisibility(View.INVISIBLE);
					}
				} else {
					mSttlImageView.setVisibility(View.INVISIBLE);
				}
			} else {
				mSttlImageView.setVisibility(View.INVISIBLE);
			}
		} else {
			mSttlImageView.setVisibility(View.INVISIBLE);
		}
	}

	private void setProgramDetailTvState(EPGProgramInfo childProgramInfo) {
		int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setProgramDetailTvState>>" + isSpecialState + "   >>" + showFlag);
    if (showFlag == 0 && mLockImageView.getVisibility() != View.VISIBLE) {
      setLockIconVisibility(true);
    }
		if (showFlag == 0 || isSpecialState/* && !mChannel.isUsrUnblocked()*/) {
			// if current channel is locked, hide program detail info
			mProgramDetailTv.setVisibility(View.INVISIBLE);
			mViewDetailTv.setText("");
			mPageInfoTv.setText("");
		} else if (childProgramInfo != null
				&& childProgramInfo.getDescribe() != null) {
			mProgramDetailTv.setVisibility(View.VISIBLE);
			String mDetailContent = childProgramInfo.getDescribe();
			mProgramDetailTv.setText(mDetailContent);
            if(TextUtils.isEmpty(mDetailContent)){
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
			mTotalPage = (line % PER_PAGE_LINE == 0) ? line / PER_PAGE_LINE : line / PER_PAGE_LINE + 1;
			mCurrentPage = 1;
			mProgramDetailTv.scrollTo(0, (mCurrentPage - 1) * PER_PAGE_LINE
					* mProgramDetailTv.getLineHeight());
			if (mTotalPage > 1) {
				mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
				mViewDetailTv.setText(getResources().getString(R.string.epg_bottom_view_detail));
			} else{
                mViewDetailTv.setText("");
                mPageInfoTv.setText("");
			}
		} else {
			mHandler.postDelayed(new Runnable() {
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
				mTypeFilter.setText(getResources().getString(R.string.setup_exit));
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
			mTypeFilter.setText(getResources().getString(R.string.epg_bottom_type_filter));
		}
	}

	private void savePreValues() {
		preValues[0] = mPrevDayTv.getText().toString();
		preValues[1] = mNextDayTv.getText().toString();
		preValues[2] = mViewDetailTv.getText().toString();
	}

	public void setIsNeedFirstShowLock(boolean isNeedFirstShowLock) {
		needFirstShowLock = isNeedFirstShowLock;
	}

	public void onDismiss(DialogInterface dialog) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWD onDismiss!!>>" + needFirstShowLock);
		showSelectedProgramInfo();
		changeBottomViewText(false, 0);
	}

	private void refreshUpdateLayout(List<EPGChannelInfo> channels) {
		for (EPGChannelInfo channel:channels) {
			if (mListViewAdpter == null || mListView == null) {
				return;
			}
			int index = mListViewAdpter.getIndexOfChannel(channel);
			if (index >= 0 && index < mListView.getChildCount()) {
				EPGLinearLayout childView = mListView.getSelectedDynamicLinearLayout(index);
				if (childView != null) {
					childView.refreshEventsLayout(channel, channel.getmTVProgramInfoList(), index);
				}
			}
		}
	}

    public void notifyEPGLinearlayoutRefresh() {
		//update for set selected program type color in EPGLinearLayout.setAdpter(..).
    	for (int i = 0; i < mListView.getChildCount(); i++) {
			EPGLinearLayout childView = mListView.getSelectedDynamicLinearLayout(i);
			if (childView != null) {
				childView.refreshTextLayout(i);
			}
		}
		if (mListViewSelectedChild != null) {
			setProgramDetailTvState(mCurrentSelectedProgramInfo);
		}
	}

	   public void calledByScheduleList(){
	        //setResult(TurnkeyUiMainActivity.RESULT_CODE_ADD_SCHEDULE_ITEM_FROM_EPG);
		   EPGChannelInfo selectedChild = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
	    	if(selectedChild!=null){
		    	List<EPGProgramInfo> programInfos = selectedChild.getmGroup();
		    	if( programInfos == null || programInfos.isEmpty()){
		    		return ;
		    	}else if(programInfos.size()==1){
		    		if(programInfos.get(0).getmStartTime()==0){
		    			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"programInfos.size()==1 =and starttime="+programInfos.get(0).getmStartTime());
		    			return ;
		    		}
		    	}else {
		    		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"programInfos.size() >1 =="+programInfos.size());
		    	}
			} else{
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectedChild=null");
				return;
			}
	    	MtkTvBookingBase item = new MtkTvBookingBase();

	    	Long startTime=EPGTimeConvert.getInstance().getStartTime(mCurrentSelectedProgramInfo);
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime=" + startTime);
	        if (startTime != -1L) {
	            item.setRecordStartTime(startTime);
	        }

	        Long endTime=EPGTimeConvert.getInstance().getEndTime(mCurrentSelectedProgramInfo);
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "endTime=" + endTime);
	        if (endTime != -1L) {
	            item.setRecordDuration(endTime / 1000 - startTime / 1000);
	        }
		   item.setRepeatMode(128);
		   item.setChannelId(selectedChild.getTVChannel().getChannelId());
		   item.setTunerType(CommonIntegration.getInstance().getTunerMode());
		   item.setEventId(mCurrentSelectedProgramInfo.getProgramId());
		   item.setType(1);
		   ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(this, item);
		   scheduleListItemDialog.setEventType(1);
		   scheduleListItemDialog.setEventId(mCurrentSelectedProgramInfo.getProgramId());
		   scheduleListItemDialog.show();

//	    	Intent intent=new Intent();
//	        ComponentName comp = new ComponentName("com.mediatek.wwtv.setting","com.mediatek.wwtv.setting.menu.MenuSetUpActivity");
//	        intent.setComponent(comp);
//	        intent.putExtra("start_schedule",true);
//	        if(mCurrentSelectedProgramInfo!=null){
//	        	Long startTime=EPGTimeConvert.getInstance().getStartTime(mCurrentSelectedProgramInfo);
//	        	Long endTime=EPGTimeConvert.getInstance().getEndTime(mCurrentSelectedProgramInfo);
//
//	        	intent.putExtra(ScheduleItem.START_TIME,startTime);// mCurrentSelectedProgramInfo.getmStartTime().getTime());
//	            intent.putExtra(ScheduleItem.END_TIME,endTime);// mCurrentSelectedProgramInfo.getmEndTime().getTime());
//
//	            com.mediatek.wwtv.tvcenter.util.MtkLog.d("Timeshift_PVR", "calledByScheduleList()Start::"+startTime);
//	            com.mediatek.wwtv.tvcenter.util.MtkLog.d("Timeshift_PVR", "calledByScheduleList()End::"+endTime);
//	        }else
//	        {
//	        	intent.putExtra(ScheduleItem.START_TIME,System.currentTimeMillis());// mCurrentSelectedProgramInfo.getmStartTime().getTime());
//	            intent.putExtra(ScheduleItem.END_TIME,System.currentTimeMillis()+60*60*1000L);// mCurrentSelectedProgramInfo.getmEndTime().getTime());
//	        }
//
////	        setResult(TurnkeyUiMainActivity.RESULT_CODE_ADD_SCHEDULE_ITEM_FROM_EPG, intent);
//	        startActivity(intent);
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Timeshift_PVR", "calledByScheduleList()");
//	        this.finish();
//	        TurnkeyUiMainActivity.setShowBannerInOnResume(false);
	    }

	   private void loadEvents() {
		   if (mThreadHandler != null && mGetEventsRunnable != null) {
			   mThreadHandler.post(mGetEventsRunnable);
		   }
//		   if (eventLoad == null) {
//			   eventLoad = new AsyncTaskEventsLoad();
//			   eventLoad.execute(new String());
//			   return;
//		   }
//		   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad eventLoad.getStatus()>>>" + eventLoad.getStatus());
//		   switch (eventLoad.getStatus()) {
//			case FINISHED:
//				eventLoad = new AsyncTaskEventsLoad();
//				eventLoad.execute(new String());
//				break;
//			case PENDING:
//				eventLoad = new AsyncTaskEventsLoad();
//				eventLoad.execute(new String());
//				break;
//			case RUNNING:
//
//				break;
//
//			default:
//				break;
//			}
		}

	   /**
	    * Async channel events
	    * @author sin_xinsheng
	    *
	    */
	   public class AsyncTaskEventsLoad extends AsyncTask<String, Integer, List<EPGChannelInfo>> {

		@Override
		protected List<EPGChannelInfo> doInBackground(String... params) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad doInBackground:");
			return mReader.readProgramInfoByTIF(mListViewAdpter.getGroup(), mListViewAdpter.getDayNum(), mListViewAdpter.getStartHour());
		}

		@Override
		protected void onCancelled() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad onCancelled:");
			super.onCancelled();
		}

		@Override
		protected void onCancelled(List<EPGChannelInfo> result) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad onCancelled:" + result);
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(List<EPGChannelInfo> result) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad onPostExecute:" + result);
			refreshUpdateLayout(result);
			showSelectedProgramInfo();
			mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
			mListView.mCanChangeChannel = true;
			isGetingData = false;
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad onPreExecute:");
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AsyncTaskEventsLoad onProgressUpdate:" + values);
			super.onProgressUpdate(values);
		}

	   }

}
