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
public class DynFormFieldAssembler {

    private String _formName = "";
    private Map<String, FormParameter> _params ;                // top level I/O params
    private FormParameter _currentParam;
    private List<DynFormField> _fieldList;
    private int _uniqueSuffix = 0;


    public DynFormFieldAssembler() { }

    public DynFormFieldAssembler(String schemaStr, String dataStr,
                           Map<String, FormParameter> params) {
        _params = params;
        buildMap(schemaStr, dataStr);
    }

    private void buildMap(String schemaStr, String dataStr) {
        Element data = JDOMUtil.stringToElement(dataStr);
        Document doc = JDOMUtil.stringToDocument(schemaStr);
        Element root = doc.getRootElement();                        // schema
        Namespace ns = root.getNamespace();
        Element element = root.getChild("element", ns) ;
        _formName = element.getAttributeValue("name") ;            // name of case/task
        _fieldList = createFieldList(element, data, ns, -1);
    }


    private List<DynFormField> createFieldList(Element schema, Element data,
                                               Namespace ns, int level) {
        List<DynFormField> fieldList;
        Element sequence;

        // increment nested depth level & get sequence contents
        ++level ;

        if (schema.getName().equals("choice")) {
            fieldList = createChoice(schema, data, ns, level);
        }
        else {
            Element complex = schema.getChild("complexType", ns);
            sequence = complex.getChild("sequence", ns);
            fieldList = createSequence(sequence, data, ns, level);
        }

        return fieldList;
    }


    private List<DynFormField> createSequence(Element sequence, Element data,
                                              Namespace ns, int level) {

        List<DynFormField> fieldList = new ArrayList<DynFormField>();

        List content = sequence.getChildren() ;
        Iterator itr = content.iterator();
        while (itr.hasNext()) {
            Element eField = (Element) itr.next();
            List<DynFormField> result = createField(eField, data, ns, level);
            setOrderForListItems(result, fieldList.size());
            fieldList.addAll(result);
        }
        return fieldList;
    }


    private List<DynFormField> createChoice(Element parent, Element data,
                                              Namespace ns, int level) {
        List<DynFormField> fieldList = new ArrayList<DynFormField>();
        List<DynFormField> result;
        String choiceID = getNextChoiceID();


        List content = parent.getChildren() ;
        Iterator itr = content.iterator();
        while (itr.hasNext()) {
            Element eField = (Element) itr.next();
            if (eField.getName().equals("sequence")) {
                List<DynFormField> subList = createSequence(eField, data, ns, level + 1);
                DynFormField field = addField("choicePanel", subList, null, null, level);
                field.setGroupID(getNextGroupID());
                fieldList.add(field);
            }
            else {
                result = createField(eField, data, ns, level);
                fieldList.addAll(result);
            }
        }

        for (DynFormField field : fieldList) {
            field.setChoiceID(choiceID);
        }
        return fieldList;
    }


    /**
     *
     * @param eField
     * @param data
     * @param ns
     * @param level
     * @return a list of 'DynFormField'
     */
    private List<DynFormField> createField(Element eField, Element data,
                                           Namespace ns, int level) {
        DynFormField field = null;
        List<DynFormField> result = new ArrayList<DynFormField>() ;

        // get eField element's attributes
        String minOccurs = eField.getAttributeValue("minOccurs");
        String maxOccurs = eField.getAttributeValue("maxOccurs");
        String name = eField.getAttributeValue("name");
        String type = eField.getAttributeValue("type");

        if (level == 0) _currentParam = _params.get(name);

        if (type == null) {

            // check for a simpleType definition
            Element simple = eField.getChild("simpleType", ns);
            if (simple != null) {
                Element restriction = simple.getChild("restriction", ns);
                if (restriction != null) {
                    String baseType = restriction.getAttributeValue("base");
                    List<String> enumMembers = getEnumeratedValues(restriction, ns);
                    field = addField(name, baseType, data, minOccurs, maxOccurs, level);
                    field.setEnumeratedValues(enumMembers);
                }
                Element union = simple.getChild("union", ns);
                if (union != null) {
                    field = addField("union", "xsd:string", null, "1", "1", level);
                }
            }
            else {
                // new complex type - recurse in a new field list
                Element subData = (data != null) ? data.getChild(name) : null;
                String groupID = getNextGroupID();
                int instances = getInitialInstanceCount(minOccurs, subData) ;
                for (int i = 0; i < instances; i++) {
                    Element data4Inst = (instances > 1) ?
                                         getIteratedContent(subData, i) : subData ;
                    field = addField(name, createFieldList(eField, data4Inst, ns, level),
                                     minOccurs, maxOccurs, level);
                    field.setOccursCount(instances);
                    field.setGroupID(groupID);
                    result.add(field);
                }
            }
        }
        else  {
            // a plain element
            field = addField(name, type, data, minOccurs, maxOccurs, level);
        }

        if (result.isEmpty()) result.add(field);

        return result;
    }


    private List<String> getEnumeratedValues(Element restriction, Namespace ns) {
        List<String> enumMembers = new ArrayList<String>();
        if (restriction != null) {
            List enumChildren = restriction.getChildren("enumeration", ns);
            if (enumChildren != null) {
                for (int i = 0; i < enumChildren.size(); i++) {
                    Element enumChild = (Element) enumChildren.get(i);
                    enumMembers.add(enumChild.getAttributeValue("value"));
                }
            }
        }
        return enumMembers;
    }


    private DynFormField addField(String name, String type, Element data,
                                  String minOccurs, String maxOccurs, int level) {
        String value = (data != null) ? data.getChildText(name) : "";
        DynFormField input = new DynFormField(name, type, value);
        input.setMinoccurs(minOccurs);
        input.setMaxoccurs(maxOccurs);
        input.setLevel(level);
        input.setRequired();
        input.setParam(_currentParam);
        return input;
    }


    private DynFormField addField(String name, List<DynFormField> subFieldList,
                                  String minOccurs, String maxOccurs,int level) {
        if (name == null) name = "choice";
        DynFormField input = new DynFormField(name, subFieldList);
        input.setMinoccurs(minOccurs);
        input.setMaxoccurs(maxOccurs);
        input.setParam(_currentParam);
        input.setLevel(level);
        return input;
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


    private void setOrderForListItems(List<DynFormField> list, int order) {
        for (DynFormField field : list) {
            field.setOrder(order++);
        }
    }


    private String getNextGroupID() {
        return "group" + String.valueOf(_uniqueSuffix++);
    }
    
    private String getNextChoiceID() {
        return "choice" + String.valueOf(_uniqueSuffix++);
    }



    public String getFormName() { return _formName ; }

    public List<DynFormField> getFieldList() { return _fieldList; }
}
