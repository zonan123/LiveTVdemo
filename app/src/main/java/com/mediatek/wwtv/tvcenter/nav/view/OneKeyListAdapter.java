package com.mediatek.wwtv.tvcenter.nav.view;

//import java.util.ArrayList;
import java.util.List;
import com.mediatek.wwtv.tvcenter.nav.view.OneKeyMenuDialog.OneKeyAction;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.mediatek.wwtv.tvcenter.R;

public class OneKeyListAdapter extends
		RecyclerView.Adapter<OneKeyListAdapter.ViewHolder> {
	private static final String TAG = "OneKeyListAdapter";
	private Context mContext;
	private List<OneKeyAction> mList;

	public OneKeyListAdapter(Context context, List<OneKeyAction> list) {
		mContext = context;
		mList = list;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemCount() {
		return mList.size();
	}

	@Override
	@NonNull
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup arg0, int arg1) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.one_key_list_item, arg0, false);
		final ViewHolder viewHolder = new ViewHolder(view);
		viewHolder.keyView.setOnFocusChangeListener(new MyFocusListner());
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder arg0, int arg1) {
		OneKeyAction mOneKeyAction = mList.get(arg1);
		arg0.keyName.setText(mOneKeyAction.getOneKeyName());
		arg0.keyIcon.setBackground(mOneKeyAction.getOneKeyIcon());
		arg0.keyView.setTag(arg1);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		View keyView;
		TextView keyName;
		ImageView keyIcon;

		ViewHolder(View arg0) {
			super(arg0);
			keyView = arg0;
			keyName = arg0.findViewById(R.id.one_key_name);
			keyIcon = arg0.findViewById(R.id.one_key_icon);
		}
	}
	
	private class MyFocusListner implements View.OnFocusChangeListener {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			Log.d(TAG, "onFocusChange||arg1 =" + arg1);
			Drawable drawable = arg1 ? mContext.getResources().getDrawable(
					R.drawable.one_key_action_focused) : mContext.getResources()
					.getDrawable(R.drawable.one_key_action_normal);
			arg0.setBackground(drawable);
		}
	}

}
