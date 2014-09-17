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

import com.sun.rave.web.ui.component.*;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;
import org.yawlfoundation.yawl.schema.ErrorHandler;
import org.yawlfoundation.yawl.util.DOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.component.UIComponent;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 7/08/2008
 */

public class DynFormValidator {

    private MessagePanel _msgPanel;
    private Map<UIComponent, DynFormField> _componentFieldLookup;

    public final static String NS_URI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    public final static String NS_PREFIX = "xsd";

    private final String _ns = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private final SchemaFactory _factory = SchemaFactory.newInstance(_ns);
    private final ErrorHandler _errorHandler = new ErrorHandler();


    public DynFormValidator() {
        _factory.setErrorHandler(_errorHandler);
    }

    
    public boolean validate(PanelLayout panel,
                            Map<UIComponent, DynFormField> componentFieldLookup,
                            MessagePanel msgPanel) {

        _componentFieldLookup = componentFieldLookup;
        _msgPanel = msgPanel;

        return validateInputs(panel);
    }


    private boolean validateInputs(PanelLayout panel) {
        boolean subResult = true, finalResult = true, ignoring = false ;
        List components = panel.getChildren();
        if (components != null) {

            // checkboxes & dropdowns are self validating - only need to do textfields
            // & calendars (calendars only for non-empty required values)
            for (Object o : components) {
                UIComponent component = (UIComponent) o;

                // if a choice is involved, the unselected field(s) must be ignored
                if (component instanceof RadioButton) {
                    ignoring = ! ((RadioButton) component).isChecked();
                }

                if (! ignoring) {
                    if (component instanceof SubPanel) {
                        subResult = validateInputs((SubPanel) component) ;
                    }
                    else if ((component instanceof TextField) ||
                             (component instanceof TextArea)) {
                        FieldBase field = (FieldBase) component;
                        if (! field.isDisabled()) {
                            subResult = validateField(field);
                        }
                    }

                    // need to check that if its required it has a value
                    else if ((component instanceof Calendar)) {
                        Calendar field = (Calendar) component;
                        if (! field.isDisabled()) {
                            subResult = validateCalendarField(field);
                        }

                    }
                    finalResult = (finalResult && subResult) ;
                }    
            }
        }
        return finalResult ;
    }


    private boolean validateField(FieldBase field) {
        boolean result;
        String text = (String) field.getText();
        DynFormField input = _componentFieldLookup.get(field);
        if ((input != null) && (! input.hasSkipValidationAttribute())) {
            if (isTimerExpiryField(input))
                result = validateExpiry(input, text);
            else
                result = validateField(input, text);
        }
        else {

            // strip type out of tooltip - this construct was necessary
            // since the whole string is passed with an offset, causing
            // equals to fail in the validateField method
            String type = new String(field.getToolTip().split(" ")[5]);
            result = validateBase(field.getId(), type, text, true);
        }
        field.setStyleClass(getStyleClass(input, result));

        return result;
    }


    private boolean validateField(DynFormField input, String value) {
        boolean isValid = validateRequired(input, value);
        if (isValid && (! isEmptyValue(value)))
            isValid = validateAgainstSchema(input, value);
        return isValid;
    }


    private boolean validateCalendarField(Calendar field) {
        boolean result = true;
        DynFormField input = _componentFieldLookup.get(field);
        if ((input != null) && (! input.hasSkipValidationAttribute())) {
            if (input.isRequired() && (field.getText() == null)) {
                if (_msgPanel != null) {
                    _msgPanel.error("Field '" + input.getLabelText() +
                            "' requires a value.\n");
                }
                result = false;
            }
        }
        field.setStyleClass(getStyleClass(input, result));
        return result;
    }


