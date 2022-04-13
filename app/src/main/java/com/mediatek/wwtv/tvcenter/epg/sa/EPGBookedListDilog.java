package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.sa.db.EPGBookListViewDataItem;
import com.mediatek.wwtv.tvcenter.epg.sa.db.DBMgrProgramList;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

/**
 * booked list
 * @author sin_xinsheng
 *
 */
public class EPGBookedListDilog extends Dialog{
	private Context mContext;
	private ListView mBookListView;
	private EPGBookListAdapter mEPGBookListAdapter;
	private DBMgrProgramList mDBMgrProgramList;
	private List<EPGBookListViewDataItem> mBookedList;

	public EPGBookedListDilog(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}

	public EPGBookedListDilog(Context context) {
		super(context, R.style.Theme_EpgSubTypeDialog);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.epg_book_list_main);
		setWindowPosition();
		mBookListView = (ListView) findViewById(R.id.nav_epg_book_list);
		mBookListView.setDividerHeight(0);
		mDBMgrProgramList = DBMgrProgramList.getInstance(mContext);
//		mDBMgrProgramList.getReadableDB();
//		mBookedList = mDBMgrProgramList.getProgramList();
//		mDBMgrProgramList.closeDB();
		if (mBookedList == null) {
			mBookedList = new ArrayList<EPGBookListViewDataItem>();
		}
		mEPGBookListAdapter = new EPGBookListAdapter(mContext, mBookListView, mBookedList);
		mBookListView.setAdapter(mEPGBookListAdapter);
		mBookListView.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					mEPGBookListAdapter.onKey(v, keyCode);
				}
				return false;
			}

		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_0:
			if (this.isShowing()) {
				dismiss();
				saveChangeProgramList();
				EPGSaActivity epgActivity = (EPGSaActivity)mContext;
				epgActivity.changeBottomViewText(false, 0);
				epgActivity.notifyEPGLinearlayoutRefresh();
			}
			break;
		case KeyMap.KEYCODE_MTKIR_SOURCE:
			return true;
		default:
			break;
		}

		if (keyCode == KeyMap.KEYCODE_VOLUME_UP
				|| keyCode == KeyMap.KEYCODE_VOLUME_DOWN) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private static int menuWidth = 800;
	private static int menuHeight = 610;
	public void setWindowPosition() {
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		menuWidth = 650*ScreenConstant.SCREEN_WIDTH /1280;
		menuHeight = 610*ScreenConstant.SCREEN_HEIGHT/720;
		lp.width = menuWidth;
		lp.height = menuHeight;
		window.setAttributes(lp);
	}

	public void updateAdapter() {
		mDBMgrProgramList.getWriteableDB();
		mBookedList = mDBMgrProgramList.getProgramListWithDelete();
		mDBMgrProgramList.closeDB();
		mEPGBookListAdapter.setEPGBookList(mBookedList);
		mEPGBookListAdapter.notifyDataSetChanged();
		if (mBookedList != null && !mBookedList.isEmpty()) {
			mBookListView.setFocusable(true);
			mBookListView.requestFocus();
			mBookListView.setSelection(0);
		}
	}

	private void saveChangeProgramList() {
		mDBMgrProgramList.getWriteableDB();
//		mBookedList = mDBMgrProgramList.getProgramList();
		for (EPGBookListViewDataItem tempInfo:mBookedList) {
			if (!tempInfo.marked) {
				mDBMgrProgramList.deleteProgram(tempInfo);
				SaveValue.getInstance(mContext).removekey(tempInfo.mProgramStartTime + "");
			}
		}
		mDBMgrProgramList.closeDB();
	}

}
