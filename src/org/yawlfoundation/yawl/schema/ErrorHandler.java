/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.schema;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the SAX ErrorHandler interface
 * allowing customised handling of all validation errors
 * in an XML document. This handler only terminates at the
 * end of the document or a fatal error. All other errors
 * are recorded for future retrieval to allow complete parsing
 * of the XML document.
 *
 * @author Mike Fowler
 *         Date: 04-Jul-2006
 */
public class ErrorHandler implements org.xml.sax.ErrorHandler
{
    // Contains all warnings since the last reset.
    private List<String> warnings = new ArrayList<String>();

    // Contains all errors since the last reset.
    private List<String> errors = new ArrayList<String>();


    /**
     * Clear the messages from any previous run
     */
    public void reset() {
        warnings.clear();
        errors.clear();
    }

    /**
     * @return String list of all warnings contained in the parsed document.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * @return String list of all errors contained in the parsed document.
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * @return true if there were no errors, false otherwise
     */
    public boolean isValid() {
        return errors.size() == 0;
    }

    /**
     * Receive notification of a warning.
     * <p/>
     * <p>SAX parsers will use this method to report conditions that
     * are not errors or fatal errors as defined by the XML
     * recommendation.  The default behaviour is to take no
     * action.</p>
     * <p/>
     * <p>The SAX parser must continue to provide normal parsing events
     * after invoking this method: it should still be possible for the
     * application to process the document through to the end.</p>
     * <p/>
     * <p>Filters may use this method to report other, non-XML warnings
     * as well.</p>
     *
     * @param e The warning information encapsulated in a
     *                  SAX parse exception.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void warning(SAXParseException e) throws SAXException {
        warnings.add("Warning: " + e.getLineNumber() + ":" + e.getColumnNumber() +
                      ": " + e.getMessage());
    }

    /**
     * Receive notification of a recoverable error.
     * <p/>
     * <p>This corresponds to the definition of "error" in section 1.2
     * of the W3C XML 1.0 Recommendation.  For example, a validating
     * parser would use this callback to report the violation of a
     * validity constraint.  The default behaviour is to take no
     * action.</p>
     * <p/>
     * <p>The SAX parser must continue to provide normal parsing
     * events after invoking this method: it should still be possible
     * for the application to process the document through to the end.
     * If the application cannot do so, then the parser should report
     * a fatal error even if the XML recommendation does not require
     * it to do so.</p>
     * <p/>
     * <p>Filters may use this method to report other, non-XML errors
     * as well.</p>
     *
     * @param e The error information encapsulated in a
     *                  SAX parse exception.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void error(SAXParseException e) throws SAXException {
        errors.add("Error: " + e.getLineNumber() + ":" + e.getColumnNumber() +
                    ": " + e.getMessage());
    }

    /**
     * Receive notification of a non-recoverable error.
     * <p/>
     * <p><strong>There is an apparent contradiction between the
     * documentation for this method and the documentation for {@link
     * org.xml.sax.ContentHandler#endDocument}.  Until this ambiguity
     * is resolved in a future major release, clients should make no
     * assumptions about whether endDocument() will or will not be
     * invoked when the parser has reported a fatalError() or thrown
     * an exception.</strong></p>
     * <p/>
     * <p>This corresponds to the definition of "fatal error" in
     * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
     * parser would use this callback to report the violation of a
     * well-formedness constraint.</p>
     * <p/>
     * <p>The application must assume that the document is unusable
     * after the parser has invoked this method, and should continue
     * (if at all) only for the sake of collecting additional error
     * messages: in fact, SAX parsers are free to stop reporting any
     * other events once this method has been invoked.</p>
     *
     * @param e The error information encapsulated in a
     *                  SAX parse exception.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void fatalError(SAXParseException e) throws SAXException {
        errors.add("Fatal Error: " + e.getLineNumber() + ":" + e.getColumnNumber() +
                   ": " + e.getMessage());
        throw e;
    }
}
