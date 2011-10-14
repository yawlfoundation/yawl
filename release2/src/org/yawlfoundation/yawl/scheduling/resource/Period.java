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

package org.yawlfoundation.yawl.scheduling.resource;

import java.util.Date;

import org.yawlfoundation.yawl.scheduling.util.Utils;


/**
 * mock class
 * 
 * @author tbe
 * @version $Id: Period.java 28230 2011-03-28 16:40:09Z tbe $
 */
public class Period {
	public Date from;
	public Date to;
	
	public Period(Date from, Date to) {
		this.from = from;
		this.to = to;
	}
	
	public String toString() {
		return "["+Utils.toString(this.from)+", "+ Utils.toString(this.to)+"]";
	}
}
