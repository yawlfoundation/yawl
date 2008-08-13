package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.TextField;
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;

import javax.faces.component.UIComponent;
import javax.xml.datatype.DatatypeFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 7/08/2008
 */

public class DynFormValidator {

    private MessagePanel _msgPanel;


    public DynFormValidator() { }

    public boolean validate(PanelLayout panel, MessagePanel msgPanel) {
        _msgPanel = msgPanel;
        return validateInputs(panel);
    }


    private boolean validateInputs(PanelLayout panel) {
         boolean subResult = true, finalResult = true ;
         List components = panel.getChildren();
         if (components != null) {
             Iterator itr = components.iterator();

             // checkboxes, dropdowns & calendars are self validating - only need to do textfields
             while (itr.hasNext()) {
                 UIComponent component = (UIComponent) itr.next();
                 if (component instanceof SubPanel)
                     subResult = validateInputs((SubPanel) component) ;
                 else if (component instanceof TextField) {
                     TextField field = (TextField) component;
                     if ((! field.isDisabled()) && (field.getText() != null) &&
                          (((String) field.getText()).length() > 0)) {

                         // strip type out of tooltip - this construct was necessary
                         // since the whole string is passed with an offset, causing
                         // equals to fail in the validateField method
                         String type = new String(field.getToolTip().split(" ")[5]);
                         subResult = validateField(type, field);
                     }
                 }
                 finalResult = (finalResult && subResult) ;
             }
         }
         return finalResult ;
     }

     private boolean validateField(String type, TextField field) {
         String text = (String) field.getText();
         boolean result = true;
         if (type.equals("string"))
             result = true ;
         else if (type.equals("long"))
             result = validateLong(text, field.getId());
         else if (type.equals("double"))
             result = validateDouble(text, field.getId());
         else if (type.equals("time"))
             result = validateTime(text, field.getId());
         else if (type.equals("duration"))
             result = validateDuration(text, field.getId());
         return result ;
     }


     private boolean validateLong(String value, String fieldName) {
         try {
             new Long(value);
             return true;
         }
         catch (NumberFormatException nfe) {
             addValidationErrorMessage(value, fieldName, "long") ;
             return false ;
         }
     }


     private boolean validateDouble(String value, String fieldName) {
         try {
             new Double(value);
             return true;
         }
         catch (NumberFormatException nfe) {
             addValidationErrorMessage(value, fieldName, "double") ;
             return false ;
         }
     }


     private boolean validateTime(String value, String fieldName) {
         try {
             DateFormat df = DateFormat.getTimeInstance();
             df.parse(value);
             return true;
         }
         catch (ParseException pe) {
             addValidationErrorMessage(value, fieldName, "time") ;
             return false ;
         }
     }


     private boolean validateDuration(String value, String fieldName) {
         try {
             DatatypeFactory factory = DatatypeFactory.newInstance();
             factory.newDuration(value);
             return true;
         }
         catch (Exception e) {
             addValidationErrorMessage(value, fieldName, "duration") ;
             return false ;
         }
     }


     private void addValidationErrorMessage(String value, String fieldName, String type) {
         String msg  =
             String.format("Invalid value '%s' in field %s, expecting a valid %s value",
                            value, normaliseFieldName(fieldName), type);
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
    
}
