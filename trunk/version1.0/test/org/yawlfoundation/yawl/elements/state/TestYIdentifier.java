/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.state;

import junit.framework.TestCase;

import java.util.List;
import java.util.Vector;

import org.yawlfoundation.yawl.exceptions.YPersistenceException;


/**
 * @author aldredl
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestYIdentifier extends TestCase {
	
	private YIdentifier id1 = new YIdentifier();
	private YIdentifier id2 = new YIdentifier();
	
	
	/**
	 * Constructor for IdentifierTest.
	 */	
	public TestYIdentifier(String name)
	{
		super(name);
	}
	
	
	
	
	public void testEquals()
	{
		assertFalse(id1.equals(id2));
		assertTrue( id1.equals(id1));
	}
	
	
	
	public void testCreateChild() throws YPersistenceException
	{
		YIdentifier id = new YIdentifier();
		YIdentifier id3 = id.createChild(null);
		assertTrue(id3.isImmediateChildOf(id));
		assertFalse(id3.isImmediateChildOf(id2));
		assertFalse(id3.isImmediateChildOf(id1));
		assertFalse(id3.isImmediateChildOf(id3));
	}
	
	
	
	public void testGetChildren() throws YPersistenceException
	{
		YIdentifier id  = new YIdentifier();
		YIdentifier id3 = id.createChild(null);
		YIdentifier id4 = id.createChild(null);
		List children = id.getChildren();
		assertTrue(id3.isImmediateChildOf(id));
		assertTrue(id4.isImmediateChildOf(id));
		assertTrue(children.size() == 2);
	}
	
	
	
	public void testIsChildOf() throws YPersistenceException
	{
		YIdentifier parent = new YIdentifier();
		YIdentifier child = parent.createChild(null);
		assertTrue(child.isImmediateChildOf(parent));
		assertFalse(child.isImmediateChildOf(id1));
	}
	
	
	
	public void testGetDescendants() throws YPersistenceException
	{
		List descendants = new Vector();
		YIdentifier parent = new YIdentifier();
		descendants.add(parent);
		YIdentifier child1 = parent.createChild(null);
		descendants.add(child1);
		YIdentifier child2 = parent.createChild(null);
		descendants.add(child2);
		YIdentifier child1Child1 = child1.createChild(null);
		descendants.add(child1Child1);
		YIdentifier child1Child2 = child1.createChild(null);
		descendants.add(child1Child2);
		YIdentifier child2Child1 = child2.createChild(null);
		descendants.add(child2Child1);
		YIdentifier child2Child2 = child2.createChild(null);
		descendants.add(child2Child2);
		assertTrue(parent.getDescendants().containsAll(descendants));
		assertTrue(descendants.size()==parent.getDescendants().size());
	}
}
