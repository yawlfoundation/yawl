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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the properties for a CellProperties backing Bean
 *
 * @author Michael Adams
 * @date 4/07/12
 */
public class NetBeanInfo extends YBeanInfo {

    public NetBeanInfo() {
        this(NetProperties.class);
    }

    public NetBeanInfo(Class subClass) {
        super(subClass);
        addSpecProperties();
        addNetProperties();
    }


    private void addSpecProperties() {
        String category = "Specification";
        addProperty("Authors", category, null, "Name(s) of the specification's authors. " +
                "Multiple author names should be separated with commas ','");
        addProperty("DataSchema", category, "Data Definitions", "Define data types")
                .setPropertyEditorClass(DataDefinitionPropertyEditor.class);
        addProperty("Description", category, null, "A description of the specification");
        addProperty("Uri", category, "Name", "Name identifier (must not be blank)");
        addProperty("Title", category, null, "A user title");
        addProperty("Version", category, "Version Number", "Version number of the " +
                "specification. Consists of a major part and a minor part");
    }


    private void addNetProperties() {
        String category = "Net";
        addProperty("BackgroundImage", category, "Background Image",
                "Set the path to an image file to use as this net's background")
                .setPropertyEditorClass(ImageFilePropertyEditor.class);
        addProperty("DataGateway", category, "Data Gateway",
                "The name of an external data gateway to use for net-level data")
                .setPropertyEditorClass(DataGatewayEditor.class);
        addProperty("LogPredicate", category, "Log Entries",
                "Logs the specified text when the net starts and/or completes at runtime")
                .setPropertyEditorClass(LogPredicatePropertyEditor.class);
        addProperty("NetFillColor", category, "Fill Colour",
                "The background colour of this net");
        addProperty("Name", category, null, "Name of this net");

        addProperty("DataVariables", category, "Data Variables", "")
                .setPropertyEditorClass(DataVariablePropertyEditor.class);

        addProperty("RootNet", category, "Root Net", "This net is the root or starting net");
    }


    /******************************************************************************/

    public static class DataGatewayEditor extends ComboPropertyEditor {

        public DataGatewayEditor() {
   	        super();
            List<String> gateways = new ArrayList<String>();
            gateways.add("None");
            try {
                gateways.addAll(YConnector.getExternalDataGateways().keySet());
            }
            catch (IOException ioe) {
                // fall through to just "None"
            }
            setAvailableValues(gateways.toArray());
   	    }
   	}

}
