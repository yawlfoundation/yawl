/*
 * Created on 18/06/2007
 * YAWLEditor v1.4.6
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
 */

package org.yawlfoundation.yawl.editor.actions.element;

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;

import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.JSingleSelectTable;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.data.DecompositionLabelField;
import org.yawlfoundation.yawl.editor.swing.element.AbstractTaskDoneDialog;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

public class DecomposeToDirectDataTransferAction extends YAWLSelectedNetAction
                                           implements TooltipTogglingWidget {

  private static final long serialVersionUID = 1L;

  private static final DecomposeToDirectDataTransferDialog dialog = new DecomposeToDirectDataTransferDialog();

  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Decompose to Direct Data Transfer...");
    putValue(Action.LONG_DESCRIPTION, "Decompose task to directly transfer data to and from the net.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
  }
  
  public DecomposeToDirectDataTransferAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
    graph.clearSelection();
  }
  
  public String getEnabledTooltipText() {
    return " Decompose this task to a direct data transfer with its net ";
  }
  
  public String getDisabledTooltipText() {
    return " You need to have selected a decomposition-free task to " +
            " decompose it to transfer data with its net directly ";
  }
  
  public boolean shouldBeEnabled() {
    if (task instanceof YAWLAtomicTask && task.getDecomposition() == null) {
       return true;      
    }
    return false;
  }
}

class DecomposeToDirectDataTransferDialog extends AbstractTaskDoneDialog  {

  private DecompositionLabelField decompositionField;
  private InputNetVarPanel inputNetVarPanel = new InputNetVarPanel();
  private OutputNetVarPanel outputNetVarPanel = new OutputNetVarPanel();
  
  private static final long serialVersionUID = 1L;
  
