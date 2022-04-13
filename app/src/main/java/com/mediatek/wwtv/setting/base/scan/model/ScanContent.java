package com.mediatek.wwtv.setting.base.scan.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.mediatek.twoworlds.tv.MtkTvChannelListBase;

import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.MtkTvScanTpInfo;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase.TunerPolarizationType;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase.ScanDvbcCountryId;
import com.mediatek.twoworlds.tv.MtkTvScanDvbcBase.ScanDvbcOperator;
import com.mediatek.twoworlds.tv.MtkTvScanDvbtBase.LcnConflictGroup;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvFreqChgCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvFreqChgParamBase;
import com.mediatek.wwtv.setting.SatActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.setting.util.SatDetailUI;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.LanguageUtil.MyLanguageData;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.PartnerSettingsConfig;
import com.mediatek.wwtv.tvcenter.util.SatelliteProviderManager;
import com.mediatek.wwtv.tvcenter.util.SatelliteConfigEntry;
import com.mediatek.wwtv.tvcenter.util.CountryConfigEntry;

import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import android.os.SystemClock;

public class ScanContent {

    private static final boolean DVBS_DEV_ING = false;

	private static ScanContent mTVContent;
	//private static Context mContext;

	public static final String SUSPEND_KEY = "debug.mtk.tkui.cancel_scan";
	private static final String TAG = "ScanContent";
	public final static String SAT_BRDCSTER = MtkTvConfigTypeBase.CFG_BS_BS_SAT_BRDCSTER; // DVBS OP
	public static final String SAT_ANTENNA_TYPE = MtkTvConfigTypeBase.CFG_BS_BS_SAT_ANTENNA_TYPE;
	public static boolean mDvbsNeedTunerReset;
	public static CableOperator mLastDvbcOperator = null;

	public static int LNB_CONFIG[][];
	private static final int LNB_SINGLE_FREQ = 1;
	private static final int LNB_DUAL_FREQ = 2;

	public static boolean isCamScanUI = false;
	public static MtkTvScanTpInfo mCamTpInfo = null;
	private static SatelliteProviderManager satelliteProviderManager;
	private static boolean mTpHasModified;
	public static int mCurrentScanType;
	public static int mCurrentStoreType;


	enum TunerMode {
		CABLE, DVBT, DVBS
	}

	ScanContent(Context activity) {
		super();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("ScanMode:" + activity);
		//mContext = activity;
	}

	public synchronized static ScanContent getInstance(Context activity) {
		// TODO Auto-generated method stub
		if (mTVContent == null) {
			mTVContent = new ScanContent(activity);
			satelliteProviderManager = new SatelliteProviderManager(activity);
			initLNBConfig();
		}
		return mTVContent;
	}

	public static void initLNBConfig() {
		LNB_CONFIG = new int[11][3];
		LNB_CONFIG[0] = new int[]{ 9750,  10600, 11700};
		LNB_CONFIG[1] = new int[]{ 9750,  10700, 11700};
		LNB_CONFIG[2] = new int[]{ 9750,  10750, 11700};
		LNB_CONFIG[3] = new int[]{ 5150,  0, 0};
		LNB_CONFIG[4] = new int[]{ 5750,  0, 0};
		LNB_CONFIG[5] = new int[]{ 9750,  0, 0};
		LNB_CONFIG[6] = new int[]{ 10600,  0, 0};
		LNB_CONFIG[7] = new int[]{ 10750,  0, 0};
		LNB_CONFIG[8] = new int[]{ 11250,  0, 0};
		LNB_CONFIG[9] = new int[]{ 11300,  0, 0};
		LNB_CONFIG[10] = new int[]{ 11475,  0, 0};
	}

	/**
	 * get operator list by country id.
	 *
	 * @param context
	 * @return the operator list,like this {upc, other}
	 */
	public static List<String> getCableOperationList(Context context) {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getCableOperationList");
	  String country = MtkTvConfig.getInstance().getCountry();
	  CountryConfigEntry countryConfigEntry = PartnerSettingsConfig.getCountryConfigMap().get(country);
	  List<String> result = new ArrayList<String>();
	  if(countryConfigEntry != null && !TextUtils.isEmpty(countryConfigEntry.dvbc_operators)) {
	    String[] strings = countryConfigEntry.dvbc_operators.trim().split(",");
	    String[] operats = context.getResources().getStringArray(R.array.dvbc_operators_eu);
      for (String string : strings) {
        try {
          int operator = Integer.parseInt(string);
          if(country.equalsIgnoreCase(MtkTvConfigTypeBase.S3166_CFG_COUNT_IRL) &&
              operator == 1) {
            result.add(context.getString(R.string.dvbc_operator_virgin_media));
          } else if(operator >= 0 && operator < operats.length){
            result.add(operats[operator]);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
	  } else {
	    /*int countryID = CountrysIndex.reflectCountryStrToInt(country);
	    result = getCableOperationList(context, countryID);*/
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"CountryConfigEntry null or dvbc_operators empty");
	  }
	  return result;
	}

	public static boolean isCountryPT() {
		int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig
				.getInstance().getCountry());
		return countryID == CountrysIndex.CTY_22_Portugal_PRT;
	}

	public static boolean isCountryBel() {
	    int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig
                .getInstance().getCountry());
	    return countryID == CountrysIndex.CTY_2_Belgium_BEL;
	}

