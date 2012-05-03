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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 9/03/2008
 */
public class ResourceMarshaller {

    /** Constructor */
    public ResourceMarshaller() { }


    /**
     * Converts the string passed to a JDOM Element and returns its child Elements
     * @param s the xml string to be converted
     * @return a list of child elements of the converted element passed
     */
    private List<Element> getChildren(String s) {
        if ((s == null) || (! s.trim().startsWith("<"))) return Collections.emptyList();
        return JDOMUtil.stringToElement(s).getChildren();
    }

    /******************************************************************************/

    // PARTICIPANTS //

    public String marshallParticipants(Set<Participant> set) {
        StringBuilder xml = new StringBuilder("<participants>") ;
        if (set != null) for (Participant p : set) xml.append(p.toXML()) ;
        xml.append("</participants>");
        return xml.toString() ;
    }


    public Set<Participant> unmarshallParticipants(String xml) {
        Set<Participant> result = new HashSet<Participant>();

        // each child is one Participant (as xml)
        for (Element ePart : getChildren(xml)) {
            result.add(unmarshallParticipant(ePart));
        }
        if (result.isEmpty()) return null;
        return result ;
    }


    public Participant unmarshallParticipant(Element e) {
        Participant p = new Participant();

        // repopulate the members from its xml
        p.reconstitute(e);
        Element roles = e.getChild("roles");
        if (roles != null) {
            for (Element eRole : roles.getChildren()) {
                p.addRole(new Role(eRole));
            }
        }
        Element positions = e.getChild("positions");
        if (positions != null) {
            for (Element ePos : positions.getChildren()) {
                p.addPosition(new Position(ePos));
            }
        }
        Element capabilities = e.getChild("capabilities");
        if (capabilities != null) {
            for (Element eCap : capabilities.getChildren()) {
                p.addCapability(new Capability(eCap));
            }
        }
        return p;
    }

    public Participant unmarshallParticipant(String xml) {
        return unmarshallParticipant(JDOMUtil.stringToElement(xml));
    }


    /******************************************************************************/

    // WORKITEMRECORDS //

    public String marshallWorkItemRecords(Set<WorkItemRecord> set) {
        StringBuilder xml = new StringBuilder("<workitemrecords>") ;
        if (set != null) for (WorkItemRecord wir : set) xml.append(wir.toXML()) ;
        xml.append("</workitemrecords>");
        return xml.toString() ;
    }

    public String marshallWorkItemRecords(List<WorkItemRecord> list) {
        return marshallWorkItemRecords(new HashSet<WorkItemRecord>(list));
    }


    public Set<WorkItemRecord> unmarshallWorkItemRecords(String xml) {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();

        // each child is one WorkItemRecord (as xml)
        for (Element e : getChildren(xml)) {
            result.add(Marshaller.unmarshalWorkItem(e));
        }
        if (result.isEmpty()) return null;
        return result ;
    }


    public WorkItemRecord unmarshallWorkItemRecord(String xml) throws IOException {
        return Marshaller.unmarshalWorkItem(xml);
    }


    /******************************************************************************/

    // SPECIFICATIONDATA //

    public String marshallSpecificationDataSet(Set<SpecificationData> set) {
        StringBuilder xml = new StringBuilder("<specificationdataset>") ;
        if (set != null) {
            for (SpecificationData specData : set) {
                xml.append(marshallSpecificationData(specData));
            }
        }
        xml.append("</specificationdataset>");
        return xml.toString() ;
    }

