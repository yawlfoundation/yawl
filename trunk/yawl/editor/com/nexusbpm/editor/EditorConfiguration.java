/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class EditorConfiguration {

	private static EditorConfiguration INSTANCE;
	private String serverUri;
	private String jmsUri;
	private String quartzUri;
	
	
	public static synchronized EditorConfiguration getInstance() throws IOException {
		if (INSTANCE == null) {
			INSTANCE = new EditorConfiguration();
		}
		return INSTANCE;
	}
	
	public void persist() throws IOException {
		Properties p = new Properties();
		InputStream is = new FileInputStream("editor.properties");
		p.load(is);
		p.setProperty("nexuseditor.serverUri", serverUri);
		p.setProperty("java.naming.provider.url", jmsUri);
		p.setProperty("nexuseditor.quartzUri", quartzUri);
		OutputStream os = new FileOutputStream("editor.properties");
		p.store(os, "Nexus Editor Configuration");
		
	}

	public void load() throws IOException {
		Properties p = new Properties();
		InputStream is = new FileInputStream("editor.properties");
		p.load(is);
		serverUri = p.getProperty("nexuseditor.serverUri");
		jmsUri = p.getProperty("java.naming.provider.url");
		quartzUri = p.getProperty("nexuseditor.quartzUri");
	}
	
	private EditorConfiguration() throws IOException {
		load();
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
