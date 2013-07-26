package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.imgscalr.Scalr;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Adams
 * @date 24/07/12
 */
public class IconPropertyEditor extends ComboPropertyEditor {

    private File[] fileList;

    private static String BASE_PATH;
    private static final String INTERNAL_PATH =
            "org/yawlfoundation/yawl/editor/ui/resources/taskicons";

    public IconPropertyEditor() {
        super();
        BASE_PATH = getBasePath();
        fileList = getFileList();
        setAvailableValues(getFileNames(fileList));
        setAvailableIcons(getIcons(fileList));
    }


    public Object getValue() {
        int selectedIndex = ((JComboBox)editor).getSelectedIndex();
        if (selectedIndex == 0) return null;
        String path = fileList[selectedIndex - 1].getAbsolutePath();
        if (path.contains(INTERNAL_PATH)) {
            path = path.substring(BASE_PATH.length() -1);
        }
        return path;
    }

    public void setValue(Object value) {
        if (value == null) return;

        String selection = (String) value;
        if (selection.equals("None")) ((JComboBox) editor).setSelectedIndex(0);
        for (int i=0; i < fileList.length; i++) {
            if (fileList[i].getAbsolutePath().endsWith(selection)) {
                ((JComboBox) editor).setSelectedIndex(i+1);
                break;
            }
        }
    }


    private String getBasePath() {
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        return src.getLocation().getPath();
    }


    private File[] getFileList() {

        // internals
        String path = BASE_PATH + "org/yawlfoundation/yawl/editor/ui/resources/taskicons";
        File file = new File(path);
        File[] files = file.listFiles();

        // externals
        path = UserSettings.getTaskIconsFilePath();
        if (path != null) {
            file = new File(path);
            File[] externals = file.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    final List<String> okay = Arrays.asList( "png", "gif", "jpg", "jpeg");
                    return okay.contains(s.substring(s.lastIndexOf('.') + 1));
                }
            });
            if (externals != null && externals.length > 0) {
                File[] combined = new File[files.length + externals.length];
                System.arraycopy(files, 0, combined, 0, files.length);
                System.arraycopy(externals, 0, combined, files.length, externals.length);
                Arrays.sort(combined, new Comparator<File>() {
                    public int compare(File file1, File file2) {
                        return file1.getName().compareToIgnoreCase(file2.getName());
                    }
                });
                files = combined;
            }
        }
        return files;
    }


    private String[] getFileNames(File[] fileList) {
        String[] names = new String[fileList.length + 1];
        names[0] = "None";
        for (int i=0; i<fileList.length; i++) {
            String name = fileList[i].getName();
            names[i+1] = name.substring(0, name.lastIndexOf('.'));
        }
        return names;
    }


    private Icon[] getIcons(File[] fileList) {
        Icon[] icons = new Icon[fileList.length + 1];
        icons[0] = null;   // "None"
        for (int i=0; i<fileList.length; i++) {
            try {
                BufferedImage b = Scalr.resize(ImageIO.read(fileList[i]), 12);
                icons[i+1] = new ImageIcon(b);
            }
            catch (IOException ioe) {
                icons[i+1] = null;
            }
        }
        return icons;
    }
}
