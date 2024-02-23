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

package org.yawlfoundation.yawl.stateless.elements;



import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifierBag;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.util.List;

/**
 * 
 * An external condition is equivalent to a condition in the YAWL paper.
 * @author Lachlan Aldred
 * 
 */
public class YCondition extends YExternalNetElement implements YConditionInterface {


    protected YIdentifierBag _bag;
    private boolean _isImplicit;

    public YCondition(String id, String label, YNet container) {
        this(id, container);
        _name = label;
    }


    public YCondition(String id, YNet container) {
        super(id, container);
        _bag = new YIdentifierBag(this);
    }


    public void setImplicit(boolean isImplicit) {
        _isImplicit = isImplicit;
    }


    public boolean isImplicit() {
        return _isImplicit;
    }


    public void verify(YVerificationHandler handler) {
        super.verify(handler);
    }


    public boolean isAnonymous() {
        return _name == null;
    }


    public void add(YIdentifier identifier) {
        _bag.addIdentifier(identifier);
    }

    public boolean contains(YIdentifier identifier) {
        return _bag.contains(identifier);
    }

    public boolean containsIdentifier() {
        return _bag.getIdentifiers().size() > 0;
    }

    public int getAmount(YIdentifier identifier) {
        return _bag.getAmount(identifier);
    }

    public List<YIdentifier> getIdentifiers() {
        return _bag.getIdentifiers();
    }

    public YIdentifier removeOne() {
        YIdentifier identifier = getIdentifiers().get(0);
        _bag.remove(identifier, 1);
        return identifier;
    }

    public void removeOne(YIdentifier identifier) {
        _bag.remove(identifier, 1);
    }

    public void remove(YIdentifier identifier, int amount)  {
        _bag.remove(identifier, amount);
    }

    public void removeAll(YIdentifier identifier) {
        _bag.remove(identifier, _bag.getAmount(identifier));
    }

    public synchronized void removeAll()  {
        _bag.removeAll();
    }

    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YCondition copiedCondition = (YCondition) super.clone();
        copiedCondition._bag = new YIdentifierBag(copiedCondition);
        return copiedCondition;
    }


    public String toXML() {
        String tag = "condition";
        if (this instanceof YInputCondition) {
            tag ="inputCondition";
        }
        else if (this instanceof YOutputCondition) {
            tag = "outputCondition";
        }
        StringBuilder xml = new StringBuilder(String.format("<%s id=\"%s\">", tag, getID()));
        xml.append(super.toXML());
        xml.append(String.format("</%s>", tag));
        return xml.toString();
    }
}
