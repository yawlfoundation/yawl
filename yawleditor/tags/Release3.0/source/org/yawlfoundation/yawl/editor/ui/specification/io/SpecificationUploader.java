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

package org.yawlfoundation.yawl.editor.ui.specification.io;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.validation.Validator;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.validation.SpecificationValidator;
import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationMessage;
import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationResultsParser;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.logging.YLogDataItemList;

import java.io.IOException;
import java.util.List;

/**
 * @author Michael Adams
 * @date 18/09/13
 */
public class SpecificationUploader {

    private YSpecification _specification;   // the currently loaded specification

    public SpecificationUploader() { }


    public String upload(boolean unloadPreviousVersions, boolean cancelCases)
            throws IOException {
        if (! validate(getSpecification())) {
            throw new IOException("Invalid Specification");
        }
        if (unloadPreviousVersions) {
            YConnector.unloadAllVersions(getSpecificationID(), cancelCases);
        }
        return YConnector.uploadSpecification(getSpecification());
    }


    public String launchCase() throws IOException {
        return launchCase(getSpecificationID(), null, null);
    }


    public String launchCase(YSpecificationID specID, String caseParams,
                             YLogDataItemList logList) throws IOException {
        return YConnector.launchCase(specID, caseParams, logList);
    }


    public void storeLayout() {
        String layoutXML = new LayoutExporter().export();
        if (layoutXML != null) {
            LayoutRepository.getInstance().add(getSpecificationID(), layoutXML);
        }
    }


    private YSpecification getSpecification() {
        if (_specification == null) {
            _specification = new SpecificationWriter().cleanSpecification();
            _specification.getSpecificationID();  // ensure specID is constructed
        }
        return _specification;
    }


    private YSpecificationID getSpecificationID() {
        return getSpecification().getSpecificationID();
    }


    private boolean validate(YSpecification specification) {
        List<String> errors = new SpecificationValidator().getValidationResults(
                specification, Validator.ERROR_MESSAGES);
        List<ValidationMessage> messages = new ValidationResultsParser().parse(errors);
        YAWLEditor.getInstance().showProblemList("Validation Results", messages);
        return errors.isEmpty();
    }

}
