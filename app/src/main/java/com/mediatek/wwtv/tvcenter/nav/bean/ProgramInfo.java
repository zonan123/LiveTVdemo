package com.mediatek.wwtv.tvcenter.nav.bean;

@SuppressWarnings("PMD")
public class ProgramInfo {
    // program info
    private String programId;
    private String mainTitle;//first title
    private String secondaryTitle;//second title
    private String mediumSynopsis;// detail
    private String shortSynopsis;//detail
    private String basicDescriptionGenreHref;//category
    private String basicDescriptionGenreType;//category
    private int basicDescriptionGenreCategoryIndex = -1;
    private String basicDescriptionHowRelated;
    private String basicDescriptionMediaUri;
    private String basicDescriptionMediaUriContentType;
    private String basicDescriptionParentalGuidanceExplanatoryTest;

    public void setBasicDescriptionParentalGuidanceExplanatoryTest(String basicDescriptionParentalGuidanceExplanatoryTest) {
        this.basicDescriptionParentalGuidanceExplanatoryTest = basicDescriptionParentalGuidanceExplanatoryTest;
    }

    public String getBasicDescriptionParentalGuidanceExplanatoryTest() {
        return basicDescriptionParentalGuidanceExplanatoryTest;
    }

    private String basicDescriptionMemberOfType;
    private int basicDescriptionMemberOfTypeIndex;
    private String basicDescriptionMemberOfTypeCrId;
    
    //program location
    //private String programLanguage; // xml:lang=eng
    private String serviceIDRef;
    private String start;
    private String end;
    private String crId;
    private String programURL;
    private String instanceDescriptionGenreHref;//category
    private String instanceDescriptionGenreType;//category
    private String captionLanguage;
    private boolean captionLanguageState;
    //audio
    private String mixTypeHref;
    private String audioLanguagePurpose;
    private String audioLanguage;
    
    //video
    private String horizontalSize;
    private String verticalSize;
    private String aspectRatio;
    
    private String codingHref;
    private String instanceDescriptionHowRelated;
    private String instanceDescriptionMediaUri;
    private String instanceDescriptionMediaUriContentType;
    private String instanceDescriptionAuxiliaryUri;
    private String instanceDescriptionAuxiliaryUriContentType;
    
    private String publishedStartTime;
    private String publishedDuration;
    private boolean firstShowing;

    private boolean isSL;
    private boolean isAD;
    private String parentalRating;

    public void setParentalRating(String parentalRating) {
        this.parentalRating = parentalRating;
    }

    public String getParentalRating() {
        return parentalRating;
    }

    public void setSL(boolean SL) {
        isSL = SL;
    }

    public boolean isSL() {
        return isSL;
    }

    public void setAD(boolean AD) {
        isAD = AD;
    }

    public boolean isAD() {
        return isAD;
    }

