package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import android.os.PowerManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvEvent;
import com.mediatek.twoworlds.tv.MtkTvEventBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvBanner;
import com.mediatek.twoworlds.tv.common.MtkTvIntent;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigMsgBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsManager;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.epg.DigitTurnCHView;
import com.mediatek.wwtv.tvcenter.epg.DigitTurnCHView.OnDigitTurnCHCallback;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckCallback;

public class EPGEuActivity extends BaseActivity implements
		ComponentStatusListener.ICStatusListener,
		ScheduleListDialog.NotifyUpDateEPGPvrMarkListener{

	private static final String TAG = "EPGEuActivity";
	public static boolean mIsEpgChannelChange;
	// private static final String BARKER_CHANNEL_PRESERENCE = "BarkerChannel";
	// private static final String IS_BARKER_CHANNEL = "isBarkerChannel";
  private static final int PER_PAGE_LINE = 4;
//	private static final int CHANGE_CHANNEL_TIME_OUT = 3000;
	private EPGListView mListView;
	private DataReader mReader;
	private MtkTvBanner mMtkTvBanner;

	private EPGListViewAdapter mListViewAdpter;
	private MyUpdata mMyUpData;
	private EPGChannelInfo mListViewSelectedChild;
	private int dayNum = 0;
	private DigitTurnCHView mDigitTurnCHView;
	private TextView mDataRetrievingShow; // data retrieving show
	private TextView mCurrentDateTv; // current date
  private ImageView leftArrow;
  private ImageView rightArrow;
	private TextView mSelectedDateTv; // selected date
	private TextView mBeginTimeTv; // begin time
	private TextView mEndTimeTv; // end time
  private LinearLayout mListViewTimeAxisRootLayout;
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
	private int mTotalPage; // total page
	private int mCurrentPage = 1; // current page
	private EPGProgramInfo mCurrentSelectedProgramInfo;
	// private EPGPasswordEditText mEPGPasswordEditText;
	private final String[] preValues = new String[3];

	private long lastTime = 0L;
	private long lastHourTime = 0L;
	private long curTime = 0L;
//	private Rect mEPGActivityRect;
	// public boolean mIsChangeChannel;
	private boolean mHasInitListView;
	private boolean mNeedNowGetData;
	private boolean mCanChangeTimeShow;
	private boolean isGetingData;
    private CableOperator operator;
    private int tunerMode;
	// private SharedPreferences mSharedPreferences;
	// private Editor mEditor;
	// private boolean mCanKeyUpToExit, mMenuKeyEixt, mMenuKeyUpProcess;
	private int mLastChannelUpdateMsg;
	private boolean mIs3rdTVSource;
	private EPGEuHelper mEPGEuHelper = new EPGEuHelper();
	private HandlerThread mHandlerThead;
	private Handler mThreadHandler;
	private final Bundle mBundle = new Bundle();
    private PinDialogFragment pinDialog;
	private CommonIntegration mIntegration = null;
	private boolean needRefreshList = false;
	private boolean hideTimeAxis = false;
	private EPGChannelInfo targetChannelInfo = null;
	private String country = "";

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGEuHandleMessage>>msg.what=" + msg.what
					+ ",msg.arg1=" + msg.arg1 + ",msg.arg2=" + msg.arg2);
			switch (msg.what) {
				case EPGConfig.EPG_NEED_UPDATE_LOCAL_DAYNUM:
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGEuHandleMessage 1000>> dayNum= " + dayNum);
					if(dayNum > 0){
						dayNum = dayNum - 1;
					}
					break;
                case EPGConfig.EPG_PWD_TIMEOUT_DISMISS:
                    pinDialog.dismiss();
                    break;
		         case EPGConfig.EPG_REFRESH_PVR_MARK:
		                refreshPvrMarks();
		                break;
        case EPGConfig.EPG_NEED_FINISH_EPG:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_NEED_FINISH_EPG~");
          finish();
          break;
			case EPGConfig.EPG_CHANGING_CHANNEL:
				targetChannelInfo = (EPGChannelInfo) msg.obj;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_CHANGING_CHANNEL>>channelInfo="
						+ targetChannelInfo);
				turnChannel(true);
				break;
			case EPGConfig.EPG_INIT_EVENT_LIST:
				if (mListViewAdpter != null && mListView != null) {
					setAdapter((List<EPGChannelInfo>) msg.obj, 0, msg.arg2);
					changeTimeViewsShow(dayNum, mListViewAdpter.getStartHour());
					EPGConfig.SELECTED_CHANNEL_POSITION = msg.arg1
							% DataReader.PER_PAGE_CHANNEL_NUMBER;
					mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
				}
				break;
			case EPGConfig.EPG_REFRESH_CHANNEL_LIST:
				if (mListViewAdpter != null && mListView != null) {
					updateAdapter((List<EPGChannelInfo>) msg.obj, 0, msg.arg2);
					changeTimeViewsShow(dayNum, mListViewAdpter.getStartHour());
					EPGConfig.SELECTED_CHANNEL_POSITION = msg.arg1
							% DataReader.PER_PAGE_CHANNEL_NUMBER;
					mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
				}
				break;
			case EPGConfig.EPG_SYNCHRONIZATION_MESSAGE:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_SYNCHRONIZATION_MESSAGE>>"
						+ msg.arg1 + "  " + msg.arg2);
				int dayNum = msg.arg1;
				int mStartTime = msg.arg2;
				mListViewTimeAxisRootLayout.setVisibility(View.INVISIBLE);
				changeTimeViewsShow(dayNum, mStartTime);
                getTifEventList();//for update 3rd channel
				break;
			case EPGConfig.EPG_SELECT_CHANNEL_COMPLETE:
				if (null != mListView) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_SELECT_CHANNEL_COMPLETE");
					if (mLockImageView.getVisibility() == View.VISIBLE) {
						mLockImageView.setVisibility(View.INVISIBLE);
					}
					updateCurrentChannelEventLockState();
					mListView.mCanChangeChannel = true;
				}
				break;
			case EPGConfig.EPG_PROGRAMINFO_SHOW:
				showSelectedProgramInfo();
				getTifEventList();
				break;
			case EPGConfig.EPG_PROGRAM_STTL_SHOW:
				if (mCurrentSelectedProgramInfo != null) {
					setSubTitleImageViewState(mCurrentSelectedProgramInfo);
				}
				break;
			case EPGConfig.EPG_DATA_RETRIEVING:
				// mDataRetrievingShow.setText(getString(R.string.epg_retrieving_show));
				mDataRetrievingShow.setVisibility(View.VISIBLE);
				break;
			case EPGConfig.EPG_DATA_RETRIEVAL_FININSH:
				// mDataRetrievingShow.setText("");
				mDataRetrievingShow.setVisibility(View.INVISIBLE);
				break;
			case EPGConfig.EPG_EVENT_REASON_SCHEDULE_UPDATE_REPEAT:
				if (null != mListView) {
					mListView.rawChangedOfChannel();
				}
				break;
			case EPGUsManager.Message_Refresh_Time:
				mCurrentDateTv.setText((String) msg.obj);
				setCurrentDate();
          refreshTimeAlixs();
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
					if (!CommonIntegration.supportTIFFunction()) {
						if (data.param2 > 0 || data.param3 > 0) {
							updateProgramList();
						}
					}
					break;
				case 6: // current window list update
					if (CommonIntegration.supportTIFFunction()) {
						if (mListView != null && mReader != null
								&& mListViewAdpter != null
								&& mListViewAdpter.getGroup() != null) {
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "666start read event in provider>"
									+ mListViewAdpter.getDayNum() + "   "
									+ mListViewAdpter.getStartHour());
							// mReader.readProgramInfoByTIF(mListViewAdpter.getGroup(),
							// mListViewAdpter.getDayNum(),
							// mListViewAdpter.getStartHour());
							// //
							// mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_NOTIFY_LIST_ADAPTER,
							// 2000);
							// mHandler.sendEmptyMessage(EPGConfig.EPG_SET_LIST_ADAPTER);
						}
					} else {
						if (mListView != null && mReader != null
								&& mListViewAdpter != null
								&& mListViewAdpter.getGroup() != null) {
							mReader.readChannelProgramInfoByTime(
									mListViewAdpter.getGroup(),
									mListViewAdpter.getDayNum(),
									mListViewAdpter.getStartHour(),
									EPGConfig.mTimeSpan);
							mHandler.removeMessages(EPGConfig.EPG_SET_LIST_ADAPTER);
							mHandler.sendEmptyMessageDelayed(
									EPGConfig.EPG_SET_LIST_ADAPTER, 100);
						}
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
						changeTimeViewsShow(mListViewAdpter.getDayNum(),
								mListViewAdpter.getStartHour());
						updateEventList(true);
					} else {
						updateEventList(false);
					}
				}
				break;
			case EPGConfig.EPG_GET_EVENT_IMMEDIATELY:
			case EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG:
			case EPGConfig.EPG_GET_TIF_EVENT_LIST:
				if (mListView != null && mReader != null
						&& mListViewAdpter != null) {
					if (isGetingData) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is readig event in provider>");
						mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
						mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
						mHandler.sendEmptyMessageDelayed(
								EPGConfig.EPG_GET_TIF_EVENT_LIST, 2000);
						return;
					} else {
						isGetingData = true;
					}
					actionCount = 0;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start read event in provider>"
							+ mListViewAdpter.getDayNum() + "   "
							+ mListViewAdpter.getStartHour());
					if (mThreadHandler != null
							&& mGetTifEventListRunnable != null) {
						mGetTifEventListRunnable.channelId = (Integer) msg.obj;
						mThreadHandler.post(mGetTifEventListRunnable);
					}
				}
				break;
			case EPGConfig.EPG_SET_LIST_ADAPTER:
				if (mListViewAdpter != null && mListView != null) {
					mListView.setAdapter(mListViewAdpter);
					mListView.setSelection(EPGConfig.SELECTED_CHANNEL_POSITION);
					mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
					mListView.mCanChangeChannel = true;
					isGetingData = false;
					Integer channelId = (Integer) msg.obj;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG_SET_LIST_ADAPTER--->channelId="
							+ channelId);
					if (channelId != null) {
						int chId = CommonIntegration.getInstance()
								.getCurrentChannelId();
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG_SET_LIST_ADAPTER--->chId=" + chId);
					}
					showSelectedProgramInfo();
				}
				break;
			case EPGConfig.EPG_NOTIFY_LIST_ADAPTER:
				if (mListViewAdpter != null) {
					mListViewAdpter.notifyDataSetChanged();
					mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
					mHandler.sendEmptyMessageDelayed(
							EPGConfig.EPG_PROGRAMINFO_SHOW, 800);
					mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVAL_FININSH);
					mListView.mCanChangeChannel = true;
				}
				break;
			case TvCallbackConst.MSG_CB_CONFIG: {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_CONFIG"
						+ ((TvCallbackData) msg.obj).param1);
				PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"MSG_CB_CONFIG: screen is on :" + pm.isInteractive());
				if ((MtkTvConfigMsgBase.ACFG_MSG_CHG_INPUT == ((TvCallbackData) msg.obj).param1)
						&& DestroyApp.isCurEPGActivity() && pm.isInteractive()) {// screen
																					// is
																					// on
					// finish();
				  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ACFG_MSG_CHG_INPUT~");
				}
			}
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
				sendMessageDelayed(message, 3000);
				// sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_CHANNEL_LIST,
				// 3000);
				break;
			case EPGConfig.EPG_UPDATE_CHANNEL_LIST:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGConfig.EPG_UPDATE_CHANNEL_LIST1111>"
						+ mListViewAdpter + ">>" + msg.arg1);
				if (mReader != null && mListViewAdpter != null
						&& mListView != null) {
					if (msg.arg1 == 8 && mListView.getList() != null
                            && mListViewAdpter.getGroup() != null) {// modify
						modifyChannelListWithThread();
					} else {// add, delete
					    refreshChannelListWithThread();
					}
				}
				break;
			case ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY reach");
				// mIsChangeChannel = false;
				break;
			default:
				break;
			}
		}

	};

    private int actionCount = 0;
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
  			    int channelId = intent.getIntExtra("channel_id", 0);
  			    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mUpdateEventReceiver channelId>>>" + channelId);
  			    if (mListViewAdpter != null
  			          && mListViewAdpter.containsChannelId(channelId)) {
    					  mListViewAdpter.addAlreadyChnnelId(channelId);
    					  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "containsChannelId true");
    				} else {
    					  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "containsChannelId false");
    					  return;
    				}

    				if (actionName.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_PF)) { // update
    																					// PF
    																					// channel
    																					// event
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MTK_INTENT_EVENT_UPDATE_PF~");
    				} else if (actionName
    						.equals(MtkTvIntent.MTK_INTENT_EVENT_UPDATE_ACTIVE_WIN)) {// update
    																					// active
    																					// window
    																					// event
      		      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mUpdateEventReceiver actionCount>>>" + actionCount);
    		        mListView.mCanChangeChannel = false;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " country>>>"+country + ",operator>> " + operator + ", tunerMode>> " + tunerMode);
                if(EPGUtil.getIsNeverReceived()){
                    EPGUtil.setIsNeverReceived(false);
                    mHandler.sendEmptyMessage(EPGConfig.EPG_GET_EVENT_IMMEDIATELY);
                }else if(country.equals(MtkTvConfigTypeBase.S3166_CFG_COUNT_DEU)
                      && tunerMode == 1
                      && operator.equals(CableOperator.Unitymedia)){
                    if (mListViewAdpter.isAlreadyGetAll()) {
                        actionCount++;
                        if(actionCount > 10){
                            mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
                            mHandler.sendEmptyMessage(EPGConfig.EPG_GET_TIF_EVENT_LIST);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mUpdateEventReceiver actionCount > 10!");
                        }
                        mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
                        mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG,
                            4000);
                    } else {
                        if (mDataRetrievingShow != null &&
                           mDataRetrievingShow.getVisibility() != View.VISIBLE) {
                            mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
                        }
                        mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
                        mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
                        int delayTime = 1000;
                        if (isGetingData) {
                            delayTime = 2000;
                        }
                        mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_GET_TIF_EVENT_LIST, delayTime);
                   }

                } else {
                    if (!mListViewAdpter.isAlreadyGetAll()) {
                        if (mDataRetrievingShow != null
                            && mDataRetrievingShow.getVisibility() != View.VISIBLE) {
                            mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
                        }
                     }else {
                       actionCount++;
                       if(actionCount > 10){
                         mHandler.sendEmptyMessage(EPGConfig.EPG_GET_EVENT_IMMEDIATELY);
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mUpdateEventReceiver actionCount > 10!");
                         return;
                       }
                    }
                     int chId = CommonIntegration.getInstance()
                             .getCurrentChannelId();
                     int delayMillis = chId == channelId ? 0 : 1000;
                     mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
                     Message msg = Message.obtain();
                     msg.obj = channelId;
                     msg.what = EPGConfig.EPG_GET_TIF_EVENT_LIST;
                     mHandler.sendMessageDelayed(msg, delayMillis);
               }
           }
        }
    }
  };

	private void updateProgramList() {
		if (!mNeedNowGetData) {
			EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(
					EPGConfig.EPG_UPDATE_API_EVENT_LIST, 1000);
		}
	}

