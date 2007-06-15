package au.edu.qut.yawl.editor.swing.menu;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import au.edu.qut.yawl.editor.actions.palette.AtomicTaskAction;
import au.edu.qut.yawl.editor.actions.palette.CompositeTaskAction;
import au.edu.qut.yawl.editor.actions.palette.ConditionAction;
import au.edu.qut.yawl.editor.actions.palette.MarqueeAction;
import au.edu.qut.yawl.editor.actions.palette.MultipleAtomicTaskAction;
import au.edu.qut.yawl.editor.actions.palette.MultipleCompositeTaskAction;
import au.edu.qut.yawl.editor.actions.palette.NetDragAction;

public class ControlFlowPalette extends JPanel {
  private static final long serialVersionUID = 1L;
  
  public static enum SelectionState {
    ATOMIC_TASK,
    COMPOSITE_TASK,
    MULTIPLE_ATOMIC_TASK,
    MULTIPLE_COMPOSITE_TASK,
    CONDITION,
    DRAG,
    MARQUEE
  };
  
  private boolean enabledState = true;
  
  private ControlFlowPaletteButton[] buttons = {
      new ControlFlowPaletteButton(
          this,
          new AtomicTaskAction(this),
          KeyEvent.VK_1
      ),
      new ControlFlowPaletteButton(
          this,
          new CompositeTaskAction(this),
          KeyEvent.VK_2
      ),
      new ControlFlowPaletteButton(
          this,
          new MultipleAtomicTaskAction(this),
          KeyEvent.VK_3
      ),
      new ControlFlowPaletteButton(
          this,
          new MultipleCompositeTaskAction(this),
          KeyEvent.
          VK_4
      ),
      new ControlFlowPaletteButton(
          this,
          new ConditionAction(this),
          KeyEvent.VK_5
      ),
      new ControlFlowPaletteButton(
          this,
          new NetDragAction(this),
          KeyEvent.VK_6
      ),
      new ControlFlowPaletteButton(
          this,
          new MarqueeAction(this),
          KeyEvent.VK_7
      )
  };
  
  public ControlFlowPalette() {
    buildInterface();
    setSelectedState(SelectionState.MARQUEE);
    ButtonGroup paletteButtons = new ButtonGroup();
    for(ControlFlowPaletteButton button: buttons) {
      paletteButtons.add(button);
    }
  }

  protected void buildInterface() {
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    add(Box.createHorizontalGlue());
    
    gbc.gridx++;
    gbc.weightx = 0;
    add(buildButtons(),gbc);

    gbc.gridx++;
    gbc.weightx = 0.5;
    add(Box.createHorizontalGlue());
  }
  
  private JPanel buildButtons() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(4,2));

    for (int i = 0; i < buttons.length; i++) {
      buttons[i].setMargin(new Insets(3,3,3,3));
      buttonPanel.add(buttons[i]);      
    }
    
    return buttonPanel;
  }
  
  public void setSelectedState(SelectionState newState) {
    getButtonWithSelectionState(newState).setSelected(true);
  }
  
  public SelectionState getSelectedState() {
    return getSelectedButton().getSelectionID();
  }
  
  private ControlFlowPaletteButton getSelectedButton() {
    for(ControlFlowPaletteButton button: buttons) {
      if (button.isSelected()) {
        return button;
      }
    }
    return null;
  }
  
  private ControlFlowPaletteButton getButtonWithSelectionState(SelectionState state) {
    for(ControlFlowPaletteButton button: buttons) {
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