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

package org.yawlfoundation.yawl.util;

/**
 * 
 * @author Lachlan Aldred
 * Date: 7/10/2003
 * Time: 14:21:11
 * 
 */
public class YVerificationMessage {
    private Object _source;
    private String _message;


    public YVerificationMessage(Object source, String message) {
        _source = source;
        _message = message;
    }


    public Object getSource() { return _source; }


    public String getMessage() { return _message; }


    public void setSource(Object source) { _source = source; }

}
