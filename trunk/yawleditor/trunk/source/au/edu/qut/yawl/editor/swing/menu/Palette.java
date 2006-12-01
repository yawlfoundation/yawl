/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.swing.menu;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jgraph.event.GraphSelectionEvent;

import au.edu.qut.yawl.editor.actions.YAWLBaseAction;
import au.edu.qut.yawl.editor.actions.palette.*;
import au.edu.qut.yawl.editor.elements.model.AtomicTask;
import au.edu.qut.yawl.editor.elements.model.CompositeTask;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.MultipleAtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleCompositeTask;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.foundations.ResourceLoader;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.specification.SpecificaitonModelListener;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.specification.SpecificationSelectionListener;
import au.edu.qut.yawl.editor.specification.SpecificationSelectionSubscriber;
import au.edu.qut.yawl.editor.swing.JStatusBar;
import au.edu.qut.yawl.editor.swing.JUtilities;

public class Palette extends YAWLToolBar implements SpecificaitonModelListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public static final int ATOMIC_TASK             = 0;
  public static final int COMPOSITE_TASK          = 1;
  public static final int MULTIPLE_ATOMIC_TASK    = 2;
  public static final int MULTIPLE_COMPOSITE_TASK = 3;
  public static final int FLOW_RELATION           = 4;
  public static final int CONDITION               = 5;
  public static final int MARQUEE                 = 6;
  public static final int DRAG                    = 7;

  private static final CorePalette CORE_PALETTE = new CorePalette();
  private static final SingleTaskPalette SINGLE_TASK_PALETTE = new SingleTaskPalette();
  
  private static final Palette INSTANCE = new Palette();

  public static Palette getInstance() {
    return INSTANCE;
  }  
    
  private Palette() {
    super("YAWLEditor Palette");
    SpecificationModel.getInstance().subscribe(this);   
  }  

  protected void buildInterface() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setMargin(new Insets(3,2,2,3));
    
    add(CORE_PALETTE);
    add(Box.createVerticalStrut(2));
    add(SINGLE_TASK_PALETTE);
    
    LinkedList<JComponent> palettes = new LinkedList<JComponent>();
    palettes.add(CORE_PALETTE);
    palettes.add(SINGLE_TASK_PALETTE);
    
    JUtilities.equalizeComponentWidths(palettes);
  }
  
  public void setSelected(int item) {
    CORE_PALETTE.setSelected(item);
  }
  
  public int getSelected() {
    return CORE_PALETTE.getSelected();
  }
  
  public void setEnabled(boolean enabled) {
    CORE_PALETTE.setEnabled(enabled);
    SINGLE_TASK_PALETTE.setVisible(enabled);
    SINGLE_TASK_PALETTE.setEnabled(enabled);
    super.setEnabled(enabled);
  }
  
  public void updateState(int state) {
    switch(state) {
      case SpecificationModel.NO_NETS_EXIST: {
        CORE_PALETTE.setSelected(MARQUEE);
        setEnabled(false);
        JStatusBar.getInstance().setStatusText("Open or create a specification to begin.");     
        break;    
      }
      case SpecificationModel.NETS_EXIST: {
        JStatusBar.getInstance().setStatusText("Select a net to continue editing it.");     
        break;    
      }
      case SpecificationModel.NO_NET_SELECTED: {
        JStatusBar.getInstance().setStatusText("Select a net to continue editing it.");     
        setEnabled(false);
        break;
      }
      case SpecificationModel.SOME_NET_SELECTED: {
        JStatusBar.getInstance().setStatusText("Use the palette toolbar to edit the selected net.");     
        setEnabled(true);
        break;
      }
      default: {
        assert false : "Invalid state passed to updateState()";   
      }    
    }
  }
}

class CorePalette extends JPanel {
  private static int selectedItem = Palette.MARQUEE;
  
  private boolean enabledState = true;
  
  public CorePalette() {
    buildInterface();
  }

