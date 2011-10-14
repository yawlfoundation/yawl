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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yawlfoundation.yawl.scheduling.Mapping;


/**
 * @author jku
 * 
 */
public class DataMapperMappingTest
{
	private static final Logger logger = Logger.getLogger(DataMapperMappingTest.class);

	private Mapping mapping1 = null;
	private Mapping mapping2 = null;
	private String caseId = null;

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
		mapping1 = new Mapping("dummy 1", 123, "state X");
		mapping2 = new Mapping("dummy 2", 987, "state Y");
		caseId = "524";
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.yawlfoundation.yawl.scheduling.persistence.DataMapper#saveMapping(org.yawlfoundation.yawl.scheduling.Mapping)}
	 * .
	 */
	@Test
	public void testSaveMapping()
	{
		try
		{
			logger.debug("testSaveMapping()");
			DataMapper dataMapper = new DataMapper();
			dataMapper.saveMapping(mapping2);
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link org.yawlfoundation.yawl.scheduling.persistence.DataMapper#removeMapping(org.yawlfoundation.yawl.scheduling.Mapping)}
	 * .
	 */
	@Test
	public void testRemoveMapping()
	{
		try
		{
			logger.debug("testRemoveMapping()");
			DataMapper dataMapper = new DataMapper();
			dataMapper.removeMapping(mapping2);
		}
		catch (Exception e)
		{
			logger.debug(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for {@link
	 * org.yawlfoundation.yawl.scheduling.persistence.DataMapper#unlockMappings(
	 * ArrayList<Mapping>)} .
	 */
	@Test
	public void testUnlockMapping()
	{
		try
		{
			logger.debug("testUnlockMapping()");
			DataMapper dataMapper = new DataMapper();
			ArrayList<Mapping> mappings = new ArrayList<Mapping>();
			mappings.add(mapping1);
			mappings.add(mapping2);
			dataMapper.unlockMappings(mappings);
		}
		catch (Exception e)
		{
			logger.debug(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link org.yawlfoundation.yawl.scheduling.persistence.DataMapper#getMappings()}
	 * .
	 */
	@Test
	public void testGetMappings()
	{
		ArrayList<Mapping> mappings = null;

		try
		{
			logger.debug("testGetMappings()");
			DataMapper dataMapper = new DataMapper();
			mappings = dataMapper.getMappings();
			// logger.info(mappings.toString());
		}
		catch (Exception e)
		{
			logger.debug(e.toString());
			fail(e.toString());
		}
	}
}
