package com.mediatek.wwtv.setting.base.scan.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvNwlBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvNwlInfoBase;
import com.mediatek.wwtv.setting.base.scan.adapter.ThirdItemAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.ThirdItemAdapter.ThirdItem;
import com.mediatek.wwtv.setting.base.scan.adapter.ThirdItemAdapter.ThirdItem.OnValueChangeListener;
import com.mediatek.wwtv.setting.base.scan.model.APTargetRegion;
import com.mediatek.wwtv.setting.base.scan.model.IRegionChangeInterface;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.model.ScannerManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.setting.util.TVContent;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.OneBatData;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.TKGSOneSvcList;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.LcnConflictGroup;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.ScanDvbtRet;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.TargetRegion;
import com.mediatek.wwtv.tvcenter.R;


public class ScanThirdlyDialog extends Dialog {
    private static String TAG = "ScanThirdlyDialog";
    Context mContext;
    ListView trdItemsListView;
    RelativeLayout trdRootLayout;
    TVContent mTV ;
    ThirdItemAdapter tRDAdapter;
    int whichViewType  = 0;
    public boolean positionCenter = false;
    public ScanThirdlyDialog(Context context,int whichView){
        super(context, R.style.nav_dialog);
        mContext = context;
        mTV = TVContent.getInstance(mContext);
        whichViewType = whichView;
        switch(whichView){
            case 1:
                this.showTRDFavNetWorkChoices();
                break;
            case 2:
                showTRDSelectRegion();
                break;
            case 3:
                this.showTRDConflictChannels();
                break;
            case 4:
                this.showTRDLCNv2Choices();
                break;
            case 5:
                this.showOrderChoices();
                break;
			case 6:
                this.showFvpUserSelectionChoices();
                break;
            default:
                break;
        }
        if(!positionCenter){
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            Display display = getWindow().getWindowManager().getDefaultDisplay();
            lp.gravity = Gravity.START | Gravity.TOP;
            lp.x = (int) (display.getWidth() * 0.625 - mContext.getResources().getDimensionPixelSize(R.dimen.scan_third_dialog_width)) / 2 + 10;
            if(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL){
                lp.x = display.getWidth() - lp.x - mContext.getResources().getDimensionPixelSize(R.dimen.scan_third_dialog_width);
            }
            lp.y = (int)(display.getHeight() * 0.4);
            getWindow().setAttributes(lp);
        }
    }

  /**
   * @param context
   * @param whichDVBSOP:1 == FRANSAT,2 == TKGS
   * @param satID
   */
  public ScanThirdlyDialog(Context context, int whichDVBSOP, int satID) {
        super(context, R.style.nav_dialog);
        mContext = context;
        mTV = TVContent.getInstance(mContext);
        switch (whichDVBSOP) {
            case 1:
                this.showTRDDVBSBATChoices(satID);
                break;
            case 2:
                this.showTRDDVBSTKGSServiceList(satID);
                break;
            case 3:
                this.showTricolorChannelList();
                break;
            case 4:
                this.showFastScanOperatorList();
                break;
            default:
                break;
        }

    }

