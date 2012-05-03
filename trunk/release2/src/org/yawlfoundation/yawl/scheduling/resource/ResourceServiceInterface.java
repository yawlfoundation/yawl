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

package org.yawlfoundation.yawl.scheduling.resource;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceCalendarGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClientAdapter;
import org.yawlfoundation.yawl.scheduling.*;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;
import org.yawlfoundation.yawl.scheduling.util.*;

import java.io.IOException;
import java.util.*;


/**
 * implementation of wrapping interface to RS interface methods
 * 
 * @author tbe
 * @version $Id$
 * 
 */
public class ResourceServiceInterface implements Constants {

    private static final boolean DEBUG_SAVE_TO_RS = false;
    private static final Logger _log = Logger.getLogger(ResourceServiceInterface.class);
    private static ResourceServiceInterface INSTANCE = null;

    private ResourceGatewayClientAdapter _resClient;
    private ResourceCalendarGatewayClient _calClient;
    private WorkQueueGatewayClientAdapter _wqClient;
    private String _handle;

    private Map<String, String> _userHandles;

    private ConfigManager _config;
    private PropertyReader _props;


	private ResourceServiceInterface()	{
		_log.info("ResourceServiceInterface starting...");
		_config = ConfigManager.getInstance();
        _props = PropertyReader.getInstance();
        _userHandles = new Hashtable<String, String>();
        initGateways();
	}


	public static ResourceServiceInterface getInstance() {
		if (INSTANCE == null) INSTANCE = new ResourceServiceInterface();
		return INSTANCE;
	}


    private void initGateways() {
        try {
            String resBackend = _props.getYAWLProperty("ResourceGatewayClient.backEndURI");
            String calBackend = _props.getYAWLProperty("ResourceCalendarGatewayClient.backEndURI");
            String wqBackend =  _props.getYAWLProperty("WorkQueueGatewayClient.backEndURI");
            _resClient = new ResourceGatewayClientAdapter(resBackend);
            _calClient = new ResourceCalendarGatewayClient(calBackend);
            _wqClient = new WorkQueueGatewayClientAdapter(wqBackend);
        }
        catch (IOException ioe) {
            _log.error("Could not instantiate Resource Service Gateways", ioe);
        }

    }


    public synchronized String getHandle() throws IOException {
        if (_resClient == null) {
            initGateways();
        }
        if (_resClient == null) {
            throw new IOException("Gateway could not be instantiated.");
        }
        if ((_handle == null) || (! _resClient.checkConnection(_handle))) {
            String user = _props.getYAWLProperty("user");
            String password = _props.getYAWLProperty("password");
            _handle = _resClient.connect(user, password);
            if (! _resClient.successful(_handle)) {
                throw new IOException("Cannot connect as user: '" + user +
                        "' to resource service: " + _handle);
            }
        }
        return _handle;
	}


	/**
	 * return list of available timeslots between given period for each resource,
	 * matching given resource string
	 * 
	 * @param resource
	 * @param from
     * @param to
	 * @return list of timeslots
	 * @throws YAWLException
	 */
	public List<Element> getAvailabilities(Element resource, Date from, Date to)
            throws YAWLException, IOException {
		String resourceXML = Utils.element2String(resource, false);
		String avail = _calClient.getAvailability(resourceXML, from, to, getHandle());

		_log.debug("-------------available " + avail + " for " + resourceXML);

		List<Element> timeSlots = new ArrayList<Element>();
		for (Element timeSlot : Utils.string2Element(avail).getChildren())	{
			timeSlots.add(timeSlot);
		}
		return timeSlots;
	}

	/**
	 * not used
	 */
	public Map<String, Object> getDropdownContent(String objectName, String prevFieldValue)
			throws ResourceGatewayException, IOException {
		_log.debug("objectName: " + objectName + ", prevFieldValue: " + prevFieldValue);
		Map<String, Object> objects;
		if (objectName.equals(Constants.XML_ID)) {
			objects = getResources(prevFieldValue);
		}
		else if (objectName.equals(Constants.XML_ROLE))	{
			objects = getRoles();
		}
		else if (objectName.equals(Constants.XML_CAPABILITY)) {
			objects = getCapabilities();
		}
		else if (objectName.equals(Constants.XML_CATEGORY))	{
			objects = getCategories();
		}
		else if (objectName.equals(Constants.XML_SUBCATEGORY)) {
			objects = getSubCategories(prevFieldValue);
		}
		else {
			objects = new TreeMap<String, Object>();
			_log.error("unknown objectName: " + objectName);
		}
		_log.debug("objects(" + objectName + ", " + prevFieldValue + ")=" + Utils.toString(objects));
		return objects;
	}

