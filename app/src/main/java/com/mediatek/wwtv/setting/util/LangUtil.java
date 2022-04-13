package com.mediatek.wwtv.setting.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.setting.base.scan.model.CountrysIndex;
/**
 * this is used for get cable operrater and osd language set
 *
 *
 */
public abstract class LangUtil {
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
		default:
			operator = CableOperator.OTHER;
			break;
		}
		return operator;
	}

	public static int getCountryIndex() {
		String countryStr = MtkTvConfig.getInstance().getCountry();
		return CountrysIndex.reflectCountryStrToInt(countryStr);
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

	public static List<String> getRegionEULanguageCodeList() {
		return new ArrayList<String>(
				Arrays.asList(new String[] { "en", "eu", "ca", "hr", "cs",
						"da", "nl", "fi", "fr", "gd", "gl", "de", "hu", "it",
						"no", "pl", "pt", "ro", "sr", "sk", "sl", "es", "sv",
						"tr", "cy", "et", "ru" }));
	}
}
