package com.nexusbpm.editor.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import operation.WorkflowOperation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YTask;

import com.nexusbpm.command.Command;
import com.nexusbpm.command.SaveDataTransferChangesCommand;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.services.NexusServiceInfo;
import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;

/**
 * Editor for the email sender component.
 * 
 * @see        com.ichg.capsela.domain.component.EmailSenderComponent
 * @author     catch23
 * @author     Daniel Gredler
 * @created    October 28, 2002
 */
public class DataTransferEditor extends ComponentEditor {
	private static final Log LOG = LogFactory.getLog( DataTransferEditor.class );
    
    private YTask task;

    private JPanel variablesPanel;
    private JPanel buttonsPanel;
    private JPanel editorPanel;
    
    private JButton deleteButton;
    private JButton addButton;
    private JButton cancelButton;
    private JButton okButton;
    
    private Set<VariableRow> deleteSet = new HashSet<VariableRow>();
    private Set<VariableRow> addSet = new HashSet<VariableRow>();
    
    private JLabel variableNameTitle;
    private JLabel transferSourceTitle;
    private JLabel transferVariableTitle;
    
    private YNet net;
    
    private NexusServiceInfo info;
    private Set<String> builtInVars = new HashSet<String>();
    
    private TreeSet<VariableRow> displayedRows = new TreeSet<VariableRow>();
    private List<VariableRow> deletedRows = new LinkedList<VariableRow>();
    
    private ComboItem defaultItem;
    private Map<ComboItem, List<String>> choices = new HashMap<ComboItem, List<String>>();
    
    private Map<String, KeyValue> inputMappings;
    
    private static class ComboItem {
        private String name;
        private String id;
        public ComboItem( String name, String id ) {
            LOG.info( "creating combo item name='" + name + "' id='" + id + "'" );
            this.name = name;
            this.id = id;
        }
        public String toString() {
            return name;
        }
    }
    
