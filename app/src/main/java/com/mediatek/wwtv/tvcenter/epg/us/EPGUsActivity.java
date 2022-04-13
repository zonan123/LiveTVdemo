
package com.mediatek.wwtv.tvcenter.epg.us;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.MtkTvEventATSC;
import com.mediatek.twoworlds.tv.MtkTvEventATSCBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckCallback;

import java.util.ArrayList;
import java.util.List;

public class EPGUsActivity extends BaseActivity implements OnKeyListener, OnItemSelectedListener,
        ComponentStatusListener.ICStatusListener {
    private static final String TAG = "EPGUsActivity";
    private static final int REQUEST_TIME_OUT = 5*1000;

    private static final int PER_PAGE_LINE = 5;
    private EPGUsManager usManager;
    private EPGUsListAdapter mListAdapter;

    private EPGProgressDialog progressDialog;
    private EPGUsListView listView;
    private List<Integer> mAlreadyRequestList;
    private List<Integer> mRequestList;
    private TextView timeTextView;
    private Button leftChannelBtn;
    private Button rightChannelBtn;
    private TextView centerChannelTextView;
    private TextView programTime;
    private View lockIconView;
    private TextView programRating;
    private TextView programDetail;
    private ImageView arrowDown;
    private ImageView arrowUp;
    private TextView mPageInfoTv;
    private TextView preText;
    private TextView nextText;
    private TextView prePageTv;
    private TextView nextPageTv;
    private int mTotalPage; // total page
    private int mCurrentPage = 1; // current page

    private PinDialogFragment pinDialog;

    private final MtkTvEventATSCBase mEpgEevent = MtkTvEventATSC.getInstance();
    private CommonIntegration integration;
    private List<ListItemData> dataGroup = new ArrayList<ListItemData>();
    private int dayOffset = 0;
    private List<Long> mPrePageStartTimeList;
    private long startTime;
    private long dayTime;
    private boolean mNeedClearNextOldData;
    private boolean mIsNextpage;
    private boolean mNeedClearPreOldData;
    private boolean mIsPrepage;
    private boolean mCanJudgeNextPage;
    private boolean mJudgeHasNextPage;
    private List<Integer> mJudgeNextpageRequestList;
    private List<Integer> mJudgeNextDayRequestList;
    private boolean mJudgeHasNextDay;
    private boolean mNoNeedRequestNextDayData;
    private long mLastTime;
    private int mMaxDayNum;
    private long mLastPageFirstTime;
    private long mCurrentKeyPageTime;
    private long mReGetDataRequsetTime;
    private int mReGetDataGetCount;
    private int reCount = 0;
    private EpgUsUpdate mEpgUsUpdate;
    // private Timer mTimer;
    // private TimerTask mTimertask;
    // private long mLastBrocastTime, mThisBrocastTime;
    // private int dayNum = 0;
    private List<Integer> mCalculatePrePagesRequestList;
    private List<Integer>  mCalculatePrePagesAlreadyRequestList;
    private List<Long> mPrePagesStartTimes;
    private long mPreStartTime;
    private boolean mCanCalcPrePages;
    private boolean mNeedFilterEvent;
    private boolean mCanChangeChannel;
//    private boolean mCanKeyUpToExit;
    private HandlerThread mHandlerThead;
    private Handler mThreadHandler;
    private boolean mTTSEnabled = false;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // com.mediatek.wwtv.tvcenter.util.MtkLog.e("US", "handleMessage");
            switch (msg.what) {
                case EPGConfig.EPG_PWD_TIMEOUT_DISMISS:
                    pinDialog.dismiss();
                    break;
                case TvCallbackConst.MSG_CB_SVCTX_NOTIFY:
                    TvCallbackData svctxData = (TvCallbackData) msg.obj;
                    int type = svctxData.param1;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "svctx notify, type: " + type);
                    if (svctxData.param1 == 5 || svctxData.param1 == 0) {
                        if (progressDialog != null && !progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        setPageNoVisivle();
                        mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                        mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData, 200);
                    }
                    break;
                case TvCallbackConst.MSG_CB_EAS_MSG:
                    if (((TvCallbackData) msg.obj).param1 == 1) {
                        Intent intent = new Intent(EPGUsActivity.this, TurnkeyUiMainActivity.class);
                        intent.putExtra(NavBasic.NAV_COMPONENT_SHOW_FLAG, NavBasic.NAV_COMP_ID_EAS);
                        EPGUsActivity.this.setResult(NavBasic.NAV_RESULT_CODE_MENU, intent);
                        EPGUsActivity.this.finish();
                    }
                    break;
                // case TvCallbackConst.MSG_CB_EVENT_NFY:
                case TvCallbackConst.MSG_CB_ATSC_EVENT_MSG:
                    TvCallbackData data = (TvCallbackData) msg.obj;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Epg message:type:" + data.param1 + "==>" + data.param2 + "==>"
                            + data.param3 + "==>"
                            + data.param4);
                    if (usManager == null) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "usManager is null hand callback MSG_CB_ATSC_EVENT_MSG");
                        break;
                    }
                     int preDataGroupSize= usManager.groupSize();
                    if (mJudgeHasNextPage && !mRequestList.contains(data.param2)
                            && mJudgeNextpageRequestList.contains(data.param2)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dddddd" + data.param1 + "==>" + data.param2 + "==>"
                                + data.param3
                                + "==>" + data.param4);
                        removeMessages(EPGUsManager.Message_ShouldFinish);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        ListItemData judgeEventItem = usManager.getEventItem(data.param2);
                        if (judgeEventItem == null || judgeEventItem.getMillsDurationTime() <= 0) {
                            if (judgeEventItem != null) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                        "dddddd" + "==>" + judgeEventItem.getMillsDurationTime());
                            } else {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dddddd" + "==>" + "null");
                            }
                            EPGUsManager.requestComplete = true;
                            arrowDown.setVisibility(View.INVISIBLE);
                        } else {
                            EPGUsManager.requestComplete = false;
                            if (listView.getVisibility() == View.VISIBLE) {
                                arrowDown.setVisibility(View.VISIBLE);
                            } else {
                                arrowDown.setVisibility(View.INVISIBLE);
                            }
                        }
                        refreshFoot();
                        mJudgeHasNextPage = false;
                        if (mEpgEevent != null) {
                            mEpgEevent.freeEvent(data.param2);
                        }
                        if (!EPGUsManager.requestComplete && !mJudgeHasNextDay
                                && !mNoNeedRequestNextDayData
                                && dayOffset == mMaxDayNum) {
                            judgeHasNextDayData();
                        }
                        break;
                    }
                    if (mJudgeHasNextDay && !mRequestList.contains(data.param2)
                            && mJudgeNextDayRequestList.contains(data.param2)) {
                        removeMessages(EPGUsManager.Message_ShouldFinish);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        ListItemData judgeEventItem = usManager.getEventItem(data.param2);
                        if (judgeEventItem == null || judgeEventItem.getMillsDurationTime() <= 0) {
                            if (judgeEventItem != null) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                        TAG,
                                        "mJudgeHasNextDay" + "==>"
                                                + judgeEventItem.getMillsDurationTime());
                            } else {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mJudgeHasNextDay" + "==>" + "null");
                            }
                            mNoNeedRequestNextDayData = true;
                            if (mMaxDayNum < dayOffset) {
                                mMaxDayNum = dayOffset;
                            }
                            // mMaxDayNum = dayOffset + 1;
                        } else {
                            mMaxDayNum = dayOffset + 1;
                        }
                        refreshFoot();
                        mJudgeHasNextDay = false;
                        if (mEpgEevent != null) {
                            mEpgEevent.freeEvent(data.param2);
                        }
                        break;
                    }
                    if (mCalculatePrePagesRequestList != null
                            && mCalculatePrePagesRequestList.contains(data.param2)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "xinsheng mCalculatePrePagesRequestList>>>>"
                                + mCalculatePrePagesRequestList.size() + "  " + data.param1);
                        if (!mCalculatePrePagesAlreadyRequestList.contains(data.param2)) {
                            mCalculatePrePagesAlreadyRequestList.add(data.param2);
                        }
                        if (data.param1 == 2 || data.param1 == 5) {
                            MtkTvEventInfoBase eventInfo = usManager.getEvent(data.param2);
                            if (eventInfo != null && eventInfo.getStartTime() > 0
                                    && usManager.getDataGroup() != null
                                    && usManager.groupSize() > 0) {
                                long firstStartTime = usManager.getDataGroup().get(0)
                                        .getMillsStartTime();
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "xinsheng eventInfo>>>" + eventInfo.getStartTime()
                                        + "  "
                                        + firstStartTime
                                        + "  " + mPrePagesStartTimes.size() + "  "
                                        + mCalculatePrePagesAlreadyRequestList.size());
                                if (eventInfo.getStartTime() < firstStartTime) {
                                    if (mPreStartTime < eventInfo.getStartTime()
                                            + eventInfo.getDuration()) {
                                        mPreStartTime = eventInfo.getStartTime()
                                                + eventInfo.getDuration();
                                    }
                                    if (mPrePagesStartTimes != null) {
                                        if (mPrePagesStartTimes.isEmpty()) {
                                            // mPrePagesStartTimes.add(0L);
                                            mPrePagesStartTimes.add(eventInfo.getStartTime());
                                        } else if (mPrePagesStartTimes.get(mPrePagesStartTimes
                                                .size() - 1) < eventInfo
                                                .getStartTime()) {
                                            mPrePagesStartTimes.add(eventInfo.getStartTime());
                                        }
                                        if (mCalculatePrePagesAlreadyRequestList.size() == mCalculatePrePagesRequestList
                                                .size()
                                                && mPrePagesStartTimes.size()
                                                        % EPGUsManager.PER_PAGE_NUM == 0) {
                                            calculatPrePages(mPreStartTime,
                                                    EPGUsManager.PER_PAGE_NUM, false);
                                        }
                                    }
                                } else {
                                    if (mCalculatePrePagesRequestList != null) {
                                      for (int i : mCalculatePrePagesRequestList) {
                                        mEpgEevent.freeEvent(i);
                                      }
                                        mCalculatePrePagesRequestList.clear();
                                    }
                                    mCalculatePrePagesAlreadyRequestList.clear();
                                    mPrePageStartTimeList.clear();
                                    if (mPrePagesStartTimes.size() % EPGUsManager.PER_PAGE_NUM != 0) {
                                        for (int i = 0; i < EPGUsManager.PER_PAGE_NUM; i++) {
                                            mPrePagesStartTimes.add(0, 0L);
                                            if (mPrePagesStartTimes.size()
                                                    % EPGUsManager.PER_PAGE_NUM == 0) {
                                                break;
                                            }
                                        }
                                    }
                                    int count = mPrePagesStartTimes.size()
                                            / EPGUsManager.PER_PAGE_NUM;
                                    for (int i = 0; i < count; i++) {
                                        if (i == 0) {
                                            mPrePageStartTimeList.add(0L);
                                        } else {
                                            mPrePageStartTimeList.add(mPrePagesStartTimes.get(i
                                                    * EPGUsManager.PER_PAGE_NUM));
                                        }
                                    }
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    if (mPrePageStartTimeList != null && !mPrePageStartTimeList.isEmpty()
                                            && mPrePageStartTimeList.get(mPrePageStartTimeList
                                                    .size() - 1) >= 0
                                            && !dataGroup.isEmpty()) {
                                        arrowUp.setVisibility(View.VISIBLE);
                                    } else {
                                        arrowUp.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    if (data.param2 != 0 && mRequestList.contains(data.param2)) {
                        if (!mAlreadyRequestList.contains(data.param2)) {
                            mAlreadyRequestList.add(data.param2);
                            reCount++;
                            if (mRequestList.size() == mAlreadyRequestList.size()) {
                                judegeOnePageHasNextDay(usManager.getDataGroup());
                            }
                        }
                    }
                    if (data.param1 == 2 || data.param1 == 5 || data.param1 == 3
                            || data.param1 == 4) {// ||data.param1==5 modify get data logic
                        if (mRequestList.contains(data.param2)) {
                            ListItemData eventItem = usManager.getEventItem(data.param2);
                            if (data.param1 == 3 || data.param1 == 4) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "data.param1==  size == >>" + data.param1);
                                if (mRequestList != null) {
                                  for (int i : mRequestList) {
                                    mEpgEevent.freeEvent(i);
                                  }
                                    mRequestList.clear();
                                }
                                if (progressDialog != null && !progressDialog.isShowing()) {
                                    progressDialog.show();
                                }
                                setPageNoVisivle();
                                mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                                mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData,
                                        3000);
                            } else if (eventItem != null) {
                                removeMessages(EPGUsManager.Message_ShouldFinish);
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                if (mNeedClearNextOldData) {
                                    if (mLastPageFirstTime > 0
                                            && mLastPageFirstTime > EPGUtil.getCurrentTime()) {
                                        mPrePageStartTimeList.add(mLastPageFirstTime);
                                    } else {
                                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add -----------------------0");
                                        mPrePageStartTimeList.add(0L);
                                    }
                                    usManager.clearDataGroup();
                                    if (mListAdapter != null) {
                                        mListAdapter.notifyDataSetChanged();
                                    }
                                    mNeedClearNextOldData = false;
                                    mIsNextpage = true;
                                }
                                if (mNeedClearPreOldData) {
                                    if (mPrePageStartTimeList != null && !mPrePageStartTimeList.isEmpty()) {
                                        mPrePageStartTimeList
                                                .remove(mPrePageStartTimeList.size() - 1);
                                    }
                                    usManager.clearDataGroup();
                                    if (mListAdapter != null) {
                                        mListAdapter.notifyDataSetChanged();
                                    }
                                    mNeedClearPreOldData = false;
                                    mIsPrepage = true;
                                }
                                if (data.param1 == 2) {
                                    usManager.addDataGroupItem(eventItem);
                                } else {
                                    usManager.updateDataGroup(eventItem, data.param2);
                                }
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                        TAG,
                                        "mNeedFilterEvent>>>" + mNeedFilterEvent + "  "
                                                + usManager.groupSize());
                                if (mNeedFilterEvent && usManager.groupSize() > 0) {
                                    if (usManager.getDataGroup().get(0).getMillsStartTime() > 0
                                            && usManager.getDataGroup().get(0).getMillsStartTime() < startTime
                                                    + dayTime) {
                                        mEpgEevent.freeEvent(usManager.getDataGroup().get(0)
                                                .getEventId());
                                        mRequestList.remove((Integer) usManager.getDataGroup()
                                                .get(0).getEventId());
                                        mAlreadyRequestList.remove((Integer) usManager
                                                .getDataGroup().get(0)
                                                .getEventId());
                                        usManager.getDataGroup().remove(0);
                                    }
                                    if (usManager.groupSize() > 0
                                            && mAlreadyRequestList.size() >= EPGUsManager.PER_PAGE_NUM) {
                                        mNeedFilterEvent = false;
                                    }
                                }
                                if (usManager.groupSize() > EPGUsManager.PER_PAGE_NUM) {
                                    usManager.getDataGroup().remove(usManager.groupSize() - 1);
                                }
                                if (mListAdapter != null) {
                                    int currentDataGroupSize=usManager.groupSize();
                                    if(preDataGroupSize!=currentDataGroupSize){
                                        mListAdapter.notifyDataSetChanged();
                                    }
                                }
                                sendEmptyMessageDelayed(EPGUsManager.Message_Refresh_ListView, 100);
                                if (mCanCalcPrePages && usManager.groupSize() > 0) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                            TAG,
                                            "xinsheng eventItem.getMillsStartTime() >>"
                                                    + eventItem.getMillsStartTime());
                                    mCanCalcPrePages = false;
                                    calculatPrePages(0, EPGUsManager.PER_PAGE_NUM, true);
                                }
                            }
                        }
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Epg message:reCount:" + reCount);
                    if (data.param1 == 0 && usManager != null && usManager.getDataGroup() != null) {
                        boolean clearSuccess = usManager.clearEvent(data.param2);
                        if (clearSuccess) {
                            if (mListAdapter != null) {
                                mListAdapter.notifyDataSetChanged();
                            }
                            if (mRequestList != null && mRequestList.contains(data.param2)) {
                                mRequestList.remove((Integer) data.param2);
                                reCount--;
                            }
                            mIsNextpage = true;
                            sendEmptyMessageDelayed(EPGUsManager.Message_Refresh_ListView, 100);
                            if (((usManager.getDataGroup() != null && usManager.getDataGroup()
                                    .size() == 0)
                                    || (usManager.getDataGroup() != null
                                    && usManager.getDataGroup().size() == 1
                                    && usManager.getDataGroup().get(0).getMillsStartTime() == 0))
                                    && !mNeedClearNextOldData && !mNeedClearPreOldData) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "data.param1==0  size == 0");
                                if (progressDialog != null && !progressDialog.isShowing()) {
                                    progressDialog.show();
                                }
                                setPageNoVisivle();
                                mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                                mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData,
                                        3000);
                            } else if ((mNeedClearNextOldData || mNeedClearPreOldData)
                                    && (usManager.getDataGroup() != null && usManager
                                            .getDataGroup().size() == 0)) {
                                if (progressDialog != null && !progressDialog.isShowing()) {
                                    progressDialog.show();
                                }
                                mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                            } else if (mRequestList != null
                                    && mRequestList.size() < EPGUsManager.PER_PAGE_NUM
                                    && usManager.getDataGroup().size() < EPGUsManager.PER_PAGE_NUM) {
                                mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                                mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData,
                                        5000);
                            }
                        }
                    }
                    break;
                // After getEvent, refresh list view
                case EPGUsManager.Message_Refresh_ListView:
                    // removeMessages(EPGUsManager.Message_ShouldFinish);
                    removeMessages(EPGUsManager.Message_Refresh_ListView);
                    // if (progressDialog != null && progressDialog.isShowing()) {
                    // progressDialog.dismiss();
                    // }
                    setListViewAdapter();
                    break;
                case EPGUsManager.Message_Refresh_Time:
                    long mCurrentTime = EPGUtil.getCurrentTime();
                    // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentTime>>>" + mCurrentTime + "  " + mLastTime + "  " +
                    // (mCurrentTime - mLastTime));
                    if (mCurrentTime - mLastTime < 0) {
                        mLastTime = mCurrentTime;
                        removeMessages(EPGUsManager.Message_ShouldFinish);
                        mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                        if (progressDialog != null && !progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        setPageNoVisivle();
                        mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData, 1000);
                    }
                    if (mCurrentTime - mLastTime > 5) {
                        mLastTime = mCurrentTime;
                    }
                    removeMessages(EPGUsManager.Message_Refresh_Time);
                    timeTextView.setText(usManager.getTimeToShow());// Time
                    if (CommonIntegration.getInstance().isCurrentSourceATV()) {
                        timeTextView.setVisibility(View.INVISIBLE);
                    } else {
                        timeTextView.setVisibility(View.VISIBLE);
                    }
                    sendEmptyMessageDelayed(EPGUsManager.Message_Refresh_Time, 1000);
                    break;
                case EPGUsManager.Message_ReFreshData:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Message_ReFreshData");
                    mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                    if( usManager != null){
                        usManager.initChannels();
                    }
                    reRefresh();
                    break;
                case EPGUsManager.Message_ShouldFinish:
                    removeMessages(EPGUsManager.Message_ShouldFinish);
                    if (mJudgeHasNextPage) {
                        EPGUsManager.requestComplete = true;
                        mJudgeHasNextPage = false;
                        break;
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "progressDialog:finish:" + progressDialog);
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    // if (mRequestList != null) {
                    // for (int i = 0; i < mRequestList.size(); i++) {
                    // mEpgEevent.freeEvent(mRequestList.get(i));
                    // }
                    // mRequestList.clear();
                    // }
                    if (usManager != null && usManager.getDataGroup() != null
                            && usManager.getDataGroup().size() == 0) {
                        if (mPrePageStartTimeList != null && !mPrePageStartTimeList.isEmpty()) {
                            removeMessages(EPGUsManager.Message_ShouldFinish);
                            mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                            if (progressDialog != null && !progressDialog.isShowing()) {
                                progressDialog.show();
                            }
                            setPageNoVisivle();
                            mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData, 1000);
                        } else {
                            usManager.getDataGroup().add(usManager.getNoProItem());
                            int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Message_ShouldFinish>>>" + showFlag);
                            if (showFlag == 0) {
                                usManager.getDataGroup().get(0).setBlocked(true);
                            }
                            setListViewAdapter();
                        }
                    } else {
                        EPGUsManager.requestComplete = true;

                        if (CommonIntegration.getInstance().is3rdTVSource()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progressDialog:finish, is3rdTVSource");
                            setListViewAdapter();
                        }
                    }
                    break;
                case ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY reach");
                    mCanChangeChannel = true;
                    break;
                case TvCallbackConst.MSG_CB_CHANNELIST:
                case TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "US EPG update channel list>" + ((TvCallbackData) msg.obj).param2);
                    // Message message = Message.obtain();
                    // message.what = EPGConfig.EPG_UPDATE_CHANNEL_LIST;
                    // message.arg1 = ((TvCallbackData) msg.obj).param2;
                    // sendMessageDelayed(message, 1000);
                    sendEmptyMessageDelayed(EPGConfig.EPG_UPDATE_CHANNEL_LIST, 1000);
                    break;
                case EPGConfig.EPG_UPDATE_CHANNEL_LIST:
                    if (usManager != null && mHandler != null) {
                        usManager.initChannels();
                        initChannelView();
                    }
                    break;
                default:
                    break;
            }
        };
    };

    protected void onResume() {
        super.onResume();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"onResume~");
    };
    @Override
    protected void onPause() {
        super.onPause();
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"onPause~");
    }
    private void judegeOnePageHasNextDay(List<ListItemData> dataGroup) {
        if (dataGroup != null) {
            for (ListItemData tempData : dataGroup) {
                if (tempData.getMillsStartTime() > 0) {
                    int dayNum = EPGUtil.getDayOffset(tempData.getMillsStartTime());
                    if (dayNum < 0) {
                        dayNum = 0;
                    }
                    if (mMaxDayNum < dayNum) {
                        mMaxDayNum = dayNum;
                    }
                }
            }
            refreshFoot();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epg_us_main);
        initViews();
        initData();
    }

    @Override
    protected void onStop() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()...");
        // if(null != mTimer){
        // mTimer.cancel();
        // }
        // if(null != mTimertask){
        // mTimertask.cancel();
        // }
        // mTimer = null;
        // mTimertask = null;
        if (mHandler != null) {
            mHandler.removeMessages(EPGUsManager.Message_Refresh_Time);
            mHandler.removeCallbacksAndMessages(null);
        }
        ComponentStatusListener lister = ComponentStatusListener.getInstance();
        lister.removeListener(this);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
        if (mRequestList != null) {
          for (int i : mRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mRequestList.clear();
        }
        TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST,
                mHandler);
        TvCallbackHandler.getInstance().removeCallBackListener(
                TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE, mHandler);
        TvCallbackHandler.getInstance().removeCallBackListener(
                TvCallbackConst.MSG_CB_ATSC_EVENT_MSG,
                mHandler);
        TvCallbackHandler.getInstance().removeCallBackListener(TvCallbackConst.MSG_CB_EAS_MSG, mHandler);
        TvCallbackHandler.getInstance().removeCallBackListener(
                TvCallbackConst.MSG_CB_SVCTX_NOTIFY, mHandler);
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacks(mChangeChannelPreRunnable);
            mThreadHandler.removeCallbacks(mChangeChannelNextRunnable);
            mThreadHandler.removeCallbacks(mGetRequestListRunnable);
            mThreadHandler.removeCallbacks(mJudgeNextPageRequesListRunnable);
            mThreadHandler.removeCallbacks(mJudgeNextDayRequesListRunnable);
            mThreadHandler.removeCallbacks(mCaculatePrePagesRequesListRunnable);
            mThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThead.quit();
            mChangeChannelPreRunnable = null;
            mChangeChannelNextRunnable = null;
            mGetRequestListRunnable = null;
            mJudgeNextPageRequesListRunnable = null;
            mJudgeNextDayRequesListRunnable = null;
            mCaculatePrePagesRequesListRunnable = null;
            mThreadHandler = null;
        }
        if (usManager != null) {
            if (listView != null) {
                listView.setAdapter(null);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdapter null");
            }
            usManager.clearData();
            usManager = null;
        }
    }

    private void initViews() {
        timeTextView = (TextView) findViewById(R.id.epg_us_currenttime);
        listView = (EPGUsListView) findViewById(R.id.epg_us_program_listview);
        listView.setFocusable(true);
        listView.setVisibility(View.VISIBLE);
        listView.setOnKeyListener(this);
        listView.setAccessibilityDelegate(mAccDelegate);
        listView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);

        listView.setOnItemSelectedListener(this);
        leftChannelBtn = (Button) findViewById(R.id.epg_us_channel_left);
        centerChannelTextView = (TextView) findViewById(R.id.epg_us_channel_center);
        rightChannelBtn = (Button) findViewById(R.id.epg_us_channel_right);

        leftChannelBtn.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "leftChannelBtn");
            changeChannelWithThread(true);
          }
        });

        if(mTTSEnabled){
          leftChannelBtn.setFocusable(true);
          rightChannelBtn.setFocusable(true);
        } else{
          leftChannelBtn.setFocusable(false);
          rightChannelBtn.setFocusable(false);
        }
        rightChannelBtn.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rightChannelBtn~");
            changeChannelWithThread(false);
          }
        });

        LinearLayout bottomParentLayout = (LinearLayout)findViewById(R.id.epg_bottom_layout);
        if(CommonIntegration.getInstance().isDisableColorKey()){
        	bottomParentLayout.setVisibility(View.INVISIBLE);
        }
        mPageInfoTv = (TextView) findViewById(R.id.epg_info_page_tv);
        preText = (TextView) findViewById(R.id.epg_bottom_prev_day_tv);
        nextText = (TextView) findViewById(R.id.epg_bottom_next_day_tv);
        prePageTv = (TextView) findViewById(R.id.epg_bottom_view_detail);
        nextPageTv = (TextView) findViewById(R.id.epg_bottom_view_filter);

        programTime = (TextView) findViewById(R.id.epg_us_program_time);
        lockIconView = findViewById(R.id.epg_info_lock_icon);
        programRating = (TextView) findViewById(R.id.epg_us_program_type);
        programRating.setVisibility(View.INVISIBLE);
        programDetail = (TextView) findViewById(R.id.epg_us_program_detail);
        programDetail.setLines(PER_PAGE_LINE);

        arrowDown = (ImageView) findViewById(R.id.epg_us_arrow_down);
        arrowUp = (ImageView) findViewById(R.id.epg_us_arrow_up);
        pinDialog = PinDialogFragment.create(PinDialogFragment.PIN_DIALOG_TYPE_COMMON_UNLOCK);
        pinDialog.setOnPinCheckCallback(new OnPinCheckCallback() {

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
                int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
                if (showFlag != 0) {
                    mListAdapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                    listView.requestFocus();
                    if (dataGroup.size() >= EPGUsManager.PER_PAGE_NUM
                            && !EPGUsManager.requestComplete) {
                        arrowDown.setVisibility(View.VISIBLE);
                    } else {
                        arrowDown.setVisibility(View.INVISIBLE);
                    }
                    programTime.setVisibility(View.VISIBLE);
                    lockIconView.setVisibility(View.INVISIBLE);
                    programDetail.setVisibility(View.VISIBLE);
                    programRating.setVisibility(View.VISIBLE);
                    initProgramDetailContent();
                    refreshFoot();
                } else  {
                    lockIconView.setVisibility(View.VISIBLE);
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
                    new MtkTvAppTVBase().unlockService(CommonIntegration.getInstance()
                            .getCurrentFocus());
                }
                return flag;
            }
        });
        leftChannelBtn.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

          @Override
          public void onGlobalLayout() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"leftChannelBtn onGlobalLayout");
            leftChannelBtn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            try {
              int height = leftChannelBtn.getHeight();
              Drawable leftDrawable = getResources().getDrawable(R.drawable.epg_us_left_arrow);
              Drawable rightDrawable = getResources().getDrawable(R.drawable.epg_us_right_arrow);
              int minimumWidth=leftDrawable.getMinimumWidth();
              int minimumHeight=leftDrawable.getMinimumHeight();
              if(minimumHeight<=0){
            	  return;
              }
              int width = (minimumWidth*height)/minimumHeight;
              leftDrawable.setBounds(0, 0, width, height);
              rightDrawable.setBounds(0, 0, width, height);
              leftChannelBtn.setCompoundDrawables(null, null, leftDrawable, null);
              rightChannelBtn.setCompoundDrawables(rightDrawable, null, null, null);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });

    }

    public void initData() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initData()");
        // mTimer = new Timer();
        // mTimertask = new TimerTask() {
        //
        // @Override
        // public void run() {
        // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mLastBrocastTime>>" + mThisBrocastTime + "  "+ mLastBrocastTime + "  " +
        // (mThisBrocastTime - mLastBrocastTime));
        // mThisBrocastTime = EPGUtil.getCurrentTime();
        // if (mLastBrocastTime == 0) {
        // mLastBrocastTime = mThisBrocastTime;
        // } else {
        // if (mThisBrocastTime - mLastBrocastTime >= 1 || mThisBrocastTime - mLastBrocastTime <=
        // -1) {
        // mLastBrocastTime = mThisBrocastTime;
        // if (mRequestList != null) {
        // for (int i = 0; i < mRequestList.size(); i++) {
        // mEpgEevent.freeEvent(mRequestList.get(i));
        // }
        // mRequestList.clear();
        // }
        // if (progressDialog != null && !progressDialog.isShowing()) {
        // progressDialog.show();
        // }
        // setPageNoVisivle();
        // mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
        // mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData, 3000);
        // }
        // }
        // }
        // };
        // mTimer.schedule(mTimertask, 500, 1000);
        if (!CommonIntegration.getInstance().isContextInit()) {
            CommonIntegration.getInstance().setContext(this.getApplicationContext());
        }
        mHandlerThead = new HandlerThread(TAG);
        mHandlerThead.start();
        mThreadHandler = new Handler(mHandlerThead.getLooper());
        DataReader.getInstance(this).loadMonthAndWeekRes();
        mCanChangeChannel = true;
        mJudgeNextpageRequestList = new ArrayList<Integer>();
        mJudgeNextDayRequestList = new ArrayList<Integer>();
        mAlreadyRequestList = new ArrayList<Integer>();
        mRequestList = new ArrayList<Integer>();
        integration = CommonIntegration.getInstance();
        dataGroup.clear();
        usManager = EPGUsManager.getInstance(this);
        if (usManager.getDataGroup() != null) {
            usManager.getDataGroup().clear();
            if (mListAdapter != null) {
                mListAdapter.notifyDataSetChanged();
            }
        }
        mPrePageStartTimeList = new ArrayList<Long>();
        mEpgUsUpdate = new EpgUsUpdate();
        mLastTime = EPGUtil.getCurrentTime();
        mTTSEnabled = TextToSpeechUtil.isTTSEnabled(this);
        initListView();
        // TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_EVENT_NFY,
        // mHandlerUs);
        TvCallbackHandler.getInstance()
                .addCallBackListener(TvCallbackConst.MSG_CB_CHANNELIST, mHandler);
        TvCallbackHandler.getInstance().addCallBackListener(
                TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE,
                mHandler);
        TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_ATSC_EVENT_MSG,
                mHandler);
        TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_EAS_MSG,
                mHandler);
        TvCallbackHandler.getInstance().addCallBackListener(TvCallbackConst.MSG_CB_SVCTX_NOTIFY,
        mHandler);
        ComponentStatusListener lister = ComponentStatusListener.getInstance();
        lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
        lister.addListener(ComponentStatusListener.NAV_ENTER_LANCHER, this);
        progressDialog = EPGUsManager.getInstance(this).loading(this, true);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) { // fix DTV00586024
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progressDialog onKey>>>>" + keyCode + "  action>>"
                        + event.getAction());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event.getRepeatCount()>>>" + event.getRepeatCount());
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            changeChannelWithThread(true);
                            return true;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            changeChannelWithThread(false);
                            return true;
                        case KeyEvent.KEYCODE_BACK:
                        case KeyMap.KEYCODE_MTKIR_GUIDE:
                            if (event.getRepeatCount() <= 0) {
                                // EPGManager.getInstance().startActivity(EPGUsActivity.this,
                                // com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
                                finish();
                            }
                            return true;
                        case KeyMap.KEYCODE_MENU:
                            // if (event.getRepeatCount() <= 0) {
                            // EPGManager.getInstance().startActivity(EPGUsActivity.this,
                            // com.mediatek.wwtv.tvcenter.menu.MenuMain.class);
                            // }
                            return true;
                        default:
                            break;
                    }
                    // } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    // switch (keyCode) {
                    // case KeyMap.KEYCODE_MTKIR_GUIDE:
                    // if (mCanKeyUpToExit) {
                    // EPGManager.getInstance().startActivity(EPGUsActivity.this,
                    // com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
                    // } else {
                    // mCanKeyUpToExit = true;
                    // }
                    // return true;
                    // default:
                    // break;
                    // }
                }
                return false;
            }
        });
        // show the list data for the first time.
        // from current time
        startTime = EPGUtil.getCurrentTime();
        dayTime = 0L;
        EPGUsManager.requestComplete = false;
        reGetdata(0, dayTime);
    }

    private Runnable mChangeChannelPreRunnable = new Runnable() {

        @Override
        public void run() {
            if (!usManager.onLeftChannel()) {
                mCanChangeChannel = true;
                EPGUsChannelManager epgcm=EPGUsChannelManager.getInstance(EPGUsActivity.this);
                if(epgcm.isPreChannelDig()!=epgcm.isCurrentChannelDig()){
                    sendChangeSourceMessage();
                }
            }
        }
    };

    private Runnable mChangeChannelNextRunnable = new Runnable() {

        @Override
        public void run() {
            if (!usManager.onRightChannel()) {
                mCanChangeChannel = true;
                EPGUsChannelManager epgcm=EPGUsChannelManager.getInstance(EPGUsActivity.this);
                if(epgcm.isPreChannelDig()!=epgcm.isCurrentChannelDig()){
                    sendChangeSourceMessage();
                }
            }
        }
    };

    private Runnable mGetRequestListRunnable = new Runnable() {

        @Override
        public void run() {
            mRequestList = usManager.getDataGroup(mReGetDataRequsetTime, mReGetDataGetCount);
        }
    };

    private Runnable mJudgeNextPageRequesListRunnable = new Runnable() {

        @Override
        public void run() {
            mJudgeNextpageRequestList = usManager.getDataGroup(mReGetDataRequsetTime,
                    mReGetDataGetCount);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "11111111mJudgeNextpageRequestList>>>" + mJudgeNextpageRequestList.size());
            if (mJudgeNextpageRequestList == null || mJudgeNextpageRequestList.isEmpty()) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }
    };

    private Runnable mJudgeNextDayRequesListRunnable = new Runnable() {

        @Override
        public void run() {
            mJudgeNextDayRequestList = usManager.getDataGroup(mReGetDataRequsetTime,
                    mReGetDataGetCount);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "11111111mJudgeNextDayRequestList>>>" + mJudgeNextDayRequestList.size());
            if (mJudgeNextDayRequestList == null || mJudgeNextDayRequestList.isEmpty()) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }
    };

    private Runnable mCaculatePrePagesRequesListRunnable = new Runnable() {

        @Override
        public void run() {
            mCalculatePrePagesRequestList = usManager.getDataGroup(mReGetDataRequsetTime,
                    mReGetDataGetCount);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                    TAG,
                    "11111111mCalculatePrePagesRequestList>>>"
                            + mCalculatePrePagesRequestList.size());
            if (mCalculatePrePagesRequestList == null || mCalculatePrePagesRequestList.isEmpty()) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }
    };

    private void changeChannelWithThread(final boolean isLeft) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeChannelWithThread >>>>" + mCanChangeChannel);
        if (mCanChangeChannel) {
            mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
            mCanChangeChannel = false;
            mHandler.sendEmptyMessageDelayed(
                    ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY,
                    ChannelListDialog.DEFAULT_CHANGE_CHANNEL_DELAY_TIME);
            if (mPrePageStartTimeList != null) {
                mPrePageStartTimeList.clear();
            }
            if (isLeft) {
                if (mThreadHandler != null && mChangeChannelPreRunnable != null) {
                    mThreadHandler.post(mChangeChannelPreRunnable);
                }

            } else {
                if (mThreadHandler != null && mChangeChannelNextRunnable != null) {
                    mThreadHandler.post(mChangeChannelNextRunnable);
                }
            }
        }
    }

    private void sendChangeSourceMessage(){
        mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
        Message changeMessage=mHandler.obtainMessage();
        changeMessage.what=EPGUsManager.Message_ReFreshData;
        mHandler.sendMessage(changeMessage);
        mCanChangeChannel = true;
    }
    private void setPageNoVisivle() {
        programTime.setVisibility(View.INVISIBLE);
        programDetail.setVisibility(View.INVISIBLE);
        programRating.setVisibility(View.INVISIBLE);
        lockIconView.setVisibility(View.INVISIBLE);
        arrowDown.setVisibility(View.INVISIBLE);
        arrowUp.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.INVISIBLE);
        refreshFoot();
    }

    public void reRefresh() {
        if (usManager == null) {
            return;
        }
        initChannelView();
        usManager.getDataGroup().clear();
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        if (mJudgeNextDayRequestList != null) {
          for (int i : mJudgeNextDayRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mJudgeNextDayRequestList.clear();
        }
        if (mJudgeNextpageRequestList != null) {
          for (int i : mJudgeNextpageRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mJudgeNextpageRequestList.clear();
        }
        refreshFoot();
        mJudgeHasNextPage = false;
        mJudgeHasNextDay = false;
        mNoNeedRequestNextDayData = false;
        dayOffset = 0;
        mMaxDayNum = 0;
        EPGUsManager.requestComplete = false;
        startTime = EPGUtil.getCurrentTime();
        dayTime = 0L;
        mCanCalcPrePages = false;
        mNeedClearPreOldData = false;
        mNeedClearNextOldData = false;
        mPrePageStartTimeList.clear();
        mNeedFilterEvent = false;
        reGetdata(0, dayTime);
    }

    /**
     *
     * @param time
     *            , 0 for current ,0+time for tomorrow
     */
    public void reGetdata(final long start, final long time) {// 24*60*60
        if (mRequestList != null) {
          for (int i : mRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mRequestList.clear();
        }
        if (mCalculatePrePagesRequestList != null) {
          for (int i : mCalculatePrePagesRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mCalculatePrePagesRequestList.clear();
        }
        if (mJudgeNextDayRequestList != null) {
          for (int i : mJudgeNextDayRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mJudgeNextDayRequestList.clear();
        }
        if (mJudgeNextpageRequestList != null) {
          for (int i : mJudgeNextpageRequestList) {
            mEpgEevent.freeEvent(i);
          }
            mJudgeNextpageRequestList.clear();
        }
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        setPageNoVisivle();
        reCount = 0;
        //When channel is ATV,means no program title.
        if(usManager.isCurChATV()){
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                mHandler.removeMessages(EPGUsManager.Message_ShouldFinish);
                mHandler.sendEmptyMessage(EPGUsManager.Message_ShouldFinish);
            }
        } else{
        	 mHandler.removeMessages(EPGUsManager.Message_ShouldFinish);
             mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ShouldFinish, REQUEST_TIME_OUT);
        }
        mAlreadyRequestList.clear();
        mCanJudgeNextPage = true;
        mJudgeHasNextPage = false;
        mReGetDataRequsetTime = start + time;
        mReGetDataGetCount = EPGUsManager.PER_PAGE_NUM + 1;
        if (mThreadHandler != null && mGetRequestListRunnable != null) {
            mThreadHandler.post(mGetRequestListRunnable);
        }
    }

    private void initListView() {
        timeTextView.setText(usManager.getTimeToShow());// Time
        mHandler.removeMessages(EPGUsManager.Message_Refresh_Time);
        mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_Refresh_Time, 1000);
        initChannelView();
    }

    private void initChannelView() {
        if (usManager != null) {
            String channelNumPre = usManager.getChannelNumPre();
            String channelNumNext = usManager.getChannelNumNext();
            if(TextUtils.isEmpty(channelNumPre)) {
              leftChannelBtn.setVisibility(View.INVISIBLE);
            } else {
              leftChannelBtn.setVisibility(View.VISIBLE);
              leftChannelBtn.setText(channelNumPre);
            }
            if(TextUtils.isEmpty(channelNumNext)) {
              rightChannelBtn.setVisibility(View.INVISIBLE);
            } else {
              rightChannelBtn.setVisibility(View.VISIBLE);
              rightChannelBtn.setText(channelNumNext);
            }
            centerChannelTextView.setText(String.format("%s  %s", usManager.getChannelNumCur(), usManager.getChannelNameCur()));
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(
                    TAG,
                    "initChannelView CurNum Name:" + usManager.getChannelNumCur()+ usManager.getChannelNameCur()+
                    "\nPreNum:"+channelNumPre+
                    "\nNextNum:"+channelNumNext);
        }
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int keyCode = event.getKeyCode();
//        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "dispatchKeyEvent: keyCode=" + keyCode);
//        switch (keyCode) {
//            case KeyMap.KEYCODE_DPAD_CENTER:
//            case KeyEvent.KEYCODE_ENTER:
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
//                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent KeyMap.KEYCODE_DPAD_CENTER>>>" + showFlag);
//                    if (showFlag == 0) {
//                        MtkTvChannelInfoBase curChannel = EPGUsChannelManager.getInstance(this)
//                                .getChannelCurrent();
//                        if (CommonIntegration.getInstance().isCurrentSourceBlocked()
//                                || (curChannel != null && curChannel.isBlock())) {
//                            mEPGPwdDialog.show();
//                            mEPGPwdDialog.sendAutoDismissMessage();
//                        } else {
//                            ListItemData itemData = (ListItemData) listView.getSelectedItem();
//                            if (itemData == null) {
//                                itemData = (ListItemData) listView.getItemAtPosition(0);
//                            }
//                            if (itemData.isBlocked()) {
//                                mEPGPwdDialog.show();
//                                mEPGPwdDialog.sendAutoDismissMessage();
//                                if (programDetail.getVisibility() == View.VISIBLE) {
//                                    programDetail.setVisibility(View.INVISIBLE);
//                                }
//                            }
//                        }
//                    } else {
//                        ListItemData itemData = (ListItemData) listView.getSelectedItem();
//                        if (itemData == null) {
//                            itemData = (ListItemData) listView.getItemAtPosition(0);
//                        }
//                        if (itemData.isBlocked()) {
//                            mEPGPwdDialog.show();
//                            mEPGPwdDialog.sendAutoDismissMessage();
//                            if (programDetail.getVisibility() == View.VISIBLE) {
//                                programDetail.setVisibility(View.INVISIBLE);
//                            }
//                        }
//                    }
//                }
//                return true;
//            default:
//                break;
//        }
//        return super.dispatchKeyEvent(event);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown>>>>" + keyCode + "  " + event.getAction());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event.getRepeatCount()>>>" + event.getRepeatCount());
        switch (keyCode) {
            case KeyMap.KEYCODE_MENU:
                // if (event.getRepeatCount() <= 0) {
                // EPGManager.getInstance().startActivity(this,
                // com.mediatek.wwtv.tvcenter.menu.MenuMain.class);
                // }
                return true;
            case KeyEvent.KEYCODE_BACK:
            case KeyMap.KEYCODE_MTKIR_GUIDE:
                if (event.getRepeatCount() <= 0) {
                    // EPGManager.getInstance().startActivity(this,
                    // com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
                    finish();
                }
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyUp>>>>>" + keyCode + "  " + event.getAction());
        switch (keyCode) {
            case KeyMap.KEYCODE_VOLUME_UP:
            case KeyMap.KEYCODE_VOLUME_DOWN:
                return true;
            case KeyMap.KEYCODE_MTKIR_RECORD:
    	    	if (CommonIntegration.getInstance().isCurrentSourceDTV()) {
    	    		calledByScheduleList();
    			}else {
    				Toast.makeText(getApplicationContext(),
    						"Source not available,please change to DTV source", Toast.LENGTH_SHORT).show();
    			}
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                changeChannelWithThread(true);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                changeChannelWithThread(false);
                return true;
            case KeyMap.KEYCODE_MTKIR_RED:
                getPreDayPrograms();
//            	if(!canGetPreData){
//            		 changeChannelWithThread(true);
//            	}
            return true;
            case KeyMap.KEYCODE_MTKIR_GREEN:
                getNextDayPrograms();
//            	if(!canGetNextData){
//            		 changeChannelWithThread(false);
//            	}
                return true;
                // case KeyMap.KEYCODE_MTKIR_GUIDE:
                // if (mCanKeyUpToExit) {
                // EPGManager.getInstance().startActivity(this,
                // com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity.class);
                // } else {
                // mCanKeyUpToExit = true;
                // }
                // return true;
            case KeyEvent.KEYCODE_Y:
            case KeyMap.KEYCODE_MTKIR_YELLOW:
            	getPrePageDetailInfo();
                return true;
            case KeyEvent.KEYCODE_B:
            case KeyMap.KEYCODE_MTKIR_BLUE:
            	getNextPageDetailInfo();
                return true;
            case KeyMap.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_E:
            	checkShowPWDDialog();
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    private AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {

      @Override
      public boolean onRequestSendAccessibilityEvent(ViewGroup host,
          View child, AccessibilityEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event>>> "+event);
        List<CharSequence> texts = event.getText();
        if(texts == null) {
            return false;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts + ",host.getId():"+host.getId());
        
        switch (host.getId()) {
          case R.id.epg_us_program_listview:
            if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
              List<ListItemData>  dataList = mListAdapter.getDataList();
              if(dataList != null && dataList.size() > 1){
                for (int i = 0; i < dataList.size(); i++) {
                  String day = dataList.get(i).getItemTime();
                  String itemProgContent = dataList.get(i).getItemProgramContent();
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "day()>>> " + day + ", itemProgContent>>>"+itemProgContent);
                  if((day != null && day.equals(texts.get(0).toString())) 
                      && (itemProgContent != null && itemProgContent.equals(texts.get(1).toString()))){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "i>>> "+i);
                    //find index ,refresh event info at top
                    refreshBottomData(dataList.get(i));
                  }
                }
              }
            }
            break;

          default:
            break;
        }
        return true;
      }
      
      
    };


    private boolean getPreDayPrograms(){
    	if (dayOffset == 0 || usManager.isCurChATV()) {
            return false;
        }
        if (System.currentTimeMillis() - mCurrentKeyPageTime < 500
                && System.currentTimeMillis() - mCurrentKeyPageTime > 0) {
            return false;
        }
        if (mPrePageStartTimeList != null) {
            mPrePageStartTimeList.clear();
        }
        mCurrentKeyPageTime = System.currentTimeMillis();
        usManager.clearDataGroup();
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        dayOffset = dayOffset - 1;
        mJudgeHasNextDay = false;
        mNoNeedRequestNextDayData = false;
        mMaxDayNum = dayOffset;
        EPGUsManager.requestComplete = false;
        if (dayOffset == 0) {
            mNeedFilterEvent = false;
            startTime = EPGUtil.getCurrentTime();
            dayTime = 0L;
            reGetdata(0, dayTime);
        } else {
            mNeedFilterEvent = true;
            mCanCalcPrePages = true;
            startTime = EPGUtil.getCurrentDayStartTime();
            dayTime = dayOffset * 24 * 60 * 60L;
            reGetdata(EPGUtil.getCurrentDayStartTime(),
                    dayTime);
        }
        return true;

    }

    private boolean getNextDayPrograms(){
    	if (dayOffset == mMaxDayNum || usManager.isCurChATV()) {
            return false;
        }
        if (System.currentTimeMillis() - mCurrentKeyPageTime < 500
                && System.currentTimeMillis() - mCurrentKeyPageTime > 0) {
            return false;
        }
        if (mPrePageStartTimeList != null) {
            mPrePageStartTimeList.clear();
        }
        mCurrentKeyPageTime = System.currentTimeMillis();
        usManager.clearDataGroup();
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        dayOffset = dayOffset + 1;
        mJudgeHasNextDay = false;
        mNoNeedRequestNextDayData = false;
        mMaxDayNum = dayOffset;
        EPGUsManager.requestComplete = false;
        if (dayOffset == 0) {
            mNeedFilterEvent = false;
            startTime = EPGUtil.getCurrentTime();
            dayTime = 0L;
            reGetdata(0, dayTime);
        } else {
            mNeedFilterEvent = true;
            mCanCalcPrePages = true;
            startTime = EPGUtil.getCurrentDayStartTime();
            dayTime = dayOffset * 24 * 60 * 60L;
            reGetdata(EPGUtil.getCurrentDayStartTime(), dayTime);
        }
        return true;
    }



    private boolean getPrePageDetailInfo(){
    	// page info
        if (mTotalPage > 1) {
            if (mCurrentPage == 1) {
                return false;
            }
            mCurrentPage--;
            if (mCurrentPage == 1) {
                prePageTv.setText("");
                nextPageTv.setText(getString(R.string.epg_bottom_next_page));
            } else {
                prePageTv.setText(getString(R.string.epg_bottom_prev_page));
                nextPageTv.setText(getString(R.string.epg_bottom_next_page));
            }
            final int tempHeight= (mCurrentPage -1 ) * PER_PAGE_LINE * programDetail.getLineHeight();
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getPrePageDetailInfo--->tempHeight="+tempHeight +",mCurrentPage: "+mCurrentPage);
        	programDetail.post(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					programDetail.scrollTo(0,tempHeight);
				}
        	});
            mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
            return true;
        }
        return false;

    }
    private boolean getNextPageDetailInfo(){
    	  // page info
        if (mTotalPage > 1) {
            if (mCurrentPage == mTotalPage) {
            	return false;
            }
            mCurrentPage++;
            if (mCurrentPage == mTotalPage) {
                prePageTv.setText(getString(R.string.epg_bottom_prev_page));
                nextPageTv.setText("");
            } else {
                prePageTv.setText(getString(R.string.epg_bottom_prev_page));
                nextPageTv.setText(getString(R.string.epg_bottom_next_page));
            }
            final int tempHeight= (mCurrentPage-1) * PER_PAGE_LINE * programDetail.getLineHeight();
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNextPageDetailInfo--->tempHeight="+tempHeight + ",programDetail.getLineHeight():"
        			+programDetail.getLineHeight());
            mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
            programDetail.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					programDetail.scrollBy(0,tempHeight);
				}

        	});
            return true;
        }
        return false;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKey>>>>" + keyCode + "   " + view.getId() + "  "
                + keyEvent.getAction());
        return false;
    }

    private void checkShowPWDDialog(){
    	int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkShowPWDDialog>>>showFlag=" + showFlag);
        if (showFlag == 0) {
            pinDialog.show(getFragmentManager(), "PinDialogFragment");
            pinDialog.setShowing(true);
            mHandler.sendEmptyMessageDelayed(EPGConfig.EPG_PWD_TIMEOUT_DISMISS,
                    10 * 1000);
            lockIconView.setVisibility(View.GONE);
        }
    }

    class EpgUsUpdate implements EPGUsListView.UpDateListView {

        @Override
        public void update(boolean isNextPage) {
            if (isNextPage) {
                if (!dataGroup.isEmpty() && !EPGUsManager.requestComplete) {
                    if (dataGroup.get(dataGroup.size() - 1).getMillsStartTime() > 0
                            && dataGroup.get(dataGroup.size() - 1).getMillsDurationTime() > 0) {
                        mNeedClearNextOldData = true;
                        mLastPageFirstTime = dataGroup.get(0).getMillsStartTime();
                        reGetdata(dataGroup.get(dataGroup.size() - 1).getMillsStartTime(),
                                dataGroup.get(dataGroup.size() - 1).getMillsDurationTime());
                    }
                }
            } else {
                if (!dataGroup.isEmpty()) {
                    if (dataGroup.get(0).getMillsStartTime() > 0
                            && dataGroup.get(0).getMillsDurationTime() > 0
                            && !mPrePageStartTimeList.isEmpty()) {
                        mNeedClearPreOldData = true;
                        reGetdata(mPrePageStartTimeList.get(mPrePageStartTimeList.size() - 1), 0);
                    }
                }
            }
        }

    }

    private void setListViewAdapter() {
        if (usManager == null) {
            return;
        }
        dataGroup = usManager.getDataGroup();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, ":dataGroup:" + dataGroup.size());
        int preDataGroupSize=dataGroup.size();
        // if (dataGroup.size() == 0) {
        // dataGroup.add(usManager.getNoProItem());
        // } else {
        if (dataGroup.size() > 1) {
            if (dataGroup.get(0).getMillsStartTime() == 0) {
                dataGroup.remove(0);
            }
        }
        // }
        for (ListItemData tempData : dataGroup) {
          if (tempData.getProgramStartTime() == null
              || tempData.getProgramStartTime().equals("")) {
            tempData.setProgramStartTime(EPGUtil.formatStartTime(tempData.getMillsStartTime()));
            tempData.setProgramTime(EPGUtil.getProTime(tempData.getMillsStartTime(),
                tempData.getMillsDurationTime()));
          }
        }
    int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
    if (showFlag == 0) {
      lockIconView.setVisibility(View.VISIBLE);
      MtkTvChannelInfoBase curChannel = EPGUsChannelManager.getInstance(this)
          .getChannelCurrent();
      if (CommonIntegration.getInstance().isCurrentSourceBlocked()
          || (curChannel != null && curChannel.isBlock())) {
        // setProgramBlock(false);
        listView.setVisibility(View.INVISIBLE);
      } else {
        // setProgramBlock(true);
        listView.setVisibility(View.VISIBLE);
      }
    } else {
      // setProgramBlock(false);
      listView.setVisibility(View.VISIBLE);
      lockIconView.setVisibility(View.INVISIBLE);
    }
        listView.initData(dataGroup, EPGUsManager.PER_PAGE_NUM, mEpgUsUpdate);
        if (mListAdapter == null || integration.is3rdTVSource()) {
            mListAdapter = new EPGUsListAdapter(getApplicationContext(), dataGroup);
            listView.setAdapter(mListAdapter);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdapter mListAdapter");
        } else {
            if (mIsNextpage) {
                listView.setSelection(0);
                mIsNextpage = false;
            }
            if (mIsPrepage) {
                listView.setSelection(mListAdapter.getCount() - 1);
                if (mListAdapter.getCount() == EPGUsManager.PER_PAGE_NUM) {
                    mIsPrepage = false;
                }
            }
            int currentDataGroupSize=dataGroup.size();
            if(preDataGroupSize!=currentDataGroupSize){
                mListAdapter.notifyDataSetChanged();
            }
        }

        if (dataGroup.size() < EPGUsManager.PER_PAGE_NUM) {
            EPGUsManager.requestComplete = true;
        }
        if (dataGroup.size() >= EPGUsManager.PER_PAGE_NUM
                && listView.getVisibility() == View.VISIBLE
                && !EPGUsManager.requestComplete) {
            arrowDown.setVisibility(View.VISIBLE);
        } else {
            arrowDown.setVisibility(View.INVISIBLE);
        }
        if (!mPrePageStartTimeList.isEmpty()
                && mPrePageStartTimeList.get(mPrePageStartTimeList.size() - 1) >= 0
                && !dataGroup.isEmpty()) {
            arrowUp.setVisibility(View.VISIBLE);
        } else {
            arrowUp.setVisibility(View.INVISIBLE);
        }
        if(mTTSEnabled){
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTS enable");
        } else {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTS disEnable");
        	 listView.setFocusable(true);
             leftChannelBtn.setFocusable(false);
             rightChannelBtn.setFocusable(false);
             listView.requestFocus();
             listView.requestFocusFromTouch();
             listView.requestFocus(ListView.FOCUS_DOWN);
        }
        refreshBottomData();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, ":mJudgeHasNextPagedataGroup:" + dataGroup.size() + "  " + mJudgeHasNextPage
                + "  " + mCanJudgeNextPage);
        if (dataGroup.size() == EPGUsManager.PER_PAGE_NUM
                && !mJudgeHasNextPage
                && mCanJudgeNextPage) {
            mCanJudgeNextPage = false;
            mJudgeHasNextPage = true;
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
            mHandler.removeMessages(EPGUsManager.Message_ShouldFinish);
            mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ShouldFinish, REQUEST_TIME_OUT);
            mReGetDataRequsetTime = dataGroup.get(dataGroup.size() - 1).getMillsStartTime()
                    + dataGroup.get(dataGroup.size() - 1).getMillsDurationTime();
            mReGetDataGetCount = 1;
            if (mThreadHandler != null && mJudgeNextPageRequesListRunnable != null) {
                mThreadHandler.post(mJudgeNextPageRequesListRunnable);
            }
        }
    }

    private void judgeHasNextDayData() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, ":judgeHasNextDayData:" + dataGroup.size() + "  " + mJudgeHasNextDay);
        mJudgeHasNextDay = true;
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        mHandler.removeMessages(EPGUsManager.Message_ShouldFinish);
        mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ShouldFinish, REQUEST_TIME_OUT);
        long mJudgeNextPageStartTime = EPGUtil.getCurrentDayStartTime() + (dayOffset + 1) * 24 * 60 * 60;
        mReGetDataRequsetTime = mJudgeNextPageStartTime;
        mReGetDataGetCount = 1;
        if (mThreadHandler != null && mJudgeNextDayRequesListRunnable != null) {
            mThreadHandler.post(mJudgeNextDayRequesListRunnable);
        }
    }

    private void calculatPrePages(final long startTime, final int count, final boolean withStart) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, ":calculatPrePages:" + dataGroup.size() + "  " + mCanCalcPrePages + "   "
                + (usManager == null ? null : usManager.groupSize()));

        //fix DTV01985305
