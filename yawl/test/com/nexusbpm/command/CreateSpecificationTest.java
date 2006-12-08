/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.operation.WorkflowOperation;
import com.nexusbpm.services.NexusServiceInfo;

/**
 * 
 * @author Dean Mao
 * @created Aug 4, 2006
 */
public class CreateSpecificationTest extends CommandTestCase  {

	DataProxy<YSpecification> specificationProxy;

	public void proxyAttached( DataProxy proxy, Object data, DataProxy parent ) {
		assertNotNull("Proxy can't be null!", proxy);
		specificationProxy = proxy;
	}

	public void testCreateCompleteSpec() throws Exception {
        Command createSpec = new CreateSpecificationCommand(
        		rootProxy,
                "Test Specification",
                this );
        createSpec.execute();

        Command createNet = new CreateNetCommand(
        		specificationProxy,
                "Test Root Net",
                null );
        createNet.execute();
        
        DataProxy<YNet> netProxy = dataContext.getDataProxy(
        		specificationProxy.getData().getDecompositions().get( 0 ) );

        assertNotNull("net proxy was null", netProxy);
        

        NexusServiceInfo jython = NexusServiceInfo.getServiceWithName( "Jython" );
        Command createJython = new CreateNexusComponentCommand(
                netProxy,
                jython.getServiceName(),
                jython.getServiceName(),
                jython,
                null );
        createJython.execute();
        
        // Find the input/output proxies from context
        DataProxy<YInputCondition> inputProxy = null;
        DataProxy<YAtomicTask> jythonProxy = null;
        DataProxy<YOutputCondition> outputProxy= null;
        for( YExternalNetElement element : netProxy.getData().getNetElements() ) {
        	if( element instanceof YAtomicTask ) {
                NexusServiceInfo info = WorkflowOperation.getNexusServiceInfoForTask( (YAtomicTask) element );
                if( info != null ) {
                    if( info.getServiceName().equals( "Jython" ) ) {
                        jythonProxy = dataContext.getDataProxy( element );
                    }
                }
            }
            else if( element instanceof YInputCondition ) {
                inputProxy = dataContext.getDataProxy( element );
            }
            else if( element instanceof YOutputCondition ) {
                outputProxy = dataContext.getDataProxy( element );
            }
        }

        assertNotNull("input proxy was null", inputProxy);
        assertNotNull("jython proxy was null", jythonProxy);
        assertNotNull("output proxy was null", outputProxy);
        
        Command flow = new CreateFlowCommand(
                inputProxy,
                jythonProxy,
                null );
        flow.execute();
        
        flow = new CreateFlowCommand(
                jythonProxy,
                outputProxy,
                null );
        flow.execute();
        
        // Get specification and validate
        YSpecification specification = specificationProxy.getData();
        String xml = specification.toXML();
        specification.verify();
	}
}