    /**
     * country=Russia, operator=Tricolor
     * @param satID
     */
    private void showTricolorChannelList() {
      LayoutInflater inflater = LayoutInflater.from(mContext);
      View view = inflater.inflate(
          R.layout.menu_main_scan_trd_fav_network2, null);
      trdItemsListView = (ListView) view
          .findViewById(R.id.scan_fav_network_list);
      TextView tview = (TextView) view.findViewById(R.id.scan_fav_network_str);
      tview.setText(R.string.nav_channel_list);

      MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
      MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan.dvbsGetNfyGetInfo();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTricolorChannelList ret:"+dvbsRet);
      final List<String> strings = new ArrayList<String>();
      final List<Integer> ids = new ArrayList<Integer>();
      if(dvbsRet == MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK
          && dvbsScan.nyfGetInfo_list != null
          && dvbsScan.nyfGetInfo_list.length == dvbsScan.nfyGetInfo_lstNfyNum) {
        for(int i=0; i < dvbsScan.nfyGetInfo_lstNfyNum; i++) {
          MtkTvScanDvbsBase.OneRECNfyData data = dvbsScan.nyfGetInfo_list[i];
          strings.add(data.recName);
          ids.add(data.recId);
        }
      }
      if(strings.isEmpty() || ids.isEmpty()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showTricolorChannelList strings isEmpty:");
        return;
      }

      trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext, R.layout.lcn_conflict_item, strings));
      trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          if(mContext instanceof ScanViewActivity) {
            ScanViewActivity activity = (ScanViewActivity) mContext;
            activity.reStartDVBSFullScanAfterTricolorChannelList(ids.get(position));
          }
          dismiss();
        }
      });
      trdItemsListView.setSelection(0);
      trdItemsListView.requestFocus();
      getWindow().setContentView(view);
      getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
    

    private void showFastScanOperatorList() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(
            R.layout.menu_main_scan_trd_fav_network2, null);
        trdItemsListView = (ListView) view
            .findViewById(R.id.scan_fav_network_list);
        TextView tview = (TextView) view.findViewById(R.id.scan_fav_network_str);
        tview.setText(mContext.getString(R.string.dvbs_fast_scan_operator_list));

        final MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
        MtkTvScanDvbsBase.ScanDvbsRet dvbsRet = dvbsScan.dvbsM7GetOptList();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFastScanOperatorList ret:"+dvbsRet);
        final List<String> strings = new ArrayList<String>();
        final List<String> showStrings = new ArrayList<String>();
        final List<Integer> nwids = new ArrayList<Integer>();
        final List<Integer> optSLIds = new ArrayList<Integer>();
        final List<MtkTvScanDvbsBase.M7OneOPT> opts = new ArrayList<>();

       if(dvbsRet == MtkTvScanDvbsBase.ScanDvbsRet.SCAN_DVBS_RET_OK
            && dvbsScan.M7_OptLists != null
            && dvbsScan.M7_OptLists.length == dvbsScan.M7_OptNum) {
          for(int i=0; i < dvbsScan.M7_OptNum; i++) {
            MtkTvScanDvbsBase.M7OneOPT data = dvbsScan.M7_OptLists[i];
            strings.add(data.subOptName);
            showStrings.add(data.subOptName+" (" + data.optName + ")");
            nwids.add(data.optNWId);
            optSLIds.add(data.optSLId);
            opts.add(data);
          }
        }
        if(strings.isEmpty() || nwids.isEmpty()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFastScanOperatorList strings or nwids isEmpty!");
          return;
        }
        trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext, R.layout.lcn_conflict_item, showStrings));
        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mContext instanceof ScanViewActivity) {
                if(ScanContent.checkFastScanOrbitsError(opts.get(position), showStrings.get(position), ScanContent.getDVBSEnablesatellites(mContext))){
                    ScanContent.getDvbsFastScanOrbitErrorDialog(mContext, opts.get(position), showStrings.get(position)).show();
                }else {
                    ScanViewActivity activity = (ScanViewActivity) mContext;
                    dvbsScan.dvbsM7SelPrefOpt(nwids.get(position), optSLIds.get(position));
                    boolean spclRegnSetup = dvbsScan.M7_OptLists[position].spclRegnSetup;
                    activity.restartFastDVBSFullScan(spclRegnSetup, strings.get(position));
                }
            }
            dismiss();
          }
        });
        trdItemsListView.setSelection(0);
        trdItemsListView.requestFocus();
        getWindow().setContentView(view);
        getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      }

    /**
     * UI Module: GBR
     */
    public void showTRDSelectRegion() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDSelectRegion()");