  private static YAWLPaletteButton[] buttons = {
    new YAWLPaletteButton(new AtomicTaskAction(),KeyEvent.VK_1),
    new YAWLPaletteButton(new CompositeTaskAction(),KeyEvent.VK_2),
    new YAWLPaletteButton(new MultipleAtomicTaskAction(),KeyEvent.VK_3),
    new YAWLPaletteButton(new MultipleCompositeTaskAction(),KeyEvent.VK_4),
    new YAWLPaletteButton(new FlowRelationAction(),KeyEvent.VK_5),
    new YAWLPaletteButton(new ConditionAction(),KeyEvent.VK_6),
    new YAWLPaletteButton(new MarqueeAction(),KeyEvent.VK_7),
    new YAWLPaletteButton(new NetDragAction(),KeyEvent.VK_8),
  };

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
  
  public void setSelected(int item) {
    assert item >= Palette.CONDITION && item <= Palette.MARQUEE : "Invalid item selection made.";
    buttons[selectedItem].setSelected(false);
    selectedItem = item;
    buttons[selectedItem].setSelected(true);   
    JStatusBar.getInstance().setStatusText(getItemText(item));
  }
  
  private String getItemText(int item) {
    switch (item) {
      case Palette.MARQUEE: {
        return "Select a number of net elements to manipulate.";     
      }
      case Palette.CONDITION: {
        return getClickAnywhereText() + "condition.";
      }
      case Palette.ATOMIC_TASK: {
        return getClickAnywhereText() + "atomic task.";
      }
      case Palette.COMPOSITE_TASK: {
        return getClickAnywhereText() + "composite task.";
      }
      case Palette.FLOW_RELATION: {
        return "Draw a flow relation between two elements. The cursor will hilight where valid connections can be made.";
      }
      case Palette.MULTIPLE_ATOMIC_TASK: {
        return getClickAnywhereText() + "multiple atomic task.";
      }
      case Palette.MULTIPLE_COMPOSITE_TASK: {
        return getClickAnywhereText() + "multiple composite task.";
      }
      case Palette.DRAG: {
        return "Drag visible window to another area of the net.";     
      }
      default: {
        return "You should never see this palette message!";
      }
    } 
  }
  
  private String getClickAnywhereText() {
    return "Click anywhere on the selected net to create a new ";    
  }
  
  public int getSelected() {
    return selectedItem;
  }
  
  public void setEnabled(boolean state) {
    if (enabledState == state) {
      return;
    }
    setVisible(false);
    for (int i = 0; i < buttons.length; i++) {
      buttons[i].setEnabled(state);      
    }
    if (state == true) {
      JStatusBar.getInstance().setStatusText(getItemText(getSelected()));
    }
    setVisible(true);
    enabledState = state;
  }
}

class SingleTaskPalette extends JTabbedPane implements SpecificationSelectionSubscriber{

  private JoinPanel joinPanel;
  private SplitPanel splitPanel;
  
  public SingleTaskPalette() {
    joinPanel = new JoinPanel(this);
    splitPanel = new SplitPanel(this);
    
    addTab("Join", joinPanel);
    addTab("Split", splitPanel);

    setVisible(false);

    SpecificationSelectionListener.getInstance().subscribe(
         this,
         new int[] { 
           SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
           SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED,
           SpecificationSelectionListener.STATE_SINGLE_TASK_SELECTED
         }
     );
  }
  
  public void refreshComponentValidity() {
    joinPanel.updateWidgetConfiguration();
    splitPanel.updateWidgetConfiguration();
  }

  class JoinPanel extends DecoratorPanel {
    public JoinPanel(SingleTaskPalette parent) {
      super(parent);
    }
    
    protected String getNoDecorationIconName() {
      return "PaletteNoJoin";
    }
    
    protected String getAndDecorationIconName() {
      return "PaletteAndJoin";
    }
    
    protected String getXorDecorationIconName() {
      return "PaletteXorJoin";
    }

    protected String getOrDecorationIconName() {
      return "PaletteOrJoin";
    }

    public void setTask(Object task) {
      super.setTask(task);
      
      if (getTask().getJoinDecorator() != null) {
        this.selectedType = getTask().getJoinDecorator().getType();
      } else {
        this.selectedType = Decorator.NO_TYPE;
      }
      
      this.selectedPosition = getTask().hasJoinObjectAt();

      if (this.selectedPosition == Decorator.NOWHERE) {
        if (getTask().hasSplitObjectAt() != Decorator.LEFT) {
          this.selectedPosition = Decorator.LEFT;
        } else {
          this.selectedPosition = Decorator.RIGHT;
        }
      }
      
      updateWidgetConfiguration();
    }
    
