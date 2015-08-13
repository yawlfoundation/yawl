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

package org.yawlfoundation.yawl.scheduling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.scheduling.persistence.DataMapper;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Bridge between Service and YAWL,
 * has to be registered with the YAWL engine.
 * 
 * @author tbe
 * @date 2010-10-22
 */
public abstract class Service extends InterfaceBWebsideController implements Constants {

    private static Logger _log = LogManager.getLogger(Service.class);

    protected Set<String> _cancelledWorkitemOrCaseIds = new HashSet<String>();
	protected DataMapper _dataMapper;
    protected String _handle;
    protected PropertyReader _props;

    public ConfigManager _config;

	// remove mapping if work item or whole case is already cancelled
	private static final boolean REMOVE_MAPPING_NOT_IN_YAWL = true;
	

	/**
	 * will be started by:
	 * - engine, because its defined in web.xml
	 * - static block
	 * - JCouplingImporter
	 * 
	 * throws JCouplingServiceDAOException
	 * throws SimpleDAOException
	 * throws TransferException
	 */
	public Service() {
//		PropertyConfigurator.configureAndWatch("log4j.properties", 60*1000);
		_config = ConfigManager.getInstance();
        _props = PropertyReader.getInstance();
	}


    protected abstract void processMappingChild(Mapping mapping) throws Exception;


    /*********************************************************************************/

	/**
	 * ********** inherited by InterfaceBWebsideController *************
	 * receives messages and requests from YAWL work items
	 * Saves or updates a mapping to the database for recovery of failed YAWL requests
	 */
	public void handleEnabledWorkItemEvent(WorkItemRecord wirParent) {
		_log.debug("wirParentID: " + wirParent.getID());
		Mapping mapping = null;
		try {
			getHandle();        // checks/makes connection to YAWL engine
			mapping = new Mapping(wirParent.getID(), null, Mapping.WORKITEM_STATUS_PARENT);
			_dataMapper.saveMapping(mapping);
			processMapping(mapping);
		}
        catch (Throwable e) {
			_log.error("cannot execute work item: " + wirParent.getID(), e);
		}
	}
	
	/**
	 * checkout parent work item from YAWL engine
	 * get children work items, cache them, process them
	 * check parent back into the engine
	 * remove the mapping for successful processed parent work items
	 * updates the mapping continuously
	 * @param mapping: contained work item is parent
	 */
	protected void processMapping(Mapping mapping) throws Exception {

			// checkout parent work item
		if (mapping.getWorkItemStatus().equals(Mapping.WORKITEM_STATUS_PARENT)) {
			checkOutWorkItem(mapping);
			mapping.setWorkItemStatus(Mapping.WORKITEM_STATUS_CHECKOUT);
			_dataMapper.saveMapping(mapping);
		}
		
		// checkout children work items
		if (mapping.getWorkItemStatus().equals(Mapping.WORKITEM_STATUS_CHECKOUT)) {
			for (WorkItemRecord wir : getChildren(mapping.getWorkItemId())) {
				Mapping mappingChild = null;
				try {
					if (isCancelledWorkitem(mapping.getWorkItemId())) {
						continue;
					}
					
					mappingChild = new Mapping(wir.getID(), null, Mapping.WORKITEM_STATUS_CACHED);
					_dataMapper.saveMapping(mappingChild);
					processMappingChild(mappingChild);
		
				}
                catch (Throwable e) {
					_log.error("cannot execute work item: " + mapping.getWorkItemId(), e);
				}
			}
			_dataMapper.removeMapping(mapping);
		}
        else {
			processMappingChild(mapping);
		}
	}

	protected Element getDataListFromWorkItem(Mapping mapping) throws Exception {
		WorkItemRecord wir = getWorkItemFromCache(mapping);
		Element dataList = wir.getDataList();
		_log.debug("dataList of work item "+wir.getID()+":\r\n" + Utils.element2String(dataList, true));
		return dataList;
	}

