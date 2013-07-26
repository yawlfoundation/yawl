package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.net.ImageFilter;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public abstract class YPropertiesBean {

    protected YSpecificationHandler specificationHandler;
    protected NetGraph graph;
    protected NetGraphModel model;

    public YPropertiesBean() {
        specificationHandler = SpecificationModel.getHandler();
    }


    protected void setGraph(NetGraph g) {
        graph = g;
        model = SpecificationModel.getInstance().getNet(graph.getName());
    }


    protected NetGraph getGraph() { return graph; }

    protected NetGraphModel getModel() { return model; }

    protected YPropertySheet getSheet() {
        return YAWLEditor.getInstance().getPropertySheet();
    }

    protected YNet getSelectedYNet() {
        return YAWLEditorDesktop.getInstance().getSelectedYNet();
    }


    protected void actionPerformed(ActionEvent event) {
        JFileChooser chooser = new JFileChooser("Select Background Image for Net");
        chooser.setFileFilter(new ImageFilter());
        int result = chooser.showOpenDialog(YAWLEditor.getInstance());
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getCanonicalPath();
                ImageIcon bgImage = ResourceLoader.getExternalImageAsIcon(path);
                if (bgImage != null) {
                    bgImage.setDescription(path);   // store path
                    graph.setBackgroundImage(bgImage);
                    SpecificationUndoManager.getInstance().setDirty(true);
                }
            }
            catch (IOException ioe) {
                // ignore
            }
        }
    }


    protected void setDirty() {
        SpecificationUndoManager.getInstance().setDirty(true);
    }


    protected void setReadOnly(String propertyName, boolean isReadOnly) {
        getSheet().setReadOnly(propertyName, isReadOnly);
    }


    protected void firePropertyChange(String propertyName, Object newValue) {
        getSheet().firePropertyChange(propertyName, newValue);
    }


    protected Color hexToColor(String hexStr) {

        // expects the format #123456
        if ((hexStr == null) || (hexStr.length() < 7)) {
            return Color.WHITE;
        }

        try {
            int r = Integer.valueOf(hexStr.substring(1, 3), 16);
            int g = Integer.valueOf(hexStr.substring(3, 5), 16);
            int b = Integer.valueOf(hexStr.substring(5, 7), 16);
            return new Color(r, g, b);
        }
        catch (NumberFormatException nfe) {
            return Color.WHITE;
        }
    }

    protected String colorToHex(Color color) {
        String hex = "#";
        hex += intToHex(color.getRed());
        hex += intToHex(color.getGreen());
        hex += intToHex(color.getBlue());
        return hex;
    }

    private String intToHex(int i) {
        String hex = Integer.toHexString(i).toUpperCase();
        if (hex.length() == 1) hex = "0" + hex;
        return hex;
    }
}
