
package com.mediatek.wwtv.tvcenter.nav.inputpanel;

import android.content.Context;
import android.graphics.PorterDuff;
import android.media.tv.TvInputManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
import com.mediatek.wwtv.tvcenter.nav.input.HdmiInput;


import java.util.ArrayList;
import java.util.List;

/**
 * @author sin_yupengwang
 */
public class InputsAdapter extends RecyclerView.Adapter<InputsAdapter.InputsViewHolder> {

  public final static String TAG = "InputsAdapter";
  private Context mContext;
  private List<AbstractInput> datas = new ArrayList<AbstractInput>();
  private int mDefaultColor;
  private int mDisconnectedStateColor;
  private OnItemClickListerner onItemClickListerner;
  private boolean isFVP = false;

  public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner) {
    this.onItemClickListerner = onItemClickListerner;
  }

  public InputsAdapter(Context context, List<AbstractInput> datas) {
    mContext = context;
    this.datas = datas;
    mDefaultColor = mContext.getColor(R.color.input_label_default_text_color);
    mDisconnectedStateColor = mContext.getColor(R.color.input_icon_disconnected_tint);
    isFVP = com.mediatek.wwtv.tvcenter.util.CommonUtil.isSupportFVP(true);
  }

  @Override
  public InputsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_inputs_item, parent,
        false);
    return new InputsViewHolder(root);
  }

  public void refresh(List<AbstractInput> datas) {
    this.datas = datas;
    notifyDataSetChanged();
  }

  public int getSelectPosition(int hardwareId) {
    int result = 0;
    for (int i = 0; i < datas.size(); i++) {
      AbstractInput input = datas.get(i);
      if (input != null) {
        if(input.getHardwareId() == hardwareId){
          result = i;
          break;
        }
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getSelectPosition:"+result);
    return result;
  }

  @Override
  public void onBindViewHolder(InputsViewHolder holder, int position) {
    AbstractInput input = getItem(position);
    int summaryVisible = View.GONE;
    if (input.isTV() || input.isDTV() || input.isATV()) {
      if(isFVP) {
        holder.icon.setImageResource(R.drawable.freeviewmonof);
      } else {
        holder.icon.setImageResource(R.drawable.ic_icon_32dp_livetv);
      }
    } else if (input.isComponent()) {
      holder.icon.setImageResource(R.drawable.ic_icon_32dp_component);
    } else if (input.isComposite()) {
      holder.icon.setImageResource(R.drawable.ic_icon_32dp_composite);
    } else if (input.isVGA()) {
      holder.icon.setImageResource(R.drawable.ic_icon_32dp_vga);
    } else if (input.isHDMI()) {
      HdmiInput hdmiInput = (HdmiInput) input;
      if (hdmiInput.isCEC()) {
        summaryVisible = View.VISIBLE;
        holder.summary.setText(hdmiInput.getParentHDMISourceName(mContext));
        holder.icon.setImageResource(R.drawable.ic_icon_32dp_playback);
      } else {
        holder.icon.setImageResource(R.drawable.ic_icon_32dp_hdmi);
      }
    } else if(input.isTVHome()) {
      holder.icon.setImageResource(R.drawable.ic_home);
    }
    holder.icon.setColorFilter(mDisconnectedStateColor, PorterDuff.Mode.SRC_IN);
    holder.summary.setVisibility(summaryVisible);
    holder.title.setText(getSourceName(input));

    holder.root.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          holder.icon.setBackgroundResource(R.drawable.hollow_circle_input_background_black);
          holder.icon.setColorFilter(mDefaultColor, PorterDuff.Mode.SRC_IN);
        } else {
          holder.icon.setBackgroundResource(R.drawable.filled_circle_input_background_black);
          holder.icon.setColorFilter(mDisconnectedStateColor, PorterDuff.Mode.SRC_IN);
        }
        holder.title.setSelected(hasFocus);
        holder.summary.setSelected(hasFocus);
      }
    });
    if(input.getState() == TvInputManager.INPUT_STATE_CONNECTED) {
      holder.title.setTextColor(mDefaultColor);
    } else {
      holder.title.setTextColor(mDisconnectedStateColor);
    }
  }

  @Override
  public int getItemCount() {
    return datas.size();
  }

  public class InputsViewHolder extends RecyclerView.ViewHolder {
    ImageView icon;
    TextView title;
    TextView summary;
    FrameLayout root;

    public InputsViewHolder(View rootView) {
      super(rootView);
      root = (FrameLayout) rootView.findViewById(R.id.input_icon_root);
      icon = (ImageView) rootView.findViewById(R.id.icon);
      title = (TextView) rootView.findViewById(R.id.title);
      summary = (TextView) rootView.findViewById(R.id.summary);
      root.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          if(onItemClickListerner != null) {
            onItemClickListerner.onItemClick(v, InputsViewHolder.this.getLayoutPosition());
          }
        }
      });
    }
  }

  public AbstractInput getItem(int position) {
    return datas.get(position);
  }

  public String getSourceName(AbstractInput input) {
    String customLabel = input.getCustomSourceName(mContext);
    if (TextUtils.isEmpty(customLabel) || TextUtils.equals(customLabel, "null")) {
      return input.getSourceName(mContext);
    }
    return customLabel;
  }

  public interface OnItemClickListerner {
    void onItemClick(View v, int position);
  }
}