	/**
	 * scheduling service retrieve all existing resources for showing in
	 * configuration dropdown boxes of custom form
	 * 
	 * @throws ResourceGatewayException
     * @throws IOException
	 */
	private Map<String, Object> getResources(String resType)
            throws ResourceGatewayException, IOException {
		Map<String, Object> objects = new TreeMap<String, Object>();
		if (resType.equals("non-human")) {
			List<NonHumanResource> nhrs = getNonHumanResources();
			_log.debug("NonHumanResources: " + Utils.toString(nhrs));
			if (nhrs != null) {
				for (NonHumanResource nhr : nhrs) {
					objects.put(nhr.getID(), nhr.getName());
				}
			}
		}
		else if (resType.equals("human")) {
			List<Participant> pars = getParticipants();
			_log.debug("Participants: " + Utils.toString(pars));
			if (pars != null) {
				for (Participant par : pars) {
					objects.put(par.getID(), par.getFullName());
				}
			}
		}
		return objects;
	}

	/**
	 * scheduling service retrieve a list of all existing roles for showing in
	 * configuration dropdown boxes of custom form
	 * 
	 * @throws ResourceGatewayException
     * @throws IOException
	 */
	private Map<String, Object> getRoles() throws ResourceGatewayException, IOException	{
		Map<String, Object> objects = new TreeMap<String, Object>();
		List roles = _resClient.getRoles(getHandle());
		_log.debug("Roles: " + Utils.toString(roles));
		if (roles != null) {
			for (Object o : roles) {
                Role role = (Role) o;
				objects.put(role.getID(), role.getName());
			}
		}
		return objects;
	}

	/**
	 * scheduling service retrieve a list of all existing capabilities for
	 * showing in configuration dropdown boxes of custom form
	 * 
     * @throws ResourceGatewayException
     * @throws IOException
	 */
	private Map<String, Object> getCapabilities()
            throws ResourceGatewayException, IOException {
		Map<String, Object> objects = new TreeMap<String, Object>();
		List caps = _resClient.getCapabilities(getHandle());
		_log.debug("Capabilities: " + Utils.toString(caps));
		if (caps != null) {
			for (Object o : caps) {
                Capability cap = (Capability) o;
				objects.put(cap.getID(), cap.getCapability());
			}
		}
		return objects;
	}

	/**
	 * scheduling service retrieve a list of all existing categories for showing
	 * in configuration dropdown boxes of custom form
	 * 
     * @throws ResourceGatewayException
     * @throws IOException
	 */
	private Map<String, Object> getCategories() throws ResourceGatewayException, IOException {
		Map<String, Object> objects = new TreeMap<String, Object>();
		List<NonHumanCategory> cats = _resClient.getNonHumanCategories(getHandle());
		_log.debug("Categories: " + Utils.toString(cats));
		if (cats != null) {
			for (NonHumanCategory cat : cats) {
				objects.put(cat.getID(), cat.getName());
			}
		}
		return objects;
	}

	/**
	 * scheduling service retrieve a list of all existing subcategories of
	 * category for showing in configuration dropdown boxes of custom form
	 * 
     * @throws ResourceGatewayException
     * @throws IOException
	 */
	private Map<String, Object> getSubCategories(String category)
            throws ResourceGatewayException, IOException {
		Map<String, Object> objects = new TreeMap<String, Object>();
		if (!category.isEmpty()) {
			List<String> subCats = _resClient.getNonHumanSubCategories(category, getHandle());
			if (subCats != null) {
				for (String subCat : subCats) {
					objects.put(subCat, subCat);
				}
			}
		}
		return objects;
	}