//      trdRootLayout.setVisibility(View.VISIBLE);
//
//      trdRootLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(
                R.layout.menu_main_scan_trd_fav_network2, null);

        TextView titleView = (TextView) view
                .findViewById(R.id.scan_fav_network_str);
        titleView.setText(mContext.getString(R.string.select_region_title));

        trdItemsListView = (ListView) view
                .findViewById(R.id.scan_fav_network_list);

        List<ThirdItem> items = prepareTRDSelectRegionAction(mContext,
                mTV.getScanManager(), 0, 0, 0);

        mTV.getScanManager().getRegionMgr()
                .setOnRegionChangeListener(new IRegionChangeInterface() {

                    @Override
                    public void onRegionChange(List<ThirdItem> items) {
                        int position = trdItemsListView
                                .getSelectedItemPosition();
                        reMapRegions(items);
                        trdItemsListView.setSelection(position);
                    }
                });

        reMapRegions(items);

        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
                saveAfterScanGBRRegionInfo();
                trdItemsListView.setSelection(position);
                if(mContext instanceof ScanDialogActivity){
                ((ScanDialogActivity)mContext).showCompleteInfo();
                }
                dismiss();
            }
        });

        trdItemsListView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
                        saveAfterScanGBRRegionInfo();
                        if(mContext instanceof ScanDialogActivity){
                        ((ScanDialogActivity)mContext).showCompleteInfo();
                        }
                        dismiss();
                    }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"region to left");
                        tRDAdapter.optionTurnLeft(trdItemsListView.getSelectedView(),null);
                    }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"region to right");
                        tRDAdapter.optionTurnRight(trdItemsListView.getSelectedView(),null);
                    }
                }
                return false;
            }
        });
        trdItemsListView.setSelection(0);
        trdItemsListView.requestFocus();
        getWindow().setContentView(view);
    }

    private void reMapRegions(List<ThirdItem> items) {

        tRDAdapter = new ThirdItemAdapter(mContext,items);
        trdItemsListView.setAdapter(tRDAdapter);
    }

    /**
     * prepare Regions Scan parameter.
     */
    public void saveAfterScanGBRRegionInfo() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveAfterScanGBRRegionInfo(),");

        ThirdItem temp  = (ThirdItem)tRDAdapter.getItem(0);
        String region1 = temp.optionValues[temp.optionValue];
        temp  = (ThirdItem)tRDAdapter.getItem(1);
        String region2 = temp.optionValues[temp.optionValue];
        temp  = (ThirdItem)tRDAdapter.getItem(2);
        String region3 = temp.optionValues[temp.optionValue];
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"region1: " + region1);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"region2: " + region2);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"region3: " + region3);

        TargetRegion region= getTaragetRegionObj(region1,region2,region3);

        if(region!=null){
            ScanDvbtRet  rect=MtkTvScan
                    .getInstance().getScanDvbtInstance().uiOpSetTargetRegion(region);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveAfterScanGBRRegionInfo(),"+rect.name());
        }else
        {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveAfterScanGBRRegionInfo(),"+"TargetRegion=====null");
        }
    }

    private TargetRegion getTaragetRegionObj(String level1,String level2,String level3){

        MtkTvScanDvbtBase.TargetRegion[] regionList = MtkTvScan
                .getInstance().getScanDvbtInstance().uiOpGetTargetRegion();
        int level=3;
        String name=level3;

        String noRegion=mContext.getString(R.string.scan_trd_uk_reg_reg_nodefine);
        if(level3.equalsIgnoreCase(noRegion)){
            level=2;
            name=level2;
        }
        if(level2.equalsIgnoreCase(noRegion)){
            level=1;
            name=level1;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,String.format("level:%d,name:%s", level,name));
        for(MtkTvScanDvbtBase.TargetRegion region : regionList){
            if(region.level==level && region.name.equalsIgnoreCase(name)){
                return region;
            }
        }
        return null;
    }

    public List<ThirdItem> prepareTRDSelectRegionAction(final Context context,final ScannerManager scanMgr,int level1,int level2,int level3) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"prepareTRDSelectRegionAction,"+String.format("level1:%d,level2:%d,level3:%d",level1,level2,level3));
        level1=Math.max(0, level1);
        level2=Math.max(0, level2);
        level3=Math.max(0, level3);

        List<ThirdItem> items = new ArrayList<ThirdItem>();

        Map<Integer, APTargetRegion> regions= scanMgr.getRegionsOfGBR();

        final List<APTargetRegion> leveL1RegionsObj = new ArrayList<APTargetRegion>();
        leveL1RegionsObj.addAll(regions.values());
        final int level1Int=Math.min(level1, leveL1RegionsObj.size());
        
        final List<APTargetRegion> leveL2RegionsObj = new ArrayList<APTargetRegion>();
        if(!leveL1RegionsObj.isEmpty()){
            leveL2RegionsObj.addAll(leveL1RegionsObj.get(level1).getChildren().values());
        }
        final int level2Int=Math.min(level2, leveL2RegionsObj.size());

        final List<APTargetRegion> leveL3RegionsObj = new ArrayList<APTargetRegion>();
        if(!leveL2RegionsObj.isEmpty()){
            leveL3RegionsObj.addAll(leveL2RegionsObj.get(level2).getChildren().values());
        }
        final int level3Int=Math.min(level3, leveL3RegionsObj.size());

        String[] level1Array=regionsObjToStringArray(context,leveL1RegionsObj);
        String[] level2Array=regionsObjToStringArray(context,leveL2RegionsObj);
        String[] level3Array=regionsObjToStringArray(context,leveL3RegionsObj);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"level1Array,"+Arrays.asList(level1Array).toString());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"level2Array,"+Arrays.asList(level2Array).toString());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"level3Array,"+Arrays.asList(level3Array).toString());

        String title1 = context.getString(R.string.scan_trd_uk_reg_reg_x, 1);
        ThirdItem level1Regions = new ThirdItem(MenuConfigManager.TV_CHANNEL_AFTER_SCAN_UK_REGION, title1,
                level1Int,level1Array,true);
        items.add(level1Regions);

        String title2 = context.getString(R.string.scan_trd_uk_reg_reg_x, 2);
        ThirdItem level2Regions = new ThirdItem(title2, title2,
            level2Int,level2Array, true);

        if(level2Array[0].equals(context.getString(R.string.scan_trd_uk_reg_reg_nodefine))){
            level2Regions.isEnable = false;
        }else{
            level2Regions.isEnable = true;
        }
        items.add(level2Regions);

        String title3 = context.getString(R.string.scan_trd_uk_reg_reg_x, 3);
        ThirdItem level3Regions = new ThirdItem(title3, title3,
                level3Int,level3Array, true);
        if(level3Array[0].equals(context.getString(R.string.scan_trd_uk_reg_reg_nodefine))){
            level3Regions.isEnable = false;
        }else{
            level3Regions.isEnable = true;
        }
        items.add(level3Regions);

        level1Regions.setValueChangeListener(new OnValueChangeListener() {

            @Override
            public void afterValueChanged(String afterName) {
                int indexLevel1=leveL1RegionsObj.indexOf(new APTargetRegion(-1,-1,-1,-1,-1,afterName));
                scanMgr.getRegionMgr().getOnRegionChangeListener().onRegionChange(prepareTRDSelectRegionAction(context,scanMgr,indexLevel1,0,0));
            }
        });

        level2Regions.setValueChangeListener(new OnValueChangeListener() {
            
            @Override
            public void afterValueChanged(String afterName) {
                int indexLevel2=leveL2RegionsObj.indexOf(new APTargetRegion(-1,-1,-1,-1,-1,afterName));
                scanMgr.getRegionMgr().getOnRegionChangeListener().onRegionChange(prepareTRDSelectRegionAction(context,scanMgr,level1Int,indexLevel2,0));
            }
        });

        return items;
    }

    private static String[] regionsObjToStringArray(
            Context context,List<APTargetRegion> leveL1RegionsObj) {
        String[] regions;
        if(leveL1RegionsObj!=null && !leveL1RegionsObj.isEmpty()){
            regions=new String[leveL1RegionsObj.size()];
            for(int i=0;i<regions.length;i++){
                regions[i]=leveL1RegionsObj.get(i).name;
            }
        }else
        {
            regions=new String[]{context.getString(R.string.scan_trd_uk_reg_reg_nodefine)};
        }
        return regions;
    }

    public void showTRDFavNetWorkChoices() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDFavNetWorkChoices()");

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(
                R.layout.menu_main_scan_trd_fav_network2, null);
        trdItemsListView = (ListView) view
                .findViewById(R.id.scan_fav_network_list);
        List<String> networkList = getTRDFavNetworkList();
        int selectedNwk = getSelectedFavNwkIndex(networkList);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectedNwk="+selectedNwk);
        trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext,R.layout.lcn_conflict_item,networkList));
        trdItemsListView.setSelection(selectedNwk);
        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

           @Override
           public void onItemClick(AdapterView<?> parent, View view,
                   int position, long id) {
	            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
	            setTRDFavNetwork();
//              trdRootLayout.setVisibility(View.INVISIBLE);
                dismiss();
            }
        });

        trdItemsListView.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
                    setTRDFavNetwork();
                    dismiss();
