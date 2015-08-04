/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;


/**
 * 
 * Objects of this type are a specification of a Workflow checkSchema model in YAWL.
 * @author Lachlan Aldred
 * 
 */
public final class YSpecification implements Cloneable, YVerifiable {
    private String _specURI;
    private YSpecificationID _specID;
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
        setDecomposition(rootNet);
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


    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<specification uri=\"%s\">", _specURI));
        if (_name != null) xml.append(StringUtil.wrap(_name, "name"));
        if (_documentation != null) xml.append(StringUtil.wrap(_documentation, "documentation"));
        xml.append(_metaData.toXML());
        xml.append(_dataValidator.getSchema());
        xml.append("<decomposition id=\"")
           .append(_rootNet.getID())
           .append("\" isRootNet=\"true\" xsi:type=\"NetFactsType\">");
        xml.append(_rootNet.toXML());
        xml.append("</decomposition>");

        for (YDecomposition decomposition : _decompositions.values()) {
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


    public void setDecomposition(YDecomposition decomposition) {
        _decompositions.put(decomposition.getID(), decomposition);
    }

    public Set<YDecomposition> getDecompositions() {
        return new HashSet<YDecomposition>(_decompositions.values());
    }

    public String getURI() {
        return _specURI;
    }

    public String getID() {
        return _metaData.getUniqueID();
    }


    public YSpecificationID getSpecificationID() {
        if (_specID == null) {
            _specID = new YSpecificationID(_metaData.getUniqueID(),
                    _metaData.getVersion(), _specURI);
        }
        return _specID;
    }

    public void setMetaData(YMetaData metaData) {
        _metaData = metaData;
    }

    public YMetaData getMetaData() {
        return _metaData;
    }


    public boolean equals(Object other) {
        return (other instanceof YSpecification) &&  // instanceof = false if other is null
                ((getSpecificationID() != null) ?
                  getSpecificationID().equals(((YSpecification) other).getSpecificationID())
                : super.equals(other));
    }

    public int hashCode() {
        return (getSpecificationID() != null) ? getSpecificationID().hashCode()
                : super.hashCode();
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

    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        for (YDecomposition decomposition : _decompositions.values()) {
            messages.addAll(decomposition.verify());
        }

        //check all nets are being used & that each decomposition works
        if (_rootNet != null) {
            messages.addAll(checkDecompositionUsage());
            messages.addAll(checkForInfiniteLoops());
            messages.addAll(checkForEmptyExecutionPaths());
        }
        else {
            messages.add(
                    new YVerificationMessage(this,
                            "Specifications must have a root net.",
                            YVerificationMessage.ERROR_STATUS)
            );
        }
        messages.addAll(checkDataTypesValidity());
        return messages;
    }


    private List<YVerificationMessage> checkDataTypesValidity() {
        List<YVerificationMessage> msgs = new ArrayList<YVerificationMessage>();
        if (!_dataValidator.validateSchema()) {
            for (String message : _dataValidator.getMessages()) {
                msgs.add(new YVerificationMessage(this,message,YVerificationMessage.ERROR_STATUS));
            }
        }
        return msgs;
    }


    private List<YVerificationMessage> checkForEmptyExecutionPaths() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        for (YDecomposition decomposition : _decompositions.values()) {
            if (decomposition instanceof YNet) {
                Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
                visited.add(((YNet) decomposition).getInputCondition());

                Set<YExternalNetElement> visiting = getEmptyPostsetAtThisLevel(visited);
                while (visiting.size() > 0) {
                    if (visiting.contains(((YNet) decomposition).getOutputCondition())) {
                        messages.add(new YVerificationMessage(
                                decomposition,
                                "It may be possible for the net (" + decomposition +
                                ") to complete without any generated work. " +
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


    private List<YVerificationMessage> checkForInfiniteLoops() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();

        //check infinite loops under rootnet and generate error messages
        Set<YDecomposition> relevantNets = new HashSet<YDecomposition>();
        relevantNets.add(_rootNet);
        Set<YExternalNetElement> relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        messages.addAll(checkTheseTasksForInfiniteLoops(relevantTasks, false));
        messages.addAll(checkForEmptyTasksWithTimerParams(relevantTasks));

        //check infinite loops not under rootnet and generate warning messages
        Set<YDecomposition> netsBeingUsed = new HashSet<YDecomposition>();
        unfoldNetChildren(_rootNet, netsBeingUsed, null);
        relevantNets = new HashSet<YDecomposition>(_decompositions.values());
        relevantNets.removeAll(netsBeingUsed);
        relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        messages.addAll(checkTheseTasksForInfiniteLoops(relevantTasks, true));
        messages.addAll(checkForEmptyTasksWithTimerParams(relevantTasks));
        return messages;
    }


    private List<YVerificationMessage> checkTheseTasksForInfiniteLoops(
                Set<YExternalNetElement> relevantTasks, boolean generateWarnings) {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        for (YExternalNetElement element : relevantTasks) {
            Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
            visited.add(element);

            Set<YExternalNetElement> visiting = getEmptyTasksPostset(visited);
            while (visiting.size() > 0) {
                if (visiting.contains(element)) {
                    messages.add(new YVerificationMessage(
                            element,
                            "The element (" + element + ") plays a part in an infinite " +
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


    private List<YVerificationMessage> checkForEmptyTasksWithTimerParams(
                Set<YExternalNetElement> relevantTasks) {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        for (YExternalNetElement element : relevantTasks) {
            YTask task = (YTask) element;
            if (task.getDecompositionPrototype() == null) {
                if (task.getTimeParameters() != null) {
                    messages.add(new YVerificationMessage( task,
                            "The task (" + task + ") has timer settings but no " +
                             "decomposition. The timer settings will be ignored at runtime.",
                             YVerificationMessage.WARNING_STATUS));
                }
            }
        }
        return messages;
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


    private List<YVerificationMessage> checkDecompositionUsage() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        Set<YDecomposition> netsBeingUsed = new HashSet<YDecomposition>();
        unfoldNetChildren(_rootNet, netsBeingUsed, null);
        Set<YDecomposition> specifiedDecompositions =
                new HashSet<YDecomposition>(_decompositions.values());
        specifiedDecompositions.removeAll(netsBeingUsed);

        for (YDecomposition decomp : specifiedDecompositions) {
            messages.add(new YVerificationMessage(decomp, "The decomposition(" +
                    decomp.getID() + ") is not being used in this specification.",
                    YVerificationMessage.WARNING_STATUS));
        }
        return messages;
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
