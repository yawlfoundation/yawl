/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 23/08/2008
 */
public class DynFormFieldUnion {

    private Element _baseElement;
    private List<DynFormFieldRestriction> _restrictionList;
    private List<String> _enumeration;
    private DynFormField _owner;
    private List<String> _memberTypes;


    public DynFormFieldUnion(Element union, Namespace ns) {
        _baseElement = union;
        _restrictionList = new ArrayList<DynFormFieldRestriction>();
        _enumeration = new ArrayList<String>();
        parse(union, ns);
    }

    
    private void parse(Element union, Namespace ns) {
        _memberTypes = parseMemberList(union.getAttributeValue("memberTypes"));
        for (Element child : union.getChildren("simpleType", ns)) {
            Element restriction = child.getChild("restriction", ns);
            if (restriction != null) {
                _restrictionList.add(new DynFormFieldRestriction(restriction, ns));
            }
        }
        combineEnumeratedValues();
    }


    private List<String> parseMemberList(String memberList) {
        List<String> result = new ArrayList<String>();
        String[] list = memberList.split(" ");
        for (String member : list) {
            result.add(member.trim());
        }
       return result;
    }

    private void combineEnumeratedValues() {
        for (DynFormFieldRestriction restriction : _restrictionList) {
            if (restriction.hasEnumeration()) {
                _enumeration.addAll(restriction.getEnumeration());
            }
        }
    }


    public boolean hasEnumeration() {
        return ! _enumeration.isEmpty();
    }


    public List<String> getEnumeration() {
        return _enumeration;
    }


    public String getBaseType() {
        if (hasEnumeration())
            return _restrictionList.get(0).getBaseType();
        else
            return "xsd:string";                     // should never have to return this
    }


    public DynFormField getOwner() {
        return _owner;
    }

    public void setOwner(DynFormField owner) {
        _owner = owner;
    }
    

    public String getBaseElement() {
        String prefix = DynFormValidator.NS_PREFIX;
        return String.format("<%s:simpleType>%s</%s:simpleType>", prefix,
                JDOMUtil.elementToStringDump(_baseElement), prefix);
    }


}
