/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class QuartzSchema {

	public static void createIfMissing() throws ClassNotFoundException, SQLException,
			IOException {
		Class.forName(System.getProperty("org.quartz.dataSource.yawlDS.driver"));
		Connection c = DriverManager.getConnection(System
				.getProperty("org.quartz.dataSource.yawlDS.URL"), System
				.getProperty("org.quartz.dataSource.yawlDS.user"), System
				.getProperty("org.quartz.dataSource.yawlDS.password"));
		String t = readStreamAsString(SchedulerService.class
				.getResourceAsStream("scripts/"
						+ System.getProperty("org.quartz.dataSource.yawlDS.runScript")));
			Statement s = c.createStatement();
			s.execute(t);
			c.commit();
			s.close();
			c.close();
	}

	public static void delete() {
		throw new UnsupportedOperationException("schema delete is not yet supported");
	}
	
	private static String readStreamAsString(InputStream stream)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

}
