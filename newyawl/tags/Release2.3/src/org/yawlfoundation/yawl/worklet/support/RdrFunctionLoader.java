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

package org.yawlfoundation.yawl.worklet.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Michael Adams
 * @date 4/04/12
 */
public class RdrFunctionLoader {

    public RdrFunctionLoader() {}


    public static List<String> getNames() {
        List<String> names = new ArrayList<String>();
        ServiceLoader<RdrFunction> loader = ServiceLoader.load(RdrFunction.class);
        for (RdrFunction func : loader) {
            names.add(func.getName());
        }
        return names;
    }


    public static String execute(String functionName, Map<String, String> args)
            throws RdrConditionException {
        ServiceLoader<RdrFunction> loader = ServiceLoader.load(RdrFunction.class);
        for (RdrFunction func : loader) {
            if (func.getName().equals(functionName)) {
                return func.execute(args);
            }
        }
        throw new RdrConditionException("Function not found: " + functionName);
    }


}
