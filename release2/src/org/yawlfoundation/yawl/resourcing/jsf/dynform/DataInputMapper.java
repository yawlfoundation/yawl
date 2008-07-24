package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 5/07/2008
 */
public class DataInputMapper extends ArrayList<DataInputElement> {

    private String _formName = "";
    private Map<String, FormParameter> _params ;                // top level I/O params

    public DataInputMapper() { }

    public DataInputMapper(String schemaStr, String dataStr,
                           Map<String, FormParameter> params) {
        _params = params;
        buildMap(schemaStr, dataStr);
    }

    public void buildMap(String schemaStr, String dataStr) {
        Element data = JDOMUtil.stringToElement(dataStr);
        Document doc = JDOMUtil.stringToDocument(schemaStr);
        Element root = doc.getRootElement();                        // schema
        Namespace ns = root.getNamespace();
        Element element = root.getChild("element", ns) ;
        _formName = element.getAttributeValue("name") ;            // name of case/task
        createDataInputElements(element, data, ns, -1);
    }


    private void createDataInputElements(Element schema, Element data,
                                         Namespace ns, int level) {

        // increment nested depth level & get sequence contents
        ++level ;
        Element complex = schema.getChild("complexType", ns);
        Element sequence = complex.getChild("sequence", ns);       // todo: other types
        List content = sequence.getChildren() ;
        Iterator itr = content.iterator();
        while (itr.hasNext()) {
            Element field = (Element) itr.next();
            createDataInputElement(field, data, ns, level);
        }
    }


    private void createDataInputElement(Element field, Element data,
                                        Namespace ns, int level) {

        // get field element's attributes
        String minOccurs = field.getAttributeValue("minOccurs");
        String maxOccurs = field.getAttributeValue("maxOccurs");
        String name = field.getAttributeValue("name");
        String type = field.getAttributeValue("type");

        if (type == null) {

            // new complex type - recurse in a new container
            Element subData = (data != null) ? data.getChild(name) : null;

            int instances = getInitialInstanceCount(minOccurs, subData) ;
            for (int i = 0; i < instances; i++) {
                Element data4Inst = (instances > 1) ? getIteratedContent(subData, i) : subData ;
                createDataInputElements(field, data4Inst, ns, level);
            }
        }
        else  {
            // an element, so init as DataInput
            String value = (data != null) ? data.getChildText(name) : "";
            DataInputElement input = new DataInputElement(name, type, value);
            input.setMinoccurs(minOccurs);
            input.setMaxoccurs(maxOccurs);
            input.setLevel(level);
            input.setOrder(this.size());
            input.setRequired();
            this.add(input);
        }
    }


    private int getInitialInstanceCount(String min, Element data) {
        int dataCount = 1;
        int minOccurs = Math.max(SubPanelController.convertOccurs(min), 1) ;
        if ((data != null) && (data.getContentSize() > 1)) {
            String dataName = ((Element) data.getContent(0)).getName();
            dataCount = data.getChildren(dataName).size();
        }
        return Math.max(minOccurs, dataCount) ;
    }


    private Element getIteratedContent(Element data, int index) {
        Element result = null ;
        if ((data != null) && (index < data.getContentSize())) {
            result = new Element(data.getName());
            Element iteratedContent = (Element) data.getContent(index);
            result.addContent((Element) iteratedContent.clone());
        }
        return result ;
    }
    


    public String getFormName() { return _formName ; }
}
