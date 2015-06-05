/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.decobar.action;

import org.yawlfoundation.yawl.decobar.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 9/05/2014
 */
public class DecoratorTypeAction extends YAWLBaseAction implements DecoratorAction {

    public static final String AND = "AND";
    public static final String XOR = "XOR";
    public static final String OR = "OR";
    public static final boolean SPLIT = false;
    public static final boolean JOIN = true;

    private String _type;
    private boolean _isJoin;
    private boolean _selected;


    public DecoratorTypeAction(String type, boolean isJoin, String iconName) {
        _type = type;
        _isJoin = isJoin;
        putValue(Action.SHORT_DESCRIPTION, getToolTipText());
        putValue(Action.SMALL_ICON, getMenuIcon(iconName));
        setEnabled(false);
    }


    public void actionPerformed(ActionEvent event) {
        _selected = ! _selected;
        String group = _isJoin ? "join" : "split";
        String type = _selected ? _type : "None";
        YAWLEditor.getPropertySheet().firePropertyChange(group, type);
    }

    public void setTask(Object cell) {
        YAWLTask task = (cell instanceof YAWLTask) ? (YAWLTask) cell : null;
        setEnabled(task != null);
        _selected = shouldBeSelected(task);
    }

    public void setTask() {
        NetGraph graph = getGraph();
        if (graph != null) setTask(graph.getSelectionCell());
    }

    public boolean isSelected() { return _selected; }


    protected String getToolTipText() {
        return _type + (_isJoin ? " join" : " split");
    }

    protected ImageIcon getMenuIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconName + ".png");
    }


    private boolean shouldBeSelected(YAWLTask task) {
        return task != null && (_isJoin ?
               task.hasJoinDecorator() && matchesType(task.getJoinDecorator().getType()) :
               task.hasSplitDecorator() && matchesType(task.getSplitDecorator().getType()));
    }


    private boolean matchesType(int type) {
        switch (type) {
            case Decorator.AND_TYPE : return _type.equals(AND);
            case Decorator.OR_TYPE : return _type.equals(OR);
            case Decorator.XOR_TYPE : return _type.equals(XOR);
        }
        return false;
    }
}
