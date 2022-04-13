package com.mediatek.wwtv.tvcenter.nav.fav;

public class TVChannel {

	private String channelName="";
	private String channelNum="";
	private int channelID=-1;
	private boolean favorite=false;
	private int frequence=-1;

	public void setChannelNum(String str) {
		// TODO Auto-generated method stub
		this.channelNum=str;
	}

	public void setChannelName(String str) {
		this.channelName=str;
	}
	public String getChannelNum() {
		// TODO Auto-generated method stub
		return channelNum;
	}

	public CharSequence getChannelName() {
		// TODO Auto-generated method stub
		return channelName;
	}

	public int getFreq() {
		// TODO Auto-generated method stub
		return frequence;
	}
	public void setFreq(int freq) {
		// TODO Auto-generated method stub
		this.frequence=freq;
	}

	public boolean isFavorite() {
		// TODO Auto-generated method stub
		return favorite;
	}

	public void setFavorite(boolean isFavorite) {
		// TODO Auto-generated method stub
		this.favorite=isFavorite;
	}

	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TVChannel) {
			if (((TVChannel) o).getChannelID() == getChannelID()) {
				return true;
			}
		}
		return false;
	}

	@Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return channelID;
    }

    public int getChannelID() {
		return channelID;
	}

	public void setChannelID(int channelID) {
		this.channelID = channelID;
	}

}
