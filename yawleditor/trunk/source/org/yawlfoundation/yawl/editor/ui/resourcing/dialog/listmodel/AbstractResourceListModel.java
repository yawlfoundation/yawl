package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel;

import javax.swing.*;
import java.util.List;

/**
 * @author Michael Adams
 * @date 24/06/13
 */
public abstract class AbstractResourceListModel extends AbstractListModel {

    public abstract void filter(String chars);

    public abstract List<Object> getSelections(int[] selectedIndices);

}
