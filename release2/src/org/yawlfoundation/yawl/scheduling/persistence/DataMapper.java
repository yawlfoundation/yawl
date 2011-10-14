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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.yawlfoundation.yawl.scheduling.Case;
import org.yawlfoundation.yawl.scheduling.Mapping;
import org.yawlfoundation.yawl.scheduling.util.Utils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class encapsulates the database. It exclusively uses stored procedures to
 * access the persistence layer, thereby maintaining the concept of logical data
 * independence. As long as the signature of the stored procedure and their
 * semantics do not change, data source and application code may be developed
 * independently from each other.
 * 
 * Specifically, DataMapper facilitates the persistence of resource utilisation
 * plans (RUPs) and of mappings.
 * 
 * A mapping is a bijective function that assigns to every YAWL workitem Id a
 * unique request key, where the latter is used by the custom service. In
 * effect, the mapping table binds the operations of the Scheduling Service to
 * the YAWL engine in a non-ambiguous way.
 * 
 * @author tbe, jku
 * @version $Id$
 * 
 */
public class DataMapper {
	private static final Logger _log = Logger.getLogger(DataMapper.class);

	public DataMapper()	{ }

	/**
	 * Saves or updates a mapping to the database for recovery of failed YAWL
	 * requests
	 * 
	 * @param mapping
	 *           object
	 * 
	 * @return ArrayList<Channel>
	 * @throws SQLException
	 */
	public void saveMapping(Mapping mapping) throws SQLException {
		CallableStatement callableStatement = null;
		Connection connection = null;

		try	{
			connection = ConnectionManager.getConnection();
			String callString = "{? = call pkg_mapping.save_( ? , ? , ? )}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.setString(2, mapping.getWorkItemId());
			callableStatement.setInt(3, mapping.getRequestKey());
			callableStatement.setString(4, mapping.getWorkItemStatus());
			int rowCount = callableStatement.executeUpdate();
			_log.debug("Saved (updated) " + rowCount + " mappings to (in) the database");
		}
		catch (SQLException e) {
			throw new SQLException("Failed to save mapping for work item ID " +
                    mapping.getWorkItemId(), e);
		}
		finally	{
			ConnectionManager.close(null, callableStatement, connection);
		}
	}


	/**
	 * removes a mapping from database
	 * 
	 * @param mapping
	 *           Object
	 * @return ArrayList<Channel>
	 * @throws SQLException
	 */
	public void removeMapping(Mapping mapping) throws SQLException {
		removeMapping(mapping.getWorkItemId(), mapping.getRequestKey());
	}


	/**
	 * Removes all mappings with specified "workItemId" and "requestKey"
	 * 
	 * @author tbe, jku
	 * @param workItemId - YAWL's unique work item identifier
	 * @param requestKey - The service's unique rqeuest identifier
	 * @throws SQLException
	 */
	public void removeMapping(String workItemId, Integer requestKey) throws SQLException {
		CallableStatement callableStatement = null;
		Connection connection = null;

		try	{
			connection = ConnectionManager.getConnection();
			String callString = "{? = call pkg_mapping.remove_( ? , ? )}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.setString(2, workItemId);
			callableStatement.setInt(3, requestKey);
			int rowCount = callableStatement.executeUpdate();
			_log.debug("Removed " + rowCount + " mappings from the database");
		}
		catch (Exception e)	{
			String message = "Failed to removing mapping " + workItemId +
                    ", request key = " + requestKey;
			throw new SQLException(message + e.getMessage(), e);
		}
		finally	{
			ConnectionManager.close(null, callableStatement, connection);
		}
	}

	/**
	 * unlock a mapping
	 * 
	 * @param mapping
	 *           object
	 * @return ArrayList<Channel>
	 * @throws SQLException
	 */
	public void unlockMapping(Mapping mapping)
	{
		if (mapping != null)
		{
			ArrayList<Mapping> mappings = new ArrayList<Mapping>();
			mappings.add(mapping);
			unlockMappings(mappings);
		}
	}