    private boolean validateExpiry(DynFormField input, String value) {
        try {
            DatatypeFactory.newInstance().newDuration(value);    // try duration 1st
            return true;
        }
        catch (Exception e) {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                sdf.parse(value);
                return true;
            }
            catch (ParseException pe) {
                addValidationErrorMessage(value, input.getName(), "Duration or DateTime", false);
                return false;
            }
        }
    }


    private boolean validateBase(String fieldName, String type, String text,
                                 boolean untreated) {
         boolean result = true;

         if ((text != null) && (text.length() > 0)) {
             if (type.equals("string"))
                 result = true ;
             else if (type.equals("long"))
                 result = validateLong(text, fieldName, untreated);
             else if (type.equals("int"))
                 result = validateInt(text, fieldName, untreated);                 
             else if (type.equals("double"))
                 result = validateDouble(text, fieldName, untreated);
             else if (type.equals("decimal"))
                 result = validateDecimal(text, fieldName, untreated);
             else if (type.equals("time"))
                 result = validateTime(text, fieldName, untreated);
             else if (type.equals("duration"))
                 result = validateDuration(text, fieldName, untreated);
         }

         return result ;
     }


    private boolean validateRequired(DynFormField input, String value) {
        boolean result = true;
        if (input.isRequired() && isEmptyValue(value)) {
            if (_msgPanel != null) {
                _msgPanel.error("Field '" + input.getLabelText() + "' requires a value.\n");
            }
            result = false;
        }
        return result;
    }


     private boolean validateLong(String value, String fieldName, boolean untreated) {
         try {
             new Long(value);
             return true;
         }
         catch (NumberFormatException nfe) {
             addValidationErrorMessage(value, fieldName, "long", untreated) ;
             return false ;
         }
     }


    private boolean validateInt(String value, String fieldName, boolean untreated) {
        try {
            new Integer(value);
            return true;
        }
        catch (NumberFormatException nfe) {
            addValidationErrorMessage(value, fieldName, "integer", untreated) ;
            return false ;
        }
    }


     private boolean validateDouble(String value, String fieldName, boolean untreated) {
         try {
             new Double(value);
             return true;
         }
         catch (NumberFormatException nfe) {
             addValidationErrorMessage(value, fieldName, "double", untreated) ;
             return false ;
         }
     }


    private boolean validateDecimal(String value, String fieldName, boolean untreated) {
         try {
             new Double(value);
             return true;
         }
         catch (NumberFormatException nfe) {
             addValidationErrorMessage(value, fieldName, "decimal", untreated) ;
             return false ;
         }
     }


     private boolean validateTime(String value, String fieldName, boolean untreated) {
         try {
             DateFormat df = DateFormat.getTimeInstance();
             df.parse(value);
             return true;
         }
         catch (ParseException pe) {
             addValidationErrorMessage(value, fieldName, "time", untreated) ;
             return false ;
         }
     }


     private boolean validateDuration(String value, String fieldName, boolean untreated) {
         try {
             DatatypeFactory factory = DatatypeFactory.newInstance();
             factory.newDuration(value);
             return true;
         }
         catch (Exception e) {
             addValidationErrorMessage(value, fieldName, "duration", untreated) ;
             return false ;
         }
     }



    private void addRestrictionErrorMessage(String type, DynFormField input) {
        String msg = null;
        String value = "";
        String name = input.getLabelText();
        if (type.equals("minLength")) {
            value = input.getRestriction().getMinLength();
            msg = "Field '%s' requires a value of at least %s characters.";
        }
        else if (type.equals("maxLength")) {
            value = input.getRestriction().getMaxLength();
            msg = "Field '%s' can have a value of no more than %s characters.";
        }
        else if (type.equals("length")) {
            value = input.getRestriction().getLength();
            msg = "Field '%s' requires a value of exactly %s characters.";
        }
        else if (type.equals("pattern")) {
            value = input.getRestriction().getPattern();
            msg = "The value in field '%s' must match the pattern '%s'.";
        }
        else if (type.equals("minInclusive")) {
            value = input.getRestriction().getMinInclusive();
            msg = "The value in field '%s' cannot be less than %s.";
        }
        else if (type.equals("minExclusive")) {
            value = input.getRestriction().getMinExclusive();
            msg = "The value in field '%s' must be greater than %s.";
        }
        else if (type.equals("maxInclusive")) {
            value = input.getRestriction().getMaxInclusive();
            msg = "The value in field '%s' cannot be greater than %s.";
        }
        else if (type.equals("maxExclusive")) {
            value = input.getRestriction().getMaxExclusive();
            msg = "The value in field '%s' must be less than %s.";
        }
        else if (type.equals("totalDigits")) {
            value = input.getRestriction().getTotalDigits();
            msg = "The value in field '%s' must have exactly %s digits.";
        }
        else if (type.equals("fractionDigits")) {
            value = input.getRestriction().getFractionDigits();
            msg = "The value in field '%s' must have exactly %s digits after the decimal point.";
        }
        if (! (msg == null || _msgPanel == null)) {
            _msgPanel.error(String.format(msg, name, value));
        }
    }


    private void addErrorMessage(DynFormField input, String value) {
        if (_msgPanel == null) return;
        String msg = input.getAlertText();
        if (msg == null) {
            String base = "The value '%s' is not valid for field '%s'. " +
                          "This field requires a value of %s type";
            msg = String.format(base, value, input.getLabelText(), input.getDataTypeUnprefixed());
            if (input.hasRestriction())
                msg += input.getRestriction().getToolTipExtn();
        }
        _msgPanel.error(msg + ".\n");
    }

    
     private void addValidationErrorMessage(String value, String fieldName,
                                            String type, boolean untreated) {
         if (_msgPanel == null) return;
         String name = untreated ? normaliseFieldName(fieldName) : fieldName ;
         String msg  =
             String.format("Invalid value '%s' in field '%s', expecting a valid %s value",
                            value, name, type);
         _msgPanel.error(msg);
     }


     private String normaliseFieldName(String name) {
         char[] chars = name.toCharArray();
         int len = chars.length ;
         char c = chars[--len];
         while ((c >= '0') && (c <= '9')) c = chars[--len];
         char[] result = new char[len-2];
         for (int i = 3; i <= len; i++) {
             result[i-3] = chars[i];
         }
         return new String(result);
     }

    
    private String getStyleClass(DynFormField input, boolean valid) {
        String result;
        if (valid) {
            if ((input != null) && input.isRequired())
                result = "dynformInputRequired";
            else
                result = "dynformInput" ;
        }
        else result = "dynformInputError" ;

        return result;
    }


    private boolean isEmptyValue(String value) {
        return ((value == null) || (value.length() < 1));
    }


    private boolean isTimerExpiryField(DynFormField input) {
        return input.getParam().getDataTypeName().equals("YTimerType");
    }


    private boolean validateAgainstSchema(DynFormField input, String value) {
        try {
            _errorHandler.reset();
            Schema schema = _factory.newSchema(getInputSchema(input));
            if (_errorHandler.isValid()) {
                Validator validator = schema.newValidator();
                validator.setErrorHandler(_errorHandler);
                validator.validate(getInputValueAsXML(input, value));
                if (! _errorHandler.isValid()) addErrorMessage(input, value);
                return _errorHandler.isValid();
            }
        }
        catch (SAXException se) {
            if (_msgPanel != null) _msgPanel.error(se.getMessage());
            se.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private SAXSource getInputSchema(DynFormField input) {
        return createSAXSource(buildInputSchema(input));
    }


    private SAXSource getInputValueAsXML(DynFormField input, String value) {
        if (input.getDataTypeUnprefixed().equals("string"))
            value = ServletUtils.urlEncode(value);               // encode string values
        return createSAXSource(StringUtil.wrap(value, input.getName()));
    }


    private SAXSource createSAXSource(String xml) {
        SAXSource result = null;
        try {
            result = new SAXSource(DOMUtil.createUTF8InputSource(xml));
        }
        catch(UnsupportedEncodingException uee) {
            // nothing to do - null will be returned
        }
        return result;
    }


    private String buildInputSchema(DynFormField input) {
        StringBuilder schema = new StringBuilder(getSchemaHeader());

        schema.append("<")
              .append(NS_PREFIX)
              .append(":element name=\"")
              .append(input.getName())
              .append("\"");

        if (input.hasUnion()) {
            schema.append(">")
                  .append(input.getUnion().getBaseElement())
                  .append("</xsd:element>");
        }
        else if (input.hasRestriction()) {
            schema.append(">")
                  .append(input.getRestriction().getBaseElement())
                  .append("</xsd:element>");
        }
        else {
            schema.append(" type=\"xsd:")
                  .append(input.getDataTypeUnprefixed())
                  .append("\"/>");
        }
        schema.append("</xsd:schema>") ;

        return schema.toString();
    }


    private String getSchemaHeader() {
        return String.format("<%s:schema xmlns:%s=\"%s\">", NS_PREFIX, NS_PREFIX, NS_URI); 
    }

}
