package com.mediatek.wwtv.setting.base.scan.model;



public enum CableOperator {
	UPC,
	TELNET,
	OTHER,
	NULL,
	Unitymedia,
	Stofa,
	Yousee,
	Canal_Digital,
	Numericable,
	Ziggo,
	Comhem,
	TELE2,
	VOLIA,
	TELEMACH,
	ONLIME,
	AKADO,
	TKT,
	DIVAN_TV,
	NET1,
	KDG,
	KBW,
	BLIZOO,
	GLENTEN,
	TELECOLUMBUS,
	RCS_RDS,
    VOO,
    KDG_HD,
    KRS,
    TELEING,
    MTS,
    TVOE_STP,
    TVOE_EKA,
    TELEKOM,
    D_SMART;

	/**
	 * please refer to String-Arrays's "dvbc_operators"
	 */
	private static final int UPC_index = 0;
	private static final int TELNET_index = 1;
	private static final int Unitymedia_index = 2;
	private static final int Stofa_index = 3;
	private static final int Yousee_index = 4;
	private static final int Canal_Digital_index = 5;
	private static final int Numericable_index = 6;
	private static final int Ziggo_index = 7;
	private static final int Comhem_index = 8;
	private static final int OTHER_index = 9;

	//

	public static final int DVBC_OPERATOR_NAME_OTHERS = 0;
	public static final int DVBC_OPERATOR_NAME_UPC             = 1;
	public static final int DVBC_OPERATOR_NAME_COMHEM          = 2;
	public static final int DVBC_OPERATOR_NAME_CANAL_DIGITAL   = 3;
	public static final int DVBC_OPERATOR_NAME_TELE2           = 4;
	public static final int DVBC_OPERATOR_NAME_STOFA           = 5;
	public static final int DVBC_OPERATOR_NAME_YOUSEE          = 6;
	public static final int DVBC_OPERATOR_NAME_ZIGGO           = 7;
	public static final int DVBC_OPERATOR_NAME_UNITYMEDIA      = 8;
	public static final int DVBC_OPERATOR_NAME_NUMERICABLE     = 9;
	public static final int DVBC_OPERATOR_NAME_VOLIA           = 10; /* Ukraine */
	public static final int DVBC_OPERATOR_NAME_TELEMACH        = 11; /* Slovenia */
	public static final int DVBC_OPERATOR_NAME_ONLIME          = 12; /* Russia */
	public static final int DVBC_OPERATOR_NAME_AKADO           = 13; /* Russia */
	public static final int DVBC_OPERATOR_NAME_TKT             = 14; /* Russia */
	public static final int DVBC_OPERATOR_NAME_DIVAN_TV        = 15; /* Russia */
	public static final int DVBC_OPERATOR_NAME_NET1            = 16; /* Bulgaria */
	public static final int DVBC_OPERATOR_NAME_KDG             = 17; /* Germany */
	public static final int DVBC_OPERATOR_NAME_KBW             = 18; /* Germany */
	public static final int DVBC_OPERATOR_NAME_BLIZOO          = 19; /* Bulgaria */
	public static final int DVBC_OPERATOR_NAME_TELENET         = 20; /* Belgium */
	public static final int DVBC_OPERATOR_NAME_GLENTEN         = 21; /* Denmark */
	public static final int DVBC_OPERATOR_NAME_TELECOLUMBUS    = 22; /* Germany */
	public static final int DVBC_OPERATOR_NAME_RCS_RDS         = 23; /* Romania */
	public static final int DVBC_OPERATOR_NAME_VOO             = 24; /* Belgium */
	public static final int DVBC_OPERATOR_NAME_KDG_HD          = 25; /* Belgium */
    public static final int DVBC_OPERATOR_NAME_KRS             = 26; /* Slovenia */
	public static final int DVBC_OPERATOR_NAME_TELEING         = 27; /* Slovenia */
	public static final int DVBC_OPERATOR_NAME_MTS             = 28; /* Russia */
	public static final int DVBC_OPERATOR_NAME_TVOE_STP        = 29; /* Russia Rostelecom-St.Petersburg (Tvoe TV) */
	public static final int DVBC_OPERATOR_NAME_TVOE_EKA        = 30; /* Russia Rostelecom-Ekaterinburg (Tvoe TV) */
	public static final int DVBC_OPERATOR_NAME_TELEKOM         = 31; /* Hungary */
	public static final int DVBC_OPERATOR_NAME_DSMART         = 32; /* Hungary */

	/**
	 * After TV-API complete the operator define, will implement this method.
	 *
	 * @param index
	 * @return
	 */
	public static CableOperator getOperatorFromIndex(int operatorIndex) {
	     com.mediatek.wwtv.tvcenter.util.MtkLog.d("getOperatorFromIndex():" + "index: " + operatorIndex);
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
	            default:
	                operator = CableOperator.OTHER;
	                break;
	            }
	        return operator;
	}
}