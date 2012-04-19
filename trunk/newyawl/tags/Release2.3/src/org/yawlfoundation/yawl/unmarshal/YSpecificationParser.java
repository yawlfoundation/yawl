/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.unmarshal;

import org.eclipse.xsd.util.XSDConstants;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.text.ParseException;
import java.util.*;


/**
 * Parses, or builds, specification objects from XML doclets.
 * 
 * @author Lachlan Aldred
 * 

 */
class YSpecificationParser {
    private YSpecification _specification;
    private YDecompositionParser[] _decompositionParser;
    private Map<String, String> _decompAndTypeMap = new HashMap<String, String>();
    private Namespace _yawlNS;
    private List<String> _emptyComplexTypeFlagTypes = new ArrayList<String>();

    private static final String _schema4SchemaURI = XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001;
    private static final String _defaultSchema =
                           "<xs:schema xmlns:xs=\"" + _schema4SchemaURI + "\"/>";

    static final String INITIAL_VERSION = "0.1";              // initial spec version

    /**
     * build a specification object from part of an XML document
     * @param specificationElem the specification part of an XMl document
     * @param version the version of the XML representation (i.e. beta2 or beta3).
     * @throws YSyntaxException
     */
    public YSpecificationParser(Element specificationElem, YSchemaVersion version)
            throws YSyntaxException {
        _yawlNS = specificationElem.getNamespace();

        parseSpecification(specificationElem, version);
        linkDecompositions();
    }


    private void parseSpecification(Element specificationElem, YSchemaVersion version)
            throws YSyntaxException {
        List decompositionElems = specificationElem.getChildren("decomposition", _yawlNS);
        for (int i = 0; i < decompositionElems.size(); i++) {
            Element decompositionElem = (Element) decompositionElems.get(i);
            Namespace xsiNameSpc = decompositionElem.getNamespace("xsi");
            String decompID = decompositionElem.getAttributeValue("id");
            Attribute type = decompositionElem.getAttribute("type", xsiNameSpc);
            if (type != null) {
                String decompType = type.getValue();
                _decompAndTypeMap.put(decompID, decompType);
            }
        }
        String uriString = specificationElem.getAttributeValue("uri");
        _specification = new YSpecification(uriString);
        _specification.setVersion(version);
        _specification.setMetaData(parseMetaData(specificationElem));
        String name = specificationElem.getChildText("name", _yawlNS);
        String documentation = specificationElem.getChildText("documentation", _yawlNS);

        Namespace schema4SchemaNS = Namespace.getNamespace(_schema4SchemaURI);
        Element schemaElem = specificationElem.getChild("schema", schema4SchemaNS);
        if (null != schemaElem) {
            extractEmptyComplexTypeFlagTypeNames(schemaElem);
            _specification.setSchema(JDOMUtil.elementToString(schemaElem));
        }
        else {

            // if the spec has no schema definition insert a default one so that a
            // DataValidator gets created
            _specification.setSchema(_defaultSchema);
        }

        // if name and doco fields missing from spec, see if they are in metadata
        if (name == null)
            name = _specification.getMetaData().getTitle();
        if (documentation == null)
            documentation = _specification.getMetaData().getDescription();

        _specification.setName(name);
        _specification.setDocumentation(documentation);

        //If is version beta2 we loop through the decompositions
        //in a slightly different way.  Rather than be tricky i think it is easier to just copy the
        //code with minor changes.
        if (isBeta2Version()) {
            _decompositionParser = new YDecompositionParser[decompositionElems.size() + 1];
            Element rootNetElem = specificationElem.getChild("rootNet", _yawlNS);
            _decompositionParser[0] = new YDecompositionParser(
                    rootNetElem,
                    this,
                    _specification.getSchemaVersion());
            YNet rootNet = (YNet) _decompositionParser[0].getDecomposition();
            _specification.setRootNet(rootNet);

            for (int i = 1; i <= decompositionElems.size(); i++) {
                Element decompositionElem = (Element) decompositionElems.get(i - 1);
                _decompositionParser[i] = new YDecompositionParser(
                        decompositionElem,
                        this,
                        _specification.getSchemaVersion());
                YDecomposition decomposition = _decompositionParser[i].getDecomposition();
                _specification.setDecomposition(decomposition);
            }
        }
        else {//must be beta3 or greater
            _decompositionParser = new YDecompositionParser[decompositionElems.size()];
            for (int i = 0; i < decompositionElems.size(); i++) {
                Element decompositionElem = (Element) decompositionElems.get(i);
                _decompositionParser[i] = new YDecompositionParser(
                        decompositionElem,
                        this,
                        _specification.getSchemaVersion());
                YDecomposition decomposition = _decompositionParser[i].getDecomposition();
                _specification.setDecomposition(decomposition);
            }
        }
        addSchema(specificationElem);
    }