  public DecomposeToDirectDataTransferDialog() {
    super("Decompose to Direct Data Transfer", true, true);
    setContentPanel(buildPanel());
    
    getDoneButton().addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
         NetCellUtilities.creatDirectTransferDecompAndParams(
             getGraph(),
             (YAWLAtomicTask) getTask(),
             decompositionField.getText(),
             inputNetVarPanel.getSelectedVariables(),
             outputNetVarPanel.getSelectedVariables()
         );
      }
    });
    
    getRootPane().setDefaultButton(getDoneButton());
  }
  
  protected void makeLastAdjustments() {
    //pack();
    setSize(640, 300);
    JUtilities.setMinSizeToCurrent(this);
  }
  
  private JPanel buildPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(
        new EmptyBorder(12,12,0,11)
    );

    // This label is confusing the weight distribution of GridBagLayout.
    // Pulled it out into it's own panel to stop oddities in actual widget
    // layout in buildWidgetPanel()
    
    JLabel discussionLabel = new JLabel(
        "<html><body>Select a number of net variables to be used as input to this task. " +
        "Do the same for output. The selected net variables will have " +
        "type-compatible task variables of the same name created for them, " +
        "and mappings that will enact a direct data copy between the newly " + 
        "created task variables and the specified selected net variables.</body></html>"
    );
    
    panel.add(discussionLabel,BorderLayout.NORTH);
    panel.add(buildWidgetPanel(), BorderLayout.CENTER);
    
    return panel;
  }
  
  private JPanel buildWidgetPanel() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(15,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel decompositionFieldLabel = new JLabel("Decomposition name:");
    decompositionFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    decompositionFieldLabel.setDisplayedMnemonic('D');
    panel.add(decompositionFieldLabel, gbc);
    
    gbc.gridx++;
    gbc.insets = new Insets(15,5,5,0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    decompositionField = buildDecompositionField();
    decompositionFieldLabel.setLabelFor(decompositionField);
    
    panel.add(decompositionField, gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weighty = 1;
    gbc.weightx = 0.5;
    gbc.insets = new Insets(0,5,10,5);
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.BOTH;

    panel.add(inputNetVarPanel,gbc);

    gbc.gridx++;

    panel.add(outputNetVarPanel,gbc);
    
    return panel;
  }
  
  private DecompositionLabelField buildDecompositionField() {
    DecompositionLabelField field = new DecompositionLabelField(20);
    field.addKeyListener(new LabelFieldDocumentListener());
    return field;
  }
  
  public void setTask(YAWLTask task, NetGraph net) {
    super.setTask(task, net);
    
    decompositionField.setText(task.getLabel());
    
    getDoneButton().setEnabled(
      SpecificationModel.getInstance().isValidNewDecompositionName(
          task.getLabel()
      )
    );
   
    inputNetVarPanel.setNet(net);
    outputNetVarPanel.setNet(net);
  }
  
  class LabelFieldDocumentListener implements KeyListener {
    
    public void keyPressed(KeyEvent e) {
      // deliberately does nothing
    }
    
    public void keyTyped(KeyEvent e) {
      // deliberately does nothing
    }

    public void keyReleased(KeyEvent e) {
      getDoneButton().setEnabled(nameFieldValid());
    }

    private boolean nameFieldValid() {
      return decompositionField.getInputVerifier().verify(decompositionField);
    }
  }
}

class InputNetVarPanel extends NetVarPanel {
  private static final long serialVersionUID = 1L;
  
  protected String getInputOutputType() {
    return "Input";
  }
  
  public void setNet(NetGraph net) {
    getNetVarTable().setVariables(
        net.getNetModel().getDecomposition().getVariables().getVariablesWithValidUsage(
            DataVariableSet.VALID_USAGE_INPUT_FROM_NET
        )
    );
  }
}


class OutputNetVarPanel extends NetVarPanel {
  private static final long serialVersionUID = 1L;
  
  protected String getInputOutputType() {
    return "Output";
  }

  public void setNet(NetGraph net) {
    getNetVarTable().setVariables(
        net.getNetModel().getDecomposition().getVariables().getVariablesWithValidUsage(
            DataVariableSet.VALID_USAGE_OUTPUT_TO_NET
        )
    );
  }
}

abstract class NetVarPanel extends JPanel {
  private SelectableNetVarTable netVarTable = new SelectableNetVarTable();
  
  protected SelectableNetVarTable getNetVarTable() {
    return this.netVarTable;
  }
  
  public NetVarPanel() {
    super();

    setBorder(
        new CompoundBorder(
           new TitledBorder("Net Variables for " + getInputOutputType()),
           new EmptyBorder(0,5,5,5)
        )
    );

    setLayout(new BorderLayout());
    
    JScrollPane tableScroller = new JScrollPane(netVarTable);
      
    add(tableScroller, BorderLayout.CENTER);
  }
  
  abstract protected String getInputOutputType();

  abstract public void setNet(NetGraph net);
  
  public List<DataVariable> getSelectedVariables() {
    return getNetVarTable().getSelectedVariables();
  }
}

class SelectableNetVarTable extends JSingleSelectTable {
  private static final long serialVersionUID = 1L;
  
  public SelectableNetVarTable() {
    super();
    setModel(new SelectableNetVarTableModel());

    getColumn("").setPreferredWidth(24);
    getColumn("").setMaxWidth(24);
    getColumn("").setResizable(false);
  }
  
  public SelectableNetVarTableModel  getSelectableNetVarTableModel() {
    return (SelectableNetVarTableModel) getModel();
  }
  
  public void setVariables(List<DataVariable> variables){
    getSelectableNetVarTableModel().setVariables(variables);
    setPreferredScrollableViewportSize(
        getPreferredSize()
    );
  }
  
  public List<DataVariable> getSelectedVariables() {
    return getSelectableNetVarTableModel().getSelectedVariables();
  }
}

class SelectableNetVarTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private List<DataVariable> variables;
  private LinkedList<Boolean> variableListSelection;
  
  private static final String[] COLUMN_LABELS = { 
    "",
    "Name",
    "Type"
  };
  
  public static final int SELECT_COLUMN        = 0;
  public static final int NAME_COLUMN          = 1;
  public static final int TYPE_COLUMN          = 2;
  
  public SelectableNetVarTableModel() {
    super();
  }
  
  public List<DataVariable> getVariables() {
    return this.variables;
  }
  
  public DataVariable getVariableAtRow(int row) {
    return variables.get(row);
  }
  
  public Boolean getVariableSelectionAtRow(int row) {
    return variableListSelection.get(row);
  }

  public void setVariableSelectionAtRow(int row, Boolean value) {
    variableListSelection.set(row, value);
  }

  public List<DataVariable> getSelectedVariables() {
    LinkedList<DataVariable> selectedVariables = new LinkedList<DataVariable>();
    for(int row = 0; row < getVariables().size(); row++) {
      if (getVariableSelectionAtRow(row).booleanValue() == true) {
        selectedVariables.add(getVariableAtRow(row));
      }
    }
    return selectedVariables;
  }
  
  public void setVariables(List<DataVariable> variables) {
    this.variables = variables;
    this.variableListSelection = new LinkedList<Boolean>();
    for(DataVariable variable: variables) {
       variableListSelection.add(new Boolean(false)); 
    }
    
    fireTableRowsUpdated(
        0, 
        variables.size() - 1
    );
  }
  
  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int column) {
    return COLUMN_LABELS[column];
  }
  
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == SELECT_COLUMN) {
      return Boolean.class;
    }
    return String.class;
  }
  
  public boolean isCellEditable(int row, int column) {
    if (column == SELECT_COLUMN) {
      return true;
    }
    return false;
  }
  
  public int getRowCount() {
    if (getVariables() != null) {
      return getVariables().size();
    }
    return 0;
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case SELECT_COLUMN:  {
        return getVariableSelectionAtRow(row);
      }
      case NAME_COLUMN:  {
        return getVariableAtRow(row).getName();
      }
      case TYPE_COLUMN:  {
        return getVariableAtRow(row).getDataType();
      }
      default: {
        return null;
      }
    }
  }
  
  public void setValueAt(Object value, int row, int col) {
    switch (col) {
      case SELECT_COLUMN:  {
        setVariableSelectionAtRow(row, (Boolean) value);
        fireTableRowsUpdated(row, row);
      }
      default: {
      }
    }
  }

}