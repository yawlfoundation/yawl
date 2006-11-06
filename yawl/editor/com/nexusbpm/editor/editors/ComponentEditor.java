/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.command.Command;
import com.nexusbpm.command.SaveTaskChangesCommand;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.desktop.CapselaInternalFrame;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.util.syntax.DefaultInputHandler;
import com.nexusbpm.editor.util.syntax.InputHandler;
import com.nexusbpm.editor.util.syntax.JEditTextArea;
import com.nexusbpm.editor.util.syntax.JeditHelper;
import com.nexusbpm.editor.util.syntax.TextAreaDefaults;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * Superclass for all component editors.
 *
 * @author catch23
 * @created October 28, 2002
 */
public abstract class ComponentEditor extends CapselaInternalFrame implements DataProxyStateChangeListener {

	private static final Log LOG = LogFactory.getLog( ComponentEditor.class );

	private boolean _dirty;
	private boolean _uiInitialized;
    
    protected NexusServiceData data;
    public void proxyDetaching(DataProxy proxy, Object data, DataProxy parent) {}
    public void proxyDetached(DataProxy proxy, Object data, DataProxy parent) {}
    public void proxyAttaching(DataProxy proxy, Object data, DataProxy parent) {}
    public void proxyAttached(DataProxy proxy, Object data, DataProxy parent) {}

    
	/**
	 * The controller for this editor's component.
	 */
	protected EditorDataProxy _proxy;
    
    /**
     * Initializes the editor, performing editor-specific transfer of data
     * from the {@link NexusServiceData} into the editor. Note that
     * {@link #setProxy(EditorDataProxy)}
     * should be called first so that the initialization process can access
     * the component's data.
     * <p>
     * This method is thread-safe, ie, you may call it from a thread other
     * than the AWT event dispatcher thread and UI updates triggered by this
     * method will still occur on the AWT event dispatcher thread.
     *
     * @throws EditorException if there is a GUI error.
     */
    public final void initialize() throws EditorException {
        if( _uiInitialized == false ) {
            _uiInitialized = true;
            final JComponent ui = this.initializeUI();
            Runnable uiUpdater = new Runnable() {
                public void run() {
                    try {
                        ComponentEditor.this.removeLoadingLabel();
                        ComponentEditor.this.setUI( ui );
                        ComponentEditor.this.validate();
                    }
                    catch( EditorException e ) {
                        LOG.error( e.getMessage(), e );
                    }
                }
            };
            if( SwingUtilities.isEventDispatchThread() ) {
                uiUpdater.run();
            }
            else {
                SwingUtilities.invokeLater( uiUpdater );
            }
        }
        else {
            LOG.warn( "ComponentEditor.initialize() is being called again!" );
        }
    }

	/**
	 * Action listener that can be added to components to set the editor
	 * to dirty when the component is used.
	 */
	private ActionListener isDirtyActionListener = new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
			LOG.debug( "isDirtyActionListener.actionPerformed: " + e.paramString() );
			ComponentEditor.this.setDirty( true );
			System.out.println("pressed a key on the editor");
		}
	};

	/**
	 * Key adapter that can be added to components to set the editor
	 * to dirty when a component is used.
	 */
	private KeyAdapter isDirtyKeyAdapter = new KeyAdapter() {
		public void keyPressed( KeyEvent e ) {
			LOG.debug( "isDirtyKeyAdapter.keyPressed: " + e.paramString() );
			ComponentEditor.this.setDirty( true );
			System.out.println("pressed a key on the editor");
		}
	};

	/**
	 * Input handler that can be added to JEditTextArea's to set the editor to
	 * dirty when the JEditTextArea is used. We cannot use KeyListener's
	 * because JEditTextArea's shortcircuit the KeyListener mechanism and use
	 * InputHandler's directly as a speed optimization.
	 */
	private InputHandler isDirtyInputHandler = new CustomInputHandler();

	private class CustomInputHandler extends DefaultInputHandler {
		public CustomInputHandler() {
			this.addDefaultKeyBindings();
		}
		public void keyPressed( KeyEvent e ) {
			LOG.debug( "isDirtyInputHandler.keyPressed: " + e.paramString() );
			ComponentEditor.this.setDirty( true );
			super.keyPressed( e );
			System.out.println("pressed a key on the editor");
		}
	}

