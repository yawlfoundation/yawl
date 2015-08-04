/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


/**
 * 
 * @author Lachlan Aldred
 * Date: 24/04/2003
 * Time: 10:03:02
 * 
 */
package au.edu.qut.yawl.util;

import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.state.YIdentifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class YConditionRecorder {
    private static YConditionRecorder _ourInstance;
    private File _file;
    private FileWriter _fileWriter;

    public synchronized static YConditionRecorder getInstance() {
        if (_ourInstance == null) {
            _ourInstance = new YConditionRecorder();
        }
        return _ourInstance;
    }

    private YConditionRecorder() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'_'HH.mm.ss");
        File recordsDir = new File(System.getProperty("user.dir") + File.separator + "records");
        if (!recordsDir.exists()) {
            recordsDir.mkdir();
        }
        _file = new File(recordsDir, formatter.format(new Date()) + "_records.dat");
        try {
            boolean success = _file.createNewFile();
            if (success) {
                _fileWriter = new FileWriter(_file);
            } else {
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void recordAdd(YConditionInterface condition, YIdentifier identifier) {
        try {
            _fileWriter.write("Added " + identifier.toString() + " to " + condition + "\n");
            _fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void recordRemove(YConditionInterface condition, YIdentifier identifier) {
        try {
            _fileWriter.write("Removed " + identifier.toString() + " from " + condition + "\n");
            _fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void finalize() {
        try {
            _fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

