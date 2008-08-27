package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * Author: Michael Adams
 * Creation Date: 23/08/2008
 */
public class DynFormFieldListType {

    private Element _baseElement;
    private String _itemType;
    private DynFormField _owner;


    public DynFormFieldListType(Element list) {
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