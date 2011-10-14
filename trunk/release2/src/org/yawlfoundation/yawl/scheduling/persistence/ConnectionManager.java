/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.scheduling.persistence;

import org.apache.log4j.Logger;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConnectionManager
{
	private static Logger logger = Logger.getLogger(ConnectionManager.class);
	private static ConnectionManager instance = null;
	private PGPoolingDataSource dataSource = null;
	private Properties props = new Properties();

	private static String dataSourceName;
	private static String serverName;
	private static Integer portNumber;
	private static String databaseName;
	private static String user;
	private static String password;
	private static Integer initialConnections;
	private static Integer loginTimeout;
	private static Integer maxConnections;
	private static Integer prepareThreshold;
	private static boolean ssl;

	private String propertyFile = "postgres83.properties";
	private String relativePath = "/";

	private static ConnectionManager getInstance() throws SQLException
	{
		if (instance == null)
		{
			instance = new ConnectionManager();
		}
		return instance;
	}

	private ConnectionManager() throws SQLException
	{
		try
		{
			props.load(this.getClass().getResourceAsStream(relativePath + propertyFile));
		}
		catch (IOException e)
		{
			throw new SQLException("cannot load property file: " + propertyFile, e);
		}

		dataSourceName = getProperty("jdbc.dataSourceName");
		serverName = getProperty("jdbc.serverName");
		portNumber = new Integer(getProperty("jdbc.portNumber"));
		databaseName = getProperty("jdbc.databaseName");
		user = getProperty("jdbc.user");
		password = getProperty("jdbc.password");
		initialConnections = new Integer(getProperty("jdbc.initialConnections"));
		loginTimeout = new Integer(getProperty("jdbc.loginTimeout"));
		maxConnections = new Integer(getProperty("jdbc.maxConnections"));
		prepareThreshold = new Integer(getProperty("jdbc.prepareThreshold"));
		ssl = getProperty("jdbc.ssl").equals("true");
	}

	private synchronized PGPoolingDataSource getPoolDataSource() throws SQLException
	{
		if (dataSource == null)
		{
			dataSource = new PGPoolingDataSource();
			dataSource.setDataSourceName(dataSourceName);
			dataSource.setServerName(serverName);
			dataSource.setDatabaseName(databaseName);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			dataSource.setMaxConnections(maxConnections);
			dataSource.setPortNumber(portNumber);
			dataSource.setInitialConnections(initialConnections);
			dataSource.setLoginTimeout(loginTimeout);
			dataSource.setMaxConnections(maxConnections);
			dataSource.setPrepareThreshold(prepareThreshold);
			dataSource.setSsl(ssl);
			// source.setSslfactory(null);
			// source.setLogWriter(null);
		}
		return dataSource;
	}

	private String getProperty(String key)
	{
		return props.getProperty(key, props.getProperty(key));
	}

	public static Connection getConnection() throws SQLException
	{
		Connection connection = null;
		try
		{
			connection = getInstance().getPoolDataSource().getConnection();
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			throw new SQLException(e.toString());
		}
		connection.setAutoCommit(true);
		return connection;
	}

	public static void close(ResultSet resultSet, Statement statement, Connection connection)
	{
		String details = null;
		if (resultSet != null)
		{
			try
			{
				resultSet.close();
			}
			catch (SQLException e)
			{
				details = resultSet.toString();
				logger.warn("Failed to close result set: " + details, e);
			}
		}

		if (statement != null)
		{
			try
			{
				statement.close();
			}
			catch (SQLException e)
			{
				details = statement.toString();
				logger.warn("Failed to close callable statement: " + details, e);
			}
		}
		if (connection != null)
		{
			try
			{
				 connection.close();
			}
			catch (SQLException e)
			{
				details = connection.toString();
				logger.warn("Failed to return connection to pool: " + details, e);
			}			
		}
	}
}
