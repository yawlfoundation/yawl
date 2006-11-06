/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * 
 * @author Dean Mao
 * @created Aug 4, 2006
 */
public class CreateFlowTest extends CommandTestCase {

	DataProxy<YFlow> flowProxy = null;
	
	public void testCreateFlow() throws Exception {
		YExternalNetElement sourceElement = new YExternalNetElement();
		DataProxy sourceProxy = dataContext.createProxy( sourceElement, null );
		dataContext.attachProxy(sourceProxy, sourceElement, null);
		
		YExternalNetElement targetElement = new YExternalNetElement();
		DataProxy targetProxy = dataContext.createProxy( targetElement, null );
		dataContext.attachProxy(targetProxy, targetElement, null);
		
		Command command = new CreateFlowCommand(sourceProxy, targetProxy, this);
		command.execute();
		
		YFlow flow = flowProxy.getData();
		assert flow.getNextElement() == targetElement : "next element should be the target";
		assert flow.getPriorElement() == sourceElement : "previous element should be the source";
		
		command.undo();
		assert flow.getNextElement() == null : "next element should be null";
		assert flow.getPriorElement() == null : "previous element should be null";
		
		command.redo();
		assert flow.getNextElement() == targetElement : "next element should be the target";
		assert flow.getPriorElement() == sourceElement : "previous element should be the source";
	}
	

	public void proxyAttached( DataProxy proxy, Object data, DataProxy parent ) {
		assert proxy != null : "Proxy can't be null!";
		flowProxy = proxy;
	}
}
