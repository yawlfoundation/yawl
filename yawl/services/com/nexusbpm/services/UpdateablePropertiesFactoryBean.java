package com.nexusbpm.services;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.io.Resource;

public class UpdateablePropertiesFactoryBean extends PropertyOverrideConfigurer {

	protected Resource locationShadow;
	protected Properties propertiesShadow;
	
	@Override
	public void setLocation(Resource arg0) {
		super.setLocation(arg0);
		locationShadow = arg0;
	}

	@Override
	protected void loadProperties(Properties arg0) throws IOException {
		super.loadProperties(arg0);
		propertiesShadow = arg0;
	}

	public Properties getProperties() throws IOException{
		return (Properties) propertiesShadow;
	}
	
	public void save() throws IOException{
		getProperties().store(new FileOutputStream(locationShadow.getFile()), "Persisted at runtime");
	}
	
}
