/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.forms;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.io.IOException;
import java.util.Map;

import org.jdom.JDOMException;

import au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution.InterfaceD_Client;


public class InterfaceD_XForm extends InterfaceD_Client{

	private String _interfaceDServerURI;
	
	 
	/**
	 * @param interfaceDServerURI
	 */
	public InterfaceD_XForm(String interfaceDServerURI) {
		
		super(interfaceDServerURI);
		_interfaceDServerURI = interfaceDServerURI;
	}

	
	/**
	 * For launching a case.
	 * @param queryMap
	 * @param userID
	 * @param sessionHandle
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public String sendWorkItemData(Map queryMap, String userID, String specID, String sessionHandle) 
		throws IOException, JDOMException {
		
		queryMap.put("userID", userID);
		queryMap.put("sessionHandle", sessionHandle);
		queryMap.put("specID", specID);
		return executePost(_interfaceDServerURI, queryMap);
	}
	 
	
	/**
	 * For editing a workitem.
	 * @param queryMap
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public String sendWorkItemData(Map queryMap, WorkItemRecord workItem, 
			String userID, String sessionHandle) 
		throws IOException, JDOMException {
		
		queryMap.put("userID", userID);
		queryMap.put("sessionHandle", sessionHandle);
		return executePost(_interfaceDServerURI, queryMap, workItem);
	}
	
	
	public String sendHTMLWorkItemData(Map queryMap) 
		throws IOException, JDOMException {
		
		return executePost(_interfaceDServerURI, queryMap);
	}
}