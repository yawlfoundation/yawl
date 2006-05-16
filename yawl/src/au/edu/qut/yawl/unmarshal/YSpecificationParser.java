/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.unmarshal;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Parses, or builds, specification objects from XML doclets.
 * 
 * @author Lachlan Aldred
 * 

 */
class YSpecificationParser {
    private YSpecification _specification;
    private YDecompositionParser[] _decompositionParser;
    private Map _decompAndTypeMap = new HashMap();
    private Namespace _yawlNS;


    /**
     * build a specification object from part of an XML document
     * @param specificationElem the specification part of an XMl document
     * @param version the version of the XML representation (i.e. beta2 or beta3).
     * @throws YSyntaxException
     * @throws YSchemaBuildingException
     */
    public YSpecificationParser(Element specificationElem, String version) throws YSyntaxException, YSchemaBuildingException {
        _yawlNS = specificationElem.getNamespace();

        parseSpecification(specificationElem, version);
        linkDecompositions();
    }


    private void parseSpecification(Element specificationElem, String version)
            throws YSyntaxException, YSchemaBuildingException {
        List decompositionElems = specificationElem.getChildren("decomposition", _yawlNS);
        for (int i = 0; i < decompositionElems.size(); i++) {
            Element decompositionElem = (Element) decompositionElems.get(i);
            Namespace xsiNameSpc = decompositionElem.getNamespace("xsi");
            String decompID = decompositionElem.getAttributeValue("id");//, _yawlNS);
            Attribute type = decompositionElem.getAttribute("type", xsiNameSpc);
            if (type != null) {
                //todo  fix it so when you load a spec with an empty decompostion JDOM doesn't
                //todo  throw a null pointer exception.
                String decompType = type.getValue();
                _decompAndTypeMap.put(decompID, decompType);
            }
        }
        String uriString = specificationElem.getAttributeValue("uri");
        _specification = new YSpecification(uriString);
        _specification.setBetaVersion(version);
        _specification.setMetaData(parseMetaData(specificationElem));
        String name = specificationElem.getChildText("name", _yawlNS);
        String documentation = specificationElem.getChildText("documentation", _yawlNS);

        Namespace schema4SchemaNS = Namespace.getNamespace(XMLToolsForYAWL.getSchema4SchemaNameSpace());
        Element schemElem = specificationElem.getChild("schema", schema4SchemaNS);
        if (null != schemElem) {
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            _specification.setSchema(outputter.outputString(schemElem));
        }

        // if name and doco fields missing from spec, see if they are in metadata
        // Added MJA 31/03/2006 
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
                    _specification.getBetaVersion());
            YNet rootNet = (YNet) _decompositionParser[0].getDecomposition();
            _specification.setRootNet(rootNet);

            for (int i = 1; i <= decompositionElems.size(); i++) {
                Element decompositionElem = (Element) decompositionElems.get(i - 1);
                _decompositionParser[i] = new YDecompositionParser(
                        decompositionElem,
                        this,
                        _specification.getBetaVersion());
                YDecomposition decomposition = _decompositionParser[i].getDecomposition();
                _specification.setDecomposition(decomposition);
            }
        } else {//must be beta3 or greater
            _decompositionParser = new YDecompositionParser[decompositionElems.size()];
            for (int i = 0; i < decompositionElems.size(); i++) {
                Element decompositionElem = (Element) decompositionElems.get(i);
                _decompositionParser[i] = new YDecompositionParser(
                        decompositionElem,
                        this,
                        _specification.getBetaVersion());
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
        for (int i = 0; i < creators.size(); i++) {
            Element creatorElem = (Element) creators.get(i);
            metaData.setCreator(creatorElem.getText());
        }

        List subjects = metaDataElem.getChildren("subject", _yawlNS);
        for (int i = 0; i < subjects.size(); i++) {
            Element subjectElem = (Element) subjects.get(i);
            metaData.setSubject(subjectElem.getText());
        }

        metaData.setDescription(metaDataElem.getChildText("description", _yawlNS));

        List contributors = metaDataElem.getChildren("contributor", _yawlNS);
        for (int i = 0; i < contributors.size(); i++) {
            Element contributor = (Element) contributors.get(i);
            metaData.setContributor(contributor.getText());
        }

        metaData.setCoverage(metaDataElem.getChildText("coverage", _yawlNS));

        String validFrom = metaDataElem.getChildText("validFrom", _yawlNS);
        if (validFrom != null) {
            try {
                metaData.setValidFrom(metaData.dateFormat.parse(validFrom));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String validUntil = metaDataElem.getChildText("validUntil", _yawlNS);
        if (validUntil != null) {
            try {
                metaData.setValidUntil(metaData.dateFormat.parse(validUntil));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String created = metaDataElem.getChildText("created", _yawlNS);
        if (created != null) {
            try {
                metaData.setCreated(metaData.dateFormat.parse(created));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        metaData.setVersion(metaDataElem.getChildText("version", _yawlNS));

        metaData.setStatus(metaDataElem.getChildText("status", _yawlNS));

        return metaData;
    }


    /**
     * @return whether this is a a beta 2 specification version or not.
     */
    private boolean isBeta2Version() {
        return _specification.getBetaVersion().equals(YSpecification._Beta2);
    }


    /**
     * adds the XML schema library to the specification.
     * @param specificationElem
     */
    private void addSchema(Element specificationElem) throws YSyntaxException {

        Element schemaElem = specificationElem.getChild("schema");
        if (schemaElem != null) {
            XMLOutputter xmlo = new XMLOutputter(Format.getCompactFormat());
            String schemaStr = xmlo.outputString(schemaElem);
            try {
                _specification.setSchema(schemaStr);
            } catch (YSchemaBuildingException e) {
                YSyntaxException f = new YSyntaxException(e.getMessage());
                f.setStackTrace(e.getStackTrace());
                throw f;
            }
        }
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
        String decoType = null;
        decoType = (String) _decompAndTypeMap.get(decomposesToID);
        return decoType;
    }


}

/*
    / **
     * @deprecated
     * @return
     * /
    private YNet getRootNet()
    {
        List allContainers = new Vector();
        List implementationContainers = new Vector();
        for(int i = 0; i < _decompositionParser.length; i++)
        {
            YNet container = _decompositionParser[i].getNet();
            allContainers.add(container);
            Map decomposesToIDs = _decompositionParser[i].getDecomposesToIDs();
            Iterator compTasksIter = decomposesToIDs.keySet().iterator();
            while(compTasksIter.hasNext())
            {
                YCompositeTask compTask = (YCompositeTask) compTasksIter.next();
                YNet implementation = (YNet) _decompositionMap.get(decomposesToIDs.get(compTask));
                implementationContainers.add(implementation);
            }
        }
        allContainers.removeAll(implementationContainers);
        return (YNet) allContainers.get(0);
    }*/
