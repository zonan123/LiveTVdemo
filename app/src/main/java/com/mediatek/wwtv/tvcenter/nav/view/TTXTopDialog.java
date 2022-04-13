package com.mediatek.wwtv.tvcenter.nav.view;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Bundle;
import android.app.Dialog;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.util.TeletextImplement;
import com.mediatek.wwtv.tvcenter.nav.util.TeletextTopItem;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextTopPageBase;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.twoworlds.tv.model.MtkTvRectangle;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import android.os.Handler;
import android.os.Message;

public class TTXTopDialog extends Dialog {
	private static final String TAG = "TTXTopDialog";
	private ListView mPageView;
	private List<TeletextTopItem> mGroupList;
	private List<TeletextTopItem> mPageList;
	private Context mContext;

	private TeletextImplement mTTXImp ;

    private MtkTvUtil mtkTvUtil;
    private static final String SOURCE_MAIN = "main";
    private Handler mHandler;
    private static final int SET_SCREEN_RECTANGLE = 100;
    MtkTvRectangle mSrcRectangleRectF;

    public TTXTopDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
//        mTeletext = MtkTvTeletext.getInstance();
        mTTXImp = TeletextImplement.getInstance();
        mtkTvUtil = MtkTvUtil.getInstance();
        mHandler = new MyHandler();
    }

    public TTXTopDialog(Context context) {
		this(context,-1);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nav_ttx_top);
		findView();
	}

	private void findView() {
	    ListView mBlockView = (ListView) findViewById(R.id.ttx_block_list);
		ListView mGroupView = (ListView) findViewById(R.id.ttx_group_list);
		mPageView = (ListView) findViewById(R.id.ttx_page_list);


		List<TeletextTopItem> mBlockList = mTTXImp.getTopList();
		mBlockView.setAdapter(new TopAdapter(mContext, mBlockList,
				new MyDataUpdateListener(mGroupView)));

		mGroupView.setAdapter(new TopAdapter(mContext, mGroupList,
				new MyDataUpdateListener(mPageView)));

		mPageView.setAdapter(new TopAdapter(mContext, mPageList));

		mBlockView.setOnItemSelectedListener(new MyItemSelectedListener(
				mGroupView));
		mGroupView.setOnItemSelectedListener(new MyItemSelectedListener(
				mPageView));
		mBlockView.setOnKeyListener(new MyPageItemOnKey());
		mGroupView.setOnKeyListener(new MyPageItemOnKey());
		mPageView.setOnKeyListener(new MyPageItemOnKey());

		mBlockView.setFocusable(true);
		mGroupView.setFocusable(true);
		mPageView.setFocusable(true);

		mBlockView.requestFocus();
		mBlockView.requestFocusFromTouch();
		mBlockView.setSelection(0);
		mPageView.setNextFocusRightId(R.id.ttx_block_list);
		mPageView.setNextFocusLeftId(R.id.ttx_group_list);
		mGroupView.setNextFocusRightId(R.id.ttx_page_list);
		mGroupView.setNextFocusLeftId(R.id.ttx_block_list);
		mBlockView.setNextFocusLeftId(R.id.ttx_page_list);
		mBlockView.setNextFocusRightId(R.id.ttx_group_list);

	}

	private class MyDataUpdateListener implements DataUpdateListener {
		private ListView subListView;

		public MyDataUpdateListener(ListView listView) {
			subListView = listView;
		}

		public void setSubList(ListView listview) {
			subListView = listview;
		}

		@Override
		public void onDataUpdate(TeletextTopItem data) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDataUpdate : " + data + "subListView:" + subListView);
			if(data != null){
				List<TeletextTopItem> sublist = data.getNextList();
				TopAdapter subAdapter = ((TopAdapter) subListView.getAdapter());
				subAdapter.update(sublist);

				if (subAdapter.getCount() > 0) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCount() > 0");
				//	subListView.setSelection(0);
				}
				subListView.invalidate();
			}
		}
	};

	@Override
    public void dismiss() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss!!!!!!!");
        mSrcRectangleRectF = mtkTvUtil.getScreenOutputDispRect(SOURCE_MAIN);
        super.dismiss();
        if (mHandler != null) {
            if (mHandler.hasMessages(SET_SCREEN_RECTANGLE)) {
                mHandler.removeMessages(SET_SCREEN_RECTANGLE);
            }
            mHandler.sendEmptyMessageDelayed(SET_SCREEN_RECTANGLE, 20);
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
          switch (msg.what) {
            case SET_SCREEN_RECTANGLE:
                float l = 0;
                float t = 0;
                float r = 0;
                float b = 0;

                if(null != mSrcRectangleRectF ){
                   l = mSrcRectangleRectF.getX();
                   t = mSrcRectangleRectF.getY();
                   r = mSrcRectangleRectF.getW();
                   b = mSrcRectangleRectF.getH();
                }

                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage,mSrcRectangleRectF,l=="+l+",t=="+t+",r=="+r+",b=="+b);
                //mtkTvUtil.setScreenOutputDispRect(SOURCE_MAIN,new MtkTvRectangle(l, t, r, b));
              break;
            default:
              break;
          }
        }
    }
    class MyPageItemOnKey implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int slectPosition = mPageView.getSelectedItemPosition();
			TeletextTopItem curItem;
			MtkTvTeletextTopPageBase curPageBase;
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"page item keyCode ="+ keyCode +" slectPosition = "
					+ slectPosition);

            if (slectPosition < 0) {
                return false;
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                	 dismiss();
					 curItem = (TeletextTopItem)mPageView.getAdapter().getItem(slectPosition);
					 curPageBase = (MtkTvTeletextTopPageBase)curItem.getObject();
					 mTTXImp.setTeletextPage(curPageBase.getNormalPageAddr());
					 return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
					return false;
				case KeyEvent.KEYCODE_PERIOD:
					 dismiss();
					 return true;
				case KeyMap.KEYCODE_MTKIR_REWIND:
					 dismiss();
					 TeletextImplement.getInstance().stopTTX();
                     return true;
                default:
                    return false;
                }
            }
            return false;
        }
    }

	private class MyItemSelectedListener implements OnItemSelectedListener {
		private ListView subListView;

		public MyItemSelectedListener(ListView listView) {
			subListView = listView;
		}

		public void setSubList(ListView listview) {
			subListView = listview;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onItemSelected : " + arg1 + "arg2:" + arg2 + "arg3 :"
					+ arg3 + "subListView:" + subListView);
			TeletextTopItem curItem;
//			MtkTvTeletextTopPageBase curPageBase = null;
			if (subListView != null) {
				curItem = (TeletextTopItem)arg0.getAdapter().getItem(arg2);
				List<TeletextTopItem> sublist = curItem.getNextList();
				TopAdapter subAdapter = (TopAdapter)subListView.getAdapter();
				if(subAdapter != null){
					subAdapter.update(sublist);
				}
				subListView.setSelection(0);
				subListView.invalidate();
			}else{
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subListView is null");
			//select new page
			//	curItem = (TeletextTopItem)arg0.getAdapter().getItem(arg2);
			//	curPageBase = (MtkTvTeletextTopPageBase)curItem.getObject();
			//	mTTXImp.setTeletextPage(curPageBase.getNormalPageAddr());
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onNothingSelected ~~ ");
		}
	};

	private class TopAdapter extends BaseAdapter {
		private List<TeletextTopItem> dataList = new ArrayList<TeletextTopItem>();
		private LayoutInflater mInflater;
		private Context mContext;

		private DataUpdateListener updateListener;

		public TopAdapter(Context context, List<TeletextTopItem> data) {
			if(data != null) {
		    		dataList.clear();
		    		dataList.addAll(data);
		  	}
			mContext = context;
			mInflater = LayoutInflater.from(mContext);

		}

		public TopAdapter(Context context, List<TeletextTopItem> data,
				DataUpdateListener listener) {
			if(data != null) {
		    		dataList.clear();
		    		dataList.addAll(data);
		  	}
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			this.updateListener = listener;
		}

		public void setListener(DataUpdateListener listener) {

			this.updateListener = listener;
		}

		@Override
		public int getCount() {
		        return dataList.size();
		}

		@Override
		public Object getItem(int arg0) {
		    return dataList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
		    return (long)arg0;
		}

		public void update(List<TeletextTopItem> list) {
			this.dataList = list;
			if (updateListener != null) {
				if (dataList != null && !dataList.isEmpty()) {
					updateListener.onDataUpdate(dataList.get(0));
				} else {
					updateListener.onDataUpdate(null);
				}
			}
			notifyDataSetChanged();
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder holder;
			if (arg1 == null) {
				arg1 = mInflater.inflate(R.layout.top_item, null);
				holder = new ViewHolder();
				holder.mTextView = (TextView) arg1.findViewById(R.id.list_item);
				arg1.setTag(holder);
			} else {
				holder = (ViewHolder) arg1.getTag();
			}

			if (dataList != null) {
				holder.mTextView.setText(dataList.get(arg0).getName());
			}
			return arg1;
		}

	}

	private class ViewHolder {
		TextView mTextView;
	}

	private interface DataUpdateListener {
		void onDataUpdate(TeletextTopItem data);
	}
}