	/**
	 * ********** inherited by InterfaceBWebsideController ************* 
	 * Removes (if necessary) the work items from the YAWL Engine
	 */
	public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord)
	{
		_log.info("wirID: " + workItemRecord.getID() + " has been cancelled");
		try {
			_cancelledWorkitemOrCaseIds.add(workItemRecord.getID());
			_dataMapper.removeMapping(workItemRecord.getID());
		}
        catch (Throwable e) {
			_log.error("Cannot remove mapping: " + workItemRecord.getID(), e);
		}
	}

	/**
	 * the SS and the RS will get the event at the same time, so the SS will need to delay the call
	 * to the RS by 10 seconds while the RS gets the credentials from the engine
	 */
	public void handleEngineInitialisationCompletedEvent() {
        int oneSecond = 1000;
        int count = 0;
		while (count <= 10) {
            count++;
			try {
				try {
                    Thread.sleep(oneSecond);
                }
                catch (InterruptedException e1) {
                    // nothing to do
                }
				getHandle();                    // exception if connect fails
				return;
			}
            catch (Throwable e) {
                // try again in a second
			}
		}
        _log.error("Unable to connect to YAWL engine, trying again in 10 s ...");
	}


	public void handleCancelledCaseEvent(String caseID) {
  	    _log.info("caseID: " + caseID + " has been cancelled");
		try {
			_cancelledWorkitemOrCaseIds.add(caseID);
			_dataMapper.removeMapping(caseID + ":");
			_dataMapper.removeMapping(caseID + ".");
		}
        catch (Throwable e) {
			_log.error("Cannot remove mappings for caseID: " + caseID, e);
		}
    }
	
	protected boolean isCancelledWorkitem(String wirID) {
		_log.debug("_cancelledWorkitemOrCaseIds=" + Utils.toString(_cancelledWorkitemOrCaseIds));
		if (_cancelledWorkitemOrCaseIds.contains(wirID)) {
			_cancelledWorkitemOrCaseIds.remove(wirID);
			_log.debug("remove workitem " + wirID);
			return true;
		}
		
		int idx = Math.min(Math.max(0, wirID.indexOf(".")), Math.max(0, wirID.indexOf(":")));
		if (idx > 0) {
			String caseId = wirID.substring(0, idx);
			if (_cancelledWorkitemOrCaseIds.contains(caseId)) {
				_cancelledWorkitemOrCaseIds.remove(caseId);
				_log.debug("remove workitem of case " + caseId);
				return true;
			}
		}
		
		return false;
	}



	public void handleCompleteCaseEvent(String caseID, String casedata) {
  	    _log.info("case ID: " + caseID + " has been completed");
  }
  
	/**
	 * TODO@tbe: validate payload before to avoid that work item failed if payload is invalid
	 * 
	 * @param mapping
	 * @param payload
	 * @throws YAWLException
	 */
	public void checkInWorkItem(Mapping mapping, String payload) throws YAWLException {
		try {
			WorkItemRecord wir = getWorkItemFromCache(mapping);
			String result = checkInWorkItem(wir.getID(), wir.getDataList(),
                    getOutputData(payload), null, getHandle());

			if (!successful(result)) {
				if (REMOVE_MAPPING_NOT_IN_YAWL && isNoWorkItemErrorMsg(result)) {
					_dataMapper.removeMapping(mapping);
					throw new YAWLException(result + ", remove mapping");
				}
                else {
					throw new YAWLException("Not successful: " + result);
				}
			}
			
			_log.info("++++++++++++++++++++ work item " + wir.getID() +
                    " successfully checked back into the engine");
		}
        catch (Throwable e) {
			throw new YAWLException(e);
		}
	}

	/**
	 * ermittelt Children eines Parent-WorkItems, dieses muss dazu bereits ausgecheckt sein
	 * 
	 * @param workItemID
	 * @return
	 * @throws IOException
	 */
	protected List<WorkItemRecord> getChildren(String workItemID)
            throws YAWLException, IOException {
		List<WorkItemRecord> list = getChildren(workItemID, getHandle());
		_log.debug("return " + list.size() + " children of parentID " + workItemID);
		
		return list;
	}
	
	/**
	 * Check the work item out of the engine
	 * InterfaceBWebsideController.checkOut() adds work item with ID=wirID to workItemCache 
	 * 
	 * @param mapping
	 *          - contains the work item ID to check out
	 */
	protected void checkOutWorkItem(Mapping mapping) throws SQLException, YAWLException	{
		String wirID = mapping.getWorkItemId();
		try {
            _log.debug("checking out work item " + wirID + "...");
			WorkItemRecord wir = checkOut(wirID, getHandle());

            if (wir != null) {
				_log.info("work item ID: '" + wirID + "' checkout successful");
			}
            else {
				throw new YAWLException("work item " + wirID + " not found in YAWL");
			}
        }
        catch (YAWLException e) {
			if (REMOVE_MAPPING_NOT_IN_YAWL && isNoWorkItemErrorMsg(e.getMessage())) {
				_dataMapper.removeMapping(mapping);
				throw new YAWLException(e.getMessage() + ", remove mapping");
			}
            else {
				throw e;
			}
		}
        catch (Throwable e) {
			throw new YAWLException(e);
		}
	}

	/**
	 * checks if there is a valid connection to the engine, and if there isn't, attempts to connect
	 * 
	 * @return true if reconnected
	 */
	protected synchronized String getHandle() throws IOException {
        if ((_handle == null) || (! checkConnection(_handle))) {
            String user = _props.getYAWLProperty("user");
            String password = _props.getYAWLProperty("password");
            _handle = connect(user, password);
            if (! successful(_handle)) {
                throw new IOException("Cannot connect as user: '" + user +
                        "' to interfaceB client: " + _handle);
            }
        }
        return _handle;
	}
	

	
	/**
	 * re-adds checkedout item to local cache after a restore (if required)
	 */
	protected WorkItemRecord getWorkItemFromCache(Mapping mapping) throws Exception	{
        _log.debug("get cached work item " + mapping.getWorkItemId() + "...");
		WorkItemRecord wir = getCachedWorkItem(mapping.getWorkItemId());
		if (wir == null) {

			// if the item is not locally cached, it means a restore has occurred
			// after a checkout & the item is still checked out, so lets put it back
			// so that it can be checked back in
			wir = getEngineStoredWorkItem(mapping.getWorkItemId(), getHandle());
			
			if (wir == null) {
				String error = "work item " + mapping.getWorkItemId() + " not found in YAWL";
				if (REMOVE_MAPPING_NOT_IN_YAWL) {
					_dataMapper.removeMapping(mapping);
					error += ", remove mapping";
				}
				throw new YAWLException(error);
			}

			_log.debug("add work item ID " + wir.getID() + " to cache...");
			getIBCache().addWorkItem(wir);
		}
        else {
			_log.debug("work item ID: " + wir.getID() + " is already cached");
		}
		
		return wir;
	}

	/**
	 * @param data
	 * @return
	 */
	protected Element getOutputData(String data) {
		String output = "<result>" + (data==null ? "" : data.trim()) + "</result>";
		return Utils.string2Element(output);
	}

	/**
	 * sort mappings by workItemId ascending
	 * 
	 * @param mappings
	 */
	protected void sort(List<Mapping> mappings) {
		Collections.sort(mappings, new Comparator<Mapping>() {
	        public int compare(Mapping m1, Mapping m2) {
	        	return norm(m1.getWorkItemId()).compareTo(norm(m2.getWorkItemId()));
	        }
	    
	        final String prefix = "0000";
	        private String norm(String wirID) {
	    	    int idx = wirID.indexOf(".");
	    	    if (idx >= 0) {
		    	    String s = prefix + wirID.substring(0, idx);
		    	    s = s.substring(s.length()-prefix.length(), s.length());
		    	    return s + "." + norm(wirID.substring(idx+1));
	    	    }
                else {
	    		    return wirID;
	    	    }
	        }
    
		});
	}

	/**
	 * returns true, if string contains error message about no work item exists in YAWL engine
	 * @return
	 */
	private boolean isNoWorkItemErrorMsg(String msg) {
		return msg != null &&
			(msg.contains("<failure><reason>No work item found with id =")
				||
			 msg.contains("The engine returned no work items.</reason></failure>")
			);
	}


    public Element getSpecificationForCase(String caseId) {
        try {
            return Utils.string2Element(
                    _interfaceBClient.getSpecificationForCase(caseId, getHandle()));
        }
        catch (IOException ioe) {
            _log.error("Could not get specification for case: " + caseId, ioe);
            return null;
        }
    }
}