//                  trdRootLayout.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

        trdItemsListView.requestFocus();
        getWindow().setContentView(view);
    }

    public List<String> getTRDFavNetworkList() {
        List<String> networkList = new ArrayList<String>();
        MtkTvScanDvbtBase.FavNwk[] nwkList = MtkTvScan.getInstance()
                .getScanDvbtInstance().uiOpGetFavNwk();
        for(MtkTvScanDvbtBase.FavNwk nwk : nwkList){
            networkList.add(nwk.networkName);
        }
        Collections.sort(networkList, String.CASE_INSENSITIVE_ORDER);
        return networkList;
    }

    private void setTRDFavNetwork(){
        MtkTvScanDvbtBase.FavNwk[] nwkList = MtkTvScan.getInstance()
                .getScanDvbtInstance().uiOpGetFavNwk();
        List<MtkTvScanDvbtBase.FavNwk> networkList = new ArrayList<MtkTvScanDvbtBase.FavNwk>();
        for(MtkTvScanDvbtBase.FavNwk nwk : nwkList){
            networkList.add(nwk);
        }
        Collections.sort(networkList, new Comparator<MtkTvScanDvbtBase.FavNwk>() {

            @Override
            public int compare(MtkTvScanDvbtBase.FavNwk arg0, MtkTvScanDvbtBase.FavNwk arg1) {
                return arg0.networkName.compareToIgnoreCase(arg1.networkName);
            }
        });
        int selection=trdItemsListView.getSelectedItemPosition();
        selection=Math.max(0, selection);
        selection=Math.min(selection, networkList.size()-1);
        ScanDvbtRet rect=MtkTvScan
                .getInstance().getScanDvbtInstance().uiOpSetFavNwk(networkList.get(selection));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveAfterScanGBRRegionInfo(),"+rect.name()+","+networkList.get(selection).networkName);
    }

        private int getSelectedFavNwkIndex(List<String> networkList) {
            MtkTvNwlBase nwlBase = new MtkTvNwlBase();
            int num = nwlBase.getNwlNumRecs(1);
            for (int i = 0; i < num; i++) {
                MtkTvNwlInfoBase nwlInfoBase = nwlBase.getNwlRecordByRecIdx(1, i).get(0);//length is 1

                if (nwlInfoBase != null && (nwlInfoBase.getNwMask() & 4) != 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSelectedFavNwkIndex network name=" + nwlInfoBase.getServiceNwName());
                    for (int j = 0; j < networkList.size(); j++) {
                        if(networkList.get(j).startsWith(nwlInfoBase.getServiceNwName())){
                            return  j;
                        }
                    }
                }
            }
            return 0;
        }


    private int mCurrentGroupIndex = 0;
    /**
     * UI Module: LCN (ITA)
     */
    public void showTRDConflictChannels() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDConflictChannels()");
        autoDissmissTask(30);
        //trdRootLayout.setBackgroundColor(Color.TRANSPARENT);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(
                R.layout.menu_main_scan_trd_lcn, null);
        trdItemsListView = (ListView) view
                .findViewById(R.id.scan_lcn_conflict_list);
       /* LinearLayout stubView = (LinearLayout) view
                .findViewById(R.id.scan_lcn_stub_view);*/
        //debug
