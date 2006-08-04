package com.nexusbpm.command;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * 
 * @author Dean Mao
 * @created Aug 4, 2006
 */
public class RenameElementTest extends CommandTestCase {

	final String oldName = "my old name";
	final String newName = "my new name";
	
	public void runTestOn(Object obj) throws Exception {
		DataProxy proxy = dataContext.createProxy( obj, null );
		dataContext.attachProxy(proxy, obj, null);
		
		Command command = new RenameElementCommand(proxy, oldName, newName);
		command.execute();

        Method getter = obj.getClass().getMethod( "getName", new Class[] {} );
        String name = (String) getter.invoke( obj, new Object[] {} );
        
		assert name.equals(newName) : "Rename didn't work for " + obj.getClass().toString();
		
		command.undo();
		name = (String) getter.invoke( obj, new Object[] {} );

		assert name.equals(oldName) : "Rename undo didn't work for " + obj.getClass().toString();
		
		command.redo();
		name = (String) getter.invoke( obj, new Object[] {} );
		
		assert name.equals(oldName) : "Rename redo didn't work for " + obj.getClass().toString();
	}
	
	public void testRenameSpecification() throws Exception {
		YSpecification specification = new YSpecification();
		specification.setName(oldName);
		runTestOn(specification);
	}
	
	public void testRenameDecomposition() throws Exception {
		YDecomposition decomposition = new YDecomposition();
		decomposition.setName(oldName);
		runTestOn(decomposition);
	}
	
	public void testExternalNetElement() throws Exception {
		YExternalNetElement element = new YExternalNetElement();
		element.setName(oldName);
		runTestOn(element);
	}
}
