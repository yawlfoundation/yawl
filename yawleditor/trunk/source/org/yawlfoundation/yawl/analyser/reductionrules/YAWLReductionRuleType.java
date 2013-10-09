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

package org.yawlfoundation.yawl.analyser.reductionrules;

/**
 * @author Michael Adams
 * @date 14/05/12
 */
public enum YAWLReductionRuleType {

    FSPY (FSPYrule.class),
    FSTY (FSTYrule.class),
    FPPY (FPPYrule.class),
    FPTY (FPTYrule.class),
    FAPY (FAPYrule.class),
    FATY (FATYrule.class),
    ELTY (ELTYrule.class),
    ELPY (ELPYrule.class),
    FXOR (FXORrule.class),
    FAND (FANDrule.class),
    FIE (FIErule.class),
    FOR (FORrule.class);


    private Class ruleClass;


    private YAWLReductionRuleType(Class clazz) {
        ruleClass = clazz;
    }

    public YAWLReductionRule getRule() {
        try {
            return (YAWLReductionRule) ruleClass.newInstance();
        }
        catch (Exception e) {
            return null;
        }
    }


}
