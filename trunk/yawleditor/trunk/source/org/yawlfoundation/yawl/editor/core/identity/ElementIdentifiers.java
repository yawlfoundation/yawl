/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.core.identity;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.elements.YSpecification;

import java.util.Hashtable;
import java.util.Map;

/**
 * Keeps track of all net element identifiers for a specification to ensure they
 * are unique.
 *
 * @author Michael Adams
 * @date 1/03/12
 */
public class ElementIdentifiers {
    
    public static String DEFAULT_ELEMENT_NAME = "element";

    // a map of element names to used and available or used suffix integers for each name
    private Map<String, SuffixStore> _identifiers;


    public ElementIdentifiers() {
        _identifiers = new Hashtable<String, SuffixStore>();
    }

    public void clear() { _identifiers.clear(); }

    /**
     * Loads the element identifiers for a specification, without any rationalisation
     * of suffixes.
     * @param spec the spec to load
     */
    public void load(YSpecification spec) {
        if (spec == null) return;
        clear();
        for (YDecomposition decomposition : spec.getDecompositions()) {
            if (decomposition instanceof YNet) {
                for (YNetElement element : ((YNet) decomposition).getNetElements().values()) {
                    ElementIdentifier id = new ElementIdentifier(element.getID(), true);
                    getSuffixStore(id.getName()).use(id.getSuffix());
                }
            }
        }
    }


    /**
     * Checks a name for uniqueness, and appends a suffix if necessary.
     * @param label the label name to check
     * @return an ElementIdentifier containing the name and a unique suffix
     */
    public ElementIdentifier getIdentifier(String label) {
        if (label == null || label.equals("null")) label = DEFAULT_ELEMENT_NAME;
        return new ElementIdentifier(label, getSuffixStore(label).getNext());
    }


    /**
     * Checks an ElementIdentifier to ensure it is unique within its specification
     * @param id the ElementIdentifier to check
     * @return the same ElementIdentifier if it is unique, or else one with the same
     * name but a new unique suffix
     */
    public ElementIdentifier ensureUniqueness(ElementIdentifier id) {
        if (id != null) {
            SuffixStore suffixes = getSuffixStore(id.getName());
            if (suffixes.isUsed(id.getSuffix())) {                // if suffix is taken
                id.setSuffix(suffixes.getNext());           // assign a free one
            }
            else suffixes.use(id.getSuffix());                    // or mark it as taken

        }
        return id;
    }


    /**
     * Removes an ElementIdentifier from the set
     * @param id the ElementIdentifier to remove
     */
    public void removeIdentifier(ElementIdentifier id) {
        if (id != null) {
            SuffixStore suffixes = _identifiers.get(id.getName());
            if (suffixes != null) {
                suffixes.free(id.getSuffix());
            }
        }
    }


    public void removeIdentifier(String idString) {
        removeIdentifier(new ElementIdentifier(idString, true));
    }



    /**
     * Rationalises the identifiers in a specification if any of the suffix stores
     * contains gaps in used suffixes
     */
    public void rationaliseIfRequired(YSpecification specification) {
        if (specification != null) {
            for (SuffixStore store : _identifiers.values()) {
                if (store.isDirty()) {
                    rationalise(specification);
                    break;
                }
            }
        }
    }


    private void rationalise(YSpecification spec) {
        clear();
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
        ElementIdentifier id = rationalise(new ElementIdentifier(original, true));
        if (! id.toString().equals(original)) {
            element.setID(id.toString());
        }
    }


    private ElementIdentifier rationalise(ElementIdentifier identifier) {
        return getIdentifier(identifier.getName());
    }


    /**
     * Gets the stored suffixes for a name.
     * @param label the label name to get the suffix store for
     * @return the suffix store corresponding to the label
     */
    private SuffixStore getSuffixStore(String label) {
        SuffixStore suffixes = _identifiers.get(label);
        if (suffixes == null) {                                // no matching label
            suffixes = new SuffixStore();
            _identifiers.put(label, suffixes);            // init store
        }
        return suffixes;
    }

}
