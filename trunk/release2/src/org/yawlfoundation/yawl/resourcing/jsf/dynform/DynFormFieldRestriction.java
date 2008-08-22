/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 15/08/2008
 */
public class DynFormFieldRestriction {

    private DynFormField _owner;
    private Element _baseElement;

    private String _baseType;
    private String _length;
    private String _minLength;
    private String _maxLength;
    private String _pattern;
    private String _whiteSpace;
    private String _minInclusive;
    private String _minExclusive;
    private String _maxInclusive;
    private String _maxExclusive;
    private String _totalDigits;
    private String _fractionDigits;

    private List<String> _enumeration;

    static final String LENGTH = "length";
    static final String MINLENGTH = "minLength";
    static final String MAXLENGTH = "maxLength";
    static final String PATTERN = "pattern";
    static final String WHITESPACE = "whiteSpace";
    static final String MININCLUSIVE = "minInclusive";
    static final String MINEXCLUSIVE = "minExclusive";
    static final String MAXINCLUSIVE = "maxInclusive";
    static final String MAXEXCLUSIVE = "maxExclusive";
    static final String TOTALDIGITS = "totalDigits";
    static final String FRACTIONDIGITS = "fractionDigits";
    
    public DynFormFieldRestriction() { }

    public DynFormFieldRestriction(DynFormField field) {
        _owner = field;
    }

    public DynFormFieldRestriction(Element restriction, Namespace ns) {
        _baseElement = restriction;
        parse(restriction, ns);
    }


    private void parse(Element restriction, Namespace ns) {
        _baseType = restriction.getAttributeValue("base");
        _enumeration = getEnumeratedValues(restriction, ns);
        _length = getRestrictionValue(restriction, ns, LENGTH);
        _minLength = getRestrictionValue(restriction, ns, MINLENGTH);
        _maxLength = getRestrictionValue(restriction, ns, MAXLENGTH);
        _pattern = getRestrictionValue(restriction, ns, PATTERN);
        _whiteSpace = getRestrictionValue(restriction, ns, WHITESPACE);
        _minInclusive = getRestrictionValue(restriction, ns, MININCLUSIVE);
        _minExclusive = getRestrictionValue(restriction, ns, MINEXCLUSIVE);
        _maxInclusive = getRestrictionValue(restriction, ns, MAXINCLUSIVE);
        _maxExclusive = getRestrictionValue(restriction, ns, MAXEXCLUSIVE);
        _totalDigits = getRestrictionValue(restriction, ns, TOTALDIGITS);
        _fractionDigits = getRestrictionValue(restriction, ns, FRACTIONDIGITS);
    }


    private List<String> getEnumeratedValues(Element restriction, Namespace ns) {
        List<String> result = null;
        if (restriction != null) {
            List enumChildren = restriction.getChildren("enumeration", ns);
            if ((enumChildren != null) && ! enumChildren.isEmpty()) {
                result = new ArrayList<String>();
                for (int i = 0; i < enumChildren.size(); i++) {
                    Element enumChild = (Element) enumChildren.get(i);
                    result.add(enumChild.getAttributeValue("value"));
                }
            }
        }
        return result;
    }

    private String getRestrictionValue(Element restriction, Namespace ns, String type) {
        String result = null;
        Element child = restriction.getChild(type, ns);
        if (child != null) {
            result = child.getAttributeValue("value");
        }
        return result;
    }


    public String getBaseType() {
        return _baseType;
    }

    public void setBaseType(String baseType) {
        _baseType = baseType;
    }

    public DynFormField getOwner() {
        return _owner;
    }

    public void setOwner(DynFormField owner) {
        _owner = owner;
    }

    public String getMinLength() {
        return _minLength;
    }

