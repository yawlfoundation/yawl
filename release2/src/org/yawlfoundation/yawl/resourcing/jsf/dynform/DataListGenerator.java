/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.*;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.component.UIComponent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 7/08/2008
 */
public class DataListGenerator {

    private DynFormFactory _factory;

    public DataListGenerator() { }

    public DataListGenerator(DynFormFactory factory) {
        _factory = factory;
    }

    public String generate(PanelLayout panel) {
        return normaliseDataList(generateDataList(panel)) ;
    }

    
    private String generateDataList(PanelLayout panel) {
        StringBuilder result = new StringBuilder() ;
        List children = panel.getChildren();
        String parentTag = "";
        int start = 1;              // by default ignore first child (static text header)
        int stop = children.size();               // by default process all components

        // a simpletype choice outer container has a radio button as first child
        Object o = children.get(0);
        if (o instanceof RadioButton) {
            SelectedChoiceBounds.calcBounds(children);
            start = SelectedChoiceBounds.start;
            stop = SelectedChoiceBounds.stop;
        }
        else {
            if ((panel instanceof SubPanel) && ((SubPanel) panel).isChoicePanel()) {
                start = 0;
            }
            else {
                // otherwise the first child is the panel heading (and thus the element name)
                parentTag = _factory.despace((String) ((StaticText) children.get(0)).getValue()) ;
                result.append("<").append(parentTag).append(">") ;
            }    
        }

        for (int i = start; i < stop; i++) {
            UIComponent child = (UIComponent) children.get(i) ;

            // if subpanel, build inner output recursively
            if (child instanceof SubPanel)
                result.append(generateDataList((PanelLayout) child)) ;

            // if a complextype choice, then deal with it
            else if (child instanceof RadioButton) {
                System.out.println("complex choice");
            }

            // each label is a reference to an input field
            else if (child instanceof Label)
                result.append(getFieldValue(panel, (Label) child)) ;
        }

        // close the xml and return
        if (parentTag.length() > 0) result.append("</").append(parentTag).append(">") ;
        return result.toString();
    }


    private String getFieldValue(PanelLayout panel, Label label) {
        String tag = (String) label.getText();
        tag = tag.trim().replaceFirst(":", "");               // remove prompt

        // get the component this label is 'for', then get its value
        String forID = label.getFor();
        String value = "";
        UIComponent field = panel.findComponent(forID);
        if (field instanceof TextField)
            value = JDOMUtil.encodeEscapes((String) ((TextField) field).getValue());
        else if (field instanceof Checkbox)
           value =  ((Checkbox) field).getValue().toString();
        else if (field instanceof Calendar)
            value = new SimpleDateFormat("yyyy-MM-dd")
                                 .format(((Calendar) field).getSelectedDate());
        else if (field instanceof DropDown)
            value = (String) ((DropDown) field).getSelected();

        return StringUtil.wrap(value, tag);

    }


    private String normaliseDataList(String dataStr) {
        if ((dataStr == null) || (dataStr.length() == 0)) return dataStr ;

        Element data = JDOMUtil.stringToElement(dataStr);
        return JDOMUtil.elementToStringDump(normaliseDataElement(data));
    }


    /**
     * Collects child elements of the same name at the same heirachy and consolidates
     * their contents into a single child element
     * @param data the data element to normalise
     * @return the normalised data element
     */
    private Element normaliseDataElement(Element data) {
        List<String> processedNames = new ArrayList<String>();
        Element result = new Element(data.getName());
        List children = data.getChildren();

        // get the child elements (if any)
        if (children.size() > 0) {
            for (Object objChild : children) {
                String name = ((Element) objChild).getName();
                if (! processedNames.contains(name)) {            // each name once only

                    // get all child elements with matching name
                    List matches = data.getContent(new ElementFilter(name));
                    Element subResult = new Element(name);
                    for (Object match : matches) {

                        // recurse for lower level content
                        Element recursedElem = normaliseDataElement((Element) match);
                        subResult.addContent(recursedElem.cloneContent()) ;
                    }
                    processedNames.add(name);
                    result.addContent(subResult);
                }
            }
        }
        else result.setText(data.getText());                         // recursion end

        return result;
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