//	private void updateEvent(TvCallbackData data) {
//		if (mListViewAdpter != null) {
//			List<EPGChannelInfo> mGroup = mListViewAdpter.getGroup();
//			if (mGroup == null) {
//				return;
//			}
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateEvent()");
////			int size = mGroup.size();
//			for (EPGChannelInfo tempChannelInfo : mGroup) {
//				List<EPGProgramInfo> tempInfoList = tempChannelInfo
//						.getmTVProgramInfoList();
//				if (tempInfoList != null) {
//					for (EPGProgramInfo tempPrograminfo : tempInfoList) {
//						if (tempPrograminfo.getChannelId() == data.param2
//								&& tempPrograminfo.getProgramId() == data.param3) {
//							List<EPGProgramInfo> programList = mReader
//									.getChannelProgramList(
//											tempChannelInfo.getTVChannel(),
//											mListViewAdpter.getDayNum(),
//											mListViewAdpter.getStartHour(),
//											EPGConfig.mTimeSpan);
//							if (programList != null) {
//								for (EPGProgramInfo tempNewinfo : programList) {
//									if (tempNewinfo.getProgramId() == data.param3) {
//										int index = tempInfoList
//												.indexOf(tempPrograminfo);
//										if (index != -1) {
//											tempInfoList
//													.set(index, tempNewinfo);
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
		Log.d(TAG, "onCreate()...");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window mWindow = getWindow();
		if (null != mWindow) {
			mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.epg_eu_main);
		mIntegration = CommonIntegration.getInstance();
		if (!mIntegration.isContextInit()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "init common integergration context");
			mIntegration.setContext(
					this.getApplicationContext());
		}
		mReader = DataReader.getInstance(this);
		ScheduleListDialog.setUpDateEPGPvrMarkListener(this);
		mIs3rdTVSource = mIntegration.is3rdTVSource();
		mHandlerThead = new HandlerThread(TAG);
		mHandlerThead.start();
		mThreadHandler = new Handler(mHandlerThead.getLooper());
		toDoWhenEnterEPG();
		mMtkTvBanner = MtkTvBanner.getInstance();
		pinDialog = PinDialogFragment.create(PinDialogFragment.PIN_DIALOG_TYPE_COMMON_UNLOCK);
		pinDialog.setOnPinCheckCallback(new OnPinCheckCallback() {
			private boolean isWordCorrect = false;

            @Override
            public void stopTimeout() {
                mHandler.removeMessages(EPGConfig.EPG_PWD_TIMEOUT_DISMISS);
            }

            @Override
            public void startTimeout() {
                mHandler.removeMessages(EPGConfig.EPG_PWD_TIMEOUT_DISMISS);
                mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PWD_TIMEOUT_DISMISS,
                        10 * 1000);
            }

            @Override
            public void pinExit() {
				pinDialog.setShowing(false);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"pinExit~ isWordCorrect>>" + isWordCorrect);
				if(!isWordCorrect){
					mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
					mHandler.sendEmptyMessage(EPGConfig.EPG_PROGRAMINFO_SHOW);
				} else {
					//word correct, should dimiss lock and show detail
					if(mCurrentSelectedProgramInfo != null){
						mCurrentSelectedProgramInfo.setProgramBlock(false);
					}
				}
				if (isSupportPvr() && (DataSeparaterUtil.getInstance() != null
						&& DataSeparaterUtil.getInstance().isSupportPvrKey())) {
					calledByScheduleList();
				}
            }

            @Override
            public void onKey(int keyCode, KeyEvent event) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKey, keyCode: " + keyCode + ",event>>"+event);
            }

            @Override
            public boolean onCheckPIN(String pin) {
				boolean flag = MtkTvPWDDialog.getInstance().checkPWD(pin);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"flag: " + flag);
				if(flag){
					new MtkTvAppTVBase().unlockService(CommonIntegration.getInstance().getCurrentFocus());
					mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_IMMEDIATELY);
					mHandler.sendEmptyMessage(EPGConfig.EPG_GET_EVENT_IMMEDIATELY);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"go to get event again immediately!!!");
				}
				isWordCorrect = flag;
				return flag;
            }
        });
		lastHourTime = EPGUtil.getCurrentDayHourMinute();
		initUI();
		mListViewAdpter.setStartHour(EPGUtil.getCurrentHour());
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_EVENT_NFY, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_CONFIG, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_CHANNELIST, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE, mHandler);
		TvCallbackHandler.getInstance().addCallBackListener(
				TvCallbackConst.MSG_CB_BANNER_MSG, mHandler);
		ComponentStatusListener lister = ComponentStatusListener.getInstance();
		lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
		lister.addListener(ComponentStatusListener.NAV_ENTER_LANCHER, this);
		lister.addListener(ComponentStatusListener.NAV_POWER_OFF, this);
		registerUpdateReceiver();
		setCurrentDate();
		EPGConfig.init = true;
		EPGConfig.avoidFoucsChange = false;
		addDigitTurnCHView();

		// if (getIntent().getBooleanExtra("keyprocess", false)) {
		// mCanKeyUpToExit = true;
		// }
		operator = ScanContent.getInstance(this).getCurrentOperator();
		tunerMode = CommonIntegration.getInstance().getTunerMode();
		Log.d(TAG, "onCreate()... end");
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
				if(mListView == null){
					return;
				}
				List<EPGChannelInfo> channelList = (List<EPGChannelInfo>) mListView.getList();
				int index = -1;
				long channelId = 0;
				EPGChannelInfo changeChannel = null;

				if(mIntegration.getFavTypeState()){
					MtkTvChannelInfoBase targetChannelInfo =
							mIntegration.favoriteListbyindex(mIntegration.getSvlFromACFG(), mIntegration.getFavStateIndex(),inputDigit-1);
                    if(targetChannelInfo != null){
						int targetId = targetChannelInfo.getChannelId();
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"targetId>>"+targetId);
						for (int i = 0; i < channelList.size(); i++) {
							EPGChannelInfo channelInfo = channelList.get(i);
							if(channelInfo.getTVChannel().getChannelId() == targetId){
                                index = i;
								changeChannel = channelInfo;
							}
						}
					}else{
                    	index = -1;
					}
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"index>>"+index+",inputDigit>> "+inputDigit + ", targetChannelInfo>>"+targetChannelInfo);
				} else {
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
				}


				if (index < 0) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index<0");
					return;
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index=" + index + ",channelId=" + channelId);
				int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
				Message msg = Message.obtain();
				msg.arg1 = index;
				msg.arg2 = pageNum;
				msg.obj = channelList;
				msg.what = EPGConfig.EPG_INIT_EVENT_LIST;
				mHandler.sendMessage(msg);
				targetChannelInfo = changeChannel;
				turnChannel(false);
			}

		});
		mDigitTurnCHView.setBackgroundResource(R.drawable.nav_sundry_bg);
		contentView.addView(mDigitTurnCHView, lp);
	}

	private Runnable mToDoTuneChannel = new Runnable() {
		@Override
		public void run() {
			if(mEPGEuHelper != null){
				mEPGEuHelper.turnChannel(targetChannelInfo);
			}
		}
	};

	/*
	 *  flag == true, means need update current channel's event info
	 */
	private void turnChannel(boolean flag) {
		if(mThreadHandler != null && mToDoTuneChannel != null){
			mThreadHandler.post(mToDoTuneChannel);
		    if(flag && mHandler != null){
		    	mHandler.removeMessages(EPGConfig.EPG_SELECT_CHANNEL_COMPLETE);
				mHandler.sendEmptyMessage(EPGConfig.EPG_SELECT_CHANNEL_COMPLETE);
			}
		}
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
                country = MtkTvConfig.getInstance().getCountry();
		if(!country.equals(mIntegration.getCountryCode())){
		    mIntegration.setCountryCode(country);
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "country=" + country);
		mDataRetrievingShow = (TextView) findViewById(R.id.epg_retrieving_data);
		mCurrentDateTv = (TextView) findViewById(R.id.epg_top_date_info_tv);
    leftArrow = (ImageView) findViewById(R.id.epg_left_arrow_icon);
    rightArrow = (ImageView) findViewById(R.id.epg_right_arrow_icon);
		mSelectedDateTv = (TextView) findViewById(R.id.epg_title_date_selected_tv);
		mBeginTimeTv = (TextView) findViewById(R.id.epg_title_time_begin_tv);
		mEndTimeTv = (TextView) findViewById(R.id.epg_title_time_end_tv);
    mListViewTimeAxisRootLayout = (LinearLayout) findViewById(R.id.epg_listview_time_axis_root_layout);
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
		ImageView mPvrImageView = (ImageView) findViewById(R.id.epg_pvr_icon);
		if(isSupportPvr()){
			mPvrImageView.setVisibility(View.VISIBLE);
			if(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportPvrKey()){
				mPvrImageView.setImageResource(R.drawable.dialog_enter_tip);
			} else {
				mPvrImageView.setImageResource(R.drawable.icon_pvr);
			}
		}else{
			mPvrImageView.setVisibility(View.INVISIBLE);
		}

		mLockImageView = (ImageView) findViewById(R.id.epg_info_lock_icon);
		mSttlImageView = (ImageView) findViewById(R.id.epg_info_sttl_icon);

		LinearLayout mRootLayout = (LinearLayout) findViewById(R.id.epg_root_layout);
		FrameLayout mListViewLayout = (FrameLayout) findViewById(R.id.epg_listview_layout);
		mListView = (EPGListView) findViewById(R.id.epg_program_forecast_listview);
		mListView.setHandler(mHandler);

		mListViewAdpter = new EPGListViewAdapter(this, EPGUtil.getCurrentHour());
    mProgramDetailTv.setMovementMethod(ScrollingMovementMethod.getInstance());
		// mListViewAdpter.setHandler(mHandler);
		mMyUpData = new MyUpdata();
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		display.getMetrics(mDisplayMetrics);
		float widthPixels = mDisplayMetrics.widthPixels;
        int width = (int)((widthPixels - 40 * mDisplayMetrics.density)/4 *3);
		mListViewAdpter.setWidth(width);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "List View Program Info Total Width----------------->" + width);
		mRootLayout.requestFocus();
		mListViewLayout.requestFocus();
		if (!mListView.hasFocus()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[ListView has not focus]");
			mListView.requestFocus();
			mListView.setSelection(0);
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[ListView has  focus]");
		}
		if (mListView.hasFocus()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[The second time, ListView has focus]");
		}
	}

	private void modifyChannelListWithThread() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "modifyChannelListWithThread~");
		if (mThreadHandler != null && mModifyChannelListRunnable != null) {
			mThreadHandler.post(mModifyChannelListRunnable);
		}
	}

	private void refreshChannelListWithThread() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "refreshChannelListWithThread~");
		if (mThreadHandler != null && mRefreshChannelListRunnable != null) {
			mThreadHandler.post(mRefreshChannelListRunnable);
		}
	}

	private void getChannelListWithThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<EPGChannelInfo> mChannelList = null;
				if (CommonIntegration.supportTIFFunction()) {
					mChannelList = (ArrayList<EPGChannelInfo>) mReader
							.getAllChannelListByTIF(true);
				} else {
					mChannelList = (ArrayList<EPGChannelInfo>) mReader
							.getAllChannelList(true);
				}
				if (mChannelList != null && !mChannelList.isEmpty()) {
					int index = mReader.getCurrentPlayChannelPosition();
					int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER
							+ 1;
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

		com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onResume mListViewAdpter>>"
				+ (mListViewAdpter == null ? null : mListViewAdpter.getGroup()));
		if (mListViewAdpter != null && mListViewAdpter.getGroup() == null) {
			getChannelListWithThread();
		}
