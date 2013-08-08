package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.editor.core.util.FileUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A map of name -> named repository record.
 * @author Michael Adams
 * @date 17/06/12
 */
public class RepoMap {

    private Map<String, RepoRecord> _map;
    private File _backingStore;

    // the absolute path to the base repository dir
    private static final String BACKINGSTORE_DIR =
            FileUtil.getHomeDir() + File.separatorChar + "repository";


    RepoMap() { }

    /**
     * Constructs a new RepoMap and loads it content from the file at 'path'
     * @param path the absolute path to the file to load into the map
     */
    protected RepoMap(String path) {
        _map = new Hashtable<String, RepoRecord>();
        load(getFilePath(path));
    }


    /**
     * Adds a record to the map
     * @param record the record to add
     * @return whether the add was successful
     */
    protected RepoRecord addRecord(RepoRecord record) {
        if (record != null) {
            record.setName(getUniqueName(record.getName())); // adjust name for uniqueness
            _map.put(record.getName(), record);
            save();
        }
        return record;
    }


    /**
     * Gets a named record from the map
     * @param name the name of the record
     * @return the named record, or null if not found
     */
    protected RepoRecord getRecord(String name) {
        return name == null ? null : _map.get(name);
    }


    /**
     * Removes a named record from the map
     * @param name the name of the record
     * @return the removed record, or null if not found
     */
    public RepoRecord removeRecord(String name) {
        if (name == null) return null;
        RepoRecord record = _map.remove(name);
        if (record != null) save();
        return record;
    }


    /**
     * Gets a sorted list of name-description pairs for all records in the map
     * @return a sorted list of RepoDescriptor objects
     */
    public List<RepoDescriptor> getDescriptors() {
        List<RepoDescriptor> descriptors = new ArrayList<RepoDescriptor>(_map.size());
        for (RepoRecord record : _map.values()) {
            descriptors.add(record.getDescriptor());
        }
        Collections.sort(descriptors);
        return descriptors;
    }


    protected boolean anyAreNull(Object... values) {
        for (Object o : values) {
            if (o == null) return true;
        }
        return false;
    }

    /************************************************************************/

    // gets the absolute file path for the named file
    private String getFilePath(String fileName) {
        return BACKINGSTORE_DIR + File.separatorChar + fileName + ".xml";
    }

    // makes a name unique by appending and underscore and digit, if it already exists
    private String getUniqueName(String name) {
        if (! _map.containsKey(name)) return name;

        String uniqueName = name;
        int i = 0;
        while (_map.containsKey(uniqueName)) {
            uniqueName = name + "_" + i++;
        }
        return uniqueName;
    }


    // loads all records for this map from file
    private void load(String path) {
        setBackingStore(path);
        String recordStr = StringUtil.fileToString(_backingStore);
        if (! StringUtil.isNullOrEmpty(recordStr)) {
            XNode records = new XNodeParser().parse(recordStr);
            if (records != null) {
                for (XNode recordNode : records.getChildren("record")) {
                    RepoRecord record = new RepoRecord(recordNode);
                    _map.put(record.getName(), record);
                }
            }
        }
    }


    // saves all the records in this map to file
    private void save() {
        XNode repository = new XNode("repository");
        for (RepoRecord record : _map.values()) {
            repository.addChild(record.toXNode());
        }
        StringUtil.stringToFile(_backingStore, repository.toPrettyString(true));
    }


    // ensure the supplied directory and file exist, and create if they don't
    private boolean setBackingStore(String path) {
        _backingStore = new File(path);
        if (! _backingStore.exists()) {

            // check (and create if necessary) the directory, then create the file
            File backingDir = new File(path.substring(0,
                    path.lastIndexOf(File.separatorChar)));
            try {
                boolean dirExists = backingDir.exists();
                if (! dirExists) {
                    dirExists = backingDir.mkdir();
                }
                return dirExists && _backingStore.createNewFile();
            }
            catch (IOException ioe) {
                // to be done
                return false;
            }
        }
        return true;
    }


}
