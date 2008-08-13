package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 5/07/2008
 */
public class DynFormField {

    private String _name;
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
    private List<String> _enumMembers;

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
    }

    /******************************************************************************/

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
        if (_datatype.indexOf(':') > -1)
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

    public boolean isInputOnly() {
        if (_param != null)
           return _param.isInputOnly();
        else
           return false;
    }

    public boolean isRequired() {
        return _required;
    }

    public void setRequired(boolean required) {
        this._required = required;
    }

    public void setRequired() {
        _required = (_minoccurs > 0);
    }

    public void setEnumeratedValues(List<String> enumValues) {
        _enumMembers = enumValues;
    }

    public List<String> getEnumeratedValues() {
        return _enumMembers;
    }

    public boolean hasEnumeratedValues() {
        return _enumMembers != null;
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


    public String getToolTip() {
        return String.format("Please enter a value of %s type", getDataTypeUnprefixed());
    }



}
