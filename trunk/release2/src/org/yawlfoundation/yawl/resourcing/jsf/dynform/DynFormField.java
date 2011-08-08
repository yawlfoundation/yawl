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

import org.yawlfoundation.yawl.schema.XSDType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 5/07/2008
 */
public class DynFormField implements Cloneable {

    private String _name;                               // the parameter name
    private String _datatype;
    private String _value;
    private long _minoccurs = 1;
    private long _maxoccurs = 1;
    private boolean _nullMinoccurs = true;
    private boolean _nullMaxoccurs = true;
    private FormParameter _param;
    private long _occursCount;
    private int _level;
    private int _order;
    private boolean _required ;
    private boolean _hidden = false;
    private boolean _emptyComplexTypeFlag = false;
    private Boolean _hideApplied = null;               // to avoid double hideIf eval
    private Font _font;                                // used for screen arithmetic

    private DynFormField _parent;
    private DynFormFieldRestriction _restriction;
    private DynFormFieldUnion _union;
    private DynFormFieldListFacet _list;

     // the variable's extended attributes
    private DynFormUserAttributes _attributes = new DynFormUserAttributes();     

    private List<DynFormField> _subFieldList;
    private String _groupID;
    private String _choiceID;


    public DynFormField() {}

    public DynFormField(String name, String datatype, String value) {
        _name = name;
        _datatype = datatype;
        _value = value ;
        _occursCount = 1;
    }

    public DynFormField(String name, List<DynFormField> subList) {
        _name = name;
        _subFieldList = subList;
        if (subList != null) {
            for (DynFormField field : subList) field.setParent(this);
        }
    }

    /******************************************************************************/

