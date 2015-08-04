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
