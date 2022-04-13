package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.TeletextImplement;
import android.widget.AdapterView;
import com.mediatek.twoworlds.tv.model.MtkTvTeletextPageBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;


public class TTXFavListDialog extends Dialog {

	private static final String TAG = "TTXFavListDialog";
	
	private int channelid = -1 ;
	private Context mContext;
//	private View mFavLayout;
	private ListView mFavListView;
	static final int CHANNEL_LIST_PAGE_MAX = 7;
    private int position = 0;

    private static MtkTvTeletextPageBase[] mFavlist = new MtkTvTeletextPageBase[CHANNEL_LIST_PAGE_MAX];
    private FavListAdapter mFavListAdapter;
    private final TeletextImplement mTTXImpl;

    private final static int DISMISS_TAG = 0;
	private final static int TIME_OUT = 8000;


	public TTXFavListDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		mTTXImpl = TeletextImplement.getInstance();
	}

	public TTXFavListDialog(Context context) {
		this(context, R.style.nav_dialog);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Constructor!");
	}

	public  void setFavPage(MtkTvTeletextPageBase page) {
		//add cur show page focus on add page/ rm cur show page from favlist, focus on 0.
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage  mFavlist.length ="+mFavlist.length);
		if(page == null){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage page is null ");
			return;
		}
		int paraPageNum = page.getPageNumber();
		int space = -1; 
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage pageNum = "+ paraPageNum);
		for (int curIndex = 0;curIndex < mFavlist.length ;curIndex++ ){
			MtkTvTeletextPageBase curPage = mFavlist[curIndex];
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage loop curPage  = "+curIndex);
			if(space == -1 && curPage == null){
				space = curIndex;
			}
			if(curPage!=null  &&
				curPage.getPageNumber()== paraPageNum){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage setCurPage null  "+paraPageNum);
				position = 0;
				mFavlist[curIndex] = null;
				return;
			}
		}
		if(space != -1){
			position = space;
			mFavlist[space] = page;
			return;
		}
		position = 0;	
	}

	public void setFavPage(MtkTvTeletextPageBase page,int position) {
		//rm selected fav page/ add cur show page[if has rm old].  (focus not change)
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage (two param) mFavlist.length ="+mFavlist.length);

			if(position >= 0 &&  position < 7){
				if(mFavlist[position] != null){
					mFavlist[position] = null;
					return ;
				}else if(page != null){
					int paraPageNum = page.getPageNumber();
							
					for (int curIndex = 0;curIndex < mFavlist.length ;curIndex++ ){
						MtkTvTeletextPageBase curPage = mFavlist[curIndex];
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage (two param)loop curPage  = "+curIndex);
						if(curPage!=null  &&
							curPage.getPageNumber()== paraPageNum){
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage (two param) setold  null  "+curIndex);
							mFavlist[curIndex] = null;
						}
					}
					mFavlist[position] = page;
				}
			}	
		}

	public MtkTvTeletextPageBase getFavPage(int position)
	{
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFavPage (two param) mFavlist.length ="+mFavlist.length);

		if(position >= 0 &&  position < 7){
			if(mFavlist[position] != null){
				return mFavlist[position];
			}
		}
		return null;			
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
		setContentView(R.layout.nav_ttx_favoritelist);
		setWindowPosition();
		init();

	}

    @Override
	protected void onStart() {
        super.onStart();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart");

		/*if (AnimationManager.getInstance().getIsAnimation()) {
			AnimationManager.getInstance()
					.channelListEnterAnimation(mFavLayout);
		}*/

	}

	public void setPositionByPage(MtkTvTeletextPageBase page){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setPositionByPage");

		if(page == null){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFavPage page is null ");
			return;
		}

		for (int curIndex = 0;curIndex < mFavlist.length ;curIndex++ ){
			MtkTvTeletextPageBase curPage = mFavlist[curIndex];
			
			if(curPage!=null  &&
				curPage.getPageNumber()== page.getPageNumber()){
				position = curIndex;
			}
		}

		position = 0;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setPositionByPage position  = "+position);
	}
	
	@Override
	public void show() {
		super.show();
		handler.sendEmptyMessageDelayed(DISMISS_TAG,TIME_OUT);
		int currentChannelId = CommonIntegration.getInstance().getCurrentChannelId();
		if(currentChannelId != channelid){
			mFavlist = null;
			mFavlist =  new MtkTvTeletextPageBase[CHANNEL_LIST_PAGE_MAX];
			channelid = currentChannelId;
		}
		mFavListAdapter.updateData(mFavlist);
		mFavListView.invalidateViews();
		mFavListView.setSelection(position);

	}
	
	public void processExtFAVkey()
	{
		int      index = 0;
		int      endIndex = 0;
		for (index = 0; index < mFavlist.length; index ++){
			if(mFavlist[index] != null){
				if(mFavlist[index].getPageNumber() == mTTXImpl.getCurrentTeletextPage().getPageNumber()){
					endIndex = index;
					index ++;
					index = index % mFavlist.length;
					//loop for find next valid page
					while(index != endIndex){
						index = index % mFavlist.length;
						if(mFavlist[index] != null){
							mTTXImpl.setTeletextPage(mFavlist[index]);
							break;
						}
						index ++;
						index = index % mFavlist.length;
					}
					break;
				}
			}
		}
		if(index == mFavlist.length){
			for (index = 0; index < mFavlist.length; index ++){
				if(mFavlist[index] != null){
					mTTXImpl.setTeletextPage(mFavlist[index]);
					break;
				}
			}
		}
	}

    public void clearFavlist(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.e("chengcl", "clean-------");
        if(mFavlist != null){
            for(int i =0;i<mFavlist.length;i++){
                mFavlist[i] = null;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyDown keyCode = "+keyCode);
		handler.removeMessages(DISMISS_TAG);
		handler.sendEmptyMessageDelayed(DISMISS_TAG,TIME_OUT);
		switch(keyCode){
		case KeyMap.KEYCODE_MTKIR_EJECT:	
			//rm selected fav page/ add cur show page[if has rm old].  (focus not change)
			setFavPage(mTTXImpl.getCurrentTeletextPage(), mFavListView.getSelectedItemPosition());
			mFavListAdapter.updateData(mFavlist);
			mFavListView.invalidateViews();
			//mFavListView.setSelection(position);
			break;
		case KeyMap.KEYCODE_DPAD_UP:
			if(mFavListView!=null && mFavListView.getSelectedItemPosition()==0){
				mFavListView.setSelection(mFavlist.length-1);
			}
			break;
		case KeyMap.KEYCODE_DPAD_DOWN:		
			if(mFavListView!=null && mFavListView.getSelectedItemPosition()==(mFavlist.length-1)){
				mFavListView.setSelection(0);
			}
			break;
		case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
			dismiss();
			break;
		case KeyMap.KEYCODE_MTKIR_STOP:
			mTTXImpl.setTeletextPage(getFavPage((mFavListView.getSelectedItemPosition()+1)%CHANNEL_LIST_PAGE_MAX));
			mFavListView.setSelection((mFavListView.getSelectedItemPosition()+1)%CHANNEL_LIST_PAGE_MAX);
			break;	
		case KeyMap.KEYCODE_MTKIR_REWIND:
			dismiss();
			TurnkeyUiMainActivity.getInstance().onKeyDown(keyCode, event);
                break;
            case KeyMap.KEYCODE_MTKIR_CHDN:
            case KeyMap.KEYCODE_MTKIR_CHUP:
            case KeyMap.KEYCODE_MTKIR_PRECH:
                TurnkeyUiMainActivity.getInstance().onKeyDown(keyCode, event);
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
	};

	/*
	 * ignore page down/up key to fix DTV02032695
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyMap.KEYCODE_PAGE_DOWN:
		case KeyMap.KEYCODE_PAGE_UP:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dispatchKeyEvent||return");
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}
	// init show data info
	private void init() {
//		mFavLayout = (LinearLayout) findViewById(R.id.nav_ttx_favoriteList_layout);
		mFavListView = (ListView) findViewById(R.id.nav_favorite_listview);

		mFavListAdapter = new FavListAdapter(mContext, mFavlist);
		mFavListView.setAdapter(mFavListAdapter);
		mFavListView.setFocusable(true);
		mFavListView.requestFocus();
		mFavListView.setSelection(position);
		mFavListView.setOnItemClickListener(mItemClickListener);
	}


    private final OnItemClickListener mItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			handler.removeMessages(DISMISS_TAG);
			handler.sendEmptyMessageDelayed(DISMISS_TAG,TIME_OUT);
			int prepagenum = mTTXImpl.getCurrentTeletextPage().getPageNumber();
			//rm selected fav page/ add cur show page[if has rm old].  (focus not change)
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemClick arg2 = "+arg2);
			mTTXImpl.setTeletextPage(getFavPage(arg2));
			if(prepagenum == mTTXImpl.getCurrentTeletextPage().getPageNumber())
			{
				setFavPage(mTTXImpl.getCurrentTeletextPage(), arg2);
				mFavListAdapter.updateData(mFavlist);
				mFavListView.invalidateViews();
			}
		}
	};

	public void setWindowPosition() {
		WindowManager m = getWindow().getWindowManager();
		Display display = m.getDefaultDisplay();
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		int marginY = (int) (display.getHeight() * 0.0139);
		int marginX = (int) (display.getWidth() * 0.48);
		int menuWidth = (int) (display.getWidth() * 0.32);
		int menuHeight = (int) (display.getHeight() * 0.56);
		lp.width = menuWidth;
		lp.height = menuHeight;

		int x = display.getWidth() / 2 - menuWidth / 2 - marginX;
		int y = (int) (display.getHeight() / 2) - marginY - menuHeight / 2;
		lp.x = x;
		lp.y = y;
		window.setAttributes(lp);
	}

	public void onDismiss(DialogInterface dialog) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDismiss!!!!!!!!!");
		mContext = null;

	}

	@Override
	public void dismiss() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss!!!!!!!");
		/*if (AnimationManager.getInstance().getIsAnimation()) {
			AnimationManager.getInstance().channelListExitAnimation(mFavLayout,
					new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);
							TTXFavListDialog.super.dismiss();
						}
					});
		} else {*/
			handler.removeMessages(DISMISS_TAG);
			super.dismiss();
		//}
	}



	class FavListAdapter extends BaseAdapter {
		private final static String TAG = "FavListAdapter.FavListAdapter";
        private final Context mContext;
        private final LayoutInflater mInflater;
        private MtkTvTeletextPageBase[] mcurrentFavList;

		public FavListAdapter(Context context,
				MtkTvTeletextPageBase[] mcurrentChannelList) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			this.mcurrentFavList = mcurrentChannelList;
		}

        @Override
        public int getCount() {
            int count = 0;
            if (mcurrentFavList != null) {
                count = mcurrentFavList.length;
            }
            return count;
        }

        @Override
        public MtkTvTeletextPageBase getItem(int position) {
            return mcurrentFavList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
		}

		public void updateData(
				MtkTvTeletextPageBase[] mcurrentChannelList) {
			this.mcurrentFavList = mcurrentChannelList;
			notifyDataSetChanged();
		}

        @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder hodler;
			MtkTvTeletextPageBase mCurrentPage;
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.nav_ttx_fav_item, null);
				hodler = new ViewHolder();
				hodler.mTextView = (TextView) convertView
						.findViewById(R.id.nav_ttx_fav_item_txt);
				convertView.setTag(hodler);
			} else {
				hodler = (ViewHolder) convertView.getTag();
			}
			mCurrentPage = mcurrentFavList[position];
			/*if (mCurrentPage.isNormalPageHasName()) {
				hodler.mTextView.setText(mCurrentPage.getNormalPageName());

			} else {*/

			if(mCurrentPage != null){
				hodler.mTextView.setText(Integer.toHexString(mCurrentPage.getPageNumber()));

			}else{
				hodler.mTextView.setText("");

			}
						//.getNormalPageAddr()

			//}

			return convertView;
		}

		private class ViewHolder {
			TextView mTextView;
		}

	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {			
			super.handleMessage(msg);
			dismiss();
		}
		
	};
	

}
