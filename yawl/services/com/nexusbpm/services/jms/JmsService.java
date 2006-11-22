/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.jms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	protected static Configuration config;

	private String configPath;
	private String sqlPath;
	
	private static JmsServer server;

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
		String setupDb = readStreamAsString(sqlPath);
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
	public void init(ServletConfig sc) throws ServletException {
		super.init(sc);
		System.out.println("starting JmsService init");
		configPath = this.getServletContext().getRealPath("/openjms.xml");
		sqlPath = this.getServletContext().getRealPath("/sql/create_hsql.sql");
		try {
			startServer();
			System.out.println("JmsService init completed successfully");
		} catch (Throwable ex) {
			System.out.println("JmsService init failed due to "
					+ ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("jmsServer", server);
		request.setAttribute("configuration", config);
		getServletConfig().getServletContext().getRequestDispatcher(
				"/jmsStatus.jsp").forward(request, response);
	}

	public final void startServer() throws NamingException, ServiceException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, IOException,
			MarshalException, ValidationException {
		config = ConfigurationReader.read(configPath);
		if (config.getDatabaseConfiguration().getRdbmsDatabaseConfiguration()
				.getUrl().startsWith("jdbc:hsqldb:mem:")) {
			createDatabase();
		}
		server = new JmsServer(config) {
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

	private static String readStreamAsString(String url)
			throws java.io.IOException {
		InputStream stream = new FileInputStream(url);
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

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public void setSqlPath(String sqlPath) {
		this.sqlPath = sqlPath;
	}

}
