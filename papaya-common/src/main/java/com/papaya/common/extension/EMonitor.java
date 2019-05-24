package com.papaya.common.extension;

/**
 * Method monitoring type
 * 
 * @author shellpo shih
 * @version 1.0
 */
public enum EMonitor {
	Online("上线"), Offline("下线");

	private String title;

	private EMonitor(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}