    public String marshallSpecificationData(SpecificationData specData) {
        StringBuilder xml = new StringBuilder("<specificationData>") ;
        xml.append(StringUtil.wrap(specData.getID().getIdentifier(), "id"));
        xml.append(StringUtil.wrap(specData.getID().getUri(), "uri"));

            if (specData.getName() != null) {
                xml.append(StringUtil.wrap(specData.getName(), "name"));
            }
            if (specData.getDocumentation() != null) {
                xml.append(StringUtil.wrap(specData.getDocumentation(), "documentation"));
            }

            Iterator inputParams = specData.getInputParams().iterator();
            if (inputParams.hasNext()) {
                xml.append("<params>");
                while (inputParams.hasNext()) {
                    YParameter inputParam = (YParameter) inputParams.next();
                    xml.append(inputParam.toSummaryXML());
                }
                xml.append("</params>");
            }
            xml.append(StringUtil.wrap(specData.getRootNetID(), "rootNetID"));
            xml.append(StringUtil.wrap(specData.getSchemaVersion().toString(), "version"));
            xml.append(StringUtil.wrap(specData.getSpecVersion(), "specversion"));
            xml.append(StringUtil.wrap(specData.getStatus(), "status"));

            String metaTitle = specData.getMetaTitle();
            if (metaTitle != null) xml.append(StringUtil.wrap(metaTitle, "metaTitle"));

            String authors = specData.getAuthors();
            if (authors != null) {
                xml.append("<authors>");
                for (String author : authors.split(",")) {
                    xml.append(StringUtil.wrap(author.trim(), "author"));
                }
                xml.append("</authors>");
            }
            String gateway = specData.getExternalDataGateway();
            if (gateway != null) {
                xml.append(StringUtil.wrap(gateway, "externalDataGateway"));
            }
        xml.append("</specificationData>");
        return xml.toString() ;
    }


    public Set<SpecificationData> unmarshallSpecificationDataSet(String xml)
                                                                   throws IOException {
        Set<SpecificationData> result = new HashSet<SpecificationData>();

        List specDataSet = Marshaller.unmarshalSpecificationSummary(xml) ;
        for (int i = 0; i < specDataSet.size(); i++)
             result.add((SpecificationData) specDataSet.get(i));

        if (result.isEmpty()) return null;
        return result ;
    }                                                                            


    public SpecificationData unmarshallSpecificationData(String xml) {
        SpecificationData result = null;
        YSpecificationID specID = null;
        if (xml != null) {
            Element specElement = JDOMUtil.stringToElement(xml);
            String id = specElement.getChildText("id");
            String uri = specElement.getChildText("uri");
            String name = specElement.getChildText("name");
            String doco = specElement.getChildText("documentation");
            String status = specElement.getChildText("status");
            String version = specElement.getChildText("version");
            String rootNetID = specElement.getChildText("rootNetID");
            String specVersion = specElement.getChildText("specversion");
            String dataGateway = specElement.getChildText("externalDataGateway");
            if (id != null && status != null) {
                specID = new YSpecificationID(id, specVersion, uri);
                YSchemaVersion schemaVersion = YSchemaVersion.fromString(version);
                result = new SpecificationData(specID, name, doco, status, schemaVersion);
                result.setRootNetID(rootNetID);
                result.setSpecVersion(specVersion);
                result.setExternalDataGateway(dataGateway);
                Element inputParams = specElement.getChild("params");
                if (inputParams != null) {
                    for (Element paramElem : inputParams.getChildren()) {
                        YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
                        YDecompositionParser.parseParameter( paramElem, param, null,false);
                        result.addInputParam(param);
                    }
                }
                result.setMetaTitle(specElement.getChildText("metaTitle"));
                Element authors = specElement.getChild("authors");
                if (authors != null) {
                    for (Element authorElem : authors.getChildren()) {
                        result.addAuthor(authorElem.getText());
                    }
                }

            }
        }
        return result;
    }


    /*******************************************************************************/

    // YAWL SERVICES //

    public Set<YAWLServiceReference> unmarshallServices(String xml) {
        Set<YAWLServiceReference> result = new HashSet<YAWLServiceReference>();
        for (Element eService : getChildren(xml)) {
            String eString = JDOMUtil.elementToString(eService);
            YAWLServiceReference service = YAWLServiceReference.unmarshal(eString);
            result.add(service);
        }
        if (result.isEmpty()) return null;
        return result ;
    }


    public Set<YParameter> parseWorkItemParams(String paramStr) {
        Set<YParameter> result = new HashSet<YParameter>();

        Element params = JDOMUtil.stringToElement(paramStr);
        if (params != null) {
            for (Element paramElem : params.getChildren()) {
                if ("formalInputParam".equals(paramElem.getName())) {
                    continue;
                }
                YParameter param = new YParameter(null, paramElem.getName());
                YDecompositionParser.parseParameter(paramElem, param, null, false);
                result.add(param);
            }
        }
        return result;
    }

}
