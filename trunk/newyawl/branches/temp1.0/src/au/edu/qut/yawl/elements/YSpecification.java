/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.unmarshal.SchemaForSchemaValidator;
import au.edu.qut.yawl.unmarshal.YMetaData;
import au.edu.qut.yawl.util.YVerificationMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;


/**
 * 
 * Objects of this type are a specification of a Workflow checkSchema model in YAWL.
 * @author Lachlan Aldred
 * 
 */
public final class YSpecification implements Cloneable, YVerifiable {
    private String _specURI;
    private YNet _rootNet;
    private Map _decompositions = new HashMap();
    private String _name;
    private String _documentation;
    private String _version;
    
    public static final String _Beta2 = "Beta 2";
    public static final String _Beta3 = "Beta 3";
    public static final String _Beta4 = "Beta 4";
    public static final String _Beta6 = "Beta 6";
    public static final String _Beta7_1 = "Beta 7.1"; 
    public static final String _Version1_0 = "1.0";

    private XMLToolsForYAWL _xmlToolsForYAWL;
    public static String _loaded = "loaded";
    public static String _unloaded = "unloaded";
    private YMetaData _metaData;


    public YSpecification(String specURI) {
        _specURI = specURI;
        _xmlToolsForYAWL = new XMLToolsForYAWL();
    }


    public YNet getRootNet() {
        return _rootNet;
    }


    public void setRootNet(YNet rootNet) {
        this._rootNet = rootNet;
        setDecomposition(rootNet);
    }


    /**
     * Gets the version number of this specification.
     * @return the version of the engine that this specification works for.
     */
    public String getBetaVersion() {
        return _version;
    }

    public boolean usesSimpleRootData() {
        return YSpecification._Beta2.equals(_version) ||
                YSpecification._Beta3.equals(_version);
    }


    public boolean isSchemaValidating() {
        return !YSpecification._Beta2.equals(_version);
    }


    /**
     * Sets the version number of the specification.
     * @param version
     * @deprecated since v1.0
     */
    public void setBetaVersion(String version) { setVersion(version) ; }

    public void setVersion(String version) {
        if (_Beta2.equals(version) ||
                _Beta3.equals(version) ||
                _Beta4.equals(version) ||
                _Beta6.equals(version) ||
                _Beta7_1.equals(version) ||
                _Version1_0.equals(version)) {
            this._version = version;
        } else if ("beta3".equals(version)) {
            this._version = _Beta3;
        } else {
            throw new IllegalArgumentException("Param version [" +
                    version + "] is not allowed.");
        }
    }


