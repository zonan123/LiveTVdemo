/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tv.util;

import android.content.ComponentName;
import android.content.Context;
import android.media.tv.TvInputInfo;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

/** A class that includes convenience methods for accessing TvProvider database. */
public final class Utils {
    public String toString() {
        return "Utils";
    }

    /**
     * Check if the index is valid for the collection,
     *
     * @param collection the collection
     * @param index the index position to test
     * @return index >= 0 && index < collection.size().
     */
    public static boolean isIndexValid(Collection<?> collection, int index) {
        return collection != null && (index >= 0 && index < collection.size());
    }

    /**
     * Returns an {@link String#intern() interned} string or null if the input is null.
     */
    public static String intern(String string) {
        return string == null ? null : string.intern();
    }

    /**
     * Returns the label for a given input. Returns the custom label, if any.
     */
    public static String loadLabel(Context context, TvInputInfo input) {
        if (input == null) {
            return null;
        }
        CharSequence customLabel = input.loadCustomLabel(context);
        String label = (customLabel == null) ? null : customLabel.toString();
        if (TextUtils.isEmpty(label)) {
            label = input.loadLabel(context).toString();
        }
        return label;
    }

    /**
     * Converts time in milliseconds to a String.
     *
     * @param fullFormat {@code true} for returning date string with a full format
     *                   (e.g., Mon Aug 15 20:08:35 GMT 2016). {@code false} for a short format,
     *                   {e.g., [8/15/16] 8:08 AM}, in which date information would only appears
     *                   when the target time is not today.
     */
    public static String toTimeString(long timeMillis, boolean fullFormat) {
        if (fullFormat) {
            return new Date(timeMillis).toString();
        } else {
            return (String) DateUtils.formatSameDayTime(timeMillis, System.currentTimeMillis(),
                    SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
        }
    }

    /**
     * Converts time in milliseconds to a String.
     */
    public static String toTimeString(long timeMillis) {
        return toTimeString(timeMillis, true);
    }

    public static String buildSelectionForIds(String idName, List<Long> ids) {
      StringBuilder sb = new StringBuilder();
      sb.append(idName).append(" in (")
              .append(ids.get(0));
      for (int i = 1; i < ids.size(); ++i) {
          sb.append(",").append(ids.get(i));
      }
      sb.append(")");
      return sb.toString();
    }

    /** Checks whether the input is internal or not. */
    public static boolean isInternalTvInput(Context context, String inputId) {
        ComponentName unflattenInputId = ComponentName.unflattenFromString(inputId);
        if (unflattenInputId == null) {
            return false;
        }
        return context.getPackageName()
                .equals(unflattenInputId.getPackageName());
    }

	public static String convertConurty(String conurty){
        String newCountry=null;
        if(MtkTvConfigTypeBase.S3166_CFG_COUNT_AUS.equalsIgnoreCase(conurty)){
        	newCountry="AU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_BEL.equalsIgnoreCase(conurty)){
        	newCountry="BE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_CHE.equalsIgnoreCase(conurty)){
        	newCountry="CH";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_CZE.equalsIgnoreCase(conurty)){
        	newCountry="CS";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_DEU.equalsIgnoreCase(conurty)){
        	newCountry="DE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_DNK.equalsIgnoreCase(conurty)){
        	newCountry="DN";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ESP.equalsIgnoreCase(conurty)){
        	newCountry="ES";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_FIN.equalsIgnoreCase(conurty)){
        	newCountry="FI";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_FRA.equalsIgnoreCase(conurty)){
        	newCountry="FR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_GBR.equalsIgnoreCase(conurty)){
        	newCountry="GB";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ITA.equalsIgnoreCase(conurty)){
        	newCountry="IT";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_LUX.equalsIgnoreCase(conurty)){
        	newCountry="LU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_NLD.equalsIgnoreCase(conurty)){
        	newCountry="NL";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_NOR.equalsIgnoreCase(conurty)){
        	newCountry="NO";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SWE.equalsIgnoreCase(conurty)){
        	newCountry="SE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_HRV.equalsIgnoreCase(conurty)){
        	newCountry="HR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_GRC.equalsIgnoreCase(conurty)){
        	newCountry="GR";
        }
        else if(MtkTvConfigTypeBase.S639_CFG_LANG_HUN.equalsIgnoreCase(conurty)){
        	newCountry="HU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_IRL.equalsIgnoreCase(conurty)){
        	newCountry="IE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_POL.equalsIgnoreCase(conurty)){
        	newCountry="PL";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_PRT.equalsIgnoreCase(conurty)){
        	newCountry="PT";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ROU.equalsIgnoreCase(conurty)){
        	newCountry="RO";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_RUS.equalsIgnoreCase(conurty)){
        	newCountry="RU";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SRB.equalsIgnoreCase(conurty)){
        	newCountry="SR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SVK.equalsIgnoreCase(conurty)){
        	newCountry="SK";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SVN.equalsIgnoreCase(conurty)){
        	newCountry="SI";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_TUR.equalsIgnoreCase(conurty)){
        	newCountry="TR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_EST.equalsIgnoreCase(conurty)){
        	newCountry="EE";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_UKR.equalsIgnoreCase(conurty)){
        	newCountry="UA";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_THA.equalsIgnoreCase(conurty)){
            newCountry="TH";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ZAF.equalsIgnoreCase(conurty)){
            newCountry="ZA";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_SQP.equalsIgnoreCase(conurty)){
            newCountry="SG";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_ARG.equalsIgnoreCase(conurty)){
            newCountry="AR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_BRA.equalsIgnoreCase(conurty)){
            newCountry="BR";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_CAN.equalsIgnoreCase(conurty)){
            newCountry="CA";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_JPN.equalsIgnoreCase(conurty)){
            newCountry="JP";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_NZL.equalsIgnoreCase(conurty)){
            newCountry="NZ";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_MYS.equalsIgnoreCase(conurty)){
            newCountry="MY";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_IDN.equalsIgnoreCase(conurty)){
            newCountry="ID";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_VNM.equalsIgnoreCase(conurty)){
            newCountry="VN";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_MMR.equalsIgnoreCase(conurty)){
            newCountry="MM";
        }
        else if(MtkTvConfigTypeBase.S3166_CFG_COUNT_IND.equalsIgnoreCase(conurty)){
            newCountry="IN";
        }
        else{
        	newCountry=conurty;
        }
        return newCountry;
	}
}
