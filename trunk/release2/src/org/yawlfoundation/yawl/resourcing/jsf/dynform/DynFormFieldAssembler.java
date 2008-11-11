/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

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
        List<DynFormField> fieldList = null;
        Element next;

        // increment nested depth level & get next contents
        ++level ;

        if (schema.getName().equals("choice")) {
            fieldList = createChoice(schema, data, ns, level);
        }
        else {
            Element complex = schema.getChild("complexType", ns);
            next = complex.getChild("sequence", ns);
            if (next == null) next = complex.getChild("all", ns);
            if (next != null) {
                fieldList = createSequence(next, data, ns, level);
            }
            else {
                next = complex.getChild("choice", ns);
                if (next != null) {
                    fieldList = createChoice(next, data, ns, level);
                }
            }
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
            String eName = eField.getName();
            if (eName.equals("sequence") || eName.equals("all")) {
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
                field = addField(name, null, data, minOccurs, maxOccurs, level);
                applySimpleTypeFacets(simple, ns, field);
                result.add(field);
            }
            else {
                // new complex type - recurse in a new field list
                String groupID = getNextGroupID();                
                List dataList = (data != null) ? data.getChildren(name) : null;
                if (dataList != null) {
                    for (Object o : dataList) {
                        field = addGroupField(name, eField, ns, (Element) o,
                                              minOccurs, maxOccurs, level);
                        field.setGroupID(groupID);
                        result.add(field);
                    }
                }
                else {
                    field = addGroupField(name, eField, ns, null,
                                          minOccurs, maxOccurs, level);
                    field.setGroupID(groupID);
                    result.add(field);
                }
            }
        }
        else  {
            // a plain element
            result.addAll(addElementField(name, type, data, minOccurs, maxOccurs, level));
        }

        return result;
    }


    private DynFormField addGroupField(String name, Element eField, Namespace ns,
                                       Element data, String minOccurs, String maxOccurs,
                                       int level) {
        DynFormField field ;
//        String groupID = getNextGroupID();
        int instances = getInitialInstanceCount(minOccurs, data) ;

        if (instances == 1) {
            field = addField(name, createFieldList(eField, data, ns, level),
                             minOccurs, maxOccurs, level);
        }
        else {
            field = addContainingField(name, minOccurs, maxOccurs, level);
            String subGroupID = getNextGroupID();
            for (int i = 0; i < instances; i++) {
                Element data4Inst = getIteratedContent(data, i) ;
                List<DynFormField> subFieldList =
                        createFieldList(eField, data4Inst, ns, level);
                DynFormField subField = subFieldList.get(0);
                subField.setGroupID(subGroupID);
                field.addSubField(subField);
            }
        }
 //       field.setGroupID(groupID);
        field.setOccursCount(instances);

        return field;
    }

    
    private List<DynFormField> addElementField(String name, String type,
                                               Element data, String minOccurs,
                                               String maxOccurs, int level) {
        List<DynFormField> result = new ArrayList<DynFormField>();
        DynFormField field ;
        String groupID = null;
        boolean cloneable = isCloneableField(minOccurs, maxOccurs);
        if (cloneable) groupID = getNextGroupID();
        int instances = getInitialInstanceCount(minOccurs, data) ;
        
        for (int i = 0; i < instances; i++) {
            Element data4Inst = (instances > 1) ? getIteratedContent(data, i) : data ;
            field = addField(name, type, data4Inst, minOccurs, maxOccurs, level);
            field.setGroupID(groupID);
            field.setOccursCount(instances);
            result.add(field);
        }
        return result;
    }


    private DynFormField addField(String name, String type, Element data,
                                  String minOccurs, String maxOccurs, int level) {
        String value = (data != null) ? data.getChildText(name) : "";
        DynFormField input = new DynFormField(name, type, value);
        input.setMinoccurs(minOccurs);
        input.setMaxoccurs(maxOccurs);
        input.setLevel(level);
        input.setParam(_currentParam);
        if (type != null) input.setRequired();
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


    private DynFormField addContainingField(String name, String minOccurs,
                                            String maxOccurs,int level) {
        if (name == null) name = "choice";
        DynFormField input = new DynFormField(name, null);
        input.setMinoccurs(minOccurs);
        input.setMaxoccurs(maxOccurs);
        input.setParam(_currentParam);
        input.setLevel(level);
        return input;
    }


    private void applySimpleTypeFacets(Element simple, Namespace ns,
                                                 DynFormField field) {
        Element restriction = simple.getChild("restriction", ns);
        if (restriction != null) {
            DynFormFieldRestriction restrict =
                    new DynFormFieldRestriction(restriction, ns);
            field.setDatatype(restrict.getBaseType());
            field.setRestriction(restrict);
        }

        Element union = simple.getChild("union", ns);
        if (union != null) {
            DynFormFieldUnion fieldUnion = new DynFormFieldUnion(union, ns);
            field.setDatatype(fieldUnion.getBaseType());
            field.setUnion(fieldUnion);
        }

        Element list = simple.getChild("list", ns);
        if (list != null) {
            DynFormFieldListFacet fieldList = new DynFormFieldListFacet(list);
            field.setDatatype(fieldList.getItemType());
            field.setListType(fieldList);
        }
        if (field.getDatatype() != null) field.setRequired();

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


    private boolean isCloneableField(String minoccurs, String maxoccurs) {
        int min = SubPanelController.convertOccurs(minoccurs);
        int max = SubPanelController.convertOccurs(maxoccurs);
        return (max > 1) && (max > min);
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
