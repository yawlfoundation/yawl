package org.yawlfoundation.yawl.miscellaneousPrograms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 23/03/2004
 * Time: 10:37:04
 * 
 */
public class LibraryConverter {
    public static void main(String[] args) {
        URL libraryFileURL = LibraryConverter.class.getResource("library.txt");
System.out.println("libraryFileURL.getFile() = " + libraryFileURL.getFile());
        File f = new File(libraryFileURL.getFile());
        File out = new File(f.getParentFile().getAbsolutePath() + File.separator + "lib.txt");
        try {
            FileWriter writer = new FileWriter(out);
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = null;
            while((line = reader.readLine()) != null){
                line = line.replaceAll("\t", "|");
                line = "|" + line + "|." + "\r\n";
                writer.write(line);
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
