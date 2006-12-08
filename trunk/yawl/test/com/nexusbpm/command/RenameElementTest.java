/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * 
 * @author Dean Mao
 * @author Nathan Rose
 * @created Aug 4, 2006
 */
public class RenameElementTest extends CommandTestCase {

	final String oldName = "my old name";
	final String newName = "my new name";
	
	public void runTestOn(Object obj, GetterMethod getter) throws Exception {
		DataProxy proxy = dataContext.createProxy( obj, null );
		dataContext.attachProxy(proxy, obj, null);
		
		Command command = RenameCommandFactory.getRenameCommand( proxy, newName, oldName );
		command.execute();

        String name = getter.invoke( obj );
        
		assertEquals("Rename didn't work for " + obj.getClass().toString(), name, newName);
		
		command.undo();
		name = getter.invoke( obj );

		assertEquals("Rename undo didn't work for " + obj.getClass().toString(), name, oldName);
		
		command.redo();
		name = getter.invoke( obj );
		
		assertEquals("Rename redo didn't work for " + obj.getClass().toString(), name, newName);
	}
	
	public void testRenameSpecification() throws Exception {
		YSpecification specification = new YSpecification(oldName);
		runTestOn(specification, new YSpecificationGetter());
	}
	
	public void testRenameDecomposition() throws Exception {
		YDecomposition decomposition = new YDecomposition();
		decomposition.setName(oldName);
		runTestOn(decomposition, new YDecompositionGetter());
	}
	
	public void testExternalNetElement() throws Exception {
		YExternalNetElement element = new YExternalNetElement();
		element.setName(oldName);
		runTestOn(element, new YExternalNetElementGetter());
	}
	
	private interface GetterMethod<Type> {
		public String invoke( Type object );
	}
	
	private class YSpecificationGetter implements GetterMethod<YSpecification> {
		public String invoke( YSpecification object ) {
			return object.getID();
		}
	}
	
	private class YDecompositionGetter implements GetterMethod<YDecomposition> {
		public String invoke( YDecomposition object ) {
			return object.getName();
		}
	}
	
	private class YExternalNetElementGetter implements GetterMethod<YExternalNetElement> {
		public String invoke( YExternalNetElement object ) {
			return object.getName();
		}
	}
}
