package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.*;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.component.UIComponent;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 23/02/2008
 */
public class DynFormDataFormatter {

    private String _header;
    private Map<String, ComplexAttributes> _attributes ;
    private Set<SubPanelController> _subPanelSet;

    public DynFormDataFormatter(PanelLayout form) {
        _attributes = new Hashtable<String, ComplexAttributes>() ;
        _subPanelSet = new HashSet<SubPanelController>();
        deconstructComponentList(form) ;
    }

    private void deconstructComponentList(PanelLayout form) {
        List components = form.getChildren();
        for (Object o : components) {
            if (o instanceof StaticText)
                _header = (String) ((StaticText) o).getText();
            else if (o instanceof SubPanel) {
                SubPanelController controller = ((SubPanel) o).getController() ;
                _subPanelSet.add(controller);
            }
            else if (o instanceof Label) {
                Label label = (Label) o ;
                String tag = (String) label.getText();
                tag = tag.trim().replaceFirst(":", "");
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


    private void addAttribute(String tag, String value) {
        ComplexAttributes ca = _attributes.get(tag);
        if (ca != null)
            ca.addValue(value);
        else
           _attributes.put(tag, new ComplexAttributes(tag, value));
    }


    public String getHeaderOpen() { return "<" + _header + ">" ; }

    public String getHeaderClose() { return "</" + _header + ">" ; }


    public String getBody() {
        StringBuilder result = new StringBuilder();
        for (ComplexAttributes ca : _attributes.values())
            result.append(ca.toXML()) ;
        return result.toString();
    }


    public Set<SubPanelController> getSubPanels() {
        return _subPanelSet;
    }


    public String cleanPanelSetOutput(String tag, String toClean) {
        toClean = StringUtil.wrap(toClean, "temp");
        Element input = JDOMUtil.stringToElement(toClean);
        Element output = new Element(tag);
        Iterator itr = input.getChildren().iterator();
        while (itr.hasNext()) {
            Element child = (Element) itr.next();
//            Iterator grandItr = child.getChildren().iterator();
//            while (grandItr.hasNext()) {
//                Element grandChild = (Element) grandItr.next();
                output.addContent(child.cloneContent());
            }
//        }
        return JDOMUtil.elementToStringDump(output);
    }

    /******************************************************************************/

    private class ComplexAttributes {

        private String _tag ;
        private List<String> _values;

        ComplexAttributes(String tag, String value) {
            _tag = tag ;
            _values = new ArrayList<String>() ;
            addValue(value);
        }

        protected void addValue(String value) { _values.add(value); }

        protected String toXML() {
            StringBuilder result = new StringBuilder() ;
            for (String value : _values)
                result.append(StringUtil.wrap(value, _tag));
            return result.toString() ;
        }
    }

}