    public void updateWidgetConfiguration() {
      typeButtons[TYPE_NONE].setEnabled(true);
      enableAllPositionButtons();

      if (getTask().getJoinDecorator() == null) {
        doTypeSelection(TYPE_NONE);
      } else {
        switch(getTask().getJoinDecorator().getType()) {
          case Decorator.AND_TYPE: {
            doTypeSelection(TYPE_AND);
            break;
          }
          case Decorator.XOR_TYPE: {
            doTypeSelection(TYPE_XOR);
            break;
          }
          case Decorator.OR_TYPE: {
            doTypeSelection(TYPE_OR);
            break;
          }
        }
        if (getTask().getIncommingFlowCount() > 1) {
          typeButtons[TYPE_NONE].setEnabled(false);
        }
      }

      setPositionDisabled(getTask().hasSplitObjectAt());
      doPositionSelection(this.selectedPosition);

      if (selectedType == Decorator.NO_TYPE) {
        disableAllPositionButtons();
      }
    }
    
    protected void applyDecoration() {
      getNet().setJoinDecorator(
          getTask(), 
          selectedType, 
          selectedPosition
      );
      selectEntireTask();
      parent.refreshComponentValidity();
    }
    
    protected String getDecoratorString() {
      return "Join";
    }
  }

  class SplitPanel extends DecoratorPanel {
    
    
    public SplitPanel(SingleTaskPalette parent) {
      super(parent);
    }
    
    protected String getNoDecorationIconName() {
      return "PaletteNoSplit";
    }

    protected String getAndDecorationIconName() {
      return "PaletteAndSplit";
    }

    protected String getXorDecorationIconName() {
      return "PaletteXorSplit";
    }

    protected String getOrDecorationIconName() {
      return "PaletteOrSplit";
    }
    
    public void setTask(Object task) {
      super.setTask(task);
      
      if (getTask().getSplitDecorator() != null) {
        this.selectedType = getTask().getSplitDecorator().getType();
      } else {
        this.selectedType = Decorator.NO_TYPE;
      }
      
      this.selectedPosition = getTask().hasSplitObjectAt();
      
      if (this.selectedPosition == Decorator.NOWHERE) {
        if (getTask().hasJoinObjectAt() != Decorator.RIGHT) {
          this.selectedPosition = Decorator.RIGHT;
        } else {
          this.selectedPosition = Decorator.LEFT;
        }
      }
      
      updateWidgetConfiguration();      
    }
    
    public void updateWidgetConfiguration() {
      typeButtons[TYPE_NONE].setEnabled(true);
      enableAllPositionButtons();

      if (getTask().getSplitDecorator() == null) {
        doTypeSelection(TYPE_NONE);
      } else {
        switch(getTask().getSplitDecorator().getType()) {
          case Decorator.AND_TYPE: {
            doTypeSelection(TYPE_AND);
            break;
          }
          case Decorator.XOR_TYPE: {
            doTypeSelection(TYPE_XOR);
            break;
          }
          case Decorator.OR_TYPE: {
            doTypeSelection(TYPE_OR);
            break;
          }
        }
        if (getTask().getOutgoingFlowCount() > 1) {
          typeButtons[TYPE_NONE].setEnabled(false);
        }
      }

      setPositionDisabled(getTask().hasJoinObjectAt());
      doPositionSelection(this.selectedPosition);

      if (selectedType == Decorator.NO_TYPE) {
        disableAllPositionButtons();
      }
    }
    
    protected void applyDecoration() {
      getNet().setSplitDecorator(
          getTask(), 
          selectedType, 
          selectedPosition
      );
      selectEntireTask();
      parent.refreshComponentValidity();
    }
    
    protected String getDecoratorString() {
      return "Split";
    }
  }
  

  abstract class DecoratorPanel extends JPanel {
    
    private NetGraph net;
    private YAWLTask task;
    
    protected JRadioButton northRadioButton;
    protected JRadioButton southRadioButton;
    protected JRadioButton eastRadioButton;
    protected JRadioButton westRadioButton;

    protected JRadioButton nowhereRadioButton = new JRadioButton();

