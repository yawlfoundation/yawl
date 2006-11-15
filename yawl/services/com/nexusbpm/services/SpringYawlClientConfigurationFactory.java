package com.nexusbpm.services;

public class SpringYawlClientConfigurationFactory implements YawlClientConfigurationFactory {

	private YawlClientConfiguration configuration;

	/* (non-Javadoc)
	 * @see com.nexusbpm.services.YawlClientConfigurationFactory#getConfiguration()
	 */
	public YawlClientConfiguration getConfiguration() {
		return configuration;
	}

	/* (non-Javadoc)
	 * @see com.nexusbpm.services.YawlClientConfigurationFactory#setConfiguration(com.nexusbpm.services.YawlClientConfiguration)
	 */
	public void setConfiguration(YawlClientConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public void saveConfiguration()  {
		throw new java.lang.UnsupportedOperationException("Spring version does not save properties");
	}
	
}
