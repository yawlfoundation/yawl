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

package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.List;

/**
 * Implementers of this interface are contracted to verify themselves against
 * YAWL language semantics.
 * @author Lachlan Aldred
 * @since 0.1
 * @date 27/10/2003
 */
public interface YVerifiable {
    /**
     * Internally verify the object against YAWL language semantics and
     * report any errors and/or warnings.
     * @see org.yawlfoundation.yawl.util.YVerificationMessage
     * @return a List of YVerificationMessage objects
     */
    List<YVerificationMessage> verify();
}
