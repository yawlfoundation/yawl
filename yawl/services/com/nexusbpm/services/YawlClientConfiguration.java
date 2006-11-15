package com.nexusbpm.services;

import java.io.IOException;

public interface YawlClientConfiguration {

	public abstract void save() throws IOException;

	public abstract void load() throws IOException;

	public abstract String getJmsUri();

	public abstract void setJmsUri(String jmsUri);

	public abstract String getQuartzUri();

	public abstract void setQuartzUri(String quartzUri);

	public abstract String getServerUri();

	public abstract void setServerUri(String serverUri);

}