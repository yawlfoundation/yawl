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

package org.yawlfoundation.yawl.scheduling;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Represents a YAWL case
 *
 * @author sga, jku
 */
public class Case {

    private static Logger _log = Logger.getLogger(Case.class);

	protected String _yawlCaseId;
	private ArrayList<Element> _data = null;
	private Document _rup;
	private String _name;
	private String _description;

	public Case(String id)	{
		setCaseId(id);
	}

	public Case(String caseId, String caseName, String caseDescription, Document rup) {
		setCaseId(caseId);
		_name = caseName;
		_description = caseDescription;
		_rup = rup;
	}

	public String getCaseId() {	return _yawlCaseId;	}

	public void setCaseId(String id) { _yawlCaseId = id; }

	public String getCaseName()	{ return _name; }

	public String getCaseDescription() { return _description; }

    public String getDescription() { return _description; }

	public Document getRUP() { return _rup;	}

	
	public ArrayList<Element> getData()	{
		if (_data == null) {
			_data = new ArrayList<Element>();
			readCaseData();
		}
		return _data;
	}

	/**
	 * Reads case data recursively
	 * <p>
	 * The problem with case data is the hierarchical order of nets, which leads
	 * to multiple sets of variables. For example, if we want to find a patient
	 * name, we should look for it in the lowest net, since these are the newest
	 * ones. If we cannot find a name, we can go ahead and look for it in the net
	 * above and so on.
	 * 
	 * @throws IOException
	 */
	private void readCaseData()	{
		String current, prev = "xx";
		current = _yawlCaseId;
		while (current != null && !current.equals(prev)) {
			try	{
				_data.add(Utils.string2Element(readCaseData(current)));
			}
			catch (Exception e)	{
				_log.warn("cannot get data for case: " + current + ", " + e.getMessage());
			}
			prev = current;
			current = getParentNetId(current);
		}
	}

	/**
	 * Read case data from the YAWL work queue
	 * 
	 * @see Case#readCaseData()
	 * @param caseId
	 * @throws IOException
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
	 * @param id
	 * @return
	 */
	public String getParentNetId(String id)	{
		int pos = id.lastIndexOf(".");
        return pos < 0 ? id : id.substring(0, pos);
    }

	/**
	 * Tries to find a text in specified case data depth
	 * 
	 * @param depth
	 * @param xPath
	 * @return
	 */
	public String getText(int depth, String xPath) {
		if (depth < getData().size()) {
			try	{
				Element e = (Element) getData().get(depth).clone();
			    _log.debug("Reading " + xPath + " from " + Utils.element2String(e, true));
				Element el = XMLUtils.getElement(new Document().setRootElement(e), xPath);
				if (el != null && el.getText() != null)	{
					return el.getText();
				}
			}
			catch (Exception e1) {
                // fall through to return null
			}
		}
		return null;
	}

	/**
	 * Tries to find a text in case data
	 * 
	 * @param xPath
	 * @return
	 */
	public String getText(String xPath)	{
		String out = null;
		for (int i = (getData().size() - 1); i >= 0; i--) {
			String thisout = getText(i, xPath);
			if (thisout != null) {
				out = thisout;
			}
		}
		return out;
	}
}
