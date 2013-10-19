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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.PropertiesLoader;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;

import java.io.IOException;
import java.util.Set;

public class SpecificationModel {


    private static final YSpecificationHandler _specificationHandler = new YSpecificationHandler();
    private static final SpecificationModel INSTANCE = new SpecificationModel();

    private NetModelSet nets;
    private DataSchemaValidator _schemaValidator;
    private PropertiesLoader _propertiesLoader;


    private SpecificationModel() {
        _propertiesLoader = new PropertiesLoader();
        nets = new NetModelSet(_propertiesLoader);
        reset();
    }


    public static SpecificationModel getInstance() {
        return INSTANCE;
    }

    public NetGraph newSpecification() {
        try {
            _specificationHandler.newSpecification();
            YNet net = _specificationHandler.getControlFlowHandler().getRootNet();
            NetGraph graph = new NetGraph(net);
            nets.addRootNet(graph.getNetModel());
            reset();
            return graph;
        }
        catch (YControlFlowHandlerException ycfhe) {
            // would only occur if we forgot to call handler.newSpecification first
        }
        catch (YSyntaxException yse) {
            // only thrown on bad data schema - which won't be the case here
        }
        return null;
    }


    public PropertiesLoader getPropertiesLoader() {
        return _propertiesLoader;
    }

    public void loadFromFile(String fileName) throws IOException {
        _specificationHandler.load(fileName);
        warnOnInvalidResources();
        reset();
    }

    public void loadFromXML(String xml, String layoutXML)
            throws IOException, YLayoutParseException {
        _specificationHandler.load(xml, layoutXML);
        warnOnInvalidResources();
        reset();
    }

    public void reset() {
        nets.clear();
        _schemaValidator = new DataSchemaValidator();
        YAWLEditor.getStatusBar().setText("Open or create a net to begin.");
        Publisher.getInstance().setSpecificationState(SpecificationState.NoNetsExist);
    }


    public static YSpecificationHandler getHandler() { return _specificationHandler; }

    public NetModelSet getNets() { return nets; }

    public DataSchemaValidator getSchemaValidator() { return _schemaValidator; }


    private void warnOnInvalidResources() {
        YResourceHandler resHandler = getHandler().getResourceHandler();
        if (YConnector.isResourceConnected()) {
            Set<InvalidReference> invalidSet = resHandler.getInvalidReferences();
            if (! invalidSet.isEmpty()) {
                new InvalidResourceReferencesDialog(invalidSet).setVisible(true);
            }
        }
    }

}
