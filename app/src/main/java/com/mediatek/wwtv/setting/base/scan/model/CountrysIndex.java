package com.mediatek.wwtv.setting.base.scan.model;

import java.lang.reflect.Field;




public class CountrysIndex {
	public final static String TAG = "CountrysIndex";
	public final static int CTY_1_Austria_AUT = 1;
	public final static int CTY_2_Belgium_BEL = 2;
	public final static int CTY_3_Switzerland_CHE = 3;
	public final static int CTY_4_Czech_Republic_CZE = 4;
	public final static int CTY_5_Germany_DEU = 5;
	public final static int CTY_6_Denmark_DNK = 6;
	public final static int CTY_7_Spain_ESP = 7;
	public final static int CTY_8_Finland_FIN = 8;
	public final static int CTY_9_France_FRA = 9;
	public final static int CTY_10_United_Kingdom_GBR = 10;
	public final static int CTY_11_Italy_ITA = 11;
	public final static int CTY_12_Luxembourg_LUX = 12;
	public final static int CTY_13_Netherlands_NLD = 13;
	public final static int CTY_14_Norway_NOR = 14;
	public final static int CTY_15_Sweden_SWE = 15;
	public final static int CTY_16_Bulgaria_BGR = 16;
	public final static int CTY_17_Croatia_HRV = 17;
	public final static int CTY_18_Greece_GRC = 18;
	public final static int CTY_19_Hungary_HUN = 19;
	public final static int CTY_20_Ireland_IRL = 20;
	public final static int CTY_21_Poland_POL = 21;
	public final static int CTY_22_Portugal_PRT = 22;
	public final static int CTY_23_Romania_ROU = 23;
	public final static int CTY_24_Russia_RUS = 24;
	public final static int CTY_25_Serbia_SRB = 25;
	public final static int CTY_26_Slovakia_SVK = 26;
	public final static int CTY_27_Slovenia_SVN = 27;
	public final static int CTY_28_Turkey_TUR = 28;
	public final static int CTY_29_Estonia_EST = 29;
	public final static int CTY_30_Australia_AUS = 30; //Oceania country
	public final static int CTY_31_New_Zealand_NZL = 31;
	public final static int CTY_32_Indonesia_IDN = 32;
	public final static int ANLG_ONLY_XAP_CTY_2_Philippines_PHL = 33;
	public final static int ANLG_ONLY_XAP_CTY_3_CIS_CIS = 34;
	public final static int ANLG_ONLY_XAP_CTY_4_Others = 35;
	public final static int CTY_36_Ukraine_UKR = 36;
	public final static int ANLG_ONLY_XAP_CTY_1_India_IND = 37;

	public static int reflectCountryStrToInt(String counrtyStr) {
		int countryIndex=-1;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("reflectCountryStrToInt(),counrtyStr:"+counrtyStr);
		try {
			CountrysIndex countryObject = new CountrysIndex();

			Object o = countryObject.getClass().newInstance();
			Class<?> c = countryObject.getClass();

			Field[] fileds = c.getDeclaredFields();

			for (Field f : fileds) {
				String paramName = f.getName();

				paramName = paramName.substring(paramName.lastIndexOf("_") + 1,
						paramName.length());

				if (paramName.equalsIgnoreCase(counrtyStr)) {
					int value = 0;
					value = f.getInt(o);
					countryIndex=value;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("reflectCountryStrToInt(),countryIndex: "+countryIndex);
		return countryIndex;
	}
	
	@Override
	public String toString() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("toString()");
	    return super.toString();
	}


}
