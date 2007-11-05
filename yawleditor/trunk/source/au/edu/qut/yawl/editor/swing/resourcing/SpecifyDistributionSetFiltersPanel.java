package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.resourcing.ResourceMapping;
import au.edu.qut.yawl.editor.resourcing.ResourcingFilter;
import au.edu.qut.yawl.editor.swing.JSingleSelectTable;
import au.edu.qut.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

public class SpecifyDistributionSetFiltersPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  private RuntimeConstraintsPanel runtimeConstraintsPanel;
  private RuntimeFiltersPanel runtimeFiltersPanel;
  
  public SpecifyDistributionSetFiltersPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(5,5,5,5));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.weighty = 0.666;
    gbc.insets = new Insets(0,5,0,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildFilterPanel(), gbc);

    gbc.gridy++;
    gbc.weighty = 0.333;
    
    add(buildRuntimeConstraintPanel(), gbc);
    
    gbc.gridy++;
    
  }

  public String getWizardStepTitle() {
    return "Specify Distribution Set Filter(s)";
  }
  
  private JPanel buildFilterPanel() {
    runtimeFiltersPanel = new RuntimeFiltersPanel(this);
    return runtimeFiltersPanel;
  }
  
  private JPanel buildRuntimeConstraintPanel() {
    runtimeConstraintsPanel = new RuntimeConstraintsPanel(this);
    return runtimeConstraintsPanel;
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}     

  void refresh() {
    runtimeFiltersPanel.setFilters(
        ResourcingServiceProxy.getInstance().getRegisteredResourcingFilters()
    );
    runtimeFiltersPanel.setTask(
        (YAWLAtomicTask) getTask()
    );
    
    runtimeConstraintsPanel.setTask(
        (YAWLAtomicTask) getTask()
    );
  }
    
  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      ResourceMapping.SYSTEM_INTERACTION_POINT &&
      getResourceMapping().getRetainFamiliarTask() == null;
  }
}


class RuntimeFiltersPanel extends JPanel implements ListSelectionListener {

  private static final long serialVersionUID = 1L;

  private SpecifyDistributionSetFiltersPanel  filterPanel;
  private SelectableFilterTable filterTable;
  private FilterParameterTable filterParamTable;

  private YAWLAtomicTask task;

  public RuntimeFiltersPanel(SpecifyDistributionSetFiltersPanel filterPanel) {
    super();
    this.filterPanel = filterPanel;
    buildContent();
  }
  
  public void setTask(YAWLAtomicTask task) {
    this.task = task;
  }

  
  protected ResourceMapping getResourceMapping() {
    return filterPanel.getResourceMapping();
  }

  private void buildContent() {
    setBorder(
        new TitledBorder("Filters")
    );

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,10,0,5);
    gbc.gridwidth = 1;
    gbc.weightx = 0.40;
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildFilterTable(), gbc);
    
    gbc.gridx++;

    gbc.insets = new Insets(0,5,0,10);
    add(buildFilterParamTable(), gbc);
  }
  
  private JScrollPane buildFilterTable() {
    this.filterTable = new SelectableFilterTable();
    this.filterTable.getSelectionModel().addListSelectionListener(this);
    return new JScrollPane(filterTable);
  }
  
  
  private JScrollPane buildFilterParamTable() {
    this.filterParamTable = new FilterParameterTable();
    return new JScrollPane(filterParamTable);
  }
  
  public void setFilters(List<ResourcingFilter> filters){
    filterTable.setFilters(filters);
  }

  public void valueChanged(ListSelectionEvent e) {
    System.out.println("slected filter changed.");
    filterParamTable.setParameters(
      filterTable.getSelectedFilter().getParameters()
    );
  }
}

class SelectableFilterTable extends JSingleSelectTable {
  private static final long serialVersionUID = 1L;
  
  public SelectableFilterTable() {
    super();
    setModel(new SelectableFilterTableModel());

    getColumn("").setPreferredWidth(24);
    getColumn("").setMaxWidth(24);
    getColumn("").setResizable(false);
  }
  
