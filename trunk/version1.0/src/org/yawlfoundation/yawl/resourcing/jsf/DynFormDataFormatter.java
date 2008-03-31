/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.*;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.resourcing.util.OneToManyStringList;

import javax.faces.component.UIComponent;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Packages the set of input data on a dynamic form to allow for systematic feeding
 * of data to translate it to the xml expected by the engine on checkin
 *
 * Author: Michael Adams
 * Date: 23/02/2008
 */

public class DynFormDataFormatter {

    private String _header;                                      // the outermost tag
    private List<OneToManyStringList> _attributes ;              // set of attrib-value
    private Set<SubPanelController> _controllerSet;              // set of nested panels

    // CONSTRUCTOR //
    public DynFormDataFormatter(PanelLayout form) {
        _attributes = new ArrayList<OneToManyStringList>() ;
        _controllerSet = new HashSet<SubPanelController>();
        deconstructComponentList(form) ;
    }

    /**
     * Called by the constructor to organise the form's data
     * @param form the outermost panel of the dynamic form
     */
    private void deconstructComponentList(PanelLayout form) {
        List components = form.getChildren();

        // for each component of the dynamic form
        for (Object o : components) {

            // the StaticText header becomes this panel's 'tag'
            if (o instanceof StaticText)
                _header = (String) ((StaticText) o).getText();

            // each subpanel contains its own components. Panel controllers are
            // stored to save confusion between cloned panels and non-cloned ones
            else if (o instanceof SubPanel) {
                SubPanelController controller = ((SubPanel) o).getController() ;
                _controllerSet.add(controller);
            }

            // ordinary fields - all have an associated label
            else if (o instanceof Label) {
                Label label = (Label) o ;
                String tag = (String) label.getText();
                tag = tag.trim().replaceFirst(":", "");               // remove prompt

                // get the component this label is 'for', then get its value
                String forID = label.getFor();
                String value = "";
                UIComponent field = form.findComponent(forID);
                if (field instanceof TextField)
                    value = (String) ((TextField) field).getValue();
                else if (field instanceof Checkbox)
                    value =  ((Checkbox) field).getValue().toString();
                else if (field instanceof Calendar)
                    value = new SimpleDateFormat("yyyy-MM-dd")
                                     .format(((Calendar) field).getSelectedDate());
                addAttribute(tag, value) ;
            }
        }
    }


    /**
     * Add a value to an associated name (a name may have several values)
     * @param tag the 'name' of the component
     * @param value its value (as read from the dyn form)
     */
    private void addAttribute(String tag, String value) {
        for (OneToManyStringList attribute : _attributes) {

            // if this tag already mapped, add another value
            if (attribute.getTag().equals(tag)) {
                attribute.add(value) ;
                return ;
            }
        }

       // else add new tag to the list
       _attributes.add(new OneToManyStringList(tag, value));
    }


    /** @return an opening XML'd tag that will enclose this form's data */
    public String getHeaderOpen() { return "<" + _header + ">" ; }


    /** @return a closing XML'd tag for this form's data */
    public String getHeaderClose() { return "</" + _header + ">" ; }


    /** @return the attribute-value sets as XML */
    public String getBody() {
        StringBuilder result = new StringBuilder();
        for (OneToManyStringList attribute : _attributes)
            result.append(attribute.toXML()) ;
        return result.toString();
    }


    /** @return the controllers of the subpanels on this form */
    public Set<SubPanelController> getSubPanelControllers() {
        return _controllerSet;
    }


    /**
     *  Reformats an xml string, removing inner duplicates of the outer tag
     * @param tag the outer tag name
     * @param toClean the String to clean
     * @return the cleaned String
     */
    public String cleanPanelSetOutput(String tag, String toClean) {
        Element output = new Element(tag);

        // wrap string to allow a parent element to be created
        toClean = StringUtil.wrap(toClean, "temp");
        Element input = JDOMUtil.stringToElement(toClean);

        // split the original string into child elements
        Iterator itr = input.getChildren().iterator();
        while (itr.hasNext()) {
            Element child = (Element) itr.next();
            output.addContent(child.cloneContent());               // add its content
        }
        return JDOMUtil.elementToStringDump(output);
    }

    
    /******************************************************************************/

}
