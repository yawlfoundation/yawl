package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.editor.core.util.FileUtil;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.XNode;

import java.io.File;
import java.util.List;

/**
 * Manages a file based repository of task decompositions, net decompositions,
 * extended variables and data definitions for re-use across different specifications
 * @author Michael Adams
 * @date 17/06/12
 */
public class Repository {

    // the 'maps' that store each repository (in memory and to/from file)
    private RepoMap _taskDecompositionMap;
    private RepoMap _netDecompositionMap;
    private RepoMap _extendedVariablesMap;
    private RepoMap _dataDefinitionMap;

    // the absolute path to the base repository dir
    private static final String BACKINGSTORE_DIR =
            FileUtil.getHomeDir() + File.separatorChar + "repository";


    public Repository() {
        _taskDecompositionMap = new RepoMap(getFilePath("taskDecompositions"));
        _netDecompositionMap = new RepoMap(getFilePath("netDecompositions"));
        _extendedVariablesMap = new RepoMap(getFilePath("extendedVariables"));
        _dataDefinitionMap = new RepoMap(getFilePath("dataDefinitions"));
    }


    /**
     * Adds a task decomposition to the repository
     * @param name a reference name for the task decomposition
     * @param description a description of it
     * @param decomposition the task decomposition to add
     * @return whether the add was successful
     */
    public boolean addTaskDecomposition(String name, String description,
                                        YDecomposition decomposition) {
        if (decomposition == null) return false;

        RepoRecord record = new RepoRecord(name, description, toXML(decomposition));
        return add(_taskDecompositionMap, record);
    }


    /**
     * Gets a task decomposition from the repository
     * @param name a reference name for the task decomposition
     * @return the referenced task decomposition, or null if not found
     */
    public YAWLServiceGateway getTaskDecomposition(String name) {
        RepoRecord record = get(_taskDecompositionMap, name);
        return (record == null) ? null :
                new RepoParser().parseTaskDecomposition(record.getValue());
    }


    /**
     * Removes a task decomposition from the repository
     * @param name a reference name for the task decomposition
     * @return whether the removal was successful
     */
    public boolean removeTaskDecomposition(String name) {
        return remove(_taskDecompositionMap, name) != null;
    }


    /**
     * Gets a sorted list of descriptors for all stored task decompositions
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getTaskDecompositionDescriptors() {
        return _taskDecompositionMap.getDescriptors();
    }


    /**************************************************************************/

    /**
     * Adds a net decomposition to the repository
     * @param name a reference name for the net decomposition
     * @param description a description of it
     * @param decomposition the net decomposition to add
     * @return whether the add was successful
     */
    public boolean addNetDecomposition(String name, String description,
                                        YDecomposition decomposition) {
        if (decomposition == null) return false;

        // todo: marshal & unmarshal nets
        RepoRecord record = new RepoRecord(name, description, decomposition.toXML());
        return add(_netDecompositionMap, record);
    }


    /**
     * Gets a net decomposition from the repository
     * @param name a reference name for the net decomposition
     * @return the referenced task decomposition, or null if not found
     */
    public YDecomposition getNetDecomposition(String name) {
        RepoRecord record = get(_netDecompositionMap, name);
        if (record != null) {
            return new RepoParser().parseNetDecomposition(record.getValue());
        }
        return null;
    }


    /**
     * Removes a net decomposition from the repository
     * @param name a reference name for the net decomposition
     * @return whether the removal was successful
     */
    public YDecomposition removeNetDecomposition(String name) {
        RepoRecord record = remove(_netDecompositionMap, name);
        if (record != null) {
            return new RepoParser().parseNetDecomposition(record.getValue());
        }
        return null;
    }


    /**
     * Gets a sorted list of descriptors for all stored net decompositions
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getNetDecompositionDescriptors() {
        return _netDecompositionMap.getDescriptors();
    }


    /******************************************************************************/

    /**
     * Adds a set of extended variables and their values to the repository
     * @param name a reference name for the net decomposition
     * @param description a description of it
     * @param attributes the set of attributes to store in the repository
     * @return whether the add was successful
     */
    public boolean addExtendedVariableMap(String name, String description,
                                        YAttributeMap attributes) {
        XNode valueNode = new XNode("attributes");
        valueNode.addContent(attributes.toXMLElements());
        RepoRecord record = new RepoRecord(name, description, valueNode.toString());
        return add(_extendedVariablesMap, record);
    }


    /**
     * Gets a set of extended variables from the repository
     * @param name a reference name for the extended variables
     * @return the referenced set of extended variables, or null if not found
     */
    public YAttributeMap getExtendedVariableMap(String name) {
        RepoRecord record = get(_extendedVariablesMap, name);
        if (record != null) {
            return new RepoParser().parseAttributeMap(record.getValue());
        }
        return null;
    }


    /**
     * Removes a set of extended variables from the repository
     * @param name a reference name for the extended variables
     * @return whether the removal was successful
     */
    public YAttributeMap removeExtendedVariableMap(String name) {
        RepoRecord record = remove(_extendedVariablesMap, name);
        if (record != null) {
            return new RepoParser().parseAttributeMap(record.getValue());
        }
        return null;
    }


    /**
     * Gets a sorted list of descriptors for all extended variable sets
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getExtendedVariableMapDescriptors() {
        return _extendedVariablesMap.getDescriptors();
    }


    /*********************************************************************************/

    /**
     * Adds a data definition to the repository
     * @param name a reference name for the data definition
     * @param description a description of it
     * @param xml the data definition XML to store in the repository
     * @return whether the add was successful
     */
    public boolean addDataDefinition(String name, String description, String xml) {
        RepoRecord record = new RepoRecord(name, description, xml);
        return add(_dataDefinitionMap, record);
    }


    /**
     * Gets a data definition from the repository
     * @param name a reference name for the data definition
     * @return the referenced data definition, or null if not found
     */
    public String getDataDefinition(String name) {
        RepoRecord record = get(_dataDefinitionMap, name);
        return record != null ? record.getValue() : null;
    }


    /**
     * Removes a data definition from the repository
     * @param name a reference name for the data definition
     * @return whether the removal was successful
     */
    public String removeDataDefinition(String name) {
        RepoRecord record = remove(_dataDefinitionMap, name);
        return record != null ? record.getValue() : null;
    }


    /**
     * Gets a sorted list of descriptors for all data definitions
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getDataDefinitionDescriptors() {
        return _dataDefinitionMap.getDescriptors();
    }


    /****************************************************************************/

    // gets the absolute file path for the named file
    private String getFilePath(String fileName) {
        return BACKINGSTORE_DIR + File.separatorChar + fileName + ".xml";
    }


    // adds a record to a map
    private boolean add(RepoMap map, RepoRecord record) {
        return map.add(record);
    }


    // gets a record from a map
    private RepoRecord get(RepoMap map, String name) {
        return map.get(name);
    }


    // removes a record from a map
    private RepoRecord remove(RepoMap map, String name) {
        return map.remove(name);
    }


    // writes a task decomposition to xml
    private String toXML(YDecomposition decomposition) {
        XNode decompNode = new XNode("decomposition");
        decompNode.addAttribute("id", decomposition.getID());
        decompNode.addContent(decomposition.toXML());
        String interaction = decomposition.requiresResourcingDecisions() ?
                "manual": "automated";
        decompNode.addChild("externalInteraction", interaction);
        String codelet = decomposition.getCodelet();
        if (codelet != null) {
            decompNode.addChild("codelet", codelet);
        }
        return decompNode.toString();
    }

}