    // OnDemandProgram
    private String onDemandProgramServiceIDRef;
    private String onDemandProgramProgramURL;
    private String onDemandProgramProgramURLContentType;
    private String onDemandProgramAuxiliaryUri;
    private String onDemandProgramAuxiliaryUriContentType;
    public String getProgramId() {
        return programId;
    }
    public void setProgramId(String programId) {
        this.programId = programId;
    }
    public String getMainTitle() {
        return mainTitle;
    }
    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }
    public String getSecondaryTitle() {
        return secondaryTitle;
    }
    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }
    public String getMediumSynopsis() {
        return mediumSynopsis;
    }
    public void setMediumSynopsis(String mediumSynopsis) {
        this.mediumSynopsis = mediumSynopsis;
    }
    public String getShortSynopsis() {
        return shortSynopsis;
    }
    public void setShortSynopsis(String shortSynopsis) {
        this.shortSynopsis = shortSynopsis;
    }
    public String getBasicDescriptionGenreHref() {
        return basicDescriptionGenreHref;
    }
    public void setBasicDescriptionGenreHref(String basicDescriptionGenreHref) {
        this.basicDescriptionGenreHref = basicDescriptionGenreHref;
    }
    public String getBasicDescriptionGenreType() {
        return basicDescriptionGenreType;
    }
    public void setBasicDescriptionGenreType(String basicDescriptionGenreType) {
        this.basicDescriptionGenreType = basicDescriptionGenreType;
    }

    public int getBasicDescriptionGenreCategoryIndex() {
        return basicDescriptionGenreCategoryIndex;
    }

    public void setBasicDescriptionGenreCategoryIndex(int basicDescriptionGenreCategoryIndex) {
        this.basicDescriptionGenreCategoryIndex = basicDescriptionGenreCategoryIndex;
    }

    public String getBasicDescriptionHowRelated() {
        return basicDescriptionHowRelated;
    }
    public void setBasicDescriptionHowRelated(String basicDescriptionHowRelated) {
        this.basicDescriptionHowRelated = basicDescriptionHowRelated;
    }
    public String getBasicDescriptionMediaUri() {
        return basicDescriptionMediaUri;
    }
    public void setBasicDescriptionMediaUri(String basicDescriptionMediaUri) {
        this.basicDescriptionMediaUri = basicDescriptionMediaUri;
    }
    public String getBasicDescriptionMediaUriContentType() {
        return basicDescriptionMediaUriContentType;
    }
    public void setBasicDescriptionMediaUriContentType(String basicDescriptionMediaUriContentType) {
        this.basicDescriptionMediaUriContentType = basicDescriptionMediaUriContentType;
    }
    public String getBasicDescriptionMemberOfType() {
        return basicDescriptionMemberOfType;
    }
    public void setBasicDescriptionMemberOfType(String basicDescriptionMemberOfType) {
        this.basicDescriptionMemberOfType = basicDescriptionMemberOfType;
    }
    public int getBasicDescriptionMemberOfTypeIndex() {
        return basicDescriptionMemberOfTypeIndex;
    }
    public void setBasicDescriptionMemberOfTypeIndex(int basicDescriptionMemberOfTypeIndex) {
        this.basicDescriptionMemberOfTypeIndex = basicDescriptionMemberOfTypeIndex;
    }
    public String getBasicDescriptionMemberOfTypeCrId() {
        return basicDescriptionMemberOfTypeCrId;
    }
    public void setBasicDescriptionMemberOfTypeCrId(String basicDescriptionMemberOfTypeCrId) {
        this.basicDescriptionMemberOfTypeCrId = basicDescriptionMemberOfTypeCrId;
    }
    public String getServiceIDRef() {
        return serviceIDRef;
    }
    public void setServiceIDRef(String serviceIDRef) {
        this.serviceIDRef = serviceIDRef;
    }
    public String getStart() {
        return start;
    }
    public void setStart(String start) {
        this.start = start;
    }
    public String getEnd() {
        return end;
    }
    public void setEnd(String end) {
        this.end = end;
    }
    public String getCrId() {
        return crId;
    }
    public void setCrId(String crId) {
        this.crId = crId;
    }
    public String getProgramURL() {
        return programURL;
    }
    public void setProgramURL(String programURL) {
        this.programURL = programURL;
    }
    public String getInstanceDescriptionGenreHref() {
        return instanceDescriptionGenreHref;
    }
    public void setInstanceDescriptionGenreHref(String instanceDescriptionGenreHref) {
        this.instanceDescriptionGenreHref = instanceDescriptionGenreHref;
    }
    public String getInstanceDescriptionGenreType() {
        return instanceDescriptionGenreType;
    }
    public void setInstanceDescriptionGenreType(String instanceDescriptionGenreType) {
        this.instanceDescriptionGenreType = instanceDescriptionGenreType;
    }
    public String getCaptionLanguage() {
        return captionLanguage;
    }
    public void setCaptionLanguage(String captionLanguage) {
        this.captionLanguage = captionLanguage;
    }
    public boolean isCaptionLanguageState() {
        return captionLanguageState;
    }
    public void setCaptionLanguageState(boolean captionLanguageState) {
        this.captionLanguageState = captionLanguageState;
    }
    public String getMixTypeHref() {
        return mixTypeHref;
    }
    public void setMixTypeHref(String mixTypeHref) {
        this.mixTypeHref = mixTypeHref;
    }
    public String getAudioLanguagePurpose() {
        return audioLanguagePurpose;
    }
    public void setAudioLanguagePurpose(String audioLanguagePurpose) {
        this.audioLanguagePurpose = audioLanguagePurpose;
    }
    public String getAudioLanguage() {
        return audioLanguage;
    }
    public void setAudioLanguage(String audioLanguage) {
        this.audioLanguage = audioLanguage;
    }
    public String getHorizontalSize() {
        return horizontalSize;
    }
    public void setHorizontalSize(String horizontalSize) {
        this.horizontalSize = horizontalSize;
    }
    public String getVerticalSize() {
        return verticalSize;
    }
    public void setVerticalSize(String verticalSize) {
        this.verticalSize = verticalSize;
    }
    public String getAspectRatio() {
        return aspectRatio;
    }
    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    public String getCodingHref() {
        return codingHref;
    }
    public void setCodingHref(String codingHref) {
        this.codingHref = codingHref;
    }
    public String getInstanceDescriptionHowRelated() {
        return instanceDescriptionHowRelated;
    }
    public void setInstanceDescriptionHowRelated(String instanceDescriptionHowRelated) {
        this.instanceDescriptionHowRelated = instanceDescriptionHowRelated;
    }
    public String getInstanceDescriptionMediaUri() {
        return instanceDescriptionMediaUri;
    }
    public void setInstanceDescriptionMediaUri(String instanceDescriptionMediaUri) {
        this.instanceDescriptionMediaUri = instanceDescriptionMediaUri;
    }
    public String getInstanceDescriptionMediaUriContentType() {
        return instanceDescriptionMediaUriContentType;
    }
    public void setInstanceDescriptionMediaUriContentType(String instanceDescriptionMediaUriContentType) {
        this.instanceDescriptionMediaUriContentType = instanceDescriptionMediaUriContentType;
    }
    public String getInstanceDescriptionAuxiliaryUri() {
        return instanceDescriptionAuxiliaryUri;
    }
    public void setInstanceDescriptionAuxiliaryUri(String instanceDescriptionAuxiliaryUri) {
        this.instanceDescriptionAuxiliaryUri = instanceDescriptionAuxiliaryUri;
    }
    public String getInstanceDescriptionAuxiliaryUriContentType() {
        return instanceDescriptionAuxiliaryUriContentType;
    }
    public void setInstanceDescriptionAuxiliaryUriContentType(
            String instanceDescriptionAuxiliaryUriContentType) {
        this.instanceDescriptionAuxiliaryUriContentType = instanceDescriptionAuxiliaryUriContentType;
    }
    public String getPublishedStartTime() {
        return publishedStartTime;
    }
    public void setPublishedStartTime(String publishedStartTime) {
        this.publishedStartTime = publishedStartTime;
    }
    public String getPublishedDuration() {
        return publishedDuration;
    }
    public void setPublishedDuration(String publishedDuration) {
        this.publishedDuration = publishedDuration;
    }
    public boolean isFirstShowing() {
        return firstShowing;
    }
    public void setFirstShowing(boolean firstShowing) {
        this.firstShowing = firstShowing;
    }
    public String getOnDemandProgramServiceIDRef() {
        return onDemandProgramServiceIDRef;
    }
    public void setOnDemandProgramServiceIDRef(String onDemandProgramServiceIDRef) {
        this.onDemandProgramServiceIDRef = onDemandProgramServiceIDRef;
    }
    public String getOnDemandProgramProgramURL() {
        return onDemandProgramProgramURL;
    }
    public void setOnDemandProgramProgramURL(String onDemandProgramProgramURL) {
        this.onDemandProgramProgramURL = onDemandProgramProgramURL;
    }
    public String getOnDemandProgramProgramURLContentType() {
        return onDemandProgramProgramURLContentType;
    }
    public void setOnDemandProgramProgramURLContentType(String onDemandProgramProgramURLContentType) {
        this.onDemandProgramProgramURLContentType = onDemandProgramProgramURLContentType;
    }
    public String getOnDemandProgramAuxiliaryUri() {
        return onDemandProgramAuxiliaryUri;
    }
    public void setOnDemandProgramAuxiliaryUri(String onDemandProgramAuxiliaryUri) {
        this.onDemandProgramAuxiliaryUri = onDemandProgramAuxiliaryUri;
    }
    public String getOnDemandProgramAuxiliaryUriContentType() {
        return onDemandProgramAuxiliaryUriContentType;
    }
    public void setOnDemandProgramAuxiliaryUriContentType(String onDemandProgramAuxiliaryUriContentType) {
        this.onDemandProgramAuxiliaryUriContentType = onDemandProgramAuxiliaryUriContentType;
    }

    @Override
    public String toString() {
        return "ProgramInfo{" +
                "programId='" + programId + '\'' +
                ", mainTitle='" + mainTitle + '\'' +
                ", secondaryTitle='" + secondaryTitle + '\'' +
                ", basicDescriptionParentalGuidanceExplanatoryTest='" + basicDescriptionParentalGuidanceExplanatoryTest + '\'' +
                ", serviceIDRef='" + serviceIDRef + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", crId='" + crId + '\'' +
                ", captionLanguage='" + captionLanguage + '\'' +
                ", captionLanguageState=" + captionLanguageState +
                ", audioLanguagePurpose='" + audioLanguagePurpose + '\'' +
                ", audioLanguage='" + audioLanguage + '\'' +
                ", codingHref='" + codingHref + '\'' +
                ", publishedStartTime='" + publishedStartTime + '\'' +
                ", publishedDuration='" + publishedDuration + '\'' +
                ", firstShowing=" + firstShowing +
                ", isSL=" + isSL +
                ", isAD=" + isAD +
                ", parentalRating='" + parentalRating + '\'' +
                '}';
    }
}
