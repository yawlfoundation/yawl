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

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.validation.Validator;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.CaseParamValueDialog;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.SampleValueGenerator;
import org.yawlfoundation.yawl.editor.ui.specification.validation.SpecificationValidator;
import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationMessage;
import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationResultsParser;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.util.XNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        String caseParams = getCaseParams();
        if (caseParams == null && ! _specification.getRootNet().getInputParameters().isEmpty()) {
            return null;
        }
        return launchCase(getSpecificationID(), caseParams, null);
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


    private String getCaseParams() {
        YNet rootNet = _specification.getRootNet();
        List<YParameter> params = getCaseInputParameters(rootNet);
        if (params.isEmpty() || rootNet.getExternalDataGateway() != null) return null;

        Collections.sort(params);
        SampleValueGenerator generator = new SampleValueGenerator();
        String schema = generateSchema(generator, params, rootNet.getID());
        String sampleValue = generateSampleValues(generator, params, rootNet.getID());

        CaseParamValueDialog dialog = new CaseParamValueDialog(
                rootNet.getID(), schema, sampleValue);
        return dialog.showDialog();
    }


    private List<YParameter> getCaseInputParameters(YNet rootNet) {
        return new ArrayList<YParameter>(rootNet.getInputParameters().values());
    }


    private String generateSampleValues(SampleValueGenerator generator,
                                        List<YParameter> params, String rootName) {
        XNode root = new XNode(rootName);
        for (YParameter param : params) {
            root.addChild(param.getName(), generator.generate(param));
        }
        return root.toPrettyString();
    }


    private String generateSchema( SampleValueGenerator generator,
                                   List<YParameter> params, String rootName) {
        Map<String, Element> map = generator.getSchemaMap(_specification.getDataSchema());
        return new DataSchemaBuilder(map).buildSchema(rootName, params);
    }

}