	/**
	 * @see ComponentEditor#initializeUI()
	 */
	public JComponent initializeUI() throws EditorException {
        task = (YTask) _proxy.getData();
        inputMappings = task.getDataMappingsForTaskStartingSet();
        
        net = ((YTask)_proxy.getData()).getParent();
        
        if( task instanceof YAtomicTask ) {
            info = WorkflowOperation.getNexusServiceInfoForTask( (YAtomicTask) task );
            if( info != null ) {
                for( int index = 0; index < info.getVariableNames().length; index++ ) {
                    builtInVars.add( info.getVariableNames()[ index ] );
                }
            }
        }
        else {
            info = null;
        }
        
        defaultItem = new ComboItem( "<initial value>", null );
        choices.put( defaultItem, null );
        for( YExternalNetElement element : net.getNetElements() ) {
            if( element instanceof YTask && element != task ) {
                LOG.info( "element is a task and not this task (" + element + ")" );
                YTask task = (YTask) element;
                NexusServiceData data = NexusServiceData.unmarshal( task, false );
                List<String> vars = data.getVariableNames();
                choices.put( new ComboItem( task.getName(), task.getID() ), vars );
            }
            else {
                LOG.info( "element is not a task or is this task (" + element + ")" );
            }
        }
        
        variablesPanel = new JPanel();
        variablesPanel.setLayout( new GridLayout( 0, 3 ) );
        
        variableNameTitle = new JLabel();
        variableNameTitle.setText( "Variable Name:" );
        variableNameTitle.setFont( variableNameTitle.getFont().deriveFont( Font.BOLD ) );
        variableNameTitle.setHorizontalAlignment( SwingConstants.CENTER );
        
        transferSourceTitle = new JLabel();
        transferSourceTitle.setText( "Transfer source:" );
        transferSourceTitle.setFont( transferSourceTitle.getFont().deriveFont( Font.BOLD ) );
        transferSourceTitle.setHorizontalAlignment( SwingConstants.CENTER );
        
        transferVariableTitle = new JLabel();
        transferVariableTitle.setText( "Source variable/value:" );
        transferVariableTitle.setFont( transferVariableTitle.getFont().deriveFont( Font.BOLD ) );
        transferVariableTitle.setHorizontalAlignment( SwingConstants.CENTER );
        
        for( String var : data.getVariableNames() ) {
            // TODO do we want to make sure all the nexus service vars are actually there?
            addRow( var, false );
        }
        
        deleteButton = new JButton( "Delete" );
        deleteButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                for( VariableRow row : new LinkedList<VariableRow>( displayedRows ) ) {
                    if( row.checkBox.isSelected() ) {
                        LOG.info( "deleting variable " + row.name );
                        deleteRow( row );
                    }
                    else {
                        LOG.info( "not deleting variable " + row.name );
                    }
                }
                redisplayRows();
            }
        });
        
        addButton = new JButton( "Add" );
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                Runnable runner = new Runnable() {
                    public void run() {
                        String name = JOptionPane.showInputDialog(
                                DataTransferEditor.this, "Please enter the variable name", "variableName" );
                        if( name != null ) {
                            LOG.info( "received name '" + name + "'" );
                            name = name.replaceAll( "  ", " " ).replaceAll( " ", "_" );
                            VariableRow row = getDeletedRow( name );
                            if( row != null ) {
                                restoreRow( row );
                            }
                            else if( getDisplayedRow( name ) != null ) {
                                LOG.info( "name '" + name + "' is already taken" );
                                JOptionPane.showMessageDialog(
                                        DataTransferEditor.this,
                                        "A variable with the name '" + name +"' already exists!",
                                        "Invalid name",
                                        JOptionPane.ERROR_MESSAGE );
                            }
                            else {
                                LOG.info( "name '" + name + "' is available" );
                                addRow( name, true );
                            }
                            redisplayRows();
                        }
                        else {
                            LOG.info( "name was null" );
                        }
                    }
                };
                new Thread( runner ).start();
            }
        });
        
        cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                DataTransferEditor.this.setDirty( false );
                close();
            }
        });
        
        okButton = new JButton( "OK" );
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if( addSet.size() > 0 || deleteSet.size() > 0 ) {
                    DataTransferEditor.this.setDirty( true );
                }
                close();
            }
        });
        
        buttonsPanel = new JPanel();
//        SpringLayout buttonsLayout = new SpringLayout();
        buttonsPanel.setLayout( new BoxLayout( buttonsPanel, BoxLayout.X_AXIS ) );
//        buttonsPanel.setLayout( buttonsLayout );
        buttonsPanel.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
        buttonsPanel.add( deleteButton );
        buttonsPanel.add( Box.createHorizontalStrut( 5 ) );
        buttonsPanel.add( addButton );
        buttonsPanel.add( new Box.Filler( new Dimension( 0, 0 ),
                new Dimension( Short.MAX_VALUE, 0 ), new Dimension( Short.MAX_VALUE, 0 ) ) );
        buttonsPanel.add( Box.createHorizontalStrut( 5 ) );
        buttonsPanel.add( cancelButton );
        buttonsPanel.add( Box.createHorizontalStrut( 5 ) );
        buttonsPanel.add( okButton );
        
        editorPanel = new JPanel();
        editorPanel.setLayout( new BorderLayout() );
        editingPanelTextEditor = new JEditorPane();
        editingPanelButtonsPanel = new JPanel();
        editingPanelCancelButton = new JButton( "Cancel" );
        editingPanelOKButton = new JButton( "Save Changes" );
        editingPanelButtonsPanel.add( editingPanelCancelButton );
        editingPanelButtonsPanel.add( editingPanelOKButton );
        setEditingRow( null );
        
        redisplayRows();
        
        variablesPanel.setBackground( r() );