    YMetaData parseMetaData(Element specificationElem) {
        Element metaDataElem = specificationElem.getChild("metaData", _yawlNS);
        YMetaData metaData = new YMetaData();

        metaData.setTitle(metaDataElem.getChildText("title", _yawlNS));

        List creators = metaDataElem.getChildren("creator", _yawlNS);
        for (Object o : creators) {
            Element creatorElem = (Element) o;
            metaData.addCreator(creatorElem.getText());
        }

        List subjects = metaDataElem.getChildren("subject", _yawlNS);
        for (Object o : subjects) {
            Element subjectElem = (Element) o;
            metaData.addSubject(subjectElem.getText());
        }

        metaData.setDescription(metaDataElem.getChildText("description", _yawlNS));

        List contributors = metaDataElem.getChildren("contributor", _yawlNS);
        for (Object o : contributors) {
            Element contributor = (Element) o;
            metaData.addContributor(contributor.getText());
        }

        metaData.setCoverage(metaDataElem.getChildText("coverage", _yawlNS));

        String validFrom = metaDataElem.getChildText("validFrom", _yawlNS);
        if (validFrom != null) {
            try {
                metaData.setValidFrom(YMetaData.dateFormat.parse(validFrom));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String validUntil = metaDataElem.getChildText("validUntil", _yawlNS);
        if (validUntil != null) {
            try {
                metaData.setValidUntil(YMetaData.dateFormat.parse(validUntil));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String created = metaDataElem.getChildText("created", _yawlNS);
        if (created != null) {
            try {
                metaData.setCreated(YMetaData.dateFormat.parse(created));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String version = metaDataElem.getChildText("version", _yawlNS);
        if(version != null && version.trim().length() > 0)
        {
            metaData.setVersion(new YSpecVersion(version.trim()));
        }
        else metaData.setVersion(new YSpecVersion(INITIAL_VERSION));

        metaData.setStatus(metaDataElem.getChildText("status", _yawlNS));
        /**
         * AJH: Add in support for the persistent property. This is simply a property which indicates if
         *      a custom service processing workitems from this specification needs to perform any
         *      "persistence" activities.
         */
        {
            String persistentText = metaDataElem.getChildText("persistent", _yawlNS);
            if (persistentText == null)
            {
                metaData.setPersistent(false);
            }
            else
            {
                metaData.setPersistent(persistentText.trim().equalsIgnoreCase("TRUE"));
            }
        }

        String uniqueID = metaDataElem.getChildText("identifier", _yawlNS);
        if (uniqueID != null)
            metaData.setUniqueID(uniqueID);

        return metaData;
    }


    /**
     * @return whether this is a a beta 2 specification version or not.
     */
    private boolean isBeta2Version() {
        return _specification.getSchemaVersion().isBeta2();
    }    


    /**
     * adds the XML schema library to the specification.
     * @param specificationElem
     */
    private void addSchema(Element specificationElem) throws YSyntaxException {

        Element schemaElem = specificationElem.getChild("schema");
        if (schemaElem != null) {
            _specification.setSchema(JDOMUtil.elementToString(schemaElem));
        }
    }


    private void extractEmptyComplexTypeFlagTypeNames(Element schemaElem) {
        if (schemaElem != null) {
            for (Object o : schemaElem.getChildren()) {
                Element elem = (Element) o;
                if (elem.getName().equals("complexType") && elem.getChildren().isEmpty()) {
                    _emptyComplexTypeFlagTypes.add(elem.getAttributeValue("name"));
                }
            }
        }
    }

    protected List<String> getEmptyComplexTypeFlagTypeNames() {
        return _emptyComplexTypeFlagTypes;
    }

    private void linkDecompositions() {
        for (int i = 0; i < _decompositionParser.length; i++) {
            Map decomposesToIDs = _decompositionParser[i].getDecomposesToIDs();
            Iterator compTasksIter = decomposesToIDs.keySet().iterator();
            while (compTasksIter.hasNext()) {
                YTask task = (YTask) compTasksIter.next();
                String decompID = (String) decomposesToIDs.get(task);
                YDecomposition implementation = _specification.getDecomposition(decompID);
                task.setDecompositionPrototype(implementation);
            }
        }
    }


    /**
     * Method getSpecification.
     * @return YSpecification
     */
    public YSpecification getSpecification() {
        return _specification;
    }

    public String getDecompositionType(String decomposesToID) {
        return _decompAndTypeMap.get(decomposesToID);
    }


}

