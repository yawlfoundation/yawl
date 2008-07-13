package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingFilter;
import org.yawlfoundation.yawl.editor.swing.JSingleSelectTable;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    
    // If we get no registered filters returned, but the editor has previously allowed
    // filters, use these cached filters in leu of ones from an engine connection.
    
    List<ResourcingFilter> filters = ResourcingServiceProxy.getInstance().getRegisteredResourcingFilters();

    if (filters.size() == 0) {
       List<ResourcingFilter> cachedFilters =
               getTask().getResourceMapping().getResourcingFilters();
       if ((cachedFilters != null) && (cachedFilters.size() > 0))
          filters = cachedFilters;      
    }
    
    runtimeFiltersPanel.setFilters(
        filters
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
      ResourceMapping.SYSTEM_INTERACTION_POINT;
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
    filterTable.setResourceMapping(
        task.getResourceMapping()
    );
    filterTable.clearSelection();
    filterParamTable.reset();
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
    gbc.gridwidth = 2;
    gbc.insets = new Insets(0,5,10,5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    add(new JLabel(
            "<html><body>Tick those filters to be applied to the specified "+
            "distribution set. Set parameter values for the selected "+
            "filter as required.</body></html>"
        ),gbc
    );
    
    gbc.gridy++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,10,0,5);
    gbc.gridwidth = 1;
    gbc.weightx = 0.5;
    
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
    if (filterTable.getSelectedRow() == -1) {
      filterParamTable.reset();
    } else {
      filterParamTable.setParameters(
          filterTable.getSelectedFilter().getParameters()
      );
    }
  }
}

class SelectableFilterTable extends JSingleSelectTable {
  private static final long serialVersionUID = 1L;
  
  public SelectableFilterTable() {
    super();
    setModel(new SelectableFilterTableModel());
    this.setPreferredSize(new Dimension(25,10));
    getColumn("").setPreferredWidth(24);
    getColumn("").setMaxWidth(24);
    getColumn("").setResizable(false);
  }
  
  public SelectableFilterTableModel  getSelectableFilterTableModel() {
    return (SelectableFilterTableModel) getModel();
  }

  public void setResourceMapping(ResourceMapping resourceMapping) {
    getSelectableFilterTableModel().setResourceMapping(
        resourceMapping
    );
  }
  