//      stubView.getLayoutParams().height=200+mScanDialog.getWindow().getAttributes().y+mScanDialog.getWindow().getAttributes().height;

        List<LcnConflictGroup> lcnList= getLcnConflictGroup(mContext);
        int currentGoup=0;
        final int totalGroup=lcnList.size();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDConflictChannels() totalGroup=="+totalGroup);
        if(totalGroup==0){
            return;
        }

        updateLCNChannelList(view, currentGoup, lcnList);
        mCurrentGroupIndex=0;

        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
                saveLCNChannel(view,position);
                if(mCurrentGroupIndex >= totalGroup){
                    dismiss();
                }else {
                    autoDissmissTask(30);
                }

            }
        });

        trdItemsListView.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config22"+event.getAction() +",keyCode"+KeyEvent.KEYCODE_DPAD_CENTER);
                if(event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config22");
                    saveLCNChannel(view,trdItemsListView.getSelectedItemPosition());
                    if(mCurrentGroupIndex >= totalGroup){
                        dismiss();
                    }else {
                        autoDissmissTask(30);
                    }
                }
                return false;
            }
        });

        //mScanDialog.hide();   //shouldn't hidden window,but dispatch keyEvent to trdViews.
        //getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        getWindow().setContentView(view);
    }

    private void saveLCNChannel(View view,int position) {
        saveLCNChannel(view,mCurrentGroupIndex,position);
    }

    private void saveLCNChannel(View view, int currentGoup, int position) {
        List<LcnConflictGroup> lcnList= getLcnConflictGroup(mContext);

        if(currentGoup>=lcnList.size()){
            return;
        }
        int currentGroupSize=lcnList.get(currentGoup).channelName.length;

        if(position > currentGroupSize - 1 || currentGoup >= lcnList.size() - 1){
            if (position > currentGroupSize - 1) {
                //for DTV02135440 don't need restore
                //ScanContent.restoreForAllLCNChannelsForMenu(mContext, currentGoup);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"do nothing");
            } else {
                ScanContent.setAfterScanLCNForMenu(mContext, currentGoup,position);
            }
            mCurrentGroupIndex = lcnList.size();
//          trdRootLayout.setVisibility(View.INVISIBLE);
        } else {
            ScanContent.setAfterScanLCNForMenu(mContext, currentGoup,position);
            mCurrentGroupIndex++;
            updateLCNChannelList(view, mCurrentGroupIndex, lcnList);
        }
    }

    private void updateLCNChannelList(View view, int currentGoup,
            List<LcnConflictGroup> lcnList) {
        List<String> nextChannelList = getTRDChannelList(mContext,currentGoup);
        updateLCNTitle(view,String.format("%d/%d", currentGoup+1,lcnList.size()),nextChannelList.size()-1, lcnList.get(currentGoup).LCN );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,R.layout.lcn_conflict_item);
        for(String str:nextChannelList){
            adapter.add(str);
        }
        trdItemsListView.setAdapter(adapter);

        trdItemsListView.setSelection(0);
        trdItemsListView.requestFocus();
    }

    public List<LcnConflictGroup> getLcnConflictGroup(Context context) {

        LcnConflictGroup[] lcnList = MtkTvScan.getInstance()
                .getScanDvbtInstance().uiOpGetLcnConflictGroup();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getLcnConflictGroup"+lcnList.toString());
        return Arrays.asList(lcnList);
    }

    public  List<String> getTRDChannelList(Context context,int index) {

        LcnConflictGroup nwkList = getLcnConflictGroup(context).get(index);

        final List<String> networkList = new ArrayList<String>();
        networkList.addAll(Arrays.asList(nwkList.channelName));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTRDChannelList-1"+Arrays.asList(nwkList.channelName));
        networkList.add(context.getString(R.string.scan_trd_lcn_CONFLICT_USE_DEFAULT));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTRDChannelList-2"+networkList.toString());
        return networkList;
    }

    /**
     * update the TRD Views:LCN (IT)
     * @param title1
     * @param title2
     */
    private void updateLCNTitle(View view,String index,int channelNum,int lcnNum) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"updateLCNTitle(),"+String.format("index:%s,channelsNum:%d,lcn:%d",index,channelNum,lcnNum));

        final TextView title1 = (TextView)view
                .findViewById(R.id.scan_lcn_group_name);
        final TextView title2 = (TextView)view
                .findViewById(R.id.scan_lcn_group_conflict_num);
        final TextView lcn = (TextView)view
                .findViewById(R.id.scan_lcn_group_conflict_lcn);

        String title1Str=mContext.getString(R.string.scan_trd_lcn_CONFLICT_INDEX);
        String title2Str=mContext.getString(R.string.scan_trd_lcn_CONFLICT_CHANL_NUM);
        String lcnString=mContext.getString(R.string.menu_channel_scan_lcn);

        title1.setText(String.format("%s %s", title1Str,index));
        title2.setText(String.format("%s %d", title2Str,channelNum));
        lcn.setText(String.format("%s: %d", lcnString,lcnNum));
    }

    public void showTRDLCNv2Choices() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDLCNv2Choices()");
        //trdRootLayout.setBackgroundColor(Color.BLUE);
