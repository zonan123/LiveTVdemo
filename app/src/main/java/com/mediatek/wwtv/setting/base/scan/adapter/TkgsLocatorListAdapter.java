
package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.List;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter.ViewHolder;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.ActionAdapter.Listener;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapter;
import com.mediatek.wwtv.setting.widget.view.ScrollAdapterBase;
import com.mediatek.wwtv.tvcenter.R;


public class TkgsLocatorListAdapter extends BaseAdapter implements ScrollAdapter,
  View.OnKeyListener {
  private static final String TAG = "TkgsLocatorListAdapter";
  private Context mContext;
  public List<TkgsLocatorItem> mList;
  String[] onOffStr;
  private Listener mListener;

  public TkgsLocatorListAdapter(Context mContext, List<TkgsLocatorItem> mList) {
    super();
    this.mContext = mContext;
    this.mList = mList;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mList.size==" + mList.size());
  }

  public void setListener(Listener ler) {
    mListener = ler;
  }

  @Override
  public void viewRemoved(View view) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "viewRemoved");
  }

  @Override
  public View getScrapView(ViewGroup parent) {
    LayoutInflater inflater = LayoutInflater.from(mContext);
    return inflater.inflate(R.layout.menu_biss_list_item, parent, false);
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
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    TkgsLocatorItem item = mList.get(position);
    item.listPos = position;
    if (convertView == null) {
      holder = new ViewHolder();
      convertView = getScrapView(parent);
      holder.numText = (TextView) convertView.findViewById(R.id.biss_num);
      holder.threePryText = (TextView) convertView.findViewById(R.id.biss_three_pry);
      holder.progIdText = (TextView) convertView.findViewById(R.id.biss_prog_id);
      holder.cwKeyText = (TextView) convertView.findViewById(R.id.biss_cwkey);
      holder.scanIcon = (ImageView) convertView.findViewById(R.id.biss_scan_icon);
      convertView.setTag(holder);

    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.scanIcon.setVisibility(View.GONE);
    if (item.isAddOrDelKey) {
      holder.numText.setVisibility(View.GONE);
      holder.progIdText.setVisibility(View.GONE);
      holder.cwKeyText.setVisibility(View.GONE);
      holder.threePryText.setText(item.getTitle());
    } else {
      holder.progIdText.setVisibility(View.VISIBLE);
      holder.cwKeyText.setVisibility(View.GONE);
      holder.numText.setVisibility(View.VISIBLE);
      holder.numText.setText(String.valueOf(position + 1));
      holder.threePryText.setText(item.threePry);
      holder.progIdText.setText(String.valueOf(item.progId));
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "item.isEnabled():" + item.isEnabled());
    convertView.setAlpha(item.isEnabled() ? 1.0f : 0.5f);
    convertView.setTag(R.id.biss_cwkey, item);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "threePry:" + item.threePry);
    convertView.setOnKeyListener(this);
    return convertView;
  }

  int selNum;

  public int getSelectItemNum() {
    return selNum;
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKey------" + keyCode);
    if (v == null) {
      return false;
    }
    boolean handled = false;
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_NUMPAD_ENTER:
      case KeyEvent.KEYCODE_ENTER:
        if (event.getAction() == 0) {
          final TkgsLocatorItem item = (TkgsLocatorItem) v.getTag(R.id.biss_cwkey);
          selNum = item.listPos;
          if (mListener != null && item.isEnabled()) {
            mListener.onActionClicked(item);
          }
          handled = true;
        }
        break;
      default:
        break;
    }
    return handled;
  }


  @Override
  public ScrollAdapterBase getExpandAdapter() {
    return null;
  }

  class ViewHolder {
    TextView numText;
    TextView threePryText;
    TextView progIdText;
    TextView cwKeyText;
    ImageView scanIcon;
  }

  public static class TkgsLocatorItem extends Action {
    // for num 1,2,3...
    public int bnum;
    // for progid
    public int progId;
    // for freq & ploazation & symbolrate
    public String threePry;

    // add cw key is a special item
    public boolean isAddOrDelKey;
    public int listPos;

    public TkgsLocatorItem(int bnum, int progId, String threePry, String title) {
      super(MenuConfigManager.TKGS_LOC_ITEM, title, Action.DataType.TKGSLOCITEMVIEW);
      this.bnum = bnum;
      this.progId = progId;
      this.threePry = threePry;
      isAddOrDelKey = false;
      this.setEnabled(true);
    }

    public TkgsLocatorItem(boolean isAddKey, String opid, String title, Action.DataType type) {
      super(opid, title, type);
      progId = -1;
      this.isAddOrDelKey = isAddKey;
      this.setEnabled(true);

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TkgsLocatorItem) {
            TkgsLocatorItem other = (TkgsLocatorItem) o;
            return (this.progId == -1 && this.progId == other.progId) ||
                    (this.progId == other.progId && this.threePry != null && this.threePry.equals(other.threePry));

        }
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hashCode");
        return super.hashCode();
    }
    

  }

}
