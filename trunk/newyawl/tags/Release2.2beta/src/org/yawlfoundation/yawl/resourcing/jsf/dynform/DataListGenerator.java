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

import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.component.UIComponent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 7/08/2008
 */
public class DataListGenerator {

    private DynFormFactory _factory;
    private SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd");


    public DataListGenerator() { }

    public DataListGenerator(DynFormFactory factory) {
        _factory = factory;
    }

    
    public String generate(PanelLayout panel, List<DynFormField> fieldList) {
        return generateDataList(panel, fieldList) ;
    }

    
    private String generateDataList(PanelLayout panel, List<DynFormField> fieldList) {
        StringBuilder result = new StringBuilder() ;
        List children = panel.getChildren();
        String parentTag = "";
        int start = 1;              // by default ignore first child (static text header)
        int stop = children.size();                 // by default process all components

        // a simpletype choice outer container has a radio button as first child
        Object o = children.get(0);
        if (o instanceof RadioButton) {
            SelectedChoiceBounds.calcBounds(children);
            start = SelectedChoiceBounds.start;
            stop = SelectedChoiceBounds.stop;
        }
        else {

            // a subpanel child of a choice also has no header
            if (! headedPanel(panel)) {
                start = 0;
            }
            else {
                // if this is the outermost panel, its title may be user defined, so get
                // the default form name
                if (! (panel instanceof SubPanel)) {
                    parentTag = _factory.getDefaultFormName();
                }
                else {
                    // otherwise the first child is the panel heading (and thus the element name)
                    parentTag = _factory.despace((String) ((StaticText) children.get(0)).getValue()) ;
                }
                result.append("<").append(parentTag).append(">") ;
            }    
        }

        for (int i = start; i < stop; i++) {
            UIComponent child = (UIComponent) children.get(i) ;

            // if subpanel, build inner output recursively
            if (child instanceof SubPanel) {
                DynFormField field = getField(child, fieldList);
                result.append(generateDataList((PanelLayout) child,
                                 field.getSubFieldList())) ;
            }

            // if a complextype choice, then deal with it
            else if (child instanceof RadioButton) {
                SelectedChoiceBounds.calcBounds(children);
                stop = SelectedChoiceBounds.stop;
            }

            // each label is a reference to an input field
            else if (child instanceof Label) {
                DynFormField field = getField(child, fieldList);
                result.append(getFieldValue(panel, (Label) child, field)) ;
            }
        }

        // close the xml and return
        if (parentTag.length() > 0) result.append("</").append(parentTag).append(">") ;
        return result.toString();
    }


    private String getFieldValue(PanelLayout panel, Label label, DynFormField field) {

        // get the component this label is 'for', then get its value
        String forID = label.getFor();
        String value = "";
        UIComponent component = panel.findComponent(forID);
        if (component instanceof TextField)
            value = JDOMUtil.encodeEscapes((String) ((TextField) component).getValue());
        else if (component instanceof Checkbox)
           value =  ((Checkbox) component).getValue().toString();
        else if (component instanceof Calendar) {
            Date date = ((Calendar) component).getSelectedDate();
            value = (date != null) ? _sdf.format(date) : null;
        }
        else if (component instanceof DropDown)
            value = (String) ((DropDown) component).getSelected();
        else if (component instanceof TextArea)
            value = JDOMUtil.encodeEscapes((String) ((TextArea) component).getValue());
        else if (component instanceof FieldBase)
            value = (String) ((FieldBase) component).getText();    // default fallthrough

        return formatField(value, field);
    }


    private String formatField(String value, DynFormField field) {

        // if no value & minOccurs=0 then don't output anything
        if (((value == null) || (value.length() == 0)) && field.hasZeroMinimum()) {
            return "";
        }

        if (field.hasBlackoutAttribute()) value = field.getValue();
        return StringUtil.wrap(value, field.getName());
    }


    private boolean headedPanel(PanelLayout panel) {
        List children = panel.getChildren();
        return (children != null) && (! children.isEmpty())
                && (children.get(0) instanceof StaticText);
    }


    private DynFormField getField(UIComponent component, List<DynFormField> fieldList) {
        String id;
        if (component instanceof SubPanel) {
            id = ((SubPanel) component).getName();
            for (DynFormField field : fieldList) {
                if (field.getName().equals(id))
                    return field;
            }
        }
        else {
            id = (String) ((Label) component).getText();
            id = id.replaceAll(":", "").trim();
            for (DynFormField field : fieldList) {
                if (field.getLabelText().equals(id))
                    return field;
            }
        }
        return null;
    }


    /******************************************************************************/

    /**
     * Calculates and holds the bounds of the selected field(s) within a choice panel 
     */
    static class SelectedChoiceBounds {
        static int start = -1;
        static int stop = -1;

        /**
         * Works through a choice panel's components, finding the selected radio button
         * then marking the bounds of it and the last component of the selection.
         * @param children the panel's child components
         */
        static void calcBounds(List children) {
            start = -1;
            stop = -1;
            int i = 0;
            do {
                Object o = children.get(i);
                if (o instanceof RadioButton) {
                    RadioButton rb = (RadioButton) o;
                    if (rb.isChecked()) {           // found the selected radio
                        start = i + 1;              // so start at next component
                    }
                    else {                          // else if radio unselected
                        if (start > -1) {           // and selected one previously found
                            stop = i ;              // then end one before next radio
                        }
                    }
                }
                i++;

            } while ((stop == -1) && (i < children.size()));

            // if no later radio found, upper bound is last component in the list
            if (stop == -1) stop = children.size();
        }

    }

}
