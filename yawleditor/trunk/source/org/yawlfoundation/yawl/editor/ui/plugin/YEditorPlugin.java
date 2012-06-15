package org.yawlfoundation.yawl.editor.ui.plugin;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.actions.specification.YAWLOpenSpecificationAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;

/**
 * @author Michael Adams
 * @date 24/04/12
 */
public interface YEditorPlugin {

    String getPluginName();

    String getPluginDescription();

    YAWLOpenSpecificationAction getSpecificationMenuAction();

    YAWLSelectedNetAction getElementsMenuAction();

    YAWLSelectedNetAction getPopupMenuAction();

    boolean setPopupMenuItemEnabled(YAWLCell cell);

    boolean setElementsMenuItemEnabled(YAWLCell cell);

}
