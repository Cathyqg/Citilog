package com.wiscom.bean;

public class AlertDetail {
	
	private String camId;
	private String typeId;
	private int lanNo;
	private int alertSta;
	private String isConfirm;
	private String startTime;
	private String endTime;
	private String collectedTime;
	private String url;
	
	
	public AlertDetail(String camId, String typeId, int lanNo, int alertSta, String isConfirm, String startTime,
			String endTime, String collectedTime, String url) {
		this.camId = camId;
		this.typeId = typeId;
		this.lanNo = lanNo;
		this.alertSta = alertSta;
		this.isConfirm = isConfirm;
		this.startTime = startTime;
		this.endTime = endTime;
		this.collectedTime = collectedTime;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCamId() {
		return camId;
	}

	public void setCamId(String camId) {
		this.camId = camId;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public int getLanNo() {
		return lanNo;
	}

	public void setLanNo(int lanNo) {
		this.lanNo = lanNo;
	}

	public int getAlertSta() {
		return alertSta;
	}

	public void setAlertSta(int alertSta) {
		this.alertSta = alertSta;
	}

	public String getIsConfirm() {
		return isConfirm;
	}

	public void setIsConfirm(String isConfirm) {
		this.isConfirm = isConfirm;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getCollectedTime() {
		return collectedTime;
	}

	public void setCollectedTime(String collectedTime) {
		this.collectedTime = collectedTime;
	}
	
	
	
	 

}