	public static boolean isCountryDenmark() {
        return MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigTypeBase.S3166_CFG_COUNT_DNK);
    }

	public static boolean isCountryIre() {
	    int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig
                .getInstance().getCountry());
	    return countryID == CountrysIndex.CTY_20_Ireland_IRL;
	}

	public static boolean isCountryIta() {
		int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig
				.getInstance().getCountry());
		return countryID == CountrysIndex.CTY_11_Italy_ITA;
	}

	//New Zealand
	public static boolean isCountryNZL() {
        return MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigTypeBase.S3166_CFG_COUNT_NZL);
    }

	   public static boolean isCountryUK() {
	       return MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigTypeBase.S3166_CFG_COUNT_GBR);
	    }

	   public static boolean isOnlyM7OperatorCountry(Context context) {
	       return getDVBSOperatorList(context, -1 ,true).isEmpty();
	   }

	   public static List<String> getDVBSAllOperatorList(Context context) {
	        return getDVBSOperatorList(context, -1 ,false);
	    }

	   public static List<String> getDVBSOperatorList(Context context) {
	        return getDVBSOperatorList(context, MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE), true);
	    }

	   public static List<String> getDVBSOperatorList(Context context, int diseqc) {
        return getDVBSOperatorList(context, diseqc, true);
	   }

	public static List<String> getDVBSOperatorList(Context context,int diseqc, boolean fileterM7) {
		if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS) &&
		        !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT)) {
			return new ArrayList<String>();
		}
		boolean isDiseqc10 = (diseqc == MenuConfigManager.DVBS_ACFG_DISEQC10);
		String country = MtkTvConfig.getInstance().getCountry();
    CountryConfigEntry countryConfigEntry = PartnerSettingsConfig.getCountryConfigMap().get(country);
    List<String> result = new ArrayList<String>();
    boolean isDynamicM7 = DataSeparaterUtil.getInstance().isDynamicM7Support();
    if(countryConfigEntry != null && !TextUtils.isEmpty(countryConfigEntry.dvbs_operators)) {
      String[] strings = countryConfigEntry.dvbs_operators.trim().split(",");
      DVBSOperator dvbsOperator = new DVBSOperator(context);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isDynamicM7= "+isDynamicM7);
      for (String string : strings) {
        if(!TextUtils.isEmpty(string)) {
          try {
            if((isDynamicM7 || (fileterM7 && !isDiseqc10)) && TVContent.getInstance(context).isM7ScanMode(Integer.parseInt(string))){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"diseqc 1.0 filter M7 operator "+string);
                continue;
            }
            result.add(dvbsOperator.getNameByOperator(Integer.parseInt(string)));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } else {
      /*int countryID = CountrysIndex.reflectCountryStrToInt(country);
      result = getDVBSOperatorList(context, countryID);*/
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"CountryConfigEntry null or dvbs_operators empty");
    }
    if(isDynamicM7 && (isDiseqc10 || !fileterM7)){
        result.add(context.getString(R.string.dvbs_operator_name_m7_fast_scan));
    }
    return result;
	}
	
	public boolean isM7ScanModeByName(String name,Context context) {
        return TVContent.getInstance(context).isM7ScanMode(new DVBSOperator(context).getOperatorByName(name));
    }

	public static String[] initScanModesForOperator(Context context,
			CableOperator operator, List<CableOperator> operatorList) {

		String advanceStr = context
				.getString(R.string.menu_arrays_Advance);
		String fullStr = context.getString(R.string.menu_arrays_Full);
		String quickStr = context.getString(R.string.menu_arrays_Quick);

		String[] scanMode = new String[] { advanceStr, quickStr, fullStr };
		int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig
				.getInstance().getCountry());

		com.mediatek.wwtv.tvcenter.util.MtkLog.d(String.format(
				"initScanModesForOperator(),operator:%s,countryID:%d",
				operator.name(), countryID));

		switch (countryID) {
		case CountrysIndex.CTY_15_Sweden_SWE:
			switch (operator) {
			case Comhem:
				scanMode = new String[] { advanceStr };
				break;
			case Canal_Digital:
				scanMode = new String[] { advanceStr, fullStr };
				break;
			case TELE2:
				scanMode = new String[] { quickStr };
				break;
			case OTHER:
				scanMode = new String[] { advanceStr, quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_6_Denmark_DNK:
			switch (operator) {
			case Stofa:
				scanMode = new String[] { advanceStr };
				break;
			case Yousee:
				scanMode = new String[] { advanceStr };
				break;
			case Canal_Digital:
				scanMode = new String[] { advanceStr, fullStr };
				break;
			case GLENTEN:
				scanMode = new String[] { quickStr, fullStr };
				break;
			case OTHER:
				scanMode = new String[] { advanceStr, quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_13_Netherlands_NLD:
			switch (operator) {
			case Ziggo:
			case UPC:
			case OTHER:
				scanMode = new String[] { advanceStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_3_Switzerland_CHE:
		case CountrysIndex.CTY_1_Austria_AUT:
		case CountrysIndex.CTY_21_Poland_POL:
		case CountrysIndex.CTY_20_Ireland_IRL:
			switch (operator) {
			case UPC:
				scanMode = new String[] { advanceStr };
				break;
			case OTHER:
				scanMode = new String[] { advanceStr, quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_19_Hungary_HUN:
			switch (operator) {
            case UPC:
              scanMode = new String[] {
                  advanceStr
              };
              break;
            case RCS_RDS:
              scanMode = new String[] {
                  quickStr
              };
              break;
            default:
              break;
          }
			break;
		case CountrysIndex.CTY_23_Romania_ROU:
			switch (operator) {
			case UPC:
				scanMode = new String[] { advanceStr };
				break;
			case RCS_RDS:
				scanMode = new String[] { quickStr };
				break;
			case OTHER:
				scanMode = new String[] { advanceStr, quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_8_Finland_FIN:
			switch (operator) {
			case Canal_Digital:
				scanMode = new String[] { advanceStr, fullStr };
				break;
			case OTHER:
				scanMode = new String[] { quickStr, fullStr };//DTV02393853
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_14_Norway_NOR:
			switch (operator) {
			case Canal_Digital:
			case OTHER:
				scanMode = new String[] { advanceStr, fullStr};
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_5_Germany_DEU:
			switch (operator) {
			case KDG:
			case Unitymedia:
				scanMode = new String[] {quickStr, advanceStr };
				break;
			case TELECOLUMBUS:
			//case OTHER://DTV02194719
				scanMode = new String[] { quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_9_France_FRA:
			switch (operator) {
			case Numericable:
				scanMode = new String[] { advanceStr };
				break;
			case OTHER:
				scanMode = new String[] { advanceStr, quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_2_Belgium_BEL:
			switch (operator) {
			case TELNET:
			case VOO:
				scanMode = new String[] { advanceStr };
				break;
			case OTHER:
				scanMode = new String[] { advanceStr, quickStr, fullStr };
				break;
			default:
				break;
			}
			break;
		case CountrysIndex.CTY_24_Russia_RUS:
            switch (operator) {
              case AKADO:
              case ONLIME:
              case TVOE_STP:
                scanMode = new String[] {
                    advanceStr
                };
                break;
              case DIVAN_TV:
              case TVOE_EKA:
                scanMode = new String[] {
                    quickStr
                };
                break;
              default:
                break;
            }
            break;
          case CountrysIndex.CTY_16_Bulgaria_BGR:
            switch (operator) {
              case BLIZOO:
              case NET1:
                scanMode = new String[] {
                    advanceStr
                };
                break;
              default:
                break;
            }
            break;
          case CountrysIndex.CTY_27_Slovenia_SVN:
            switch (operator) {
              case TELEING:
              case TELEMACH:
                scanMode = new String[] {
                    quickStr
                };
                break;
              default:
                break;
            }
            break;
          case CountrysIndex.CTY_28_Turkey_TUR:
              switch (operator) {
                case D_SMART:
                  scanMode = new String[] {
                      advanceStr
                  };
                  break;
                default:
                  break;
              }
              break;
          case CountrysIndex.CTY_36_Ukraine_UKR:
            switch (operator) {
              case VOLIA:
                scanMode = new String[] {
                    quickStr
                };
                break;
              default:
                break;
            }
            break;
		default:
			scanMode = new String[] { advanceStr, quickStr, fullStr };
			break;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("ScanMode:" + Arrays.asList(scanMode).toString());
		return scanMode;
	}

	/*static List<String> getDVBSOperatorList(Context context, int countryID) {
		// 1.check country
		// 2.check other SPEC.
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSOperatorList():" + "countryID: " + countryID);
//		dvbs_operator_name_freeview
		String astrahdplus = context
				.getString(R.string.dvbs_operator_astra_hd_plus);
		String skyDeutschland = context
				.getString(R.string.dvbs_operator_sky_deutschland);
		String internationalLcn = context
		        .getString(R.string.dvbs_operator_astra_international_lcn);
		String ors = context
				.getString(R.string.dvbs_operator_ors);
		String austriasat = context
				.getString(R.string.dvbs_operator_austriasat);
		String cannaldigital = context
				.getString(R.string.dvbs_operator_cannal_digital);
		String tvVlaanderen = context
				.getString(R.string.dvbs_operator_tv_vlaanderen);
		String dvbs_operator_tv_vlaanderen_sd = context
				.getString(R.string.dvbs_operator_tv_vlaanderen_sd);
		String telesat = context
				.getString(R.string.dvbs_operator_telesat);
		String dvbs_operator_telesat_luxembourg = context
				.getString(R.string.dvbs_operator_telesat_luxembourg);
		String seznamKanaluProCesko = context
				.getString(R.string.dvbs_operator_seznam_kanalu_pro_cesko);
		String seznamKanaluProSlovensko = context
				.getString(R.string.dvbs_operator_seznam_kanalu_pro_slovensko);
		String cannaldigitaal = context
				.getString(R.string.dvbs_operator_cannaldigitaal);
		String dvbs_operator_cannaldigitaal_sd = context
				.getString(R.string.dvbs_operator_cannaldigitaal_sd);
		String nnakarte = context
				.getString(R.string.dvbs_operator_n_na_karte);
		String dititurkEutelsat = context
				.getString(R.string.dvbs_operator_digiturk_eutelsat);
		String digiturkTurksat = context
				.getString(R.string.dvbs_operator_digiturk_turksat);
		String dvbs_operator_fransat = context
				.getString(R.string.dvbs_operator_fransat);
		String dvbs_operator_cyfraplus = context
				.getString(R.string.dvbs_operator_cyfraplus);
		String dvbsOperatorCyfrowyPolsat = context
				.getString(R.string.dvbs_operator_cyfrowy_polsat);
		String dsmart = context
				.getString(R.string.dvbs_operator_dsmart);
		String ntvPlus = context
				.getString(R.string.dvbs_operator_ntvplus);
		String dvbs_operator_others = context
				.getString(R.string.dvbs_operator_others);
		String dvbs_satellite_status = context
				.getString(R.string.dvbs_satellite_status);

		String ncPlus = context
				.getString(R.string.dvbs_operator_name_nc_plus);
		String smartHdPlus = context
				.getString(R.string.dvbs_operator_name_smart_hd_plus);
		String tkgs = context
                .getString(R.string.dvbs_operator_name_tkgs);
		String tivusat = context
				.getString(R.string.dvbs_operator_name_tivusat);

		String dvbs_operator_name_austriaSat_Mag = context
				.getString(R.string.dvbs_operator_name_turksat_hello);
		String diveo = context
				.getString(R.string.dvbs_operator_name_diveo);
		String tivibu = context
                .getString(R.string.dvbs_operator_name_tivibu);
		String digi = context
				.getString(R.string.dvbs_operator_name_digi);
		String whiteLabelPlatformLcn = context
		    .getString(R.string.dvbs_operator_name_white_label_platform_lcn);
		String simpliTv = context
		    .getString(R.string.dvbs_operator_name_simpliTV);
		String digitvcze = context
		    .getString(R.string.dvbs_operator_name_digi_tv_cze);
		String dititvsvk = context
		    .getString(R.string.dvbs_operator_name_digi_tv_svk);
		String tricolor = context
		    .getString(R.string.dvbs_operator_name_tricolor);
		String telekarta = context
		    .getString(R.string.dvbs_operator_name_telekarta);
		String dvbs_operator_name_freeview = context
                .getString(R.string.dvbs_operator_name_freeview);



		List<String> list = new ArrayList<String>();
		// list = new LinkedList<String>(
		// Arrays.asList(new String[] { dvbs_operator_others }));
		list = new LinkedList<String>();

		switch (countryID) {
		case CountrysIndex.CTY_1_Austria_AUT:
			list.clear();
			list.add(ors);
			list.add(skyDeutschland);
			list.add(austriasat);
			list.add(simpliTv);
			list.add(internationalLcn);
			list.add(whiteLabelPlatformLcn);
			break;
		case CountrysIndex.CTY_2_Belgium_BEL:
			list.clear();
			list.add(tvVlaanderen);
			list.add(telesat);
			break;
		case CountrysIndex.CTY_3_Switzerland_CHE:
            list.add(internationalLcn);
			break;
		case CountrysIndex.CTY_4_Czech_Republic_CZE:
			list.clear();
			list.add(seznamKanaluProCesko);
			list.add(seznamKanaluProSlovensko);
	        list.add(internationalLcn);
	        list.add(digitvcze);
	        list.add(dititvsvk);
			break;
		case CountrysIndex.CTY_5_Germany_DEU:
			list.clear();
			list.add(astrahdplus);
			list.add(skyDeutschland);
	        list.add(internationalLcn);
	        list.add(diveo);
	        list.add(whiteLabelPlatformLcn);
			break;
		case CountrysIndex.CTY_6_Denmark_DNK:
			list.clear();
			list.add(cannaldigital);
			break;
		case CountrysIndex.CTY_7_Spain_ESP:
			break;
		case CountrysIndex.CTY_8_Finland_FIN:
			list.clear();
			list.add(cannaldigital);
			break;
		case CountrysIndex.CTY_9_France_FRA:
			list.clear();
			//list.add(dvbs_operator_fransat);//0218spec remove
			break;
		case CountrysIndex.CTY_10_United_Kingdom_GBR:
			break;
		case CountrysIndex.CTY_11_Italy_ITA:
			list.clear();
			list.add(tivusat);
			break;
		case CountrysIndex.CTY_12_Luxembourg_LUX:
			list.clear();
			break;
		case CountrysIndex.CTY_13_Netherlands_NLD:
			list.clear();
			list.add(cannaldigitaal);
			break;
		case CountrysIndex.CTY_14_Norway_NOR:
			list.clear();
			list.add(cannaldigital);
			break;
		case CountrysIndex.CTY_15_Sweden_SWE:
			list.clear();
			list.add(cannaldigital);
			break;
		case CountrysIndex.CTY_16_Bulgaria_BGR:
			break;
		case CountrysIndex.CTY_17_Croatia_HRV:
			break;
		case CountrysIndex.CTY_18_Greece_GRC:
			break;
		case CountrysIndex.CTY_19_Hungary_HUN:
			list.clear();
	        list.add(internationalLcn);
	        list.add(digi);
			break;
		case CountrysIndex.CTY_20_Ireland_IRL:
			break;
		case CountrysIndex.CTY_21_Poland_POL:
			list.clear();
			list.add(ncPlus);
			list.add(nnakarte);
			list.add(smartHdPlus);
			list.add(dvbsOperatorCyfrowyPolsat);
			break;
		case CountrysIndex.CTY_22_Portugal_PRT:
			break;
		case CountrysIndex.CTY_23_Romania_ROU:
			list.clear();
			list.add(digi);
			break;
		case CountrysIndex.CTY_24_Russia_RUS:
			list.clear();
			list.add(ntvPlus);
			list.add(tricolor);
			list.add(telekarta);
			break;
		case CountrysIndex.CTY_25_Serbia_SRB:
			break;
		case CountrysIndex.CTY_26_Slovakia_SVK:
			list.clear();
			list.add(seznamKanaluProSlovensko);
			list.add(seznamKanaluProCesko);
	        list.add(internationalLcn);
	        list.add(dititvsvk);
	        list.add(digitvcze);
			break;
		case CountrysIndex.CTY_27_Slovenia_SVN:
	        list.add(internationalLcn);
			break;
		case CountrysIndex.CTY_28_Turkey_TUR:
			list.clear();
			list.add(dititurkEutelsat);
			list.add(digiturkTurksat);
			list.add(dsmart);
			list.add(tkgs);
			list.add(tivibu);
			break;
		case CountrysIndex.CTY_29_Estonia_EST:
			break;
		default:
			break;
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("info.getCountryID():" + countryID);

		return list;

	}*/

	public static String getOperator(Context context){
			int i = MtkTvConfig.getInstance().getConfigValue(
	              MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER);
	      String[] operats = context.getResources().getStringArray(
	              R.array.dvbc_operators_eu);
	      return operats[i];
	  }

	public static boolean enableScanStoreTypeForCustomer(){ //DTV03173278
		int operator = getDVBSCurrentOP();
		return operator == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ASTRA_HD_PLUS ||
			operator == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SKY_DEUTSCHLAND ||
			operator == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ASTRA_INTERNATIONAL_LCN;
	}

	public static void setOperator(Context context, String operator) {
	    mLastDvbcOperator = getCurrentOperator();
	    if(!operator.equals(getOperatorStr(context, mLastDvbcOperator))){
	        SaveValue.saveWorldValue(context,  "dvbc_network_freq", -1000, true);
	        SaveValue.saveWorldValue(context,  "dvbc_network_id", -1000, true);
	    }
		String[] operats = context.getResources().getStringArray(
				R.array.dvbc_operators_eu);
		for (int i = 0; i < operats.length; i++) {
			if (operator.equalsIgnoreCase(operats[i])) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("_EU", "setOperator()," + operator);
				MtkTvConfig.getInstance().setConfigValue(
						MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER, i);
				/*if(TVContent.getInstance(context).isBelgiumCountry()){
				    if(operator.equals(context.getResources().getString(R.string.dvbc_operator_voo))){
				        addBlockedRatingByAge(context,12);
				    }else if(operator.equals(context.getResources().getString(R.string.dvbc_operator_telent_eu))){
				        addBlockedRatingByAge(context,17);
				    }
				}*/
				break;
			}
		}
		if(isZiggoUPCOp()){ //DTV03022861
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"Ziggo UPC channel update msg set Off");
			MtkTvConfig.getInstance().setConfigValue(
					MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG, 0);
		}
	}

	/*public static void addBlockedRatingByAge(Context context, int age) {
        TvInputManager tvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        tvInputManager.setParentalControlsEnabled(true);
        for (TvContentRating tvContentRating : tvInputManager.getBlockedRatings()) {
          tvInputManager.removeBlockedRating(tvContentRating);
        }
        if(age > 0) {
          for (int i = age; i <= 18; i++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addBlockedRatingByAge()," + i);
            tvInputManager.addBlockedRating(TvContentRating.createRating("com.android.tv", "DVB", "DVB_"+i));
          }
        }
      }*/

	public void setOperator(CableOperator operator) {
		int operatorIndex = 0;
		switch (operator) {
		case UPC:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_UPC;
			break;
		case TELNET:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_TELENET;
			break;
		case Stofa:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_STOFA;
			break;
		case Yousee:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_YOUSEE;
			break;
		case Numericable:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_NUMERICABLE;
			break;
		case Ziggo:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_ZIGGO;
			break;
		case Comhem:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_COMHEM;
			break;
		case OTHER:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_OTHERS;
			break;
		case NULL:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_OTHERS;
			break;
		case Unitymedia:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_UNITYMEDIA;
			break;
		case TELE2:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_TELE2;
			break;
		case VOLIA:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_VOLIA;
			break;
		case TELEMACH:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_TELEMACH;
			break;
		case ONLIME:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_ONLIME;
			break;
		case AKADO:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_AKADO;
			break;
		case TKT:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_TKT;
			break;
		case DIVAN_TV:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_DIVAN_TV;
			break;
		case NET1:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_NET1;
			break;
		case KDG:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_KDG;
			break;
		case KBW:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_KBW;
			break;
		case GLENTEN:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_GLENTEN;
			break;
		case BLIZOO:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_BLIZOO;
			break;
		case TELECOLUMBUS:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_TELECOLUMBUS;
			break;
		case RCS_RDS:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_RCS_RDS;
			break;
		case Canal_Digital:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_CANAL_DIGITAL;
			break;
		default:
			operatorIndex = CableOperator.DVBC_OPERATOR_NAME_OTHERS;
			break;
		}
		MtkTvConfig.getInstance().setConfigValue(
				MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER, operatorIndex);
	}

	public static String getCurrentOperatorStr(Context context) {
		CableOperator operator = getCurrentOperator();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getCurrentOperatorStr()," + operator.name());
		return getOperatorStr(context, operator);
	}

	public static boolean isRCSRDSOp() {
		return getCurrentOperator().equals(CableOperator.RCS_RDS);
	}

	public static boolean isTELNETOp() {
		return getCurrentOperator().equals(CableOperator.TELNET);
	}

	public static boolean isOnlimeOp() {
		return MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER) == ScanDvbcOperator.DVBC_OPERATOR_NAME_ONLIME.idOf();
	}

	public static boolean isRostelecomSpbOp() {
		return MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER) == ScanDvbcOperator.DVBC_OPERATOR_NAME_TVOE_STP.idOf();
	}

	public static boolean isTvoeEkaOp() {
		return MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER) == ScanDvbcOperator.DVBC_OPERATOR_NAME_TVOE_EKA.idOf();
	}

	public static boolean isVooOp() {
		return getCurrentOperator().equals(CableOperator.VOO);
	}
	
	public static boolean isZiggoUPCOp() {
	    CableOperator operator = getCurrentOperator();
	    return operator.equals(CableOperator.Ziggo) || operator.equals(CableOperator.UPC);
	}

	public static boolean isAkadoOp() {
	    return getCurrentOperator().equals(CableOperator.AKADO);
        }

	public static boolean isNLDOtherOp() {
	    CableOperator operator = getCurrentOperator();
	    return operator.equals(CableOperator.OTHER) &&
                MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NLD);
	}

  public static boolean isZiggoOp() {
    return getCurrentOperator().equals(CableOperator.Ziggo);
  }

  public static boolean isUPCOp() {
    return getCurrentOperator().equals(CableOperator.UPC);
  }

  public static boolean isZiggo() {
    return (isZiggoOp() || isUPCOp()) && isCurrentCable();
  }

	public static CableOperator getCurrentOperator() {
		int operatorIndex = MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER);
		CableOperator operator = CableOperator.OTHER;
		switch (operatorIndex) {
		case CableOperator.DVBC_OPERATOR_NAME_OTHERS:
			operator = CableOperator.OTHER;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_UPC:
			operator = CableOperator.UPC;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_COMHEM:
			operator = CableOperator.Comhem;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_CANAL_DIGITAL:
			operator = CableOperator.Canal_Digital;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TELE2:
			operator = CableOperator.TELE2;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_STOFA:
			operator = CableOperator.Stofa;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_YOUSEE:
			operator = CableOperator.Yousee;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_ZIGGO:
			operator = CableOperator.Ziggo;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_UNITYMEDIA:
			operator = CableOperator.Unitymedia;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_NUMERICABLE:
			operator = CableOperator.Numericable;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_VOLIA:
			operator = CableOperator.VOLIA;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TELEMACH:
			operator = CableOperator.TELEMACH;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_ONLIME:
			operator = CableOperator.ONLIME;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_AKADO:
			operator = CableOperator.AKADO;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TKT:
			operator = CableOperator.TKT;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_DIVAN_TV:
			operator = CableOperator.DIVAN_TV;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_NET1:
			operator = CableOperator.NET1;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_KDG:
			operator = CableOperator.KDG;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_KBW:
			operator = CableOperator.KBW;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_BLIZOO:
			operator = CableOperator.BLIZOO;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TELENET:
			operator = CableOperator.TELNET;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_GLENTEN:
			operator = CableOperator.GLENTEN;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TELECOLUMBUS:
			operator = CableOperator.TELECOLUMBUS;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_RCS_RDS:
			operator = CableOperator.RCS_RDS;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_VOO:
			operator = CableOperator.VOO;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_KDG_HD:
			operator = CableOperator.KDG_HD;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_KRS:
			operator = CableOperator.KRS;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TELEING:
			operator = CableOperator.TELEING;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_MTS:
			operator = CableOperator.MTS;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TVOE_STP:
			operator = CableOperator.TVOE_STP;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TVOE_EKA:
			operator = CableOperator.TVOE_EKA;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_TELEKOM:
			operator = CableOperator.TELEKOM;
			break;
		case CableOperator.DVBC_OPERATOR_NAME_DSMART:
			operator = CableOperator.D_SMART;
			break;
		default:
			operator = CableOperator.OTHER;
			break;
		}
		return operator;
	}

	public static String getOperatorStr(Context context, CableOperator operator) {

		String upc = context.getString(R.string.dvbc_operator_upc);
		String telent = context.getString(R.string.dvbc_operator_telent_eu);
		String unitymedia = context
				.getString(R.string.dvbc_operator_unitymedia);
		String stofa = context.getString(R.string.dvbc_operator_stofa);
		String yousee = context.getString(R.string.dvbc_operator_yousee);
		String canaldigital = context
				.getString(R.string.dvbc_operator_canal_digital);
		String numericable = context
				.getString(R.string.dvbc_operator_numericable);
		String ziggo = context.getString(R.string.dvbc_operator_ziggo);
		String comhem = context.getString(R.string.dvbc_operator_comhem);
		String others = context.getString(R.string.menu_c_channelscan_oth);
		String glenten = context.getString(R.string.dvbc_operator_glenten);
		String rcsrds = context.getString(R.string.dvbc_operator_digi);
		String telecolumbus = context
				.getString(R.string.dvbc_operator_tele_columbus);

		String tele2 = context.getString(R.string.dvbc_operator_tele2);
		String volia = context.getString(R.string.dvbc_operator_volia);
		String telemach = context.getString(R.string.dvbc_operator_telemach);
		String onlime = context.getString(R.string.dvbc_operator_onlime);
		String akado = context.getString(R.string.dvbc_operator_akado);
		String tkt = context.getString(R.string.dvbc_operator_tkt);
		String divantv = context.getString(R.string.dvbc_operator_divan_tv);
		String net1 = context.getString(R.string.dvbc_operator_net1);
		String kdg = context.getString(R.string.dvbc_operator_kdg);
		String kbw = context.getString(R.string.dvbc_operator_kbw);
		String blizoo = context.getString(R.string.dvbc_operator_blizoo);
		String voo = context.getString(R.string.dvbc_operator_voo);

		String kdghd = context.getString(R.string.dvbc_operator_kdg_hd);
		String teleing = context.getString(R.string.dvbc_operator_teleing);
		String krs = context.getString(R.string.dvbc_operator_krs);
		String mts = context.getString(R.string.dvbc_operator_mts);
		String tvoestp = context.getString(R.string.dvbc_operator_tvoe_stp);
		String tvoeeka = context.getString(R.string.dvbc_operator_tvoe_eka);
		String telekom = context.getString(R.string.dvbc_operator_telekom);
		String dsmart = context.getString(R.string.dvbc_operator_dsmart);

		switch (operator) {
		case UPC:
			return upc;
		case TELNET:
			return telent;
		case Stofa:
			return stofa;
		case Yousee:
			return yousee;
		case Numericable:
			return numericable;
		case Ziggo:
			return ziggo;
		case Comhem:
			return comhem;
		case OTHER:
			return others;
		case NULL:
			return "";
		case Unitymedia:
			return unitymedia;
		case Canal_Digital:
			return canaldigital;
		case GLENTEN:
			return glenten;
		case RCS_RDS:
			return rcsrds;
		case TELECOLUMBUS:
			return telecolumbus;
		case TELE2:
			return tele2;
		case VOLIA:
			return volia;
		case TELEMACH:
			return telemach;
		case ONLIME:
			return onlime;
		case AKADO:
			return akado;
		case TKT:
			return tkt;
		case DIVAN_TV:
			return divantv;
		case NET1:
			return net1;
		case KDG:
			return kdg;
		case KBW:
			return kbw;
		case BLIZOO:
			return blizoo;
		case VOO:
            return voo;
		case KDG_HD:
            return kdghd;
		case TELEING:
            return teleing;
		case KRS:
            return krs;
		case MTS:
            return mts;
		case TVOE_EKA:
            return tvoeeka;
		case TVOE_STP:
            return tvoestp;
		case TELEKOM:
            return telekom;
		case D_SMART:
		    return dsmart;
		default:
			return others;
		}

	}

	public static List<CableOperator> convertStrOperator(Context context,
			List<String> operatorStrList) {
		List<CableOperator> cableOperators = new ArrayList<CableOperator>();

		String upc = context.getString(R.string.dvbc_operator_upc);
		String virginMedia = context.getString(R.string.dvbc_operator_virgin_media);
		String vodafone = context.getString(R.string.dvbc_operator_vodafone);
		String akadoRus = context.getString(R.string.dvbc_operator_akado_russia);
		String telenor = context.getString(R.string.dvbc_operator_telenor);
		String magenta = context.getString(R.string.dvbc_operator_magenta);
		String telent = context.getString(R.string.dvbc_operator_telent_eu);
		String unitymedia = context
				.getString(R.string.dvbc_operator_unitymedia);
		String stofa = context.getString(R.string.dvbc_operator_stofa);
		String yousee = context.getString(R.string.dvbc_operator_yousee);
		String canaldigital = context
				.getString(R.string.dvbc_operator_canal_digital);
		String numericable = context
				.getString(R.string.dvbc_operator_numericable);
		String ziggo = context.getString(R.string.dvbc_operator_ziggo);
		String comhem = context.getString(R.string.dvbc_operator_comhem);
		String others = context.getString(R.string.menu_c_channelscan_oth);
		String glenten = context.getString(R.string.dvbc_operator_glenten);
		String rcsrds = context.getString(R.string.dvbc_operator_digi);
		String telecolumbus = context
				.getString(R.string.dvbc_operator_tele_columbus);

		String tele2 = context.getString(R.string.dvbc_operator_tele2);
		String volia = context.getString(R.string.dvbc_operator_volia);
		String telemach = context.getString(R.string.dvbc_operator_telemach);
		String onlime = context.getString(R.string.dvbc_operator_onlime);
		String akado = context.getString(R.string.dvbc_operator_akado);
		String tkt = context.getString(R.string.dvbc_operator_tkt);
		String divantv = context.getString(R.string.dvbc_operator_divan_tv);
		String net1 = context.getString(R.string.dvbc_operator_net1);
		String kdg = context.getString(R.string.dvbc_operator_kdg);
		String kbw = context.getString(R.string.dvbc_operator_kbw);
		String blizoo = context.getString(R.string.dvbc_operator_blizoo);
		String voo = context.getString(R.string.dvbc_operator_voo);

		String kdghd = context.getString(R.string.dvbc_operator_kdg_hd);
		String teleing = context.getString(R.string.dvbc_operator_teleing);
		String krs = context.getString(R.string.dvbc_operator_krs);
		String mts = context.getString(R.string.dvbc_operator_mts);
		String tvoestp = context.getString(R.string.dvbc_operator_tvoe_stp);
		String tvoeeka = context.getString(R.string.dvbc_operator_tvoe_eka);
		String telekom = context.getString(R.string.dvbc_operator_telekom);
		String dsmart = context.getString(R.string.dvbc_operator_dsmart);

		String operatorName;

		for (String operator : operatorStrList) {

			operatorName = operator;
			if (operatorName.equalsIgnoreCase(upc) || operatorName.equalsIgnoreCase(virginMedia) ||
			        operatorName.equalsIgnoreCase(vodafone) || operatorName.equalsIgnoreCase(magenta)) {
				cableOperators.add(CableOperator.UPC);
			} else if (operatorName.equalsIgnoreCase(telent)) {
				cableOperators.add(CableOperator.TELNET);
			} else if (operatorName.equalsIgnoreCase(unitymedia)) {
				cableOperators.add(CableOperator.Unitymedia);
			} else if (operatorName.equalsIgnoreCase(stofa)) {
				cableOperators.add(CableOperator.Stofa);
			} else if (operatorName.equalsIgnoreCase(yousee)) {
				cableOperators.add(CableOperator.Yousee);
			} else if (operatorName.equalsIgnoreCase(canaldigital)|| operatorName.equalsIgnoreCase(telenor)) {
				cableOperators.add(CableOperator.Canal_Digital);
			} else if (operatorName.equalsIgnoreCase(numericable)) {
				cableOperators.add(CableOperator.Numericable);
			} else if (operatorName.equalsIgnoreCase(ziggo)) {
				cableOperators.add(CableOperator.Ziggo);
			} else if (operatorName.equalsIgnoreCase(comhem)) {
				cableOperators.add(CableOperator.Comhem);
			} else if (operatorName.equalsIgnoreCase(others)) {
				cableOperators.add(CableOperator.OTHER);
			} else if (operatorName.equalsIgnoreCase(glenten)) {
				cableOperators.add(CableOperator.GLENTEN);
			} else if (operatorName.equalsIgnoreCase(rcsrds)) {
				cableOperators.add(CableOperator.RCS_RDS);
			} else if (operatorName.equalsIgnoreCase(telecolumbus)) {
				cableOperators.add(CableOperator.TELECOLUMBUS);
			} else if (operatorName.equalsIgnoreCase(tele2)) {
				cableOperators.add(CableOperator.TELE2);
			} else if (operatorName.equalsIgnoreCase(volia)) {
				cableOperators.add(CableOperator.VOLIA);
			} else if (operatorName.equalsIgnoreCase(telemach)) {
				cableOperators.add(CableOperator.TELEMACH);
			} else if (operatorName.equalsIgnoreCase(onlime)) {
				cableOperators.add(CableOperator.ONLIME);
			} else if (operatorName.equalsIgnoreCase(akado) || operatorName.equalsIgnoreCase((akadoRus))) {
				cableOperators.add(CableOperator.AKADO);
			} else if (operatorName.equalsIgnoreCase(tkt)) {
				cableOperators.add(CableOperator.TKT);
			} else if (operatorName.equalsIgnoreCase(divantv)) {
				cableOperators.add(CableOperator.DIVAN_TV);
			} else if (operatorName.equalsIgnoreCase(net1)) {
				cableOperators.add(CableOperator.NET1);
			} else if (operatorName.equalsIgnoreCase(kdg)) {
				cableOperators.add(CableOperator.KDG);
			} else if (operatorName.equalsIgnoreCase(kbw)) {
				cableOperators.add(CableOperator.KBW);
			} else if (operatorName.equalsIgnoreCase(blizoo)) {
				cableOperators.add(CableOperator.BLIZOO);
			} else if(operatorName.equalsIgnoreCase(voo)) {
                cableOperators.add(CableOperator.VOO);
            } else if(operatorName.equalsIgnoreCase(kdghd)) {
                cableOperators.add(CableOperator.KDG_HD);
            } else if(operatorName.equalsIgnoreCase(krs)) {
                cableOperators.add(CableOperator.KRS);
            } else if(operatorName.equalsIgnoreCase(teleing)) {
                cableOperators.add(CableOperator.TELEING);
            } else if(operatorName.equalsIgnoreCase(mts)) {
                cableOperators.add(CableOperator.MTS);
            } else if(operatorName.equalsIgnoreCase(tvoestp)) {
                cableOperators.add(CableOperator.TVOE_STP);
            } else if(operatorName.equalsIgnoreCase(tvoeeka)) {
                cableOperators.add(CableOperator.TVOE_EKA);
            } else if(operatorName.equalsIgnoreCase(telekom)) {
                cableOperators.add(CableOperator.TELEKOM);
            }else if(operatorName.equalsIgnoreCase(dsmart)){
                cableOperators.add(CableOperator.D_SMART);
            }
		}

		return cableOperators;
	}


	/**
	 * 1.Only show cable operation when region=EU.
	 * <p>
	 * 2.different country has different operation list.
	 *
	 * @param manager
	 * @param countryID
	 * @param selectedCountryPosition
	 * @return
	 */
	static List<String> getCableOperationList(Context context, int countryID) {
		// 1.check country
		// 2.check other SPEC.
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getCableOperationList():" + "countryID: " + countryID);

		String upc = context.getString(R.string.dvbc_operator_upc);
		String virginMedia = context.getString(R.string.dvbc_operator_virgin_media);
		String telent = context.getString(R.string.dvbc_operator_telent_eu);
		String unitymedia = context
				.getString(R.string.dvbc_operator_unitymedia);
		String stofa = context.getString(R.string.dvbc_operator_stofa);
		String yousee = context.getString(R.string.dvbc_operator_yousee);
		String canaldigital = context
				.getString(R.string.dvbc_operator_canal_digital);
		String numericable = context
				.getString(R.string.dvbc_operator_numericable);
		String ziggo = context.getString(R.string.dvbc_operator_ziggo);
		String comhem = context.getString(R.string.dvbc_operator_comhem);
		String others = context.getString(R.string.menu_c_channelscan_oth);
		String glenten = context.getString(R.string.dvbc_operator_glenten);
		String rcsrds = context.getString(R.string.dvbc_operator_digi);
		String telecolumbus = context.getString(R.string.dvbc_operator_tele_columbus);
		String voo = context.getString(R.string.dvbc_operator_voo);
		String blizoo = context.getString(R.string.dvbc_operator_blizoo);
        String net1 = context.getString(R.string.dvbc_operator_net1);
        String kdg = context.getString(R.string.dvbc_operator_kdg);
        String akado = context.getString(R.string.dvbc_operator_akado);
        String divantv = context.getString(R.string.dvbc_operator_divan_tv);
        String onlime = context.getString(R.string.dvbc_operator_onlime);
        String teleing = context.getString(R.string.dvbc_operator_teleing);
        String telemach = context.getString(R.string.dvbc_operator_telemach);
        String tele2 = context.getString(R.string.dvbc_operator_tele2);
        String volia = context.getString(R.string.dvbc_operator_volia);
        String tvoestp = context.getString(R.string.dvbc_operator_tvoe_stp);
        String dsmart = context.getString(R.string.dvbc_operator_dsmart);
		List<String> list = new ArrayList<String>();
		list = new LinkedList<String>(
				Arrays.asList(new String[] { upc, others }));
		if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA)) {
			switch (countryID) {
			case CountrysIndex.CTY_30_Australia_AUS:
			case CountrysIndex.CTY_31_New_Zealand_NZL:
			case CountrysIndex.CTY_32_Indonesia_IDN:
				list.clear();
				break;
			default:
				break;
			}
		} else {
			switch (countryID) {
			case CountrysIndex.CTY_1_Austria_AUT:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { upc,
						others }));
				break;
			case CountrysIndex.CTY_2_Belgium_BEL:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { telent,voo,
						others }));
				break;
			case CountrysIndex.CTY_3_Switzerland_CHE:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { upc,
						others }));
				break;
			case CountrysIndex.CTY_4_Czech_Republic_CZE:
				list.clear();
				break;
			case CountrysIndex.CTY_5_Germany_DEU:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] {
						unitymedia, telecolumbus, kdg, others }));
				break;
			case CountrysIndex.CTY_6_Denmark_DNK:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { stofa,
						yousee, canaldigital, glenten, others }));
				break;
			case CountrysIndex.CTY_7_Spain_ESP:
				list.clear();
				break;
			case CountrysIndex.CTY_8_Finland_FIN:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] {
						canaldigital, others }));
				break;
			case CountrysIndex.CTY_9_France_FRA:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] {
						numericable, others }));
				break;
			case CountrysIndex.CTY_10_United_Kingdom_GBR:
				list.clear();
				break;
			case CountrysIndex.CTY_11_Italy_ITA:
				list.clear();
				break;
			case CountrysIndex.CTY_12_Luxembourg_LUX:
				list.clear();
				break;
			case CountrysIndex.CTY_13_Netherlands_NLD:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { ziggo,
						others }));
				break;
			case CountrysIndex.CTY_14_Norway_NOR:
				list.clear();
				list = new LinkedList<String>(
				Arrays.asList(new String[] { canaldigital }));
				break;
			case CountrysIndex.CTY_15_Sweden_SWE:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { comhem,
						canaldigital, tele2, others }));
				break;
			case CountrysIndex.CTY_16_Bulgaria_BGR:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { blizoo,
						net1, others }));
				break;
			case CountrysIndex.CTY_17_Croatia_HRV:
				list.clear();
				break;
			case CountrysIndex.CTY_18_Greece_GRC:
				list.clear();
				break;
			case CountrysIndex.CTY_19_Hungary_HUN:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { upc,
						rcsrds,
						others }));
				break;
			case CountrysIndex.CTY_20_Ireland_IRL:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { virginMedia,
						others }));
				break;
			case CountrysIndex.CTY_21_Poland_POL:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { upc,
						others }));
				break;
			case CountrysIndex.CTY_22_Portugal_PRT:
				list.clear();
				break;
			case CountrysIndex.CTY_23_Romania_ROU:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { upc,
						rcsrds, others }));
				break;
			case CountrysIndex.CTY_24_Russia_RUS:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] { akado,
						divantv, onlime, tvoestp, others }));
				break;
			case CountrysIndex.CTY_25_Serbia_SRB:
				list.clear();
				break;
			case CountrysIndex.CTY_26_Slovakia_SVK:
				list.clear();
				break;
			case CountrysIndex.CTY_27_Slovenia_SVN:
				list.clear();
				list = new LinkedList<String>(Arrays.asList(new String[] {
                        teleing, telemach, others
                    }));
				break;
			case CountrysIndex.CTY_28_Turkey_TUR:
                            list.clear();
			    list = new LinkedList<String>(Arrays.asList(new String[] { dsmart }));
				break;
			case CountrysIndex.CTY_29_Estonia_EST:
				list.clear();
				break;
			case CountrysIndex.CTY_36_Ukraine_UKR:
                list.clear();
                list = new LinkedList<String>(Arrays.asList(new String[] {
                    volia, others
                }));
                break;
			default:
				break;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("info.getCountryID():" + countryID);

		return list;
	}

	public static List<String> getScanMode(Context context) {

		List<String> list = new ArrayList<String>();
		String[] scanMode = initScanModesForOperator(context,
				getCurrentOperator(), null);

		list = Arrays.asList(scanMode);
		return list;
	}

	public static List<String> getDVBSScanMode(Context context) {
		List<String> list = new ArrayList<String>();

		String networkScanMode = context.getResources().getString(
				R.string.dvbs_scan_mode_network);
		String fullScanMode = context.getResources().getString(
				R.string.dvbs_scan_mode_full);

		int op = MtkTvConfig.getInstance().getConfigValue(SAT_BRDCSTER);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSScanMode(),op:" + op);

		switch (op) {
		case MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_OTHERS: // operator==other
		    list.add(networkScanMode);
			list.add(fullScanMode);
			break;
		default:
			list.add(networkScanMode);
			break;
		}
		return list;
	}


	/**
	 * ??? ???
	 *
	 * @param manager
	 * @return
	 */
	public static List<String> getDVBSConfigInfoChannels(Context context) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();
		if (DVBS_DEV_ING) {
			list = Arrays.asList(new String[] { "All", "Free" }); // debug
		} else {
			list = Arrays.asList(context.getResources().getStringArray(
					R.array.dvbs_channel_arrays));
		}
		return list;
	}

        public static List<String> getDVBSConfigInfoChannelStoreTypes(Context context) {
          // ConfigInfo info = manager.getConfigInfo();
          return Arrays.asList(context.getResources().getStringArray(
                R.array.dvbs_channel_story_type_arrays));
        }

	/**
	 * Get all satellites,filter the data.
	 *
	 * @param manager
	 * @param antenaType
	 * @param tunerType
	 * @return
	 */
	public static List<String> getDVBSFrequencyList(Context context,
			int antenaType, int tunerType) {
		// ConfigInfo info = manager.getConfigInfo();
		final int universal = 0;
		//final int SINGLECABLE = 1;

		List<String> list = new ArrayList<String>();
		if (antenaType == universal) {
			return list;
		}

		// get all satellite data.
		// traverse the data,find tunerType,get all frequency group.
		if (DVBS_DEV_ING) {
			switch (tunerType) {
			case 0:
				list = Arrays.asList(new String[] { "AP_Debug_1210", "1310",
						"1410" });
				break;
			case 1:
				list = Arrays.asList(new String[] { "AP_Debug_2210", "2310",
						"2410", "2510" });
				break;
			default:
				list = Arrays.asList(new String[] { "AP_Debug_3210", "3310",
						"3410", "3510", "3610" });
				break;
			}
		} else {
			return getSingleCableFreqsList(context, tunerType);
		}

		return list;
	}

	/**
	 * Get all satellites,filter the data.
	 *
	 * @param manager
	 * @param antenaType
	 * @return
	 */
	public static List<String> getDVBSTunerList(Context context, int antenaType) {
		final int universal = 0;
		final int singleCable = 1;

		List<String> list = new ArrayList<String>();

		switch (antenaType) {
		case universal:
			list.clear();
			break;
		case singleCable:
			list = getSingleCableUserBandList(context);
			break;
		default:
			list = Arrays.asList(new String[] {});
			break;
		}

		return list;
	}

	/**
	 * Get all satellites,filter the data.
	 *
	 * @param manager
	 * @param antenaType
	 * @return
	 */
	private static List<String> getSingleCableUserBandList(Context context) {

		List<String> list = new ArrayList<String>();
		//
		//List<SatelliteInfo> satList = getDVBSsatellites(context);
		if (DVBS_DEV_ING) {
			list = Arrays.asList(new String[] { "AP_Debug_User band 1-1",
					"AP_Debug_User band 1-2" });
		} else {
			list = Arrays.asList(context.getResources().getStringArray(
					R.array.dvbs_user_band_arrays));

		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getSingleCableUserBandList()," + list.toString());
		return list;
	}

	public static List<String> getSingleCableFreqsList(Context context,
			int index) {
		List<String> list = new ArrayList<String>();
		String userDefine = context
				.getString(R.string.dvbs_band_freq_user_define);
		if (DVBS_DEV_ING) {
			list = Arrays.asList("AP_Debug_SingleCableFreqs 1-1",
					"AP_Debug_SingleCableFreqs 1-2");
		} else {
			switch (index) {
			case -1: // default value
			case 0:
				list = Arrays.asList("1210", "1284", "1400",
						userDefine);
				break;
			case 1:
                list =  Arrays.asList("1420", "1400", "1516",
						userDefine);
                break;
            case 2:
                list =  Arrays.asList("1680", "1516", "1632",
						userDefine);
                break;
            case 3:
                list =  Arrays.asList("2040", "1632", "1748",
						userDefine);
                break;
            case 4:
                list =  Arrays.asList("985", "1748", userDefine);
                break;
            case 5:
                list =  Arrays.asList("1050", "1864", userDefine);
                break;
            case 6:
                list =  Arrays.asList("1115", "1980", userDefine);
                break;
            case 7:
                list =  Arrays.asList("1275", "2096", userDefine);
                break;
            case 8:
                list =  Arrays.asList("1340", userDefine);
                break;
            case 9:
                list =  Arrays.asList("1485", userDefine);
                break;
            case 10:
                list =  Arrays.asList("1550", userDefine);
                break;
            case 11:
                list =  Arrays.asList("1615", userDefine);
                break;
            case 12:
                list =  Arrays.asList("1745", userDefine);
                break;
            case 13:
                list =  Arrays.asList("1810", userDefine);
                break;
            case 14:
                list =  Arrays.asList("1875", userDefine);
                break;
            case 15:
                list =  Arrays.asList("1940", userDefine);
                break;
			default:
			    list = Arrays.asList(userDefine);
				break;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getSingleCableFreqsList()," + list.toString());
		return list;
	}

	/**
	 * always return Universal/SingleCable
	 *
	 * @param manager
	 * @return
	 */
	public static List<String> getDVBSAntennaTypeList(Context context) {
		return Arrays.asList(context.getResources()
				.getStringArray(R.array.dvbs_antenna_type_arrays));
	}

	static List<SatelliteInfo> filterSatsbyAntennaType(Context context, int antennaType, int svlID) {
		List<SatelliteInfo> satList = getDVBSALLsatellites(svlID);
		boolean isCamScanUI = (svlID == 7);
		List<SatelliteInfo> list = new ArrayList<SatelliteInfo>();
		List<String> namesList = new ArrayList<String>();
		//final int universal = 0;
		final int singlecable = 1;
		//final int jesscable = 2;
		String positionArray[] = context.getResources().getStringArray(R.array.dvbs_position_arrays);
		if(antennaType==MenuConfigManager.DVBS_ACFG_UNICABLE2){
            positionArray = context.getResources().getStringArray(R.array.dvbs_jess_cable_position_arrays);
        }
		String []diseqcInputArray = getDVBSDiseqcInput(context);
		antennaType = Math.max(antennaType, 0);
		boolean isGerenalSat = !isPreferedSat();
		int size = satList.size();
		int limit = -1;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("filterSatsbyAntennaType(),antennaType:" + antennaType);
		for (int i = 0; i < size; i++) {
		    int mask = satList.get(i).getMask();
		    switch (antennaType) {
		        case MenuConfigManager.DVBS_ACFG_UNICABLE2:
				if (!isCamScanUI && (satList.get(i).getMask()
		                    & (1 << 16)) == 0) {
		                continue;
		            }
		            limit = 8;
		            break;
		        case MenuConfigManager.DVBS_ACFG_UNICABLE1:
				if (!isCamScanUI && (satList.get(i).getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_SINGCAB) == 0) {
		                continue;
		            }
		            limit = 2;
		            break;
		        case MenuConfigManager.DVBS_ACFG_SINGLE:
		            if (!isCamScanUI && (satList.get(i).getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER) == 0) {
                        continue;
                    }
                    limit = 1;
		            break;
		        case MenuConfigManager.DVBS_ACFG_TONEBURST:
		            if (!isCamScanUI && (satList.get(i).getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER) == 0) {
		                continue;
		            }
		            limit = 2;
		            break;
		        case MenuConfigManager.DVBS_ACFG_DISEQC10:
					if (!isCamScanUI && (satList.get(i).getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER) == 0) {
		                continue;
		            }
		            limit = 4;
		            break;
		        case 0:
		        case MenuConfigManager.DVBS_ACFG_DISEQC11://total 16
		        case MenuConfigManager.DVBS_ACFG_DISEQC12://total 16
		        default:
				if (!isCamScanUI && (satList.get(i).getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER) == 0) {
		                continue;
		            }
		            limit = 16;
		            break;
		    }
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterSatsbyAntennaType mask="+Integer.toBinaryString(mask));
		    SatelliteInfo satelliteInfo = satList.get(i);
		    satelliteInfo.setName(satList.get(i).getSatName());
			satelliteInfo.setMask(mask);
		    if (antennaType == singlecable||antennaType==2) {
		        int defaultPosition = Math.max(satelliteInfo.getPosition() - 1, 0);//satl position begin with 1
		        defaultPosition = Math.min(defaultPosition, positionArray.length-1);
		        satelliteInfo.setType(positionArray[defaultPosition]);
		    } else {
		        if (satelliteInfo.getDiseqcType() == 2 && satelliteInfo.getPort() != 255 && satelliteInfo.getPort() < diseqcInputArray.length) {//4x1
		            satelliteInfo.setType(diseqcInputArray[Math.max(satelliteInfo.getPort(), 0)]);
		        } else {
		            satelliteInfo.setType(diseqcInputArray[diseqcInputArray.length - 1]);
		        }
		    }
		    if ((satelliteInfo.getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_STATUS_ON) != 0) {
		        satelliteInfo.setEnabled(true);
		    } else {
		        satelliteInfo.setEnabled(false);
		    }
		    if(com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag){
		        com.mediatek.wwtv.tvcenter.util.MtkLog.d("dvbs satelliteInfo","recId:"+satelliteInfo.getSatlRecId()+",position:"+satelliteInfo.getPosition()+"info:"+satelliteInfo.toString());
		    }

		    list.add(satelliteInfo);
		    namesList.add(satelliteInfo.getSatName());

		    if(list.size() == limit){
		        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterSatsbyAntennaType(), limit= " + list.size());
		        break;
		    }
		}

		//init scan mode type
		if(isGerenalSat){
		    List<SatelliteConfigEntry> entries = new SatelliteProviderManager(context).getSatelliteInfoByNameFromDB(namesList);
		    for(SatelliteInfo satInfo : list){
		        for(SatelliteConfigEntry entry : entries){
		            if(satInfo.getSatName().equals(entry.name)){
		                satInfo.dvbsScanMode = entry.scan_mode;
		                satInfo.dvbsScanType = entry.scan_type;
		                satInfo.dvbsStoreType = entry.store_type;
		                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "entry.name:"+entry.name+","+satInfo.dvbsScanMode+","+satInfo.dvbsScanType+","+satInfo.dvbsStoreType);
		                break;
		            }
		        }
		    }
		}else if(enableScanStoreTypeForCustomer()){
			for(SatelliteInfo satInfo : list){
				if(satInfo.getEnable()){
					satInfo.dvbsScanType = mCurrentScanType;
					satInfo.dvbsStoreType = mCurrentStoreType;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satInfo.name:"+satInfo.name+","+satInfo.dvbsScanMode+","+satInfo.dvbsScanType+","+satInfo.dvbsStoreType);
				}
			}
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("filterSatsbyAntennaType(),list.size::" + list.size());
		return list;
	}

	public static void setCamSatellitesMask(){
		int antennaType = MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE);
		List<SatelliteInfo> satList = getDVBSALLsatellites(7);
		antennaType = Math.max(antennaType, 0);
		int size = satList.size();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setCamSatellitesMask(),antennaType:" + antennaType+", size="+size);
		for (SatelliteInfo info : satList) {
			int mask = info.getMask();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("setCamSatellitesMask() mask="+mask);
			switch (antennaType) {
				case MenuConfigManager.DVBS_ACFG_UNICABLE2:
						mask |= (1 << 16);
						mask &= ~MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_SINGCAB;
						mask &= ~MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER;
					break;
				case MenuConfigManager.DVBS_ACFG_UNICABLE1:
						mask &= ~(1 << 16);
						mask |= MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_SINGCAB;
						mask &= ~MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER;
					break;
				case 0:
				case MenuConfigManager.DVBS_ACFG_SINGLE:
				case MenuConfigManager.DVBS_ACFG_TONEBURST:
				case MenuConfigManager.DVBS_ACFG_DISEQC10:
				case MenuConfigManager.DVBS_ACFG_DISEQC11://total 16
				case MenuConfigManager.DVBS_ACFG_DISEQC12://total 16
				default:
						mask &= ~(1 << 16);
						mask &= ~MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_SINGCAB;
						mask |= MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_LNB_UNIVER;
					break;
			}
			if ((info.getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_STATUS_ON) != 0) {
				info.setEnabled(true);
			} else {
				info.setEnabled(false);
			}
			info.setMask(mask);
			saveDVBSSatelliteToSatl(info,false, 7);
		}
	}

	public static void initCamSatelliteUserBandFreq(Context context){
		int userBand = 0;
		int bandFreq = 0;
		List<SatelliteInfo> satelliteInfos = getDVBSsatellites(context);
		if(!satelliteInfos.isEmpty()){
			userBand = satelliteInfos.get(0).getUserBand();
			bandFreq = satelliteInfos.get(0).getBandFreq();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "userBand="+userBand + ",bandFreq="+bandFreq);
		}

		//for cam
		List<SatelliteInfo> camList = getDVBSsatellites(context, 7);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandAndFreq(),camList size="+camList.size());
		for (SatelliteInfo info : camList) {
			if (userBand !=-1) {
				info.setUserBand(userBand);
			}
			if (bandFreq !=-1) {
				info.setBandFreq(bandFreq);
			}
			saveDVBSSatelliteToSatl(info,false, 7);
		}
	}

	public static void initCurrUserBandDefaultBandFreq(Context context, boolean unicable1) {
		boolean isSyncDefBandFreq = SaveValue.readWorldBooleanValue(context, "is_sync_def_band_freq");
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSyncDefBandFreq = " + isSyncDefBandFreq);
		if (!isSyncDefBandFreq) {
			int userBand = 0;
			int bandFreq = 0;
			List<SatelliteInfo> satelliteInfos = getDVBSsatellites(context);
			if (!satelliteInfos.isEmpty()) {
				userBand = satelliteInfos.get(0).getUserBand();
				bandFreq = satelliteInfos.get(0).getBandFreq();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCurrUserBandDefaultBandFreq userBand=" + userBand + ",bandFreq=" + bandFreq);
			}
			List<Integer> freqes = null;
			if (unicable1) {
				freqes = DataSeparaterUtil.getInstance().getDefaultUnicable1Frequency();
			} else {
				freqes = DataSeparaterUtil.getInstance().getDefaultUnicable2Frequency();
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCurrUserBandDefaultBandFreq  freqes1 size= " + freqes.size());
			if (freqes.size() > userBand) {
				int defFreq = freqes.get(userBand);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "defFreq = " + defFreq);
				if (bandFreq != defFreq && defFreq >= 950 && defFreq <= 2150) {
					for (SatelliteInfo info : satelliteInfos) {
						info.setBandFreq(defFreq);
						saveDVBSSatelliteToSatl(info);
					}

					//for otherBroadcast
					int otherBroadcastSvl = isPreferedSat() ? 3 : 4 ;
					List<SatelliteInfo> broadcastList = getDVBSsatellites(context, otherBroadcastSvl);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initCurrUserBandDefaultBandFreq(),broadcastList size="+broadcastList.size());
					for (SatelliteInfo info : broadcastList) {
						info.setBandFreq(defFreq);
						saveDVBSSatelliteToSatl(info,false, otherBroadcastSvl);
					}
				}
			}
			SaveValue.saveWorldBooleanValue(context, "is_sync_def_band_freq", true, false);
		}
	}


	public void saveDVBSConfigSetting(Context context, int atnaType, int band, int freq) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG ,"saveDVBSConfigSetting()," + "atnaType:" + atnaType
	        + ",band:" + band + ",freq:" + freq);
	    List<SatelliteInfo> satList = getDVBSsatellites(context);
	    for (SatelliteInfo info : satList) {
	      if (band != -1) {
	        info.setUserBand(band);
	      }

	      if (freq != -1) {
	        info.setBandFreq(freq);
	      } else {
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveDVBSConfigSetting() freq is -1");
	      }
	      saveDVBSSatelliteToSatl(info);
	    }

	  }

	public void saveDVBSConfigSetting(Context context, int atnaType, int band, int freq,
	          int subBand,int subFreq){
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSConfigSetting(),"+"atnaType:"+atnaType+",band:"+band
	              +",freq:"+freq+",subBand:"+subBand+",subFreq:"+subFreq);
	      List<SatelliteInfo> satList = getDVBSsatellites(context);
	      for (SatelliteInfo info : satList) {
	        if (band !=-1) {
	            info.setUserBand(band);
	        }
	        if (freq !=-1) {
	            info.setBandFreq(freq);
	        }
	        if (subBand !=-1){
	            info.setSubUserBand(subBand);
	        }
	        if (subFreq !=-1){
	            info.setSubBandFreq(subFreq);
	        }
	        saveDVBSSatelliteToSatl(info);
	    }
	  }

	public void saveDVBUserBand(Context context, int band){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBUserBand()"+",band:"+band);
        List<SatelliteInfo> satList = getDVBSsatellites(context);
        for (SatelliteInfo info : satList) {
          if (band !=-1) {
              info.setUserBand(band);
          }
          saveDVBSSatelliteToSatl(info);
      }
        //for cam
		List<SatelliteInfo> camList = getDVBSsatellites(context, 7);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBUserBand(),camList size="+camList.size());
		for (SatelliteInfo info : camList) {
			if (band !=-1) {
				info.setUserBand(band);
			}
			saveDVBSSatelliteToSatl(info, false, 7);
		}

		//for broadcast
		int otherBroadcastSvl = isPreferedSat() ? 3 : 4 ;
		List<SatelliteInfo> broadcastList = getDVBSsatellites(context, otherBroadcastSvl);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandFreq(),broadcastList size="+broadcastList.size());
		for (SatelliteInfo info : broadcastList) {
			if (band !=-1) {
				info.setUserBand(band);
			}
			saveDVBSSatelliteToSatl(info,false, otherBroadcastSvl);
		}
    }

	public void saveDVBSBandFreq(Context context, int freq){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandFreq(),"+"freq:"+freq);
        List<SatelliteInfo> satList = getDVBSsatellites(context);
        for (SatelliteInfo info : satList) {
          if (freq !=-1) {
              info.setBandFreq(freq);
          }
          saveDVBSSatelliteToSatl(info);
      }
        //for cam
        List<SatelliteInfo> camList = getDVBSsatellites(context, 7);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandFreq(),camList size="+camList.size());
        for (SatelliteInfo info : camList) {
          if (freq !=-1) {
              info.setBandFreq(freq);
          }
          saveDVBSSatelliteToSatl(info,false, 7);
      }

        //for broadcast
		int otherBroadcastSvl = isPreferedSat() ? 3 : 4 ;
		List<SatelliteInfo> broadcastList = getDVBSsatellites(context, otherBroadcastSvl);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandFreq(),broadcastList size="+broadcastList.size());
		for (SatelliteInfo info : broadcastList) {
			if (freq !=-1) {
				info.setBandFreq(freq);
			}
			saveDVBSSatelliteToSatl(info,false, otherBroadcastSvl);
		}
    }

	public void saveDVBSBandAndFreq(Context context, int band, int freq){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandAndFreq(),"+"band:"+band+",freq:"+freq);
        List<SatelliteInfo> satList = getDVBSsatellites(context);
        for (SatelliteInfo info : satList) {
          if (band !=-1) {
              info.setUserBand(band);
          }
          if (freq !=-1) {
              info.setBandFreq(freq);
          }
          saveDVBSSatelliteToSatl(info);
      }

		//for cam
		List<SatelliteInfo> camList = getDVBSsatellites(context, 7);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandAndFreq(),camList size="+camList.size());
		for (SatelliteInfo info : camList) {
			if (band !=-1) {
				info.setUserBand(band);
			}
			if (freq !=-1) {
				info.setBandFreq(freq);
			}
			saveDVBSSatelliteToSatl(info,false, 7);
		}

		//for broadcast
		int otherBroadcastSvl = isPreferedSat() ? 3 : 4 ;
		List<SatelliteInfo> broadcastList = getDVBSsatellites(context, otherBroadcastSvl);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveDVBSBandFreq(),broadcastList size="+broadcastList.size());
		for (SatelliteInfo info : broadcastList) {
			if (band !=-1) {
				info.setUserBand(band);
			}
			if (freq !=-1) {
				info.setBandFreq(freq);
			}
			saveDVBSSatelliteToSatl(info,false, otherBroadcastSvl);
		}
    }

	public static void saveDVBSSatelliteToSatl(SatelliteInfo info) {
	    saveDVBSSatelliteToSatl(info, true);
	}

	/**
	 * update satellite info to SATL
	 * @param info
	 */
	public static void saveDVBSSatelliteToSatl(SatelliteInfo info, boolean needStoreToDb) {
		int svlID = isCamScanUI ? CommonIntegration.DB_CI_PLUS_SVLID_SAT : CommonIntegration.getInstance().getSvl();//change from getSvlFromACFG
		saveDVBSSatelliteToSatl(info, needStoreToDb, svlID);
	}

	public static void saveDVBSSatelliteToSatl(SatelliteInfo info, boolean needStoreToDb, int svlID) {
		if(info!=null){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveDVBSSatelliteToSatl().info.Enable:" + info.getEnable() + ">>>>" + info.toString());
		}else{
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveDVBSSatelliteToSatl().info==null;");
			return;
		}

		MtkTvDvbsConfigBase base = new MtkTvDvbsConfigBase();

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveDVBSSatelliteToSatl().svlID=="+svlID);
		if ((info.getMask() & MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_STATUS_ON) == 0) {
			if (info.getEnable()) {
				info.setMask(info.getMask() + MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_STATUS_ON);
			}
		} else {
			if (!info.getEnable()) {
				info.setMask(info.getMask() - MtkTvDvbsConfigBase.DVBS_CFG_SATL_MASK_STATUS_ON);
			}
		}

		try {
			//base.satllistLockDatabase(svlID);
			base.updateSatlRecord(svlID, (MtkTvDvbsConfigInfoBase) info, true);
		} catch (Exception e) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveDVBSSatelliteToSatl() Exception!!!!!," + e.toString());
		} catch (Error e) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("saveDVBSSatelliteToSatl() ERROR!!!!!!!," + e.toString());
		} finally {
		    //base.satllistUnLockDatabase(svlID);
		    if(needStoreToDb && !CommonIntegration.getInstance().isPreferSatMode()) {
		        saveSatelliteInfoToDb(info);
		    }
		}
		MtkTvConfig.getInstance().setConfigValue(
				MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
	}

	public synchronized static boolean saveSatelliteInfoToDb(SatelliteInfo info) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveSatelliteInfoToDb. info:" + info.toString());
	    int antennaType = MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE);
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveSatelliteInfoToDb. antennaType:" + antennaType);
	    SatelliteConfigEntry dbEntry = new SatelliteConfigEntry();
	    int recID = info.getSatlRecId();
	    dbEntry.name = info.getSatName();
	    dbEntry.scan_mode = info.dvbsScanMode;
	    dbEntry.scan_type = info.dvbsScanType;
	    dbEntry.store_type = info.dvbsStoreType;

	    dbEntry.sat_position = info.getOrbPos();
	    dbEntry.lnb_power = info.getLnbPower();
	    dbEntry.lnb_frequency_low = info.getLnbLowFreq();
	    dbEntry.lnb_frequency_high = info.getLnbHighFreq();
	    dbEntry.lnb_frequency_switch = info.getLnbSwitchFreq();
	    dbEntry.tone22K = info.getM22k();
	    dbEntry.tone_burst = info.getToneBurst();

	    if(antennaType == MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_SINGLECABLE) {
	        dbEntry.unicableI_position = info.getPosition();
	    } else if(antennaType == MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_SINGLECABLE_JESS) {
	        dbEntry.unicableII_position = info.getPosition();
	    } else if(antennaType == MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_DISEQC1D0) {
	        if(info.getPort() == 255) {
	            dbEntry.diseqc10_port = 0;
	        } else {
	            dbEntry.diseqc10_port = info.getPort() + 1;
	        }
	    } else if(antennaType == MtkTvConfigTypeBase.DVBS_ANTENNA_TYPE_UNIVERSAL_DISEQC1D1) {
	        if(info.getPortEx() == 255) {
	            dbEntry.diseqc11_port = 0;
	        } else {
	            dbEntry.diseqc11_port = info.getPortEx() + 1;
	        }
	    }
	    MtkTvScanTpInfo tpInfo = getDVBSTransponder(recID);
	    dbEntry.transponder_frequency = tpInfo.i4Frequency;
	    dbEntry.transponder_symbolrate = tpInfo.i4Symbolrate;
	    if(tpInfo.ePol == TunerPolarizationType.POL_LIN_HORIZONTAL) {
	        dbEntry.transponder_epol = MenuConfigManager.TP_POLARIZATION_HORIZONTAL;
	    } else if(tpInfo.ePol  == TunerPolarizationType.POL_LIN_VERTICAL) {
	        dbEntry.transponder_epol = MenuConfigManager.TP_POLARIZATION_VERTICAL;
	    } else if(tpInfo.ePol == TunerPolarizationType.POL_CIR_LEFT) {
	        dbEntry.transponder_epol = MenuConfigManager.TP_POLARIZATION_LEFT;
	    } else if(tpInfo.ePol == TunerPolarizationType.POL_CIR_RIGHT) {
	        dbEntry.transponder_epol = MenuConfigManager.TP_POLARIZATION_RIGHT;
	    }
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveSatelliteInfoToDb. dbEntry:" + dbEntry.toString());
	    return satelliteProviderManager.saveSatelliteInfoToDB(dbEntry) == 1;
	}

	public synchronized static void initSatellitesFromXML(Context context, int antennaType){
	    if(isPreferedSat()) {
	        return;
	    }
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSatellitesFromXML. start="+SystemClock.uptimeMillis());
	    if(satelliteProviderManager == null){
	        satelliteProviderManager = new SatelliteProviderManager(context);
	    }
		if(satelliteProviderManager.getAllSatellitesFromDB().isEmpty()){//satellite DB maybe cleaned by cleaning data
            satelliteProviderManager.syncSatellitesToDBFromXML();
		}
	    List<SatelliteConfigEntry> mSatelliteConfigEntries = PartnerSettingsConfig.
				getSatelliteInfoListByCountry(MtkTvConfig.getInstance().getCountry(), PartnerSettingsConfig.getSatelliteinfolist());
	    List<SatelliteInfo> satelliteInfos = getDVBSsatellites(context);
	    if(!mSatelliteConfigEntries.isEmpty()){
	        for (int i = 0; i < mSatelliteConfigEntries.size(); i++) {
                SatelliteConfigEntry entry = mSatelliteConfigEntries.get(i);

                //keep diseqc port setting same to satl
                refactSatEntryExceptDiseqc(satelliteInfos, entry, antennaType);

                satelliteProviderManager.saveSatelliteInfoToDB(entry);

                if(i < satelliteInfos.size()){

                    ScanContent.initSatInfoFromEntry(satelliteInfos.get(i), entry, antennaType);

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSatellitesFromXML. satelliteInfos="+satelliteInfos.get(i).getSatName());
                    saveDVBSSatelliteToSatl(satelliteInfos.get(i),false);
                    
                    setDVBSTPInfo(satelliteInfos.get(i).getSatlRecId(), getDVBSTransponder(satelliteInfos.get(i).getSatlRecId()), entry);
                }
            }
	    }
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initSatellitesFromXML. end="+SystemClock.uptimeMillis());
	}

	private static void refactSatEntryExceptDiseqc(List<SatelliteInfo> satelliteInfos, SatelliteConfigEntry entry, int antennaType){
	    for(SatelliteInfo info : satelliteInfos){
	        if(info.getSatName().equals(entry.name)){
	            switch (antennaType) {
                    case MenuConfigManager.DVBS_ACFG_DISEQC10:
                        entry.diseqc10_port = info.getPort() == 255 ? 0 : info.getPort() + 1;
                        break;
                    case MenuConfigManager.DVBS_ACFG_DISEQC11:
                        entry.diseqc11_port = info.getPortEx() == 255 ? 0 : info.getPortEx() + 1;
                        break;
                    case MenuConfigManager.DVBS_ACFG_UNICABLE1:
                        entry.unicableI_position = info.getPosition() == 0 ? 0 : info.getPosition() - 1;
                        break;
                    case MenuConfigManager.DVBS_ACFG_UNICABLE2:
                        entry.unicableII_position = info.getPosition() == 0 ? 0 : info.getPosition() - 1;
                        break;
                    default:
                        break;
                }
	        }
	    }
	}

	public synchronized static void applyDBSatellitesToSatl(Context context, int antennaType){
	    if(isPreferedSat()) {
	        return;
	    }
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "applyDBSatellitesToSatl. start="+SystemClock.uptimeMillis());
	    if(satelliteProviderManager == null){
	        satelliteProviderManager = new SatelliteProviderManager(context);
	    }
	    List<SatelliteInfo> satelliteInfos = getDVBSsatellites(context);
	    List<SatelliteConfigEntry> mSatelliteConfigEntries = PartnerSettingsConfig.
				getSatelliteInfoListByCountry(MtkTvConfig.getInstance().getCountry(),satelliteProviderManager.getAllSatellitesFromDB());
	    if(!mSatelliteConfigEntries.isEmpty()){
	        for (int i = 0; i < mSatelliteConfigEntries.size(); i++) {
	            boolean foundInDb = false;
	            if(i < satelliteInfos.size() && i < mSatelliteConfigEntries.size()){
	                for(SatelliteConfigEntry entry : mSatelliteConfigEntries){
	                    if(satelliteInfos.get(i).getSatName().equals(entry.name)){
	                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "applyDBSatellitesToSatl. same name not apply");
	                        foundInDb = true;
	                        break;
	                    }
	                }
	                if(!foundInDb){
	                    ScanContent.initSatInfoFromEntry(satelliteInfos.get(i), mSatelliteConfigEntries.get(i), antennaType);
	                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "applyDBSatellitesToSatl. satelliteInfos="+satelliteInfos.get(i).getSatName());
	                    saveDVBSSatelliteToSatl(satelliteInfos.get(i),false);
	                    setDVBSTPInfo(satelliteInfos.get(i).getSatlRecId(), getDVBSTransponder(satelliteInfos.get(i).getSatlRecId()), mSatelliteConfigEntries.get(i));
	                }
	            }
	        }
	    }else {//satellite DB maybe cleaned by cleaning data
			satelliteProviderManager.syncSatellitesToDBFromXML();
			initSatellitesFromXML(context, antennaType);
		}
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "applyDBSatellitesToSatl. end="+SystemClock.uptimeMillis());
	}

	public static List<String> getAvailableSatelliteNameList(List<SatelliteInfo> list) {
	    List<String> names = new ArrayList<String>();
	    for (SatelliteInfo info : list) {
	        names.add(info.getSatName());
	    }
	    List<String> result = new ArrayList<String>();
	    List<SatelliteConfigEntry> entries = PartnerSettingsConfig.
				getSatelliteInfoListByCountry(MtkTvConfig.getInstance().getCountry(),PartnerSettingsConfig.getSatelliteinfolist());
	    for (SatelliteConfigEntry entry : entries) {
	        if(!names.contains(entry.name)) {
	            result.add(entry.name);
	        }
	    }
	    return result;
	}

	public static boolean isSatelliteNameExistInXml(String name){
		if(name == null || name.length() == 0){
			return  false;
		}
		List<SatelliteConfigEntry> entries = PartnerSettingsConfig.getSatelliteinfolist();
		for (SatelliteConfigEntry entry : entries) {
			if(name.equals(entry.name)) {
				return true;
			}
		}
		return  false;
	}

	public static void initSatInfoFromEntry(SatelliteInfo satInfo, SatelliteConfigEntry entry, int antennaType) {
	    satInfo.setSatName(entry.name);
	    satInfo.setOrbPos(entry.sat_position);
        satInfo.dvbsScanMode = entry.scan_mode;
        satInfo.dvbsScanType = entry.scan_type;
        satInfo.dvbsStoreType = entry.store_type;
        satInfo.setLnbPower(entry.lnb_power);
        satInfo.setLnbLowFreq(entry.lnb_frequency_low);
        satInfo.setLnbHighFreq(entry.lnb_frequency_high);
        satInfo.setLnbSwitchFreq(entry.lnb_frequency_switch);
        if(0 == entry.lnb_frequency_high){
              satInfo.setLnbType(LNB_SINGLE_FREQ);
         } else {
              satInfo.setLnbType(LNB_DUAL_FREQ);
         }
        satInfo.setM22k(entry.tone22K);
        satInfo.setToneBurst(entry.tone_burst);
        switch (antennaType) {
            case MenuConfigManager.DVBS_ACFG_UNICABLE1:
                satInfo.setPosition(entry.unicableI_position + 1);
                break;
            case MenuConfigManager.DVBS_ACFG_UNICABLE2:
                satInfo.setPosition(entry.unicableII_position + 1);
                break;
            case MenuConfigManager.DVBS_ACFG_DISEQC10:
                satInfo.setPort(entry.diseqc10_port == 0 ? 255 : entry.diseqc10_port - 1);
                satInfo.setDiseqcType(entry.diseqc10_port == 0 ? 0 : 2);//is 4x1
                break;
            case MenuConfigManager.DVBS_ACFG_DISEQC11:
                satInfo.setPortEx(entry.diseqc11_port == 0 ? 255 : entry.diseqc11_port - 1);
                satInfo.setDiseqcTypeEx(entry.diseqc11_port == 0 ? 0 : 4);
                break;
            default:
                break;
        }
    }

	public static int setSelectedSatelliteOPFromMenu(Context context, int position) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedSatelliteOPFromMenu(),position:" + position);
		//MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
		if (ScanContent.isPreferedSat()) {
			int currentOP = ScanContent.getDVBSCurrentOP();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedSatelliteOPFromMenu(),currentOP:" + currentOP);
			//if (currentOP == 0) {// this is for after user change tuner mode but has no set Prefer sat OP
				currentOP = setSatOP(context, position);
				MtkTvConfig.getInstance().setConfigValue(SAT_BRDCSTER, currentOP);
			/*} else {//if user already set OP, not change it in ACFG, only set sat OP before scan
				currentOP = setSatOP(context, position);
			}*/
			return currentOP;
		} else {
		    //new UI no need init dvbs satl
			//base.dvbsSetUIDeftSateInfoByOpt(0); // this is for general sat set JAVA OP to 0 for get re-scan satellites
			MtkTvConfig.getInstance().setConfigValue(SAT_BRDCSTER, 0);//set ACFG OP to 0
			return 0;
		}
	}

	public static void setSelectedCamScanOP(String profileName) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedCamScanOP(),profileName:" + profileName);
		new MtkTvScanDvbsBase().dvbsSetUIDeftSateInfoByOpName(profileName);
	}

	/**
	 * Only set DVBS OP,init sat list,not write to A-CFG.
	 *
	 * @param context
	 * @param position
	 * @return
	 */
	public static int setSatOP(Context context, int position) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSatOP(),position:" + position);
		MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();

		//List<String> opsStr = Arrays.asList(context.getResources().getStringArray(R.array.dvbs_operators));
		List<String> opList = getDVBSOperatorList(context);
		int value = 0;
		if (!opList.isEmpty() && position < opList.size() && position >= 0) {
		    DVBSOperator dvbsOperator = new DVBSOperator(context);
		    value = dvbsOperator.getOperatorByName(opList.get(position));
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedSatelliteOP(),value:" + value);
		} else if(position == -1) {
		    // base.dvbsSetUIDeftSateInfoByOpt(curop);
		    return ScanContent.getDVBSCurrentOP();
		} else {
		    return 0;
		}
		if(opList.get(position).equals(context.getString(R.string.dvbs_operator_name_digi))) {
		  String country = MtkTvConfig.getInstance().getCountry();
		  if(country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_HUN)) {
		    value = MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGI_TV_HUN;
		  } else if(country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ROU)) {
		    value = MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGI_TV_ROU;
		  }
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedSatelliteOP(),op value:" + value);
		base.dvbsSetUIDeftSateInfoByOpt(value);
