package com.mediatek.wwtv.tvcenter.util.tif;

/**
 * 
 * @author sin_xinsheng
 *
 */
public class TIFProgramInfo {
	/**sqlite auto increment id, not same as tv api channel id*/
	public long mChannelId;
	public int mEventId;
	public String mTitle;
	public int mSeasonNumber;
	public int mEpisodeNumber;
	public String mEpisodeTitle;
	public long mStartTimeUtcSec;
	public long mEndTimeUtcSec;
	public String mBroadcastGenre;
	public String mCanonicalGenre;
	public String mDescription;
	public String mLongDescription;
	public int mVideoWidth;
	public int mVideoHeight;
	public String mAudioLanguage;
	public String mRating;
	public String mPosterArtUri;
	public String mThumbnailUri;
	public String mData;
	
	@Override
	public String toString() {
		return "[TIFProgramInfo] mChannelId:" + mChannelId + ",  mEventId:" + mEventId + ",  mTitle:" + mTitle 
				+ ",  mSeasonNumber:" + mSeasonNumber + ",  mEpisodeNumber:" + mEpisodeNumber 
				+ ",  mEpisodeTitle:" + mEpisodeTitle + ",  mStartTimeUtcSec:" + mStartTimeUtcSec 
				+ ",  mEndTimeUtcSec:" + mEndTimeUtcSec + ",  mBroadcastGenre:" + mBroadcastGenre + ",  mCanonicalGenre:" + mCanonicalGenre 
				+ ",  mDescription:" + mDescription + ",  mLongDescription:" + mLongDescription 
				+ ",  mVideoWidth:" + mVideoWidth + ",  mVideoHeight:" + mVideoHeight + ",  mAudioLanguage:" + mAudioLanguage 
				+ ",  mRating:" + mRating + ",  mPosterArtUri:" + mPosterArtUri + ",  mThumbnailUri:" + mThumbnailUri
				+ ",  mData:" + mData ;
	}

    public void copyFrom(TIFProgramInfo other) {
        if (this == other) {
            return;
        }

        mChannelId = other.mChannelId;
        mTitle = other.mTitle;
        mEpisodeTitle = other.mEpisodeTitle;
        mSeasonNumber = other.mSeasonNumber;
        mEpisodeNumber = other.mEpisodeNumber;
        mStartTimeUtcSec = other.mStartTimeUtcSec;
        mEndTimeUtcSec = other.mEndTimeUtcSec;
        mDescription = other.mDescription;
        mVideoWidth = other.mVideoWidth;
        mVideoHeight = other.mVideoHeight;
        mPosterArtUri = other.mPosterArtUri;
        mThumbnailUri = other.mThumbnailUri;
        mRating = other.mRating;
    }

    public long getmChannelId() {
        return mChannelId;
    }

    public void setmChannelId(long mChannelId) {
        this.mChannelId = mChannelId;
    }

    public int getmEventId() {
        return mEventId;
    }

