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

package org.yawlfoundation.yawl.elements.predicate;

import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.elements.data.external.AbstractExternalDBGateway;
import org.yawlfoundation.yawl.util.PluginLoaderUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of classes in this package that
 * implement the PredicateEvaluator interface.
 *
 * @author Michael Adams
 * @date 05/12/2012
 */

public class PredicateEvaluatorFactory {

    private static Map<String, Class<PredicateEvaluator>> _classMap;

    private static final String BASE_PACKAGE = "org.yawlfoundation.yawl.elements.predicate.";
    private static final PluginLoaderUtil _loader = new PluginLoaderUtil();


    private PredicateEvaluatorFactory() { }


    public static void setExternalPaths(String externalPaths) {
         _loader.setExternalPaths(externalPaths);
    }


    public static Set<PredicateEvaluator> getInstances() {
        return _loader.toInstanceSet(getClassMap().values());
    }


    private static Map<String, Class<PredicateEvaluator>> getClassMap() {
        if (_classMap == null) {
            _classMap = _loader.load(PredicateEvaluator.class);
        }
        return _classMap;
    }


}
