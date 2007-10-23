/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing;

import org.jdom.Element;

import java.util.*;

/**
 * The base class inherited by all of the 'selector' classes :- filters, constraints
 * and allocators
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public abstract class AbstractSelector {

    protected String _name ;                       // the (class) name for this selector
    protected String _displayName ;                // a 'user-friendly' name
    protected String _description ;                // what does it do?
    protected HashMap<String,String> _params =
                 new HashMap<String,String>() ;    // params used by the 'selection'

    /********************************************************************************/

    public AbstractSelector() {}                   // constructor for reflection


    public AbstractSelector(String name) { _name = name ;}


    public AbstractSelector(String name, String desc) {
        _name = name ;
        _description = desc ;
    }


    public AbstractSelector(String name, HashMap<String,String> params) {
        _name = name ;
        _params = params ;
    }
                  

    public AbstractSelector(String name, String desc, HashMap<String,String> params) {
        this(name, desc);
        _params = params ;
    }


    /********************************************************************************/

    // GETTERS & SETTERS //

    /**
     * @return a Set of parameter names needing values to be used in the
     *         performance of the selection
     */
    protected Set<String> getParamKeys() { return _params.keySet() ; }

    /** @return the name of this selector */
    public String getName() { return _name; }

    /** @return the display name of this selector */
    public String getDisplayName() { return _displayName; }

    /** @return how this selector is described */
    public String getDescription() { return _description ; }

    /** @return a HashMap of parameters of the form [name, value] */
    public HashMap getParams() { return _params; }

    /** @return the name of this selector class */
    public String getClassName() { return this.getClass().getSimpleName(); }

    /**
     * Retrieves the value of the specified parameter
     * @param key the name of the parameter
     * @return the specified parameter's value
     */
    public String getParamValue(String key) { return _params.get(key); }


    /**
     * Stores the class name of this 'selector'
     * @param name the name to set
     */
    public void setName(String name) { _name = name ; }

    /**
     * Sets the user-friendly display name of this 'selector'
     * @param name the name to set
     */
    public void setDisplayName(String name) { _displayName = name ; }

    /**
     * Sets the description of this 'selector'
     * @param desc the description value to set
     */
    public void setDescription(String desc) { _description = desc ; }

    /**
     * Sets (replaces) the parameters with the map passed
     * @param paramsMap the new parameter map of the form [name, value] (both Strings)
     */
    public void setParams(Map<String,String> paramsMap) {
        _params = (HashMap<String,String>) paramsMap ;
    }

    /**
     * Adds (does not replace) the parameters in the map passed to the selectors
     * parameters
     * @param paramMap the new parameter map of the form [name, value] (both Strings)
     */
    public void addParams(Map<String,String> paramMap) {
        _params.putAll(paramMap);
    }

    /**
     * Adds a single parameter passed to the selector's parameters
     * @param key the name of the parameter
     * @param value the value of the parameter
     */
    public void addParam(String key, String value) { _params.put(key, value) ; }


    /*******************************************************************************/

    /** @return an xml representation of this object's parameters (if any). Used to
     *  build the specification xml */
    protected String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append("<name>").append(_name).append("</name>");
        if (_params != null) {
            xml.append("<params>");
            String key ;
            Iterator itr = _params.keySet().iterator();

            // write the key and value for each parameter
            while (itr.hasNext()) {
                key = (String) itr.next() ;
                xml.append("<param>") ;
                xml.append("<key>").append(key).append("</key>");
                xml.append("<value>").append(_params.get(key)).append("</value>");
                xml.append("</param>");
            }
            xml.append("</params>");
        }
        return xml.toString();
    }

    /**
     * Unpacks the xml describing the parameters to a HashMap object
     * @param eParams
     * @return
     */
    protected static HashMap<String,String> unmarshalParams(Element eParams) {
        HashMap<String,String> result = new HashMap<String,String>() ;
        List<Element> params = eParams.getChildren();
        for (Element param : params)
            result.put(param.getChildText("key"), param.getChildText("value"));
        if (result.isEmpty()) return null ;
        return result ;
    }


    protected String getInformation(String outerTag) {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(outerTag).append(">");

        xml.append("<name>");
        if (_name != null) xml.append(_name);
        xml.append("</name>");

        xml.append("<displayName>");
        if (_displayName != null) xml.append(_displayName);
        xml.append("</displayName>");

        xml.append("<description>");
        if (_description != null) xml.append(_description);
        xml.append("</description>");

        xml.append("<keys>") ;
        for (String key : _params.keySet())
            xml.append("<key>").append(key).append("</key>");
        xml.append("</keys>") ;

        xml.append("</").append(outerTag).append(">");
        return xml.toString();
    }
}
