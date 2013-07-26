package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.beans.editor.FilePropertyEditor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 13/07/12
 */
public class ImageFilePropertyEditor extends FilePropertyEditor {

    public ImageFilePropertyEditor() {
        super();
        removeCancelButton();
    }


    protected void customizeFileChooser(JFileChooser chooser) {
        FileFilter filter = new FileNameExtensionFilter("Image files",
                "gif", "jpg", "jpeg", "png");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filter);
    }


    private void removeCancelButton() {
        for (Component component : ((JPanel) editor).getComponents()) {
            if (component instanceof JButton && ((JButton) component).getText().equals("X")) {
                ((JPanel) editor).remove(component);
            }
        }
    }
}
