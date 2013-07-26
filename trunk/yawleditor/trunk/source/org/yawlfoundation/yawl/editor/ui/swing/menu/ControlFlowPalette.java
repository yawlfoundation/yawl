package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.palette.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

public class ControlFlowPalette extends JPanel {

    private java.util.List<ControlFlowPaletteListener> paletteListeners =
            new LinkedList<ControlFlowPaletteListener>();

    public static enum SelectionState {
        ATOMIC_TASK,
        MULTIPLE_ATOMIC_TASK,
        COMPOSITE_TASK,
        MULTIPLE_COMPOSITE_TASK,
        CONDITION,
        DRAG,
        MARQUEE
    }

    private boolean enabledState = true;

    private ControlFlowPaletteButton[] buttons = {
        new ControlFlowPaletteButton(this, new AtomicTaskAction(this), KeyEvent.VK_1),
        new ControlFlowPaletteButton(this, new MultipleAtomicTaskAction(this), KeyEvent.VK_2),
        new ControlFlowPaletteButton(this, new ConditionAction(this), KeyEvent.VK_3),
        new ControlFlowPaletteButton(this, new CompositeTaskAction(this), KeyEvent.VK_4),
        new ControlFlowPaletteButton(this, new MultipleCompositeTaskAction(this), KeyEvent.VK_5),
        new ControlFlowPaletteButton(this, new MarqueeAction(this), KeyEvent.VK_6),
        new ControlFlowPaletteButton(this, new NetDragAction(this), KeyEvent.VK_7)
    };

    public ControlFlowPalette() {
        buildInterface();
        setSelectedState(SelectionState.MARQUEE);
        ButtonGroup paletteButtons = new ButtonGroup();
        for(ControlFlowPaletteButton button: buttons) {
            paletteButtons.add(button);
        }
    }

    public void subscribeForSelectionStateChanges(ControlFlowPaletteListener listener) {
        paletteListeners.add(listener);
        listener.controlFlowPaletteStateChanged(getSelectedState());
    }

    private void publishSelectionState() {
        for(ControlFlowPaletteListener listener: paletteListeners) {
            listener.controlFlowPaletteStateChanged(getSelectedState());
        }
    }

    protected void buildInterface() {
        add(buildButtons());
        setBorder(BorderFactory.createTitledBorder("Palette"));
    }

    private JPanel buildButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
        for (ControlFlowPaletteButton button : buttons) {
            button.setMargin(new Insets(2,2,2,2));
            buttonPanel.add(button);
        }
        return buttonPanel;
    }

    public void setSelectedState(SelectionState newState) {
        getButtonWithSelectionState(newState).setSelected(true);
        publishSelectionState();
    }

    public SelectionState getSelectedState() {
        return getSelectedButton().getSelectionID();
    }

    private ControlFlowPaletteButton getSelectedButton() {
        for (ControlFlowPaletteButton button: buttons) {
            if (button.isSelected()) {
                return button;
            }
        }
        return null;
    }

    private ControlFlowPaletteButton getButtonWithSelectionState(SelectionState state) {
        for (ControlFlowPaletteButton button: buttons) {
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
        for (ControlFlowPaletteButton button: buttons) {
            button.setEnabled(state);
        }
        setVisible(true);
        enabledState = state;
    }
}