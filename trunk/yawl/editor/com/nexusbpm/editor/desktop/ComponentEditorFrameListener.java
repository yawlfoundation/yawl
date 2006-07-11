package com.nexusbpm.editor.desktop;

import java.io.Serializable;

import javax.swing.event.InternalFrameEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.worker.GlobalEventQueue;
import com.nexusbpm.editor.worker.Worker;

/**
 * Frame listener for component editors.
 * 
 * @author catch23
 * @author Daniel Gredler
 * @created May 25, 2004	
 */
public class ComponentEditorFrameListener extends ClosingFrameListener implements Serializable {

	private static final Log LOG = LogFactory.getLog( ComponentEditorFrameListener.class );

	private EditorDataProxy _node;

	private ComponentEditor _editor;

	/**
	 * Creates a component editor frame listener for the component specified by
	 * the given node and editor.
	 * @param node the <tt>ComponentNode</tt> for the component.
	 * @param editor the <tt>ComponentEditor</tt> for the component.
	 */
	public ComponentEditorFrameListener( EditorDataProxy node, ComponentEditor editor ) {
		super( editor );

		if( LOG.isDebugEnabled() ) {
			LOG.debug( "ComponentEditorFrameListener construct editor=" + ( editor == null ? "null" : editor.getClass().getName() ) );
		}

		_node = node;
		_editor = editor;
	}

	/**
	 * When the editor is closed, it should release the lock and nullify the
	 * editor (to initiate garbage collection on it since some editors are
	 * huge... like the chart editor). It will also remove itself from the list
	 * of open windows menu on the capsela client itself.
	 * @see ClosingFrameListener#internalFrameClosing(InternalFrameEvent)
	 */
	public void internalFrameClosing( InternalFrameEvent e ) {
		super.internalFrameClosing( e );
		Worker worker = new Worker( ) {
			public String getName() {return "closing frame";}
			public void execute() {
				EditorDataProxy proxy = _node;
				try {
					if( _editor.isDirty() ) {
						LOG.debug( "Editor closing, saving: " );
						_editor.saveAttributes();
						// ClientOperation.update( controller, 3, false );
						throw new RuntimeException("implement the save operation, or prompt to save?");
					}//if
					else {
						LOG.debug( "Editor closing, NOT saving: (" + "isDirty=" + _editor.isDirty() );
					}
//					_node.nullifyEditors();
				}//try
				catch( EditorException ce ) {
					// CapselaException is already logged.
				}//catch
				finally {
					try {
						if( null != _editor ) {
							ComponentEditor tmp = _editor;
							_editor = null;
							tmp.clear();
						}
					}
					catch( Throwable throwable ) {
						LOG.warn( "ComponentEditorFrameListener.run clearing editor", throwable );
					}

					_node = null;
				}//finally
//				return null;
			}//run()
		};//new CapselaWorker()
		GlobalEventQueue.add( worker );
		Exception ex = new RuntimeException("TODO: (Output only) create global event queue (Executor) and add worker");
		LOG.error(ex.getMessage(), ex);
	}

	/**
	 * Attempts to acquire a lock on the component that the editor is for and
	 * disables the close button while the editor is opening.
	 * @see ClosingFrameListener#internalFrameOpened(InternalFrameEvent)
	 */
	public void internalFrameOpened( final InternalFrameEvent e ) {
		final EditorDataProxy proxy = _node;
		LOG.debug( "internalFrameOpened()" );
		Worker worker = new Worker( ) {
			public String getName() {return "open internal frame";}
			public void execute() {
				// Don't allow the user to close the editor while it's loading.
				boolean wasClosable = _editor.isClosable();
				_editor.setClosable( false );
				try {
					_editor.setProxy( proxy );
				}//try
				catch( EditorException ce ) {
					// CapselaExceptions are already logged.
				}//catch
				finally {
					try {
						callSuperInternalFrameOpened( e );
					}//try
					finally {
						// Now that we're done loading the frame, we can go back to allowing the
						// user to close it. Do it in a finally block so it happens no matter what.
						_editor.setClosable( wasClosable );
					}//finally
				}//finally
//				return null;
			}//run()
		};
		GlobalEventQueue.add( worker );
		Exception ex = new RuntimeException("Create global event queue (Executor) and add worker");
		LOG.error("TODO:" + ex.getMessage(), ex);
	}

	private void callSuperInternalFrameOpened( InternalFrameEvent e ) {
		super.internalFrameOpened( e );
	}

	/**
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug( "CompomentEditorFrameListener.finalize" );
		_node = null;
		_editor = null;
		super.finalize();

	}
}