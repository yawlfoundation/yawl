package org.yawlfoundation.yawl.resourcing.jsf.dynform;

/**
 * Author: Michael Adams
 * Creation Date: 5/07/2008
 */
public class DataInputElement {

    private String _name;
    private String _datatype;
    private String _value;
    private long _minoccurs = 1;
    private long _maxoccurs = 1;
    private long _occursCount;
    private int _level;
    private int _order;
    private boolean _inputOnly ;
    private boolean _required ;


//    private parent
//yparam

    public DataInputElement() {}

    public DataInputElement(String name, String datatype, String value) {
        _name = name;
        _datatype = datatype;
        _value = value ;
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

    public void setMinoccurs(String minoccurs) {
        this._minoccurs = convertOccurs(minoccurs);
    }
    public long getMaxoccurs() {
        return _maxoccurs;
    }

    public void setMaxoccurs(long maxoccurs) {
        this._maxoccurs = maxoccurs;
    }

    public void setMaxoccurs(String maxoccurs) {
        this._maxoccurs = convertOccurs(maxoccurs);
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
        return _inputOnly;
    }

    public void setInputOnly(boolean inputOnly) {
        this._inputOnly = inputOnly;
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

}
