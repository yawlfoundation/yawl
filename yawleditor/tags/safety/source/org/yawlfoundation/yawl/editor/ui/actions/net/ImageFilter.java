package org.yawlfoundation.yawl.editor.ui.actions.net;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 5/07/12
 */
public class ImageFilter extends FileFilter {

    private static final List<String> validExtns =
            Arrays.asList("tiff", "tif", "gif", "jpeg", "jpg", "png");

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        return extension != null && validExtns.contains(extension);
    }

    //The description of this filter
    public String getDescription() {
        return "Image Files";
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
