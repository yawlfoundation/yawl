/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;

import java.io.IOException;
import java.util.List;

/**
 * @author Michael Adams
 * @date 22/04/2014
 */
public class ClientAllocateInteraction extends AllocateInteraction {

    public ClientAllocateInteraction() {
        super();
    }

    public ClientAllocateInteraction(String taskID) {
        super(taskID);
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        parseInitiator(e, nsYawl);

        Element eAllocator = e.getChild("allocator", nsYawl);
        if (eAllocator != null) {
            String allocatorClassName = eAllocator.getChildText("name", nsYawl);
            if (allocatorClassName != null) {
                setAllocator(getInstance(allocatorClassName));
            }
            else throw new ResourceParseException("Missing allocator element: name");
        }
    }


    private AbstractAllocator getInstance(String allocatorClassName) {
        if (! allocatorClassName.contains(".")) {
            allocatorClassName = "org.yawlfoundation.yawl.resourcing.allocators." +
                    allocatorClassName;
        }
        try {
            List<AbstractSelector> allocators = YConnector.getAllocators();
            if (allocators != null) {
                for (AbstractSelector allocator : allocators) {
                    if (allocator.getCanonicalName().equals(allocatorClassName)) {
                        return (AbstractAllocator) allocator;
                    }
                }
            }
        }
        catch (IOException ioe) {
            // fall through
        }
        return null;
    }
}
