package org.yawlfoundation.yawl.editor.core.identity;

import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.elements.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Keeps tracks of all net element identifiers for a specification to ensure they
 * are unique.
 *
 * @author Michael Adams
 * @date 1/03/12
 */
public class ElementIdentifiers {
    
    public static String DEFAULT_ELEMENT_NAME = "unnamed";

    // a map of element names to used and available suffix integers for each name
    private Map<String, SuffixStore> uniqueIdentifiers;


    public ElementIdentifiers() {
        uniqueIdentifiers = new Hashtable<String, SuffixStore>();
    }

    public void clear() { uniqueIdentifiers.clear(); }


    /**
     * Checks a name for uniqueness, and appends a suffix if necessary.
     * @param label the label name to check
     * @return an EngineIdentifier containing the name and a unique suffix
     */
    public EngineIdentifier getIdentifier(String label) {
        if (label == null || label.equals("null")) label = DEFAULT_ELEMENT_NAME;
        return new EngineIdentifier(label, getSuffixStore(label).getNextSuffix());
    }


    /**
     * Checks an EngineIdentifier to ensure it is unique within its specification
     * @param id the EngineIdentifier to check
     * @return the same EngineIdentifier if it is unique, or else one with the same
     * name but a new unique suffix
     */
    public EngineIdentifier ensureUniqueness(EngineIdentifier id) {
        if (id != null) {
            SuffixStore suffixes = getSuffixStore(id.getName());
            if (suffixes.isUsed(id.getSuffix())) {                // if suffix is taken
                id.setSuffix(suffixes.getNextSuffix());           // assign a free one
            }
            else suffixes.use(id.getSuffix());                    // or mark it as taken

        }
        return id;
    }


    /**
     * Removes an EngineIdentifier from the set
     * @param engineID the EngineIdentifier to remove
     */
    public void removeIdentifier(EngineIdentifier engineID) {
        if (engineID != null) {
            SuffixStore suffixes = uniqueIdentifiers.get(engineID.getName());
            if (suffixes != null) {
                suffixes.freeSuffix(engineID.getSuffix());
            }
        }
    }


    /**
     * Rationalises the set of EngineIdentifiers for the current specification; only
     * applies suffixes where necessary to ensure uniqueness, and renumbers suffixes
     * to the lowest contiguous set.
     * @param nets the set of nets in the current specification
     */
    public void rationalise(Set<NetGraphModel> nets) {
        uniqueIdentifiers.clear();
        for (NetGraphModel net : nets) {
            for (Object o : net.getRoots()) {
               if (o instanceof VertexContainer) {
                   rationalise(((VertexContainer) o).getVertex());
               }
               else if (o instanceof YAWLVertex) {                       // empty tasks
                   rationalise((YAWLVertex) o);
               }
            }
        }
    }


    public void rationalise(YSpecification spec) {
        uniqueIdentifiers.clear();
        for (YDecomposition decomposition : spec.getDecompositions()) {
            if (decomposition instanceof YNet) {
                for (YNetElement element : ((YNet) decomposition).getNetElements().values()) {
                    rationalise(element);
                }
            }
        }
    }


    private void rationalise(YNetElement element) {
        String original = element.getID();
        EngineIdentifier id = EngineIdentifier.parse(original);
        if (! id.toString().equals(original)) {
            element.setID(id.toString());
        }
    }
    

    /**
     * Rationalises the EngineIdentifier of a vertex; only applies a suffix if the name
     * itself is not unique, and then to the lowest one not already taken.
     * @param vertex the vertex to rationalise
     */
    private void rationalise(YAWLVertex vertex) {
        EngineIdentifier id = rationalise(vertex.getEngineIdentifier());
        vertex.setEngineID(id, false);
    }


    private EngineIdentifier rationalise(EngineIdentifier identifier) {
        return getIdentifier(identifier.getName());
    }


    /**
     * Gets the stored suffixes for a name.
     * @param label the label name to get the suffix store for
     * @return
     */
    private SuffixStore getSuffixStore(String label) {
        SuffixStore suffixes = uniqueIdentifiers.get(label);
        if (suffixes == null) {                                // no matching label
            suffixes = new SuffixStore();
            uniqueIdentifiers.put(label, suffixes);            // init store
        }
        return suffixes;
    }

    /***************************************************************************/

    /**
     * Stores the used and available suffixes for a particular name. Uses boolean[]
     * rather than BitSet since size will be typically small (i.e. in all but the
     * most complex specifications).
     */
    private class SuffixStore {
        int size = 4;                                 // initial size
        boolean[] used = new boolean[size];

        int getNextSuffix() {
            int i = 0;
            while (used[i] && ++i < size);            // find a free suffix
            if (i == size) growArray(size * 2);       // all spots taken, double size
            used[i] = true;                           // mark as used
            return i;
        }

        void use(int suffix) {
            if (suffix > size - 1) growArray(suffix + 1);
            used[suffix] = true;                       // mark as used
        }

        void freeSuffix(int suffix) {
            if (suffix > -1 && suffix < size) used[suffix] = false;   // mark as free
        }

        boolean isUsed(int suffix) {
            return suffix < size && used[suffix];
        }

        void growArray(int newSize) {
            boolean[] temp = new boolean[newSize];
            System.arraycopy(used, 0, temp, 0, size);
            used = temp;
            size = newSize;
        }
    }

}