	/**
	 * unlock mappings
	 * 
	 * @param mappings
	 *           object
	 * @return ArrayList<Channel>
	 */
	public void unlockMappings(ArrayList<Mapping> mappings)
	{
		if (mappings == null || mappings.isEmpty())
		{
			return;
		}

		CallableStatement callableStatement = null;
		Connection connection = null;
		String[] workItemIds = new String[mappings.size()];

		for (int i = 0; i < workItemIds.length; i++)
		{
			workItemIds[i] = mappings.get(i).getWorkItemId();
		}

		try
		{
			connection = ConnectionManager.getConnection();
			Array array = connection.createArrayOf("varchar", workItemIds);
			String callString = "{? = call pkg_mapping.unlock_( ? )}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.setArray(2, array);
			int rowCount = callableStatement.executeUpdate();
			_log.debug("Unlocked " + rowCount + " mappings in the database");
		}
		catch (Throwable e)
		{
			_log.error("Failed to prepare callable statement", e);
		}
		finally
		{
			ConnectionManager.close(null, callableStatement, connection);
		}
	}

	/**
	 * Get all mappings from the database
	 * 
	 * @return ArrayList<Mapping>
	 * @throws SQLException
	 */
	public ArrayList<Mapping> getMappings() throws SQLException
	{
		ArrayList<Mapping> allMappings = new ArrayList<Mapping>();
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			//
			// We must be inside a transaction for cursors to work.
			//
			connection.setAutoCommit(false);
			String callString = "{ ? = call pkg_mapping.get_() }";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);
			while (resultSet.next())
			{
				String workItemId = resultSet.getString(1);
				int requestKey = resultSet.getInt(2);
				String workItemStatus = resultSet.getString(4);
				allMappings.add(new Mapping(workItemId, requestKey, workItemStatus));
			}
			int size = allMappings.size();
			_log.debug("Got " + size + " mappings from the database");