//      trdRootLayout.setVisibility(View.VISIBLE);
//      trdRootLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(
                R.layout.menu_main_scan_trd_fav_network2, null);
        trdItemsListView = (ListView) view
                .findViewById(R.id.scan_fav_network_list);

        TextView titleView = (TextView) view
                .findViewById(R.id.scan_fav_network_str);
        titleView.setText(mContext.getString(R.string.scan_trd_lcnv2_title));

        List<String> lcnList = getTRDLCNv2ChannelList();

        trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext,R.layout.lcn_conflict_item,lcnList));

        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
                setTRDLCNv2();
//              trdRootLayout.setVisibility(View.INVISIBLE);
                dismiss();
            }
        });

        trdItemsListView.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"write settings to System Config");
                    setTRDLCNv2();
//                  trdRootLayout.setVisibility(View.INVISIBLE);
                    dismiss();
                }
                return false;
            }
        });

        trdItemsListView.setSelection(0);
        trdItemsListView.requestFocus();
        getWindow().setContentView(view);
    }


    public void showOrderChoices() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(
                R.layout.menu_main_scan_trd_fav_network2, null);
        trdItemsListView = (ListView) view
                .findViewById(R.id.scan_fav_network_list);

        TextView titleView = (TextView) view
                .findViewById(R.id.scan_fav_network_str);
        titleView.setText(mContext.getString(R.string.scan_way));

        view.findViewById(R.id.italy_clarify_text).setVisibility(View.VISIBLE);

        final List<String> lcnV2List = Arrays.asList(mContext.getResources().getStringArray(R.array.order_array));

        trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext,R.layout.lcn_conflict_item,lcnV2List));

        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                dismiss();
                //set params to framework
                String autoOrdering = mContext.getResources().getString(R.string.automatic_Channel_ordering);
                String mediaset = mContext.getResources().getString(R.string.mediaset_premium_ordering);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoOrdering:"+autoOrdering+",--,mediaset:"+mediaset+","+lcnV2List.get(position).equals(autoOrdering)+","+lcnV2List.get(position).equals(mediaset));
                if(lcnV2List.get(position).equals(autoOrdering)){
                	 MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER, 1);
                }
                if(lcnV2List.get(position).equals(mediaset)){
               	 MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER, 2);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanconfigOnItem"+MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER));
                if(mContext instanceof ScanDialogActivity){
                	 ((ScanDialogActivity)mContext).startScan();
                }

            }
        });

        trdItemsListView.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
                	 dismiss();
                     //set params to framework
                	 //set params to framework
                	 String selectScanType = (String)trdItemsListView.getSelectedItem();
                	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectscantype:"+selectScanType);
                	 String autoOrdering = mContext.getResources().getString(R.string.automatic_Channel_ordering);
                     String mediaset = mContext.getResources().getString(R.string.mediaset_premium_ordering);
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoOrdering:"+autoOrdering+",--,mediaset:"+mediaset+","+selectScanType.equals(autoOrdering)+","+selectScanType.equals(mediaset));
                     if(selectScanType.equals(autoOrdering)){
                     	 MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER, 1);
                     }
                     if(selectScanType.equals(mediaset)){
                     	 MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER, 2);
                     }
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanconfigOnKey"+MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER));
                     //TODO
                	 if(mContext instanceof ScanDialogActivity){
                    	 ((ScanDialogActivity)mContext).startScan();
                    }
                }else if(event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode==KeyEvent.KEYCODE_BACK){
                    if(mContext instanceof ScanDialogActivity){
                        ((ScanDialogActivity)mContext).isChannelSelected = true;
                        ((ScanDialogActivity)mContext).cancleScan();
                   }
                }
                return false;
            }
        });

        trdItemsListView.setSelection(0);
        trdItemsListView.requestFocus();
        getWindow().setContentView(view);
        //getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }


    private void setTRDLCNv2() {
        // TODO Auto-generated method stub
        MtkTvScanDvbtBase.LCNv2ChannelList[] lcnv2List = MtkTvScan.getInstance()
                .getScanDvbtInstance().uiOpGetLCNv2ChannelList();
        int selection=trdItemsListView.getSelectedItemPosition();
        selection=Math.max(0, selection);
        selection=Math.min(selection, lcnv2List.length-1);
        ScanDvbtRet  rect=MtkTvScan
                .getInstance().getScanDvbtInstance().uiOpSetLCNv2ChannelList(lcnv2List[selection]);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveAfterScanGBRRegionInfo(),"+rect.name());
    }

    public List<String> getTRDLCNv2ChannelList() {
        List<String> channelList = new ArrayList<String>();
        MtkTvScanDvbtBase.LCNv2ChannelList[] lcnv2List = MtkTvScan.getInstance()
                .getScanDvbtInstance().uiOpGetLCNv2ChannelList();
        for(MtkTvScanDvbtBase.LCNv2ChannelList lcnList : lcnv2List ){
            channelList.add(lcnList.channelListName);
        }
        return channelList;
    }

    public void showTRDDVBSBATChoices(final int satID) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showDVBSBATChoices()");
        //trdRootLayout.setBackgroundColor(Color.BLUE);