//        if (progressDialog != null && !progressDialog.isShowing()) {
//            progressDialog.show();
//        }
        if (mCalculatePrePagesRequestList != null) {
            for (int i : mCalculatePrePagesRequestList) {
              mEpgEevent.freeEvent(i);
            }
            mCalculatePrePagesRequestList.clear();
        }
        if (withStart) {
            mPreStartTime = 0;
            if (mPrePagesStartTimes == null) {
                mPrePagesStartTimes = new ArrayList<Long>();
            } else {
                mPrePagesStartTimes.clear();
            }
        }
        if (mCalculatePrePagesAlreadyRequestList == null) {
            mCalculatePrePagesAlreadyRequestList = new ArrayList<Integer>();
        } else {
            mCalculatePrePagesAlreadyRequestList.clear();
        }
        mReGetDataRequsetTime = startTime;
        mReGetDataGetCount = count;
        if (mThreadHandler != null && mCaculatePrePagesRequesListRunnable != null) {
            mThreadHandler.post(mCaculatePrePagesRequesListRunnable);
        }
    }

    public void setProgramBlock(boolean isBlock) {
      for (ListItemData tempData : dataGroup) {
        if (tempData != null) {
          tempData.setBlocked(isBlock);
        }
        
      }
    }

    protected void initProgramDetailContent() {
        int line = programDetail.getLineCount();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- initProgramDetailContent()---- Lines: " + line);
        if (line > 0 && programDetail.getVisibility() == View.VISIBLE) {
            mTotalPage = (line % PER_PAGE_LINE == 0) ? (line / PER_PAGE_LINE)
                    : (line / PER_PAGE_LINE + 1);
            mCurrentPage = 1;
            programDetail.scrollTo(0,
                    (mCurrentPage - 1) * PER_PAGE_LINE * programDetail.getLineHeight());
            if (mTotalPage > 1) {
                mPageInfoTv.setText(String.format("%d/%d", mCurrentPage, mTotalPage));
                prePageTv.setText("");
                nextPageTv.setText(getResources().getString(
                        R.string.epg_bottom_next_page));
            } else {
                mPageInfoTv.setText("");
                prePageTv.setText("");
                nextPageTv.setText("");
            }
            // } else {
            // mHandler.postDelayed(new Runnable() {
            // public void run() {
            // initProgramDetailContent();
            //
            // }
            // }, 800);
        } else {
            mPageInfoTv.setText("");
            prePageTv.setText("");
            nextPageTv.setText("");
        }
    }

    private void refreshBottomData() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                TAG,
                "listView.getVisibility()>>>" + listView.getVisibility() + "   "
                        + listView.hasFocus()
                        + " "
                        + listView.getSelectedItemPosition());
        if (listView.getSelectedItemPosition() > mListAdapter.getDataList().size()) {
            listView.setSelection(0);
        }
        ListItemData itemData = (ListItemData) listView.getSelectedItem();
        refreshBottomData(itemData);
    }
    
    private void refreshBottomData(ListItemData itemData) {
      if (listView.getSelectedItemPosition() == -1
          || listView.getSelectedItemPosition() > mListAdapter.getDataList().size()) {
        itemData = (ListItemData) listView.getItemAtPosition(0);
      }
      if (itemData == null) {
          return;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onItemSelected:" + itemData + "   " + itemData.getItemProgramName());
      if (itemData.getMillsStartTime() == 0 || itemData.getMillsDurationTime() == 0) {
          programTime.setText("");
      } else {
          // if (itemData.getProgramTime() == null ||
          // itemData.getProgramTime().equals("")) {
          // String ptime = EPGUtil.getProTime(itemData.getMillsStartTime(),
          // itemData.getMillsDurationTime());
          // programTime.setText(ptime);
          // }
          programTime.setText(itemData.getProgramTime());
          dayOffset = EPGUtil.getDayOffset(itemData.getMillsStartTime());
          if (dayOffset < 0) {
              dayOffset = 0;
          }
      }
      programDetail.setText(itemData.getItemProgramDetail());
      programRating.setText(itemData.getItemProgramType());
      if (usManager.getDataGroup().size() == 1
              && !usManager.getDataGroup().get(0).isValid()
              && !CommonIntegration.getInstance().is3rdTVSource()) {
          programRating.setText(getString(R.string.nav_epg_not_rated));
          programDetail
                  .setText(getString(R.string.nav_No_program_details));
          programTime.setVisibility(View.INVISIBLE);
          arrowDown.setVisibility(View.INVISIBLE);
      }
    
      if (EPGUtil.getCurrentTime() > itemData.getMillsStartTime()//make sure this is the first event
          && MtkTvPWDDialog.getInstance().PWDShow() == 0) {
        MtkTvChannelInfoBase curChannel = EPGUsChannelManager.getInstance(
            this).getChannelCurrent();
        if (CommonIntegration.getInstance().isCurrentSourceBlocked()
            || (curChannel != null && curChannel.isBlock())) {
          programTime.setVisibility(View.INVISIBLE);
          programDetail.setVisibility(View.INVISIBLE);
          programRating.setVisibility(View.INVISIBLE);
          lockIconView.setVisibility(View.VISIBLE);
        } else if (pinDialog != null && pinDialog.isShowing()) {
          programTime.setVisibility(View.INVISIBLE);
          programDetail.setVisibility(View.INVISIBLE);
          programRating.setVisibility(View.VISIBLE);
          lockIconView.setVisibility(View.INVISIBLE);
        } else {
          programDetail.setVisibility(View.INVISIBLE);
          programTime.setVisibility(View.INVISIBLE);
          programRating.setVisibility(View.VISIBLE);
          lockIconView.setVisibility(View.VISIBLE);
      
      //  if (itemData.isBlocked()) {
      //    programDetail.setVisibility(View.INVISIBLE);
      //  } else {
      //    programDetail.setVisibility(View.VISIBLE);
      //  }
      //  programTime.setVisibility(View.VISIBLE);
      //  programRating.setVisibility(View.VISIBLE);
      //  lockIconView.setVisibility(View.INVISIBLE);
        }
      } else {
    //if (itemData.isBlocked()) {
    //  programDetail.setVisibility(View.INVISIBLE);
    //} else {
    //  programDetail.setVisibility(View.VISIBLE);
    //}
        programDetail.setVisibility(View.VISIBLE);
        programTime.setVisibility(View.VISIBLE);
        programRating.setVisibility(View.VISIBLE);
        lockIconView.setVisibility(View.INVISIBLE);
    }
    if (pinDialog != null && pinDialog.isShowing()) {
        programTime.setVisibility(View.INVISIBLE);
        programDetail.setVisibility(View.INVISIBLE);
        programRating.setVisibility(View.VISIBLE);
        lockIconView.setVisibility(View.INVISIBLE);
    }
      initProgramDetailContent();
      refreshFoot();
    }

    public void calledByScheduleList() {
        // setResult(TurnkeyUiMainActivity.RESULT_CODE_ADD_SCHEDULE_ITEM_FROM_EPG);
        if(usManager == null || usManager.getDataGroup() == null || usManager.getDataGroup().size() < 1){
            return;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "usManager.getDataGroup().size="
                + usManager.getDataGroup().size());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "usManager.getDataGroup().get(0).getItemProgramName()="
                + usManager.getDataGroup().get(0).getItemProgramName());
        if (usManager.getDataGroup().size() == 1
                && this.getString(R.string.nav_epg_no_program_title).equals(usManager.getDataGroup().get(0).getItemProgramName())) {
            return;
        }

		MtkTvBookingBase item = new MtkTvBookingBase();

		if (listView.getSelectedItem() !=null ) {
			ListItemData itemData = (ListItemData) listView.getSelectedItem();
//			long startTime = EPGTimeConvert.getInstance().getStartTime(itemData);
//			long endTime =EPGTimeConvert.getInstance().getEndTime(itemData);
//			item.setRecordDuration(endTime / 1000 - startTime / 1000);
//            item.setRecordStartTime(startTime / 1000);
//            item.setTunerType(CommonIntegration.getInstance().getTunerMode());
		    long startTime=itemData.getMillsStartTime()*1000;
		    MtkTvTimeFormatBase from = new MtkTvTimeFormatBase();
            MtkTvTimeFormatBase to = new MtkTvTimeFormatBase();

            from.setByUtc(startTime/1000);
            MtkTvTimeBase time = new MtkTvTimeBase();

            time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_BRDCST_LOCAL, from, to );
            startTime = to.toSeconds() *1000;

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime=" + startTime + " str = " + Util.timeToTimeStringEx(startTime * 1000,0));
            if (startTime != -1L) {
                item.setRecordStartTime(startTime / 1000);
            }

            Long endTime = (itemData.getMillsStartTime()+itemData.getMillsDurationTime())*1000;
            from.setByUtc(endTime/1000);
            time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_UTC_TO_BRDCST_LOCAL, from, to );
            endTime = to.toSeconds() *1000;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "endTime=" + endTime);
            if (endTime != -1L) {
                item.setRecordDuration(endTime / 1000 - startTime / 1000);
            }
            item.setTunerType(CommonIntegration.getInstance().getTunerMode());
		ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(this, item);
		scheduleListItemDialog.setEventType(1);
        scheduleListItemDialog.setEventId(itemData.getEventId());
		scheduleListItemDialog.show();
		}
