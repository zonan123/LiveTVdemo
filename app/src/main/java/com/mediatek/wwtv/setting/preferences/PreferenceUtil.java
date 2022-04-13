
package com.mediatek.wwtv.setting.preferences;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.provider.Settings;
import android.content.Intent;
import android.media.tv.TvTrackInfo;
import android.util.Log;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.util.RegionConst;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.fragments.BaseContentFragment;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.twoworlds.tv.MtkTvTimeshift;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.SwitchPreference;
import com.mediatek.twoworlds.tv.MtkTvSubtitle;

import java.util.List;
import mediatek.sysprop.VendorProperties;

public class PreferenceUtil {
  private static final String TAG = "PreferenceUtil";
  private volatile static PreferenceUtil mPreference = null;
  public static final String PARENT_PREFERENCE_ID = "parent";
  public static final String CHILD_PREFERENCE_ID = "child";
  public static  boolean isFromSubtitleTrackclick = false;
  public boolean isFromSubtitleTrackNoSignal = false;
  private Context mThemedContext;
  public MenuConfigManager mConfigManager = null;
  public LanguageUtil mOsdLanguage;
  private AsyncTimeTask att;
  private PreferenceUtil(Context themedContext) {
    mThemedContext = themedContext;
    mConfigManager = MenuConfigManager.getInstance(
        themedContext.getApplicationContext());
    mOsdLanguage = new LanguageUtil(
        mThemedContext.getApplicationContext());

  }

  private static synchronized void free(){
    if(mPreference != null){
      mPreference.mThemedContext = null;
    }
    mPreference = null;
  }

  public static synchronized PreferenceUtil getInstance(Context themedContext) {
    if (null == mPreference) {
      mPreference = new PreferenceUtil(themedContext);
      RxBus.instance.onEvent(ActivityDestroyEvent.class)
          .filter(it -> {
            synchronized (PreferenceUtil.class){
              if(mPreference == null || mPreference.mThemedContext == null){
                return true;
              }
              if (it.activityClass == mPreference.mThemedContext.getClass()){
                free();
                return true;
              }
              return false;
            }
          })
          .firstElement()
          .subscribe();

    }
    if(mPreference.mThemedContext != themedContext) {
      mPreference.mThemedContext = themedContext;
    }
    if (themedContext == null){
      Log.e(TAG, "ThemedContext is null", new Exception("ThemedContext is null"));
    }
    return mPreference;
  }


  /**
   * create CharSequence
   * like "0", "1", "2", ...
   */
  public static String[] getCharSequence(int size) {
    String[] seq = new String[size];
    for (int i = 0; i < size; i++) {
      seq[i] = String.valueOf(i);
    }

    return seq;
  }

  /**
   * create Fragment Preference
   */
  public Preference createFragmentPreference(String key, int resTitle,
      boolean enabled, String className) {
    final Preference preference = createPreferenceInternal(key);
    preference.setTitle(resTitle);
    preference.setFragment(className);
    preference.setEnabled(enabled);
    return preference;
  }

  /**
   * create Preference
   */
  private Preference createPreferenceInternal(String key) {
    final Preference preference = new Preference(mThemedContext);
    preference.setKey(key);

    // important here!!!
    // this is used to load content of sub page for BaseContentFragment
    preference.getExtras().putCharSequence(
        PreferenceUtil.PARENT_PREFERENCE_ID, preference.getKey());
    return preference;
  }

  /**
   * create Preference
   */
  public Preference createPreference(String key, int resTitle) {
    final Preference preference = createPreferenceInternal(key);
    preference.setTitle(resTitle);
    preference.setFragment(BaseContentFragment.class.getName());

    return preference;
  }

  /**
   * create Preference
   */
  public Preference createPreference(String key, String title) {
    final Preference preference = createPreferenceInternal(key);
    preference.setTitle(title);
    preference.setFragment(BaseContentFragment.class.getName());

    return preference;
  }

  /**
   * create Preference
   */
  public Preference createPreference(
      String key,
      int resTitle,
      Intent intent) {
    final Preference preference = createPreferenceInternal(key);
    preference.setTitle(resTitle);
    preference.setIntent(intent);

    return preference;
  }

