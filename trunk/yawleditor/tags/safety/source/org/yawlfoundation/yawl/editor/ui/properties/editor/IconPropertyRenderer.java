package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

import java.io.File;

/**
 * @author Michael Adams
 * @date 13/07/12
 */
public class IconPropertyRenderer extends DefaultCellRenderer {

    // show the icon file absolute path as a simple file name (no path, no extension)
    protected String convertToString(Object value) {
        String path = null;
        if (value != null) {
            path = (String) value;
            path = path.substring(path.lastIndexOf(File.separatorChar) + 1);
            path = path.substring(0, path.lastIndexOf('.'));
        }
        return path;
    }

}
