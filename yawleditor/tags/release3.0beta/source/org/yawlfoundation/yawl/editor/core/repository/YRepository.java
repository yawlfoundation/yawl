/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

    private final static YRepository INSTANCE = new YRepository();


    private YRepository() { }

    public static YRepository getInstance() {
        return INSTANCE;
    }


    public String getRepositoryDir() {
        return RepoMap.BACKINGSTORE_DIR;
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
