
package com.mediatek.wwtv.setting.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

//import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.R;
//import com.mediatek.wwtv.setting.util.MenuConfigManager;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.setting.widget.detailui.Action;

import org.xmlpull.v1.XmlPullParserException;

public class GetTimeZone {
  public static final String TAG = "GetTimeZone";
  private final TimeZone mTz;
//  private final String[] zoneIdNames;
  private static GetTimeZone getTimeZone;
  private final Context mContext;
  private List<Action> mTimeZoneActions;
  private String[] zonesArray;
  private static final String XMLTAG_TIMEZONE = "timezone";
  AlarmManager alarm;

  public GetTimeZone(Context context) {
//    zoneIdNames = context.getResources().getStringArray(
//        R.array.menu_setup_timezone_array);
    mTz = Calendar.getInstance().getTimeZone();
    mContext = context;
    alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
  }

  public static GetTimeZone getInstance(Context context) {
    getTimeZone = new GetTimeZone(context);
    return getTimeZone;
  }

  public void setTimeZone(int value, String zoneinfo) {
    String tzoneID = MtkTvConfigType.CFG_TIME_ZONE;
    //int gmtM12 = MtkTvConfigTypeBase.TIMEZONE_gmtM1200;
    String broadcastCfgId = MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS;
    int asBcst = MtkTvConfigTypeBase.TIMEZONE_AS_BROADCAST;
    int tzOffset = 0;

    if (value == asBcst) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setTimeZone:set broadcast");
      TVContent.getInstance(mContext).setConfigValue(broadcastCfgId, 1);
    } else {
      int gmtPos = zoneinfo.indexOf("/");
      String gmtPrefix = "GMT";
      String gmtValue = zoneinfo.substring(gmtPos + gmtPrefix.length() + 1);
      String[] hours = gmtValue.substring(1).split(":");
      int hour = Integer.parseInt(hours[0]);
      int min = Integer.parseInt(hours[1]);
      tzOffset = hour * 60 * 60 + min * 60;
      if (gmtValue.contains("-")) {// -06:00
        tzOffset = tzOffset * (-1);
      }
      TVContent.getInstance(mContext).setConfigValue(broadcastCfgId, 0);
//      boolean dsl = TVContent.getInstance(mContext).getConfigValue(
//      MtkTvConfigType.CFG_TIME_AUTO_DST) == 1 ? true : false;
//      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setTimeZone:value:" + value + "dsl:" + dsl);
//      TVContent.getInstance(mContext).setConfigValue(tzoneID, tzOffset, dsl);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setTimeZone:tzOffset:" + tzOffset);
//    TVContent.getInstance(mContext).setConfigValue(tzoneID, tzOffset);
    boolean dsl = TVContent.getInstance(mContext).getConfigValue(
            MtkTvConfigType.CFG_TIME_AUTO_DST) == 1 ? true : false;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setTimeZone:value:" + value + "dsl:" + dsl);
    TVContent.getInstance(mContext).setConfigValue(tzoneID, tzOffset, dsl);
  }