  /**
   * create Preference
   */
  public Preference createPreference(
      String key,
      String title,
      Intent intent) {
    final Preference preference = createPreferenceInternal(key);
    preference.setTitle(title);
    preference.setIntent(intent);

    return preference;
  }
  public PicturePreference createPicturePreference(
          String key,
          String title,
          Intent intent) {
      final PicturePreference preference = new PicturePreference(mThemedContext);
      preference.setKey(key);

      preference.getExtras().putCharSequence(
          PreferenceUtil.PARENT_PREFERENCE_ID, preference.getKey());
      preference.setIntent(intent);
      
      return preference;
  }

  /**
   * create Preference
   */
  public Preference createPreference(
      String key,
      int resTitle,
      boolean status) {
    final Preference preference = new Preference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(resTitle);
    preference.setEnabled(status);
    preference.setOnPreferenceChangeListener(mChangeListener);

    return preference;
  }

  /**
   * create Preference
   */
  public Preference createPreference(
      String key,
      String title,
      boolean status) {
    final Preference preference = new Preference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(title);
    preference.setEnabled(status);
    preference.setOnPreferenceChangeListener(mChangeListener);

    return preference;
  }

  /**
   * create ProgressPreference
   */
  public Preference createProgressPreference(
      String key,
      int resTitle,
      boolean isPositionView,
      int minValue,
      int maxValue,
      int defValue) {
    final ProgressPreference preference = new ProgressPreference(mThemedContext);
    preference.setPositionView(isPositionView);
    preference.setKey(key);
    preference.setTitle(resTitle);
    // preference.setSummary(resTitle);
    preference.setMinValue(minValue);
    preference.setMaxValue(maxValue);
    preference.setCurrentValue(defValue);
    preference.setOnPreferenceChangeListener(mChangeListener);

    return preference;
  }

  /**
   * create ProgressPreference
   */
  public Preference createProgressPreference(
      String key,
      int resTitle,
      boolean isPositionView) {
    int min = mConfigManager.getMin(key);
    int max = mConfigManager.getMax(key);
    int cur = mConfigManager.getDefault(key);

    return createProgressPreference(key, resTitle, isPositionView, min, max, cur);
  }

  /**
   * create SwitchPreference
   */
  public Preference createSwitchPreference(
      String key,
      int resTitle,
      boolean checked) {
    final SwitchPreference preference = new SwitchPreference(mThemedContext);
    preference.setPersistent(false);
    preference.setTitle(resTitle);
    preference.setKey(key);
    // preference.setSummary(resTitle);
    preference.setChecked(checked);
    preference.setOnPreferenceChangeListener(mChangeListener);

    return preference;
  }

  /**
   * create SwitchPreference
   */
  public Preference createSwitchPreference(
      String key,
      String title,
      boolean checked) {
    final SwitchPreference preference = new SwitchPreference(mThemedContext);
    preference.setPersistent(false);
    preference.setTitle(title);
    preference.setKey(key);
    // preference.setSummary(resTitle);
    preference.setChecked(checked);
    preference.setOnPreferenceChangeListener(mChangeListener);

    return preference;
  }