    private ButtonGroup positionButtonGroup = new ButtonGroup();
    private ButtonGroup typeButtonGroup = new ButtonGroup();
    
    protected static final int TYPE_NONE = 0;
    protected static final int TYPE_AND  = 1;
    protected static final int TYPE_XOR  = 2;
    protected static final int TYPE_OR   = 3;
    
    protected JToggleButton[] typeButtons;
    
    protected int selectedType = Decorator.NO_TYPE;
    protected int selectedPosition = YAWLTask.NOWHERE;
    
    private JLabel taskLabel;
    
    protected SingleTaskPalette parent;
    
    public DecoratorPanel(SingleTaskPalette parent) {
      this.parent = parent;
      setBorder(new EmptyBorder(2,2,2,2));
      buildContent();
    }
    
    private void buildContent() {
      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();

      setLayout(gbl);

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridwidth = 3;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets = new Insets(0,0,4,0);
      
      add(buildDecoratorButtonPanel(), gbc);

      gbc.gridy++;
      gbc.insets = new Insets(0,0,0,0);
      add(buildNorthRadioButton(), gbc);

      gbc.gridy++;
      gbc.gridwidth = 1;
      gbc.weightx = 0.5;
      gbc.anchor = GridBagConstraints.EAST;
      add(buildWestRadioButton(), gbc);

      gbc.gridx++;
      gbc.weightx = 0;
      gbc.anchor = GridBagConstraints.CENTER;
      add(buildTaskLabel(), gbc);

      gbc.gridx++;
      gbc.weightx = 0.5;
      gbc.anchor = GridBagConstraints.WEST;
      add(buildEastRadioButton(), gbc);

      gbc.gridx = 0;
      gbc.gridy++;
      gbc.gridwidth = 3;
      gbc.weightx = 0;
      gbc.anchor = GridBagConstraints.CENTER;
      add(buildSouthRadioButton(), gbc);
      
      positionButtonGroup.add(northRadioButton);
      positionButtonGroup.add(southRadioButton);
      positionButtonGroup.add(eastRadioButton);
      positionButtonGroup.add(westRadioButton);
      positionButtonGroup.add(nowhereRadioButton);
    }
    
    protected void selectEntireTask() {
      if (getTask().getParent() != null) {
        getNet().setSelectionCell(getTask().getParent());
      } else {
        getNet().setSelectionCell(getTask());
      }
    }

    private JLabel buildTaskLabel() {
      taskLabel = new JLabel(
          ResourceLoader.getImageAsIcon(
              "/au/edu/qut/yawl/editor/resources/menuicons/" 
              + "PaletteAtomicTask" + "24.gif"
          )
      );
      return taskLabel;
    }
    
