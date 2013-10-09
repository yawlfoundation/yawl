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

import org.yawlfoundation.yawl.elements.YCondition;

import java.text.NumberFormat;

/**
 *  Stores the layout information for a particular condition
 *
 * @author Michael Adams
 * @date 19/06/12
 */
public class YConditionLayout extends YNetElementNode {

    private YCondition _condition;


    /**
     * Creates a new YConditionLayout object
     * @param condition the YCondition this layout describes
     * @param formatter a number format for the specific locale
     */
    public YConditionLayout(YCondition condition, NumberFormat formatter) {
        _condition = condition;
        setID(condition.getID());
        setNumberFormatter(formatter);
    }


    public YCondition getCondition() { return _condition; }

    public void setCondition(YCondition condition) { _condition = condition; }

}