//		Toast.makeText(context, value+"-oper", 5000).show();
		return value;
	}

	public static int setSelectedSatelliteOP(Context context, int position) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedSatelliteOP(),position:" + position);
		// setSatOP(context, position);

		MtkTvConfig.getInstance().setConfigValue(SAT_BRDCSTER, setSatOP(context, position));

		int currentOP = ScanContent.getDVBSCurrentOP();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setSelectedSatelliteOP(),currentOP()," + currentOP);
		return currentOP;
	}

	public static String getDVBSCurrentOPStr(Context context) {
		int op = MtkTvConfig.getInstance().getConfigValue(SAT_BRDCSTER);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSCurrentOPStr(),currentOP()," + op);
		return new DVBSOperator(context).getNameByOperator(op);
	}

	public static int getDVBSCurrentOP() {
		return MtkTvConfig.getInstance().getConfigValue(SAT_BRDCSTER);
	}

	public static boolean isDVBSTivibuOP(){
		return getDVBSCurrentOP() == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TIVIBU;
	}
	public static boolean isDVBSSkyOP(){
		return getDVBSCurrentOP() == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SKY_DEUTSCHLAND;
	}
	public static boolean isDVBSCanalDigitalOP(){
		return getDVBSCurrentOP() == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANAL_DIGITAL;
	}

	public static List<LcnConflictGroup> getLcnConflictGroup(Context context) {

		LcnConflictGroup[] lcnList = MtkTvScan.getInstance()
				.getScanDvbtInstance().uiOpGetLcnConflictGroup();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getLcnConflictGroup"+lcnList.toString());
		return Arrays.asList(lcnList);
	}

	public static List<String> getTRDChannelList(Context context,int index) {

		LcnConflictGroup nwkList = getLcnConflictGroup(context).get(index);

		final List<String> networkList = new ArrayList<String>();
		networkList.addAll(Arrays.asList(nwkList.channelName));
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getTRDChannelList-1"+Arrays.asList(nwkList.channelName));
		networkList.add(context.getString(R.string.scan_trd_lcn_CONFLICT_USE_DEFAULT));
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getTRDChannelList-2"+networkList.toString());
		return networkList;
	}
	/**
	 * Only for Menu.
	 *
	 * @param context
	 * @param groupIndex
	 * @param position
	 */
	public static void setAfterScanLCNForMenu(Context context, int groupIndex,
			int position) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setAfterScanLCN(),position:" + position);
		position = Math.max(position, 0);

		if (getTRDChannelList(context, groupIndex)
				.get(position)
				.equalsIgnoreCase(
						context.getString(R.string.scan_trd_lcn_CONFLICT_USE_DEFAULT))) {
			restoreForAllLCNChannelsForMenu(context, groupIndex);
		} else {
			int currentGroup = groupIndex;
			List<LcnConflictGroup> groups = getLcnConflictGroup(context);

			MtkTvScan
					.getInstance()
					.getScanDvbtInstance()
					.uiOpSetLcnConflictGroup(groups.get(currentGroup),
							groups.get(currentGroup).channelName[position]);
		}
	}

	/**
	 * Only for Menu.
	 *
	 * @param context
	 * @param groupIndex
	 */
	public static void restoreForAllLCNChannelsForMenu(Context context,
			int groupIndex) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("restoreForAllLCNChannels()");
		int currentGroup = groupIndex;
		List<LcnConflictGroup> groups = getLcnConflictGroup(context);
		if (currentGroup >= groups.size()) {
			return;
		}

		for (int i = currentGroup; i < groups.size(); i++) {
			LcnConflictGroup group = groups.get(i);
			MtkTvScan.getInstance().getScanDvbtInstance()
					.uiOpSetLcnConflictGroup(group, group.channelName[0]);
		}
	}

	// zh-CN is Chinese
	// en- is English
	// zh-TW is Chinese-TW
	/*private static String getLocaleLanguage() {
		Locale mLocale = Locale.getDefault();
		return String.format("%s-%s", mLocale.getLanguage(),
				mLocale.getCountry());
	}

	private static String getLocaleLanguage(Locale mLocale) {
		return String.format("%s-%s", mLocale.getLanguage(),
				mLocale.getCountry());
	}*/


	public static List<String> getRegionEULanguageCodeList() {
		return new ArrayList<String>(
				Arrays.asList(new String[] { "en", "eu", "ca", "hr", "cs",
						"da", "nl", "fi", "fr", "gd", "gl", "de", "hu", "it",
						"no", "pl", "pt", "ro", "sr", "sk", "sl", "es", "sv",
						"tr", "cy", "et", "ru" }));
	}

	public static List<String> getRegionEUMTKLanguageList() {
		// copy from menu/util/OSDLanguage.java
		return new ArrayList<String>(
				Arrays.asList(new String[] { MtkTvConfigType.S639_CFG_LANG_ENG,
						MtkTvConfigType.S639_CFG_LANG_BAQ,
						MtkTvConfigType.S639_CFG_LANG_CAT,
						MtkTvConfigType.S639_CFG_LANG_SCR,
						MtkTvConfigType.S639_CFG_LANG_CZE,
						MtkTvConfigType.S638_CFG_LANG_DAN,
						MtkTvConfigType.S639_CFG_LANG_DUT,
						MtkTvConfigType.S639_CFG_LANG_FIN,
						MtkTvConfigType.S639_CFG_LANG_FRA, "gla", "glg",
						MtkTvConfigType.S639_CFG_LANG_DEU,
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
						MtkTvConfigType.S639_CFG_LANG_RUS }));
	}

	public static MyLanguageData getRegionEULanguageStrForMenu(Context context,
			int position) {
		List<String> mLanguageOSDArray = getRegionEUMTKLanguageList();

		LanguageUtil.MyLanguageData data = new LanguageUtil(context).new MyLanguageData();

		List<String> iso2LanguageCodeList = getRegionEULanguageCodeList();
		if (position < iso2LanguageCodeList.size()) {
			data.local = new Locale(iso2LanguageCodeList.get(position), "");
		} else {
			data.local = Locale.ENGLISH;
		}

		if (position < mLanguageOSDArray.size()) {
			data.tvAPILanguageStr = mLanguageOSDArray.get(position);
		} else {
			data.tvAPILanguageStr = MtkTvConfigType.S639_CFG_LANG_ENG;
		}

		return data;
	}

	public void setDVBSAntennaType(int value) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBS_AntennaType()," + value);
		MtkTvConfig.getInstance().setConfigValue(SAT_ANTENNA_TYPE, value);
	}

	public void updateDVBSConfigInfo(Context context, String userBand, String bandFreq) {
		// context.getString(R.string.dvbs_band_freq_user_define
		// dvbs_user_band_arrays
		int userBandInt = Arrays.asList(context.getResources().getStringArray(
						R.array.dvbs_user_band_arrays)).indexOf(userBand);
		int bandFreqInt = 0;

		if (!bandFreq.equalsIgnoreCase(context.getString(R.string.dvbs_band_freq_user_define))) {
			try {
				bandFreqInt = Integer.valueOf(bandFreq);
			} catch (Exception e) {
			    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Exception");
			}
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSConfigInfo()," + "tunerBandInt:" + userBand
				+ ",bandFreq:" + bandFreq);

		List<SatelliteInfo> satList = getDVBSsatellites(context);
		for (SatelliteInfo info : satList) {
			if (userBandInt != -1) {
				info.setUserBand(userBandInt);
			}

			if (bandFreqInt != -1) {
				info.setBandFreq(bandFreqInt);
			}
			saveDVBSSatelliteToSatl(info);
		}
	}


	List<String> getDVBSSatelliteType(Context context,
			String selectedSatelliteID) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();

		list = Arrays.asList(context.getResources().getStringArray(
				R.array.dvbs_sat_on_off));
		return list;
	}

	String[] getDVBSSatelliteType(Context context) {
		// ConfigInfo info = manager.getConfigInfo();

		return context.getResources().getStringArray(R.array.dvbs_sat_on_off);
	}

	public String[] getDVBSPosition(Context context) {
		// ConfigInfo info = manager.getConfigInfo();

		return context.getResources().getStringArray(R.array.dvbs_position_arrays);
	}

	List<String> getDVBSLNBPower(Context context, String selectedSatelliteID) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();

		list = Arrays.asList(context.getResources().getStringArray(
				R.array.dvbs_sat_on_off));
		return list;
	}

	String[] getDVBSLNBPower(Context context) {
		return context.getResources().getStringArray(R.array.dvbs_sat_lnb_power);
	}

	List<String> getDVBSDiseqcInput(Context context, String selectedSatelliteID) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();

		list = Arrays.asList(getDVBSDiseqcInput(context));
		return list;
	}

