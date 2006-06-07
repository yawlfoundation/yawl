package com.nexusbpm.editor.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.util.syntax.DefaultInputHandler;
import com.nexusbpm.editor.util.syntax.InputHandler;
import com.nexusbpm.editor.util.syntax.JEditTextArea;
import com.nexusbpm.editor.util.syntax.JeditHelper;
import com.nexusbpm.editor.util.syntax.TextAreaDefaults;

/**
 * Superclass for all component editors.
 *
 * @author catch23
 * @created October 28, 2002
 */
public abstract class ComponentEditor extends CapselaInternalFrame implements PropertyChangeListener {

	private static final Log LOG = LogFactory.getLog( ComponentEditor.class );

	private static final ImageIcon ICON_RED_LOCK = ApplicationIcon.getIcon( "ComponentEditor.red_lock" );
	private static final ImageIcon ICON_GREEN_LOCK = ApplicationIcon.getIcon( "ComponentEditor.red_lock" );

	private boolean _dirty;
	private boolean _uiInitialized;

	/**
	 * The controller for this editor's component.
	 */
	protected EditorDataProxy _proxy;

	/**
	 * Action listener that can be added to components to set the editor
	 * to dirty when the component is used.
	 */
	private ActionListener isDirtyActionListener = new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
			LOG.debug( "isDirtyActionListener.actionPerformed: " + e.paramString() );
			ComponentEditor.this.setDirty( true );
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
		}
	}

	/**
	 * Drop panel listener that can be added to calendar combo boxes to set the
	 * editor to dirty when a calendar combo box is used.
	 */
	private ChangeListener isDirtyChangeListener = new ChangeListener() {
		public void stateChanged( ChangeEvent e ) {
			LOG.debug( "isDirtyChangeListener.stateChanged: " + e );
			ComponentEditor.this.setDirty( true );
		}
	};

	/**
	 * Disables all input elements in the editor if the edited component
	 * is locked by someone else.
	 */
	protected void disableInputElementsIfLockedBySomeoneElse() {
		throw new RuntimeException("needs a new context for yawl");
	}

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
		else {
			LOG.error( "Unable to remove an isDirty listener to: " + o );
		}
	}

	/**
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
		String property = event.getPropertyName();
		if( property.equals( "name" ) ) {
			String name = (String) event.getNewValue();
			setTitle( name );
			LOG.debug( "RENAMING INTERNAL FRAME TITLE: " + name );
		}
		throw new RuntimeException("editor title names may need a new context in YAWL");
	}

	/**
	 * Sets the controller of the component that this editor is for.
	 * Performs editor-specific transfer of data from the component into the
	 * editor.
	 * <p>
	 * This method is thread-safe, ie, you may call it from a thread other than
	 * the AWT event dispatcher thread and UI updates triggered by this method
	 * will still occur on the AWT event dispatcher thread.
	 *
	 * @param controller the controller for this editor's component.
	 * @throws EditorException if there is a GUI error.
	 */
	public void setController( EditorDataProxy proxy ) throws EditorException {
		if( _proxy != null ) {
			LOG.debug( "ComponentEditor.setController - overriding controller" );
			_proxy.removePropertyChangeListener( this );
		}
		_proxy = proxy;
		_proxy.addPropertyChangeListener( this );
		if( _uiInitialized == false ) {
			_uiInitialized = true;
			final JComponent ui = this.initializeUI();
			Runnable uiUpdater = new Runnable() {
				public void run() {
					try {
						ComponentEditor.this.removeLoadingLabel();
						ComponentEditor.this.setUI( ui );
						ComponentEditor.this.disableInputElementsIfLockedBySomeoneElse();
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
	}

	/**
	 * Initializes and returns the interface for this editor. This method can take as much time as it wants,
	 * because while it is executing, the loading icon will be displayed to the user.
	 *
	 * @return The editor's user interface.
	 * @throws EditorException If something goes wrong initializing the editor interface.
	 */
	protected abstract JComponent initializeUI() throws EditorException;

	/**
	 * Uses the specified Swing component to set the editor's user inteface.
	 *
	 * @param component The Swing component to use for the editor's user inteface.
	 * @throws EditorException If something goes wrong using the specified Swing component.
	 */
	protected abstract void setUI( JComponent component ) throws EditorException;

	/**
	 * Performs editor-specific transfer of data from the editor into the
	 * component.
	 *
	 * @throws EditorException if there is an error saving the attributes.
	 */
	public abstract void saveAttributes() throws EditorException;

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
		_dirty = dirty;
	}

	/**
	 * Removes this as a property change listener of this node's domain object.
	 *
	 * @throws Throwable not thrown in the code.
	 */
	public void clear() throws Throwable {
		if( LOG.isDebugEnabled() ) {
			LOG.debug( "ComponentEditor.clear " + getClass().getName() );
		}

		if( null != _proxy ) {
			_proxy.removePropertyChangeListener( this );

// nulling controller is really bad, it makes saveAttributes fail - understanding why would make me feel better
//			_proxy = null;

		}

		setFrameIcon( null );

//		removeEditorFrameListeners();

		JeditHelper.removeListeners( this );

		// may not be strictly necessary but makes it a little easier to see in profile what is left
		isDirtyActionListener = null;
		isDirtyChangeListener = null;
		isDirtyKeyAdapter = null;

		super.clear();

/*
		if (!isDirty()) {
			_proxy = null;  // 2005-05-16 15:08 mjf
		}
*/
	}


	/**
	 * Cleans up any resources held by this editor. If this method is overriden
	 * by a subclass, it MUST call this method (ie, <tt>super.finalize()</tt>).
	 *
	 * @throws Throwable declared by superclass definition of finalize() but
	 *                   not thrown in the code.
	 */
	public void finalize() throws Throwable {
		if( LOG.isDebugEnabled() ) {
			LOG.debug( "ComponentEditor.finalize " + getClass().getName() );
		}
		clear();

		// need to delay this so if deleting component but component on desktop - this will be handled correctly
		// though with hack on ClosingFrameListener - menu will be updated in ClosingFrameListener.finalize
		removeEditorFrameListeners();

		super.finalize();
	}

}
