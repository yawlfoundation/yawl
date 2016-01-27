package org.yawlfoundation.yawl.worklet.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;

import java.util.*;

/**
 * @author Michael Adams
 * @date 15/09/15
 */
public class WorkletLoader {

    private final Logger _log = LogManager.getLogger(WorkletLoader.class);


    public WorkletLoader() { }


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


    public boolean remove(String specKey) {
        return remove(getPersistedWorkletSpecification(specKey));
    }


    public WorkletSpecification get(YSpecificationID specID) {
        return specID != null ? get(specID.getKey()) : null;
    }


    // get from db
    public WorkletSpecification get(String key) {
        return getPersistedWorkletSpecification(key);
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


    public Set<WorkletSpecification> getOrphanedWorklets() {
        Map<String, WorkletSpecification> persistedMap = getPersistedWorkletsMap();
        for (String key : getTargettedWorkletKeys()) {
             persistedMap.remove(key);
        }
        return new HashSet<WorkletSpecification>(persistedMap.values());
    }


    private Map<String, WorkletSpecification> getPersistedWorkletsMap() {
        Map<String, WorkletSpecification> map = new HashMap<String, WorkletSpecification>();
        for (WorkletSpecification worklet : loadAllPersistedWorkletSpecifications()) {
            map.put(worklet.getKey(), worklet);
        }
        return map;
    }


    private Set<String> getTargettedWorkletKeys() {
        Set<String> keys = new HashSet<String>();
        Query query = Persister.getInstance().createQuery("from RdrConclusion");
        Iterator it = query.iterate();
        while (it.hasNext()) {
            RdrConclusion conclusion = (RdrConclusion) it.next();
             for (RdrPrimitive primitive : conclusion.getPrimitives()) {
                if (primitive.getExletAction().isWorkletAction()) {
                    String target = primitive.getTarget();
                    if (target.contains(";")) {
                        keys.addAll(extractKeysFromTarget(target));
                    }
                    else {
                        keys.add(target);
                    }
                }
            }
        }
        Persister.getInstance().commit();
        return keys;
    }


    public Set<WorkletSpecification> loadAllWorkletSpecifications() {
        return loadAllPersistedWorkletSpecifications();
    }


    private Set<WorkletSpecification> loadAllPersistedWorkletSpecifications() {
        Set<WorkletSpecification> worklets = new HashSet<WorkletSpecification>();
        Query query = Persister.getInstance().createQuery("from WorkletSpecification");
        Iterator it = query.iterate();
        while (it.hasNext()) {
            WorkletSpecification spec = (WorkletSpecification) it.next();
            Hibernate.initialize(spec);
            worklets.add(spec);
        }
        Persister.getInstance().commit();
        return worklets;
    }


    private WorkletSpecification getPersistedWorkletSpecification(YSpecificationID specID) {
        return specID != null ? getPersistedWorkletSpecification(specID.getKey()) : null;
    }


    private WorkletSpecification getPersistedWorkletSpecification(String key) {
        return key != null ? (WorkletSpecification) Persister.getInstance()
                        .get(WorkletSpecification.class, key) : null;
    }


    private List<String> extractKeysFromTarget(String target) {
        String[] keys = target.split(";");
        for (int i=0; i < keys.length; i++) {
             keys[i] = keys[i].trim();
        }
        return Arrays.asList(keys);
    }

}
