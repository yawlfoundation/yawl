package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class SingleTaskPalette extends JTabbedPane
        implements GraphStateListener {

  private static final long serialVersionUID = 1L;

  private JoinPanel joinPanel;
  private SplitPanel splitPanel;
  
  public SingleTaskPalette() {
    joinPanel = new JoinPanel(this);
    splitPanel = new SplitPanel(this);
    
    addTab("Join", joinPanel);
    addTab("Split", splitPanel);
    
    setSelectedIndex(1);   // Make split the default

    setVisible(false);

      Publisher.getInstance().subscribe(this,
              Arrays.asList(GraphState.NoElementSelected,
                      GraphState.ElementsSelected,
                      GraphState.OneTaskSelected));
  }
  
  public void refreshComponentValidity() {
    joinPanel.updateWidgetConfiguration();
    splitPanel.updateWidgetConfiguration();
  }

  class JoinPanel extends DecoratorPanel {

    private static final long serialVersionUID = 1L;
    
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
    
    protected Color getButtonColour() {
      return UserSettings.getJoinFillColour();
    }

    protected void setButtonColour(Color colour) {
        UserSettings.setJoinFillColour(colour);
    }

    public void setTask(Object task) {
      super.setTask(task);
      
      if (getTask().getJoinDecorator() != null) {
        this.selectedType = getTask().getJoinDecorator().getType();
      } else {
        this.selectedType = Decorator.NO_TYPE;
      }
      
      this.selectedPosition = getTask().getJoinDecoratorPos();

      if (this.selectedPosition == Decorator.NOWHERE) {
        if (getTask().getSplitDecoratorPos() != Decorator.LEFT) {
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
        if (getTask().getIncomingFlowCount() > 1) {
          typeButtons[TYPE_NONE].setEnabled(false);
        }
      }

      setPositionDisabled(getTask().getSplitDecoratorPos());
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

    private static final long serialVersionUID = 1L;
    
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
    
    protected Color getButtonColour() {
      return UserSettings.getSplitFillColour();
    }

      protected void setButtonColour(Color colour) {
          UserSettings.setSplitFillColour(colour);
      }
    
    public void setTask(Object task) {
      super.setTask(task);
      
      if (getTask().getSplitDecorator() != null) {
        this.selectedType = getTask().getSplitDecorator().getType();
      } else {
        this.selectedType = Decorator.NO_TYPE;
      }
      
      this.selectedPosition = getTask().getSplitDecoratorPos();
      
      if (this.selectedPosition == Decorator.NOWHERE) {
        if (getTask().getJoinDecoratorPos() != Decorator.RIGHT) {
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

      setPositionDisabled(getTask().getJoinDecoratorPos());
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
      setBorder(new EmptyBorder(3,3,3,3));
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

      gbc.gridx = 0;
      gbc.gridy++;
      gbc.gridwidth = 1;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.insets = new Insets(4,0,4,0);
      
      add(new JLabel("Fill:"), gbc);
      
      gbc.gridx++;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.fill = GridBagConstraints.VERTICAL;
      gbc.insets = new Insets(4,4,4,4);
          
      add(buildColourButton(), gbc);
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
              "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
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
          SwingConstants.LEFT
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
          SwingConstants.RIGHT
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
    
    private JButton buildColourButton() {
      JButton colourButton = new JButton("");
      colourButton.setToolTipText("Select decorator fill colour");

      colourButton.setBackground(getButtonColour());
      
      colourButton.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              Color newColor = JColorChooser.showDialog(
                  YAWLEditor.getInstance(),
                  "Select decorator fill colour",
                  getButtonColour());

              if (newColor != null) {
                setButtonColour(newColor);
              }
              ((JButton) event.getSource()).setBackground(newColor);
              SpecificationUtilities.refreshNetViews(
                  SpecificationModel.getInstance()
              );
            }
          }
      );
      
      return colourButton;
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
              "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
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
      private static final long serialVersionUID = 1L;
      
      {
        putValue(Action.SHORT_DESCRIPTION, " No " + getDecoratorString() + " ");
        putValue(Action.NAME, "NONE");
        putValue(Action.LONG_DESCRIPTION, " No " + getDecoratorString() + " ");
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
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
      private static final long serialVersionUID = 1L;

      {
        putValue(Action.SHORT_DESCRIPTION, " AND " + getDecoratorString() + " ");
        putValue(Action.NAME, "AND");
        putValue(Action.LONG_DESCRIPTION, " AND " + getDecoratorString() + " ");
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
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
      private static final long serialVersionUID = 1L;

      {
        putValue(Action.SHORT_DESCRIPTION, " XOR " + getDecoratorString() + " ");
        putValue(Action.NAME, "XOR");
        putValue(Action.LONG_DESCRIPTION, " XOR " + getDecoratorString() + " ");
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
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
      private static final long serialVersionUID = 1L;

      {
        putValue(Action.SHORT_DESCRIPTION, "OR " + getDecoratorString());
        putValue(Action.NAME, "OR");
        putValue(Action.LONG_DESCRIPTION, "OR " + getDecoratorString());
        putValue(Action.SMALL_ICON, 
            ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
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
    protected abstract Color getButtonColour();
    protected abstract void setButtonColour(Color colour);

    
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
  
  public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
    switch(state) {
      case OneTaskSelected: {
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