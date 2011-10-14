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

/**
 * 
 */
package org.yawlfoundation.yawl.scheduling.persistence;

import static org.junit.Assert.*;
//import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
//import java.sql.SQLException;
import java.sql.Statement;
//import java.util.Properties;


import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jku
 *
 */
public class ConnectionManagerTest
{
	private static final Logger logger = Logger.getLogger(ConnectionManagerTest.class);
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link org.yawlfoundation.yawl.scheduling.persistence.ConnectionManager#getConnection()}.
	 */
	@Test
	public void testGetConnection()
	{
		Connection connection = null;
		logger.debug("testGetConnection()");
		try
		{
			connection = ConnectionManager.getConnection();	
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select count(1) from MAPPING");
			while (rs.next())
			{
				int count = rs.getInt(1);
				System.out.println("count = " + count);
				logger.debug("count="+count);				
			}
		}
		catch (Exception e)
		{
			fail(e.toString());
		}
		ConnectionManager.close(null, null, connection);
//		logger.debug("Checkpoint 200");
	}

	/**
	 * Test method for {@link org.yawlfoundation.yawl.scheduling.persistence.ConnectionManager#close(java.sql.ResultSet, java.sql.Statement, java.sql.Connection)}.
	 */
	@Test
	public void testClose()
	{
		fail("Not yet implemented");
	}

}
