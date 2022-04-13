package com.mediatek.wwtv.tvcenter.nav.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.mediatek.mtkmdsclient.data.tvmain.AVAttributes;
import com.mediatek.mtkmdsclient.data.tvmain.AudioAttributes;
import com.mediatek.mtkmdsclient.data.tvmain.AudioLanguage;
import com.mediatek.mtkmdsclient.data.tvmain.CaptionLanguage;
import com.mediatek.mtkmdsclient.data.tvmain.CaptioningAttributes;
import com.mediatek.mtkmdsclient.data.tvmain.ExplanatoryText;
import com.mediatek.mtkmdsclient.data.tvmain.Genre;
import com.mediatek.mtkmdsclient.data.tvmain.InstanceDescription;
import com.mediatek.mtkmdsclient.data.tvmain.MemberOf;
import com.mediatek.mtkmdsclient.data.tvmain.Mpeg7ParentalRating;
import com.mediatek.mtkmdsclient.data.tvmain.OnDemandProgram;
import com.mediatek.mtkmdsclient.data.tvmain.ParentalGuidance;
import com.mediatek.mtkmdsclient.data.tvmain.ProgramInformation;
import com.mediatek.mtkmdsclient.data.tvmain.Schedule;
import com.mediatek.mtkmdsclient.data.tvmain.ScheduleEvent;
import com.mediatek.mtkmdsclient.data.tvmain.SignLanguage;
import com.mediatek.mtkmdsclient.data.tvmain.Synopsis;
import com.mediatek.mtkmdsclient.data.tvmain.TVAMain;
import com.mediatek.mtkmdsclient.data.tvmain.Title;
import com.mediatek.wwtv.tvcenter.nav.bean.ProgramInfo;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class IPChannelDataReader {
  private static final String TAG = "IPChannelDataReader";
  private static final IPChannelDataReader dtReader = new IPChannelDataReader();

  private final List<String> captionCodingList = new ArrayList<String>(
          Arrays.asList("urn:tva:metadata:cs:CaptionCodingFormatCS:2010:2.1",
                        "urn:tva:metadata:cs:CaptionCodingFormatCS:2010:2.2"
          ));

  private final List<String> captionLangList = new ArrayList<String>(
          Arrays.asList("eng",
                        "cym",
                        "wel",
                        "gla",
                        "gle",
                        "und",
                        "qaa"
          ));

  private final List<String> categoryList = new ArrayList<String>(
          Arrays.asList("urn:fvc:metadata:cs:ContentSubjectCS:2014-07:0",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:1",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:2",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:2.1",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:2.2",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:2.3",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:3",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:3.1",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:3.2",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:4",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:5",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:6",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:7",
                  "urn:fvc:metadata:cs:ContentSubjectCS:2014-07:8"
          ));

  private IPChannelDataReader() {
  }

  public static IPChannelDataReader getInstance() {
    return dtReader;
  }

  public List<ProgramInfo> toCreateProgram(TVAMain mTVAMain) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start toCreateProgram");
    List<ProgramInfo> mListProgramInfos = new ArrayList<ProgramInfo>();
    if (mTVAMain != null
            && mTVAMain.programDescription != null
            && mTVAMain.programDescription.programInformationTable != null
            && mTVAMain.programDescription.programInformationTable.programInformation != null
            && mTVAMain.programDescription.programInformationTable.programInformation.size()> 0) {

      List<ProgramInformation> programInformations =
              mTVAMain.programDescription.programInformationTable.programInformation;
      Schedule schedule =
              mTVAMain.programDescription.programLocationTable.schedule.get(0);
      for (ProgramInformation programInformation : programInformations) {
        ProgramInfo progInfo = new ProgramInfo();
        progInfo.setProgramId(programInformation.programId);

        Genre genre = programInformation.basicDescription.genre;
        if (genre != null) {
          progInfo.setBasicDescriptionGenreHref(genre.href);
          progInfo.setBasicDescriptionGenreType(genre.type);
          //category
          if ("main".equals(genre.type)) {
            progInfo.setBasicDescriptionGenreCategoryIndex(categoryList.indexOf(genre.href));
          }
        }

        ParentalGuidance parentalGuidance = programInformation.basicDescription.parentalGuidance;

        if (parentalGuidance != null) {
          //guidance text
          ExplanatoryText explanatoryText = parentalGuidance.explanatoryText;
          progInfo.setBasicDescriptionParentalGuidanceExplanatoryTest(explanatoryText != null ? explanatoryText.content : null);

          //rating
          parseRating(progInfo, parentalGuidance.mpeg7ParentalRating);
        }

        List<Title> listTitles = programInformation.basicDescription.title;
        if (listTitles != null) {
          for (Title title : listTitles) {
            if ("main".equals(title.type)) {
              progInfo.setMainTitle(title.content);
            } else if ("secondary".equals(title.type)) {
              progInfo.setSecondaryTitle(title.content);
            }
          }
        }

        List<Synopsis> listSynopsiss = programInformation.basicDescription.synopsis;
        if (listSynopsiss != null) {
          for (Synopsis synopsis : listSynopsiss) {
            if ("medium".equals(synopsis.length)) {
              progInfo.setMediumSynopsis(synopsis.content);
            } else if ("short".equals(synopsis.length)) {
              progInfo.setShortSynopsis(synopsis.content);
            }
          }
        }

        MemberOf memberOf = programInformation.memberOf;
        if (memberOf != null) {
          progInfo.setBasicDescriptionMemberOfType(memberOf.xsiType);
          progInfo.setBasicDescriptionMemberOfTypeIndex(memberOf.index);
          progInfo.setBasicDescriptionMemberOfTypeCrId(memberOf.crid);
        }

        if (schedule.scheduleEvent != null && schedule.scheduleEvent.size() > 0) {
          for (ScheduleEvent event : schedule.scheduleEvent) {
            if (event.program.crid.equals(programInformation.programId)) {
              progInfo.setProgramURL(event.programURL);
              progInfo.setPublishedStartTime(event.publishedStartTime);
              progInfo.setPublishedDuration(event.publishedDuration);
              InstanceDescription instanceDescription = event.instanceDescription;
              parseIcons(progInfo, instanceDescription);
              break;
            }
          }
        }

        mListProgramInfos.add(progInfo);
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end toCreateProgram");
    return mListProgramInfos;
  }

  private void parseIcons(ProgramInfo progInfo, InstanceDescription instanceDescription) {
    //subtitle
    CaptionLanguage captionLanguage = instanceDescription.captionLanguage;
    if (captionLanguage != null && captionLanguage.closed) {
      progInfo.setCaptionLanguage(captionLanguage.content);
      progInfo.setCaptionLanguageState(captionLangList.contains(captionLanguage.content));
    }
    AVAttributes aVAttributes = instanceDescription.aVAttributes;
    if (aVAttributes != null) {
      CaptioningAttributes captioningAttributes = aVAttributes.captioningAttributes;
      if (captioningAttributes != null && captioningAttributes.coding != null) {
        String captionCoding = captioningAttributes.coding.href;
        if(!TextUtils.isEmpty(captionCoding) && !captionCodingList.contains(captionCoding)) {
          progInfo.setCaptionLanguageState(false);
        }
      }

      //sl icon
      SignLanguage signLanguage = instanceDescription.signLanguage;
      progInfo.setSL(signLanguage != null && !signLanguage.closed &&
              !TextUtils.isEmpty(signLanguage.content) &&
              !"invalid".equals(signLanguage.content));
      //ad icon
      List<AudioAttributes> audioAttributes = aVAttributes.audioAttributes;
      if(audioAttributes != null) {
        for (AudioAttributes audioAttribute : audioAttributes) {
          AudioLanguage audioLanguage = audioAttribute.audioLanguage;
          if (audioLanguage != null && "urn:tva:metadata:cs:AudioPurposeCS:2007:1".equals(audioLanguage.purpose)) {
            String content = audioLanguage.content;
            if (!TextUtils.isEmpty(content)) {
              progInfo.setAD(true);
              break;
            }
          }
        }
      }
    }
  }

  private void parseRating(ProgramInfo progInfo, Mpeg7ParentalRating parentalRating) {
    if (parentalRating != null && !TextUtils.isEmpty(parentalRating.href)) {
      if(parentalRating.href.contains("urn:dtg:cs:BBFCContentRatingCS:2002") ||
              parentalRating.href.contains("urn:fvc:metadata:cs:ContentRatingCS:2014-07")) {
        int i = parentalRating.href.lastIndexOf(":");
        if(i + 1 < parentalRating.href.length()) {
          String substring = parentalRating.href.substring(i + 1);
          switch (substring) {
            case "U":
            case "unrated":
              progInfo.setParentalRating("U");
              break;
            case "PG":
            case "parental_guidance":
              progInfo.setParentalRating("PG");
              break;
            case "12":
            case "twelve":
              progInfo.setParentalRating("12");
              break;
            case "15":
            case "fifteen":
              progInfo.setParentalRating("15");
              break;
            case "16":
            case "sixteen":
              progInfo.setParentalRating("16");
              break;
            case "18":
            case "eighteen":
              progInfo.setParentalRating("18");
              break;
            default:
              break;
          }
        }
      } else if(parentalRating.href.contains("urn:dtg:metadata:cs:DTGContentWarningCS:2011") ||
              parentalRating.href.contains("urn:dtg:metadata:cs:DTGContentWarningCS:2011-1")) {
        int i = parentalRating.href.lastIndexOf(":");
        if(i + 1 < parentalRating.href.length()) {
          String substring = parentalRating.href.substring(i + 1);
          if (!TextUtils.isEmpty(substring)) {
            progInfo.setParentalRating(substring);
          }
        }
      }
    }
  }

  /**
   * <PublishedStartTime>2019-09-10T09:00:00Z</PublishedStartTime>
   * <PublishedDuration>PT1H</PublishedDuration>
   */
  @SuppressLint("SimpleDateFormat")
  public String parseDuration(String startTime, String durationStr) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parseDuration startTime=" + startTime + ", durationStr=" + durationStr);
    SimpleDateFormat hmFormat = new SimpleDateFormat("HH:mm");
    try {
      String formatStartTime = "";
      String formatEndTime = "";
      Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(startTime);
      long dur = getFvpDuration(durationStr) * 1000;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parseDuration dur=" + dur);
      long endTimeMills = startDate.getTime() + dur;
      Date endDate = new Date(endTimeMills);
      formatStartTime = hmFormat.format(startDate);
      formatEndTime = hmFormat.format(endDate);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
              TAG,
              "parseDuration formatStartTime=" + formatStartTime + ", formatEndTime=" + formatEndTime);
      return formatStartTime + " - " + formatEndTime;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public long getFvpDuration(String durationStr) {
    String regexH = "PT\\d+H";
    String regexM = "PT\\d+M";
    String regexHM = "PT\\d+H\\d+M";
    long dur = 0;//The unit is seconds
    if (durationStr.matches(regexHM)) {
      String h = durationStr.substring(2, durationStr.indexOf("H"));
      String m = durationStr.substring(durationStr.indexOf("H") + 1, durationStr.indexOf("M"));
      dur = Integer.parseInt(h) * (long)60 * 60 + Integer.parseInt(m) * 60;
    } else if (durationStr.matches(regexH)) {
      String h = durationStr.substring(2, durationStr.indexOf("H"));
      dur = Integer.parseInt(h) * (long)60 * 60;
    } else if (durationStr.matches(regexM)) {
      String m = durationStr.substring(2, durationStr.indexOf("M"));
      dur = Integer.parseInt(m) * (long)60;
    }
    return dur;
  }
}
