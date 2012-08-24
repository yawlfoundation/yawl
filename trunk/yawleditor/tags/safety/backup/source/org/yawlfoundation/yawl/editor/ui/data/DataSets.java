package org.yawlfoundation.yawl.editor.ui.data;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAtomicTask;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 27/06/12
 */
public class DataSets {

    private static final Map<String, DataVariableSet> _dataSets =
            new Hashtable<String, DataVariableSet>();


    public static void add(String id, DataVariableSet set) {
        _dataSets.put(id, set);
    }

    public static DataVariableSet get(String id) {
        DataVariableSet set = _dataSets.get(id);
        if (set == null) {
            set = new DataVariableSet();
            add(id, set);
        }
        return set;
    }

    public static DataVariableSet remove(String id) {
        return _dataSets.remove(id);
    }


    public static YAtomicTask getAtomicTask(YAWLTask task) {
        return SpecificationModel.getHandler().getAtomicTask(
                task.getDecomposition().getNet().getID(), task.getEngineId());
    }

}
