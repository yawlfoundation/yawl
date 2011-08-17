package org.yawlfoundation.yawl.editor.specification;

import java.io.*;
import java.util.zip.*;

/**
 * Author: Lindsay Bradford & Michael Adams
 * Creation Date: 15/10/2008
 */
public class SpecificationConverter {

    public SpecificationConverter() {}


    public String convert(String fileName) {
        String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_v2.ywl";
        return saveStringToFile(
                convertPackageNames(
                        getLoadFileAsString(fileName)), newFileName);
    }


    private String getLoadFileAsString(String s) {
        InputStreamReader reader;
        char ac[] = new char[8192];
        StringBuffer buffer = new StringBuffer();
        try {
            ZipInputStream zipinputstream = new ZipInputStream(
                                  new BufferedInputStream(new FileInputStream(s)));
            zipinputstream.getNextEntry();
            reader = new InputStreamReader(zipinputstream);
            for(; reader.read(ac) != -1; buffer.append(ac));
            reader.close();
        }
        catch (Exception e) {
            return null;
        }
        return buffer.toString();
    }


    private String convertPackageNames(String s) {
        if (s != null) {
            String s1 = s.replaceAll("au\\.edu\\.qut\\.yawl", "org.yawlfoundation.yawl");
            s = s1.replaceAll("au/edu/qut/yawl", "org/yawlfoundation/yawl");
        }
        return s;
    }


    private String saveStringToFile(String fileContents, String fileName) {
        if (fileContents == null) return "";
        try {
            ZipOutputStream zipoutputstream = new ZipOutputStream(
                     new BufferedOutputStream(new FileOutputStream(fileName)));
            zipoutputstream.putNextEntry(new ZipEntry("specification.xml"));
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(zipoutputstream);
            outputstreamwriter.write(fileContents);
            outputstreamwriter.close();
            zipoutputstream.close();
            return fileName;
        }
        catch (Exception exception) {
            return null;
        }
    }

}