    public int getMinLengthValue() {
        try {
            return Integer.parseInt(_minLength) ;
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public void setMinLength(String minLength) {
        _minLength = minLength;
    }

    public String getMaxLength() {
        return _maxLength;
    }

    public int getMaxLengthValue() {
        try {
            return Integer.parseInt(_maxLength) ;
        }
        catch (NumberFormatException nfe) {
            return Integer.MAX_VALUE;
        }
    }

    public void setMaxLength(String maxLength) {
        _maxLength = maxLength;
    }

    public String getPattern() {
        return _pattern;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;
    }

    public List<String> getEnumeration() {
        return _enumeration;
    }

    public void setEnumeration(List<String> enumeration) {
        _enumeration = enumeration;
    }

    public String getMinInclusive() {
        return _minInclusive;
    }

    public int getMinInclusiveValue() {
        try {
            return Integer.parseInt(_minInclusive) ;
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public void setMinInclusive(String minInclusive) {
        _minInclusive = minInclusive;
    }

    public String getMinExclusive() {
        return _minExclusive;
    }

    public int getMinExclusiveValue() {
        try {
            return Integer.parseInt(_minExclusive) ;
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public void setMinExclusive(String minExclusive) {
        _minExclusive = minExclusive;
    }

    public String getMaxInclusive() {
        return _maxInclusive;
    }

    public int getMaxInclusiveValue() {
        try {
            return Integer.parseInt(_maxInclusive) ;
        }
        catch (NumberFormatException nfe) {
            return Integer.MAX_VALUE;
        }
    }

    public void setMaxInclusive(String maxInclusive) {
        _maxInclusive = maxInclusive;
    }

    public String getMaxExclusive() {
        return _maxExclusive;
    }

    public int getMaxExclusiveValue() {
        try {
            return Integer.parseInt(_maxExclusive) ;
        }
        catch (NumberFormatException nfe) {
            return Integer.MAX_VALUE;
        }
    }

    public void setMaxExclusive(String maxExclusive) {
        _maxExclusive = maxExclusive;
    }

    public String getLength() {
        return _length;
    }

    public int getLengthValue() {
        try {
            return Integer.parseInt(_length) ;
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }



    public void setLength(String length) {
        _length = length;
    }

    public String getWhitespace() {
        return _whiteSpace;
    }

    public void setWhitespace(String whitespace) {
        _whiteSpace = whitespace;
    }

    public String getTotalDigits() {
        return _totalDigits;
    }

    public int getTotalDigitsValue() {
        try {
            return Integer.parseInt(_totalDigits) ;
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }


    public void setTotalDigits(String totalDigits) {
        _totalDigits = totalDigits;
    }

    public String getFractionDigits() {
        return _fractionDigits;
    }

    public int getFractionDigitsValue() {
        try {
            return Integer.parseInt(_fractionDigits) ;
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }


    public void setFractionDigits(String fractionDigits) {
        _fractionDigits = fractionDigits;
    }


    private String getBaseTypeUnprefixed() {
        if (_baseType.indexOf(':') > -1)
            return _baseType.split(":")[1] ;
        else
            return _baseType ;

    }


    public String getToolTipExtn() {
        List<String> msgList = new ArrayList<String>();
        String result = "";

        if (hasLength()) {
            msgList.add(String.format(" with a length of exactly %s characters", _length));
        }
        
        if (hasMinLength() && hasMaxLength()) {
            msgList.add(String.format(" with a length between %s and %s characters",
                                       _minLength, _maxLength));
        }
        else if (hasMinLength()) {
            msgList.add(String.format(" with a minimum length of %s characters", _minLength));
        }
        else if (hasMaxLength()) {
            msgList.add(String.format(" with a maximum length of %s characters", _maxLength));
        }

        if (hasPattern()) {
            msgList.add(String.format(" matching the pattern '%s'", _pattern));
        }

        if (hasWhitespace()) {}

        if (hasMinInclusive()) {
            msgList.add(String.format(" with a value >= %s", _minInclusive));
        }
        if (hasMaxInclusive()) {
            msgList.add(String.format(" with a value <= %s", _maxInclusive));
        }
        if (hasMinExclusive()) {
            msgList.add(String.format(" with a value > %s", _minExclusive));
        }
        if (hasMaxExclusive()) {
            msgList.add(String.format(" with a value < %s", _maxExclusive));
        }
        if (hasTotalDigits()) {
            msgList.add(String.format(
                " having no more than %s digits (excluding the decimal point)", _totalDigits));
        }
        if (hasFractionDigits()) {
            msgList.add(String.format(
                " having no more than %s digits to the right of the decimal point", _fractionDigits));
        }

        if (msgList.size() == 1)
            result = msgList.get(0) ;
        else {
            int i;
            for (i=0; i < msgList.size()-1; i++) {
                result += "," + msgList.get(i);
            }
            result += " and" + msgList.get(i);
        }

        return result;

    }


    public String getBaseElement() {
        String prefix = DynFormValidator.NS_PREFIX;
        return String.format("<%s:simpleType>%s</%s:simpleType>", prefix,
                             JDOMUtil.elementToStringDump(_baseElement), prefix);
    }

    // rebuild the restriction part of the schema
    public String rebuildSchema() {
        String prefix = DynFormValidator.NS_PREFIX;
        StringBuilder schema = new StringBuilder(getSchemaLeadIn(prefix));

        if (hasLength())
            schema.append(getSchemaTrack(prefix, LENGTH, _length));
        if (hasMinLength())
            schema.append(getSchemaTrack(prefix, MINLENGTH, _minLength));
        if (hasMaxLength())
            schema.append(getSchemaTrack(prefix, MAXLENGTH, _maxLength));
        if (hasPattern())
            schema.append(getSchemaTrack(prefix, PATTERN, _pattern));
        if (hasWhitespace())
            schema.append(getSchemaTrack(prefix, WHITESPACE, _whiteSpace));
        if (hasMinInclusive())
            schema.append(getSchemaTrack(prefix, MININCLUSIVE, _minInclusive));
        if (hasMinExclusive())
            schema.append(getSchemaTrack(prefix, MINEXCLUSIVE, _minExclusive));
        if (hasMaxInclusive())
            schema.append(getSchemaTrack(prefix, MAXINCLUSIVE, _maxInclusive));
        if (hasMaxExclusive())
            schema.append(getSchemaTrack(prefix, MAXEXCLUSIVE, _maxExclusive));
        if (hasTotalDigits())
            schema.append(getSchemaTrack(prefix, TOTALDIGITS, _totalDigits));
        if (hasFractionDigits())
            schema.append(getSchemaTrack(prefix, FRACTIONDIGITS, _fractionDigits));

        schema.append(getSchemaFadeOut(prefix));
        return schema.toString();
    }

    private String getSchemaLeadIn(String prefix) {
        return String.format("<%s:simpleType><%s:restriction base=\"%s:%s\">",
                         prefix, prefix, prefix, getBaseTypeUnprefixed());
    }

    private String getSchemaFadeOut(String prefix) {
       return String.format("</%s:restriction></%s:simpleType>", prefix, prefix);
    }

    private String getSchemaTrack(String prefix, String track, String value) {
        return String.format("<%s:%s value=\"%s\"/>", prefix, track, value);
    }
    

    public boolean hasLength()         { return _length != null; }
    public boolean hasMinLength()      { return _minLength != null; }
    public boolean hasMaxLength()      { return _maxLength != null; }
    public boolean hasPattern()        { return _pattern != null; }
    public boolean hasWhitespace()     { return _whiteSpace != null; }
    public boolean hasMinInclusive()   { return _minInclusive != null; }
    public boolean hasMinExclusive()   { return _minExclusive != null; }
    public boolean hasMaxInclusive()   { return _maxInclusive != null; }
    public boolean hasMaxExclusive()   { return _maxExclusive != null; }
    public boolean hasTotalDigits()    { return _totalDigits != null; }
    public boolean hasFractionDigits() { return _fractionDigits != null; }
    public boolean hasEnumeration()    { return _enumeration != null; }

}
