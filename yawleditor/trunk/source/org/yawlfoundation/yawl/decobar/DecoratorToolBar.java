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

package org.yawlfoundation.yawl.decobar;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.decobar.action.DecoratorAction;
import org.yawlfoundation.yawl.decobar.action.DecoratorPosAction;
import org.yawlfoundation.yawl.decobar.action.DecoratorTypeAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToggleToolBarButton;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * @author Michael Adams
 * @date 9/05/2014
 */
public class DecoratorToolBar extends JToolBar implements GraphStateListener,
        FileStateListener {

    private ButtonGroup _splitGroup = new ButtonGroup();
    private ButtonGroup _joinGroup = new ButtonGroup();


    public DecoratorToolBar() {
        super("Task Decorators", JToolBar.HORIZONTAL);
        setRollover(true);
        addSplitButtons();
        _splitGroup = addPositionButtons(DecoratorPosAction.SPLIT);
        addSeparator();
        addJoinButtons();
        _joinGroup = addPositionButtons(DecoratorPosAction.JOIN);

        // listen to graph changes
        Publisher.getInstance().subscribe(this);   // file state
        Publisher.getInstance().subscribe(this,
              Arrays.asList(GraphState.NoElementSelected,
                      GraphState.ElementsSelected,
                      GraphState.OneElementSelected));
    }


    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        if (event == null) return;
        YAWLVertex vertex = null;
        switch(state) {
            case NoElementSelected: {
                vertex = null;
                break;
            }
            case OneElementSelected:
            case ElementsSelected: {
                Object cell = event.getCell();
                if (cell instanceof VertexContainer) {
                    vertex = ((VertexContainer) cell).getVertex();
                }
                else if (cell instanceof YAWLVertex) {
                    vertex = (YAWLVertex) cell;
                }
            }
        }
        setTask(vertex);
    }


    public void specificationFileStateChange(FileState state) {
        if (state == FileState.Open) {
            for (Component c : getComponents()) {
                if (c instanceof YAWLToggleToolBarButton) {
                    YAWLToggleToolBarButton button = (YAWLToggleToolBarButton) c;
                    ((DecoratorAction) button.getAction()).setTask();
                }
            }
        }
    }


    private void addSplitButtons() {
        add(new YAWLToggleToolBarButton(new DecoratorTypeAction(
                DecoratorTypeAction.AND, DecoratorTypeAction.SPLIT, "and_split"
        )));
        add(new YAWLToggleToolBarButton(new DecoratorTypeAction(
                DecoratorTypeAction.XOR, DecoratorTypeAction.SPLIT, "xor_split"
        )));
        add(new YAWLToggleToolBarButton(new DecoratorTypeAction(
                DecoratorTypeAction.OR, DecoratorTypeAction.SPLIT, "or_split"
        )));
    }

    private void addJoinButtons() {
        add(new YAWLToggleToolBarButton(new DecoratorTypeAction(
                DecoratorTypeAction.AND, DecoratorTypeAction.JOIN, "and_join"
        )));
        add(new YAWLToggleToolBarButton(new DecoratorTypeAction(
                DecoratorTypeAction.XOR, DecoratorTypeAction.JOIN, "xor_join"
        )));
        add(new YAWLToggleToolBarButton(new DecoratorTypeAction(
                DecoratorTypeAction.OR, DecoratorTypeAction.JOIN, "or_join"
        )));
    }

    private ButtonGroup addPositionButtons(boolean isJoin) {
        ButtonGroup group = new ButtonGroup();
        group.add(new YAWLToggleToolBarButton(new DecoratorPosAction(
                DecoratorPosAction.NORTH, isJoin, "dec_north"
        )));
        group.add(new YAWLToggleToolBarButton(new DecoratorPosAction(
                DecoratorPosAction.EAST, isJoin, "dec_east"
        )));
        group.add(new YAWLToggleToolBarButton(new DecoratorPosAction(
                DecoratorPosAction.SOUTH, isJoin, "dec_south"
        )));
        group.add(new YAWLToggleToolBarButton(new DecoratorPosAction(
                DecoratorPosAction.WEST, isJoin, "dec_west"
        )));
        Enumeration<AbstractButton> buttons = group.getElements();
        while (buttons.hasMoreElements()) add(buttons.nextElement());
        return group;
    }

    private void setTask(Object cell) {
        setPosSelections(cell);
        for (Component c : getComponents()) {
            if (c instanceof YAWLToggleToolBarButton) {
                YAWLToggleToolBarButton button = (YAWLToggleToolBarButton) c;
                DecoratorAction action = (DecoratorAction) button.getAction();
                action.setTask(cell);
                button.setEnabled(action.isEnabled());
                button.setSelected(action.isSelected());
            }
        }
    }


    private void setPosSelections(Object cell) {
        boolean hasSplit = (cell instanceof YAWLTask) && ((YAWLTask) cell).hasSplitDecorator();
        boolean hasJoin = (cell instanceof YAWLTask) && ((YAWLTask) cell).hasJoinDecorator();
        if (! hasSplit) _splitGroup.clearSelection();
        if (! hasJoin) _joinGroup.clearSelection();
    }

}
