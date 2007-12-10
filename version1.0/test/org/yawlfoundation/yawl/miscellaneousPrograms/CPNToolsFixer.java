/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.miscellaneousPrograms;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 6/09/2004
 * Time: 18:47:24
 * 
 */
public class CPNToolsFixer {
    private static JFileChooser _fileChooser;
    private static File _selectedFile;

    public CPNToolsFixer() {
        _fileChooser = setUpChooser();
    }


    private JFileChooser setUpChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if (extension != null) {
                    if ("cpn".equals(extension) ||
                            "xml".equals(extension) ||
                            "yawl".equals(extension)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "CPN Files";
            }

            private String getExtension(File f) {
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }
                return ext;
            }
        });
//        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        chooser.setCurrentDirectory(new File("D:/Phd/Paper/cpnTools"));
        return chooser;
    }


    private static String modify(File selectedFile) {
        Set ids = new HashSet();
        StringBuffer result = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(selectedFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                String line2 = "";
                String id;
                boolean newIDCreated = false;
                int begin = line.indexOf(" id=\"");
                if (begin != -1) {
                    int end = line.indexOf("\">", begin);
                    begin += 5;

                    id = line.substring(begin, end);
                    newIDCreated = false;
                    while (ids.contains(id)) {
                        newIDCreated = true;
                        id = id + (int) (Math.random() % 10);
                    }
                    ids.add(id);
                    line2 = line.substring(0, begin) + id + line.substring(end, line.length());

                    if (newIDCreated) {
                        Logger.getLogger(CPNToolsFixer.class).debug(
                        "line = " + line +
                        "\n\tline.substring(begin, end) = " + id +
                        "\nline2 = " + line2 + "\n\n");
                    }
                } else {
                    line2 = line;
                }
                result.append(line2 + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }


    public static void main(String[] args) {
        CPNToolsFixer p = new CPNToolsFixer();
        int returnVal = _fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _selectedFile = _fileChooser.getSelectedFile();
            String result = modify(_selectedFile);
            returnVal = _fileChooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File outPut = _fileChooser.getSelectedFile();
                try {
                    FileWriter out = new FileWriter(outPut);
                    out.write(result);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.exit(0);
    }

}
