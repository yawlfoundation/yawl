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
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a YAWL case
 *
 * @author sga, jku
 */
public class Case {

    private static Logger _log = LogManager.getLogger(Case.class);
    
    private long id;                                         // hibernate pkey

	protected String caseId;
	private List<Element> data = null;
	private Document rup;
	private String caseName;
	private String description;
    private long timestamp;
    private String savedBy;
    private String rupAsString;                             // for hibernate transport
    private boolean active;

    private Case() { }                                      // for hibernate

	public Case(String id)	{
		setCaseId(id);
	}

	public Case(String caseId, String caseName, String caseDescription, Document rup) {
		this(caseId);
		this.caseName = caseName;
		description = caseDescription;
		this.rup = rup;
	}


    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getCaseId() {	return caseId;	}

	public void setCaseId(String id) { caseId = id; }

	public String getCaseName()	{ return caseName; }
    
    public void setCaseName(String name) { caseName = name; }

	public String getCaseDescription() { return description; }

    public String getDescription() { return description; }
    
    public void setDescription(String desc) { description = desc; }

	public Document getRUP() { return rup;	}
    
    public void setRUP(Document doc) { rup = doc; }
    
    public String getSavedBy() { return savedBy; }
    
    public void setSavedBy(String saver) { savedBy = saver; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public long getTimestamp() {
        if (timestamp == 0) timestamp = System.currentTimeMillis();
        return timestamp;
    }
    
    public void setTimestamp(long time)  { timestamp = time; }
    
    public String getRupAsString() {
        if ((rupAsString == null) && (rup != null)) {
            rupAsString = JDOMUtil.documentToString(rup);
        }
        return rupAsString;
    }
    
    public void setRupAsString(String ras) {
        rupAsString = ras;
        if (ras != null) {
            rup = JDOMUtil.stringToDocument(ras);
        }
    }

	
	public List<Element> getData()	{
		if (data == null) {
			data = new ArrayList<Element>();
			readCaseData();
		}
		return data;
	}

	/**
	 * Reads case data recursively
	 * <p>
	 * The problem with case data is the hierarchical order of nets, which leads
	 * to multiple sets of variables. For example, if we want to find a patient
	 * caseName, we should look for it in the lowest net, since these are the newest
	 * ones. If we cannot find a caseName, we can go ahead and look for it in the net
	 * above and so on.
	 */
	private void readCaseData()	{
		String current, prev = "xx";
		current = caseId;
		while (! (current == null || current.equals(prev))) {
            try {
                data.add(Utils.string2Element(readCaseData(current)));
			}
			catch (IOException e)	{
				_log.warn("Cannot get data for case: " + current + ", " + e.getMessage());
			}
			prev = current;
			current = getParentNetId(current);
		}
	}

	/**
	 * Read case data from the YAWL work queue
	 * 
	 * @see Case#readCaseData()
	 * @param caseId the case to get the data for
     * @return the net-level data for the case id
	 * @throws IOException if the case data can't be retrieved from the engine
	 */
	public String readCaseData(String caseId) throws IOException {
		String response = ResourceServiceInterface.getInstance().getCaseData(caseId);
		if (response.isEmpty() || response.startsWith("<failure")) {
			throw new IOException(response);
		}
		return response;
	}

	/**
	 * Returns ID of parent net
	 * <p>
	 * If the Id passed as the argument is a root ID, it will be returned
	 * unchanged.
	 * 
	 * @param id the id of a child net
	 * @return the id of its parent
	 */
	public String getParentNetId(String id)	{
		int pos = id.lastIndexOf('.');
        return pos < 0 ? id : id.substring(0, pos);
    }

	/**
	 * Gets the value for an XPath expression for an Element at a specified depth
     * in the case's data
	 * 
	 * @param depth the depth of the Element to use from the case data
	 * @param xPath the XPath expression
	 * @return the result of the XPath evaluation, or null if the depth exceeds the
     * depth of the case data's Elements, or if the XPath expression evaluates to null,
     * or if the Element result of the XPath expression contains no text
	 */
	public String getText(int depth, String xPath) {
		if (depth < getData().size()) {
			try	{
				Element depthElement = getData().get(depth).clone();
			    _log.debug("Reading " + xPath + " from " +
                        Utils.element2String(depthElement, true));
				Element xpathResult = XMLUtils.getElement(
                        new Document().setRootElement(depthElement), xPath);
				if (xpathResult != null) {
					return xpathResult.getText();
				}
			}
			catch (Exception e) {
                // fall through to return null
			}
		}
		return null;
	}

	/**
	 * Traverses case data attempting to find an element that returns a non-null result
     * for an XPath expression
	 * 
	 * @param xPath the expression to evaluate
	 * @return the text matching the XPath evaluation, or null if no match is found
	 */
	public String getText(String xPath)	{
		for (int i = (getData().size() - 1); i >= 0; i--) {
			String text = getText(i, xPath);
			if (text != null) {
				return text;
			}
		}
		return null;
	}
}