  public void setFilters(List<ResourcingFilter> filters){
    getSelectableFilterTableModel().setFilters(filters);
    setPreferredScrollableViewportSize(
        getPreferredSize()
    );
  }
  
  
  public ResourcingFilter getSelectedFilter() {
    if (getSelectedRow() == -1) {
      return null;
    }
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

  private ResourceMapping resourceMapping;
  
  private static final String[] COLUMN_LABELS = { 
    "",
    "Filter",
  };
  
  public static final int SELECT_COLUMN        = 0;
  public static final int FILTER_NAME_COLUMN   = 1;
  
  public SelectableFilterTableModel() {
    super();
  }

  public void setResourceMapping(ResourceMapping resourceMapping) {
    this.resourceMapping = resourceMapping;
    setFlaggedFilters();
  }
  
  private void setFlaggedFilters() {
    if (getResourceMapping().getResourcingFilters() == null) {
      getResourceMapping().setResourcingFilters(new LinkedList<ResourcingFilter>());
      return; // already defaults to false per filter, so we're done here.
    }
    
    for(int i = 0; i < filters.size(); i++) {
      for(int j = 0; j < getResourceMapping().getResourcingFilters().size(); j++) {
        if (getFilterAtRow(i).equals(getResourceMapping().getResourcingFilters().get(j))) {

          // The filters we get off the engine are like classes;  uninstantiated as yet.
          // The filters stored in ResourceMapping are like instances of the classes.
          // there is extra information that we should be using instead. We swap
          // out the engine filter in favour of the instantiated filter at this point.
          
          filters.set(i, getResourceMapping().getResourcingFilters().get(j));
          setFilterSelectionAtRow(i, Boolean.TRUE);
          fireTableRowsUpdated(i, i);
        }
      }
    }
  }
  
  public ResourceMapping getResourceMapping() {
    return this.resourceMapping;
  }
  
  public List<ResourcingFilter> getFilters() {
    return this.filters;
  }
  
  public ResourcingFilter getFilterAtRow(int row) {
    if (filters == null) {
      return null;
    }
    return filters.get(row);
  }
  
  public Boolean getFilterSelectionAtRow(int row) {
    return filterListSelection.get(row);
  }

  public void setFilterSelectionAtRow(int row, Boolean value) {
    filterListSelection.set(row, value);
    if (value.booleanValue()) { // true == filter selected
      boolean filterAlreadySelected = false;
      for(ResourcingFilter alreadySelectedFilter : getResourceMapping().getResourcingFilters()) {
        if (getFilterAtRow(row).equals(alreadySelectedFilter)) {
          filterAlreadySelected = true;
        }
        if (!filterAlreadySelected) {
          getResourceMapping().getResourcingFilters().add(
              getFilterAtRow(row)    
            ); 
        }
      }
    } else { // false == filter removed.
      for(ResourcingFilter alreadySelectedFilter : getResourceMapping().getResourcingFilters()) {
        if (getFilterAtRow(row).equals(alreadySelectedFilter)) {
          getResourceMapping().getResourcingFilters().remove(
              alreadySelectedFilter    
          ); 
        }
      }
    }
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
    fireTableDataChanged();
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
      this.setPreferredSize(new Dimension(25,10));
      
    setModel(new FilterParameterTableModel());
  }
  
  public void reset() {
    setModel(new FilterParameterTableModel());
  }
  
  public FilterParameterTableModel getFilterParameterTableModel() {
    return (FilterParameterTableModel) getModel();
  }
  
  public void setParameters(Map<String, String> parameters){
    getFilterParameterTableModel().setParameters(parameters);
    resetFormat();
  }
  
  private void resetFormat() {
    String paramColumnName = getModel().getColumnName(
        FilterParameterTableModel.PARAM_NAME_COLUMN    
    );

    getColumn(paramColumnName).setMinWidth(
        getMaximumParameterWidth()
    );
    getColumn(paramColumnName).setPreferredWidth(
        getMaximumParameterWidth()
    );
    getColumn(paramColumnName).setMaxWidth(
        getMaximumParameterWidth()
    );
    
    setPreferredScrollableViewportSize(
        getPreferredSize()
    );
  }

  private int getMaximumParameterWidth() {
    int maxWidth = getMessageWidth(
        getModel().getColumnName(
            FilterParameterTableModel.PARAM_NAME_COLUMN    
        )   
    );
    for(int i = 0; i < this.getRowCount(); i++) {
      maxWidth = Math.max(
          maxWidth, 
          getMessageWidth(
              getParamNameAt(i)
          )
      );
    }
    return maxWidth;
  }
  
  private int getMessageWidth(String message) {
    return  getFontMetrics(getFont()).stringWidth(message) + 5;
  }
  
  public String getParamNameAt(int row) {
    return getFilterParameterTableModel().getParamNameAt(row);
  }
}

class FilterParameterTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private Map<String, String> parameters;
  private String[][] parameterArray;

  private static final int KEY_INDEX = 0;
  private static final int VALUE_INDEX = 1;

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
    
    parameterArray = new String[parameters.size()][2];

    int i = 0;
    for(String key: parameters.keySet()) {
      parameterArray[i][KEY_INDEX] = key;
      parameterArray[i][VALUE_INDEX] = parameters.get(key);
      i++;
    }
    
    fireTableDataChanged();
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
  
  public String getParamNameAt(int row) {
    return (String) getValueAt(row, KEY_INDEX);
  }

  public Object getValueAt(int row, int col) {
    return parameterArray[row][col];
  }
  
  public void setValueAt(Object value, int row, int col) {
    if (col != VALUE_INDEX) {
      return;
    }
    parameterArray[row][col] = (String) value;
    parameters.put(
        parameterArray[row][KEY_INDEX], 
        (String) value
    );
    fireTableRowsUpdated(row, row);
  }
}


class RuntimeConstraintsPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private FamiliarTaskComboBox separationOfDutiesFamiliarTaskBox; 
  
  private SpecifyDistributionSetFiltersPanel  filterPanel;
  private JCheckBox separationOfDutiesCheckBox;
  private JCheckBox piledExecutionCheckBox;
  
  private JCheckBox retainFamiliarCheckBox;
  private FamiliarTaskComboBox familiarTaskComboBox;
  
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
    gbc.insets = new Insets(5,5,5,5);
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    add(buildPiledExecutionCheckBox(), gbc);
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(5,5,5,5);
  
    retainFamiliarCheckBox = buildRetainFamiliarButton();

    add(retainFamiliarCheckBox, gbc);
    
    gbc.gridx++;

    familiarTaskComboBox = buildFamiliarTaskComboBox();

    add(familiarTaskComboBox, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    
    add(buildSeparationOfDutiesCheckBox(), gbc);

    gbc.gridx++;
    add(buildSeparationOfDutiesFamilarTaskBox(), gbc);
  }
  
  private JCheckBox buildRetainFamiliarButton() {
    final JCheckBox button = new JCheckBox("Choose participant(s) who completed previous task: ");
    button.setHorizontalTextPosition(SwingConstants.RIGHT);
    button.setMnemonic(KeyEvent.VK_R);
    button.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (button.isSelected()) {
            familiarTaskComboBox.setEnabled(true);
            getResourceMapping().setRetainFamiliarTask(
                familiarTaskComboBox.getSelectedFamiliarTask()    
            );
          } else {
            familiarTaskComboBox.setEnabled(false);
            getResourceMapping().setRetainFamiliarTask(null);
          }
        }
      }
    );
    return button;
  }

  private FamiliarTaskComboBox buildFamiliarTaskComboBox() {
    final FamiliarTaskComboBox box = new FamiliarTaskComboBox();
    box.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (retainFamiliarCheckBox.isSelected() && box.isEnabled()) {
              getResourceMapping().setRetainFamiliarTask(
                  box.getSelectedFamiliarTask()    
              );
            }
          }
        }
    );
    
    return box;
  }
  
  private JCheckBox buildPiledExecutionCheckBox() {
    piledExecutionCheckBox = new JCheckBox("Allow this task to be piled to a single participant.");
    piledExecutionCheckBox.setMnemonic(KeyEvent.VK_P);
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
    separationOfDutiesCheckBox = new JCheckBox("Do not choose participant(s) who completed previous task: ");
    separationOfDutiesCheckBox.setMnemonic(KeyEvent.VK_N);
    separationOfDutiesCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (separationOfDutiesCheckBox.isSelected()) {
              separationOfDutiesFamiliarTaskBox.setEnabled(true);
              getResourceMapping().setSeparationOfDutiesTask(
                  separationOfDutiesFamiliarTaskBox.getSelectedFamiliarTask()    
              );
            } else {
              separationOfDutiesFamiliarTaskBox.setEnabled(false);
              getResourceMapping().setSeparationOfDutiesTask(null);  
            }
          }
        }
    );

    return separationOfDutiesCheckBox;
  }
  
  private JComboBox buildSeparationOfDutiesFamilarTaskBox() {
    separationOfDutiesFamiliarTaskBox = new FamiliarTaskComboBox();
    separationOfDutiesFamiliarTaskBox .addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (separationOfDutiesCheckBox.isSelected() && separationOfDutiesFamiliarTaskBox.isEnabled()) {
              getResourceMapping().setSeparationOfDutiesTask(
                  separationOfDutiesFamiliarTaskBox.getSelectedFamiliarTask()    
              );
            }
          }
        }
    );
    
    return separationOfDutiesFamiliarTaskBox;
  }
  
  public void setTask(YAWLAtomicTask task) {

    piledExecutionCheckBox.setSelected(
        this.getResourceMapping().isPrivilegeEnabled(
            ResourceMapping.CAN_PILE_PRIVILEGE    
        )    
      );
    
    separationOfDutiesFamiliarTaskBox.setTask(
        task
    );

    familiarTaskComboBox.setTask(
        task
    );


    separationOfDutiesCheckBox.setEnabled(true);
    separationOfDutiesFamiliarTaskBox.setEnabled(true);

    retainFamiliarCheckBox.setEnabled(true);
    familiarTaskComboBox.setEnabled(true);

    
    // if there are no familiar tasks possible, shut both widget sets down.
    // Either task box will tell us how many tasks can be used as a familiar task.
    
    if (separationOfDutiesFamiliarTaskBox.getFamiliarTaskNumber() == 0) {
      separationOfDutiesCheckBox.setEnabled(false);
      separationOfDutiesFamiliarTaskBox.setEnabled(false);

      retainFamiliarCheckBox.setEnabled(false);
      familiarTaskComboBox.setEnabled(false);
      
      return;
    } 

    YAWLAtomicTask famTask = getResourceMapping().getSeparationOfDutiesTask();
    if (famTask == null) {
      separationOfDutiesCheckBox.setSelected(false);
      separationOfDutiesFamiliarTaskBox.setEnabled(false);
    } else {
      separationOfDutiesFamiliarTaskBox.setEnabled(true);
      separationOfDutiesFamiliarTaskBox.setSelectedFamiliarTask(famTask);
      separationOfDutiesCheckBox.setSelected(true);
      separationOfDutiesCheckBox.setEnabled(true);
    } 

    famTask = getResourceMapping().getRetainFamiliarTask();
    if (famTask == null) {
      retainFamiliarCheckBox.setSelected(false);
      familiarTaskComboBox.setEnabled(false);
    } else {
      familiarTaskComboBox.setEnabled(true);
      familiarTaskComboBox.setSelectedFamiliarTask(famTask);
      retainFamiliarCheckBox.setSelected(true);
      retainFamiliarCheckBox.setEnabled(true);
    }
  }
}