/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

/*
 * 
 * @author Lachlan Aldred
 * 

 */

/**
 * Exception which indicates a failure has occured within the persistence layer of the YAWL engine.<P>
 *
 * Notes: This exception should be caught and handled as a fatal exception within the engine code. As it
 *        indicates some failure to persist a runtime object to storage, the usual action would be to gracefully
 *        terminate the engine without processing any other work.
 */
public class YPersistenceException extends YAWLException {
    public YPersistenceException(String message) {
        super(message);
    }

    public YPersistenceException(Throwable cause) {
        super(cause);
    }

    public YPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