    public void setmEventId(int mEventId) {
        this.mEventId = mEventId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getmSeasonNumber() {
        return mSeasonNumber;
    }

    public void setmSeasonNumber(int mSeasonNumber) {
        this.mSeasonNumber = mSeasonNumber;
    }

    public int getmEpisodeNumber() {
        return mEpisodeNumber;
    }

    public void setmEpisodeNumber(int mEpisodeNumber) {
        this.mEpisodeNumber = mEpisodeNumber;
    }

    public String getmEpisodeTitle() {
        return mEpisodeTitle;
    }

    public void setmEpisodeTitle(String mEpisodeTitle) {
        this.mEpisodeTitle = mEpisodeTitle;
    }

    public long getmStartTimeUtcSec() {
        return mStartTimeUtcSec;
    }

    public void setmStartTimeUtcSec(long mStartTimeUtcSec) {
        this.mStartTimeUtcSec = mStartTimeUtcSec;
    }

    public long getmEndTimeUtcSec() {
        return mEndTimeUtcSec;
    }

    public void setmEndTimeUtcSec(long mEndTimeUtcSec) {
        this.mEndTimeUtcSec = mEndTimeUtcSec;
    }

    public String getmBroadcastGenre() {
        return mBroadcastGenre;
    }

    public void setmBroadcastGenre(String mBroadcastGenre) {
        this.mBroadcastGenre = mBroadcastGenre;
    }

    public String getmCanonicalGenre() {
        return mCanonicalGenre;
    }

    public void setmCanonicalGenre(String mCanonicalGenre) {
        this.mCanonicalGenre = mCanonicalGenre;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmLongDescription() {
        return mLongDescription;
    }

    public void setmLongDescription(String mLongDescription) {
        this.mLongDescription = mLongDescription;
    }

    public int getmVideoWidth() {
        return mVideoWidth;
    }

    public void setmVideoWidth(int mVideoWidth) {
        this.mVideoWidth = mVideoWidth;
    }

    public int getmVideoHeight() {
        return mVideoHeight;
    }

    public void setmVideoHeight(int mVideoHeight) {
        this.mVideoHeight = mVideoHeight;
    }

    public String getmAudioLanguage() {
        return mAudioLanguage;
    }

    public void setmAudioLanguage(String mAudioLanguage) {
        this.mAudioLanguage = mAudioLanguage;
    }

    public String getmRating() {
        return mRating;
    }

    public void setmRating(String mRating) {
        this.mRating = mRating;
    }

    public String getmPosterArtUri() {
        return mPosterArtUri;
    }

    public void setmPosterArtUri(String mPosterArtUri) {
        this.mPosterArtUri = mPosterArtUri;
    }

    public String getmThumbnailUri() {
        return mThumbnailUri;
    }

    public void setmThumbnailUri(String mThumbnailUri) {
        this.mThumbnailUri = mThumbnailUri;
    }

    public String getmData() {
        return mData;
    }

    public void setmData(String mData) {
        this.mData = mData;
    }

    public static final class Builder {

        private final TIFProgramInfo mProgramInfo;

        public Builder() {
            mProgramInfo = new TIFProgramInfo();
            mProgramInfo.mChannelId = -1;
            mProgramInfo.mTitle = "title";
            mProgramInfo.mSeasonNumber = -1;
            mProgramInfo.mEpisodeNumber = -1;
            mProgramInfo.mStartTimeUtcSec = -1;
            mProgramInfo.mEndTimeUtcSec = -1;
            mProgramInfo.mDescription = "description";
        }

        public Builder setmChannelId(long mChannelId) {
            mProgramInfo.mChannelId = mChannelId;
            return this;
        }

        public Builder setmEventId(int mEventId) {
            mProgramInfo.mEventId = mEventId;
            return this;
        }

        public Builder setmTitle(String mTitle) {
            mProgramInfo.mTitle = mTitle;
            return this;
        }

        public Builder setmSeasonNumber(int mSeasonNumber) {
            mProgramInfo.mSeasonNumber = mSeasonNumber;
            return this;
        }

        public Builder setmEpisodeNumber(int mEpisodeNumber) {
            mProgramInfo.mEpisodeNumber = mEpisodeNumber;
            return this;
        }

        public Builder setmEpisodeTitle(String mEpisodeTitle) {
            mProgramInfo.mEpisodeTitle = mEpisodeTitle;
            return this;
        }

        public Builder setmStartTimeUtcSec(long mStartTimeUtcSec) {
            mProgramInfo.mStartTimeUtcSec = mStartTimeUtcSec;
            return this;
        }

        public Builder setmEndTimeUtcSec(long mEndTimeUtcSec) {
            mProgramInfo.mEndTimeUtcSec = mEndTimeUtcSec;
            return this;
        }

        public Builder setmBroadcastGenre(String mBroadcastGenre) {
            mProgramInfo.mBroadcastGenre = mBroadcastGenre;
            return this;
        }

        public Builder setmCanonicalGenre(String mCanonicalGenre) {
            mProgramInfo.mCanonicalGenre = mCanonicalGenre;
            return this;
        }

        public Builder setmDescription(String mDescription) {
            mProgramInfo.mDescription = mDescription;
            return this;
        }

        public Builder setmLongDescription(String mLongDescription) {
            mProgramInfo.mLongDescription = mLongDescription;
            return this;
        }

        public Builder setmVideoWidth(int mVideoWidth) {
            mProgramInfo.mVideoWidth = mVideoWidth;
            return this;
        }

        public Builder setmVideoHeight(int mVideoHeight) {
            mProgramInfo.mVideoHeight = mVideoHeight;
            return this;

        }

        public Builder setmAudioLanguage(String mAudioLanguage) {
            mProgramInfo.mAudioLanguage = mAudioLanguage;
            return this;
        }

        public Builder setmRating(String mRating) {
            mProgramInfo.mRating = mRating;
            return this;
        }

        public Builder setmPosterArtUri(String mPosterArtUri) {
            mProgramInfo.mPosterArtUri = mPosterArtUri;
            return this;
        }

        public Builder setmThumbnailUri(String mThumbnailUri) {
            mProgramInfo.mThumbnailUri = mThumbnailUri;
            return this;
        }

        public Builder setmData(String mData) {
            mProgramInfo.mData = mData;
            return this;
        }

        public TIFProgramInfo build() {
            TIFProgramInfo program = new TIFProgramInfo();
            program.copyFrom(mProgramInfo);
            return program;
        }
    }

}
