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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.palette.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Palette extends JPanel {

    private final java.util.List<PaletteListener> paletteListeners = new ArrayList<PaletteListener>();

    public enum SelectionState {
        ATOMIC_TASK,
        MULTIPLE_ATOMIC_TASK,
        COMPOSITE_TASK,
        MULTIPLE_COMPOSITE_TASK,
        CONDITION,
        MARQUEE
    }

    private boolean enabledState = true;

    private final PaletteButton[] buttons = {
        new PaletteButton(this, new AtomicTaskAction(), KeyEvent.VK_1),
        new PaletteButton(this, new MultipleAtomicTaskAction(), KeyEvent.VK_2),
        new PaletteButton(this, new ConditionAction(), KeyEvent.VK_3),
        new PaletteButton(this, new CompositeTaskAction(), KeyEvent.VK_4),
        new PaletteButton(this, new MultipleCompositeTaskAction(), KeyEvent.VK_5),
        new PaletteButton(this, new MarqueeAction(), KeyEvent.VK_6)
    };


    public Palette() {
        buildInterface();
        setSelectedState(SelectionState.MARQUEE);
        ButtonGroup paletteButtons = new ButtonGroup();
        for (PaletteButton button: buttons) {
            paletteButtons.add(button);
        }
    }

    public void subscribeForSelectionStateChanges(PaletteListener listener) {
        paletteListeners.add(listener);
        listener.PaletteStateChanged(getSelectedState());
    }

    private void publishSelectionState() {
        for(PaletteListener listener: paletteListeners) {
            listener.PaletteStateChanged(getSelectedState());
        }
    }

    protected void buildInterface() {
        add(buildButtons());
        setBorder(BorderFactory.createTitledBorder("Palette"));
    }

    private JPanel buildButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
        for (PaletteButton button : buttons) {
            button.setMargin(new Insets(2,2,2,2));
            buttonPanel.add(button);
        }
        return buttonPanel;
    }

    public void setSelectedState(SelectionState newState) {
        PaletteButton button = getButtonWithSelectionState(newState);
        if (button != null) {
            button.setSelected(true);
            publishSelectionState();
        }
    }

    public SelectionState getSelectedState() {
        PaletteButton button = getSelectedButton();
        return button != null ? button.getSelectionID() : SelectionState.MARQUEE;
    }

    private PaletteButton getSelectedButton() {
        for (PaletteButton button: buttons) {
            if (button.isSelected()) {
                return button;
            }
        }
        return null;
    }

    private PaletteButton getButtonWithSelectionState(SelectionState state) {
        for (PaletteButton button: buttons) {
            if (button.getSelectionID() == state) {
                return button;
            }
        }
        return null;
    }

    public void setEnabled(boolean state) {
        if (enabledState == state) {
            return;
        }
        setVisible(false);
        for (PaletteButton button: buttons) {
            button.setEnabled(state);
        }
        setVisible(true);
        enabledState = state;
    }
}