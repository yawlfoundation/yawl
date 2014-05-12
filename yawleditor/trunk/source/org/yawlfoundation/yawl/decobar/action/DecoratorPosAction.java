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
public class DecoratorPosAction extends YAWLBaseAction implements DecoratorAction {

    public static final String NORTH = "North";
    public static final String SOUTH = "South";
    public static final String EAST = "East";
    public static final String WEST = "West";
    public static final boolean SPLIT = false;
    public static final boolean JOIN = true;


    private String _pos;
    private boolean _isJoin;
    private boolean _selected;


    public DecoratorPosAction(String pos, boolean isJoin, String iconName) {
        _pos = pos;
        _isJoin = isJoin;
        putValue(Action.SHORT_DESCRIPTION, getToolTipText());
        putValue(Action.SMALL_ICON, getPNGIcon(iconName));
    }


    public void actionPerformed(ActionEvent event) {
        if (! _selected) {
            String group = _isJoin ? "joinPosition" : "splitPosition";
            YAWLEditor.getPropertySheet().firePropertyChange(group, _pos);
        }
    }

    public void setTask(Object cell) {
        YAWLTask task = (cell instanceof YAWLTask) ? (YAWLTask) cell : null;
        setEnabled(shouldBeEnabled(task));
        _selected = shouldBeSelected(task);
    }

    public void setTask() {
        NetGraph graph = getGraph();
        if (graph != null) setTask(graph.getSelectionCell());
    }

    public boolean isSelected() { return _selected; }


    protected String getToolTipText() {
        return _pos + " position";
    }

    protected ImageIcon getPNGIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconName + ".png");
    }

    private boolean shouldBeSelected(YAWLTask task) {
        return isEnabled() && task != null && (_isJoin ?
               task.hasJoinDecorator() && matchesPosition(
                       task.getJoinDecorator().getCardinalPosition()) :
               task.hasSplitDecorator() && matchesPosition(
                       task.getSplitDecorator().getCardinalPosition()));
    }


    private boolean shouldBeEnabled(YAWLTask task) {
        if (task != null) {
            if (_isJoin && task.hasJoinDecorator()) {
                return !(task.hasSplitDecorator() &&
                        matchesPosition(task.getSplitDecoratorPos()));
            }
            if (!_isJoin && task.hasSplitDecorator()) {
                return !(task.hasJoinDecorator() &&
                        matchesPosition(task.getJoinDecoratorPos()));
            }
        }
        return false;
    }


    private boolean matchesPosition(int pos) {
        switch (pos) {
            case Decorator.TOP : return _pos.equals(NORTH);
            case Decorator.BOTTOM : return _pos.equals(SOUTH);
            case Decorator.LEFT : return _pos.equals(WEST);
            case Decorator.RIGHT : return _pos.equals(EAST);
        }
        return false;
    }
}
