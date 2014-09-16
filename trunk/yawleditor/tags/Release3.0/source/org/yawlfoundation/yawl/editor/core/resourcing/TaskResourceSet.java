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

package org.yawlfoundation.yawl.editor.core.resourcing;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 14/06/12
 */
public class TaskResourceSet {

    private final YAtomicTask _task;

    private final BasicOfferInteraction _offer;
    private final ClientAllocateInteraction _allocate;
    private final StartInteraction _start;
    private final BasicSecondaryResources _secondary;

    // user-task privileges
    private final TaskPrivileges _privileges ;


    public TaskResourceSet(YAtomicTask task) {
        _task = task;
        _offer = new BasicOfferInteraction(_task) ;
        _allocate = new ClientAllocateInteraction(_task.getID());
        _start = new StartInteraction(_task.getID()) ;
        _secondary = new BasicSecondaryResources(_task);
        _privileges = new TaskPrivileges();
        parse();
        setTaskXML();
    }

    public YAtomicTask getTask() { return _task; }

    public BasicOfferInteraction getOffer() { return _offer; }

    public AllocateInteraction getAllocate() { return _allocate; }

    public StartInteraction getStart() { return _start; }

    public BasicSecondaryResources getSecondary() { return _secondary; }

    public TaskPrivileges getTaskPrivileges() { return _privileges; }


    public void setTaskXML() { _task.setResourcingXML(toXML()); }


    public Set<InvalidReference> getInvalidReferences() {
        Set<InvalidReference> invalids = _offer.getInvalidReferences();
        invalids.addAll(_secondary.getInvalidReferences());
        return invalids;
    }

    public boolean hasResources() {
        return _offer.hasResources() || _secondary.hasResources();
    }

    public boolean hasAnyResourceReferences() {
        return hasResources() || ! getInvalidReferences().isEmpty();
    }

    public String getInitiatorChars() {
        StringBuilder s = new StringBuilder(3);
        s.append(getInitiatorChar(getOffer().getInitiator()));
        s.append(getInitiatorChar(getAllocate().getInitiator()));
        s.append(getInitiatorChar(getStart().getInitiator()));
        return s.toString();
    }

     private static char getInitiatorChar(int initiator) {
         return initiator == AbstractInteraction.USER_INITIATED ? 'U' : 'S';
     }

    private void parse() {
        if (_task != null) {
            Element resourceXML = _task.getResourcingSpecs();
            if (resourceXML != null) {
                parse(resourceXML);
            }
        }
    }

    /**
     * Parse the Element passed for task resourcing info and build the appropriate
     * objects.
     * @param eleSpec the [resourcing] section from a particular task definition
     * within a specification file.
     */
    public void parse(Element eleSpec) {
        if (eleSpec != null) {
            Namespace nsYawl = eleSpec.getNamespace() ;
            try {
                _offer.parse(eleSpec.getChild("offer", nsYawl), nsYawl) ;
                _allocate.parse(eleSpec.getChild("allocate", nsYawl), nsYawl) ;
                _start.parse(eleSpec.getChild("start", nsYawl), nsYawl) ;
                _secondary.parse(eleSpec.getChild("secondary", nsYawl), nsYawl);
                _privileges.parse(eleSpec.getChild("privileges", nsYawl)) ;
            }
            catch (ResourceParseException rpe) {
                // "Error parsing resourcing specification for task: " + _taskID
            }
        }
    }


    public void parse(String xml) {
        parse(JDOMUtil.stringToElement(xml));
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder("<resourcing>");
        xml.append(_offer.toXML()) ;
        xml.append(_allocate.toXML()) ;
        xml.append(_start.toXML()) ;
        xml.append(_secondary.toXML());
        xml.append(_privileges.toXML()) ;
        xml.append("</resourcing>");
        return xml.toString() ;
    }

}
