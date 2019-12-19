/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;
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


    private DynFormFieldAssembler() { }

    public DynFormFieldAssembler(String schemaStr, String dataStr,
                           Map<String, FormParameter> params) throws DynFormException {
        _params = params;
        buildMap(schemaStr, dataStr);
    }

    private void buildMap(String schemaStr, String dataStr) throws DynFormException {
        Element data = JDOMUtil.stringToElement(dataStr);
        Document doc = JDOMUtil.stringToDocument(schemaStr);
        Element root = doc.getRootElement();                        // schema
        Namespace ns = root.getNamespace();
        Element element = root.getChild("element", ns) ;
        _formName = element.getAttributeValue("name") ;            // name of case/task
        _fieldList = createFieldList(element, data, ns, -1);
    }


    private List<DynFormField> createFieldList(Element schema, Element data,
                                       Namespace ns, int level) throws DynFormException {
        List<DynFormField> fieldList = null;
        Element next;

        // increment nested depth level & get next contents
        ++level ;

        if (schema.getName().equals("choice")) {
            fieldList = createChoice(schema, data, ns, level);
        }
        else {
            ns = schema.getNamespace();
            Element complex = schema.getChild("complexType", ns);
            if (complex == null) {
                throw new DynFormException("Malformed data schema, at element: " +
                        JDOMUtil.elementToString(schema));
            }

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
                                      Namespace ns, int level) throws DynFormException {

        List<DynFormField> fieldList = new ArrayList<DynFormField>();

        for (Element eField : sequence.getChildren()) {
            List<DynFormField> result = createField(eField, data, ns, level);
            setOrderForListItems(result, fieldList.size());
            fieldList.addAll(result);
        }
        return fieldList;
    }


    private List<DynFormField> createChoice(Element parent, Element data,
                                      Namespace ns, int level) throws DynFormException {
        List<DynFormField> fieldList = new ArrayList<DynFormField>();
        List<DynFormField> result;
        String choiceID = getNextChoiceID();

        for (Element eField : parent.getChildren()) {
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
                                     Namespace ns, int level) throws DynFormException {
        DynFormField field;
        List<DynFormField> result = new ArrayList<DynFormField>() ;

        // get eField element's attributes
        String minOccurs = eField.getAttributeValue("minOccurs");
        String maxOccurs = eField.getAttributeValue("maxOccurs");
        String name = eField.getAttributeValue("name");
        String type = getElementDataType(eField, ns);

        if (level == 0) _currentParam = _params.get(name);

        if (type == null) {

            // check for a simpleType definition
            Element simple = eField.getChild("simpleType", ns);
            if (simple != null) {
                List<DynFormField> fieldList = addElementField(name, null, data,
                        minOccurs, maxOccurs, level);
                for (DynFormField aField : fieldList) {
                    applySimpleTypeFacets(simple, ns, aField);
                }
                result.addAll(fieldList);
            }
            else {
                // check for empty complex type (flag defn)
                Element complex = eField.getChild("complexType", ns);
                if ((complex != null) && complex.getContentSize() == 0) {
                    field = addField(name, null, data, minOccurs, maxOccurs, level);
                    field.setEmptyComplexTypeFlag(true);
                    if ((data != null) && (data.getChild(name) != null)) {
                        field.setValue("true");
                    }
                    result.add(field);
                }
                else {
                    // new populated complex type - recurse in a new field list
                    String groupID = getNextGroupID();
                    List<Element> dataList = (data != null) ? data.getChildren(name) : null;
                    if (! (dataList == null || dataList.isEmpty())) {
                        for (Element var : dataList) {
                            field = addGroupField(name, eField, ns, var,
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
        }
        else  {
            // a plain element
            result.addAll(addElementField(name, type, data, minOccurs, maxOccurs, level));
        }

        return result;
    }

    private String getElementDataType(Element eField, Namespace ns) {
        String type = eField.getAttributeValue("type");
        if (type == null && eField.getChildren().isEmpty()) {
            type = ns.getPrefix() + ":string";            // default type for element
        }
        return type;
    }

    private DynFormField addGroupField(String name, Element eField, Namespace ns,
                                       Element data, String minOccurs, String maxOccurs,
                                       int level) throws DynFormException {
        DynFormField field ;
        List<DynFormField> simpleContents = new ArrayList<DynFormField>();
        int instances = getInitialInstanceCount(minOccurs, data, name) ;

        if (instances == 1) {
            field = addField(name, createFieldList(eField, data, ns, level),
                             minOccurs, maxOccurs, level);
        }
        else {
            field = addContainingField(name, minOccurs, maxOccurs, level);
            String subGroupID = getNextGroupID();
            for (int i = 0; i < instances; i++) {
                Element data4Inst = getIteratedContent(data, i, name) ;
                List<DynFormField> subFieldList =
                        createFieldList(eField, data4Inst, ns, level);
                DynFormField subField = subFieldList.get(0);      // the multi-inst field
                subField.setGroupID(subGroupID);
                field.addSubField(subField);
                simpleContents = addSimpleContents(simpleContents, subFieldList);
            }
        }
        field.setOccursCount(instances);
        field.addSubFieldList(simpleContents);
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
        int instances = getInitialInstanceCount(minOccurs, data, name) ;
        
        for (int i = 0; i < instances; i++) {
            Element data4Inst = (instances > 1) ? getIteratedContent(data, i, name) : data ;
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
        populateField(input, name, minOccurs, maxOccurs, level) ;
        return input;
    }


    private DynFormField addField(String name, List<DynFormField> subFieldList,
                                  String minOccurs, String maxOccurs,int level) {
        if (name == null) name = "choice";
        DynFormField input = new DynFormField(name, subFieldList);
        populateField(input, name, minOccurs, maxOccurs, level) ;
        return input;
    }


    private void populateField(DynFormField input, String name,
                                  String minOccurs, String maxOccurs,int level) {
        input.setMinoccurs(minOccurs);
        input.setMaxoccurs(maxOccurs);
        input.setParam(_currentParam);
        input.setLevel(level);

        DynFormUserAttributes attributes = new DynFormUserAttributes();
        if (level == 0) {
            attributes.set(getAttributeMap(name));
        }
        input.setAttributes(attributes);
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
    }


    private List<DynFormField> addSimpleContents(List<DynFormField> simpleContents,
                                                 List<DynFormField> subFieldList) {
        if (subFieldList == null) return simpleContents;

        boolean contains = false;

        for (int i=1; i<subFieldList.size(); i++) {
            DynFormField field = subFieldList.get(i);
            for (DynFormField simple : simpleContents) {
                contains = simple.equals(field) ;
                if (contains) break;
            }
            if (! contains) simpleContents.add(field);
        }
        return simpleContents;
    }


    private int getInitialInstanceCount(String min, Element data, String dataName) {
        int dataCount = 1;
        int minOccurs = Math.max(SubPanelController.convertOccurs(min), 1) ;
        if ((data != null) && (data.getContentSize() > 1)) {
            dataCount = data.getChildren(dataName).size();
        }
        return Math.max(minOccurs, dataCount) ;
    }


    private Element getIteratedContent(Element data, int index, String name) {
        Element result = null ;
        if ((data != null) && (index < data.getContentSize())) {
            List<Element> relevantChildren = data.getChildren(name);
            result = new Element(data.getName());
            Element iteratedContent = relevantChildren.get(index);
            result.addContent(iteratedContent.clone());
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
        return "group" + _uniqueSuffix++;
    }
    
    private String getNextChoiceID() {
        return "choice" + _uniqueSuffix++;
    }

    private YAttributeMap getAttributeMap(String name) {
        YAttributeMap map = new YAttributeMap();
        FormParameter param = _params.get(name);
        if (param != null) map.set(param.getAttributes());
        return map;
    }

    
    public String getFormName() { return _formName ; }

    public List<DynFormField> getFieldList() { return _fieldList; }
}
