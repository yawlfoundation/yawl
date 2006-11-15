package com.nexusbpm.services;

import java.io.IOException;

public class YawlClientConfigurationBean implements YawlClientConfiguration {

	private String jmsUri;
	private String serverUri;
	private String quartzUri;
	
	public void load() throws IOException {
		//do nothing
	}

	public void save() throws IOException {
		throw new java.lang.UnsupportedOperationException("default config does not support save");
	}

	public String getJmsUri() {
		return jmsUri;
	}

	public void setJmsUri(String jmsUri) {
		this.jmsUri = jmsUri;
	}

	public String getQuartzUri() {
		return quartzUri;
	}

	public void setQuartzUri(String quartzUri) {
		this.quartzUri = quartzUri;
	}

	public String getServerUri() {
		return serverUri;
	}

	public void setServerUri(String serverUri) {
		this.serverUri = serverUri;
	}

}