/*	private static String[] getDVBSDiseqc10Port(Context context) {
		return context.getResources().getStringArray(R.array.dvbs_diseqc_10port_arrays);
	}

	private static String[] getDVBSDiseqc11Port(Context context) {
		return context.getResources().getStringArray(R.array.dvbs_diseqc_input_arrays);
	}

	private static String[] getDVBSDiseqcMotor(Context context) {
		return context.getResources().getStringArray(R.array.dvbs_diseqc_motor_arrays);
	}*/

	static String[] getDVBSDiseqcInput(Context context) {
		return context.getResources().getStringArray(R.array.dvbs_diseqc_input_arrays);
	}

	List<String> getDVBSLNBFreq(Context context, String selectedSatelliteID) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();
		list = Arrays.asList(context.getResources().getStringArray(
				R.array.dvbs_sat_lnbfreq));
		return list;
	}

	String[] getDVBSLNBFreq(Context context) {
		return context.getResources().getStringArray(R.array.dvbs_sat_lnbfreq);
	}

	List<String> getDVBSTone22KHZ(Context context, String selectedSatelliteID) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();

		list = Arrays.asList(context.getResources().getStringArray(
				R.array.dvbs_tone_22khz_arrays));
		return list;
	}

	String[] getDVBSTone22KHZ(Context context) {
		return context.getResources().getStringArray(
				R.array.dvbs_tone_22khz_arrays);
	}

	List<String> getDVBSToneBurst(Context context, String selectedSatelliteID) {
		// ConfigInfo info = manager.getConfigInfo();

		List<String> list = new ArrayList<String>();

		list = Arrays.asList(context.getResources().getStringArray(
				R.array.dvbs_tone_burst_arrays));
		return list;
	}

	String[] getDVBSToneBurst(Context context) {
		return context.getResources().getStringArray(
				R.array.dvbs_tone_burst_arrays);
	}

	public static void setDVBSTPInfo(int satID, MtkTvScanTpInfo tpinfo) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSTPInfo(),satID:" + satID+",isCamScanUI:"+isCamScanUI);
        
        if(isCamScanUI){
            mCamTpInfo = tpinfo;
            return;
        }       
        try {
            MtkTvScanDvbsBase scanDVBSbase = new MtkTvScanDvbsBase();
            scanDVBSbase.dvbsSetUISateTransponder(satID, tpinfo);
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSTPInfo(),satID:" + e.toString());
        }
    }
	
	public static void setDVBSTPInfo(Context context, int satID, MtkTvScanTpInfo tpinfo) {//save to DB
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSTPInfo(),satID:" + satID+",isCamScanUI:"+isCamScanUI);

		if(isCamScanUI){
		    mCamTpInfo = tpinfo;
		    return;
		}
		try {
			MtkTvScanDvbsBase scanDVBSbase = new MtkTvScanDvbsBase();
			scanDVBSbase.dvbsSetUISateTransponder(satID, tpinfo);
		} catch (Exception e) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSTPInfo(),satID:" + e.toString());
		}
		if(!isPreferedSat()){//save to DB		
		    saveSatelliteInfoToDb(getDVBSsatellitesBySatID(context, satID));
		}
	}
	
	public static void setDVBSTPInfo(int satID, MtkTvScanTpInfo tpinfo, SatelliteConfigEntry entry) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSTPInfo(),satID:" + satID+",isCamScanUI:"+isCamScanUI+",entry="+entry.toString());
	    if(isCamScanUI){
	        mCamTpInfo = tpinfo;
	        return;
	    }		
	    try {
	        tpinfo.i4Frequency = entry.transponder_frequency;
	        tpinfo.i4Symbolrate = entry.transponder_symbolrate;
	        if (entry.transponder_epol == MenuConfigManager.TP_POLARIZATION_HORIZONTAL) {
	            tpinfo.ePol = TunerPolarizationType.POL_LIN_HORIZONTAL;
	        } else if (entry.transponder_epol == MenuConfigManager.TP_POLARIZATION_VERTICAL) {
	            tpinfo.ePol  = TunerPolarizationType.POL_LIN_VERTICAL;
	        } else if(entry.transponder_epol == MenuConfigManager.TP_POLARIZATION_LEFT) {
	            tpinfo.ePol = TunerPolarizationType.POL_CIR_LEFT;
	        } else if(entry.transponder_epol == MenuConfigManager.TP_POLARIZATION_RIGHT) {
	            tpinfo.ePol = TunerPolarizationType.POL_CIR_RIGHT;
	        }

	        MtkTvScanDvbsBase scanDVBSbase = new MtkTvScanDvbsBase();
	        scanDVBSbase.dvbsSetUISateTransponder(satID, tpinfo);
	    } catch (Exception e) {
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSTPInfo(),satID:" + e.toString());
	    }
	}

	public static List<SatelliteTPInfo> getDVBSTransponderList(List<SatelliteInfo> satelliteList) {
		List<SatelliteTPInfo> tempList = new ArrayList<SatelliteTPInfo>();
		for (SatelliteInfo tempInfo:satelliteList) {
			SatelliteTPInfo satelliteTPInfo = new SatelliteTPInfo();
			satelliteTPInfo.mRescanSatLocalTPInfo = getDVBSTransponder(tempInfo.getSatlRecId());
			satelliteTPInfo.mSatRecId = tempInfo.getSatlRecId();
			tempList.add(satelliteTPInfo);
		}
		return tempList;
	}

	public static void restoreSatTpInfo(List<SatelliteInfo> satelliteList, List<SatelliteTPInfo> satelliteTPInfoList) {
		if (satelliteList != null && satelliteTPInfoList != null) {
			for (SatelliteInfo tempInfo:satelliteList) {
				for (SatelliteTPInfo tempTPInfo:satelliteTPInfoList) {
					if (tempInfo.getSatlRecId() == tempTPInfo.mSatRecId) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d("restoreSatTpInfo(),satID:" + tempTPInfo.mSatRecId);
						setDVBSTPInfo(tempTPInfo.mSatRecId, tempTPInfo.mRescanSatLocalTPInfo);
//						satelliteList.remove(tempInfo);
						satelliteTPInfoList.remove(tempTPInfo);
						break;
					}
				}
			}
		}
	}

	public static MtkTvScanTpInfo getSavedBgmTPInfo(int satelliteID){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSavedBgmTPInfo(),satID:" + satelliteID+",mTpHasModified="+mTpHasModified);
		if(mTpHasModified){
			return getDVBSTransponder(satelliteID);
		}
		MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
		MtkTvScanDvbsBase.MtkTvSbDvbsBGMData bgmData = base.new MtkTvSbDvbsBGMData();
		base.dvbsGetSaveBgmData(bgmData);
		MtkTvScanTpInfo tpInfo = getDVBSTransponder(satelliteID);
		for (MtkTvScanDvbsBase.MtkTvSbDvbsBGMData.OneBGMData one : bgmData.bgmData_List) {
			if (one != null && one.i4SatRecID == satelliteID) {
				if (one.eSbDvbsScanType == MtkTvScanDvbsBase.SbDvbsScanType.SB_DVBS_SCAN_TYPE_NETWORK_SCAN) {
					if (one.networkScanInfo != null && one.networkScanInfo.tpInfo != null) {
						tpInfo = one.networkScanInfo.tpInfo;
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "one i4Frequency" + tpInfo.i4Frequency + "," + tpInfo.ePol + "," + tpInfo.i4Symbolrate);
						break;
					}
				} else {//Saved BGM TP only used for Network Scan, so Full scan will use Satl TP
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSavedBgmTPInfo(),  Full scan will use Satl TP ");
					//tpInfo = getDVBSTransponder(satelliteID);
					break;
				}

			}
		}
		return tpInfo;
	}

	public static MtkTvScanTpInfo getDVBSTransponder(int satelliteID) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSTransponder(),satID:" + satelliteID +",isCamScanUI="+isCamScanUI);

		if(isCamScanUI && mCamTpInfo != null){
		    return mCamTpInfo;
		}

		MtkTvScanDvbsBase scanDVBSbase = new MtkTvScanDvbsBase();
		MtkTvScanTpInfo tpInfo = scanDVBSbase.new MtkTvScanTpInfo();

		scanDVBSbase.dvbsGetUISateTransponder(satelliteID, tpInfo);

		if (tpInfo != null) {
			if (tpInfo.ePol == null) {
				tpInfo.ePol = TunerPolarizationType.POL_LIN_HORIZONTAL;
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(String.format(
					"getDVBSTransponder(),Freq:%d,Symb:%d,Pol:%s",
					tpInfo.i4Frequency, tpInfo.i4Symbolrate, tpInfo.ePol.name()));
		}
		return tpInfo;
	}
	
	public static void setTpHasModifiedForUpdateScan(boolean tpHasModified){
		mTpHasModified = tpHasModified;
	}

	public static String getDVBSTransponderStrTitle(boolean isUpdateScan,
			int satelliteID) {
		MtkTvScanTpInfo tpInfo = isUpdateScan ? getSavedBgmTPInfo(satelliteID) : getDVBSTransponder(satelliteID);
		return getDVBSTransponderStr(tpInfo);

	}

	public static String getDVBSTransponderStr(MtkTvScanTpInfo tpInfo) {
		if(tpInfo.ePol.name().equals("POL_LIN_VERTICAL")){
			return String.format("%dV%d", tpInfo.i4Frequency, tpInfo.i4Symbolrate);
      } else if(tpInfo.ePol.name().equals("POL_CIR_LEFT")){
          return String.format("%dL%d", tpInfo.i4Frequency, tpInfo.i4Symbolrate);
      } else if(tpInfo.ePol.name().equals("POL_CIR_RIGHT")) {
          return String.format("%dR%d", tpInfo.i4Frequency, tpInfo.i4Symbolrate);
		}else{
			return String.format("%dH%d", tpInfo.i4Frequency, tpInfo.i4Symbolrate);
		}
	}

	public void iResetDefault() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("iResetDefault()");
		if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
			for (int i = 1; i < 8; i++) {//total svl count is 7
				if (i == 3 || i == 4 || i == 7) {
					MtkTvChannelListBase.cleanChannelList(i, false);
				} else {
					MtkTvChannelListBase.cleanChannelList(i);
				}
			}
		} else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US
				|| MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA
				|| MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_CN) {
			for (int i = 1; i < 3; i++) {//total svl count is 2
				MtkTvChannelListBase.cleanChannelList(i);
			}
		}
		MtkTvConfig.getInstance().setConfigValue(
				MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
	}

	public void setDefaultChannel() {
        if (!CommonIntegration.getInstance().isCurrentSourceTv()) {
            return;
        }

        if (CommonIntegration.getInstance().getChannelAllNumByAPI() <= 0) {
            return;
        }

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDefaultChannel()");
		int index = 0;
		List<MtkTvChannelInfoBase> chList = CommonIntegration.getInstance()
				.getChList(0, 0, 3);
		if (chList != null && !chList.isEmpty()) {
			for (int i = 0; i < chList.size(); i++) {
				if ((chList.get(i).getNwMask() & MtkTvChCommonBase.SB_VNET_FAKE) != MtkTvChCommonBase.SB_VNET_FAKE
						&& (chList.get(i).getNwMask() & MtkTvChCommonBase.SB_VNET_VISIBLE) != MtkTvChCommonBase.SB_VNET_VISIBLE) {
					index = i;
					break;
				}
			}
			if (index < chList.size()) {
				int channelID = chList.get(index).getChannelId();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDefaultChannel(),ChannelID:" + channelID);
				CommonIntegration.getInstance().selectChannelById(channelID);
			}
		}
	}

	/*
	List<TVChannel> iGetChannelList() {
		return null;
	}
	*/
	int iGetChannelLength() {
		return 0;
	}


	public static int getFrequencyOperator(Context context, String scanMode) {
		return getFrequencyOperator(context, scanMode, getCurrentOperator());
	}

	// freq -2 disable
	// freq -1 auto
	// freq -3 empty
	public static int getFrequencyOperator(Context context, String scanMode,
			CableOperator operator) {
	    if(operator.equals(mLastDvbcOperator)){
	        int savedFreq = SaveValue.readWorldIntValue(context, "dvbc_network_freq", -1000);
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFrequencyOperator savedFreq:" + savedFreq);
	        if(savedFreq != -1000){
	            return savedFreq;
	        }
	    }
		int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig.getInstance().getCountry());
		int freq = getDVBCOperatorFrequency(countryID, operator);
		String advanceStr = context.getString(R.string.menu_arrays_Advance);
		String fullStr = context.getString(R.string.menu_arrays_Full);
		String quickStr = context.getString(R.string.menu_arrays_Quick);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(String.format(
				"getFrequencyOperator(),scanMode:%s,operator:%s,freq:%d",
				scanMode, operator.name(), freq));
		if (scanMode != null) {
			switch (countryID) {
			case CountrysIndex.CTY_15_Sweden_SWE:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)
							/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
						freq = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						freq = 362000;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_6_Denmark_DNK:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
					break;
				case GLENTEN:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
                    break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)
							/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
						freq = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						freq = 143000;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_13_Netherlands_NLD:
				switch (operator) {
				case UPC:
					/*if (scanMode.equalsIgnoreCase(advanceStr)) {
						freq = -3;
					}*/
					break;
				case Ziggo:
					if (scanMode.equalsIgnoreCase(advanceStr)) {
						freq = 474000;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(advanceStr)) {
						freq = 474000;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_3_Switzerland_CHE:
			case CountrysIndex.CTY_1_Austria_AUT:
			case CountrysIndex.CTY_19_Hungary_HUN:
			case CountrysIndex.CTY_21_Poland_POL:
			case CountrysIndex.CTY_20_Ireland_IRL:
				switch (operator) {
				case UPC:
					if (scanMode.equalsIgnoreCase(advanceStr)) {//DTV02196484
						freq = -3;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)
							/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
						freq = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_23_Romania_ROU:
				switch (operator) {
				case UPC:
					if (scanMode.equalsIgnoreCase(advanceStr)) {//DTV02196484
						freq = -3;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)
							/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
						freq = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_8_Finland_FIN:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					} else if (scanMode.equalsIgnoreCase(quickStr)) {
						freq = -1;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_14_Norway_NOR:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(advanceStr)) {
						freq = 386000;
					} else if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_5_Germany_DEU:
				switch (operator) {
				case Unitymedia:
					break;
				case TELECOLUMBUS:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						freq = -2;
					}
					break;
				case OTHER:
				    if (scanMode.equalsIgnoreCase(fullStr)) {
                        freq = -2;
                    }
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_9_France_FRA:
				switch (operator) {
				case Numericable:
					//freq = -3;
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)
							/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
						freq = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_2_Belgium_BEL:
				switch (operator) {
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)
							/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
						freq = -2;
					}
					break;
				default:
					break;
				}
				break;
			default:
				if (scanMode.equalsIgnoreCase(fullStr)
						/*|| scanMode.equalsIgnoreCase(quickStr)*/) {
					freq = -2;
				}
				break;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getFrequency: " + freq);
		return freq;
	}

	public static int getNetWorkIDOperator(Context context, String scanMode) {
		return getNetWorkIDOperator(context, scanMode, getCurrentOperator());
	}

	// netID -2 disable
	// netID -1 auto
	// netID -3 empty
	public static int getNetWorkIDOperator(Context context, String scanMode,
			CableOperator operator) {
	    if(operator.equals(mLastDvbcOperator)){
	        int savedNid = SaveValue.readWorldIntValue(context, "dvbc_network_id", -1000);
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNetWorkIDOperator savedNid:" + savedNid);
	        if(savedNid != -1000){
	            return savedNid;
	        }
	    }
		int countryID = CountrysIndex.reflectCountryStrToInt(MtkTvConfig
				.getInstance().getCountry());
		int netID = getDVBCOperatorNetWorkID(countryID, operator);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(String.format(
				"getNetWorkIDOperator(),scanMode:%s,operator:%s,netID:%d",
				scanMode, operator.name(), netID));
		String advanceStr = context
				.getString(R.string.menu_arrays_Advance);
		String fullStr = context.getString(R.string.menu_arrays_Full);
		String quickStr = context.getString(R.string.menu_arrays_Quick);

		if (scanMode != null) {
			switch (countryID) {
			case CountrysIndex.CTY_15_Sweden_SWE:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -1;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(quickStr)) {
						netID = -1;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_6_Denmark_DNK:
				switch (operator) {
				case Stofa:
					// case Yousee:
					netID = -1;
					break;
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -1;
					}
					break;
				case GLENTEN:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(quickStr)) {
						netID = -1;
					} else if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_13_Netherlands_NLD:
				switch (operator) {
				case UPC:
					/*if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -3;
					}*/
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_3_Switzerland_CHE:
			case CountrysIndex.CTY_1_Austria_AUT:
			case CountrysIndex.CTY_19_Hungary_HUN:
			case CountrysIndex.CTY_21_Poland_POL:
			case CountrysIndex.CTY_20_Ireland_IRL:
				switch (operator) {
				case UPC:
					if (scanMode.equalsIgnoreCase(advanceStr)) {//DTV02196484
						netID = -3;
					}
					break;
				case RCS_RDS:
					if(scanMode.equalsIgnoreCase(quickStr)){
						netID = -1;
					}
					break;	
				case OTHER:
					if (scanMode.equalsIgnoreCase(quickStr)) {
						netID = -1;
					} else if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_23_Romania_ROU:
				switch (operator) {
				case UPC:
					netID = -3;
					break;
				case RCS_RDS:
					netID = -1;
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(quickStr)) {
						netID = -1;
					} else if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_8_Finland_FIN:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -1;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(quickStr)) {
						netID = -1;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_14_Norway_NOR:
				switch (operator) {
				case Canal_Digital:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -1;
					}
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -1;
					} else if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_5_Germany_DEU:
				switch (operator) {
				case Unitymedia:
				   if (scanMode.equalsIgnoreCase(quickStr)) {
                        netID = -1;
                    }
					break;
				case TELECOLUMBUS:
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(quickStr)) {
						netID = -1;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_9_France_FRA:
				switch (operator) {
				case Numericable:
					//netID = -3;
					break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else if (scanMode.equalsIgnoreCase(advanceStr)) {
						netID = -1;
					} else {
						netID = -1;
					}
					break;
				default:
					break;
				}
				break;
			case CountrysIndex.CTY_2_Belgium_BEL:
				switch (operator) {
				case VOO://DTV02240515
				    if (scanMode.equalsIgnoreCase(advanceStr)){
				        netID = -3;
				    }
				    break;
				case OTHER:
					if (scanMode.equalsIgnoreCase(fullStr)) {
						netID = -2;
					} else {
						netID = -1;
					}
					break;
				default:
					break;
				}
				break;
				case CountrysIndex.CTY_24_Russia_RUS://DTV03041412 DTV03064270
					switch (operator) {
						case AKADO:
						case ONLIME:
						case TVOE_STP:
							if (scanMode.equalsIgnoreCase(advanceStr)){
								netID = -2;
							}
							break;
                                                case TVOE_EKA:
							if (scanMode.equalsIgnoreCase(quickStr)){
								netID = -2;
							}
							break;
						default:
							break;
					}
					break;
			default:
				if (scanMode.equalsIgnoreCase(fullStr)) {
					netID = -2;
				} else {
					netID = -1;
				}
				break;
			}
		}
		return netID;
	}

	/**
	 * @param countryID
	 * @param operator
	 * @return -1=auto
	 */
	private static int getDVBCOperatorFrequency(int countryID, CableOperator operator) {
		ScanDvbcCountryId mwCountry = reflectCountryIntToMWObject(countryID);
		ScanDvbcOperator mwOperator = reflectOperatorToMWObject(operator);
		int freq = -1;
		if (mwCountry == null || mwOperator == null) {
			freq = -1;
		} else {
			try {
				MtkTvScanDvbcBase base = new MtkTvScanDvbcBase();
				freq = base.getDefaultFrequency(mwCountry, mwOperator);
				freq = freq / 1000;
			} catch (Exception e) {
				freq = 101010;
			}
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "operator: " + operator.name() + ",getFrequency: " + freq);
		return freq;
	}

	/**
	 * @param countryID
	 * @param operator
	 * @return -1=auto, -2=""
	 */
	private static int getDVBCOperatorNetWorkID(int countryID, CableOperator operator) {

		ScanDvbcCountryId mwCountry = reflectCountryIntToMWObject(countryID);
		ScanDvbcOperator mwOperator = reflectOperatorToMWObject(operator);
		int netWorkID = -1;
		if (mwCountry == null || mwOperator == null) {
			netWorkID = -1;
		} else {

			try {
				MtkTvScanDvbcBase base = new MtkTvScanDvbcBase();
				netWorkID = base.getDefaultNetworkId(mwCountry, mwOperator);
			} catch (Exception e) {
				netWorkID = 123456;
			}

		}
		return netWorkID;
	}

	public static int getSystemRate() {
		CableOperator operator = getCurrentOperator();
		String countryStr = MtkTvConfig.getInstance().getCountry();
		int countryInt = CountrysIndex.reflectCountryStrToInt(countryStr);
		ScanDvbcCountryId mwCountry = reflectCountryIntToMWObject(countryInt);
		ScanDvbcOperator mwOperator = reflectOperatorToMWObject(operator);
		int sysRate = 6875;
		if (mwCountry == null || mwOperator == null) {
			sysRate = 6875;
		} else {
			try {
				MtkTvScanDvbcBase base = new MtkTvScanDvbcBase();
				sysRate = base.getDefaultSymbolRate(mwCountry, mwOperator);
				sysRate = sysRate / 1000;
			} catch (Exception e) {
				sysRate = 6875;
			}
		}
		return sysRate;
	}

	boolean isScanning() {
		return MtkTvScan.getInstance().isScanning();
	}

	private static ScanDvbcCountryId reflectCountryIntToMWObject(int index) {
		switch (index) {
		case CountrysIndex.CTY_1_Austria_AUT:
			return ScanDvbcCountryId.DVBC_COUNRY_AUT;
		case CountrysIndex.CTY_2_Belgium_BEL:
			return ScanDvbcCountryId.DVBC_COUNRY_BEL;
		case CountrysIndex.CTY_3_Switzerland_CHE:
			return ScanDvbcCountryId.DVBC_COUNRY_CHE;
		case CountrysIndex.CTY_4_Czech_Republic_CZE:
			return ScanDvbcCountryId.DVBC_COUNRY_CZE;
		case CountrysIndex.CTY_5_Germany_DEU:
			return ScanDvbcCountryId.DVBC_COUNRY_DEU;
		case CountrysIndex.CTY_6_Denmark_DNK:
			return ScanDvbcCountryId.DVBC_COUNRY_DNK;
		case CountrysIndex.CTY_7_Spain_ESP:
			return ScanDvbcCountryId.DVBC_COUNRY_ESP;
		case CountrysIndex.CTY_8_Finland_FIN:
			return ScanDvbcCountryId.DVBC_COUNRY_FIN;
		case CountrysIndex.CTY_9_France_FRA:
			return ScanDvbcCountryId.DVBC_COUNRY_FRA;
		case CountrysIndex.CTY_10_United_Kingdom_GBR:
			return ScanDvbcCountryId.DVBC_COUNRY_GBR;
		case CountrysIndex.CTY_11_Italy_ITA:
			return ScanDvbcCountryId.DVBC_COUNRY_ITA;
		case CountrysIndex.CTY_12_Luxembourg_LUX:
			return ScanDvbcCountryId.DVBC_COUNRY_LUX;
		case CountrysIndex.CTY_13_Netherlands_NLD:
			return ScanDvbcCountryId.DVBC_COUNRY_NLD;
		case CountrysIndex.CTY_14_Norway_NOR:
			return ScanDvbcCountryId.DVBC_COUNRY_NOR;
		case CountrysIndex.CTY_15_Sweden_SWE:
			return ScanDvbcCountryId.DVBC_COUNRY_SWE;
		case CountrysIndex.CTY_16_Bulgaria_BGR:
			return ScanDvbcCountryId.DVBC_COUNRY_BGR;
		case CountrysIndex.CTY_17_Croatia_HRV:
			return ScanDvbcCountryId.DVBC_COUNRY_HRV;
		case CountrysIndex.CTY_18_Greece_GRC:
			return ScanDvbcCountryId.DVBC_COUNRY_GRC;
		case CountrysIndex.CTY_19_Hungary_HUN:
			return ScanDvbcCountryId.DVBC_COUNRY_HUN;
		case CountrysIndex.CTY_20_Ireland_IRL:
			return ScanDvbcCountryId.DVBC_COUNRY_IRL;
		case CountrysIndex.CTY_21_Poland_POL:
			return ScanDvbcCountryId.DVBC_COUNRY_POL;
		case CountrysIndex.CTY_22_Portugal_PRT:
			return ScanDvbcCountryId.DVBC_COUNRY_PRT;
		case CountrysIndex.CTY_23_Romania_ROU:
			return ScanDvbcCountryId.DVBC_COUNRY_ROU;
		case CountrysIndex.CTY_24_Russia_RUS:
			return ScanDvbcCountryId.DVBC_COUNRY_RUS;
		case CountrysIndex.CTY_25_Serbia_SRB:
			return ScanDvbcCountryId.DVBC_COUNRY_SRB;
		case CountrysIndex.CTY_26_Slovakia_SVK:
			return ScanDvbcCountryId.DVBC_COUNRY_SVK;
		case CountrysIndex.CTY_27_Slovenia_SVN:
			return ScanDvbcCountryId.DVBC_COUNRY_SVN;
		case CountrysIndex.CTY_28_Turkey_TUR:
			return ScanDvbcCountryId.DVBC_COUNRY_TUR;
		case CountrysIndex.CTY_29_Estonia_EST:
			return ScanDvbcCountryId.DVBC_COUNRY_EST;

//		case CountrysIndex.CTY_30_Australia_AUS:
//		case CountrysIndex.CTY_31_New_Zealand_NZL:
		default:
			break;
		}

		return null;
	}

	private static ScanDvbcOperator reflectOperatorToMWObject(
			CableOperator operator) {
		switch (operator) {
		case UPC:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_UPC;
		case TELNET:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_TELENET;
		case OTHER:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_OTHERS;
		case NULL:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_OTHERS;
		case Unitymedia:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_UNITYMEDIA;
		case Stofa:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_STOFA;
		case Yousee:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_YOUSEE;
		case Canal_Digital:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_CANAL_DIGITAL;
		case Numericable:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_NUMERICABLE;
		case Ziggo:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_ZIGGO;
		case Comhem:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_COMHEM;
		case TELE2:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_TELE2;
		case VOLIA:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_VOLIA;
		case TELEMACH:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_TELEMACH;
		case ONLIME:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_ONLIME;
		case AKADO:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_AKADO;
		case TKT:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_TKT;
		case DIVAN_TV:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_DIVAN_TV;
		case NET1:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_NET1;
		case KDG:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_KDG;
		case KBW:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_KBW;
		case BLIZOO:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_BLIZOO;
		case GLENTEN:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_GLENTEN;
		case TELECOLUMBUS:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_TELECOLUMBUS;
		case RCS_RDS:
			return ScanDvbcOperator.DVBC_OPERATOR_NAME_RCS_RDS;
		case VOO:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_VOO;
		case KDG_HD:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_KDG_HD;
		case KRS:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_KRS;
		case TELEING:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_TELEING;
		case MTS:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_MTS;
		case TVOE_STP:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_TVOE_STP;
		case TVOE_EKA:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_TVOE_EKA;
		case TELEKOM:
            return ScanDvbcOperator.DVBC_OPERATOR_NAME_TELEKOM;
		case D_SMART:
		    return ScanDvbcOperator.DVBC_OPERATOR_NAME_DSMART;
		default:
		    return null;
		}
	}

	public TunerMode getTuneMode() {
		int tuneMode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("tuneMode?" + tuneMode);

		switch (tuneMode) {
		case MtkTvConfigType.BS_SRC_AIR:
			return TunerMode.DVBT;
		case MtkTvConfigType.BS_SRC_CABLE:
			return TunerMode.CABLE;
		case MtkTvConfigTypeBase.ACFG_BS_SRC_SAT://S
		case CommonIntegration.DB_GENERAL_SAT_OPTID://should be have no this value(3) when get tuner mode
			return TunerMode.DVBS;
		default:
			return TunerMode.DVBT;
		}
	}

	public static SatelliteInfo getDVBSsatellitesBySatID(Context context, int recID) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellitesBySatID(),recID:" + recID);
		List<SatelliteInfo> list = getDVBSsatellites(context);
		for (SatelliteInfo info : list) {
			if (info.getSatlRecId() == recID) {
				return info;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellitesBySatID(),info==null");
		return null;
	}
	public static SatelliteInfo getDVBSsatellitesBySatID(Context context, int recID, List<SatelliteInfo> list) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellitesBySatID(),recID:" + recID);
	    for (SatelliteInfo info : list) {
	        if (info.getSatlRecId() == recID) {
	            return info;
	        }
	    }
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellitesBySatID(),info==null");
	    return null;
	}

	public static List<SatelliteInfo> getDVBSEnablesatellites(Context context) {
        return getDVBSEnablesatellites(context, getDVBSsatellites(context));
    }
    
    public static List<SatelliteInfo> getDVBSEnablesatellites(Context context, List<SatelliteInfo> satelliteInfos) {
        List<SatelliteInfo> list = satelliteInfos;
        List<SatelliteInfo> enableList = new ArrayList<SatelliteInfo>();
        for (SatelliteInfo info : list) {
            if (info.getEnable()) {
                enableList.add(info);
            }
        }
        return enableList;
    }

	public static List<SatelliteInfo> getDVBSDisableSatellites(Context context) {
		List<SatelliteInfo> list = getDVBSsatellites(context);
		List<SatelliteInfo> disableList = new ArrayList<SatelliteInfo>();
		for (SatelliteInfo info : list) {
			if (!info.getEnable()) {
				disableList.add(info);
			}
		}
		return disableList;
	}

	public static List<SatelliteInfo> getDVBSsatellites(Context context) {
		int svlID = isCamScanUI ? CommonIntegration.DB_CI_PLUS_SVLID_SAT : CommonIntegration.getInstance().getSvl();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellites(), svlID="+svlID);
		return getDVBSsatellites(context, svlID);
	}

	public static List<SatelliteInfo> getDVBSsatellites(Context context, int svlID) {
		int antennaType = MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellites(),antennaType:" + antennaType+", svlID="+svlID);
		return filterSatsbyAntennaType(context, antennaType , svlID);
	}

	public static boolean isPreferedSat() {
		return CommonIntegration.getInstance().getSvl() == 4;
	}

	public static boolean isSatScan() {
		int tunerMode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
		return tunerMode >= CommonIntegration.DB_SAT_OPTID;
	}

	public static boolean isCurrentCable() {
    return CommonIntegration.DB_CAB_OPTID ==
        MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
  }

	public static boolean isATVScanFirst() {
		int value = MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigTypeBase.CFG_COUNTRY_ANLG_FIRST_COUNTRY);

		// value=1,ATV first; 0:DTV first.
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("isATVScanFirst(),FirstCountry:" + value);
		return value == 1;
	}

	private static List<SatelliteInfo> getDVBSALLsatellites(int svlID) {
		MtkTvDvbsConfigBase mSatl = new MtkTvDvbsConfigBase();

		int count = mSatl.getSatlNumRecs(svlID);
		List<MtkTvDvbsConfigInfoBase> allSatellite = new ArrayList<MtkTvDvbsConfigInfoBase>();
		for (int i = 0; i < count; i++) {
			List<MtkTvDvbsConfigInfoBase> list = mSatl.getSatlRecordByRecIdx(svlID, i);
			allSatellite.addAll(list);
		}

		List<SatelliteInfo> sates = new ArrayList<SatelliteInfo>();
		for (MtkTvDvbsConfigInfoBase tempInfoBase:allSatellite) {
			SatelliteInfo satInfo = new SatelliteInfo(tempInfoBase);
			sates.add(satInfo);
		}

		if(com.mediatek.wwtv.tvcenter.util.MtkLog.logOnFlag){
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellites()" + String.format("svlID:%d,count:%d,size:%d", svlID, count, allSatellite.size()));
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d("getDVBSsatellites(),List:" + allSatellite.toString());
		}

		return sates;
	}

	public static int getCountryIndex() {
		String countryStr = MtkTvConfig.getInstance().getCountry();
		return CountrysIndex.reflectCountryStrToInt(countryStr);
	}

	public static int getDefaultLnbFreqIndex(SatelliteInfo info, int[][] lnbConfigArray) {
		int lowFreq = info.getLnbLowFreq();
		int highFreq = info.getLnbHighFreq();
		int switchFreq = info.getLnbSwitchFreq();
		for (int i = 0; i < lnbConfigArray.length; i++) {
			if (lnbConfigArray[i][0] == lowFreq && lnbConfigArray[i][1] == highFreq
					&& lnbConfigArray[i][2] == switchFreq) {
				return i;
			}
		}
		int rows = SatDetailUI.LNB_CONFIG.length;
		SatDetailUI.LNB_CONFIG[rows - 1][0] = lowFreq;
		SatDetailUI.LNB_CONFIG[rows - 1][1] = highFreq;
		SatDetailUI.LNB_CONFIG[rows - 1][2] = switchFreq;
		return rows - 1;
	}

	/**
	 * prepare DVBS Scan parameter.
	 */
	public static SatelliteInfo updateDVBSSatInfo(Activity context,
			ListView listview, SatelliteInfo satInfo, boolean atMenu) {
		//boolean isDiseqc12 = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DISEQC12_IMPROVE);
		//com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfo for isDiseqc12!!" + isDiseqc12);
		//if (false/*isDiseqc12*/) {
		//	return updateDVBSSatInfoDiseqc12(context, listview, satInfo, atMenu);
		//}

		if(satInfo==null){
			return null;
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfo(),id:"+satInfo.getSatlRecId());
		int index = 0;

		if(listview==null||listview!=null&&listview.getCount()<=1){
            return null;
        }
		Action typeAction=(Action) listview.getItemAtPosition(index);
		int typeInt=0;
		if(typeAction!=null){
		    typeInt = typeAction.mInitValue;
		}
		if (atMenu) {
			++index; // skip Sat Name.
		}
		int lNBPowerInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
		int antennaType = MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE);
		boolean isSingleCable = antennaType == 1 ? true : false;
		int lnbFreqInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
		int diseqcInputIntPort = 0;
		int diseqcInputIntType = 0;
		int tone22KHZInt = 0;
		int toneBurstInt = 0;
		if (!isSingleCable) {
			Action diseqcItem = (Action) listview.getItemAtPosition(++index);
			diseqcInputIntPort = diseqcItem.mInitValue;
			if (diseqcInputIntPort == diseqcItem.mOptionValue.length - 1) {//disable
				diseqcInputIntType = 0;
			} else {//Port is A/B/C/D
				diseqcInputIntType = 2; //is 4x1
			}
			tone22KHZInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
			toneBurstInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
		}

		SatelliteInfo info = satInfo;

		info.setEnabled(typeInt == 0 ? true : false);

		if (isSingleCable) {
			info.setPosition(lNBPowerInt);
		} else {
			info.setDiseqcType(diseqcInputIntType);
			info.setPort(diseqcInputIntPort);
			info.setLnbPower(lNBPowerInt);
			info.setToneBurst(toneBurstInt);
			info.setM22k(tone22KHZInt);
		}
		if (lnbFreqInt < LNB_CONFIG.length) {
			info.setLnbLowFreq(LNB_CONFIG[lnbFreqInt][0]);
			info.setLnbHighFreq(LNB_CONFIG[lnbFreqInt][1]);
			info.setLnbSwitchFreq(LNB_CONFIG[lnbFreqInt][2]);
			if (lnbFreqInt > 2) {
				info.setLnbType(LNB_SINGLE_FREQ);
			} else {
				info.setLnbType(LNB_DUAL_FREQ);
			}
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfo>>" + info.toString());
		return info;
	}

	/**
	 * prepare DVBS Scan parameter diseqc1.2.
	 */
	public static SatelliteInfo updateDVBSSatInfoDiseqc12(Activity context,
			ListView listview, SatelliteInfo satInfo, boolean atMenu) {

		if(satInfo == null){
			return null;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfoDiseqc12(),id:"+satInfo.getSatlRecId());
		int index = 0;
		if(listview==null||listview != null && listview.getCount() <= 1){
            return null;
        }
		Action typeAction=(Action) listview.getItemAtPosition(index);
		int typeInt=0;
		if(typeAction!=null){
		     typeInt = typeAction.mInitValue;
		}
		if (atMenu) {
			++index; // skip Sat Name.
		}
		int lNBPowerInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
		int antennaType = MtkTvConfig.getInstance().getConfigValue(SAT_ANTENNA_TYPE);
		boolean isSingleCable = antennaType == 1 ? true : false;
		int lNBFreqInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
		int tone22KHZInt = 0;
		if (!isSingleCable) {
			++index;
			tone22KHZInt = ((Action) listview.getItemAtPosition(++index)).mInitValue;
		}
		SatelliteInfo info = satInfo;
		info.setEnabled(typeInt == 0 ? true : false);
		if (isSingleCable) {
			info.setPosition(lNBPowerInt);
		} else {
			info.setLnbPower(lNBPowerInt);
			info.setM22k(tone22KHZInt);
		}
		if (lNBFreqInt < LNB_CONFIG.length) {
			info.setLnbLowFreq(LNB_CONFIG[lNBFreqInt][0]);
			info.setLnbHighFreq(LNB_CONFIG[lNBFreqInt][1]);
			info.setLnbSwitchFreq(LNB_CONFIG[lNBFreqInt][2]);
			if (lNBFreqInt > 2) {
				info.setLnbType(LNB_SINGLE_FREQ);
			} else {
				info.setLnbType(LNB_DUAL_FREQ);
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfoDiseqc12>>" + info.toString());
		return info;
	}

	/**
	 * prepare DVBS Scan diseqc 1.2 set  parameter
	 */
	public static SatelliteInfo updateDVBSSatInfoDiseqcSet12(ListView listview, SatelliteInfo satInfo) {
		if(listview==null || listview != null && listview.getCount() <= 1){
			return null;
		}
		if(satInfo == null){
			return null;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfoDiseqcSet12(),id:"+satInfo.getSatlRecId());
		int index = 0;
		Action diseqc10Item = (Action) listview.getItemAtPosition(index);
		if(diseqc10Item == null || diseqc10Item.mOptionValue == null) {
		    return satInfo;
		}
		int diseqc10Port = diseqc10Item.mInitValue;
		int diseqcInputIntType = 0;
		int toneBrust = 2;//disable
		if (diseqc10Port > 3) {//disable
			diseqcInputIntType = 0;
			if (diseqc10Port == diseqc10Item.mOptionValue.length - 1) {
				toneBrust = 2;//disable
			} else if (diseqc10Port == diseqc10Item.mOptionValue.length - 2){
				toneBrust = 1;//B
			}else if (diseqc10Port == diseqc10Item.mOptionValue.length - 3){
				toneBrust = 0;//A
			}
			diseqc10Port = 0;
			satInfo.setToneBurst(toneBrust);
		} else {//Port is A/B/C/D
			diseqcInputIntType = 2; //is 4x1
			if (diseqc10Port == 0 || diseqc10Port == 2) {
				toneBrust = 0;
			} else {
				toneBrust = 1;
			}

		}

		Action diseqc11Item = (Action) listview.getItemAtPosition(++index);
		int diseqc11Port = diseqc11Item.mInitValue;
		int diseqc11InputIntType = 0;
		if (diseqc11Port == diseqc11Item.mOptionValue.length - 1) {
			diseqc11InputIntType = 0;
		} else {
			diseqc11InputIntType = 2;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfoDiseqcSet12>diseqcInputIntType>" + diseqcInputIntType
				+ ">>>" + diseqc10Port + ">>>" + toneBrust + ">>>" + diseqc11InputIntType + ">>>" + diseqc11Port);
		satInfo.setDiseqcType(diseqcInputIntType);
		satInfo.setPort(diseqc10Port);
		satInfo.setDiseqcTypeEx(diseqc11InputIntType);
		satInfo.setPortEx(diseqc11Port);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("updateDVBSSatInfoDiseqcSet12>>" + satInfo.toString());
		return satInfo;
	}

	/**
	 * tune DVBS service to get signal quality and signal level
	 * @param satID tune satellite id
	 */
	public static void setDVBSFreqToGetSignalQualityFromLocal(int satID, MtkTvScanTpInfo tpInfo){
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSFreqToGetSignalQualityFromLocal(),satID:"+satID);
		int frequency = tpInfo.i4Frequency;
		int symRate = tpInfo.i4Symbolrate;

		int conType = 3; // MtkTvFreqChgCommonBase.CON_SRC_TYPE_SAT
		int freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_SAT;
		int tunerMod = MtkTvFreqChgCommonBase.MOD_UNKNOWN; //Not use

		MtkTvFreqChgParamBase freqInfo = new MtkTvFreqChgParamBase(conType,
				freqType, frequency, tunerMod, symRate);

		int satPol = Arrays.asList(TunerPolarizationType.values()).indexOf(tpInfo.ePol);

		int satLstId = CommonIntegration.getInstance().getSvl();
		int satLstRecId = satID;

		freqInfo.setSatPol(satPol);
		freqInfo.setSatLstId(satLstId);
		freqInfo.setSatLstRecId(satLstRecId);

		//MtkTvAppTVBase apptv = new MtkTvAppTVBase();
		MtkTvBroadcast.getInstance().changeFreq(CommonIntegration.getInstance().getCurrentFocus(), freqInfo);
	}

	/**
	 * tune DVBS service to get signal quality and signal level
	 * @param satID tune satellite id
	 */
	public static void setDVBSFreqToGetSignalQuality(int satID){
		MtkTvScanTpInfo tpInfo = ScanContent.getDVBSTransponder(satID);
		int frequency = tpInfo.i4Frequency;
		int symRate = tpInfo.i4Symbolrate;
		int satPol = Arrays.asList(TunerPolarizationType.values()).indexOf(tpInfo.ePol);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("setDVBSFreqToGetSignalQuality(),satID:"+satID + "  frequency:" + frequency + "  symRate:" + symRate + "  satPol:" + satPol);

		int conType = 3; // MtkTvFreqChgCommonBase.CON_SRC_TYPE_SAT
		int freqType = MtkTvFreqChgCommonBase.FREQ_TYPE_DIG_SAT;
		int tunerMod = MtkTvFreqChgCommonBase.MOD_UNKNOWN; //Not use

		MtkTvFreqChgParamBase freqInfo = new MtkTvFreqChgParamBase(conType,
				freqType, frequency, tunerMod, symRate);

		int satLstId = isCamScanUI ? CommonIntegration.DB_CI_PLUS_SVLID_SAT : CommonIntegration.getInstance().getSvl();
		int satLstRecId = satID;

		freqInfo.setSatPol(satPol);
		freqInfo.setSatLstId(satLstId);
		freqInfo.setSatLstRecId(satLstRecId);

		//MtkTvAppTVBase apptv = new MtkTvAppTVBase();
		TVAsyncExecutor.getInstance().execute(new Runnable() {
		    @Override
		    public void run() {
		        MtkTvBroadcast.getInstance().changeFreq(CommonIntegration.getInstance().getCurrentFocus(), freqInfo);
		    }
		});
	}

	public static String getCurrentSourceFocus() {
		String focusWin = "";
		int result = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN);
		if (0 == result) {
			focusWin = "main";
		} else if (1 == result) {
			focusWin = "sub";
		}
		return focusWin;
	}

	public static int getSignalLevel() {
		int level = MtkTvBroadcast.getInstance().getSignalLevel();
		if (level < 0) {
			level = 0;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getSignalLevel():"+level);
		return level;
	}

	public static int getSignalQuality() {
		int ber = 0;
		ber = MtkTvBroadcast.getInstance().getSignalQuality();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("getSignalQuality():"+ber);
		return ber;
	}

	private static int dvbsCurrOperator =-1;
	public static void setDVBSCurroperator(int op){
		dvbsCurrOperator = op;
	}

	public static int getDVBSCurroperator(){
		return dvbsCurrOperator;
	}
	public static boolean isSatOpHDAustria() {
        return dvbsCurrOperator == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_AUSTRIASAT;
    }

    public static boolean isSatOpDiveo() {
        return dvbsCurrOperator == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DEUTSCHLAND;
    }
    
    public static boolean isDSmartOp() {
        return MtkTvConfig.getInstance().getConfigValue(
                MtkTvConfigType.CFG_BS_BS_CABLE_BRDCSTER) == 32;
    }
    
    public static boolean isDVBSForRCSRDSOp() {
        return getDVBSCurrentOP() == 111 || getDVBSCurrentOP() == 112;
    }
    
    public static boolean isDVBSForDSmartOp() {
        return getDVBSCurrentOP() == 20;
    }

    public static boolean isCanalDigitalOp() {
        return getCurrentOperator().equals(CableOperator.Canal_Digital);
    }

    public static boolean isDVBSForTivusatOp() {
        return getDVBSCurrentOP() == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TIVU_SAT;
    }
    
    public static boolean isSatOpFastScan() {
        return MtkTvConfig.getInstance().getConfigValue(SAT_BRDCSTER) == 49;//MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_M7_FAST_SCAN;
    }

    public static boolean checkFastScanOrbitsError(MtkTvScanDvbsBase.M7OneOPT opt, String optName, List<SatelliteInfo> enabledSatellites){
		int orbitNum = opt.orbitNum;
		int[] orbits = opt.orbitInfo;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"checkFastScanOrbits(): "+orbitNum);
		if(orbitNum == 0 || enabledSatellites == null || enabledSatellites.isEmpty()){
			return false;
		}
		List<Integer> satelliteOrbits = new ArrayList<>();
		for(SatelliteInfo info : enabledSatellites){
			satelliteOrbits.add(info.getOrbPos());
		}
		for(int i = 0; i < orbitNum; i++){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"checkFastScanOrbits(): orbit="+orbits[i]);
			if(satelliteOrbits.contains(orbits[i])){
				return false;
			}
		}
		return true;
	}

	public static String getFastScanOrbitsErrorMsg(Context context, MtkTvScanDvbsBase.M7OneOPT opt, String optName){
		switch (opt.orbitNum){
			case 1:
				return context.getResources().getQuantityString(R.plurals.dvbs_fast_orbits_error_msg, opt.orbitNum,
						optName, convertToSatNameByOrbit(opt.orbitInfo[0]));
			case 2:
				return context.getResources().getQuantityString(R.plurals.dvbs_fast_orbits_error_msg, opt.orbitNum,
								optName, convertToSatNameByOrbit(opt.orbitInfo[0]), convertToSatNameByOrbit(opt.orbitInfo[1]));
			case 3:
				return context.getResources().getQuantityString(R.plurals.dvbs_fast_orbits_error_msg, opt.orbitNum,
								optName, convertToSatNameByOrbit(opt.orbitInfo[0])+"," +
								convertToSatNameByOrbit(opt.orbitInfo[1]), convertToSatNameByOrbit(opt.orbitInfo[2]));
			case 4:
				return context.getResources().getQuantityString(R.plurals.dvbs_fast_orbits_error_msg, opt.orbitNum,
								optName, convertToSatNameByOrbit(opt.orbitInfo[0])+"," +
								convertToSatNameByOrbit(opt.orbitInfo[1])+"," + convertToSatNameByOrbit(opt.orbitInfo[2]),
								convertToSatNameByOrbit(opt.orbitInfo[3]));
			default:
				return null;
		}
	}

	public static String convertToSatNameByOrbit(int orbit) {
		String sufix = "E";
		if(orbit < 0){
			sufix = "W";
			orbit = -orbit;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(orbit / 10);
		if(orbit % 10 != 0) {
			sb.append(".").append(orbit % 10);
		}
		return sb.append(sufix).toString();
	}

	public static LiveTVDialog getDvbsFastScanOrbitErrorDialog(Context mContext, MtkTvScanDvbsBase.M7OneOPT oneOPT, String optName) {
		LiveTVDialog dialog = new LiveTVDialog(mContext, 7);
		dialog.setButtonNoName(mContext.getString(android.R.string.ok));
		dialog.setMessage(ScanContent.getFastScanOrbitsErrorMsg(mContext,oneOPT,optName));
		View.OnKeyListener listener = new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER
							|| keyCode == KeyMap.KEYCODE_MTKIR_RED) {
						if (v.getId() == dialog.getButtonNo().getId()) {
							if(mContext instanceof ScanViewActivity){
								((ScanViewActivity) mContext).finish();
							}else {
								Intent intent = new Intent(mContext, SatActivity.class);
								intent.putExtra("mItemId", MenuConfigManager.DVBS_SAT_UPDATE_SCAN);
								intent.putExtra("title", mContext.getString(R.string.menu_s_sate_update));
								intent.putExtra("selectPos", -1);
								mContext.startActivity(intent);
							}
							dialog.dismiss();
						}
						return true;
					}else if(keyCode == KeyEvent.KEYCODE_BACK){
						dialog.dismiss();
						return true;
					}
				}
				return false;
			}
		};
		dialog.bindKeyListener(listener);
		return dialog;
	}
}
