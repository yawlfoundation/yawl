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

package org.yawlfoundation.yawl.editor.core.layout;

/**
 * An enumeration of decorator positions, following the Java convention
 *
 * @author Michael Adams
 * @date 20/06/12
*/
public enum DecoratorPosition {

    // the indices match the legacy jgraph ordinals
    North(10), South(11), West(12), East(13);

    private int index;

    private DecoratorPosition(int i) {index = i; }

    public int getCardinality() { return index; }

    public static DecoratorPosition valueOf(int i) {
        switch(i) {
            case 10 : return North;
            case 11 : return South;
            case 12 : return West;
            default : return East;
        }
    }
}
