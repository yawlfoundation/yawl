/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.*;
import org.yawlfoundation.yawl.scheduling.Case;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.fail;


/**
 * @author jku
 * 
 */
public class DataMapperRupTest
{
	private static final Logger logger = LogManager.getLogger(DataMapperRupTest.class);

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
	 * {@link DataMapper#saveRup(Case)}
	 * .
	 */
	@Test
	public void testSaveRup_()
	{
		Element rupElement = null;
		Element caseIdElem = null;
		Document rup = null;
		Case case_ = null;
		String caseId = "14"; // make sure the case Id really exists in the YAWL database
		String savedBy = "emil";
		String caseName = "emils case";
		String caseDescription = "my little case description";
		DataMapper dataMapper = null;

		try
		{
			rupElement = new Element(Constants.XML_RUP);
			rup = new Document(rupElement);
			caseIdElem = XMLUtils.getElement(rup, Constants.XML_RUP + "/" + Constants.XML_CASEID);
			if (caseIdElem == null)
			{
				caseIdElem = new Element(Constants.XML_CASEID);
				rup.getRootElement().addContent(caseIdElem);
			}
			caseIdElem.setText(caseId);
			case_ = new Case(caseId, caseName, caseDescription, rup);
            case_.setSavedBy(savedBy);
			dataMapper = new DataMapper();
			dataMapper.saveRup(case_);
			// logger.info("Saved RUP to the database");
		}
		catch (Exception e)
		{
			logger.debug(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link DataMapper#getRupActivityTypes}
	 * .
	 */
	@Test
	public void testGetRupActivityTypes()
	{
		DataMapper dataMapper = null;
		String activityName = "AnesthesiologicalAssessment";
		String activityType = null;
		List<String> activityTypeList = null;
		
		try
		{
			dataMapper = new DataMapper();	
			activityTypeList = dataMapper.getRupActivityTypes(activityName);
			Iterator<String> iterator = activityTypeList.iterator();
			while (iterator.hasNext())
			{
				activityType = (String) iterator.next();
				logger.debug(activityType);
			}
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link DataMapper#getRupNodes}
	 * .
	 */
	@Test
	public void testGetRupNodes()
	{
		DataMapper dataMapper = null;
		List<List<Element>> nodes = null;
		List<Element> node = null;
		
		try
		{
			dataMapper = new DataMapper();			
			nodes = dataMapper.getRupNodes("AnesthesiologicalAssessment", "SOU","Reservation");
		
			Iterator<List<Element>> iterator = nodes.iterator();
			while (iterator.hasNext())
			{
				node = (List<Element>) iterator.next();
				Iterator<Element> iter = node.iterator();
				while (iter.hasNext())
				{
					Element element = iter.next();
					logger.debug(element.toString());
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			fail(e.toString());
		}
	}
	
	/**
	 * Test method for
	 * {@link DataMapper#getRupByCaseId}
	 * .
	 */
	@Test
	public void testGetRupByCaseId()
	{
		DataMapper dataMapper = null;
		List<Case> cases = null;
		Case case_ = null;

		try
		{
			dataMapper = new DataMapper();			
			cases = dataMapper.getRupByCaseId(caseId);
			case_ = cases.get(0);
			logger.debug("caseId: " + case_.getCaseId());
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link DataMapper#updateRup}
	 * .
	 */
	@Test
	public void testUpdateRup()
	{
		String caseId = "14"; 
		DataMapper dataMapper = null;
	
		try
		{
			dataMapper = new DataMapper();
			dataMapper.updateRup(caseId, false);
		}
		catch (Exception e)
		{
			logger.debug(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link DataMapper#getRupsByActivity}
	 * .
	 */
	@Test
	public void testGetRupsByActivityl()
	{
		DataMapper dataMapper = null;
		String activityName = "AnesthesiologicalAssessment";
		List<Case> cases = null;
		Case case_ = null;
			
		try
		{
			dataMapper = new DataMapper();	
			cases = dataMapper.getRupsByActivity(activityName);
			logger.debug("size="+cases.size());
			case_ = cases.get(0);
			logger.debug(case_.toString());
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			fail(e.toString());
		}
	}

	/**
	 * Test method for
	 * {@link DataMapper#getRupsByInterval}
	 * .
	 */
	@Test
	public void testGetRupsByInterval()
	{
		DataMapper dataMapper = null;
		List<String> excludedCaseIds = null;
		List<Case> cases = null;
		Case case_ = null;
		
		try
		{
			dataMapper = new DataMapper();	
			Date from = new Date();
			Date to = new Date();
			excludedCaseIds = new ArrayList<String>();
			excludedCaseIds.add("5");
			excludedCaseIds.add("100");
			cases = dataMapper.getRupsByInterval(from, to, excludedCaseIds, true);
			logger.debug("size="+cases.size());
			case_ = cases.get(0);
			logger.debug(case_.toString());
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			fail(e.toString());
		}
	}
}
