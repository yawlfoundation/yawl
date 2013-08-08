package org.yawlfoundation.yawl.editor.core.repository;

/**
 * Manages a file based repository of task decompositions, net decompositions,
 * extended variables and data definitions for re-use across different specifications
 * @author Michael Adams
 * @date 17/06/12
 */
public class YRepository {

    // the 'maps' that store each repository (in memory and to/from file)
    private TaskDecompositionRepository _taskDecompositionMap;
    private NetRepository _netDecompositionMap;
    private ExtendedAttributesRepository _extendedAttributesMap;
    private DataDefinitionRepository _dataDefinitionMap;

    private static YRepository INSTANCE;


    private YRepository() { }

    public static YRepository getInstance() {
        if (INSTANCE == null) INSTANCE = new YRepository();
        return INSTANCE;
    }


    public TaskDecompositionRepository getTaskDecompositionRepository() {
        if (_taskDecompositionMap == null) {
            _taskDecompositionMap = new TaskDecompositionRepository();
        }
        return _taskDecompositionMap;
    }

    public NetRepository getNetRepository() {
        if (_netDecompositionMap == null) {
            _netDecompositionMap = new NetRepository();
        }
        return _netDecompositionMap;
    }

    public ExtendedAttributesRepository getExtendedAttributesRepository() {
        if (_extendedAttributesMap == null) {
             _extendedAttributesMap = new ExtendedAttributesRepository();
        }
        return _extendedAttributesMap;
    }

    public DataDefinitionRepository getDataDefinitionRepository() {
        if (_dataDefinitionMap == null) {
            _dataDefinitionMap = new DataDefinitionRepository();
        }
        return _dataDefinitionMap;
    }

    public RepoMap getRepository(Repo repo) {
        switch (repo) {
            case TaskDecomposition:  return getTaskDecompositionRepository();
            case NetDecomposition:   return getNetRepository();
            case ExtendedAttributes: return getExtendedAttributesRepository();
            case DataDefinition:     return getDataDefinitionRepository();
        }
        return null;
    }
}
