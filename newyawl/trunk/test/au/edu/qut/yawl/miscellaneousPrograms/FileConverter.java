/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.miscellaneousPrograms;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 18/02/2004
 * Time: 14:47:54
 * 
 */
public class FileConverter {
    private static final String EOL = "\r\n";
    private static final String COMMENT_START = "/**";
    private static final String COMMENT_END = "*/";



    public static void main(String[] args) throws IOException {


        File currDir = new File(".");
        FileIterator fIter = new FileIterator(currDir);
        Iterator iter = fIter.getIterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            System.out.println("Coverting file: " + file.getAbsolutePath());
            convertFile(file);
        }
    }


    private static void convertFile(File file) throws IOException {

            StringBuffer fixedContents = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;

            String author = "";
            String date = "";
            String deprecated = "";
            String time = "";

            while((line = reader.readLine()) != null){
                fixedContents.append(line + EOL);

                if( line.indexOf("@author") != -1) {
                    author += line + EOL;
                }
                if( line.indexOf("@deprecated") != -1) {
                    deprecated += line + EOL;
                }
                if( line.indexOf("Date:") != -1) {
                    date += line + EOL;
                }
                if( line.indexOf("Time:") != -1) {
                    time += line + EOL;
                }

            }
            file.delete();




            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(fixedContents.toString());
            writer.flush();
            writer.close();

    }
}


class FileIterator{
    private FileFilter _xmlFileFilter;
    private List _files = new ArrayList();
    private FileFilter _directoryFilter;

    FileIterator(File directory){
        _xmlFileFilter = new FileFilter(){
            public boolean accept(File file){
                if(file.getAbsolutePath().endsWith(".java")){
                    return true;
                }
                return false;
            }
        };
        _directoryFilter = new FileFilter(){
            public boolean accept(File file){
                if(file.isDirectory()){
                    return true;
                }
                return false;
            }
        };
        buildFileList(directory);
    }

    private void buildFileList(File dir){
        File [] xmlFiles = dir.listFiles(_xmlFileFilter);
        for (int i = 0; i < xmlFiles.length; i++) {
            _files.add(xmlFiles[i]);
        }
        File[] dirs = dir.listFiles(_directoryFilter);
        for (int i = 0; i < dirs.length; i++) {
            File dirChild = dirs[i];
            buildFileList(dirChild);
        }
    }

    public Iterator getIterator(){
        return _files.iterator();
    }
}