//	/**
//	 * Drop panel listener that can be added to calendar combo boxes to set the
//	 * editor to dirty when a calendar combo box is used.
//	 */
//	private ChangeListener isDirtyChangeListener = new ChangeListener() {
//		public void stateChanged( ChangeEvent e ) {
//			LOG.debug( "isDirtyChangeListener.stateChanged: " + e );
//			ComponentEditor.this.setDirty( true );
//			System.out.println("pressed a key on the editor");
//		}
//	};

	/**
	 * Adds an appropriate listener to the specified component, such that when
	 * the component is used, the editor is set to dirty. A dirty editor is one
	 * whose component has been modified, and thus needs the component to be
	 * saved when the editor is closed.
	 *
	 * @param o the GUI component.
	 * @see ComponentEditor#isDirty()
	 */
	protected void addIsDirtyListener( Object o ) {
		if( o instanceof JTextField ) {
			((JTextField) o).addKeyListener( this.isDirtyKeyAdapter );
		}
		else if( o instanceof JEditorPane ) {
			((JEditorPane) o).addKeyListener( this.isDirtyKeyAdapter );
		}
		else if( o instanceof JEditTextArea ) {
			((JEditTextArea) o).setInputHandler( this.isDirtyInputHandler );
		}
		else if( o instanceof JComboBox ) {
			((JComboBox) o).addActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JCheckBox ) {
			((JCheckBox) o).addActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JRadioButton ) {
			((JRadioButton) o).addActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JButton ) {
			((JButton) o).addActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JList ) {
			((JList) o).addKeyListener( this.isDirtyKeyAdapter );
		}
//        else if( o instanceof JCalendarComboBox ) {
//            ((JCalendarComboBox) o).addChangeListener( this.isDirtyChangeListener );
//        }
//        else if( o instanceof JPVTime ) {
//            ((JPVTime) o).addKeyListener( this.isDirtyKeyAdapter );
//        }
//        else if( o instanceof DropPanel ) {
//            ((DropPanel) o).addDropPanelListener( this.isDirtyDropPanelListener );
//        }
//        else if( o instanceof PreferencesModel ) {
//            ((PreferencesModel) o).addListener( this.isDirtyActionListener );
//        }
//        else if( o instanceof JPVEdit ) {
//            ((JPVEdit) o).addActionListener( this.isDirtyActionListener );
//        }
//        else if( o instanceof RecurrencePanel ) {
//            ((RecurrencePanel) o).addActionListener( this.isDirtyActionListener );
//        }
		else {
			LOG.error( "Unable to add an isDirty listener to: " + o );
		}
	}

	/**
	 * Removes the listener for the given GUI component that marks this editor
	 * as dirty whenever the given component is modified.
	 *
	 * @param o the GUI component whose listener should be removed.
	 */
	protected void removeIsDirtyListener( Object o ) {
		if( null == o ) return;

		if( o instanceof JTextField ) {
			((JTextField) o).removeKeyListener( this.isDirtyKeyAdapter );
		}
		else if( o instanceof JEditorPane ) {
			((JEditorPane) o).removeKeyListener( this.isDirtyKeyAdapter );
		}
		else if( o instanceof JEditTextArea ) {
			((JEditTextArea) o).setInputHandler( TextAreaDefaults.getDefaults().inputHandler );
		}
		else if( o instanceof JComboBox ) {
			((JComboBox) o).removeActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JCheckBox ) {
			((JCheckBox) o).removeActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JRadioButton ) {
			((JRadioButton) o).removeActionListener( this.isDirtyActionListener );
		}
		else if( o instanceof JButton ) {
			((JButton) o).removeActionListener( this.isDirtyActionListener );
		}
        else if( o instanceof JList ) {
            ((JList) o).removeKeyListener( this.isDirtyKeyAdapter );
        }
		else {
			LOG.error( "Unable to remove an isDirty listener to: " + o );
		}
	}

	/**
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
		String property = event.getPropertyName();
		if( property.equals( DataProxyStateChangeListener.PROPERTY_NAME ) ) {
            resetTitle( _proxy );
		}
        else if( property.equals( DataProxyStateChangeListener.PROPERTY_TASK_VARIABLES ) ) {
            data = (NexusServiceData) event.getNewValue();
        }
	}
    
    public void resetTitle( DataProxy proxy ) {
        String name = proxy.getLabel() + getIdString( proxy.getData() );
        if( isDirty() ) name += "*";
        setTitle( name );
        LOG.debug( "RENAMING INTERNAL FRAME TITLE: " + name );
    }
    
    private String getIdString( Object data ) {
        if( data instanceof YExternalNetElement ) {
            YExternalNetElement netElement = (YExternalNetElement) data;
            return " (ID: " + netElement.getID() + ")";
        } else if( data instanceof YDecomposition ) {
            YDecomposition decomp = (YDecomposition) data;
            return " (ID: " + decomp.getId() + ")";
        } else if( data instanceof YSpecification ) {
            YSpecification spec = (YSpecification) data;
            return " (ID: " + spec.getID() + ")";
        } else {
            return "";
        }
    }

	/**
	 * Sets the controller of the component that this editor is for.
	 *
	 * @param proxy the proxy for this editor's component.
	 */
	public final void setProxy( EditorDataProxy proxy ) {
		if( _proxy != null ) {
			LOG.debug( "ComponentEditor.setController - overriding controller" );
			_proxy.removeChangeListener( this );
		}
		_proxy = proxy;
        addListeningProxy( _proxy );
        if( _proxy.getData() instanceof YAtomicTask ) {
            YAtomicTask task = (YAtomicTask) _proxy.getData();
            data = NexusServiceData.unmarshal( task, false );
        }
	}

	/**
	 * Initializes and returns the interface for this editor. This method can take as much time as it wants,
	 * because while it is executing, the loading icon will be displayed to the user.
	 *
	 * @return The editor's user interface.
	 * @throws EditorException If something goes wrong initializing the editor interface.
	 */
	public abstract JComponent initializeUI() throws EditorException;

	/**
	 * Uses the specified Swing component to set the editor's user inteface.
	 *
	 * @param component The Swing component to use for the editor's user inteface.
	 * @throws EditorException If something goes wrong using the specified Swing component.
	 */
	protected abstract void setUI( JComponent component ) throws EditorException;

	/**
	 * Performs editor-specific transfer of data from the editor into the
	 * {@link NexusServiceData}. Note that {@link #persistAttributes()}
     * should be called after this method has finished executing to
     * propagate the changes into the actual workflow specification.
	 *
	 * @throws EditorException if there is an error saving the attributes.
	 */
	public abstract void saveAttributes() throws EditorException;
    
    public final void persistAttributes() {
        Command saveCommand = getSaveChangesCommand();
        if( saveCommand != null ) {
            WorkflowEditor.getExecutor().executeCommand( saveCommand );
        }
        else if( _proxy.getData() instanceof YTask ) {
            WorkflowEditor.getExecutor().executeCommand( new SaveTaskChangesCommand( _proxy, data ) );
        }
        else {
            LOG.debug( "Not persisting changes" );
        }
    }
    
    /**
     * Override this in a particular editor to provide a custom persistence command.
     */
    protected Command getSaveChangesCommand() {
        return null;
    }

	/**
	 * Returns <tt>true</tt> if the editor was used to modify the corresponding
	 * component.
	 *
	 * @return whether the component has been modified.
	 */
	public boolean isDirty() {
		return _dirty;
	}

	/**
	 * Sets whether or not the editor has been used to modify the corresponding
	 * component.
	 *
	 * @param dirty whether the component has been modified.
	 */
	protected void setDirty( boolean dirty ) {
        if( _dirty != dirty ) {
            _dirty = dirty;
            resetTitle( _proxy );
        }
	}

	/**
	 * Removes this as a property change listener of this node's domain object.
	 *
	 * @throws Throwable not thrown in the code.
	 */
	public void frameClosed() throws Exception {
        // TODO XXX
		if( LOG.isDebugEnabled() ) {
			LOG.debug( "ComponentEditor.clear " + getClass().getName() );
		}
        
        for( DataProxy proxy : new ArrayList<DataProxy>( listeningProxies ) ) {
            removeListeningProxy( proxy );
        }

		if( null != _proxy ) {
            removeListeningProxy( _proxy );
		}

		setFrameIcon( null );

//		removeEditorFrameListeners();

		JeditHelper.removeListeners( this );

		// may not be strictly necessary but makes it a little easier to see in profile what is left
		isDirtyActionListener = null;
//		isDirtyChangeListener = null;
		isDirtyKeyAdapter = null;

		super.frameClosed();
	}
    
    private List<DataProxy> listeningProxies = new ArrayList<DataProxy>();
    
    public void addListeningProxy( DataProxy proxy ) {
        if( listeningProxies.contains( proxy ) ) {
            new RuntimeException( "Adding listener twice!" ).fillInStackTrace().printStackTrace();
        }
        proxy.addChangeListener( this );
        listeningProxies.add( proxy );
    }
    
    public void removeListeningProxy( DataProxy proxy ) {
        proxy.removeChangeListener( this );
        listeningProxies.remove( proxy );
    }


//	/**
//	 * Cleans up any resources held by this editor. This method calls {@link #clear()}
//     * and {@link CapselaInternalFrame#removeEditorFrameListeners()}.
//	 *
//	 * @throws Throwable declared by superclass definition of finalize() but
//	 *                   not thrown in the code.
//	 */
//	public final void finalize() throws Throwable {
//		if( LOG.isDebugEnabled() ) {
//			LOG.debug( "ComponentEditor.finalize " + getClass().getName() );
//		}
//		clear();
//
//		// need to delay this so if deleting component but component on desktop - this will be handled correctly
//		// though with hack on ClosingFrameListener - menu will be updated in ClosingFrameListener.finalize
//		removeInternalFrameListeners();
//
//		super.finalize();
//	}

}