//		mEPGActivityRect = new Rect(0, 0, ScreenConstant.SCREEN_WIDTH,
//				ScreenConstant.SCREEN_HEIGHT);
		getTifEventList();
		com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onResume end");
	}

	private void getTifEventList() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "mIs3rdTVSource=" + mIs3rdTVSource);
				if (mIs3rdTVSource){
				  mHandler.sendEmptyMessageDelayed(
				      EPGConfig.EPG_GET_TIF_EVENT_LIST, 1000);
				}
			}
		}).start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onPause()");
	}

	@Override
	protected void onStop() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onDestroy()");
		 mEPGEuHelper.selectCHAfterExitEPG();
		clearData();
	}

	public void setBarkerChannel(boolean isBarkerChannel) {
		if (isBarkerChannel) {
			mBundle.putByte(MtkTvTISMsgBase.MSG_CHANNEL_IS_BARKER_CHANNEL,
					MtkTvTISMsgBase.MTK_TIS_VALUE_TRUE);
		} else {
			mBundle.putByte(MtkTvTISMsgBase.MSG_CHANNEL_IS_BARKER_CHANNEL,
					MtkTvTISMsgBase.MTK_TIS_VALUE_FALSE);
		}
		TurnkeyUiMainActivity
				.getInstance()
				.getTvView()
				.sendAppPrivateCommand(MtkTvTISMsgBase.MTK_TIS_MSG_CHANNEL,
						mBundle);
		// if (mEditor != null) {
		// mEditor.putBoolean(IS_BARKER_CHANNEL, isBarkerChannel);
		// mEditor.commit();
		// com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setBarkerChannel>>>" +
		// mSharedPreferences.getBoolean(IS_BARKER_CHANNEL, false));
		// }
	}

	private void toDoWhenEnterEPG() {
	    if (mThreadHandler != null && mToDoWhenEnterEPGRunnable != null) {
	        mThreadHandler.post(mToDoWhenEnterEPGRunnable);
	    }
	}

	private Runnable mToDoWhenEnterEPGRunnable = new Runnable() {

	    @Override
	    public void run() {
	        mEPGEuHelper.selectCHWithEnterEPG();
	    }
	};

	class MyUpdata implements EPGListView.UpDateListView {
		@Override
		@SuppressWarnings("unchecked")
		public void updata(final boolean isNext) {
			if (mHasInitListView) {
				mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
				mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
				List<EPGChannelInfo> mGroup = (List<EPGChannelInfo>) mListView
						.getCurrentList();
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

	private void updateAdapter(List<?> adpter, int dayNum, int pageNum) {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"updateAdapter dayNum>>>"+dayNum);
		mListView.initData(adpter, DataReader.PER_PAGE_CHANNEL_NUMBER,
				mMyUpData, pageNum);
		List<EPGChannelInfo> mGroup = (List<EPGChannelInfo>) mListView
				.getCurrentList();
		mListViewAdpter.setGroup(mGroup);
		mListView.updateEnablePosition(mListViewAdpter);
	}

	private void setAdapter(List<?> adpter, final int dayNum, int pageNum) {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"setAdapter dayNum>>>"+dayNum);
		if (mListView != null && mListViewAdpter != null) {
			mListView.initData(adpter, DataReader.PER_PAGE_CHANNEL_NUMBER,
					mMyUpData, pageNum);
			mHasInitListView = true;
			List<EPGChannelInfo> mGroup = (List<EPGChannelInfo>) mListView
					.getCurrentList();
			mListViewAdpter.setGroup(mGroup);
			mListView.setAdapter(mListViewAdpter);
		}
	}

	private Runnable mGetCurrentTimeRunnable = new Runnable() {

		@Override
		public void run() {
			if (mListViewAdpter != null) {
				curTime = EPGUtil.getCurrentTime();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"needRefreshList>> "+needRefreshList
						+ ",curTime - lastTime>> " + (curTime - lastTime)
				        + "=====curTime - last time: " + (curTime - lastHourTime));
				if (needRefreshList || curTime - lastTime < -60 * 2
						|| curTime - lastHourTime >= 60 * 60) {
					needRefreshList = false;
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
					mHandler.sendEmptyMessageDelayed(
							EPGConfig.EPG_UPDATE_API_EVENT_LIST, 1000);
				}
				lastTime = curTime;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPGEuTest_mGetCurrentTimeRunnable---->formatCurrentTime");
				String mDate = EPGUtil.formatCurrentTime(EPGEuActivity.this);// tmCvt.getDetailDate(DataReader.getCurrentDate());
				Message msg = Message.obtain();
				msg.obj = mDate;
				msg.what = EPGUsManager.Message_Refresh_Time;
				mHandler.sendMessageDelayed(msg, 1000);

			}
		}
	};

  // refresh time alixs
  private void refreshTimeAlixs() {
    if(mListViewAdpter == null){
      return;
	}
	  int curBeginHour = -1;
	  if(!TextUtils.isEmpty(mBeginTimeTv.getText())){
		  curBeginHour = Integer.parseInt((mBeginTimeTv.getText().toString().split(":"))[0]);
	  }else{
	  	return;
	  }
	  int curHour = EPGUtil.getCurrentLocalHour();
	  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "curBeginHour: " + curBeginHour + ", curHour: " + curHour
            + ",dayNum: " + mListViewAdpter.getDayNum());
	  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "0201 time alxis ：" + mListViewTimeAxisRootLayout.getVisibility()
			  + ", hideTimeAxis>> "+ hideTimeAxis);
	  if(curHour == 0){
		  curHour = 24;
	  }
	  if(curBeginHour == 0){
	  	curBeginHour = 24;
	  }
	  if(mListViewTimeAxisRootLayout.getVisibility() == View.VISIBLE
			  && curHour - curBeginHour >= EPGConfig.mTimeSpan){
		  needRefreshList = true;
		  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Special case, next second need refresh list view!");
		  return;
	  }
	  if (mListViewAdpter.getDayNum() == 0
			  && !hideTimeAxis
			  && curHour >= curBeginHour
			  && curHour < curBeginHour + EPGConfig.mTimeSpan) {
          mListViewTimeAxisRootLayout.setVisibility(View.VISIBLE);
      } else {
		  if(hideTimeAxis){
			  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "hideTimeAxis need be reset!");
			  hideTimeAxis = false;
		  }
          mListViewTimeAxisRootLayout.setVisibility(View.INVISIBLE);
          return;
    }

    float curDensity = getResources().getDisplayMetrics().density;

    // cal height
    float heightPixels = getResources().getDisplayMetrics().heightPixels;
    int channelCount = 0;
    if (mListViewAdpter != null) {
      channelCount = mListViewAdpter.getCount();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "channelCount: " + channelCount +",mListViewAdpter.getCount()::"+mListViewAdpter.getCount());
    if (channelCount == 0) {
      return;
    }
    float height = ((heightPixels * 0.52f) / 6 -1.5f) * channelCount  + curDensity*2.0f *(channelCount-1) ;
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "height: " + height +",curDensity:"+curDensity);

    // cal width
    if(TextUtils.isEmpty(mBeginTimeTv.getText())){
        return;
    }
    int showWidth = mListViewAdpter.getWidth()
            * EPGUtil.getTimeAlixsWidthWithHourMinuteSecond(curBeginHour%24) / (60 * EPGConfig.mTimeSpan * 60);
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showWidth: " + showWidth + ", EPGUtil.getCurrentMinuteSecond() : "
            + EPGUtil.getCurrentMinuteSecond() + ", mListViewAdpter.getWidth(): "
            + mListViewAdpter.getWidth() + ", curDensity:"+curDensity);

    float widthPixels = getResources().getDisplayMetrics().widthPixels;
    float channelWidth = (widthPixels - 20 * 2 * curDensity) / 4 * 1;
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "widthPixels: " + widthPixels + ", channelWidth: " + channelWidth);
    int width = showWidth - (int) (1 * curDensity + 0.5f);
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "width: " + width);
    if(width <= 0){
        width = 1;
    }
    // set params
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, (int) height);
    if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
		layoutParams.rightMargin = (int) channelWidth;
	}else {
		layoutParams.leftMargin = (int) channelWidth;
	}
    mListViewTimeAxisRootLayout.setLayoutParams(layoutParams);

  }

    private Runnable mModifyChannelListRunnable = new Runnable() {

        @Override
        public void run() {updateChannelList(false);}
    };

    private Runnable mRefreshChannelListRunnable = new Runnable() {

        @Override
        public void run() {updateChannelList(true);
        }
    };

    private void updateChannelList(boolean needRefreshAllChannels) {
        ArrayList<EPGChannelInfo> mChannelList = null;
        if (CommonIntegration.supportTIFFunction()) {
            mChannelList = (ArrayList<EPGChannelInfo>) mReader
                    .getAllChannelListByTIF();
        } else {
            mChannelList = (ArrayList<EPGChannelInfo>) mReader
                    .getAllChannelList();
        }

        int index = 0;
        if (mListView != null) {
            EPGChannelInfo currentChannel = (EPGChannelInfo) mListView
                    .getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
            if (currentChannel != null
                    && mReader.isChannelExit(currentChannel
                            .getTVChannel().getChannelId())) {
                index = mReader.getChannelPosition(currentChannel
                        .getTVChannel());
            } else {
                index = mReader.getCurrentPlayChannelPosition();
            }
        } else {
            index = mReader.getCurrentPlayChannelPosition();
        }
        int pageNum = index / DataReader.PER_PAGE_CHANNEL_NUMBER + 1;
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setAdapter.>index>>>" + index + "   pageNum>>" + pageNum);

        if (mChannelList != null && !mChannelList.isEmpty()) {
            if(needRefreshAllChannels || (mListView != null && pageNum != mListView.getCurrentPageNum())){
                Message msg = Message.obtain();
                msg.arg1 = index;
                msg.arg2 = pageNum;
                msg.obj = mChannelList;
                msg.what = EPGConfig.EPG_REFRESH_CHANNEL_LIST;
                mHandler.sendMessage(msg);
            } else {
                if (mListView != null && mListView.getList() != null
                        && mListViewAdpter != null) {
                    mReader.updateChannList(
                            (List<EPGChannelInfo>) mListView.getList(),
                            mChannelList);
                    EPGConfig.FROM_WHERE = EPGConfig.SET_LAST_POSITION;
                    this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mListViewAdpter != null) {
                                mListViewAdpter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        }
    }

	private GetTifEventListRunnable mGetTifEventListRunnable = new GetTifEventListRunnable();
	private class GetTifEventListRunnable implements Runnable {
		public Integer channelId;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mListView != null && mReader != null && mListViewAdpter != null
					&& mListViewAdpter.getGroup() != null) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(
						TAG,
						"start read event in provider>"
								+ mListViewAdpter.getDayNum() + "   "
								+ mListViewAdpter.getStartHour());
				mReader.readProgramInfoByTIF(mListViewAdpter.getGroup(),
						mListViewAdpter.getDayNum(),
						mListViewAdpter.getStartHour());
				Message msg = Message.obtain();
				msg.what = EPGConfig.EPG_SET_LIST_ADAPTER;
				msg.obj = channelId;
				mHandler.sendMessage(msg);
			}
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
	    ScheduleListDialog.setUpDateEPGPvrMarkListener(null);
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_CHANNELIST, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE, mHandler);
		if (mHandler != null) {
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST);
		}
		EPGLinearLayout.mCurrentSelectPosition = 0;
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_EVENT_NFY, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_CONFIG, mHandler);
		TvCallbackHandler.getInstance().removeCallBackListener(
				TvCallbackConst.MSG_CB_BANNER_MSG, mHandler);
		ComponentStatusListener lister = ComponentStatusListener.getInstance();
		lister.removeListener(this);
		if (mUpdateEventReceiver != null) {
			unregisterReceiver(mUpdateEventReceiver);
			mUpdateEventReceiver = null;
		}
		if (mHandler != null) {
			mHandler.removeMessages(EPGUsManager.Message_Refresh_Time);
			mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
			mHandler.removeMessages(EPGConfig.EPG_GET_EVENT_LIST_DELAY_LONG);
			mHandler.removeMessages(EPGConfig.EPG_SET_LIST_ADAPTER);
			mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.removeMessages(EPGConfig.EPG_INIT_EVENT_LIST);
			mHandler.removeCallbacksAndMessages(null);
		}
		if (mThreadHandler != null) {
			mThreadHandler.removeCallbacks(mGetTifEventListRunnable);
			mThreadHandler.removeCallbacks(mGetCurrentTimeRunnable);
			mThreadHandler.removeCallbacks(mModifyChannelListRunnable);
			mThreadHandler.removeCallbacks(mRefreshChannelListRunnable);
			mThreadHandler.removeCallbacks(mToDoWhenEnterEPGRunnable);
			mThreadHandler.removeCallbacks(mToDoTuneChannel);
			mThreadHandler.removeCallbacks(mCheckCurrentEventLockStateRunnable);
			mThreadHandler.removeCallbacksAndMessages(null);
			mHandlerThead.quit();
			mToDoWhenEnterEPGRunnable = null;
			mGetTifEventListRunnable = null;
			mGetCurrentTimeRunnable = null;
			mModifyChannelListRunnable = null;
			mRefreshChannelListRunnable = null;
			mCheckCurrentEventLockStateRunnable = null;
			mToDoTuneChannel = null;
			mThreadHandler = null;
			mHandlerThead = null;
		}
		MtkTvEvent.getInstance().clearActiveWindows();
		if (mListViewAdpter != null) {
			mListViewAdpter = null;
		}
		if (mListView != null) {
			mListView = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown event.getRepeatCount()>>>" + event.getRepeatCount());
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
				if(isChinese(mProgramDetailTv.getText().toString())){
					int pageHeight = mProgramDetailTv.getHeight();
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- Yellow key---- getPaddingTop(): "
							+ mProgramDetailTv.getPaddingTop() + ",getPaddingBottom: "
							+ mProgramDetailTv.getPaddingBottom() + ",pageHeight: "+pageHeight);
					if(mCurrentPage > 1){
						pageHeight = (int)((pageHeight - mProgramDetailTv.getPaddingTop()
								- mProgramDetailTv.getPaddingBottom())*1.02);
					}
					mProgramDetailTv.scrollTo(0, (mCurrentPage - 1) * pageHeight);
				} else {
				    mProgramDetailTv.scrollTo(0, (mCurrentPage - 1) * PER_PAGE_LINE	* mProgramDetailTv.getLineHeight());
				}
				mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
			}
			return true;
		case KeyMap.KEYCODE_MTKIR_BLUE:
		case KeyEvent.KEYCODE_B:
			changeBottomViewText(true, keyCode);
			final EpgType mEpgType = new EpgType(this);
			mEpgType.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface arg0) {
					changeBottomViewText(false, 0);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guanglei mEpgType.mHasEditType: "+ mEpgType.mHasEditType);
					if (mEpgType.mHasEditType) {
						notifyEPGLinearlayoutRefresh();
						mEpgType.mHasEditType = false;
					}
				}

			});
			mEpgType.show();
			return true;
		case KeyMap.KEYCODE_MTKIR_GREEN:
		case KeyEvent.KEYCODE_G:
			dayNum = mListViewAdpter.getDayNum();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei  KEYCODE_MTKIR_GREEN dayNum: " + dayNum);
			if (dayNum == EPGConfig.mMaxDayNum){
				return false;
			}
			if(TextUtils.isEmpty(mBeginTimeTv.getText())){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei  KEYCODE_MTKIR_GREEN mBeginTimeTv is null!");
				return false;
			}
			EPGConfig.init = false;
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_MTKIR_RED;
			int curLocalStartHour = Integer.parseInt((mBeginTimeTv.getText().toString()).split(":")[0]);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei KEYCODE_MTKIR_GREEN curLocalStartHour: " + curLocalStartHour + ",mListViewAdpter.getStartHour()>> "+mListViewAdpter.getStartHour());

			if((mListViewAdpter.getStartHour() + (24 - curLocalStartHour)) >= 24){
				long boundMills = EPGUtil.getEpgLastTimeMills(EPGConfig.mMaxDayNum, 0, true);
				long lastMills = EPGUtil.getEpgLastTimeMills(dayNum + 1,(mListViewAdpter.getStartHour() + (24 - curLocalStartHour)) % 24, false);
				if(boundMills < lastMills){
					return  false;
				}
				dayNum = dayNum + 1;
				mListViewAdpter.setDayNum(dayNum);
		    }
			long time = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(),
					mListViewAdpter.getDayNum(), (mListViewAdpter.getStartHour() + (24 - curLocalStartHour)) % 24);
			int hourInLocal = EPGUtil.getLocalHourByUtc(time);
			int diffHour = 0;
			if(hourInLocal == 1){
				diffHour = -1;
			}else if(hourInLocal == 23){
				diffHour = 1;
			}
			mListViewTimeAxisRootLayout.setVisibility(View.INVISIBLE);
			hideTimeAxis = true;
			mListViewAdpter.setStartHour((mListViewAdpter.getStartHour() + (24 - curLocalStartHour)) % 24 + diffHour);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei  2KEYCODE_MTKIR_GREEN dayNum: " + dayNum + ",diffHour>> "+diffHour + ",hourInLocal>> "+hourInLocal);

			mCanChangeTimeShow = true;
			mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(
					EPGConfig.EPG_UPDATE_API_EVENT_LIST, 500);
			return true;
		case KeyMap.KEYCODE_MTKIR_RED:
		case KeyEvent.KEYCODE_R:
			dayNum = mListViewAdpter.getDayNum();
			if(TextUtils.isEmpty(mBeginTimeTv.getText())){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei  KEYCODE_MTKIR_RED mBeginTimeTv is null!");
				return false;
			}
			if("".equals(mPrevDayTv.getText().toString())){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," KEYCODE_MTKIR_RED, previous day is not displayed, please confirm it.");
				return false;
		     }
			curLocalStartHour = Integer.parseInt((mBeginTimeTv.getText().toString()).split(":")[0]);
			int curLocalHour = EPGUtil.getCurrentLocalHour();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei KEYCODE_MTKIR_RED dayNum: " + dayNum + ",curLocalStartHour>> "+curLocalStartHour + " curLocalHour>> " + curLocalHour);
			if (dayNum == 0) {
				if (curLocalHour == 0) {
					curLocalHour = 24;
				}
				if (curLocalStartHour == 0) {
					curLocalStartHour = 24;
				}
				if (curLocalHour >= curLocalStartHour
						&& curLocalHour < curLocalStartHour + EPGConfig.mTimeSpan) {
					hideTimeAxis = false;
					return false;
				} else {
					curLocalHour = curLocalHour%24;
					curLocalStartHour = curLocalStartHour%24;
				}
			}

			EPGConfig.init = false;
			EPGConfig.FROM_WHERE = EPGConfig.FROM_KEYCODE_MTKIR_GREEN;
			int tempDayNum = dayNum;
			if(dayNum > 0){
				dayNum = dayNum - 1;
				mListViewAdpter.setDayNum(dayNum);
			}

			int targetHour = 0;
			long beforeUtcTime = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(),
					tempDayNum, mListViewAdpter.getStartHour());
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei 000KEYCODE_MTKIR_RED curUtcStartHour: " + mListViewAdpter.getStartHour()
			+ ",dayNum>> "+dayNum + ", beforeUtcTime - current>>"+ (beforeUtcTime - EPGUtil.getCurrentTime()));
			if (dayNum == 0 && (beforeUtcTime - EPGUtil.getCurrentTime()) < 24 * 60 *60) {
				targetHour = EPGUtil.getCurrentHour();
				hideTimeAxis = false;
			} else {
				int curUtcStartHour = mListViewAdpter.getStartHour();
				if(curUtcStartHour < curLocalStartHour){
					targetHour = curUtcStartHour - curLocalStartHour + 24;
					if (dayNum > 0) {
						dayNum = dayNum - 1;
						mListViewAdpter.setDayNum(dayNum);
					}
					if(dayNum == 0){
						targetHour = EPGUtil.getCurrentHour();
						hideTimeAxis = false;
					}
				} else {
					targetHour = curUtcStartHour - curLocalStartHour;
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei KEYCODE_MTKIR_RED curUtcStartHour: " + curUtcStartHour);
			}
			int diffHour2 = 0;
			if(dayNum > 0){
				long time2 = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(),
						mListViewAdpter.getDayNum(), (targetHour) % 24);
				int hourInLocal2 = EPGUtil.getLocalHourByUtc(time2);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,  "hourInLoca2>> "+hourInLocal2);
				if(hourInLocal2 == 1){
					diffHour2 = -1;
				}else if(hourInLocal2 == 23){
					diffHour2 = 1;
				}
			}
			mListViewTimeAxisRootLayout.setVisibility(View.INVISIBLE);
			mListViewAdpter.setStartHour(targetHour + diffHour2);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei  KEYCODE_MTKIR_RED curLocalStartHour: " + curLocalStartHour
					 + ", targetHour>> " + targetHour + ",dayNum>> "+dayNum
					+ ",diffHour2>> "+diffHour2);
			mCanChangeTimeShow = true;
			mHandler.removeMessages(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.sendEmptyMessage(EPGConfig.EPG_DATA_RETRIEVING);
			mHandler.removeMessages(EPGConfig.EPG_UPDATE_API_EVENT_LIST);
			mHandler.sendEmptyMessageDelayed(
					EPGConfig.EPG_UPDATE_API_EVENT_LIST, 500);
			return true;
		case KeyMap.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_E:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_DPAD_CENTER");
			mListViewSelectedChild = (EPGChannelInfo) mListView
					.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
			if (mListViewSelectedChild != null) {
				// If current channel is locked
				checkPwdShow(new IUIRefresh() {
					public void refreshUI() {
						if (isProgramBlocked()) {
						    pinDialog.show(getFragmentManager(), "PinDialogFragment");
						    pinDialog.setShowing(true);
						    mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PWD_TIMEOUT_DISMISS,
			                        10 * 1000);
                                                    mProgramDetailTv.setVisibility(View.INVISIBLE);
						    mLockImageView.setVisibility(View.INVISIBLE);
						}else{
							if (isSupportPvr() && (DataSeparaterUtil.getInstance() != null
									&& DataSeparaterUtil.getInstance().isSupportPvrKey())) {
								calledByScheduleList();
							}
						}
					}
				});
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
		case KeyMap.KEYCODE_MTKIR_RECORD:
		  if (isSupportPvr()) {
		    boolean isCurrentSourceDTV = CommonIntegration.getInstance()
		        .isCurrentSourceDTV();
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceDTV=" + isCurrentSourceDTV);
		    if (isCurrentSourceDTV) {
		      calledByScheduleList();
		    } else {
		      Toast.makeText(getApplicationContext(),
		          "Source not available,please change to DTV source",
		          Toast.LENGTH_SHORT).show();
		    }
		  }
			return true;
			// case KeyMap.KEYCODE_MTKIR_GUIDE:
			// if (mCanKeyUpToExit) {
			// EPGManager.getInstance().startActivity(this,
			// com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
			// } else {
			// mCanKeyUpToExit = true;
			// }
			// return true;
			// case KeyMap.KEYCODE_MENU:
			// mMenuKeyUpProcess = true;
			// return true;
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void changeTimeViewsShow(int dayNumUtc, int startHour) {
		long time = EPGTimeConvert.getInstance().setDate(EPGUtil.getCurrentDateDayAsMills(), dayNumUtc, startHour);
		String dateStr = EPGUtil.getSimpleDate(time);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guanglei dateStr>> " + dateStr + ",dayNumUtc>> "+dayNumUtc + ", startHour>> "+ startHour + ", dayNum>> "+ dayNum);
		mSelectedDateTv.setText(dateStr);

		int mBeginTime = EPGUtil.getEUIntervalHour(dayNumUtc, startHour, 0) % 24;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei changeTimeViewsShow mBeginTime>> "+mBeginTime);
		if (dayNum == 0 && (EPGUtil.isNZL() || EPGUtil.getLocalHourByUtc(EPGUtil.getCurrentTime()) <= (mBeginTime + 1))){
			mPrevDayTv.setText("");
		} else {
			mPrevDayTv.setText(getString(R.string.epg_bottom_prev_day));
		}
		if (dayNumUtc == EPGConfig.mMaxDayNum) {
			mNextDayTv.setText("");

			long boundMills = EPGUtil.getEpgLastTimeMills(
					mListViewAdpter.getDayNum(), 0, true);
			long lastMills = EPGUtil.getEpgLastTimeMills(
					mListViewAdpter.getDayNum(),
					mListViewAdpter.getStartHour() + 2, false);
			if(boundMills <= lastMills){
				if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
					leftArrow.setVisibility(View.INVISIBLE);
				} else {
					rightArrow.setVisibility(View.INVISIBLE);
				}
			} else {
				if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
					leftArrow.setVisibility(View.VISIBLE);
				} else {
					rightArrow.setVisibility(View.VISIBLE);
				}
			}
		} else if(dayNumUtc == EPGConfig.mMaxDayNum - 1) {
			long boundMills = EPGUtil.getEpgLastTimeMills(EPGConfig.mMaxDayNum, 0, true);
			long lastMills = EPGUtil.getEpgLastTimeMills(dayNumUtc + 1, startHour, false);
			if(boundMills < lastMills){
				mNextDayTv.setText("");
			} else {
				mNextDayTv.setText(getString(R.string.epg_bottom_next_day));
			}
			if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
				leftArrow.setVisibility(View.VISIBLE);
			} else {
				rightArrow.setVisibility(View.VISIBLE);
			}
		} else {
			mNextDayTv.setText(getString(R.string.epg_bottom_next_day));
			if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
				leftArrow.setVisibility(View.VISIBLE);
			} else {
				rightArrow.setVisibility(View.VISIBLE);
			}
		}

		mBeginTimeTv.setText(String.format("%d:%s", mBeginTime, getResources().getString(R.string.epg_active_minute)));
		int mEndTime = EPGUtil.getEUIntervalHour(dayNumUtc, startHour, 1) % 24;
		mEndTimeTv.setText(String.format("%d:%s", mEndTime, getResources().getString(R.string.epg_active_minute)));

		if (dayNumUtc == 0 && (startHour <= EPGUtil.getCurrentHour())){
			if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
				rightArrow.setVisibility(View.INVISIBLE);
			} else {
				leftArrow.setVisibility(View.INVISIBLE);
			}
        } else {
			if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
				rightArrow.setVisibility(View.VISIBLE);
			} else {
				leftArrow.setVisibility(View.VISIBLE);
			}
        }
	    refreshTimeAlixs();
	}

	private void updateEventList(boolean needSetActiveWindow) {
		if (mListViewAdpter != null) {
			mListView.mCanChangeChannel = false;
			mNeedNowGetData = false;
			if (needSetActiveWindow) {
				if (!mListViewAdpter.setActiveWindow()) {
					mHandler.removeMessages(EPGConfig.EPG_GET_TIF_EVENT_LIST);
					mHandler.sendEmptyMessageDelayed(
							EPGConfig.EPG_GET_TIF_EVENT_LIST, 500);
				}
			} else if (!CommonIntegration.supportTIFFunction()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(
						TAG,
						"5start read event from api>"
								+ mListViewAdpter.getDayNum() + "   "
								+ mListViewAdpter.getStartHour());
				mReader.readChannelProgramInfoByTime(
						mListViewAdpter.getGroup(),
						mListViewAdpter.getDayNum(),
						mListViewAdpter.getStartHour(), EPGConfig.mTimeSpan);
				// mHandler.sendEmptyMessage(EPGConfig.EPG_NOTIFY_LIST_ADAPTER);
				mHandler.removeMessages(EPGConfig.EPG_SET_LIST_ADAPTER);
				mHandler.sendEmptyMessageDelayed(
						EPGConfig.EPG_SET_LIST_ADAPTER, 100);
			}
		}
	}

	private void setLockIconVisibility(boolean isVisible) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLockIconVisibility>>>" + isVisible);
		if (isVisible) {
			if (pinDialog != null && pinDialog.isShowing()) {
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
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAM_STTL_SHOW,
					1000);
		}
	}

	private void showSelectedProgramInfo() {
		checkPwdShow(new IUIRefresh() {
			public void refreshUI() {
				refreshSelectedProgramInfo();
			}
		});
	}

	private void refreshSelectedProgramInfo() {
		if (mListViewAdpter != null) {
			mListViewSelectedChild = (EPGChannelInfo) mListView
					.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
					"showSelectedProgramInfo EPGConfig.SELECTED_CHANNEL_POSITION:"
							+ EPGConfig.SELECTED_CHANNEL_POSITION
							+ "   "
							+ (mListViewSelectedChild == null ? null
									: "not null"));
			if (mListViewSelectedChild != null) {
				EPGLinearLayout childView = mListView
						.getSelectedDynamicLinearLayout(EPGConfig.SELECTED_CHANNEL_POSITION);
				List<EPGProgramInfo> mProgramList = mListViewSelectedChild
						.getmTVProgramInfoList();

				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSelectedProgramInfo childView:"
						+ (childView == null ? null : "not null")
						+ "  mProgramList:"
						+ (mProgramList == null ? null : mProgramList.size())
						+ "   CurrentSelectPosition:"
						+ EPGLinearLayout.mCurrentSelectPosition);
				if (mProgramList != null
						&& childView != null
						&& !mProgramList.isEmpty()
						&& childView.getmCurrentSelectPosition() < mProgramList.size()
						&& childView.getmCurrentSelectPosition() >= 0) {
					mCurrentSelectedProgramInfo = mProgramList.get(childView
							.getmCurrentSelectPosition());
					if (mCurrentSelectedProgramInfo != null) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
								"showSelectedProgramInfo mCurrentSelectedProgramInfo:"
										+ (mCurrentSelectedProgramInfo == null ? null
												: mCurrentSelectedProgramInfo.getTitle())
										+ "  "
										+ childView.getmCurrentSelectPosition()
										+ "  Type:"
										+ mCurrentSelectedProgramInfo.getMainType());
						if (mProgramNameTv.getVisibility() != View.VISIBLE) {
							mProgramNameTv.setVisibility(View.VISIBLE);
						}
            String noProgramTitle = this.getResources().getString(R.string.nav_epg_no_program_title);
            if (country != null && (country.equals(EpgType.COUNTRY_DNK)
                    || country.equals(EpgType.COUNTRY_FIN)
                    || country.equals(EpgType.COUNTRY_IRL)
                    || country.equals(EpgType.COUNTRY_NOR)
                    ||country.equals(EpgType.COUNTRY_SWE))) {
                noProgramTitle = "";
            }
						String title = TextUtils
								.isEmpty(mCurrentSelectedProgramInfo.getTitle()) ? noProgramTitle
								: mCurrentSelectedProgramInfo.getTitle();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"title>> "+title);
						mProgramNameTv.setText(title);
						if (mProgramTimeTv.getVisibility() != View.VISIBLE) {
							mProgramTimeTv.setVisibility(View.VISIBLE);
						}

						int timeType = EPGUtil.judgeFormatTime(this);
						mProgramTimeTv.setText(EPGTimeConvert.getInstance().formatProgramTimeInfo(
										mCurrentSelectedProgramInfo, timeType));
						setProgramDetailTvState(mCurrentSelectedProgramInfo);

						if (mCurrentSelectedProgramInfo.getMainType() >= 0
						    && mCurrentSelectedProgramInfo.getMainType() < mReader.getMainType().length) {
							mProgramType.setText(mReader.getMainType()[mCurrentSelectedProgramInfo.getMainType()]);
						}else {
              mProgramType.setText(getString(R.string.nav_epg_unclassified));
            }

						if (mProgramRating.getVisibility() != View.VISIBLE) {
							mProgramRating.setVisibility(View.VISIBLE);
						}
						String strRating = mReader.mapRating2CustomerStr(
								mCurrentSelectedProgramInfo.getRatingValue(),
								mCurrentSelectedProgramInfo.getRatingType());
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "regetProgramInfo------->strRating="
								+ strRating);
						mProgramRating.setText(strRating);
						if (!isProgramBlocked()) {
							setSubTitleImageViewState(mCurrentSelectedProgramInfo);
						} else if (mSttlImageView.getVisibility() != View.INVISIBLE) {
							mSttlImageView.setVisibility(View.INVISIBLE);
						}
						if (pinDialog != null && pinDialog.isShowing()) {
					            mProgramDetailTv.setVisibility(View.INVISIBLE);
					            mLockImageView.setVisibility(View.INVISIBLE);
						}
					} else {
						if (mProgramList != null && !mProgramList.isEmpty()
								&& !this.isFinishing()) {
							mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
							mHandler.sendEmptyMessage(EPGConfig.EPG_PROGRAMINFO_SHOW);
						} else {
							setProgramInfoViewsInVisiable();
						}
					}
				} else if (childView == null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
							"showSelectedProgramInfo childView == null this.isFinishing():"
									+ this.isFinishing());
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
		if (mProgramNameTv.getVisibility() != View.INVISIBLE) {
			mProgramNameTv.setVisibility(View.INVISIBLE);
		}
		if (mProgramTimeTv.getVisibility() != View.INVISIBLE) {
			mProgramTimeTv.setVisibility(View.INVISIBLE);
		}
		if (mProgramDetailTv.getVisibility() != View.INVISIBLE) {
			mProgramDetailTv.setVisibility(View.INVISIBLE);
		}
		if (mSttlImageView.getVisibility() != View.INVISIBLE) {
			mSttlImageView.setVisibility(View.INVISIBLE);
		}
		if (mProgramRating.getVisibility() != View.INVISIBLE) {
			mProgramRating.setVisibility(View.INVISIBLE);
		}
		mPageInfoTv.setText("");
		mViewDetailTv.setText("");
		mProgramType.setText("");
		mProgramRating.setText("");
	}

	interface IUIRefresh {
		void refreshUI();
	}

	private void checkPwdShow(final IUIRefresh uiRefresh) {
		uiRefresh.refreshUI();
	}

