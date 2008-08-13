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

        // first child is always the panel heading (and thus the element name)
        String parentTag = (String) ((StaticText) children.get(0)).getValue() ;
        result.append("<").append(_factory.despace(parentTag)).append(">") ;

        for (int i = 1; i < children.size(); i++) {
            UIComponent child = (UIComponent) children.get(i) ;

            // if subpanel, build inner output recursively
            if (child instanceof SubPanel)
                result.append(generate((PanelLayout) child)) ;

            // ordinary fields - all have an associated label
            else if (child instanceof Label) {
                Label label = (Label) child ;
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

                result.append(StringUtil.wrap(value, tag));
            }
        }

        // close the xml and return
        result.append("</").append(parentTag).append(">") ;
        return result.toString();
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

}
