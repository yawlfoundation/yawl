package com.nexusbpm.editor.desktop;

import java.io.Serializable;

import javax.swing.event.InternalFrameEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.editors.ComponentEditor;
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

	private EditorDataProxy _proxy;

	private ComponentEditor _editor;

	/**
	 * Creates a component editor frame listener for the component specified by
	 * the given node and editor.
	 * @param proxy the <tt>DataProxy</tt> for the component.
	 * @param editor the <tt>ComponentEditor</tt> for the component.
	 */
	public ComponentEditorFrameListener( EditorDataProxy proxy, ComponentEditor editor ) {
		super( editor );

		if( LOG.isDebugEnabled() ) {
			LOG.debug( "ComponentEditorFrameListener construct editor=" + ( editor == null ? "null" : editor.getClass().getName() ) );
		}

		_proxy = proxy;
		_editor = editor;
	}
    
    /**
     * When the editor is closed, saves the attributes in the editor and
     * alerts the proxy (so that it no longer holds onto the editor and
     * creates and returns a new editor next time it is asked for its
     * editor).
     * @see ClosingFrameListener#internalFrameClosing(InternalFrameEvent)
     */
    public void internalFrameClosing( InternalFrameEvent e ) {
        super.internalFrameClosing( e );
        if( _proxy != null ) {
            Worker worker = new CloseWorker();
            GlobalEventQueue.add( worker );
        }
    }

	/**
	 * When the editor is closed, saves the attributes in the editor and
     * alerts the proxy (so that it no longer holds onto the editor and
     * creates and returns a new editor next time it is asked for its
     * editor).
	 * @see ClosingFrameListener#internalFrameClosed(InternalFrameEvent)
	 */
	public void internalFrameClosed( InternalFrameEvent e ) {
		super.internalFrameClosed( e );
        if( _proxy != null ) {
        	Worker worker = new CloseWorker();
        	GlobalEventQueue.add( worker );
        }
	}
    
    private class CloseWorker implements Worker {
        public String getName() {return "closing frame";}
        public void execute() {
            EditorDataProxy proxy = _proxy;
            proxy.editorClosed();
            try {
                if( _editor.isDirty() ) {
                    LOG.debug( "Editor closing, saving: " );
                    _editor.saveAttributes();
                    _editor.persistAttributes();
                    // ClientOperation.update( controller, 3, false );
                }//if
                else {
                    LOG.debug( "Editor closing, NOT saving: (" + "isDirty=" + _editor.isDirty() );
                }
            }//try
            catch( EditorException exc ) {
                LOG.error( "Error saving attributes!", exc );
            }//catch
            finally {
                try {
                    if( null != _editor ) {
                        ComponentEditor tmp = _editor;
                        _editor = null;
                        tmp.frameClosed();
                    }
                }
                catch( Throwable throwable ) {
                    LOG.warn( "ComponentEditorFrameListener.run clearing editor", throwable );
                }
                _proxy = null;
            }//finally
        }//execute()
    };//new CapselaWorker()

	/**
	 * Initializes the editor and disables the close button while the
     * editor is opening.
	 * @see ClosingFrameListener#internalFrameOpened(InternalFrameEvent)
	 */
	public void internalFrameOpened( final InternalFrameEvent e ) {
		final EditorDataProxy proxy = _proxy;
		LOG.debug( "internalFrameOpened()" );
		Worker worker = new Worker( ) {
			public String getName() {return "open internal frame";}
			public void execute() {
				// Don't allow the user to close the editor while it's loading.
//				boolean wasClosable = _editor.isClosable();
                assert _editor.isClosable() : "editor not closeable";
				_editor.setClosable( false );
				try {
                    _editor.setProxy( proxy );
					_editor.initialize();
				}//try
				catch( Exception e ) {
					LOG.error( "Error initializing UI!", e );
				}//catch
				finally {
					try {
						callSuperInternalFrameOpened( e );
					}//try
					finally {
						_editor.setClosable( true );
					}//finally
				}//finally
//				return null;
			}//run()
		};
		GlobalEventQueue.add( worker );
	}

	private void callSuperInternalFrameOpened( InternalFrameEvent e ) {
		super.internalFrameOpened( e );
	}
}