//	private void setLockImageViewState(MtkTvChannelInfoBase mChannel,
//			int showFlag) {
//		if (mChannel != null) {
//			if (showFlag == 0
//					&& !(mEPGPwdDialog != null && mEPGPwdDialog.isShowing())) {
//				isSpecialState = true;
//				mLockImageView.setVisibility(View.VISIBLE);
//			} else {
//				isSpecialState = false;
//				mLockImageView.setVisibility(View.INVISIBLE);
//			}
//		}
//	}

	/**
	 * get caption icon display state
	 *
	 * @return
	 */
	private boolean isShowSTTLIcon() {
		if (mIs3rdTVSource) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowCaptionIcon, is3rdTVSource == true");
			return false;
		}
		boolean showCaptionIcon = mMtkTvBanner.isDisplayCaptionIcon();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowCaptionIcon, value == " + showCaptionIcon);
		return showCaptionIcon;
	}

  /**
   * get pvr function is support or not
   *
   * @return
   */
  private boolean isSupportPvr() {
    if (DataSeparaterUtil.getInstance() != null
        && DataSeparaterUtil.getInstance().isSupportPvr()
        && !CommonIntegration.getInstance().is3rdTVSource()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isSupportPvr, isSupportPvr == true");
      return true;
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isSupportPvr, isSupportPvr == false");
      return false;
    }
  }

	private void setSubTitleImageViewState(EPGProgramInfo childProgramInfo) {
		if (childProgramInfo != null && mListView != null
				&& mListView.getCurrentChannel() != null
        && !mIs3rdTVSource) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(
					TAG,
					"setSubTitleImageViewState== "
							+ childProgramInfo.isHasSubTitle() + "  "
							+ mMtkTvBanner.isDisplayCaptionIcon());
			final Long time = EPGUtil.getCurrentTime();
			List<EPGProgramInfo> mChildViewData = mListView.getCurrentChannel()
					.getmTVProgramInfoList();
			if (mChildViewData != null) {
				boolean hasFind = false;
				for (EPGProgramInfo tempEpgProgram : mChildViewData) {
					if (tempEpgProgram.getChannelId() == childProgramInfo
							.getChannelId()
							&& tempEpgProgram.getProgramId() == childProgramInfo
									.getProgramId()) {
						hasFind = true;
						break;
					}
				}
				if (hasFind) {
					if (time < childProgramInfo.getmStartTime()
							|| time > childProgramInfo.getmEndTime()) { // check
																		// is
																		// outof
																		// current
																		// time
						if (childProgramInfo.isHasSubTitle()) {
							mSttlImageView.setVisibility(View.VISIBLE);
						} else {
							mSttlImageView.setVisibility(View.INVISIBLE);
						}
					} else if (isShowSTTLIcon()) { // check whether show ttx
													// icon
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
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setProgramDetailTvState~");
		setLockIconVisibility(isProgramBlocked());
		if (isProgramBlocked()) {
			// if current channel is locked, hide program detail info
			mProgramDetailTv.setVisibility(View.INVISIBLE);
			mViewDetailTv.setText("");
			mPageInfoTv.setText("");
		} else if (childProgramInfo != null
				&& childProgramInfo.getDescribe() != null) {
			mProgramDetailTv.setVisibility(View.VISIBLE);
			String mDetailContent = childProgramInfo.getDescribe();
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
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- initProgramDetailContent()---- Lines: " + line + ",total: "+mProgramDetailTv.getHeight()
				+",item: "+mProgramDetailTv.getLineHeight());
		if (line > 0) {
			mTotalPage = (line % PER_PAGE_LINE == 0) ? (line / PER_PAGE_LINE)
					: (line / PER_PAGE_LINE + 1);
			mCurrentPage = 1;

			mProgramDetailTv.scrollTo(0, 0);
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

	public void setIsNeedFirstShowLock(boolean isNeedFirstShowLock) {
//		needFirstShowLock NeedFirstShowLock= isNeedFirstShowLock;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isNeedFirstShowLock : "+isNeedFirstShowLock);
	}

	public void notifyEPGLinearlayoutRefresh() {
		for (int i = 0; i < mListView.getChildCount(); i++) {
			EPGLinearLayout childView = mListView
					.getSelectedDynamicLinearLayout(i);
			if (childView != null) {
				childView.refreshTextLayout(i);
			}
		}
			if (mListViewSelectedChild != null) {
				checkPwdShow(new IUIRefresh() {
					public void refreshUI() {
						setProgramDetailTvState(mCurrentSelectedProgramInfo);
					}
				});
			}
	}

  public void refreshPvrMarks() {
  	if(mListView == null || mListViewAdpter == null){
  		return;
	}
    for (int i = 0; i < mListView.getChildCount(); i++) {
      RelativeLayout pvrRelativeLayout = mListView.getSelectedChannelLayout(i);
      if(pvrRelativeLayout == null){
          continue;
      }
      mListViewAdpter.refreshPvrMarks(mListViewAdpter.getGroup().get(i),pvrRelativeLayout);
    }
  }

	public void calledByScheduleList() {
		// setResult(TurnkeyUiMainActivity.RESULT_CODE_ADD_SCHEDULE_ITEM_FROM_EPG);
		EPGChannelInfo selectedChild = (EPGChannelInfo) mListView
				.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
		if (selectedChild != null) {
			List<EPGProgramInfo> programInfos = selectedChild.getmGroup();
			if (programInfos == null || programInfos.isEmpty()) {
				return;
			} else if (programInfos.size() == 1) {
				if (programInfos.get(0).getmStartTime() == 0) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programInfos.size()==1 =and starttime="
							+ programInfos.get(0).getmStartTime());
					return;
				}
			} else {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programInfos.size() >1 ==" + programInfos.size());
			}
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectedChild=null");
			return;
		}

		if (mCurrentSelectedProgramInfo != null) {
			MtkTvBookingBase item = new MtkTvBookingBase();
			// Long startTime = EPGTimeConvert.getInstance().getStartTime(
			// mCurrentSelectedProgramInfo);
			Long startTime = mCurrentSelectedProgramInfo.getmStartTime() * 1000;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start time in epg:" + startTime);

			MtkTvTimeFormatBase from = new MtkTvTimeFormatBase();
			MtkTvTimeFormatBase to = new MtkTvTimeFormatBase();

			from.setByUtc(startTime / 1000);
			MtkTvTimeBase time = new MtkTvTimeBase();

			time.convertTime(
					time.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_BRDCST_LOCAL, from,
					to);

			startTime = to.toSeconds() * 1000;

			com.mediatek.wwtv.tvcenter.util.MtkLog.d(
					TAG,
					"startTime=" + startTime + " str = "
							+ Util.timeToTimeStringEx(startTime * 1000, 0));
			if (startTime != -1L) {
				item.setRecordStartTime(startTime / 1000);
			}

			// Long endTime = EPGTimeConvert.getInstance().getEndTime(
			// mCurrentSelectedProgramInfo);
			Long endTime = mCurrentSelectedProgramInfo.getmEndTime() * 1000;
			from.setByUtc(endTime / 1000);
			time.convertTime(
					time.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_BRDCST_LOCAL, from,
					to);
			endTime = to.toSeconds() * 1000;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "endTime=" + endTime);
			if (endTime != -1L) {
				item.setRecordDuration(endTime / 1000 - startTime / 1000);
			}
			item.setRepeatMode(128);
			item.setChannelId(selectedChild.getTVChannel().getChannelId());
			item.setTunerType(tunerMode);
			item.setEventId(mCurrentSelectedProgramInfo.getProgramId());
			item.setType(1);
			ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(this, item);
			scheduleListItemDialog.setEventType(1);
            scheduleListItemDialog.setEventId(mCurrentSelectedProgramInfo.getProgramId());
			scheduleListItemDialog.show();

		}
	}

    @Override
    @SuppressWarnings("unchecked")
    public void updatePvrMark() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updatePvrMark: "+ StateScheduleList.getInstance().mHasEditPvr);
        mHandler.removeMessages(EPGConfig.EPG_REFRESH_PVR_MARK);
        mHandler.sendEmptyMessage(EPGConfig.EPG_REFRESH_PVR_MARK);
    }

	@Override
	public void updateComponentStatus(int statusID, int value) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EU EPG updateComponentStatus>>>" + statusID + ">>"
				+ value);
		if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
			CommonIntegration.getInstance().setCHChanging(false);
			mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
		} else if (statusID == ComponentStatusListener.NAV_ENTER_LANCHER) {
			finish();
		} else if (statusID == ComponentStatusListener.NAV_POWER_OFF) {
			finish();
		}
	}

	private boolean isChinese(char c) {
		return c >= 0x4E00 && c <= 0x9FA5;
	}

	private boolean isChinese(String string) {
		for (char c: string.toCharArray()) {
			if (isChinese(c)) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isChinese yes");
				return true;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isChinese no");
		return false;
	}

	private boolean isProgramBlocked() {
		if (mCurrentSelectedProgramInfo == null) {
			if (mListViewSelectedChild == null && mListView != null) {
				mListViewSelectedChild = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
			}
			if (mListViewSelectedChild == null) {
				return false;
			}
			List<EPGProgramInfo> programInfos = mListViewSelectedChild.getmTVProgramInfoList();
			if(programInfos != null && !programInfos.isEmpty()){
				mCurrentSelectedProgramInfo = programInfos.get(mListViewSelectedChild.getPlayingTVProgramPositon());
			}
		}
		if (mCurrentSelectedProgramInfo == null) {
			return false;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mtitle>> "+mCurrentSelectedProgramInfo.getmTitle()
				+ ", isProgramBlock>> "+mCurrentSelectedProgramInfo.isProgramBlock());
		return mCurrentSelectedProgramInfo.isProgramBlock();
	}

	private void updateCurrentChannelEventLockState(){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateCurrentChannelEventLockState~");
		if (mThreadHandler != null && mCheckCurrentEventLockStateRunnable != null) {
			mThreadHandler.post(mCheckCurrentEventLockStateRunnable);
		}
	}

	private Runnable mCheckCurrentEventLockStateRunnable = new Runnable() {
		@Override
		public void run() {
			if (mListViewSelectedChild == null && mListView != null) {
				mListViewSelectedChild = (EPGChannelInfo) mListView.getItemAtPosition(EPGConfig.SELECTED_CHANNEL_POSITION);
			}
			if (mListViewSelectedChild == null) {
				return;
			}
			List<EPGProgramInfo> programInfos = mListViewSelectedChild.getmTVProgramInfoList();
			MtkTvEventBase mMtkTvEventBase = new MtkTvEventBase();
			if(mMtkTvEventBase != null && programInfos != null && !programInfos.isEmpty()){
				for (EPGProgramInfo programInfo : programInfos){
					boolean isBlocked = mMtkTvEventBase.checkEventBlock(programInfo.getChannelId(), programInfo.getProgramId());
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateCurrentChannelEventLockState programInfo>> "+ programInfo.getmTitle() + " isBlocked>>"+isBlocked);
					programInfo.setProgramBlock(isBlocked);
				}
			}
			mHandler.removeMessages(EPGConfig.EPG_PROGRAMINFO_SHOW);
			mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PROGRAMINFO_SHOW, 500);
		}
	};
}