    /**
     * Allows users to add schemas.
     * @param schemaString
     * @throws YSchemaBuildingException
     */
    public void setSchema(String schemaString) throws YSchemaBuildingException, YSyntaxException {
        _xmlToolsForYAWL.setPrimarySchema(schemaString);
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<specification uri=\"");
        xml.append(_specURI);
        xml.append("\">");
        if (_name != null) {
            xml.append("<name>").
                    append(_name).
                    append("</name>");
        }
        if (_documentation != null) {
            xml.append("<documentation>").
                    append(_documentation).
                    append("</documentation>");
        }
        xml.append(_metaData.toXML());
        xml.append(_xmlToolsForYAWL.getSchemaString());
        xml.append("<decomposition id=\"").
                append(_rootNet.getID()).
                append("\" isRootNet=\"true\" xsi:type=\"NetFactsType\">");
        xml.append(_rootNet.toXML());
        xml.append("</decomposition>");
        for (Iterator iterator = _decompositions.values().iterator(); iterator.hasNext();) {
            YDecomposition decomposition = (YDecomposition) iterator.next();
            if (!decomposition.getID().equals(_rootNet.getID())) {
                xml.append("<decomposition id=\"").
                        append(decomposition.getID()).
                        append("\"");
                xml.append(" xsi:type=\"");
                if (decomposition instanceof YNet) {
                    xml.append("NetFactsType");
                } else {
                    xml.append("WebServiceGatewayFactsType");
                }
                xml.append("\"");

                //AJH: Add in any additional attributes
                for(Enumeration enumeration = decomposition.getAttributes().keys(); enumeration.hasMoreElements();)
                {
                    String key = enumeration.nextElement().toString();
                    xml.append(" ").append(key).append("=\"").
                            append(decomposition.getAttribute(key)).append("\"");
                }
                
                xml.append(">").append(decomposition.toXML());
                xml.append("</decomposition>");
            }
        }
        xml.append("</specification>");
        return xml.toString();
    }


    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }

    public YDecomposition getDecomposition(String id) {
        return (YDecomposition) _decompositions.get(id);
    }


    public void setDecomposition(YDecomposition decomposition) {
        _decompositions.put(decomposition.getID(), decomposition);
    }


    //#####################################################################################
    //                              VERIFICATION TASKS
    //#####################################################################################
    public List verify() {
        List messages = new ArrayList();
        for (Iterator iterator = _decompositions.values().iterator(); iterator.hasNext();) {
            YDecomposition decomposition = (YDecomposition) iterator.next();
            messages.addAll(decomposition.verify());
        }
        //check all nets are being used
        //check that decomposition works
        if (_rootNet != null) {
            messages.addAll(checkDecompositionUsage());
            messages.addAll(checkForInfiniteLoops());
            messages.addAll(checkForEmptyExecutionPaths());
        } else {
            messages.add(
                    new YVerificationMessage(this,
                            "Specifications must have a root net.",
                            YVerificationMessage.ERROR_STATUS)
            );
        }
        messages.addAll(checkDataTypesValidity());
        return messages;
    }


    private List checkDataTypesValidity() {
        List msgs = new ArrayList();
        String schemaString = _xmlToolsForYAWL.getSchemaString();
        String errors = SchemaForSchemaValidator.getInstance().validateSchema(schemaString);
        BufferedReader bReader = new BufferedReader(new StringReader(errors));
        String result;
        try {
            while ((result = bReader.readLine()) != null) {
                if (result.length() > 0) {
                    msgs.add(new YVerificationMessage(
                            this,
                            "DataType problem: " + result,
                            YVerificationMessage.ERROR_STATUS));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgs;
    }


    private List checkForEmptyExecutionPaths() {
        List messages = new ArrayList();
        for (Iterator iterator = _decompositions.values().iterator(); iterator.hasNext();) {
            YDecomposition decomposition = (YDecomposition) iterator.next();
            if (decomposition instanceof YNet) {
                Set visited = new HashSet();
                visited.add(((YNet) decomposition).getInputCondition());

                Set visiting = getEmptyPostsetAtThisLevel(visited);
                while (visiting.size() > 0) {
                    if (visiting.contains(((YNet) decomposition).getOutputCondition())) {
                        messages.add(new YVerificationMessage(
                                decomposition,
                                "The net (" + decomposition +
                                ") may complete without any generated work.  " +
                                "Check the empty tasks linking from i to o.",
                                YVerificationMessage.WARNING_STATUS));
                    }
                    visiting.removeAll(visited);
                    visited.addAll(visiting);
                    visiting = getEmptyPostsetAtThisLevel(visiting);
                }
            }
        }
        return messages;
    }

    private Set getEmptyPostsetAtThisLevel(Set aSet) {
        Set elements = YNet.getPostset(aSet);
        Set resultSet = new HashSet();
        for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) iterator.next();
            if (element instanceof YTask && ((YTask) element).getDecompositionPrototype() == null ||
                    element instanceof YCondition) {
                resultSet.add(element);
            }
        }
        return resultSet;
    }


    private List checkForInfiniteLoops() {
        List messages = new ArrayList();
        //check inifinite loops under rootnet and generate error messages
        Set relevantNets = new HashSet();
        relevantNets.add(_rootNet);
        Set relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        messages.addAll(checkTheseTasksForInfiniteLoops(relevantTasks, false));
        //check inifinite loops not under rootnet and generate warning messages
        Set netsBeingUsed = new HashSet();
        unfoldNetChildren(_rootNet, netsBeingUsed, null);
        relevantNets = new HashSet(_decompositions.values());
        relevantNets.removeAll(netsBeingUsed);
        relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        messages.addAll(checkTheseTasksForInfiniteLoops(relevantTasks, true));
        return messages;
    }


    private List checkTheseTasksForInfiniteLoops(Set relevantTasks, boolean generateWarnings) {
        List messages = new ArrayList();
        for (Iterator iterator = relevantTasks.iterator(); iterator.hasNext();) {
            YExternalNetElement q = (YExternalNetElement) iterator.next();
            Set visited = new HashSet();
            visited.add(q);

            Set visiting = getEmptyTasksPostset(visited);
            while (visiting.size() > 0) {
                if (visiting.contains(q)) {
                    messages.add(new YVerificationMessage(
                            q,
                            "The element (" + q + ") plays a part in an inifinite " +
                            "loop/recursion in which no work items may be created.",
                            generateWarnings ?
                            YVerificationMessage.WARNING_STATUS :
                            YVerificationMessage.ERROR_STATUS));
                }
                visiting.removeAll(visited);
                visited.addAll(visiting);
                visiting = getEmptyTasksPostset(visiting);
            }
        }
        return messages;
    }


    private Set selectEmptyAndDecomposedTasks(Set relevantNets) {
        Set relevantTasks = new HashSet();
        for (Iterator iterator = relevantNets.iterator(); iterator.hasNext();) {
            YDecomposition decomposition = (YDecomposition) iterator.next();
            relevantTasks.addAll(unfoldNetChildren(decomposition, new HashSet(), "emptyTasks"));
            relevantTasks.addAll(unfoldNetChildren(decomposition, new HashSet(), "decomposedTasks"));
        }
        return relevantTasks;
    }


    private Set getEmptyTasksPostset(Set set) {
        Set resultSet = new HashSet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            YTask task = (YTask) iterator.next();
            if (task.getDecompositionPrototype() instanceof YNet) {
                YInputCondition input = ((YNet) task.getDecompositionPrototype()).getInputCondition();
                Set tasks = input.getPostsetElements();
                for (Iterator iterator2 = tasks.iterator(); iterator2.hasNext();) {
                    YTask task2 = (YTask) iterator2.next();
                    if (task2.getDecompositionPrototype() == null || task2.getDecompositionPrototype() instanceof YNet) {
                        resultSet.add(task2);
                    }
                }
            } else {
                Set postSet = task.getPostsetElements();
                Set taskPostSet = YNet.getPostset(postSet);
                for (Iterator iterator2 = taskPostSet.iterator(); iterator2.hasNext();) {
                    YTask task2 = (YTask) iterator2.next();
                    if (task2.getDecompositionPrototype() == null) {
                        resultSet.add(task2);
                    }
                }
            }
        }
        return resultSet;
    }


    private List checkDecompositionUsage() {
        List messages = new ArrayList();
        Set netsBeingUsed = new HashSet();
        unfoldNetChildren(_rootNet, netsBeingUsed, null);
        Set specifiedDecompositons = new HashSet(_decompositions.values());
        specifiedDecompositons.removeAll(netsBeingUsed);

        Set decompositionsNotBeingUsed = specifiedDecompositons;
        if (decompositionsNotBeingUsed.size() > 0) {
            for (Iterator iterator = decompositionsNotBeingUsed.iterator(); iterator.hasNext();) {
                YDecomposition decomp = (YDecomposition) iterator.next();
                messages.add(new YVerificationMessage(decomp, "The decompositon(" + decomp.getID() +
                        ") is not being used in this specification.", YVerificationMessage.WARNING_STATUS));
            }
        }
        return messages;
    }


    private Set unfoldNetChildren(YDecomposition decomposition, Set netsAlreadyExplored, String criterion) {
        Set resultSet = new HashSet();
        netsAlreadyExplored.add(decomposition);
        if (decomposition instanceof YAWLServiceGateway) {
            return resultSet;
        }
        Set visited = new HashSet();
        Set visiting = new HashSet();
        visiting.add(((YNet) decomposition).getInputCondition());
        do {
            visited.addAll(visiting);
            visiting = YNet.getPostset(visiting);
            visiting.removeAll(visited);
            for (Iterator iterator = visiting.iterator(); iterator.hasNext();) {
                YExternalNetElement element = (YExternalNetElement) iterator.next();
                if (element instanceof YTask) {
                    YDecomposition decomp = ((YTask) element).getDecompositionPrototype();
                    if (decomp != null) {
                        if (decomp instanceof YNet) {
                            if ("decomposedTasks".equals(criterion)) {
                                resultSet.add(element);
                            }
                        }
                        if (!netsAlreadyExplored.contains(decomp)) {
                            resultSet.addAll(unfoldNetChildren(decomp, netsAlreadyExplored, criterion));
                        }
                    } else if ("emptyTasks".equals(criterion)) {
                        resultSet.add(element);
                    }
                } else if (element instanceof YCondition) {
                    if ("allConditions".equals(criterion)) {
                        resultSet.add(element);
                    }
                }
            }
        } while (visiting.size() > 0);
        return resultSet;
    }


    public Set getDecompositions() {
        return new HashSet(_decompositions.values());
    }

    public String getID() {
        return _specURI;
    }


    /**
     * Returns the XML tools for YAWL.
     * @return the XMLTools4Yawl object.
     */
    public XMLToolsForYAWL getToolsForYAWL() {
        return _xmlToolsForYAWL;
    }

    public void setMetaData(YMetaData metaData) {
        _metaData = metaData;
    }

    public YMetaData getMetaData() {
        return _metaData;
    }
}
