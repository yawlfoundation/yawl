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

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.validation.Validator;
import org.yawlfoundation.yawl.elements.YSpecification;

import java.util.List;

/**
 * A class whose sole responsibility is to provide engine validation results of the
 * current specification.
 */

public class SpecificationValidator {

    public List<String> getValidationResults() {
        return getValidationResults(Validator.ALL_MESSAGES);
    }


    public List<String> getValidationResults(int msgType) {
        return getValidationResults(
                new SpecificationWriter().cleanSpecification(), msgType);
    }


    public List<String> getValidationResults(YSpecification specification) {
        return getValidationResults(specification, Validator.ALL_MESSAGES);
    }


    public List<String> getValidationResults(YSpecification specification, int msgType) {
        return new Validator().validate(specification, msgType);
    }

}
