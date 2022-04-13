package com.mediatek.wwtv.tvcenter.nav.fav;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;

public class ChannelAdapter extends BaseAdapter {
	/*
	 * context
	 */
	private final LayoutInflater mInflater;
    private List<MtkTvChannelInfoBase> mcurrentChannelList;
	private int favlistMovepostion = -1; 
	private static final int FAVOURITE_1 = 0;
    private static final int FAVOURITE_2 = 1;
    private static final int FAVOURITE_3 = 2;
    private static final int FAVOURITE_4 = 3;
    private int cURRENTFAVOURITETYPE = FAVOURITE_1;

  public ChannelAdapter(Context context, List<MtkTvChannelInfoBase> mcurrentChannelList) {
		mInflater = LayoutInflater.from(context);
		this.mcurrentChannelList = mcurrentChannelList;
		favlistMovepostion = -1;
	}

  public List<MtkTvChannelInfoBase> getChannellist() {
    return mcurrentChannelList;
  }

	@Override
  public int getCount() {
		int s = 0;
		if (mcurrentChannelList != null) {
			s = mcurrentChannelList.size();
		}
		return s;
	}

  @Override
  public MtkTvChannelInfoBase getItem(int position) {
		return mcurrentChannelList.get(position);
	}
  
  public void setFavlistMovepostion(int favlistMovepostion) {
      this.favlistMovepostion=favlistMovepostion;
    }
  
  public void setCurrentFavouriteType(int cURRENTFAVOURITETYPE) {
      this.cURRENTFAVOURITETYPE=cURRENTFAVOURITETYPE;
    }
  
	@Override
  public long getItemId(int position) {
		return position;
	}

  public int isExistCh(int chId) {
    if (mcurrentChannelList != null) {
      int size = mcurrentChannelList.size();
      for (int index = 0; index < size; index++) {
        if (mcurrentChannelList.get(index).getChannelId() == chId) {
          return index;
        }
      }
    }
    return -1;
  }

  public void updateData(List<MtkTvChannelInfoBase> mcurrentChannelList) {
		this.mcurrentChannelList = mcurrentChannelList;
		notifyDataSetChanged();
	}
  
  public void updateView(View view ,int index,int visible) {
    if(view == null){
      return;  
    }
    this.favlistMovepostion= index;
    ViewHolder hodler=(ViewHolder) view.getTag();
/*    hodler.mChannelNumberTextView = (TextView) hodler.findViewById(R.id.nav_channel_list_item_NumberTV);
    hodler.mChannelNameTextView = (TextView) hodler.findViewById(R.id.nav_channel_list_item_NameTV);
    hodler.mFavListMove = (ImageView) hodler.findViewById(R.id.nav_channel_list_move_icon);*/
    hodler.mFavListMove.setVisibility(visible);
  }
	@Override
  public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder hodler;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.nav_favchannel_item, null);
			hodler = new ViewHolder();
			hodler.mChannelNumberTextView = (TextView) convertView
					.findViewById(R.id.nav_channel_list_item_NumberTV);
			hodler.mChannelNameTextView = (TextView) convertView
					.findViewById(R.id.nav_channel_list_item_NameTV);
			hodler.mFavListMove= (ImageView) convertView
                    .findViewById(R.id.nav_channel_list_move_icon);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHolder) convertView.getTag();
		}
    MtkTvChannelInfoBase mCurrentChannel = mcurrentChannelList.get(position);
    if (mCurrentChannel instanceof MtkTvATSCChannelInfo) {
      MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) mCurrentChannel;
      hodler.mChannelNumberTextView.setText(tmpAtsc.getMajorNum() + "-" + tmpAtsc.getMinorNum());
    } else if (mCurrentChannel instanceof MtkTvISDBChannelInfo) {
      MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) mCurrentChannel;
      hodler.mChannelNumberTextView.setText(tmpIsdb.getMajorNum() + "-" + tmpIsdb.getMinorNum());
    } else {
        switch (cURRENTFAVOURITETYPE) {
        case FAVOURITE_1:
            hodler.mChannelNumberTextView.setText("" + mCurrentChannel.getFavorites1Index());
            break;
        case FAVOURITE_2:
            hodler.mChannelNumberTextView.setText("" + mCurrentChannel.getFavorites2Index());
            break;
        case FAVOURITE_3:
            hodler.mChannelNumberTextView.setText("" + mCurrentChannel.getFavorites3Index());
            break;
       case FAVOURITE_4:
            hodler.mChannelNumberTextView.setText("" + mCurrentChannel.getFavorites4Index());
            break;
       default:
           hodler.mChannelNumberTextView.setText("" + mCurrentChannel.getFavorites1Index());
           break;
    }
    
    }
    hodler.mChannelNameTextView.setText(TvSingletons.getSingletons().getCommonIntegration().
            getAvailableString(mCurrentChannel.getServiceName()));
    
    if(favlistMovepostion != -1 && favlistMovepostion == position){
       hodler.mFavListMove.setVisibility(View.VISIBLE); 
    }else{
        hodler.mFavListMove.setVisibility(View.INVISIBLE);  
    }
		return convertView;
	}

	private class ViewHolder {
		TextView mChannelNumberTextView;
		TextView mChannelNameTextView;
		ImageView mFavListMove;
	}
}