//        JScrollPane p = new JScrollPane(
//                variablesPanel,
//                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        
        
        JPanel ui = new JPanel();
        SpringLayout layout = new SpringLayout();
//        ui.setLayout( layout );
        ui.setLayout( new BorderLayout() );
		ui.setBorder( new EmptyBorder( 2, 3, 2, 3 ) );
        
//        editorPanel.setBackground( r() );
//        buttonsPanel.setBackground( r() );
        
        
//        layout.putConstraint( SpringLayout.SOUTH, ui, 4, SpringLayout.SOUTH, deleteButton );
        
//        layout.putConstraint( SpringLayout.WEST, p, 3, SpringLayout.WEST, ui );
//        layout.putConstraint( SpringLayout.NORTH, p, 2, SpringLayout.NORTH, ui );
//        layout.putConstraint( SpringLayout.EAST, ui, 2, SpringLayout.EAST, p );
//        
//        layout.putConstraint( SpringLayout.WEST, editorPanel, 3, SpringLayout.WEST, ui );
//        layout.putConstraint( SpringLayout.NORTH, editorPanel, 3, SpringLayout.SOUTH, p );
//        layout.putConstraint( SpringLayout.EAST, editorPanel, 0, SpringLayout.EAST, p );
//        
//        layout.putConstraint( SpringLayout.WEST, buttonsPanel, 3, SpringLayout.WEST, ui );
//        layout.putConstraint( SpringLayout.NORTH, buttonsPanel, 3, SpringLayout.SOUTH, editorPanel );
//        layout.putConstraint( SpringLayout.EAST, buttonsPanel, 0, SpringLayout.EAST, editorPanel );
//        
//        layout.putConstraint( SpringLayout.SOUTH, ui, 0, SpringLayout.SOUTH, buttonsPanel );
        
        ui.add( variablesPanel, BorderLayout.NORTH );