//		Intent intent=new Intent();
//        ComponentName comp = new ComponentName("com.mediatek.wwtv.setting","com.mediatek.wwtv.setting.menu.MenuSetUpActivity");
//        intent.setComponent(comp);
//        intent.putExtra("start_schedule",true);
//		if (listView.getSelectedItem() != null) {
//			ListItemData itemData = (ListItemData) listView.getSelectedItem();
//			intent.putExtra(ScheduleItem.START_TIME, EPGTimeConvert.getInstance().getStartTime(itemData));
//			intent.putExtra(ScheduleItem.END_TIME, EPGTimeConvert.getInstance().getEndTime(itemData));
//		} else {
//			intent.putExtra(ScheduleItem.START_TIME, System.currentTimeMillis());
//			intent.putExtra(ScheduleItem.END_TIME, System.currentTimeMillis() + 60 * 60 * 1000L);
//		}
//		startActivity(intent);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("Timeshift_PVR", "calledByScheduleList()");
//		this.finish();
	}

    // public final Runnable runable = new Runnable() {
    // @Override
    // public void run() {
    // // TODO Auto-generated method stub
    // if (mListAdapter != null) {
    // int startHour = 0;
    // if (dayNum == 0) {
    // startHour = EPGUtil.getCurrentHour();
    // }
    // // setListViewAdapter(EPGUtil.getStartTime(EPGUtil.getCurrentTime(),dayNum,startHour));
    // }
    // }
    // };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        refreshBottomData();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onItemSelected:" + position + "   "
                + EPGUsManager.requestComplete
                + "  " + mJudgeHasNextDay + mNoNeedRequestNextDayData + dayOffset + "  "
                + mMaxDayNum);
        if (dataGroup.size() == EPGUsManager.PER_PAGE_NUM
                && !EPGUsManager.requestComplete && !mJudgeHasNextDay
                && !mNoNeedRequestNextDayData && dayOffset == mMaxDayNum) {
            judgeHasNextDayData();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onNothingSelected:");
    }

    public void refreshFoot() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "dayOff:" + dayOffset + "  " + mMaxDayNum);
        if (listView.getVisibility() == View.VISIBLE) {
            if (usManager != null && usManager.isCurChATV()) {
                preText.setText("");
                nextText.setText("");
            } else if (dayOffset == 0) {
                preText.setText("");
                if (mMaxDayNum == dayOffset) {
                    nextText.setText("");
                } else {
                    nextText.setText(R.string.epg_bottom_next_day);
                }
            } else if (dayOffset == -1) {
                preText.setText("");
                nextText.setText(R.string.epg_bottom_next_day);
            } else if (dayOffset == mMaxDayNum) {
                preText.setText(R.string.epg_bottom_prev_day);
                nextText.setText("");
            } else {
                preText.setText(R.string.epg_bottom_prev_day);
                nextText.setText(R.string.epg_bottom_next_day);
            }
        } else {
            preText.setText("");
            nextText.setText("");
        }
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "US EPG updateComponentStatus>>>" + statusID + ">>" + value + ">>>"
                + usManager);
        if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
            Log.d(TAG, "nextChannel()...");
            mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
            if (value == 0 && usManager != null) {
                mHandler.removeMessages(EPGConfig.EPG_UPDATE_CHANNEL_LIST);
                usManager.initChannels();
                mHandler.removeMessages(EPGUsManager.Message_ReFreshData);
                mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_ReFreshData, 100);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "US EPG updateComponentStatus:send message_refreshData");
                if(integration.is3rdTVSource()){
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "US EPG guanglei Message_Refresh_ListView");
                	mHandler.sendEmptyMessageDelayed(EPGUsManager.Message_Refresh_ListView, 100);
                }
            }
            mCanChangeChannel = true;
        } else if (statusID == ComponentStatusListener.NAV_ENTER_LANCHER) {
//            finish();
          Log.d(TAG, "NAV_ENTER_LANCHER()...");
        }
    }

}
