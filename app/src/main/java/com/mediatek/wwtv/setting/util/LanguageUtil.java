
package com.mediatek.wwtv.setting.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.text.TextUtils;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvSubtitle;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.wwtv.tvcenter.util.Constants;

public class LanguageUtil {
  static final String TAG = "LanguageUtil";
  private IActivityManager am;
  private Configuration config;
  private final Context mContext;
  private final TVContent mTV;
  private List<String> mLanguageOSDArray = new ArrayList<String>();
  private List<String> mLanguageAudioArray = new ArrayList<String>();
  private List<String> mLanguageSubtitleArray = new ArrayList<String>();
  //private List<String> mLanguageSubtitleArrayStr = new ArrayList<String>();
  private List<String> mLanguageAudioArrayPA = new ArrayList<String>();
  private List<String> mLanguageAudioArrayCN = new ArrayList<String>();
  private List<String> mLanguageAudioArrayPANZL = new ArrayList<String>();
  private List<String> mLanguageAudioArrayUS = new ArrayList<String>();
  private List<String> mLanguageAudioArraySA = new ArrayList<String>();
  private List<String> mMtsLanguageArrayList = new ArrayList<String>();
  private List<String> mMtsLanguageArrayListstr = new ArrayList<String>();
  private List<String> mSubtitleAudioUnormalBeforeListstr = new ArrayList<String>();
  private List<String> mSubtitleAudioUnormalAfterListstr = new ArrayList<String>();
  private List<String> mSubtitleAudioUnormalList = new ArrayList<String>();
  private List<String> mSubtitleAudioUnormalListstr = new ArrayList<String>();
  // private TVEventCommand tvEventCommand;
  public LanguageUtil(Context context) {
    super();
    mContext = context.getApplicationContext();
    mTV = TVContent.getInstance(mContext);
    mLanguageOSDArray.clear();
    mLanguageAudioArray.clear();
    mLanguageAudioArrayUS.clear();
    mLanguageAudioArraySA.clear();
    mLanguageAudioArrayPA.clear();
    mLanguageAudioArrayCN.clear();
    mLanguageAudioArrayPANZL.clear();
    mLanguageAudioArray = new LinkedList<String>(
            Arrays.asList(context.getResources().getStringArray(R.array.menu_tv_audio_language_eu_array_value)));
    mLanguageAudioArrayUS = new LinkedList<String>(
            Arrays.asList(context.getResources().getStringArray(R.array.menu_tv_audio_language_array_US_value)));
    mLanguageAudioArraySA = new LinkedList<String>(
            Arrays.asList(context.getResources().getStringArray(R.array.menu_tv_audio_language_array_SA_value)));
    mLanguageAudioArrayPA = new LinkedList<String>(
    		Arrays.asList(context.getResources().getStringArray(R.array.menu_tv_audio_language_array_PA_value)));
    mLanguageAudioArrayCN = new LinkedList<String>(
            Arrays.asList(context.getResources().getStringArray(R.array.menu_tv_audio_language_array_CN_value)));
    mLanguageAudioArrayPANZL = new LinkedList<String>(
    		Arrays.asList(context.getResources().getStringArray(R.array.menu_tv_audio_language_array_PA_NZL_value)));
    mLanguageOSDArray = new LinkedList<String>(Arrays.asList(new String[] {
        MtkTvConfigType.S639_CFG_LANG_ENG,
        MtkTvConfigType.S639_CFG_LANG_BAQ,
        MtkTvConfigType.S639_CFG_LANG_CAT,
        MtkTvConfigType.S639_CFG_LANG_SCR,
        MtkTvConfigType.S639_CFG_LANG_CZE,
        MtkTvConfigType.S638_CFG_LANG_DAN,
        MtkTvConfigType.S639_CFG_LANG_DUT,
        MtkTvConfigType.S639_CFG_LANG_FIN,
        MtkTvConfigType.S639_CFG_LANG_FRA, "gla", "glg",
        MtkTvConfigType.S639_CFG_LANG_GER,
        MtkTvConfigType.S639_CFG_LANG_HUN,
        MtkTvConfigType.S639_CFG_LANG_ITA,
        MtkTvConfigType.S639_CFG_LANG_NOR,
        MtkTvConfigType.S639_CFG_LANG_POL,
        MtkTvConfigType.S639_CFG_LANG_POR, "rum",
        MtkTvConfigType.S639_CFG_LANG_SCC, "slo", "slv",
        MtkTvConfigType.S639_CFG_LANG_SPA,
        MtkTvConfigType.S639_CFG_LANG_SWE,
        MtkTvConfigType.S639_CFG_LANG_TUR, "wel",
        MtkTvConfigType.S639_CFG_LANG_EST,
        MtkTvConfigType.S639_CFG_LANG_RUS
    }));
    // tvEventCommand=new TVEventCommand();
    int arrRes=0;
    //int arrResStr=0;
	if(mTV.isIDNCountry()||mTV.isMYSCountry()||mTV.isAUSCountry()||mTV.isVNMCountry()){
		arrRes=R.array.menu_tv_subtitle_language_in_mys_aus_tha_vnm_array_value;
		//arrResStr=R.array.menu_tv_subtitle_language_in_mys_aus_tha_vnm_array;
    }else if(mTV.isSQPCountry()){
        arrRes=R.array.menu_tv_subtitle_language_sgp_array_value;
        //arrResStr=R.array.menu_tv_subtitle_language_sgp_array;
    }else if(mTV.isNZLCountry()){
        arrRes=R.array.menu_tv_subtitle_language_nzl_array_value;
        //arrResStr=R.array.menu_tv_subtitle_language_nzl_array;
    }else if(CommonIntegration.isEUPARegion()){
        arrRes=R.array.menu_tv_subtitle_language_pa_array_value;
        //arrResStr=R.array.menu_tv_subtitle_language_pa_array;
    }else if(CommonIntegration.isEUPARegion()){
        arrRes=R.array.menu_tv_subtitle_language_sa_array_value;
    }else {
  	    arrRes=R.array.menu_tv_subtitle_language_eu_array_value;
  	    //arrResStr=R.array.menu_tv_subtitle_language_eu_array;
    }
	mLanguageSubtitleArray = new LinkedList<String>(
		        Arrays.asList(mContext.getResources().getStringArray(arrRes)));
	/*mLanguageSubtitleArrayStr=new LinkedList<String>(
	        Arrays.asList(mContext.getResources().getStringArray(arrResStr)));*/
	 mMtsLanguageArrayList = new LinkedList<String>(
	            Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_audio_language_mts_array_value)));
	 mMtsLanguageArrayListstr = new LinkedList<String>(
	            Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_audio_language_mts_array)));
    mSubtitleAudioUnormalBeforeListstr = new LinkedList<>(
            Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_language_unnormal_before_change_value)));
    mSubtitleAudioUnormalAfterListstr = new LinkedList<>(
            Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_language_unnormal_after_change_value)));
    mSubtitleAudioUnormalList = new LinkedList<String>(
            Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_audio_language_unnormal_array)));
    mSubtitleAudioUnormalListstr = new LinkedList<String>(
            Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_audio_language_unnormal_array_value)));
  }


  public String getSubitleNameByValue(String value){
	  if(TextUtils.isEmpty(value)){
		  return "";
	  }

    value=value.trim().toLowerCase(Locale.getDefault());
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"subtitle_value =="+value);

    for (int mUnBeforeSubtitle = 0; mUnBeforeSubtitle < mSubtitleAudioUnormalBeforeListstr.size(); mUnBeforeSubtitle++){
      if(value.contains(mSubtitleAudioUnormalBeforeListstr.get(mUnBeforeSubtitle))){
        value = mSubtitleAudioUnormalAfterListstr.get(mUnBeforeSubtitle);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subtitle_change_value:"+value);
        break;
      }
    }

    if("msa".equals(value)){
      return mContext.getResources().getString(R.string.menu_arrays_Bahasa_Melayu);
    }
    if(TVContent.getInstance(mContext).isEspCountry() && "und".equalsIgnoreCase(value)){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"esp--> und");
      return  mContext.getResources().getString(R.string.menu_arrays_other);
    }else if(TVContent.getInstance(mContext).isEspCountry() && "qaa".equalsIgnoreCase(value)){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"esp--> qaa");
      return  mContext.getResources().getString(R.string.menu_arrays_Original_Version);
    }else if(mTV.getCurrentTunerMode() == 0 &&
            MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_HUN) &&
            "qaa".equalsIgnoreCase(value)){
      return  mContext.getResources().getString(R.string.menu_arrays_hun_Original_Audio);
    }else if(mTV.getCurrentTunerMode() == 0 &&
            MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_HUN) &&
            "mul".equalsIgnoreCase(value)){
      return  mContext.getResources().getString(R.string.menu_arrays_hun_Multiple_Languages);
    }

    Locale locale = new Locale(value);
    String localename= locale.getDisplayLanguage();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"subtitle_locale language=="+localename);
    if(!localename.equals(value)){
      return localename;
    }

	  int index=-1;
	  for(int i = 0; i< mSubtitleAudioUnormalListstr.size(); i++){
		  String subtileValue= mSubtitleAudioUnormalListstr.get(i);
		  if(value.contains(subtileValue)){
			  index=i;
			  break;
		  }
	  }
	  String subtitleName=value;
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubitleNameByValue mLanguageSubtitleArray size: " + mSubtitleAudioUnormalList.size());
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubitleNameByValue replace before------>subtitleName: " + subtitleName+"index: " + index);
	  if(index>=0&&index< mSubtitleAudioUnormalList.size()){
		  subtitleName= mSubtitleAudioUnormalList.get(index);
		  //subtitleName=value.replace(mSubtitleAudioUnormalList.get(index), subtitleName);
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubitleNameByValue replace after------>subtitleName: " + subtitleName);
        return subtitleName;
      }else {
        if(!TextUtils.isEmpty(subtitleName)){
          subtitleName = subtitleName.substring(0,1).toUpperCase(Locale.getDefault())+value.substring(1);
        }
      }

    return subtitleName;
  }


  public String getMtsNameByValue(String value){
	  if(TextUtils.isEmpty(value)){
		  return "Unknown";
	  }
	  value=value.trim().toLowerCase(Locale.getDefault());
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"Audio_value =="+value);
    for (int mUnBeforeSubtitle = 0; mUnBeforeSubtitle < mSubtitleAudioUnormalBeforeListstr.size(); mUnBeforeSubtitle++){
      if(value.contains(mSubtitleAudioUnormalBeforeListstr.get(mUnBeforeSubtitle))){
        value = mSubtitleAudioUnormalAfterListstr.get(mUnBeforeSubtitle);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Audio_change_value:"+value);
        break;
      }
    }


    int index=-1;
    if(MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_CH_LST_TYPE) > 0){
        if("und".equals(value) || "qaa".equalsIgnoreCase(value)){
           return MtkTvCI.getInstance(Constants.slot_id).getProfileISO639LangCode();
        }
    }
      if(TVContent.getInstance(mContext).isTHACountry()&&"und".equals(value)){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tha--> und");
        return mContext.getResources().getString(R.string.menu_arrays_Undefined);
        }
      if("msa".equals(value)){
        return mContext.getResources().getString(R.string.menu_arrays_Bahasa_Melayu);
      }

      if(TVContent.getInstance(mContext).isEspCountry() && "und".equalsIgnoreCase(value)){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"esp--> und");
        return  mContext.getResources().getString(R.string.menu_arrays_other);
      }else if(TVContent.getInstance(mContext).isEspCountry() && "qaa".equalsIgnoreCase(value)){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"esp--> qaa");
        return  mContext.getResources().getString(R.string.menu_arrays_Original_Version);
      }else if(mTV.getCurrentTunerMode() == 0 &&
              MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_HUN) &&
              "qaa".equalsIgnoreCase(value)){
        return  mContext.getResources().getString(R.string.menu_arrays_hun_Original_Audio);
      }else if(mTV.getCurrentTunerMode() == 0 &&
              MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_HUN) &&
              "mul".equalsIgnoreCase(value)){
        return  mContext.getResources().getString(R.string.menu_arrays_hun_Multiple_Languages);
      }


	  for(int i=0;i<mMtsLanguageArrayList.size();i++){
		  String subtileValue=mMtsLanguageArrayList.get(i);
		  if(value.contains(subtileValue)){
			  index=i;
			  break;
		  }
	  }
	  String subtitleName=value;
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubitleNameByValue replace before------>subtitleName: " + subtitleName+"index: " + index);
	 if(index>=0){
		 subtitleName=mMtsLanguageArrayListstr.get(index);
		 //subtitleName=value.replace(mMtsLanguageArrayList.get(index), subtitleName);
	     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubitleNameByValue replace after------>subtitleName: " + subtitleName);
	     return subtitleName;
	 }


	 Locale locale = new Locale(value);
	 String mtsname= locale.getDisplayLanguage();
	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"locale language=="+mtsname);
	 if(!mtsname.equals(value)){
	      return mtsname;
	 }else {
	   if(!TextUtils.isEmpty(value)){
	     value = value.substring(0,1).toUpperCase(Locale.getDefault())+value.substring(1);
       }
     }
	 return value;
  }
  public void setTimeZone(int position) {
    // TODO Auto-generated method stub
    int[] timeZoneArray = mContext.getResources().getIntArray(
        R.array.menu_setup_us_timezone_values);
    if (position < 0 || position >= timeZoneArray.length) {
      return;
    }

    final AlarmManager alarm = (AlarmManager) mContext
        .getSystemService(Context.ALARM_SERVICE);

    String[] tmezoneStr = mContext.getResources().getStringArray(
        R.array.menu_setup_us_timezone_str);
    final int hours1 = 60 * 60000;

    final TimeZone tz = TimeZone.getTimeZone(tmezoneStr[position]);
    final long date = Calendar.getInstance().getTimeInMillis();
    final int offset = tz.getOffset(date);
    final int p = Math.abs(offset);
    final StringBuilder name = new StringBuilder();
    name.append("GMT");

    if (offset < 0) {
      name.append('-');
    } else {
      name.append('+');
    }

    name.append(p / (hours1));
    name.append(':');

    int min = p / 60000;
    min %= 60;

    if (min < 10) {
      name.append('0');
    }
    name.append(min);

    // Set Android's TimeZone
    alarm.setTimeZone(name.toString());
    // Set Linux-World's TimeZone
    // MtkTvTime.getInstance().setTimeZone(timeZoneArray[position] * 3600L);
  }

  public void setOSDLanguage(int choose) throws RemoteException {
    try {
      am = ActivityManagerNative.getDefault();
      config = am.getConfiguration();

      switch (choose) {
        case 0:
          config.locale = Locale.US;
          // tvEventCommand.setPrefLanuage(new String[] { "eng", "",
          // "", "" });
          break;
        case 1:
          config.locale = Locale.SIMPLIFIED_CHINESE;
          // tvEventCommand.setPrefLanuage(new String[] { "chn",
          // "chi", "", "" }) ;
          break;
        case 2:
          config.locale = Locale.TRADITIONAL_CHINESE;
          // tvEventCommand.setPrefLanuage(new String[] { "chn",
          // "chi", "", "" }) ;
          break;
        default:
          break;
      }
      // mask |= EventService.EVENT_CMD_DO_RESTART;
      // tvEventCommand.setDoRestart(true);
      // tvEventCommand.setCommandMask(mask);
      // mTV.getEventManager().setCommand(tvEventCommand);
      config.userSetLocale = true;
      am.updateConfiguration(config);
      BackupManager.dataChanged("com.android.providers.settings");
    } catch (RemoteException e) {
      throw e;
    }
  }

  public boolean getOSDLanguageIsChinese() {
    am = ActivityManagerNative.getDefault();
    try {
      config = am.getConfiguration();

    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
      return null != config && Locale.SIMPLIFIED_CHINESE.equals(config.locale);
  }

  public class MyLanguageData {
    public Locale local;
    public String tvAPILanguageStr;
  }

  private void setOSDLanguage(MyLanguageData languageData) {
    try {
      am = ActivityManagerNative.getDefault();
      config = am.getConfiguration();

      config.locale = languageData.local;

      config.userSetLocale = true;
      am.updateConfiguration(config);
      MtkTvConfig.getInstance().setLanguage(languageData.tvAPILanguageStr);
      BackupManager.dataChanged("com.android.providers.settings");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  // /
  // zh-CN is Chinese
  // en- is English
  // zh-TW is Chinese-TW
  private static String getLocaleLanguage() {
    Locale mLocale = Locale.getDefault();
    return String.format("%s-%s", mLocale.getLanguage(),
        mLocale.getCountry());
  }

  private static String getLocaleLanguage(Locale mLocale) {
    return String.format("%s-%s", mLocale.getLanguage(),
        mLocale.getCountry());
  }

  public void setLanguage(int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.setLanguage value: " + value);
    MyLanguageData languageData = getLauguageData(value);

    String systemLanguage = getLocaleLanguage();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.systemLanguage: " + systemLanguage);

    String selectedLanguage = getLocaleLanguage(languageData.local);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.selectedLanguage: " + selectedLanguage);

    if (systemLanguage.equalsIgnoreCase(selectedLanguage)) {
      // do nothing.
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ");
    } else {
      try {
        setOSDLanguage(languageData);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.setOSDLanguage: " + selectedLanguage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    SaveValue saveLanguage = SaveValue.getInstance(mContext);
    saveLanguage.saveValue(MenuConfigManager.OSD_LANGUAGE, value);
  }

  public int getLanguage(String cfgid) {
    String lang = MtkTvConfig.getInstance().getLanguage(cfgid);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getLanguage:" + lang);
    if (CommonIntegration.isEURegion()) {
      Configuration conf = mContext.getResources().getConfiguration();
      String language = conf.locale.getLanguage();

      return getLanguageOSDIndex(language);
    }
    return getLanguageOSDIndex(lang);
  }

  public void setAudioLanguage(String cfgid, int value) {
    String lang = getLauguageStr(value);
    MtkTvConfig.getInstance().setLanguage(cfgid, lang);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setAudioLanguage: "+cfgid+" "+lang);
    SaveValue saveLanguage = SaveValue.getInstance(mContext);
    saveLanguage.saveValue(cfgid, value);
  }

  public int getAudioLanguage(String cfgid) {
    String lang = MtkTvConfig.getInstance().getLanguage(cfgid);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getAudioLanguage: "+cfgid+" "+lang);
    lang=lang.trim().toLowerCase(Locale.getDefault());
    for (int mUnBeforeSubtitle = 0; mUnBeforeSubtitle < mSubtitleAudioUnormalBeforeListstr.size(); mUnBeforeSubtitle++){
      if(lang.contains(mSubtitleAudioUnormalBeforeListstr.get(mUnBeforeSubtitle))){
        lang = mSubtitleAudioUnormalAfterListstr.get(mUnBeforeSubtitle);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subtitle_change_value:"+lang);
        break;
      }
    }

    return getLanguageAudioIndex(lang);
  }

  public void setSubtitleLanguage(String cfgid, int value) {
    String  lang = mLanguageSubtitleArray.get(value);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSubtitleLanguage cfgid:" + cfgid + "lang:" + lang);
    MtkTvConfig.getInstance().setLanguage(cfgid, lang);
    
    if( (cfgid == MtkTvConfigType.CFG_SUBTITLE_SUBTITLE_LANG) && ("off".equalsIgnoreCase(lang)) ){
        if(MtkTvSubtitle.getInstance() != null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setSubtitleLanguage:off, stop subtitle.");
//            MtkTvSubtitle.getInstance().playStream(255);
        }
    }
  }

  public int getSubtitleLanguage(String cfgid) {
      if (MenuConfigManager.DIGITAL_SUBTITLE_LANG.equals(cfgid)){
        int subtitleEnable = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubtitleLanguage cfgid:" + cfgid + "subtitleEnable:" + subtitleEnable);
//        if (subtitleEnable == 0){
//            return 0;
//        }
      } else if (MenuConfigManager.DIGITAL_SUBTITLE_LANG_2ND.equals(cfgid)){
        int subtitleEnable2nd = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE_2ND);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubtitleLanguage cfgid:" + cfgid + "subtitleEnable2nd:" + subtitleEnable2nd);
//        if (subtitleEnable2nd == 0){
//            return 0;
//        }
      }

      String lang = MtkTvConfig.getInstance().getLanguage(cfgid);
      lang=lang.trim().toLowerCase(Locale.getDefault());
      for (int mUnBeforeSubtitle = 0; mUnBeforeSubtitle < mSubtitleAudioUnormalBeforeListstr.size(); mUnBeforeSubtitle++){
        if(lang.contains(mSubtitleAudioUnormalBeforeListstr.get(mUnBeforeSubtitle))){
          lang = mSubtitleAudioUnormalAfterListstr.get(mUnBeforeSubtitle);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subtitle_change_value:"+lang);
          break;
        }
      }

      int index = 7;
      if(mTV.isIDNCountry()||mTV.isMYSCountry()||mTV.isAUSCountry()||mTV.isVNMCountry()||mTV.isSQPCountry()){
           index = 5;
      }
      for (int i = 0; i < mLanguageSubtitleArray.size(); i++) {
        if (lang.equalsIgnoreCase(mLanguageSubtitleArray.get(i))) {
          index = i;
          break;
        }
      }
      //setSubtitleLanguage(cfgid,index);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSubtitleLanguage cfgid:" + cfgid + "lang:" + lang+",index:"+index);
      return index;
  }

  private int getLanguageOSDIndex(String lang) {
    int index = 0;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:" + lang);
    if (CommonIntegration.isUSRegion()) {
      if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_ENG)) {
        index = 0;
      } else if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_SPA)) {
        index = 1;
      } else if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_FRA)
          || lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_FRE)) {
        index = 2;
      } else {
        index = 0;
      }
    } else if (CommonIntegration.isSARegion()) {
      if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_ENG)) {
        index = 0;
      } else if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_POR)) {
        index = 1;
      } else if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_SPA)) {
        index = 2;
      } else {
        index = 0;
      }
    } else if (CommonIntegration.isEURegion()) {
      // for (int i = 0; i < mLanguageOSDArray.size(); i++) {
      // if (lang.equalsIgnoreCase(mLanguageOSDArray.get(i))) {
      // index = i;
      // break;
      // }
      // }

      index = ScanContent.getRegionEULanguageCodeList().indexOf(lang);
      index = Math.max(0, index);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index:" + index);
    return index;
  }

  private int getLanguageAudioIndex(String lang) {
    int index = 0;
    if (CommonIntegration.isUSRegion()) {
      for (int i = 0; i < mLanguageAudioArrayUS.size(); i++) {
        if (lang.equalsIgnoreCase(mLanguageAudioArrayUS.get(i))) {
          index = i;
          break;
        }
      }
    } else if (CommonIntegration.isSARegion()) {
      for (int i = 0; i < mLanguageAudioArraySA.size(); i++) {
        if (lang.equalsIgnoreCase(mLanguageAudioArraySA.get(i))) {
          index = i;
          break;
        }
      }
    } else if (CommonIntegration.isEURegion()) {
    	if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
    		List<String> langListTmp = mLanguageAudioArrayPA;
    		if(MtkTvConfig.getInstance().getCountry()
    				.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) {
    			langListTmp = mLanguageAudioArrayPANZL;
    		}
    		for (int i = 0; i < langListTmp.size(); i++) {
    			if (lang.equalsIgnoreCase(langListTmp.get(i))) {
    				index = i;
    				break;
    			}
    		}
    	} else {
    		for (int i = 0; i < mLanguageAudioArray.size(); i++) {
    			if (lang.equalsIgnoreCase(mLanguageAudioArray.get(i))) {
    				index = i;
    				break;
    			}
    			/*if (lang.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_GER)) {
    				index = 7;
    				break;
    			}*/
    		}
    	}
    }else if(CommonIntegration.isCNRegion()){
        for (int i = 0; i < mLanguageAudioArrayCN.size(); i++) {
            if (lang.equalsIgnoreCase(mLanguageAudioArrayCN.get(i))) {
                index = i;
                break;
            }
        }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getLanguageAudioIndex index: " + index);
    return index;
  }

  private MyLanguageData getLauguageData(int position) {
    MyLanguageData data = new MyLanguageData();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.mTV.isEURegion(): " + CommonIntegration.isEURegion());
    if (CommonIntegration.isCNRegion()) {
      data = getRegionCNLanguageData(position);
    } else if (CommonIntegration.isUSRegion()) {
      data = getRegionUSLanguageData(position);
    } else if (CommonIntegration.isEURegion()) {
      data = getRegionEULanguageData(position);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "osdLanguage.data: " + data.tvAPILanguageStr);
    } else if (CommonIntegration.isSARegion()) {
      data = getRegionSALanguageData(position);
    }
    return data;
  }

  private String getLauguageStr(int position) {
    String lang = "";
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.getLauguageStr() position: " + position);
    if (CommonIntegration.isCNRegion()) {
      lang = getRegionCNLanguageStr(position);
    } else if (CommonIntegration.isUSRegion()) {
      lang = getRegionUSLanguageStr(position);
    } else if (CommonIntegration.isEURegion()) {
      lang = getRegionEULanguageStr(position);
    } else if (CommonIntegration.isSARegion()) {
      lang = getRegionSALanguageStr(position);
    }
    return lang;
  }

  private MyLanguageData getRegionCNLanguageData(int position) {
    MyLanguageData data = new MyLanguageData();

    switch (position) {
      case 0:
        data.local = Locale.ENGLISH;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
        break;
      case 1:
        data.local = Locale.CHINESE;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_CHI;
        break;
      default:
        data.local = Locale.ENGLISH;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
        break;
    }
    return data;
  }

  private String getRegionCNLanguageStr(int position) {
      String lang = MtkTvConfigType.S639_CFG_LANG_ENG;
      List<String> langListTmp = mLanguageAudioArrayCN;
      if (position < langListTmp.size()) {
          lang = langListTmp.get(position);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRegionCNLanguageStr.lang: " + lang);
      return lang;
  }

  private MyLanguageData getRegionUSLanguageData(int position) {
    MyLanguageData data = new MyLanguageData();

    switch (position) {
      case 0:
        data.local = Locale.US;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
        break;
      case 1:
        data.local = new Locale("es", "ES");
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_SPA;
        break;
      case 2:
        data.local = Locale.FRANCE;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_FRA;
        break;
      default:
        data.local = Locale.US;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
        break;
    }
    return data;

  }

  private String getRegionUSLanguageStr(int position) {
    String lang = MtkTvConfigType.S639_CFG_LANG_ENG;
    List<String> langListTmp = mLanguageAudioArrayUS;
    if(position < langListTmp.size()){
      lang = langListTmp.get(position);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRegionUSLanguageStr.lang: " + lang);
    return lang;
  }

  private MyLanguageData getRegionSALanguageData(int position) {
    MyLanguageData data = new MyLanguageData();

    switch (position) {
      case 0:
        data.local = Locale.ENGLISH;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
        break;
      case 1:
        data.local = new Locale("pt", "PT");
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_POR;
        break;
      case 2:
        data.local = new Locale("es", "ES");
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_SPA;
        break;
      default:
        data.local = Locale.ENGLISH;
        data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
        break;
    }
    return data;

  }

  private String getRegionSALanguageStr(int position) {
    String lang = MtkTvConfigType.S639_CFG_LANG_ENG;
    List<String> langListTmp = mLanguageAudioArraySA;
    if(position < langListTmp.size()){
      lang = langListTmp.get(position);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRegionSALanguageStr.lang: " + lang);
    return lang;

  }

  private MyLanguageData getRegionEULanguageData(int position) {
    MyLanguageData data = new MyLanguageData();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRegionEULanguageData.position: " + position);

    MyLanguageData wizardData = ScanContent
        .getRegionEULanguageStrForMenu(mContext, position);

    data.local = wizardData.local;
    data.tvAPILanguageStr = wizardData.tvAPILanguageStr;

    return data;
  }

  private String getRegionEULanguageStr(int position) {
	  String lang = MtkTvConfigType.S639_CFG_LANG_ENG;
	  List<String> langListTmp = mLanguageAudioArray;
	  if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_PA)) {
		  langListTmp = mLanguageAudioArrayPA;
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRegionEULanguageStr.getCountry: " + MtkTvConfig.getInstance().getCountry());
		  if(MtkTvConfig.getInstance().getCountry()
				  .equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) {
			  langListTmp = mLanguageAudioArrayPANZL;
		  }
	  }
	  if (position < langListTmp.size()) {
		  lang = langListTmp.get(position);
	  }
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getRegionEULanguageStr.lang: " + lang);
	  return lang;
  }
}
