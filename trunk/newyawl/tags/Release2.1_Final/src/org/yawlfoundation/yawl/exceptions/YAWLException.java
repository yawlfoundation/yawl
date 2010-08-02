/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.exceptions;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 26/11/2004
 * Time: 15:26:54
 */
public class YAWLException extends Exception {
    protected static SAXBuilder _builder = new SAXBuilder();
    protected String _message;
    public static final String MESSAGE_NM = "message";

    public YAWLException() {
    }

    public YAWLException(String message) {
        _message = message;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public YAWLException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public YAWLException(String message, Throwable cause) {
        super(cause);
        _message = message;
    }

    public String getMessage() {
        return _message;
    }

    public String toXML() {
        return "<" + this.getClass().getName() + ">" +
                toXMLGuts() +
                "</" + this.getClass().getName() + ">";
    }

    protected String toXMLGuts() {
        return "<message>" + getMessage() + "</message>";
    }

    public static YAWLException unmarshal(Document exceptionDoc) throws JDOMException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String exceptionType = exceptionDoc.getRootElement().getName();
        if ("YDataStateException".equals(exceptionType)) {
            return YDataStateException.unmarshall(exceptionDoc);
        }
        if ("YDataQueryException".equals(exceptionType)) {
            return YDataQueryException.unmarshall(exceptionDoc);
        }
        if ("YDataValidationException".equals(exceptionType)) {
            return YDataValidationException.unmarshall(exceptionDoc);
        }
        YAWLException e = (YAWLException) Class.forName(exceptionType).newInstance();
        e.setMessage(parseMessage(exceptionDoc));
        return e;
    }

    protected static String parseMessage(Document exceptionDoc) {
        return exceptionDoc.getRootElement().getChildText(MESSAGE_NM);
    }

    public void setMessage(String message) {
        _message = message;
    }


    /**
     * A convenience method that effectively rethrows the exceptions listed.
     * Caveat: ALL 5 exceptions must appear in the throws clause of any methods that
     * call this method.
     * @throws YStateException
     * @throws YDataStateException
     * @throws YQueryException
     * @throws YSchemaBuildingException
     * @throws YPersistenceException
     */

    public void rethrow() throws YStateException, YDataStateException, YQueryException,
                                 YSchemaBuildingException, YPersistenceException
    {
        if (this instanceof YStateException) {
            throw (YStateException) this;
        } else if (this instanceof YDataStateException) {
            throw (YDataStateException) this;
        } else if (this instanceof YQueryException) {
            throw (YQueryException) this;
        } else if (this instanceof YSchemaBuildingException) {
            throw (YSchemaBuildingException) this;
        } else if (this instanceof YPersistenceException) {
            throw (YPersistenceException) this;
        }

    }
}