  /**
   * @deprecated
   * @return
   */
  @Deprecated
  public int getZoneId() {
    int value = 0;
    value = TVContent.getInstance(mContext).getConfigValue(MtkTvConfigType.CFG_TIME_ZONE);
    int tzOffset = value;
    if (CommonIntegration.isSARegion()) {
      if (tzOffset == -16 * 3600)
      {
        tzOffset = 16 * 3600;
      }
      /* need adjustment as Brazil uses UTC-3 time in SI tables */
      tzOffset -= 3 * 3600;
    }
    int index = -1;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("zone", "tzOffset:" + tzOffset);
    for (int i = 0; i <= MenuConfigManager.MAX_TIME_ZONE; i++)
    {
      if (MenuConfigManager.zoneValue[i] == tzOffset)
      {
        index = i;
        break;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("zone", "index:" + index);
    int gmtM12 = MtkTvConfigTypeBase.TIMEZONE_GMT_M_1200;
    if (index >= gmtM12 && index < gmtM12 + 13) {
      value = index - gmtM12;
    } else if (index > 0 && index < gmtM12) {
      value = index + 13;
    } else if (index == 0) {
      value = 14;
    } else {
      value = 13;
    }
    String broadcastCfgId = MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS;
    if (TVContent.getInstance(mContext).getConfigValue(broadcastCfgId) == 1) {
      value = 13;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("zone", "value:" + value);
    return value;
  }

  /**
   * Formats the provided timezone offset into a string of the form GMT+XX:XX
   */
  private static StringBuilder formatOffset(StringBuilder sb, long offset) {
    long off = offset / 1000 / 60;

    sb.append("GMT");
    if (off < 0) {
      sb.append('-');
      off = -off;
    } else {
      sb.append('+');
    }

    int hours = (int) (off / 60);
    int minutes = (int) (off % 60);

    sb.append((char) ('0' + hours / 10));
    sb.append((char) ('0' + hours % 10));

    sb.append(':');

    sb.append((char) ('0' + minutes / 10));
    sb.append((char) ('0' + minutes % 10));

    return sb;
  }

  /**
   * Helper class to hold the time zone data parsed from the Time Zones XML file.
   */
  private class TimeZoneInfo implements Comparable<TimeZoneInfo> {
    public String tzId;
    public String tzName;
    public long tzOffset;

    public TimeZoneInfo(String id, String name, long offset) {
      tzId = id;
      tzName = name;
      tzOffset = offset;
    }

    @Override
    public int compareTo(TimeZoneInfo another) {
      return (int) (tzOffset - another.tzOffset);
    }
  }

  /**
   * Parse the Time Zones information from the XML file and creates Action objects for each time
   * zone.
   */
  private List<Action> getZoneActions(Context context) {
    if (mTimeZoneActions != null && !mTimeZoneActions.isEmpty()) {
      return mTimeZoneActions;
    }

    List<TimeZoneInfo> timeZones = getTimeZones(context);

    mTimeZoneActions = new ArrayList<Action>();

    // Sort the Time Zones list in ascending offset order
    Collections.sort(timeZones);

    TimeZone currentTz = TimeZone.getDefault();

    for (TimeZoneInfo tz : timeZones) {
      StringBuilder name = new StringBuilder();
      boolean checked = currentTz.getID().equals(tz.tzId);
      mTimeZoneActions.add(getTimeZoneAction(tz.tzId, tz.tzName,
          formatOffset(name, tz.tzOffset).toString(), checked));
    }

    return mTimeZoneActions;
  }

  /**
   * Parses the XML time zone information into an array of TimeZoneInfo objects.
   */
  private List<TimeZoneInfo> getTimeZones(Context context) {
    ArrayList<TimeZoneInfo> timeZones = new ArrayList<TimeZoneInfo>();
    final long date = Calendar.getInstance().getTimeInMillis();
    try {
      XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
      while (xrp.next() != XmlResourceParser.START_TAG){
//        continue;
        xrp.next();
      }
      while (xrp.getEventType() != XmlResourceParser.END_TAG) {
        while (xrp.getEventType() != XmlResourceParser.START_TAG &&
            xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
          xrp.next();
        }

        if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
          break;
        }

        if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
          String id = xrp.getAttributeValue(0);
          String displayName = xrp.nextText();
          TimeZone tz = TimeZone.getTimeZone(id);
          long offset;
          if (tz != null) {
            offset = tz.getOffset(date);
            timeZones.add(new TimeZoneInfo(id, displayName, offset));
          } else {
            continue;
          }
        }
        while (xrp.getEventType() != XmlResourceParser.END_TAG) {
          xrp.next();
        }
        xrp.next();
      }
      xrp.close();
    } catch (XmlPullParserException xppe) {
      Log.e(TAG, "Ill-formatted timezones.xml file");
    } catch (java.io.IOException ioe) {
      Log.e(TAG, "Unable to read timezones.xml file");
    }
    return timeZones;
  }

  private static Action getTimeZoneAction(String tzId, String displayName, String gmt,
      boolean setChecked) {
    return new Action.Builder().key(tzId).title(displayName).description(gmt).
        checked(setChecked).build();
  }

  public String getTimeZoneOlsonID(int index) {
    if (mTimeZoneActions == null) {
      getZoneActions(mContext);
    }
    String olsonID = mTimeZoneActions.get(index).getKey();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTimeZoneOlsonID " + olsonID);
    return olsonID;
  }

  public String[] generateTimeZonesArray() {
    if (zonesArray != null) {
      return zonesArray;
    }
    if (mTimeZoneActions == null) {
      getZoneActions(mContext);
    }
    zonesArray = new String[mTimeZoneActions.size() + 1];
    zonesArray[0] = "As Broadcast";
    for (int i = 1; i <= mTimeZoneActions.size(); i++) {
      Action zone = mTimeZoneActions.get(i - 1);
      zonesArray[i] = zone.getmTitle() + "/" + zone.getDescription();
      // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "generateTimeZonesArray " + zonesArray[i]);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "generateTimeZonesArray length:" + zonesArray.length);
    return zonesArray;
  }

  /**
   * get the current time zone index in timezoneactions
   *
   * @return
   */
  public int getCurrentTimeZoneIndex() {
    if (mTimeZoneActions == null) {
      getZoneActions(mContext);
    }
    int value = -1;
    String broadcastCfgId = MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS;
    // if is as boradcast
    if (TVContent.getInstance(mContext).getConfigValue(broadcastCfgId) == 1) {
      value = 0;
      return value;
    } else {
      String zoneName = mTz.getDisplayName();
      String zoneID = mTz.getID();
      TimeZone currentTz = TimeZone.getDefault();
      final long datemills = Calendar.getInstance().getTimeInMillis();
      long offset = currentTz.getOffset(datemills);
      StringBuilder matcher = new StringBuilder();
      formatOffset(matcher, offset);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "matcher:" + matcher);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "zoneName:" + zoneName + ",zoneID=" + zoneID);
      for (int i = 0; i < mTimeZoneActions.size(); i++) {
        Action zone = mTimeZoneActions.get(i);
        // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "zonekey=" + zone.getKey() + ",gmt==" + zone.getDescription());
        if (zoneID.equals(zone.getKey())) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "id equals index:" + i);
          value = i;
          break;
        }
        if (matcher.toString().equals(zone.getDescription())) {// gmt+01:00
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "matcher equals index:" + i);
          value = i;
          List<Action> sameGmtzones = findZonesBySameGmt(matcher.toString(), i);
          for (int j = 0; j < sameGmtzones.size(); j++) {
            Action gmtzone = sameGmtzones.get(j);
            if (zoneID.equals(gmtzone.getKey())) {
              value += j;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "matcher samegmt index:" + value);
              break;
            }
          }
          break;
        }
      }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "match value:" + (value + 1));
    // because the zonearrays first child is 'as broad cast',
    // but mTimeZoneActions hasn't 'as broad cast' so set the index +1
    return value + 1;
  }

  /**
   * find the zones with same gmt
   *
   * @param matcher
   * @param pos
   * @return
   */
  private List<Action> findZonesBySameGmt(String matcher, int pos) {
    ArrayList<Action> sameGmtZones = new ArrayList<Action>();
    for (int i = pos; i < mTimeZoneActions.size(); i++) {
      Action zone = mTimeZoneActions.get(i);
      if (matcher.equals(zone.getDescription())) {
        sameGmtZones.add(zone);
      } else {
        break;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "findZonesBySameGmt size:" + sameGmtZones.size());
    return sameGmtZones;
  }

}