  private ListPreference createListPreferenceInternal(
      String key,
      boolean status,
      String[] entries,
      String[] entryValues,
      String defValue) {
    final ListPreference preference = new ListPreference(mThemedContext);

    preference.setKey(key);
    preference.setPersistent(false);
    preference.setEnabled(status);

    preference.setEntries(entries);
    preference.setEntryValues(entryValues);
    preference.setValue(defValue);

    for (int i = 0; i < entryValues.length; i++) {
      if (entryValues[i].equals(defValue)) {
        preference.setSummary(entries[i]);
      }
    }

    preference.setOnPreferenceChangeListener(mChangeListener);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create ListPreference
   */
  public Preference createListPreference(
      String key,
      String title,
      boolean status,
      String[] entries,
      int defValue) {
    final ListPreference preference = createListPreferenceInternal(
        key,
        status,
        entries,
        PreferenceUtil.getCharSequence(entries.length),
        String.valueOf(defValue));

    preference.setTitle(title);
    preference.setDialogTitle(title);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create ListPreference
   */
  public Preference createListPreference(
      String key,
      int resTitle,
      boolean status,
      String[] entries,
      int defValue) {
    final ListPreference preference = createListPreferenceInternal(
        key,
        status,
        entries,
        PreferenceUtil.getCharSequence(entries.length),
        String.valueOf(defValue));

    preference.setTitle(resTitle);
    preference.setDialogTitle(resTitle);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create ListPreference
   */
  public Preference createListPreference(
      String key,
      String title,
      boolean status,
      String[] entries,
      String[] entryValues,
      String defValue) {
    final ListPreference preference = createListPreferenceInternal(
        key,
        status,
        entries,
        entryValues,
        defValue);

    preference.setTitle(title);
    preference.setDialogTitle(title);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create ListPreference
   */
  public Preference createListPreference(
      String key,
      int resTitle,
      boolean status,
      String[] entries,
      String[] entryValues,
      String defValue) {
    final ListPreference preference = createListPreferenceInternal(
        key,
        status,
        entries,
        entryValues,
        defValue);

    preference.setTitle(resTitle);
    preference.setDialogTitle(resTitle);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create Dialog Preference
   */
  public DialogPreference createDialogPreference(String key, int resTitle, Dialog dialog) {
    final DialogPreference preference = new DialogPreference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(resTitle);
    preference.setDialog(dialog);

    return preference;
  }

  /**
   * create Dialog Preference
   */
  public DialogPreference createDialogPreference(String key, String title, Dialog dialog) {
    final DialogPreference preference = new DialogPreference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(title);
    preference.setDialog(dialog);

    return preference;
  }
  public DialogPreference createDialogPreference(String key, int resTitle, Dialog dialog, boolean needSubScreen) {
      final DialogPreference preference = new DialogPreference(mThemedContext);

      preference.setKey(key);
      preference.setTitle(resTitle);
      preference.setDialog(dialog);
      if(needSubScreen){
          preference.getExtras().putCharSequence(
                  PreferenceUtil.PARENT_PREFERENCE_ID, preference.getKey());
      }

      return preference;
  }

  /**
   * create ClickPreference
   */
  public Preference createClickPreference(
      String key,
      int resTitle) {
    final Preference preference = new Preference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(resTitle);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create ClickPreference
   */
  public Preference createClickPreference(
      String key,
      String title) {
    final Preference preference = new Preference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(title);
    preference.setOnPreferenceClickListener(mClickListener);

    return preference;
  }

  /**
   * create ClickPreference
   */
  public Preference createClickPreference(
      String key,
      int resTitle,
      OnPreferenceClickListener clickListener) {
    final Preference preference = new Preference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(resTitle);
    preference.setOnPreferenceClickListener(clickListener);

    return preference;
  }

  /**
   * create ClickPreference
   */
  public Preference createClickPreference(
      String key,
      String title,
      OnPreferenceClickListener clickListener) {
    final Preference preference = new Preference(mThemedContext);

    preference.setKey(key);
    preference.setTitle(title);
    preference.setOnPreferenceClickListener(clickListener);

    return preference;
  }
  /**
   * create ClickPreference
   */
  public Preference createClickPreference(
          String key,
          int resTitle,
          Intent intent,
          OnPreferenceClickListener clickListener) {
      final Preference preference = createPreferenceInternal(key);
      preference.setTitle(resTitle);
      preference.setIntent(intent);
      preference.setOnPreferenceClickListener(clickListener);

      return preference;
  }

  public Preference createPreferenceWithSummary(
          String key,
          int title,
          String summary) {
        final Preference preference = new Preference(mThemedContext);
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummary(summary);

        return preference;
      }

  private void setupAlphonsoService(){
      Intent pkgitent = new Intent();
      pkgitent.setPackage("com.mediatek.alphonso.acr");
      pkgitent.setAction("mtk.intent.setup.AlphonsoService.Optin");
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "broadcastIntent mtk.intent.setup.AlphonsoService.Optin");
      mThemedContext.sendBroadcast(pkgitent);
  }

  private void cleanupAlphonsoSDK(){
  /*
      String PACKAGE_NAME = "com.mediatek.alphonso.acr";
      String MYACR_CLASS_NAME = "com.mediatek.alphonso.acr.MyACR";

      try {
          String apkName = mThemedContext.getPackageManager().getApplicationInfo(PACKAGE_NAME, 0).sourceDir;
          PathClassLoader myClassLoader = new dalvik.system.PathClassLoader(apkName, ClassLoader.getSystemClassLoader());
          Class clazz = Class.forName(MYACR_CLASS_NAME, true, myClassLoader);
          Method method = clazz.getMethod("cleanupAlphonsoSDK");
          method.invoke(null);
          Log.d(TAG,"cleanupAlphonsoSDK be called!");
      }
      catch(Exception e){
          e.printStackTrace();
      }
   */
    Intent pkgitent = new Intent();
    pkgitent.setPackage("com.mediatek.alphonso.acr");
    pkgitent.setAction("mtk.intent.cleanup.AlphonsoSDK");
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "broadcastIntent mtk.intent.cleanup.AlphonsoSDK");
    mThemedContext.sendBroadcast(pkgitent);
  }

  private void onRecommendationStatusChanged(Boolean enable){
	/*
      String PACKAGE_NAME = "com.mediatek.alphonso.acr";
      String MYACR_CLASS_NAME = "com.mediatek.alphonso.acr.MyACR";

      try {
          String apkName = mThemedContext.getPackageManager().getApplicationInfo(PACKAGE_NAME, 0).sourceDir;
          PathClassLoader myClassLoader = new dalvik.system.PathClassLoader(apkName, ClassLoader.getSystemClassLoader());
          Class clazz = Class.forName(MYACR_CLASS_NAME, true, myClassLoader);
          Method method = clazz.getMethod("onRecommendationStatusChanged", Boolean.class);
          method.invoke(null, enable);
          Log.d(TAG,"onRecommendationStatusChanged be called!");
      }
      catch(Exception e){
          e.printStackTrace();
      }
     */
    SaveValue save = SaveValue.getInstance(mThemedContext);
	save.writeWorldStringValue(mThemedContext, "persist.vendor.sys.alphonso.acr.recommendations", enable ? "1" : "0", true);
    Intent pkgitent = new Intent();
    pkgitent.setPackage("com.mediatek.alphonso.acr");
    pkgitent.setAction("mtk.intent.Recommendation.StatusChanged");
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mtk.intent.Recommendation.StatusChanged");
	pkgitent.putExtra("status",enable);
    mThemedContext.sendBroadcast(pkgitent);
  }

  /**
   * notify for value changed, you should add your flow into setValue method but not here.
   */
  OnPreferenceChangeListener mChangeListener = new OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceChange " + preference +
          "," + preference.getKey() + "," + newValue);

            if (preference.getKey().startsWith(MenuConfigManager.PARENTAL_INPUT_BLOCK_SOURCE)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
				/*
                boolean block = EditChannel.getInstance(mThemedContext.getApplicationContext()).isInputBlock((String)preference.getTitle());
                boolean isoper = false;
                int value = Integer.parseInt((String)newValue);

                if ((block && 0 == value) || (!block && 1 == value)){
                    isoper = true;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChangeListener:input block===" + block + ",isoper=" + isoper);
                if (isoper) {
                    EditChannel.getInstance(mThemedContext.getApplicationContext()).blockInput((String)preference.getTitle(), !block);
                }*/
            } else if (preference.getKey().equals(MenuConfigManager.SLEEP_TIMER)) {
              ListPreference tmp = (ListPreference) preference;
              int value = Integer.parseInt((String)newValue);
              PreferenceData.getInstance(mThemedContext.getApplicationContext())
              .handleSleepTimerChange(tmp, value);
            } else if(preference.getKey().equals(MenuConfigManager.AUTO_SYNC)){
            	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.AUTO_SYNC"+MenuConfigManager.AUTO_SYNC);
            	 if(att!=null){
            		 if(att.getStatus()==AsyncTask.Status.RUNNING){
            			 att.cancel(true);
            			 att=null;
            		 }
            		 att=new AsyncTimeTask(preference);
            		 att.execute((String)newValue);
            	 }else{
            		 att=new AsyncTimeTask(preference);
            		 att.execute((String)newValue);
            	 }
			} else if (preference.getKey().equals(
					MenuConfigManager.SETUP_SHIFTING_MODE)) {
				if (newValue instanceof Boolean) {
					newValue = (Boolean) newValue ? 1 : 0;
				}
				mConfigManager.setValue(preference.getKey(), newValue);
				if (newValue.equals(0)) {
                    TifTimeShiftManager.getInstance().stop();
                    TifTimeShiftManager.getInstance().stopAll();
					MtkTvTimeshift.getInstance().setAutoRecord(false);
				}else if (newValue.equals(1)) {
					if (VendorProperties.mtk_tif_timeshift().orElse(0) == 1) {
						MtkTvTimeshift.getInstance().setAutoRecord(true);
						com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,
								"MtkTvTimeshift.getInstance().setAutoRecord(true)");
					} else {
						com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "vendor.mtk.tif.timeshift != 1 ");
					}
				}

			}else if(preference.getKey().contains(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING)){
	            int itemPosition=new Integer(preference.getKey().replace(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING, ""));
	            int value = Integer.parseInt((String)newValue);
	            SaveValue.getInstance(mThemedContext).saveValue(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING, itemPosition);
	            SaveValue.getInstance(mThemedContext).saveValue(MenuConfigManager.SETUP_REGION_SETTING_SELECT, value);
	            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"item positon:"+itemPosition+",select position:"+value);
	            int areaCode=RegionConst.getEcuadorAreaCodeArray(itemPosition)[value];
	            TVContent.getInstance(mThemedContext).setConfigValue(MtkTvConfigTypeBase.CFG_SET_AND_GET_AREA_CODE, areaCode);
			}else if (preference.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_LUZON)
                    || preference.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS)
                    || preference.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_MINDANAO)
                        ){
			    int itemPosition=0;
			    int areaCode=0;
			    int value = Integer.parseInt((String)newValue);
			    if(preference.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_LUZON)){
			        itemPosition=Integer.valueOf(preference.getKey().replace(MenuConfigManager.SETUP_REGION_SETTING_LUZON, ""));
			        SaveValue.getInstance(mThemedContext).saveStrValue(MenuConfigManager.SETUP_REGION_SETTING,MenuConfigManager.SETUP_REGION_SETTING_LUZON);
			        areaCode=RegionConst.phiCityAreaCodeLuzong[itemPosition][value];
			    }else if(preference.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS)){
			        itemPosition=Integer.valueOf(preference.getKey().replace(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS, ""));
			        SaveValue.getInstance(mThemedContext).saveStrValue(MenuConfigManager.SETUP_REGION_SETTING,MenuConfigManager.SETUP_REGION_SETTING_VISAYAS);
			        areaCode=RegionConst.phiCityAreaCodeVisayas[itemPosition][value];
			    }else{
			        itemPosition=Integer.valueOf(preference.getKey().replace(MenuConfigManager.SETUP_REGION_SETTING_MINDANAO, ""));
			        SaveValue.getInstance(mThemedContext).saveStrValue(MenuConfigManager.SETUP_REGION_SETTING,MenuConfigManager.SETUP_REGION_SETTING_MINDANAO);
			        areaCode=RegionConst.phiCityAreaCodeMindanao[itemPosition][value];
			    }
                SaveValue.getInstance(mThemedContext).saveValue(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING, itemPosition);
                SaveValue.getInstance(mThemedContext).saveValue(MenuConfigManager.SETUP_REGION_SETTING_SELECT, value);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"item positon:"+itemPosition+",select position:"+value);
                TVContent.getInstance(mThemedContext).setConfigValue(MtkTvConfigTypeBase.CFG_SET_AND_GET_AREA_CODE, areaCode);
			} else if(preference.getKey().equals(MenuConfigManager.PICTURE_MODE)) {
				Log.d(TAG, "set picture mode" + preference.getKey() + " " +MenuConfigManager.PICTURE_MODE_dOVI);
				ListPreference listPreference = null;
				if(preference instanceof ListPreference) {
					listPreference =(ListPreference)preference;
				}
				int value = Integer.valueOf((String)newValue);
				Log.d(TAG, "picture mode value is" + value);
				if(listPreference != null) {
					CharSequence[] entry = listPreference.getEntries();
					if(value >= 0 && value < entry.length) {
						listPreference.setSummary(entry[value]);
					}
				}
				if(MenuConfigManager.PICTURE_MODE_dOVI) {
					if(value == 0) {
						value = 5;
					} else if(value == 1) {
						value = 6;
					}
					mConfigManager.setValue(preference.getKey(), value);
				} else {
					if (newValue instanceof Boolean) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "instanceof Boolean");
						newValue = ((Boolean) newValue) ? 1 : 0;
					}
					mConfigManager.setValue(preference.getKey(), newValue);
				}
			}

			else if(preference.getKey().equals(MenuConfigManager.POWER_SETTING_CONFIG_VALUE)){
				Log.d(TAG, "power setting mode" + preference.getKey() + " " +MenuConfigManager.POWER_SETTING_CONFIG_VALUE);
				ListPreference listPreference = null;
				if(preference instanceof ListPreference) {
					listPreference =(ListPreference)preference;
				}
				int value = Integer.valueOf((String)newValue)+1;
				Log.d(TAG, "power setting value is" + value);
				if(listPreference != null) {
					CharSequence[] entry = listPreference.getEntries();
					if(value >= 0 && value < entry.length) {
						listPreference.setSummary(entry[value-1]);
					}
				}

				 SaveValue.saveWorldValue(mThemedContext,MenuConfigManager.POWER_SETTING_VALUE,value,true);
			}else if (preference.getKey().contains(MenuConfigManager.SOUNDTRACKS_GET_STRING)){
                String value = preference.getKey().substring(MenuConfigManager.SOUNDTRACKS_GET_STRING.length() + 1);

                try {
                    //int va = Integer.parseInt(value);
                    Log.d(TAG, "SOUNDTRACKS_GET_STRING value" + value);
                    //mConfigManager.setValue(MenuConfigManager.SOUNDTRACKS_SET_SELECT, va);
                    TurnkeyUiMainActivity.getInstance().getTvView().selectTrack(TvTrackInfo.TYPE_AUDIO,value);
                }catch (Exception e){
                    Log.d(TAG, "SOUNDTRACKS_GET_STRING error" );
                }
            }else if (preference.getKey().equals(MenuConfigManager.AUTOMATIC_CONTENT)){
                try {
                    int value = (boolean)newValue ? 1 : 0;
					Log.d(TAG, "value:" + value);
					SaveValue save = SaveValue.getInstance(mThemedContext);
					int propvalue = save.readWorldIntValue(mThemedContext,"persist.vendor.sys.alphonso.acr");
					Log.d(TAG, "propvalue:" + propvalue);
                    //SaveValue.getInstance(mThemedContext).saveValue(MenuConfigManager.AUTOMATIC_CONTENT,value);
					Preference preRecommendations = PreferenceData.getInstance(mThemedContext.getApplicationContext())
							.getData().findPreference(MenuConfigManager.RECOMMENDATIONS);
                    if(value == 0){
						save.writeWorldStringValue(mThemedContext, "persist.vendor.sys.alphonso.acr", "0", true);
                    	preRecommendations.setEnabled(false);
                        //if (propvalue == 1) {
                            cleanupAlphonsoSDK();
                            //Log.d(TAG, "change persist.vendor.sys.alphonso.acr to 0");
                        //}
                    }else{
                    	//preRecommendations.setEnabled(true);
                        setupAlphonsoService();
                    }
                    Log.d(TAG, "AUTOMATIC_CONTENT value :" + value);
                }catch (Exception e){
                    Log.d(TAG, "SUBTITLE_GET_STRING error" );
                }
            }else if (preference.getKey().equals(MenuConfigManager.RECOMMENDATIONS)) {
				//TODO: onPreferenceChange for Recommendations
            	 int value = (boolean)newValue ? 1 : 0;
                 Log.d(TAG, "value:" + value);
				 SaveValue save = SaveValue.getInstance(mThemedContext);
				 save.readWorldIntValue(mThemedContext,"persist.vendor.sys.alphonso.acr.recommendations");
                 if(value == 0){
                     onRecommendationStatusChanged(false);//Opt out
                 }else{
                     onRecommendationStatusChanged(true);//Opt in
                 }
			} else if(preference.getKey().equals(MenuConfigManager.SUBTITLE_TRACKS)){
              try {
                if(Integer.valueOf((String)newValue) == 0xFF){
                  //int result = MtkTvSubtitle.getInstance().nextStream();
                  MtkTvSubtitle.getInstance().playStream(255);
                } else{
                  TurnkeyUiMainActivity.getInstance().getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE,(String)newValue);
                }
                Log.d(TAG, "SOUNDTRACKS_GET_STRING value" + newValue);
                SaveValue.getInstance(mThemedContext).saveValue(MenuConfigManager.SUBTITLE_TRACKS,Integer.valueOf((String)newValue));
                if(SundryImplement.getInstanceNavSundryImplement(mThemedContext).isFreeze()){
                  SundryImplement.getInstanceNavSundryImplement(mThemedContext).setFreeze(false);
                }
              }catch (Exception e){
                Log.d(TAG, "SUBTITLE_GET_STRING error" );
              }
            }else if(preference.getKey().equals(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_TUNER)){
                ListPreference bandFreqPre = (ListPreference)PreferenceData.getInstance(mThemedContext.getApplicationContext())
                        .getData().findPreference(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_BANDFREQ);
                int attennaType = mConfigManager.getDefault(MenuConfigManager.DVBS_SAT_ATENNA_TYPE);

                List<String> frequencyList = ScanContent.getSingleCableFreqsList(mThemedContext, Integer.parseInt(newValue+""));
                String[] freqArray = frequencyList.toArray(new String[0]);
                //int bandFreqTypeIndex = 0;
              List<Integer> defBandFreqList;
              String defBandFreq = freqArray[0];
              int userBandIndex = Integer.parseInt(newValue.toString());
              if(attennaType == MenuConfigManager.DVBS_ACFG_UNICABLE1){
                defBandFreqList = DataSeparaterUtil.getInstance().getDefaultUnicable1Frequency();
              }else{
                defBandFreqList = DataSeparaterUtil.getInstance().getDefaultUnicable2Frequency();
              }
              if(defBandFreqList.size() > userBandIndex){
                defBandFreq = defBandFreqList.get(userBandIndex).toString();
              }
                bandFreqPre.setEntries(freqArray);
                bandFreqPre.setEntryValues(freqArray);
                if(mThemedContext.getString(R.string.dvbs_band_freq_user_define).equals(freqArray[0]) || !frequencyList.contains(defBandFreq)){
                  bandFreqPre.setValue(mThemedContext.getString(R.string.dvbs_band_freq_user_define));
                  if(mThemedContext.getString(R.string.dvbs_band_freq_user_define).equals(defBandFreq)){
                    ScanContent.getInstance(mThemedContext).saveDVBUserBand(mThemedContext, Integer.parseInt(newValue+""));
                  }else {
                    ScanContent.getInstance(mThemedContext).saveDVBSBandAndFreq(mThemedContext, Integer.parseInt(newValue+""), Integer.parseInt(defBandFreq));
                  }
                }else {
                  bandFreqPre.setValue(defBandFreq);
                  ScanContent.getInstance(mThemedContext).saveDVBSBandAndFreq(mThemedContext, Integer.parseInt(newValue+""), Integer.parseInt(defBandFreq));
                }
			}else if(preference.getKey().equals(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_BANDFREQ)){
			    if(mThemedContext.getString(R.string.dvbs_band_freq_user_define).equals(newValue.toString())){
			      PreferenceData.getInstance(mThemedContext.getApplicationContext())
                            .getData().findPreference(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_USERDEF).setVisible(false);
			    }else{
			        ScanContent.getInstance(mThemedContext).saveDVBSBandFreq(mThemedContext, Integer.parseInt(newValue+""));
			    }
			}else if (preference.getKey().equals(MenuConfigManager.GINGA_ENABLE)){
                if (newValue instanceof Boolean) {
                    newValue = ((Boolean) newValue) ? 1 : 0;
                }
                    mConfigManager.setValue(preference.getKey(), newValue);
                    int status = mConfigManager.getDefault(MenuConfigManager.SETUP_ENABLE_CAPTION);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "GINGA_SETUP Boolean :"+newValue);
                    if((Integer)newValue == 1
                            && status != 1){
                        mConfigManager.setValue(MenuConfigManager.SETUP_ENABLE_CAPTION, 1);
                    }
                
            }else if (preference.getKey().equals(MenuConfigManager.SETUP_ENABLE_CAPTION)){
                if (newValue instanceof Boolean) {
                    newValue = ((Boolean) newValue) ? 1 : 0;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SETUP_ENABLE_CAPTION int :"+newValue);
                mConfigManager.setValue(preference.getKey(), newValue);
                int status = mConfigManager.getDefault(MenuConfigManager.GINGA_ENABLE);
                if(Integer.valueOf((String)newValue) != 1
                        && status == 1){
                    mConfigManager.setValue(MenuConfigManager.GINGA_ENABLE, 0);
                }
            }
			else{
				if (newValue instanceof Boolean) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "instanceof Boolean");
					newValue = ((Boolean) newValue) ? 1 : 0;
				}
				mConfigManager.setValue(preference.getKey(), newValue);
			}
      PreferenceData.getInstance(mThemedContext.getApplicationContext())
          .invalidate(preference.getKey(), newValue);
      return true;
    }
  };

  class AsyncTimeTask extends AsyncTask<String,String,String>{
	  Preference taskPreference;
	  AsyncTimeTask(Preference taskPreference){
		  this.taskPreference=taskPreference;
	  }
		@Override
		protected String doInBackground(String... arg0) {
	      	int value = Integer.parseInt(arg0[0]);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("Adapter", "setTimeSyncSource :" + value);
           mConfigManager.setValue(taskPreference.getKey(), value);
           MtkTvTime.getInstance().setTimeSyncSource(value);
           Settings.Global.putInt(mThemedContext.getApplicationContext().getContentResolver(),
           Settings.Global.AUTO_TIME, (value == 2)? 1 : 0);
			return arg0[0];
		}
	  }

  OnPreferenceClickListener mClickListener = new OnPreferenceClickListener() {
    @Override
    public boolean onPreferenceClick(Preference preference) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceClick " + preference+"---------getkey:"+preference.getKey());



      mConfigManager.setValueDefault(preference.getKey());
      if (MenuConfigManager.RESET_SETTING.equals(preference.getKey())) {
          Message msg = mHandler.obtainMessage();
          msg.what = MESSAGE_RESET;
          mHandler.sendMessageDelayed(msg, DELAY_MILLIS);
      }else if(MenuConfigManager.SUBTITLE_TRACKS.equals(preference.getKey())){
        isFromSubtitleTrackclick = true;
        isFromSubtitleTrackNoSignal = true;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFromSubtitleTrackclick");

      }
      return true;
    }
  };
  public static final int MESSAGE_RESET = 10001;
  public static final int DELAY_MILLIS = 300;
  public Handler mHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "msg.what: " + msg.what);
          switch(msg.what) {
              case MESSAGE_RESET:
                  mHandler.removeMessages(MESSAGE_RESET);
                  break;
              default:
                break;
          }
      }
  };
}