	/**
	 * Saves all reservation XML elements of the ResourceUtilisationPlan,
	 * depending on "reservation.planningStatus" (see rup.xsd for the XML data
	 * model) and removes or updates older reservations for the case and for each
	 * activity. Then sets planning status and error or warning values on XML
	 * elements (e.g. if a resource cannot be reserved or technical errors occur)
	 * and returns the ResourceUtilisationPlan.
	 * 
	 * @param rup
	 * @param checkOnly
	 * @param resourceChange
	 */
	public Document saveReservations(Document rup, boolean checkOnly, boolean resourceChange)
			throws ResourceGatewayException, JDOMException, IOException {
		String caseId = null;

        try	{
			caseId = XMLUtils.getCaseId(rup);
			Case cas = new Case(caseId);
			cas.readCaseData(caseId);
		}
		catch (IOException e) {
			_log.warn("Failed to save RUP for case Id  " + caseId + ", " + e.getMessage());
			return rup;
		}

		// remove unchecked reservations, because RS cannot handle them, readd
		// them after save in RS
		Map<String, List<Element>> resUnchecked = removeReservations(rup, RESOURCE_STATUS_UNCHECKED);

		if (resourceChange)	{

			// remove and readd all reservations, because resource changes cannot
			// be updated in RS
			Map<String, List<Element>> resAll = removeReservations(rup, null);
			try	{
				String rupStr = Utils.element2String(rup.getRootElement(), false);
				rupStr = _calClient.saveReservations(rupStr, false, getHandle());
				rup = new Document(Utils.string2Element(rupStr));
			}
			catch (Exception e)	{
				_log.error("cannot save empty rup", e);
			}
			finally	{
				addReservations(rup, resAll);
			}
		}

		try	{
			// remove errors from root element and reservations to avoid cancelling
			// of save in RS
			XMLUtils.removeAttribute(rup.getRootElement(), XML_ERROR);
			XMLUtils.removeAttribute(rup.getRootElement(), XML_WARNING);

			String xpath = XMLUtils.getXPATH_ActivityElement(null, XML_RESERVATION, null);
			List<Element> reservations = XMLUtils.getXMLObjects(rup, xpath);
			for (Element reservation : reservations) {
				XMLUtils.removeAttributes(reservation, XML_ERROR);
				XMLUtils.removeAttributes(reservation, XML_WARNING);
			}

			if (DEBUG_SAVE_TO_RS) {
				_log.debug("checkOnly=" + checkOnly + ", resourceChange=" +
                        resourceChange + ", save rup in RS: " +
                        Utils.document2String(rup, true)
                );
            }
			String rupStr = Utils.element2String(rup.getRootElement(), false);
			rupStr = _calClient.saveReservations(rupStr, checkOnly, getHandle());
			rup = new Document(Utils.string2Element(rupStr));
			if (DEBUG_SAVE_TO_RS) {
                _log.debug("saved rup from RS: " + Utils.document2String(rup, true));
            }
		}
		finally	{
			// add formerly removed unchecked reservations
			addReservations(rup, resUnchecked);
		}
		return rup;
	}


	public Map<String, List<Element>> removeReservations(Document rup, String statusToBe)
            throws JDOMException {
		Map<String, List<Element>> res = new HashMap<String, List<Element>>();
		String where = statusToBe == null ? "" : "[" + XML_STATUSTOBE + "='" + statusToBe + "']";
		String xpath = XMLUtils.getXPATH_ActivityElement(null, XML_RESERVATION + where, null);
		List<Element> reservations = XMLUtils.getXMLObjects(rup, xpath);
		for (Element reservation : reservations) {
			Element activity = reservation.getParentElement();
			activity.removeContent(reservation);

			List<Element> l = res.get(activity.getChildText(XML_ACTIVITYNAME));
			if (l == null)	l = new ArrayList<Element>();

			Element reservationId = reservation.getChild(XML_RESERVATIONID);
			if (reservationId == null) {
				reservation.addContent(new Element(XML_RESERVATIONID));
			}
			else {
				reservationId.setText("");
			}

			l.add(reservation);
			res.put(activity.getChildText(XML_ACTIVITYNAME), l);
		}

		return res;
	}

