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

import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.core.exception.IllegalIdentifierException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class NetProperties extends YPropertiesBean {

    protected boolean idLabelSynch;       // set by Cell & MultiCell Properties


    public NetProperties() {
        super(YAWLEditor.getPropertySheet());
    }


    /*** SPEC PROPERTIES ***/

    public String getUri() { return specHandler.getURI(); }

    public void setUri(String uri) {
        try {
            specHandler.setURI(uri);
        }
        catch (IllegalIdentifierException iie) {
            showWarning("Invalid Specification Name",
                  "Specification Name cannot be blank or contain invalid XML characters");
            firePropertyChange("Uri", specHandler.getURI());
        }
    }


    public String getTitle() { return specHandler.getTitle(); }

    public void setTitle(String title) { specHandler.setTitle(title); }


    public String getDescription() {
        String description = specHandler.getDescription();
        return description.equals("No description provided") ? "" : description;
    }

    public void setDescription(String desc) { specHandler.setDescription(desc); }


    public String getAuthors() {
        return StringUtil.join(specHandler.getAuthors(), ',');
    }

    public void setAuthors(String authors) {
        specHandler.setAuthors(StringUtil.splitToList(authors, ","));
    }


    public String getVersion() {
        return specHandler.getVersion().toString();
    }

    public void setVersion(String version) {
        try {
            specHandler.setVersion(validateVersion(version));
        }
        catch (IllegalArgumentException iae) {
            showWarning("Invalid Version", iae.getMessage());
            firePropertyChange("Version", specHandler.getVersion().toString());
        }
    }


    public String getDataSchema() {
        try {
            int typeCount = specHandler.getDataHandler().getUserDefinedTypeNames().size();
            return typeCount + " defined type" + (typeCount != 1 ? "s" : "");
        }
        catch (YDataHandlerException ydhe) {
            return null;
        }
    }

    public void setDataSchema(String schema) {
        // nothing to do - updates handled by dialog
    }


    /**** NET PROPERTIES ****/


    public String getName() { return graph.getName(); }

    public void setName(String name) {
        String oldName = getName();
        if (oldName.equals(name)) return;
        try {
            String newName = XMLUtilities.toValidXMLName(name);
            flowHandler.checkDecompositionID(newName);  // throws exception if invalid
            specHandler.getDataHandler().renameDecomposition(oldName, newName);
            graph.setName(newName);
            SpecificationModel.getNets().propagateDecompositionNameChange(
                    model.getDecomposition(), oldName);
            setDirty();
        }
        catch (Exception e) {
            showWarning("Net Rename Error", e.getMessage());
            firePropertyChange("Name", oldName);
        }
    }


    public boolean isRootNet() {

        // model is null when a new spec is first created
        return model == null || model.isRootNet();
    }

    public void setRootNet(boolean value) { model.setIsRootNet(value); }


    public Color getNetFillColor() { return graph.getBackground(); }

    public void setNetFillColor(Color value) {
        graph.setBackground(value);
        setDirty();
    }


    public String getDataGateway() {
        String gateway = model != null ? model.getExternalDataGateway() : null;
        return gateway != null ? gateway : "None";
    }

    public void setDataGateway(String value) {
        model.setExternalDataGateway(value);
        setDirty();
    }


    public NetTaskPair getDataVariables() {
        return new NetTaskPair((YNet) model.getDecomposition(), null, null);
    }

    public void setDataVariables(NetTaskPair value) {
        // nothing to do - updates handled by dialog
    }


    public File getBackgroundImage() {
        ImageIcon bgImage = graph.getBackgroundImage();
        return bgImage != null ? new File(bgImage.getDescription()) : null;
    }

    public void setBackgroundImage(File file) {
        if (file != null) {
            String path = file.getAbsolutePath();
            ImageIcon bgImage = ResourceLoader.getExternalImageAsIcon(path);
            if (bgImage != null) {
                bgImage.setDescription(path);   // store path
                graph.setBackgroundImage(bgImage);
                graph.repaint();
                setDirty();
            }
        }
        else {   // remove existing
            graph.setBackgroundImage(null);
            graph.repaint();
            setDirty();
        }
    }

    private YSpecVersion validateVersion(String version) throws IllegalArgumentException {
        if (StringUtil.strToDouble(version, 0) == 0) {
            throw new IllegalArgumentException("Invalid version format");
        }
        YSpecVersion oldVersion = specHandler.getVersion();
        YSpecVersion newVersion = new YSpecVersion(version);
        if (newVersion.compareTo(oldVersion) < 0) {
           throw new IllegalArgumentException(
                   "Version cannot be less than the current version");
        }
        return newVersion;
    }

}
