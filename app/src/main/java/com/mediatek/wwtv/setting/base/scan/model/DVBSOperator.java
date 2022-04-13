package com.mediatek.wwtv.setting.base.scan.model;

import android.content.Context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;

public class DVBSOperator {
  private Map<String,Integer> operators = new HashMap<String,Integer>();
  public DVBSOperator(Context context) {
    operators.put(context.getString(R.string.dvbs_operator_others),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_OTHERS);
    operators.put(context.getString(R.string.dvbs_operator_astra_hd_plus),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ASTRA_HD_PLUS);
    operators.put(context.getString(R.string.dvbs_operator_sky_deutschland),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SKY_DEUTSCHLAND);
    operators.put(context.getString(R.string.dvbs_operator_austriasat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_AUSTRIASAT);
    operators.put(context.getString(R.string.dvbs_operator_cannaldigitaal),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANALDIGITAAL_HD);
    operators.put(context.getString(R.string.dvbs_operator_cannaldigitaal_sd),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANALDIGITAAL_SD);
    operators.put(context.getString(R.string.dvbs_operator_tv_vlaanderen),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TV_VLAANDEREN_HD);
    operators.put(context.getString(R.string.dvbs_operator_tv_vlaanderen_sd),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TV_VLAANDEREN_SD);
    operators.put(context.getString(R.string.dvbs_operator_seznam_kanalu_pro_cesko),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SYKLINK_CZ);
    operators.put(context.getString(R.string.dvbs_operator_seznam_kanalu_pro_slovensko),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SYKLINK_SK);
    operators.put(context.getString(R.string.dvbs_operator_ors),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ORS);
    operators.put(context.getString(R.string.dvbs_operator_telesat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELESAT_BELGIUM);
    operators.put(context.getString(R.string.dvbs_operator_telesat_luxembourg),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELESAT_LUXEMBOURG);
    operators.put(context.getString(R.string.dvbs_operator_cannal_digital),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CANAL_DIGITAL);
    operators.put(context.getString(R.string.dvbs_operator_n_na_karte),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NNK);
    operators.put(context.getString(R.string.dvbs_operator_digiturk_turksat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_TURKSAT);
    operators.put(context.getString(R.string.dvbs_operator_digiturk_eutelsat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGITURK_EUTELSAT);
    operators.put(context.getString(R.string.dvbs_operator_fransat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FRANSAT);
    operators.put(context.getString(R.string.dvbs_operator_cyfraplus),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CYFRA_PLUS);
    operators.put(context.getString(R.string.dvbs_operator_cyfrowy_polsat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_CYFROWY_POLSAT);
    operators.put(context.getString(R.string.dvbs_operator_dsmart),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_D_SMART);
    operators.put(context.getString(R.string.dvbs_operator_ntvplus),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS);
    operators.put(context.getString(R.string.dvbs_operator_astra_international_lcn),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_ASTRA_INTERNATIONAL_LCN);
    operators.put(context.getString(R.string.dvbs_operator_name_smart_hd_plus),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SMART_HD_PLUS);
    operators.put(context.getString(R.string.dvbs_operator_name_nc_plus),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NC_PLUS);
    operators.put(context.getString(R.string.dvbs_operator_name_tivusat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TIVU_SAT);
    operators.put(context.getString(R.string.dvbs_operator_name_turksat_hello),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_HELLO);
    operators.put(context.getString(R.string.dvbs_operator_name_tkgs), 
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TKGS);
    operators.put(context.getString(R.string.dvbs_operator_name_freeview),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREEVIEW_SAT);
    operators.put(context.getString(R.string.dvbs_operator_name_digi),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGI_TV);
    operators.put(context.getString(R.string.dvbs_operator_name_diveo),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DEUTSCHLAND);
    operators.put(context.getString(R.string.dvbs_operator_name_tivibu),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TIVIBU);
    operators.put(context.getString(R.string.dvbs_operator_name_tricolor),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR);
    operators.put(context.getString(R.string.dvbs_operator_name_simpliTV),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SIMPLITV);
    operators.put(context.getString(R.string.dvbs_operator_name_telekarta),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA);
    operators.put(context.getString(R.string.dvbs_operator_name_white_label_platform_lcn),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_WHITE_LABEL_PLATFORM_LCN);

    /* For Digi TV */
    operators.put(context.getString(R.string.dvbs_operator_name_digi_tv_cze),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGI_TV_CZE);
    operators.put(context.getString(R.string.dvbs_operator_name_digi_tv_svk),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_DIGI_TV_SVK);
    
    operators.put(context.getString(R.string.dvbs_operator_name_joyne_nld),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_JOYNE_NLD);
    operators.put(context.getString(R.string.dvbs_operator_name_joyne_bel),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_JOYNE_BEL);
    operators.put(context.getString(R.string.dvbs_operator_name_upc_direct),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_UPC_DIRECT);
    operators.put(context.getString(R.string.dvbs_operator_name_upc_direct_sd),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_UPC_DIRECT_SD);
    operators.put(context.getString(R.string.dvbs_operator_name_focussat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FOCUSSAT);
    operators.put(context.getString(R.string.dvbs_operator_name_focussat_sd),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FOCUSSAT_SD);
    
    operators.put(context.getString(R.string.dvbs_operator_name_freesat),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT);
    operators.put(context.getString(R.string.dvbs_operator_name_freesat_cz),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_CZ);
    operators.put(context.getString(R.string.dvbs_operator_name_freesat_sk),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_SK);
    operators.put(context.getString(R.string.dvbs_operator_name_freesat_cz_sd),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_CZ_SD);
    operators.put(context.getString(R.string.dvbs_operator_name_freesat_sk_sd),
                                    MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FREESAT_SK_SD);
    operators.put(context.getString(R.string.dvbs_operator_ntvplus_56E),
            MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_56E);
    operators.put(context.getString(R.string.dvbs_operator_ntvplus_140E),
            MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_NTV_PLUS_140E);
    operators.put(context.getString(R.string.dvbs_operator_name_tricolor_siberia),
            MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR_SIBERIA);
    operators.put(context.getString(R.string.dvbs_operator_name_tricolor_far_east),
            MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TRICOLOR_FAR_EAST);
    operators.put(context.getString(R.string.dvbs_operator_name_telekarta_140E),
            MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_TELEKARTA_140E);
    operators.put(context.getString(R.string.dvbs_operator_name_m7_fast_scan),
            MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_M7_FAST_SCAN);
  }

  public int getOperatorByName(String name) {
    return operators.get(name).intValue();
  }

  public String getNameByOperator(int id) {
    String result = "OTHERS";
    Iterator<Entry<String, Integer>> iterator = operators.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Integer> entry = iterator.next();
      if(entry.getValue() == id) {
        result = entry.getKey();
        break;
      }
    }
    return result;
  }
}