//        ui.add( p, BorderLayout.NORTH );
        ui.add( editorPanel, BorderLayout.CENTER );
        ui.add( buttonsPanel, BorderLayout.SOUTH );
        
		return ui;
	}
    
    private void close() {
        try {
            setClosed( true );
        }
        catch( PropertyVetoException e ) {
            dispose();
        }
    }
    
    Random r = new Random();
    
    private Color r() {
        return new Color( r.nextInt( 255 ), r.nextInt( 255 ), r.nextInt( 255 ) );
    }
    
    private void addRow( String name, boolean newVar ) {
        VariableRow row = new VariableRow( name, !builtInVars.contains( name ) );
        VariableListener listener = new VariableListener( row );
        displayedRows.add( row );
        if( newVar ) {
            row.type = Variable.TYPE_TEXT;
            row.value = "";
            addSet.add( row );
            setDirty( true );
        }
    }
    
    private void restoreRow( VariableRow row ) {
        displayedRows.add( row );
        deletedRows.remove( row );
        if( deleteSet.contains( row ) ) {
            LOG.info( "variable being removed from delete set" );
            deleteSet.remove( row );
        }
        else {
            LOG.info( "variable being added to add set" );
            addSet.add( row );
        }
    }
    
    private void deleteRow( VariableRow row ) {
        displayedRows.remove( row );
        deletedRows.add( row );
        if( addSet.contains( row ) ) {
            LOG.info( "variable being removed from add set" );
            addSet.remove( row );
        }
        else {
            LOG.info( "variable being added to remove set" );
            deleteSet.add( row );
            setDirty( true );
        }
    }
    
    private VariableRow getDeletedRow( String name ) {
        for( VariableRow row : deletedRows ) {
            if( row.name.equals( name ) ) {
                return row;
            }
        }
        return null;
    }
    
    private VariableRow getDisplayedRow( String name ) {
        for( VariableRow row : displayedRows ) {
            if( row.name.equals( name ) ) {
                return row;
            }
        }
        return null;
    }
    
    private void redisplayRows() {
        LOG.info( "redisplaying rows" );
        variablesPanel.removeAll();
        variablesPanel.add( variableNameTitle );
        variablesPanel.add( transferSourceTitle );
        variablesPanel.add( transferVariableTitle );
        deleteButton.setEnabled( false );
        for( VariableRow row : displayedRows ) {
            row.checkBox.setSelected( false );
            row.checkBox.invalidate();
            variablesPanel.add( row.checkBox );
            variablesPanel.add( row.sources );
            variablesPanel.add( row.getDisplayedEditor() );
        }
        variablesPanel.invalidate();
        getContentPane().validate();
    }
    
    private class VariableRow implements Comparable {
        JCheckBox checkBox = new JCheckBox();
        JComboBox sources = new JComboBox();
        JComboBox variablesCombo = new JComboBox();
        JButton editValueButton = new JButton();
        String name;
        VariableListener listener;
        Object value;
        String type;
        WorkflowOperation.VariableMapping originalMapping;
        
        public VariableRow( String name, boolean dynamic ) {
            LOG.info( "creating variable row for variable '" + name + "' dynamic=" + dynamic );
            this.name = name;
            
            checkBox.setEnabled( dynamic );
            checkBox.setText( name );
            for( ComboItem item : choices.keySet() ) {
                sources.addItem( item );
            }
            if( inputMappings.get( name ) == null ) {
                sources.setSelectedItem( defaultItem );
            }
            else {
                String mapping = inputMappings.get( name ).getValue();
                try {
                    WorkflowOperation.VariableMapping m =
                        new WorkflowOperation.VariableMapping( mapping );
                    assert name.equals( m.elementID ) : "name (" + name + ") != elementID (" + m.elementID + ")";
                    if( task.getID().equals( m.taskID ) ) {
                        sources.setSelectedItem( defaultItem );
                    }
                    else {
                        originalMapping = m;
                        for( ComboItem choice : choices.keySet() ) {
                            if( choice.id != null && choice.id.equals( originalMapping.taskID ) ) {
                                sources.setSelectedItem( choice );
                            }
                        }
                    }
                }
                catch( IllegalArgumentException e ) {
                    // thrown if the mapping isn't a nexus workflow mapping
                    // TODO we may want to just display the mapping in this case and allow them to
                    // modify it directly, or to "reset" it to a valid nexus workflow mapping
                    LOG.warn( "Error reading input mapping for variable '" + name + "' (mapping="
                            + mapping + ")", e );
                    sources.setSelectedItem( defaultItem );
                }
            }
            
            resetVariablesCombo();
            
//            namePanel.setLayout( new BoxLayout( namePanel, BoxLayout.X_AXIS ) );
//            namePanel.add( checkBox );
//            namePanel.add( Box.createHorizontalStrut( 3 ) );
//            namePanel.add( label );
            
            editValueButton.setText( "Edit Initial Value" );
            
            resetEditorValue();
//            DataTransferEditor.this.addIsDirtyListener( editor );
        }
        
        public JComponent getDisplayedEditor() {
            if( sources.getSelectedItem() == defaultItem ) {
                LOG.info( "displayed editor for " + name + " is button " + editValueButton );
                return editValueButton;
            }
            else {
                LOG.info( "displayed editor for " + name + " is combo box " + variablesCombo );
                return variablesCombo;
            }
        }
        
        public void resetVariablesCombo() {
            LOG.info( "resetting variables combo for variable " + name );
            variablesCombo.removeAllItems();
            ComboItem item = (ComboItem) sources.getSelectedItem();
            assert item != null : "item was null";
            if( choices.get( item ) != null ) {
                LOG.info( "choices.get( item ) wasn't null" );
                for( String variable : choices.get( item ) ) {
                    LOG.info( "adding variable " + variable );
                    variablesCombo.addItem( variable );
                }
                if( originalMapping != null && originalMapping.taskID.equals( item.id ) ) {
                    variablesCombo.setSelectedItem( originalMapping.variableName );
                }
                else if( variablesCombo.getItemCount() > 0 ) {
                    LOG.info( "setting selected index to 0" );
                    variablesCombo.setSelectedIndex( 0 );
                }
                else {
                    variablesCombo.addItem( "" );
                    variablesCombo.setSelectedIndex( 0 );
                }
            }
        }
        
        public void resetEditorValue() {
            LOG.info( "resetting editor value for variable " + name );
            type = data.getType( name );
            if( type == null ) {
                value = null;
            }
            else if( type.equals( Variable.TYPE_TEXT ) ) {
                value = data.getPlain( name );
            }
            else if( type.equals( Variable.TYPE_BASE64 ) ) {
                value = data.getBase64( name );
            }
            else if( type.equals( Variable.TYPE_BINARY ) ) {
                try {
                    value = data.get( name );
                }
                catch( Exception e ) {
                    // shouldn't happen
                    LOG.error( "Error retrieving binary data for variable " + name );
                    value = null;
                }
            }
            else if( type.equals( Variable.TYPE_OBJECT ) ) {
                try {
                    value = data.get( name );
                }
                catch( Exception e ) {
                    LOG.error( "Error retrieving data for variable " + name, e );
                    value = null;
                }
            }
            else {
                type = null;
                value = null;
                LOG.error( "Invalid data type" );
            }
        }
        
        @Override
        public boolean equals( Object object ) {
            return name.equals( object.toString() );
        }
        
        public int compareTo( Object object ) {
            if( object != null ) {
                return this.name.compareTo( object.toString() );
            }
            else {
                return -1;
            }
        }
        
        public String toString() {
            return name;
        }
    }
    
    private class VariableListener implements ItemListener, ActionListener {
        private VariableRow row;
        public VariableListener( VariableRow row ) {
            this.row = row;
            row.listener = this;
            row.checkBox.addItemListener( this );
            row.sources.addItemListener( this );
            row.variablesCombo.addItemListener( this );
            row.editValueButton.addActionListener( this );
        }
        public void itemStateChanged( ItemEvent e ) {
            if( e.getSource() == row.checkBox ) {
                resetDeleteButton();
            }
            else if( e.getSource() == row.sources ) {
                if( e.getStateChange() == ItemEvent.SELECTED ) {
                    LOG.info( "setting dirty" );
                    DataTransferEditor.this.setDirty( true );
                    row.resetVariablesCombo();
                    redisplayRows();
                }
            }
            else if( e.getSource() == row.variablesCombo ) {
                if( e.getStateChange() == ItemEvent.SELECTED ) {
                    LOG.info( "setting dirty" );
                    DataTransferEditor.this.setDirty( true );
                }
            }
        }
        public void actionPerformed( ActionEvent e ) {
            if( e.getSource() == row.editValueButton ) {
                setEditingRow( row );
            }
            else if( e.getSource() == editingPanelCancelButton ) {
                setEditingRow( null );
            }
            else if( e.getSource() == editingPanelOKButton ) {
                // TODO get type from editor
                // for now, assume it's text
                row.type = Variable.TYPE_TEXT;
                row.value = editingPanelTextEditor.getText();
                setEditingRow( null );
            }
        }
    }
    
    private JPanel editingPanelButtonsPanel;
    private JEditorPane editingPanelTextEditor;
    private JButton editingPanelCancelButton;
    private JButton editingPanelOKButton;
    private ActionListener editingPanelListener;
    
    private void setEditingRow( VariableRow row ) {
        if( row == null ) {
            if( editingPanelListener != null ) {
                editingPanelCancelButton.removeActionListener( editingPanelListener );
                editingPanelOKButton.removeActionListener( editingPanelListener );
                editingPanelListener = null;
            }
            editorPanel.removeAll();
            editorPanel.setBorder( null );
            editorPanel.add( Box.createVerticalGlue(), BorderLayout.CENTER );
            setComponentsEnabled( true );
        }
        else {
            // TODO open different editors for different types of data, allow user to change data type
            if( row.value != null && row.value instanceof String ) {
                editingPanelTextEditor.setText( (String) row.value );
            }
            else {
                editingPanelTextEditor.setText( "" );
            }
            editorPanel.removeAll();
            editorPanel.setBorder( BorderFactory.createTitledBorder( "Edit value for variable " + row.name ) );
            editorPanel.add( Box.createVerticalStrut( 1 ), BorderLayout.NORTH );
            editorPanel.add( new JScrollPane( editingPanelTextEditor ), BorderLayout.CENTER );
            editorPanel.add( editingPanelButtonsPanel, BorderLayout.SOUTH );
            editingPanelCancelButton.addActionListener( row.listener );
            editingPanelOKButton.addActionListener( row.listener );
            editingPanelListener = row.listener;
            setComponentsEnabled( false );
        }
    }
    
    private void setComponentsEnabled( boolean enabled ) {
        this.setClosable( enabled );
        
        for( VariableRow row : displayedRows ) {
            row.checkBox.setEnabled( enabled && !builtInVars.contains( row.name ) );
            row.editValueButton.setEnabled( enabled );
            row.sources.setEnabled( enabled );
            row.variablesCombo.setEnabled( enabled );
        }
        
        if( enabled ) {
            resetDeleteButton();
        }
        else {
            deleteButton.setEnabled( false );
        }
        addButton.setEnabled( enabled );
        cancelButton.setEnabled( enabled );
        okButton.setEnabled( enabled );
    }
    
    protected void setDirty( boolean dirty ) {
        LOG.info( "setting dirty to " + dirty );
        super.setDirty( dirty );
        okButton.setEnabled( dirty );
    }
    
    private void resetDeleteButton() {
        boolean enabled = false;
        for( VariableRow row : displayedRows ) {
            if( row.checkBox.isSelected() ) {
                enabled = true;
            }
        }
        deleteButton.setEnabled( enabled );
    }
    
    protected Command getSaveChangesCommand() {
        NexusServiceData newData = new NexusServiceData();
        Set<SaveDataTransferChangesCommand.VariableMapping> mappings =
            new HashSet<SaveDataTransferChangesCommand.VariableMapping>();
        
        for( VariableRow row : displayedRows ) {
            SaveDataTransferChangesCommand.VariableMapping mapping;
            if( row.sources.getSelectedItem() == defaultItem ) {
                // just map regular value, no data transfer
                mapping = new SaveDataTransferChangesCommand.VariableMapping(
                        row.name, ((YTask) _proxy.getData()).getID(), row.name );
                mappings.add( mapping );
            }
            else {
                // map data transfer
                ComboItem item = (ComboItem) row.sources.getSelectedItem();
                String sourceVar = (String) row.variablesCombo.getSelectedItem();
                mapping = new SaveDataTransferChangesCommand.VariableMapping(
                        row.name, item.id, sourceVar );
                mappings.add( mapping );
            }
            if( row.type != null ) {
                try {
                    newData.setType( row.name, row.type );
                    newData.set( row.name, row.value );
                }
                catch( Exception e ) {
                    LOG.error( "Error saving value for variable " + row.name, new Exception().fillInStackTrace() );
                }
            }
            else {
                if( row.value != null ) {
                    newData.setPlain( row.name, row.value.toString() );
                }
            }
        }
        
        Command c = new SaveDataTransferChangesCommand( _proxy, newData, mappings );
        
        return c;
    }

	/**
	 * @see ComponentEditor#setUI(JComponent)
	 */
	protected void setUI( JComponent component ) {
		this.getContentPane().add( component );
	}

	/**
	 * @see ComponentEditor#saveAttributes()
	 */
	public void saveAttributes() {
        // Empty.
	}

}