//      trdRootLayout.setVisibility(View.VISIBLE);
//
//      trdRootLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(
                R.layout.menu_main_scan_trd_fav_network2, null);
        trdItemsListView = (ListView) view
                .findViewById(R.id.scan_fav_network_list);

        final List<OneBatData> batList = getDvbsBATList();
        List<String> batStrList = getDvbsBATStrList(batList);

        trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext,R.layout.lcn_conflict_item,batStrList));

        trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"show write settings to System Config");
//              trdRootLayout.setVisibility(View.INVISIBLE);
                setDvbsBAT(batList,satID);
                dismiss();
            }
        });

        trdItemsListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"show write settings to System Config KEYCODE_DPAD_CENTER");
                        setDvbsBAT(batList,satID);
                        dismiss();
                        return true;
                    }else if (keyCode == KeyEvent.KEYCODE_BACK) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"show KeyEvent.KEYCODE_BACK");
                        dismiss();
                        return true;
                    }
                }
                return false;
            }
        });

        trdItemsListView.setSelection(0);
        getWindow().setContentView(view);
    }

    private void setDvbsBAT(List<OneBatData> list,int satID) {

        int selection = trdItemsListView.getSelectedItemPosition();
        selection = Math.max(0, selection);
        selection = Math.min(selection, list.size() - 1);

        int batID = list.get(selection).batId;
    ((ScanViewActivity) mContext).startDVBSFullScan(satID, batID, -1, null);

    }

    public static List<OneBatData> getDvbsBATList() {
        MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
        dvbsScan.dvbsGetNfyBatInfo();
        int num = dvbsScan.nfyBatInfo_batNum;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"num:" + num);
        OneBatData[] data = dvbsScan.nyfBatInfo_batList;
        for (OneBatData one : data) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"bat name:" + one.batName);
        }
        return Arrays.asList(data);
    }

    public static List<String> getDvbsBATStrList(List<OneBatData> list) {
        List<String> batList = new ArrayList<String>();

        for (OneBatData one : list) {
            batList.add(one.batName);
        }
        return batList;
    }

  public void showTRDDVBSTKGSServiceList(final int satID) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showTRDDVBSTKGSServiceList()");


    LayoutInflater inflater = LayoutInflater.from(mContext);
    View view = inflater.inflate(
        R.layout.menu_main_scan_trd_fav_network2, null);
    trdItemsListView = (ListView) view
        .findViewById(R.id.scan_fav_network_list);
    TextView tview = (TextView) view.findViewById(R.id.scan_fav_network_str);
    tview.setText(mContext.getString(R.string.tkgs_select_service_list));

    MenuDataHelper helper = MenuDataHelper.getInstance(mContext);
    final List<TKGSOneSvcList> svcList = helper.getTKGSOneSvcList();
    List<String> batStrList = helper.getTKGSOneServiceStrList(svcList);
    int tkgsSvcListSelPos = helper.getTKGSOneServiceSelectValue();

    trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext,
        R.layout.lcn_conflict_item, batStrList));

    trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemClick setServiceList for tkgs");
        // trdRootLayout.setVisibility(View.INVISIBLE);
        setDvbsTKGS(svcList, satID);
        dismiss();
      }
    });

    trdItemsListView.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setServiceList for tkgs KEYCODE_DPAD_CENTER");
            setDvbsTKGS(svcList, satID);
            dismiss();
            return true;
          } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setServiceList show KeyEvent.KEYCODE_BACK");
            dismiss();
            return true;
          }
        }
        return false;
      }
    });

    trdItemsListView.setSelection(tkgsSvcListSelPos);
    getWindow().setContentView(view);
  }

  private void setDvbsTKGS(List<TKGSOneSvcList> list, int satID) {

    int selection = trdItemsListView.getSelectedItemPosition();
    selection = Math.max(0, selection);
    selection = Math.min(selection, list.size() - 1);

    int svcListNo = list.get(selection).svcListNo;
    MtkTvScanDvbsBase dvbsScan = new MtkTvScanDvbsBase();
    int ret = dvbsScan.dvbsTKGSSelSvcList(svcListNo).ordinal();
    if (ret == 0) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTKGSOneSvcList set svcListNo to:" + svcListNo);

    }
    // tkgs normal rescan
    ((ScanViewActivity) mContext).startDVBSFullScan(satID, -1, 1);//use current mLocationId

  }
  
  public void showFvpUserSelectionChoices() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showFvpUserSelectionChoices()");
      LayoutInflater inflater = LayoutInflater.from(mContext);
      View view = inflater.inflate(
              R.layout.menu_main_scan_trd_fav_network2, null);
      TextView titleView = (TextView) view
              .findViewById(R.id.scan_fav_network_str);
      String title = MtkTvScan.getInstance().getScanDvbtInstance().FVPGetUserSelectionTitle();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"showFvpUserSelectionChoices() title="+title);
      titleView.setText(title);
      trdItemsListView = (ListView) view
              .findViewById(R.id.scan_fav_network_list);
      List<String> selectionList = getFvpUserSelectionList();
      trdItemsListView.setAdapter(new ArrayAdapter<String>(mContext,R.layout.lcn_conflict_item,selectionList));
      trdItemsListView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemClick setFvpUserSelection");
              setFvpUserSelection();
              dismiss();
          }
      });

      trdItemsListView.setOnKeyListener(new View.OnKeyListener(){

          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
              if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode==KeyEvent.KEYCODE_DPAD_CENTER){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKey setFvpUserSelection");
                  setFvpUserSelection();
                  dismiss();
              }
              return false;
          }
      });

      trdItemsListView.setSelection(0);
      trdItemsListView.requestFocus();
      getWindow().setContentView(view);
  }
  
  public List<String> getFvpUserSelectionList() {
      List<String> selectList = new ArrayList<String>();
      MtkTvScanDvbtBase.UserSelection[] selectionList = MtkTvScan.getInstance()
              .getScanDvbtInstance().FVPGetUserSelection();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFvpUserSelectionList selectionList length="+selectionList.length);
      for (MtkTvScanDvbtBase.UserSelection userSelection : selectionList) {
          selectList.add(userSelection.text);
      }
      return selectList;
  }
  
  private void setFvpUserSelection(){
      List<MtkTvScanDvbtBase.UserSelection> selectList = new ArrayList<MtkTvScanDvbtBase.UserSelection>();
      selectList.addAll(Arrays.asList(MtkTvScan.getInstance().getScanDvbtInstance().FVPGetUserSelection()));
     
      int selection=trdItemsListView.getSelectedItemPosition();
      selection=Math.max(0, selection);
      selection=Math.min(selection, selectList.size()-1);
      ScanDvbtRet rect=MtkTvScan
              .getInstance().getScanDvbtInstance().FVPSetUserSelection(selectList.get(selection));
      if(mContext != null){
          ((ScanDialogActivity)mContext).resetFvpTipMsg();
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setFvpUserSelection(),"+rect.name()+","+selectList.get(selection).text+","+selectList.get(selection).index);
  }
  
  public static boolean haveTargetRegionForUK(){
      MtkTvScanDvbtBase.TargetRegion[] regionList = MtkTvScan
              .getInstance().getScanDvbtInstance()
              .uiOpGetTargetRegion();
      MtkTvScanDvbtBase.UiOpSituation opSituation = MtkTvScan.getInstance().getScanDvbtInstance().uiOpGetSituation();
      if(regionList != null){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"haveTargetRegionForUK regionList length="+regionList.length+", opSituation.targetRegionPopUp="+opSituation.targetRegionPopUp);
      }
      return opSituation.targetRegionPopUp && regionList != null
              && regionList.length > 0 ; 
  }
  
  private static int dissmissMsg = 1;
  Handler mHandler = new Handler(){
      public void handleMessage(Message msg) {
          if(msg.what == dissmissMsg){
              if(isShowing()){
                  dismiss();
              }
          }
      };
  };
  private void autoDissmissTask(int seconds){
      mHandler.removeMessages(dissmissMsg);
      mHandler.sendEmptyMessageDelayed(dissmissMsg, seconds * 1000);
  }

}
