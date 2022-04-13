package com.mediatek.wwtv.tvcenter.oad;

import java.util.List;
import com.mediatek.wwtv.tvcenter.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OadActionListAdapter extends BaseAdapter {
	private List<String> mList;
	LayoutInflater inflater;

	public OadActionListAdapter(Context context, List<String> list) {
		super();
		Context mContext = context;
		mList = list;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ActionHolder actionHolder;
		if (arg1 == null) {
			actionHolder = new ActionHolder();
			arg1 = inflater.inflate(R.layout.oad_action_list_item, arg2, false);
			actionHolder.actionName = (TextView) arg1
					.findViewById(R.id.oad_action_name);
			arg1.setTag(actionHolder);
		} else {
			actionHolder = (ActionHolder) arg1.getTag();
		}
		actionHolder.actionName.setText(mList.get(arg0));
		return arg1;
	}

	public class ActionHolder {
		public TextView actionName;
	}
}
