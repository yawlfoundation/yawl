package org.yawlfoundation.yawl.balancer.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/8/17
 */
abstract class AbstractLoadOutputter {

    protected FileWriter _out;
    private boolean _writeTime;


    public AbstractLoadOutputter(String baseFileName, String engineName, String extn) {
        try {
            _out = openFile(baseFileName, engineName, extn);
            _writeTime = true;
            writeHeader();
        }
        catch (IOException ioe) {
            if (_out != null) _out = null;
        }
    }


    abstract void writeValues(Map<String, String> values) throws IOException;

    abstract String getHeader();

    abstract void finalise() throws IOException;


    public void setWriteTime(boolean write) { _writeTime = write; }


    public void closeFile() {
        if (_out != null) {
            try {
                finalise();
                _out.flush();
                _out.close();
            }
            catch (IOException ioe) {
                // we tried
            }
        }
    }


    public void add(String line) {
        if (_out != null) {
            try {
                for (String sub : line.split("\\r?\\n")) {
                    writeTime();
                    _out.write(sub);
                    _out.write('\n');
                }
                _out.flush();
            }
            catch (IOException ioe) {
                // forget it
            }
        }
    }


    public void add(Map<String, String> values) {
        if (_out != null) {
            try {
                writeTime();
                writeValues(values);
                _out.write('\n');
                _out.flush();
            }
            catch (IOException ioe) {
                // forget it
            }
        }
    }


    private void writeHeader() throws IOException {
        if (_out != null) {
            _out.write(getHeader());
            _out.write('\n');
        }
    }


    private void writeTime() throws IOException {
        if (_out != null && _writeTime) {
            Date date = new Date();
            String fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date());
            _out.write(fDate);
            _out.write("," + date.getTime() + ",");
        }
    }


    private FileWriter openFile(String baseFileName, String engineName, String extn)
            throws IOException {
        File f = getFile(baseFileName, engineName, extn);
        if (f.createNewFile()) {
            return new FileWriter(f);
        }
        return null;
    }


    private File getFile(String baseFileName, String engineName, String extn) {
        String root = System.getenv("CATALINA_HOME");
        String baseName = root + "/logs/" + baseFileName + '_' + engineName;
        File f = new File(baseName + extn);
        int i = 1;
        while (f.exists()) {
           f  = new File(baseName + "_" + i++ + extn);
        }
        return f;
    }

}