    public DynFormField clone() {
        try {
            return (DynFormField) super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }


    // SETTERS & GETTERS //

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getDatatype() {
        return _datatype;
    }

    public void setDatatype(String datatype) {
        this._datatype = datatype;
    }

    public String getDataTypeUnprefixed() {
        if ((_datatype != null) && (_datatype.indexOf(':') > -1))
            return _datatype.split(":")[1] ;
        else
            return _datatype ;
    }

    public String getNamespacePrefix() {
        if (_datatype.indexOf(':') > -1)
            return _datatype.split(":")[0] ;
        else
            return "" ;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public long getMinoccurs() {
        return _minoccurs;
    }

    public void setMinoccurs(long minoccurs) {
        this._minoccurs = minoccurs;
    }

    public String getMinOccursStr() {
        return String.valueOf(_minoccurs) ;
    }

    public void setMinoccurs(String minoccurs) {
        _nullMinoccurs = (minoccurs == null) ;
        this._minoccurs = convertOccurs(minoccurs);
    }
    public long getMaxoccurs() {
        return _maxoccurs;
    }

    public void setMaxoccurs(long maxoccurs) {
        this._maxoccurs = maxoccurs;
    }

    public void setMaxoccurs(String maxoccurs) {
        _nullMaxoccurs = (maxoccurs == null) ;
        this._maxoccurs = convertOccurs(maxoccurs);
    }

    public String getMaxOccursStr() {
        return String.valueOf(_maxoccurs) ;
    }


    public boolean isNullMinoccurs() {
        return _nullMinoccurs;
    }

    public void setNullMinoccurs(boolean nullMinoccurs) {
        this._nullMinoccurs = nullMinoccurs;
    }

    public boolean isNullMaxoccurs() {
        return _nullMaxoccurs;
    }

    public void setNullMaxoccurs(boolean nullMaxoccurs) {
        this._nullMaxoccurs = nullMaxoccurs;
    }

    public FormParameter getParam() {
        return _param;
    }

    public void setParam(FormParameter param) {
        _param = param;
    }

    public int getLevel() {
        return _level;
    }

    public void setLevel(int level) {
        this._level = level;
    }

    public int getOrder() {
        return _order;
    }

    public void setOrder(int order) {
        this._order = order;
    }

    public long getOccursCount() {
        return _occursCount;
    }

    public void setOccursCount(long occursCount) {
        this._occursCount = occursCount;
    }

    public DynFormField getParent() {
        return _parent;
    }

    public void setParent(DynFormField parent) {
        _parent = parent;
    }


    public void setEmptyComplexTypeFlag(boolean flag) {
        _emptyComplexTypeFlag = flag;
    }


    public boolean isEmptyComplexTypeFlag() { return _emptyComplexTypeFlag; }


    public void setRequired(boolean required) {
        this._required = required;
    }


    public boolean isRequired() {
        _required = (! (isInputOnly() || hasZeroMinimum() || _attributes.isOptional()))
                || _attributes.isMandatory();
        return _required;
    }


    public void hide() { _hidden = true; }

    public void show() { _hidden = false; }


    public boolean hasZeroMinimum() {
        return (_minoccurs == 0);
    }
    
    public void setEnumeratedValues(List<String> enumValues) {
        if (_restriction != null)
            _restriction.setEnumeration(enumValues);
    }

    public List<String> getEnumeratedValues() {
        if ((_union != null) && _union.hasEnumeration()) {
            return _union.getEnumeration();
        }
        else if ((_restriction != null) && _restriction.hasEnumeration()) {
            return _restriction.getEnumeration();
        }
        else return null;
    }

    public boolean hasEnumeratedValues() {
        return  ((_union != null) && _union.hasEnumeration()) ||
                ((_restriction != null) && _restriction.hasEnumeration()) ;
    }

    public void setSubFieldList(List<DynFormField> subList) {
        _subFieldList = subList;
    }

    public List<DynFormField> getSubFieldList() {
        return _subFieldList;
    }

    public boolean isFieldContainer() {
        return _subFieldList != null ;
    }

    public void addSubField(DynFormField field) {
        if (_subFieldList == null)
            _subFieldList = new ArrayList<DynFormField>();
        field.setParent(this);
        _subFieldList.add(field);
    }

    public void addSubFieldList(List<DynFormField> fieldList) {
        if (fieldList != null) {
            for (DynFormField field : fieldList) {
                addSubField(field);
            }
        }
    }

    public boolean removeSubField(DynFormField field) {
        return _subFieldList == null || _subFieldList.remove(field);
    }
    

    public String getGroupID() {
        return _groupID;
    }

    public void setGroupID(String groupID) {
        this._groupID = groupID;
    }

    public String getChoiceID() {
        return _choiceID;
    }

    public void setChoiceID(String choiceID) {
        this._choiceID = choiceID;
    }

    public boolean isChoiceField() { return _choiceID != null; }

    public boolean isSimpleField() {
        return _subFieldList == null ;
    }

    public Font getFont() {
        if (_font == null) _font = getUserDefinedFont();
        return _font;
    }

    public DynFormFieldRestriction getRestriction() {
        return _restriction;
    }

    public void setRestriction(DynFormFieldRestriction restriction) {
        this._restriction = restriction;
        _restriction.setOwner(this);
    }

    public boolean hasRestriction() {
        return _restriction != null;
    }

    public void setUnion(DynFormFieldUnion union) {
        _union = union;
        _union.setOwner(this);
    }

    public DynFormFieldUnion getUnion() {
        return _union;
    }

    public boolean hasUnion() {
        return _union != null;
    }

    public boolean hasParent() {
        return _parent != null;
    }


    public void setListType(DynFormFieldListFacet list) {
        _list = list;
        _list.setOwner(this);
    }

    public DynFormFieldListFacet getListType() {
        return _list;
    }

    public boolean hasListType() {
        return _list != null;
    }

    public DynFormUserAttributes getAttributes() {
        return _attributes;
    }

    public void setAttributes(DynFormUserAttributes attributes) {
        _attributes = attributes;
    }

    private long convertOccurs(String occurs) {
        long result = 1 ;

        if (occurs != null) {
            if (occurs.equals("unbounded"))
                result = Long.MAX_VALUE ;
            else {
                try {
                    result = new Long(occurs) ;
                }
                catch (Exception e) {
                    // nothing to do - default 1 will be returned
                }
            }
        }
        return result;
    }

    
    public boolean isPrimitiveType() {
        String type = getDataTypeUnprefixed();
        return (type.equals("string")  ||
                type.equals("double")  ||
                type.equals("long")    ||
                type.equals("boolean") ||
                type.equals("date")    ||
                type.equals("time")    ||
                type.equals("duration"));
    }


    public boolean equals(DynFormField other) {
        return (other.getName().equals(this.getName()) &&
                other.getDatatype().equals(this.getDatatype())); 
    }


    // extended attribute support //
    
    public String getAlertText() {
        return hasParent() ? _parent.getAlertText() : _attributes.getAlertText();
    }

    
    public String getLabelText() {
        String label = _attributes.getLabelText();
        return (label != null) ? label : _name ;              // default to param name
    }


    public boolean isInputOnly() {
        return (hasParent() && _parent.isInputOnly()) ||
               (_param != null && (_param.isInputOnly() || _param.isReadOnly())) ||
               _attributes.isReadOnly() || hasBlackoutAttribute();
    }


    public boolean hasHideAttribute() {
        return (hasParent() && _parent.hasHideAttribute()) ||
                _hidden || _attributes.isHidden() ;
    }


    public boolean hasHideIfAttribute(String data) {        
        return (hasParent() && _parent.hasHideIfAttribute(data)) ||
                _attributes.isHideIf(data);
    }

    public boolean isHidden(String data) {
        if (data == null) return false;
        if (_hideApplied == null) {
            _hideApplied = hasHideAttribute() || hasHideIfAttribute(data);
        }
        return _hideApplied;
    }

    public boolean isVisible() {
        return (_hideApplied == null) || (! _hideApplied);
    }

    public boolean isEmptyOptionalInputOnly() {
        return isInputOnly() && _attributes.isOptional() && hasNullValue();
    }

    public boolean hasNullValue() {
        String type = getDataTypeUnprefixed();
        return (_value == null) && (type != null) && (! type.equals("string"));
    }

    public String getToolTip() {
        String tip = _attributes.getToolTipText();
        return (tip != null) ? tip : getDefaultToolTip();
    }


    public String getDefaultToolTip() {
        if (hasBlackoutAttribute()) {
            return " This field is intentionally blacked-out ";
        }
        String type = (_param.getDataTypeName().equals("YTimerType")) ? "Duration or DateTime"
                                                              : getDataTypeUnprefixed();
        String tip = String.format(" Please enter a value of %s type", type);
        if (hasRestriction())
            tip += _restriction.getToolTipExtn();
        else if (hasListType())
            tip = " Please enter " + _list.getToolTipExtn();

        return tip + " ";
    }

    public boolean hasSkipValidationAttribute() {
        return (hasParent() && _parent.hasSkipValidationAttribute()) ||
                _attributes.isSkipValidation() ;
    }


    public String getTextJustify() {
        return hasParent() ? _parent.getTextJustify() : _attributes.getTextJustify();
    }

    public boolean hasBlackoutAttribute() {
        return (hasParent() && _parent.hasBlackoutAttribute()) ||
                _attributes.isBlackout() ;
    }

    public String getUserDefinedFontStyle() {
        return hasParent() ? _parent.getUserDefinedFontStyle() :
                _attributes.getUserDefinedFontStyle();
    }

    public String getBackgroundColour() {
        return hasParent() ? _parent.getBackgroundColour() :
                _attributes.getBackgroundColour();
    }

    public boolean isTextArea() {
        return _attributes.isTextArea();
    }

    public String getImageAbove() {
        return _attributes.getImageAbove();
    }

    public String getImageBelow() {
        return _attributes.getImageBelow();
    }

    public String getImageAboveAlign() {
        return _attributes.getImageAboveAlign();
    }

    public String getImageBelowAlign() {
        return _attributes.getImageBelowAlign();
    }

    public boolean isLineAbove() {
        return _attributes.isLineAbove();
    }

    public boolean isLineBelow() {
        return _attributes.isLineBelow();
    }

    public String getTextAbove() {
        return _attributes.getTextAbove();
    }

    public String getTextBelow() {
        return _attributes.getTextBelow();
    }

    public Font getUserDefinedFont() {
        return hasParent() ? _parent.getUserDefinedFont() :
                _attributes.getUserDefinedFont();
    }

    public void setRestrictionAttributes() {
        XSDType xsdType = XSDType.getInstance();
        if (xsdType.isBuiltInType(getDataTypeUnprefixed())) {    // base types only
            char[] validFacetMap = xsdType.getConstrainingFacetMap(getDataTypeUnprefixed());
            for (XSDType.RestrictionFacet facet : XSDType.RestrictionFacet.values()) {
                if (validFacetMap[facet.ordinal()] == '1') {
                    String value = _attributes.getValue(facet.name());
                    if (value != null) {
                        getOrCreateRestriction().setRestrictionFacet(facet, value);
                    }
                }
            }
        }
    }

    private DynFormFieldRestriction getOrCreateRestriction() {
        if (! hasRestriction()) {
            _restriction = new DynFormFieldRestriction(this);
            _restriction.setBaseType(getDatatype());
        }
        _restriction.setModifiedFlag();          // causes a restriction element rebuild
        return _restriction;
    }

}
