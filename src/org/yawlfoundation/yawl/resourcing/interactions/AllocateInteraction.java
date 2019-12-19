/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.interactions;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.util.PluginFactory;

import java.util.Set;

/**
 * This class describes the requirements of a task at the allocate phase of
 * allocating resources.
 *
 * @author Michael Adams
 *         v0.1, 02/08/2007
 */

public class AllocateInteraction extends AbstractInteraction {

    private AbstractAllocator _allocator;

    public AllocateInteraction(int initiator) {
        super(initiator);
    }

    public AllocateInteraction() { super(); }

    public AllocateInteraction(String ownerTaskID) { super(ownerTaskID); }


    public void setAllocator(AbstractAllocator allocator) {
        _allocator = allocator;
    }

    public AbstractAllocator getAllocator() { return _allocator; }

    public void clearAllocator() { _allocator = null; }


    public Participant performAllocation(Set<Participant> offerSet, WorkItemRecord wir) {
        return _allocator.performAllocation(offerSet, wir);
    }

    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        parseInitiator(e, nsYawl);

        Element eAllocator = e.getChild("allocator", nsYawl);
        if (eAllocator != null) {
            String allocatorClassName = eAllocator.getChildText("name", nsYawl);
            if (allocatorClassName != null) {
                _allocator = PluginFactory.newAllocatorInstance(allocatorClassName);
                if (_allocator != null)
                    _allocator.setParams(parseParams(eAllocator, nsYawl));
                else
                    throw new ResourceParseException("Unknown allocator name: " +
                            allocatorClassName);
            } else throw new ResourceParseException("Missing allocator element: name");
        }
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<allocate ");
        xml.append("initiator=\"").append(getInitiatorString()).append("\">");

        if (isSystemInitiated())
            if (_allocator != null) xml.append(_allocator.toXML());
        xml.append("</allocate>");
        return xml.toString();
    }
}
