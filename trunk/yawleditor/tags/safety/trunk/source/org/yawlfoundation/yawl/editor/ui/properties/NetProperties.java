package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.net.ImageFilter;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class NetProperties extends YPropertiesBean {

    public NetProperties() {
        super();
    }


    /*** SPEC PROPERTIES ***/

    public String getUri() { return specification.getID().getUri(); }

    public void setUri(String uri) { specification.getID().setUri(uri); }


    public String getTitle() { return specification.getTitle(); }

    public void setTitle(String title) { specification.setTitle(title); }


    public String getDescription() { return specification.getDescription(); }

    public void setDescription(String desc) { specification.setDescription(desc); }


    public String getAuthors() {
        return StringUtil.join(specification.getAuthors(), ',');
    }

    public void setAuthors(String authors) {
        specification.setAuthors(StringUtil.splitToList(authors, ","));
    }


    public double getVersion() {
        return SpecificationModel.getInstance().getVersionNumber().toDouble();
    }

    public void setVersion(double version) {
        YSpecVersion specVersion;
        try {
            specVersion = new YSpecVersion(String.valueOf(version));
        }
        catch (NumberFormatException nfe) {
            specVersion = SpecificationModel.getInstance().getVersionNumber(); // rollback
        }
        SpecificationModel.getInstance().setVersionNumber(specVersion);
    }


    public String getDataSchema() {
        return SpecificationModel.getInstance().getDataTypeDefinition();
    }

    public void setDataSchema(String schema) {
        SpecificationModel.getInstance().setDataTypeDefinition(schema);
    }

    /**** NET PROPERTIES ****/


    public String getName() { return graph.getName(); }

    public void setName(String value) {
        String oldValue = getName();
        graph.setName(value);
        SpecificationModel.getInstance().propogateDecompositionLabelChange(
                model.getDecomposition(), oldValue);
        setDirty();
    }


    public boolean isRootNet() {

        // model is null when a new spec is first created
        return model == null || model.isStartingNet();
    }

    public void setRootNet(boolean value) { model.setIsStartingNet(value); }


    public Color getNetFillColor() { return graph.getBackground(); }

    public void setNetFillColor(Color value) {
        graph.setBackground(value);
        setDirty();
    }


    public String getDataGateway() {
        String gateway = model.getExternalDataGateway();
        return gateway != null ? gateway : "None";
    }

    public void setDataGateway(String value) {
        model.setExternalDataGateway(value);
        setDirty();
    }


    public NetTaskPair getDataVariables() {
        return new NetTaskPair(specification.getRootNet());
    }

    public void setDataVariables(NetTaskPair value) {
        // nothing to do - updates handled by dialog
    }


    public File getBackgroundImage() {
        ImageIcon bgImage = graph.getBackgroundImage();
        return bgImage != null ? new File(bgImage.getDescription()) : null;
    }

    public void setBackgroundImage(File file) {
        String path = file.getAbsolutePath();
        ImageIcon bgImage = ResourceLoader.getExternalImageAsIcon(path);
        if (bgImage != null) {
            bgImage.setDescription(path);   // store path
            graph.setBackgroundImage(bgImage);
            graph.repaint();
            setDirty();
        }
    }

    public void actionPerformed(ActionEvent event) {
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


}