    private JRadioButton buildNorthRadioButton() {
      northRadioButton = new JRadioButton();
      northRadioButton.setHorizontalAlignment(
          SwingConstants.CENTER
      );
      northRadioButton.setVerticalAlignment(
          SwingConstants.BOTTOM
      );
      northRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            selectedPosition = YAWLTask.TOP;
            applyDecoration();
          }
        }
      );
      northRadioButton.setToolTipText(
          " Places the " + getDecoratorString().toLowerCase() + 
          " at the top of the task. "
      );
      return northRadioButton;
    }
    
    private JRadioButton buildSouthRadioButton() {
      southRadioButton = new JRadioButton();
      southRadioButton.setHorizontalAlignment(
          SwingConstants.CENTER
      );
      southRadioButton.setVerticalAlignment(
          SwingConstants.TOP
      );
      southRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            selectedPosition = YAWLTask.BOTTOM;
            applyDecoration();
          }
        }
      );
      southRadioButton.setToolTipText(
          " Places the " + getDecoratorString().toLowerCase() + 
          " at the bottom of the task. "
      );
      return southRadioButton;
    }
    
    private JRadioButton buildEastRadioButton() {
      eastRadioButton = new JRadioButton();
      eastRadioButton.setHorizontalAlignment(
          SwingConstants.RIGHT
      );
      eastRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            selectedPosition = YAWLTask.RIGHT;
            applyDecoration();
          }
        }
      );
      eastRadioButton.setToolTipText(
          " Places the " + getDecoratorString().toLowerCase() + 
          " on the right side of the task. "
      );
      return eastRadioButton;
    }
    
    private JRadioButton buildWestRadioButton() {
      westRadioButton = new JRadioButton();
      westRadioButton.setHorizontalAlignment(
          SwingConstants.LEFT
      );
      westRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            selectedPosition = YAWLTask.LEFT;
            applyDecoration();
          }
        }
      );
      westRadioButton.setToolTipText(
          " Places the " + getDecoratorString().toLowerCase() + 
          " on the left side of the task. "
      );
      return westRadioButton;
    }
    
    public void setTask(Object task) {
      if (task instanceof VertexContainer) {
        this.task = (YAWLTask) ((VertexContainer) task).getVertex();
      } else {
        this.task = (YAWLTask) task;
      }
      if (this.task instanceof AtomicTask) {
        setTaskLabel("PaletteAtomicTask");
      }
      if (this.task instanceof MultipleAtomicTask) {
        setTaskLabel("PaletteMultipleAtomicTask");
      }
      if (this.task instanceof CompositeTask) {
        setTaskLabel("PaletteCompositeTask");
      }
      if (this.task instanceof MultipleCompositeTask) {
        setTaskLabel("PaletteMultipleCompositeTask");
      }
    }
    
    public YAWLTask getTask() {
      return this.task;
    }
    
    private void setTaskLabel(String labelImageName) {
      taskLabel.setIcon(
          ResourceLoader.getImageAsIcon(
              "/au/edu/qut/yawl/editor/resources/menuicons/" 
              + labelImageName + "24.gif"
          )
      );
    }
    
    private JPanel buildDecoratorButtonPanel() {
      JPanel panel = new JPanel();
      
      panel.setLayout(new GridLayout(2,2));

      typeButtons = new JToggleButton[] {
        new JToggleButton(new NoDecoratorAction()),
        new JToggleButton(new AndDecoratorAction()),
        new JToggleButton(new XorDecoratorAction()),
        new JToggleButton(new OrDecoratorAction()),
      };
      
      for (int i = 0; i < typeButtons.length; i++) {
        typeButtons[i].setVerticalTextPosition(AbstractButton.BOTTOM);
        typeButtons[i].setHorizontalTextPosition(AbstractButton.CENTER);
        typeButtons[i].setMargin(new Insets(2,5,2,5));
        typeButtonGroup.add(typeButtons[i]);
        
        panel.add(typeButtons[i]);   
      }
      
      return panel;
    }
    
    protected void enableAllPositionButtons() {
      setAllPositionButtonEnablement(true);
    }
    
    protected void disableAllPositionButtons() {
      setAllPositionButtonEnablement(false);
    }
    
    protected void setAllPositionButtonEnablement(boolean enabled) {
      northRadioButton.setEnabled(enabled);
      southRadioButton.setEnabled(enabled);
      eastRadioButton.setEnabled(enabled);
      westRadioButton.setEnabled(enabled);
    }

    class NoDecoratorAction extends YAWLBaseAction {
      {
        putValue(Action.SHORT_DESCRIPTION, " No " + getDecoratorString() + " ");
        putValue(Action.NAME, "NONE");
        putValue(Action.LONG_DESCRIPTION, " No " + getDecoratorString() + " ");
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/au/edu/qut/yawl/editor/resources/menuicons/" 
                + getNoDecorationIconName() + "16.gif"
            )
        );
      }
      
      public void actionPerformed(ActionEvent event) {
          selectedType = Decorator.NO_TYPE;
          applyDecoration();
      }
    }
    
    class AndDecoratorAction extends YAWLBaseAction {
      {
        putValue(Action.SHORT_DESCRIPTION, " AND " + getDecoratorString() + " ");
        putValue(Action.NAME, "AND");
        putValue(Action.LONG_DESCRIPTION, " AND " + getDecoratorString() + " ");
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/au/edu/qut/yawl/editor/resources/menuicons/" 
                + getAndDecorationIconName() + "16.gif"
            )
        );
      }
      
      public void actionPerformed(ActionEvent event) {
        selectedType = Decorator.AND_TYPE;
        applyDecoration();
      }
    }
    
    class XorDecoratorAction extends YAWLBaseAction {
      {
        putValue(Action.SHORT_DESCRIPTION, " XOR " + getDecoratorString() + " ");
        putValue(Action.NAME, "XOR");
        putValue(Action.LONG_DESCRIPTION, " XOR " + getDecoratorString() + " ");
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/au/edu/qut/yawl/editor/resources/menuicons/" 
                + getXorDecorationIconName() + "16.gif"
            )
        );
      }

      public void actionPerformed(ActionEvent event) {
        selectedType = Decorator.XOR_TYPE;
        applyDecoration();
      }
    }
   
    class OrDecoratorAction extends YAWLBaseAction {
      {
        putValue(Action.SHORT_DESCRIPTION, "OR " + getDecoratorString());
        putValue(Action.NAME, "OR");
        putValue(Action.LONG_DESCRIPTION, "OR " + getDecoratorString());
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/au/edu/qut/yawl/editor/resources/menuicons/" 
                + getOrDecorationIconName() + "16.gif"
            )
        );
      }

      public void actionPerformed(ActionEvent event) {
        selectedType = Decorator.OR_TYPE;
        applyDecoration();
      }
    }
    
    protected abstract String getNoDecorationIconName();
    protected abstract String getAndDecorationIconName();
    protected abstract String getXorDecorationIconName();
    protected abstract String getOrDecorationIconName();
    protected abstract String getDecoratorString();
    
    protected void doTypeSelection(int type) {
      for(int i = 0; i < typeButtons.length; i++) {
        if (i == type) {
          typeButtonGroup.setSelected(
              typeButtons[i].getModel(),
              true
          );
        }
      }
    }
    
    protected void doPositionSelection(int position) {
      switch(position) {
        case Decorator.TOP: {
          positionButtonGroup.setSelected(
              northRadioButton.getModel(), 
              true
          );
          break;
        }
        case Decorator.BOTTOM: {
          positionButtonGroup.setSelected(
              southRadioButton.getModel(), 
              true
          );
          break;
        }
        case Decorator.LEFT: {
          positionButtonGroup.setSelected(
              westRadioButton.getModel(), 
              true
          );
          break;
        }
        case Decorator.RIGHT: {
          positionButtonGroup.setSelected(
              eastRadioButton.getModel(), 
              true
          );
          break;
        }
        case YAWLTask.NOWHERE: {
          positionButtonGroup.setSelected(
              nowhereRadioButton.getModel(), 
              true
          );
          break;
        }
      }
    }

    public void setPositionDisabled(int position) {
      switch (position) {
        case YAWLTask.TOP: {
          northRadioButton.setEnabled(false);
          southRadioButton.setEnabled(true);
          eastRadioButton.setEnabled(true);
          westRadioButton.setEnabled(true);
          break;
        }
        case YAWLTask.BOTTOM: {
          northRadioButton.setEnabled(true);
          southRadioButton.setEnabled(false);
          eastRadioButton.setEnabled(true);
          westRadioButton.setEnabled(true);
          break;
        }
        case YAWLTask.LEFT: {
          northRadioButton.setEnabled(true);
          southRadioButton.setEnabled(true);
          eastRadioButton.setEnabled(true);
          westRadioButton.setEnabled(false);
          break;
        }
        case YAWLTask.RIGHT: {
          northRadioButton.setEnabled(true);
          southRadioButton.setEnabled(true);
          eastRadioButton.setEnabled(false);
          westRadioButton.setEnabled(true);
          break;
        }
        default: {
          northRadioButton.setEnabled(true);
          southRadioButton.setEnabled(true);
          eastRadioButton.setEnabled(true);
          westRadioButton.setEnabled(true);
          break;
        }
      }
    }
    
    abstract void applyDecoration();

    public NetGraph getNet() {
      return this.net;
    }
    
    public void setNet(NetGraph net) {
      this.net = net;
    }
  }
  
  
  public void receiveSubscription(int state, GraphSelectionEvent event) {
    switch(state) {
      case SpecificationSelectionListener.STATE_SINGLE_TASK_SELECTED: {
        joinPanel.setTask(event.getCell());
        joinPanel.setNet((NetGraph) event.getSource());
        splitPanel.setTask(event.getCell());
        splitPanel.setNet((NetGraph) event.getSource());

        setVisible(true);
        break;
      }
      default: {
        setVisible(false);
        break;
      }
    }
    Palette.getInstance().refresh();
  }
}
