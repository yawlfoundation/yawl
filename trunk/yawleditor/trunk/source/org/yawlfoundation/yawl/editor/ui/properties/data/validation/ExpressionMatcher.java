/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import java.util.regex.Pattern;

/**
 * @author Michael Adams
 * @date 17/02/15
 */
public class ExpressionMatcher {

    private Pattern pattern;


    public ExpressionMatcher(String regex) {
        pattern = Pattern.compile(regex);
    }


    public boolean contains(String predicate) {
        return pattern.matcher(predicate).find();
    }


    public String replaceAll(String predicate, String value) {
        return pattern.matcher(predicate).replaceAll(value);
    }

}
