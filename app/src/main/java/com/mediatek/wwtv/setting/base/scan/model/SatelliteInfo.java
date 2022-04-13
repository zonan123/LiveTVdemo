package com.mediatek.wwtv.setting.base.scan.model;

import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;

public class SatelliteInfo extends MtkTvDvbsConfigInfoBase {

	protected String name = "";
	protected String type = "";
	protected boolean enable = false;

	public int dvbsScanMode = DVBSSettingsInfo.NETWORK_SCAN_MODE; 
    public int dvbsScanType = DVBSSettingsInfo.CHANNELS_ALL; 
    public int dvbsStoreType = DVBSSettingsInfo.CHANNELS_ALL;
	
	public SatelliteInfo() {
		super();
	}

	public SatelliteInfo(MtkTvDvbsConfigInfoBase base) {
		super();
		this.satlRecId = base.getSatlRecId();
		this.mask = base.getMask();
		this.lnbType = base.getLnbType();
		this.lnbLowFreq = base.getLnbLowFreq();
		this.lnbHighFreq = base.getLnbHighFreq();
		this.lnbSwitchFreq = base.getLnbSwitchFreq();
		this.diseqcType = base.getDiseqcType();
		this.port = base.getPort();
		this.m22k = base.getM22k();
		this.toneBurst = base.getToneBurst();
		this.lnbPower = base.getLnbPower();
		this.position = base.getPosition();
		this.userBand = base.getUserBand();
		this.bandFreq = base.getBandFreq();
		this.mduType = base.getMduType();
		this.diseqcTypeEx = base.getDiseqcTypeEx();
		this.portEx = base.getPortEx();
		this.subDiseqcType = base.getSubDiseqcType();
		this.subPort = base.getSubPort();
		this.subM22k = base.getSubM22k();
		this.subToneBurst = base.getSubToneBurst();
		this.subLnbPower = base.getSubLnbPower();
		this.subPosition = base.getSubPosition();
		this.subUserBand = base.getSubUserBand();
		this.subBandFreq = base.getSubBandFreq();
		this.subMduType = base.getSubMduType();
		this.orbPos = base.getOrbPos();
		this.satName = base.getSatName();
		this.motorType=base.getMotorType();
        this.motorPosition=base.getMotorPosition();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getEnable() {
		return enable;
	}

	public void setEnabled(boolean enable) {
		this.enable = enable;
	}

}
