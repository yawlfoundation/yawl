/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * Author: Michael Adams
 * Creation Date: 23/08/2008
 */
public class DynFormFieldListFacet {

    private Element _baseElement;
    private String _itemType;
    private DynFormField _owner;


    public DynFormFieldListFacet(Element list) {
        _baseElement = list;
        _itemType = list.getAttributeValue("itemType");
    }


    public String getItemType() {
        return _itemType;
    }


    public String getItemTypeUnprefixed() {
        if (_itemType.indexOf(':') > -1)
            return _itemType.split(":")[1] ;
        else
            return _itemType ;
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


    public String getToolTipExtn() {
        return String.format("a list of '%s' values, separated by spaces",
                getItemTypeUnprefixed());
    }



}