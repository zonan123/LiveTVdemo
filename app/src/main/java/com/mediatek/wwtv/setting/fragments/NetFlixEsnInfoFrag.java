
package com.mediatek.wwtv.setting.fragments;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.SaveValue;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

/**
 * setup_version_info
 *
 * @author hs_haosun
 */
public class NetFlixEsnInfoFrag extends Fragment {


  private Context context;
  private ViewGroup mRootView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    context = getActivity();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mRootView = (ViewGroup) inflater.inflate(R.layout.netflix_esn_info,
        null);
    init();
    return mRootView;
  }

  public void init() {
    TextView esnName;
    TextView esnNumber;
    esnName = (TextView) mRootView.findViewById(R.id.netflix_esn_info_name);
    esnNumber = (TextView) mRootView.findViewById(R.id.netflix_esn_info_number);
    esnName.setText(context.getResources().getString(R.string.nav_ens_name));
    String nummber = "QWERTYUIOP=DFGHJ567889";
    nummber += "\n" + "QWERTYUIOP=DFGHJ567889=ERE44=DFGHJK-TYUITESD45567F4";
    nummber += "\n" + "QWERTYUIOP=DFGHJ567889=ERE44=DFGHJK-TYUITESD45567F4";
    nummber += "\n" + "QWERTYUIOP=DFGHJ567889=ERE44=DFGHJK-TYUITESD45567F4";
    esnNumber.setText(SaveValue.getInstance(context).readStrValue("ESNStr"));
    // esnNumber.setText(nummber);
  }

}
