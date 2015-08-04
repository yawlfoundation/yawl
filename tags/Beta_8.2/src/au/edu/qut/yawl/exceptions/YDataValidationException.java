/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

import org.jdom.Element;

/**
 * 
 * @author Lachlan Aldred
 * Date: 1/09/2005
 * Time: 08:35:08
 * 
 */
public class YDataValidationException extends YDataStateException {

    public YDataValidationException(String schema, Element dataInput, String xercesErrors,
                                    String source, String message) {
        super(null, null, schema, dataInput, xercesErrors, source, message);
    }

    public String getMessage() {
        return super.getMessage();
    }




}