  public SelectableFilterTableModel  getSelectableFilterTableModel() {
    return (SelectableFilterTableModel) getModel();
  }
  
  public void setFilters(List<ResourcingFilter> filters){
    getSelectableFilterTableModel().setFilters(filters);
    setPreferredScrollableViewportSize(
        getPreferredSize()
    );
  }
  
  public ResourcingFilter getSelectedFilter() {
    return getSelectableFilterTableModel().getFilterAtRow(
      getSelectedRow()    
    );
  }
  
  public List<ResourcingFilter> getFlaggedFilters() {
    return getSelectableFilterTableModel().getFlaggedFilters();
  }
}

class SelectableFilterTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private List<ResourcingFilter> filters;
  private LinkedList<Boolean> filterListSelection;
  
  private static final String[] COLUMN_LABELS = { 
    "",
    "Filter",
  };
  
  public static final int SELECT_COLUMN        = 0;
  public static final int FILTER_NAME_COLUMN   = 1;
  
  public SelectableFilterTableModel() {
    super();
  }
  
  public List<ResourcingFilter> getFilters() {
    return this.filters;
  }
  
  public ResourcingFilter getFilterAtRow(int row) {
    return filters.get(row);
  }
  
  public Boolean getFilterSelectionAtRow(int row) {
    return filterListSelection.get(row);
  }

  public void setFilterSelectionAtRow(int row, Boolean value) {
    filterListSelection.set(row, value);
  }

  public List<ResourcingFilter> getFlaggedFilters() {
    LinkedList<ResourcingFilter> selectedFilters = new LinkedList<ResourcingFilter>();
    for(int row = 0; row < getFilters().size(); row++) {
      if (getFilterSelectionAtRow(row).booleanValue() == true) {
        selectedFilters.add(
            getFilterAtRow(row)
        );
      }
    }
    return selectedFilters;
  }
  
  public void setFilters(List<ResourcingFilter> filters) {
    this.filters = filters;
    this.filterListSelection = new LinkedList<Boolean>();
    for(ResourcingFilter filter: filters) {
       filterListSelection.add(
           new Boolean(false)
       ); 
    }
    
    fireTableRowsUpdated(
        0, 
        filters.size() - 1
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
    if (getFilters() != null) {
      return getFilters().size();
    }
    return 0;
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case SELECT_COLUMN:  {
        return getFilterSelectionAtRow(row);
      }
      case FILTER_NAME_COLUMN:  {
        return getFilterAtRow(row).getDisplayName();
      }
      default: {
        return null;
      }
    }
  }
  
  public void setValueAt(Object value, int row, int col) {
    switch (col) {
      case SELECT_COLUMN:  {
        setFilterSelectionAtRow(row, (Boolean) value);
        fireTableRowsUpdated(row, row);
      }
      default: {
      }
    }
  }
}

class FilterParameterTable extends JSingleSelectTable {
  private static final long serialVersionUID = 1L;
  
  public FilterParameterTable() {
    super();
    setModel(new FilterParameterTableModel());
  }
  
  public FilterParameterTableModel getFilterParameterTableModel() {
    return (FilterParameterTableModel) getModel();
  }
  
  public void setParameters(Map<String, String> parameters){
    getFilterParameterTableModel().setParameters(parameters);
    setPreferredScrollableViewportSize(
        getPreferredSize()
    );
  }
}

class FilterParameterTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private Map<String, String> parameters;
  private String[][] parameterArray;


  private static final String[] COLUMN_LABELS = { 
    "Parameter",
    "Value",
  };
  
  public static final int PARAM_NAME_COLUMN    = 0;
  public static final int PARAM_VALUE_COLUMN   = 1;
  
  public FilterParameterTableModel() {
    super();
  }
  
  public Map<String, String> getParameters() {
    return this.parameters;
  }
  
  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
    
    fireTableRowsUpdated(
        0, 
        parameters.size() - 1
    );
  }
  
  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int column) {
    return COLUMN_LABELS[column];
  }
  
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }
  
  public boolean isCellEditable(int row, int column) {
    if (column == PARAM_VALUE_COLUMN) {
      return true;
    }
    return false;
  }
  
  public int getRowCount() {
    if (getParameters() != null) {
      return getParameters().size();
    }
    return 0;
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case PARAM_NAME_COLUMN:  {
        return "greebo";
      }
      case PARAM_VALUE_COLUMN:  {
        return "wibble";
      }
      default: {
        return null;
      }
    }
  }
  
  public void setValueAt(Object value, int row, int col) {
    switch (col) {
    case PARAM_VALUE_COLUMN:  {
        //TODO: value update needed
        fireTableRowsUpdated(row, row);
      }
      default: {
      }
    }
  }
}



class RuntimeConstraintsPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private FamiliarTaskComboBox familiarTaskBox; 
  
  private SpecifyDistributionSetFiltersPanel  filterPanel;
  private JCheckBox separationOfDutiesCheckBox;
  private JCheckBox piledExecutionCheckBox;
  
  public RuntimeConstraintsPanel(SpecifyDistributionSetFiltersPanel filterPanel) {
    super();
    this.filterPanel = filterPanel;
    buildContent();
  }
  
  protected ResourceMapping getResourceMapping() {
    return filterPanel.getResourceMapping();
  }
  
  private void buildContent() {
    setBorder(
        new TitledBorder("Runtime Constraints")
    );

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(10,5,10,5);
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    add(buildPiledExecutionCheckBox(), gbc);
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(10,5,10,5);
    
    add(buildSeparationOfDutiesCheckBox(), gbc);
    
    gbc.gridx++;
    add(buildSeparationOfDutiesFamilarTaskBox(), gbc);
  }
  
  private JCheckBox buildPiledExecutionCheckBox() {
    piledExecutionCheckBox = new JCheckBox("Allow workitems of this task to be piled across cases to a single user.");
    piledExecutionCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            filterPanel.getResourceMapping().enablePrivilege(
                ResourceMapping.CAN_PILE_PRIVILEGE, 
                piledExecutionCheckBox.isSelected()
            );
          }
        }
    );

    return piledExecutionCheckBox;
  }
  
  private JCheckBox buildSeparationOfDutiesCheckBox() {
    separationOfDutiesCheckBox = new JCheckBox("Do not allow users who've done workitems of the following previous task to also do this task: ");
    separationOfDutiesCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            familiarTaskBox.setEnabled(
                separationOfDutiesCheckBox.isSelected()    
            );
          }
        }
    );

    return separationOfDutiesCheckBox;
  }
  
  private JComboBox buildSeparationOfDutiesFamilarTaskBox() {
    familiarTaskBox = new FamiliarTaskComboBox();
    
    return familiarTaskBox;
  }
  
  public void setTask(YAWLAtomicTask task) {
    familiarTaskBox.setTask(task);

    if (familiarTaskBox.getFamiliarTaskNumber() == 0) {
      separationOfDutiesCheckBox.setEnabled(false);
      familiarTaskBox.setEnabled(false);
    } else {
      if (getResourceMapping().getSeparationOfDutiesTask()!= null) {
        separationOfDutiesCheckBox.setEnabled(true);
        separationOfDutiesCheckBox.setSelected(true);
        familiarTaskBox.setEnabled(true);
        familiarTaskBox.setSelectedFamiliarTask(
            getResourceMapping().getSeparationOfDutiesTask()    
        );
      } else {
        separationOfDutiesCheckBox.setEnabled(true);
        separationOfDutiesCheckBox.setSelected(false);
        familiarTaskBox.setEnabled(false);
      }
    }
    
    piledExecutionCheckBox.setSelected(
      this.getResourceMapping().isPrivilegeEnabled(
          ResourceMapping.CAN_PILE_PRIVILEGE    
      )    
    );
  }
}