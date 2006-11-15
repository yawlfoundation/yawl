package com.nexusbpm.services;

import java.io.IOException;

public interface YawlClientConfigurationFactory {

	YawlClientConfiguration getConfiguration();

	void setConfiguration(YawlClientConfiguration configuration);

	void saveConfiguration() throws IOException ;
	
}