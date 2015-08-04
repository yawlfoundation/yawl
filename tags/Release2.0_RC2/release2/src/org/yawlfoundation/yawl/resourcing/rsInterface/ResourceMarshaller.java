package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private List getChildren(String s) {
        if (s == null) return null;
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


    public Set<Participant> unmarshallParticipants(String xml) throws IOException {
        Set<Participant> result = new HashSet<Participant>();

        // each child is one Participant (as xml)
        List eList = getChildren(xml);
        if (eList != null) {
            Iterator itr = eList.iterator();
            while (itr.hasNext()) {
                Element e = (Element) itr.next();
                Participant p = new Participant();

                // repopulate the members from its xml
                p.reconstitute(e);
                result.add(p);
            }
        }
        if (result.isEmpty()) return null;
        return result ;
    }

    /******************************************************************************/

    // WORKITEMRECORDS //

    public String marshallWorkItemRecords(Set<WorkItemRecord> set) {
        StringBuilder xml = new StringBuilder("<workitemrecords>") ;
        if (set != null) for (WorkItemRecord wir : set) xml.append(wir.toXML()) ;
        xml.append("</workitemrecords>");
        return xml.toString() ;
    }


    public Set<WorkItemRecord> unmarshallWorkItemRecords(String xml) throws IOException {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();

        // each child is one WorkItemRecord (as xml)
        List eList = getChildren(xml);
        if (eList != null) {
            Iterator itr = eList.iterator();
            while (itr.hasNext()) {
                Element e = (Element) itr.next();
                result.add(Marshaller.unmarshalWorkItem(e));
            }
        }
        if (result.isEmpty()) return null;
        return result ;
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
        if (specData != null) {
            xml.append(StringUtil.wrap(specData.getID(), "id"));

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
            xml.append(StringUtil.wrap(specData.getSchemaVersion(),"version"));
            xml.append(StringUtil.wrap(specData.getSpecVersion(), "specversion"));
            xml.append(StringUtil.wrap(specData.getStatus(), "status"));
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
        if (xml != null) {
            Element specElement = JDOMUtil.stringToElement(xml);
            String id = specElement.getChildText("id");
            String name = specElement.getChildText("name");
            String doco = specElement.getChildText("documentation");
            String status = specElement.getChildText("status");
            String version = specElement.getChildText("version");
            String rootNetID = specElement.getChildText("rootNetID");
            String specVersion = specElement.getChildText("specversion");
            if (id != null && status != null) {
                result = new SpecificationData(id, name, doco, status, version);
                result.setRootNetID(rootNetID);
                result.setSpecVersion(specVersion);
                Element inputParams = specElement.getChild("params");
                if (inputParams != null) {
                    List paramElements = inputParams.getChildren();
                    for (int j = 0; j < paramElements.size(); j++) {
                        Element paramElem = (Element) paramElements.get(j);
                        YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
                        YDecompositionParser.parseParameter( paramElem, param, null,false);
                        result.addInputParam(param);
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
        List eList = getChildren(xml);
        if (eList != null) {
            Iterator itr = eList.iterator();
            while (itr.hasNext()) {
                Element eService = (Element) itr.next();
                String eString = JDOMUtil.elementToString(eService);
                YAWLServiceReference service = YAWLServiceReference.unmarshal(eString);
                result.add(service);
            }
        }
        if (result.isEmpty()) return null;
        return result ;
    }
}
