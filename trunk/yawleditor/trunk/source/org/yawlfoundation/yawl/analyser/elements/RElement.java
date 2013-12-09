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

package org.yawlfoundation.yawl.analyser.elements;

import org.yawlfoundation.yawl.elements.YExternalNetElement;

import java.util.*;

/**
 *
 * The base class for RTransition and RPlace 
 *
 **/

public class RElement {

    private String _name;
    private final String _id;
    private Map<String, RFlow> _presetFlows = new HashMap<String, RFlow>();
    private Map<String, RFlow> _postsetFlows = new HashMap<String, RFlow>();
    private final Set<RTransition> _cancelledBySet = new HashSet<RTransition>(); // for reduction rules code
    private final Set<YExternalNetElement> _yElementsSet = new HashSet<YExternalNetElement>(); // for reduction rules mapping
    private final Set<RElement> _rElementsSet = new HashSet<RElement>(); // for reduced net mappings

    public RElement(String id) {
        _id = id;
    }

    public String getID() { return _id;	}

    public void setName(String name) { _name = name; }

    public String getName() { return _name; }

    public Map<String, RFlow> getPresetFlows() { return _presetFlows; }

    public Map<String, RFlow> getPostsetFlows() { return _postsetFlows; }

    public void setPresetFlows(Map<String, RFlow> presetFlows) {
        _presetFlows = presetFlows;
    }

    public void setPostsetFlows(Map<String, RFlow> postsetFlows) {
        _postsetFlows = postsetFlows;
    }

    public Set<RElement> getPostsetElements() {
        Set<RElement> postsetElements = new HashSet<RElement>();
        for (RFlow flow : _postsetFlows.values()) {
            postsetElements.add(flow.getNextElement());
        }
        return postsetElements;
    }

    public Set<RElement> getPresetElements() {
        Set<RElement> presetElements = new HashSet<RElement>();
        for (RFlow flow : _presetFlows.values()) {
            presetElements.add(flow.getPriorElement());
        }
        return presetElements;
    }

    public void setPreset(RFlow flowsInto) {
        if (! ((flowsInto == null) || (flowsInto.getPriorElement() == null))) {
            _presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
            flowsInto.getPriorElement().getPostsetFlows().put(
                    flowsInto.getNextElement().getID(), flowsInto);
        }
    }

    public void setPostset(RFlow flowsInto) {
        if (! ((flowsInto == null) || (flowsInto.getNextElement() == null))) {
            _postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
            flowsInto.getNextElement().getPresetFlows().put(
                    flowsInto.getPriorElement().getID(), flowsInto);
        }
    }


    public RElement getPostsetElement(String id) {
        RFlow flow = _postsetFlows.get(id);
        return (flow != null) ? flow.getNextElement() : null;
    }


    public RElement getPresetElement(String id) {
        RFlow flow = _presetFlows.get(id);
        return (flow != null) ? flow.getPriorElement() : null;
    }

    public void removePresetFlow(RFlow flowsInto) {
        if (flowsInto != null) {
            _presetFlows.remove(flowsInto.getPriorElement().getID());
            flowsInto.getPriorElement().getPostsetFlows().remove(
                    flowsInto.getNextElement().getID());
        }
    }

    public void removePostsetFlow(RFlow flowsInto) {
        if (flowsInto != null) {
            _postsetFlows.remove(flowsInto.getNextElement().getID());
            flowsInto.getNextElement().getPresetFlows().remove(
                    flowsInto.getPriorElement().getID());
        }
    }

    public Set<RElement> getResetMappings() { return _rElementsSet; }

    public void addToResetMappings(RElement e) { _rElementsSet.add(e); }

    public void addToResetMappings(Set<RElement> elements) {
        _rElementsSet.addAll(elements);
    }

    public Set<YExternalNetElement> getYawlMappings() { return _yElementsSet; }

    public void addToYawlMappings(YExternalNetElement e) {
        _yElementsSet.add(e);
    }

    public void addToYawlMappings(Set<YExternalNetElement> elements){
        _yElementsSet.addAll(elements);
    }

    public Set<RTransition> getCancelledBySet(){
        return _cancelledBySet;
    }

    public void addToCancelledBySet(RTransition t){
        if (t != null) _cancelledBySet.add(t);
    }

    public void removeFromCancelledBySet(RTransition t){
        if (t != null) _cancelledBySet.remove(t);
    }

}

