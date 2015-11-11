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

package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YDataValidator;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;

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
    private Map<String, YDecomposition> _decompositions =
                                        new HashMap<String, YDecomposition>();
    private String _name;
    private String _documentation;
    private YSchemaVersion _version = YSchemaVersion.defaultVersion();
    private YDataValidator _dataValidator;
    private YMetaData _metaData;

    public static final String _loaded = "loaded";
    public static final String _unloaded = "unloaded";


    public YSpecification() { }

    public YSpecification(String specURI) {
        _specURI = specURI;
    }


    public YNet getRootNet() {
        return _rootNet;
    }


    public void setRootNet(YNet rootNet) {
        _rootNet = rootNet;
        addDecomposition(rootNet);
    }


    /**
     * @deprecated since v2.0: use getSchemaVersion() instead
     * Gets the version number of this specification's schema (as opposed to the
     * version number of the specification itself).
     * @return the version of the engine that this specification was first designed for.
     */
    public String getBetaVersion() {
        return getSchemaVersion().toString();
    }

    public YSchemaVersion getSchemaVersion() {
        return _version;
    }

    /**
      * Gets the version number of this specification (as opposed to the
      * version number of the specification's schema).
      * @return the version of this specification.
      */
    public String getSpecVersion() {
        if (_metaData != null) {
            YSpecVersion specVersion = _metaData.getVersion();
            if (specVersion != null) {
                return specVersion.toString();
            }
        }
        return "0.1";                                  // default version number       
    }

    
    /**
     * Sets the version number of the specification.
     * @param version
     * @deprecated since v2.0: use setVersion() instead.
     */
    public void setBetaVersion(String version) { setVersion(version) ; }

    public void setVersion(String version) {
        if (version.equals("beta3")) version = "Beta 3";
        _version = YSchemaVersion.fromString(version);
        if (_version == null) {
            throw new IllegalArgumentException("Param version [" +
                    version + "] is not allowed.");
        }
    }

    
    public void setVersion(YSchemaVersion version) {
        _version = version;
    }


    /**
     * Sets the data schema for this specification.
     * @param schemaString
     */
    public void setSchema(String schemaString) throws YSyntaxException {
        _dataValidator = new YDataValidator(schemaString);
    }

    public YDataValidator getDataValidator() {
        return _dataValidator;
    }

    public String getDataSchema() {
        return (_dataValidator != null) ? _dataValidator.getSchema() : null;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<specification uri=\"%s\">", JDOMUtil.encodeEscapes(_specURI)));
        if (_name != null) xml.append(StringUtil.wrapEscaped(_name, "name"));
        if (_documentation != null) xml.append(StringUtil.wrapEscaped(_documentation, "documentation"));
        xml.append(_metaData.toXML());
        xml.append(_dataValidator.getSchema());
        xml.append("<decomposition id=\"")
           .append(_rootNet.getID())
           .append("\" isRootNet=\"true\" xsi:type=\"NetFactsType\">");
        xml.append(_rootNet.toXML());
        xml.append("</decomposition>");

        // sort decompositions by YNet, then ID
        List<YDecomposition> sortedDecompositions  =
                new ArrayList<YDecomposition>(_decompositions.values());
        Collections.sort(sortedDecompositions, new Comparator<YDecomposition>() {
            public int compare(YDecomposition d1, YDecomposition d2) {
                    if (d1 instanceof YNet) {
                        if (! (d2 instanceof YNet)) return -1;   // d1 is YNet, d2 is not
                    }
                    else if (d2 instanceof YNet) return 1;       // d2 is YNet, d1 is not

                    if (d1.getID() == null) return -1;           // either both are YNets
                    if (d2.getID() == null) return 1;            // or both are not
                    return d1.getID().compareTo(d2.getID());     // so sort on ids
                }
        });

        for (YDecomposition decomposition : sortedDecompositions) {
            if (! decomposition.getID().equals(_rootNet.getID())) {
                String factsType = (decomposition instanceof YNet) ? "NetFactsType" :
                                                        "WebServiceGatewayFactsType";
                xml.append(String.format("<decomposition id=\"%s\" xsi:type=\"%s\"%s>",
                               decomposition.getID(), factsType,
                               decomposition.getAttributes().toXML()));

                xml.append(decomposition.toXML());

                // set flag for resourcing requirements on task decompositions
                if (! (decomposition instanceof YNet)) {
                    if (decomposition.getCodelet() != null) {
                        xml.append(StringUtil.wrap(decomposition.getCodelet(), "codelet"));
                    }
                    if (! _version.isBetaVersion()) {
                        xml.append("<externalInteraction>")
                           .append(decomposition.requiresResourcingDecisions() ? "manual": "automated")
                           .append("</externalInteraction>");
                    }
                }
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
        return _decompositions.get(id);
    }


    public void addDecomposition(YDecomposition decomposition) {
        _decompositions.put(decomposition.getID(), decomposition);
    }

    public Set<YDecomposition> getDecompositions() {
        return new HashSet<YDecomposition>(_decompositions.values());
    }

    public YDecomposition removeDecomposition(String decompositionID) {
        return _decompositions.remove(decompositionID);
    }

    public String getURI() {
        return _specURI;
    }

    public void setURI(String uri) {
        _specURI = uri;
    }

    public String getID() {
        return _metaData != null ? _metaData.getUniqueID() : null;
    }


    public YSpecificationID getSpecificationID() {
        return new YSpecificationID(getID(), getSpecVersion(), _specURI);
    }

    public void setMetaData(YMetaData metaData) {
        _metaData = metaData;
    }

    public YMetaData getMetaData() {
        return _metaData;
    }


    public boolean equals(Object other) {
        return (other instanceof YSpecification) ?  // instanceof = false if other is null
                getSpecificationID().equals(((YSpecification) other).getSpecificationID())
                : super.equals(other);
    }

    public int hashCode() {
        return getSpecificationID().hashCode();
    }

    /************************************/

    // for hibernate persistence

    private long rowKey ;                                       // PK - auto generated
    private String persistedXML ;

    private String getPersistedXML() {
        try {
            if (persistedXML == null)
                persistedXML = YMarshal.marshal(this);
            return persistedXML;
        }
        catch (Exception e) {
            return null;
        }
    }

    private void setPersistedXML(String xml) { persistedXML = xml; }

    public long getRowKey() { return rowKey; }

    public void setRowKey(long key) { rowKey = key; }


    // for YEngineRestorer
    
    public String getRestoredXML() { return persistedXML; }



    //##################################################################################
    //                              VERIFICATION TASKS                                //
    //##################################################################################

    public void verify(YVerificationHandler handler) {
        for (YDecomposition decomposition : _decompositions.values()) {
            decomposition.verify(handler);
        }

        //check all nets are being used & that each decomposition works
        if (_rootNet != null) {
            checkDecompositionUsage(handler);
            checkForInfiniteLoops(handler);
            checkForEmptyExecutionPaths(handler);
        }
        else {
            handler.error(this, "Specifications must have a root net.");
        }
        checkDataTypesValidity(handler);
    }


    private void checkDataTypesValidity(YVerificationHandler handler) {
        if (! _dataValidator.validateSchema()) {
            for (String message : _dataValidator.getMessages()) {
                handler.error(this, message);
            }
        }
    }


    private void checkForEmptyExecutionPaths(YVerificationHandler handler) {
        for (YDecomposition decomposition : _decompositions.values()) {
            if (decomposition instanceof YNet) {
                Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
                visited.add(((YNet) decomposition).getInputCondition());

                Set<YExternalNetElement> visiting = getEmptyPostsetAtThisLevel(visited);
                while (visiting.size() > 0) {
                    if (visiting.contains(((YNet) decomposition).getOutputCondition())) {
                        handler.warn(decomposition,
                                "It may be possible for the net [" + decomposition +
                                "] to complete without any generated work. " +
                                "Check the empty tasks linking from i to o.");
                    }
                    visiting.removeAll(visited);
                    visited.addAll(visiting);
                    visiting = getEmptyPostsetAtThisLevel(visiting);
                }
            }
        }
    }


    private Set<YExternalNetElement> getEmptyPostsetAtThisLevel(Set<YExternalNetElement> aSet) {
        Set<YExternalNetElement> elements = YNet.getPostset(aSet);
        Set<YExternalNetElement> resultSet = new HashSet<YExternalNetElement>();
        for (YExternalNetElement element : elements) {
            if ((element instanceof YCondition) || ((element instanceof YTask) &&
                    (((YTask) element).getDecompositionPrototype() == null))) {
                resultSet.add(element);
            }
        }
        return resultSet;
    }


    private void checkForInfiniteLoops(YVerificationHandler handler) {

        //check infinite loops under rootnet and generate error messages
        Set<YDecomposition> relevantNets = new HashSet<YDecomposition>();
        relevantNets.add(_rootNet);
        Set<YExternalNetElement> relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        checkTheseTasksForInfiniteLoops(relevantTasks, false, handler);
        checkForEmptyTasksWithTimerParams(relevantTasks, handler);

        //check infinite loops not under rootnet and generate warning messages
        Set<YDecomposition> netsBeingUsed = new HashSet<YDecomposition>();
        unfoldNetChildren(_rootNet, netsBeingUsed, null);
        relevantNets = new HashSet<YDecomposition>(_decompositions.values());
        relevantNets.removeAll(netsBeingUsed);
        relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        checkTheseTasksForInfiniteLoops(relevantTasks, true, handler);
        checkForEmptyTasksWithTimerParams(relevantTasks, handler);
    }


    private void checkTheseTasksForInfiniteLoops(Set<YExternalNetElement> relevantTasks,
                boolean generateWarnings, YVerificationHandler handler) {
        for (YExternalNetElement element : relevantTasks) {
            Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
            visited.add(element);

            Set<YExternalNetElement> visiting = getEmptyTasksPostset(visited);
            while (visiting.size() > 0) {
                if (visiting.contains(element)) {
                    handler.add(element,
                            "The element (" + element + ") plays a part in an " +
                            "infinite loop/recursion in which no work items may be created.",
                            generateWarnings ? YVerificationHandler.MessageType.warning :
                                    YVerificationHandler.MessageType.error);
                }
                visiting.removeAll(visited);
                visited.addAll(visiting);
                visiting = getEmptyTasksPostset(visiting);
            }
        }
    }


    private void checkForEmptyTasksWithTimerParams(Set<YExternalNetElement> relevantTasks,
                                                   YVerificationHandler handler) {
        for (YExternalNetElement element : relevantTasks) {
            YTask task = (YTask) element;
            if (task.getDecompositionPrototype() == null) {
                if (task.getTimerParameters() != null) {
                    handler.warn(task, "The task [" + task + "] has timer settings but " +
                             "no decomposition. The timer settings will be ignored at runtime.");
                }
            }
        }
    }


    private Set<YExternalNetElement> selectEmptyAndDecomposedTasks(Set<YDecomposition> relevantNets) {
        Set<YExternalNetElement> relevantTasks = new HashSet<YExternalNetElement>();
        for (YDecomposition decomposition : relevantNets) {
            relevantTasks.addAll(unfoldNetChildren(decomposition,
                                 new HashSet<YDecomposition>(), "emptyTasks"));
            relevantTasks.addAll(unfoldNetChildren(decomposition,
                                 new HashSet<YDecomposition>(), "decomposedTasks"));
        }
        return relevantTasks;
    }


    private Set<YExternalNetElement> getEmptyTasksPostset(Set<YExternalNetElement> set) {
        Set<YExternalNetElement> resultSet = new HashSet<YExternalNetElement>();
        for (YExternalNetElement element : set) {
            YTask task = (YTask) element;
            if (task.getDecompositionPrototype() instanceof YNet) {
                YInputCondition input = ((YNet) task.getDecompositionPrototype()).getInputCondition();
                Set<YExternalNetElement> tasks = input.getPostsetElements();
                for (YExternalNetElement otherElement : tasks) {
                    YTask otherTask = (YTask) otherElement;
                    if (otherTask.getDecompositionPrototype() == null ||
                        otherTask.getDecompositionPrototype() instanceof YNet) {
                        resultSet.add(otherTask);
                    }
                }
            }
            else {
                Set<YExternalNetElement> postSet = task.getPostsetElements();
                Set<YExternalNetElement> taskPostSet = YNet.getPostset(postSet);
                for (YExternalNetElement otherElement : taskPostSet) {
                    YTask otherTask = (YTask) otherElement;
                    if (otherTask.getDecompositionPrototype() == null) {
                        resultSet.add(otherTask);
                    }
                }
            }
        }
        return resultSet;
    }


    private void checkDecompositionUsage(YVerificationHandler handler) {
        Set<YDecomposition> netsBeingUsed = new HashSet<YDecomposition>();
        unfoldNetChildren(_rootNet, netsBeingUsed, null);
        Set<YDecomposition> specifiedDecompositions =
                new HashSet<YDecomposition>(_decompositions.values());
        specifiedDecompositions.removeAll(netsBeingUsed);

        for (YDecomposition decomp : specifiedDecompositions) {
            handler.warn(decomp, "The " +
                    (decomp instanceof YNet ? "net" : "decomposition") +
                    " [" + decomp.getID() +
                    "] is not being used in this specification.");
        }
    }


    private Set<YExternalNetElement> unfoldNetChildren(YDecomposition decomposition,
                                  Set<YDecomposition> netsAlreadyExplored, String criterion) {
        Set<YExternalNetElement> resultSet = new HashSet<YExternalNetElement>();
        netsAlreadyExplored.add(decomposition);
        if (decomposition instanceof YAWLServiceGateway) {
            return resultSet;
        }
        Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
        visiting.add(((YNet) decomposition).getInputCondition());
        do {
            visited.addAll(visiting);
            visiting = YNet.getPostset(visiting);
            visiting.removeAll(visited);
            for (YExternalNetElement element : visiting) {
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
                    }
                    else if ("emptyTasks".equals(criterion)) {
                        resultSet.add(element);
                    }
                }
                else if (element instanceof YCondition) {
                    if ("allConditions".equals(criterion)) {
                        resultSet.add(element);
                    }
                }
            }
        } while (visiting.size() > 0);
        return resultSet;
    }

}
