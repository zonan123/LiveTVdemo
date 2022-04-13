
package com.mediatek.wwtv.tvcenter.nav.view;

//import android.app.Activity;
import android.app.Dialog;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;

import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.R;
//import com.mediatek.wwtv.tvcenter.util.*;
import com.mediatek.twoworlds.tv.MtkTvHBBTVBase;
//import com.mediatek.twoworlds.tv.model.MtkTvHBBTVBase;
//import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
//import com.mediatek.wwtv.tvcenter.util.SaveValue;
import java.lang.ref.WeakReference;
import java.util.Locale;
//import android.R.integer;
import android.content.Context;
import android.os.Bundle;
//import android.provider.Settings;
//import android.util.Log;
import android.view.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
//import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
//import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
//import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
//import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
//import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
//import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import android.widget.ListView;

//import android.app.Activity;
//import android.content.ComponentName;
//import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
//import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
//import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
//import android.widget.ListView;
import com.mediatek.wwtv.tvcenter.nav.adapter.HbbtvAudioSubtitleAdapter;
import com.mediatek.wwtv.tvcenter.nav.adapter.HbbtvSubtitleAdapter;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;

public class HbbtvAudioSubtitleDialog extends Dialog implements OnItemClickListener{
    private static final String TAG = "HbbtvAudioSubtitleDialog.java";
    /** audio/subtitle**/
    public static final String KEY_AUDIO = "audio_hbbtv";
    public static final String KEY_SUBTITLE="subtitle_hbbtv";
    //private String mStartKey = null;
    private Context mContext;
    MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[] strmSbtl;
    MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[] strmaudio;
    MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[] defaultoff = new MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[1];
    MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[] defalutoffsub= new MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[1];
    MtkTvHBBTVBase hbbtv = new MtkTvHBBTVBase();
    int nmemb =15;
    int[] ntotal = new int[]{0};
    int defaultvalue =-1;
    int defaultaudiovalue =-1;
    int actualtotal = 1;
    int actualtotalaudio =1;
    //private MtkTvBroadcastBase mMtkTvBroadcastBase;
    private TextView titleview;
    private ListView listView;
    private ListView subtitleListView;
    private Drawable mSourceSelectedIcon;
    private Drawable mSourceUnSelectedIcon;
    private Drawable mConflictIcon;
    private HbbtvAudioSubtitleAdapter mHbbtvAudioSubtitleAdapter;
    private HbbtvSubtitleAdapter mHbbtvSubtitleAdapter;
    private List<Integer> mSelectList = new ArrayList<Integer>();
    //    private List<String> mSourceList2 =  mSourceList;
    private List<String> mContentList = new ArrayList<>();
    private List<String> subtitleItemName = new ArrayList<>();
    private List<String> subtitleItemvalue = new ArrayList<>();
    public static int marginX;
    public static int marginY;
    public static int menuWidth = 343;
    public static int menuHeight = 400;
    public int x = 0;
    public int y = 0;
    private String hbbtvType="";
    private  MenuConfigManager mConfigManager;
    private boolean isSubtitleList=false;
    public static WeakReference<HbbtvAudioSubtitleDialog> mHbbtvAudioSubtitleDialog;
    private String subtitleName ;
    private TVCallBack mTVCallBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
        setContentView(R.layout.hbbtv_audio_subtitle_dialog);
        mConfigManager = MenuConfigManager.getInstance(mContext);
        initViews();
        setWindowPosition();
        initData();
    }

    public HbbtvAudioSubtitleDialog(Context mContext){
        super(mContext, R.style.nav_dialog);
        this.mContext = mContext;
        
    }

    private void initViews(){
        mHbbtvAudioSubtitleDialog = new WeakReference<>(this);
        titleview = (TextView)findViewById(R.id.hbbtv_au_sub_title);
        listView = (ListView)findViewById(R.id.hbbtv_list);
        subtitleListView = (ListView)findViewById(R.id.hbbtv_subtitle_list);
//        titleview.setText("Audio Track");
        mSourceSelectedIcon = mContext.getResources().getDrawable(
                R.drawable.source_list_selected);
        int iconw = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_widgh);
        int iconh = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_height);
        mSourceSelectedIcon.setBounds(0, 0,iconw,iconh);
        mSourceUnSelectedIcon = mContext.getResources().getDrawable(
                R.drawable.source_list_selected_nor);
        mSourceUnSelectedIcon.setBounds(0, 0,iconw,iconh);

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_NEW_APP)) {
            mConflictIcon = mContext.getResources().getDrawable(
                    R.drawable.translucent_background);
        } else {
            mConflictIcon = mContext.getResources().getDrawable(
                    R.drawable.nav_source_pip_disable_icon);
        }
        mConflictIcon.setBounds(0, 0, iconw,iconh);
        subtitleName =  mContext.getResources().getString(R.string.menu_setup_subtitle);       

    }


    private void initData() {
        if(mTVCallBack==null) {
            mTVCallBack = new TVCallBack();
        }
        strmSbtl = new MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[nmemb];
        strmaudio = new MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[nmemb];
        defaultoff[0] = new MtkTvHBBTVBase.MtkTvHbbTVStreamAudio();
        defalutoffsub[0] = new MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle();
        defaultoff[0].index=-1;
        defaultoff[0].active =false;
        defalutoffsub[0].index=-1;
        defalutoffsub[0].active=false;
        if(getType().equals(KEY_AUDIO)){
        hbbtv.hbbtvAudioGetList(strmaudio,nmemb,ntotal);
        actualtotalaudio = ntotal[0]+1;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"actualtotalaudio="+actualtotalaudio+",callback->"+mTVCallBack.toString());
        }else{
        defaultvalue = -1;
        hbbtv.hbbtvSubtitleGetList(strmSbtl,nmemb,ntotal);
        actualtotal = ntotal[0]+1;
            }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,actualtotalaudio+",strmsbtl=="+strmSbtl+",,strmaudio=="+strmaudio+",,total="+actualtotal);





    }

    private void setSourceListData() {
        if(!mSelectList.isEmpty()) {
            mSelectList.clear();
        }
        if(!mContentList.isEmpty()){
            mContentList.clear();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"gettype--->"+getType());
        if(getType().equals(KEY_SUBTITLE)) {
            getSubtitle();
            titleview.setText(R.string.menu_setup_subtitle);
            listView.setVisibility(View.GONE);
            listView.setFocusable(false);
            subtitleListView.setVisibility(View.VISIBLE);
            subtitleListView.setFocusable(true);
            subtitleListView.requestFocus();
            setSubtitleList();
        }else{
            getAudio();
            titleview.setText(R.string.menu_audio_sound_tracks);
            listView.setVisibility(View.VISIBLE);
            listView.setFocusable(true);
            listView.requestFocus();
            subtitleListView.setVisibility(View.GONE);
            subtitleListView.setFocusable(false);
            setTracksList();
        }

        
    }

    private void setSubtitleList(){
        subtitleItemName.clear();
        subtitleItemvalue.clear();
        subtitleItemName.add(subtitleName);     
        if(mConfigManager.getDefault(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE)==0) {
            subtitleItemvalue.add(mContext.getResources().getString(R.string.common_off));
        }else{
            subtitleItemvalue.add(mContext.getResources().getString(R.string.common_on));
            subtitleItemName.add(mContext.getResources().getString(R.string.menu_subtitle_track));
        }
        if(mConfigManager.getDefault(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE)==1){
            if(!mContentList.isEmpty()) {
                subtitleItemvalue.add(mContentList.get(defaultvalue));
            }else{
                subtitleItemvalue.add(mContext.getResources().getString(R.string.common_off));
            }
        }
        if(mHbbtvSubtitleAdapter==null){
            mHbbtvSubtitleAdapter = new HbbtvSubtitleAdapter(mContext,subtitleItemName,subtitleItemvalue);
            subtitleListView.setAdapter(mHbbtvSubtitleAdapter);
        }else{
            mHbbtvSubtitleAdapter.updateList(subtitleItemName,subtitleItemvalue);
        }

        subtitleListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subtitle onItemClick-->" + position);
                if(position==0) {
                    isSubtitleList = true;
                    getSubtitleList();
                }else{
                    isSubtitleList = false;
                    showList();
                }
//                    mConfigManager.setValue(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE, position==0?0:1);
//                    setSourceListData();
            }
        });
        subtitleListView.setSelection(0);
        mHbbtvSubtitleAdapter.notifyDataSetChanged();

    }

    private void showList(){
        listView.setVisibility(View.VISIBLE);
        subtitleListView.setVisibility(View.GONE);
        mContentList.clear();
        mSelectList.clear();
        getSubtitle();
        titleview.setText(R.string.menu_subtitle_track);
        setTracksList();
    }

    private void setTracksList(){
        if(mHbbtvAudioSubtitleAdapter == null) {
            mHbbtvAudioSubtitleAdapter = new HbbtvAudioSubtitleAdapter(mContext, mSelectList,
                    mContentList, mSourceSelectedIcon, mSourceUnSelectedIcon,
                    mConflictIcon);
            listView.setAdapter(mHbbtvAudioSubtitleAdapter);
            listView.setOnItemClickListener(this);
        } else {
            mHbbtvAudioSubtitleAdapter.updateList(mSelectList, mContentList);
        }
        
        listView.setFocusable(true);
        listView.requestFocus();
        if(getType().equals(KEY_SUBTITLE)){
            listView.setSelection(defaultvalue);
        }
         
        if(getType().equals(KEY_AUDIO)){
            listView.setSelection(defaultaudiovalue);
        }
         mHbbtvAudioSubtitleAdapter.notifyDataSetChanged();
    }

    /**
     * subtitle  include: off  on
     */
    private void getSubtitleList(){
        if(!mSelectList.isEmpty()) {
            mSelectList.clear();
        }
        if(!mContentList.isEmpty()){
            mContentList.clear();
        }
        mContentList.add(mContext.getResources().getString(R.string.common_off));
        mContentList.add(mContext.getResources().getString(R.string.common_on));
        listView.setVisibility(View.VISIBLE);
        subtitleListView.setVisibility(View.GONE);
        if(mConfigManager.getDefault(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE)==0) {
            mSelectList.add(1);
            mSelectList.add(0);
            defaultvalue = 0;
        }else{
            mSelectList.add(0);
            mSelectList.add(1);
            defaultvalue = 1;
        }
        setTracksList();
    }

    private void getAudio(){
        String [] totalStrings = new String[actualtotalaudio];
        if(strmaudio==null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hbbtvaudioGetList is null");
            return ;
        }
        if(!mSelectList.isEmpty()) {
            mSelectList.clear();
        }
        if(!mContentList.isEmpty()){
            mContentList.clear();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"total"+totalStrings.length+"==="+strmaudio.length);
        for(int i=0;i<actualtotalaudio-1;i++){
            if(null!=strmaudio[i]&&strmaudio[i].lang!=null) {
                totalStrings[i] = String.valueOf(strmaudio[i].lang);
                Locale locale = new Locale(totalStrings[i]);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio=" + totalStrings[i] + ",,audio real=" + locale.getDisplayLanguage());
                mContentList.add(totalStrings[i]);
                if (strmaudio[i].active) {
                    defaultaudiovalue = i;
                    mSelectList.add(1);
                }else{
                    mSelectList.add(0);
                }
            }
        }
        if(totalStrings.length!=0){
            totalStrings[totalStrings.length-1]=mContext.getResources().getString(R.string.common_off);
            mContentList.add( totalStrings[totalStrings.length-1]);
        }
        if(defaultaudiovalue==-1){
            defaultaudiovalue=totalStrings.length-1;
            mSelectList.add(1);
        }else{
            mSelectList.add(0);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"defaultaudiovalue=="+defaultaudiovalue);

    }



    private void getSubtitle(){
        String [] totalStrings = new String[actualtotal];
        if(strmSbtl==null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hbbtvsubtitleGetList is null");
            return ;
        }
        if(!mSelectList.isEmpty()) {
            mSelectList.clear();
        }
        if(!mContentList.isEmpty()){
            mContentList.clear();
        }
        defaultvalue = -1;
        for(int i=0;i<actualtotal-1;i++){
            if(strmSbtl[i]!=null&&strmSbtl[i].lang!=null) {
                totalStrings[i] = String.valueOf(strmSbtl[i].lang);
                Locale locale = new Locale(totalStrings[i]);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subtitle=" + totalStrings[i] + ",,real==" + locale.getDisplayLanguage());
                mContentList.add(totalStrings[i]);
                if (strmSbtl[i].active) {
                    defaultvalue = i;
                    mSelectList.add(1);
                }else{
                    mSelectList.add(0);
                }

            }
        }
        if(totalStrings.length!=0){
            totalStrings[totalStrings.length-1]=mContext.getResources().getString(R.string.common_off);
            mContentList.add(totalStrings[totalStrings.length-1]);
        }
        if(defaultvalue==-1){
            defaultvalue=totalStrings.length-1;
            mSelectList.add(1);
        }else{
            mSelectList.add(0);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"defaultvalue=="+defaultvalue);

    }
    @Override
    public void show() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show()");
        super.show();
        setSourceListData();
    }

    // init dialog position
    private void setWindowPosition() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        TypedValue sca = new TypedValue();
        mContext.getResources().getValue(R.dimen.nav_channellist_marginY,sca ,true);
        //float chmarginY  = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_marginX,sca ,true);
        //float chmarginX  = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_width,sca ,true);
        float chwidth  = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_height,sca ,true);
        //float chheight  = sca.getFloat();
        //int marginY = (int) (display.getHeight() * chmarginY);
        //int marginX = (int) (display.getWidth() * chmarginX);
        int menuWidth = (int) (display.getWidth() * chwidth);
        //int menuHeight = (int) (display.getHeight() * chheight);
        lp.width = menuWidth;
        lp.height = display.getHeight();
        int x = display.getWidth();
        int y =0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setWindowPosition menuWidth "+menuWidth+" x "+x +" display.getWidth() "+display.getWidth());
        lp.x = x;
        lp.y = y;
        window.setAttributes(lp);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemClick-->"+position+",isSubtitleList-->"+isSubtitleList);

            if(isSubtitleList){
                mConfigManager.setValue(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE, position==0?0:1);
                if(position==1){
                initData();
                }
                setSourceListData();
                return;
            }
        if(getType().equals(KEY_SUBTITLE)){
            MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[] strmSbt12;
            if(position==(actualtotal-1)){
                if(strmSbtl[defaultvalue]!=null){
                    //strmSbtl[defaultvalue].index=-1;
                    //strmSbt12 = new MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[]{strmSbtl[defaultvalue]};
                    hbbtv.hbbtvSubtitleSetActive(defalutoffsub,1);      
                    strmSbtl[defaultvalue].active = false;
                }
            }else {
                strmSbt12 = new MtkTvHBBTVBase.MtkTvHbbTVStreamSubtitle[]{strmSbtl[position]};
                hbbtv.hbbtvSubtitleSetActive(strmSbt12,1);
                if(strmSbtl[defaultvalue]!=null){
                strmSbtl[defaultvalue].active = false;
                    }
                strmSbtl[position].active = true;
            }
            setSourceListData();
            titleview.setText(subtitleName);
            listView.setVisibility(View.GONE);
            subtitleListView.setVisibility(View.VISIBLE);
            return;
        }else {
            MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[] strmaudios ;
            if(position==(actualtotalaudio-1)){
                if(strmaudio[defaultaudiovalue]!=null){
                    strmaudio[defaultaudiovalue].index=-1;
                    strmaudios = new  MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[]{strmaudio[defaultaudiovalue]};
                    hbbtv.hbbtvAudioSetActive(strmaudios,1);
                }
            }else{
                strmaudios = new  MtkTvHBBTVBase.MtkTvHbbTVStreamAudio[]{strmaudio[position]};
                hbbtv.hbbtvAudioSetActive(strmaudios,1);
            }
        }
            this.dismiss();
    }

    public String getType(){

        return hbbtvType;
    }

    public void setHbbtvType(String type){

        hbbtvType = type;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onkeydown==" + keyCode);
        if(KeyMap.KEYCODE_BACK==keyCode){
            if(getType().equals(KEY_SUBTITLE)&&listView.getVisibility() == View.VISIBLE){
                listView.setVisibility(View.GONE);
                subtitleListView.setVisibility(View.VISIBLE);
                titleview.setText(subtitleName);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
  
    public static HbbtvAudioSubtitleDialog getHbbtvDialog(){
        if(mHbbtvAudioSubtitleDialog == null){
            return null;
        }
        return mHbbtvAudioSubtitleDialog.get();
    }

    public class TVCallBack extends MtkTvTVCallbackHandler {
        public int notifyHBBTVMessage(int callbackType, int[] callbackData, int callbackDataLen){
            // TODO Auto-generated method stub
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Handle notifyHBBTVMessage=" + callbackType);
            if (callbackType == 268){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Handle notifyHBBTVMessage enter 268");
                mhandler.sendEmptyMessageDelayed(1, 0);
            }
            return 0;
        }
    }

    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage=" + msg.what);
            switch(msg.what){
                case 1:
                    initData();
                    setSourceListData();
                break;
                default:
                break;
            }
        }
    };

}