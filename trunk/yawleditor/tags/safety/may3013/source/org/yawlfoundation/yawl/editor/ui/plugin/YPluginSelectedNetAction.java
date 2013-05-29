package org.yawlfoundation.yawl.editor.ui.plugin;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;

/**
 * @author Michael Adams
 * @date 19/06/12
 */
public class YPluginSelectedNetAction extends YAWLSelectedNetAction {

    public boolean enabled(YAWLCell cell) {
        return true;
    }

    public boolean visible(YAWLCell cell) {
        return true;
    }
}
