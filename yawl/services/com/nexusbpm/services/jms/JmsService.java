/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.jms.config.Configuration;
import org.exolab.jms.config.ConfigurationReader;
import org.exolab.jms.server.JmsServer;
import org.exolab.jms.server.ServerException;
import org.exolab.jms.service.ServiceException;
import org.exolab.jms.tools.admin.OnlineConnection;

/**
 * Provides an OpenJms server inside a servlet container 
 */
public class JmsService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected Configuration config;

	protected void createDatabase() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException,
			IOException {
		String url = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getUrl();
		String user = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getUser();
		String passwd = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getPassword();
		String driver = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getDriver();
		Class.forName(driver).newInstance();
		java.sql.Connection conn = DriverManager.getConnection(url, user,
				passwd);
		String setupDb = readStreamAsString(this.getClass()
				.getResourceAsStream("/create_hsql.sql"));
		PreparedStatement s = conn.prepareStatement(setupDb);
		s.execute();
	}

	protected void destroyDatabase() throws SQLException {
		String url = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getUrl();
		String user = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getUser();
		String passwd = config.getDatabaseConfiguration()
				.getRdbmsDatabaseConfiguration().getPassword();
		java.sql.Connection conn = DriverManager.getConnection(url, user,
				passwd);
		conn.createStatement().execute("SHUTDOWN");
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			startServer();
			log("started jms init");
		} catch (Exception e) {
			log("failed to start jms init", e);
		}
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	public final void startServer() throws NamingException, ServiceException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, IOException,
			MarshalException, ValidationException {
		config = ConfigurationReader.read(this.getClass().getResourceAsStream(
				"/openjms.xml"));
		if (config.getDatabaseConfiguration().getRdbmsDatabaseConfiguration()
				.getUrl().startsWith("jdbc:hsqldb:mem:")) {
			createDatabase();
		}
		JmsServer server = new JmsServer(config) {
			@Override
			public void init() throws NamingException, ServiceException {
				try {
					registerServices();
					getServices().start();
				} catch (ServiceException exception) {
					throw new ServerException("Failed to start services",
							exception);
				}
			}
		};
		server.init();
	}

	public void stopServer() throws Exception {
		OnlineConnection oc = new OnlineConnection(null, config);
		oc.stopServer();
		if (config.getDatabaseConfiguration().getRdbmsDatabaseConfiguration()
				.getUrl().startsWith("jdbc:hsqldb:mem:")) {
			destroyDatabase();
		}
	}

	public static void main(String[] args) {
		JmsService s = new JmsService();
		try {
			s.startServer();
			s.stopServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