	public void addReservations(Document rup, Map<String, List<Element>> res)
            throws JDOMException {
		String xpath = XMLUtils.getXPATH_Activities();
		List<Element> activities = XMLUtils.getXMLObjects(rup, xpath);
		for (Element activity : activities)	{
			List<Element> l = res.get(activity.getChildText(XML_ACTIVITYNAME));
			if (l != null) {
				for (Element reservation : l) {
					activity.addContent(reservation);
				}
			}
		}
	}


    public List<Participant> getParticipants()
            throws IOException, ResourceGatewayException {
        return _resClient.getParticipants(getHandle());
    }


    public List<Role> getParticipantRoles(String pid)
            throws IOException, ResourceGatewayException {
        List<Role> roles = new ArrayList<Role>();
        for (Object o : _resClient.getParticipantRoles(pid, getHandle())) {
            roles.add((Role) o);
        }
        return roles;
    }


    public List<Capability> getParticipantCapabilities(String cid)
            throws IOException, ResourceGatewayException {
        List<Capability> capabilities = new ArrayList<Capability>();
        for (Object o : _resClient.getParticipantCapabilities(cid, getHandle())) {
            capabilities.add((Capability) o);
        }
        return capabilities;
    }


    public List<NonHumanResource> getNonHumanResources()
            throws IOException, ResourceGatewayException {
        return _resClient.getNonHumanResources(getHandle());
    }


    public Role getRole(String roleID) throws IOException, ResourceGatewayException {
        return _resClient.getRole(roleID,  getHandle());
    }


    public Participant getParticipant(String pID)
            throws IOException, ResourceGatewayException {
        return _resClient.getParticipant(pID,  getHandle());
    }


    public NonHumanResource getNonHumanResource(String resID)
            throws IOException, ResourceGatewayException {
        return _resClient.getNonHumanResource(resID,  getHandle());
    }


    public NonHumanCategory getNonHumanCategory(String catID)
            throws IOException, ResourceGatewayException {
        return _resClient.getNonHumanCategory(catID,  getHandle());
    }


    public List<NonHumanCategory> getNonHumanCategories()
            throws IOException, ResourceGatewayException {
        return _resClient.getNonHumanCategories(getHandle());
    }


    public Map<String, String> getRoleIdentifiers()
            throws IOException, ResourceGatewayException {
         return _resClient.getRoleIdentifiers(getHandle());
    }


    public boolean checkConnection(String handle) {
        return _resClient.checkConnection(handle);
    }


    public String registerCalendarStatusChangeListener(String uri) throws IOException {
        return _calClient.registerStatusChangeListener(uri, getHandle());
    }


    public String getCaseData(String caseID) throws IOException {
        return _wqClient.getCaseData(caseID, getHandle());
    }


    public boolean isValidUserSession(String handle) throws IOException {
        return _wqClient.isValidUserSession(handle);
    }


    public String getWorkItem(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return _wqClient.getWorkItem(itemID, handle);
    }


    public String updateWorkItemData(String itemID, String data, String handle)
            throws IOException, ResourceGatewayException {
        return _wqClient.updateWorkItemData(itemID, data, handle);
    }


    public String getFullNameForUserID(String userID)
            throws IOException, ResourceGatewayException {
        return _wqClient.getFullNameForUserID(userID, getHandle());
    }


    public boolean isValidSession(String userID, String handle) throws IOException {
        boolean valid = isValidUserSession(handle);
        if (valid) _userHandles.put(userID, handle);
        return valid;
    }


    public String getUserSessionHandle(String userID, String password) throws IOException {
        String handle = _userHandles.get(userID);
        if (! isValidUserSession(handle)) {
            _log.debug("work queue gateway client connecting...");
            handle = _wqClient.userlogin(userID, password);
            if (! _resClient.successful(handle)) {
                throw new IOException("cannot login as user: '" + userID +
                        "' to work queue gateway client: " + handle);
            }
            _userHandles.put(userID, handle);
        }
        return handle;
    }


    public String getUserName(String handle) throws IOException, ResourceGatewayException {
        for (String userID : _userHandles.keySet()) {
            if (_userHandles.get(userID).equals(handle)) {
                return _wqClient.getFullNameForUserID(userID, handle);
            }
        }
        return null;
    }

}
