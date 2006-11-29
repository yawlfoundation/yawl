/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.services;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

public class LocalClientConfiguration extends BootstrapConfiguration {

	private String appContextFileName;

	private String propertiesFileName;

	private String YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_KEY = "yawl.environment.propertyfilename";

	public LocalClientConfiguration(String appContextFileName, String propertiesFileName) {
		super();
		this.setPropertiesFileName(propertiesFileName);
		this.setAppContextFileName(appContextFileName);
	}

	public void refresh() {
		synchronized (applicationContext) {
			applicationContext = new ClassPathXmlApplicationContext(
					appContextFileName);
		}
	}

	public String getAppContextFileName() {
		return appContextFileName;
	}

	public void setAppContextFileName(String appContextFileName) {
		this.appContextFileName = appContextFileName;
		applicationContext = new ClassPathXmlApplicationContext(
				appContextFileName);
	}

	public String getPropertiesFileName() {
		return propertiesFileName;
	}

	public void setPropertiesFileName(String propertiesFileName) {
		System.setProperty(YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_KEY,
				propertiesFileName);
		this.propertiesFileName = propertiesFileName;
	}
}