			// Due to a limitation of the Postgres JDBC 4 driver, we must
			// separately lock the mappings by calling the stored function
			// "pkg_mapping.lock_()".
			if (size > 0)
			{
				String[] workItemIds = new String[size];
				for (int i = 0; i < workItemIds.length; i++)
				{
					workItemIds[i] = allMappings.get(i).getWorkItemId();
				}
				Array array = connection.createArrayOf("varchar", workItemIds);
				callString = "{ ? = call pkg_mapping.lock_(?) }";
				callableStatement = connection.prepareCall(callString);
				callableStatement.registerOutParameter(1, Types.INTEGER);
				callableStatement.setArray(2, array);
				int rowCount = callableStatement.executeUpdate();
				_log.info("Locked " + rowCount + " mappings in the database");
			}
			connection.commit();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			_log.error(e.toString());
			throw new SQLException("Failed to get mappings: ", e);
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
		return allMappings;
	}

	/**
	 * Saves a RUP to the database using the JDBC4 SQLXML interface
	 * 
	 * @author jku
	 * @param case_
	 * @param savedBy
	 * @throws JDOMException
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	public void saveRup(Case case_, String savedBy) throws JDOMException, SQLException
	{
		Connection connection = null;
		CallableStatement callableStatement = null;

		try
		{
			String caseName = case_.getCaseName();
			String caseDescription = case_.getCaseDescription();
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			connection = ConnectionManager.getConnection();
			String callString = "{? = call pkg_utilisationplan.save_(?, ?, ?, ?, ?, ?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.setString(2, case_.getCaseId());
			SQLXML rupxml = connection.createSQLXML();
			rupxml.setString(Utils.document2String(case_.getRUP(), true));
			callableStatement.setSQLXML(3, rupxml);
			callableStatement.setString(4, caseName);
			callableStatement.setString(5, caseDescription);
			callableStatement.setString(6, savedBy);
			callableStatement.setTimestamp(7, currentTime);
			callableStatement.executeUpdate();
			int rowCount = callableStatement.getInt(1);
			_log.info("Saved (updated) " + rowCount + " RUPs to (in) the database");
		}
		catch (Exception e)
		{
			_log.error("error: ", e);
		}
		finally
		{
			ConnectionManager.close(null, callableStatement, connection);
		}
	}

	/**
	 * Get all RUPs from the database that start after "from" and end before
	 * "to". Allow for specifying a set of Yawl case Ids that are to be excluded
	 * from the result. Also, it is possible to select only RUPs that are active.
	 * 
	 * @author jku, tbe
	 * @param from
	 * @param to
	 * @param yCaseIdsToExclude
	 * @param activeOnly
	 * @return List<Case> all cases with RUPs that meet the selection criteria
	 * @throws SQLException
	 */
	public List<Case> getRupsByInterval(Date from, Date to, List<String> yCaseIdsToExclude, boolean activeOnly)
			throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		List<Case> yCases = null;

		int size = 0;
		if (yCaseIdsToExclude != null)
		{
			size = yCaseIdsToExclude.size();
		}
		String[] excludedIds = new String[size];
		for (int i = 0; i < excludedIds.length; i++)
		{
			excludedIds[i] = yCaseIdsToExclude.get(i);
		}

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			//
			// We must be inside a transaction for cursors to work.
			//
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_by_interval(?,?,?,?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			String minTimestamp = Utils.date2String(from, Utils.DATETIME_PATTERN_XML);
			String maxTimestamp = Utils.date2String(to, Utils.DATETIME_PATTERN_XML);
			callableStatement.setString(2, minTimestamp);
			callableStatement.setString(3, maxTimestamp);
			Array arrayOfExcludedIds = connection.createArrayOf("varchar", excludedIds);
			callableStatement.setArray(4, arrayOfExcludedIds);
			callableStatement.setBoolean(5, activeOnly);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			yCases = new ArrayList<Case>();
			while (resultSet.next())
			{
				String yCaseId = resultSet.getString("case_id");
				SQLXML rupxml = resultSet.getSQLXML("rupxml");
				String ySpecName = resultSet.getString("spec_name");
				String ySpecDescription = resultSet.getString("spec_description");
				Document rup = Utils.string2Document(rupxml.getString());
				yCases.add(new Case(yCaseId, ySpecName, ySpecDescription, rup));
			}
		}
		catch (SQLException e)
		{
			_log.error(e.toString());
		}
		catch (Exception e)
		{
			_log.error(e.toString());
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
		return yCases;
	}


	public List<Case> getRupByCaseId(String caseId) throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		SQLXML rupxml = null;
		String specificationName = null;
		String specificationDescription = null;
		List<Case> cases = new ArrayList<Case>();

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			//
			// We must be inside a transaction for cursors to work.
			//
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_by_caseid(?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.setString(2, caseId);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);
			while (resultSet.next())
			{
				caseId = resultSet.getString("case_id");
				rupxml = resultSet.getSQLXML("rupxml");
				specificationName = resultSet.getString("spec_name");
				specificationDescription = resultSet.getString("spec_description");
			}
			String rupstring = rupxml.getString();
			rupstring = rupstring.trim().replaceFirst("^([\\W]+)<","<");
			Document rup = Utils.string2Document(rupstring);
			cases.add(new Case(caseId, specificationName, specificationDescription, rup));
		}
		catch (Exception e)
		{
			_log.error(e.toString());
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
		return cases;
	}

	/**
	 * @author jku, tbe
	 * @param
	 * @return
	 * @throws SQLException
	 */
	public List<Case> getAllRups() throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		List<Case> cases = new ArrayList<Case>();

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			//
			// We must be inside a transaction for cursors to work, hence we must
			// set auto commit to FALSE.
			//
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_all()}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			while (resultSet.next())
			{
				String caseId = resultSet.getString("case_id");
				SQLXML rupxml = resultSet.getSQLXML("rupxml");
				Document rup = Utils.string2Document(rupxml.getString());
				String specificationName = resultSet.getString("spec_name");
				String specificationDescription = resultSet.getString("spec_description");
				cases.add(new Case(caseId, specificationName, specificationDescription, rup));
			}
			return cases;
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
	}

	/**
	 * @author jku, tbe
	 * @param timestamp
	 * @return
	 * @throws SQLException
	 */
	public List<Case> getActiveRups(String timestamp) throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		List<Case> cases = new ArrayList<Case>();

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			//
			// We must be inside a transaction for cursors to work, hence we must
			// set auto commit to FALSE.
			//
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_active(?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.setString(2, timestamp);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			while (resultSet.next())
			{
				String caseId = resultSet.getString("case_id");
				SQLXML rupxml = resultSet.getSQLXML("rupxml");
				Document rup = Utils.string2Document(rupxml.getString());
				String specificationName = resultSet.getString("spec_name");
				String specificationDescription = resultSet.getString("spec_description");
				cases.add(new Case(caseId, specificationName, specificationDescription, rup));
			}
			return cases;
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
	}

	/**
	 * @author jku, tbe
	 * @param activityName
	 * @param activityType
	 * @param nodeName
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public List<List<Element>> getRupNodes(String activityName, String activityType, String nodeName)
			throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		List<List<Element>> nodesList = new ArrayList<List<Element>>();

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			//
			// We must be inside a transaction for cursors to work, hence we must
			// set auto commit to FALSE.
			//
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_nodes(?,?,?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.setString(2, activityName);
			callableStatement.setString(3, activityType);
			callableStatement.setString(4, nodeName);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			while (resultSet.next())
			{
				SQLXML sqlxml = resultSet.getSQLXML("nodexml");
				List<Element> document = Utils.string2Elements(sqlxml.getString());
				nodesList.add(document);
			}
			return nodesList;
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
	}

	/**
	 * @author jku, tbe
	 * @param activityName
	 * @return
	 * @throws SQLException
	 */
	public List<Case> getRupsByActivity(String activityName) throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		List<Case> cases = null;

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			// -----------------------------------------------------------------------
			// We must be inside a transaction for cursors to work, hence auto
			// commit must be set to FALSE.
			// -----------------------------------------------------------------------
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_by_activity(?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.setString(2, activityName);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			cases = new ArrayList<Case>();
			while (resultSet.next())
			{
				String caseId = resultSet.getString("case_id");
				SQLXML rupxml = resultSet.getSQLXML("rupxml");
				String name = resultSet.getString("spec_name");
				String description = resultSet.getString("spec_description");
				Document rup = Utils.string2Document(rupxml.getString());
				cases.add(new Case(caseId, name, description, rup));
			}
			return cases;
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
	}

	/**
	 * @param activityName
	 * @return
	 * @throws SQLException
	 */
	public List<String> getRupActivityTypes(String activityName) throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;
		ResultSet resultSet = null;
		List<String> values = null;

		try
		{
			connection = ConnectionManager.getConnection();
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			// -----------------------------------------------------------------------
			// We must be inside a transaction for cursors to work, hence auto
			// commit must be set to FALSE.
			// -----------------------------------------------------------------------
			connection.setAutoCommit(false);

			String callString = "{? = call pkg_utilisationplan.get_activity_types(?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.OTHER);
			callableStatement.setString(2, activityName);
			callableStatement.execute();
			resultSet = (ResultSet) callableStatement.getObject(1);

			values = new ArrayList<String>();
			while (true)
			{
				try
				{
					boolean next = resultSet.next();
					if (!next)
					{
						break;
					}
				}
				catch (SQLException e)
				{
					_log.warn("Exception while iterating result set: " + e.getMessage());
					break;
				}

				String value = resultSet.getString("activity_type");
				if (value != null)
				{
					values.add(value);
				}
			}
			return values;
		}
		finally
		{
			ConnectionManager.close(resultSet, callableStatement, connection);
		}
	}

	/**
	 * Update RUP, set "active status"
	 * 
	 * @param caseId
	 * @param active
	 * @return
	 * @throws SQLException
	 */
	public void updateRup(String caseId, boolean active) throws SQLException
	{
		CallableStatement callableStatement = null;
		Connection connection = null;

		try
		{
			connection = ConnectionManager.getConnection();
			String callString = "{? = call pkg_utilisationplan.update_status(?,?)}";
			callableStatement = connection.prepareCall(callString);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.setString(2, caseId);
			callableStatement.setInt(3, active ? 1 : 0);
			callableStatement.executeUpdate();
			int rowCount = callableStatement.getInt(1);
			_log.debug(rowCount + " rows updated");
		}
		finally
		{
			ConnectionManager.close(null, callableStatement, connection);
		}
	}
}