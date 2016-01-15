package org.yawlfoundation.yawl.worklet.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * @author Michael Adams
 * @date 15/09/15
 */
public class WorkletLoader {

    private final Logger _log = LogManager.getLogger(WorkletLoader.class);


    public boolean add(YSpecificationID specID, String xml) {
        return add(new WorkletSpecification(specID, xml));
    }


    public boolean add(WorkletSpecification wSpec) {
        String key = wSpec.getKey();
        if (key != null) {

            // replace current spec with that key, if any
            if (getPersistedWorkletSpecification(wSpec.getSpecID()) != null) {
                return Persister.update(wSpec);
            }
            else {
                return Persister.insert(wSpec);      // no current spec with that key
            }
        }
        return false;
    }


    public boolean remove(WorkletSpecification wSpec) {
        return wSpec != null && wSpec.getSpecID() != null && Persister.delete(wSpec);
    }


    public boolean remove(YSpecificationID specID) {
        return specID != null && remove(new WorkletSpecification(specID, null));
    }


    public WorkletSpecification get(YSpecificationID specID) {
        return specID != null ? get(specID.getKey()) : null;
    }


    public WorkletSpecification get(String key) {

        // get from db
        WorkletSpecification wSpec = getPersistedWorkletSpecification(key);
        if (wSpec != null) {
            return wSpec;
        }

        // not there? try from disk
        String xml = StringUtil.fileToString(getWorkletFile(key));
        if (xml != null) {
            wSpec = new WorkletSpecification(xml);
            Persister.insert(wSpec);
        }
        return wSpec;
    }


    public Set<WorkletSpecification> parseTarget(String target) {
        if (target == null || target.isEmpty()) {
            return Collections.emptySet();
        }

        Set<WorkletSpecification> parsed = new HashSet<WorkletSpecification>();
        for (String key : extractKeysFromTarget(target)) {
            try {
                WorkletSpecification wSpec = get(key);
                if (wSpec != null) {
                    parsed.add(wSpec);
                }
                else {
                    _log.info("Rule search found: {}, but there is no worklet of that " +
                            "name in the repository, or there was a problem " +
                            "opening/reading the worklet specification", key);
                }

            }
            catch (IllegalArgumentException iae) {
                _log.error("Error parsing rule: " + iae.getMessage());
            }
        }
        return parsed;
    }


    public Set<String> getAllWorkletKeys() {
        Set<String> uris = new HashSet<String>();
        for (WorkletSpecification worklet : loadAllWorkletSpecifications()) {
            uris.add(worklet.getSpecID().getKey());
        }
        return uris;
    }



    public Set<WorkletSpecification> loadAllWorkletSpecifications() {
        Set<WorkletSpecification> worklets = loadAllPersistedWorkletSpecifications();
        worklets.addAll(loadAllFileWorkletSpecifications());
        return worklets;
    }


    private Set<WorkletSpecification> loadAllPersistedWorkletSpecifications() {
        Set<WorkletSpecification> worklets = new HashSet<WorkletSpecification>();
        Query query = Persister.getInstance().createQuery("from WorkletSpecification");
        Iterator it = query.iterate();
        while (it.hasNext()) {
            worklets.add((WorkletSpecification) it.next());
        }
        return worklets;
    }


    private Set<WorkletSpecification> loadAllFileWorkletSpecifications() {
        Set<WorkletSpecification> worklets = new HashSet<WorkletSpecification>();
        for (File file : populateFileList(new File(WorkletConstants.wsWorkletsDir))) {
            if (file.isFile()) {
                String xml = StringUtil.fileToString(file);
                if (xml != null) {
                    worklets.add(new WorkletSpecification(xml));
                }
            }
        }
        return worklets;
    }


    private File getWorkletFile(String workletName) {
        String path = WorkletConstants.wsWorkletsDir + workletName;
        String fileName = path + ".yawl";        // try .yawl first
        File file = getFile(fileName);
        if (file == null) {
            fileName = path + ".xml";            // no good? try .xml next
            file = getFile(fileName);
        }
        return file;
    }


    private File getFile(String fileName) {
        File file = new File(fileName);
        return file.exists() ? file : null;
    }


    private WorkletSpecification getPersistedWorkletSpecification(YSpecificationID specID) {
        return specID != null ? getPersistedWorkletSpecification(specID.getKey()) : null;
    }


    private WorkletSpecification getPersistedWorkletSpecification(String key) {
        return key != null ? (WorkletSpecification) Persister.getInstance()
                        .get(WorkletSpecification.class, key) : null;
    }


    private List<File> populateFileList(File dir) {
        List<File> fileList = new ArrayList<File>();
        if (dir.exists() && dir.isDirectory()) {
            File[] fileNames = dir.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    String lcs = s.toLowerCase();
                    return lcs.endsWith(".yawl") || lcs.endsWith(".xml");
                }
            });
            if (fileNames != null) {
                fileList = Arrays.asList(fileNames);
            }
        }
        return fileList;
    }


    private List<String> extractKeysFromTarget(String target) {
        String[] keys = target.split(";");
        for (int i=0; i < keys.length; i++) {
             keys[i] = keys[i].trim();
        }
        return Arrays.asList(keys);
    }

}
