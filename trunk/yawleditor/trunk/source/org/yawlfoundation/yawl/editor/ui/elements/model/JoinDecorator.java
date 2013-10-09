/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.elements.model;

public class JoinDecorator extends Decorator {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * This constructor is ONLY to be invoked when we are reconstructing a decorator
     * from saved state. Ports will not be created with this constructor, as they
     * are already part of the JGraph state-space.
     */

    public JoinDecorator() {
        super();
    }

    /**
     * This constructor is to be invoked whenever we are creating a new decorator
     * from scratch. It also creates the correct ports needed for the decorator
     * as an intended side-effect.
     */

    public JoinDecorator(YAWLTask task, int type, int position) {
        super(task, type, position);
    }

    public static int getDefaultPosition() {
        return Decorator.LEFT;
    }

    public boolean generatesOutgoingFlows() {
        return false;
    }

    public boolean acceptsIncomingFlows() {
        return true;
    }

    public String toString() {
        switch (getType()) {
            case Decorator.NO_TYPE:
            default: {
                return null;
            }
            case Decorator.AND_TYPE: {
                return "AND join";
            }
            case Decorator.OR_TYPE: {
                return "OR join";
            }
            case Decorator.XOR_TYPE: {
                return "XOR join";
            }
        }
    }
}
