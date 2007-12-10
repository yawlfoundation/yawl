/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.exceptions;

import junit.framework.TestCase;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class TestYConnectivityException extends TestCase {

	private String messageIn = "This is an unexceptional exception";

	public TestYConnectivityException(String name)
	{
		super(name);
	}

	public void testConstructor()
	{
		YConnectivityException yce = new YConnectivityException(messageIn);
		String messageOut = yce.getMessage();
		this.assertEquals(messageOut, messageIn);
	}
}
