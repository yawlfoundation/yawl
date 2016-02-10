/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.rdrutil;


/**
 *  RdrConditionException Class.
 *
 *  An RdrConditionException is thrown when an attempt is made to evaluate
 *  a rule's condition and is found to be malformed or does not evaluate
 *  to a boolean result.
 *
 *  @author Michael Adams
 *  v0.7, 10/12/2005
 */

public class RdrConditionException extends Exception {
   RdrConditionException() {
       super();
   }
   
   RdrConditionException(String message) {
       super(message);
   }

}  