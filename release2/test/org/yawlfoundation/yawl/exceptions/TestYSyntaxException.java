package org.yawlfoundation.yawl.exceptions;

import junit.framework.TestCase;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class TestYSyntaxException extends TestCase {

	private String messageIn = "This is an unexceptional exception";



	/**
	 * Constructor for TestYSyntaxException.
	 * @param name
	 */
	public TestYSyntaxException(String name) {
		super(name);
	}

    public void testConstructor()
    {
        YSyntaxException syntaxException = new YSyntaxException(messageIn);
        assertEquals(syntaxException.getMessage(), messageIn);
    }





}
