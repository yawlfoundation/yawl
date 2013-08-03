/*
 * Created on 05/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifiers;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.PropertiesLoader;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

public class SpecificationModel {

    public static final int   DEFAULT_FONT_SIZE = 15;
    public static final int   DEFAULT_NET_BACKGROUND_COLOR = Color.WHITE.getRGB();
    public static final String DEFAULT_TYPE_DEFINITION =
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";

    private static final YSpecificationHandler _specificationHandler = new YSpecificationHandler();
    private static final SpecificationModel INSTANCE = new SpecificationModel();

    private NetModelSet nets;
    private String dataTypeDefinition = DEFAULT_TYPE_DEFINITION;
    private ElementIdentifiers uniqueIdentifiers;
    private int     fontSize;
    private int     defaultNetBackgroundColor;
    private Color   defaultVertexBackground;
    private YSpecVersion versionNumber;
    private YSpecVersion prevVersionNumber;
    private boolean _versionChanged;
    private DataSchemaValidator _schemaValidator;
    private PropertiesLoader _propertiesLoader;


    private SpecificationModel() {
        _propertiesLoader = new PropertiesLoader(this);
        nets = new NetModelSet(_propertiesLoader);
        uniqueIdentifiers = _specificationHandler.getControlFlowHandler().getIdentifiers();
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
            nets.addNoUndo(graph.getNetModel());
            reset();
            return graph;
        }
        catch (YControlFlowHandlerException ycfhe) {
            // only occurs if we forgot to call handler.newSpecification first
        }
        return null;
    }


    public void loadFromFile(String fileName) throws IOException {
        _specificationHandler.load(fileName);
        warnOnInvalidResources();
        reset();
    }

    public void reset() {
        nets.clear();
        _schemaValidator = new DataSchemaValidator();
        fontSize = DEFAULT_FONT_SIZE;
        defaultNetBackgroundColor = DEFAULT_NET_BACKGROUND_COLOR;
        defaultVertexBackground = getPreferredVertexBackground();
        setDataTypeDefinition(DEFAULT_TYPE_DEFINITION);
        setVersionNumber(new YSpecVersion("0.0"));
        YAWLEditor.getStatusBar().setText("Open or create a net to begin.");
        getPublisher().setSpecificationState(SpecificationState.NoNetsExist);
        prevVersionNumber = null;
        setVersionChanged(false);
    }


    public static YSpecificationHandler getHandler() { return _specificationHandler; }

    public NetModelSet getNets() { return nets; }

    public DataSchemaValidator getSchemaValidator() { return _schemaValidator; }


    public String getFileName() {
        return _specificationHandler.getFileName();
    }


    public String getDataTypeDefinition() {
        return (dataTypeDefinition.contains("\n")) ? dataTypeDefinition :
                DEFAULT_TYPE_DEFINITION ;
    }

    public void setDataTypeDefinition(String dataTypeDefinition) {
        this.dataTypeDefinition = dataTypeDefinition;
        _schemaValidator.setDataTypeSchema(dataTypeDefinition);
    }


    public void setFontSize(int oldSize, int newSize) {
        if (oldSize == newSize) {
            return;
        }
        setFontSize(newSize);
        nets.propagateSpecificationFontSize(oldSize, newSize);
    }

    public void setFontSize(int size) {
        this.fontSize = size;
    }

    public int getFontSize() {
        return this.fontSize;
    }


    public void setDefaultNetBackgroundColor(int color) {
        defaultNetBackgroundColor = color;
    }

    public int getDefaultNetBackgroundColor() {
        return this.defaultNetBackgroundColor;
    }

    public void setDefaultVertexBackgroundColor(Color color) {
        defaultVertexBackground = color;
        setPreferredVertexBackground(color);
    }

    public Color getDefaultVertexBackgroundColor() {
        return this.defaultVertexBackground;
    }


    public void setVersionNumber(YSpecVersion version) {
        if (versionNumber != null) {
            _versionChanged = (! versionNumber.equals(version));
            if (_versionChanged) {
                prevVersionNumber = versionNumber;
            }
        }
        versionNumber = version;
    }

    public YSpecVersion getVersionNumber() {
        return this.versionNumber;
    }

    public void setVersionChanged(boolean b) {
        _versionChanged = b;
        if (! _versionChanged) prevVersionNumber = null;
    }


    private Publisher getPublisher() { return Publisher.getInstance(); }

    private void warnOnInvalidResources() {
        YResourceHandler resHandler = getHandler().getResourceHandler();
        if (YConnector.isResourceConnected()) {
            Set<InvalidReference> invalidSet = resHandler.getInvalidReferences();
            if (! invalidSet.isEmpty()) {
                new InvalidResourceReferencesDialog(invalidSet).setVisible(true);
            }
        }
    }


    private Color getPreferredVertexBackground() {
        return new Color(UserSettings.getSettings().getInt(
                "PREFERRED_VERTEX_BACKGROUND_COLOR", Color.WHITE.getRGB()));
    }

    private void setPreferredVertexBackground(Color color) {
        UserSettings.getSettings().putInt(
                "PREFERRED_VERTEX_BACKGROUND_COLOR", color.getRGB());
    }



}
