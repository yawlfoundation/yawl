/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author Lachlan Aldred
 * Date: 3/11/2003
 * Time: 15:33:18
 * 
 */
public class YDataStateException extends YAWLException {
    //query fields
    protected String _queryString;
    protected Element _queriedData;

    //schema validation fields
    protected Element _schema;
    protected Element _dataInput;
    protected String _xercesErrors;

    protected String _source;


    protected static final XMLOutputter _machineout = new XMLOutputter(Format.getCompactFormat());
    protected static final XMLOutputter _out = new XMLOutputter(Format.getPrettyFormat());

    public static final String QUERYSTRING_NM = "queryString";
    public static final String QUERIEDDATA_NM = "queriedData";
    public static final String SCHEMA_NM = "schema";
    public static final String DATAINPUT_NM = "dataInput";
    public static final String XERCESERRORS_NM = "xercesErrors";
    public static final String SOURCE_NM = "source";


    public YDataStateException(String query, Element queriedData, String schema,
                               Element validationData,
                               String xercesErrors, String source, String message) {

        _queryString = query;
        _queriedData = queriedData;
        _dataInput = validationData;
        _xercesErrors = xercesErrors;
        _source = source;
        _message = message;
        if (schema != null) {
            try {
                _schema = _builder.build(new StringReader(schema)).getRootElement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getMessage() {
        return
                _message +
                "\nTask [" + _source + "]" +
                "\nXQuery [" + _queryString + "] " +
                "\nDocument [" + (_queriedData != null ? _out.outputString(_queriedData): "") + "]" +
                "\nSchema for Expected [" + (_schema != null ? _out.outputString(_schema): "") + "]" +
                "\nBut received [" + (_dataInput != null ? _out.outputString(_dataInput) : "") + "]" +
                "\nValidation error message [" + _xercesErrors + "]";
    }

    public Element get_dataInput() {
        return _dataInput;
    }

    public Object getSource() {
        return _source;
    }

    public String getErrors() {
        return _xercesErrors;
    }


    protected String toXMLGuts() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toXMLGuts());
        if (null != _queryString) {
            sb.append("<" + QUERYSTRING_NM + ">" + _queryString + "</" + QUERYSTRING_NM + ">");
        }
        if (null != _queriedData) {
            sb.append("<" + QUERIEDDATA_NM + ">" + _machineout.outputString(_queriedData) + "</" + QUERIEDDATA_NM + ">");
        }
        if (null != _schema) {
            sb.append("<" + SCHEMA_NM + ">" + _machineout.outputString(_schema) + "</" + SCHEMA_NM + ">");
        }
        if (null != _dataInput) {
            sb.append("<" + DATAINPUT_NM + ">" + _machineout.outputString(_dataInput) + "</" + DATAINPUT_NM + ">");
        }
        if (null != _xercesErrors) {
            sb.append("<" + XERCESERRORS_NM + ">" + _xercesErrors + "</" + XERCESERRORS_NM + ">");
        }
        if (null != _source) {
            sb.append("<" + SOURCE_NM + ">" + _source + "</" + SOURCE_NM + ">");
        }
        if (null != _message) {
            sb.append("<" + MESSAGE_NM + ">" + _message + "</" + MESSAGE_NM + ">");
        }
        return sb.toString();
    }

    public static YDataStateException unmarshall(Document exceptionDoc) {
        Element root = exceptionDoc.getRootElement();
        String queryString = root.getChildText(QUERYSTRING_NM);
        Element queriedData = root.getChild(QUERIEDDATA_NM);

        Element schema = root.getChild(SCHEMA_NM);
        Element dataInput = root.getChild(DATAINPUT_NM);
        String xercesErrors = root.getChildText(XERCESERRORS_NM);

        String source = root.getChildText(SOURCE_NM);
        String message = parseMessage(exceptionDoc);
        if (queryString == null) {
            return new YDataValidationException(
                    _out.outputString(schema), dataInput, xercesErrors, source, message);
        } else if (schema == null) {
            return new YDataQueryException(queryString, queriedData, source, message);
        }
        return new YDataStateException(
                queryString, queriedData, _out.outputString(schema), dataInput,
                xercesErrors, source, message);
    }

    public Element getSchema() {
        return _schema;
    }

}
